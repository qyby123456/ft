import assert from 'node:assert/strict';
import { createHash } from 'node:crypto';
import { existsSync, mkdirSync, readFileSync, writeFileSync } from 'node:fs';
import { dirname, resolve, join } from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';
import { spawnSync } from 'node:child_process';

const root = fileURLToPath(new URL('../', import.meta.url));
const skill = process.env.MINECRAFT_BUILDER_SKILL ?? 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill';
const resources = resolve(root, 'src/main/resources/data/fteclipsegen');
const output = resolve(root, 'output/rooms-team1');
const catalog = JSON.parse(readFileSync(join(resources, 'catalog.json'), 'utf8'));
const styles = ['clockwork', 'forest'];
const palettes = {
  clockwork: { wall: 'deepslate_tiles', floor: 'polished_andesite', trim: 'waxed_cut_copper', pillar: 'iron_block', light: 'sea_lantern', stair: 'stone_brick_stairs' },
  forest: { wall: 'mossy_stone_bricks', floor: 'mossy_cobblestone', trim: 'dark_oak_planks', pillar: 'oak_log', light: 'shroomlight', stair: 'dark_oak_stairs' },
};
const hash = bytes => createHash('sha256').update(bytes).digest('hex');
const air = block => block === 'air' || block === 'minecraft:air';
const blockName = block => block.replace(/^minecraft:/, '').split('[')[0];
const featureKeys = new Set(['spawn/spawn', 'boss/eclipse_keeper']);
const priorManifests = { clockwork: '9ef7bfd0036552c836715c34f5939a9d0c2a6c7a31190a1498f30f700d735c93', forest: 'd020a9735111934ee6cbbfff91f2f75e1ca8707a3c57343a8661671d9209dc52' };

function ellipseBand(room, center, radiusX, radiusZ, elevation, material, thickness = 1, teeth = false) {
  for (let z = 1; z < room.depth - 1; z++) for (let x = 1; x < room.width - 1; x++) {
    const distance = Math.hypot((x - center[0]) / radiusX, (z - center[1]) / radiusZ);
    const angle = Math.atan2((z - center[1]) / radiusZ, (x - center[0]) / radiusX);
    const tooth = teeth && Math.cos(angle * 16) > 0.5 && distance > 1 && distance < 1.13;
    if (Math.abs(distance - 1) < thickness / Math.max(radiusX, radiusZ) || tooth) room.set(x, elevation, z, tooth ? 'iron_block' : material);
  }
}

function landmarkArchitecture(room) {
  const boss = room.variant.key.startsWith('boss/');
  const clock = room.style === 'clockwork';
  const floor = room.floorY();
  if (!boss) {
    for (let y = 4; y <= 14; y++) {
      const inset = y < 7 ? 2 : y > 11 ? Math.max(1, y - 10) : 1;
      for (const x of [1, 14]) room.box([Math.max(1, x - (x > 7 ? inset - 1 : 0)), y, 1], [Math.min(14, x + (x < 7 ? inset - 1 : 0)), y, 13], clock ? 'deepslate_bricks' : 'mossy_stone_bricks');
    }
    ellipseBand(room, [7.5, 7], 5, 5, 3, room.palette.trim, 0.8);
    if (clock) {
      room.features.push('REVISION: stopped calibration vestibule; upper half gear, silent axial power spine, asymmetric calibration consoles');
      for (let x = 1; x <= 14; x++) for (let y = 7; y <= 14; y++) {
        const distance = Math.hypot(x - 7.5, y - 7);
        if (Math.abs(distance - 6) < 0.8 || (Math.abs(distance - 6.6) < 0.8 && x % 3 === 0)) room.box([x, y, 4], [x, y, 5], 'waxed_cut_copper');
      }
      room.box([7, 11, 1], [8, 12, 13], 'iron_block');
      room.box([7, 11, 6], [8, 11, 8], 'black_concrete');
      for (const x of [2, 12]) {
        room.box([x, 4, 3], [x + 1, 6, 5], 'smooth_stone');
        room.set(x, 6, 4, 'sea_lantern');
        room.segment([x, 5, 7], [x, 10, 4], 'iron_block', 1);
      }
      room.box([2, 4, 10], [4, 5, 12], 'waxed_cut_copper');
      room.box([11, 4, 8], [13, 6, 10], 'polished_andesite');
      room.box([6, 7, 2], [9, 8, 2], 'black_concrete');
      room.set(6, 8, 2, 'sea_lantern');
      room.set(9, 8, 2, 'sea_lantern');
    } else {
      room.features.push('REVISION: seedling guardian root shrine; living seed cradle, bent guardian roots, small bifurcated canopy');
      room.box([5, 4, 2], [10, 4, 5], 'rooted_dirt');
      room.box([6, 5, 3], [9, 5, 4], 'moss_block');
      room.segment([7, 6, 3], [8, 9, 3], 'oak_log');
      room.segment([8, 8, 3], [5, 10, 4], 'oak_log');
      room.segment([8, 8, 3], [10, 10, 4], 'oak_log');
      room.box([5, 10, 3], [10, 11, 4], 'moss_block');
      room.set(8, 9, 4, 'shroomlight');
      for (const x of [2, 13]) {
        room.segment([x, 4, 5], [x < 7 ? 3 : 12, 9, 5], 'dark_oak_log', 1);
        room.segment([x < 7 ? 3 : 12, 9, 5], [x < 7 ? 6 : 9, 13, 6], 'oak_log', 1);
        room.segment([x, 4, 6], [x < 7 ? 4 : 11, 4, 9], 'rooted_dirt');
      }
      room.segment([5, 13, 6], [10, 13, 6], 'moss_block');
      for (const z of [9, 12]) for (const x of [3, 12]) room.set(x, 3, z, 'shroomlight');
    }
    room.box([6, 4, 7], [9, 8, 14], 'air');
    return;
  }
  room.features.push(clock ? 'REVISION: full transmission core; suspended toothed annulus, radial drive spokes, sloping perimeter machine ribs' : 'REVISION: ancient root crown sanctuary; hollow elevated crown, split perimeter trunks, radial root buttresses');
  for (let y = 2; y <= 21; y++) {
    const inset = y > 16 ? Math.max(1, Math.floor((y - 14) / 2)) : y < 6 ? 2 : 1;
    for (const x of [1, 32]) room.box([Math.max(1, x - (x > 16 ? inset - 1 : 0)), y, 1], [Math.min(32, x + (x < 16 ? inset - 1 : 0)), y, 21], clock ? 'deepslate_bricks' : 'mossy_stone_bricks');
    for (const z of [2, 19]) for (let x = 2; x <= 31; x++) {
      const arch = 17 + Math.floor(4 * (1 - Math.abs(x - 16.5) / 15));
      if (y >= arch) room.set(x, y, z, room.palette.trim);
    }
  }
  ellipseBand(room, [16.5, 11.5], 12, 8.5, 1, room.palette.trim, 0.8);
  ellipseBand(room, [16.5, 11.5], 9.5, 6, 1, clock ? 'black_concrete' : 'rooted_dirt', 0.5);
  if (clock) {
    for (const y of [12, 13]) ellipseBand(room, [16.5, 11.5], 11, 7.5, y, 'waxed_cut_copper', 1, true);
    for (let spoke = 0; spoke < 8; spoke++) {
      const angle = spoke * Math.PI / 4;
      room.segment([16, 12, 12], [Math.round(16.5 + Math.cos(angle) * 11), 12, Math.round(11.5 + Math.sin(angle) * 7.5)], 'iron_block');
    }
    room.box([15, 11, 10], [18, 16, 13], 'waxed_copper_block');
    room.box([16, 15, 1], [17, 16, 21], 'iron_block');
    room.box([16, 11, 11], [17, 11, 12], 'sea_lantern');
    room.gear(16, 14, 19, 6, 'xy');
    for (const x of [3, 30]) for (const z of [4, 17]) {
      const shoulder = x < 16 ? 6 : 27;
      room.box([x - 1, 2, z], [x + 1, 6, z + 1], 'polished_andesite');
      room.segment([x, 5, z], [shoulder, 12, z], 'iron_block', 1);
      room.segment([shoulder, 12, z], [x < 16 ? 10 : 23, 17, z + 1], 'waxed_cut_copper', 1);
      room.set(x, 7, z, 'piston[facing=up,extended=false]');
    }
  } else {
    for (const x of [3, 29]) {
      room.box([x, 2, 8], [x + 1, 10, 15], 'dark_oak_log');
      room.segment([x, 7, 10], [x < 16 ? 9 : 24, 16, 9], 'oak_log', 1);
      room.segment([x + 1, 8, 14], [x < 16 ? 10 : 23, 18, 16], 'dark_oak_log', 1);
      for (const z of [5, 18]) room.segment([x, 4, 11], [x < 16 ? 7 : 26, 2, z], 'rooted_dirt', 1);
    }
    for (const y of [16, 17, 18]) ellipseBand(room, [16.5, 11.5], 11 - (y - 16), 7.5 - (y - 16) * 0.5, y, y % 2 ? 'moss_block' : 'dark_oak_log', 1.4);
    for (let spoke = 0; spoke < 6; spoke++) {
      const angle = spoke * Math.PI / 3;
      room.segment([Math.round(16 + 10 * Math.cos(angle)), 17, Math.round(12 + 7 * Math.sin(angle))], [Math.round(16 + 4 * Math.cos(angle)), 21, Math.round(12 + 3 * Math.sin(angle))], 'oak_log', 1);
    }
    room.box([13, 20, 9], [20, 21, 14], 'moss_block');
    for (const x of [10, 23]) for (const z of [8, 16]) room.set(x, 15, z, 'shroomlight');
  }
  room.box([13, floor, 3], [20, 6, 19], 'air');
}

