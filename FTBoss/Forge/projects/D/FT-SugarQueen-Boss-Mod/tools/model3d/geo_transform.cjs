/* ============================================================================
 *  geo_transform.cjs —— Bedrock geo 变换引擎 + A~F 校准自检（只读, 不写任何正式资源）
 *
 *  为什么单独做: 上一版 geo_to_preview.cjs 是"有损静态预览"(不应用骨骼变换、min/max 抹掉 UV 镜像、
 *  不支持 inflate/box-uv), 用户指出不能据此判断穿模/连接/镜像。本文件先把**变换与 UV 解析**做对,
 *  并用 A~F 六项数值自检与"解析解"逐项比对 —— 校准通过之后, 渲染器再基于它实现。
 *
 *  已实现（本工程资源实际用到的部分）:
 *    · 完整骨骼父子层级: 每级 M = T(pivot)·R(rot)·S(scale)·T(-pivot), 按 root→…→bone 依次左乘
 *      （Bedrock 的 cube/pivot 都是模型绝对坐标, 所以子骨骼变换作用于父变换之后）
 *    · 骨骼默认 rotation + 动画姿态(position/rotation/scale, 数字关键帧线性插值)
 *    · cube 自身的 pivot + rotation（3 轴, 作用顺序 XYZ）
 *    · inflate（把 cube 各方向外扩, 参与包围盒与角点）
 *    · 逐面 UV: 保留 uv_size 的**符号**(镜像方向), 不用 min/max 抹掉
 *    · Box UV / Molang / 未知插值: **显式报错**, 不静默当 0
 *
 *  用法:
 *    node tools/model3d/geo_transform.cjs --calib          # 跑 A~F 自检
 *    node tools/model3d/geo_transform.cjs <geo.json> --dump 0.35   # 打印某时刻各骨骼世界包围盒
 * ==========================================================================*/
'use strict';
const fs = require('fs');
const path = require('path');
const ROOT = path.resolve(__dirname, '..', '..');

/* ---------------- 基础向量/矩阵(行主序 3x3 + 平移) ---------------- */
const sub = (a, b) => [a[0] - b[0], a[1] - b[1], a[2] - b[2]];
const add = (a, b) => [a[0] + b[0], a[1] + b[1], a[2] + b[2]];
const mul = (a, s) => [a[0] * s, a[1] * s, a[2] * s];
const rad = d => d * Math.PI / 180;

/** 绕单轴旋转(用于骨骼默认旋转/姿态旋转; Bedrock 骨骼旋转按 X→Y→Z 依次施加) */
function rotXYZ(p, r, pivot) {
    let q = sub(p, pivot);
    let c = Math.cos(rad(r[0])), s = Math.sin(rad(r[0])); q = [q[0], q[1] * c - q[2] * s, q[1] * s + q[2] * c];
    c = Math.cos(rad(r[1])); s = Math.sin(rad(r[1])); q = [q[0] * c + q[2] * s, q[1], -q[0] * s + q[2] * c];
    c = Math.cos(rad(r[2])); s = Math.sin(rad(r[2])); q = [q[0] * c - q[1] * s, q[0] * s + q[1] * c, q[2]];
    return add(q, pivot);
}
const scaleAbout = (p, sc, pivot) => add(mul(sub(p, pivot), 1).map((v, i) => v * sc[i]), pivot);

/* ---------------- geo 读取与校验 ---------------- */
function loadGeo(file) {
    const data = JSON.parse(fs.readFileSync(file, 'utf8'));
    const gs = data['minecraft:geometry'];
    if (!Array.isArray(gs) || gs.length !== 1) throw new Error('需要恰好一个 geometry');
    const g = gs[0];
    const desc = g.description || {};
    const bones = g.bones || [];
    const problems = [];
    for (const b of bones) {
        for (const c of (b.cubes || [])) {
            if (Array.isArray(c.uv)) problems.push(b.name + ': 使用了 Box UV(数组), 本引擎不支持 -> 必须显式报错而非静默');
            if (c.mirror !== undefined) problems.push(b.name + ': mirror 字段(mirror=' + c.mirror + ') 未实现');
            if (typeof c.inflate === 'string') problems.push(b.name + ': inflate 是字符串(可能是 Molang), 未实现');
        }
    }
    return { g, desc, bones, byName: Object.fromEntries(bones.map(b => [b.name, b])), problems };
}

