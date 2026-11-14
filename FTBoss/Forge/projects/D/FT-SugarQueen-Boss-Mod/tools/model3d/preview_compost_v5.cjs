'use strict';

const fs = require('fs');
const path = require('path');
const {Canvas,decodePNG} = require('./png.cjs');
const {loadGeo,listCubes,cubeCorners} = require('./cube_space.cjs');
const directory = path.resolve(process.argv[2]);
const model = loadGeo(path.join(directory,'compost_superman.geo.json'));
const animations = JSON.parse(fs.readFileSync(path.join(directory,'compost_superman.animation.json'),'utf8')).animations;
const rawGeometry = JSON.stringify(model.json);
for (const entry of model.bones) {
  for (const item of [entry,...(entry.cubes || [])]) {
    if (item.rotation) item.rotation = [-item.rotation[0],item.rotation[1],-item.rotation[2]];
  }
}
const faceIndices = {north:[6,2,0,4],south:[3,7,5,1],east:[7,6,4,5],west:[2,3,1,0],up:[2,6,7,3],down:[1,5,4,0]};
const dot = (first,second) => first.reduce((sum,value,axis) => sum+value*second[axis],0);
const subtract = (first,second) => first.map((value,axis) => value-second[axis]);
const normalize = values => values.map(value => value/Math.hypot(...values));
const center = points => [0,1,2].map(axis => points.reduce((sum,point) => sum+point[axis],0)/points.length);
const cubes = listCubes(model);

function poseAt(name,time) {
  const pose = {};
  const animation = animations['animation.compost_superman.'+name];
  if (!animation) return pose;
  for (const [owner,channels] of Object.entries(animation.bones)) {
    pose[owner] = {};
    for (const [channel,frames] of Object.entries(channels)) {
      const keys = Object.entries(frames).map(([stamp,value]) => [Number(stamp),value]).sort((first,second) => first[0]-second[0]);
      let value = keys.at(-1)[1];
      for (let index = 0; index < keys.length-1; index++) {
        if (time <= keys[index+1][0]) {
          const amount = Math.max(0,(time-keys[index][0])/(keys[index+1][0]-keys[index][0]));
          value = keys[index][1].map((coordinate,axis) => coordinate+(keys[index+1][1][axis]-coordinate)*amount);
          break;
        }
      }
      pose[owner][channel] = channel === 'rotation' ? [-value[0],value[1],-value[2]] : value;
    }
  }
  return pose;
}

