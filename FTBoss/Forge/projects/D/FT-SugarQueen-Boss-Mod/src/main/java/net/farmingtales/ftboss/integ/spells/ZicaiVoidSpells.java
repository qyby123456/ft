package net.farmingtales.ftboss.integ.spells;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.network.ZicaiVoidNetwork;
import net.farmingtales.ftboss.util.ManorSpellRules;
import net.farmingtales.ftboss.util.ManorSpellRules.Kind;
import net.farmingtales.ftboss.util.ZicaiVoidRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

@Mod.EventBusSubscriber(modid = "ftboss")
public final class ZicaiVoidSpells {
    private static final Map<ServerLevel, List<Field>> ACTIVE = new WeakHashMap<>();
    private static final DustParticleOptions DUST = new DustParticleOptions(new Vector3f(1F, 0.85F, 0.5F), 1);

    private ZicaiVoidSpells() {}

    public static boolean matches(Kind kind) { return ZicaiVoidRules.matches(kind.id); }

    public static boolean warning(ServerLevel level, LivingEntity caster, Kind kind, Vec3 point) {
        if (!matches(kind)) return false;
        level.sendParticles(DUST, caster.getX(), caster.getY() + 1.1, caster.getZ(), 8, 0.5, 0.6, 0.5, 0);
        return true;
    }

    public static boolean cast(ServerLevel level, LivingEntity caster, ManorSignatureSpell spell, int rank, Vec3 point) {
        if (!matches(spell.kind)) return false;
        if (!caster.isAlive() || !spell.isEnabled() || !ManorSpellRules.validPoint(point.distanceToSqr(caster.position()))) return true;
        if (caster instanceof EasterBossEntity boss && !ManorSpellCombat.eligible(boss)) return true;
        boolean fountain = spell.kind == Kind.VOID_FOUNTAIN_CASCADE;
        Vec3 direction = point.subtract(caster.position()).multiply(1, 0, 1);
        double angle = Math.atan2(direction.z, direction.x);
        Vec3 origin = caster.position();
        List<ZicaiVoidNetwork.Mark> marks = new ArrayList<>();
        for (ZicaiVoidRules.Mark mark : net.farmingtales.ftboss.util.ManorSpellAreaRules.voidLayout(fountain, angle,
                direction.length(), net.farmingtales.ftboss.skill.SpellVisuals.playerRadiusScale(caster))) {
            Vec3 position = ground(level, caster, origin.add(mark.x(), 0, mark.z()));
            if (position != null) marks.add(new ZicaiVoidNetwork.Mark(position, mark.warning(), mark.strike()));
        }
        if (marks.isEmpty()) return true;
        float power = BossSpellTuning.playerPower(caster, BossSpellTuning.manorBoss(caster)
                ? spell.kind.damage + 8 : spell.getSpellPower(ManorSpellRules.level(rank), caster));
        if (!Float.isFinite(power) || power <= 0) return true;
        List<Field> fields = ACTIVE.computeIfAbsent(level, ignored -> new ArrayList<>());
        fields.removeIf(field -> field.owner.equals(caster.getUUID()) && field.spell.kind == spell.kind);
        if (fields.size() >= ZicaiVoidRules.MAX_FIELDS) fields.remove(0);
        fields.add(new Field(caster.getUUID(), spell, fountain, List.copyOf(marks), power, level.getGameTime(), new HashMap<>()));
        ZicaiVoidNetwork.show(level, caster, fountain, marks);
        level.playSound(null, caster.blockPosition(), SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.PLAYERS, 0.8F, fountain ? 0.65F : 1.1F);
        return true;
    }

    private static Vec3 ground(ServerLevel level, LivingEntity caster, Vec3 point) {
        if (!level.hasChunkAt(BlockPos.containing(point))) return null;
        HitResult hit = level.clip(new ClipContext(point.add(0, 2, 0), point.add(0, -10, 0),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
        return hit.getType() == HitResult.Type.BLOCK ? hit.getLocation().add(0, 0.035, 0) : null;
    }

    @SubscribeEvent
    public static void tick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) return;
        List<Field> fields = ACTIVE.get(level);
        if (fields == null) return;
        Iterator<Field> iterator = fields.iterator();
        while (iterator.hasNext()) {
            Field field = iterator.next();
            long age = level.getGameTime() - field.born;
            int last = field.marks.stream().mapToInt(ZicaiVoidNetwork.Mark::strike).max().orElse(0);
            if (!(level.getEntity(field.owner) instanceof LivingEntity caster) || !caster.isAlive()
                    || !field.spell.isEnabled() || age < 0 || age > last + ZicaiVoidRules.TAIL_TICKS) {
                iterator.remove();
                continue;
            }
            boolean sound = false;
            for (ZicaiVoidNetwork.Mark mark : field.marks) {
                if (!ZicaiVoidRules.hits(field.fountain, (int) age, mark.strike())) continue;
                if (!level.hasChunkAt(BlockPos.containing(mark.position()))) continue;
                strike(level, caster, field, mark.position(), age);
                if (age == mark.strike()) sound = true;
            }
            if (sound) level.playSound(null, caster.blockPosition(), SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(),
                    SoundSource.PLAYERS, 0.55F, field.fountain ? 0.75F : 1.5F);
        }
    }

    private static void strike(ServerLevel level, LivingEntity caster, Field field, Vec3 point, long age) {
        double radius = ZicaiVoidRules.radius(field.fountain) * net.farmingtales.ftboss.skill.SpellVisuals.playerRadiusScale(caster);
        AABB area = new AABB(point.x - radius, point.y, point.z - radius,
                point.x + radius, point.y + ZicaiVoidRules.height(field.fountain), point.z + radius);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
                target -> BossSpellEffects.canTarget(caster, target) && caster.hasLineOfSight(target));
        for (LivingEntity target : targets.stream().limit(ManorSpellRules.MAX_TARGETS).toList()) {
            double reach = radius + target.getBbWidth() * 0.5;
            if (target.position().subtract(point).horizontalDistanceSqr() > reach * reach
                    || age - field.hits.getOrDefault(target.getUUID(), -100L) < ZicaiVoidRules.HIT_INTERVAL) continue;
            if (!ManorSpellEffects.hurt(caster, target, field.spell, field.power)) continue;
            field.hits.put(target.getUUID(), age);
            ManorSpellEffects.effect(caster, target, MobEffects.MOVEMENT_SLOWDOWN, field.fountain ? 50 : 30, 0);
        }
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) { ACTIVE.remove(event.getLevel()); }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) { ACTIVE.clear(); }

    private record Field(UUID owner, ManorSignatureSpell spell, boolean fountain, List<ZicaiVoidNetwork.Mark> marks,
            float power, long born, Map<UUID, Long> hits) {}
}
