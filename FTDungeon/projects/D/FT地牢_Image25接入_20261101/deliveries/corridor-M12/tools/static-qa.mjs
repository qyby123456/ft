import fs from 'node:fs/promises'
import path from 'node:path'
import crypto from 'node:crypto'
import { fileURLToPath } from 'node:url'
import { NbtFile, Structure } from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
await fs.mkdir(path.join(root, 'qa'), { recursive: true })
const source = 'D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/M12-stone-brick-aged.png'
const referenceHash = 'd525bbd2f9b7059a2ae60caf736231c753665a1b2b6c906194e497fa83db0e8f'
const digest = bytes => crypto.createHash('sha256').update(bytes).digest('hex')
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const nbt = NbtFile.read(bytes)
const structure = Structure.fromNbt(nbt.root)
const blocks = structure.getBlocks()
const failures = []
const checks = []
const verify = (name, condition, details) => {
  checks.push({ name, pass: Boolean(condition), details })
  if (!condition) failures.push(name)
}
const get = position => structure.getBlock(position)?.state
const identifier = position => get(position)?.getName().toString()
const fullMasonry = new Set(['stone_bricks', 'mossy_stone_bricks', 'cracked_stone_bricks', 'chiseled_stone_bricks', 'polished_andesite', 'polished_blackstone'].map(name => `minecraft:${name}`))
const forbidden = /(?:command_block|structure_block|structure_void|jigsaw|spawner|bedrock|barrier|lava|water|fire|tnt|sand|gravel)$/
verify('reference_sha256', digest(await fs.readFile(source)) === referenceHash, referenceHash)
verify('gzip_java_single_palette', bytes[0] === 31 && bytes[1] === 139 && !nbt.root.has('palettes') && nbt.root.getNumber('DataVersion') === 3465, { data_version: nbt.root.getNumber('DataVersion'), compression: nbt.compression })
verify('medium_40_block_length', JSON.stringify(structure.getSize()) === '[13,11,40]', structure.getSize())
verify('complete_explicit_volume_unique_in_bounds', blocks.length === 5720 && new Set(blocks.map(block => block.pos.join(','))).size === 5720 && blocks.every(block => structure.isInside(block.pos)), { records: blocks.length, expected: 5720 })
verify('no_entities_or_block_entity_inventory', nbt.root.getList('entities').length === 0 && blocks.every(block => !block.nbt), { entities: nbt.root.getList('entities').length, block_entities: blocks.filter(block => block.nbt).length })
verify('vanilla_only_no_forbidden_or_fluid', blocks.every(block => block.state.getName().namespace === 'minecraft' && !forbidden.test(block.state.getName().path)), 'Native Forge validates exact registry identities and property enums separately.')
let roofCells = 0
let floorCells = 0
let sealedSideCells = 0
let laneCells = 0
for (let blockX = 0; blockX < 13; blockX++) {
  for (let blockZ = 0; blockZ < 40; blockZ++) {
    if (fullMasonry.has(identifier([blockX, 10, blockZ]))) roofCells++
    if (fullMasonry.has(identifier([blockX, 0, blockZ]))) floorCells++
  }
}
for (const blockX of [0, 12]) {
  for (let blockY = 1; blockY < 10; blockY++) {
    for (let blockZ = 0; blockZ < 40; blockZ++) if (fullMasonry.has(identifier([blockX, blockY, blockZ]))) sealedSideCells++
  }
}
for (let blockX = 4; blockX <= 8; blockX++) {
  for (let blockY = 1; blockY <= 4; blockY++) {
    for (let blockZ = 0; blockZ < 40; blockZ++) if (identifier([blockX, blockY, blockZ]) === 'minecraft:air') laneCells++
  }
}
verify('full_roof_no_skylight_holes', roofCells === 520, { full_cube_cells: roofCells })
verify('complete_solid_floor', floorCells === 520, { full_cube_cells: floorCells })
verify('parallel_sealed_sidewalls_no_branches', sealedSideCells === 720, { full_cube_cells: sealedSideCells })
let pierJointCells = 0
for (const blockZ of [10, 20, 30]) {
  for (const blockX of [2, 10]) {
    if (fullMasonry.has(identifier([blockX, 6, blockZ])) && fullMasonry.has(identifier([blockX, 7, blockZ])) && fullMasonry.has(identifier([blockX, 8, blockZ]))) pierJointCells++
  }
}
verify('internal_piers_face_connect_to_arch_beams', pierJointCells === 6, { full_face_joints: pierJointCells })
verify('straight_five_wide_four_high_continuous_lane', laneCells === 800, { air_cells: laneCells, feet_y: 1, min_width: 5, no_steps: true })
const materialCounts = {}
for (const block of blocks) {
  const name = block.state.getName().toString()
  materialCounts[name] = (materialCounts[name] ?? 0) + 1
}
const candles = blocks.filter(block => block.state.is('minecraft:candle'))
verify('eight_lit_supported_candle_niches', candles.length === 8 && candles.every(block => block.state.getProperty('lit') === 'true' && fullMasonry.has(identifier([block.pos[0], block.pos[1] - 1, block.pos[2]]))), { clusters: candles.length, candles: 24 })
const chains = blocks.filter(block => block.state.is('minecraft:chain'))
verify('eight_roof_anchored_cages', chains.length === 8 && chains.every(block => fullMasonry.has(identifier([block.pos[0], 10, block.pos[2]])) && identifier([block.pos[0], 8, block.pos[2]]) === 'minecraft:polished_blackstone'), { chains: chains.length, cages: 8 })
let cageRingCells = 0
for (const cageZ of [6, 16, 26, 35]) {
  for (const cageX of [3, 7]) {
    for (let localX = 0; localX < 3; localX++) {
      for (let localZ = 0; localZ < 3; localZ++) {
        if (localX === 0 || localX === 2 || localZ === 0 || localZ === 2) {
          for (const blockY of [6, 7]) if (identifier([cageX + localX, blockY, cageZ + localZ]) === 'minecraft:iron_bars') cageRingCells++
        }
      }
    }
  }
}
verify('complete_suspended_iron_cage_rings', cageRingCells === 128 && blocks.filter(block => block.state.is('minecraft:iron_trapdoor')).every(block => block.state.getProperty('open') === 'false'), { connected_bar_cells: cageRingCells, all_lids_closed: true })
const roundTrip = Structure.fromNbt(NbtFile.read(structure.writeNbt({ dataVersion: 3465, compression: 'gzip' })).root)
verify('lodestone_roundtrip_exact', roundTrip.getBlocks().length === 5720 && blocks.every(block => roundTrip.getBlock(block.pos)?.state.toString() === block.state.toString()), { records: roundTrip.getBlocks().length })
const metadata = {
  id: 'corridor-M12', source_design_id: 'M12', name: '陈旧石砖吊笼烛廊', category: 'straight-medium-corridor',
  size: [13, 11, 40], entrance: [6, 1, 0], interior: [6, 1, 3], exit: [6, 1, 39], facing: 'north',
  exits: [{ face: 'north', feet: [6, 1, 0] }, { face: 'south', feet: [6, 1, 39] }],
  sha256: digest(bytes), reference: source, reference_sha256: referenceHash,
  minecraft: '1.20.1', data_version: 3465, builder: 'Minecraft Builder skill / Lodestone 0.6.0',
  route: { axis: 'Z', length: 40, width: 5, height: 4, x: [4, 8], feet_y: 1, z: [0, 39], turns: 0, branches: 0, stairs: 0 },
  landmarks: { cages: 8, candle_niches: 8, candles: 24, stone_ribs: 5, corner_cobwebs: materialCounts['minecraft:cobweb'] },
  integration: { candidate_only: true, slot: null, production_deployed: false, player_save_modified: false, note: 'corridor-M12 is distinct from the existing M12 fungal-cellar room. Coordinator assigns final integration ID/slot.' }
}
await fs.writeFile(path.join(root, 'room.json'), JSON.stringify(metadata, null, 2) + '\n')
await fs.writeFile(path.join(root, 'qa/materials.json'), JSON.stringify(materialCounts, null, 2) + '\n')
const report = { status: failures.length ? 'FAIL' : 'PASS', sha256: digest(bytes), checks, failures, records: blocks.length, non_air: blocks.filter(block => !block.state.is('minecraft:air')).length, note: 'Static checks do not replace real Forge placement and settled-state checks.' }
await fs.writeFile(path.join(root, 'qa/static.json'), JSON.stringify(report, null, 2) + '\n')
console.log(JSON.stringify(report, null, 2))
if (failures.length) process.exitCode = 1
