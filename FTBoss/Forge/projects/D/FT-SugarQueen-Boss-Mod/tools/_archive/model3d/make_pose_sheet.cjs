/* 姿态预览修正: 每姿态渲 4 视图 -> 裁左上(正面)一格 -> 横向拼接成对比图 */
const fs = require('fs');
const path = require('path');
const { spawnSync } = require('child_process');
const { Canvas } = require('./png.cjs');
const OUT = 'out_compost_boss';
const A = 'animation.compost_superman.';
const POSES = [['idle', 0.6, 'idle'], ['walk', 0.25, 'walk'], ['cannon_fire', 0.35, 'fire'], ['transform', 1.8, 'super']];
const DEST = process.argv[2];
const cells = [];
for (const [name, t, tag] of POSES) {
    const baked = path.join(OUT, `pose_${tag}.geo.json`);
    const four = path.join(OUT, `pose_${tag}_4v.png`);
    const r1 = spawnSync('node', ['pose_bake.cjs', path.join(OUT, 'compost_superman.geo.json'),
        path.join(OUT, 'compost_superman.animation.json'), A + name, String(t), baked], { stdio: 'pipe', encoding: 'utf8' });
    if (r1.status !== 0) { console.log(`  ${name} 烘焙失败: ${(r1.stderr || '').split('\n').slice(0, 2).join(' ')}`); continue; }
    const r2 = spawnSync('node', ['render_tex.cjs', baked, path.join(OUT, 'compost_superman.png'), four, '2.9', '4.0'],
        { stdio: 'pipe', encoding: 'utf8', env: { ...process.env, FT_VIEWS: 'front,right,back,iso', FT_PANEL: '330', FT_SCALE: '5.6', FT_OY: '300' } });
    if (!fs.existsSync(four)) { console.log(`  ${name} 渲染失败: ${(r2.stderr || r2.stdout || '').split('\n').slice(0, 2).join(' ')}`); continue; }
    const im = Canvas.load(four);
    /* 裁左上 1/4(正面视图) */
    const cw = Math.floor(im.w / 2), ch = Math.floor(im.h / 2);
    const cell = new Canvas(cw, ch);
    for (let y = 0; y < ch; y++) for (let x = 0; x < cw; x++) { const p = im.get(x, y); cell.set(x, y, p[0], p[1], p[2], p[3]); }
    cells.push(cell);
    console.log(`  ${name} @${t}s 已渲染并裁剪 (${cw}x${ch})`);
}
if (!cells.length) { console.log('❌ 没有任何姿态可用'); process.exit(1); }
const W = cells.reduce((a, b) => a + b.w, 0), H = Math.max(...cells.map(c => c.h));
const sheet = new Canvas(W, H);
for (let y = 0; y < H; y++) for (let x = 0; x < W; x++) sheet.set(x, y, 16, 16, 20, 1);
let ox = 0;
for (const c of cells) { for (let y = 0; y < c.h; y++) for (let x = 0; x < c.w; x++) { const p = c.get(x, y); if (p[3] > 0) sheet.set(ox + x, y, p[0], p[1], p[2], 1); } ox += c.w; }
sheet.save(DEST);
console.log(`★ 姿态对比图已输出: ${DEST}  ${W}x${H}  共 ${cells.length} 个姿态(idle / walk / cannon_fire / transform)`);
