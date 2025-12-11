package auction.agents;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import java.util.*;

public class AuthenticatorAgent extends Agent {
    private Map<String, AgentCredentials> credentials;
    private Set<String> blacklistedAgents;
    private Map<String, Integer> loginAttempts;
    
    class AgentCredentials {
        String agentId;
        String role;
        Date registrationDate;
        boolean verified;
        
        AgentCredentials(String id, String role) {
            this.agentId = id;
            this.role = role;
            this.registrationDate = new Date();
            this.verified = false;
        }
    }
    
    @Override
    protected void setup() {
        System.out.println("🔐 AuthenticatorAgent " + getLocalName() + " démarré!");
        
        credentials = new HashMap<>();
        blacklistedAgents = new HashSet<>();
        loginAttempts = new HashMap<>();
        
        registerToDF();
        
        addBehaviour(new AuthenticationBehaviour());
        addBehaviour(new SecurityMonitorBehaviour(this, 8000));
        
        System.out.println("✅ Système de sécurité actif");
    }
    
    private void registerToDF() {
        try {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setType("security-service");
            sd.setName("authentication");
            dfd.addServices(sd);
            DFService.register(this, dfd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    class AuthenticationBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = receive();
            
            if (msg != null) {
                String content = msg.getContent();
                String[] parts = content.split(";");
                String agentId = msg.getSender().getLocalName();
                
                if (parts[0].equals("REGISTER")) {
                    handleRegistration(msg, agentId, parts);
                } else if (parts[0].equals("VERIFY")) {
                    handleVerification(msg, agentId);
                } else if (parts[0].equals("CHECK_PERMISSION")) {
                    handlePermissionCheck(msg, agentId, parts);
                }
            } else {
                block();
            }
        }
        
        private void handleRegistration(ACLMessage msg, String agentId, String[] parts) {
            ACLMessage reply = msg.createReply();
            
            if (blacklistedAgents.contains(agentId)) {
                reply.setPerformative(ACLMessage.REFUSE);
                reply.setContent("BLACKLISTED");
                System.out.println("❌ Tentative d'enregistrement d'agent blacklisté: " + agentId);
            } else {
                String role = parts.length > 1 ? parts[1] : "BIDDER";
                credentials.put(agentId, new AgentCredentials(agentId, role));
                reply.setPerformative(ACLMessage.CONFIRM);
                reply.setContent("REGISTERED");
                System.out.println("✅ Agent enregistré: " + agentId + " (Role: " + role + ")");
            }
            
            send(reply);
        }
        
        private void handleVerification(ACLMessage msg, String agentId) {
            ACLMessage reply = msg.createReply();
            
            if (credentials.containsKey(agentId)) {
                AgentCredentials cred = credentials.get(agentId);
                cred.verified = true;
                reply.setPerformative(ACLMessage.CONFIRM);
                reply.setContent("VERIFIED");
                System.out.println("✅ Agent vérifié: " + agentId);
            } else {
                reply.setPerformative(ACLMessage.DISCONFIRM);
                reply.setContent("NOT_REGISTERED");
                
                loginAttempts.put(agentId, loginAttempts.getOrDefault(agentId, 0) + 1);
                
                if (loginAttempts.get(agentId) > 3) {
                    blacklistedAgents.add(agentId);
                    System.out.println("⚠️ Agent blacklisté après tentatives multiples: " + agentId);
                }
            }
            
            send(reply);
        }
        
        private void handlePermissionCheck(ACLMessage msg, String agentId, String[] parts) {
            ACLMessage reply = msg.createReply();
            String action = parts.length > 1 ? parts[1] : "UNKNOWN";
            
            if (credentials.containsKey(agentId) && credentials.get(agentId).verified) {
                reply.setPerformative(ACLMessage.CONFIRM);
                reply.setContent("AUTHORIZED");
            } else {
                reply.setPerformative(ACLMessage.REFUSE);
                reply.setContent("UNAUTHORIZED");
                System.out.println("⚠️ Tentative d'action non autorisée: " + agentId + " -> " + action);
            }
            
            send(reply);
        }
    }
    
    class SecurityMonitorBehaviour extends TickerBehaviour {
        public SecurityMonitorBehaviour(Agent a, long period) {
            super(a, period);
        }
        
        @Override
        protected void onTick() {
            System.out.println("\n🔒 RAPPORT DE SÉCURITÉ");
            System.out.println("  Agents enregistrés: " + credentials.size());
            System.out.println("  Agents vérifiés: " + 
                credentials.values().stream().filter(c -> c.verified).count());
            System.out.println("  Agents blacklistés: " + blacklistedAgents.size());
            
            if (!blacklistedAgents.isEmpty()) {
                System.out.println("  ⚠️ Liste noire: " + blacklistedAgents);
            }
        }
    }
    
    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (Exception e) {}
        System.out.println("🛑 AuthenticatorAgent terminé");
    }
}