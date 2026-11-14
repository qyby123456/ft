/*
 * ⑤ 代码接入 · 第 1 步(2026-09-12)
 *   A) 把 out_compost_boss 的模型/动画/贴图落地到 resources
 *   B) 新建: CompostBossEntity(实体) / CompostGeoModel(模型, 按阶段切 _p2) / CompostRenderer(渲染器, 带发光膜层)
 *   C) 注册: FTEntities(实体+属性) / FTClientSetup(渲染器) / lang(中英)
 *   D) 不做: 技能逻辑、掉落、石像、创造栏、召唤物(下一步)
 *  用法: node wire_compost.cjs <项目根>
 */
const fs = require('fs');
const path = require('path');
const ROOT = process.argv[2];
const J = 'src/main/java/net/farmingtales/ftboss';
const RES = 'src/main/resources/assets/ftboss';
const O = path.join(ROOT, 'tools/model3d/out_compost_boss');
const read = p => fs.readFileSync(path.join(ROOT, p), 'utf8');
const write = (p, s) => { fs.mkdirSync(path.dirname(path.join(ROOT, p)), { recursive: true }); fs.writeFileSync(path.join(ROOT, p), s); };
function rep(rel, a, b, label) {
    const s = read(rel);
    if (s.includes(b)) { console.log('  skip ' + label); return true; }
    if (!s.includes(a)) { console.log('  FAIL ' + label); return false; }
    write(rel, s.replace(a, b));
    console.log('  OK   ' + label);
    return true;
}

/* ---------- A) 资产落地 ---------- */
fs.mkdirSync(path.join(ROOT, RES, 'geo'), { recursive: true });
fs.mkdirSync(path.join(ROOT, RES, 'animations'), { recursive: true });
fs.mkdirSync(path.join(ROOT, RES, 'textures/entity'), { recursive: true });
const cp = [
    ['compost_superman.geo.json', 'geo/compost_superman.geo.json'],
    ['compost_superman_p2.geo.json', 'geo/compost_superman_p2.geo.json'],
    ['compost_superman.animation.json', 'animations/compost_superman.animation.json'],
    ['compost_superman.png', 'textures/entity/compost_superman.png'],
    ['compost_superman_p2.png', 'textures/entity/compost_superman_p2.png'],
    ['compost_superman_glowmask.png', 'textures/entity/compost_superman_glowmask.png'],
    ['compost_superman_p2_glowmask.png', 'textures/entity/compost_superman_p2_glowmask.png']
];
let n = 0;
for (const [a, b] of cp) { const s = path.join(O, a); if (fs.existsSync(s)) { fs.copyFileSync(s, path.join(ROOT, RES, b)); n++; } else console.log('  缺 ' + a); }
console.log(`  资产落地: ${n}/${cp.length}`);

