package me.adoloveschicken.entombed.config;

public class ConfigData {
    public ConfigData() {
    }

    /*-- Vanilla --*/
    // Tomb settings
    public static boolean tombsHaveCollision = true;
    public static boolean requireOpForRetrieve = true;
    public static boolean tombsCanBeBrokenDirectly = false;
    public static boolean tombsCanBeBrokenIndirectly = false;

    // Keep items, xp:
    public static DropBehavior itemsOnDeath = DropBehavior.TOMBED;
    public static DropBehavior mainInvOnDeath = DropBehavior.DEFAULT;
    public static DropBehavior hotbarOnDeath = DropBehavior.DEFAULT;
    public static DropBehavior armorOnDeath = DropBehavior.DEFAULT;
    public static DropBehavior experienceOnDeath = DropBehavior.PARTIAL;

    public static short itemPercentKept = 100;
    public static short durabilityPercent = 100;
    public static short experiencePercentKept = 100;

    /*-- Sable, Simulated, Aeronautics --*/
    public static boolean tombsCanBecomeSublevel = true;
    public static boolean tombsFloatInLiquid = true;
    public static boolean allTombsAreSublevel = false;

    /*-- Other Integrations --*/

    public enum DropBehavior {
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
        DURABILITY_PERCENT { // A percent of durability kept
            @Override
            public boolean shouldStore() { return true; }
            @Override
            public short getDurabilityPercent() { return ConfigData.durabilityPercent; }
        },
        PERCENT_KEPT { // A percent of items, exp kept
            @Override
            public boolean shouldStore() { return true; }
            @Override
            public short getPercent(boolean isExperience) { return isExperience ? ConfigData.experiencePercentKept : ConfigData.itemPercentKept; }
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
            public short getDurabilityPercent() { return ConfigData.itemsOnDeath.getDurabilityPercent(); }
            @Override
            public short getPercent(boolean isExperience) { return ConfigData.itemsOnDeath.getPercent(isExperience); }
        };

        public boolean shouldStore() { return false; }
        public boolean shouldKeep() { return false; }
        public boolean isPartial() { return false; }
        public short getPercent(boolean isExperience) { return 100; }
        public short getDurabilityPercent() { return 100; }
    }
}
