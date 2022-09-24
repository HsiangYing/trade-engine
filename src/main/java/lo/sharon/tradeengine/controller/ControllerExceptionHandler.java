package lo.sharon.tradeengine.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lo.sharon.tradeengine.common.exception.TradeEngineErrorCode;
import lo.sharon.tradeengine.common.exception.TradeEngineException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@ControllerAdvice
@Slf4j
public class ControllerExceptionHandler<T extends Exception> {
    @Autowired
    private ObjectMapper mapper;

    @ExceptionHandler(Exception.class)
    public void handle(T exception, HttpServletResponse response) {
        log.error("Error [{}]", exception.getClass().getCanonicalName(), exception.getMessage());
        handleException(exception, response);
    }

    private void handleException(Exception exception, HttpServletResponse response) {
        try {
            String cause = "";
            if (exception.getCause() != null) {
                cause = exception.getCause().getClass().getCanonicalName();
            }
            if(exception instanceof TradeEngineException){
                handleTradeEngineException((TradeEngineException) exception, response);
            }else if(exception instanceof MethodArgumentTypeMismatchException || exception instanceof MethodArgumentNotValidException
                    || exception instanceof MissingServletRequestParameterException ||cause.contains("InvalidFormatException")){
                TradeEngineExceptionResponse tradeEngineExceptionResponse = new TradeEngineExceptionResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), TradeEngineErrorCode.INVALID_REQUEST_PARAMETER);
                response.setStatus(tradeEngineExceptionResponse.getStatus().value());
                mapper.writeValue(response.getWriter(), tradeEngineExceptionResponse);
            }else {
                TradeEngineExceptionResponse tradeEngineExceptionResponse = new TradeEngineExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage(), TradeEngineErrorCode.INTERNAL_SERVER_ERROR);
                response.setStatus(tradeEngineExceptionResponse.getStatus().value());
                mapper.writeValue(response.getWriter(), tradeEngineExceptionResponse);
            }
        } catch (IOException e) {
            log.error("Can't handle exception", e ,e.getMessage());
        }
    }

    private void handleTradeEngineException(TradeEngineException tradeEngineException, HttpServletResponse response) throws IOException {
        TradeEngineErrorCode tradeEngineErrorCode = tradeEngineException.getTradeEngineErrorCode();
        HttpStatus status;
        switch (tradeEngineErrorCode){
            case ITEM_NOT_FOUND:
                status = HttpStatus.NOT_FOUND;
                break;
            default:
                status = HttpStatus.INTERNAL_SERVER_ERROR;
                break;
        }
        response.setStatus(status.value());
        TradeEngineExceptionResponse tradeEngineExceptionResponse = new TradeEngineExceptionResponse(status, tradeEngineException.getMessage(), tradeEngineErrorCode);
        mapper.writeValue(response.getWriter(), tradeEngineExceptionResponse);
    }
}
