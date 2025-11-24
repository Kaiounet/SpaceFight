# Jaylib Multiplayer Game

A simple multiplayer game using Jaylib (Raylib Java port) with socket-based networking.

## Project Structure

```
src/main/java/com/kaiounet/
├── GameServerApp.java          # Entry point for server
├── GameClientApp.java          # Entry point for client
├── game/
│   ├── Player.java             # Player data model
│   └── MultiplayerGame.java    # Game logic and rendering
└── network/
    ├── GameMessage.java        # Network message protocol
    ├── GameClient.java         # Client networking
    └── GameServer.java         # Server with connection handling
```

## How It Works

### Server
- Listens on port 5555 (configurable)
- Manages player connections
- Broadcasts player movements to all connected clients
- Assigns unique IDs and colors to players

### Client
- Connects to server at `localhost:5555` (configurable)
- Sends local player movements
- Receives and renders remote players
- Handles disconnections

### Game
- Simple 1200x800 window
- Players are 30x30 colored squares
- Control with Arrow Keys or WASD
- Real-time position updates sent to server
- Server broadcasts to all clients

## Building

```bash
./gradlew build
```

## Running

### Start Server
```bash
./gradlew run --args=GameServerApp
# Or specify custom port:
java -cp build/libs/Jaylib101-1.0-SNAPSHOT.jar com.kaiounet.GameServerApp 5555
```

### Start Client(s)
```bash
./gradlew run --args=GameClientApp
# Or with custom host/port:
java -cp build/libs/Jaylib101-1.0-SNAPSHOT.jar com.kaiounet.GameClientApp localhost 5555
```

## Features

- ✅ Multiplayer support (2+ players)
- ✅ Real-time position synchronization (60 FPS)
- ✅ Unique player colors (auto-assigned)
- ✅ Simple movement mechanics (arrow keys/WASD)
- ✅ Clean separation of concerns (game, network, data)
- ✅ **Server-side state tracking** - accurate positions for new joiners
- ✅ **Correct player ID assignment** - no ID collisions
- ✅ **No position glitching** - clients ignore own movement echoes

## Protocol

Messages are serialized Java objects:
- `PLAYER_JOIN`: Sent when player connects (includes CURRENT position, not default)
- `PLAYER_MOVE`: Sent when player moves (broadcasted to all except sender)
- `PLAYER_LEAVE`: Sent when player disconnects
- `STATE_UPDATE`: Reserved for future use

## Critical Implementation Details

### Message Ordering (Fixed)
When a new player joins, the server sends:
1. **New player's own ID first** (ensures correct localPlayerId assignment)
2. **All existing players' current positions** (not default starting positions)
3. **Broadcast to others** that new player joined (excluding new player)

### Position Tracking (Fixed)
- Server maintains `playerPositions` map
- Updated on every PLAYER_MOVE message
- Used to give new players accurate state of existing players

### Movement Filtering (Fixed)
- Clients ignore PLAYER_MOVE messages where `playerId == localPlayerId`
- Local player position controlled by keyboard input only
- Prevents players from "snapping" to other players' positions

See [DEBUGGING.md](DEBUGGING.md) for details on issues fixed.

## Next Steps

You can extend this with:
- Item pickup/collision detection
- Scoring system
- Chat messages
- Different game modes
- Persistent lobbies
- UDP for better latency (if needed)
