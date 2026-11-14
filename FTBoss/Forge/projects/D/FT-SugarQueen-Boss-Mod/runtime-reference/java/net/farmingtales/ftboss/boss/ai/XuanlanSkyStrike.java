/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.body.BodySignals;
import net.farmingtales.ftboss.boss.HiddenBossIntro;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorSafeTeleport;
import net.farmingtales.ftboss.boss.ai.ManorLivingTargets;
import net.farmingtales.ftboss.boss.ai.ManorPredictiveAttacks;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.skill.XuanlanFxPacket;
import net.farmingtales.ftboss.util.XuanlanSkyRules;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class XuanlanSkyStrike {
    private final EasterBossEntity boss;
    private int uses;
    private int age;
    private int strikes;
    private int warnings;
    private int recovery;
    private boolean active;
    private boolean oldInvisible;
    private boolean oldNoGravity;
    private boolean oldNoPhysics;
    private Vec3 home = Vec3.f_82478_;
    private Vec3 landing = Vec3.f_82478_;
    private final ArrayDeque<PendingStrike> pendingStrikes = new ArrayDeque();
    private LivingEntity target;

    public XuanlanSkyStrike(EasterBossEntity easterBossEntity) {
        this.boss = easterBossEntity;
    }

    public boolean active() {
        return this.active;
    }

    /*
     * WARNING - void declaration
     */
    public boolean tick(ServerLevel serverLevel) {
        if (!this.active) {
            LivingEntity livingEntity;
            if (this.recovery > 0) {
                --this.recovery;
                return false;
            }
            if (this.boss.getVariant() != 6 || this.boss.isMini() || this.boss.isGiant() || this.boss.m_21525_() || this.boss.m_20147_() || this.boss.isPerformingSkill() || ManorFight.isFinaleMember((LivingEntity)this.boss) || HiddenBossIntro.isActing(this.boss.m_20148_()) || !XuanlanSkyRules.ready(this.uses, this.boss.m_21223_(), this.boss.m_21233_()) || !((livingEntity = this.boss.m_5448_()) instanceof LivingEntity) || !this.valid(livingEntity, this.boss.m_20182_())) {
                return false;
            }
            Vec3 vec3 = ManorSafeTeleport.find(serverLevel, (Entity)this.boss, this.boss.m_20182_(), 4);
            if (vec3 == null) {
                return false;
            }
            this.landing = this.home = vec3;
            this.target = livingEntity;
            this.oldInvisible = this.boss.m_20145_();
            this.oldNoGravity = this.boss.m_20068_();
            this.oldNoPhysics = this.boss.f_19794_;
            this.active = true;
            ++this.uses;
            this.age = 0;
            this.strikes = 0;
            this.warnings = 0;
            this.pendingStrikes.clear();
            this.boss.m_8127_();
            this.boss.m_20242_(true);
            this.boss.f_19794_ = true;
            this.send(serverLevel, 20, this.home, this.home.m_82520_(0.0, 24.0, 0.0));
            serverLevel.m_6263_(null, this.home.f_82479_, this.home.f_82480_, this.home.f_82481_, SoundEvents.f_11736_, SoundSource.HOSTILE, 1.8f, 1.5f);
            for (LivingEntity object : ManorLivingTargets.nearby((Level)serverLevel, (Entity)this.boss)) {
                if (!(object.m_20238_(this.home) <= 4096.0)) continue;
                ManorLivingTargets.tell(object, (Component)Component.m_237113_((String)"\u00a7b\u00a7l\u7384\u84dd \u00b7 \u5929\u7a79\u9501\u51fb \u00a7f60\u8fde\u51fb\uff01\u00a7e\u79bb\u5f00\u811a\u4e0b\u9884\u8b66\u5708\uff01"), true);
            }
            FTBossMod.LOGGER.info("[FTBoss][XuanlanSky] {} begin use={}/{}", new Object[]{this.boss.m_20148_(), this.uses, 2});
        }
        if (!this.boss.m_6084_() || this.boss.m_21525_() || ManorFight.isFinaleMember((LivingEntity)this.boss)) {
            this.cancel();
            return true;
        }
        this.boss.m_21573_().m_26573_();
        this.boss.m_20256_(Vec3.f_82478_);
        this.boss.f_19789_ = 0.0f;
        if (this.age < 20) {
            this.move(this.home.m_82520_(0.0, 24.0 * XuanlanSkyRules.rise(((double)this.age + 1.0) / 20.0), 0.0));
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123810_, this.boss.m_20185_(), this.boss.m_20186_() + 0.8, this.boss.m_20189_(), 5, 0.35, 0.5, 0.35, 0.04);
        } else {
            int n = this.age - 20;
            int n2 = XuanlanSkyRules.attackTicks();
            if (n < n2) {
                this.boss.setXuanlanSkyHidden(true);
                this.boss.m_6842_(true);
                this.move(this.home.m_82520_(0.0, 24.0, 0.0));
                if (this.warnings < 60 && n == XuanlanSkyRules.warningTick(this.warnings)) {
                    void var5_11;
                    ArrayList<LivingEntity> arrayList = new ArrayList<LivingEntity>();
                    for (LivingEntity livingEntity : ManorLivingTargets.nearby((Level)serverLevel, (Entity)this.boss)) {
                        if (!this.valid(livingEntity, this.home)) continue;
                        arrayList.add(livingEntity);
                    }
                    if (arrayList.isEmpty() && this.valid(this.target, this.home)) {
                        arrayList.add(this.target);
                    }
                    if (arrayList.isEmpty()) {
                        this.cancel();
                        return true;
                    }
                    this.target = (LivingEntity)arrayList.get(0);
                    Object var5_10 = null;
                    Iterator iterator = arrayList.iterator();
                    while (iterator.hasNext()) {
                        LivingEntity livingEntity = (LivingEntity)iterator.next();
                        Vec3 vec3 = livingEntity.m_20182_();
                        vec3 = serverLevel.m_45547_(new ClipContext(vec3.m_82520_(0.0, 0.5, 0.0), vec3.m_82520_(0.0, -12.0, 0.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)this.boss)).m_82450_().m_82520_(0.0, 0.04, 0.0);
                        Vec3 vec32 = vec3.m_82520_(0.0, Math.max(24.0, vec3.f_82480_ - vec3.f_82480_ + 8.0), 0.0);
                        this.pendingStrikes.addLast(new PendingStrike(vec3, vec32, n + 10));
                        this.queuePrediction(serverLevel, livingEntity, n + 10);
                        this.send(serverLevel, 21, vec3, vec32);
                        if (var5_11 != null) continue;
                        Vec3 vec33 = vec3;
                    }
                    ++this.warnings;
                    if (var5_11 != null) {
                        serverLevel.m_6263_(null, var5_11.f_82479_, var5_11.f_82480_, var5_11.f_82481_, (SoundEvent)SoundEvents.f_12211_.m_203334_(), SoundSource.HOSTILE, 1.2f, 1.6f);
                    }
                }
                while (!this.pendingStrikes.isEmpty() && this.pendingStrikes.peekFirst().impactAge() <= n) {
                    this.strike(serverLevel, this.pendingStrikes.removeFirst());
                    ++this.strikes;
                }
            } else {
                int n3 = n - n2;
                if (n3 == 0) {
                    Vec3 vec3 = ManorSafeTeleport.find(serverLevel, (Entity)this.boss, this.home, 8);
                    if (vec3 != null) {
                        this.landing = vec3;
                    }
                    this.boss.setXuanlanSkyHidden(false);
                    this.boss.m_6842_(this.oldInvisible);
                    this.send(serverLevel, 23, this.landing, this.landing.m_82520_(0.0, 24.0, 0.0));
                }
                this.move(this.landing.m_82520_(0.0, 24.0 * (1.0 - XuanlanSkyRules.rise(((double)n3 + 1.0) / 20.0)), 0.0));
                if (n3 + 1 >= 20) {
                    this.cancel();
                    return true;
                }
            }
        }
        ++this.age;
        return true;
    }

    private boolean valid(LivingEntity livingEntity, Vec3 vec3) {
        if (!ManorLivingTargets.valid((Entity)this.boss, livingEntity)) {
            return false;
        }
        return livingEntity != null && livingEntity.m_9236_() == this.boss.m_9236_() && livingEntity.m_6084_() && !livingEntity.m_213877_() && !ManorLivingTargets.creative(livingEntity) && !livingEntity.m_5833_() && !this.boss.m_7307_((Entity)livingEntity) && livingEntity.m_20238_(vec3) <= 4096.0;
    }

    private void move(Vec3 vec3) {
        this.boss.m_6021_(vec3.f_82479_, vec3.f_82480_, vec3.f_82481_);
        this.boss.m_20256_(Vec3.f_82478_);
        this.boss.f_19789_ = 0.0f;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void strike(ServerLevel serverLevel, PendingStrike pendingStrike) {
        Vec3 vec3 = pendingStrike.locked();
        Vec3 vec32 = pendingStrike.beamTop();
        this.send(serverLevel, 22, vec3, vec32);
        serverLevel.m_6263_(null, vec3.f_82479_, vec3.f_82480_, vec3.f_82481_, SoundEvents.f_12001_, SoundSource.HOSTILE, 1.5f, 1.4f);
        serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123810_, vec3.f_82479_, vec3.f_82480_ + 0.3, vec3.f_82481_, 24, 1.4, 0.3, 1.4, 0.16);
        double d = 3.0;
        AABB aABB = new AABB(vec3.f_82479_ - d, vec3.f_82480_ - 0.1, vec3.f_82481_ - d, vec3.f_82479_ + d, vec32.f_82480_, vec3.f_82481_ + d);
        for (LivingEntity livingEntity2 : serverLevel.m_6443_(LivingEntity.class, aABB, livingEntity -> this.valid((LivingEntity)livingEntity, this.home))) {
            double d2;
            double d3;
            AABB aABB2 = livingEntity2.m_20191_();
            double d4 = Math.max(aABB2.f_82288_, Math.min(vec3.f_82479_, aABB2.f_82291_));
            if ((d4 - vec3.f_82479_) * (d4 - vec3.f_82479_) + (d3 - vec3.f_82481_) * ((d2 = Math.max(aABB2.f_82290_, Math.min(vec3.f_82481_, aABB2.f_82293_))) - vec3.f_82481_) > d * d) continue;
            int n = livingEntity2.f_19802_;
            livingEntity2.f_19802_ = 0;
            try {
                livingEntity2.m_6469_(serverLevel.m_269111_().m_269104_((Entity)this.boss, (Entity)this.boss), this.boss.manorSkillDamage(livingEntity2, 230.0f, 2.0));
            }
            finally {
                livingEntity2.f_19802_ = Math.max(n, livingEntity2.f_19802_);
            }
        }
    }

    private void send(ServerLevel serverLevel, int n, Vec3 vec3, Vec3 vec32) {
        BodySignals.sky(this.boss, n);
        XuanlanFxPacket.send(serverLevel, n, this.boss.m_19879_(), vec3, vec32);
    }

    public void cancel() {
        if (!this.active) {
            return;
        }
        Level level = this.boss.m_9236_();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            this.send(serverLevel, 24, this.home, this.boss.m_20182_());
            Vec3 vec3 = ManorSafeTeleport.find(serverLevel, (Entity)this.boss, this.landing, 8);
            this.move(vec3 == null ? this.home : vec3);
            serverLevel.m_6263_(null, this.boss.m_20185_(), this.boss.m_20186_(), this.boss.m_20189_(), SoundEvents.f_11738_, SoundSource.HOSTILE, 1.2f, 1.4f);
        }
        this.boss.setXuanlanSkyHidden(false);
        this.boss.m_6842_(this.oldInvisible);
        this.boss.m_20242_(this.oldNoGravity);
        this.boss.f_19794_ = this.oldNoPhysics;
        this.active = false;
        this.pendingStrikes.clear();
        this.target = null;
        this.recovery = 200;
        FTBossMod.LOGGER.info("[FTBoss][XuanlanSky] {} end strikes={} uses={}", new Object[]{this.boss.m_20148_(), this.strikes, this.uses});
    }

    public void save(CompoundTag compoundTag) {
        CompoundTag compoundTag2 = new CompoundTag();
        compoundTag2.m_128405_("Uses", this.uses);
        compoundTag2.m_128405_("Recovery", this.recovery);
        compoundTag2.m_128379_("Active", this.active);
        compoundTag2.m_128379_("Invisible", this.oldInvisible);
        compoundTag2.m_128379_("NoGravity", this.oldNoGravity);
        compoundTag2.m_128379_("NoPhysics", this.oldNoPhysics);
        compoundTag2.m_128347_("HomeX", this.home.f_82479_);
        compoundTag2.m_128347_("HomeY", this.home.f_82480_);
        compoundTag2.m_128347_("HomeZ", this.home.f_82481_);
        compoundTag.m_128365_("XuanlanSkyStrike", (Tag)compoundTag2);
    }

    public void load(CompoundTag compoundTag) {
        CompoundTag compoundTag2 = compoundTag.m_128469_("XuanlanSkyStrike");
        this.uses = Math.max(0, Math.min(2, compoundTag2.m_128451_("Uses")));
        this.recovery = Math.max(0, Math.min(200, compoundTag2.m_128451_("Recovery")));
        if (!compoundTag2.m_128471_("Active")) {
            return;
        }
        this.home = new Vec3(compoundTag2.m_128459_("HomeX"), compoundTag2.m_128459_("HomeY"), compoundTag2.m_128459_("HomeZ"));
        if (!(Double.isFinite(this.home.f_82479_) && Double.isFinite(this.home.f_82480_) && Double.isFinite(this.home.f_82481_))) {
            this.home = this.boss.m_20182_();
        }
        this.landing = this.home;
        this.oldInvisible = compoundTag2.m_128471_("Invisible");
        this.oldNoGravity = compoundTag2.m_128471_("NoGravity");
        this.oldNoPhysics = compoundTag2.m_128471_("NoPhysics");
        this.active = true;
        this.cancel();
    }

    private void queuePrediction(ServerLevel serverLevel, LivingEntity livingEntity, int n) {
        Vec3 vec3 = ManorPredictiveAttacks.skyPrediction(serverLevel, this.boss, livingEntity, this.home);
        if (vec3 == null) {
            return;
        }
        Vec3 vec32 = vec3.m_82520_(0.0, Math.max(24.0, livingEntity.m_20186_() - vec3.f_82480_ + 8.0), 0.0);
        this.pendingStrikes.addLast(new PendingStrike(vec3, vec32, n));
        XuanlanFxPacket.send(serverLevel, 21, this.boss.m_19879_(), vec3, vec32);
    }

    private record PendingStrike(Vec3 locked, Vec3 beamTop, int impactAge) {
    }
}

