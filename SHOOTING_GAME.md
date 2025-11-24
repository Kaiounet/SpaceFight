# Open-World Shooting Game - Multiplayer

An open-world 1v1 (or many) shooting game where players battle each other with beam weapons, health systems, and a scoreboard.

## Features

### Gameplay
- **Movement**: Arrow Keys or WASD to move around the map
- **Shooting**: Press SPACE to shoot a beam (aim with mouse)
- **Health System**: Each player has 100 HP
- **Damage**: Each beam does 10 damage
- **Killing**: When a player's HP reaches 0, they respawn with full health and the killer gets +10 score
- **Scoreboard**: Real-time scoreboard showing all players' scores, updated in real-time

### Game Mechanics
- Players are colored squares
- Beams are small projectiles that travel in the direction you aimed
- Health is displayed above each player as a color-coded bar (green = high, yellow = medium, red = low)
- Scoreboard displays players ranked by kills
- **Multiplayer Synchronization**: All hits, deaths, and scores are synced across all players

### Network Protocol
- Server maintains authoritative player state (health, score, position)
- New messages added:
  - `BEAM_FIRE`: Broadcast when a player shoots
  - `PLAYER_HIT`: Sent when a beam hits a player (includes damage, new health, shooter's new score)
  - `PLAYER_RESPAWN`: Sent when a player dies and respawns

## How to Play

### Start the Server
```bash
cd /home/kai/dev/Jaylib101
java -cp build/libs/Jaylib101-1.0-SNAPSHOT.jar com.kaiounet.GameServerApp 5555
```

### Start Clients (multiple terminals)
```bash
java -cp build/libs/Jaylib101-1.0-SNAPSHOT.jar com.kaiounet.GameClientApp localhost 5555
```

### Controls
- **WASD** or **Arrow Keys**: Move around
- **SPACE**: Shoot beam
- **Mouse**: Aim direction for beam
- Beams travel based on your mouse position when you fire

## Game Rules

1. Players start with 100 HP
2. Each player can move freely around the 1200x800 map
3. Beams are destroyed after hitting a player or leaving the map
4. When a player takes damage:
   - They lose 10 HP per beam hit
   - If HP reaches 0, they respawn at starting position with 100 HP
   - The killer gets +10 score
5. Scoreboard updates in real-time for all players
6. Game continues until server/clients disconnect

## Code Structure

### New Classes
- **Beam.java**: Represents a projectile with position, velocity, and collision detection
- Updated **Player.java**: Now includes health and score tracking
- Updated **GameMessage.java**: New message types for beams, hits, and respawns
- Updated **MultiplayerGame.java**: Shooting mechanics, hit detection, scoreboard rendering
- Updated **GameServer.java**: Handles new message types and maintains game state

### New Message Types
- `BEAM_FIRE`: Contains beam ID, position, velocity, shooter ID, color
- `PLAYER_HIT`: Contains target player, damage amount, new health, shooter's new score
- `PLAYER_RESPAWN`: Contains respawned player ID, new health, score

## Future Enhancements

- Different weapon types with different damage/speed
- Ammo system with pickups
- Power-ups (health regeneration, damage boost)
- Different game modes (capture the flag, team deathmatch)
- Map obstacles and cover
- Sound effects and visual effects
- Replay system
