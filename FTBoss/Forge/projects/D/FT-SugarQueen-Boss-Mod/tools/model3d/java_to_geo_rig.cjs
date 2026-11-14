/* ============================================================================
 *  java_to_geo_rig.cjs —— 把工程的**静态 Java 物品模型**转成**带骨骼的 Bedrock geo** + 生成动画
 *
 *  为什么需要: Java 物品模型 JSON 没有骨骼, 做不了"手持摆动/挥砍"。要动就得换成 GeckoLib 的
 *  `GeoItem`(Bedrock geometry + animation), 也就是本工程 Boss 实体那套。
 *
 *  转换要点:
 *   · Java 元素 → Bedrock cube: `origin` = from, `size` = to-from, `inflate` 同名同单位, 旋转 {origin,axis,angle}
 *   · UV: Java 是 0..16 归一化 + 逐面四角 [u1,v1,u2,v2]; Bedrock 是**纹理像素**的 `uv`(左上角) + `uv_size`
 *     → uv = [u1,v1]×PPU, uv_size = [(u2-u1), (v2-v1)]×PPU; u1>u2(镜像面)时 uv_size 取负 = Bedrock 的镜像写法
 *   · 按元素名归到骨骼: 柄首/握柄/护手/钩爪/魔眼/剑身/剑尖/晶核 各一根, 全部挂到 root 下
 *   · pivot 取该部件的"根部"(手抓的地方), 这样旋转才像真在摆
 *
 *  用法: node tools/model3d/java_to_geo_rig.cjs <物品id> [geo名] [动画名]
 *  产物: assets/ftboss/geo/<geo名>.geo.json  +  assets/ftboss/animations/<动画名>.animation.json
 * ==========================================================================*/
const fs = require('fs');
const path = require('path');
const { writeSafe } = require('./lib_itemtex.cjs');

const ROOT = path.resolve(__dirname, '..', '..');
const RES = path.join(ROOT, 'src', 'main', 'resources', 'assets', 'ftboss');
const id = process.argv[2] || 'shadow_sword';
const geoName = process.argv[3] || id;
const animName = process.argv[4] || id;
// 输入: 默认读物品模型; 若物品模型已改成 builtin/entity(没有 elements), 用第 4 个参数指定静态备份
// 例: node java_to_geo_rig.cjs shadow_sword shadow_sword shadow_sword ref/shadow_sword_static_v18.json.bak
const MODEL = process.argv[5] ? process.argv[5] : path.join(RES, 'models', 'item', id + '.json');
const model = JSON.parse(fs.readFileSync(MODEL, 'utf8'));
const PPU = (model.texture_size ? model.texture_size[0] : 16) / 16;    // 0..16 -> 像素

