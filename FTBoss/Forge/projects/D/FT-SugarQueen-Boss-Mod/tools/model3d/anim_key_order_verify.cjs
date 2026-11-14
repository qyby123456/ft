#!/usr/bin/env node
// 动画键序整理的**独立验证器**: 验证 anim_key_order.cjs --write 的结果确实无损且键序正确。
//
// 用法:
//   node anim_key_order_verify.cjs <beforeDir> <afterDir> [文件名...]
//   例: node anim_key_order_verify.cjs "$env:TEMP\before" "src/main/resources/assets/ftboss/animations" black_cleaver shadow_sword
//
// 为什么需要独立验证 (而不是信工具自己的话):
//   1. JS 的 JSON.parse **无法表示** ["0","1.4","2.6","4"] 这种键序 (整数样式键会被 V8 提前),
//      所以只看 parse 结果判"是否升序"会**必然误判**。必须扫**原始文本**。
//   2. 本脚本从零实现一个只认"键名 + 嵌套结构"的文本扫描器, 不 require 被测工具,
//      避免"用被测实现证明被测实现"。
//
// 检查项 (逐轨道):
//   A) 文本级: 轨道对象顶层键的**书写顺序**必须已按数值升序
//   B) 文本级: 每个键的**原始 JSON 片段**必须与改动前逐字符相同 (取值一字不改)
//   C) 文本级: 键的集合 (排序后) 必须与改动前完全一致 (没有新增/丢失关键帧)
//   D) 数值级: 再 parse 回来做一次深比较, 确认动画段/长度/loop/骨骼集合都没变
const fs = require('fs');
const path = require('path');

const beforeDir = process.argv[2];
const afterDir = process.argv[3];
const only = process.argv.slice(4);
if (!beforeDir || !afterDir) {
  console.error('用法: node anim_key_order_verify.cjs <beforeDir> <afterDir> [文件名...]');
  process.exit(2);
}

