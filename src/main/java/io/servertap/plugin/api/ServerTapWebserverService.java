package io.servertap.plugin.api;

import io.javalin.Javalin;
import io.javalin.http.Handler;
import io.javalin.websocket.WsConfig;

import java.util.function.Consumer;

/**
 * API for the ServerTap Webserver.
 */
public interface ServerTapWebserverService {

    /**
     * Get the Javalin Webserver Instance.<br>
     * Use with caution, as this gives full access to the Webserver and might break ServerTaps functionality.
     *
     * @return the Javalin Webserver Instance
     */
    Javalin getWebserver();

    /**
     * Adds a GET Request Handler for the specified Path to the Webserver.
     *
     * @param path The Path to handle
     * @param handler The Handler
     */
    void get(String path, Handler handler);

    /**
     * Adds a POST Request Handler for the specified Path to the Webserver.
     *
     * @param path The Path to handle
     * @param handler The Handler
     */
    void post(String path, Handler handler);

    /**
     * Adds a PUT Request Handler for the specified Path to the Webserver.
     *
     * @param path The Path to handle
     * @param handler The Handler
     */
    void put(String path, Handler handler);

    /**
     * Adds a DELETE Request Handler for the specified Path to the Webserver.
     *
     * @param path The Path to handle
     * @param handler The Handler
     */
    void delete(String path, Handler handler);

    /**
     * Adds a WebSocket Handler on the specified Path to the Webserver.
     *
     * @param path The Path to handle
     * @param socket The Websocket Configuration
     */
    void websocket(String path, Consumer<WsConfig> socket);
}
