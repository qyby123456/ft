import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath, pathToFileURL } from 'node:url'
import { createHash } from 'node:crypto'
import { spawnSync } from 'node:child_process'
import assert from 'node:assert/strict'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const skill = process.env.MINECRAFT_BUILDER_SKILL ?? 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
const { NbtFile, Structure } = await import(pathToFileURL(path.join(skill, 'node_modules/@mattzh72/lodestone/lib/index.js')).href)
const reference = 'D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/S02-stone-brick-aged.png'
const referenceHash = 'e068ca984a82719da731f79474db5a7d3cd0bbdade57e5bfaca420e46e665239'
const digest = bytes => createHash('sha256').update(bytes).digest('hex')
const writeJson = async (name, value) => {
  await fs.mkdir(path.dirname(path.join(root, name)), { recursive: true })
  await fs.writeFile(path.join(root, name), JSON.stringify(value, null, 2) + String.fromCharCode(10))
}

if (!process.argv.includes('--audit-only')) {
  const result = spawnSync(process.execPath, ['scripts/run-build.mjs', path.join(root, 'recipe.mjs'), '--out-dir', root, '--name', 'room', '--data-version', '3465', '--json'], { cwd: skill, encoding: 'utf8', windowsHide: true })
  await fs.mkdir(path.join(root, 'qa'), { recursive: true })
  await fs.writeFile(path.join(root, 'qa/builder.log'), result.stdout + result.stderr)
  assert.equal(result.status, 0, result.stderr)
}
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
assert.equal(digest(await fs.readFile(reference)), referenceHash, 'Reference hash mismatch')
assert.equal(bytes[0], 0x1f)
assert.equal(bytes[1], 0x8b)
const nbt = NbtFile.read(bytes)
const structure = Structure.fromNbt(nbt.root)
const blocks = structure.getBlocks()
const size = structure.getSize()
assert.deepEqual(size, [11, 9, 16])
assert.equal(nbt.root.getNumber('DataVersion'), 3465)
assert.equal(nbt.root.getList('entities').length, 0)
assert.equal(nbt.root.has('palettes'), false)
assert.equal(nbt.root.getList('blocks').length, 1584)
assert.equal(blocks.length, 1584)
const allowed = new Set(['air', 'stone_bricks', 'cracked_stone_bricks', 'mossy_stone_bricks', 'andesite', 'chiseled_stone_bricks', 'stone_brick_stairs', 'stone_brick_slab', 'wall_torch', 'iron_bars', 'cobweb'].map(name => 'minecraft:' + name))
const key = position => position.join(',')
const world = new Map(blocks.map(block => [key(block.pos), block]))
assert.equal(world.size, 1584)
const nameAt = (position, map = world) => map.get(key(position))?.state.getName().toString() ?? 'minecraft:air'
const full = position => ['minecraft:stone_bricks', 'minecraft:cracked_stone_bricks', 'minecraft:mossy_stone_bricks', 'minecraft:andesite', 'minecraft:chiseled_stone_bricks'].includes(nameAt(position))
const materials = {}
for (const block of blocks) {
  const name = block.state.getName().toString()
  assert(allowed.has(name), 'Unexpected block ' + name)
  assert(!block.nbt, 'Unexpected block entity')
  materials[name] = (materials[name] ?? 0) + 1
}
const walk = (map = world) => {
  for (let posZ = 0; posZ < 16; posZ += 1) {
    for (let posX = 4; posX <= 6; posX += 1) {
      for (let posY = 1; posY <= 3; posY += 1) assert.equal(nameAt([posX, posY, posZ], map), 'minecraft:air', 'Central lane blocked')
      assert(full([posX, 0, posZ]), 'Missing full floor')
    }
  }
}
walk()
for (let posX = 0; posX < 11; posX += 1) {
  for (let posZ = 0; posZ < 16; posZ += 1) {
    assert(full([posX, 0, posZ]), 'Floor hole')
    assert(full([posX, 8, posZ]), 'Roof hole')
  }
}
for (let posZ = 0; posZ < 16; posZ += 1) {
  for (let posY = 1; posY < 8; posY += 1) {
    for (const posX of [0, 10]) assert(full([posX, posY, posZ]), 'Side branch or wall hole')
  }
}
const torches = blocks.filter(block => block.state.getName().toString() === 'minecraft:wall_torch')
const webs = blocks.filter(block => block.state.getName().toString() === 'minecraft:cobweb')
const brackets = blocks.filter(block => block.state.getName().toString() === 'minecraft:iron_bars')
for (const torch of torches) {
  const [posX, posY, posZ] = torch.pos
  assert.equal(torch.state.getProperties().facing, posX === 1 ? 'east' : 'west')
  assert(full([posX === 1 ? 0 : 10, posY, posZ]), 'Unsupported torch')
}
for (const web of webs) {
  assert(web.pos[1] >= 5 && [1, 9].includes(web.pos[0]), 'Web in player lane')
  assert(full([web.pos[0] === 1 ? 0 : 10, web.pos[1], web.pos[2]]), 'Web is not wall anchored')
}
for (const bracket of brackets) {
  const [posX, posY, posZ] = bracket.pos
  assert(full([posX === 1 ? 0 : 10, posY, posZ]), 'Iron bracket is not wall anchored')
}
const occupied = new Set(blocks.filter(block => block.state.getName().toString() !== 'minecraft:air').map(block => key(block.pos)))
const reached = new Set(['0,0,0'])
const queue = [[0, 0, 0]]
for (let index = 0; index < queue.length; index += 1) {
  const position = queue[index]
  for (const offset of [[1, 0, 0], [-1, 0, 0], [0, 1, 0], [0, -1, 0], [0, 0, 1], [0, 0, -1]]) {
    const neighbor = position.map((value, axis) => value + offset[axis])
    if (occupied.has(key(neighbor)) && !reached.has(key(neighbor))) { reached.add(key(neighbor)); queue.push(neighbor) }
  }
}
assert.equal(reached.size, occupied.size, 'Detached decorative island')
const blocked = new Map(world)
blocked.set('5,1,0', world.get('0,0,0'))
assert.throws(() => walk(blocked), /blocked/)
const metadata = {
  id: 'corridor-S02', source_id: 'S02', name: '陈旧石砖短直廊', kind: 'short-straight-corridor', size,
  length_blocks: 16, length_chunks: 1, axis: 'z', facing: 'north', entrance: [5, 1, 0], interior: [5, 1, 3],
  exit: [5, 1, 15], ports: [{ side: 'north', feet: [5, 1, 0], width: 3, height: 3 }, { side: 'south', feet: [5, 1, 15], width: 3, height: 3 }],
  safe_lane: { min: [4, 1, 0], max: [6, 3, 15], full_floor_y: 0 },
  sha256: digest(bytes), reference, reference_sha256: referenceHash,
  source_documents: ['D:/ft工程文件/image2.5/dungeon-corridors-20261103/SPEC.md', 'D:/ft工程文件/image2.5/dungeon-corridors-20261103/manifest.json', 'D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/corridor_index.json', 'D:/ft工程文件/FT地牢_Image25接入_20261101/CONTRACT.md'],
  feature_coordinates: { arch_planes_z: [0, 5, 10, 15], torch_positions: torches.map(block => block.pos), web_positions: webs.map(block => block.pos), chipped_corbel_positions: [[3, 5, 5], [8, 4, 10]], rubble_positions: [[2, 1, 3], [8, 1, 11]] },
  reference_adaptations: ['参考图尽头的关闭木门改为南侧常开拱口，满足一字贯通与三格宽通路。', '破损仅缺少两处拱下装饰齿，不破坏承重墙、完整屋顶或主路。', '角落蛛网位于 Y=5，碎石贴边；火把原生附着实体外墙，铁栏杆仅作为托架装饰。'],
  runtime_scope: 'Vanilla Java 1.20.1 structure; no FT logic, deployment, slot allocation or player-save changes.'
}
await writeJson('room.json', metadata)
await writeJson('materials.json', materials)
const report = {
  status: 'PASS', structure_sha256: metadata.sha256, reference_sha256: referenceHash, size,
  data_version: 3465, compression: 'gzip', single_palette: true, explicit_volume_records: blocks.length,
  non_air_blocks: occupied.size, air_records: materials['minecraft:air'], bounds_and_unique_positions: true,
  full_floor_blocks: 176, full_roof_blocks: 176, sealed_parallel_side_walls: true, no_turns_branches_or_stairs_in_lane: true,
  continuous_lane: { width: 3, clear_height: 3, length: 16, checked_air_cells: 144, full_floor_cells: 48 },
  entrance_and_exit_open: true, safe_point_reachable_both_directions: true, floor_stepping_required: false,
  torches_anchored: torches.length, iron_brackets_wall_anchored: brackets.length, high_corner_webs: webs.length,
  detached_islands: occupied.size - reached.size, entities: 0, containers: 0, block_entities: 0, inventory_items: 0,
  liquids: 0, forbidden_technical_blocks: 0, central_hazards: 0, negative_probes: { blocked_entrance_rejected: true },
  palette_local_whitelist: [...allowed], limitations: ['Version registry, collision geometry, state validity and live tick survival are checked separately by Forge, not inferred from static geometry.']
}
await writeJson('qa/static.json', report)
const empty = new Structure([1, 1, 1])
empty.addBlock([0, 0, 0], 'minecraft:air')
const emptyPath = path.join(root, 'src/main/resources/data/fts02qa/structures/empty.nbt')
await fs.mkdir(path.dirname(emptyPath), { recursive: true })
await fs.writeFile(emptyPath, empty.writeNbt({ name: '', dataVersion: 3465, compression: 'gzip' }))
console.log(JSON.stringify({ status: report.status, size, sha256: metadata.sha256, non_air: occupied.size, blocks: blocks.length }))
