package me.adoloveschicken.entombed.command;

import com.mojang.brigadier.CommandDispatcher;
import me.adoloveschicken.entombed.api.TombIntegration;
import me.adoloveschicken.entombed.api.TombIntegrationRegistry;
import me.adoloveschicken.entombed.block.GravestoneBlockEntity;
import me.adoloveschicken.entombed.storage.GraveEntry;
import me.adoloveschicken.entombed.storage.GraveIndex;
import me.adoloveschicken.entombed.storage.GraveStorageManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class TombCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("tomb")
                        .then(Commands.literal("retrieve")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> {
                                    Player player = context.getSource().getPlayerOrException();
                                    RegistryAccess registryAccess = player.level().getServer().registryAccess();
                                    retrieveItems(player, player, registryAccess);

                                    return 1;
                                })
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(context -> {
                                            Player target = EntityArgument.getPlayer(context, "target");
                                            Player sender = context.getSource().getPlayerOrException();
                                            RegistryAccess registryAccess = sender.level().getServer().registryAccess();
                                            retrieveItems(target, sender, registryAccess);
                                            return 1;
                                        })
                                        .then(Commands.argument("recipient", EntityArgument.player())
                                                .executes(context -> {
                                                    Player target = EntityArgument.getPlayer(context, "target");
                                                    Player recipient = EntityArgument.getPlayer(context, "recipient");
                                                    RegistryAccess registryAccess = target.level().getServer().registryAccess();
                                                    retrieveItems(target, recipient, registryAccess);
                                                    return 1;
                                                })
                                        )
                                )
                        )
        );
    }

    private static void retrieveItems(Player target, Player recipient, RegistryAccess registryAccess) {
        GraveEntry entry = GraveIndex.getLastGrave(target.getUUID());
        if (entry != null) {
            // Try block entity first
            ResourceLocation dimensionId = ResourceLocation.parse(entry.getDimension());
            ServerLevel graveLevel = recipient.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, dimensionId));
            if (graveLevel != null) {
                BlockPos gravePos = new BlockPos(entry.getX(), entry.getY(), entry.getZ());
                if (graveLevel.getBlockEntity(gravePos) instanceof GravestoneBlockEntity grave) {
                    grave.restoreAll(recipient);
                    recipient.sendSystemMessage(Component.literal("Tomb retrieved successfully!"));
                    return;
                }
            }

            // Fallback: restore directly from file
            CompoundTag graveData = GraveStorageManager.loadGrave(entry.getGraveID());
            if (graveData != null) {
                GravestoneBlockEntity.restoreExperience(recipient, graveData);

                NonNullList<ItemStack> itemStacks = NonNullList.withSize(GravestoneBlockEntity.INVENTORY_SIZE, ItemStack.EMPTY);
                ContainerHelper.loadAllItems(graveData, itemStacks, registryAccess);
                for (int i = 0; i < itemStacks.size(); i++) {
                    ItemStack itemStack = itemStacks.get(i);
                    if (!itemStack.isEmpty()) GravestoneBlockEntity.restoreItem(recipient, i, itemStack.copy());
                }

                CompoundTag integrationsTag = graveData.getCompound("ModExtras");
                for (TombIntegration integration : TombIntegrationRegistry.getIntegrations()) {
                    integration.retrieveData(recipient, integrationsTag);
                }

                GraveStorageManager.markRetrieved(entry.getGraveID());
                GraveIndex.removeGrave(target.getUUID(), entry.getGraveID());
                recipient.sendSystemMessage(Component.literal("Tomb retrieved successfully!"));
            } else {
                recipient.sendSystemMessage(Component.literal("Could not load tomb data!"));
            }
        } else {
            recipient.sendSystemMessage(Component.literal("No tombs found for " + target.getName().getString()));
        }
    }
}
