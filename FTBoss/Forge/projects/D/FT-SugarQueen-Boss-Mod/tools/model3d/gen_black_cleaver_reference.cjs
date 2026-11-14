'use strict';

const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const assert = require('node:assert/strict');
const { makeCanvas, makePacker } = require('./lib_itemtex.cjs');
const space = require('./cube_space.cjs');
const { WINGS, FPS, buildAnimations, buildEffects } = require('./cleaver_reference_motion.cjs');
const { THIRD_PERSON_GRIP_Y, thirdPersonDisplay } = require('./cleaver_hold.cjs');

const ROOT = path.resolve(__dirname, '../..');
const outputArgument = process.argv.indexOf('--out');
const OUT = outputArgument < 0 ? path.join(ROOT, 'art/black_cleaver_reference') : path.resolve(process.argv[outputArgument + 1]);
const SOURCE = 'C:/Users/YOUR_USERNAME/OneDrive/桌面/自绘黑切.png';
const ATLAS = 256;
const DENSITY = 2;
const GRID = 0.5;
const BASIS = Math.SQRT1_2;
const bones = [];
const byName = new Map();
const pieces = [];
const texture = makeCanvas(ATLAS, ATLAS);
const glow = makeCanvas(ATLAS, ATLAS);
const seamGlow = makeCanvas(ATLAS, ATLAS);
const normalGlow = makeCanvas(ATLAS, ATLAS);
const coreGlow = makeCanvas(ATLAS, ATLAS);
const runeGlow = makeCanvas(ATLAS, ATLAS);
const edgeGlow = makeCanvas(ATLAS, ATLAS);
const packer = makePacker([{ x: 78, w: 175, top: 2, bottom: 253 }]);
const tiles = new Map();
const PALETTE = {
  iron: [78, 62, 69], ironDark: [46, 35, 46], spine: [57, 44, 55], silver: [183, 178, 187], bevel: [135, 128, 142],
  edge: [204, 24, 43], blackEdge: [45, 31, 42], leather: [72, 45, 44],
  grip: [27, 25, 34], collar: [63, 57, 69], red: [167, 17, 35],
  ember: [213, 28, 43], backing: [88, 9, 23], bolt: [140, 130, 139]
};
const DEPTH = { iron: 4.8, ironDark: 4.0, spine: 5.4, bevel: 3.65, silver: 2.2, edge: 0.8, blackEdge: 1.8 };
const SHAPES = [
  {
    name: 'main', parent: 'blade', upper: 'blade_upper_wing', lower: 'blade_lower_wing',
    upperY: 17.5, lowerY: 4.8, upperPivot: [-3.8, 17.5, 0], lowerPivot: [-4.5, 4.8, 0],
    polygon: [[20.8,0.5],[21.3,1.1],[21.3,10.3],[19.5,11.3],[17.4,13.3],[13.3,14.0],[10.9,16.1],[8.0,19.8],[5.3,20.5],[5.3,11.4],[6.15,10.8],[6.3,7.5],[7.5,5.5],[10.1,3.0],[12.7,1.6],[16.2,0.5]],
    cutting: [[20.5,0.8],[16.2,0.8],[12.8,1.85],[10.35,3.25],[7.8,5.7],[6.6,7.6],[6.4,11.2],[5.65,11.7],[5.65,19.95],[7.25,19.65]],
    scar: [[15.55,11.1],[16.45,10.0],[17.8,9.7],[18.7,8.7],[18.65,7.1]]
  },
  {
    name: 'secondary', parent: 'blade_right', upper: 'blade_right_upper_wing', lower: 'blade_right_lower_wing',
    upperY: 17.8, lowerY: 10.2, upperPivot: [2.1, 17.8, 0], lowerPivot: [5.45, 10.2, 0],
    polygon: [[22.0,11.4],[27.1,11.4],[29.1,12.9],[30.3,14.4],[30.0,16.5],[28.4,18.65],[25.8,21.05],[23.5,22.5],[19.0,22.5],[19.1,21.6],[21.55,20.15],[23.8,18.0],[25.5,15.65],[24.2,14.3],[22.0,13.75]],
    cutting: [[29.35,13.35],[29.95,14.6],[29.65,16.35],[28.1,18.4],[25.55,20.8],[23.35,22.15],[19.45,22.15]],
    scar: []
  }
];

