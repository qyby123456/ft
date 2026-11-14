/*
 * 2026-09-12
 *  A) 炸弹为主: 投弹间隔 26 -> 12t, 扇形弹幕 80~120 -> 170~230t(减少)
 *  B) 减少滞留/法术: 水坑 8% -> 4%; 控制魔法间隔 160~260 -> 320~440t, 法术池缩到 2 个
 *  C) 移动改为**光之女王式冲刺**: 悬停定位 -> 高速直线冲刺(0.95/tick, 14t) -> 换角度再来, 基础步长 0.38 -> 0.55
 *  D) 大便小怪: 生成"大答辩"模型(7 层堆叠, 64x64) + PoopMinionEntity + 渲染器 + 注册 + 二阶段每 15s 召唤 2~3 只
 */
const fs = require('fs');
const path = require('path');
const ROOT = process.argv[2];
const J = 'src/main/java/net/farmingtales/ftboss';
const read = p => fs.readFileSync(path.join(ROOT, p), 'utf8');
const write = (p, s) => { fs.mkdirSync(path.dirname(path.join(ROOT, p)), { recursive: true }); fs.writeFileSync(path.join(ROOT, p), s); };
function rep(rel, a, b, label) {
    const s = read(rel);
    if (!s.includes(a)) { console.log('  FAIL ' + label); return; }
    write(rel, s.replace(a, b));
    console.log('  OK   ' + label);
}
const CB = `${J}/entity/CompostBossEntity.java`;

/* A 炸弹为主 */
rep(CB, '                    this.bombCd = 26;', '                    this.bombCd = 12;   // 2026-09-12: 以炸弹为主(26 -> 12t)', 'A 投弹加密');
rep(CB, '                this.barrageCd = 80 + this.random.nextInt(40);', '                this.barrageCd = 170 + this.random.nextInt(60);   // 减少弹幕', 'A 弹幕减少');
/* B 减少滞留/法术 */
rep(CB, '        bomb.setPuddle(this.random.nextFloat() < 0.08f);   // 20% -> 8%', '        bomb.setPuddle(this.random.nextFloat() < 0.04f);   // 2026-09-12: 8% -> 4%', 'B 水坑再减');
rep(CB, '            this.spellCd = 160 + this.random.nextInt(100);', '            this.spellCd = 320 + this.random.nextInt(120);   // 减少法术使用', 'B 法术减少');
rep(CB, `            String[] pool = { "irons_spellbooks:black_hole", "irons_spellbooks:ice_block",
                    "irons_spellbooks:thunderstorm", "irons_spellbooks:magic_missile" };`,
`            String[] pool = { "irons_spellbooks:black_hole", "irons_spellbooks:magic_missile" };   // 只留 2 个, 减少法术占比`, 'B 法术池缩小');
/* C 冲刺移动 */
rep(CB, '                double step = Math.min(0.38, dl);   // 2026-09-12: 飞行速度 0.22 -> 0.38',
`                /* 2026-09-12 按用户要求: 学习**光之女王**的移动 —— 悬停定位后高速直线冲刺 */
                if (this.dashTicks <= 0) {
                    this.dashCd--;
                    if (this.dashCd <= 0) {
                        this.dashCd = 26 + this.random.nextInt(14);
                        this.dashTicks = 14;
                        this.dashX = -Math.sin(Math.toRadians(this.getYRot()));
                        this.dashZ = Math.cos(Math.toRadians(this.getYRot()));
                    }
                }
                double step;
                if (this.dashTicks > 0) {
                    this.dashTicks--;
                    step = 0.95;                       // 冲刺速度
                    this.setPos(this.getX() + this.dashX * step, this.getY(), this.getZ() + this.dashZ * step);
                } else {
                    step = Math.min(0.55, dl);         // 基础移动也更快(0.38 -> 0.55)
                }`, 'C 冲刺移动');
rep(CB, '    private int bombWindup = -1;', `    private int bombWindup = -1;
    /** 光之女王式冲刺 */
    private int dashTicks = 0;
    private int dashCd = 20;
    private double dashX = 0, dashZ = 0;`, 'C 冲刺字段');

