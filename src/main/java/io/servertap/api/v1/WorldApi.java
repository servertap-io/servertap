package io.servertap.api.v1;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.InternalServerErrorResponse;
import io.javalin.http.NotFoundResponse;
import io.javalin.plugin.openapi.annotations.*;
import io.servertap.Constants;
import io.servertap.api.v1.models.World;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.World.Environment;
import org.bukkit.plugin.Plugin;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class WorldApi {

    private static final Logger log = Bukkit.getLogger();

    @OpenApi(
            path = "/v1/worlds/save",
            summary = "Triggers a world save of all worlds",
            operationId = "saveAllWorlds",
            method = HttpMethod.POST,
            tags = {"Server"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            responses = {
                    @OpenApiResponse(status = "200")
            }
    )
    public static void saveAllWorlds(Context ctx) {
        org.bukkit.Server bukkitServer = Bukkit.getServer();

        Plugin pluginInstance = bukkitServer.getPluginManager().getPlugin("ServerTap");

        if (pluginInstance != null) {
            // Run the saves on the main thread, can't use sync methods from here otherwise
            bukkitServer.getScheduler().scheduleSyncDelayedTask(pluginInstance, () -> {
                for (org.bukkit.World world : Bukkit.getWorlds()) {
                    try {
                        world.save();
                    } catch (Exception e) {
                        // Just warn about the issue
                        log.warning(String.format("Couldn't save World %s %s", world.getName(), e.getMessage()));
                    }
                }
            });
        }

        ctx.json("success");
    }

    @OpenApi(
            path = "/v1/worlds/{uuid}/save",
            summary = "Triggers a world save",
            method = HttpMethod.POST,
            operationId = "saveWorld",
            tags = {"Server"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            pathParams = {
                    @OpenApiParam(name = "uuid", description = "The UUID of the World to save")
            },
            responses = {
                    @OpenApiResponse(status = "200")
            }
    )
    public static void saveWorld(Context ctx) {
        org.bukkit.Server bukkitServer = Bukkit.getServer();

        UUID worldUUID = ValidationUtils.safeUUID(ctx.pathParam("uuid"));
        if (worldUUID == null) {
            throw new BadRequestResponse(Constants.INVALID_UUID);
        }

        org.bukkit.World world = bukkitServer.getWorld(worldUUID);

        if (world != null) {
            Plugin pluginInstance = bukkitServer.getPluginManager().getPlugin("ServerTap");

            if (pluginInstance != null) {
                // Run the saves on the main thread, can't use sync methods from here otherwise
                bukkitServer.getScheduler().scheduleSyncDelayedTask(pluginInstance, () -> {

                    try {
                        world.save();
                    } catch (Exception e) {
                        // Just warn about the issue
                        log.warning(String.format("Couldn't save World %s %s", world.getName(), e.getMessage()));
                    }
                });
            }
        }

        ctx.json("success");
    }

    private static void addFolderToTarGz(File folder, TarArchiveOutputStream tar, String baseName, String rootName) throws IOException {
        File[] files = folder.listFiles();
        assert files != null;
        for (File file : files) {
            if (file.isDirectory()) {
                if (rootName == null) {
                    addFolderToTarGz(file, tar, baseName, folder.getName());
                } else {
                    addFolderToTarGz(file, tar, baseName, rootName);
                }
            } else {
                String name = file.getAbsolutePath().substring(baseName.length())
                        // Trim first slash (absolute path)
                        .replaceFirst("^" + File.separator, "");
                // Join path and folder name
                if (rootName == null) {
                    name = folder.getName() + File.separator + name;
                } else {
                    name = rootName + File.separator + name;
                }

                TarArchiveEntry tarEntry = new TarArchiveEntry(file, name);
                tar.putArchiveEntry(tarEntry);

                FileInputStream in = new FileInputStream(file);
                IOUtils.copy(in, tar);
                in.close();

                tar.closeArchiveEntry();
            }
        }
    }

    @OpenApi(
            path = "/v1/worlds/{uuid}/download",
            summary = "Downloads a ZIP compressed archive of the world's folder",
            operationId = "downloadWorldZip",
            method = HttpMethod.GET,
            tags = {"Server"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            pathParams = {
                    @OpenApiParam(name = "uuid", description = "The UUID of the World to download")
            },
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(type = "application/zip")),
            }
    )
    public static void downloadWorld(Context ctx) throws IOException {
        org.bukkit.Server bukkitServer = Bukkit.getServer();

        UUID worldUUID = ValidationUtils.safeUUID(ctx.pathParam("uuid"));
        if (worldUUID == null) {
            throw new BadRequestResponse(Constants.INVALID_UUID);
        }

        org.bukkit.World world = bukkitServer.getWorld(worldUUID);

        if (world != null) {
            File folder = world.getWorldFolder();

            // Set headers for proper zip download
            ctx.header("Content-Disposition", "attachment; filename=\"" + folder.getName() + ".tar.gz\"");
            ctx.header("Content-Type", "application/zip");

            BufferedOutputStream buffOut = new BufferedOutputStream(ctx.res.getOutputStream());
            GzipCompressorOutputStream gzOut = new GzipCompressorOutputStream(buffOut);
            TarArchiveOutputStream tar = new TarArchiveOutputStream(gzOut);
            addFolderToTarGz(folder, tar, folder.getAbsolutePath(), null);
            tar.finish();
            tar.close();
            gzOut.close();
            buffOut.close();
        } else {
            throw new NotFoundResponse(Constants.WORLD_NOT_FOUND);
        }
    }

    @OpenApi(
            path = "/v1/worlds/download",
            summary = "Downloads a ZIP compressed archive of all the worlds' folders",
            operationId = "downloadAllWorldsZip",
            method = HttpMethod.GET,
            tags = {"Server"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(type = "application/zip")),
            }
    )
    public static void downloadWorlds(Context ctx) throws IOException {
        org.bukkit.Server bukkitServer = Bukkit.getServer();

        Plugin pluginInstance = bukkitServer.getPluginManager().getPlugin("ServerTap");

        ctx.header("Content-Disposition", "attachment; filename=\"worlds.tar.gz\"");
        ctx.header("Content-Type", "application/zip");

        BufferedOutputStream buffOut = new BufferedOutputStream(ctx.res.getOutputStream());
        GzipCompressorOutputStream gzOut = new GzipCompressorOutputStream(buffOut);
        TarArchiveOutputStream tar = new TarArchiveOutputStream(gzOut);

        for (org.bukkit.World world : Bukkit.getWorlds()) {
            try {

                File folder = world.getWorldFolder();

                addFolderToTarGz(folder, tar, folder.getAbsolutePath(), null);

            } catch (Exception e) {
                // Just warn about the issue
                log.warning(String.format("Couldn't save World %s %s", world.getName(), e.getMessage()));
                throw new InternalServerErrorResponse("Couldn't download world " + world.getName() + ": " + e.getMessage());
            }
        }
        tar.finish();
        tar.close();
        gzOut.close();
        buffOut.close();
    }

    @OpenApi(
            path = "/v1/worlds",
            summary = "Get information about all worlds",
            operationId = "getWorlds",
            tags = {"Server"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(from = World.class, isArray = true, type = "application/json"))
            }
    )
    public static void worldsGet(Context ctx) {
        List<World> worlds = new ArrayList<>();
        Bukkit.getServer().getWorlds().forEach(world -> worlds.add(fromBukkitWorld(world)));

        ctx.json(worlds);
    }

    @OpenApi(
            path = "/v1/worlds/{uuid}",
            summary = "Get information about a specific world",
            operationId = "getWorld",
            tags = {"Server"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            pathParams = {
                    @OpenApiParam(name = "uuid", description = "The uuid of the world")
            },
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(from = World.class, type = "application/json"))
            }
    )
    public static void worldGet(Context ctx) {

        UUID worldUUID = ValidationUtils.safeUUID(ctx.pathParam("uuid"));
        if (worldUUID == null) {
            throw new BadRequestResponse(Constants.INVALID_UUID);
        }

        org.bukkit.World bukkitWorld = Bukkit.getServer().getWorld(worldUUID);

        // 404 if no world found
        if (bukkitWorld == null) throw new NotFoundResponse();

        ctx.json(fromBukkitWorld(bukkitWorld));
    }

    private static World fromBukkitWorld(org.bukkit.World bukkitWorld) {
        World world = new World();

        world.setName(bukkitWorld.getName());
        world.setUuid(bukkitWorld.getUID().toString());
        world.setEnvironment(bukkitWorld.getEnvironment());
        world.setTime(BigDecimal.valueOf(bukkitWorld.getTime()));
        world.setAllowAnimals(bukkitWorld.getAllowAnimals());
        world.setAllowMonsters(bukkitWorld.getAllowMonsters());
        world.setGenerateStructures(bukkitWorld.canGenerateStructures());
        world.setDifficulty(bukkitWorld.getDifficulty());
        world.setSeed(BigDecimal.valueOf(bukkitWorld.getSeed()));
        world.setStorm(bukkitWorld.hasStorm());
        world.setThundering(bukkitWorld.isThundering());

        return world;
    }
}