function bone(name, parent, pivot = [0,0,0]) {
  assert.ok(!byName.has(name));
  if (parent) assert.ok(byName.has(parent));
  const entry = { name, pivot: [...pivot] };
  if (parent) entry.parent = parent;
  bones.push(entry);
  byName.set(name, entry);
  return entry;
}

bone('root');
bone('grip_anchor', 'root');
bone('weapon', 'grip_anchor');
bone('handle', 'weapon');
bone('haft', 'weapon', [0,8,0]);
bone('axe_head', 'weapon', [0,15.5,0]);
bone('blade', 'axe_head', [-1.1,15.5,0]);
bone('blade_right', 'axe_head', [1.1,15.5,0]);
bone('rear_hook', 'axe_head', [4.6,11,0]);
bone('fittings', 'axe_head', [0,15.5,0]);
bone('ember_core', 'axe_head', [0,16,0]);
bone('pommel', 'weapon', [0,-12.4,0]);
bone('impact_socket', 'axe_head', [-12.5,9,0]);
for (const shape of SHAPES) {
  bone(shape.upper, shape.parent, shape.upperPivot);
  bone(shape.lower, shape.parent, shape.lowerPivot);
}

function sourcePoint(horizontal, vertical, depth = 0) {
  return [(horizontal + vertical - 32) * BASIS, (horizontal - vertical + 11) * BASIS, depth];
}

function inside(point, polygon) {
  let result = false;
  for (let index = 0, previous = polygon.length - 1; index < polygon.length; previous = index++) {
    const first = polygon[index];
    const second = polygon[previous];
    if ((first[1] > point[1]) !== (second[1] > point[1]) &&
        point[0] < (second[0] - first[0]) * (point[1] - first[1]) / (second[1] - first[1]) + first[0]) result = !result;
  }
  return result;
}

function lineDistance(point, route) {
  let best = Infinity;
  for (let index = 1; index < route.length; index++) {
    const start = route[index - 1];
    const delta = route[index].map((value, axis) => value - start[axis]);
    const lengthSquared = delta[0] ** 2 + delta[1] ** 2;
    const fraction = Math.max(0, Math.min(1, ((point[0]-start[0])*delta[0] + (point[1]-start[1])*delta[1]) / lengthSquared));
    best = Math.min(best, Math.hypot(point[0]-start[0]-fraction*delta[0], point[1]-start[1]-fraction*delta[1]));
  }
  return best;
}

function materialAt(shape, horizontal, vertical) {
  const distance = lineDistance([horizontal, vertical], shape.cutting);
  if (distance < 0.55) return 'edge';
  if (shape.name === 'main' && distance < 1.15) return 'silver';
  if (shape.name === 'main' && distance < 1.95) return 'bevel';
  if (shape.name === 'secondary' && distance < 1.45) return 'blackEdge';
  if (shape.name === 'main' && horizontal > 19.7 && vertical < 10.15) return 'spine';
  return shape.name === 'main' ? 'iron' : 'ironDark';
}

function ownerAt(shape, horizontal, vertical) {
  const height = sourcePoint(horizontal, vertical)[1];
  if (height > shape.upperY) return shape.upper;
  if (height < shape.lowerY) return shape.lower;
  return shape.parent;
}

function colorFor(material, horizontal = 0, vertical = 0) {
  const base = PALETTE[material];
  const band = (Math.floor(horizontal / 3) + Math.floor(vertical / 5)) % 4;
  let factor = [0.98, 1.0, 1.04, 1.0][band];
  if (material === 'silver') factor = [1.09, 0.97, 0.91, 1.03][band];
  if (material === 'edge') factor = [0.93, 1.03, 1.12, 0.94][band];
  return base.map(channel => Math.max(0, Math.min(255, Math.round(channel * factor))));
}

