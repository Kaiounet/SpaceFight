package com.kaiounet.ui;

import com.kaiounet.game.Player;
import java.util.ArrayList;
import java.util.List;

import static com.raylib.Colors.*;
import static com.raylib.Raylib.*;
import com.raylib.Raylib.Color;

public class UIRenderer {
    
    // Feedback system
    private List<DamageNumber> damageNumbers = new ArrayList<>();
    private List<KillMessage> killMessages = new ArrayList<>();
    private static final int MAX_DAMAGE_NUMBERS = 100;
    private static final int MAX_KILL_MESSAGES = 50;
    
    /**
     * Draw improved health bar for a player
     */
    public void drawHealthBar(Player player) {
        float barWidth = 50;
        float barHeight = 8;
        float x = player.x - barWidth / 2;
        float y = player.y - 35;
        
        // Background (dark border)
        DrawRectangle((int) x - 2, (int) y - 2, (int) barWidth + 4, (int) barHeight + 4, 
            BLACK);
        
        // Health bar background (dark gray)
        DrawRectangle((int) x, (int) y, (int) barWidth, (int) barHeight, 
            Fade(BLACK, 0.7f));
        
        // Health fill (gradient color based on health)
        int healthFill = (int) (barWidth * player.health / Player.MAX_HEALTH);
        Color healthColor = getHealthColor(player.health);
        DrawRectangle((int) x, (int) y, healthFill, (int) barHeight, healthColor);
        
        // Health border
        DrawRectangleLines((int) x, (int) y, (int) barWidth, (int) barHeight, WHITE);
        
        // Health text (inside bar)
        String healthText = (int) player.health + "/" + Player.MAX_HEALTH;
        int textWidth = MeasureText(healthText, 10);
        DrawText(healthText, 
            (int) (x + barWidth / 2 - textWidth / 2), 
            (int) y - 1, 
            10, 
            WHITE);
    }
    
    /**
     * Draw improved scoreboard
     */
    public void drawScoreboard(java.util.Map<Integer, Player> players, int localPlayerId, 
                               int screenWidth, int screenHeight) {
        int scoreboardX = screenWidth - 280;
        int scoreboardY = 50;
        int entryHeight = 28;
        int padding = 15;
        
        // Sort players by score (descending)
        List<Player> sortedPlayers = new ArrayList<>(players.values());
        sortedPlayers.sort((a, b) -> Integer.compare(b.score, a.score));
        
        int boardHeight = 40 + sortedPlayers.size() * entryHeight;
        
        // Background with border
        DrawRectangle(scoreboardX - padding, scoreboardY - 30, 250 + padding * 2, boardHeight + 20,
            Fade(BLACK, 0.7f));
        DrawRectangleLines(scoreboardX - padding, scoreboardY - 30, 250 + padding * 2, boardHeight + 20,
            YELLOW);
        
        // Title
        DrawText("SCOREBOARD", scoreboardX + 20, scoreboardY - 20, 18, YELLOW);
        DrawLine(scoreboardX - padding + 5, scoreboardY - 5, scoreboardX + 250, scoreboardY - 5, YELLOW);
        
        // Draw entries
        int rank = 1;
        for (Player player : sortedPlayers) {
            int yPos = scoreboardY + (rank - 1) * entryHeight;
            
            // Highlight local player
            if (player.id == localPlayerId) {
                DrawRectangle(scoreboardX - padding + 5, yPos - 2, 240, entryHeight - 2,
                    Fade(YELLOW, 0.2f));
            }
            
            // Rank number
            DrawText(String.valueOf(rank) + ".", scoreboardX, yPos, 14, WHITE);
            
            // Player info: ID, Health, Kills
            String playerInfo = String.format("P%-2d | HP:%3d | %d kills",
                player.id, (int)player.health, player.score);
            Color textColor = (player.id == localPlayerId) ? YELLOW : WHITE;
            DrawText(playerInfo, scoreboardX + 25, yPos, 14, textColor);
            
            rank++;
        }
    }
    
    /**
     * Draw local player HUD (bottom left)
     */
    public void drawPlayerHUD(Player localPlayer, int localPlayerId, int screenHeight) {
        int hudX = 15;
        int hudY = screenHeight - 120;
        int panelWidth = 280;
        int panelHeight = 110;
        
        // Background panel
        DrawRectangle(hudX - 5, hudY - 5, panelWidth, panelHeight,
            Fade(BLACK, 0.7f));
        DrawRectangleLines(hudX - 5, hudY - 5, panelWidth, panelHeight, SKYBLUE);
        
        // Title
        DrawText("YOUR STATUS", hudX, hudY, 14, SKYBLUE);
        DrawLine(hudX, hudY + 18, hudX + 150, hudY + 18, SKYBLUE);
        
        // Player ID
        DrawText("ID: P" + localPlayerId, hudX + 10, hudY + 25, 13, WHITE);
        
        // Health section
        DrawText("Health:", hudX + 10, hudY + 45, 13, WHITE);
        
        // Large health bar
        int healthBarX = hudX + 10;
        int healthBarY = hudY + 60;
        int healthBarWidth = 200;
        int healthBarHeight = 18;
        
        // Background
        DrawRectangle(healthBarX, healthBarY, healthBarWidth, healthBarHeight,
            Fade(BLACK, 0.7f));
        
        // Health fill
        int healthFill = (int) (healthBarWidth * localPlayer.health / Player.MAX_HEALTH);
        Color healthColor = getHealthColor(localPlayer.health);
        DrawRectangle(healthBarX, healthBarY, healthFill, healthBarHeight, healthColor);
        
        // Border
        DrawRectangleLines(healthBarX, healthBarY, healthBarWidth, healthBarHeight, WHITE);
        
        // Health text
        String healthText = String.format("%d / %d", (int)localPlayer.health, Player.MAX_HEALTH);
        int textWidth = MeasureText(healthText, 12);
        DrawText(healthText, healthBarX + healthBarWidth / 2 - textWidth / 2, healthBarY + 2, 12, WHITE);
    }
    
