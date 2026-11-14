'use strict';

const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const {Canvas} = require('./png.cjs');
const OUT = path.resolve(process.argv[2] || 'out_compost_boss_v5');
const ATLAS = 512;
const DENSITY = 1.5;
const bones = [];
const parts = [];
const zero = [0, 0, 0];
const round = value => Math.round(value * 1000) / 1000;
const vector = values => values.map(round);
const palette = {
  compost: [115, 69, 41], ridge: [146, 91, 51], dark: [57, 37, 32],
  cloth: [152, 129, 83], clothDark: [100, 89, 61], straw: [205, 169, 78],
  leather: [70, 55, 43], metal: [57, 73, 64], gold: [187, 137, 59],
  cape: [140, 42, 53], capeDark: [90, 35, 47], ivory: [234, 224, 183],
  slime: [151, 207, 65], pupil: [215, 109, 39], wing: [171, 196, 166]
};
const rainbow = [[193, 68, 114], [211, 132, 58], [202, 184, 70],
  [79, 174, 114], [67, 159, 187], [144, 101, 192]];

function bone(name, parent, pivot) {
  const entry = {name, pivot};
  if (parent) entry.parent = parent;
  bones.push(entry);
}

function cube(name, owner, center, size, material, rotation = zero, layer = 0) {
  const origin = vector(center.map((value, axis) => value - size[axis] / 2));
  const entry = {origin, size: vector(size), uv: {}};
  if (rotation.some(value => value !== 0)) {
    entry.rotation = [-rotation[0],rotation[1],-rotation[2]];
    entry.pivot = center;
  }
  const parent = bones.find(entry => entry.name === owner);
  if (!parent) throw new Error(`Unknown bone ${owner}`);
  (parent.cubes ||= []).push(entry);
  parts.push({name, owner, material, layer, entry});
}

function pair(name, owner, center, size, material, rotation = zero) {
  for (const side of [-1, 1]) {
    const suffix = side < 0 ? 'l' : 'r';
    cube(`${name}_${suffix}`, owner.replace('{side}', suffix),
      [center[0] * side, center[1], center[2]], size, material,
      [rotation[0], rotation[1] * side, rotation[2] * side]);
  }
}

bone('root', null, zero);
bone('pole', 'root', [0, 18, 0]);
bone('pelvis', 'pole', [0, 18, 0]);
bone('waist', 'pelvis', [0, 21.5, 0]);
bone('torso', 'waist', [0, 25, 0]);
bone('crossbar', 'torso', [0, 32, 0]);
bone('head', 'torso', [0, 34.5, 0]);
for (const side of [-1, 1]) {
  const suffix = side < 0 ? 'l' : 'r';
  bone(`leg_${suffix}`, 'pelvis', [side * 4.8, 18, 0]);
  bone(`shin_${suffix}`, `leg_${suffix}`, [side * 4.8, 10, 0]);
  bone(`foot_${suffix}`, `shin_${suffix}`, [side * 4.8, 3, 0]);
  bone(`arm_${suffix}`, 'crossbar', [side * 10.4, 31.5, 0]);
  bone(`forearm_${suffix}`, `arm_${suffix}`, [side * 12.1, 24, 0]);
  bone(`hand_${suffix}`, `forearm_${suffix}`, [side * 12.3, 17.3, -0.6]);
}
bone('fork', 'hand_l', [-12.3, 17.3, -0.6]);
bone('shovel', 'hand_r', [12.3, 17.3, -0.6]);
bone('cannon', 'hand_r', [12.3, 17.3, -0.6]);
for (let index = 0; index < 4; index++) {
  bone(`cape${index + 1}`, index === 0 ? 'torso' : `cape${index}`,
    [0, 32 - index * 5.5, 5.9 + index * 0.8]);
}
for (const side of [-1, 1]) {
  const name = side < 0 ? 'fly1' : 'fly2';
  bone(name, 'head', [side * 13, 46 + side * 2, 0]);
  bone(`${name}_wing_l`, name, [side * 13 - 0.4, 46.8 + side * 2, 0]);
  bone(`${name}_wing_r`, name, [side * 13 + 0.4, 46.8 + side * 2, 0]);
}

