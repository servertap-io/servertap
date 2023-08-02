package io.servertap;

public class Constants {

    public static final String API_V1 = "v1";

    // Economy Related Messages
    public static final String VAULT_MISSING = "Vault not found or you are missing an economy plugin. Related functionality disabled";
    public static final String VAULT_MISSING_PAY_PARAMS = "Missing uuid and/or amount";
    public static final String VAULT_GREATER_THAN_ZERO = "You must use a value greater than zero";
    public static final String ECONOMY_PLUGIN_MISSING = "Missing economy plugin";

    // Player Related Messages
    public static final String PLAYER_MISSING_PARAMS = "Missing uuid and/or world";
    public static final String PLAYER_UUID_MISSING = "Player UUID is required";
    public static final String PLAYER_INV_PARSE_FAIL = "A problem occured when attempting to parse the user file";
    public static final String PLAYER_NOT_FOUND = "Player cannot be found";

    //Whitelist Related Messages
    public static final String WHITELIST_MISSING_PARAMS = "Missing one of uuid or username";
    public static final String WHITELIST_NAME_NOT_FOUND = "Player name doesn't exist";
    public static final String WHITELIST_UUID_NOT_FOUND = "Player UUID doesn't exist";
    public static final String WHITELIST_MOJANG_API_FAIL = "Failed to access Mojang API";

    // World messages
    public static final String WORLD_NOT_FOUND = "World cannot be found";

    // Chat messages
    public static final String CHAT_MISSING_MESSAGE = "Missing Message";

    // Command Execution messages
    public static final String COMMAND_PAYLOAD_MISSING = "Missing Command";
    public static final String COMMAND_GENERIC_ERROR = "An error occurred while executing command";

    // General errors
    public static final String INVALID_UUID = "Invalid UUID";

    // PAPI Messages
    public static final String PAPI_MESSAGE_MISSING = "Missing message from request";

    // Plugin Errors
    public static final String PLUGIN_INVALID_URL = "Invalid URL submitted (malformed)";

    // SSE & Reverse Polling Event Names
    public static final String PLAYER_JOIN_EVENT = "playerJoin";
    public static final String PLAYER_QUIT_EVENT = "playerQuit";
    public static final String PLAYER_KICKED_EVENT = "playerKicked";
    public static final String UPDATE_ONLINE_PLAYER_LIST_EVENT = "updateOnlinePlayersList";
    public static final String UPDATE_ALL_PLAYER_LIST_EVENT = "updateAllPlayersList";
    public static final String UPDATE_WORLD_DATA_EVENT = "updateWorldsData";
    public static final String UPDATE_SERVER_DATA_EVENT = "updateServerData";
    public static final String UPDATE_WHITELIST_EVENT = "updateWhitelistList";
    public static final String UPDATE_OPS_LIST_EVENT = "updateOperatorsList";
    public static final String UPDATE_PLAYER_INVENTORY_EVENT_POSTFIX = "updateInventory";
    public static final String UPDATE_SCOREBOARD_DATA_EVENT = "updateScoreboardData";
    public static final String UPDATE_ADVANCEMENTS_DATA_EVENT = "updateAdvancementsData";
}
