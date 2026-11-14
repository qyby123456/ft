package net.farmingtales.ftboss.integ.spells;

import io.redspace.ironsspellbooks.damage.DamageSources;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.util.BossSpellRules;
import net.farmingtales.ftboss.util.BossSpellRules.Kind;
import net.farmingtales.ftboss.skill.SpellVisuals;
import net.farmingtales.ftboss.util.SpellVisualRules.Phase;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.farmingtales.ftboss.util.SpellBeamModels;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Vector3f;

public final class BossSpellEffects {
    private static final Map<Kind, DustParticleOptions> COLORS = new EnumMap<>(Kind.class);
    private static final Map<ServerLevel, List<SporeField>> FIELDS = new WeakHashMap<>();

    static {
        for (Kind kind : Kind.values()) COLORS.put(kind, new DustParticleOptions(new Vector3f(
                (kind.color >> 16 & 255) / 255F, (kind.color >> 8 & 255) / 255F, (kind.color & 255) / 255F), 1.3F));
    }

    private BossSpellEffects() {}

    public static boolean canTarget(LivingEntity caster, LivingEntity target) {
        if (SparrowBorrowedSpells.friendly(caster, target)) return false;
        if (target == caster || !target.isAlive() || target.isRemoved() || target.isSpectator()
                || target instanceof ArmorStand || DamageSources.isFriendlyFireBetween(caster, target)
                || caster.isAlliedTo(target) || target.isAlliedTo(caster)) return false;
        if (target instanceof Player player && (player.isCreative() || player.isSpectator()
                || caster instanceof Player && (!((Player) caster).canHarmPlayer(player)
                || caster.getServer() == null || !caster.getServer().isPvpAllowed()))) return false;
        if (target instanceof OwnableEntity pet && (caster.getUUID().equals(pet.getOwnerUUID())
                || pet.getOwner() != null && caster.isAlliedTo(pet.getOwner()))) return false;
        return !(caster instanceof Mob && EntityType.getKey(caster.getType()).getNamespace().equals("ftboss")
                && EntityType.getKey(target.getType()).getNamespace().equals("ftboss"));
    }

