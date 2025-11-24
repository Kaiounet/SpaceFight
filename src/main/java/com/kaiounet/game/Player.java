package com.kaiounet.game;

public class Player {
    public int id;
    public float x;
    public float y;
    public int color;
    public int health;
    public int score;
    public static final int SIZE = 30;
    public static final int MAX_HEALTH = 100;
    
    public Player(int id, float x, float y, int color) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.color = color;
        this.health = MAX_HEALTH;
        this.score = 0;
    }
    
    public void move(float newX, float newY) {
        this.x = newX;
        this.y = newY;
    }
    
    public void takeDamage(int damage) {
        this.health = Math.max(0, this.health - damage);
    }
    
    public void heal() {
        this.health = MAX_HEALTH;
    }
    
    public void addScore(int points) {
        this.score += points;
    }
    
    public boolean isAlive() {
        return health > 0;
    }
    
    @Override
    public String toString() {
        return String.format("Player{id=%d, pos=(%.1f,%.1f), health=%d, score=%d}", id, x, y, health, score);
    }
}
