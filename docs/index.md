# ServerTap

ServerTap is a REST API for Bukkit, Spigot, and PaperMC Minecraft servers. It
allows for server admins to query and interact with their servers using
simple REST semantics.

Head over to https://github.com/servertap-io/servertap/releases/latest to grab the latest and greatest plugin JAR.

# Discord

Join the Discord to talk about this plugin https://discord.gg/nSWRYzBMfp

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

# TLS

ServerTap supports TLS (a.k.a. SSL) via a Java "keystore" file. You can generate a keystore for yourself using the `keytool` utility
that ships with Java.

Using TLS is **highly recommended** as it encrypts the requests and responses to/from your server on the wire.

Example:

```
keytool -genkey -keyalg RSA -alias servertap -keystore selfsigned.jks -validity 365 -keysize 2048

Enter keystore password:
Re-enter new password:
What is your first and last name?
  [Unknown]:
...
<you can mostly answer whatever you want to all these questions>
```

Make sure to save the output file `selfsigned.jks` into the `plugins/ServerTap` directory

Then in `config.yml`:

```yaml
tls:
  enabled: true
  keystore: selfsigned.jks
  keystorePassword: testing
```

Then make sure to use `https://` when talking to the API.

## SNI

TLS optionally supports Server Name Indication (SNI) since `v0.5.0`. Set `tls.sni` to `true` in your config to enable it
(expert). 99.9% of users won't need to think about this option and can just leave it `false`.

# Authentication

Authentication is very rudimentary at this point. Add this to your `plugins/ServerTap/config.yml` file:

```yaml
useKeyAuth: true
key: some-long-super-random-string
```

Then include a Header called `key` with your specified key on every request to Authenticate.

We need help making this better! See https://github.com/servertap-io/servertap/issues/5 for more info.

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

# Websockets

ServerTap has a bi-directional websockets interface which allows you to
receive server log lines in realtime (no polling!).

Once connected, any server log line that goes through the normal logging
filters on the server will come down the websocket in a JSON object like
this:

```json
{
  "message": "§6/version: §fGets the version of this server including any plugins in use",
  "timestampMillis": 1631834015918,
  "loggerName": "",
  "level": "INFO"
}
```

Note: you can use a library like
[ansicolors](https://www.npmjs.com/package/ansicolor) to parse the color
codes for the browser.

Connect to `ws://<host>:4567/v1/ws/console` (or use `wss://` if you
have [TLS](#tls) enabled). The last 1000 server log messages will be sent
to the connecting client. You can configure  the size of the server log
buffer by changing `websocketConsoleBuffer` in `config.yml`.

You can also send commands through the WS connection and they will be
executed on the server.

!!! warning
    If you don't have authentication enabled, you are basically opening a remote admin console to your server up to the
    internet (bad idea).

### Authenticating Websockets

Since you can't set headers on websocket connections, you can't use the
header `key` to authenticate like you can with regular API routes.

Instead you must set a cookie called `x-servertap-key` on the page hosting
the websocket connection.

Example:

```js
// set cookie to authenticate the connection
document.cookie = "x-servertap-key=change_me";

this.ws = new WebSocket("ws://localhost:4567/v1/ws/console");

this.ws.onopen = function() {
  console.log("Opened connection");
};
```

### Note: If you don't have authentication enabled, you are basically opening a remote admin console to your server up to the internet (bad idea).

# Contributing to ServerTap

You need a few things to get started

- An IDE (e.g. IntelliJ)
- JDK 19
- Maven

Then, you can build the plugin `jar` by using the `mvn package` command.
