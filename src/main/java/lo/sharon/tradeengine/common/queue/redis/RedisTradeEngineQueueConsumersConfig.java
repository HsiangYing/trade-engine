package lo.sharon.tradeengine.common.queue.redis;

import lo.sharon.tradeengine.common.queue.factory.RedisTradeEngineQueueFactory;
import lo.sharon.tradeengine.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.hash.ObjectHashMapper;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@ConditionalOnExpression("'${queue.type}'=='redis'")
public class RedisTradeEngineQueueConsumersConfig {

    @Autowired
    RedisConnectionFactory redisConnectionFactory;
    @Autowired
    RedisTradeEngineQueueFactory redisTradeEngineQueueFactory;
    @Value("${queue.pending-order-queue.topic-name}")
    private String pendingOrderStreamKey;
    @Value("${queue.pending-order-queue.consumer-group-name}")
    private String consumerGroupName;
    @Value("${queue.pending-order-queue.consumer-name}")
    private String consumerName;
    @Autowired
    private RedisPendingOrderQueueConsumer redisPendingOrderQueueConsumer;

    @Bean
    public StreamMessageListenerContainer<String, ObjectRecord<String, Order>> pendingOrderQueueListenerContainer(RedisConnectionFactory factory){
        redisTradeEngineQueueFactory.createConsumerGroup(pendingOrderStreamKey, consumerGroupName);
        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, ObjectRecord<String, Order>> options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                        .builder()
                        // 一次拿多少個訊息
                        .batchSize(5)
                        // thread-safe
                        .executor(singleThreadExecutor)
                        // Stream 中没有消息时，阻塞多长时间，需要比 `spring.redis.timeout` 的时间小
                        .pollTimeout(Duration.ofSeconds(10))
                        .objectMapper(new ObjectHashMapper())
                        .targetType(Order.class)
                        .errorHandler(new RedisConsumerExceptionHandler())
                        .build();

        StreamMessageListenerContainer container = StreamMessageListenerContainer.create(factory, options);
        container.receive(Consumer.from(consumerGroupName, consumerName),
                StreamOffset.create(pendingOrderStreamKey, ReadOffset.lastConsumed()),
                redisPendingOrderQueueConsumer);
        container.start();
        return container;
    }


}
