package lo.sharon.tradeengine.common.queue.errorHandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ErrorHandler;

@Slf4j
public class RedisStreamErrorHandler implements ErrorHandler {
    @Override
    public void handleError(Throwable t) {
        log.error("[REDIS STREAM] {}", t.getMessage());
    }
}
