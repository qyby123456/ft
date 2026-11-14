#!/usr/bin/env node
'use strict';

/* ============================================================================
 *  cleaver_check.cjs —— black_cleaver 结构体检 + 刃翼拆分的量化守门
 *
 *  为什么需要它：
 *    拆刃翼骨骼最容易踩的两条硬规矩（AGENTS.md 有记录，都是实测炸过的）：
 *      ① 相邻段必须**重叠**（几何上传动/视觉才不断裂）—— 目标 ≥2 单位；
 *      ② 相邻段**每个轴**都要错开 ≥0.2 单位，否则同平面 z-fighting 闪烁。
 *    这两条光靠肉眼看不出来，必须算。
 *
 *  本脚本**只读**，不写任何文件。
 *
 *  用法：
 *    node tools/model3d/cleaver_check.cjs
 *    node tools/model3d/cleaver_check.cjs --geo <file> --anim <file>
 *    node tools/model3d/cleaver_check.cjs --blade blade          # 指定刃翼父骨骼
 *    node tools/model3d/cleaver_check.cjs --json                 # 同时写报告
 * ==========================================================================*/

const fs = require('fs');
const path = require('path');
const cube = require('./cube_space.cjs');

const ROOT = path.resolve(__dirname, '..', '..');
const AS = path.join(ROOT, 'src', 'main', 'resources', 'assets', 'ftboss');

/** 相邻段最小重叠（模型单位，1 格 = 16 单位）。 */
const MIN_OVERLAP = 2.0;

/** 相邻段每轴最小错开（模型单位）——低于它就同平面闪烁。 */
const MIN_STAGGER = 0.2;

function parseArgs(argv) {
    const o = {
        geo: path.join(AS, 'geo', 'black_cleaver.geo.json'),
        anim: path.join(AS, 'animations', 'black_cleaver.animation.json'),
        blade: 'blade',
        json: false
    };

    for (let i = 0; i < argv.length; i++) {
        const a = argv[i];
        if (a === '--geo') o.geo = argv[++i];
        else if (a === '--anim') o.anim = argv[++i];
        else if (a === '--blade') o.blade = argv[++i];
        else if (a === '--json') o.json = true;
        else if (a === '--help') { o.help = true; }
        else cube.fail(`未知参数：${a}`);
    }

    return o;
}

/** 骨骼静止姿态下所有 cube 的 AABB（模型单位，子→父骨骼链已作用）。 */
function boneBounds(model, boneName) {
    const bone = model.byName.get(boneName);
    const pts = [];

    for (const info of cube.listCubes(model)) {
        if (info.bone !== boneName) continue;
        pts.push(...cube.cubeCorners(model, info, {}));
    }

    if (pts.length === 0) return null;

    return [0, 1, 2].map(axis => {
        const v = pts.map(p => p[axis]);
        return [Math.min(...v), Math.max(...v)];
    });
}

/** 两个 AABB 的重叠体积与逐轴重叠量。 */
function overlapOf(a, b) {
    const per = [0, 1, 2].map(axis => {
        const lo = Math.max(a[axis][0], b[axis][0]);
        const hi = Math.min(a[axis][1], b[axis][1]);
        return hi - lo;               // 负数 = 该轴分离
    });

    const volume = per.every(v => v > 0)
        ? per[0] * per[1] * per[2]
        : 0;

    return { per, volume };
}

/**
 * 两个 AABB 是否"每轴都错开 ≥ MIN_STAGGER"（不是精确共面）。
 * 逐轴看：要么分离，要么重叠量够大；只有"几乎贴合"才算可疑。
 */
function staggerReport(a, b) {
    const suspicious = [];

    for (let axis = 0; axis < 3; axis++) {
        const ov = Math.min(a[axis][1], b[axis][1]) - Math.max(a[axis][0], b[axis][0]);

        if (ov > 0 && ov < MIN_STAGGER) {
            suspicious.push({
                axis: ['x', 'y', 'z'][axis],
                overlap: cube.round(ov, 3)
            });
        }
    }

    return suspicious;
}

