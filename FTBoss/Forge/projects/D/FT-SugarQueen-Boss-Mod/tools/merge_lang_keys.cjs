#!/usr/bin/env node
/*
 * merge_lang_keys.cjs —— 把 <source> 里有、<target> 里没有的键**追加**到 <target>（就地改写）。
 *
 * 用途：`tools/merge_jar_extras.ps1` 把旧 jar 里那些"没有源码"的条目并回新构建时，
 * lang 不能整file覆盖（目标文件里有本次改动，例如头衔风味文本），只能"补键"。
 * 手写 JSON 插入太容易把逗号/行尾弄坏（本工程 v189 就试过两次），所以这里复用
 * `tools/advancements/apply_boss_advancement_lang.cjs` 定下的同一套写法：
 *   **无 BOM / CRLF / 两空格缩进 / 键序保持（新键追加在末尾）/ 落盘前后各检一次 JSON**。
 *
 * 用法: node tools/merge_lang_keys.cjs <target.json> <source.json>
 * 退出码: 0 = 成功（无论有没有补键）；非 0 = 出错。
 */
const fs = require('node:fs');
const path = require('node:path');

const [targetPath, sourcePath] = process.argv.slice(2);
if (!targetPath || !sourcePath) {
  console.error('用法: node tools/merge_lang_keys.cjs <target.json> <source.json>');
  process.exit(2);
}

const readJson = (p) => {
  const raw = fs.readFileSync(p, 'utf8');
  const body = raw.charCodeAt(0) === 0xfeff ? raw.slice(1) : raw;   // 容忍 BOM，但输出不带
  return JSON.parse(body);
};

const data = readJson(targetPath);
const source = readJson(sourcePath);

// Old deployment jars can still contain translations for content that has since been retired.
// Purge these from the target as well as ignoring them in the source, so the extras merge
// cannot silently resurrect removed content.
const RETIRED_KEYS = new Set([
  'item.ftboss.gourmet_blank_bow',
  'item.ftboss.gourmet_blank_staff',
  'tooltip.ftboss.gourmet.bow_control',
  'tooltip.ftboss.gourmet.staff_control',
  'message.ftboss.evolution.boss_no_weapon',
  'message.ftboss.evolution.boss_xp',
  'msg.ftboss.hitcap'
]);
let removed = 0;
for (const key of RETIRED_KEYS) {
  if (!Object.prototype.hasOwnProperty.call(data, key)) continue;
  delete data[key];
  removed++;
}

let added = 0;
for (const [key, value] of Object.entries(source)) {
  if (RETIRED_KEYS.has(key)) continue;
  if (Object.prototype.hasOwnProperty.call(data, key)) continue;    // 目标已有 -> 保持目标的值
  data[key] = value;
  added++;
}

if (added === 0 && removed === 0) {
  console.log(`  ${path.basename(targetPath)}: 无需补键或清理（${Object.keys(data).length} 键）`);
  process.exit(0);
}

const entries = Object.entries(data);
const output = '{\r\n'
  + entries.map(([k, v], i) => `  ${JSON.stringify(k)}: ${JSON.stringify(v)}${i === entries.length - 1 ? '' : ','}`).join('\r\n')
  + '\r\n}\r\n';

JSON.parse(output);                        // 落盘前自检
fs.writeFileSync(targetPath, output);
const after = fs.readFileSync(targetPath, 'utf8');
JSON.parse(after);                         // 落盘后复验
if (after.charCodeAt(0) === 0xfeff) throw new Error('输出带了 BOM（本工程构建不允许）');
if ((after.match(/\r\n/g) || []).length === 0) throw new Error('输出不是 CRLF');
console.log(`  ${path.basename(targetPath)}: 补 ${added} 键，清理 ${removed} 个退役键 -> 共 ${entries.length} 键（CRLF/无 BOM）`);
