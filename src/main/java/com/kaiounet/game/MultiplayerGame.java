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
    private final Map<Integer, Beam> beams = new HashMap<>();
    private final float moveSpeed = 5;
    private Player localPlayer;
    private int localPlayerId = -1;
    private int nextBeamId = 1;
    private long lastFireTime = 0;
    private static final long FIRE_COOLDOWN = 200; // ms between shots
    
    public MultiplayerGame(GameClient client) {
        this.client = client;
    }
    
    public void initialize() {
        InitWindow(width, height, "Multiplayer Shooting Game - Jaylib");
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
            // Movement
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
                    localPlayer.color,
                    localPlayer.health,
                    localPlayer.score
                );
                client.sendMessage(msg);
            }
            
            // Shooting
            handleShooting();
        }
        
        // Update all beams
        List<Integer> beamsToRemove = new ArrayList<>();
        for (Beam beam : beams.values()) {
            beam.update();
            if (beam.isOutOfBounds(width, height)) {
                beamsToRemove.add(beam.id);
            }
        }
        beamsToRemove.forEach(beams::remove);
        
        // Check collisions with players
        checkBeamCollisions();
    }
    
    private void handleShooting() {
        if (IsKeyPressed(KEY_SPACE)) {
            long now = System.currentTimeMillis();
            if (now - lastFireTime > FIRE_COOLDOWN) {
                // Get direction to nearest enemy (simple approach: shoot in 4 directions)
                // For now, shoot in the direction of mouse or a fixed direction
                int mouseX = GetMouseX();
                int mouseY = GetMouseY();
                
                float dirX = mouseX - (localPlayer.x + Player.SIZE / 2);
                float dirY = mouseY - (localPlayer.y + Player.SIZE / 2);
                float len = (float) Math.sqrt(dirX * dirX + dirY * dirY);
                
                if (len > 0) {
                    dirX /= len;
                    dirY /= len;
                } else {
                    dirX = 1;
                    dirY = 0;
                }
                
                int beamId = nextBeamId++;
                Beam beam = new Beam(
                    beamId,
                    localPlayerId,
                    localPlayer.x + Player.SIZE / 2 - Beam.WIDTH / 2,
                    localPlayer.y + Player.SIZE / 2 - Beam.HEIGHT / 2,
                    dirX,
                    dirY,
                    localPlayer.color
                );
                
                beams.put(beamId, beam);
                lastFireTime = now;
                
                // Broadcast beam fire to other players
                GameMessage msg = new GameMessage(GameMessage.MessageType.BEAM_FIRE, localPlayerId, 
                    beam.x, beam.y, beam.color);
                msg.beamId = beamId;
                msg.vx = dirX;
                msg.vy = dirY;
                client.sendMessage(msg);
            }
        }
    }
    
    private void checkBeamCollisions() {
        List<Integer> beamsToRemove = new ArrayList<>();
        
        for (Beam beam : new ArrayList<>(beams.values())) {
            if (!beam.isActive()) continue;
            
            for (Player player : players.values()) {
                if (player.id == beam.shooterId || !player.isAlive()) {
                    if (player.id != beam.shooterId && !player.isAlive()) {
                        System.out.println("COLLISION CHECK: Beam from P" + beam.shooterId + " skipping P" + player.id + " (health=" + player.health + ", alive=" + player.isAlive() + ")");
                    }
                    continue;
                }
                
                // Simple AABB collision
                if (beam.x < player.x + Player.SIZE &&
                    beam.x + Beam.WIDTH > player.x &&
                    beam.y < player.y + Player.SIZE &&
                    beam.y + Beam.HEIGHT > player.y) {
                    
                    // Hit!
                    System.out.println("COLLISION: Beam hit P" + player.id + " (health before=" + player.health + ")");
                    player.takeDamage(Beam.DAMAGE);
                    System.out.println("COLLISION: P" + player.id + " health after damage=" + player.health);
                    beam.deactivate();
                    beamsToRemove.add(beam.id);
                    
                    Player shooter = players.get(beam.shooterId);
                    
                    // If player died, credit shooter BEFORE sending messages
                    if (player.health == 0) {
                        System.out.println("DEATH: Player " + player.id + " died! Health was " + player.health);
                        if (shooter != null) {
                            shooter.addScore(1);
                            System.out.println("DEATH: Shooter " + shooter.id + " score now: " + shooter.score);
                        }
                    }
                    
                    // Send hit message (always broadcast)
                    GameMessage hitMsg = new GameMessage(GameMessage.MessageType.PLAYER_HIT,
                        beam.shooterId, player.x, player.y, beam.color);
                    hitMsg.targetPlayerId = player.id;
                    hitMsg.damage = Beam.DAMAGE;
                    hitMsg.health = player.health;
                    hitMsg.score = (shooter != null) ? shooter.score : 0;
                    client.sendMessage(hitMsg);
                    
                    // If player died, respawn and send respawn message
                    if (player.health == 0) {
                        // Reset player health IMMEDIATELY so they're alive again this frame
                        player.health = Player.MAX_HEALTH;
                        System.out.println("DEATH: Player " + player.id + " respawned with health " + player.health + " (isAlive=" + player.isAlive() + ")");
                        
                        // Send respawn message with killer info
                        GameMessage respawnMsg = new GameMessage(GameMessage.MessageType.PLAYER_RESPAWN,
                            player.id, player.x, player.y, player.color, Player.MAX_HEALTH, 0);
                        respawnMsg.killerId = beam.shooterId;
                        respawnMsg.score = (shooter != null) ? shooter.score : 0;
                        System.out.println("DEATH: Sending respawn message for player " + player.id + " killed by " + beam.shooterId);
                        client.sendMessage(respawnMsg);
                    }
                }
            }
        }
        
        beamsToRemove.forEach(beams::remove);
    }
    
    private void processNetworkMessages() {
        GameMessage message;
        while ((message = client.pollMessage()) != null) {
            switch (message.type) {
                case PLAYER_JOIN:
                    if (localPlayerId == -1) {
                        localPlayerId = message.playerId;
                        localPlayer = new Player(message.playerId, message.x, message.y, message.color);
                        localPlayer.health = message.health;
                        localPlayer.score = message.score;
                        players.put(message.playerId, localPlayer);
                    } else {
                        Player newPlayer = new Player(message.playerId, message.x, message.y, message.color);
                        newPlayer.health = message.health;
                        newPlayer.score = message.score;
                        players.put(message.playerId, newPlayer);
                    }
                    break;
                    
                case PLAYER_MOVE:
                    if (message.playerId != localPlayerId) {
                        Player player = players.get(message.playerId);
                        if (player != null) {
                            player.move(message.x, message.y);
                            // Don't update health from PLAYER_MOVE - only from PLAYER_HIT and PLAYER_RESPAWN
                            // This prevents late-arriving PLAYER_MOVE messages from resetting health after respawn
                        }
                    }
                    break;
                    
                case PLAYER_LEAVE:
                    players.remove(message.playerId);
                    break;
                
                case BEAM_FIRE:
                    if (message.playerId != localPlayerId) {
                        Beam beam = new Beam(message.beamId, message.playerId, 
                            message.x, message.y, message.vx, message.vy, message.color);
                        beams.put(message.beamId, beam);
                    }
                    break;
                
                case PLAYER_HIT:
                    Player targetPlayer = players.get(message.targetPlayerId);
                    if (targetPlayer != null) {
                        targetPlayer.health = message.health;
                        System.out.println("HIT: Player " + message.targetPlayerId + " health now " + targetPlayer.health);
                    } else {
                        System.out.println("HIT: Target player " + message.targetPlayerId + " not found!");
                    }
                    // Don't update score on HIT - will update on RESPAWN if death
                    break;
                
                case PLAYER_RESPAWN:
                    Player respawnPlayer = players.get(message.playerId);
                    if (respawnPlayer != null) {
                        System.out.println("RESPAWN HANDLER: P" + message.playerId + " health before=" + respawnPlayer.health + " isAlive=" + respawnPlayer.isAlive());
                        respawnPlayer.health = Player.MAX_HEALTH;
                        System.out.println("RESPAWN HANDLER: P" + message.playerId + " health after=" + respawnPlayer.health + " isAlive=" + respawnPlayer.isAlive());
                    } else {
                        System.out.println("RESPAWN HANDLER: Player " + message.playerId + " NOT FOUND in players map! (map keys: " + players.keySet() + ")");
                    }
                    // Update killer's score
                    if (message.killerId > 0) {
                        Player killer = players.get(message.killerId);
                        if (killer != null) {
                            killer.score = message.score;
                            System.out.println("RESPAWN HANDLER: Killer " + message.killerId + " score now " + killer.score);
                        }
                    }
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
        
        // Draw all beams
        for (Beam beam : beams.values()) {
            if (beam.isActive()) {
                Color rayColor = createColorFromInt(beam.color);
                DrawRectangle(
                    (int) beam.x,
                    (int) beam.y,
                    Beam.WIDTH,
                    Beam.HEIGHT,
                    rayColor
                );
            }
        }
        
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
            
            // Draw health bar above player
            int healthBarWidth = 30;
            DrawRectangleLines((int) player.x - 5, (int) player.y - 25, healthBarWidth, 8, WHITE);
            int healthFill = (int) (healthBarWidth * player.health / Player.MAX_HEALTH);
            Color healthColor = player.health > 50 ? GREEN : (player.health > 25 ? YELLOW : RED);
            DrawRectangle((int) player.x - 5, (int) player.y - 25, healthFill, 8, healthColor);
            
            // Draw player ID
            DrawText(
                "P" + player.id,
                (int) player.x + 5,
                (int) player.y - 40,
                12,
                WHITE
            );
        }
        
        // Draw scoreboard
        drawScoreboard();
        
        // Draw UI
        DrawText("Arrow Keys/WASD: Move | SPACE: Shoot | Mouse: Aim", 10, 10, 14, WHITE);
        DrawText("Players: " + players.size(), 10, 35, 14, WHITE);
        
        if (localPlayerId >= 0 && localPlayer != null) {
            DrawText("Your ID: " + localPlayerId, 10, 60, 14, YELLOW);
            DrawText("Health: " + localPlayer.health + "/" + Player.MAX_HEALTH, 10, 85, 14, 
                localPlayer.health > 50 ? GREEN : (localPlayer.health > 25 ? YELLOW : RED));
        }
        
        DrawFPS(width - 100, 10);
        EndDrawing();
    }
    
    private void drawScoreboard() {
        int scoreboardX = width - 250;
        int scoreboardY = 50;
        int entryHeight = 25;
        
        DrawRectangle(scoreboardX - 10, scoreboardY - 25, 240, 20 + players.size() * entryHeight, 
            Fade(BLACK, 0.5f));
        DrawText("SCOREBOARD", scoreboardX, scoreboardY - 20, 16, YELLOW);
        
        // Sort players by score (descending)
        List<Player> sortedPlayers = new ArrayList<>(players.values());
        sortedPlayers.sort((a, b) -> Integer.compare(b.score, a.score));
        
        int yOffset = 0;
        for (Player player : sortedPlayers) {
            String text = String.format("P%d: %3d kills", player.id, player.score);
            Color textColor = (player.id == localPlayerId) ? YELLOW : WHITE;
            DrawText(text, scoreboardX, scoreboardY + yOffset, 14, textColor);
            yOffset += entryHeight;
        }
    }
    
    private Color createColorFromInt(int colorInt) {
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
