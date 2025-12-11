package auction.agents;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import java.util.*;

public class MonitorAgent extends Agent {
    private Map<String, Integer> bidCounts;
    private Map<String, Double> totalSpent;
    private int totalAuctions;
    private List<String> activityLog;
    
    @Override
    protected void setup() {
        System.out.println("📊 MonitorAgent " + getLocalName() + " démarré!");
        
        bidCounts = new HashMap<>();
        totalSpent = new HashMap<>();
        activityLog = new ArrayList<>();
        totalAuctions = 0;
        
        registerToDF();
        
        addBehaviour(new MonitorCommunicationsBehaviour());
        addBehaviour(new GenerateReportsBehaviour(this, 10000));
        addBehaviour(new DetectAnomaliesBehaviour(this, 5000));
        
        System.out.println("✅ Monitoring système actif");
    }
    
    private void registerToDF() {
        try {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setType("monitor-service");
            sd.setName("system-monitor");
            dfd.addServices(sd);
            DFService.register(this, dfd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    class MonitorCommunicationsBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = receive();
            
            if (msg != null) {
                String sender = msg.getSender().getLocalName();
                String content = msg.getContent();
                int performative = msg.getPerformative();
                
                String logEntry = String.format("[%s] %s -> %s: %s", 
                    new Date(), sender, ACLMessage.getPerformative(performative), content);
                activityLog.add(logEntry);
                
                if (performative == ACLMessage.PROPOSE) {
                    bidCounts.put(sender, bidCounts.getOrDefault(sender, 0) + 1);
                }
                
                if (content != null && content.contains("YOU_WON")) {
                    String[] parts = content.split(";");
                    if (parts.length >= 3) {
                        double amount = Double.parseDouble(parts[2]);
                        totalSpent.put(sender, totalSpent.getOrDefault(sender, 0.0) + amount);
                    }
                }
            } else {
                block();
            }
        }
    }
    
    class GenerateReportsBehaviour extends TickerBehaviour {
        public GenerateReportsBehaviour(Agent a, long period) {
            super(a, period);
        }
        
        @Override
        protected void onTick() {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("📈 RAPPORT DE MONITORING - " + new Date());
            System.out.println("=".repeat(60));
            
            System.out.println("\n🎯 Activité des enchérisseurs:");
            for (Map.Entry<String, Integer> entry : bidCounts.entrySet()) {
                System.out.println("  • " + entry.getKey() + ": " + entry.getValue() + " offres");
            }
            
            System.out.println("\n💰 Dépenses totales:");
            double grandTotal = 0;
            for (Map.Entry<String, Double> entry : totalSpent.entrySet()) {
                System.out.println("  • " + entry.getKey() + ": " + 
                                 String.format("%.2f€", entry.getValue()));
                grandTotal += entry.getValue();
            }
            System.out.println("  TOTAL: " + String.format("%.2f€", grandTotal));
            
            System.out.println("\n📝 Dernières activités:");
            int start = Math.max(0, activityLog.size() - 5);
            for (int i = start; i < activityLog.size(); i++) {
                System.out.println("  " + activityLog.get(i));
            }
            
            System.out.println("=".repeat(60) + "\n");
        }
    }
    
    class DetectAnomaliesBehaviour extends TickerBehaviour {
        public DetectAnomaliesBehaviour(Agent a, long period) {
            super(a, period);
        }
        
        @Override
        protected void onTick() {
            for (Map.Entry<String, Integer> entry : bidCounts.entrySet()) {
                if (entry.getValue() > 50) {
                    System.out.println("⚠️ ANOMALIE: " + entry.getKey() + 
                                     " a fait " + entry.getValue() + " offres (possible bot)");
                }
            }
            
            for (Map.Entry<String, Double> entry : totalSpent.entrySet()) {
                if (entry.getValue() > 50000) {
                    System.out.println("⚠️ ANOMALIE: " + entry.getKey() + 
                                     " a dépensé " + entry.getValue() + "€ (budget suspect)");
                }
            }
        }
    }
    
    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (Exception e) {}
        System.out.println("🛑 MonitorAgent terminé");
    }
}
