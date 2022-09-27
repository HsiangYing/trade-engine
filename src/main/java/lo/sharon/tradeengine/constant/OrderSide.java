package lo.sharon.tradeengine.constant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@ApiModel("買方/賣方")
public enum OrderSide {
    @ApiModelProperty("BUY")
    BUY,
    @ApiModelProperty("SELL")
    SELL
}
