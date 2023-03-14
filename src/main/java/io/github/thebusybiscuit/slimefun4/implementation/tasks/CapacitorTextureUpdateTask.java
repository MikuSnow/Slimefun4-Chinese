package io.github.thebusybiscuit.slimefun4.implementation.tasks;

import javax.annotation.Nonnull;

import io.github.thebusybiscuit.slimefun4.utils.UpdateSkullBlock;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;

import io.github.bakedlibs.dough.skins.PlayerHead;
import io.github.bakedlibs.dough.skins.PlayerSkin;
import io.github.thebusybiscuit.slimefun4.implementation.items.electric.Capacitor;
import io.github.thebusybiscuit.slimefun4.utils.HeadTexture;
import io.papermc.lib.PaperLib;

/**
 * This task is run whenever a {@link Capacitor} needs to update their texture.
 * <strong>This must be executed on the main {@link Server} {@link Thread}!</strong>
 * 
 * @author TheBusyBiscuit
 *
 */
public class CapacitorTextureUpdateTask implements Runnable {

    /**
     * The {@link Location} of the {@link Capacitor}.
     */
    private final Location l;

    /**
     * The level of how "full" this {@link Capacitor} is.
     * From 0.0 to 1.0.
     */
    private final double filledPercentage;

    /**
     * This creates a new {@link CapacitorTextureUpdateTask} with the given parameters.
     * 
     * @param l
     *            The {@link Location} of the {@link Capacitor}
     * @param charge
     *            The amount of charge in this {@link Capacitor}
     * @param capacity
     *            The capacity of this {@link Capacitor}
     */
    public CapacitorTextureUpdateTask(@Nonnull Location l, double charge, double capacity) {
        Validate.notNull(l, "The Location cannot be null");

        this.l = l;
        this.filledPercentage = charge / capacity;
    }

    @Override
    public void run() {
        Block b = l.getBlock();
        Material type = b.getType();

        // Ensure that this Block is still a Player Head
        if (type == Material.PLAYER_HEAD || type == Material.PLAYER_WALL_HEAD) {
            System.out.println(filledPercentage);
            if (filledPercentage <= 0.0) {
                UpdateSkullBlock.manageCapacitorProcess(l, 0);
                // 0-25% capacity
            } else if (filledPercentage <= 0.20) {
                // 25-50% capacity
                UpdateSkullBlock.manageCapacitorProcess(l, 20);
            } else if (filledPercentage <= 0.40) {
                // 25-50% capacity
                UpdateSkullBlock.manageCapacitorProcess(l, 40);
            } else if (filledPercentage <= 0.60) {
                // 25-50% capacity
                UpdateSkullBlock.manageCapacitorProcess(l, 60);
            } else if (filledPercentage <= 0.80) {
                // 50-75% capacity
                UpdateSkullBlock.manageCapacitorProcess(l, 80);
            } else {
                // 75-100% capacity
                UpdateSkullBlock.manageCapacitorProcess(l, 100);
            }
        }
    }

    private void setTexture(@Nonnull Block b, @Nonnull HeadTexture texture) {
        PlayerSkin skin = PlayerSkin.fromHashCode(texture.getUniqueId(), texture.getTexture());
        PlayerHead.setSkin(b, skin, false);

        PaperLib.getBlockState(b, false).getState().update(true, false);
    }

}
