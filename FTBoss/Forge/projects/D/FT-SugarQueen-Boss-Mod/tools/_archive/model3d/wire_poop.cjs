/*
 * ⑤ 第 2 步(2026-09-12): 彩色大便投掷物 + 加农炮连射
 *  A) 新实体 RainbowPoopEntity(继承原版 ThrowableItemProjectile, 零新增模型/贴图):
 *       · 一阶段用**棕色**道具外观, 二阶段用**彩色**(洋红/青/黄 轮换)
 *       · setPuddle(true) 的弹丸落地生成 **"肥水坑"**(原版 AreaEffectCloud, **棕色**) —— 范围内减速+中毒, 持续 8 秒
 *       · 命中实体造成 6 点伤害(以发射者为来源)
 *  B) Boss 连射: 每 ~120t 起一轮 **4~5 发**普通答辩(间隔 4t), 每发 **25%** 概率是"汇水坑答辩"
 *  C) 注册: FTEntities(实体, MISC 分类无需属性) + FTClientSetup(用原版 ThrownItemRenderer)
 *  用法: node wire_poop.cjs <项目根>
 */
const fs = require('fs');
const path = require('path');
const ROOT = process.argv[2];
const J = 'src/main/java/net/farmingtales/ftboss';
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

/* ---------- A) 投掷物实体 ---------- */
write(`${J}/entity/RainbowPoopEntity.java`, `package net.farmingtales.ftboss.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/**
 * **彩色大便投掷物**(2026-09-12 按用户要求):
 * 一阶段用棕色外观, 二阶段用彩色外观; **落地一律生成棕色的"肥水坑"**(原版 AreaEffectCloud)。
 * 零新增模型/贴图: 直接复用原版物品外观 + 原版 ThrownItemRenderer。
 */
public class RainbowPoopEntity extends ThrowableItemProjectile {

    /** 是否落地生成肥水坑(概率弹) */
    private boolean puddle = false;
    /** 命中伤害 */
    private float dmg = 6.0f;
    /** 二阶段(彩色)外观 */
    private boolean rainbow = false;

    public RainbowPoopEntity(EntityType<? extends RainbowPoopEntity> type, Level level) {
        super(type, level);
    }

    public RainbowPoopEntity(Level level, LivingEntity shooter) {
        super(FTEntities.RAINBOW_POOP.get(), shooter, level);
    }

    public void setPuddle(boolean v) { this.puddle = v; }
    public void setDamage(float v) { this.dmg = v; }
    public void setRainbow(boolean v) {
        this.rainbow = v;
        this.setItem(new ItemStack(itemFor(level().getRandom().nextInt(3))));
    }

    private static Item itemFor(int i) {
        return switch (i) {
            case 0 -> Items.MAGENTA_DYE;
            case 1 -> Items.CYAN_DYE;
            default -> Items.YELLOW_DYE;
        };
    }

    @Override
    protected Item getDefaultItem() {
        return Items.BROWN_DYE;      // 一阶段: 棕色
    }

    @Override
    protected void onHitEntity(net.minecraft.world.phys.EntityHitResult hit) {
        super.onHitEntity(hit);
        if (this.level().isClientSide) return;
        Entity e = hit.getEntity();
        Entity owner = this.getOwner();
        if (e instanceof LivingEntity le) {
            le.hurt(this.damageSources().thrown(this, owner), this.dmg);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level().isClientSide) return;
        if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
            var pos = result.getLocation();
            /* 落地飞溅(棕色为主, 彩色弹追加彩虹点) */
            net.farmingtales.ftboss.util.Fx.particles(sl, pos.x, pos.y + 0.2, pos.z, 14,
                    ParticleTypes.ITEM_SLIME, 0.6, 0.08);
            if (this.rainbow) {
                net.farmingtales.ftboss.util.Fx.particles(sl, pos.x, pos.y + 0.3, pos.z, 10,
                        ParticleTypes.HAPPY_VILLAGER, 0.7, 0.06);
            }
            if (this.puddle) {
                AreaEffectCloud cloud = new AreaEffectCloud(sl, pos.x, pos.y + 0.1, pos.z);
                cloud.setRadius(3.2f);
                cloud.setDuration(160);                 // 8 秒
                cloud.setRadiusOnUse(-0.05f);
                cloud.setWaitTime(10);
                cloud.setColor(0x8A6A3C);               // **棕色肥水坑**
                cloud.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0));
                cloud.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0));
                sl.addFreshEntity(cloud);
                sl.playSound(null, pos.x, pos.y, pos.z, net.minecraft.sounds.SoundEvents.SLIME_BLOCK_PLACE,
                        net.minecraft.sounds.SoundSource.HOSTILE, 1.2f, 0.7f);
            }
        }
        this.discard();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;
        if (this.tickCount % 3 == 0) {
            if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                net.farmingtales.ftboss.util.Fx.particles(sl, this.getX(), this.getY(), this.getZ(), 1,
                        this.rainbow ? ParticleTypes.HAPPY_VILLAGER : ParticleTypes.ITEM_SLIME, 0.08, 0.01);
            }
        }
    }
}
`);

