package lunatech.strength.task;

import lunatech.strength.Strength;
import lunatech.strength.config.AxeConfig;
import lunatech.strength.listener.player.AxeAbilityListener;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Task managing active Axe Ultimate (Executioner's Mark): storing post-mitigation damage
 * for the configured duration, rendering a floating rotating skull ItemDisplay mounted as passenger (zero lag),
 * dynamic TAB belowname nametag height clearance, authentic blood particle crumbs, and releasing capped burst damage upon expiration.
 */
public final class AxeUltimateTask extends BukkitRunnable {
    private final Player attacker;
    private final Strength plugin;
    private final int durationTicks;
    private int elapsedTicks = 0;

    private final Map<UUID, ItemDisplay> skullDisplays = new ConcurrentHashMap<>();
    private final Map<UUID, Float> skullAngles = new ConcurrentHashMap<>();
    private ItemStack customSkullItem = null;

    public AxeUltimateTask(@NotNull Player attacker, @NotNull Strength plugin, int durationSeconds) {
        this.attacker = attacker;
        this.plugin = plugin;
        this.durationTicks = durationSeconds * 20;
        initSkullItem();
    }

    private void initSkullItem() {
        final AxeConfig settings = plugin.getConfigHandler().getAxeConfig();
        final ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        final SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null && settings.skullBase64Texture != null && !settings.skullBase64Texture.isEmpty()) {
            try {
                final PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), "ExecutionerSkull");
                profile.setProperty(new ProfileProperty("textures", settings.skullBase64Texture));
                meta.setPlayerProfile(profile);
                head.setItemMeta(meta);
            } catch (Throwable ignored) {}
        }
        this.customSkullItem = head;
    }

    @Override
    public void run() {
        final UUID attackerUuid = attacker.getUniqueId();
        final AxeConfig settings = plugin.getConfigHandler().getAxeConfig();

        if (!attacker.isOnline() || attacker.isDead() || !AxeAbilityListener.activeUltimateAttackers.containsKey(attackerUuid) || elapsedTicks >= durationTicks) {
            endUltimate(attacker, plugin);
            cancel();
            return;
        }

        // Render Actionbar, floating skull ItemDisplay, and bleeding particles for marked targets
        final Map<UUID, Double> damageMap = AxeAbilityListener.storedDamagePools.get(attackerUuid);
        if (damageMap != null) {
            for (Map.Entry<UUID, Double> entry : damageMap.entrySet()) {
                final Player target = plugin.getServer().getPlayer(entry.getKey());
                if (target == null || !target.isOnline() || target.isDead() || entry.getValue() <= 0.0) {
                    final ItemDisplay deadDisplay = skullDisplays.remove(entry.getKey());
                    if (deadDisplay != null && deadDisplay.isValid()) {
                        deadDisplay.remove();
                    }
                    skullAngles.remove(entry.getKey());
                    continue;
                }

                final double total = entry.getValue() * settings.damageMultiplier;
                final String msg = settings.pendingDamageActionbarMessage.replace("{amount}", String.format("%.1f", total));
                target.sendActionBar(ColorParser.of(msg).build());

                // 1. Floating & Rotating Skull ItemDisplay on target's head (Passenger Mounted for Zero Lag/Desync)
                updateFloatingSkull(target, settings);

                // 2. Bleeding Particle Effect dripping from target's body (Authentic Blood Item/Block Crumbs)
                if (settings.enableBleedParticles && elapsedTicks % Math.max(1, settings.bleedParticleFrequencyTicks) == 0) {
                    spawnBloodParticles(target, settings);
                }
            }
        }

        elapsedTicks++;
    }

    private void spawnBloodParticles(Player target, AxeConfig settings) {
        try {
            Material mat = Material.matchMaterial(settings.bleedParticleMaterial);
            if (mat == null) mat = Material.REDSTONE_BLOCK;

            final String typeStr = settings.bleedParticleType != null ? settings.bleedParticleType.toUpperCase() : "ITEM";

            if ("BLOCK".equals(typeStr) || "BLOCK_CRUMB".equals(typeStr)) {
                target.getWorld().spawnParticle(
                    Particle.BLOCK,
                    target.getLocation().add(0, 1.0, 0),
                    settings.bleedParticleCount,
                    0.3, 0.5, 0.3,
                    settings.bleedParticleSpeed,
                    mat.createBlockData()
                );
            } else if ("DAMAGE_INDICATOR".equals(typeStr)) {
                target.getWorld().spawnParticle(
                    Particle.DAMAGE_INDICATOR,
                    target.getLocation().add(0, 1.0, 0),
                    settings.bleedParticleCount,
                    0.3, 0.5, 0.3,
                    settings.bleedParticleSpeed
                );
            } else {
                // Default: Particle.ITEM (blood item crumbs)
                target.getWorld().spawnParticle(
                    Particle.ITEM,
                    target.getLocation().add(0, 1.0, 0),
                    settings.bleedParticleCount,
                    0.3, 0.5, 0.3,
                    settings.bleedParticleSpeed,
                    new ItemStack(mat)
                );
            }
        } catch (Throwable ignored) {
            target.getWorld().spawnParticle(
                Particle.ITEM,
                target.getLocation().add(0, 1.0, 0),
                settings.bleedParticleCount,
                0.3, 0.5, 0.3,
                settings.bleedParticleSpeed,
                new ItemStack(Material.REDSTONE_BLOCK)
            );
        }
    }

    private void updateFloatingSkull(Player target, AxeConfig settings) {
        final UUID targetUuid = target.getUniqueId();
        final float finalAngleRad = (float) ((skullAngles.getOrDefault(targetUuid, 0.0f) + Math.toRadians(settings.skullRotationSpeedDegrees)) % (2.0 * Math.PI));
        skullAngles.put(targetUuid, finalAngleRad);

        final float yTranslation = (float) settings.skullHeightOffset;
        final float scale = (float) settings.skullScale;

        ItemDisplay display = skullDisplays.get(targetUuid);
        if (display == null || !display.isValid()) {
            final Location spawnLoc = target.getLocation().add(0, target.getHeight() + yTranslation, 0);
            spawnLoc.setPitch(0.0f);
            spawnLoc.setYaw(0.0f);

            display = target.getWorld().spawn(spawnLoc, ItemDisplay.class, entity -> {
                entity.setRotation(0.0f, 0.0f);
                entity.setItemStack(customSkullItem != null ? customSkullItem : new ItemStack(Material.PLAYER_HEAD));
                entity.setTransformation(new Transformation(
                    new Vector3f(0, yTranslation, 0),
                    new AxisAngle4f(finalAngleRad, 0, 1, 0),
                    new Vector3f(scale, scale, scale),
                    new AxisAngle4f(0, 0, 1, 0)
                ));
                entity.setBillboard(ItemDisplay.Billboard.FIXED);
                entity.setViewRange((float) (settings.skullViewDistanceBlocks / 64.0));
                entity.setMetadata("AxeExecutionerSkull", new org.bukkit.metadata.FixedMetadataValue(plugin, true));
            });

            target.addPassenger(display);
            skullDisplays.put(targetUuid, display);
        } else {
            // Ensure passenger mounting is maintained and rotation remains level (0 pitch/yaw)
            display.setRotation(0.0f, 0.0f);
            if (!target.getPassengers().contains(display)) {
                target.addPassenger(display);
            }

            display.setTransformation(new Transformation(
                new Vector3f(0, yTranslation, 0),
                new AxisAngle4f(finalAngleRad, 0, 1, 0),
                new Vector3f(scale, scale, scale),
                new AxisAngle4f(0, 0, 1, 0)
            ));
        }
    }

    private void endUltimate(Player attacker, Strength plugin) {
        // Despawn all floating skull ItemDisplays
        for (ItemDisplay display : skullDisplays.values()) {
            if (display != null && display.isValid()) {
                display.remove();
            }
        }
        skullDisplays.clear();
        skullAngles.clear();

        final UUID attackerUuid = attacker.getUniqueId();
        AxeAbilityListener.activeUltimateAttackers.remove(attackerUuid);

        final Map<UUID, Double> damageMap = AxeAbilityListener.storedDamagePools.remove(attackerUuid);
        final AxeConfig settings = plugin.getConfigHandler().getAxeConfig();

        if (damageMap != null) {
            for (Map.Entry<UUID, Double> entry : damageMap.entrySet()) {
                final Player target = plugin.getServer().getPlayer(entry.getKey());
                if (target != null && target.isOnline() && !target.isDead()) {
                    final double rawDamage = entry.getValue() * settings.damageMultiplier;

                    // Multi-Totem & Totem Bypass Guardrail: Cap final burst damage to player's current health + absorption
                    final double currentHealth = target.getHealth();
                    final double absorption = target.getAbsorptionAmount();
                    final double healthPool = currentHealth + absorption;
                    final double finalCappedDamage = Math.min(rawDamage, healthPool);

                    if (finalCappedDamage > 0.0) {
                        target.damage(finalCappedDamage, attacker);
                    }
                }
            }
        }

        if (attacker.isOnline()) {
            attacker.sendMessage(ColorParser.of(settings.ultimateExpiredMessage).build());
        }
    }
}
