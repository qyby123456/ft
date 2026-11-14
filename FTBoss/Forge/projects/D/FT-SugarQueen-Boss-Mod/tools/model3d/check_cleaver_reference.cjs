'use strict';

const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const assert = require('node:assert/strict');
const space = require('./cube_space.cjs');
const { decodePNG } = require('./png.cjs');
const { CLIPS, WINGS, PREFIX, sampleAnimation, sampleEffects } = require('./cleaver_reference_motion.cjs');

const OUT = path.resolve(__dirname,'../../art/black_cleaver_reference');
const load = name => JSON.parse(fs.readFileSync(path.join(OUT,name),'utf8'));
const model = space.loadGeo(path.join(OUT,'black_cleaver.geo.json'),{ geckolib: true });
const cubes = space.listCubes(model);
const project = load('black_cleaver.bbmodel');
const report = load('report.json');
const animation = load('black_cleaver.animation.json');
const effects = load('black_cleaver.effects.json');
const primary = decodePNG(path.join(OUT,'black_cleaver_geo.png'));
const hash = buffer => crypto.createHash('sha256').update(buffer).digest('hex');
const checks = [];
assert.equal(cubes.length,project.elements.length);
assert.equal(cubes.length,report.cubes);
assert.equal(model.bones.length,17);
assert.deepEqual(Object.keys(animation.animations),Object.keys(CLIPS).map(name => PREFIX+name));
assert.equal(project.animations.length,Object.keys(CLIPS).length);
assert.equal(hash(fs.readFileSync(path.join(OUT,'reference.png'))),report.source_sha256);
assert.equal(hash(Buffer.from(project.textures[0].source.split(',')[1],'base64')),
  hash(fs.readFileSync(path.join(OUT,'black_cleaver_geo.png'))));
checks.push('Reference and embedded texture hashes match');

const elementIds = new Set(project.elements.map(element => element.uuid));
const groupIds = new Set();
const references = [];
function walk(entries) {
  for (const entry of entries) {
    if (typeof entry === 'string') {
      assert.ok(elementIds.has(entry));
      references.push(entry);
    } else {
      assert.ok(!groupIds.has(entry.uuid));
      groupIds.add(entry.uuid);
      assert.deepEqual(entry.origin,space.geckoNativePosition(model.byName.get(entry.name).pivot));
      walk(entry.children);
    }
  }
}
walk(project.outliner);
assert.equal(references.length,cubes.length);
assert.equal(new Set(references).size,cubes.length);
assert.equal(groupIds.size,model.bones.length);
for (const clip of project.animations) {
  const source = animation.animations[clip.name];
  assert.ok(source);
  assert.equal(clip.length,source.animation_length);
  assert.equal(clip.loop,source.loop ? 'loop' : 'once');
  for (const [groupId,animator] of Object.entries(clip.animators)) {
    assert.ok(groupIds.has(groupId));
    const sourceChannels = source.bones[animator.name];
    assert.ok(sourceChannels);
    assert.equal(animator.keyframes.length,Object.values(sourceChannels).reduce((sum,track) => sum+Object.keys(track).length,0));
    for (const key of animator.keyframes) {
      assert.ok(Number.isFinite(key.time) && key.time <= clip.length);
      assert.equal(key.data_points.length,1);
      assert.ok(Object.values(key.data_points[0]).every(value => Number.isFinite(Number(value))));
      const sourceKey = Object.keys(sourceChannels[key.channel]).find(time => Number(time) === key.time);
      assert.ok(sourceKey);
      const nativeValue = sourceChannels[key.channel][sourceKey];
      assert.deepEqual(['x','y','z'].map(axis => Number(key.data_points[0][axis])),
        key.channel === 'rotation' ? space.geckoNativeRotation(nativeValue) : nativeValue);
    }
  }
}
checks.push('Blockbench hierarchy and animation keyframes are internally consistent');

