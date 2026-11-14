const fs = require('node:fs');
const path = require('node:path');
const { Canvas, decodePNG } = require('./png.cjs');
const { classify } = require('./gen_xuanlan_rig.cjs');
const root = path.resolve(__dirname, '../..');
const output = path.resolve(process.argv[2] || path.join(root, 'build/verification/xuanlan-motion'));
const poses = JSON.parse(fs.readFileSync(path.join(root, 'build/verification/xuanlan-motion/poses.json'), 'utf8'));
const assets = path.join(root, 'src/main/resources/assets/ftboss');
const atlas = decodePNG(path.join(assets, 'textures/item/xuanlan_green_white_v2.png'));
const canvas = new Canvas(1440, 850);
canvas.rect(0, 0, 1440, 850, 12, 21, 29);
const faces = {
  north: [[0,1,0],[1,1,0],[1,0,0],[0,0,0]], south: [[1,1,1],[0,1,1],[0,0,1],[1,0,1]],
  east: [[1,1,0],[1,1,1],[1,0,1],[1,0,0]], west: [[0,1,1],[0,1,0],[0,0,0],[0,0,1]],
  up: [[0,1,1],[1,1,1],[1,1,0],[0,1,0]], down: [[0,0,0],[1,0,0],[1,0,1],[0,0,1]]
};
function rotate(point, axis, angle) {
  const cosine = Math.cos(angle * Math.PI / 180), sine = Math.sin(angle * Math.PI / 180);
  const [horizontal, vertical, depth] = point;
  if (axis === 'x') return [horizontal, vertical * cosine - depth * sine, vertical * sine + depth * cosine];
  if (axis === 'y') return [horizontal * cosine + depth * sine, vertical, -horizontal * sine + depth * cosine];
  return [horizontal * cosine - vertical * sine, horizontal * sine + vertical * cosine, depth];
}
function transform(point, part, state, rotation) {
  if (rotation) point = rotate(point.map((value, axis) => value - rotation.origin[axis]), rotation.axis, rotation.angle).map((value, axis) => value + rotation.origin[axis]);
  const pivot = part.startsWith('wheel_') ? [8, 8, 8] : part === 'gun_stock' ? [1,5,8] : part === 'gun_body' ? [1,6,8] : [10.75,8.1,8];
  const [moveX, moveY, angle, scale] = state.bones[part];
  point = rotate(point.map((value, axis) => (value - pivot[axis]) * scale), 'z', angle).map((value, axis) => value + pivot[axis] + (axis === 0 ? moveX : axis === 1 ? moveY : 0));
  return rotate(rotate(point.map((value, axis) => value - (axis === 0 && !state.white ? 9 : 8)), 'x', 9), 'y', -18);
}
function color(uv) {
  const total = [0,0,0];
  let count = 0;
  for (let vertical = Math.floor(Math.min(uv[1],uv[3])*8); vertical < Math.ceil(Math.max(uv[1],uv[3])*8); vertical++) {
    for (let horizontal = Math.floor(Math.min(uv[0],uv[2])*8); horizontal < Math.ceil(Math.max(uv[0],uv[2])*8); horizontal++) {
      const offset = (Math.min(127,vertical)*128 + Math.min(127,horizontal))*4;
      for (let channel = 0; channel < 3; channel++) total[channel] += atlas.data[offset+channel];
      count++;
    }
  }
  return total.map(value => value / Math.max(1,count));
}
poses.forEach((state,index) => {
  const left = (index%3)*480, top = Math.floor(index/3)*365 + 95;
  canvas.rect(left+15,top,450,348,18,31,40);
  canvas.rect(left+15,top,450,2,53,115,110);
  for (let row = 0; row < 10; row++) canvas.hline(left+30,left+450,top+54+row*28,32,48,57,0.45);
  const model = JSON.parse(fs.readFileSync(path.join(assets, `models/item/${state.white ? 'xuanlan_crescendum_v2' : 'xuanlan_calibrum_v2'}.json`),'utf8'));
  const polygons=[];
  for (const element of model.elements) {
    const part=classify(element.name,state.white);
    for (const [side,corners] of Object.entries(faces)) {
      const points=corners.map(corner => transform(corner.map((unit,axis) => unit ? element.to[axis] : element.from[axis]),part,state,element.rotation));
      polygons.push({points,color:color(element.faces[side].uv),depth:points.reduce((sum,point)=>sum+point[2],0)/4,part,side});
    }
  }
  polygons.sort((first,second)=>first.depth-second.depth);
  const scale=state.white ? 12.5 : 9.2;
  for(const polygon of polygons) {
    const points=polygon.points.map(point=>[left+240+point[0]*scale,top+205-point[1]*scale]);
    const shade=polygon.part==='gun_core' ? 1.25 : polygon.side==='up' ? 1.1 : polygon.side==='south' ? 1 : 0.72;
    canvas.quad(points,...polygon.color.map(value=>value*shade),1);
  }
  if(index===1 || index===2) {
    const muzzle=transform([28.5,8.1,8],'gun_core',state);
    const center=[left+240+muzzle[0]*scale,top+205-muzzle[1]*scale];
    for(let radius=1;radius<=15;radius++) {
      for(let step=0;step<64;step++) {
        const angle=step*Math.PI/32;
        canvas.set(Math.round(center[0]+Math.cos(angle)*radius*0.45),Math.round(center[1]+Math.sin(angle)*radius),116,255,218,0.05);
      }
    }
  }
});
fs.mkdirSync(output,{recursive:true});
canvas.save(path.join(output,'xuanlan-poses-raw.png'));
console.log(path.join(output,'xuanlan-poses-raw.png'));
