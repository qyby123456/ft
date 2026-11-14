/* ============================================================================
 *  check_sword_compat.cjs —— 用户 §"补充" 那份 Python 检查器的 Node 移植版
 *  比较「改造前 vs 改造后」：
 *    · 原有骨骼是否全部保留；
 *    · parent / pivot / rotation 是否被改动；
 *    · 骨骼父级是否有循环或引用不存在的父级；
 *    · 动画是否引用了不存在的骨骼；
 *    · **动画文件是否逐字节完全一致**（sha256）；
 *    · 立方体数量变化、新增骨骼列表。
 *  它**不能**判断视觉穿模或贴图质量 —— 那些仍需 Blockbench / 实机（用户原话）。
 *  用法:
 *    node tools/model3d/check_sword_compat.cjs [旧geo] [新geo] [旧anim] [新anim]
 *  默认:
 *    旧 = ref/shadow_sword.v3pre.geo.json / ref/shadow_sword.v3pre.animation.json
 *    新 = src/main/resources/assets/ftboss/geo|animations/shadow_sword.*
 *  退出码 0 = 通过, 1 = 不通过（可直接用于流水线门禁）。
 * ==========================================================================*/
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

const ROOT = path.resolve(__dirname, '..', '..');
const AS = path.join(ROOT, 'src', 'main', 'resources', 'assets', 'ftboss');
const argv = process.argv.slice(2);
const OLD_GEO = argv[0] || path.join(ROOT, 'ref', 'shadow_sword.v3pre.geo.json');
const NEW_GEO = argv[1] || path.join(AS, 'geo', 'shadow_sword.geo.json');
const OLD_ANIM = argv[2] || path.join(ROOT, 'ref', 'shadow_sword.v3pre.animation.json');
const NEW_ANIM = argv[3] || path.join(AS, 'animations', 'shadow_sword.animation.json');

const sha = p => crypto.createHash('sha256').update(fs.readFileSync(p)).digest('hex');
const readJson = p => JSON.parse(fs.readFileSync(p, 'utf8'));

function readBones(p) {
    const g = readJson(p)['minecraft:geometry'];
    if (!g || g.length !== 1) throw new Error('本检查器要求模型中恰好有一个 geometry: ' + p);
    const bones = g[0].bones || [];
    const names = bones.map(b => b.name);
    if (names.length !== new Set(names).size) throw new Error('模型存在重复骨骼名称: ' + p);
    return Object.fromEntries(bones.map(b => [b.name, b]));
}
const cubeCount = bones => Object.values(bones).reduce((n, b) => n + (b.cubes ? b.cubes.length : 0), 0);

const oldBones = readBones(OLD_GEO);
const newBones = readBones(NEW_GEO);
const animations = readJson(NEW_ANIM).animations || {};
const errors = [];

/* 1) 原有骨骼保留 + parent/pivot/rotation 不变 */
const DEFAULTS = { parent: null, pivot: [0, 0, 0], rotation: [0, 0, 0] };
const j = v => JSON.stringify(v === undefined ? null : v);
for (const [name, ob] of Object.entries(oldBones)) {
    const nb = newBones[name];
    if (!nb) { errors.push('丢失原有骨骼：' + name); continue; }
    for (const [field, dflt] of Object.entries(DEFAULTS)) {
        const before = ob[field] === undefined ? dflt : ob[field];
        const after = nb[field] === undefined ? dflt : nb[field];
        if (j(before) !== j(after)) errors.push(name + '.' + field + ' 改变：' + j(before) + ' -> ' + j(after));
    }
}

/* 2) 父级存在 + 无循环 */
for (const name of Object.keys(newBones)) {
    const visited = new Set();
    let cur = name;
    while (cur != null) {
        if (visited.has(cur)) { errors.push('骨骼父级形成循环：' + name); break; }
        visited.add(cur);
        const b = newBones[cur];
        if (!b) { errors.push('骨骼 ' + name + ' 引用不存在的父级：' + cur); break; }
        cur = b.parent === undefined ? null : b.parent;
    }
}

/* 3) 动画引用的骨骼必须存在 */
for (const [an, a] of Object.entries(animations)) {
    for (const bn of Object.keys(a.bones || {})) {
        if (!newBones[bn]) errors.push('动画 ' + an + ' 引用不存在的骨骼：' + bn);
    }
}

/* 4) 动画文件逐字节一致 */
const oldAnimSha = sha(OLD_ANIM), newAnimSha = sha(NEW_ANIM);
const unchanged = oldAnimSha === newAnimSha;
/* 2026-09-15 放宽: 允许**新增**动画片段(裂瞳回响等), 但已有片段必须逐段完全一致。
   逐段比对取代整文件 sha256 —— 否则每次加一段新动画都会让门禁误报。 */
const oldClips = readJson(OLD_ANIM).animations || {};
const newClipsAll = readJson(NEW_ANIM).animations || {};
const clipChanged = [], clipRemoved = [];
for (const [k, v] of Object.entries(oldClips)) {
    if (!(k in newClipsAll)) { clipRemoved.push(k); continue; }
    if (JSON.stringify(v) !== JSON.stringify(newClipsAll[k])) clipChanged.push(k);
}
const clipAdded = Object.keys(newClipsAll).filter(k => !(k in oldClips));
if (clipRemoved.length) errors.push('动画片段被删除：' + clipRemoved.join(', '));
if (clipChanged.length) errors.push('已有动画片段被修改：' + clipChanged.join(', '));
console.log('动画片段：' + Object.keys(oldClips).length + ' -> ' + Object.keys(newClipsAll).length +
    '  新增 ' + (clipAdded.length ? clipAdded.map(k => k.split('.').pop()).join(',') : '无') +
    '  改动 ' + (clipChanged.length ? clipChanged.join(',') : '无') +
    '  删除 ' + (clipRemoved.length ? clipRemoved.join(',') : '无'));

const added = Object.keys(newBones).filter(n => !oldBones[n]).sort();

console.log('=== 裂瞳 V3 骨架兼容性检查 ===');
console.log('旧 geo : ' + path.relative(ROOT, OLD_GEO));
console.log('新 geo : ' + path.relative(ROOT, NEW_GEO));
console.log('旧立方体数：' + cubeCount(oldBones) + '   新立方体数：' + cubeCount(newBones));
console.log('骨骼数：' + Object.keys(oldBones).length + ' -> ' + Object.keys(newBones).length);
console.log('新增骨骼：' + (added.length ? added.join(', ') : '无'));
console.log('动画文件完全一致：' + unchanged + '   (' + newAnimSha.slice(0, 12) + ')');
console.log('动画段数：' + Object.keys(animations).length + '  ' + Object.keys(animations).map(k => k.split('.').pop()).join(', '));

if (errors.length) {
    console.log('\n兼容性检查未通过：');
    errors.forEach(e => console.log('  - ' + e));
    process.exit(1);
}
console.log('\n基础兼容性检查通过。');
console.log('仍需检查：几何运动范围、贴图、显隐及游戏手持效果（Blockbench / 实机）。');
