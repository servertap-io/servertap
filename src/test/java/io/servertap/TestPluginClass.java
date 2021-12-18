package io.servertap;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import io.servertap.utils.AuthHandler;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
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
    @DisplayName("swagger endpoint loads")
    void verifySwaggerUI() {
        HttpResponse<JsonNode> response = Unirest.get(TEST_URL_BASE + "/swagger").asJson();
        Assertions.assertEquals(200, response.getStatus());
    }

    @Test
    @DisplayName("swagger-docs endpoint loads")
    void verifySwaggerDocs() {
        HttpResponse<JsonNode> response = Unirest.get(TEST_URL_BASE + "/swagger-docs").asJson();
        Assertions.assertEquals(200, response.getStatus());
    }

    @Test
    @DisplayName("check a route")
    void testRouteAuth() {
        AuthHandler a = new AuthHandler(MockConfiguration.authConfig().getConfigurationSection("auth"));

        // Exact path matches
        Assertions.assertTrue(a.checkAuth("fakekey1", "/v1/worlds"));
        Assertions.assertTrue(a.checkAuth("fakekey1", "/v1/success"));

        // Some wildcards
        Assertions.assertTrue(a.checkAuth("fakekey1", "/v1/a/asdf/b/c"));
        Assertions.assertTrue(a.checkAuth("fakekey1", "/v1/a/a/b/c"));
        Assertions.assertTrue(a.checkAuth("fakekey1", "/v1/a/a/a/a/a/a/b/c"));

        // Should fail
        Assertions.assertFalse(a.checkAuth("fakekey3", "/v1/unspecified"));
        Assertions.assertFalse(a.checkAuth("fakekey3", "/v1/console"));
        Assertions.assertFalse(a.checkAuth("fakekey3", "/kjsbdfklsjbgfjkdsg"));
        Assertions.assertFalse(a.checkAuth("wrong-key-here", "/v1/success"));
    }

    @Test
    @DisplayName("check deny-allow routes")
    void testDenyAllow() {
        AuthHandler a = new AuthHandler(MockConfiguration.authConfig().getConfigurationSection("auth"));

        Assertions.assertFalse(a.checkAuth("fakekey3", "/v1/shouldfail"));
        Assertions.assertTrue(a.checkAuth("fakekey3", "/v1/allowed"));
    }
    }
