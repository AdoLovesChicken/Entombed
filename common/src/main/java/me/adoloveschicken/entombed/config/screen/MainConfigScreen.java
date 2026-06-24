//package me.adoloveschicken.entombed.config.screen;
//
//import me.adoloveschicken.entombed.config.Config;
//import me.adoloveschicken.entombed.config.ConfigData;
//import net.minecraft.client.gui.components.Button;
//import net.minecraft.client.gui.components.CycleButton;
//import net.minecraft.client.gui.screens.Screen;
//import net.minecraft.network.chat.CommonComponents;
//import net.minecraft.network.chat.Component;
//
//import java.lang.reflect.Field;
//
//public class MainConfigScreen extends Screen {
//    private final Screen parent;
//
//    public MainConfigScreen(Screen parent) {
//        super(Component.translatable("entombed.gui.config"));
//        this.parent = parent;
//    }
//
//    @Override
//    public void onClose() {
//        Config.save();
//        minecraft.setScreen(parent);
//    }
//
//    @Override
//    protected void init() {
//        int y = 40;
//        for (Field field : ConfigData.class.getDeclaredFields()) {
//            if (field.getType() == boolean.class) {
//                boolean value;
//                try { value = field.getBoolean(null); } catch (Exception e) { continue; }
//                boolean finalValue = value;
//                this.addRenderableWidget(
//                        CycleButton.onOffBuilder(finalValue)
//                                .create(this.width / 2 - 155, y, 150, 20,
//                                        Component.literal(field.getName()),
//                                        (button, val) -> {
//                                            try { field.setBoolean(null, val); } catch (Exception ignored) {}
//                                        })
//                );
//            } else if (field.getType() == short.class) {
//
//            } else if (field.getType() == ConfigData.DropBehavior.class) {
//                ConfigData.DropBehavior behavior;
//                try {
//                    behavior = field.get(ConfigData.DropBehavior);
//                } catch (Exception e) {
//                    continue;
//                }
//            }
//            y += 25;
//        }
//
//        // Done button
//        this.addRenderableWidget(
//                Button.builder(CommonComponents.GUI_DONE, btn -> onClose())
//                        .bounds(this.width / 2 - 100, this.height - 27, 200, 20)
//                        .build()
//        );
//    }
//}
