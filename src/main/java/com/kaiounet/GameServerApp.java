package com.kaiounet;

import com.kaiounet.config.Config;
import com.kaiounet.network.GameServer;
import java.io.IOException;
import java.net.InetAddress;

public class GameServerApp {
    public static void main(String[] args) {
        // Load configuration from .env file
        Config.load();
        
        String host = Config.getServerHost();
        int port = Config.getServerPort();
        
        try {
            GameServer server = new GameServer(host, port);
            server.start();
            
            // Get the actual server address
            String serverAddress = host.equals("0.0.0.0") ? 
                InetAddress.getLocalHost().getHostAddress() : host;
            
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║         MULTIPLAYER GAME SERVER        ║");
            System.out.println("╠════════════════════════════════════════╣");
            System.out.println("║ Server Address: " + String.format("%-22s", serverAddress) + " ║");
            System.out.println("║ Port: " + String.format("%-32d", port) + " ║");
            System.out.println("║ Listening: " + String.format("%-26s", host + ":" + port) + " ║");
            System.out.println("╠════════════════════════════════════════╣");
            System.out.println("║ Waiting for clients...                 ║");
            System.out.println("║ Press Ctrl+C to stop                   ║");
            System.out.println("╚════════════════════════════════════════╝\n");
            
            // Keep server running
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n╔════════════════════════════════════════╗");
                System.out.println("║     Shutting down server...            ║");
                System.out.println("╚════════════════════════════════════════╝");
                server.stop();
            }));
            
            // Block main thread
            Thread.currentThread().join();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
