# Quick Start Guide - Multiplayer Jaylib Game

## 📋 What You Have

A complete multiplayer game system with:
- **Server** (GameServerApp.java) - Manages connections and broadcasts state
- **Client** (GameClientApp.java) - Connects to server and renders game
- **Game Logic** (MultiplayerGame.java) - Handles input and rendering
- **Network Layer** (GameClient.java, GameServer.java) - Socket-based communication
- **Data Model** (Player.java, GameMessage.java) - Game entities and protocol

## 🚀 How to Run

### Option 1: Using Gradle (Recommended)

**Terminal 1 - Start Server:**
```bash
cd /home/kai/dev/Jaylib101
./gradlew run --main-class com.kaiounet.GameServerApp
```

**Terminal 2 - Start Client 1:**
```bash
cd /home/kai/dev/Jaylib101
./gradlew run --main-class com.kaiounet.GameClientApp
```

**Terminal 3+ - Start Additional Clients:**
```bash
cd /home/kai/dev/Jaylib101
./gradlew run --main-class com.kaiounet.GameClientApp
```

### Option 2: Using Java Directly

First build the jar:
```bash
./gradlew build
```

**Server:**
```bash
java -cp build/libs/Jaylib101-1.0-SNAPSHOT.jar com.kaiounet.GameServerApp
```

**Client (on same machine):**
```bash
java -cp build/libs/Jaylib101-1.0-SNAPSHOT.jar com.kaiounet.GameClientApp localhost 5555
```

**Client (on different machine):**
```bash
java -cp build/libs/Jaylib101-1.0-SNAPSHOT.jar com.kaiounet.GameClientApp 192.168.1.100 5555
```

## 🎮 Controls

- **Arrow Keys** or **WASD** - Move your colored square
- **Close Window** - Disconnect from server

## 🔍 What's Happening

1. **Server** waits for connections and assigns player IDs
2. Each **Client** connects and receives a unique ID + color
3. When you move, your client sends position to server
4. **Server** broadcasts all player positions to all clients
5. Each **Client** renders all players in real-time

## 📦 File Structure

```
src/main/java/com/kaiounet/
├── GameServerApp.java          # Server main entry
├── GameClientApp.java          # Client main entry
├── game/
│   ├── Player.java             # Player: id, x, y, color
│   └── MultiplayerGame.java    # Game: update/render loop
└── network/
    ├── GameMessage.java        # Message protocol (PLAYER_JOIN/MOVE/LEAVE)
    ├── GameClient.java         # Async socket client
    └── GameServer.java         # Multi-threaded socket server
```

## 🔧 Customization Ideas

### Change Port Number
Edit `GameServerApp.main()` or pass custom port:
```bash
java -cp build/libs/... com.kaiounet.GameServerApp 9999
```

### Adjust Game Settings
In `MultiplayerGame.java`:
- `moveSpeed = 5` - How fast players move
- `width = 1200, height = 800` - Window size
- `Player.SIZE = 30` - Player square size

### Add Features
- **Collision detection** - Check player overlaps in MultiplayerGame
- **Items** - Create Item class, broadcast item pickups
- **Chat** - Add MESSAGE type to GameMessage enum
- **Health/Score** - Add to Player class and broadcast
- **Different game modes** - Team-based, free-for-all, etc.

## 📊 How the Network Works

```
Client 1          Server              Client 2
   |                 |                   |
   |--PLAYER_JOIN--->|                   |
   |                 |---PLAYER_JOIN---->|
   |                 |                   |
   |--PLAYER_MOVE--->|                   |
   |                 |---PLAYER_MOVE---->|
   |                 |<--PLAYER_MOVE-----|
   |<--PLAYER_MOVE---|                   |
   |                 |                   |
   X (disconnect)    |--PLAYER_LEAVE---->|
   |                 |                   |
```

## 🐛 Troubleshooting

**"Connection refused"**
- Make sure server is running first
- Check port 5555 isn't blocked by firewall

**"No display found"**
- Make sure you have X11/Wayland display server running
- Use WSL with X11 forwarding if on Windows

**Janky movement**
- Network latency is normal - server broadcasts at 60 FPS
- Add client-side prediction for smoother feel (future enhancement)

---

**Happy gaming! 🎮**
