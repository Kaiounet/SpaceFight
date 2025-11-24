package com.kaiounet.network;

import com.kaiounet.game.Player;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class GameServer {
    private final int port;
    private ServerSocket serverSocket;
    private final Map<Integer, ClientHandler> clients = new ConcurrentHashMap<>();
    private final Map<Integer, PlayerState> playerStates = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private int nextPlayerId = 1;
    private volatile boolean running = true;
    
    private static class PlayerState {
        int health;
        int score;
        float x;
        float y;
        
        PlayerState(float x, float y, int health, int score) {
            this.x = x;
            this.y = y;
            this.health = health;
            this.score = score;
        }
    }
    
    public GameServer(int port) {
        this.port = port;
    }
    
    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server started on port " + port);
        
        executor.execute(() -> {
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    int playerId = nextPlayerId++;
                    System.out.println("New client connected with ID: " + playerId);
                    
                    ClientHandler handler = new ClientHandler(playerId, clientSocket, this);
                    clients.put(playerId, handler);
                    executor.execute(handler);
                    
                } catch (IOException e) {
                    if (running) e.printStackTrace();
                }
            }
        });
    }
    
    public void broadcastMessage(GameMessage message) {
        // Track player state for new joiners
        if (message.type == GameMessage.MessageType.PLAYER_MOVE) {
            playerStates.put(message.playerId, new PlayerState(message.x, message.y, message.health, message.score));
        }
        
        for (ClientHandler handler : clients.values()) {
            handler.sendMessage(message);
        }
    }
    
    public void broadcastMessageExcept(GameMessage message, int excludePlayerId) {
        // Track player state for new joiners
        if (message.type == GameMessage.MessageType.PLAYER_MOVE) {
            playerStates.put(message.playerId, new PlayerState(message.x, message.y, message.health, message.score));
        }
        
        for (ClientHandler handler : clients.values()) {
            if (handler.playerId != excludePlayerId) {
                handler.sendMessage(message);
            }
        }
    }
    
    public void sendMessageToClient(int playerId, GameMessage message) {
        ClientHandler handler = clients.get(playerId);
        if (handler != null) {
            handler.sendMessage(message);
        }
    }
    
    public Map<Integer, ClientHandler> getClients() {
        return clients;
    }
    
    public void removeClient(int playerId) {
        clients.remove(playerId);
        playerStates.remove(playerId);
        broadcastMessage(new GameMessage(
            GameMessage.MessageType.PLAYER_LEAVE,
            playerId, 0, 0, 0
        ));
        System.out.println("Client disconnected: " + playerId);
    }
    
    public int getPlayerColor(int playerId) {
        int[] colors = {0xFF0000FF, 0x00FF00FF, 0x0000FFFF, 0xFFFF00FF, 0xFF00FFFF, 0x00FFFFFF};
        return colors[playerId % colors.length];
    }
    
    public void stop() {
        running = false;
        executor.shutdown();
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Inner class for handling individual clients
    private static class ClientHandler implements Runnable {
        private final int playerId;
        private final Socket socket;
        private final GameServer server;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        
        public ClientHandler(int playerId, Socket socket, GameServer server) {
            this.playerId = playerId;
            this.socket = socket;
            this.server = server;
        }
        
        @Override
        public void run() {
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
                in = new ObjectInputStream(socket.getInputStream());
                
                float startX = 100 + (playerId * 50);
                float startY = 100 + (playerId * 50);
                server.playerStates.put(playerId, new PlayerState(startX, startY, 100, 0));
                
                // Send this player its own ID
                sendMessage(new GameMessage(
                    GameMessage.MessageType.PLAYER_JOIN,
                    playerId,
                    startX,
                    startY,
                    server.getPlayerColor(playerId),
                    100,
                    0
                ));
                
                // Send all OTHER existing players to this new player
                for (ClientHandler existingHandler : server.getClients().values()) {
                    if (existingHandler.playerId != this.playerId) {
                        PlayerState state = server.playerStates.get(existingHandler.playerId);
                        float x = (state != null) ? state.x : (100 + existingHandler.playerId * 50);
                        float y = (state != null) ? state.y : (100 + existingHandler.playerId * 50);
                        int health = (state != null) ? state.health : 100;
                        int score = (state != null) ? state.score : 0;
                        
                        sendMessage(new GameMessage(
                            GameMessage.MessageType.PLAYER_JOIN,
                            existingHandler.playerId,
                            x,
                            y,
                            server.getPlayerColor(existingHandler.playerId),
                            health,
                            score
                        ));
                    }
                }
                
                // Broadcast to others that this player joined
                server.broadcastMessageExcept(new GameMessage(
                    GameMessage.MessageType.PLAYER_JOIN,
                    playerId,
                    startX,
                    startY,
                    server.getPlayerColor(playerId),
                    100,
                    0
                ), playerId);
                
                while (true) {
                    try {
                        GameMessage message = (GameMessage) in.readObject();
                        
                        switch (message.type) {
                            case PLAYER_MOVE:
                                message.playerId = playerId;
                                server.broadcastMessage(message);
                                break;
                            
                            case BEAM_FIRE:
                                message.playerId = playerId;
                                server.broadcastMessageExcept(message, playerId);
                                break;
                            
                            case PLAYER_HIT:
                                message.playerId = playerId;
                                server.broadcastMessage(message);
                                break;
                            
                            case PLAYER_RESPAWN:
                                // IMPORTANT: Don't overwrite playerId for PLAYER_RESPAWN!
                                // playerId contains the ID of the respawned player
                                PlayerState state = server.playerStates.get(message.playerId);
                                if (state != null) {
                                    state.health = 100;
                                }
                                server.broadcastMessage(message);
                                break;
                            
                            default:
                                break;
                        }
                    } catch (EOFException e) {
                        break;
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Client handler error for player " + playerId);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                server.removeClient(playerId);
            }
        }
        
        public synchronized void sendMessage(GameMessage message) {
            try {
                out.writeObject(message);
                out.flush();
            } catch (IOException e) {
                System.err.println("Error sending message to player " + playerId);
            }
        }
    }
    
    public static void main(String[] args) throws IOException {
        GameServer server = new GameServer(5555);
        server.start();
        System.out.println("Server is running. Press Ctrl+C to stop.");
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server...");
            server.stop();
        }));
    }
}
