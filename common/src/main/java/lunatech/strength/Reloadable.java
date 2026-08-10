package lunatech.strength;

/**
 * Implemented in classes that should support being reloaded IE executing the methods during runtime after startup.
 */
public interface Reloadable {
    /**
     * On plugin load.
     */
    default void onLoad(AbstractStrength plugin) {
    }

    /**
     * On plugin enable.
     */
    default void onEnable(AbstractStrength plugin) {
    }

    /**
     * On plugin disable.
     */
    default void onDisable(AbstractStrength plugin) {
    }

}
