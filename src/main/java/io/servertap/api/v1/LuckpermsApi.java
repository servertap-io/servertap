package io.servertap.api.v1;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpResponseException;
import io.javalin.http.InternalServerErrorResponse;
import io.javalin.openapi.*;
import io.servertap.Constants;
import io.servertap.utils.pluginwrappers.LuckpermsWrapper;
import io.servertap.api.v1.models.PermissionRequest;
import io.servertap.api.v1.models.GroupRequest;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.PermissionNode;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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

        // Use CachedData for more efficient group lookup
        Collection<Group> inheritedGroups = user.getInheritedGroups(user.getQueryOptions());
        Set<String> groups = inheritedGroups.stream()
            .map(Group::getName)
            .collect(Collectors.toSet());

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

        List<Map<String, Object>> permissionList = new ArrayList<>();
        
        // Get directly set permissions with full context
        user.getNodes().stream()
            .filter(node -> node.getType() == NodeType.PERMISSION)
            .map(node -> (PermissionNode) node)
            .forEach(node -> {
                Map<String, Object> perm = createPermissionData(node);
                perm.put("direct", true);
                permissionList.add(perm);
            });

        // Get inherited/effective permissions
        CachedPermissionData permissionData = user.getCachedData().getPermissionData();
        permissionData.getPermissionMap().forEach((permission, value) -> {
            // Only add if not already in the list (not directly set)
            boolean isDirect = permissionList.stream()
                .anyMatch(p -> p.get("permission").equals(permission));
                
            if (!isDirect) {
                Map<String, Object> perm = new HashMap<>();
                perm.put("permission", permission);
                perm.put("value", value);
                perm.put("direct", false);
                perm.put("contexts", new ArrayList<>()); // Empty contexts for inherited perms
                permissionList.add(perm);
            }
        });

        ctx.json(permissionList);
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

        // Use InheritanceNode builder for better type safety
        InheritanceNode node = InheritanceNode.builder(group).build();
        user.data().add(node);
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
        LuckPerms luckperms = getLuckPermsApi();

        // Get the group to ensure it exists and for type safety
        Group group = luckperms.getGroupManager().getGroup(groupName);
        if (group == null) {
            throw new BadRequestResponse("Group not found");
        }
        
        // Use InheritanceNode builder for better type safety
        InheritanceNode node = InheritanceNode.builder(group).build();
        user.data().remove(node);
        saveUser(user);

        ctx.json("Successfully removed group from user");
    }

    @OpenApi(
            path = "/v1/luckperms/user/{uuid}/permissions",
            methods = {HttpMethod.POST},
            summary = "Add permission to user",
            description = "Adds a permission to the specified user",
            tags = {"Luckperms"},
            pathParams = {
                    @OpenApiParam(name = "uuid", description = "The UUID of the player")
            },
            requestBody = @OpenApiRequestBody(content = {
                    @OpenApiContent(from = PermissionRequest.class)
            }),
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
        
        // Parse JSON manually
        JsonObject json = new Gson().fromJson(ctx.body(), JsonObject.class);
        String permission = json.has("permission") ? json.get("permission").getAsString() : null;
        Boolean value = json.has("value") ? json.get("value").getAsBoolean() : true;
        
        if (permission == null || permission.isEmpty()) {
            throw new BadRequestResponse("Permission is required");
        }
        
        User user = loadUser(playerUUID);
        
        // Use PermissionNode builder for better type safety
        PermissionNode node = PermissionNode.builder(permission)
            .value(value)
            .build();
        user.data().add(node);
        saveUser(user);

        ctx.json("Successfully added permission to user");
    }

    @OpenApi(
            path = "/v1/luckperms/user/{uuid}/permissions",
            methods = {HttpMethod.DELETE},
            summary = "Remove permission from user",
            description = "Removes a permission from the specified user",
            tags = {"Luckperms"},
            pathParams = {
                    @OpenApiParam(name = "uuid", description = "The UUID of the player")
            },
            requestBody = @OpenApiRequestBody(content = {
                    @OpenApiContent(from = PermissionRequest.class)
            }),
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
        
        // Parse JSON manually
        JsonObject json = new Gson().fromJson(ctx.body(), JsonObject.class);
        String permission = json.has("permission") ? json.get("permission").getAsString() : null;
        Boolean value = json.has("value") ? json.get("value").getAsBoolean() : true;
        
        if (permission == null || permission.isEmpty()) {
            throw new BadRequestResponse("Permission is required");
        }
        
        User user = loadUser(playerUUID);
        
        // Use PermissionNode builder for better type safety
        PermissionNode node = PermissionNode.builder(permission)
            .value(value)
            .build();
        user.data().remove(node);
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

    @OpenApi(
            path = "/v1/luckperms/groups",
            methods = {HttpMethod.POST},
            summary = "Create a new group",
            description = "Creates a new LuckPerms group with the specified properties",
            tags = {"Luckperms"},
            requestBody = @OpenApiRequestBody(content = {
                    @OpenApiContent(from = GroupRequest.class)
            }),
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
    public void addGroup(Context ctx) {
        LuckPerms luckperms = getLuckPermsApi();
        
        // Parse request body using Gson
        Gson gson = new Gson();
        GroupRequest request = gson.fromJson(ctx.body(), GroupRequest.class);

        // Validate required fields
        if (request.getName() == null || request.getName().isEmpty()) {
            throw new BadRequestResponse("Group name is required");
        }

        // Check if group already exists
        if (luckperms.getGroupManager().getGroup(request.getName()) != null) {
            throw new BadRequestResponse("Group already exists");
        }

        try {
            // Create the group
            Group group = luckperms.getGroupManager().createAndLoadGroup(request.getName()).get();

            // Set display name if provided
            if (request.getDisplayName() != null && !request.getDisplayName().isEmpty()) {
                group.data().add(Node.builder("displayname." + request.getDisplayName()).build());
            }

            // Set weight if provided
            if (request.getWeight() != null) {
                group.data().add(Node.builder("weight." + request.getWeight()).build());
            }

            // Set prefix if provided
            if (request.getPrefix() != null) {
                group.data().add(Node.builder("prefix." + request.getPrefix()).build());
            }

            // Set suffix if provided
            if (request.getSuffix() != null) {
                group.data().add(Node.builder("suffix." + request.getSuffix()).build());
            }

            // Add parent group if provided
            if (request.getParent() != null && !request.getParent().isEmpty()) {
                Group parentGroup = luckperms.getGroupManager().getGroup(request.getParent());
                if (parentGroup == null) {
                    throw new BadRequestResponse("Parent group not found");
                }
                group.data().add(InheritanceNode.builder(parentGroup).build());
            }

            // Save the group
            luckperms.getGroupManager().saveGroup(group);

            ctx.json("Successfully created group");

        } catch (Exception e) {
            throw new InternalServerErrorResponse("Failed to create group: " + e.getMessage());
        }
    }
}