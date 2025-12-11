package auction.agents;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import java.util.*;

public class RegulatorAgent extends Agent {
    private List<Violation> violations;
    private Map<String, Integer> penaltyPoints;
    private Map<String, Double> fines;
    
    class Violation {
        String agentId;
        String violationType;
        String description;
        Date timestamp;
        
        Violation(String agentId, String type, String desc) {
            this.agentId = agentId;
            this.violationType = type;
            this.description = desc;
            this.timestamp = new Date();
        }
    }
    
    @Override
    protected void setup() {
        System.out.println("⚖️ RegulatorAgent " + getLocalName() + " démarré!");
        
        violations = new ArrayList<>();
        penaltyPoints = new HashMap<>();
        fines = new HashMap<>();
        
        registerToDF();
        
        addBehaviour(new MonitorComplianceBehaviour());
        addBehaviour(new HandleDisputesBehaviour());
        addBehaviour(new EnforceRulesBehaviour(this, 10000));
        
        System.out.println("✅ Régulation du marché active");
    }
    
    private void registerToDF() {
        try {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setType("regulator-service");
            sd.setName("market-regulation");
            dfd.addServices(sd);
            DFService.register(this, dfd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    class MonitorComplianceBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = receive();
            
            if (msg != null) {
                String content = msg.getContent();
                String[] parts = content.split(";");
                String sender = msg.getSender().getLocalName();
                
                if (parts[0].equals("REPORT_VIOLATION")) {
                    String violatorId = parts[1];
                    String violationType = parts[2];
                    String description = parts.length > 3 ? parts[3] : "";
                    
                    Violation v = new Violation(violatorId, violationType, description);
                    violations.add(v);
                    
                    penaltyPoints.put(violatorId, 
                        penaltyPoints.getOrDefault(violatorId, 0) + calculatePenalty(violationType));
                    
                    System.out.println("⚠️ Violation reportée: " + violatorId + 
                                     " (" + violationType + ")");
                    
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent("VIOLATION_RECORDED");
                    send(reply);
                    
                    if (penaltyPoints.get(violatorId) > 10) {
                        imposeSanction(violatorId);
                    }
                }
            } else {
                block();
            }
        }
        
        private int calculatePenalty(String type) {
            switch (type) {
                case "PRICE_MANIPULATION": return 5;
                case "LATE_PAYMENT": return 2;
                case "FALSE_BID": return 3;
                case "COLLUSION": return 10;
                default: return 1;
            }
        }
    }
    
    class HandleDisputesBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
            
            if (msg != null) {
                String content = msg.getContent();
                String[] parts = content.split(";");
                
                if (parts[0].equals("RESOLVE_DISPUTE")) {
                    String party1 = parts[1];
                    String party2 = parts[2];
                    String issue = parts.length > 3 ? parts[3] : "Unknown";
                    
                    System.out.println("⚖️ Résolution de litige: " + party1 + " vs " + party2);
                    System.out.println("   Motif: " + issue);
                    
                    String decision = arbitrateDispute(party1, party2, issue);
                    
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent("DISPUTE_RESOLVED;" + decision);
                    send(reply);
                    
                    System.out.println("   Décision: " + decision);
                }
            } else {
                block();
            }
        }
        
        private String arbitrateDispute(String p1, String p2, String issue) {
            int points1 = penaltyPoints.getOrDefault(p1, 0);
            int points2 = penaltyPoints.getOrDefault(p2, 0);
            
            if (points1 > points2) {
                return "FAULT_" + p1;
            } else if (points2 > points1) {
                return "FAULT_" + p2;
            } else {
                return "SHARED_RESPONSIBILITY";
            }
        }
    }
    
    class EnforceRulesBehaviour extends TickerBehaviour {
        public EnforceRulesBehaviour(Agent a, long period) {
            super(a, period);
        }
        
        @Override
        protected void onTick() {
            if (!violations.isEmpty() || !penaltyPoints.isEmpty()) {
                System.out.println("\n⚖️ RAPPORT DE RÉGULATION");
                System.out.println("  Total violations: " + violations.size());
                System.out.println("  Agents avec pénalités: " + penaltyPoints.size());
                
                for (Map.Entry<String, Integer> entry : penaltyPoints.entrySet()) {
                    if (entry.getValue() > 5) {
                        System.out.println("  ⚠️ " + entry.getKey() + ": " + 
                                         entry.getValue() + " points de pénalité");
                    }
                }
                
                if (!fines.isEmpty()) {
                    double totalFines = fines.values().stream().mapToDouble(Double::doubleValue).sum();
                    System.out.println("  💰 Amendes totales: " + 
                                     String.format("%.2f€", totalFines));
                }
            }
        }
    }
    
    private void imposeSanction(String agentId) {
        double fine = 100 + Math.random() * 400;
        fines.put(agentId, fines.getOrDefault(agentId, 0.0) + fine);
        
        System.out.println("🚨 SANCTION imposée à " + agentId + ": " + 
                         String.format("%.2f€", fine));
        
        ACLMessage notif = new ACLMessage(ACLMessage.INFORM);
        notif.setContent("SANCTION_IMPOSED;" + fine);
        notif.addReceiver(new jade.core.AID(agentId, jade.core.AID.ISLOCALNAME));
        send(notif);
    }
    
    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (Exception e) {}
        System.out.println("🛑 RegulatorAgent terminé");
    }
}
