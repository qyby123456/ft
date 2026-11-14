/* wing_pose_bake4.cjs —— 把**四片**刃翼的展开角烘焙成静态 geo，用于离线出图。
 *
 * 为什么不用工程自带的 wing_pose_bake.cjs：
 *   它只认两片翼，而且 PIVOT 是**写死的** black_cleaver 正式版坐标
 *   （blade_upper_wing: [-1.55, 17.9]、blade_lower_wing: [-1.55, 12.2]），
 *   自身也从 geo 里读 pivot。重建版四个枢轴都在别处（例：左·上翼 [-1.811, 16.657]），
 *   直接套用会绕错误的点旋转 → 出的图是错的。
 *   本脚本**从 geo 里读每根骨骼的真实 pivot**，并对四片翼统一处理。
 *
 * Bedrock/GeckoLib：展开写入骨骼 rotation，保留每个 cube 的 origin、pivot 与 rotation。
 * cube 省略 pivot 时默认绕模型原点旋转，不是绕自身中心。
 *
 * 用法:
 *   node tools/model3d/wing_pose_bake4.cjs <in.geo> <out.geo> [角度]
 *   例: node tools/model3d/wing_pose_bake4.cjs art/rebuild/x.geo.json art/rebuild/x_open11.geo.json 11
 */
'use strict';
const fs = require('fs');
const path = require('path');

const [inFile, outFile, degArg] = process.argv.slice(2);
if (!inFile || !outFile) {
  console.error('用法: node wing_pose_bake4.cjs <in.geo> <out.geo> [角度=11]');
  process.exit(1);
}
const magnitude = Number(degArg ?? '11');
if (!Number.isFinite(magnitude) || magnitude < 0 || magnitude > 90) {
  throw new Error('展开角必须是 0..90 之间的有限数字');
}
if (path.resolve(inFile) === path.resolve(outFile)) {
  throw new Error('输出路径不能覆盖输入模型');
}

/* 符号与 blood_pose 一致（左·上负 / 左·下正 / 右·上正 / 右·下负），
   与正式版 gen_black_cleaver.cjs 的 blood_pose 符号约定完全一样。 */
const SIGN = {
  blade_upper_wing: -1,
  blade_lower_wing: +1,
  blade_right_upper_wing: +1,
  blade_right_lower_wing: -1,
};

const json = JSON.parse(fs.readFileSync(inFile, 'utf8'));
const geo = json['minecraft:geometry'][0];
const lines = [];
let moved = 0;
for (const [boneName, sign] of Object.entries(SIGN)) {
  const bone = geo.bones.find(entry => entry.name === boneName);
  if (!bone) throw new Error('缺少刃翼骨骼：' + boneName);
  const pivot = bone.pivot || [0, 0, 0];
  const degrees = sign * magnitude;
  const rotation = bone.rotation || [0, 0, 0];
  if (rotation.length !== 3 || !rotation.every(Number.isFinite)) {
    throw new Error('骨骼 rotation 非法：' + boneName);
  }
  bone.rotation = [rotation[0], rotation[1], rotation[2] + degrees];
  moved += (bone.cubes || []).length;
  lines.push(`  ${boneName.padEnd(24)} pivot [${pivot[0]}, ${pivot[1]}]  角度 ${degrees > 0 ? '+' : ''}${degrees}°  cube ${(bone.cubes || []).length}`);
}

fs.writeFileSync(outFile, JSON.stringify(json, null, 2) + '\n');
console.log(`[bake4] ${path.basename(inFile)} -> ${path.basename(outFile)}   展开角 ±${magnitude}°   保留 ${moved} 个 cube 的局部变换`);
console.log('  枢轴全部取自 geo（不是写死的正式版坐标）：');
for (const l of lines) console.log(l);
