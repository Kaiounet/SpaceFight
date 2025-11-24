package com.kaiounet.network;

import java.io.Serializable;

public class GameMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum MessageType {
        PLAYER_JOIN,
        PLAYER_MOVE,
        PLAYER_LEAVE,
        STATE_UPDATE,
        BEAM_FIRE,
        PLAYER_HIT,
        PLAYER_RESPAWN
    }
    
    public MessageType type;
    public int playerId;
    public float x;
    public float y;
    public int color;
    public int health;
    public int score;
    // Beam specific fields
    public int beamId;
    public float vx;
    public float vy;
    // Hit/damage fields
    public int targetPlayerId;
    public int killerId;
    public int damage;
    
    public GameMessage(MessageType type, int playerId, float x, float y, int color) {
        this.type = type;
        this.playerId = playerId;
        this.x = x;
        this.y = y;
        this.color = color;
        this.health = 100;
        this.score = 0;
    }
    
    public GameMessage(MessageType type, int playerId, float x, float y, int color, int health, int score) {
        this.type = type;
        this.playerId = playerId;
        this.x = x;
        this.y = y;
        this.color = color;
        this.health = health;
        this.score = score;
    }
    
    @Override
    public String toString() {
        return String.format("GameMessage{type=%s, playerId=%d, pos=(%.1f,%.1f)}", 
            type, playerId, x, y);
    }
}

