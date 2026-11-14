package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Grants configured FTB rewards through an inventory chest for a regular (non-Manor) FT boss.
 *
 * <p>This deliberately does not enable FTB Quests' global entity-crate switch: that switch
 * also rolls crates for unrelated mobs. The dedicated death hook keeps the reward scoped to
 * real FT bosses while preserving the boss' existing statue, weapon, spell and relic drops.
 */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class NonManorLootCrateDrops {
    public static final String CRATE_TYPE = "ftboss_non_manor";

    private NonManorLootCrateDrops() {}

    /**
     * LOWEST lets revive/totem handlers cancel an apparent death first. Canceled first lives
     * therefore never consume or duplicate the final victory reward.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBossDeath(LivingDeathEvent event) {
        FoodBossInventoryRewards.onBossDeath(event);
    }

    static boolean eligible(LivingEntity boss) {
        return !(boss instanceof EasterBossEntity)
                && BossMercy.typeOf(boss) != null
                && !LemonSummonRewards.suppressed(boss);
    }
}
