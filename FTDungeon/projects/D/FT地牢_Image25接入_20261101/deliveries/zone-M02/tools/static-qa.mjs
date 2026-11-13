import fs from 'node:fs/promises'
import path from 'node:path'
import crypto from 'node:crypto'
import { fileURLToPath } from 'node:url'
import { NbtFile } from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'
import { specification } from './build.mjs'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const hash = bytes => crypto.createHash('sha256').update(bytes).digest('hex')
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const data = NbtFile.read(bytes).root.toSimplifiedJson()
const entries = new Map(data.blocks.map(block => [block.pos.join(','), data.palette[block.state]]))
const state = (posX, posY, posZ) => entries.get([posX, posY, posZ].join(','))
const name = (posX, posY, posZ) => state(posX, posY, posZ)?.Name
const fluid = block => block && (block.Name === 'minecraft:water' || block.Properties?.waterlogged === 'true')
const full = block => block && !/:(air|water|rail|spruce_fence|chain|lantern|pointed_dripstone|polished_andesite_stairs)$/.test(block.Name)
const clear = block => block && !fluid(block) && /:(air|rail)$/.test(block.Name)
const tests = []
const check = (test, passed, detail = {}) => { tests.push({ test, passed, ...detail }); if (!passed) throw new Error('Static QA failed: ' + test) }
const counts = {}
for (const block of data.blocks) { const identity = data.palette[block.state].Name; counts[identity] = (counts[identity] ?? 0) + 1 }
check('reference_and_batch', hash(await fs.readFile(specification.reference)) === specification.reference_sha256)
check('independent_zone_id', specification.id === 'zone-M02' && specification.distinct_from === 'corridor-M02')
check('java_1_20_1_single_palette', data.DataVersion === 3465 && !data.palettes && data.size.join(',') === specification.size.join(','))
check('full_explicit_volume_unique_coordinates', data.blocks.length === 9375 && entries.size === 9375)
check('bounds_and_indexes', data.blocks.every(block => block.pos.length === 3 && block.pos.every((value, axis) => Number.isInteger(value) && value >= 0 && value < data.size[axis]) && block.state >= 0 && block.state < data.palette.length))
check('no_entities_or_inventories', data.entities.length === 0 && data.blocks.every(block => !block.nbt))
check('vanilla_without_forbidden_blocks', data.palette.every(block => block.Name.startsWith('minecraft:') && !/(command_block|structure_block|structure_void|jigsaw|spawner|lava|fire|campfire|magma_block|cobweb|powder_snow|barrier)$/.test(block.Name)))
check('full_foundation_and_double_roof', Array.from({ length: 25 }, (_, posX) => Array.from({ length: 25 }, (_, posZ) => [0, 13, 14].every(posY => full(state(posX, posY, posZ)))).every(Boolean)).every(Boolean))
let dryCells = 0
let dryPass = true
for (const [left, near, right, far] of specification.dry_rectangles) {
  for (let posX = left; posX <= right; posX++) {
    for (let posZ = near; posZ <= far; posZ++) {
      if (!full(state(posX, 3, posZ)) || ![4, 5, 6].every(posY => clear(state(posX, posY, posZ)))) { dryPass = false; console.error('Unsafe dry cell', posX, posZ) }
      dryCells++
    }
  }
}
check('dry_3wide_route_crosswalk_and_safe_platform', dryPass, { cells_checked: dryCells })
const waters = data.blocks.filter(block => fluid(data.palette[block.state]))
check('sealed_two_layer_pool', waters.length > 800 && waters.every(block => {
  const [posX, posY, posZ] = block.pos
  return [1, 2].includes(posY) && (fluid(state(posX, posY - 1, posZ)) || full(state(posX, posY - 1, posZ))) && [[1, 0], [-1, 0], [0, 1], [0, -1]].every(([deltaX, deltaZ]) => fluid(state(posX + deltaX, posY, posZ + deltaZ)) || full(state(posX + deltaX, posY, posZ + deltaZ)))
}), { fluid_cells: waters.length })
check('four_headframe_pillars_grounded_to_roof', specification.pillars.every(([posX, posZ]) => [posX, posX + 1].every(columnX => [posZ, posZ + 1].every(columnZ => Array.from({ length: 15 }, (_, posY) => full(state(columnX, posY, columnZ))).every(Boolean)))))
check('lantern_suspensions_anchored', specification.suspension.every(([posX, posZ]) => name(posX, 8, posZ) === 'minecraft:lantern' && [9, 10, 11].every(posY => name(posX, posY, posZ) === 'minecraft:chain') && [12, 13, 14].every(posY => full(state(posX, posY, posZ)))))
check('rails_supported', data.blocks.filter(block => data.palette[block.state].Name === 'minecraft:rail').every(block => full(state(block.pos[0], block.pos[1] - 1, block.pos[2]))))
let unguardedEdges = 0
for (const block of data.blocks) {
  const [posX, posY, posZ] = block.pos
  if (posY !== 3 || block.state < 0 || data.palette[block.state].Name !== 'minecraft:spruce_planks' || !clear(state(posX, 4, posZ))) continue
  for (const [deltaX, deltaZ] of [[1, 0], [-1, 0], [0, 1], [0, -1]]) {
    if (fluid(state(posX + deltaX, 2, posZ + deltaZ)) && !full(state(posX + deltaX, 3, posZ + deltaZ))) unguardedEdges++
  }
}
check('all_walkable_deck_edges_guarded', unguardedEdges === 0, { unguarded_edges: unguardedEdges })
check('dripstone_ceiling_support', data.blocks.filter(block => data.palette[block.state].Name === 'minecraft:pointed_dripstone').every(block => name(block.pos[0], block.pos[1] + 1, block.pos[2]) === 'minecraft:dripstone_block'))
check('mine_theme_preserved', counts['minecraft:spruce_log'] > 250 && counts['minecraft:rail'] === 33 && counts['minecraft:lantern'] === 4 && counts['minecraft:iron_block'] === 9)
const metadata = { ...specification, sha256: hash(bytes), data_version: 3465, block_count: data.blocks.length, palette_count: data.palette.length, fluid_cells: waters.length, builder: 'Minecraft Builder skill / Lodestone 0.6.0', deployment: 'not_deployed', entity_count: 0, inventory_count: 0 }
await fs.mkdir(path.join(root, 'qa'), { recursive: true })
await fs.writeFile(path.join(root, 'room.json'), JSON.stringify(metadata, null, 2) + '\n')
await fs.writeFile(path.join(root, 'qa/static.json'), JSON.stringify({ status: 'PASS', structure_sha256: metadata.sha256, reference_sha256: specification.reference_sha256, tests, counts }, null, 2) + '\n')
console.log(JSON.stringify({ status: 'PASS', tests: tests.length, sha256: metadata.sha256, fluid_cells: waters.length, counts }, null, 2))
