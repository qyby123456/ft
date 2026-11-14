// 骨骼穿模检测: 按骨骼汇总 cube 的世界 AABB, 两两求交, 报告重叠尺寸(模型单位, 16=1格)
// 用法: node check_clip.cjs <geo.json> [阈值=0.5]
const fs = require('fs');
const file = process.argv[2];
const TH = parseFloat(process.argv[3] || '0.5');
const g = JSON.parse(fs.readFileSync(file, 'utf8'));
const geo = g['minecraft:geometry'][0];

const I = () => [1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1];
const mul = (a, b) => { const o = new Array(16); for (let r = 0; r < 4; r++) for (let c = 0; c < 4; c++) { let s = 0; for (let k = 0; k < 4; k++) s += a[r * 4 + k] * b[k * 4 + c]; o[r * 4 + c] = s; } return o; };
const tr = (x, y, z) => [1, 0, 0, x, 0, 1, 0, y, 0, 0, 1, z, 0, 0, 0, 1];
const rX = d => { const r = d * Math.PI / 180, c = Math.cos(r), s = Math.sin(r); return [1, 0, 0, 0, 0, c, -s, 0, 0, s, c, 0, 0, 0, 0, 1]; };
const rY = d => { const r = d * Math.PI / 180, c = Math.cos(r), s = Math.sin(r); return [c, 0, s, 0, 0, 1, 0, 0, -s, 0, c, 0, 0, 0, 0, 1]; };
const rZ = d => { const r = d * Math.PI / 180, c = Math.cos(r), s = Math.sin(r); return [c, -s, 0, 0, s, c, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1]; };
const xf = (m, p) => [m[0] * p[0] + m[1] * p[1] + m[2] * p[2] + m[3], m[4] * p[0] + m[5] * p[1] + m[6] * p[2] + m[7], m[8] * p[0] + m[9] * p[1] + m[10] * p[2] + m[11]];

const boxes = {};   // bone -> [minx,miny,minz,maxx,maxy,maxz]
function walk(bones, parentM) {
  for (const b of bones) {
    const piv = b.pivot || [0, 0, 0];
    let m = mul(parentM, tr(piv[0], piv[1], piv[2]));
    if (b.rotation) { m = mul(m, rZ(b.rotation[2] || 0)); m = mul(m, rY(b.rotation[1] || 0)); m = mul(m, rX(b.rotation[0] || 0)); }
    m = mul(m, tr(-piv[0], -piv[1], -piv[2]));
    for (const c of (b.cubes || [])) {
      const inf = c.inflate || 0;
      const s = [c.size[0] + inf * 2, c.size[1] + inf * 2, c.size[2] + inf * 2];
      const o = [c.origin[0] - inf, c.origin[1] - inf, c.origin[2] - inf];
      let cm = m;
      if (c.rotation) {
        const ctr = [o[0] + s[0] / 2, o[1] + s[1] / 2, o[2] + s[2] / 2];
        cm = mul(cm, tr(ctr[0], ctr[1], ctr[2]));
        cm = mul(cm, rZ(c.rotation[2] || 0)); cm = mul(cm, rY(c.rotation[1] || 0)); cm = mul(cm, rX(c.rotation[0] || 0));
        cm = mul(cm, tr(-ctr[0], -ctr[1], -ctr[2]));
      }
      for (let i = 0; i < 8; i++) {
        const p = xf(cm, [o[0] + (i & 1 ? s[0] : 0), o[1] + (i & 2 ? s[1] : 0), o[2] + (i & 4 ? s[2] : 0)]);
        const bb = boxes[b.name] || (boxes[b.name] = [1e9, 1e9, 1e9, -1e9, -1e9, -1e9]);
        for (let k = 0; k < 3; k++) { bb[k] = Math.min(bb[k], p[k]); bb[k + 3] = Math.max(bb[k + 3], p[k]); }
      }
    }
    if (b.children) walk(b.children, m);
  }
}
function toTree(bones) {
  if (bones.some(b => b.children)) return bones;
  const map = {}; for (const b of bones) map[b.name] = Object.assign({}, b, { children: [] });
  const roots = []; for (const b of bones) { if (b.parent && map[b.parent]) map[b.parent].children.push(map[b.name]); else roots.push(map[b.name]); }
  return roots;
}
walk(toTree(geo.bones), I());

const names = Object.keys(boxes);
console.log(`骨骼 ${names.length} 个; 阈值 ${TH} 单位(${(TH / 16).toFixed(2)} 格)`);
console.log('各骨骼 AABB (Bedrock: y 向上):');
for (const n of names) {
  const b = boxes[n];
  console.log(`  ${n.padEnd(16)} X ${b[0].toFixed(1)}..${b[3].toFixed(1)}  Y ${b[1].toFixed(1)}..${b[4].toFixed(1)}  Z ${b[2].toFixed(1)}..${b[5].toFixed(1)}`);
}
console.log('\n重叠的骨骼对:');
let found = 0;
for (let i = 0; i < names.length; i++) for (let j = i + 1; j < names.length; j++) {
  const a = boxes[names[i]], b = boxes[names[j]];
  const ov = [];
  for (let k = 0; k < 3; k++) ov.push(Math.min(a[k + 3], b[k + 3]) - Math.max(a[k], b[k]));
  if (ov[0] > TH && ov[1] > TH && ov[2] > TH) {
    // 相对较薄的两轴比例, 便于判断是"关节插入"还是"真的穿模"
    const vol = ov[0] * ov[1] * ov[2];
    console.log(`  ${names[i]} <-> ${names[j]}  重叠 X${ov[0].toFixed(1)} Y${ov[1].toFixed(1)} Z${ov[2].toFixed(1)}  体积 ${vol.toFixed(0)}`);
    found++;
  }
}
if (!found) console.log('  (无超过阈值的重叠)');