for (let pixelY = 0; pixelY < 64; pixelY++) {
  for (let pixelX = 0; pixelX < 64; pixelX++) {
    const horizontal = (pixelX + 0.5) / DENSITY;
    const vertical = (pixelY + 0.5) / DENSITY;
    const shape = SHAPES.find(candidate => inside([horizontal, vertical], candidate.polygon));
    const material = shape ? materialAt(shape, horizontal, vertical) : 'ironDark';
    let color = colorFor(material, horizontal, vertical);
    let emission = null;
    if (shape) {
      const scar = lineDistance([horizontal, vertical], shape.scar);
      const branch = shape.name === 'main' ? lineDistance([horizontal,vertical], [[16.4,10.0],[16.0,8.8]]) : Infinity;
      if (Math.min(scar, branch) < 0.43 && material === 'iron') {
        color = scar < 0.20 ? [227,26,47] : [129,9,30];
        emission = [233,43,57];
        runeGlow.put(2 + pixelX, 2 + pixelY, emission);
      }
      if (material === 'edge') {
        emission = [235,35,48];
        edgeGlow.put(2 + pixelX, 2 + pixelY, emission);
      }
      const plateBorder = lineDistance([horizontal,vertical], [...shape.polygon,shape.polygon[0]]);
      if (plateBorder < 0.4 && material === 'iron') color = [61,43,60];
      if (material === 'iron' && horizontal > 18.8 && vertical < 8.7) color = [66,49,60];
    }
    texture.put(2 + pixelX, 2 + pixelY, color);
    if (emission) glow.put(2 + pixelX, 2 + pixelY, emission);
  }
}

function tile(material, width, height, direction) {
  const pixelWidth = Math.max(1, Math.round(width * DENSITY));
  const pixelHeight = Math.max(1, Math.round(height * DENSITY));
  const key = `${material}:${pixelWidth}:${pixelHeight}:${direction}`;
  if (tiles.has(key)) return tiles.get(key);
  const area = packer.alloc(key, pixelWidth + 2, pixelHeight + 2);
  const startX = Math.round(area[0]) + 1;
  const startY = Math.round(area[1]) + 1;
  assert.ok(startX + pixelWidth + 1 <= ATLAS && startY + pixelHeight + 1 <= ATLAS);
  const light = { north: 1.02, south: 1.0, east: 0.82, west: 0.88, up: 1.1, down: 0.7 }[direction];
  for (let pixelY = -1; pixelY <= pixelHeight; pixelY++) {
    for (let pixelX = -1; pixelX <= pixelWidth; pixelX++) {
      const edge = pixelX <= 0 || pixelY <= 0;
      const sideFace = !['north','south'].includes(direction);
      const thicknessIndex = ['east','west'].includes(direction) ? pixelX : pixelY;
      const thicknessPixels = ['east','west'].includes(direction) ? pixelWidth : pixelHeight;
      const rim = sideFace && ['iron','ironDark','spine','bevel','silver'].includes(material) &&
        (thicknessIndex === 0 || thicknessIndex === thicknessPixels - 1);
      const factor = rim ? 1.18 : (edge ? 1.03 : 0.92);
      const color = PALETTE[material].map(channel => Math.round(channel * light * factor));
      texture.put(startX + pixelX, startY + pixelY, color);
      if (['red','ember','edge'].includes(material)) glow.put(startX + pixelX, startY + pixelY, color);
      if (material === 'backing') seamGlow.put(startX + pixelX, startY + pixelY, [174,13,36]);
      if (material === 'ember') normalGlow.put(startX + pixelX, startY + pixelY, color);
      if (material === 'ember') coreGlow.put(startX + pixelX, startY + pixelY, [255,95,80]);
      if (material === 'red') runeGlow.put(startX + pixelX, startY + pixelY, [213,33,51]);
      if (material === 'edge') edgeGlow.put(startX + pixelX, startY + pixelY, [235,35,48]);
    }
  }
  const result = { uv: [startX,startY], uv_size: [pixelWidth,pixelHeight] };
  tiles.set(key, result);
  return result;
}

