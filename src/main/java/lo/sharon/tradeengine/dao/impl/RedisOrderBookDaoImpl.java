package lo.sharon.tradeengine.dao.impl;

import lo.sharon.tradeengine.constant.OrderSide;
import lo.sharon.tradeengine.constant.OrderType;
import lo.sharon.tradeengine.dao.OrderBookDao;
import lo.sharon.tradeengine.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class RedisOrderBookDaoImpl implements OrderBookDao {

    @Autowired
    @Qualifier("redisOrderTemplate")
    RedisTemplate<String, Order> redisOrderTemplate;
    @Autowired
    @Qualifier("redisStringTemplate")
    RedisTemplate<String, String> redisStringTemplate;
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

    @Override
    public Map<String, List<Order>> getAll() {
        Map<String, List<Order>> ordersInOrderBook = new HashMap<>();

        Set<String> buyKeys = redisStringTemplate.keys("BUY*");
        Map<String, List<Order>> ordersInOrderBookByBuyKey = getAllByKeys(buyKeys);

        Set<String> sellKeys = redisStringTemplate.keys("SELL*");
        Map<String, List<Order>> ordersInOrderBookBySellKey = getAllByKeys(sellKeys);

        ordersInOrderBook.putAll(ordersInOrderBookByBuyKey);
        ordersInOrderBook.putAll(ordersInOrderBookBySellKey);

        return ordersInOrderBook;
    }

    public Map<String, List<Order>> getAllByKeys (Set<String> keys) {
        Map<String, List<Order>> ordersInOrderBookBuyKey = new HashMap<>();
        Iterator<String> keysIterator = keys.iterator();
        while(keysIterator.hasNext()){
            String key = keysIterator.next();
            List<Order> orders = redisOrderTemplate.opsForList().range(key,0L, 01L);
            ordersInOrderBookBuyKey.put(key, orders);
        }
        return ordersInOrderBookBuyKey;
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
