package lo.sharon.tradeengine.controller;

import lo.sharon.tradeengine.common.exception.TradeEngineErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class TradeEngineExceptionResponse {
    private final HttpStatus status;
    private final String message;
    private final TradeEngineErrorCode tradeEngineErrorCode;
    protected TradeEngineExceptionResponse(HttpStatus status, String message, TradeEngineErrorCode tradeEngineErrorCode){
        this.status = status;
        this.message = message;
        this.tradeEngineErrorCode = tradeEngineErrorCode;
    }
}
