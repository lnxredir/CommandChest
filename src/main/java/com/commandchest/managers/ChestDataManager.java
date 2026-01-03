package com.commandchest.managers;

import com.commandchest.CommandChest;
import com.commandchest.models.ChestData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ChestDataManager {

    private final CommandChest plugin;
    private final Map<Location, ChestData> chestDataMap;
    private final File chestsFolder;

    public ChestDataManager(CommandChest plugin) {
        this.plugin = plugin;
        this.chestDataMap = new HashMap<>();
        this.chestsFolder = new File(plugin.getDataFolder(), "chests");
        
        if (!chestsFolder.exists()) {
            chestsFolder.mkdirs();
        }
    }

    public void loadAllChests() {
        File[] files = chestsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            try {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                ChestData chestData = loadChestData(config);
                if (chestData != null && chestData.getLocation() != null) {
                    chestDataMap.put(chestData.getLocation(), chestData);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load chest data from " + file.getName() + ": " + e.getMessage());
            }
        }

        plugin.getLogger().info("Loaded " + chestDataMap.size() + " configured chest(s).");
    }

    public void saveAllChests() {
        for (ChestData chestData : chestDataMap.values()) {
            saveChestData(chestData);
        }
    }

    public ChestData getChestData(Location location) {
        return chestDataMap.get(location);
    }

    public void addChestData(ChestData chestData) {
        chestDataMap.put(chestData.getLocation(), chestData);
        saveChestData(chestData);
    }

    public void removeChestData(Location location) {
        ChestData chestData = chestDataMap.remove(location);
        if (chestData != null) {
            deleteChestFile(chestData.getChestUUID());
        }
    }

    public boolean hasChestData(Location location) {
        return chestDataMap.containsKey(location);
    }

    public Collection<ChestData> getAllChestData() {
        return chestDataMap.values();
    }

    private ChestData loadChestData(FileConfiguration config) {
        try {
            UUID chestUUID = UUID.fromString(config.getString("uuid"));
            String worldName = config.getString("location.world");
            int x = config.getInt("location.x");
            int y = config.getInt("location.y");
            int z = config.getInt("location.z");

            Location location = new Location(Bukkit.getWorld(worldName), x, y, z);
            ChestData chestData = new ChestData(chestUUID, location);

            chestData.setNameLines(config.getStringList("name.lines"));
            chestData.setNameVisible(config.getBoolean("name.visible", true));
            chestData.setCommand(config.getString("command", ""));
            chestData.setCooldown(config.getInt("cooldown", 0));
            
            String activationMethodStr = config.getString("activation-method", "RIGHT");
            try {
                chestData.setActivationMethod(ChestData.ActivationMethod.valueOf(activationMethodStr));
            } catch (IllegalArgumentException e) {
                chestData.setActivationMethod(ChestData.ActivationMethod.RIGHT);
            }

            // Load required item
            if (config.contains("required-item")) {
                String materialStr = config.getString("required-item.material");
                if (materialStr != null) {
                    try {
                        Material material = Material.valueOf(materialStr);
                        ItemStack item = new ItemStack(material);
                        if (config.contains("required-item.amount")) {
                            item.setAmount(config.getInt("required-item.amount"));
                        }
                        chestData.setRequiredItem(item);
                    } catch (IllegalArgumentException e) {
                        // Invalid material, skip
                    }
                }
            }

            // Load last activation times
            if (config.contains("last-activations")) {
                Map<UUID, Long> activations = new HashMap<>();
                for (String key : config.getConfigurationSection("last-activations").getKeys(false)) {
                    try {
                        UUID playerUUID = UUID.fromString(key);
                        long timestamp = config.getLong("last-activations." + key);
                        activations.put(playerUUID, timestamp);
                    } catch (IllegalArgumentException e) {
                        // Invalid UUID, skip
                    }
                }
                chestData.setLastActivationTimes(activations);
            }

            return chestData;
        } catch (Exception e) {
            plugin.getLogger().warning("Error loading chest data: " + e.getMessage());
            return null;
        }
    }

    private void saveChestData(ChestData chestData) {
        File file = new File(chestsFolder, chestData.getChestUUID().toString() + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        config.set("uuid", chestData.getChestUUID().toString());
        config.set("location.world", chestData.getLocation().getWorld().getName());
        config.set("location.x", chestData.getLocation().getBlockX());
        config.set("location.y", chestData.getLocation().getBlockY());
        config.set("location.z", chestData.getLocation().getBlockZ());

        config.set("name.lines", chestData.getNameLines());
        config.set("name.visible", chestData.isNameVisible());
        config.set("command", chestData.getCommand());
        config.set("cooldown", chestData.getCooldown());
        config.set("activation-method", chestData.getActivationMethod().name());

        // Save required item
        if (chestData.getRequiredItem() != null) {
            config.set("required-item.material", chestData.getRequiredItem().getType().name());
            config.set("required-item.amount", chestData.getRequiredItem().getAmount());
        } else {
            config.set("required-item", null);
        }

        // Save last activation times
        config.set("last-activations", null);
        for (Map.Entry<UUID, Long> entry : chestData.getLastActivationTimes().entrySet()) {
            config.set("last-activations." + entry.getKey().toString(), entry.getValue());
        }

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save chest data: " + e.getMessage());
        }
    }

    private void deleteChestFile(UUID chestUUID) {
        File file = new File(chestsFolder, chestUUID.toString() + ".yml");
        if (file.exists()) {
            file.delete();
        }
    }
}

