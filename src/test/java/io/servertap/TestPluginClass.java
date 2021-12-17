package io.servertap;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.junit.jupiter.api.*;

public class TestPluginClass {
    private static ServerMock server;
    private static PluginEntrypoint plugin;

    @BeforeAll
    public static void setUp() {
        MockBukkit.mock();
        plugin = (PluginEntrypoint) MockBukkit.load(PluginEntrypoint.class);
    }

    @AfterAll
    public static void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Verify that auth is on")
    void verifyTestEnvironment() {
        HttpResponse<JsonNode> response = Unirest.get("http://localhost:4567/v1/players/all").asJson();
        Assertions.assertEquals(401, response.getStatus());
    }
}
