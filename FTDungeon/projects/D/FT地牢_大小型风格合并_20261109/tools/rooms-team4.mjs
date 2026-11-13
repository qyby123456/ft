import fs from 'node:fs/promises';
import path from 'node:path';
import crypto from 'node:crypto';
import { pathToFileURL, fileURLToPath } from 'node:url';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const skill = process.env.MINECRAFT_BUILDER_SKILL || 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill';
const { Structure, NbtFile } = await import(pathToFileURL(path.join(skill, 'node_modules/@mattzh72/lodestone/lib/index.js')));
const data = path.join(root, 'src/main/resources/data/fteclipsegen');
const catalog = JSON.parse(await fs.readFile(path.join(data, 'catalog.json'), 'utf8'));
const output = path.join(root, 'output');
const themes = {
  lava: { wall: 'polished_blackstone_bricks', floor: 'polished_blackstone', edge: 'chiseled_polished_blackstone', metal: 'netherite_block', light: 'shroomlight', glass: 'orange_stained_glass', accent: 'waxed_cut_copper', core: 'ochre_froglight' },
  amethyst: { wall: 'end_stone_bricks', floor: 'smooth_quartz', edge: 'purpur_pillar', metal: 'obsidian', light: 'sea_lantern', glass: 'purple_stained_glass', accent: 'amethyst_block', core: 'pearlescent_froglight' },
};
const meanings = [
  ['冷炉铸印门厅', '碎星定向门厅'], ['近场铸炉进气室', '近场天文仪室'], ['远场烟囱工场', '远场折光观测室'],
  ['双炉膛通道', '双星轨回廊'], ['铸造液压门廊', '晶面折射门廊'], ['封底熔芯输送桥', '封底星河棱镜桥'],
  ['分舱锻模工房', '晶簇祭台列室'], ['铸锭立架仓库', '末地星库'], ['双烟囱机房', '双星仪塔厅'],
  ['封闭熔芯检修井', '封闭天穹测量井'], ['分层熔芯机台', '分层晶簇观测台'], ['三向承炉柱厅', '三向星图柱厅'],
  ['大型铸件转运厅', '大型折光拱厅'], ['小型模具分流厅', '晶镜分流厅'], ['封底淬炼转向厅', '封底星盘转向厅'],
  ['中央铸造提升工场', '中央星仪升阶厅'], ['环炉烟道升阶厅', '环晶观测升阶厅'], ['螺旋熔芯烟囱', '螺旋星轨观测塔'],
  ['折角铸锭滑轨', '折角棱镜回廊'], ['铸工配给休息所', '观星者静修室'], ['铸锭终端库', '星核终端库'],
  ['封存锻模终端', '晶簇祭台终端'], ['嵌入式锻模阵列', '嵌入式晶簇祭台'], ['嵌入式铸锭架', '嵌入式末地星库'],
  ['嵌入式烟囱组', '嵌入式星仪组'], ['巨型主炉铸造王座', '完整星环晶核穹殿'], ['铸造出厂检验廊', '星门观测前廊'],
];

const signatureKeys = new Set(['spawn/spawn', 'boss/eclipse_keeper']);

