# ServerTap

ServerTap is a REST API for Bukkit, Spigot, and PaperMC Minecraft servers. It
allows for server admins to query and interact with their servers using
simple REST semantics.

This plugin is under development and is **not ready for real usage yet**.

**But** if you're feeling brave, head over to https://github.com/phybros/servertap/releases to grab the latest and greatest plugin JAR.

# Discord

Join the Discord to talk about this plugin https://discord.gg/tG4AEt7

# Usage

Install this plugin by dropping the jar into the `plugins/` directory on your
server.

Then, you can query the server using `curl` or Postman, or anything that speaks
HTTP.

For example, query for information about the server itself:

```bash
$ curl http://localhost:4567/v1/server

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

# Developing

You need a few things to get started

- An IDE (e.g. IntelliJ)
- JDK 8
- Maven

Then, you can build the plugin `jar` by using the `mvn package` command.
