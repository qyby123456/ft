/* patch_teleport_strike.cjs —— 暗影剑技能：**去掉下坠，改成"传送到落点并造成伤害"**
 *
 * 用户 2026-09-16 要求：「暗影剑技能去掉下坠部分，改成传送造成伤害」。
 *
 * 改前链路：珍珠命中 -> 把人瞬移到落点**上方 14 格** -> 给向下初速 -> 登记 SkyDrop
 *           -> ShadowSwordItem 每 tick 压坠落伤害 + 往下拽 -> 触地 skyImpact 结算。
 * 改后链路：珍珠命中 -> **直接传送到落点**（就地找落脚点）-> 同一瞬间结算范围魔法伤害。
 *           SkyDrop 整套状态机（含卡住/登出/换维度的清理）**全部删除**。
 *
 * 涉及 3 个文件，其中 ShadowSwordItem/ShadowCastManager 是并行会话也在改的文件，
 * 所以这里用**锚点断言**：任何一个锚点找不到就整体不写盘并报错，绝不半途改坏。
 *
 * 用法: node tools/model3d/patch_teleport_strike.cjs [--dry]
 */
'use strict';

const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..', '..');
const SRC = path.join(ROOT, 'src/main/java/net/farmingtales/ftboss');
const DRY = process.argv.includes('--dry');

const F_SWORD = path.join(SRC, 'item/weapons/ShadowSwordItem.java');
const F_PEARL = path.join(SRC, 'entity/ShadowPearlEntity.java');
const F_CAST = path.join(SRC, 'skill/ShadowCastManager.java');

const edits = [];
const cut = (from, to) => ({ kind: 'cut', from, to });
const swap = (repl, from, to) => ({ kind: 'swap', from, to, repl });

/* ---------------- A. ShadowSwordItem ---------------- */

/* A1: 删掉 inventoryTick 里的下坠驱动钩子 */
const A1 = cut(
  `
                // ★ 暗影坠落: 下坠期间持续加速 + 压制坠落伤害; 触地瞬间结算范围魔法伤害
                SkyDrop drop = SKY_DROP.get(sp.getUUID());
                if (drop != null) {
                    tickSkyDrop(sp, drop);
                }`,
  `
                if (tag.getBoolean(TAG_READY) != ready) {`);

