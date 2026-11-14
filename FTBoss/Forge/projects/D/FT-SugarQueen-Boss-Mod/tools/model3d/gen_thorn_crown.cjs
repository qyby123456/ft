const fs = require('fs');
const path = require('path');
const { makeCanvas, writeSafe } = require('./lib_itemtex.cjs');

const project = path.resolve(__dirname, '../..');
const output = path.resolve(process.argv[2] || path.join(project, 'tools/_archive/out/thorn_crown'));
const assets = path.join(output, 'assets/ftboss');
const atlasSize = 64;
const pieces = [];
const add = (name, center, size, material, axis = 'z', angle = 0) => {
  pieces.push({ name, center, size, material, axis, angle });
};
const addThorn = (name, base, height, axis, angle) => {
  const radians = angle * Math.PI / 180;
  const stages = [[0.23, 0.52, 1.1, 'thorn'], [0.59, 0.32, 0.7, 'thorn'], [0.85, 0.26, 0.3, 'tip']];
  stages.forEach(([offset, length, thickness, material], index) => {
    const center = [...base];
    center[1] += offset * height * Math.cos(radians);
    center[axis === 'z' ? 0 : 2] += offset * height * Math.sin(radians) * (axis === 'z' ? -1 : 1);
    add(`${name}_${index}`, center, [thickness, height * length, thickness], material, axis, angle);
  });
};
for (const side of [-1, 1]) {
  add(`band_z_${side}`, [0, 7.3, side * 5.2], [10, 1, 1], 'bark');
  add(`band_x_${side}`, [side * 5.2, 7.55, 0], [1, 1, 10], 'bark');
  for (let index = -2; index <= 2; index++) {
    const height = index === 0 ? 3.7 : 2.8;
    const lean = index === 0 ? 0 : Math.sign(index) * -22.5;
    addThorn(`thorn_z_${side}_${index}`, [index * 2, 7.5, side * 5.2], height, 'z', lean);
  }
  for (let index = -1; index <= 1; index++) {
    addThorn(`thorn_x_${side}_${index}`, [side * 5.2, 7.65, index * 2.8], 2.5, 'x', index * 22.5);
  }
  for (const offset of [-3, 0, 3]) {
    add(`weave_z_${side}_${offset}`, [offset, 7.45, side * 5.5], [3, 1, 1], 'vine', 'z', offset === 0 ? -22.5 : 22.5);
    add(`weave_x_${side}_${offset}`, [side * 5.5, 7.8, offset], [1, 1, 3], 'vine', 'x', offset === 0 ? 22.5 : -22.5);
  }
}
add('blood_heart', [0, 8, -5.85], [2, 2, 1], 'ruby', 'z', 45);
add('blood_drop', [0, 6.25, -5.6], [1, 1, 1], 'ruby', 'z', 45);
for (const side of [-1, 1]) {
  add(`leaf_${side}`, [side * 2.6, 7.5, -5.85], [2, 1, 1], 'leaf', 'z', side * 22.5);
}

const canvas = makeCanvas(atlasSize, atlasSize);
const palette = {
  bark: [[43, 28, 38], [74, 46, 46], [106, 71, 61], [141, 101, 77]],
  vine: [[38, 43, 33], [63, 67, 43], [92, 91, 54], [128, 116, 69]],
  thorn: [[54, 24, 36], [85, 32, 42], [132, 48, 50], [169, 77, 65]],
  tip: [[90, 23, 38], [145, 44, 50], [191, 74, 73], [224, 126, 111]],
  ruby: [[58, 11, 33], [124, 17, 47], [210, 39, 65], [255, 149, 142]],
  leaf: [[26, 46, 35], [42, 70, 43], [69, 98, 52], [123, 143, 70]]
};
const patches = new Map();
let cursorX = 1;
let cursorY = 1;
let rowHeight = 0;
for (const piece of pieces) {
  const key = `${piece.material}:${piece.size.join(',')}`;
  if (!patches.has(key)) {
    const [width, height, depth] = piece.size;
    const patchWidth = Math.ceil(2 * (width + depth));
    const patchHeight = Math.ceil(height + depth);
    if (cursorX + patchWidth + 1 > atlasSize) {
      cursorX = 1;
      cursorY += rowHeight + 2;
      rowHeight = 0;
    }
    if (cursorY + patchHeight + 1 > atlasSize) throw new Error('Crown UV atlas overflow');
    const colors = palette[piece.material];
    for (let pixelY = -1; pixelY <= patchHeight; pixelY++) {
      for (let pixelX = -1; pixelX <= patchWidth; pixelX++) {
        let shade = (pixelX + pixelY * 2) % 7 === 0 ? 1 : 2;
        if (pixelY === 0 || pixelY === depth) shade = 3;
        if (pixelY === patchHeight - 1) shade = 1;
        if (piece.material === 'thorn' || piece.material === 'tip') shade = pixelX % 3 === 0 ? 1 : 2;
        if (piece.material === 'ruby') shade = ((pixelX + pixelY) % 4 + 4) % 4;
        canvas.put(cursorX + pixelX, cursorY + pixelY, colors[shade]);
      }
    }
    patches.set(key, [cursorX, cursorY]);
    cursorX += patchWidth + 2;
    rowHeight = Math.max(rowHeight, patchHeight);
  }
  piece.uv = patches.get(key);
}

