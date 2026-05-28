package me.adoloveschicken.entombed.block;

import me.adoloveschicken.entombed.Entombed;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Entombed.MODID);

    public static final DeferredBlock<Block> TOMB = BLOCKS.register("tomb",
            () -> new GravestoneBlock(BlockBehaviour.Properties.of()
                    .noOcclusion()
                    .destroyTime(0.5f)
                    .sound(SoundType.STONE)
            ));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        eventBus.addListener((FMLCommonSetupEvent event) -> {
            CommonModBlocks.TOMB = TOMB.get();
        });
    }
}
