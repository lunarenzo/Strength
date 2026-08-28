package lunatech.strength.config;

import lunatech.strength.AbstractStrength;
import lunatech.strength.Reloadable;
import lunatech.strength.config.loading.ConfigLoader;
import org.slf4j.Logger;

import java.nio.file.Path;

/**
 * A class that generates/loads &amp; provides access to main and decoupled weapon configuration files.
 */
public class ConfigHandler implements Reloadable {
    private final AbstractStrength plugin;
    private final Path configDir;
    private final Logger logger;

    private PluginConfig cfg;
    private TridentConfig tridentCfg;
    private BowConfig bowCfg;
    private ShieldConfig shieldCfg;
    private CrossbowConfig crossbowCfg;
    private SwordConfig swordCfg;
    private AxeConfig axeCfg;
    private MaceConfig maceCfg;
    private PotionConfig potionCfg;
    private EnchantmentConfig enchantmentCfg;
    private RulesConfig rulesCfg;
    private WeaponsGuiConfig weaponsGuiCfg;

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

        // 5. Load decoupled Crossbow configuration
        crossbowCfg = new ConfigLoader()
            .withLogger(logger)
            .withDirectory()
            .withPath(weaponsDir.resolve("crossbow.yml"))
            .withHeader("Crossbow Weapon Configuration")
            .build(CrossbowConfig.class);

        // 6. Load decoupled Sword configuration
        swordCfg = new ConfigLoader()
            .withLogger(logger)
            .withDirectory()
            .withPath(weaponsDir.resolve("sword.yml"))
            .withHeader("Sword Weapon Configuration")
            .build(SwordConfig.class);

        // 7. Load decoupled Axe configuration
        axeCfg = new ConfigLoader()
            .withLogger(logger)
            .withDirectory()
            .withPath(weaponsDir.resolve("axe.yml"))
            .withHeader("Axe Weapon Configuration")
            .build(AxeConfig.class);

        // 8. Load decoupled Mace configuration
        maceCfg = new ConfigLoader()
            .withLogger(logger)
            .withDirectory()
            .withPath(weaponsDir.resolve("mace.yml"))
            .withHeader("Mace Weapon & Feature Configuration")
            .build(MaceConfig.class);

        // 9. Load decoupled Potion Restriction configuration
        potionCfg = new ConfigLoader()
            .withLogger(logger)
            .withDirectory()
            .withPath(configDir.resolve("potion-restrictions.yml"))
            .withHeader("Potion & Potion Effect Restriction Configuration")
            .build(PotionConfig.class);

        // 10. Load decoupled Enchantment Restriction configuration
        enchantmentCfg = new ConfigLoader()
            .withLogger(logger)
            .withDirectory()
            .withPath(configDir.resolve("enchantment-restrictions.yml"))
            .withHeader("Global Enchantment Restriction Configuration")
            .build(EnchantmentConfig.class);

        // 11. Load decoupled Modular Rules configuration
        rulesCfg = new ConfigLoader()
            .withLogger(logger)
            .withDirectory()
            .withPath(configDir.resolve("rules.yml"))
            .withHeader("Modular Server Rules Configuration")
            .build(RulesConfig.class);

        // 12. Load Weapons GUI configuration
        weaponsGuiCfg = new ConfigLoader()
            .withLogger(logger)
            .withDirectory()
            .withPath(configDir.resolve("weapons-gui.yml"))
            .withHeader("StrengthSMP Weapons GUI Configuration")
            .build(WeaponsGuiConfig.class);
    }

    /**
     * Gets modular rules configuration.
     *
     * @return the rules config
     */
    public RulesConfig getRulesConfig() {
        return rulesCfg;
    }

    /**
     * Gets enchantment restriction configuration.
     *
     * @return the enchantment config
     */
    public EnchantmentConfig getEnchantmentConfig() {
        return enchantmentCfg;
    }

    /**
     * Gets weapons GUI configuration.
     *
     * @return the weapons gui config
     */
    public WeaponsGuiConfig getWeaponsGuiConfig() {
        return weaponsGuiCfg;
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

    /**
     * Gets crossbow weapon configuration.
     *
     * @return the crossbow config
     */
    public CrossbowConfig getCrossbowConfig() {
        return crossbowCfg;
    }

    /**
     * Gets sword weapon configuration.
     *
     * @return the sword config
     */
    public SwordConfig getSwordConfig() {
        return swordCfg;
    }

    /**
     * Gets axe weapon configuration.
     *
     * @return the axe config
     */
    public AxeConfig getAxeConfig() {
        return axeCfg;
    }

    /**
     * Gets mace weapon configuration.
     *
     * @return the mace config
     */
    public MaceConfig getMaceConfig() {
        return maceCfg;
    }

    /**
     * Gets potion restriction configuration.
     *
     * @return the potion config
     */
    public PotionConfig getPotionConfig() {
        return potionCfg;
    }
}
