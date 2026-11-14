const fs = require('fs');
const path = require('path');
const { decodePNG, encodePNG } = require('./png.cjs');

const work = path.resolve(process.argv[2]);
const output = path.join(work, 'previews');
const manifest = JSON.parse(fs.readFileSync(path.join(output, 'manifest.json'), 'utf8'));
const poses = JSON.parse(fs.readFileSync(path.join(work, 'runtime-poses.json'), 'utf8'));
const size = 192;
const textures = new Map();
const views = ['front', 'back', 'left', 'right'];
const records = [];
const thumbnails = [];
const surfaceThumbnails = [];

function texture(name) {
  if (!name) return null;
  if (!textures.has(name)) textures.set(name, decodePNG(path.join(output, name)));
  return textures.get(name);
}

function project(point, view) {
  if (view === 4) return [-point[0], point[2], point[1]];
  if (view === 5) return [(point[0] - point[2]) * 0.7071067812,
    -point[1] * 0.8164965809 + (point[0] + point[2]) * 0.4082482905, (point[0] + point[2]) * 0.5773502692 + point[1] * 0.5773502692];
  const horizontal = view === 0 ? -point[0] : view === 1 ? point[0] : view === 2 ? point[2] : -point[2];
  const depth = view === 0 ? -point[2] : view === 1 ? point[2] : view === 2 ? -point[0] : point[0];
  return [horizontal, -point[1], depth];
}

function pose(point, bone, matrices, pivot = [0, 0, 0]) {
  if (!matrices || !matrices[bone]) return point;
  const basis = matrices[bone];
  return [0, 1, 2].map(axis => pivot[axis] + basis[0][axis] + point.reduce((sum, coordinate, index) =>
    sum + (coordinate - pivot[index]) * (basis[index + 1][axis] - basis[0][axis]), 0));
}

function prepare(faces, matrices, entry, state) {
  const pivoted = entry.poseKey.startsWith('lemon:') || entry.poseKey.startsWith('dandan:');
  return faces.map(face => ({ ...face, p: face.p.map(point => {
    if (entry.poseKey.startsWith('optimized:')) {
      const age = Number(state);
      const bone = face.bone;
      const moved = point.slice();
      if (bone >= 8) moved[bone === 8 ? 2 : 1] += bone === 8 ? 0.65 * Math.sin(age * 0.16) : 0.45 * Math.cos(age * 0.13);
      else if (bone) {
        const phase = age % 5 / 5;
        const wing = ((phase < 0.5 ? phase * 2 : 2 - phase * 2) - 0.5) * 70 * 0.35 * Math.PI / 180;
        const angle = ({ 1: -wing, 2: wing, 3: age * 0.045, 4: age * 0.065, 5: age * 0.018, 7: -age * 0.038 })[bone] || 0;
        const axis = bone <= 2 ? 2 : 1;
        const horizontal = point[0] - face.pivot[0];
        const vertical = point[axis] - face.pivot[axis];
        moved[0] = face.pivot[0] + horizontal * Math.cos(angle) + (bone <= 2 ? 1 : -1) * vertical * Math.sin(angle);
        moved[axis] = face.pivot[axis] + (bone <= 2 ? -1 : 1) * horizontal * Math.sin(angle) + vertical * Math.cos(angle);
      }
      return moved;
    }
    return pose(point, face.poseKey || face.bone, matrices, pivoted ? face.pivot : [0, 0, 0]);
  }) }));
}

