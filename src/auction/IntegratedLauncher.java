// ============================================================================
// LAUNCHER INTÉGRÉ - JADE + JavaFX Interface
// ============================================================================
package auction;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.*;
import auction.ui.AuctionDashboard;

public class IntegratedLauncher {
    
    private static AgentContainer mainContainer;
    private static boolean jadeStarted = false;
    
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║  SYSTÈME MULTI-AGENTS D'ENCHÈRES                 ║");
        System.out.println("║  JADE Platform + Interface JavaFX Moderne        ║");
        System.out.println("╚══════════════════════════════════════════════════╝\n");
        
        // Démarrer JADE dans un thread séparé
        new Thread(() -> {
            try {
                startJadeSystem();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        
        // Attendre que JADE démarre
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Lancer l'interface JavaFX
        Application.launch(AuctionDashboard.class, args);
    }
    
    private static void startJadeSystem() {
        try {
            System.out.println("🚀 Démarrage de la plateforme JADE...\n");
            
            Runtime rt = Runtime.instance();
            Profile profile = new ProfileImpl();
            profile.setParameter(Profile.MAIN_HOST, "localhost");
            profile.setParameter(Profile.GUI, "false"); // Pas besoin de GUI JADE
            
            mainContainer = rt.createMainContainer(profile);
            jadeStarted = true;
            
            System.out.println("✅ Plateforme JADE démarrée\n");
            System.out.println("📌 Phase 1: Lancement des agents principaux...");
            
            // Agents Principaux
            createAgent("auctioneer", "auction.agents.AuctioneerAgent");
            Thread.sleep(500);
            
            createAgent("monitor", "auction.agents.MonitorAgent");
            Thread.sleep(500);
            
            System.out.println("\n📌 Phase 2: Lancement des agents support...");
            
            // Agents Support
            createAgent("bank", "auction.agents.BankAgent");
            Thread.sleep(500);
            
            createAgent("authenticator", "auction.agents.AuthenticatorAgent");
            Thread.sleep(500);
            
            createAgent("analyst", "auction.agents.MarketAnalystAgent");
            Thread.sleep(500);
            
            createAgent("logistics", "auction.agents.LogisticsAgent");
            Thread.sleep(500);
            
            createAgent("notifier", "auction.agents.NotificationAgent");
            Thread.sleep(500);
            
            createAgent("regulator", "auction.agents.RegulatorAgent");
            Thread.sleep(500);
            
            createAgent("coalition", "auction.agents.CoalitionAgent");
            Thread.sleep(500);
            
            System.out.println("\n📌 Phase 3: Lancement des agents acheteurs...");
            
            // Acheteurs Agressifs
            for (int i = 1; i <= 2; i++) {
                createAgent("aggressive" + i, 
                          "auction.agents.AggressiveBidderAgent",
                          new Object[]{5000.0 + Math.random() * 5000});
                Thread.sleep(300);
            }
            
            // Acheteurs Conservateurs
            for (int i = 1; i <= 2; i++) {
                createAgent("conservative" + i, 
                          "auction.agents.ConservativeBidderAgent",
                          new Object[]{3000.0 + Math.random() * 3000});
                Thread.sleep(300);
            }
            
            // Acheteurs Intelligents
            for (int i = 1; i <= 2; i++) {
                createAgent("intelligent" + i, 
                          "auction.agents.IntelligentBidderAgent",
                          new Object[]{8000.0 + Math.random() * 4000});
                Thread.sleep(300);
            }
            
            System.out.println("\n╔══════════════════════════════════════════════════╗");
            System.out.println("║  ✅ TOUS LES AGENTS SONT OPÉRATIONNELS          ║");
            System.out.println("║  📊 Interface JavaFX en cours de chargement...  ║");
            System.out.println("╚══════════════════════════════════════════════════╝\n");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du démarrage de JADE:");
            e.printStackTrace();
        }
    }
    
    private static void createAgent(String name, String className) throws StaleProxyException {
        createAgent(name, className, new Object[]{});
    }
    
    private static void createAgent(String name, String className, Object[] args) 
            throws StaleProxyException {
        AgentController agent = mainContainer.createNewAgent(name, className, args);
        agent.start();
        System.out.println("  ✓ Agent créé: " + name);
    }
    
    public static AgentContainer getMainContainer() {
        return mainContainer;
    }
    
    public static boolean isJadeStarted() {
        return jadeStarted;
    }
}