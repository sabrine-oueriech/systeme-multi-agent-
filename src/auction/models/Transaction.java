package auction.models;

import java.io.Serializable;
import java.util.Date;

public class Transaction implements Serializable {
    private String type;
    private double amount;
    private Date timestamp;
    
    public Transaction(String type, double amount, Date timestamp) {
        this.type = type;
        this.amount = amount;
        this.timestamp = timestamp;
    }
}