const singles = [
  ['hip_sack', 'pelvis', [0,19.8,0], [12.8,6.2,8], 'clothDark'],
  ['waist_taper', 'waist', [0,23.4,0], [12,5.8,8.4], 'cloth'],
  ['barrel_chest', 'torso', [0,29.3,0], [16,10,9.8], 'cloth'],
  ['belt', 'waist', [0,22.2,0], [13.3,2.5,9.1], 'leather'],
  ['buckle_frame', 'waist', [0,22.2,-4.8], [3.9,2.9,1.1], 'gold'],
  ['buckle_inset', 'waist', [0,22.2,-5.5], [2.4,1.6,0.5], 'metal'],
  ['scarf_neck', 'torso', [0,34,0], [10.4,2.8,9], 'cape'],
  ['crest_frame', 'torso', [0,29.9,-6.2], [5.8,5.8,1.2], 'gold', [0,0,45]],
  ['crest_field', 'torso', [0,29.9,-7], [4.6,4.6,0.7], 'metal', [0,0,45]],
  ['crest_tip', 'torso', [-0.35,32.05,-7.65], [0.8,1,0.6], 'slime', [0,0,-22]],
  ['hip_patch', 'pelvis', [-3.2,18.9,-4.3], [3.6,2.7,0.7], 'capeDark', [0,0,-9]]
];
singles.forEach(spec => cube(...spec));
const mirrored = [
  ['pectoral','torso',[4.1,30.1,-4.7],[7.4,5.4,2.2],'cloth',[0,0,-5]],
  ['rib_wrap','torso',[6.3,26.2,-0.2],[3,5.4,8.6],'clothDark',[0,0,-8]],
  ['belt_rivet','waist',[4.7,22.3,-4.9],[0.8,0.8,0.6],'gold'],
  ['collar','torso',[6.1,33.2,0],[4,3.6,8.4],'capeDark',[0,0,-18]],
  ['cape_clasp','torso',[6.9,32.4,-4.8],[2,2,1.3],'gold'],
  ['chest_seam','torso',[6.1,27.1,-5.2],[0.6,2,0.5],'gold',[0,0,15]],
  ['thigh','leg_{side}',[4.8,14.4,0],[5.8,8.8,6.2],'clothDark'],
  ['thigh_straw_front','leg_{side}',[4.8,13.7,-3],[4.5,5.8,1],'straw'],
  ['kneecap','shin_{side}',[4.8,9.6,-2.7],[5.2,3.4,2],'leather'],
  ['shin_bundle','shin_{side}',[4.8,6.5,0],[4.7,7.8,5],'straw'],
  ['shin_wrap','shin_{side}',[4.8,6.4,-0.1],[5.2,1.5,5.7],'leather'],
  ['ankle_cuff','foot_{side}',[4.8,3.2,-0.2],[6.1,3.2,6.5],'leather'],
  ['mud_boot','foot_{side}',[4.8,1.4,-1.7],[6.7,2.8,9.3],'metal'],
  ['boot_toe','foot_{side}',[4.8,1.6,-5.7],[6.1,2.1,2],'leather'],
  ['boot_sole','foot_{side}',[4.8,0.5,-1.5],[7.1,1,9.6],'dark'],
  ['shoulder_bundle','arm_{side}',[10.6,31,0],[7.2,6.1,7.4],'straw',[0,0,12]],
  ['shoulder_cap','arm_{side}',[10.9,33,0],[7.1,2.2,7.9],'leather',[0,0,12]],
  ['shoulder_front','arm_{side}',[10.8,31.2,-3.8],[5.5,3.3,1],'metal',[0,0,12]],
  ['shoulder_stud','arm_{side}',[11,31.8,-4.4],[1.3,1.3,0.8],'gold'],
  ['upper_arm','arm_{side}',[11.5,27,0],[5.4,7.1,5.8],'cloth',[0,0,8]],
  ['elbow','forearm_{side}',[12.1,24,0],[4.9,3.2,5.3],'straw'],
  ['gauntlet','forearm_{side}',[12.3,20.8,-0.1],[6.2,5.7,6.5],'leather'],
  ['gauntlet_plate','forearm_{side}',[12.3,21.2,-3.2],[4.9,3.9,1],'metal'],
  ['wrist_ring','forearm_{side}',[12.3,18.3,-0.1],[6.5,1.3,6.9],'gold'],
  ['palm','hand_{side}',[12.3,16.5,-0.4],[5.1,3.6,4.5],'straw'],
  ['thumb','hand_{side}',[9.9,16.5,-1.2],[1.8,2.5,2.2],'straw',[0,0,-18]]
];
mirrored.forEach(spec => pair(...spec));
for (let index = 0; index < 3; index++) {
  cube(`crest_coil_${index}`, 'torso', [0.15*index,28.5+index*1.25,-7.65], [3.7-index,1,0.6], 'slime');
  pair(`finger_${index}`, 'hand_{side}', [10.7+index*1.6,14.9,-1.9], [1.25,2.3,2.7], 'straw', [8,0,0]);
}
for (let index = 0; index < 2; index++) {
  pair(`wrist_straw_${index}`, 'forearm_{side}', [14.4+index*0.65,23.7+index*0.6,1.7], [1.1,3.1,1.1], 'straw', [14,0,18]);
}
const coils = [[0,37.6,20,4.8,15.5],[0.3,41.5,18.4,4.3,14.1],[0.8,45.1,15.1,4,11.8],
  [0.15,48.35,11.5,3.7,9.3],[-0.9,51,7.5,3,6.8]];
