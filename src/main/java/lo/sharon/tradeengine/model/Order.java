package lo.sharon.tradeengine.model;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
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
public class Order {
    private String orderId;
    private OrderSide side;
    private Long quantity;
    private OrderType type;
    private Long price;

}
