package auction.agents;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import java.util.*;

public class LogisticsAgent extends Agent {
    private Map<String, DeliveryInfo> deliveries;
    
    class DeliveryInfo {
        String itemId;
        String buyer;
        String destination;
        double shippingCost;
        Date estimatedDelivery;
        String status;
        
        DeliveryInfo(String itemId, String buyer, String destination) {
            this.itemId = itemId;
            this.buyer = buyer;
            this.destination = destination;
            this.shippingCost = calculateShippingCost(destination);
            this.estimatedDelivery = calculateDeliveryDate();
            this.status = "PENDING";
        }
        
        private double calculateShippingCost(String dest) {
            return 10 + Math.random() * 40;
        }
        
        private Date calculateDeliveryDate() {
            long daysToAdd = 2 + (long)(Math.random() * 5);
            return new Date(System.currentTimeMillis() + daysToAdd * 86400000);
        }
    }
    
    @Override
    protected void setup() {
        System.out.println("🚚 LogisticsAgent " + getLocalName() + " démarré!");
        
        deliveries = new HashMap<>();
        
        registerToDF();
        
        addBehaviour(new ProcessDeliveryRequestsBehaviour());
        addBehaviour(new UpdateDeliveryStatusBehaviour(this, 6000));
        
        System.out.println("✅ Services logistiques actifs");
    }
    
    private void registerToDF() {
        try {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setType("logistics-service");
            sd.setName("delivery-management");
            dfd.addServices(sd);
            DFService.register(this, dfd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    class ProcessDeliveryRequestsBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = receive();
            
            if (msg != null) {
                String content = msg.getContent();
                String[] parts = content.split(";");
                
                if (parts[0].equals("ARRANGE_DELIVERY")) {
                    String itemId = parts[1];
                    String buyer = parts[2];
                    String destination = parts.length > 3 ? parts[3] : "Default Location";
                    
                    DeliveryInfo delivery = new DeliveryInfo(itemId, buyer, destination);
                    deliveries.put(itemId, delivery);
                    
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent("DELIVERY_ARRANGED;" + delivery.shippingCost + ";" + 
                                   delivery.estimatedDelivery);
                    send(reply);
                    
                    System.out.println("📦 Livraison organisée: " + itemId + " -> " + buyer);
                    System.out.println("   Coût: " + String.format("%.2f€", delivery.shippingCost));
                    System.out.println("   Livraison estimée: " + delivery.estimatedDelivery);
                }
            } else {
                block();
            }
        }
    }
    
    class UpdateDeliveryStatusBehaviour extends TickerBehaviour {
        public UpdateDeliveryStatusBehaviour(Agent a, long period) {
            super(a, period);
        }
        
        @Override
        protected void onTick() {
            Date now = new Date();
            
            for (DeliveryInfo delivery : deliveries.values()) {
                if (delivery.status.equals("PENDING") && Math.random() > 0.7) {
                    delivery.status = "IN_TRANSIT";
                    System.out.println("🚚 " + delivery.itemId + " en transit vers " + delivery.buyer);
                } else if (delivery.status.equals("IN_TRANSIT") && 
                          now.after(delivery.estimatedDelivery)) {
                    delivery.status = "DELIVERED";
                    System.out.println("✅ " + delivery.itemId + " livré à " + delivery.buyer);
                    
                    ACLMessage notif = new ACLMessage(ACLMessage.INFORM);
                    notif.setContent("ITEM_DELIVERED;" + delivery.itemId);
                    notif.addReceiver(new jade.core.AID(delivery.buyer, jade.core.AID.ISLOCALNAME));
                    send(notif);
                }
            }
        }
    }
    
    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (Exception e) {}
        System.out.println("🛑 LogisticsAgent terminé");
    }
}
