package lunatech.strength.constant;

import org.bukkit.NamespacedKey;

/**
 * Constants for Persistent Data Container (PDC) keys.
 */
public final class PDCKeys {
    public static final NamespacedKey STRENGTH = new NamespacedKey("strengthsmp", "strength");
    public static final NamespacedKey ITEM_STRENGTH = new NamespacedKey("strengthsmp", "item_strength");
    public static final NamespacedKey ASSIGNED_WEAPON = new NamespacedKey("strengthsmp", "assigned_weapon");
    public static final NamespacedKey STRENGTH_RECIPE = new NamespacedKey("strengthsmp", "strength_item_recipe");
    public static final NamespacedKey ITEM_REROLL = new NamespacedKey("strengthsmp", "item_reroll");
    public static final NamespacedKey REROLL_RECIPE = new NamespacedKey("strengthsmp", "reroll_item_recipe");
    public static final NamespacedKey UPGRADED_GEAR = new NamespacedKey("strengthsmp", "upgraded_gear");
    public static final NamespacedKey SPEAR_PRE_ULT_ENCHANTS = new NamespacedKey("strengthsmp", "spear_pre_enchants");
    public static final NamespacedKey CROSSBOW2_SHOT = new NamespacedKey("strengthsmp", "crossbow2_shot");
    public static final NamespacedKey CROSSBOW2_PRE_ULT_ENCHANTS = new NamespacedKey("strengthsmp", "crossbow2_pre_enchants");
    public static final NamespacedKey CROSSBOW2_TEMP_ULT = new NamespacedKey("strengthsmp", "crossbow2_temp_ult");

    private PDCKeys() {
        throw new UnsupportedOperationException("Constant class");
    }
}
