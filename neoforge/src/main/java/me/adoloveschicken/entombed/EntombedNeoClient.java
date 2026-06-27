package me.adoloveschicken.entombed;

import me.adoloveschicken.entombed.config.screen.YaclConfigBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = Entombed.MODID)
public class EntombedNeoClient {

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("entombed")
                        .then(Commands.literal("config")
                                .executes(context -> {
                                    Minecraft.getInstance().tell(() ->
                                            Minecraft.getInstance().setScreen(YaclConfigBuilder.createScreen(null))
                                    );
                                    return 1;
                                })
                        )
        );
    }
}