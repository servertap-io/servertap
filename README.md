# ServerTap

<a href="https://github.com/servertap-io/servertap/actions/workflows/build.yml"><img alt="GitHub Workflow Status" src="https://img.shields.io/github/actions/workflow/status/servertap-io/servertap/build.yml?branch=main"></a>
<img alt="bukkit version %3E%3D 1.16" src="https://img.shields.io/badge/bukkit%20version-%3E%3D1.16-brightgreen">
<img alt="GitHub all releases" src="https://img.shields.io/github/downloads/servertap-io/servertap/total?color=brightgreen">
<a href="https://discord.gg/nSWRYzBMfp"><img src="https://img.shields.io/discord/919982507271802890?logo=discord&label=discord&color=brightgreen" alt="chat on Discord"></a>

ServerTap is a REST API for Bukkit, Spigot, and PaperMC Minecraft servers. It
allows for server admins to query and interact with their servers using
simple REST semantics.

Head over to https://github.com/servertap-io/servertap/releases/latest to grab the latest and greatest plugin JAR.

# Discord

Join the Discord to talk about this plugin https://discord.gg/nSWRYzBMfp

**Note:** If you have a question please post in the support forum on our discord instead of just asking in general chat.
This helps us keep track of issues and questions more effectively and answer your questions quicker.

# Contents

- [Usage](#usage)
- [ServerTap Command](#servertap-command)
- [Current Endpoints](#current-endpoints)
- [TLS](#tls)
- [Authentication](#authentication)
- [CORS](#cors)
- [Webhooks](#webhooks)
- [Websockets](#websockets)
  - [Authenticating Websockets](#authenticating-websockets)
- [Using the Developer API](#using-the-developer-api)
- [Contributing to ServerTap](#contributing-to-servertap)

# Usage

Install this plugin by dropping the jar into the `plugins/` directory on your
server. Then, you can query the server using `curl` or Postman, or anything that speaks
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
# ServerTap Command

ServerTap currently supports only one management command in game. The supported subcommands are `reload` & `info`
which as their names imply let you reload and display basic information about the plugin (version, author, etc).

**Note**: The Permission for the `/servertap` Command is `servertap.admin`.

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

# Using the Developer API

ServerTap provides a Developer API allowing you to register your own Request Handlers and Websockets from your Plugin!

To get ServerTap Builds you can use [Jitpack](https://jitpack.io). First, add the Jitpack Repository to your `pom.xml`:
```xml
<repository>
  <id>jitpack.io</id>
  <url>https://jitpack.io</url>
</repository>
```
Then you can add the following Dependency:
```xml
<dependency>
  <groupId>com.github.phybros</groupId>
  <artifactId>servertap</artifactId>
  <version>vX.X.X</version>
  <scope>provided</scope>
</dependency>
```
Replace the Version with the latest available Releases Version Number.

You can retrieve the API using Bukkits ServiceProvider:
```java
// In your Main Class extended from JavaPlugin, for example in the onEnable() Method
ServerTapWebserverService webserverService = this.getServer().getServicesManager().load(ServerTapWebserverService.class);
```

The Interface provides you with methods to directly add Endpoints to the Webserver:
```java
webserverService.get("/test/ping", ctx -> ctx.status(200).result("Pong!"));
webserverService.websocket("/test/ws", websocketConfig -> {
    websocketConfig.onMessage(wsMessageContext -> System.out.println(wsMessageContext.message()));
});
```
Your Endpoints (HTTP & WebSocket) are protected the same way all other Endpoints in the Server are.

The API provides the `getWebserver()` Method that will return the internal [Javalin](https://javalin.io) Instance.
This will give you deep access to the Webserver providing you every ability possible.
Be careful not to break ServerTaps Functionality (e.g. the AccessManager checking Security)!
Use this only if necessary.

# Contributing to ServerTap

You need a few things to get started

- An IDE (e.g. IntelliJ)
- JDK 19
- Maven

Then, you can build the plugin `jar` by using the `mvn package` command.
