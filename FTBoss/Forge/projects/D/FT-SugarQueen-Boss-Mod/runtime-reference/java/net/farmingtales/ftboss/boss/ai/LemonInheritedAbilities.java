/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.redspace.ironsspellbooks.entity.spells.fireball.MagicFireball
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Position
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.projectile.EvokerFangs
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.Level$ExplosionInteraction
 *  net.minecraft.world.level.levelgen.Heightmap$Types
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.boss.ai;

import io.redspace.ironsspellbooks.entity.spells.fireball.MagicFireball;
import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.boss.BossHandler;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.ai.BarrelTactics;
import net.farmingtales.ftboss.boss.ai.BossRage;
import net.farmingtales.ftboss.boss.ai.ButcherCombatRules;
import net.farmingtales.ftboss.boss.ai.ButcherHitEffects;
import net.farmingtales.ftboss.boss.ai.LemonInheritedAbilityRules;
import net.farmingtales.ftboss.boss.ai.LemonSacrifice;
import net.farmingtales.ftboss.boss.ai.MinionBlessing;
import net.farmingtales.ftboss.boss.ai.TeaTactics;
import net.farmingtales.ftboss.entity.BarrelCannonballEntity;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.entity.RatBossEntity;
import net.farmingtales.ftboss.integ.spells.BossSpellEffects;
import net.farmingtales.ftboss.integ.spells.LemonSpellBarrage;
import net.farmingtales.ftboss.util.Fx;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

public final class LemonInheritedAbilities {
    private final EasterBossEntity owner;
    private final LemonSacrifice sacrifice;
    private final TeaTactics tea;
    private final BarrelTactics barrel;
    private Vec3 rollDirection = Vec3.f_82478_;
    private long rollStarted = -1L;
    private long blastAt = -1L;

    public LemonInheritedAbilities(EasterBossEntity owner, LemonSacrifice sacrifice) {
        this.owner = owner;
        this.sacrifice = sacrifice;
        this.tea = new TeaTactics((Mob)owner, sacrifice::inheritedChild);
        this.barrel = new BarrelTactics((Mob)owner, 5.0f);
    }

    private boolean due(String name, int period) {
        long now = this.owner.m_9236_().m_46467_();
        String key = "next_" + name;
        if (!this.sacrifice.state().m_128441_(key)) {
            this.sacrifice.state().m_128356_(key, now + (long)period);
            return false;
        }
        if (now < this.sacrifice.state().m_128454_(key)) {
            return false;
        }
        this.sacrifice.state().m_128356_(key, now + (long)period);
        return true;
    }

    public void tick() {
        boolean valid;
        Level level = this.owner.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        LivingEntity target = this.owner.m_5448_();
        boolean bl = valid = target != null && target.m_9236_() == level2 && BossSpellEffects.canTarget((LivingEntity)this.owner, target) && this.owner.m_20280_((Entity)target) <= 1024.0 && this.owner.m_142582_((Entity)target);
        if (this.due("purge", 20)) {
            for (MobEffectInstance effect : new ArrayList(this.owner.m_21220_())) {
                if (effect.m_19544_().m_19486_()) continue;
                this.owner.m_21195_(effect.m_19544_());
            }
        }
        if (this.due("blessing", 60)) {
            this.blessOwned(level2);
        }
        if (this.sacrifice.has("sugar_queen")) {
            this.queen((LivingEntity)(valid ? target : null));
        }
        if (this.sacrifice.has("tea_tree")) {
            this.tea(level2, (LivingEntity)(valid ? target : null));
        }
        if (this.sacrifice.has("wine_barrel")) {
            this.wine(level2, (LivingEntity)(valid ? target : null));
        }
        if (this.sacrifice.has("rat_boss")) {
            this.rats(level2, (LivingEntity)(valid ? target : null));
        }
        if (this.sacrifice.has("barrel_minion")) {
            this.barrelBlast(level2, (LivingEntity)(valid ? target : null));
        }
        if (this.sacrifice.has("meat_mountain") && valid && this.owner.m_20280_((Entity)target) <= 9.0 && this.due("butcher", ButcherCombatRules.interval(this.owner.m_21223_() < this.owner.m_21233_() * 0.5f))) {
            int hits = this.sacrifice.state().m_128451_("butcher_hits");
            float before = target.m_21223_() + target.m_6103_();
            if (target.m_6469_(this.owner.m_269291_().m_269333_((LivingEntity)this.owner), LemonInheritedAbilityRules.damage(ButcherCombatRules.damage(5.5f, hits)))) {
                this.onMeleeHit(target, Math.max(0.0f, before - target.m_21223_() - target.m_6103_()));
                this.owner.m_6674_(InteractionHand.MAIN_HAND);
            }
        }
    }

