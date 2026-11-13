import fs from 'node:fs';
import path from 'node:path';
import crypto from 'node:crypto';
import { createRequire } from 'node:module';
import { fileURLToPath, pathToFileURL } from 'node:url';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const skill = process.env.MINECRAFT_BUILDER_SKILL ?? 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill';
const loader = createRequire(path.join(skill, 'package.json'));
const lodestone = await import(pathToFileURL(loader.resolve('@mattzh72/lodestone')).href);
const dataRoot = path.join(root, 'src/main/resources/data/fteclipsegen');
const catalog = JSON.parse(fs.readFileSync(path.join(dataRoot, 'catalog.json'), 'utf8'));
const air = 'minecraft:air';
const themes = {
  desert: { wall: 'sandstone', trim: 'cut_sandstone', floor: 'smooth_sandstone', column: 'chiseled_sandstone', accent: 'red_sandstone', dark: 'brown_terracotta', metal: 'gold_block', glass: 'orange_stained_glass', light: 'glowstone', stair: 'sandstone_stairs', slab: 'sandstone_slab', motifs: ['列柱祭殿', '日轮祭坛', '石棺库', '考古工作间', '砂岩拱廊'] },
  frost: { wall: 'calcite', trim: 'polished_diorite', floor: 'smooth_quartz', column: 'quartz_pillar', accent: 'blue_ice', dark: 'cyan_terracotta', metal: 'lapis_block', glass: 'light_blue_stained_glass', light: 'sea_lantern', stair: 'quartz_stairs', slab: 'quartz_slab', motifs: ['档案书库', '冰晶研究台', '冷藏库', '观测厅', '霜纹拱廊'] },
};

function gridFor(size) {
  const [width, height, depth] = size;
  const blocks = new Array(width * height * depth).fill(air);
  const index = (x, y, z) => (y * depth + z) * width + x;
  const inside = (x, y, z) => x >= 0 && x < width && y >= 0 && y < height && z >= 0 && z < depth;
  const set = (x, y, z, block) => {
    if (!inside(x, y, z)) throw new Error(`Out of bounds ${[x, y, z]} / ${size}`);
    blocks[index(x, y, z)] = block.includes(':') ? block : `minecraft:${block}`;
  };
  const get = (x, y, z) => inside(x, y, z) ? blocks[index(x, y, z)] : air;
  const box = (from, to, block) => {
    for (let y = from[1]; y <= to[1]; y++) for (let z = from[2]; z <= to[2]; z++) for (let x = from[0]; x <= to[0]; x++) set(x, y, z, block);
  };
  return { size, blocks, index, set, get, box };
}

function shell(grid, variant, theme) {
  const [width, height, depth] = grid.size;
  const base = variant.key === 'spawn/spawn' ? 4 : variant.key.includes('/layout/') ? 1 : 2;
  grid.box([0, variant.minimum_y, 0], [width - 1, base - 1, depth - 1], theme.floor);
  for (let y = base; y < height; y++) {
    const material = y === base + 3 || y === height - 3 ? theme.trim : theme.wall;
    for (const z of [0, depth - 1]) grid.box([0, y, z], [width - 1, y, z], material);
    for (const x of [0, width - 1]) grid.box([x, y, 0], [x, y, depth - 1], material);
  }
  grid.box([0, height - 1, 0], [width - 1, height - 1, depth - 1], theme.trim);
  for (let z = 1; z < depth - 1; z++) for (let x = 1; x < width - 1; x++) {
    if ((x + z) % 7 === 0) grid.set(x, base - 1, z, theme.accent);
    if (x % 5 === 2 && z % 5 === 2) grid.set(x, height - 1, z, theme.light);
  }
  return base;
}

function pillar(grid, x, z, base, top, theme) {
  grid.set(x, base, z, theme.trim);
  grid.box([x, base + 1, z], [x, top - 1, z], theme.column);
  grid.set(x, top, z, theme.metal);
}

function arch(grid, z, lo, hi, base, top, theme) {
  for (const x of [lo, hi]) pillar(grid, x, z, base, top, theme);
  for (let x = lo; x <= hi; x++) {
    const step = Math.min(2, x - lo, hi - x);
    if (top + step < grid.size[1] - 1) grid.set(x, top + step, z, theme.trim);
    if (step > 0 && top + step - 1 >= base + 3) grid.set(x, top + step - 1, z, theme.accent);
  }
}

function coffin(grid, x, z, base, theme) {
  grid.box([x, base, z], [x + 1, base, z + 2], theme.dark);
  grid.box([x, base + 1, z], [x + 1, base + 1, z + 2], theme.column);
  grid.set(x, base + 1, z, theme.metal);
}

function desk(grid, x, z, base, theme) {
  grid.box([x, base, z], [x + 2, base, z + 1], theme.dark);
  grid.box([x, base + 1, z], [x + 2, base + 1, z + 1], theme.trim);
  grid.set(x + 1, base + 2, z, theme.glass);
  grid.set(x + 2, base + 2, z, theme.light);
}

function stack(grid, x, z, base, height, length, axis = 'z') {
  for (let offset = 0; offset < length; offset++) for (let y = base; y < base + height; y++) grid.set(x + (axis === 'x' ? offset : 0), y, z + (axis === 'z' ? offset : 0), (y - base) % 3 === 0 ? 'polished_diorite' : 'bookshelf');
}

function sun(grid, centerX, centerY, z, radius, theme) {
  for (let x = centerX - radius; x <= centerX + radius; x++) for (let y = centerY - radius; y <= centerY + radius; y++) {
    const distance = Math.hypot(x - centerX, y - centerY);
    if (distance <= radius) grid.set(x, y, z, distance >= radius - 1.2 ? theme.metal : theme.glass);
  }
  grid.set(centerX, centerY, z, theme.light);
}

function crystal(grid, x, z, base, radius, height, theme) {
  for (let offsetY = 0; offsetY < height; offsetY++) {
    const extent = Math.max(0, Math.floor(radius * (1 - offsetY / height)));
    for (let offsetX = -extent; offsetX <= extent; offsetX++) for (let offsetZ = -extent; offsetZ <= extent; offsetZ++) if (Math.abs(offsetX) + Math.abs(offsetZ) <= extent) grid.set(x + offsetX, base + offsetY, z + offsetZ, offsetY === height - 1 ? theme.light : theme.accent);
  }
}

