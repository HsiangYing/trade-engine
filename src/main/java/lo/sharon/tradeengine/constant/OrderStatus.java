package lo.sharon.tradeengine.constant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("委託單狀態")
public enum OrderStatus {
    @ApiModelProperty("PENDING_IN_QUEUE")
    PENDING_IN_QUEUE,
    @ApiModelProperty("PENDING_IN_ORDER_BOOK")
    PENDING_IN_ORDER_BOOK,
    @ApiModelProperty("FILLED")
    FILLED
}
