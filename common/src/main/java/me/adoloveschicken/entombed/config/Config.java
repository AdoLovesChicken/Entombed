//package me.adoloveschicken.entombed.config;
//
//public class Config {
//    /*-- Vanilla --*/
//    // if the player can walk through the tomb or not
//    public static boolean tombsHaveHitboxes = true;
//    // if player needs Operator permissions for right-click retrieval if it is another player's tomb
//    public static boolean requireOpForRetrieve = true;
//    // Keep items, xp:
//    // itemsOnDeath behavior will break if set to DropBehavior.DEFAULT;
//    public static DropBehavior itemsOnDeath = DropBehavior.TOMBED;
//    public static itemPercentKept
//    public static DropBehavior mainInvOnDeath = DropBehavior.DEFAULT;
//    public static DropBehavior hotbarOnDeath = DropBehavior.DEFAULT;
//    public static DropBehavior armorOnDeath = DropBehavior.DEFAULT;
//
//    public static DropBehavior experienceOnDeath = DropBehavior.PARTIAL;
//    // how much exp is kept on
//    public static float durabilityPercent = 100f;
//
//    /*-- Sable, Simulated, Aeronautics --*/
//    // if tombs can become sub-levels (ignored for safe-placement ON sub-levels)
//    public static boolean tombsCanBecomeSublevel = true;
//
//    /*-- Other Integrations --*/
//
//
//
//    public enum DropBehavior {
//        KEPT, // KeepInventory behavior
//        TOMBED, // Stored into Tomb
//        PARTIAL, // Stores tools, armor, not other items. For exp, stores the vanilla amount given on death
//        DURABILITY_PERCENT, // A percent of durability kept
//        PERCENT_KEPT, // A percent of items, exp kept
//        VOIDED, // Items are lost, voided
//        DEFAULT // Refers to itemsOnDeath value
//    }
//}
