/* ============================================================================
 *  物品模型「正面视图」快速预览器(2026-09-12 v14, 专为暗影剑 UV 取向排查新增)
 *
 *  与既有 render_model_tex.cjs 的区别: **遵守 texture_size**(那张工具按 16x16 采样,
 *  遇到 32x32 贴图会取错区域), 并严格按"贴图上边缘 -> 面上边缘"的规则采样,
 *  于是能直接看出"柄 / 护手 / 刃"有没有贴到对的位置。
 *  用法: node preview_item.cjs <model.json> <tex.png> <out.png> [每单位像素]
 * ==========================================================================*/
const fs = require('fs');
const { Canvas, decodePNG } = require('./png.cjs');

const [modelPath, texPath, outPath, pxArg] = process.argv.slice(2);
const S = parseFloat(pxArg || '18');
const model = JSON.parse(fs.readFileSync(modelPath, 'utf8'));
const tex = decodePNG(texPath);
const T = model.texture_size || [16, 16];

const VIEW = 18;                                    // 视野(模型单位)
const cv = new Canvas(Math.round(VIEW * S), Math.round(VIEW * S));
cv.rect(0, 0, cv.w, cv.h, 26, 26, 34, 1);

const CX = parseFloat(process.env.FT_CX || '8');    // 模型水平中心(标准方块物品是 8; 以原点为中心的模型传 0)
const sx = (x) => Math.round((x - CX + VIEW / 2) * S);
const sy = (y) => Math.round((17 - y) * S);         // 屏幕向下 = y 减小

function sample(u, v) {
    const x = Math.max(0, Math.min(tex.w - 1, Math.floor(u * (tex.w / T[0]))));
    const y = Math.max(0, Math.min(tex.h - 1, Math.floor(v * (tex.h / T[1]))));
    const i = (y * tex.w + x) * 4;
    return [tex.data[i], tex.data[i + 1], tex.data[i + 2], tex.data[i + 3]];
}

/* 只画正面看得见的那层: 默认取 z 最小的 north 面; 用环境变量 FT_FACE=face 可换面 */
const FACE = process.env.FT_FACE || 'north';
const elems = (model.elements || []).slice().sort((a, b) => b.from[2] - a.from[2]);
for (const el of elems) {
    const uv = el.faces[FACE] && el.faces[FACE].uv;
    if (!uv) continue;
    const [x0, y0] = el.from, [x1, y1] = el.to;
    const [u1, v1, u2, v2] = uv;
    const w = Math.max(1, Math.round((x1 - x0) * S));
    const h = Math.max(1, Math.round((y1 - y0) * S));
    const px0 = sx(x0), py0 = sy(y1);
    for (let j = 0; j < h; j++) {
        for (let i = 0; i < w; i++) {
            const u = u1 + (u2 - u1) * ((i + 0.5) / w);
            const v = v1 + (v2 - v1) * ((j + 0.5) / h);   // 图 v1(上) -> 面 y1(上)
            const c = sample(u, v);
            if (c[3] < 16) continue;
            cv.set(px0 + i, py0 + j, c[0], c[1], c[2], c[3] / 255);
        }
    }
}

/* 参考线: 每 4 单位一条淡线, y=0(握点) 用亮线 */
for (let y = 0; y <= 16; y += 4) {
    cv.hline(0, cv.w - 1, sy(y), 255, 90, 90, y === 0 ? 0.85 : 0.35);
}
/* 两侧竖线标出模型中心 x=8 */
cv.vline(sx(8), 0, cv.h - 1, 90, 160, 255, 0.35);
cv.save(outPath);
console.log('已写出 ' + outPath + '  (' + cv.w + 'x' + cv.h + ', 红线 y=0/4/8/12/16, 上=刃 下=柄, 蓝线=x8)');
