package com.kaiounet.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Config {
    private static final Map<String, String> config = new HashMap<>();
    private static final String DEFAULT_ENV_PATH = ".env";
    private static boolean loaded = false;
    
    // Configuration keys
    public static final String SERVER_HOST = "SERVER_HOST";
    public static final String SERVER_PORT = "SERVER_PORT";
    
    // Default values
    private static final String DEFAULT_SERVER_HOST = "0.0.0.0";
    private static final int DEFAULT_SERVER_PORT = 5555;
    
    /**
     * Load configuration from .env file
     */
    public static void load() {
        load(DEFAULT_ENV_PATH);
    }
    
    /**
     * Load configuration from specified .env file
     */
    public static void load(String envPath) {
        if (loaded) {
            return; // Already loaded
        }
        
        File envFile = new File(envPath);
        
        if (!envFile.exists()) {
            // Create default .env file if it doesn't exist
            createDefaultEnv(envPath);
            loadDefaults();
        } else {
            // Load from existing .env file
            try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Skip comments and empty lines
                    if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                        continue;
                    }
                    
                    // Parse key=value
                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {
                        String key = parts[0].trim();
                        String value = parts[1].trim();
                        // Remove quotes if present
                        if ((value.startsWith("\"") && value.endsWith("\"")) ||
                            (value.startsWith("'") && value.endsWith("'"))) {
                            value = value.substring(1, value.length() - 1);
                        }
                        config.put(key, value);
                    }
                }
                System.out.println("✓ Loaded configuration from: " + envFile.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("✗ Error reading .env file: " + e.getMessage());
                loadDefaults();
            }
        }
        
        loaded = true;
    }
    
    /**
     * Get server host
     */
    public static String getServerHost() {
        return config.getOrDefault(SERVER_HOST, DEFAULT_SERVER_HOST);
    }
    
    /**
     * Get server port
     */
    public static int getServerPort() {
        try {
            return Integer.parseInt(config.getOrDefault(SERVER_PORT, String.valueOf(DEFAULT_SERVER_PORT)));
        } catch (NumberFormatException e) {
            return DEFAULT_SERVER_PORT;
        }
    }
    
    /**
     * Load default configuration values
     */
    private static void loadDefaults() {
        config.put(SERVER_HOST, DEFAULT_SERVER_HOST);
        config.put(SERVER_PORT, String.valueOf(DEFAULT_SERVER_PORT));
    }
    
    /**
     * Create default .env file if it doesn't exist
     */
    private static void createDefaultEnv(String envPath) {
        try {
            StringBuilder content = new StringBuilder();
            content.append("# Multiplayer Shooting Game - Server Configuration\n");
            content.append("# ================================================\n\n");
            content.append("# SERVER CONFIGURATION\n");
            content.append("# Bind address: 0.0.0.0 (all interfaces) or specific IP\n");
            content.append("SERVER_HOST=0.0.0.0\n");
            content.append("# Server port (1024-65535)\n");
            content.append("SERVER_PORT=5555\n\n");
            content.append("# NOTE: Client will prompt user to enter server address when starting\n");
            content.append("#       No client configuration needed here\n\n");
            content.append("# EXAMPLES:\n");
            content.append("# --------\n");
            content.append("# Local testing (accept connections from anywhere):\n");
            content.append("#   SERVER_HOST=0.0.0.0\n");
            content.append("#   SERVER_PORT=5555\n\n");
            content.append("# Network play (bind to specific IP):\n");
            content.append("#   SERVER_HOST=192.168.1.100\n");
            content.append("#   SERVER_PORT=5555\n\n");
            content.append("# Custom port for multiple servers:\n");
            content.append("#   SERVER_HOST=0.0.0.0\n");
            content.append("#   SERVER_PORT=9999\n\n");
            content.append("# Localhost only (no network access):\n");
            content.append("#   SERVER_HOST=127.0.0.1\n");
            content.append("#   SERVER_PORT=5555\n");
            
            Files.write(Paths.get(envPath), content.toString().getBytes());
            System.out.println("✓ Created default .env file at: " + new File(envPath).getAbsolutePath());
            loadDefaults();
        } catch (IOException e) {
            System.err.println("✗ Error creating .env file: " + e.getMessage());
            loadDefaults();
        }
    }
    
    /**
     * Print current configuration (for debugging)
     */
    public static void printConfig() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║       SERVER CONFIGURATION             ║");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║ Server Host:    " + String.format("%-21s", getServerHost()) + " ║");
        System.out.println("║ Server Port:    " + String.format("%-21d", getServerPort()) + " ║");
        System.out.println("╚════════════════════════════════════════╝\n");
    }
}