function corridor(grid, variant, theme, style, base) {
  const key = variant.key;
  const top = Math.min(grid.size[1] - 5, base + (style === 'desert' ? 6 : 7));
  if (style === 'desert') {
    for (const z of [3, 8, 12]) arch(grid, z, 2, 13, base, top, theme);
    for (const x of [1, 14]) for (const z of [5, 10]) grid.box([x, base, z], [x, base + 2, z + 1], theme.column);
    for (let z = 1; z < 15; z++) for (const x of [6, 9]) grid.set(x, base - 1, z, theme.accent);
  } else {
    for (const z of [2, 6, 10, 13]) arch(grid, z, 2, 13, base, top, theme);
    for (const z of [2, 7, 12]) {
      grid.box([1, base, z], [2, base + 4, z + 1], theme.glass);
      grid.box([13, base, z], [14, base + 3, z + 1], theme.accent);
    }
    for (let z = 1; z < 15; z++) grid.set(7, base - 1, z, theme.light);
  }
  if (key.endsWith('/door') || key.includes('doors')) for (const z of key.includes('big') ? [5, 10] : [7]) {
    for (const x of [4, 11]) pillar(grid, x, z, base, base + 5, theme);
    grid.box([4, base + 5, z], [11, base + 5, z], theme.metal);
  }
  if (key.endsWith('/bridge')) {
    for (const x of [2, 12]) grid.box([x, base, 3], [x + 1, base + 2, 12], theme.glass);
    for (const x of [3, 12]) grid.box([x, base + 3, 2], [x, base + 3, 13], theme.trim);
    for (const z of [3, 7, 11]) grid.box([5, base - 1, z], [10, base - 1, z], theme.metal);
  }
  if (key.endsWith('/hole')) for (const x of [1, 13]) {
    grid.box([x, base, 5], [x + 1, base + 1, 10], theme.dark);
    grid.box([x, base + 2, 5], [x + 1, base + 2, 10], theme.glass);
  }
  if (key.startsWith('curve/')) {
    if (style === 'desert') {
      for (const x of [10, 12]) pillar(grid, x, 12, base, base + 8, theme);
      sun(grid, 11, base + 5, 14, 3, theme);
    } else {
      stack(grid, 10, 11, base, 6, 4, 'x');
      crystal(grid, 12, 7, base, 1, 5, theme);
    }
  }
  if (key === 'turn/pillar_room') {
    if (style === 'desert') for (const x of [4, 11]) for (const z of [4, 11]) pillar(grid, x, z, base, base + 8, theme);
    else for (const z of [3, 11]) {
      stack(grid, 5, z, base + 4, 4, 6, 'x');
      grid.box([5, base + 3, z], [10, base + 3, z], theme.glass);
    }
  }
}

function room(grid, variant, theme, style, base) {
  const key = variant.key;
  const [width, height, depth] = grid.size;
  const small = width === 12;
  const last = width - 2;
  const back = depth - 2;
  if (style === 'desert') {
    if (key.includes('cells')) {
      for (const z of small ? [2, 7] : [3, 10]) {
        coffin(grid, small ? 3 : 1, z, base, theme);
        coffin(grid, small ? 9 : 12, z, base, theme);
        if (!small) arch(grid, z + 1, 2, 13, base + 3, base + 6, theme);
      }
      grid.box([5, height - 3, 2], [6, height - 3, back], theme.metal);
    } else if (key.includes('storage')) {
      for (const z of small ? [1, 5, 9] : [2, 7, 12]) {
        grid.box([2, base, z], [3, base + 2, Math.min(back, z + 1)], theme.dark);
        grid.set(2, base + 3, z, theme.column);
        grid.box([last - 2, base, z], [last - 1, base + 3, Math.min(back, z + 1)], theme.accent);
        grid.set(last - 2, base + 4, z, theme.metal);
      }
      for (const z of [3, back - 2]) arch(grid, z, 1, last, base + 3, Math.min(base + 6, height - 3), theme);
    } else {
      for (const z of small ? [3, 8] : [3, 10]) {
        desk(grid, 2, z, base, theme);
        desk(grid, last - 3, z, base, theme);
      }
      grid.box([2, base, back], [last, base + 1, back], theme.trim);
      if (!small) sun(grid, 7, base + 6, 14, 3, theme);
    }
  } else if (key.includes('cells')) {
    for (const z of small ? [2, 6] : [2, 7, 11]) {
      grid.box([2, base, z], [3, base + 3, z + 1], theme.accent);
      grid.box([last - 2, base, z], [last - 1, base + 3, z + 1], theme.glass);
      grid.set(2, base + 4, z, theme.light);
    }
    grid.box([5, base + 4, 2], [6, base + 4, back], theme.trim);
  } else if (key.includes('storage')) {
    for (const x of small ? [3, 6, 9] : [2, 5, 10, 13]) for (const z of small ? [4, 7] : [5, 10]) stack(grid, x, z, base, small ? 4 : 6, small ? 2 : 3);
    grid.box([2, base, back], [last - 1, base + 3, back], 'bookshelf');
  } else {
    for (const z of small ? [3, 8] : [4, 10]) {
      grid.box([2, base, z], [4, base, z + 1], theme.trim);
      crystal(grid, 3, z, base + 1, 1, 4, theme);
      desk(grid, last - 3, z, base, theme);
    }
    grid.box([2, base + 5, back], [last, base + 5, back], theme.glass);
  }
  if (key.startsWith('straight/room_')) corridor(grid, variant, theme, style, base);
}

