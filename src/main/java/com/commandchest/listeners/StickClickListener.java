package com.commandchest.listeners;

import com.commandchest.CommandChest;
import com.commandchest.managers.ChestDataManager;
import com.commandchest.models.ChestData;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class StickClickListener implements Listener {

    private final CommandChest plugin;

    public StickClickListener(CommandChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Support both right-click and shift-right-click
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Check if holding configuration stick
        if (item.getType() != Material.STICK || item.getItemMeta() == null) {
            return;
        }

        if (!item.getItemMeta().hasDisplayName() || 
            !item.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Configuration Stick")) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        // Check if it's a container block
        Material blockType = block.getType();
        if (!isContainerBlock(blockType)) {
            String message = plugin.getConfig().getString("messages.config.invalid-block", 
                "&cYou can only configure container blocks (chests, barrels, etc.).");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return;
        }

        event.setCancelled(true);

        ChestDataManager dataManager = plugin.getChestDataManager();
        ChestData chestData = dataManager.getChestData(block.getLocation());

        // Check if shift-clicking on an existing configuration
        boolean isShiftClick = player.isSneaking();
        
        if (isShiftClick && chestData == null) {
            // Shift-clicking on a non-configured chest - inform user
            String message = plugin.getConfig().getString("messages.config.chest-not-configured",
                "&cThis chest is not configured. Right-click to configure it.");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return;
        }

        // Create new chest data if it doesn't exist (normal right-click)
        if (chestData == null) {
            chestData = new ChestData(UUID.randomUUID(), block.getLocation());
        }

        // Open configuration GUI (works for both new and existing configurations)
        plugin.getChestConfigGUI().openGUI(player, chestData);
    }

    private boolean isContainerBlock(Material material) {
        return material == Material.CHEST ||
               material == Material.TRAPPED_CHEST ||
               material == Material.BARREL ||
               material == Material.SHULKER_BOX ||
               material == Material.BLACK_SHULKER_BOX ||
               material == Material.BLUE_SHULKER_BOX ||
               material == Material.BROWN_SHULKER_BOX ||
               material == Material.CYAN_SHULKER_BOX ||
               material == Material.GRAY_SHULKER_BOX ||
               material == Material.GREEN_SHULKER_BOX ||
               material == Material.LIGHT_BLUE_SHULKER_BOX ||
               material == Material.LIGHT_GRAY_SHULKER_BOX ||
               material == Material.LIME_SHULKER_BOX ||
               material == Material.MAGENTA_SHULKER_BOX ||
               material == Material.ORANGE_SHULKER_BOX ||
               material == Material.PINK_SHULKER_BOX ||
               material == Material.PURPLE_SHULKER_BOX ||
               material == Material.RED_SHULKER_BOX ||
               material == Material.WHITE_SHULKER_BOX ||
               material == Material.YELLOW_SHULKER_BOX ||
               material == Material.DISPENSER ||
               material == Material.DROPPER ||
               material == Material.HOPPER ||
               material == Material.FURNACE ||
               material == Material.BLAST_FURNACE ||
               material == Material.SMOKER ||
               material == Material.BREWING_STAND;
    }
}

