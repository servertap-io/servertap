# Changelog

## v0.6.1

### ⚠️ BREAKING CHANGES ⚠️

* Lowest supported version of Minecraft/Bukkit/Paper/Spigot is now `1.18.2`
* ServerTap is no longer supported on `1.16.5`
* ServerTap `v0.5.3` is now the last version that supports `1.16.5`

### New Features

* We have a new developer API! This means you can create plugins that extend servertap's functionality while taking
  advantage of all the boilerplate that ServerTap provides (like auth, routing, security etc). Read more about it [in the README!](https://github.com/servertap-io/servertap#using-the-developer-api). Thanks to [@Velyn-N](https://github.com/Velyn-N) for this awesome feature.
* There is a new `/servertap reload` command which can be used to reload ServerTap's config on the fly

### Internals/Bugfixes

* The entire internal structure of the API has been refactored ([@Velyn-N](https://github.com/Velyn-N) & [@srmullaney](https://github.com/srmullaney))
* Updated ServerTap to be compatible with Paper `1.20+` ([@TimeCodings](https://github.com/TimeCodings))
* ServerTap now lives in its own org at <https://github.com/servertap-io/servertap>

## v0.5.3

### New Features

* You can now disable swagger by setting `disable-swagger` to `true`
  in your config (default false)
* You can now block individual paths in your config (thanks [@Velyn-N](https://github.com/Velyn-N))

### Internals/Bugfixes

* Fixed errant reverse lookup on player join (fixes #68)

## v0.5.2

### New Features

* New endpoint: `DELETE /v1/server/whitelist` to remove someone from the
  whitelist

### Internals/Bugfixes

* Updated to preserve compatibility with 1.16.5 / Java 16

## v0.5.1

### Internals/Bugfixes

* v0.5.0 was not backwards compatible and **required** Java 19, my bad.
  Now works with Java 17 thru 20.
* Upgraded `item-nbt-api-plugin` to latest

## v0.5.0

### ⚠️ BREAKING CHANGES ⚠️

* Gamemode, Environment/Dimension, and Difficulty now use their native
  Bukkit names
  * e.g. in `/v1/players` instead of returning `0` for difficulty it will
    now return `SURVIVAL`.
* Now compiled with ☕️ **Java 19**

### New Features

* TLS now optionally supports Server Name Indication (SNI)
  * Set `tls.sni` to `true` in your config to enable it (expert)
* Player is now included in the `PlayerChatWebhookEvent` request body (#194)

### Internals/Bugfixes

* ServerTap is now using Javalin v5!
* Fixed OpenApi spewing tons of WARNs into logs when viewing `/swagger` (#60)
* Updated versions of javalin, slf4j, unirest, jackson-databind, junit
  dependencies.
* No longer messes with ClassLoader on startup

## v0.4.0

### New Features

* Added new route to get all available advancements `GET /v1/advancements`

### Internals/Bugfixes

* Fixed buggy interaction between Vault API and Swagger/OpenApi (#175)

## v0.3.1

### Internals/Bugfixes

* Fixed bug with the `/economy` routes not working (#173)
* Updated versions of javalin, junit, jackson-databind, and unirest

## v0.3.0

### New Features

* You can now install plugins by `POST`ing their URLs to `/v1/plugins`

### API Changes

* ServerTap is now built with JDK 17
* ServerTap now requres Spigot/Bukkit/Paper `1.16` and above
* `GET /v1/worlds/download` and friends now produce `.tar.gz` files instead of `.zip` files (#118)
* New attributes added to Server: `maxPlayers`, `onlinePlayers`
* New attributes added to Player: `lastPlayed`
* New attributes added to Plugin: `website`, `depends`, `softDepends`, `apiVersion`
* New attributes added to ConsoleLine: `level`

### Internals/Bugfixes

* Renamed `master` branch to `main`
* Fixed a bug in `GET /v1/server/ops` where it would ignore ops who have never joined th server
* Added base framework for unit testing
* Imported the whole `Metrics` class to enable unit testing
* Added some basic unit tests

## v0.2.0

### New Features

* You can now download a .zip of your world from /v1/worlds/{uuid}/download or all worlds at `/v1/worlds/download` (by @matteoturini)
* ServerTap now loads in the STARTUP phase

### Internals/Bugfixes

* Upgrade Javalin from v3 to v4
* Fix bug where log4j was being included in the shaded jar
