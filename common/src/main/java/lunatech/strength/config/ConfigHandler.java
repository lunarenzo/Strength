package lunatech.strength.config;

import lunatech.strength.AbstractStrength;
import lunatech.strength.Reloadable;
import lunatech.strength.config.loading.ConfigLoader;
import org.slf4j.Logger;

import java.nio.file.Path;

/**
 * A class that generates/loads & provides access to main and decoupled weapon configuration files.
 */
public class ConfigHandler implements Reloadable {
    private final AbstractStrength plugin;
    private final Path configDir;
    private final Logger logger;

    private PluginConfig cfg;
    private TridentConfig tridentCfg;
    private BowConfig bowCfg;
    private ShieldConfig shieldCfg;

    /**
     * Instantiates a new Config handler.
     *
     * @param plugin the plugin instance
     */
    public ConfigHandler(AbstractStrength plugin) {
        this.plugin = plugin;
        this.configDir = plugin.getDataFolder().toPath();
        this.logger = plugin.getComponentLogger();
    }

    public ConfigHandler(AbstractStrength plugin, Path configDir, Logger logger) {
        this.plugin = plugin;
        this.configDir = configDir;
        this.logger = logger;
    }

    @Override
    public void onLoad(AbstractStrength plugin) {
        // 1. Load general plugin configuration
        cfg = new ConfigLoader()
            .withLogger(logger)
            .withDirectory()
            .withPath(configDir.resolve("config.yml"))
            .withHeader("StrengthSMP Core Configuration")
            .build(PluginConfig.class);

        final Path weaponsDir = configDir.resolve("weapons");

        // 2. Load decoupled Trident configuration
        tridentCfg = new ConfigLoader()
            .withLogger(logger)
            .withDirectory()
            .withPath(weaponsDir.resolve("trident.yml"))
            .withHeader("Trident Weapon Configuration")
            .build(TridentConfig.class);

        // 3. Load decoupled Bow configuration
        bowCfg = new ConfigLoader()
            .withLogger(logger)
            .withDirectory()
            .withPath(weaponsDir.resolve("bow.yml"))
            .withHeader("Bow Weapon Configuration")
            .build(BowConfig.class);

        // 4. Load decoupled Shield configuration
        shieldCfg = new ConfigLoader()
            .withLogger(logger)
            .withDirectory()
            .withPath(weaponsDir.resolve("shield.yml"))
            .withHeader("Shield Weapon Configuration")
            .build(ShieldConfig.class);
    }

    /**
     * Gets main config object.
     *
     * @return the config object
     */
    public PluginConfig getConfig() {
        return cfg;
    }

    /**
     * Gets trident weapon configuration.
     *
     * @return the trident config
     */
    public TridentConfig getTridentConfig() {
        return tridentCfg;
    }

    /**
     * Gets bow weapon configuration.
     *
     * @return the bow config
     */
    public BowConfig getBowConfig() {
        return bowCfg;
    }

    /**
     * Gets shield weapon configuration.
     *
     * @return the shield config
     */
    public ShieldConfig getShieldConfig() {
        return shieldCfg;
    }
}