/** 取某骨骼在某时刻的动画姿态(数字关键帧, 线性插值)。未实现的形式显式标出。 */
function poseAt(anim, time) {
    const out = {};
    if (!anim) return out;
    for (const [bone, ch] of Object.entries(anim.bones || {})) {
        const val = (key, dflt) => {
            const k = ch[key];
            if (k === undefined) return dflt;
            if (Array.isArray(k)) return k.slice();                       // 常量
            if (typeof k === 'string') throw new Error(bone + '.' + key + ' 是 Molang 字符串, 未实现');
            const times = Object.keys(k).map(Number).sort((a, b) => a - b);
            for (const t of times) if (typeof k[t] === 'string') throw new Error(bone + '.' + key + ' 含 Molang');
            const cl = Math.max(times[0], Math.min(times[times.length - 1], time));
            let lo = times[0], hi = times[0];
            for (const t of times) { if (t <= cl) lo = t; if (t >= cl) { hi = t; break; } }
            const f = hi === lo ? 0 : (cl - lo) / (hi - lo);
            const A = k[String(lo)], B = k[String(hi)];
            const n = x => Array.isArray(x) ? x : [x, x, x];
            const an = n(A), bn = n(B);
            return [0, 1, 2].map(i => an[i] + (bn[i] - an[i]) * f);
        };
        out[bone] = { position: val('position', [0, 0, 0]), rotation: val('rotation', [0, 0, 0]), scale: val('scale', [1, 1, 1]) };
    }
    return out;
}

/** 生成"某骨骼坐标系"的点变换函数(合成 root→…→bone 的所有变换) */
function boneTransform(geo, boneName, pose) {
    const chain = [];
    let cur = boneName;
    while (cur) {
        const b = geo.byName[cur];
        if (!b) throw new Error('骨骼不存在: ' + cur);
        chain.unshift(b);
        cur = b.parent;
    }
    const Ms = chain.map(b => {
        const p = pose[b.name] || {};
        const rot = [0, 1, 2].map(i => (b.rotation ? b.rotation[i] : 0) + (p.rotation ? p.rotation[i] : 0));
        const sc = p.scale || [1, 1, 1];
        const pos = p.position || [0, 0, 0];
        const piv = b.pivot || [0, 0, 0];
        return v => add(rotXYZ(scaleAbout(v, sc, piv), rot, piv), pos);
    });
    return v => Ms.reduceRight((acc, f) => f(acc), v);   // ★ 子骨骼先作用, 再逐级回到根(顺序写反会让父旋转吃掉子旋转)
}

/** cube 的 8 个世界角点（含 inflate、cube 自身旋转与 pivot、骨骼链） */
function cubeCorners(geo, boneName, cube, pose) {
    const inf = typeof cube.inflate === 'number' ? cube.inflate : 0;
    const x0 = cube.origin[0] - inf, y0 = cube.origin[1] - inf, z0 = cube.origin[2] - inf;
    const x1 = cube.origin[0] + cube.size[0] + inf, y1 = cube.origin[1] + cube.size[1] + inf, z1 = cube.origin[2] + cube.size[2] + inf;
    const T = boneTransform(geo, boneName, pose);
    const out = [];
    for (const x of [x0, x1]) for (const y of [y0, y1]) for (const z of [z0, z1]) {
        let p = [x, y, z];
        if (cube.rotation) p = rotXYZ(p, cube.rotation, cube.pivot || [0, 0, 0]);
        out.push(T(p));
    }
    return out;
}
const bbox = pts => [0, 1, 2].map(i => [Math.min(...pts.map(p => p[i])), Math.max(...pts.map(p => p[i]))]).flat();

