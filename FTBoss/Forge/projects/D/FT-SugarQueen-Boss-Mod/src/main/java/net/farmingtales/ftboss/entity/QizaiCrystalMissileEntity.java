package net.farmingtales.ftboss.entity;

import java.util.UUID;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.skill.QizaiCrystals;
import net.farmingtales.ftboss.util.QizaiCrystalRules;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public final class QizaiCrystalMissileEntity extends Projectile {
    private UUID crystalId;
    private UUID targetId;
    private int lifetime = QizaiCrystalRules.MISSILE_LIFETIME;

    public QizaiCrystalMissileEntity(EntityType<? extends QizaiCrystalMissileEntity> type, Level level) { super(type, level); setNoGravity(true); }

    public QizaiCrystalMissileEntity(ServerLevel level, QizaiCrystalEntity crystal, EasterBossEntity owner, LivingEntity target) {
        this(FTEntities.QIZAI_CRYSTAL_MISSILE.get(), level);
        setOwner(owner);
        crystalId = crystal.getUUID();
        targetId = target.getUUID();
        ManorFightMembers.inherit(owner, this);
    }

    @Override protected void defineSynchedData() {}

    @Override public void tick() {
        super.tick();
        if (level() instanceof ServerLevel level) {
            if (!(getOwner() instanceof EasterBossEntity owner) || !QizaiCrystals.validOwner(owner)
                    || crystalId == null || !(level.getEntity(crystalId) instanceof QizaiCrystalEntity crystal)
                    || !crystal.isAlive() || crystal.isNoAi() || !QizaiCrystals.current(crystal)
                    || QizaiCrystals.combatTarget(owner) == null || --lifetime <= 0) { discard(); return; }
            var target = targetId == null ? null : level.getEntity(targetId);
            if (!QizaiCrystals.target(owner, target)) { discard(); return; }
            Vec3 desired = target.getEyePosition().subtract(position()).normalize().scale(0.8);
            setDeltaMovement(getDeltaMovement().scale(0.85).add(desired.scale(0.15)).normalize().scale(0.8));
            HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
            if (hit.getType() != HitResult.Type.MISS) {
                if (hit instanceof EntityHitResult contact) {
                    if (contact.getEntity() instanceof LivingEntity living) living.invulnerableTime = 0;
                    contact.getEntity().hurt(level.damageSources().indirectMagic(this, owner), QizaiCrystalRules.MISSILE_DAMAGE);
                }
                level.sendParticles(ParticleTypes.END_ROD, hit.getLocation().x, hit.getLocation().y, hit.getLocation().z, 8, 0.15, 0.15, 0.15, 0.05);
                discard();
                return;
            }
        }
        setPos(position().add(getDeltaMovement()));
        updateRotation();
    }

    @Override protected boolean canHitEntity(Entity entity) {
        return getOwner() instanceof EasterBossEntity owner && QizaiCrystals.target(owner, entity) && super.canHitEntity(entity);
    }

    @Override protected void addAdditionalSaveData(CompoundTag tag) { super.addAdditionalSaveData(tag); }
    @Override protected void readAdditionalSaveData(CompoundTag tag) { super.readAdditionalSaveData(tag); lifetime = 0; }
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket() { return NetworkHooks.getEntitySpawningPacket(this); }
}
