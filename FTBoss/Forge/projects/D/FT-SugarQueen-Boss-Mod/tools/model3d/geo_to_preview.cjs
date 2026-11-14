/* ============================================================================
 *  geo_to_preview.cjs —— 把 geo 反向转成临时 Java 物品模型, 只为了"能看见"
 *  背景: 物品模型切成 builtin/entity 后没有 elements, preview_spin.cjs 渲染不出来。
 *  做法: Bedrock cube(origin/size/uv/rotation/pivot) -> Java element(from/to/faces),
 *        坐标加回 OFF=[8,4.5,8], 逐面 UV 由"像素 uv + uv_size"换算回 0..16 单位(取 min/max)。
 *  ⚠ 仅供预览: Java 只支持单轴旋转, 多轴组合的 cube 会丢掉旋转; 北/南面的镜像方向按 min/max 近似。
 *  用法: node tools/model3d/geo_to_preview.cjs <geo.json> <out.json> [textureWidth]
 * ==========================================================================*/
const fs = require('fs');
const path = require('path');
const ROOT = path.resolve(__dirname, '..', '..');
const GEO = process.argv[2] || path.join(ROOT, 'src/main/resources/assets/ftboss/geo/shadow_sword.geo.json');
const OUT = process.argv[3] || path.join(ROOT, 'art', 'preview_model.json');
const TW = Number(process.argv[4] || 256), TH = TW;

const g = JSON.parse(fs.readFileSync(GEO, 'utf8'))['minecraft:geometry'][0];
const OFF = [8, 4.5, 8];
const k = 16 / TW;                       // 像素 -> Java UV 单位
const AXES = ['x', 'y', 'z'];
const elements = [];
let droppedRot = 0;

for (const bone of g.bones) {
    for (const c of (bone.cubes || [])) {
        const el = {
            from: [c.origin[0] + OFF[0], c.origin[1] + OFF[1], c.origin[2] + OFF[2]],
            to: [c.origin[0] + c.size[0] + OFF[0], c.origin[1] + c.size[1] + OFF[1], c.origin[2] + c.size[2] + OFF[2]],
            name: bone.name,
            faces: {},
        };
        if (c.rotation) {
            const nz = c.rotation.map((v, i) => [v, i]).filter(([v]) => Math.abs(v) > 0.001);
            if (nz.length === 1) {
                el.rotation = {
                    origin: [c.pivot[0] + OFF[0], c.pivot[1] + OFF[1], c.pivot[2] + OFF[2]],
                    axis: AXES[nz[0][1]],
                    angle: nz[0][0],
                    rescale: false,
                };
            } else { droppedRot++; }
        }
        for (const [face, f] of Object.entries(c.uv || {})) {
            const [u, v] = f.uv, [w, h] = f.uv_size;
            const u1 = Math.min(u, u + w) * k, u2 = Math.max(u, u + w) * k;
            const v1 = Math.min(v, v + h) * k, v2 = Math.max(v, v + h) * k;
            el.faces[face] = { uv: [+u1.toFixed(4), +v1.toFixed(4), +u2.toFixed(4), +v2.toFixed(4)], texture: '#0' };
        }
        elements.push(el);
    }
}
const model = {
    credit: 'PREVIEW ONLY (由 geo_to_preview.cjs 从 ' + path.basename(GEO) + ' 反向生成)',
    textures: { '0': 'ftboss:item/shadow_sword', particle: 'ftboss:item/shadow_sword' },
    elements,
};
fs.mkdirSync(path.dirname(OUT), { recursive: true });
try { fs.chmodSync(OUT, 0o666); } catch (e) { }
fs.writeFileSync(OUT, JSON.stringify(model, null, 2));
console.log('geo cube -> Java element: ' + elements.length + ' 个' + (droppedRot ? '（' + droppedRot + ' 个多轴旋转被丢弃）' : ''));
console.log('临时预览模型 -> ' + path.relative(ROOT, OUT));
