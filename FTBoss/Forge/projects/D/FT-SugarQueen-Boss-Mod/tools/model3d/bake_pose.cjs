// 姿势烘焙自检: node bake_pose.cjs <in.geo.json> <out.geo.json> rotX=<deg> posY=<units> [posZ=<units>]
// 只支持绕 X 轴 ±90/180 的整角旋转(动画里的"倒地"就是这种) —— 轴对齐的 cube 转完仍是轴对齐, 可以精确重写 origin/size。
// 用途: 把动画的**最后一个姿势**烘焙成静态 geo, 再用 render_tex.cjs 出图, 亲眼确认"倒地后有没有陷进地里"。
const fs = require('fs');
const geo = JSON.parse(fs.readFileSync(process.argv[2], 'utf8'));
const outFile = process.argv[3];
let deg = 0, ty = 0, tz = 0;
for (const a of process.argv.slice(4)) {
  const m = a.match(/^(rotX|posY|posZ)=(-?[\d.]+)$/);
  if (!m) continue;
  if (m[1] === 'rotX') deg = parseFloat(m[2]);
  if (m[1] === 'posY') ty = parseFloat(m[2]);
  if (m[1] === 'posZ') tz = parseFloat(m[2]);
}
const q = ((deg % 360) + 360) % 360;
if (![0, 90, 180, 270].includes(q)) { console.error('rotX 只支持 90 的整数倍'); process.exit(1); }
// 绕 X 轴旋转: (y,z) -> (y cos - z sin, y sin + z cos)
function mapPt(p) {
  const [x, y, z] = p;
  let ny, nz;
  if (q === 0) { ny = y; nz = z; }
  else if (q === 90) { ny = -z; nz = y; }
  else if (q === 180) { ny = -y; nz = -z; }
  else { ny = z; nz = -y; }
  return [x, ny + ty, nz + tz];
}
/** 轴对齐 cube 旋转后仍是轴对齐: 用两个对角点换算新的 origin/size */
function mapBox(o, s) {
  const a = mapPt(o), b = mapPt([o[0] + s[0], o[1] + s[1], o[2] + s[2]]);
  const no = [Math.min(a[0], b[0]), Math.min(a[1], b[1]), Math.min(a[2], b[2])];
  const ns = [Math.abs(b[0] - a[0]), Math.abs(b[1] - a[1]), Math.abs(b[2] - a[2])];
  return { o: no, s: ns };
}
const g = JSON.parse(JSON.stringify(geo));
for (const geom of g['minecraft:geometry']) {
  for (const bone of geom.bones) {
    if (bone.pivot) bone.pivot = mapPt(bone.pivot);
    if (bone.cubes) for (const c of bone.cubes) {
      const r = mapBox(c.origin, c.size);
      c.origin = r.o; c.size = r.s;
    }
  }
}
fs.writeFileSync(outFile, JSON.stringify(g, null, 1));
// 打印烘焙后的包围盒, 直接看有没有低于地面(y<0)
let mn = [1e9, 1e9, 1e9], mx = [-1e9, -1e9, -1e9];
for (const geom of g['minecraft:geometry']) for (const bone of geom.bones) for (const c of (bone.cubes || []))
  for (let i = 0; i < 3; i++) { mn[i] = Math.min(mn[i], c.origin[i]); mx[i] = Math.max(mx[i], c.origin[i] + c.size[i]); }
console.log(`已写出 ${outFile}`);
console.log(`烘焙后包围盒 X ${mn[0].toFixed(1)}..${mx[0].toFixed(1)}  Y ${mn[1].toFixed(1)}..${mx[1].toFixed(1)}  Z ${mn[2].toFixed(1)}..${mx[2].toFixed(1)}`);
console.log(mn[1] < -0.01 ? `⚠ 模型有 ${(-mn[1]).toFixed(1)} 单位陷进地面以下!` : '✓ 没有陷进地面');
