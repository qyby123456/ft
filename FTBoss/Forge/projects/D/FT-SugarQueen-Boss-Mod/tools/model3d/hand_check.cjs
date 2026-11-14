#!/usr/bin/env node
'use strict';

/* ============================================================================
 *  hand_check.cjs —— 第三人称**比例参考图**：玩家参照体 + 手持影刃
 *
 *  本工具能做什么：
 *    · 用同一套 cubeCorners()（子→父骨骼链）把影刃的立方体放进玩家参照空间
 *    · 画出参照体的头/躯干/双臂/双腿比例、手部参考点(红点)、地平面网格
 *    · 用共享 Z-buffer 处理"手掌包住握柄"这类互相遮挡
 *
 *  本工具**不能**做什么（不要把结果当成游戏内正确握持）：
 *    · 没有复现玩家持物手臂姿态（PlayerModel 的 arm pose）
 *    · 没有复现左右手的运行时镜像规则
 *    · 没有复现 GeckoLib 渲染器的额外变换
 *    · 只有第三人称槽位；第一人称需要独立相机与手部变换
 *
 *  结论用法：只用于判断"剑长/护手宽度/握柄相对手心的偏移"这类比例问题；
 *  不可据此自动改写正式 display。
 *
 *  用法：
 *    node tools/model3d/hand_check.cjs [slot] [zoom] [选项]
 *
 *  位置参数：
 *    slot   thirdperson_righthand(默认) | thirdperson_lefthand
 *    zoom   相机倍率(> 0，默认 1；越大越近)
 *
 *  选项：
 *    --out <file>    输出 PNG（默认 art/hand_check.png）
 *    --zoom <n>      相机倍率（等价于第二个位置参数）
 *    --slot <name>   槽位（等价于第一个位置参数）
 *    --hand          只取景手部区域（放大看握柄与手心）
 *    --geo <file>    覆盖 geo 路径
 *    --item <file>   覆盖物品模型路径
 *    --tex <file>    覆盖贴图路径
 *    --help          显示帮助
 *
 *  退出码：0 成功，2 参数或数据错误
 * ==========================================================================*/

const fs = require('fs');
const path = require('path');

const cube = require('./cube_space.cjs');
const { decodePNG, encodePNG } = require('./png.cjs');

const ROOT = path.resolve(__dirname, '..', '..');
const AS = path.join(ROOT, 'src', 'main', 'resources', 'assets', 'ftboss');

const DEFAULTS = {
    geo: path.join(AS, 'geo', 'shadow_sword.geo.json'),
    item: path.join(AS, 'models', 'item', 'shadow_sword.json'),
    tex: path.join(AS, 'textures', 'item', 'shadow_sword.png'),
    out: path.join(ROOT, 'art', 'hand_check.png')
};

/* 只支持第三人称：第一人称不能用站立玩家的手臂位置代替。 */
const SUPPORTED_SLOTS = new Set([
    'thirdperson_righthand',
    'thirdperson_lefthand'
]);

/* ============================================================
 * 1. 参照体的坐标/镜像约定
 *
 *  Minecraft 渲染手持物品时，右手会额外套一层 scale(-1, 1, 1)：
 *      poseStack.scale(-1, 1, 1)   // 右手（ItemInHandRenderer）
 *  也就是说 display 的平移/旋转是写在**手部局部空间**里的，右手那个
 *  局部空间相对身体是镜像的。本工具用同一套镜像来搭参考图：
 *
 *      右手：swordView = applyDisplay( mirrorX(geoBlocks) )
 *      左手：swordView = applyDisplay( geoBlocks )
 *
 *  于是右手的手部参考点落在 +X 侧，左手落在 -X 侧。
 *  镜像是对整幅图统一做的，所以 display 的旋转数值不用改符号。
 *  这仍是简化约定，正式校准必须在游戏渲染器里画坐标轴。
 * ==========================================================*/

const MIRROR_RIGHT_HAND = true;

/* ============================================================
 * 2. 参照体：经典玩家比例（1 方块 = 16 单位，脚底 y = 0）
 *
 *  头 8×8×8 单位、躯干 8×12×4、手臂 4×12×4、腿 4×12×4。
 *  这套尺寸是**渲染模型**比例，不等于 1.8 格碰撞箱。
 * ==========================================================*/

const GREY = [122, 122, 132];
const GREY2 = [98, 98, 108];
const MARKER = [236, 64, 64];
const GROUND = [150, 156, 170];

const player = [
    {
        n: 'head',
        from: [-0.25, 1.5, -0.25],
        to: [0.25, 2.0, 0.25],
        c: GREY2
    },
    {
        n: 'torso',
        from: [-0.25, 0.75, -0.125],
        to: [0.25, 1.5, 0.125],
        c: GREY
    },
    {
        n: 'armR',                              // 右手在 +X 侧（镜像约定）
        from: [-0.5, 0.75, -0.125],
        to: [-0.25, 1.5, 0.125],
        c: GREY
    },
    {
        n: 'armL',
        from: [0.25, 0.75, -0.125],
        to: [0.5, 1.5, 0.125],
        c: GREY
    },
    {
        n: 'legR',
        from: [-0.25, 0, -0.125],
        to: [0, 0.75, 0.125],
        c: GREY2
    },
    {
        n: 'legL',
        from: [0, 0, -0.125],
        to: [0.25, 0.75, 0.125],
        c: GREY2
    }
];

