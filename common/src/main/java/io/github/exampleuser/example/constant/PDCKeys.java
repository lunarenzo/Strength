package io.github.exampleuser.example.constant;

import org.bukkit.NamespacedKey;

/**
 * Constants for Persistent Data Container (PDC) keys.
 */
public final class PDCKeys {
    public static final NamespacedKey STRENGTH = new NamespacedKey("strengthsmp", "strength");

    private PDCKeys() {
        throw new UnsupportedOperationException("Constant class");
    }
}
