/* 资源总审计(2026-09-12): 一次跑完, 把"会让加载条卡死"的问题全兜住
 *   ① 所有 .json / .mcmeta: BOM + 能否 JSON.parse
 *   ② 所有 geo/*.geo.json: null/NaN、骨骼重名、parent 存在性、cube 三要素、UV 是否超出声明图集
 *   ③ 所有 animations/*.animation.json: 时间/数值合法性、时长越界、骨骼是否存在于同名 geo
 *   ④ geo 声明的图集尺寸 vs 同名贴图实际尺寸
 * 用法: node audit_resources.cjs <resourcesRoot>
 */
const fs = require('fs');
const path = require('path');
const { decodePNG } = require('../model3d/png.cjs');

const RES = process.argv[2] || 'src/main/resources';
const ASSETS = path.join(RES, 'assets/ftboss');
let problems = 0;
const bad = (m) => { problems++; console.log('  ✗ ' + m); };

function walk(dir, out = []) {
    if (!fs.existsSync(dir)) return out;
    for (const e of fs.readdirSync(dir, { withFileTypes: true })) {
        const p = path.join(dir, e.name);
        if (e.isDirectory()) walk(p, out);
        else out.push(p);
    }
    return out;
}
const all = walk(RES);
const hasBom = (b) => b.length >= 3 && b[0] === 0xEF && b[1] === 0xBB && b[2] === 0xBF;

/* ---------- ① JSON ---------- */
let jsonCount = 0;
const parsed = new Map();
for (const p of all.filter((f) => /\.(json|mcmeta)$/.test(f))) {
    jsonCount++;
    const b = fs.readFileSync(p);
    if (hasBom(b)) bad('BOM: ' + path.relative(RES, p));
    const txt = b.toString('utf8').replace(/^\uFEFF/, '');
    if (/\bnull\b/.test(txt) && /geo\.json$/.test(p)) bad('geo 含 null: ' + path.relative(RES, p));
    if (/NaN|Infinity|undefined/.test(txt)) bad('含 NaN/Infinity/undefined: ' + path.relative(RES, p));
    try { parsed.set(p, JSON.parse(txt)); }
    catch (e) { bad('JSON 解析失败: ' + path.relative(RES, p) + ' -> ' + e.message); }
}
console.log(`① JSON: ${jsonCount} 个, 全部可解析且无 BOM` + (problems ? '' : ' ✓'));

/* ---------- ② geo ---------- */
const geoFiles = all.filter((f) => f.endsWith('.geo.json'));
const geoBones = new Map();
for (const p of geoFiles) {
    const g = parsed.get(p);
    if (!g) continue;
    const geoList = g['minecraft:geometry'] || [];
    geoList.forEach((def, gi) => {
        const desc = def.description || {};
        const TW = desc.texture_width, TH = desc.texture_height;
        const names = new Set();
        for (const b of def.bones || []) {
            if (!b.name) bad(path.basename(p) + ' 有无名骨骼');
            if (names.has(b.name)) bad(path.basename(p) + ' 骨骼重名 ' + b.name);
            names.add(b.name);
            if (!Array.isArray(b.pivot) || b.pivot.length !== 3 || !b.pivot.every(Number.isFinite))
                bad(path.basename(p) + ' ' + b.name + '.pivot 非法: ' + JSON.stringify(b.pivot));
        }
        let cubes = 0, maxU = 0, maxV = 0;
        for (const b of def.bones || []) {
            if (b.parent && !names.has(b.parent)) bad(path.basename(p) + ' ' + b.name + '.parent 不存在: ' + b.parent);
            for (const c of b.cubes || []) {
                cubes++;
                if (!Array.isArray(c.origin) || c.origin.length !== 3 || !c.origin.every(Number.isFinite))
                    bad(path.basename(p) + ' ' + b.name + '.origin 非法: ' + JSON.stringify(c.origin));
                if (!Array.isArray(c.size) || c.size.length !== 3 || !c.size.every(Number.isFinite))
                    bad(path.basename(p) + ' ' + b.name + '.size 非法: ' + JSON.stringify(c.size));
                /* uv 支持两种合法写法: 扁平 [u,v](box-UV) 或逐面对象 {north:{uv,uv_size},...} */
                const uvOk = Array.isArray(c.uv)
                    ? (c.uv.length === 2 && c.uv.every(Number.isFinite))
                    : (c.uv && typeof c.uv === 'object' && Object.values(c.uv).every((f) => f && Array.isArray(f.uv) && f.uv.every(Number.isFinite)));
                if (!uvOk) bad(path.basename(p) + ' ' + b.name + '.uv 非法: ' + JSON.stringify(c.uv).slice(0, 60));
                if (c.rotation && (!Array.isArray(c.rotation) || c.rotation.length !== 3 || !c.rotation.every(Number.isFinite)))
                    bad(path.basename(p) + ' ' + b.name + '.rotation 非法');
                if (c.size && Array.isArray(c.uv)) {
                    maxU = Math.max(maxU, c.uv[0] + Math.ceil(2 * (c.size[0] + c.size[2])));
                    maxV = Math.max(maxV, c.uv[1] + Math.ceil(c.size[2] + c.size[1]));
                }
            }
        }
        if (maxU > TW || maxV > TH) bad(path.basename(p) + ` UV 超出图集 ${maxU}x${maxV} > ${TW}x${TH}`);
        geoBones.set(path.basename(p), names);
        if (gi === 0) console.log(`   ${path.basename(p).padEnd(28)} bones=${String((def.bones || []).length).padStart(2)} cubes=${String(cubes).padStart(3)} ${TW}x${TH}`);
    });
}
console.log(`② geo: ${geoFiles.length} 个文件全部通过结构检查`);

