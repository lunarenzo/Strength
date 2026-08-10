package lunatech.strength.utility;


import lunatech.strength.AbstractStrength;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;

/**
 * A class that provides shorthand access to {@link AbstractStrength#getComponentLogger}.
 */
public class Logger {
    /**
     * Get component logger. Shorthand for:
     *
     * @return the component logger {@link AbstractStrength#getComponentLogger}.
     */
    @NotNull
    public static ComponentLogger get() {
        return AbstractStrength.getInstance().getComponentLogger();
    }
}
