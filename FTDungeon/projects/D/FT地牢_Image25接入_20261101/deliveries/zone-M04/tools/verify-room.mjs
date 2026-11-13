import fs from 'node:fs/promises'
import path from 'node:path'
import crypto from 'node:crypto'
import { fileURLToPath } from 'node:url'
import { NbtFile, Structure } from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const reference = 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/mines/M04-mines-lava-seam-mine.png'
const expectedReference = '8f286ff92140350e6a640892d251f870954b4e1ead0117ae5cce2f50950d5ac2'
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const hash = crypto.createHash('sha256').update(bytes).digest('hex')
const referenceHash = crypto.createHash('sha256').update(await fs.readFile(reference)).digest('hex')
const structure = Structure.fromNbt(NbtFile.read(bytes).root)
const entries = structure.getBlocks()
const blocks = new Map(entries.map(entry => [entry.pos.join(','), entry]))
const get = (width, height, depth) => blocks.get([width, height, depth].join(','))?.state.getName().toString().replace('minecraft:', '')
const checks = []
function check(name, condition, details) {
  checks.push({ name, status: condition ? 'PASS' : 'FAIL', details })
  if (!condition) throw new Error(`${name}: ${JSON.stringify(details)}`)
}
check('reference_sha256', referenceHash === expectedReference, referenceHash)
check('full_unique_bounded_volume', entries.length === 8750 && blocks.size === 8750 && structure.getSize().join(',') === '25,14,25', { entries: entries.length, size: structure.getSize() })
const forbidden = /command_block|structure_block|jigsaw|spawner|end_portal|nether_portal|end_gateway|^fire$|soul_fire|cobweb|powder_snow|tnt/
check('vanilla_no_execution_or_reward_payload', entries.every(entry => !forbidden.test(get(...entry.pos)) && !entry.nbt), 'No injected inventory, loot tables or block entity payloads; native test separately validates empty auto-created containers and absent entities.')
const lava = entries.filter(entry => get(...entry.pos) === 'lava')
const faceOffsets = [[1, 0, 0], [-1, 0, 0], [0, 1, 0], [0, -1, 0], [0, 0, 1], [0, 0, -1]]
const sealing = new Set(['lava', 'polished_basalt', 'deepslate_tiles', 'polished_andesite', 'orange_stained_glass'])
check('lava_six_face_seal', lava.length === 57 && lava.every(entry => faceOffsets.every(offset => sealing.has(get(...entry.pos.map((value, index) => value + offset[index]))))), { source_blocks: lava.length, bounds: [[3, 1, 13], [21, 1, 15]], capped: true })
check('complete_roof', Array.from({ length: 625 }, (_, index) => get(index % 25, 13, Math.floor(index / 25))).every(block => block === 'deepslate_tiles'), '625 full roof blocks; preview cutaway never alters NBT.')
let routeCells = 0
for (let width = 11; width <= 13; width++) {
  for (let depth = 0; depth <= 23; depth++) {
    if (get(width, 2, depth) === 'polished_andesite' && [3, 4, 5].every(height => ['air', 'rail'].includes(get(width, height, depth)))) routeCells++
  }
}
check('three_wide_dry_route_and_bridge', routeCells === 72, { checked_cells: routeCells, feet_min: [11, 3, 0], feet_max: [13, 3, 23], clear_height: 3 })
check('five_by_five_safe_landing', Array.from({ length: 25 }, (_, index) => [10 + index % 5, 2 + Math.floor(index / 5)]).every(([width, depth]) => get(width, 2, depth) === 'polished_andesite' && [3, 4, 5].every(height => get(width, height, depth) === 'air')), { feet_bounds: [[10, 3, 2], [14, 3, 6]] })
check('four_grounded_roof_supports', [4, 20].every(width => [5, 20].every(depth => Array.from({ length: 12 }, (_, index) => get(width, index + 2, depth)).every(block => ['polished_blackstone_bricks', 'deepslate_tiles', 'cracked_deepslate_bricks', 'polished_basalt', 'stripped_spruce_log', 'iron_block'].includes(block)))), 'Four columns at (4,5),(20,5),(4,20),(20,20), continuous floor to roof.')
check('lantern_chain_anchors', entries.filter(entry => get(...entry.pos) === 'lantern').length === 6 && entries.filter(entry => get(...entry.pos) === 'lantern').every(entry => {
  const [width, height, depth] = entry.pos
  return Array.from({ length: 11 - height }, (_, index) => get(width, height + index + 1, depth)).every(block => block === 'chain') && ['stripped_spruce_log', 'chiseled_polished_blackstone'].includes(get(width, 12, depth)) && get(width, 13, depth) === 'deepslate_tiles'
}), 'Six lamps connected vertically to complete roof.')
const combustible = entries.filter(entry => /spruce|barrel/.test(get(...entry.pos)))
check('lava_fire_spread_clearance', lava.every(source => combustible.every(entry => Math.max(Math.abs(source.pos[0] - entry.pos[0]), Math.abs(source.pos[2] - entry.pos[2])) > 3 || entry.pos[1] - source.pos[1] > 4)), { combustible_records: combustible.length, sealed_lava: true, active_fire: 0 })
const palette = {}
for (const entry of entries) { const name = entry.state.getName().toString(); palette[name] = (palette[name] ?? 0) + 1 }
const metadata = {
  id: 'zone-M04', source_id: 'M04', batch: 'dungeon-zone-batch-20261103', category: 'mine_room', name: '熔岩矿脉', minecraft: '1.20.1', data_version: 3465,
  size: [25, 14, 25], entrance: [12, 3, 0], interior: [12, 3, 4], facing: 'north', sha256: hash, reference, reference_sha256: referenceHash,
  ports: [{ side: 'north', center: [12, 3, 0], width: 3, height: 4, floor_y: 2 }],
  safe_route: { min: [11, 3, 0], max: [13, 3, 23], width: 3, clear_height: 3 }, safe_landing: { min: [10, 3, 2], max: [14, 3, 6] },
  lava: { sources: 57, min: [3, 1, 13], max: [21, 1, 15], sealed_faces: 6, cap: 'orange_stained_glass; central polished_andesite bridge', guardrail: 'iron_bars', open_lava_surface: false },
  palette, design_changes: ['Concept open lava replaced by six-face fully sealed source channel beneath transparent full glass blocks, with iron guards and three-wide stone bridge.', 'Forge uses enclosed solid glowstone and orange glass, no live fire; empty unlit furnaces cannot emit fuel or rewards.', 'Coal handcart is stationary block-built decoration; no minecart or other entity.', 'Full roof retained; software cutaway views alone hide roof and near walls.'],
  deployment: { deployed: false, formal_files_modified: false, player_saves_opened: false, integration_performed: false, ready_for_unified_integration: false },
  preview: { source: 'Actual final room.nbt via Minecraft Builder/Lodestone software WebGL.' }
}
await fs.mkdir(path.join(root, 'qa'), { recursive: true })
await fs.writeFile(path.join(root, 'room.json'), JSON.stringify(metadata, null, 2) + '\n')
await fs.writeFile(path.join(root, 'qa/static.json'), JSON.stringify({ status: 'PASS', id: metadata.id, structure_sha256: hash, reference_sha256: referenceHash, route_cells: routeCells, checks }, null, 2) + '\n')
console.log(JSON.stringify({ status: 'PASS', checks: checks.length, structure_sha256: hash, lava_sources: lava.length, route_cells: routeCells }))
