package lo.sharon.tradeengine.common.queue.factory;

import lo.sharon.tradeengine.common.queue.TradeEngineQueueProducer;
import lo.sharon.tradeengine.common.queue.TradeEngineQueueViewer;
import lo.sharon.tradeengine.common.queue.redis.RedisTradeEngineQueueProducer;
import lo.sharon.tradeengine.common.queue.redis.RedisTradeEngineQueueViewer;
import lo.sharon.tradeengine.dto.OrderRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    @Qualifier("redisOrderRequestTemplate")
    private RedisTemplate<String, OrderRequest> redisOrderRequestTemplate;
    @Autowired
    @Qualifier("redisStringTemplate")
    private RedisTemplate<String, String> redisStringTemplate;
    @Value("${queue.pending-order-queue.topic-name}")
    private String pendingOrderStreamKey;
    @Value("${queue.pending-order-queue.consumer-group-name}")
    private String pendingOrderStreamConsumerGroupName;
    @Override
    public TradeEngineQueueProducer createPendingOrderQueueProducer() {
        return new RedisTradeEngineQueueProducer(pendingOrderStreamKey, redisOrderRequestTemplate);
    }
    @Override
    public TradeEngineQueueViewer createPendingOrderQueueViewer() {
        return new RedisTradeEngineQueueViewer(pendingOrderStreamKey, redisOrderRequestTemplate);
    }

    @Override
    public void createConsumerGroup(String streamKey, String consumerGroupName) {
        try {
            log.info("[REDIS STREAM] Creating Consumer Group: {}", consumerGroupName);
            redisStringTemplate.opsForStream().createGroup(streamKey, ReadOffset.from("0-0"), consumerGroupName);
        } catch (Exception exception){
            if(exception.getMessage().contains("BUSYGROUP")){
                log.info("[REDIS STREAM]Failed to Create consumer group [{}] for stream [{}], because it already exists", consumerGroupName, streamKey);
            } else {
                log.error("[REDIS STREAM] {}", exception.getMessage());
            }
        }
    }


}
