/* ============================================================================
 *  export_bbmodel.cjs —— 把 geo + animation + 贴图 打包成 Blockbench .bbmodel
 *  目的: 让用户/协作者一键导入查看与校准(FACE_CORNERS / UV 方向 / 动画姿态)。
 *  只读正式资源, 只写 art/ —— 不修改 geo / animation / png。
 *  格式: model_format = geckolib_model（Bedrock 几何: 坐标与 UV 均按 geo 原样, 不做 Java 换算）
 *  用法: node tools/model3d/export_bbmodel.cjs [输出路径]
 * ==========================================================================*/
'use strict';
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

const ROOT = path.resolve(__dirname, '..', '..');
const AS = path.join(ROOT, 'src', 'main', 'resources', 'assets', 'ftboss');
const GEO = path.join(AS, 'geo', 'shadow_sword.geo.json');
const ANIM = path.join(AS, 'animations', 'shadow_sword.animation.json');
const PNG = path.join(AS, 'textures', 'item', 'shadow_sword.png');
const OUT = process.argv[2] || path.join(ROOT, 'art', 'shadow_sword_v3.bbmodel');

const g = JSON.parse(fs.readFileSync(GEO, 'utf8'))['minecraft:geometry'][0];
const anims = JSON.parse(fs.readFileSync(ANIM, 'utf8')).animations;
const TEX_W = g.description.texture_width, TEX_H = g.description.texture_height;
const uuid = () => crypto.randomUUID();

/* ---------- 1) cube -> element（UV 用像素, 保留负宽/负高 = 镜像方向） ---------- */
const elements = [];
const cubesOf = {};          // boneName -> [elementUuid]
for (const b of g.bones) {
    cubesOf[b.name] = [];
    for (let i = 0; i < (b.cubes || []).length; i++) {
        const c = b.cubes[i];
        const id = uuid();
        const faces = {};
        for (const [f, u] of Object.entries(c.uv || {})) {
            const [uu, vv] = u.uv, [w, h] = u.uv_size;
            faces[f] = { uv: [uu, vv, uu + w, vv + h], texture: 0 };
        }
        const el = {
            name: b.name + '#' + i, uuid: id, type: 'cube',
            from: c.origin.slice(), to: c.origin.map((v, k) => +(v + c.size[k]).toFixed(4)),
            origin: (c.pivot || [c.origin[0] + c.size[0] / 2, c.origin[1] + c.size[1] / 2, c.origin[2] + c.size[2] / 2]).slice(),
            faces,
            box_uv: false,
        };
        if (c.rotation) el.rotation = c.rotation.slice();
        if (typeof c.inflate === 'number' && c.inflate !== 0) el.inflate = c.inflate;
        elements.push(el);
        cubesOf[b.name].push(id);
    }
}

/* ---------- 2) 骨骼树（按 parent 关系嵌套） ---------- */
const groupOf = {};
for (const b of g.bones) {
    groupOf[b.name] = {
        name: b.name, uuid: uuid(), isOpen: true,
        origin: (b.pivot || [0, 0, 0]).slice(),
        children: [],
    };
    if (b.rotation) groupOf[b.name].rotation = b.rotation.slice();
}
const roots = [];
for (const b of g.bones) {
    const node = groupOf[b.name];
    node.children.push(...cubesOf[b.name]);
    if (b.parent && groupOf[b.parent]) groupOf[b.parent].children.push(node);
    else roots.push(node);
}

/* ---------- 3) 动画（数值关键帧; 字符串时间键照原样搬） ---------- */
const animations = [];
for (const [full, a] of Object.entries(anims)) {
    const animators = {};
    for (const [bone, ch] of Object.entries(a.bones || {})) {
        const keyframes = [];
        const push = (channel, obj) => {
            if (obj === undefined) return;
            const entries = Array.isArray(obj) || typeof obj === 'number'
                ? [['0', obj]] : Object.entries(obj);
            for (const [stamp, value] of entries.sort((first, second) => Number(first[0]) - Number(second[0]))) {
                const vector = Array.isArray(value) ? value : [value, value, value];
                if (!Number.isFinite(Number(stamp)) || vector.length !== 3 || !vector.every(component => typeof component === 'number')) {
                    throw new Error(`Unsupported keyframe: ${full}/${bone}/${channel}@${stamp}`);
                }
                keyframes.push({
                    channel, time: Number(stamp), uuid: uuid(), interpolation: 'linear',
                    data_points: [{ x: String(vector[0]), y: String(vector[1]), z: String(vector[2]) }]
                });
            }
        };
        push('rotation', ch.rotation);
        push('position', ch.position);
        push('scale', ch.scale);
        if (!groupOf[bone]) throw new Error(`Unknown animation bone: ${bone}`);
        animators[groupOf[bone].uuid] = { name: bone, type: 'bone', keyframes };
    }
    animations.push({
        uuid: uuid(), name: full, loop: a.loop === true ? 'loop' : a.loop === 'hold_on_last_frame' ? 'hold' : 'once',
        length: a.animation_length, snapping: 24, animators,
    });
}

/* ---------- 4) 组装并写出 ---------- */
const bb = {
    meta: { format_version: '4.5', model_format: 'geckolib_model', box_uv: false },
    name: path.basename(OUT, '.bbmodel'),
    resolution: { width: TEX_W, height: TEX_H },
    elements,
    outliner: roots,
    textures: [{
        path: '', name: 'shadow_sword.png', folder: 'item', namespace: 'ftboss',
        id: '0', particle: false,
        source: 'data:image/png;base64,' + fs.readFileSync(PNG).toString('base64'),
    }],
    animations,
};

fs.mkdirSync(path.dirname(OUT), { recursive: true });
try { fs.chmodSync(OUT, 0o666); } catch (e) { }
fs.writeFileSync(OUT, JSON.stringify(bb, null, 1));

console.log('已导出 -> ' + path.relative(ROOT, OUT) + '  (' + (fs.statSync(OUT).size / 1024).toFixed(1) + ' KB)');
console.log('  骨骼(组) ' + Object.keys(groupOf).length + ' 个 / cube(element) ' + elements.length + ' 个 / 根组 ' + roots.length + ' 个');
console.log('  贴图内嵌（base64, ' + TEX_W + 'x' + TEX_H + '）; 动画 ' + animations.length + ' 段: ' + animations.map(a => a.name + '(' + a.length + 's,' + a.loop + ')').join(', '));
console.log('  ⚠ 只读导出, 未修改 geo / animation / png; 导入后若 Blockbench 提示格式差异, 也可直接导入 geo/shadow_sword.geo.json');
