package me.adoloveschicken.entombed;

import me.adoloveschicken.entombed.block.CommonModBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.RenderType;

public class EntombedFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(CommonModBlocks.TOMB, RenderType.cutout());
    }
}