function victoryWall(room) {
  room.box([0, 2, 22], [33, 22, 22], room.palette.wall);
  for (const x of [14, 19]) room.box([x, 2, 21], [x, 10, 23], room.palette.trim);
  room.box([14, 8, 21], [19, 9, 23], room.palette.trim);
  room.box([15, 2, 22], [18, 7, 22], 'air');
  for (const position of [[16, 2, 2], [16, 2, 13], [16, 2, 25]]) room.clearFeet(...position);
  room.features.push('sealed victory partition z22, floor-to-ceiling; only the 4x6 runtime gate aperture connects the return chamber');
}

function architecture(room) {
  const key = room.variant.key;
  const floor = room.floorY();
  const layout = key.includes('/layout/');
  const boss = key.startsWith('boss/');
  const exterior = key.includes('outside_');
  if (room.style === 'clockwork') {
    if (boss) {
      room.features.push('assembly-atrium: piston gantries, interlocked gear wall, overhead drive spine');
      for (const x of [3, 27]) for (const z of [5, 16]) room.frame(x, z, 11);
      for (const x of [8, 17, 26]) room.gear(x, 12, 25, 4, 'xy');
      room.box([15, 16, 3], [18, 17, 24], 'iron_block');
      for (const z of [6, 12, 18]) room.box([4, 17, z], [29, 17, z], 'waxed_cut_copper');
      room.box([3, floor, 21], [10, floor + 1, 24], 'waxed_cut_copper');
    } else if (exterior) {
      room.features.push('mechanical court: freestanding machine buttresses and gear crown');
      for (const x of [2, 12]) for (const z of [2, 11]) room.frame(x, z, 13);
      room.gear(7, 17, 13, 4, 'xy');
      room.box([5, 2, 5], [10, 3, 10], 'waxed_cut_copper');
    } else if (key.includes('cells')) {
      room.features.push('piston-rack workshop: alternating solid backed maintenance bays');
      for (const x of [2, room.width - 4]) for (const z of [2, room.depth - 5]) room.frame(x, z, layout ? 3 : 7);
      room.box([2, floor + 1, 2], [3, floor + 1, room.depth - 3], 'iron_block');
    } else if (key.includes('storage')) {
      room.features.push('drive-shaft repository: stacked longitudinal shafts and copper stock');
      for (const x of [2, room.width - 3]) {
        room.box([x, floor, 2], [x, Math.min(room.height - 3, floor + 6), room.depth - 3], 'polished_andesite');
        for (const y of [floor + 1, floor + 4]) room.box([Math.max(1, x - 1), y, 2], [Math.min(room.width - 2, x + 1), y, room.depth - 3], 'waxed_cut_copper');
      }
    } else if (key.includes('towers') || key.includes('pillar_room')) {
      room.features.push('assembly atrium: offset piston gantries and suspended transmission crossbars');
      for (const x of [2, room.width - 4]) for (const z of [2, room.depth - 5]) room.frame(x, z, layout ? 3 : 8);
      if (!layout) room.box([2, 11, 7], [13, 11, 8], 'iron_block');
    } else if (key.includes('canteen') || key === 'promenade' || key === 'spawn/spawn') {
      room.features.push('maintenance bench hall: solid worktops and segmented wall gears');
      for (const x of [2, 12]) {
        room.box([x, floor, 4], [x + 1, floor + 1, 11], 'smooth_stone');
        room.gear(x, floor + 6, 8, 3);
      }
    } else {
      room.features.push('gear workshop gallery: offset wall gear trains, overhead axle and copper tracks');
      room.gear(2, floor + 5, 5, 3);
      room.gear(13, floor + 7, 10, 3);
      room.box([2, floor + 9, 6], [13, floor + 9, 7], 'iron_block');
      for (const x of [5, 10]) room.box([x, floor - 1, 1], [x, floor - 1, 14], 'waxed_cut_copper');
      if (key.includes('door')) for (const z of [4, 11]) room.box([3, floor + 5, z], [12, floor + 5, z], 'waxed_cut_copper');
      if (key.includes('hole') || key.includes('bridge')) for (const x of [2, 12]) room.box([x, floor, 3], [x + 1, floor + 1, 12], 'gray_concrete');
    }
  } else {
    if (boss) {
      room.features.push('root boss altar: radial root buttresses, branching sacred trees, fungal crown');
      for (const x of [4, 28]) for (const z of [4, 16]) room.rootTree(x, z, 13, 3);
      for (const x of [9, 17, 25]) room.segment([x, 15, 24], [17, 20, 18], 'dark_oak_log', 1);
      room.box([3, floor, 21], [10, floor, 24], 'mossy_stone_bricks');
      room.box([5, floor + 1, 22], [8, floor + 1, 23], 'moss_block');
      for (const x of [12, 21]) room.mushroom(x, 23, 5, 2);
    } else if (exterior) {
      room.features.push('ancient grove court: branching trees and thick canopy');
      for (const x of [3, 11]) for (const z of [3, 10]) room.rootTree(x, z, 15, 2);
      room.box([3, 20, 3], [12, 21, 12], 'moss_block');
    } else if (key.includes('cells') || key.includes('pillar_room') || key === 'spawn/spawn') {
      room.features.push('root chapel: branching buttresses and pointed wooden rib vaults');
      if (layout) {
        for (const x of [2, 9]) for (const z of [2, 8]) room.rootTree(x, z, 3, 1);
      } else {
        for (const z of [3, 11]) {
          room.rootTree(2, z, 7, 2);
          room.rootTree(12, z, 7, 2);
          room.segment([3, floor + 6, z], [7, floor + 10, z], 'dark_oak_log');
          room.segment([12, floor + 6, z], [8, floor + 10, z], 'dark_oak_log');
        }
      }
    } else if (key.includes('storage')) {
      room.features.push('seed vault: terraced moss beds, solid seed cubes and mushroom lamps');
      for (const x of [2, room.width - 4]) for (const z of [3, room.depth - 5]) {
        room.box([x, floor, z], [x + 1, floor, z + 2], 'rooted_dirt');
        room.box([x, floor + 1, z + 1], [x + 1, floor + 2, z + 1], 'melon');
        room.mushroom(x, z, layout ? 3 : 5, 1);
      }
    } else if (key.includes('towers') || key.includes('canteen') || key.includes('hole')) {
      room.features.push('fungal beds: unequal mushroom stems, broad caps and mycelium islands');
      for (const x of [2, room.width - 3]) for (const z of [3, room.depth - 4]) {
        room.box([x - 1, floor, z - 1], [x + 1, floor, z + 1], 'mycelium');
        room.mushroom(x, z, layout ? 3 : (z % 2 ? 5 : 7), 1);
      }
    } else {
      room.features.push('woodland cloister: asymmetric root trees, diagonal high boughs, moss arch crowns');
      for (const z of [3, 10]) room.rootTree(2, z, 8, 2);
      room.rootTree(12, 7, 10, 2);
      room.segment([2, floor + 8, 4], [12, floor + 11, 9], 'dark_oak_log');
      if (key.includes('door') || key === 'promenade') room.box([4, floor + 7, 5], [11, floor + 7, 6], 'moss_block');
    }
  }
}