/* 简化参照点，**不宣称**等于 Minecraft 的实际手部挂接矩阵。
   取"握拳的拳心"：手臂在 y 0.75~1.5，拳头约 1/3 在手腕以下
   （玩家手臂 12 单位长、拳 4 单位高 → 拳心比手腕低约 1.3 单位 = 0.083 格），
   所以拳心 ≈ 1.5 - 0.75 - 0.083 = 0.667；再往身前挪一点（握东西时拳在身前）。
   早先取 0.8125（= 手腕位置）偏高 0.13 格，会让"剑看起来偏低"是假象。 */
function handReference(slot) {
    const leftHand = slot === 'thirdperson_lefthand';

    return [
        leftHand ? 0.375 : -0.375,
        0.667,
        0.06
    ];
}

/* 不画的骨骼根（含全部后代）：静态聚能球/光效不该出现在比例图里。
   注意：名字里带 glow 不代表就该隐藏，必须显式配置。 */
const HIDDEN_ROOTS = new Set([
    'charge_orb',
    'blade_glow',
    'eye_glow',
    'crystal_glow'
]);

/* 立方体角点索引 → 每面 4 个角点（贴图左上/右上/右下/左下）。
   index 的 bit 约定见 cube_space.cubeCorners()（index = 4x + 2y + z）。
   绕序必须是**外法线**方向；resolveFaceOrder() 会把写反的面自动翻转，
   变量声明顺序为 [uv 左上, 右上, 右下, 左下]。
   这份 UV 对应关系仍需用六面方向贴图对照 Blockbench 校准。 */
const FACE_UV_ORDER = {
    north: [0, 4, 6, 2],
    south: [3, 7, 5, 1],
    west: [2, 3, 1, 0],
    east: [7, 6, 4, 5],
    up: [2, 6, 7, 3],
    down: [1, 5, 4, 0]
};

/* 每面在 geo 空间的外法线（用于背面剔除）。 */
const FACE_NORMAL = {
    north: [0, 0, -1],
    south: [0, 0, 1],
    west: [-1, 0, 0],
    east: [1, 0, 0],
    up: [0, 1, 0],
    down: [0, -1, 0]
};

function cornerAt(index) {
    return [
        (index >> 2) & 1,
        (index >> 1) & 1,
        index & 1
    ];
}

/* 每个面所在的固定轴（角点索引 bit：4=x, 2=y, 1=z）与该轴上的固定取值。 */
const FACE_FIXED_AXIS = {
    north: { axis: 2, value: 0 },
    south: { axis: 2, value: 1 },
    west: { axis: 0, value: 0 },
    east: { axis: 0, value: 1 },
    down: { axis: 1, value: 0 },
    up: { axis: 1, value: 1 }
};

/** 角点在"剩余两轴"上的取值 → 贴图四角（0 左上 / 1 右上 / 2 右下 / 3 左下）。 */
const UV_SLOT_OF_KEY = {
    '00': 0,   // 左上
    '01': 1,   // 右上
    '11': 2,   // 右下
    '10': 3    // 左下
};

/**
 * 为某个面建 8 项 UV 查找表（按角点索引 4x+2y+z）。
 *
 * 固定轴上的那一维不参与贴图；剩下两轴依次对应贴图的
 * 「竖直方向」「水平方向」，于是四角落在 (0,0)(1,0)(1,1)(0,1)。
 * 与 GeckoLib 的实际贴图朝向仍需实机校准。
 */
function buildUvTable(name, quad) {
    const fixed = FACE_FIXED_AXIS[name];

    cube.assert(fixed, `未知面：${name}`);

    const axes = [0, 1, 2].filter(axis => axis !== fixed.axis);
    const table = new Array(8).fill(null);
    const seen = [];
    const groups = new Map();

    for (let i = 0; i < 8; i++) {
        const at = cornerAt(i);

        if (at[fixed.axis] !== fixed.value) continue;

        groups.set(`${at[axes[0]]}${at[axes[1]]}`, i);
    }

    cube.assert(
        groups.size === 4,
        `面 ${name} 的角点没有分成 4 组（实际 ${groups.size}）`
    );

    for (const [key, index] of groups) {
        const slot = UV_SLOT_OF_KEY[key];

        cube.assert(
            slot !== undefined,
            `面 ${name} 的组合 ${key} 没有贴图角`
        );

        table[index] = quad[slot];
        seen.push(slot);
    }

    cube.assert(
        new Set(seen).size === 4,
        `面 ${name} 的贴图四角被重复占用：${seen.join(',')}`
    );

    return table;
}

/**
 * 让绕序朝外。
 *
 * 判据必须用立方体中心：0..1 的立方体中心是 (0.5, 0.5, 0.5)，
 * 面心到立方体中心的方向就是外法线方向。曾经用固定外法线表去校验，
 * 结果整张表被翻成朝内（渲染时背面剔除会剔掉外表面）。
 */
