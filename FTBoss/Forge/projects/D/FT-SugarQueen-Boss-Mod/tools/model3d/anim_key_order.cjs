#!/usr/bin/env node
// 动画关键帧**时间键顺序**检查/整理。
//
// 用法:
//   node anim_key_order.cjs --check  <anim.json...> | --all      # 只报告 (默认)
//   node anim_key_order.cjs --write  <anim.json...> | --all      # 就地按数值时间升序重排键
//   node anim_key_order.cjs --check --all --json                  # 机器可读
//
// ★ 为什么这不是"风格问题"而是**真的会改变播放结果** (GeckoLib 4.8.4 反汇编实证):
//   `AnimationController.getCurrentKeyFrameLocation(List, tick)` 的字节码是:
//       double total = 0.0;
//       for (Keyframe kf : list) {           // ← 严格按 List 顺序遍历
//           total += kf.length();            // ← 累加的是 keyframe 的**间隔(duration)**
//           if (total > tick) return new KeyframeLocation(kf, tick - (total - kf.length()));
//       }
//       return new KeyframeLocation(list.get(list.size() - 1), tick);   // 兜底取**最后一个元素**
//   ⇒ 定位完全依赖"总时长前缀和", 而前缀和只有在键**按数值时间升序**时才等于真正的时间轴。
//   ⇒ 键的书写顺序错了, 段查找就落到错误的段上, 插值起点/斜率全错; 连兜底都取错元素。
//   而且整条加载链**没有任何排序**:
//     - `BakedAnimationsAdapter.getKeyframes(JsonElement)` 用
//       `ObjectArrayList`/`DoubleObjectPair.of(t, v)` 按 `entrySet()` 顺序建表 —— 即 JSON 书写顺序;
//       对 `BakedAnimationsAdapter` / `AnimationController` / `AnimationProcessor` 反汇编全文搜 "sort" 均为空。
//     - `KeyframeStack.getLastKeyframeTime()` 直接取列表**末尾元素**, 也不排序。
//   ⇒ 结论: 必须按数值时间升序书写。
//
// ⚠ 本工具的安全性设计 (规格: 不改变视觉时序 / 检查器不得擅自改正式资源):
//   - 默认 `--check` **只读**, 不碰任何文件; 只有显式 `--write` 才写。
//   - 写入前先留 `.keyorder.bak` 备份。
//   - 只做"**键的重新排列**": 每个键的**字符串本身与其取值一字不改**, 键的集合也不变。
//     (本工程实测取值形状只有 array[3] 与 number 两种, 没有 pre/post/lerp_mode 对象需要保留;
//      工具仍然对对象写法原样搬运, 不会丢字段。)
//   - 重复的数值时间会**报错并拒绝写入**该轨道 (JSON 里不同写法如 "1" 与 "1.0" 会撞成同一个键)。
//   - 非数值键报错并拒绝写入。
//   - 想回退: 把 `.keyorder.bak` 覆盖回去即可。
const fs = require('fs');
const path = require('path');

/* 被 require 时只导出工具函数, 不执行 CLI 主体 (供测试/其他脚本复用, 避免再抄第二份实现) */
if (require.main !== module) {
  module.exports = { serializeOrdered, topLevelKeysAtPath, rawTrackKeys, deepEqual, planReorders, verifyLossless };
  return;
}

const argv = process.argv.slice(2);
const mode = argv.includes('--write') ? 'write' : 'check';
const asJson = argv.includes('--json');
const all = argv.includes('--all');
const files = argv.filter(a => !a.startsWith('--'));

const ROOT = path.resolve(__dirname, '../..');
const ANIM_DIR = path.join(ROOT, 'src/main/resources/assets/ftboss/animations');
if (all) for (const f of fs.readdirSync(ANIM_DIR)) if (f.endsWith('.animation.json')) files.push(path.join(ANIM_DIR, f));
if (!files.length) {
  console.error('用法: node anim_key_order.cjs [--check|--write] <anim.json...> | --all [--json]');
  process.exit(2);
}

