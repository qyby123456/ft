import fs from 'node:fs/promises'
import path from 'node:path'
import crypto from 'node:crypto'
import { fileURLToPath } from 'node:url'
import { NbtFile, Structure } from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'
import { size, footprint } from './recipe.mjs'
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
  { name: 'north-leg', facing: 'north', position: [8, 5, 0], width: 5, height: 4 },
  { name: 'east-leg', facing: 'east', position: [36, 5, 26], width: 5, height: 4 },
]
const targets = [
  ...connections.map(connection => ({ id: connection.name, role: 'connection', pos: connection.position })),
  { id: 'interior-safe', role: 'safe-point', pos: [8, 5, 4] },
  ...[8, 16, 23].map(south => ({ id: `north-progress-${south}`, role: 'leg-node', pos: [8, 5, south] })),
  { id: 'turn', role: 'right-angle-bend', pos: [8, 5, 26] },
  { id: 'bridge', role: 'ice-deck', pos: [8, 5, 29] },
  { id: 'corner-lantern', role: 'landmark-approach', pos: [12, 5, 22] },
  ...[19, 27, 33].map(east => ({ id: `east-progress-${east}`, role: 'leg-node', pos: [east, 5, 26] })),
]
const boundary = blocks.filter(block => {
  const [east, height, south] = block.pos
  return height === 5 && (east === 0 || east === 36 || south === 0 || south === 34) && at(east, height, south) === 'minecraft:air' && at(east, height + 1, south) === 'minecraft:air' && at(east, 4, south) !== 'minecraft:air'
}).map(block => block.pos)
const palette = {}
for (const block of blocks) { const name = block.state.getName().toString(); palette[name] = (palette[name] || 0) + 1 }
const roof = []
for (let east = 0; east < 37; east++) for (let south = 0; south < 35; south++) if (footprint(east, south)) {
  const solidY = []
  for (let height = 15; height < 19; height++) if (at(east, height, south) !== 'minecraft:air') solidY.push(height)
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
const lineCells = (start, finish) => Array.from({ length: Math.max(Math.abs(finish[0] - start[0]), Math.abs(finish[2] - start[2])) + 1 }, (_, index) => [start[0] + Math.sign(finish[0] - start[0]) * index, 5, start[2] + Math.sign(finish[2] - start[2]) * index])
const dogleg = [...lineCells([8, 5, 0], [8, 5, 26]), ...lineCells([8, 5, 26], [36, 5, 26])]
const direct = Array.from({ length: 141 }, (_, index) => [8 + 28 * index / 140, 26 * index / 140])
const tests = {
  size: JSON.stringify(structure.getSize()) === JSON.stringify(size),
  dataversion: nbt.getNumber('DataVersion') === 3465,
  unique_complete_volume: blocks.length === 24605 && grid.size === blocks.length,
  bounded: blocks.every(block => structure.isInside(block.pos)),
  vanilla: blocks.every(block => block.state.getName().toString().startsWith('minecraft:')),
  no_entities_or_block_entities: nbt.getList('entities').length === 0 && blocks.every(block => !block.nbt),
  no_hazards_containers_fluids: blocks.every(block => !/command_block|jigsaw|structure_block|spawner|lava$|water$|powder_snow|fire$|chest$|barrel$/.test(block.state.getName().toString())),
  no_snow_layers_or_ordinary_ice: !(palette['minecraft:snow'] || palette['minecraft:ice']),
  apertures: connections.every(connection => {
    const [east, height, south] = connection.position
    for (let offset = -2; offset <= 2; offset++) {
      const axis = connection.facing === 'north'
      for (let rise = 0; rise < 4; rise++) if ((axis ? at(east + offset, height + rise, south) : at(east, height + rise, south + offset)) !== 'minecraft:air') return false
      if ((axis ? at(east + offset, 4, south) : at(east, 4, south + offset)) === 'minecraft:air') return false
    }
    return true
  }),
  exact_two_perpendicular_supported_ports: boundary.length === 10 && boundary.every(([east, , south]) => (south === 0 && east >= 6 && east <= 10) || (east === 36 && south >= 24 && south <= 28)),
  roof_all_footprint_columns: roof.every(column => column.solid_y.length > 0),
  unused_corner_all_air: blocks.every(block => footprint(block.pos[0], block.pos[2]) || block.state.getName().toString() === 'minecraft:air'),
  cutaway_exact_air_only: previewGrid.size === grid.size && blocks.every(block => previewGrid.get(block.pos.join(',')) === (remove(block.pos) ? 'minecraft:air' : block.state.toString())),
  clear_dogleg: dogleg.every(([east, height, south]) => [height, height + 1, height + 2, height + 3].every(rise => at(east, rise, south) === 'minecraft:air') && at(east, 4, south) !== 'minecraft:air'),
  true_corner_blocks_direct_chord: direct.some(([east, south]) => at(Math.floor(east), 5, Math.floor(south)) === 'minecraft:stone_bricks'),
  bridge_has_air_under_deck: [6, 7, 8, 9, 10, 11, 12].every(east => [27, 28, 29, 30].every(south => at(east, 4, south) === 'minecraft:blue_ice' && [1, 2, 3].every(height => at(east, height, south) === 'minecraft:air'))),
  corner_lantern: at(13, 11, 20) === 'minecraft:lantern' && at(14, 8, 18) === 'minecraft:stone_bricks',
  all_solids_face_connected: detached.length === 0,
}
const reference = 'D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S3-frost-tundra/out/S3-06-corner.png'
const write = async (name, data) => fs.writeFile(path.join(root, name), JSON.stringify(data, null, 2))
await write('room.json', { id: 'S3-06-REWORK-B', name: '灯角冰桥回廊', size, entrance: [8, 5, 0], interior: [8, 5, 4], facing: 'north', origin: [0, 0, 0], connections, sha256, reference, reference_sha256: hash(await fs.readFile(reference)), runtime_slot: null, deployed: false, footprint: 'x<=18 OR z>=16; unoccupied quadrant explicit air', floor_y: 4 })
await write('route-targets.json', { sha256, coordinate_system: 'Integer feet cell Y5; body center x+0.5/z+0.5, +X east/+Z south', connections, boundary_foot_cells: boundary, targets, start: 'north-leg', landmarks: { turn: [8, 5, 26], lamp: [13, 11, 20], ice_bridge: { deck: [[6, 4, 27], [12, 4, 30]], void_below_y: [1, 3], not_bottomless: true }, dogleg } })
await write('roof-columns.json', { sha256, coverage: 'L footprint only; unused quadrant is air', columns: roof })
await write('blocks.json', blocks.map(block => ({ pos: block.pos, state: block.state.toString() })))
await write('attachment-check.json', { sha256, method: 'non-air voxel face connectivity, not world physics', detached })
await write('snow-light-boundary.json', { sha256, ordinary_ice: 0, snow_layers: 0, snow_blocks: palette['minecraft:snow_block'], packed_ice: palette['minecraft:packed_ice'], blue_ice: palette['minecraft:blue_ice'], warm_lanterns: palette['minecraft:lantern'], glow_lichen: palette['minecraft:glow_lichen'], lantern_emission: 15, rationale: 'Full snow/packed ice/blue ice used around warm lamps; no melting layers or flowing water.', world_light_ticks: 'NOT_RUN' })
await write('static-qa.json', { sha256, status: Object.values(tests).every(Boolean) ? 'PASS' : 'FAIL', tests, palette, block_entities: [], preview_sha256: hash(previewBytes), world_ticks: 'NOT_RUN' })
await write('rotated-connectors.json', { sha256, pivot: 'nonnegative rotated bounds; account for engine pivot translation', rotations: [0, 90, 180, 270].map(angle => ({ angle, size: angle % 180 ? [35, 19, 37] : size, connections: connections.map(connection => {
  const [east, height, south] = connection.position
  const directions = ['north', 'east', 'south', 'west']
  return { ...connection, facing: directions[(directions.indexOf(connection.facing) + angle / 90) % 4], position: angle === 0 ? [east, height, south] : angle === 90 ? [34 - south, height, east] : angle === 180 ? [36 - east, height, 34 - south] : [south, height, 36 - east] }
}) })) })
console.log(JSON.stringify({ sha256, tests, palette, roof_columns: roof.length }, null, 2))
if (!Object.values(tests).every(Boolean)) process.exitCode = 1
