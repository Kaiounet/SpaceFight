# Architecture

## Overview

This is a client-server multiplayer game using TCP sockets for networking.

```
                    Network Layer (TCP Sockets)
                            ↓
         ┌────────────────────────────────────────┐
         │         Game Server (Main Thread)       │
         │  - Listens on port 5555                │
         │  - Manages player connections          │
         │  - Broadcasts player state             │
         │  - One thread per client               │
         └────────────────────────────────────────┘
                    ↙──────────────────↖
        ┌──────────────────┐  ┌──────────────────┐
        │  Game Client 1   │  │  Game Client 2   │
        ├──────────────────┤  ├──────────────────┤
        │ Main Thread      │  │ Main Thread      │
        │ - Rendering      │  │ - Rendering      │
        │ - Input Handling │  │ - Input Handling │
        │                  │  │                  │
        │ Network Thread   │  │ Network Thread   │
        │ - Recv messages  │  │ - Recv messages  │
        └──────────────────┘  └──────────────────┘
```

## Project Structure

```
src/main/java/com/kaiounet/
├── GameServerApp.java              # Server entry point
├── GameClientApp.java              # Client entry point
├── game/
│   ├── Player.java                 # Player entity with position, movement
│   └── MultiplayerGame.java        # Game logic and rendering
├── network/
│   ├── GameMessage.java            # Serializable message protocol
│   ├── GameClient.java             # Client networking (sender + receiver)
│   └── GameServer.java             # Server connection manager
└── utils/
    └── [utility classes]
```

## Data Flow

### Startup
1. Server starts, listens on port 5555
2. Each client connects, receives unique player ID and color
3. Server maintains list of all connected players

### Game Loop
1. **Client Input Phase:**
   - Player presses arrow keys or WASD
   - Local player position updates immediately

2. **Network Phase:**
   - Client sends player position to server every frame
   - Server broadcasts all player positions to all clients
   - Client receives other players' positions

3. **Render Phase:**
   - Client renders local player and all remote players
   - Display updated every frame (~60 FPS)

## Key Classes

### GameMessage
Protocol for network communication. Contains:
- Message type (PLAYER_MOVED, PLAYER_JOINED, etc.)
- Player ID
- Player position (x, y)
- Player color (RGB)

### Player
Represents a game player. Manages:
- Position (x, y)
- Velocity (vx, vy)
- Color (for visual distinction)
- Player ID
- Movement updates based on input

### GameServer
Manages server-side logic:
- TCP socket listening
- Client connection handling
- Message broadcasting
- Player state synchronization

### GameClient
Manages client-side networking:
- TCP connection to server
- Send player position updates
- Receive remote player state
- Separate thread for receiving messages

### MultiplayerGame
Main game logic:
- Game state (all players)
- Update player positions
- Handle input
- Render graphics using Raylib

## Network Protocol

All communication uses Java serialization of `GameMessage` objects over TCP sockets.

**Message Types:**
- `PLAYER_MOVED` - Player position update
- `PLAYER_JOINED` - New player connected (server → client)
- `PLAYER_LEFT` - Player disconnected (server → client)

**Frequency:**
- Client sends position: ~60 times per second
- Server broadcasts: immediately upon receiving message

## Threading Model

### Server
- **Main thread:** Listens for connections
- **One thread per client:** Handles that client's messages and broadcasts

### Client
- **Main thread:** Rendering and input handling
- **Network thread:** Receives messages from server (non-blocking)

## Performance Considerations

- TCP chosen for reliability over UDP (network lag acceptable for this game)
- Simple serialization used (Java ObjectOutputStream)
- No message batching (one message per update)
- Potential improvements: UDP for lower latency, message compression, delta encoding
