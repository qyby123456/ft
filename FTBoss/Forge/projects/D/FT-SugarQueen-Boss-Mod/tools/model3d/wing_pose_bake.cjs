/* wing_pose_bake.cjs —— 把"血怒展开"的两个角度烘焙成一份静态 geo，用来**离线出图**看效果。
 *
 * 为什么需要它：刃翼的实机角度是由 Java 在渲染时 `setRotZ(基础 + 偏移×进度)` 直接设的，
 * 不经过动画文件。所以想在不进游戏的情况下确认"展开到底长什么样、往哪边开、关节会不会露缝"，
 * 就得把那个姿态固化进 geo 再看图。
 *
 * Bedrock/GeckoLib 的骨骼旋转 = 绕**骨骼 pivot**转；而 cube 自带的 `rotation` 是绕
 * **cube 自身中心**转。轴对齐的盒子绕 Z 转完仍是同尺寸的盒子，所以可以精确等价地写成：
 *     center' = rotateZ(center, pivot, deg)
 *     origin  = center' - size/2
 *     rotation = [0, 0, deg]
 *
 * 用法:
 *   node tools/model3d/wing_pose_bake.cjs <in.geo> <out.geo> [upper=<deg>] [lower=<deg>]
 *   例: node tools/model3d/wing_pose_bake.cjs geo/black_cleaver.geo.json out_open.geo.json upper=-8 lower=8
 */
'use strict';

const fs = require('fs');
const path = require('path');

const [inFile, outFile, ...rest] = process.argv.slice(2);

if (!inFile || !outFile) {
  console.error('用法: node wing_pose_bake.cjs <in.geo> <out.geo> [upper=<deg>] [lower=<deg>]');
  process.exit(1);
}

const angleOf = (name, fallback) => {
  const hit = rest.find(a => a.startsWith(name + '='));
  return hit ? parseFloat(hit.slice(name.length + 1)) : fallback;
};

const UPPER = angleOf('upper', -8);
const LOWER = angleOf('lower', 8);

/** 与 geo 里的 makeBone 一致（规格给的枢轴）。 */
const PIVOT = {
  blade_upper_wing: [-1.55, 17.9],
  blade_lower_wing: [-1.55, 12.2]
};

const rotZ = (x, y, pivot, deg) => {
  const r = deg * Math.PI / 180;
  const c = Math.cos(r), s = Math.sin(r);
  const dx = x - pivot[0], dy = y - pivot[1];
  return [pivot[0] + dx * c - dy * s, pivot[1] + dx * s + dy * c];
};

const json = JSON.parse(fs.readFileSync(inFile, 'utf8'));
const geo = json['minecraft:geometry'][0];
let moved = 0;

for (const bone of geo.bones || []) {
  const pivot = PIVOT[bone.name];
  if (!pivot) continue;

  const deg = bone.name === 'blade_upper_wing' ? UPPER : LOWER;
  const r = deg * Math.PI / 180;
  const c = Math.cos(r), s = Math.sin(r);

  for (const cube of bone.cubes || []) {
    const [ox, oy, oz] = cube.origin;
    const [sx, sy, sz] = cube.size;
    /* 绕自身中心转是 GeckoLib/render 对 cube.rotation 的解释，所以只搬运中心。 */
    const cx = ox + sx / 2, cy = oy + sy / 2;
    const [nx, ny] = rotZ(cx, cy, pivot, deg);
    cube.origin = [nx - sx / 2, ny - sy / 2, oz];
    cube.rotation = [0, 0, deg];
    moved++;
  }
}

fs.writeFileSync(outFile, JSON.stringify(json, null, 2));
console.log(`[bake] ${path.basename(inFile)} -> ${path.basename(outFile)}  上翼 ${UPPER}° / 下翼 ${LOWER}°  动了 ${moved} 个 cube`);
