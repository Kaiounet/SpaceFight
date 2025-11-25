# Extending the Game

## Common Modifications

### Add a New Feature

**1. Server-side logic:**
- Edit `src/main/java/com/kaiounet/game/MultiplayerGame.java` in the `update()` method
- Add new data to `GameMessage.java` if needed
- Update `GameServer.java` if broadcasting new state

**2. Client-side rendering:**
- Edit `src/main/java/com/kaiounet/game/MultiplayerGame.java` in the `render()` method
- Use Raylib functions (see Raylib documentation)

**3. Network protocol:**
- Modify `GameMessage.java` to include new fields
- Update sender and receiver in `GameClient.java` and `GameServer.java`

### Change Player Speed

```java
// In Player.java, modify moveSpeed constant
private static final float MOVE_SPEED = 200.0f; // pixels per second (default: 150)
```

### Change Game Window Size

```java
// In GameClientApp.java, modify constants
private static final int WINDOW_WIDTH = 1200;  // default: 800
private static final int WINDOW_HEIGHT = 800;  // default: 600
```

### Add Shooting Mechanics

1. **Add ammunition tracking to Player.java:**
   ```java
   private int ammo = 10;
   
   public void shoot() {
       if (ammo > 0) {
           ammo--;
           // Broadcast shot to server
       }
   }
   ```

2. **Extend GameMessage to include shot data:**
   ```java
   public int shotX, shotY;  // Shot position
   ```

3. **Add input handling in MultiplayerGame.java:**
   ```java
   if (IsMouseButtonPressed(MOUSE_LEFT_BUTTON)) {
       // Send shot to server
   }
   ```

4. **Update server to broadcast shots to all clients**

### Add Collectibles/Power-ups

1. **Create Collectible class** in `game/` directory
2. **Add to MultiplayerGame state:**
   ```java
   private List<Collectible> collectibles = new ArrayList<>();
   ```
3. **Implement collision detection** in `update()` method
4. **Broadcast collection events** via new GameMessage type
5. **Render collectibles** in `render()` method

### Add Obstacles

1. **Create Obstacle class** storing position and size
2. **Add collision detection** in `update()` method
3. **Sync with server** if needed (for shared world)
4. **Render obstacles** in Raylib

### Change Game Server Port

```java
// In GameServerApp.java
private static final int PORT = 5555;  // Change this value
```

### Add Chat/Communication

1. **Extend GameMessage with chat data:**
   ```java
   public String chatMessage;
   ```

2. **Add input handling for keyboard** (e.g., press 'T' to type)

3. **Display in-game** using `DrawText()` from Raylib

### Add Score/Stats Tracking

1. **Add fields to Player.java:**
   ```java
   private int score = 0;
   private int kills = 0;
   private int deaths = 0;
   ```

2. **Extend GameMessage to include stats**

3. **Display score on screen** using Raylib's `DrawText()`

## Testing Changes

After modifying code:

```bash
# Build the project
./gradlew build

# Run server and clients to test
./gradlew run --main-class com.kaiounet.GameServerApp    # Terminal 1
./gradlew run --main-class com.kaiounet.GameClientApp    # Terminal 2
./gradlew run --main-class com.kaiounet.GameClientApp    # Terminal 3
```

## Debugging

### Enable Verbose Logging

Add print statements to `GameMessage.java`, `GameClient.java`, and `GameServer.java`:

```java
System.out.println("Received message: " + message.type);
```

### Monitor Network Traffic

In `GameClient.java` receive thread:
```java
System.out.println("Got update for player " + msg.playerId + " at " + msg.x + "," + msg.y);
```

In `GameServer.java` broadcast:
```java
System.out.println("Broadcasting to " + connectedPlayers.size() + " players");
```

## Performance Tips

- **Reduce broadcast frequency:** Modify update loop timing
- **Use object pooling:** Reuse GameMessage objects instead of creating new ones
- **Compress data:** Encode position as shorts instead of floats if precision allows
- **Batch updates:** Send multiple player updates in one message

## Adding New Game Modes

1. **Add mode constant to GameMessage:**
   ```java
   public enum GameMode { DEATHMATCH, SURVIVAL, COOPERATIVE }
   public GameMode gameMode;
   ```

2. **Implement mode logic in MultiplayerGame.java**

3. **Broadcast mode-specific data** as needed

4. **Display mode status** on HUD using Raylib

## Common Issues

**"Address already in use"**
- Server is still running; kill the process or change port number

**"Connection refused"**
- Server isn't running; start server before clients

**"Players not syncing"**
- Check GameMessage serialization; ensure fields are included
- Verify server is broadcasting to all clients
- Check network thread is receiving messages properly

**Performance lag**
- Reduce number of players
- Increase send frequency (lower = more updates = more lag)
- Use smaller data types if precision allows
