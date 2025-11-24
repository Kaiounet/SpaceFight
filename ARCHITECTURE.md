# Architecture Overview

## System Design

```
┌─────────────────────────────────────────────────────────────┐
│                     MULTIPLAYER GAME                         │
└─────────────────────────────────────────────────────────────┘

                    Network Layer (Sockets)
                            TCP
                    ↙─────────────────────↖
                   /                       \
         ┌────────────────┐      ┌────────────────┐
         │  Game Server   │      │  Game Client 1 │
         ├────────────────┤      ├────────────────┤
         │ Multi-threaded │◄────►│ Game Window    │
         │ Connection Mgr │      │ + Input Handling
         │ Broadcaster    │      │ + Renderer     │
         └────────────────┘      └────────────────┘
         Thread per client       ↑
                 ↑               Main thread +
                 │               Network reader thread
         ┌───────┴──────┐        
         │              │        ┌────────────────┐
         │          ┌────────────►│ Game Client 2  │
         │          │   (...)    ├────────────────┤
         │          │            │ Game Window    │
         └──────────┘            │ + Input Handlers
                                 │ + Renderer     │
                                 └────────────────┘
```

## Component Details

### 1. GameServer (Server-side)
**Responsibilities:**
- Accept TCP connections
- Manage client connections in thread pool
- Broadcast state to all clients
- Handle player disconnections

**Threading Model:**
- Main thread: Accepts new connections
- Worker threads: One per client connection
- Executor service: Thread pool management

**Message Flow (Critical Order):**
1. Client connects → server assigns ID + color
2. **Server sends new player ITS OWN ID first** (so client sets localPlayerId immediately)
3. **Server sends new player all existing players' current positions** (not default start positions!)
4. **Server broadcasts new player join to OTHER clients only** (not back to new player)
5. Clients send PLAYER_MOVE messages continuously
6. Server tracks player positions and broadcasts to all
7. Server sends PLAYER_LEAVE on disconnect

### 2. GameClient (Client-side Network)
**Responsibilities:**
- Connect to server
- Send player input as messages
- Receive server broadcasts
- Queue messages for game loop

**Threading Model:**
- Main thread: Sends messages (queue-based)
- Reader thread: Polls messages and stores in queue
- Executor service: Background threads

**Message Queue:**
- Non-blocking queue for game thread safety
- Game loop polls for messages each frame

### 3. MultiplayerGame (Client-side Logic)
**Responsibilities:**
- Initialize Jaylib window
- Process network messages
- Handle local player input
- Render all players

**Update Loop:**
```
while (!WindowShouldClose) {
  processNetworkMessages()  → Update player positions
  handleLocalInput()        → Move local player
  sendUpdates()            → Send to server if moved
  render()                 → Draw all players
}
```

### 4. Data Model
**Player.java:**
- id: unique identifier
- x, y: position
- color: player's color
- SIZE: 30x30 pixels

**GameMessage.java:**
- Serializable protocol object
- Types: PLAYER_JOIN, PLAYER_MOVE, PLAYER_LEAVE
- Contains: playerId, position, color

## Communication Protocol

### Message Types

```java
PLAYER_JOIN {
  playerId: assigned by server
  x, y: CURRENT position (not starting position!)
  color: assigned by server
  
  Sent in order:
  1. New player receives their own ID
  2. New player receives all existing players' current positions
  3. Other players receive notification of new player join
}

PLAYER_MOVE {
  playerId: player being moved
  x, y: new position (CURRENT, not delta)
  color: player's color
  
  Sent by: Any connected client
  Broadcasted by: Server to all clients EXCEPT sender
}

PLAYER_LEAVE {
  playerId: player who left
  x, y: ignored
  color: ignored
}
```

### Important Protocol Details

**Position Tracking:**
- Server maintains `playerPositions` map of all player locations
- Updated on every PLAYER_MOVE message
- Used to send accurate state when new players join

