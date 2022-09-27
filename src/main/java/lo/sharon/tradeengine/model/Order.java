package lo.sharon.tradeengine.model;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lo.sharon.tradeengine.constant.OrderSide;
import lo.sharon.tradeengine.constant.OrderType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(with = {JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_VALUES, JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES})
@ApiModel("委託單")
public class Order {
    private String orderId;
    @ApiModelProperty("BUY or SELL")
    private OrderSide side;

    @ApiModelProperty("委託數量")
    private Long quantity;

    @ApiModelProperty("MARKET or LIMIT")
    private OrderType type;

    @ApiModelProperty("委託價格")
    private Long price;

}
