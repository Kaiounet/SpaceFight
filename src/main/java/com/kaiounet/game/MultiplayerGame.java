package com.kaiounet.game;

import com.kaiounet.network.*;
import static com.raylib.Colors.*;
import static com.raylib.Raylib.*;
import com.raylib.Raylib.Color;
import java.util.*;

public class MultiplayerGame {
    private final GameClient client;
    private final int width = 1200;
    private final int height = 800;
    private final Map<Integer, Player> players = new HashMap<>();
    private final float moveSpeed = 5;
    private Player localPlayer;
    private int localPlayerId = -1;
    
    public MultiplayerGame(GameClient client) {
        this.client = client;
    }
    
    public void initialize() {
        InitWindow(width, height, "Multiplayer Game - Jaylib");
        SetTargetFPS(60);
    }
    
    public void run() {
        while (!WindowShouldClose() && client.isConnected()) {
            update();
            render();
        }
    }
    
    private void update() {
        // Process network messages FIRST
        processNetworkMessages();
        
        // Handle local input
        if (localPlayer != null) {
            float newX = localPlayer.x;
            float newY = localPlayer.y;
            
            if (IsKeyDown(KEY_LEFT) || IsKeyDown(KEY_A)) newX -= moveSpeed;
            if (IsKeyDown(KEY_RIGHT) || IsKeyDown(KEY_D)) newX += moveSpeed;
            if (IsKeyDown(KEY_UP) || IsKeyDown(KEY_W)) newY -= moveSpeed;
            if (IsKeyDown(KEY_DOWN) || IsKeyDown(KEY_S)) newY += moveSpeed;
            
            // Clamp to screen bounds
            newX = Math.max(0, Math.min(newX, width - Player.SIZE));
            newY = Math.max(0, Math.min(newY, height - Player.SIZE));
            
            // Send position update if moved
            if (newX != localPlayer.x || newY != localPlayer.y) {
                localPlayer.move(newX, newY);
                GameMessage msg = new GameMessage(
                    GameMessage.MessageType.PLAYER_MOVE,
                    localPlayerId,
                    newX,
                    newY,
                    localPlayer.color
                );
                System.out.println("P" + localPlayerId + " sending MOVE to (" + newX + "," + newY + ")");
                client.sendMessage(msg);
            }
        }
    }
    
    private void processNetworkMessages() {
        GameMessage message;
        while ((message = client.pollMessage()) != null) {
            System.out.println("[" + localPlayerId + "] Received message type=" + message.type + " playerId=" + message.playerId + " pos=(" + message.x + "," + message.y + ")");
            
            switch (message.type) {
                case PLAYER_JOIN:
                    if (localPlayerId == -1) {
                        localPlayerId = message.playerId;
                        localPlayer = new Player(message.playerId, message.x, message.y, message.color);
                        players.put(message.playerId, localPlayer);
                        System.out.println(">>> SETTING localPlayerId to: " + message.playerId);
                    } else {
                        Player newPlayer = new Player(message.playerId, message.x, message.y, message.color);
                        players.put(message.playerId, newPlayer);
                        System.out.println("Player joined: " + message.playerId + " at (" + message.x + "," + message.y + ")");
                    }
                    break;
                    
                case PLAYER_MOVE:
                    // Skip our own movement messages - we control our position locally
                    if (message.playerId != localPlayerId) {
                        Player player = players.get(message.playerId);
                        if (player != null) {
                            System.out.println("P" + localPlayerId + " received P" + message.playerId + " MOVE to (" + message.x + "," + message.y + ")");
                            player.move(message.x, message.y);
                        }
                    } else {
                        System.out.println("P" + localPlayerId + " ignoring own MOVE message");
                    }
                    break;
                    
                case PLAYER_LEAVE:
                    players.remove(message.playerId);
                    System.out.println("Player left: " + message.playerId);
                    break;
                    
                case STATE_UPDATE:
                default:
                    break;
            }
        }
    }
    
    private void render() {
        BeginDrawing();
        ClearBackground(DARKGRAY);
        
        // Draw all players
        for (Player player : players.values()) {
            Color rayColor = createColorFromInt(player.color);
            DrawRectangle(
                (int) player.x,
                (int) player.y,
                Player.SIZE,
                Player.SIZE,
                rayColor
            );
            
            // Draw player ID above the square
            DrawText(
                "P" + player.id,
                (int) player.x + 5,
                (int) player.y - 20,
                12,
                WHITE
            );
        }
        
        // Draw UI
        DrawText("Arrow Keys or WASD to Move", 10, 10, 14, WHITE);
        DrawText("Players: " + players.size(), 10, 30, 14, WHITE);
        if (localPlayerId >= 0) {
            DrawText("Your ID: " + localPlayerId, 10, 50, 14, YELLOW);
        }
        
        DrawFPS(width - 100, 10);
        EndDrawing();
    }
    
    private Color createColorFromInt(int colorInt) {
        // Map integer colors to predefined Raylib colors
        switch (colorInt) {
            case 0xFF0000FF: return RED;
            case 0x00FF00FF: return GREEN;
            case 0x0000FFFF: return BLUE;
            case 0xFFFF00FF: return YELLOW;
            case 0xFF00FFFF: return MAGENTA;
            case 0xFF8800FF: return ORANGE;
            default: return WHITE;
        }
    }
    
    public void close() {
        client.disconnect();
        CloseWindow();
    }
}
