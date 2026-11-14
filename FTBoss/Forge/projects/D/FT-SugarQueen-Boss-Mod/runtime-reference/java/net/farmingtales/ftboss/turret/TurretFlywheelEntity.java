/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientGamePacketListener
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.network.NetworkHooks
 */
package net.farmingtales.ftboss.turret;

import java.util.UUID;
import net.farmingtales.ftboss.balance.ManorBalanceHooks;
import net.farmingtales.ftboss.boss.ai.ManorAdaptiveDamage;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public final class TurretFlywheelEntity
extends Entity {
    private UUID owner;
    private UUID target;
    private int age;

    public TurretFlywheelEntity(EntityType<? extends TurretFlywheelEntity> type, Level level) {
        super(type, level);
        this.m_20242_(true);
    }

    public void launch(EasterBossEntity boss, ServerPlayer player, Vec3 origin, int index) {
        this.owner = boss.m_20148_();
        this.target = player.m_20148_();
        this.m_6034_(origin.f_82479_, origin.f_82480_, origin.f_82481_);
        Vec3 direction = player.m_20191_().m_82399_().m_82546_(origin).m_82541_().m_82524_((float)(index - 1) * 0.45f).m_82520_(0.0, index == 1 ? 0.2 : 0.0, 0.0).m_82541_();
        this.m_20256_(direction.m_82490_(0.8));
    }

    protected void m_8097_() {
    }

    public void m_8119_() {
        Vec3 to;
        BlockHitResult block;
        ServerPlayer player;
        EasterBossEntity boss;
        ServerLevel level;
        block10: {
            block9: {
                Entity entity;
                super.m_8119_();
                Level level2 = this.m_9236_();
                if (!(level2 instanceof ServerLevel)) {
                    this.m_146884_(this.m_20182_().m_82549_(this.m_20184_()));
                    return;
                }
                level = (ServerLevel)level2;
                if (++this.age > 60 || this.owner == null || !((entity = level.m_8791_(this.owner)) instanceof EasterBossEntity) || !(boss = (EasterBossEntity)entity).m_6084_() || boss.m_213877_() || boss.getVariant() != 6 || this.target == null || !((entity = level.m_8791_(this.target)) instanceof ServerPlayer)) break block9;
                player = (ServerPlayer)entity;
                if (ManorBalanceHooks.players(boss, level).contains(player)) break block10;
            }
            this.m_146870_();
            return;
        }
        Vec3 from = this.m_20182_();
        Vec3 goal = player.m_20191_().m_82399_();
        Vec3 velocity = this.m_20184_();
        if (this.age > 4) {
            velocity = velocity.m_82490_(0.78).m_82549_(goal.m_82546_(from).m_82541_().m_82490_(0.22)).m_82541_().m_82490_(0.8);
        }
        if ((block = level.m_45547_(new ClipContext(from, to = from.m_82549_(velocity), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)this))).m_6662_() != HitResult.Type.MISS) {
            to = block.m_82450_();
        }
        if (player.m_20191_().m_82400_(0.2).m_82390_(from) || player.m_20191_().m_82400_(0.2).m_82371_(from, to).isPresent()) {
            float base = 10.67f * (float)boss.flywheelDamageMul() / 3.0f;
            if (player.m_21223_() > 100.0f) {
                base *= 1.25f;
            }
            player.f_19802_ = 0;
            player.m_6469_(level.m_269111_().m_269390_((Entity)this, (Entity)boss), base * 0.5f);
            player.f_19802_ = 0;
            player.m_6469_(level.m_269111_().m_269104_((Entity)this, (Entity)boss), base * 0.5f);
            player.f_19802_ = 0;
            player.m_6469_(level.m_269111_().m_269104_((Entity)this, (Entity)boss), 0.44333336f);
            player.f_19802_ = 0;
            ManorAdaptiveDamage.hurtSkill((LivingEntity)player, (Entity)boss, level.m_269111_().m_269341_(), 0.33333334f);
            player.f_19802_ = 0;
            ManorAdaptiveDamage.hurtSkill((LivingEntity)player, (Entity)boss, level.m_269111_().m_269549_(), 0.33333334f);
            this.m_146870_();
            return;
        }
        if (block.m_6662_() != HitResult.Type.MISS) {
            this.m_146870_();
            return;
        }
        this.m_20256_(velocity);
        this.m_146884_(to);
        this.m_146922_((float)Math.toDegrees(Math.atan2(-velocity.f_82479_, velocity.f_82481_)));
    }

    protected void m_7380_(CompoundTag data) {
    }

    protected void m_7378_(CompoundTag data) {
    }

    public Packet<ClientGamePacketListener> m_5654_() {
        return NetworkHooks.getEntitySpawningPacket((Entity)this);
    }
}

