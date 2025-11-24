package com.kaiounet.game;

public class Beam {
    public int id;
    public int shooterId;
    public float x;
    public float y;
    public float vx;
    public float vy;
    public int color;
    public static final int WIDTH = 10;
    public static final int HEIGHT = 10;
    public static final float SPEED = 8;
    public static final int DAMAGE = 10;
    private boolean active = true;
    
    public Beam(int id, int shooterId, float x, float y, float vx, float vy, int color) {
        this.id = id;
        this.shooterId = shooterId;
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.color = color;
    }
    
    public void update() {
        x += vx * SPEED;
        y += vy * SPEED;
    }
    
    public boolean isOutOfBounds(int width, int height) {
        return x < 0 || x > width || y < 0 || y > height;
    }
    
    public void deactivate() {
        active = false;
    }
    
    public boolean isActive() {
        return active;
    }
}
