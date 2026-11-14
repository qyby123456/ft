/* ============================================================================
 *  v77 「把全局倍率烘焙进数据」批量改写(2026-09-13)
 *
 *  背景: v69/v72 起 Boss→玩家 的伤害是在 `BossDamageTuning.onLivingHurt` 钩子里**运行时**乘
 *  (全局 ×0.5, 彩蛋九人再 ×4/3 → 彩蛋系净 ×2/3)。用户要求"把全局修改过的数据直接写进数据里",
 *  少一次逐次计算。于是:
 *    · 彩蛋系(0~10 + 其投射物) -> 把净倍率 2/3 折进 `BOSS_DAMAGE_MULT`(2.0 -> 4/3), 一条就够;
 *    · 非彩蛋系(食物系 Boss/召唤物/道具) -> 逐个把数字乘 0.5(近战 ATTACK_DAMAGE 也一起);
 *    · 然后**删掉运行时钩子**。
 *  本脚本把上面这些"数字"改成烘焙后的值, 每条都断言"恰好命中 1 次"(同一处出现多次的单独标注),
 *  任何一条不匹配都会打印出来并以非 0 退出 —— 不会静默漏改。
 *
 *  用法: node tools/bake_global_damage.cjs
 * ==========================================================================*/
const fs = require('fs');
const path = require('path');

const ROOT = path.join(__dirname, '..');
const SRC = path.join(ROOT, 'src', 'main', 'java', 'net', 'farmingtales', 'ftboss');

