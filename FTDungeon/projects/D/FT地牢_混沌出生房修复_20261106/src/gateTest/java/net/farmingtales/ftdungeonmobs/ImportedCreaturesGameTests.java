package net.farmingtales.ftdungeonmobs;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftdungeoncreaturetests")
@PrefixGameTestTemplate(false)
public final class ImportedCreaturesGameTests {
    @GameTest(template = "empty", timeoutTicks = 80)
    public static void eggsAttributesCombatAndPersistence(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ImportedMonsterEntity ghost = ImportedCreatures.GHOST_SCREAMER.get().create(level);
        ImportedMonsterEntity ore = ImportedCreatures.ORE_MONSTER.get().create(level);
        helper.assertTrue(ghost != null && ore != null, "Both monsters are registered with native factories");
        helper.assertTrue(ghost.getMaxHealth() == 24 && ore.getMaxHealth() == 40, "Native monster attributes are installed");
        helper.assertTrue(ore.getAttributeValue(Attributes.ARMOR) == 8, "Ore monster has armor");
        for (var egg : ImportedCreatures.EGGS) {
            helper.assertTrue(egg.get() instanceof net.minecraftforge.common.ForgeSpawnEggItem, "Each creature has a real spawn egg");
        }
        SkeletonMerchantEntity merchant = ImportedCreatures.SKELETON_MERCHANT.get().create(level);
        helper.assertTrue(merchant != null && merchant.getOffers().size() == 3, "Spawn egg merchant has working offers");
        helper.assertTrue(merchant.getOffers().get(0).getResult().is(Items.BREAD), "Merchant sells original supplies");
        merchant.getOffers().get(0).increaseUses();
        CompoundTag saved = merchant.saveWithoutId(new CompoundTag());
        SkeletonMerchantEntity restored = ImportedCreatures.SKELETON_MERCHANT.get().create(level);
        restored.load(saved);
        helper.assertTrue(restored.getOffers().get(0).getUses() == 1, "Trade uses survive save and reload");
        helper.assertTrue(restored.getUUID().equals(merchant.getUUID()), "Merchant UUID survives save and reload");
        helper.assertTrue(ghost.doHurtTarget(ore), "Monster melee attacks work with GeckoLib animation trigger");
        helper.assertTrue(ore.getHealth() < ore.getMaxHealth(), "Melee attack applies damage");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void existingDungeonMerchantMigration(GameTestHelper helper) {
        migration(helper, helper.getLevel().getServer().getLevel(DungeonGateTravel.DUNGEON));
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void existingEclipseMerchantMigration(GameTestHelper helper) {
        migration(helper, helper.getLevel().getServer().getLevel(eclipse.generator.EclipseMod.DIMENSION));
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void existingChaosMerchantMigration(GameTestHelper helper) {
        var dimension = net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,
                new net.minecraft.resources.ResourceLocation("ftmaze", "depths"));
        migration(helper, helper.getLevel().getServer().getLevel(dimension));
    }

    private static void migration(GameTestHelper helper, ServerLevel dungeon) {
        helper.assertTrue(dungeon != null, "Dungeon dimension exists");
        dungeon.setChunkForced(128, 128, true);
        dungeon.getChunk(128, 128);
        helper.runAfterDelay(5, () -> spawnMigration(helper, dungeon));
    }

    private static void spawnMigration(GameTestHelper helper, ServerLevel dungeon) {
        Villager original = EntityType.VILLAGER.create(dungeon);
        original.moveTo(2048.5, 90, 2048.5, 135, 0);
        original.setCustomName(Component.literal("Preserved dungeon merchant"));
        original.setInvulnerable(true);
        original.setNoAi(true);
        original.addTag("fteclipsegen.merchant");
        original.getPersistentData().putString("migration_test", "preserved");
        SkeletonMerchantEntity stock = ImportedCreatures.SKELETON_MERCHANT.get().create(dungeon);
        original.setOffers(stock.getOffers());
        original.getOffers().get(1).increaseUses();
        var migrated = new java.util.concurrent.atomic.AtomicReference<SkeletonMerchantEntity>();
        java.util.function.Consumer<net.minecraftforge.event.entity.EntityJoinLevelEvent> listener = event -> {
            if (event.getEntity() instanceof SkeletonMerchantEntity merchant && merchant.getUUID().equals(original.getUUID())) {
                migrated.set(merchant);
            }
        };
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(listener);
        try {
            helper.assertTrue(dungeon.addFreshEntity(original), "Original merchant is added to the native entity manager");
            original.tick();
            verifyMigration(helper, dungeon, original, migrated.get());
        } finally {
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(listener);
        }
    }

    private static void verifyMigration(GameTestHelper helper, ServerLevel dungeon, Villager original, SkeletonMerchantEntity merchant) {
        helper.assertTrue(merchant != null && merchant.isAddedToWorld() && original.isRemoved(), "Existing dungeon villagers become skeleton merchants in the native entity manager");
        helper.assertTrue(merchant.getUUID().equals(original.getUUID()), "Migration preserves merchant UUID");
        helper.assertTrue(merchant.getOffers().size() == 3 && merchant.getOffers().get(1).getUses() == 1, "Migration preserves offers and trade uses");
        helper.assertTrue(merchant.getCustomName().getString().equals("Preserved dungeon merchant"), "Migration preserves name");
        helper.assertTrue(merchant.isInvulnerable() && merchant.isNoAi(), "Migration preserves protection and AI flags");
        helper.assertTrue(merchant.getPersistentData().getString("migration_test").equals("preserved"), "Migration preserves Forge persistent data");
        helper.assertTrue(merchant.getTags().contains("fteclipsegen.merchant") && merchant.getYRot() == 135, "Migration preserves tracking tags and facing");
        merchant.discard();
        Villager ordinary = EntityType.VILLAGER.create(helper.getLevel());
        ordinary.moveTo(helper.absolutePos(new net.minecraft.core.BlockPos(1, 2, 1)), 0, 0);
        helper.getLevel().addFreshEntity(ordinary);
        helper.assertTrue(helper.getLevel().getEntity(ordinary.getUUID()) == ordinary, "Overworld villagers remain villagers");
        ordinary.discard();
        dungeon.setChunkForced(128, 128, false);
        helper.succeed();
    }
}
