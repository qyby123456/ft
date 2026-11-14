'use strict';

const fs = require('fs');
const path = require('path');
const root = path.resolve(__dirname, '../..');
const models = path.join(root, 'src/main/resources/assets/ftboss/models/item');
const source = JSON.parse(fs.readFileSync(path.join(root, 'ref/barrel_crossbow_draw_original.json'), 'utf8'));
const output = process.argv[2] ? path.resolve(process.argv[2]) : models;
const groups = {
  left: [14, 15, 16, 23],
  right: [19, 20, 21, 22],
  latch: [31],
  string: [32]
};
const moving = new Set(Object.values(groups).flat());
const clone = value => JSON.parse(JSON.stringify(value));
const display = clone(source.display);
delete display.on_shelf;
const base = { textures: source.textures, display };
const parts = {
  body: source.elements.filter((element, index) => !moving.has(index)),
  left: groups.left.map(index => source.elements[index]),
  right: groups.right.map(index => source.elements[index]),
  latch: groups.latch.map(index => source.elements[index])
};

for (const side of ['left', 'right']) {
  const string = clone(source.elements[32]);
  string.from[0] = side === 'left' ? -4 : 8;
  string.to[0] = side === 'left' ? 8 : 20;
  string.faces = Object.fromEntries(['up', 'down'].map(face => {
    const material = clone(source.elements[32].faces[face]);
    const middle = (material.uv[0] + material.uv[2]) / 2;
    material.uv[side === 'left' ? 2 : 0] = middle;
    return [face, material];
  }));
  parts['string_' + side] = [string];
}

function save(relative, value) {
  const destination = path.join(output, relative);
  fs.mkdirSync(path.dirname(destination), { recursive: true });
  fs.writeFileSync(destination, JSON.stringify(value, null, 2) + '\n', 'utf8');
  console.log(relative + ': ' + fs.statSync(destination).size + ' bytes');
}

for (const [part, elements] of Object.entries(parts)) {
  save('barrel_crossbow_parts/' + part + '.json', { textures: source.textures, elements });
}
save('barrel_crossbow.json', { ...base, elements: Object.values(parts).flat() });
const ready = JSON.parse(fs.readFileSync(path.join(root, 'ref/barrel_crossbow_ready_draw_original.json'), 'utf8'));
delete ready.display.on_shelf;
for (const element of ready.elements) {
  if (element.from[1] === element.to[1]) {
    element.faces = Object.fromEntries(Object.entries(element.faces).filter(([face]) => face === 'up' || face === 'down'));
  }
}
save('barrel_crossbow_ready.json', { textures: ready.textures, display, elements: ready.elements });
