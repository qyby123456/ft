const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const assert = require('assert/strict');
const { execFileSync } = require('child_process');
const { decodePNG } = require('../../png.cjs');
const { writeSafe } = require('../../lib_itemtex.cjs');
const { transform, faceVertices, dot, cross, normalized, subtract, loadRuntime, render } = require('./raster.cjs');
const root = path.resolve(__dirname, '../../../..');
const resource = 'src/main/resources/assets/ftboss/';
const art = path.join(root, 'art/boss_relics/refine/apple');
const files = [resource + 'models/item/golden_apple_brooch.json', resource + 'models/item/boss_relics/golden_apple_brooch.json', resource + 'models/item/boss_relics/golden_apple_brooch_gui.json', resource + 'textures/item/golden_apple_brooch.png', resource + 'textures/item/boss_relics/golden_apple_brooch.png', 'art/boss_relics/blockbench/golden_apple_brooch.bbmodel'];
const digest = relative => crypto.createHash('sha256').update(fs.readFileSync(path.join(root, relative))).digest('hex');
const reproducibility = { performed: process.argv.includes('--reproduce'), identical: null, assets: files.length };
if (reproducibility.performed) {
  const before = files.map(digest);
  execFileSync(process.execPath, [path.join(__dirname, 'build.cjs')], { cwd: root, stdio: 'ignore' });
  execFileSync(process.execPath, [path.join(__dirname, 'render.cjs'), '--write-icon'], { cwd: root, stdio: 'ignore' });
  reproducibility.identical = files.every((file, index) => digest(file) === before[index]);
}
const runtime = loadRuntime(root);
const { model, wrapper } = runtime;
const atlas = runtime.textures.atlas;
const gui = JSON.parse(fs.readFileSync(path.join(root, files[2]), 'utf8'));
const bb = JSON.parse(fs.readFileSync(path.join(root, files[5]), 'utf8'));
const failures = [];
const check = (condition, label) => { if (!condition) failures.push(label); };
const near = (first, second, tolerance = 0.0006) => Math.abs(first - second) < tolerance;
check(!reproducibility.performed || reproducibility.identical, 'deterministic rebuild of all six assets');
check(wrapper.loader === 'forge:separate_transforms', 'Forge separate transforms loader');
check(wrapper.base.parent === 'ftboss:item/boss_relics/golden_apple_brooch', '3D base reference');
check(wrapper.perspectives.gui.parent === 'ftboss:item/boss_relics/golden_apple_brooch_gui', 'GUI override reference');
check(gui.parent === 'minecraft:item/generated' && gui.textures.layer0 === 'ftboss:item/golden_apple_brooch', 'independent GUI sprite');
check(!model.parent, '3D model has no generated or handheld parent');
check(Object.keys(model.display).length === 8, 'eight display slots');
check(bb.elements.length === model.elements.length, 'BB geometry count');
check(Buffer.from(bb.textures[0].source.split(',')[1], 'base64').equals(fs.readFileSync(path.join(root, files[4]))), 'BB embedded texture matches runtime PNG');
check(JSON.stringify(bb.display) === JSON.stringify(model.display), 'BB display matches runtime');

