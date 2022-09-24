package lo.sharon.tradeengine.common.queue.factory;

import io.lettuce.core.RedisBusyException;
import lo.sharon.tradeengine.common.queue.TradeEngineQueueProducer;
import lo.sharon.tradeengine.common.queue.TradeEngineQueueViewer;
import lo.sharon.tradeengine.common.queue.redis.RedisTradeEngineQueueProducer;
import lo.sharon.tradeengine.common.queue.redis.RedisTradeEngineQueueViewer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;

@Component
@ConditionalOnExpression("'${queue.type}'=='redis'")
@Slf4j
public class RedisTradeEngineQueueFactory implements TradeEngineQueueFactory{

    @Autowired
    private RedisTemplate redisTemplate;

    @PostConstruct
    public void init() {
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());
        this.createConsumerGroupForPendingOrderStream();
    }


    @Value("${queue.pending-order-queue.topic-name}")
    private String pendingOrderStreamKey;
    @Value("${queue.pending-order-queue.consumer-group-name}")
    private String pendingOrderStreamConsumerGroupName;
    @Override
    public TradeEngineQueueProducer createPendingOrderQueueProducer() {
        return new RedisTradeEngineQueueProducer(pendingOrderStreamKey, redisTemplate);
    }
    @Override
    public TradeEngineQueueViewer createPendingOrderQueueViewer() {
        return new RedisTradeEngineQueueViewer(pendingOrderStreamKey, redisTemplate);
    }

    private void createConsumerGroupForPendingOrderStream() {
        try {
            redisTemplate.opsForStream().createGroup(this.pendingOrderStreamKey, ReadOffset.from("0-0") , this.pendingOrderStreamConsumerGroupName);
        } catch (Exception exception){
            String cause = exception.getClass().getCanonicalName();
            if(cause.contains("Consumer Group name already exists"));
            log.info("Failed to Create consumer group for pending order stream because Consumer Group name already exists");
        }
    }
}