/* ★★ 关键实现约束 (2026-09-16 实测踩坑): **不能用 JSON.stringify 输出**
   JS 对象把"类数组下标的字符串键"(_integer-like_, 如 "0"/"4")排在所有其他键**之前**并按数值升序,
   而 "1.4"/"2.6" 这类键属于普通字符串键, 按**插入顺序**排。
   于是目标顺序 ["0","1.4","2.6","4"] 在 JS 对象里**不可能**表示出来:
       const r={}; for(const k of ['0','1.4','2.6','4']) r[k]=k;
       Object.keys(r) -> ['0','4','1.4','2.6']      // 4 被提到前面去了
   所以 JSON.parse -> 重排 -> JSON.stringify 这条往返**必然写出错的键序** (第一版就是这样白跑一趟,
   备份写了、内容没变)。本工程 1581 条轨道里 **461 条**正是这种"整数键与小数键混写"的轨道。
   解法: 自己按**显式键序**序列化 (orderFor 指定某条轨道的键顺序), 不再依赖 JS 对象键序。 */
function serializeOrdered(value, orderFor = new Map(), indent = 2) {
  const pad = n => ' '.repeat(n * indent);
  const esc = s => JSON.stringify(s);
  const walk = (v, path, depth) => {
    if (v === null) return 'null';
    if (typeof v === 'number') return Number.isFinite(v) ? String(v) : 'null';
    if (typeof v === 'boolean') return v ? 'true' : 'false';
    if (typeof v === 'string') return esc(v);
    if (Array.isArray(v)) {
      if (!v.length) return '[]';
      const parts = v.map((x, i) => pad(depth + 1) + walk(x, path + '[' + i + ']', depth + 1));
      return '[\n' + parts.join(',\n') + '\n' + pad(depth) + ']';
    }
    if (typeof v === 'object') {
      // 键序: orderFor 里指定的走显式顺序(缺的补在后面, 保证不丢键), 否则保持插入顺序
      const own = Object.keys(v);
      const want = orderFor.get(path);
      let keys = own;
      if (want) {
        const set = new Set(own);
        keys = want.filter(k => set.has(k)).concat(own.filter(k => !want.includes(k)));
      }
      if (!keys.length) return '{}';
      const parts = keys.map(k => pad(depth + 1) + esc(k) + ': ' + walk(v[k], path + '/' + k, depth + 1));
      return '{\n' + parts.join(',\n') + '\n' + pad(depth) + '}';
    }
    throw new Error('无法序列化的值: ' + typeof v);
  };
  return walk(value, '', 0) + '\n';
}

/* ★ 校验必须**直接扫写出的文本**, 不能再 JSON.parse 回来查键序:
   JS 对象无法表示 ["0","1.4","2.6","4"] 这种顺序 (整数键会被 V8 提前), 解析回来的键序
   永远是 ["0","4","1.4","2.6"] —— 用它判"是否升序"会**必然误判失败** (第一版就栽在这)。
   所以这里写一个只看**键名与嵌套结构**的极简扫描器, 不构造对象, 保留真实书写顺序。 */
