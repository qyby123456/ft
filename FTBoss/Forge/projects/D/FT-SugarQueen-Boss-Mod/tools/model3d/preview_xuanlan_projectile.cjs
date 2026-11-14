const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const { Canvas, decodePNG } = require('./png.cjs');
const assets = path.resolve(process.argv[2]);
const output = path.resolve(process.argv[3]);
const texturePath = path.join(assets, 'textures/item/xuanlan_green_white_v2.png');
const texture = decodePNG(texturePath);
const modelNames = ['wheel_hub', 'wheel_upper', 'wheel_lower', 'wheel_locks'];
const models = modelNames.map(name => ({ name, file: path.join(assets, 'models/item/xuanlan_rig', name + '.json') }));
for (const model of models) model.data = JSON.parse(fs.readFileSync(model.file, 'utf8'));
const renderer = fs.readFileSync(path.resolve(__dirname, '../../src/main/java/net/farmingtales/ftboss/client/render/PlayerMoonChakramRenderer.java'), 'utf8');
const scale = Number(renderer.match(/PROJECTILE_SCALE = ([0-9.]+)F/)[1]);
assert.equal(scale, 0.7);
const corners = {
  north: [[1,1,0],[1,0,0],[0,0,0],[0,1,0]], south: [[0,1,1],[0,0,1],[1,0,1],[1,1,1]],
  east: [[1,1,1],[1,0,1],[1,0,0],[1,1,0]], west: [[0,1,0],[0,0,0],[0,0,1],[0,1,1]],
  up: [[0,1,0],[0,1,1],[1,1,1],[1,1,0]], down: [[0,0,1],[0,0,0],[1,0,0],[1,0,1]]
};
const views = [
  { name: 'FRONT', project: point => [-point[0], point[1], -point[2]] },
  { name: 'BACK', project: point => [point[0], point[1], point[2]] },
  { name: 'LEFT', project: point => [point[2], point[1], -point[0]] },
  { name: 'RIGHT', project: point => [-point[2], point[1], point[0]] }
];
const letters = {
  A:['010','101','111','101','101'], B:['110','101','110','101','110'], C:['011','100','100','100','011'],
  D:['110','101','101','101','110'], E:['111','100','110','100','111'], F:['111','100','110','100','100'],
  G:['011','100','101','101','011'], H:['101','101','111','101','101'], I:['111','010','010','010','111'],
  K:['101','101','110','101','101'], L:['100','100','100','100','111'], N:['101','111','111','111','101'],
  O:['010','101','101','101','010'], R:['110','101','110','101','101'], T:['111','010','010','010','010'],
  U:['101','101','101','101','111'], W:['101','101','111','111','101'], '0':['111','101','101','101','111'],
  '7':['111','001','010','010','010'], '%':['101','001','010','100','101'], ' ':['0','0','0','0','0']
};
function label(canvas, text, horizontal, vertical) {
  for (const character of text) {
    const glyph = letters[character] || letters[' '];
    glyph.forEach((row, rowIndex) => [...row].forEach((pixel, column) => {
      if (pixel === '1') canvas.rect(horizontal + column * 3, vertical + rowIndex * 3, 3, 3, 193, 218, 230, 1);
    }));
    horizontal += 12;
  }
}
function rotate(point, axis, degrees) {
  const angle = degrees * Math.PI / 180;
  const cosine = Math.cos(angle), sine = Math.sin(angle);
  const [horizontal, vertical, depth] = point;
  if (axis === 'x') return [horizontal, vertical*cosine-depth*sine, vertical*sine+depth*cosine];
  if (axis === 'y') return [horizontal*cosine+depth*sine, vertical, -horizontal*sine+depth*cosine];
  return [horizontal*cosine-vertical*sine, horizontal*sine+vertical*cosine, depth];
}
function transform(point, element, part, fold, spin, multiplier) {
  if (element.rotation) {
    const rotation = element.rotation;
    point = point.map((value, axis) => value - rotation.origin[axis]);
    if (rotation.rescale) point = point.map((value, axis) => axis === 'xyz'.indexOf(rotation.axis) ? value : value / Math.cos(rotation.angle*Math.PI/180));
    point = rotate(point, rotation.axis, rotation.angle).map((value, axis) => value + rotation.origin[axis]);
  }
  point = point.map(value => value - 8);
  if (part === 'wheel_upper') point = rotate(point, 'z', 32*fold).map((value, axis) => value + (axis===0 ? -0.7*fold : axis===1 ? -2.2*fold : 0));
  if (part === 'wheel_lower') point = rotate(point, 'z', 32*fold).map((value, axis) => value + (axis===0 ? 0.7*fold : axis===1 ? 2.2*fold : 0));
  if (part === 'wheel_locks') point = rotate(point.map(value => value*(1-0.18*fold)), 'z', -24*fold);
  return rotate(point.map(value => value * 0.5 * multiplier), 'z', spin);
}
function triangle(canvas, depth, points, coordinates, texture, panelLeft, panelTop) {
  const denominator = (points[1][1]-points[2][1])*(points[0][0]-points[2][0])+(points[2][0]-points[1][0])*(points[0][1]-points[2][1]);
  if (Math.abs(denominator)<0.00001) return;
  const left = Math.max(panelLeft, Math.floor(Math.min(...points.map(point=>point[0]))));
  const right = Math.min(panelLeft+359, Math.ceil(Math.max(...points.map(point=>point[0]))));
  const top = Math.max(panelTop+40, Math.floor(Math.min(...points.map(point=>point[1]))));
  const bottom = Math.min(panelTop+359, Math.ceil(Math.max(...points.map(point=>point[1]))));
  for (let vertical=top;vertical<=bottom;vertical++) for (let horizontal=left;horizontal<=right;horizontal++) {
    const first = ((points[1][1]-points[2][1])*(horizontal+0.5-points[2][0])+(points[2][0]-points[1][0])*(vertical+0.5-points[2][1]))/denominator;
    const second = ((points[2][1]-points[0][1])*(horizontal+0.5-points[2][0])+(points[0][0]-points[2][0])*(vertical+0.5-points[2][1]))/denominator;
    const third = 1-first-second;
    if (Math.min(first,second,third)<-0.0001) continue;
    const weights = [first,second,third];
    const distance = weights.reduce((sum,weight,index)=>sum+weight*points[index][2],0);
    const offset = vertical*canvas.w+horizontal;
    if (distance<depth[offset]) continue;
    const uv = [0,1].map(axis=>weights.reduce((sum,weight,index)=>sum+weight*coordinates[index][axis],0));
    const textureX = Math.max(0,Math.min(texture.w-1,Math.floor(uv[0]/16*texture.w)));
    const textureY = Math.max(0,Math.min(texture.h-1,Math.floor(uv[1]/16*texture.h)));
    const source = (textureY*texture.w+textureX)*4;
    if (texture.data[source+3]<128) continue;
    depth[offset]=distance;
    canvas.set(horizontal,vertical,texture.data[source],texture.data[source+1],texture.data[source+2],1);
  }
}
fs.mkdirSync(output,{recursive:true});
const report = { scale, texture: [texture.w, texture.h], states: [], elements: models.reduce((sum,model)=>sum+model.data.elements.length,0),
  files: Object.fromEntries([...models.map(model=>model.file),texturePath].map(file=>[file,crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex')])),
  limitations: ['Offline textured orthographic rasterization; client atlas padding, additive glow and shader interaction are not simulated.'] };
for (const fold of [0,0.35]) for (const spin of [0,120]) {
  const canvas=new Canvas(1440,720);
  canvas.rect(0,0,1440,720,16,25,36,1);
  const depth=new Float64Array(canvas.w*canvas.h).fill(-Infinity);
  for (let row=0;row<2;row++) views.forEach((view,column)=>{
    const panelLeft=column*360,panelTop=row*360,multiplier=row===0?1:scale;
    label(canvas,(row===0?'OLD 100% ':'NEW 70% ')+view.name,panelLeft+18,panelTop+16);
    for (let grid=0;grid<11;grid++) canvas.hline(panelLeft+10,panelLeft+350,panelTop+60+grid*28,23,37,48,1);
    for (const model of models) for (const element of model.data.elements) for (const [face,definition] of Object.entries(element.faces)) {
      const points = corners[face].map(corner=>view.project(transform(corner.map((unit,axis)=>unit?element.to[axis]:element.from[axis]),element,model.name,fold,spin,multiplier)))
        .map(point=>[panelLeft+180+point[0]*25,panelTop+205-point[1]*25,point[2]]);
      const [left,top,right,bottom]=definition.uv;
      let uv=[[left,top],[left,bottom],[right,bottom],[right,top]];
      const turns=(definition.rotation||0)/90;
      uv=uv.map((value,index)=>uv[(index+turns)%4]);
      for (const indices of [[0,1,2],[0,2,3]]) triangle(canvas,depth,indices.map(index=>points[index]),indices.map(index=>uv[index]),texture,panelLeft,panelTop);
    }
  });
  const file=path.join(output,(fold?'return':'outbound')+'-spin-'+spin+'-four-views.png');
  canvas.save(file);
  report.states.push({fold,spin,file});
  console.log(file);
}
fs.writeFileSync(path.join(output,'preview-report.json'),JSON.stringify(report,null,2)+'\n');
