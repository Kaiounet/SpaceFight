package com.kaiounet.game;

public class Player {
    public int id;
    public float x;
    public float y;
    public int color;
    public static final int SIZE = 30;
    
    public Player(int id, float x, float y, int color) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.color = color;
    }
    
    public void move(float newX, float newY) {
        this.x = newX;
        this.y = newY;
    }
    
    @Override
    public String toString() {
        return String.format("Player{id=%d, pos=(%.1f,%.1f)}", id, x, y);
    }
}
