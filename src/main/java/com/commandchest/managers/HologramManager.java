package com.commandchest.managers;

import com.commandchest.CommandChest;
import com.commandchest.models.ChestData;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

import java.util.*;

public class HologramManager {

    private final CommandChest plugin;
    private final Map<Location, List<UUID>> armorStandMap; // chest location -> list of armor stand UUIDs (fallback)
    private final Map<Location, String> fancyHologramMap; // chest location -> FancyHolograms hologram ID
    private final boolean useFancyHolograms;

    public HologramManager(CommandChest plugin) {
        this.plugin = plugin;
        this.armorStandMap = new HashMap<>();
        this.fancyHologramMap = new HashMap<>();
        
        // Check if FancyHolograms is available and API is accessible
        org.bukkit.plugin.Plugin fancyHologramsPlugin = plugin.getServer().getPluginManager().getPlugin("FancyHolograms");
        boolean hasPlugin = fancyHologramsPlugin != null;
        boolean hasApi = false;
        
        if (hasPlugin) {
            // Try to verify API is accessible
            try {
                Class.forName("de.oliver.fancyholograms.api.FancyHologramsAPI");
                hasApi = true;
                plugin.getLogger().info("FancyHolograms detected! Using FancyHolograms for hologram display.");
            } catch (ClassNotFoundException e) {
                // FancyHolograms is installed but API not accessible - use armor stands
                plugin.getLogger().info("FancyHolograms plugin found but API not accessible. Using armor stands for hologram display.");
            }
        } else {
            plugin.getLogger().info("FancyHolograms not found. Using vanilla armor stands for hologram display.");
        }
        
        this.useFancyHolograms = hasPlugin && hasApi;
    }

    public void createHologram(ChestData chestData) {
        if (!chestData.isNameVisible() || chestData.getNameLines().isEmpty()) {
            return;
        }

        Location chestLocation = chestData.getLocation();
        if (chestLocation.getWorld() == null) {
            return;
        }

        // Remove existing holograms for this chest
        removeHologram(chestLocation);

        if (useFancyHolograms) {
            createFancyHologram(chestData);
        } else {
            createArmorStandHologram(chestData);
        }
    }