function addBox(owner, label, center, size, material, rotation = [0,0,0], chart = null) {
  assert.ok(size.every(value => Number.isFinite(value) && value > 0));
  const cube = {
    origin: center.map((coordinate, axis) => Number((coordinate - size[axis]/2).toFixed(6))),
    size: size.map(value => Number(value.toFixed(6))),
    uv: {}
  };
  if (rotation.some(value => value !== 0)) {
    cube.pivot = [...center];
    cube.rotation = space.geckoRotation(rotation);
  }
  const dimensions = {
    north: [size[0],size[1]], south: [size[0],size[1]],
    east: [size[2],size[1]], west: [size[2],size[1]],
    up: [size[0],size[2]], down: [size[0],size[2]]
  };
  for (const [direction, dimension] of Object.entries(dimensions)) {
    if (chart && (direction === 'north' || direction === 'south')) {
      const startX = 2 + chart[0]*DENSITY;
      const startY = 2 + chart[1]*DENSITY;
      const width = chart[2]*DENSITY;
      const height = chart[3]*DENSITY;
      cube.uv[direction] = direction === 'north'
        ? { uv: [startX,startY], uv_size: [width,height] }
        : { uv: [startX+width,startY], uv_size: [-width,height] };
    } else cube.uv[direction] = structuredClone(tile(material,...dimension,direction));
  }
  const target = byName.get(owner);
  (target.cubes ||= []).push(cube);
  pieces.push({ label, owner, material, cube });
  return cube;
}

function addStrip(shape, owner, material, startX, endX, row, backing = false) {
  const center = sourcePoint((startX + endX)/2, row + GRID/2);
  const label = `${shape.name}_${owner}_${material}_${pieces.length}`;
  const thickness = backing ? 0.7 : DEPTH[material] - (Object.hasOwn(WINGS,owner) && material !== 'edge' ? 0.4 : 0);
  addBox(owner,label,center,[endX-startX,GRID,thickness],material,[0,0,45],
    backing ? null : [startX,row,endX-startX,GRID]);
}

for (const shape of SHAPES) {
  for (let row = 0; row < 32; row += GRID) {
    let run = null;
    const flush = endX => {
      if (run) addStrip(shape,run.owner,run.material,run.start,endX,row);
      run = null;
    };
    for (let column = 0; column <= 32; column += GRID) {
      const point = [column+GRID/2,row+GRID/2];
      const occupied = column < 32 && inside(point,shape.polygon);
      const owner = occupied ? ownerAt(shape,...point) : null;
      const material = occupied ? materialAt(shape,...point) : null;
      if (run && (!occupied || owner !== run.owner || material !== run.material)) flush(column);
      if (occupied && !run) run = { start: column, owner, material };
    }
    let bridge = null;
    for (let column = 0; column <= 32; column += GRID) {
      const point = [column+GRID/2,row+GRID/2];
      const height = sourcePoint(...point)[1];
      const occupied = column < 32 && inside(point,shape.polygon) &&
        Math.min(Math.abs(height-shape.upperY),Math.abs(height-shape.lowerY)) < 1.65;
      if (bridge !== null && !occupied) {
        addStrip(shape,shape.parent,'backing',bridge,column,row,true);
        bridge = null;
      }
      if (occupied && bridge === null) bridge = column;
    }
  }
}

for (const vertical of [4.4,8.5]) {
  const shape = SHAPES[0];
  const owner = ownerAt(shape,20.3,vertical);
  for (const side of [-1,1]) {
    const center = sourcePoint(20.3,vertical,side * (2.8-(Object.hasOwn(WINGS,owner) ? 0.2 : 0)));
    addBox(owner,`spine_pin_${vertical}_${side}`,center,[0.54,0.54,0.38],'bolt',[0,0,45]);
  }
}