function resolveFaceOrder(order, name) {
    const a = cornerAt(order[0]);
    const b = cornerAt(order[1]);
    const c = cornerAt(order[2]);

    const e1 = cube.sub(b, a);
    const e2 = cube.sub(c, a);

    const cross = [
        e1[1] * e2[2] - e1[2] * e2[1],
        e1[2] * e2[0] - e1[0] * e2[2],
        e1[0] * e2[1] - e1[1] * e2[0]
    ];

    cube.assert(
        Math.hypot(cross[0], cross[1], cross[2]) > 0.5,
        `FACE_UV_ORDER.${name} 不是共面的四边形`
    );

    const outward = cube.sub([0.5, 0.5, 0.5], a);
    const dot = cross[0] * outward[0] +
        cross[1] * outward[1] +
        cross[2] * outward[2];

    return dot > 0
        ? order.slice()
        : order.slice().reverse();
}

const FACE_INDEX = Object.fromEntries(
    Object.entries(FACE_UV_ORDER).map(([name, order]) => [
        name,
        resolveFaceOrder(order, name)
    ])
);

/* ============================================================
 * 3. 参数
 * ==========================================================*/

function printHelp() {
    console.log(`
第三人称比例参考图（玩家参照体 + 手持影刃）

用法：
  node tools/model3d/hand_check.cjs [slot] [zoom] [选项]

位置参数：
  slot   thirdperson_righthand(默认) | thirdperson_lefthand
  zoom   相机倍率 > 0（默认 1）

选项：
  --out <file>    输出 PNG（默认 art/hand_check.png）
  --zoom <n>      相机倍率
  --slot <name>   槽位
  --hand          只取景手部区域（放大看握柄与手心）
  --reference-only
                  不把剑平移到手心，按 display 原位画出。
                  用来直观看"当前 display 究竟把剑放在了哪里"。
  --sword-bias <n>
                  剑整体前移的视空间深度(默认 -1.2，方块单位，负值=更近)。
                  剑正好挂在大腿后面，不前移就完全看不见；
                  传 0 可看真实遮挡关系。
  --geo <file>    覆盖 geo 路径
  --item <file>   覆盖物品模型路径
  --tex <file>    覆盖贴图路径
  --help          显示帮助

判读范围：
  本图是第三人称**简化比例**参考。
  红点 = 人工配置的手部参考点，不是游戏里的手部挂接矩阵。
  未复现实际玩家手臂姿态、左右手镜像、GeckoLib 额外变换。
  不可直接据此自动修改正式 display。
`);
}

function parseArgs(argv) {
    const options = {
        geo: DEFAULTS.geo,
        item: DEFAULTS.item,
        tex: DEFAULTS.tex,
        out: DEFAULTS.out,
        slot: null,
        zoom: null,
        hand: false,
        referenceOnly: false,
        swordBias: -1.2,
        positional: []
    };

    for (let i = 0; i < argv.length; i++) {
        const argument = argv[i];

        if (argument === '--out') {
            options.out = argv[++i];
        } else if (argument === '--zoom') {
            options.zoom = argv[++i];
        } else if (argument === '--slot') {
            options.slot = argv[++i];
        } else if (argument === '--sword-bias') {
            options.swordBias = Number(argv[++i]);
        } else if (argument === '--geo') {
            options.geo = argv[++i];
        } else if (argument === '--item') {
            options.item = argv[++i];
        } else if (argument === '--tex') {
            options.tex = argv[++i];
        } else if (argument === '--hand') {
            options.hand = true;
        } else if (argument === '--reference-only') {
            options.referenceOnly = true;
        } else if (argument === '--help' || argument === '-h') {
            printHelp();
            process.exit(0);
        } else if (argument.startsWith('--')) {
            cube.fail(`未知参数：${argument}`);
        } else {
            options.positional.push(argument);
        }
    }

    if (options.slot === null && options.positional.length > 0) {
        options.slot = options.positional[0];
    }

    if (options.zoom === null && options.positional.length > 1) {
        options.zoom = options.positional[1];
    }

    if (options.positional.length > 2) {
        cube.fail(
            '多余的位置参数：' +
            options.positional.slice(2).join(' ')
        );
    }

    options.slot = options.slot || 'thirdperson_righthand';

    if (!SUPPORTED_SLOTS.has(options.slot)) {
        cube.fail(
            `不支持的槽位：${options.slot}\n` +
            '当前工具仅支持第三人称比例参考（' +
            [...SUPPORTED_SLOTS].join(' / ') +
            '）；第一人称需要独立相机和手部变换。'
        );
    }

    const zoom = Number(options.zoom === null ? 1 : options.zoom);

    if (!Number.isFinite(zoom) || zoom <= 0) {
        cube.fail('相机倍率必须大于 0');
    }

    options.zoomValue = zoom;

    cube.assert(
        Number.isFinite(options.swordBias),
        '--sword-bias 必须是有限数字'
    );

    for (const key of ['geo', 'item', 'tex']) {
        options[key] = path.resolve(options[key]);
        cube.assert(
            fs.existsSync(options[key]),
            `文件不存在：${options[key]}`
        );
    }

    options.out = path.resolve(options.out);

    return options;
}

