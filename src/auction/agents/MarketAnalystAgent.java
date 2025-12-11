package auction.agents;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import auction.models.*;
import java.util.*;

public class MarketAnalystAgent extends Agent {
    private Map<String, List<Double>> priceHistory;
    private Map<String, Double> volatilityIndex;
    private MarketStatistics stats;
    
    @Override
    protected void setup() {
        System.out.println("📈 MarketAnalystAgent " + getLocalName() + " démarré!");
        
        priceHistory = new HashMap<>();
        volatilityIndex = new HashMap<>();
        stats = new MarketStatistics();
        
        registerToDF();
        
        addBehaviour(new CollectMarketDataBehaviour());
        addBehaviour(new AnalyzeMarketBehaviour(this, 7000));
        addBehaviour(new ProvideRecommendationsBehaviour());
        
        System.out.println("✅ Analyse de marché active");
    }
    
    private void registerToDF() {
        try {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setType("analyst-service");
            sd.setName("market-analysis");
            dfd.addServices(sd);
            DFService.register(this, dfd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    class CollectMarketDataBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
            
            if (msg != null) {
                String content = msg.getContent();
                String[] parts = content.split(";");
                
                if (parts[0].equals("BID_UPDATE")) {
                    String itemId = parts[1];
                    double price = Double.parseDouble(parts[2]);
                    
                    priceHistory.putIfAbsent(itemId, new ArrayList<>());
                    priceHistory.get(itemId).add(price);
                }
            } else {
                block();
            }
        }
    }
    
    class AnalyzeMarketBehaviour extends TickerBehaviour {
        public AnalyzeMarketBehaviour(Agent a, long period) {
            super(a, period);
        }
        
        @Override
        protected void onTick() {
            System.out.println("\n📊 ANALYSE DE MARCHÉ");
            System.out.println("-".repeat(50));
            
            double totalAvg = 0;
            int count = 0;
            
            for (Map.Entry<String, List<Double>> entry : priceHistory.entrySet()) {
                String itemId = entry.getKey();
                List<Double> prices = entry.getValue();
                
                if (prices.size() >= 2) {
                    double avg = prices.stream().mapToDouble(Double::doubleValue).average().orElse(0);
                    double volatility = calculateVolatility(prices);
                    
                    volatilityIndex.put(itemId, volatility);
                    totalAvg += avg;
                    count++;
                    
                    String trend = determineTrend(prices);
                    
                    System.out.println("  " + itemId + ":");
                    System.out.println("    Prix moyen: " + String.format("%.2f€", avg));
                    System.out.println("    Volatilité: " + String.format("%.2f%%", volatility * 100));
                    System.out.println("    Tendance: " + trend);
                }
            }
            
            if (count > 0) {
                stats.setAveragePrice(totalAvg / count);
                double avgVolatility = volatilityIndex.values().stream()
                    .mapToDouble(Double::doubleValue).average().orElse(0);
                stats.setVolatilityIndex(avgVolatility);
                
                System.out.println("\n  📈 Marché global:");
                System.out.println("    Prix moyen: " + String.format("%.2f€", stats.getAveragePrice()));
                System.out.println("    Volatilité: " + String.format("%.2f%%", 
                                 stats.getVolatilityIndex() * 100));
            }
            
            System.out.println("-".repeat(50) + "\n");
        }
        
        private double calculateVolatility(List<Double> prices) {
            double mean = prices.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            double variance = prices.stream()
                .mapToDouble(p -> Math.pow(p - mean, 2))
                .average()
                .orElse(0);
            return Math.sqrt(variance) / mean;
        }
        
        private String determineTrend(List<Double> prices) {
            if (prices.size() < 3) return "NEUTRE";
            
            double firstThird = prices.subList(0, prices.size() / 3).stream()
                .mapToDouble(Double::doubleValue).average().orElse(0);
            double lastThird = prices.subList(2 * prices.size() / 3, prices.size()).stream()
                .mapToDouble(Double::doubleValue).average().orElse(0);
            
            double change = (lastThird - firstThird) / firstThird;
            
            if (change > 0.1) return "📈 HAUSSIÈRE";
            if (change < -0.1) return "📉 BAISSIÈRE";
            return "➡️ STABLE";
        }
    }
    
    class ProvideRecommendationsBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
            
            if (msg != null) {
                String content = msg.getContent();
                String[] parts = content.split(";");
                
                if (parts[0].equals("GET_RECOMMENDATION")) {
                    String itemId = parts[1];
                    ACLMessage reply = msg.createReply();
                    
                    if (priceHistory.containsKey(itemId)) {
                        List<Double> prices = priceHistory.get(itemId);
                        double currentPrice = prices.get(prices.size() - 1);
                        double avgPrice = prices.stream().mapToDouble(Double::doubleValue)
                            .average().orElse(currentPrice);
                        
                        String recommendation;
                        if (currentPrice < avgPrice * 0.9) {
                            recommendation = "BUY;Prix attractif";
                        } else if (currentPrice > avgPrice * 1.2) {
                            recommendation = "WAIT;Prix trop élevé";
                        } else {
                            recommendation = "NEUTRAL;Prix correct";
                        }
                        
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent(recommendation);
                    } else {
                        reply.setPerformative(ACLMessage.FAILURE);
                        reply.setContent("NO_DATA");
                    }
                    
                    send(reply);
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
        System.out.println("🛑 MarketAnalystAgent terminé");
    }
}