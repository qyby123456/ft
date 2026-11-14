package net.farmingtales.ftboss.skill;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.QizaiCrystalEntity;
import net.farmingtales.ftboss.util.QizaiCrystalRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class QizaiCrystals {
    private static final Map<ServerLevel, Map<UUID, Formation>> ACTIVE = new WeakHashMap<>();

    private static final class Formation {
        final QizaiCrystalEntity[] crystals = new QizaiCrystalEntity[QizaiCrystalRules.COUNT];
        final long[] ready = new long[QizaiCrystalRules.COUNT];
        LivingEntity target;
        int idle;

        void close() {
            for (var crystal : crystals) if (crystal != null) crystal.discard();
        }
    }

    private QizaiCrystals() {}

    public static boolean validOwner(EasterBossEntity boss) {
        return boss != null && boss.isAlive() && !boss.isRemoved()
                && (!boss.isNoAi() || boss.isQizaiUninterruptibleCasting())
                && boss.getVariant() == EasterBossEntity.QIZAI_VARIANT && !boss.isMini() && !boss.isGiant()
                && (!boss.isInvulnerable() || boss.isQizaiProtectedCasting()) && !ManorFight.isFinaleMember(boss);
    }

    public static boolean target(EasterBossEntity boss, Entity entity) {
        return entity instanceof Player player && player.isAlive() && !player.isRemoved()
                && !player.isCreative() && !player.isSpectator() && player.level() == boss.level()
                && boss.distanceToSqr(player) <= 64 * 64;
    }

    private static Formation formation(EasterBossEntity boss) {
        return boss != null && boss.level() instanceof ServerLevel level
                ? ACTIVE.getOrDefault(level, Map.of()).get(boss.getUUID()) : null;
    }

    public static List<QizaiCrystalEntity> members(EasterBossEntity boss) {
        Formation formation = formation(boss);
        return formation == null ? List.of() : Arrays.stream(formation.crystals)
                .filter(crystal -> crystal != null && crystal.isAlive() && !crystal.isRemoved()).toList();
    }

    public static boolean current(QizaiCrystalEntity crystal) {
        Formation formation = formation(crystal.getOwner());
        return formation != null && formation.crystals[crystal.role()] == crystal;
    }

    /**
     * 三水晶自己的被动索敌线。只读取本体目标，不写本体目标、施法动画或主动技能状态；
     * 本体主动技临时清目标时，水晶继续使用缓存目标或独立寻找最近玩家。
     */
    public static LivingEntity combatTarget(EasterBossEntity boss) {
        if (!validOwner(boss)) return null;
        Formation formation = formation(boss);
        if (target(boss, boss.getTarget())) {
            if (formation != null) formation.target = boss.getTarget();
            return boss.getTarget();
        }
        if (formation != null && target(boss, formation.target)) return formation.target;
        if (!(boss.level() instanceof ServerLevel level)) return null;
        return level.players().stream().filter(player -> target(boss, player))
                .min(java.util.Comparator.comparingDouble(boss::distanceToSqr)).orElse(null);
    }

    public static void tick(EasterBossEntity boss) {
        if (!(boss.level() instanceof ServerLevel level)) return;
        if (!validOwner(boss)) { cancel(boss); return; }
        var forms = ACTIVE.computeIfAbsent(level, ignored -> new HashMap<>());
        Formation formation = forms.get(boss.getUUID());
        LivingEntity target = combatTarget(boss);
        if (target == null) {
            if (formation != null && ++formation.idle >= QizaiCrystalRules.IDLE_TICKS) cancel(boss);
            return;
        }
        if (formation == null) {
            if (forms.size() >= 64) return;
            formation = new Formation();
            forms.put(boss.getUUID(), formation);
        }
        formation.target = target;
        formation.idle = 0;
        long now = level.getGameTime();
        for (int role = 0; role < QizaiCrystalRules.COUNT; role++) {
            var crystal = formation.crystals[role];
            if (crystal != null && (!crystal.isAlive() || crystal.isRemoved() || level.getEntity(crystal.getUUID()) != crystal)) {
                crystal.discard();
                formation.crystals[role] = null;
                formation.ready[role] = now + QizaiCrystalRules.RESUMMON_TICKS;
            }
            if (formation.crystals[role] != null || now < formation.ready[role]) continue;
            var summoned = new QizaiCrystalEntity(level, boss, role);
            double angle = role * Math.PI * 2 / QizaiCrystalRules.COUNT;
            Vec3 spawn = boss.position().add(Math.cos(angle) * 2, 2.8, Math.sin(angle) * 2);
            summoned.setPos(spawn);
            if (!level.noCollision(summoned) || !level.addFreshEntity(summoned)) {
                formation.ready[role] = now + 20;
                continue;
            }
            formation.crystals[role] = summoned;
        }
    }

    public static void cancel(EasterBossEntity boss) {
        if (boss.level() instanceof ServerLevel level) {
            var forms = ACTIVE.get(level);
            Formation formation = forms == null ? null : forms.remove(boss.getUUID());
            if (formation != null) formation.close();
        }
    }

    @SubscribeEvent public static void tick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof EasterBossEntity boss
                && (boss.getVariant() == EasterBossEntity.QIZAI_VARIANT || formation(boss) != null)) tick(boss);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void defend(LivingHurtEvent event) {
        if (event.isCanceled() || !(event.getEntity() instanceof EasterBossEntity boss) || combatTarget(boss) == null
                || event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                || !Float.isFinite(event.getAmount()) || event.getAmount() <= 0) return;
        for (var crystal : members(boss)) if (crystal.role() == QizaiCrystalRules.GUARD && crystal.distanceToSqr(boss) <= 16 * 16) {
            event.setAmount(event.getAmount() - crystal.absorb(event.getAmount() * QizaiCrystalRules.WARD_REDUCTION));
            break;
        }
    }

    @SubscribeEvent public static void death(LivingDeathEvent event) {
        if (event.getEntity() instanceof EasterBossEntity boss) cancel(boss);
    }

    @SubscribeEvent public static void leave(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof EasterBossEntity boss) cancel(boss);
    }

    @SubscribeEvent public static void unload(LevelEvent.Unload event) {
        var forms = ACTIVE.remove(event.getLevel());
        if (forms != null) forms.values().forEach(Formation::close);
    }

    @SubscribeEvent public static void stop(ServerStoppedEvent event) { ACTIVE.clear(); }
}
