'use strict';

const fs = require('node:fs');
const path = require('node:path');
const { createCanvas, GlobalFonts } = require(path.join(process.env.FT_NODE_MODULES || 'C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules', '@napi-rs/canvas'));
const { bossParts, weaponParts, render, textures, weaponTexture } = require('./preview.cjs');
GlobalFonts.registerFromPath('C:/Windows/Fonts/msyh.ttc', 'Yahei');
const directory = path.resolve(process.argv[2]);
const canvas = createCanvas(1600, 900), context = canvas.getContext('2d');
context.fillStyle = '#161e20'; context.fillRect(0, 0, 1600, 900);
for (const [index, [name, time, label]] of [['beam', 7.7, '光束蓄力'], ['beam', 8.1, '光束推出'], ['exhausted', 0.4, '落地衰弱'], ['death_overload', 1, '死亡收束']].entries()) {
  context.drawImage(render(bossParts(name, time), textures.glass, 400, 460, { yaw: -24, pitch: 5, zoom: 7.6 }), index * 400, 0);
  context.fillStyle = '#e9d3a0'; context.font = '21px Yahei'; context.fillText(label, index * 400 + 140, 477);
}
for (const [index, yaw] of [0, 90, 180, -30].entries()) {
  context.drawImage(render(weaponParts(), weaponTexture, 350, 340, { center: [7, 10, 7], yaw, pitch: 8, zoom: 14 }), 25 + index * 400, 530);
}
fs.writeFileSync(path.join(directory, 'extra-views.png'), canvas.toBuffer('image/png'));