/* D 大便小怪: 模型生成器 + 实体 + 渲染器 */
write('tools/model3d/gen_poop_minion.cjs', `/* 大答辩小怪模型(2026-09-12): 7 层堆叠 + 番茄眼, 64x64 */
const fs=require('fs'),path=require('path');const {Canvas}=require('./png.cjs');
const OUT=process.argv[2]||'out_poop_minion';const W=64,H=64;
const cv=new Canvas(W,H);for(let y=0;y<H;y++)for(let x=0;x<W;x++)cv.set(x,y,0,0,0,0);
const P={poop:[112,78,46],dark:[80,52,30],light:[148,108,66],eyeW:[246,246,240],tomato:[214,58,48]};
let px=1,py=1,rh=0;const C=[];
function alloc(w,h,d){const fw=Math.ceil(2*(w+d)),fh=Math.ceil(d+h);if(px+fw>W){px=1;py+=rh+2;rh=0;}const u=px,v=py;px+=fw+2;rh=Math.max(rh,fh);return[u,v];}
function cube(b,o,s,m,rot){const uv=alloc(s[0],s[1],s[2]);C.push({b,o,s,uv,m,rot});}
const L=[[10,3.0,9,6],[9.2,2.8,8.2,-8],[8.2,2.6,7.2,10],[7,2.4,6,-12],[5.6,2.2,4.8,14],[4,2.0,3.4,-16],[2.4,2.4,2.2,18]];
let hy=1.0;
L.forEach((l,i)=>{cube('body',[-l[0]/2,hy,-l[2]/2],[l[0],l[1],l[2]],i%3===0?'poop':(i%3===1?'dark':'light'),l[3]);hy+=l[1]*0.97;});
cube('body',[-2.6,3.6,-4.6],[2.0,2.0,0.6],'tomato');cube('body',[0.6,3.6,-4.6],[2.0,2.0,0.6],'tomato');
cube('body',[-2.1,4.2,-4.9],[0.8,0.8,0.3],'eyeW');cube('body',[1.1,4.2,-4.9],[0.8,0.8,0.3],'eyeW');
for(const c of C){const fw=Math.ceil(2*(c.s[0]+c.s[2])),fh=Math.ceil(c.s[2]+c.s[1]);const col=P[c.m];
 for(let x=0;x<fw;x++)for(let y=0;y<fh;y++)cv.set(c.uv[0]+x,c.uv[1]+y,col[0],col[1],col[2],1);}
const bones=[{name:'root',pivot:[0,0,0]},{name:'body',pivot:[0,1,0],parent:'root',cubes:C.map(c=>{const o={origin:c.o,size:c.s,uv:c.uv};if(c.rot)o.rotation=[0,c.rot,0];return o;})}];
const geo={format_version:'1.12.0','minecraft:geometry':[{description:{identifier:'geometry.poop_minion',texture_width:W,texture_height:H,visible_bounds_width:2,visible_bounds_height:2,visible_bounds_offset:[0,1,0]},bones}]};
const anim={format_version:'1.8.0',animations:{'animation.poop_minion.idle':{loop:true,animation_length:1.2,bones:{root:{position:{'0.0':[0,0,0],'0.6':[0,0.35,0],'1.2':[0,0,0]}},body:{rotation:{'0.0':[0,-6,0],'0.6':[0,6,0],'1.2':[0,-6,0]}}}}}};
fs.mkdirSync(OUT,{recursive:true});
fs.writeFileSync(path.join(OUT,'poop_minion.geo.json'),JSON.stringify(geo,null,1));
fs.writeFileSync(path.join(OUT,'poop_minion.animation.json'),JSON.stringify(anim,null,1));
cv.save(path.join(OUT,'poop_minion.png'));
console.log('大便小怪模型已生成: cube='+C.length);
`);
{
    const r = require('child_process').spawnSync('node', ['gen_poop_minion.cjs', 'out_poop_minion'], { cwd: path.join(ROOT, 'tools/model3d'), stdio: 'inherit' });
    console.log('  D 模型生成退出码: ' + r.status);
    const A = path.join(ROOT, 'src/main/resources/assets/ftboss');
    for (const [a, b] of [['poop_minion.geo.json', 'geo/poop_minion.geo.json'], ['poop_minion.animation.json', 'animations/poop_minion.animation.json'], ['poop_minion.png', 'textures/entity/poop_minion.png']]) {
        const src = path.join(ROOT, 'tools/model3d/out_poop_minion', a);
        if (fs.existsSync(src)) { fs.copyFileSync(src, path.join(A, b)); console.log('  同步 ' + b); }
    }
}
write(`${J}/entity/PoopMinionEntity.java`, `package net.farmingtales.ftboss.entity;

import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/** Mobile poop minion (2026-09-12): a big poop that hops after players and stacks a debuff on hit. */
public class PoopMinionEntity extends Monster implements GeoAnimatable {

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.poop_minion.idle");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public PoopMinionEntity(EntityType<? extends PoopMinionEntity> type, Level level) {
        super(type, level);
        this.xpReward = 0;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 16.0)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.34)
                .add(Attributes.FOLLOW_RANGE, 28.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15, false));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity le) {
            le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 0));
            le.addEffect(new MobEffectInstance(MobEffects.HUNGER, 100, 0));
        }
        return hit;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && this.tickCount > 600) this.discard();   // 30s 后自行消散
    }

    @Override
    public boolean removeWhenFarAway(double d) { return false; }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "poop_ctrl", 6, this::predicate));
    }

    private PlayState predicate(AnimationState<PoopMinionEntity> state) {
        state.setAnimation(IDLE);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    @Override
    public double getTick(Object o) { return this.tickCount; }
}
`);
write(`${J}/client/model/PoopMinionModel.java`, `package net.farmingtales.ftboss.client.model;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.PoopMinionEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PoopMinionModel extends GeoModel<PoopMinionEntity> {
    @Override public ResourceLocation getModelResource(PoopMinionEntity e) { return new ResourceLocation(FTBossMod.MOD_ID, "geo/poop_minion.geo.json"); }
    @Override public ResourceLocation getTextureResource(PoopMinionEntity e) { return new ResourceLocation(FTBossMod.MOD_ID, "textures/entity/poop_minion.png"); }
    @Override public ResourceLocation getAnimationResource(PoopMinionEntity e) { return new ResourceLocation(FTBossMod.MOD_ID, "animations/poop_minion.animation.json"); }
}
`);
write(`${J}/client/render/PoopMinionRenderer.java`, `package net.farmingtales.ftboss.client.render;

import net.farmingtales.ftboss.client.model.PoopMinionModel;
import net.farmingtales.ftboss.entity.PoopMinionEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PoopMinionRenderer extends GeoEntityRenderer<PoopMinionEntity> {
    public PoopMinionRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new PoopMinionModel());
        this.shadowRadius = 0.4f;
    }
}
`);
rep(`${J}/entity/FTEntities.java`, '    /** 炫彩答辩超人(第 7 号 Boss, 2026-09-12)',
`    /** 大便小怪(2026-09-12): 可移动, 撞到玩家叠缓慢/饥饿 */
    public static final RegistryObject<EntityType<PoopMinionEntity>> POOP_MINION =
            register("poop_minion", PoopMinionEntity::new, 0.8f, 0.9f);
    /** 炫彩答辩超人(第 7 号 Boss, 2026-09-12)`, 'D 实体注册');
