package lo.sharon.tradeengine.common.queue.redis;

import lo.sharon.tradeengine.dto.OrderRequest;
import lo.sharon.tradeengine.model.Order;
import lo.sharon.tradeengine.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
public class RedisPendingOrderQueueConsumer implements StreamListener<String, ObjectRecord<String, OrderRequest>> {
    @Value("${queue.pending-order-queue.consumer-group-name}")
    private String consumerGroupName;
    @Value("${queue.pending-order-queue.consumer-name}")
    private String consumerName;
    @Autowired
    @Qualifier("redisOrderRequestTemplate")
    private RedisTemplate<String, OrderRequest>  redisTemplate;

    @Autowired
    OrderService orderService;
    @Override
    public void onMessage(ObjectRecord<String, OrderRequest> objectRecord) {
        String streamKey = objectRecord.getStream();
        RecordId id = objectRecord.getId();
        OrderRequest orderRequest = objectRecord.getValue();

        Order order = orderRequest.transformToOrder();
        order.setOrderId(id.getValue());
        log.info("[REDIS STREAM][On Message] group:[{}] consumerName:[{}] stream:[{}] id:[{}] value:[{}]",
                consumerGroupName, consumerName, streamKey, id, order);
        orderService.matchOrder(order);
        Long ackCount = redisTemplate.opsForStream().acknowledge(consumerGroupName, objectRecord);
        log.info("[REDIS STREAM][Ack Message] total ack count:[{}] group:[{}] consumerName:[{}] stream:[{}] id:[{}] value:[{}]",
                ackCount, consumerGroupName, consumerName, streamKey, id, order);


    }
}