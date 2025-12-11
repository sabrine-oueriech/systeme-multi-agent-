package auction.models;

import java.io.Serializable;
import java.util.*;

public class MarketStatistics implements Serializable {
    private double averagePrice;
    private double volatilityIndex;
    private int totalBids;
    private int activeAuctions;
    private Map<String, Double> categoryTrends;
    
    public MarketStatistics() {
        this.categoryTrends = new HashMap<>();
    }
    
    public double getAveragePrice() { return averagePrice; }
    public void setAveragePrice(double avg) { this.averagePrice = avg; }
    public double getVolatilityIndex() { return volatilityIndex; }
    public void setVolatilityIndex(double vol) { this.volatilityIndex = vol; }
}