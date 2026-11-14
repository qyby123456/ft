const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');
const { makeCanvas, makePacker, checks, writeSafe } = require('./lib_itemtex.cjs');
const { encodePNG } = require('./png.cjs');
const spec = require('./benger_weapon_spec.json');

const palette = {
  iron: [52, 63, 73], edge: [105, 121, 132], ivory: [223, 221, 197],
  ivoryEdge: [245, 239, 215], gold: [179, 138, 74], goldEdge: [230, 199, 127],
  leather: [114, 59, 55], wrap: [166, 91, 74], wood: [69, 76, 67],
  amber: [243, 185, 77], black: [22, 31, 38], redEdge: [199, 132, 97]
};
const round = value => Number(value.toFixed(4));
function generate(assetsRoot = path.resolve(__dirname, '../../src/main/resources/assets/ftboss'), artRoot = path.resolve(__dirname, '../../art/benger_weapon')) {
  const canvas = makeCanvas(128, 128);
  const packer = makePacker([{ x: 0.1, w: 15.8, top: 0.1, bottom: 15.9 }]);
  const islands = [];
  const groupMap = new Map();
  const elements = spec.parts.map((part, index) => {
    if (!groupMap.has(part.group)) groupMap.set(part.group, []);
    groupMap.get(part.group).push(index);
    const [width, height, depth] = part.to.map((value, axis) => round(value - part.from[axis]));
    for (const [kind, faceWidth, faceHeight] of [['front', width, height], ['side', depth, height], ['cap', width, depth]]) {
      islands.push({ key: index + '_' + kind, index, kind, part, width: Math.max(0.2, faceWidth * 0.18), height: Math.max(0.2, faceHeight * 0.18), faceWidth, faceHeight });
    }
    return { name: part.name, from: part.from, to: part.to, ...(part.rotation ? { rotation: part.rotation } : {}), faces: {} };
  });
  islands.sort((first, second) => second.height - first.height || second.width - first.width);
  const rectangles = {};
  for (const island of islands) {
    const packed = packer.alloc(island.key, island.width + 0.3, island.height + 0.3);
    const rectangle = [packed[0] + 0.15, packed[1] + 0.15, packed[2] - 0.15, packed[3] - 0.15].map(round);
    rectangles[island.key] = rectangle;
    const [left, top, right, bottom] = rectangle.map(value => value * 8);
    const base = palette[island.part.material];
    for (let row = Math.floor(top) - 1; row <= Math.ceil(bottom) + 1; row++) {
      for (let column = Math.floor(left) - 1; column <= Math.ceil(right) + 1; column++) {
        const horizontal = Math.max(0, Math.min(1, (column + 0.5 - left) / (right - left)));
        const vertical = Math.max(0, Math.min(1, (row + 0.5 - top) / (bottom - top)));
        const grain = (column * 17 + row * 31 + island.index * 13) % 7 - 3;
        let brightness = 0.88 + 0.19 * (1 - Math.abs(horizontal - 0.35));
        if (island.part.material === 'wood') brightness += Math.floor(horizontal * 9 + vertical * 2) % 3 === 0 ? -0.12 : 0.03;
        if (['leather', 'wrap', 'redEdge'].includes(island.part.material)) {
          brightness += Math.abs((vertical * 4 + horizontal * 0.5) % 1 - 0.5) < 0.08 ? 0.15 : -0.025;
          if (horizontal < 0.14 && Math.floor(vertical * 12) % 2 === 0) brightness += 0.12;
        }
        if (island.part.material.startsWith('ivory')) brightness += Math.max(0, 0.08 - Math.abs(horizontal - 0.3) * 0.2);
        if (island.part.name.includes('眼')) brightness = 1.02 - Math.abs(horizontal - 0.35) * 0.12;
        const edge = Math.min(column + 0.5 - left, right - column - 0.5, row + 0.5 - top, bottom - row - 0.5);
        if (right - left >= 5 && bottom - top >= 5 && edge < 0.85) brightness += row < top + 1 || column < left + 1 ? 0.12 : -0.14;
        canvas.put(column, row, base.map(value => value * brightness + grain));
      }
    }
    if (island.width > 0.201 && island.height > 0.201) assert(Math.abs(island.width / island.height - island.faceWidth / island.faceHeight) < 0.002, island.part.name + ' stretched UV');
  }
  for (const [index, element] of elements.entries()) {
    const face = rectangle => ({ uv: rectangle, texture: '#atlas' });
    const reverse = rectangle => [rectangle[2], rectangle[1], rectangle[0], rectangle[3]];
    const front = rectangles[index + '_front'];
    const side = rectangles[index + '_side'];
    const cap = rectangles[index + '_cap'];
    element.faces = { north: face(reverse(front)), south: face(front), east: face(side), west: face(reverse(side)), up: face(cap), down: face(reverse(cap)) };
  }
  assert.deepEqual(checks.mirror(elements), []);
  assert.deepEqual(checks.noise(elements), []);
  const model = { ambientocclusion: false, gui_light: 'front', textures: { atlas: 'ftboss:item/benger_shepherd_fang', particle: 'ftboss:item/benger_shepherd_fang_particle' }, elements, display: spec.display };
  const texture = encodePNG(128, 128, canvas.buf);
  const particle = makeCanvas(16, 16);
  particle.rect(0, 0, 16, 16, (horizontal, vertical) => horizontal < 0.3 ? palette.ivory : vertical > 0.82 ? palette.gold : palette.iron);
  const write = (filename, data) => { fs.mkdirSync(path.dirname(filename), { recursive: true }); writeSafe(filename, data); };
  write(path.join(assetsRoot, 'models/item/dog_beating_stick.json'), JSON.stringify(model, null, 2) + '\n');
  write(path.join(assetsRoot, 'textures/item/benger_shepherd_fang.png'), texture);
  write(path.join(assetsRoot, 'textures/item/benger_shepherd_fang_particle.png'), encodePNG(16, 16, particle.buf));
  const uuid = index => 'be09e000-0000-4000-8000-' + String(index).padStart(12, '0');
  const blockbench = {
    meta: { format_version: '4.10', model_format: 'java_block', box_uv: false }, name: spec.name, model_identifier: 'dog_beating_stick', resolution: { width: 128, height: 128 },
    elements: elements.map((element, index) => ({ name: element.name, type: 'cube', uuid: uuid(index), from: element.from, to: element.to, origin: element.rotation?.origin || [8, 8, 8], rotation: [0, 0, element.rotation?.angle || 0], box_uv: false, autouv: 0, rescale: false, faces: Object.fromEntries(Object.entries(element.faces).map(([side, face]) => [side, { uv: face.uv.map(value => round(value * 8)), texture: 0 }])) })),
    outliner: Array.from(groupMap, ([name, indices], index) => ({ name, origin: [8, 8, 8], rotation: [0, 0, 0], uuid: uuid(1000 + index), export: true, visibility: true, children: indices.map(uuid) })),
    textures: [{ name: 'benger_shepherd_fang.png', id: 'atlas', uuid: uuid(2000), namespace: 'ftboss', folder: 'item', width: 128, height: 128, uv_width: 128, uv_height: 128, mode: 'bitmap', source: 'data:image/png;base64,' + texture.toString('base64') }], display: model.display, front_gui_light: true, ambientocclusion: false
  };
  write(path.join(artRoot, 'Benger_Shepherd_Fang.bbmodel'), JSON.stringify(blockbench, null, 2) + '\n');
  write(path.join(artRoot, 'preview-data.json'), JSON.stringify({ model, texture: { width: 128, height: 128, rgba: Array.from(canvas.buf) } }) + '\n');
  write(path.join(artRoot, 'uv-report.json'), JSON.stringify({ cubes: elements.length, groups: groupMap.size, islands: islands.length, textureSize: 128, mirroredFaces: true, tinyIslands: islands.filter(island => island.width <= 0.201 || island.height <= 0.201).length }, null, 2) + '\n');
  console.log('Benger: ' + elements.length + ' cubes / ' + islands.length + ' UV islands -> ' + assetsRoot);
  return model;
}
if (require.main === module) generate(process.argv[2], process.argv[3]);
module.exports = { generate };
