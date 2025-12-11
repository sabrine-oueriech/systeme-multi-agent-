package auction;

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.*;

public class AuctionSystemLauncher {
    
    public static void main(String[] args) {
        try {
            System.out.println("╔══════════════════════════════════════════════════╗");
            System.out.println("║   SYSTÈME MULTI-AGENTS D'ENCHÈRES - JADE        ║");
            System.out.println("║   ");
            System.out.println("\n");
            
            Runtime rt = Runtime.instance();
            
            Profile profile = new ProfileImpl();
            profile.setParameter(Profile.MAIN_HOST, "localhost");
            profile.setParameter(Profile.GUI, "true");
            
            AgentContainer mainContainer = rt.createMainContainer(profile);
            
            System.out.println("🚀 Lancement des agents...\n");
            
            // Phase 1: Agents Principaux
            System.out.println("📌 Phase 1: Agents Principaux");
            AgentController auctioneer = mainContainer.createNewAgent(
                "auctioneer", 
                "auction.agents.AuctioneerAgent", 
                new Object[]{}
            );
            auctioneer.start();
            Thread.sleep(500);
            
            AgentController monitor = mainContainer.createNewAgent(
                "monitor", 
                "auction.agents.MonitorAgent", 
                new Object[]{}
            );
            monitor.start();
            Thread.sleep(500);
            
            // Phase 2: Agents Support
            System.out.println("\n📌 Phase 2: Agents Support");
            AgentController bank = mainContainer.createNewAgent(
                "bank", 
                "auction.agents.BankAgent", 
                new Object[]{}
            );
            bank.start();
            Thread.sleep(500);
            
            AgentController authenticator = mainContainer.createNewAgent(
                "authenticator", 
                "auction.agents.AuthenticatorAgent", 
                new Object[]{}
            );
            authenticator.start();
            Thread.sleep(500);
            
            AgentController marketAnalyst = mainContainer.createNewAgent(
                "analyst", 
                "auction.agents.MarketAnalystAgent", 
                new Object[]{}
            );
            marketAnalyst.start();
            Thread.sleep(500);
            
            AgentController logistics = mainContainer.createNewAgent(
                "logistics", 
                "auction.agents.LogisticsAgent", 
                new Object[]{}
            );
            logistics.start();
            Thread.sleep(500);
            
            AgentController notification = mainContainer.createNewAgent(
                "notifier", 
                "auction.agents.NotificationAgent", 
                new Object[]{}
            );
            notification.start();
            Thread.sleep(500);
            
            AgentController regulator = mainContainer.createNewAgent(
                "regulator", 
                "auction.agents.RegulatorAgent", 
                new Object[]{}
            );
            regulator.start();
            Thread.sleep(500);
            
            AgentController coalition = mainContainer.createNewAgent(
                "coalition", 
                "auction.agents.CoalitionAgent", 
                new Object[]{}
            );
            coalition.start();
            Thread.sleep(500);
            
            // Phase 3: Agents Acheteurs
            System.out.println("\n📌 Phase 3: Agents Acheteurs");
            
            for (int i = 1; i <= 2; i++) {
                AgentController aggressive = mainContainer.createNewAgent(
                    "aggressive" + i, 
                    "auction.agents.AggressiveBidderAgent", 
                    new Object[]{5000.0 + Math.random() * 5000}
                );
                aggressive.start();
                Thread.sleep(300);
            }
            
            for (int i = 1; i <= 2; i++) {
                AgentController conservative = mainContainer.createNewAgent(
                    "conservative" + i, 
                    "auction.agents.ConservativeBidderAgent", 
                    new Object[]{3000.0 + Math.random() * 3000}
                );
                conservative.start();
                Thread.sleep(300);
            }
            
            for (int i = 1; i <= 2; i++) {
                AgentController intelligent = mainContainer.createNewAgent(
                    "intelligent" + i, 
                    "auction.agents.IntelligentBidderAgent", 
                    new Object[]{8000.0 + Math.random() * 4000}
                );
                intelligent.start();
                Thread.sleep(300);
            }
            
            System.out.println("\n✅ Tous les agents sont démarrés!");
            System.out.println("\n╔══════════════════════════════════════════════════╗");
            System.out.println("║  SYSTÈME OPÉRATIONNEL                            ║");
            System.out.println("║  - 12 Agents actifs                              ║");
            System.out.println("║  - 6 Acheteurs en compétition                    ║");
            System.out.println("║  - Enchères automatiques en cours                ║");
            System.out.println("╚══════════════════════════════════════════════════╝\n");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}