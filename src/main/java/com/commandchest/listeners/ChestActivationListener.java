package com.commandchest.listeners;

import com.commandchest.CommandChest;
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

public class ChestActivationListener implements Listener {

    private final CommandChest plugin;

    public ChestActivationListener(CommandChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK && 
            event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        Player player = event.getPlayer();
        
        // Skip if player is holding configuration stick (let StickClickListener handle it)
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item != null && item.getType() == Material.STICK && item.getItemMeta() != null) {
            if (item.getItemMeta().hasDisplayName() && 
                item.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Configuration Stick")) {
                return; // Let StickClickListener handle this
            }
        }

        ChestData chestData = plugin.getChestDataManager().getChestData(block.getLocation());
        if (chestData == null) {
            return;
        }

        // This is a configured chest - prevent normal interaction
        event.setCancelled(true);

        // Check activation method
        boolean isLeftClick = event.getAction() == Action.LEFT_CLICK_BLOCK;
        boolean isRightClick = event.getAction() == Action.RIGHT_CLICK_BLOCK;
        boolean isShiftClick = player.isSneaking();

        ChestData.ActivationMethod method = chestData.getActivationMethod();
        boolean shouldActivate = false;

        switch (method) {
            case LEFT:
                shouldActivate = isLeftClick && !isShiftClick;
                break;
            case RIGHT:
                shouldActivate = isRightClick && !isShiftClick;
                break;
            case BOTH:
                shouldActivate = !isShiftClick;
                break;
            case SHIFT:
                shouldActivate = isShiftClick;
                break;
        }

        if (!shouldActivate) {
            String message = plugin.getConfig().getString("messages.activation.activation-method-mismatch",
                "&cThis chest cannot be activated with this click type.");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return;
        }

        // Check item requirement
        if (chestData.getRequiredItem() != null) {
            ItemStack heldItem = player.getInventory().getItemInMainHand();
            if (heldItem == null || 
                heldItem.getType() != chestData.getRequiredItem().getType() ||
                !itemsMatch(heldItem, chestData.getRequiredItem())) {
                String message = plugin.getConfig().getString("messages.activation.item-required",
                    "&cYou must be holding the required item to use this chest.");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                return;
            }
        }

        // Check cooldown
        if (plugin.getCooldownManager().isOnCooldown(chestData, player.getUniqueId())) {
            long remaining = plugin.getCooldownManager().getRemainingCooldown(chestData, player.getUniqueId());
            String message = plugin.getConfig().getString("messages.activation.on-cooldown",
                "&cThis chest is on cooldown. Please wait {time} seconds.")
                .replace("{time}", String.valueOf(remaining));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return;
        }

        // Execute command
        String command = chestData.getCommand();
        if (command == null || command.isEmpty()) {
            String message = plugin.getConfig().getString("messages.config.chest-not-configured",
                "&cThis chest is not configured.");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return;
        }

        // Execute command as player
        try {
            plugin.getServer().dispatchCommand(player, command);
            plugin.getCooldownManager().setLastActivation(chestData, player.getUniqueId());
            plugin.getChestDataManager().addChestData(chestData); // Save cooldown data
            
            String message = plugin.getConfig().getString("messages.activation.command-executed",
                "&aCommand executed!");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        } catch (Exception e) {
            String message = plugin.getConfig().getString("messages.activation.command-failed",
                "&cFailed to execute command.");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            plugin.getLogger().warning("Failed to execute command for chest: " + e.getMessage());
        }
    }

    private boolean itemsMatch(ItemStack item1, ItemStack item2) {
        // Basic comparison - can be enhanced for NBT data if needed
        return item1.getType() == item2.getType() &&
               item1.getAmount() >= item2.getAmount();
    }
}

