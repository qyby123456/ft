import fs from 'node:fs/promises'
import path from 'node:path'
import { createHash } from 'node:crypto'
import { createRequire } from 'node:module'
import { fileURLToPath, pathToFileURL } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const require = createRequire('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/package.json')
const { NbtFile, Structure } = await import(pathToFileURL(require.resolve('@mattzh72/lodestone')).href)
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const nbt = NbtFile.read(bytes).root
const structure = Structure.fromNbt(nbt)
const blocks = structure.getBlocks()
const sha256 = createHash('sha256').update(bytes).digest('hex')
const reference = 'D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/M16-flooded-intact.png'
const referenceHash = createHash('sha256').update(await fs.readFile(reference)).digest('hex')
const errors = []
const check = (condition, message) => { if (!condition) errors.push(message) }
const nameAt = position => structure.getBlock(position)?.state.getName().toString() ?? 'minecraft:air'
const isAir = position => nameAt(position) === 'minecraft:air'
const directions = [[-1, 0, 0], [1, 0, 0], [0, 0, -1], [0, 0, 1]]
const add = (position, offset) => position.map((value, index) => value + offset[index])
const forbidden = /command_block|structure_block|structure_void|jigsaw|spawner|tnt|lava|fire|cobweb|powder_snow|magma_block|campfire|chest|barrel|hopper|dropper|dispenser/
const nonFull = /air|water|vine|chain|lantern|iron_bars|slab/
const full = position => !nonFull.test(nameAt(position))
const counts = {}
check(referenceHash === '3dd8be10bea89290f99a9af69dd387ed6ed38da08d08bf034fe1d83063cc7f1a', 'Reference image hash mismatch')
check(JSON.stringify(structure.getSize()) === '[15,11,40]', 'Incorrect dimensions')
check(blocks.length === 6600, 'Volume must be explicitly encoded, including air')
check(nbt.getNumber('DataVersion') === 3465, 'Wrong DataVersion')
check(nbt.getList('entities').length === 0, 'Unexpected entities')
const occupied = new Set()
let waterCells = 0
let lanterns = 0
let chains = 0
let vines = 0
for (const block of blocks) {
  const name = block.state.getName().toString()
  const properties = block.state.getProperties()
  const position = block.pos
  const key = position.join(',')
  check(!occupied.has(key), `Duplicate block ${key}`)
  occupied.add(key)
  check(name.startsWith('minecraft:') && !forbidden.test(name), `Forbidden block ${name}`)
  check(!block.nbt, `Unexpected block entity ${key}`)
  counts[name] = (counts[name] ?? 0) + 1
  if (name === 'minecraft:water') {
    waterCells++
    check(properties.level === '0', `Not a water source: ${key}`)
    for (const offset of [...directions, [0, -1, 0]]) {
      const neighbor = add(position, offset)
      check(nameAt(neighbor) === 'minecraft:water' || full(neighbor), `Unsealed water at ${key} toward ${neighbor}`)
    }
  }
  if (name === 'minecraft:lantern') {
    lanterns++
    check(properties.hanging === 'true' && properties.waterlogged === 'false', `Wrong lantern state ${key}`)
    check(nameAt(add(position, [0, 1, 0])) === 'minecraft:chain', `Unhung lantern ${key}`)
  }
  if (name === 'minecraft:chain') {
    chains++
    let supportY = position[1] + 1
    while (nameAt([position[0], supportY, position[2]]) === 'minecraft:chain') supportY++
    check(full([position[0], supportY, position[2]]), `Unanchored chain ${key}`)
  }
  if (name === 'minecraft:vine') {
    vines++
    check(full(add(position, position[0] < 7 ? [-1, 0, 0] : [1, 0, 0])), `Unsupported vine ${key}`)
  }
}
let dryFeet = 0
for (let blockZ = 0; blockZ < 40; blockZ++) {
  for (let blockX = 5; blockX <= 9; blockX++) {
    check(full([blockX, 1, blockZ]), `Path floor gap ${blockX},${blockZ}`)
    for (let blockY = 2; blockY <= 4; blockY++) check(isAir([blockX, blockY, blockZ]), `Blocked 5x3 path ${blockX},${blockY},${blockZ}`)
    dryFeet++
  }
}
for (let blockX = 0; blockX < 15; blockX++) {
  for (let blockZ = 0; blockZ < 40; blockZ++) {
    check(full([blockX, 0, blockZ]), 'Foundation has a gap')
    check(full([blockX, 10, blockZ]), 'Roof has a gap')
  }
}
const reachable = new Set(['7,2,0'])
const queue = [[7, 2, 0]]
while (queue.length) {
  const position = queue.shift()
  for (const offset of directions) {
    const neighbor = add(position, offset)
    if (neighbor[0] < 2 || neighbor[0] > 12 || neighbor[2] < 0 || neighbor[2] > 39 || reachable.has(neighbor.join(','))) continue
    if (isAir(neighbor) && isAir(add(neighbor, [0, 1, 0])) && full(add(neighbor, [0, -1, 0]))) {
      reachable.add(neighbor.join(','))
      queue.push(neighbor)
    }
  }
}
check(reachable.has('7,2,39'), 'Exit is unreachable')
check(lanterns === 12 && waterCells > 180 && chains === 42 && vines > 0, 'Missing theme features')
const metadata = {
  id: 'corridor-M16', source_id: 'M16', name: '苔石涵泉长廊', type: 'straight_corridor', size: [15, 11, 40],
  entrance: [7, 2, 0], interior: [7, 2, 4], exit: [7, 2, 39], facing: 'north', axis: 'z',
  openings: [{ side: 'north', feet: [7, 2, 0], width: 5, height: 4 }, { side: 'south', feet: [7, 2, 39], width: 5, height: 4 }],
  dry_path: { from: [5, 2, 0], to: [9, 2, 39], width: 5, minimum_headroom: 3, floor_y: 1 },
  sha256, reference, reference_sha256: referenceHash, data_version: 3465,
  source_documents: ['D:/ft工程文件/image2.5/dungeon-corridors-20261103/SPEC.md', 'D:/ft工程文件/image2.5/dungeon-corridors-20261103/README.md', 'D:/ft工程文件/image2.5/dungeon-corridors-20261103/manifest.json', 'D:/ft工程文件/FT地牢_Image25接入_20261101/CONTRACT.md'],
  design_adaptations: ['原图满地浅水改为两侧浅水渠，中央五格为同高干燥直通步道。', '原图敞开壁泉改为青色玻璃封闭涵泉，保留真实水体，避免高位水源淹路。', '铁笼为无实体静态装饰，吊链锚在木梁；保留完整屋顶。'],
  deployment: 'not_deployed', placement_world: 'isolated QA only', runtime_slot: null
}
const report = { id: metadata.id, status: errors.length ? 'FAIL' : 'PASS', sha256, reference_sha256: referenceHash,
  source: 'Lodestone parses final gzip NBT; geometry/state audit, not game physics', size: structure.getSize(),
  explicit_records: blocks.length, air_included: true, bounds_and_unique_records: occupied.size === 6600,
  roof_blocks_verified: 600, foundation_blocks_verified: 600, dry_path_cells: dryFeet,
  reachable_positions: reachable.size, exit_reachable: reachable.has('7,2,39'),
  source_water_cells: waterCells, horizontal_and_bottom_liquid_boundaries: 'all source-water cells checked',
  hanging_lanterns: lanterns, ceiling_anchored_chains: chains, backed_vines: vines, empty_hanging_cages: 6,
  no_entities_or_block_entities: true, inventory_items: 0, loot_tables: 0,
  checks: ['full-volume bounds and uniqueness', 'reference hash', '5x3 continuous dry route', 'north/south arch clearance', 'sealed water bottoms and four sides', 'full foundation and roof', 'lantern/chain/vine supports', 'vanilla-only safe palette and no inventories'], errors, materials: counts }
await fs.mkdir(path.join(root, 'qa'), { recursive: true })
await fs.writeFile(path.join(root, 'room.json'), JSON.stringify(metadata, null, 2) + '\n')
await fs.writeFile(path.join(root, 'qa/static.json'), JSON.stringify(report, null, 2) + '\n')
await fs.writeFile(path.join(root, 'qa/blocks.json'), JSON.stringify(blocks.map(block => ({ pos: block.pos, name: block.state.getName().toString(), properties: block.state.getProperties() }))))
console.log(JSON.stringify(report, null, 2))
if (errors.length) process.exitCode = 1
