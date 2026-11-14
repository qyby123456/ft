/*
 * 2026-09-12 修五个问题:
 *  ① 腿与身体叠一起: 腿向外移(±5.6 -> ±6.6), 衣摆抬高收窄(下摆/锯齿/稻草各上移 ~1 格), 让腿露出来
 *  ② 头部贴图闪烁: 层间叠压 0.8 -> 0.98(不再互相穿插), 层边暗圈外移(-0.15 -> -0.3)避免共面
 *  ③ 手持加农炮不显示: CompostRenderer 加 ItemInHandLayer(GeckoLib 实体默认不渲染手持物)
 *  ④ 抬手动画缺骨骼: 动画里引用了 v4 模型**不存在**的 forearm_r / hand_r -> 全部删掉, 并把抬起角度加大(arm_r -58 -> -72)
 *  ⑤ 炮口位置: 保持右手+前方(已实现), 抬炮时炮口更高
 */
const fs = require('fs');
const path = require('path');
const { spawnSync } = require('child_process');
const ROOT = process.argv[2];
const read = p => fs.readFileSync(path.join(ROOT, p), 'utf8');
const write = (p, s) => fs.writeFileSync(path.join(ROOT, p), s);

/* ① ② 模型生成器 */
{
    const rel = 'tools/model3d/gen_compost_boss_v4.cjs';
    let s = read(rel);
    let n = 0;
    const fixes = [
        ['    const cx = sx * 5.6;', '    const cx = sx * 6.6;   // legs pushed outward so the tunic does not swallow them'],
        ["cube('torso', [-8.0, B - 3.6, -4.3], [16.0, 5.0, 8.6], 'burlap');", "cube('torso', [-7.6, B - 2.4, -4.1], [15.2, 4.2, 8.2], 'burlap');"],
        ["for (let i = -3; i <= 3; i++) cube('torso', [i * 2.3 - 0.9, B - 6.4, -4.3], [1.8, 3.0, 1.9], 'burlapDark', [0, 0, i * 14]);",
         "for (let i = -3; i <= 3; i++) cube('torso', [i * 2.3 - 0.9, B - 5.2, -4.1], [1.8, 2.6, 1.9], 'burlapDark', [0, 0, i * 14]);"],
        ["    cube('torso', [i * 2.1 - 0.8, B - 8.6, -3.6], [1.7, 3.2, 1.7], i % 2 ? 'straw' : 'strawDark', [0, 0, i * 12]);",
         "    cube('torso', [i * 2.1 - 0.8, B - 7.2, -3.4], [1.7, 2.8, 1.7], i % 2 ? 'straw' : 'strawDark', [0, 0, i * 12]);"],
        ["    cube('torso', [i * 2.1 - 0.8, B - 8.4, 3.2], [1.7, 3.2, 1.7], i % 2 ? 'strawDark' : 'straw', [0, 0, -i * 12]);",
         "    cube('torso', [i * 2.1 - 0.8, B - 7.0, 3.0], [1.7, 2.8, 1.7], i % 2 ? 'strawDark' : 'straw', [0, 0, -i * 12]);"],
        ['    hy += L[1] * 0.8;', '    hy += L[1] * 0.98;   // no layer overlap -> kills the z-fighting flicker'],
        ['cube(\'head\', [-L[0] / 2 + L[4] - 0.2, hy + L[1] - 0.6, -L[2] / 2 - 0.15], [L[0] + 0.4, 0.8, 0.5], deep);',
         'cube(\'head\', [-L[0] / 2 + L[4] - 0.2, hy + L[1] - 0.6, -L[2] / 2 - 0.30], [L[0] + 0.4, 0.8, 0.5], deep);']
    ];
    for (const [a, b] of fixes) { if (s.includes(a)) { s = s.replace(a, b); n++; } else console.log('  模型: 未命中 ' + a.slice(0, 44)); }
    write(rel, s);
    console.log(`  ①② 模型生成器: ${n}/${fixes.length} 处`);
}
for (const mode of ['brown', 'rainbow']) {
    const r = spawnSync('node', ['gen_compost_boss_v4.cjs', 'out_compost_boss', mode], { cwd: path.join(ROOT, 'tools/model3d'), stdio: 'inherit' });
    console.log(`  ${mode}: 退出码 ${r.status}`);
}
{
    const A = path.join(ROOT, 'src/main/resources/assets/ftboss');
    fs.copyFileSync(path.join(ROOT, 'tools/model3d/out_compost_boss/compost_superman.geo.json'), path.join(A, 'geo/compost_superman.geo.json'));
    fs.copyFileSync(path.join(ROOT, 'tools/model3d/out_compost_boss/compost_superman.geo.json'), path.join(A, 'geo/compost_superman_p2.geo.json'));
    fs.copyFileSync(path.join(ROOT, 'tools/model3d/out_compost_boss/compost_superman.png'), path.join(A, 'textures/entity/compost_superman.png'));
    fs.copyFileSync(path.join(ROOT, 'tools/model3d/out_compost_boss/compost_superman_glowmask.png'), path.join(A, 'textures/entity/compost_superman_glowmask.png'));
    console.log('  模型已同步到资源(含 p2 同名几何)');
    const g = JSON.parse(fs.readFileSync(path.join(A, 'geo/compost_superman.geo.json'), 'utf8'))['minecraft:geometry'][0];
    const bad = JSON.stringify(g).includes(': null');
    console.log('  几何自检: cube=' + g.bones.reduce((a, b) => a + (b.cubes ? b.cubes.length : 0), 0) + '  null=' + (bad ? '⚠有' : '无'));
}
/* ③ 手持物品渲染层 */
{
    const rel = 'src/main/java/net/farmingtales/ftboss/client/render/CompostRenderer.java';
    let s = read(rel);
    if (s.includes('ItemInHandLayer')) console.log('  ③ 已存在 ItemInHandLayer');
    else {
        s = s.replace('        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));',
            `        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
        /* 2026-09-12: GeckoLib 实体默认不渲染手持物, 加一层原版 ItemInHandLayer 才能看到手持的土豆加农炮 */
        this.addRenderLayer(new net.minecraft.client.renderer.entity.layers.ItemInHandLayer<>(this, ctx.getItemInHandRenderer()));`);
        write(rel, s);
        console.log('  ③ CompostRenderer: 已加 ItemInHandLayer');
    }
}
/* ④ 动画: 删掉不存在的骨骼 + 加大抬手 */
{
    const rel = 'src/main/resources/assets/ftboss/animations/compost_superman.animation.json';
    const j = JSON.parse(read(rel));
    let removed = 0, raised = 0;
    for (const an of Object.values(j.animations)) {
        for (const b of ['forearm_r', 'forearm_l', 'hand_r', 'hand_l']) {
            if (an.bones && an.bones[b]) { delete an.bones[b]; removed++; }
        }
        const arm = an.bones && an.bones['arm_r'];
        if (arm && arm.rotation) {
            for (const t of Object.keys(arm.rotation)) {
                const v = arm.rotation[t];
                if (Array.isArray(v) && v[0] < -40) { v[0] = Math.max(-78, v[0] * 1.25); raised++; }
            }
        }
    }
    write(rel, JSON.stringify(j, null, 1));
    console.log(`  ④ 动画: 删除不存在骨骼 ${removed} 处, 加大抬手 ${raised} 个关键帧`);
    const geo = JSON.parse(read('src/main/resources/assets/ftboss/geo/compost_superman.geo.json'))['minecraft:geometry'][0];
    const bones = new Set(geo.bones.map(b => b.name));
    const bad = new Set();
    for (const an of Object.values(j.animations)) for (const b of Object.keys(an.bones || {})) if (!bones.has(b)) bad.add(b);
    console.log('  动画引用但模型没有的骨骼: ' + ([...bad].join(', ') || '无 OK'));
}
console.log('完成');
