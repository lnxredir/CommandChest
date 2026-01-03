package com.commandchest.gui;

import com.commandchest.CommandChest;
import com.commandchest.models.ChestData;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class NameEditorGUI implements Listener {

    private final CommandChest plugin;
    private final Map<UUID, ChestData> editingPlayers;
    private final Map<UUID, String> chatInputWaiting;

    private static final int SLOT_ADD_LINE = 4;
    private static final int SLOT_TOGGLE_VISIBILITY = 49;
    private static final int SLOT_BACK = 45;
    private static final int START_LINE_SLOTS = 9; // Start displaying lines from slot 9

    public NameEditorGUI(CommandChest plugin) {
        this.plugin = plugin;
        this.editingPlayers = new HashMap<>();
        this.chatInputWaiting = new HashMap<>();
    }

    public void openGUI(Player player, ChestData chestData) {
        Inventory inv = plugin.getServer().createInventory(null, 54,
            ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.gui.name-editor-title", "&6Edit Chest Name")));

        // Fill background
        ItemStack background = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta bgMeta = background.getItemMeta();
        if (bgMeta != null) {
            bgMeta.setDisplayName(" ");
            background.setItemMeta(bgMeta);
        }
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, background);
        }

        // Display current lines
        List<String> nameLines = chestData.getNameLines();
        for (int i = 0; i < Math.min(nameLines.size(), 36); i++) {
            int slot = START_LINE_SLOTS + i;
            inv.setItem(slot, createLineItem(nameLines.get(i), i));
        }

        // Add line button
        inv.setItem(SLOT_ADD_LINE, createAddLineButton());

        // Toggle visibility button
        inv.setItem(SLOT_TOGGLE_VISIBILITY, createToggleVisibilityButton(chestData));

        // Back button
        inv.setItem(SLOT_BACK, createBackButton());

        editingPlayers.put(player.getUniqueId(), chestData);
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        String title = event.getView().getTitle();
        String expectedTitle = ChatColor.translateAlternateColorCodes('&',
            plugin.getConfig().getString("messages.gui.name-editor-title", "&6Edit Chest Name"));
        if (!title.equals(expectedTitle)) return;

        // Cancel all interactions when our GUI is open (both top and bottom inventory)
        event.setCancelled(true);
        
        if (!editingPlayers.containsKey(player.getUniqueId())) return;
        
        // Check if this is our custom inventory
        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null) return;

        ChestData chestData = editingPlayers.get(player.getUniqueId());
        int slot = event.getSlot();

        if (slot == SLOT_ADD_LINE) {
            // Request chat input - set waiting BEFORE closing inventory
            requestChatInput(player, "add-line");
            player.closeInventory();
            return;
        }

        if (slot == SLOT_TOGGLE_VISIBILITY) {
            chestData.setNameVisible(!chestData.isNameVisible());
            openGUI(player, chestData);
            return;
        }

        if (slot == SLOT_BACK) {
            editingPlayers.remove(player.getUniqueId());
            player.closeInventory();
            plugin.getChestConfigGUI().openGUI(player, chestData);
            return;
        }

        // Check if clicking on a line item (remove line)
        if (slot >= START_LINE_SLOTS && slot < START_LINE_SLOTS + 36) {
            int lineIndex = slot - START_LINE_SLOTS;
            List<String> nameLines = chestData.getNameLines();
            if (lineIndex < nameLines.size()) {
                nameLines.remove(lineIndex);
                chestData.setNameLines(nameLines);
                openGUI(player, chestData);
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        String title = event.getView().getTitle();
        String expectedTitle = ChatColor.translateAlternateColorCodes('&',
            plugin.getConfig().getString("messages.gui.name-editor-title", "&6Edit Chest Name"));
        if (title.equals(expectedTitle)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        String title = event.getView().getTitle();
        String expectedTitle = ChatColor.translateAlternateColorCodes('&',
            plugin.getConfig().getString("messages.gui.name-editor-title", "&6Edit Chest Name"));
        if (title.equals(expectedTitle)) {
            // Don't remove from editingPlayers immediately - keep it for chat input
            // Only remove if not waiting for chat input
            if (!chatInputWaiting.containsKey(player.getUniqueId())) {
                editingPlayers.remove(player.getUniqueId());
            }
        }
    }

    public void handleChatInput(Player player, String input) {
        String type = chatInputWaiting.remove(player.getUniqueId());
        if (type == null) return;

        ChestData chestData = editingPlayers.get(player.getUniqueId());
        if (chestData == null) {
            // If chestData was lost, we can't continue
            String message = plugin.getConfig().getString("messages.chat.cancel-input", "&cConfiguration cancelled.");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            editingPlayers.remove(player.getUniqueId());
            return;
        }

        // Ensure chestData stays in editingPlayers
        editingPlayers.put(player.getUniqueId(), chestData);

        if (type.equals("add-line")) {
            List<String> nameLines = chestData.getNameLines();
            nameLines.add(input);
            chestData.setNameLines(nameLines);
            String message = plugin.getConfig().getString("messages.chat.name-line-added", "&aName line added!");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            // Reopen GUI after a small delay to ensure chat is processed
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (editingPlayers.containsKey(player.getUniqueId())) {
                    openGUI(player, chestData);
                }
            }, 1L);
        }
    }

    public boolean isWaitingForInput(Player player) {
        return chatInputWaiting.containsKey(player.getUniqueId());
    }

    private void requestChatInput(Player player, String type) {
        chatInputWaiting.put(player.getUniqueId(), type);
        String message = plugin.getConfig().getString("messages.chat.enter-name-line", "&aPlease type a line for the chest name:");
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    private ItemStack createLineItem(String line, int index) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', line));
            meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Line " + (index + 1),
                ChatColor.RED + "Click to remove"
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createAddLineButton() {
        ItemStack item = new ItemStack(Material.GREEN_WOOL);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.gui.button-add-line", "&aAdd Line")));
            meta.setLore(Arrays.asList(
                ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.gui.description-add-line", "&7Click to add a new line"))
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createToggleVisibilityButton(ChestData chestData) {
        ItemStack item = new ItemStack(chestData.isNameVisible() ? Material.SPYGLASS : Material.ENDER_EYE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.gui.button-toggle-visibility", "&eToggle Visibility")));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.gui.description-toggle-visibility", "&7Click to toggle name visibility")));
            lore.add("");
            if (chestData.isNameVisible()) {
                lore.add(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.gui.status-visible", "&aVisible")));
            } else {
                lore.add(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.gui.status-hidden", "&cHidden")));
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createBackButton() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.gui.button-back", "&7Back")));
            meta.setLore(Arrays.asList(
                ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.gui.description-back", "&7Click to go back"))
            ));
            item.setItemMeta(meta);
        }
        return item;
    }
}