/* ============================================================
 * 4. 物品 display 逐字段补默认值
 *
 *  只写 rotation 的文件不能让 disp.scale[0] 直接抛 TypeError；
 *  但也要记住：display 可能继承自父模型，缺字段不等于游戏里就是默认值。
 * ==========================================================*/

function readVec3(value, fallback, label) {
    return cube.vec3(value, label, fallback);
}

function readDisplay(item, slot) {
    const raw = (item.display && item.display[slot]) || {};

    return {
        rotation: readVec3(
            raw.rotation, [0, 0, 0], 'display.rotation'
        ),
        translation: readVec3(
            raw.translation, [0, 0, 0], 'display.translation'
        ),
        scale: readVec3(
            raw.scale, [1, 1, 1], 'display.scale'
        )
    };
}

/* ============================================================
 * 5. 采样与光栅化
 * ==========================================================*/

/** UV 直角坐标 → 像素（uv 与 uv_size 都可以是负数/小数）。 */
function sampleTexture(tex, u, v) {
    const px = Math.floor(u * tex.w);
    const py = Math.floor(v * tex.h);

    if (px < 0 || py < 0 || px >= tex.w || py >= tex.h) {
        return null;
    }

    const o = (py * tex.w + px) * 4;

    return [
        tex.data[o],
        tex.data[o + 1],
        tex.data[o + 2],
        tex.data[o + 3]
    ];
}

function shade(color, factor) {
    return [
        Math.min(255, Math.round(color[0] * factor)),
        Math.min(255, Math.round(color[1] * factor)),
        Math.min(255, Math.round(color[2] * factor))
    ];
}

/**
 * 简易方向光照：面越朝上/朝观察者越亮。
 * 参照体是纯色块，没有光照就分不出面，整张图会像一块灰板。
 * 基准值要足够高，否则整张图会发黑看不清。
 */
function faceLight(normal, yaw, pitch) {
    const cy = Math.cos(cube.rad(yaw));
    const sy = Math.sin(cube.rad(yaw));
    const cp = Math.cos(cube.rad(pitch));
    const sp = Math.sin(cube.rad(pitch));

    const nx = normal[0] * cy + normal[2] * sy;
    const nz = -normal[0] * sy + normal[2] * cy;
    const ny = normal[1];

    const vz = ny * sp + nz * cp;

    const lit = 0.80 + 0.26 * Math.max(0, ny) + 0.10 * Math.max(0, -vz);

    return Math.min(1.2, lit);
}

/**
 * 一个视角的渲染：正交投影 + 共享 Z-buffer + cutout 贴图。
 *
 * 深度约定：投影返回的是相机空间 z，**越小越近**。
 * target 可以是"多联图"的整块缓冲：用 offset(像素) 与 rowStride(字节)
 * 把这个视角写进对应的一列。
 *
 * depthBias：按来源（sword / body / ground）整体前移或后移。
 * 这里只用来让剑不被手臂完全挡住——**画得出来 ≠ 位置上就在前面**，
 * 真实前后关系看控制台输出的视空间深度数值。
 */
