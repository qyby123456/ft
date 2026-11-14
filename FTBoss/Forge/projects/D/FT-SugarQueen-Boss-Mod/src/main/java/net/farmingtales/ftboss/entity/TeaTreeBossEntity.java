package net.farmingtales.ftboss.entity;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.BossBars;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

/**
 * 茶树古树 Boss「沙耶」(实体完善版):
 * - 扎根原地不移动(空目标AI), 白天不燃烧, 免负面效果
 * - 攻击: 根刺(唤魔尖牙)周期性爆发 + 茶毒飞沫(中毒+伤害) + 近身轻推 + 铁魔法连发
 * - 半血"茶心觉醒": 攻速提升 + 召唤守护茶芽(茶芽庇佑, 清空前免伤); 首次死亡触发 图腾二命(回30%血)
 * - 常驻茶buff: herbalbrews:renewal(焕新)+伤害吸收; 绿色Boss条
 * 外观: 原版 Alex 人形 + {@code client.model.SayaPlayerModel} 手写的铁魔法施法姿态(沙耶不是玩家, 用不了 playeranimator);
 * 她的"施法动作"由该模型按 MagicData 的 CastType/法术 id 区分为六种姿势, 见 SayaPlayerModel。
 */
public class TeaTreeBossEntity extends AbstractSpellCastingMob implements net.farmingtales.ftboss.integ.spells.RegularBossCastHost {
    @Override
    public void initiateCastSpell(AbstractSpell spell, int spellLevel) {
        if (this.isCasting()) return;
        super.initiateCastSpell(spell, spellLevel);
        net.farmingtales.ftboss.integ.spells.BossSpellTuning.fasterLegacyCast(this, spell);
    }

