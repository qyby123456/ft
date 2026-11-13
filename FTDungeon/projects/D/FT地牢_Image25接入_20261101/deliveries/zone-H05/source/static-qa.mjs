import fs from 'node:fs/promises'
import path from 'node:path'
import crypto from 'node:crypto'
import { fileURLToPath } from 'node:url'
import { NbtFile, Structure } from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'
import { hall, roofHeight } from './recipe.mjs'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const hash = bytes => crypto.createHash('sha256').update(bytes).digest('hex')
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const sha256 = hash(bytes)
const file = NbtFile.read(bytes)
const structure = Structure.fromNbt(file.root)
const records = structure.getBlocks()
const blocks = new Map(records.map(block => [block.pos.join(','), block]))
const at = (blockX, blockY, blockZ) => blocks.get([blockX, blockY, blockZ].join(','))?.state.getName().toString().replace('minecraft:', '')
const counts = {}
for (const record of records) {
  const name = record.state.getName().toString()
  counts[name] = (counts[name] || 0) + 1
}
const checks = []
const check = (name, condition, evidence) => {
  checks.push({ name, status: condition ? 'PASS' : 'FAIL', evidence })
}
await fs.mkdir(path.join(root, 'qa'), { recursive: true })
const referenceBytes = await fs.readFile(hall.reference)
const referenceRoot = 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103'
const manifest = JSON.parse(await fs.readFile(referenceRoot + '/docs/manifest.json', 'utf8'))
const verification = JSON.parse(await fs.readFile(referenceRoot + '/docs/verify.json', 'utf8'))
const relativeReference = 'out/halls/H05-halls-vaulted-grand-hall.png'
const entries = [manifest.files.find(entry => entry.file === relativeReference), verification.records.find(entry => entry.file === relativeReference)]
check('reference_hash_and_batch_records', hash(referenceBytes) === hall.referenceSha256 && entries.every(entry => entry?.sha256 === hall.referenceSha256 && entry?.bytes === referenceBytes.length && entry?.ok), { path: hall.reference, sha256: hall.referenceSha256, bytes: referenceBytes.length, batch: '35/35', png: [referenceBytes.readUInt32BE(16), referenceBytes.readUInt32BE(20)] })
check('java_1_20_1_gzip_single_palette', bytes[0] === 31 && bytes[1] === 139 && file.root.getNumber('DataVersion') === 3465 && !file.root.has('palettes'), { dataVersion: 3465, compression: 'gzip' })
check('dimensions_and_unique_full_volume', JSON.stringify(structure.getSize()) === JSON.stringify(hall.size) && records.length === 32643 && blocks.size === 32643 && records.every(record => record.pos.every((coordinate, axis) => Number.isInteger(coordinate) && coordinate >= 0 && coordinate < hall.size[axis])), { size: hall.size, records: records.length, explicitAir: counts['minecraft:air'] })
check('no_entities_inventories_or_block_entities', file.root.getList('entities').length === 0 && records.every(record => !record.nbt), { entities: 0, block_entities: 0, inventories: 0 })
const forbidden = /command_block|structure_block|structure_void|jigsaw|spawner|lava|water$|fire$|magma_block|cactus|powder_snow|berry_bush|wither_rose|campfire|gravel|sand$|anvil/
check('vanilla_palette_and_no_active_hazards', Object.keys(counts).every(name => name.startsWith('minecraft:') && !forbidden.test(name)), { palette: Object.keys(counts), fluids: 0, falling_blocks: 0, open_fire: 0, decorative_lit_candles: counts['minecraft:candle'] })
let floorCells = 0
let roofColumns = 0
let roofStepSeams = 0
for (let blockX = 0; blockX < 31; blockX++) {
  for (let blockZ = 0; blockZ < 39; blockZ++) {
    if (at(blockX, 0, blockZ) !== 'air' && at(blockX, 1, blockZ) !== 'air') floorCells++
    if (at(blockX, roofHeight(blockX), blockZ) !== 'air' && at(blockX, roofHeight(blockX) + 1, blockZ) !== 'air') roofColumns++
    if (blockX < 30) {
      const lower = Math.min(roofHeight(blockX), roofHeight(blockX + 1))
      const upper = Math.max(roofHeight(blockX), roofHeight(blockX + 1))
      let joined = false
      for (let blockY = lower; blockY <= upper + 1; blockY++) if (at(blockX, blockY, blockZ) !== 'air' && at(blockX + 1, blockY, blockZ) !== 'air') joined = true
      if (joined) roofStepSeams++
    }
  }
}
check('complete_double_floor_and_vault_shell', floorCells === 1209 && roofColumns === 1209 && roofStepSeams === 1170, { floor_columns: floorCells, two_layer_vault_columns: roofColumns, face_joined_roof_seams: roofStepSeams, note: 'full vaulted shell; upper bounding-box air is intentional' })
let pillarCells = 0
for (const [centerX, centerZ] of hall.piers) {
  for (let blockY = 1; blockY <= 17; blockY++) if (at(centerX, blockY, centerZ) !== 'air') pillarCells++
}
check('six_grounded_continuous_piers', pillarCells === 102, { piers: hall.piers, center_spine_cells: pillarCells, foot_y: 1, capital_y: 17 })
let anchors = 0
for (const [centerX, baseY, centerZ] of hall.chandeliers) {
  let anchored = at(centerX, baseY, centerZ) === 'polished_blackstone' && at(centerX, roofHeight(centerX), centerZ) !== 'air'
  for (let blockY = baseY + 1; blockY < roofHeight(centerX); blockY++) anchored &&= at(centerX, blockY, centerZ) === 'chain'
  if (anchored) anchors++
}
check('four_chandeliers_anchored_to_final_roof', anchors === 4, { anchored: anchors, positions: hall.chandeliers })
let aisleCells = 0
for (let blockX = 13; blockX <= 17; blockX++) {
  for (let blockZ = 0; blockZ <= 30; blockZ++) {
    for (let blockY = 2; blockY <= 5; blockY++) if (at(blockX, blockY, blockZ) === 'air') aisleCells++
  }
}
check('five_wide_four_high_main_aisle', aisleCells === 620, { width: 5, headroom: 4, length: 31, clear_voxels: aisleCells, entry: hall.entrance, safe: hall.safe })
const airQueue = [[-1, -1, -1]]
const exteriorAir = new Set(['-1,-1,-1'])
for (let cursor = 0; cursor < airQueue.length; cursor++) {
  const current = airQueue[cursor]
  for (const offset of [[1, 0, 0], [-1, 0, 0], [0, 1, 0], [0, -1, 0], [0, 0, 1], [0, 0, -1]]) {
    const neighbor = current.map((coordinate, axis) => coordinate + offset[axis])
    if (neighbor.some((coordinate, axis) => coordinate < -1 || coordinate > hall.size[axis])) continue
    const [blockX, blockY, blockZ] = neighbor
    const identity = neighbor.join(',')
    if (exteriorAir.has(identity)) continue
    if (blockZ === 0 && blockX >= 11 && blockX <= 19 && blockY >= 2 && blockY <= 10) continue
    const material = at(blockX, blockY, blockZ)
    if (material && material !== 'air') continue
    exteriorAir.add(identity)
    airQueue.push(neighbor)
  }
}
const leaks = []
for (let blockX = 2; blockX <= 28; blockX++) {
  for (let blockZ = 2; blockZ <= 36; blockZ++) {
    for (let blockY = 2; blockY < roofHeight(blockX) - 1; blockY++) if (at(blockX, blockY, blockZ) === 'air' && exteriorAir.has([blockX, blockY, blockZ].join(','))) leaks.push([blockX, blockY, blockZ])
  }
}
check('closed_shell_when_only_declared_entry_is_sealed', leaks.length === 0, { leaks: leaks.slice(0, 10), leak_count: leaks.length, method: 'six-neighbor exterior-air flood over expanded NBT bounds; north opening temporarily sealed only in analysis' })
const occupied = records.filter(record => record.state.getName().toString() !== 'minecraft:air')
const connected = new Set(['0,0,0'])
const solidQueue = [[0, 0, 0]]
for (let cursor = 0; cursor < solidQueue.length; cursor++) {
  const current = solidQueue[cursor]
  for (const offset of [[1, 0, 0], [-1, 0, 0], [0, 1, 0], [0, -1, 0], [0, 0, 1], [0, 0, -1]]) {
    const neighbor = current.map((coordinate, axis) => coordinate + offset[axis])
    const identity = neighbor.join(',')
    if (!connected.has(identity) && blocks.has(identity) && at(...neighbor) !== 'air') { connected.add(identity); solidQueue.push(neighbor) }
  }
}
check('no_detached_block_components', connected.size === occupied.length, { connected_non_air: connected.size, total_non_air: occupied.length, physical_canSurvive: 'checked separately by actual Forge placement' })
const report = { status: checks.every(check => check.status === 'PASS') ? 'PASS' : 'FAIL', sha256, checks, counts }
const metadata = { ...hall, sha256, minecraft: '1.20.1', dataVersion: 3465, format: 'vanilla gzip structure NBT', records: records.length, nonAir: occupied.length, paletteCount: Object.keys(counts).length, northEntrance: { feet: hall.entrance, clearWidth: 9, centerHeadroom: 9 }, safePoint: { feet: hall.safe, clearFootprint: [5, 5], clearHeadroom: 4 }, mainAisle: { x: [13, 17], z: [0, 30], feetY: 2, clearHeadroom: 4 }, platform: { feet: [15.5, 4.0625, 35.5], note: 'two south-facing stair flights; red carpet on final dais adds 1/16 block' }, roofProfile: Array.from({ length: 31 }, (_, blockX) => roofHeight(blockX)), integrationStatus: 'independent delivery; no production files modified; not deployed', rotations: ['NONE', 'CLOCKWISE_90', 'CLOCKWISE_180', 'COUNTERCLOCKWISE_90'], deviations: ['Concept cutaway converted to fully enclosed three-span vaulted hall.', 'Blueprint scaled to 31 x 27 x 39 with six grounded piers.', 'Five-wide flush red-wool runner used for the level main aisle; actual carpet retained on the dais.', 'Teal and gold heraldry uses terracotta inlays rather than custom banner textures.', 'Crates and bookshelves are decorative; no inventories, loot, mobs, rewards or active FT interactions.', 'Vanilla default-pack lighting differs from the image concept; previews are software renders.'] }
await fs.writeFile(path.join(root, 'room.json'), JSON.stringify(metadata, null, 2) + '\n')
await fs.writeFile(path.join(root, 'qa/static.json'), JSON.stringify(report, null, 2) + '\n')
console.log(JSON.stringify({ status: report.status, sha256, records: records.length, nonAir: occupied.length, checks: checks.map(check => ({ name: check.name, status: check.status })) }, null, 2))
if (report.status !== 'PASS') process.exitCode = 1
