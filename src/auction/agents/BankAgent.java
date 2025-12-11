package auction.agents;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import auction.models.*;
import java.util.*;

public class BankAgent extends Agent {
    private Map<String, BankAccount> accounts;
    
    @Override
    protected void setup() {
        System.out.println("🏦 BankAgent " + getLocalName() + " démarré!");
        
        accounts = new HashMap<>();
        
        registerToDF();
        
        addBehaviour(new ProcessBankingRequestsBehaviour());
        addBehaviour(new CreateAccountsBehaviour(this, 3000));
        
        System.out.println("✅ Services bancaires actifs");
    }
    
    private void registerToDF() {
        try {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setType("bank-service");
            sd.setName("banking-service");
            dfd.addServices(sd);
            DFService.register(this, dfd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    class CreateAccountsBehaviour extends TickerBehaviour {
        public CreateAccountsBehaviour(Agent a, long period) {
            super(a, period);
        }
        
        @Override
        protected void onTick() {
            try {
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("bidder-service");
                template.addServices(sd);
                
                DFAgentDescription[] results = DFService.search(myAgent, template);
                
                for (DFAgentDescription dfd : results) {
                    String agentName = dfd.getName().getLocalName();
                    if (!accounts.containsKey(agentName)) {
                        double initialBalance = 5000 + Math.random() * 10000;
                        accounts.put(agentName, new BankAccount(agentName, initialBalance));
                        System.out.println("💳 Compte créé pour " + agentName + 
                                         " - Solde: " + String.format("%.2f€", initialBalance));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    class ProcessBankingRequestsBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = receive();
            
            if (msg != null) {
                String content = msg.getContent();
                String[] parts = content.split(";");
                
                if (parts[0].equals("CHECK_SOLVENCY")) {
                    handleSolvencyCheck(msg, parts);
                } else if (parts[0].equals("BLOCK_FUNDS")) {
                    handleBlockFunds(msg, parts);
                } else if (parts[0].equals("PROCESS_PAYMENT")) {
                    handlePayment(msg, parts);
                } else if (parts[0].equals("GET_BALANCE")) {
                    handleBalanceInquiry(msg, parts);
                }
            } else {
                block();
            }
        }
        
        private void handleSolvencyCheck(ACLMessage msg, String[] parts) {
            String agentId = parts[1];
            double amount = Double.parseDouble(parts[2]);
            
            ACLMessage reply = msg.createReply();
            
            if (accounts.containsKey(agentId)) {
                BankAccount account = accounts.get(agentId);
                if (account.hasSufficientFunds(amount)) {
                    reply.setPerformative(ACLMessage.CONFIRM);
                    reply.setContent("SOLVENT");
                } else {
                    reply.setPerformative(ACLMessage.DISCONFIRM);
                    reply.setContent("INSUFFICIENT_FUNDS");
                }
            } else {
                reply.setPerformative(ACLMessage.FAILURE);
                reply.setContent("ACCOUNT_NOT_FOUND");
            }
            
            send(reply);
        }
        
        private void handleBlockFunds(ACLMessage msg, String[] parts) {
            String agentId = parts[1];
            double amount = Double.parseDouble(parts[2]);
            
            ACLMessage reply = msg.createReply();
            
            if (accounts.containsKey(agentId)) {
                BankAccount account = accounts.get(agentId);
                if (account.blockFunds(amount)) {
                    reply.setPerformative(ACLMessage.CONFIRM);
                    reply.setContent("FUNDS_BLOCKED");
                    System.out.println("🔒 Fonds bloqués: " + amount + "€ pour " + agentId);
                } else {
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("INSUFFICIENT_FUNDS");
                }
            } else {
                reply.setPerformative(ACLMessage.FAILURE);
                reply.setContent("ACCOUNT_NOT_FOUND");
            }
            
            send(reply);
        }
        
        private void handlePayment(ACLMessage msg, String[] parts) {
            String buyer = parts[1];
            double amount = Double.parseDouble(parts[2]);
            
            ACLMessage reply = msg.createReply();
            
            if (accounts.containsKey(buyer)) {
                BankAccount account = accounts.get(buyer);
                if (account.debit(amount)) {
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent("PAYMENT_PROCESSED");
                    System.out.println("💸 Paiement traité: " + amount + "€ de " + buyer);
                } else {
                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent("PAYMENT_FAILED");
                }
            }
            
            send(reply);
        }
        
        private void handleBalanceInquiry(ACLMessage msg, String[] parts) {
            String agentId = parts[1];
            
            ACLMessage reply = msg.createReply();
            
            if (accounts.containsKey(agentId)) {
                BankAccount account = accounts.get(agentId);
                reply.setPerformative(ACLMessage.INFORM);
                reply.setContent("BALANCE;" + account.getBalance() + ";" + 
                               account.getAvailableBalance());
            } else {
                reply.setPerformative(ACLMessage.FAILURE);
                reply.setContent("ACCOUNT_NOT_FOUND");
            }
            
            send(reply);
        }
    }
    
    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (Exception e) {}
        System.out.println("🛑 BankAgent terminé");
    }
}