function ports(room) {
  for (const port of room.variant.ports) for (const [x, y, z] of port.cells) {
    const inwardX = x === 0 ? 1 : x === room.width - 1 ? -1 : 0;
    const inwardZ = z === 0 ? 1 : z === room.depth - 1 ? -1 : 0;
    for (let step = 0; step <= 2; step++) room.clearFeet(x + inwardX * step, y, z + inwardZ * step, room.palette.floor, 5);
  }
}

function ordinaryRoutes(room) {
  const floor = room.floorY();
  const center = Math.floor(room.width / 2) - 1;
  room.box([center - 1, floor, 1], [center + 2, floor + 2, room.depth - 2], 'air');
  room.box([1, floor, Math.floor(room.depth / 2) - 2], [room.width - 2, floor + 2, Math.floor(room.depth / 2) + 1], 'air');
  for (const socket of room.variant.sockets) if (socket.position[1] === floor) room.route([center, floor, Math.floor(room.depth / 2)], socket.position);
  for (const socket of room.variant.sockets) {
    if (socket.position[1] !== floor) continue;
    room.clearFeet(...socket.position);
    room.clearFeet(...socket.spawn_position);
    room.set(...socket.support, room.palette.floor);
  }
  if (room.variant.key === 'spawn/spawn') room.clearFeet(7, 4, 8);
  if (room.variant.key === 'promenade') room.clearFeet(7, 2, 11);
  if (room.variant.key.startsWith('boss/')) {
    for (let x = 15; x <= 18; x++) for (let z = 0; z <= 3; z++) room.clearFeet(x, floor, z);
    room.box([15, 2, 22], [18, 7, 22], 'air');
    for (const position of [[16, 2, 2], [16, 2, 13], [16, 2, 25]]) room.clearFeet(...position);
    room.features.push('boss fixed entry/return/center and x15..18,y2..7,z22 barrier reserved');
  }
  ports(room);
}

function platform(room) {
  room.features.push('new solid side terraces with approach ramps and raised eastern socket step');
  for (const [minX, maxX] of [[2, 4], [11, 13]]) {
    room.box([minX, 1, 5], [maxX, 3, 10], room.palette.trim);
    room.box([minX, 4, 5], [maxX, 7, 10], 'air');
    for (let z = 3; z <= 5; z++) for (let x = minX; x <= maxX; x++) {
      const feet = Math.min(4, z);
      room.clearFeet(x, feet, z);
      if (z < 5) room.set(x, feet - 1, z, `${room.palette.stair}[facing=south,half=bottom,shape=straight,waterlogged=false]`);
    }
  }
  for (let z = 7; z <= 9; z++) room.clearFeet(13, 5, z, room.palette.trim);
  room.clearFeet(12, 5, 8);
  room.set(12, 4, 8, `${room.palette.stair}[facing=east,half=bottom,shape=straight,waterlogged=false]`);
  for (const socket of room.variant.sockets) { room.clearFeet(...socket.position); room.set(...socket.support, room.palette.floor); }
}

async function lodestone() {
  return import(pathToFileURL(join(skill, 'node_modules/@mattzh72/lodestone/lib/index.js')).href);
}

function originalRoom(variant, library) {
  return library.Structure.fromNbt(library.NbtFile.read(readFileSync(join(resources, 'structures', `${variant.key}.nbt`))).root);
}

function walkGraph(room, clearance = 3) {
  const valid = (x, y, z) => room.inside(x, y + clearance - 1, z) && y > 0 && !air(room.get(x, y - 1, z)) && room.get(x, y - 1, z) !== 'void' && Array.from({ length: clearance }, (_, offset) => room.get(x, y + offset, z)).every(air);
  const all = new Set();
  for (let y = 1; y < room.height - clearance + 1; y++) for (let z = 0; z < room.depth; z++) for (let x = 0; x < room.width; x++) if (valid(x, y, z)) all.add(`${x},${y},${z}`);
  return { all, valid };
}

function flood(room, start, graph, avoidReservedRises = false) {
  const fixed = new Set(room.variant.sockets.map(socket => socket.position.join(',')).concat(room.variant.ports.flatMap(port => port.cells).map(position => position.join(','))));
  const pending = [start];
  const visited = new Set([start.join(',')]);
  const parent = new Map();
  for (let cursor = 0; cursor < pending.length; cursor++) {
    const [x, y, z] = pending[cursor];
    for (const [dx, dz] of [[1, 0], [-1, 0], [0, 1], [0, -1]]) for (const dy of [0, 1, -1]) {
      const next = [x + dx, y + dy, z + dz];
      const nextKey = next.join(',');
      if (!graph.all.has(nextKey) || visited.has(nextKey)) continue;
      if (avoidReservedRises && dy === 1 && fixed.has(nextKey)) continue;
      if (dy === 1 && !air(room.get(x, y + 3, z))) continue;
      if (dy === -1 && !air(room.get(next[0], y + 2, next[2]))) continue;
      visited.add(nextKey);
      parent.set(nextKey, [x, y, z]);
      pending.push(next);
    }
  }
  return { visited, parent };
}

function stairs(room, library) {
  const original = originalRoom(room.variant, library);
  room.features.push('permitted original staircase circulation skeleton; solid enclosure with machine core or branching root core');
  for (const block of original.getBlocks()) {
    const [x, y, z] = block.pos;
    const name = blockName(block.state.toString());
    if (name === 'air') continue;
    let material = name.includes('stairs') ? `${room.palette.stair}${block.state.toString().slice(block.state.toString().indexOf('['))}` : name.includes('lantern') ? room.palette.light : room.palette.trim;
    if (y <= 1 || y >= 30 || x === 0 || x === 15 || z === 0 || z === 15) material = room.palette.wall;
    if (name.includes('slab')) material = room.palette.floor;
    room.set(x, y, z, material);
  }
  room.box([0, 31, 0], [15, 31, 15], room.palette.wall);
  for (let y = 2; y <= 30; y++) for (const side of [0, 15]) {
    room.box([side, y, 0], [side, y, 15], room.palette.wall);
    room.box([0, y, side], [15, y, side], room.palette.wall);
  }
  for (const socket of room.variant.sockets) { room.clearFeet(...socket.position); room.set(...socket.support, room.palette.floor); }
  ports(room);
  let graph = walkGraph(room, 2);
  let reachable = flood(room, [7, 2, 0], graph, true);
  const targets = room.variant.ports.flatMap(port => port.cells).concat(room.variant.sockets.map(socket => socket.position));
  for (const target of targets) assert(reachable.visited.has(target.join(',')), `${room.variant.key} original circulation disconnected ${target}`);
  for (const target of targets) {
    let cursor = target;
    while (cursor) {
      room.box(cursor, [cursor[0], cursor[1] + 2, cursor[2]], 'air');
      cursor = reachable.parent.get(cursor.join(','));
    }
  }
  const fixedSupports = new Set(room.variant.sockets.map(socket => socket.support.join(',')).concat(room.variant.ports.flatMap(port => port.cells).map(([x, y, z]) => [x, y - 1, z].join(','))));
  let stairRepairs = 0;
  for (const target of targets) {
    let cursor = target;
    while (cursor) {
      const parent = reachable.parent.get(cursor.join(','));
      if (parent && cursor[1] !== parent[1]) {
        const lower = cursor[1] < parent[1] ? cursor : parent;
        const higher = cursor[1] > parent[1] ? cursor : parent;
        const support = [higher[0], higher[1] - 1, higher[2]];
        assert(!fixedSupports.has(support.join(',')), `Reserved support on rising staircase edge ${room.variant.key} ${support}`);
        const facing = higher[0] > lower[0] ? 'east' : higher[0] < lower[0] ? 'west' : higher[2] > lower[2] ? 'south' : 'north';
        for (let offset = -1; offset <= 1; offset++) {
          const side = higher[0] === lower[0] ? [support[0] + offset, support[1], support[2]] : [support[0], support[1], support[2] + offset];
          if (!room.inside(...side) || fixedSupports.has(side.join(',')) || air(room.get(...side))) continue;
          if (![1, 2, 3].every(rise => air(room.get(side[0], side[1] + rise, side[2])))) continue;
          room.set(...side, `${room.palette.stair}[facing=${facing},half=bottom,shape=straight,waterlogged=false]`);
        }
        stairRepairs++;
      }
      cursor = parent;
    }
  }
  room.features.push(`walkable half-step repairs on original skeleton: ${stairRepairs} certified-route edges`);
  graph = walkGraph(room);
  reachable = flood(room, [7, 2, 0], graph);
  for (const target of targets) assert(reachable.visited.has(target.join(',')), `${room.variant.key} clearance repair disconnected ${target}`);
  if (room.style === 'clockwork') {
    for (let y = 7; y <= 27; y++) for (const x of [7, 8]) for (const z of [7, 8]) if (!air(room.get(x, y, z))) room.set(x, y, z, y % 4 === 0 ? 'iron_block' : 'waxed_cut_copper');
    room.gear(7, 25, 7, 3, 'xy');
  } else {
    for (let y = 3; y <= 27; y++) for (const x of [7, 8]) for (const z of [7, 8]) if (!air(room.get(x, y, z))) room.set(x, y, z, 'dark_oak_log');
    room.segment([7, 24, 7], [3, 28, 10], 'oak_log');
    room.segment([8, 24, 8], [12, 29, 4], 'oak_log');
  }
  for (const socket of room.variant.sockets) room.clearFeet(...socket.position);
  ports(room);
}

