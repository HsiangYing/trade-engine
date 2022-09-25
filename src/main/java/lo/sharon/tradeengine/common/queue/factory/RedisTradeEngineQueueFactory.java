package lo.sharon.tradeengine.common.queue.factory;

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
import org.springframework.stereotype.Component;

@Component
@ConditionalOnExpression("'${queue.type}'=='redis'")
@Slf4j
public class RedisTradeEngineQueueFactory implements TradeEngineQueueFactory{

    @Autowired
    private RedisTemplate<String, Object>  redisTemplate;

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

    @Override
    public void createConsumerGroup(String streamKey, String consumerGroupName) {
        try {
            log.info("Creating Consumer Group: {}", consumerGroupName);
            redisTemplate.opsForStream().createGroup(streamKey, ReadOffset.from("0-0"), consumerGroupName);
        } catch (Exception exception){
            if(exception.getMessage().contains("BUSYGROUP")){
                log.info("Failed to Create consumer group [{}] for stream [{}], because it already exists", consumerGroupName, streamKey);
            } else {
                log.error("Error: {}", exception.getMessage(), exception);
            }
        }
    }


}
