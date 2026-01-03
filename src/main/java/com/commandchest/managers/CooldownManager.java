package com.commandchest.managers;

import com.commandchest.models.ChestData;
import java.util.UUID;

public class CooldownManager {

    // This manager is mainly a helper - actual cooldown data is stored in ChestData
    // This class can be used for additional cooldown utilities if needed

    public boolean isOnCooldown(ChestData chestData, UUID playerUUID) {
        if (chestData.getCooldown() <= 0) {
            return false;
        }

        long lastActivation = chestData.getLastActivationTime(playerUUID);
        if (lastActivation == 0) {
            return false;
        }

        long currentTime = System.currentTimeMillis();
        long timePassed = (currentTime - lastActivation) / 1000; // Convert to seconds

        return timePassed < chestData.getCooldown();
    }

    public long getRemainingCooldown(ChestData chestData, UUID playerUUID) {
        if (chestData.getCooldown() <= 0) {
            return 0;
        }

        long lastActivation = chestData.getLastActivationTime(playerUUID);
        if (lastActivation == 0) {
            return 0;
        }

        long currentTime = System.currentTimeMillis();
        long timePassed = (currentTime - lastActivation) / 1000; // Convert to seconds
        long remaining = chestData.getCooldown() - timePassed;

        return Math.max(0, remaining);
    }

    public void setLastActivation(ChestData chestData, UUID playerUUID) {
        chestData.setLastActivationTime(playerUUID, System.currentTimeMillis());
    }
}

