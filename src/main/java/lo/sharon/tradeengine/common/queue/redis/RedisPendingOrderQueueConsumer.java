package lo.sharon.tradeengine.common.queue.redis;

import lo.sharon.tradeengine.model.Order;
import lo.sharon.tradeengine.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnExpression("'${queue.type}'=='redis'")
public class RedisPendingOrderQueueConsumer implements StreamListener<String, ObjectRecord<String, Order>> {
    @Value("${queue.pending-order-queue.consumer-group-name}")
    private String consumerGroupName;
    @Value("${queue.pending-order-queue.consumer-name}")
    private String consumerName;
    @Autowired
    private RedisTemplate<String, Object>  redisTemplate;

    @Autowired
    OrderService orderService;
    @Override
    public void onMessage(ObjectRecord<String, Order> objectRecord) {
        Object streamKey = objectRecord.getStream();
        RecordId id = objectRecord.getId();
        Order order = objectRecord.getValue();
        order.setOrderId(id.getValue());
        log.info("[On Message] group:[{}] consumerName:[{}] stream:[{}] id:[{}] value:[{}]",
                consumerGroupName, consumerName, streamKey, id, order);

        //match order
        orderService.matchOrder(order);

        Long ackCount = redisTemplate.opsForStream().acknowledge(consumerGroupName, objectRecord);
        log.info("[Ack Message] total ack count:[{}] group:[{}] consumerName:[{}] stream:[{}] id:[{}] value:[{}]",
                ackCount, consumerGroupName, consumerName, streamKey, id, order);
    }
}