addBox('handle','grip_core',[0,-5.5,0],[2.3,14.2,2.2],'grip');
addBox('haft','haft_core',[0,12.1,0],[2.1,21.0,2.05],'leather');
for (let index = 0; index < 9; index++) {
  const height = 2.3 + index*1.32;
  for (const side of [-1,1]) {
    addBox('haft',`leather_wrap_${index}_${side}`,[0,height,side*1.09],[2.52,0.48,0.28],'leather',[0,0,32]);
  }
}
for (let index = 0; index < 5; index++) {
  const height = -10.55 + index*2.25;
  addBox('handle',`grip_collar_${index}`,[0,height-0.48,0],[2.9,0.48,3.0],'collar');
  for (const side of [-1,1]) {
    addBox('handle',`grip_red_inlay_${index}_${side}`,[0,height,side*1.15],[1.15,1.22,0.3],'red',[0,0,45]);
  }
}
addBox('pommel','pommel_ring',[0,-12.32,0],[3.1,0.95,3.05],'collar');
addBox('pommel','pommel_dark',[0,-12.95,0],[1.8,1.8,2.1],'grip',[0,0,45]);
for (const side of [-1,1]) addBox('pommel',`pommel_red_${side}`,[0,-12.94,side*1.1],[0.95,1.1,0.3],'red',[0,0,45]);
for (const height of [-0.3,14.5,17.6,21.9]) addBox('fittings',`shaft_band_${height}`,[0,height,0],[2.75,0.75,2.75],'collar');
addBox('axe_head','compact_socket',[0,15.8,0],[3.3,3.8,5.2],'ironDark');
for (const side of [-1,1]) {
  addBox('fittings',`axle_bezel_${side}`,[0,16,side*2.78],[2.35,2.35,0.44],'collar',[0,0,45]);
  addBox('ember_core',`axle_red_${side}`,[0,16,side*3.13],[1.1,1.1,0.34],'ember',[0,0,45]);
  addBox('fittings',`upper_red_${side}`,[0,20.65,side*1.08],[1.3,1.3,0.3],'red',[0,0,45]);
}
addBox('fittings','crown_dark',[0,22.85,0],[1.9,1.5,2.65],'grip',[0,0,45]);
for (const side of [-1,1]) addBox('fittings',`crown_ruby_${side}`,[0,22.82,side*1.43],[1.0,0.95,0.3],'red',[0,0,45]);

const gripOffset = 5.5;
for (const entry of bones) {
  if (!['root','grip_anchor','weapon'].includes(entry.name)) entry.pivot[1] += gripOffset;
  for (const cube of entry.cubes || []) {
    cube.origin[1] = Number((cube.origin[1]+gripOffset).toFixed(6));
    if (cube.pivot) cube.pivot[1] += gripOffset;
  }
}

const geometry = {
  format_version: '1.12.0',
  'minecraft:geometry': [{
    description: { identifier: 'geometry.black_cleaver', texture_width: ATLAS, texture_height: ATLAS,
      visible_bounds_width: 5, visible_bounds_height: 6, visible_bounds_offset: [0,0.45,0] },
    bones
  }]
};
const animations = buildAnimations();
const effects = buildEffects();

fs.mkdirSync(OUT, { recursive: true });
const writeJSON = (name, value) => fs.writeFileSync(path.join(OUT,name), JSON.stringify(value,null,2)+'\n');
writeJSON('black_cleaver.geo.json',geometry);
writeJSON('black_cleaver.animation.json',{ format_version: '1.8.0', animations });
writeJSON('black_cleaver.effects.json',effects);
texture.save(path.join(OUT,'black_cleaver_geo.png'));
glow.save(path.join(OUT,'black_cleaver_blood_emissive.png'));
seamGlow.save(path.join(OUT,'black_cleaver_blood_seam_emissive.png'));
normalGlow.save(path.join(OUT,'black_cleaver_emissive.png'));
coreGlow.save(path.join(OUT,'black_cleaver_core_emissive.png'));
runeGlow.save(path.join(OUT,'black_cleaver_rune_emissive.png'));
edgeGlow.save(path.join(OUT,'black_cleaver_edge_emissive.png'));
fs.copyFileSync(SOURCE,path.join(OUT,'reference.png'));

