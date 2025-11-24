# Network Setup Guide - Multiplayer Shooting Game

## Quick Start

### Local Testing (Default)
```bash
# Terminal 1 - Start Server
java -cp build/libs/Jaylib101-1.0-SNAPSHOT.jar com.kaiounet.GameServerApp

# Terminal 2+ - Start Clients
java -cp build/libs/Jaylib101-1.0-SNAPSHOT.jar com.kaiounet.GameClientApp
```

### Network Play (Same Network)

**On Server PC:**
```bash
# First, find your IP address
hostname -I    # Linux
ipconfig       # Windows
ifconfig       # Mac

# Run server with your IP
java -cp build/libs/Jaylib101-1.0-SNAPSHOT.jar com.kaiounet.GameServerApp 192.168.1.100 5555
```

**On Client PC(s):**
```bash
java -cp build/libs/Jaylib101-1.0-SNAPSHOT.jar com.kaiounet.GameClientApp 192.168.1.100 5555
```

---

## Server Configuration

### Default (Listen on all interfaces, port 5555)
```bash
java -cp build/libs/Jaylib101-1.0-SNAPSHOT.jar com.kaiounet.GameServerApp
```

### Custom Port
```bash
java -cp build/libs/Jaylib101-1.0-SNAPSHOT.jar com.kaiounet.GameServerApp 9999
```

### Specific IP Address
```bash
java -cp build/libs/Jaylib101-1.0-SNAPSHOT.jar com.kaiounet.GameServerApp 192.168.1.100
```

### Specific IP and Port
```bash
java -cp build/libs/Jaylib101-1.0-SNAPSHOT.jar com.kaiounet.GameServerApp 192.168.1.100 5555
```

### Localhost Only (Local testing only)
```bash
java -cp build/libs/Jaylib101-1.0-SNAPSHOT.jar com.kaiounet.GameServerApp 127.0.0.1 5555
```

---

## Client Configuration

### Default (Connect to localhost, port 5555)
```bash
java -cp build/libs/Jaylib101-1.0-SNAPSHOT.jar com.kaiounet.GameClientApp
```

### Custom Server Address and Port
```bash
java -cp build/libs/Jaylib101-1.0-SNAPSHOT.jar com.kaiounet.GameClientApp 192.168.1.100 5555
```

### Using Hostname
```bash
java -cp build/libs/Jaylib101-1.0-SNAPSHOT.jar com.kaiounet.GameClientApp game.example.com 5555
```

---

## Finding Your IP Address

### Linux
```bash
hostname -I
# or
ifconfig | grep "inet " | grep -v 127.0.0.1
```

### Windows (Command Prompt)
```bash
ipconfig
```
Look for "IPv4 Address" under your network adapter.

### Windows (PowerShell)
```powershell
ipconfig | findstr "IPv4"
```

### Mac
```bash
ifconfig | grep "inet " | grep -v 127.0.0.1
```

---

## Understanding Network Addresses

| Address | Meaning | Use Case |
|---------|---------|----------|
| `0.0.0.0` | All interfaces | Server listening to all network adapters (default) |
| `127.0.0.1` | Localhost only | Local testing only, not accessible from network |
| `192.168.x.x` | Private IP | Local network play |
| `10.x.x.x` | Private IP | Local network play |
| `172.16-31.x.x` | Private IP | Local network play |

---

## Network Configuration Examples

### Example 1: Local Testing (Single Computer)
```bash
# Terminal 1
java -cp build/libs/Jaylib101-1.0-SNAPSHOT.jar com.kaiounet.GameServerApp

# Terminal 2
java -cp build/libs/Jaylib101-1.0-SNAPSHOT.jar com.kaiounet.GameClientApp

# Terminal 3
java -cp build/libs/Jaylib101-1.0-SNAPSHOT.jar com.kaiounet.GameClientApp
```

### Example 2: Home Network (2+ Computers)

**Computer A (Server - IP: 192.168.1.100):**
```bash
java -cp build/libs/Jaylib101-1.0-SNAPSHOT.jar com.kaiounet.GameServerApp 192.168.1.100 5555
```

**Computer B (Client):**
```bash
java -cp build/libs/Jaylib101-1.0-SNAPSHOT.jar com.kaiounet.GameClientApp 192.168.1.100 5555
```

**Computer C (Client):**
```bash
java -cp build/libs/Jaylib101-1.0-SNAPSHOT.jar com.kaiounet.GameClientApp 192.168.1.100 5555
```

