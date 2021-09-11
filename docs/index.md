---
title: Welcome to ServerTap
---

ServerTap is a REST API for Bukkit, Spigot, and PaperMC Minecraft servers. It
allows for server admins to query and interact with their servers using
simple REST semantics.

This plugin is under development and is **not ready for real usage yet**.

**But** if you're feeling brave, head over to https://github.com/phybros/servertap/releases to grab the latest and greatest plugin JAR.

# Discord

Join the Discord to talk about this plugin https://discord.gg/rhqXArkQ3U

# Usage

Install this plugin by dropping the jar into the `plugins/` directory on your
server.

Then, you can query the server using `curl` or Postman, or anything that speaks
HTTP.

For example, query for information about the server itself:

```bash
$ curl http://localhost:4567/v1/server
```

```json
{
  "name": "Paper",
  "motd": "This is my MOTD",
  "version": "git-Paper-89 (MC: 1.15.2)",
  "bukkitVersion": "1.15.2-R0.1-SNAPSHOT",
  "health": {
    "cpus": 4,
    "uptime": 744,
    "totalMemory": 2010644480,
    "maxMemory": 2010644480,
    "freeMemory": 1332389360
  },
  "bannedIps": [],
  "bannedPlayers": [
    {
      "target": "phybros",
      "source": "Server"
    }
  ]
}
```

Or get a list of players that are currently online:

```bash
$ curl http://localhost:4567/v1/players
```

```json
[
  {
    "uuid": "55f584e4-f095-48e0-bb8a-eb5c87ffe494",
    "displayName": "phybros",
    "address": "localhost",
    "port": 58529,
    "exhaustion": 3.5640976,
    "exp": 0.45454547,
    "whitelisted": false,
    "banned": false,
    "op": true
  }
]
```

# Current Endpoints

This plugin self-hosts its own API documentation using Swagger.
You can see the full API documentation at http://your-server.net:4567/swagger.
You can even explore and test the API right from the UI!

>Note: there is a known issue that causes the OpenApi plugin to spew
>tons of logs into your server log. See https://github.com/phybros/servertap/issues/60 for details.

Some examples of capabilities are:

- Ping
- Server
  - Get/Add/Remove Ops
  - Get/Add Whitelist
- Get/Save/List Worlds
- List/Read Scoreboard(s) 
- Broadcast
- List/Find Players
- Get/Pay/Debt Economy (W/ Plugin)
- List Plugins 

# Authentication

Authentication is very rudimentary at this point. Add this to your `plugins/ServerTap/config.yml` file:

```yaml
useKeyAuth: true
key: some-long-super-random-string
```

Then include a Header called `key` with your specified key on every request to Authenticate.

We need help making this better! See https://github.com/phybros/servertap/issues/5 for more info.

# CORS

By default, ServerTap allows cross-origin requests from any origin (`*`). To change this, change the `corsOrigins`
setting in the config file.

Example:

```yaml
corsOrigins:
  - https://mysite.com
```

The setting supports as many origins as you want, just add them to the array.

# Webhooks

ServerTap can send webhook messages in response to events on the server.

To use webhooks, just define them in your `plugins/ServerTap/config.yml` file like so:

```yaml
webhooks:
  default:
    listener: "https://your-webhook-target.com/whatever"
    events:
    - PlayerJoin
    - PlayerQuit
```

The webhook requests are `POST` containing a simple JSON payload:

```json
{
  "player": {
    "uuid": "55f584e4-f095-48e0-bb8a-eb5c87ffe494",
    "displayName": "phybros",
    "address": "localhost",
    "port": 52809,
    "exhaustion": 0,
    "exp": 0.5714286,
    "whitelisted": true,
    "banned": false,
    "op": true
  },
  "joinMessage": "§ephybros joined the game",
  "eventType": "PlayerJoin"
}
```

The available events are currently:

 * `PlayerJoin`
 * `PlayerDeath`
 * `PlayerChat`
 * `PlayerKick`
 * `PlayerQuit`

# Developing

You need a few things to get started

- An IDE (e.g. IntelliJ)
- JDK 8 (soon to be 11)
- Maven

Then, you can build the plugin `jar` by using the `mvn package` command.
