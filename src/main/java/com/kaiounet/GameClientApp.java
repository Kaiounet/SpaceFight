package com.kaiounet;

import com.kaiounet.game.MultiplayerGame;
import com.kaiounet.network.GameClient;
import java.util.Scanner;

public class GameClientApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║      MULTIPLAYER GAME CLIENT           ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        // Prompt user for server address
        System.out.print("Enter server address (default: localhost): ");
        String host = scanner.nextLine().trim();
        if (host.isEmpty()) {
            host = "localhost";
        }
        
        // Prompt user for server port
        System.out.print("Enter server port (default: 5555): ");
        int port = 5555;
        try {
            String portInput = scanner.nextLine().trim();
            if (!portInput.isEmpty()) {
                port = Integer.parseInt(portInput);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid port, using default: 5555");
        }
        
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║ Connecting to: " + String.format("%-22s", host) + " ║");
        System.out.println("║ Port: " + String.format("%-32d", port) + " ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        GameClient client = new GameClient(host, port);
        
        if (!client.connect()) {
            System.err.println("✗ Failed to connect to server at " + host + ":" + port);
            System.exit(1);
        }
        
        System.out.println("✓ Connected to server!");
        System.out.println("Starting game...\n");
        
        MultiplayerGame game = new MultiplayerGame(client);
        game.initialize();
        game.run();
        game.close();
        
        scanner.close();
    }
}
