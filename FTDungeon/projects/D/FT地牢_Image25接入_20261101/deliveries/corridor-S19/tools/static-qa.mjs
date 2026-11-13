import fs from 'node:fs/promises'
import path from 'node:path'
import assert from 'node:assert/strict'
import { createHash } from 'node:crypto'
import { fileURLToPath, pathToFileURL } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const skillRoot = 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
const { NbtFile, Structure } = await import(pathToFileURL(`${skillRoot}/node_modules/@mattzh72/lodestone/lib/index.js`))
const reference = 'D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/S19-obsidian-trial-intact.png'
const expectedReference = '420458b3957e67b0f5c6b995fdf154363c12656f9027084270574e4c7b556c95'
const digest = bytes => createHash('sha256').update(bytes).digest('hex')
const writeJson = async (filename, data) => fs.writeFile(path.join(root, filename), JSON.stringify(data, null, 2) + '\n')
await fs.mkdir(path.join(root, 'qa'), { recursive: true })
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const hash = digest(bytes)
assert.equal(digest(await fs.readFile(reference)), expectedReference)
assert.equal(bytes[0], 0x1f)
assert.equal(bytes[1], 0x8b)
const file = NbtFile.read(bytes)
const structure = Structure.fromNbt(file.root)
assert.deepEqual(structure.getSize(), [15, 11, 16])
assert.equal(file.root.getNumber('DataVersion'), 3465)
assert.equal(file.root.has('palettes'), false)
assert.equal(file.root.getList('entities').length, 0)
const blocks = structure.getBlocks()
assert.equal(blocks.length, 2640)
assert.equal(file.root.getList('blocks').length, blocks.length)
const names = new Map()
const nameAt = position => structure.getBlock(position)?.state.getName().path
const solid = position => {
  const name = nameAt(position)
  return name && !['air', 'chain', 'end_rod', 'iron_bars'].includes(name) && !name.endsWith('_slab') && !name.endsWith('_stairs')
}
const forbidden = new Set(['command_block', 'chain_command_block', 'repeating_command_block', 'structure_block', 'structure_void', 'jigsaw', 'spawner', 'chest', 'barrel', 'hopper', 'dispenser', 'dropper', 'water', 'lava', 'fire', 'soul_fire', 'magma_block', 'cobweb', 'powder_snow'])
const positions = new Set()
for (const block of blocks) {
  const name = block.state.getName()
  assert.equal(name.namespace, 'minecraft')
  assert(!forbidden.has(name.path), name.path)
  assert(!block.nbt, 'No inventory or block-entity payloads')
  assert.equal(block.state.isWaterlogged(), false)
  assert(!positions.has(block.pos.join(',')))
  positions.add(block.pos.join(','))
  names.set(name.path, (names.get(name.path) ?? 0) + 1)
}
let roofCells = 0
let coreAir = 0
for (let posX = 0; posX < 15; posX++) {
  for (let posZ = 0; posZ < 16; posZ++) {
    assert(solid([posX, 10, posZ]))
    assert(solid([posX, 0, posZ]))
    roofCells++
  }
}
for (let posZ = 0; posZ < 16; posZ++) {
  for (let posX = 6; posX <= 8; posX++) {
    assert(solid([posX, 1, posZ]))
    for (let posY = 2; posY <= 4; posY++) {
      assert.equal(nameAt([posX, posY, posZ]), 'air')
      coreAir++
    }
  }
}
const clearFoot = position => nameAt(position) === 'air' && nameAt([position[0], position[1] + 1, position[2]]) === 'air' && solid([position[0], position[1] - 1, position[2]])
const queue = [[7, 2, 0]]
const reachable = new Set(['7,2,0'])
for (let queueIndex = 0; queueIndex < queue.length; queueIndex++) {
  const current = queue[queueIndex]
  for (const [offsetX, offsetZ] of [[1, 0], [-1, 0], [0, 1], [0, -1]]) {
    const candidate = [current[0] + offsetX, 2, current[2] + offsetZ]
    if (!reachable.has(candidate.join(',')) && clearFoot(candidate)) {
      reachable.add(candidate.join(','))
      queue.push(candidate)
    }
  }
}
for (const target of [[7, 2, 3], [7, 2, 12], [7, 2, 15]]) assert(reachable.has(target.join(',')))
for (const centerX of [4, 10]) {
  for (const centerZ of [4, 11]) {
    assert.equal(nameAt([centerX, 8, centerZ]), 'chain')
    assert(solid([centerX, 9, centerZ]))
    assert.equal(nameAt([centerX, 7, centerZ]), 'iron_block')
    assert.equal(nameAt([centerX, 6, centerZ]), 'air')
    assert.equal(nameAt([centerX, 5, centerZ]), 'polished_blackstone_slab')
  }
}
assert.equal(names.get('iron_bars'), 32)
assert.equal(names.get('chain'), 4)
assert.equal(names.get('end_rod'), 20)
const metadata = {
  id: 'CORRIDOR_S19', design_id: 'S19', name: '黑曜试炼短廊 · 金纹悬笼', category: 'straight_corridor', kind: 'short',
  size: [15, 11, 16], length_blocks: 16, footprint_chunks_when_aligned: [1, 1], entrance: [7, 2, 0], interior: [7, 2, 3], facing: 'north',
  exit: [7, 2, 15], exit_facing: 'south', exit_interior: [7, 2, 12], portal_clearance: [3, 3],
  through_route: { axis: 'z', x_range: [6, 8], feet_y: 2, headroom: 3, z_range: [0, 15], turns: 0, branches: 0, stairs: 0 },
  sha256: hash, data_version: 3465, minecraft: '1.20.1', reference, reference_sha256: expectedReference,
  batch_spec: 'D:/ft工程文件/image2.5/dungeon-corridors-20261103/SPEC.md',
  design_traits: { theme: 'obsidian-trial', condition: 'intact', light: 'end-rods', feature: 'hanging-cages', cages: 4 },
  adaptations: ['远端封闭金纹门转译为同主题开放拱门，确保一字贯通', '熔岩缝转译为菌光体和橙色染色玻璃，零流体零伤害', '金纹用原版金块与镶金黑石体素镶嵌，不含自定义贴图', '末地烛采用原版白光；无人工青色光照或粒子'],
  suggested_resource_id: 'ftdungeon:rooms/corridor_s19_obsidian_trial', slot_assigned: false, deployed: false,
  safety: { complete_roof: true, entities: 0, block_entities: 0, containers: 0, fluids: 0, generated_loot: false, clear_occupied_world: false },
  placement_contract: '完整体积和外边界/实体/流体预检，占用拒绝；一次性持久标记；不得清空或覆盖玩家修改；由统一接入负责人分配唯一槽位'
}
await writeJson('room.json', metadata)
await writeJson('qa/materials.json', Object.fromEntries([...names].sort((first, second) => second[1] - first[1])))
await writeJson('qa/static.json', {
  status: 'PASS', nbt_sha256: hash, reference_sha256: expectedReference, size: [15, 11, 16], data_version: 3465,
  explicit_records: blocks.length, explicit_air: names.get('air'), single_palette: true, gzip: true,
  unique_in_bounds_records: positions.size, roof_full_cells: roofCells, sealed_bottom_cells: 240,
  continuous_three_by_three_air_cells: coreAir, main_path_floor_cells: 48, dry_reachable_feet: reachable.size,
  entrance_exit_bidirectional: true, cages: 4, anchored_chains: 4, iron_bars: 32, end_rods: 20,
  entities: 0, block_entities: 0, fluids: 0, forbidden_blocks: 0,
  limitations: '静态检查不是 Forge 放置验收；原生注册表、旋转、邻居更新与稳定性以 qa/native.json 为准'
})
const empty = new Structure([1, 1, 1])
empty.addBlock([0, 0, 0], 'minecraft:air')
const emptyDir = path.join(root, 'forge/src/main/resources/data/fts19qa/structures')
await fs.mkdir(emptyDir, { recursive: true })
await fs.writeFile(path.join(emptyDir, 'empty.nbt'), empty.writeNbt({ dataVersion: 3465, compression: 'gzip' }))
console.log(JSON.stringify({ status: 'PASS', hash, records: blocks.length, reachable: reachable.size }))