coils.forEach(([offset,height,width,thickness,depth],index) => {
  cube(`coil_${index}_wide`, 'head', [offset,height,0], [width,thickness,depth-2.8], 'compost', zero, index);
  cube(`coil_${index}_round`, 'head', [offset,height+0.18,0], [width-2.7,thickness-0.55,depth], 'ridge', zero, index);
});
cube('curl_root','head',[-1.6,52.4,0],[4.8,2.6,4.4],'compost',[0,0,-18],5);
cube('curl_hook','head',[-0.65,53.5,0],[3.6,2.3,3.4],'ridge',[0,0,-35],5);
cube('curl_tip','head',[1.05,53.7,0],[2.8,1.6,2.4],'compost',[0,0,-16],5);
cube('hero_mask','head',[0,41,-7.45],[14.6,3.8,1.1],'dark');
pair('eye','head',[4.15,41.2,-8.15],[4.5,2.2,0.65],'ivory',[0,0,7]);
pair('pupil','head',[3.55,41.05,-8.57],[1.1,1.65,0.3],'pupil');
pair('brow','head',[4.2,42.75,-8.4],[5.6,1,1],'dark',[0,0,12]);
cube('smirk','head',[0.1,37.5,-7.9],[9.2,1.6,0.8],'dark',[0,0,-5]);
cube('grin','head',[0.2,37.9,-8.45],[6.6,0.85,0.5],'ivory',[0,0,-5]);
cube('tooth','head',[-2.7,37.1,-8.43],[1.3,1.1,0.55],'ivory');
cube('cheek_dimple','head',[-5.5,37.9,-7.65],[1,1.4,0.7],'dark',[0,0,-15]);
cube('slime_trail','head',[7.65,37,-5.85],[0.8,2.5,0.75],'slime');
cube('slime_drop','head',[7.65,35.7,-5.85],[1.25,1.1,1.15],'slime');
for (let index = 0; index < 4; index++) {
  const width = 13 + index*1.15;
  const owner = `cape${index+1}`;
  const height = 29.3-index*5.5;
  const depth = 6.1+index*0.8;
  cube(`cape_panel_${index}`,owner,[0,height,depth],[width,6.7,1.15],'cape');
  pair(`cape_edge_${index}`,owner,[(width-0.7)/2,height-0.15,depth+0.25],[0.7,6.5,1.25],'capeDark');
  pair(`cape_fold_${index}`,owner,[width*0.24,height+0.12,depth+0.6],[0.85,6.3,0.7],'capeDark');
}
for (let index = 0; index < 5; index++) {
  cube(`cape_tatter_${index}`,'cape4',[(index-2)*3.1,8.7-(index%2)*0.8,8.65],
    [2.4,2.5+(index%2),1],'capeDark',[0,0,(index-2)*3]);
}
for (const side of [-1,1]) {
  const owner = side < 0 ? 'fly1' : 'fly2';
  cube(`${owner}_body`,owner,[side*13,46+side*2,0],[1.35,1.4,2.1],'metal');
  cube(`${owner}_eyes`,owner,[side*13,46.4+side*2,-1],[1.65,0.75,0.6],'pupil');
  for (const wingSide of [-1,1]) {
    const suffix = wingSide < 0 ? 'l' : 'r';
    cube(`${owner}_${suffix}`,`${owner}_wing_${suffix}`,[side*13+wingSide*1.5,46.9+side*2,0.3],
      [2.8,0.3,1.4],'wing',[0,wingSide*22,0]);
  }
}

