const fs = require('fs');
const path = require('path');
const { decodePNG } = require('../../png.cjs');
const dot = (first, second) => first.reduce((sum, value, index) => sum + value * second[index], 0);
const subtract = (first, second) => first.map((value, index) => value - second[index]);
const cross = (first, second) => [first[1] * second[2] - first[2] * second[1], first[2] * second[0] - first[0] * second[2], first[0] * second[1] - first[1] * second[0]];
const normalized = vector => vector.map(value => value / Math.hypot(...vector));

function transform(point, rotation) {
  if (!rotation) return point.slice();
  const local = subtract(point, rotation.origin);
  const angle = rotation.angle * Math.PI / 180;
  const cosine = Math.cos(angle);
  const sine = Math.sin(angle);
  const axis = { x: 0, y: 1, z: 2 }[rotation.axis];
  const first = (axis + 1) % 3;
  const second = (axis + 2) % 3;
  const result = local.slice();
  const rescale = rotation.rescale ? 1 / Math.cos(angle) : 1;
  result[first] = (local[first] * cosine - local[second] * sine) * rescale;
  result[second] = (local[first] * sine + local[second] * cosine) * rescale;
  return result.map((value, index) => value + rotation.origin[index]);
}

function faceVertices(element, face) {
  const [left, bottom, near] = element.from;
  const [right, top, far] = element.to;
  const vertices = {
    north: [[right, top, near], [right, bottom, near], [left, bottom, near], [left, top, near]],
    south: [[left, top, far], [left, bottom, far], [right, bottom, far], [right, top, far]],
    west: [[left, top, near], [left, bottom, near], [left, bottom, far], [left, top, far]],
    east: [[right, top, far], [right, bottom, far], [right, bottom, near], [right, top, near]],
    up: [[left, top, near], [left, top, far], [right, top, far], [right, top, near]],
    down: [[left, bottom, far], [left, bottom, near], [right, bottom, near], [right, bottom, far]]
  }[face];
  return vertices.map(point => transform(point, element.rotation));
}

function loadRuntime(root) {
  const resourceRoot = path.join(root, 'src/main/resources/assets');
  const loadModel = id => {
    const [namespace, location] = id.split(':');
    return JSON.parse(fs.readFileSync(path.join(resourceRoot, namespace, 'models', location + '.json'), 'utf8'));
  };
  const wrapper = loadModel('ftboss:item/golden_apple_brooch');
  const model = loadModel(wrapper.base.parent);
  const textures = {};
  for (const [key, reference] of Object.entries(model.textures)) {
    if (reference.startsWith('#')) continue;
    const [namespace, location] = reference.split(':');
    textures[key] = decodePNG(path.join(resourceRoot, namespace, 'textures', location + '.png'));
  }
  for (const [key, reference] of Object.entries(model.textures)) if (reference.startsWith('#')) textures[key] = textures[reference.slice(1)];
  return { wrapper, model, textures };
}

