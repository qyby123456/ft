package net.farmingtales.ftboss.entity;

import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.util.QizaiRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public final class QizaiStarEntity extends Projectile {
    private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> DATA_TRACKING =
            net.minecraft.network.syncher.SynchedEntityData.defineId(QizaiStarEntity.class, net.minecraft.network.syncher.EntityDataSerializers.BOOLEAN);
    private java.util.UUID trackingTarget;
    private int lifetime = QizaiRules.STAR_LIFETIME;
    private boolean spellCast;
    private float spellDamage;

    public QizaiStarEntity(EntityType<? extends QizaiStarEntity> type, Level level) { super(type, level); }

    public QizaiStarEntity(ServerLevel level, EasterBossEntity owner, Vec3 origin) {
        this(FTEntities.QIZAI_STAR.get(), level);
        setOwner(owner);
        setPos(origin);
        ManorFightMembers.inherit(owner, this);
    }

    public QizaiStarEntity(ServerLevel level, LivingEntity owner, Vec3 origin, float damage) {
        this(FTEntities.QIZAI_STAR.get(), level);
        setOwner(owner);
        setPos(origin);
        spellCast = true;
        spellDamage = Float.isFinite(damage) ? Math.max(0, Math.min(100000, damage)) : 0;
        ManorFightMembers.inherit(owner, this);
    }

    @Override
    protected void defineSynchedData() { this.entityData.define(DATA_TRACKING, false); }

    public boolean isTrackingMissile() { return this.entityData.get(DATA_TRACKING); }

    public void track(Player target, int ticks) {
        trackingTarget = target.getUUID();
        lifetime = Math.max(1, Math.min(QizaiRules.STAR_LIFETIME, ticks));
        this.entityData.set(DATA_TRACKING, true);
    }

    private void steer(ServerLevel level) {
        if (trackingTarget == null || spellCast) return;
        Player target = level.getPlayerByUUID(trackingTarget);
        if (target == null || !target.isAlive() || target.isCreative() || target.isSpectator()
                || target.isRemoved() || target.distanceToSqr(this) > 48 * 48
                || getOwner() == null || getOwner().isAlliedTo(target) || target.isAlliedTo(getOwner())) {
            trackingTarget = null;
            return;
        }
        Vec3 desired = target.getBoundingBox().getCenter().subtract(position()).normalize();
        Vec3 current = getDeltaMovement().normalize();
        double dot = Math.max(-1, Math.min(1, current.dot(desired)));
        double angle = Math.acos(dot);
        double turn = net.farmingtales.ftboss.util.QizaiArsenalRules.MISSILE_TURN;
        Vec3 direction = desired;
        if (angle > turn) {
            Vec3 tangent = desired.subtract(current.scale(dot)).normalize();
            if (tangent.lengthSqr() < 0.01) tangent = current.cross(Math.abs(current.y) < 0.9
                    ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0)).normalize();
            direction = current.scale(Math.cos(turn)).add(tangent.scale(Math.sin(turn))).normalize();
        }
        setDeltaMovement(direction.scale(net.farmingtales.ftboss.util.QizaiArsenalRules.MISSILE_SPEED));
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            if (!(getOwner() instanceof LivingEntity owner) || !owner.isAlive() || owner.isRemoved() || owner.isSpectator()
                    || owner instanceof net.minecraft.world.entity.Mob mob && mob.isNoAi()
                    || owner.level() != level() || !spellCast && !(owner instanceof EasterBossEntity)
                    || --lifetime <= 0) { discard(); return; }
            if (level() instanceof ServerLevel server) steer(server);
            HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
            if (hit.getType() != HitResult.Type.MISS) {
                if (hit instanceof EntityHitResult entityHit) {
                    if (spellCast) {
                        var spell = net.farmingtales.ftboss.integ.spells.QizaiPlayerSpells.spell(net.farmingtales.ftboss.integ.spells.QizaiPlayerSpells.Kind.STAR);
                        io.redspace.ironsspellbooks.damage.DamageSources.applyDamage(entityHit.getEntity(), spellDamage, spell.getDamageSource(this, owner));
                    } else entityHit.getEntity().hurt(level().damageSources().indirectMagic(this, owner), QizaiRules.STAR_DAMAGE);
                }
                discard();
                return;
            }
        }
        setPos(position().add(getDeltaMovement()));
        updateRotation();
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (isTrackingMissile() && getOwner() != null
                && (getOwner().isAlliedTo(entity) || entity.isAlliedTo(getOwner()))) return false;
        if (spellCast) return getOwner() instanceof LivingEntity owner && entity instanceof LivingEntity target
                && net.farmingtales.ftboss.integ.spells.BossSpellEffects.canTarget(owner, target) && super.canHitEntity(entity);
        return entity instanceof Player player && player.isAlive() && !player.isCreative() && !player.isSpectator()
                && super.canHitEntity(entity);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Life", lifetime);
        if (trackingTarget != null) tag.putUUID("TrackingTarget", trackingTarget);
        tag.putBoolean("Tracking", isTrackingMissile());
        tag.putBoolean("QizaiSpell", spellCast);
        tag.putFloat("SpellDamage", spellDamage);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        lifetime = Math.max(1, Math.min(QizaiRules.STAR_LIFETIME, tag.getInt("Life")));
        trackingTarget = tag.hasUUID("TrackingTarget") ? tag.getUUID("TrackingTarget") : null;
        this.entityData.set(DATA_TRACKING, tag.getBoolean("Tracking"));
        spellCast = tag.getBoolean("QizaiSpell");
        spellDamage = Float.isFinite(tag.getFloat("SpellDamage")) ? Math.max(0, Math.min(100000, tag.getFloat("SpellDamage"))) : 0;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() { return NetworkHooks.getEntitySpawningPacket(this); }
}
