/* ============================================================================
 *  物品模型�?D 预览�?2026-09-12 v1, 专为排查"手持石像看起来像贴图展开�?)
 *
 *  既有 preview_item.cjs 只能�?*单面正视�?*, 看不出立体效�? 这个脚本做真正的
 *  正交 3D 投影: 逐面仿射贴图 + 画家算法(�?>�?, 于是能直接看出石像是不是一�? *  正常的人�? 还是"皮肤图摊在板子上"�? *
 *  用法: node preview_3d.cjs <model.json> <tex.png> <out.png> [px/单位] [yaw度] [pitch度]
 * ==========================================================================*/
const fs = require('fs');
const { Canvas, decodePNG } = require('./png.cjs');

const [modelPath, texPath, outPath, pxArg, yawArg, pitchArg] = process.argv.slice(2);
const S = parseFloat(pxArg || '14');
const YAW = (parseFloat(yawArg || '28')) * Math.PI / 180;
const PITCH = (parseFloat(pitchArg || '16')) * Math.PI / 180;

const model = JSON.parse(fs.readFileSync(modelPath, 'utf8'));
const tex = decodePNG(texPath);
const T = model.texture_size || [16, 16];
/* UV 缩放(诊断�?: 有的模型 UV 写在 0..16 空间�?texture_size �?64, �?FT_UV_SCALE=4 试试另一种解�?*/
const UVS = parseFloat(process.env.FT_UV_SCALE || '1');

/* 元素自身旋转(MC 方块模型 rotation: �?axis 轴、过 origin 点、逆时�?angle �? */
function applyElemRot(p, el) {
    const r = el.rotation;
    if (!r || !r.angle) return p;
    const a = (r.angle * Math.PI) / 180;
    const o = r.origin || [0, 0, 0];
    const dx = p[0] - o[0], dy = p[1] - o[1], dz = p[2] - o[2];
    const c = Math.cos(a), s = Math.sin(a);
    let nx = dx, ny = dy, nz = dz;
    if (r.axis === 'x') { ny = dy * c - dz * s; nz = dy * s + dz * c; }
    else if (r.axis === 'y') { nx = dx * c + dz * s; nz = -dx * s + dz * c; }
    else { nx = dx * c - dy * s; ny = dx * s + dy * c; }
    return [nx + o[0], ny + o[1], nz + o[2]];
}

/* MC �?display 变换: p' = 平移 + 旋转(ZYX 顺序) + 缩放 * p —�?�?FT_DISPLAY=thirdperson_righthand 打开 */
const DISPLAY = process.env.FT_DISPLAY || '';
function applyDisplay(p) {
    if (!DISPLAY) return p;
    const d = (model.display || {})[DISPLAY];
    if (!d) return p;
    const s = d.scale || [1, 1, 1];
    const t = d.translation || [0, 0, 0];
    const r = (d.rotation || [0, 0, 0]).map((v) => (v * Math.PI) / 180);
    /* ZYX: �?Z, �?Y, �?X */
    let [x, y, z] = [p[0] * s[0], p[1] * s[1], p[2] * s[2]];
    let c = Math.cos(r[2]), si = Math.sin(r[2]);
    [x, y] = [x * c - y * si, x * si + y * c];
    c = Math.cos(r[1]); si = Math.sin(r[1]);
    [x, z] = [x * c + z * si, -x * si + z * c];
    c = Math.cos(r[0]); si = Math.sin(r[0]);
    [y, z] = [y * c - z * si, y * si + z * c];
    return [x + t[0], y + t[1], z + t[2]];
}

/* ---- 正交投影: 先绕 Y �?yaw, 再绕 X �?pitch ---- */
function project(p) {
    const cy = Math.cos(YAW), sy = Math.sin(YAW);
    let x = p[0] * cy + p[2] * sy;
    let z = -p[0] * sy + p[2] * cy;
    let y = p[1];
    const cx = Math.cos(PITCH), sx = Math.sin(PITCH);
    const y2 = y * cx - z * sx;
    const z2 = y * sx + z * cx;
    return [x, y2, z2];
}

/* 模型包围�?-> 自动居中 */
let mnx = 1e9, mxx = -1e9, mny = 1e9, mxy = -1e9;
for (const el of (model.elements || [])) {
    mnx = Math.min(mnx, el.from[0]); mxx = Math.max(mxx, el.to[0]);
    mny = Math.min(mny, el.from[1]); mxy = Math.max(mxy, el.to[1]);
}
const cxModel = (mnx + mxx) / 2, cyModel = (mny + mxy) / 2;
const W = Math.ceil(Math.max(mxx - mnx, mxy - mny) * S * 1.6) + 40;
const cv = new Canvas(W, W);
cv.rect(0, 0, cv.w, cv.h, 24, 24, 32, 1);
const O = W / 2;

