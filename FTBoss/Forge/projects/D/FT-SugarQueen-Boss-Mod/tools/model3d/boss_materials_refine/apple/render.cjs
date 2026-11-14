const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const { makeCanvas, writeSafe } = require('../../lib_itemtex.cjs');
const { encodePNG, decodePNG } = require('../../png.cjs');
const { loadRuntime, render } = require('./raster.cjs');
const root = path.resolve(__dirname, '../../../..');
const art = path.join(root, 'art/boss_materials/refine/apple');
const resourceRoot = path.join(root, 'src/main/resources/assets/ftboss');
const runtime = loadRuntime(root);
const font = {
  A: '01110100011000111111100011000110001', B: '11110100011000111110100011000111110', C: '01111100001000010000100001000001111',
  D: '11110100011000110001100011000111110', E: '11111100001000011110100001000011111', F: '11111100001000011110100001000010000',
  G: '01111100001000010111100011000101111', H: '10001100011000111111100011000110001', I: '11111001000010000100001000010011111',
  J: '00111000100001000010100101001001100', K: '10001100101010011000101001001010001', L: '10000100001000010000100001000011111',
  M: '10001110111010110101100011000110001', N: '10001110011010110011100011000110001', O: '01110100011000110001100011000101110',
  P: '11110100011000111110100001000010000', Q: '01110100011000110001101011001001101', R: '11110100011000111110101001001010001',
  S: '01111100001000001110000010000111110', T: '11111001000010000100001000010000100', U: '10001100011000110001100011000101110',
  V: '10001100011000110001100010101000100', W: '10001100011000110101101011101110001', X: '10001100010101000100010101000110001',
  Y: '10001100010101000100001000010000100', Z: '11111000010001000100010001000011111',
  '0': '01110100011001110101110011000101110', '1': '00100011000010000100001000010001110', '2': '01110100010000100010001000100011111',
  '3': '11110000010000101110000010000111110', '4': '00010001100101010010111110001000010', '5': '11111100001000011110000010000111110',
  '6': '01110100001000011110100011000101110', '7': '11111000010001000100010000100001000', '8': '01110100011000101110100011000101110',
  '9': '01110100011000101111000010000101110', '-': '00000000000000011111000000000000000', '_': '00000000000000000000000000000011111', '.': '00000000000000000000000000110001100',
  '/': '00001000100001000100010000100010000', ':': '00000011000110000000011000110000000', '+': '00000001000010011111001000010000000'
};
const text = (canvas, label, left, top, size = 2, color = [223, 227, 213]) => {
  for (let characterIndex = 0; characterIndex < label.length; characterIndex++) {
    const glyph = font[label[characterIndex].toUpperCase()];
    if (!glyph) continue;
    for (let row = 0; row < 7; row++) for (let column = 0; column < 5; column++) if (glyph[row * 5 + column] === '1') {
      for (let offsetY = 0; offsetY < size; offsetY++) for (let offsetX = 0; offsetX < size; offsetX++) canvas.put(left + characterIndex * size * 6 + column * size + offsetX, top + row * size + offsetY, color);
    }
  }
};
const rectangle = (canvas, left, top, width, height, color) => canvas.rect(left, top, width, height, () => color);
const board = (width, height) => {
  const canvas = makeCanvas(width, height);
  rectangle(canvas, 0, 0, width, height, [19, 28, 33]);
  return canvas;
};
const blit = (canvas, image, left, top, scale = 1) => {
  for (let pixelY = 0; pixelY < image.h; pixelY++) for (let pixelX = 0; pixelX < image.w; pixelX++) {
    const address = (pixelY * image.w + pixelX) * 4;
    if (!image.data[address + 3]) continue;
    for (let offsetY = 0; offsetY < scale; offsetY++) for (let offsetX = 0; offsetX < scale; offsetX++) canvas.put(left + pixelX * scale + offsetX, top + pixelY * scale + offsetY, Array.from(image.data.slice(address, address + 4)));
  }
};
const save = (name, image) => writeSafe(path.join(art, name), encodePNG(image.w, image.h, image.data || image.buf));
const iconPath = path.join(resourceRoot, 'textures/item/apple_material.png');
if (process.argv.includes('--write-icon')) {
  const image = render(runtime.model, runtime.textures, { width: 32, height: 32, yaw: -18, pitch: 10, fit: true, padding: 2, unlit: false });
  const outlined = new Uint8Array(image.data);
  for (let pixelY = 1; pixelY < 31; pixelY++) for (let pixelX = 1; pixelX < 31; pixelX++) {
    const address = (pixelY * 32 + pixelX) * 4;
    if (image.data[address + 3]) continue;
    if ([[-1, 0], [1, 0], [0, -1], [0, 1]].some(([offsetX, offsetY]) => image.data[((pixelY + offsetY) * 32 + pixelX + offsetX) * 4 + 3])) {
      outlined.set([49, 34, 27, 255], address);
    }
  }
  writeSafe(iconPath, encodePNG(32, 32, outlined));
}
const icon = decodePNG(iconPath);
const views = [['FRONT - NORTH', 0], ['BACK - SOUTH', 180], ['LEFT - WEST', -90], ['RIGHT - EAST', 90]];
const four = board(1120, 1120);
text(four, 'GOLDEN APPLE CORE', 28, 28, 4, [248, 218, 149]);
text(four, 'FINAL RUNTIME MODEL / ORTHOGRAPHIC', 30, 69, 2);
const viewStats = {};
views.forEach(([label, yaw], index) => {
  const left = 20 + index % 2 * 550;
  const top = 108 + Math.floor(index / 2) * 483;
  rectangle(four, left, top, 530, 466, [28, 39, 43]);
  rectangle(four, left, top, 530, 2, [80, 94, 88]);
  text(four, `${index + 1} ${label}`, left + 18, top + 18, 2);
  const image = render(runtime.model, runtime.textures, { width: 500, height: 418, yaw, pitch: 0, span: 15.1 });
  blit(four, image, left + 15, top + 44);
  const name = ['front', 'back', 'left', 'right'][index];
  save(`${name}.png`, image);
  viewStats[name] = { yaw, pitch: 0, visibleFaces: image.visibleFaces, pixels: image.owners.reduce((sum, owner) => sum + (owner >= 0 ? 1 : 0), 0), clipped: Array.from({ length: image.w }, (_, pixelX) => image.data[pixelX * 4 + 3] || image.data[((image.h - 1) * image.w + pixelX) * 4 + 3]).some(Boolean) };
});
text(four, `${runtime.model.elements.length} PARTS / ${runtime.model.elements.length * 6} FACE ISLANDS / 1.75 PX PER UNIT`, 28, 1080, 2, [158, 180, 168]);
save('four_views.png', four);

