package com.commandchest.commands;

import com.commandchest.CommandChest;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class CChestCommand implements CommandExecutor {

    private final CommandChest plugin;

    public CChestCommand(CommandChest plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("commandchest.use")) {
            String message = plugin.getConfig().getString("messages.command.no-permission", "&cYou don't have permission to use this command.");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return true;
        }

        // Create configuration stick
        ItemStack stick = new ItemStack(Material.STICK);
        ItemMeta meta = stick.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "Configuration Stick");
            List<String> lore = Arrays.asList(
                    ChatColor.GRAY + "Right-click on a chest",
                    ChatColor.GRAY + "to configure it."
            );
            meta.setLore(lore);
            // Add custom NBT tag to identify this as the config stick
            stick.setItemMeta(meta);
        }

        // Give stick to player
        player.getInventory().addItem(stick);
        
        String message = plugin.getConfig().getString("messages.command.stick-received", "&aYou have received the configuration stick!");
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));

        return true;
    }
}

