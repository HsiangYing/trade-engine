package lo.sharon.tradeengine.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "transaction")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "buy_order_id", columnDefinition = "VARCHAR(128) NOT NULL")
    private String buyOrderId;

    @Column(name = "sell_order_id", columnDefinition = "VARCHAR(128) NOT NULL")
    private String sellOrderId;

    @Column(columnDefinition = "BIGINT NOT NULL")
    private Long quantity;

    @Column(columnDefinition = "BIGINT NOT NULL")
    private Long price;

    @Column(name = "created_timestamp", columnDefinition = "TIMESTAMP DEFAULT now()")
    private Date createdTimestamp;
}
