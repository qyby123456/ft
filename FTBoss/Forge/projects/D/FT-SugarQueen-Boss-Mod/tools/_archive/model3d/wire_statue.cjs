/*
 * ⑤-3 收尾(2026-09-12):
 *  ① gen_statue_real.cjs: 屠夫输入(butcher_preview.geo.json)缺失时**跳过该项**而不是整体崩
 *  ② 跑石像生成器, 只拷贝 statue_compost* 资源
 *  ③ 六处挂钩: FTBlocks 注册 / 战利品表 / lang / 创造栏 / BossMercy 石像掉落 / 本体掉炫彩肥料
 */
const fs = require('fs');
const path = require('path');
const { spawnSync } = require('child_process');
const ROOT = process.argv[2];
const J = 'src/main/java/net/farmingtales/ftboss';
const A = 'src/main/resources/assets/ftboss';
const D = 'src/main/resources/data/ftboss';
const read = p => fs.readFileSync(path.join(ROOT, p), 'utf8');
const write = (p, s) => { fs.mkdirSync(path.dirname(path.join(ROOT, p)), { recursive: true }); fs.writeFileSync(path.join(ROOT, p), s); };
function rep(rel, a, b, label, regex) {
    const s = read(rel);
    if (s.includes(b)) { console.log('  skip ' + label); return true; }
    if (regex) { if (regex.test(s)) { write(rel, s.replace(regex, b)); console.log('  OK   ' + label); return true; } }
    else if (s.includes(a)) { write(rel, s.replace(a, b)); console.log('  OK   ' + label); return true; }
    console.log('  FAIL ' + label);
    return false;
}

/* ① 屠夫输入缺失时跳过 */
{
    const rel = 'tools/model3d/gen_statue_real.cjs';
    let s = read(rel);
    if (s.includes('preview missing')) console.log('  skip 生成器补丁');
    else {
        const re = /else if \(boss\.geo === '__BUTCHER__'\)[^\n]*/;
        const to = "else if (boss.geo === '__BUTCHER__') { const bp = path.join(__dirname, 'out', 'butcher_preview.geo.json'); if (!fs.existsSync(bp)) { console.log('  skip butcher (preview missing)'); continue; } boxes = loadBoxes(bp, boss.skip); }";
        if (re.test(s)) { write(rel, s.replace(re, to)); console.log('  OK   生成器: 屠夫缺失即跳过'); }
        else console.log('  FAIL 找不到屠夫分支');
    }
}
/* ② 跑生成器 + 只拷 statue_compost* */
for (const g of ['gen_statue_real.cjs', 'gen_statue_skins.cjs']) {
    const r = spawnSync('node', [g], { cwd: path.join(ROOT, 'tools/model3d'), stdio: 'inherit' });
    console.log(`  ${g}: 退出码 ${r.status}`);
}
let copied = 0;
for (const base of ['out_statues_real', 'out_statues']) {
    for (const sub of ['models/block', 'blockstates', 'textures/block']) {
        const dir = path.join(ROOT, 'tools/model3d', base, sub);
        if (!fs.existsSync(dir)) continue;
        for (const f of fs.readdirSync(dir)) {
            if (!/^statue_compost/.test(f)) continue;         // 只动自己这座, 绝不覆盖别的石像
            const dst = path.join(ROOT, A, sub, f);
            fs.mkdirSync(path.dirname(dst), { recursive: true });
            fs.copyFileSync(path.join(dir, f), dst);
            copied++;
        }
    }
}
console.log(`  石像资源: ${copied} 个`);
for (const f of ['models/block/statue_compost_lower.json', 'models/block/statue_compost_upper.json', 'models/block/statue_compost_lower_painted.json', 'models/block/statue_compost_upper_painted.json', 'textures/block/statue_compost.png', 'blockstates/statue_compost.json']) {
    const p = path.join(ROOT, A, f);
    console.log('   ' + (fs.existsSync(p) ? 'OK  ' : '缺失') + ' ' + f + (fs.existsSync(p) ? '  ' + fs.statSync(p).size + ' B' : ''));
}

/* ③ 六处挂钩 */
rep(`${J}/block/FTBlocks.java`,
    `    public static final RegistryObject<Block> STATUE_DEFECT = statue("statue_defect", 6);`,
    `    public static final RegistryObject<Block> STATUE_DEFECT = statue("statue_defect", 6);
    /** Compost Superman statue (2026-09-12) */
    public static final RegistryObject<Block> STATUE_COMPOST = statue("statue_compost", 7);`, 'FTBlocks 注册');
try {
    const tpl = path.join(ROOT, D, 'loot_tables/blocks/statue_rat_swarm.json');
    if (fs.existsSync(tpl)) {
        write(`${D}/loot_tables/blocks/statue_compost.json`, fs.readFileSync(tpl, 'utf8').split('statue_rat_swarm').join('statue_compost'));
        console.log('  OK   战利品表');
    } else console.log('  FAIL 战利品表模板缺失');
} catch (e) { console.log('  FAIL 战利品表: ' + e.message); }
for (const [f, v] of [['zh_cn.json', '\u77f3\u50cf\u00b7\u70ab\u5f69\u7b54\u8fa9\u8d85\u4eba'], ['en_us.json', 'Statue: Compost Superman']]) {
    const p = path.join(ROOT, A, 'lang', f);
    const j = JSON.parse(fs.readFileSync(p, 'utf8'));
    j['block.ftboss.statue_compost'] = v;
    fs.writeFileSync(p, JSON.stringify(j, null, 2));
}
console.log('  OK   lang');
rep(`${J}/item/FTCreativeTabs.java`,
    `                out.accept(net.farmingtales.ftboss.block.FTBlocks.STATUE_DEFECT.get().asItem());`,
    `                out.accept(net.farmingtales.ftboss.block.FTBlocks.STATUE_DEFECT.get().asItem());
                out.accept(net.farmingtales.ftboss.block.FTBlocks.STATUE_COMPOST.get().asItem());`, '创造栏');
rep(`${J}/boss/ai/BossMercy.java`,
    `            if (t == net.farmingtales.ftboss.entity.FTEntities.DEFECT_CORE.get())`,
    `            if (t == net.farmingtales.ftboss.entity.FTEntities.COMPOST_BOSS.get())
                statue = net.farmingtales.ftboss.block.FTBlocks.STATUE_COMPOST.get().asItem();
            else if (t == net.farmingtales.ftboss.entity.FTEntities.DEFECT_CORE.get())`, '石像掉落');
/* 本体掉炫彩肥料 */
rep(`${J}/entity/CompostBossEntity.java`,
    `    @Override
    public void die(DamageSource source) {`,
    `    /** 死亡掉落: 3~6 个炫彩肥料(2026-09-12 按用户要求) */
    private void dropFertilizer() {
        try {
            Item fert = net.farmingtales.ftboss.item.FTBossItems.RAINBOW_FERTILIZER.get();
            int n = 3 + this.random.nextInt(4);
            for (int i = 0; i < n; i++) this.spawnAtLocation(new ItemStack(fert));
            FTBossMod.LOGGER.info("[FTBoss] Compost Superman dropped {} rainbow fertilizer", n);
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss] fertilizer drop failed: {}", t.toString());
        }
    }

    @Override
    public void die(DamageSource source) {`, '肥料掉落方法');
rep(`${J}/entity/CompostBossEntity.java`,
    `        if (this.level() instanceof ServerLevel sl) BossMusic.release(sl, this);`,
    `        if (this.level() instanceof ServerLevel sl) BossMusic.release(sl, this);
        this.dropFertilizer();`, '调用肥料掉落');
console.log('完成');