rep(`${J}/entity/FTEntities.java`, '        e.put(COMPOST_BOSS.get(), CompostBossEntity.createAttributes().build());',
    '        e.put(COMPOST_BOSS.get(), CompostBossEntity.createAttributes().build());\n        e.put(POOP_MINION.get(), PoopMinionEntity.createAttributes().build());', 'D 属性注册');
rep(`${J}/client/FTClientSetup.java`, '        e.registerEntityRenderer(FTEntities.COMPOST_BOSS.get(), CompostRenderer::new);',
    '        e.registerEntityRenderer(FTEntities.COMPOST_BOSS.get(), CompostRenderer::new);\n        e.registerEntityRenderer(FTEntities.POOP_MINION.get(), PoopMinionRenderer::new);', 'D 渲染器');
rep(`${J}/client/FTClientSetup.java`, 'import net.farmingtales.ftboss.client.render.CompostRenderer;',
    'import net.farmingtales.ftboss.client.render.CompostRenderer;\nimport net.farmingtales.ftboss.client.render.PoopMinionRenderer;', 'D 渲染器 import');
/* 召唤: 二阶段每 15s 2~3 只 */
rep(CB, '    private int dashCd = 20;',
`    private int dashCd = 20;
    /** 召唤大便小怪 */
    private int minionCd = 200;`, 'D 召唤字段');
rep(CB, '        // attack speed decay: no hit on a player for 3s -> reset',
`        /* 2026-09-12: 二阶段召唤**可移动的大便小怪** */
        if (this.getPhase() >= 2 && this.minionCd > 0 && --this.minionCd <= 0 && this.level() instanceof ServerLevel msl) {
            this.minionCd = 300;
            int n = 2 + this.random.nextInt(2);
            for (int i = 0; i < n; i++) {
                PoopMinionEntity m = FTEntities.POOP_MINION.get().create(msl);
                if (m == null) continue;
                m.moveTo(this.getX() + (this.random.nextDouble() - 0.5) * 3, this.getY(), this.getZ() + (this.random.nextDouble() - 0.5) * 3, 0f, 0f);
                if (this.getTarget() != null) m.setTarget(this.getTarget());
                msl.addFreshEntity(m);
            }
            FTBossMod.LOGGER.info("[FTBoss][CompostMan] summoned {} poop minions", n);
        }

        // attack speed decay: no hit on a player for 3s -> reset`, 'D 召唤逻辑');
console.log('完成');
