const fs = require('node:fs');
const path = require('node:path');
const { encodePNG, decodePNG } = require('./png.cjs');

const input = path.resolve(process.argv[2]);
const atlas = decodePNG(path.resolve(process.argv[3]));
const columns = atlas.w / 128;
const solidTile = columns * columns - 1;
const output = path.resolve(process.argv[4]);
const root = path.resolve(__dirname, '../..');
const names = JSON.parse(fs.readFileSync(path.join(root, 'src/main/resources/assets/ftboss/lang/zh_cn.json'), 'utf8'));
const models = JSON.parse(fs.readFileSync(input, 'utf8'));
fs.mkdirSync(output, { recursive: true });
const size = 256;
const sheetHeight = size * Math.ceil(models.length / 6);
const sheet = new Uint8Array(size * 6 * sheetHeight * 4);

function project(point) {
  return [(point[0] - point[2]) * Math.SQRT1_2, (point[0] + point[2]) * 0.3 - point[1] * 0.9,
    (point[0] + point[2]) * 0.64 + point[1] * 0.42];
}

function triangle(image, points, tint, opacity) {
  const [first, second, third] = points;
  const area = (second[1] - third[1]) * (first[0] - third[0]) + (third[0] - second[0]) * (first[1] - third[1]);
  if (Math.abs(area) < 0.01) return;
  const minX = Math.max(0, Math.floor(Math.min(...points.map(point => point[0]))));
  const maxX = Math.min(size - 1, Math.ceil(Math.max(...points.map(point => point[0]))));
  const minY = Math.max(0, Math.floor(Math.min(...points.map(point => point[1]))));
  const maxY = Math.min(size - 1, Math.ceil(Math.max(...points.map(point => point[1]))));
  for (let pixelY = minY; pixelY <= maxY; pixelY++) for (let pixelX = minX; pixelX <= maxX; pixelX++) {
    const across = pixelX + 0.5;
    const down = pixelY + 0.5;
    const weightFirst = ((second[1] - third[1]) * (across - third[0]) + (third[0] - second[0]) * (down - third[1])) / area;
    const weightSecond = ((third[1] - first[1]) * (across - third[0]) + (first[0] - third[0]) * (down - third[1])) / area;
    const weightThird = 1 - weightFirst - weightSecond;
    if (Math.min(weightFirst, weightSecond, weightThird) < 0) continue;
    const textureX = Math.max(0, Math.min(atlas.w - 1, Math.floor(first[3] * weightFirst + second[3] * weightSecond + third[3] * weightThird)));
    const textureY = Math.max(0, Math.min(atlas.h - 1, Math.floor(first[4] * weightFirst + second[4] * weightSecond + third[4] * weightThird)));
    const textureOffset = (textureY * atlas.w + textureX) * 4;
    const alpha = atlas.data[textureOffset + 3] / 255 * opacity;
    if (alpha <= 0) continue;
    const offset = (pixelY * size + pixelX) * 4;
    for (let channel = 0; channel < 3; channel++) image[offset + channel] = Math.round(
      image[offset + channel] * (1 - alpha) + atlas.data[textureOffset + channel] * tint[channel] / 255 * alpha);
  }
}

models.forEach((model, index) => {
  const quads = model.quads.map(quad => ({ tint: [quad[0] >> 16 & 255, quad[0] >> 8 & 255, quad[0] & 255],
    opacity: quad[1], tile: quad[2], points: [0, 1, 2, 3].map(corner => project(quad.slice(3 + corner * 3, 6 + corner * 3))) }));
  const all = quads.flatMap(quad => quad.points);
  const minX = Math.min(...all.map(point => point[0]));
  const maxX = Math.max(...all.map(point => point[0]));
  const minY = Math.min(...all.map(point => point[1]));
  const maxY = Math.max(...all.map(point => point[1]));
  const scale = Math.min(224 / Math.max(2, maxX - minX), 208 / Math.max(2, maxY - minY));
  const image = new Uint8Array(size * size * 4);
  for (let pixel = 0; pixel < size * size; pixel++) image.set([12, 19, 29, 255], pixel * 4);
  quads.sort((first, second) => first.points.reduce((sum, point) => sum + point[2], 0) - second.points.reduce((sum, point) => sum + point[2], 0));
  for (const quad of quads) {
    const textureX = quad.tile % columns * 128 + 1;
    const textureY = Math.floor(quad.tile / columns) * 128 + 1;
    const corners = [[0, 126], [0, 0], [126, 0], [126, 126]];
    const points = quad.points.map((point, corner) => [128 + (point[0] - (minX + maxX) / 2) * scale,
      126 + (point[1] - (minY + maxY) / 2) * scale, point[2],
      quad.tile === solidTile ? atlas.w - 64 : textureX + corners[corner][0], quad.tile === solidTile ? atlas.h - 64 : textureY + corners[corner][1]]);
    triangle(image, [points[0], points[1], points[2]], quad.tint, quad.opacity);
    triangle(image, [points[0], points[2], points[3]], quad.tint, quad.opacity);
  }
  fs.writeFileSync(path.join(output, model.id + '.png'), encodePNG(size, size, image));
  for (let row = 0; row < size; row++) {
    sheet.set(image.subarray(row * size * 4, (row + 1) * size * 4),
      ((Math.floor(index / 6) * size + row) * size * 6 + index % 6 * size) * 4);
  }
});
fs.writeFileSync(path.join(output, '全部法术立体特效.png'), encodePNG(size * 6, sheetHeight, sheet));
const escape = value => String(value).replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('"', '&quot;');
fs.writeFileSync(path.join(output, '特效图鉴.html'), `<!doctype html><html lang="zh-CN"><meta charset="utf-8">
<title>FTBoss · ${models.length} 门法术特效图鉴</title><style>body{background:#0c131d;color:#e0e8ef;font:16px system-ui;margin:36px}
main{display:grid;grid-template-columns:repeat(auto-fit,minmax(256px,1fr));gap:18px}article{background:#131f2e;border:1px solid #304356;border-radius:12px;padding:14px}
img{width:100%;image-rendering:auto}h1{font-size:28px}h2{font-size:18px;margin:8px 0}small{color:#9bacbf;overflow-wrap:anywhere}p{line-height:1.7}</style>
<h1>${models.length} 门法术 · 独立法阵与立体特效</h1><p>以下为游戏实际使用的 Java 几何网格离线预览，不是游戏截图。每门法术还包含蓄力、旋转/展开、淡出；持续和延迟效果由服务端实际生效时点驱动。<br>发光强度、地形遮挡和观察角度以游戏中为准；此页为选定瞬间的静态图鉴。</p><main>
${models.map((model, index) => `<article><img src="${model.id}.png" alt="${escape(names['spell.ftboss.' + model.id])}"><h2>${String(index + 1).padStart(2, '0')} · ${escape(names['spell.ftboss.' + model.id])}</h2><small>${model.id} · ${model.phase}</small></article>`).join('\n')}
</main></html>`);
console.log(`Rendered ${models.length} actual runtime meshes, contact sheet and Chinese gallery`);