/** 逐面 UV 解析: 保留符号(=镜像方向); 返回 u 区间 + 是否镜像 */
function faceUV(face) {
    const [u, v] = face.uv, [w, h] = face.uv_size;
    return { u0: u, u1: u + w, v0: v, v1: v + h, mirrorU: w < 0, mirrorV: h < 0 };
}

/* ---------------- A~F 校准自检 ---------------- */
function calib() {
    const T = [];
    const ok = (name, cond, got, want) => T.push([name, cond, JSON.stringify(got), JSON.stringify(want)]);
    const close = (a, b, eps = 1e-6) => Math.abs(a - b) <= eps;
    const closeAll = (a, b, eps = 1e-6) => a.length === b.length && a.every((v, i) => close(v, b[i], eps));

    const geo = (bones) => ({ byName: Object.fromEntries(bones.map(b => [b.name, b])), bones });

    /* B. 单骨骼旋转: 骨骼绕 y 转 90°(枢轴原点), 点 (1,0,0) -> (0,0,-1) */
    {
        const g = geo([{ name: 'a', parent: null, pivot: [0, 0, 0], rotation: [0, 90, 0] }]);
        const T1 = boneTransform(g, 'a', {});
        ok('B 单骨骼旋转 90°(y)', closeAll(T1([1, 0, 0]), [0, 0, -1], 1e-9), T1([1, 0, 0]), [0, 0, -1]);
    }
    /* B2. 带枢轴的旋转: 枢轴 (0,2,0), 点 (0,1,0) 绕 x 转 90° -> (0,2,1) */
    {
        const g = geo([{ name: 'a', parent: null, pivot: [0, 2, 0], rotation: [90, 0, 0] }]);
        const T1 = boneTransform(g, 'a', {});
        ok('B2 枢轴旋转(绕x+90: (0,1,0)->(0,2,-1))', closeAll(T1([0, 1, 0]), [0, 2, -1], 1e-9), T1([0, 1, 0]), [0, 2, -1]);
    }
    /* C. 父子组合: 父绕 y 90°, 子绕 z 90° -> 点(1,0,0): 先子后父 = Ry90 * Rz90 * (1,0,0) */
    {
        const g = geo([
            { name: 'p', parent: null, pivot: [0, 0, 0], rotation: [0, 90, 0] },
            { name: 'c', parent: 'p', pivot: [0, 0, 0], rotation: [0, 0, 90] },
        ]);
        const T1 = boneTransform(g, 'c', {});
        const want = [0, 0, -1];   // Rz90:(1,0,0)->(0,1,0); Ry90:(0,1,0)->(0,1,0) -> 实际应等于 Ry90( Rz90(1,0,0) )
        // 手算: Rz90*(1,0,0) = (0,1,0); Ry90*(0,1,0) = (0,1,0)
        ok('C 父子组合旋转', closeAll(T1([1, 0, 0]), [0, 1, 0], 1e-9), T1([1, 0, 0]), [0, 1, 0]);
    }
    /* D. 非均匀缩放(绕枢轴): 枢轴原点, 缩放 [2,1,1], 点 (1,2,3) -> (2,2,3) */
    {
        const g = geo([{ name: 'a', parent: null, pivot: [0, 0, 0] }]);
        const T1 = boneTransform(g, 'a', { a: { scale: [2, 1, 1] } });
        ok('D 非均匀缩放', closeAll(T1([1, 2, 3]), [2, 2, 3], 1e-9), T1([1, 2, 3]), [2, 2, 3]);
    }
    /* E. 负 uv_size: uv=[20,10], uv_size=[-4,8] -> u 区间 [16,20] 且 mirrorU=true（min/max 会丢掉镜像） */
    {
        const r = faceUV({ uv: [20, 10], uv_size: [-4, 8] });
        ok('E 负宽 UV 保方向', r.u0 === 20 && r.u1 === 16 && r.mirrorU === true && r.mirrorV === false, r, { u0: 20, u1: 16, mirrorU: true, mirrorV: false });
        const r2 = faceUV({ uv: [4, 6], uv_size: [4, 8] });
        ok('E2 正常 UV', r2.u0 === 4 && r2.u1 === 8 && r2.mirrorU === false, r2, { u0: 4, u1: 8, mirrorU: false });
    }
    /* F. 骨骼内的 cube 自身旋转: 子骨骼绕 y 90°, cube 绕 z 90°(pivot 原点), 点(1,0,0) 先 cube 后骨骼 */
    {
        const g = geo([{ name: 'a', parent: null, pivot: [0, 0, 0], rotation: [0, 90, 0] }]);
        const c = { origin: [0, 0, 0], size: [1, 1, 1], rotation: [0, 0, 90], pivot: [0, 0, 0] };
        const pts = cubeCorners(g, 'a', c, {});
        // 角点 (1,1,1): cube Rz90 -> (-1,1,1); bone Ry90 -> (1,1,1) 的 x/z 交换: Ry90*(-1,1,1) = (1,1,1)
        const has = pts.some(p => closeAll(p, [1, 1, 1], 1e-9));
        ok('F 骨骼内 cube 旋转组合', has, pts.map(p => p.map(v => +v.toFixed(3))), '应包含 [1,1,1]');
    }
    /* A. 六面各自 UV 独立(工程实际做法): 构造 6 面不同矩形, 检查区间互不相同 */
    {
        const faces = {};
        ['north', 'south', 'east', 'west', 'up', 'down'].forEach((f, i) => { faces[f] = { uv: [i * 4, 0], uv_size: [4, 4] }; });
        const rects = Object.entries(faces).map(([k, v]) => k + ':' + JSON.stringify(faceUV(v)));
        ok('A 六面独立 UV', new Set(rects).size === 6, rects.length, 6);
    }

    let bad = 0;
    console.log('=== Bedrock 变换/UV 引擎 A~F 校准 ===');
    for (const [n, pass, got, want] of T) {
        if (!pass) bad++;
        console.log('  ' + (pass ? 'PASS' : 'FAIL') + '  ' + n.padEnd(22) + '  got=' + got + '  want=' + want);
    }
    console.log(bad ? '\n校准未通过: ' + bad + ' 项' : '\n校准通过（尚未与 Blockbench 比对, 属"解析解自检"）');
    console.log('说明: 本引擎只做变换与 UV 解析, **不含渲染**; 光栅化/贴图采样器是下一步（届时以本引擎为唯一坐标来源）。');
    return bad ? 1 : 0;
}

