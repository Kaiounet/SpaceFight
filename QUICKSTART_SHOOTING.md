# Quick Start Guide - Shooting Game

## Building

```bash
cd /home/kai/dev/Jaylib101
./gradlew build
```

## Running the Game

### Terminal 1: Start the Server
```bash
cd /home/kai/dev/Jaylib101
java -cp build/libs/Jaylib101-1.0-SNAPSHOT.jar com.kaiounet.GameServerApp 5555
```

You should see:
```
Server started on port 5555
Server is running. Press Ctrl+C to stop.
```

### Terminal 2+: Start Clients (open multiple terminals for each player)
```bash
cd /home/kai/dev/Jaylib101
java -cp build/libs/Jaylib101-1.0-SNAPSHOT.jar com.kaiounet.GameClientApp localhost 5555
```

Each client will start a game window showing:
- Your player (a colored square)
- Other players (other colored squares)
- Health bar above each player
- Scoreboard on the right side
- Your score, health, and ID on the left side

## Gameplay

### Controls
- **W/A/S/D** or **Arrow Keys**: Move your player around the map
- **SPACE**: Fire a beam
- **Mouse**: Move mouse to aim the beam direction (beams fire toward mouse)

### Winning
- Hit other players with beams to reduce their health
- When a player's health reaches 0, they respawn with full health
- You get +10 points for each kill
- The player with the most kills wins!

### Tips
- Move around to dodge enemy fire
- Aim at enemies' predicted positions
- Check the scoreboard to see who's winning
- Team up to focus on the leader!

## Testing Different Scenarios

### Test 1: Basic Movement & Shooting
1. Start server
2. Start 2 clients
3. Both players move around
4. Try shooting at each other
5. Check that beams appear and disappear when hitting players

### Test 2: Health & Respawning
1. Start server
2. Start 2 clients
3. One player shoots the other multiple times (10 damage each, 100 HP total = 10 hits to kill)
4. When health reaches 0, player respawns with 100 HP
5. Killer gets +10 points

### Test 3: Multiplayer Synchronization
1. Start server
2. Start 3+ clients
3. One player shoots another
4. Verify:
   - All players see the beam fire
   - Health updates on all screens
   - Scoreboard updates for all players
   - Respawn happens for all players

### Test 4: Disconnection
1. Start server and 2 clients
2. Kill a player a few times
3. Disconnect one client
4. Verify:
   - "Player left" message appears for other client
   - Game continues normally with remaining players
   - Disconnected player disappears from scoreboard

## Troubleshooting

### "Connection refused"
- Make sure server is running first
- Check port 5555 is not in use: `lsof -i :5555`

### Beams not appearing
- Make sure your mouse position is different from your player position
- Spacebar must be pressed (not held)
- Check that 200ms cooldown between shots has passed

### Health not updating
- Wait for network sync (happens automatically)
- Check server logs for hit messages

### Game freezes
- Check console for errors
- Restart server and clients
- Make sure you're on port 5555

## Project Files

- `src/main/java/com/kaiounet/GameServerApp.java` - Server entry point
- `src/main/java/com/kaiounet/GameClientApp.java` - Client entry point
- `src/main/java/com/kaiounet/game/MultiplayerGame.java` - Game logic & rendering
- `src/main/java/com/kaiounet/game/Player.java` - Player data model
- `src/main/java/com/kaiounet/game/Beam.java` - Beam/projectile model
- `src/main/java/com/kaiounet/network/GameMessage.java` - Network protocol
- `src/main/java/com/kaiounet/network/GameServer.java` - Server networking
- `src/main/java/com/kaiounet/network/GameClient.java` - Client networking

Enjoy the game!
