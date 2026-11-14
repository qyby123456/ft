const fs = require('node:fs');
const path = require('node:path');
const { Canvas, decodePNG } = require('./png.cjs');
const space = require('./cube_space.cjs');
const { classify } = require('./gen_dandan_rig.cjs');
const root = path.resolve(__dirname, '../..');
const assets = path.join(root, 'src/main/resources/assets/ftboss');
const samples = JSON.parse(fs.readFileSync(process.argv[2], 'utf8'));
const output = path.resolve(process.argv[3]);
const model = JSON.parse(fs.readFileSync(path.join(assets, 'models/item/dandan_scythe.json')));
const texture = decodePNG(path.join(assets, 'textures/item/dandan_end_rod_scythe_v2.png'));
const width = 380, height = 490;
const sheet = new Canvas(width * 4, height * 2);
sheet.rect(0, 0, sheet.w, sheet.h, 17, 21, 31);
const indices = { north: [6, 4, 0, 2], south: [3, 1, 5, 7], west: [2, 0, 1, 3], east: [7, 5, 4, 6], up: [3, 7, 6, 2], down: [0, 4, 5, 1] };
const shades = { north: 1, south: 0.83, west: 0.74, east: 0.87, up: 1.05, down: 0.62 };
const glyphs = {
  A:['010','101','111','101','101'], B:['110','101','110','101','110'], C:['011','100','100','100','011'],
  D:['110','101','101','101','110'], E:['111','100','110','100','111'], F:['111','100','110','100','100'],
  G:['011','100','101','101','011'], H:['101','101','111','101','101'], I:['111','010','010','010','111'],
  L:['100','100','100','100','111'], M:['10101','11111','10101','10101','10101'], N:['1001','1101','1011','1001','1001'],
  O:['010','101','101','101','010'], P:['110','101','110','100','100'], R:['110','101','110','101','101'],
  S:['011','100','010','001','110'], T:['111','010','010','010','010'], U:['101','101','101','101','111'],
  V:['101','101','101','101','010'], W:['10101','10101','10101','11111','01010'], Y:['101','101','010','010','010'],
  '1':['010','110','010','010','111'], '2':['110','001','010','100','111'], '3':['110','001','010','001','110'],
  '4':['101','101','111','001','001'], '5':['111','100','110','001','110'], '6':['011','100','110','101','010'],
  '7':['111','001','010','010','010'], '9':['010','101','011','001','110'], '0':['111','101','101','101','111']
};
function text(canvas, label, left, top, color = [214, 220, 237], scale = 2) {
  for (const character of label.replaceAll('_', ' ')) {
    const glyph = glyphs[character];
    if (!glyph) { left += 4 * scale; continue; }
    glyph.forEach((row, vertical) => [...row].forEach((pixel, horizontal) => {
      if (pixel === '1') canvas.rect(left + horizontal * scale, top + vertical * scale, scale, scale, ...color);
    }));
    left += (glyph[0].length + 1) * scale;
  }
}
function rotate(point, roll) {
  const angle = roll * Math.PI / 180;
  return [point[0] * Math.cos(angle) - point[1] * Math.sin(angle), point[0] * Math.sin(angle) + point[1] * Math.cos(angle), point[2]];
}
function transform(point, name, bones) {
  const bone = bones.get(name);
  const local = rotate(point.map((value, axis) => (value - bone.pivot[axis]) * bone.scale), bone.roll)
    .map((value, axis) => value + bone.pivot[axis] + bone.move[axis]);
  return bone.parent ? transform(local, bone.parent, bones) : local;
}
function project(point) {
  const yaw = 0.28;
  const horizontal = point[0] * Math.cos(yaw) + (point[2] - 8) * Math.sin(yaw);
  const depth = -point[0] * Math.sin(yaw) + (point[2] - 8) * Math.cos(yaw);
  return [width / 2 + (horizontal + 1) * 8.6, height - 82 - (point[1] + 5) * 8.6, depth];
}
function triangle(canvas, depth, points, uv, shade) {
  const [first, second, third] = points;
  const denominator = (second[1] - third[1]) * (first[0] - third[0]) + (third[0] - second[0]) * (first[1] - third[1]);
  if (Math.abs(denominator) < 1e-8) return;
  const left = Math.max(0, Math.floor(Math.min(...points.map(point => point[0]))));
  const right = Math.min(width - 1, Math.ceil(Math.max(...points.map(point => point[0]))));
  const top = Math.max(45, Math.floor(Math.min(...points.map(point => point[1]))));
  const bottom = Math.min(height - 35, Math.ceil(Math.max(...points.map(point => point[1]))));
  for (let row = top; row <= bottom; row++) for (let column = left; column <= right; column++) {
    const firstWeight = ((second[1] - third[1]) * (column + 0.5 - third[0]) + (third[0] - second[0]) * (row + 0.5 - third[1])) / denominator;
    const secondWeight = ((third[1] - first[1]) * (column + 0.5 - third[0]) + (first[0] - third[0]) * (row + 0.5 - third[1])) / denominator;
    const weights = [firstWeight, secondWeight, 1 - firstWeight - secondWeight];
    if (Math.min(...weights) < 0) continue;
    const distance = points.reduce((total, point, index) => total + point[2] * weights[index], 0);
    const pixel = row * width + column;
    if (distance >= depth[pixel]) continue;
    const texU = uv.reduce((total, point, index) => total + point[0] * weights[index], 0);
    const texV = uv.reduce((total, point, index) => total + point[1] * weights[index], 0);
    const texX = Math.max(0, Math.min(texture.w - 1, Math.floor(texU * texture.w / 16)));
    const texY = Math.max(0, Math.min(texture.h - 1, Math.floor(texV * texture.h / 16)));
    const offset = (texY * texture.w + texX) * 4;
    if (texture.data[offset + 3] < 128) continue;
    depth[pixel] = distance;
    canvas.set(column, row, texture.data[offset] * shade, texture.data[offset + 1] * shade, texture.data[offset + 2] * shade);
  }
}
const edge = [[9.5,23.9,8],[4.5,25.1,8],[0.5,24.4,8],[-3.5,22.6,8],[-6.5,20.1,8],[-8.5,17.4,8],[-10.75,13.9,8],[-11.8,12.4,8]];
function render(sample, index) {
  const canvas = new Canvas(width, height);
  canvas.rect(0, 0, width, height, 23, 28, 41);
  canvas.rect(0, 0, width, 39, 32, 39, 56);
  text(canvas, `${index + 1}  ${sample.name}`, 18, 15);
  const bones = new Map(sample.bones.map(bone => [bone.name, bone]));
  const depth = new Float64Array(width * height).fill(Infinity);
  const edgePoint = (point, lag) => project(rotate(transform(point, point[0] < -7 ? 'blade_tip' : point[0] < 0 ? 'blade_mid' : 'blade_base', bones)
    .map((value, axis) => value - [8,4,8][axis]), lag).map((value, axis) => value + [8,4,8][axis]));
  for (let layer = 4; layer >= 1; layer--) {
    if (sample.trail <= 0) continue;
    const start = layer * (3 + 9 * sample.trail), end = start + 2 + 5 * sample.trail;
    const color = layer % 2 ? [187,163,255] : [255,242,217];
    for (let segment = 0; segment < edge.length - 1; segment++) {
      const corners = [edgePoint(edge[segment],start),edgePoint(edge[segment+1],start),edgePoint(edge[segment+1],end),edgePoint(edge[segment],end)];
      canvas.tri(corners[0],corners[1],corners[2],...color,sample.trail * 0.24 / layer);
      canvas.tri(corners[0],corners[2],corners[3],...color,sample.trail * 0.24 / layer);
    }
  }
  const geometry = { geckolib: false, bones: [{ name:'root' }], byName:new Map([['root',{name:'root'}]]) };
  for (const element of model.elements) {
    const cube = { origin:element.from, size:element.to.map((value,axis)=>value-element.from[axis]) };
    if (element.rotation) {
      cube.pivot = element.rotation.origin;
      cube.rotation = [0,0,0];
      cube.rotation[['x','y','z'].indexOf(element.rotation.axis)] = element.rotation.angle;
    }
    const corners = space.cubeCorners(geometry,{bone:'root',cube}).map(point=>project(transform(point,classify(element),bones)));
    for(const [name,face] of Object.entries(element.faces)) {
      const [left,top,right,bottom] = face.uv;
      const uv = [[left,top],[left,bottom],[right,bottom],[right,top]];
      for(let rotation=0;rotation<(face.rotation||0)/90;rotation++) uv.unshift(uv.pop());
      const points = indices[name].map(index=>corners[index]);
      for(const triangleIndices of [[0,1,2],[0,2,3]]) triangle(canvas,depth,triangleIndices.map(index=>points[index]),
        triangleIndices.map(index=>uv[index]),element.shade===false ? 1.12 : shades[name]);
    }
  }
  for(const name of ['front_core']) {
    const bone = bones.get(name);
    const center = bone.pivot.map((value,axis)=>value+(axis===2?-0.7:0));
    for(let ring=0;ring<2;ring++) {
      const radius = (1.15+sample.open*1.44)*(ring?1.55:1);
      for(let segment=0;segment<40;segment++) {
        if(segment%10>=7) continue;
        const point=angle=>project(transform(center.map((value,axis)=>value+(axis===0?Math.cos(angle)*radius:axis===1?Math.sin(angle)*radius:0)),name,bones));
        const first=point(segment*Math.PI/20),second=point((segment+1)*Math.PI/20);
        canvas.line(Math.round(first[0]),Math.round(first[1]),Math.round(second[0]),Math.round(second[1]),
          ...(ring?[187,163,255]:[255,242,217]), Math.min(1,ring?sample.open*0.55:sample.glow));
      }
    }
  }
  text(canvas, 'OFFLINE POSE PREVIEW', 40, height-24, [125,139,169], 1);
  return canvas;
}
fs.mkdirSync(output,{recursive:true});
samples.forEach((sample,index)=>{
  const canvas=render(sample,index);
  canvas.save(path.join(output,`${index+1}-${sample.name.toLowerCase()}.png`));
  const left=(index%4)*width,top=Math.floor(index/4)*height;
  for(let row=0;row<height;row++) sheet.d.set(canvas.d.subarray(row*width*4,(row+1)*width*4),((top+row)*sheet.w+left)*4);
});
text(sheet,'DANDAN SCYTHE',width*3+30,height+100,[255,242,217],3);
text(sheet,'9 BONE RIG',width*3+30,height+150,[187,163,255],2);
text(sheet,'IVORY AND VIOLET',width*3+30,height+185,[187,163,255],2);
text(sheet,'REAL UV TEXTURE',width*3+30,height+220,[156,169,194],2);
text(sheet,'NOT IN GAME',width*3+30,height+300,[156,169,194],2);
sheet.save(path.join(output,'dandan-motion-contact.png'));
console.log('Saved textured Dandan poses from Java motion samples; offline visualization, not an in-game screenshot.');