function makeRoom(variant, style, library) {
  const room = new Room(variant, style);
  if (variant.key.startsWith('stairs/')) stairs(room, library);
  else {
    room.shell();
    if (featureKeys.has(variant.key)) landmarkArchitecture(room);
    else architecture(room);
    if (variant.key === 'spawn/spawn') {
      room.clearFeet(7, 4, 8);
      ports(room);
    } else ordinaryRoutes(room);
    if (variant.key.startsWith('boss/')) victoryWall(room);
    if (variant.key === 'straight/platform') platform(room);
  }
  return room;
}

export default async function build(api) {
  const style = process.env.ROOMS_TEAM1_STYLE;
  const key = process.env.ROOMS_TEAM1_KEY;
  assert(styles.includes(style), 'Set ROOMS_TEAM1_STYLE to clockwork or forest');
  const variant = catalog.variants.find(entry => entry.key === key);
  assert(variant, `Unknown key ${key}`);
  const room = makeRoom(variant, style, api.lodestone);
  const structure = api.createStructure(variant.size);
  for (let y = 0; y < room.height; y++) for (let z = 0; z < room.depth; z++) for (let x = 0; x < room.width; x++) api.setBlock(structure, [x, y, z], `minecraft:${room.get(x, y, z)}`);
  return { structure, metadata: { name: key.split('/').at(-1), style, features: room.features } };
}

function validate(bytes, variant, library, expectedRoom) {
  const file = library.NbtFile.read(bytes);
  assert.equal(file.root.getNumber('DataVersion'), 3465);
  assert.equal(file.root.getList('entities').length, 0);
  const palette = file.root.getList('palette');
  for (let index = 0; index < palette.length; index++) {
    const entry = palette.getCompound(index);
    const name = entry.getString('Name');
    assert(/^minecraft:[a-z0-9_/]+$/.test(name), `Invalid palette.Name, states must be separate Properties: ${variant.key} ${name}`);
  }
  const structure = library.Structure.fromNbt(file.root);
  assert.deepEqual(structure.getSize(), variant.size);
  const blocks = structure.getBlocks();
  assert.equal(blocks.length, variant.size.reduce((product, value) => product * value, 1));
  const positions = new Set();
  const room = new Room(variant, expectedRoom.style);
  for (const block of blocks) {
    assert(!block.nbt, `Block entity ${variant.key}`);
    assert(room.inside(...block.pos));
    positions.add(block.pos.join(','));
    room.set(...block.pos, block.state.toString().replace(/^minecraft:/, ''));
  }
  assert.equal(positions.size, blocks.length);
  const fullSupport = position => {
    const name = blockName(room.get(...position));
    assert(!['air', 'void'].includes(name) && !/stairs|slab|glass_pane|fence|chain|piston|leaves/.test(name), `Non full support ${variant.key} ${position}: ${name}`);
  };
  const safe = (position, clearance = 3) => {
    for (let offset = 0; offset < clearance; offset++) assert(air(room.get(position[0], position[1] + offset, position[2])), `Blocked clearance ${variant.key} ${position}`);
    fullSupport([position[0], position[1] - 1, position[2]]);
  };
  for (const socket of variant.sockets) { safe(socket.position); safe(socket.spawn_position); fullSupport(socket.support); }
  for (const port of variant.ports) for (const position of port.cells) safe(position, 5);
  if (variant.key === 'spawn/spawn') safe([7, 4, 8]);
  if (variant.key === 'promenade') safe([7, 2, 11]);
  if (variant.key.startsWith('boss/')) {
    for (const position of [[16, 2, 2], [16, 2, 13], [16, 2, 25]]) safe(position);
    for (let x = 15; x <= 18; x++) for (let y = 2; y <= 7; y++) assert(air(room.get(x, y, 22)));
    for (let x = 13; x <= 20; x++) for (let z = 3; z <= 19; z++) for (let y = 2; y <= 6; y++) assert(air(room.get(x, y, z)), `Central combat zone obstructed ${room.style} ${[x, y, z]}`);
    for (let x = 15; x <= 18; x++) for (let z = 1; z <= 26; z++) safe([x, 2, z]);
  }
  if (variant.key === 'spawn/spawn') for (let x = 6; x <= 9; x++) for (let z = 7; z <= 15; z++) safe([x, 4, z]);
  const graph = walkGraph(room);
  const start = variant.ports[0]?.cells[0] ?? (variant.key.startsWith('boss/') ? [16, 2, 0] : [Math.floor(room.width / 2) - 1, room.floorY(), Math.floor(room.depth / 2)]);
  assert(graph.all.has(start.join(',')), `Invalid walk start ${variant.key} ${start}`);
  const { visited } = flood(room, start, graph);
  const targets = variant.ports.flatMap(port => port.cells).concat(variant.sockets.flatMap(socket => [socket.position, socket.spawn_position]));
  for (const target of targets) assert(visited.has(target.join(',')), `Unreachable ${variant.key} ${target}`);
  let victoryGate = null;
  if (variant.key.startsWith('boss/')) {
    assert(visited.has('16,2,25'), 'Open victory gate must connect the return chamber');
    for (let x = 0; x < 34; x++) for (let y = 2; y < 23; y++) {
      if (x >= 15 && x <= 18 && y <= 7) continue;
      assert(!air(room.get(x, y, 22)), `Victory wall gap ${room.style} ${[x, y, 22]}`);
    }
    const closed = new Room(variant, room.style);
    closed.blocks = room.blocks.slice();
    closed.box([15, 2, 22], [18, 7, 22], 'bedrock');
    const locked = flood(closed, [16, 2, 2], walkGraph(closed));
    assert(!locked.visited.has('16,2,25'), 'Victory barrier can be bypassed');
    victoryGate = { openReturnReachable: true, closedReturnReachable: false, partitionCells: 34 * 21, apertureCells: 24 };
  }
  const halfStepWalk = variant.key.startsWith('stairs/') || variant.key === 'straight/platform' ? certifyHalfSteps(room, start, targets) : null;
  const alternativeSpawns = [];
  const reserved = new Set(variant.sockets.map(socket => socket.position.join(',')));
  const assignedSpawns = new Set();
  for (const socket of variant.sockets) {
    const [x, y, z] = socket.position;
    const candidates = [...visited].map(value => value.split(',').map(Number)).filter(position => position[1] === y && !reserved.has(position.join(',')) && Math.abs(position[0] - x) + Math.abs(position[2] - z) >= 1 && Math.abs(position[0] - x) + Math.abs(position[2] - z) <= 6);
    assert(candidates.length > 0, `No independent monster spawn near ${variant.key} ${socket.position}`);
    const selected = candidates.find(position => !assignedSpawns.has(position.join(',')) && !/stairs|slab/.test(room.get(position[0], position[1] - 1, position[2])));
    assert(selected, `No solid alternative monster spawn ${variant.key} ${socket.position}`);
    safe(selected);
    assignedSpawns.add(selected.join(','));
    alternativeSpawns.push({ socket: socket.id, position: selected, candidates: candidates.length });
  }
  const allowedOpen = new Set();
  for (const port of variant.ports) for (const [x, y, z] of port.cells) for (let offset = 0; offset < 5; offset++) allowedOpen.add([x, y + offset, z].join(','));
  if (variant.key.startsWith('boss/')) for (let x = 15; x <= 18; x++) for (let y = 2; y <= 4; y++) allowedOpen.add([x, y, 0].join(','));
  for (const block of blocks) {
    const [x, y, z] = block.pos;
    if (x !== 0 && x !== room.width - 1 && z !== 0 && z !== room.depth - 1 && y !== room.height - 1) continue;
    if (air(room.get(x, y, z))) assert(allowedOpen.has(block.pos.join(',')), `Unexpected shell opening ${variant.key} ${block.pos}`);
  }
  const original = originalRoom(variant, library);
  let changedOccupancy = 0;
  let interiorChangedOccupancy = 0;
  for (const block of original.getBlocks()) {
    const [x, y, z] = block.pos;
    if (air(block.state.toString()) !== air(room.get(x, y, z))) {
      changedOccupancy++;
      if (x > 0 && x < room.width - 1 && z > 0 && z < room.depth - 1 && y > room.floorY() && y < room.height - 1) interiorChangedOccupancy++;
    }
  }
  assert(changedOccupancy > 0, `Geometry unchanged ${variant.key}`);
  return { key: variant.key, size: variant.size, minimum_y: variant.minimum_y, blocks: blocks.length, sha256: hash(bytes), ports: variant.ports.length, socketCount: variant.sockets.length, socketClearance: 3, portClearance: 5, reachableWalkCells: visited.size, halfStepWalk, victoryGate, changedOccupancy, interiorChangedOccupancy, occupancySha256: hash(Buffer.from(room.blocks.map(block => air(block) ? 0 : 1))), alternativeSpawns, features: expectedRoom.features };
}