const round = value => Math.round(value * 1000000) / 1000000;
const vector = values => values.map(round);
const uvFaces = piece => {
  const [width, height, depth] = piece.size;
  const [originU, originV] = piece.uv;
  return {
    west: [originU, originV + depth, originU + depth, originV + depth + height],
    north: [originU + depth, originV + depth, originU + depth + width, originV + depth + height],
    east: [originU + depth + width, originV + depth, originU + 2 * depth + width, originV + depth + height],
    south: [originU + 2 * depth + width, originV + depth, originU + 2 * depth + 2 * width, originV + depth + height],
    up: [originU + depth, originV, originU + depth + width, originV + depth],
    down: [originU + depth + width, originV, originU + depth + 2 * width, originV + depth]
  };
};
const model = {
  ambientocclusion: false,
  gui_light: 'front',
  textures: { crown: 'ftboss:item/thorn_crown', particle: 'ftboss:item/thorn_crown' },
  display: {
    gui: { rotation: [28, 145, 0], translation: [0, -1, 0], scale: [1.1, 1.1, 1.1] },
    ground: { rotation: [0, 0, 0], translation: [0, 2, 0], scale: [0.55, 0.55, 0.55] },
    fixed: { rotation: [0, 180, 0], translation: [0, 0, 0], scale: [0.85, 0.85, 0.85] },
    head: { rotation: [0, 0, 0], translation: [0, 0, 0], scale: [1, 1, 1] },
    thirdperson_righthand: { rotation: [75, 0, 0], translation: [0, 2, 0], scale: [0.6, 0.6, 0.6] },
    thirdperson_lefthand: { rotation: [75, 0, 0], translation: [0, 2, 0], scale: [0.6, 0.6, 0.6] },
    firstperson_righthand: { rotation: [0, -35, 0], translation: [1, 1, 0], scale: [0.65, 0.65, 0.65] },
    firstperson_lefthand: { rotation: [0, 35, 0], translation: [1, 1, 0], scale: [0.65, 0.65, 0.65] }
  },
  elements: pieces.map(piece => {
    const center = [piece.center[0] + 8, piece.center[1], piece.center[2] + 8];
    const faces = {};
    for (const [direction, rectangle] of Object.entries(uvFaces(piece))) {
      const oriented = ['up', 'down'].includes(direction)
        ? [rectangle[0], rectangle[3], rectangle[2], rectangle[1]]
        : [rectangle[2], rectangle[1], rectangle[0], rectangle[3]];
      faces[direction] = { uv: vector(oriented.map(value => value * 16 / atlasSize)), texture: '#crown' };
    }
    const element = {
      from: vector(center.map((value, axis) => value - piece.size[axis] / 2)),
      to: vector(center.map((value, axis) => value + piece.size[axis] / 2)),
      faces
    };
    if (piece.angle) element.rotation = { origin: vector(center), axis: piece.axis, angle: piece.angle };
    return element;
  })
};

const javaFloat = value => `${round(value).toFixed(6).replace(/0+$/, '').replace(/\.$/, '.0')}F`;
const javaVector = values => values.map(javaFloat).join(', ');
const javaParts = pieces.map(piece => {
  const rotation = [0, 0, 0];
  rotation['xyz'.indexOf(piece.axis)] = -piece.angle * Math.PI / 180;
  return `        head.addOrReplaceChild("${piece.name}", CubeListBuilder.create().texOffs(${piece.uv.join(', ')})\n`
    + `                .addBox(${javaVector(piece.size.map(value => -value / 2))}, ${javaVector(piece.size)}),\n`
    + `                PartPose.offsetAndRotation(${javaVector([piece.center[0], -piece.center[1], piece.center[2]])}, ${javaVector(rotation)}));`;
}).join('\n');
const javaSource = `package net.farmingtales.ftboss.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.world.entity.LivingEntity;

public final class ThornCrownModel extends HumanoidModel<LivingEntity> {
    public ThornCrownModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        var root = mesh.getRoot();
        var head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        for (String part : new String[]{"hat", "body", "right_arm", "left_arm", "right_leg", "left_leg"}) {
            root.addOrReplaceChild(part, CubeListBuilder.create(), PartPose.ZERO);
        }
${javaParts}
        return LayerDefinition.create(mesh, ${atlasSize}, ${atlasSize});
    }
}
`;

const preview = {
  format_version: '1.12.0',
  'minecraft:geometry': [{
    description: { identifier: 'geometry.thorn_crown', texture_width: atlasSize, texture_height: atlasSize,
      visible_bounds_width: 2, visible_bounds_height: 2, visible_bounds_offset: [0, 0.5, 0] },
    bones: [{ name: 'crown', pivot: [0, 0, 0], cubes: pieces.map(piece => {
      const rotation = [0, 0, 0];
      rotation['xyz'.indexOf(piece.axis)] = piece.angle;
      return { origin: vector(piece.center.map((value, axis) => value - piece.size[axis] / 2)),
        size: piece.size, pivot: piece.center, rotation, uv: piece.uv };
    }) }]
  }]
};

const save = (relative, data) => {
  const destination = path.join(output, relative);
  fs.mkdirSync(path.dirname(destination), { recursive: true });
  writeSafe(destination, data);
};
const json = value => JSON.stringify(value, null, 2) + '\n';
save('assets/ftboss/models/item/thorn_crown.json', json(model));
save('ThornCrownModel.java', javaSource);
save('thorn_crown.geo.json', json(preview));
for (const folder of ['textures/item', 'textures/models/armor']) {
  fs.mkdirSync(path.join(assets, folder), { recursive: true });
  canvas.save(path.join(assets, folder, 'thorn_crown.png'));
}
require('./gen_saya_flesh_vision.cjs').generate(assets);
console.log(`Crown: ${pieces.length} cubes; atlas ${atlasSize}x${atlasSize}; UV height ${cursorY + rowHeight}; ${output}`);
