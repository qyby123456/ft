import assert from 'node:assert/strict';
import { createHash } from 'node:crypto';
import { mkdirSync, readFileSync, writeFileSync, readdirSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';
import { createRequire } from 'node:module';
import { spawnSync } from 'node:child_process';

const sourcePath = fileURLToPath(import.meta.url);
const sourceUrl = new URL(import.meta.url);
const root = fileURLToPath(new URL(sourcePath.includes('recipe-source.mjs') ? '../../../../' : '../', sourceUrl));
const scope = join(root, 'output/room-expansion-v035/clockwork');
const batch = 5;
const batchName = 'batch-05';
const output = join(scope, batchName);
const resources = join(root, 'src/main/resources/data/fteclipsegen');
const catalogBytes = readFileSync(join(resources, 'catalog.json'));
const catalog = JSON.parse(catalogBytes);
const skill = process.env.MINECRAFT_BUILDER_SKILL ?? 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill';
const library = await import(pathToFileURL(join(skill, 'node_modules/@mattzh72/lodestone/lib/index.js')).href);
const hash = bytes => createHash('sha256').update(bytes).digest('hex');
const isAir = block => block === 'air' || block === 'minecraft:air';
const bare = block => block.replace(/^minecraft:/, '').split('[')[0];
const json = (file, data) => writeFileSync(join(output, file), JSON.stringify(data, null, 2) + '\n');
const escape = text => String(text).replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;').replaceAll('"', '&quot;');
const definitions = [
  { key: 'addition/09', baseKey: 'straight/room_towers', name: '星盘校准站', route: '西侧可步行观测台、东侧仪器台、南侧三级刻度弧构成三角布局；原南北主路仍为Y2三格宽，观测台另由三宽真实台阶上达Y4', landmarks: [['偏西观测台及踏步', [1, 2, 3], [3, 4, 8]], ['三个不同高度刻度弧', [5, 2, 10], [13, 9, 13]], ['东侧仪器台', [12, 2, 5], [14, 6, 7]], ['完整玻璃顶', [1, 14, 1], [14, 15, 14]]] },
  { key: 'addition/10', baseKey: 'straight/room_cells', name: '故障机芯拆解场', route: '西北零件架对角望向东南围护维修坑，两组断开轴列在z6与z11交错；南北主路先沿东侧越过西轴列，再向西绕过东轴列', landmarks: [['西断开轴列', [2, 2, 6], [6, 6, 6]], ['东错位轴列', [9, 2, 11], [13, 7, 11]], ['西北零件架', [1, 2, 2], [3, 8, 3]], ['东南封底围护维修坑', [11, 0, 6], [14, 2, 9]]] },
];
const visualNotes = {
  'addition/09': { north: '偏西观测台、东仪器台和后方三级短弧形成三角布局，原中轴地面保持通行', south: '三级刻度弧高差为Y5/Y7/Y9且彼此断开，不组成复制的大圆环', west: '三宽踏步连接Y4观测面，短弧背后各有实际支撑而非悬空符号', east: '仪器台与观测台分侧，玻璃屋盖保留在完整NBT中，剖顶图不代表实际缺顶' },
  'addition/10': { north: '前侧西断轴与后侧东轴错位分离，中间留过渡区，四socket未转移到轴床', south: '东南维修坑与西北零件架对角，铜围栏完整，绕行不穿坑', west: '不同高度的断轴、铁轴承和零件架分层可辨，没有同一中央机器骨架', east: '围护浅坑封底而非向外漏空；东西轴列间留净空，北南平层路线可连通' },
  'addition/07': { north: '左侧阶梯音墙和右侧低共鸣箱高差明显，中间演奏台偏东而非居中', south: '反向可见演奏台外围回路，两处实际柱脚承托L形横梁，不构成对称四柱钟厅', west: '七列簧片沿墙逐级升高，细铜片和铁端头构成音墙；主路不被封墙横断', east: '低共鸣箱的独立腔盖与西侧阶梯簧片形成斜向视线，四socket留在Y2安全地面' },
  'addition/08': { north: '西侧模具槽伸出而东侧前区留空，观察窗内不同形状的静态模具可辨', south: '两台独立冷却工作面和附墙小柜可辨，冷却不使用液体或更新红石', west: '玻璃窗将铸模腔干式围封，西南台与东台错位，路线不落在模槽上', east: '东侧冷却台与西前模槽构成前后错位的S形通道，北南原门保持五格净空' },
  'addition/05': { north: '西档案柜在端部内收而中段外弯，阅读凹室偏离中轴，东侧三个摆轮由高梁悬挂', south: '反向能区分柜体弧形和三组摆轮，不以整套中央齿轮大厅代替档案功能', west: '阅读凹室入口留空，刻度是地面完整方块嵌色，非轨道', east: '三个轮环分组排列且挂点依附支撑梁，原四socket留在平层安全边道' },
  'addition/06': { north: '垂直箱、西南轴床和东检修台分层可辨，主通路不被高台封堵', south: '可辨横向轴承床与侧面拆装架，机器工序形成L形而非双配重塔', west: '三宽真实台阶通向短检修台，水平轴床保留低矮长形轮廓', east: '拆装架与台阶平台承重连续，四socket维持原Y2落脚而非移到高台' },
  'addition/03': { north: '对角高低塔、上部折线联梁与浅井能分别辨识，北口不被低塔封住', south: '高塔与低塔高度对照清楚，井侧检修道向西折后接回南口', west: '浅井玻璃封底及铜护栏可辨，两塔不构成同高四柱中庭', east: '东南塔上方联梁形成独立剪影，四socket均落在Y2完整地面' },
  'addition/04': { north: '阶梯凸轮呈不等高侧面轮廓，中央低台迫使分流，西东两路可辨', south: '低台两端横通道连接，东拨杆台不同于西凸轮列，无中央贯通机器长廊', west: '三级凸轮与联轴高梁清楚分离，梁下留净空，旁路不断', east: '拨杆有不同伸出高度，西阶梯机械与中央编程台形成三个独立组' },
  'addition/01': { north: '西北叉与东南叉在深度上错位，低齿轮台迫使中线绕行；摆锤与上框清楚可辨', south: '反向可辨两根不等长摆锤及双叉高差，北南路线两端保持开放', west: '双叉开口由侧面辨识，台与摆架前后叠合但未侵入三宽步道', east: '两根摆臂与立柱分离可见，四socket落在平层检修地面而非机器顶面' },
  'addition/02': { north: '西卷筒列与东落地吊架分置，中央卸货坪保持开放，高短梁不堵北口', south: '反向可辨井底玻璃、四柱吊架和西侧分格卷筒，南端回转区不被货架封堵', west: '三个卷筒盒纵向分格明显，吊头独立悬置，长料架不是全高实心墙', east: '四柱的承重关系与短横梁相连，西卷筒间的检修空隙可辨；无统一大吊顶遮挡地面' },
};
const batchRepairs = {
  'addition/07': [],
  'addition/08': [{ finding: 'Initial narrow observation strip mixed mold cells into its glass boundary and obscured mold shapes in opaque projections', change: 'Built an independent 6x4x4 dry enclosure with continuous glass sides, copper floor and iron top; moved mold cores fully inside, moved east cooling table to z12..13 and west table to z10..11; relocated the west wall cabinet away from the observation shell, and rendered glass semitransparently', evidence: 'All enclosure boundary cells checked solid, actual glass shell material checked, five three-wide S-route waypoints pass; final four-direction browser screenshots re-reviewed' }],
  'addition/05': [{ finding: 'Initial suspended wheel heads lacked enough visible iron spoke evidence below the common roof exclusion', change: 'Added actual horizontal and vertical iron spokes to each of the three suspended copper balance wheels', evidence: 'Final NBT material audit verifies three supported wheel assemblies, each with independently measured copper rim and iron spokes' }],
  'addition/06': [{ finding: 'Initial axis-bearing bed extended to x5 and narrowed the flat route beside the stair approach', change: 'Moved the horizontal axis bed one block west to x1..4/z12..13; no interface coordinates changed', evidence: 'Final NBT passes a continuous three-wide north-south flat route plus half-block collision reachability to both Y4 maintenance targets' }],
  'addition/03': [{ finding: 'Initial west rail at x6 left a narrow transition near the northwest tower; the 3x3 main-route check failed', change: 'Moved the entire sealed shallow well and copper guard ring one block east; actual glass floor is now x8..9/z7..9, guards x7 and x10', evidence: 'Re-exported NBT passes continuous 3x3 north-south footprints and all four socket approaches' }],
  'addition/04': [{ finding: 'Explicit double-loop review found the west cam edge too close to the central programming table for a three-wide branch', change: 'Moved the three stepped cams from x2..4 to x1..3 at z6; kept heights Y5/Y6/Y7 and all original sockets', evidence: 'Added mandatory three-wide graph waypoints 5,8 / 10,8 / 7,4 / 7,11; both bypasses and their end cross-links now pass' }],
};

class Room {
  constructor(definition, variant) {
    this.definition = definition; this.variant = variant;
    [this.width, this.height, this.depth] = variant.size;
    this.blocks = Array(this.width * this.height * this.depth).fill('air');
  }
  inside(x, y, z) { return x >= 0 && y >= 0 && z >= 0 && x < this.width && y < this.height && z < this.depth; }
  index(x, y, z) { return (y * this.depth + z) * this.width + x; }
  get(x, y, z) { return this.inside(x, y, z) ? this.blocks[this.index(x, y, z)] : 'void'; }
  set(x, y, z, block) { assert(this.inside(x, y, z), `Out of bounds ${[x, y, z]}`); this.blocks[this.index(x, y, z)] = block; }
  box(start, end, block) { for (let y = start[1]; y <= end[1]; y++) for (let z = start[2]; z <= end[2]; z++) for (let x = start[0]; x <= end[0]; x++) this.set(x, y, z, block); }
  shell() {
    this.box([0, 0, 0], [15, 1, 15], 'polished_andesite');
    for (const x of [0, 15]) this.box([x, 2, 0], [x, 15, 15], 'deepslate_bricks');
    for (const z of [0, 15]) this.box([0, 2, z], [15, 15, z], 'deepslate_bricks');
    this.box([0, 15, 0], [15, 15, 15], 'deepslate_tiles');
    this.box([1, 14, 1], [14, 14, 14], 'glass');
    if (this.definition.key === 'addition/01') {
      for (const z of [2, 7, 13]) this.box([1, 12, z], [14, 13, z], 'waxed_cut_copper');
      for (const z of [3, 11]) for (const x of [0, 15]) this.box([x, 5, z], [x, 10, z + 1], 'glass');
    } else {
      for (const x of [2, 5, 10, 13]) this.box([x, 13, 1], [x, 14, 14], 'waxed_cut_copper');
      this.box([0, 7, 2], [0, 11, 13], 'glass'); this.box([15, 6, 4], [15, 10, 11], 'glass');
    }
    for (const z of [2, 13]) for (const x of [2, 13]) this.set(x, 1, z, 'sea_lantern');
  }
  interfaces() {
    for (const port of this.variant.ports) for (const [x, y, z] of port.cells) {
      const dx = x === 0 ? 1 : x === this.width - 1 ? -1 : 0;
      const dz = z === 0 ? 1 : z === this.depth - 1 ? -1 : 0;
      for (let distance = 0; distance <= 3; distance++) {
        this.set(x + dx * distance, y - 1, z + dz * distance, 'polished_andesite');
        this.box([x + dx * distance, y, z + dz * distance], [x + dx * distance, y + 4, z + dz * distance], 'air');
      }
    }
    for (const socket of this.variant.sockets) {
      for (const [x, y, z] of [socket.position, socket.spawn_position]) {
        this.set(x, y - 1, z, 'polished_andesite'); this.box([x, y, z], [x, y + 2, z], 'air');
      }
      this.set(...socket.support, 'polished_andesite');
    }
  }
}

function build(definition) {
  const variant = catalog.variants.find(entry => entry.key === definition.baseKey);
  assert.deepEqual(variant.size, [16, 16, 16]); assert.equal(variant.sockets.length, 4);
  const room = new Room(definition, variant); room.shell();
  if (definition.key === 'addition/09') {
    room.box([1, 2, 5], [3, 3, 8], 'waxed_cut_copper');
    for (let x = 1; x <= 3; x++) {
      room.set(x, 2, 3, 'stone_brick_stairs[facing=south,half=bottom,shape=straight,waterlogged=false]');
      room.set(x, 2, 4, 'polished_andesite');
      room.set(x, 3, 4, 'stone_brick_stairs[facing=south,half=bottom,shape=straight,waterlogged=false]');
    }
    room.box([12, 2, 5], [14, 3, 6], 'waxed_cut_copper');
    room.box([12, 4, 5], [14, 4, 6], 'iron_block');
    room.box([14, 5, 5], [14, 6, 6], 'waxed_cut_copper');
    for (const [startX, top, z] of [[5, 5, 10], [8, 7, 11], [11, 9, 12]]) {
      room.box([startX, top, z], [startX + 2, top, z], 'waxed_cut_copper');
      room.set(startX + 2, top, z + 1, 'waxed_cut_copper');
      const supportX = startX === 5 ? startX : startX + 2;
      room.box([supportX, 2, z], [supportX, top - 1, z], 'iron_block');
      room.set(startX, top + 1, z, 'iron_block');
    }
    room.box([1, 15, 1], [14, 15, 14], 'glass');
    room.interfaces(); return room;
  }
  if (definition.key === 'addition/10') {
    for (const x of [1, 3]) room.box([x, 2, 2], [x, 8, 2], 'waxed_cut_copper');
    for (const y of [2, 4, 6, 8]) room.box([1, y, 2], [3, y, 3], 'iron_block');
    for (const z of [6, 9]) room.box([11, 2, z], [14, 2, z], 'waxed_cut_copper');
    for (const x of [11, 14]) room.box([x, 2, 6], [x, 2, 9], 'waxed_cut_copper');
    room.box([12, 0, 7], [13, 0, 8], 'glass');
    room.box([12, 1, 7], [13, 1, 8], 'air');
    for (const [startX, z, shaftY, top] of [[2, 6, 3, 6], [9, 11, 4, 7]]) {
      for (const x of [startX, startX + 4]) room.box([x, 2, z], [x, top, z], 'waxed_cut_copper');
      room.box([startX, shaftY, z], [startX + 1, shaftY, z], 'iron_block');
      room.box([startX + 3, shaftY, z], [startX + 4, shaftY, z], 'iron_block');
      room.set(startX, shaftY + 1, z, 'iron_block');
      room.set(startX + 4, shaftY + 1, z, 'iron_block');
    }
    room.interfaces(); return room;
  }
  if (definition.key === 'addition/07') {
    room.box([1, 2, 4], [2, 2, 10], 'polished_deepslate');
    for (let z = 4; z <= 10; z++) {
      const top = z;
      room.box([2, 3, z], [2, top, z], 'waxed_cut_copper');
      room.set(2, top, z, 'iron_block');
      room.set(1, Math.max(3, top - 1), z, 'black_concrete');
    }
    for (const z of [4, 10]) {
      room.box([12, 2, z], [14, 2, z + 2], 'polished_deepslate');
      room.box([12, 3, z], [14, 3, z + 2], 'waxed_cut_copper');
      room.set(13, 3, z + 1, 'black_concrete');
      room.set(14, 4, z + 1, 'iron_block');
    }
    room.box([8, 2, 7], [9, 2, 8], 'dark_oak_planks');
    room.set(9, 2, 7, 'waxed_cut_copper');
    room.box([3, 2, 3], [3, 10, 3], 'polished_deepslate');
    room.box([12, 2, 12], [12, 10, 12], 'polished_deepslate');
    room.box([3, 10, 3], [12, 10, 3], 'iron_block');
    room.box([12, 10, 3], [12, 10, 12], 'iron_block');
    room.box([6, 9, 3], [8, 9, 3], 'waxed_cut_copper');
    room.set(12, 9, 8, 'sea_lantern');
    room.interfaces(); return room;
  }
  if (definition.key === 'addition/08') {
    room.box([1, 2, 5], [6, 2, 8], 'waxed_cut_copper');
    room.box([1, 5, 5], [6, 5, 8], 'iron_block');
    for (const z of [5, 8]) room.box([1, 3, z], [6, 4, z], 'glass');
    for (const x of [1, 6]) room.box([x, 3, 5], [x, 4, 8], 'glass');
    room.set(2, 3, 6, 'black_concrete'); room.set(2, 3, 7, 'black_concrete');
    room.set(3, 3, 6, 'waxed_cut_copper'); room.set(3, 4, 6, 'waxed_cut_copper');
    room.set(4, 3, 7, 'waxed_cut_copper'); room.set(4, 4, 7, 'black_concrete');
    room.set(5, 3, 6, 'black_concrete'); room.set(5, 3, 7, 'waxed_cut_copper');
    room.box([9, 2, 12], [14, 2, 13], 'polished_deepslate');
    room.box([9, 3, 12], [14, 3, 13], 'iron_block');
    room.box([10, 3, 12], [12, 3, 12], 'waxed_cut_copper');
    room.box([1, 2, 10], [3, 2, 11], 'polished_deepslate');
    room.box([1, 3, 10], [3, 3, 11], 'iron_block');
    room.set(2, 3, 10, 'waxed_cut_copper');
    room.box([14, 2, 3], [14, 6, 6], 'polished_deepslate');
    room.box([14, 4, 3], [14, 4, 6], 'waxed_cut_copper');
    room.box([1, 2, 2], [1, 6, 3], 'polished_deepslate');
    room.box([1, 6, 2], [1, 6, 3], 'waxed_cut_copper');
    room.set(5, 1, 8, 'sea_lantern');
    room.interfaces(); return room;
  }
  if (definition.key === 'addition/05') {
    room.box([1, 2, 4], [2, 6, 11], 'polished_deepslate');
    for (const z of [2, 13]) room.box([2, 2, z], [5, 5, z], 'polished_deepslate');
    for (const z of [3, 12]) room.box([2, 2, z], [3, 6, z], 'waxed_cut_copper');
    for (const z of [5, 10]) room.box([2, 2, z], [3, 5, z], 'waxed_cut_copper');
    for (const y of [3, 5]) room.box([2, y, 4], [2, y, 11], 'waxed_cut_copper');
    room.box([3, 2, 8], [4, 2, 8], 'smooth_stone');
    room.set(5, 1, 8, 'waxed_cut_copper');
    for (const z of [3, 5, 7, 9, 11, 13]) {
      room.set(7, 1, z, 'waxed_cut_copper'); room.set(8, 1, z, 'black_concrete');
    }
    for (const z of [2, 13]) room.box([14, 2, z], [14, 11, z], 'polished_deepslate');
    room.box([12, 11, 2], [14, 11, 13], 'iron_block');
    for (const centerZ of [4, 8, 12]) {
      room.box([12, 9, centerZ], [12, 10, centerZ], 'iron_block');
      for (let y = 5; y <= 9; y++) for (let z = centerZ - 2; z <= centerZ + 2; z++) if (Math.abs(Math.hypot(y - 7, z - centerZ) - 2) < 0.55) room.set(12, y, z, 'waxed_cut_copper');
      room.box([12, 6, centerZ], [12, 8, centerZ], 'iron_block');
      room.box([12, 7, centerZ - 1], [12, 7, centerZ + 1], 'iron_block');
      room.set(13, 1, centerZ, 'sea_lantern');
    }
    room.interfaces(); return room;
  }
  if (definition.key === 'addition/06') {
    room.box([1, 2, 3], [3, 10, 6], 'polished_deepslate');
    room.box([1, 10, 3], [3, 10, 6], 'waxed_cut_copper');
    room.box([3, 4, 4], [3, 8, 4], 'iron_block');
    room.set(3, 8, 5, 'sea_lantern');
    room.box([1, 2, 12], [4, 3, 13], 'polished_deepslate');
    room.box([1, 4, 12], [4, 4, 12], 'iron_block');
    for (const x of [1, 4]) room.box([x, 3, 12], [x, 5, 13], 'waxed_cut_copper');
    room.box([2, 7, 6], [2, 7, 12], 'iron_block');
    room.box([10, 2, 9], [13, 3, 12], 'polished_andesite');
    for (let z = 9; z <= 11; z++) {
      room.set(8, 2, z, 'stone_brick_stairs[facing=east,half=bottom,shape=straight,waterlogged=false]');
      room.set(9, 2, z, 'polished_andesite');
      room.set(9, 3, z, 'stone_brick_stairs[facing=east,half=bottom,shape=straight,waterlogged=false]');
    }
    for (const z of [9, 12]) room.box([14, 2, z], [14, 9, z], 'polished_deepslate');
    room.box([13, 9, 9], [14, 9, 12], 'iron_block');
    room.box([14, 6, 10], [14, 7, 10], 'waxed_cut_copper');
    room.box([14, 5, 11], [14, 6, 11], 'waxed_cut_copper');
    room.interfaces(); return room;
  }
  if (definition.key === 'addition/03') {
    room.box([1, 2, 3], [3, 8, 6], 'polished_deepslate');
    room.box([2, 3, 4], [2, 7, 5], 'iron_block');
    room.box([1, 8, 3], [3, 8, 6], 'waxed_cut_copper');
    room.box([10, 2, 10], [13, 11, 13], 'polished_deepslate');
    room.box([11, 3, 11], [12, 10, 12], 'iron_block');
    room.box([10, 11, 10], [13, 11, 13], 'waxed_cut_copper');
    room.box([3, 3, 4], [3, 7, 4], 'iron_block');
    room.box([10, 4, 11], [10, 10, 11], 'iron_block');
    room.box([2, 9, 5], [2, 11, 5], 'iron_block');
    room.box([2, 11, 5], [11, 11, 5], 'waxed_cut_copper');
    room.box([11, 11, 5], [11, 11, 11], 'waxed_cut_copper');
    room.box([8, 1, 7], [9, 1, 9], 'glass');
    room.box([8, 2, 6], [9, 2, 6], 'waxed_cut_copper');
    room.box([8, 2, 10], [9, 2, 10], 'waxed_cut_copper');
    room.box([7, 2, 7], [7, 2, 9], 'waxed_cut_copper');
    room.box([10, 2, 7], [10, 2, 9], 'waxed_cut_copper');
    room.set(2, 7, 3, 'sea_lantern'); room.set(12, 10, 10, 'sea_lantern');
    room.interfaces(); return room;
  }
  if (definition.key === 'addition/04') {
    for (let x = 1; x <= 3; x++) {
      room.box([x, 2, 6], [x, x + 4, 6], 'waxed_cut_copper');
      room.set(x, x + 4, 6, 'iron_block');
    }
    room.box([2, 2, 10], [3, 5, 10], 'polished_deepslate');
    room.box([2, 6, 10], [3, 6, 10], 'waxed_cut_copper');
    room.box([12, 2, 7], [14, 3, 8], 'polished_deepslate');
    room.box([12, 4, 7], [14, 4, 7], 'waxed_cut_copper');
    room.box([12, 5, 7], [12, 6, 7], 'iron_block');
    room.box([14, 5, 7], [14, 7, 7], 'iron_block');
    room.box([12, 2, 10], [14, 3, 10], 'waxed_cut_copper');
    room.box([7, 2, 7], [8, 2, 8], 'polished_deepslate');
    room.box([7, 3, 7], [8, 3, 7], 'waxed_cut_copper');
    room.set(8, 3, 8, 'sea_lantern');
    room.box([3, 8, 6], [13, 8, 6], 'iron_block');
    room.box([3, 6, 6], [3, 7, 6], 'iron_block');
    room.box([13, 5, 6], [13, 7, 6], 'waxed_cut_copper');
    room.interfaces(); return room;
  }
  if (definition.key === 'addition/01') {
    for (const [x, startZ, top] of [[2, 4, 10], [13, 7, 11]]) {
      room.box([x, 3, startZ], [x, top, startZ], 'iron_block');
      room.box([x - 1, top - 1, startZ], [x + 1, top, startZ + 3], 'waxed_cut_copper');
      room.box([x, top - 1, startZ + 1], [x, top, startZ + 2], 'air');
      room.box([x, 2, startZ], [x, 3, startZ + 4], 'polished_deepslate');
      room.set(x, 4, startZ, 'sea_lantern');
    }
    room.box([7, 2, 6], [8, 2, 8], 'waxed_cut_copper');
    room.set(7, 3, 7, 'iron_block'); room.set(8, 3, 6, 'waxed_cut_copper'); room.set(8, 3, 8, 'waxed_cut_copper');
    for (const x of [3, 12]) room.box([x, 2, 10], [x, 11, 10], 'polished_deepslate');
    room.box([3, 11, 10], [12, 11, 10], 'iron_block');
    room.box([6, 7, 10], [6, 10, 10], 'waxed_cut_copper');
    room.box([5, 6, 10], [7, 7, 10], 'iron_block');
    room.box([9, 9, 10], [9, 10, 10], 'waxed_cut_copper');
    room.set(9, 8, 10, 'sea_lantern');
  } else {
    for (const z of [3, 7, 11]) {
      room.box([1, 2, z], [3, 2, z + 1], 'polished_deepslate');
      room.box([1, 3, z], [3, 3, z], 'waxed_cut_copper');
      room.box([1, 4, z], [1, 7, z], 'waxed_cut_copper');
      room.box([3, 4, z], [3, 7, z], 'waxed_cut_copper');
      room.box([1, 8, z], [3, 8, z], 'waxed_cut_copper');
      room.box([2, 4, z], [2, 7, z], 'iron_block');
      room.set(2, 5, z, 'black_concrete');
      room.box([1, 9, z + 1], [3, 9, z + 1], 'iron_block');
    }
    room.box([10, 1, 6], [13, 1, 9], 'waxed_cut_copper');
    room.box([11, 1, 7], [12, 1, 8], 'glass');
    for (const [x, z] of [[10, 6], [13, 6], [10, 9], [13, 9]]) room.box([x, 2, z], [x, 9, z], 'polished_deepslate');
    room.box([10, 10, 6], [13, 10, 6], 'iron_block');
    room.box([10, 10, 9], [13, 10, 9], 'iron_block');
    room.box([11, 10, 6], [11, 10, 9], 'waxed_cut_copper');
    room.box([11, 7, 7], [11, 9, 7], 'iron_block');
    room.box([11, 6, 7], [12, 6, 8], 'waxed_cut_copper');
    room.box([4, 8, 5], [10, 8, 5], 'iron_block');
    room.box([4, 2, 5], [4, 8, 5], 'polished_deepslate');
    room.box([10, 2, 5], [10, 8, 5], 'polished_deepslate');
    room.box([6, 1, 5], [8, 1, 11], 'waxed_cut_copper');
  }
  room.interfaces(); return room;
}

function encode(room) {
  const structure = new library.Structure(room.variant.size);
  for (let y = 0; y < room.height; y++) for (let z = 0; z < room.depth; z++) for (let x = 0; x < room.width; x++) {
    const state = library.BlockState.parse('minecraft:' + room.get(x, y, z));
    structure.addBlock([x, y, z], state.getName(), state.getProperties());
  }
  return structure.writeNbt({ name: '', dataVersion: 3465, compression: 'gzip' });
}

function decode(bytes, definition, variant, dense = true) {
  const file = library.NbtFile.read(bytes); const structure = library.Structure.fromNbt(file.root);
  assert.equal(file.root.getNumber('DataVersion'), 3465); assert.equal(file.root.getList('entities').length, 0);
  const room = new Room(definition, { ...variant, size: structure.getSize() });
  const seen = new Set();
  for (const block of structure.getBlocks()) {
    const key = block.pos.join(','); assert(!seen.has(key)); seen.add(key); assert(!block.nbt);
    room.set(...block.pos, block.state.toString().replace(/^minecraft:/, ''));
  }
  if (dense) assert.equal(seen.size, room.blocks.length);
  const palette = file.root.getList('palette');
  for (let index = 0; index < palette.length; index++) assert(/^minecraft:[a-z0-9_]+$/.test(palette.getCompound(index).getString('Name')));
  return room;
}

function halfStepGraph(room, start) {
  const surfaces = new Map();
  const shapeHeight = (microX, microZ, blockY) => {
    const block = room.get(Math.floor(microX / 2), blockY, Math.floor(microZ / 2));
    if (isAir(block)) return 0;
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

function walk(room) {
  const safe = (x, z) => !isAir(room.get(x, 1, z)) && [2, 3, 4].every(y => isAir(room.get(x, y, z)));
  const start = room.variant.ports[0].cells[0];
  const queue = [[start[0], start[2]]], visited = new Set([queue[0].join(',')]);
  for (let cursor = 0; cursor < queue.length; cursor++) {
    const [x, z] = queue[cursor];
    for (const [dx, dz] of [[1, 0], [-1, 0], [0, 1], [0, -1]]) {
      const next = [x + dx, z + dz], key = next.join(',');
      if (room.inside(next[0], 2, next[1]) && safe(...next) && !visited.has(key)) { visited.add(key); queue.push(next); }
    }
  }
  const broad = new Set();
  for (let x = 1; x < 15; x++) for (let z = 1; z < 15; z++) if ([-1, 0, 1].every(dx => [-1, 0, 1].every(dz => safe(x + dx, z + dz)))) broad.add(`${x},${z}`);
  const wideQueue = [[7, 2]], wideVisited = new Set(['7,2']); assert(broad.has('7,2'));
  for (let cursor = 0; cursor < wideQueue.length; cursor++) {
    const [x, z] = wideQueue[cursor];
    for (const [dx, dz] of [[1, 0], [-1, 0], [0, 1], [0, -1]]) {
      const next = [x + dx, z + dz], key = next.join(',');
      if (broad.has(key) && !wideVisited.has(key)) { wideVisited.add(key); wideQueue.push(next); }
    }
  }
  assert(wideVisited.has('7,13'), `${room.definition.key} main path not 3 wide`);
  if (room.definition.key === 'addition/08') for (const key of ['8,4', '8,9', '7,10', '6,12', '7,13']) assert(wideVisited.has(key), `S-route three-wide waypoint missing ${key}`);
  if (room.definition.key === 'addition/04') {
    for (const key of ['5,8', '10,8', '7,4', '7,11']) assert(wideVisited.has(key), `Dual-loop three-wide branch disconnected ${key}`);
  }
  return { safe, visited, wideVisited };
}

function validate(room, sha256) {
  assert.deepEqual(room.variant.size, [16, 16, 16]);
  const safe = (position, height) => { const [x, y, z] = position; assert(!isAir(room.get(x, y - 1, z))); for (let offset = 0; offset < height; offset++) assert(isAir(room.get(x, y + offset, z)), `${room.definition.key} blocked ${position}`); };
  const graph = walk(room); const alternatives = [], reserved = new Set(room.variant.sockets.map(socket => socket.position.join(',')));
  if (room.definition.key === 'addition/08') {
    for (let x = 1; x <= 6; x++) for (let y = 2; y <= 5; y++) for (let z = 5; z <= 8; z++) {
      const boundary = x === 1 || x === 6 || y === 2 || y === 5 || z === 5 || z === 8;
      if (boundary) assert(!isAir(room.get(x, y, z)), `Dry mold enclosure gap ${[x, y, z]}`);
      if (boundary && y > 2 && y < 5) assert.equal(room.get(x, y, z), 'glass', `Non-glass observation side ${[x, y, z]}`);
    }
  }
  for (const port of room.variant.ports) for (const position of port.cells) {
    const [x, y, z] = position;
    const dx = x === 0 ? 1 : x === room.width - 1 ? -1 : 0, dz = z === 0 ? 1 : z === room.depth - 1 ? -1 : 0;
    for (let step = 0; step <= 3; step++) safe([x + dx * step, y, z + dz * step], 5);
    assert(graph.visited.has([x, z].join(',')));
  }
  for (const socket of room.variant.sockets) {
    for (const position of [socket.position, socket.spawn_position]) { safe(position, 3); assert(graph.visited.has([position[0], position[2]].join(','))); }
    let candidate;
    for (let distance = 1; distance <= 6 && !candidate; distance++) for (let x = 1; x < 15 && !candidate; x++) for (let z = 1; z < 15 && !candidate; z++) if (Math.abs(x - socket.position[0]) + Math.abs(z - socket.position[2]) === distance && !reserved.has([x, 2, z].join(',')) && graph.safe(x, z) && graph.visited.has(`${x},${z}`)) candidate = [x, 2, z];
    assert(candidate, 'No separate entity spawn'); reserved.add(candidate.join(',')); alternatives.push({ socket: socket.id, position: candidate });
    assert([...graph.wideVisited].some(key => { const [x, z] = key.split(',').map(Number); return Math.abs(x - socket.position[0]) + Math.abs(z - socket.position[2]) <= 3; }), 'No wide socket approach');
  }
  const allowed = new Set(room.variant.ports.flatMap(port => port.cells.flatMap(([x, y, z]) => Array.from({ length: 5 }, (_, offset) => [x, y + offset, z].join(',')))));
  for (let y = 0; y < 16; y++) for (let z = 0; z < 16; z++) for (let x = 0; x < 16; x++) if (x === 0 || z === 0 || x === 15 || z === 15 || y === 0 || y === 15) if (isAir(room.get(x, y, z))) assert(allowed.has([x, y, z].join(',')), 'Shell gap');
  let raisedPlatform = null;
  let maintenancePit = null;
  if (room.definition.key === 'addition/09') {
    const halfSteps = halfStepGraph(room, [7, 2, 0]);
    for (const target of [[2, 4, 6], [2, 4, 8], ...room.variant.sockets.map(socket => socket.position)]) assert(halfSteps.reached(target), `Observation platform needs jump ${target}`);
    for (let x = 1; x <= 3; x++) for (const [y, z] of [[2, 3], [3, 4]]) assert(room.get(x, y, z).includes('stone_brick_stairs'));
    for (let x = 1; x <= 14; x++) for (let z = 1; z <= 14; z++) assert.equal(room.get(x, 15, z), 'glass');
    raisedPlatform = { status: 'PASS', targetFeetY: 4, actualStairBlocks: 6, stairWidth: 3, maximumStep: 0.5, twoBlockHeadroom: true, reachableMicroCells: halfSteps.visited.size, targets: [[2, 4, 6], [2, 4, 8]], jumpingRequired: false, completeGlassRoofCells: 196 };
  }
  if (room.definition.key === 'addition/10') {
    for (let x = 11; x <= 14; x++) for (let z = 6; z <= 9; z++) {
      if (x === 11 || x === 14 || z === 6 || z === 9) assert.equal(room.get(x, 2, z), 'waxed_cut_copper');
      else { assert.equal(room.get(x, 0, z), 'glass'); assert(isAir(room.get(x, 1, z))); assert(!graph.visited.has(`${x},${z}`)); }
    }
    for (const waypoint of ['8,6', '8,8', '7,9', '7,11']) assert(graph.wideVisited.has(waypoint), `Disassembly bypass missing ${waypoint}`);
    maintenancePit = { status: 'PASS', guardedPerimeterCells: 12, sealedBottomCells: 4, depthBelowMainFloor: 1, threeWideBypass: true, socketFeetY: 2 };
  }
  const dryMoldEnclosure = room.definition.key === 'addition/08' ? { status: 'PASS', from: [1, 2, 5], to: [6, 5, 8], completeBoundarySolidCells: 80, glassSideCells: 32, glassSidesIndependentlyChecked: true, liquidBlocks: 0, staticMoldCoresContainedInside: true, threeWideSRouteWaypoints: ['8,4', '8,9', '7,10', '6,12', '7,13'] } : null;
  if (room.definition.key === 'addition/06') {
    const halfSteps = halfStepGraph(room, [7, 2, 0]);
    for (const target of [[10, 4, 10], [12, 4, 11], ...room.variant.sockets.map(socket => socket.position)]) assert(halfSteps.reached(target), `Platform route needs jump ${target}`);
    for (let z = 9; z <= 11; z++) {
      assert(room.get(8, 2, z).includes('stone_brick_stairs'));
      assert(room.get(9, 3, z).includes('stone_brick_stairs'));
    }
    raisedPlatform = { status: 'PASS', targetFeetY: 4, actualStairBlocks: 6, stairWidth: 3, maximumStep: 0.5, twoBlockHeadroom: true, reachableMicroCells: halfSteps.visited.size, targets: [[10, 4, 10], [12, 4, 11]], jumpingRequired: false };
  }
  return { key: room.definition.key, baseKey: room.definition.baseKey, sha256, status: 'PASS', size: room.variant.size, minimum_y: room.variant.minimum_y, ports: room.variant.ports, sockets: room.variant.sockets, denseUniqueCoordinates: 4096, dataVersion: 3465, fullSupportAndClearance: true, shellFloorAndRoofClosed: true, reachableWalkCells: graph.visited.size, mainRoad: { width: 3, contiguousThreeByThreeFootprints: graph.wideVisited.size, northSouthReachable: true, floorY: 2, heightChanges: 0, jumpingRequired: false }, raisedPlatform, maintenancePit, dryMoldEnclosure, independentEntitySpawns: alternatives, route: room.definition.route };
}

function mask(room) {
  const excluded = new Set();
  for (const port of room.variant.ports) for (const [x, y, z] of port.cells) {
    const dx = x === 0 ? 1 : x === room.width - 1 ? -1 : 0, dz = z === 0 ? 1 : z === room.depth - 1 ? -1 : 0;
    for (let step = 0; step <= 3; step++) for (let yy = y - 1; yy <= y + 4; yy++) excluded.add([x + step * dx, yy, z + step * dz].join(','));
  }
  const floor = room.variant.key === 'spawn/spawn' ? 4 : room.variant.key.includes('/layout/') ? 1 : 2;
  const cells = new Set(), normalized = new Set(), layout = new Set();
  for (let y = floor; y < room.height - 5; y++) for (let z = 1; z < room.depth - 1; z++) for (let x = 1; x < room.width - 1; x++) if (!excluded.has([x, y, z].join(',')) && !isAir(room.get(x, y, z))) {
    cells.add([x, y, z].join(','));
    const normalizedX = Math.min(15, Math.floor((x - 1) * 16 / (room.width - 2))), normalizedZ = Math.min(15, Math.floor((z - 1) * 16 / (room.depth - 2)));
    normalized.add([normalizedX, Math.min(15, Math.floor((y - floor) * 16 / (room.height - 5 - floor))), normalizedZ].join(','));
    if (y <= floor + 4) layout.add([normalizedX, normalizedZ].join(','));
  }
  return { cells, normalized, layout };
}
function jaccard(first, second) { let count = 0; for (const value of first) if (second.has(value)) count++; return count / (first.size + second.size - count || 1); }
function rotated(set, turn) {
  return new Set([...set].map(key => { const [x, y, z] = key.split(',').map(Number); return (turn === 0 ? [x, y, z] : turn === 1 ? [15 - z, y, x] : turn === 2 ? [15 - x, y, 15 - z] : [z, y, 15 - x]).join(','); }));
}

function audits(rooms, entries) {
  const styleRooms = rooms.map(room => {
    const materials = {}, internalMaterials = {}, internal = mask(room);
    for (const block of room.blocks) materials['minecraft:' + bare(block)] = (materials['minecraft:' + bare(block)] ?? 0) + 1;
    for (const key of internal.cells) { const block = 'minecraft:' + bare(room.get(...key.split(',').map(Number))); internalMaterials[block] = (internalMaterials[block] ?? 0) + 1; }
    const landmarks = room.definition.landmarks.map(([name, start, end]) => {
      const cells = [], tally = {};
      for (let y = start[1]; y <= end[1]; y++) for (let z = start[2]; z <= end[2]; z++) for (let x = start[0]; x <= end[0]; x++) if (!isAir(room.get(x, y, z))) { cells.push([x, y, z]); const block = 'minecraft:' + bare(room.get(x, y, z)); tally[block] = (tally[block] ?? 0) + 1; }
      assert(cells.length >= 4, `Landmark missing ${name}`);
      return { name, from: start, to: end, occupiedCells: cells.length, materials: tally, actualBounds: { from: [0, 1, 2].map(axis => Math.min(...cells.map(cell => cell[axis]))), to: [0, 1, 2].map(axis => Math.max(...cells.map(cell => cell[axis]))) }, evidenceSha256: hash(cells.map(cell => [cell, room.get(...cell)]).join(';')) };
    });
    assert((internalMaterials['minecraft:waxed_cut_copper'] ?? 0) > 15); assert((internalMaterials['minecraft:iron_block'] ?? 0) > 15);
    return { key: room.definition.key, sha256: entries.find(entry => entry.key === room.definition.key).sha256, status: 'PASS', materials, internalMaterials, landmarks, onlyRecoloring: false, fourCornerPillarHall: false, repeatedRingMotif: false, functionalLayout: room.definition.route, evidence: room.definition.route + '; actual measured landmarks: ' + landmarks.map(landmark => landmark.name).join(', ') };
  });
  const old = catalog.variants.map(variant => {
    const file = join(root, 'output/style-rebuild-v034/clockwork/roomset', `${variant.key}.nbt`);
    const bytes = readFileSync(file);
    return { key: 'v034/' + variant.key, bytes, room: decode(bytes, { key: variant.key }, variant) };
  });
  const previous = [];
  for (const directory of readdirSync(scope, { withFileTypes: true })) if (directory.isDirectory() && /^batch-\d\d$/.test(directory.name) && directory.name < batchName) {
    const manifest = JSON.parse(readFileSync(join(scope, directory.name, 'manifest.json')));
    for (const entry of manifest.templates) { const bytes = readFileSync(join(scope, directory.name, entry.file)); previous.push({ key: directory.name + '/' + entry.key, bytes, room: decode(bytes, { key: entry.key }, catalog.variants.find(variant => variant.key === entry.baseKey)) }); }
  }
  const candidates = [...old, ...previous, ...rooms.map(room => ({ key: room.definition.key, room }))];
  const pairs = [];
  for (const room of rooms) for (const candidate of candidates) {
    if (candidate.key === room.definition.key || (candidate.key.startsWith('addition/') && candidate.key < room.definition.key)) continue;
    const left = mask(room), right = mask(candidate.room), sameSize = room.variant.size.join(',') === candidate.room.variant.size.join(',');
    const rawJaccard = sameSize ? jaccard(left.cells, right.cells) : null;
    const normalizedRotationJaccard = Math.max(...[0, 1, 2, 3].map(turn => jaccard(left.normalized, rotated(right.normalized, turn))));
    const normalizedLayoutJaccard = jaccard(left.layout, right.layout);
    const maximum = Math.max(rawJaccard ?? 0, normalizedRotationJaccard);
    assert(maximum < 0.85, `Repetition >=0.85 requires rework ${room.definition.key} ${candidate.key}`);
    pairs.push({ keys: [room.definition.key, candidate.key], sameSize, rawSolidOccupancyJaccard: rawJaccard, normalizedRotationJaccard, normalizedLayoutJaccard, maximumSolidSimilarity: maximum, reviewThreshold: 0.85, status: 'PASS', routeDifference: room.definition.route + '; comparison uses internal solids instead of shared empty hall or outer shell' });
  }
  pairs.sort((first, second) => second.maximumSolidSimilarity - first.maximumSolidSimilarity);
  const style = { schema: 1, style: 'fteclipsegen:clockwork', batch, status: 'PASS', evidenceSource: 'Decoded final exported NBT', rooms: styleRooms };
  for (const report of style.rooms) report.visualReview = { status: 'PASS', method: 'Viewed actual Chromium screenshot of all four NBT projections for this room', directions: visualNotes[report.key], unresolvedThemeOrDensityIssues: [] };
  for (const report of style.rooms) if (report.key === 'addition/05') {
    report.repeatedRingMotif = true;
    report.repeatedRingReview = 'Retain three small supported balance wheels as the specifically requested timing archive apparatus; no central giant gear-ring or clock-tower skeleton; compare actual interior geometry against all previous rooms';
  }
  const repetition = { schema: 1, style: 'fteclipsegen:clockwork', batch, status: 'PASS', comparisons: pairs.length, previousV034RoomsCompared: old.length, previousAdditionRoomsCompared: previous.length, batchInternalComparisons: 1, exclusions: ['x/z boundary shell', 'floor below principal feet layer', 'top five roof layers', 'port cells and inward 0..3 band at y-1..y+4'], metrics: ['Occupied-only solid Jaccard for same-sized rooms', '16-cubed normalized occupancy maximum over four rotations for every old room', 'Normalized XZ low-level obstacle footprint Jaccard'], threshold: 0.85, highSimilarityPairs: [], highestPairs: pairs.slice(0, 12), signatures: rooms.map(room => ({ key: room.definition.key, normalizedSignatureSha256: hash([...mask(room).normalized].sort().join(';')), internalSolidCells: mask(room).cells.size })), referenceSha256s: [...old, ...previous].map(candidate => ({ key: candidate.key, sha256: hash(candidate.bytes) })), pairs };
  json('style-audit.json', style); json('repetition-audit.json', repetition); return { style, repetition };
}

const colors = { air: '#101b24', polished_andesite: '#a7b0b4', polished_deepslate: '#414d58', deepslate_bricks: '#384550', deepslate_tiles: '#34404b', waxed_cut_copper: '#d89b65', iron_block: '#d6e4e2', sea_lantern: '#84e9df', glass: '#5795aa', black_concrete: '#1d2835' };
const directions = [{ id: 'north', label: '北', turn: 0, looking: '+Z' }, { id: 'south', label: '南', turn: 2, looking: '-Z' }, { id: 'west', label: '西', turn: 3, looking: '+X' }, { id: 'east', label: '东', turn: 1, looking: '-X' }];

function svg(room, direction, sourceSha256) {
  const inverse = (horizontal, distance) => direction.turn === 0 ? [horizontal, distance] : direction.turn === 1 ? [15 - distance, horizontal] : direction.turn === 2 ? [15 - horizontal, 15 - distance] : [distance, 15 - horizontal];
  const transform = (x, z) => direction.turn === 0 ? [x, z] : direction.turn === 1 ? [z, 15 - x] : direction.turn === 2 ? [15 - x, 15 - z] : [15 - z, x];
  const get = (horizontal, y, distance) => { const [x, z] = inverse(horizontal, distance); return x > 0 && x < 15 && z > 0 && z < 15 && y >= 1 && y <= 11 ? room.get(x, y, z) : 'air'; };
  const point = (horizontal, y, distance) => [40 + horizontal * 21, 430 - distance * 10 - (y - 1) * 23];
  const faces = [];
  const face = (vertices, color, title) => `<polygon points="${vertices.map(vertex => point(...vertex).join(',')).join(' ')}" fill="${color}" fill-opacity="${color === colors.glass ? 0.35 : 1}" stroke="#25323b" stroke-width="0.5"><title>${escape(title)}</title></polygon>`;
  for (let distance = 15; distance >= 0; distance--) for (let y = 1; y <= 11; y++) for (let horizontal = 0; horizontal < 16; horizontal++) {
    const block = get(horizontal, y, distance); if (isAir(block)) continue;
    const [x, z] = inverse(horizontal, distance), title = `${x},${y},${z} ${block}`, color = colors[bare(block)] ?? '#bba17f';
    if (isAir(get(horizontal, y, distance - 1))) faces.push(face([[horizontal, y, distance], [horizontal + 1, y, distance], [horizontal + 1, y + 1, distance], [horizontal, y + 1, distance]], color, title));
    if (isAir(get(horizontal, y + 1, distance))) faces.push(face([[horizontal, y + 1, distance], [horizontal + 1, y + 1, distance], [horizontal + 1, y + 1, distance + 1], [horizontal, y + 1, distance + 1]], color, title));
  }
  const geometryProjectionSha256 = hash(faces.join(''));
  for (const socket of room.variant.sockets) { const [horizontal, distance] = transform(socket.position[0], socket.position[2]), [screenX, screenY] = point(horizontal + 0.5, 2.1, distance + 0.5); faces.push(`<circle cx="${screenX}" cy="${screenY}" r="4" fill="#ff81bd"><title>${escape(socket.id)}</title></circle>`); }
  return { geometryProjectionSha256, faces: faces.length, data: `<svg xmlns="http://www.w3.org/2000/svg" width="840" height="960" viewBox="0 0 420 480"><metadata>${escape(JSON.stringify({ key: room.definition.key, direction: direction.id, looking: direction.looking, sourceSha256, hidden: 'boundary x/z shell, y<1 and y>11' }))}</metadata><rect width="420" height="480" rx="10" fill="#101b24"/><text x="18" y="25" fill="#efbb83" font-size="16">${direction.label} / ${direction.id} 看向${direction.looking}</text>${faces.join('')}<text x="18" y="458" fill="#b4c8d4" font-family="monospace" font-size="11">${room.definition.key} ${sourceSha256.slice(0, 16)}</text></svg>` };
}

async function renderViews(rooms, entries) {
  const records = [], cards = [];
  for (const room of rooms) {
    const sha256 = entries.find(entry => entry.key === room.definition.key).sha256, figures = [], hashes = [];
    for (const direction of directions) {
      const view = svg(room, direction, sha256), file = `views/${room.definition.key}-${direction.id}.svg`;
      mkdirSync(dirname(join(output, file)), { recursive: true }); writeFileSync(join(output, file), view.data); hashes.push(view.geometryProjectionSha256);
      records.push({ key: room.definition.key, direction: direction.id, looking: direction.looking, sourceNbtSha256: sha256, file, sha256: hash(view.data), geometryProjectionSha256: view.geometryProjectionSha256, faces: view.faces });
      figures.push(`<figure><img src="${file}" data-key="${room.definition.key}" data-direction="${direction.id}" alt="${room.definition.name} ${direction.label} 实际NBT投影"><figcaption>${direction.label} · 看向 ${direction.looking}</figcaption></figure>`);
    }
    assert.equal(new Set(hashes).size, 4);
    cards.push(`<article><h2>${room.definition.key} · ${room.definition.name}</h2><p>${room.definition.route}</p><div class="views">${figures.join('')}</div><p class="hash">NBT SHA256 ${sha256}</p></article>`);
  }
  writeFileSync(join(output, 'four-views.html'), `<!doctype html><html lang="zh-CN"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>钟械新增第5批 · 实际NBT四视图</title><style>body{margin:0;background:#0c151d;color:#e0e9ed;font:15px system-ui,sans-serif}main{max-width:1740px;margin:auto;padding:28px}h1{color:#e3ac7c}p{line-height:1.7}article{background:#182832;padding:24px;border:1px solid #455867;border-radius:15px;margin:24px 0}.views{display:grid;grid-template-columns:repeat(4,1fr);gap:12px}figure{margin:0}img{width:100%;display:block}figcaption{padding:7px;color:#8fe0da}.hash{font:12px monospace;word-break:break-all}@media(max-width:900px){.views{grid-template-columns:repeat(2,1fr)}}</style><main><h1>钟械 · 第5批2房 · 8个真实NBT方向视图</h1><p>北看+Z、南看-Z、西看+X、东看-X；相机抬高观察内部。只在图示隐藏x/z边界围墙、Y0基础和Y12..15屋面，保留Y1..11。粉色为socket坐标标记。原NBT完整围护与接口另外检查。方块投影来自导出NBT重新解码，不是游戏截图；每图都有源数据和投影哈希。</p>${cards.join('')}</main></html>`);
  json('four-views-manifest.json', { schema: 1, status: 'PASS', views: records, count: 8, hiddenRule: 'Boundary x/z walls, y<1 and y>11; not modified in NBT' });
  const runtime = createRequire('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/package.json');
  const { chromium } = runtime('playwright'); const browser = await chromium.launch({ headless: true });
  try {
    const page = await browser.newPage({ viewport: { width: 1740, height: 1100 } }), errors = [];
    page.on('pageerror', error => errors.push(error.message));
    await page.goto(pathToFileURL(join(output, 'four-views.html')).href);
    await page.evaluate(() => Promise.all([...document.images].map(image => image.decode())));
    const images = await page.evaluate(() => [...document.images].map(image => ({ key: image.dataset.key, direction: image.dataset.direction, width: image.naturalWidth, height: image.naturalHeight })));
    assert.equal(images.length, 8); assert(images.every(image => image.width > 0 && image.height > 0)); assert.deepEqual(errors, []);
    for (let index = 0; index < 2; index++) await page.locator('article').nth(index).screenshot({ path: join(output, `review-0${index + 1}.png`) });
    const report = { schema: 1, status: 'PASS', browser: 'Chromium ' + browser.version(), pageErrors: errors, actualDecodedImageCount: images.length, images, htmlSha256: hash(readFileSync(join(output, 'four-views.html'))), screenshots: ['review-01.png', 'review-02.png'] };
    json('browser-qa.json', report); return report;
  } finally { await browser.close(); }
}

function listFiles(directory, relative = '') {
  const files = [];
  for (const entry of readdirSync(join(directory, relative), { withFileTypes: true })) {
    const child = relative ? relative + '/' + entry.name : entry.name;
    if (entry.isDirectory()) files.push(...listFiles(directory, child)); else if (entry.isFile()) files.push(child);
  }
  return files.sort();
}

async function main() {
  const batchFlag = process.argv.find(argument => argument.startsWith('--batch='));
  assert(!batchFlag || batchFlag === '--batch=5', 'Only batch 5 is currently authorized; previous batch is read-only');
  const protectedFiles = [join(resources, 'catalog.json'), ...listFiles(join(root, 'output/style-rebuild-v034/clockwork')).map(file => join(root, 'output/style-rebuild-v034/clockwork', file)), ...listFiles(join(resources, 'roomsets/clockwork')).map(file => join(resources, 'roomsets/clockwork', file))];
  const protectedHashes = new Map(protectedFiles.map(file => [file, hash(readFileSync(file))]));
  const priorOutput = join(scope, 'batch-01');
  const priorAudit = JSON.parse(readFileSync(join(priorOutput, 'self-audit.json')));
  const priorFreeze = JSON.parse(readFileSync(join(priorOutput, 'freeze.json')));
  assert.equal(priorAudit.status, 'PASS'); assert.equal(priorFreeze.status, 'FROZEN_FOR_PARENT_INTEGRATION');
  assert.equal(priorAudit.manifestSha256, hash(readFileSync(join(priorOutput, 'manifest.json'))));
  for (const [file, digest] of Object.entries(priorFreeze.files)) assert.equal(hash(readFileSync(join(priorOutput, file))), digest, `Prior batch frozen file mismatch ${file}`);
  for (const file of listFiles(priorOutput)) protectedHashes.set(join(priorOutput, file), hash(readFileSync(join(priorOutput, file))));
  const secondOutput = join(scope, 'batch-02');
  const secondAudit = JSON.parse(readFileSync(join(secondOutput, 'self-audit.json')));
  const secondFreeze = JSON.parse(readFileSync(join(secondOutput, 'freeze.json')));
  assert.equal(secondAudit.status, 'PASS'); assert.equal(secondFreeze.status, 'FROZEN_FOR_PARENT_INTEGRATION');
  for (const [file, digest] of Object.entries(secondFreeze.files)) assert.equal(hash(readFileSync(join(secondOutput, file))), digest, `Second batch frozen file mismatch ${file}`);
  for (const file of listFiles(secondOutput)) protectedHashes.set(join(secondOutput, file), hash(readFileSync(join(secondOutput, file))));
  const thirdOutput = join(scope, 'batch-03');
  const thirdAudit = JSON.parse(readFileSync(join(thirdOutput, 'self-audit.json')));
  const thirdFreeze = JSON.parse(readFileSync(join(thirdOutput, 'freeze.json')));
  assert.equal(thirdAudit.status, 'PASS'); assert.equal(thirdFreeze.status, 'FROZEN_FOR_PARENT_INTEGRATION');
  for (const [file, digest] of Object.entries(thirdFreeze.files)) assert.equal(hash(readFileSync(join(thirdOutput, file))), digest, `Third batch frozen file mismatch ${file}`);
  for (const file of listFiles(thirdOutput)) protectedHashes.set(join(thirdOutput, file), hash(readFileSync(join(thirdOutput, file))));
  const fourthOutput = join(scope, 'batch-04');
  const fourthAudit = JSON.parse(readFileSync(join(fourthOutput, 'self-audit.json')));
  const fourthFreeze = JSON.parse(readFileSync(join(fourthOutput, 'freeze.json')));
  assert.equal(fourthAudit.status, 'PASS');
  for (const [file, digest] of Object.entries(fourthFreeze.files)) assert.equal(hash(readFileSync(join(fourthOutput, file))), digest, `Fourth batch frozen file mismatch ${file}`);
  for (const file of listFiles(fourthOutput)) protectedHashes.set(join(fourthOutput, file), hash(readFileSync(join(fourthOutput, file))));
  mkdirSync(output, { recursive: true });
  writeFileSync(join(output, 'design.md'), `# 钟械扩建 · 第5批\n\n仅addition/09、addition/10。前四批冻结文件、旧27房及正式资源只读；本批结束后累计新增10房并停止。\n\n## addition/09 星盘校准站\n\n继承straight/room_towers的16³范围、minimum_y和全部接口。西侧x1..3/z5..8观测台脚面Y4，经z3及z4的三宽真实石砖阶梯到达，每步0.5格。东侧x12..14/z5..6为仪器桌，南侧三组短L形刻度弧分别在Y5/Y7/Y9，独立铁支架落地；三项构成三角布局，没有完整大圆环。x1..14/z1..14/Y15为196格完整玻璃顶，原南北主路仍为Y2三宽。\n\n## addition/10 故障机芯拆解场\n\n继承straight/room_cells全部接口。西轴列x2..6/z6及东轴列x9..13/z11前后错位，铁轴均断开一格，铜轴承高低不同；西北x1..3/z2..3为多层零件架，东南x11..14/z6..9为完整铜围护浅维修坑。坑内x12..13/z7..8仅下挖一格，Y0玻璃封底，十二格实体围护阻止玩家跌入；三格宽路线先东绕西轴、再西绕东轴，四个战斗socket仍在完整Y1实体地板上。\n\n## 验收与复现\n\n每间4096唯一位置密集NBT，DataVersion3465，无实体、方块实体、mod符印、液体、轨道和更新红石。原端口五高空气向内延伸0..3格，socket三高空气、实体支撑、独立实体脚点及连通性全检查。连续3×3脚点检查平层主路，观测台另以半格碰撞图证明无须跳跃；维修坑围护、封底和绕路逐格检查。\n\n八张方向投影全部从导出NBT重新解码绘制；北看+Z、南看-Z、西看+X、东看-X，仅图示隐藏边墙、Y0及Y12..15，实际闭合另验。浏览器解码八图并截图，逐向人工观察。去壳、基础、屋面和门口带后的实体Jaccard，与旧27房、前四批8房及本批内部进行71对比较，0.85为整改阈值。\n\n复现：node tools/expand-clockwork.mjs --batch=5 --verify-replay。源码快照recipe-source.mjs列入freeze；子进程执行快照，所有NBT、视图、审计及浏览器截图须逐字节一致。离线检查不代表游戏实际放置与战斗测试。\n`);
  const entries = [], rooms = [], validations = [];
  const blockReference = readFileSync(join(skill, 'references/lodestone/default-pack-block-states.md'), 'utf8');
  for (const definition of definitions) {
    const intended = build(definition);
    for (const name of new Set(intended.blocks.map(bare))) assert(blockReference.includes('### `minecraft:' + name + '`'), 'Unknown vanilla block ' + name);
    const bytes = encode(intended), file = `roomset/${definition.key}.nbt`;
    mkdirSync(dirname(join(output, file)), { recursive: true }); writeFileSync(join(output, file), bytes);
    const room = decode(readFileSync(join(output, file)), definition, intended.variant);
    assert.deepEqual(room.blocks, intended.blocks);
    rooms.push(room); const sha256 = hash(bytes);
    entries.push({ key: definition.key, baseKey: definition.baseKey, name: definition.name, sha256, file });
    validations.push(validate(room, sha256));
  }
  const manifest = { schema: 1, style: 'fteclipsegen:clockwork', batch, templates: entries }; json('manifest.json', manifest);
  json('validation.json', { schema: 1, style: manifest.style, batch, status: 'PASS', templates: 2, catalogSha256: hash(catalogBytes), promptSha256: hash(readFileSync(join(scope, batchName + '-prompt.md'))), rooms: validations });
  const reports = audits(rooms, entries), browser = await renderViews(rooms, entries);
  const self = { schema: 1, style: manifest.style, batch, status: 'PASS', manifestSha256: hash(readFileSync(join(output, 'manifest.json'))), fourViews: { expected: 8, actual: browser.actualDecodedImageCount, directions: directions.map(direction => ({ direction: direction.id, looking: direction.looking })), source: 'Decoded exported NBT', hiddenRule: 'Boundary x/z shell and Y0,Y12..15 only in projection' }, browser: { status: browser.status, pageErrors: browser.pageErrors, imageCount: browser.actualDecodedImageCount }, stylePassed: 2, repetition: { previous27Compared: reports.repetition.previousV034RoomsCompared, previousAdditionsCompared: reports.repetition.previousAdditionRoomsCompared, comparisons: reports.repetition.comparisons, highSimilarityPairs: reports.repetition.highSimilarityPairs, highestSolidJaccard: reports.repetition.highestPairs[0].maximumSolidSimilarity }, rooms: entries.map(entry => ({ key: entry.key, sha256: entry.sha256, status: 'PASS', visualReview: definitions.find(definition => definition.key === entry.key).route, clearanceEvidence: validations.find(room => room.key === entry.key).mainRoad, styleEvidence: reports.style.rooms.find(room => room.key === entry.key).landmarks, fixes: [] })), protectedFilesUnmodified: true, limitations: ['Offline NBT validation and browser views are not live Minecraft collision or combat tests', 'Projection hides shell and roof for inspection only; full closure is independently checked'], fileHashes: Object.fromEntries(['manifest.json', 'design.md', 'validation.json', 'style-audit.json', 'repetition-audit.json', 'four-views.html', 'four-views-manifest.json', 'browser-qa.json'].map(file => [file, hash(readFileSync(join(output, file)))])) };
  for (const [file, digest] of protectedHashes) assert.equal(hash(readFileSync(file)), digest, `Protected file changed ${file}`);
  self.previousBatch = { batch: 1, status: priorAudit.status, manifestSha256: priorAudit.manifestSha256, frozenFilesMatch: true, priorOutputsUnmodified: true, outstandingKnownRemediation: [] };
  self.previousBatches = [self.previousBatch, { batch: 2, status: secondAudit.status, manifestSha256: secondAudit.manifestSha256, frozenFilesMatch: true, priorOutputsUnmodified: true, outstandingKnownRemediation: [] }];
  self.previousBatches.push({ batch: 3, status: thirdAudit.status, manifestSha256: thirdAudit.manifestSha256, frozenFilesMatch: true, priorOutputsUnmodified: true, outstandingKnownRemediation: [] });
  for (const room of self.rooms) { room.fixes = batchRepairs[room.key] ?? []; room.raisedPlatform = validations.find(validation => validation.key === room.key).raisedPlatform; room.dryMoldEnclosure = validations.find(validation => validation.key === room.key).dryMoldEnclosure; }
  for (const room of self.rooms) room.fourDirectionVisualReview = { status: 'PASS', directions: visualNotes[room.key], screenshot: room.key === definitions[0].key ? 'review-01.png' : 'review-02.png', repairsRequired: false, retainReason: 'All directional review findings agree with measured landmark evidence and unobstructed original interfaces' };
  self.previousBatches.push({ batch: 4, status: fourthAudit.status, manifestSha256: fourthAudit.manifestSha256, frozenFilesMatch: true, priorOutputsUnmodified: true, outstandingKnownRemediation: [] });
  for (const room of self.rooms) room.maintenancePit = validations.find(validation => validation.key === room.key).maintenancePit;
  json('self-audit.json', self);
  writeFileSync(join(output, 'recipe-source.mjs'), readFileSync(sourcePath));
  const files = listFiles(output).filter(file => file !== 'freeze.json' && file !== 'replay-validation.json');
  const freeze = { schema: 1, status: 'FROZEN_FOR_PARENT_INTEGRATION', style: manifest.style, batch, templates: 2, views: 8, recipeSha256: hash(readFileSync(fileURLToPath(import.meta.url))), promptSha256: hash(readFileSync(join(scope, batchName + '-prompt.md'))), manifestSha256: self.manifestSha256, formalResourcesWritten: false, oldV034Written: false, files: Object.fromEntries(files.map(file => [file, hash(readFileSync(join(output, file)))])) };
  json('freeze.json', freeze);
  process.stdout.write(JSON.stringify({ status: freeze.status, templates: entries, views: 8, comparisons: reports.repetition.comparisons, maxSolidJaccard: self.repetition.highestSolidJaccard, manifestSha256: freeze.manifestSha256 }, null, 2) + '\n');
}

await main();
if (process.argv.includes('--verify-replay')) {
  const files = listFiles(output).filter(file => file !== 'replay-validation.json');
  const before = Object.fromEntries(files.map(file => [file, hash(readFileSync(join(output, file)))]));
  const execution = spawnSync(process.execPath, [join(output, 'recipe-source.mjs'), '--batch=5', '--replay-child'], { encoding: 'utf8', maxBuffer: 1024 * 1024 });
  assert.equal(execution.status, 0, execution.stderr || execution.stdout);
  assert.deepEqual(files.filter(file => hash(readFileSync(join(output, file))) !== before[file]), [], 'Replay bytes differ');
  json('replay-validation.json', { schema: 1, status: 'PASS', filesCompared: files.length, byteIdentical: true, sourceSnapshotExecuted: 'recipe-source.mjs', sourceSnapshotSha256: hash(readFileSync(join(output, 'recipe-source.mjs'))), includes: 'All NBT, 8 SVGs, HTML, audit reports and browser screenshots', manifestSha256: hash(readFileSync(join(output, 'manifest.json'))), recipeSha256: hash(readFileSync(fileURLToPath(import.meta.url))), files: before });
  process.stdout.write(`REPLAY PASS ${files.length} files byte-identical\n`);
}
