package auction.agents;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import auction.models.*;
import auction.web.WebInterface;  // ← NOUVEAU
import java.util.*;

public class AuctioneerAgent extends Agent {
    private Map<String, AuctionItem> activeAuctions;
    private Map<String, List<Bid>> bidHistory;
    private String auctionType;
    
    @Override
    protected void setup() {
        System.out.println("🏛️ AuctioneerAgent " + getLocalName() + " démarré!");
        
        activeAuctions = new HashMap<>();
        bidHistory = new HashMap<>();
        auctionType = "ENGLISH";
        
        registerToDF();
        
        addBehaviour(new CreateAuctionsBehaviour(this, 5000));
        addBehaviour(new ReceiveBidsBehaviour());
        addBehaviour(new CheckAuctionEndBehaviour(this, 2000));
        
        // ✨ NOUVEAU: Logger vers l'interface web
        WebInterface.log("AuctioneerAgent démarré", "success");
        
        System.out.println("✅ Auctioneer prêt à gérer les enchères");
    }
    
    private void registerToDF() {
        try {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setType("auction-service");
            sd.setName("auction-management");
            dfd.addServices(sd);
            DFService.register(this, dfd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    class CreateAuctionsBehaviour extends TickerBehaviour {
        private int auctionCounter = 0;
        
        public CreateAuctionsBehaviour(Agent a, long period) {
            super(a, period);
        }
        
        @Override
        protected void onTick() {
            if (activeAuctions.size() < 5) {
                String itemId = "ITEM-" + (++auctionCounter);
                double startPrice = 100 + Math.random() * 900;
                double reservePrice = startPrice * 1.5;
                
                AuctionItem item = new AuctionItem(
                    itemId,
                    "Article " + auctionCounter,
                    startPrice,
                    reservePrice
                );
                
                activeAuctions.put(itemId, item);
                bidHistory.put(itemId, new ArrayList<>());
                
                System.out.println("🆕 Nouvelle enchère: " + itemId + " - Prix départ: " + startPrice + "€");
                
                // ✨ NOUVEAU: Notifier l'interface web
                WebInterface.notifyNewAuction(itemId, item.getName(), startPrice, reservePrice);
                WebInterface.log("Nouvelle enchère créée: " + itemId, "info");
                
                // Mettre à jour les statistiques
                updateStatistics();
                
                broadcastNewAuction(item);
            }
        }
    }
    
    class ReceiveBidsBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));
            
            if (msg != null) {
                try {
                    String content = msg.getContent();
                    String[] parts = content.split(";");
                    String itemId = parts[0];
                    double bidAmount = Double.parseDouble(parts[1]);
                    String bidderId = msg.getSender().getLocalName();
                    
                    if (activeAuctions.containsKey(itemId)) {
                        AuctionItem item = activeAuctions.get(itemId);
                        
                        if (bidAmount > item.getCurrentPrice()) {
                            if (checkSolvency(bidderId, bidAmount)) {
                                item.setCurrentPrice(bidAmount);
                                item.setCurrentWinner(bidderId);
                                
                                Bid bid = new Bid(bidderId, itemId, bidAmount);
                                bidHistory.get(itemId).add(bid);
                                
                                System.out.println("✅ Offre acceptée: " + bidderId + " - " + bidAmount + "€ pour " + itemId);
                                
                                // ✨ NOUVEAU: Notifier l'interface web
                                WebInterface.notifyBidUpdate(itemId, bidAmount, bidderId);
                                WebInterface.log(String.format("%s a placé une offre de %.2f€ sur %s", 
                                               bidderId, bidAmount, itemId), "info");
                                
                                // Mettre à jour les statistiques
                                updateStatistics();
                                
                                ACLMessage reply = msg.createReply();
                                reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                                reply.setContent("Offre acceptée: " + bidAmount + "€");
                                send(reply);
                                
                                notifyBidUpdate(itemId, bidAmount, bidderId);
                            } else {
                                rejectBid(msg, "Fonds insuffisants");
                                // ✨ NOUVEAU: Logger le rejet
                                WebInterface.log(bidderId + " - Offre rejetée: fonds insuffisants", "warning");
                            }
                        } else {
                            rejectBid(msg, "Offre trop basse");
                            WebInterface.log(bidderId + " - Offre trop basse", "warning");
                        }
                    } else {
                        rejectBid(msg, "Enchère inexistante");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                block();
            }
        }
    }
    
    class CheckAuctionEndBehaviour extends TickerBehaviour {
        public CheckAuctionEndBehaviour(Agent a, long period) {
            super(a, period);
        }
        
        @Override
        protected void onTick() {
            Date now = new Date();
            List<String> toRemove = new ArrayList<>();
            
            for (Map.Entry<String, AuctionItem> entry : activeAuctions.entrySet()) {
                AuctionItem item = entry.getValue();
                
                if (now.after(item.getEndTime())) {
                    String winner = item.getCurrentWinner();
                    double finalPrice = item.getCurrentPrice();
                    
                    System.out.println("🏁 Enchère terminée: " + item.getId());
                    
                    if (winner != null && finalPrice >= item.getReservePrice()) {
                        System.out.println("🎉 Gagnant: " + winner + " - Prix: " + finalPrice + "€");
                        
                        // ✨ NOUVEAU: Notifier l'interface web
                        WebInterface.notifyAuctionEnd(item.getId(), winner, finalPrice);
                        WebInterface.log(String.format("🎉 Enchère terminée - Gagnant: %s (%.2f€)", 
                                       winner, finalPrice), "success");
                        
                        notifyWinner(winner, item);
                        processPayment(winner, finalPrice);
                    } else {
                        System.out.println("❌ Enchère échouée - Prix de réserve non atteint");
                        WebInterface.log("Enchère " + item.getId() + " échouée", "error");
                    }
                    
                    toRemove.add(entry.getKey());
                }
            }
            
            toRemove.forEach(activeAuctions::remove);
            
            // Mettre à jour les statistiques après suppression
            if (!toRemove.isEmpty()) {
                updateStatistics();
            }
        }
    }
    
    // ✨ NOUVEAU: Méthode pour mettre à jour les statistiques
    private void updateStatistics() {
        int activeAuctionCount = activeAuctions.size();
        
        // Compter les agents (simplification)
        int agentCount = 15; // TODO: compter dynamiquement
        
        // Calculer le volume total
        double totalVolume = activeAuctions.values().stream()
            .mapToDouble(AuctionItem::getCurrentPrice)
            .sum();
        
        WebInterface.updateStatistics(activeAuctionCount, agentCount, totalVolume);
    }
    
    // Méthodes existantes (inchangées)
    private boolean checkSolvency(String bidderId, double amount) {
        return true;
    }
    
    private void rejectBid(ACLMessage originalMsg, String reason) {
        ACLMessage reply = originalMsg.createReply();
        reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
        reply.setContent(reason);
        send(reply);
    }
    
    private void broadcastNewAuction(AuctionItem item) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setContent("NEW_AUCTION;" + item.getId() + ";" + item.getCurrentPrice());
        
        try {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("bidder-service");
            template.addServices(sd);
            
            DFAgentDescription[] results = DFService.search(this, template);
            for (DFAgentDescription dfd : results) {
                msg.addReceiver(dfd.getName());
            }
            
            send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void notifyBidUpdate(String itemId, double newPrice, String bidder) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setContent("BID_UPDATE;" + itemId + ";" + newPrice + ";" + bidder);
        
        try {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("bidder-service");
            template.addServices(sd);
            
            DFAgentDescription[] results = DFService.search(this, template);
            for (DFAgentDescription dfd : results) {
                if (!dfd.getName().getLocalName().equals(bidder)) {
                    msg.addReceiver(dfd.getName());
                }
            }
            
            send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void notifyWinner(String winner, AuctionItem item) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setContent("YOU_WON;" + item.getId() + ";" + item.getCurrentPrice());
        msg.addReceiver(new jade.core.AID(winner, jade.core.AID.ISLOCALNAME));
        send(msg);
    }
    
    private void processPayment(String buyer, double amount) {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.setContent("PROCESS_PAYMENT;" + buyer + ";" + amount);
        msg.addReceiver(new jade.core.AID("bank", jade.core.AID.ISLOCALNAME));
        send(msg);
    }
    
    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (Exception e) {}
        WebInterface.log("AuctioneerAgent arrêté", "info");
        System.out.println("🛑 AuctioneerAgent terminé");
    }
}