/* 骨骼表: 名字前缀 -> {bone, pivot, parent}  (pivot = 旋转轴心/根部) */
/* 骨骼表: 名字前缀 -> {bone, pivot}  (按教学给的骨骼树: root -> weapon -> 各部件, eye->pupil, pommel->crystal) */
const BONES = [
    ['pommel_crystal', 'crystal', [8, 0.5, 8]],      // 必须排在 pommel 之前
    ['crystal', 'crystal', [8, 0.5, 8]],
    ['pommel', 'pommel', [8, 1.0, 8]],
    ['band_b', 'handle', [8, 2.0, 8]],
    ['handle', 'handle', [8, 2.0, 8]],
    ['wrap', 'handle', [8, 2.0, 8]],
    ['band_a', 'handle', [8, 2.0, 8]],
    ['guard', 'guard', [8, 8.6, 8]],
    ['claw_l', 'claw_left', [6.0, 8.6, 8]],
    ['claw_r', 'claw_right', [10.0, 8.6, 8]],
    ['eye_iris', 'eye', [8, 8.7, 8]],
    ['eye_pupil', 'pupil', [8, 8.7, 8]],
    ['barb', 'blade', [8, 9.6, 8]],      // V3 倒钩必须归 blade, 否则落到默认骨骼(handle)上
    ['blade', 'blade', [8, 9.6, 8]],
    ['tip_', 'blade', [8, 20.6, 8]],
    ['cham_', 'blade', [8, 9.6, 8]],
];
const boneOf = name => { for (const [pre, bone] of BONES) if (name.startsWith(pre)) return bone; return 'handle'; };
const PIVOT = Object.fromEntries(BONES.map(([, b, p]) => [b, p]));
Object.assign(PIVOT, {
    root: [8, 4.5, 8], grip_anchor: [8, 4.5, 8], weapon: [8, 4.5, 8],
    pommel: [8, 1.0, 8], crystal_link: [8, 1.0, 8], crystal: [8, 0.5, 8],
    handle: [8, 2.0, 8], guard: [8, 8.6, 8],
    claw_left: [6.0, 8.6, 8], claw_left_tip: [5.2, 8.9, 8],
    claw_right: [10.0, 8.6, 8], claw_right_tip: [10.8, 8.9, 8],
    eye_mount: [8, 8.7, 8], eye: [8, 8.7, 8], pupil: [8, 8.7, 8], blade: [8, 9.6, 8],
});
const PARENT = {
    root: null, grip_anchor: 'root', weapon: 'grip_anchor',
    handle: 'weapon', blade: 'weapon', guard: 'weapon', pommel: 'weapon',
    claw_left: 'guard', claw_left_tip: 'claw_left', claw_right: 'guard', claw_right_tip: 'claw_right',
    eye_mount: 'guard', eye: 'eye_mount', pupil: 'eye',
    crystal_link: 'pommel', crystal: 'crystal_link',
};

/* ★★ 原点用**握点**，不是方块中心 —— GeckoLib 武器的通行约定: 手抓的地方就是 (0,0,0)。
   2026-09-15 实测: 用方块中心(y-8)时, 握柄落在 geo 的 y −6.15..−0.15(握点=原点下方 3.1),
   原版显示变换假设"手抓在原点" → 第三人称整把剑抬高 3.1 格、看着不在手里(用户截图点名)。
   柄芯 Java y 1.85..7.85、握点在 y≈4.5 → 取 yOff = 4.5 让握点正好落在原点。x/z 仍减 8(居中对齐)。 */
const OFF = [8, 4.5, 8];
const fix = p => [ +(p[0] - OFF[0]).toFixed(4), +(p[1] - OFF[1]).toFixed(4), +(p[2] - OFF[2]).toFixed(4) ];

