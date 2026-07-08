package me.adoloveschicken.entombed.integration.galosphere;

import net.minecraft.world.item.ItemStack;

public class GalosphereHelper {
    public static boolean hasPreservedComponent(ItemStack stack) {
        return stack.getComponents().stream()
                .anyMatch(typed -> typed.type().toString().contains("preserved"));
    }
}
