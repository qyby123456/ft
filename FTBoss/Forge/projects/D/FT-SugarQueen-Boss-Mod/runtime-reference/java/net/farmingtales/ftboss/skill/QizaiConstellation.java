/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Position
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.TickEvent$LevelTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.level.LevelEvent$Unload
 *  net.minecraftforge.event.server.ServerStoppedEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.skill;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.skill.SpellVisuals;
import net.farmingtales.ftboss.util.QizaiRules;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss")
public final class QizaiConstellation {
    private static final Map<ServerLevel, List<Volley>> ACTIVE = new WeakHashMap<ServerLevel, List<Volley>>();

    private QizaiConstellation() {
    }

    public static List<Vec3> marks(ServerLevel level, LivingEntity caster, int kind, Vec3 point) {
        Vec3 center = QizaiConstellation.floor(level, caster, point);
        if (center == null || !QizaiRules.constellation(kind)) {
            return List.of();
        }
        ArrayList<Vec3> marks = new ArrayList<Vec3>();
        marks.add(center);
        if (kind == 6) {
            for (int index = 0; index < 4; ++index) {
                double angle = (double)index * Math.PI / 2.0;
                Vec3 mark = QizaiConstellation.floor(level, caster, center.m_82520_(Math.cos(angle) * 7.6, 0.0, Math.sin(angle) * 7.6));
                if (mark == null) continue;
                marks.add(mark);
            }
        }
        return List.copyOf(marks);
    }

    private static Vec3 floor(ServerLevel level, LivingEntity caster, Vec3 point) {
        if (!QizaiConstellation.valid(level, caster, point)) {
            return null;
        }
        BlockHitResult hit = level.m_45547_(new ClipContext(point.m_82520_(0.0, 1.0, 0.0), point.m_82520_(0.0, -10.0, 0.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)caster));
        Vec3 floor = hit.m_82450_().m_82520_(0.0, 0.035, 0.0);
        return hit.m_6662_() == HitResult.Type.BLOCK && QizaiConstellation.valid(level, caster, floor) ? floor : null;
    }

    private static boolean valid(ServerLevel level, LivingEntity caster, Vec3 point) {
        return Double.isFinite(point.f_82479_ + point.f_82480_ + point.f_82481_) && caster.m_9236_() == level && caster.m_20182_().m_82557_(point) <= 1444.0 && level.m_46805_(BlockPos.m_274446_((Position)point)) && level.m_6857_().m_61937_(BlockPos.m_274446_((Position)point));
    }

    public static boolean active(LivingEntity caster) {
        Player player;
        if (!caster.m_6084_() || caster.m_213877_()) {
            return false;
        }
        if (caster instanceof Player) {
            Player player2 = (Player)caster;
            return !player2.m_5833_();
        }
        if (!(caster instanceof EasterBossEntity)) {
            return true;
        }
        EasterBossEntity boss = (EasterBossEntity)caster;
        LivingEntity target = boss.m_5448_();
        return boss.getVariant() == 11 && !boss.m_21525_() && !boss.m_20147_() && !boss.isMini() && !boss.isGiant() && !ManorFight.isFinaleMember((LivingEntity)boss) && target instanceof Player && (player = (Player)target).m_6084_() && !player.m_213877_() && !player.m_7500_() && !player.m_5833_() && player.m_9236_() == boss.m_9236_() && boss.m_20280_((Entity)player) <= 1024.0;
    }

    public static void warning(ServerLevel level, LivingEntity caster, int kind, List<Vec3> marks) {
        for (Vec3 mark : marks) {
            if (!QizaiConstellation.valid(level, caster, mark)) continue;
            SpellVisuals.show(level, caster, QizaiRules.id(kind), SpellVisualRules.Phase.CHARGE, mark);
        }
    }

    public static void cast(ServerLevel level, LivingEntity caster, int kind, List<Vec3> marks, Hit hit) {
        if (!QizaiRules.constellation(kind) || !QizaiConstellation.active(caster) || marks.isEmpty() || marks.size() > 5 || marks.stream().anyMatch(mark -> !QizaiConstellation.valid(level, caster, mark))) {
            return;
        }
        QizaiConstellation.impact(level, caster, kind, marks.get(0), hit);
        if (kind == 6 && marks.size() > 1) {
            EasterBossEntity boss;
            List volleys = ACTIVE.computeIfAbsent(level, ignored -> new ArrayList());
            if (caster instanceof EasterBossEntity && (boss = (EasterBossEntity)caster).getVariant() == 11) {
                if (volleys.stream().filter(volley -> volley.caster == caster).count() >= 3L) {
                    volleys.stream().filter(volley -> volley.caster == caster).findFirst().ifPresent(volleys::remove);
                }
            } else {
                volleys.removeIf(volley -> volley.caster == caster);
            }
            if (volleys.size() >= 64) {
                volleys.remove(0);
            }
            volleys.add(new Volley(caster, marks, hit));
            if (!(caster instanceof EasterBossEntity)) {
                QizaiConstellation.warning(level, caster, kind, marks.subList(1, marks.size()));
            }
        }
    }