function sanctuary(grid, variant, theme, style, base) {
  const [width, height, depth] = grid.size;
  const entrance = variant.key === 'spawn/spawn';
  const featureZ = entrance ? 2 : depth - 4;
  if (style === 'desert') {
    for (const z of [3, 8, depth - 4]) arch(grid, z, 3, width - 4, base, Math.min(height - 5, base + 7), theme);
    for (const x of [2, width - 4]) coffin(grid, x, depth - 6, base, theme);
    sun(grid, 7, Math.min(height - 5, base + 7), entrance ? 1 : depth - 2, 3, theme);
    grid.box([5, base, featureZ], [10, base + 1, featureZ + 1], theme.accent);
    grid.box([6, base + 2, featureZ], [9, base + 2, featureZ + 1], theme.metal);
  } else {
    for (const x of [2, width - 3]) stack(grid, x, 3, base, Math.min(9, height - base - 3), depth - 6);
    for (const z of [4, depth - 5]) arch(grid, z, 4, width - 5, base, Math.min(height - 5, base + 7), theme);
    grid.box([5, base, featureZ - 1], [10, base, featureZ + 1], theme.trim);
    crystal(grid, 7, featureZ, base + 1, 2, Math.min(6, height - base - 4), theme);
    grid.box([3, height - 2, depth - 2], [width - 4, height - 2, depth - 2], theme.glass);
  }
}

function exterior(grid, variant, theme, style, base) {
  const near = variant.key.endsWith('near');
  if (style === 'desert') {
    for (const z of [3, 7, 11]) arch(grid, z, near ? 3 : 2, near ? 12 : 13, base, near ? 13 : 10, theme);
    sun(grid, 7, near ? 18 : 16, 13, near ? 4 : 3, theme);
    grid.box([4, 20, 3], [11, 21, 12], theme.trim);
  } else {
    for (const z of [3, 8, 12]) {
      stack(grid, 2, z, base, near ? 12 : 7, 3, 'x');
      stack(grid, 11, z, base, near ? 9 : 13, 3, 'x');
    }
    for (const x of [6, 9]) grid.box([x, 14, 3], [x, 16, 12], theme.glass);
    crystal(grid, 7, 7, 17, near ? 2 : 3, 7, theme);
  }
}

function platforms(grid, theme, style, base) {
  grid.box([1, base, 4], [4, 3, 11], theme.trim);
  grid.box([10, base, 4], [14, 3, 11], theme.trim);
  for (const range of [[1, 4], [10, 14]]) for (let z = 2; z <= 3; z++) for (let x = range[0]; x <= range[1]; x++) {
    if (z > 2) grid.box([x, base, z], [x, z - 1, z], theme.wall);
    grid.set(x, z, z, `${theme.stair}[facing=south,half=bottom,shape=straight,waterlogged=false]`);
  }
  grid.box([12, 4, 7], [14, 4, 9], theme.accent);
  for (let z = 7; z <= 9; z++) grid.set(11, 4, z, `${theme.stair}[facing=east,half=bottom,shape=straight,waterlogged=false]`);
  if (style === 'desert') sun(grid, 7, 10, 13, 3, theme);
  else {
    stack(grid, 1, 5, 5, 5, 6);
    crystal(grid, 13, 13, base, 1, 8, theme);
    grid.box([5, 10, 3], [10, 10, 11], theme.glass);
  }
}

function stairs(grid, variant, theme, style) {
  const original = lodestone.Structure.fromNbt(lodestone.NbtFile.read(fs.readFileSync(path.join(dataRoot, 'structures', `${variant.key}.nbt`))).root);
  for (let y = 2; y < 24; y++) for (let z = 1; z < 15; z++) for (let x = 1; x < 15; x++) {
    const source = original.getBlock([x, y, z]);
    const name = source?.state.getName().toString() ?? air;
    if (x >= 5 && x <= 10 && z >= 5 && z <= 10 && y >= 3 && y <= 16) continue;
    if (name === air || /chain|glass|lantern/.test(name)) continue;
    if (name.includes('stairs') || name.includes('slab')) {
      const replacement = name.includes('stairs') ? theme.stair : theme.slab;
      grid.set(x, y, z, `${replacement}[${Object.entries(source.state.getProperties()).map(([key, value]) => `${key}=${value}`).join(',')}]`);
    } else if (!/quartz|amethyst|copper/.test(name)) grid.set(x, y, z, y === 17 ? theme.floor : theme.trim);
  }
  if (style === 'desert') {
    for (const z of [3, 12]) arch(grid, z, 5, 10, 21, 25, theme);
    sun(grid, 7, 25, 8, 3, theme);
    for (const z of [5, 10]) grid.box([5, 29, z], [10, 29, z], theme.metal);
  } else {
    for (const x of [5, 10]) stack(grid, x, 5, 21, 7, 6);
    crystal(grid, 7, 8, 24, 1, 5, theme);
    grid.box([5, 30, 4], [10, 30, 11], theme.glass);
  }
}

