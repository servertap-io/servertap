package io.servertap;

import com.google.gson.Gson;
import io.javalin.Javalin;
import io.javalin.community.ssl.SSLPlugin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.*;
import io.javalin.openapi.BasicAuth;
import io.javalin.openapi.BearerAuth;
import io.javalin.openapi.SecurityScheme;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.OpenApiPluginConfiguration;
import io.javalin.openapi.plugin.swagger.SwaggerConfiguration;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import io.javalin.security.RouteRole;
import io.javalin.websocket.WsConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.servertap.utils.GsonJsonMapper;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Logger;

import javax.crypto.spec.SecretKeySpec;

public class WebServer {

    public static final String SERVERTAP_KEY_HEADER = "Authorization";
    public static final String SERVERTAP_KEY_COOKIE = "x-servertap-authorization";
    private static final String[] noAuthPaths = new String[]{"/swagger", "/swagger-docs", "/webjars", "/v1/login"};

    private final Logger log;
    private final Javalin javalin;

    private final boolean isDebug;
    private final Object blockedPaths;
    private final boolean isAuthEnabled;
    private final boolean disableSwagger;
    private final boolean tlsEnabled;
    private final boolean sni;
    private final String keyStorePath;
    private final String keyStorePassword;
    private static String authKey = "";
    private final List<String> corsOrigin;
    private final int securePort;
    private static List<Map<String, String>> users;

