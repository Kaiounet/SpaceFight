# Debugging Guide - Multiplayer Game Session Issues

This document covers the issues encountered and fixed during development.

## Issues Fixed in This Session

### 1. NullPointerException: ObjectOutputStream is null

**Symptom:**
```
Exception in thread "pool-1-thread-1" java.lang.NullPointerException: 
Cannot invoke "java.io.ObjectOutputStream.writeObject(Object)" because "this.out" is null
```

**Root Cause:**
Server tried to broadcast PLAYER_JOIN message in the main acceptance thread before the ClientHandler had initialized its output stream.

**Flow (Wrong):**
```
Main thread:
  1. Accept connection
  2. Create ClientHandler
  3. Add to clients map
  4. Broadcast PLAYER_JOIN immediately ← out is still null!
  
Worker thread:
  5. Initialize out/in streams
  6. Start listening
```

**Solution:**
Move the broadcast into the ClientHandler's run() method AFTER streams are initialized.

**Code Change:**
```java
// Before: broadcast in main thread (wrong)
broadcastMessage(new GameMessage(...));

// After: broadcast in ClientHandler after streams initialized
out = new ObjectOutputStream(socket.getOutputStream());
out.flush();
in = new ObjectInputStream(socket.getInputStream());
// NOW safe to send messages
broadcastMessage(new GameMessage(...));
```

---

### 2. Player Cannot See Other Players (One-way Visibility)

**Symptom:**
- Player 1 joins and moves: All clients see them
- Player 2 joins: Player 1 sees Player 2 instantly
- Player 2 moves: Player 1 sees movement, but Player 2 doesn't see Player 1

**Root Cause:**
When Player 2 joined, it only received default starting positions (150, 150) for Player 1, but Player 1 had actually moved to (300, 215). Player 2 rendered Player 1 at the wrong starting location, then Player 1's movements never updated Player 2 because the messages were being ignored.

**Solution:**
Server must track current player positions and send those (not default positions) to new joiners.

**Code Change:**
```java
// Add position tracking map
private final Map<Integer, float[]> playerPositions = new ConcurrentHashMap<>();

// Update on every move
if (message.type == GameMessage.MessageType.PLAYER_MOVE) {
    playerPositions.put(message.playerId, new float[]{message.x, message.y});
}

// Send current position to new players
float[] pos = server.playerPositions.get(existingHandler.playerId);
float x = (pos != null) ? pos[0] : (100 + existingHandler.playerId * 50);
float y = (pos != null) ? pos[1] : (100 + existingHandler.playerId * 50);
```

---

### 3. Player Snaps to Other Player's Position

**Symptom:**
- Player 1 moves smoothly
- Player 2 moves smoothly  
- But Player 1's client suddenly jumps to where Player 2 is and follows them
- Appears as if Player 1 is "possessed" by Player 2's input

**Root Cause:**
When server broadcasts PLAYER_MOVE from Player 2, it sends it to ALL clients including Player 1. Player 1 was receiving the broadcast and applying it to Player 1's position:

```java
// WRONG - updates any player including self
Player player = players.get(message.playerId);
if (player != null) {
    player.move(message.x, message.y);  // Updates Player 1!
}
```

**Solution:**
Client must check if the PLAYER_MOVE is for itself and ignore it (local input is authoritative).

**Code Change:**
```java
case PLAYER_MOVE:
    // CORRECT - skip own movement messages
    if (message.playerId != localPlayerId) {
        Player player = players.get(message.playerId);
        if (player != null) {
            player.move(message.x, message.y);
        }
    }
    break;
```

---

### 4. Player 2 Gets Assigned ID = 1

**Symptom:**
- Server console shows: "New client connected with ID: 2"
- Client 2 console shows: "You are player: 1"
- Both players think they are Player 1

**Root Cause:**
Race condition in message ordering. The server was sending:

```
Order 1: PLAYER_JOIN for ID=1 (existing player to new player)
Order 2: PLAYER_JOIN for ID=2 (new player's own ID)
```

Client processes messages in order:
```java
if (localPlayerId == -1) {
    localPlayerId = message.playerId;  // Sets to 1!
}
```

The FIRST PLAYER_JOIN received (ID=1) set localPlayerId, so Player 2 thought it WAS Player 1.

**Solution:**
Send new player's OWN ID first, before any other players.