### Example 3: Custom Port (Multiple Games on Same Network)

**Game 1 Server:**
```bash
java -cp build/libs/Jaylib101-1.0-SNAPSHOT.jar com.kaiounet.GameServerApp 192.168.1.100 5555
```

**Game 1 Clients:**
```bash
java -cp build/libs/Jaylib101-1.0-SNAPSHOT.jar com.kaiounet.GameClientApp 192.168.1.100 5555
```

**Game 2 Server:**
```bash
java -cp build/libs/Jaylib101-1.0-SNAPSHOT.jar com.kaiounet.GameServerApp 192.168.1.100 5556
```

**Game 2 Clients:**
```bash
java -cp build/libs/Jaylib101-1.0-SNAPSHOT.jar com.kaiounet.GameClientApp 192.168.1.100 5556
```

---

## Troubleshooting

### Server Starts But Clients Can't Connect

1. **Check firewall**
   - Windows: Add Java to Windows Firewall exceptions
   - Linux: `sudo ufw allow 5555`
   - Mac: System Preferences → Security & Privacy → Firewall

2. **Verify server is listening**
   ```bash
   netstat -an | grep 5555  # Linux/Mac
   netstat -an | findstr 5555  # Windows
   ```

3. **Check IP address is correct**
   - Make sure you're using the correct IP address
   - Not 127.0.0.1 for network play
   - Not 0.0.0.0 as client address

4. **Verify network connectivity**
   ```bash
   ping 192.168.1.100  # Test if you can reach server
   ```

### Port Already in Use

If port 5555 is already in use:
```bash
# Server
java -cp build/libs/Jaylib101-1.0-SNAPSHOT.jar com.kaiounet.GameServerApp 5556

# Client
java -cp build/libs/Jaylib101-1.0-SNAPSHOT.jar com.kaiounet.GameClientApp localhost 5556
```

### Connection Refused Error

Make sure:
1. Server is running first
2. Client is using correct IP address
3. Client is using correct port
4. Firewall is not blocking the port

---

## Server Console Output

When the server starts, you'll see:
```
╔════════════════════════════════════════╗
║         MULTIPLAYER GAME SERVER        ║
╠════════════════════════════════════════╣
║ Server Address: 192.168.1.100          ║
║ Port: 5555                             ║
║ Listening: 0.0.0.0:5555                ║
╠════════════════════════════════════════╣
║ Waiting for clients...                 ║
║ Press Ctrl+C to stop                   ║
╚════════════════════════════════════════╝
```

---

## Client Console Output

When a client connects, you'll see:
```
╔════════════════════════════════════════╗
║      MULTIPLAYER GAME CLIENT           ║
╠════════════════════════════════════════╣
║ Connecting to: 192.168.1.100           ║
║ Port: 5555                             ║
╚════════════════════════════════════════╝
✓ Connected to server!
Starting game...
```

---

## Backward Compatibility

The server maintains full backward compatibility:

```bash
# Old command (still works)
java -cp build/libs/Jaylib101-1.0-SNAPSHOT.jar com.kaiounet.GameServerApp 5555

# New commands (also work)
java -cp build/libs/Jaylib101-1.0-SNAPSHOT.jar com.kaiounet.GameServerApp 192.168.1.100
java -cp build/libs/Jaylib101-1.0-SNAPSHOT.jar com.kaiounet.GameServerApp 192.168.1.100 5555
```

---

## Tips

- **Use hostname if available**: If your network has DNS, use hostnames instead of IPs
- **Port forwarding**: For internet play, configure port forwarding on your router
- **Test locally first**: Always test on localhost before attempting network play
- **Firewall**: Make sure port 5555 (or your custom port) is open in firewall

---

## Architecture

### Server Architecture
- **Bind Address**: `0.0.0.0` (all interfaces) by default
- **Bind Port**: `5555` by default
- **Can accept**: Connections from any network interface
- **Maintains**: Authoritative game state

### Client Architecture
- **Connect to**: User-specified server address
- **Connect port**: User-specified port (5555 default)
- **Supports**: IP addresses, hostnames, localhost
- **Sends**: Movement, weapon fire, and state updates

---

## Performance Notes

- Network bandwidth: ~1-2 KB/s per player
- Latency: Works best with < 200ms ping
- Supports: 10+ simultaneous players
- Update rate: 60 FPS client-side, synced server updates

