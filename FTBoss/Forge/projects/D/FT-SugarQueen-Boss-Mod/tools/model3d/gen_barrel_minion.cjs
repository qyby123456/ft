/* ============================================================================
 * 小酒桶生物(barrel_minion) 生成器  —— 2026-09-11 按用户要求新增
 *   "画1个小酒桶生物模型, 替换烈焰人召唤, 生物ai类似于小僵尸的攻击和小姜饼人差不多"
 *
 * 产出(写入 OUT 目录):
 *   barrel_minion.geo.json          几何(Bedrock 1.12.0): 桶身/两道铁箍/两眼/嘴/酒嘴/两腿/两臂 = 11 cube, 5 骨骼
 *   barrel_minion.animation.json    动画: idle / walk / attack / hurt / death
 *   barrel_minion.png               贴图 64x64(程序化绘制: 木条 + 铁箍 + 眼睛)
 *
 * 约定(与 GeckoLib 对齐, 已多次验证):
 *   - cube.origin 是**绝对模型坐标**, y 向上, 脚底 y=0, 模型中心 x=z=0
 *   - box-UV: uv=[u,v], 布局 row1: up[u+d,v,w,d] down[u+d+w,v,w,d]
 *                          row2: east[u,v+d,d,h] north[u+d,v+d,w,h] west[u+d+w,v+d,d,h] south[u+d+w+d,v+d,w,h]
 *   - 动画 position 单位 = 1/16 格; rotation 单位 = 度
 * ========================================================================== */
const fs = require('fs');
const path = require('path');
const { Canvas } = require('./png.cjs');

const OUT = process.argv[2] || 'out';
const NAME = 'barrel_minion';
const TEX_W = 64, TEX_H = 64;

/* ---------------- 1. 立方体规格 ---------------- */
const C = [];
const cube = (bone, o, s, uv) => C.push({ bone, o, s, uv });

// 桶身(10x11x10) + 两道铁箍(略大一圈)
cube('body', [-5, 4, -5], [10, 11, 10], [0, 0]);
cube('body', [-5, 5.0, -5], [10, 2, 10], [0, 24]);      // 下箍
cube('body', [-5, 12.0, -5], [10, 2, 10], [0, 24]);     // 上箍
// 眼睛(白+瞳) 在正面 z=-5 略外凸
cube('body', [-4.0, 10.5, -5.2], [2, 2, 1], [32, 40]);
cube('body', [2.0, 10.5, -5.2], [2, 2, 1], [32, 40]);
cube('body', [-3.6, 10.9, -5.5], [1.2, 1.2, 1], [32, 47]);
cube('body', [2.4, 10.9, -5.5], [1.2, 1.2, 1], [32, 47]);
// 嘴(横槽)
cube('body', [-2, 7.5, -5.2], [4, 1, 1], [40, 40]);
// 酒嘴(黄铜)
cube('body', [0.5, 4.6, -6.2], [2, 2, 2], [50, 40]);
// 两腿(3x4x4) 两臂(2x7x3)
cube('leg_l', [1.0, 0, -2], [3, 4, 4], [0, 40]);
cube('leg_r', [-4.0, 0, -2], [3, 4, 4], [0, 40]);
cube('arm_l', [5, 6, -1.5], [2, 7, 3], [16, 40]);
cube('arm_r', [-7, 6, -1.5], [2, 7, 3], [16, 40]);

/* ---------------- 2. 贴图 ---------------- */
const cv = new Canvas(TEX_W, TEX_H);
const hash = (x, y) => ((x * 7 + y * 13) % 5) / 5;   // 稳定伪随机(木纹)

