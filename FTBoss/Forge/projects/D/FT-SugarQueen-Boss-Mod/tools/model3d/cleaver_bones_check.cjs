/* cleaver_bones_check.cjs —— 「Java 的刃翼骨骼名 ↔ geo ↔ 动画」三方对齐检查（只读）
 *
 * 为什么需要它：血怒展开是靠 **Java 按骨骼名直接 set 旋转** 实现的（不走动画控制器）。
 * 于是名字打错一个字母的后果是**完全静默**的：
 *   · geo 里没有那个名字 → GeckoLib 拿不到骨骼 → 那一片刃翼永远不展开；
 *   · 没有报错、没有日志、validate.cjs / validate_anim.cjs 也全绿
 *     （它们只查"动画引用的骨骼存不存在"，而 blood_pose 根本没被 Java 使用）。
 * 本脚本把三方对起来：
 *   ① geo 里必须存在 Java 常量里的每个骨骼名；
 *   ② blood_pose 引用的骨骼必须在 geo 里存在；
 *   ③ Java 管的骨骼集合 与 blood_pose 的骨骼集合必须**完全一致**（不多不少）。
 *
 * 用法: node tools/model3d/cleaver_bones_check.cjs [--geo f] [--anim f] [--java f] [--json]
 */
'use strict';

const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..', '..');
const args = process.argv.slice(2);
const opt = (name, fallback) => {
  const i = args.indexOf(name);
  return i >= 0 && args[i + 1] !== undefined ? args[i + 1] : fallback;
};

const GEO = opt('--geo', path.join(ROOT, 'src/main/resources/assets/ftboss/geo/black_cleaver.geo.json'));
const ANIM = opt('--anim', path.join(ROOT, 'src/main/resources/assets/ftboss/animations/black_cleaver.animation.json'));
const JAVA = opt('--java', path.join(ROOT, 'src/main/java/net/farmingtales/ftboss/client/render/CleaverBloodVisualState.java'));
const AS_JSON = args.includes('--json');

const geo = JSON.parse(fs.readFileSync(GEO, 'utf8'))['minecraft:geometry'][0];
const anim = JSON.parse(fs.readFileSync(ANIM, 'utf8')).animations || {};
const java = fs.readFileSync(JAVA, 'utf8');

const geoBones = new Set((geo.bones || []).map(b => b.name));

/* Java 里形如： public static final String BONE_XXX = "blade_..."; */
const javaNames = [...java.matchAll(/String\s+BONE_[A-Z_]+\s*=\s*"([^"]+)"/g)].map(m => m[1]);

const clipName = 'animation.black_cleaver.blood_pose';
const clip = anim[clipName];
const clipBones = clip ? Object.keys(clip.bones || {}) : [];

const problems = [];

for (const n of javaNames) {
  if (!geoBones.has(n)) problems.push(`Java 常量 ${n} 在 geo 里不存在（那片刃翼永远不会展开）`);
}
if (!clip) {
  problems.push(`动画里没有 ${clipName}`);
} else {
  for (const n of clipBones) {
    if (!geoBones.has(n)) problems.push(`blood_pose 引用了 geo 里不存在的骨骼 ${n}`);
  }
  for (const n of clipBones) {
    if (!javaNames.includes(n)) problems.push(`blood_pose 有 ${n}，但 Java 没管它（这片刃翼不会动）`);
  }
  for (const n of javaNames) {
    if (!clipBones.includes(n)) problems.push(`Java 管了 ${n}，但 blood_pose 里没有（Blockbench 预览看不到它）`);
  }
}

if (AS_JSON) {
  console.log(JSON.stringify({ geo: GEO, javaNames, clipBones, problems }, null, 2));
} else {
  console.log('=== black_cleaver 刃翼骨骼名三方对齐 ===');
  console.log('geo 骨骼数 ' + geoBones.size + ' | Java 常量 ' + javaNames.length + ' 个 | blood_pose ' + clipBones.length + ' 根');
  console.log('');
  console.log('Java 常量 : ' + javaNames.join(', '));
  console.log('blood_pose: ' + clipBones.join(', '));
  console.log('');
  if (problems.length === 0) {
    console.log('✓ 三方完全对齐（四片刃翼 Java 都管得到，动画预览也都能看到）');
  } else {
    for (const p of problems) console.log('✗ ' + p);
    console.log('');
    console.log('结论: ' + problems.length + ' 项对不齐');
  }
  process.exit(problems.length === 0 ? 0 : 1);
}
