package com.commandchest.models;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ChestData {
    private UUID chestUUID;
    private Location location;
    private List<String> nameLines;
    private boolean nameVisible;
    private String command;
    private int cooldown; // in seconds
    private ActivationMethod activationMethod;
    private ItemStack requiredItem; // null if no item required
    private Map<UUID, Long> lastActivationTimes; // player UUID -> timestamp

    public ChestData(UUID chestUUID, Location location) {
        this.chestUUID = chestUUID;
        this.location = location;
        this.nameLines = new ArrayList<>();
        this.nameVisible = true;
        this.command = "";
        this.cooldown = 0;
        this.activationMethod = ActivationMethod.RIGHT;
        this.requiredItem = null;
        this.lastActivationTimes = new HashMap<>();
    }

    public UUID getChestUUID() {
        return chestUUID;
    }

    public void setChestUUID(UUID chestUUID) {
        this.chestUUID = chestUUID;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public List<String> getNameLines() {
        return nameLines;
    }

    public void setNameLines(List<String> nameLines) {
        this.nameLines = nameLines != null ? nameLines : new ArrayList<>();
    }

    public boolean isNameVisible() {
        return nameVisible;
    }

    public void setNameVisible(boolean nameVisible) {
        this.nameVisible = nameVisible;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command != null ? command : "";
    }

    public int getCooldown() {
        return cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = Math.max(0, cooldown);
    }

    public ActivationMethod getActivationMethod() {
        return activationMethod;
    }

    public void setActivationMethod(ActivationMethod activationMethod) {
        this.activationMethod = activationMethod != null ? activationMethod : ActivationMethod.RIGHT;
    }

    public ItemStack getRequiredItem() {
        return requiredItem;
    }

    public void setRequiredItem(ItemStack requiredItem) {
        this.requiredItem = requiredItem;
    }

    public Map<UUID, Long> getLastActivationTimes() {
        return lastActivationTimes;
    }

    public void setLastActivationTimes(Map<UUID, Long> lastActivationTimes) {
        this.lastActivationTimes = lastActivationTimes != null ? lastActivationTimes : new HashMap<>();
    }

    public void setLastActivationTime(UUID playerUUID, long timestamp) {
        lastActivationTimes.put(playerUUID, timestamp);
    }

    public long getLastActivationTime(UUID playerUUID) {
        return lastActivationTimes.getOrDefault(playerUUID, 0L);
    }

    public enum ActivationMethod {
        LEFT,
        RIGHT,
        BOTH,
        SHIFT
    }
}

