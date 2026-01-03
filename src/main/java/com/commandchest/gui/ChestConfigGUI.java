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
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ChestConfigGUI implements Listener {

    private final CommandChest plugin;
    private final Map<UUID, ChestData> editingPlayers;
    private final Map<UUID, String> chatInputWaiting;

    // Slot constants
    private static final int SLOT_NAME = 10;
    private static final int SLOT_COMMAND = 12;
    private static final int SLOT_COOLDOWN = 14;
    private static final int SLOT_ACTIVATION_LEFT = 28;
    private static final int SLOT_ACTIVATION_RIGHT = 30;
    private static final int SLOT_ACTIVATION_BOTH = 32;
    private static final int SLOT_ACTIVATION_SHIFT = 34;
    private static final int SLOT_ITEM = 22; // Middle slot
    private static final int SLOT_DELETE = 40;
    private static final int SLOT_SAVE = 48;
    private static final int SLOT_CLOSE = 50;

    public ChestConfigGUI(CommandChest plugin) {
        this.plugin = plugin;
        this.editingPlayers = new HashMap<>();
        this.chatInputWaiting = new HashMap<>();
    }

    public void openGUI(Player player, ChestData chestData) {
        Inventory inv = plugin.getServer().createInventory(null, 54, 
            ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfig().getString("messages.gui.main-title", "&6Chest Configuration")));

        // Fill background with gray glass panes
        ItemStack background = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta bgMeta = background.getItemMeta();
        if (bgMeta != null) {
            bgMeta.setDisplayName(" ");
            background.setItemMeta(bgMeta);
        }
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, background);
        }

        // Name button
        inv.setItem(SLOT_NAME, createNameButton(chestData));

        // Command button
        inv.setItem(SLOT_COMMAND, createCommandButton(chestData));

        // Cooldown button
        inv.setItem(SLOT_COOLDOWN, createCooldownButton(chestData));

        // Activation method buttons
        inv.setItem(SLOT_ACTIVATION_LEFT, createActivationButton(chestData, ChestData.ActivationMethod.LEFT));
        inv.setItem(SLOT_ACTIVATION_RIGHT, createActivationButton(chestData, ChestData.ActivationMethod.RIGHT));
        inv.setItem(SLOT_ACTIVATION_BOTH, createActivationButton(chestData, ChestData.ActivationMethod.BOTH));
        inv.setItem(SLOT_ACTIVATION_SHIFT, createActivationButton(chestData, ChestData.ActivationMethod.SHIFT));

        // Item requirement slot
        if (chestData.getRequiredItem() != null) {
            inv.setItem(SLOT_ITEM, chestData.getRequiredItem().clone());
        } else {
            ItemStack itemSlot = new ItemStack(Material.BARRIER);
            ItemMeta itemMeta = itemSlot.getItemMeta();
            if (itemMeta != null) {
                itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.gui.button-item", "&eRequired Item")));
                itemMeta.setLore(Arrays.asList(
                    ChatColor.translateAlternateColorCodes('&',
                        plugin.getConfig().getString("messages.gui.description-item", "&7Place item here to require it"))
                ));
                itemSlot.setItemMeta(itemMeta);
            }
            inv.setItem(SLOT_ITEM, itemSlot);
        }

        // Delete button
        inv.setItem(SLOT_DELETE, createDeleteButton());

        // Save button
        inv.setItem(SLOT_SAVE, createSaveButton());

        // Close button
        inv.setItem(SLOT_CLOSE, createCloseButton());

        editingPlayers.put(player.getUniqueId(), chestData);
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        String title = event.getView().getTitle();
        String expectedTitle = ChatColor.translateAlternateColorCodes('&',
            plugin.getConfig().getString("messages.gui.main-title", "&6Chest Configuration"));
        if (!title.equals(expectedTitle)) return;

        // Cancel all interactions when our GUI is open (both top and bottom inventory)
        event.setCancelled(true);
        
        if (!editingPlayers.containsKey(player.getUniqueId())) return;
        
        // Check if this is our custom inventory
        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null) return;

        ChestData chestData = editingPlayers.get(player.getUniqueId());
        int slot = event.getSlot();

        if (slot == SLOT_NAME) {
            // Open name editor - keep chestData in editingPlayers
            player.closeInventory();
            // Small delay to ensure inventory closes properly
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                plugin.getNameEditorGUI().openGUI(player, chestData);
            }, 1L);
            return;
        }

        if (slot == SLOT_COMMAND) {
            // Request command input - set waiting BEFORE closing inventory
            requestChatInput(player, "command");
            player.closeInventory();
            return;
        }

        if (slot == SLOT_COOLDOWN) {
            // Request cooldown input - set waiting BEFORE closing inventory
            requestChatInput(player, "cooldown");
            player.closeInventory();
            return;
        }

        if (slot == SLOT_ACTIVATION_LEFT) {
            chestData.setActivationMethod(ChestData.ActivationMethod.LEFT);
            // Update the chestData in editingPlayers before reopening
            editingPlayers.put(player.getUniqueId(), chestData);
            // Use scheduler to reopen after current inventory closes
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (editingPlayers.containsKey(player.getUniqueId())) {
                    openGUI(player, chestData);
                }
            }, 1L);
            return;
        }

        if (slot == SLOT_ACTIVATION_RIGHT) {
            chestData.setActivationMethod(ChestData.ActivationMethod.RIGHT);
            // Update the chestData in editingPlayers before reopening
            editingPlayers.put(player.getUniqueId(), chestData);
            // Use scheduler to reopen after current inventory closes
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (editingPlayers.containsKey(player.getUniqueId())) {
                    openGUI(player, chestData);
                }
            }, 1L);
            return;
        }

        if (slot == SLOT_ACTIVATION_BOTH) {
            chestData.setActivationMethod(ChestData.ActivationMethod.BOTH);
            // Update the chestData in editingPlayers before reopening
            editingPlayers.put(player.getUniqueId(), chestData);
            // Use scheduler to reopen after current inventory closes
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (editingPlayers.containsKey(player.getUniqueId())) {
                    openGUI(player, chestData);
                }
            }, 1L);
            return;
        }

        if (slot == SLOT_ACTIVATION_SHIFT) {
            chestData.setActivationMethod(ChestData.ActivationMethod.SHIFT);
            // Update the chestData in editingPlayers before reopening
            editingPlayers.put(player.getUniqueId(), chestData);
            // Use scheduler to reopen after current inventory closes
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (editingPlayers.containsKey(player.getUniqueId())) {
                    openGUI(player, chestData);
                }
            }, 1L);
            return;
        }

        if (slot == SLOT_ITEM) {
            // Handle item placement/removal
            ItemStack cursor = event.getCursor();
            ItemStack current = event.getCurrentItem();
            
            if (cursor != null && cursor.getType() != Material.AIR && cursor.getAmount() > 0) {
                // Player is placing an item
                chestData.setRequiredItem(cursor.clone());
                event.setCursor(null);
                openGUI(player, chestData);
            } else if (current != null && current.getType() != Material.BARRIER && current.getType() != Material.GRAY_STAINED_GLASS_PANE) {
                // Player is removing the item (clicking on existing item)
                chestData.setRequiredItem(null);
                openGUI(player, chestData);
            }
            return;
        }

        if (slot == SLOT_DELETE) {
            // Delete configuration
            plugin.getChestDataManager().removeChestData(chestData.getLocation());
            plugin.getHologramManager().removeHologram(chestData.getLocation());
            editingPlayers.remove(player.getUniqueId());
            player.closeInventory();
            String message = plugin.getConfig().getString("messages.config.chest-deleted", "&aChest configuration deleted.");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return;
        }

        if (slot == SLOT_SAVE) {
            // Save configuration
            plugin.getChestDataManager().addChestData(chestData);
            plugin.getHologramManager().updateHologram(chestData);
            editingPlayers.remove(player.getUniqueId());
            player.closeInventory();
            String message = plugin.getConfig().getString("messages.config.chest-configured", "&aChest configured successfully!");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return;
        }

        if (slot == SLOT_CLOSE) {
            editingPlayers.remove(player.getUniqueId());
            player.closeInventory();
            return;
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        String title = event.getView().getTitle();
        String expectedTitle = ChatColor.translateAlternateColorCodes('&',
            plugin.getConfig().getString("messages.gui.main-title", "&6Chest Configuration"));
        if (title.equals(expectedTitle)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        String title = event.getView().getTitle();
        String expectedTitle = ChatColor.translateAlternateColorCodes('&',
            plugin.getConfig().getString("messages.gui.main-title", "&6Chest Configuration"));
        if (title.equals(expectedTitle)) {
            // Don't remove from editingPlayers immediately - keep it for chat input or GUI reopening
            // Only remove if not waiting for chat input
            if (!chatInputWaiting.containsKey(player.getUniqueId())) {
                // Delay removal to check if GUI is being reopened
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    // Check if player has our GUI open (was reopened)
                    if (player.getOpenInventory() != null) {
                        String openTitle = player.getOpenInventory().getTitle();
                        if (openTitle.equals(expectedTitle)) {
                            // GUI was reopened, don't remove
                            return;
                        }
                    }
                    // No GUI open, safe to remove
                    if (!chatInputWaiting.containsKey(player.getUniqueId())) {
                        editingPlayers.remove(player.getUniqueId());
                    }
                }, 3L); // Give enough time for GUI to reopen
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

        if (type.equals("command")) {
            chestData.setCommand(input);
            String message = plugin.getConfig().getString("messages.chat.command-set", "&aCommand set to: &e{command}")
                .replace("{command}", input);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            // Reopen GUI after a small delay to ensure chat is processed
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (editingPlayers.containsKey(player.getUniqueId())) {
                    openGUI(player, chestData);
                }
            }, 1L);
        } else if (type.equals("cooldown")) {
            try {
                int cooldown = Integer.parseInt(input);
                chestData.setCooldown(cooldown);
                String message = plugin.getConfig().getString("messages.chat.cooldown-set", "&aCooldown set to: &e{cooldown} seconds")
                    .replace("{cooldown}", String.valueOf(cooldown));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                // Reopen GUI after a small delay to ensure chat is processed
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (editingPlayers.containsKey(player.getUniqueId())) {
                        openGUI(player, chestData);
                    }
                }, 1L);
            } catch (NumberFormatException e) {
                String message = plugin.getConfig().getString("messages.chat.invalid-cooldown", "&cInvalid cooldown. Please enter a number.");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                // Reopen GUI after a small delay to ensure chat is processed
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (editingPlayers.containsKey(player.getUniqueId())) {
                        openGUI(player, chestData);
                    }
                }, 1L);
            }
        }
    }

    public boolean isWaitingForInput(Player player) {
        return chatInputWaiting.containsKey(player.getUniqueId());
    }

    private void requestChatInput(Player player, String type) {
        chatInputWaiting.put(player.getUniqueId(), type);
        String message;
        if (type.equals("command")) {
            message = plugin.getConfig().getString("messages.chat.enter-command", "&aPlease type the command to execute (without /):");
        } else {
            message = plugin.getConfig().getString("messages.chat.enter-cooldown", "&aPlease type the cooldown in seconds:");
        }
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    private ItemStack createNameButton(ChestData chestData) {
        ItemStack item = new ItemStack(Material.NAME_TAG);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.gui.button-name", "&eChest Name")));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.gui.description-name", "&7Click to edit the chest name")));
            if (!chestData.getNameLines().isEmpty()) {
                lore.add("");
                lore.add(ChatColor.GRAY + "Current lines: " + chestData.getNameLines().size());
                lore.add(ChatColor.GRAY + "Visible: " + (chestData.isNameVisible() ? "Yes" : "No"));
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createCommandButton(ChestData chestData) {
        ItemStack item = new ItemStack(Material.COMMAND_BLOCK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.gui.button-command", "&eCommand")));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.gui.description-command", "&7Click to set the command")));
            if (!chestData.getCommand().isEmpty()) {
                lore.add("");
                lore.add(ChatColor.GRAY + "Current: " + ChatColor.WHITE + chestData.getCommand());
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createCooldownButton(ChestData chestData) {
        ItemStack item = new ItemStack(Material.CLOCK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.gui.button-cooldown", "&eCooldown")));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.gui.description-cooldown", "&7Click to set cooldown (seconds)")));
            lore.add("");
            lore.add(ChatColor.GRAY + "Current: " + ChatColor.WHITE + chestData.getCooldown() + " seconds");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createActivationButton(ChestData chestData, ChestData.ActivationMethod method) {
        Material material;
        String descriptionKey;
        String buttonKey;
        
        switch (method) {
            case LEFT:
                material = Material.LEVER;
                descriptionKey = "messages.gui.description-activation-left";
                buttonKey = "messages.gui.button-activation";
                break;
            case RIGHT:
                material = Material.STONE_BUTTON;
                descriptionKey = "messages.gui.description-activation-right";
                buttonKey = "messages.gui.button-activation";
                break;
            case BOTH:
                material = Material.TRIPWIRE_HOOK;
                descriptionKey = "messages.gui.description-activation-both";
                buttonKey = "messages.gui.button-activation";
                break;
            case SHIFT:
                material = Material.PISTON;
                descriptionKey = "messages.gui.description-activation-shift";
                buttonKey = "messages.gui.button-activation";
                break;
            default:
                material = Material.STONE_BUTTON;
                descriptionKey = "messages.gui.description-activation-right";
                buttonKey = "messages.gui.button-activation";
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String name = method.name() + " Click";
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e" + name));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString(descriptionKey, "&7Click to set activation method")));
            lore.add("");
            if (chestData.getActivationMethod() == method) {
                lore.add(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.gui.status-enabled", "&aEnabled")));
            } else {
                lore.add(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.gui.status-disabled", "&cDisabled")));
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createDeleteButton() {
        ItemStack item = new ItemStack(Material.RED_WOOL);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.gui.button-delete", "&cDelete Configuration")));
            meta.setLore(Arrays.asList(
                ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.gui.description-delete", "&7Click to delete this configuration"))
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createSaveButton() {
        ItemStack item = new ItemStack(Material.GREEN_WOOL);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.gui.button-save", "&aSave")));
            meta.setLore(Arrays.asList(
                ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.gui.description-save", "&7Click to save changes"))
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createCloseButton() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.gui.button-close", "&7Close")));
            meta.setLore(Arrays.asList(
                ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.gui.description-close", "&7Click to close"))
            ));
            item.setItemMeta(meta);
        }
        return item;
    }
}

