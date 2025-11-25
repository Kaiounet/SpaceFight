# Getting Started

## Quick Start (5 minutes)

### Prerequisites
- Java 11+
- Gradle (provided via gradlew)

### Run the Game

Open 3 terminal windows in the project root:

**Terminal 1 - Start Server:**
```bash
./gradlew build
./gradlew run --main-class com.kaiounet.GameServerApp
```
Wait for: `Server started on port 5555`

**Terminal 2 - Start Client 1:**
```bash
./gradlew run --main-class com.kaiounet.GameClientApp
```
Wait for game window to appear.

**Terminal 3 - Start Client 2:**
```bash
./gradlew run --main-class com.kaiounet.GameClientApp
```

### Controls
- **Arrow Keys** or **WASD** - Move player
- **Mouse** - Aim and shoot (if shooting mode enabled)

## Troubleshooting

**Port already in use:**
Edit `src/main/java/com/kaiounet/GameServerApp.java` and change the port number (default: 5555).

**Can't connect:**
Ensure server is running first, then start clients. Check that firewall isn't blocking port 5555.

**Game won't build:**
Run `./gradlew clean build` to rebuild from scratch.
