package lo.sharon.tradeengine.dao.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lo.sharon.tradeengine.constant.OrderSide;
import lo.sharon.tradeengine.constant.OrderType;
import lo.sharon.tradeengine.dao.OrderBookDao;
import lo.sharon.tradeengine.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    @Qualifier("redisOrderTemplate")
    RedisTemplate<String, Order> redisOrderTemplate;

    @Override
    public Long getSize(OrderSide orderSide, boolean isMarketOrder, Long price) {
        String orderBookKey = generateOrderBookKey(orderSide, isMarketOrder, price);
        Long size = redisOrderTemplate.opsForList().size(orderBookKey);
        log.info("[REDIS LIST][length] key: {}, result: {}", orderBookKey, size);
        return size;
    }
    @Override
    public Optional<Order> popFromHead(OrderSide orderSide, boolean isMarketOrder, Long price) {
        String orderBookKey = generateOrderBookKey(orderSide, isMarketOrder, price);
        Order order = new Order();
        try {
            order = redisOrderTemplate.opsForList().leftPop(orderBookKey);
            log.info("[REDIS LIST][left pop] key: {}, result: {}", orderBookKey, order);
        }catch(Exception exception){
            log.error("[REDIS LIST][left pop] key: {}, error: {},", orderBookKey, exception.getMessage());
        }
        return Optional.ofNullable(order);
    }
    @Override
    public Optional<Long> pushToHead(Order order){
        boolean isMarketOrder = order.getType().equals(OrderType.MARKET) ? true : false;
        String orderBookKey = generateOrderBookKey(order.getSide(), isMarketOrder, order.getPrice());
        Long countInList = null;
        try{
            countInList = redisOrderTemplate.opsForList().leftPush(orderBookKey, order);
            log.info("[REDIS LIST][left push] key: {}, order: {}, result: {}", orderBookKey, order, countInList);
        }catch(Exception exception){
            log.error("[REDIS LIST][left push] key: {}, order: {}, error: {},", orderBookKey, order, exception.getMessage());
        }
        return Optional.ofNullable(countInList);

    }
    @Override
    public Optional<Long> pushToTail(Order order){
        boolean isMarketOrder = order.getType().equals(OrderType.MARKET) ? true : false;
        String orderBookKey = generateOrderBookKey(order.getSide(), isMarketOrder, order.getPrice());
        Long countInList = null;
        try {
            countInList = redisOrderTemplate.opsForList().rightPush(orderBookKey, order);
            log.info("[REDIS LIST][left push] key: {}, order: {}, result: {}", orderBookKey, order, countInList);

        }catch (Exception exception){
            log.error("[REDIS LIST][right push] key: {}, order: {}, error: {},", orderBookKey, order, exception.getMessage());
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
