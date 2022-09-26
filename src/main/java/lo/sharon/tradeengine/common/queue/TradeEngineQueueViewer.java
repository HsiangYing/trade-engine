package lo.sharon.tradeengine.common.queue;

import lo.sharon.tradeengine.model.AbstractConsumerGroupInfo;

import java.util.List;

public interface TradeEngineQueueViewer<I extends AbstractConsumerGroupInfo, S extends Object> {
     List<I> viewConsumerGroupInfo();

     List<S> viewPendingInQueueItems(String viewFrom, Class<S> targetType);
}
