package lo.sharon.tradeengine.service.impl;

import com.google.common.util.concurrent.*;
import lo.sharon.tradeengine.common.queue.TradeEngineQueueProducer;
import lo.sharon.tradeengine.common.queue.TradeEngineQueueViewer;
import lo.sharon.tradeengine.common.queue.factory.TradeEngineQueueFactory;
import lo.sharon.tradeengine.constant.OrderSide;
import lo.sharon.tradeengine.constant.OrderStatus;
import lo.sharon.tradeengine.constant.OrderType;
import lo.sharon.tradeengine.dao.CurrentPriceDao;
import lo.sharon.tradeengine.dao.OrderBookDao;
import lo.sharon.tradeengine.dao.TransactionRepository;
import lo.sharon.tradeengine.dto.OrderRequest;
import lo.sharon.tradeengine.model.Order;
import lo.sharon.tradeengine.model.RedisConsumerGroupInfo;
import lo.sharon.tradeengine.model.Transaction;
import lo.sharon.tradeengine.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;


import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    private TradeEngineQueueFactory tradeEngineQueueFactory;
    @Autowired
    private OrderBookDao orderBookDao;

    @Autowired
    private CurrentPriceDao currentPriceDao;

    @Autowired
    private TransactionRepository transactionRepository;


    private ListeningExecutorService listeningExecService;
    @PostConstruct
    void init(){
        ExecutorService execService = Executors.newCachedThreadPool();
        listeningExecService = MoreExecutors.listeningDecorator(execService);
    }
    @Override
    public String putOrderToPendingOrderQueue(OrderRequest orderRequest){
        TradeEngineQueueProducer tradeEngineQueueProducer = tradeEngineQueueFactory.createPendingOrderQueueProducer();
        String orderId = tradeEngineQueueProducer.send(orderRequest);
        return orderId;
    }

    @Override
    public List<Order> getOrdersByStatus(OrderStatus orderStatus) {
        if(orderStatus.name().equals(OrderStatus.PENDING_IN_QUEUE.name())){
            return this.getPendingInQueueOrders();
        }
        return null;
    }
    @Override
    public synchronized void matchOrder(Order orderFromQueue){
        OrderSide orderFromQueueOrderSide = orderFromQueue.getSide();
        OrderType orderType = orderFromQueue.getType();
        Long priceToBeMatch = orderType.equals(OrderType.MARKET) ? currentPriceDao.getCurrentPrice() : orderFromQueue.getPrice();
        OrderSide orderFromOrderBookOrderSide = orderFromQueueOrderSide.equals(OrderSide.SELL) ? OrderSide.BUY : OrderSide.SELL;


        // INITIAL
        Long restOfOrderFromQueueQty = orderFromQueue.getQuantity();
        log.info("[MATCH ORDER][INIT] orderFromQueue 委託數量: {}, 委託價格: {}", restOfOrderFromQueueQty, priceToBeMatch);
        boolean isMarketOrderInOrderBook = orderBookDao.getSize(orderFromOrderBookOrderSide, true, null) > 0 ? true : false;
        log.info("[MATCH ORDER] is market order in orderBook: {}", isMarketOrderInOrderBook);

        while(isMarketOrderInOrderBook && restOfOrderFromQueueQty > 0){
            Optional<Order> marketOrderFromOrderBook = orderBookDao.popFromHead(orderFromOrderBookOrderSide, true, null);
            restOfOrderFromQueueQty = doExchange(orderFromQueue, marketOrderFromOrderBook.get(), priceToBeMatch);
            log.info("[MATCH ORDER] rest of order from queue after exchange: {}", restOfOrderFromQueueQty);

            orderFromQueue.setQuantity(restOfOrderFromQueueQty);
            isMarketOrderInOrderBook = orderBookDao.getSize(orderFromOrderBookOrderSide, true, null) > 0 ? true : false;
        }
        log.info("[MATCH ORDER] is market order in orderBook: {}", isMarketOrderInOrderBook);
        log.info("[MATCH ORDER] rest of order from queue after compare with market order: {}", restOfOrderFromQueueQty);
        if (restOfOrderFromQueueQty == 0) {
            return;
        }

        // INITIAL
        boolean isLimitOrderInOrderBook = orderBookDao.getSize(orderFromOrderBookOrderSide, false, priceToBeMatch) > 0 ? true : false;
        log.info("[MATCH ORDER] is limit order in orderBook] {}", isLimitOrderInOrderBook);

        while(isLimitOrderInOrderBook && restOfOrderFromQueueQty > 0){
            Optional<Order> limitOrderFromOrderBook = orderBookDao.popFromHead(orderFromOrderBookOrderSide, false, priceToBeMatch);
            restOfOrderFromQueueQty = doExchange(orderFromQueue, limitOrderFromOrderBook.get(), priceToBeMatch);
            log.info("[MATCH ORDER] rest of order from queue after exchange: {}", restOfOrderFromQueueQty);

            orderFromQueue.setQuantity(restOfOrderFromQueueQty);
            isLimitOrderInOrderBook = orderBookDao.getSize(orderFromOrderBookOrderSide, false, priceToBeMatch) > 0 ? true : false;
        }

        log.info("[MATCH ORDER] is limit order in orderBook {}", isLimitOrderInOrderBook);
        log.info("[MATCH ORDER] rest of order from queue after compare with limit order: {}", restOfOrderFromQueueQty);
        if(restOfOrderFromQueueQty > 0){
            orderFromQueue.setQuantity(restOfOrderFromQueueQty);
            orderBookDao.pushToTail(orderFromQueue);
        }
    }
    private List<Order> getPendingInQueueOrders(){
        // getLastDeliveredId
        TradeEngineQueueViewer tradeEngineQueueViewer = tradeEngineQueueFactory.createPendingOrderQueueViewer();
        List<RedisConsumerGroupInfo> redisConsumerGroupsInfo = tradeEngineQueueViewer.viewConsumerGroupInfo();
        String lastConsumedOrderId = redisConsumerGroupsInfo.get(0).getLastDeliveredId();

        // get orders behind lastDeliveredId
        List<Order> pendingInQueueOrders = tradeEngineQueueViewer.viewPendingInQueueItems(lastConsumedOrderId, Order.class);
        return pendingInQueueOrders;
    }

    private Long doExchange(Order orderFromQueue, Order orderFromOrderBook, Long matchPrice) {
        Long orderFromQueueQty = orderFromQueue.getQuantity();
        Long orderFromOrderBookQty = orderFromOrderBook.getQuantity();
        Long exchangeQty = orderFromQueueQty-orderFromOrderBookQty >0 ? orderFromOrderBookQty : orderFromQueueQty;
        log.info("[MATCH ORDER][EXCHANGE] matchPrice: {}, exchangeQty: {},  order from queue:[{}], order from order book:[{}]", matchPrice, exchangeQty, orderFromQueue, orderFromOrderBook);
        currentPriceDao.setCurrentPrice(matchPrice.toString());

        ListenableFuture<Transaction> asyncTask = listeningExecService.submit(() -> recordExchange(orderFromQueue, orderFromOrderBook, exchangeQty, matchPrice));
        Futures.addCallback(asyncTask, new FutureCallback<Transaction>() {
            @Override
            public void onSuccess(Transaction result) {
                log.info("[RECORD EXCHANGE] Success, result {}", result);
            }
            @Override
            public void onFailure(Throwable t) {
                log.info("[RECORD EXCHANGE] Failed, reason {}", t.getMessage());
            }
        }, listeningExecService);

        Long restOfOrderFromQueue = null;
        if(orderFromQueueQty > orderFromOrderBookQty){
            restOfOrderFromQueue = orderFromQueueQty - exchangeQty;
        }
        if(orderFromOrderBookQty > orderFromQueueQty) {
            orderFromOrderBook.setQuantity(orderFromOrderBookQty - exchangeQty);
            orderBookDao.pushToHead(orderFromOrderBook);
            restOfOrderFromQueue = 0L;
        }
        if(orderFromOrderBookQty == orderFromQueueQty){
            restOfOrderFromQueue = 0L;
        }
        return restOfOrderFromQueue;
    }

    private Transaction recordExchange(Order orderFromQueue, Order orderFromOrderBook, Long exchangeQty, Long matchPrice){
        Transaction transaction = new Transaction();
        if(orderFromQueue.getSide().equals(OrderSide.SELL)){
            transaction.setSellOrderId(orderFromQueue.getOrderId());
            transaction.setBuyOrderId(orderFromOrderBook.getOrderId());
        } else{
            transaction.setSellOrderId(orderFromOrderBook.getOrderId());
            transaction.setBuyOrderId(orderFromQueue.getOrderId());
        }
        transaction.setQuantity(exchangeQty);
        transaction.setPrice(matchPrice);
        Transaction savedTransaction = transactionRepository.save(transaction);
        return savedTransaction;
    }
}
