package net.farmingtales.ftboss.integ.spells;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.ICastData;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.damage.DamageSources;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.farmingtales.ftboss.boss.ManorSafeTeleport;
import net.farmingtales.ftboss.entity.QizaiSigilEntity;
import net.farmingtales.ftboss.entity.QizaiStarEntity;
import net.farmingtales.ftboss.integ.spells.QizaiPlayerSpells.Kind;
import net.farmingtales.ftboss.item.weapons.QizaiPrayerStaffItem;
import net.farmingtales.ftboss.util.QizaiRules;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class QizaiSpell extends FullPowerBossSpell {
    public final Kind kind;
    private final DefaultConfig config;

    public QizaiSpell(Kind kind) {
        super(kind.mana, kind.damage, 0, 0);
        this.kind = kind;
        castTime = kind.castTicks;
        config = new DefaultConfig().setMinRarity(SpellRarity.LEGENDARY).setMaxLevel(1)
                .setSchoolResource(new ResourceLocation("irons_spellbooks", "ender"))
                .setCooldownSeconds(kind.cooldown).setAllowCrafting(true).build();
    }

    @Override public ResourceLocation getSpellResource() { return new ResourceLocation("ftboss", kind.id); }
    @Override public DefaultConfig getDefaultConfig() { return config; }
    @Override public CastType getCastType() { return CastType.LONG; }
    @Override public boolean allowLooting() { return false; }
    @Override public float getSpellPower(int level, net.minecraft.world.entity.Entity caster) {
        float power = super.getSpellPower(level, caster);
        return caster instanceof LivingEntity living ? BossSpellTuning.playerPower(living, power) : power;
    }
    @Override public Optional<SoundEvent> getCastStartSound() { return Optional.of(SoundEvents.AMETHYST_BLOCK_CHIME); }
    @Override public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(kind == Kind.STEP ? SoundEvents.ENDERMAN_TELEPORT : SoundEvents.AMETHYST_BLOCK_BREAK);
    }

    @Override public List<MutableComponent> getUniqueInfo(int level, LivingEntity caster) {
        List<MutableComponent> lines = new ArrayList<>();
        lines.add(Component.translatable("spell.ftboss." + kind.id + ".effect"));
        if (kind.damage > 0) lines.add(Component.translatable("ui.ftboss.manor.pulse_damage", Math.round(ManorRewardBalance.displayDamage(getSpellPower(level, caster), caster))));
        lines.add(Component.translatable("ui.ftboss.signature.full_power"));
        lines.add(Component.translatable("ui.ftboss.signature.source", Component.translatable("ui.ftboss.manor.owner.11")));
        return lines;
    }

    @Override public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity caster, MagicData data) {
        if (!caster.isAlive() || caster.isSpectator() || !isEnabled()) return false;
        if (level instanceof ServerLevel server) {
            Prepared prepared = prepare(server, caster);
            if (prepared == null) return false;
            data.setAdditionalCastData(prepared);
        }
        return true;
    }

    @Override public void onServerPreCast(Level level, int spellLevel, LivingEntity caster, MagicData data) {
        if (!(level instanceof ServerLevel server)) return;
        Prepared prepared = data.getAdditionalCastData() instanceof Prepared current && current.kind == kind
                ? current : prepare(server, caster);
        if (prepared == null) return;
        data.setAdditionalCastData(prepared);
        int duration = getEffectiveCastTime(spellLevel, caster);
        if (prepared.sigils.isEmpty()) for (Vec3 mark : prepared.marks) {
            QizaiSigilEntity sigil = new QizaiSigilEntity(server, caster, mark, kind.cue, duration);
            if (server.addFreshEntity(sigil)) prepared.sigils.add(sigil);
        }
        if (kind == Kind.PRAYER) QizaiPrayerStaffItem.animate(caster, true);
        super.onServerPreCast(level, spellLevel, caster, data);
    }

    @Override public void onCast(Level level, int spellLevel, LivingEntity caster, CastSource source, MagicData data) {
        if (!(level instanceof ServerLevel server) || !caster.isAlive() || caster.isSpectator() || !isEnabled()
                || !(data.getAdditionalCastData() instanceof Prepared prepared) || prepared.kind != kind
                || prepared.released || prepared.cancelled || prepared.caster != caster || caster.level() != server) return;
        prepared.released = true;
        float damage = getSpellPower(spellLevel, caster);
        if (kind == Kind.STAR) {
            Vec3 origin = caster.getEyePosition().subtract(0, 0.15, 0);
            Vec3 direction = prepared.aim.subtract(origin).normalize();
            if (direction.lengthSqr() < 0.01) direction = caster.getLookAngle();
            for (int index = -1; index <= 1; index++) {
                QizaiStarEntity star = new QizaiStarEntity(server, caster, origin, damage);
                star.setDeltaMovement(direction.yRot(index * 0.19F).scale(QizaiRules.STAR_SPEED));
                server.addFreshEntity(star);
            }
        } else if (kind == Kind.STEP) {
            if (safeStep(server, caster, prepared.aim)) {
                QizaiSigilEntity origin = new QizaiSigilEntity(server, caster, caster.position(), QizaiRules.STEP, 12);
                origin.release();
                server.addFreshEntity(origin);
                ManorSafeTeleport.move(server, caster, prepared.aim);
            }
        } else {
            double radiusScale = net.farmingtales.ftboss.skill.SpellVisuals.playerRadiusScale(caster);
            double searchRadius = (radiusScale > 1 && kind == Kind.PRAYER ? 7 + QizaiRules.radius(kind.cue) : 12) * radiusScale;
            List<LivingEntity> targets = server.getEntitiesOfClass(LivingEntity.class,
                    new AABB(prepared.aim, prepared.aim).inflate(searchRadius, 4, searchRadius), target -> BossSpellEffects.canTarget(caster, target));
            targets.sort(Comparator.comparingDouble(target -> target.distanceToSqr(prepared.aim)));
            int hitCount = 0;
            for (LivingEntity target : targets) {
                for (Vec3 mark : prepared.marks) {
                    if (!server.hasChunkAt(BlockPos.containing(mark))
                            || !net.farmingtales.ftboss.util.ManorSpellAreaRules.inside(kind.cue,
                                    target.getX() - mark.x, target.getY() - mark.y, target.getZ() - mark.z, radiusScale)
                            || !BossSpellEffects.visible(server, caster, mark.add(0, 1.5, 0), target.getEyePosition())) continue;
                    DamageSources.applyDamage(target, damage, getDamageSource(caster));
                    hitCount++;
                    break;
                }
                if (hitCount >= 12) break;
            }
        }
        for (QizaiSigilEntity sigil : prepared.sigils) if (!sigil.isRemoved()) sigil.release();
        if (kind == Kind.PRAYER) QizaiPrayerStaffItem.animate(caster, false);
        super.onCast(level, spellLevel, caster, source, data);
    }

    @Override public void onServerCastComplete(Level level, int spellLevel, LivingEntity caster, MagicData data, boolean cancelled) {
        if (data.getAdditionalCastData() instanceof Prepared prepared && prepared.kind == kind) prepared.reset();
        super.onServerCastComplete(level, spellLevel, caster, data, cancelled);
    }

    private Prepared prepare(ServerLevel level, LivingEntity caster) {
        if (kind == Kind.STEP) {
            Vec3 forward = caster.getLookAngle().multiply(1, 0, 1).normalize();
            for (double distance = 4.5; distance >= 1.5; distance -= 0.5) {
                Vec3 destination = ManorSafeTeleport.find(level, caster, caster.position().add(forward.scale(distance)), 0);
                if (destination != null && safeStep(level, caster, destination))
                    return new Prepared(caster, kind, destination, List.of(destination));
            }
            return null;
        }
        Vec3 aim = BossSpellEffects.aim(caster);
        if (kind == Kind.STAR) return new Prepared(caster, kind, aim, List.of());
        Vec3 center = ground(level, caster, aim);
        if (center == null) return null;
        List<Vec3> marks = new ArrayList<>();
        marks.add(center);
        if (kind == Kind.PRAYER) {
            Vec3 forward = center.subtract(caster.position()).multiply(1, 0, 1).normalize();
            if (forward.lengthSqr() < 0.01) forward = new Vec3(0, 0, 1);
            Vec3 side = new Vec3(-forward.z, 0, forward.x).scale(7 * net.farmingtales.ftboss.skill.SpellVisuals.playerRadiusScale(caster));
            for (int sign : new int[]{-1, 1}) {
                Vec3 mark = ground(level, caster, center.add(side.scale(sign)));
                if (mark != null) marks.add(mark);
            }
        }
        return new Prepared(caster, kind, center, List.copyOf(marks));
    }

    private static Vec3 ground(ServerLevel level, LivingEntity caster, Vec3 aim) {
        if (!level.hasChunkAt(BlockPos.containing(aim)) || !level.getWorldBorder().isWithinBounds(BlockPos.containing(aim))) return null;
        var hit = level.clip(new ClipContext(aim.add(0, 0.5, 0), aim.add(0, -8, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
        if (hit.getType() == HitResult.Type.MISS || hit.getDirection() != net.minecraft.core.Direction.UP) return null;
        Vec3 point = hit.getLocation().add(0, 0.025, 0);
        return BossSpellEffects.visible(level, caster, caster.getEyePosition(), point.add(0, 0.1, 0)) ? point : null;
    }

    private static boolean safeStep(ServerLevel level, LivingEntity caster, Vec3 point) {
        Vec3 offset = point.subtract(caster.position());
        if (offset.lengthSqr() > 25 || offset.lengthSqr() < 1 || Math.abs(offset.y) > 1.5
                || !level.hasChunkAt(BlockPos.containing(point))) return false;
        Vec3 floor = ManorSafeTeleport.find(level, caster, point, 0);
        if (floor == null || floor.distanceToSqr(point) > 0.01) return false;
        for (int step = 1; step <= 20; step++) {
            AABB body = caster.getBoundingBox().move(offset.scale(step / 20.0));
            if (!level.getWorldBorder().isWithinBounds(body) || !level.noCollision(caster, body)
                    || level.containsAnyLiquid(body)) return false;
        }
        return true;
    }

    private static final class Prepared implements ICastData {
        final LivingEntity caster;
        final Kind kind;
        final Vec3 aim;
        final List<Vec3> marks;
        final List<QizaiSigilEntity> sigils = new ArrayList<>();
        boolean released;
        boolean cancelled;

        Prepared(LivingEntity caster, Kind kind, Vec3 aim, List<Vec3> marks) {
            this.caster = caster;
            this.kind = kind;
            this.aim = aim;
            this.marks = marks;
        }

        @Override public void reset() {
            cancelled = !released;
            if (!released) for (QizaiSigilEntity sigil : sigils) sigil.discard();
            if (kind == Kind.PRAYER) QizaiPrayerStaffItem.animate(caster, false);
        }
    }
}
