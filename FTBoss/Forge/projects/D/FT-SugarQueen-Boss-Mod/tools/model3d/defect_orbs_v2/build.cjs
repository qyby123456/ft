'use strict';

const fs = require('node:fs');
const path = require('node:path');
const { atlas } = require('../defect_spire/materials.cjs');
const { rotateXYZ } = require('../cube_space.cjs');
const types = {
  glass: { title: '玻璃 · 棱晶容器', tint: [90, 222, 229] },
  lightning: { title: '雷电 · 六极电容', tint: [255, 219, 82] },
  frost: { title: '冰霜 · 六瓣晶簇', tint: [151, 218, 255] },
  ion: { title: '离子 · 交叉磁环', tint: [239, 143, 231] },
  dark: { title: '黑暗 · 破裂束缚核', tint: [157, 95, 234] }
};
const center = [0, 4, 0];
const vector = amount => [amount, amount, amount];
const exportedRotation = angles => angles.map((value, axis) => value === 0 ? 0 : axis === 1 ? value : -value);

function geometry(type) {
  const texture = atlas(256, 3);
  const bones = [];
  function bone(name, parent = 'root', pivot = center, rotation = [0, 0, 0]) {
    const entry = { name, pivot, rotation: exportedRotation(rotation), cubes: [] };
    if (parent) entry.parent = parent;
    bones.push(entry);
    return entry;
  }
  function cube(owner, position, size, material, rotation = [0, 0, 0]) {
    owner.cubes.push({ origin: position.map((value, axis) => value - size[axis] / 2), size,
      pivot: position, rotation: exportedRotation(rotation), uv: texture.uv(size, material, owner.name) });
  }
  function ring(owner, radius, material, count = 12, thickness = 0.5) {
    for (let index = 0; index < count; index++) {
      const angle = index * 360 / count;
      const point = rotateXYZ([0, radius, 0], [0, 0, angle]).map((value, axis) => value + center[axis]);
      cube(owner, point, [radius * Math.PI * 1.68 / count, thickness, thickness], material, [0, 0, angle]);
    }
  }
  bone('root', null);
  const core = bone('core');
  const shell = bone('shell');
  const front = bone('ring_front', 'shell');
  const rear = bone('ring_rear', 'shell', center, [64, 32, 0]);
  cube(core, center, [2.5, 2.5, 2.5], 'core', [0, 0, 45]);
  cube(core, center, [2.1, 2.1, 2.1], 'core', [45, 45, 0]);
  cube(core, [0, 4.05, -1.6], [0.7, 0.9, 0.35], 'white', [0, 0, 45]);
  if (type === 'glass') {
    ring(front, 2.75, 'edge', 12, 0.35);
    ring(rear, 2.9, 'bronze', 12, 0.4);
    for (let index = 0; index < 4; index++) {
      const plate = bone('petal_' + index, 'shell', center, [0, index * 90, 0]);
      cube(plate, [0, 4, -2.3], [1.9, 3.1, 0.5], 'core', [0, 0, 45]);
      cube(plate, [0.6, 4.5, -2.64], [0.3, 1.1, 0.15], 'white', [0, 0, -25]);
      cube(plate, [0, 6.5, -1.1], [0.7, 1.5, 0.8], 'brass', [-28, 0, 0]);
      cube(plate, [0, 1.5, -1.1], [0.7, 1.5, 0.8], 'brass', [28, 0, 0]);
    }
  } else if (type === 'lightning') {
    ring(front, 2.8, 'bronze', 12, 0.5);
    for (let index = 0; index < 6; index++) {
      const electrode = bone('petal_' + index, 'shell', center, [0, 0, index * 60]);
      cube(electrode, [0, 7.3, 0], [1.5, 1.2, 1.5], 'iron');
      cube(electrode, [0, 8.15, 0], [1.05, 0.6, 1.05], 'edge');
      cube(electrode, [0, 6.4, 0], [0.5, 0.65, 0.5], 'core');
      cube(electrode, [0, 7.35, -0.8], [0.75, 0.35, 0.15], 'core');
    }
    for (let index = 0; index < 3; index++)
      cube(core, [(index % 2 ? -1 : 1) * 0.32, 5.1 - index * 1.05, -1.95], [0.5, 1.4, 0.45], 'white', [0, 0, -36]);
    ring(rear, 2.65, 'core', 8, 0.22);
  } else if (type === 'frost') {
    ring(rear, 2.7, 'steel', 12, 0.32);
    for (let index = 0; index < 6; index++) {
      const crystal = bone('petal_' + index, 'shell', center, [0, 0, index * 60]);
      cube(crystal, [0, 7.35, 0], [0.8, 2.3, 0.8], 'core');
      cube(crystal, [0, 8.4, 0], [0.6, 0.85, 0.6], 'white', [0, 0, 45]);
      for (const side of [-1, 1])
        cube(crystal, [side * 0.7, 7.25, 0], [0.45, 1.6, 0.45], 'core', [0, 0, side * -42]);
      cube(crystal, [0, 6.45, 0], [1.15, 0.35, 1.1], 'steel');
    }
    cube(core, [0, 4, -1.5], [1.8, 1.8, 0.5], 'white', [0, 0, 45]);
  } else if (type === 'ion') {
    front.rotation = exportedRotation([24, -28, -24]);
    rear.rotation = exportedRotation([72, 45, 38]);
    ring(front, 3.6, 'edge', 16, 0.45);
    ring(rear, 3.6, 'core', 16, 0.4);
    for (let index = 0; index < 4; index++) {
      const node = bone('petal_' + index, index % 2 ? 'ring_rear' : 'ring_front', center, [0, 0, index * 90]);
      cube(node, [0, 7.6, 0], [1.1, 1.1, 1.1], 'iron', [0, 0, 45]);
      cube(node, [0, 7.6, -0.75], [0.8, 0.8, 0.35], 'core', [0, 0, 45]);
    }
    cube(core, center, [0.65, 4.6, 0.65], 'core');
  } else {
    ring(front, 3.05, 'bronze', 12, 0.48);
    ring(rear, 3.3, 'edge', 10, 0.32);
    cube(core, center, [2.75, 2.75, 2.75], 'joint', [22, 0, 45]);
    cube(core, [0, 4, -1.7], [0.35, 2.5, 0.3], 'core', [0, 0, -24]);
    for (let index = 0; index < 6; index++) {
      const fragment = bone('petal_' + index, 'shell', center, [index % 2 * 24, 0, index * 60]);
      cube(fragment, [0, 7.25, 0], [1.55, 1.65, 1.8], 'joint', [0, 0, 15]);
      cube(fragment, [0, 6.6, -0.9], [1.35, 0.4, 0.35], 'core', [0, 0, 15]);
      cube(fragment, [0.3, 7.2, -1], [0.25, 1, 0.2], 'core', [0, 0, 15]);
    }
  }
  return { texture, bones, geometry: { format_version: '1.12.0', 'minecraft:geometry': [{
    description: { identifier: 'geometry.orb_link_' + type, texture_width: 256, texture_height: 256,
      visible_bounds_width: 2, visible_bounds_height: 2, visible_bounds_offset: [0, 0.25, 0] }, bones }] } };
}

