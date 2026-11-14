package net.farmingtales.ftboss.integ.spells;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.QizaiStarEntity;
import net.farmingtales.ftboss.skill.SpellVisuals;
import net.farmingtales.ftboss.util.ManorSpellRules;
import net.farmingtales.ftboss.util.QizaiRules;
import net.farmingtales.ftboss.util.QizaiSweepRules;
import net.farmingtales.ftboss.util.SpellVisualRules.Phase;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class QizaiInfernoEffects {
    private static final Map<ServerLevel, List<Cast>> ACTIVE = new WeakHashMap<>();
    private static final int ROUND_TICKS = QizaiSweepRules.SWEEP_TICKS + 1
            + (QizaiSweepRules.RAIN_WAVES - 1) * QizaiSweepRules.RAIN_GAP;
    private static final int MAX_CASTS = 8;

    private QizaiInfernoEffects() {}

    public static void warning(ServerLevel level, LivingEntity caster) {
        SpellVisuals.show(level, caster, "rose_starfall", Phase.CHARGE, caster.position());
    }

    public static void cast(ServerLevel level, LivingEntity caster, ManorSignatureSpell spell, int rank) {
        if (!caster.isAlive() || caster.isSpectator() || !spell.isEnabled()
                || !level.hasChunkAt(caster.blockPosition())) return;
        float damage = BossSpellTuning.playerPower(caster, spell.getSpellPower(rank, caster));
        if (!Float.isFinite(damage) || damage <= 0) return;
        List<Cast> casts = ACTIVE.computeIfAbsent(level, ignored -> new ArrayList<>());
        for (Iterator<Cast> iterator = casts.iterator(); iterator.hasNext();) {
            Cast current = iterator.next();
            if (current.caster.equals(caster.getUUID())) {
                current.clear();
                iterator.remove();
            }
        }
        if (casts.size() >= MAX_CASTS) casts.remove(0).clear();
        Vec3 forward = caster.getLookAngle();
        casts.add(new Cast(caster.getUUID(), caster.position(), Math.toDegrees(Math.atan2(forward.z, forward.x)),
                damage, level.getGameTime(), spell));
    }

    @SubscribeEvent
    public static void tick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) return;
        List<Cast> casts = ACTIVE.get(level);
        if (casts == null) return;
        for (Iterator<Cast> iterator = casts.iterator(); iterator.hasNext();) {
            Cast cast = iterator.next();
            long age = level.getGameTime() - cast.started;
            LivingEntity caster = level.getEntity(cast.caster) instanceof LivingEntity living ? living : null;
            if (caster == null || !caster.isAlive() || caster.isSpectator() || caster.isRemoved()
                    || !cast.spell.isEnabled() || age < 0 || age >= QizaiRules.RELEASES * ROUND_TICKS + QizaiRules.STAR_LIFETIME
                    || !level.hasChunkAt(BlockPos.containing(cast.anchor))) {
                cast.clear();
                iterator.remove();
                continue;
            }
            cast.steer(level, caster);
            if (age >= QizaiRules.RELEASES * ROUND_TICKS) continue;
            int round = (int) age / ROUND_TICKS;
            int elapsed = (int) age % ROUND_TICKS;
            if (elapsed < QizaiSweepRules.SWEEP_TICKS) {
                cast.beams(level, caster, round, elapsed, age);
                if (elapsed % QizaiSweepRules.VOLLEY_INTERVAL == 0) cast.volley(level, caster, elapsed);
            } else {
                int rainAge = elapsed - QizaiSweepRules.SWEEP_TICKS;
                if (rainAge % QizaiSweepRules.RAIN_GAP == 0
                        && rainAge / QizaiSweepRules.RAIN_GAP < QizaiSweepRules.RAIN_WAVES) {
                    cast.rain(level, caster, rainAge / QizaiSweepRules.RAIN_GAP);
                }
            }
        }
        if (casts.isEmpty()) ACTIVE.remove(level);
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            List<Cast> casts = ACTIVE.remove(level);
            if (casts != null) casts.forEach(Cast::clear);
        }
    }

    @SubscribeEvent
    public static void stop(ServerStoppedEvent event) {
        ACTIVE.values().forEach(casts -> casts.forEach(Cast::clear));
        ACTIVE.clear();
    }

    private record Star(QizaiStarEntity entity, UUID target, long born) {}

    private static final class Cast {
        final UUID caster;
        final Vec3 anchor;
        final double initialAngle;
        final float damage;
        final long started;
        final ManorSignatureSpell spell;
        final Map<UUID, Long> hits = new HashMap<>();
        final List<Star> stars = new ArrayList<>();

        Cast(UUID caster, Vec3 anchor, double angle, float damage, long started, ManorSignatureSpell spell) {
            this.caster = caster;
            this.anchor = anchor;
            this.initialAngle = angle;
            this.damage = damage;
            this.started = started;
            this.spell = spell;
        }

        List<LivingEntity> targets(ServerLevel level, LivingEntity owner) {
            double radius = QizaiSweepRules.RADIUS * SpellVisuals.playerRadiusScale(owner);
            return level.getEntitiesOfClass(LivingEntity.class, new AABB(anchor, anchor).inflate(radius, QizaiSweepRules.RADIUS, radius),
                    target -> BossSpellEffects.canTarget(owner, target) && owner.hasLineOfSight(target))
                    .stream().limit(ManorSpellRules.MAX_TARGETS).toList();
        }

        void beams(ServerLevel level, LivingEntity owner, int round, int elapsed, long age) {
            List<LivingEntity> candidates = targets(level, owner);
            for (int arm = 0; arm < QizaiSweepRules.arms(round); arm++) {
                double heading = Math.toRadians(QizaiSweepRules.angle(initialAngle, elapsed, round, arm));
                Vec3 origin = anchor.add(0, QizaiSweepRules.height(round, arm), 0);
                double radius = QizaiSweepRules.RADIUS * SpellVisuals.playerRadiusScale(owner);
                Vec3 end = origin.add(Math.cos(heading) * radius, 0, Math.sin(heading) * radius);
                end = level.clip(new ClipContext(origin, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner)).getLocation();
                LivingEntity first = null;
                double distance = origin.distanceToSqr(end);
                for (LivingEntity target : candidates) {
                    double width = QizaiSweepRules.BEAM_HALF_WIDTH * SpellVisuals.playerRadiusScale(owner);
                    AABB body = target.getBoundingBox().inflate(width, 0, width);
                    var contact = body.contains(origin) ? java.util.Optional.of(origin) : body.clip(origin, end);
                    if (contact.isPresent() && origin.distanceToSqr(contact.get()) <= distance) {
                        distance = origin.distanceToSqr(contact.get());
                        first = target;
                    }
                }
                if (distance > 0) end = origin.add(end.subtract(origin).normalize().scale(Math.sqrt(distance)));
                if (elapsed % 4 == 0) SpellVisuals.beam(level, owner, "star_cross", origin, end);
                if (first != null && age - hits.getOrDefault(first.getUUID(), -100L) >= QizaiSweepRules.BEAM_HIT_COOLDOWN) {
                    first.invulnerableTime = 0;
                    if (ManorSpellEffects.hurt(owner, first, spell, damage)) hits.put(first.getUUID(), age);
                }
            }
        }

        void volley(ServerLevel level, LivingEntity owner, int elapsed) {
            List<LivingEntity> candidates = targets(level, owner);
            for (int index = 0; index < QizaiSweepRules.VOLLEY_STARS; index++) {
                double heading = Math.toRadians(index * 360.0 / QizaiSweepRules.VOLLEY_STARS + elapsed);
                Vec3 direction = new Vec3(Math.cos(heading), 0, Math.sin(heading));
                UUID target = candidates.isEmpty() ? null : candidates.get(index % candidates.size()).getUUID();
                spawn(level, owner, anchor.add(direction.scale(1.4)).add(0, 1.6, 0),
                        direction.scale(QizaiSweepRules.VOLLEY_SPEED), target);
            }
        }

        void rain(ServerLevel level, LivingEntity owner, int wave) {
            for (int index = 0; index < QizaiSweepRules.RAIN_COUNT; index++) {
                double heading = Math.toRadians(initialAngle + wave * QizaiSweepRules.RAIN_ROTATION_PER_WAVE
                        + index * 360.0 / QizaiSweepRules.RAIN_COUNT);
                spawn(level, owner, anchor.add(0, 1.4, 0),
                        new Vec3(Math.cos(heading), 0, Math.sin(heading)).scale(QizaiSweepRules.RAIN_SPEED), null);
            }
            SpellVisuals.show(level, owner, "rose_starfall", Phase.RELEASE, anchor);
        }

        void spawn(ServerLevel level, LivingEntity owner, Vec3 origin, Vec3 velocity, UUID target) {
            if (!level.hasChunkAt(BlockPos.containing(origin))) return;
            QizaiStarEntity entity = new QizaiStarEntity(level, owner, origin, damage);
            entity.setDeltaMovement(velocity);
            if (level.addFreshEntity(entity)) stars.add(new Star(entity, target, level.getGameTime()));
        }

        void steer(ServerLevel level, LivingEntity owner) {
            for (Iterator<Star> iterator = stars.iterator(); iterator.hasNext();) {
                Star star = iterator.next();
                int lifetime = star.target == null ? QizaiRules.STAR_LIFETIME : QizaiSweepRules.VOLLEY_LIFETIME;
                if (star.entity.isRemoved() || !star.entity.isAlive() || level.getGameTime() - star.born >= lifetime) {
                    star.entity.discard();
                    iterator.remove();
                } else if (star.target != null && level.getEntity(star.target) instanceof LivingEntity target
                        && BossSpellEffects.canTarget(owner, target) && owner.hasLineOfSight(target)) {
                    star.entity.setDeltaMovement(QizaiSweepRules.turn(star.entity.getDeltaMovement(),
                            target.getBoundingBox().getCenter().subtract(star.entity.position())));
                }
            }
        }

        void clear() {
            stars.forEach(star -> star.entity.discard());
            stars.clear();
        }
    }
}
