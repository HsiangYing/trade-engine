package lo.sharon.tradeengine.dao.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lo.sharon.tradeengine.constant.OrderSide;
import lo.sharon.tradeengine.constant.OrderType;
import lo.sharon.tradeengine.dao.OrderBookDao;
import lo.sharon.tradeengine.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Optional;

@Component
@Slf4j
public class RedisOrderBookDaoImpl implements OrderBookDao {

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    @Override
    public Optional<Order> getOrderFromOrderBook(OrderSide orderSide, boolean isMarketOrder, Long price) {
        String orderBookKey = generateOrderBookKey(orderSide, isMarketOrder, price);
        try {
            Object order = redisTemplate.opsForList().leftPop(orderBookKey);
            log.info("[left pop from order book] key: {}, result: {}", orderBookKey, order);
        }catch (Exception exception){
            log.error("Failed to pop order to redis list , key: {}, order: {}, error: {},", orderBookKey, exception.getMessage());
        }
        return Optional.of(new Order());
    }
    @Override
    public Optional<Long> pushOrderToOrderBookHead(Order order){
        boolean isMarketOrder = order.getType().equals(OrderType.MARKET) ? true : false;
        String orderBookKey = generateOrderBookKey(order.getSide(), isMarketOrder, order.getPrice());
        order.setOrderId("uio");
        Long countInList = redisTemplate.opsForList().leftPush(orderBookKey, order.toString());
        log.info("[left push to order book] key: {}, order: {}, result: {}", orderBookKey, order, countInList);
        return Optional.ofNullable(countInList);

    }
    @Override
    public Optional<Long> pushOrderToOrderBookTail(Order order){
        boolean isMarketOrder = order.getType().equals(OrderType.MARKET) ? true : false;
        String orderBookKey = generateOrderBookKey(order.getSide(), isMarketOrder, order.getPrice());
        Long countInList = null;
        try {
            countInList = redisTemplate.opsForList().rightPush(orderBookKey, order.toString());
            log.info("[right push to order book] key: {}, order: {}, result: {}", orderBookKey, order, countInList);
        }catch (Exception exception){
            log.error("Failed to push order to redis list , key: {}, order: {}, error: {},", orderBookKey, order, exception.getMessage());
        }
        return Optional.ofNullable(countInList);

    }

    private String generateOrderBookKey(OrderSide orderSide, boolean isMarketOrder, Long price){
        String orderBookKey;
        if(isMarketOrder){
            orderBookKey = orderSide.name() + OrderType.MARKET.name();
        } else{
            orderBookKey = orderSide.name() + price;

        }
        return orderBookKey;
    }

}