function pairedEntrance(grid, theme, style) {
  if (style === 'desert') {
    sun(grid, 7, 10, 1, 4, theme);
    for (let mark = 0; mark < 12; mark++) {
      const angle = mark * Math.PI / 6;
      grid.set(7 + Math.round(5 * Math.cos(angle)), 10 + Math.round(5 * Math.sin(angle)), 1, mark % 3 === 0 ? theme.light : theme.accent);
    }
    for (const x of [2, 11]) {
      grid.box([x, 4, 3], [x + 2, 4, 6], theme.accent);
      grid.box([x, 5, 3], [x + 2, 5, 5], theme.trim);
      grid.box([x + 1, 6, 4], [x + 1, 9, 4], theme.column);
      grid.set(x + 1, 10, 4, theme.metal);
      grid.set(x + 1, 11, 4, `${theme.slab}[type=bottom,waterlogged=false]`);
      grid.box([x, 4, 10], [x + 2, 4, 12], theme.trim);
      grid.box([x, 5, 11], [x + 2, 5, 11], theme.dark);
      grid.set(x + 1, 6, 11, theme.metal);
    }
    for (let x = 1; x <= 14; x++) {
      const y = 11 + Math.min(3, x, 15 - x);
      grid.set(x, y, 6, theme.trim);
      grid.set(x, y - 1, 6, theme.column);
    }
    grid.box([7, 9, 5], [8, 15, 6], 'yellow_stained_glass');
    grid.box([6, 15, 4], [9, 15, 7], theme.metal);
    grid.box([7, 16, 5], [8, 16, 6], theme.light);
    for (let z = 4; z < 14; z++) for (let x = 2; x < 14; x++) {
      const radius = Math.hypot(x - 7.5, z - 8.5);
      if (Math.abs(radius - 4) < 0.6 || (x === 4 || x === 11) && z % 2 === 0) grid.set(x, 3, z, theme.metal);
    }
    grid.box([1, 8, 8], [1, 12, 13], theme.column);
    grid.box([14, 8, 8], [14, 12, 13], theme.column);
  } else {
    for (const x of [1, 13]) for (const z of [3, 7, 11]) {
      stack(grid, x, z, 4, 6, 2);
      grid.box([x, 10, z], [x + 1, 10, z + 1], theme.trim);
      grid.box([x + 1, 4, z], [x + 1, 7, z + 1], theme.glass);
    }
    grid.box([5, 4, 1], [10, 5, 3], theme.trim);
    grid.box([6, 6, 2], [9, 6, 3], theme.metal);
    crystal(grid, 7, 2, 7, 2, 6, theme);
    for (const x of [4, 11]) {
      grid.box([x, 4, 1], [x, 10, 2], theme.column);
      grid.box([x, 11, 1], [x, 13, 1], theme.accent);
    }
    for (let x = 4; x <= 11; x++) grid.set(x, 11 + Math.min(2, x - 4, 11 - x), 1, theme.glass);
    for (let x = 3; x <= 12; x++) for (let z = 4; z <= 12; z++) {
      const distance = Math.abs(x - 7.5) + Math.abs(z - 8);
      if (distance >= 4 && distance < 5) grid.set(x, 11, z, theme.accent);
      if (distance >= 3 && distance < 4) grid.set(x, 12, z, theme.trim);
      if (Math.abs(distance - 4) < 0.6) grid.set(x, 3, z, theme.metal);
    }
    for (const [x, z] of [[4, 8], [11, 8], [7, 5], [7, 11]]) grid.box([x, 12, z], [x, 15, z], theme.glass);
    grid.box([2, 4, 12], [4, 4, 13], theme.trim);
    grid.set(3, 5, 12, theme.light);
    grid.box([11, 4, 12], [13, 5, 13], theme.accent);
  }
}

function terraceRamp(grid, fromX, toX, fromZ, fromY, rise, theme) {
  for (let step = 0; step < rise; step++) for (let x = fromX; x <= toX; x++) {
    if (step > 0) grid.box([x, fromY, fromZ + step], [x, fromY + step - 1, fromZ + step], theme.trim);
    grid.set(x, fromY + step, fromZ + step, `${theme.stair}[facing=south,half=bottom,shape=straight,waterlogged=false]`);
  }
}

function hangingCrystal(grid, x, z, tip, radius, height, theme) {
  for (let y = tip; y < tip + height; y++) {
    const progress = (y - tip) / (height - 1);
    const extent = Math.floor(radius * Math.min(1, progress * 1.5, (1 - progress) * 3 + 0.2));
    for (let deltaX = -extent; deltaX <= extent; deltaX++) for (let deltaZ = -extent; deltaZ <= extent; deltaZ++) if (Math.abs(deltaX) + Math.abs(deltaZ) <= extent) grid.set(x + deltaX, y, z + deltaZ, y % 3 === 0 ? theme.glass : theme.accent);
  }
  grid.set(x, tip, z, theme.light);
  grid.box([x, tip + height, z], [x, 21, z], theme.glass);
}

function boss(grid, theme, style, base) {
  if (style === 'desert') {
    for (const [low, high] of [[1, 6], [27, 32]]) {
      grid.box([low, 2, 5], [high, 4, 20], theme.trim);
      const upperFrom = low === 1 ? 1 : 30;
      const upperTo = low === 1 ? 3 : 32;
      grid.box([upperFrom, 5, 10], [upperTo, 8, 20], theme.wall);
      terraceRamp(grid, low === 1 ? 2 : 29, low === 1 ? 4 : 31, 2, 2, 3, theme);
      terraceRamp(grid, upperFrom, upperTo, 6, 5, 4, theme);
      for (const z of [11, 17]) coffin(grid, low === 1 ? 4 : 28, z, 5, theme);
      for (const z of [10, 15, 20]) grid.box([upperFrom, 8, z], [upperTo, 8, z], theme.metal);
      for (const z of [6, 14, 20]) {
        grid.box([low === 1 ? 1 : 32, 9, z], [low === 1 ? 1 : 32, 15, z], theme.column);
        grid.box([low, 15, z], [high, 15, z], theme.trim);
      }
    }
    sun(grid, 16, 12, 21, 8, theme);
    for (let mark = 0; mark < 16; mark++) {
      const angle = mark * Math.PI / 8;
      const x = 16 + Math.round(9 * Math.cos(angle));
      const y = 12 + Math.round(9 * Math.sin(angle));
      grid.set(x, y, 21, mark % 4 === 0 ? theme.light : theme.accent);
    }
    for (const z of [4, 18]) {
      for (let x = 6; x <= 27; x++) {
        const y = 16 + Math.min(3, x - 6, 27 - x);
        grid.set(x, y, z, theme.column);
        grid.set(x, y + 1, z, theme.trim);
      }
    }
    for (const x of [8, 24]) grid.box([x, 2, 23], [x + 1, 5, 25], theme.column);
    for (let z = 4; z <= 20; z++) for (let x = 9; x <= 24; x++) {
      const radius = Math.hypot(x - 16.5, z - 12);
      if (Math.abs(radius - 6) < 0.55) grid.set(x, 1, z, theme.metal);
    }
    for (let z = 3; z <= 20; z++) for (const x of [11, 22]) if (z % 3 !== 0) grid.set(x, 1, z, theme.accent);
  } else {
    for (const z of [3, 10, 17]) {
      for (const x of [1, 31]) {
        stack(grid, x, z, base, 8, 4);
        grid.box([x, 10, z], [x + 1, 10, z + 3], theme.trim);
        grid.box([x + (x === 1 ? 2 : -2), 2, z], [x + (x === 1 ? 2 : -2), 7, z], theme.column);
        grid.box([x + (x === 1 ? 2 : -2), 2, z + 3], [x + (x === 1 ? 2 : -2), 7, z + 3], theme.column);
      }
      grid.box([3, 2, z + 1], [5, 3, z + 2], theme.glass);
      grid.box([28, 2, z + 1], [30, 3, z + 2], theme.glass);
    }
    for (const z of [4, 10, 16, 20]) for (let x = 1; x <= 32; x++) {
      const normalized = Math.abs(x - 16.5) / 15.5;
      const y = 10 + Math.floor(10 * (1 - normalized * normalized));
      grid.box([x, y, z], [x, Math.min(21, y + 2), z], theme.trim);
      if (y > 10) grid.set(x, y - 1, z, theme.glass);
    }
    hangingCrystal(grid, 11, 7, 11, 2, 7, theme);
    hangingCrystal(grid, 22, 7, 11, 2, 7, theme);
    hangingCrystal(grid, 16, 14, 9, 3, 11, theme);
    grid.box([8, 8, 21], [25, 9, 21], theme.metal);
    for (const [x, peak] of [[8, 13], [11, 16], [14, 19], [18, 20], [21, 17], [25, 14]]) {
      grid.box([x - 1, 10, 21], [x + 1, peak - 2, 21], theme.accent);
      grid.box([x, peak - 1, 21], [x, peak, 21], theme.glass);
      grid.set(x, peak, 21, theme.light);
    }
    for (const x of [3, 29]) stack(grid, x, 24, 2, 7, 3);
    for (let x = 7; x < 27; x++) for (let z = 4; z < 21; z++) {
      const distance = Math.abs(x - 16.5) + Math.abs(z - 12);
      if (Math.abs(distance - 7) < 0.6) grid.set(x, 1, z, theme.metal);
      if (Math.abs(distance - 10) < 0.6) grid.set(x, 1, z, theme.accent);
    }
  }
}