let faceCount = 0;
let opacitySamples = 0;
let maxDensityError = 0;
let maxAspectError = 0;
const rectangles = [];
const faceData = [];
model.elements.forEach((element, index) => {
  const editable = bb.elements[index];
  check(element.name === editable.name, `BB name ${index}`);
  check(JSON.stringify(element.from) === JSON.stringify(editable.from) && JSON.stringify(element.to) === JSON.stringify(editable.to), `BB coordinates ${element.name}`);
  const dimensions = subtract(element.to, element.from);
  check(dimensions.every(value => value > 0), `positive volume ${element.name}`);
  check([...element.from, ...element.to].every(value => value >= -16 && value <= 32), `Java coordinates ${element.name}`);
  if (element.rotation) {
    check(['x', 'y', 'z'].includes(element.rotation.axis) && [-45, -22.5, 0, 22.5, 45].includes(element.rotation.angle), `legal rotation ${element.name}`);
    check(editable.rotation.filter(value => value !== 0).length === 1, `BB one-axis rotation ${element.name}`);
    check(near(editable.rotation[{ x: 0, y: 1, z: 2 }[element.rotation.axis]], element.rotation.angle), `BB angle ${element.name}`);
    check(JSON.stringify(editable.origin) === JSON.stringify(element.rotation.origin), `BB pivot ${element.name}`);
  }
  check(Object.keys(element.faces).length === 6, `six opaque faces ${element.name}`);
  Object.entries(element.faces).forEach(([face, surface]) => {
    faceCount++;
    check(surface.texture === '#atlas', `atlas reference ${element.name}.${face}`);
    check(surface.uv.every(value => Number.isFinite(value) && value >= 0 && value <= 16), `UV bounds ${element.name}.${face}`);
    const uv = surface.uv.map(value => value / 16 * atlas.w);
    const extent = [uv[2] - uv[0], uv[3] - uv[1]];
    const expected = ['east', 'west'].includes(face) ? [dimensions[2], dimensions[1]] : ['up', 'down'].includes(face) ? [dimensions[0], dimensions[2]] : [dimensions[0], dimensions[1]];
    const densityError = Math.max(...extent.map((value, axis) => Math.abs(value / expected[axis] - 1.75)));
    const aspectError = Math.abs(extent[0] / extent[1] / (expected[0] / expected[1]) - 1);
    maxDensityError = Math.max(maxDensityError, densityError);
    maxAspectError = Math.max(maxAspectError, aspectError);
    check(densityError < 0.003 && aspectError < 0.002, `UV physical aspect ${element.name}.${face}`);
    check(editable.faces[face].texture === 0 && editable.faces[face].uv.every((value, axis) => near(value, uv[axis])), `BB UV ${element.name}.${face}`);
    for (let pixelY = Math.floor(uv[1]); pixelY <= Math.ceil(uv[3]); pixelY++) for (let pixelX = Math.floor(uv[0]); pixelX <= Math.ceil(uv[2]); pixelX++) {
      opacitySamples++;
      check(atlas.data[(Math.min(atlas.h - 1, pixelY) * atlas.w + Math.min(atlas.w - 1, pixelX)) * 4 + 3] === 255, `UV opaque ${element.name}.${face}`);
    }
    rectangles.push({ name: `${element.name}.${face}`, uv });
    const vertices = faceVertices(element, face);
    const normal = normalized(cross(subtract(vertices[1], vertices[0]), subtract(vertices[2], vertices[0])));
    faceData.push({ name: `${element.name}.${face}`, elementIndex: index, vertices, normal });
  });
});
for (let firstIndex = 0; firstIndex < rectangles.length; firstIndex++) for (let secondIndex = firstIndex + 1; secondIndex < rectangles.length; secondIndex++) {
  const first = rectangles[firstIndex];
  const second = rectangles[secondIndex];
  const overlapX = Math.min(first.uv[2], second.uv[2]) - Math.max(first.uv[0], second.uv[0]);
  const overlapY = Math.min(first.uv[3], second.uv[3]) - Math.max(first.uv[1], second.uv[1]);
  check(!(overlapX > 0.0001 && overlapY > 0.0001), `unique islands ${first.name} / ${second.name}`);
}

const corners = model.elements.map(element => [0, 1, 2, 3, 4, 5, 6, 7].map(mask => transform([0, 1, 2].map(axis => mask & (1 << axis) ? element.to[axis] : element.from[axis]), element.rotation)));
const axes = model.elements.map(element => {
  const origin = element.rotation ? element.rotation.origin : [0, 0, 0];
  const transformedOrigin = transform(origin, element.rotation);
  return [0, 1, 2].map(axis => normalized(subtract(transform(origin.map((value, index) => value + (index === axis ? 1 : 0)), element.rotation), transformedOrigin)));
});
function connected(firstIndex, secondIndex) {
  const candidates = [...axes[firstIndex], ...axes[secondIndex], ...axes[firstIndex].flatMap(first => axes[secondIndex].map(second => cross(first, second)))].filter(axis => Math.hypot(...axis) > 0.00001);
  for (const candidate of candidates) {
    const axis = normalized(candidate);
    const firstProjection = corners[firstIndex].map(point => dot(point, axis));
    const secondProjection = corners[secondIndex].map(point => dot(point, axis));
    if (Math.max(...firstProjection) < Math.min(...secondProjection) - 0.00001 || Math.max(...secondProjection) < Math.min(...firstProjection) - 0.00001) return false;
  }
  return true;
}
const adjacency = model.elements.map(() => []);
for (let firstIndex = 0; firstIndex < model.elements.length; firstIndex++) for (let secondIndex = firstIndex + 1; secondIndex < model.elements.length; secondIndex++) if (connected(firstIndex, secondIndex)) {
  adjacency[firstIndex].push(secondIndex);
  adjacency[secondIndex].push(firstIndex);
}
const reached = new Set([0]);
const queue = [0];
while (queue.length) for (const neighbor of adjacency[queue.shift()]) if (!reached.has(neighbor)) {
  reached.add(neighbor);
  queue.push(neighbor);
}
check(reached.size === model.elements.length, `connected OBB component: floating ${model.elements.filter((element, index) => !reached.has(index)).map(element => element.name).join(', ')}`);