    private void createFancyHologram(ChestData chestData) {
        try {
            // Try to get FancyHolograms plugin instance
            org.bukkit.plugin.Plugin fancyHologramsPlugin = plugin.getServer().getPluginManager().getPlugin("FancyHolograms");
            if (fancyHologramsPlugin == null) {
                createArmorStandHologram(chestData);
                return;
            }
            
            // Try multiple possible API class paths
            Class<?> apiClass = null;
            String[] possiblePaths = {
                "de.oliver.fancyholograms.api.FancyHologramsAPI",
                "de.oliver.fancyholograms.FancyHolograms",
                "de.oliver.fancyholograms.api.hologram.FancyHologramsAPI"
            };
            
            for (String path : possiblePaths) {
                try {
                    apiClass = Class.forName(path);
                    break;
                } catch (ClassNotFoundException ignored) {
                    // Try next path
                }
            }
            
            if (apiClass == null) {
                plugin.getLogger().warning("FancyHolograms API class not found. Falling back to armor stands.");
                createArmorStandHologram(chestData);
                return;
            }
            
            // Get API instance
            Object api;
            try {
                api = apiClass.getMethod("get").invoke(null);
            } catch (Exception e) {
                // Try getting from plugin instance
                api = fancyHologramsPlugin;
            }
            
            Location chestLocation = chestData.getLocation();
            String hologramId = "commandchest_" + chestData.getChestUUID().toString();
            Location hologramLocation = chestLocation.clone().add(0.5, 1.0, 0.5);
            
            // Get hologram manager
            Object hologramManager;
            try {
                hologramManager = apiClass.getMethod("getHologramManager").invoke(api);
            } catch (Exception e) {
                // Try alternative method name
                hologramManager = apiClass.getMethod("getHologramManager").invoke(api);
            }
            
            // Try to find Hologram class
            Class<?> hologramClass = null;
            String[] hologramPaths = {
                "de.oliver.fancyholograms.api.hologram.Hologram",
                "de.oliver.fancyholograms.hologram.Hologram",
                "de.oliver.fancyholograms.api.Hologram"
            };
            
            for (String path : hologramPaths) {
                try {
                    hologramClass = Class.forName(path);
                    break;
                } catch (ClassNotFoundException ignored) {
                    // Try next path
                }
            }
            
            if (hologramClass == null) {
                plugin.getLogger().warning("FancyHolograms Hologram class not found. Falling back to armor stands.");
                createArmorStandHologram(chestData);
                return;
            }
            
            // Create hologram
            Object hologram = hologramClass.getConstructor(String.class, Location.class)
                    .newInstance(hologramId, hologramLocation);
            
            // Set text lines
            List<String> nameLines = chestData.getNameLines();
            List<String> formattedLines = new ArrayList<>();
            for (String line : nameLines) {
                // FancyHolograms supports MiniMessage, so we can use color codes directly
                formattedLines.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            
            // Set text using setText method
            hologramClass.getMethod("setText", List.class).invoke(hologram, formattedLines);
            
            // Create and show the hologram
            Class<?> managerClass = hologramManager.getClass();
            managerClass.getMethod("addHologram", hologramClass).invoke(hologramManager, hologram);
            hologramClass.getMethod("createHologram").invoke(hologram);
            hologramClass.getMethod("showHologram").invoke(hologram);
            
            fancyHologramMap.put(chestLocation, hologramId);
        } catch (Exception e) {
            // Any error - silently fall back to armor stands
            // Don't log to avoid spam since this happens on every hologram creation
            createArmorStandHologram(chestData);
        }
    }

    private void createArmorStandHologram(ChestData chestData) {
        Location chestLocation = chestData.getLocation();
        List<UUID> armorStandUUIDs = new ArrayList<>();
        List<String> nameLines = chestData.getNameLines();

        // Create armor stands from bottom to top
        for (int i = 0; i < nameLines.size(); i++) {
            String line = nameLines.get(i);
            Location standLocation = chestLocation.clone().add(0.5, 1.0 + (nameLines.size() - 1 - i) * 0.3, 0.5);

            ArmorStand armorStand = (ArmorStand) chestLocation.getWorld().spawnEntity(standLocation, EntityType.ARMOR_STAND);
            armorStand.setVisible(false);
            armorStand.setGravity(false);
            armorStand.setInvulnerable(true);
            armorStand.setCustomNameVisible(true);
            armorStand.setCustomName(ChatColor.translateAlternateColorCodes('&', line));
            armorStand.setMarker(true);
            armorStand.setSmall(i == nameLines.size() - 1); // Make top line smaller if multiple lines

            armorStandUUIDs.add(armorStand.getUniqueId());
        }

        armorStandMap.put(chestLocation, armorStandUUIDs);
    }

    public void updateHologram(ChestData chestData) {
        removeHologram(chestData.getLocation());
        createHologram(chestData);
    }

    public void removeHologram(Location chestLocation) {
        // Remove FancyHolograms hologram if exists
        String hologramId = fancyHologramMap.remove(chestLocation);
        if (hologramId != null && useFancyHolograms) {
            try {
                org.bukkit.plugin.Plugin fancyHologramsPlugin = plugin.getServer().getPluginManager().getPlugin("FancyHolograms");
                if (fancyHologramsPlugin == null) return;
                
                // Try to find API class
                Class<?> apiClass = null;
                String[] possiblePaths = {
                    "de.oliver.fancyholograms.api.FancyHologramsAPI",
                    "de.oliver.fancyholograms.FancyHolograms",
                    "de.oliver.fancyholograms.api.hologram.FancyHologramsAPI"
                };
                
                for (String path : possiblePaths) {
                    try {
                        apiClass = Class.forName(path);
                        break;
                    } catch (ClassNotFoundException ignored) {
                        // Try next path
                    }
                }
                
                if (apiClass == null) return;
                
                Object api = apiClass.getMethod("get").invoke(null);
                Object hologramManager = apiClass.getMethod("getHologramManager").invoke(api);
                Class<?> managerClass = hologramManager.getClass();
                managerClass.getMethod("removeHologram", String.class).invoke(hologramManager, hologramId);
            } catch (Exception e) {
                // Silently fail - armor stands will be cleaned up anyway
            }
        }
        
        // Remove armor stand holograms if exists
        List<UUID> armorStandUUIDs = armorStandMap.remove(chestLocation);
        if (armorStandUUIDs != null && chestLocation.getWorld() != null) {
            for (UUID uuid : armorStandUUIDs) {
                org.bukkit.entity.Entity entity = chestLocation.getWorld().getEntity(uuid);
                if (entity != null && entity instanceof ArmorStand) {
                    entity.remove();
                }
            }
        }
    }

    public void removeAllHolograms() {
        for (Location location : new HashSet<>(fancyHologramMap.keySet())) {
            removeHologram(location);
        }
        for (Location location : new HashSet<>(armorStandMap.keySet())) {
            removeHologram(location);
        }
    }

    public void loadHologramsForWorld(org.bukkit.World world) {
        // Load holograms for all chests in the given world
        for (ChestData chestData : plugin.getChestDataManager().getAllChestData()) {
            if (chestData.getLocation().getWorld().equals(world)) {
                createHologram(chestData);
            }
        }
    }
}
