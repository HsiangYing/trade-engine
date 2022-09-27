package lo.sharon.tradeengine.service;

import lo.sharon.tradeengine.constant.OrderStatus;
import lo.sharon.tradeengine.dto.OrderRequest;
import lo.sharon.tradeengine.model.Order;

import java.util.List;
import java.util.Map;

public interface OrderService {
    String putOrderToPendingOrderQueue(OrderRequest orderRequest);
    Map<String, List<Order>> getOrdersByStatus(OrderStatus orderStatus);
    void matchOrder(Order order);
}
