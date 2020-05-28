package io.servertap;

public class Constants {

    public static final String API_V1 = "v1";

    // Economy Related Messages
    public static final String VAULT_MISSING = "Vault not found. Related functionality disabled";
    public static final String VAULT_MISSING_PAY_PARAMS = "Missing uuid and/or amount";
    public static final String VAULT_GREATER_THAN_ZERO = "You must use a value greater than zero";
    public static final String ECONOMY_PLUGIN_MISSING = "Missing economy plugin";

    // Player Related Messages
    public static final String PLAYER_MISSING_PARAMS = "Missing uuid and/or world";
    public static final String PLAYER_UUID_MISSING = "Player UUID is required";
    public static final String PLAYER_INV_PARSE_FAIL = "A problem occured when attempting to parse the user file";
    public static final String PLAYER_NOT_FOUND = "Player cannot be found";

    // World messages
    public static final String WORLD_NOT_FOUND = "World cannot be found";
}