    private void blessOwned(ServerLevel level) {
        int count = 0;
        int duration = (int)Math.min(140L, this.sacrifice.state().m_128454_("until") - level.m_46467_());
        block0: for (Mob minion : level.m_6443_(Mob.class, this.owner.m_20191_().m_82400_(40.0), entity -> entity.m_6084_() && this.sacrifice.inheritedChild((Entity)entity))) {
            if (++count > 8) break;
            int effects = 0;
            for (MobEffectInstance effect : this.owner.m_21220_()) {
                if (!effect.m_19544_().m_19486_() || minion instanceof RatBossEntity && (effect.m_19544_() == MobEffects.f_19605_ || effect.m_19544_() == MobEffects.f_19617_ || effect.m_19544_() == MobEffects.f_19616_ || effect.m_19544_() == MobEffects.f_19618_ || effect.m_19544_() == MobEffects.f_19601_)) continue;
                minion.m_7292_(new MobEffectInstance(effect.m_19544_(), duration, Math.min(1, Math.max(0, effect.m_19564_() - 1)), true, false, true));
                if (++effects < 3) continue;
                continue block0;
            }
        }
    }

    private void queen(LivingEntity target) {
        Vec3 offset;
        if (this.due("sweet_aura", 10)) {
            this.grant("farm_and_charm:sweets", 600, 2);
            this.grant("brewinandchewin:sweet_heart", 600, 0);
        }
        if (target != null && !this.owner.isPerformingSkill() && this.rollStarted < 0L && (offset = target.m_20182_().m_82520_(0.0, 2.2, 0.0).m_82546_(this.owner.m_20182_())).m_82556_() > 4.0) {
            Vec3 destination = this.owner.m_20182_().m_82549_(offset.m_82541_().m_82490_(0.25));
            if (this.owner.m_9236_().m_45756_((Entity)this.owner, this.owner.m_20191_().m_82383_(destination.m_82546_(this.owner.m_20182_())))) {
                this.owner.m_6021_(destination.f_82479_, destination.f_82480_, destination.f_82481_);
            }
        }
    }

    private void tea(ServerLevel level, LivingEntity target) {
        double distance;
        boolean lowHealth;
        boolean bl = lowHealth = this.owner.m_21223_() < this.owner.m_21233_() * 0.5f;
        if (this.due("tea_aura", 10)) {
            this.grant("herbalbrews:renewal", 600, 1);
        }
        if (target != null) {
            this.tea.tickMinionSummon();
            this.tea.absorb();
        }
        if (this.due("tea_buffs", 110 + this.owner.m_217043_().m_188503_(60))) {
            List<MobEffect> pool = List.of(MobEffects.f_19596_, MobEffects.f_19600_, MobEffects.f_19598_, MobEffects.f_19606_, MobEffects.f_19605_, MobEffects.f_19617_, MobEffects.f_19618_, MobEffects.f_19607_, MobEffects.f_19621_, MobEffects.f_19603_, MobEffects.f_19608_);
            for (int count = 0; count < 2; ++count) {
                this.grant(pool.get(this.owner.m_217043_().m_188503_(pool.size())), 100 + this.owner.m_217043_().m_188503_(120), this.owner.m_217043_().m_188503_(3));
            }
        }
        if (target == null) {
            return;
        }
        if (this.due("tea_growth", 1200)) {
            int rank = Math.min(10, this.sacrifice.state().m_128451_("tea_growth") + 1);
            this.sacrifice.state().m_128405_("tea_growth", rank);
            this.grant(MobEffects.f_19616_, 12000, rank - 1);
            this.owner.m_5634_(4.0f);
        }
        if ((distance = (double)this.owner.m_20270_((Entity)target)) > 1.5 && distance < 16.0 && this.due("roots", lowHealth ? 62 : 100)) {
            Vec3 direction = target.m_20182_().m_82546_(this.owner.m_20182_()).m_82542_(1.0, 0.0, 1.0).m_82541_();
            int count = 9 + this.owner.m_217043_().m_188503_(3);
            for (int index = 1; index <= count; ++index) {
                Vec3 position = this.owner.m_20182_().m_82549_(direction.m_82490_((double)index * 1.6));
                BlockPos block = BlockPos.m_274446_((Position)position);
                double height = level.m_6924_(Heightmap.Types.MOTION_BLOCKING, block.m_123341_(), block.m_123343_());
                EvokerFangs fangs = new EvokerFangs((Level)level, position.f_82479_, height, position.f_82481_, (float)Math.atan2(direction.f_82481_, direction.f_82479_), (index - 1) * 2, (LivingEntity)this.owner);
                ManorFightMembers.inherit((Entity)this.owner, (Entity)fangs);
                level.m_7967_((Entity)fangs);
            }
            Fx.sound((Level)level, this.owner.m_20185_(), this.owner.m_20186_(), this.owner.m_20189_(), SoundEvents.f_144209_, 1.0f, 0.7f);
        }
        if (distance < 16.0 && this.due("poison", lowHealth ? 100 : 160)) {
            target.m_7292_(new MobEffectInstance(MobEffects.f_19614_, 220, 2));
            target.m_6469_(this.owner.m_269291_().m_269104_((Entity)this.owner, (Entity)this.owner), LemonInheritedAbilityRules.damage(2.5f));
            level.m_8767_((ParticleOptions)ParticleTypes.f_175833_, target.m_20185_(), target.m_20186_() + 1.0, target.m_20189_(), 16, 0.5, 0.5, 0.5, 0.03);
        }
    }

