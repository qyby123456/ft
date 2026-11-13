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
const connections = [{ name: 'north-stone-arch', facing: 'north', position: [10, 2, 0], width: 5, height: 4 }]
const targets = [
  { id: 'north', role: 'only-connection', pos: [10, 2, 0] },
  { id: 'interior-safe', role: 'safe-point', pos: [10, 2, 5] },
  { id: 'shelf-a', role: 'bone-display-approach', pos: [5, 2, 7] },
  { id: 'shelf-b', role: 'bone-display-approach', pos: [5, 2, 12] },
  { id: 'barrel-pot', role: 'empty-barrel-approach', pos: [14, 2, 4] },
  { id: 'chest-front', role: 'empty-chest-approach', pos: [14, 2, 11] },
  { id: 'plinth-near', role: 'low-plinth-approach', pos: [11, 2, 8] },
  { id: 'bone-fragment', role: 'decorative-view', pos: [12, 2, 16] },
]
const reference = 'D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S3-frost-tundra/out/S3-03-small-room.png'
const palette = {}
for (const block of blocks) { const name = block.state.getName().toString(); palette[name] = (palette[name] || 0) + 1 }
const boundary = blocks.filter(block => {
  const [x, y, z] = block.pos
  return y === 2 && (x === 0 || x === 20 || z === 0 || z === 18) && at(x, y, z) === 'minecraft:air' && at(x, y + 1, z) === 'minecraft:air'
}).map(block => block.pos)
const roof = []
for (let x = 0; x < 21; x++) for (let z = 0; z < 19; z++) {
  const solidY = []
  for (let y = 11; y < 15; y++) if (at(x, y, z) !== 'minecraft:air') solidY.push(y)
  roof.push({ x, z, solid_y: solidY })
}
const previewBytes = await fs.readFile(path.join(root, 'preview-only/preview-only-cutaway.nbt'))
const preview = Structure.fromNbt(NbtFile.read(previewBytes).root)
const previewGrid = new Map(preview.getBlocks().map(block => [block.pos.join(','), block.state.toString()]))
const entities = blocks.filter(block => block.nbt).map(block => ({ pos: block.pos, state: block.state.toString(), nbt: block.nbt.toString() }))
const lights = blocks.filter(block => block.state.getName().toString() === 'minecraft:soul_lantern').map(block => ({ pos: block.pos, light_level: 10 }))
const tests = {
  size: JSON.stringify(structure.getSize()) === '[21,15,19]',
  dataversion: nbt.getNumber('DataVersion') === 3465,
  unique_complete_volume: blocks.length === 5985 && grid.size === blocks.length,
  bounded: blocks.every(block => structure.isInside(block.pos)),
  vanilla: blocks.every(block => block.state.getName().toString().startsWith('minecraft:')),
  no_entities: nbt.getList('entities').length === 0,
  no_hazards: blocks.every(block => !/command_block|jigsaw|structure_block|spawner|lava$|water$|powder_snow|fire$/.test(block.state.getName().toString())),
  single_boundary_port: boundary.length === 5 && boundary.every(([x, , z]) => x >= 8 && x <= 12 && z === 0),
  aperture: [8, 9, 10, 11, 12].every(x => [2, 3, 4, 5].every(y => at(x, y, 0) === 'minecraft:air') && ['minecraft:stone_bricks', 'minecraft:cracked_stone_bricks', 'minecraft:cobblestone'].includes(at(x, 1, 0))),
  roof: roof.every(column => column.solid_y.length > 0),
  cutaway_exact_deletions: blocks.every(block => previewGrid.get(block.pos.join(',')) === (remove(block.pos) ? 'minecraft:air' : block.state.toString())),
  empty_block_entities: entities.length === 2 && entities.every(entity => !/Items|LootTable|LootTableSeed/.test(entity.nbt)),
  chest_headroom: at(11, 4, 11) === 'minecraft:air' && at(11, 5, 11) === 'minecraft:air',
  chest_orientation: grid.get('11,3,11').state.getProperty('facing') === 'east',
  lights_supported: lights.every(({ pos: [x, y, z] }) => { let top = y + 1; while (at(x, top, z) === 'minecraft:chain') top++; return at(x, top, z) === 'minecraft:stone_bricks' }),
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
const lightChecks = blocks.filter(block => block.state.getName().toString() === 'minecraft:snow').map(block => ({ pos: block.pos, support: at(block.pos[0], block.pos[1] - 1, block.pos[2]), internal_block_light_upper_bound: Math.max(0, ...lights.map(light => light.light_level - light.pos.reduce((sum, coordinate, axis) => sum + Math.abs(coordinate - block.pos[axis]), 0))) }))
tests.internal_lights_below_snow_melt_threshold = lightChecks.every(check => check.internal_block_light_upper_bound <= 11)
const metadata = { id: 'S3-03-REWORK-B', name: '霜封骨藏小室', size: structure.getSize(), entrance: [10, 2, 0], interior: [10, 2, 5], facing: 'north', origin: [0, 0, 0], connections, sha256, reference, reference_sha256: hash(await fs.readFile(reference)), runtime_slot: null, deployed: false }
await fs.writeFile(path.join(root, 'room.json'), JSON.stringify(metadata, null, 2))
await fs.writeFile(path.join(root, 'route-targets.json'), JSON.stringify({ sha256, coordinate_system: 'Integer feet block cell; body center x+0.5/z+0.5; +X east,+Z south', connections, boundary_foot_cells: boundary, targets, start: 'north', chest: { pos: [11, 3, 11], facing: 'east', approach: [14, 2, 11], reward: 'none-empty', plinth: [[9, 2, 9], [13, 2, 13]], plinth_surface_y: 3 }, shelf_boxes: [[[2, 4, 5], [3, 8, 8]], [[2, 4, 10], [3, 8, 13]]] }, null, 2))
await fs.writeFile(path.join(root, 'roof-columns.json'), JSON.stringify({ sha256, columns: roof }, null, 2))
await fs.writeFile(path.join(root, 'blocks.json'), JSON.stringify(blocks.map(block => ({ pos: block.pos, state: block.state.toString() }))))
await fs.writeFile(path.join(root, 'attachment-check.json'), JSON.stringify({ sha256, method: 'non-air voxel face connectivity, not world physics', detached: occupied.filter(block => !seen.has(block.pos.join(','))).map(block => block.pos) }, null, 2))
await fs.writeFile(path.join(root, 'snow-light-boundary.json'), JSON.stringify({ sha256, method: 'open-air Manhattan attenuation conservative upper bound, not actual world light propagation; no external sources assumed', lights, snow_layers: lightChecks, ordinary_ice: palette['minecraft:ice'] || 0, snow_melts_if_block_light_greater_than: 11, world_ticks: 'NOT_RUN' }, null, 2))
await fs.writeFile(path.join(root, 'static-qa.json'), JSON.stringify({ sha256, status: Object.values(tests).every(Boolean) ? 'PASS' : 'FAIL', tests, palette, block_entities: entities, preview_sha256: hash(previewBytes), world_ticks: 'NOT_RUN' }, null, 2))
const rotations = [0, 90, 180, 270].map(angle => ({ angle, size: angle % 180 ? [19, 15, 21] : [21, 15, 19], connections: connections.map(connection => {
  const [x, y, z] = connection.position
  return { ...connection, facing: ['north', 'east', 'south', 'west'][angle / 90], position: angle === 0 ? [x, y, z] : angle === 90 ? [18 - z, y, x] : angle === 180 ? [20 - x, y, 18 - z] : [z, y, 20 - x] }
}) }))
await fs.writeFile(path.join(root, 'rotated-connectors.json'), JSON.stringify({ sha256, pivot: 'nonnegative rotated bounds; account for engine pivot translation', rotations }, null, 2))
console.log(JSON.stringify({ sha256, tests, palette }, null, 2))
if (!Object.values(tests).every(Boolean)) process.exitCode = 1
