/* 严格校验 Bedrock geo + 动画 + 贴图尺寸(2026-09-12)
 * 卡加载条的两大元凶: ① JSON 里混进 null/NaN(JS undefined -> null) ② 贴图尺寸与 geo 声明不符
 * 用法: node check_geo.cjs <geo.json> [animation.json] [texture.png ...]
 */
const fs = require('fs');
const path = require('path');
const { decodePNG } = require('../model3d/png.cjs');

const geoPath = process.argv[2];
const animPath = process.argv[3] && process.argv[3].endsWith('.json') ? process.argv[3] : null;
const texPaths = process.argv.slice(animPath ? 4 : 3).filter((p) => p && !p.startsWith('--'));

let problems = 0;
const bad = (m) => { problems++; console.log('  FAIL ' + m); };
const finite3 = (a) => Array.isArray(a) && a.length === 3 && a.every((n) => typeof n === 'number' && Number.isFinite(n));

/* ---------- 深扫 null / NaN(文本级, 防止 JS undefined 漏网) ---------- */
for (const p of [geoPath, animPath].filter(Boolean)) {
    const txt = fs.readFileSync(p, 'utf8');
    if (/\bnull\b/.test(txt)) bad(path.basename(p) + ' 含 null');
    if (/NaN|Infinity|undefined/.test(txt)) bad(path.basename(p) + ' 含 NaN/Infinity/undefined');
}

/* ---------- geo ---------- */
const geo = JSON.parse(fs.readFileSync(geoPath, 'utf8'));
const def = geo['minecraft:geometry'][0];
const desc = def.description;
const TW = desc.texture_width, TH = desc.texture_height;
const names = new Set();
let cubes = 0, maxU = 0, maxV = 0;
for (const b of def.bones) {
    if (!b.name) bad('bone 无名');
    if (names.has(b.name)) bad('骨骼重名: ' + b.name);
    names.add(b.name);
    if (!finite3(b.pivot)) bad(b.name + '.pivot 不是 3 个有限数');
}
for (const b of def.bones) {
    if (b.parent && !names.has(b.parent)) bad(b.name + '.parent 不存在: ' + b.parent);
    for (const c of b.cubes || []) {
        cubes++;
        if (!finite3(c.origin)) bad(b.name + ' cube.origin 非法');
        if (!finite3(c.size)) bad(b.name + ' cube.size 非法');
        if (!Array.isArray(c.uv) || c.uv.length !== 2 || !c.uv.every(Number.isFinite)) bad(b.name + ' cube.uv 非法');
        if (c.rotation && !finite3(c.rotation)) bad(b.name + ' cube.rotation 非法');
        if (c.size && c.uv) {
            const fw = Math.ceil(2 * (c.size[0] + c.size[2])), fh = Math.ceil(c.size[2] + c.size[1]);
            maxU = Math.max(maxU, c.uv[0] + fw); maxV = Math.max(maxV, c.uv[1] + fh);
        }
    }
}
if (maxU > TW || maxV > TH) bad(`UV 超出图集: 用到 ${maxU}x${maxV} > 声明 ${TW}x${TH}`);
console.log(`  geo: bones=${def.bones.length} cubes=${cubes} 声明图集=${TW}x${TH} UV用到=${maxU}x${maxV}`);

/* ---------- 动画 ---------- */
if (animPath) {
    const anim = JSON.parse(fs.readFileSync(animPath, 'utf8'));
    let keys = 0;
    for (const k in anim.animations) {
        const a = anim.animations[k], len = a.animation_length;
        if (!(len > 0)) bad(k + ' animation_length 非法: ' + len);
        for (const b in a.bones || {}) {
            if (!names.has(b)) bad(k + ': 动画骨骼不在 geo 里 -> ' + b);
            for (const ch in a.bones[b]) {
                if (!['rotation', 'position', 'scale'].includes(ch)) bad(k + ' 非法通道 ' + ch);
                for (const t in a.bones[b][ch]) {
                    keys++;
                    const tv = Number(t);
                    if (!Number.isFinite(tv)) bad(k + ' ' + b + '.' + ch + ' 时间非法: ' + t);
                    if (tv > len + 1e-6) bad(k + ' ' + b + '.' + ch + ' 关键帧 ' + t + ' 超出时长 ' + len);
                    const v = a.bones[b][ch][t];
                    const arr = Array.isArray(v) ? v : (v && v.vector);
                    if (!arr || !arr.every(Number.isFinite)) bad(k + ' ' + b + '.' + ch + ' @' + t + ' 值非法');
                }
            }
        }
    }
    console.log(`  anim: ${Object.keys(anim.animations).length} 个动画 / ${keys} 关键帧`);
}

/* ---------- 贴图尺寸 ---------- */
for (const p of texPaths) {
    if (!fs.existsSync(p)) { bad('贴图不存在: ' + p); continue; }
    const img = decodePNG(p);
    const okSize = img.w === TW && img.h === TH;
    console.log(`  贴图 ${path.basename(p)}: ${img.w}x${img.h}` + (okSize ? '  ✓与geo声明一致' : `  ✗与geo声明 ${TW}x${TH} 不一致`));
    if (!okSize) bad(path.basename(p) + ' 尺寸与 geo 声明不符');
}

console.log(problems === 0 ? 'OK: 全部通过' : problems + ' 处问题');
process.exit(problems === 0 ? 0 : 1);
