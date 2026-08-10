package lunatech.strength.api;

import org.jetbrains.annotations.ApiStatus;

/**
 * The StrengthAPI class is the main entry point for accessing the Strength API.
 */
public abstract class StrengthAPI {
    private static StrengthAPI INSTANCE;

    /**
     * Gets the instance of the StrengthAPI.
     *
     * @return the instance of StrengthAPI
     * @since 1.0.0
     */
    public static StrengthAPI getInstance() {
        if (INSTANCE == null)
            throw new RuntimeException("API was accessed before being initialized!");
        return INSTANCE;
    }

    /**
     * Sets the instance of the StrengthAPI.
     * This method is intended for internal use by the api provider only.
     *
     * @param api the instance of StrengthAPI to set
     * @since 1.0.0
     */
    @ApiStatus.Internal
    protected static void setInstance(StrengthAPI api) {
        INSTANCE = api;
    }
}
