'use strict';

const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');
const space = require('./cube_space.cjs');
const { WINGS, PREFIX, sampleAnimation } = require('./cleaver_reference_motion.cjs');
const OUT = path.resolve(__dirname,'../../art/black_cleaver_reference');
const model = space.loadGeo(process.argv[2] || path.join(OUT,'black_cleaver.geo.json'),{ geckolib: true });
const cubes = space.listCubes(model);
const cross = (start,end,point) => (end[0]-start[0])*(point[1]-start[1])-(end[1]-start[1])*(point[0]-start[0]);
const area = polygon => Math.abs(polygon.reduce((sum,point,index) => {
  const next = polygon[(index+1)%polygon.length];
  return sum+point[0]*next[1]-point[1]*next[0];
},0))/2;

function intersection(subject,clip) {
  let result = subject;
  for (let index = 0; index < clip.length && result.length; index++) {
    const start = clip[index];
    const end = clip[(index+1)%clip.length];
    const input = result;
    result = [];
    for (let pointIndex = 0; pointIndex < input.length; pointIndex++) {
      const previous = input[(pointIndex+input.length-1)%input.length];
      const current = input[pointIndex];
      const previousDistance = cross(start,end,previous);
      const currentDistance = cross(start,end,current);
      if ((previousDistance >= 0) !== (currentDistance >= 0)) {
        const fraction = previousDistance/(previousDistance-currentDistance);
        result.push(previous.map((value,axis) => value+(current[axis]-value)*fraction));
      }
      if (currentDistance >= 0) result.push(current);
    }
  }
  return result;
}

function inside(point,polygon) {
  return polygon.every((start,index) => cross(start,polygon[(index+1)%polygon.length],point) >= -0.00001);
}

const visibleCollisions = [];
const angles = Array.from({ length: 45 },(_,index) => index*11/44);
const samples = angles.map(angle => ({ name: `wing:${angle}`,pose: Object.fromEntries(Object.entries(WINGS).map(([name,sign]) => [name,{ rotation: [0,0,angle*sign] }])) }));
const animations = JSON.parse(fs.readFileSync(path.join(OUT,'black_cleaver.animation.json'),'utf8')).animations;
for (const name of ['blood_enter','blood_idle','blood_slam','blood_recover','blood_exit']) {
  const clip = animations[PREFIX+name];
  for (let frame = 0; frame <= Math.ceil(clip.animation_length*60); frame++) {
    const pose = sampleAnimation(clip,Math.min(frame/60,clip.animation_length));
    delete pose.weapon;
    samples.push({ name: `${name}:${frame}`,pose });
  }
}
for (const { name,pose } of samples) {
  for (const side of [-1,1]) {
    const faces = cubes.map(info => {
      const corners = space.cubeCorners(model,info,pose);
      const points = (side === 1 ? [1,5,7,3] : [0,4,6,2]).map(index => corners[index]);
      return { name: info.name,plane: points[0][2]*side,points,bounds: [0,1].map(axis => [Math.min(...points.map(point => point[axis])),Math.max(...points.map(point => point[axis]))]) };
    });
    for (let first = 0; first < faces.length; first++) {
      const left = faces[first];
      for (let second = first+1; second < faces.length; second++) {
        const right = faces[second];
        if (Math.abs(left.plane-right.plane) > 0.00001 || left.bounds.some((range,axis) => range[0] >= right.bounds[axis][1] || range[1] <= right.bounds[axis][0])) continue;
        const polygon = intersection(left.points,right.points);
        if (polygon.length < 3 || area(polygon) < 0.0001) continue;
        const center = [0,1].map(axis => polygon.reduce((sum,point) => sum+point[axis],0)/polygon.length);
        const probes = [center,...polygon.map(point => [0,1].map(axis => point[axis]*0.9+center[axis]*0.1))];
        const visible = probes.some(point => !faces.some(face => face.plane > left.plane+0.00001 && inside(point,face.points)));
        if (visible) visibleCollisions.push({ sample: name,side,first: left.name,second: right.name,area: area(polygon),plane: left.plane });
      }
    }
  }
}
const result = { status: visibleCollisions.length ? 'FAIL' : 'PASS',poses: samples.length,sides: 2,visible_coplanar_overlaps: visibleCollisions };
fs.writeFileSync(process.argv[3] || path.join(OUT,'surface-validation.json'),JSON.stringify(result,null,2)+'\n');
console.log(JSON.stringify({ ...result,visible_coplanar_overlaps: visibleCollisions.slice(0,24),total: visibleCollisions.length },null,2));
assert.equal(visibleCollisions.length,0,'Visible coplanar surfaces can flicker in-game');
