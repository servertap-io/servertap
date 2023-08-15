package io.servertap.api.v1;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.InternalServerErrorResponse;
import io.javalin.openapi.*;
import io.servertap.Constants;
import io.servertap.ServerTapMain;
import io.servertap.api.v1.models.Plugin;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Logger;

public class PluginApi {
    private final Logger log;
    private final ServerTapMain main;

    public PluginApi(ServerTapMain main, Logger log) {
        this.log = log;
        this.main = main;
    }

    @OpenApi(
            path = "/v1/plugins",
            methods = {HttpMethod.GET},
            summary = "Get a list of installed plugins",
            description = "Responds with an array of objects containing keys name and enabled.",
            tags = {"Plugins"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(type = "application/json"))
            }
    )
    public void listPlugins(Context ctx) {
        ArrayList<Plugin> pluginList = new ArrayList<>();
        for (org.bukkit.plugin.Plugin plugin : Bukkit.getPluginManager().getPlugins()) {

            io.servertap.api.v1.models.Plugin pl = new io.servertap.api.v1.models.Plugin();
            pl.setName(plugin.getName());
            pl.setEnabled(plugin.isEnabled());
            pl.setVersion(plugin.getDescription().getVersion());
            pl.setAuthors(plugin.getDescription().getAuthors());
            pl.setDescription(plugin.getDescription().getDescription());
            pl.setWebsite(plugin.getDescription().getWebsite());
            pl.setDepends(plugin.getDescription().getDepend());
            pl.setSoftDepends(plugin.getDescription().getSoftDepend());
            pl.setApiVersion(plugin.getDescription().getAPIVersion());

            pluginList.add(pl);
        }

        ctx.json(pluginList);
    }

    @OpenApi(
            path = "/v1/plugins",
            methods = {HttpMethod.POST},
            summary = "Download and install a plugin from a URL (URL MUST be urlencoded)",
            tags = {"Plugins"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            requestBody = @OpenApiRequestBody(
                    required = true,
                    content = {
                            @OpenApiContent(
                                    mimeType = "application/x-www-form-urlencoded",
                                    properties = {
                                            @OpenApiContentProperty(name = "downloadUrl", type = "string")
                                    }
                            )
                    }
            ),
            responses = {
                    @OpenApiResponse(status = "201", content = @OpenApiContent(type = "application/json"))
            }
    )
    public void installPlugin(Context ctx) {
        String stagingPath = main.getDataFolder().getPath() + File.separator + "downloads";
        File holdingArea = new File(stagingPath);
        URL url = null;

        // Validate the URL
        String stringUrl = ctx.formParam("downloadUrl");
        if (stringUrl != null && !stringUrl.isEmpty()) {
            try {
                url = new URL(stringUrl);
            } catch (MalformedURLException e) {
                log.warning(Constants.PLUGIN_INVALID_URL);
                throw new BadRequestResponse(Constants.PLUGIN_INVALID_URL);
            }
        }

        // Validate the storage area
        if (!holdingArea.exists()) {
            log.info("[ServerTap] Plugin downloads directory doesn't exist, trying to create");

            if (!holdingArea.mkdir()) {
                log.severe("[ServerTap] Could not create downloads directory!");
                throw new InternalServerErrorResponse("Could not create downloads directory!");
            }
        }

        try {
            if (url != null) {
                long startTime = System.currentTimeMillis();
                String downloadFileName = stagingPath + "/" + FilenameUtils.getName(url.getPath());
                File downloadedFile = new File(downloadFileName);
                FileUtils.copyURLToFile(url, downloadedFile);
                long elapsed = System.currentTimeMillis() - startTime;

                String msg = String.format("Downloaded plugin in %.2f seconds", elapsed / 1000.0);
                log.info("[ServerTap]" + msg);

                String targetFilename = main.getDataFolder().getAbsoluteFile().getParent() + File.separator + FilenameUtils.getName(url.getPath());
                boolean success = downloadedFile.renameTo(new File(targetFilename));
                if (!success) {
                    throw new InternalServerErrorResponse("Error moving plugin to plugins dir");
                }

                ctx.status(201).json(msg);
            }
        } catch (IOException e) {
            throw new InternalServerErrorResponse(String.format("Error downloading plugin: %s", e.getMessage()));
        }
    }
}
