import fs from 'node:fs/promises'
import path from 'node:path'
import { createHash } from 'node:crypto'
import { createRequire } from 'node:module'
import { fileURLToPath, pathToFileURL } from 'node:url'
import recipe, { blueprint, footprint, shaft, boundary } from '../recipe.mjs'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const require = createRequire('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/package.json')
const { NbtFile, Structure } = await import(pathToFileURL(require.resolve('@mattzh72/lodestone')).href)
const hash = bytes => createHash('sha256').update(bytes).digest('hex')
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const file = NbtFile.read(bytes)
const structure = Structure.fromNbt(file.root)
const records = structure.getBlocks()
const index = new Map(records.map(block => [block.pos.join(','), block]))
const at = (across, height, depth) => index.get([across, height, depth].join(','))
const nameAt = (...position) => at(...position)?.state.getName().toString()
const air = (...position) => nameAt(...position) === 'minecraft:air'
const solid = (...position) => !['minecraft:air', 'minecraft:lantern', 'minecraft:oak_wall_sign', 'minecraft:chain'].includes(nameAt(...position))
const { metadata } = recipe({ createStructure: () => ({}), setBlock: () => {}, fill: () => {}, createBlockNbt: data => data })
const checks = []
const check = (name, success, details) => checks.push({ name, status: success ? 'PASS' : 'FAIL', details })
await fs.mkdir(path.join(root, 'qa'), { recursive: true })
check('reference_sha256', hash(await fs.readFile(blueprint.reference)) === blueprint.reference_sha256, blueprint.reference_sha256)
check('gzip_and_version', bytes[0] === 31 && bytes[1] === 139 && file.root.getNumber('DataVersion') === 3465 && !file.root.has('palettes'), 'Java 1.20.1 / single palette / DataVersion 3465')
check('explicit_full_volume', records.length === 22050 && index.size === 22050 && structure.getSize().join(',') === blueprint.size.join(','), records.length)
check('no_entities', file.root.getList('entities').length === 0, 'No mobs, item frames, loot or commands')
const forbidden = new Set(['lava', 'water', 'fire', 'soul_fire', 'powder_snow', 'cobweb', 'magma_block', 'cactus', 'spawner', 'jigsaw', 'structure_block', 'command_block', 'chain_command_block', 'repeating_command_block', 'structure_void', 'light'])
check('vanilla_no_hazards', records.every(block => block.state.getName().toString().startsWith('minecraft:') && !forbidden.has(block.state.getName().toString().slice(10)) && block.state.getProperties().waterlogged !== 'true'), 'Registry and complete state enums independently checked in Forge')
const roof = []
const exterior = []
const shaftSafety = []
for (let across = 0; across < 35; across++) {
  for (let depth = 0; depth < 35; depth++) {
    if (footprint(across, depth)) roof.push(solid(across, 17, depth))
    if (shaft(across, depth)) {
      shaftSafety.push(solid(across, 0, depth))
      for (let height = 1; height < 17; height++) shaftSafety.push(air(across, height, depth))
    }
    if (!boundary(across, depth)) continue
    for (let height = 1; height < 17; height++) {
      const port = ((depth === 0 || depth === 34) && across >= 16 && across <= 18 && height <= 3) || ((across === 0 || across === 34) && depth >= 16 && depth <= 18 && height >= 13 && height <= 15)
      exterior.push(port ? air(across, height, depth) : solid(across, height, depth))
    }
  }
}
check('complete_octagonal_roof', roof.every(Boolean), { roof_blocks: roof.length, cutaway_only_in_renderer: true })
check('exact_four_external_ports', exterior.every(Boolean), 'North/south feet +1, east/west feet +13; all other boundary cells closed')
check('sealed_open_shaft', shaftSafety.every(Boolean), 'Octagonal shaft is a guarded ornament, not a vertical connector')
check('continuous_1_5_high_balustrade', metadata.balustrade.every(([across, depth]) => solid(across, 6, depth) && solid(across, 7, depth) && nameAt(across, 8, depth) === 'minecraft:stone_brick_slab'), { segments: metadata.balustrade.length, height_above_hub_floor: 1.5 })
const stairDetails = []
for (const flight of blueprint.stairs) {
  const [deltaAcross, deltaDepth] = flight.direction
  let success = true
  for (let step = 0; step < 6; step++) {
    for (let lane = -1; lane <= 1; lane++) {
      const across = flight.first[0] + deltaAcross * step + (deltaAcross === 0 ? lane : 0)
      const depth = flight.first[2] + deltaDepth * step + (deltaAcross === 0 ? 0 : lane)
      const height = flight.first[1] + step
      const state = at(across, height, depth)?.state
      success &&= state?.getName().toString() === 'minecraft:stone_brick_stairs' && state.getProperties().facing === flight.facing && state.getProperties().half === 'bottom' && state.getProperties().shape === 'straight'
      for (let below = 0; below < height; below++) success &&= solid(across, below, depth)
      for (let above = 1; above <= 3; above++) success &&= air(across, height + above, depth)
    }
  }
  stairDetails.push({ flight: flight.id, success, stair_blocks: 18, rise: 6, run: 6, half_rises: 12, width: 3 })
}
check('four_supported_continuous_stairs', stairDetails.every(flight => flight.success), stairDetails)
check('all_lamps_supported', metadata.lamps.every(lamp => nameAt(...lamp.position) === 'minecraft:lantern' && (lamp.hanging ? nameAt(...lamp.support) === 'minecraft:chain' && solid(...lamp.anchor) : solid(...lamp.support))), { lamps: metadata.lamps.length })
check('four_floor_to_roof_pillars', blueprint.pillars.every(([across, depth]) => Array.from({ length: 18 }, (_, height) => solid(across, height, depth)).every(Boolean)), blueprint.pillars)
check('four_supported_signs_only_block_entities', records.filter(block => block.nbt && block.nbt.size > 0).length === 4 && metadata.signs.every(sign => nameAt(...sign.position) === 'minecraft:oak_wall_sign' && solid(...sign.support)), 'Four waxed signs; no containers or rewards')
check('safe_points_flat_supported', blueprint.safe_points.every(point => solid(point.feet[0], point.feet[1] - 1, point.feet[2]) && [0, 1, 2].every(offset => air(point.feet[0], point.feet[1] + offset, point.feet[2]))), blueprint.safe_points)
const counts = {}
for (const record of records) {
  const blockName = record.state.getName().toString()
  counts[blockName] = (counts[blockName] ?? 0) + 1
}
const room = { ...metadata, name: blueprint.name, schema_version: 1, category: 'transport_hub', minecraft: '1.20.1', data_version: 3465, sha256: hash(bytes), block_records: records.length, block_counts: counts, external_vertical_ports: [], deployed: false, player_saves_modified: false, interfaces_note: 'Four horizontal external mouths on two elevations. Vertical travel is internal stairs: feet 1 ↔ 7 ↔ 13. Central shaft sealed and nontraversable.' }
await fs.writeFile(path.join(root, 'room.json'), JSON.stringify(room, null, 2) + '\n')
const report = { status: checks.every(entry => entry.status === 'PASS') ? 'PASS' : 'FAIL', structure_sha256: room.sha256, reference_sha256: blueprint.reference_sha256, checks, block_records: records.length, block_counts: counts, limitations: ['Static geometry checks; native collision and neighbor stability require the Forge report.'] }
await fs.writeFile(path.join(root, 'qa/static.json'), JSON.stringify(report, null, 2) + '\n')
console.log(JSON.stringify({ status: report.status, checks: checks.length, failures: checks.filter(entry => entry.status === 'FAIL'), structure_sha256: room.sha256 }, null, 2))
if (report.status !== 'PASS') process.exitCode = 1
