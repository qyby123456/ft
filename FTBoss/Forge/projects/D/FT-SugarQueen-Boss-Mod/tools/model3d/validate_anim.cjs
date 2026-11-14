#!/usr/bin/env node
// 动画校验: 动画文件与 geo 的一致性 + 关键帧时间键序 + 是否真的被代码引用
//
// 用法:
//   node validate_anim.cjs <geo.json> <animation.json> [java源码目录]
//   node validate_anim.cjs --all [--json]        # 批量: 每个 geo 配同名动画, 代码目录自动取 src/main/java
//
// 输出分类统一 [error] / [warning] / [review]; 退出码: 有 error 时非 0。
//
// 检查项:
//   1) 每个动画引用的骨骼名都存在于 geo (含父级链)
//   2) ★ 关键帧时间键**必须按数值升序书写** —— 见下面"为什么这是 error"
//   3) keyframe 时间不超过 animation_length
//   4) 关键帧数值都是 3 个有限数字
//   5) 重复的数值时间键 (如 "1" 与 "1.0" 撞键)
//   6) loop=true 的动画在 length 处要有键 (否则循环会跳)
//   7) 给出 java 源码目录时: 逐个动画名搜代码, 报出**从未被引用**的动画 (死动画)
//   8) 骨骼在 geo 里存在但所有动画都没碰过 -> review (可能是挂点, 也可能是忘接线)
//
// ★★ 为什么"关键帧键序"是 error 而不是风格问题 (2026-09-16 反汇编 GeckoLib 4.8.4 实证):
//   `AnimationController.getCurrentKeyFrameLocation(List, tick)` 字节码:
//       double total = 0.0;
//       for (Keyframe kf : list) { total += kf.length();          // 累加"间隔"
//           if (total > tick) return new KeyframeLocation(kf, tick - (total - kf.length())); }
//       return new KeyframeLocation(list.get(list.size() - 1), tick);
//   ⇒ 定位依赖"总时长前缀和", 只有键**按数值时间升序**时前缀和才等于真正的时间轴;
//     键序错了就落到错误的段, 插值起点/斜率全错, 连兜底都取错元素。
//   加载链全程**无排序**: BakedAnimationsAdapter 用 ObjectArrayList 按 entrySet 顺序建表
//   (= JSON 书写顺序), 对 BakedAnimationsAdapter / AnimationController / AnimationProcessor
//   反汇编全文搜 "sort" 均为空; KeyframeStack.getLastKeyframeTime() 也直接取末尾元素。
const fs = require('fs');
const path = require('path');

const argv = process.argv.slice(2);
const asJson = argv.includes('--json');
const useAll = argv.includes('--all');
const ROOT = path.resolve(__dirname, '../..');
const GEO_DIR = path.join(ROOT, 'src/main/resources/assets/ftboss/geo');
const ANIM_DIR = path.join(ROOT, 'src/main/resources/assets/ftboss/animations');
const DEFAULT_SRC = path.join(ROOT, 'src/main/java');
/* 时间键的书写顺序只能从原始文本读 (见下面用到处的注释)。
   直接复用 anim_key_order.cjs 里的扫描器, 保证全工程只有一份判据。 */
const { rawTrackKeys } = require('./anim_key_order.cjs');

