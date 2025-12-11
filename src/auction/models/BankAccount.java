package auction.models;

import java.io.Serializable;
import java.util.*;

public class BankAccount implements Serializable {
    private String ownerId;
    private double balance;
    private double blockedAmount;
    private List<Transaction> transactions;
    
    public BankAccount(String ownerId, double initialBalance) {
        this.ownerId = ownerId;
        this.balance = initialBalance;
        this.blockedAmount = 0;
        this.transactions = new ArrayList<>();
    }
    
    public boolean hasSufficientFunds(double amount) {
        return (balance - blockedAmount) >= amount;
    }
    
    public boolean blockFunds(double amount) {
        if (hasSufficientFunds(amount)) {
            blockedAmount += amount;
            return true;
        }
        return false;
    }
    
    public void releaseFunds(double amount) {
        blockedAmount -= amount;
    }
    
    public boolean debit(double amount) {
        if (balance >= amount) {
            balance -= amount;
            transactions.add(new Transaction("DEBIT", amount, new Date()));
            return true;
        }
        return false;
    }
    
    public void credit(double amount) {
        balance += amount;
        transactions.add(new Transaction("CREDIT", amount, new Date()));
    }
    
    public double getBalance() { return balance; }
    public double getAvailableBalance() { return balance - blockedAmount; }
}