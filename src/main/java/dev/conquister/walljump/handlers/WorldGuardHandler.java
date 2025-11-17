package dev.conquister.walljump.handlers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.association.Associables;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import dev.conquister.walljump.WallJump;
import com.sk89q.worldguard.domains.Association;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;

import java.lang.reflect.*;
import java.util.logging.Level;
import org.bukkit.World;

public class WorldGuardHandler {

    private static final String WALL_JUMP_FLAG = "wall-jump";
    private final Plugin owningPlugin = WallJump.getPlugin(WallJump.class);
    private WorldGuardPlugin worldGuardPlugin;
    private Object worldGuard;
    private Object regionContainer;
    private Method regionContainerGetMethod;
    private Method worldAdaptMethod;
    private Method regionManagerGetMethod;
    private Method vectorAtMethod; // For BlockVector3.at()
    private Constructor<?> vectorConstructor; // For old Vector constructor
    private boolean initialized = false;
    private boolean useNewAPI = false; // Track which API version to use
    public static StateFlag ALLOW_WALL_JUMP;

    public WorldGuardHandler(Plugin plugin) {
        if (plugin instanceof WorldGuardPlugin worldGuardPlugin1) {
            this.worldGuardPlugin = worldGuardPlugin1;
            initializeWorldGuard();
            try {
                registerFlag();
            } catch (Throwable ex) {
                owningPlugin.getLogger().log(Level.WARNING, "Failed to register WorldGuard custom flags", ex);
                // Try to get existing flag as fallback
                ALLOW_WALL_JUMP = (StateFlag) getExistingFlag();
                if (ALLOW_WALL_JUMP != null) {
                    owningPlugin.getLogger().info("Using existing wall-jump flag");
                }
            }
        }
    }

    private void initializeWorldGuard() {
        try {
            Class<?> worldGuardClass = Class.forName("com.sk89q.worldguard.WorldGuard");
            Method getInstanceMethod = worldGuardClass.getMethod("getInstance");
            worldGuard = getInstanceMethod.invoke(null);
            owningPlugin.getLogger().info("Found WorldGuard 7+");
        } catch (Exception ex) {
            owningPlugin.getLogger().info("Found WorldGuard <7");
        }
    }

    private void registerFlag() {
        if (worldGuard == null) return;

        FlagRegistry registry = (FlagRegistry) invokeMethod(worldGuard, "getFlagRegistry");
        if (registry == null) {
            owningPlugin.getLogger().warning("Could not get FlagRegistry from WorldGuard");
            return;
        }

        // Check if flag already exists
        Flag<?> existingFlag = registry.get(WALL_JUMP_FLAG);
        if (existingFlag instanceof StateFlag) {
            ALLOW_WALL_JUMP = (StateFlag) existingFlag;
            owningPlugin.getLogger().info("Found existing wall-jump flag");
            return;
        }

        // Try to register new flag
        try {
            StateFlag flag = new StateFlag(WALL_JUMP_FLAG, WallJump.getInstance().getConfig().getBoolean("worldGuardFlagDefault"));
            registry.register(flag);
            ALLOW_WALL_JUMP = flag;
            owningPlugin.getLogger().info("Registered new wall-jump flag");
        } catch (FlagConflictException e) {
            // Flag exists but wasn't found above - get it now
            ALLOW_WALL_JUMP = (StateFlag) registry.get(WALL_JUMP_FLAG);
            owningPlugin.getLogger().info("Flag conflict - using existing wall-jump flag");
        } catch (IllegalStateException e) {
            // Registration window closed - try to get existing flag
            ALLOW_WALL_JUMP = (StateFlag) registry.get(WALL_JUMP_FLAG);
            if (ALLOW_WALL_JUMP != null) {
                owningPlugin.getLogger().info("Registration window closed - using existing wall-jump flag");
            } else {
                owningPlugin.getLogger().warning("Cannot register flag (registration window closed) and no existing flag found");
            }
        }
    }

    private Flag<?> getExistingFlag() {
        try {
            FlagRegistry registry = (FlagRegistry) invokeMethod(worldGuard, "getFlagRegistry");
            if (registry != null) {
                return registry.get(WALL_JUMP_FLAG);
            }
        } catch (Exception e) {
            owningPlugin.getLogger().log(Level.WARNING, "Error fetching existing flag", e);
        }
        return null;
    }

    private Object invokeMethod(Object target, String methodName, Object... args) {
        try {
            Method method = target.getClass().getMethod(methodName, getParameterTypes(args));
            return method.invoke(target, args);
        } catch (IllegalAccessException | NoSuchMethodException | SecurityException | InvocationTargetException ex) {
            owningPlugin.getLogger().log(Level.WARNING, "Error invoking method " + methodName, ex);
            return null;
        }
    }