    public static Vec3 aim(LivingEntity caster) {
        Vec3 start = caster.getEyePosition();
        Vec3 end = start.add(caster.getLookAngle().scale(BossSpellRules.RANGE));
        if (caster instanceof Mob mob && mob.getTarget() != null && canTarget(caster, mob.getTarget())
                && caster.hasLineOfSight(mob.getTarget())) {
            Vec3 offset = mob.getTarget().position().add(0, 0.5, 0).subtract(start);
            end = start.add(offset.normalize().scale(Math.min(BossSpellRules.RANGE, offset.length())));
        }
        Vec3 clipped = caster.level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, caster)).getLocation();
        EntityHitResult entity = ProjectileUtil.getEntityHitResult(caster, start, clipped,
                caster.getBoundingBox().expandTowards(clipped.subtract(start)).inflate(1),
                candidate -> candidate instanceof LivingEntity living && canTarget(caster, living), start.distanceToSqr(clipped));
        return entity == null ? clipped : entity.getLocation();
    }

    static boolean visible(ServerLevel level, LivingEntity caster, Vec3 start, Vec3 end) {
        return level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster))
                .getType() == HitResult.Type.MISS;
    }

    static List<LivingEntity> targets(ServerLevel level, LivingEntity caster, Vec3 center, double radius) {
        return level.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(radius, 3, radius),
                target -> canTarget(caster, target) && target.position().distanceToSqr(center) <= radius * radius + 4
                        && visible(level, caster, center.add(0, 0.2, 0), target.getEyePosition()))
                .stream().sorted(Comparator.comparingDouble(target -> target.distanceToSqr(center)))
                .limit(BossSpellRules.MAX_TARGETS).toList();
    }

    public static void warning(ServerLevel level, LivingEntity caster, Kind kind, Vec3 center) {
        SpellVisuals.show(level, caster, kind.spell, Phase.CHARGE, center);
        ring(level, kind, center, BossSpellRules.RADIUS, 16);
        level.sendParticles(COLORS.get(kind), caster.getX(), caster.getY() + 1.2, caster.getZ(), 4, 0.4, 0.6, 0.4, 0);
    }

    static void ring(ServerLevel level, Kind kind, Vec3 center, double radius, int points) {
        for (int index = 0; index < points; index++) {
            double angle = index * Math.PI * 2 / points;
            level.sendParticles(COLORS.get(kind), center.x + Math.cos(angle) * radius,
                    center.y + 0.12, center.z + Math.sin(angle) * radius, 1, 0, 0.02, 0, 0);
        }
    }

    static void beam(ServerLevel level, LivingEntity caster, Kind kind, Vec3 start, Vec3 end) {
        SpellVisuals.beam(level, caster, kind.spell, start, end);
        if (SpellBeamModels.replaces(SpellVisualRules.find(kind.spell), Phase.BEAM)) return;
        int points = Math.max(2, Math.min(40, (int) (start.distanceTo(end) * 2)));
        for (int index = 0; index <= points; index++) {
            Vec3 point = start.lerp(end, index / (double) points);
            level.sendParticles(COLORS.get(kind), point.x, point.y, point.z, 1, 0.04, 0.04, 0.04, 0);
        }
    }

    public static void cast(ServerLevel level, LivingEntity caster, BossSignatureSpell spell, int spellLevel, Vec3 center) {
        if (!Double.isFinite(center.x) || !Double.isFinite(center.y) || !Double.isFinite(center.z)
                || center.distanceToSqr(caster.position()) > 32 * 32 || !level.hasChunkAt(BlockPos.containing(center))) return;
        /* 2026-09-19: 玩家用卷轴施法时数值 ×2（Boss 自己施法不受影响）。 */
        float damage = BossSpellTuning.playerPower(caster, BossSpellTuning.regularBoss(caster)
                ? BossSpellTuning.scaledPower(spell, caster, BossSpellTuning.signaturePower(spell.kind)) : spell.getSpellPower(spellLevel, caster));
        SpellVisuals.show(level, caster, spell.kind.spell, Phase.RELEASE, center);
        if (BossSpellRules.expanded(spell.kind)) {
            BossExpandedSpellEffects.cast(level, caster, spell, center, damage);
            return;
        }
        switch (spell.kind) {
            case FROST, TEA, WINE -> bloom(level, caster, spell, center, damage);
            case DEFECT -> chain(level, caster, spell, spellLevel, center, damage);
            case COMPOST -> {
                spores(level, caster, spell, center, damage);
                List<SporeField> fields = FIELDS.computeIfAbsent(level, ignored -> new ArrayList<>());
                fields.removeIf(field -> field.caster().equals(caster.getUUID()));
                if (fields.size() >= 64) fields.remove(0);
                fields.add(new SporeField(caster.getUUID(), center, damage, level.getGameTime()));
            }
            case APPLE -> verdict(level, caster, spell, center, damage);
            case TIDE -> tide(level, caster, spell, center, damage);
            default -> BossSecondSpellEffects.cast(level, caster, spell, spellLevel, center, damage);
        }
    }

    private static void bloom(ServerLevel level, LivingEntity caster, BossSignatureSpell spell, Vec3 center, float damage) {
        ring(level, spell.kind, center, BossSpellRules.RADIUS, 36);
        float stolen = 0;
        for (LivingEntity target : targets(level, caster, center, BossSpellRules.RADIUS)) {
            float before = target.getHealth();
            if (!DamageSources.applyDamage(target, damage, spell.getDamageSource(caster))) continue;
            switch (spell.kind) {
                case FROST -> {
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, BossSpellTuning.buffTicks(caster, MobEffects.MOVEMENT_SLOWDOWN, 80), 2), caster);
                    target.setTicksFrozen(Math.max(target.getTicksFrozen(), 100));
                    level.sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY() + 1, target.getZ(), 18, 0.5, 0.8, 0.5, 0.02);
                }
                case TEA -> {
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, BossSpellTuning.buffTicks(caster, MobEffects.MOVEMENT_SLOWDOWN, 60), 3), caster);
                    stolen += Math.max(0, before - target.getHealth()) * 0.15F;
                    beam(level, caster, Kind.TEA, target.getEyePosition(), caster.getEyePosition());
                }
                case WINE -> {
                    Vec3 pull = center.subtract(target.position()).multiply(1, 0, 1);
                    if (pull.lengthSqr() > 0.04) net.farmingtales.ftboss.util.BossKnockback.push(target, pull.normalize().x * 0.6, 0.12, pull.normalize().z * 0.6);
                    target.hurtMarked = true;
                    target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, BossSpellTuning.buffTicks(caster, MobEffects.WEAKNESS, 100), 0), caster);
                }
                default -> { }
            }
        }
        if (stolen > 0) caster.heal(Math.min(6, stolen));
        if (spell.kind == Kind.WINE) {
            for (int height = 0; height < 4; height++) ring(level, Kind.WINE, center.add(0, height * 0.45, 0), 3 - height * 0.55, 16);
        }
    }

    private static void chain(ServerLevel level, LivingEntity caster, BossSignatureSpell spell, int spellLevel, Vec3 center, float damage) {
        Set<UUID> hit = new HashSet<>();
        Vec3 previous = caster.getEyePosition();
        Vec3 search = center;
        for (int jump = 0; jump < BossSpellRules.chainTargets(spellLevel); jump++) {
            Vec3 origin = previous;
            LivingEntity target = targets(level, caster, search, jump == 0 ? 4 : 6).stream()
                    .filter(candidate -> !hit.contains(candidate.getUUID()) && visible(level, caster, origin, candidate.getEyePosition()))
                    .findFirst().orElse(null);
            if (target == null) break;
            hit.add(target.getUUID());
            beam(level, caster, Kind.DEFECT, previous, target.getEyePosition());
            DamageSources.applyDamage(target, damage * BossSpellRules.chainScale(jump), spell.getDamageSource(caster));
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1, target.getZ(), 14, 0.4, 0.6, 0.4, 0.04);
            previous = target.getEyePosition();
            search = target.position();
        }
    }

    private static void spores(ServerLevel level, LivingEntity caster, BossSignatureSpell spell, Vec3 center, float damage) {
        SpellVisuals.show(level, caster, spell.kind.spell, Phase.PULSE, center);
        ring(level, Kind.COMPOST, center, 4, 24);
        level.sendParticles(COLORS.get(Kind.COMPOST), center.x, center.y + 0.7, center.z, 32, 2, 0.65, 2, 0);
        for (LivingEntity target : targets(level, caster, center, 4)) {
            if (DamageSources.applyDamage(target, damage, spell.getDamageSource(caster))) {
                target.addEffect(new MobEffectInstance(MobEffects.POISON, BossSpellTuning.buffTicks(caster, MobEffects.POISON, 80), 0), caster);
            }
        }
    }

    private static void verdict(ServerLevel level, LivingEntity caster, BossSignatureSpell spell, Vec3 center, float damage) {
        Vec3 start = caster.getEyePosition();
        Vec3 direction = center.subtract(start).normalize();
        Set<UUID> hit = new HashSet<>();
        for (int ray = -1; ray <= 1; ray++) {
            Vec3 end = level.clip(new ClipContext(start, start.add(direction.yRot(ray * 0.16F).scale(24)),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster)).getLocation();
            Vec3 segment = end.subtract(start);
            beam(level, caster, Kind.APPLE, start, end);
            List<LivingEntity> candidates = level.getEntitiesOfClass(LivingEntity.class, new AABB(start, end).inflate(1.2),
                    target -> canTarget(caster, target)).stream()
                    .sorted(Comparator.comparingDouble(caster::distanceToSqr)).limit(BossSpellRules.MAX_TARGETS).toList();
            for (LivingEntity target : candidates) {
                Vec3 body = target.getBoundingBox().getCenter();
                double progress = segment.lengthSqr() < 0.01 ? -1 : body.subtract(start).dot(segment) / segment.lengthSqr();
                if (progress < 0 || progress > 1 || body.distanceToSqr(start.add(segment.scale(progress))) > 1.44
                        || !visible(level, caster, start, body) || !hit.add(target.getUUID())) continue;
                if (DamageSources.applyDamage(target, damage, spell.getDamageSource(caster))) {
                    target.addEffect(new MobEffectInstance(MobEffects.GLOWING, BossSpellTuning.buffTicks(caster, MobEffects.GLOWING, 100), 0), caster);
                    caster.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, BossSpellTuning.buffTicks(caster, MobEffects.ABSORPTION, 80), 0), caster);
                }
            }
        }
    }

    private static void tide(ServerLevel level, LivingEntity caster, BossSignatureSpell spell, Vec3 center, float damage) {
        Vec3 direction = center.subtract(caster.position()).multiply(1, 0, 1).normalize();
        if (direction.lengthSqr() < 0.01) direction = caster.getLookAngle().multiply(1, 0, 1).normalize();
        for (int step = 1; step <= 6; step++) {
            double distance = step * 2;
            Vec3 middle = caster.position().add(direction.scale(distance)).add(0, 0.7, 0);
            Vec3 side = new Vec3(-direction.z, 0, direction.x).scale(1.2 + distance * 0.45);
            beam(level, caster, Kind.TIDE, middle.subtract(side), middle.add(side));
        }
        for (LivingEntity target : targets(level, caster, caster.position(), 14)) {
            Vec3 offset = target.position().subtract(caster.position());
            double forward = offset.x * direction.x + offset.z * direction.z;
            double sideways = offset.x * -direction.z + offset.z * direction.x;
            if (!BossSpellRules.cone(forward, sideways) || Math.abs(offset.y) > 3) continue;
            if (DamageSources.applyDamage(target, damage, spell.getDamageSource(caster))) {
                net.farmingtales.ftboss.util.BossKnockback.knockback(target, 0.9, -direction.x, -direction.z);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, BossSpellTuning.buffTicks(caster, MobEffects.MOVEMENT_SLOWDOWN, 60), 1), caster);
                target.clearFire();
                level.sendParticles(ParticleTypes.SPLASH, target.getX(), target.getY() + 0.5, target.getZ(), 20, 0.6, 0.6, 0.6, 0.05);
            }
        }
    }

    @SubscribeEvent
    public static void tickFields(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) return;
        List<SporeField> fields = FIELDS.get(level);
        if (fields == null) return;
        fields.removeIf(field -> {
            long elapsed = level.getGameTime() - field.started();
            Entity entity = level.getEntity(field.caster());
            if (elapsed < 0 || elapsed >= 60 || !(entity instanceof LivingEntity caster) || !caster.isAlive()
                    || !level.hasChunkAt(BlockPos.containing(field.center()))) return true;
            if (elapsed > 0 && BossSpellRules.sporePulse((int) elapsed)) {
                spores(level, caster, BossIronSpells.spell(Kind.COMPOST), field.center(), field.damage());
            }
            return false;
        });
        if (fields.isEmpty()) FIELDS.remove(level);
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) { FIELDS.remove(event.getLevel()); }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) { FIELDS.clear(); }

    private record SporeField(UUID caster, Vec3 center, float damage, long started) {}
}