/* ---------- B) 实体 / 模型 / 渲染器 ---------- */
write(`${J}/entity/CompostBossEntity.java`, `package net.farmingtales.ftboss.entity;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.BossBars;
import net.farmingtales.ftboss.sound.BossMusic;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 第 7 号 Boss: **炫彩答辩超人**(Compost Superman, 2026-09-12 起做).
 *
 * <p>设计要点:
 * <ul>
 *   <li>肥料/堆肥主题, **滑稽英雄**气质; 地面型(不飞), 手持 Create 的**手持土豆加农炮**;</li>
 *   <li>血量 340(落在 300~400 区间), 二阶段起**换用彩色答辩头模型**( _p2 )并整体变强;</li>
 *   <li>接入现有系统: 人数缩放(BossScaling 自动)/ Boss 血条(BossBars)/ 战斗 BGM(BossMusic)/ 石像掉落(BossMercy)。</li>
 * </ul>
 * 技能与机制(抛粪/招蝇/沼爆/射击/变身)下一步接入, 本类已预留 phase / 动作计时。
 */
public class CompostBossEntity extends Monster implements GeoAnimatable {

    /** 三阶段血量阈值 */
    private static final float P2_AT = 0.70f, P3_AT = 0.35f;
    /** 是否已强化(二阶段) */

    private static final EntityDataAccessor<Integer> DATA_PHASE =
            SynchedEntityData.defineId(CompostBossEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_EMPOWERED =
            SynchedEntityData.defineId(CompostBossEntity.class, EntityDataSerializers.BOOLEAN);

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.compost_superman.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.compost_superman.walk");
    private static final RawAnimation FIRE = RawAnimation.begin().thenPlay("animation.compost_superman.cannon_fire");
    private static final RawAnimation CHARGE = RawAnimation.begin().thenPlay("animation.compost_superman.gas_charge");
    private static final RawAnimation SHIFT = RawAnimation.begin().thenPlay("animation.compost_superman.phase_shift");
    private static final RawAnimation HURT = RawAnimation.begin().thenPlay("animation.compost_superman.hurt");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlay("animation.compost_superman.death");
    private static final RawAnimation TRANSFORM = RawAnimation.begin().thenPlay("animation.compost_superman.transform");
    private static final double T_FIRE = 22.0, T_CHARGE = 52.0, T_SHIFT = 44.0, T_TRANSFORM = 56.0, T_HURT = 9.0;

    private final AnimatableInstanceCache geckoCache = GeckoLibUtil.createInstanceCache(this);
    private final ServerBossEvent bossBar;

    private double actionTicks = 0.0;
    private String actionName = "";
    private int cfgCd = 200;

    public CompostBossEntity(EntityType<? extends CompostBossEntity> type, Level level) {
        super(type, level);
        this.xpReward = 120;
        this.setPersistenceRequired();
        this.bossBar = new ServerBossEvent(this.getDisplayName(),
                BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.PROGRESS);
        BossBars.track(this, this.bossBar);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 340.0)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.MOVEMENT_SPEED, 0.24)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.4);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_PHASE, 1);
        this.entityData.define(DATA_EMPOWERED, false);
    }

    public int getPhase() {
        return this.entityData.get(DATA_PHASE);
    }

    public boolean isEmpowered() {
        return this.entityData.get(DATA_EMPOWERED);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("phase", this.getPhase());
        tag.putBoolean("empowered", this.isEmpowered());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("phase")) this.entityData.set(DATA_PHASE, tag.getInt("phase"));
        if (tag.contains("empowered")) this.entityData.set(DATA_EMPOWERED, tag.getBoolean("empowered"));
    }

    /** 播放一次性动作(技能用) */
    public void playAction(String name, double ticks) {
        this.actionName = name;
        this.actionTicks = ticks;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hit = super.hurt(source, amount);
        if (hit && this.actionTicks <= 0 && !this.isDeadOrDying()) {
            this.actionName = "hurt";
            this.actionTicks = T_HURT;
        }
        return hit;
    }

    /** 手持 Create 的手持土豆加农炮(取不到 mod 就留空, 不崩) */
    private void ensureCannon() {
        ItemStack cur = this.getItemInHand(InteractionHand.MAIN_HAND);
        if (!cur.isEmpty()) return;
        Item cannon = ForgeRegistries.ITEMS.getValue(new net.minecraft.resources.ResourceLocation("create", "potato_cannon"));
        if (cannon == null) {
            if (this.cfgCd > 0) { FTBossMod.LOGGER.warn("[FTBoss][炫彩答辩超人] 未找到 create:potato_cannon, 主手留空"); this.cfgCd = -1; }
            return;
        }
        this.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(cannon));
        FTBossMod.LOGGER.info("[FTBoss][炫彩答辩超人] 已手持 create:potato_cannon");
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;
        if (this.actionTicks > 0) this.actionTicks -= 1.0;
        this.ensureCannon();
        /* 阶段推进(按血量) */
        float ratio = this.getMaxHealth() > 0 ? this.getHealth() / this.getMaxHealth() : 1f;
        int want = ratio <= P3_AT ? 3 : (ratio <= P2_AT ? 2 : 1);
        if (want > this.getPhase()) {
            this.entityData.set(DATA_PHASE, want);
            this.entityData.set(DATA_EMPOWERED, want >= 2);
            this.playAction("phase_shift", T_SHIFT);
            if (this.level() instanceof ServerLevel sl) {
                sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.4f, 0.7f);
                net.farmingtales.ftboss.util.Fx.particles(sl, this.getX(), this.getY() + 1.0, this.getZ(), 24, ParticleTypes.HAPPY_VILLAGER, 0.8, 0.05);
            }
            FTBossMod.LOGGER.info("[FTBoss] 炫彩答辩超人进入阶段 {}", want);
        }
        /* 血条 + BGM */
        if (this.bossBar != null) {
            this.bossBar.setProgress(Math.max(0f, Math.min(1f, ratio)));
            this.bossBar.setVisible(this.isAlive());
            for (Player p : this.level().players()) {
                if (p instanceof net.minecraft.server.level.ServerPlayer sp) {
                    if (this.distanceToSqr(sp) <= 48.0 * 48.0) this.bossBar.addPlayer(sp);
                    else this.bossBar.removePlayer(sp);
                }
            }
        }
        if (this.level() instanceof ServerLevel bsl) BossMusic.request(bsl, this);
        /* 战斗动作(占位: 周期性开炮动作; 真实弹丸下一步接入) */
        if (this.actionTicks <= 0 && this.tickCount % 80 == 0 && this.getTarget() != null) {
            this.playAction("cannon_fire", T_FIRE);
        }
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.level() instanceof ServerLevel sl) BossMusic.release(sl, this);
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!this.level().isClientSide) BossBars.untrack(this, this.bossBar);
        super.remove(reason);
    }

    /* ---------------- GeckoLib ---------------- */
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "compost_ctrl", 4, this::predicate));
    }

    private PlayState predicate(AnimationState<CompostBossEntity> state) {
        if (this.isDeadOrDying()) { state.setAnimation(DEATH); return PlayState.CONTINUE; }
        if (this.actionTicks > 0) {
            switch (this.actionName) {
                case "cannon_fire" -> { state.setAnimation(FIRE); return PlayState.CONTINUE; }
                case "gas_charge" -> { state.setAnimation(CHARGE); return PlayState.CONTINUE; }
                case "phase_shift" -> { state.setAnimation(SHIFT); return PlayState.CONTINUE; }
                case "transform" -> { state.setAnimation(TRANSFORM); return PlayState.CONTINUE; }
                default -> { state.setAnimation(HURT); return PlayState.CONTINUE; }
            }
        }
        state.setAnimation(state.isMoving() ? WALK : IDLE);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geckoCache;
    }

    @Override
    public double getTick(Object o) {
        return this.tickCount;
    }
}
`);

