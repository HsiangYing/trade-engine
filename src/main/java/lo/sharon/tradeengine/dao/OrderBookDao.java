package lo.sharon.tradeengine.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import lo.sharon.tradeengine.constant.OrderSide;
import lo.sharon.tradeengine.constant.OrderType;
import lo.sharon.tradeengine.model.Order;

import java.util.Optional;

public interface OrderBookDao {

    Long getSize(OrderSide orderSide, boolean isMarketOrder, Long price);
    Optional<Order> popFromHead(OrderSide orderSide, boolean isMarketOrder, Long price);
    Optional<Long> pushToHead(Order order);
    Optional<Long> pushToTail(Order order);

}
