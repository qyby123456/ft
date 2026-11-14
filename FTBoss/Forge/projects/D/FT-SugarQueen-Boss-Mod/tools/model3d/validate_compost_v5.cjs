'use strict';

const fs = require('fs');
const path = require('path');
const assert = require('assert/strict');
const {decodePNG} = require('./png.cjs');
const {loadGeo,listCubes,cubeCorners} = require('./cube_space.cjs');
const directory = path.resolve(process.argv[2]);
const root = path.resolve(__dirname,'../..');
const geometry = loadGeo(path.join(directory,'compost_superman.geo.json'));
const second = loadGeo(path.join(directory,'compost_superman_p2.geo.json'));
const manifest = JSON.parse(fs.readFileSync(path.join(directory,'model_manifest.json'),'utf8'));
const animations = JSON.parse(fs.readFileSync(path.join(directory,'compost_superman.animation.json'),'utf8')).animations;
const errors = [];
const check = (condition,message) => { if (!condition) errors.push(message); };
check(JSON.stringify(geometry.json) === JSON.stringify(second.json),'Phase geometries differ');
const cubes = listCubes(geometry);
check(cubes.length === manifest.cubes,'Manifest cube count differs');
check(geometry.bones.length === manifest.bones,'Manifest bone count differs');
check(new Set(manifest.parts.map(part => part.name)).size === cubes.length,'Duplicate cube names');
const required = ['root','torso','head','arm_l','arm_r','hand_r','leg_l','leg_r','cape1','cape2','cape3','cape4','cannon','fly1','fly2'];
required.forEach(name => check(geometry.byName.has(name),'Missing bone '+name));
check(geometry.byName.get('hand_r').parent === 'forearm_r','Held-item anchor does not follow forearm');
const java = fs.readFileSync(path.join(root,'src/main/java/net/farmingtales/ftboss/entity/CompostBossEntity.java'),'utf8');
const animationNames = [...java.matchAll(/then(?:Loop|Play)\("(animation\.compost_superman\.[^"]+)"\)/g)].map(match => match[1]);
check(animationNames.length === Object.keys(animations).length,'Animation count differs from Java controller');
animationNames.forEach(name => check(Boolean(animations[name]),'Missing runtime animation '+name));
const constants = {cannon_fire:'T_FIRE',gas_charge:'T_CHARGE',phase_shift:'T_SHIFT',transform:'T_TRANSFORM',hurt:'T_HURT'};
for (const [name,constant] of Object.entries(constants)) {
  const ticks = Number(java.match(new RegExp(constant+'\\s*=\\s*([0-9.]+)'))?.[1]);
  check(animations['animation.compost_superman.'+name].animation_length*20 === ticks,'Timing mismatch '+name);
}
for (const [name,animation] of Object.entries(animations)) {
  for (const [owner,channels] of Object.entries(animation.bones)) {
    check(geometry.byName.has(owner),'Unknown animated bone '+owner);
    for (const [channel,frames] of Object.entries(channels)) {
      const keys = Object.keys(frames);
      check(keys.every(key => /^\d+\.\d{4}$/.test(key)),'Unsafe JS integer time keys '+name);
      check(keys.every((key,index) => index === 0 || Number(key)>Number(keys[index-1])),'Unsorted time keys '+name);
      check(Number(keys.at(-1)) === animation.animation_length,'Track ends early '+name+'/'+owner+'/'+channel);
      if (animation.loop === true) check(JSON.stringify(frames[keys[0]]) === JSON.stringify(frames[keys.at(-1)]),'Loop discontinuity '+name+'/'+owner);
      for (const values of Object.values(frames)) check(values.length === 3 && values.every(Number.isFinite),'Invalid keyframe '+name);
    }
  }
}
const textures = {};
for (const suffix of ['', '_p2']) {
  const name = 'compost_superman'+suffix;
  const texture = decodePNG(path.join(directory,name+'.png'));
  const glow = decodePNG(path.join(directory,name+'_glowmask.png'));
  check(texture.w === geometry.textureWidth && texture.h === geometry.textureHeight,'Texture dimensions '+name);
  check(texture.w === glow.w && texture.h === glow.h,'Glow dimensions '+name);
  let covered = 0;
  for (const item of cubes) {
    check(Object.keys(item.cube.uv).length === 6,'Incomplete UV '+item.name);
    for (const [face,mapping] of Object.entries(item.cube.uv)) {
      const [left,top] = mapping.uv;
      const [width,height] = mapping.uv_size;
      check(left >= 0 && top >= 0 && left+width <= texture.w && top+height <= texture.h,'Out-of-bounds UV '+item.name+'/'+face);
      for (let pixelY = top; pixelY < top+height; pixelY++) {
        for (let pixelX = left; pixelX < left+width; pixelX++) {
          check(texture.data[(pixelY*texture.w+pixelX)*4+3] === 255,'Transparent UV '+item.name+'/'+face);
          covered++;
        }
      }
    }
  }
  const bb = JSON.parse(fs.readFileSync(path.join(directory,name+'.bbmodel'),'utf8'));
  check(bb.elements.length === cubes.length,'Blockbench cube count '+name);
  check(bb.animations.length === animationNames.length,'Blockbench animation count '+name);
  check(Buffer.from(bb.textures[0].source.split(',')[1],'base64').equals(fs.readFileSync(path.join(directory,name+'.png'))),'Embedded texture mismatch '+name);
  const placed = [];
  function walk(entries) {
    for (const entry of entries) {
      if (typeof entry === 'string') placed.push(entry);
      else walk(entry.children);
    }
  }
  walk(bb.outliner);
  check(placed.length === cubes.length && new Set(placed).size === cubes.length,'Blockbench hierarchy '+name);
  let emissivePixels = 0;
  for (let offset = 3; offset < glow.data.length; offset += 4) {
    if (glow.data[offset]) {
      emissivePixels++;
      check(texture.data[offset] === 255,'Glow outside texture '+name);
    }
  }
  check(emissivePixels > 0,'Empty glow mask '+name);
  textures[name] = {uvTexelSamples:covered,emissivePixels};
}
check(textures.compost_superman_p2.emissivePixels > textures.compost_superman.emissivePixels,'Phase two glow not increased');
const points = cubes.flatMap(item => cubeCorners(geometry,item));
const minimum = [0,1,2].map(axis => Math.min(...points.map(point => point[axis])));
const maximum = [0,1,2].map(axis => Math.max(...points.map(point => point[axis])));
check(minimum[1] >= -0.001,'Rest pose below floor');
check(maximum[1]/16 <= 3.55,'Model exceeds intended boss height');
const report = {passed:errors.length === 0,errors,bones:geometry.bones.length,cubes:cubes.length,
  animations:animationNames.length,textures,staticBounds:{minimum,maximum},
  hitbox:{width:1.6,height:3.4},notes:['Hands, shoulders and decorative flies extend outside the damage hitbox.',
    'Offline validation does not replace Minecraft/Blockbench visual acceptance; held cannon uses unchanged renderer.']};
fs.writeFileSync(path.join(directory,'validation.json'),JSON.stringify(report,null,2)+'\n');
console.log(JSON.stringify(report,null,2));
assert.equal(errors.length,0,'Compost v5 validation failed');
