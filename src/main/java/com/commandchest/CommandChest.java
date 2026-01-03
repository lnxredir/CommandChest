package com.commandchest;

import com.commandchest.commands.CChestCommand;
import com.commandchest.gui.ChestConfigGUI;
import com.commandchest.gui.NameEditorGUI;
import com.commandchest.listeners.ChatInputListener;
import com.commandchest.listeners.ChestActivationListener;
import com.commandchest.listeners.ChestBreakListener;
import com.commandchest.listeners.StickClickListener;
import com.commandchest.managers.ChestDataManager;
import com.commandchest.managers.CooldownManager;
import com.commandchest.managers.HologramManager;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandChest extends JavaPlugin {

    private static CommandChest instance;
    private ChestDataManager chestDataManager;
    private HologramManager hologramManager;
    private CooldownManager cooldownManager;
    private ChestConfigGUI chestConfigGUI;
    private NameEditorGUI nameEditorGUI;

    @Override
    public void onEnable() {
        instance = this;
        
        // Save default config
        saveDefaultConfig();
        
        // Initialize managers
        this.chestDataManager = new ChestDataManager(this);
        this.hologramManager = new HologramManager(this);
        this.cooldownManager = new CooldownManager();
        
        // Initialize GUI instances
        this.chestConfigGUI = new ChestConfigGUI(this);
        this.nameEditorGUI = new NameEditorGUI(this);
        
        // Load all chest configurations
        chestDataManager.loadAllChests();
        
        // Create holograms for all loaded chests
        for (com.commandchest.models.ChestData chestData : chestDataManager.getAllChestData()) {
            hologramManager.createHologram(chestData);
        }
        
        // Register commands
        getCommand("cchest").setExecutor(new CChestCommand(this));
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new StickClickListener(this), this);
        getServer().getPluginManager().registerEvents(new ChestActivationListener(this), this);
        getServer().getPluginManager().registerEvents(new ChestBreakListener(this), this);
        getServer().getPluginManager().registerEvents(chestConfigGUI, this);
        getServer().getPluginManager().registerEvents(nameEditorGUI, this);
        getServer().getPluginManager().registerEvents(new ChatInputListener(this, chestConfigGUI, nameEditorGUI), this);
        
        getLogger().info("CommandChest has been enabled!");
    }

    @Override
    public void onDisable() {
        // Save all chest data
        if (chestDataManager != null) {
            chestDataManager.saveAllChests();
        }
        
        // Remove all holograms
        if (hologramManager != null) {
            hologramManager.removeAllHolograms();
        }
        
        getLogger().info("CommandChest has been disabled!");
    }

    public static CommandChest getInstance() {
        return instance;
    }

    public ChestDataManager getChestDataManager() {
        return chestDataManager;
    }

    public HologramManager getHologramManager() {
        return hologramManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public ChestConfigGUI getChestConfigGUI() {
        return chestConfigGUI;
    }

    public NameEditorGUI getNameEditorGUI() {
        return nameEditorGUI;
    }
}