write(`${J}/client/model/CompostGeoModel.java`, `package net.farmingtales.ftboss.client.model;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.CompostBossEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 炫彩答辩超人模型: **二阶段起自动换用彩色答辩头版本(_p2)**(2026-09-12)。
 * GeckoLib 会在资源路径变化时自动重载模型, 无需额外实体或动画。
 */
public class CompostGeoModel extends GeoModel<CompostBossEntity> {

    private static ResourceLocation rl(String p) {
        return new ResourceLocation(FTBossMod.MOD_ID, p);
    }

    @Override
    public ResourceLocation getModelResource(CompostBossEntity e) {
        return rl(e.getPhase() >= 2 ? "geo/compost_superman_p2.geo.json" : "geo/compost_superman.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CompostBossEntity e) {
        return rl(e.getPhase() >= 2 ? "textures/entity/compost_superman_p2.png" : "textures/entity/compost_superman.png");
    }

    @Override
    public ResourceLocation getAnimationResource(CompostBossEntity e) {
        return rl("animations/compost_superman.animation.json");
    }
}
`);

write(`${J}/client/render/CompostRenderer.java`, `package net.farmingtales.ftboss.client.render;

import net.farmingtales.ftboss.client.model.CompostGeoModel;
import net.farmingtales.ftboss.entity.CompostBossEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

/** 炫彩答辩超人渲染器(2026-09-12): 带发光膜层(彩虹油膜/稻草/黄铜发光) */
public class CompostRenderer extends GeoEntityRenderer<CompostBossEntity> {
    public CompostRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new CompostGeoModel());
        this.shadowRadius = 1.2f;
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
`);

/* ---------- C) 注册 ---------- */
rep(`${J}/entity/FTEntities.java`,
    `    /** 茶树 Boss(沙耶) */`,
    `    /** 炫彩答辩超人(第 7 号 Boss, 2026-09-12): 肥料/堆肥主题, 地面型, 手持 Create 土豆加农炮 */
    public static final RegistryObject<EntityType<CompostBossEntity>> COMPOST_BOSS =
            register("compost_boss", CompostBossEntity::new, 1.6f, 3.4f);
    /** 茶树 Boss(沙耶) */`, 'FTEntities 实体注册');
rep(`${J}/entity/FTEntities.java`,
    `        e.put(TEA_TREE.get(), TeaTreeBossEntity.createAttributes().build());`,
    `        e.put(TEA_TREE.get(), TeaTreeBossEntity.createAttributes().build());
        e.put(COMPOST_BOSS.get(), CompostBossEntity.createAttributes().build());`, 'FTEntities 属性注册');
rep(`${J}/client/FTClientSetup.java`,
    `        e.registerEntityRenderer(FTEntities.MAGGOT_MINION.get(), MaggotRenderer::new);`,
    `        e.registerEntityRenderer(FTEntities.MAGGOT_MINION.get(), MaggotRenderer::new);
        // 炫彩答辩超人(2026-09-12)
        e.registerEntityRenderer(FTEntities.COMPOST_BOSS.get(), CompostRenderer::new);`, 'FTClientSetup 渲染器');
rep(`${J}/client/FTClientSetup.java`,
    `import net.farmingtales.ftboss.client.render.MaggotRenderer;`,
    `import net.farmingtales.ftboss.client.render.MaggotRenderer;
import net.farmingtales.ftboss.client.render.CompostRenderer;`, 'FTClientSetup import');

/* ---------- D) lang ---------- */
for (const [f, v] of [['zh_cn.json', '\u70ab\u5f69\u7b54\u8fa9\u8d85\u4eba'], ['en_us.json', 'Rainbow Compost Superman']]) {
    const p = path.join(ROOT, RES, 'lang', f);
    const j = JSON.parse(fs.readFileSync(p, 'utf8'));
    j['entity.ftboss.compost_boss'] = v;
    fs.writeFileSync(p, JSON.stringify(j, null, 2));
}
console.log('  lang: 已加 entity.ftboss.compost_boss');
console.log('第 1 步完成');
