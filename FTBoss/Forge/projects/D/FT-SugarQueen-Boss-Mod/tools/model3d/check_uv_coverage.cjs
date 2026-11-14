// check_uv_coverage.cjs -- 检查模型每个面的 UV 是否落在贴图的已绘制(不透明)像素上
//   用途: 判断"这张贴图到底是不是这个模型的展开图"。平面图标贴图配 3D 模型 = 花屏/大片空白,
//         本脚本会在这种错配上直接把面列出来(不透明占比 < 50%)。
// 用法: node tools/model3d/check_uv_coverage.cjs <model.json> <texture.png>
const fs = require('fs');
const path = require('path');
const { decodePNG } = require(path.join(__dirname, 'png.cjs'));

const [, , modelPath, texPath] = process.argv;
if (!modelPath || !texPath) { console.error('用法: node tools/model3d/check_uv_coverage.cjs <model.json> <texture.png>'); process.exit(2); }
const j = JSON.parse(fs.readFileSync(modelPath, 'utf8'));
const img = decodePNG(texPath);
console.log('model   ' + path.basename(modelPath) + '  (' + (j.elements ? j.elements.length : 0) + ' 部件)');
console.log('texture ' + path.basename(texPath) + ' = ' + img.w + 'x' + img.h);

const alphaAt = (x, y) => {
  const xi = Math.min(img.w - 1, Math.max(0, Math.round(x)));
  const yi = Math.min(img.h - 1, Math.max(0, Math.round(y)));
  return img.data[(yi * img.w + xi) * 4 + 3];
};
let opaque = 0, minX = 1e9, maxX = -1e9, minY = 1e9, maxY = -1e9;
for (let y = 0; y < img.h; y++) for (let x = 0; x < img.w; x++) if (alphaAt(x, y) > 8) {
  opaque++; if (x < minX) minX = x; if (x > maxX) maxX = x; if (y < minY) minY = y; if (y > maxY) maxY = y;
}
console.log('不透明像素 ' + opaque + '/' + (img.w * img.h) + ' (' + (opaque / (img.w * img.h) * 100).toFixed(1) + '%), 不透明包围盒 x ' + minX + '..' + maxX + ' y ' + minY + '..' + maxY);

// Java 物品/方块模型的 UV 恒为 0..16 空间, 贴图按 16 单位铺满整张图
let faces = 0; const bad = [];
(j.elements || []).forEach((el, i) => {
  Object.entries(el.faces || {}).forEach(([fk, fc]) => {
    if (!Array.isArray(fc.uv)) return;
    faces++;
    const x0 = Math.round(fc.uv[0] / 16 * img.w), x1 = Math.round(fc.uv[2] / 16 * img.w);
    const y0 = Math.round(fc.uv[1] / 16 * img.h), y1 = Math.round(fc.uv[3] / 16 * img.h);
    const sx = Math.max(1, Math.abs(x1 - x0)), sy = Math.max(1, Math.abs(y1 - y0));
    let hits = 0, samples = 0;
    for (let s = 0; s <= sx; s++) for (let t = 0; t <= sy; t++) {
      samples++;
      if (alphaAt(Math.min(x0, x1) + s, Math.min(y0, y1) + t) > 8) hits++;
    }
    const ratio = hits / samples;
    if (ratio < 0.5) bad.push('  elements[' + i + '].' + fk + ' uv=' + JSON.stringify(fc.uv) + ' -> px x' + Math.min(x0, x1) + '-' + Math.max(x0, x1) + ' y' + Math.min(y0, y1) + '-' + Math.max(y0, y1) + '  不透明占比 ' + (ratio * 100).toFixed(0) + '% (' + samples + ' 采样)');
  });
});
console.log('面总数 ' + faces + ', 采样大部分落空的面: ' + bad.length);
bad.forEach(l => console.log(l));
console.log(bad.length === 0
  ? '=> 通过: 所有面都采到已绘制像素, 贴图与该模型匹配'
  : '=> 可疑: 该贴图可能不是这个模型的展开图(或是错的 flat 图标贴图)');
process.exit(bad.length === 0 ? 0 : 1);
