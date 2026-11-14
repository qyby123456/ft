/* uv_atlas_check.cjs —— 逐面 UV 图集自检（只读）
 *
 * 查三件"看图看不出来、进游戏才发现"的事：
 *   ① 每个面的 uv 矩形是否**越界**（超出 0..texture_size）；
 *   ② 任意两个面的矩形是否**重叠** —— 一重叠，两个面就会互相显示对方的像素，
 *      在游戏里就是"贴图错乱"（本工程图集是货架式打包，不检查就看不出来）；
 *   ③ geo 声明的 texture_width/height 与图集 PNG 的实际尺寸是否一致。
 *
 * 用法: node tools/model3d/uv_atlas_check.cjs <geo.json> [图集.png] [--json]
 */
'use strict';

const fs = require('fs');
const path = require('path');

const geoFile = process.argv[2];
const texFile = process.argv[3] && !process.argv[3].startsWith('--') ? process.argv[3] : null;
const AS_JSON = process.argv.includes('--json');

if (!geoFile) {
  console.error('用法: node uv_atlas_check.cjs <geo.json> [图集.png] [--json]');
  process.exit(1);
}

const geo = JSON.parse(fs.readFileSync(geoFile, 'utf8'))['minecraft:geometry'][0];
const desc = geo.description || {};
const TW = desc.texture_width || 64;
const TH = desc.texture_height || 64;

const rects = [];
const problems = [];

for (const bone of geo.bones || []) {
  for (const [ci, cube] of (bone.cubes || []).entries()) {
    if (!cube.uv || typeof cube.uv !== 'object') { problems.push(`${bone.name}[${ci}] 不是逐面 UV`); continue; }
    for (const [face, v] of Object.entries(cube.uv)) {
      if (!v || !Array.isArray(v.uv) || !Array.isArray(v.uv_size)) {
        problems.push(`${bone.name}[${ci}].${face} 缺 uv/uv_size`);
        continue;
      }
      const [x, y] = v.uv;
      const [w, h] = v.uv_size;
      const r = { bone: bone.name, ci, face, x, y, w, h, x2: x + w, y2: y + h };
      rects.push(r);
      if (w <= 0 || h <= 0) problems.push(`${bone.name}[${ci}].${face} uv_size 非正 (${w},${h})`);
      if (x < 0 || y < 0 || r.x2 > TW || r.y2 > TH) {
        problems.push(`${bone.name}[${ci}].${face} 越界 x[${x},${r.x2}] y[${y},${r.y2}] > ${TW}x${TH}`);
      }
    }
  }
}

/* 两两求交（面数不多，直接 O(n²)） */
const overlaps = [];
for (let i = 0; i < rects.length; i++) {
  for (let j = i + 1; j < rects.length; j++) {
    const a = rects[i], b = rects[j];
    const ox = Math.min(a.x2, b.x2) - Math.max(a.x, b.x);
    const oy = Math.min(a.y2, b.y2) - Math.max(a.y, b.y);
    if (ox > 0 && oy > 0) {
      overlaps.push({ a: `${a.bone}[${a.ci}].${a.face}`, b: `${b.bone}[${b.ci}].${b.face}`, ox, oy });
    }
  }
}

let texDims = null;
if (texFile && fs.existsSync(texFile)) {
  const b = fs.readFileSync(texFile);
  texDims = { w: b.readUInt32BE(16), h: b.readUInt32BE(20) };
  if (texDims.w !== TW || texDims.h !== TH) {
    problems.push(`geo 声明 ${TW}x${TH}，但图集 PNG 实际是 ${texDims.w}x${texDims.h}`);
  }
}

const result = { geo: geoFile, texture: texFile, declared: [TW, TH], actual: texDims, faces: rects.length, overlaps, problems };

if (AS_JSON) {
  console.log(JSON.stringify(result, null, 2));
} else {
  console.log('=== 逐面 UV 图集自检 ===');
  console.log('geo : ' + path.basename(geoFile) + '   声明贴图 ' + TW + 'x' + TH);
  if (texDims) console.log('图集: ' + path.basename(texFile) + '   实际 ' + texDims.w + 'x' + texDims.h);
  console.log('逐面 UV 面数: ' + rects.length);
  console.log('');
  console.log('越界/字段问题 : ' + problems.length + (problems.length ? '' : ' ✓'));
  for (const p of problems.slice(0, 12)) console.log('  ✗ ' + p);
  console.log('矩形互相重叠 : ' + overlaps.length + (overlaps.length ? ' ✗（两个面会互相显示对方的像素 = 游戏里"贴图错乱"）' : ' ✓'));
  for (const o of overlaps.slice(0, 12)) console.log(`  ✗ ${o.a} × ${o.b}  交叠 ${o.ox}x${o.oy} px`);
  console.log('');
  const bad = problems.length + overlaps.length;
  console.log(bad === 0 ? '结论: 图集自检通过 ✓' : '结论: ' + bad + ' 项问题 ✗');
  process.exit(bad === 0 ? 0 : 1);
}