/* 1) 元素 -> 骨骼分组 */
const groups = {};
for (const e of model.elements) {
    const b = boneOf(e.name || '');
    (groups[b] = groups[b] || []).push(e);
}
/* 2) 元素 -> Bedrock cube */
const cubeOf = e => {
    const size = [0, 1, 2].map(i => +(e.to[i] - e.from[i]).toFixed(4));
    const uv = {};
    for (const [face, f] of Object.entries(e.faces)) {
        if (!f.uv) continue;
        const [u1, v1, u2, v2] = f.uv;
        const flip = u1 > u2;                                  // 镜像(Java 里 south 是 u2,u1 的顺序)
        uv[face] = {
            uv: [+(Math.min(u1, u2) * PPU).toFixed(2), +(v1 * PPU).toFixed(2)],
            uv_size: [+((u2 - u1) * PPU).toFixed(2), +((v2 - v1) * PPU).toFixed(2)],
        };
        if (flip) { uv[face].uv = [+(u1 * PPU).toFixed(2), +(v1 * PPU).toFixed(2)]; uv[face].uv_size[0] = +((u2 - u1) * PPU).toFixed(2); }
    }
    const cube = { origin: fix(e.from), size, uv };
    if (e.inflate) cube.inflate = e.inflate;
    if (e.rotation) {
        /* ★★ Bedrock 的 cube 旋转是**数组 [x,y,z]**(单位度), 不是 Java 的 {origin,axis,angle} 对象 ——
           抄错这一处 GeckoLib 会直接抛 GeckoLibException: Error loading model file,
           表现为"资源重载阶段卡死"(2026-09-15 踩过)。Java 只允许单轴, 所以把角度放到对应分量上。 */
        const ax = e.rotation.axis, ang = e.rotation.angle;
        cube.rotation = [ax === 'x' ? ang : 0, ax === 'y' ? ang : 0, ax === 'z' ? ang : 0];
        cube.pivot = fix(e.rotation.origin);
    }
    cube.pivot = fix(PIVOT[boneOf(e.name || '')] || [8, 8, 8]);
    return cube;
};
/* 3) 组装 geometry */
const bones = Object.entries(groups).map(([name, els]) => {
    const b = { name, pivot: fix(PIVOT[name] || [8, 8, 8]) };
    if (PARENT[name]) b.parent = PARENT[name];
    b.cubes = els.map(cubeOf);
    return b;
});
bones.unshift({ name: 'weapon', pivot: fix(PIVOT.weapon), parent: 'root', cubes: [] });
bones.unshift({ name: 'root', pivot: fix(PIVOT.root), cubes: [] });
/* ===== 关节改造(2026-09-15) ===== */
{
    const byName = Object.fromEntries(bones.map(b => [b.name, b]));
    /* 爪: 原来一整块(带自身旋转) -> 拆"根 + 尖", 静态倾角交给骨骼 */
    for (const side of ['left', 'right']) {
        const bn = 'claw_' + side, tipN = bn + '_tip';
        const b = byName[bn];
        if (b && b.cubes.length === 1) {
            const c = b.cubes[0];
            const [x0, y0, z0] = c.origin, [w, h, d] = c.size;
            const inward = side === 'left' ? 1 : -1;                 // 靠护手的一端
            const rootW = +(w * 0.55).toFixed(3), tipW = +(w - rootW + 0.1).toFixed(3);
            const rootX = side === 'left' ? x0 : x0 + (w - rootW);
            const tipX = side === 'left' ? x0 + rootW - 0.1 : x0;
            b.cubes = [{ origin: [rootX, y0 - 0.05, z0], size: [rootW, h + 0.15, d], uv: c.uv }];   // V3: 爪根加厚
            b.rotation = [0, 0, side === 'left' ? -22.5 : 22.5];      // 静态倾角移到骨骼
            b.pivot = fix(PIVOT[bn]);
            bones.push({
                name: tipN, parent: bn, pivot: fix(PIVOT[tipN]), rotation: [0, 0, side === 'left' ? -4 : 4],
                cubes: [{ origin: [tipX, y0 + 0.15, z0 + 0.22], size: [tipW, h - 0.45, Math.max(0.2, d - 0.45)], uv: c.uv }],   // V3: 爪尖收细
            });
        }
    }
    /* 瞳: 一片贯穿 -> 前后两片, 各自贴在虹膜表面(消除悬空间隙) */
    const pu = byName['pupil'];
    if (pu && pu.cubes.length === 1) {
        const c = pu.cubes[0];
        const [x0, y0, z0] = c.origin, [w, h, d] = c.size;
        const th = 0.50;   // V3: 0.25 时虹膜放大 1.18 会让瞳孔脱离眼球表面(§10.3), 加厚到 0.5
        pu.cubes = [
            { origin: [x0, y0, z0], size: [w, h, th], uv: c.uv },
            { origin: [+(x0).toFixed(3), y0, +(z0 + d - th).toFixed(3)], size: [w, h, th], uv: c.uv },
        ];
    }
    /* 眼睛底座: 空骨骼(层级用) + 把 eye 挂到它下面 */
    if (!byName['eye_mount']) {
        /* 眼眶: 4 条边条包住虹膜(虹膜前后各凸 0.2); 挂在 eye_mount -> **不随虹膜缩放**(用户 §5 硬要求) */
        /* 眼眶用护手的 UV(近黑) —— 用虹膜的 UV 会让眼眶变成紫色方块(用户 2026-09-15 点名) */
        const eUv = Object.fromEntries([["north",{uv:[206,80],uv_size:[40,24]}],["south",{uv:[206,80],uv_size:[40,24]}],["east",{uv:[206,80],uv_size:[40,24]}],["west",{uv:[206,80],uv_size:[40,24]}],["up",{uv:[206,80],uv_size:[40,24]}],["down",{uv:[206,80],uv_size:[40,24]}]]);   // 独立岛: 眼眶四条边条(原来复用护手 UV, 无法单独画)
        const bar = (x0, x1, y0, y1) => ({ origin: [x0, y0, -1.375], size: [+(x1 - x0).toFixed(3), +(y1 - y0).toFixed(3), 2.75], uv: eUv });
        bones.push({
            name: 'eye_mount', parent: 'guard', pivot: fix(PIVOT.eye_mount),
            cubes: [bar(-1.75, -1.35, 3.30, 4.80), bar(1.35, 1.75, 3.30, 4.80), bar(-1.35, 1.35, 4.70, 4.95), bar(-1.35, 1.35, 3.05, 3.30)],
        });
    }
    /* 晶核: 加一根短轴做可见连接(不再凭空悬浮) + crystal_link 骨骼 */
    const pm = byName['pommel'], cr = byName['crystal'];
    if (pm && cr) {
        const topOfPommel = Math.max(...pm.cubes.map(c => c.origin[1] + c.size[1]));
        const botOfCrystal = Math.min(...cr.cubes.map(c => c.origin[1]));
        const uv = cr.cubes[0].uv;
        if (!byName['crystal_link']) bones.push({
            name: 'crystal_link', parent: 'pommel', pivot: fix(PIVOT.crystal_link),
            cubes: [{ origin: [-0.18, +(botOfCrystal - 0.35).toFixed(3), -0.18], size: [0.36, +((topOfPommel + 0.35) - (botOfCrystal - 0.35)).toFixed(3), 0.36], uv }],
        });
    }
    /* 新骨骼树: root → grip_anchor → weapon */
    if (!byName['grip_anchor']) bones.push({ name: 'grip_anchor', parent: 'root', pivot: fix(PIVOT.grip_anchor), cubes: [] });
}
const geo = {
    format_version: '1.12.0',
    'minecraft:geometry': [{
        description: { identifier: 'geometry.ftboss.' + geoName, texture_width: model.texture_size[0], texture_height: model.texture_size[1], visible_bounds_width: 2, visible_bounds_height: 3, visible_bounds_offset: [0, 1, 0] },
        bones,
    }],
};
const GEO = path.join(RES, 'geo', geoName + '.geo.json');
fs.mkdirSync(path.dirname(GEO), { recursive: true });
writeSafe(GEO, JSON.stringify(geo, null, 2) + '\n');

