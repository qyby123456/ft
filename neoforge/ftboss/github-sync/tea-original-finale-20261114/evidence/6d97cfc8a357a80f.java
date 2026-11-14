package net.farmingtales.ftboss.entity;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ICastData;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.BossBarVisibility;
import net.farmingtales.ftboss.boss.BossBars;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.ai.BossActionState;
import net.farmingtales.ftboss.boss.ai.BossDamageGate;
import net.farmingtales.ftboss.boss.ai.BossMeleeWindup;
import net.farmingtales.ftboss.boss.ai.BossRage;
import net.farmingtales.ftboss.boss.ai.BossTargets;
import net.farmingtales.ftboss.boss.ai.LemonSummonRewards;
import net.farmingtales.ftboss.boss.ai.MinionBlessing;
import net.farmingtales.ftboss.boss.ai.NonManorBossLootRouter;
import net.farmingtales.ftboss.boss.ai.SayaBuds;
import net.farmingtales.ftboss.boss.ai.SummonSweep;
import net.farmingtales.ftboss.boss.ai.TeaTactics;
import net.farmingtales.ftboss.boss.ai.TrapBreaker;
import net.farmingtales.ftboss.integ.WizardSummon;
import net.farmingtales.ftboss.integ.spells.BossSpellPacing;
import net.farmingtales.ftboss.integ.spells.BossSpellTuning;
import net.farmingtales.ftboss.integ.spells.RegularBossCastHost;
import net.farmingtales.ftboss.item.FTBossItems;
import net.farmingtales.ftboss.saya.SayaCastSync;
import net.farmingtales.ftboss.sound.BossMusic;
import net.farmingtales.ftboss.util.Fx;
import net.farmingtales.ftboss.util.BossSpellRules.Kind;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

public class TeaTreeBossEntity extends AbstractSpellCastingMob implements RegularBossCastHost {
   private static final EntityDataAccessor<CompoundTag> DATA_ACTION = SynchedEntityData.m_135353_(TeaTreeBossEntity.class, EntityDataSerializers.f_135042_);
   private final BossActionState actionState = new BossActionState(this, DATA_ACTION);
   private static final EntityDataAccessor<CompoundTag> DATA_SIGNATURE_CAST = SynchedEntityData.m_135353_(
      TeaTreeBossEntity.class, EntityDataSerializers.f_135042_
   );
   private final BossActionState signatureCast = new BossActionState(this, DATA_SIGNATURE_CAST);
   private final BossMeleeWindup meleeWindup = new BossMeleeWindup(this, this.actionState, 6, 14, 3.0);
   private UUID nativeTarget;
   private Vec3 nativeAim = Vec3.f_82478_;
   private Vec3 rootDirection = Vec3.f_82478_;
   private boolean nativePending;
   private static final double BOSS_BAR_RANGE = 48.0;
   private static final double MAX_HP = 340.0;
   private static final String FINALE_TAG = "ftboss:saya_finale";
   private static final double FINALE_HEALTH_MULTIPLIER = 2.0;
   private static final double FINALE_ATTACK_MULTIPLIER = 2.0;
   private static final int FANG_CD = 100;
   private static final int SPIT_CD = 160;
   private static final int FANG_CD_P2 = 62;
   private static final int SPIT_CD_P2 = 100;
   private static final int HP_BOOST_INTERVAL = 1200;
   private static final int HP_BOOST_MAX = 10;
   private static final float HP_BOOST_PER_LEVEL = 4.0F;
   private final ServerBossEvent bossBar;
   private MobEffect teaRenewal;
   private int auraTimer = 0;
   private int fangCd = 100;
   private int spitCd = 160;
   private int contactCd = 0;
   private int castCd = 80;
   private int buffWheelCd = 60;
   private static final String MINION_TAG = "tea_minion";
   private final SayaBuds buds = new SayaBuds(this);
   private boolean musicStarted = false;
   private final TeaTactics tactics = new TeaTactics(this);
   private final List<AbstractSpell> castQueue = new ArrayList<>();
   private boolean phase2 = false;
   private boolean secondLifeUsed = false;
   private int hpBoostLevel = 0;
   private static final String[] SAYA_SELF_SPELL_FIELDS = new String[]{"OAKSKIN_SPELL", "ABSORPTION_SPELL"};
   private List<AbstractSpell> sayaSelfSpells;