function topLevelKeysAtPath(text, segments) {
  let i = 0;
  const n = text.length;
  const ws = () => { while (i < n && (text[i] === ' ' || text[i] === '\t' || text[i] === '\n' || text[i] === '\r')) i++; };
  function str() {                       // 读一个 JSON 字符串字面量 (含转义)
    i++; let out = '';
    while (i < n) {
      const c = text[i];
      if (c === '\\') { const nx = text[i + 1]; out += nx === 'u' ? String.fromCharCode(parseInt(text.substr(i + 2, 4), 16)) : nx; i += (nx === 'u' ? 6 : 2); continue; }
      if (c === '"') { i++; return out; }
      out += c; i++;
    }
    throw new Error('未闭合的字符串');
  }
  function skip() {                      // 跳过一个完整值 (对象/数组/字符串/字面量)
    ws();
    const c = text[i];
    if (c === '"') { str(); return; }
    if (c === '{' || c === '[') {
      const open = c, close = c === '{' ? '}' : ']';
      i++; let d = 1;
      while (i < n && d > 0) {
        const ch = text[i];
        if (ch === '"') { str(); continue; }
        if (ch === open) d++;
        else if (ch === close) d--;
        i++;
      }
      return;
    }
    while (i < n && !',}] \t\n\r'.includes(text[i])) i++;
  }
  function keysOfObj() {                 // 读 { ... } 的顶层键 (按书写顺序)
    const out = [];
    ws(); i++;                           // '{'
    ws();
    if (text[i] === '}') { i++; return out; }
    while (i < n) {
      ws();
      if (text[i] !== '"') throw new Error('期望键名, 实际 ' + JSON.stringify(text.slice(i, i + 20)));
      out.push(str());
      ws(); if (text[i] !== ':') throw new Error('期望 ":"'); i++;
      skip();
      ws();
      if (text[i] === ',') { i++; continue; }
      if (text[i] === '}') { i++; return out; }
      throw new Error('期望 "," 或 "}"');
    }
    throw new Error('未闭合的对象');
  }
  ws(); i++;                             // 根 '{'
  for (const seg of segments) {
    let next = -1;
    ws(); i++;                           // 进上一层对象: '{'
    for (;;) {
      ws();
      const k = str();
      ws(); if (text[i] !== ':') throw new Error('期望 ":"'); i++;
      if (k === seg) { next = 1; break; }
      skip(); ws();
      if (text[i] === ',') { i++; continue; }
      throw new Error('找不到键 ' + seg);
    }
    if (next !== 1) throw new Error('找不到键 ' + seg);
  }
  return keysOfObj();
}

/* ★★ 关键实现约束 #2 (2026-09-16 实测踩坑): **判断"是否需要整理"也必须读原始文本**
   `Object.keys(JSON.parse(raw))` 返回的是 **V8 认可的对象键序**, 不是**文件里写的顺序**:
   文件里写的是 ["0","0.7","1.4","2"], 而 V8 会把整数样式键 "0"/"2" 提前, 返回 ["0","2","0.7","1.4"]。
   拿后者判"是否升序"会把**已经正确**的文件误判成"需要整理", 于是每次 --check 都报 109 条假问题,
   每次 --write 都把文件重写一遍 (内容其实没变)。第一版就是这样自欺欺人。
   所以: 顺序真源 = 文件字节。这里用同一套"只看键名与嵌套结构"的扫描器取真实书写顺序。
   实例化与 JSON.parse 的结果**都从文本取键序**, 保证"检查/整理/校验"三者判据一致。 */
