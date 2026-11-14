/*
 * 2026-09-12 六项:
 *  ① 飞行速度: 0.22 -> 0.38/tick(更快)
 *  ② 投弹参考酒桶王抛射: 改成**抛物线迫击炮**(水平引导 + 明显上抛分量), 并在投前留 10t 抬手
 *  ③ 减少滞留给药水/水坑: 水坑概率 20% -> 8%; 云时长 260 -> 160t; 负面效果 5 种 -> 3 种(缓慢/虚弱/饥饿), 时长下调
 *  ④ 飞行高度 +1 格: 地面 +4.0 -> +5.0
 *  ⑤ 去除激光: 直接把激光触发条件置为永不成立(光点与判定随之不再执行)
 *  ⑥ 使用控制魔法: 铁魔法池改为控制系(black_hole 吸附 / ice_block 冻结 / thunderstorm / magic_missile), 等级 2
 */
const fs = require('fs');
const path = require('path');
const ROOT = process.argv[2];
const read = p => fs.readFileSync(path.join(ROOT, p), 'utf8');
const write = (p, s) => fs.writeFileSync(path.join(ROOT, p), s);
function rep(rel, a, b, label) {
    const s = read(rel);
    if (!s.includes(a)) { console.log('  FAIL ' + label); return; }
    write(rel, s.replace(a, b));
    console.log('  OK   ' + label);
}
const CB = 'src/main/java/net/farmingtales/ftboss/entity/CompostBossEntity.java';

/* ① 速度 + ④ 高度 */
rep(CB, '            double wantY = gp.getY() + 4.0 + Math.sin(this.tickCount * 0.08) * 0.3;',
        '            double wantY = gp.getY() + 5.0 + Math.sin(this.tickCount * 0.08) * 0.3;   // 2026-09-12: 高度 +1 格', '④ 高度+1');
rep(CB, '                double step = Math.min(0.22, dl);',
        '                double step = Math.min(0.38, dl);   // 2026-09-12: 飞行速度 0.22 -> 0.38', '① 飞行加速');

/* ② 炸弹改抛物线 + 投前抬手 */
rep(CB, `                if (--this.bombCd <= 0) {
                    this.bombCd = 24;
                    this.dropBomb(mt, fsl);
                }`,
`                if (--this.bombCd <= 0) {
                    this.bombCd = 26;
                    this.playAction("cannon_fire", T_FIRE);   // 2026-09-12: 投弹前抬手
                    this.bombWindup = 10;
                }
                if (this.bombWindup > 0 && --this.bombWindup == 0) this.dropBomb(mt, fsl);`, '② 投弹抬手');
rep(CB, '    private int bombCd = 20;', '    private int bombCd = 20;\n    private int bombWindup = -1;', '② 抬手字段');
rep(CB, `        bomb.shoot(dx / dl * 0.35, -1.0, dz / dl * 0.35, 1.1f, 0.4f);   // 主要向下`,
        `        /* 2026-09-12 参考酒桶王的抛射: 水平引导 + 明显上抛 -> 抛物线砸向玩家(迫击炮手感) */
        bomb.shoot(dx / dl * 0.95, 1.05, dz / dl * 0.95, 1.15f, 0.45f);`, '② 抛物线');

/* ③ 减少水坑 */
rep(CB, '        bomb.setPuddle(this.random.nextFloat() < 0.20f);', '        bomb.setPuddle(this.random.nextFloat() < 0.08f);   // 20% -> 8%', '③ 炸弹水坑');
rep('src/main/java/net/farmingtales/ftboss/entity/RainbowPoopEntity.java',
    'cloud.setDuration(260);                 // 13 秒', 'cloud.setDuration(160);                 // 2026-09-12: 13 秒 -> 8 秒(减少滞留)', '③ 云时长');
rep('src/main/java/net/farmingtales/ftboss/entity/RainbowPoopEntity.java',
    `                cloud.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 140, 0));
                cloud.addEffect(new MobEffectInstance(MobEffects.HUNGER, 200, 1));`,
    `                // 2026-09-12: 移除挖掘疲劳与饥饿, 只留 3 种(减少滞留效果)
                cloud.addEffect(new MobEffectInstance(MobEffects.HUNGER, 100, 0));`, '③ 效果精简');
rep('src/main/java/net/farmingtales/ftboss/entity/RainbowPoopEntity.java',
    'cloud.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 140, 1));',
    'cloud.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 0));', '③ 缓慢下调');
rep('src/main/java/net/farmingtales/ftboss/entity/RainbowPoopEntity.java',
    'cloud.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 0));',
    'cloud.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 110, 0));', '③ 虚弱下调');

/* ⑤ 去除激光 */
rep(CB, '            if (this.laserTicks < 0 && this.diveTicks <= 0 && this.laserCd > 0 && --this.laserCd <= 0) {',
        '            if (false && this.laserTicks < 0 && this.diveTicks <= 0 && this.laserCd > 0 && --this.laserCd <= 0) {   // 2026-09-12 按用户要求: 去除激光效果', '⑤ 去激光');

/* ⑥ 控制魔法 */
rep(CB, `            String[] pool = { "irons_spellbooks:fireball", "irons_spellbooks:magic_missile", "irons_spellbooks:ice_block",
                    "irons_spellbooks:lightning_bolt", "irons_spellbooks:poison_arrow", "irons_spellbooks:acid_orb" };`,
`            /* 2026-09-12 按用户要求: 改用**控制系**铁魔法(吸附/冻结/压制) */
            String[] pool = { "irons_spellbooks:black_hole", "irons_spellbooks:ice_block",
                    "irons_spellbooks:thunderstorm", "irons_spellbooks:magic_missile" };`, '⑥ 控制魔法池');
rep(CB, '            net.farmingtales.ftboss.integ.WizardSummon.castByName(spl, this, this.getTarget(), id, 2 + this.getPhase());',
        '            net.farmingtales.ftboss.integ.WizardSummon.castByName(spl, this, this.getTarget(), id, 2 + this.getPhase());   // 控制魔法等级 2+阶段', '⑥ 控制魔法等级');
console.log('完成');