/* ---------- Boss 连射 ---------- */
rep(`${J}/entity/CompostBossEntity.java`,
    `    private double actionTicks = 0.0;`,
    `    /** 连射: 剩余发数 / 间隔计时 */
    private int burstLeft = 0;
    private int burstGap = 0;
    private int burstCd = 120;
    private double actionTicks = 0.0;`, 'Boss 连射字段');
rep(`${J}/entity/CompostBossEntity.java`,
    `        /* \u8840\u6761 + BGM */`,
    `        /* \u52a0\u519c\u70ae\u8fde\u5c04: \u6bcf ~120t \u4e00\u8f6e 4~5 \u53d1, \u6bcf\u53d1 25% \u6982\u7387\u662f"\u6c47\u6c34\u5751\u7b54\u8fa9" */
        if (this.burstLeft > 0) {
            if (--this.burstGap <= 0) {
                this.burstGap = 4;
                this.burstLeft--;
                this.firePoop();
            }
        } else if (--this.burstCd <= 0 && this.getTarget() != null && this.getTarget().isAlive()) {
            this.burstCd = 100 + this.random.nextInt(60);
            this.burstLeft = 4 + this.random.nextInt(2);      // 4~5 \u53d1
            this.burstGap = 1;
            this.playAction("cannon_fire", T_FIRE);
        }
        /* \u8840\u6761 + BGM */`, 'Boss 连射逻辑');
rep(`${J}/entity/CompostBossEntity.java`,
    `    @Override
    public boolean removeWhenFarAway(double distance) {`,
    `    /** \u53d1\u5c04\u4e00\u53d1\u7b54\u8fa9(\u4e8c\u9636\u6bb5\u5f69\u8272, 25% \u6982\u7387\u5e26\u80a5\u6c34\u5751) */
    private void firePoop() {
        if (!(this.level() instanceof ServerLevel sl)) return;
        LivingEntity t = this.getTarget();
        RainbowPoopEntity poop = new RainbowPoopEntity(sl, this);
        poop.setRainbow(this.getPhase() >= 2);
        poop.setPuddle(this.random.nextFloat() < 0.25f);
        poop.setDamage(6.0f + this.getPhase() * 2.0f);
        double sx = this.getX(), sy = this.getY() + 1.6, sz = this.getZ();
        poop.setPos(sx, sy, sz);
        if (t != null) {
            double dx = t.getX() - sx, dy = t.getEyeY() - sy, dz = t.getZ() - sz;
            double len = Math.max(0.1, Math.sqrt(dx * dx + dy * dy + dz * dz));
            poop.shoot(dx / len, dy / len + 0.12, dz / len, 1.25f, 3.0f);
        } else {
            poop.shoot(0, 0, 0, 0, 0);
        }
        sl.addFreshEntity(poop);
        sl.playSound(null, sx, sy, sz, SoundEvents.SNOWBALL_THROW, SoundSource.HOSTILE, 1.0f, 1.4f);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {`, 'Boss firePoop');

/* ---------- C) 注册 ---------- */
rep(`${J}/entity/FTEntities.java`,
    `    /** \u70ab\u5f69\u7b54\u8fa9\u8d85\u4eba(\u7b2c 7 \u53f7 Boss, 2026-09-12): \u80a5\u6599/\u5806\u80a5\u4e3b\u9898, \u5730\u9762\u578b, \u624b\u6301 Create \u571f\u8c46\u52a0\u519c\u70ae */`,
    `    /** \u5f69\u8272\u5927\u4fbf\u6295\u63b7\u7269(2026-09-12): \u4e00\u9636\u6bb5\u68d5\u8272 / \u4e8c\u9636\u6bb5\u5f69\u8272, \u843d\u5730\u53ef\u751f\u6210\u80a5\u6c34\u5751 */
    public static final RegistryObject<EntityType<RainbowPoopEntity>> RAINBOW_POOP =
            register("rainbow_poop", RainbowPoopEntity::new, 0.5f, 0.5f, MobCategory.MISC);
    /** \u70ab\u5f69\u7b54\u8fa9\u8d85\u4eba(\u7b2c 7 \u53f7 Boss, 2026-09-12): \u80a5\u6599/\u5806\u80a5\u4e3b\u9898, \u5730\u9762\u578b, \u624b\u6301 Create \u571f\u8c46\u52a0\u519c\u70ae */`,
    'FTEntities 投掷物注册');
rep(`${J}/client/FTClientSetup.java`,
    `        e.registerEntityRenderer(FTEntities.COMPOST_BOSS.get(), CompostRenderer::new);`,
    `        e.registerEntityRenderer(FTEntities.COMPOST_BOSS.get(), CompostRenderer::new);
        // \u5f69\u8272\u5927\u4fbf\u6295\u63b7\u7269: \u76f4\u63a5\u7528\u539f\u7248\u7269\u54c1\u6295\u63b7\u6e32\u67d3\u5668(\u96f6\u65b0\u589e\u8d44\u6e90)
        e.registerEntityRenderer(FTEntities.RAINBOW_POOP.get(),
                new net.minecraft.client.renderer.entity.ThrownItemRenderer<>(e.getContext(), 1.0f, true));`, 'FTClientSetup 投掷物渲染器');
console.log('第 2 步完成');
