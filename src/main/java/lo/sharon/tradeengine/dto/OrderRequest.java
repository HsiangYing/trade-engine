package lo.sharon.tradeengine.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lo.sharon.tradeengine.constant.OrderSide;
import lo.sharon.tradeengine.constant.OrderType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(with = {JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_VALUES, JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES})
public class OrderRequest {

    @NotNull
    private OrderSide side;

    @NotNull
    @Min(1)
    private Long quantity;

    @NotNull
    private OrderType type;

    private Long price;

    @AssertTrue(message = "if orderType is market, don't specify price; if order type is limit, price must be > 0")
    private boolean isPriceValid() {
        if (type != null && type.name().equals(OrderType.LIMIT.name())) {
            if (this.price != null && this.price > 0)
                return true;
        }
        if (type != null && type.name().equals(OrderType.MARKET.name())) {
            if (this.price == null)
                return true;
        }
        return false;
    }
}
