#!/usr/bin/env node
// geo 结构校验: 骨骼名/父子引用/重复名/循环父级/父级优先/尺寸/旋转/UV(两种写法)/贴图越界
//
// 用法:
//   node validate.cjs <geo.json> [必需骨骼名,逗号分隔]
//   node validate.cjs --all                 # 校验 assets 下全部 geo (批量)
//   node validate.cjs --all --json
//
// 输出分类统一为 [error] / [warning] / [review]; 退出码: 有 error 时非 0。
//
// ★ 2026-09-16 修复的真实缺陷:
//   旧版用 `(function walk(bs){ ... if (b.children) walk(b.children) })(geo.bones)` 遍历骨骼,
//   但 **Bedrock geo 的 bones 必须是扁平列表 + `parent` 字段** (本工程 README 自己写了:
//   "嵌套 children 会让 GeckoLib 读不到 cube")。于是那段"递归"其实只跑了一层,
//   虽然结果凑巧正确 (扁平列表一层就是全部), 但写法会误导后来人以为骨骼可以是树;
//   而且它**完全没有检查** parent 是否存在/是否有重复名/是否有循环父级/是否父级优先。
//   现在统一走扁平列表 + 显式父级图检查。
//
// UV 支持两种写法:
//   (a) box-UV   "uv": [U, V]                                  -> 必须整数(浮点会让像素写入被静默丢弃)
//   (b) 逐面 UV  "uv": { "north": {"uv":[u,v],"uv_size":[w,h]} } -> 允许小数(Bedrock 合法),
//       uv_size 为负 = 镜像 (合法, 不是错误)
const fs = require('fs');
const path = require('path');

const argv = process.argv.slice(2);
const asJson = argv.includes('--json');
const useAll = argv.includes('--all');
const ROOT = path.resolve(__dirname, '../..');
const GEO_DIR = path.join(ROOT, 'src/main/resources/assets/ftboss/geo');

let files = argv.filter(a => !a.startsWith('--'));
if (useAll) {
  files = fs.readdirSync(GEO_DIR).filter(f => f.endsWith('.geo.json')).sort().map(f => path.join(GEO_DIR, f));
}
if (!files.length) {
  console.error('用法: node validate.cjs <geo.json> [必需骨骼名,逗号分隔] | --all [--json]');
  process.exit(2);
}
// 兼容旧的单文件用法: 只有第一个文件时, 第二个位置参数是"必需骨骼名"
const need = (!useAll && files.length > 1 && !files[1].endsWith('.json')) ? files.splice(1)[0].split(',') : [];

const FACES = ['north', 'south', 'east', 'west', 'up', 'down'];
const num3 = v => Array.isArray(v) && v.length === 3 && v.every(n => typeof n === 'number' && Number.isFinite(n));

const reports = [];
let anyError = false;

