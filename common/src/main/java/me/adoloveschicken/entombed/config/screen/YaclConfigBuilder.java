package me.adoloveschicken.entombed.config.screen;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import me.adoloveschicken.entombed.config.ConfigData;
import me.adoloveschicken.entombed.config.ConfigData.DropBehavior;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class YaclConfigBuilder {
    public static Screen createScreen(Screen parent) {
        YetAnotherConfigLib config = YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("entombed.gui.config"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("entombed.gui.category.vanilla"))
                        .option(boolOption("entombed.config.tombsHaveCollision",
                                () -> ConfigData.tombsHaveCollision, val -> ConfigData.tombsHaveCollision = val))
                        .option(boolOption("entombed.config.requireOpForRetrieve",
                                () -> ConfigData.requireOpForRetrieve, val -> ConfigData.requireOpForRetrieve = val))
                        .option(boolOption("entombed.config.tombsCanBeBrokenDirectly",
                                () -> ConfigData.tombsCanBeBrokenDirectly, val -> ConfigData.tombsCanBeBrokenDirectly = val))
                        .option(boolOption("entombed.config.tombsCanBeBrokenIndirectly",
                                () -> ConfigData.tombsCanBeBrokenIndirectly, val -> ConfigData.tombsCanBeBrokenIndirectly = val))
                        .option(boolOption("entombed.config.tombsCanPlaceInLiquid",
                                () -> ConfigData.tombsCanPlaceInLiquid, val -> ConfigData.tombsCanPlaceInLiquid = val))
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("entombed.gui.group.drop_behaviors"))
                                .option(dropBehaviorOption("entombed.config.itemsOnDeath",
                                        () -> ConfigData.itemsOnDeath, ConfigData::setItemsOnDeath, DropBehavior.TOMBED))
                                .option(dropBehaviorOption("entombed.config.mainInvOnDeath",
                                        () -> ConfigData.mainInvOnDeath, val -> ConfigData.mainInvOnDeath = val, DropBehavior.DEFAULT))
                                .option(dropBehaviorOption("entombed.config.hotbarOnDeath",
                                        () -> ConfigData.hotbarOnDeath, val -> ConfigData.hotbarOnDeath = val, DropBehavior.DEFAULT))
                                .option(dropBehaviorOption("entombed.config.armorOnDeath",
                                        () -> ConfigData.armorOnDeath, val -> ConfigData.armorOnDeath = val, DropBehavior.DEFAULT))
                                .option(dropBehaviorOption("entombed.config.experienceOnDeath",
                                        () -> ConfigData.experienceOnDeath, val -> ConfigData.experienceOnDeath = val, DropBehavior.PARTIAL))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("entombed.gui.group.percents"))
                                .option(percentOption("entombed.config.itemPercentKept",
                                        () -> ConfigData.itemPercentKept, val -> ConfigData.itemPercentKept = val,
                                        () -> ConfigData.isPercentKept(ConfigData.itemsOnDeath)))
                                .option(percentOption("entombed.config.mainInvPercentKept",
                                        () -> ConfigData.mainInvPercentKept, val -> ConfigData.mainInvPercentKept = val,
                                        () -> ConfigData.isPercentKept(ConfigData.mainInvOnDeath)))
                                .option(percentOption("entombed.config.hotbarPercentKept",
                                        () -> ConfigData.hotbarPercentKept, val -> ConfigData.hotbarPercentKept = val,
                                        () -> ConfigData.isPercentKept(ConfigData.hotbarOnDeath)))
                                .option(percentOption("entombed.config.armorPercentKept",
                                        () -> ConfigData.armorPercentKept, val -> ConfigData.armorPercentKept = val,
                                        () -> ConfigData.isPercentKept(ConfigData.armorOnDeath)))
                                .option(percentOption("entombed.config.experiencePercentKept",
                                        () -> ConfigData.experiencePercentKept, val -> ConfigData.experiencePercentKept = val,
                                        () -> ConfigData.isPercentKept(ConfigData.experienceOnDeath)))
                                .build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("entombed.gui.category.integrations"))
                        .option(boolOption("entombed.config.tombsCanBecomeSublevel",
                                () -> ConfigData.tombsCanBecomeSublevel, val -> ConfigData.tombsCanBecomeSublevel = val))
                        .option(boolOption("entombed.config.tombsFloatInLiquid",
                                () -> ConfigData.tombsFloatInLiquid, val -> ConfigData.tombsFloatInLiquid = val))
                        .option(boolOption("entombed.config.allTombsAreSublevel",
                                () -> ConfigData.allTombsAreSublevel, val -> ConfigData.allTombsAreSublevel = val))
                        .build())
                .build();
        return config.generateScreen(parent);
    }

    private static Option<Boolean> boolOption(String key, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        return Option.<Boolean>createBuilder()
                .name(Component.translatable(key))
                .binding(true, getter::get, setter::accept)
                .controller(opt -> BooleanControllerBuilder.create(opt)
                        .formatValue(val -> Component.translatable(val ? "options.on" : "options.off"))
                        .coloured(true))
                .build();
    }

    private static Option<DropBehavior> dropBehaviorOption(String key, Supplier<DropBehavior> getter, Consumer<DropBehavior> setter, DropBehavior binding) {
        return Option.<DropBehavior>createBuilder()
                .name(Component.translatable(key))
                .binding(binding, getter::get, setter::accept)
                .controller(opt -> EnumControllerBuilder.create(opt)
                        .enumClass(DropBehavior.class))
                .build();
    }

    private static Option<Integer> percentOption(String key, Supplier<Short> getter, Consumer<Short> setter, Supplier<Boolean> availableWhen) {
        return Option.<Integer>createBuilder()
                .name(Component.translatable(key))
                .binding(100,
                        () -> (int) getter.get(),
                        val -> setter.accept(val.shortValue()))
                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                        .range(0, 100)
                        .step(1)
                        .formatValue(val -> Component.literal(val + "%")))
                .available(availableWhen.get())
                .build();
    }
}