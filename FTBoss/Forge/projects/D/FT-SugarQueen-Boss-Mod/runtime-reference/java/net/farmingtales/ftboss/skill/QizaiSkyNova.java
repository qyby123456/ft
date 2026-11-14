/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.skill;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.balance.ManorExpansionHooks;
import net.farmingtales.ftboss.boss.ai.ManorLivingTargets;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.QizaiRules;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class QizaiSkyNova {
    private static final int IDLE = 0;
    private static final int RISE = 1;
    private static final int CHARGE = 2;
    private static final int IMPACT = 3;
    private static final int RECOVER = 4;
    private final EasterBossEntity boss;
    private int phase = 0;
    private int ticks;
    private int impacts;
    private int repeatTicks;
    private int riseTicks = 20;
    private int chargeTicks = 100;
    private Vec3 origin = Vec3.f_82478_;
    private Vec3 apex = Vec3.f_82478_;
    private long castStarted;

    public QizaiSkyNova(EasterBossEntity easterBossEntity) {
        this.boss = easterBossEntity;
    }

    public boolean busy() {
        return this.phase != 0;
    }

    public boolean begin(ServerLevel serverLevel, LivingEntity livingEntity) {
        if (this.busy() || serverLevel != this.boss.m_9236_()) {
            return false;
        }
        if (this.boss.getVariant() != 11 || !this.boss.m_6084_() || this.boss.isMini() || this.boss.isGiant()) {
            return false;
        }
        this.origin = this.boss.m_20182_();
        this.castStarted = serverLevel.m_46467_();
        this.apex = this.origin.m_82520_(0.0, 24.0, 0.0);
        this.phase = 1;
        this.riseTicks = Math.max(1, this.boss.getChallengeDifficulty().acceleratedTicks(20));
        this.chargeTicks = Math.max(1, this.boss.getChallengeDifficulty().acceleratedTicks(100));
        this.ticks = this.riseTicks;
        this.impacts = 0;
        this.repeatTicks = 0;
        this.boss.m_20331_(true);
        this.boss.m_6710_(null);
        this.boss.m_21573_().m_26573_();
        this.boss.playCast(this.riseTicks + this.chargeTicks, 0);
        this.cue(serverLevel, false);
        serverLevel.m_6263_(null, this.origin.f_82479_, this.origin.f_82480_, this.origin.f_82481_, SoundEvents.f_11736_, SoundSource.HOSTILE, 1.6f, 0.6f);
        serverLevel.m_6263_(null, this.origin.f_82479_, this.origin.f_82480_, this.origin.f_82481_, SoundEvents.f_11893_, SoundSource.HOSTILE, 1.4f, 0.7f);
        this.announce(serverLevel, "\u00a75\u3010\u5929\u9668\u661f\u88c1\u3011\u00a77\u7941\u5d3d\u5347\u4e0a\u9ad8\u7a7a \u2014\u2014 \u00a7c\u5feb\u6563\u5f00\uff0c\u5b83\u8981\u6765\u4e86\uff01");
        FTBossMod.LOGGER.info("[FTBoss][Qizai] \u300c\u5929\u9668\u661f\u88c1\u300d\u8d77\u624b: \u60ac\u505c\u4e8e {} \u683c\u9ad8, \u5347\u7a7a {} tick, \u84c4\u529b {} tick", new Object[]{String.format("%.1f", 24.0), this.riseTicks, this.chargeTicks});
        return true;
    }

    public boolean tick(ServerLevel serverLevel) {
        ManorExpansionHooks.nova(this.boss, serverLevel, this.origin, this.phase);
        if (this.phase == 0) {
            return false;
        }
        if (!this.boss.m_6084_() || this.boss.m_213877_()) {
            this.cancel();
            return false;
        }
        this.boss.m_20331_(true);
        switch (this.phase) {
            case 1: {
                double d = 1.0 - (double)this.ticks / (double)Math.max(1, this.riseTicks);
                Vec3 vec3 = this.origin.m_82520_(0.0, 24.0 * d, 0.0);
                this.boss.m_7678_(vec3.f_82479_, vec3.f_82480_, vec3.f_82481_, this.boss.m_146908_(), this.boss.m_146909_());
                this.boss.m_20256_(Vec3.f_82478_);
                this.boss.f_19864_ = true;
                serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123810_, this.boss.m_20185_(), this.boss.m_20186_(), this.boss.m_20189_(), 6, 0.5, 0.5, 0.5, 0.02);
                this.boss.m_6710_(null);
                if (--this.ticks > 0) break;
                this.phase = 2;
                this.ticks = this.chargeTicks;
                serverLevel.m_6263_(null, this.boss.m_20185_(), this.boss.m_20186_(), this.boss.m_20189_(), SoundEvents.f_12376_, SoundSource.HOSTILE, 1.8f, 0.5f);
                break;
            }
            case 2: {
                this.boss.m_7678_(this.apex.f_82479_, this.apex.f_82480_, this.apex.f_82481_, this.boss.m_146908_() + 6.0f, this.boss.m_146909_());
                this.boss.m_20256_(Vec3.f_82478_);
                this.boss.f_19864_ = true;
                this.boss.m_6710_(null);
                if (this.ticks % 4 == 0) {
                    this.cue(serverLevel, false);
                }
                double d = 1.0 - (double)this.ticks / (double)Math.max(1, this.chargeTicks);
                int n = 4 + (int)(d * 14.0);
                serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123809_, this.apex.f_82479_, this.apex.f_82480_, this.apex.f_82481_, n, 1.2 + d * 2.0, 1.2 + d * 2.0, 1.2 + d * 2.0, 0.35 + d * 0.5);
                serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123810_, this.apex.f_82479_, this.apex.f_82480_, this.apex.f_82481_, 2, 0.3 + d, 0.3 + d, 0.3 + d, 0.02);
                if (this.ticks % 10 == 0) {
                    serverLevel.m_6263_(null, this.apex.f_82479_, this.apex.f_82480_, this.apex.f_82481_, SoundEvents.f_144243_, SoundSource.HOSTILE, 1.0f, (float)(0.8 + d * 1.4));
                }
                if (--this.ticks > 0) break;
                this.phase = 3;
                this.ticks = 20 + QizaiRules.repeatTicks();
                this.impacts = 1;
                this.impact(serverLevel);
                break;
            }
            case 3: 
            case 4: {
                if (this.impacts < 3 && ++this.repeatTicks >= 12) {
                    this.repeatTicks = 0;
                    ++this.impacts;
                    this.impact(serverLevel);
                }
                if (--this.ticks > 0) break;
                this.settle(serverLevel);
                this.phase = 0;
                this.boss.setQizaiCue(new CompoundTag());
                return false;
            }
        }
        return true;
    }

    private void impact(ServerLevel serverLevel) {
        this.boss.m_20331_(true);
        Vec3 vec3 = this.origin;
        this.boss.m_7678_(vec3.f_82479_, vec3.f_82480_, vec3.f_82481_, this.boss.m_146908_(), this.boss.m_146909_());
        this.boss.m_20256_(Vec3.f_82478_);
        this.boss.f_19864_ = true;
        this.cue(serverLevel, true);
        serverLevel.m_6263_(null, vec3.f_82479_, vec3.f_82480_, vec3.f_82481_, SoundEvents.f_11913_, SoundSource.HOSTILE, 2.2f, 0.5f);
        serverLevel.m_6263_(null, vec3.f_82479_, vec3.f_82480_, vec3.f_82481_, SoundEvents.f_215771_, SoundSource.HOSTILE, 1.6f, 0.6f);
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123812_, vec3.f_82479_, vec3.f_82480_ + 0.5, vec3.f_82481_, 4, 3.0, 0.5, 3.0, 0.0);
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123747_, vec3.f_82479_, vec3.f_82480_ + 1.0, vec3.f_82481_, 3, 2.0, 0.5, 2.0, 0.0);
        int n = 0;
        double d = 2304.0;
        for (LivingEntity livingEntity : ManorLivingTargets.nearby((Level)serverLevel, (Entity)this.boss)) {
            Vec3 vec32;
            LivingEntity livingEntity2;
            if (!(livingEntity instanceof LivingEntity) || !(livingEntity2 = livingEntity).m_6084_() || livingEntity2.m_213877_() || ManorLivingTargets.creative(livingEntity2) || livingEntity2.m_5833_() || (vec32 = livingEntity2.m_20182_().m_82546_(vec3)).m_165925_() > d || Math.abs(vec32.f_82480_) > 32.0) continue;
            livingEntity2.f_19802_ = 0;
            livingEntity2.m_6469_(serverLevel.m_269111_().m_269104_((Entity)this.boss, (Entity)this.boss), 1000.0f);
            ++n;
        }
        this.announce(serverLevel, (String)(n > 0 ? "\u00a75\u3010\u5929\u9668\u661f\u88c1\u3011\u00a77\u661f\u88c1\u843d\u4e0b \u2014\u2014 \u00a7c" + n + " \u00a77\u4eba\u88ab\u51fb\u4e2d\uff081000 \u70b9\u9b54\u6cd5\u4f24\u5bb3\uff09" : "\u00a75\u3010\u5929\u9668\u661f\u88c1\u3011\u00a77\u661f\u88c1\u843d\u7a7a \u2014\u2014 \u00a7a\u6240\u6709\u4eba\u90fd\u5728 48 \u683c\u4e4b\u5916"));
        FTBossMod.LOGGER.info("[FTBoss][Qizai] \u300c\u5929\u9668\u661f\u88c1\u300d\u843d\u5730\u7ed3\u7b97: \u547d\u4e2d {} \u4eba, \u534a\u5f84 {} \u683c, \u56fa\u5b9a {} \u70b9\u9b54\u6cd5\u4f24\u5bb3", new Object[]{n, 48, 1000});
    }

    private void settle(ServerLevel serverLevel) {
        this.boss.m_20331_(false);
        Vec3 vec3 = this.origin;
        this.boss.m_7678_(vec3.f_82479_, vec3.f_82480_, vec3.f_82481_, this.boss.m_146908_(), this.boss.m_146909_());
        this.boss.m_20256_(Vec3.f_82478_);
        this.boss.f_19864_ = true;
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123796_, vec3.f_82479_, vec3.f_82480_ + 0.4, vec3.f_82481_, 24, 1.2, 0.3, 1.2, 0.05);
    }

    public void cancel() {
        Level level;
        ManorExpansionHooks.nova(this.boss, null, this.origin, 0);
        if (this.phase == 0) {
            return;
        }
        boolean bl = this.phase == 1 || this.phase == 2;
        this.phase = 0;
        this.ticks = 0;
        this.impacts = 0;
        this.repeatTicks = 0;
        this.boss.m_20331_(false);
        if (bl && (level = this.boss.m_9236_()) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            level = this.origin;
            this.boss.m_7678_(level.f_82479_, level.f_82480_, level.f_82481_, this.boss.m_146908_(), this.boss.m_146909_());
            this.boss.m_20256_(Vec3.f_82478_);
            this.boss.f_19864_ = true;
            this.boss.setQizaiCue(new CompoundTag());
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123796_, level.f_82479_, level.f_82480_ + 0.4, level.f_82481_, 16, 1.0, 0.3, 1.0, 0.04);
        }
    }

    private void cue(ServerLevel serverLevel, boolean bl) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.m_128405_("qizai", 8);
        compoundTag.m_128356_("start", bl ? serverLevel.m_46467_() : this.castStarted);
        compoundTag.m_128405_("duration", bl ? 20 : this.riseTicks + this.chargeTicks);
        compoundTag.m_128379_("released", bl);
        compoundTag.m_128405_("volley", this.impacts);
        compoundTag.m_128347_("radius", 48.0);
        compoundTag.m_128347_("x", this.origin.f_82479_);
        compoundTag.m_128347_("y", this.origin.f_82480_);
        compoundTag.m_128347_("z", this.origin.f_82481_);
        compoundTag.m_128347_("apex", this.apex.f_82480_);
        compoundTag.m_128405_("phase", this.phase);
        compoundTag.m_128405_("left", Math.max(0, this.ticks));
        this.boss.setQizaiCue(compoundTag);
    }

    private void announce(ServerLevel level, String text) {
        MutableComponent message = Component.m_237113_((String)text);
        for (ServerPlayer player : level.m_6907_()) {
            if (!(player.m_20280_((Entity)this.boss) <= 4096.0)) continue;
            player.m_5661_((Component)message, true);
        }
    }
}