function signatureRoom(variant, style) {
  const palette = themes[style];
  const [width, height, depth] = variant.size;
  const floor = variant.key === 'spawn/spawn' ? 3 : 1;
  const blocks = new Array(width * height * depth).fill('air');
  const index = (x, y, z) => (y * depth + z) * width + x;
  const inside = (x, y, z) => x >= 0 && x < width && y >= 0 && y < height && z >= 0 && z < depth;
  const get = (x, y, z) => inside(x, y, z) ? blocks[index(x, y, z)] : 'outside';
  const set = (x, y, z, block) => {
    if (!inside(x, y, z)) throw new Error(`Signature bounds ${variant.key}: ${[x, y, z]}`);
    blocks[index(x, y, z)] = block;
  };
  const box = (from, to, block) => {
    for (let y = from[1]; y <= to[1]; y++) for (let z = from[2]; z <= to[2]; z++) for (let x = from[0]; x <= to[0]; x++) set(x, y, z, block);
  };
  const ring = (centerX, centerZ, radius, y, block, gap = false) => {
    for (let x = 1; x < width - 1; x++) for (let z = 1; z < depth - 1; z++) {
      if (Math.abs(Math.hypot(x - centerX, z - centerZ) - radius) < 0.65 && !(gap && x > centerX && z < centerZ)) set(x, y, z, block);
    }
  };
  box([0, variant.minimum_y, 0], [width - 1, floor, depth - 1], palette.floor);
  box([0, variant.minimum_y, 0], [0, height - 1, depth - 1], palette.wall);
  box([width - 1, variant.minimum_y, 0], [width - 1, height - 1, depth - 1], palette.wall);
  box([0, variant.minimum_y, 0], [width - 1, height - 1, 0], palette.wall);
  box([0, variant.minimum_y, depth - 1], [width - 1, height - 1, depth - 1], palette.wall);
  box([0, height - 1, 0], [width - 1, height - 1, depth - 1], palette.wall);
  if (variant.key === 'spawn/spawn') {
    if (style === 'lava') {
      box([3, 4, 1], [12, 11, 4], palette.edge);
      box([5, 5, 4], [10, 9, 4], palette.metal);
      box([6, 6, 4], [9, 8, 4], 'tinted_glass');
      box([6, 6, 3], [9, 8, 3], 'coal_block');
      box([5, 10, 1], [10, 15, 3], palette.wall);
      box([4, 11, 1], [11, 12, 4], palette.accent);
      for (const x of [2, 11]) {
        box([x, 4, 7], [x + 2, 5, 10], palette.metal);
        box([x + 1, 5, 8], [x + 1, 5, 9], 'air');
        box([x, 6, 7], [x + 2, 6, 7], palette.accent);
        set(x + 1, 6, 8, 'redstone_lamp');
      }
      for (let z = 5; z <= 14; z++) for (const x of [5, 10]) set(x, 3, z, z % 3 === 0 ? palette.accent : palette.metal);
      box([2, 11, 7], [13, 12, 8], palette.metal);
      for (const x of [2, 13]) box([x, 4, 7], [x, 10, 8], palette.edge);
      for (const [x, z] of [[3, 12], [12, 12]]) set(x, 3, z, 'sea_lantern');
      box([6, 3, 10], [9, 3, 10], palette.accent);
      box([6, 3, 12], [9, 3, 12], palette.accent);
      set(6, 3, 11, palette.accent);
      set(9, 3, 11, palette.accent);
    } else {
      for (let x = 1; x < 15; x++) for (let z = 1; z < 15; z++) {
        if (Math.min(x, 15 - x) + Math.min(z, 15 - z) < 5) box([x, 4, z], [x, 15, z], palette.wall);
      }
      ring(7.5, 8, 4.6, 3, palette.accent, true);
      ring(7.5, 8, 5.5, 3, palette.metal, true);
      for (let x = 2; x <= 13; x++) for (let y = 5; y <= 14; y++) {
        const offsetX = x - 7.5;
        const offsetY = y - 9;
        if (Math.abs(Math.hypot(offsetX, offsetY) - 4.6) < 0.65 && !(offsetX > 1 && offsetY > 0)) box([x, y, 2], [x, y, 3], palette.accent);
      }
      for (const x of [3, 11]) box([x, 4, 2], [x + 1, 6, 4], palette.edge);
      for (let x = 5; x <= 10; x++) for (let y = 5; y <= 10; y++) for (let z = 1; z <= 4; z++) {
        const radius = Math.abs(x - 7.5) / 2.5 + Math.abs(y - 7.5) / 3 + Math.abs(z - 2) / 2;
        if (radius <= 1.15) set(x, y, z, radius < 0.5 ? palette.core : palette.accent);
      }
      for (let z = 5; z <= 14; z++) {
        set(7, 3, z, z % 3 === 0 ? palette.core : palette.accent);
        set(8, 3, z, palette.edge);
      }
      for (const [x, z] of [[3, 7], [12, 9], [5, 12]]) {
        set(x, 3, z, palette.core);
        set(x + 1, 3, z, palette.accent);
        set(x, 3, z + 1, palette.accent);
      }
    }
    box([6, 4, 5], [9, 8, 15], 'air');
    box([6, 3, 15], [9, 3, 15], palette.floor);
  } else {
    if (style === 'lava') {
      for (const x of [10, 22]) box([x, 2, 18], [x + 1, 18, 21], palette.edge);
      box([10, 8, 18], [23, 18, 21], palette.edge);
      box([12, 10, 19], [21, 15, 20], palette.light);
      box([12, 9, 18], [21, 16, 18], palette.glass);
      box([11, 17, 17], [22, 18, 21], palette.metal);
      for (const x of [13, 19]) box([x, 10, 18], [x, 15, 18], palette.metal);
      box([15, 11, 18], [18, 14, 18], palette.core);
      box([3, 2, 10], [4, 18, 12], palette.metal);
      box([3, 17, 10], [17, 18, 12], palette.accent);
      box([14, 12, 11], [16, 17, 12], palette.metal);
      box([13, 10, 10], [18, 12, 13], palette.edge);
      for (const x of [3, 27]) {
        box([x, 2, 6], [x + 3, 3, 9], palette.edge);
        box([x, 4, 6], [x + 3, 4, 8], palette.metal);
        box([x + 1, 5, 7], [x + 2, 5, 8], 'iron_block');
        box([x, 2, 15], [x + 3, 3, 17], palette.edge);
        box([x + 1, 4, 15], [x + 2, 4, 16], palette.accent);
      }
      for (let z = 2; z <= 21; z++) for (const x of [10, 23]) set(x, 1, z, z % 3 === 0 ? palette.light : palette.accent);
      for (let x = 11; x <= 22; x++) for (const z of [7, 16]) set(x, 1, z, palette.accent);
      box([12, 19, 19], [14, 21, 21], palette.metal);
      box([19, 19, 19], [21, 21, 21], palette.metal);
    } else {
      ring(16.5, 12, 8.3, 1, palette.accent);
      ring(16.5, 12, 6.2, 1, palette.edge);
      for (let x = 5; x <= 28; x++) for (let z = 2; z <= 21; z++) {
        const offsetX = Math.abs(x - 16.5);
        const offsetZ = Math.abs(z - 12);
        if (Math.abs(offsetX - offsetZ) < 0.6 || x === 16 || x === 17 || z === 12) set(x, 1, z, (x + z) % 4 === 0 ? palette.light : palette.accent);
      }
      for (const [radius, y] of [[10.2, 10], [7.5, 14], [4.5, 18]]) {
        ring(16.5, 12, radius, y, palette.accent);
        ring(16.5, 12, radius, y + 1, palette.edge);
      }
      for (let direction = 0; direction < 8; direction++) {
        const angle = direction * Math.PI / 4;
        for (let y = 5; y <= 21; y++) {
          const radius = 12.5 - (y - 5) * 0.64;
          const x = Math.round(16.5 + Math.cos(angle) * radius);
          const z = Math.max(1, Math.min(21, Math.round(12 + Math.sin(angle) * radius)));
          box([x, y, z], [Math.min(32, x + 1), y, z], y % 5 === 0 ? palette.light : palette.edge);
        }
      }
      box([5, 8, 21], [28, 20, 21], palette.metal);
      for (let x = 6; x <= 27; x++) for (let y = 9; y <= 19; y++) {
        const offsetX = Math.abs(x - 16.5);
        const offsetY = Math.abs(y - 14);
        if (Math.abs(offsetX - offsetY * 1.7) < 0.9 || offsetY === 0 || offsetX < 0.6) set(x, y, 20, palette.accent);
        if ((x * 11 + y * 7) % 31 === 0) set(x, y, 20, palette.light);
      }
      for (let x = 13; x <= 20; x++) for (let y = 8; y <= 18; y++) for (let z = 16; z <= 20; z++) {
        const radius = Math.abs(x - 16.5) / 3.5 + Math.abs(y - 13) / 5 + Math.abs(z - 18) / 2;
        if (radius <= 1.25) set(x, y, z, radius < 0.65 ? palette.core : radius < 0.9 ? palette.glass : palette.accent);
      }
      for (let y = 17; y <= 21; y++) box([16, y, 18 - Math.floor((y - 17) / 2)], [17, y, 19 - Math.floor((y - 17) / 2)], palette.edge);
      for (const x of [4, 28]) for (const z of [3, 18]) {
        box([x, 2, z], [x + 1, 5, z + 2], palette.edge);
        box([x + 1, 5, z], [x + 2, 7, z + 2], palette.accent);
      }
    }
    box([15, 2, 1], [18, 7, 21], 'air');
    box([10, 2, 7], [23, 7, 16], 'air');
    box([0, 2, 22], [33, 21, 22], palette.wall);
    box([15, 2, 22], [18, 7, 26], 'air');
    box([15, 1, 22], [18, 1, 26], palette.floor);
    for (const socket of variant.sockets) {
      const [centerX, feet, centerZ] = socket.position;
      box([centerX - 1, feet, centerZ - 1], [centerX + 1, feet + 2, centerZ + 1], 'air');
      box([centerX - 1, feet - 1, centerZ - 1], [centerX + 1, feet - 1, centerZ + 1], palette.floor);
    }
  }
  const structure = new Structure(variant.size);
  for (let y = 0; y < height; y++) for (let z = 0; z < depth; z++) for (let x = 0; x < width; x++) structure.addBlock([x, y, z], `minecraft:${get(x, y, z)}`);
  return { structure, blocks, get, index, raised: false, floor, signatureRedesign: true };
}

