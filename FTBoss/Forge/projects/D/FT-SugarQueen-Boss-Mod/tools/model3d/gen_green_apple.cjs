/* ============================================================================
 *  青苹果召唤物(2026-09-12 按用户要求): **专属模型**, 不再复用答辩小怪
 *  一只会蹦的小青苹果: 圆果身(带棱) + 果梗 + 叶片 + 凶眼 + 嘴 + 两只小根脚
 *  骨骼 9 个 / 方块 12 个 / 64x64 图集 / 一条 hop 弹跳循环动画(0.8s)
 *  用法: node gen_green_apple.cjs <outDir>
 * ==========================================================================*/
const fs = require('fs');
const path = require('path');
const { Canvas } = require('./png.cjs');
const OUT = process.argv[2] || 'out_green_apple';
const W = 64, H = 64;

const M = {
    g: [150, 202, 84], gL: [196, 232, 132], gD: [92, 142, 54],
    leaf: [96, 168, 74], leafD: [64, 122, 52],
    stem: [104, 66, 38], bark: [118, 82, 50],
    white: [246, 244, 236], pupil: [34, 30, 28], mouth: [92, 32, 36],
};
const cv = new Canvas(W, H);
let penX = 1, penY = 1, rowH = 0;
const C = [];
const nz = (x, y, s) => { const n = Math.sin(x * 12.9898 + y * 78.233 + s * 3.7) * 43758.5453; return n - Math.floor(n); };

function cube(bone, o, s, mat, rot) {
    const w = s[0], h = s[1], d = s[2];
    const fw = Math.ceil(2 * (w + d)), fh = Math.ceil(d + h);
    if (penX + fw > W) { penX = 1; penY += rowH + 2; rowH = 0; }
    if (penY + fh > H) throw new Error('图集放不下: ' + fw + 'x' + fh);
    const u = penX, v = penY;
    penX += fw + 2; rowH = Math.max(rowH, fh);
    C.push({ bone, o, s, mat, rot });
    /* 整块 UV 区域填色(小模型没必要逐面精修): 果皮类加一点明暗噪点 */
    const base = M[mat];
    const noisy = mat === 'g' || mat === 'gD' || mat === 'gL' || mat === 'bark' || mat === 'leaf';
    for (let y = 0; y < fh; y++) {
        for (let x = 0; x < fw; x++) {
            const n = noisy ? Math.round(nz(x, y, 5) * 20 - 10) : 0;
            cv.set(u + x, v + y,
                Math.max(0, Math.min(255, base[0] + n)),
                Math.max(0, Math.min(255, base[1] + n)),
                Math.max(0, Math.min(255, base[2] + n)), 1);
        }
    }
}

/* ---------- 造型(y=0 为脚底) ---------- */
const B = 'body';
cube(B, [-4.5, 1.5, -4.5], [9, 8, 9], 'g');                       // 果身
for (const sx of [-1, 1]) for (const sz of [-1, 1]) {             // 四角棱(做出苹果圆润感)
    cube(B, [sx * 4.0 - (sx > 0 ? 0 : 2.0), 2.0, sz * 4.0 - (sz > 0 ? 0 : 2.0)], [2.0, 7.0, 2.0], 'gL');
}
cube(B, [-2.6, 9.3, -2.6], [5.2, 1.6, 5.2], 'gL');                // 顶部收口
cube(B, [-3.0, 1.0, -3.0], [6.0, 1.2, 6.0], 'gD');                // 底部
cube('stem', [-0.5, 10.6, -0.5], [1.0, 2.2, 1.0], 'stem');        // 果梗
cube('leaf', [0.6, 11.6, -1.4], [3.4, 0.7, 2.2], 'leaf');         // 叶片
cube('leaf', [1.4, 12.0, -0.6], [2.0, 0.5, 1.4], 'leafD');
/* 凶眼 + 嘴(正面 z=-4.5) */
cube('eye_l', [-3.1, 5.4, -4.9], [1.8, 1.8, 0.5], 'white');
cube('eye_r', [1.3, 5.4, -4.9], [1.8, 1.8, 0.5], 'white');
cube('eye_l', [-2.7, 5.8, -5.2], [1.0, 1.0, 0.4], 'pupil');
cube('eye_r', [1.7, 5.8, -5.2], [1.0, 1.0, 0.4], 'pupil');
cube('mouth', [-2.0, 3.0, -4.9], [4.0, 0.8, 0.5], 'mouth');
/* 小根脚 */
for (const sx of [-1, 1]) {
    cube(sx < 0 ? 'foot_l' : 'foot_r', [sx * 3.0 - (sx > 0 ? 0 : 2.0), 0.0, -1.4], [2.0, 1.6, 2.8], 'bark');
}