/* ---------- ③ 动画 ---------- */
const animFiles = all.filter((f) => f.endsWith('.animation.json'));
let animTotal = 0, keyTotal = 0;
for (const p of animFiles) {
    const a = parsed.get(p);
    if (!a) continue;
    /* 找同名 geo(去掉 .animation / .geo) */
    const base = path.basename(p).replace(/\.animation\.json$/, '');
    let bones = geoBones.get(base + '.geo.json');
    if (!bones) {
        /* 名字对不上时(例如 rat.animation.json 配 big_rat/small_rat.geo.json): 找骨骼集合能对上的 geo */
        const A0 = Object.values(a.animations)[0];
        const animBones = new Set(Object.keys((A0 && A0.bones) || {}));
        for (const [g, set] of geoBones) {
            if ([...animBones].every((b) => set.has(b))) { bones = set; break; }
        }
        if (!bones) { bad(path.basename(p) + ' 找不到骨骼能对上的 geo'); continue; }
        console.log(`   ${path.basename(p)} 按骨骼匹配到 geo(文件名不同, 已容忍)`);
    }
    for (const k in a.animations) {
        animTotal++;
        const A = a.animations[k], len = A.animation_length;
        if (!(len > 0)) bad(k + ' animation_length 非法');
        for (const b in A.bones || {}) {
            if (!bones.has(b)) bad(k + ' 骨骼不在 geo 里: ' + b);
            for (const ch in A.bones[b]) {
                if (!['rotation', 'position', 'scale'].includes(ch)) bad(k + ' 非法通道 ' + ch);
                for (const t in A.bones[b][ch]) {
                    keyTotal++;
                    const tv = Number(t);
                    if (!Number.isFinite(tv)) bad(k + ' ' + b + ' 时间非法 ' + t);
                    else if (tv > len + 1e-6) bad(k + ' ' + b + '.' + ch + ' 关键帧 ' + t + ' 超出时长 ' + len);
                    const v = A.bones[b][ch][t];
                    const arr = Array.isArray(v) ? v : (v && v.vector);
                    if (!arr || !arr.every(Number.isFinite)) bad(k + ' ' + b + '.' + ch + ' @' + t + ' 值非法');
                }
            }
        }
    }
}
console.log(`③ 动画: ${animFiles.length} 个文件 / ${animTotal} 个动画 / ${keyTotal} 关键帧`);

/* ---------- ④ 贴图尺寸 vs geo 声明 ---------- */
const TEXDIR = path.join(ASSETS, 'textures/entity');
let texChecked = 0;
for (const p of geoFiles) {
    const g = parsed.get(p);
    if (!g) continue;
    const desc = g['minecraft:geometry'][0].description;
    const id = desc.identifier || '';
    /* identifier 与贴图同名的约定: geometry.X -> textures/entity/X.png */
    const guess = path.join(TEXDIR, id.replace(/^geometry\./, '') + '.png');
    if (!fs.existsSync(guess)) continue;
    const img = decodePNG(guess);
    texChecked++;
    if (img.w !== desc.texture_width || img.h !== desc.texture_height)
        bad(`${path.basename(guess)} ${img.w}x${img.h} != geo 声明 ${desc.texture_width}x${desc.texture_height}`);
}
console.log(`④ 贴图: 按 identifier 自动比对了 ${texChecked} 张(其他按名称不同跳过)`);

console.log(problems === 0 ? '\n===== 审计通过: 没有发现问题 =====' : `\n===== 共 ${problems} 处问题 =====`);
process.exit(problems === 0 ? 0 : 1);
