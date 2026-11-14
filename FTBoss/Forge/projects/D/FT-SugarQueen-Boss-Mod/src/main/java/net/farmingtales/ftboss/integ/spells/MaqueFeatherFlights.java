package net.farmingtales.ftboss.integ.spells;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.boss.ManorFightData;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.skill.SpellVisuals;
import net.farmingtales.ftboss.util.FlywheelCombatRules;
import net.farmingtales.ftboss.util.MaqueRicochetRules;
import net.farmingtales.ftboss.util.ManorSpellRules;
import net.farmingtales.ftboss.util.ManorSpellRules.Kind;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.farmingtales.ftboss.util.SpellVisualRules.Phase;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
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
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(modid = "ftboss")
public final class MaqueFeatherFlights {
    private static final Map<ServerLevel, List<Flight>> ACTIVE = new WeakHashMap<>();
    private static final Map<ServerLevel, Map<UUID, Long>> LAST_HIT = new WeakHashMap<>();

    private MaqueFeatherFlights() {}

    public static boolean cast(ServerLevel level, LivingEntity caster, ManorSignatureSpell spell, int rank, Vec3 center) {
        if (spell.kind != Kind.FEATHER_FAN) return false;
        if (!caster.isAlive() || caster.isRemoved() || caster instanceof EasterBossEntity boss && boss.isMini()
                || !ManorSpellRules.validPoint(center.distanceToSqr(caster.position()))
                || !level.hasChunkAt(BlockPos.containing(center))) return true;
        float damage = BossSpellTuning.playerPower(caster, BossSpellTuning.manorBoss(caster)
                ? ManorBossSpellProfile.power(spell.kind) : spell.getSpellPower(ManorSpellRules.level(rank), caster));
        if (!Float.isFinite(damage) || damage < 0) return true;
        List<Flight> flights = ACTIVE.computeIfAbsent(level, unused -> new ArrayList<>());
        if (flights.size() + MaqueRicochetRules.FEATHERS > MaqueRicochetRules.MAX_FLIGHTS) return true;
        SpellVisuals.show(level, caster, spell.kind.id, Phase.RELEASE, center);
        Vec3 direction = center.subtract(caster.getEyePosition()).normalize();
        if (direction.lengthSqr() < 1e-8) direction = caster.getLookAngle();
        double radius = BossSpellTuning.radius(caster, MaqueRicochetRules.AREA_RADIUS);
        for (int lane = -2; lane <= 2; lane++) {
            Vec3 velocity = direction.yRot(lane * 0.16F).scale(1.1);
            Flight flight = new Flight(caster.getUUID(), center, radius, damage, level.getGameTime(),
                    caster.getEyePosition(), velocity);
            flights.add(flight);
            send(level, flight, false);
        }
        return true;
    }

    private static boolean valid(ServerLevel level, LivingEntity caster, Flight flight, LivingEntity target) {
        return target.level() == level && BossSpellEffects.canTarget(caster, target)
                && (caster instanceof Player || target instanceof Player)
                && target.distanceToSqr(flight.center) <= flight.radius * flight.radius;
    }

    private static List<LivingEntity> targets(ServerLevel level, LivingEntity caster, Flight flight) {
        return level.getEntitiesOfClass(LivingEntity.class, new AABB(flight.center, flight.center).inflate(flight.radius),
                target -> valid(level, caster, flight, target) && visible(level, caster, flight.position,
                        target.getBoundingBox().getCenter()));
    }