/* A2: 整段"暗影坠落"子系统 -> 新的"暗影传送"API（含结算本体） */
const NEW_SECTION = `    /* ============ 暗影传送（v2: 去掉下坠，改为"传送到落点并结算"） ============ */

    /** 落点范围魔法伤害（半径 3 格，**无视护甲**）。 */
    public static final float SKY_DAMAGE = 5.0F;
    public static final double SKY_RADIUS = 3.0D;
    /** 竖向判定半高：落点是"柱子"不是"球"，高处的敌人也该吃到。 */
    public static final double SKY_HALF_HEIGHT = 2.5D;
    public static final int SKY_BLIND_TICKS = 120;
    /** 落点把人往外推的力量（贴脸时不至于零击退）。 */
    private static final double SKY_KNOCKBACK = 1.4D;
    /** 传送落点最多向下找几格落脚点；都找不到就**不传送**、只结算伤害。 */
    private static final int LANDING_SEARCH_DOWN = 3;

    /** 找一个能站人的落点（脚下实心、身位两格空），找不到返回 NaN。 */
    private static double findLandingY(ServerLevel sl, double x, double y, double z) {
        net.minecraft.core.BlockPos base = net.minecraft.core.BlockPos.containing(x, y, z);
        for (int dy = 0; dy <= LANDING_SEARCH_DOWN; dy++) {
            net.minecraft.core.BlockPos feet = base.below(dy);
            if (sl.isEmptyBlock(feet) && sl.isEmptyBlock(feet.above()) && !sl.isEmptyBlock(feet.below())) {
                return feet.getY();
            }
        }
        return Double.NaN;
    }

    /**
     * 【暗影传送】把使用者**直接传送到珍珠落点**，并在落点结算范围魔法伤害。
     *
     * <p>v2（2026-09-16 按用户要求）：原来是把人瞬移到落点上方 14 格再"砸下来"，
     * 下坠期间要每 tick 压坠落伤害，还要处理卡住 / 登出 / 换维度的残留状态。
     * 现在**整段下坠都删掉了** —— 传送与结算是同一个瞬间，没有滞空状态，那些边角问题一并消失。
     *
     * <p>伤害走 {@code indirectMagic}（无视护甲，但受魔法抗性减免），与本模组其它"技能魔法伤害"一致。
     * PvP 保护：同一个队伍/友军不吃伤害（只吃击退与致盲）。
     */
    public static void teleportStrike(ServerPlayer sp, double x, double y, double z) {
        Level lv = sp.level();
        ServerLevel sl = sp.serverLevel();

        /* 0) 先找落脚点：落点本身能站就站，否则往下找几格；都找不到就不传送（伤害照常结算） */
        double landY = findLandingY(sl, x, y, z);
        if (!Double.isNaN(landY)) {
            sp.teleportTo(sl, x, landY, z, sp.getYRot(), sp.getXRot());
            sp.resetFallDistance();                    // 传送前若正在下落，别把这笔坠落伤害带过来
            sp.hurtMarked = true;
            Fx.sound(lv, x, landY, z, net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT, 1.0F, 0.9F);
            sp.displayClientMessage(net.minecraft.network.chat.Component.literal(
                    "§5【暗影传送】§7已抵达落点 —— 周围造成范围魔法伤害"), true);
        }

        /* 1) 范围伤害：横向用半径，竖向用柱子半高（高处的敌人也吃到） */
        AABB box = new AABB(x - SKY_RADIUS, y - SKY_HALF_HEIGHT, z - SKY_RADIUS,
                x + SKY_RADIUS, y + SKY_HALF_HEIGHT, z + SKY_RADIUS);
        DamageSource src = lv.damageSources().indirectMagic(sp, sp);
        int hit = 0;

        for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, box)) {
            if (e == sp) {
                continue;                                   // 不伤自己
            }
            // 横向超出半径的（AABB 是方的）跳过 -> 实际判定是圆柱
            double dx = e.getX() - x;
            double dz = e.getZ() - z;
            if (dx * dx + dz * dz > SKY_RADIUS * SKY_RADIUS) {
                continue;
            }
            // PvP / 友军保护：不伤害，但照样吃击退与致盲
            boolean hurtable = !(e instanceof Player victim && !sp.canHarmPlayer(victim));
            if (hurtable && e.hurt(src, SKY_DAMAGE)) {
                hit++;
            }
            e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, SKY_BLIND_TICKS, 0));
            // 2) 击退：从落点向外 + 略微抬起（贴脸时给一个保底力度）
            Vec3 away = new Vec3(e.getX() - x, 0.0D, e.getZ() - z);
            away = away.lengthSqr() < 1.0E-4D
                    ? new Vec3(sp.getRandom().nextDouble() - 0.5D, 0.0D, sp.getRandom().nextDouble() - 0.5D)
                    : away.normalize();
            e.push(away.x * SKY_KNOCKBACK, 0.35D, away.z * SKY_KNOCKBACK);
            e.hurtMarked = true;
        }

        /* 3) 冲击波：三圈错开半径的地面环 + 上冲暗影柱 + 碎屑（复用 Fx 收口的工具） */
        Fx.ring(lv, x, y + 0.1D, z, 2.2D, 28, ParticleTypes.REVERSE_PORTAL, 0.02D);
        Fx.ring(lv, x, y + 0.25D, z, SKY_RADIUS, 36, ParticleTypes.SQUID_INK, 0.05D);
        Fx.ring(lv, x, y + 0.4D, z, 1.2D, 20, ParticleTypes.CRIT, 0.08D);
        Fx.particles(lv, x, y + 0.5D, z, 40, ParticleTypes.SQUID_INK, 1.4D, 0.22D);
        Fx.particles(lv, x, y + 1.2D, z, 24, ParticleTypes.REVERSE_PORTAL, 1.0D, 0.35D);
        Fx.particles(lv, x, y + 0.2D, z, 18, ParticleTypes.CLOUD, 1.8D, 0.12D);
        Fx.dust(lv, x, y + 0.3D, z, 60, 0.35F, 0.16F, 0.5F, 2.6D, 0.25D);
        lv.addParticle(ParticleTypes.FLASH, x, y + 0.6D, z, 0.0D, 0.0D, 0.0D);

        /* 4) 音效：铁砧落地（主）+ 低频轰击 + 末影传送音做"暗影"味 */
        Fx.sound(lv, x, y, z, net.minecraft.sounds.SoundEvents.ANVIL_LAND, 1.1F, 0.55F);
        Fx.sound(lv, x, y, z, net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE, 0.7F, 1.35F);

        /* 5) 传送抵达瞬间武器闪光一次 */
        flashGlow(sp);

        FTBossMod.LOGGER.info("[FTBoss][暗影传送] {} 落点结算: 命中 {} 个目标, 半径 {} 格 / {} 点魔法伤害",
                sp.getGameProfile().getName(), hit, SKY_RADIUS, SKY_DAMAGE);
    }

`;
const A2 = swap(
  NEW_SECTION,
  `    /* ============ 暗影坠落（魔法下砸）: 待落地表 + 下坠驱动 + 触地结算 ============ */`,
  `    /** 手上（主手或副手）拿着暗影剑 -> 触发一次武器光效闪光。 */`);