function certifyHalfSteps(room, start, targets) {
  const surfaces = new Map();
  const heights = (microX, microZ, blockY) => {
    const block = room.get(Math.floor(microX / 2), blockY, Math.floor(microZ / 2));
    if (air(block)) return 0;
    if (!block.includes('stairs')) return 2;
    const high = block.includes('facing=east') ? microX % 2 === 1 : block.includes('facing=west') ? microX % 2 === 0 : block.includes('facing=south') ? microZ % 2 === 1 : microZ % 2 === 0;
    return high ? 2 : 1;
  };
  const clear = (microX, feet, microZ) => {
    for (let blockY = Math.floor(feet / 2); blockY < Math.ceil((feet + 4) / 2); blockY++) {
      if (blockY >= room.height) return false;
      const height = heights(microX, microZ, blockY);
      if (height && blockY * 2 < feet + 4 && blockY * 2 + height > feet) return false;
    }
    return true;
  };
  for (let microX = 0; microX < room.width * 2; microX++) for (let microZ = 0; microZ < room.depth * 2; microZ++) {
    const column = [];
    for (let blockY = 0; blockY < room.height - 2; blockY++) {
      const height = heights(microX, microZ, blockY);
      const feet = blockY * 2 + height;
      if (height && clear(microX, feet, microZ)) column.push(feet);
    }
    surfaces.set(`${microX},${microZ}`, column);
  }
  const seed = [start[0] * 2, start[1] * 2, start[2] * 2];
  const queue = [seed];
  const visited = new Set([seed.join(',')]);
  for (let cursor = 0; cursor < queue.length; cursor++) {
    const [microX, feet, microZ] = queue[cursor];
    for (const [dx, dz] of [[1, 0], [-1, 0], [0, 1], [0, -1]]) {
      const nextX = microX + dx;
      const nextZ = microZ + dz;
      for (const nextFeet of surfaces.get(`${nextX},${nextZ}`) ?? []) {
        if (Math.abs(nextFeet - feet) > 1 || !clear(microX, Math.max(feet, nextFeet), microZ) || !clear(nextX, Math.max(feet, nextFeet), nextZ)) continue;
        const next = [nextX, nextFeet, nextZ];
        const key = next.join(',');
        if (visited.has(key)) continue;
        visited.add(key);
        queue.push(next);
      }
    }
  }
  for (const [x, y, z] of targets) assert([0, 1].some(dx => [0, 1].some(dz => visited.has([x * 2 + dx, y * 2, z * 2 + dz].join(',')))), `No jump-free half-step path ${room.variant.key} ${[x, y, z]}`);
  return { microGrid: '0.5 block horizontal/vertical', maximumStep: 0.5, headroom: 2, reachableMicroCells: visited.size, targets: targets.length, allTargetsReachableWithoutJump: true };
}

function atlas(style, library) {
  const parts = ['<svg xmlns="http://www.w3.org/2000/svg" width="1260" height="3210" viewBox="0 0 1260 3210"><rect width="1260" height="3210" fill="#111820"/><g font-family="Arial,sans-serif" fill="#edf2f7"><text x="28" y="38" font-size="25">' + style + ' / 27 independent room templates</text><text x="28" y="63" font-size="14">Two horizontal NBT slices per room. Dark = air; amber/green = solid; cyan = light; pink = socket.</text>'];
  for (let index = 0; index < catalog.variants.length; index++) {
    const variant = catalog.variants[index];
    const room = makeRoom(variant, style, library);
    const originX = 20 + (index % 3) * 415;
    const originY = 90 + Math.floor(index / 3) * 345;
    parts.push(`<rect x="${originX}" y="${originY}" width="400" height="330" rx="12" fill="#1d2935"/><text x="${originX + 12}" y="${originY + 24}" font-size="15">${variant.key}</text><text x="${originX + 12}" y="${originY + 46}" font-size="12" fill="#afbdc9">${variant.size.join(' x ')} / sockets ${variant.sockets.length}</text>`);
    const slices = [room.floorY(), variant.key.startsWith('stairs/') ? 25 : Math.min(room.height - 2, room.floorY() + 6)];
    for (let slice = 0; slice < slices.length; slice++) {
      const elevation = slices[slice];
      const scale = Math.min(175 / room.width, 235 / room.depth);
      const left = originX + 13 + slice * 195;
      const top = originY + 82;
      parts.push(`<text x="${left}" y="${originY + 68}" font-size="12">Y=${elevation}</text>`);
      for (let z = 0; z < room.depth; z++) for (let x = 0; x < room.width; x++) {
        const material = room.get(x, elevation, z);
        const isSocket = variant.sockets.some(socket => socket.position[0] === x && socket.position[2] === z && socket.position[1] === elevation);
        const color = isSocket ? '#ff80c0' : air(material) ? '#0d141d' : material.includes('lantern') || material.includes('shroomlight') ? '#58e5ed' : style === 'clockwork' ? material.includes('copper') ? '#e7a455' : '#a1afba' : material.includes('log') ? '#ac8658' : '#8ab96c';
        parts.push(`<rect x="${left + x * scale}" y="${top + z * scale}" width="${scale - 0.3}" height="${scale - 0.3}" fill="${color}"/>`);
      }
    }
  }
  parts.push('</g></svg>');
  writeFileSync(join(output, `${style}-atlas.svg`), parts.join('\n'));
}

