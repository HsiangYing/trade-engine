package lo.sharon.tradeengine.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import lo.sharon.tradeengine.constant.OrderSide;
import lo.sharon.tradeengine.constant.OrderType;
import lo.sharon.tradeengine.model.Order;

import java.util.Optional;

public interface OrderBookDao {
    Optional<Order> getOrderFromOrderBook(OrderSide orderSide, boolean isMarketOrder, Long price);
    Optional<Long> pushOrderToOrderBookHead(Order order);
    Optional<Long> pushOrderToOrderBookTail(Order order);

}
