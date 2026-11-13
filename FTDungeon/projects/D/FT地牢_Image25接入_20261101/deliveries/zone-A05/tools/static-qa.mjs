import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createHash } from 'node:crypto'
import { NbtFile, Structure } from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
await fs.mkdir(path.join(root, 'qa'), { recursive: true })
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const file = NbtFile.read(bytes)
const structure = Structure.fromNbt(file.root)
const blocks = structure.getBlocks()
const digest = data => createHash('sha256').update(data).digest('hex')
const hash = digest(bytes)
const referenceRoot = 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103'
const referenceRelative = 'out/abandoned/A05-abandoned-abandoned-storage-room.png'
const reference = path.join(referenceRoot, referenceRelative)
const expectedReference = 'a04d973577416ee3eb03731a510975c77f62a64240227ba5ce614167e2eb70be'
const manifest = JSON.parse(await fs.readFile(path.join(referenceRoot, 'docs/manifest.json'), 'utf8'))
const verify = JSON.parse(await fs.readFile(path.join(referenceRoot, 'docs/verify.json'), 'utf8'))
const checks = []
const check = (name, success, details) => {
  checks.push({ name, status: success ? 'PASS' : 'FAIL', details })
  if (!success) throw new Error(name + ': ' + JSON.stringify(details))
}
const get = position => structure.getBlock(position)
const nameAt = position => get(position)?.state.getName().toString()
const air = position => nameAt(position) === 'minecraft:air'
const partial = /(?:air|lantern|chain|flower_pot|cobweb|chest|_stairs|_slab|_trapdoor)$/
const full = position => Boolean(nameAt(position)) && !partial.test(nameAt(position))
const materials = {}
for (const block of blocks) {
  const name = block.state.getName().toString()
  materials[name] = (materials[name] ?? 0) + 1
}
check('reference_sha256_matches_request_manifest_and_verify', digest(await fs.readFile(reference)) === expectedReference && manifest.files.find(entry => entry.file === referenceRelative)?.sha256 === expectedReference && verify.records.find(entry => entry.file === referenceRelative)?.sha256 === expectedReference, expectedReference)
check('gzip_java_1201_single_palette', bytes[0] === 31 && bytes[1] === 139 && file.root.getNumber('DataVersion') === 3465 && !file.root.has('palettes'))
check('dense_unique_in_bounds', JSON.stringify(structure.getSize()) === '[21,11,21]' && blocks.length === 4851 && new Set(blocks.map(block => block.pos.join(','))).size === 4851 && blocks.every(block => structure.isInside(block.pos)), { size: structure.getSize(), records: blocks.length })
check('vanilla_blocks_no_entities_no_active_fluids_no_commands', file.root.getList('entities').length === 0 && Object.keys(materials).every(name => name.startsWith('minecraft:')) && !Object.keys(materials).some(name => /minecraft:(?:water|lava|fire|soul_fire|spawner|jigsaw|structure_block|structure_void|.*command_block|sand|gravel|magma_block|campfire|soul_campfire|tnt)$/.test(name)))
check('complete_foundation_and_roof', Array.from({ length: 441 }, (_, index) => [index % 21, Math.floor(index / 21)]).every(([across, depth]) => nameAt([across, 0, depth]) === 'minecraft:deepslate_bricks' && nameAt([across, 10, depth]) === 'minecraft:stone_bricks'), { foundation: 441, roof: 441 })
check('three_wide_three_high_main_aisle', Array.from({ length: 60 }, (_, index) => [9 + index % 3, Math.floor(index / 3)]).every(([across, depth]) => full([across, 1, depth]) && [2, 3, 4].every(height => air([across, height, depth]))), { width: 3, height: 3, length: 20 })
const walkable = position => air(position) && air([position[0], 3, position[2]]) && full([position[0], 1, position[2]])
const reachable = new Set(['10,2,0'])
const queue = [[10, 2, 0]]
while (queue.length) {
  const position = queue.shift()
  for (const [offsetX, offsetZ] of [[1, 0], [-1, 0], [0, 1], [0, -1]]) {
    const candidate = [position[0] + offsetX, 2, position[2] + offsetZ]
    if (candidate[0] < 0 || candidate[0] >= 21 || candidate[2] < 0 || candidate[2] >= 21 || !walkable(candidate) || reachable.has(candidate.join(','))) continue
    reachable.add(candidate.join(','))
    queue.push(candidate)
  }
}
check('entrance_safe_point_and_back_aisle_connected', ['10,2,4', '10,2,19'].every(position => reachable.has(position)), { reachableFeet: reachable.size, entrance: [10, 2, 0], interior: [10, 2, 4] })
const containers = blocks.filter(block => /minecraft:(chest|barrel)$/.test(block.state.getName().toString()))
const vectors = { north: [0, -1], south: [0, 1], east: [1, 0], west: [-1, 0] }
for (const block of containers) {
  const name = block.state.getName().toString()
  const [across, height, depth] = block.pos
  const properties = block.state.getProperties()
  const nbt = block.nbt
  check('empty_container_' + block.pos.join('_'), Boolean(nbt) && nbt.getString('id') === name && nbt.has('Items') && nbt.getList('Items').length === 0 && !nbt.has('LootTable') && !nbt.has('LootTableSeed') && !nbt.has('Lock'))
  if (name === 'minecraft:chest') check('chest_lid_' + block.pos.join('_'), air([across, height + 1, depth]) && properties.type === 'single')
  const [offsetX, offsetZ] = vectors[properties.facing]
  const distance = across === 1 || across === 19 ? 2 : 1
  const approach = [across + offsetX * distance, 2, depth + offsetZ * distance]
  check('container_access_' + block.pos.join('_'), reachable.has(approach.join(',')) && air([across + offsetX, height, depth + offsetZ]), { approach })
  check('container_supported_' + block.pos.join('_'), full([across, height - 1, depth]) || get([across, height - 1, depth])?.state.getProperties().type === 'top')
}
check('container_inventory_count', containers.length === 23 && materials['minecraft:chest'] === 4 && materials['minecraft:barrel'] === 19, { chests: materials['minecraft:chest'], barrels: materials['minecraft:barrel'] })
check('no_unexpected_block_entities', blocks.every(block => !block.nbt || containers.includes(block)))
check('racks_grounded_and_wall_attached', [1, 19].every(across => Array.from({ length: 14 }, (_, index) => index + 3).every(depth => full([across, 1, depth]) && full([across, 2, depth]) && full([across === 1 ? 0 : 20, 4, depth]))) && [[3, 7], [13, 17]].every(([start, end]) => Array.from({ length: end - start + 1 }, (_, index) => start + index).every(across => full([across, 2, 19]) && full([across, 1, 19]) && full([across, 4, 20]))))
check('six_columns_ground_to_beam', [5, 15].every(across => [5, 13, 18].every(depth => Array.from({ length: 9 }, (_, index) => index + 1).every(height => full([across, height, depth])))))
const pots = blocks.filter(block => block.state.getName().toString() === 'minecraft:flower_pot')
check('shelf_pots_supported', pots.length === 14 && pots.every(block => get([block.pos[0], block.pos[1] - 1, block.pos[2]])?.state.getProperties().type === 'top'), pots.length)
check('lantern_chain_and_floor_supports', [5, 13].every(depth => nameAt([10, 7, depth]) === 'minecraft:lantern' && nameAt([10, 8, depth]) === 'minecraft:chain' && full([10, 9, depth])) && full([16, 1, 10]))
check('cobwebs_above_player_aisle', blocks.filter(block => block.state.getName().toString() === 'minecraft:cobweb').every(block => block.pos[1] >= 7 && (block.pos[0] < 9 || block.pos[0] > 11)))
const deviations = ['概念图为剖视；NBT 补齐四壁和 441 格完整屋顶，剖切只发生在预览。', '图中散落谷物、破箱、小陶罐用原版木板/活板门、空花盆和空箱桶表达；不写奖励物品或掉落实体。', '浅积水用完全氧化铜地砖表示水渍，不放活水，主路不放蛛网。', '石柱排为主通道两侧六根落地柱，北侧补三格宽入口，箱盖留出净空。', '独立 Java 1.20.1 模板及隔离 Forge 验证，未接入正式地牢。']
const metadata = {
  id: 'zone-A05', source_id: 'A05', name: '废弃储藏室', category: 'abandoned', minecraft: '1.20.1', data_version: 3465,
  size: structure.getSize(), entrance: [10, 2, 0], interior: [10, 2, 4], safe_point: [10, 2, 4], facing: 'north',
  ports: [{ face: 'north', feet: [10, 2, 0], width: 3, height: 4 }], main_aisle: { x: [9, 11], z: [0, 19], feet_y: 2, clear_height: 3 },
  sha256: hash, nbt: 'room.nbt', records: blocks.length, palette_count: file.root.getList('palette').length,
  reference, reference_sha256: expectedReference, materials,
  empty_containers: containers.map(block => ({ position: block.pos, block: block.state.getName().toString(), facing: block.state.getProperties().facing, items: 0 })),
  reachable_feet: reachable.size, roof_cells: 441, deployed: false, deviations,
}
await fs.writeFile(path.join(root, 'room.json'), JSON.stringify(metadata, null, 2) + '\n')
await fs.writeFile(path.join(root, 'qa/static.json'), JSON.stringify({ status: 'PASS', source_sha256: hash, checks, materials, reachable_feet: reachable.size, container_count: containers.length, deviations }, null, 2) + '\n')
console.log(JSON.stringify({ status: 'PASS', sha256: hash, checks: checks.length, containers: containers.length, reachableFeet: reachable.size }))