for (const file of files) {
  const abs = path.resolve(file);
  let g;
  try { g = JSON.parse(fs.readFileSync(abs, 'utf8')); }
  catch (e) { reports.push({ file: path.relative(ROOT, abs).replace(/\\/g, '/'), fatal: 'JSON 解析失败: ' + e.message }); anyError = true; continue; }

  const out = { file: path.relative(ROOT, abs).replace(/\\/g, '/'), geos: [] };
  for (const geo of (g['minecraft:geometry'] || [])) {
    const err = [], warn = [], review = [];
    const desc = geo.description || {};
    const W = desc.texture_width, H = desc.texture_height;
    const bones = geo.bones || [];

    // ---- 骨骼是扁平列表 (Bedrock 规范) ----
    if (!Array.isArray(bones)) { err.push('bones 不是数组'); continue; }
    const nested = bones.filter(b => b && b.children !== undefined);
    if (nested.length) {
      err.push(`有 ${nested.length} 个骨骼带 "children" 字段 (${nested.slice(0, 3).map(b => b.name).join(', ')}...) —— ` +
        'Bedrock/GekkoLib 要求扁平列表 + parent, 嵌套 children 会导致 GeckoLib 读不到 cube, 整个模型不渲染');
    }

    const names = [], indexOf = new Map();
    bones.forEach((b, i) => {
      names.push(b.name);
      if (!indexOf.has(b.name)) indexOf.set(b.name, i);
    });
    // 重复名
    const dup = names.filter((n, i) => names.indexOf(n) !== i);
    for (const n of [...new Set(dup)]) err.push(`重复骨骼名: ${n}`);

    let cubes = 0, boxUv = 0, faceUv = 0, mirroredUv = 0, negativeUvSize = 0;

    for (const b of bones) {
      if (typeof b.name !== 'string' || !b.name) { err.push(`第 ${indexOf.get(b.name)} 个骨骼缺少 name`); continue; }
      // parent 必须存在
      if (b.parent !== undefined && b.parent !== null) {
        if (!indexOf.has(b.parent)) err.push(`${b.name}: parent "${b.parent}" 不存在`);
        else if (indexOf.get(b.parent) > indexOf.get(b.name)) {
          // 不是错误, 但属于本次整理要消除的可维护性问题
          review.push(`${b.name}(#${indexOf.get(b.name)}) 排在父级 ${b.parent}(#${indexOf.get(b.parent)}) 之前 (渲染无影响, 建议父级优先)`);
        }
      }
      if (!num3(b.pivot)) err.push(`${b.name}: pivot 异常 ${JSON.stringify(b.pivot)}`);
      if (b.rotation && !num3(b.rotation)) err.push(`${b.name}: rotation 异常 ${JSON.stringify(b.rotation)}`);
      if (b.inflate !== undefined && typeof b.inflate !== 'number') err.push(`${b.name}: inflate 非数值 ${JSON.stringify(b.inflate)}`);

      for (const c of (b.cubes || [])) {
        cubes++;
        if (!num3(c.size) || c.size.some(s => !(s > 0))) err.push(`${b.name}: size ${JSON.stringify(c.size)}`);
        if (!num3(c.origin)) err.push(`${b.name}: origin ${JSON.stringify(c.origin)}`);
        if (c.rotation && !num3(c.rotation)) err.push(`${b.name}: cube rotation 异常 ${JSON.stringify(c.rotation)} (注意 Bedrock 是数组 [x,y,z], 不是 Java 的 {origin,axis,angle})`);
        if (Array.isArray(c.uv)) {
          boxUv++;
          if (c.uv.length !== 2 || c.uv.some(v => !Number.isFinite(v))) {
            err.push(`${b.name}: uv ${JSON.stringify(c.uv)}`);
          } else {
            if (!c.uv.every(Number.isInteger)) err.push(`${b.name}: box-UV 非整数 ${JSON.stringify(c.uv)}(浮点 UV 会让像素写入被丢)`);
            const [w, h, d] = c.size;
            const nw = 2 * (d + w), nh = d + h;
            if (Number.isFinite(W) && Number.isFinite(H) && (c.uv[0] + nw > W || c.uv[1] + nh > H)) {
              err.push(`${b.name}: box-UV 越界 uv=${JSON.stringify(c.uv)} 需要 ${nw}x${nh} > ${W}x${H}`);
            }
          }
        } else if (c.uv && typeof c.uv === 'object') {
          faceUv++;
          for (const k of Object.keys(c.uv)) {
            if (!FACES.includes(k)) err.push(`${b.name}: 未知面 "${k}"`);
            const e = c.uv[k];
            if (!e || !Array.isArray(e.uv)) { err.push(`${b.name}.${k}: 缺 uv`); continue; }
            const s = e.uv_size;
            if (s !== undefined && !(Array.isArray(s) && s.length === 2 && s.every(n => typeof n === 'number'))) {
              err.push(`${b.name}.${k}: uv_size ${JSON.stringify(s)}`);
            }
            if (Array.isArray(s) && s.some(n => n < 0)) negativeUvSize++;   // 镜像 UV: 合法
            // 镜像的另一种写法: uv[0] > uv[1] 之类 —— 这里只统计, 不判错
            const endU = e.uv[0] + (s ? s[0] : c.size[0]);
            const endV = e.uv[1] + (s ? s[1] : c.size[1]);
            if (Number.isFinite(W) && Number.isFinite(H) &&
                (Math.min(e.uv[0], endU) < 0 || Math.min(e.uv[1], endV) < 0 || Math.max(e.uv[0], endU) > W || Math.max(e.uv[1], endV) > H)) {
              warn.push(`${b.name}.${k}: UV 区域超出贴图 uv=${JSON.stringify(e.uv)} end=${endU},${endV} 贴图 ${W}x${H}`);
            }
          }
        } else {
          err.push(`${b.name}: 缺少 uv`);
        }
      }
    }

    // ---- 循环父级 ----
    for (const b of bones) {
      const chain = new Set([b.name]);
      let cur = b;
      while (cur && cur.parent !== undefined && cur.parent !== null) {
        if (chain.has(cur.parent)) { err.push(`循环父级: ${b.name} -> ... -> ${cur.parent}`); break; }
        chain.add(cur.parent);
        cur = bones[indexOf.get(cur.parent)];
      }
    }

    // ---- 要求存在的骨骼 (旧用法兼容) ----
    if (need.length) {
      const missing = need.filter(n => !names.includes(n));
      if (missing.length) err.push('必需骨骼缺失: ' + missing.join(', '));
    }

    // ---- review: 无 cube 的骨骼不是错误 (可能是挂点/动画节点/兼容接口) ----
    const emptyBones = bones.filter(b => !(b.cubes && b.cubes.length)).map(b => b.name);
    const roots = bones.filter(b => !b.parent).map(b => b.name);
    if (Number.isFinite(W) && Number.isFinite(H) && (W % 16 || H % 16)) {
      review.push(`贴图尺寸 ${W}x${H} 不是 16 的整数倍 (逐面 UV 下合法, 但核对一下是否本意)`);
    }

    if (err.length) anyError = true;
    out.geos.push({
      identifier: desc.identifier,
      texture: [W, H],
      boneCount: bones.length, cubeCount: cubes,
      boxUv, faceUv, negativeUvSize,
      roots, emptyBones,
      parentFirst: review.every(r => !r.includes('排在父级')),
      error: err, warning: warn, review
    });
  }
  reports.push(out);
}

