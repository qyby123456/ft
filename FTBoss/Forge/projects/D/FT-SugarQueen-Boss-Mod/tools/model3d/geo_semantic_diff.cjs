#!/usr/bin/env node
// geo 语义差异对比: **不对比文件哈希**, 对比"模型实际含义"
//
// 用法:
//   node geo_semantic_diff.cjs <before.geo.json> <after.geo.json> [--json]
//   node geo_semantic_diff.cjs --anim <before.animation.json> <after.animation.json> [--json]
//
// 为什么需要它 (规格要求): 文件哈希一变就说"改过了"没有信息量。
// 骨骼顺序整理属于"只动数组顺序"的改动, 必须能证明出"语义零变化"。
// 本脚本把语义拆成若干可核对维度, 逐维度比对并给出结论:
//
//   geo:
//     1. 骨骼集合 (多/少/改名)
//     2. 每个骨骼的 parent / pivot / rotation 与**全部其他字段**(含自定义字段)
//     3. 每个骨骼的 cube 数量, 以及 cube 的**逐个顺序与内容** (顺序也算语义: Bedrock 允许依赖)
//     4. description (identifier / 贴图尺寸 / visible_bounds*)
//     5. 烘焙后的**父子层级**: 根骨骼顺序 + 每个骨骼的子骨骼顺序
//     6. 父级优先性 (子是否排在父之后)
//     7. 每个骨骼的 cube 包围盒 (位置/尺寸的数值摘要) —— 位置改了必须报出来
//     8. 逐面 UV 与 box-UV 的归属数量
//
//   anim:
//     1. 动画段集合
//     2. 每段 animation_length / loop
//     3. 每段引用的骨骼集合
//     4. 每个 骨骼.轨道 的关键帧**时间键集合**与每个键的取值(含 pre/post/lerp_mode 等对象写法)
//     5. 关键帧时间是否按数值升序 (渲染依赖)
const fs = require('fs');

const argv = process.argv.slice(2);
const asJson = argv.includes('--json');
const isAnim = argv.includes('--anim');
const pos = argv.filter(a => !a.startsWith('--'));
if (pos.length < 2) {
  console.error('用法: node geo_semantic_diff.cjs [--anim] <before.json> <after.json> [--json]');
  process.exit(2);
}

/* 递归深比较: **与对象键的书写顺序无关**。
   为什么不能用 JSON.stringify 比: 骨骼顺序整理会经 JSON.parse -> JSON.stringify 往返,
   对象键的插入顺序可能变化, stringify 结果就不同 —— 那是"文件写法"差异, 不是语义差异。
   (本脚本第一版就踩了这个坑, 报了 4 个假差异。)
   数组**按索引**比较: cube 顺序属于语义, 必须能报出来。 */
function deepEqual(a, b) {
  if (a === b) return true;
  if (typeof a === 'number' && typeof b === 'number') return Object.is(a, b) || Math.abs(a - b) < 1e-12;
  if (a === null || b === null || typeof a !== 'object' || typeof b !== 'object') return false;
  if (Array.isArray(a) || Array.isArray(b)) {
    if (!Array.isArray(a) || !Array.isArray(b) || a.length !== b.length) return false;
    for (let i = 0; i < a.length; i++) if (!deepEqual(a[i], b[i])) return false;
    return true;
  }
  const ka = Object.keys(a), kb = Object.keys(b);
  if (ka.length !== kb.length) return false;
  for (const k of ka) { if (!Object.prototype.hasOwnProperty.call(b, k)) return false; if (!deepEqual(a[k], b[k])) return false; }
  return true;
}
/** 键序无关的规范字符串: 对象键排序后序列化 (数组顺序保留) */
function CANON(v) {
  if (v === null || typeof v !== 'object') return JSON.stringify(v);
  if (Array.isArray(v)) return '[' + v.map(CANON).join(',') + ']';
  return '{' + Object.keys(v).sort().map(k => JSON.stringify(k) + ':' + CANON(v[k])).join(',') + '}';
}
function num(v) { return Number(v.toFixed(6)); }
function boxOf(c) { return { o: (c.origin || []).map(num), s: (c.size || []).map(num), inflate: c.inflate === undefined ? 0 : num(c.inflate) }; }
function uvKind(c) { return Array.isArray(c.uv) ? 'box' : (c.uv && typeof c.uv === 'object' ? 'face' : 'none'); }

