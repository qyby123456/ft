/* weapons_icon_audit.cjs —— 批量体检"平面物品模型 + 贴图"是否配对（只读）
 *
 * 背景（2026-09-15 用户反馈）："几乎全部武器除了已经自带模型的武器外都有一样的问题"。
 * 自带 3D 模型的武器（无 parent / parent=block/block）显示正常，
 * 而走 `item/handheld` + `layer0` 的平面武器全都不对。
 *
 * 本脚本对每个"平面"物品模型查四件事：
 *   ① 是否挂了生成式父链（item/handheld | item/generated）却没有 layer0 → 烘不出面（AGENTS.md 记过）；
 *   ② UV 是否越出 0..16（Java 硬规则）；
 *   ③ 每个面的贴图引用能否解析到实际存在的贴图文件；
 *   ④ **采样该面 UV 区域时，落点是否几乎全是透明像素**（= 贴图与模型不匹配，
 *      也就是"贴图错乱"最典型的一种：拿 A 的贴图配 B 的模型）。
 *
 * 用法: node tools/model3d/weapons_icon_audit.cjs [--dir <models/item 目录>] [--json]
 */
'use strict';

const fs = require('fs');
const path = require('path');
const { decodePNG } = require('./png.cjs');

const ROOT = path.resolve(__dirname, '..', '..');
const args = process.argv.slice(2);
const opt = (n, d) => { const i = args.indexOf(n); return i >= 0 && args[i + 1] !== undefined ? args[i + 1] : d; };
const DIR = opt('--dir', path.join(ROOT, 'src/main/resources/assets/ftboss/models/item'));
const TEXDIR = path.join(ROOT, 'src/main/resources/assets/ftboss/textures');
const AS_JSON = args.includes('--json');

const texCache = new Map();
function texInfo(res) {
  // "ftboss:item/xxx" 或 "ftboss:block/xxx"
  const id = res.includes(':') ? res.split(':')[1] : res;
  const p = path.join(TEXDIR, id + '.png');
  if (!texCache.has(p)) {
    if (!fs.existsSync(p)) texCache.set(p, null);
    else { const { w, h, data } = decodePNG(p); texCache.set(p, { w, h, data, p }); }
  }
  return texCache.get(p);
}

const rows = [];
for (const f of fs.readdirSync(DIR)) {
  if (!f.endsWith('.json')) continue;
  let m;
  try { m = JSON.parse(fs.readFileSync(path.join(DIR, f), 'utf8')); } catch { rows.push({ f, err: 'JSON 解析失败' }); continue; }
  const parent = m.parent || '';
  const generated = /item\/(handheld|generated)/.test(parent);
  const hasLayer0 = !!(m.textures && m.textures.layer0);
  const elements = m.elements || [];
  const texKeys = m.textures || {};

  const problems = [];
  if (generated && !hasLayer0) problems.push('挂了生成式父链却没有 layer0（会烘不出面）');
  if (elements.length === 0) problems.push('没有 elements（纯平面）');

  let uvBad = 0, emptyFaces = 0, unknownTex = 0, sampled = 0;
  for (const e of elements) {
    for (const [face, fd] of Object.entries(e.faces || {})) {
      const t = fd.texture;
      if (!t) continue;
      const res = texKeys[t.replace('#', '')];
      if (!res) { unknownTex++; continue; }
      const info = texInfo(res);
      if (!info) { unknownTex++; continue; }
      let uv = fd.uv;
      if (!uv) continue;
      const [u1, v1, u2, v2] = uv;
      if ([u1, v1, u2, v2].some(v => v < -0.001 || v > 16.001)) uvBad++;
      // 采样该面 UV 矩形内的像素，统计不透明比例
      const x0 = Math.round(Math.min(u1, u2) / 16 * info.w), x1 = Math.round(Math.max(u1, u2) / 16 * info.w);
      const y0 = Math.round(Math.min(v1, v2) / 16 * info.h), y1 = Math.round(Math.max(v1, v2) / 16 * info.h);
      let tot = 0, op = 0;
      for (let y = y0; y < Math.max(y0 + 1, y1); y++) {
        for (let x = x0; x < Math.max(x0 + 1, x1); x++) {
          if (x < 0 || y < 0 || x >= info.w || y >= info.h) continue;
          const i = (y * info.w + x) * 4;
          tot++;
          if (info.data[i + 3] > 24) op++;
        }
      }
      sampled++;
      if (tot > 0 && op / tot < 0.4) emptyFaces++;      // 大半落空 = 贴图不匹配
    }
  }
  if (uvBad) problems.push(uvBad + ' 个面 UV 越出 0..16');
  if (unknownTex) problems.push(unknownTex + ' 个面贴图引用解析不到');
  if (sampled > 0 && emptyFaces > 0) problems.push(emptyFaces + '/' + sampled + ' 个面采样大半落空（贴图与模型不匹配）');

  rows.push({ f, parent, generated, hasLayer0, elements: elements.length, sampled, emptyFaces, uvBad, problems });
}

const bad = rows.filter(r => r.problems && r.problems.length);
if (AS_JSON) console.log(JSON.stringify(rows, null, 2));
else {
  console.log('=== 平面物品模型体检 ===');
  console.log('目录: ' + path.relative(ROOT, DIR) + '   共 ' + rows.length + ' 个模型');
  console.log('');
  console.log('有问题的模型: ' + bad.length);
  for (const r of bad) {
    console.log('  ✗ ' + r.f + '   [parent=' + (r.parent || '无') + ']');
    for (const p of r.problems) console.log('      · ' + p);
  }
  const flatBroken = bad.filter(r => r.generated && r.hasLayer0);
  console.log('');
  console.log('其中"平面武器"(item/handheld + layer0) 出问题的: ' + flatBroken.length +
    ' / ' + rows.filter(r => r.generated && r.hasLayer0).length);
  process.exit(bad.length === 0 ? 0 : 1);
}
