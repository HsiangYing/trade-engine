package lo.sharon.tradeengine.dao.impl;

import lo.sharon.tradeengine.dao.CurrentPriceDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
public class RedisCurrentPriceDaoImpl implements CurrentPriceDao {
    @Autowired
    RedisTemplate redisTemplate;
    @Value("${cache.current-price-key}")
    private String currentPriceKey;

    @Value("${cache.default-current-price}")
    private String defaultCurrentPrice;

    @PostConstruct
    void init(){
        if(this.getCurrentPrice() == null){
            this.setCurrentPrice(defaultCurrentPrice);
            log.info("[3]");
        }
    }

    @Override
    public void setCurrentPrice(String price) {
        log.info("[Set Current Price] {}", price);
        redisTemplate.opsForValue().set(currentPriceKey, price);
    }

    @Override
    public Long getCurrentPrice() {
        Long currentPrice;
        if(redisTemplate.opsForValue().get(currentPriceKey) == null){
            log.info("[1]");
            currentPrice = null;
        }else{
            log.info("[2] {}", redisTemplate.opsForValue().get(currentPriceKey) );
            currentPrice = Long.parseLong(redisTemplate.opsForValue().get(currentPriceKey).toString());
        }
        return currentPrice;
    }
}