function renderView(target, view) {
    const {
        width, height, yaw, pitch, centerY, scale, faces, tex,
        backCull
    } = view;

    const depthBias = view.depthBias || {};
    const offset = view.offset || 0;
    const rowStride = view.rowStride || width * 4;

    const depthBuffer = new Float64Array(width * height);
    depthBuffer.fill(Infinity);

    const cy = Math.cos(cube.rad(yaw));
    const sy = Math.sin(cube.rad(yaw));
    const cp = Math.cos(cube.rad(pitch));
    const sp = Math.sin(cube.rad(pitch));

    const projected = faces.map(face => {
        const bias = depthBias[face.source] || 0;

        const P = face.points.map(p => {
            const x = p[0] * cy + p[2] * sy;
            const z = -p[0] * sy + p[2] * cy;
            const y = p[1];

            const y2 = y * cp - z * sp;
            const z2 = y * sp + z * cp;

            return [
                width / 2 + x * scale,
                height / 2 + (centerY - y2) * scale,
                z2 + bias
            ];
        });

        return { face, P };
    });

    /* cutout 预览：只做遮挡，不做半透明混合。 */
    projected.sort((a, b) => {
        const da = (a.P[0][2] + a.P[1][2] + a.P[2][2]) / 3;
        const db = (b.P[0][2] + b.P[1][2] + b.P[2][2]) / 3;
        return da - db;
    });

    for (const item of projected) {
        const face = item.face;
        const [A, B, C, D] = item.P;

        const area = (B[1] - C[1]) * (A[0] - C[0]) +
            (C[0] - B[0]) * (A[1] - C[1]);

        if (Math.abs(area) < 1e-9) continue;

        if (backCull) {
            /* 背面剔除用**视空间**法线（用 geo 空间的 normal[2] 是错的：
               侧面与上下面在那个测试里恒为 0，会被整片剔掉）。
               投影后三角形的有向面积恰好等于 视空间法线z × scale²，
               所以两者同号即朝前。

               默认关闭剔除：剑的 display 含一次镜像（右手 scale(-1,1,1)），
               镜像会把绕序整体反过来，而法线表没有跟着翻，剔除就不可靠。
               共享 Z-buffer 已经能给正确的遮挡关系，剔除只是省时间。 */
            const world = face.normal;

            const nx = world[0] * cy + world[2] * sy;
            const nz = -world[0] * sy + world[2] * cy;
            const ny = world[1];

            const viewZ = ny * sp + nz * cp;

            if (area * viewZ <= 0) continue;
        }

        /* 剑面才有 uv（已经是四角直角坐标）；参照体/地面只有纯色 */
        const uvs = face.uv || null;

        const light = face.solid
            ? faceLight(face.normal, yaw, pitch)
            : 1;

        const triangles = [[0, 1, 2], [0, 2, 3]];

        for (const [i0, i1, i2] of triangles) {
            const p0 = [A, B, C, D][i0];
            const p1 = [A, B, C, D][i1];
            const p2 = [A, B, C, D][i2];

            const minX = Math.max(
                0, Math.floor(Math.min(p0[0], p1[0], p2[0]))
            );
            const maxX = Math.min(
                width - 1, Math.ceil(Math.max(p0[0], p1[0], p2[0]))
            );
            const minY = Math.max(
                0, Math.floor(Math.min(p0[1], p1[1], p2[1]))
            );
            const maxY = Math.min(
                height - 1, Math.ceil(Math.max(p0[1], p1[1], p2[1]))
            );

            const d = (p1[1] - p2[1]) * (p0[0] - p2[0]) +
                (p2[0] - p1[0]) * (p0[1] - p2[1]);

            if (Math.abs(d) < 1e-9) continue;

            for (let y = minY; y <= maxY; y++) {
                for (let x = minX; x <= maxX; x++) {
                    /* 像素中心采样，避免半像素偏移造成的接缝 */
                    const sampleX = x + 0.5;
                    const sampleY = y + 0.5;

                    const w0 = (
                        (p1[1] - p2[1]) * (sampleX - p2[0]) +
                        (p2[0] - p1[0]) * (sampleY - p2[1])
                    ) / d;

                    const w1 = (
                        (p2[1] - p0[1]) * (sampleX - p2[0]) +
                        (p0[0] - p2[0]) * (sampleY - p2[1])
                    ) / d;

                    const w2 = 1 - w0 - w1;

                    if (w0 < -0.002 || w1 < -0.002 || w2 < -0.002) {
                        continue;
                    }

                    const depth =
                        w0 * p0[2] + w1 * p1[2] + w2 * p2[2];

                    const pixel = y * width + x;

                    if (depth >= depthBuffer[pixel]) continue;

                    let color = face.color;

                    if (uvs) {
                        /* 直接用已定义的 uv 四角插值，不再另建一套 S/T 索引 */
                        const su =
                            w0 * uvs[i0][0] +
                            w1 * uvs[i1][0] +
                            w2 * uvs[i2][0];

                        const sv =
                            w0 * uvs[i0][1] +
                            w1 * uvs[i1][1] +
                            w2 * uvs[i2][1];

                        const texel = sampleTexture(tex, su, sv);

                        if (!texel) continue;

                        if (texel[3] < 8) continue;   // 完全透明不写深度

                        color = [texel[0], texel[1], texel[2]];
                    } else if (light !== 1) {
                        color = shade(color, light);
                    }

                    depthBuffer[pixel] = depth;

                    const o = offset * 4 + y * rowStride + x * 4;
                    target[o] = color[0];
                    target[o + 1] = color[1];
                    target[o + 2] = color[2];
                    target[o + 3] = 255;
                }
            }
        }
    }
}

/* ============================================================
 * 6. 组装场景
 * ==========================================================*/

/**
 * 组装一把剑的所有面。
 *
 * 变换链（顺序不能改，代码里也是这个顺序）：
 *   cube 原始角点
 *   → cube 自身旋转 + 完整骨骼链（cube_space.cubeCorners，子→父）
 *   → geo 单位 → 方块单位
 *   → 手部局部空间镜像（右手）
 *   → 物品 display（缩放 → 旋转 → 平移）
 *   → 平移到手部参考点（attachToHand 时；见函数上方说明）
 */
