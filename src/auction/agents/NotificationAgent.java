package auction.agents;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import java.util.*;

public class NotificationAgent extends Agent {
    private Map<String, List<String>> subscriptions;
    
    @Override
    protected void setup() {
        System.out.println("🔔 NotificationAgent " + getLocalName() + " démarré!");
        
        subscriptions = new HashMap<>();
        
        registerToDF();
        
        addBehaviour(new ManageSubscriptionsBehaviour());
        addBehaviour(new SendNotificationsBehaviour());
        
        System.out.println("✅ Service de notifications actif");
    }
    
    private void registerToDF() {
        try {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setType("notification-service");
            sd.setName("alerts-management");
            dfd.addServices(sd);
            DFService.register(this, dfd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    class ManageSubscriptionsBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE));
            
            if (msg != null) {
                String agentId = msg.getSender().getLocalName();
                String content = msg.getContent();
                String[] parts = content.split(";");
                String eventType = parts[0];
                
                subscriptions.putIfAbsent(eventType, new ArrayList<>());
                if (!subscriptions.get(eventType).contains(agentId)) {
                    subscriptions.get(eventType).add(agentId);
                    System.out.println("📬 " + agentId + " abonné aux notifications: " + eventType);
                }
                
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.AGREE);
                reply.setContent("SUBSCRIBED");
                send(reply);
            } else {
                block();
            }
        }
    }
    
    class SendNotificationsBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
            
            if (msg != null) {
                String content = msg.getContent();
                String[] parts = content.split(";");
                
                if (parts[0].equals("BROADCAST")) {
                    String eventType = parts[1];
                    String message = parts.length > 2 ? parts[2] : "";
                    
                    if (subscriptions.containsKey(eventType)) {
                        for (String subscriberId : subscriptions.get(eventType)) {
                            ACLMessage notif = new ACLMessage(ACLMessage.INFORM);
                            notif.setContent("NOTIFICATION;" + eventType + ";" + message);
                            notif.addReceiver(new jade.core.AID(subscriberId, 
                                           jade.core.AID.ISLOCALNAME));
                            send(notif);
                        }
                        System.out.println("🔔 Notification envoyée (" + eventType + ") à " + 
                                         subscriptions.get(eventType).size() + " agents");
                    }
                }
            } else {
                block();
            }
        }
    }
    
    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (Exception e) {}
        System.out.println("🛑 NotificationAgent terminé");
    }
}