   public void initiateCastSpell(AbstractSpell spell, int spellLevel) {
      if (!this.isCasting()) {
         super.initiateCastSpell(spell, spellLevel);
         BossSpellTuning.fasterLegacyCast(this, spell);
      }
   }

   protected void m_8097_() {
      super.m_8097_();
      this.f_19804_.m_135372_(DATA_ACTION, new CompoundTag());
      this.f_19804_.m_135372_(DATA_SIGNATURE_CAST, new CompoundTag());
   }

   public boolean m_7327_(Entity target) {
      this.meleeWindup.start(target);
      return false;
   }

   public BossActionState nativeAction() {
      return this.actionState;
   }

   public BossActionState signatureCastAction() {
      return this.signatureCast;
   }

   @Override
   public void startSignatureCast(Kind var1, int var2) {
      this.signatureCast.start(SayaCastSync.action(var1, var2), SayaCastSync.duration(var2));
   }

   private void startNativeSkill(String name, LivingEntity target) {
      this.nativeTarget = target.m_20148_();
      this.nativeAim = target.m_146892_();
      this.rootDirection = target.m_20182_().m_82546_(this.m_20182_()).m_82542_(1.0, 0.0, 1.0).m_82541_();
      this.nativePending = true;
      this.actionState.start(name, "roots".equals(name) ? 26 : 20);
   }

   private void tickNativeSkill() {
      this.meleeWindup.tick(x$0 -> super.m_7327_(x$0));
      if (this.nativePending && this.m_9236_() instanceof ServerLevel server) {
         if (!this.actionState.active()) {
            this.nativePending = false;
         } else {
            boolean roots = "roots".equals(this.actionState.name());
            int release = roots ? 12 : 8;
            if (this.actionState.elapsed(0.0F) < (float)release) {
               if (this.f_19797_ % 3 == 0) {
                  if (roots) {
                     for (int step = 2; step <= 16; step += 2) {
                        double pointX = this.m_20185_() + this.rootDirection.f_82479_ * (double)step;
                        double pointZ = this.m_20189_() + this.rootDirection.f_82481_ * (double)step;
                        double ground = (double)server.m_6924_(Types.MOTION_BLOCKING, Mth.m_14107_(pointX), Mth.m_14107_(pointZ));
                        server.m_8767_(ParticleTypes.f_123749_, pointX, ground + 0.1, pointZ, 1, 0.06, 0.02, 0.06, 0.0);
                     }
                  } else {
                     server.m_8767_(ParticleTypes.f_123763_, this.m_20185_(), this.m_20186_() + 1.4, this.m_20189_(), 3, 0.2, 0.12, 0.2, 0.01);
                  }
               }
            } else {
               this.nativePending = false;
               if (server.m_8791_(this.nativeTarget) instanceof LivingEntity target && BossTargets.valid(target)) {
                  if (roots) {
                     this.eruptionRoots(target);
                  } else {
                     Vec3 origin = this.m_146892_();
                     Vec3 delta = this.nativeAim.m_82546_(origin);

                     for (int step = 0; step <= 16; step++) {
                        Vec3 point = origin.m_82549_(delta.m_82490_((double)step / 16.0));
                        server.m_8767_(ParticleTypes.f_123763_, point.f_82479_, point.f_82480_, point.f_82481_, 1, 0.07, 0.07, 0.07, 0.01);
                     }

                     if (target.m_20191_().m_82400_(0.85).m_82390_(this.nativeAim) && this.m_142582_(target) && this.m_20280_(target) <= 256.0) {
                        this.spitTeaPoison(target);
                     }
                  }

                  return;
               }
            }
         }
      }
   }

   public boolean m_6051_() {
      return false;
   }

   public boolean isTeaMinion() {
      return this.getPersistentData().m_128471_("tea_minion");
   }

   public void setTeaMinion(boolean v) {
      this.getPersistentData().m_128379_("tea_minion", v);
   }

   public boolean isFinaleChallenge() {
      return this.getPersistentData().m_128471_("ftboss:saya_finale");
   }

   public void setFinaleChallenge(boolean finale) {
      this.getPersistentData().m_128379_("ftboss:saya_finale", finale);
      if (!this.m_9236_().f_46443_) {
         AttributeInstance health = this.m_21051_(Attributes.f_22276_);
         AttributeInstance attack = this.m_21051_(Attributes.f_22281_);
         if (health != null) {
            health.m_22100_(340.0 * (finale ? 2.0 : 1.0));
         }

         if (attack != null) {
            attack.m_22100_(3.5 * (finale ? 2.0 : 1.0));
         }

         this.m_21153_(this.m_21233_());
         this.updateBossBarName();
      }
   }

