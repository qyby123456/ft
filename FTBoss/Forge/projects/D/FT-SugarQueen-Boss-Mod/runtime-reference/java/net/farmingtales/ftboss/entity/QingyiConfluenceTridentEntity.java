/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientGamePacketListener
 *  net.minecraft.network.syncher.EntityDataAccessor
 *  net.minecraft.network.syncher.EntityDataSerializer
 *  net.minecraft.network.syncher.EntityDataSerializers
 *  net.minecraft.network.syncher.SynchedEntityData
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.projectile.ThrownTrident
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraftforge.network.NetworkHooks
 *  software.bernie.geckolib.core.animatable.GeoAnimatable
 *  software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache
 *  software.bernie.geckolib.core.animation.AnimatableManager$ControllerRegistrar
 *  software.bernie.geckolib.core.animation.AnimationController
 *  software.bernie.geckolib.core.animation.RawAnimation
 *  software.bernie.geckolib.util.GeckoLibUtil
 */
package net.farmingtales.ftboss.entity;

import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.skill.QingyiConfluenceSkill;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.network.NetworkHooks;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public final class QingyiConfluenceTridentEntity
extends ThrownTrident
implements GeoAnimatable {
    private static final EntityDataAccessor<Boolean> FOIL = SynchedEntityData.m_135353_(QingyiConfluenceTridentEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135035_);
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.qingyi_confluence.idle");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);

    public QingyiConfluenceTridentEntity(EntityType<? extends QingyiConfluenceTridentEntity> type, Level level) {
        super(type, level);
    }

    public QingyiConfluenceTridentEntity(Level level, LivingEntity owner, ItemStack stack) {
        this((EntityType<? extends QingyiConfluenceTridentEntity>)((EntityType)FTEntities.QINGYI_CONFLUENCE_TRIDENT.get()), level);
        CompoundTag data = new CompoundTag();
        super.m_7380_(data);
        data.m_128365_("Trident", (Tag)stack.m_255036_(1).m_41739_(new CompoundTag()));
        this.m_7378_(data);
        this.m_5602_((Entity)owner);
        this.m_6034_(owner.m_20185_(), owner.m_20188_() - 0.1, owner.m_20189_());
    }

    protected void m_8097_() {
        super.m_8097_();
        this.f_19804_.m_135372_(FOIL, (Object)false);
    }

    public void m_7378_(CompoundTag data) {
        super.m_7378_(data);
        this.f_19804_.m_135381_(FOIL, (Object)this.m_7941_().m_41790_());
    }

    public boolean m_37593_() {
        return (Boolean)this.f_19804_.m_135370_(FOIL);
    }

    protected boolean m_5603_(Entity candidate) {
        Entity entity = this.m_19749_();
        if (entity instanceof LivingEntity) {
            LivingEntity victim;
            LivingEntity owner = (LivingEntity)entity;
            if (candidate instanceof LivingEntity && !QingyiConfluenceSkill.canTarget(owner, victim = (LivingEntity)candidate)) {
                return false;
            }
        }
        return super.m_5603_(candidate);
    }

    protected void m_5790_(EntityHitResult hit) {
        LivingEntity owner;
        Entity entity;
        LivingEntity living;
        Entity entity2 = hit.m_82443_();
        LivingEntity victim = entity2 instanceof LivingEntity ? (living = (LivingEntity)entity2) : null;
        float before = victim == null ? 0.0f : victim.m_21223_() + victim.m_6103_();
        super.m_5790_(hit);
        if (!this.m_9236_().f_46443_ && victim != null && (entity = this.m_19749_()) instanceof LivingEntity && QingyiConfluenceSkill.canTarget(owner = (LivingEntity)entity, victim) && victim.m_21223_() + victim.m_6103_() < before) {
            QingyiConfluenceSkill.mark(victim, 40);
        }
    }

    public Packet<ClientGamePacketListener> m_5654_() {
        return NetworkHooks.getEntitySpawningPacket((Entity)this);
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, "flight", 0, state -> state.setAndContinue(IDLE))});
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public double getTick(Object entity) {
        return this.f_19797_;
    }
}

