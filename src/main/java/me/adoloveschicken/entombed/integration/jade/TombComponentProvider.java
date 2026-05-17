package me.adoloveschicken.entombed.integration.jade;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import me.adoloveschicken.entombed.Entombed;
import me.adoloveschicken.entombed.block.GravestoneBlockEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

import java.util.Optional;

public enum TombComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        if (blockAccessor.getServerData().contains("Owner")) {
            CompoundTag serverData = blockAccessor.getServerData();
            IElementHelper elements = IElementHelper.get();
            if (serverData.getString("Owner").equals("Unknown")) {
                IElement icon = elements.item(new ItemStack(Items.SKELETON_SKULL), 0.5f).size(new Vec2(10, 10)).translate(new Vec2(0, -1));
                icon.message(null);
                iTooltip.add(icon);
                iTooltip.append(Component.translatable("entombed.tomb_unclaimed"));
            } else {
                ItemStack ownerSkull = new ItemStack(Items.PLAYER_HEAD);
                if (serverData.contains("OwnerUUID")) {
                    ownerSkull.set(DataComponents.PROFILE, new ResolvableProfile(
                            Optional.of(serverData.getString("Owner")),
                            Optional.of(serverData.getUUID("OwnerUUID")),
                            new PropertyMap()));
                } else {
                    ownerSkull.set(DataComponents.PROFILE, new ResolvableProfile(
                            Optional.of(serverData.getString("Owner")),
                            Optional.empty(),
                            new PropertyMap()));
                }
                IElement icon = elements.item(ownerSkull, 0.5f).size(new Vec2(10, 10)).translate(new Vec2(0, -1));
                icon.message(null);
                iTooltip.add(icon);
                iTooltip.append(Component.translatable("entombed.tomb_owner", blockAccessor.getServerData().getString("Owner")));
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return TombPlugin.TOMB_OWNER;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor blockAccessor) {
        GravestoneBlockEntity tomb = (GravestoneBlockEntity) blockAccessor.getBlockEntity();
        if (tomb.getOwnerUUID() == null) {
            data.putString("Owner", "Unknown");
            return;
        }
        MinecraftServer server = blockAccessor.getLevel().getServer();
        Optional<GameProfile> profile = server.getProfileCache().get(tomb.getOwnerUUID());
        if (profile.isPresent()) {
            String name = profile.get().getName();
            data.putString("Owner", name);
            data.putUUID("OwnerUUID", tomb.getOwnerUUID());
        } else {
            String storedName = tomb.getOwnerName();
            if (storedName != null && !storedName.isEmpty()) {
                data.putString("Owner", storedName);
            }
        }
    }
}
