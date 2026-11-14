/* 收尾: ① 先拷基础模型再跑上色 ② 补 blockstate(必要时从机器人石像改写) ③ 校验并补 6 处挂钩 */
const fs = require('fs');
const path = require('path');
const { spawnSync } = require('child_process');
const ROOT = process.argv[2];
const J = 'src/main/java/net/farmingtales/ftboss';
const A = path.join(ROOT, 'src/main/resources/assets/ftboss');
const D = path.join(ROOT, 'src/main/resources/data/ftboss');
const read = p => fs.readFileSync(path.join(ROOT, p), 'utf8');
const write = (p, s) => { fs.mkdirSync(path.dirname(path.join(ROOT, p)), { recursive: true }); fs.writeFileSync(path.join(ROOT, p), s); };
const cp = (from, sub) => {
    const dir = path.join(ROOT, 'tools/model3d', from, sub);
    if (!fs.existsSync(dir)) return 0;
    let n = 0;
    for (const f of fs.readdirSync(dir)) {
        if (!/^statue_compost/.test(f)) continue;
        const dst = path.join(A, sub, f);
        fs.mkdirSync(path.dirname(dst), { recursive: true });
        fs.copyFileSync(path.join(dir, f), dst);
        n++;
    }
    return n;
};
/* ① 基础模型先进资源目录 */
console.log('  ① 几何产物 -> 资源: ' + (cp('out_statues_real', 'models/block') + cp('out_statues_real', 'textures/block')));
/* ② 跑上色 */
const r = spawnSync('node', ['gen_statue_skins.cjs'], { cwd: path.join(ROOT, 'tools/model3d'), stdio: 'inherit' });
console.log('  ② gen_statue_skins 退出码: ' + r.status);
/* ③ 上色产物 */
console.log('  ③ 上色产物 -> 资源: ' + (cp('out_statues', 'models/block') + cp('out_statues', 'textures/block') + cp('out_statues', 'blockstates') + cp('out_statue_skins', 'models/block') + cp('out_statue_skins', 'textures/block') + cp('out_statue_skins', 'blockstates')));
/* ④ blockstate 兜底: 从机器人石像改写 */
{
    const dst = path.join(A, 'blockstates/statue_compost.json');
    if (!fs.existsSync(dst)) {
        const src = path.join(A, 'blockstates/statue_defect.json');
        if (fs.existsSync(src)) {
            fs.writeFileSync(dst, fs.readFileSync(src, 'utf8').split('statue_defect').join('statue_compost'));
            console.log('  ④ blockstate: 已从机器人石像改写生成');
        } else console.log('  ④ ❌ 连模板都没有');
    } else console.log('  ④ blockstate 已存在');
}
/* ⑤ 资源清单 */
for (const f of ['models/block/statue_compost_lower.json', 'models/block/statue_compost_upper.json', 'models/block/statue_compost_lower_painted.json', 'models/block/statue_compost_upper_painted.json', 'textures/block/statue_compost.png', 'blockstates/statue_compost.json', 'loot_tables/blocks/statue_compost.json']) {
    const p = f.startsWith('loot') ? path.join(D, f) : path.join(A, f);
    console.log('    ' + (fs.existsSync(p) ? 'OK  ' : '缺失') + ' ' + f + (fs.existsSync(p) ? '  ' + fs.statSync(p).size + ' B' : ''));
}
/* ⑥ 校验/补 6 处挂钩 */
function ensure(rel, a, b, label) {
    const s = read(rel);
    if (s.includes(b.trim().split('\n').filter(x => x.trim())[0]) && s.includes(b.trim().split('\n').filter(x => x.trim()).pop())) { console.log('  skip ' + label); return; }
    if (!s.includes(a)) { console.log('  FAIL ' + label); return; }
    write(rel, s.replace(a, b));
    console.log('  OK   ' + label);
}
ensure(`${J}/block/FTBlocks.java`,
    `    public static final RegistryObject<Block> STATUE_DEFECT = statue("statue_defect", 6);`,
    `    public static final RegistryObject<Block> STATUE_DEFECT = statue("statue_defect", 6);
    public static final RegistryObject<Block> STATUE_COMPOST = statue("statue_compost", 7);`, 'FTBlocks 注册');
ensure(`${J}/item/FTCreativeTabs.java`,
    `                out.accept(net.farmingtales.ftboss.block.FTBlocks.STATUE_DEFECT.get().asItem());`,
    `                out.accept(net.farmingtales.ftboss.block.FTBlocks.STATUE_DEFECT.get().asItem());
                out.accept(net.farmingtales.ftboss.block.FTBlocks.STATUE_COMPOST.get().asItem());`, '创造栏');
ensure(`${J}/boss/ai/BossMercy.java`,
    `            if (t == net.farmingtales.ftboss.entity.FTEntities.DEFECT_CORE.get())`,
    `            if (t == net.farmingtales.ftboss.entity.FTEntities.COMPOST_BOSS.get())
                statue = net.farmingtales.ftboss.block.FTBlocks.STATUE_COMPOST.get().asItem();
            else if (t == net.farmingtales.ftboss.entity.FTEntities.DEFECT_CORE.get())`, '石像掉落');
ensure(`${J}/entity/CompostBossEntity.java`,
    `        if (this.level() instanceof ServerLevel sl) BossMusic.release(sl, this);`,
    `        if (this.level() instanceof ServerLevel sl) BossMusic.release(sl, this);
        this.dropFertilizer();`, '肥料掉落调用');
/* lang + 战利品表兜底 */
for (const [f, v] of [['zh_cn.json', '\u77f3\u50cf\u00b7\u70ab\u5f69\u7b54\u8fa9\u8d85\u4eba'], ['en_us.json', 'Statue: Compost Superman']]) {
    const p = path.join(A, 'lang', f);
    const j = JSON.parse(fs.readFileSync(p, 'utf8'));
    j['block.ftboss.statue_compost'] = v;
    fs.writeFileSync(p, JSON.stringify(j, null, 2));
}
{
    const dst = path.join(D, 'loot_tables/blocks/statue_compost.json');
    if (!fs.existsSync(dst)) {
        const tpl = path.join(D, 'loot_tables/blocks/statue_rat_swarm.json');
        if (fs.existsSync(tpl)) { fs.writeFileSync(dst, fs.readFileSync(tpl, 'utf8').split('statue_rat_swarm').join('statue_compost')); console.log('  OK   战利品表(兜底生成)'); }
        else console.log('  FAIL 战利品表模板缺失');
    } else console.log('  skip 战利品表');
}
console.log('完成');
