/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.projectile.ThrowableItemProjectile
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ai.BossTargets;
import net.farmingtales.ftboss.boss.ai.ManorAdaptiveDamage;
import net.farmingtales.ftboss.boss.ai.ManorLivingTargets;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.entity.XuanlanEliteFlywheels;
import net.farmingtales.ftboss.util.FlywheelCombatRules;
import net.farmingtales.ftboss.util.XuanlanFlywheelLifetime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

public class FlywheelProjectileEntity
extends ThrowableItemProjectile {
    public static final float DAMAGE = 10.67f;
    public static final float ARMOR_PIERCE_RATIO = 0.5f;
    public static final float HEALTH_BONUS_THRESHOLD = 100.0f;
    public static final float HEALTH_BONUS_MULT = 1.25f;
    public static final float EXTRA_MAGIC = 1.33f;
    public static final float EXTRA_VOID = 1.0f;
    public static final float EXTRA_FIRE = 1.0f;
    public static final int HOMING_TICKS = 40;
    public static final double HOMING_RANGE = 24.0;
    public static final int ORBIT_MAX_LIFE = 3600;
    public static final int HIT_CD_NEAR = 2;
    public static final int HIT_CD_FAR = 6;
    public static final double NEAR_DIST = 1.0;
    public static final double FAR_DIST = 6.0;
    public static final int HIT_COOLDOWN = 5;
    public static final int HIT_COOLDOWN_MAXED = 2;
    public static final int MAX_FLYWHEELS = 15;
    private static final Map<Level, Map<UUID, Long>> LAST_HIT = new WeakHashMap<Level, Map<UUID, Long>>();
    private double orbitAngle = 0.0;
    private double orbitRadius = 1.9;
    private double orbitSpeed = 0.06;
    private double orbitHeight = 1.0;
    private boolean orbiting = false;
    private int runTicks = 0;
    private int orbitTicks = 0;

    public FlywheelProjectileEntity(EntityType entityType, Level level) {
        super(entityType, level);
        this.m_20242_(true);
    }

    public FlywheelProjectileEntity(Level level, LivingEntity livingEntity) {
        super((EntityType)FTEntities.FLYWHEEL.get(), livingEntity, level);
        this.m_20242_(true);
        this.m_37446_(new ItemStack((ItemLike)this.m_7881_()));
        this.orbitAngle = this.f_19796_.m_188500_() * Math.PI * 2.0;
        this.orbitRadius = 1.7 + this.f_19796_.m_188500_() * 1.1;
        this.orbitSpeed = 0.045 + this.f_19796_.m_188500_() * 0.045;
        this.orbitHeight = 0.6 + this.f_19796_.m_188500_() * 1.0;
    }

    protected Item m_7881_() {
        try {
            Item item = (Item)ForgeRegistries.ITEMS.getValue(new ResourceLocation("ftboss", "wenxuanlan_flywheel"));
            if (item != null && item != Items.f_41852_) {
                return item;
            }
            Item item2 = (Item)ForgeRegistries.ITEMS.getValue(new ResourceLocation("create", "flywheel"));
            if (item2 != null && item2 != Items.f_41852_) {
                return item2;
            }
        }
        catch (Throwable throwable) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] \u53d6\u98de\u8f6e\u7269\u54c1\u5931\u8d25: {}", (Object)throwable.toString());
        }
        return Items.f_42416_;
    }

    public boolean isOrbiting() {
        return this.orbiting;
    }

    public void beginOrbit(double d) {
        Entity entity = this.m_19749_();
        if (entity == null) {
            return;
        }
        this.orbiting = true;
        this.orbitAngle = d;
        this.orbitTicks = 0;
        this.runTicks = 0;
        this.m_6034_(entity.m_20185_() + Math.cos(d) * XuanlanEliteFlywheels.widen((Entity)this, this.orbitRadius), entity.m_20186_() + this.orbitHeight, entity.m_20189_() + Math.sin(d) * XuanlanEliteFlywheels.widen((Entity)this, this.orbitRadius));
        this.m_20256_(Vec3.f_82478_);
    }

    public void launchAgain() {
        this.orbiting = false;
        this.runTicks = 0;
        this.orbitTicks = 0;
    }

    public void m_8119_() {
        if (!this.m_9236_().f_46443_) {
            ++this.runTicks;
            this.steer();
            if (this.m_213877_()) {
                return;
            }
        }
        super.m_8119_();
    }

    /*
     * Unable to fully structure code
     */
    private void steer() {
        block16: {
            var1_1 = this.m_19749_();
            this.runTicks = XuanlanFlywheelLifetime.activeAge(this.runTicks, var1_1);
            if (var1_1 == null || !var1_1.m_6084_() || var1_1.m_9236_() != this.m_9236_()) {
                if (var1_1 != null || this.runTicks > 40) {
                    this.poof();
                }
                return;
            }
            if (var1_1 instanceof EasterBossEntity && (var2_2 = (EasterBossEntity)var1_1).isXuanlanSkyCasting()) {
                --this.runTicks;
                this.orbiting = true;
                this.m_6842_(false);
                XuanlanFlywheelLifetime.skyPosition(this, var1_1.m_20185_(), var1_1.m_20186_() + this.orbitHeight, var1_1.m_20189_(), this.orbitAngle, this.orbitRadius);
                this.m_20256_(Vec3.f_82478_);
                return;
            }
            this.m_6842_(false);
            if (!this.orbiting) break block16;
            if (this.runTicks > 3600) {
                this.poof();
                return;
            }
            ++this.orbitTicks;
            var2_2 = this.nearestPlayer();
            v0 = var3_5 = var1_1 instanceof EasterBossEntity != false && (var4_3 = (EasterBossEntity)var1_1).isFlywheelMaxed() != false;
            if (!(var1_1 instanceof EasterBossEntity)) ** GOTO lbl27
            var5_8 = (EasterBossEntity)var1_1;
            if (!var5_8.m_21525_()) {
lbl27:
                // 2 sources

                v1 = true;
            } else {
                v1 = var4_4 = false;
            }
            if (var2_2 != null && FlywheelCombatRules.shouldRelaunch(this.orbitTicks, var1_1.m_20270_((Entity)var2_2), var3_5, var4_4)) {
                this.launchAgain();
            } else {
                this.orbitStep(var1_1);
                return;
            }
        }
        if (this.runTicks > 3600) {
            this.poof();
            return;
        }
        var2_2 = null;
        if (this.runTicks <= 40) {
            var3_6 = this.nearestPlayer();
            if (var3_6 != null) {
                var2_2 = var3_6.m_20182_().m_82520_(0.0, (double)var3_6.m_20206_() * 0.55, 0.0);
            }
        } else if (var1_1 != null) {
            if (this.m_20280_(var1_1) < 4.0) {
                this.orbiting = true;
                this.orbitTicks = 0;
                this.orbitAngle = Math.atan2(this.m_20189_() - var1_1.m_20189_(), this.m_20185_() - var1_1.m_20185_());
                this.orbitStep(var1_1);
                return;
            }
            var2_2 = var1_1.m_20182_().m_82520_(0.0, (double)var1_1.m_20206_() * 0.55, 0.0);
        }
        if (var2_2 == null) {
            return;
        }
        var3_7 = var2_2.m_82546_(this.m_20182_());
        if (var3_7.m_82556_() < 1.0E-4) {
            return;
        }
        var3_7 = var3_7.m_82541_();
        this.m_20256_(this.m_20184_().m_82490_(0.55).m_82549_(var3_7.m_82490_(0.62)));
        this.m_146922_((float)Math.toDegrees(Math.atan2(var3_7.f_82479_, var3_7.f_82481_)));
        this.f_19859_ = this.m_146908_();
    }

    private void orbitStep(Entity entity) {
        if (entity == null) {
            return;
        }
        this.orbitAngle += this.orbitSpeed;
        double d = entity.m_20185_() + Math.cos(this.orbitAngle) * XuanlanEliteFlywheels.widen((Entity)this, this.orbitRadius);
        double d2 = entity.m_20189_() + Math.sin(this.orbitAngle) * XuanlanEliteFlywheels.widen((Entity)this, this.orbitRadius);
        double d3 = entity.m_20186_() + this.orbitHeight;
        Vec3 vec3 = new Vec3(d - this.m_20185_(), d3 - this.m_20186_(), d2 - this.m_20189_());
        this.m_20256_(vec3.m_82490_(0.45));
        this.m_146922_((float)Math.toDegrees(Math.atan2(vec3.f_82479_, vec3.f_82481_)));
        this.f_19859_ = this.m_146908_();
    }

    private LivingEntity nearestPlayer() {
        return XuanlanEliteFlywheels.pick((Entity)this);
    }

    protected boolean m_5603_(Entity entity) {
        EasterBossEntity easterBossEntity;
        if (!ManorLivingTargets.canHit((Entity)this, entity)) {
            return false;
        }
        Entity entity2 = this.m_19749_();
        if (entity2 instanceof EasterBossEntity && (easterBossEntity = (EasterBossEntity)entity2).isXuanlanSkyCasting()) {
            return false;
        }
        return entity != this.m_19749_() && entity instanceof LivingEntity && BossTargets.valid((LivingEntity)(easterBossEntity = (LivingEntity)entity)) && super.m_5603_(entity);
    }

    protected void m_5790_(EntityHitResult entityHitResult) {
        EasterBossEntity easterBossEntity;
        double d;
        boolean bl;
        Entity entity;
        EasterBossEntity easterBossEntity2;
        Entity entity2 = this.m_19749_();
        if (entity2 instanceof EasterBossEntity && (easterBossEntity2 = (EasterBossEntity)entity2).isXuanlanSkyCasting()) {
            return;
        }
        super.m_5790_(entityHitResult);
        if (this.m_9236_().f_46443_) {
            return;
        }
        easterBossEntity2 = entityHitResult.m_82443_();
        if (easterBossEntity2 == this.m_19749_()) {
            return;
        }
        if (!(easterBossEntity2 instanceof LivingEntity)) {
            return;
        }
        entity2 = (LivingEntity)easterBossEntity2;
        if (!BossTargets.valid((LivingEntity)entity2)) {
            return;
        }
        Level level2 = this.m_9236_();
        if (level2 instanceof ServerLevel) {
            Long l;
            Object object;
            EasterBossEntity easterBossEntity3;
            ServerLevel serverLevel = (ServerLevel)level2;
            long l2 = serverLevel.m_46467_();
            entity = this.m_19749_();
            if (entity == null || !entity.m_6084_() || entity.m_9236_() != serverLevel) {
                return;
            }
            boolean bl2 = entity instanceof EasterBossEntity && (easterBossEntity3 = (EasterBossEntity)entity).isFlywheelMaxed();
            int n = FlywheelCombatRules.hitCooldown(entity.m_20270_(entity2), bl2);
            if (entity instanceof EasterBossEntity) {
                object = (EasterBossEntity)entity;
                n = ((EasterBossEntity)object).getChallengeDifficulty().acceleratedTicks(n);
            }
            if (!FlywheelCombatRules.hitReady(l2, l = (Long)(object = LAST_HIT.computeIfAbsent((Level)serverLevel, level -> new HashMap())).get(entity2.m_20148_()), n)) {
                return;
            }
            object.put(entity2.m_20148_(), l2);
            if (object.size() > 64) {
                object.entrySet().removeIf(entry -> l2 - (Long)entry.getValue() > 400L);
            }
        }
        float f = 10.67f;
        boolean bl3 = bl = entity2.m_21223_() > 100.0f;
        if (bl) {
            f *= 1.25f;
        }
        if ((entity = this.m_19749_()) instanceof EasterBossEntity && (d = (easterBossEntity = (EasterBossEntity)entity).flywheelDamageMul()) > 1.0) {
            f *= (float)d;
        }
        float f2 = f * 0.5f;
        float f3 = f * 0.5f;
        entity2.f_19802_ = 0;
        entity2.m_6469_(this.m_269291_().m_269390_((Entity)this, this.m_19749_()), f2);
        entity2.f_19802_ = 0;
        entity2.m_6469_(this.m_269291_().m_269104_((Entity)this, this.m_19749_()), f3);
        entity2.f_19802_ = 0;
        entity2.m_6469_(this.m_269291_().m_269104_((Entity)this, this.m_19749_()), 1.33f);
        entity2.f_19802_ = 0;
        ManorAdaptiveDamage.hurtSkill((LivingEntity)entity2, this.m_19749_(), this.m_269291_().m_269341_(), 1.0f);
        entity2.f_19802_ = 0;
        ManorAdaptiveDamage.hurtSkill((LivingEntity)entity2, this.m_19749_(), this.m_269291_().m_269549_(), 1.0f);
        entity2.m_20254_(2);
        entity2.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 100, 1));
        entity2.m_7292_(new MobEffectInstance(MobEffects.f_19613_, 100, 1));
        if (!this.orbiting) {
            this.runTicks = Math.max(this.runTicks, 41);
        }
    }

    protected void m_6532_(HitResult hitResult) {
        if (this.m_9236_().f_46443_) {
            return;
        }
        if (hitResult.m_6662_() == HitResult.Type.ENTITY) {
            this.m_5790_((EntityHitResult)hitResult);
        }
    }

    private void poof() {
        this.m_146870_();
    }
}

