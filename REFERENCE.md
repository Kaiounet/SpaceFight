# Quick Reference Card

## 🎮 Controls
| Key | Action |
|-----|--------|
| ↑ / W | Move up |
| ↓ / S | Move down |
| ← / A | Move left |
| → / D | Move right |
| Close window | Disconnect |

## 🖥️ Commands

### Build
```bash
./gradlew build              # Compile all files
./gradlew clean build        # Clean build
```

### Run
```bash
./gradlew run --main-class com.kaiounet.GameServerApp    # Server
./gradlew run --main-class com.kaiounet.GameClientApp    # Client
```

### Direct Java
```bash
java -cp build/libs/Jaylib101-1.0-SNAPSHOT.jar \
  com.kaiounet.GameServerApp [port]              # Server
java -cp build/libs/Jaylib101-1.0-SNAPSHOT.jar \
  com.kaiounet.GameClientApp [host] [port]       # Client
```

## 📝 File Locations
```
Server Files:
  src/main/java/com/kaiounet/GameServerApp.java
  src/main/java/com/kaiounet/network/GameServer.java

Client Files:
  src/main/java/com/kaiounet/GameClientApp.java
  src/main/java/com/kaiounet/network/GameClient.java
  src/main/java/com/kaiounet/game/MultiplayerGame.java

Shared Files:
  src/main/java/com/kaiounet/network/GameMessage.java
  src/main/java/com/kaiounet/game/Player.java

Documentation:
  README_MULTIPLAYER.md   - Overview
  QUICKSTART.md          - Getting started
  ARCHITECTURE.md        - Deep dive
  STRUCTURE_SUMMARY.txt  - Code structure
  FILES_CREATED.md       - What was created
  REFERENCE.md           - This file
```

## 🔧 Common Modifications

### Change port
**File:** `GameServerApp.java`
```java
GameServer server = new GameServer(9999);  // Was: 5555
```

### Change movement speed
**File:** `MultiplayerGame.java`
```java
private final float moveSpeed = 10;  // Was: 5
```

### Change window size
**File:** `MultiplayerGame.java`
```java
private final int width = 1600;  // Was: 1200
private final int height = 1000; // Was: 800
```

### Add new message type
**File:** `GameMessage.java`
```java
public enum MessageType {
    PLAYER_JOIN,
    PLAYER_MOVE,
    PLAYER_LEAVE,
    CHAT_MESSAGE,  // NEW
    STATE_UPDATE
}
```

### Add new player color
**File:** `GameServer.java`
```java
public int getPlayerColor(int playerId) {
    int[] colors = {
        0xFF0000FF, // RED
        0x00FF00FF, // GREEN
        0x0000FFFF, // BLUE
        0xFFFF00FF, // YELLOW
        0xFF00FFFF, // MAGENTA
        0xFF8800FF, // ORANGE
        0xFFFFFFFF  // WHITE (new)
    };
    return colors[playerId % colors.length];
}
```

## 📊 Architecture Pattern

```
┌─────────────────────────┐
│   Game Client Main      │ (GameClientApp)
│   └─ MultiplayerGame    │ (Game loop)
│       ├─ update()       │ Process network + input
│       └─ render()       │ Draw with Jaylib
└─────────────────────────┘
           ↕ (sockets)
┌─────────────────────────┐
│   Game Server Main      │ (GameServerApp)
│   └─ GameServer         │ (Multi-threaded)
│       └─ ClientHandler[]│ (One per client)
└─────────────────────────┘
```

## 🔗 Network Messages

### PLAYER_JOIN
```java
type: PLAYER_JOIN
playerId: 1 (assigned by server)
x, y: 50, 50 (starting position)
color: 0xFF0000FF (RED)
```

### PLAYER_MOVE
```java
type: PLAYER_MOVE
playerId: 1
x, y: 100, 150 (new position)
color: 0xFF0000FF
```

### PLAYER_LEAVE
```java
type: PLAYER_LEAVE
playerId: 1
x, y: (ignored)
color: (ignored)
```

## ⚙️ Threading Model

### Server
```
Main Thread (accepts connections)
  └─ Thread Pool
      └─ ClientHandler #1 (reads from client 1)
      └─ ClientHandler #2 (reads from client 2)
      └─ ClientHandler #3 (reads from client 3)
```

### Client
```
Main Thread (game loop - update/render)
  └─ Executor Service
      ├─ Network Reader (polls from server)
      └─ Message Sender (async writes)
```

## 🔍 Debug Tips

### See all players connected
**File:** `GameServerApp.java` console output
```
Server started on port 5555
New client connected: 1
New client connected: 2
Player joined: 2
```

### See local player ID and count
**In-game display** (top-left corner)
```
Arrow Keys or WASD to Move
Players: 3
Your ID: 2
```

### Connection refused error
- Make sure server is running FIRST
- Check firewall allows port 5555
- Check no other app uses that port

### No window appears
- Make sure display is available (X11/Wayland)
- On WSL, enable X11 forwarding: `export DISPLAY=:0`

## 📈 Performance Targets

| Metric | Target | Notes |
|--------|--------|-------|
| FPS | 60 | Game loop speed |
| Update rate | 60/sec | Max messages from server |
| Latency | 50-200ms | Depends on network |
| Max players | 50-100 | Typical server limit |

## 🎯 Next Steps to Extend

1. **Add scoring** - Track points, display leaderboard
2. **Collision** - Detect when players touch
3. **Items** - Spawn pickups, broadcast consumption
4. **Chat** - Add MESSAGE type to protocol
5. **Health** - Add health pool, damage mechanics
6. **Lobby** - Room selection before game
7. **Persistence** - Save scores to database
8. **Better protocol** - Use binary instead of serialization

## 🐛 Common Issues & Fixes

| Issue | Fix | File |
|-------|-----|------|
| NullPointerException on broadcast | Initialize streams before broadcasting | GameServer.java |
| New player can't see existing players | Server tracks current positions | GameServer.playerPositions |
| Player snaps to other player | Ignore own PLAYER_MOVE messages | MultiplayerGame.java |
| Wrong player ID assigned | Send own ID before other players | GameServer.ClientHandler |

See [DEBUGGING.md](DEBUGGING.md) for detailed explanations of each fix.

---

**Last Updated:** November 24, 2025  
**Status:** Ready to use ✅
