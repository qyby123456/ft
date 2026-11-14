#!/usr/bin/env node
// geo 骨骼顺序整理: 把 Bedrock geo 的 bones 扁平列表排成**稳定的父级优先顺序**
//   根骨骼 → 父骨骼 → 子骨骼   (同级骨骼保持原有相对顺序)
//
// 用法:
//   node reorder_bones.cjs --check <geo.json...>     # 只报告, 不写文件 (默认行为)
//   node reorder_bones.cjs --write <geo.json...>     # 就地重排 (先写 .reorder.bak 备份)
//   node reorder_bones.cjs --check --json <geo...>   # 机器可读输出
//   node reorder_bones.cjs --check --all             # 检查 assets 下全部 geo
//
// 为什么这个重排是**语义等价**的 (不是"看起来差不多"):
//   1. Bedrock geo 的 bones 是**扁平列表**, 层级由 `parent` 字段决定, 不是由数组顺序决定。
//      重排只改数组顺序, 每个骨骼对象的字段与取值逐个字节不动 (Cube 顺序也一律不动)。
//   2. GeckoLib 4.8.4 烘焙时 (javap 核对 BakedModelFactory / BakedGeoModel):
//      - `BakedGeoModel` 只持有 `topLevelBones` (无 parent 的那些) + properties;
//      - 找骨骼走 `getBone(String)` / `searchForChildBone`, **按名字**, 不按索引;
//      - 渲染沿父子链递归。
//      ⇒ 骨内数组顺序对构建、查找、渲染都没有影响。
//   3. 同级骨骼的**相对顺序**用"按原索引优先"的稳定规则保留, 所以不是字母序、
//      不会打乱透明几何相关的手工排列。
//   4. 本工程所有离线工具 (hand_check / check_shadow_struct / cleaver_check / cube_space)
//      都按**名字**取骨骼 (grep 已核对), 不依赖索引。
//
// ⚠ 本脚本**绝不**自动改正式资源: 必须显式给 --write 才写文件, 且先留 .reorder.bak。
const fs = require('fs');
const path = require('path');

const argv = process.argv.slice(2);
const mode = argv.includes('--write') ? 'write' : 'check';
const asJson = argv.includes('--json');
const all = argv.includes('--all');
const files = argv.filter(a => !a.startsWith('--'));

const ROOT = path.resolve(__dirname, '../..');
const GEO_DIR = path.join(ROOT, 'src/main/resources/assets/ftboss/geo');
if (all) for (const f of fs.readdirSync(GEO_DIR)) if (f.endsWith('.geo.json')) files.push(path.join(GEO_DIR, f));
if (!files.length) {
  console.error('用法: node reorder_bones.cjs [--check|--write] <geo.json...> | --all');
  process.exit(2);
}

/**
 * 稳定拓扑排序 (父级优先)。
 * 保证: (a) 每个骨骼排在其 parent 之后; (b) 同级/无约束骨骼尽量保持原有相对顺序。
 * 做法: 按原顺序扫描, 想要输出第 i 个时, 先递归把它的祖先链按"原索引升序"输出;
 *       同一轮里已输出的祖先会让扫描指针继续前进 (不会整体重排)。
 */
function stableParentFirst(bones) {
  const n = bones.length;
  const indexOf = new Map();
  bones.forEach((b, i) => { if (!indexOf.has(b.name)) indexOf.set(b.name, i); });

  const emitted = new Array(n).fill(false);
  const out = [];
  const visiting = new Set();

  function demand(i) {
    if (emitted[i]) return;
    if (visiting.has(i)) return;            // 循环父级: 就地打断, 由 --check 单独报错
    visiting.add(i);
    const p = bones[i].parent;
    if (p !== undefined && p !== null) {
      const pi = indexOf.get(p);
      if (pi !== undefined && pi !== i) demand(pi);
    }
    visiting.delete(i);
    if (emitted[i]) return;
    emitted[i] = true;
    out.push(bones[i]);
  }

  for (let i = 0; i < n; i++) demand(i);
  // 兜底: 循环父级可能让个别骨骼没被输出, 按原顺序补齐 (不丢骨骼)
  for (let i = 0; i < n; i++) if (!emitted[i]) { emitted[i] = true; out.push(bones[i]); }
  return out;
}

