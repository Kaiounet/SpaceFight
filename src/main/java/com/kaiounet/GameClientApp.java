package com.kaiounet;

import com.kaiounet.game.MultiplayerGame;
import com.kaiounet.network.GameClient;

public class GameClientApp {
    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 5555;
        
        GameClient client = new GameClient(host, port);
        
        if (!client.connect()) {
            System.err.println("Failed to connect to server");
            System.exit(1);
        }
        
        MultiplayerGame game = new MultiplayerGame(client);
        game.initialize();
        game.run();
        game.close();
    }
}