    private static boolean visible(ServerLevel level, LivingEntity caster, Vec3 start, Vec3 end) {
        return level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster))
                .getType() == HitResult.Type.MISS;
    }

    private static void advance(ServerLevel level, Flight flight, long now) {
        Entity entity = level.getEntity(flight.caster);
        LivingEntity caster = entity instanceof LivingEntity living ? living : null;
        boolean ownerValid = caster != null && caster.isAlive() && !caster.isRemoved()
                && !ManorFightData.get(level.getServer()).retired(ManorFightMembers.fightId(caster));
        if (MaqueRicochetRules.finished(flight.hits, now - flight.born, ownerValid)
                || !level.hasChunkAt(BlockPos.containing(flight.position))) {
            flight.done = true;
            send(level, flight, true);
            return;
        }
        LivingEntity target = flight.target == null ? null : level.getEntity(flight.target) instanceof LivingEntity living ? living : null;
        if (target == null || !valid(level, caster, flight, target)
                || !visible(level, caster, flight.position, target.getBoundingBox().getCenter())) {
            List<LivingEntity> candidates = targets(level, caster, flight);
            int previous = -1;
            for (int index = 0; index < candidates.size(); index++) {
                if (candidates.get(index).getUUID().equals(flight.previous)) previous = index;
            }
            int selected = MaqueRicochetRules.choose(candidates.size(), previous, level.random.nextDouble());
            target = selected < 0 ? null : candidates.get(selected);
            flight.target = target == null ? null : target.getUUID();
        }
        if (target != null) {
            Vec3 direction = target.getBoundingBox().getCenter().subtract(flight.position);
            double[] next = MaqueRicochetRules.steer(new double[]{flight.velocity.x, flight.velocity.y, flight.velocity.z},
                    new double[]{direction.x, direction.y, direction.z});
            flight.velocity = new Vec3(next[0], next[1], next[2]);
        }
        Vec3 end = flight.position.add(flight.velocity);
        HitResult wall = level.clip(new ClipContext(flight.position, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
        double blockDistance = wall.getType() == HitResult.Type.MISS ? Double.POSITIVE_INFINITY : flight.position.distanceToSqr(wall.getLocation());
        LivingEntity contact = null;
        Vec3 contactPoint = null;
        double nearest = blockDistance;
        for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class,
                new AABB(flight.position, end).inflate(MaqueRicochetRules.COLLISION_RADIUS + 1),
                candidate -> valid(level, caster, flight, candidate))) {
            AABB body = candidate.getBoundingBox().inflate(MaqueRicochetRules.COLLISION_RADIUS);
            Vec3 point = body.contains(flight.position) ? flight.position : body.clip(flight.position, end).orElse(null);
            if (point == null) continue;
            double distance = flight.position.distanceToSqr(point);
            if (distance < nearest) { nearest = distance; contact = candidate; contactPoint = point; }
        }
        if (contact != null && now >= flight.ready) {
            Map<UUID, Long> stamps = LAST_HIT.computeIfAbsent(level, unused -> new HashMap<>());
            int cooldown = FlywheelCombatRules.hitCooldown(caster.distanceTo(contact), false);
            if (FlywheelCombatRules.hitReady(now, stamps.get(contact.getUUID()), cooldown)) {
                ManorSignatureSpell spell = ManorIronSpells.spell(Kind.FEATHER_FAN);
                flight.ready = now + cooldown;
                if (ManorSpellEffects.hurt(caster, contact, spell, flight.damage)) {
                    stamps.put(contact.getUUID(), now);
                    flight.hits++;
                    flight.previous = contact.getUUID();
                    flight.target = null;
                    ManorSpellEffects.effect(caster, contact, MobEffects.GLOWING, 80, 0);
                    if (flight.hits == MaqueRicochetRules.HITS) {
                        flight.position = contactPoint;
                        flight.done = true;
                        send(level, flight, true);
                        return;
                    }
                }
            }
        }
        if (wall.getType() != HitResult.Type.MISS) {
            flight.position = wall.getLocation();
            flight.done = true;
            send(level, flight, true);
            return;
        }
        flight.position = end;
        if ((now - flight.born) % 2 == 0) send(level, flight, false);
    }

    private static void send(ServerLevel level, Flight flight, boolean done) {
        SpellVisuals.send(level, new SpellVisualPacket(level.dimension().location(), Integer.MAX_VALUE, flight.id,
                SpellVisualRules.find("feather_fan"), done ? Phase.PULSE : Phase.BEAM,
                flight.position, done ? flight.position : flight.position.add(flight.velocity)));
    }

    @SubscribeEvent
    public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || ServerLifecycleHooks.getCurrentServer() == null) return;
        for (var entry : List.copyOf(ACTIVE.entrySet())) {
            ServerLevel level = entry.getKey();
            long now = level.getGameTime();
            for (Flight flight : List.copyOf(entry.getValue())) advance(level, flight, now);
            entry.getValue().removeIf(flight -> flight.done);
            Map<UUID, Long> stamps = LAST_HIT.get(level);
            if (stamps != null && now % 200 == 0) stamps.entrySet().removeIf(stamp -> now < stamp.getValue() || now - stamp.getValue() > 400);
        }
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) { ACTIVE.remove(event.getLevel()); LAST_HIT.remove(event.getLevel()); }

    @SubscribeEvent
    public static void stop(ServerStoppedEvent event) { ACTIVE.clear(); LAST_HIT.clear(); }

    private static final class Flight {
        final UUID id = UUID.randomUUID();
        final UUID caster;
        final Vec3 center;
        final double radius;
        final float damage;
        final long born;
        Vec3 position;
        Vec3 velocity;
        UUID target;
        UUID previous;
        int hits;
        long ready;
        boolean done;

        Flight(UUID caster, Vec3 center, double radius, float damage, long born, Vec3 position, Vec3 velocity) {
            this.caster = caster;
            this.center = center;
            this.radius = radius;
            this.damage = damage;
            this.born = born;
            this.position = position;
            this.velocity = velocity;
        }
    }
}