function buildSwordFaces(model, display, tex, slot, hidden, options = {}) {
    const mirrorX = slot === 'thirdperson_lefthand'
        ? !MIRROR_RIGHT_HAND
        : MIRROR_RIGHT_HAND;

    const attach = options.attachToHand !== false;
    const hand = options.hand || handReference(slot);

    /* 剑的原点就是握柄基准；把它平移到手心，即"握住"。
       注意：这是**显式平移**，不是复现游戏的手部挂接矩阵。
       平移到手心后，红点必然重合，所以这里不能作为"握持正确"的证据；
       真正的判据是下面打印的"未平移前的偏差"。 */
    const attachOffset = attach
        ? cube.sub(hand, [0, 0, 0])
        : [0, 0, 0];

    const faces = [];

    for (const info of cube.listCubes(model)) {
        if (hidden.has(info.bone)) continue;

        const corners = cube.cubeCorners(model, info, {});

        const mapped = corners.map(p => cube.add(
            cube.applyDisplayTransform(
                cube.itemBlocksToView(
                    cube.geoToItemBlocks(p),
                    mirrorX
                ),
                display
            ),
            attachOffset
        ));

        for (const [name, uv] of Object.entries(info.cube.uv || {})) {
            const index = FACE_INDEX[name];

            if (!index) cube.fail(`未知面：${name}`);

            const texWidth = model.textureWidth;
            const texHeight = model.textureHeight;

            const [u0, v0] = uv.uv;
            const [uw, vh] = uv.uv_size;

            /* 贴图四角，直接来自 uv / uv_size（可以是负尺寸，方向由此保留） */
            const quad = [
                [u0 / texWidth, v0 / texHeight],                 // 左上
                [(u0 + uw) / texWidth, v0 / texHeight],          // 右上
                [(u0 + uw) / texWidth, (v0 + vh) / texHeight],   // 右下
                [u0 / texWidth, (v0 + vh) / texHeight]           // 左下
            ];

            const uvTable = buildUvTable(name, quad);

            const faceUv = index.map(i => {
                cube.assert(
                    uvTable[i],
                    `面 ${name} 的角点索引 ${i} 取不到贴图坐标`
                );

                return uvTable[i];
            });

            faces.push({
                name: `${info.name}.${name}`,
                points: index.map(i => mapped[i]),
                uv: faceUv,
                uvTable,
                normal: FACE_NORMAL[name],
                color: null,
                source: 'sword'
            });
        }
    }

    return { faces, mirrorX, hidden, attachOffset, attached: attach };
}

/** 地平面网格：0.5 格一条线，用很薄的方块画（放在 y=-0.02，避免遮住贴地的剑）。 */
function buildGroundFaces() {
    const faces = [];
    const half = 2.0;
    const step = 0.5;
    const t = 0.004;
    const y = -0.02;

    const pushBox = (from, to) => {
        const points = [];

        for (const x of [from[0], to[0]]) {
            for (const yy of [from[1], to[1]]) {
                for (const z of [from[2], to[2]]) {
                    points.push([x, yy, z]);
                }
            }
        }

        for (const [name, index] of Object.entries(FACE_INDEX)) {
            faces.push({
                name: `ground.${name}`,
                points: index.map(i => points[i]),
                uv: null,
                normal: FACE_NORMAL[name],
                color: GROUND,
                solid: true,
                source: 'ground'
            });
        }
    };

    const count = Math.round(half / step);

    for (let i = -count; i <= count; i++) {
        const c = i * step;

        pushBox([c - t, y, -half], [c + t, y, half]);
        pushBox([-half, y, c - t], [half, y, c + t]);
    }

    return faces;
}

function buildBodyFaces(hand, slot) {
    const faces = [];

    const pushBox = (from, to, color, source) => {
        const points = [];

        for (const x of [from[0], to[0]]) {
            for (const y of [from[1], to[1]]) {
                for (const z of [from[2], to[2]]) {
                    points.push([x, y, z]);
                }
            }
        }

        for (const [name, index] of Object.entries(FACE_INDEX)) {
            faces.push({
                name: `${source}.${name}`,
                points: index.map(i => points[i]),
                uv: null,
                normal: FACE_NORMAL[name],
                color,
                solid: true,
                source
            });
        }
    };

    for (const box of player) {
        pushBox(box.from, box.to, box.c, box.n);
    }

    /* 掌心标记 + 一小段"手心朝向"提示（画在最前面，见 depthBias） */
    const r = 0.032;

    pushBox(
        [hand[0] - r, hand[1] - r, hand[2] - r],
        [hand[0] + r, hand[1] + r, hand[2] + r],
        MARKER,
        'hand'
    );

    pushBox(
        [hand[0] - r * 0.4, hand[1] - r * 0.4, hand[2] - r * 3.2],
        [hand[0] + r * 0.4, hand[1] + r * 0.4, hand[2] + r * 3.2],
        MARKER,
        'hand-front'
    );

    return faces;
}

/* ============================================================
 * 7. 自检
 * ==========================================================*/

function selfCheck(model, hidden) {
    /* 面绕序：已由 resolveFaceOrder() 在加载时保证朝外，这里做最终复核 */
    const flipped = Object.entries(FACE_UV_ORDER)
        .filter(([name, order]) => (
            order.join(',') !== FACE_INDEX[name].join(',')
        ))
        .map(([name]) => name);

    console.log('--- 自检 ---');
    console.log(
        '  面绕序：' +
        (flipped.length === 0
            ? '全部朝外（无需翻转）'
            : `已翻转 ${flipped.join('/')} 以朝外`)
    );

    /* 骨骼链顺序：子骨骼必须先于父骨骼作用 */
    const chain = cube.getBoneChain(model, 'charge_core');

    cube.assert(
        chain.map(b => b.name).join('>') ===
        'root>weapon>guard>eye_mount>eye>cast_socket>charge_orb>charge_core',
        'getBoneChain 顺序异常：' + chain.map(b => b.name).join('>')
    );

    const pivotProbe = cube.transformByBoneChain(
        model, 'charge_core', [0, 4.2, -2.4], {}
    );

    console.log(
        '  骨骼链：root→…→charge_core 共 ' + chain.length +
        ' 级，静止姿态下 pivot 不动 ' +
        `[${pivotProbe.map(v => cube.round(v)).join(', ')}]`
    );

    /* 隐藏骨骼必须连带后代 */
    cube.assert(
        hidden.has('charge_core'),
        'charge_core 未随 charge_orb 一起隐藏'
    );

    cube.assert(
        !hidden.has('blade'),
        'blade 被误判为隐藏'
    );

    console.log(
        `  隐藏骨骼 ${hidden.size} 个：` +
        [...hidden].join(', ')
    );
}

