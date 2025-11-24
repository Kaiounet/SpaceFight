package com.kaiounet.network;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class GameClient {
    private final String host;
    private final int port;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private final BlockingQueue<GameMessage> messageQueue = new LinkedBlockingQueue<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private volatile boolean connected = false;
    
    public GameClient(String host, int port) {
        this.host = host;
        this.port = port;
    }
    
    public boolean connect() {
        try {
            socket = new Socket(host, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
            connected = true;
            
            // Start listening for messages
            executor.execute(this::listenForMessages);
            System.out.println("Connected to server at " + host + ":" + port);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
            return false;
        }
    }
    
    public void sendMessage(GameMessage message) {
        if (!connected) return;
        executor.execute(() -> {
            try {
                synchronized (out) {
                    out.writeObject(message);
                    out.flush();
                }
            } catch (IOException e) {
                System.err.println("Error sending message: " + e.getMessage());
                disconnect();
            }
        });
    }
    
    private void listenForMessages() {
        try {
            while (connected) {
                GameMessage message = (GameMessage) in.readObject();
                messageQueue.offer(message);
            }
        } catch (EOFException e) {
            System.out.println("Connection closed by server");
        } catch (IOException | ClassNotFoundException e) {
            if (connected) {
                System.err.println("Error receiving messages: " + e.getMessage());
            }
        } finally {
            disconnect();
        }
    }
    
    public GameMessage pollMessage() {
        return messageQueue.poll();
    }
    
    public GameMessage peekMessage() {
        return messageQueue.peek();
    }
    
    public boolean hasMessages() {
        return !messageQueue.isEmpty();
    }
    
    public void disconnect() {
        connected = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        executor.shutdown();
    }
    
    public boolean isConnected() {
        return connected;
    }
}