function contracts(grid, variant, theme) {
  const [width, height, depth] = grid.size;
  const base = variant.key === 'spawn/spawn' ? 4 : variant.key.includes('/layout/') ? 1 : 2;
  if (!variant.key.startsWith('stairs/')) {
    const routeX = variant.key.includes('/layout/') ? [4, 6] : variant.key.startsWith('boss/') ? [15, 18] : [6, 9];
    const fromZ = variant.key === 'spawn/spawn' ? 4 : 1;
    const toZ = variant.key === 'promenade' ? 11 : depth - 2;
    grid.box([routeX[0], base, fromZ], [routeX[1], base + 2, toZ], air);
    grid.box([routeX[0], base - 1, fromZ], [routeX[1], base - 1, toZ], theme.floor);
  }
  for (const port of variant.ports) for (const [x, y, z] of port.cells) {
    const inward = x === 0 ? [1, 0] : x === width - 1 ? [-1, 0] : z === 0 ? [0, 1] : [0, -1];
    for (let distance = 0; distance <= 3; distance++) {
      const nextX = x + inward[0] * distance;
      const nextZ = z + inward[1] * distance;
      grid.set(nextX, y - 1, nextZ, theme.floor);
      grid.box([nextX, y, nextZ], [nextX, y + 4, nextZ], air);
    }
  }
  for (const socket of variant.sockets) {
    for (const [x, y, z] of [socket.position, socket.spawn_position]) {
      grid.set(x, y - 1, z, theme.floor);
      grid.box([x, y, z], [x, y + 2, z], air);
      if (!variant.key.startsWith('stairs/')) for (let deltaZ = -1; deltaZ <= 1; deltaZ++) for (let deltaX = -1; deltaX <= 1; deltaX++) {
        const nextX = x + deltaX;
        const nextZ = z + deltaZ;
        if (nextX > 0 && nextX < width - 1 && nextZ > 0 && nextZ < depth - 1) grid.box([nextX, y, nextZ], [nextX, y + 2, nextZ], air);
      }
    }
    grid.set(...socket.support, theme.floor);
  }
  const safe = variant.key === 'spawn/spawn' ? [[7, 4, 8]] : variant.key === 'promenade' ? [[7, 2, 11]] : variant.key.startsWith('boss/') ? [[16, 2, 2], [16, 2, 25], [16, 2, 13]] : [];
  for (const [x, y, z] of safe) {
    grid.box([x - 1, y, z - 1], [x + 1, y + 4, z + 1], air);
    grid.box([x - 1, y - 1, z - 1], [x + 1, y - 1, z + 1], theme.floor);
  }
  if (variant.key.startsWith('boss/')) {
    grid.box([1, 2, 22], [width - 2, height - 2, 22], theme.wall);
    grid.box([15, 2, 22], [18, 7, 22], air);
    for (const x of [14, 19]) grid.box([x, 2, 22], [x, 8, 22], theme.column);
    grid.box([14, 8, 22], [19, 8, 22], theme.metal);
  }
  for (let y = variant.minimum_y; y < height; y++) for (let x = 0; x < width; x++) for (let z = 0; z < depth; z++) {
    if (x !== 0 && x !== width - 1 && z !== 0 && z !== depth - 1) continue;
    const opening = variant.ports.some(port => port.cells.some(cell => cell[0] === x && cell[2] === z && y >= cell[1] && y <= cell[1] + 4));
    grid.set(x, y, z, opening ? air : theme.wall);
  }
}

export function design(style, variant) {
  const theme = themes[style];
  const grid = gridFor(variant.size);
  const base = shell(grid, variant, theme);
  if (variant.key.startsWith('stairs/')) stairs(grid, variant, theme, style);
  else if (variant.key.startsWith('boss/')) boss(grid, theme, style, base);
  else if (variant.key.includes('/layout/') || variant.key.includes('/room_') || variant.key.startsWith('terminal/')) room(grid, variant, theme, style, base);
  else if (variant.key === 'spawn/spawn') pairedEntrance(grid, theme, style);
  else if (variant.key === 'promenade') sanctuary(grid, variant, theme, style, base);
  else if (variant.key.startsWith('spawn/outside')) exterior(grid, variant, theme, style, base);
  else if (variant.key === 'straight/platform') platforms(grid, theme, style, base);
  else corridor(grid, variant, theme, style, base);
  contracts(grid, variant, theme);
  return grid;
}