/** 结构诊断: 重名 / 缺父级 / 循环父级 / 现有顺序问题 */
function diagnose(bones) {
  const err = [], warn = [];
  const seen = new Map();
  for (const b of bones) {
    if (seen.has(b.name)) err.push(`重复骨骼名: ${b.name}`);
    seen.set(b.name, b);
  }
  const indexOf = new Map();
  bones.forEach((b, i) => { if (!indexOf.has(b.name)) indexOf.set(b.name, i); });

  const outOfOrder = [];
  for (let i = 0; i < bones.length; i++) {
    const p = bones[i].parent;
    if (p === undefined || p === null) continue;
    if (!seen.has(p)) { err.push(`${bones[i].name}: parent "${p}" 不存在`); continue; }
    const pi = indexOf.get(p);
    if (pi > i) outOfOrder.push({ child: bones[i].name, childIndex: i, parent: p, parentIndex: pi });
  }
  // 循环父级
  for (const b of bones) {
    const chain = new Set([b.name]);
    let cur = b;
    while (cur && cur.parent) {
      if (chain.has(cur.parent)) { err.push(`循环父级: ${b.name} -> ... -> ${cur.parent}`); break; }
      chain.add(cur.parent);
      cur = seen.get(cur.parent);
    }
  }
  // 无 cube 的骨骼 = 挂点/动画节点/兼容接口, **不是**错误 (规格: 不得删除)
  const emptyBones = bones.filter(b => !(b.cubes && b.cubes.length)).map(b => b.name);
  const roots = bones.filter(b => !b.parent).map(b => b.name);
  return { err, warn, outOfOrder, emptyBones, roots };
}

const report = [];
let totalProblems = 0;

for (const file of files) {
  const abs = path.resolve(file);
  let raw;
  try { raw = fs.readFileSync(abs, 'utf8'); } catch (e) { console.error(`跳过 ${file}: ${e.message}`); totalProblems++; continue; }
  const g = JSON.parse(raw);
  const geos = g['minecraft:geometry'] || [];
  let fileChanged = false;
  const perGeo = [];

  for (const geo of geos) {
    const bones = geo.bones || [];
    const d = diagnose(bones);
    const before = bones.map(b => b.name);
    const reordered = stableParentFirst(bones);
    const after = reordered.map(b => b.name);
    const changed = before.some((nm, i) => nm !== after[i]);
    if (changed) fileChanged = true;

    // 语义保持断言: 骨骼集合与每个骨骼对象内容必须逐项相同 (只是顺序变)
    const sameSet = before.length === after.length && [...before].sort().join('\u0000') === [...after].sort().join('\u0000');
    const sameObjects = sameSet && after.every(nm => {
      const a = bones[before.indexOf(nm)], b = reordered[after.indexOf(nm)];
      return JSON.stringify(a) === JSON.stringify(b);   // 字段顺序+取值都必须一致
    });

    if (changed) geo.bones = reordered;
    perGeo.push({
      identifier: geo.description && geo.description.identifier,
      bones: bones.length,
      cubes: bones.reduce((a, b) => a + ((b.cubes || []).length), 0),
      changed, sameSet, sameObjects,
      roots: d.roots, emptyBones: d.emptyBones,
      errors: d.err, outOfOrder: d.outOfOrder,
      before, after
    });
    totalProblems += d.err.length;
  }

  if (mode === 'write' && fileChanged) {
    if (!fs.existsSync(abs + '.reorder.bak')) fs.writeFileSync(abs + '.reorder.bak', raw, 'utf8');
    fs.writeFileSync(abs, JSON.stringify(g, null, 2) + '\n', 'utf8');
  }
  report.push({ file: path.relative(ROOT, abs).replace(/\\/g, '/'), changed: fileChanged, geos: perGeo });
}

/* ---------- 输出 ---------- */
if (asJson) {
  console.log(JSON.stringify(report, null, 2));
} else {
  for (const r of report) {
    console.log(`\n=== ${r.file} ===`);
    for (const gg of r.geos) {
      console.log(`  ${gg.identifier}  骨骼 ${gg.bones} / cube ${gg.cubes}  根=${gg.roots.join(',') || '(无)'}`);
      console.log(`  顺序需要整理: ${gg.changed ? '是' : '否 (已是父级优先)'}`);
      if (gg.changed) {
        const moved = gg.before.filter((nm, i) => nm !== gg.after[i]).length;
        console.log(`    位置发生变化的骨骼: ${moved} / ${gg.bones}`);
        if (gg.outOfOrder.length) {
          console.log('    子骨骼排在父骨骼之前:');
          for (const o of gg.outOfOrder) console.log(`      ${o.child}(#${o.childIndex})  在父级 ${o.parent}(#${o.parentIndex}) 之前`);
        }
        console.log(`    排序后: ${gg.after.join(' → ')}`);
      }
      console.log(`  语义保持: 骨骼集合一致=${gg.sameSet ? '✓' : '✗'} 每个骨骼对象逐字节一致=${gg.sameObjects ? '✓' : '✗'}`);
      console.log(`  无 cube 的骨骼 (挂点/动画节点, 保留): ${gg.emptyBones.length ? gg.emptyBones.join(', ') : '无'}`);
      // error 分类
      if (gg.errors.length) for (const e of gg.errors) console.log(`  [error] ${e}`);
    }
  }
  const changedCount = report.filter(r => r.changed).length;
  console.log(`\n结论: 检查 ${report.length} 个文件, ${changedCount} 个需要整理, 结构 error ${totalProblems} 项`);
  if (mode === 'check' && changedCount) console.log('(只读模式; 加 --write 才会就地重排并留 .reorder.bak 备份)');
}
process.exit(totalProblems ? 1 : 0);
