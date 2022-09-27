package lo.sharon.tradeengine.constant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("市價/限價")
public enum OrderType {
    @ApiModelProperty("MARKET")
    MARKET,
    @ApiModelProperty("LIMIT")
    LIMIT
}