/* 4) 动画: idle(待机) / charge(蓄力) / release(释放) / recover(收束)
   ★ handle / blade / grip_anchor / weapon 在 idle 里**完全不动**(不飘、不脱手), 只有爪/瞳/虹膜/晶核在动 */
const A = (loop, len, bones) => ({ loop, animation_length: len, bones });
const anim = {
    format_version: '1.8.0',
    animations: {
        [`animation.ftboss.${animName}.idle`]: A(true, 4, {
            claw_left: { rotation: { 0: [0, 0, 0], 1: [0, 0, -2.5], 2: [0, 0, -4], 3: [0, 0, -1.5], 4: [0, 0, 0] } },
            claw_left_tip: { rotation: { 0: [0, 0, 0], 1.4: [0, 0, -6], 2.6: [0, 0, 3], 4: [0, 0, 0] } },
            claw_right: { rotation: { 0: [0, 0, 0], 1: [0, 0, 2.5], 2: [0, 0, 4], 3: [0, 0, 1.5], 4: [0, 0, 0] } },
            claw_right_tip: { rotation: { 0: [0, 0, 0], 1.4: [0, 0, 6], 2.6: [0, 0, -3], 4: [0, 0, 0] } },
            eye: { scale: { 0: [1, 1, 1], 2: [1.03, 1.05, 1.02], 4: [1, 1, 1] } },
            pupil: { position: { 0: [0, 0, 0], 1: [-0.22, 0.04, 0], 2: [0, 0, 0], 3: [0.22, -0.04, 0], 4: [0, 0, 0] } },
            crystal: {
                position: { 0: [0, 0, 0], 2: [0, -0.12, 0], 4: [0, 0, 0] },
                rotation: { 0: [0, 0, 0], 1: [0, 90, 0], 2: [0, 180, 0], 3: [0, 270, 0], 4: [0, 360, 0] },
            },
        }),
        [`animation.ftboss.${animName}.charge`]: A(false, 0.8, {
            claw_left: { rotation: { 0: [0, 0, 0], 0.5: [0, 0, -16], 0.8: [0, 0, -20] } },
            claw_right: { rotation: { 0: [0, 0, 0], 0.5: [0, 0, 16], 0.8: [0, 0, 20] } },
            claw_left_tip: { rotation: { 0: [0, 0, 0], 0.8: [0, 0, -10] } },
            claw_right_tip: { rotation: { 0: [0, 0, 0], 0.8: [0, 0, 10] } },
            eye: { scale: { 0: [1, 1, 1], 0.8: [1.10, 1.14, 1.04] } },
            pupil: { position: { 0: [0, 0, 0], 0.8: [0, -0.10, 0] } },
            crystal: { rotation: { 0: [0, 0, 0], 0.8: [0, 540, 0] }, position: { 0: [0, 0, 0], 0.8: [0, -0.22, 0] } },
        }),
        [`animation.ftboss.${animName}.release`]: A(false, 0.5, {
            claw_left: { rotation: { 0: [0, 0, -20], 0.08: [0, 0, -30], 0.22: [0, 0, -6], 0.5: [0, 0, 0] } },
            claw_right: { rotation: { 0: [0, 0, 20], 0.08: [0, 0, 30], 0.22: [0, 0, 6], 0.5: [0, 0, 0] } },
            claw_left_tip: { rotation: { 0: [0, 0, -10], 0.12: [0, 0, -18], 0.5: [0, 0, 0] } },
            claw_right_tip: { rotation: { 0: [0, 0, 10], 0.12: [0, 0, 18], 0.5: [0, 0, 0] } },
            eye: { scale: { 0: [1.10, 1.14, 1.04], 0.1: [1.16, 1.20, 1.08], 0.5: [1, 1, 1] } },
            pupil: { position: { 0: [0, -0.10, 0], 0.5: [0, 0, 0] } },
            crystal: { rotation: { 0: [0, 540, 0], 0.5: [0, 720, 0] }, position: { 0: [0, -0.22, 0], 0.2: [0, 0.16, 0], 0.5: [0, 0, 0] } },
        }),
        [`animation.ftboss.${animName}.recover`]: A(false, 0.6, {
            claw_left: { rotation: { 0: [0, 0, -6], 0.3: [0, 0, 2], 0.6: [0, 0, 0] } },
            claw_right: { rotation: { 0: [0, 0, 6], 0.3: [0, 0, -2], 0.6: [0, 0, 0] } },
            eye: { scale: { 0: [1.16, 1.20, 1.08], 0.3: [1.02, 1.02, 1.01], 0.6: [1, 1, 1] } },
            pupil: { position: { 0: [0, 0, 0], 0.3: [0, 0.05, 0], 0.6: [0, 0, 0] } },
            crystal: { rotation: { 0: [0, 720, 0], 0.6: [0, 720, 0] }, position: { 0: [0, 0, 0], 0.3: [0, -0.08, 0], 0.6: [0, 0, 0] } },
        }),
    },
};
const ANIM = path.join(RES, 'animations', animName + '.animation.json');
fs.mkdirSync(path.dirname(ANIM), { recursive: true });
writeSafe(ANIM, JSON.stringify(anim, null, 2) + '\n');

console.log('骨骼 ' + bones.length + ' 根: ' + bones.map(b => b.name + '(' + b.cubes.length + ' cube' + (b.parent ? ' ↑' + b.parent : '') + ')').join(', '));
console.log('geo   -> ' + path.relative(ROOT, GEO) + '   ' + (fs.statSync(GEO).size / 1024).toFixed(1) + ' KB  (' + model.elements.length + ' cube)');
console.log('动画  -> ' + path.relative(ROOT, ANIM) + '   2 段: idle(4s loop, 爪/瞳/晶核) / awaken(0.7s)');
