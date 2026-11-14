package net.farmingtales.ftboss.skill;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.QizaiRules;
import net.farmingtales.ftboss.util.SpellVisualRules.Phase;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class QizaiConstellation {
    @FunctionalInterface
    public interface Hit { void apply(LivingEntity target); }

    private static final Map<ServerLevel, List<Volley>> ACTIVE = new WeakHashMap<>();

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

    private QizaiConstellation() {}

    public static List<Vec3> marks(ServerLevel level, LivingEntity caster, int kind, Vec3 point) {
        Vec3 center = floor(level, caster, point);
        if (center == null || !QizaiRules.constellation(kind)) return List.of();
        List<Vec3> marks = new ArrayList<>();
        marks.add(center);
        if (kind == QizaiRules.ROSE_STARFALL) {
            for (int index = 0; index < 4; index++) {
                double angle = index * Math.PI / 2;
                Vec3 mark = floor(level, caster, center.add(Math.cos(angle) * QizaiRules.STARFALL_OFFSET * SpellVisuals.playerRadiusScale(caster), 0,
                        Math.sin(angle) * QizaiRules.STARFALL_OFFSET * SpellVisuals.playerRadiusScale(caster)));
                if (mark != null) marks.add(mark);
            }
        }
        return List.copyOf(marks);
    }

    private static Vec3 floor(ServerLevel level, LivingEntity caster, Vec3 point) {
        if (!valid(level, caster, point)) return null;
        var hit = level.clip(new ClipContext(point.add(0, 1, 0), point.add(0, -10, 0),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
        Vec3 floor = hit.getLocation().add(0, 0.035, 0);
        return hit.getType() == HitResult.Type.BLOCK && valid(level, caster, floor) ? floor : null;
    }

    private static boolean valid(ServerLevel level, LivingEntity caster, Vec3 point) {
        return Double.isFinite(point.x + point.y + point.z) && caster.level() == level
                && caster.position().distanceToSqr(point) <= Math.pow(38 * SpellVisuals.playerRadiusScale(caster), 2)
                && level.hasChunkAt(BlockPos.containing(point)) && level.getWorldBorder().isWithinBounds(BlockPos.containing(point));
    }

    public static boolean active(LivingEntity caster) {
        if (!caster.isAlive() || caster.isRemoved()) return false;
        if (caster instanceof Player player) return !player.isSpectator();
        if (!(caster instanceof EasterBossEntity boss)) return true;
        LivingEntity target = boss.getTarget();
        return boss.getVariant() == EasterBossEntity.QIZAI_VARIANT && !boss.isNoAi() && !boss.isInvulnerable()
                && !boss.isMini() && !boss.isGiant() && !ManorFight.isFinaleMember(boss)
                && target instanceof Player player && player.isAlive() && !player.isRemoved() && !player.isCreative()
                && !player.isSpectator() && player.level() == boss.level() && boss.distanceToSqr(player) <= QizaiRules.RANGE * QizaiRules.RANGE;
    }

    public static void warning(ServerLevel level, LivingEntity caster, int kind, List<Vec3> marks) {
        for (Vec3 mark : marks) if (valid(level, caster, mark))
            SpellVisuals.show(level, caster, QizaiRules.id(kind), Phase.CHARGE, mark);
    }

    public static void cast(ServerLevel level, LivingEntity caster, int kind, List<Vec3> marks, Hit hit) {
        if (!QizaiRules.constellation(kind) || !active(caster) || marks.isEmpty() || marks.size() > 5
                || marks.stream().anyMatch(mark -> !valid(level, caster, mark))) return;
        impact(level, caster, kind, marks.get(0), hit);
        if (kind == QizaiRules.ROSE_STARFALL && marks.size() > 1) {
            List<Volley> volleys = ACTIVE.computeIfAbsent(level, ignored -> new ArrayList<>());
            if (caster instanceof EasterBossEntity boss && boss.getVariant() == EasterBossEntity.QIZAI_VARIANT) {
                if (volleys.stream().filter(volley -> volley.caster == caster).count() >= QizaiRules.RELEASES)
                    volleys.stream().filter(volley -> volley.caster == caster).findFirst().ifPresent(volleys::remove);
            } else volleys.removeIf(volley -> volley.caster == caster);
            if (volleys.size() >= 64) volleys.remove(0);
            volleys.add(new Volley(caster, marks, hit));
            if (!(caster instanceof EasterBossEntity)) warning(level, caster, kind, marks.subList(1, marks.size()));
        }
    }

    private static void impact(ServerLevel level, LivingEntity caster, int kind, Vec3 center, Hit hit) {
        if (!valid(level, caster, center)) return;
        double scale = SpellVisuals.playerRadiusScale(caster);
        double radius = QizaiRules.radius(kind) * scale;
        var targets = level.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(radius, 3, radius),
                target -> target != caster && target.isAlive() && !target.isRemoved());
        targets.sort(Comparator.comparingDouble(target -> target.distanceToSqr(center)));
        int count = 0;
        for (LivingEntity target : targets) {
            Vec3 delta = target.position().subtract(center);
            boolean inside = net.farmingtales.ftboss.util.ManorSpellAreaRules.inside(kind, delta.x, delta.y, delta.z, scale);
            if (!inside || level.clip(new ClipContext(center.add(0, 1.5, 0), target.getEyePosition(),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster)).getType() != HitResult.Type.MISS) continue;
            if (count++ >= 16) break;
            hit.apply(target);
        }
        String id = QizaiRules.id(kind);
        SpellVisuals.show(level, caster, id, Phase.RELEASE, center);
        if (kind == QizaiRules.STAR_CROSS) {
            SpellVisuals.beam(level, caster, id, center.add(-radius, 0.3, 0), center.add(radius, 0.3, 0));
            SpellVisuals.beam(level, caster, id, center.add(0, 0.3, -radius), center.add(0, 0.3, radius));
        } else SpellVisuals.beam(level, caster, id, center.add(0, 7, 0), center.add(0, 0.15, 0));
        level.playSound(null, BlockPos.containing(center), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.HOSTILE, 1, 1.4F);
    }

    public static void cancel(LivingEntity caster) {
        if (caster.level() instanceof ServerLevel level) {
            List<Volley> volleys = ACTIVE.get(level);
            if (volleys != null) volleys.removeIf(volley -> volley.caster == caster);
        }
    }

    public static void tick(ServerLevel level) {
        List<Volley> volleys = ACTIVE.get(level);
        if (volleys == null) return;
        for (Volley volley : List.copyOf(volleys)) {
            if (!volleys.contains(volley)) continue;
            if (volley.caster.level() != level || !active(volley.caster)) { volleys.remove(volley); continue; }
            if (++volley.age >= volley.next * QizaiRules.STARFALL_INTERVAL) {
                Vec3 mark = volley.marks.get(volley.next++);
                if (volley.next >= volley.marks.size()) volleys.remove(volley);
                impact(level, volley.caster, QizaiRules.ROSE_STARFALL, mark, volley.hit);
            }
            if (volley.age % 6 == 0 && !(volley.caster instanceof EasterBossEntity) && volleys.contains(volley))
                warning(level, volley.caster, QizaiRules.ROSE_STARFALL, volley.marks.subList(volley.next, volley.marks.size()));
        }
        if (volleys.isEmpty()) ACTIVE.remove(level);
    }

    @SubscribeEvent
    public static void tick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.level instanceof ServerLevel level) tick(level);
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) { ACTIVE.remove(event.getLevel()); }

    @SubscribeEvent
    public static void stop(ServerStoppedEvent event) { ACTIVE.clear(); }
}
