package lo.sharon.tradeengine.dao;

public interface CurrentPriceDao {
    void setCurrentPrice(String price);
    Long getCurrentPrice();
}