/* A3: 删掉旧的 skyImpact（结算本体已搬进 teleportStrike） */
const A3 = cut(
  `    /**
     * 落地结算：**范围魔法伤害（无视护甲）** + 致盲 + 击退 + 冲击波特效/音效 + 武器闪光。`,
  `    /** 冷却完成瞬间: 魔眼与柄首附近 8 个暗紫/浅紫粒子, 向外扩散, 只撒一次。 */`);

/* ---------------- B. ShadowPearlEntity ---------------- */

const B1 = swap(
  `        /* 2) 【暗影传送】(v2 2026-09-16 按用户要求): 删掉"瞬移到 14 格高再砸下来"的下坠，
              改成**直接传送到落点、并在落点结算范围魔法伤害** —— 传送与结算是同一瞬间，没有滞空状态。 */
        if (owner instanceof ServerPlayer sp && sp.isAlive()) {
            net.farmingtales.ftboss.item.weapons.ShadowSwordItem.teleportStrike(sp, at.x, at.y, at.z);
        }`,
  `        /* 2) 暗影坠落: 把使用者瞬移到命中点**正上方 14 格**, 再向下砸落（一个技能, 无模式切换） */`,
  `        /* 3) 特效 + 音效（一次） */`);

const B2 = cut(
  `
    /**
     * 找"天上落点": 从命中点上方 {@link #SKY_HEIGHT} 格起向上找两格高的空位。
     * 找不到（下界低顶 / 头顶被堵 / 超出建筑高度）返回 null -> 调用方回退为"不瞬移"。
     */`,
  `}
`);

/* ---------------- C. ShadowCastManager ---------------- */

const C1 = cut(
  `            /* ★ 暗影坠落: 登出时必须清坠落状态 —— 否则下次登录还挂着"下坠中"，
               一进来就被每 tick 的 -1.6 速度按着往地里钻。 */
            ShadowSwordItem.clearSkyDrop(player.getUUID());
`,
  `        }
    }

    @SubscribeEvent
    public static void onDeath(`);

const C2 = cut(
  `            ShadowSwordItem.clearSkyDrop(player.getUUID());   // 死了就别再往下砸了
`,
  `        }
    }

    @SubscribeEvent
    public static void onChangedDimension(`);

const C3 = cut(
  `            /* 换维度 = 坐标不连续，登记的落点已经没意义了 -> 清掉，别在新区块里乱砸 */
            ShadowSwordItem.clearSkyDrop(player.getUUID());
`,
  `        }
    }

    @SubscribeEvent
    public static void onServerStopped(`);

const C4 = cut(
  `        ShadowSwordItem.clearAllSkyDrops();
`,
  `    }
}`);

/* ---------------- 执行 ---------------- */
const plan = [
  [F_SWORD, [A1, A2, A3], 'ShadowSwordItem.java'],
  [F_PEARL, [B1, B2], 'ShadowPearlEntity.java'],
  [F_CAST, [C1, C2, C3, C4], 'ShadowCastManager.java']
];

const BAK = path.join(ROOT, '_backup', 'teleport_strike_20260916');
if (!DRY) fs.mkdirSync(BAK, { recursive: true });

let failed = false;
const outputs = [];

for (const [file, ops, label] of plan) {
  let text = fs.readFileSync(file, 'utf8');
  const before = text;
  const applied = [];

  for (const op of ops) {
    const i = text.indexOf(op.from);
    if (i < 0) { console.log(`  ✗ ${label}: 锚点找不到 -> ${JSON.stringify(op.from.slice(0, 60))}`); failed = true; continue; }
    const j = text.indexOf(op.to, i + op.from.length);
    if (j < 0) { console.log(`  ✗ ${label}: 结束锚点找不到 -> ${JSON.stringify(op.to.slice(0, 60))}`); failed = true; continue; }
    const repl = op.kind === 'cut' ? '' : op.repl;
    // cut: 删掉 [from, to) 之间的全部内容（保留 to 锚点）
    // swap: 把 [from, to) 整段换成 repl + to 锚点
    text = text.slice(0, i) + repl + text.slice(j);
    applied.push(op.kind + ':' + op.from.slice(0, 34).replace(/\n/g, '⏎'));
  }
  outputs.push([file, label, before, text, applied]);
}

console.log('=== 补丁计划 ===');
for (const [, label, , , applied] of outputs) {
  console.log(`  ${label}: ${applied.length} 处`);
  for (const a of applied) console.log('      · ' + a);
}
if (failed) { console.log('\n有锚点没命中 —— **整体不写盘**（避免半途改坏）。'); process.exit(1); }

if (DRY) { console.log('\n(dry-run，未写盘)'); process.exit(0); }

for (const [file, label, before, after] of outputs) {
  fs.copyFileSync(file, path.join(BAK, path.basename(file)));
  fs.writeFileSync(file, after, 'utf8');
  console.log(`  写入 ${label}: ${before.length} -> ${after.length} 字节`);
}
console.log('\n原件备份在: ' + path.relative(ROOT, BAK));
