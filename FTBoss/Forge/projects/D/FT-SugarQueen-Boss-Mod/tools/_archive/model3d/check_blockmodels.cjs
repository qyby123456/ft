// 方块模型校验: 元素范围/朝向/贴图引用/blockstate 指向
// 用法: node check_blockmodels.cjs <assets/ftboss 目录>
const fs = require('fs');
const path = require('path');
const A = process.argv[2].replace(/\\/g, '/');
const dir = path.join(A, 'blockstates');
let bad = 0;
for (const f of fs.readdirSync(dir)) {
  const id = f.replace(/\.json$/, '');
  const bs = JSON.parse(fs.readFileSync(path.join(dir, f), 'utf8'));
  const errs = [];
  for (const [k, v] of Object.entries(bs.variants || {})) {
    const rel = v.model.split(':')[1];
    const mp = path.join(A, 'models', rel + '.json');
    if (!fs.existsSync(mp)) { errs.push(`变体 ${k} 缺模型 ${v.model}`); continue; }
    const m = JSON.parse(fs.readFileSync(mp, 'utf8'));
    const texRef = m.textures && m.textures['0'];
    if (texRef) {
      const tf = path.join(A, 'textures', texRef.split(':')[1] + '.png');
      if (!fs.existsSync(tf)) errs.push(`缺贴图 ${texRef}`);
    }
    if (!m.elements || !m.elements.length) { errs.push(`${k}: 无元素(会渲染成空)`); continue; }
    let oob = 0, deg = 0, ymin = 1e9, ymax = -1e9, xmin = 1e9, xmax = -1e9;
    for (const e of m.elements) {
      for (let i = 0; i < 3; i++) {
        if (e.from[i] < -16.001 || e.to[i] > 32.001) oob++;
        if (e.from[i] >= e.to[i]) deg++;
      }
      ymin = Math.min(ymin, e.from[1]); ymax = Math.max(ymax, e.to[1]);
      xmin = Math.min(xmin, e.from[0]); xmax = Math.max(xmax, e.to[0]);
    }
    if (oob) errs.push(`${k}: ${oob} 处坐标越界(-16..32)`);
    if (deg) errs.push(`${k}: ${deg} 处 from>=to`);
    errs.push(`${k}: ${m.elements.length} 元素 y${ymin.toFixed(1)}..${ymax.toFixed(1)} x${xmin.toFixed(1)}..${xmax.toFixed(1)}`);
  }
  const flag = errs.some(e => /缺|无元素|越界|from>=to/.test(e));
  if (flag) bad++;
  console.log(`  ${id.padEnd(24)} ${flag ? '✗' : '✓'} ${errs.join(' | ')}`);
}
console.log(bad ? `\n有 ${bad} 个模型存在问题` : '\n全部通过');
