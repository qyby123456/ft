const fs = require('node:fs');
const path = require('node:path');
const {Canvas, decodePNG} = require('./png.cjs');
const {loadGeo, listCubes, cubeCorners, transformByBoneChain, rotateXYZ} = require('./cube_space.cjs');

const root = path.resolve(__dirname, '../..');
const assets = path.join(root, 'src/main/resources/assets/ftboss');
const report = JSON.parse(fs.readFileSync(process.argv[2], 'utf8'));
const output = path.resolve(process.argv[3]);
fs.mkdirSync(output, {recursive: true});
const model = loadGeo(path.join(assets, 'geo/compost_superman.geo.json'), {geckolib: true});
const weapon = JSON.parse(fs.readFileSync(path.join(assets, 'models/item/compost_crown.json'), 'utf8'));
const animations = JSON.parse(fs.readFileSync(path.join(assets, 'animations/compost_superman.animation.json'), 'utf8')).animations;
const weaponTexture = decodePNG(path.join(assets, 'textures/item/compost_crown.png'));
const indices = {north:[6,2,0,4],south:[3,7,5,1],east:[7,6,4,5],west:[2,3,1,0],up:[2,6,7,3],down:[1,5,4,0]};
const dot = (first, second) => first.reduce((sum, value, axis) => sum + value * second[axis], 0);
const subtract = (first, second) => first.map((value, axis) => value - second[axis]);
const center = points => [0,1,2].map(axis => points.reduce((sum, point) => sum + point[axis], 0) / points.length);
const normalize = vector => vector.map(value => value / Math.hypot(...vector));
const hand = model.byName.get('hand_r');

function poseAt(name, time) {
  return Object.fromEntries(Object.entries(animations['animation.compost_superman.' + name].bones).map(([bone, channels]) =>
    [bone, Object.fromEntries(Object.entries(channels).map(([channel, frames]) => {
      const keys = Object.entries(frames).map(([stamp, value]) => [Number(stamp), value]).sort((first, second) => first[0] - second[0]);
      let value = keys.at(-1)[1];
      for (let index = 0; index < keys.length - 1; index++) {
        if (time <= keys[index + 1][0]) {
          const amount = Math.max(0, (time - keys[index][0]) / (keys[index + 1][0] - keys[index][0]));
          value = keys[index][1].map((coordinate, axis) => coordinate + amount * (keys[index + 1][1][axis] - coordinate));
          break;
        }
      }
      return [channel, value];
    }))]));
}

function heldPoint(point, matrix, pose) {
  const local = [0,1,2].map(row => 16 * matrix[12 + row] + point.reduce((sum, value, axis) => sum + matrix[axis * 4 + row] * value, 0));
  const world = [hand.pivot[0] - local[0], hand.pivot[1] + local[1], hand.pivot[2] + local[2]];
  return transformByBoneChain(model, 'hand_r', world, pose);
}

function facesFor(pose, matrix, texture) {
  const faces = [];
  const add = (points, mapping, image, multiplier) => {
    const middle = center(points);
    for (const [name, corners] of Object.entries(indices)) {
      if (!mapping[name]) continue;
      const vertices = corners.map(index => points[index]);
      const rectangle = mapping[name].uv;
      const coordinates = multiplier === 1
        ? [rectangle[0], rectangle[1], rectangle[0] + mapping[name].uv_size[0], rectangle[1] + mapping[name].uv_size[1]]
        : rectangle.map(value => value * multiplier);
      let uv = [[coordinates[0], coordinates[1]], [coordinates[2], coordinates[1]],
        [coordinates[2], coordinates[3]], [coordinates[0], coordinates[3]]];
      for (let step = 0; step < (mapping[name].rotation || 0) / 90; step++) uv.unshift(uv.pop());
      faces.push({vertices, uv, image, normal: normalize(subtract(center(vertices), middle))});
    }
  };
  for (const cube of listCubes(model)) add(cubeCorners(model, cube, pose), cube.cube.uv, texture, 1);
  for (const element of weapon.elements) {
    const points = [];
    for (const coordX of [element.from[0], element.to[0]]) {
      for (const coordY of [element.from[1], element.to[1]]) {
        for (const coordZ of [element.from[2], element.to[2]]) {
          let point = [coordX, coordY, coordZ];
          if (element.rotation) {
            const rotation = [0,0,0];
            rotation['xyz'.indexOf(element.rotation.axis)] = element.rotation.angle;
            point = rotateXYZ(point, rotation, element.rotation.origin);
          }
          points.push(heldPoint(point, matrix, pose));
        }
      }
    }
    add(points, element.faces, weaponTexture, weaponTexture.w / 16);
  }
  return faces;
}

