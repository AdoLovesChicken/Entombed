package me.adoloveschicken.entombed.integration.jade;

import me.adoloveschicken.entombed.Entombed;
import me.adoloveschicken.entombed.block.GravestoneBlock;
import me.adoloveschicken.entombed.block.GravestoneBlockEntity;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class TombPlugin implements IWailaPlugin{
    public static final ResourceLocation TOMB_OWNER = ResourceLocation.fromNamespaceAndPath(Entombed.MODID, "tomb_owner");

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(TombComponentProvider.INSTANCE, GravestoneBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(TombComponentProvider.INSTANCE, GravestoneBlock.class);
    }
}