function screen(p) {
    const q = project([p[0] - cxModel, p[1] - cyModel, p[2]]);
    return [O + q[0] * S, O - q[1] * S, q[2]];
}
function sample(u, v) {
    const x = Math.max(0, Math.min(tex.w - 1, Math.floor(u * (tex.w / T[0]))));
    const y = Math.max(0, Math.min(tex.h - 1, Math.floor(v * (tex.h / T[1]))));
    const i = (y * tex.w + x) * 4;
    return [tex.data[i], tex.data[i + 1], tex.data[i + 2], tex.data[i + 3]];
}

/* 每个�? 原点�?+ u 方向向量 + v 方向向量(**带长�?*, 单位: 模型�? */
function faceQuad(el, face) {
    const [x0, y0, z0] = el.from, [x1, y1, z1] = el.to;
    const w = x1 - x0, h = y1 - y0, d = z1 - z0;
    switch (face) {
        case 'north': return { o: [x1, y1, z0], eu: [-w, 0, 0], ev: [0, -h, 0] };  // �?-Z �? u -> -X, v -> -Y
        case 'south': return { o: [x0, y1, z1], eu: [w, 0, 0], ev: [0, -h, 0] };   // �?+Z �? u -> +X, v -> -Y
        case 'west': return { o: [x0, y1, z1], eu: [0, 0, d], ev: [0, -h, 0] };    // �?-X �? u -> +Z
        case 'east': return { o: [x1, y1, z0], eu: [0, 0, -d], ev: [0, -h, 0] };   // �?+X �? u -> -Z
        case 'up': return { o: [x0, y1, z0], eu: [w, 0, 0], ev: [0, 0, d] };       // 从上往�? u -> +X, v -> +Z
        case 'down': return { o: [x0, y0, z1], eu: [w, 0, 0], ev: [0, 0, -d] };    // 从下往上看
        default: return null;
    }
}

const polys = [];
for (const el of (model.elements || [])) {
    for (const face of ['north', 'south', 'west', 'east', 'up', 'down']) {
        const f = el.faces && el.faces[face];
        if (!f || !f.uv) continue;
        const q = faceQuad(el, face);
        /* 先把面片 4 个角按元素自身旋转摆�? 再投�?*/
        const c0 = applyDisplay(applyElemRot(q.o, el));
        const c1 = applyDisplay(applyElemRot([q.o[0] + q.eu[0], q.o[1] + q.eu[1], q.o[2] + q.eu[2]], el));
        const c2 = applyDisplay(applyElemRot([q.o[0] + q.ev[0], q.o[1] + q.ev[1], q.o[2] + q.ev[2]], el));
        const p0 = screen(c0);
        const pu = screen(c1);
        const pv = screen(c2);
        const du = [pu[0] - p0[0], pu[1] - p0[1]];
        const dv = [pv[0] - p0[0], pv[1] - p0[1]];
        /* 不做背面剔除 —�?靠画家算�?�?>�?覆盖, 简单又不会剔错方向 */
        polys.push({ p0, du, dv, uv: f.uv.map((v) => v * UVS), z: (p0[2] + pu[2] + pv[2]) / 3 });
    }
}
polys.sort((a, b) => b.z - a.z);        // �?-> �?
for (const poly of polys) {
    const xs = [poly.p0[0], poly.p0[0] + poly.du[0], poly.p0[0] + poly.dv[0], poly.p0[0] + poly.du[0] + poly.dv[0]];
    const ys = [poly.p0[1], poly.p0[1] + poly.du[1], poly.p0[1] + poly.dv[1], poly.p0[1] + poly.du[1] + poly.dv[1]];
    const bx0 = Math.floor(Math.min(...xs)), bx1 = Math.ceil(Math.max(...xs));
    const by0 = Math.floor(Math.min(...ys)), by1 = Math.ceil(Math.max(...ys));
    const det = poly.du[0] * poly.dv[1] - poly.du[1] * poly.dv[0];
    if (Math.abs(det) < 1e-6) continue;
    const [u1, v1, u2, v2] = poly.uv;
    for (let py = by0; py <= by1; py++) {
        for (let px = bx0; px <= bx1; px++) {
            const rx = px + 0.5 - poly.p0[0], ry = py + 0.5 - poly.p0[1];
            const a = (rx * poly.dv[1] - ry * poly.dv[0]) / det;
            const b = (poly.du[0] * ry - poly.du[1] * rx) / det;
            if (a < 0 || a > 1 || b < 0 || b > 1) continue;
            const c = sample(u1 + (u2 - u1) * a, v1 + (v2 - v1) * b);
            if (c[3] < 16) continue;
            cv.set(px, py, c[0], c[1], c[2], c[3] / 255);
        }
    }
}
cv.save(outPath);
console.log(`已写�?${outPath} (${cv.w}x${cv.h}, 面数 ${polys.length}, yaw=${(YAW * 180 / Math.PI).toFixed(0)} pitch=${(PITCH * 180 / Math.PI).toFixed(0)})`);