function documentation(report, library) {
  const rows = catalog.variants.map(variant => {
    const clock = report.results.find(result => result.style === 'clockwork' && result.key === variant.key);
    const forest = report.results.find(result => result.style === 'forest' && result.key === variant.key);
    const comparison = report.comparisons.find(result => result.key === variant.key);
    return `| ${variant.key} | ${variant.size.join('×')} | ${clock.changedOccupancy} | ${forest.changedOccupancy} | ${comparison.occupancyDifference} |`;
  });
  const text = `# Team 1：钟械与古林独立中型地牢房间

完整生成54个NBT：clockwork、forest各27个，覆盖原catalog每个variant。总计275312个显式坐标记录（包含air）；174个socket、94组port。两风格各27/27个模板均与旧版有solid/air占位差异；两个风格对应的27对模板也全部存在占位差异。

## 文件与复现

- 配方：tools/rooms-team1.mjs。
- 正式资源：src/main/resources/data/fteclipsegen/roomsets/{clockwork,forest}/<key>.nbt及manifest.json。
- 校验：output/rooms-team1/validation.json。
- 几何切片图集：clockwork-atlas.svg、forest-atlas.svg。每模板显示步行层与装饰层的实际NBT占位，包含各层socket标记。
- Lodestone CLI输出记录：output/rooms-team1/lodestone/。
- 重新生成：node tools/rooms-team1.mjs；只检查已生成文件：node tools/rooms-team1.mjs --verify-only。
- 默认使用已安装技能；MINECRAFT_BUILDER_SKILL可指定技能目录。依赖必须先安装在技能自身目录。

## 独立几何设计

钟械：有齿轮轮缘与轮齿的车间、带实体活塞的机架、维修工作台、分层传动轴库、悬挂传动横梁与四组装配机架组成的Boss中庭。

古林：分叉树根扶壁与尖拱礼拜堂、不同高度的菌床和大型蘑菇、非对称林木回廊、阶梯种子床、放射树枝穹顶与树根Boss祭坛。

普通房间从空网格重新构建，未拷贝旧钟塔内装。走廊在各自风格内统一模块，可重复。仅三种楼梯按任务许可沿用原通行骨架，重建外壳、三格头部净空、机械动力芯或分叉树根芯，并把通行路线及侧向可用踏步换成真实朝向台阶。straight/platform重新制作有实体支撑的三格宽侧台阶与高台。

## 安全检查

DataVersion=3465；尺寸、minimum_y、ports、sockets按未改动catalog；逐坐标密集NBT、坐标唯一且不越界，无实体或方块实体。所有声明门洞脚点上方rise0..4均AIR，下方完整实心。socket原坐标与spawn_position三格AIR，下有完整顶面；每个socket都分配一个同高度距离1..6的独立非socket、非台阶出生点，且这些备选点在模板内互不重复。

常规房间保留至少三格宽主路。所有socket与每个port连通；楼梯覆盖y2/y18全部端口与悬高socket。额外使用0.5格水平/垂直分辨率的台阶表面图，最大步差0.5、两格头部空间，确认三种楼梯与高台所有目标无需跳跃可达。socket本身和所有门洞仍按更严格的3/5格净空检查。

封闭边界仅在catalog门洞及原Boss隐式北门x15..18/z0开口；不向外扩张。入口[7,4,8]、promenade[7,2,11]与Boss入口[16,2,2]、中心[16,2,13]、返程[16,2,25]都有地面与净空；Boss屏障x15..18/y2..7/z22完全留空。Boss主场地保留通行地面。室外模板以封闭庭院表达，12格子房间保持原尺寸。

## Manifest字节哈希

manifest格式为schema/style/templates，恰好27个模板项，每项仅key、真实文件sha256。以下供主线程独立复核并冻结，不写入Java。

${report.styleSummaries.map(summary => `- ${summary.style}: ${summary.manifestSha256}`).join('\n')}

## 占位差异证据

数字是逐坐标solid/air占位差异数量，忽略所有方块材质。证明不是换色。详细内部占位差异和完整签名见validation.json。

| key | 尺寸 | 钟械对旧版 | 古林对旧版 | 两风格间 |
|---|---|---:|---:|---:|
${rows.join('\n')}

## 验收限制

已用技能原有Lodestone CLI导出并round-trip，预览服务与NBT端点HTTP 200。未进行实际Minecraft内的碰撞、战斗、光照与视觉验收，半格图仍是静态近似；室外模板的封闭庭院取舍以及子房间装配体验需游戏内复核。未运行全工程Gradle，未改Java、旧styles/catalog/structures、版本或正式mods。主线程负责manifest冻结和运行时集成。
`;
  writeFileSync(join(output, 'README.md'), text);
  for (const style of styles) atlas(style, library);
  landmarkDocumentation(report, library);
}