    /**
     * Draw information panel (top left)
     */
    public void drawInfoPanel(int playerCount) {
        int infoX = 15;
        int infoY = 15;
        int panelWidth = 380;
        int panelHeight = 65;
        
        // Background panel
        DrawRectangle(infoX - 5, infoY - 5, panelWidth, panelHeight,
            Fade(BLACK, 0.7f));
        DrawRectangleLines(infoX - 5, infoY - 5, panelWidth, panelHeight, GREEN);
        
        // Title
        DrawText("GAME INFO", infoX, infoY, 14, GREEN);
        DrawLine(infoX, infoY + 18, infoX + 120, infoY + 18, GREEN);
        
        // Info text
        DrawText("Controls: WASD/Arrows=Move | SPACE=Shoot | Mouse=Aim", 
            infoX + 10, infoY + 25, 12, WHITE);
        DrawText("Players Online: " + playerCount, 
            infoX + 10, infoY + 45, 12, YELLOW);
    }
    
    /**
     * Add a damage number popup
     */
    public void addDamageNumber(float x, float y, int damage) {
        if (damageNumbers.size() < MAX_DAMAGE_NUMBERS) {
            damageNumbers.add(new DamageNumber(x, y, damage));
        }
    }
    
    /**
     * Add a kill message
     */
    public void addKillMessage(String killerName, String victimName, int killCount) {
        if (killMessages.size() < MAX_KILL_MESSAGES) {
            killMessages.add(new KillMessage(killerName, victimName, killCount));
        }
    }
    
    /**
     * Update and draw floating damage numbers
     */
    public void updateAndDrawDamageNumbers() {
        for (int i = damageNumbers.size() - 1; i >= 0; i--) {
            DamageNumber dn = damageNumbers.get(i);
            dn.update();
            
            if (dn.isAlive()) {
                dn.draw();
            } else {
                damageNumbers.remove(i);
            }
        }
    }
    
    /**
     * Update and draw kill messages
     */
    public void updateAndDrawKillMessages(int screenHeight) {
        for (int i = killMessages.size() - 1; i >= 0; i--) {
            KillMessage km = killMessages.get(i);
            km.update();
            
            if (km.isAlive()) {
                km.draw(screenHeight - 20 - i * 25);
            } else {
                killMessages.remove(i);
            }
        }
    }
    
    /**
     * Get color based on health percentage
     */
    private Color getHealthColor(float health) {
        float healthPercent = health / Player.MAX_HEALTH;
        
        if (healthPercent > 0.66f) {
            return GREEN;
        } else if (healthPercent > 0.33f) {
            return YELLOW;
        } else {
            return RED;
        }
    }
    
    /**
     * Damage number feedback (floats up and fades out)
     */
    private static class DamageNumber {
        private float x, y;
        private int damage;
        private float lifeTime = 1.5f;
        private float maxLifeTime = 1.5f;
        
        DamageNumber(float x, float y, int damage) {
            this.x = x;
            this.y = y;
            this.damage = damage;
        }
        
        void update() {
            y -= 0.8f; // Move up
            lifeTime -= GetFrameTime();
        }
        
        void draw() {
            float alpha = lifeTime / maxLifeTime;
            int fontSize = 20;
            String text = "-" + damage;
            
            // Damage number fades out
            Color color = Fade(ORANGE, alpha);
            
            DrawText(text, (int) x, (int) y, fontSize, color);
        }
        
        boolean isAlive() {
            return lifeTime > 0;
        }
    }
    
    /**
     * Kill message feedback (displayed on screen)
     */
    private static class KillMessage {
        private String killerName;
        private String victimName;
        private int killCount;
        private float lifeTime = 3.0f;
        private float maxLifeTime = 3.0f;
        
        KillMessage(String killerName, String victimName, int killCount) {
            this.killerName = killerName;
            this.victimName = victimName;
            this.killCount = killCount;
        }
        
        void update() {
            lifeTime -= GetFrameTime();
        }
        
        void draw(int yPos) {
            float alpha = lifeTime / maxLifeTime;
            
            // Fade out effect
            Color killerColor = Fade(YELLOW, alpha);
            Color victimColor = Fade(RED, alpha);
            Color separatorColor = Fade(WHITE, alpha);
            
            // Draw with formatting
            int x = 20;
            DrawText(killerName, x, yPos, 14, killerColor);
            
            int killerWidth = MeasureText(killerName, 14);
            DrawText(" killed ", x + killerWidth, yPos, 14, separatorColor);
            
            int separatorWidth = MeasureText(" killed ", 14);
            DrawText(victimName, x + killerWidth + separatorWidth, yPos, 14, victimColor);
        }
        
        boolean isAlive() {
            return lifeTime > 0;
        }
    }
}