**Code Change:**
```java
// CORRECT ORDER:
// 1. Send new player its own ID
sendMessage(new GameMessage(MessageType.PLAYER_JOIN, playerId, ...));

// 2. Then send existing players
for (ClientHandler existing : ...) {
    sendMessage(new GameMessage(MessageType.PLAYER_JOIN, existing.playerId, ...));
}

// 3. Finally broadcast to others
broadcastMessageExcept(new GameMessage(...), playerId);
```

**Message Sequence Diagram:**
```
Server                  Client 1            Client 2
  |                       |                   |
  |--- ID=1 join -------->|                   |
  |                       |-- sets ID=1 ----->|
  |                       |                   |
  [Client 2 connects]     |                   |
  |                       |                   |
  |--- ID=2 join (to C2)--+-- only to C2 --->|
  |                       |  -- sets ID=2 -->|
  |                       |                   |
  |--- ID=1 join (from C2 auth) (to C1) --->|
  |                       |<-- sets as player 1
  |                       |
  |--- Broadcast ID=2 join (except C2) ----->|
  |                       |<-- sets as player 2
```

---

## Debug Output Format

The fixed code includes debug logging. Look for these patterns:

**Server Console:**
```
New client connected with ID: 1 (nextPlayerId now: 2)
Sending player 1 its own ID assignment: (150.0,150.0)
Sending player 1 info about existing player...
Broadcasting PLAYER_JOIN for ID 1 to all other clients
```

**Client Console:**
```
[-1] Received message type=PLAYER_JOIN playerId=1 pos=(150.0,150.0)
>>> SETTING localPlayerId to: 1
[1] Received message type=PLAYER_MOVE playerId=2 pos=(210.0,155.0)
P1 ignoring own MOVE message
```

---

## Key Lessons

### 1. Message Ordering Matters
In distributed systems, the order in which messages arrive can be critical. Always consider:
- What does the client expect first?
- What state should be established before other messages?

### 2. Race Conditions are Subtle
Just because code works once doesn't mean it's thread-safe. Test with:
- Rapid connections (multiple clients joining quickly)
- Existing players moving while new players join
- Disconnects during state broadcasts

### 3. Server State Tracking
The server must maintain authoritative state:
- Player positions (for new joiners)
- Active connections (for broadcasts)
- Player IDs (cannot reuse without careful cleanup)

### 4. Client-side Authority Limits
Clients should not be authoritative about:
- Their own ID (server assigns)
- Other players' positions (server broadcasts)
But ARE authoritative about:
- Local input (keyboard)
- Local rendering
- When to disconnect

### 5. Filtering Broadcasts
Sometimes you need selective broadcasts:
- `broadcastMessage()` - send to all
- `broadcastMessageExcept()` - send to all but one
- `sendMessageToClient()` - send to one

---

## Testing Checklist

When making multiplayer changes, test these scenarios:

- [ ] Single player joins, moves around
- [ ] Two players connect (in order A then B)
- [ ] Two players connect (in order B then A)
- [ ] First player moves, then second joins
- [ ] Second player joins, then both move
- [ ] Three or more players simultaneously
- [ ] Player disconnects, others continue
- [ ] New player joins after disconnect
- [ ] Rapid movements by multiple players
- [ ] Connection with high latency

---

## Performance Considerations

### Observed Issues
- Java Object Serialization has overhead (~1-2ms per message)
- At 60 FPS with many players, message volume can spike
- No message batching (each move = 1 message)

### Optimization Ideas
- Batch multiple PLAYER_MOVE into one network packet
- Use delta encoding (only send position changes)
- Compress positions (e.g., uint16 instead of float)
- Use UDP for movement (fire-and-forget)
- Implement dead reckoning (predict next position)

---

## Future Improvements

### Short Term
- [ ] Add client-side prediction for smoother movement
- [ ] Implement message throttling (max updates per second)
- [ ] Add ping/latency display
- [ ] Implement graceful disconnect handling

### Medium Term
- [ ] Binary protocol (replace Java serialization)
- [ ] UDP support for lower latency
- [ ] Room/lobby system
- [ ] Spectator mode

### Long Term
- [ ] Dedicated server infrastructure
- [ ] Cloud deployment
- [ ] Match-making system
- [ ] Persistent player data
