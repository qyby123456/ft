/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Position
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.TickEvent$LevelTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$ServerTickEvent
 *  net.minecraftforge.event.level.LevelEvent$Unload
 *  net.minecraftforge.event.server.ServerStoppedEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.skill.QizaiConstellation;
import net.farmingtales.ftboss.util.ManorRefinementRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss")
public final class ManorPredictiveAttacks {
    private static final Map<ServerLevel, List<ExtraSpell>> PENDING = new WeakHashMap<ServerLevel, List<ExtraSpell>>();
    private static final Map<Player, Vec3> POSITIONS = new WeakHashMap<Player, Vec3>();
    private static final Map<Player, Vec3> VELOCITIES = new WeakHashMap<Player, Vec3>();

    private ManorPredictiveAttacks() {
    }

    private static boolean player(LivingEntity livingEntity, ServerLevel serverLevel) {
        Player player;
        return livingEntity instanceof Player && (player = (Player)livingEntity).m_6084_() && !player.m_213877_() && !player.m_7500_() && !player.m_5833_() && player.m_9236_() == serverLevel;
    }

    private static Vec3 prediction(LivingEntity livingEntity) {
        Vec3 vec3 = VELOCITIES.get(livingEntity);
        double d = vec3 == null ? livingEntity.m_20185_() - livingEntity.f_19854_ : vec3.f_82479_;
        double d2 = vec3 == null ? livingEntity.m_20189_() - livingEntity.f_19856_ : vec3.f_82481_;
        double d3 = ManorRefinementRules.leadScale(d, d2);
        return livingEntity.m_20182_().m_82520_(d * d3, 0.0, d2 * d3);
    }

    private static Vec3 ground(ServerLevel serverLevel, EasterBossEntity easterBossEntity, Vec3 vec3, Vec3 vec32, double d) {
        if (!Double.isFinite(vec3.f_82479_ + vec3.f_82480_ + vec3.f_82481_) || vec3.m_82546_(vec32).m_165925_() > d * d) {
            return null;
        }
        Vec3 vec33 = ManorFight.anchorOf(ManorFight.fightId((Entity)easterBossEntity));
        if (vec33 != null && vec3.m_82546_(vec33).m_165925_() > 5776.0) {
            return null;
        }
        BlockPos blockPos = BlockPos.m_274446_((Position)vec3);
        if (!serverLevel.m_46805_(blockPos) || !serverLevel.m_6857_().m_61937_(blockPos)) {
            return null;
        }
        BlockHitResult blockHitResult = serverLevel.m_45547_(new ClipContext(vec3.m_82520_(0.0, 1.0, 0.0), vec3.m_82520_(0.0, -12.0, 0.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)easterBossEntity));
        return blockHitResult.m_6662_() == HitResult.Type.BLOCK ? blockHitResult.m_82450_().m_82520_(0.0, 0.04, 0.0) : null;
    }

    public static Vec3 skyPrediction(ServerLevel serverLevel, EasterBossEntity easterBossEntity, LivingEntity livingEntity, Vec3 vec3) {
        if (!ManorPredictiveAttacks.player(livingEntity, serverLevel)) {
            return null;
        }
        return ManorPredictiveAttacks.ground(serverLevel, easterBossEntity, ManorPredictiveAttacks.prediction(livingEntity), vec3, 64.0);
    }

    public static void extraSpell(ServerLevel serverLevel2, EasterBossEntity easterBossEntity, LivingEntity livingEntity, int n) {
        if (!(ManorRefinementRules.minorSpell(n) && ManorPredictiveAttacks.player(livingEntity, serverLevel2) && easterBossEntity.getVariant() == 11 && QizaiConstellation.active((LivingEntity)easterBossEntity))) {
            return;
        }
        Vec3 vec3 = ManorPredictiveAttacks.prediction(livingEntity);
        double d = easterBossEntity.m_217043_().m_188500_() * Math.PI * 2.0;
        double d2 = easterBossEntity.m_217043_().m_188500_() * 1.5;
        Vec3 vec32 = ManorPredictiveAttacks.ground(serverLevel2, easterBossEntity, vec3.m_82520_(Math.cos(d) * d2, 0.0, Math.sin(d) * d2), easterBossEntity.m_20182_(), 32.0);
        if (vec32 == null) {
            vec32 = ManorPredictiveAttacks.ground(serverLevel2, easterBossEntity, vec3, easterBossEntity.m_20182_(), 32.0);
        }
        if (vec32 == null) {
            return;
        }
        int n2 = easterBossEntity.m_217043_().m_188499_() ? 5 : 6;
        QizaiConstellation.warning(serverLevel2, (LivingEntity)easterBossEntity, n2, List.of(vec32));
        PENDING.computeIfAbsent(serverLevel2, serverLevel -> new ArrayList()).add(new ExtraSpell(easterBossEntity, n2, vec32, serverLevel2.m_46467_() + 10L));
    }

    @SubscribeEvent
    public static void sample(TickEvent.ServerTickEvent serverTickEvent) {
        if (serverTickEvent.phase != TickEvent.Phase.START) {
            return;
        }
        Iterator iterator = serverTickEvent.getServer().m_6846_().m_11314_().iterator();
        while (iterator.hasNext()) {
            Vec3 vec3;
            Player player;
            Vec3 vec32 = POSITIONS.put(player = (Player)iterator.next(), vec3 = player.m_20182_());
            VELOCITIES.put(player, vec32 == null ? Vec3.f_82478_ : vec3.m_82546_(vec32));
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.LevelTickEvent levelTickEvent) {
        Object object;
        if (levelTickEvent.phase != TickEvent.Phase.END || !((object = levelTickEvent.level) instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)object;
        object = PENDING.get(serverLevel);
        if (object == null) {
            return;
        }
        object.removeIf(extraSpell -> {
            EasterBossEntity easterBossEntity = extraSpell.boss();
            if (easterBossEntity.m_9236_() != serverLevel || !QizaiConstellation.active((LivingEntity)easterBossEntity)) {
                return true;
            }
            if (serverLevel.m_46467_() < extraSpell.releaseAt()) {
                return false;
            }
            QizaiConstellation.cast(serverLevel, (LivingEntity)easterBossEntity, extraSpell.kind(), List.of(extraSpell.mark()), livingEntity -> {
                if (!ManorPredictiveAttacks.player(livingEntity, serverLevel) || easterBossEntity.m_7307_((Entity)livingEntity) || livingEntity.m_7307_((Entity)easterBossEntity)) {
                    return;
                }
                int n = livingEntity.f_19802_;
                livingEntity.f_19802_ = 0;
                try {
                    float f = extraSpell.kind() == 5 ? 104.0f : 64.0f;
                    livingEntity.m_6469_(serverLevel.m_269111_().m_269104_((Entity)easterBossEntity, (Entity)easterBossEntity), easterBossEntity.manorSkillDamage(livingEntity, f, 2.0));
                }
                finally {
                    livingEntity.f_19802_ = Math.max(n, livingEntity.f_19802_);
                }
            });
            return true;
        });
        if (object.isEmpty()) {
            PENDING.remove(serverLevel);
        }
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload unload) {
        PENDING.remove(unload.getLevel());
    }

    @SubscribeEvent
    public static void stop(ServerStoppedEvent serverStoppedEvent) {
        PENDING.clear();
        POSITIONS.clear();
        VELOCITIES.clear();
    }

    private record ExtraSpell(EasterBossEntity boss, int kind, Vec3 mark, long releaseAt) {
    }
}

