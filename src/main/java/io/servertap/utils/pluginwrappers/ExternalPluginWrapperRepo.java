package io.servertap.utils.pluginwrappers;

import io.servertap.ServerTapMain;

import java.util.logging.Logger;

public class ExternalPluginWrapperRepo {

    private final EconomyWrapper economyWrapper;
    private final LuckpermsWrapper luckpermsWrapper;

    public ExternalPluginWrapperRepo(ServerTapMain main, Logger logger) {
        this.economyWrapper = new EconomyWrapper(main, logger);
        this.luckpermsWrapper = new LuckpermsWrapper(main, logger);
    }

    public EconomyWrapper getEconomyWrapper() {
        return this.economyWrapper;
    }
    public LuckpermsWrapper getLuckpermsWrapper() { return  this.luckpermsWrapper; }
}
