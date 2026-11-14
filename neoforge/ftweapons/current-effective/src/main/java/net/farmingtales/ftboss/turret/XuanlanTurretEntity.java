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
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class XuanlanTurretEntity extends Mob {
    private static final EntityDataAccessor<Integer> AGE = SynchedEntityData.defineId(XuanlanTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FIRED = SynchedEntityData.defineId(XuanlanTurretEntity.class, EntityDataSerializers.INT);
    private UUID owner;
    private int age;

    public XuanlanTurretEntity(EntityType<? extends XuanlanTurretEntity> var1, Level var2) {
        super(var1, var2);
        this.setNoAi(true);
        this.setHealth(150.0F);
        this.setCustomName(Component.literal("玄蓝月轮炮台"));
    }

    public static Builder attributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 150.0)
            .add(Attributes.ARMOR, 50.0)
            .add(Attributes.MOVEMENT_SPEED, 0.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    public int getArmorValue() {
        return 50;
    }

    public UUID ownerId() {
        return this.owner;
    }

    public void setOwner(EasterBossEntity var1) {
        this.owner = var1.getUUID();
    }

    public int age() {
        return this.level().isClientSide ? this.entityData.get(AGE) : this.age;
    }

    public float recoil(float var1) {
        return Math.max(0.0F, 1.0F - ((float)this.age() + var1 - (float)this.entityData.get(FIRED).intValue()) / 5.0F);
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(AGE, 0);
        builder.define(FIRED, -100);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel var1) {
            if (this.owner != null
                && var1.getEntity(this.owner) instanceof EasterBossEntity var14
                && var14.isAlive()
                && !var14.isRemoved()
                && var14.getVariant() == 6
                && ManorBalanceHooks.mainBoss(var14)
                && ZhuniTurretDrops.inEncounter(var14, this)
                && ++this.age < 600) {
                this.entityData.set(AGE, this.age);
                if (this.isAlive()
                    && !var14.isNoAi()
                    && !var14.isInvulnerable()
                    && !var14.isInvisible()
                    && !var14.isManorIntroHidden()
                    && ManorExpansionRules.turretFires(this.age)) {
                    Vec3 var15 = this.position().add(0.0, 1.35, 0.0);
                    ServerPlayer var4 = ManorBalanceHooks.players(var14, var1)
                        .stream()
                        .filter(var3 -> this.distanceToSqr(var3) <= 1024.0 && ManorExpansionHooks.clear(var1, var15, var3.getBoundingBox().getCenter(), this))
                        .min(Comparator.comparingDouble(this::distanceToSqr))
                        .orElse(null);
                    if (var4 != null) {
                        Vec3 var5 = var4.getBoundingBox().getCenter();
                        Vec3 var6 = var5.subtract(var15);
                        this.setYRot((float)Math.toDegrees(Math.atan2(-var6.x, var6.z)));
                        this.entityData.set(FIRED, this.age);
                        Vec3 var7 = var6.normalize();

                        for (ServerPlayer var9 : ManorBalanceHooks.players(var14, var1)) {
                            Vec3 var10 = var9.getBoundingBox().getCenter().subtract(var15);
                            double var11 = var10.dot(var7);
                            if (!(var11 < 0.0)
                                && !(var11 > var6.length() + 0.01)
                                && !(var10.subtract(var7.scale(var11)).length() > 2.6 + (double)var9.getBbWidth() * 0.5)
                                && ManorExpansionHooks.clear(var1, var15, var9.getBoundingBox().getCenter(), this)) {
                                float var13 = var14.manorSkillDamage(var9, 251.4F, 2.0) / 3.0F;
                                var9.invulnerableTime = 0;
                                var9.hurt(var1.damageSources().mobAttack(var14), var13);
                            }
                        }

                        for (double var16 = 0.0; var16 <= var6.length(); var16 += 0.25) {
                            Vec3 var19 = var15.add(var6.normalize().scale(var16));
                            var1.sendParticles(ParticleTypes.END_ROD, var19.x, var19.y, var19.z, 1, 0.0, 0.0, 0.0, 0.0);
                            var1.sendParticles(ParticleTypes.ELECTRIC_SPARK, var19.x, var19.y, var19.z, 1, 0.0, 0.0, 0.0, 0.0);
                        }

                        var1.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, var5.x, var5.y, var5.z, 8, 0.2, 0.2, 0.2, 0.03);
                        var1.playSound(null, this.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 0.7F, 1.5F);

                        for (int var17 = 0; var17 < 3; var17++) {
                            TurretFlywheelEntity var18 = new TurretFlywheelEntity(XuanlanTurrets.FLYWHEEL, var1);
                            var18.launch(var14, var4, var15, var17);
                            ManorFightMembers.inherit(var14, var18);
                            var1.addFreshEntity(var18);
                        }
                    }
                }
            } else {
                this.discard();
            }
        }
    }

    @Override
    public boolean hurt(DamageSource var1, float var2) {
        if (this.level() instanceof ServerLevel var3
            && Float.isFinite(var2)
            && !(var2 <= 0.0F)
            && this.owner != null
            && var3.getEntity(this.owner) instanceof EasterBossEntity var4
            && var1.getEntity() instanceof ServerPlayer var5
            && ManorBalanceHooks.players(var4, var3).contains(var5)) {
            return super.hurt(var1, var2);
        }

        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag var1) {
        super.addAdditionalSaveData(var1);
        if (this.owner != null) {
            var1.putUUID("Owner", this.owner);
        }

        var1.putInt("Age", this.age);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag var1) {
        super.readAdditionalSaveData(var1);
        this.owner = var1.hasUUID("Owner") ? var1.getUUID("Owner") : null;
        this.age = Math.max(0, var1.getInt("Age"));
    }
}
