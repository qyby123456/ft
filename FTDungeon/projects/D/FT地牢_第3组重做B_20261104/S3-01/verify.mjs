import fs from 'node:fs/promises'
import path from 'node:path'
import crypto from 'node:crypto'
import { fileURLToPath } from 'node:url'
import { NbtFile, Structure } from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'

const root = path.dirname(fileURLToPath(import.meta.url))
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const nbt = NbtFile.read(bytes).root
const structure = Structure.fromNbt(nbt)
const blocks = structure.getBlocks()
const grid = new Map(blocks.map(block => [block.pos.join(','), block.state.getName().toString()]))
const at = (x, y, z) => grid.get([x, y, z].join(','))
const air = value => value === 'minecraft:air' || value === 'minecraft:snow'
const walkable = (x, z) => x >= 0 && z >= 0 && x < 35 && z < 35 && air(at(x, 2, z)) && at(x, 3, z) === 'minecraft:air' && !air(at(x, 1, z))
const queue = [[17, 0]]
const visited = new Set(['17,0'])
for (let index = 0; index < queue.length; index++) {
  const [x, z] = queue[index]
  for (const [nextX, nextZ] of [[x - 1, z], [x + 1, z], [x, z - 1], [x, z + 1]]) {
    const key = `${nextX},${nextZ}`
    if (visited.has(key) || !walkable(nextX, nextZ)) continue
    visited.add(key)
    queue.push([nextX, nextZ])
  }
}
const connections = [
  { name: 'north-adapter', facing: 'north', position: [17, 2, 0], width: 5, height: 4 },
  { name: 'west-arch', facing: 'west', position: [0, 2, 13], width: 5, height: 4 },
  { name: 'east-arch', facing: 'east', position: [34, 2, 13], width: 5, height: 4 },
]
const tests = {}
tests.lodestone_actual_nbt_load = true
tests.dimensions = JSON.stringify(structure.getSize()) === '[35,22,35]'
tests.complete_volume_unique_in_bounds = blocks.length === 35 * 22 * 35 && grid.size === blocks.length && blocks.every(({ pos }) => structure.isInside(pos))
tests.dataversion_1_20_1 = nbt.getNumber('DataVersion') === 3465
tests.no_entities = nbt.getList('entities').length === 0
tests.all_vanilla = blocks.every(block => block.state.getName().toString().startsWith('minecraft:'))
tests.no_hazards_or_runtime_blocks = blocks.every(block => !/command_block|structure_block|jigsaw|spawner|lava|water$|powder_snow|fire$|chest|barrel/.test(block.state.getName().toString()))
tests.connections = connections.map(connection => {
  const [x, y, z] = connection.position
  let aperture = true
  for (let offset = -2; offset <= 2; offset++) for (let rise = 0; rise < 4; rise++) {
    const pos = connection.facing === 'north' ? [x + offset, y + rise, z] : [x, y + rise, z + offset]
    aperture &&= at(...pos) === 'minecraft:air'
  }
  return { name: connection.name, aperture, flat_floor: [-2, -1, 0, 1, 2].every(offset => !air(connection.facing === 'north' ? at(x + offset, 1, z) : at(x, 1, z + offset))), reachable: visited.has(`${x},${z}`) }
})
tests.ring_route = [[8, 10], [26, 10], [26, 27], [10, 27]].every(([x, z]) => visited.has(`${x},${z}`))
tests.roof_coverage = true
for (let x = 0; x < 35; x++) for (let z = 0; z < 35; z++) {
  let covered = false
  for (let y = 15; y < 22; y++) covered ||= !air(at(x, y, z))
  tests.roof_coverage &&= covered
}
tests.lantern_support = blocks.filter(block => block.state.getName().toString() === 'minecraft:soul_lantern').every(({ pos: [x, y, z] }) => {
  let top = y + 1
  while (at(x, top, z) === 'minecraft:chain') top++
  return at(x, top, z) === 'minecraft:stone_bricks'
})
const palette = {}
for (const block of blocks) {
  const name = block.state.getName().toString()
  palette[name] = (palette[name] || 0) + 1
}
const sha256 = crypto.createHash('sha256').update(bytes).digest('hex')
const passed = Object.values(tests).every(value => typeof value === 'boolean' ? value : value.every(result => result.aperture && result.flat_floor && result.reachable))
let native = { status: 'NOT_RUN' }
try {
  const report = JSON.parse(await fs.readFile(path.join(root, 'native-check.json'), 'utf8'))
  if (report.sha256 === sha256) native = report
} catch {}
let survival = { status: 'NOT_RUN' }
try {
  const reports = JSON.parse(await fs.readFile(path.join(root, 'supplement/independent-native-frozen.json'), 'utf8'))
  if (reports[0].sha256 === sha256) survival = reports[0]
} catch {}
const qa = { status: passed ? 'TECHNICAL_SELF_CHECK_PASS' : 'FAIL', tests, walkable_floor_cells: visited.size, palette, sha256, native_minecraft_load: native, tagged_static_survival: survival, in_game_placement_ticks_and_playtest: 'NOT_RUN', visual_fidelity: 'PENDING_MANUAL_REVIEW', cross_group_similarity: 'PENDING_FINAL_AUDIT' }
await fs.writeFile(path.join(root, 'qa.json'), JSON.stringify(qa, null, 2))
await fs.writeFile(path.join(root, 'blocks.json'), JSON.stringify(blocks.map(block => ({ pos: block.pos, state: block.state.toString() }))))
const reference = 'D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S3-frost-tundra/out/S3-01-large-chamber.png'
await fs.writeFile(path.join(root, 'room.json'), JSON.stringify({ id: 'S3-01-REWORK-B', name: '冰晶主厅', size: structure.getSize(), entrance: [17, 2, 0], interior: [17, 2, 6], facing: 'north', origin: [0, 0, 0], connections, sha256, reference, reference_sha256: crypto.createHash('sha256').update(await fs.readFile(reference)).digest('hex'), runtime_slot: null, deployed: false }, null, 2))
const rotations = [0, 90, 180, 270].map(degrees => ({ degrees, connections: connections.map(connection => {
  const [x, y, z] = connection.position
  const position = degrees === 0 ? [x, y, z] : degrees === 90 ? [34 - z, y, x] : degrees === 180 ? [34 - x, y, 34 - z] : [z, y, 34 - x]
  const directions = ['north', 'east', 'south', 'west']
  return { ...connection, position, facing: directions[(directions.indexOf(connection.facing) + degrees / 90) % 4] }
}) }))
await fs.writeFile(path.join(root, 'rotated-connectors.json'), JSON.stringify({ pivot: 'nonnegative rotated bounding box; world origin must account for rotation translation', rotations }, null, 2))
console.log(JSON.stringify(qa, null, 2))
if (!passed) process.exitCode = 1
