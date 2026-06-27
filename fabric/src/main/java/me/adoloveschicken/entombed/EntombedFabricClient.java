package me.adoloveschicken.entombed;

import me.adoloveschicken.entombed.block.CommonModBlocks;
import me.adoloveschicken.entombed.config.screen.YaclConfigBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;

public class EntombedFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(CommonModBlocks.TOMB, RenderType.cutout());

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                    ClientCommandManager.literal("entombed")
                            .then(ClientCommandManager.literal("config")
                                    .executes(context -> {
                                        Minecraft.getInstance().tell(() ->
                                                Minecraft.getInstance().setScreen(YaclConfigBuilder.createScreen(null))
                                        );
                                        return 1;
                                    })
                            )
            );
        });
    }
}