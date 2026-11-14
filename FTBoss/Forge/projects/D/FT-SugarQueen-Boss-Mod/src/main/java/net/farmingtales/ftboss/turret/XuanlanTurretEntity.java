package net.farmingtales.ftboss.turret;

import java.util.Comparator;
import java.util.UUID;
import net.farmingtales.ftboss.balance.ManorBalanceHooks;
import net.farmingtales.ftboss.balance.ManorExpansionHooks;
import net.farmingtales.ftboss.balance.ManorExpansionRules;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** 竹鸟投放的月轮炮台：750 生命、50 护甲，玄蓝三分之一光束与三枚一次性飞轮。 */
public final class XuanlanTurretEntity extends Mob {
    private static final float MAX_HEALTH = 750.0F;
    private static final EntityDataAccessor<Integer> AGE = SynchedEntityData.defineId(XuanlanTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FIRED = SynchedEntityData.defineId(XuanlanTurretEntity.class, EntityDataSerializers.INT);
    private UUID owner;
    private int age;

    public XuanlanTurretEntity(EntityType<? extends XuanlanTurretEntity> type, Level level) {
        super(type, level);
        setNoAi(true);
        setHealth(MAX_HEALTH);
        setCustomName(Component.literal("玄蓝月轮炮台"));
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, MAX_HEALTH).add(Attributes.ARMOR, 50)
                .add(Attributes.MOVEMENT_SPEED, 0).add(Attributes.KNOCKBACK_RESISTANCE, 1);
    }

    @Override
    public int getArmorValue() { return 50; }

    public UUID ownerId() { return owner; }
    public void setOwner(EasterBossEntity boss) { owner = boss.getUUID(); }
    public int age() { return level().isClientSide ? entityData.get(AGE) : age; }
    public float recoil(float partialTick) {
        return Math.max(0, 1.0F - (age() + partialTick - entityData.get(FIRED)) / 5.0F);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(AGE, 0);
        entityData.define(FIRED, -100);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel level)) return;
        if (owner == null || !(level.getEntity(owner) instanceof EasterBossEntity boss) || !boss.isAlive()
                || boss.isRemoved() || boss.getVariant() != 6 || !ManorBalanceHooks.mainBoss(boss)
                || !ZhuniTurretDrops.inEncounter(boss, this) || ++age >= ManorExpansionRules.TURRET_LIFETIME) {
            discard();
            return;
        }
        entityData.set(AGE, age);
        if (!isAlive() || net.farmingtales.ftboss.boss.ai.ZhuniPairCombat.turretNoAi(boss)
                || net.farmingtales.ftboss.boss.ai.ZhuniPairCombat.turretInvulnerable(boss)
                || net.farmingtales.ftboss.boss.ai.ZhuniPairCombat.turretInvisible(boss)
                || net.farmingtales.ftboss.boss.ai.ZhuniPairCombat.turretIntroHidden(boss)
                || !ManorExpansionRules.turretFires(age)) return;
        Vec3 muzzle = position().add(0, 1.35, 0);
        ServerPlayer target = ManorBalanceHooks.players(boss, level).stream()
                .filter(player -> distanceToSqr(player) <= ManorExpansionRules.TURRET_RANGE * ManorExpansionRules.TURRET_RANGE
                        && ManorExpansionHooks.clear(level, muzzle, player.getBoundingBox().getCenter(), this))
                .min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
        if (target == null) return;
        Vec3 end = target.getBoundingBox().getCenter();
        Vec3 delta = end.subtract(muzzle);
        setYRot((float) Math.toDegrees(Math.atan2(-delta.x, delta.z)));
        entityData.set(FIRED, age);
        Vec3 direction = delta.normalize();
        for (ServerPlayer player : ManorBalanceHooks.players(boss, level)) {
            Vec3 relative = player.getBoundingBox().getCenter().subtract(muzzle);
            double along = relative.dot(direction);
            if (along < 0 || along > delta.length() + 0.01
                    || relative.subtract(direction.scale(along)).length() > EasterBossEntity.BEAM_STRIKE_RADIUS + player.getBbWidth() * 0.5
                    || !ManorExpansionHooks.clear(level, muzzle, player.getBoundingBox().getCenter(), this)) continue;
            float damage = boss.manorSkillDamage(player,
                    251.4F, 2.0) / 3.0F;
            player.invulnerableTime = 0;
            player.hurt(level.damageSources().mobAttack(boss), damage);
        }
        for (double distance = 0; distance <= delta.length(); distance += 0.25) {
            Vec3 sample = muzzle.add(delta.normalize().scale(distance));
            level.sendParticles(ParticleTypes.END_ROD, sample.x, sample.y, sample.z, 1, 0, 0, 0, 0);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, sample.x, sample.y, sample.z, 1, 0, 0, 0, 0);
        }
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, end.x, end.y, end.z, 8, 0.2, 0.2, 0.2, 0.03);
        level.playSound(null, blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 0.7F, 1.5F);
        for (int index = 0; index < 3; index++) {
            TurretFlywheelEntity wheel = new TurretFlywheelEntity(XuanlanTurrets.FLYWHEEL, level);
            wheel.launch(boss, target, muzzle, index);
            ManorFightMembers.inherit(boss, wheel);
            level.addFreshEntity(wheel);
        }
    }

    @Override
    public boolean hurt(DamageSource damage, float amount) {
        if (!(level() instanceof ServerLevel level) || !Float.isFinite(amount) || amount <= 0 || owner == null
                || !(level.getEntity(owner) instanceof EasterBossEntity boss)
                || !(damage.getEntity() instanceof ServerPlayer player)
                || !ManorBalanceHooks.players(boss, level).contains(player)) return false;
        return super.hurt(damage, amount);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag data) {
        super.addAdditionalSaveData(data);
        if (owner != null) data.putUUID("Owner", owner);
        data.putInt("Age", age);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag data) {
        super.readAdditionalSaveData(data);
        owner = data.hasUUID("Owner") ? data.getUUID("Owner") : null;
        age = Math.max(0, data.getInt("Age"));
    }
}
