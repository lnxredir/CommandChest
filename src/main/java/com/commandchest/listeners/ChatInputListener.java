package com.commandchest.listeners;

import com.commandchest.CommandChest;
import com.commandchest.gui.ChestConfigGUI;
import com.commandchest.gui.NameEditorGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;

public class ChatInputListener implements Listener {

    private final CommandChest plugin;
    private final ChestConfigGUI mainGUI;
    private final NameEditorGUI nameEditorGUI;

    public ChatInputListener(CommandChest plugin, ChestConfigGUI mainGUI, NameEditorGUI nameEditorGUI) {
        this.plugin = plugin;
        this.mainGUI = mainGUI;
        this.nameEditorGUI = nameEditorGUI;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(PlayerChatEvent event) {
        Player player = event.getPlayer();

        // Check if main GUI is waiting for input
        if (mainGUI.isWaitingForInput(player)) {
            event.setCancelled(true);
            String message = event.getMessage();
            mainGUI.handleChatInput(player, message);
            return;
        }

        // Check if name editor GUI is waiting for input
        if (nameEditorGUI.isWaitingForInput(player)) {
            event.setCancelled(true);
            String message = event.getMessage();
            nameEditorGUI.handleChatInput(player, message);
            return;
        }
    }
}

