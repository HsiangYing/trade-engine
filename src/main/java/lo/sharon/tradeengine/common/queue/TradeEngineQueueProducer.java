package lo.sharon.tradeengine.common.queue;

public interface TradeEngineQueueProducer<M> {
    String send(M message);
}
