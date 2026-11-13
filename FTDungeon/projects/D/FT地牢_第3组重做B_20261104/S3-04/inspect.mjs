import fs from 'node:fs/promises'
import path from 'node:path'
import crypto from 'node:crypto'
import { fileURLToPath } from 'node:url'
import { NbtFile, Structure } from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'
import { remove } from './cutaway.mjs'

const root = path.dirname(fileURLToPath(import.meta.url))
const hash = bytes => crypto.createHash('sha256').update(bytes).digest('hex')
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const sha256 = hash(bytes)
const nbt = NbtFile.read(bytes).root
const structure = Structure.fromNbt(nbt)
const blocks = structure.getBlocks()
const grid = new Map(blocks.map(block => [block.pos.join(','), block]))
const at = (x, y, z) => grid.get([x, y, z].join(','))?.state.getName().toString()
const connections = [
  { name: 'north-axis', facing: 'north', position: [17, 2, 0], width: 5, height: 4 },
  { name: 'south-ice-arch', facing: 'south', position: [17, 2, 56], width: 5, height: 4 },
  ...[20, 38].flatMap(z => [
    { name: `west-${z}`, facing: 'west', position: [0, 2, z], width: 5, height: 4 },
    { name: `east-${z}`, facing: 'east', position: [34, 2, z], width: 5, height: 4 },
  ]),
]
const targets = [
  ...connections.map(connection => ({ id: connection.name, role: 'connection', pos: connection.position })),
  { id: 'interior-safe', role: 'safe-point', pos: [17, 2, 5] },
  ...[10, 22, 34, 46].map(z => ({ id: `axis-${z}`, role: 'nave-axis', pos: [17, 2, z] })),
  { id: 'ice-cavern-approach', role: 'decorative-arch-not-magic-portal', pos: [17, 2, 51] },
  { id: 'bone-view', role: 'side-decoration-approach', pos: [5, 2, 19] },
  { id: 'west-aisle', role: 'longitudinal-side-aisle', pos: [5, 2, 28] },
  { id: 'east-aisle', role: 'longitudinal-side-aisle', pos: [29, 2, 28] },
]
const palette = {}
for (const block of blocks) { const name = block.state.getName().toString(); palette[name] = (palette[name] || 0) + 1 }
const boundary = blocks.filter(block => {
  const [x, y, z] = block.pos
  return y === 2 && (x === 0 || x === 34 || z === 0 || z === 56) && at(x, y, z) === 'minecraft:air' && at(x, y + 1, z) === 'minecraft:air'
}).map(block => block.pos)
const roof = []
for (let x = 0; x < 35; x++) for (let z = 0; z < 57; z++) {
  const solidY = []
  for (let y = 18; y < 27; y++) if (at(x, y, z) !== 'minecraft:air') solidY.push(y)
  roof.push({ x, z, solid_y: solidY })
}
const previewBytes = await fs.readFile(path.join(root, 'preview-only/preview-only-cutaway.nbt'))
const preview = Structure.fromNbt(NbtFile.read(previewBytes).root)
const previewGrid = new Map(preview.getBlocks().map(block => [block.pos.join(','), block.state.toString()]))
const entities = blocks.filter(block => block.nbt).map(block => ({ pos: block.pos, state: block.state.toString(), nbt: block.nbt.toString() }))
const tests = {
  size: JSON.stringify(structure.getSize()) === '[35,27,57]',
  dataversion: nbt.getNumber('DataVersion') === 3465,
  unique_complete_volume: blocks.length === 53865 && grid.size === blocks.length,
  bounded: blocks.every(block => structure.isInside(block.pos)),
  vanilla: blocks.every(block => block.state.getName().toString().startsWith('minecraft:')),
  no_entities: nbt.getList('entities').length === 0,
  no_hazards_or_containers: blocks.every(block => !/command_block|jigsaw|structure_block|spawner|lava$|water$|powder_snow|fire$|chest$|barrel$/.test(block.state.getName().toString())),
  no_snow_layers_ordinary_ice_or_fluids: !(palette['minecraft:snow'] || palette['minecraft:ice'] || palette['minecraft:water'] || palette['minecraft:lava']),
  apertures: connections.every(connection => {
    const [x, y, z] = connection.position
    for (let offset = -2; offset <= 2; offset++) {
      const axis = connection.facing === 'north' || connection.facing === 'south'
      for (let rise = 0; rise < 4; rise++) if ((axis ? at(x + offset, y + rise, z) : at(x, y + rise, z + offset)) !== 'minecraft:air') return false
      if ((axis ? at(x + offset, 1, z) : at(x, 1, z + offset)) === 'minecraft:air') return false
    }
    return true
  }),
  exact_boundary_ports: boundary.length === 30 && boundary.every(([x, , z]) => ((z === 0 || z === 56) && x >= 15 && x <= 19) || ((x === 0 || x === 34) && ([20, 38].some(center => Math.abs(z - center) <= 2)))),
  roof: roof.every(column => column.solid_y.length > 0),
  cutaway_exact_deletions: blocks.every(block => previewGrid.get(block.pos.join(',')) === (remove(block.pos) ? 'minecraft:air' : block.state.toString())),
  banner_entities_only: entities.length === 8 && entities.every(entity => entity.state.startsWith('minecraft:blue_wall_banner') && entity.nbt.includes('minecraft:banner') && !/Items|LootTable|Command/.test(entity.nbt)),
  central_axis_clear: Array.from({ length: 57 }, (_, z) => z).every(z => [15, 16, 17, 18, 19].every(x => [2, 3, 4, 5].every(y => at(x, y, z) === 'minecraft:air'))),
}
const occupied = blocks.filter(block => block.state.getName().toString() !== 'minecraft:air')
const occupiedKeys = new Set(occupied.map(block => block.pos.join(',')))
const seen = new Set(['0,0,0'])
const queue = [[0, 0, 0]]
for (let index = 0; index < queue.length; index++) {
  const [x, y, z] = queue[index]
  for (const next of [[x - 1, y, z], [x + 1, y, z], [x, y - 1, z], [x, y + 1, z], [x, y, z - 1], [x, y, z + 1]]) {
    const key = next.join(',')
    if (seen.has(key) || !occupiedKeys.has(key)) continue
    seen.add(key); queue.push(next)
  }
}
tests.non_air_face_connected = seen.size === occupied.length
const reference = 'D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S3-frost-tundra/out/S3-04-great-hall.png'
await fs.writeFile(path.join(root, 'room.json'), JSON.stringify({ id: 'S3-04-REWORK-B', name: '霜旗双列长殿', size: structure.getSize(), entrance: [17, 2, 0], interior: [17, 2, 5], facing: 'north', origin: [0, 0, 0], connections, sha256, reference, reference_sha256: hash(await fs.readFile(reference)), runtime_slot: null, deployed: false }, null, 2))
await fs.writeFile(path.join(root, 'route-targets.json'), JSON.stringify({ sha256, coordinate_system: 'Integer feet block cell; body center x+0.5/z+0.5; +X east,+Z south', connections, boundary_foot_cells: boundary, targets, start: 'north-axis', landmarks: { pillar_centers: [9, 25].flatMap(x => [10, 22, 34, 46].map(z => [x, 2, z])), axis: [[17, 2, 0], [17, 2, 56]], ice_arch_plane_z: 52, magic_portal: false, side_aisle_centers_x: [5, 29] } }, null, 2))
await fs.writeFile(path.join(root, 'roof-columns.json'), JSON.stringify({ sha256, columns: roof }, null, 2))
await fs.writeFile(path.join(root, 'blocks.json'), JSON.stringify(blocks.map(block => ({ pos: block.pos, state: block.state.toString() }))))
await fs.writeFile(path.join(root, 'attachment-check.json'), JSON.stringify({ sha256, method: 'non-air voxel face connectivity, not world physics', detached: occupied.filter(block => !seen.has(block.pos.join(','))).map(block => block.pos) }, null, 2))
await fs.writeFile(path.join(root, 'snow-light-boundary.json'), JSON.stringify({ sha256, ordinary_ice: 0, snow_layers: 0, snow_blocks: palette['minecraft:snow_block'], ice: { packed: palette['minecraft:packed_ice'], blue: palette['minecraft:blue_ice'] }, warm_lanterns: palette['minecraft:lantern'], vanilla_emission: 15, rationale: 'Warm lanterns match reference. Snow blocks/packed ice/blue ice, not snow layers/ordinary ice. No claim of world light propagation or tick simulation.', snow_layer_melt_threshold_if_added_by_player: 'block light >11', world_ticks: 'NOT_RUN' }, null, 2))
await fs.writeFile(path.join(root, 'static-qa.json'), JSON.stringify({ sha256, status: Object.values(tests).every(Boolean) ? 'PASS' : 'FAIL', tests, palette, block_entities: entities, preview_sha256: hash(previewBytes), world_ticks: 'NOT_RUN' }, null, 2))
const rotations = [0, 90, 180, 270].map(angle => ({ angle, size: angle % 180 ? [57, 27, 35] : [35, 27, 57], connections: connections.map(connection => {
  const [x, y, z] = connection.position
  const directions = ['north', 'east', 'south', 'west']
  return { ...connection, facing: directions[(directions.indexOf(connection.facing) + angle / 90) % 4], position: angle === 0 ? [x, y, z] : angle === 90 ? [56 - z, y, x] : angle === 180 ? [34 - x, y, 56 - z] : [z, y, 34 - x] }
}) }))
await fs.writeFile(path.join(root, 'rotated-connectors.json'), JSON.stringify({ sha256, pivot: 'nonnegative rotated bounds; account for engine pivot translation', rotations }, null, 2))
console.log(JSON.stringify({ sha256, tests, palette }, null, 2))
if (!Object.values(tests).every(Boolean)) process.exitCode = 1
