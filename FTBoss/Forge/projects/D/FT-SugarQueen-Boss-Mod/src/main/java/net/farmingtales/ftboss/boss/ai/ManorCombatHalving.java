package net.farmingtales.ftboss.boss.ai;

import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.ManorMercy;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.turret.ZhuniEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class ManorCombatHalving {
    private static final UUID HEALTH_ID = UUID.fromString("b1c3cb57-19e7-4dd6-a763-5867d839b7df");
    private static final String MEMBER = "ftboss_manor_combat_halved";
    private static final float MULTIPLIER = 0.5F;

    private ManorCombatHalving() {}

    public static boolean applies(Entity entity) {
        for (int depth = 0; entity != null && depth < 16; depth++) {
            if (entity.getPersistentData().getBoolean(MEMBER)) return true;
            if (entity instanceof EasterBossEntity boss && !boss.isMini() && !boss.isGiant()
                    && (EasterBossEntity.isManorVariant(boss.getVariant())
                    || boss.getVariant() == EasterBossEntity.SPARROW_VARIANT)) return true;
            Entity owner = ManorFightMembers.owner(entity);
            if (owner == null && entity.level() instanceof ServerLevel level) {
                UUID marked = MinionBlessing.ownerId(entity);
                if (marked != null) owner = level.getEntity(marked);
            }
            if (owner == entity) return false;
            entity = owner;
        }
        return false;
    }

    public static void normalize(Entity entity) {
        if (entity.level().isClientSide || !applies(entity)) return;
        entity.getPersistentData().putBoolean(MEMBER, true);
        if (!(entity instanceof LivingEntity living) || !living.isAlive()) return;
        var maximum = living.getAttribute(Attributes.MAX_HEALTH);
        if (maximum == null) return;
        boolean fixedHealth = living instanceof ZhuniEntity
                || living instanceof EasterBossEntity boss && ZhuniPairCombat.paired(boss);
        if (fixedHealth) {
            if (maximum.getModifier(HEALTH_ID) == null) return;
        } else if (maximum.getModifier(HEALTH_ID) != null) return;
        float previousMaximum = living.getMaxHealth();
        float previousHealth = living.getHealth();
        if (fixedHealth) {
            maximum.removeModifier(HEALTH_ID);
        } else {
            maximum.addPermanentModifier(new AttributeModifier(HEALTH_ID, MEMBER, MULTIPLIER - 1.0,
                    AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
        if (previousMaximum > 0.0F) {
            float health = living instanceof EasterBossEntity boss && ZhuniPairCombat.xuanlanDefeated(boss)
                    ? 1.0F : previousHealth / previousMaximum * living.getMaxHealth();
            living.setHealth(Math.min(living.getMaxHealth(), health));
        }
    }

    public static float statusDamage(float amount) {
        return applies(ManorMercy.damageOwner()) ? amount : amount * MULTIPLIER;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onJoin(EntityJoinLevelEvent event) {
        if (!event.isCanceled()) normalize(event.getEntity());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTick(LivingEvent.LivingTickEvent event) {
        normalize(event.getEntity());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onHurt(LivingHurtEvent event) {
        if (event.isCanceled() || event.getEntity().level().isClientSide
                || !Float.isFinite(event.getAmount()) || event.getAmount() <= 0.0F) return;
        if (applies(event.getSource().getEntity()) || applies(event.getSource().getDirectEntity())
                || applies(ManorMercy.damageOwner())) {
            event.setAmount(event.getAmount() * MULTIPLIER);
        }
    }
}
