package lo.sharon.tradeengine.common.queue;

public interface TradeEngineQueueProducer<M extends Object> {
    String send(M message);
}
