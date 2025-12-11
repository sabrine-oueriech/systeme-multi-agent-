package auction.agents;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import java.util.*;

public class AggressiveBidderAgent extends Agent {
    private double budget;
    private Map<String, Double> trackedAuctions;
    private double aggressiveness = 1.2;
    
    @Override
    protected void setup() {
        System.out.println("🔥 AggressiveBidder " + getLocalName() + " démarré!");
        
        Object[] args = getArguments();
        budget = (args != null && args.length > 0) ? (Double) args[0] : 5000.0;
        trackedAuctions = new HashMap<>();
        
        registerToDF();
        
        addBehaviour(new ListenAuctionsBehaviour());
        addBehaviour(new AggressiveBiddingBehaviour(this, 1000));
        
        System.out.println("💰 Budget: " + budget + "€ - Style: Agressif");
    }
    
    private void registerToDF() {
        try {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setType("bidder-service");
            sd.setName("aggressive-bidder");
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
                    trackedAuctions.put(itemId, startPrice);
                    System.out.println("📢 " + getLocalName() + " détecte: " + itemId);
                } else if (parts[0].equals("BID_UPDATE")) {
                    String itemId = parts[1];
                    double newPrice = Double.parseDouble(parts[2]);
                    trackedAuctions.put(itemId, newPrice);
                }
            } else {
                block();
            }
        }
    }
    
    class AggressiveBiddingBehaviour extends TickerBehaviour {
        public AggressiveBiddingBehaviour(Agent a, long period) {
            super(a, period);
        }
        
        @Override
        protected void onTick() {
            for (Map.Entry<String, Double> entry : trackedAuctions.entrySet()) {
                String itemId = entry.getKey();
                double currentPrice = entry.getValue();
                double myBid = currentPrice * aggressiveness;
                
                if (myBid <= budget * 0.8) {
                    placeBid(itemId, myBid);
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
                System.out.println("💸 " + getLocalName() + " offre " + amount + "€ pour " + itemId);
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
        System.out.println("🛑 AggressiveBidder terminé");
    }
}