const iso = board(1180, 820);
text(iso, 'GOLDEN APPLE CORE', 30, 27, 4, [248, 218, 149]);
text(iso, 'FTBOSS:APPLE_MATERIAL / REFINED RAW MATERIAL', 32, 69, 2);
rectangle(iso, 24, 108, 715, 648, [28, 39, 43]);
const oblique = render(runtime.model, runtime.textures, { width: 700, height: 620, yaw: -30, pitch: 17, span: 15.7 });
blit(iso, oblique, 30, 113);
save('oblique.png', oblique);
text(iso, '32X32 GUI ICON', 780, 124, 2, [248, 218, 149]);
blit(iso, icon, 790, 163, 9);
text(iso, 'GOLD RIND', 780, 493, 2);
text(iso, 'FIBROUS CUT FLESH', 780, 530, 2);
text(iso, 'RECESSED SEED BAYS', 780, 567, 2);
text(iso, 'STEM + VEINED LEAF', 780, 604, 2);
text(iso, 'RUNTIME JSON + PNG / ROTATION + FACE UV / OFFLINE PREVIEW', 30, 784, 2, [158, 180, 168]);
save('showcase.png', iso);

const comparison = board(1120, 660);
text(comparison, 'GUI ICON / MODEL CONSISTENCY', 28, 28, 3, [248, 218, 149]);
text(comparison, 'BEFORE 16X16', 40, 106, 2);
const backup = path.join(root, '_work/boss-materials-refine-20261021/apple/backup-before-refine/src/main/resources/assets/ftboss/textures/item/apple_material.png');
const oldIcon = decodePNG(backup);
blit(comparison, oldIcon, 65, 168, 12);
text(comparison, 'AFTER 32X32', 345, 106, 2);
blit(comparison, icon, 345, 147, 11);
text(comparison, 'RUNTIME 3D', 788, 106, 2);
const matching = render(runtime.model, runtime.textures, { width: 300, height: 370, yaw: -18, pitch: 10, fit: true, padding: 10 });
blit(comparison, matching, 780, 146);
text(comparison, 'NATIVE', 346, 550, 2);
blit(comparison, icon, 440, 537);
text(comparison, '16X16 CHECK', 530, 550, 2);
const small = { w: 16, h: 16, data: new Uint8Array(16 * 16 * 4) };
for (let pixelY = 0; pixelY < 16; pixelY++) for (let pixelX = 0; pixelX < 16; pixelX++) {
  const source = ((pixelY * 2 + 1) * 32 + pixelX * 2 + 1) * 4;
  small.data.set(icon.data.slice(source, source + 4), (pixelY * 16 + pixelX) * 4);
}
blit(comparison, small, 702, 530, 3);
text(comparison, 'ICON IS RENDERED FROM THE SAME FINAL MESH AND TEXTURE', 28, 616, 2, [158, 180, 168]);
save('icon_comparison.png', comparison);

const reverse = render(runtime.model, runtime.textures, { width: 700, height: 700, yaw: 145, pitch: 15, fit: true, padding: 45 });
save('oblique_back.png', reverse);
const atlasBoard = board(720, 680);
text(atlasBoard, 'APPLE / 128X128 UV ATLAS', 25, 22, 3, [248, 218, 149]);
blit(atlasBoard, runtime.textures.atlas, 104, 98, 4);
text(atlasBoard, `${runtime.model.elements.length * 6} UNIQUE ISLANDS / 1 PX GUTTERS`, 24, 635, 2);
save('atlas_preview.png', atlasBoard);
const hash = file => crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex');
const report = { source: 'final src/main/resources JSON and PNG', renderer: 'apple/raster.cjs', transforms: ['single-axis element rotation', 'Java rescale', 'Java face vertex and UV orientation', 'UV rotation', 'opaque alpha test', 'z-buffer', 'orthographic projection'], modelSha256: hash(path.join(resourceRoot, 'models/item/boss_materials/apple_material.json')), textureSha256: hash(path.join(resourceRoot, 'textures/item/boss_materials/apple.png')), iconSha256: hash(iconPath), views: viewStats };
writeSafe(path.join(art, 'render_manifest.json'), JSON.stringify(report, null, 2) + '\n');
console.log(JSON.stringify(report, null, 2));