function drawTriangle(pixels, corners, atlas, color, opacity) {
  const [first, second, third] = corners;
  const area = (second[1] - third[1]) * (first[0] - third[0]) + (third[0] - second[0]) * (first[1] - third[1]);
  if (Math.abs(area) < 1e-12) return;
  const minimumX = Math.max(0, Math.floor(Math.min(...corners.map(point => point[0]))));
  const maximumX = Math.min(size - 1, Math.ceil(Math.max(...corners.map(point => point[0]))));
  const minimumY = Math.max(0, Math.floor(Math.min(...corners.map(point => point[1]))));
  const maximumY = Math.min(size - 1, Math.ceil(Math.max(...corners.map(point => point[1]))));
  for (let down = minimumY; down <= maximumY; down++) for (let across = minimumX; across <= maximumX; across++) {
    const firstWeight = ((second[1] - third[1]) * (across + 0.5 - third[0]) + (third[0] - second[0]) * (down + 0.5 - third[1])) / area;
    const secondWeight = ((third[1] - first[1]) * (across + 0.5 - third[0]) + (first[0] - third[0]) * (down + 0.5 - third[1])) / area;
    const thirdWeight = 1 - firstWeight - secondWeight;
    if (firstWeight < -1e-10 || secondWeight < -1e-10 || thirdWeight < -1e-10) continue;
    let alpha = opacity;
    let sampled = [255, 255, 255];
    if (atlas) {
      const textureX = Math.max(0, Math.min(atlas.w - 1, Math.floor((first[3] * firstWeight + second[3] * secondWeight + third[3] * thirdWeight) * atlas.w)));
      const textureY = Math.max(0, Math.min(atlas.h - 1, Math.floor((first[4] * firstWeight + second[4] * secondWeight + third[4] * thirdWeight) * atlas.h)));
      const offset = (textureY * atlas.w + textureX) * 4;
      sampled = atlas.data.subarray(offset, offset + 3);
      alpha *= atlas.data[offset + 3] / 255;
    }
    const offset = (down * size + across) * 4;
    for (let channel = 0; channel < 3; channel++) pixels[offset + channel] = Math.round(
      pixels[offset + channel] * (1 - alpha) + sampled[channel] * color[channel] / 255 * alpha);
  }
}

function render(faces, view, bounds) {
  const image = new Uint8Array(size * size * 4);
  for (let index = 0; index < size * size; index++) image.set([12, 19, 29, 255], index * 4);
  const scale = Math.min((size - 20) / Math.max(0.1, bounds[3] - bounds[0]), (size - 20) / Math.max(0.1, bounds[4] - bounds[1]));
  const projected = faces.map(face => ({ face, p: face.p.map(point => project(point, view)) }));
  projected.sort((first, second) => first.p.reduce((sum, point) => sum + point[2], 0) - second.p.reduce((sum, point) => sum + point[2], 0));
  for (const item of projected) {
    const corners = item.p.map((point, index) => [size / 2 + (point[0] - (bounds[0] + bounds[3]) / 2) * scale,
      size / 2 + (point[1] - (bounds[1] + bounds[4]) / 2) * scale, point[2], ...(item.face.uv ? item.face.uv[index] : [0, 0])]);
    const color = [item.face.color >> 16 & 255, item.face.color >> 8 & 255, item.face.color & 255];
    const atlas = texture(item.face.texture);
    drawTriangle(image, [corners[0], corners[1], corners[2]], atlas, color, item.face.alpha);
    drawTriangle(image, [corners[0], corners[2], corners[3]], atlas, color, item.face.alpha);
  }
  return image;
}

function blit(target, width, image, left, top) {
  for (let row = 0; row < size; row++) target.set(image.subarray(row * size * 4, (row + 1) * size * 4), ((top + row) * width + left) * 4);
}

