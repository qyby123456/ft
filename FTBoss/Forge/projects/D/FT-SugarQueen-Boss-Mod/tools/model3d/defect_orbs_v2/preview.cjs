'use strict';

const fs = require('node:fs');
const path = require('node:path');
const { createCanvas } = require(path.join(process.env.FT_NODE_MODULES || 'C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules', '@napi-rs/canvas'));
const { loadGeo, listCubes, cubeCorners } = require('../cube_space.cjs');
const { decodePNG } = require('../png.cjs');
const { types } = require('./build.cjs');
const directory = path.resolve(process.argv[2]);
process.argv[2] = path.resolve(__dirname, '../../../src/main/resources');
const { render, bossParts, textures } = require('../defect_spire/preview.cjs');
process.argv[2] = directory;
const loaded = Object.fromEntries(Object.keys(types).map(type => {
  const model = loadGeo(path.join(directory, 'assets/ftboss/geo/orb_link_' + type + '.geo.json'));
  for (const bone of model.bones) {
    bone.rotation = bone.rotation.map((value, axis) => axis === 1 ? value : -value);
    for (const cube of bone.cubes) cube.rotation = cube.rotation.map((value, axis) => axis === 1 ? value : -value);
  }
  return [type, { model, cubes: listCubes(model),
    texture: decodePNG(path.join(directory, 'assets/ftboss/textures/entity/orb_link_' + type + '.png')),
    animations: JSON.parse(fs.readFileSync(path.join(directory, 'assets/ftboss/animations/orb_link_' + type + '.animation.json'))).animations }];
}));

function parts(type, state, time) {
  const { model, cubes, animations } = loaded[type];
  const animation = animations['animation.orb.' + state];
  const moment = animation.loop === true ? time % animation.animation_length : Math.min(time, animation.animation_length);
  const pose = Object.fromEntries(Object.entries(animation.bones).map(([name, channels]) => [name,
    Object.fromEntries(Object.entries(channels).map(([channel, frames]) => {
      const entries = Object.entries(frames).map(([stamp, value]) => [Number(stamp), value]).sort((left, right) => left[0] - right[0]);
      let start = entries[0], end = entries.at(-1);
      for (let index = 1; index < entries.length; index++) if (moment <= entries[index][0]) { start = entries[index - 1]; end = entries[index]; break; }
      const progress = Math.max(0, Math.min(1, (moment - start[0]) / Math.max(0.00001, end[0] - start[0])));
      return [channel, start[1].map((value, axis) => (value + (end[1][axis] - value) * progress) * (channel === 'rotation' && axis !== 1 ? -1 : 1))];
    }))]));
  return cubes.map(entry => ({ points: cubeCorners(model, entry, pose), faces: entry.cube.uv }));
}

function text(context, value, horizontal, vertical, size, color = '#bfcdce') {
  context.font = size + 'px Yahei'; context.fillStyle = color; context.fillText(value, horizontal, vertical);
}
function backdrop(width, height) {
  const canvas = createCanvas(width, height), context = canvas.getContext('2d');
  context.fillStyle = '#10191d'; context.fillRect(0, 0, width, height);
  return { canvas, context };
}
function sheet() {
  const { canvas, context } = backdrop(1600, 640);
  text(context, 'DEFECT  /  五种充能球', 38, 53, 30, '#f0d7a1');
  text(context, '独立几何 · 实际贴图 UV · 每种八段骨骼动作 · 与核心 / 手部联动', 38, 86, 18);
  let index = 0;
  for (const [type, settings] of Object.entries(types)) {
    const left = 24 + index * 315;
    context.fillStyle = '#19272d'; context.fillRect(left, 111, 304, 491);
    const tint = '#' + settings.tint.map(value => value.toString(16).padStart(2, '0')).join('');
    context.fillStyle = tint; context.fillRect(left, 111, 304, 3);
    const glow = context.createRadialGradient(left + 152, 289, 15, left + 152, 289, 145);
    glow.addColorStop(0, tint + '28'); glow.addColorStop(1, tint + '00');
    context.fillStyle = glow; context.fillRect(left, 125, 304, 322);
    context.drawImage(render(parts(type, 'idle', 0.35), loaded[type].texture, 304, 328,
      { center: [0, 4, 0], zoom: 27, yaw: -18, pitch: 8 }), left, 127);
    text(context, settings.title, left + 20, 470, 21, tint);
    const descriptions = {
      glass: ['分片晶壳 / 黄铜支架', '碎晶范围激发'], lightning: ['六枚电极 / 闪电核', '单体雷击 / 核心充能'],
      frost: ['六向分叉 / 冰晶棱面', '回流形成薄霜护盾'], ion: ['交叉双环 / 四枚磁极', '速度强化 / 核心充能'],
      dark: ['裂纹暗核 / 悬浮束缚片', '蓄力积累 / 原球发射']
    };
    descriptions[type].forEach((line, row) => text(context, line, left + 20, 510 + row * 29, 17));
    index++;
  }
  text(context, '离线模型预览，非游戏截图；发光由运行时 glowmask 渲染。', 38, 625, 15, '#799294');
  fs.writeFileSync(path.join(directory, 'orbs-preview.png'), canvas.toBuffer('image/png'));
}

