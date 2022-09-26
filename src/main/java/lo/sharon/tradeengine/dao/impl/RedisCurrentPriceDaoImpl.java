package lo.sharon.tradeengine.dao.impl;

import lo.sharon.tradeengine.dao.CurrentPriceDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
public class RedisCurrentPriceDaoImpl implements CurrentPriceDao {
    @Autowired
    @Qualifier("redisStringTemplate")
    RedisTemplate redisStringTemplate;
    @Value("${cache.current-price-key}")
    private String currentPriceKey;

    @Value("${cache.default-current-price}")
    private String defaultCurrentPrice;

    @PostConstruct
    void init(){
        if(this.getCurrentPrice() == null){
            this.setCurrentPrice(defaultCurrentPrice);
        }
    }

    @Override
    public void setCurrentPrice(String price) {
        log.info("[REDIS STRING][set current price] {}", price);
        redisStringTemplate.opsForValue().set(currentPriceKey, price);
    }

    @Override
    public Long getCurrentPrice() {
        Long currentPrice;
        if(redisStringTemplate.opsForValue().get(currentPriceKey) == null){
            currentPrice = null;
        }else{
            currentPrice = Long.parseLong(redisStringTemplate.opsForValue().get(currentPriceKey).toString());
        }
        return currentPrice;
    }
}