function geoModel(j) {
  const geo = (j['minecraft:geometry'] || [])[0] || {};
  const bones = geo.bones || [];
  const byName = new Map(bones.map(b => [b.name, b]));
  const childrenOf = new Map();
  const roots = [];
  for (const b of bones) {
    if (b.parent && byName.has(b.parent)) {
      if (!childrenOf.has(b.parent)) childrenOf.set(b.parent, []);
      childrenOf.get(b.parent).push(b.name);
    } else roots.push(b.name);      // 无 parent 或 parent 指不到 -> GeckoLib 也视作顶层
  }
  // 全字段(除 cubes 与 name)摘要, 保证自定义字段也被比到
  const fields = {};
  for (const b of bones) {
    const rest = {};
    for (const k of Object.keys(b)) if (k !== 'name' && k !== 'cubes') rest[k] = b[k];
    fields[b.name] = rest;
  }
  const cubes = {};
  for (const b of bones) cubes[b.name] = (b.cubes || []).map(c => ({ canon: CANON(c), box: boxOf(c), uv: uvKind(c) }));
  const orderIndex = new Map(bones.map((b, i) => [b.name, i]));
  const parentFirst = bones.every((b, i) => {
    if (!b.parent || !byName.has(b.parent)) return true;
    return orderIndex.get(b.parent) < i;
  });
  const boxSummary = {};
  for (const b of bones) {
    const bs = (b.cubes || []).map(boxOf);
    boxSummary[b.name] = bs;
  }
  return {
    identifier: geo.description && geo.description.identifier,
    description: geo.description,
    boneNames: bones.map(b => b.name),
    boneCount: bones.length,
    cubeCount: bones.reduce((a, b) => a + ((b.cubes || []).length), 0),
    uvCounts: bones.reduce((a, b) => { for (const c of (b.cubes || [])) a[uvKind(c)]++; return a; }, { box: 0, face: 0, none: 0 }),
    roots: [...roots].sort(),
    childrenOf: Object.fromEntries([...childrenOf].map(([k, v]) => [k, [...v].sort()])),
    fields, cubes, boxSummary, parentFirst
  };
}

function animModel(j) {
  const anims = j.animations || {};
  const out = {};
  for (const [name, a] of Object.entries(anims)) {
    const tracks = {};
    let sorted = true;
    for (const [bn, tr] of Object.entries(a.bones || {})) {
      for (const [track, keys] of Object.entries(tr)) {
        const times = Object.keys(keys).map(Number);
        const nums = times.slice().sort((x, y) => x - y);
        if (times.join(',') !== nums.join(',')) sorted = false;
        const kv = {};
        for (const k of Object.keys(keys)) kv[k] = CANON(keys[k]);
        tracks[bn + '.' + track] = { times: nums, values: kv };
      }
    }
    out[name] = {
      animation_length: a.animation_length, loop: a.loop,
      bones: Object.keys(a.bones || {}).sort(),
      boneCount: Object.keys(a.bones || {}).length,
      trackCount: Object.keys(tracks).length,
      keysSortedByTime: sorted,
      tracks,
      extraFields: Object.fromEntries(Object.keys(a).filter(k => !['animation_length', 'loop', 'bones'].includes(k)).map(k => [k, CANON(a[k])]))
    };
  }
  return out;
}

const before = JSON.parse(fs.readFileSync(pos[0], 'utf8'));
const after = JSON.parse(fs.readFileSync(pos[1], 'utf8'));
const diffs = [];
const notes = [];

/** 把两个值之间的差异定位成"人类可读的小条目", 而不是整段 JSON */
function describeDiff(a, b, path = '') {
  const out = [];
  const walk = (x, y, p) => {
    if (deepEqual(x, y)) return;
    const tx = x === null ? 'null' : Array.isArray(x) ? 'array' : typeof x;
    const ty = y === null ? 'null' : Array.isArray(y) ? 'array' : typeof y;
    if (tx !== ty) { out.push(`${p}: 类型 ${tx} -> ${ty}`); return; }
    if (tx === 'array') {
      if (x.length !== y.length) out.push(`${p}: 元素数 ${x.length} -> ${y.length}`);
      const n = Math.max(x.length, y.length);
      for (let i = 0; i < n; i++) walk(x[i], y[i], `${p}[${i}]`);
      return;
    }
    if (tx === 'object') {
      const keys = [...new Set([...Object.keys(x), ...Object.keys(y)])].sort();
      for (const k of keys) {
        const hasX = Object.prototype.hasOwnProperty.call(x, k);
        const hasY = Object.prototype.hasOwnProperty.call(y, k);
        if (!hasX) { out.push(`${p}.${k}: 新增 = ${short(y[k])}`); continue; }
        if (!hasY) { out.push(`${p}.${k}: 移除 (原 ${short(x[k])})`); continue; }
        walk(x[k], y[k], `${p}.${k}`);
      }
      return;
    }
    out.push(`${p}: ${short(x)} -> ${short(y)}`);
  };
  const short = v => JSON.stringify(v) === undefined ? String(v) : JSON.stringify(v).slice(0, 90);
  walk(a, b, path || '(根)');
  return out;
}