    private static final net.minecraft.network.syncher.EntityDataAccessor<net.minecraft.nbt.CompoundTag> DATA_ACTION =
            net.minecraft.network.syncher.SynchedEntityData.defineId(TeaTreeBossEntity.class,
                    net.minecraft.network.syncher.EntityDataSerializers.COMPOUND_TAG);
    private final net.farmingtales.ftboss.boss.ai.BossActionState actionState =
            new net.farmingtales.ftboss.boss.ai.BossActionState(this, DATA_ACTION);
    private static final net.minecraft.network.syncher.EntityDataAccessor<net.minecraft.nbt.CompoundTag> DATA_SIGNATURE_CAST =
            net.minecraft.network.syncher.SynchedEntityData.defineId(TeaTreeBossEntity.class,
                    net.minecraft.network.syncher.EntityDataSerializers.COMPOUND_TAG);
    private final net.farmingtales.ftboss.boss.ai.BossActionState signatureCast =
            new net.farmingtales.ftboss.boss.ai.BossActionState(this, DATA_SIGNATURE_CAST);
    private final net.farmingtales.ftboss.boss.ai.BossMeleeWindup meleeWindup =
            new net.farmingtales.ftboss.boss.ai.BossMeleeWindup(this, this.actionState, 6, 14, 3.0);
    private java.util.UUID nativeTarget;
    private Vec3 nativeAim = Vec3.ZERO;
    private Vec3 rootDirection = Vec3.ZERO;
    private boolean nativePending;

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ACTION, new net.minecraft.nbt.CompoundTag());
        this.entityData.define(DATA_SIGNATURE_CAST, new net.minecraft.nbt.CompoundTag());
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        this.meleeWindup.start(target);
        return false;
    }

    public net.farmingtales.ftboss.boss.ai.BossActionState nativeAction() {
        return this.actionState;
    }

    public net.farmingtales.ftboss.boss.ai.BossActionState signatureCastAction() {
        return this.signatureCast;
    }

    @Override
    public void startSignatureCast(net.farmingtales.ftboss.util.BossSpellRules.Kind kind, int castTicks) {
        this.signatureCast.start(net.farmingtales.ftboss.integ.spells.RegularBossCastHost.action(kind),
                net.farmingtales.ftboss.integ.spells.RegularBossCastHost.visualTicks(castTicks));
    }

    private void startNativeSkill(String name, LivingEntity target) {
        this.nativeTarget = target.getUUID();
        this.nativeAim = target.getEyePosition();
        this.rootDirection = target.position().subtract(this.position()).multiply(1, 0, 1).normalize();
        this.nativePending = true;
        this.actionState.start(name, "roots".equals(name) ? 26 : 20);
    }

    private void tickNativeSkill() {
        this.meleeWindup.tick(super::doHurtTarget);
        if (!this.nativePending || !(this.level() instanceof ServerLevel server)) return;
        if (!this.actionState.active()) { this.nativePending = false; return; }
        boolean roots = "roots".equals(this.actionState.name());
        int release = roots ? 12 : 8;
        if (this.actionState.elapsed(0) < release) {
            if (this.tickCount % 3 == 0) {
                if (roots) {
                    for (int step = 2; step <= 16; step += 2) {
                        double pointX = this.getX() + this.rootDirection.x * step;
                        double pointZ = this.getZ() + this.rootDirection.z * step;
                        double ground = server.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,
                                net.minecraft.util.Mth.floor(pointX), net.minecraft.util.Mth.floor(pointZ));
                        server.sendParticles(net.minecraft.core.particles.ParticleTypes.COMPOSTER,
                                pointX, ground + 0.1, pointZ, 1, 0.06, 0.02, 0.06, 0);
                    }
                } else {
                    server.sendParticles(net.minecraft.core.particles.ParticleTypes.SNEEZE,
                            this.getX(), this.getY() + 1.4, this.getZ(), 3, 0.2, 0.12, 0.2, 0.01);
                }
            }
            return;
        }
        this.nativePending = false;
        if (!(server.getEntity(this.nativeTarget) instanceof LivingEntity target)
                || !net.farmingtales.ftboss.boss.ai.BossTargets.valid(target)) return;
        if (roots) eruptionRoots(target);
        else {
            Vec3 origin = this.getEyePosition();
            Vec3 delta = this.nativeAim.subtract(origin);
            for (int step = 0; step <= 16; step++) {
                Vec3 point = origin.add(delta.scale(step / 16.0));
                server.sendParticles(net.minecraft.core.particles.ParticleTypes.SNEEZE,
                        point.x, point.y, point.z, 1, 0.07, 0.07, 0.07, 0.01);
            }
            if (target.getBoundingBox().inflate(0.85).contains(this.nativeAim) && this.hasLineOfSight(target)
                    && this.distanceToSqr(target) <= 16 * 16) spitTeaPoison(target);
        }
    }

    /**
     * 2026-09-11 按用户要求"去除 Boss 燃烧动画避免阻碍观察":
     * 着火时不再往模型上叠火焰图层(火焰伤害照旧), 免得模型/动画被挡住看不清。
     */
    @Override
    public boolean displayFireAnimation() {
        return false;
    }
    private static final double BOSS_BAR_RANGE = 48.0;
    /** 2026-09-10 加强沙耶: 330 → 420 → 500; 2026-09-11 按用户要求血量对半: 500 → 250 */
    private static final double MAX_HP = 680.0;
    private static final String FINALE_TAG = "ftboss:saya_finale";
    private static final double FINALE_HEALTH_MULTIPLIER = 2.0;
    private static final double FINALE_ATTACK_MULTIPLIER = 2.0;
    /** 2026-09-10 加强沙耶: 根刺 150/95 → 110/70 → 100/62 */
    private static final int FANG_CD = 100;
    private static final int SPIT_CD = 160;
    private static final int FANG_CD_P2 = 62;
    private static final int SPIT_CD_P2 = 100;
    /* ===== 生命提升(2026-09-12 按用户要求): 每 60 秒 +1 级, 每级 +4 最大生命 ===== */
    /** 60 秒 = 1200 tick */
    private static final int HP_BOOST_INTERVAL = 1200;
    /** 最高 10 级(+40 最大生命) */
    private static final int HP_BOOST_MAX = 10;
    /** 原版 HEALTH_BOOST 每级 +4 点最大生命(2 颗心) */
    private static final float HP_BOOST_PER_LEVEL = 4.0f;

    private final ServerBossEvent bossBar;
    private MobEffect teaRenewal;
    private int auraTimer = 0;
    private int fangCd = FANG_CD;
    private int spitCd = SPIT_CD;
    private int contactCd = 0;
    private int castCd = 80;
    private int buffWheelCd = 60;
    private static final String MINION_TAG = "tea_minion";
    /** 二阶段「茶芽庇佑」: 守护茶芽未清空时沙耶免疫伤害 */
    private final net.farmingtales.ftboss.boss.ai.SayaBuds buds =
            new net.farmingtales.ftboss.boss.ai.SayaBuds(this);
    private boolean musicStarted = false;

    /** 召唤/汲取战术组件(见 boss.ai.TeaTactics) */
    private final net.farmingtales.ftboss.boss.ai.TeaTactics tactics =
            new net.farmingtales.ftboss.boss.ai.TeaTactics(this);

    public boolean isTeaMinion() {
        return this.getPersistentData().getBoolean(MINION_TAG);
    }

    public void setTeaMinion(boolean v) {
        this.getPersistentData().putBoolean(MINION_TAG, v);
    }

    public boolean isFinaleChallenge() {
        return this.getPersistentData().getBoolean(FINALE_TAG);
    }

    public void setFinaleChallenge(boolean finale) {
        this.getPersistentData().putBoolean(FINALE_TAG, finale);
        if (this.level().isClientSide) return;
        var health = this.getAttribute(Attributes.MAX_HEALTH);
        var attack = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (health != null) health.setBaseValue(MAX_HP * (finale ? FINALE_HEALTH_MULTIPLIER : 1.0));
        if (attack != null) attack.setBaseValue(3.5 * (finale ? FINALE_ATTACK_MULTIPLIER : 1.0));
        this.setHealth(this.getMaxHealth());
        this.updateBossBarName();
    }

    private void updateBossBarName() {
        this.bossBar.setName(Component.translatable("boss.ftboss.saya")
                .append(this.isFinaleChallenge() ? Component.literal(" §4[终结]") : Component.empty()));
    }
    private final List<AbstractSpell> castQueue = new ArrayList<>();
    private boolean phase2 = false;
    private boolean secondLifeUsed = false;
    /** 生命提升已叠加的级数(每 60 秒 +1, 上限 HP_BOOST_MAX) */
    private int hpBoostLevel = 0;

    public TeaTreeBossEntity(EntityType<? extends TeaTreeBossEntity> type, Level level) {
        super(type, level);
        this.xpReward = 150;
        this.bossBar = new ServerBossEvent(
                Component.translatable("boss.ftboss.saya"), BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.PROGRESS);
        BossBars.track(this, this.bossBar);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HP)
                .add(Attributes.ARMOR, 5.0)          // 6 → 10(加强沙耶)
                .add(Attributes.ATTACK_DAMAGE, 3.5)   // v110 修正注释: 实际就是 3.5(贴身轻推); 旧注释"5 → 7"是改数值前留下的
                .add(Attributes.MOVEMENT_SPEED, 0.0) // 扎根
                .add(Attributes.KNOCKBACK_RESISTANCE, net.farmingtales.ftboss.util.BossKnockback.BOSS_RESISTANCE)   // 2026-09-19: 允许被击退
                .add(Attributes.FOLLOW_RANGE, 40.0);
    }

    /** 扎根: 不添加任何原版寻路/攻击AI, 由本类 tick 驱动战斗 */
    @Override
    protected void registerGoals() {
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // 二阶段「茶芽庇佑」: 守护茶芽全部清空前完全免伤
        if (!this.isTeaMinion() && this.buds.shielded()) {
            this.buds.onBlocked(source);
            return false;
        }
        amount = net.farmingtales.ftboss.boss.ai.BossDamageGate.gate(this, source, amount);
        if (amount <= 0F) return false;
        boolean hit = super.hurt(source, amount);
        if (hit) net.farmingtales.ftboss.boss.ai.BossDamageGate.onHit(this);
        if (hit) this.bossBar.setVisible(true);
        return hit;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            net.farmingtales.ftboss.boss.ai.BossDamageGate.tick(this);
            net.farmingtales.ftboss.boss.ai.BossRage.tickBoss(this);
            tickServer();
            if (!this.isTeaMinion()) {
                net.farmingtales.ftboss.boss.ai.TrapBreaker.tick(this);
                this.buds.tick();
                this.tactics.tickMinionSummon();
                net.farmingtales.ftboss.boss.ai.MinionBlessing.tick(this, 40);
            }
        }
        tickBossBar();
    }

    // 茶芽小妖召唤与「茶心汲取」已移至 boss.ai.TeaTactics(见 this.tactics)

    /** BGM(B方案): 开战后每 tick 向全局 BGM 调度器登记(多个 Boss 只播一份; 调度器按血量推进三幕剧乐幕) */
    private void tickMusic(LivingEntity target) {
        if (!net.farmingtales.ftboss.sound.FTSounds.MUSIC_ENABLED) return;
        if (!this.isAlive()) return;   // 死亡动画期间 tick 仍在跑, 不能再请求音乐
        if (!(this.level() instanceof ServerLevel sl)) return;
        this.musicStarted = true;
        net.farmingtales.ftboss.sound.BossMusic.request(sl, this);
    }

    /** 停止BGM: 真死/被移除时调用, 全部 Boss 都退场后音乐才停 */
    private void stopMusic() {
        if (!this.musicStarted) return;
        this.musicStarted = false;
        if (this.level() instanceof ServerLevel sl) {
            net.farmingtales.ftboss.sound.BossMusic.release(sl, this);
        }
    }

    private void tickServer() {
        if (!this.isAlive()) { this.nativePending = false; return; }
        tickNativeSkill();
        if (this.tickCount % 20 == 0) {
            for (MobEffectInstance inst : new ArrayList<>(this.getActiveEffects())) {
                if (!inst.getEffect().isBeneficial()) this.removeEffect(inst.getEffect());
            }
        }
        tickTeaAura();
        tickBuffWheel();
        tickLifeGrowth();
        this.tactics.absorb();
        LivingEntity target = nearestTarget(20.0);
        if (target == null) return;
        this.setTarget(target);
        faceTarget(target);
        tickMusic(target);

        // 半血 茶心觉醒
        if (!this.phase2 && this.getHealth() / this.getMaxHealth() < 0.5) {
            this.phase2 = true;
            FTBossMod.LOGGER.info("[FTBoss] 茶树古树 茶心觉醒: 根刺与茶毒提速");
            if (this.level() instanceof ServerLevel sl) {
                sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 1.0f, 0.6f);
            }
            // 二阶段: 召唤守护茶芽, 开启免伤护盾(清空前沙耶不会受伤)
            this.buds.trigger();
        }
        int fc = this.phase2 ? FANG_CD_P2 : FANG_CD;
        int sc = this.phase2 ? SPIT_CD_P2 : SPIT_CD;
        if (this.isFinaleChallenge()) {
            fc = (fc + 1) / 2;
            sc = (sc + 1) / 2;
        }
        double dist = this.distanceTo(target);
        tickIronMagic(target);
        if (this.actionState.active()) return;

        // 1) 根刺爆发(范围 3..16)
        this.fangCd--;
        if (this.fangCd <= 0) {
            this.fangCd = fc;
            if (dist < 16.0 && dist > 1.5) {
                startNativeSkill("roots", target);
                return;
            }
        }
        // 2) 茶毒飞沫(16格内, 中毒II+伤害)
        this.spitCd--;
        if (this.spitCd <= 0) {
            this.spitCd = sc;
            if (dist < 16.0) {
                startNativeSkill("poison", target);
                return;
            }
        }
        // 4) 贴身轻推
        this.contactCd--;
        if (this.contactCd <= 0 && dist < 3.0) {
            this.contactCd = this.isFinaleChallenge() ? 15 : 30;
            this.doHurtTarget(target);
        }
    }

    private void tickIronMagic(LivingEntity target) {
        if (this.isCasting()) return;
        this.castCd--;
        if (this.castCd <= 0) {
            // 2026-09-10 深夜按用户要求: "减少法术量 + 用更高级的魔法" —— 间隔 46/30 → 62/44, 等级 5→6 起步
            int nextCast = this.phase2 ? 44 : 62;
            this.castCd = this.isFinaleChallenge() ? (nextCast + 1) / 2 : nextCast;
            if (this.castQueue.isEmpty()) {
                fillCastQueue();
            }
            if (!this.castQueue.isEmpty()) {
                tryCastSpell(target, this.castQueue.remove(0));
            }
        }
    }

    /**
     * 沙耶专属「自施法」法术(不在共用进攻池里): 树皮护体 / 伤害吸收。
     * 加它们的目的是让 {@code SayaPlayerModel} 的"收手护身"施法姿势真的会出现,
     * 而不是五种姿势里有两种永远看不到; 反射取不到自动跳过(不装铁魔法也不会炸)。
     */
    private static final String[] SAYA_SELF_SPELL_FIELDS = { "OAKSKIN_SPELL", "ABSORPTION_SPELL" };
    private List<AbstractSpell> sayaSelfSpells;

    /** 2~3个不同法术组队, 另有 35% 几率掺一个沙耶专属自施法法术(收手护身姿势); 狂暴时更倾向史诗/传说 */
    private void fillCastQueue() {
        List<AbstractSpell> pool = net.farmingtales.ftboss.integ.WizardSummon.spellPool();
        if (pool.isEmpty()) return;
        // 2026-09-10 深夜按用户要求: 狂暴时 80% 概率改成从"史诗+传说"里挑(用更高级的魔法)
        if (net.farmingtales.ftboss.boss.ai.BossRage.tierOf(this) >= 1 && this.random.nextFloat() < 0.8f) {
            pool = net.farmingtales.ftboss.integ.WizardSummon.spellPoolRare(8, 3);   // 3=EPIC 4=LEGENDARY
        }
        int n = 2;   // 2026-09-10: 每次 2 个(原 2~3), 配合"减少法术量"
        List<AbstractSpell> tmp = new ArrayList<>(pool);
        while (!tmp.isEmpty() && this.castQueue.size() < n) {
            this.castQueue.add(tmp.remove(this.random.nextInt(tmp.size())));
        }
        if (this.random.nextFloat() < 0.35f) {
            if (this.sayaSelfSpells == null) {
                this.sayaSelfSpells = net.farmingtales.ftboss.integ.WizardSummon.spellPoolOf(SAYA_SELF_SPELL_FIELDS);
            }
            if (!this.sayaSelfSpells.isEmpty()) {
                this.castQueue.add(this.sayaSelfSpells.get(this.random.nextInt(this.sayaSelfSpells.size())));
            }
        }
    }

    /** 铁魔法施法(参考女王链路): 反射绑定目标 + initiateCastSpell */
    private void tryCastSpell(LivingEntity target, AbstractSpell s) {
        try {
            if (s == null) return;
            if (!net.farmingtales.ftboss.integ.spells.BossSpellPacing.tryLegacyCast(this, s)) return;
            // 狂暴等级越高法术等级越高: 6 -> 7 -> 8 (2026-09-10 深夜: 法术更少但更高级)
            int lvl = Math.max(s.getMinLevel(),
                    Math.min(6 + net.farmingtales.ftboss.boss.ai.BossRage.tierOf(this), s.getMaxLevel()));
            try {
                Class<?> tdc = Class.forName("io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData");
                for (var ctor : tdc.getConstructors()) {
                    if (ctor.getParameterCount() == 1
                            && ctor.getParameterTypes()[0].isInstance(target)) {
                        Object inst = ctor.newInstance(target);
                        java.lang.reflect.Method set = io.redspace.ironsspellbooks.api.magic.MagicData.class
                                .getMethod("setAdditionalCastData", io.redspace.ironsspellbooks.api.spells.ICastData.class);
                        Object md = io.redspace.ironsspellbooks.api.magic.MagicData
                                .getPlayerMagicData((net.minecraft.world.entity.LivingEntity) this);
                        set.invoke(md, inst);
                        break;
                    }
                }
            } catch (Throwable ignored) {
            }
            ((io.redspace.ironsspellbooks.api.entity.IMagicEntity) this).initiateCastSpell(s, lvl);
            FTBossMod.LOGGER.info("[FTBoss] 茶树古树施法: {} lv{} 目标={}", s.getSpellId(), lvl,
                    target.getName().getString());
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss] 茶树古树施法异常: {}", t.toString());
        }
    }

    /** 唤魔尖牙模拟树根破土(沿目标方向一波3-5根) */
    private void eruptionRoots(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel sl)) return;
        Vec3 dir = this.rootDirection;
        double baseX = this.getX(), baseZ = this.getZ();
        double step = 1.6;
        int count = 9 + this.random.nextInt(3);   // 加强沙耶: 4~5 → 6~8 → 9~11 根
        for (int i = 1; i <= count; i++) {
            double px = baseX + dir.x * i * step;
            double pz = baseZ + dir.z * i * step;
            BlockPos pos = BlockPos.containing(px, this.getY(), pz);
            double py = this.level().getHeight(
                    net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ()) + 0.0;
            EvokerFangs fangs = new EvokerFangs(this.level(), px, py, pz,
                    (float) Math.atan2(dir.z, dir.x), (i - 1) * 2, this);
            net.farmingtales.ftboss.boss.ManorFightMembers.inherit(this, fangs);
            sl.addFreshEntity(fangs);
        }
        sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ROOTED_DIRT_PLACE, SoundSource.HOSTILE, 1.0f, 0.7f);
    }

    /** 茶毒飞沫: 直接施加中毒+一次魔法伤害(占位弹道, 模型接入后再换实体弹体) */
    private void spitTeaPoison(LivingEntity target) {
        // 加强沙耶: 中毒 III/220t, 魔法伤害 1.5 → 3.0 → 5.0(按"提高伤害")
        target.addEffect(new MobEffectInstance(MobEffects.POISON, 220, 2, false, false, true));
        target.hurt(this.damageSources().indirectMagic(this, this),
                this.isFinaleChallenge() ? 5.0F : 2.5F);   // 终结难度伤害翻倍
        if (this.level() instanceof ServerLevel sl) {
            sl.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.CAVE_VINES_PICK_BERRIES, SoundSource.HOSTILE, 1.0f, 1.3f);
        }
    }

    /** 原版buff短时轮换: 每次随机 2 个(2026-09-10 深夜按用户要求"减少一点buff数量": 4 → 2 个, 间隔也拉长) */
    private void tickBuffWheel() {
        if (--this.buffWheelCd > 0) return;
        this.buffWheelCd = 110 + this.random.nextInt(60);
        java.util.List<MobEffect> pool = java.util.List.of(
                MobEffects.MOVEMENT_SPEED, MobEffects.DAMAGE_BOOST, MobEffects.DIG_SPEED,
                MobEffects.DAMAGE_RESISTANCE, MobEffects.REGENERATION, MobEffects.ABSORPTION,
                MobEffects.SATURATION, MobEffects.FIRE_RESISTANCE, MobEffects.LUCK,
                MobEffects.JUMP, MobEffects.WATER_BREATHING);
        /* 2026-09-12: HEALTH_BOOST 从轮盘里摘掉 —— 它现在归「生命提升」成长机制(每 60 秒 +1 级)专管,
           否则轮盘的随机短时 0~2 级会和成长等级互相打架 */
        for (int i = 0; i < 2; i++) {
            MobEffect fx = pool.get(this.random.nextInt(pool.size()));
            int dur = 100 + this.random.nextInt(120);   // 短时
            int amp = this.random.nextInt(3);           // 0-2级
            this.addEffect(new MobEffectInstance(fx, dur, amp, false, false, true));
        }
    }

    /**
     * 【生命提升】成长(2026-09-12 按用户要求): 每 <b>60 秒</b>叠 1 级原版 {@code HEALTH_BOOST}
     * (每级 +4 最大生命), 上限 {@value #HP_BOOST_MAX} 级(+40)。
     *
     * <p>三条设计细节:
     * <ul>
     *   <li>只在<b>附近有玩家</b>时成长(48 格内) —— 免得沙耶在无人区自己偷偷长大;</li>
     *   <li>升级时同步 {@code heal(4)} —— 上限涨了但当前血不涨的话, 血条会突然掉一截, 看着像被削;</li>
     *   <li>持续时间给 600 秒并被每分钟刷新 —— HEALTH_BOOST 必须"常驻", 不能到期掉级。</li>
     * </ul>
     */
    private void tickLifeGrowth() {
        if (this.hpBoostLevel >= HP_BOOST_MAX) return;
        if (this.tickCount <= 0 || this.tickCount % HP_BOOST_INTERVAL != 0) return;
        if (this.nearestTarget(48.0) == null) return;      // 附近没人就不成长
        this.hpBoostLevel++;
        this.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, 20 * 600,
                this.hpBoostLevel - 1, false, false, true));
        this.heal(HP_BOOST_PER_LEVEL);
        if (this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.BEACON_POWER_SELECT, SoundSource.HOSTILE, 1.0F, 0.8F);
            net.farmingtales.ftboss.util.Fx.particles(sl, this.getX(), this.getY() + 1.5, this.getZ(), 30,
                    net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER, 1.6, 0.08);
            for (ServerPlayer p : sl.players()) {
                if (p.distanceToSqr(this) <= 48.0 * 48.0) {
                    p.displayClientMessage(Component.literal("§2沙耶 §7汲取茶心: §c生命提升 "
                            + this.hpBoostLevel + " 级 §8(最大生命 +"
                            + (int) (HP_BOOST_PER_LEVEL * this.hpBoostLevel) + ")"), true);
                }
            }
        }
        FTBossMod.LOGGER.info("[FTBoss] 茶树古树 生命提升 -> {} 级 (+{} 最大生命)",
                this.hpBoostLevel, (int) (HP_BOOST_PER_LEVEL * this.hpBoostLevel));
    }

    private void tickTeaAura() {
        if (++this.auraTimer < 10) return;
        this.auraTimer = 0;
        if (this.teaRenewal == null) this.teaRenewal = ForgeRegistries.MOB_EFFECTS.getValue(
                new net.minecraft.resources.ResourceLocation("herbalbrews", "renewal"));
        if (this.teaRenewal != null) {
            MobEffectInstance cur = this.getEffect(this.teaRenewal);
            if (cur == null || cur.getDuration() < 120) {
                this.addEffect(new MobEffectInstance(this.teaRenewal, 600, 1, false, false, true));
            }
        }
        // 2026-09-10 深夜按用户要求"减少一点buff数量": 常驻吸收光环去掉(只留焕新)
    }

    private LivingEntity nearestTarget(double range) {
        return net.farmingtales.ftboss.boss.ai.BossTargets.combatTarget(this, range);
    }

    private void faceTarget(LivingEntity target) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        if (dx * dx + dz * dz < 0.0001) return;
        this.setYRot((float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0));
        this.setYBodyRot(this.getYRot());
        this.setYHeadRot(this.getYRot());
    }

    /** 图腾二命(由 BossHandler 死亡事件调用) */
    public boolean tryRevive() {
        if (this.secondLifeUsed || !this.isAlive()) return false;
        this.secondLifeUsed = true;
        this.setHealth((float) (this.getMaxHealth() * 0.3));
        this.removeAllEffects();
        this.fangCd = 40;
        this.spitCd = 70;
        this.buds.reset();
        this.buds.trigger();   // 二命重生: 再来一次「茶芽庇佑」
        if (this.level() instanceof ServerLevel sl) {
            sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.TOTEM_USE, SoundSource.HOSTILE, 1.0f, 1.0f);
        }
        FTBossMod.LOGGER.info("[FTBoss] 茶树古树 茶根再生, 进入第二阶段");
        return true;
    }

    /** 死亡掉落(由 BossHandler 死亡事件调用) */
    public void dropRewards() {
        this.buds.reset();   // 死亡: 移除守护茶芽
        /* 2026-09-12 v58 按用户反馈「召唤物死不干净」: 茶芽小妖是 setPersistenceRequired 的,
           以前主人死了它们会一直在场上打人 —— 现在随主消散(类型白名单, 不误伤别的 Boss 的小弟) */
        net.farmingtales.ftboss.boss.ai.SummonSweep.clear(this,
                net.farmingtales.ftboss.boss.ai.SummonSweep.RANGE, TeaMinionEntity.class);
        this.stopMusic();
        if (net.farmingtales.ftboss.boss.ai.LemonSummonRewards.suppressed(this)) {
            FTBossMod.LOGGER.info("[FTBoss][Lemon] summoned Tea Tree defeated: rewards suppressed");
            return;
        }
        if (!(this.level() instanceof ServerLevel sl)) return;
        double x = this.getX(), y = this.getY(), z = this.getZ();
        net.farmingtales.ftboss.boss.ai.NonManorBossLootRouter.spawn(sl, new ItemEntity(sl, x, y, z, new ItemStack(Items.OAK_LEAVES, 24)), this);
        net.farmingtales.ftboss.boss.ai.NonManorBossLootRouter.spawn(sl, new ItemEntity(sl, x, y + 0.4, z, new ItemStack(Items.OAK_SAPLING, 8)), this);
        /* 2026-09-12 v57 按用户要求: 掉落武器【绽放的荆棘】(用户提供模型的 3D 剑) */
        net.farmingtales.ftboss.boss.ai.NonManorBossLootRouter.spawn(sl, new ItemEntity(sl, x, y + 0.8, z,
                new ItemStack(net.farmingtales.ftboss.item.FTBossItems.BLOOMING_THORNS.get())), this);
        net.farmingtales.ftboss.boss.ai.NonManorBossLootRouter.spawn(sl, new ItemEntity(sl, x, y + 1.0, z,
                new ItemStack(net.farmingtales.ftboss.item.FTBossItems.THORN_CROWN.get())), this);
        net.farmingtales.ftboss.boss.ai.NonManorBossLootRouter.spawn(sl, new ItemEntity(sl, x + 0.25, y + 1.0, z,
                new ItemStack(net.farmingtales.ftboss.item.FTBossItems.THORN_CHESTPLATE.get())), this);
        net.farmingtales.ftboss.boss.ai.NonManorBossLootRouter.spawn(sl, new ItemEntity(sl, x - 0.25, y + 1.0, z,
                new ItemStack(net.farmingtales.ftboss.item.FTBossItems.THORN_LEGGINGS.get())), this);
        net.farmingtales.ftboss.boss.ai.NonManorBossLootRouter.spawn(sl, new ItemEntity(sl, x, y + 1.0, z + 0.25,
                new ItemStack(net.farmingtales.ftboss.item.FTBossItems.THORN_BOOTS.get())), this);
        boolean silverKey = this.isFinaleChallenge() && this.getKillCredit() instanceof ServerPlayer;
        if (silverKey) {
            net.farmingtales.ftboss.boss.ai.NonManorBossLootRouter.spawn(sl, new ItemEntity(sl, x, y + 1.35, z,
                    new ItemStack(net.farmingtales.ftboss.item.FTBossItems.SILVER_KEY.get())), this);
        }
        net.farmingtales.ftboss.boss.ai.NonManorBossLootRouter.spawn(sl, new ExperienceOrb(sl, x, y, z, this.isFinaleChallenge() ? 300 : 150), this);
        FTBossMod.LOGGER.info("[FTBoss] 沙耶被击败：荆棘全套已掉落，终结难度银之钥={}", silverKey);
    }

    private void tickBossBar() {
        if (!this.isAlive()) {
            BossBars.untrack(this, this.bossBar);
            return;
        }
        if (this.isTeaMinion()) {
            this.hideBossBar();
            return;
        }
        this.updateBossBarName();
        this.bossBar.setProgress(this.getHealth() / this.getMaxHealth());
        boolean want = false;
        for (ServerPlayer p : this.level().getEntitiesOfClass(ServerPlayer.class,
                this.getBoundingBox().inflate(BOSS_BAR_RANGE))) {
            if (this.distanceToSqr(p) < BOSS_BAR_RANGE * BOSS_BAR_RANGE) {
                this.bossBar.addPlayer(p);
                want = true;
            } else {
                this.bossBar.removePlayer(p);
            }
        }
        if (this.bossBar.isVisible() != want) this.bossBar.setVisible(want);
    }

    @Override
    public void remove(RemovalReason reason) {
        /* v58: 被清场 discard 时也把茶芽小妖一起收掉(dropRewards 不一定跑) */
        if (!this.level().isClientSide) {
            this.buds.reset();
            net.farmingtales.ftboss.boss.ai.SummonSweep.clear(this,
                    net.farmingtales.ftboss.boss.ai.SummonSweep.RANGE, TeaMinionEntity.class);
        }
        BossBars.untrack(this, this.bossBar);
        this.stopMusic();
        super.remove(reason);
    }

    /** 统一清理: 隐藏 + 移除所有玩家 */
    public void hideBossBar() {
        this.bossBar.setVisible(false);
        this.bossBar.removeAllPlayers();
    }

    public ServerBossEvent getBossBar() {
        return this.bossBar;
    }

    @Override
    protected boolean isSunBurnTick() {
        return false;
    }
}