let pairs = [];   // [{geo, anim, srcDir}]
if (useAll) {
  const geoFiles = fs.readdirSync(GEO_DIR).filter(f => f.endsWith('.geo.json')).sort();
  const geoNames = geoFiles.map(f => f.replace(/\.geo\.json$/, ''));
  const animFiles = fs.readdirSync(ANIM_DIR).filter(f => f.endsWith('.animation.json'));
  for (const f of geoFiles) {
    const base = f.replace(/\.geo\.json$/, '');
    const own = path.join(ANIM_DIR, base + '.animation.json');
    if (fs.existsSync(own)) { pairs.push({ geo: path.join(GEO_DIR, f), anim: own, srcDir: DEFAULT_SRC }); continue; }
    /* 没有同名动画: 未必是问题 —— 多个模型可以**共用**一份动画
       (本工程实测: big_rat / small_rat 共用 rat.animation.json;
        compost_superman_p2 是 compost_superman 的重复 geo)。
       这里用"骨骼名重合度"找候选, 只作为 review 提示, 不判错。 */
    const geo = JSON.parse(fs.readFileSync(path.join(GEO_DIR, f), 'utf8'))['minecraft:geometry'][0];
    const bones = new Set((geo.bones || []).map(b => b.name));
    let best = null;
    for (const af of animFiles) {
      const anims = JSON.parse(fs.readFileSync(path.join(ANIM_DIR, af), 'utf8')).animations || {};
      const used = new Set();
      for (const a of Object.values(anims)) for (const bn of Object.keys(a.bones || {})) used.add(bn);
      let hit = 0;
      for (const n of used) if (bones.has(n)) hit++;
      const cov = used.size ? hit / used.size : 0;
      if (!best || cov > best.cov) best = { file: af, cov, used: used.size, hit };
    }
    pairs.push({
      geo: path.join(GEO_DIR, f), anim: null, srcDir: DEFAULT_SRC,
      shared: best && best.cov >= 0.8 ? { file: path.join(ANIM_DIR, best.file), cov: best.cov, hit: best.hit, used: best.used } : null,
      otherGeos: geoNames.filter(n => n !== base)
    });
  }
} else {
  const pos = argv.filter(a => !a.startsWith('--'));
  if (pos.length < 2) { console.error('用法: node validate_anim.cjs <geo.json> <animation.json> [java源码目录] | --all [--json]'); process.exit(2); }
  pairs.push({ geo: pos[0], anim: pos[1], srcDir: pos[2] || null });
}

/** 收集源码里出现的所有字符串字面量(粗略但足够) */
function loadSrc(srcDir) {
  if (!srcDir || !fs.existsSync(srcDir)) return null;
  const files = [];
  (function walk(d) {
    for (const e of fs.readdirSync(d, { withFileTypes: true })) {
      const p = path.join(d, e.name);
      if (e.isDirectory()) walk(p); else if (e.name.endsWith('.java')) files.push(p);
    }
  })(srcDir);
  return files.map(f => fs.readFileSync(f, 'utf8')).join('\n');
}

const reports = [];
let anyError = false;

