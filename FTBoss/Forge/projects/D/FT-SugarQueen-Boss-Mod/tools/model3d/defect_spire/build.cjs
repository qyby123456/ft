'use strict';

const fs = require('node:fs');
const path = require('node:path');
const { makeCanvas } = require('../lib_itemtex.cjs');
const { build: buildGeometry } = require('./geometry.cjs');
const { build: buildAnimations } = require('./animations.cjs');
const { build: buildWeapon } = require('./weapon.cjs');
const directory = path.resolve(process.argv[2] || path.join(__dirname, 'output'));
const resource = path.join(directory, 'assets', 'ftboss');
function write(relative, data) {
  const destination = path.join(resource, relative);
  fs.mkdirSync(path.dirname(destination), { recursive: true });
  fs.writeFileSync(destination, JSON.stringify(data, null, 2) + '\n');
}
const boss = buildGeometry();
const animations = buildAnimations(boss.geometry['minecraft:geometry'][0].bones.map(bone => bone.name));
write('geo/defect_spire.geo.json', boss.geometry);
write('animations/defect_spire.animation.json', animations);
fs.mkdirSync(path.join(resource, 'textures/entity'), { recursive: true });
for (const [name, tint] of Object.entries({ glass: [94, 219, 236], lightning: [251, 215, 81], frost: [151, 214, 254], ion: [236, 146, 229], dark: [149, 92, 226] })) {
  const { base, glow } = boss.texture.paint(tint);
  base.save(path.join(resource, 'textures/entity/defect_spire_' + name + '.png'));
  glow.save(path.join(resource, 'textures/entity/defect_spire_' + name + '_glowmask.png'));
}
const weapon = buildWeapon();
write('models/item/defect_claw.json', weapon.model);
fs.mkdirSync(path.join(resource, 'textures/item'), { recursive: true });
const paintedWeapon = weapon.texture.paint();
paintedWeapon.base.save(path.join(resource, 'textures/item/defect_claw.png'));
paintedWeapon.glow.save(path.join(resource, 'textures/item/defect_claw_e.png'));
const particle = makeCanvas(16, 16);
for (let row = 0; row < 16; row++) for (let column = 0; column < 16; column++) particle.put(column, row,
  (column === 7 || column === 8) ? [85, 210, 222] : (row % 7 === 0 ? [103, 81, 49] : [178, 139, 68]));
particle.save(path.join(resource, 'textures/item/defect_claw_particle.png'));
fs.writeFileSync(path.join(directory, 'manifest.json'), JSON.stringify({
  bossBones: boss.geometry['minecraft:geometry'][0].bones.length,
  bossCubes: boss.geometry['minecraft:geometry'][0].bones.reduce((total, bone) => total + bone.cubes.length, 0),
  animations: Object.fromEntries(Object.entries(animations.animations).map(([name, entry]) => [name, entry.animation_length])),
  weaponElements: weapon.model.elements.length, density: 1.5,
  rotationConvention: 'Specs use right-handed model coordinates. Bedrock export negates X/Z angles; offline pose inspection reverses that conversion after GeckoLib X reflection.'
}, null, 2) + '\n');
console.log('Generated Defect boss, 17 animations, five glow variants and charged claw at ' + directory);