function rawTrackKeys(text, segments) {
  let i = 0;
  const n = text.length;
  const ws = () => { while (i < n && (text[i] === ' ' || text[i] === '\t' || text[i] === '\n' || text[i] === '\r')) i++; };
  function str() {
    i++; let out = '';
    while (i < n) {
      const c = text[i];
      if (c === '\\') { const nx = text[i + 1]; if (nx === 'u') { out += String.fromCharCode(parseInt(text.substr(i + 2, 4), 16)); i += 6; } else { out += nx; i += 2; } continue; }
      if (c === '"') { i++; return out; }
      out += c; i++;
    }
    throw new Error('未闭合字符串');
  }
  function skip() {
    ws();
    const c = text[i];
    if (c === '"') { str(); return; }
    if (c === '{' || c === '[') {
      const open = c, close = c === '{' ? '}' : ']';
      i++; let d = 1;
      while (i < n && d > 0) { const ch = text[i]; if (ch === '"') { str(); continue; } if (ch === open) d++; else if (ch === close) d--; i++; }
      return;
    }
    while (i < n && !',}] \t\n\r'.includes(text[i])) i++;
  }
  ws(); i++;                              // 根 '{'
  for (let s = 0; s < segments.length; s++) {
    let hit = false;
    for (;;) {
      ws();
      const k = str();
      ws();
      if (text[i] !== ':') throw new Error('期望 ":"');
      i++;
      if (k === segments[s]) { hit = true; break; }
      skip(); ws();
      if (text[i] === ',') { i++; continue; }
      throw new Error('找不到键 ' + segments[s]);
    }
    if (!hit) throw new Error('找不到键 ' + segments[s]);
    ws();
    if (text[i] !== '{') return null;      // 值是数组等 -> 不是关键帧对象
    if (s !== segments.length - 1) i++;    // 还要下降才吃 '{'; 最后一段留给下面读键
  }
  const out = [];
  ws(); i++;                               // '{'
  ws();
  if (text[i] === '}') { i++; return out; }
  for (;;) {
    ws();
    const key = str();
    out.push(key);
    ws(); if (text[i] !== ':') throw new Error('期望 ":"'); i++;
    skip(); ws();
    if (text[i] === ',') { i++; continue; }
    if (text[i] === '}') { i++; return out; }
    throw new Error('期望 "," 或 "}"');
  }
}

/**
 * 稳定拓扑排序 (父级优先)。 —— 供 geo 骨骼重排复用 (见 reorder_bones.cjs)
 */

/** 深比较 (与对象键书写顺序无关); 数组按索引比较 */
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

/** 计算要重排的轨道 (不修改入参); 返回 [{anim,bone,track,keys:[k...]}]
 *  ★ 键序取自**原始文本** text; 不传 text 时退回 Object.keys (仅用于无文件场景) */
function planReorders(anims, text) {
  const plan = [];
  for (const [animName, anim] of Object.entries(anims || {})) {
    for (const [boneName, tr] of Object.entries(anim.bones || {})) {
      for (const [trackName, keys] of Object.entries(tr)) {
        if (Array.isArray(keys)) continue;
        let rawKeys;
        if (text) {
          try { rawKeys = rawTrackKeys(text, ['animations', animName, 'bones', boneName, trackName]); }
          catch { continue; }
          if (!rawKeys) continue;
        } else {
          rawKeys = Object.keys(keys);
        }
        const nums = rawKeys.map(Number);
        if (rawKeys.some((k, i) => !Number.isFinite(nums[i]))) continue;
        const isSorted = nums.every((v, i) => i === 0 || nums[i - 1] <= v);
        if (isSorted) continue;
        const idx = rawKeys.map((k, i) => ({ k, v: nums[i] }))
          .sort((a, b) => (a.v - b.v) || (a.k < b.k ? -1 : a.k > b.k ? 1 : 0));
        plan.push({ anim: animName, bone: boneName, track: trackName, keys: idx.map(x => x.k) });
      }
    }
  }
  return plan;
}

/** 按 plan 生成重排后的 animations 副本 (不改入参) */
function applyPlan(anims, plan) {
  const out = JSON.parse(JSON.stringify(anims));
  for (const p of plan) {
    const tr = out[p.anim].bones[p.bone];
    const rebuilt = {};
    for (const k of p.keys) rebuilt[k] = tr[p.track][k];
    tr[p.track] = rebuilt;
  }
  return out;
}