/* ============================================================
 * 8. main
 * ==========================================================*/

function main() {
    const options = parseArgs(process.argv.slice(2));

    const model = cube.loadGeo(options.geo);
    const item = JSON.parse(fs.readFileSync(options.item, 'utf8'));
    const tex = decodePNG(options.tex);

    const display = readDisplay(item, options.slot);

    /* display 可能继承自父模型：本工具只读本文件，缺字段不代表游戏里是默认值。 */
    const rawDisplay =
        (item.display && item.display[options.slot]) || {};

    const inheritedFields = ['rotation', 'translation', 'scale']
        .filter(key => rawDisplay[key] === undefined);

    const hand = handReference(options.slot);

    const hidden = cube.hiddenBoneSet(model, HIDDEN_ROOTS);

    selfCheck(model, hidden);

    const sword = buildSwordFaces(
        model, display, tex, options.slot, hidden, {
        attachToHand: !options.referenceOnly,
        hand
    });

    const body = buildBodyFaces(hand, options.slot);
    const ground = buildGroundFaces();

    const faces = [...ground, ...body, ...sword.faces];

    /* 视角：右前 45° / 右侧 / 正面 */
    const yaws = [40, 90, 0];
    const pitch = options.hand ? 4 : 8;

    const PW = 460;
    const PH = 470;
    const SS = 2;                       // 超采样倍率
    const VW = PW * SS;
    const VH = PH * SS;

    const frame = options.hand
        ? { centerY: 0.70, base: PW * 1.35 }
        : { centerY: 1.10, base: PW * 0.215 };

    /* --reference-only 时剑按 display 原位挂在很低的位置（常常在膝盖以下），
       原来的取景会把剑挤出画面，所以自动把取景中心压到场景包围盒中间。 */
    const ys = faces.flatMap(f => f.points.map(p => p[1]));
    const low = Math.min(...ys);
    const high = Math.max(...ys);

    const centerY = options.referenceOnly && !options.hand
        ? (low + high) / 2
        : frame.centerY;

    const spanY = options.referenceOnly && !options.hand
        ? Math.max(0.6, high - low + 0.3)
        : 0;

    const baseScale = options.referenceOnly && !options.hand
        ? Math.min(frame.base, PW * 0.9 / spanY)
        : frame.base;

    const scale = baseScale * options.zoomValue * SS;

    const fullWidth = VW * yaws.length;
    const canvas = Buffer.alloc(fullWidth * VH * 4);

    for (let i = 0; i < fullWidth * VH; i++) {
        canvas[i * 4] = 246;
        canvas[i * 4 + 1] = 246;
        canvas[i * 4 + 2] = 249;
        canvas[i * 4 + 3] = 255;
    }

    yaws.forEach((yaw, index) => {
        const view = {
            width: VW,
            height: VH,
            yaw,
            pitch,
            centerY,
            scale,
            faces,
            tex,
            backCull: false,
            intersectionMode: options.hand,
            /* 把剑整体前移：否则手掌/手臂会把剑完全挡住，
               比例参考图就没法看。这是**画法**上的处理，
               真实前后关系以控制台输出的深度数值为准。
               红点再往前一点，保证"手心在哪"永远可见。 */
            depthBias: {
                sword: options.swordBias,
                hand: options.swordBias - 1,
                'hand-front': options.swordBias - 1
            }
        };

        const offset = index * VW;

        renderView(canvas, {
            ...view,
            offset,
            rowStride: fullWidth * 4
        });
    });

    /* 超采样降采样 */
    const W = PW * yaws.length;
    const H = PH;
    const rgba = new Uint8Array(W * H * 4);

    for (let y = 0; y < H; y++) {
        for (let x = 0; x < W; x++) {
            let r = 0;
            let g = 0;
            let b = 0;

            for (let dy = 0; dy < SS; dy++) {
                for (let dx = 0; dx < SS; dx++) {
                    const o = (
                        (y * SS + dy) * fullWidth + x * SS + dx
                    ) * 4;
                    r += canvas[o];
                    g += canvas[o + 1];
                    b += canvas[o + 2];
                }
            }

            const n = SS * SS;
            const o = (y * W + x) * 4;

            rgba[o] = Math.round(r / n);
            rgba[o + 1] = Math.round(g / n);
            rgba[o + 2] = Math.round(b / n);
            rgba[o + 3] = 255;
        }
    }

    fs.mkdirSync(path.dirname(options.out), { recursive: true });
    fs.writeFileSync(options.out, encodePNG(W, H, rgba));

    /* ---------- 数值输出 ---------- */
    /* 这里算的是"未平移前"的剑位置，也就是当前 display 自己把剑放到了哪里。
       图上画的是平移到手心之后的结果，所以图看不出这个偏差，必须打印。 */
    const toView = local => cube.applyDisplayTransform(
        cube.itemBlocksToView(
            [local[0] / 16, local[1] / 16, local[2] / 16],
            sword.mirrorX
        ),
        display
    );

    const origin = toView([0, 0, 0]);
    const gripBottom = toView([0, -3.5, 0]);     // pommel 骨骼 pivot
    const guard = toView([0, 4.1, 0]);           // guard 骨骼 pivot

    const delta = cube.sub(hand, origin);        // 需要补的位移（参照空间）

    const distance = Math.hypot(
        origin[0] - hand[0],
        origin[1] - hand[1],
        origin[2] - hand[2]
    );

    /* 手心到"握柄轴线"的垂距：只看横向偏差，不受剑长影响 */
    const axis = cube.sub(guard, gripBottom);
    const axisLength = Math.hypot(axis[0], axis[1], axis[2]);

    const relative = cube.sub(hand, gripBottom);
    const along = (
        relative[0] * axis[0] +
        relative[1] * axis[1] +
        relative[2] * axis[2]
    ) / (axisLength * axisLength);

    const nearest = cube.add(
        gripBottom,
        cube.scaleVec(axis, along)
    );

    const perpendicular = Math.hypot(
        hand[0] - nearest[0],
        hand[1] - nearest[1],
        hand[2] - nearest[2]
    );

    console.log('=== 第三人称比例参考图 ===');
    console.log(`槽位：${options.slot}   相机倍率：${options.zoomValue}`);
    console.log(
        `display[${options.slot}] ` +
        `rot ${JSON.stringify(display.rotation)} ` +
        `trans ${JSON.stringify(display.translation)} ` +
        `scale ${JSON.stringify(display.scale)}`
    );

    if (inheritedFields.length > 0) {
        console.log(
            `  注意：本文件未写 ${inheritedFields.join('/')}，` +
            '这里按默认值处理；若 display 继承自父模型，游戏里可能不同。'
        );
    }

    console.log(
        `隐藏骨骼根：${[...HIDDEN_ROOTS].join(', ')}（含全部后代）`
    );

    console.log('--- 参考坐标（方块单位，X 已按手部局部空间镜像）---');
    console.log(
        `  手部参考点 HAND   = [${hand.map(v => cube.round(v)).join(', ')}]`
    );
    console.log(
        `  display 后的剑原点 = [${origin.map(v => cube.round(v)).join(', ')}]`
    );
    console.log(
        `  握柄下端 pommel   = [${gripBottom.map(v => cube.round(v)).join(', ')}]`
    );
    console.log(
        `  护手 guard        = [${guard.map(v => cube.round(v)).join(', ')}]`
    );
    console.log(
        `  未平移前偏差      = [${delta.map(v => cube.round(v)).join(', ')}] 方块` +
        ` = [${delta.map(v => cube.round(v * 16)).join(', ')}] 单位`
    );
    console.log(
        `  剑原点到手心距离  = ${cube.round(distance)} 方块` +
        ` = ${cube.round(distance * 16)} 单位`
    );
    console.log(
        `  手心到握柄轴线垂距 = ${cube.round(perpendicular)} 方块` +
        ` = ${cube.round(perpendicular * 16)} 单位`
    );

    if (sword.attached) {
        console.log(
            '  图上已把剑平移到手心（所以红点必然重合）；' +
            '上面的偏差才是 display 的真实状态。'
        );
    } else {
        console.log(
            '  --reference-only：图上画的是 display 原位，未平移到手心。'
        );
    }

    console.log('--- 判读范围 ---');
    console.log('  本图为第三人称简化比例参考，红点为人工配置的手部参考点。');
    console.log('  尚未复现实际玩家手臂姿态、左右手运行时镜像、GeckoLib 完整渲染矩阵。');
    console.log('  剑通过显式 HAND 平移进入玩家参照空间；实际手部矩阵尚未校准。');
    console.log('  不可直接据此自动修改正式 display。');
    console.log(`输出：${path.relative(ROOT, options.out)}`);

    return 0;
}

try {
    if (require.main === module) {
        process.exitCode = main();
    }
} catch (error) {
    console.error('错误：' + error.message);
    console.error(error.stack);
    process.exitCode = 2;
}

/* 供其它脚本/自检引用（避免再复制一份变换函数）。 */
module.exports = {
    FACE_INDEX,
    FACE_NORMAL,
    FACE_UV_ORDER,
    HIDDEN_ROOTS,
    SUPPORTED_SLOTS,
    player,
    handReference,
    readDisplay,
    buildSwordFaces,
    buildBodyFaces,
    buildGroundFaces,
    renderView,
    selfCheck,
    resolveFaceOrder
};
