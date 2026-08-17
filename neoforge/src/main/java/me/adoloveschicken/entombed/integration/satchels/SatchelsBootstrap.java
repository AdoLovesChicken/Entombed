package me.adoloveschicken.entombed.integration.satchels;

import me.adoloveschicken.entombed.api.TombIntegrationRegistry;

public class SatchelsBootstrap {
    private SatchelsBootstrap() {}

    public static void register() {
        TombIntegrationRegistry.register(new SatchelsHandler());
    }
}