function landmarkDocumentation(report, library) {
  const parts = ['<svg xmlns="http://www.w3.org/2000/svg" width="1320" height="1660" viewBox="0 0 1320 1660"><rect width="1320" height="1660" fill="#101820"/><g font-family="Arial,sans-serif" fill="#eef4f7"><text x="24" y="35" font-size="23">Landmark redesign / archived 0.32 vs current</text><text x="24" y="60" font-size="14">Exact solid/air sections: front and axial side. Dark=air; amber=machinery; green/brown=roots; cyan=light.</text>'];
  let row = 0;
  for (const style of styles) for (const key of featureKeys) {
    const variant = catalog.variants.find(entry => entry.key === key);
    const current = makeRoom(variant, style, library);
    const previous = new Room(variant, style);
    const archive = join(resources, 'roomsets-history', style, priorManifests[style], `${key}.nbt`);
    for (const block of library.Structure.fromNbt(library.NbtFile.read(readFileSync(archive)).root).getBlocks()) previous.set(...block.pos, block.state.toString().replace(/^minecraft:/, ''));
    const top = 90 + row * 390;
    const change = report.landmarkRevision.changedTemplates.find(entry => entry.style === style && entry.key === key);
    parts.push(`<text x="24" y="${top + 22}" font-size="19">${style} / ${key} / ${change.occupancyDifference} solid-air cells changed</text>`);
    const frontDepth = key.startsWith('boss/') ? style === 'clockwork' ? 19 : 12 : 4;
    const sideX = key.startsWith('boss/') ? 16 : 7;
    for (let panel = 0; panel < 4; panel++) {
      const room = panel % 2 ? current : previous;
      const front = panel < 2;
      const horizontal = front ? room.width : room.depth;
      const scale = Math.min(290 / horizontal, 275 / room.height);
      const left = 24 + panel * 325;
      const sliceLabel = front ? `front Z=${frontDepth}` : `side X=${sideX}`;
      parts.push(`<text x="${left}" y="${top + 50}" font-size="14">${panel % 2 ? 'NEW' : '0.32'} / ${sliceLabel}</text><rect x="${left}" y="${top + 65}" width="300" height="285" fill="#081019"/>`);
      for (let y = 0; y < room.height; y++) for (let across = 0; across < horizontal; across++) {
        const block = front ? room.get(across, y, frontDepth) : room.get(sideX, y, across);
        const color = air(block) ? '#081019' : /lantern|shroomlight/.test(block) ? '#4be5dd' : /copper/.test(block) ? '#e0a05b' : /log/.test(block) ? '#b18c61' : /moss|dirt/.test(block) ? '#88b772' : /iron/.test(block) ? '#dae6ee' : /black_concrete/.test(block) ? '#34475c' : '#627485';
        parts.push(`<rect x="${left + across * scale}" y="${top + 65 + (room.height - 1 - y) * scale}" width="${scale - 0.2}" height="${scale - 0.2}" fill="${color}"/>`);
      }
    }
    row++;
  }
  parts.push('</g></svg>');
  writeFileSync(join(output, 'landmark-revision-sections.svg'), parts.join('\n'));
  const rows = report.landmarkRevision.changedTemplates.map(entry => `| ${entry.style} | ${entry.key} | ${entry.occupancyDifference} | ${entry.materialDifference} | ${entry.victoryGate ? '开门可达 / 封门不可达' : '四格轴线连通' } |`);
  const text = `# 初始房与Boss房：四模板重新设计

此次只改变clockwork、forest的spawn/spawn与boss/eclipse_keeper。其余50个NBT逐项SHA-256与归档0.32 manifest一致；当前27项manifest真实哈希全部重新计算。没有修改history、Java、版本或正式mods。

## 主题起承

钟械初始为停机校准门厅：上半环齿轮、黑色断能段、轴向动力脊、错层校准台、削角壁龛。Boss变为完整大型悬空传动核心：双层带齿环形机架、八向辐条、完整竖向齿轮、中央高悬齿轮箱和斜撑机肋。地面同心标记把校准半环引向完整动力环，中心地面无大柱。

古林初始为幼苗守护根殿：低矮种子台、分叉幼苗、两侧弯根护拱与小冠盖。Boss演进为古树根冠圣所：外缘宽根干、放射根须、分叉巨枝、三层中空树冠与高位冠顶。以同心根纹和轴线呼应幼苗，古树主干位于两侧，保留中心战斗区。

两套外墙和顶部完整封闭；内壁削角、厚壁龛与起伏拱顶改变室内轮廓。初始四格轴线x6..9/z7..15有三格净空与完整地面，出口五格净空。Boss中心8×17格战斗地面上方五格无大柱，四格轴线x15..18/z1..26连通，八个socket和各自独立出生脚点安全。

## Boss胜利隔墙

z22整平面从地面封到屋顶，仅保留x15..18/y2..7这个24格机制门孔。入口[16,2,2]、Boss中心[16,2,13]、返程[16,2,25]均安全。静态检测：门孔AIR时前场可达返程；用实心方块模拟原机制封门后返程不可达，不能绕侧墙或翻墙。没有改变Boss机制。

## 新旧占位变化与安全

| 风格 | 模板 | solid/air占位差异 | 方块状态差异 | 连通 |
|---|---|---:|---:|---|
${rows.join('\n')}

DataVersion3465、密集坐标含air、无实体和方块实体、palette.Name为纯ID且Properties单列、原尺寸/minimum_y/ports/sockets全部保留。其余50模板字节不变的逐项证据和四个新旧文件哈希见validation.json的landmarkRevision。

## 剖视预览

landmark-revision-sections.svg逐行展示4个模板的旧前剖、新前剖、旧轴向侧剖、新轴向侧剖，可直接比较半环/完整齿轮、幼苗/巨冠、完整隔墙和头部空间。两份全模板atlas.svg同步更新。

## 复现与冻结

仅更新四模板：node tools/rooms-team1.mjs --features-only。
完整生成54模板：node tools/rooms-team1.mjs（其余50个结果必须与归档manifest字节哈希一致，相同文件不会重新写入）。
只检查当前资源：node tools/rooms-team1.mjs --verify-only。

${report.styleSummaries.map(entry => `- ${entry.style} manifest SHA-256: ${entry.manifestSha256}`).join('\n')}

限制：未跑Gradle或实际游戏；静态步行图与剖视图不替代游戏视觉和碰撞验收。旧归档的promenade.nbt缺失，因此未修改模板的保留证据使用归档manifest的真实文件哈希；四个新旧几何对比使用归档中实际NBT。没有修补或修改history。
`;
  writeFileSync(join(output, 'landmark-revision.md'), text);
}

async function main() {
  assert(existsSync(join(skill, 'node_modules/@mattzh72/lodestone')), 'Install the skill dependencies first');
  const library = await lodestone();
  mkdirSync(output, { recursive: true });
  const results = [];
  const preservation = [];
  const featureChanges = [];
  for (const style of styles) {
    const templates = [];
    const previousManifest = JSON.parse(readFileSync(join(resources, 'roomsets-history', style, priorManifests[style], 'manifest.json'), 'utf8'));
    for (const variant of catalog.variants) {
      const target = join(resources, 'roomsets', style, `${variant.key}.nbt`);
      const staging = join(output, 'lodestone', style, variant.key);
      if (!process.argv.includes('--verify-only') && (!process.argv.includes('--features-only') || featureKeys.has(variant.key))) {
        const generated = spawnSync(process.execPath, [join(skill, 'scripts/run-build.mjs'), fileURLToPath(import.meta.url), '--out-dir', staging, '--name', variant.key.split('/').at(-1), '--data-version', '3465', '--no-block-state-updates'], { env: { ...process.env, ROOMS_TEAM1_STYLE: style, ROOMS_TEAM1_KEY: variant.key }, encoding: 'utf8' });
        assert.equal(generated.status, 0, `${style}/${variant.key}: ${generated.stderr}`);
        const bytes = readFileSync(join(staging, `${variant.key.split('/').at(-1).replaceAll('_', '-')}.nbt`));
        mkdirSync(dirname(target), { recursive: true });
        if (!existsSync(target) || !readFileSync(target).equals(bytes)) writeFileSync(target, bytes);
      }
      const bytes = readFileSync(target);
      const result = validate(bytes, variant, library, makeRoom(variant, style, library));
      const archivedPath = join(resources, 'roomsets-history', style, priorManifests[style], `${variant.key}.nbt`);
      if (!featureKeys.has(variant.key)) {
        assert.equal(hash(bytes), previousManifest.templates.find(entry => entry.key === variant.key).sha256, `Unrelated template changed: ${style}/${variant.key}`);
        preservation.push({ style, key: variant.key, sha256: hash(bytes), byteIdentical: true });
      } else {
        const archived = readFileSync(archivedPath);
        const previous = library.Structure.fromNbt(library.NbtFile.read(archived).root);
        const current = library.Structure.fromNbt(library.NbtFile.read(bytes).root);
        let occupancyDifference = 0;
        let materialDifference = 0;
        for (const block of previous.getBlocks()) {
          const next = current.getBlock(block.pos).state.toString();
          if (air(block.state.toString()) !== air(next)) occupancyDifference++;
          if (block.state.toString() !== next) materialDifference++;
        }
        assert(occupancyDifference > 100, `Insufficient landmark geometry change ${style}/${variant.key}`);
        featureChanges.push({ style, key: variant.key, previousSha256: hash(archived), sha256: hash(bytes), occupancyDifference, materialDifference, victoryGate: result.victoryGate, features: result.features });
      }
      results.push({ style, ...result });
      templates.push({ key: variant.key, sha256: result.sha256 });
      console.log(`${style} ${variant.key}: dense=${result.blocks} geometry=${result.changedOccupancy} reachable=${result.reachableWalkCells}`);
    }
    const manifest = { schema: 1, style: `fteclipsegen:${style}`, templates };
    const manifestPath = join(resources, 'roomsets', style, 'manifest.json');
    if (process.argv.includes('--verify-only')) assert.deepEqual(JSON.parse(readFileSync(manifestPath, 'utf8')), manifest);
    else writeFileSync(manifestPath, `${JSON.stringify(manifest, null, 2)}\n`);
  }
  const comparisons = [];
  for (const variant of catalog.variants) {
    const clock = makeRoom(variant, 'clockwork', library);
    const forest = makeRoom(variant, 'forest', library);
    let occupancyDifference = 0;
    let interiorOccupancyDifference = 0;
    for (let y = 0; y < clock.height; y++) for (let z = 0; z < clock.depth; z++) for (let x = 0; x < clock.width; x++) {
      if (air(clock.get(x, y, z)) !== air(forest.get(x, y, z))) {
        occupancyDifference++;
        if (x > 0 && x < clock.width - 1 && z > 0 && z < clock.depth - 1 && y > clock.floorY() && y < clock.height - 1) interiorOccupancyDifference++;
      }
    }
    assert(occupancyDifference > 0, `Clockwork and forest geometry identical: ${variant.key}`);
    comparisons.push({ key: variant.key, occupancyDifference, interiorOccupancyDifference });
  }
  const report = { schema: 1, totalTemplates: results.length, styles, catalogSha256: hash(readFileSync(join(resources, 'catalog.json'))), tests: ['3465', 'dense unique bounded coordinates including air', 'no entities or block entities', 'catalog sizes unchanged', 'ports have 5 air and full support', 'sockets and spawn positions have 3 air and full support', 'independent same-height monster spawn within 1..6', 'step adjacency connectivity from sockets to every port', 'sealed envelope except catalog ports and original boss entry', 'boss fixed positions and barrier reserved', 'occupancy differs from original and across styles'], comparisons, results };
  report.tests.push('half-block surface graph: stairs and platforms walkable without jumping');
  report.tests.push('palette.Name is a plain minecraft:block_id without brackets; states stored separately');
  report.tests.push('50 unrelated templates byte-identical to archived 0.32; four landmark revisions differ in occupancy');
  report.tests.push('Boss floor-to-ceiling victory partition: return reachable when open, unreachable when sealed');
  report.landmarkRevision = { changedTemplates: featureChanges, preservedTemplates: preservation };
  report.styleSummaries = styles.map(style => ({ style, templates: results.filter(result => result.style === style).length, changedGeometryTemplates: results.filter(result => result.style === style && result.changedOccupancy > 0).length, manifestSha256: hash(readFileSync(join(resources, 'roomsets', style, 'manifest.json'))), occupancySignature: hash(Buffer.from(results.filter(result => result.style === style).map(result => result.occupancySha256).join('\n'))) }));
  for (const summary of report.styleSummaries) assert(summary.changedGeometryTemplates >= 20);
  writeFileSync(join(output, 'validation.json'), `${JSON.stringify(report, null, 2)}\n`);
  documentation(report, library);
  console.log(`Validated ${results.length} templates; report ${join(output, 'validation.json')}`);
}

