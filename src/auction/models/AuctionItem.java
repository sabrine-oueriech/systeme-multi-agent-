package auction.models;

import java.io.Serializable;
import java.util.Date;

public class AuctionItem implements Serializable {
    private String id;
    private String name;
    private String description;
    private double startingPrice;
    private double currentPrice;
    private double reservePrice;
    private String category;
    private Date startTime;
    private Date endTime;
    private String currentWinner;
    
    public AuctionItem(String id, String name, double startingPrice, double reservePrice) {
        this.id = id;
        this.name = name;
        this.startingPrice = startingPrice;
        this.currentPrice = startingPrice;
        this.reservePrice = reservePrice;
        this.startTime = new Date();
        this.endTime = new Date(System.currentTimeMillis() + 300000);
    }
    
    public String getId() { return id; }
    public String getName() { return name; }
    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double price) { this.currentPrice = price; }
    public String getCurrentWinner() { return currentWinner; }
    public void setCurrentWinner(String winner) { this.currentWinner = winner; }
    public double getReservePrice() { return reservePrice; }
    public Date getEndTime() { return endTime; }
}