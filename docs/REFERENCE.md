# Codebase Reference

## File Locations

### Entry Points
- `src/main/java/com/kaiounet/GameServerApp.java` - Start the server
- `src/main/java/com/kaiounet/GameClientApp.java` - Start a client

### Core Game Logic
- `src/main/java/com/kaiounet/game/MultiplayerGame.java` - Main game class with update/render loops
- `src/main/java/com/kaiounet/game/Player.java` - Player entity

### Networking
- `src/main/java/com/kaiounet/network/GameClient.java` - Client networking (connect, send, receive)
- `src/main/java/com/kaiounet/network/GameServer.java` - Server logic (listen, broadcast, manage connections)
- `src/main/java/com/kaiounet/network/GameMessage.java` - Network message format

### Configuration
- `build.gradle.kts` - Build configuration and dependencies
- `.env` and `.env.example` - Environment variables (if used)

## Key Methods

### GameServerApp
```java
public static void main(String[] args)  // Entry point; creates and starts server
```

### GameClientApp
```java
public static void main(String[] args)  // Entry point; creates and starts client
```

### GameServer
```java
public GameServer(int port)                          // Constructor; sets port
public void start()                                  // Starts listening for connections
public void broadcastMessage(GameMessage msg)        // Sends message to all connected clients
private void handleClient(Socket socket)             // Manages individual client connection
```

### GameClient
```java
public GameClient(String host, int port)            // Constructor; sets server address
public void connect()                                // Establishes TCP connection to server
public void sendMessage(GameMessage msg)             // Sends message to server
public void startReceivingThread()                   // Starts thread to receive messages
public GameMessage receiveMessage()                  // Receives one message (blocking)
private void messageReceived(GameMessage msg)        // Callback when message arrives
```

### MultiplayerGame
```java
public void update(float deltaTime)                  // Updates game state
public void render()                                 // Renders game graphics
public void handleInput()                            // Processes player input
public void addPlayer(Player player)                 // Adds player to game
public void removePlayer(int playerId)               // Removes player from game
public Player getLocalPlayer()                       // Gets the client's own player
```

### Player
```java
public void update(float deltaTime)                  // Updates position based on velocity
public void setVelocity(float vx, float vy)          // Sets movement direction
public void render()                                 // Draws player on screen
public float getX(), getY()                          // Gets position
public void setPosition(float x, float y)            // Sets position
```

### GameMessage
```java
public GameMessage()                                 // No-arg constructor for serialization
public enum MessageType { ... }                      // Message type enum
// Fields: type, playerId, x, y, colorR, colorG, colorB
```

## Build System

### Gradle Tasks

```bash
./gradlew build              # Compile and build project
./gradlew clean              # Remove build artifacts
./gradlew run --main-class com.kaiounet.GameServerApp   # Run server
./gradlew run --main-class com.kaiounet.GameClientApp   # Run client
./gradlew dependencies       # Show all dependencies
```

### Dependencies (in build.gradle.kts)

```kotlin
implementation("com.github.raylib-java:raylib-java-core:1.0.0")  // Raylib graphics
implementation("com.github.raylib-java:raylib-java-natives-linux:1.0.0")  // Linux support
// ... (other platform-specific natives)
```

## Game Loop (Client)

In `GameClientApp.main()`:

```
1. Create game window (800x600)
2. Connect to server
3. Create MultiplayerGame instance
4. Loop while window is open:
   a. Calculate deltaTime
   b. Call multiplayerGame.handleInput()
   c. Call multiplayerGame.update(deltaTime)
   d. Call multiplayerGame.render()
```

## Game Loop (Server)

In `GameServerApp.main()`:

```
1. Create GameServer on port 5555
2. Call server.start()
3. For each client connection:
   a. Create thread to handle that client
   b. In thread: read messages, broadcast to all clients
```

## Player Lifecycle

### On Server
1. Client connects
2. Server creates new Player with unique ID
3. Server broadcasts PLAYER_JOINED to all clients
4. Server receives PLAYER_MOVED messages, broadcasts to all clients
5. Client disconnects
6. Server broadcasts PLAYER_LEFT, removes from list

### On Client
1. Receive PLAYER_JOINED message
2. Add player to local game state
3. Receive PLAYER_MOVED messages
4. Update player position
5. Receive PLAYER_LEFT message
6. Remove player from game state

## Network Protocol Details

### Message Format (in GameMessage)
```
[Object Header]
[MessageType enum] (4 bytes)
[playerId] (int, 4 bytes)
[x] (float, 4 bytes)
[y] (float, 4 bytes)
[colorR, colorG, colorB] (int, 12 bytes)
```

### Message Flow (Per Frame)

**Client → Server:**
- Every frame, send PLAYER_MOVED with current position

**Server → All Clients:**
- Broadcast each received PLAYER_MOVED to all other clients
- Broadcast PLAYER_JOINED when new client connects
- Broadcast PLAYER_LEFT when client disconnects

### Connection Lifecycle
```
Client connects → ObjectOutputStream created → ObjectInputStream created
                  ↓
               Server creates handler thread
                  ↓
               Client sends PLAYER_JOINED response
                  ↓
               Game loop: Client sends positions, Server broadcasts
                  ↓
               Client disconnects → Socket closed → Handler thread exits
```

## Raylib Common Functions Used

In `MultiplayerGame.render()`:

```java
ClearBackground(Color.WHITE)                        // Clear screen
DrawRectangleRec(rectangle, Color.RED)              // Draw filled rectangle
DrawCircle(x, y, radius, Color.BLUE)                // Draw filled circle
DrawText("Hello", x, y, size, Color.BLACK)          // Draw text
IsKeyPressed(KEY_W)                                  // Check if key pressed
IsKeyDown(KEY_W)                                     // Check if key held
GetMouseX(), GetMouseY()                            // Get mouse position
IsMouseButtonPressed(MOUSE_LEFT_BUTTON)             // Check mouse click
```

## Configuration

### Server Configuration
- Port: Default 5555 (modify in `GameServerApp`)
- Max players: Unlimited (server handles dynamically)
- Update frequency: Every message received (real-time)

### Client Configuration
- Server address: Default localhost (modify in `GameClientApp`)
- Server port: Default 5555 (match server setting)
- Window size: 800x600 (modify in `GameClientApp`)
- FPS: 60 (Raylib default)

### Player Configuration
- Speed: 150 pixels/second (modify in `Player.java`)
- Size: Render as rectangle (modify in `Player.render()`)
- Colors: Auto-assigned per player (modify assignment in `GameServer`)

## Extending the Codebase

For detailed extension examples, see `docs/EXTENDING.md`.

Common tasks:
- Add new game features → Modify `MultiplayerGame.java`
- Change network protocol → Modify `GameMessage.java` and handlers
- Add new player types → Create subclass of `Player`
- Add new game modes → Add to `GameMessage` and implement in game logic
- Optimize performance → Use object pooling, reduce message frequency, compress data
