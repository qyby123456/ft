'use strict';

const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');
const { render } = require('./render_cleaver_reference.cjs');
const { encodePNG } = require('./png.cjs');
const OUT = path.resolve(__dirname,'../../art/black_cleaver_reference');
const report = JSON.parse(fs.readFileSync(process.argv[2] || path.join(OUT,'hand-validation.json'),'utf8'));
const filename = process.argv[3] || path.join(OUT,'native-hand-preview.png');
const geometry = JSON.parse(fs.readFileSync(path.join(OUT,'black_cleaver.geo.json'),'utf8'))['minecraft:geometry'][0];
const bones = new Map(geometry.bones.map(bone => [bone.name,bone]));
const views = [
  { hand: 'right',pose: 'closed',yaw: 0 },
  { hand: 'right',pose: 'closed',yaw: 90 },
  { hand: 'right',pose: 'blood_enter:1.4',yaw: 48 },
  { hand: 'left',pose: 'closed',yaw: -48 }
];
const panelWidth = 520;
const height = 800;
const width = panelWidth*views.length;
const output = Buffer.alloc(width*height*4);
for (const [index,view] of views.entries()) {
  const entry = report.find(sample => sample.hand === view.hand && sample.model === 'classic' && sample.idle_bob_tick === -1);
  assert.ok(entry?.scene,'Native player scene is required; run the Gecko check first');
  const scene = entry.scene.body.map(cube => ({ corners: cube.corners,
    color: cube.part === '/head' ? [207,175,139] : cube.part.includes('leg') ? [76,84,107] : [64,139,145] }));
  scene.push(...entry.scene[view.pose].map(cube => ({ corners: cube.corners,uv: bones.get(cube.bone).cubes[cube.index].uv })));
  const panel = render({ scene,width: panelWidth,height,yaw: view.yaw,pitch: 0,scale: 17,
    centerY: -4,lightBackground: true,effects: {} });
  for (let row = 0; row < height; row++) panel.buffer.copy(output,(row*width+index*panelWidth)*4,row*panelWidth*4,(row+1)*panelWidth*4);
}
fs.writeFileSync(filename,encodePNG(width,height,output));
console.log(filename);