function asStructure(grid, Structure) {
  const structure = new Structure(grid.size);
  const states = new Map();
  for (let y = 0; y < grid.size[1]; y++) for (let z = 0; z < grid.size[2]; z++) for (let x = 0; x < grid.size[0]; x++) {
    const text = grid.get(x, y, z);
    if (!states.has(text)) states.set(text, lodestone.BlockState.parse(text));
    const state = states.get(text);
    structure.addBlock([x, y, z], state.getName(), state.getProperties());
  }
  return structure;
}

export default function build(api) {
  const style = process.env.ROOM_STYLE ?? 'desert';
  const key = process.env.ROOM_KEY ?? 'boss/eclipse_keeper';
  const variant = catalog.variants.find(candidate => candidate.key === key);
  if (!themes[style] || !variant) throw new Error('Unknown ROOM_STYLE / ROOM_KEY');
  const grid = design(style, variant);
  if (process.env.ROOM_CUTAWAY === '1') {
    const [width, height, depth] = grid.size;
    const base = key === 'spawn/spawn' ? 4 : key.includes('/layout/') ? 1 : 2;
    for (let y = base; y < height; y++) for (let z = 0; z < depth; z++) for (let x = 0; x < width; x++) if (x === 0 || x === width - 1 || z === 0 || z === depth - 1 || y === height - 1) grid.set(x, y, z, air);
  }
  return { structure: asStructure(grid, api.Structure), metadata: { name: `rooms-team2-${style}-${key.replaceAll('/', '-')}${process.env.ROOM_CUTAWAY === '1' ? '-cutaway' : ''}` } };
}

function full(block) { return block !== air && !/_stairs|_slab|chain|lantern|torch|bars|pane/.test(block); }

function shapeAt(block, halfX, halfY, halfZ) {
  if (block === air) return false;
  if (block.includes('_slab')) return block.includes('type=top') ? halfY === 1 : block.includes('type=double') ? true : halfY === 0;
  if (block.includes('_stairs')) {
    const back = block.includes('facing=north') ? halfZ === 0 : block.includes('facing=south') ? halfZ === 1 : block.includes('facing=east') ? halfX === 1 : halfX === 0;
    return block.includes('half=top') ? halfY === 1 || back : halfY === 0 || back;
  }
  return true;
}

function navigation(grid, targets) {
  const width = grid.size[0] * 2;
  const height = grid.size[1] * 2;
  const depth = grid.size[2] * 2;
  const index = (x, y, z) => (y * depth + z) * width + x;
  const collision = new Uint8Array(width * height * depth);
  for (let y = 0; y < height; y++) for (let z = 0; z < depth; z++) for (let x = 0; x < width; x++) collision[index(x, y, z)] = shapeAt(grid.get(x >> 1, y >> 1, z >> 1), x % 2, y % 2, z % 2) ? 1 : 0;
  const walkable = (x, y, z) => x >= 0 && x < width && z >= 0 && z < depth && y >= 1 && y + 3 < height && collision[index(x, y - 1, z)] && [0, 1, 2, 3].every(offset => !collision[index(x, y + offset, z)]);
  const seen = new Uint8Array(collision.length);
  const queue = [];
  const origin = targets[0];
  if (!origin) return { visited: 0, missing: [] };
  for (const deltaX of [0, 1]) for (const deltaZ of [0, 1]) {
    const point = [origin[0] * 2 + deltaX, origin[1] * 2, origin[2] * 2 + deltaZ];
    if (walkable(...point)) { seen[index(...point)] = 1; queue.push(point); }
  }
  for (let head = 0; head < queue.length; head++) {
    const [x, y, z] = queue[head];
    for (const [deltaX, deltaZ] of [[1, 0], [-1, 0], [0, 1], [0, -1]]) for (const deltaY of [0, 1, -1]) {
      const nextX = x + deltaX;
      const nextY = y + deltaY;
      const nextZ = z + deltaZ;
      if (!walkable(nextX, nextY, nextZ) || seen[index(nextX, nextY, nextZ)]) continue;
      if (deltaY === 1 && collision[index(x, y + 4, z)]) continue;
      if (deltaY === -1 && collision[index(nextX, nextY + 4, nextZ)]) continue;
      seen[index(nextX, nextY, nextZ)] = 1;
      queue.push([nextX, nextY, nextZ]);
    }
  }
  const missing = targets.filter(([x, y, z]) => ![0, 1].some(deltaX => [0, 1].some(deltaZ => seen[index(x * 2 + deltaX, y * 2, z * 2 + deltaZ)])));
  return { visited: queue.length, missing };
}