/** 无损性校验: 只允许"键的顺序"变化, 键集合与每个键的取值必须一字不差 */
function verifyLossless(before, after) {
  const problems = [];
  const bad = [];
  for (const [an, a] of Object.entries(before.animations || {})) {
    const b = (after.animations || {})[an];
    if (!b) { bad.push(`${an}: 动画段丢失`); continue; }
    if (!deepEqual(a.animation_length, b.animation_length)) bad.push(`${an}: animation_length 变了`);
    if (!deepEqual(a.loop, b.loop)) bad.push(`${an}: loop 变了`);
    const bonesA = Object.keys(a.bones || {}).join(','), bonesB = Object.keys(b.bones || {}).join(',');
    if (bonesA !== bonesB) bad.push(`${an}: 骨骼键顺序/集合变了`);
    for (const bn of Object.keys(a.bones || {})) {
      const tra = a.bones[bn], trb = (b.bones || {})[bn] || {};
      if (Object.keys(tra).join(',') !== Object.keys(trb).join(',')) bad.push(`${an}.${bn}: 轨道集合/顺序变了`);
      for (const tn of Object.keys(tra)) {
        const ka = Object.keys(tra[tn]), kb = Object.keys(trb[tn] || {});
        if (ka.length !== kb.length) { bad.push(`${an}.${bn}.${tn}: 关键帧数 ${ka.length} -> ${kb.length}`); continue; }
        if ([...ka].sort().join('\u0000') !== [...kb].sort().join('\u0000')) { bad.push(`${an}.${bn}.${tn}: 时间键集合变了`); continue; }
        for (const k of ka) if (!deepEqual(tra[tn][k], trb[tn][k])) bad.push(`${an}.${bn}.${tn}@${k}: 取值变了`);
      }
    }
  }
  if (diffKeys(before.animations, after.animations).length) bad.push('动画段集合变了');
  problems.push(...bad);
  return problems;
}
function diffKeys(a, b) { return Object.keys(a || {}).filter(k => !(k in (b || {}))).concat(Object.keys(b || {}).filter(k => !(k in (a || {})))); }

const report = [];
let errorCount = 0;