// ① 桶身外层: box-UV 布局里的 4 个侧面矩形(v 从 11 到 22, u 0..40)
const WOOD = [[122, 78, 44], [104, 64, 34], [138, 92, 54]];
function paintStaves(x0, y0, w, h) {
  for (let x = 0; x < w; x++) {
    const stave = Math.floor(x / 5);
    const base = WOOD[stave % WOOD.length];
    const gap = (x % 5 === 0);
    for (let y = 0; y < h; y++) {
      const n = hash(x, y) * 12 - 6;
      const r = base[0] + n, g = base[1] + n, b = base[2] + n;
      cv.set(x0 + x, y0 + y, gap ? r * 0.62 : r, gap ? g * 0.62 : g, gap ? b * 0.62 : b, 1);
    }
  }
}
paintStaves(0, 11, 10, 11);    // east
paintStaves(10, 11, 10, 11);   // north
paintStaves(20, 11, 10, 11);   // west
paintStaves(30, 11, 10, 11);   // south
// ② 桶盖(up 在 u10..20 / down 在 u20..30, v 0..10): 木板 + 一圈深色边
for (const ux of [10, 20]) {
  cv.rect(ux, 0, 10, 10, 138, 92, 54, 1);
  for (let i = 0; i < 10; i++) {
    cv.set(ux + i, 0, 96, 60, 32, 1);
    cv.set(ux + i, 9, 96, 60, 32, 1);
    cv.set(ux, i, 96, 60, 32, 1);
    cv.set(ux + 9, i, 96, 60, 32, 1);
  }
  cv.vline(ux + 5, 1, 8, 110, 70, 40, 1);
}
// ③ 铁箍(uv 0,24 起, 大小 10x2x10 -> 宽 40 高 12): 深铁灰 + 高光
for (let x = 0; x < 40; x++) {
  for (let y = 0; y < 12; y++) {
    const hi = (y === 0 || y === 1);
    const v = hi ? 108 : (y >= 10 ? 46 : 74);
    cv.set(x, 24 + y, v, v + 4, v + 8, 1);
  }
}
// ④ 眼睛白(2x2x1 -> 宽 6 高 3) + 瞳(1.2x1.2x1 -> 宽 5 高 3) + 嘴(4x1x1 -> 宽 10 高 2) + 酒嘴(2x2x2 -> 宽 8 高 4)
cv.rect(32, 40, 6, 3, 244, 244, 238, 1);
cv.rect(32, 47, 5, 3, 30, 26, 30, 1);
cv.rect(40, 40, 10, 2, 48, 30, 30, 1);
cv.rect(50, 40, 8, 4, 196, 158, 62, 1);
for (let i = 0; i < 8; i++) cv.set(50 + i, 42, 156, 120, 44, 1);
// ⑤ 腿/臂(uv 0,40 / 16,40): 深木色 + 铁灰端
cv.rect(0, 40, 14, 7, 96, 62, 36, 1);
cv.rect(16, 40, 10, 10, 96, 62, 36, 1);
for (let i = 0; i < 7; i++) { cv.set(1 + i, 43, 68, 44, 26, 1); }
for (let i = 0; i < 9; i++) { cv.set(17 + (i % 3), 41 + Math.floor(i / 3), 68, 44, 26, 1); }

/* ---------------- 3. geo ---------------- */
const PIVOT = {
  body: [0, 4, 0],
  leg_l: [2.5, 4, 0],
  leg_r: [-2.5, 4, 0],
  arm_l: [5, 12, 0],
  arm_r: [-5, 12, 0]
};
const ORDER = ['body', 'leg_l', 'leg_r', 'arm_l', 'arm_r'];
const bones = ORDER.map(name => {
  const cubes = C.filter(c => c.bone === name).map(c => ({ origin: c.o, size: c.s, uv: c.uv }));
  return { name, pivot: PIVOT[name], cubes };
});
const geo = {
  format_version: '1.12.0',
  'minecraft:geometry': [{
    description: {
      identifier: `geometry.${NAME}`,
      texture_width: TEX_W,
      texture_height: TEX_H,
      visible_bounds_width: 2,
      visible_bounds_height: 2.5,
      visible_bounds_offset: [0, 1, 0]
    },
    bones
  }]
};