const faceDimensions = size => ({
  north: [size[0],size[1]], south: [size[0],size[1]], east: [size[2],size[1]],
  west: [size[2],size[1]], up: [size[0],size[2]], down: [size[0],size[2]]
});
const islands = parts.flatMap(part => Object.entries(faceDimensions(part.entry.size)).map(([face,dimensions]) => ({
  part, face, width: Math.max(1,Math.ceil(dimensions[0]*DENSITY)),
  height: Math.max(1,Math.ceil(dimensions[1]*DENSITY))
}))).sort((first,second) => second.height-first.height || second.width-first.width);
let cursorX = 1;
let cursorY = 1;
let rowHeight = 0;
for (const island of islands) {
  if (cursorX+island.width+1 > ATLAS) {
    cursorX = 1;
    cursorY += rowHeight+2;
    rowHeight = 0;
  }
  if (cursorY+island.height+1 > ATLAS) throw new Error('Atlas overflow');
  island.left = cursorX;
  island.top = cursorY;
  island.part.entry.uv[island.face] = {uv: [cursorX,cursorY], uv_size: [island.width,island.height]};
  cursorX += island.width+2;
  rowHeight = Math.max(rowHeight,island.height);
}

function paint(island,pixelX,pixelY,phase) {
  const {material,name,layer} = island.part;
  const unitX = pixelX/Math.max(1,island.width-1);
  const unitY = pixelY/Math.max(1,island.height-1);
  const edge = Math.min(pixelX,pixelY,island.width-1-pixelX,island.height-1-pixelY);
  const organic = material === 'compost' || material === 'ridge';
  const base = phase && organic ? rainbow[layer] : palette[material];
  let brightness = island.face === 'up' ? 1.13 : island.face === 'down' ? 0.68 : 1;
  const blockNoise = ((Math.floor(pixelX/3)*13+Math.floor(pixelY/2)*7+name.length*3)%11)/11;
  if (organic) {
    const arc = 0.22+Math.pow(unitX-0.35,2)*0.72;
    brightness *= unitY < arc ? 1.18 : unitY < arc+0.19 ? 1.06 : unitY > 0.81 ? 0.69 : 0.88;
    brightness += blockNoise > 0.83 ? 0.055 : 0;
  } else if (material === 'cloth' || material === 'clothDark') {
    brightness *= edge === 0 ? 0.64 : pixelY === 1 ? 1.17 : 0.94+blockNoise*0.10;
    if (edge === 1 && (pixelX+pixelY)%4 < 2) brightness = 1.23;
    if (pixelX%7 === 2 && pixelY%5 === 2) brightness *= 0.85;
  } else if (material === 'straw') {
    brightness *= [0.77,1.14,1.02,0.9,1.12][pixelX%5];
    if ((pixelY+Math.floor(pixelX/3)*2)%13 === 0) brightness *= 0.83;
  } else if (material === 'cape' || material === 'capeDark') {
    brightness *= [0.72,0.95,1.12,1.02][Math.floor(unitX*8)%4];
    if (edge === 0) brightness *= 0.76;
    if (pixelY === island.height-2 && pixelX%3 !== 0) brightness *= 1.22;
  } else if (['metal','gold','leather'].includes(material)) {
    brightness *= edge === 0 ? 0.61 : pixelY === 1 ? 1.28 : pixelX === 1 ? 1.1 : 0.95;
    if (material === 'leather' && pixelY%8 === 5) brightness *= 0.78;
  } else if (material === 'ivory') {
    brightness *= pixelY === 0 ? 1.06 : pixelY === island.height-1 ? 0.81 : 0.97;
  }
  const color = base.map(channel => Math.max(0,Math.min(255,Math.round(channel*brightness))));
  const emissive = material === 'slime' || material === 'pupil' || (phase && organic && unitY < 0.28 && edge > 0);
  return {color,emissive};
}