if (asJson) {
  console.log(JSON.stringify(reports, null, 2));
} else {
  let e = 0, w = 0, r = 0;
  for (const rep of reports) {
    console.log(`\n=== ${rep.file} ===`);
    if (rep.fatal) { console.log('  [error] ' + rep.fatal); e++; continue; }
    for (const g of rep.geos) {
      console.log(`  ${g.identifier}  贴图 ${g.texture[0]}x${g.texture[1]}  骨骼 ${g.boneCount} / cube ${g.cubeCount} (box-UV ${g.boxUv} / 逐面UV ${g.faceUv}${g.negativeUvSize ? ` / 负 uv_size ${g.negativeUvSize}` : ''})`);
      console.log(`  根骨骼: ${g.roots.join(', ') || '(无)'}   父级优先: ${g.parentFirst ? '是 ✓' : '否'}`);
      if (g.emptyBones.length) console.log(`  [info]    无 cube 的骨骼 (挂点/动画节点/兼容接口, 按规格**不得删除**): ${g.emptyBones.join(', ')}`);
      for (const m of g.error) { console.log('  [error]   ' + m); e++; }
      for (const m of g.warning) { console.log('  [warning] ' + m); w++; }
      for (const m of g.review) { console.log('  [review]  ' + m); r++; }
    }
  }
  console.log(`\n结论: 检查 ${reports.length} 个文件 —— error ${e} / warning ${w} / review ${r}`);
}
process.exit(anyError ? 1 : 0);
