package lo.sharon.tradeengine.service.impl;

import lo.sharon.tradeengine.common.queue.TradeEngineQueueProducer;
import lo.sharon.tradeengine.common.queue.TradeEngineQueueViewer;
import lo.sharon.tradeengine.common.queue.factory.TradeEngineQueueFactory;
import lo.sharon.tradeengine.constant.OrderStatus;
import lo.sharon.tradeengine.dto.OrderRequest;
import lo.sharon.tradeengine.model.Order;
import lo.sharon.tradeengine.model.RedisConsumerGroupInfo;
import lo.sharon.tradeengine.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    private TradeEngineQueueFactory tradeEngineQueueFactory;
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

    private List<Order> getPendingInQueueOrders(){
        // getLastDeliveredId
        TradeEngineQueueViewer tradeEngineQueueViewer = tradeEngineQueueFactory.createPendingOrderQueueViewer();
        List<RedisConsumerGroupInfo> redisConsumerGroupsInfo = tradeEngineQueueViewer.viewConsumerGroupInfo();
        String lastConsumedOrderId = redisConsumerGroupsInfo.get(0).getLastDeliveredId();

        // get orders behind lastDeliveredId
        List<Order> pendingInQueueOrders = tradeEngineQueueViewer.viewPendingInQueueItems(lastConsumedOrderId, Order.class);
        return pendingInQueueOrders;
    }
}