function decorate({ variant, style, palette, width, height, depth, floor, box, set }) {
  const semantic = variant.key.split('/').at(-1);
  const seed = catalog.variants.findIndex(item => item.key === variant.key);
  if (style === 'lava') {
    const stations = semantic.includes('storage') ? [3, 6, 9, 12] : semantic.includes('cells') ? [3, 7, 11] : [3 + seed % 2, depth - 4];
    for (const z of stations.filter(value => value < depth - 2)) for (const x of [2, width - 3]) {
      const top = Math.min(height - 3, semantic.includes('towers') ? height - 4 : floor + 5 + seed % 3);
      box([x - 1, floor + 1, z - 1], [x + 1, top, z + 1], palette.edge);
      box([x, floor + 2, z], [x, top - 1, z], palette.light);
      set(x, floor + 2, z + (x < width / 2 ? 1 : -1), palette.glass);
      box([x - 1, top + 1, z - 1], [x + 1, Math.min(height - 2, top + 2), z + 1], palette.metal);
    }
    for (let z = 2 + seed % 3; z < depth - 2; z += 4) {
      box([1, height - 4, z], [width - 2, height - 3, z], palette.edge);
      for (const x of [2, width - 3]) box([x, floor + 1, z], [x, height - 3, z], palette.metal);
    }
    for (let z = 1; z < depth - 1; z++) for (const x of [5, width - 6]) set(x, floor, z, z % 3 === 0 ? palette.light : palette.accent, true);
  } else {
    const centerX = (width - 1) / 2;
    const centerZ = (depth - 1) / 2;
    const radius = Math.floor(Math.min(width, depth) / 2) - 2;
    for (let z = 1; z < depth - 1; z++) for (let x = 1; x < width - 1; x++) {
      if (Math.abs(x - centerX) + Math.abs(z - centerZ) > width / 2 + depth / 2 - 6) box([x, floor + 1, z], [x, height - 2, z], palette.edge);
      if (Math.abs(Math.hypot(x - centerX, z - centerZ) - radius) < 0.7) {
        set(x, floor, z, palette.accent, true);
        set(x, height - 3 - seed % 2, z, palette.accent);
        if ((x + z) % 3 === 0) set(x, height - 2, z, palette.light);
      }
    }
    const spokes = semantic.includes('storage') || semantic.includes('cells') ? [3, 6, 9, 12] : [3, depth - 4];
    for (const z of spokes.filter(value => value < depth - 2)) for (const x of [2, width - 3]) {
      box([x, floor + 1, z], [x, height - 4, z], palette.edge);
      for (let offset = -1; offset <= 1; offset++) set(x + offset, floor + 3 + Math.abs(offset), z, palette.accent);
      set(x, floor + 2, z + (x < width / 2 ? 1 : -1), palette.core);
    }
    for (let x = 2; x < width - 2; x++) if (x % 3 === seed % 3) box([x, height - 2, 1], [x, height - 2, depth - 2], palette.glass);
  }
  if (semantic === 'canteen') {
    for (const x of [3, width - 4]) {
      box([x, floor + 1, 3], [x, floor + 2, depth - 4], style === 'lava' ? palette.accent : palette.metal);
      for (const z of [3, depth - 4]) box([x - 1, floor + 1, z], [x + 1, floor + 1, z], palette.edge);
    }
  }
  if (semantic.includes('door')) {
    const portalHeight = semantic === 'big_doors' ? height - 5 : floor + 6;
    for (const x of [4, width - 5]) box([x, floor + 1, 6], [x, portalHeight, 7], palette.edge);
    box([4, portalHeight, 6], [width - 5, portalHeight + 1, 7], style === 'lava' ? palette.metal : palette.accent);
  }
  if (semantic === 'bridge') {
    for (const x of [3, width - 4]) box([x, floor + 1, 2], [x, floor + 3, depth - 3], palette.edge);
    box([6, floor, 2], [9, floor, depth - 3], palette.glass, true);
  }
  if (semantic === 'hole' && !variant.key.startsWith('stairs/')) {
    box([6, floor, 6], [9, floor, 9], palette.glass, true);
    box([6, height - 5, 6], [9, height - 3, 9], palette.light);
  }
  if (semantic === 'promenade') {
    box([3, floor + 1, depth - 4], [4, height - 5, depth - 3], palette.edge);
    box([width - 5, floor + 1, depth - 4], [width - 4, height - 5, depth - 3], palette.edge);
    box([3, height - 5, depth - 4], [width - 4, height - 4, depth - 3], palette.accent);
  }
  if (variant.key === 'boss/eclipse_keeper') {
    if (style === 'lava') {
      box([12, 2, 4], [21, 6, 8], palette.edge);
      box([14, 3, 5], [19, 5, 7], palette.light);
      for (const x of [12, 21]) box([x, 6, 5], [x + 1, 19, 7], palette.metal);
      box([13, 12, 5], [20, 14, 7], palette.accent);
    } else {
      for (let z = 5; z <= 17; z++) for (let x = 10; x <= 23; x++) if (Math.abs(Math.hypot((x - 16.5) / 1.1, z - 11) - 6) < 0.7) box([x, 11, z], [x, 12, z], palette.accent);
      box([15, 2, 5], [18, 3, 7], palette.metal);
      box([16, 4, 6], [17, 6, 6], palette.core);
    }
    box([14, 2, 22], [19, 8, 26], palette.edge, true);
    box([15, 2, 22], [18, 7, 26], 'air', true);
    box([15, 1, 22], [18, 1, 26], palette.floor, true);
    box([15, 2, 19], [18, 7, 22], 'air', true);
  }
}

