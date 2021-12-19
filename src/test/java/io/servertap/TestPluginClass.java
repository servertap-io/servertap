package io.servertap;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import io.servertap.utils.AuthHandler;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.*;

public class TestPluginClass {
    private static ServerMock server;
    private static ServerTapMain plugin;

    private static final String TEST_URL_BASE = "http://localhost:4567";

    @BeforeAll
    public static void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(ServerTapMain.class);
    }

    @AfterAll
    public static void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Verify that auth is on")
    void verifyTestEnvironment() {
        HttpResponse<JsonNode> response = Unirest.get(TEST_URL_BASE + "/v1/players/all").asJson();
        Assertions.assertEquals(401, response.getStatus());
    }

    @Test
    @DisplayName("test swagger endpoint loads")
    void testSwaggerUI() {
        HttpResponse<JsonNode> response = Unirest.get(TEST_URL_BASE + "/swagger").asJson();
        Assertions.assertEquals(200, response.getStatus());
    }

    @Test
    @DisplayName("test swagger-docs endpoint loads")
    void testSwaggerDocs() {
        HttpResponse<JsonNode> response = Unirest.get(TEST_URL_BASE + "/swagger-docs").asJson();
        Assertions.assertEquals(200, response.getStatus());
    }

    @Test
    @DisplayName("check exact path matches")
    void testExactMatch() {
        AuthHandler a = new AuthHandler(MockConfiguration.authConfig().getConfigurationSection("auth"));

        // Exact path matches
        Assertions.assertTrue(a.checkAuth("fakekey1", "/v1/worlds"));
        Assertions.assertTrue(a.checkAuth("fakekey1", "/v1/success"));

        // Should fail
        Assertions.assertFalse(a.checkAuth("fakekey3", "/v1/unspecified"));
        Assertions.assertFalse(a.checkAuth("fakekey3", "/v1/console"));
        Assertions.assertFalse(a.checkAuth("fakekey3", "/kjsbdfklsjbgfjkdsg"));
        Assertions.assertFalse(a.checkAuth("wrong-key-here", "/v1/success"));

        Assertions.assertTrue(a.checkAuth("fakekey2", "/v1/allowed"));
        Assertions.assertTrue(a.checkAuth("fakekey2", "/single/awesome/wildcard"));
        Assertions.assertFalse(a.checkAuth("fakekey2", "/single/awesome/fail/wildcard"));

        Assertions.assertFalse(a.checkAuth("fakekey2", "/wildcardend"));
        Assertions.assertTrue(a.checkAuth("fakekey2", "/wildcardend/anything/at/all"));
        Assertions.assertTrue(a.checkAuth("fakekey2", "/double/thing/test/awesome/wildcard"));
        Assertions.assertFalse(a.checkAuth("fakekey2", "/double/thing/test/awesome"));

        Assertions.assertTrue(a.checkAuth("fakekey3", "/v1/allowed"));
        Assertions.assertFalse(a.checkAuth("fakekey3", "/notallowed"));
        Assertions.assertFalse(a.checkAuth("fakekey3", "/v1/notallowed"));

    }


    @Test
    @DisplayName("test wildcard")
    void testWildcard() {
        AuthHandler a = new AuthHandler(MockConfiguration.authConfig().getConfigurationSection("auth"));

        // Some wildcards
        Assertions.assertTrue(a.checkAuth("fakekey1", "/v1/a/asdf/b/c"));
        Assertions.assertTrue(a.checkAuth("fakekey1", "/v1/a/a/b/c"));
        Assertions.assertTrue(a.checkAuth("fakekey1", "/v1/a/a/a/a/a/a/b/c"));
    }

    @Test
    @DisplayName("test implicit deny")
    void testImplicitDeny() {
        AuthHandler a = new AuthHandler(MockConfiguration.authConfig().getConfigurationSection("auth"));

        Assertions.assertFalse(a.checkAuth("fakekey4", "/notallowed"));
        Assertions.assertTrue(a.checkAuth("fakekey4", "/allowed"));

    }
}
