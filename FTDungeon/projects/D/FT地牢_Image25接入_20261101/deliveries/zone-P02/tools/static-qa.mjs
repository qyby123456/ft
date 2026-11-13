import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath, pathToFileURL } from 'node:url'
import { createHash } from 'node:crypto'
import assert from 'node:assert/strict'
import { plan } from '../recipe.mjs'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const skill = 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
const { NbtFile, Structure } = await import(pathToFileURL(path.join(skill, 'node_modules/@mattzh72/lodestone/lib/index.js')).href)
const hash = bytes => createHash('sha256').update(bytes).digest('hex')
const write = async (name, value) => fs.writeFile(path.join(root, name), JSON.stringify(value, null, 2) + '\n')
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
assert.equal(bytes[0], 31)
assert.equal(bytes[1], 139)
assert.equal(hash(await fs.readFile(plan.reference)), plan.referenceSha256)
const docs = 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103'
const manifest = JSON.parse(await fs.readFile(path.join(docs, 'docs/manifest.json'), 'utf8'))
const verify = JSON.parse(await fs.readFile(path.join(docs, 'docs/verify.json'), 'utf8'))
const sourceRecord = manifest.files.find(entry => entry.file.endsWith('P02-parkour-water-channel-jumps.png'))
assert.equal(sourceRecord.sha256, plan.referenceSha256)
const verifiedRecord = verify.records.find(entry => JSON.stringify(entry).includes('P02-parkour-water-channel-jumps.png'))
assert(verifiedRecord && JSON.stringify(verifiedRecord).includes(plan.referenceSha256))
const nbt = NbtFile.read(bytes)
const structure = Structure.fromNbt(nbt.root)
const blocks = structure.getBlocks()
const world = new Map(blocks.map(block => [block.pos.join(','), block]))
const at = (posX, posY, posZ) => world.get([posX, posY, posZ].join(','))
const nameAt = (posX, posY, posZ) => at(posX, posY, posZ)?.state.getName().toString()
const fullNames = new Set(['deepslate_bricks', 'stone_bricks', 'cracked_stone_bricks', 'mossy_stone_bricks', 'chiseled_stone_bricks', 'polished_andesite', 'glass', 'barrel'].map(name => 'minecraft:' + name))
const full = (posX, posY, posZ) => fullNames.has(nameAt(posX, posY, posZ))
const allowed = new Set([...fullNames, ...['air', 'water', 'stone_brick_stairs', 'dark_oak_fence', 'chain', 'lantern', 'cyan_wall_banner'].map(name => 'minecraft:' + name)])
assert.deepEqual(structure.getSize(), plan.size)
assert.equal(nbt.root.getNumber('DataVersion'), 3465)
assert.equal(nbt.root.has('palettes'), false)
assert.equal(nbt.root.getList('entities').length, 0)
assert.equal(blocks.length, 7293)
assert.equal(world.size, 7293)
const materials = {}
let furniture = 0
for (const block of blocks) {
  const name = block.state.getName().toString()
  assert(allowed.has(name), 'Unknown block ' + name)
  assert(block.pos.every((coordinate, axis) => coordinate >= 0 && coordinate < plan.size[axis]))
  materials[name] = (materials[name] ?? 0) + 1
  if (block.nbt) {
    furniture += 1
    assert(['minecraft:barrel', 'minecraft:cyan_wall_banner'].includes(name))
    assert(!block.nbt.has('LootTable'))
    assert(!block.nbt.has('Items') || block.nbt.getList('Items').length === 0)
  }
  if (name === 'minecraft:lantern') {
    const [posX, posY, posZ] = block.pos
    if (block.state.getProperties().hanging === 'true') {
      for (let supportY = posY + 1; supportY < 10; supportY += 1) assert.equal(nameAt(posX, supportY, posZ), 'minecraft:chain')
      assert(full(posX, 10, posZ))
    } else assert(full(posX, posY - 1, posZ))
  }
  if (name === 'minecraft:cyan_wall_banner') assert(full(0, block.pos[1], block.pos[2]))
}
assert.equal(furniture, 7)
for (let posX = 0; posX < 17; posX += 1) for (let posZ = 0; posZ < 39; posZ += 1) for (const posY of [0, 1, 10]) assert(full(posX, posY, posZ), 'Base/roof hole')
for (const posZ of [0, 38]) for (let posX = 7; posX <= 9; posX += 1) {
  assert(full(posX, 2, posZ))
  for (let posY = 3; posY <= 5; posY += 1) assert.equal(nameAt(posX, posY, posZ), 'minecraft:air', 'Port blocked')
}
const dry = (posX, posZ) => full(posX, 2, posZ) && [3, 4, 5].every(posY => nameAt(posX, posY, posZ) === 'minecraft:air')
const dryReach = new Set(['8,0'])
const queue = [[8, 0]]
for (let cursor = 0; cursor < queue.length; cursor += 1) {
  const [posX, posZ] = queue[cursor]
  for (const [nextX, nextZ] of [[posX - 1, posZ], [posX + 1, posZ], [posX, posZ - 1], [posX, posZ + 1]]) {
    const key = [nextX, nextZ].join(',')
    if (!dryReach.has(key) && dry(nextX, nextZ)) { dryReach.add(key); queue.push([nextX, nextZ]) }
  }
}
assert(dryReach.has('8,3'), 'Entrance is not linked to safe point')
assert(!dryReach.has('8,38'), 'Dry walking bypass defeats parkour')
const jumps = []
for (let index = 0; index < plan.platforms.length - 1; index += 1) {
  const from = plan.platforms[index]
  const target = plan.platforms[index + 1]
  const gap = target.min[2] - from.max[2] - 1
  assert.equal(gap, 1)
  assert(from.min[0] <= 8 && from.max[0] >= 8 && target.min[0] <= 8 && target.max[0] >= 8)
  for (let posZ = from.max[2]; posZ <= target.min[2]; posZ += 1) for (let posY = 3; posY <= 6; posY += 1) assert.equal(nameAt(8, posY, posZ), 'minecraft:air', 'Jump headroom')
  jumps.push({ from: from.id, to: target.id, clear_gap_blocks: gap, height_delta: 0, minimum_headroom: 4, takeoff_xz: [8.5, from.max[2] + 0.72], landing_first_cell_xz: [8.5, target.min[2] + 0.5] })
}
for (const safe of [plan.entrance, plan.interior, plan.checkpoint, plan.exit]) {
  assert(dry(safe[0], safe[2]), 'Unsafe feet position')
}
for (const posZ of plan.recoveryPlanes) for (const posX of [4, 12]) for (let offset = -1; offset <= 1; offset += 1) {
  assert.equal(nameAt(posX, 2, posZ + offset), 'minecraft:stone_brick_stairs')
  assert.equal(at(posX, 2, posZ + offset).state.getProperties().waterlogged, 'true')
  assert.equal(nameAt(posX, 3, posZ + offset), 'minecraft:air')
}
const wet = blocks.filter(block => block.state.getName().toString() === 'minecraft:water' || block.state.getProperties().waterlogged === 'true')
const wetSet = new Set(wet.map(block => block.pos.join(',')))
for (const block of wet) {
  const [posX, posY, posZ] = block.pos
  assert(posX > 0 && posX < 16 && posZ > 0 && posZ < 38)
  if (posY === 2) assert(full(posX, 1, posZ))
  for (const [nextX, nextY, nextZ] of [[posX - 1, posY, posZ], [posX + 1, posY, posZ], [posX, posY, posZ - 1], [posX, posY, posZ + 1], [posX, posY - 1, posZ]]) {
    assert(wetSet.has([nextX, nextY, nextZ].join(',')) || full(nextX, nextY, nextZ), 'Unsealed fluid neighbor ' + [nextX, nextY, nextZ])
  }
}
const flooded = new Set()
const pools = []
for (const block of wet.filter(block => block.pos[1] === 2)) {
  const start = block.pos.join(',')
  if (flooded.has(start)) continue
  const poolQueue = [block.pos]
  flooded.add(start)
  const recoveries = new Set()
  for (let cursor = 0; cursor < poolQueue.length; cursor += 1) {
    const [posX, posY, posZ] = poolQueue[cursor]
    if ([4, 12].includes(posX)) recoveries.add(posX + ',' + posZ)
    for (const position of [[posX - 1, posY, posZ], [posX + 1, posY, posZ], [posX, posY, posZ - 1], [posX, posY, posZ + 1]]) {
      const key = position.join(',')
      if (wetSet.has(key) && !flooded.has(key)) { flooded.add(key); poolQueue.push(position) }
    }
  }
  assert(recoveries.size > 0, 'Pool has no recovery exit')
  pools.push({ cells: poolQueue.length, recovery_stair_cells: recoveries.size })
}
const metadata = {
  id: 'zone-P02', source_id: 'P02', name: '苔石水渠跳台', kind: 'water-channel-parkour', size: plan.size,
  entrance: plan.entrance, interior: plan.interior, exit: plan.exit, facing: 'north',
  ports: [{ side: 'north', feet: plan.entrance, width: 3, height: 3 }, { side: 'south', feet: plan.exit, width: 3, height: 3 }],
  safe_points: [{ id: 'entry', feet: plan.interior }, { id: 'midpoint-rest', feet: plan.checkpoint }, { id: 'exit', feet: [8, 3, 36] }],
  parkour: { difficulty: 'easy', jumps: 8, edge_gap_blocks: 1, platform_top_y: 3, minimum_platform_size: [3, 3], platforms: plan.platforms, route: jumps, reversible: true },
  recovery: { water_depth_blocks: 1, basin_floor_y: 1, water_y: 2, max_failure_drop_from_walkway: 1, stair_ports: plan.recoveryPlanes.flatMap(posZ => [{ side: 'west', feet: [4, 3, posZ] }, { side: 'east', feet: [12, 3, posZ] }]), return: 'Both side galleries return to the north starting deck. Midpoint is a dry rest platform; no spawnpoint or teleport is installed.' },
  sha256: hash(bytes), reference: plan.reference, reference_sha256: plan.referenceSha256,
  source_documents: [docs + '/README.md', docs + '/docs/manifest.json', docs + '/docs/verify.json', 'D:/ft工程文件/FT地牢_Image25接入_20261101/CONTRACT.md'],
  reference_adaptations: ['完整石屋顶仅在预览中剖切。', '跳台设为有基座的3×3落脚面，八次同高一格净空短跳。', '保留两侧木栏、苔石柱梁、空木桶与青色旗帜。', '水深一格；四组双侧含水台阶提供失败恢复。', '末端玻璃护面封闭水景避免瀑布冲刷跳台；出口位于南侧中轴。', '南端两侧设挡墙，阻止侧廊直接步行绕过跳跃路线；水中恢复不作计分或强制挑战。'],
  integrated: false, deployed: false, runtime_scope: 'Vanilla Java 1.20.1 structure. No FT logic, deployment, slot allocation, command blocks or player-save changes.',
}
await write('room.json', metadata)
await write('qa/static.json', { status: 'PASS', structure_sha256: hash(bytes), reference_sha256: plan.referenceSha256, size: plan.size, data_version: 3465, explicit_records: blocks.length, palette_entries: nbt.root.getList('palette').length, materials, source_manifest_record: sourceRecord, source_verify_record: verifiedRecord, checks: { gzip_single_palette: true, bounds_unique_complete_volume: true, full_roof_floor_and_basin: true, dry_three_by_three_ports: true, entry_to_safe_point_walkable: true, no_dry_walking_bypass: true, empty_containers_and_no_entities: true, supported_lights_and_banners: true, fluid_local_seal: true, every_pool_has_recovery_stairs: true, eight_short_jumps: true, jump_headroom: true }, jumps, water_cells: wet.length, shallow_pools: pools, dry_reachable_positions: dryReach.size, limitations: ['Static geometry is supplemented by Forge native collision, jump travel and 200 ticking placement evidence.'] })
console.log(JSON.stringify({ status: 'PASS', sha256: metadata.sha256, jumps: jumps.length, water_cells: wet.length, pools, materials }, null, 2))