if (process.argv[1] && resolve(process.argv[1]) === fileURLToPath(import.meta.url)) main().catch(error => { console.error(error); process.exitCode = 1; });

class Room {
  constructor(variant, style) {
    this.variant = variant;
    this.style = style;
    this.palette = palettes[style];
    [this.width, this.height, this.depth] = variant.size;
    this.blocks = Array(this.width * this.height * this.depth).fill('air');
    this.features = [];
  }
  index(x, y, z) { return (y * this.depth + z) * this.width + x; }
  inside(x, y, z) { return x >= 0 && x < this.width && y >= 0 && y < this.height && z >= 0 && z < this.depth; }
  get(x, y, z) { return this.inside(x, y, z) ? this.blocks[this.index(x, y, z)] : 'void'; }
  set(x, y, z, material) {
    assert(this.inside(x, y, z), `Out of bounds ${this.variant.key}: ${[x, y, z]}`);
    this.blocks[this.index(x, y, z)] = material;
  }
  box(from, to, material) {
    for (let y = from[1]; y <= to[1]; y++) for (let z = from[2]; z <= to[2]; z++) for (let x = from[0]; x <= to[0]; x++) this.set(x, y, z, material);
  }
  segment(from, to, material, radius = 0) {
    const length = Math.max(...from.map((value, axis) => Math.abs(value - to[axis])));
    for (let step = 0; step <= length; step++) {
      const position = from.map((value, axis) => Math.round(value + (to[axis] - value) * (step / (length || 1))));
      for (let offset = -radius; offset <= radius; offset++) {
        const next = [position[0] + offset, position[1], position[2]];
        if (this.inside(...next)) this.set(...next, material);
      }
    }
  }
  floorY() { return this.variant.key.includes('/layout/') ? 1 : this.variant.key === 'spawn/spawn' ? 4 : 2; }
  clearFeet(x, y, z, material = this.palette.floor, clearance = 3) {
    if (!this.inside(x, y + clearance - 1, z) || y < 1) return;
    this.set(x, y - 1, z, material);
    this.box([x, y, z], [x, y + clearance - 1, z], 'air');
  }
  route(from, to, width = 3) {
    assert.equal(from[1], to[1]);
    let [x, y, z] = from;
    const carve = () => {
      for (let sideways = -Math.floor(width / 2); sideways <= Math.floor(width / 2); sideways++) {
        const nextX = x + sideways;
        if (nextX > 0 && nextX < this.width - 1 && z > 0 && z < this.depth - 1) this.clearFeet(nextX, y, z);
      }
    };
    carve();
    while (z !== to[2]) { z += Math.sign(to[2] - z); carve(); }
    while (x !== to[0]) { x += Math.sign(to[0] - x); carve(); }
  }
  shell() {
    const floor = this.floorY();
    this.box([0, 0, 0], [this.width - 1, floor - 1, this.depth - 1], this.palette.floor);
    for (let y = floor; y < this.height; y++) {
      const material = y % 5 === 0 ? this.palette.trim : this.palette.wall;
      this.box([0, y, 0], [this.width - 1, y, 0], material);
      this.box([0, y, this.depth - 1], [this.width - 1, y, this.depth - 1], material);
      this.box([0, y, 0], [0, y, this.depth - 1], material);
      this.box([this.width - 1, y, 0], [this.width - 1, y, this.depth - 1], material);
    }
    this.box([0, this.height - 1, 0], [this.width - 1, this.height - 1, this.depth - 1], this.palette.wall);
    for (let z = 2; z < this.depth - 2; z += 4) for (const x of [2, this.width - 3]) this.set(x, floor - 1, z, this.palette.light);
  }
  gear(centerX, centerY, centerZ, radius, plane = 'yz') {
    for (let first = -radius - 1; first <= radius + 1; first++) for (let second = -radius - 1; second <= radius + 1; second++) {
      const distance = Math.sqrt(first * first + second * second);
      const tooth = (Math.abs(first) % 3 === 0 || Math.abs(second) % 3 === 0) && distance >= radius - 0.5 && distance <= radius + 1;
      if ((distance >= radius - 1.1 && distance <= radius) || tooth || (first === 0 && Math.abs(second) <= radius) || (second === 0 && Math.abs(first) <= radius)) {
        const position = plane === 'yz' ? [centerX, centerY + first, centerZ + second] : [centerX + first, centerY + second, centerZ];
        if (this.inside(...position)) this.set(...position, tooth ? 'waxed_copper_block' : this.palette.trim);
      }
    }
    this.set(centerX, centerY, centerZ, 'iron_block');
  }
  frame(x, z, height = 7) {
    const floor = this.floorY();
    this.box([x, floor, z], [x, floor + height, z], this.palette.pillar);
    this.box([x, floor + height, z], [Math.min(x + 2, this.width - 2), floor + height, z + 2], this.palette.trim);
    this.box([x, floor + 1, z + 1], [Math.min(x + 1, this.width - 2), floor + 2, z + 2], 'smooth_stone');
    this.set(x, floor + height - 1, z + 1, 'piston[facing=down,extended=false]');
    this.set(x, floor + height - 2, z + 1, 'iron_block');
  }
  rootTree(x, z, height = 8, spread = 3) {
    const floor = this.floorY();
    this.box([x, floor, z], [x, floor + height, z], 'oak_log[axis=y]');
    for (const direction of [-1, 1]) {
      this.segment([x, floor + 1, z], [Math.max(1, Math.min(this.width - 2, x + direction * spread)), floor, z + 1], 'dark_oak_log');
      this.segment([x, floor + height - 3, z], [Math.max(1, Math.min(this.width - 2, x + direction * spread)), floor + height, z + 2], 'oak_log');
    }
    this.box([Math.max(1, x - 1), floor + height, z], [Math.min(this.width - 2, x + 1), floor + height + 1, z + 2], 'moss_block');
    this.set(x, floor + height - 1, z + 1, 'shroomlight');
  }
  mushroom(x, z, height, radius = 1) {
    const floor = this.floorY();
    this.box([x, floor, z], [x, floor + height, z], 'mushroom_stem');
    this.box([x - radius, floor + height, z - radius], [x + radius, floor + height, z + radius], 'red_mushroom_block');
    this.set(x, floor + height - 1, z, 'shroomlight');
  }
}
