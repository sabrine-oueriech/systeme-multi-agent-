package auction;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.*;
import auction.web.WebInterface;
import auction.web.WebServer;

public class IntegratedWebLauncher {
    
    private static WebServer httpServer;
    
    public static void main(String[] args) {
        try {
            System.out.println("╔══════════════════════════════════════════════════╗");
            System.out.println("║   SYSTÈME MULTI-AGENTS D'ENCHÈRES - WEB         ║");
            System.out.println("║   JADE + WebSocket + Interface Moderne           ║");
            System.out.println("╚══════════════════════════════════════════════════╝\n");
            
            System.out.println("🔌 Démarrage du WebSocket Server...");
            WebInterface.start(9090);
            Thread.sleep(1000);
            
            System.out.println("🌐 Démarrage du serveur HTTP...");
            httpServer = new WebServer(8080, "web");
            httpServer.start();
            Thread.sleep(1000);
            
            System.out.println("\n🤖 Démarrage de la plateforme JADE...\n");
            
            jade.core.Runtime rt = jade.core.Runtime.instance();
            Profile profile = new ProfileImpl();
            profile.setParameter(Profile.MAIN_HOST, "localhost");
            profile.setParameter(Profile.GUI, "false");
            
            AgentContainer mainContainer = rt.createMainContainer(profile);
            
            System.out.println("📌 Phase 1: Agents Principaux");
            createAgent(mainContainer, "auctioneer", "auction.agents.AuctioneerAgent");
            Thread.sleep(500);
            
            createAgent(mainContainer, "monitor", "auction.agents.MonitorAgent");
            Thread.sleep(500);
            
            System.out.println("\n📌 Phase 2: Agents Support");
            createAgent(mainContainer, "bank", "auction.agents.BankAgent");
            Thread.sleep(500);
            
            createAgent(mainContainer, "authenticator", "auction.agents.AuthenticatorAgent");
            Thread.sleep(500);
            
            createAgent(mainContainer, "analyst", "auction.agents.MarketAnalystAgent");
            Thread.sleep(500);
            
            createAgent(mainContainer, "logistics", "auction.agents.LogisticsAgent");
            Thread.sleep(500);
            
            createAgent(mainContainer, "notifier", "auction.agents.NotificationAgent");
            Thread.sleep(500);
            
            createAgent(mainContainer, "regulator", "auction.agents.RegulatorAgent");
            Thread.sleep(500);
            
            createAgent(mainContainer, "coalition", "auction.agents.CoalitionAgent");
            Thread.sleep(500);
            
            System.out.println("\n📌 Phase 3: Agents Acheteurs");
            
            for (int i = 1; i <= 2; i++) {
                createAgent(mainContainer, "aggressive" + i, 
                          "auction.agents.AggressiveBidderAgent",
                          new Object[]{5000.0 + Math.random() * 5000});
                
                WebInterface.notifyAgentUpdate("aggressive" + i, "aggressive", 
                                              5000.0 + Math.random() * 5000, 0);
                Thread.sleep(300);
            }
            
            for (int i = 1; i <= 2; i++) {
                createAgent(mainContainer, "conservative" + i, 
                          "auction.agents.ConservativeBidderAgent",
                          new Object[]{3000.0 + Math.random() * 3000});
                
                WebInterface.notifyAgentUpdate("conservative" + i, "conservative", 
                                              3000.0 + Math.random() * 3000, 0);
                Thread.sleep(300);
            }
            
            for (int i = 1; i <= 2; i++) {
                createAgent(mainContainer, "intelligent" + i, 
                          "auction.agents.IntelligentBidderAgent",
                          new Object[]{8000.0 + Math.random() * 4000});
                
                WebInterface.notifyAgentUpdate("intelligent" + i, "intelligent", 
                                              8000.0 + Math.random() * 4000, 0);
                Thread.sleep(300);
            }
            
            // ✨ NOUVEAU : Envoyer les agents support à l'interface
            System.out.println("\n📊 Envoi des agents support à l'interface...");
            Thread.sleep(1000);
            
            String[][] supportAgents = {
                {"auctioneer", "support"},
                {"monitor", "support"},
                {"bank", "support"},
                {"authenticator", "support"},
                {"analyst", "support"},
                {"logistics", "support"},
                {"notifier", "support"},
                {"regulator", "support"},
                {"coalition", "support"}
            };
            
            for (String[] agent : supportAgents) {
                WebInterface.notifyAgentUpdate(agent[0], agent[1], 0, 0);
                System.out.println("  ✓ Agent envoyé: " + agent[0]);
                Thread.sleep(100);
            }
            
            System.out.println("✅ Tous les agents envoyés !");
            
            System.out.println("\n✅ Système complètement opérationnel !");
            System.out.println("\n╔══════════════════════════════════════════════════╗");
            System.out.println("║  🌐 Interface Web: http://localhost:8080        ║");
            System.out.println("║  🔌 WebSocket: ws://localhost:9090              ║");
            System.out.println("║  🤖 JADE: 15 agents actifs                      ║");
            System.out.println("║  📊 Données en temps réel activées              ║");
            System.out.println("╚══════════════════════════════════════════════════╝\n");
            
            System.out.println("💡 INSTRUCTIONS:");
            System.out.println("  1. Ouvrez votre navigateur");
            System.out.println("  2. Allez sur http://localhost:8080");
            System.out.println("  3. Les données s'afficheront en temps réel!\n");
            
            WebInterface.log("Système démarré avec succès!", "success");
            WebInterface.updateStatistics(0, 15, 0);
            
            java.lang.Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n🛑 Arrêt du système...");
                
                if (httpServer != null) {
                    httpServer.stop();
                }
                
                WebInterface.stop();
                
                System.out.println("✅ Système arrêté proprement");
            }));
            
        } catch (Exception e) {
            System.err.println("❌ ERREUR lors du démarrage:");
            e.printStackTrace();
            
            if (httpServer != null) {
                httpServer.stop();
            }
            WebInterface.stop();
        }
    }
    
    private static void createAgent(AgentContainer container, String name, 
                                   String className) throws Exception {
        createAgent(container, name, className, new Object[]{});
    }
    
    private static void createAgent(AgentContainer container, String name, 
                                   String className, Object[] args) throws Exception {
        AgentController agent = container.createNewAgent(name, className, args);
        agent.start();
        System.out.println("  ✓ Agent créé: " + name);
    }
}