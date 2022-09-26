package lo.sharon.tradeengine.common.queue.redis;

import lo.sharon.tradeengine.common.queue.factory.RedisTradeEngineQueueFactory;
import lo.sharon.tradeengine.dto.OrderRequest;
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
import org.springframework.data.redis.stream.Subscription;

import java.time.Duration;
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
    public StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, ?> pendingOrderQueueListenerContainerOptions(){
        ExecutorService cacheThreadExecutor = Executors.newCachedThreadPool();
        return StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                .builder()
                .batchSize(10)
                .executor(cacheThreadExecutor)
                .pollTimeout(Duration.ofSeconds(299))
                .objectMapper(new ObjectHashMapper())
                .targetType(OrderRequest.class)
                .errorHandler(new RedisStreamErrorHandler())
                .build();
    }

    @Bean
    public StreamMessageListenerContainer<String, ObjectRecord<String, OrderRequest>> pendingOrderQueueListenerContainer(
            RedisConnectionFactory factory,
            StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, ObjectRecord<String, OrderRequest>> options) {

        StreamMessageListenerContainer listenerContainer = StreamMessageListenerContainer.create(factory, options);
        listenerContainer.start();
        return listenerContainer;
    }

    @Bean
    public Subscription subscription(StreamMessageListenerContainer listenerContainer){

        redisTradeEngineQueueFactory.createConsumerGroup(pendingOrderStreamKey, consumerGroupName);
        StreamMessageListenerContainer.StreamReadRequest requestOptions = StreamMessageListenerContainer.StreamReadRequest
                .builder(StreamOffset.create(pendingOrderStreamKey, ReadOffset.lastConsumed()))
                .consumer(Consumer.from(consumerGroupName, consumerName))
                .autoAcknowledge(false)
                .errorHandler(new RedisStreamErrorHandler())
                .cancelOnError(t -> false)
                .build();
        Subscription subscription = listenerContainer.register(requestOptions, redisPendingOrderQueueConsumer);
        return subscription;
    }


}