function validate(grid, variant, bytes) {
  const failures = [];
  const check = (condition, text) => { if (!condition) failures.push(text); };
  const nbt = lodestone.NbtFile.read(bytes).root;
  const loaded = lodestone.Structure.fromNbt(nbt);
  check(nbt.getNumber('DataVersion') === 3465, 'DataVersion');
  check(JSON.stringify(loaded.getSize()) === JSON.stringify(variant.size), 'exact catalog size');
  const palette = nbt.getList('palette');
  for (let paletteIndex = 0; paletteIndex < palette.length; paletteIndex++) check(/^minecraft:[a-z0-9_]+$/.test(palette.getCompound(paletteIndex).getString('Name')), 'Name/Properties separation');
  check(nbt.getList('entities').length === 0, 'entities');
  check(loaded.getBlocks().length === grid.blocks.length, 'dense volume');
  check(loaded.getBlocks().every(block => !block.nbt), 'block entities');
  const targets = variant.ports.flatMap(port => port.cells);
  let spawnCandidates = 0;
  const reservedCandidates = new Set();
  const independentSpawns = [];
  for (const socket of variant.sockets) {
    for (const point of [socket.position, socket.spawn_position]) {
      targets.push(point);
      const [x, y, z] = point;
      check([0, 1, 2].every(offset => grid.get(x, y + offset, z) === air), `socket clearance ${point}`);
      check(full(grid.get(x, y - 1, z)), `socket support ${point}`);
    }
    check(full(grid.get(...socket.support)), `declared support ${socket.id}`);
    const [x, y, z] = socket.position;
    const candidates = [];
    for (let nextZ = Math.max(1, z - 6); nextZ <= Math.min(grid.size[2] - 2, z + 6); nextZ++) for (let nextX = Math.max(1, x - 6); nextX <= Math.min(grid.size[0] - 2, x + 6); nextX++) {
      const distance = Math.abs(nextX - x) + Math.abs(nextZ - z);
      if (distance < 1 || distance > 6 || reservedCandidates.has(`${nextX},${y},${nextZ}`) || variant.sockets.some(other => other.position[0] === nextX && other.position[2] === nextZ && other.position[1] === y)) continue;
      if (full(grid.get(nextX, y - 1, nextZ)) && [0, 1, 2].every(offset => grid.get(nextX, y + offset, nextZ) === air)) candidates.push([nextX, y, nextZ]);
    }
    check(candidates.length > 0, `independent spawn candidate ${socket.id}`);
    candidates.sort((left, right) => Math.abs(left[0] - x) + Math.abs(left[2] - z) - Math.abs(right[0] - x) - Math.abs(right[2] - z) || left[0] - right[0] || left[2] - right[2]);
    if (candidates.length) {
      targets.push(candidates[0]);
      reservedCandidates.add(candidates[0].join(','));
      independentSpawns.push({ socket: socket.id, position: candidates[0] });
      spawnCandidates++;
    }
  }
  const safe = variant.key === 'spawn/spawn' ? [[7, 4, 8]] : variant.key === 'promenade' ? [[7, 2, 11]] : variant.key.startsWith('boss/') ? [[16, 2, 2], [16, 2, 25], [16, 2, 13]] : [];
  for (const [x, y, z] of safe) {
    targets.push([x, y, z]);
    check(full(grid.get(x, y - 1, z)) && [0, 1, 2].every(offset => grid.get(x, y + offset, z) === air), `safe point ${[x, y, z]}`);
  }
  if (variant.key.startsWith('boss/')) {
    for (let z = 4; z <= 20; z++) for (let x = 10; x <= 23; x++) check(full(grid.get(x, 1, z)) && [2, 3, 4].every(y => grid.get(x, y, z) === air), `clear combat floor ${[x, 2, z]}`);
    if (grid.get(2, 8, 12) !== air && grid.get(31, 8, 12) !== air && grid.get(2, 9, 12) === air && grid.get(31, 9, 12) === air) targets.push([2, 9, 12], [31, 9, 12], [5, 5, 7], [28, 5, 7]);
  }
  for (const port of variant.ports) for (const [x, y, z] of port.cells) {
    check([0, 1, 2, 3, 4].every(offset => grid.get(x, y + offset, z) === air), `port clearance ${[x, y, z]}`);
    check(full(grid.get(x, y - 1, z)), `port support ${[x, y, z]}`);
  }
  for (let y = 0; y < variant.minimum_y; y++) for (let z = 0; z < grid.size[2]; z++) for (let x = 0; x < grid.size[0]; x++) check(grid.get(x, y, z) === air, 'minimum_y');
  for (let y = variant.minimum_y; y < grid.size[1]; y++) for (let z = 0; z < grid.size[2]; z++) for (let x = 0; x < grid.size[0]; x++) {
    if (x > 0 && x < grid.size[0] - 1 && z > 0 && z < grid.size[2] - 1) continue;
    const opening = variant.ports.some(port => port.cells.some(cell => cell[0] === x && cell[2] === z && y >= cell[1] && y <= cell[1] + 4));
    check((grid.get(x, y, z) === air) === opening, `shell ${[x, y, z]}`);
  }
  const walk = navigation(grid, targets);
  check(walk.missing.length === 0, `unreachable ${JSON.stringify(walk.missing)}`);
  if (variant.key.startsWith('boss/')) {
    for (let y = 2; y < grid.size[1] - 1; y++) for (let x = 1; x < grid.size[0] - 1; x++) check((grid.get(x, y, 22) === air) === (x >= 15 && x <= 18 && y <= 7), `Boss barrier partition ${[x, y, 22]}`);
  }
  if (!variant.key.startsWith('stairs/')) {
    const routeX = variant.key.includes('/layout/') ? [4, 6] : variant.key.startsWith('boss/') ? [15, 18] : [6, 9];
    const base = variant.key === 'spawn/spawn' ? 4 : variant.key.includes('/layout/') ? 1 : 2;
    for (let z = variant.key === 'spawn/spawn' ? 4 : 1; z <= (variant.key === 'promenade' ? 11 : grid.size[2] - 2); z++) for (let x = routeX[0]; x <= routeX[1]; x++) check(full(grid.get(x, base - 1, z)) && [0, 1, 2].every(offset => grid.get(x, base + offset, z) === air), `main route ${[x, base, z]}`);
  }
  return { key: variant.key, size: variant.size, minimum_y: variant.minimum_y, ports: variant.ports.length, sockets: variant.sockets.length, spawnCandidates, independentSpawns, blocks: grid.blocks.length, ...walk, failures };
}

