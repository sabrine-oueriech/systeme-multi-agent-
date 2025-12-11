package auction.web;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import javax.json.*;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AuctionWebSocketServer extends WebSocketServer {

    private static AuctionWebSocketServer instance;
    private Set<WebSocket> clients;

    public AuctionWebSocketServer(int port) {
        super(new InetSocketAddress(port));
        clients = ConcurrentHashMap.newKeySet();
        instance = this;
    }

    public static AuctionWebSocketServer getInstance() {
        return instance;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        clients.add(conn);
        sendInitialState(conn);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        clients.remove(conn);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {}

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("WebSocket Server port = " + getPort());
        setConnectionLostTimeout(60);
    }

    // ------------------------------------------------
    // BROADCAST GENERIC
    // ------------------------------------------------
    public void broadcast(String msg) {
        for (WebSocket client : clients) {
            try { client.send(msg); }
            catch(Exception e){ e.printStackTrace(); }
        }
    }

    // ------------------------------------------------
    //   WEB INTERFACE : NOTIFICATIONS
    // ------------------------------------------------

    public void broadcastNewAuction(String id, String name, double start, double max) {
        JsonObject json = Json.createObjectBuilder()
            .add("type", "NEW_AUCTION")
            .add("data", Json.createObjectBuilder()
                .add("id", id)
                .add("name", name)
                .add("currentPrice", start)
                .add("maxPrice", max)
                .add("winner", "")
                .add("timeLeft", 300)
            ).build();
        broadcast(json.toString());
    }

    public void broadcastBidUpdate(String id, double price, String bidder) {
        JsonObject json = Json.createObjectBuilder()
            .add("type", "BID_UPDATE")
            .add("data", Json.createObjectBuilder()
                .add("id", id)
                .add("currentPrice", price)
                .add("winner", bidder)
            ).build();
        broadcast(json.toString());
    }

    public void broadcastAuctionEnd(String id, String winner, double finalPrice) {
        JsonObject json = Json.createObjectBuilder()
            .add("type", "AUCTION_END")
            .add("data", Json.createObjectBuilder()
                .add("id", id)
                .add("winner", winner)
                .add("finalPrice", finalPrice)
            ).build();
        broadcast(json.toString());
    }

    public void broadcastAgentUpdate(String name, String type, double budget, int bids) {
        JsonObject json = Json.createObjectBuilder()
            .add("type", "AGENT_UPDATE")
            .add("data", Json.createObjectBuilder()
                .add("name", name)
                .add("type", type)
                .add("budget", budget)
                .add("bids", bids)
                .add("activity", "Actif")
            ).build();
        broadcast(json.toString());
    }

    public void broadcastLog(String message, String level) {
        JsonObject json = Json.createObjectBuilder()
            .add("type", "LOG")
            .add("data", Json.createObjectBuilder()
                .add("message", message)
                .add("level", level)
                .add("ts", System.currentTimeMillis())
            ).build();
        broadcast(json.toString());
    }

    public void broadcastStatistics(int activeAuctions, int activeAgents, double totalVolume) {
        JsonObject json = Json.createObjectBuilder()
            .add("type", "STATS_UPDATE")
            .add("data", Json.createObjectBuilder()
                .add("activeAuctions", activeAuctions)
                .add("activeAgents", activeAgents)
                .add("totalVolume", totalVolume)
            ).build();
        broadcast(json.toString());
    }

    // ------------------------------------------------
    // INITIAL STATE sent to new connections
    // ------------------------------------------------
    private void sendInitialState(WebSocket conn) {
        JsonObject json = Json.createObjectBuilder()
            .add("type", "INITIAL_STATE")
            .add("data", Json.createObjectBuilder()
                .add("message", "Connexion OK")
            ).build();
        conn.send(json.toString());
    }
}
