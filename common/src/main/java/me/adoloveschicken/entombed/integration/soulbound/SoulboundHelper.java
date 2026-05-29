package me.adoloveschicken.entombed.integration.soulbound;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class SoulboundHelper {
    public static final ResourceKey<Enchantment> SOULBOUND = ResourceKey.create(
            Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(
                    "soulbound_enchantment", "soulbound"
            ));

    public static boolean hasSoulboundEnchantment(ItemStack stack, RegistryAccess registryAccess) {
        try {
            return EnchantmentHelper.getItemEnchantmentLevel(
                    registryAccess.lookupOrThrow(Registries.ENCHANTMENT)
                            .getOrThrow(SOULBOUND), stack
            ) > 0;
        } catch (IllegalStateException e) {
            return false;
        }
    }
}
