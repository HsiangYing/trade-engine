package lo.sharon.tradeengine.common.queue.factory;

import lo.sharon.tradeengine.common.queue.TradeEngineQueueProducer;
public interface TradeEngineQueueFactory {

    TradeEngineQueueProducer createPendingOrderQueueProducer();
}
