/*
 * 2026-09-12 一批修改:
 *  ① 删除"糖果石像"(通用石像): 从创造栏/交易/资源文件/语言条目移除(保留注册以免破坏其它引用)
 *  ② Boss 攻速: 连射间隔 100~160t -> 55~80t; 发间间隔 4t -> 3t
 *  ③ 命中率: 弹道散布 3.0 -> 0.6, 并按目标速度做**预判**(提前量)
 *  ④ 肥水坑: debuff 从 2 种加到 **5 种**(缓慢/中毒/虚弱/饥饿/挖掘疲劳)
 *  ⑤ 攻击抬手: 每轮连射前留 **12t 抬炮**(cannon_fire 动画) 再开火
 *  ⑥ 去燃烧动画: displayFireAnimation() -> false
 */
const fs = require('fs');
const path = require('path');
const ROOT = process.argv[2];
const J = path.join(ROOT, 'src/main/java/net/farmingtales/ftboss');
const A = path.join(ROOT, 'src/main/resources/assets/ftboss');
const D = path.join(ROOT, 'src/main/resources/data/ftboss');
const rw = (p, fn) => { if (!fs.existsSync(p)) { console.log('  缺文件 ' + p); return false; } let s = fs.readFileSync(p, 'utf8'); const o = s; s = fn(s); if (s !== o) { fs.writeFileSync(p, s); return true; } return false; };
const rep = (p, a, b, label) => console.log('  ' + (rw(p, s => s.includes(b) ? s : s.replace(a, b)) ? 'OK  ' : 'skip') + ' ' + label);

/* ① 糖果石像: 可见处移除 + 资源删除 */
rep(path.join(J, 'item/FTCreativeTabs.java'),
    `                out.accept(net.farmingtales.ftboss.block.FTBlocks.STATUE.get().asItem());\n`, '', '创造栏移除糖果石像');
{
    const p = path.join(J, 'trade/TradeHandler.java');
    const ok = rw(p, s => s.split('\n').filter(l => !l.includes('ftboss:statue')).join('\n'));
    console.log('  ' + (ok ? 'OK  ' : 'skip') + ' 交易移除糖果石像');
}
for (const f of ['blockstates/statue.json', 'models/block/statue_item.json', 'loot_tables/blocks/statue.json']) {
    const p = path.join(f.startsWith('loot') ? D : A, f.replace('loot_tables/', ''));
    if (fs.existsSync(p)) { fs.unlinkSync(p); console.log('  删除资源 ' + p.replace(ROOT, '')); }
}
for (const f of ['zh_cn.json', 'en_us.json']) {
    const p = path.join(A, 'lang', f);
    const j = JSON.parse(fs.readFileSync(p, 'utf8'));
    delete j['block.ftboss.statue']; delete j['item.ftboss.statue'];
    fs.writeFileSync(p, JSON.stringify(j, null, 2));
}
console.log('  OK   lang 移除 statue 条目');

/* ②③⑤⑥ Boss 调优 */
const cb = path.join(J, 'entity/CompostBossEntity.java');
rep(cb, `            this.burstCd = 100 + this.random.nextInt(60);`, `            this.burstCd = 55 + this.random.nextInt(25);   // 2026-09-12: 攻速提高(100~160 -> 55~80)`, '攻速');
rep(cb, `                this.burstGap = 4;`, `                this.burstGap = 3;`, '发间间隔');
rep(cb, `            this.burstGap = 1;`, `            this.burstGap = 12;   // 开火前 12t 抬炮(可见的抬手动作)`, '抬手');
rep(cb, `            poop.shoot(dx / len, dy / len + 0.12, dz / len, 1.25f, 3.0f);`,
    `            /* 预判: 按目标当前速度提前量, 并把散布 3.0 -> 0.6(命中率大幅提高) */
            double lead = Math.min(1.0, len / 24.0);
            dx += t.getDeltaMovement().x * lead * 12.0;
            dy += t.getDeltaMovement().y * lead * 6.0;
            dz += t.getDeltaMovement().z * lead * 12.0;
            double len2 = Math.max(0.1, Math.sqrt(dx * dx + dy * dy + dz * dz));
            poop.shoot(dx / len2, dy / len2 + 0.10, dz / len2, 1.35f, 0.6f);`, '命中率/预判');
rep(cb, `    @Override
    public boolean removeWhenFarAway(double distance) {`,
    `    /** 2026-09-12 按用户要求: 本体**不显示火焰燃烧动画**(伤害照旧) */
    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {`, '去燃烧动画');

/* ④ 肥水坑 debuff 加到 5 种 */
rep(path.join(J, 'entity/RainbowPoopEntity.java'),
    `                cloud.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0));`,
    `                cloud.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 0));
                cloud.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
                cloud.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 100, 0));
                cloud.addEffect(new MobEffectInstance(MobEffects.HUNGER, 120, 0));`, '水池 debuff 5 种');
/* 水池持续时间也更长一点 */
rep(path.join(J, 'entity/RainbowPoopEntity.java'), `                cloud.setDuration(160);                 // 8 秒`, `                cloud.setDuration(200);                 // 10 秒`, '水池时长');
console.log('完成');
