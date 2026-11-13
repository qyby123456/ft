import fs from 'node:fs/promises'
import path from 'node:path'
import crypto from 'node:crypto'
import { fileURLToPath } from 'node:url'
import { NbtFile, Structure } from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'
import { size, roofed } from './recipe.mjs'
import { remove } from './cutaway.mjs'

const root = path.dirname(fileURLToPath(import.meta.url))
const hash = bytes => crypto.createHash('sha256').update(bytes).digest('hex')
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const sha256 = hash(bytes)
const nbt = NbtFile.read(bytes).root
const structure = Structure.fromNbt(nbt)
const blocks = structure.getBlocks()
const grid = new Map(blocks.map(block => [block.pos.join(','), block]))
const at = (east, height, south) => grid.get([east, height, south].join(','))?.state.getName().toString()
const connections = [
  { name: 'south-approach', facing: 'south', position: [16, 2, 36], width: 6, width_offsets: [-2, 3], height: 4 },
  { name: 'north-dungeon', facing: 'north', position: [16, 8, 0], width: 6, width_offsets: [-2, 3], height: 4 },
]
const targets = [
  ...connections.map(connection => ({ id: connection.name, role: 'connection', pos: connection.position })),
  { id: 'front-step-low', role: 'stair', pos: [16, 3, 35] },
  { id: 'front-step-middle', role: 'stair', pos: [16, 4, 34] },
  { id: 'front-step-high', role: 'stair', pos: [16, 5, 33] },
  { id: 'open-gate', role: 'door-passage', pos: [16, 5, 29] },
  { id: 'courtyard-safe', role: 'safe-point', pos: [16, 5, 24] },
  { id: 'notice-board', role: 'decoration-approach', pos: [8, 5, 17] },
  { id: 'bench', role: 'decoration-approach', pos: [8, 5, 24] },
  { id: 'east-crates', role: 'decoration-approach', pos: [25, 5, 18] },
  { id: 'rear-step-low', role: 'stair', pos: [16, 6, 10] },
  { id: 'rear-step-high', role: 'stair', pos: [16, 8, 8] },
  { id: 'inner-ice-arch', role: 'landing', pos: [16, 8, 5] },
]
const palette = {}
for (const block of blocks) { const name = block.state.getName().toString(); palette[name] = (palette[name] || 0) + 1 }
const roof = []
for (let east = 0; east < 34; east++) for (let south = 0; south < 37; south++) if (roofed(east, south)) {
  const solidY = []
  for (let height = 18; height < 24; height++) if (at(east, height, south) !== 'minecraft:air') solidY.push(height)
  roof.push({ x: east, z: south, solid_y: solidY })
}
const previewBytes = await fs.readFile(path.join(root, 'preview-only/preview-only-cutaway.nbt'))
const previewGrid = new Map(Structure.fromNbt(NbtFile.read(previewBytes).root).getBlocks().map(block => [block.pos.join(','), block.state.toString()]))
const occupied = blocks.filter(block => block.state.getName().toString() !== 'minecraft:air')
const seen = new Set(['0,0,0'])
const queue = [[0, 0, 0]]
for (let index = 0; index < queue.length; index++) {
  const [east, height, south] = queue[index]
  for (const offset of [[1,0,0],[-1,0,0],[0,1,0],[0,-1,0],[0,0,1],[0,0,-1]]) {
    const position = [east + offset[0], height + offset[1], south + offset[2]]
    const key = position.join(',')
    if (!seen.has(key) && grid.has(key) && at(...position) !== 'minecraft:air') { seen.add(key); queue.push(position) }
  }
}
const detached = occupied.filter(block => !seen.has(block.pos.join(','))).map(block => block.pos)
const doors = blocks.filter(block => block.state.getName().toString() === 'minecraft:spruce_door')
const entities = blocks.filter(block => block.nbt).map(block => ({ pos: block.pos, state: block.state.toString(), nbt: block.nbt.toString() }))
const tests = {
  size: JSON.stringify(structure.getSize()) === JSON.stringify(size),
  dataversion: nbt.getNumber('DataVersion') === 3465,
  unique_complete_volume: blocks.length === 30192 && grid.size === blocks.length,
  bounded: blocks.every(block => structure.isInside(block.pos)),
  vanilla: blocks.every(block => block.state.getName().toString().startsWith('minecraft:')),
  no_entities: nbt.getList('entities').length === 0,
  no_commands_containers_or_fluids: blocks.every(block => !/command_block|jigsaw|structure_block|spawner|lava$|water$|powder_snow|chest$|barrel$/.test(block.state.getName().toString())),
  no_snow_layers_or_ordinary_ice: !(palette['minecraft:snow'] || palette['minecraft:ice']),
  port_clearance: connections.every(connection => {
    const [east, height, south] = connection.position
    for (let offset = -2; offset <= 3; offset++) {
      for (let rise = 0; rise < 4; rise++) if (at(east + offset, height + rise, south) !== 'minecraft:air') return false
      if (at(east + offset, height - 1, south) === 'minecraft:air') return false
    }
    return true
  }),
  rear_tunnel_roof: roof.every(column => column.solid_y.length > 0),
  exact_boundary_openings_at_anchor_heights: Array.from({ length: 34 }, (_, east) => east).every(east => (at(east, 8, 0) === 'minecraft:air') === (east >= 14 && east <= 19) && (at(east, 2, 36) === 'minecraft:air') === (east >= 14 && east <= 19)),
  courtyard_intentionally_open_sky: [14,15,16,17,18,19].every(east => [16,20,24].every(south => [18,19,20,21,22,23].every(height => at(east, height, south) === 'minecraft:air'))),
  cutaway_exact_air_only: previewGrid.size === grid.size && blocks.every(block => previewGrid.get(block.pos.join(',')) === (remove(block.pos) ? 'minecraft:air' : block.state.toString())),
  four_matched_open_door_halves: doors.length === 4 && doors.every(block => block.state.toString().includes('open=true')) && [16,17].every(east => [5,6].every(height => at(east, height, 29) === 'minecraft:spruce_door')),
  actual_stair_blocks: palette['minecraft:stone_brick_stairs'] === 42 && [33,34,35].every(south => at(16, 37 - south, south) === 'minecraft:stone_brick_stairs') && [8,9,10].every(south => at(16, 15 - south, south) === 'minecraft:stone_brick_stairs'),
  all_solids_face_connected: detached.length === 0,
  single_known_campfire_entity: entities.length === 1 && entities[0].state.startsWith('minecraft:campfire') && !/Items:\[\{|LootTable|Command/.test(entities[0].nbt),
}
const reference = 'D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S3-frost-tundra/out/S3-07-entrance.png'
const write = async (name, data) => fs.writeFile(path.join(root, name), JSON.stringify(data, null, 2))
await write('room.json', { id: 'S3-07-REWORK-B', name: '雪阶门庭入口', size, entrance: [16,2,36], interior: [16,5,24], facing: 'south', origin: [0,0,0], connections, sha256, reference, reference_sha256: hash(await fs.readFile(reference)), runtime_slot: null, deployed: false, courtyard_open_sky: true, door_state: 'two spruce doors both open; 2-block nominal throat, actual collision checked', elevations: { approach_feet: 2, courtyard_feet: 5, rear_feet: 8 }, fire: { position: [12,8,16], lit: true, contact_damage_possible: true, not_on_route: true } })
await write('route-targets.json', { sha256, coordinate_system: 'Feet Y variable, x+0.5/z+0.5 body centers, +X east/+Z south', connections, targets, start: 'south-approach', landmarks: { door_halves: doors.map(block => ({ pos: block.pos, state: block.state.toString() })), front_stairs: [[14,2,35],[19,4,33]], rear_stairs: [[13,5,10],[20,7,8]], board: [8,9,14], campfire: [12,8,16], main_axis_east: 16 } })
await write('roof-columns.json', { sha256, coverage: '144 columns over rear tunnel only; courtyard intentionally open', columns: roof })
await write('blocks.json', blocks.map(block => ({ pos: block.pos, state: block.state.toString() })))
await write('attachment-check.json', { sha256, method: 'non-air voxel face connectivity, not world physics', detached })
await write('snow-light-boundary.json', { sha256, ordinary_ice: 0, snow_layers: 0, snow_blocks: palette['minecraft:snow_block'], packed_ice: palette['minecraft:packed_ice'], blue_ice: palette['minecraft:blue_ice'], warm_lanterns: palette['minecraft:lantern'], campfires: 1, contact_damage_possible_at_campfire: true, no_live_fluid: true, world_light_ticks: 'NOT_RUN' })
await write('static-qa.json', { sha256, status: Object.values(tests).every(Boolean) ? 'PASS' : 'FAIL', tests, palette, block_entities: entities, preview_sha256: hash(previewBytes), world_ticks: 'NOT_RUN' })
await write('rotated-connectors.json', { sha256, pivot: 'nonnegative rotated bounds; account for engine pivot translation', rotations: [0,90,180,270].map(angle => ({ angle, size: angle % 180 ? [37,24,34] : size, connections: connections.map(connection => {
  const [east,height,south] = connection.position
  const directions = ['north','east','south','west']
  return { ...connection, facing: directions[(directions.indexOf(connection.facing) + angle / 90) % 4], position: angle === 0 ? [east,height,south] : angle === 90 ? [36-south,height,east] : angle === 180 ? [33-east,height,36-south] : [south,height,33-east] }
}) })) })
console.log(JSON.stringify({ sha256, tests, palette, entities }, null, 2))
if (!Object.values(tests).every(Boolean)) process.exitCode = 1
