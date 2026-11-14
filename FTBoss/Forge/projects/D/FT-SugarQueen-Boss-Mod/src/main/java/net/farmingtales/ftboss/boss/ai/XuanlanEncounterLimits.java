package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ImpossibleSparrowSupport;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FlywheelProjectileEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class XuanlanEncounterLimits {
    private static final int VARIANT = 6;

    private XuanlanEncounterLimits() {}

    public static boolean blocksSupport(Entity entity) {
        return entity instanceof EasterBossEntity boss && boss.getVariant() == VARIANT;
    }

    public static EasterBossEntity supportTarget(EasterBossEntity boss) {
        return blocksSupport(boss) ? null : boss;
    }

    public static boolean stopSupport(EasterBossEntity bird) {
        if (bird.getVariant() != EasterBossEntity.SPARROW_VARIANT || !(bird.level() instanceof ServerLevel level)) return false;
        UUID fight = ManorFightMembers.fightId(bird);
        UUID owner = ManorFightMembers.ownerId(bird);
        if (fight != null && ManorFight.currentVariant(fight) == VARIANT
                || owner != null && blocksSupport(level.getEntity(owner))) {
            ImpossibleSparrowSupport.failedSpawn(bird);
            bird.discard();
            return true;
        }
        return false;
    }

    private static List<FlywheelProjectileEntity> wheels(ServerLevel level, EasterBossEntity boss, Entity excluded) {
        List<FlywheelProjectileEntity> result = new ArrayList<>();
        for (Entity entity : level.getAllEntities()) {
            if (entity == excluded || !(entity instanceof FlywheelProjectileEntity wheel) || wheel.isRemoved()) continue;
            Entity owner = wheel.getOwner();
            if (owner != null && owner.getUUID().equals(boss.getUUID())) result.add(wheel);
        }
        return result;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onJoin(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        Entity entity = event.getEntity();
        if (entity instanceof EasterBossEntity bird && stopSupport(bird)) {
            event.setCanceled(true);
        } else if (entity instanceof FlywheelProjectileEntity wheel && wheel.getOwner() instanceof EasterBossEntity boss
                && blocksSupport(boss) && wheels(level, boss, wheel).size() >= ZhuniPairCombat.flywheelLimit(boss)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof EasterBossEntity boss) || !(boss.level() instanceof ServerLevel level)) return;
        if (stopSupport(boss) || !blocksSupport(boss) || level.getGameTime() % 20 != 0) return;
        List<FlywheelProjectileEntity> existing = wheels(level, boss, null);
        int limit = ZhuniPairCombat.flywheelLimit(boss);
        if (existing.size() <= limit) return;
        existing.sort(Comparator.comparingInt(Entity::getId));
        for (int index = limit; index < existing.size(); index++) existing.get(index).discard();
    }
}
