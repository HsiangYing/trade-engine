package lo.sharon.tradeengine.common.queue.redis;

import lo.sharon.tradeengine.common.queue.TradeEngineQueueViewer;
import lo.sharon.tradeengine.model.RedisConsumerGroupInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class RedisTradeEngineQueueViewer<S> implements TradeEngineQueueViewer<RedisConsumerGroupInfo, S> {

    private final String streamKey;
    private final RedisTemplate redisTemplate;

    public RedisTradeEngineQueueViewer(String streamKey, RedisTemplate redisTemplate) {
        this.streamKey = streamKey;
        this.redisTemplate = redisTemplate;
    }

    public List<RedisConsumerGroupInfo> viewConsumerGroupInfo(){
        StreamInfo.XInfoGroups consumerGroupsInfo = redisTemplate.opsForStream().groups(streamKey);
        Iterator<StreamInfo.XInfoGroup> consumerGroupsInfoIterator = consumerGroupsInfo.iterator();
        List<RedisConsumerGroupInfo> redisConsumerGroupsInfo = new ArrayList<>();

        while(consumerGroupsInfoIterator.hasNext()){
            StreamInfo.XInfoGroup consumerGroupInfo = consumerGroupsInfoIterator.next();
            RedisConsumerGroupInfo redisConsumerGroupInfo = new RedisConsumerGroupInfo();
            redisConsumerGroupInfo.setGroupName(consumerGroupInfo.groupName());
            redisConsumerGroupInfo.setConsumerCount(consumerGroupInfo.consumerCount());
            redisConsumerGroupInfo.setPendingCount(consumerGroupInfo.pendingCount());
            redisConsumerGroupInfo.setLastDeliveredId(consumerGroupInfo.lastDeliveredId());
            redisConsumerGroupsInfo.add(redisConsumerGroupInfo);
        }
        log.info("[RedisConsumerGroupsInfo] {}", redisConsumerGroupsInfo);
        return redisConsumerGroupsInfo;
    }
    public List<S> viewPendingInQueueItems(String readFrom, Class<S> targetType){
        List<S> pendingInQueueOrders = redisTemplate.opsForStream().read(targetType, StreamOffset.create(streamKey, ReadOffset.from(readFrom)));
        log.info("[PendingInQueueOrders] {}", pendingInQueueOrders);
        return pendingInQueueOrders;
    }
}