function boundsOf(model, boneName) {
    const bone = model.byName.get(boneName);

    if (!bone) return null;

    const direct = boneBounds(model, boneName);
    const kids = model.bones.filter(b => b.parent === boneName);

    if (kids.length === 0) return direct;

    // 父骨骼自身没有 cube 时，用后代的并集当它的范围
    const all = [];

    for (const k of kids) {
        const kb = boundsOf(model, k.name);
        if (kb) all.push(kb);
    }

    if (direct) all.push(direct);
    if (all.length === 0) return null;

    return [0, 1, 2].map(axis => [
        Math.min(...all.map(b => b[axis][0])),
        Math.max(...all.map(b => b[axis][1]))
    ]);
}

function main() {
    const o = parseArgs(process.argv.slice(2));

    if (o.help) {
        console.log('用法：node tools/model3d/cleaver_check.cjs [--geo f] [--anim f] [--blade name] [--json]');
        return 0;
    }

    const model = cube.loadGeo(o.geo);
    const out = [];
    const problems = [];

    out.push('=== black_cleaver 结构体检 + 刃翼拆分守门 ===');
    out.push(`geo : ${path.relative(ROOT, o.geo)}`);
    out.push(`骨骼 ${model.bones.length} 个，立方体 ${cube.listCubes(model).length} 个`);
    out.push('');

    /* ---------- 1. 基础结构 ---------- */
    out.push('--- 1. 结构自洽 ---');

    const nameSeen = new Set();
    const dup = [];

    for (const b of model.bones) {
        if (nameSeen.has(b.name)) dup.push(b.name);
        nameSeen.add(b.name);
    }

    out.push((dup.length === 0 ? '  ✓ ' : '  ✗ ') +
        '骨骼名唯一' + (dup.length ? '（重复：' + dup.join(',') + '）' : ''));

    if (dup.length) problems.push('骨骼重名：' + dup.join(','));

    /* 父级链（cube_space.getBoneChain 会自己断言循环/悬空，这里逐个走一遍） */
    const chainErrors = [];

    for (const b of model.bones) {
        try {
            cube.getBoneChain(model, b.name);
        } catch (e) {
            chainErrors.push(b.name + ': ' + e.message);
        }
    }

    out.push((chainErrors.length === 0 ? '  ✓ ' : '  ✗ ') +
        '父级链完整无环' + (chainErrors.length ? '（' + chainErrors.slice(0, 4).join('; ') + '）' : ''));

    if (chainErrors.length) problems.push(...chainErrors.slice(0, 4));

    /* cube 字段（GeckoLib 严格：多写 name 会抛异常） */
    const badCube = [];

    for (const info of cube.listCubes(model)) {
        const c = info.cube;

        if (c.name !== undefined) badCube.push(info.name + ': 多余 name 字段');
        if (!c.uv) badCube.push(info.name + ': 缺 uv');
        if (!Array.isArray(c.origin) || c.origin.length !== 3) badCube.push(info.name + ': origin');
        if (!Array.isArray(c.size) || c.size.length !== 3) badCube.push(info.name + ': size');
    }

    out.push((badCube.length === 0 ? '  ✓ ' : '  ✗ ') +
        `立方体字段合法（${cube.listCubes(model).length} 个）` +
        (badCube.length ? '（' + badCube.slice(0, 4).join(', ') + '）' : ''));

    if (badCube.length) problems.push(...badCube.slice(0, 4));

    /* ★ 重复 cube：同一骨骼上 origin 与 size 完全相同的两块 = 必然逐面 z-fighting。
       2026-09-15 真踩到：把左刃改成 "镜像对" 时，左边既保留了原来的 addBox，
       又加了一次 addPairBox（它本身就会给左右各加一份）→ 左侧重复 3 块。
       光看 validate.cjs 是看不出来的（结构完全合法），只有数 cube 数才露馅。 */
    const dupCube = [];

    for (const bone of model.bones) {
        const seen = new Map();

        for (const c of bone.cubes || []) {
            const key = c.origin.join(',') + '|' + c.size.join(',');

            if (seen.has(key)) {
                dupCube.push(`${bone.name} 上重复 cube @${key}`);
            } else {
                seen.set(key, true);
            }
        }
    }

    out.push((dupCube.length === 0 ? '  ✓ ' : '  ✗ ') +
        '无重复 cube（同骨骼同 origin+size）' +
        (dupCube.length ? '（' + dupCube.slice(0, 4).join('; ') + '）' : ''));

    if (dupCube.length) problems.push(...dupCube.slice(0, 4));

    /* 动画引用 */
    let animReport = null;

    if (fs.existsSync(o.anim)) {
        const anim = JSON.parse(fs.readFileSync(o.anim, 'utf8'));
        const animations = anim.animations || anim;
        const orphan = [];

        for (const [an, a] of Object.entries(animations)) {
            for (const boneName of Object.keys(a.bones || {})) {
                if (!model.byName.has(boneName)) orphan.push(`${an} -> ${boneName}`);
            }
        }

        out.push((orphan.length === 0 ? '  ✓ ' : '  ✗ ') +
            `动画骨骼引用全部存在（${Object.keys(animations).length} 条）` +
            (orphan.length ? '（悬空：' + orphan.slice(0, 6).join(', ') + '）' : ''));

        if (orphan.length) problems.push('动画悬空引用：' + orphan.slice(0, 6).join(','));

        animReport = { count: Object.keys(animations).length, orphan };
    } else {
        out.push('  · 未找到动画文件，跳过引用检查');
    }

    /* ---------- 2. 刃翼拆分守门 ---------- */
    out.push('');
    out.push(`--- 2. 刃翼拆分守门（父骨骼 "${o.blade}"）---`);

    const parentBone = model.byName.get(o.blade);

    if (!parentBone) {
        out.push(`  · 没有名为 "${o.blade}" 的骨骼，跳过（用 --blade 指定）；当前顶层骨骼：` +
            model.bones.filter(b => !b.parent).map(b => b.name).join(', '));
        problems.push(`--blade 指定的骨骼不存在：${o.blade}`);
    } else {
        const kids = model.bones.filter(b => b.parent === o.blade);

        out.push(`  ${o.blade} 有 ${kids.length} 个子骨骼：` +
            (kids.length ? kids.map(k => k.name).join(', ') : '（无）'));

        if (kids.length === 0) {
            out.push('  · 刃翼还没拆（预期如此）—— 拆完再跑本脚本，会逐对算重叠/错开。');
        } else {
            const bounds = new Map();

            for (const k of kids) {
                const b = boundsOf(model, k.name);
                if (b) bounds.set(k.name, b);
            }

            /* 父骨骼**自己身上**的几何（不含子骨骼）—— 本工程的拆法是
               "上翼 / 静止中刃 / 下翼"三段，中刃留在 blade 自己身上，
               所以它是判断"两片刃翼是否相邻"以及"刃翼转开会不会飘"的关键。 */
            const parentOwn = boneBounds(model, o.blade);

            for (let i = 0; i < kids.length; i++) {
                for (let j = i + 1; j < kids.length; j++) {
                    const a = bounds.get(kids[i].name);
                    const b = bounds.get(kids[j].name);

                    if (!a || !b) continue;

                    const { per, volume } = overlapOf(a, b);
                    const stagger = staggerReport(a, b);

                    const label = `${kids[i].name} × ${kids[j].name}`;
                    const maxOv = Math.max(...per);

                    if (volume <= 0) {
                        /* "兄弟骨骼不重叠"只有在两者**本来是同一根链条的相邻段**时才是缺陷。
                           若父骨骼自身的几何同时与双方重叠（几何上夹在中间），
                           它们就不是相邻段 —— 此时该查的是"各自与父几何有没有咬住"（见下面新增的门槛），
                           以及"转开会不会撕出缝"（交给 wing_joint_check.cjs 按角度量化）。 */
                        const bridged = parentOwn &&
                            overlapOf(parentOwn, a).volume > 0 &&
                            overlapOf(parentOwn, b).volume > 0;

                        if (bridged) {
                            out.push(`  · ${label}：不相邻（中间隔着父骨骼 "${o.blade}" 自己的几何）` +
                                `—— 不是同一根链条的相邻段，本条跳过，改查下方"× 父骨骼自身几何"`);
                        } else {
                            out.push(`  ✗ ${label}：**不重叠**（逐轴重叠 ${per.map(v => cube.round(v, 2)).join(', ')}）`);
                            problems.push(`${label} 不重叠，刃翼会断裂`);
                        }
                    } else if (maxOv < MIN_OVERLAP) {
                        out.push(`  ⚠ ${label}：重叠偏小 ${cube.round(volume, 2)}（最大轴 ${cube.round(maxOv, 2)} < ${MIN_OVERLAP}）`);
                        problems.push(`${label} 重叠 < ${MIN_OVERLAP} 单位`);
                    } else {
                        out.push(`  ✓ ${label}：重叠体积 ${cube.round(volume, 2)}，最大轴 ${cube.round(maxOv, 2)}`);
                    }

                    if (stagger.length > 0) {
                        out.push('      ⚠ 近乎共面：' +
                            stagger.map(s => `${s.axis} 轴仅错开 ${s.overlap}`).join('，') +
                            `（要求 ≥${MIN_STAGGER}，否则 z-fighting 闪烁）`);
                        problems.push(`${label} 存在近乎共面的轴`);
                    }
                }
            }

            /* 硬门槛：每片刃翼都必须与**父骨骼自身的几何**咬住，
               否则它一转开就是个凭空飘着的零件。 */
            if (parentOwn) {
                for (const k of kids) {
                    const kb = bounds.get(k.name);
                    if (!kb) continue;

                    const { per, volume } = overlapOf(parentOwn, kb);
                    const mo = Math.max(...per);

                    if (volume <= 0 || mo < MIN_OVERLAP) {
                        out.push(`  ✗ ${k.name} × 父骨骼自身几何：咬不住` +
                            `（逐轴重叠 ${per.map(v => cube.round(v, 2)).join(', ')}，要求最大轴 ≥${MIN_OVERLAP}）`);
                        problems.push(`${k.name} 与父骨骼自身几何重叠 < ${MIN_OVERLAP} 单位`);
                    } else {
                        out.push(`  ✓ ${k.name} × 父骨骼自身几何：重叠体积 ${cube.round(volume, 2)}，最大轴 ${cube.round(mo, 2)}`);
                    }
                }
            }
        }
    }

    /* ---------- 3. 全模型相邻骨骼错开体检 ---------- */
    out.push('');
    out.push('--- 3. 父子骨骼"近乎共面"体检（全模型）---');

    let coplanar = 0;

    for (const b of model.bones) {
        if (!b.parent) continue;

        const pb = boundsOf(model, b.parent);
        const cb = boundsOf(model, b.name);

        if (!pb || !cb) continue;

        const stagger = staggerReport(pb, cb);

        // 只有"既重叠、又贴得极近"才算可疑；父子故意埋进去是正常的，
        // 这里只报"重叠量小于阈值"的轴，属于提示不是错误。
        if (stagger.length >= 2) {
            coplanar++;
            out.push(`  · ${b.name} × ${b.parent}：` +
                stagger.map(s => `${s.axis} 仅 ${s.overlap}`).join('，') + '（提示）');
        }
    }

    if (coplanar === 0) {
        out.push('  ✓ 未发现父子骨骼多轴近乎共面');
    }

    /* ---------- 汇总 ---------- */
    out.push('');
    out.push('--- 汇总 ---');
    out.push(`  问题 ${problems.length} 项`);

    for (const p of problems) out.push('    · ' + p);

    out.push('  门槛：相邻段重叠 ≥ ' + MIN_OVERLAP + ' 单位、每轴错开 ≥ ' + MIN_STAGGER + ' 单位');
    out.push('  （依据 AGENTS.md「装饰件贴在主体上必须每个轴都错开 ≥0.2」与「用重叠串联代替旋转拼接」）');

    const text = out.join('\n');
    console.log(text);

    if (o.json) {
        const file = path.join(ROOT, 'art', 'cleaver_check.json');
        fs.mkdirSync(path.dirname(file), { recursive: true });
        fs.writeFileSync(file, JSON.stringify({
            geo: path.relative(ROOT, o.geo),
            anim: animReport,
            problems,
            thresholds: { minOverlap: MIN_OVERLAP, minStagger: MIN_STAGGER }
        }, null, 2), 'utf8');
        console.log(`\n报告：${path.relative(ROOT, file)}`);
    }

    return problems.length === 0 ? 0 : 1;
}

try {
    process.exitCode = main();
} catch (error) {
    console.error('错误：' + error.message);
    process.exitCode = 2;
}
