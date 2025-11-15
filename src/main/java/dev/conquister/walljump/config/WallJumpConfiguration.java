package dev.conquister.walljump.config;

import dev.conquister.walljump.WallJump;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import org.bukkit.configuration.InvalidConfigurationException;

public final class WallJumpConfiguration extends YamlConfiguration {

    private final File configFile;
    private final Map<String, Object> data;

    public WallJumpConfiguration(String fileName) {
        super();

        WallJump plugin = WallJump.getInstance();
        configFile = new File(plugin.getDataFolder(), fileName);
        data = new HashMap<>(); // Inicialize aqui

        if (!configFile.exists()) {
            boolean status = configFile.getParentFile().mkdirs();
            if(!status)
                WallJump.warning("[WallJump] Failed to create the config file!");
            plugin.saveResource(fileName, false);
        }

        reload();

        InputStream defaultConfigInputStream = WallJump.class.getResourceAsStream("/" + fileName);
        assert defaultConfigInputStream != null;
        InputStreamReader defaultConfigReader = new InputStreamReader(defaultConfigInputStream);
        setDefaults(YamlConfiguration.loadConfiguration(defaultConfigReader));

        WallJump.debug("Configuration loaded: " + fileName);
    }

    public void reload() {
        try {
            load(configFile);
            data.clear(); // Limpe em vez de criar novo
            WallJump.debug("Configuration reloaded: " + configFile.getName());
        } catch (IOException | InvalidConfigurationException e) {
            WallJump.warning("[WallJump] An error occurred while reloading the config.");
            WallJump.debug("Config reload error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            save(configFile);
            WallJump.debug("Configuration saved: " + configFile.getName());
        } catch(IOException e) {
            WallJump.warning("[WallJump] An error occurred while saving the config.");
            WallJump.debug("Config save error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked") // Suprime o warning de unchecked cast
    public List<Material> getMaterialList(String path) {
        try {
            Object cached = data.get(path);
            if(cached instanceof List<?>) {
                WallJump.debug("Material list cache hit: " + path);
                return (List<Material>) cached;
            }

            List<Material> result = new ArrayList<>();
            List<String> materialNames = getStringList(path);
            WallJump.debug("Loading material list from path: " + path + " (" + materialNames.size() + " entries)");

            for(String materialName : materialNames) {
                try {
                    result.add(Material.valueOf(materialName));
                } catch(IllegalArgumentException e) {
                    WallJump.debug("Invalid material name: " + materialName);
                }
            }

            data.put(path, result);
            WallJump.debug("Material list loaded: " + path + " -> " + result.size() + " materials");
            return result;
        }catch (Exception e){
            WallJump.warning("[WallJump] An error occurred while getting a material list from the config.");
            WallJump.debug("Material list error: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @SuppressWarnings("unchecked") // Suprime o warning de unchecked cast
    public List<World> getWorldList(String path) {
        try {
            Object cached = data.get(path);
            if(cached instanceof List<?>) {
                WallJump.debug("World list cache hit: " + path);
                return (List<World>) cached;
            }

            List<World> result = new ArrayList<>();
            List<String> worldNames = getStringList(path);
            WallJump.debug("Loading world list from path: " + path + " (" + worldNames.size() + " entries)");

            for(String worldName : worldNames) {
                World world = Bukkit.getWorld(worldName);
                if(world != null) {
                    result.add(world);
                } else {
                    WallJump.debug("World not found: " + worldName);
                }
            }

            data.put(path, result);
            WallJump.debug("World list loaded: " + path + " -> " + result.size() + " worlds");
            return result;
        }catch (Exception e){
            WallJump.warning("[WallJump] An error occurred while getting a world list from the config.");
            WallJump.debug("World list error: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}