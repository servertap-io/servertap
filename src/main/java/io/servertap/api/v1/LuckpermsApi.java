package io.servertap.api.v1;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpResponseException;
import io.javalin.http.InternalServerErrorResponse;
import io.javalin.openapi.*;
import io.servertap.Constants;
import io.servertap.utils.pluginwrappers.LuckpermsWrapper;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class LuckpermsApi {
    private final org.bukkit.Server bukkitServer = Bukkit.getServer();
    private final LuckpermsWrapper luckperms;

    public LuckpermsApi(LuckpermsWrapper luckpermsWrapper) {
        this.luckperms = luckpermsWrapper;
    }

    // Helper method to check Luckperms availability and get API
    private LuckPerms getLuckPermsApi() {
        if (!luckperms.isAvailable()) {
            throw new HttpResponseException(424, Constants.LUCKPERMS_PLUGIN_MISSING, new HashMap<>());
        }

        RegisteredServiceProvider<LuckPerms> provider = bukkitServer.getServicesManager().getRegistration(LuckPerms.class);
        if (provider == null) {
            throw new InternalServerErrorResponse(Constants.LUCKPERMS_PLUGIN_MISSING);
        }

        return provider.getProvider();
    }

    // Helper method to load and validate user
    private User loadUser(UUID playerUUID) {
        try {
            LuckPerms luckperms = getLuckPermsApi();
            User user = luckperms.getUserManager().loadUser(playerUUID).get();
            
            if (user == null) {
                throw new BadRequestResponse("User not found");
            }
            
            return user;
        } catch (Exception e) {
            throw new InternalServerErrorResponse("Failed to load user: " + e.getMessage());
        }
    }

    // Helper method to validate UUID
    private UUID validateUUID(String uuid) {
        UUID playerUUID = ValidationUtils.safeUUID(uuid);
        if (playerUUID == null) {
            throw new BadRequestResponse(Constants.INVALID_UUID);
        }
        return playerUUID;
    }

    // Helper method to save user changes
    private void saveUser(User user) {
        try {
            getLuckPermsApi().getUserManager().saveUser(user);
        } catch (Exception e) {
            throw new InternalServerErrorResponse("Failed to save user changes: " + e.getMessage());
        }
    }

    private List<Map<String, String>> getContextList(Node node) {
        List<Map<String, String>> contexts = new ArrayList<>();
        node.getContexts().forEach(context -> {
            Map<String, String> contextPair = new HashMap<>();
            contextPair.put("key", context.getKey());
            contextPair.put("value", context.getValue());
            contexts.add(contextPair);
        });
        return contexts;
    }

    private void addExpiryIfPresent(Map<String, Object> data, Node node) {
        if (node.hasExpiry() && node.getExpiry() != null) {
            data.put("expiry", node.getExpiry().toEpochMilli());
        }
    }

    private Map<String, Object> createParentData(InheritanceNode node) {
        Map<String, Object> parentData = new HashMap<>();
        parentData.put("group", node.getGroupName());
        parentData.put("contexts", getContextList(node));
        addExpiryIfPresent(parentData, node);
        return parentData;
    }

    private Map<String, Object> createPermissionData(PermissionNode node) {
        Map<String, Object> perm = new HashMap<>();
        perm.put("permission", node.getPermission());
        perm.put("value", node.getValue());
        perm.put("contexts", getContextList(node));
        addExpiryIfPresent(perm, node);
        return perm;
    }

    private Map<String, Object> createGroupData(Group group) {
        // Use LinkedHashMap to maintain insertion order
        Map<String, Object> groupData = new LinkedHashMap<>();
        
        // Basic group info in specified order
        groupData.put("name", group.getName());
        groupData.put("displayName", group.getFriendlyName());
        
        // Prefix and suffix
        String prefix = group.getCachedData().getMetaData().getPrefix();
        String suffix = group.getCachedData().getMetaData().getSuffix();
        groupData.put("prefix", prefix != null ? prefix : "");
        groupData.put("suffix", suffix != null ? suffix : "");
        
        // Weight
        groupData.put("weight", group.getWeight().orElse(0));
        
        // Parents and permissions
        List<Map<String, Object>> parents = new ArrayList<>();
        List<Map<String, Object>> permissions = new ArrayList<>();
        
        group.getNodes().forEach(node -> {
            if (node.getType() == NodeType.INHERITANCE) {
                parents.add(createParentData((InheritanceNode) node));
            } else if (node.getType() == NodeType.PERMISSION) {
                permissions.add(createPermissionData((PermissionNode) node));
            }
        });
        
        // Add collections in specified order
        groupData.put("parents", parents);
        groupData.put("permissions", permissions);
        
        return groupData;
    }

    @OpenApi(
            path = "/v1/luckperms",
            methods = {HttpMethod.GET},
            summary = "Luckperms information",
            tags = {"Luckperms"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(type = "application/json")),
                    @OpenApiResponse(status = "500", content = @OpenApiContent(type = "application/json"))
            }
    )
    public void getLuckpermsInformation(Context ctx) {
        // Do not change this to getLuckPermsApi().getPlugin()
        Plugin lpPlugin = bukkitServer.getPluginManager().getPlugin("LuckPerms");
        if (lpPlugin == null) {
            throw new InternalServerErrorResponse(Constants.LUCKPERMS_PLUGIN_MISSING);
        }

        io.servertap.api.v1.models.Plugin plugin = new io.servertap.api.v1.models.Plugin();
        plugin.setName(lpPlugin.getName());
        plugin.setEnabled(lpPlugin.isEnabled());
        plugin.setVersion(lpPlugin.getDescription().getVersion());

        ctx.json(plugin);
    }

    @OpenApi(
            path = "/v1/luckperms/user/{uuid}/groups",
            methods = {HttpMethod.GET},
            summary = "Get user's groups",
            description = "Gets all groups that the specified user is in",
            tags = {"Luckperms"},
            pathParams = {
                    @OpenApiParam(name = "uuid", description = "The UUID of the player")
            },
            headers = {
                    @OpenApiParam(name = "key")
            },
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(type = "application/json")),
                    @OpenApiResponse(status = "400", content = @OpenApiContent(type = "application/json")),
                    @OpenApiResponse(status = "424", content = @OpenApiContent(type = "application/json")),
                    @OpenApiResponse(status = "500", content = @OpenApiContent(type = "application/json"))
            }
    )
    public void getUserGroups(Context ctx) {
        UUID playerUUID = validateUUID(ctx.pathParam("uuid"));
        User user = loadUser(playerUUID);

        Set<String> groups = new HashSet<>();
        user.getNodes().forEach(node -> {
            if (node.getType().name().equals("INHERITANCE")) {
                groups.add(node.getKey().substring(6)); // Remove 'group.' prefix
            }
        });

        ctx.json(groups);
    }

    @OpenApi(
            path = "/v1/luckperms/user/{uuid}/permissions",
            methods = {HttpMethod.GET},
            summary = "Get user's permissions",
            description = "Gets all permissions that the specified user has",
            tags = {"Luckperms"},
            pathParams = {
                    @OpenApiParam(name = "uuid", description = "The UUID of the player")
            },
            headers = {
                    @OpenApiParam(name = "key")
            },
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(type = "application/json")),
                    @OpenApiResponse(status = "400", content = @OpenApiContent(type = "application/json")),
                    @OpenApiResponse(status = "424", content = @OpenApiContent(type = "application/json")),
                    @OpenApiResponse(status = "500", content = @OpenApiContent(type = "application/json"))
            }
    )
    public void getUserPermissions(Context ctx) {
        UUID playerUUID = validateUUID(ctx.pathParam("uuid"));
        User user = loadUser(playerUUID);

        List<Map<String, Object>> permissions = new ArrayList<>();
        user.getNodes().forEach(node -> {
            if (node.getType().name().equals("PERMISSION")) {
                Map<String, Object> perm = new HashMap<>();
                perm.put("permission", node.getKey());
                perm.put("value", node.getValue());
                permissions.add(perm);
            }
        });

        ctx.json(permissions);
    }

    @OpenApi(
            path = "/v1/luckperms/user/{uuid}/groups/{group}",
            methods = {HttpMethod.POST},
            summary = "Add user to group",
            description = "Adds the specified user to a group",
            tags = {"Luckperms"},
            pathParams = {
                    @OpenApiParam(name = "uuid", description = "The UUID of the player"),
                    @OpenApiParam(name = "group", description = "The name of the group")
            },
            headers = {
                    @OpenApiParam(name = "key")
            },
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(type = "application/json")),
                    @OpenApiResponse(status = "400", content = @OpenApiContent(type = "application/json")),
                    @OpenApiResponse(status = "424", content = @OpenApiContent(type = "application/json")),
                    @OpenApiResponse(status = "500", content = @OpenApiContent(type = "application/json"))
            }
    )
    public void addUserToGroup(Context ctx) {
        UUID playerUUID = validateUUID(ctx.pathParam("uuid"));
        String groupName = ctx.pathParam("group");
        
        User user = loadUser(playerUUID);
        LuckPerms luckperms = getLuckPermsApi();

        Group group = luckperms.getGroupManager().getGroup(groupName);
        if (group == null) {
            throw new BadRequestResponse("Group not found");
        }

        user.data().add(Node.builder("group." + group.getName()).build());
        saveUser(user);
        
        ctx.json("Successfully added user to group");
    }

    @OpenApi(
            path = "/v1/luckperms/user/{uuid}/groups/{group}",
            methods = {HttpMethod.DELETE},
            summary = "Remove user from group",
            description = "Removes the specified user from a group",
            tags = {"Luckperms"},
            pathParams = {
                    @OpenApiParam(name = "uuid", description = "The UUID of the player"),
                    @OpenApiParam(name = "group", description = "The name of the group")
            },
            headers = {
                    @OpenApiParam(name = "key")
            },
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(type = "application/json")),
                    @OpenApiResponse(status = "400", content = @OpenApiContent(type = "application/json")),
                    @OpenApiResponse(status = "424", content = @OpenApiContent(type = "application/json")),
                    @OpenApiResponse(status = "500", content = @OpenApiContent(type = "application/json"))
            }
    )
    public void removeUserFromGroup(Context ctx) {
        UUID playerUUID = validateUUID(ctx.pathParam("uuid"));
        String groupName = ctx.pathParam("group");
        
        User user = loadUser(playerUUID);
        
        user.data().remove(Node.builder("group." + groupName).build());
        saveUser(user);

        ctx.json("Successfully removed group from user");
    }

    @OpenApi(
            path = "/v1/luckperms/user/{uuid}/permissions/{permission}",
            methods = {HttpMethod.POST},
            summary = "Add permission to user",
            description = "Adds a permission to the specified user",
            tags = {"Luckperms"},
            pathParams = {
                    @OpenApiParam(name = "uuid", description = "The UUID of the player"),
                    @OpenApiParam(name = "permission", description = "The permission node")
            },
            headers = {
                    @OpenApiParam(name = "key")
            },
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(type = "application/json")),
                    @OpenApiResponse(status = "400", content = @OpenApiContent(type = "application/json")),
                    @OpenApiResponse(status = "424", content = @OpenApiContent(type = "application/json")),
                    @OpenApiResponse(status = "500", content = @OpenApiContent(type = "application/json"))
            }
    )
    public void addUserPermission(Context ctx) {
        UUID playerUUID = validateUUID(ctx.pathParam("uuid"));
        String permission = ctx.pathParam("permission");
        
        User user = loadUser(playerUUID);
        
        user.data().add(Node.builder(permission).build());
        saveUser(user);

        ctx.json("Successfully added permission to user");
    }

    @OpenApi(
            path = "/v1/luckperms/user/{uuid}/permissions/{permission}",
            methods = {HttpMethod.DELETE},
            summary = "Remove permission from user",
            description = "Removes a permission from the specified user",
            tags = {"Luckperms"},
            pathParams = {
                    @OpenApiParam(name = "uuid", description = "The UUID of the player"),
                    @OpenApiParam(name = "permission", description = "The permission node")
            },
            headers = {
                    @OpenApiParam(name = "key")
            },
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(type = "application/json")),
                    @OpenApiResponse(status = "400", content = @OpenApiContent(type = "application/json")),
                    @OpenApiResponse(status = "424", content = @OpenApiContent(type = "application/json")),
                    @OpenApiResponse(status = "500", content = @OpenApiContent(type = "application/json"))
            }
    )
    public void removeUserPermission(Context ctx) {
        UUID playerUUID = validateUUID(ctx.pathParam("uuid"));
        String permission = ctx.pathParam("permission");
        
        User user = loadUser(playerUUID);
        
        user.data().remove(Node.builder(permission).build());
        saveUser(user);

        ctx.json("Successfully removed permission from user");
    }

    @OpenApi(
            path = "/v1/luckperms/groups",
            methods = {HttpMethod.GET},
            summary = "Get all groups",
            description = "Gets all LuckPerms groups with their details and permissions",
            tags = {"Luckperms"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(type = "application/json")),
                    @OpenApiResponse(status = "424", content = @OpenApiContent(type = "application/json")),
                    @OpenApiResponse(status = "500", content = @OpenApiContent(type = "application/json"))
            }
    )
    public void getGroups(Context ctx) {
        LuckPerms luckperms = getLuckPermsApi();
        List<Map<String, Object>> groupsList = new ArrayList<>();
        
        luckperms.getGroupManager().getLoadedGroups().forEach(group -> 
            groupsList.add(createGroupData(group))
        );

        ctx.json(groupsList);
    }
}