for (const pair of pairs) {
  const rep = { geo: path.relative(ROOT, pair.geo).replace(/\\/g, '/'), anim: pair.anim ? path.relative(ROOT, pair.anim).replace(/\\/g, '/') : null, anims: [], error: [], warning: [], review: [] };
  if (!pair.anim) {
    /* 没有同名动画: 不是错误。能高比例匹配上别的动画 -> 判为"共用"; 否则标 review 而不是 error。
       (规格: 检查未覆盖的内容必须写为"未检查", 不能视作通过。) */
    if (pair.shared) {
      rep.review.push(`没有同名动画文件; 骨骼与 ${path.basename(pair.shared.file)} 的重合度 ${(pair.shared.cov * 100).toFixed(0)}%` +
        ` (${pair.shared.hit}/${pair.shared.used} 骨骼) —— 判定为**共用该动画**, 本组按"已检查(共用)"处理`);
      // 用共用的动画做一次真正的一致性检查, 而不是跳过
      pair.anim = pair.shared.file;
      rep.anim = path.relative(ROOT, pair.anim).replace(/\\/g, '/');
      rep.sharedFrom = rep.anim;
    } else {
      rep.review.push('没有同名动画文件, 且找不到骨骼重合度 ≥80% 的动画 —— **该模型的动画未检查** (不代表通过)');
      reports.push(rep);
      continue;
    }
  }

  const geo = JSON.parse(fs.readFileSync(pair.geo, 'utf8'))['minecraft:geometry'][0];
  const boneNames = new Set((geo.bones || []).map(b => b.name));
  const animRaw = fs.readFileSync(pair.anim, 'utf8');
  const anims = JSON.parse(animRaw).animations || {};
  const srcText = loadSrc(pair.srcDir);

  const animatedBones = new Set();

  for (const [name, a] of Object.entries(anims)) {
    const msgs = { error: [], warning: [], review: [] };
    const len = a.animation_length;
    const holdLast = a.loop === 'hold_on_last_frame';
    const loop = a.loop === true || a.loop === 'true' || a.loop === 1;
    if (typeof len !== 'number' || !(len > 0)) msgs.error.push(`animation_length 非法: ${len}`);
    if (a.loop === undefined) msgs.warning.push('缺少 loop 字段');
    const bones = a.bones || {};
    if (Object.keys(bones).length === 0) msgs.warning.push('没有任何骨骼关键帧');

    for (const [bn, tracks] of Object.entries(bones)) {
      animatedBones.add(bn);
      if (!boneNames.has(bn)) msgs.error.push(`骨骼 ${bn} 不在 geo 中 (会静默失效)`);
      for (const [track, keys] of Object.entries(tracks)) {
        if (!['rotation', 'position', 'scale'].includes(track)) { msgs.error.push(`未知轨道 ${bn}.${track}`); continue; }
        // ★ 整段静态取值: "rotation": [0,0,0] —— 合法, 没有时间键
        if (Array.isArray(keys)) {
          if (keys.length !== 3 || keys.some(n => typeof n !== 'number' || !isFinite(n))) {
            msgs.error.push(`${bn}.${track} 静态取值非法: ${JSON.stringify(keys)}`);
          } else {
            msgs.review.push(`${bn}.${track} 是**整段静态取值** ${JSON.stringify(keys)} (没有关键帧键序可谈)`);
          }
          continue;
        }
        if (typeof keys !== 'object' || keys === null) { msgs.error.push(`${bn}.${track} 取值类型异常`); continue; }
        /* ★ 时间键的**书写顺序**必须取自原始文本, 不能用 Object.keys(JSON.parse(...)):
           JS 会把整数样式的键 ("0"/"2") 提前并按数值升序, 而 "0.7"/"1.4" 属普通字符串键按插入顺序,
           于是文件里写的 ["0","0.7","1.4","2"] 会被读成 ["0","2","0.7","1.4"] ——
           拿它判"是否升序"会把**已经正确**的文件全部误判成 error (实测误报 119 条)。
           扫描器与 anim_key_order.cjs / anim_key_order_scan.cjs 共用同一实现, 避免多份判据漂移。 */
        let rawKeys = null;
        try { rawKeys = rawTrackKeys(animRaw, ['animations', name, 'bones', bn, track]); }
        catch { rawKeys = null; }
        if (!rawKeys) { msgs.error.push(`${bn}.${track} 无法从文本读出时间键 (见 anim_key_order.cjs 的扫描器)`); continue; }
        if (!rawKeys.length) { msgs.error.push(`${bn}.${track} 无关键帧`); continue; }
        const entries = rawKeys.map(k => [Number(k), k, keys[k]]);
        if (entries.some(([t]) => !Number.isFinite(t))) msgs.error.push(`${bn}.${track} 有非数值时间键: ${JSON.stringify(rawKeys.filter(k => !Number.isFinite(Number(k))))}`);

        // ★★ 核心检查: 时间键必须按数值升序
        const nums = entries.map(([t]) => t);
        const badPairs = [];
        for (let i = 1; i < nums.length; i++) if (nums[i] < nums[i - 1]) badPairs.push(`${nums[i - 1]} > ${nums[i]}`);
        if (badPairs.length) {
          const want = rawKeys.map(k => [Number(k), k]).sort((x, y) => x[0] - y[0]).map(x => x[1]);
          msgs.error.push(`${bn}.${track} 时间键**未按数值升序** —— GeckoLib 会定位到错误的插值段。` +
            `当前 [${rawKeys.join(',')}] 应为 [${want.join(',')}] (逆序处: ${badPairs.join('; ')})`);
        }
        // 撞键
        const seen = new Map();
        for (const t of nums) { if (seen.has(t)) msgs.error.push(`${bn}.${track} 数值时间重复 ${t} (不同写法撞同一个键)`); seen.set(t, 1); }

        const sorted = entries.slice().sort((x, y) => x[0] - y[0]);
        for (const [t, k, v] of entries) {
          if (!Array.isArray(v) || v.length !== 3 || v.some(n => typeof n !== 'number' || !isFinite(n))) {
            msgs.error.push(`${bn}.${track}@${k} 数值非法: ${JSON.stringify(v)}`);
          }
        }
        const last = sorted.length ? sorted[sorted.length - 1][0] : 0;
        if (typeof len === 'number' && last > len + 1e-6) msgs.error.push(`${bn}.${track} 关键帧 ${last} 超过动画长度 ${len}`);
        if (loop && !holdLast && typeof len === 'number' && last < len - 1e-6) msgs.warning.push(`${bn}.${track} 循环动画末尾 ${last} < ${len} (循环会跳)`);
        if (track === 'scale') {
          /* 注意: scale 的某个分量为 0 (整组 [0,0,0]) 在本工程是**故意的"隐藏"手法** ——
             暗影剑的 charge_orb / *_glow 用 [0,0,0] 把充能球与光效种子块收起来,
             apple_boss 的 lid/orbit 同理。所以这**不是错误**, 只记为 review 说明用途。 */
          const zeros = entries.filter(([, , v]) => Array.isArray(v) && v.every(n => n === 0));
          const negs = entries.filter(([, , v]) => Array.isArray(v) && v.some(n => n < 0));
          if (zeros.length) msgs.review.push(`${bn}.scale 有 ${zeros.length} 个关键帧为 [0,0,0] (按本工程惯例=隐藏该部件, 非错误)`);
          if (negs.length) msgs.error.push(`${bn}.scale 有**负**缩放 ${JSON.stringify(negs[0][2])} (负缩放会翻转面朝向, 确认是否有意)`);
        }
        if (track === 'position') for (const [t, k, v] of entries) {
          if (Array.isArray(v) && v.some(n => Math.abs(n) > 24)) msgs.warning.push(`${bn}.position@${k} 偏移过大 ${JSON.stringify(v)} (>1.5格; position 单位是 1/16 格)`);
        }
      }
    }

    // 代码引用检查
    let used = null;
    if (srcText) used = srcText.includes(`"${name}"`);
    if (srcText && !used) {
      msgs.review.push('该动画名在 java 源码中找不到 —— 实体控制器没接 (铁魔法内部按名调用的动画请忽略此项)');
    }
    if (msgs.error.length) anyError = true;
    rep.error.push(...msgs.error.map(m => `[${name}] ${m}`));
    rep.warning.push(...msgs.warning.map(m => `[${name}] ${m}`));
    rep.review.push(...msgs.review.map(m => `[${name}] ${m}`));
    rep.anims.push({ name, length: len, loop: a.loop, boneCount: Object.keys(bones).length, trackCount: Object.values(bones).reduce((n, tr) => n + Object.keys(tr).length, 0), codeReferenced: used, error: msgs.error, warning: msgs.warning, review: msgs.review });
  }

  // geo 里从未被任何动画碰过的骨骼 -> review (不是错误: 可能是挂点)
  const untouched = [...boneNames].filter(n => !animatedBones.has(n));
  if (Object.keys(anims).length && untouched.length) {
    rep.review.push(`geo 中从未被动画触碰的骨骼 (可能是挂点, 也可能是忘接线): ${untouched.join(', ')}`);
  }
  reports.push(rep);
}

if (asJson) {
  console.log(JSON.stringify(reports, null, 2));
} else {
  let e = 0, w = 0, r = 0;
  for (const rep of reports) {
    console.log(`\n=== ${rep.geo}  +  ${rep.anim || '(缺动画)'} ===`);
    for (const a of rep.anims) {
      const tag = a.error.length ? 'ERR ' : a.warning.length ? 'WARN' : 'OK  ';
      console.log(`  ${tag} ${a.name.padEnd(34)} len=${a.length}s loop=${a.loop} 骨骼=${a.boneCount} 轨道=${a.trackCount}` +
        (a.codeReferenced === null ? '' : `  代码引用=${a.codeReferenced ? '是' : '否'}`));
    }
    for (const m of rep.error) { console.log('  [error]   ' + m); e++; }
    for (const m of rep.warning) { console.log('  [warning] ' + m); w++; }
    for (const m of rep.review) { console.log('  [review]  ' + m); r++; }
  }
  console.log(`\n结论: 检查 ${reports.length} 组 —— error ${e} / warning ${w} / review ${r}`);
}
process.exit(anyError ? 1 : 0);
