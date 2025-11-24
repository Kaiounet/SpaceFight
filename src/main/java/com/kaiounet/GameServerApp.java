package com.kaiounet;

import com.kaiounet.network.GameServer;
import java.io.IOException;

public class GameServerApp {
    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 5555;
        
        try {
            GameServer server = new GameServer(port);
            server.start();
            System.out.println("Server started on port " + port);
            System.out.println("Waiting for clients...");
            
            // Keep server running
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nShutting down server...");
                server.stop();
            }));
            
            // Block main thread
            Thread.currentThread().join();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