    private Class<?>[] getParameterTypes(Object[] args) {
        if (args == null) return new Class<?>[0];
        Class<?>[] paramTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            paramTypes[i] = args[i].getClass();
        }
        return paramTypes;
    }

    public boolean canWallJump(Player player) {
        try {
            Location location = player.getLocation();

            // If no WorldGuard plugin or flag, allow by default
            if (worldGuardPlugin == null || ALLOW_WALL_JUMP == null) {
                return true;
            }

            ApplicableRegionSet regionSet = getRegionSet(location);
            if (regionSet == null) {
                return true; // No regions, allow by default
            }

            RegionAssociable associable = getAssociable(player);
            if (associable == null) {
                return true; // Can't check, allow by default
            }

            return regionSet.queryState(associable, ALLOW_WALL_JUMP) != StateFlag.State.DENY;
        } catch (Exception e) {
            WallJump.warning("Error checking if player can wall jump: " + e.getMessage());
            if (WallJump.isDebugMode()) {
                e.printStackTrace();
            }
            return true; // On error, allow by default
        }
    }

    private RegionAssociable getAssociable(Player player) {
        try {
            if (player == null) return Associables.constant(Association.NON_MEMBER);
            return worldGuardPlugin.wrapPlayer(player);
        } catch (Exception e) {
            WallJump.warning("Error getting Region associable: " + e.getMessage());
            return null;
        }
    }

    private void initialize() {
        if (initialized) return;
        initialized = true;
        try {
            setupReflectionMethods();
        } catch (Exception ex) {
            owningPlugin.getLogger().log(Level.WARNING, "Failed to initialize WorldGuard integration", ex);
        }
    }

    private void setupReflectionMethods() throws Exception {
        // First, get the regionContainer from WorldGuard
        if (regionContainer == null && worldGuard != null) {
            Method getPlatformMethod = worldGuard.getClass().getMethod("getPlatform");
            Object platform = getPlatformMethod.invoke(worldGuard);
            Method getRegionContainerMethod = platform.getClass().getMethod("getRegionContainer");
            regionContainer = getRegionContainerMethod.invoke(platform);
        }

        // Verify regionContainer was successfully obtained
        if (regionContainer == null) {
            throw new IllegalStateException("Failed to obtain regionContainer from WorldGuard");
        }

        Class<?> worldEditWorldClass = Class.forName("com.sk89q.worldedit.world.World");
        Class<?> worldEditAdapterClass = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
        worldAdaptMethod = worldEditAdapterClass.getMethod("adapt", World.class);

        regionContainerGetMethod = regionContainer.getClass().getMethod("get", worldEditWorldClass);

        // Try to use new BlockVector3 API first (WorldEdit 7+)
        try {
            Class<?> blockVector3Class = Class.forName("com.sk89q.worldedit.math.BlockVector3");
            vectorAtMethod = blockVector3Class.getMethod("at", double.class, double.class, double.class);
            regionManagerGetMethod = RegionManager.class.getMethod("getApplicableRegions", blockVector3Class);
            useNewAPI = true;
            owningPlugin.getLogger().info("Using WorldEdit 7+ API (BlockVector3)");
        } catch (ClassNotFoundException e) {
            // Fall back to old Vector API (WorldEdit 6)
            Class<?> vectorClass = Class.forName("com.sk89q.worldedit.Vector");
            vectorConstructor = vectorClass.getConstructor(Double.TYPE, Double.TYPE, Double.TYPE);
            regionManagerGetMethod = RegionManager.class.getMethod("getApplicableRegions", vectorClass);
            useNewAPI = false;
            owningPlugin.getLogger().info("Using WorldEdit 6 API (Vector)");
        }
    }

    private RegionManager getRegionManager(World world) {
        initialize();
        if (regionContainer == null || regionContainerGetMethod == null) return null;
        try {
            Object worldEditWorld = worldAdaptMethod != null ? worldAdaptMethod.invoke(null, world) : world;
            return (RegionManager) regionContainerGetMethod.invoke(regionContainer, worldEditWorld);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            owningPlugin.getLogger().log(Level.WARNING, "An error occurred looking up a WorldGuard RegionManager", ex);
            return null;
        }
    }

    private ApplicableRegionSet getRegionSet(Location location) {
        RegionManager regionManager = getRegionManager(location.getWorld());
        if (regionManager == null) return null;
        try {
            Object vector;
            if (useNewAPI) {
                // Use BlockVector3.at(x, y, z) for WorldEdit 7+
                vector = vectorAtMethod.invoke(null, location.getX(), location.getY(), location.getZ());
            } else {
                // Use Vector constructor for WorldEdit 6
                vector = vectorConstructor.newInstance(location.getX(), location.getY(), location.getZ());
            }
            return (ApplicableRegionSet) regionManagerGetMethod.invoke(regionManager, vector);
        } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | InvocationTargetException ex) {
            owningPlugin.getLogger().log(Level.WARNING, "An error occurred looking up a WorldGuard ApplicableRegionSet", ex);
            return null;
        }
    }
}