const bones = [
    { name: 'root', pivot: [0, 0, 0] },
    { name: 'body', parent: 'root', pivot: [0, 5.5, 0] },
    { name: 'stem', parent: 'body', pivot: [0, 10.6, 0] },
    { name: 'leaf', parent: 'stem', pivot: [0.6, 11.6, -0.4] },
    { name: 'eye_l', parent: 'body', pivot: [-2.2, 6.3, -4.6] },
    { name: 'eye_r', parent: 'body', pivot: [2.2, 6.3, -4.6] },
    { name: 'mouth', parent: 'body', pivot: [0, 3.4, -4.6] },
    { name: 'foot_l', parent: 'root', pivot: [-2.0, 0.8, 0] },
    { name: 'foot_r', parent: 'root', pivot: [2.0, 0.8, 0] },
];
const byBone = {};
for (const c of C) (byBone[c.bone] = byBone[c.bone] || []).push(c);
for (const b of bones) {
    b.cubes = (byBone[b.name] || []).map((c) => {
        const d = { origin: c.o, size: c.s, uv: [c.o.u, c.o.v] };
        return d;
    });
}
/* UV 需要按分配结果回填: 重新按顺序算一次 */
let px = 1, py = 1, rh = 0;
for (const c of C) {
    const fw = Math.ceil(2 * (c.s[0] + c.s[2])), fh = Math.ceil(c.s[2] + c.s[1]);
    if (px + fw > W) { px = 1; py += rh + 2; rh = 0; }
    c.uv = [px, py]; px += fw + 2; rh = Math.max(rh, fh);
}
for (const b of bones) {
    b.cubes = (byBone[b.name] || []).map((c, i) => {
        const o = { origin: c.o, size: c.s, uv: c.uv };
        void i;
        return o;
    });
}

const geo = {
    format_version: '1.12.0',
    'minecraft:geometry': [{
        description: {
            identifier: 'geometry.green_apple',
            texture_width: W, texture_height: H,
            visible_bounds_width: 1.6, visible_bounds_height: 1.8, visible_bounds_offset: [0, 0.7, 0],
        },
        bones,
    }],
};
fs.mkdirSync(OUT, { recursive: true });
fs.writeFileSync(path.join(OUT, 'green_apple.geo.json'), JSON.stringify(geo, null, 1));
cv.save(path.join(OUT, 'green_apple.png'));

/* ---------- 动画: hop 弹跳循环(0.8s) ---------- */
const anim = {
    format_version: '1.8.0',
    animations: {
        'animation.green_apple.hop': {
            loop: true,
            animation_length: 0.8,
            bones: {
                body: {
                    position: { 0: [0, 0, 0], 0.2: [0, 1.1, 0], 0.42: [0, 0, 0], 0.6: [0, 0.35, 0], 0.8: [0, 0, 0] },
                    scale: { 0: [1, 0.9, 1], 0.2: [0.94, 1.12, 0.94], 0.42: [1.08, 0.86, 1.08], 0.6: [1, 1, 1], 0.8: [1, 0.9, 1] },
                    rotation: { 0: [0, 0, 0], 0.2: [-6, 0, 0], 0.42: [3, 0, 0], 0.8: [0, 0, 0] },
                },
                leaf: { rotation: { 0: [0, 0, 0], 0.26: [0, 0, -18], 0.5: [0, 0, 14], 0.8: [0, 0, 0] } },
                stem: { rotation: { 0: [0, 0, 0], 0.26: [4, 0, -6], 0.5: [-3, 0, 5], 0.8: [0, 0, 0] } },
                mouth: { rotation: { 0: [0, 0, 0], 0.2: [10, 0, 0], 0.42: [-4, 0, 0], 0.8: [0, 0, 0] } },
                foot_l: { rotation: { 0: [0, 0, 0], 0.2: [18, 0, 0], 0.42: [-10, 0, 0], 0.8: [0, 0, 0] } },
                foot_r: { rotation: { 0: [0, 0, 0], 0.34: [18, 0, 0], 0.56: [-10, 0, 0], 0.8: [0, 0, 0] } },
                eye_l: { scale: { 0: [1, 1, 1], 0.42: [1, 0.55, 1], 0.56: [1, 1, 1], 0.8: [1, 1, 1] } },
                eye_r: { scale: { 0: [1, 1, 1], 0.42: [1, 0.55, 1], 0.56: [1, 1, 1], 0.8: [1, 1, 1] } },
            },
        },
    },
};
fs.writeFileSync(path.join(OUT, 'green_apple.animation.json'), JSON.stringify(anim, null, 1));
console.log(`青苹果模型: cube=${C.length} bones=${bones.length} 贴图=${W}x${H}  动画=hop(0.8s)`);