function render(name, time, phase) {
  const width = 520, height = 680;
  const canvas = new Canvas(width * 2, height);
  canvas.rect(0, 0, width * 2, height, 25, 31, 40, 1);
  const depth = new Float32Array(width * 2 * height).fill(-Infinity);
  const pose = poseAt(name, time);
  const texture = decodePNG(path.join(assets, 'textures/entity/compost_superman' + (phase ? '_p2' : '') + '.png'));
  const panels = [report.before, report.after].map(matrix => facesFor(pose, matrix, texture));
  const camera = normalize([0.64, 0.16, -1]);
  const right = normalize([1, 0, 0.64]);
  const up = [-camera[1] * right[2], camera[0] * right[2] - camera[2] * right[0], camera[1] * right[0]];
  const points = panels.flatMap(faces => faces.flatMap(face => face.vertices));
  const minimum = [right, up].map(axis => Math.min(...points.map(point => dot(point, axis))));
  const maximum = [right, up].map(axis => Math.max(...points.map(point => dot(point, axis))));
  const scale = Math.min((width - 60) / (maximum[0] - minimum[0]), (height - 60) / (maximum[1] - minimum[1]));
  const lighting = normalize([-0.5, 1, -0.8]);
  panels.forEach((faces, panel) => {
    const project = point => [panel * width + width / 2 + (dot(point, right) - (minimum[0] + maximum[0]) / 2) * scale,
      height / 2 - (dot(point, up) - (minimum[1] + maximum[1]) / 2) * scale, dot(point, camera)];
    canvas.rect(panel * width + 12, 12, width - 24, 4, ...(panel ? [80,210,140] : [220,105,95]), 1);
    for (const face of faces) {
      if (dot(face.normal, camera) <= 0) continue;
      const projected = face.vertices.map(project);
      const shade = 0.65 + 0.35 * Math.max(0, dot(face.normal, lighting));
      for (const triangle of [[0,1,2],[0,2,3]]) {
        const [first, second, third] = triangle.map(index => projected[index]);
        const denominator = (second[1]-third[1])*(first[0]-third[0])+(third[0]-second[0])*(first[1]-third[1]);
        if (Math.abs(denominator) < 0.00001) continue;
        const left = Math.max(panel * width, Math.floor(Math.min(first[0], second[0], third[0])));
        const rightEdge = Math.min((panel + 1) * width - 1, Math.ceil(Math.max(first[0], second[0], third[0])));
        const top = Math.max(0, Math.floor(Math.min(first[1], second[1], third[1])));
        const bottom = Math.min(height - 1, Math.ceil(Math.max(first[1], second[1], third[1])));
        for (let pixelY = top; pixelY <= bottom; pixelY++) {
          for (let pixelX = left; pixelX <= rightEdge; pixelX++) {
            const firstWeight = ((second[1]-third[1])*(pixelX+0.5-third[0])+(third[0]-second[0])*(pixelY+0.5-third[1]))/denominator;
            const secondWeight = ((third[1]-first[1])*(pixelX+0.5-third[0])+(first[0]-third[0])*(pixelY+0.5-third[1]))/denominator;
            const weights = [firstWeight, secondWeight, 1-firstWeight-secondWeight];
            if (Math.min(...weights) < -0.00001) continue;
            const distance = weights.reduce((sum, value, index) => sum + value * projected[triangle[index]][2], 0);
            const offset = pixelY * width * 2 + pixelX;
            if (distance <= depth[offset]) continue;
            const uv = [0,1].map(axis => weights.reduce((sum, value, index) => sum + value * face.uv[triangle[index]][axis], 0));
            const texX = Math.max(0, Math.min(face.image.w - 1, Math.floor(uv[0])));
            const texY = Math.max(0, Math.min(face.image.h - 1, Math.floor(uv[1])));
            const texel = (texY * face.image.w + texX) * 4;
            if (face.image.data[texel + 3] < 128) continue;
            depth[offset] = distance;
            canvas.set(pixelX, pixelY, ...[0,1,2].map(channel => face.image.data[texel + channel] * shade), 1);
          }
        }
      }
    }
  });
  canvas.save(path.join(output, `${name}${phase ? '_p2' : ''}.png`));
}

for (const [name, time, phase] of [['idle',0,false], ['walk',0.225,false], ['cannon_fire',0.42,false],
  ['float_move',0.35,true], ['transform',1.12,true]]) render(name, time, phase);
console.log('Compost grip previews: left/red = previous player transform; right/green = corrected boss grip. Offline geometry preview, not a game screenshot.');