function linkage() {
  const { canvas, context } = backdrop(1560, 810);
  text(context, '故障机器人 × 充能球  /  动作联动', 35, 53, 28, '#f0d7a1');
  const stages = [ ['orb_summon', 0.43, 'summon', '01  手部成球 → 回到环绕槽'],
    ['cast_dark', 0.62, 'focus', '02  指定球聚焦 → 蓄力激发'], ['beam', 7.2, 'beam', '03  五色球同步向胸口输能'] ];
  stages.forEach(([action, time, state, label], index) => {
    const left = index * 520;
    context.fillStyle = '#19272d'; context.fillRect(left + 15, 86, 490, 670);
    context.drawImage(render(bossParts(action, time), textures[index === 1 ? 'dark' : 'ion'], 490, 625,
      { yaw: -14, pitch: 4, zoom: 10.8 }), left + 15, 85);
    Object.keys(types).forEach((type, orbIndex) => {
      const angle = orbIndex * Math.PI * 2 / 5 - Math.PI / 2;
      const horizontal = left + 260 + Math.cos(angle) * 178;
      const vertical = 340 + Math.sin(angle) * 112;
      const selected = index !== 1 || type === 'dark';
      if (selected) {
        context.strokeStyle = '#' + types[type].tint.map(value => value.toString(16).padStart(2, '0')).join('') + 'aa';
        context.lineWidth = 1.7; context.beginPath(); context.moveTo(horizontal, vertical); context.lineTo(left + 260, index === 2 ? 338 : 380); context.stroke();
      }
      context.drawImage(render(parts(type, selected ? state : 'idle', 0.25), loaded[type].texture, 98, 108,
        { center: [0, 4, 0], zoom: 8.4, yaw: -18, pitch: 8 }), horizontal - 49, vertical - 54);
    });
    text(context, label, left + 35, 733, 21, '#dbdfd6');
  });
  text(context, '实际模型 / 动作的离线分镜。连线为机制示意；游戏中实时跟随胸口与双手骨骼。', 35, 790, 17, '#899da0');
  fs.writeFileSync(path.join(directory, 'linkage-preview.png'), canvas.toBuffer('image/png'));
}

sheet();
linkage();
if (process.argv.includes('--frames')) {
  const frameDirectory = path.join(directory, 'frames'); fs.mkdirSync(frameDirectory, { recursive: true });
  const stages = [['summon', 0.5], ['idle', 1.2], ['focus', 0.8], ['dash', 0.6], ['return', 0.8], ['beam', 1.2], ['drain', 0.3], ['burst', 0.3]];
  let frameNumber = 0;
  for (const [state, duration] of stages) for (let frame = 0; frame < Math.ceil(duration * 20); frame++) {
    const { canvas, context } = backdrop(1200, 440);
    text(context, '充能球骨骼动作 / ' + state, 30, 42, 25, '#f0d7a1');
    Object.entries(types).forEach(([type, settings], index) => {
      context.drawImage(render(parts(type, state, frame / 20), loaded[type].texture, 240, 305,
        { center: [0, 4, 0], zoom: 20, yaw: -20, pitch: 8 }), index * 240, 60);
      text(context, settings.title, index * 240 + 20, 410, 19);
    });
    fs.writeFileSync(path.join(frameDirectory, String(frameNumber++).padStart(4, '0') + '.png'), canvas.toBuffer('image/png'));
  }
}
console.log('Rendered five models and linkage storyboard.');
