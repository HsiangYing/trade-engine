package lo.sharon.tradeengine.service.impl;

import lo.sharon.tradeengine.common.queue.TradeEngineQueueProducer;
import lo.sharon.tradeengine.common.queue.TradeEngineQueueViewer;
import lo.sharon.tradeengine.common.queue.factory.TradeEngineQueueFactory;
import lo.sharon.tradeengine.constant.OrderSide;
import lo.sharon.tradeengine.constant.OrderStatus;
import lo.sharon.tradeengine.constant.OrderType;
import lo.sharon.tradeengine.dao.CurrentPriceDao;
import lo.sharon.tradeengine.dao.OrderBookDao;
import lo.sharon.tradeengine.dto.OrderRequest;
import lo.sharon.tradeengine.model.Order;
import lo.sharon.tradeengine.model.RedisConsumerGroupInfo;
import lo.sharon.tradeengine.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    private TradeEngineQueueFactory tradeEngineQueueFactory;
    @Autowired
    private OrderBookDao orderBookDao;

    @Autowired
    private CurrentPriceDao currentPriceDao;
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
        //orderFromQueue 剩餘委託數量
        Long restOfOrderFromQueueQty = orderFromQueue.getQuantity();
        // OrderBook 中是否有反方市價單
        Optional<Order> marketOrderFromOrderBook = orderBookDao.getOrderFromOrderBook(orderFromOrderBookOrderSide, true, null);
        boolean isMarketOrderInOrderBook = marketOrderFromOrderBook.orElse(null) != null;

        while(isMarketOrderInOrderBook && restOfOrderFromQueueQty > 0){ //1
            restOfOrderFromQueueQty = doExchange(orderFromQueue, marketOrderFromOrderBook.get(), priceToBeMatch);
            orderFromQueue.setQuantity(restOfOrderFromQueueQty);
            marketOrderFromOrderBook = orderBookDao.getOrderFromOrderBook(orderFromOrderBookOrderSide, true, null);
            isMarketOrderInOrderBook = marketOrderFromOrderBook.orElse(null) != null;
        }

        if (restOfOrderFromQueueQty == 0) {
            return;
        }

        // INITIAL
        // OrderBook中是否有反方限價單
        Optional<Order> limitOrderFromOrderBook = orderBookDao.getOrderFromOrderBook(orderFromOrderBookOrderSide, false, priceToBeMatch);
        boolean isLimitOrderInOrderBook = limitOrderFromOrderBook.orElse(null) != null;

        while(isLimitOrderInOrderBook && restOfOrderFromQueueQty > 0){ //2+3
            restOfOrderFromQueueQty = doExchange(orderFromQueue, limitOrderFromOrderBook.get(), priceToBeMatch);
            orderFromQueue.setQuantity(restOfOrderFromQueueQty);
            limitOrderFromOrderBook = orderBookDao.getOrderFromOrderBook(OrderSide.BUY, false, priceToBeMatch);
            isLimitOrderInOrderBook = limitOrderFromOrderBook.orElse(null) != null;
        }
        if(restOfOrderFromQueueQty > 0){
            //order from queue 剩下的單放到 OrderBook 中
            orderFromQueue.setQuantity(restOfOrderFromQueueQty);
            orderBookDao.pushOrderToOrderBookTail(orderFromQueue);
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


    /**
     *
     * @param orderFromQueue
     * @param orderFromOrderBook
     * @param matchPrice 成交價格
     * @return orderFromQueue 剩餘的委託量
     */
    private Long doExchange(Order orderFromQueue, Order orderFromOrderBook, Long matchPrice) {
        Long orderFromQueueQty = orderFromQueue.getQuantity();
        Long orderFromOrderBookQty = orderFromOrderBook.getQuantity();
        Long exchangeQty = orderFromQueueQty-orderFromOrderBookQty >0 ? orderFromOrderBookQty : orderFromQueueQty;
        log.info("[Exchange] matchPrice: {}, exchangeQty: {},  order from queue:[{}], order from order book:[{}]", matchPrice, exchangeQty, orderFromQueue, orderFromOrderBook);
        currentPriceDao.setCurrentPrice(matchPrice.toString());
        //todo: save to db 注意數量
        Long restOfOrderFromQueue = null;
        if(orderFromQueueQty > orderFromOrderBookQty){
            restOfOrderFromQueue = orderFromQueueQty - exchangeQty;
        }
        if(orderFromOrderBookQty > orderFromQueueQty) {
            orderFromOrderBook.setQuantity(orderFromOrderBookQty - exchangeQty);
            orderBookDao.pushOrderToOrderBookHead(orderFromOrderBook);
            restOfOrderFromQueue = 0L;
        }
        if(orderFromOrderBookQty == orderFromQueueQty){
            restOfOrderFromQueue = 0L;
        }
        return restOfOrderFromQueue;
    }
}