   private void updateBossBarName() {
      this.bossBar.m_6456_(Component.m_237115_("boss.ftboss.saya").m_7220_(this.isFinaleChallenge() ? Component.m_237113_(" §4[终结]") : Component.m_237119_()));
   }

   public TeaTreeBossEntity(EntityType<? extends TeaTreeBossEntity> type, Level level) {
      super(type, level);
      this.f_21364_ = 150;
      this.bossBar = new ServerBossEvent(Component.m_237115_("boss.ftboss.saya"), BossBarColor.GREEN, BossBarOverlay.PROGRESS);
      BossBars.track(this, this.bossBar);
   }

   public static Builder createAttributes() {
      return Mob.m_21552_()
         .m_22268_(Attributes.f_22276_, 340.0)
         .m_22268_(Attributes.f_22284_, 5.0)
         .m_22268_(Attributes.f_22281_, 3.5)
         .m_22268_(Attributes.f_22279_, 0.0)
         .m_22268_(Attributes.f_22278_, 0.85)
         .m_22268_(Attributes.f_22277_, 40.0);
   }

   protected void m_8099_() {
   }

   public boolean m_6469_(DamageSource source, float amount) {
      if (!this.isTeaMinion() && this.buds.shielded()) {
         this.buds.onBlocked(source);
         return false;
      } else {
         amount = BossDamageGate.gate(this, source, amount);
         if (amount <= 0.0F) {
            return false;
         } else {
            boolean hit = super.m_6469_(source, amount);
            if (hit) {
               BossDamageGate.onHit(this);
            }

            if (hit) {
               BossBarVisibility.visible(this.bossBar, true);
            }

            return hit;
         }
      }
   }

   public void m_8119_() {
      super.m_8119_();
      if (!this.m_9236_().f_46443_) {
         BossDamageGate.tick(this);
         BossRage.tickBoss(this);
         this.tickServer();
         if (!this.isTeaMinion()) {
            TrapBreaker.tick(this);
            this.buds.tick();
            this.tactics.tickMinionSummon();
            MinionBlessing.tick(this, 40.0);
         }
      }

      this.tickBossBar();
   }

   private void tickMusic(LivingEntity target) {
      if (this.m_6084_()) {
         if (this.m_9236_() instanceof ServerLevel sl) {
            this.musicStarted = true;
            BossMusic.request(sl, this);
         }
      }
   }

   private void stopMusic() {
      if (this.musicStarted) {
         this.musicStarted = false;
         if (this.m_9236_() instanceof ServerLevel sl) {
            BossMusic.release(sl, this);
         }
      }
   }

