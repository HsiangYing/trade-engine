package lo.sharon.tradeengine.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RedisConsumerGroupInfo extends AbstractConsumerGroupInfo {
    private Long consumerCount;
    private Long pendingCount;
    private String lastDeliveredId;
}
