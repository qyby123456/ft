package net.farmingtales.ftboss.integ.spells;

import io.redspace.ironsspellbooks.damage.DamageSources;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.network.QizaiBarrageNetwork;
import net.farmingtales.ftboss.util.QizaiBarrageRules;
import net.farmingtales.ftboss.util.QizaiLaserBarrageRules;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "ftboss")
public final class QizaiBarrageEffects {
    private static final Map<ServerLevel, List<Cast>> ACTIVE = new WeakHashMap<>();

    private QizaiBarrageEffects() {}

    public static void cast(ServerLevel level, LivingEntity caster, QizaiBarrageSpell spell, float damage) {
        if (!Float.isFinite(damage) || damage <= 0) return;
        List<Cast> casts = ACTIVE.computeIfAbsent(level, ignored -> new ArrayList<>());
        casts.removeIf(previous -> {
            if (!previous.owner.equals(caster.getUUID())) return false;
            previous.clear(level);
            return true;
        });
        if (casts.size() >= QizaiBarrageRules.MAX_CASTS) casts.remove(0).clear(level);
        Cast cast = new Cast(caster.getUUID(), caster.position(), level.getGameTime(), spell, damage);
        casts.add(cast);
        cast.sync(level);
    }

    @SubscribeEvent
    public static void tick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) return;
        List<Cast> casts = ACTIVE.get(level);
        if (casts == null) return;
        for (Iterator<Cast> iterator = casts.iterator(); iterator.hasNext();) {
            Cast cast = iterator.next();
            long elapsed = level.getGameTime() - cast.started;
            LivingEntity caster = level.getEntity(cast.owner) instanceof LivingEntity living ? living : null;
            if (caster == null || !caster.isAlive() || caster.isRemoved() || caster.isSpectator()
                    || !cast.spell.isEnabled() || elapsed < 0 || elapsed >= QizaiBarrageRules.DURATION
                    || !level.hasChunkAt(BlockPos.containing(cast.center))) {
                cast.clear(level);
                iterator.remove();
            } else cast.tick(level, caster, (int) elapsed);
        }
        if (casts.isEmpty()) ACTIVE.remove(level);
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) ACTIVE.remove(level);
    }

    @SubscribeEvent
    public static void stop(ServerStoppedEvent event) { ACTIVE.clear(); }

    private static final class Beam {
        final Vec3 origin;
        Vec3 end;
        final int spawn;
        final int fire;
        final boolean targeted;
        boolean fired;

        Beam(Vec3 origin, Vec3 end, int spawn, boolean targeted) {
            this.origin = origin;
            this.end = end;
            this.spawn = spawn;
            this.fire = spawn + QizaiLaserBarrageRules.WARNING_TICKS;
            this.targeted = targeted;
        }
    }

    private static final class Cast {
        final UUID owner;
        final Vec3 center;
        final long started;
        final QizaiBarrageSpell spell;
        final float damage;
        final List<Beam> beams = new ArrayList<>();
        int round;
        int nextBeam;

        Cast(UUID owner, Vec3 center, long started, QizaiBarrageSpell spell, float damage) {
            this.owner = owner;
            this.center = center;
            this.started = started;
            this.spell = spell;
            this.damage = damage;
        }

        void tick(ServerLevel level, LivingEntity caster, int age) {
            boolean changed = beams.removeIf(beam -> age >= beam.fire + QizaiLaserBarrageRules.BEAM_FADE);
            int currentRound = Math.min(QizaiLaserBarrageRules.ROUNDS - 1, age / QizaiBarrageRules.ROUND_TICKS);
            if (round != currentRound) { round = currentRound; nextBeam = 0; changed = true; }
            int count = QizaiLaserBarrageRules.roundBeamCount(round);
            while (nextBeam < count && age >= QizaiBarrageRules.spawnTick(round, nextBeam)) {
                spawn(level, caster, age, nextBeam++);
                changed = true;
            }
            for (Beam beam : beams) if (!beam.fired && age >= beam.fire) {
                beam.fired = true;
                beam.end = clip(level, caster, beam.origin, beam.end);
                for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class,
                        new AABB(beam.origin, beam.end).inflate(QizaiLaserBarrageRules.BEAM_RADIUS + 0.5),
                        target -> BossSpellEffects.canTarget(caster, target))) {
                    if (!QizaiLaserBarrageRules.intersects(target.getBoundingBox(), beam.origin, beam.end)) continue;
                    int previousInvulnerability = target.invulnerableTime;
                    target.invulnerableTime = 0;
                    try { DamageSources.applyDamage(target, damage, spell.getDamageSource(caster)); }
                    finally { target.invulnerableTime = Math.max(previousInvulnerability, target.invulnerableTime); }
                }
                level.playSound(null, BlockPos.containing(beam.origin), SoundEvents.GUARDIAN_ATTACK,
                        SoundSource.PLAYERS, beam.targeted ? 0.75F : 0.35F, 1.4F);
                changed = true;
            }
            if (changed) sync(level);
        }

        void spawn(ServerLevel level, LivingEntity caster, int age, int index) {
            double radiusScale = net.farmingtales.ftboss.skill.SpellVisuals.playerRadiusScale(caster);
            double range = QizaiLaserBarrageRules.RANGE * radiusScale;
            double beamLength = QizaiLaserBarrageRules.BEAM_LENGTH * radiusScale;
            var random = caster.getRandom();
            Vec3 origin = null;
            for (int attempt = 0; attempt < QizaiLaserBarrageRules.ANCHOR_ATTEMPTS; attempt++) {
                double heading = random.nextDouble() * Math.PI * 2;
                double radius = Math.sqrt(random.nextDouble()) * QizaiLaserBarrageRules.FIELD_RADIUS * radiusScale;
                double height = QizaiLaserBarrageRules.MIN_ANCHOR_HEIGHT + random.nextDouble()
                        * (QizaiLaserBarrageRules.MAX_ANCHOR_HEIGHT - QizaiLaserBarrageRules.MIN_ANCHOR_HEIGHT);
                Vec3 point = center.add(Math.cos(heading) * radius, height, Math.sin(heading) * radius);
                if (level.hasChunkAt(BlockPos.containing(point)) && level.getWorldBorder().isWithinBounds(BlockPos.containing(point))
                        && level.noCollision(new AABB(point, point).inflate(0.28))) { origin = point; break; }
            }
            if (origin == null) return;
            List<LivingEntity> targets = QizaiLaserBarrageRules.targetsPlayer(index)
                    ? level.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(range, QizaiLaserBarrageRules.RANGE, range),
                            target -> BossSpellEffects.canTarget(caster, target)
                                    && target.position().distanceToSqr(center) <= range * range)
                    : List.of();
            boolean targeted = !targets.isEmpty();
            Vec3 end;
            if (targeted) end = targets.get(random.nextInt(targets.size())).getBoundingBox().getCenter();
            else {
                double heading = random.nextDouble() * Math.PI * 2;
                double vertical = -0.65 + random.nextDouble() * 0.9;
                double horizontal = Math.sqrt(1 - vertical * vertical);
                end = origin.add(new Vec3(Math.cos(heading) * horizontal, vertical,
                        Math.sin(heading) * horizontal).scale(beamLength));
            }
            Vec3 direction = end.subtract(origin);
            if (direction.lengthSqr() > beamLength * beamLength)
                end = origin.add(direction.normalize().scale(beamLength));
            end = clip(level, caster, origin, end);
            if (origin.distanceToSqr(end) < 0.01) return;
            beams.add(new Beam(origin, end, age, targeted));
        }

        void sync(ServerLevel level) {
            CompoundTag cue = new CompoundTag();
            cue.putDouble("radiusScale", net.farmingtales.ftboss.skill.SpellVisuals.playerRadiusScale(level.getEntity(owner)));
            cue.putLong("start", started);
            cue.putInt("duration", QizaiBarrageRules.DURATION);
            cue.putInt("activeTicks", QizaiLaserBarrageRules.ACTIVE_TICKS);
            cue.putInt("fadeTicks", QizaiLaserBarrageRules.BEAM_FADE);
            cue.putInt("roundStart", round * QizaiBarrageRules.ROUND_TICKS);
            point(cue, "center", center);
            ListTag entries = new ListTag();
            for (Beam beam : beams) {
                CompoundTag entry = new CompoundTag();
                point(entry, "", beam.origin);
                point(entry, "end", beam.end);
                entry.putInt("spawn", beam.spawn);
                entry.putInt("fire", beam.fire);
                entry.putBoolean("targeted", beam.targeted);
                entry.putBoolean("fired", beam.fired);
                entries.add(entry);
            }
            cue.put("beams", entries);
            QizaiBarrageNetwork.show(level, owner, center, cue);
        }

        void clear(ServerLevel level) { QizaiBarrageNetwork.show(level, owner, center, new CompoundTag()); }
    }

    private static Vec3 clip(ServerLevel level, LivingEntity caster, Vec3 origin, Vec3 end) {
        if (!level.hasChunksAt(BlockPos.containing(Math.min(origin.x, end.x), Math.min(origin.y, end.y), Math.min(origin.z, end.z)),
                BlockPos.containing(Math.max(origin.x, end.x), Math.max(origin.y, end.y), Math.max(origin.z, end.z)))) return origin;
        return level.clip(new ClipContext(origin, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster)).getLocation();
    }

    private static void point(CompoundTag tag, String prefix, Vec3 point) {
        tag.putDouble(prefix + "X", point.x);
        tag.putDouble(prefix + "Y", point.y);
        tag.putDouble(prefix + "Z", point.z);
    }
}