function render(name,phase,yawDegrees,pitchDegrees,poseName = '',time = 0) {
  const width = 640;
  const height = 760;
  const canvas = new Canvas(width,height);
  const depthBuffer = new Float32Array(width*height).fill(-Infinity);
  const texture = decodePNG(path.join(directory,`compost_superman${phase ? '_p2' : ''}.png`));
  const yaw = yawDegrees*Math.PI/180;
  const pitch = pitchDegrees*Math.PI/180;
  const camera = [Math.sin(yaw)*Math.cos(pitch),Math.sin(pitch),-Math.cos(yaw)*Math.cos(pitch)];
  const right = [Math.cos(yaw),0,Math.sin(yaw)];
  const up = [-Math.sin(pitch)*Math.sin(yaw),Math.cos(pitch),Math.sin(pitch)*Math.cos(yaw)];
  const pose = poseAt(poseName,time);
  const allPoints = cubes.flatMap(item => cubeCorners(model,item,pose));
  const screenPoints = allPoints.map(point => [dot(point,right),dot(point,up)]);
  const minimum = [0,1].map(axis => Math.min(...screenPoints.map(point => point[axis])));
  const maximum = [0,1].map(axis => Math.max(...screenPoints.map(point => point[axis])));
  const scale = Math.min((width-110)/(maximum[0]-minimum[0]),(height-100)/(maximum[1]-minimum[1]));
  const centerX = (minimum[0]+maximum[0])/2;
  const centerY = (minimum[1]+maximum[1])/2;
  const project = point => [(dot(point,right)-centerX)*scale+width/2,height/2-(dot(point,up)-centerY)*scale,dot(point,camera)];
  const lighting = normalize([-0.6,1,-0.75]);
  function triangle(points,uvs,brightness) {
    const [first,second,third] = points;
    const denominator = (second[1]-third[1])*(first[0]-third[0])+(third[0]-second[0])*(first[1]-third[1]);
    if (Math.abs(denominator) < 0.0001) return;
    const left = Math.max(0,Math.floor(Math.min(...points.map(point => point[0]))));
    const rightEdge = Math.min(width-1,Math.ceil(Math.max(...points.map(point => point[0]))));
    const top = Math.max(0,Math.floor(Math.min(...points.map(point => point[1]))));
    const bottom = Math.min(height-1,Math.ceil(Math.max(...points.map(point => point[1]))));
    for (let pixelY = top; pixelY <= bottom; pixelY++) {
      for (let pixelX = left; pixelX <= rightEdge; pixelX++) {
        const weightFirst = ((second[1]-third[1])*(pixelX+0.5-third[0])+(third[0]-second[0])*(pixelY+0.5-third[1]))/denominator;
        const weightSecond = ((third[1]-first[1])*(pixelX+0.5-third[0])+(first[0]-third[0])*(pixelY+0.5-third[1]))/denominator;
        const weightThird = 1-weightFirst-weightSecond;
        if (Math.min(weightFirst,weightSecond,weightThird) < -0.000001) continue;
        const weights = [weightFirst,weightSecond,weightThird];
        const depth = dot(weights,points.map(point => point[2]));
        const offset = pixelY*width+pixelX;
        if (depth <= depthBuffer[offset]) continue;
        const texX = Math.max(0,Math.min(texture.w-1,Math.floor(dot(weights,uvs.map(uv => uv[0])))));
        const texY = Math.max(0,Math.min(texture.h-1,Math.floor(dot(weights,uvs.map(uv => uv[1])))));
        const texOffset = (texY*texture.w+texX)*4;
        if (texture.data[texOffset+3] < 128) continue;
        canvas.set(pixelX,pixelY,...[0,1,2].map(channel => texture.data[texOffset+channel]*brightness),1);
        depthBuffer[offset] = depth;
      }
    }
  }
  for (const item of cubes) {
    const points = cubeCorners(model,item,pose);
    const middle = center(points);
    for (const [face,indices] of Object.entries(faceIndices)) {
      const corners = indices.map(index => points[index]);
      const normal = normalize(subtract(center(corners),middle));
      if (dot(normal,camera) <= 0) continue;
      const mapping = item.cube.uv[face];
      const [left,top] = mapping.uv;
      const [faceWidth,faceHeight] = mapping.uv_size;
      const uvs = [[left,top],[left+faceWidth,top],[left+faceWidth,top+faceHeight],[left,top+faceHeight]];
      const projected = corners.map(project);
      const brightness = 0.66+0.34*Math.max(0,dot(normal,lighting));
      triangle([projected[0],projected[1],projected[2]],[uvs[0],uvs[1],uvs[2]],brightness);
      triangle([projected[0],projected[2],projected[3]],[uvs[0],uvs[2],uvs[3]],brightness);
    }
  }
  canvas.save(path.join(directory,name+'.png'));
}

for (const phase of [false,true]) {
  const suffix = phase ? '_p2' : '';
  render('hero'+suffix,phase,-28,9);
  render('front'+suffix,phase,0,0);
  render('back'+suffix,phase,180,0);
}
render('pose_fire',false,-42,8,'cannon_fire',0.42);
render('pose_flight',false,-30,8,'float_move',0.35);
render('pose_transform',true,-26,8,'transform',1.12);
render('pose_death',false,-25,28,'death',2);
render('pose_walk',false,-30,5,'walk',0.225);
const report = {static: {},poses: {},offlineConvention: 'GeckoLib 4.8.4: mirror X coordinates; rotation=(-X,+Y,-Z) in original model space. No held item or glow pass.'};
for (const [name,time] of [['idle',0],['cannon_fire',0.42],['float_move',0.35],['transform',1.12],['death',2],['walk',0.225]]) {
  const points = cubes.flatMap(item => cubeCorners(model,item,poseAt(name,time)));
  const minimum = [0,1,2].map(axis => Math.min(...points.map(point => point[axis])));
  const maximum = [0,1,2].map(axis => Math.max(...points.map(point => point[axis])));
  report.poses[name] = {minimum,maximum,sizeBlocks: maximum.map((value,axis) => (value-minimum[axis])/16)};
}
report.sampledFloor = {};
for (const name of ['walk','death']) {
  let minimum = Infinity;
  const duration = animations['animation.compost_superman.'+name].animation_length;
  const body = cubes.filter(item => !item.bone.startsWith('fly'));
  for (let frame = 0; frame <= 60; frame++) {
    const pose = poseAt(name,duration*frame/60);
    const bottom = Math.min(...body.flatMap(item => cubeCorners(model,item,pose)).map(point => point[1]));
    minimum = Math.min(minimum,bottom);
  }
  report.sampledFloor[name] = minimum;
}
fs.writeFileSync(path.join(directory,'pose_report.json'),JSON.stringify(report,null,2)+'\n');
if (JSON.stringify(JSON.parse(rawGeometry)['minecraft:geometry'][0].bones.map(entry => entry.name)) !==
    JSON.stringify(model.bones.map(entry => entry.name))) throw new Error('Bone list changed');
console.log('Rendered both phases and five action poses. '+JSON.stringify(report.poses.death));
