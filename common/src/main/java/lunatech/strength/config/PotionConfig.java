package lunatech.strength.config;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.interfaces.meta.Exclude;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import lunatech.strength.config.exception.ConfigValidationException;
import lunatech.strength.config.migration.Migration;

import java.util.List;
import java.util.Map;

/**
 * Decoupled configuration for Potion and Potion Effect restrictions.
 */
@ConfigSerializable
public class PotionConfig implements VersionedConfig {
    @Comment("Do not change this value!")
    public int configVersion = 1;

    @Override
    @Exclude
    public int configVersion() {
        return configVersion;
    }

    @Override
    @Exclude
    public @NotNull Map<Integer, Migration> migrations() {
        return Map.of();
    }

    @Override
    @Exclude
    public void validate() throws ConfigValidationException {
    }

    @Comment("Master toggle for the potion & potion effect restriction feature module.")
    public boolean enabled = true;

    @Comment("Mode engine for potion effect restrictions: BLACKLIST or WHITELIST.")
    public String mode = "BLACKLIST";

    @Comment("Should drinking/consuming blacklisted potion items be completely blocked?")
    public boolean blockConsumption = true;

    @Comment("Should brewing blacklisted potions or potion effects in brewing stands be completely blocked?")
    public boolean blockBrewing = true;

    @Comment("""
        ================================================================================
         POTION & POTION EFFECT RESTRICTION FORMAT GUIDE
        ================================================================================
         Configure list of potion effect names or namespaced keys that players are forbidden to use.
         Supported Format Examples:
           - 'STRENGTH' (or 'minecraft:strength', 'INCREASE_DAMAGE')
           - 'SPEED' (or 'minecraft:speed')
           - 'REGENERATION' (or 'minecraft:regeneration')
           - 'INVISIBILITY' (or 'minecraft:invisibility')
           - 'WEAKNESS' (or 'minecraft:weakness')
           - 'POISON' (or 'minecraft:poison')
         You may enter either full namespaced keys (e.g. 'minecraft:strength') or short names (e.g. 'STRENGTH').
        ================================================================================
        """)
    public List<String> blacklistedEffects = List.of(
        "STRENGTH",
        "INCREASE_DAMAGE",
        "minecraft:strength"
    );
}
