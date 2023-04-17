package io.github.thebusybiscuit.slimefun4.utils;

import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;

public class UpdateSkullBlock {


    public static void manageMachineBlockActive(Location location, boolean isActive) {

        new BukkitRunnable() {
            @Override
            public void run() {
                Block block = location.getBlock();
                if (block.getType().equals(Material.AIR)) return;
                BlockState state = block.getState();
                if (state instanceof Skull skull) {
                    String skullName = skull.getOwner();
                    if (skullName == null) return;
                    String newSkullName = null;
                    if (isActive) {
                        if (!skullName.contains("_active")) {
                            newSkullName = skullName + "_active";
                        }
                    } else {
                        if (skullName.contains("_active")) {
                            newSkullName = skullName.replace("_active", "");
                        }
                    }
                    if (skullName.equals(newSkullName) || newSkullName == null) return;
                    OfflinePlayer newOfflinePlayer = getFakeOfflinePlayer(newSkullName);
                    skull.setOwnerProfile(null);
                    skull.setOwningPlayer(newOfflinePlayer);
                    skull.update();
                }
            }
        }.runTask(Slimefun.instance());

    }
    public static void manageCapacitorProcess(Location location, int process) {

        new BukkitRunnable() {
            @Override
            public void run() {
                Block block = location.getBlock();
                if (block.getType().equals(Material.AIR)) return;
                BlockState state = block.getState();
                if (state instanceof Skull skull) {
                    String skullName = skull.getOwner();
                    if (skullName == null) return;
                    if (skullName.endsWith("_" + process)) return;
                    String newSkullName = null;

                    newSkullName = skullName.replace("_1", "")
                            .replace("_2", "")
                            .replace("_3", "")
                            .replace("_4", "")
                            .replace("_5", "")
                            .replace("_6", "")
                            .replace("_7", "")
                            .replace("_8", "");

                    if (process != 0) {
                        newSkullName = newSkullName + "_" + process;
                    }
                    if (skullName.equals(newSkullName)) return;
                    OfflinePlayer newOfflinePlayer = getFakeOfflinePlayer(newSkullName);
                    skull.setOwnerProfile(null);
                    skull.setOwningPlayer(newOfflinePlayer);
                    skull.update();
                }
            }
        }.runTask(Slimefun.instance());

    }


    public static OfflinePlayer getFakeOfflinePlayer(String name) {
        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes());



        OfflinePlayer offlinePlayer = new OfflinePlayer() {
            @Override
            public boolean isOp() {
                return false;
            }

            @Override
            public void setOp(boolean b) {

            }

            @Override
            public Map<String, Object> serialize() {
                return null;
            }

            @Override
            public boolean isOnline() {
                return false;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public UUID getUniqueId() {
                return uuid;
            }

            @Override
            public PlayerProfile getPlayerProfile() {
                return null;
            }

            @Override
            public boolean isBanned() {
                return false;
            }

            @Override
            public boolean isWhitelisted() {
                return false;
            }

            @Override
            public void setWhitelisted(boolean b) {

            }

            @Override
            public Player getPlayer() {
                return null;
            }

            @Override
            public long getFirstPlayed() {
                return 0;
            }

            @Override
            public long getLastPlayed() {
                return 0;
            }

            @Override
            public boolean hasPlayedBefore() {
                return false;
            }

            @Override
            public Location getBedSpawnLocation() {
                return null;
            }

            @Override
            public void incrementStatistic(Statistic statistic) throws IllegalArgumentException {

            }

            @Override
            public void decrementStatistic(Statistic statistic) throws IllegalArgumentException {

            }

            @Override
            public void incrementStatistic(Statistic statistic, int i) throws IllegalArgumentException {

            }

            @Override
            public void decrementStatistic(Statistic statistic, int i) throws IllegalArgumentException {

            }

            @Override
            public void setStatistic(Statistic statistic, int i) throws IllegalArgumentException {

            }

            @Override
            public int getStatistic(Statistic statistic) throws IllegalArgumentException {
                return 0;
            }

            @Override
            public void incrementStatistic(Statistic statistic, Material material) throws IllegalArgumentException {

            }

            @Override
            public void decrementStatistic(Statistic statistic, Material material) throws IllegalArgumentException {

            }

            @Override
            public int getStatistic(Statistic statistic, Material material) throws IllegalArgumentException {
                return 0;
            }

            @Override
            public void incrementStatistic(Statistic statistic, Material material, int i) throws IllegalArgumentException {

            }

            @Override
            public void decrementStatistic(Statistic statistic, Material material, int i) throws IllegalArgumentException {

            }

            @Override
            public void setStatistic(Statistic statistic, Material material, int i) throws IllegalArgumentException {

            }

            @Override
            public void incrementStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {

            }

            @Override
            public void decrementStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {

            }

            @Override
            public int getStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {
                return 0;
            }

            @Override
            public void incrementStatistic(Statistic statistic, EntityType entityType, int i) throws IllegalArgumentException {

            }

            @Override
            public void decrementStatistic(Statistic statistic, EntityType entityType, int i) {

            }

            @Override
            public void setStatistic(Statistic statistic, EntityType entityType, int i) {

            }

            @Override
            public Location getLastDeathLocation() {
                return null;
            }
        };
        return offlinePlayer;
    }
}