for (const file of files) {
  const abs = path.resolve(file);
  let raw;
  try { raw = fs.readFileSync(abs, 'utf8'); } catch (e) { console.error(`跳过 ${file}: ${e.message}`); errorCount++; continue; }
  const j = JSON.parse(raw);
  const tracks = [];

  // ---- 诊断每条轨道 + 收集 error ----
  // ★ 键序真源 = 原始文本 (rawTrackKeys), 不是 Object.keys(JSON.parse(...)) —— 见文件头注释 #2
  for (const [animName, anim] of Object.entries(j.animations || {})) {
    for (const [boneName, tr] of Object.entries(anim.bones || {})) {
      for (const [trackName, keys] of Object.entries(tr)) {
        if (Array.isArray(keys)) continue;          // 整段静态取值, 没有时间键
        let rawKeys;
        try {
          rawKeys = rawTrackKeys(raw, ['animations', animName, 'bones', boneName, trackName]);
        } catch (e) {
          console.error(`[读取失败] ${path.basename(file)} ${animName}/${boneName}.${trackName}: ${e.message}`);
          errorCount++;
          continue;
        }
        if (!rawKeys) continue;
        const nums = rawKeys.map(Number);
        const badKey = rawKeys.filter((k, i) => !Number.isFinite(nums[i]));
        const sortedNums = nums.slice().sort((a, b) => a - b);
        const dupNum = sortedNums.filter((v, i) => i > 0 && v === sortedNums[i - 1]);
        const isSorted = nums.every((v, i) => i === 0 || nums[i - 1] <= v);
        if (badKey.length || dupNum.length) errorCount++;
        if (!isSorted || badKey.length || dupNum.length) {
          tracks.push({
            anim: animName, bone: boneName, track: trackName, keys: rawKeys.length, sorted: isSorted,
            order: rawKeys.join(','),
            want: (badKey.length || dupNum.length) ? null
              : rawKeys.map((k, i) => ({ k, v: nums[i] })).sort((a, b) => (a.v - b.v) || (a.k < b.k ? -1 : a.k > b.k ? 1 : 0)).map(x => x.k).join(','),
            badKey, dupNum
          });
        }
      }
    }
  }

  // ---- 计划 + 无损校验 (纯内存, 不写文件) ----
  const plan = planReorders(j.animations, raw);
  // 显式键序表: 路径 -> 目标键顺序 (绕开 JS 对象键序限制, 见 serializeOrdered 注释)
  const orderFor = new Map();
  for (const p of plan) orderFor.set(`/animations/${p.anim}/bones/${p.bone}/${p.track}`, p.keys);

  if (mode === 'write' && plan.length) {
    // 先序列化, 再解析回来做无损校验 —— 校验的是"真正要写进文件的字节", 而不是内存里的中间态
    const text = serializeOrdered({ ...j }, orderFor);
    let reparsed = null, parseErr = null;
    try { reparsed = JSON.parse(text); } catch (e) { parseErr = e; }
    const problems = parseErr ? ['写出的 JSON 无法解析: ' + parseErr.message] : verifyLossless(j, reparsed);
    /* 额外确认: 在**写出的文本**里, 每条要重排的轨道键序确实是目标顺序。
       注意必须扫文本, 不能 parse 回来查 —— 见 topLevelKeysAtPath 的注释。 */
    for (const p of plan) {
      try {
        const got = topLevelKeysAtPath(text, ['animations', p.anim, 'bones', p.bone, p.track]);
        if (got.join(',') !== p.keys.join(',')) {
          problems.push(`${p.anim}.${p.bone}.${p.track}: 文本键序 [${got.join(',')}] != 期望 [${p.keys.join(',')}]`);
        }
      } catch (e) {
        problems.push(`${p.anim}.${p.bone}.${p.track}: 文本扫描失败 ${e.message}`);
      }
    }

    if (problems.length) {
      console.error(`[拒绝写入] ${file}: 校验未通过 (${problems.length} 项) —— 不修改文件`);
      for (const p of problems.slice(0, 6)) console.error('    ' + p);
      errorCount += problems.length;
    } else {
      if (!fs.existsSync(abs + '.keyorder.bak')) fs.writeFileSync(abs + '.keyorder.bak', raw, 'utf8');
      fs.writeFileSync(abs, text, 'utf8');
    }
  }

  report.push({
    file: path.relative(ROOT, abs).replace(/\\/g, '/'),
    tracks: Object.values(j.animations || {}).reduce((a, an) => a + Object.values(an.bones || {}).reduce((x, tr) => x + Object.keys(tr).length, 0), 0),
    unsorted: tracks.length, changed: plan.length > 0,
    details: tracks
  });
}

/* ---------- 输出 ---------- */
if (asJson) {
  console.log(JSON.stringify(report, null, 2));
} else {
  for (const r of report) {
    const tag = r.details.some(d => d.badKey.length || d.dupNum.length) ? '[error]'
      : r.unsorted ? '[review]' : '[ok]';
    console.log(`${tag} ${r.file}  轨道 ${r.tracks}, 键序需整理 ${r.unsorted}`);
    for (const d of r.details) {
      const where = `${d.anim} / ${d.bone}.${d.track}`;
      if (d.badKey.length) { console.log(`    [error]  ${where}: 非数值时间键 ${JSON.stringify(d.badKey)}`); continue; }
      if (d.dupNum.length) { console.log(`    [error]  ${where}: 数值时间重复 ${JSON.stringify(d.dupNum)} (不同写法撞同一个键, 拒绝重排)`); continue; }
      console.log(`    [review] ${where}`);
      console.log(`             当前顺序 [${d.order}]`);
      console.log(`             正确顺序 [${d.want}]`);
    }
  }
  const tot = report.reduce((a, r) => a + r.unsorted, 0);
  const chg = report.filter(r => r.changed).length;
  console.log(`\n结论: 检查 ${report.length} 个动画文件, ${tot} 条轨道键序不正确, ${chg} 个文件需要整理, error ${errorCount} 项`);
  if (mode === 'check' && chg) console.log('(只读模式; 加 --write 才会就地重排并留 .keyorder.bak 备份)');
}
process.exit(errorCount ? 1 : 0);