function makeAtlas(phase) {
  const texture = new Canvas(ATLAS,ATLAS);
  const glow = new Canvas(ATLAS,ATLAS);
  for (const island of islands) {
    for (let pixelY = -1; pixelY <= island.height; pixelY++) {
      for (let pixelX = -1; pixelX <= island.width; pixelX++) {
        const sample = paint(island,Math.max(0,Math.min(island.width-1,pixelX)),
          Math.max(0,Math.min(island.height-1,pixelY)),phase);
        texture.set(island.left+pixelX,island.top+pixelY,...sample.color,1);
        if (sample.emissive) glow.set(island.left+pixelX,island.top+pixelY,255,255,255,1);
      }
    }
  }
  return {texture,glow};
}

const animations = require('./compost_v5_anims.cjs')();
const {listCubes,cubeCorners} = require('./cube_space.cjs');
const fitBones = JSON.parse(JSON.stringify(bones));
for (const entry of fitBones) {
  for (const item of entry.cubes || []) {
    if (item.rotation) item.rotation = [-item.rotation[0],item.rotation[1],-item.rotation[2]];
  }
}
const fitModel = {bones:fitBones,byName:new Map(fitBones.map(entry => [entry.name,entry]))};
const feet = listCubes(fitModel).filter(item => item.bone.startsWith('foot_'));
const walk = animations['animation.compost_superman.walk'];
const grounded = {};
for (const stamp of Object.keys(walk.bones.pelvis.position)) {
  const pose = {};
  for (const [owner,channels] of Object.entries(walk.bones)) {
    pose[owner] = {};
    for (const [channel,frames] of Object.entries(channels)) {
      const value = frames[stamp];
      if (!value) throw new Error('Ground fitting requires matching keyframe grids');
      pose[owner][channel] = channel === 'rotation' ? [-value[0],value[1],-value[2]] : value;
    }
  }
  const bottom = Math.min(...feet.flatMap(item => cubeCorners(fitModel,item,pose)).map(point => point[1]));
  grounded[stamp] = [0,round(-bottom),0];
}
walk.bones.root = {position:grounded};
const geometry = {
  format_version: '1.12.0',
  'minecraft:geometry': [{description: {identifier: 'geometry.compost_superman',
    texture_width: ATLAS,texture_height: ATLAS,visible_bounds_width: 6,
    visible_bounds_height: 8,visible_bounds_offset: [0,2.6,0]},bones}]
};
const uuid = name => {
  const hash = crypto.createHash('sha256').update(`ftboss:compost_v5/${name}`).digest('hex').slice(0,32);
  return `${hash.slice(0,8)}-${hash.slice(8,12)}-${hash.slice(12,16)}-${hash.slice(16,20)}-${hash.slice(20)}`;
};
const write = (file,value) => fs.writeFileSync(path.join(OUT,file),JSON.stringify(value,null,2)+'\n');

