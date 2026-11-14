const fs = require('node:fs');
const path = require('node:path');
const { encodePNG, decodePNG } = require('./png.cjs');

const source = path.resolve(process.argv[2]);
const output = path.resolve(process.argv[3]);
const models = JSON.parse(fs.readFileSync(path.join(source, 'native-models.json'), 'utf8'));
fs.mkdirSync(output, { recursive: true });
const size = 400;
const sheet = new Uint8Array(size * 2 * size * 2 * 4);
const titles = { ICE_CROWN: '糖霜加冕 · 原生冰刺晶冠', GOLD_SHIELD: '金果圣佑 · 六面原生盾', IVORY_SHIELD: '灵偶守御 · 四面原生盾', WINGS: '乘羽轻身 · 原生羽翼' };

function project(vertex, front) {
  const [across, vertical, depth, textureU, textureV, color, alpha] = vertex;
  return front ? [across, -vertical, depth, textureU, textureV, color, alpha]
    : [(across - depth) * Math.SQRT1_2, (across + depth) * 0.3 - vertical * 0.9,
      (across + depth) * 0.64 + vertical * 0.42, textureU, textureV, color, alpha];
}

function triangle(image, texture, first, second, third) {
  const area = (second[1] - third[1]) * (first[0] - third[0]) + (third[0] - second[0]) * (first[1] - third[1]);
  if (Math.abs(area) < 0.001) return;
  const minX = Math.max(0, Math.floor(Math.min(first[0], second[0], third[0])));
  const maxX = Math.min(size - 1, Math.ceil(Math.max(first[0], second[0], third[0])));
  const minY = Math.max(0, Math.floor(Math.min(first[1], second[1], third[1])));
  const maxY = Math.min(size - 1, Math.ceil(Math.max(first[1], second[1], third[1])));
  for (let vertical = minY; vertical <= maxY; vertical++) for (let across = minX; across <= maxX; across++) {
    const weightFirst = ((second[1] - third[1]) * (across + 0.5 - third[0]) + (third[0] - second[0]) * (vertical + 0.5 - third[1])) / area;
    const weightSecond = ((third[1] - first[1]) * (across + 0.5 - third[0]) + (first[0] - third[0]) * (vertical + 0.5 - third[1])) / area;
    const weightThird = 1 - weightFirst - weightSecond;
    if (Math.min(weightFirst, weightSecond, weightThird) < 0) continue;
    const textureX = Math.min(texture.w - 1, Math.max(0, Math.floor((first[3] * weightFirst + second[3] * weightSecond + third[3] * weightThird) * texture.w)));
    const textureY = Math.min(texture.h - 1, Math.max(0, Math.floor((first[4] * weightFirst + second[4] * weightSecond + third[4] * weightThird) * texture.h)));
    const texel = (textureY * texture.w + textureX) * 4;
    const alpha = texture.data[texel + 3] / 255 * first[6];
    if (alpha <= 0) continue;
    const pixel = (vertical * size + across) * 4;
    const tint = [first[5] >> 16 & 255, first[5] >> 8 & 255, first[5] & 255];
    for (let channel = 0; channel < 3; channel++) image[pixel + channel] = Math.round(image[pixel + channel] * (1 - alpha) + texture.data[texel + channel] * tint[channel] / 255 * alpha);
  }
}

models.forEach((model, index) => {
  const texture = decodePNG(path.join(source, model.texture));
  const vertices = model.vertices.map(vertex => project(vertex, model.id === 'WINGS'));
  const minX = Math.min(...vertices.map(vertex => vertex[0]));
  const maxX = Math.max(...vertices.map(vertex => vertex[0]));
  const minY = Math.min(...vertices.map(vertex => vertex[1]));
  const maxY = Math.max(...vertices.map(vertex => vertex[1]));
  const scale = Math.min(344 / Math.max(1, maxX - minX), 336 / Math.max(1, maxY - minY));
  const quads = [];
  for (let first = 0; first < vertices.length; first += 4) quads.push(vertices.slice(first, first + 4));
  quads.sort((first, second) => first.reduce((sum, point) => sum + point[2], 0) - second.reduce((sum, point) => sum + point[2], 0));
  const image = new Uint8Array(size * size * 4);
  for (let pixel = 0; pixel < size * size; pixel++) image.set([16, 23, 34, 255], pixel * 4);
  for (const quad of quads) {
    const points = quad.map(vertex => [200 + (vertex[0] - (minX + maxX) / 2) * scale, 200 + (vertex[1] - (minY + maxY) / 2) * scale, ...vertex.slice(2)]);
    triangle(image, texture, points[0], points[1], points[2]);
    triangle(image, texture, points[0], points[2], points[3]);
  }
  fs.writeFileSync(path.join(output, model.id + '.png'), encodePNG(size, size, image));
  for (let row = 0; row < size; row++) sheet.set(image.subarray(row * size * 4, (row + 1) * size * 4), ((Math.floor(index / 2) * size + row) * size * 2 + index % 2 * size) * 4);
});
fs.writeFileSync(path.join(output, '原生模型适配.png'), encodePNG(size * 2, size * 2, sheet));
fs.writeFileSync(path.join(output, '原生模型图鉴.html'), `<!doctype html><html lang="zh-CN"><meta charset="utf-8"><title>铁魔法原生模型适配</title><style>body{background:#101722;color:#e3edf5;font:16px system-ui;margin:32px}main{display:grid;grid-template-columns:repeat(2,minmax(260px,440px));gap:20px}article{border:1px solid #456;border-radius:12px;padding:16px}img{width:100%}p{max-width:900px;line-height:1.8}</style><h1>原生模型适配 · 与线条线程分开</h1><p>实际调用 Iron 3.16.3 模型构建器，再将运行时同一份模型、骨骼变换、UV 和原版材质离线光栅化。不是游戏截图，不代替光影兼容性验收。下列四门替换释放主体，不叠加原线框主体；另八门采用原生粒子，只有游戏内施法才能验收运动表现。</p><main>${models.map(model => `<article><h2>${titles[model.id]}</h2><img src="${model.id}.png" alt="${titles[model.id]}"></article>`).join('')}</main>`);
console.log('Rendered four actual Iron model adapters with original native textures; no line-thread files changed');
