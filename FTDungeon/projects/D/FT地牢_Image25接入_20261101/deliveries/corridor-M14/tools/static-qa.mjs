import fs from 'node:fs/promises'
import path from 'node:path'
import crypto from 'node:crypto'
import { fileURLToPath } from 'node:url'
import { NbtFile, Structure } from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const reference = 'D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/M14-fungal-cave-aged.png'
const referenceSha256 = 'aeea1468c5e12912daa7b14bded792897778cd3331cca76999c302646e11ea31'
const hash = bytes => crypto.createHash('sha256').update(bytes).digest('hex')
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const sha256 = hash(bytes)
const file = NbtFile.read(bytes)
const structure = Structure.fromNbt(file.root)
const records = structure.getBlocks()
const blocks = new Map(records.map(block => [block.pos.join(','), block]))
const nameAt = position => blocks.get(position.join(','))?.state.getName().toString().replace('minecraft:', '')
const counts = {}
for (const block of records) {
  const name = block.state.getName().toString()
  counts[name] = (counts[name] || 0) + 1
}
const checks = []
function check(name, condition, detail) {
  checks.push({ name, status: condition ? 'PASS' : 'FAIL', detail })
  if (!condition) throw new Error(name + ': ' + JSON.stringify(detail))
}
await fs.mkdir(path.join(root, 'qa'), { recursive: true })
try {
  check('reference_sha256', hash(await fs.readFile(reference)) === referenceSha256, referenceSha256)
  check('gzip_single_palette_java_1_20_1', bytes[0] === 31 && bytes[1] === 139 && file.root.getNumber('DataVersion') === 3465 && !file.root.has('palettes'), 3465)
  check('size_15_12_41', JSON.stringify(structure.getSize()) === '[15,12,41]', structure.getSize())
  check('full_unique_bounded_records', records.length === 7380 && blocks.size === 7380 && records.every(block => block.pos.every((value, axis) => Number.isInteger(value) && value >= 0 && value < [15, 12, 41][axis])), records.length)
  check('no_entities_or_block_entities', file.root.getList('entities').length === 0 && records.every(block => !block.nbt), { entities: 0, inventories: 0 })
  const forbidden = /command_block|structure_block|structure_void|jigsaw|spawner|lava|water$|fire$|magma_block|cactus|powder_snow|berry_bush|wither_rose|campfire/
  check('vanilla_no_active_hazards', Object.keys(counts).every(name => name.startsWith('minecraft:') && !forbidden.test(name)), Object.keys(counts))
  let roofCells = 0
  let floorCells = 0
  for (let blockX = 0; blockX < 15; blockX++) {
    for (let blockZ = 0; blockZ < 41; blockZ++) {
      if (nameAt([blockX, 11, blockZ]) === 'deepslate_tiles') roofCells++
      if (nameAt([blockX, 0, blockZ]) !== 'air' && nameAt([blockX, 1, blockZ]) !== 'air') floorCells++
    }
  }
  check('full_roof_double_floor', roofCells === 615 && floorCells === 615, { roofCells, floorCells })
  let shellClosed = true
  for (let blockY = 2; blockY < 11; blockY++) {
    for (let blockZ = 0; blockZ < 41; blockZ++) {
      for (const blockX of [0, 14]) shellClosed &&= nameAt([blockX, blockY, blockZ]) !== 'air'
    }
  }
  check('no_side_branches_closed_sidewalls', shellClosed, 738)
  let portsValid = true
  for (const blockZ of [0, 40]) {
    for (let blockX = 0; blockX < 15; blockX++) {
      for (let blockY = 2; blockY < 11; blockY++) {
        const opening = blockX >= 6 && blockX <= 8 && blockY <= 6
        portsValid &&= (nameAt([blockX, blockY, blockZ]) === 'air') === opening
      }
    }
  }
  check('two_3x5_open_ports', portsValid, { north: [7, 2, 0], south: [7, 2, 40] })
  let aisleClear = true
  for (let blockZ = 1; blockZ < 40; blockZ++) {
    for (let blockX = 5; blockX <= 9; blockX++) {
      for (let blockY = 2; blockY <= 5; blockY++) aisleClear &&= nameAt([blockX, blockY, blockZ]) === 'air'
    }
  }
  check('five_wide_four_high_straight_aisle', aisleClear, { width: 5, height: 4, length: 39, stairs: 0 })
  const passable = position => nameAt(position) === 'air' && nameAt([position[0], 3, position[2]]) === 'air' && nameAt([position[0], 1, position[2]]) !== 'air'
  const pending = [[7, 2, 0]]
  const visited = new Set(['7,2,0'])
  for (let cursor = 0; cursor < pending.length; cursor++) {
    const current = pending[cursor]
    for (const [offsetX, offsetZ] of [[-1, 0], [1, 0], [0, -1], [0, 1]]) {
      const neighbor = [current[0] + offsetX, 2, current[2] + offsetZ]
      const identity = neighbor.join(',')
      if (neighbor[0] >= 0 && neighbor[0] < 15 && neighbor[2] >= 0 && neighbor[2] < 41 && passable(neighbor) && !visited.has(identity)) {
        visited.add(identity)
        pending.push(neighbor)
      }
    }
  }
  check('entrance_interior_exit_roundtrip', visited.has('7,2,4') && visited.has('7,2,40'), visited.size)
  const solid = position => ![undefined, 'air', 'hanging_roots', 'spore_blossom', 'glow_lichen', 'cobweb', 'warped_fungus'].includes(nameAt(position))
  let supportsValid = true
  let supportedDecorations = 0
  for (const block of records) {
    const [blockX, blockY, blockZ] = block.pos
    const name = nameAt(block.pos)
    if (['spore_blossom', 'hanging_roots', 'chain'].includes(name)) {
      supportsValid &&= solid([blockX, blockY + 1, blockZ])
      supportedDecorations++
    }
    if (['potted_red_mushroom', 'warped_fungus', 'lantern'].includes(name)) {
      supportsValid &&= solid([blockX, blockY - 1, blockZ])
      supportedDecorations++
    }
  }
  check('decorative_supports', supportsValid, supportedDecorations)
  check('theme_features_preserved', counts['minecraft:red_mushroom_block'] === 60 && counts['minecraft:mushroom_stem'] === 18 && counts['minecraft:sea_lantern'] === 6 && counts['minecraft:lantern'] === 8 && counts['minecraft:chain'] === 8 && counts['minecraft:spore_blossom'] === 6, counts)
  const metadata = { id: 'corridor-M14', design_id: 'M14', category: 'medium_straight_corridor', name: '陈旧菌洞吊笼直廊', size: [15, 12, 41], entrance: [7, 2, 0], interior: [7, 2, 4], exit: [7, 2, 40], facing: 'north', ports: [{ facing: 'north', feet: [7, 2, 0], opening: [3, 5] }, { facing: 'south', feet: [7, 2, 40], opening: [3, 5] }], data_version: 3465, sha256, reference, reference_sha256: referenceSha256, builder: 'Minecraft Builder Skill / Lodestone 0.6.0', recipe: 'recipe.mjs', length_blocks: 41, chunk_length: 2.5625, path: { axis: 'z', level_y: 2, interior_x: [5, 9], clear_height: 4, no_turns: true, no_branches: true, no_stairs: true }, adaptations: ['Orange glass over shroomlight replaces exposed lava; no live fluids or fire.', 'Sea lantern and warped-wart caps represent cyan luminous mushrooms.', 'Stable podzol fungal beds and potted mushrooms avoid uncontrolled spreading.'], entities: 0, block_entities: 0, deployed: false, production_saves_touched: false }
  await fs.writeFile(path.join(root, 'room.json'), JSON.stringify(metadata, null, 2) + '\n')
  const report = { status: 'PASS', sha256, reference_sha256: referenceSha256, size: structure.getSize(), records: records.length, counts, summary: { check_count: checks.length, roof_cells: roofCells, floor_cells: floorCells, dry_reachable_feet: visited.size, supported_decorations: supportedDecorations }, checks }
  await fs.writeFile(path.join(root, 'qa', 'static.json'), JSON.stringify(report, null, 2) + '\n')
  console.log(JSON.stringify({ status: report.status, sha256, counts, summary: report.summary }, null, 2))
} catch (error) {
  await fs.writeFile(path.join(root, 'qa', 'static.json'), JSON.stringify({ status: 'FAIL', sha256, error: error.message, checks }, null, 2) + '\n')
  throw error
}
