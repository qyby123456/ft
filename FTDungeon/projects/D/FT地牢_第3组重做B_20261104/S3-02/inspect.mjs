import fs from 'node:fs/promises'
import path from 'node:path'
import crypto from 'node:crypto'
import { fileURLToPath } from 'node:url'
import { NbtFile, Structure } from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'
import { remove } from './cutaway.mjs'

const root = path.dirname(fileURLToPath(import.meta.url))
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const sha256 = crypto.createHash('sha256').update(bytes).digest('hex')
const nbt = NbtFile.read(bytes).root
const structure = Structure.fromNbt(nbt)
const blocks = structure.getBlocks()
const grid = new Map(blocks.map(block => [block.pos.join(','), block]))
const at = (x, y, z) => grid.get([x, y, z].join(','))?.state.getName().toString()
const connections = [
  { name: 'north-timber-portal', facing: 'north', position: [14, 2, 0], width: 5, height: 4 },
  { name: 'west-timber-portal', facing: 'west', position: [0, 2, 9], width: 5, height: 4 },
]
const targets = [
  { id: 'north', role: 'connection', pos: [14, 2, 0] },
  { id: 'west', role: 'connection', pos: [0, 2, 9] },
  { id: 'interior-safe', role: 'safe-point', pos: [14, 2, 6] },
  { id: 'reading-bench', role: 'furniture-approach', pos: [8, 2, 5] },
  { id: 'furnaces', role: 'utility-approach', pos: [20, 2, 4] },
  { id: 'barrels', role: 'empty-container-approach', pos: [5, 2, 16] },
  { id: 'wall-shelf', role: 'furniture-approach', pos: [24, 2, 6] },
  { id: 'basin-near', role: 'basin-approach-not-interaction', pos: [15, 2, 21] },
  { id: 'basin-west', role: 'bypass', pos: [9, 2, 16] },
  { id: 'basin-east', role: 'bypass', pos: [21, 2, 16] },
  { id: 'bone-view', role: 'landmark-approach', pos: [22, 2, 18] },
]
const palette = {}
for (const block of blocks) { const name = block.state.getName().toString(); palette[name] = (palette[name] || 0) + 1 }
const reference = 'D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S3-frost-tundra/out/S3-02-medium-room.png'
const metadata = { id: 'S3-02-REWORK-B', name: '冻池休憩室', size: structure.getSize(), entrance: [14, 2, 0], interior: [14, 2, 6], facing: 'north', origin: [0, 0, 0], connections, sha256, reference, reference_sha256: crypto.createHash('sha256').update(await fs.readFile(reference)).digest('hex'), runtime_slot: null, deployed: false }
const tests = {
  dimensions: JSON.stringify(structure.getSize()) === '[29,17,27]',
  dataversion: nbt.getNumber('DataVersion') === 3465,
  complete_unique_volume: blocks.length === 29 * 17 * 27 && grid.size === blocks.length,
  bounded: blocks.every(block => structure.isInside(block.pos)),
  vanilla_only: blocks.every(block => block.state.getName().toString().startsWith('minecraft:')),
  no_entities: nbt.getList('entities').length === 0,
  no_hazards: blocks.every(block => !/command_block|jigsaw|structure_block|spawner|lava$|water$|powder_snow|fire$/.test(block.state.getName().toString())),
  portal_apertures: connections.every(connection => {
    const [x, y, z] = connection.position
    for (let offset = -2; offset <= 2; offset++) for (let rise = 0; rise < 4; rise++) if ((connection.facing === 'north' ? at(x + offset, y + rise, z) : at(x, y + rise, z + offset)) !== 'minecraft:air') return false
    return true
  }),
}
const roof = []
for (let x = 0; x < 29; x++) for (let z = 0; z < 27; z++) {
  const solidY = []
  for (let y = 12; y < 17; y++) if (at(x, y, z) !== 'minecraft:air') solidY.push(y)
  roof.push({ x, z, solid_y: solidY })
}
tests.complete_roof = roof.every(column => column.solid_y.length > 0)
const previewBytes = await fs.readFile(path.join(root, 'preview-only/preview-only-cutaway.nbt'))
const preview = Structure.fromNbt(NbtFile.read(previewBytes).root)
const previewGrid = new Map(preview.getBlocks().map(block => [block.pos.join(','), block.state.toString()]))
tests.cutaway_only_deletions = blocks.every(block => previewGrid.get(block.pos.join(',')) === (remove(block.pos) ? 'minecraft:air' : block.state.toString()))
const boundary = blocks.filter(block => {
  const [x, y, z] = block.pos
  return y === 2 && (x === 0 || x === 28 || z === 0 || z === 26) && at(x, y, z) === 'minecraft:air' && at(x, y + 1, z) === 'minecraft:air'
}).map(block => block.pos)
tests.exactly_two_boundary_portals = boundary.length === 10 && boundary.every(([x, , z]) => (z === 0 && x >= 12 && x <= 16) || (x === 0 && z >= 7 && z <= 11))
const entities = blocks.filter(block => block.nbt).map(block => ({ pos: block.pos, state: block.state.toString(), nbt: block.nbt.toString() }))
tests.only_empty_declared_block_entities = entities.length === 4 && entities.every(entity => !/Items|LootTable|LootTableSeed/.test(entity.nbt))
tests.furnaces_face_interior_unlit = blocks.filter(block => block.state.getName().toString() === 'minecraft:furnace').every(block => block.state.getProperty('facing') === 'south' && block.state.getProperty('lit') === 'false')
tests.no_lit_candles = blocks.filter(block => block.state.getName().toString() === 'minecraft:candle').every(block => block.state.getProperty('lit') === 'false')
const lights = blocks.filter(block => block.state.getName().toString() === 'minecraft:soul_lantern').map(block => ({ pos: block.pos, light_level: 10 }))
const lightChecks = blocks.filter(block => block.state.getName().toString() === 'minecraft:snow').map(block => {
  const conservativeUpperBound = Math.max(0, ...lights.map(light => light.light_level - light.pos.reduce((sum, coordinate, axis) => sum + Math.abs(coordinate - block.pos[axis]), 0)))
  return { pos: block.pos, support: at(block.pos[0], block.pos[1] - 1, block.pos[2]), internal_block_light_upper_bound: conservativeUpperBound }
})
tests.internal_lights_below_snow_melt_threshold = lightChecks.every(check => check.internal_block_light_upper_bound <= 11)
tests.lantern_chain_support = lights.every(light => {
  const [x, y, z] = light.pos
  let top = y + 1
  while (at(x, top, z) === 'minecraft:chain') top++
  return at(x, top, z) === 'minecraft:stone_bricks'
})
await fs.writeFile(path.join(root, 'snow-light-boundary.json'), JSON.stringify({ sha256, method: 'Conservative open-air Manhattan attenuation upper bound from the four internal soul lanterns, not actual light propagation or random ticks; unlit candle/furnaces emit zero. No external light sources assumed.', ordinary_ice: palette['minecraft:ice'] || 0, snow_layers: lightChecks, lights, snow_random_tick_removal_if_block_light_greater_than: 11, external_sources_modded_behavior_and_world_ticks: 'NOT_RUN' }, null, 2))
const occupied = blocks.filter(block => block.state.getName().toString() !== 'minecraft:air')
const occupiedKeys = new Set(occupied.map(block => block.pos.join(',')))
const seed = [0, 0, 0]
const seen = new Set([seed.join(',')])
const queue = [seed]
for (let index = 0; index < queue.length; index++) {
  const [x, y, z] = queue[index]
  for (const next of [[x - 1, y, z], [x + 1, y, z], [x, y - 1, z], [x, y + 1, z], [x, y, z - 1], [x, y, z + 1]]) {
    const key = next.join(',')
    if (!occupiedKeys.has(key) || seen.has(key)) continue
    seen.add(key)
    queue.push(next)
  }
}
tests.all_non_air_face_connected_to_floor = seen.size === occupied.length
await fs.writeFile(path.join(root, 'attachment-check.json'), JSON.stringify({ sha256, method: 'Face adjacency of non-air voxels to [0,0,0], not physical stability simulation; native tagged canSurvive checked separately.', non_air: occupied.length, connected: seen.size, detached: occupied.filter(block => !seen.has(block.pos.join(','))).map(block => block.pos) }, null, 2))
await fs.writeFile(path.join(root, 'room.json'), JSON.stringify(metadata, null, 2))
await fs.writeFile(path.join(root, 'route-targets.json'), JSON.stringify({ sha256, coordinate_system: 'Integer block positions are player feet block cells. Entity center uses x+0.5,z+0.5, y unchanged. +X east,+Z south.', connections, boundary_foot_cells: boundary, targets, start: 'north', basin: { base_box: [[11, 2, 12], [19, 2, 20]], rim_box: [[12, 3, 13], [18, 3, 19]], frozen_surface_box: [[14, 3, 15], [16, 3, 17]], summit_or_water_route: 'not required; frozen solid decoration, no water or scripted interaction' } }, null, 2))
await fs.writeFile(path.join(root, 'roof-columns.json'), JSON.stringify({ sha256, columns: roof }, null, 2))
await fs.writeFile(path.join(root, 'blocks.json'), JSON.stringify(blocks.map(block => ({ pos: block.pos, state: block.state.toString() }))))
await fs.writeFile(path.join(root, 'static-qa.json'), JSON.stringify({ sha256, status: Object.values(tests).every(Boolean) ? 'PASS' : 'FAIL', tests, palette, block_entities: entities, preview_sha256: crypto.createHash('sha256').update(previewBytes).digest('hex'), world_ticks: 'NOT_RUN' }, null, 2))
const rotations = [0, 90, 180, 270].map(angle => ({ angle, size: angle % 180 ? [27, 17, 29] : [29, 17, 27], connections: connections.map(connection => {
  const [x, y, z] = connection.position
  const directions = ['north', 'east', 'south', 'west']
  const position = angle === 0 ? [x, y, z] : angle === 90 ? [26 - z, y, x] : angle === 180 ? [28 - x, y, 26 - z] : [z, y, 28 - x]
  return { ...connection, position, facing: directions[(directions.indexOf(connection.facing) + angle / 90) % 4] }
}) }))
await fs.writeFile(path.join(root, 'rotated-connectors.json'), JSON.stringify({ sha256, pivot: 'nonnegative rotated bounding box; apply engine pivot translation', rotations }, null, 2))
console.log(JSON.stringify({ sha256, tests, entities, palette }, null, 2))
if (!Object.values(tests).every(Boolean)) process.exitCode = 1
