package me.adoloveschicken.entombed.block;

import me.adoloveschicken.entombed.Entombed;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ModBlocks {
    public static void register() {
        CommonModBlocks.TOMB = Registry.register(
                BuiltInRegistries.BLOCK,
                ResourceLocation.fromNamespaceAndPath(Entombed.MODID, "tomb"),
                new GravestoneBlock(BlockBehaviour.Properties.of()
                        .noOcclusion()
                        .destroyTime(0.5f)
                        .sound(SoundType.STONE)
                )
        );
    }
}