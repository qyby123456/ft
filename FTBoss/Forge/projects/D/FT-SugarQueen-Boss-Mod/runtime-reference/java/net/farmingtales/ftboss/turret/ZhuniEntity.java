/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.MoverType
 *  net.minecraft.world.entity.ai.attributes.AttributeSupplier$Builder
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.turret;

import java.util.UUID;
import net.farmingtales.ftboss.balance.ManorBalanceHooks;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.ai.ZhuniBlackwallDisruption;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.turret.ZhuniOpeningMovement;
import net.farmingtales.ftboss.turret.ZhuniStage;
import net.farmingtales.ftboss.turret.ZhuniTurretDrops;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class ZhuniEntity
extends Mob {
    private UUID owner;
    private long nextDrop;
    private final ZhuniTurretDrops turretDrops = new ZhuniTurretDrops();

    public ZhuniEntity(EntityType<? extends ZhuniEntity> entityType, Level level) {
        super(entityType, level);
        this.m_21557_(true);
        this.m_20242_(true);
        this.m_6593_((Component)Component.m_237113_((String)"\u7af9\u9e1f"));
        this.m_20340_(true);
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.m_21552_().m_22268_(Attributes.f_22276_, 1.0).m_22268_(Attributes.f_22279_, 0.0).m_22268_(Attributes.f_22278_, 1.0);
    }

    public UUID ownerId() {
        return this.owner;
    }

    public void setOwner(EasterBossEntity easterBossEntity) {
        this.owner = easterBossEntity.m_20148_();
    }

    public void revealedAt(long l) {
        this.turretDrops.revealedAt(l);
    }

    public void m_8119_() {
        Entity entity;
        super.m_8119_();
        Object object = this.m_9236_();
        if (!(object instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)object;
        if (!(this.owner != null && (entity = serverLevel.m_8791_(this.owner)) instanceof EasterBossEntity && (object = (EasterBossEntity)entity).m_6084_() && !object.m_213877_() && ((EasterBossEntity)object).getVariant() == 6 && ManorBalanceHooks.mainBoss((EasterBossEntity)object) && ManorFightMembers.fightId((Entity)object) != null && ManorFight.hasActive(ManorFightMembers.fightId((Entity)object)))) {
            this.m_146870_();
            return;
        }
        this.m_21153_(1.0f);
        ZhuniStage.syncBossBar((EasterBossEntity)object);
        boolean bl = ZhuniStage.waiting((EasterBossEntity)object);
        double d = (double)serverLevel.m_46467_() * 0.04;
        Vec3 vec3 = bl ? ZhuniOpeningMovement.preferred((EasterBossEntity)object) : object.m_20182_().m_82520_(Math.cos(d) * 1.8, 2.8, Math.sin(d) * 1.8);
        Vec3 vec32 = vec3.m_82546_(this.m_20182_()).m_82490_(0.2);
        this.m_20256_(Vec3.f_82478_);
        this.m_6478_(MoverType.SELF, vec32);
        this.m_146922_((float)Math.toDegrees(Math.atan2(-vec32.f_82479_, vec32.f_82481_)));
        if (bl || ((EasterBossEntity)object).isManorIntroHidden()) {
            this.revealedAt(serverLevel.m_46467_());
            return;
        }
        this.turretDrops.tick((EasterBossEntity)object, serverLevel);
        ZhuniBlackwallDisruption.tick(this, (EasterBossEntity)object, serverLevel);
    }

    public boolean m_6469_(DamageSource damageSource, float f) {
        EasterBossEntity easterBossEntity;
        block6: {
            block5: {
                Level level = this.m_9236_();
                if (!(level instanceof ServerLevel)) break block5;
                ServerLevel serverLevel = (ServerLevel)level;
                if (!Float.isFinite(f) || f <= 0.0f || this.owner == null || !((level = serverLevel.m_8791_(this.owner)) instanceof EasterBossEntity)) break block5;
                easterBossEntity = (EasterBossEntity)level;
                level = damageSource.m_7639_();
                if (!(level instanceof ServerPlayer)) break block5;
                ServerPlayer serverPlayer = (ServerPlayer)level;
                if (ManorBalanceHooks.players(easterBossEntity, serverLevel).contains(serverPlayer)) break block6;
            }
            return false;
        }
        boolean bl = ZhuniStage.waiting(easterBossEntity);
        ZhuniStage.reveal(easterBossEntity, this);
        this.m_21153_(1.0f);
        boolean bl2 = easterBossEntity.m_6469_(damageSource, f);
        if (easterBossEntity.m_6084_()) {
            ZhuniStage.syncBossBar(easterBossEntity);
        } else {
            this.m_146870_();
        }
        return bl2 || bl;
    }

    public void m_7380_(CompoundTag compoundTag) {
        super.m_7380_(compoundTag);
        if (this.owner != null) {
            compoundTag.m_128362_("Owner", this.owner);
        }
    }

    public void m_7378_(CompoundTag compoundTag) {
        super.m_7378_(compoundTag);
        this.owner = compoundTag.m_128403_("Owner") ? compoundTag.m_128342_("Owner") : null;
        this.revealedAt(this.m_9236_().m_46467_());
    }
}

