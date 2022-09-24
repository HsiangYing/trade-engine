package lo.sharon.tradeengine.common.exception;

import lombok.Getter;
@Getter
public class TradeEngineException extends Exception{

    private TradeEngineErrorCode tradeEngineErrorCode;

    public TradeEngineException(){
        super();
    }
    public TradeEngineException(TradeEngineErrorCode tradeEngineErrorCode){
        this.tradeEngineErrorCode = tradeEngineErrorCode;
    }
    public TradeEngineException(String message, TradeEngineErrorCode tradeEngineErrorCode){
        super(message);
        this.tradeEngineErrorCode = tradeEngineErrorCode;
    }

}
