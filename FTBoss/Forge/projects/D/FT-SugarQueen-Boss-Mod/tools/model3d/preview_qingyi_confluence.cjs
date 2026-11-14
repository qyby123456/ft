const fs = require('fs');
const path = require('path');
const { execFileSync } = require('child_process');
const { Canvas, decodePNG } = require('./png.cjs');
const root = path.resolve(__dirname, '../..');
const assets = path.join(root, 'src/main/resources/assets/ftboss');
const output = path.resolve(process.argv[2]);
const id = 'qingyi_confluence_trident';
const geo = path.join(assets, `geo/${id}.geo.json`);
const animations = path.join(assets, `animations/${id}.animation.json`);
const texture = path.join(assets, `textures/item/${id}.png`);
fs.mkdirSync(output, { recursive: true });
const panel = 380;
const colors = { idle: [88, 193, 208], attack: [213, 85, 119], charge: [120, 115, 221], release: [240, 202, 111] };
const environment = { ...process.env, FT_PANEL: String(panel), FT_SCALE: '4.1', FT_OY: '280', FT_VIEWS: 'front,right,back,iso' };
const sequence = [['idle', 1.2], ['attack', 0.5], ['idle', 0.5], ['charge', 1.6], ['release', 0.8], ['idle', 0.6]];
const frames = [];
for (const [name, duration] of sequence) {
  for (let frame = 0; frame < Math.round(duration * 10); frame++) frames.push({ name, time: frame / 10 });
}
function run(script, args) {
  execFileSync(process.execPath, [script, ...args], { env: environment, stdio: 'pipe' });
}
function crop(source, canvas, fromX, fromY, toX) {
  for (let pixelY = 0; pixelY < panel; pixelY++) {
    for (let pixelX = 0; pixelX < panel; pixelX++) {
      const position = ((fromY + pixelY) * source.w + fromX + pixelX) * 4;
      canvas.set(toX + pixelX, pixelY, ...source.data.slice(position, position + 3), 1);
    }
  }
}
frames.forEach(({ name, time }, frame) => {
  const pose = path.join(output, 'preview_pose.geo.json');
  const rendered = path.join(output, 'preview_views.png');
  run(path.join(root, 'tools/_archive/model3d/pose_bake.cjs'), [geo, animations, `animation.qingyi_confluence.${name}`, String(time), pose]);
  run(path.join(__dirname, 'render_tex.cjs'), [pose, texture, rendered, '0', '0']);
  const source = decodePNG(rendered);
  const result = new Canvas(panel * 2, panel + 12);
  crop(source, result, 0, 0, 0);
  crop(source, result, panel + 8, panel + 8, panel);
  result.rect(0, panel, panel * 2, 12, ...colors[name], 1);
  result.save(path.join(output, `frame_${String(frame).padStart(3, '0')}.png`));
});
fs.writeFileSync(path.join(output, 'timeline.json'), JSON.stringify({ fps: 10, sequence, frames }, null, 2) + '\n');
console.log(`Rendered ${frames.length} frames: idle / attack / charge / release, using actual animation durations`);
