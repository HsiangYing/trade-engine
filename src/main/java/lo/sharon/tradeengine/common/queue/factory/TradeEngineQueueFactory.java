package lo.sharon.tradeengine.common.queue.factory;

import lo.sharon.tradeengine.common.queue.TradeEngineQueueProducer;
import lo.sharon.tradeengine.common.queue.TradeEngineQueueViewer;

public interface TradeEngineQueueFactory {

    TradeEngineQueueProducer createPendingOrderQueueProducer();
    TradeEngineQueueViewer createPendingOrderQueueViewer();
}