/* ---------------- 4. 动画 ---------------- */
const A = `animation.${NAME}.`;
const anims = {
  format_version: '1.8.0',
  animations: {
    [`${A}idle`]: {
      loop: true, animation_length: 1.6,
      bones: {
        body: { position: { '0.0': [0, 0, 0], '0.8': [0, -0.4, 0], '1.6': [0, 0, 0] } },
        arm_l: { rotation: { '0.0': [0, 0, -3], '0.8': [0, 0, 3], '1.6': [0, 0, -3] } },
        arm_r: { rotation: { '0.0': [0, 0, 3], '0.8': [0, 0, -3], '1.6': [0, 0, 3] } }
      }
    },
    [`${A}walk`]: {
      loop: true, animation_length: 0.8,
      bones: {
        body: { position: { '0.0': [0, 0, 0], '0.4': [0, -0.6, 0], '0.8': [0, 0, 0] },
                rotation: { '0.0': [0, 0, -1.5], '0.4': [0, 0, 1.5], '0.8': [0, 0, -1.5] } },
        leg_l: { rotation: { '0.0': [-25, 0, 0], '0.4': [25, 0, 0], '0.8': [-25, 0, 0] } },
        leg_r: { rotation: { '0.0': [25, 0, 0], '0.4': [-25, 0, 0], '0.8': [25, 0, 0] } },
        arm_l: { rotation: { '0.0': [25, 0, 0], '0.4': [-25, 0, 0], '0.8': [25, 0, 0] } },
        arm_r: { rotation: { '0.0': [-25, 0, 0], '0.4': [25, 0, 0], '0.8': [-25, 0, 0] } }
      }
    },
    [`${A}attack`]: {
      loop: false, animation_length: 0.5,
      bones: {
        body: { rotation: { '0.0': [0, 0, 0], '0.15': [-18, 0, 0], '0.35': [6, 0, 0], '0.5': [0, 0, 0] } },
        arm_l: { rotation: { '0.0': [0, 0, 0], '0.15': [-95, 0, 0], '0.35': [20, 0, 0], '0.5': [0, 0, 0] } },
        arm_r: { rotation: { '0.0': [0, 0, 0], '0.15': [-70, 0, 0], '0.35': [15, 0, 0], '0.5': [0, 0, 0] } }
      }
    },
    [`${A}hurt`]: {
      loop: false, animation_length: 0.4,
      bones: { body: { rotation: { '0.0': [0, 0, 0], '0.1': [12, 0, -8], '0.4': [0, 0, 0] } } }
    },
    [`${A}death`]: {
      loop: false, animation_length: 1.0,
      bones: {
        body: { rotation: { '0.0': [0, 0, 0], '0.5': [0, 0, 78], '1.0': [0, 0, 88] },
                position: { '0.0': [0, 0, 0], '0.5': [0, -1.5, 0], '1.0': [0, -2.0, 0] } },
        arm_l: { rotation: { '0.0': [0, 0, 0], '0.5': [0, 0, 40] } },
        arm_r: { rotation: { '0.0': [0, 0, 0], '0.5': [0, 0, -40] } }
      }
    }
  }
};

/* ---------------- 5. 写出 ---------------- */
fs.mkdirSync(OUT, { recursive: true });
fs.writeFileSync(path.join(OUT, `${NAME}.geo.json`), JSON.stringify(geo, null, 1));
fs.writeFileSync(path.join(OUT, `${NAME}.animation.json`), JSON.stringify(anims, null, 1));
cv.save(path.join(OUT, `${NAME}.png`));

const total = C.length;
console.log(`已生成 ${NAME}: cube=${total} 骨骼=${bones.length} 贴图=${TEX_W}x${TEX_H}`);
console.log(`  ${path.join(OUT, NAME + '.geo.json')}`);
console.log(`  ${path.join(OUT, NAME + '.animation.json')}`);
console.log(`  ${path.join(OUT, NAME + '.png')}`);