function render(model, textures, options = {}) {
  const width = options.width || 512;
  const height = options.height || 512;
  const pixels = new Uint8Array(width * height * 4);
  const depths = new Float64Array(width * height).fill(-Infinity);
  const owners = new Int32Array(width * height).fill(-1);
  const yaw = (options.yaw || 0) * Math.PI / 180;
  const pitch = (options.pitch || 0) * Math.PI / 180;
  const camera = [Math.sin(yaw) * Math.cos(pitch), Math.sin(pitch), -Math.cos(yaw) * Math.cos(pitch)];
  const right = [Math.cos(yaw), 0, Math.sin(yaw)];
  const up = [-Math.sin(yaw) * Math.sin(pitch), Math.cos(pitch), Math.cos(yaw) * Math.sin(pitch)];
  const center = options.center || [8, 9, 8];
  const allPoints = model.elements.flatMap(element => Object.keys(element.faces).flatMap(face => faceVertices(element, face)));
  const projected = allPoints.map(point => [dot(subtract(point, center), right), dot(subtract(point, center), up)]);
  const bounds = [Math.min(...projected.map(point => point[0])), Math.min(...projected.map(point => point[1])), Math.max(...projected.map(point => point[0])), Math.max(...projected.map(point => point[1]))];
  const pad = options.padding === undefined ? 10 : options.padding;
  const scale = options.fit ? Math.min((width - pad * 2) / (bounds[2] - bounds[0]), (height - pad * 2) / (bounds[3] - bounds[1])) : height / (options.span || 17.8);
  const offset = options.fit ? [(bounds[0] + bounds[2]) / 2, (bounds[1] + bounds[3]) / 2] : [0, 0];
  const project = point => {
    const relative = subtract(point, center);
    return [width / 2 + (dot(relative, right) - offset[0]) * scale, height / 2 - (dot(relative, up) - offset[1]) * scale, dot(relative, camera)];
  };
  const keyLight = normalized([-0.4, 0.75, -0.65]);
  const edge = (first, second, point) => (point[0] - first[0]) * (second[1] - first[1]) - (point[1] - first[1]) * (second[0] - first[0]);
  let visibleFaces = 0;
  model.elements.forEach((element, elementIndex) => Object.entries(element.faces).forEach(([face, surface]) => {
    const vertices = faceVertices(element, face);
    const normal = normalized(cross(subtract(vertices[1], vertices[0]), subtract(vertices[2], vertices[0])));
    if (dot(normal, camera) <= 0.000001) return;
    visibleFaces++;
    const texture = textures[surface.texture.slice(1)];
    if (!texture) throw new Error(`Unresolved ${surface.texture}`);
    const [left, top, rightUv, bottom] = surface.uv;
    const sourceUv = [[left, top], [left, bottom], [rightUv, bottom], [rightUv, top]];
    const shift = (surface.rotation || 0) / 90;
    const uv = sourceUv.map((value, index) => sourceUv[(index + shift) % 4]);
    const projectedVertices = vertices.map(project);
    const shade = options.unlit ? 1 : 0.73 + 0.27 * Math.max(0, dot(normal, keyLight));
    for (const triangle of [[0, 1, 2], [0, 2, 3]]) {
      const corners = triangle.map(index => projectedVertices[index]);
      const denominator = edge(corners[0], corners[1], corners[2]);
      if (Math.abs(denominator) < 0.000001) continue;
      const minX = Math.max(0, Math.floor(Math.min(...corners.map(point => point[0]))));
      const maxX = Math.min(width - 1, Math.ceil(Math.max(...corners.map(point => point[0]))));
      const minY = Math.max(0, Math.floor(Math.min(...corners.map(point => point[1]))));
      const maxY = Math.min(height - 1, Math.ceil(Math.max(...corners.map(point => point[1]))));
      for (let pixelY = minY; pixelY <= maxY; pixelY++) for (let pixelX = minX; pixelX <= maxX; pixelX++) {
        const sample = [pixelX + 0.5, pixelY + 0.5];
        const weights = [edge(corners[1], corners[2], sample) / denominator, edge(corners[2], corners[0], sample) / denominator, edge(corners[0], corners[1], sample) / denominator];
        if (weights.some(weight => weight < -0.0000001)) continue;
        const depth = weights.reduce((sum, weight, index) => sum + weight * corners[index][2], 0);
        const address = pixelY * width + pixelX;
        if (depth <= depths[address] + 0.00000001) continue;
        const sampleUv = [0, 1].map(axis => weights.reduce((sum, weight, index) => sum + weight * uv[triangle[index]][axis], 0));
        const sourceX = Math.min(texture.w - 1, Math.max(0, Math.floor(sampleUv[0] / 16 * texture.w)));
        const sourceY = Math.min(texture.h - 1, Math.max(0, Math.floor(sampleUv[1] / 16 * texture.h)));
        const source = (sourceY * texture.w + sourceX) * 4;
        if (texture.data[source + 3] < 128) continue;
        depths[address] = depth;
        owners[address] = elementIndex;
        for (let channel = 0; channel < 3; channel++) pixels[address * 4 + channel] = Math.round(texture.data[source + channel] * shade);
        pixels[address * 4 + 3] = 255;
      }
    }
  }));
  return { w: width, h: height, data: pixels, owners, visibleFaces, projectedBounds: bounds };
}

module.exports = { dot, subtract, cross, normalized, transform, faceVertices, loadRuntime, render };