for (const entry of manifest) {
  const model = JSON.parse(fs.readFileSync(path.join(output, entry.file), 'utf8'));
  const states = entry.poseKey.startsWith('optimized:') ? [['0', null], ['12', null], ['24', null]] : Object.entries(poses[entry.poseKey]);
  for (const [state, matrices] of states) {
    const before = prepare(model.before, matrices, entry, state);
    const after = prepare(model.after, matrices, entry, state);
    const sheet = new Uint8Array(size * 4 * size * 2 * 4);
    const thumbnail = new Uint8Array(size * 2 * size * 2 * 4);
    const differences = [];
    for (let view = 0; view < 4; view++) {
      const points = before.flatMap(face => face.p.map(point => project(point, view)));
      const bounds = [0, 1, 2].map(axis => Math.min(...points.map(point => point[axis]))).concat(
        [0, 1, 2].map(axis => Math.max(...points.map(point => point[axis]))));
      const baseline = render(before, view, bounds);
      const optimized = render(after, view, bounds);
      blit(sheet, size * 4, baseline, view * size, 0);
      blit(sheet, size * 4, optimized, view * size, size);
      blit(thumbnail, size * 2, optimized, view % 2 * size, Math.floor(view / 2) * size);
      let absoluteError = 0;
      for (let index = 0; index < baseline.length; index++) if (index % 4 !== 3) absoluteError += Math.abs(baseline[index] - optimized[index]);
      differences.push(absoluteError / (size * size * 3));
    }
    const image = `${entry.id}_${state.replaceAll('.', '_')}.png`;
    fs.writeFileSync(path.join(output, image), encodePNG(size * 4, size * 2, sheet));
    records.push({ ...entry, state, image, meanAbsoluteError: differences });
    thumbnails.push({ image: thumbnail, label: `${entry.id} / ${state}` });
    if (entry.poseKey.startsWith('domain:') && state === '64.0') {
      const surface = new Uint8Array(size * 2 * size * 2 * 4);
      for (let view = 4; view < 6; view++) {
        const points = before.flatMap(face => face.p.map(point => project(point, view)));
        const bounds = [0, 1, 2].map(axis => Math.min(...points.map(point => point[axis]))).concat(
          [0, 1, 2].map(axis => Math.max(...points.map(point => point[axis]))));
        blit(surface, size * 2, render(before, view, bounds), (view - 4) * size, 0);
        blit(surface, size * 2, render(after, view, bounds), (view - 4) * size, size);
      }
      const file = `${entry.id}_surface.png`;
      fs.writeFileSync(path.join(output, file), encodePNG(size * 2, size * 2, surface));
      surfaceThumbnails.push({ image: surface, label: entry.id, file });
    }
  }
}

const pages = [];
for (let offset = 0; offset < thumbnails.length; offset += 16) {
  const page = new Uint8Array(size * 8 * size * 8 * 4);
  const selected = thumbnails.slice(offset, offset + 16);
  selected.forEach((item, index) => {
    for (let row = 0; row < size * 2; row++) page.set(item.image.subarray(row * size * 2 * 4, (row + 1) * size * 2 * 4),
      ((Math.floor(index / 4) * size * 2 + row) * size * 8 + index % 4 * size * 2) * 4);
  });
  const file = `contact-${pages.length + 1}.png`;
  fs.writeFileSync(path.join(output, file), encodePNG(size * 8, size * 8, page));
  pages.push({ file, labels: selected.map(item => item.label) });
}
const surfaceSheet = new Uint8Array(size * 8 * size * 4 * 4);
surfaceThumbnails.forEach((item, index) => {
  for (let row = 0; row < size * 2; row++) surfaceSheet.set(item.image.subarray(row * size * 2 * 4, (row + 1) * size * 2 * 4),
    ((Math.floor(index / 4) * size * 2 + row) * size * 8 + index % 4 * size * 2) * 4);
});
fs.writeFileSync(path.join(output, 'domain-surface-contact.png'), encodePNG(size * 8, size * 4, surfaceSheet));
fs.writeFileSync(path.join(output, 'visual-checks.json'), JSON.stringify({ views, pages, records,
  supplementalSurfaces: surfaceThumbnails.map(({ label, file }) => ({ label, file })) }, null, 2));
fs.writeFileSync(path.join(output, 'index.html'), `<!doctype html><meta charset="utf-8"><title>技能模型减面核查</title>
<style>body{background:#0c131d;color:#ddd;font:15px system-ui;margin:28px}img{max-width:100%}article{margin-bottom:24px}a{color:#7cd6ff}</style>
<h1>技能模型减面四视图对比</h1><p>每张图上排优化前、下排优化后；依次正面、背面、左侧、右侧。使用实际资源几何与贴图；麻雀各关键帧使用已安装 JAR 的骨骼变换。离线预览不模拟游戏光影、泛光与地形。</p>
${records.map(record => `<article><h3>${record.id} / ${record.state}: ${record.before} → ${record.after} faces</h3><a href="${record.image}"><img loading="lazy" src="${record.image}"></a></article>`).join('\n')}`);
console.log(`Rendered ${manifest.length} changed meshes, ${records.length} animation states, ${records.length * 8} before/after views; ${pages.length} contact sheets`);
