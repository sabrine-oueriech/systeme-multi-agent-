package auction.web;

public class WebInterface {

    private static AuctionWebSocketServer ws;

    public static void start(int port) {
        if (ws == null) {
            ws = new AuctionWebSocketServer(port);
            ws.start();
            System.out.println("WebSocket démarré sur le port " + port);
        }
    }

    public static void stop() {
        if (ws != null) {
            try { ws.stop(); }
            catch (Exception e) { e.printStackTrace(); }
        }
    }

    public static void notifyNewAuction(String id, String name, double start, double max) {
        if (ws != null) ws.broadcastNewAuction(id, name, start, max);
    }

    public static void notifyBidUpdate(String id, double price, String bidder) {
        if (ws != null) ws.broadcastBidUpdate(id, price, bidder);
    }

    public static void notifyAuctionEnd(String id, String winner, double finalPrice) {
        if (ws != null) ws.broadcastAuctionEnd(id, winner, finalPrice);
    }

    public static void notifyAgentUpdate(String name, String type, double budget, int bids) {
        if (ws != null) ws.broadcastAgentUpdate(name, type, budget, bids);
    }

    public static void log(String msg, String level) {
        if (ws != null) ws.broadcastLog(msg, level);
    }

    public static void updateStatistics(int active, int agents, double volume) {
        if (ws != null) ws.broadcastStatistics(active, agents, volume);
    }
}
