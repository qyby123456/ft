import fs from 'node:fs/promises'
import path from 'node:path'
import crypto from 'node:crypto'
import { fileURLToPath } from 'node:url'
import { NbtFile, Structure } from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'
import { remove } from './cutaway.mjs'
import { size, bays } from './recipe.mjs'

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
  { name: 'north-passage', facing: 'north', position: [10, 2, 0], width: 5, height: 4 },
  { name: 'south-ice-passage', facing: 'south', position: [10, 2, 48], width: 5, height: 4 },
]
const boundary = blocks.filter(block => {
  const [east, height, south] = block.pos
  return height === 2 && (east === 0 || east === 20 || south === 0 || south === 48) && at(east, height, south) === 'minecraft:air' && at(east, height + 1, south) === 'minecraft:air'
}).map(block => block.pos)
const targets = [
  ...connections.map(connection => ({ id: connection.name, role: 'connection', pos: connection.position })),
  { id: 'interior-safe', role: 'safe-point', pos: [10, 2, 4] },
  ...bays.map(south => ({ id: `bay-${south}`, role: 'axis-node', pos: [10, 2, south] })),
  { id: 'lichen-west', role: 'side-observation', pos: [6, 2, 17] },
  { id: 'lichen-east', role: 'side-observation', pos: [14, 2, 25] },
  { id: 'bone-west', role: 'decoration-approach', pos: [6, 2, 3] },
  { id: 'bone-east', role: 'decoration-approach', pos: [14, 2, 43] },
  { id: 'ice-exit-approach', role: 'exit-approach', pos: [10, 2, 46] },
]
const palette = {}
for (const block of blocks) { const name = block.state.getName().toString(); palette[name] = (palette[name] || 0) + 1 }
const roof = []
for (let east = 0; east < 21; east++) for (let south = 0; south < 49; south++) {
  const solidY = []
  for (let height = 10; height < 16; height++) if (at(east, height, south) !== 'minecraft:air') solidY.push(height)
  roof.push({ x: east, z: south, solid_y: solidY })
}
const previewBytes = await fs.readFile(path.join(root, 'preview-only/preview-only-cutaway.nbt'))
const preview = Structure.fromNbt(NbtFile.read(previewBytes).root)
const previewGrid = new Map(preview.getBlocks().map(block => [block.pos.join(','), block.state.toString()]))
const occupied = blocks.filter(block => block.state.getName().toString() !== 'minecraft:air')
const seen = new Set(['0,0,0'])
const queue = [[0, 0, 0]]
for (let index = 0; index < queue.length; index++) {
  const [east, height, south] = queue[index]
  for (const offset of [[1, 0, 0], [-1, 0, 0], [0, 1, 0], [0, -1, 0], [0, 0, 1], [0, 0, -1]]) {
    const pos = [east + offset[0], height + offset[1], south + offset[2]]
    const key = pos.join(',')
    if (!seen.has(key) && grid.has(key) && at(...pos) !== 'minecraft:air') { seen.add(key); queue.push(pos) }
  }
}
const detached = occupied.filter(block => !seen.has(block.pos.join(','))).map(block => block.pos)
const tests = {
  size: JSON.stringify(structure.getSize()) === JSON.stringify(size),
  dataversion: nbt.getNumber('DataVersion') === 3465,
  unique_complete_volume: blocks.length === 16464 && grid.size === blocks.length,
  bounded: blocks.every(block => structure.isInside(block.pos)),
  vanilla: blocks.every(block => block.state.getName().toString().startsWith('minecraft:')),
  no_entities_or_block_entities: nbt.getList('entities').length === 0 && blocks.every(block => !block.nbt),
  no_hazards_containers_fluids: blocks.every(block => !/command_block|jigsaw|structure_block|spawner|lava$|water$|powder_snow|fire$|chest$|barrel$/.test(block.state.getName().toString())),
  no_melting_snow_layers_or_ordinary_ice: !(palette['minecraft:snow'] || palette['minecraft:ice']),
  apertures: connections.every(connection => {
    const [east, height, south] = connection.position
    for (let offset = -2; offset <= 2; offset++) {
      for (let rise = 0; rise < 4; rise++) if (at(east + offset, height + rise, south) !== 'minecraft:air') return false
      if (at(east + offset, 1, south) !== 'minecraft:stone_bricks') return false
    }
    return true
  }),
  exact_two_boundary_ports: boundary.length === 10 && boundary.every(([east, , south]) => (south === 0 || south === 48) && east >= 8 && east <= 12),
  roof_all_columns: roof.every(column => column.solid_y.length > 0),
  cutaway_exact_air_only: previewGrid.size === grid.size && blocks.every(block => previewGrid.get(block.pos.join(',')) === (remove(block.pos) ? 'minecraft:air' : block.state.toString())),
  clear_five_wide_axis: Array.from({ length: 49 }, (_, south) => south).every(south => [8, 9, 10, 11, 12].every(east => [2, 3, 4, 5].every(height => at(east, height, south) === 'minecraft:air'))),
  all_solids_face_connected: detached.length === 0,
  twelve_wall_timber_supports: [3, 17].every(east => bays.every(south => [6, 7, 8, 9].every(height => at(east, height, south) === 'minecraft:spruce_log'))),
  no_free_standing_centerpiece: [8, 9, 10, 11, 12].every(east => Array.from({ length: 45 }, (_, index) => index + 2).every(south => at(east, 2, south) === 'minecraft:air')),
}
const reference = 'D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S3-frost-tundra/out/S3-05-corridor.png'
const write = async (name, data) => fs.writeFile(path.join(root, name), JSON.stringify(data, null, 2))
await write('room.json', { id: 'S3-05-REWORK-B', name: '冰苔木撑直廊', size, entrance: [10, 2, 0], interior: [10, 2, 4], facing: 'north', origin: [0, 0, 0], connections, sha256, reference, reference_sha256: hash(await fs.readFile(reference)), runtime_slot: null, deployed: false })
await write('route-targets.json', { sha256, coordinate_system: 'Integer feet cell; body center x+0.5/z+0.5, +X east/+Z south', connections, boundary_foot_cells: boundary, targets, start: 'north-passage', landmarks: { bays, timber_posts_x: [3, 17], axis: [[10, 2, 0], [10, 2, 48]], magic_portal: false } })
await write('roof-columns.json', { sha256, columns: roof })
await write('blocks.json', blocks.map(block => ({ pos: block.pos, state: block.state.toString() })))
await write('attachment-check.json', { sha256, method: 'non-air voxel face connectivity, not world physics', detached })
await write('snow-light-boundary.json', { sha256, ordinary_ice: 0, snow_layers: 0, snow_blocks: palette['minecraft:snow_block'], packed_ice: palette['minecraft:packed_ice'], blue_ice: palette['minecraft:blue_ice'], warm_lanterns: palette['minecraft:lantern'], glow_lichen: palette['minecraft:glow_lichen'], lantern_emission: 15, rationale: 'Warm lights and cold vegetation match reference; nonmelting full snow/packed and blue ice only. Lichen replaces teal vines without unsupported hanging plants.', world_light_ticks: 'NOT_RUN', player_added_snow_layer_melt_threshold: 'block light >11' })
await write('static-qa.json', { sha256, status: Object.values(tests).every(Boolean) ? 'PASS' : 'FAIL', tests, palette, block_entities: [], preview_sha256: hash(previewBytes), world_ticks: 'NOT_RUN' })
await write('rotated-connectors.json', { sha256, pivot: 'nonnegative rotated bounds; engine pivot translation must be accounted for', rotations: [0, 90, 180, 270].map(angle => ({ angle, size: angle % 180 ? [49, 16, 21] : size, connections: connections.map(connection => {
  const [east, height, south] = connection.position
  const directions = ['north', 'east', 'south', 'west']
  return { ...connection, facing: directions[(directions.indexOf(connection.facing) + angle / 90) % 4], position: angle === 0 ? [east, height, south] : angle === 90 ? [48 - south, height, east] : angle === 180 ? [20 - east, height, 48 - south] : [south, height, 20 - east] }
}) })) })
console.log(JSON.stringify({ sha256, tests, palette }, null, 2))
if (!Object.values(tests).every(Boolean)) process.exitCode = 1
