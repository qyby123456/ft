package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ManorDifficulty;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.ManorSupportRules;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class ManorHealthBalance {
    private static final String SUPPORT = "ftboss_sparrow_support_role";
    private static final String APPLIED = "ftboss_manor_health_balance";
    private static final String SCALING = "ftboss_base_hp";
    private static final String IMPOSSIBLE_READY = "ftboss_impossible_sparrow_health";

    private ManorHealthBalance() {}

    public static float supportHealth(EasterBossEntity bird) {
        bird.getPersistentData().putBoolean(SUPPORT, true);
        normalize(bird);
        return ManorSupportRules.SUPPORT_HEALTH;
    }

    public static boolean support(EasterBossEntity bird) {
        var data = bird.getPersistentData();
        var maximum = bird.getAttribute(Attributes.MAX_HEALTH);
        return bird.getVariant() == EasterBossEntity.SPARROW_VARIANT && (data.getBoolean(SUPPORT)
                || data.getBoolean(IMPOSSIBLE_READY) || ManorFightMembers.ownerId(bird) != null
                || maximum != null && maximum.getBaseValue() <= 250);
    }

    public static void prepareImpossible(EasterBossEntity bird) {
        bird.setChallengeDifficulty(ManorDifficulty.IMPOSSIBLE);
        supportHealth(bird);
        normalize(bird);
        bird.getPersistentData().putBoolean(IMPOSSIBLE_READY, true);
    }

    public static void normalize(LivingEntity entity) {
        if (DoraPursuitClone.isClone(entity)) return;
        if (ZhuniPairCombat.normalize(entity)) return;
        if (!(entity instanceof EasterBossEntity boss) || entity.level().isClientSide || !entity.isAlive()
                || boss.isMini() || boss.isGiant() || !(EasterBossEntity.isManorVariant(boss.getVariant())
                || boss.getVariant() == EasterBossEntity.SPARROW_VARIANT)) return;
        var maximum = boss.getAttribute(Attributes.MAX_HEALTH);
        if (maximum == null) return;
        var data = boss.getPersistentData();
        boolean supporting = support(boss);
        if (supporting) data.putBoolean(SUPPORT, true);
        double desired = supporting ? ManorSupportRules.SUPPORT_HEALTH : ManorSupportRules.BASE_HEALTH;
        if (data.getDouble(APPLIED) == desired) return;
        double previous = data.contains(SCALING) ? data.getDouble(SCALING) : maximum.getBaseValue();
        float health = boss.getHealth();
        float before = boss.getMaxHealth();
        maximum.setBaseValue(ManorSupportRules.scaledBase(maximum.getBaseValue(), previous, desired));
        if (data.contains(SCALING)) data.putDouble(SCALING, desired);
        boss.setHealth(ManorSupportRules.health(health, before, boss.getMaxHealth()));
        data.putDouble(APPLIED, desired);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onJoin(EntityJoinLevelEvent event) {
        if (!event.isCanceled() && event.getEntity() instanceof EasterBossEntity boss) normalize(boss);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof EasterBossEntity boss) normalize(boss);
    }
}