    private void wine(ServerLevel level, LivingEntity target) {
        int stage = this.owner.m_21223_() < this.owner.m_21233_() * 0.34f ? 2 : (this.owner.m_21223_() < this.owner.m_21233_() * 0.67f ? 1 : 0);
        stage = Math.max(stage, this.sacrifice.state().m_128451_("wine_stage"));
        this.sacrifice.state().m_128405_("wine_stage", stage);
        if (this.due("wine_aura", 10)) {
            this.grant("vinery:armor_effect", 600, stage);
            this.grant("vinery:health_effect", 600, 1 + stage);
            if (stage >= 1) {
                this.grant("vinery:resistance_effect", 600, stage - 1);
            }
            if (stage >= 2) {
                this.grant("vinery:luck_effect", 600, 1);
            }
            this.barrel.mist();
        }
        if (target == null) {
            this.rollStarted = -1L;
            return;
        }
        if (this.due("drunken_aura", 30)) {
            for (ServerPlayer player : level.m_6907_()) {
                if (!BossSpellEffects.canTarget((LivingEntity)this.owner, (LivingEntity)player) || !(this.owner.m_20280_((Entity)player) <= 49.0) || !this.owner.m_142582_((Entity)player)) continue;
                LemonInheritedAbilities.debuff((LivingEntity)player, "brewery:drunk", 200, 0);
                LemonInheritedAbilities.debuff((LivingEntity)player, "brewinandchewin:tipsy", 240, 4);
            }
        }
        if (this.due("cannon", stage >= 2 ? 110 : 170)) {
            this.cannon(level, target);
        }
        if (this.due("fireball", stage >= 2 ? 45 : 70)) {
            MagicFireball fireball = new MagicFireball((Level)level, (LivingEntity)this.owner);
            Vec3 direction = target.m_146892_().m_82546_(this.owner.m_146892_()).m_82541_();
            fireball.m_146884_(this.owner.m_146892_().m_82549_(direction.m_82490_(1.8)));
            fireball.setDamage(LemonInheritedAbilityRules.damage(11 + BossRage.tierOf((LivingEntity)this.owner) * 4));
            fireball.shoot(direction);
            ManorFightMembers.inherit((Entity)this.owner, (Entity)fireball);
            level.m_7967_((Entity)fireball);
            Fx.sound((Level)level, this.owner.m_20185_(), this.owner.m_20186_(), this.owner.m_20189_(), SoundEvents.f_11705_, 1.2f, 0.85f);
        }
        if (this.due("wine_summon", stage >= 1 ? 150 : 210)) {
            Entity entity2;
            int alive = level.m_6443_(Mob.class, this.owner.m_20191_().m_82400_(48.0), entity -> entity.m_6084_() && this.sacrifice.inheritedChild((Entity)entity) && (entity.m_6095_() == FTEntities.BARREL_MINION.get() || ForgeRegistries.ENTITY_TYPES.getKey((Object)entity.m_6095_()).toString().equals("brewery:beer_elemental"))).size();
            int count = Math.min(2 + this.owner.m_217043_().m_188503_(2), Math.max(0, 8 - alive));
            for (int index = 0; index < count; ++index) {
                this.summon(level, (Mob)((EntityType)FTEntities.BARREL_MINION.get()).m_20615_((Level)level), target);
            }
            EntityType beer = (EntityType)ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("brewery", "beer_elemental"));
            if (alive + count < 8 && beer != null && (entity2 = beer.m_20615_((Level)level)) instanceof Mob) {
                Mob minion = (Mob)entity2;
                this.summon(level, minion, target);
            }
        }
        this.roll(level, target);
        if (this.rollStarted < 0L && this.due("spray", 100)) {
            this.withFacing(target, this.barrel::corkSpray);
        }
    }

    private void cannon(ServerLevel level, LivingEntity target) {
        int count = 3 + this.owner.m_217043_().m_188503_(3);
        for (int index = 0; index < count; ++index) {
            double offsetX = target.m_20185_() + (this.owner.m_217043_().m_188500_() - 0.5) * 7.0 - this.owner.m_20185_();
            double offsetZ = target.m_20189_() + (this.owner.m_217043_().m_188500_() - 0.5) * 7.0 - this.owner.m_20189_();
            double distance = Math.max(1.0, Math.sqrt(offsetX * offsetX + offsetZ * offsetZ));
            double flight = distance / 1.05;
            BarrelCannonballEntity ball = new BarrelCannonballEntity((Level)level, (LivingEntity)this.owner);
            ball.setExplosionPower(15.0f);
            ball.m_6034_(this.owner.m_20185_() + offsetX / distance * 2.2, this.owner.m_20186_() + 1.9, this.owner.m_20189_() + offsetZ / distance * 2.2);
            ball.m_20334_(offsetX / distance * 1.05, (target.m_20188_() - this.owner.m_20186_() - 1.8) / flight + 0.0225 * flight, offsetZ / distance * 1.05);
            ManorFightMembers.inherit((Entity)this.owner, (Entity)ball);
            level.m_7967_((Entity)ball);
        }
        Fx.sound((Level)level, this.owner.m_20185_(), this.owner.m_20186_(), this.owner.m_20189_(), SoundEvents.f_11932_, 1.3f, 0.7f);
    }

    private void roll(ServerLevel level, LivingEntity target) {
        long elapsed;
        if (this.rollStarted < 0L) {
            if (this.owner.isPerformingSkill() || !this.due("roll", 130)) {
                return;
            }
            this.rollDirection = target.m_20182_().m_82546_(this.owner.m_20182_()).m_82542_(1.0, 0.0, 1.0).m_82541_();
            this.rollStarted = level.m_46467_();
        }
        if ((elapsed = level.m_46467_() - this.rollStarted) < 10L) {
            if (elapsed % 2L == 0L) {
                this.barrel.warnCharge(this.rollDirection.f_82479_, this.rollDirection.f_82481_);
            }
        } else if (elapsed < 32L) {
            if (!this.owner.isPerformingSkill()) {
                this.owner.m_20334_(this.rollDirection.f_82479_ * 0.42, this.owner.m_20184_().f_82480_, this.rollDirection.f_82481_ * 0.42);
                this.owner.f_19812_ = true;
            }
            if (elapsed % 3L == 0L) {
                this.barrel.rollTrail();
            }
            if (elapsed % 8L == 0L) {
                this.barrel.hitPlayers(2.9, 8.0f, 1.1);
            }
        } else if (elapsed < 42L) {
            if (elapsed % 2L == 0L) {
                this.barrel.warnSlam();
            }
        } else {
            this.barrel.slam();
            this.rollStarted = -1L;
        }
    }

    private void rats(ServerLevel level, LivingEntity target) {
        if (this.due("cheese", 160)) {
            MobEffect cheese = ForgeRegistries.MOB_EFFECTS.getValues().stream().filter(effect -> effect.m_19486_() && ForgeRegistries.MOB_EFFECTS.getKey(effect).m_135815_().contains("cheese")).findFirst().orElse(MobEffects.f_19596_);
            this.grant(cheese, 400, 0);
        }
        if (target == null || !this.due("swarm", this.owner.m_21223_() < this.owner.m_21233_() * 0.5f ? 65 : 100)) {
            return;
        }
        List rats = level.m_6443_(RatBossEntity.class, this.owner.m_20191_().m_82400_(48.0), rat -> rat.isMinion() && rat.m_6084_() && this.sacrifice.inheritedChild((Entity)rat));
        for (RatBossEntity rat2 : rats) {
            rat2.m_6710_(target);
        }
        int cap = this.owner.m_21223_() < this.owner.m_21233_() * 0.5f ? 16 : 12;
        int spawned = 0;
        for (int index = rats.size(); index < Math.min(cap, rats.size() + 6); ++index) {
            RatBossEntity rat3 = (RatBossEntity)((EntityType)FTEntities.RAT_BOSS.get()).m_20615_((Level)level);
            if (rat3 == null) continue;
            rat3.setMinion(true);
            if (!this.summon(level, (Mob)rat3, target)) continue;
            ++spawned;
        }
        if (spawned > 0) {
            this.owner.m_5634_(spawned * 3);
        }
        Fx.ring((Level)level, this.owner.m_20185_(), this.owner.m_20186_() + 0.1, this.owner.m_20189_(), 3.0, 20, (ParticleOptions)ParticleTypes.f_123759_, 0.02);
    }

    private boolean summon(ServerLevel level, Mob minion, LivingEntity target) {
        if (minion == null) {
            return false;
        }
        double angle = this.owner.m_217043_().m_188500_() * Math.PI * 2.0;
        double radius = 2.5 + this.owner.m_217043_().m_188500_() * 3.5;
        minion.m_6034_(this.owner.m_20185_() + Math.cos(angle) * radius, this.owner.m_20186_(), this.owner.m_20189_() + Math.sin(angle) * radius);
        minion.m_21530_();
        minion.m_6710_(target);
        MinionBlessing.mark((Entity)minion, this.owner.m_20148_());
        ManorFightMembers.inherit((Entity)this.owner, (Entity)minion);
        for (MobEffectInstance effect : this.owner.m_21220_()) {
            if (!effect.m_19544_().m_19486_() || minion instanceof RatBossEntity) continue;
            minion.m_7292_(new MobEffectInstance(effect));
        }
        return level.m_7967_((Entity)minion);
    }

    private void grant(String id, int duration, int amplifier) {
        this.grant((MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(id)), duration, amplifier);
    }

    private void grant(MobEffect effect, int duration, int amplifier) {
        if (effect != null) {
            this.sacrifice.grant(new MobEffectInstance(effect, duration, amplifier));
        }
    }

    private static void debuff(LivingEntity target, String id, int duration, int amplifier) {
        MobEffect effect = (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(id));
        if (effect != null) {
            target.m_7292_(new MobEffectInstance(effect, duration, amplifier));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void withFacing(LivingEntity target, Runnable action) {
        float previousYaw = this.owner.m_146908_();
        Vec3 direction = target.m_20182_().m_82546_(this.owner.m_20182_());
        this.owner.m_146922_((float)Math.toDegrees(Math.atan2(-direction.f_82479_, direction.f_82481_)));
        try {
            action.run();
        }
        finally {
            this.owner.m_146922_(previousYaw);
        }
    }

    public void onMeleeHit(LivingEntity target, float dealt) {
        if (dealt <= 0.0f) {
            return;
        }
        if (this.sacrifice.has("meat_mountain")) {
            int hits = this.sacrifice.state().m_128451_("butcher_hits");
            this.owner.m_5634_(ButcherCombatRules.lifesteal(dealt, this.owner.m_21233_() - this.owner.m_21223_()));
            ButcherHitEffects.apply((Mob)this.owner, target, ButcherCombatRules.empowered(hits));
            this.sacrifice.state().m_128405_("butcher_hits", hits + 1);
        }
        if (this.sacrifice.has("rat_boss") || this.sacrifice.has("rat_minion")) {
            this.owner.m_5634_(1.0f);
            if (target instanceof ServerPlayer) {
                ServerPlayer player = (ServerPlayer)target;
                BossHandler.applyRandomRatDebuff(player);
            }
        }
        if (this.sacrifice.has("sugar_queen")) {
            target.m_7292_(new MobEffectInstance(MobEffects.f_19612_, 80, 1));
            target.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 60));
        }
    }

    public void reset() {
        this.pause();
    }

    public void pause() {
        this.rollStarted = -1L;
        this.blastAt = -1L;
        LemonSpellBarrage.cancel(this.owner);
    }

    private void barrelBlast(ServerLevel level, LivingEntity target) {
        if (target == null || this.owner.m_20280_((Entity)target) > 81.0) {
            this.blastAt = -1L;
            return;
        }
        if (this.blastAt < 0L && this.owner.m_20280_((Entity)target) <= 25.0 && this.due("barrel_blast", 100)) {
            this.blastAt = level.m_46467_() + 30L;
        }
        if (this.blastAt < 0L) {
            return;
        }
        if (this.owner.f_19797_ % 2 == 0) {
            level.m_8767_((ParticleOptions)ParticleTypes.f_123762_, this.owner.m_20185_(), this.owner.m_20186_() + 0.8, this.owner.m_20189_(), 2, 0.2, 0.2, 0.2, 0.02);
        }
        if (level.m_46467_() >= this.blastAt) {
            this.blastAt = -1L;
            level.m_254849_((Entity)this.owner, this.owner.m_20185_(), this.owner.m_20186_() + 0.4, this.owner.m_20189_(), 1.2f, Level.ExplosionInteraction.NONE);
        }
    }
}

