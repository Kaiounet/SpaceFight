# Jaylib Multiplayer Game

A simple multiplayer game using Jaylib (Raylib Java port) with socket-based networking.

## Quick Links

- **[Getting Started](docs/GETTING_STARTED.md)** - How to run the game
- **[Architecture](docs/ARCHITECTURE.md)** - How the game works internally
- **[Extending the Game](docs/EXTENDING.md)** - How to add features and modify the codebase
- **[Codebase Reference](docs/REFERENCE.md)** - API reference and file locations

## Overview

This project demonstrates:
- Client-server architecture with TCP sockets
- Multi-threaded server handling multiple clients
- Real-time game state synchronization
- Graphics rendering using Raylib
- Network protocol design with Java serialization

## What's Included

- **Server:** Multi-threaded TCP server managing player connections and state
- **Client:** Graphics-based client using Raylib, synced with server state
- **Game Logic:** Simple real-time multiplayer environment with movement and rendering

## Requirements

- Java 11+
- Gradle (included via gradlew)

Start with [Getting Started](docs/GETTING_STARTED.md).
