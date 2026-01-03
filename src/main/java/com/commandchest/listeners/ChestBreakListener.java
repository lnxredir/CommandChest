package com.commandchest.listeners;

import com.commandchest.CommandChest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class ChestBreakListener implements Listener {

    private final CommandChest plugin;

    public ChestBreakListener(CommandChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // Check if the broken block is a configured chest
        if (plugin.getChestDataManager().hasChestData(event.getBlock().getLocation())) {
            // Remove chest data and hologram
            plugin.getChestDataManager().removeChestData(event.getBlock().getLocation());
            plugin.getHologramManager().removeHologram(event.getBlock().getLocation());
        }
    }
}

