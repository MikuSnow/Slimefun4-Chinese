package io.github.bakedlibs.dough.skins;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.github.bakedlibs.dough.reflection.ReflectionUtils;
import org.bukkit.inventory.meta.SkullMeta;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

final class CustomGameProfile extends GameProfile {

    /**
     * The player name for this profile.
     * "CS-CoreLib" for historical reasons and backwards compatibility.
     */
    public static String PLAYER_NAME = "CS-CoreLib";

    /**
     * The skin's property key.
     */
    static final String PROPERTY_KEY = "textures";

    CustomGameProfile(@Nonnull UUID uuid, @Nullable String texture) {
        super(uuid, PLAYER_NAME);

        if (texture != null) {
            getProperties().put(PROPERTY_KEY, new Property(PROPERTY_KEY, texture));
        }
    }

    CustomGameProfile(@Nonnull UUID uuid, @Nullable String texture, @Nonnull String customName) {
        super(uuid, "heypixel:" + customName);

        if (texture != null) {
            getProperties().put(PROPERTY_KEY, new Property(PROPERTY_KEY, texture));
        }
    }

    void apply(@Nonnull SkullMeta meta) throws NoSuchFieldException, IllegalAccessException {
        ReflectionUtils.setFieldValue(meta, "profile", this);

        // Forces SkullMeta to properly deserialize and serialize the profile
        meta.setOwningPlayer(meta.getOwningPlayer());

        // Now override the texture again
        ReflectionUtils.setFieldValue(meta, "profile", this);
    }

    void apply(@Nonnull SkullMeta meta, @Nonnull String customName) throws NoSuchFieldException, IllegalAccessException {
        PLAYER_NAME = "heypixel:" + customName;
        this.apply(meta);
    }

}