function cmp(label, a, b) {
  if (!deepEqual(a, b)) diffs.push({ label, before: a, after: b });
}

if (!isAnim) {
  const A = geoModel(before), B = geoModel(after);
  cmp('description (identifier/贴图尺寸/bounds)', A.description, B.description);
  cmp('骨骼数量', A.boneCount, B.boneCount);
  cmp('cube 总数', A.cubeCount, B.cubeCount);
  cmp('UV 写法计数 (box/逐面/缺)', A.uvCounts, B.uvCounts);
  cmp('根骨骼集合', A.roots, B.roots);
  cmp('父子层级(每骨骼的子骨骼集合)', A.childrenOf, B.childrenOf);
  cmp('每个骨骼的字段(parent/pivot/rotation/自定义)', A.fields, B.fields);
  cmp('每个骨骼的 cube **顺序与内容**', A.cubes, B.cubes);
  cmp('每个 cube 的包围盒数值', A.boxSummary, B.boxSummary);

  const orderChanged = A.boneNames.join('\u0000') !== B.boneNames.join('\u0000');
  if (orderChanged) {
    const movedFrom = [];
    const setB = new Set(B.boneNames);
    for (let i = 0; i < A.boneNames.length; i++) {
      if (A.boneNames[i] !== B.boneNames[i]) movedFrom.push(A.boneNames[i]);
    }
    notes.push(`骨骼数组顺序变化: 是 (${movedFrom.length} 个骨骼的索引位置变了)`);
    notes.push(`  位置索引有变的骨骼: ${movedFrom.join(', ')}`);
    notes.push(`  整理前顺序: ${A.boneNames.join(' → ')}`);
    notes.push(`  整理后顺序: ${B.boneNames.join(' → ')}`);
  } else {
    notes.push('骨骼数组顺序变化: 否');
  }
  notes.push(`整理前已是父级优先: ${A.parentFirst ? '是' : '否'}`);
  notes.push(`整理后已是父级优先: ${B.parentFirst ? '是' : '否'}`);

  // 对"骨骼顺序整理"这个用途给出明确结论
  const onlyOrder = diffs.length === 0;
  notes.push('');
  notes.push(onlyOrder
    ? '结论: **语义零差异** —— 除 bones 数组顺序外, 骨骼集合/字段/cube 顺序与内容/UV/包围盒/层级全部逐项相同。'
    : `结论: 存在**语义差异** ${diffs.length} 项 (见下) —— 这不是纯顺序整理, 必须逐项确认。`);
} else {
  const A = animModel(before), B = animModel(after);
  cmp('动画段集合', Object.keys(A).sort(), Object.keys(B).sort());
  for (const name of new Set([...Object.keys(A), ...Object.keys(B)])) {
    const a = A[name], b = B[name];
    if (!a || !b) continue;
    cmp(`[${name}] animation_length`, a.animation_length, b.animation_length);
    cmp(`[${name}] loop`, a.loop, b.loop);
    cmp(`[${name}] 骨骼集合`, a.bones, b.bones);
    cmp(`[${name}] 轨道数量`, a.trackCount, b.trackCount);
    cmp(`[${name}] 关键帧时间与取值`, a.tracks, b.tracks);
    cmp(`[${name}] 其他字段`, a.extraFields, b.extraFields);
  }
  for (const name of Object.keys(B)) if (!B[name].keysSortedByTime) notes.push(`⚠ [${name}] 存在**未按数值时间升序**的关键帧键 (渲染按数值排序读, 但建议整理)`);
  notes.push(diffs.length === 0 ? '结论: **语义零差异** (动画段/长度/循环/骨骼/关键帧时间与取值全部相同)。'
    : `结论: 存在**语义差异** ${diffs.length} 项 (见下)。`);
}

if (asJson) {
  console.log(JSON.stringify({ before: pos[0], after: pos[1], diffCount: diffs.length, diffs, notes }, null, 2));
} else {
  console.log(`对比: ${pos[0]}`);
  console.log(`  vs: ${pos[1]}`);
  console.log('');
  for (const n of notes) console.log(n);
  if (diffs.length) {
    console.log('\n---- 语义差异明细 ----');
    /* 每个差异只给"路径定位 + 具体条目", 不整段倾倒 JSON —— 不然 150 cube 的模型刷屏看不出重点 */
    for (const d of diffs) {
      console.log(`\n[差异] ${d.label}`);
      const detail = describeDiff(d.before, d.after);
      for (const line of detail.slice(0, 40)) console.log('  ' + line);
      if (detail.length > 40) console.log(`  ...(共 ${detail.length} 条, 省略 ${detail.length - 40} 条)`);
    }
  } else {
    console.log('\n语义差异明细: 无');
  }
}
process.exit(diffs.length ? 1 : 0);
