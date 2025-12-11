package auction.agents;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import java.util.*;

public class IntelligentBidderAgent extends Agent {
    private double budget;
    private Map<String, List<Double>> priceHistory;
    private Map<String, Double> predictedMaxPrices;
    
    @Override
    protected void setup() {
        System.out.println("🧠 IntelligentBidder " + getLocalName() + " démarré!");
        
        Object[] args = getArguments();
        budget = (args != null && args.length > 0) ? (Double) args[0] : 10000.0;
        priceHistory = new HashMap<>();
        predictedMaxPrices = new HashMap<>();
        
        registerToDF();
        
        addBehaviour(new ListenAuctionsBehaviour());
        addBehaviour(new IntelligentBiddingBehaviour(this, 2000));
        addBehaviour(new LearnPatternsBehaviour(this, 5000));
        
        System.out.println("💰 Budget: " + budget + "€ - Style: Intelligence Artificielle");
    }
    
    private void registerToDF() {
        try {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setType("bidder-service");
            sd.setName("intelligent-bidder");
            dfd.addServices(sd);
            DFService.register(this, dfd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    class ListenAuctionsBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
            
            if (msg != null) {
                String content = msg.getContent();
                String[] parts = content.split(";");
                
                if (parts[0].equals("NEW_AUCTION")) {
                    String itemId = parts[1];
                    double startPrice = Double.parseDouble(parts[2]);
                    priceHistory.putIfAbsent(itemId, new ArrayList<>());
                    priceHistory.get(itemId).add(startPrice);
                } else if (parts[0].equals("BID_UPDATE")) {
                    String itemId = parts[1];
                    double newPrice = Double.parseDouble(parts[2]);
                    if (priceHistory.containsKey(itemId)) {
                        priceHistory.get(itemId).add(newPrice);
                    }
                }
            } else {
                block();
            }
        }
    }
    
    class LearnPatternsBehaviour extends TickerBehaviour {
        public LearnPatternsBehaviour(Agent a, long period) {
            super(a, period);
        }
        
        @Override
        protected void onTick() {
            for (Map.Entry<String, List<Double>> entry : priceHistory.entrySet()) {
                String itemId = entry.getKey();
                List<Double> prices = entry.getValue();
                
                if (prices.size() >= 3) {
                    double avgGrowth = calculateAverageGrowth(prices);
                    double predictedMax = prices.get(prices.size() - 1) * (1 + avgGrowth * 3);
                    predictedMaxPrices.put(itemId, predictedMax);
                    
                    System.out.println("📊 " + getLocalName() + " prédit max pour " + 
                                     itemId + ": " + predictedMax + "€");
                }
            }
        }
        
        private double calculateAverageGrowth(List<Double> prices) {
            double totalGrowth = 0;
            for (int i = 1; i < prices.size(); i++) {
                totalGrowth += (prices.get(i) - prices.get(i-1)) / prices.get(i-1);
            }
            return totalGrowth / (prices.size() - 1);
        }
    }
    
    class IntelligentBiddingBehaviour extends TickerBehaviour {
        public IntelligentBiddingBehaviour(Agent a, long period) {
            super(a, period);
        }
        
        @Override
        protected void onTick() {
            for (Map.Entry<String, List<Double>> entry : priceHistory.entrySet()) {
                String itemId = entry.getKey();
                List<Double> prices = entry.getValue();
                
                if (prices.isEmpty()) continue;
                
                double currentPrice = prices.get(prices.size() - 1);
                Double predictedMax = predictedMaxPrices.get(itemId);
                
                if (predictedMax != null && predictedMax < budget * 0.6) {
                    double optimalBid = currentPrice * 1.08;
                    
                    if (optimalBid < predictedMax * 0.9) {
                        placeBid(itemId, optimalBid);
                        System.out.println("🤖 " + getLocalName() + " enchère stratégique: " + 
                                         optimalBid + "€");
                    }
                }
            }
        }
    }
    
    private void placeBid(String itemId, double amount) {
        try {
            ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
            msg.setContent(itemId + ";" + amount);
            
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("auction-service");
            template.addServices(sd);
            
            DFAgentDescription[] results = DFService.search(this, template);
            if (results.length > 0) {
                msg.addReceiver(results[0].getName());
                send(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (Exception e) {}
        System.out.println("🛑 IntelligentBidder terminé");
    }
}