/** [文件, 旧文本, 新文本, 期望出现次数(默认1)] */
const EDITS = [
    /* ---------- A. 彩蛋系: 净倍率折进 BOSS_DAMAGE_MULT ---------- */
    ['entity/EasterBossEntity.java',
        `    /* ==================== 全局平衡倍率(2026-09-12 v23 用户定案) ====================
       以后调平衡只改这三个数即可:
         WEAPON_DAMAGE_MULT  —— 武器(近战)伤害倍率: **x5** (ATTACK_DAMAGE 9 -> 45)
         SKILL_MULT          —— 技能效果倍率:     **x3** (技能伤害 x3 且技能减益时长 x3)
         SKILL_RADIUS_MULT   —— 技能范围倍率:     **x1.5**(只放大半径, 不跟着 x3,
                                免得断罪斩半径 15 格/毒雾 13 格那样一刀覆盖全场; 要更狠就把这个也调大) */
    public static final double WEAPON_DAMAGE_MULT = 5.0;
    public static final double SKILL_MULT = 3.0;
    public static final double SKILL_RADIUS_MULT = 1.5;
    /** 2026-09-12 v29 按用户要求: **全部 Boss 伤害再翻倍**(近战/技能/投射物/践踏 一起 ×2) */
    public static final double BOSS_DAMAGE_MULT = 2.0;`,
        `    /* ==================== 全局平衡倍率(2026-09-12 v23; 2026-09-13 v77 烘焙) ====================
       调平衡只改这几个数即可:
         WEAPON_DAMAGE_MULT  —— 武器(近战)伤害倍率: **x5** (ATTACK_DAMAGE 9 -> 45)
         SKILL_MULT          —— 技能效果倍率:     **x3** (技能伤害 x3 且技能减益时长 x3)
         SKILL_RADIUS_MULT   —— 技能范围倍率:     **x1.5**(只放大半径, 不跟着 x3)
         BOSS_DAMAGE_MULT    —— 基础倍率 **x2**, **并且已经包含**"Boss→玩家 全局减半(×0.5)+ 彩蛋九人 +1/3(×4/3)"
                                这一道全局修正: 2.0 × (0.5 × 4/3) = **4/3**。
                                => 下面所有「× BOSS_DAMAGE_MULT」出来的数字**就是玩家实际吃到的伤害**,
                                   不再依赖运行时事件钩子(钩子已删除, 见 BossDamageTuning 注释)。 */
    public static final double WEAPON_DAMAGE_MULT = 5.0;
    public static final double SKILL_MULT = 3.0;
    public static final double SKILL_RADIUS_MULT = 1.5;
    public static final double BOSS_DAMAGE_MULT = 4.0 / 3.0;`],
    /* 折耳根的"虚空那一半"旧版不吃全局倍率 -> 补回 1.5 保持原来的实际值 */
    ['entity/EasterBossEntity.java',
        `            float voidPart = (float) (total * SHADOW_VOID_HALF);`,
        `            /* 虚空伤害没有攻击者实体 -> 旧版全局钩子**不会**给它打折, 所以这里补回 1.5 倍,
               保持和"钩子时代"完全一样的实际数值 */
            float voidPart = (float) (total * SHADOW_VOID_HALF * 1.5);`],

    /* ---------- B. 彩蛋系投射物 ---------- */
    ['entity/EasterPearlEntity.java', `public static final float LAND_DAMAGE = 45.0f;`,
        `public static final float LAND_DAMAGE = 30.0f;      // 45 → ×2/3(已含全局修正, v77)`],
    ['entity/QingyiTridentEntity.java', `target.hurt(this.damageSources().indirectMagic(this, this.getOwner()), 8.0f);`,
        `target.hurt(this.damageSources().indirectMagic(this, this.getOwner()), 5.33f);   // 8 → ×2/3(v77)`],
    ['entity/FlywheelProjectileEntity.java', `public static final float DAMAGE = 16.0f;`,
        `public static final float DAMAGE = 10.67f;      // 16 → ×2/3(已含全局修正, v77)`],
    ['entity/FlywheelProjectileEntity.java', `public static final float EXTRA_MAGIC = 2.0f;`,
        `public static final float EXTRA_MAGIC = 1.33f;  // 2 → ×2/3(v77; 这一下带攻击者, 会吃全局修正)`],

    /* ---------- C. 非彩蛋系: 数字 ×0.5 ---------- */
    /* 近战 ATTACK_DAMAGE */
    ['entity/AppleBossEntity.java', `.add(Attributes.ATTACK_DAMAGE, 7.0)`, `.add(Attributes.ATTACK_DAMAGE, 3.5)`],
    ['entity/AppleMothEntity.java', `.add(Attributes.ATTACK_DAMAGE, 3.0)`, `.add(Attributes.ATTACK_DAMAGE, 1.5)`],
    ['entity/AppleWormEntity.java', `.add(Attributes.ATTACK_DAMAGE, 4.0)`, `.add(Attributes.ATTACK_DAMAGE, 2.0)`],
    ['entity/CoinManEntity.java', `.add(Attributes.ATTACK_DAMAGE, 5.0)`, `.add(Attributes.ATTACK_DAMAGE, 2.5)`],
    ['entity/CompostBossEntity.java', `.add(Attributes.ATTACK_DAMAGE, 8.0)`, `.add(Attributes.ATTACK_DAMAGE, 4.0)`],
    ['entity/DefectCoreEntity.java', `.add(Attributes.ATTACK_DAMAGE, 18.0)`, `.add(Attributes.ATTACK_DAMAGE, 9.0)`],
    ['entity/FlyMinionEntity.java', `.add(Attributes.ATTACK_DAMAGE, 3.0)`, `.add(Attributes.ATTACK_DAMAGE, 1.5)`],
    ['entity/GiantGrubEntity.java', `.add(Attributes.ATTACK_DAMAGE, 9.0)`, `.add(Attributes.ATTACK_DAMAGE, 4.5)`],
    ['entity/GingerbreadMinionEntity.java', `.add(Attributes.ATTACK_DAMAGE, 6.0)`, `.add(Attributes.ATTACK_DAMAGE, 3.0)`],
    ['entity/GreenAppleEntity.java', `.add(Attributes.ATTACK_DAMAGE, 3.0)`, `.add(Attributes.ATTACK_DAMAGE, 1.5)`],
    ['entity/MaggotMinionEntity.java', `.add(Attributes.ATTACK_DAMAGE, 8.0)`, `.add(Attributes.ATTACK_DAMAGE, 4.0)`],
    ['entity/MeatMountainEntity.java', `.add(Attributes.ATTACK_DAMAGE, 11.0)`, `.add(Attributes.ATTACK_DAMAGE, 5.5)`],
    ['entity/PoopMinionEntity.java', `.add(Attributes.ATTACK_DAMAGE, 4.0)`, `.add(Attributes.ATTACK_DAMAGE, 2.0)`],
    ['entity/RatBossEntity.java', `.add(Attributes.ATTACK_DAMAGE, 9.0)`, `.add(Attributes.ATTACK_DAMAGE, 4.5)`],
    ['entity/RottenAppleEntity.java', `.add(Attributes.ATTACK_DAMAGE, 4.0)`, `.add(Attributes.ATTACK_DAMAGE, 2.0)`],
    ['entity/SmallBarrelEntity.java', `.add(Attributes.ATTACK_DAMAGE, 6.0)`, `.add(Attributes.ATTACK_DAMAGE, 3.0)`],
    ['entity/SugarQueenEntity.java', `.add(Attributes.ATTACK_DAMAGE, 3.0)`, `.add(Attributes.ATTACK_DAMAGE, 1.5)`],
    ['entity/TeaMinionEntity.java', `.add(Attributes.ATTACK_DAMAGE, 3.0)`, `.add(Attributes.ATTACK_DAMAGE, 1.5)`],
    ['entity/TeaTreeBossEntity.java', `.add(Attributes.ATTACK_DAMAGE, 7.0)`, `.add(Attributes.ATTACK_DAMAGE, 3.5)`],
    ['entity/WineBarrelBossEntity.java', `.add(Attributes.ATTACK_DAMAGE, 11.0)`, `.add(Attributes.ATTACK_DAMAGE, 5.5)`],

    /* 显式 hurt 数字 */
    ['entity/AppleBossEntity.java', `            p.hurt(this.damageSources().mobAttack(this), 6.0f);`,
        `            p.hurt(this.damageSources().mobAttack(this), 3.0f);            // 6 → ×0.5(v77)`],
    ['entity/AppleBossEntity.java', `                p.hurt(this.damageSources().mobAttack(this), 20.0f + this.getPhase() * 3.0f);`,
        `                p.hurt(this.damageSources().mobAttack(this), 10.0f + this.getPhase() * 1.5f);   // 20+3p → ×0.5(v77)`],
    ['entity/AppleBossEntity.java', `            p.hurt(this.damageSources().mobAttack(this), (float) (60.0 * Math.max(0.4, 1.0 - d / R)));`,
        `            p.hurt(this.damageSources().mobAttack(this), (float) (30.0 * Math.max(0.4, 1.0 - d / R)));   // 60 → ×0.5(v77)`],
    ['entity/AppleBossEntity.java', `            p.hurt(this.damageSources().explosion(this, this), (float) (base * Math.max(0.25, 1.0 - d / R)));`,
        `            /* v77: base 是"设计值", 这里直接乘 0.5 得到玩家实际吃到的(原来由全局钩子做) */
            p.hurt(this.damageSources().explosion(this, this), (float) (base * 0.5 * Math.max(0.25, 1.0 - d / R)));`],
    ['entity/AppleBossEntity.java', `                p.hurt(this.damageSources().mobAttack(this),
                        (float) (14.0 * Math.max(0.35, 1.0 - pd / 3.5)));`,
        `                p.hurt(this.damageSources().mobAttack(this),
                        (float) (7.0 * Math.max(0.35, 1.0 - pd / 3.5)));   // 14 → ×0.5(v77)`],
    ['entity/AppleBossEntity.java', `            p.hurt(this.damageSources().mobAttack(this), dmg);`,
        `            p.hurt(this.damageSources().mobAttack(this), dmg * 0.5f);      // ×0.5(v77)`],
    ['entity/AppleCloneEntity.java', `                p.hurt(this.damageSources().explosion(this, this), (float) (base * Math.max(0.25, 1.0 - d / R)));`,
        `                p.hurt(this.damageSources().explosion(this, this), (float) (base * 0.5 * Math.max(0.25, 1.0 - d / R)));   // ×0.5(v77)`],
    ['entity/CompostBossEntity.java', `                                lp.hurt(this.damageSources().mobAttack(this), 9.0f + this.getPhase() * 2.0f);`,
        `                                lp.hurt(this.damageSources().mobAttack(this), 4.5f + this.getPhase() * 1.0f);   // ×0.5(v77)`],
    ['entity/CompostBossEntity.java', `                        dp.hurt(this.damageSources().mobAttack(this), 18.0f + this.getPhase() * 4.0f);`,
        `                        dp.hurt(this.damageSources().mobAttack(this), 9.0f + this.getPhase() * 2.0f);   // ×0.5(v77)`],
    ['entity/CompostBossEntity.java', `                p.hurt(this.damageSources().mobAttack(this), 16.0f + this.getPhase() * 3.0f);`,
        `                p.hurt(this.damageSources().mobAttack(this), 8.0f + this.getPhase() * 1.5f);   // ×0.5(v77)`],
    ['entity/TeaTreeBossEntity.java', `        target.hurt(this.damageSources().indirectMagic(this, this), 5.0f);`,
        `        target.hurt(this.damageSources().indirectMagic(this, this), 2.5f);   // 5 → ×0.5(v77)`],
    ['entity/SheepDungEntity.java', `            p.hurt(this.damageSources().mobAttack(this), 5.0F);`,
        `            p.hurt(this.damageSources().mobAttack(this), 2.5F);   // 5 → ×0.5(v77)`],
    ['entity/RottenAppleEntity.java', `            p.hurt(this.damageSources().mobAttack(this), 6.0F);`,
        `            p.hurt(this.damageSources().mobAttack(this), 3.0F);   // 6 → ×0.5(v77)`],
    ['entity/RainbowPoopEntity.java', `    private float dmg = 6.0f;`,
        `    private float dmg = 3.0f;      // 6 → ×0.5(已含全局修正, v77)`],
    ['boss/SugarQueenBoss.java', `        final float dmg = 16f;`,
        `        final float dmg = 8f;      // 16 → ×0.5(已含全局修正, v77)`],
    ['boss/ai/BarrelTactics.java', `            if (p.hurt(e.damageSources().mobAttack(e), 9.5f)) {`,
        `            if (p.hurt(e.damageSources().mobAttack(e), 4.75f)) {   // ×0.5(v77)`],
    ['boss/ai/BarrelTactics.java', `            if (p.hurt(e.damageSources().mobAttack(e), 6.0f)) {`,
        `            if (p.hurt(e.damageSources().mobAttack(e), 3.0f)) {    // ×0.5(v77)`],
    ['boss/ai/BarrelTactics.java', `            if (p.hurt(e.damageSources().mobAttack(e), dmg)) {`,
        `            if (p.hurt(e.damageSources().mobAttack(e), dmg * 0.5f)) {   // ×0.5(v77)`],
    /* 故障机器人的球/光束: 原来"技能自身 50% 拆分 × 全局 0.5" = ×0.25 */
    ['boss/ai/DefectOrbs.java', `        t.hurt(sl.damageSources().mobAttack(this.boss), (dmg) * 0.5f);   // 物理`,
        `        t.hurt(sl.damageSources().mobAttack(this.boss), (dmg) * 0.25f);   // 物理(技能半 × 全局 0.5 = 0.25, v77)`, 2],
    ['boss/ai/DefectOrbs.java', `        t.hurt(sl.damageSources().indirectMagic(this.boss, this.boss), (dmg) * 0.5f);   // 魔法(挂上攻击者, 便于全局减半)`,
        `        t.hurt(sl.damageSources().indirectMagic(this.boss, this.boss), (dmg) * 0.25f);   // 魔法(同上, v77)`, 2],
    ['boss/ai/DefectOrbs.java', `        p.hurt(sl.damageSources().mobAttack(this.boss), (net.farmingtales.ftboss.boss.ai.BossScaling.dmg(this.boss, 20f * this.orbDamageMul)) * 0.5f);   // 物理`,
        `        p.hurt(sl.damageSources().mobAttack(this.boss), (net.farmingtales.ftboss.boss.ai.BossScaling.dmg(this.boss, 20f * this.orbDamageMul)) * 0.25f);   // 物理(v77)`],
    ['boss/ai/DefectOrbs.java', `        p.hurt(sl.damageSources().indirectMagic(this.boss, this.boss), (net.farmingtales.ftboss.boss.ai.BossScaling.dmg(this.boss, 20f * this.orbDamageMul)) * 0.5f);   // 魔法`,
        `        p.hurt(sl.damageSources().indirectMagic(this.boss, this.boss), (net.farmingtales.ftboss.boss.ai.BossScaling.dmg(this.boss, 20f * this.orbDamageMul)) * 0.25f);   // 魔法(v77)`],
    ['boss/ai/DefectOrbs.java', `        target.hurt(sl.damageSources().mobAttack(this.boss), (net.farmingtales.ftboss.boss.ai.BossScaling.dmg(this.boss, 30f * this.orbDamageMul)) * 0.5f);   // 物理`,
        `        target.hurt(sl.damageSources().mobAttack(this.boss), (net.farmingtales.ftboss.boss.ai.BossScaling.dmg(this.boss, 30f * this.orbDamageMul)) * 0.25f);   // 物理(v77)`],
    ['boss/ai/DefectOrbs.java', `        target.hurt(sl.damageSources().indirectMagic(this.boss, this.boss), (net.farmingtales.ftboss.boss.ai.BossScaling.dmg(this.boss, 30f * this.orbDamageMul)) * 0.5f);   // 魔法`,
        `        target.hurt(sl.damageSources().indirectMagic(this.boss, this.boss), (net.farmingtales.ftboss.boss.ai.BossScaling.dmg(this.boss, 30f * this.orbDamageMul)) * 0.25f);   // 魔法(v77)`],
    ['entity/ChargedOrbEntity.java', `        target.hurt(sl.damageSources().mobAttack(boss), dmg * 0.5f);      // ① 物理`,
        `        target.hurt(sl.damageSources().mobAttack(boss), dmg * 0.25f);      // ① 物理(技能半 × 全局 0.5, v77)`],
    ['entity/ChargedOrbEntity.java', `        target.hurt(sl.damageSources().indirectMagic(boss, boss), dmg * 0.5f);              // ② 魔法(挂攻击者, 便于全局减半)`,
        `        target.hurt(sl.damageSources().indirectMagic(boss, boss), dmg * 0.25f);              // ② 魔法(v77)`],
    ['entity/ChargedOrbEntity.java', `            p.hurt(sl.damageSources().mobAttack(boss), dmg * 0.3f);       // ① 物理(溅射)`,
        `            p.hurt(sl.damageSources().mobAttack(boss), dmg * 0.15f);       // ① 物理(溅射, v77)`],
    ['entity/ChargedOrbEntity.java', `            p.hurt(sl.damageSources().indirectMagic(boss, boss), dmg * 0.3f);               // ② 魔法(溅射)`,
        `            p.hurt(sl.damageSources().indirectMagic(boss, boss), dmg * 0.15f);               // ② 魔法(溅射, v77)`],
    ['entity/DefectCoreEntity.java', `                float dmg = 9f + this.phase * 3f;`,
        `                float dmg = 4.5f + this.phase * 1.5f;      // ×0.5(v77)`],
    /* 爆炸威力(原来爆炸伤害也吃全局 0.5; 威力近似线性, 直接对半) */
    ['entity/CompostBossEntity.java', `        sl.explode(this, x, y, z, 3.2f, net.minecraft.world.level.Level.ExplosionInteraction.NONE);`,
        `        sl.explode(this, x, y, z, 1.6f, net.minecraft.world.level.Level.ExplosionInteraction.NONE);   // 3.2 → ×0.5(v77)`],
    ['entity/DefectCoreEntity.java', `    public static final float OVERLOAD_POWER = 8.0F;`,
        `    public static final float OVERLOAD_POWER = 4.0F;    // 8 → ×0.5(v77)`],
    ['entity/SheepDungEntity.java', `    private static final float BLAST_RADIUS = 1.9F;`,
        `    private static final float BLAST_RADIUS = 0.95F;    // 1.9 → ×0.5(v77)`],
    ['entity/RottenAppleEntity.java', `    private static final float BLAST_RADIUS = 2.2F;`,
        `    private static final float BLAST_RADIUS = 1.1F;     // 2.2 → ×0.5(v77)`],
    ['entity/SmallBarrelEntity.java', `    private static final float BLAST = 2.4f;`,
        `    private static final float BLAST = 1.2f;            // 2.4 → ×0.5(v77)`],
    ['entity/BarrelCannonballEntity.java', `    public static final float EXPLOSION_POWER = 6.0F;`,
        `    public static final float EXPLOSION_POWER = 3.0F;   // 6 → ×0.5(v77; 爆炸伤害近似线性)`],
    ['entity/BarrelCannonballEntity.java', `    private static final float DIRECT_DAMAGE = 8.0F;`,
        `    private static final float DIRECT_DAMAGE = 4.0F;    // 8 → ×0.5(v77)`],

    /* ---------- D. 光束四分之一份: 去掉手动 0.5, 直接四份都取 1/8 ---------- */
    ['boss/ai/DefectOrbs.java', `            float bQuarter = bTotal / 4f;`,
        `            float bQuarter = bTotal / 8f;      // 四份 × 全局 0.5(v77)`],
    ['boss/ai/DefectOrbs.java', `            float bQuarterRaw = bQuarter * (float) BossDamageTuning.DAMAGE_TO_PLAYER_MULT;\n`, ``],
    ['boss/ai/DefectOrbs.java', `            le.hurt(sl.damageSources().fellOutOfWorld(), bQuarterRaw);        // ③ 虚空(手动减半)`,
        `            le.hurt(sl.damageSources().fellOutOfWorld(), bQuarter);           // ③ 虚空`],
    ['boss/ai/DefectOrbs.java', `            le.hurt(beamDamageSource(sl), bQuarterRaw);                       // ④ 真实伤害(armamentarium, 手动减半)`,
        `            le.hurt(beamDamageSource(sl), bQuarter);                          // ④ 真实伤害(armamentarium)`],

    /* ---------- E. 庄园重击: 无攻击者那支保留旧有效值 ---------- */
    ['boss/ManorFight.java', `            float slam = src == null ? SLAM_DAMAGE * (float) net.farmingtales.ftboss.boss.ai.BossDamageTuning.DAMAGE_TO_PLAYER_MULT : SLAM_DAMAGE;`,
        `            /* v77: 有攻击者(src)的那支 = SLAM_DAMAGE(数值里已含全局修正);
               无攻击者的 magic 那支旧版不吃修正, 保持旧有效值 25 */
            float slam = src == null ? 25.0f : SLAM_DAMAGE;`],
];

let bad = 0;
for (const [rel, oldText, newText, wantRaw] of EDITS) {
    const want = wantRaw === undefined ? 1 : wantRaw;
    const file = path.join(SRC, rel);
    let txt = fs.readFileSync(file, 'utf8');
    const count = txt.split(oldText).length - 1;
    if (count !== want) {
        console.log(`!! 命中 ${count} 次(期望 ${want}): ${rel} :: ${oldText.slice(0, 70).replace(/\n/g, '\\n')}`);
        bad++;
        continue;
    }
    txt = txt.split(oldText).join(newText);
    fs.writeFileSync(file, txt);
    console.log(`ok  ${rel}  (x${want})  ${oldText.slice(0, 60).replace(/\n/g, '\\n')}`);
}
console.log(bad === 0 ? '\n全部改写成功' : `\n有 ${bad} 条没匹配上, 需要人工处理`);
process.exit(bad === 0 ? 0 : 1);
