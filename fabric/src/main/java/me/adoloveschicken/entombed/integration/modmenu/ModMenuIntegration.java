package me.adoloveschicken.entombed.integration.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.adoloveschicken.entombed.config.screen.YaclConfigBuilder;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return YaclConfigBuilder::createScreen;
    }
}