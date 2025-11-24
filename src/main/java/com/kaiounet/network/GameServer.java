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
    private final Map<Integer, float[]> playerPositions = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private int nextPlayerId = 1;
    private volatile boolean running = true;
    
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
                    System.out.println("New client connected with ID: " + playerId + " (nextPlayerId now: " + nextPlayerId + ")");
                    
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
        // Track player positions for new joiners
        if (message.type == GameMessage.MessageType.PLAYER_MOVE) {
            playerPositions.put(message.playerId, new float[]{message.x, message.y});
        }
        
        for (ClientHandler handler : clients.values()) {
            handler.sendMessage(message);
        }
    }
    
    public void broadcastMessageExcept(GameMessage message, int excludePlayerId) {
        // Track player positions for new joiners
        if (message.type == GameMessage.MessageType.PLAYER_MOVE) {
            playerPositions.put(message.playerId, new float[]{message.x, message.y});
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
        playerPositions.remove(playerId);
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
                
                // FIRST: Send THIS PLAYER its own ID (so it sets localPlayerId correctly)
                float startX = 100 + (playerId * 50);
                float startY = 100 + (playerId * 50);
                server.playerPositions.put(playerId, new float[]{startX, startY});
                
                System.out.println("Sending player " + playerId + " its own ID assignment: (" + startX + "," + startY + ")");
                sendMessage(new GameMessage(
                    GameMessage.MessageType.PLAYER_JOIN,
                    playerId,
                    startX,
                    startY,
                    server.getPlayerColor(playerId)
                ));
                
                // THEN: Send all OTHER existing players to this new player with their CURRENT positions
                for (ClientHandler existingHandler : server.getClients().values()) {
                    if (existingHandler.playerId != this.playerId) {
                        // Get current position or use default
                        float[] pos = server.playerPositions.get(existingHandler.playerId);
                        float x = (pos != null) ? pos[0] : (100 + existingHandler.playerId * 50);
                        float y = (pos != null) ? pos[1] : (100 + existingHandler.playerId * 50);
                        
                        // Send the existing player's join message to this new player
                        System.out.println("Sending player " + playerId + " info about existing player " + existingHandler.playerId + " at (" + x + "," + y + ")");
                        sendMessage(new GameMessage(
                            GameMessage.MessageType.PLAYER_JOIN,
                            existingHandler.playerId,
                            x,
                            y,
                            server.getPlayerColor(existingHandler.playerId)
                        ));
                    }
                }
                
                // FINALLY: Broadcast to OTHERS that this player joined (don't send to the new player again)
                System.out.println("Broadcasting PLAYER_JOIN for ID " + playerId + " to all other clients");
                server.broadcastMessageExcept(new GameMessage(
                    GameMessage.MessageType.PLAYER_JOIN,
                    playerId,
                    startX,
                    startY,
                    server.getPlayerColor(playerId)
                ), playerId);
                
                while (true) {
                    try {
                        GameMessage message = (GameMessage) in.readObject();
                        
                        if (message.type == GameMessage.MessageType.PLAYER_MOVE) {
                            message.playerId = playerId; // Ensure correct ID
                            server.broadcastMessage(message);
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
        
        // Keep server running
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server...");
            server.stop();
        }));
    }
}
