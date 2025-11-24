package com.kaiounet.network;

import java.io.Serializable;

public class GameMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum MessageType {
        PLAYER_JOIN,
        PLAYER_MOVE,
        PLAYER_LEAVE,
        STATE_UPDATE
    }
    
    public MessageType type;
    public int playerId;
    public float x;
    public float y;
    public int color;
    
    public GameMessage(MessageType type, int playerId, float x, float y, int color) {
        this.type = type;
        this.playerId = playerId;
        this.x = x;
        this.y = y;
        this.color = color;
    }
    
    @Override
    public String toString() {
        return String.format("GameMessage{type=%s, playerId=%d, pos=(%.1f,%.1f)}", 
            type, playerId, x, y);
    }
}
