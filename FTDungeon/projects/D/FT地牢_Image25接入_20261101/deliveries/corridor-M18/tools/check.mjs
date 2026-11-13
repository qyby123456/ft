import fs from 'node:fs/promises'
import path from 'node:path'
import { createHash } from 'node:crypto'
import { createRequire } from 'node:module'
import { fileURLToPath, pathToFileURL } from 'node:url'
import recipe from '../recipe.mjs'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const require = createRequire('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/package.json')
const { NbtFile, Structure } = await import(pathToFileURL(require.resolve('@mattzh72/lodestone')).href)
const fingerprint = bytes => createHash('sha256').update(bytes).digest('hex')
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const file = NbtFile.read(bytes)
const structure = Structure.fromNbt(file.root)
const { metadata } = recipe({ createStructure: () => ({}), setBlock: () => undefined, fill: () => undefined })
const records = structure.getBlocks()
const names = new Map(records.map(block => [block.pos.join(','), block.state.getName().toString()]))
const at = (across, height, depth) => names.get([across, height, depth].join(','))
const checks = []
const check = (name, condition, details) => {
  checks.push({ name, status: condition ? 'PASS' : 'FAIL', details })
  if (!condition) throw new Error(`${name}: ${JSON.stringify(details)}`)
}
await fs.mkdir(path.join(root, 'qa'), { recursive: true })
check('reference_sha256', fingerprint(await fs.readFile(metadata.reference)) === metadata.reference_sha256, metadata.reference_sha256)
check('gzip_java_1_20_1', bytes[0] === 31 && bytes[1] === 139 && file.root.getNumber('DataVersion') === 3465, 'gzip / DataVersion 3465')
check('size_and_explicit_volume', structure.getSize().join(',') === '15,12,48' && records.length === 8640 && names.size === records.length, records.length)
check('no_entities_or_block_entities', file.root.getList('entities').length === 0 && records.every(block => !block.nbt || block.nbt.size === 0), 'No mobs, loot, containers or block-entity payloads')
const forbidden = new Set(['ice', 'frosted_ice', 'water', 'lava', 'powder_snow', 'snow', 'fire', 'soul_fire', 'magma_block', 'cactus', 'cobweb', 'spawner', 'command_block', 'chain_command_block', 'repeating_command_block', 'jigsaw', 'structure_block', 'structure_void', 'light'])
check('vanilla_only_no_hazards', records.every(block => { const name = block.state.getName().toString(); return name.startsWith('minecraft:') && !forbidden.has(name.slice(10)) && block.state.getProperties().waterlogged !== 'true' }), 'Nonmelting packed/blue ice; no liquids or hidden blocks')
check('continuous_full_roof', Array.from({ length: 15 * 48 }, (_, index) => at(index % 15, 11, Math.floor(index / 15))).every(name => name === 'minecraft:stone_bricks'), '720 roof blocks; no cutaway in delivered NBT')
const aisle = []
for (let depth = 0; depth < 48; depth += 1) {
  for (let across = 5; across <= 9; across += 1) {
    for (let height = 1; height <= 3; height += 1) aisle.push(at(across, height, depth) === 'minecraft:air')
    aisle.push(['minecraft:packed_ice', 'minecraft:blue_ice', 'minecraft:calcite', 'minecraft:smooth_stone'].includes(at(across, 0, depth)))
  }
}
check('straight_five_wide_three_high_aisle', aisle.every(Boolean), '48 blocks, no turns, stairs or side exits')
check('solid_side_enclosure', Array.from({ length: 10 * 48 }, (_, index) => [at(0, index % 10 + 1, Math.floor(index / 10)), at(14, index % 10 + 1, Math.floor(index / 10))]).flat().every(name => name !== 'minecraft:air'), 'No branch openings')
check('candles_supported', metadata.blueprint.candle_supports.every(item => at(...item.candle) === 'minecraft:candle' && at(...item.support) === 'minecraft:calcite'), '12 lit candle clusters')
check('cages_chain_anchored_and_headroom', metadata.blueprint.cages.every(item => at(...item.chain) === 'minecraft:chain' && at(...item.anchor) === 'minecraft:stone_bricks' && at(...item.cap) === 'minecraft:chiseled_stone_bricks' && item.bottom[1] >= 5), '6 empty cages; floor y=5; player foot y=1')
check('icicle_connected', metadata.blueprint.icicles.every(item => at(...item.cap) === 'minecraft:blue_ice' && at(...item.tip) === 'minecraft:light_blue_stained_glass_pane' && at(...item.anchor) === 'minecraft:stone_brick_stairs'), '12 nonmelting icicle ornaments')
check('connections_idempotent', structure.clone().updateBlockStates().updatedBlocks === 0, 'Iron bars and glass panes normalized by Lodestone')
const counts = {}
for (const name of names.values()) counts[name] = (counts[name] ?? 0) + 1
await fs.writeFile(path.join(root, 'room.json'), `${JSON.stringify({ ...metadata, name: metadata.title, schema_version: 1, batch_id: 'M18', kind: 'medium_straight_corridor', minecraft: '1.20.1', data_version: 3465, sha256: fingerprint(bytes), length_blocks: 48, length_chunks: 3, port: { width: 5, height: 4, north_z: 0, south_z: 47, floor_y: 0, feet_y: 1 }, deployed: false, player_saves_modified: false, block_counts: counts }, null, 2)}\n`)
const report = { status: 'PASS', structure_sha256: fingerprint(bytes), source: 'room.nbt read using Lodestone 0.6.0', checks, block_records: records.length, solid_and_decorative_blocks: records.length - (counts['minecraft:air'] ?? 0), block_counts: counts, limitations: ['Static checks do not substitute for Forge collision and block-registry verification.', 'No FT core integration, production save or client gameplay tested.'] }
await fs.writeFile(path.join(root, 'qa/static.json'), `${JSON.stringify(report, null, 2)}\n`)
console.log(JSON.stringify(report, null, 2))