    private static void impact(ServerLevel level, LivingEntity caster, int kind, Vec3 center, Hit hit) {
        if (!QizaiConstellation.valid(level, caster, center)) {
            return;
        }
        double radius = QizaiRules.radius(kind);
        List targets = level.m_6443_(LivingEntity.class, new AABB(center, center).m_82377_(radius, 3.0, radius), target -> target != caster && target.m_6084_() && !target.m_213877_());
        targets.sort(Comparator.comparingDouble(target -> target.m_20238_(center)));
        int count = 0;
        for (LivingEntity target2 : targets) {
            Vec3 delta = target2.m_20182_().m_82546_(center);
            boolean inside = kind == 5 ? QizaiRules.cross(delta.f_82479_, delta.f_82480_, delta.f_82481_) : QizaiRules.inside(delta.m_165925_(), delta.f_82480_, kind);
            if (!inside || level.m_45547_(new ClipContext(center.m_82520_(0.0, 1.5, 0.0), target2.m_146892_(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)caster)).m_6662_() != HitResult.Type.MISS) continue;
            if (count++ >= 16) break;
            hit.apply(target2);
        }
        String id = QizaiRules.id(kind);
        SpellVisuals.show(level, caster, id, SpellVisualRules.Phase.RELEASE, center);
        if (kind == 5) {
            SpellVisuals.beam(level, caster, id, center.m_82520_(-radius, 0.3, 0.0), center.m_82520_(radius, 0.3, 0.0));
            SpellVisuals.beam(level, caster, id, center.m_82520_(0.0, 0.3, -radius), center.m_82520_(0.0, 0.3, radius));
        } else {
            SpellVisuals.beam(level, caster, id, center.m_82520_(0.0, 7.0, 0.0), center.m_82520_(0.0, 0.15, 0.0));
        }
        level.m_5594_(null, BlockPos.m_274446_((Position)center), SoundEvents.f_276532_, SoundSource.HOSTILE, 1.0f, 1.4f);
    }

    public static void cancel(LivingEntity caster) {
        ServerLevel level;
        List<Volley> volleys;
        Level level2 = caster.m_9236_();
        if (level2 instanceof ServerLevel && (volleys = ACTIVE.get(level = (ServerLevel)level2)) != null) {
            volleys.removeIf(volley -> volley.caster == caster);
        }
    }

    public static void tick(ServerLevel level) {
        List<Volley> volleys = ACTIVE.get(level);
        if (volleys == null) {
            return;
        }
        for (Volley volley : List.copyOf(volleys)) {
            if (!volleys.contains(volley)) continue;
            if (volley.caster.m_9236_() != level || !QizaiConstellation.active(volley.caster)) {
                volleys.remove(volley);
                continue;
            }
            if (++volley.age >= volley.next * 8) {
                Vec3 mark = volley.marks.get(volley.next++);
                if (volley.next >= volley.marks.size()) {
                    volleys.remove(volley);
                }
                QizaiConstellation.impact(level, volley.caster, 6, mark, volley.hit);
            }
            if (volley.age % 6 != 0 || volley.caster instanceof EasterBossEntity || !volleys.contains(volley)) continue;
            QizaiConstellation.warning(level, volley.caster, 6, volley.marks.subList(volley.next, volley.marks.size()));
        }
        if (volleys.isEmpty()) {
            ACTIVE.remove(level);
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.LevelTickEvent event) {
        Level level;
        if (event.phase == TickEvent.Phase.END && (level = event.level) instanceof ServerLevel) {
            ServerLevel level2 = (ServerLevel)level;
            QizaiConstellation.tick(level2);
        }
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        ACTIVE.remove(event.getLevel());
    }

    @SubscribeEvent
    public static void stop(ServerStoppedEvent event) {
        ACTIVE.clear();
    }

    @FunctionalInterface
    public static interface Hit {
        public void apply(LivingEntity var1);
    }

    private static final class Volley {
        final LivingEntity caster;
        final List<Vec3> marks;
        final Hit hit;
        int age;
        int next = 1;

        Volley(LivingEntity caster, List<Vec3> marks, Hit hit) {
            this.caster = caster;
            this.marks = List.copyOf(marks);
            this.hit = hit;
        }
    }
}

