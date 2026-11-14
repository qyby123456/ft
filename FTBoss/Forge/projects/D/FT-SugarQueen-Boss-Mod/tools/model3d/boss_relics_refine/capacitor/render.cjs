'use strict';
const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const { root, raster, parts } = require('../../boss_materials_refine/capacitor/render.cjs');
const { decodePNG } = require('../../png.cjs');
const { createCanvas, loadImage, GlobalFonts } = require(path.join(process.env.FT_NODE_MODULES || 'C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules', '@napi-rs/canvas'));
const output = path.join(root, 'art/boss_relics/refine/capacitor');
const base = path.join(root, 'src/main/resources/assets/ftboss');
const item = 'charged_core_pendant';
GlobalFonts.registerFromPath('C:/Windows/Fonts/msyh.ttc', 'Yahei');
async function sheet(filename, items, columns, cellWidth = 500, cellHeight = 580, heading = '') {
  const canvas = createCanvas(columns * cellWidth, Math.ceil(items.length / columns) * cellHeight + 80), context = canvas.getContext('2d');
  context.fillStyle = '#101c27'; context.fillRect(0, 0, canvas.width, canvas.height);
  context.fillStyle = '#e8e4cf'; context.font = 'bold 26px Yahei'; context.fillText(heading, 24, 38);
  context.fillStyle = '#7ca6b6'; context.font = '14px Yahei'; context.fillText('FTBOSS  /  CHARGED CORE PENDANT  /  Final runtime JSON + PNG', 24, 64);
  for (const [index, entry] of items.entries()) {
    const left = index % columns * cellWidth, top = Math.floor(index / columns) * cellHeight + 80;
    context.fillStyle = '#152533'; context.fillRect(left + 8, top + 8, cellWidth - 16, cellHeight - 16);
    const image = await loadImage(entry.png || entry.path), fit = Math.min((cellWidth - 36) / image.width, (cellHeight - 68) / image.height);
    context.imageSmoothingEnabled = false; context.drawImage(image, left + (cellWidth - image.width * fit) / 2, top + 16, image.width * fit, image.height * fit);
    context.font = 'bold 19px Yahei'; context.fillStyle = '#dfdcc9'; context.fillText(entry.label, left + 24, top + cellHeight - 24);
  }
  fs.writeFileSync(path.join(output, filename), canvas.toBuffer('image/png'));
}
async function main() {
  fs.mkdirSync(output, { recursive: true });
  const model = JSON.parse(fs.readFileSync(path.join(base, 'models/item/boss_relics/' + item + '.json')));
  const texture = decodePNG(path.join(base, 'textures/item/boss_relics/' + item + '.png')), entries = parts(model, texture);
  const views = [['front', 0, '正面 / NORTH'], ['back', 180, '背面 / SOUTH'], ['left', -90, '左侧 / WEST'], ['right', 90, '右侧 / EAST']];
  const images = views.map(([name, yaw, label]) => {
    const image = raster(entries, texture, { width: 600, height: 780, center: [8, 8.5, 8], yaw, zoom: 55 });
    fs.writeFileSync(path.join(output, name + '.png'), image.png); return { png: image.png, label };
  });
  await sheet('four-views.png', images, 4, 420, 630, '蓄能核心坠 · 最终运行时四视图');
  const front = raster(entries, texture, { width: 900, height: 960, yaw: -28, pitch: -14, center: [8, 8.5, 8], zoom: 67 });
  const back = raster(entries, texture, { width: 900, height: 960, yaw: 148, pitch: -14, center: [8, 8.5, 8], zoom: 67 });
  fs.writeFileSync(path.join(output, 'oblique.png'), front.png); fs.writeFileSync(path.join(output, 'reverse-oblique.png'), back.png);
  await sheet('detail-and-icon.png', [{ png: front.png, label: '镶芯正面 · 倒角 / 闪电 / 电极' }, { png: back.png, label: '检修背面 · 汇流纹 / 扣环' },
    { path: path.join(base, 'textures/item/' + item + '.png'), label: '独立 GUI 图标 · 32×32' }], 3, 520, 660, '蓄能核心坠 · 成品饰物结构与图标');
  const materialModel = JSON.parse(fs.readFileSync(path.join(base, 'models/item/boss_materials/capacitor_material.json')));
  const materialTexture = decodePNG(path.join(base, 'textures/item/boss_materials/capacitor.png'));
  const material = raster(parts(materialModel, materialTexture), materialTexture, { width: 900, height: 960, yaw: -34, pitch: -19, center: [8, 8.1, 8], zoom: 64 });
  await sheet('material-relic-comparison.png', [{ png: material.png, label: '原料 · 铜绕组蓄能电芯' }, { png: front.png, label: '成品 · 八角镶芯核心坠' }], 2, 620, 720, '同一 Boss · 材料到成品的视觉关系');
  const icon = await loadImage(path.join(base, 'textures/item/' + item + '.png')), oldIcon = await loadImage(path.join(output, 'references/old_charged_core_pendant.png'));
  const comparison = createCanvas(1000, 420), context = comparison.getContext('2d');
  context.fillStyle = '#152533'; context.fillRect(0, 0, 1000, 420); context.imageSmoothingEnabled = false;
  context.drawImage(oldIcon, 40, 70, 240, 240); context.drawImage(icon, 345, 90, 32, 32); context.drawImage(icon, 410, 90, 16, 16); context.drawImage(icon, 530, 40, 320, 320);
  context.fillStyle = '#e8e4cf'; context.font = '20px Yahei'; context.fillText('旧版 · 平面挂坠', 40, 355); context.fillText('32px 原尺寸', 310, 185); context.fillText('16px GUI', 405, 230); context.fillText('新版 · 10× 放大', 585, 390);
  fs.writeFileSync(path.join(output, 'icon-sizes.png'), comparison.toBuffer('image/png'));
  await sheet('reference-study.png', [
    { path: path.join(root, 'art/boss_materials/refine/capacitor/references/boss-runtime.png'), label: '实际 Boss · 铜黄 / 青蓝' },
    { path: path.join(output, 'references/old_charged_core_pendant.png'), label: '旧遗物 · 闪电主题' },
    { path: path.join(output, 'references/assets__createaddition__textures__item__electrum_amulet.png'), label: '成熟配饰 · 金属护符参考' }
  ], 3, 420, 600, '参考研究 · 同源材质与精加工吊坠结构');
  const hash = filename => crypto.createHash('sha256').update(fs.readFileSync(filename)).digest('hex');
  const inputs = ['models/item/boss_relics/' + item + '.json', 'textures/item/boss_relics/' + item + '.png', 'textures/item/' + item + '.png'];
  const imageNames = ['front.png', 'back.png', 'left.png', 'right.png', 'four-views.png', 'oblique.png', 'reverse-oblique.png', 'detail-and-icon.png', 'material-relic-comparison.png', 'icon-sizes.png'];
  fs.writeFileSync(path.join(root, '_work/boss-relics-refine-20261021/capacitor/preview-manifest.json'), JSON.stringify({
    inputSha256: Object.fromEntries(inputs.map(filename => [filename, hash(path.join(base, filename))])),
    imageSha256: Object.fromEntries(imageNames.map(filename => [filename, hash(path.join(output, filename))])),
    renderer: 'capacitor material tested Java UV/rotation/rescale/cutout software renderer, read-only reuse'
  }, null, 2) + '\n');
  console.log('Rendered charged_core_pendant final runtime four views, details, icon sizes and material comparison.');
}
main().catch(error => { console.error(error); process.exitCode = 1; });
