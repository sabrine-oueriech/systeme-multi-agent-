package auction.agents;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import java.util.*;

public class ConservativeBidderAgent extends Agent {
    private double budget;
    private Map<String, AuctionInfo> trackedAuctions;
    
    class AuctionInfo {
        String id;
        double currentPrice;
        long lastUpdate;
        
        AuctionInfo(String id, double price) {
            this.id = id;
            this.currentPrice = price;
            this.lastUpdate = System.currentTimeMillis();
        }
    }
    
    @Override
    protected void setup() {
        System.out.println("🐢 ConservativeBidder " + getLocalName() + " démarré!");
        
        Object[] args = getArguments();
        budget = (args != null && args.length > 0) ? (Double) args[0] : 3000.0;
        trackedAuctions = new HashMap<>();
        
        registerToDF();
        
        addBehaviour(new ListenAuctionsBehaviour());
        addBehaviour(new ConservativeBiddingBehaviour(this, 3000));
        
        System.out.println("💰 Budget: " + budget + "€ - Style: Conservateur (Sniping)");
    }
    
    private void registerToDF() {
        try {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setType("bidder-service");
            sd.setName("conservative-bidder");
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
                    trackedAuctions.put(itemId, new AuctionInfo(itemId, startPrice));
                } else if (parts[0].equals("BID_UPDATE")) {
                    String itemId = parts[1];
                    double newPrice = Double.parseDouble(parts[2]);
                    if (trackedAuctions.containsKey(itemId)) {
                        AuctionInfo info = trackedAuctions.get(itemId);
                        info.currentPrice = newPrice;
                        info.lastUpdate = System.currentTimeMillis();
                    }
                }
            } else {
                block();
            }
        }
    }
    
    class ConservativeBiddingBehaviour extends TickerBehaviour {
        public ConservativeBiddingBehaviour(Agent a, long period) {
            super(a, period);
        }
        
        @Override
        protected void onTick() {
            long now = System.currentTimeMillis();
            
            for (AuctionInfo info : trackedAuctions.values()) {
                long timeSinceUpdate = now - info.lastUpdate;
                
                if (timeSinceUpdate > 240000) {
                    double maxBid = budget * 0.4;
                    double myBid = info.currentPrice * 1.05;
                    
                    if (myBid <= maxBid) {
                        placeBid(info.id, myBid);
                        System.out.println("🎯 " + getLocalName() + " SNIPING sur " + info.id);
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
        System.out.println("🛑 ConservativeBidder terminé");
    }
}
