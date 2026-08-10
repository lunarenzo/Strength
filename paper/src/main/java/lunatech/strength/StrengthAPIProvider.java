package lunatech.strength;

import lunatech.strength.api.StrengthAPI;

class StrengthAPIProvider extends StrengthAPI implements Reloadable {
    private final Strength plugin;

    StrengthAPIProvider(Strength plugin) {
        super();
        this.plugin = plugin;
        setInstance(this);
    }
}
