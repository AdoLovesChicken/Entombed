package me.adoloveschicken.entombed.config;

import dev.isxander.yacl3.api.NameableEnum;
import net.minecraft.network.chat.Component;

public class ConfigData {
    public ConfigData() {
    }

    public static boolean usedYACL = true;
    /*-- Vanilla --*/
    // Tomb settings
    public static boolean tombsHaveCollision = true;
    public static boolean requireOpForRetrieve = true;
    public static boolean tombsCanBeBrokenDirectly = false;
    public static boolean tombsCanBeBrokenIndirectly = false;

    // Placement
    public static boolean tombsCanPlaceInLiquid = false;

    // Keep items, xp:
    public static DropBehavior itemsOnDeath = DropBehavior.TOMBED;
    public static DropBehavior mainInvOnDeath = DropBehavior.DEFAULT;
    public static DropBehavior hotbarOnDeath = DropBehavior.DEFAULT;
    public static DropBehavior armorOnDeath = DropBehavior.DEFAULT;
    public static DropBehavior experienceOnDeath = DropBehavior.PARTIAL;

    public static short itemPercentKept = 100;
    public static short mainInvPercentKept = 100;
    public static short hotbarPercentKept = 100;
    public static short armorPercentKept = 100;
    public static short experiencePercentKept = 100;

    /*-- Sable, Simulated, Aeronautics --*/
    public static boolean tombsCanBecomeSublevel = true;
    public static boolean tombsFloatInLiquid = false; // overrides tombsCanPlaceInLiquid to false
    public static boolean allTombsAreSublevel = false;

    /*-- Other Integrations --*/

    public enum DropBehavior implements NameableEnum {
        KEPT { // KeepInventory behavior
            @Override
            public boolean shouldStore() {
                return false;
            }
            @Override
            public boolean shouldKeep() {
                return true;
            }
        },
        TOMBED { // Stored into Tomb
            @Override
            public boolean shouldStore() {
                return true;
            }
        },
        PARTIAL { // Stores tools, armor, not other items. For exp, stores the vanilla amount given on death
            @Override
            public boolean shouldStore() { return true; }
            @Override
            public boolean isPartial() { return true; }
        },
        PERCENT_KEPT { // A percent of items, exp kept
            @Override
            public boolean shouldStore() { return true; }
        },
        VOIDED { // Items are lost, voided
            @Override
            public boolean shouldStore() { return false; }
        },
        DEFAULT { // Refers to itemsOnDeath value
            @Override
            public boolean shouldStore() { return ConfigData.itemsOnDeath.shouldStore(); }
            @Override
            public boolean shouldKeep() { return ConfigData.itemsOnDeath.shouldKeep(); }
            @Override
            public boolean isPartial() { return ConfigData.itemsOnDeath.isPartial(); }
            @Override
            public short getPercent(PercentSource source) {
                return ConfigData.itemsOnDeath.getPercent(
                        source == PercentSource.EXPERIENCE ? PercentSource.EXPERIENCE : PercentSource.ITEMS
                );
            }
        };

        @Override
        public Component getDisplayName() {
            return Component.translatable("entombed.config.dropBehavior." + name());
        }

        public boolean shouldStore() { return false; }
        public boolean shouldKeep() { return false; }
        public boolean isPartial() { return false; }
        public short getPercent(PercentSource source) {
            return switch (source) {
                case ITEMS -> ConfigData.itemPercentKept;
                case MAIN_INV -> ConfigData.mainInvPercentKept;
                case HOTBAR -> ConfigData.hotbarPercentKept;
                case ARMOR -> ConfigData.armorPercentKept;
                case EXPERIENCE -> ConfigData.experiencePercentKept;
            };
        }
    }

    public static void setItemsOnDeath(DropBehavior value) {
        ConfigData.itemsOnDeath = (value == DropBehavior.DEFAULT) ? DropBehavior.TOMBED : value;
    }

    public static void setLiquidProperties(boolean floatInLiquid) {
        if (floatInLiquid) ConfigData.tombsCanPlaceInLiquid = true;
    }

    public enum PercentSource { ITEMS, MAIN_INV, HOTBAR, ARMOR, EXPERIENCE }

    public static boolean isPercentKept(DropBehavior behavior) {
        if (behavior == DropBehavior.PERCENT_KEPT) return true;
        if (behavior == DropBehavior.DEFAULT) return itemsOnDeath == DropBehavior.PERCENT_KEPT;
        return false;
    }

}