function uuid(name) {
  const hex = crypto.createHash('sha256').update(name).digest('hex');
  return `${hex.slice(0,8)}-${hex.slice(8,12)}-4${hex.slice(13,16)}-a${hex.slice(17,20)}-${hex.slice(20,32)}`;
}
const elementIds = new Map(pieces.map(piece => [piece.cube,uuid(piece.label)]));
const groups = new Map(bones.map(entry => [entry.name, {
  name: entry.name, uuid: uuid(entry.name), origin: space.geckoNativePosition(entry.pivot), rotation: [0,0,0],
  export: true, visibility: true, isOpen: false, children: []
}]));
for (const entry of bones) {
  const group = groups.get(entry.name);
  if (entry.parent) groups.get(entry.parent).children.push(group);
  for (const cube of entry.cubes || []) group.children.push(elementIds.get(cube));
}
const blockbenchAnimations = Object.entries(animations).map(([name,animation]) => ({
  uuid: uuid(name), name, loop: animation.loop ? 'loop' : 'once', length: animation.animation_length,
  snapping: FPS, override: false, animators: Object.fromEntries(Object.entries(animation.bones).map(([boneName,channels]) => [
    uuid(boneName), { name: boneName, type: 'bone', keyframes: Object.entries(channels).flatMap(([channel,values]) => {
      const track = Array.isArray(values) ? { '0.0': values } : values;
      return Object.entries(track).map(([time,vector]) => ({
        uuid: uuid(`${name}:${boneName}:${channel}:${time}`), channel, time: Number(time),
        data_points: [Object.fromEntries(['x','y','z'].map((axis,index) =>
          [axis,String((channel === 'rotation' ? space.geckoNativeRotation(vector) : vector)[index])]))], interpolation: 'linear'
      }));
    }) }
  ]))
}));
const blockbench = {
  meta: { format_version: '4.10', model_format: 'geckolib_model', box_uv: false },
  name: '黑切 · 厚刃血怒动画版', model_identifier: 'black_cleaver', visible_box: [5,6,0.45],
  resolution: { width: ATLAS, height: ATLAS },
  elements: pieces.map(piece => ({
    name: piece.label, uuid: elementIds.get(piece.cube), type: 'cube', box_uv: false, export: true, visibility: true,
    from: piece.cube.origin.map((coordinate,axis) => axis === 0 ? -coordinate-piece.cube.size[axis] : coordinate),
    to: piece.cube.origin.map((coordinate,axis) => axis === 0 ? -coordinate : coordinate+piece.cube.size[axis]),
    origin: space.geckoNativePosition(piece.cube.pivot || [0,0,0]), rotation: space.geckoNativeRotation(piece.cube.rotation || [0,0,0]),
    faces: Object.fromEntries(Object.entries(piece.cube.uv).map(([direction,face]) => [direction, {
      uv: [face.uv[0],face.uv[1],face.uv[0]+face.uv_size[0],face.uv[1]+face.uv_size[1]], texture: 0
    }]))
  })),
  outliner: bones.filter(entry => !entry.parent).map(entry => groups.get(entry.name)),
  textures: [{ name: 'black_cleaver_geo.png', id: '0', uuid: uuid('texture'), path: '',
    source: 'data:image/png;base64,'+fs.readFileSync(path.join(OUT,'black_cleaver_geo.png')).toString('base64'),
    mode: 'bitmap', saved: false, particle: false, render_mode: 'default' }],
  animations: blockbenchAnimations
};
writeJSON('black_cleaver.bbmodel',blockbench);