function validate(variant, room) {
  const failures = [];
  const { get, index } = room;
  const [width, height, depth] = variant.size;
  const air = (x, y, z) => get(x, y, z) === 'air';
  const solid = (x, y, z) => !['air', 'outside'].includes(get(x, y, z));
  const full = (x, y, z) => solid(x, y, z) && !/_stairs$|_slab$/.test(get(x, y, z)?.name || '');
  const stand = (x, y, z) => air(x, y, z) && air(x, y + 1, z) && solid(x, y - 1, z);
  const check = (condition, label) => { if (!condition) failures.push(label); };
  const endpoints = variant.ports.flatMap(port => port.cells);
  if (variant.key === 'spawn/spawn') endpoints.push([7, 4, 8]);
  if (variant.key === 'promenade') endpoints.push([7, 2, 11]);
  if (variant.key === 'boss/eclipse_keeper') endpoints.push([16, 2, 2], [16, 2, 25], [16, 2, 13]);
  for (const socket of variant.sockets) endpoints.push(socket.position, socket.spawn_position);
  if (!endpoints.length) endpoints.push([Math.floor(width / 2), room.floor + 1, Math.floor(depth / 2)]);
  for (const port of variant.ports) for (const [x, y, z] of port.cells) {
    check(solid(x, y - 1, z), `port support ${[x, y, z]}`);
    for (let rise = 0; rise < 5; rise++) check(air(x, y + rise, z), `port clearance ${[x, y + rise, z]}`);
  }
  const queue = [endpoints[0]];
  const reached = new Set([index(...endpoints[0])]);
  for (let head = 0; head < queue.length; head++) {
    const [x, y, z] = queue[head];
    for (const [offsetX, offsetZ] of [[1, 0], [-1, 0], [0, 1], [0, -1]]) for (const rise of [0, 1, -1]) {
      const next = [x + offsetX, y + rise, z + offsetZ];
      if (next[0] < 0 || next[0] >= width || next[2] < 0 || next[2] >= depth || next[1] < 1 || next[1] + 1 >= height) continue;
      const nextIndex = index(...next);
      if (!reached.has(nextIndex) && stand(...next) && (rise !== 1 || air(x, y + 2, z)) && (rise !== -1 || air(next[0], y + 1, next[2]))) { reached.add(nextIndex); queue.push(next); }
    }
  }
  for (const position of endpoints) check(stand(...position) && reached.has(index(...position)), `unreachable ${position}`);
  const reservedFeet = new Set(variant.sockets.flatMap(socket => [socket.position.join(','), socket.spawn_position.join(',')]));
  for (const socket of variant.sockets) {
    const [x, y, z] = socket.position;
    check(solid(...socket.support), `socket support ${socket.id}`);
    for (let rise = 0; rise < 3; rise++) check(air(x, y + rise, z), `socket air ${socket.id}`);
    let candidates = 0;
    for (let offsetX = -6; offsetX <= 6; offsetX++) for (let offsetZ = -6; offsetZ <= 6; offsetZ++) {
      const distance = Math.abs(offsetX) + Math.abs(offsetZ);
      if (distance >= 1 && distance <= 6 && !reservedFeet.has([x + offsetX, y, z + offsetZ].join(',')) && full(x + offsetX, y - 1, z + offsetZ) && stand(x + offsetX, y, z + offsetZ) && air(x + offsetX, y + 2, z + offsetZ) && reached.has(index(x + offsetX, y, z + offsetZ))) candidates++;
    }
    check(candidates > 0, `no independent mob feet ${socket.id}`);
  }
  const portAir = new Set(variant.ports.flatMap(port => port.cells.flatMap(([x, y, z]) => Array.from({ length: 5 }, (_, rise) => index(x, y + rise, z)))));
  for (let y = variant.minimum_y; y < height; y++) for (let z = 0; z < depth; z++) for (let x = 0; x < width; x++) if (y === height - 1 || x === 0 || x === width - 1 || z === 0 || z === depth - 1) check(!air(x, y, z) || portAir.has(index(x, y, z)), `shell opening ${[x, y, z]}`);
  let signatureSafety;
  if (room.signatureRedesign) {
    const feet = room.floor + 1;
    const flatReach = (start, wide = false, gateClosed = false) => {
      const open = (x, z) => {
        for (let offsetX = wide ? -1 : 0; offsetX <= (wide ? 1 : 0); offsetX++) for (let offsetZ = wide ? -1 : 0; offsetZ <= (wide ? 1 : 0); offsetZ++) {
          const nextX = x + offsetX;
          const nextZ = z + offsetZ;
          if (gateClosed && nextZ === 22 && nextX >= 15 && nextX <= 18) return false;
          if (!full(nextX, feet - 1, nextZ)) return false;
          for (let rise = 0; rise < 3; rise++) if (!air(nextX, feet + rise, nextZ)) return false;
        }
        return true;
      };
      const pending = [start];
      const visited = new Set([start.join(',')]);
      for (let head = 0; head < pending.length; head++) {
        const [x, z] = pending[head];
        for (const [offsetX, offsetZ] of [[1, 0], [-1, 0], [0, 1], [0, -1]]) {
          const next = [x + offsetX, z + offsetZ];
          if (!visited.has(next.join(',')) && open(...next)) { visited.add(next.join(',')); pending.push(next); }
        }
      }
      return visited;
    };
    const spawn = variant.key === 'spawn/spawn';
    const start = spawn ? [7, 8] : [16, 2];
    const flat = flatReach(start);
    for (const [x, y, z] of endpoints) {
      check(y === feet && flat.has([x, z].join(',')), `flat walking ${[x, y, z]}`);
      for (let rise = 0; rise < 3; rise++) check(air(x, y + rise, z), `fixed feet clearance ${[x, y + rise, z]}`);
    }
    const wide = flatReach(start, true);
    const wideTarget = spawn ? [7, 13] : [16, 25];
    check(wide.has(wideTarget.join(',')), 'three-wide main route');
    if (!spawn) {
      for (let x = 0; x < width; x++) for (let y = 2; y < height - 1; y++) {
        const gate = x >= 15 && x <= 18 && y <= 7;
        check(gate ? air(x, y, 22) : full(x, y, 22), `barrier wall ${[x, y, 22]}`);
      }
      check(!flatReach(start, false, true).has('16,25'), 'barrier bypass');
      for (let x = 10; x <= 23; x++) for (let z = 7; z <= 16; z++) for (let y = 2; y <= 7; y++) check(air(x, y, z), `battle clearance ${[x, y, z]}`);
    }
    signatureSafety = { flatWalking: true, threeWideMainRoute: true, fixedFeetAir: 3, barrierCannotBypass: !spawn, centralBattleClearCuboid: spawn ? null : [[10, 2, 7], [23, 7, 16]], flatReachableFeet: flat.size };
  }
  if (failures.length) throw new Error(`${variant.key}: ${failures.slice(0, 20).join('; ')} (${failures.length})`);
  return { key: variant.key, size: variant.size, minimum_y: variant.minimum_y, ports: variant.ports.length, sockets: variant.sockets.length, reachableFeet: reached.size, portAirHeight: 5, socketAirHeight: 3, independentMobFeet: true, sealed: true, reusedTraversalSkeleton: room.raised, ...(signatureSafety ? { signatureSafety } : {}) };
}