   private void tickServer() {
      if (!this.m_6084_()) {
         this.nativePending = false;
      } else {
         this.tickNativeSkill();
         if (this.f_19797_ % 20 == 0) {
            for (MobEffectInstance inst : new ArrayList(this.m_21220_())) {
               if (!inst.m_19544_().m_19486_()) {
                  this.m_21195_(inst.m_19544_());
               }
            }
         }

         this.tickTeaAura();
         this.tickBuffWheel();
         this.tickLifeGrowth();
         this.tactics.absorb();
         LivingEntity target = this.nearestTarget(20.0);
         if (target != null) {
            this.m_6710_(target);
            this.faceTarget(target);
            this.tickMusic(target);
            if (!this.phase2 && (double)(this.m_21223_() / this.m_21233_()) < 0.5) {
               this.phase2 = true;
               FTBossMod.LOGGER.info("[FTBoss] 茶树古树 茶心觉醒: 根刺与茶毒提速");
               if (this.m_9236_() instanceof ServerLevel sl) {
                  sl.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11736_, SoundSource.HOSTILE, 1.0F, 0.6F);
               }

               this.buds.trigger();
            }

            int fc = this.phase2 ? 62 : 100;
            int sc = this.phase2 ? 100 : 160;
            if (this.isFinaleChallenge()) {
               fc = (fc + 1) / 2;
               sc = (sc + 1) / 2;
            }

            double dist = (double)this.m_20270_(target);
            this.tickIronMagic(target);
            if (!this.actionState.active()) {
               this.fangCd--;
               if (this.fangCd <= 0) {
                  this.fangCd = fc;
                  if (dist < 16.0 && dist > 1.5) {
                     this.startNativeSkill("roots", target);
                     return;
                  }
               }

               this.spitCd--;
               if (this.spitCd <= 0) {
                  this.spitCd = sc;
                  if (dist < 16.0) {
                     this.startNativeSkill("poison", target);
                     return;
                  }
               }

               this.contactCd--;
               if (this.contactCd <= 0 && dist < 3.0) {
                  this.contactCd = this.isFinaleChallenge() ? 15 : 30;
                  this.m_7327_(target);
               }
            }
         }
      }
   }

   private void tickIronMagic(LivingEntity target) {
      if (!this.isCasting()) {
         this.castCd--;
         if (this.castCd <= 0) {
            int nextCast = this.phase2 ? 44 : 62;
            this.castCd = this.isFinaleChallenge() ? (nextCast + 1) / 2 : nextCast;
            if (this.castQueue.isEmpty()) {
               this.fillCastQueue();
            }

            if (!this.castQueue.isEmpty()) {
               this.tryCastSpell(target, this.castQueue.remove(0));
            }
         }
      }
   }

   private void fillCastQueue() {
      List<AbstractSpell> pool = WizardSummon.spellPool();
      if (!pool.isEmpty()) {
         if (BossRage.tierOf(this) >= 1 && this.f_19796_.m_188501_() < 0.8F) {
            pool = WizardSummon.spellPoolRare(8, 3);
         }

         int n = 2;
         List<AbstractSpell> tmp = new ArrayList<>(pool);

         while (!tmp.isEmpty() && this.castQueue.size() < n) {
            this.castQueue.add(tmp.remove(this.f_19796_.m_188503_(tmp.size())));
         }

         if (this.f_19796_.m_188501_() < 0.35F) {
            if (this.sayaSelfSpells == null) {
               this.sayaSelfSpells = WizardSummon.spellPoolOf(SAYA_SELF_SPELL_FIELDS);
            }

            if (!this.sayaSelfSpells.isEmpty()) {
               this.castQueue.add(this.sayaSelfSpells.get(this.f_19796_.m_188503_(this.sayaSelfSpells.size())));
            }
         }
      }
   }

   private void tryCastSpell(LivingEntity target, AbstractSpell s) {
      try {
         if (s == null) {
            return;
         }

         if (!BossSpellPacing.tryLegacyCast(this, s)) {
            return;
         }

         int lvl = Math.max(s.getMinLevel(), Math.min(6 + BossRage.tierOf(this), s.getMaxLevel()));

         try {
            Class<?> tdc = Class.forName("io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData");

            for (Constructor<?> ctor : tdc.getConstructors()) {
               if (ctor.getParameterCount() == 1 && ctor.getParameterTypes()[0].isInstance(target)) {
                  Object inst = ctor.newInstance(target);
                  Method set = MagicData.class.getMethod("setAdditionalCastData", ICastData.class);
                  Object md = MagicData.getPlayerMagicData(this);
                  set.invoke(md, inst);
                  break;
               }
            }
         } catch (Throwable var12) {
         }

         this.initiateCastSpell(s, lvl);
         FTBossMod.LOGGER.info("[FTBoss] 茶树古树施法: {} lv{} 目标={}", new Object[]{s.getSpellId(), lvl, target.m_7755_().getString()});
      } catch (Throwable var13) {
         FTBossMod.LOGGER.warn("[FTBoss] 茶树古树施法异常: {}", var13.toString());
      }
   }

   private void eruptionRoots(LivingEntity target) {
      if (this.m_9236_() instanceof ServerLevel sl) {
         Vec3 var20 = this.rootDirection;
         double baseX = this.m_20185_();
         double baseZ = this.m_20189_();
         double step = 1.6;
         int count = 9 + this.f_19796_.m_188503_(3);

         for (int i = 1; i <= count; i++) {
            double px = baseX + var20.f_82479_ * (double)i * step;
            double pz = baseZ + var20.f_82481_ * (double)i * step;
            BlockPos pos = BlockPos.m_274561_(px, this.m_20186_(), pz);
            double py = (double)this.m_9236_().m_6924_(Types.MOTION_BLOCKING, pos.m_123341_(), pos.m_123343_()) + 0.0;
            EvokerFangs fangs = new EvokerFangs(this.m_9236_(), px, py, pz, (float)Math.atan2(var20.f_82481_, var20.f_82479_), (i - 1) * 2, this);
            ManorFightMembers.inherit(this, fangs);
            sl.m_7967_(fangs);
         }

         sl.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_144209_, SoundSource.HOSTILE, 1.0F, 0.7F);
      }
   }

   private void spitTeaPoison(LivingEntity target) {
      target.m_7292_(new MobEffectInstance(MobEffects.f_19614_, 220, 2, false, false, true));
      target.m_6469_(this.m_269291_().m_269104_(this, this), this.isFinaleChallenge() ? 5.0F : 2.5F);
      if (this.m_9236_() instanceof ServerLevel sl) {
         sl.m_6263_(null, target.m_20185_(), target.m_20186_(), target.m_20189_(), SoundEvents.f_144088_, SoundSource.HOSTILE, 1.0F, 1.3F);
      }
   }

   private void tickBuffWheel() {
      if (--this.buffWheelCd <= 0) {
         this.buffWheelCd = 110 + this.f_19796_.m_188503_(60);
         List<MobEffect> pool = List.of(
            MobEffects.f_19596_,
            MobEffects.f_19600_,
            MobEffects.f_19598_,
            MobEffects.f_19606_,
            MobEffects.f_19605_,
            MobEffects.f_19617_,
            MobEffects.f_19618_,
            MobEffects.f_19607_,
            MobEffects.f_19621_,
            MobEffects.f_19603_,
            MobEffects.f_19608_
         );

         for (int i = 0; i < 2; i++) {
            MobEffect fx = pool.get(this.f_19796_.m_188503_(pool.size()));
            int dur = 100 + this.f_19796_.m_188503_(120);
            int amp = this.f_19796_.m_188503_(3);
            this.m_7292_(new MobEffectInstance(fx, dur, amp, false, false, true));
         }
      }
   }

   private void tickLifeGrowth() {
      if (this.hpBoostLevel < 10) {
         if (this.f_19797_ > 0 && this.f_19797_ % 1200 == 0) {
            if (this.nearestTarget(48.0) != null) {
               this.hpBoostLevel++;
               this.m_7292_(new MobEffectInstance(MobEffects.f_19616_, 12000, this.hpBoostLevel - 1, false, false, true));
               this.m_5634_(4.0F);
               if (this.m_9236_() instanceof ServerLevel sl) {
                  sl.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11739_, SoundSource.HOSTILE, 1.0F, 0.8F);
                  Fx.particles(sl, this.m_20185_(), this.m_20186_() + 1.5, this.m_20189_(), 30, ParticleTypes.f_123748_, 1.6, 0.08);

                  for (ServerPlayer p : sl.m_6907_()) {
                     if (p.m_20280_(this) <= 2304.0) {
                        Component.m_237113_("§2沙耶 §7汲取茶心: §c生命提升 " + this.hpBoostLevel + " 级 §8(最大生命 +" + (int)(4.0F * (float)this.hpBoostLevel) + ")");
                        boolean var10002 = true;
                     }
                  }
               }

               FTBossMod.LOGGER.info("[FTBoss] 茶树古树 生命提升 -> {} 级 (+{} 最大生命)", this.hpBoostLevel, (int)(4.0F * (float)this.hpBoostLevel));
            }
         }
      }
   }

   private void tickTeaAura() {
      if (++this.auraTimer >= 10) {
         this.auraTimer = 0;
         if (this.teaRenewal == null) {
            this.teaRenewal = (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("herbalbrews", "renewal"));
         }

         if (this.teaRenewal != null) {
            MobEffectInstance cur = this.m_21124_(this.teaRenewal);
            if (cur == null || cur.m_19557_() < 120) {
               this.m_7292_(new MobEffectInstance(this.teaRenewal, 600, 1, false, false, true));
            }
         }
      }
   }

   private LivingEntity nearestTarget(double range) {
      return BossTargets.combatTarget(this, range);
   }

   private void faceTarget(LivingEntity target) {
      double dx = target.m_20185_() - this.m_20185_();
      double dz = target.m_20189_() - this.m_20189_();
      if (!(dx * dx + dz * dz < 1.0E-4)) {
         this.m_146922_((float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0));
         this.m_5618_(this.m_146908_());
         this.m_5616_(this.m_146908_());
      }
   }

   public boolean tryRevive() {
      if (!this.secondLifeUsed && this.m_6084_()) {
         this.secondLifeUsed = true;
         this.m_21153_((float)((double)this.m_21233_() * 0.3));
         this.m_21219_();
         this.fangCd = 40;
         this.spitCd = 70;
         this.buds.reset();
         this.buds.trigger();
         if (this.m_9236_() instanceof ServerLevel sl) {
            sl.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_12513_, SoundSource.HOSTILE, 1.0F, 1.0F);
         }

         FTBossMod.LOGGER.info("[FTBoss] 茶树古树 茶根再生, 进入第二阶段");
         return true;
      } else {
         return false;
      }
   }

   public void dropRewards() {
      this.buds.reset();
      SummonSweep.clear(this, 80.0, new Class[]{TeaMinionEntity.class});
      this.stopMusic();
      if (LemonSummonRewards.suppressed(this)) {
         FTBossMod.LOGGER.info("[FTBoss][Lemon] summoned Tea Tree defeated: rewards suppressed");
      } else if (this.m_9236_() instanceof ServerLevel sl) {
         double var9 = this.m_20185_();
         double y = this.m_20186_();
         double z = this.m_20189_();
         NonManorBossLootRouter.spawn(sl, new ItemEntity(sl, var9, y, z, new ItemStack(Items.f_41896_, 24)), this);
         NonManorBossLootRouter.spawn(sl, new ItemEntity(sl, var9, y + 0.4, z, new ItemStack(Items.f_42799_, 8)), this);
         NonManorBossLootRouter.spawn(sl, new ItemEntity(sl, var9, y + 0.8, z, new ItemStack((ItemLike)FTBossItems.BLOOMING_THORNS.get())), this);
         NonManorBossLootRouter.spawn(sl, new ItemEntity(sl, var9, y + 1.0, z, new ItemStack((ItemLike)FTBossItems.THORN_CROWN.get())), this);
         NonManorBossLootRouter.spawn(sl, new ItemEntity(sl, var9 + 0.25, y + 1.0, z, new ItemStack((ItemLike)FTBossItems.THORN_CHESTPLATE.get())), this);
         NonManorBossLootRouter.spawn(sl, new ItemEntity(sl, var9 - 0.25, y + 1.0, z, new ItemStack((ItemLike)FTBossItems.THORN_LEGGINGS.get())), this);
         NonManorBossLootRouter.spawn(sl, new ItemEntity(sl, var9, y + 1.0, z + 0.25, new ItemStack((ItemLike)FTBossItems.THORN_BOOTS.get())), this);
         boolean silverKey = this.isFinaleChallenge() && this.m_21232_() instanceof ServerPlayer;
         if (silverKey) {
            NonManorBossLootRouter.spawn(sl, new ItemEntity(sl, var9, y + 1.35, z, new ItemStack((ItemLike)FTBossItems.SILVER_KEY.get())), this);
         }

         NonManorBossLootRouter.spawn(sl, new ExperienceOrb(sl, var9, y, z, this.isFinaleChallenge() ? 300 : 150), this);
         FTBossMod.LOGGER.info("[FTBoss] 沙耶被击败：荆棘全套已掉落，终结难度银之钥={}", silverKey);
      }
   }

   private void tickBossBar() {
      if (!this.m_6084_()) {
         BossBars.untrack(this, this.bossBar);
      } else if (this.isTeaMinion()) {
         this.hideBossBar();
      } else {
         this.updateBossBarName();
         this.bossBar.m_142711_(this.m_21223_() / this.m_21233_());
         boolean want = false;

         for (ServerPlayer p : this.m_9236_().m_45976_(ServerPlayer.class, this.m_20191_().m_82400_(48.0))) {
            if (this.m_20280_(p) < 2304.0) {
               BossBarVisibility.addPlayer(this.bossBar, p);
               want = true;
            } else {
               this.bossBar.m_6539_(p);
            }
         }

         if (this.bossBar.m_8323_() != want) {
            BossBarVisibility.visible(this.bossBar, want);
         }
      }
   }

   public void m_142687_(RemovalReason reason) {
      if (!this.m_9236_().f_46443_) {
         this.buds.reset();
         SummonSweep.clear(this, 80.0, new Class[]{TeaMinionEntity.class});
      }

      BossBars.untrack(this, this.bossBar);
      this.stopMusic();
      super.m_142687_(reason);
   }

   public void hideBossBar() {
      BossBarVisibility.visible(this.bossBar, false);
      this.bossBar.m_7706_();
   }

   public ServerBossEvent getBossBar() {
      return this.bossBar;
   }

   protected boolean m_21527_() {
      return false;
   }
}
