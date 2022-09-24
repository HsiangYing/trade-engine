package lo.sharon.tradeengine.common.queue.factory;

import lo.sharon.tradeengine.common.queue.TradeEngineQueueProducer;
import lo.sharon.tradeengine.common.queue.redis.RedisTradeEngineQueueProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;

@Component
@ConditionalOnExpression("'${queues.type}'=='redis'")
public class RedisTradeEngineQueueFactory implements TradeEngineQueueFactory{

    @Autowired
    private RedisTemplate redisTemplate;

    @PostConstruct
    public void init() {
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());
    }
    @Value("${queues.pending-order-queue-topic-name}")
    private String streamKey;
    @Override
    public TradeEngineQueueProducer createPendingOrderQueueProducer() {
        return new RedisTradeEngineQueueProducer(streamKey, redisTemplate);
    }
}