function buildRoom(variant, style, original) {
  if (signatureKeys.has(variant.key)) return signatureRoom(variant, style);
  const palette = themes[style];
  const [width, height, depth] = variant.size;
  const blocks = new Array(width * height * depth).fill('air');
  const protectedCells = new Set();
  const index = (x, y, z) => (y * depth + z) * width + x;
  const inside = (x, y, z) => x >= 0 && x < width && y >= 0 && y < height && z >= 0 && z < depth;
  const get = (x, y, z) => inside(x, y, z) ? blocks[index(x, y, z)] : 'outside';
  const set = (x, y, z, block, force = false) => {
    if (!inside(x, y, z)) throw new Error(`Outside ${variant.key}: ${[x, y, z]}`);
    if (force || !protectedCells.has(index(x, y, z))) blocks[index(x, y, z)] = block;
  };
  const box = (from, to, block, force = false) => {
    for (let y = from[1]; y <= to[1]; y++) for (let z = from[2]; z <= to[2]; z++) for (let x = from[0]; x <= to[0]; x++) set(x, y, z, block, force);
  };
  const floor = variant.key === 'spawn/spawn' ? 3 : variant.key.includes('/layout/') ? 0 : 1;
  box([0, variant.minimum_y, 0], [width - 1, floor, depth - 1], palette.floor);
  const raised = variant.key.startsWith('stairs/') || variant.key === 'straight/platform';
  if (raised) {
    for (const block of original.getBlocks()) {
      const name = block.state.getName().toString().replace('minecraft:', '');
      if (/air|glass|chain|lantern|iron_bars|amethyst|copper/.test(name)) continue;
      const material = name.endsWith('_stairs') ? `${style === 'lava' ? 'polished_blackstone_brick' : 'purpur'}_stairs` : name.endsWith('_slab') ? `${style === 'lava' ? 'polished_blackstone_brick' : 'purpur'}_slab` : palette.floor;
      set(...block.pos, { name: material, properties: name.endsWith('_stairs') || name.endsWith('_slab') ? block.state.getProperties() : undefined }, true);
    }
    for (let y = 1; y < height - 3; y++) for (let z = 1; z < depth - 1; z++) for (let x = 1; x < width - 1; x++) {
      if (get(x, y - 1, z) !== 'air' && get(x, y, z) === 'air' && get(x, y + 1, z) === 'air') {
        for (let rise = 0; rise < 3; rise++) if (get(x, y + rise, z) === 'air') protectedCells.add(index(x, y + rise, z));
      }
    }
  }
  const reserve = (position, radius = 0, clearance = 3) => {
    const [centerX, feet, centerZ] = position;
    for (let z = Math.max(1, centerZ - radius); z <= Math.min(depth - 2, centerZ + radius); z++) for (let x = Math.max(1, centerX - radius); x <= Math.min(width - 2, centerX + radius); x++) {
      set(x, feet - 1, z, palette.floor, true);
      for (let rise = 0; rise < clearance; rise++) {
        set(x, feet + rise, z, 'air', true);
        protectedCells.add(index(x, feet + rise, z));
      }
    }
  };
  if (!raised) {
    for (let z = 1; z < depth - 1; z++) reserve([Math.floor(width / 2), floor + 1, z], 1);
    for (let x = 1; x < width - 1; x++) reserve([x, floor + 1, Math.floor(depth / 2)], 1);
    for (const socket of variant.sockets) {
      const [socketX, feet, socketZ] = socket.position;
      for (let x = Math.min(socketX, Math.floor(width / 2)); x <= Math.max(socketX, Math.floor(width / 2)); x++) reserve([x, feet, socketZ], 1);
    }
  }
  for (const socket of variant.sockets) { reserve(socket.position, raised ? 0 : 1); reserve(socket.spawn_position); set(...socket.support, palette.floor, true); }
  if (variant.key === 'spawn/spawn') reserve([7, 4, 8], 1);
  if (variant.key === 'promenade') reserve([7, 2, 11], 1);
  box([0, variant.minimum_y, 0], [0, height - 1, depth - 1], palette.wall, true);
  box([width - 1, variant.minimum_y, 0], [width - 1, height - 1, depth - 1], palette.wall, true);
  box([0, variant.minimum_y, 0], [width - 1, height - 1, 0], palette.wall, true);
  box([0, variant.minimum_y, depth - 1], [width - 1, height - 1, depth - 1], palette.wall, true);
  box([0, height - 1, 0], [width - 1, height - 1, depth - 1], palette.wall, true);
  decorate({ variant, style, palette, width, height, depth, floor, box, set });
  for (const port of variant.ports) for (const [x, feet, z] of port.cells) {
    const inwardX = x === 0 ? 1 : x === width - 1 ? -1 : 0;
    const inwardZ = z === 0 ? 1 : z === depth - 1 ? -1 : 0;
    for (let step = 0; step <= 2; step++) {
      set(x + inwardX * step, feet - 1, z + inwardZ * step, palette.floor, true);
      for (let rise = 0; rise < 5; rise++) set(x + inwardX * step, feet + rise, z + inwardZ * step, 'air', true);
    }
  }
  const structure = new Structure(variant.size);
  for (let y = 0; y < height; y++) for (let z = 0; z < depth; z++) for (let x = 0; x < width; x++) {
    const block = get(x, y, z);
    if (typeof block === 'string') structure.addBlock([x, y, z], `minecraft:${block}`);
    else structure.addBlock([x, y, z], `minecraft:${block.name}`, block.properties);
  }
  return { structure, blocks, get, index, raised, floor };
}