**Message Ordering (Critical):**
1. New client's own PLAYER_JOIN arrives first
2. Then existing players' PLAYER_JOIN messages
3. New player ignores its own echoed broadcasts (using playerId check)
4. Other players see new player via broadcast

## Common Issues & Solutions

### Issue 1: Player gets wrong ID when joining
**Problem:** New player receives existing player's PLAYER_JOIN before its own
**Solution:** Always send new player's own ID first, then send other players
**Code:** GameServer.ClientHandler.run() - FIRST sends playerId, THEN loops through existing

### Issue 2: New player can't see existing players
**Problem:** New player only receives default starting positions, not current positions
**Solution:** Server tracks current positions in `playerPositions` map
**Code:** GameServer.broadcastMessage() updates map on PLAYER_MOVE; new players get current positions

### Issue 3: Player snaps to other player's position when that player moves
**Problem:** Client receives PLAYER_MOVE and applies it to local player
**Solution:** Client checks `if (message.playerId != localPlayerId)` and ignores own moves
**Code:** MultiplayerGame.processNetworkMessages() - PLAYER_MOVE case

### Issue 4: Duplicated broadcasts going back to new player
**Problem:** Broadcasting to all clients includes sending join message back to new player
**Solution:** Use `broadcastMessageExcept(msg, newPlayerId)` to exclude new player
**Code:** GameServer.broadcastMessageExcept() - loops through clients and skips sender

### Serialization
- Java Object Serialization (ObjectOutputStream/ObjectInputStream)
- Pros: Simple, automatic, handles complex objects
- Cons: Slower than binary, Java-only compatibility

### Reliability
- TCP ensures all messages arrive in order

## Performance Considerations

### Current Approach
- **Updates**: 60 FPS (SetTargetFPS)
- **Message frequency**: Up to 60/sec per player
- **Latency**: Depends on network + processing
- **Scalability**: ~50-100 concurrent players (typical)

### Potential Bottlenecks
1. **Serialization overhead** - Java serialization is slow
2. **TCP overhead** - Header + overhead per message
3. **Single server thread broadcasting** - Could be parallelized

### Optimizations (Future)
- **Binary protocol** - Custom serialization (2x faster)
- **Message batching** - Send multiple updates in one packet
- **State compression** - Only send changed positions
- **Spatial partitioning** - Only broadcast to nearby players
- **UDP** - For lower latency (but no reliability)
- **Server-side prediction** - Reduce perceived lag

## Thread Safety

### Server
```
Main thread:     Accepts connections
ClientHandler:   One per connected client
Executor:        Manages thread pool

Shared Data:     clients (ConcurrentHashMap)
Synchronization: Atomic operations on map
```

### Client
```
Main thread:     Game loop
Reader thread:   Receives messages
Executor:        Manages background tasks

Shared Data:     messageQueue (BlockingQueue)
Synchronization: Queue handles threading
```

## Error Handling

**Graceful Disconnections:**
- Client closes window → disconnect cleanly
- Server detects EOF → remove client
- Network error → reconnect logic (future)

**Current Limitations:**
- No reconnection attempts
- No heartbeat/keep-alive
- No message ordering verification
- No encryption (LAN only)

## Extensions

### Level 1 (Easy)
- [ ] Add score/points system
- [ ] Collision detection between players
- [ ] Chat system (add MESSAGE type)
- [ ] Name/nickname display

### Level 2 (Medium)
- [ ] Items/powerups to collect
- [ ] Health system with damage
- [ ] Shooting/combat mechanics
- [ ] Game state synchronization (current map state)

### Level 3 (Hard)
- [ ] Binary protocol for efficiency
- [ ] UDP for lower latency
- [ ] Client-side prediction
- [ ] Lobby/room system
- [ ] Persistent storage (database)

---

**Total Code:** ~1000 lines across 7 files
**Network Calls:** Object serialization + TCP sockets
**Complexity:** Low (great learning project!)
