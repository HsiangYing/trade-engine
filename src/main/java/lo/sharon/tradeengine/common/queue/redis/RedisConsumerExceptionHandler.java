package lo.sharon.tradeengine.common.queue.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ErrorHandler;

@Slf4j
public class RedisConsumerExceptionHandler implements ErrorHandler {
    @Override
    public void handleError(Throwable t) {
        log.error("[Consume redis stream error] {} ", t.getCause(), t.getMessage());
    }
}