async function main() {
  await fs.mkdir(output, { recursive: true });
  const results = {};
  const masks = {};
  const originals = {};
  const baselinePath = path.join(output, 'rooms-team4-redesign-before.json');
  const baseline = await fs.readFile(baselinePath, 'utf8').then(JSON.parse).catch(() => null);
  const redesign = { schema: 1, changedTemplates: [], preservedTemplates: [], manifests: {} };
  let paletteEntries = 0;
  for (const variant of catalog.variants) {
    const bytes = await fs.readFile(path.join(data, 'structures', `${variant.key}.nbt`));
    if (crypto.createHash('sha256').update(bytes).digest('hex') !== variant.sha256) throw new Error(`Legacy hash mismatch: ${variant.key}`);
    originals[variant.key] = Structure.fromNbt(NbtFile.read(bytes).root);
  }
  for (const style of Object.keys(themes)) {
    const templates = [];
    const manifestPath = path.join(data, 'roomsets', style, 'manifest.json');
    const previousManifest = await fs.readFile(manifestPath).catch(() => null);
    results[style] = [];
    masks[style] = {};
    for (const variant of catalog.variants) {
      const room = buildRoom(variant, style, originals[variant.key]);
      const checks = validate(variant, room);
      const bytes = room.structure.writeNbt({ name: '', dataVersion: 3465, compression: 'gzip' });
      const nbtRoot = NbtFile.read(bytes).root;
      if (nbtRoot.getNumber('DataVersion') !== 3465 || nbtRoot.getList('entities').length !== 0) throw new Error('NBT header/entities failed');
      for (const entry of nbtRoot.getList('palette').getItems()) {
        if (!/^minecraft:[a-z0-9_]+$/.test(entry.getString('Name'))) throw new Error(`Invalid palette.Name: ${entry.getString('Name')}`);
        paletteEntries++;
      }
      const roundtrip = Structure.fromNbt(nbtRoot);
      if (roundtrip.getBlocks().length !== variant.size.reduce((total, size) => total * size, 1) || roundtrip.getBlocks().some(block => block.nbt)) throw new Error('Dense NBT roundtrip failed');
      const destination = path.join(data, 'roomsets', style, `${variant.key}.nbt`);
      const previous = await fs.readFile(destination).catch(() => null);
      const digest = crypto.createHash('sha256').update(bytes).digest('hex');
      if (!signatureKeys.has(variant.key)) {
        if (previous && !previous.equals(bytes)) throw new Error(`Unowned template drift ${style}/${variant.key}`);
        if (baseline && digest !== baseline.styles[style].templates[variant.key].sha256) throw new Error(`Baseline mismatch ${style}/${variant.key}`);
        redesign.preservedTemplates.push({ style, key: variant.key, sha256: digest });
      } else {
        const before = baseline?.styles[style].templates[variant.key];
        const mask = before?.occupancyBase64 ? Buffer.from(before.occupancyBase64, 'base64') : null;
        const differingSolidAirCells = mask ? room.blocks.reduce((total, block, offset) => total + ((block === 'air' ? 0 : 1) !== mask[offset] ? 1 : 0), 0) : null;
        if (mask && differingSolidAirCells === 0) throw new Error('Signature geometry unchanged');
        redesign.changedTemplates.push({ style, key: variant.key, oldSha256: before?.sha256, newSha256: digest, differingSolidAirCells, ...checks });
      }
      await fs.mkdir(path.dirname(destination), { recursive: true });
      if (!previous || !previous.equals(bytes)) await fs.writeFile(destination, bytes);
      templates.push({ key: variant.key, sha256: digest });
      masks[style][variant.key] = room.blocks.map(block => block === 'air' ? '0' : '1').join('');
      let differsFromOriginal = 0;
      for (const block of room.structure.getBlocks()) {
        const previous = originals[variant.key].getBlock(block.pos);
        if ((block.state.getName().toString() === 'minecraft:air') !== (!previous || previous.state.getName().toString() === 'minecraft:air')) differsFromOriginal++;
      }
      results[style].push({ ...checks, bytes: bytes.length, denseVolume: room.blocks.length, dataVersion: 3465, entities: 0, blockEntities: 0, differsFromOriginal, semantic: meanings[catalog.variants.indexOf(variant)][style === 'lava' ? 0 : 1] });
      process.stdout.write(`${style} ${variant.key} OK\n`);
    }
    const manifestBytes = Buffer.from(`${JSON.stringify({ schema: 1, style: `fteclipsegen:${style}`, templates }, null, 2)}\n`);
    if (!previousManifest || !previousManifest.equals(manifestBytes)) await fs.writeFile(manifestPath, manifestBytes);
    redesign.manifests[style] = { sha256: crypto.createHash('sha256').update(manifestBytes).digest('hex'), stableAgainstPreviousRun: Boolean(previousManifest?.equals(manifestBytes)) };
    if (results[style].filter(item => item.differsFromOriginal > 0).length < 20) throw new Error('Insufficient independent room geometry');
  }
  const geometry = catalog.variants.map(variant => ({ key: variant.key, differingSolidAirCells: [...masks.lava[variant.key]].reduce((total, cell, offset) => total + (cell !== masks.amethyst[variant.key][offset] ? 1 : 0), 0) }));
  if (geometry.some(item => item.differingSolidAirCells === 0)) throw new Error('Identical theme geometry');
  const signatures = Object.fromEntries(Object.keys(themes).map(style => [style, crypto.createHash('sha256').update(Object.values(masks[style]).join('')).digest('hex')]));
  await fs.writeFile(path.join(output, 'rooms-team4-redesign-checks.json'), `${JSON.stringify(redesign, null, 2)}\n`);
  await fs.writeFile(path.join(output, 'rooms-team4-resource-audit.json'), `${JSON.stringify({ templates: 54, paletteEntries, invalidNames: [], stableManifests: redesign.manifests }, null, 2)}\n`);
  const redesignRows = redesign.changedTemplates.map(item => `| ${item.style} | ${item.key} | ${item.differingSolidAirCells} | ${item.signatureSafety.flatReachableFeet} |`);
  await fs.writeFile(path.join(output, 'rooms-team4-redesign.md'), `# 初始房 / Boss房配套重设计\n\n只变更 lava、amethyst 的 spawn/spawn、boss/eclipse_keeper，共4份NBT。其余50份与 rooms-team4-redesign-before.json 的SHA256逐项一致；生成器遇到非目标模板字节变化会立即失败。原始模板、history、Java、版本、mods均未修改。\n\n熔火：初始冷炉为厚壁铸印炉、黑色未激活炉芯、凹槽铸模及铜色铸印轴线；Boss为巨型封闭发光炉胆、炉顶烟道、横跨式吊臂/锤头及双侧锻台，中央14×10地面战斗区保留六格净空，冷炉铜色门印呼应主炉战斗区铸造轨。\n\n紫晶：初始为切角碎星门厅，带残缺竖向星环、棱镜标记和不完整地面星轨；Boss将其补全为三层悬环、八向拱肋穹顶、巨大封闭晶核和星图后壁。两房共享晶核与仪式轴线，轮廓和空间规模不同。\n\n| style | key | 相对本次改造前的实心/AIR差异 | 同层可步行脚点 |\n|---|---|---|---|\n${redesignRows.join('\n')}\n\n四房均从空体积独立建筑，无楼梯或半砖；验证为同层实际步行图，不依赖跳跃。三格宽主路采用3×3脚点邻域、完整支撑及三格净空检查。初始端口五格AIR；Boss8个socket原位及独立怪物脚点、固定入口/Boss/返程点全部安全。Boss全宽隔墙z22只有规定门洞，关闭该门后返程点不可从战斗区到达。无真岩浆、危险地面、重力块或方块实体。\n\n剖视图 rooms-team4-preview.svg / rooms-team4-preview.png 直接来自最终NBT；仅为展示隐藏外墙高段、屏障隔墙高段及顶盖。完整资源外壳保持封闭。\n\nmanifest SHA256:\n- lava: ${redesign.manifests.lava.sha256}\n- amethyst: ${redesign.manifests.amethyst.sha256}\n\n重现：node tools/rooms-team4.mjs。仅重做预览：node tools/rooms-team4.mjs --preview-only。未执行Gradle。\n`);
  await fs.writeFile(path.join(output, 'rooms-team4-checks.json'), `${JSON.stringify({ schema: 1, traversalModel: 'Orthogonal feet graph, 2-block body, +/-1 step and swept headroom. Stairs/slabs are a voxel approximation; Minecraft playtest required.', signatures, results, geometry }, null, 2)}\n`);
  const rows = catalog.variants.map((variant, offset) => `| ${variant.key} | ${meanings[offset].join(' | ')} | ${geometry[offset].differingSolidAirCells} |`);
  await fs.writeFile(path.join(output, 'rooms-team4-semantics.md'), `# 熔火 / 紫晶完整房间包\n\n每种27项，Lodestone 0.6.0 配方 tools/rooms-team4.mjs，运行 node tools/rooms-team4.mjs。\n\n| key | 熔火语义 | 紫晶语义 | 实心/空气差异坐标数 |\n|---|---|---|---|\n${rows.join('\n')}\n\n熔火采用矩形厚壁炉膛、纵向铜色输送轨、烟道、横向承重梁和巨炉。紫晶采用切角墙体、圆星轨、晶簇柱、折光顶带和椭圆星环。几何差异按实心/空气掩码统计。\n\nstairs/centered、stairs/hole、stairs/spiral、straight/platform 明确复用原石质通行骨架、楼梯/半砖朝向；删除原玻璃、链条、灯笼后重新建筑。其他23种从空体积重建。无实体或方块实体，所有坐标含AIR，DataVersion=3465。\n\n检查覆盖五格端口AIR及支撑、三格socket AIR及support、spawn_position、入口[7,4,8]、promenade[7,2,11]、同高1..6格独立怪物脚点及门连通、密闭墙顶、密集NBT回读。Boss门 x15..18/y2..7/z22 留空，入口[16,2,2]、返程[16,2,25]、Boss[16,2,13]安全连通。\n\n限制：整数脚点图对楼梯/半砖为体素近似，需主线程进行游戏内移动验证。未执行Gradle、运行时接入或修改旧catalog/styles/structures。无岩浆、火焰、岩浆块、重力块、依赖附着的水晶装饰。\n`);
}