const textures = ['black_cleaver_geo.png','black_cleaver_emissive.png','black_cleaver_blood_emissive.png',...Object.values(effects.textures)];
for (const filename of textures) {
  const image = decodePNG(path.join(OUT,filename));
  assert.equal(image.w,256);
  assert.equal(image.h,256);
  assert.ok(image.data.some((value,index) => index%4 === 3 && value === 255));
}
let sampled = 0;
for (const info of cubes) {
  assert.ok(!info.cube.rotation || info.cube.pivot?.length === 3);
  const entry = project.elements.find(element =>
    element.from.every((value,axis) => Math.abs(value-(axis === 0 ? -info.cube.origin[axis]-info.cube.size[axis] : info.cube.origin[axis])) < 1e-8) &&
    JSON.stringify(element.rotation) === JSON.stringify(space.geckoNativeRotation(info.cube.rotation || [0,0,0])) &&
    element.to.every((value,axis) => Math.abs(value-(axis === 0 ? -info.cube.origin[axis] : info.cube.origin[axis]+info.cube.size[axis])) < 1e-8));
  assert.ok(entry, 'Blockbench cube differs from geometry');
  for (const face of Object.values(info.cube.uv)) {
    const lowerU = Math.min(face.uv[0],face.uv[0]+face.uv_size[0]);
    const upperU = Math.max(face.uv[0],face.uv[0]+face.uv_size[0]);
    const lowerV = Math.min(face.uv[1],face.uv[1]+face.uv_size[1]);
    const upperV = Math.max(face.uv[1],face.uv[1]+face.uv_size[1]);
    assert.ok(lowerU >= 0 && lowerV >= 0 && upperU <= 256 && upperV <= 256);
    for (let pixelY = Math.floor(lowerV); pixelY < Math.ceil(upperV); pixelY++) {
      for (let pixelX = Math.floor(lowerU); pixelX < Math.ceil(upperU); pixelX++) {
        assert.equal(primary.data[(pixelY*primary.w+pixelX)*4+3],255,'Unpainted face');
        sampled++;
      }
    }
  }
}
checks.push(`${sampled} UV texels checked; no blank faces or out-of-bounds UVs`);

function prepare(info,pose) {
  const corners = space.cubeCorners(model,info,pose);
  return {
    polygon: [0,4,6,2].map(index => corners[index]),
    bounds: [0,1,2].map(axis => [Math.min(...corners.map(point => point[axis])),Math.max(...corners.map(point => point[axis]))])
  };
}
function touching(left,right) {
  const tolerance = 0.00002;
  if (left.bounds.some((interval,axis) => interval[1] < right.bounds[axis][0]-tolerance || right.bounds[axis][1] < interval[0]-tolerance)) return false;
  for (const polygon of [left.polygon,right.polygon]) {
    for (let index = 0; index < 2; index++) {
      const first = polygon[index];
      const second = polygon[index+1];
      const axis = [first[1]-second[1],second[0]-first[0]];
      const norm = Math.hypot(...axis);
      const project = point => (point[0]*axis[0]+point[1]*axis[1])/norm;
      const leftSpan = left.polygon.map(project);
      const rightSpan = right.polygon.map(project);
      if (Math.max(...leftSpan) < Math.min(...rightSpan)-tolerance || Math.max(...rightSpan) < Math.min(...leftSpan)-tolerance) return false;
    }
  }
  return true;
}
const connectivity = [];
function componentsFor(pose) {
  const prepared = cubes.map(info => prepare(info,pose));
  const parents = cubes.map((entry,index) => index);
  function find(index) {
    while (parents[index] !== index) {
      parents[index] = parents[parents[index]];
      index = parents[index];
    }
    return index;
  }
  for (let left = 0; left < cubes.length; left++) {
    for (let right = left+1; right < cubes.length; right++) {
      if (touching(prepared[left],prepared[right])) parents[find(right)] = find(left);
    }
  }
  const groups = new Map();
  for (let index = 0; index < cubes.length; index++) {
    const key = find(index);
    if (!groups.has(key)) groups.set(key,[]);
    groups.get(key).push(cubes[index].name);
  }
  const components = [...groups.values()].sort((left,right) => right.length-left.length);
  return components;
}
for (const angle of [0,2.75,5.5,8.25,11]) {
  const pose = Object.fromEntries(Object.entries(report.wings).map(([name,sign]) => [name,{ rotation: [0,0,sign*angle] }]));
  const components = componentsFor(pose);
  connectivity.push({ angle, components: components.map(entries => ({ size: entries.length, examples: entries.slice(0,5) })) });
}
checks.push('Five simultaneous four-wing poses checked using full cube/bone transforms and rectangle SAT');

