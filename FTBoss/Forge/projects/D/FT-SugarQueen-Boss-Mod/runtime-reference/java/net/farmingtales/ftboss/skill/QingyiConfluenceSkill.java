/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.DustParticleOptions
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.OwnableEntity
 *  net.minecraft.world.entity.decoration.ArmorStand
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Vector3f
 */
package net.farmingtales.ftboss.skill;

import java.util.Optional;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.animation.qingyicast.QingyiCastSync;
import net.farmingtales.ftboss.boss.ai.ManorLivingTargets;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.item.weapons.QingyiConfluenceTridentItem;
import net.farmingtales.ftboss.qingyilight.LightHooks;
import net.farmingtales.ftboss.util.ManorWeaponBalanceRules;
import net.farmingtales.ftboss.util.QingyiConfluenceRules;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class QingyiConfluenceSkill {
    private static final DustParticleOptions[] ELEMENTS = new DustParticleOptions[]{new DustParticleOptions(new Vector3f(0.2f, 0.95f, 1.0f), 1.2f), new DustParticleOptions(new Vector3f(0.95f, 0.22f, 0.48f), 1.2f), new DustParticleOptions(new Vector3f(0.4f, 0.34f, 1.0f), 1.2f), new DustParticleOptions(new Vector3f(1.0f, 0.85f, 0.36f), 1.2f)};
    private final EasterBossEntity boss;
    private int elapsed = -1;
    private LivingEntity target;
    private ItemStack weapon = ItemStack.f_41583_;
    private Vec3 center = Vec3.f_82478_;

    public QingyiConfluenceSkill(EasterBossEntity boss) {
        this.boss = boss;
    }

    public boolean busy() {
        return this.elapsed >= 0;
    }

    public void start(ServerLevel level, LivingEntity victim) {
        if (this.busy() || this.boss.isMini() || this.boss.m_21525_() || !this.boss.m_6084_() || this.boss.getVariant() != 5 || !QingyiConfluenceSkill.canTarget((LivingEntity)this.boss, victim) || !this.boss.m_142582_((Entity)victim) || this.boss.m_20280_((Entity)victim) > 576.0 || !(this.boss.m_21205_().m_41720_() instanceof QingyiConfluenceTridentItem)) {
            return;
        }
        this.target = victim;
        this.weapon = this.boss.m_21205_();
        Vec3 offset = victim.m_20182_().m_82520_(0.0, 0.15, 0.0).m_82546_(this.boss.m_146892_());
        this.center = QingyiConfluenceSkill.clipped(level, (LivingEntity)this.boss, this.boss.m_146892_(), this.boss.m_146892_().m_82549_(offset.m_82541_().m_82490_(Math.min(24.0, offset.m_82553_()))));
        this.elapsed = 32 - this.boss.getChallengeDifficulty().acceleratedTicks(32);
        this.boss.beginManorFiveCast(QingyiConfluenceRules.useTicks() - this.elapsed, QingyiConfluenceRules.useTicks() - this.elapsed, this.center, 3.5, true);
        LightHooks.confluence(this.boss, this.center, this.elapsed);
        QingyiCastSync.startConfluence(this.boss, this.elapsed);
        QingyiConfluenceTridentItem.animate((LivingEntity)this.boss, "charge");
        level.m_5594_(null, this.boss.m_20183_(), SoundEvents.f_11739_, SoundSource.HOSTILE, 1.1f, 0.7f);
        FTBossMod.LOGGER.info("[FTBoss][Qingyi] \u56db\u8c61\u5f52\u589f charge -> {}", (Object)victim.m_7755_().getString());
    }

    public void tick(ServerLevel level) {
        if (!this.busy()) {
            return;
        }
        if (!this.boss.m_6084_() || this.boss.m_21525_() || this.boss.getVariant() != 5 || this.boss.m_21205_() != this.weapon || this.target == null || this.target.m_9236_() != level || !QingyiConfluenceSkill.canTarget((LivingEntity)this.boss, this.target) || this.boss.m_20280_((Entity)this.target) > 2304.0) {
            this.cancel();
            return;
        }
        ++this.elapsed;
        this.boss.m_21563_().m_24950_(this.center.f_82479_, this.center.f_82480_, this.center.f_82481_, 60.0f, 60.0f);
        if (this.elapsed < QingyiConfluenceRules.useTicks() && this.elapsed % 4 == 0) {
            QingyiConfluenceSkill.warning(level, this.center, this.elapsed);
            QingyiConfluenceSkill.chargeFx(level, (LivingEntity)this.boss, this.elapsed);
        }
        if (this.elapsed == 32) {
            QingyiConfluenceTridentItem.animate((LivingEntity)this.boss, "release");
            QingyiCastSync.pulseNative(this.boss);
        }
        if (QingyiConfluenceRules.impactAt(this.elapsed)) {
            this.boss.finishManorFiveCast(this.center);
            QingyiConfluenceSkill.burst(level, (LivingEntity)this.boss, this.center);
        }
        if (QingyiConfluenceRules.finished(this.elapsed)) {
            this.elapsed = -1;
            this.target = null;
            this.weapon = ItemStack.f_41583_;
        }
    }

    public void cancel() {
        if (!this.busy()) {
            return;
        }
        QingyiConfluenceTridentItem.cancel((LivingEntity)this.boss, this.weapon);
        this.elapsed = -1;
        this.target = null;
        this.weapon = ItemStack.f_41583_;
        this.boss.cancelManorFiveCast();
    }

    /*
     * WARNING - void declaration
     */
    public static boolean canTarget(LivingEntity livingEntity, LivingEntity livingEntity2) {
        LivingEntity source;
        void victim;
        if (livingEntity instanceof EasterBossEntity) {
            return ManorLivingTargets.valid((Entity)livingEntity, livingEntity2);
        }
        if (victim == source || !victim.m_6084_() || victim.m_213877_() || victim.m_5833_() || victim instanceof ArmorStand || source.m_7307_((Entity)victim) || victim.m_7307_((Entity)source)) {
            return false;
        }
        if (source instanceof EasterBossEntity && !(victim instanceof Player)) {
            return false;
        }
        if (victim instanceof Player) {
            Player attacker;
            Player player = (Player)victim;
            if (player.m_7500_() || player.m_5833_()) {
                return false;
            }
            if (!(!(source instanceof Player) || (attacker = (Player)source).m_7099_(player) && source.m_20194_() != null && source.m_20194_().m_129799_())) {
                return false;
            }
        }
        if (victim instanceof OwnableEntity) {
            OwnableEntity pet = (OwnableEntity)victim;
            LivingEntity owner = pet.m_269323_();
            if (source.m_20148_().equals(pet.m_21805_()) || owner != null && source.m_7307_((Entity)owner)) {
                return false;
            }
        }
        return true;
    }

    private static Vec3 clipped(ServerLevel level, LivingEntity source, Vec3 start, Vec3 end) {
        BlockHitResult result = level.m_45547_(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)source));
        return result.m_6662_() == HitResult.Type.MISS ? end : result.m_82450_().m_82546_(end.m_82546_(start).m_82541_().m_82490_(0.15));
    }

    public static Vec3 aim(Player player) {
        ServerLevel level = (ServerLevel)player.m_9236_();
        Vec3 start = player.m_146892_();
        Vec3 end = QingyiConfluenceSkill.clipped(level, (LivingEntity)player, start, start.m_82549_(player.m_20154_().m_82490_(24.0)));
        double nearest = start.m_82557_(end);
        Vec3 selected = end;
        for (LivingEntity victim : level.m_6443_(LivingEntity.class, new AABB(start, end).m_82400_(0.5), candidate -> QingyiConfluenceSkill.canTarget((LivingEntity)player, candidate))) {
            Optional intersection = victim.m_20191_().m_82400_(0.3).m_82371_(start, end);
            if (!intersection.isPresent() || !(start.m_82557_((Vec3)intersection.get()) < nearest)) continue;
            nearest = start.m_82557_((Vec3)intersection.get());
            selected = (Vec3)intersection.get();
        }
        return selected;
    }

    public static void mark(LivingEntity target, int ticks) {
        target.m_7292_(new MobEffectInstance(MobEffects.f_19597_, ticks, 1));
        target.m_7292_(new MobEffectInstance(MobEffects.f_19615_, ticks, 0));
        target.m_7292_(new MobEffectInstance(MobEffects.f_19599_, ticks, 0));
        target.m_7292_(new MobEffectInstance(MobEffects.f_19619_, ticks, 0));
    }

    /*
     * WARNING - void declaration
     */
    public static void burst(ServerLevel serverLevel, LivingEntity livingEntity, Vec3 vec3) {
        void source;
        ServerLevel level;
        void center;
        LightHooks.burst(livingEntity, vec3);
        AABB area = new AABB((Vec3)center, (Vec3)center).m_82377_(3.5, 3.0, 3.5);
        int hits = 0;
        for (LivingEntity victim : level.m_6443_(LivingEntity.class, area, arg_0 -> QingyiConfluenceSkill.lambda$burst$1((LivingEntity)source, arg_0))) {
            Vec3 offset = victim.m_20182_().m_82546_((Vec3)center);
            if (!QingyiConfluenceRules.inside(offset.f_82479_, offset.f_82480_, offset.f_82481_) || !source.m_142582_((Entity)victim) || level.m_45547_(new ClipContext((Vec3)center, victim.m_20191_().m_82399_(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)source)).m_6662_() != HitResult.Type.MISS) continue;
            float damage = ManorWeaponBalanceRules.skillDamage(48.0f);
            if (source instanceof EasterBossEntity) {
                EasterBossEntity boss = (EasterBossEntity)source;
                float base = 54.0f + boss.qingyiDamageBonus();
                damage = boss.manorSkillDamage(victim, base, 2.0);
            }
            if (!victim.m_6469_(level.m_269111_().m_269104_((Entity)source, (Entity)source), damage)) continue;
            QingyiConfluenceSkill.mark(victim, 80);
            Vec3 pull = center.m_82546_(victim.m_20182_());
            if (pull.m_165925_() > 0.01) {
                Vec3 direction = new Vec3(pull.f_82479_, 0.0, pull.f_82481_).m_82541_().m_82490_(0.45);
                victim.m_5997_(direction.f_82479_, 0.12, direction.f_82481_);
                victim.f_19864_ = true;
            }
            if (++hits < 32) continue;
            break;
        }
        for (int segment = 0; segment < 64; ++segment) {
            double angle = (double)segment * Math.PI / 32.0;
            double radius = 3.5;
            level.m_8767_((ParticleOptions)ELEMENTS[segment / 16], center.f_82479_ + Math.cos(angle) * radius, center.f_82480_ + 0.15, center.f_82481_ + Math.sin(angle) * radius, 1, 0.0, 0.0, 0.0, 0.0);
        }
        Vec3 start = source.m_146892_();
        for (int point = 0; point <= 32; ++point) {
            Vec3 position = start.m_165921_((Vec3)center, (double)point / 32.0);
            level.m_8767_((ParticleOptions)ELEMENTS[point % 4], position.f_82479_, position.f_82480_, position.f_82481_, 1, 0.035, 0.035, 0.035, 0.0);
        }
        level.m_8767_((ParticleOptions)ParticleTypes.f_175830_, center.f_82479_, center.f_82480_ + 1.0, center.f_82481_, 28, 0.8, 1.3, 0.8, 0.12);
        level.m_8767_((ParticleOptions)ParticleTypes.f_123810_, center.f_82479_, center.f_82480_ + 0.5, center.f_82481_, 16, 0.5, 0.8, 0.5, 0.08);
        level.m_6263_(null, center.f_82479_, center.f_82480_, center.f_82481_, SoundEvents.f_12521_, SoundSource.PLAYERS, 1.2f, 1.25f);
        level.m_6263_(null, center.f_82479_, center.f_82480_, center.f_82481_, (SoundEvent)SoundEvents.f_12377_.m_203334_(), SoundSource.PLAYERS, 0.7f, 1.4f);
    }

    public static void chargeFx(ServerLevel level, LivingEntity source, int elapsed) {
        for (int element = 0; element < 4; ++element) {
            double angle = (double)elapsed * 0.18 + (double)element * Math.PI / 2.0;
            double radius = 0.7 * (1.0 - Math.min(1.0, (double)elapsed / 32.0)) + 0.12;
            Vec3 point = source.m_146892_().m_82549_(source.m_20154_().m_82490_(0.6)).m_82520_(Math.cos(angle) * radius, Math.sin(angle) * radius, 0.0);
            level.m_8767_((ParticleOptions)ELEMENTS[element], point.f_82479_, point.f_82480_, point.f_82481_, 1, 0.02, 0.02, 0.02, 0.0);
        }
    }

    private static void warning(ServerLevel level, Vec3 center, int elapsed) {
        for (int point = 0; point < 24; ++point) {
            double angle = (double)point * Math.PI / 12.0 + (double)elapsed * 0.015;
            level.m_8767_((ParticleOptions)ELEMENTS[point / 6], center.f_82479_ + Math.cos(angle) * 3.5, center.f_82480_ + 0.08, center.f_82481_ + Math.sin(angle) * 3.5, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    private static /* synthetic */ boolean lambda$burst$1(LivingEntity source, LivingEntity candidate) {
        return QingyiConfluenceSkill.canTarget(source, candidate);
    }
}

