package lunatech.strength.constant;

import org.bukkit.NamespacedKey;

/**
 * Constants for Persistent Data Container (PDC) keys.
 */
public final class PDCKeys {
    public static final NamespacedKey STRENGTH = new NamespacedKey("strengthsmp", "strength");
    public static final NamespacedKey ITEM_STRENGTH = new NamespacedKey("strengthsmp", "item_strength");

    private PDCKeys() {
        throw new UnsupportedOperationException("Constant class");
    }
}
