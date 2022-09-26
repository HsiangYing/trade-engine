package lo.sharon.tradeengine.common.queue;

import lo.sharon.tradeengine.model.AbstractConsumerGroupInfo;
import org.springframework.data.redis.connection.stream.ObjectRecord;

import java.util.List;

public interface TradeEngineQueueViewer<I extends AbstractConsumerGroupInfo, S extends Object>{
     List<I> viewConsumerGroupInfo();
     List<ObjectRecord<String, S>> viewPendingInQueueItems(String viewFrom, Class<S> targetType);
}
