package lo.sharon.tradeengine.service;

import lo.sharon.tradeengine.dto.OrderRequest;

public interface OrderService {
    public String putOrderToPendingOrderQueue(OrderRequest orderRequest);
}