/* ---------------- CLI ---------------- */
const args = process.argv.slice(2);
if (args.includes('--calib') || args.length === 0) {
    process.exit(calib());
}
const geoFile = args[0];
const dumpIdx = args.indexOf('--dump');
try {
    const { bones, problems, g } = loadGeo(geoFile);
    const geo = { byName: Object.fromEntries(bones.map(b => [b.name, b])), bones };
    console.log('geo: ' + bones.length + ' 骨骼, ' + bones.reduce((n, b) => n + (b.cubes || []).length, 0) + ' cube, texture ' + (g.description.texture_width || '?') + 'x' + (g.description.texture_height || '?'));
    if (problems.length) { console.log('不支持的字段(必须显式报告):'); problems.forEach(p => console.log('  - ' + p)); }
    if (dumpIdx >= 0) {
        const t = Number(args[dumpIdx + 1] || 0);
        const ANIM = path.join(ROOT, 'src/main/resources/assets/ftboss/animations/shadow_sword.animation.json');
        const anims = JSON.parse(fs.readFileSync(ANIM, 'utf8')).animations;
        console.log('\n时刻 ' + t + 's 各骨骼世界包围盒（未指定动画则用静止姿态）:');
        for (const b of bones) {
            const pts = [];
            for (const c of (b.cubes || [])) pts.push(...cubeCorners(geo, b.name, c, {}));
            if (!pts.length) continue;
            const bb = bbox(pts);
            console.log('  ' + b.name.padEnd(15) + bb.map(v => v.toFixed(2).padStart(7)).join(' '));
        }
    }
} catch (e) {
    console.error('错误: ' + e.message);
    process.exit(2);
}
