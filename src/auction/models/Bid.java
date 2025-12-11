package auction.models;

import java.io.Serializable;
import java.util.Date;

public class Bid implements Serializable {
    private String bidderId;
    private String itemId;
    private double amount;
    private Date timestamp;
    
    public Bid(String bidderId, String itemId, double amount) {
        this.bidderId = bidderId;
        this.itemId = itemId;
        this.amount = amount;
        this.timestamp = new Date();
    }
    
    public String getBidderId() { return bidderId; }
    public String getItemId() { return itemId; }
    public double getAmount() { return amount; }
    public Date getTimestamp() { return timestamp; }
}