const model = space.loadGeo(path.join(OUT,'black_cleaver.geo.json'),{ geckolib: true });
const modelCubes = space.listCubes(model);
const corners = modelCubes.flatMap(info => space.cubeCorners(model,info));
const bounds = [0,1,2].map(axis => [Math.min(...corners.map(point => point[axis])),Math.max(...corners.map(point => point[axis]))]);
const guiRotation = [0,0,-15];
const guiCenter = space.rotateXYZ(bounds.map((range,axis) => (range[0]+range[1])/2*(axis === 0 ? -1 : 1)),guiRotation);
const heldThird = thirdPersonDisplay();
const heldFirst = { rotation: [0,125,0], translation: [-0.7,-0.5,-4.5], scale: [0.55,0.55,0.55] };
const oppositeHand = display => ({ ...display,rotation: [display.rotation[0],display.rotation[1]-180,-display.rotation[2]] });
writeJSON('black_cleaver.item.json',{
  parent: 'builtin/entity', gui_light: 'front',
  textures: { particle: 'ftboss:item/black_cleaver_geo' },
  display: {
    thirdperson_righthand: heldThird, thirdperson_lefthand: oppositeHand(heldThird),
    firstperson_righthand: heldFirst, firstperson_lefthand: oppositeHand(heldFirst),
    gui: { rotation: guiRotation, translation: guiCenter.map(value => Number((-value*0.34).toFixed(6))), scale: [0.34,0.34,0.34] },
    ground: { rotation: [0,0,90], translation: [0,3,0], scale: [0.35,0.35,0.35] },
    fixed: { rotation: [0,180,0], translation: [0,-3.7,0], scale: [0.34,0.34,0.34] }
  }
});
let uvFaces = 0;
let opaqueSamples = 0;
for (const info of modelCubes) {
  for (const face of Object.values(info.cube.uv)) {
    const pixelX = Math.floor(face.uv[0] + face.uv_size[0]/2);
    const pixelY = Math.floor(face.uv[1] + face.uv_size[1]/2);
    assert.ok(texture.alphaAt(pixelX,pixelY) === 255, `${info.name}: blank texture`);
    opaqueSamples++;
    uvFaces++;
  }
}
const poseBounds = [];
for (const progress of [0,0.5,1]) {
  const pose = Object.fromEntries(Object.entries(WINGS).map(([name,sign]) => [name,{ rotation: [0,0,sign*11*progress] }]));
  const posed = modelCubes.flatMap(info => space.cubeCorners(model,info,pose));
  assert.ok(posed.every(point => point.every(Number.isFinite)));
  poseBounds.push({ progress, angle: progress*11, bounds: [0,1,2].map(axis => [
    Math.min(...posed.map(point => point[axis])),Math.max(...posed.map(point => point[axis]))
  ]) });
}
const report = {
  design: 'hand-drawn reference remake, not the DS v4 geometry',
  revision: '08_third_person_grip_height', animation_fps: FPS, blade_thickness: DEPTH,
  third_person_grip: [0,THIRD_PERSON_GRIP_Y,0],
  coordinate_convention: 'GeckoLib 4: reflected X, native rotations (-X,-Y,+Z); preview unreflects X',
  grip_origin: [0,0,0], grip_rebase_units: gripOffset,
  wing_surface_recess: 0.2,
  section_sizes: Object.fromEntries(['grip_core','haft_core','compact_socket'].map(label =>
    [label,pieces.find(piece => piece.label === label).cube.size])),
  source: SOURCE, source_sha256: crypto.createHash('sha256').update(fs.readFileSync(SOURCE)).digest('hex'),
  bones: bones.length, cubes: pieces.length, faces: uvFaces, texture: [ATLAS,ATLAS],
  bounds, size: bounds.map(range => range[1]-range[0]), uv_faces_with_opaque_centers: opaqueSamples,
  wings: WINGS, poses: poseBounds, animations: Object.keys(animations),
  runtime_replaced: false, game_tested: false, per_bone: bones.map(entry => ({ name: entry.name, cubes: (entry.cubes || []).length }))
};
writeJSON('report.json',report);
console.log(JSON.stringify({ output: OUT, bones: bones.length, cubes: pieces.length, size: report.size, faces: uvFaces },null,2));

if (process.argv.includes('--publish')) {
  const resources = path.join(ROOT,'src/main/resources/assets/ftboss');
  const entries = {
    'black_cleaver.geo.json': 'geo/black_cleaver.geo.json',
    'black_cleaver.animation.json': 'animations/black_cleaver.animation.json',
    'black_cleaver.effects.json': 'effects/black_cleaver.effects.json',
    'black_cleaver.item.json': 'models/item/black_cleaver.json'
  };
  for (const filename of ['black_cleaver_geo.png','black_cleaver_emissive.png','black_cleaver_blood_emissive.png',
    'black_cleaver_blood_seam_emissive.png','black_cleaver_core_emissive.png','black_cleaver_rune_emissive.png','black_cleaver_edge_emissive.png']) {
    entries[filename] = 'textures/item/'+filename;
  }
  const backup = path.join(ROOT,'_backup','black_cleaver_publish_'+new Date().toISOString().replaceAll(/[:.]/g,'-'));
  const manifest = [];
  for (const [filename,relative] of Object.entries(entries)) {
    const destination = path.join(resources,relative);
    if (fs.existsSync(destination)) {
      const previous = path.join(backup,relative);
      fs.mkdirSync(path.dirname(previous),{ recursive: true });
      fs.copyFileSync(destination,previous);
    }
    fs.mkdirSync(path.dirname(destination),{ recursive: true });
    fs.copyFileSync(path.join(OUT,filename),destination);
    manifest.push({ file: 'assets/ftboss/'+relative,sha256: crypto.createHash('sha256').update(fs.readFileSync(destination)).digest('hex') });
  }
  writeJSON('published-manifest.json',{ backup,resources: manifest,jar_deployed: false });
  console.log(`Published ${manifest.length} isolated cleaver resources; previous files backed up to ${backup}`);
}
