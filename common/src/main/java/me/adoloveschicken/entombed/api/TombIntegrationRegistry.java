package me.adoloveschicken.entombed.api;

import me.adoloveschicken.entombed.Entombed;

import java.util.ArrayList;
import java.util.List;

public class TombIntegrationRegistry {
    private static final List<TombIntegration> integrations = new ArrayList<>();

    public static void register(TombIntegration integration) {
        integrations.add(integration);
        Entombed.LOGGER.info("Registered tomb integration {}", integration.integrationId());
    }

    public static List<TombIntegration> getIntegrations() {
        return integrations;
    }
}