    public WebServer(ServerTapMain main, FileConfiguration bukkitConfig, Logger logger) {
        this.log = logger;

        this.isDebug = bukkitConfig.getBoolean("debug", false);
        this.blockedPaths = bukkitConfig.isConfigurationSection("blocked-paths") ? bukkitConfig.getConfigurationSection("blocked-paths") : bukkitConfig.getStringList("blocked-paths");
        this.isAuthEnabled = bukkitConfig.getBoolean("enableAuth", true);
        this.disableSwagger = bukkitConfig.getBoolean("disable-swagger", false);
        this.tlsEnabled = bukkitConfig.getBoolean("tls.enabled", false);
        this.sni = bukkitConfig.getBoolean("tls.sni", false);
        this.keyStorePath = bukkitConfig.getString("tls.keystore", "keystore.jks");
        this.keyStorePassword = bukkitConfig.getString("tls.keystorePassword", "");
        authKey = bukkitConfig.getString("key", "change_me");
        this.corsOrigin = bukkitConfig.getStringList("corsOrigins");
        this.securePort = bukkitConfig.getInt("port", 4567);
        this.javalin = Javalin.create(config -> configureJavalin(config, main));

        String usersFilePath = main.getDataFolder().getAbsolutePath() + File.separator + "users.yml";
        Yaml yaml = new Yaml();
        try (InputStream in = new FileInputStream(usersFilePath)) {
            Map<String, List<Map<String, String>>> yamlData = yaml.load(in);
            users = yamlData.get("users");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (bukkitConfig.getBoolean("debug")) {
            this.javalin.before(ctx -> log.info(ctx.req().getPathInfo()));
        }
    }

    private void configureJavalin(JavalinConfig config, ServerTapMain main) {
        config.jsonMapper(new GsonJsonMapper());
        config.http.defaultContentType = "application/json";
        config.showJavalinBanner = false;

        configureTLS(config, main);
        configureCors(config);

        if (isAuthEnabled && "change_me".equals(authKey)) {
            log.warning("[ServerTap] AUTH KEY IS SET TO DEFAULT \"change_me\"");
            log.warning("[ServerTap] CHANGE THE key IN THE config.yml FILE");
            log.warning("[ServerTap] FAILURE TO CHANGE THE KEY MAY RESULT IN SERVER COMPROMISE");
        }
        config.accessManager(this::manageAccess);

        if (!disableSwagger) {
            OpenApiPluginConfiguration openApiConfig = getOpenApiConfig(main);
            config.plugins.register(new OpenApiPlugin(openApiConfig));
            SwaggerConfiguration swaggerConfiguration = new SwaggerConfiguration();
            swaggerConfiguration.setDocumentationPath("/swagger-docs");
            config.plugins.register(new SwaggerPlugin(swaggerConfiguration));
        }
    }

    private static String checkJWT(String authKey, String token) {
        Key key = new SecretKeySpec(authKey.getBytes(), SignatureAlgorithm.HS256.getJcaName());
        Jws<Claims> claimsJws = Jwts.parserBuilder().build().parseClaimsJws(token);
        Claims claims = claimsJws.getBody();
        return claims.getSubject();
    }

    public static String generateJWT(String uuid) {
        Key key = new SecretKeySpec(authKey.getBytes(), SignatureAlgorithm.HS256.getJcaName());
        return Jwts
                .builder()
                .setSubject(uuid)
                .setExpiration(new Date(System.currentTimeMillis() + 30000))
                .signWith(key)
                .compact();
    }

    public static boolean validateCredentials(String username, String password) {
        if (users != null) {
            String pwd_hash = BCrypt.hashpw(password, BCrypt.gensalt(12));
            System.out.println(pwd_hash);
            for (Map<String, String> user : users) {
                if (user.get("username").equals(username) && BCrypt.checkpw(password, user.get("password"))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Verifies the Path is a wagger call or has the correct authentication
     */
    private void manageAccess(Handler handler, Context ctx, Set<? extends RouteRole> routeRoles) throws Exception {
        // If auth is not enabled just serve it all
        if (!this.isAuthEnabled) {
            handler.handle(ctx);
            return;
        }

        if (isNoAuthPath(ctx.req().getPathInfo())) {
            handler.handle(ctx);
            return;
        }

        // Auth is turned on, make sure there is a header called "key"
        String authHeader = ctx.header(SERVERTAP_KEY_HEADER);
        if (authHeader != null && WebServer.checkJWT(authKey, authHeader) != null) {    // TODO: does checkJWT return null if invalid jwt? is authHeader a valid token or does it contains the Bearer string part?
            handler.handle(ctx);
            return;
        }

        // If the request is still not handled, check for a cookie (websockets use cookies for auth)
        String authCookie = ctx.cookie(SERVERTAP_KEY_COOKIE);
        if (authCookie != null && WebServer.checkJWT(authKey, authCookie) != null) {    // TODO: does checkJWT return null if invalid jwt? is authCookie a valid token or does it contains the Bearer string part?
            handler.handle(ctx);
            return;
        }

        // fall through, failsafe
        //ctx.status(401).result("Invalid token");
        ctx.header(Header.WWW_AUTHENTICATE, "Basic");
        throw new UnauthorizedResponse();
    }

    private static boolean isNoAuthPath(String requestPath) {
        return Arrays.stream(noAuthPaths).anyMatch(requestPath::startsWith);
    }

    private void configureCors(JavalinConfig config) {
        config.plugins.enableCors(cors -> cors.add(corsConfig -> {
            if (corsOrigin.contains("*")) {
                log.info("[ServerTap] Enabling CORS for *");
                corsConfig.anyHost();
            } else {
                corsOrigin.forEach(origin -> {
                    log.info(String.format("[ServerTap] Enabling CORS for %s", origin));
                    corsConfig.allowHost(origin);
                });
            }
        }));
    }

    private void configureTLS(JavalinConfig config, ServerTapMain main) {
        if (!tlsEnabled) {
            log.warning("[ServerTap] TLS is not enabled.");
            return;
        }
        try {
            final String fullKeystorePath = main.getDataFolder().getAbsolutePath() + File.separator + keyStorePath;

            if (Files.exists(Paths.get(fullKeystorePath))) {
                // Register the SSL plugin
                SSLPlugin plugin = new SSLPlugin(conf -> {
                    conf.keystoreFromPath(fullKeystorePath, keyStorePassword);
                    conf.http2 = false;
                    conf.insecure = false;
                    conf.secure = true;
                    conf.securePort = securePort;
                    conf.sniHostCheck = sni;
                });
                config.plugins.register(plugin);
                log.info("[ServerTap] TLS is enabled.");
            } else {
                log.warning(String.format("[ServerTap] TLS is enabled but %s doesn't exist. TLS disabled.", fullKeystorePath));
            }
        } catch (Exception e) {
            log.severe("[ServerTap] Error while enabling TLS: " + e.getMessage());
            log.warning("[ServerTap] TLS is not enabled.");
        }
    }

    private OpenApiPluginConfiguration getOpenApiConfig(ServerTapMain main) {
        return new OpenApiPluginConfiguration()
                .withDocumentationPath("/swagger-docs")
                .withDefinitionConfiguration((version, definition) -> definition
                        .withOpenApiInfo((openApiInfo) -> {
                            openApiInfo.setTitle(main.getDescription().getName());
                            openApiInfo.setVersion(main.getDescription().getVersion());
                            openApiInfo.setDescription(main.getDescription().getDescription());
                        }));
    }

    public void get(String route, Handler handler) {
        this.addRoute(HandlerType.GET, route, handler);
    }

    public void post(String route, Handler handler) {
        this.addRoute(HandlerType.POST, route, handler);
    }

    public void put(String route, Handler handler) {
        this.addRoute(HandlerType.PUT, route, handler);
    }

    public void delete(String route, Handler handler) {
        this.addRoute(HandlerType.DELETE, route, handler);
    }

    public void addRoute(HandlerType httpMethod, String route, Handler handler) {
        // Checks to see if passed route is blocked in the config.
        // Note: The second check is for any blocked routes that start with a /
        if (blockedPaths instanceof ConfigurationSection) {
            final ConfigurationSection confBlockedPath = (ConfigurationSection) blockedPaths;
            List<String> allBlockedPath = confBlockedPath.getStringList("all");
            List<String> blockedPathsByMethod = confBlockedPath.getStringList(httpMethod.toString().toLowerCase());

            if (!allBlockedPath.isEmpty()) {
                if (!(allBlockedPath.contains(route) || allBlockedPath.contains("/" + route))) {
                    this.javalin.addHandler(httpMethod, route, handler);
                } else if (isDebug) {
                    log.info(String.format("Not adding Route ['%s'] '%s' because it is blocked in the config.", httpMethod, route));
                }
            } else {
                if (!(blockedPathsByMethod.contains(route) || blockedPathsByMethod.contains("/" + route))) {
                    this.javalin.addHandler(httpMethod, route, handler);
                } else if (isDebug) {
                    log.info(String.format("Not adding Route ['%s'] '%s' because it is blocked in the config.", httpMethod, route));
                }
            }
        } else {
            final List<String> listBlockedPath = (List<String>) blockedPaths;
            if (!(listBlockedPath.contains(route) || listBlockedPath.contains("/" + route))) {
                this.javalin.addHandler(httpMethod, route, handler);
            } else if (isDebug) {
                log.info(String.format("Not adding Route '%s' because it is blocked in the config.", route));
            }
        }

    }

    public void ws(String route, Consumer<WsConfig> wsConfig) {
        this.javalin.ws(route, wsConfig);
    }

    public void start(int port) {
        this.javalin.start(port);
    }

    public void stop() {
        this.javalin.stop();
    }

    public Javalin getJavalin() {
        return this.javalin;
    }
}
