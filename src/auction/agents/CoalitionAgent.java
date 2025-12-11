package auction.agents;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import java.util.*;

public class CoalitionAgent extends Agent {
    private Map<String, Coalition> activeCoalitions;
    private int coalitionCounter = 0;
    
    class Coalition {
        String id;
        List<String> members;
        double pooledBudget;
        String targetItem;
        
        Coalition(String id) {
            this.id = id;
            this.members = new ArrayList<>();
            this.pooledBudget = 0;
        }
        
        void addMember(String memberId, double contribution) {
            members.add(memberId);
            pooledBudget += contribution;
        }
    }
    
    @Override
    protected void setup() {
        System.out.println("🤝 CoalitionAgent " + getLocalName() + " démarré!");
        
        activeCoalitions = new HashMap<>();
        
        registerToDF();
        
        addBehaviour(new ManageCoalitionsBehaviour());
        addBehaviour(new CoordinateGroupBidsBehaviour());
        
        System.out.println("✅ Service de coalitions actif");
    }
    
    private void registerToDF() {
        try {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setType("coalition-service");
            sd.setName("group-bidding");
            dfd.addServices(sd);
            DFService.register(this, dfd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    class ManageCoalitionsBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = receive();
            
            if (msg != null) {
                String content = msg.getContent();
                String[] parts = content.split(";");
                String senderId = msg.getSender().getLocalName();
                
                if (parts[0].equals("CREATE_COALITION")) {
                    String coalitionId = "COALITION-" + (++coalitionCounter);
                    Coalition coalition = new Coalition(coalitionId);
                    activeCoalitions.put(coalitionId, coalition);
                    
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent("COALITION_CREATED;" + coalitionId);
                    send(reply);
                    
                    System.out.println("🤝 Coalition créée: " + coalitionId);
                    
                } else if (parts[0].equals("JOIN_COALITION")) {
                    String coalitionId = parts[1];
                    double contribution = Double.parseDouble(parts[2]);
                    
                    ACLMessage reply = msg.createReply();
                    
                    if (activeCoalitions.containsKey(coalitionId)) {
                        Coalition coalition = activeCoalitions.get(coalitionId);
                        coalition.addMember(senderId, contribution);
                        
                        reply.setPerformative(ACLMessage.AGREE);
                        reply.setContent("JOINED_COALITION");
                        
                        System.out.println("👥 " + senderId + " rejoint " + coalitionId + 
                                         " (contribution: " + contribution + "€)");
                    } else {
                        reply.setPerformative(ACLMessage.REFUSE);
                        reply.setContent("COALITION_NOT_FOUND");
                    }
                    
                    send(reply);
                    
                } else if (parts[0].equals("SET_TARGET")) {
                    String coalitionId = parts[1];
                    String itemId = parts[2];
                    
                    if (activeCoalitions.containsKey(coalitionId)) {
                        activeCoalitions.get(coalitionId).targetItem = itemId;
                        System.out.println("🎯 " + coalitionId + " cible: " + itemId);
                    }
                }
            } else {
                block();
            }
        }
    }
    
    class CoordinateGroupBidsBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
            
            if (msg != null) {
                String content = msg.getContent();
                String[] parts = content.split(";");
                
                if (parts[0].equals("PLACE_GROUP_BID")) {
                    String coalitionId = parts[1];
                    
                    if (activeCoalitions.containsKey(coalitionId)) {
                        Coalition coalition = activeCoalitions.get(coalitionId);
                        
                        if (coalition.targetItem != null) {
                            double groupBid = coalition.pooledBudget * 0.8;
                            
                            System.out.println("💪 " + coalitionId + " enchère groupée: " + 
                                             groupBid + "€ pour " + coalition.targetItem);
                            
                            ACLMessage bid = new ACLMessage(ACLMessage.PROPOSE);
                            bid.setContent(coalition.targetItem + ";" + groupBid);
                            
                            try {
                                DFAgentDescription template = new DFAgentDescription();
                                ServiceDescription sd = new ServiceDescription();
                                sd.setType("auction-service");
                                template.addServices(sd);
                                
                                DFAgentDescription[] results = DFService.search(myAgent, template);
                                if (results.length > 0) {
                                    bid.addReceiver(results[0].getName());
                                    send(bid);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
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
        System.out.println("🛑 CoalitionAgent terminé");
    }
}