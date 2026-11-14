'use strict';

const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');
const { types } = require('./build.cjs');
const project = path.resolve(__dirname, '../../..');
const output = path.resolve(process.argv[2]);
const resources = path.join(project, 'src/main/resources/assets/ftboss');
const animationPath = path.join(resources, 'animations/defect_spire.animation.json');
const existing = JSON.parse(fs.readFileSync(animationPath));
const geometry = JSON.parse(fs.readFileSync(path.join(resources, 'geo/defect_spire.geo.json')));
const generated = require('../defect_spire/animations.cjs').build(geometry['minecraft:geometry'][0].bones.map(bone => bone.name));
const key = 'animation.defect_core.cast_dark';
assert.equal(existing.animations[key].animation_length, generated.animations[key].animation_length);
existing.animations[key] = generated.animations[key];
fs.writeFileSync(animationPath, JSON.stringify(existing, null, 2) + '\n');
for (const type of Object.keys(types)) for (const relative of ['geo/orb_link_' + type + '.geo.json',
  'animations/orb_link_' + type + '.animation.json', 'textures/entity/orb_link_' + type + '.png',
  'textures/entity/orb_link_' + type + '_glowmask.png']) {
  fs.copyFileSync(path.join(output, 'assets/ftboss', relative), path.join(resources, relative));
}
console.log('Installed 20 orb assets and the dark release animation only.');