const signedArea = polygon => polygon.reduce((sum, point, index) => { const next = polygon[(index + 1) % polygon.length]; return sum + point[0] * next[1] - point[1] * next[0]; }, 0) / 2;
const areaIntersection = (first, second) => {
  let output = first;
  const winding = Math.sign(signedArea(second));
  for (let edgeIndex = 0; edgeIndex < second.length && output.length; edgeIndex++) {
    const start = second[edgeIndex];
    const end = second[(edgeIndex + 1) % second.length];
    const distance = point => winding * ((end[0] - start[0]) * (point[1] - start[1]) - (end[1] - start[1]) * (point[0] - start[0]));
    const input = output;
    output = [];
    for (let index = 0; index < input.length; index++) {
      const current = input[index];
      const previous = input[(index + input.length - 1) % input.length];
      const currentDistance = distance(current);
      const previousDistance = distance(previous);
      if ((currentDistance >= -0.000001) !== (previousDistance >= -0.000001)) {
        const ratio = previousDistance / (previousDistance - currentDistance);
        output.push(previous.map((value, axis) => value + (current[axis] - value) * ratio));
      }
      if (currentDistance >= -0.000001) output.push(current);
    }
  }
  return Math.abs(signedArea(output));
};
const coplanar = [];
for (let firstIndex = 0; firstIndex < faceData.length; firstIndex++) for (let secondIndex = firstIndex + 1; secondIndex < faceData.length; secondIndex++) {
  const first = faceData[firstIndex];
  const second = faceData[secondIndex];
  if (first.elementIndex === second.elementIndex || dot(first.normal, second.normal) < 0.999999 || Math.abs(dot(first.normal, subtract(second.vertices[0], first.vertices[0]))) > 0.00001) continue;
  const droppedAxis = first.normal.map(Math.abs).indexOf(Math.max(...first.normal.map(Math.abs)));
  const project = vertices => vertices.map(point => point.filter((value, axis) => axis !== droppedAxis));
  const area = areaIntersection(project(first.vertices), project(second.vertices));
  if (area > 0.0001) coplanar.push({ first: first.name, second: second.name, projectedOverlapArea: area });
}
check(coplanar.length === 0, `coplanar overlap ${JSON.stringify(coplanar)}`);

assert.deepEqual(transform([1, 0, 0], { axis: 'z', angle: 45, origin: [0, 0, 0], rescale: true }).map(value => Math.round(value * 1000) / 1000), [1, 1, 0]);
assert.deepEqual(transform([0, 1, 0], { axis: 'x', angle: 45, origin: [0, 0, 0], rescale: true }).map(value => Math.round(value * 1000) / 1000), [0, 1, 1]);
const icon = decodePNG(path.join(root, files[3]));
check(icon.w === 32 && icon.h === 32, 'native 32x32 GUI icon');
check(icon.data.some((value, index) => index % 4 === 3 && value === 255), 'nonempty icon');
const viewResults = {};
for (const [name, yaw] of [['front', 0], ['back', 180], ['left', -90], ['right', 90]]) {
  const view = render(model, runtime.textures, { width: 500, height: 418, yaw, span: 13.5 });
  const opaqueBorder = [];
  for (let pixelY = 0; pixelY < view.h; pixelY++) for (let pixelX = 0; pixelX < view.w; pixelX++) if ((pixelX === 0 || pixelY === 0 || pixelX === view.w - 1 || pixelY === view.h - 1) && view.data[(pixelY * view.w + pixelX) * 4 + 3]) opaqueBorder.push([pixelX, pixelY]);
  check(opaqueBorder.length === 0, `unclipped ${name}`);
  viewResults[name] = { visibleFaces: view.visibleFaces, opaqueBorderPixels: opaqueBorder.length };
}
for (const relative of files.filter(file => /json$|bbmodel$/.test(file))) {
  const contents = fs.readFileSync(path.join(root, relative));
  check(!(contents[0] === 239 && contents[1] === 187 && contents[2] === 191), `no UTF-8 BOM ${relative}`);
  check(!contents.includes(13) && contents.at(-1) === 10, `LF ending ${relative}`);
}
const hashes = Object.fromEntries(files.map(relative => [relative, digest(relative)]));
const report = { passed: failures.length === 0, elements: model.elements.length, rotatedElements: model.elements.filter(element => element.rotation).length, faceCount, atlas: [atlas.w, atlas.h], pixelDensity: 1.75, maxDensityError, maxAspectError, uniqueUvIslands: rectangles.length, opacitySamples, connectedElements: reached.size, coplanar, views: viewResults, rendererFixtures: 'z45 and x45 rescale PASS', reproducibility, hashes, failures };
writeSafe(path.join(art, 'verification.json'), JSON.stringify(report, null, 2) + '\n');
console.log(JSON.stringify(report, null, 2));
process.exitCode = failures.length ? 1 : 0;
