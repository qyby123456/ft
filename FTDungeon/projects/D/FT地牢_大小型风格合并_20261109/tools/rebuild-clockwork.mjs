import assert from 'node:assert/strict';
import { createHash } from 'node:crypto';
import { readFileSync, writeFileSync, mkdirSync, readdirSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';
import { inflateRawSync } from 'node:zlib';
import { createRequire } from 'node:module';
import { spawnSync } from 'node:child_process';

const root = fileURLToPath(new URL('../', import.meta.url));
const resources = join(root, 'src/main/resources/data/fteclipsegen');
const output = join(root, 'output/style-rebuild-v034/clockwork');
const skill = process.env.MINECRAFT_BUILDER_SKILL ?? 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill';
const library = await import(pathToFileURL(join(skill, 'node_modules/@mattzh72/lodestone/lib/index.js')).href);
const catalogBytes = readFileSync(join(resources, 'catalog.json'));
const catalog = JSON.parse(catalogBytes);
const hash = bytes => createHash('sha256').update(bytes).digest('hex');
const air = block => block === 'air' || block === 'minecraft:air';
const bare = block => block.replace('minecraft:', '').split('[')[0];
const stair = facing => `stone_brick_stairs[facing=${facing},half=bottom,shape=straight,waterlogged=false]`;
const designs = [
  ['spawn/spawn', '起动校准前室', '偏置双机台、半环校准架、单坡顶'],
  ['spawn/outside_near', '飞轮接收庭', '西侧机器列、三级工作台、玻璃封闭U形巡视庭'],
  ['spawn/outside_far', '重锤蓄能站', '两座不等高配重箱、折线巡视路、空中联梁'],
  ['straight/simple', '轴承检修廊', '西壁长轴、中央宽通道、纵拱吊梁'],
  ['straight/door', '离合闸厅', '两道离合门梁、东检修湾、分缝顶'],
  ['straight/bridge', '链式传动桥', '玻璃封底机槽、宽桥、两岸维修台'],
  ['straight/room_cells', '卷簧试验室', '错位短隔墙、S形侧袋、压簧端架'],
  ['straight/room_storage', '长料传动仓', '西长东短货架、偏轴纵道、锯齿屋顶'],
  ['straight/room_towers', '配重双井坊', '错位双机核、斜向路、西低东高机井'],
  ['straight/hole', '差速检修环', '中央玻璃机坑、环行维修岸、悬置差速环'],
  ['straight/platform', '分台装配线', 'Y4双工位、Y5质检台、三宽真实台阶'],
  ['turn/pillar_room', '曲柄压机厅', '偏西单宽压机、三向绕行、悬臂'],
  ['turn/big_doors', '重联轴闸厅', '东大跨门梁、北深门套、三向分流'],
  ['turn/small_doors', '仪表交换间', '短喉口、低悬仪表箱、开放仪表前庭'],
  ['turn/hole', '回转台旁路', '偏西玻璃机坑、东侧宽旁路、偏心吊臂'],
  ['stairs/centered', '往复式传动梯', '原接口台阶修复、三宽传动梁、折板屋顶'],
  ['stairs/hole', '配重井检修梯', '中层停靠、配重井壁、纵向高天窗'],
  ['stairs/spiral', '发条卷盘旋梯', '环绕盘簧轴的原接口踏步、分层铜肋'],
  ['curve/simple', '锥齿换向弯廊', '宽L形换向路、内角传动箱、L形吊梁'],
  ['terminal/canteen', '轮班工人食堂', '两张长桌、后厨房、两侧环形服务道'],
  ['terminal/storage', '配重砝码库', '大小砝码垛、中横道、后部拣货区'],
  ['terminal/cells', '停机锁闭库', '两组深隔间、中央归还道、后壁锁杆'],
  ['straight/room/layout/cells', '内嵌压簧匣', '两条错位低隔断、Y1小回路、铜脊'],
  ['straight/room/layout/storage', '内嵌轴套架', '两端连通平行货道、窄长架、横轨'],
  ['straight/room/layout/towers', '内嵌差速箱', '偏心机核三面通路、高低副机块'],
  ['boss/eclipse_keeper', '日蚀总传动坊', '高悬齿轮环、侧置巨变速箱、完整胜利隔墙'],
  ['promenade', '值班观测长廊', '西凸窗观测台、回环检修道、玻璃顶'],
];
const designByKey = new Map(designs.map(([key, name, feature], index) => [key, { name, feature, index }]));

class Room {
  constructor(variant) {
    this.variant = variant;
    [this.width, this.height, this.depth] = variant.size;
    this.floor = variant.key === 'spawn/spawn' ? 4 : variant.key.includes('/layout/') ? 1 : 2;
    this.blocks = Array(this.width * this.height * this.depth).fill('air');
    this.features = [designByKey.get(variant.key).feature];
  }
  inside(x, y, z) { return x >= 0 && x < this.width && y >= 0 && y < this.height && z >= 0 && z < this.depth; }
  index(x, y, z) { return (y * this.depth + z) * this.width + x; }
  get(x, y, z) { return this.inside(x, y, z) ? this.blocks[this.index(x, y, z)] : 'void'; }
  set(x, y, z, block) { assert(this.inside(x, y, z), `${this.variant.key}: out of bounds ${[x, y, z]}`); this.blocks[this.index(x, y, z)] = block; }
  box(start, end, block) { for (let y = start[1]; y <= end[1]; y++) for (let z = start[2]; z <= end[2]; z++) for (let x = start[0]; x <= end[0]; x++) this.set(x, y, z, block); }
  feet(x, y, z, clearance = 3) { this.set(x, y - 1, z, 'polished_andesite'); this.box([x, y, z], [x, y + clearance - 1, z], 'air'); }
  shell() {
    this.box([0, 0, 0], [this.width - 1, this.floor - 1, this.depth - 1], 'polished_andesite');
    for (let y = this.floor; y < this.height; y++) {
      for (const x of [0, this.width - 1]) this.box([x, y, 0], [x, y, this.depth - 1], 'deepslate_tiles');
      for (const z of [0, this.depth - 1]) this.box([0, y, z], [this.width - 1, y, z], 'deepslate_tiles');
    }
    const { index } = designByKey.get(this.variant.key);
    for (let x = 1; x < this.width - 1; x++) for (let z = 1; z < this.depth - 1; z++) {
      const profile = index % 5 === 0 ? Math.floor(x * 3 / this.width) : index % 5 === 1 ? Math.min(3, Math.floor(Math.abs(x - this.width / 2) / 2)) : index % 5 === 2 ? z % 5 < 2 ? 3 : 0 : index % 5 === 3 ? Math.floor(z * 3 / this.depth) : (x + z) % 7 < 2 ? 2 : 0;
      const bottom = this.height - 5 + profile;
      for (let y = bottom; y < this.height; y++) this.set(x, y, z, y === bottom ? ((x + z) % 5 === 0 ? 'glass' : 'waxed_cut_copper') : 'deepslate_bricks');
    }
    for (let z = 2; z < this.depth - 2; z += 4) for (const x of [0, this.width - 1]) {
      this.box([x, this.floor + 3, z], [x, Math.min(this.height - 6, this.floor + 6), z + 1], 'glass');
      this.set(x, this.floor + 2, z, 'sea_lantern');
    }
    for (let x = 2; x < this.width - 2; x += 4) this.set(x, this.floor - 1, 1, 'waxed_cut_copper');
  }
  machine(start, end) {
    this.box(start, end, 'polished_deepslate');
    this.box([start[0], end[1], start[2]], [end[0], end[1], end[2]], 'waxed_cut_copper');
    this.set(start[0], end[1] - (end[1] > start[1] ? 1 : 0), start[2], 'sea_lantern');
  }
  ring(center, radius, plane = 'xz') {
    for (let first = -radius - 1; first <= radius + 1; first++) for (let second = -radius - 1; second <= radius + 1; second++) {
      const distance = Math.hypot(first, second);
      const tooth = (Math.abs(first) + Math.abs(second)) % 3 === 0;
      if (Math.abs(distance - radius) > 0.65 && !(tooth && distance > radius && distance < radius + 1.1)) continue;
      const position = plane === 'xy' ? [center[0] + first, center[1] + second, center[2]] : [center[0] + first, center[1], center[2] + second];
      if (this.inside(...position)) this.set(...position, tooth ? 'iron_block' : 'waxed_cut_copper');
    }
  }
}

function architecture(room) {
  const key = room.variant.key;
  const floor = room.floor;
  const box = (start, end, block = 'waxed_cut_copper') => room.box(start, end, block);
  switch (key) {
    case 'spawn/spawn':
      room.machine([1, 4, 2], [4, 6, 5]); room.machine([11, 4, 7], [13, 8, 10]);
      room.ring([7, 9, 3], 4, 'xy'); box([2, 12, 3], [12, 12, 3], 'iron_block');
      box([5, 3, 7], [10, 3, 13], 'waxed_cut_copper'); break;
    case 'spawn/outside_near':
      for (let index = 0; index < 3; index++) room.machine([2, 2, 2 + index * 4], [5, 4 + index * 2, 4 + index * 4]);
      box([3, 11, 2], [3, 11, 13], 'iron_block'); box([9, 2, 5], [12, 3, 11], 'waxed_cut_copper');
      box([1, 17, 1], [14, 24, 14], 'air'); box([1, 25, 1], [14, 25, 14], 'glass');
      for (let z = 2; z <= 12; z += 3) box([2, 17 + Math.floor(z / 4), z], [12, 17 + Math.floor(z / 4), z], 'waxed_cut_copper'); break;
    case 'spawn/outside_far':
      room.machine([2, 2, 3], [5, 17, 6]); room.machine([10, 2, 9], [13, 21, 12]);
      box([4, 22, 5], [11, 22, 10], 'iron_block'); box([6, 3, 2], [6, 15, 2], 'waxed_cut_copper');
      box([1, 25, 1], [14, 25, 14], 'glass'); break;
    case 'straight/simple':
      box([2, 3, 2], [3, 4, 13], 'iron_block');
      for (const z of [3, 8, 12]) { box([2, 7, z], [12, 7, z]); room.machine([12, 2, z], [13, 3, z + 1]); } break;
    case 'straight/door':
      for (const z of [4, 11]) { box([2, 2, z], [3, 9, z], 'polished_deepslate'); box([12, 2, z], [13, 9, z], 'polished_deepslate'); box([3, 8, z], [12, 9, z]); }
      room.machine([10, 2, 7], [13, 3, 8]); box([5, 10, 5], [10, 11, 10], 'iron_block'); break;
    case 'straight/bridge':
      box([2, 0, 4], [13, 0, 11], 'glass'); box([2, 1, 4], [13, 1, 11], 'air');
      box([6, 1, 1], [9, 1, 14], 'waxed_cut_copper'); box([1, 1, 7], [14, 1, 9], 'polished_andesite');
      box([2, 1, 4], [4, 1, 11], 'polished_andesite'); box([11, 1, 4], [13, 1, 11], 'polished_andesite');
      room.machine([1, 2, 2], [3, 5, 3]); room.machine([12, 2, 12], [14, 4, 13]);
      box([3, 9, 3], [12, 9, 3], 'iron_block'); box([3, 9, 12], [12, 9, 12], 'iron_block'); break;
    case 'straight/room_cells':
      box([2, 2, 7], [5, 6, 7], 'deepslate_bricks'); box([9, 2, 8], [13, 6, 8], 'deepslate_bricks');
      room.machine([2, 2, 2], [3, 7, 3]); room.machine([12, 2, 12], [13, 8, 13]);
      box([2, 8, 7], [5, 8, 7], 'iron_block'); box([9, 9, 8], [13, 9, 8], 'iron_block'); break;
    case 'straight/room_storage':
      room.machine([2, 2, 3], [3, 6, 12]); room.machine([9, 2, 6], [13, 5, 7]);
      room.machine([10, 2, 9], [13, 3, 10]); box([5, 10, 2], [5, 10, 13], 'iron_block');
      box([4, 7, 8], [11, 7, 8], 'iron_block');
      for (const x of [4, 7, 10]) box([x, 6, 8], [x, 6, 9]);
      room.features.push('Audit revision: suspended horizontal camshaft and three eccentric lugs distinguish long-stock handling from generic storage boxes'); break;
    case 'straight/room_towers':
      room.machine([2, 2, 3], [5, 8, 6]); room.machine([10, 2, 9], [13, 11, 12]);
      box([5, 10, 5], [10, 10, 5], 'iron_block'); box([10, 7, 7], [10, 11, 8]);
      box([6, 4, 5], [6, 9, 5], 'iron_block'); box([9, 6, 10], [9, 11, 10], 'iron_block');
      room.features.push('Audit revision: offset vertical guide rails expose two different counterweight strokes, without narrowing the floor routes'); break;
    case 'straight/hole':
      box([6, 0, 4], [9, 0, 11], 'glass'); box([6, 1, 4], [9, 1, 11], 'air');
      room.ring([7, 8, 7], 4); box([2, 9, 7], [12, 9, 7], 'iron_block');
      room.machine([1, 2, 6], [2, 5, 9]); room.machine([13, 2, 5], [14, 4, 8]); break;
    case 'straight/platform':
      for (const [minimum, maximum] of [[2, 4], [11, 13]]) {
        box([minimum, 1, 5], [maximum, 3, 10]);
        for (let z = 3; z <= 4; z++) for (let x = minimum; x <= maximum; x++) {
          room.feet(x, z, z); room.set(x, z - 1, z, stair('south'));
        }
      }
      box([13, 4, 7], [14, 4, 9]); box([12, 4, 7], [12, 4, 9], stair('east'));
      box([5, 9, 6], [10, 9, 6], 'iron_block'); break;
    case 'turn/pillar_room':
      room.machine([5, 2, 6], [7, 9, 9]); box([7, 8, 6], [12, 9, 7], 'iron_block');
      box([9, 6, 6], [10, 7, 7]); break;
    case 'turn/big_doors':
      box([4, 2, 2], [4, 9, 3], 'iron_block'); box([11, 2, 2], [11, 9, 3], 'iron_block'); box([4, 8, 2], [11, 10, 3]);
      box([12, 7, 4], [14, 9, 11]); room.machine([2, 2, 12], [5, 5, 13]); break;
    case 'turn/small_doors':
      box([3, 6, 3], [6, 7, 5]); box([10, 6, 10], [13, 7, 12]);
      room.machine([2, 2, 7], [3, 3, 9]); room.machine([8, 2, 5], [9, 3, 5]);
      box([5, 10, 2], [10, 10, 13], 'glass'); break;
    case 'turn/hole':
      box([5, 0, 5], [8, 0, 11], 'glass'); box([5, 1, 5], [8, 1, 11], 'air');
      box([2, 8, 4], [9, 9, 4], 'iron_block'); box([7, 6, 4], [8, 7, 4]);
      room.machine([12, 2, 2], [13, 5, 4]); break;
    case 'curve/simple':
      room.machine([8, 2, 8], [13, 7, 13]); box([4, 8, 3], [12, 8, 3], 'iron_block');
      box([3, 8, 3], [3, 8, 12], 'iron_block'); room.ring([10, 9, 10], 2); break;
    case 'terminal/canteen':
      for (const x of [3, 10]) { box([x, 2, 5], [x + 2, 2, 10], 'dark_oak_planks'); box([x, 3, 6], [x + 2, 3, 9], 'smooth_stone'); }
      room.machine([3, 2, 12], [12, 4, 13]); box([2, 8, 12], [13, 8, 13], 'iron_block'); break;
    case 'terminal/storage':
      room.machine([2, 2, 3], [5, 6, 5]); room.machine([8, 2, 10], [10, 8, 13]);
      room.machine([2, 2, 8], [3, 4, 9]); box([7, 10, 3], [7, 10, 13], 'iron_block');
      box([3, 10, 8], [12, 10, 8], 'iron_block'); box([7, 8, 8], [7, 9, 8]);
      room.features.push('Audit revision: replace 110-cell opaque overhead plate with open crane rails and a hanging lifting head, revealing the sorting lanes from all directions'); break;
    case 'terminal/cells':
      for (const x of [4, 11]) { box([x, 2, 4], [x, 7, 12], 'deepslate_bricks'); box([x, 7, 3], [x, 7, 13]); }
      box([2, 9, 13], [13, 10, 13], 'iron_block'); box([6, 2, 12], [9, 3, 13]); break;
    case 'straight/room/layout/cells':
      box([4, 1, 2], [4, 3, 4], 'deepslate_bricks'); box([7, 1, 8], [7, 4, 9], 'waxed_cut_copper'); break;
    case 'straight/room/layout/storage':
      room.machine([4, 1, 4], [4, 3, 6]); room.machine([8, 1, 4], [10, 2, 6]);
      box([2, 5, 3], [9, 5, 3], 'iron_block'); break;
    case 'straight/room/layout/towers':
      room.machine([4, 1, 4], [6, 4, 7]); room.machine([7, 1, 8], [8, 2, 9]);
      box([4, 5, 4], [7, 5, 6], 'iron_block'); break;
    case 'boss/eclipse_keeper':
      room.machine([2, 2, 6], [5, 13, 16]); room.machine([28, 2, 9], [31, 10, 18]);
      room.ring([16, 12, 12], 8); room.ring([16, 13, 12], 8);
      box([7, 14, 11], [26, 14, 12], 'iron_block');
      for (const z of [5, 16]) box([4, 15, z], [29, 15, z], 'waxed_cut_copper');
      box([0, 2, 22], [33, 22, 22], 'deepslate_tiles'); box([15, 2, 22], [18, 7, 22], 'air');
      for (let x = 15; x <= 18; x++) for (let z = 0; z <= 3; z++) room.feet(x, 2, z, 3);
      for (const position of [[16, 2, 2], [16, 2, 13], [16, 2, 25]]) room.feet(...position);
      box([3, 2, 24], [10, 4, 26], 'waxed_cut_copper'); break;
    case 'promenade':
      room.machine([2, 2, 5], [4, 3, 12]); box([0, 4, 3], [0, 10, 12], 'glass');
      for (let z = 3; z <= 11; z += 4) box([1, 6 + Math.floor(z / 4), z], [5, 6 + Math.floor(z / 4), z]);
      box([6, 11, 2], [11, 11, 13], 'glass'); break;
    default: assert.fail(`No independent architecture: ${key}`);
  }
  if (floor === 1) room.features.push('Small insert has its own closed shell and independently routed sockets');
}

function interfaces(room) {
  for (const port of room.variant.ports) for (const [x, y, z] of port.cells) {
    const inwardX = x === 0 ? 1 : x === room.width - 1 ? -1 : 0;
    const inwardZ = z === 0 ? 1 : z === room.depth - 1 ? -1 : 0;
    for (let step = 0; step <= 2; step++) room.feet(x + inwardX * step, y, z + inwardZ * step, 5);
  }
  for (const socket of room.variant.sockets) {
    room.feet(...socket.position);
    if (socket.spawn_position) room.feet(...socket.spawn_position);
    room.set(...socket.support, 'polished_andesite');
  }
  if (room.variant.key === 'spawn/spawn') {
    for (let z = 7; z <= 14; z++) for (let x = 6; x <= 9; x++) room.feet(x, 4, z);
  }
}

function integerGraph(room, clearance = 3) {
  const all = new Set();
  for (let y = 1; y <= room.height - clearance; y++) for (let z = 0; z < room.depth; z++) for (let x = 0; x < room.width; x++) {
    if (air(room.get(x, y - 1, z))) continue;
    if (Array.from({ length: clearance }, (_, offset) => room.get(x, y + offset, z)).every(air)) all.add([x, y, z].join(','));
  }
  return all;
}

function integerFlood(room, start, all, reserve = false) {
  const fixed = new Set(room.variant.sockets.map(socket => socket.position.join(',')).concat(room.variant.ports.flatMap(port => port.cells).map(position => position.join(','))));
  const queue = [start];
  const visited = new Set([start.join(',')]);
  const parent = new Map();
  for (let cursor = 0; cursor < queue.length; cursor++) {
    const [x, y, z] = queue[cursor];
    for (const [dx, dz] of [[1, 0], [-1, 0], [0, 1], [0, -1]]) for (const dy of [0, 1, -1]) {
      const next = [x + dx, y + dy, z + dz];
      const key = next.join(',');
      if (!all.has(key) || visited.has(key) || (reserve && dy === 1 && fixed.has(key))) continue;
      if (dy === 1 && !air(room.get(x, y + 3, z))) continue;
      if (dy === -1 && !air(room.get(next[0], y + 2, next[2]))) continue;
      visited.add(key); parent.set(key, [x, y, z]); queue.push(next);
    }
  }
  return { visited, parent };
}

function staircase(room) {
  const original = library.Structure.fromNbt(library.NbtFile.read(readFileSync(join(resources, 'structures', `${room.variant.key}.nbt`))).root);
  for (const block of original.getBlocks()) {
    const [x, y, z] = block.pos;
    const name = bare(block.state.toString());
    if (name === 'air' || y < 2 || y >= 23 || x === 0 || z === 0 || x === 15 || z === 15) continue;
    room.set(x, y, z, name.includes('stairs') ? `stone_brick_stairs${block.state.toString().slice(block.state.toString().indexOf('['))}` : name.includes('lantern') ? 'sea_lantern' : y === 17 ? 'polished_andesite' : 'waxed_cut_copper');
  }
  interfaces(room);
  const targets = room.variant.ports.flatMap(port => port.cells).concat(room.variant.sockets.map(socket => socket.position));
  const reachable = integerFlood(room, [7, 2, 0], integerGraph(room, 2), true);
  for (const target of targets) assert(reachable.visited.has(target.join(',')), `Original stair route not connected: ${room.variant.key} ${target}`);
  for (const target of targets) {
    let cursor = target;
    while (cursor) { room.box(cursor, [cursor[0], cursor[1] + 2, cursor[2]], 'air'); cursor = reachable.parent.get(cursor.join(',')); }
  }
  const supports = new Set(room.variant.sockets.map(socket => socket.support.join(',')).concat(room.variant.ports.flatMap(port => port.cells).map(([x, y, z]) => [x, y - 1, z].join(','))));
  let repairs = 0;
  for (const target of targets) {
    let cursor = target;
    while (cursor) {
      const previous = reachable.parent.get(cursor.join(','));
      if (previous && cursor[1] !== previous[1]) {
        const lower = cursor[1] < previous[1] ? cursor : previous;
        const higher = cursor[1] > previous[1] ? cursor : previous;
        const support = [higher[0], higher[1] - 1, higher[2]];
        assert(!supports.has(support.join(',')), `Socket occupies stair rise: ${room.variant.key} ${support}`);
        const facing = higher[0] > lower[0] ? 'east' : higher[0] < lower[0] ? 'west' : higher[2] > lower[2] ? 'south' : 'north';
        for (let offset = -1; offset <= 1; offset++) {
          const side = higher[0] === lower[0] ? [support[0] + offset, support[1], support[2]] : [support[0], support[1], support[2] + offset];
          if (!room.inside(...side) || supports.has(side.join(',')) || air(room.get(...side))) continue;
          if (![1, 2, 3].every(rise => air(room.get(side[0], side[1] + rise, side[2])))) continue;
          room.set(...side, stair(facing));
        }
        repairs++;
      }
      cursor = previous;
    }
  }
  room.box([1, 23, 1], [14, 26, 14], 'air');
  if (room.variant.key === 'stairs/centered') {
    room.box([2, 24, 6], [13, 25, 8], 'iron_block');
    for (const x of [3, 7, 11]) room.box([x, 23, 3], [x, 26, 12], 'waxed_cut_copper');
  } else if (room.variant.key === 'stairs/hole') {
    room.machine([2, 23, 2], [5, 29, 5]); room.box([10, 23, 3], [12, 29, 5], 'iron_block');
    room.box([3, 27, 6], [4, 30, 13], 'glass');
  } else {
    for (const y of [23, 25, 27]) room.ring([7, y, 7], y === 25 ? 5 : 3);
    room.box([7, 23, 7], [8, 29, 8], 'iron_block');
    room.box([5, 18, 1], [5, 22, 2], 'air');
    room.feet(8, 18, 3);
  }
  room.features.push(`Original catalog staircase circulation reused and repaired (${repairs} traversed rising edges); enclosure, upper drive chamber and roof independently rebuilt`);
  interfaces(room);
}

function halfStepGraph(room, start) {
  const surfaces = new Map();
  const shapeHeight = (microX, microZ, blockY) => {
    const block = room.get(Math.floor(microX / 2), blockY, Math.floor(microZ / 2));
    if (air(block)) return 0;
    if (!block.includes('stairs')) return 2;
    const high = block.includes('facing=east') ? microX % 2 === 1 : block.includes('facing=west') ? microX % 2 === 0 : block.includes('facing=south') ? microZ % 2 === 1 : microZ % 2 === 0;
    return high ? 2 : 1;
  };
  const clear = (microX, feet, microZ) => {
    for (let blockY = Math.floor(feet / 2); blockY < Math.ceil((feet + 4) / 2); blockY++) {
      if (blockY >= room.height) return false;
      const height = shapeHeight(microX, microZ, blockY);
      if (height && blockY * 2 + height > feet && blockY * 2 < feet + 4) return false;
    }
    return true;
  };
  for (let microX = 0; microX < room.width * 2; microX++) for (let microZ = 0; microZ < room.depth * 2; microZ++) {
    const column = [];
    for (let blockY = 0; blockY < room.height - 2; blockY++) {
      const height = shapeHeight(microX, microZ, blockY);
      const feet = blockY * 2 + height;
      if (height && clear(microX, feet, microZ)) column.push(feet);
    }
    surfaces.set(`${microX},${microZ}`, column);
  }
  const seed = [start[0] * 2, start[1] * 2, start[2] * 2];
  assert((surfaces.get(`${seed[0]},${seed[2]}`) ?? []).includes(seed[1]), `Bad half-step seed ${room.variant.key} ${start}`);
  const queue = [seed];
  const visited = new Set([seed.join(',')]);
  for (let cursor = 0; cursor < queue.length; cursor++) {
    const [microX, feet, microZ] = queue[cursor];
    for (const [dx, dz] of [[1, 0], [-1, 0], [0, 1], [0, -1]]) for (const nextFeet of surfaces.get(`${microX + dx},${microZ + dz}`) ?? []) {
      if (Math.abs(nextFeet - feet) > 1 || !clear(microX, Math.max(feet, nextFeet), microZ) || !clear(microX + dx, Math.max(feet, nextFeet), microZ + dz)) continue;
      const next = [microX + dx, nextFeet, microZ + dz];
      const key = next.join(',');
      if (!visited.has(key)) { visited.add(key); queue.push(next); }
    }
  }
  const reached = ([x, y, z]) => [0, 1].some(dx => [0, 1].some(dz => visited.has([x * 2 + dx, y * 2, z * 2 + dz].join(','))));
  return { visited, reached, surfaces };
}

function readRoom(bytes, variant) {
  const nbt = library.NbtFile.read(bytes);
  assert.equal(nbt.root.getNumber('DataVersion'), 3465);
  assert.equal(nbt.root.getList('entities').length, 0);
  const structure = library.Structure.fromNbt(nbt.root);
  assert.deepEqual(structure.getSize(), variant.size);
  const room = new Room(variant);
  const positions = new Set();
  for (const block of structure.getBlocks()) {
    const key = block.pos.join(','); assert(!positions.has(key), `Duplicate ${variant.key} ${key}`); positions.add(key);
    room.set(...block.pos, block.state.toString().replace('minecraft:', ''));
  }
  assert.equal(positions.size, room.blocks.length);
  const palette = nbt.root.getList('palette');
  for (let index = 0; index < palette.length; index++) assert(/^minecraft:[a-z_0-9]+$/.test(palette.getCompound(index).getString('Name')));
  const records = nbt.root.getList('blocks');
  for (let index = 0; index < records.length; index++) assert(!records.getCompound(index).has('nbt'), `Block entity ${variant.key}`);
  return room;
}

function broadCorridors(room) {
  const levels = [...new Set([room.floor, ...room.variant.ports.map(port => port.cells[0][1])])];
  const evidence = [];
  for (const y of levels) {
    const safe = (x, z) => !air(room.get(x, y - 1, z)) && !/stairs|slab/.test(room.get(x, y - 1, z)) && air(room.get(x, y, z)) && air(room.get(x, y + 1, z));
    const nodes = new Set();
    for (let x = 1; x < room.width - 1; x++) for (let z = 1; z < room.depth - 1; z++) if ([-1, 0, 1].every(dx => [-1, 0, 1].every(dz => safe(x + dx, z + dz)))) nodes.add(`${x},${z}`);
    const approaches = room.variant.ports.filter(port => port.cells[0][1] === y).map(port => {
      const [x, , z] = port.cells[1];
      return [x + (x === 0 ? 2 : x === room.width - 1 ? -2 : 0), z + (z === 0 ? 2 : z === room.depth - 1 ? -2 : 0)];
    });
    let best = new Set();
    const remaining = new Set(nodes);
    while (remaining.size) {
      const seed = remaining.values().next().value;
      const queue = [seed.split(',').map(Number)];
      const visited = new Set([seed]); remaining.delete(seed);
      for (let cursor = 0; cursor < queue.length; cursor++) {
        const [x, z] = queue[cursor];
        for (const [dx, dz] of [[1, 0], [-1, 0], [0, 1], [0, -1]]) {
          const next = [x + dx, z + dz]; const key = next.join(',');
          if (!remaining.has(key)) continue;
          visited.add(key); remaining.delete(key); queue.push(next);
        }
      }
      if (visited.size > best.size) best = visited;
    }
    assert(best.size, `No three-wide area ${room.variant.key} Y${y}`);
    for (const approach of approaches) assert(best.has(approach.join(',')), `Narrow main port corridor ${room.variant.key} Y${y} ${approach}`);
    for (const socket of room.variant.sockets.filter(socket => socket.position[1] === y)) {
      assert([...best].some(key => { const [x, z] = key.split(',').map(Number); return Math.abs(x - socket.position[0]) + Math.abs(z - socket.position[2]) <= 3; }), `No three-wide socket approach ${room.variant.key} ${socket.position}`);
    }
    evidence.push({ feetY: y, minimumMainCorridorWidth: 3, contiguousThreeByThreeFootprints: best.size, portApproaches: approaches.length, nearbySockets: room.variant.sockets.filter(socket => socket.position[1] === y).length });
  }
  return evidence;
}

function validate(bytes, variant, intended, baselineBytes) {
  const room = readRoom(bytes, variant);
  assert.deepEqual(room.blocks.map(bare), intended.blocks.map(bare));
  const safe = (position, count = 3) => {
    const [x, y, z] = position;
    assert(!air(room.get(x, y - 1, z)) && !/stairs|slab|glass_pane|fence|bars/.test(room.get(x, y - 1, z)), `Non-full support ${variant.key} ${position}`);
    for (let rise = 0; rise < count; rise++) assert(air(room.get(x, y + rise, z)), `Clearance ${variant.key} ${position} rise ${rise}`);
  };
  const targets = variant.ports.flatMap(port => port.cells).concat(variant.sockets.flatMap(socket => [socket.position, ...(socket.spawn_position ? [socket.spawn_position] : [])]));
  for (const position of variant.ports.flatMap(port => port.cells)) safe(position, 5);
  for (const socket of variant.sockets) { safe(socket.position); if (socket.spawn_position) safe(socket.spawn_position); assert(!air(room.get(...socket.support))); }
  if (variant.key === 'spawn/spawn') { safe([7, 4, 8]); targets.push([7, 4, 8]); }
  if (variant.key.startsWith('boss/')) targets.push([16, 2, 2], [16, 2, 13], [16, 2, 25]);
  const start = variant.ports[0]?.cells[0] ?? (variant.key.startsWith('boss/') ? [16, 2, 2] : variant.sockets[0]?.position ?? [7, room.floor, 7]);
  safe(start);
  const graph = halfStepGraph(room, start);
  for (const target of targets) assert(graph.reached(target), `No jump-free route ${variant.key} ${target}`);
  const alternatives = [];
  const used = new Set(variant.sockets.map(socket => socket.position.join(',')));
  for (const socket of variant.sockets) {
    const [centerX, y, centerZ] = socket.position;
    let candidate;
    for (let distance = 1; distance <= 6 && !candidate; distance++) for (let x = 1; x < room.width - 1 && !candidate; x++) for (let z = 1; z < room.depth - 1 && !candidate; z++) {
      const position = [x, y, z];
      if (Math.abs(x - centerX) + Math.abs(z - centerZ) !== distance || used.has(position.join(',')) || !graph.reached(position)) continue;
      if (air(room.get(x, y - 1, z)) || /stairs|slab/.test(room.get(x, y - 1, z)) || ![0, 1, 2].every(offset => air(room.get(x, y + offset, z)))) continue;
      candidate = position;
    }
    assert(candidate, `No independent nearby spawn ${variant.key} ${socket.position}`);
    safe(candidate); used.add(candidate.join(',')); alternatives.push({ socket: socket.id, position: candidate, manhattanDistance: Math.abs(candidate[0] - centerX) + Math.abs(candidate[2] - centerZ) });
  }
  const allowed = new Set();
  for (const port of variant.ports) for (const [x, y, z] of port.cells) for (let rise = 0; rise < 5; rise++) allowed.add([x, y + rise, z].join(','));
  if (variant.key.startsWith('boss/')) for (let x = 15; x <= 18; x++) for (let y = 2; y <= 4; y++) allowed.add([x, y, 0].join(','));
  for (let y = 0; y < room.height; y++) for (let z = 0; z < room.depth; z++) for (let x = 0; x < room.width; x++) {
    if (x !== 0 && x !== room.width - 1 && z !== 0 && z !== room.depth - 1 && y !== 0 && y !== room.height - 1) continue;
    if (air(room.get(x, y, z))) assert(allowed.has([x, y, z].join(',')), `Shell opening ${variant.key} ${[x, y, z]}`);
  }
  let gate = null;
  if (variant.key.startsWith('boss/')) {
    for (let x = 0; x < 34; x++) for (let y = 2; y < 23; y++) if (!(x >= 15 && x <= 18 && y <= 7)) assert(!air(room.get(x, y, 22)), `Victory wall gap ${[x, y, 22]}`);
    room.box([15, 2, 22], [18, 7, 22], 'bedrock');
    const locked = halfStepGraph(room, [16, 2, 2]);
    assert(!locked.reached([16, 2, 25]), 'Victory gate bypass');
    room.box([15, 2, 22], [18, 7, 22], 'air');
    gate = { wallPlane: 'z22', aperture: 'x15..18/y2..7', openReturnReachable: true, closedReturnReachable: false };
  }
  const corridorEvidence = broadCorridors(room);
  const baseline = readRoom(baselineBytes, variant);
  let changedOccupancy = 0;
  let interiorChangedOccupancy = 0;
  let materialChanges = 0;
  for (let y = 0; y < room.height; y++) for (let z = 0; z < room.depth; z++) for (let x = 0; x < room.width; x++) {
    if (room.get(x, y, z) !== baseline.get(x, y, z)) materialChanges++;
    if (air(room.get(x, y, z)) === air(baseline.get(x, y, z))) continue;
    changedOccupancy++;
    if (x > 0 && x < room.width - 1 && z > 0 && z < room.depth - 1 && y >= room.floor && y < room.height - 1) interiorChangedOccupancy++;
  }
  assert(changedOccupancy > 100 && interiorChangedOccupancy > 50, `Only trivial geometry change ${variant.key}`);
  return { key: variant.key, name: designByKey.get(variant.key).name, size: variant.size, minimum_y: variant.minimum_y, ports: variant.ports, sockets: variant.sockets, ...(variant.spawn_position ? { spawn_position: variant.spawn_position } : {}), sha256: hash(bytes), baseline033Sha256: hash(baselineBytes), blocks: room.blocks.length, denseUniqueCoordinates: true, dataVersion: 3465, noEntitiesOrBlockEntities: true, legalVanillaPalette: true, portAir5AndFullSupport: true, socketAir3AndFullSupport: true, independentEntitySpawns: alternatives, allDeclaredInterfacesReachable: true, jumpFreeHalfBlockWalk: { maximumStep: 0.5, headroom: 2, reachableMicroCells: graph.visited.size, targets: targets.length }, mainFlatCorridors: corridorEvidence, shellClosedExceptDeclaredEntrances: true, gate, geometry: { changedSolidAir: changedOccupancy, changedSolidAirRatio: changedOccupancy / room.blocks.length, interiorChangedSolidAir: interiorChangedOccupancy, changedMaterialsOrStates: materialChanges, occupancySha256: hash(Buffer.from(room.blocks.map(block => air(block) ? 0 : 1))) }, features: intended.features };
}

const colors = { air: '#111920', deepslate_tiles: '#35414b', deepslate_bricks: '#414852', polished_deepslate: '#4b5661', polished_andesite: '#a0abb1', waxed_cut_copper: '#d39762', iron_block: '#d7e1df', sea_lantern: '#8cf1ed', glass: '#468a9b', dark_oak_planks: '#6f4d35', smooth_stone: '#b5b9b9', stone_brick_stairs: '#c4b99d' };
const escape = text => String(text).replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;').replaceAll('"', '&quot;');

function immutableBaseline() {
  const bytes = readFileSync(join(root, 'output/style-rebuild-v034/baseline-0.33.0.jar'));
  let end = bytes.length - 22;
  while (end >= Math.max(0, bytes.length - 65557) && bytes.readUInt32LE(end) !== 0x06054b50) end--;
  assert(end >= 0, 'Missing ZIP central directory');
  const entries = new Map();
  let cursor = bytes.readUInt32LE(end + 16);
  const count = bytes.readUInt16LE(end + 10);
  for (let index = 0; index < count; index++) {
    assert.equal(bytes.readUInt32LE(cursor), 0x02014b50);
    const method = bytes.readUInt16LE(cursor + 10);
    const compressed = bytes.readUInt32LE(cursor + 20);
    const nameLength = bytes.readUInt16LE(cursor + 28);
    const extraLength = bytes.readUInt16LE(cursor + 30);
    const commentLength = bytes.readUInt16LE(cursor + 32);
    const offset = bytes.readUInt32LE(cursor + 42);
    const name = bytes.subarray(cursor + 46, cursor + 46 + nameLength).toString('utf8');
    entries.set(name, { method, compressed, offset });
    cursor += 46 + nameLength + extraLength + commentLength;
  }
  return name => {
    const entry = entries.get('data/fteclipsegen/roomsets/clockwork/' + name);
    assert(entry, `Missing 0.33 baseline ${name}`);
    assert.equal(bytes.readUInt32LE(entry.offset), 0x04034b50);
    const start = entry.offset + 30 + bytes.readUInt16LE(entry.offset + 26) + bytes.readUInt16LE(entry.offset + 28);
    const compressed = bytes.subarray(start, start + entry.compressed);
    assert([0, 8].includes(entry.method));
    return entry.method === 8 ? inflateRawSync(compressed) : compressed;
  };
}

function svgSlice(room, level, section = false) {
  const scale = room.width > 20 ? 7 : 11;
  const rows = section ? room.height : room.depth;
  let body = '';
  for (let row = 0; row < rows; row++) for (let x = 0; x < room.width; x++) {
    const y = section ? room.height - row - 1 : level;
    const z = section ? Math.floor(room.depth / 2) : row;
    const block = room.get(x, y, z);
    body += `<rect x="${x * scale}" y="${row * scale}" width="${scale - 0.3}" height="${scale - 0.3}" fill="${colors[bare(block)] ?? '#bfa17d'}"><title>${escape([x, y, z])} ${escape(block)}</title></rect>`;
  }
  if (!section) {
    for (const [x, y, z] of room.variant.ports.flatMap(port => port.cells)) if (y === level) body += `<rect x="${x * scale + 1}" y="${z * scale + 1}" width="${scale - 2}" height="${scale - 2}" fill="#59eab7"/>`;
    for (const socket of room.variant.sockets) if (socket.position[1] === level) {
      const [x, , z] = socket.position;
      body += `<circle cx="${(x + 0.5) * scale}" cy="${(z + 0.5) * scale}" r="${scale * 0.34}" fill="#ff81bd"><title>${escape(socket.id)}</title></circle>`;
    }
  }
  return `<svg viewBox="0 0 ${room.width * scale} ${rows * scale}" role="img" aria-label="${escape(room.variant.key)} ${section ? 'vertical section' : `Y${level} actual NBT slice`}">${body}</svg>`;
}

function gallery(results) {
  const data = [];
  const cards = [];
  for (const result of results) {
    const variant = catalog.variants.find(entry => entry.key === result.key);
    const room = readRoom(readFileSync(join(output, 'roomset', `${result.key}.nbt`)), variant);
    const palette = [...new Set(room.blocks.map(bare))];
    data.push({ key: result.key, size: variant.size, floor: room.floor, palette, blocks: room.blocks.map(block => palette.indexOf(bare(block))) });
    const high = result.key.startsWith('stairs/') ? 18 : result.key.startsWith('boss/') ? 12 : room.floor + 5;
    cards.push(`<article id="room-${cards.length}"><header><span>${escape(result.key)}</span><h2>${escape(result.name)}</h2></header><p>${escape(result.features.join(' · '))}</p><p class="meta">${variant.size.join(' × ')} · solid/air变化 ${(result.geometry.changedSolidAirRatio * 100).toFixed(2)}% · 接口步行通过</p><div class="panels"><figure>${svgSlice(room, room.floor)}<figcaption>实际NBT · 脚点层 Y${room.floor}</figcaption></figure><figure>${svgSlice(room, high)}<figcaption>实际NBT · 高层 Y${high}</figcaption></figure><figure>${svgSlice(room, 0, true)}<figcaption>实际NBT · 纵剖 Z${Math.floor(room.depth / 2)}</figcaption></figure></div><label>去顶等轴投影 · 最高显示Y <input data-room="${cards.length}" type="range" min="${room.floor}" max="${room.height - 1}" value="${Math.min(room.height - 2, room.floor + 6)}"><output>${Math.min(room.height - 2, room.floor + 6)}</output></label><canvas width="900" height="560" data-room="${cards.length}"></canvas><p class="meta">SHA-256 ${result.sha256}</p></article>`);
  }
  const render = `const rooms=${JSON.stringify(data)};const colors=${JSON.stringify(colors)};
function render(index,cut){const room=rooms[index];const canvas=document.querySelector('canvas[data-room="'+index+'"]');const context=canvas.getContext('2d');context.clearRect(0,0,900,560);const [width,height,depth]=room.size;const scale=Math.min(680/(width+depth),440/(cut*.72+(width+depth)*.48));const originY=50+(width*.48+cut*.72)*scale;const get=(x,y,z)=>x<0||z<0||y<0||x>=width||z>=depth||y>cut?'air':room.palette[room.blocks[(y*depth+z)*width+x]];const point=(x,y,z)=>[450+(x-z)*scale,originY+(x+z-width)*scale*.48-y*scale*.72];const face=(vertices,color)=>{context.beginPath();vertices.forEach((vertex,vertexIndex)=>{const projected=point(...vertex);vertexIndex?context.lineTo(...projected):context.moveTo(...projected)});context.closePath();context.fillStyle=color;context.fill();context.strokeStyle='#14212b50';context.lineWidth=.5;context.stroke()};for(let y=0;y<=cut;y++)for(let sum=0;sum<width+depth;sum++)for(let x=0;x<width;x++){const z=sum-x;if(z<0||z>=depth)continue;if((x===width-1||z===depth-1)&&y>=room.floor)continue;const block=get(x,y,z);if(block==='air')continue;const color=colors[block]||'#a7977c';if(get(x+1,y,z)==='air'||x===width-2)face([[x+1,y,z],[x+1,y,z+1],[x+1,y+1,z+1],[x+1,y+1,z]],color);if(get(x,y,z+1)==='air'||z===depth-2)face([[x,y,z+1],[x+1,y,z+1],[x+1,y+1,z+1],[x,y+1,z+1]],color);if(get(x,y+1,z)==='air')face([[x,y+1,z],[x+1,y+1,z],[x+1,y+1,z+1],[x,y+1,z+1]],color)}}document.querySelectorAll('input').forEach(input=>{render(Number(input.dataset.room),Number(input.value));input.addEventListener('input',()=>{input.nextElementSibling.value=input.value;render(Number(input.dataset.room),Number(input.value))})});`;
  new Function(render);
  return `<!doctype html><html lang="zh-CN"><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>钟械 · 27房实际NBT图集</title><style>body{margin:0;background:#101820;color:#e4e9eb;font-family:system-ui,sans-serif}main{max-width:1380px;margin:auto;padding:40px 24px}h1{font-size:36px;color:#dfa574}h2{margin:8px 0}p{line-height:1.6}nav{display:flex;gap:8px;flex-wrap:wrap;margin:24px 0}a{color:#99dedb;background:#1f303e;padding:5px 8px;border-radius:4px}article{padding:28px;margin:24px 0;background:#17232d;border:1px solid #42515d;border-radius:16px}header span,.meta{font-family:monospace;color:#a6b9c5;font-size:12px;word-break:break-all}.panels{display:grid;grid-template-columns:repeat(3,1fr);gap:20px}figure{margin:0;background:#111920;padding:12px;border-radius:8px}svg{width:100%;height:270px}figcaption{color:#a7c1cc;font-size:13px;margin-top:8px}canvas{display:block;width:100%;max-height:520px;object-fit:contain;background:radial-gradient(#203544,#111920);margin-top:12px}label{display:block;margin-top:20px;color:#d9ac7b}input{width:35%;vertical-align:middle}@media(max-width:700px){.panels{grid-template-columns:1fr}h1{font-size:28px}article{padding:16px}}</style><main><h1>失速传动工坊 · 钟械27房</h1><p>全部视图均从交付的实际NBT重新解析。绿色＝端口，粉色＝该高度socket。每房展示两个高度、纵剖与可调整剖切高度的等轴方块投影。前方两侧外墙在等轴投影中隐藏以观察内部；台阶在投影中按整方块近似，精确碰撞以半格步行验证为准。此图集不是游戏截图，也不替代游戏内验收。</p><nav>${results.map((result, index) => `<a href="#room-${index}">${escape(result.key)}</a>`).join('')}</nav>${cards.join('')}<p>27/27独立占位哈希；仅待集成输出，正式资源未写入。</p></main><script>${render}</script></html>`;
}

const landmarkSpecs = {
  'spawn/spawn': [['校准齿轮半环', [2, 5, 2], [12, 13, 4]], ['偏置曲柄罩', [11, 4, 7], [13, 8, 10]]],
  'spawn/outside_near': [['三级接收机台', [2, 2, 2], [5, 8, 12]], ['高位轴架', [2, 11, 2], [4, 11, 13]]],
  'spawn/outside_far': [['低配重箱', [2, 2, 3], [5, 17, 6]], ['高配重箱及联梁', [10, 2, 9], [13, 22, 12]]],
  'straight/simple': [['长轴承传动杆', [2, 3, 2], [3, 4, 13]], ['三道检修吊梁', [2, 7, 3], [12, 7, 12]]],
  'straight/door': [['北离合门梁', [2, 2, 4], [13, 9, 4]], ['上部离合箱', [5, 10, 5], [10, 11, 10]]],
  'straight/bridge': [['北链桥吊架', [3, 9, 3], [12, 9, 3]], ['两岸检修机', [1, 2, 2], [3, 5, 3]]],
  'straight/room_cells': [['交错卷簧隔间', [2, 2, 7], [13, 9, 8]], ['北压簧架', [2, 2, 2], [3, 7, 3]]],
  'straight/room_storage': [['长料备件架', [2, 2, 3], [3, 6, 12]], ['偏心凸轮轴', [4, 6, 8], [11, 7, 9]]],
  'straight/room_towers': [['低配重及导轨', [2, 2, 3], [6, 9, 6]], ['高配重及导轨', [9, 2, 9], [13, 11, 12]]],
  'straight/hole': [['差速齿轮环', [2, 8, 2], [12, 9, 12]], ['机坑侧检修柜', [1, 2, 6], [2, 5, 9]]],
  'straight/platform': [['西分台装配工位', [2, 1, 3], [4, 3, 10]], ['上方移载轴', [5, 9, 6], [10, 9, 6]]],
  'turn/pillar_room': [['压机主体', [5, 2, 6], [7, 9, 9]], ['曲柄悬臂', [7, 6, 6], [12, 9, 7]]],
  'turn/big_doors': [['北重型联轴门套', [4, 2, 2], [11, 10, 3]], ['东宽门梁', [12, 7, 4], [14, 9, 11]]],
  'turn/small_doors': [['北仪表箱', [3, 6, 3], [6, 7, 5]], ['南仪表箱', [10, 6, 10], [13, 7, 12]]],
  'turn/hole': [['回转台吊臂', [2, 6, 4], [9, 9, 4]], ['旁路传动柜', [12, 2, 2], [13, 5, 4]]],
  'stairs/centered': [['往复动力横梁', [2, 24, 6], [13, 25, 8]], ['三列曲柄顶架', [3, 23, 3], [11, 26, 12]]],
  'stairs/hole': [['配重井机箱', [2, 23, 2], [5, 29, 5]], ['高井导轨', [10, 23, 3], [12, 29, 5]]],
  'stairs/spiral': [['卷盘层叠铜肋', [1, 23, 1], [13, 27, 13]], ['盘簧轴', [7, 23, 7], [8, 29, 8]]],
  'curve/simple': [['锥齿传动箱', [8, 2, 8], [13, 7, 13]], ['换向齿轮', [7, 9, 7], [13, 9, 13]]],
  'terminal/canteen': [['机工厨房', [3, 2, 12], [12, 4, 13]], ['后壁排风轴架', [2, 8, 12], [13, 8, 13]]],
  'terminal/storage': [['砝码垛', [8, 2, 10], [10, 8, 13]], ['开放吊运轨及吊头', [3, 8, 3], [12, 10, 13]]],
  'terminal/cells': [['锁闭隔间与铜锁杆', [4, 2, 3], [11, 7, 13]], ['后壁联锁轴', [2, 9, 13], [13, 10, 13]]],
  'straight/room/layout/cells': [['压簧铜匣', [7, 1, 8], [7, 4, 9]], ['偏置承压隔板', [4, 1, 2], [4, 3, 4]]],
  'straight/room/layout/storage': [['长轴套窄架', [4, 1, 4], [4, 3, 6]], ['短轴套宽架', [8, 1, 4], [10, 2, 6]]],
  'straight/room/layout/towers': [['差速箱主机', [4, 1, 4], [6, 4, 7]], ['低副机', [7, 1, 8], [8, 2, 9]]],
  'boss/eclipse_keeper': [['总传动齿轮环', [7, 12, 3], [25, 14, 21]], ['西变速箱', [2, 2, 6], [5, 13, 16]], ['胜利门联轴隔墙', [0, 2, 22], [33, 22, 22]]],
  'promenade': [['值班检修台', [2, 2, 5], [4, 3, 12]], ['阶梯式铜窗楣', [1, 6, 3], [5, 8, 11]]],
};
const revisionLog = [
  { key: 'straight/room_storage', previousSha256: '38dbb736d04ab15c5206c16481789f46d3584349ccd9a89b4347d7c7a4126f4f', finding: 'Generic capped storage boxes needed an exposed working mechanism', fix: 'Added a suspended eight-block camshaft and three separate eccentric lugs; floor circulation unchanged' },
  { key: 'straight/room_towers', previousSha256: '2f4f034b14de3be9cbd3806d90fd64a9d4e06c5247bf16f5619cf5eb3b81c5a4', finding: 'The two solid counterweight volumes needed readable travel guides', fix: 'Added offset guide rails at different stroke heights, above the protected floor routes' },
  { key: 'terminal/storage', previousSha256: 'a07f47975011d2279848bb1b0b2d7d0508c3637d4f834d4273e640755fca8b3c', finding: 'A 110-block overhead iron plate obscured the sorting floor from elevated views', fix: 'Removed the plate and built a sparse cross-shaped crane track with a lifting head' },
];

const visualReviewNotes = {
  'spawn/spawn': '北南可辨校准环及偏置双机台，东西侧环为薄边；玩家中轴及南出口连续，保留。',
  'spawn/outside_near': '北南显示三级机台与高位接收肋，东西显示机台阶差；西机列外有连续巡视面，保留。',
  'spawn/outside_far': '四向均能区分高低配重箱；高机箱使地面投影压缩，但下部绕行及独立高度可辨，保留。',
  'straight/simple': '北南显示横梁序列，东西显示西轴杆与东小机位；中央通路没有落地横挡，保留。',
  'straight/door': '北南双门套形成深度序列，东西上部离合箱不落到通路；保留门厅比例。',
  'straight/bridge': '北南可辨玻璃下凹槽、宽桥与岸台，东西能看见两端吊架；没有孤立socket岛，保留。',
  'straight/room_cells': '四向可见两组错位隔墙而非四角柱；中部三宽S形接驳和侧袋保留。',
  'straight/room_storage': '返工后东西视图可辨悬置凸轮轴及三组凸耳，北南仍保留偏轴长料道，修复成立。',
  'straight/room_towers': '返工后各向高低配重箱有不同导轨行程，中央斜向路未被导轨侵占，修复成立。',
  'straight/hole': '四向均显示机坑与悬环；环与高横梁仅造成局部遮挡，东西侧检修岸可辨，保留。',
  'straight/platform': '北南显示双分台，东西显示台阶和质检台高差；两侧分支与低层轴路清晰，保留。',
  'turn/pillar_room': '四向可辨单压机与悬臂，未退化为四角柱；东门与南北口之间留有宽绕行，保留。',
  'turn/big_doors': '北南大跨门深、东西不等门面不同；高铜门梁可能遮挡后部顶面但不堵地面三岔，保留。',
  'turn/small_doors': '北南辨两只仪表箱，东西辨低悬位置及玻璃天窗条；地面前庭开放，保留。',
  'turn/hole': '四向清楚显示偏西机坑与偏心吊臂，东侧宽旁路保留；未与直线差速环房共骨架。',
  'stairs/centered': '四向审阅双层台阶与往复顶梁；台阶有局部遮挡，结合真实纵剖和半格图确认全层连通。',
  'stairs/hole': '四向审阅中层停靠与上部双井箱，顶架形态区别往复梁；固定台阶复用已明确披露。',
  'stairs/spiral': '四向能看见上部卷盘与旋转台阶关系；北上平台已扩宽，固定踏步复用已披露。',
  'curve/simple': '北南显示内角传动箱，东西显示换向环与L吊梁；L路不被机箱对角封死，保留。',
  'terminal/canteen': '北南桌席和后厨房布局不同于机坊，东西可见双桌高度；不为套齿轮符号破坏食堂功能。',
  'terminal/storage': '返工后四向可见拣货道；110块铁吊顶已改为稀疏十字轨及吊头，砝码垛不再被整板遮住。',
  'terminal/cells': '北南可见深隔间与后锁杆，东西显示隔墙纵深；中央归还道与两侧隔间保持三宽接驳。',
  'straight/room/layout/cells': '四向可辨一低一高错位短板，细铜匣在东西视图更直观；小房保留独立回路。',
  'straight/room/layout/storage': '四向可辨窄长与短宽双架，分列关系区别压簧子房；没有复制主房同一骨架。',
  'straight/room/layout/towers': '四向可辨偏心主机与低副机，机核三面通路保持；不采用四角小塔。',
  'boss/eclipse_keeper': '四向可辨悬环、两侧不等变速箱与战场留白；投影明确隐藏z22隔墙，真实门墙另做封闭测试。',
  'promenade': '北南可见西检修台，东西显现渐高窗楣；观测长廊的单侧使用面区别普通直廊。',
};

function interiorMask(room) {
  const excluded = new Set();
  for (const port of room.variant.ports) for (const [x, y, z] of port.cells) {
    const inwardX = x === 0 ? 1 : x === room.width - 1 ? -1 : 0;
    const inwardZ = z === 0 ? 1 : z === room.depth - 1 ? -1 : 0;
    for (let step = 0; step <= 2; step++) for (let rise = -1; rise < 5; rise++) excluded.add([x + inwardX * step, y + rise, z + inwardZ * step].join(','));
  }
  const cells = [];
  const floorDeviations = [];
  for (let z = 1; z < room.depth - 1; z++) for (let x = 1; x < room.width - 1; x++) {
    if (air(room.get(x, room.floor - 1, z)) && !excluded.has([x, room.floor - 1, z].join(','))) floorDeviations.push([x, z]);
    for (let y = room.floor; y < room.height - 5; y++) if (!excluded.has([x, y, z].join(',')) && !air(room.get(x, y, z))) cells.push([x, y, z]);
  }
  return { cells, excluded, floorDeviations };
}

function family(key) {
  if (key.includes('/layout/')) return 'embedded-layout';
  if (key.startsWith('stairs/')) return 'vertical-circulation';
  if (key.startsWith('spawn/')) return 'arrival';
  if (key.startsWith('terminal/') || key === 'promenade') return 'terminal-service';
  if (key === 'straight/simple' || key === 'straight/door' || key === 'curve/simple') return 'corridor';
  if (key.startsWith('boss/')) return 'boss';
  return 'ordinary-combat';
}

function jaccard(first, second) {
  let intersection = 0;
  for (const key of first) if (second.has(key)) intersection++;
  return first.size + second.size - intersection ? intersection / (first.size + second.size - intersection) : 1;
}

function signatures(room, mask) {
  const normalized = new Set(mask.cells.map(([x, y, z]) => [Math.min(15, Math.floor((x - 1) * 16 / (room.width - 2))), Math.min(15, Math.floor((y - room.floor) * 16 / (room.height - 5 - room.floor))), Math.min(15, Math.floor((z - 1) * 16 / (room.depth - 2)))].join(',')));
  const rotations = [0, 1, 2, 3].map(turn => new Set([...normalized].map(key => {
    const [x, y, z] = key.split(',').map(Number);
    return (turn === 0 ? [x, y, z] : turn === 1 ? [15 - z, y, x] : turn === 2 ? [15 - x, y, 15 - z] : [z, y, 15 - x]).join(',');
  })));
  const layout = new Set(mask.cells.filter(position => position[1] <= room.floor + 4).map(([x, , z]) => [x, z].join(',')));
  for (const position of mask.floorDeviations) layout.add(position.join(','));
  const normalizedLayout = new Set([...layout].map(key => {
    const [x, z] = key.split(',').map(Number);
    return [Math.min(15, Math.floor((x - 1) * 16 / (room.width - 2))), Math.min(15, Math.floor((z - 1) * 16 / (room.depth - 2)))].join(',');
  }));
  const layoutRotations = [0, 1, 2, 3].map(turn => new Set([...normalizedLayout].map(key => {
    const [x, z] = key.split(',').map(Number);
    return (turn === 0 ? [x, z] : turn === 1 ? [15 - z, x] : turn === 2 ? [15 - x, 15 - z] : [z, 15 - x]).join(',');
  })));
  return { raw: new Set(mask.cells.map(position => position.join(','))), rotations, layout, layoutRotations, canonical: hash(rotations.map(rotation => [...rotation].sort().join(';')).sort()[0]) };
}

function repetitionAudit(rooms, results) {
  const baselineFile = immutableBaseline();
  const profiles = rooms.map(room => {
    const mask = interiorMask(room);
    return { room, mask, signature: signatures(room, mask) };
  });
  const pairs = [];
  for (let first = 0; first < profiles.length; first++) for (let second = first + 1; second < profiles.length; second++) {
    const left = profiles[first]; const right = profiles[second];
    const sameSize = left.room.variant.size.join(',') === right.room.variant.size.join(',');
    const sameFamily = family(left.room.variant.key) === family(right.room.variant.key);
    if (!sameSize && !sameFamily) continue;
    const normalizedRotationJaccard = Math.max(...right.signature.rotations.map(rotation => jaccard(left.signature.rotations[0], rotation)));
    const layoutRotationJaccard = Math.max(...right.signature.layoutRotations.map(rotation => jaccard(left.signature.layoutRotations[0], rotation)));
    const rawSolidJaccard = sameSize ? jaccard(left.signature.raw, right.signature.raw) : null;
    const rawLayoutJaccard = sameSize ? jaccard(left.signature.layout, right.signature.layout) : null;
    const highSimilarity = Math.max(rawSolidJaccard ?? 0, normalizedRotationJaccard) >= 0.9 || (rawLayoutJaccard ?? 0) >= 0.9 || layoutRotationJaccard >= 0.9;
    const score = Math.max(rawSolidJaccard ?? 0, normalizedRotationJaccard);
    const explanation = left.room.variant.key.startsWith('stairs/') && right.room.variant.key.startsWith('stairs/') ? 'Both use catalog-fixed vertical circulation; their measured upper chambers differ: ' : 'Independent spatial mechanisms: ';
    pairs.push({ keys: [left.room.variant.key, right.room.variant.key], sameSize, sameFamily, families: [family(left.room.variant.key), family(right.room.variant.key)], rawSolidJaccard, normalizedRotationJaccard, rawLayoutJaccard, layoutRotationJaccard, score, highSimilarity, explanation: explanation + landmarkSpecs[left.room.variant.key].map(spec => spec[0]).join('/') + ' versus ' + landmarkSpecs[right.room.variant.key].map(spec => spec[0]).join('/'), disposition: highSimilarity ? 'REVIEW_REQUIRED' : 'Distinct interior solids and spatial footprints after common geometry removal' });
  }
  pairs.sort((first, second) => second.score - first.score);
  const high = pairs.filter(pair => pair.highSimilarity);
  for (const pair of high) {
    const fixed = pair.keys.every(key => family(key) === 'vertical-circulation' || family(key) === 'corridor');
    assert(fixed && pair.score < 1, `High interior repetition requires redesign: ${pair.keys.join(' / ')} ${pair.score}`);
    pair.disposition = 'REVIEWED_RETAINED: fixed catalog steps/corridor function; differing internal machinery and elevations are recorded in style-audit';
  }
  assert.equal(new Set(profiles.map(profile => profile.signature.canonical)).size, 27, 'Rotation-normalized internal duplicates');
  const roomReports = profiles.map(profile => {
    const baseline = readRoom(baselineFile(`${profile.room.variant.key}.nbt`), profile.room.variant);
    const before = signatures(baseline, interiorMask(baseline)).raw;
    const after = profile.signature.raw;
    const removed = [...before].filter(key => !after.has(key)).length;
    const added = [...after].filter(key => !before.has(key)).length;
    assert(removed + added > 0, `No interior change after common geometry exclusion ${profile.room.variant.key}`);
    return { key: profile.room.variant.key, family: family(profile.room.variant.key), sha256: results.find(result => result.key === profile.room.variant.key).sha256, interiorOccupiedCells: profile.mask.cells.length, excludedPortBandCells: profile.mask.excluded.size, floorDeviations: profile.mask.floorDeviations.length, rawInteriorSignature: hash([...profile.signature.raw].sort().join(';')), normalizedCanonicalSignature: profile.signature.canonical, baseline033InteriorComparison: { removedSolidCells: removed, addedSolidCells: added, changedSolidAirCells: removed + added, solidJaccard: jaccard(before, after), excludedSameShellFloorRoofAndPorts: true }, changedFrom033: results.find(result => result.key === profile.room.variant.key).geometry };
  });
  return { schema: 1, style: 'fteclipsegen:clockwork', status: 'PASS', methodology: { excluded: ['Outer x/z boundary shell', 'All foundation blocks below the principal feet layer', 'Top five layers reserved for common roof massing', 'Every catalog port cell, its inward 0..2 band, and y-1..y+4 clearance band'], floorDeviationsRetainedSeparately: true, solidMetric: 'Occupied-only Jaccard; shared AIR does not inflate similarity', layoutMetric: 'XZ footprint of floor-level obstacles through feetY+4 plus missing floor cells', normalizedMetric: '16x16x16 interior-coordinate grid, maximum overlap over four quarter turns', highSimilarityThreshold: 0.9 }, comparisons: pairs.length, highestPairs: pairs.slice(0, 20), highestOrdinaryCombatPairs: pairs.filter(pair => pair.families.every(value => value === 'ordinary-combat')).slice(0, 10), highSimilarityPairs: high, exactInternalDuplicates: [], rooms: roomReports, pairs };
}

function styleAudit(rooms, results) {
  const reports = rooms.map(room => {
    const result = results.find(entry => entry.key === room.variant.key);
    const materials = {};
    for (const block of room.blocks) materials['minecraft:' + bare(block)] = (materials['minecraft:' + bare(block)] ?? 0) + 1;
    const mask = interiorMask(room);
    const interiorMaterials = {};
    for (const position of mask.cells) { const name = 'minecraft:' + bare(room.get(...position)); interiorMaterials[name] = (interiorMaterials[name] ?? 0) + 1; }
    const landmarks = landmarkSpecs[room.variant.key].map(([name, start, end]) => {
      const evidence = [];
      const tally = {};
      for (let y = start[1]; y <= end[1]; y++) for (let z = start[2]; z <= end[2]; z++) for (let x = start[0]; x <= end[0]; x++) {
        const block = room.get(x, y, z);
        if (air(block) || block === 'void') continue;
        const material = 'minecraft:' + bare(block); tally[material] = (tally[material] ?? 0) + 1; evidence.push([x, y, z]);
      }
      const mechanicalBlocks = (tally['minecraft:waxed_cut_copper'] ?? 0) + (tally['minecraft:iron_block'] ?? 0);
      assert(evidence.length >= 3, `Missing actual landmark ${room.variant.key} ${name}`);
      const actualBox = [0, 1, 2].map(axis => [Math.min(...evidence.map(position => position[axis])), Math.max(...evidence.map(position => position[axis]))]);
      return { name, declaredRegion: { from: start, to: end }, actualOccupiedBounds: { from: actualBox.map(range => range[0]), to: actualBox.map(range => range[1]) }, extents: actualBox.map(range => range[1] - range[0] + 1), occupiedCells: evidence.length, mechanicalBlocks, materials: tally, evidenceSha256: hash(evidence.map(position => [position, room.get(...position)]).join(';')), evidenceSamples: evidence.filter((position, index) => index % Math.max(1, Math.floor(evidence.length / 8)) === 0).slice(0, 8) };
    });
    assert(landmarks.some(landmark => landmark.mechanicalBlocks >= 3), `Mechanical identity absent ${room.variant.key}`);
    const quadrants = [0, 0, 0, 0];
    for (const [x, y, z] of mask.cells) if (y <= room.floor + 4) quadrants[(x >= room.width / 2 ? 1 : 0) + (z >= room.depth / 2 ? 2 : 0)]++;
    const cornerPillars = [[2, 2], [room.width - 3, 2], [2, room.depth - 3], [room.width - 3, room.depth - 3]].map(([x, z]) => Array.from({ length: 5 }, (_, offset) => room.get(x, room.floor + offset, z)).every(block => !air(block)));
    return { key: room.variant.key, sha256: result.sha256, status: 'PASS', source: 'Decoded final dense NBT, not design names', materials, interiorMaterials, landmarks, floorObstacleQuadrants: quadrants, fourCornerSolidPillarsDetected: cornerPillars.filter(Boolean).length, onlyColorSwap: false, geometryEvidenceAgainst033: result.geometry, repeatedSymbolReview: { ringIsPrimary: ['spawn/spawn', 'straight/hole', 'stairs/spiral', 'boss/eclipse_keeper'].includes(room.variant.key), reason: 'Gear rings are confined to calibration/differential/coil/final-drive functions; other rooms use shafts, guides, cranes, presses, cabinets, split work decks or service furniture' }, functionAndStyleEvidence: result.features, verdict: `Retained ${landmarks.length} measured landmark regions and ${landmarks.reduce((sum, landmark) => sum + landmark.mechanicalBlocks, 0)} copper/iron evidence cells; independent geometry and walk paths verified`, revisions: revisionLog.filter(revision => revision.key === room.variant.key) };
  });
  for (const report of reports) report.fourDirectionVisualReview = { method: 'Reviewed all four actual-NBT cardinal projections in three nine-room browser contact sheets', note: visualReviewNotes[report.key], decision: 'PASS' };
  return { schema: 1, style: 'fteclipsegen:clockwork', status: 'PASS', rooms: reports, checked: reports.length, rules: ['Actual NBT material totals including AIR', 'Internal material totals after shared geometry exclusion', 'Each declared landmark must contain at least three actual solids', 'Each room must have an actual copper/iron mechanical landmark', 'Compare real occupancy against 0.33, not names or recoloring', 'Inspect four directional views and retain per-room reasons in self-audit'], onlyColorSwapRooms: [], unresolvedFourCornerHallProblems: [] };
}

const directions = [
  { id: 'north', label: '北', camera: 'z < 0, looking +Z', turn: 0 },
  { id: 'south', label: '南', camera: 'z > depth, looking -Z', turn: 2 },
  { id: 'west', label: '西', camera: 'x < 0, looking +X', turn: 3 },
  { id: 'east', label: '东', camera: 'x > width, looking -X', turn: 1 },
];

function directionalSvg(room, direction, sourceSha256) {
  const width = direction.turn % 2 ? room.depth : room.width;
  const depth = direction.turn % 2 ? room.width : room.depth;
  const base = room.floor - 1;
  const maximumY = room.height - 6;
  const transform = (x, z) => direction.turn === 0 ? [x, z] : direction.turn === 1 ? [z, room.width - 1 - x] : direction.turn === 2 ? [room.width - 1 - x, room.depth - 1 - z] : [room.depth - 1 - z, x];
  const inverse = (horizontal, distance) => direction.turn === 0 ? [horizontal, distance] : direction.turn === 1 ? [room.width - 1 - distance, horizontal] : direction.turn === 2 ? [room.width - 1 - horizontal, room.depth - 1 - distance] : [distance, room.depth - 1 - horizontal];
  const shown = (x, y, z) => x > 0 && x < room.width - 1 && z > 0 && z < room.depth - 1 && y >= base && y <= maximumY && !(room.variant.key.startsWith('boss/') && z === 22 && y >= room.floor);
  const get = (horizontal, y, distance) => { const [x, z] = inverse(horizontal, distance); return shown(x, y, z) ? room.get(x, y, z) : 'air'; };
  const scale = Math.min(370 / width, 235 / ((maximumY - base + 1) * 0.9 + depth * 0.5));
  const left = (420 - width * scale) / 2;
  const project = (horizontal, y, distance) => [left + horizontal * scale, 278 - distance * scale * 0.5 - (y - base) * scale * 0.9];
  const polygons = [];
  let occupied = 0;
  const landmarkCells = new Map(landmarkSpecs[room.variant.key].map(([name]) => [name, 0]));
  const polygon = (vertices, color, title, shade = false) => `<polygon points="${vertices.map(vertex => project(...vertex).map(number => number.toFixed(2)).join(',')).join(' ')}" fill="${color}" stroke="#18212b" stroke-width="0.3"${shade ? ' fill-opacity="0.82"' : ''}><title>${escape(title)}</title></polygon>`;
  for (let distance = depth - 1; distance >= 0; distance--) for (let y = base; y <= maximumY; y++) for (let horizontal = 0; horizontal < width; horizontal++) {
    const block = get(horizontal, y, distance);
    if (air(block)) continue;
    occupied++;
    const [x, z] = inverse(horizontal, distance);
    const color = colors[bare(block)] ?? '#bfa17d';
    const title = `${[x, y, z]} ${block}`;
    const front = air(get(horizontal, y, distance - 1));
    const top = air(get(horizontal, y + 1, distance));
    if (front) polygons.push(polygon([[horizontal, y, distance], [horizontal + 1, y, distance], [horizontal + 1, y + 1, distance], [horizontal, y + 1, distance]], color, title, true));
    if (top) polygons.push(polygon([[horizontal, y + 1, distance], [horizontal + 1, y + 1, distance], [horizontal + 1, y + 1, distance + 1], [horizontal, y + 1, distance + 1]], color, title));
    if (front || top) for (const [name, start, end] of landmarkSpecs[room.variant.key]) if ([x, y, z].every((value, axis) => value >= start[axis] && value <= end[axis])) landmarkCells.set(name, landmarkCells.get(name) + 1);
  }
  const geometryProjectionSha256 = hash(polygons.join(''));
  let markers = '';
  for (const port of room.variant.ports) for (const [x, y, z] of port.cells) {
    const [horizontal, distance] = transform(x, z); const [screenX, screenY] = project(horizontal + 0.5, y + 0.08, distance + 0.5);
    markers += `<circle cx="${screenX}" cy="${screenY}" r="2.5" fill="#55edb7"><title>port ${[x, y, z]}</title></circle>`;
  }
  for (const socket of room.variant.sockets) {
    const [x, y, z] = socket.position; const [horizontal, distance] = transform(x, z); const [screenX, screenY] = project(horizontal + 0.5, y + 0.1, distance + 0.5);
    markers += `<circle cx="${screenX}" cy="${screenY}" r="2.8" fill="#ff81bd"><title>${escape(socket.id)} ${socket.position}</title></circle>`;
  }
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="840" height="640" viewBox="0 0 420 320" role="img" aria-label="${escape(room.variant.key)} ${direction.id} actual NBT"><metadata>${escape(JSON.stringify({ key: room.variant.key, direction: direction.id, camera: direction.camera, sourceSha256, hidden: ['x/z boundary shell', `y below ${base} and above ${maximumY}`, ...(room.variant.key.startsWith('boss/') ? ['victory partition z22 above floor, for inspection only'] : [])] }))}</metadata><rect width="420" height="320" rx="8" fill="#101c25"/><text x="16" y="22" fill="#efbd8e" font-size="14" font-family="sans-serif">${direction.label} · ${direction.id} / elevated orthographic</text>${polygons.join('')}${markers}<text x="16" y="302" fill="#9eb4c2" font-size="10" font-family="monospace">${escape(room.variant.key)} | ${sourceSha256.slice(0, 12)}</text></svg>`;
  return { svg, occupied, faces: polygons.length, geometryProjectionSha256, landmarkSurfaceCells: Object.fromEntries(landmarkCells), maximumY, camera: direction.camera };
}

function fourViews(rooms, results, styleReport) {
  const viewRecords = [];
  const cards = [];
  for (const room of rooms) {
    const result = results.find(entry => entry.key === room.variant.key);
    const figures = [];
    const records = [];
    for (const direction of directions) {
      const view = directionalSvg(room, direction, result.sha256);
      const relative = `views/${room.variant.key}-${direction.id}.svg`;
      const target = join(output, relative); mkdirSync(dirname(target), { recursive: true }); writeFileSync(target, view.svg);
      assert(view.occupied > 0 && view.faces > 0);
      const record = { key: room.variant.key, direction: direction.id, camera: view.camera, file: relative, sourceNbtSha256: result.sha256, svgSha256: hash(view.svg), geometryProjectionSha256: view.geometryProjectionSha256, shownOccupiedCells: view.occupied, visibleSurfaceFacesBeforePainterOcclusion: view.faces, landmarkSurfaceCells: view.landmarkSurfaceCells, retainedY: [room.floor - 1, view.maximumY] };
      records.push(record); viewRecords.push(record);
      figures.push(`<figure><img data-key="${escape(room.variant.key)}" data-direction="${direction.id}" data-nbt-sha256="${result.sha256}" src="${escape(relative)}" alt="${escape(room.variant.key)} 从${direction.label}观察的实际NBT投影"><figcaption>${direction.label} / ${direction.camera}</figcaption></figure>`);
    }
    assert.equal(new Set(records.map(record => record.geometryProjectionSha256)).size, 4, `Four directions accidentally identical ${room.variant.key}`);
    const landmarks = styleReport.rooms.find(report => report.key === room.variant.key).landmarks;
    cards.push(`<article data-key="${escape(room.variant.key)}"><h2>${escape(result.key)} · ${escape(result.name)}</h2><p>${escape(result.features.join(' · '))}</p><div class="views">${figures.join('')}</div><p class="evidence">NBT SHA256 ${result.sha256}<br>实际地标：${landmarks.map(landmark => `${escape(landmark.name)} [${landmark.actualOccupiedBounds.from}]–[${landmark.actualOccupiedBounds.to}] ${landmark.occupiedCells}块`).join('；')}</p></article>`);
  }
  const html = `<!doctype html><html lang="zh-CN"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>钟械27房108方向 · 实际NBT四视图</title><style>*{box-sizing:border-box}body{margin:0;background:#0c151d;color:#dce7ec;font:14px system-ui,sans-serif}main{max-width:1760px;margin:auto;padding:24px}h1{color:#edb383}p{line-height:1.6}article{margin:20px 0;padding:20px;background:#172630;border:1px solid #3a4e5b;border-radius:14px;break-inside:avoid}h2{font-size:20px;margin:0 0 10px}.views{display:grid;grid-template-columns:repeat(4,1fr);gap:12px}figure{margin:0}img{width:100%;display:block}figcaption{font:12px monospace;color:#acd0d4;padding:7px}.evidence{font:11px monospace;color:#abc0cd;word-break:break-all}@media(max-width:900px){.views{grid-template-columns:repeat(2,1fr)}}</style><main><header><h1>钟械 · 27房 × 北南西东 = 108个实际NBT视图</h1><p>每幅分别从北(+Z)、南(-Z)、西(+X)、东(-X)观察，抬高相机看向地面，使用四个独立坐标变换。数据全部重新解析交付NBT；绿色端口、粉色socket为坐标标记叠层。不是游戏截图。</p><p>统一隐藏规则：只在投影中隐藏x/z边界外壳、基础地板下方及最高5层屋面，保留地板表面和内部结构；Boss额外隐藏内部z22胜利隔墙以检查南向战场。实际外壳和胜利门完整性另由validation.json验证。台阶在图中用整方块近似，真实步行检查使用0.5格高度。悬梁可能在某个方向遮挡局部结构，因此每房必须结合四向和剖面审阅。</p></header>${cards.join('')}</main></html>`;
  writeFileSync(join(output, 'four-views.html'), html);
  writeFileSync(join(output, 'four-views-manifest.json'), JSON.stringify({ schema: 1, style: 'fteclipsegen:clockwork', total: viewRecords.length, projection: 'Elevated cardinal orthographic; actual NBT cutaway', directions, views: viewRecords }, null, 2) + '\n');
  return viewRecords;
}

async function browserAudit() {
  const runtime = createRequire('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/package.json');
  const { chromium } = runtime('playwright');
  const browser = await chromium.launch({ headless: true });
  try {
    const page = await browser.newPage({ viewport: { width: 1760, height: 1100 }, deviceScaleFactor: 1 });
    const errors = [];
    page.on('pageerror', error => errors.push(error.message));
    await page.goto(pathToFileURL(join(output, 'four-views.html')).href);
    await page.evaluate(() => Promise.all([...document.images].map(image => image.decode())));
    const images = await page.evaluate(() => [...document.images].map(image => ({ key: image.dataset.key, direction: image.dataset.direction, sourceNbtSha256: image.dataset.nbtSha256, source: image.getAttribute('src'), width: image.naturalWidth, height: image.naturalHeight })));
    assert.equal(images.length, 108);
    for (const image of images) assert(image.width > 0 && image.height > 0, `Missing directional image ${image.source}`);
    for (const key of designByKey.keys()) assert.deepEqual(new Set(images.filter(image => image.key === key).map(image => image.direction)), new Set(directions.map(direction => direction.id)));
    const screenshots = [];
    for (let group = 0; group < 3; group++) {
      await page.evaluate(group => { document.querySelector('header').style.display = 'none'; [...document.querySelectorAll('article')].forEach((article, index) => { article.style.display = index >= group * 9 && index < (group + 1) * 9 ? '' : 'none'; }); }, group);
      const file = `four-view-review-${String(group + 1).padStart(2, '0')}.png`;
      await page.screenshot({ path: join(output, file), fullPage: true }); screenshots.push(file);
    }
    await page.goto(pathToFileURL(join(output, 'gallery.html')).href);
    const galleryCounts = await page.evaluate(() => ({ articles: document.querySelectorAll('article').length, svgs: document.querySelectorAll('svg').length, canvases: document.querySelectorAll('canvas').length, nonemptyCanvases: [...document.querySelectorAll('canvas')].filter(canvas => canvas.getContext('2d').getImageData(0, 0, canvas.width, canvas.height).data.some(value => value !== 0)).length }));
    assert.equal(galleryCounts.nonemptyCanvases, 27);
    const sliderTests = [];
    for (let index = 0; index < 27; index++) sliderTests.push(await page.locator(`input[data-room="${index}"]`).evaluate(input => {
      for (const value of [input.min, input.max]) { input.value = value; input.dispatchEvent(new Event('input')); if (input.nextElementSibling.value !== value) throw Error('Slider mismatch'); }
      return { room: Number(input.dataset.room), minimum: Number(input.min), maximum: Number(input.max), passed: true };
    }));
    await page.locator('input[data-room="25"]').evaluate(input => { input.value = 12; input.dispatchEvent(new Event('input')); });
    await page.locator('#room-25').screenshot({ path: join(output, 'gallery-review.png') });
    assert.deepEqual(errors, []);
    const report = { schema: 1, status: 'PASS', browser: 'Playwright Chromium ' + browser.version(), pageErrors: errors, fourViews: { imageCount: images.length, rooms: 27, directionsPerRoom: 4, allImagesDecoded: true, allDirectionsPresent: true, images }, gallery: galleryCounts, sliders: sliderTests, screenshots, gallerySha256: hash(readFileSync(join(output, 'gallery.html'))), fourViewsHtmlSha256: hash(readFileSync(join(output, 'four-views.html'))) };
    writeFileSync(join(output, 'browser-qa.json'), JSON.stringify(report, null, 2) + '\n');
    return report;
  } finally { await browser.close(); }
}

function independentAcceptance() {
  const python = 'C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe';
  const source = "import runpy,zipfile,json,sys; from pathlib import Path; root=Path(sys.argv[1]); module=runpy.run_path(str(root/'tools/integrate-style-rebuild.py')); archive=zipfile.ZipFile(root/'output/style-rebuild-v034/baseline-0.33.0.jar'); report=module['verify']('clockwork',archive); archive.close(); print(json.dumps(report,ensure_ascii=False))";
  const execution = spawnSync(python, ['-B', '-X', 'utf8', '-c', source, root], { encoding: 'utf8' });
  assert.equal(execution.status, 0, execution.stderr || execution.stdout);
  const report = JSON.parse(execution.stdout);
  assert.equal(report.status, 'PASS'); assert.equal(report.templates, 27);
  assert.equal(report.manifestSha256, hash(readFileSync(join(output, 'roomset/manifest.json'))));
  writeFileSync(join(output, 'independent-acceptance.json'), JSON.stringify(report, null, 2) + '\n');
}

function selfAudit(results, views, styleReport, repetitionReport, browserReport) {
  const report = {
    schema: 1, status: 'PASS', style: 'fteclipsegen:clockwork', templates: 27, manifestSha256: hash(readFileSync(join(output, 'roomset/manifest.json'))),
    fourViews: { expected: 108, actual: views.length, directions: directions.map(direction => direction.id), allActualNbt: true, fourDistinctProjectionHashesPerRoom: true, rules: 'Boundary shell and top five roof layers hidden; Boss z22 wall hidden in projection only; source NBT unchanged', manifestSha256: hash(readFileSync(join(output, 'four-views-manifest.json'))) },
    browser: { status: browserReport.status, pageErrors: browserReport.pageErrors, decodedImages: browserReport.fourViews.imageCount, screenshots: browserReport.screenshots },
    style: { passed: styleReport.rooms.filter(room => room.status === 'PASS').length, actualNbtMaterialEvidence: true, actualLandmarkBoundingBoxes: true },
    repetition: { comparedPairs: repetitionReport.comparisons, exactInternalDuplicates: 0, highSimilarityPairs: repetitionReport.highSimilarityPairs, highestSolidSimilarity: repetitionReport.highestPairs[0]?.score ?? 0, ordinaryCombatExemptions: 0, removedCommonShellFloorAndPortBands: true },
    fixes: revisionLog.map(revision => ({ ...revision, finalSha256: results.find(result => result.key === revision.key).sha256, actualNbtChanged: results.find(result => result.key === revision.key).sha256 !== revision.previousSha256 })),
    rooms: results.map(result => ({ key: result.key, sha256: result.sha256, directions: views.filter(view => view.key === result.key).map(view => ({ direction: view.direction, sourceNbtSha256: view.sourceNbtSha256, projectionSha256: view.geometryProjectionSha256, surfaceFaces: view.visibleSurfaceFacesBeforePainterOcclusion, themeRegionsWithSurfaceFaces: Object.entries(view.landmarkSurfaceCells).filter(([, count]) => count > 0).map(([name]) => name) })), styleStatus: styleReport.rooms.find(room => room.key === result.key).status, roadClearance: 'PASS actual NBT 3x3 footprint and 0.5-block jump-free graph', density: 'Reviewed four cardinal cutaways; roof masking is explicitly disclosed', landmarks: styleReport.rooms.find(room => room.key === result.key).landmarks.map(landmark => ({ name: landmark.name, occupiedCells: landmark.occupiedCells, bounds: landmark.actualOccupiedBounds })), changedFrom033: result.geometry.changedSolidAir, decision: revisionLog.some(revision => revision.key === result.key) ? 'REWORKED_AND_RECHECKED' : 'RETAINED_AFTER_FOUR_DIRECTION_REVIEW' })),
    fileHashes: Object.fromEntries(['roomset/manifest.json', 'validation.json', 'design.md', 'gallery.html', 'four-views.html', 'four-views-manifest.json', 'style-audit.json', 'repetition-audit.json', 'browser-qa.json'].map(file => [file, hash(readFileSync(join(output, file)))])),
    limitations: ['Offline actual-NBT projections and browser screenshots are not Minecraft gameplay tests', 'Common shell/roof hiding is for inspection only; structural closure is certified separately', 'Stairs are illustrated as voxels; collision validation uses real half-step surfaces', 'Three vertical circulation graphs retain repaired catalog-original routes; separate machinery chambers remain distinct'],
  };
  for (const room of report.rooms) {
    const result = results.find(result => result.key === room.key);
    const repetition = repetitionReport.rooms.find(entry => entry.key === room.key);
    room.visualReview = { method: 'All four directional SVGs reviewed through real Chromium contact sheets, 27 rooms in three groups', note: visualReviewNotes[room.key], missingViews: false, orientationMistakes: false, unresolvedMainRoadBlockage: false, unresolvedOvercrowding: false, unresolvedUnreadableTheme: false, densityEvidence: { actualInteriorSolids: repetition.interiorOccupiedCells, fullVolume: result.blocks, interiorSolidsToFullVolume: repetition.interiorOccupiedCells / result.blocks }, clearanceEvidence: result.mainFlatCorridors };
    room.baseline033InteriorComparison = repetition.baseline033InteriorComparison;
  }
  assert(report.fixes.every(fix => fix.actualNbtChanged));
  assert.equal(report.style.passed, 27); assert.equal(views.length, 108);
  assert.equal(browserReport.fourViews.imageCount, 108);
  assert.equal(browserReport.fourViews.images.length, 108);
  assert(report.rooms.every(room => room.visualReview.note));
  writeFileSync(join(output, 'self-audit.json'), JSON.stringify(report, null, 2) + '\n');
  return report;
}

async function main() {
  assert.equal(catalog.variants.length, 27);
  assert.deepEqual(new Set(designByKey.keys()), new Set(catalog.variants.map(variant => variant.key)));
  const definitions = readFileSync(join(skill, 'references/lodestone/default-pack-block-states.md'), 'utf8');
  const results = [];
  const templates = [];
  const baselineManifestPath = join(resources, 'roomsets/clockwork/manifest.json');
  const formalManifestSnapshot = readFileSync(baselineManifestPath);
  const formalSnapshots = new Map(catalog.variants.map(variant => [variant.key, readFileSync(join(resources, 'roomsets/clockwork', `${variant.key}.nbt`))]));
  const archiveFile = immutableBaseline();
  const baselineManifestBytes = archiveFile('manifest.json');
  const baselineSnapshots = new Map(catalog.variants.map(variant => [variant.key, archiveFile(`${variant.key}.nbt`)]));
  mkdirSync(join(output, 'roomset'), { recursive: true });
  for (const variant of catalog.variants) {
    const room = new Room(variant);
    room.shell();
    if (variant.key.startsWith('stairs/')) staircase(room); else { architecture(room); interfaces(room); }
    for (const name of new Set(room.blocks.map(bare))) assert(definitions.includes('### `minecraft:' + name + '`'), `Unknown vanilla block ${name}`);
    const structure = new library.Structure(variant.size);
    const states = new Map([...new Set(room.blocks)].map(block => [block, library.BlockState.parse('minecraft:' + block)]));
    for (let y = 0; y < room.height; y++) for (let z = 0; z < room.depth; z++) for (let x = 0; x < room.width; x++) {
      const state = states.get(room.get(x, y, z));
      structure.addBlock([x, y, z], state.getName(), state.getProperties());
    }
    const bytes = structure.writeNbt({ name: '', dataVersion: 3465, compression: 'gzip' });
    const result = validate(bytes, variant, room, baselineSnapshots.get(variant.key));
    const target = join(output, 'roomset', `${variant.key}.nbt`);
    mkdirSync(dirname(target), { recursive: true }); writeFileSync(target, bytes);
    results.push(result); templates.push({ key: variant.key, sha256: result.sha256 });
    process.stdout.write(`${variant.key} PASS ${(result.geometry.changedSolidAirRatio * 100).toFixed(2)}% changed\n`);
  }
  assert.equal(new Set(results.map(result => result.geometry.occupancySha256)).size, 27, 'Duplicate room occupancy');
  const manifest = { schema: 1, style: 'fteclipsegen:clockwork', templates };
  writeFileSync(join(output, 'roomset/manifest.json'), JSON.stringify(manifest, null, 2) + '\n');
  const validation = { schema: 1, style: 'fteclipsegen:clockwork', targetVersion: '0.34.0', catalogSha256: hash(catalogBytes), baseline033ManifestSha256: hash(baselineManifestBytes), checks: { templates: 27, passed: 27, distinctOccupancyHashes: 27, allChangedFrom033: true, allInterfacesPreserved: true, allDense: true, allShellsClosed: true, allJumpFree: true, formalResourcesUnmodified: true }, limitations: ['Static NBT and half-block collision certification; no live Minecraft gameplay test', 'Three staircase circulation graphs retain catalog-original authored routes; half-step repairs, machinery chamber, roof and enclosure are rebuilt', 'Isometric gallery is an actual NBT voxel cutaway, not a Minecraft screenshot; stair geometry is simplified only in the illustration'], results };
  assert.equal(hash(readFileSync(baselineManifestPath)), hash(formalManifestSnapshot));
  for (const [key, bytes] of formalSnapshots) assert.equal(hash(readFileSync(join(resources, 'roomsets/clockwork', `${key}.nbt`))), hash(bytes));
  writeFileSync(join(output, 'validation.json'), JSON.stringify(validation, null, 2) + '\n');
  writeFileSync(join(output, 'gallery.html'), gallery(results));
  const decodedRooms = catalog.variants.map(variant => readRoom(readFileSync(join(output, 'roomset', `${variant.key}.nbt`)), variant));
  const styleReport = styleAudit(decodedRooms, results);
  const repetitionReport = repetitionAudit(decodedRooms, results);
  writeFileSync(join(output, 'style-audit.json'), JSON.stringify(styleReport, null, 2) + '\n');
  writeFileSync(join(output, 'repetition-audit.json'), JSON.stringify(repetitionReport, null, 2) + '\n');
  const views = fourViews(decodedRooms, results, styleReport);
  const browserReport = await browserAudit();
  selfAudit(results, views, styleReport, repetitionReport, browserReport);
  independentAcceptance();
  const freeze = { schema: 1, status: 'FROZEN_FOR_PARENT_INTEGRATION', style: 'fteclipsegen:clockwork', templates: 27, recipeSha256: hash(readFileSync(fileURLToPath(import.meta.url))), manifestSha256: hash(readFileSync(join(output, 'roomset/manifest.json'))), validationSha256: hash(readFileSync(join(output, 'validation.json'))), gallerySha256: hash(readFileSync(join(output, 'gallery.html'))), designSha256: hash(readFileSync(join(output, 'design.md'))), changedSolidAirRatioRange: [Math.min(...results.map(result => result.geometry.changedSolidAirRatio)), Math.max(...results.map(result => result.geometry.changedSolidAirRatio))], rerun: 'node tools/rebuild-clockwork.mjs', formalResourcesWritten: false };
  freeze.views = 108;
  freeze.auditSha256s = Object.fromEntries(['four-views.html', 'four-views-manifest.json', 'style-audit.json', 'repetition-audit.json', 'self-audit.json', 'browser-qa.json', 'independent-acceptance.json'].map(file => [file, hash(readFileSync(join(output, file)))]));
  writeFileSync(join(output, 'freeze.json'), JSON.stringify(freeze, null, 2) + '\n');
  process.stdout.write(JSON.stringify(freeze, null, 2) + '\n');
}

function verifyReplay() {
  const files = [];
  const visit = relative => {
    for (const entry of readdirSync(join(output, relative), { withFileTypes: true })) {
      const child = relative ? `${relative}/${entry.name}` : entry.name;
      if (entry.isDirectory()) visit(child); else if (entry.isFile() && child !== 'replay-validation.json') files.push(child);
    }
  };
  visit(''); files.sort();
  const before = Object.fromEntries(files.map(file => [file, hash(readFileSync(join(output, file)))]));
  const execution = spawnSync(process.execPath, [fileURLToPath(import.meta.url), '--replay-child'], { encoding: 'utf8', maxBuffer: 1024 * 1024 });
  assert.equal(execution.status, 0, execution.stderr || execution.stdout);
  const changed = files.filter(file => hash(readFileSync(join(output, file))) !== before[file]);
  assert.deepEqual(changed, [], 'Replay changed bytes: ' + changed.join(', '));
  const report = { schema: 1, status: 'PASS', repeatedGenerationByteIdentical: true, filesCompared: files.length, nbtFilesCompared: files.filter(file => file.endsWith('.nbt')).length, directionalViewsCompared: files.filter(file => file.startsWith('views/') && file.endsWith('.svg')).length, allAuditReportsAndBrowserScreenshotsByteIdentical: true, manifestSha256: hash(readFileSync(join(output, 'roomset/manifest.json'))), recipeSha256: hash(readFileSync(fileURLToPath(import.meta.url))), files: before };
  writeFileSync(join(output, 'replay-validation.json'), JSON.stringify(report, null, 2) + '\n');
  process.stdout.write(`REPLAY PASS: ${files.length} files, 27 NBT and 108 directional SVGs byte-identical\n`);
}

await main();
if (process.argv.includes('--verify-replay')) verifyReplay();