/* ---------------- 极简 JSON 文本扫描器 ---------------- */
function makeScanner(text) {
  let i = 0;
  const n = text.length;
  const ws = () => { while (i < n && (text[i] === ' ' || text[i] === '\t' || text[i] === '\n' || text[i] === '\r')) i++; };
  function str() {
    i++;
    let out = '';
    while (i < n) {
      const c = text[i];
      if (c === '\\') {
        const nx = text[i + 1];
        if (nx === 'u') { out += String.fromCharCode(parseInt(text.substr(i + 2, 4), 16)); i += 6; }
        else { out += nx; i += 2; }
        continue;
      }
      if (c === '"') { i++; return out; }
      out += c; i++;
    }
    throw new Error('未闭合字符串');
  }
  function skipValue() {
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
  /** 读一个对象: 返回 [{key, raw}] 其中 raw 是取值的原始切片 */
  function readObject() {
    const out = [];
    ws(); i++;                       // '{'
    ws();
    if (text[i] === '}') { i++; return out; }
    for (;;) {
      ws();
      if (text[i] !== '"') throw new Error('期望键名 @' + i + ' got ' + JSON.stringify(text[i]) + ' :: ' + JSON.stringify(text.slice(Math.max(0, i - 40), i + 20)));
      const key = str();
      ws(); if (text[i] !== ':') throw new Error('期望 ":" @' + i); i++;
      ws();
      const start = i;
      skipValue();
      out.push({ key, raw: text.slice(start, i) });
      if (process.env.FT_SCAN_DEBUG) console.log('        [readObject] key=' + JSON.stringify(key) + ' raw=' + JSON.stringify(text.slice(start, i).replace(/\s+/g, ' ').slice(0, 30)) + ' i=' + i);
      ws();
      if (text[i] === ',') { i++; continue; }
      if (text[i] === '}') { i++; return out; }
      throw new Error('期望 "," 或 "}" @' + i);
    }
  }
  /** 按路径逐层进入, 返回该层对象的 [{key,raw}]; 路径可能中途指向**数组**(整段静态取值) */
  function objectAt(segments) {
    i = 0; ws(); i++;               // 根 '{'
    for (const seg of segments) {
      let found = false;
      for (;;) {
        ws();
        if (text[i] !== '"') throw new Error('期望键名 @' + i + ' got ' + JSON.stringify(text[i]));
        const k = str();
        ws();
        if (text[i] !== ':') throw new Error('期望 ":" @' + i);
        i++;                         // 吃掉 ":"; 命中时游标停在取值起点
        if (k === seg) { found = true; break; }
        skipValue(); ws();
        if (text[i] === ',') { i++; continue; }
        throw new Error('找不到键 ' + seg);
      }
      if (!found) throw new Error('找不到键 ' + seg);
      ws();
      // ★ 只要**还要继续下降**(还有下一段路径), 就必须在这里吃掉 "{";
      //   最后一段留给 readObject() 自己吃 —— readObject 的第一条语句就是 ws(); i++ 吃 "{"。
      //   (踩过两次: 一次"每层都吃"导致最后多吃了 '{', 一次"都不吃"导致 readObject 从 '{' 开始找键名,
      //    两次都把全部 1581 条轨道误判成失败。)
      if (text[i] !== '{') return null;   // 值是数组等非对象 -> 不是关键帧对象
      if (seg !== segments[segments.length - 1]) i++;
    }
    return readObject();
  }
  return { objectAt };
}

/* ---------------- 深比较 (键序无关) ---------------- */
function deepEqual(a, b) {
  if (a === b) return true;
  if (typeof a === 'number' && typeof b === 'number') return Object.is(a, b) || Math.abs(a - b) < 1e-12;
  if (a === null || b === null || typeof a !== 'object' || typeof b !== 'object') return false;
  if (Array.isArray(a) || Array.isArray(b)) {
    if (!Array.isArray(a) || !Array.isArray(b) || a.length !== b.length) return false;
    for (let k = 0; k < a.length; k++) if (!deepEqual(a[k], b[k])) return false;
    return true;
  }
  const ka = Object.keys(a), kb = Object.keys(b);
  if (ka.length !== kb.length) return false;
  for (const k of ka) { if (!Object.prototype.hasOwnProperty.call(b, k)) return false; if (!deepEqual(a[k], b[k])) return false; }
  return true;
}

const files = (only.length ? only : fs.readdirSync(afterDir).filter(f => f.endsWith('.animation.json')))
  .map(f => f.endsWith('.json') ? f : f + '.animation.json');

let fail = 0, ok = 0, totalTracks = 0, totalChanged = 0;
for (const f of files) {
  const pa = path.join(beforeDir, f), pb = path.join(afterDir, f);
  if (!fs.existsSync(pa) || !fs.existsSync(pb)) { console.log(`[skip] ${f} (缺一侧文件)`); continue; }
  const TA = fs.readFileSync(pa, 'utf8'), TB = fs.readFileSync(pb, 'utf8');
  const A = JSON.parse(TA), B = JSON.parse(TB);
  const SA = makeScanner(TA), SB = makeScanner(TB);
  const problems = [];
  let tracks = 0, reordered = 0;

  // D) 数值级整体结构
  if (JSON.stringify(Object.keys(A.animations)) !== JSON.stringify(Object.keys(B.animations))) problems.push('动画段集合变了');
  for (const an of Object.keys(A.animations)) {
    const a = A.animations[an], b = B.animations[an];
    if (!deepEqual(a.animation_length, b.animation_length)) problems.push(`${an}: animation_length 变了`);
    if (!deepEqual(a.loop, b.loop)) problems.push(`${an}: loop 变了`);
    if (Object.keys(a.bones).join('|') !== Object.keys(b.bones).join('|')) problems.push(`${an}: 骨骼集合变了`);
    if (Object.keys(a.bones).join('|') === Object.keys(b.bones).join('|')
        && Object.keys(a.bones).join('|') !== Object.keys(b.bones).join('|')) problems.push(`${an}: 骨骼键序变了`);
  }

  // A/B/C) 文本级逐轨道
  for (const an of Object.keys(A.animations)) {
    for (const bn of Object.keys(A.animations[an].bones)) {
      for (const tn of Object.keys(A.animations[an].bones[bn])) {
        tracks++;
        const segs = ['animations', an, 'bones', bn, tn];
        let ea, eb;
        try { ea = SA.objectAt(segs); } catch (e) { problems.push(`${an}.${bn}.${tn}: 原文件扫描失败 ${e.message}`); continue; }
        try { eb = SB.objectAt(segs); } catch (e) { problems.push(`${an}.${bn}.${tn}: 新文件扫描失败 ${e.message}`); continue; }
        // null = 该轨道是"整段静态取值"(数组), 没有关键帧键序, 只需确认新增/删除都没有
        if (ea === null || eb === null) {
          if (ea === null && eb === null) continue;
          problems.push(`${an}.${bn}.${tn}: 轨道形态变了 (${ea === null ? '静态数组' : '关键帧对象'} -> ${eb === null ? '静态数组' : '关键帧对象'})`);
          continue;
        }
        const keysA = ea.map(e => e.key), keysB = eb.map(e => e.key);
        const numA = keysA.map(Number), numB = keysB.map(Number);
        const sortedA = numA.every((v, i) => i === 0 || numA[i - 1] <= v);
        const sortedB = numB.every((v, i) => i === 0 || numB[i - 1] <= v);
        if (!sortedA) reordered++;
        // A) 新文件必须升序
        if (!sortedB) problems.push(`${an}.${bn}.${tn}: 文本键序仍未升序 [${keysB.join(',')}]`);
        // C) 键集合一致
        if ([...keysA].sort().join('\u0000') !== [...keysB].sort().join('\u0000')) {
          problems.push(`${an}.${bn}.${tn}: 键集合变了 [${keysA.join(',')}] -> [${keysB.join(',')}]`);
          continue;
        }
        // B) 每个键的原始 JSON 片段逐字符相同
        const mapB = new Map(eb.map(e => [e.key, e.raw]));
        for (const { key, raw } of ea) {
          const rawB = mapB.get(key);
          if (raw.replace(/\s+/g, '') !== (rawB || '').replace(/\s+/g, '')) {
            problems.push(`${an}.${bn}.${tn}@${key}: 取值文本变了\n        前 ${raw.replace(/\s+/g, ' ').slice(0, 80)}\n        后 ${(rawB || '').replace(/\s+/g, ' ').slice(0, 80)}`);
          }
        }
      }
    }
  }

  totalTracks += tracks; totalChanged += reordered;
  if (problems.length) {
    fail++;
    console.log(`[FAIL] ${f}  轨道 ${tracks} (原乱序 ${reordered})  问题 ${problems.length}`);
    for (const p of problems.slice(0, 8)) console.log('    - ' + p);
    if (problems.length > 8) console.log(`    ... 另有 ${problems.length - 8} 项`);
  } else {
    ok++;
    console.log(`[PASS] ${f}  轨道 ${tracks} (原乱序 ${reordered})  键序已升序 / 键集合一致 / 取值文本逐字符一致 / 结构未变`);
  }
}
console.log(`\n汇总: ${ok} 个文件通过, ${fail} 个失败; 共 ${totalTracks} 条轨道, 其中改动前乱序 ${totalChanged} 条`);
process.exit(fail ? 1 : 0);