function gallery(grids) {
  const cards = [];
  for (const style of Object.keys(themes)) for (const variant of catalog.variants) {
    const grid = grids.get(`${style}/${variant.key}`);
    const base = variant.key === 'spawn/spawn' ? 4 : variant.key.includes('/layout/') ? 1 : 2;
    const slices = [...new Set([base, base + 4, ...(variant.key.startsWith('stairs/') ? [7, 12, 18] : [])])];
    const maps = slices.map(y => {
      let pixels = '';
      for (let z = 0; z < grid.size[2]; z++) for (let x = 0; x < grid.size[0]; x++) {
        const block = grid.get(x, y, z);
        const color = block === air ? '#19202c' : /glass/.test(block) ? '#67b8cf' : /gold|lantern|glowstone/.test(block) ? '#f6d668' : /bookshelf/.test(block) ? '#55718c' : style === 'desert' ? '#c9a375' : '#c9e5eb';
        pixels += `<rect x="${x * 8}" y="${z * 8}" width="8" height="8" fill="${color}"/>`;
      }
      for (const socket of variant.sockets) if (socket.position[1] === y) pixels += `<circle cx="${socket.position[0] * 8 + 4}" cy="${socket.position[2] * 8 + 4}" r="2.5" fill="#ee5c98"/>`;
      return `<figure><svg viewBox="0 0 ${grid.size[0] * 8} ${grid.size[2] * 8}">${pixels}</svg><figcaption>y=${y}</figcaption></figure>`;
    }).join('');
    cards.push(`<article data-style="${style}"><h2>${style} · ${variant.key}</h2><div>${maps}</div><p>${variant.size.join(' × ')} · ${variant.sockets.length} sockets</p></article>`);
  }
  return `<!doctype html><html lang="zh-CN"><meta charset="utf-8"><title>沙漠 / 冰霜 54 房间</title><style>body{margin:24px;background:#111821;color:#dce8ee;font:14px system-ui}h1{font-size:26px}button{padding:8px 18px;margin:4px;background:#253849;color:white;border:1px solid #597287;border-radius:6px}main{display:grid;grid-template-columns:repeat(auto-fit,minmax(410px,1fr));gap:14px}article{background:#1b2734;padding:16px;border-radius:12px}article>div{display:flex;flex-wrap:wrap;gap:12px}figure{margin:0;flex:1;min-width:100px;max-width:270px}svg{width:100%;image-rendering:pixelated}h2{font-size:15px}p,figcaption{color:#9cb0c1}</style><h1>沙漠 / 冰霜：54 个独立房间</h1><p>俯视剖层，粉色为 socket。楼梯复用行走骨架，移除中央塔芯并重建主题内饰。</p><nav><button onclick="filter('all')">全部</button><button onclick="filter('desert')">沙漠</button><button onclick="filter('frost')">冰霜</button></nav><main>${cards.join('')}</main><script>function filter(style){document.querySelectorAll('article').forEach(card=>card.hidden=style!=='all'&&card.dataset.style!==style)}</script></html>`;
}

async function main() {
  const out = path.join(root, 'output');
  fs.mkdirSync(out, { recursive: true });
  const report = { schema: 1, generator: 'tools/rooms-team2.mjs', dataVersion: 3465, navigation: 'Half-block collision lattice, two-block headroom, maximum rise 0.5 block, no jump edges', stairReuse: 'Only stairs/* reuse original walk skeleton; central tower removed, shell and upper thematic hall rebuilt', styles: {}, geometryDifferences: [] };
  const grids = new Map();
  for (const style of Object.keys(themes)) {
    const templates = [];
    const checks = [];
    for (const variant of catalog.variants) {
      const grid = design(style, variant);
      grids.set(`${style}/${variant.key}`, grid);
      const structure = asStructure(grid, lodestone.Structure);
      const bytes = structure.writeNbt({ name: '', dataVersion: 3465, compression: 'gzip' });
      const check = validate(grid, variant, bytes);
      checks.push(check);
      const destination = path.join(dataRoot, 'roomsets', style, `${variant.key}.nbt`);
      fs.mkdirSync(path.dirname(destination), { recursive: true });
      const existing = fs.existsSync(destination) ? fs.readFileSync(destination) : undefined;
      if (existing && !['spawn/spawn', 'boss/eclipse_keeper'].includes(variant.key) && !Buffer.from(bytes).equals(existing)) throw new Error(`Protected template changed: ${style}/${variant.key}`);
      if (!existing || !Buffer.from(bytes).equals(existing)) fs.writeFileSync(destination, bytes);
      templates.push({ key: variant.key, sha256: crypto.createHash('sha256').update(fs.readFileSync(destination)).digest('hex') });
      console.log(`${style} ${variant.key}: ${check.failures.length ? check.failures.join('; ') : 'PASS'}`);
    }
    const manifestFile = path.join(dataRoot, 'roomsets', style, 'manifest.json');
    const manifestText = `${JSON.stringify({ schema: 1, style: `fteclipsegen:${style}`, templates }, null, 2)}\n`;
    if (!fs.existsSync(manifestFile) || fs.readFileSync(manifestFile, 'utf8') !== manifestText) fs.writeFileSync(manifestFile, manifestText);
    report.styles[style] = { templates: templates.length, motifs: themes[style].motifs, checks };
  }
  for (const variant of catalog.variants) {
    const desert = grids.get(`desert/${variant.key}`);
    const frost = grids.get(`frost/${variant.key}`);
    const original = lodestone.Structure.fromNbt(lodestone.NbtFile.read(fs.readFileSync(path.join(dataRoot, 'structures', `${variant.key}.nbt`))).root);
    let betweenStyles = 0;
    let desertOriginal = 0;
    let frostOriginal = 0;
    for (let y = 0; y < variant.size[1]; y++) for (let z = 0; z < variant.size[2]; z++) for (let x = 0; x < variant.size[0]; x++) {
      const oldSolid = (original.getBlock([x, y, z])?.state.getName().toString() ?? air) !== air;
      const desertSolid = desert.get(x, y, z) !== air;
      const frostSolid = frost.get(x, y, z) !== air;
      betweenStyles += desertSolid !== frostSolid;
      desertOriginal += desertSolid !== oldSolid;
      frostOriginal += frostSolid !== oldSolid;
    }
    report.geometryDifferences.push({ key: variant.key, betweenStyles, desertOriginal, frostOriginal, volume: desert.blocks.length });
  }
  for (const style of Object.keys(themes)) {
    report.styles[style].occupancySignature = crypto.createHash('sha256').update(catalog.variants.map(variant => grids.get(`${style}/${variant.key}`).blocks.map(block => block === air ? '0' : '1').join('')).join('|')).digest('hex');
    report.styles[style].changedFromOriginal = report.geometryDifferences.filter(entry => entry[`${style}Original`] > 0).length;
    report.styles[style].uniqueGeometryTemplates = new Set(catalog.variants.map(variant => crypto.createHash('sha256').update(grids.get(`${style}/${variant.key}`).blocks.map(block => block === air ? '0' : '1').join('')).digest('hex'))).size;
  }
  report.failures = Object.entries(report.styles).flatMap(([style, entry]) => entry.checks.filter(check => check.failures.length).map(check => ({ style, key: check.key, failures: check.failures })));
  fs.writeFileSync(path.join(out, 'rooms-team2-validation.json'), `${JSON.stringify(report, null, 2)}\n`);
  fs.writeFileSync(path.join(out, 'rooms-team2-gallery.html'), gallery(grids));
  if (report.failures.length) process.exitCode = 1;
}

if (process.argv[1] && path.resolve(process.argv[1]) === fileURLToPath(import.meta.url)) await main();
