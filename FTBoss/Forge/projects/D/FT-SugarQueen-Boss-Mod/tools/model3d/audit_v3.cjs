/* ============================================================================
 *  audit_v3.cjs —— 裂瞳 V3 改造前的 §1 审计 + 备份（只读 + 复制，不改模型）
 *   ① 备份 geo / animation / png 到 ref/（带 v3pre 标记，不覆盖已有备份）
 *   ② 输出: 骨骼名称与父级、各骨骼 pivot/rotation、每个 cube 数
 *   ③ 输出: 动画引用的骨骼（逐动画）
 *   ④ 输出: Java 代码里引用到的骨骼/动画名（扫描源码，防止改名漏改）
 *   ⑤ 输出: 贴图尺寸 + 逐面 UV 是否独立（有无两个部件共用同一 UV 矩形）
 *  用法: node tools/model3d/audit_v3.cjs
 * ==========================================================================*/
const fs = require('fs');
const path = require('path');
const ROOT = path.resolve(__dirname, '..', '..');
const AS = path.join(ROOT, 'src', 'main', 'resources', 'assets', 'ftboss');
const GEO = path.join(AS, 'geo', 'shadow_sword.geo.json');
const ANIM = path.join(AS, 'animations', 'shadow_sword.animation.json');
const PNG = path.join(AS, 'textures', 'item', 'shadow_sword.png');
const REF = path.join(ROOT, 'ref');
fs.mkdirSync(REF, { recursive: true });

/* ① 备份 */
for (const [src, name] of [[GEO, 'shadow_sword.v3pre.geo.json'], [ANIM, 'shadow_sword.v3pre.animation.json'], [PNG, 'shadow_sword.v3pre.png']]) {
    const dst = path.join(REF, name);
    if (!fs.existsSync(dst)) { fs.copyFileSync(src, dst); console.log('备份 -> ref/' + name); }
    else { console.log('备份已存在(未覆盖) -> ref/' + name); }
}

const geo = JSON.parse(fs.readFileSync(GEO, 'utf8'))['minecraft:geometry'][0];
const anims = JSON.parse(fs.readFileSync(ANIM, 'utf8')).animations;

/* ② 骨骼表 */
console.log('\n=== 骨骼 ' + geo.bones.length + ' 根（父级 / pivot / rotation / cube 数）===');
let cubes = 0;
geo.bones.forEach(b => {
    cubes += b.cubes.length;
    console.log('  ' + b.name.padEnd(16) + 'parent=' + String(b.parent || '-').padEnd(13) +
        'cube=' + String(b.cubes.length).padStart(2) +
        '  pivot=' + JSON.stringify(b.pivot) + (b.rotation ? '  rot=' + JSON.stringify(b.rotation) : ''));
});
console.log('  cube 合计 ' + cubes + ' / 导出面 ' + (cubes * 6) + ' / 估三角形 ' + (cubes * 12));

/* ③ 动画引用的骨骼 */
console.log('\n=== 动画 ' + Object.keys(anims).length + ' 段（时长 / 循环 / 引用骨骼）===');
const referenced = new Set();
Object.entries(anims).forEach(([k, a]) => {
    Object.keys(a.bones).forEach(b => referenced.add(b));
    console.log('  ' + k.split('.').pop().padEnd(9) + a.animation_length + 's loop=' + String(a.loop).padEnd(5) + Object.keys(a.bones).join(','));
});
const missing = [...referenced].filter(n => !geo.bones.some(b => b.name === n));
console.log('  动画引用了但 geo 里不存在的骨骼: ' + (missing.length ? missing.join(', ') : '无 ✓'));
const unused = geo.bones.filter(b => !referenced.has(b.name) && b.cubes.length > 0).map(b => b.name);
console.log('  有几何但没被任何动画引用的骨骼: ' + (unused.length ? unused.join(', ') : '无'));

/* ④ Java 引用扫描 */
const JAVA = path.join(ROOT, 'src', 'main', 'java', 'net', 'farmingtales', 'ftboss');
const javaHits = new Set();
const walk = d => fs.readdirSync(d, { withFileTypes: true }).forEach(e => {
    const p = path.join(d, e.name);
    if (e.isDirectory()) { walk(p); return; }
    if (!e.name.endsWith('.java')) return;
    const t = fs.readFileSync(p, 'utf8');
    (t.match(/animation\.ftboss\.shadow_sword\.[a-z_]+/g) || []).forEach(m => javaHits.add(m));
});
walk(JAVA);
console.log('\n=== Java 里写死的动画名（改名必须同步）===');
console.log('  ' + ([...javaHits].join(', ') || '(无)'));
const inFile = new Set(Object.keys(anims));
const bad = [...javaHits].filter(n => !inFile.has(n));
console.log('  Java 引用但动画文件里没有的: ' + (bad.length ? bad.join(', ') + '  ← 必须修' : '无 ✓'));

/* ⑤ 贴图与 UV */
const png = fs.readFileSync(PNG);
const w = png.readUInt32BE(16), h = png.readUInt32BE(20);
console.log('\n=== 贴图 ===');
console.log('  shadow_sword.png = ' + w + '×' + h + '（' + (fs.statSync(PNG).size / 1024).toFixed(1) + ' KB）');
console.log('  ' + (w === 128 ? '已经是规格建议的 128×128 ✓' : '当前不是 128×128；若维持需说明理由（见报告）'));
const key = c => { const u = c.faces && c.faces.north && c.faces.north.uv; return u ? u.uv.join(',') + '|' + u.uv_size.join(',') : null; };
const byKey = {};
geo.bones.forEach(b => b.cubes.forEach((c, i) => { const k = key(c); if (k) (byKey[k] = byKey[k] || []).push(b.name + '#' + i); }));
const shared = Object.entries(byKey).filter(([, v]) => v.length > 1);
console.log('  逐面 UV 独立: ' + (shared.length ? '否 —— 共用矩形 ' + shared.length + ' 组' : '是（无任何两组共用）✓'));
shared.slice(0, 6).forEach(([k, v]) => console.log('    共用 [' + k + ']: ' + v.join(', ')));