function animations(bones, type) {
  const result = {};
  for (const [name, duration, loop] of [['idle', 3.2, true], ['summon', 0.5, false], ['focus', 0.6, true],
    ['dash', 0.4, true], ['return', 0.8, true], ['beam', 0.8, true], ['drain', 0.3, false], ['burst', 0.3, false]]) {
    const animated = {};
    for (const bone of bones) {
      const channels = { rotation: {}, position: {}, scale: {} };
      for (let frame = 0; frame <= 8; frame++) {
        const progress = frame / 8;
        const pulse = Math.sin(progress * Math.PI * 2);
        const halfPulse = Math.sin(progress * Math.PI);
        let rotation = [0, 0, 0], position = [0, 0, 0], scale = [1, 1, 1];
        const energized = name === 'focus' || name === 'beam';
        if (bone.name === 'root') {
          if (name === 'summon') { scale = vector(0.04 + 0.96 * Math.min(1, progress * 1.3)); rotation = [0, (1 - progress) * 135, 0]; }
          if (name === 'drain') scale = vector(1 - 0.98 * progress);
          if (name === 'burst') scale = vector(1 - 0.96 * progress);
          if (name === 'dash') { rotation = [0, 0, pulse * 12]; scale = [0.9, 0.9, 1.18]; }
          if (name === 'idle') position = [0, pulse * 0.15, 0];
        } else if (bone.name === 'core') {
          scale = vector(1 + pulse * (energized ? 0.16 : 0.055));
          rotation = [pulse * 8, halfPulse * 28, pulse * 6];
          if (type === 'dark') scale = vector(1 + pulse * (energized ? 0.18 : 0.085));
        } else if (bone.name.startsWith('ring_')) {
          const sign = bone.name === 'ring_front' ? 1 : -1;
          rotation = [pulse * 12 * sign, pulse * 18, halfPulse * (energized ? 80 : 34) * sign];
        } else if (bone.name.startsWith('petal_')) {
          position = [0, energized ? 0.3 + pulse * 0.24 : halfPulse * 0.1, 0];
          if (name === 'burst') position = [0, progress * 5, -progress * 1.5];
          if (name === 'drain') position = [0, -progress * 0.8, 0];
          rotation = [energized ? pulse * 8 : 0, 0, name === 'burst' ? progress * 60 : pulse * 3];
        }
        const stamp = (duration * progress).toFixed(4);
        channels.rotation[stamp] = exportedRotation(rotation).map(value => Math.abs(value) < 1e-8 ? 0 : Number(value.toFixed(4)));
        channels.position[stamp] = position.map(value => Number(value.toFixed(4)) || 0);
        channels.scale[stamp] = scale.map(value => Number(value.toFixed(4)));
      }
      animated[bone.name] = channels;
    }
    result['animation.orb.' + name] = { loop: loop ? true : 'hold_on_last_frame', animation_length: duration, bones: animated };
  }
  return { format_version: '1.8.0', animations: result };
}

function build(directory) {
  const resource = path.join(directory, 'assets/ftboss');
  const manifest = [];
  function write(relative, content) {
    const filename = path.join(resource, relative);
    fs.mkdirSync(path.dirname(filename), { recursive: true });
    fs.writeFileSync(filename, JSON.stringify(content, null, 2) + '\n');
  }
  fs.mkdirSync(path.join(resource, 'textures/entity'), { recursive: true });
  for (const [type, settings] of Object.entries(types)) {
    const model = geometry(type);
    write('geo/orb_link_' + type + '.geo.json', model.geometry);
    write('animations/orb_link_' + type + '.animation.json', animations(model.bones, type));
    const painted = model.texture.paint(settings.tint);
    painted.base.save(path.join(resource, 'textures/entity/orb_link_' + type + '.png'));
    painted.glow.save(path.join(resource, 'textures/entity/orb_link_' + type + '_glowmask.png'));
    manifest.push({ type, bones: model.bones.length, cubes: model.bones.reduce((total, bone) => total + bone.cubes.length, 0), animations: 8 });
  }
  fs.writeFileSync(path.join(directory, 'orb-manifest.json'), JSON.stringify(manifest, null, 2) + '\n');
  console.log(manifest);
}

if (require.main === module) build(path.resolve(process.argv[2]));
module.exports = { geometry, animations, types, build };