async function previews() {
  const selected = ['spawn/spawn', 'boss/eclipse_keeper'];
  const panels = [];
  for (let row = 0; row < selected.length; row++) for (const [column, style] of ['lava', 'amethyst'].entries()) {
    const key = selected[row];
    const variant = catalog.variants.find(item => item.key === key);
    const structure = Structure.fromNbt(NbtFile.read(await fs.readFile(path.join(data, 'roomsets', style, `${key}.nbt`))).root);
    const [width, height, depth] = variant.size;
    const unit = Math.min(18, 700 / (width + depth), 400 / (height + (width + depth) / 2));
    const reverse = key === 'boss/eclipse_keeper';
    const project = (x, y, z) => {
      const viewZ = reverse ? depth - z : z;
      return [440 + (x - viewZ - (width - depth) / 2) * unit, 416 + (x + viewZ - (width + depth) / 2) * unit / 2 - y * unit];
    };
    const cells = new Map();
    for (const block of structure.getBlocks()) {
      const [x, y, z] = block.pos;
      const name = block.state.getName().toString().replace('minecraft:', '');
      if (name === 'air' || y === height - 1 || (y > 2 && (x === 0 || z === 0 || x === width - 1 || z === depth - 1)) || (key === 'boss/eclipse_keeper' && z === 22 && y > 7)) continue;
      if (!reverse && y > 4 && Math.min(x, 15 - x) + Math.min(z, 15 - z) < 5) continue;
      cells.set(block.pos.join(','), { x, y, z, name });
    }
    const shades = name => {
      if (/light|lantern/.test(name)) return style === 'lava' ? ['#ffe8ac', '#e5aa61', '#bf7334'] : ['#ebeeff', '#bcb8e6', '#a49bd4'];
      if (/copper/.test(name)) return ['#db8c50', '#ad5d30', '#854020'];
      if (/glass/.test(name)) return style === 'lava' ? ['#e99235', '#a95a24', '#7b4226'] : ['#a88ad0', '#765b9b', '#594175'];
      if (/amethyst/.test(name)) return ['#c59be8', '#9869bd', '#6f4395'];
      if (/purpur/.test(name)) return ['#b59bbf', '#8b719b', '#685475'];
      if (/quartz|end_stone/.test(name)) return ['#e5dfce', '#b9b2a0', '#99917f'];
      if (/obsidian|netherite/.test(name)) return ['#585060', '#39333f', '#29232e'];
      return ['#68616b', '#48414d', '#332f39'];
    };
    const polygon = (points, color) => `<polygon points="${points.map(point => project(...point).map(value => value.toFixed(1)).join(',')).join(' ')}" fill="${color}" stroke="#131520" stroke-width="0.22"/>`;
    const shapes = [];
    for (const { x, y, z, name } of [...cells.values()].sort((first, second) => first.x + (reverse ? -first.z : first.z) - second.x - (reverse ? -second.z : second.z) || first.y - second.y)) {
      const colors = shades(name);
      if (!cells.has([x + 1, y, z].join(','))) shapes.push(polygon([[x + 1, y, z], [x + 1, y, z + 1], [x + 1, y + 1, z + 1], [x + 1, y + 1, z]], colors[1]));
      const faceZ = reverse ? z : z + 1;
      if (!cells.has([x, y, z + (reverse ? -1 : 1)].join(','))) shapes.push(polygon([[x, y, faceZ], [x + 1, y, faceZ], [x + 1, y + 1, faceZ], [x, y + 1, faceZ]], colors[2]));
      if (!cells.has([x, y + 1, z].join(','))) shapes.push(polygon([[x, y + 1, z], [x + 1, y + 1, z], [x + 1, y + 1, z + 1], [x, y + 1, z + 1]], colors[0]));
    }
    const title = meanings[catalog.variants.indexOf(variant)][column];
    panels.push(`<g transform="translate(${column * 900},${row * 570 + 100})"><rect x="12" y="10" width="876" height="548" rx="18" fill="#181d2a" stroke="#30384c"/><text x="40" y="48" fill="${style === 'lava' ? '#f6b46c' : '#cfb4ff'}" font-size="24">${title}</text><text x="40" y="76" fill="#9ca8be" font-size="15">${style} / ${key} · ${variant.size.join(' × ')}</text>${shapes.join('')}</g>`);
  }
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="1800" height="1840" viewBox="0 0 1800 1840"><rect width="1800" height="1840" fill="#0c101a"/><g font-family="Segoe UI,Microsoft YaHei,sans-serif"><text x="36" y="43" fill="#f1f3fa" font-size="28">熔火工场 / 紫晶星库 — 实际NBT几何剖视</text><text x="36" y="77" fill="#a1acc2" font-size="17">为展示内部移除外墙高段与最顶层；楼梯/半砖以整格显示。颜色为主题示意，非材质渲染。</text>${panels.join('')}<text x="36" y="1830" fill="#98a3b8" font-size="15">54 dense templates · DataVersion 3465 · 5-high ports · 174 safe sockets · generated directly from final NBT</text></g></svg>`;
  const signatureSvg = svg.replaceAll('1840', '1270').replaceAll('1830', '1250').replace('与最顶层', '、屏障隔墙高段与顶盖').replace('楼梯/半砖以整格显示。', '实际NBT外壳保持封闭。');
  await fs.writeFile(path.join(output, 'rooms-team4-preview.svg'), signatureSvg);
}

if (process.argv[1] && path.resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  if (!process.argv.includes('--preview-only')) await main();
  await previews();
}

export default function recipe(api) {
  const variant = catalog.variants.find(item => item.key === (process.env.ROOM_KEY || 'straight/simple'));
  const style = process.env.ROOM_STYLE || 'lava';
  if (variant.key.startsWith('stairs/') || variant.key === 'straight/platform') throw new Error('Use batch CLI for skeleton rooms');
  return { structure: buildRoom(variant, style, null).structure, metadata: { name: `rooms-team4-${style}-${variant.key}` } };
}
