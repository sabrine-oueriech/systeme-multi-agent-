
package auction.web;

import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class WebServer {
    private HttpServer server;
    private int port;
    private String webRoot;
    
    public WebServer(int port, String webRoot) {
        this.port = port;
        this.webRoot = webRoot;
    }
    
    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        
        // Routes
        server.createContext("/", new StaticFileHandler(webRoot));
        server.createContext("/api/auctions", new AuctionsHandler());
        server.createContext("/api/agents", new AgentsHandler());
        server.createContext("/api/logs", new LogsHandler());
        
        server.setExecutor(null);
        server.start();
        
        System.out.println("🌐 Serveur Web démarré sur http://localhost:" + port);
        System.out.println("📂 Dossier web: " + webRoot);
    }
    
    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("🛑 Serveur Web arrêté");
        }
    }
    

    
    static class StaticFileHandler implements HttpHandler {
        private String webRoot;
        
        public StaticFileHandler(String webRoot) {
            this.webRoot = webRoot;
        }
        
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            
            if (path.equals("/")) {
                path = "/index.html";
            }
            
            File file = new File(webRoot + path);
            
            if (file.exists() && !file.isDirectory()) {
                // Déterminer le type MIME
                String contentType = getContentType(path);
                
                byte[] content = Files.readAllBytes(file.toPath());
                
                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, content.length);
                
                OutputStream os = exchange.getResponseBody();
                os.write(content);
                os.close();
            } else {
                String response = "404 - Fichier non trouvé";
                exchange.sendResponseHeaders(404, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
        
        private String getContentType(String path) {
            if (path.endsWith(".html")) return "text/html; charset=utf-8";
            if (path.endsWith(".css")) return "text/css";
            if (path.endsWith(".js")) return "application/javascript";
            if (path.endsWith(".json")) return "application/json";
            if (path.endsWith(".png")) return "image/png";
            if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
            return "text/plain";
        }
    }
    

    
    static class AuctionsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // CORS headers
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            
            // Récupérer les données des enchères depuis JADE
            // TODO: Intégrer avec les agents JADE
            
            String jsonResponse = getAuctionsJSON();
            
            exchange.sendResponseHeaders(200, jsonResponse.length());
            OutputStream os = exchange.getResponseBody();
            os.write(jsonResponse.getBytes());
            os.close();
        }
        
        private String getAuctionsJSON() {
            // Exemple de données
            return "[{" +
                "\"id\": \"ITEM-1\"," +
                "\"name\": \"Article Premium 1\"," +
                "\"currentPrice\": 245.50," +
                "\"maxPrice\": 500," +
                "\"winner\": \"aggressive1\"," +
                "\"timeLeft\": 180" +
                "}]";
        }
    }
    
   
    
    static class AgentsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            
            String jsonResponse = getAgentsJSON();
            
            exchange.sendResponseHeaders(200, jsonResponse.length());
            OutputStream os = exchange.getResponseBody();
            os.write(jsonResponse.getBytes());
            os.close();
        }
        
        private String getAgentsJSON() {
            return "[{" +
                "\"name\": \"aggressive1\"," +
                "\"type\": \"aggressive\"," +
                "\"budget\": 5247.80," +
                "\"bids\": 23," +
                "\"activity\": \"Actif\"" +
                "}]";
        }
    }
    
   
    
    static class LogsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            
            String jsonResponse = "[]";
            
            exchange.sendResponseHeaders(200, jsonResponse.length());
            OutputStream os = exchange.getResponseBody();
            os.write(jsonResponse.getBytes());
            os.close();
        }
    }
}