const near = (left,right,label) => {
  assert.equal(Array.isArray(left),Array.isArray(right),label);
  if (Array.isArray(left)) {
    assert.equal(left.length,right.length,label);
    left.forEach((value,index) => near(value,right[index],label));
  } else assert.ok(Math.abs(left-right) < 0.00002,`${label}: ${left} != ${right}`);
};
const poseNear = (left,right,label) => {
  const names = new Set([...Object.keys(left),...Object.keys(right)]);
  for (const name of names) {
    for (const channel of ['rotation','position','scale']) {
      const fallback = channel === 'scale' ? [1,1,1] : [0,0,0];
      near(left[name]?.[channel] || fallback,right[name]?.[channel] || fallback,`${label}:${name}.${channel}`);
    }
  }
};
let keyframes = 0;
for (const [name,clip] of Object.entries(animation.animations)) {
  assert.ok(!clip.bones.root && !clip.bones.grip_anchor,`${name}: grip anchors must not be animated`);
  assert.ok(!clip.bones.weapon?.position && !clip.bones.weapon?.scale,`${name}: weapon must pivot without grip drift`);
  const effect = effects.clips[name];
  assert.equal(effect.duration,clip.animation_length);
  for (const channels of Object.values(clip.bones)) {
    for (const track of Object.values(channels)) {
      const times = Object.keys(track).map(Number);
      assert.ok(times.every((value,index) => Number.isFinite(value) && (index === 0 || value > times[index-1])));
      near(times[0],0,name);
      near(times[times.length-1],clip.animation_length,name);
      keyframes += times.length;
    }
  }
  for (const track of Object.values(effect.tracks)) {
    const times = Object.keys(track).map(Number);
    assert.ok(times.every((value,index) => Number.isFinite(value) && (index === 0 || value > times[index-1])));
    assert.ok(Object.values(track).every(value => Number.isFinite(value) && value >= 0 && value <= 1.1));
  }
  if (clip.loop) {
    poseNear(sampleAnimation(clip,0),sampleAnimation(clip,clip.animation_length),`${name} loop`);
    const first = sampleEffects(effect,0);
    const last = sampleEffects(effect,effect.duration);
    for (const channel of Object.keys(first)) near(first[channel],last[channel],`${name}.${channel} loop`);
  }
}
const transitions = [['blood_enter','blood_idle'],['blood_idle','blood_slam'],['blood_slam','blood_recover'],
  ['blood_recover','blood_idle'],['blood_idle','blood_exit'],['blood_exit','idle'],['slam','recover'],['recover','idle']];
for (const [before,after] of transitions) {
  const first = animation.animations[PREFIX+before];
  const last = animation.animations[PREFIX+after];
  poseNear(sampleAnimation(first,first.animation_length),sampleAnimation(last,0),`${before} -> ${after}`);
  const firstEffects = sampleEffects(effects.clips[PREFIX+before],first.animation_length);
  const lastEffects = sampleEffects(effects.clips[PREFIX+after],0);
  for (const channel of Object.keys(firstEffects)) near(firstEffects[channel],lastEffects[channel],`${before} -> ${after}: ${channel}`);
}
checks.push(`${keyframes} exported bone keyframes match Blockbench; loops and eight clip transitions are continuous`);

const motionSamples = [];
const motionErrors = [];
function checkMotionPose(pose,label) {
  for (const [name,sign] of Object.entries(WINGS)) {
    const angle = (pose[name]?.rotation?.[2] || 0)*sign;
    assert.ok(angle >= -0.00001 && angle <= 11.00001,`${label}: unsafe wing angle ${angle}`);
  }
  const internal = structuredClone(pose);
  delete internal.weapon;
  for (const channels of Object.values(internal)) {
    assert.ok(!channels.rotation || (channels.rotation[0] === 0 && channels.rotation[1] === 0));
    assert.ok(!channels.scale || Math.abs(channels.scale[0]-channels.scale[1]) < 0.00001);
  }
  const components = componentsFor(internal);
  if (components.length !== 1) motionErrors.push({ label,components: components.map(entries => ({ count: entries.length,examples: entries.slice(0,3) })) });
}
for (const name of ['blood_enter','blood_idle','blood_slam','blood_recover','blood_exit']) {
  const clip = animation.animations[PREFIX+name];
  const count = Math.ceil(clip.animation_length*60);
  for (let frame = 0; frame <= count; frame++) {
    const time = Math.min(clip.animation_length,frame/60);
    checkMotionPose(sampleAnimation(clip,time),`${name}@${time.toFixed(4)}`);
  }
  motionSamples.push({ clip: name,samples: count+1 });
}
for (const start of [0.2,0.55,0.9]) {
  const pose = sampleAnimation(animation.animations[PREFIX+'blood_enter'],start);
  for (let frame = 0; frame <= 12; frame++) {
    const amount = frame/12;
    const faded = Object.fromEntries(Object.entries(pose).map(([name,channels]) => [name,
      Object.fromEntries(Object.entries(channels).map(([channel,values]) => [channel,
        values.map(value => value*(1-amount)+(channel === 'scale' ? amount : 0))]))]));
    checkMotionPose(faded,`interrupted_enter@${start}:${frame}`);
  }
}
checks.push('Blood animation internal connectivity sampled at 60 fps; 39 interrupted-entry fade poses checked; rigid weapon swing preserves the grip pivot');
const errors = connectivity.filter(entry => entry.components.length > 1);
const result = { status: errors.length || motionErrors.length ? 'REVIEW' : 'PASS', checks, connectivity,
  animation: { clips: Object.keys(CLIPS).length,keyframes,transitions: transitions.length,motion_samples: motionSamples,
    interruption_samples: 39,errors: motionErrors },game_tested: false };
fs.writeFileSync(path.join(OUT,'validation.json'),JSON.stringify(result,null,2)+'\n');
console.log(JSON.stringify(result,null,2));
process.exitCode = result.status === 'PASS' ? 0 : 1;