function blockbench(name,texturePath) {
  const elements = parts.map(part => ({
    name: part.name,type: 'cube',uuid: uuid(part.name),from: part.entry.origin,
    to: vector(part.entry.origin.map((value,axis) => value+part.entry.size[axis])),
    origin: part.entry.pivot || part.entry.origin,rotation: part.entry.rotation || zero,
    box_uv: false,autouv: 0,color: 0,visibility: true,
    faces: Object.fromEntries(Object.entries(part.entry.uv).map(([face,mapping]) => [face,
      {uv: [...mapping.uv,mapping.uv[0]+mapping.uv_size[0],mapping.uv[1]+mapping.uv_size[1]],texture: 0}]))
  }));
  const groups = new Map(bones.map(entry => [entry.name,{name: entry.name,uuid: uuid(entry.name+'/bone'),
    origin: entry.pivot,rotation: zero,export: true,isOpen: false,visibility: true,
    children: parts.filter(part => part.owner === entry.name).map(part => uuid(part.name))}]));
  const outliner = [];
  for (const entry of bones) {
    if (entry.parent) groups.get(entry.parent).children.push(groups.get(entry.name));
    else outliner.push(groups.get(entry.name));
  }
  const bbAnimations = Object.entries(animations).map(([animName,entry]) => ({
    uuid: uuid(animName),name: animName,length: entry.animation_length,
    loop: entry.loop === true ? 'loop' : entry.loop === 'hold_on_last_frame' ? 'hold' : 'once',
    snapping: 24,animators: Object.fromEntries(Object.entries(entry.bones).map(([owner,channels]) => [uuid(owner+'/bone'),{
      name: owner,type: 'bone',keyframes: Object.entries(channels).flatMap(([type,frames]) =>
        Object.entries(frames).map(([time,value]) => ({channel: type,time: Number(time),
          uuid: uuid(`${animName}/${owner}/${type}/${time}`),interpolation: 'linear',
          data_points: [{x: String(value[0]),y: String(value[1]),z: String(value[2])}]})))
    }]))
  }));
  write(`${name}.bbmodel`,{meta: {format_version: '4.10',model_format: 'bedrock',box_uv: false},
    name,model_identifier: 'geometry.compost_superman',visible_box: [6,8,2.6],
    resolution: {width: ATLAS,height: ATLAS},elements,outliner,animations: bbAnimations,
    textures: [{name: name+'.png',uuid: uuid(name+'/texture'),id: '0',width: ATLAS,height: ATLAS,
      uv_width: ATLAS,uv_height: ATLAS,source: 'data:image/png;base64,'+fs.readFileSync(texturePath).toString('base64')}]
  });
}

fs.mkdirSync(OUT,{recursive: true});
for (const phase of [false,true]) {
  const name = 'compost_superman'+(phase ? '_p2' : '');
  const {texture,glow} = makeAtlas(phase);
  write(`${name}.geo.json`,geometry);
  texture.save(path.join(OUT,name+'.png'));
  glow.save(path.join(OUT,name+'_glowmask.png'));
  blockbench(name,path.join(OUT,name+'.png'));
}
write('compost_superman.animation.json',{format_version: '1.8.0',animations});
write('model_manifest.json',{version: 5,bones: bones.length,cubes: parts.length,atlas: [ATLAS,ATLAS],
  density: DENSITY,animations: Object.keys(animations),atlasUsedHeight: cursorY+rowHeight,
  parts: parts.map(({name,owner,material}) => ({name,owner,material}))});
console.log(JSON.stringify({output: OUT,bones: bones.length,cubes: parts.length,
  animations: Object.keys(animations).length,atlasUsedHeight: cursorY+rowHeight}));
