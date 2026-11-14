/*
 * 2026-09-12 修四个问题:
 *  ① 腿间距过远: ±6.6 -> ±5.2
 *  ② 腿与外部干草穿模: 下摆稻草收窄(i -4..4 -> -3..3)且上移(B-7.2 -> B-6.4), 腿部稻草略细(4.0 -> 3.6)
 *  ③ 二阶段贴图糊一起: **上一轮只拷了棕色贴图, 漏拷 p2 贴图** -> 这次两套 geo/png/glowmask 全部同步
 *  ④ 二阶段直接起飞: 阶段>=2 时 setNoGravity(true), 并悬浮到地面上方 ~3.2 格(带轻微起伏)
 */
const fs = require('fs');
const path = require('path');
const { spawnSync } = require('child_process');
const ROOT = process.argv[2];
const read = p => fs.readFileSync(path.join(ROOT, p), 'utf8');
const write = (p, s) => fs.writeFileSync(path.join(ROOT, p), s);

/* ① ② 生成器 */
{
    const rel = 'tools/model3d/gen_compost_boss_v4.cjs';
    let s = read(rel); let n = 0;
    const fixes = [
        ['    const cx = sx * 6.6;   // legs pushed outward so the tunic does not swallow them',
         '    const cx = sx * 5.2;   // 2026-09-12: legs back closer together'],
        ["    cube(bone, [cx - 2.0, 2.4, -2.2], [4.0, 6.0, 4.4], 'straw');",
         "    cube(bone, [cx - 1.8, 2.4, -2.2], [3.6, 6.0, 4.2], 'straw');"],
        ["    cube(bone, [cx - 2.1, 8.4, -2.3], [4.2, 6.6, 4.6], 'straw');",
         "    cube(bone, [cx - 1.9, 8.4, -2.3], [3.8, 6.6, 4.4], 'straw');"],
        ["for (let i = -4; i <= 4; i++) {\n    cube('torso', [i * 2.1 - 0.8, B - 7.2, -3.4], [1.7, 2.8, 1.7], i % 2 ? 'straw' : 'strawDark', [0, 0, i * 12]);\n    cube('torso', [i * 2.1 - 0.8, B - 7.0, 3.0], [1.7, 2.8, 1.7], i % 2 ? 'strawDark' : 'straw', [0, 0, -i * 12]);\n}",
         "for (let i = -3; i <= 3; i++) {\n    cube('torso', [i * 2.0 - 0.8, B - 6.2, -3.6], [1.6, 2.6, 1.6], i % 2 ? 'straw' : 'strawDark', [0, 0, i * 10]);\n    cube('torso', [i * 2.0 - 0.8, B - 6.0, 3.2], [1.6, 2.6, 1.6], i % 2 ? 'strawDark' : 'straw', [0, 0, -i * 10]);\n}"]
    ];
    for (const [a, b] of fixes) { if (s.includes(a)) { s = s.replace(a, b); n++; } else console.log('  模型: 未命中 ' + a.slice(0, 40)); }
    write(rel, s);
    console.log(`  ①② 模型: ${n}/${fixes.length} 处`);
}
for (const mode of ['brown', 'rainbow']) {
    const r = spawnSync('node', ['gen_compost_boss_v4.cjs', 'out_compost_boss', mode], { cwd: path.join(ROOT, 'tools/model3d'), stdio: 'inherit' });
    console.log(`  ${mode}: ${r.status}`);
}
/* ③ 两套资源全部同步(上次漏了 p2 贴图) */
{
    const A = path.join(ROOT, 'src/main/resources/assets/ftboss');
    const O = path.join(ROOT, 'tools/model3d/out_compost_boss');
    const pairs = [
        ['compost_superman.geo.json', 'geo/compost_superman.geo.json'],
        ['compost_superman.geo.json', 'geo/compost_superman_p2.geo.json'],
        ['compost_superman.png', 'textures/entity/compost_superman.png'],
        ['compost_superman_p2.png', 'textures/entity/compost_superman_p2.png'],
        ['compost_superman_glowmask.png', 'textures/entity/compost_superman_glowmask.png'],
        ['compost_superman_p2_glowmask.png', 'textures/entity/compost_superman_p2_glowmask.png']
    ];
    for (const [a, b] of pairs) {
        const src = path.join(O, a);
        if (fs.existsSync(src)) { fs.copyFileSync(src, path.join(A, b)); console.log('  同步 ' + a + ' -> ' + b + '  ' + fs.statSync(src).size + 'B'); }
        else console.log('  ⚠ 缺 ' + a);
    }
}
/* ④ 二阶段起飞 */
{
    const rel = 'src/main/java/net/farmingtales/ftboss/entity/CompostBossEntity.java';
    let s = read(rel);
    if (s.includes('hover flight in phase 2')) console.log('  ④ 已存在');
    else {
        const anchor = '        // attack speed decay: no hit on a player for 3s -> reset';
        const block = `        /* hover flight in phase 2 (2026-09-12): 二阶段直接飞起来 */
        boolean fly = this.getPhase() >= 2;
        if (this.isNoGravity() != fly) this.setNoGravity(fly);
        if (fly && this.level() instanceof ServerLevel fsl) {
            net.minecraft.core.BlockPos gp = fsl.getHeightmapPos(
                    net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, this.blockPosition());
            double want = gp.getY() + 3.2 + Math.sin(this.tickCount * 0.08) * 0.35;
            double cy = this.getY();
            this.setDeltaMovement(0, 0, 0);
            this.setPos(this.getX(), cy + Math.max(-0.3, Math.min(0.3, want - cy)), this.getZ());
        }

`;
        if (s.includes(anchor)) { s = s.replace(anchor, block + anchor); write(rel, s); console.log('  ④ 已加二阶段悬飞逻辑'); }
        else console.log('  ④ FAIL 找不到锚点');
    }
}
console.log('完成');
