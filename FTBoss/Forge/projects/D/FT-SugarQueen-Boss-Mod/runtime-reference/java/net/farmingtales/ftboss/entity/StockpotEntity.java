/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.ThrowableItemProjectile
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.entity;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.util.Fx;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.registries.ForgeRegistries;

public class StockpotEntity
extends ThrowableItemProjectile {
    public static final float POT_DAMAGE = 40.0f;
    public static final double SPLASH_RADIUS = 3.0;
    private static final int MAX_LIFE = 200;

    public StockpotEntity(EntityType<? extends StockpotEntity> type, Level level) {
        super(type, level);
    }

    public StockpotEntity(Level level, LivingEntity shooter) {
        super((EntityType)FTEntities.STOCKPOT.get(), shooter, level);
        this.m_37446_(new ItemStack((ItemLike)this.m_7881_()));
    }

    protected Item m_7881_() {
        try {
            Item pot = (Item)ForgeRegistries.ITEMS.getValue(new ResourceLocation("kaleidoscope_cookery:stockpot"));
            if (pot != null && pot != Items.f_41852_) {
                return pot;
            }
            Item alt = (Item)ForgeRegistries.ITEMS.getValue(new ResourceLocation("youkaisfeasts", "stockpot"));
            if (alt != null && alt != Items.f_41852_) {
                return alt;
            }
        }
        catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] \u53d6\u6c64\u9505\u7269\u54c1\u5931\u8d25: {}", (Object)t.toString());
        }
        return Items.f_42544_;
    }

    public void m_8119_() {
        super.m_8119_();
        if (this.m_9236_().f_46443_) {
            return;
        }
        if (this.f_19797_ > 200 || this.m_20069_()) {
            this.splash(null);
        }
    }

    protected void m_5790_(EntityHitResult hit) {
        super.m_5790_(hit);
        if (this.m_9236_().f_46443_) {
            return;
        }
        this.splash(hit.m_82443_());
    }

    protected void m_6532_(HitResult result) {
        if (this.m_9236_().f_46443_) {
            return;
        }
        if (result.m_6662_() == HitResult.Type.ENTITY) {
            this.m_5790_((EntityHitResult)result);
        } else {
            this.splash(null);
        }
    }

    private void splash(Entity direct) {
        Level level = this.m_9236_();
        if (!(level instanceof ServerLevel)) {
            this.m_146870_();
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        double x = this.m_20185_();
        double y = this.m_20186_();
        double z = this.m_20189_();
        float half = 20.0f;
        int hits = 0;
        for (Player p : sl.m_6907_()) {
            if (!p.m_6084_() || p.m_5833_() || p.m_20275_(x, y, z) > 9.0) continue;
            p.f_19802_ = 0;
            p.m_6469_(this.m_269291_().m_269390_((Entity)this, this.m_19749_()), half);
            p.f_19802_ = 0;
            p.m_6469_(this.m_269291_().m_269104_((Entity)this, this.m_19749_()), half);
            EasterBossEntity.applyXueliFrost((LivingEntity)p);
            ++hits;
        }
        sl.m_6263_(null, x, y, z, SoundEvents.f_11668_, SoundSource.HOSTILE, 1.2f, 0.6f);
        sl.m_6263_(null, x, y, z, SoundEvents.f_11677_, SoundSource.HOSTILE, 1.0f, 0.5f);
        Fx.particles((Level)sl, x, y + 0.2, z, 26, (ParticleOptions)ParticleTypes.f_123769_, 1.0, 0.12);
        Fx.particles((Level)sl, x, y + 0.2, z, 10, (ParticleOptions)ParticleTypes.f_123755_, 0.7, 0.05);
        FTBossMod.LOGGER.info("[FTBoss][Easter] \u96ea\u9ece\u6c64\u9505\u843d\u5730: \u547d\u4e2d {} \u4eba ({} \u4f24\u5bb3, \u534a\u5f84 {} \u683c)", new Object[]{hits, String.format("%.1f", Float.valueOf(40.0f)), 3});
        this.m_146870_();
    }

    protected float m_7139_() {
        return 0.05f;
    }
}

