/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
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

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ai.ExtraDebuffs;
import net.farmingtales.ftboss.boss.ai.HealBan;
import net.farmingtales.ftboss.boss.ai.ManorLivingTargets;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.util.Fx;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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

public class XueliKnifeEntity
extends ThrowableItemProjectile {
    public static final float KNIFE_DAMAGE = 12.0f;
    public static final int BLEED_TICKS = 160;
    public static final double NEAR_RADIUS = 1.6;
    private static final int MAX_LIFE = 120;

    public XueliKnifeEntity(EntityType entityType, Level level) {
        super(entityType, level);
    }

    public XueliKnifeEntity(Level level, LivingEntity livingEntity) {
        super((EntityType)FTEntities.XUELI_KNIFE.get(), livingEntity, level);
        this.m_37446_(new ItemStack((ItemLike)this.m_7881_()));
    }

    protected Item m_7881_() {
        try {
            for (String string : new String[]{"farmersdelight:diamond_knife", "farmersdelight:iron_knife", "farmersdelight:flint_knife", "farmersdelight:netherite_knife"}) {
                Item item = (Item)ForgeRegistries.ITEMS.getValue(new ResourceLocation(string));
                if (item == null || item == Items.f_41852_) continue;
                return item;
            }
        }
        catch (Throwable throwable) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] \u53d6\u98de\u5200\u7269\u54c1\u5931\u8d25: {}", (Object)throwable.toString());
        }
        return Items.f_42383_;
    }

    public void m_8119_() {
        Vec3 vec3 = this.m_20182_();
        super.m_8119_();
        if (this.m_9236_().f_46443_ || this.m_213877_()) {
            return;
        }
        this.trail(vec3, this.m_20182_());
        if (this.f_19797_ > 120 || this.m_20069_()) {
            this.strike(null);
        }
    }

    protected boolean m_5603_(Entity entity) {
        LivingEntity livingEntity;
        if (!ManorLivingTargets.canHit((Entity)this, entity)) {
            return false;
        }
        return entity instanceof LivingEntity && this.combatPlayer(livingEntity = (LivingEntity)entity) && super.m_5603_(entity);
    }

    private boolean combatPlayer(LivingEntity livingEntity) {
        if (!ManorLivingTargets.valid((Entity)this, livingEntity)) {
            return false;
        }
        return livingEntity.m_6084_() && !ManorLivingTargets.creative(livingEntity) && !livingEntity.m_5833_();
    }

    private void trail(Vec3 vec3, Vec3 vec32) {
        Level level = this.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        int n = Math.min(8, (int)Math.ceil(vec3.m_82554_(vec32) / 0.35));
        for (int i = 1; i <= n; ++i) {
            Vec3 vec33 = vec3.m_165921_(vec32, (double)i / (double)n);
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123797_, vec33.f_82479_, vec33.f_82480_, vec33.f_82481_, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    protected void m_5790_(EntityHitResult entityHitResult) {
        super.m_5790_(entityHitResult);
        if (this.m_9236_().f_46443_) {
            return;
        }
        this.strike(entityHitResult.m_82443_());
    }

    protected void m_6532_(HitResult hitResult) {
        if (this.m_9236_().f_46443_ || this.m_213877_()) {
            return;
        }
        this.trail(this.m_20182_(), hitResult.m_82450_());
        this.m_146884_(hitResult.m_82450_());
        if (hitResult.m_6662_() == HitResult.Type.ENTITY) {
            this.m_5790_((EntityHitResult)hitResult);
        } else {
            this.strike(null);
        }
    }

    private void strike(Entity entity) {
        LivingEntity livingEntity;
        if (this.m_213877_()) {
            return;
        }
        Level level = this.m_9236_();
        if (!(level instanceof ServerLevel)) {
            this.m_146870_();
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        double d = this.m_20185_();
        double d2 = this.m_20186_();
        double d3 = this.m_20189_();
        int n = 0;
        if (entity instanceof LivingEntity && this.combatPlayer(livingEntity = (LivingEntity)entity)) {
            this.cut(livingEntity, false);
            ++n;
        } else if (entity == null) {
            for (LivingEntity livingEntity2 : ManorLivingTargets.nearby((Level)serverLevel, (Entity)this)) {
                if (!this.combatPlayer(livingEntity2) || livingEntity2.m_20275_(d, d2, d3) > 2.5600000000000005) continue;
                this.cut(livingEntity2, true);
                ++n;
            }
        }
        serverLevel.m_6263_(null, d, d2, d3, SoundEvents.f_12514_, SoundSource.HOSTILE, 0.9f, 1.7f);
        serverLevel.m_6263_(null, d, d2, d3, SoundEvents.f_11677_, SoundSource.HOSTILE, 0.7f, 1.4f);
        Fx.particles((Level)serverLevel, d, d2 + 0.2, d3, 8, (ParticleOptions)ParticleTypes.f_123797_, 0.6, 0.06);
        Fx.particles((Level)serverLevel, d, d2 + 0.2, d3, 6, (ParticleOptions)ParticleTypes.f_123808_, 0.5, 0.04);
        if (n > 0) {
            FTBossMod.LOGGER.info("[FTBoss][Easter] \u96ea\u9ece\u7981\u7597\u98de\u5200\u547d\u4e2d {} \u4eba ({} \u4f24\u5bb3 + \u7981\u7597 {} \u79d2)", new Object[]{n, String.format("%.1f", Float.valueOf(12.0f)), 30});
        }
        this.m_146870_();
    }

    private void cut(LivingEntity livingEntity, boolean bl) {
        livingEntity.f_19802_ = 0;
        livingEntity.m_6469_(this.m_269291_().m_269390_((Entity)this, this.m_19749_()), bl ? 7.2000003f : 12.0f);
        EasterBossEntity.applyXueliFrost(livingEntity);
        if (livingEntity instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)livingEntity;
            HealBan.ban(serverPlayer, "\u96ea\u9ece\u7684\u98de\u5200");
            ExtraDebuffs.bleed(livingEntity, 160, 1);
        }
    }

    protected float m_7139_() {
        return 0.03f;
    }
}

