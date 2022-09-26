package lo.sharon.tradeengine.service;

import lo.sharon.tradeengine.constant.OrderStatus;
import lo.sharon.tradeengine.dto.OrderRequest;
import lo.sharon.tradeengine.model.Order;

import java.util.List;

public interface OrderService {
    public String putOrderToPendingOrderQueue(OrderRequest orderRequest);
    public List<Order> getOrdersByStatus(OrderStatus orderStatus);
}
