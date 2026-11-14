/*
 * 2026-09-12 按用户要求: Boss 攻速**每命中玩家一次就提升**, 3 秒内未命中则清零
 *   · hitStreak: 命中层数(上限 12)
 *   · lastHitTick: 最后一次命中玩家的 tick; 超过 60t(3 秒) 归零
 *   · 连射冷却 = max(16, 55 - 层数*4)  (层数越高越快)
 *   · 发间间隔 = max(1, 3 - 层数/4)
 *   · 命中来源: 投掷物 onHitEntity(仅当目标是玩家) -> 回调 boss.noteHitOnPlayer()
 */
const fs = require('fs');
const path = require('path');
const ROOT = process.argv[2];
const read = p => fs.readFileSync(path.join(ROOT, p), 'utf8');
const write = (p, s) => fs.writeFileSync(path.join(ROOT, p), s);
function rep(rel, a, b, label) {
    const s = read(rel);
    if (s.includes(b.trim().split('\n')[0]) && s.includes(b.trim().split('\n').slice(-1)[0])) { console.log('  skip ' + label); return; }
    if (!s.includes(a)) { console.log('  FAIL ' + label); return; }
    write(rel, s.replace(a, b));
    console.log('  OK   ' + label);
}
const CB = 'src/main/java/net/farmingtales/ftboss/entity/CompostBossEntity.java';
/* 字段 */
rep(CB, '    private int gasFuse = -1;',
    `    private int gasFuse = -1;
    /** attack speed stacking: +1 per hit on a player, reset after 3s without a hit */
    private int hitStreak = 0;
    private int lastHitTick = -1000;`, '攻速叠层字段');
/* 命中回调 + 衰减 */
rep(CB, '    /** lobbed poop: upward arc toward the target */',
    `    /** called by the projectile when it hits a player */
    public void noteHitOnPlayer() {
        this.hitStreak = Math.min(12, this.hitStreak + 1);
        this.lastHitTick = this.tickCount;
    }

    /** 0..12 */
    public int getHitStreak() {
        return this.hitStreak;
    }

    /** lobbed poop: upward arc toward the target */`, '命中回调');
/* tick 里衰减 + 冷却公式 */
rep(CB, '        // SKILL: lobbed poop (3 shots, upward arc)',
    `        // attack speed decay: no hit on a player for 3s -> reset
        if (this.tickCount - this.lastHitTick > 60) this.hitStreak = 0;

        // SKILL: lobbed poop (3 shots, upward arc)`, '衰减');
rep(CB, '            this.burstCd = 55 + this.random.nextInt(25);   // 2026-09-12: 攻速提高(100~160 -> 55~80)',
    `            /* 攻速叠层: 每命中一次玩家就更快(max 12 层), 3 秒未命中回到基础速度 */
            this.burstCd = Math.max(16, 55 - this.hitStreak * 4) + this.random.nextInt(12);`, '冷却公式');
rep(CB, '                this.burstGap = 3;',
    `                this.burstGap = Math.max(1, 3 - this.hitStreak / 4);`, '发间间隔');
/* 投掷物回调 */
rep('src/main/java/net/farmingtales/ftboss/entity/RainbowPoopEntity.java',
    `            le.hurt(this.damageSources().thrown(this, owner), this.dmg);`,
    `            le.hurt(this.damageSources().thrown(this, owner), this.dmg);
            /* 2026-09-12: 命中玩家 -> 通知 Boss 提升攻速(3 秒内未命中会清零) */
            if (le instanceof Player && this.getOwner() instanceof net.farmingtales.ftboss.entity.CompostBossEntity cb) {
                cb.noteHitOnPlayer();
            }`, '投掷物回调');
console.log('完成');
