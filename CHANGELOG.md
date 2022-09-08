# Changelog

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
