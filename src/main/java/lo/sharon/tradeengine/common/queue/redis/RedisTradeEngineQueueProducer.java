package lo.sharon.tradeengine.common.queue.redis;

import lo.sharon.tradeengine.common.queue.TradeEngineQueueProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.core.RedisTemplate;

@Slf4j
public class RedisTradeEngineQueueProducer<M> implements TradeEngineQueueProducer<M> {
    private final String streamKey;
    private final RedisTemplate<String, Object>  redisTemplate;

    public RedisTradeEngineQueueProducer(String streamKey, RedisTemplate redisTemplate) {
        this.streamKey = streamKey;
        this.redisTemplate = redisTemplate;
    }
    @Override
    public String send(M message){
        ObjectRecord<String, Object> record = StreamRecords.newRecord()
                .in(this.streamKey)
                .ofObject(message);
        RecordId recordId = redisTemplate.opsForStream().add(record);
        log.info("[REDIS STEAM][Produce message] stream key: [{}], message: [{}]", this.streamKey, message);
        return recordId.getValue();
    }

}
