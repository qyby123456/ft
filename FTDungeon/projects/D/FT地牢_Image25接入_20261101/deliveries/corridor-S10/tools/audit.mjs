import fs from 'node:fs/promises'
import path from 'node:path'
import {createHash} from 'node:crypto'
import {createRequire} from 'node:module'
import {fileURLToPath, pathToFileURL} from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const skill = 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
const require = createRequire(path.join(skill, 'package.json'))
const {NbtFile, Structure} = await import(pathToFileURL(require.resolve('@mattzh72/lodestone')))
const reference = 'D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/S10-copper-machinery-damaged.png'
const referenceHash = 'c6a7c3600395f94a6af3160b9fed188cc08906428831ecfb684844e5e6db6907'
const hash = bytes => createHash('sha256').update(bytes).digest('hex')
const requireCheck = (value, message) => { if (!value) throw new Error(message) }
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const encoded = NbtFile.read(bytes)
const data = encoded.root.toSimplifiedJson()
const structure = Structure.fromNbt(encoded.root)
const blocks = structure.getBlocks()
const size = structure.getSize()
const full = block => block && !['minecraft:air', 'minecraft:iron_trapdoor'].includes(block.state.getName().toString()) && !block.state.getName().toString().match(/stairs|slab|rod|button/)
const at = (posX, posY, posZ) => structure.getBlock([posX, posY, posZ])
const air = (posX, posY, posZ) => at(posX, posY, posZ)?.state.is('air') === true
const materials = {}
const coordinates = new Set()
const banned = /(?:command_block|structure_block|structure_void|jigsaw|spawner|lava|water|fire|campfire|magma_block|gravel|sand|chest|barrel|shulker_box|powder_snow|cobweb|cactus|wither_rose)$/
requireCheck(encoded.compression === 'gzip' && bytes[0] === 31 && bytes[1] === 139, 'Not gzip')
requireCheck(data.DataVersion === 3465 && data.palette && !data.palettes, 'Not Java 1.20.1 single-palette NBT')
requireCheck(JSON.stringify(size) === '[13,11,16]' && blocks.length === 2288, 'Wrong dimensions or incomplete explicit volume')
requireCheck(data.entities.length === 0, 'Template entities')
requireCheck(hash(await fs.readFile(reference)) === referenceHash, 'Reference fingerprint differs')
for (const block of blocks) {
  const key = block.pos.join(',')
  const identity = block.state.getName().toString()
  requireCheck(!coordinates.has(key), `Duplicate ${key}`)
  coordinates.add(key)
  requireCheck(block.pos.every((value, axis) => Number.isInteger(value) && value >= 0 && value < size[axis]), `Bounds ${key}`)
  requireCheck(identity.startsWith('minecraft:') && !banned.test(identity), `Forbidden ${identity}`)
  requireCheck(!block.nbt, `Unexpected block entity ${key}`)
  requireCheck(!identity.includes('copper') || identity.startsWith('minecraft:waxed_'), `Unwaxed copper ${key}`)
  materials[identity] = (materials[identity] ?? 0) + 1
}
let aisleCells = 0
for (let posZ = 0; posZ < 16; posZ += 1) {
  for (let posX = 5; posX <= 7; posX += 1) {
    requireCheck(full(at(posX, 1, posZ)), `Missing full floor ${posX},1,${posZ}`)
    for (let posY = 2; posY <= 4; posY += 1) {
      requireCheck(air(posX, posY, posZ), `Blocked aisle ${posX},${posY},${posZ}`)
      aisleCells += 1
    }
  }
}
for (let posX = 0; posX < 13; posX += 1) {
  for (let posZ = 0; posZ < 16; posZ += 1) {
    requireCheck(full(at(posX, 0, posZ)) && full(at(posX, 10, posZ)), 'Roof or foundation hole')
  }
}
const lights = [2, 7, 12].map(posZ => ({position: [6, 8, posZ], support: [6, 9, posZ], roof: [6, 10, posZ]}))
for (const light of lights) requireCheck(at(...light.position).state.is('end_rod') && full(at(...light.support)) && full(at(...light.roof)), 'Unattached light')
const metadata = {
  id: 'corridor-S10', producer_metadata_id: 'S10', name: '轻损锈铜机械短廊',
  size, entrance: [6, 2, 0], interior: [6, 2, 3], exit: [6, 2, 15], facing: 'north',
  connectors: [{side: 'north', position: [6, 2, 0], width: 3, height: 3}, {side: 'south', position: [6, 2, 15], width: 3, height: 3}],
  main_path: {from: [5, 2, 0], to: [7, 4, 15], floor_y: 1, width: 3, clearance: 3, turns: 0, branches: 0, stairs: 0},
  category: 'short_straight_corridor', axis: 'z', length_blocks: 16, minecraft_version: '1.20.1', data_version: 3465,
  sha256: hash(bytes), reference, reference_sha256: referenceHash,
  source_batch: 'D:/ft工程文件/image2.5/dungeon-corridors-20261103',
  template_candidate: 'ftdungeonmobs:image25/corridor_s10', reserved_queue_slot: 70,
  builder: {skill, recipe: 'tools/recipe.mjs', runner: 'scripts/run-build.mjs', lodestone: '0.6.0', explicit_air: true},
  adaptations: [
    'Java 1.20.1 has no copper grates: closed upper-half iron trapdoors over oxidized copper backing reproduce the grate rhythm outside the central lane.',
    'Lightning rods represent copper pipes; gold blocks, targets and wall-mounted stone buttons represent passive gauges without entities, loot or mod blocks.',
    'The reference far-end closed door is replaced by an open matching south arch for mandatory two-ended transit.',
    'All copper is waxed to preserve the orange/teal design under random ticks; damage uses supported missing corbels and non-gravity stone slabs, not a broken weather shell.'
  ],
  scope: {deployed: false, production_save_access: false, core_integration_tested: false, dedicated_qa_world_only: true}
}
const report = {
  status: 'PASS', room_sha256: metadata.sha256, reference_sha256: referenceHash,
  gzip: true, data_version: 3465, single_palette: true, size, record_count: blocks.length,
  explicit_air_records: materials['minecraft:air'], palette_count: data.palette.length, unique_coordinates: coordinates.size,
  bounds: true, vanilla_namespace_only: true, no_block_entities: true, no_entities: true,
  no_fluids_or_hazards: true, no_loot_or_containers: true, no_execution_blocks: true,
  sealed_roof_cells: 208, complete_foundation_cells: 208, main_path_air_cells: aisleCells,
  north_south_straight_transit: true, full_floor_cells: 48, mounted_lights: lights,
  waxed_copper_only: true, native_registry_validation: 'qa/native.json; static namespace checks alone are not registry validation',
  limitations: ['Static checks do not substitute for Forge survival, collision and tick stability tests.']
}
await fs.mkdir(path.join(root, 'qa'), {recursive: true})
for (const [filename, value] of [['room.json', metadata], ['qa/static.json', report], ['qa/materials.json', materials]]) {
  await fs.writeFile(path.join(root, filename), JSON.stringify(value, null, 2) + '\n')
}
const fixtureDir = path.join(root, 'src/main/resources/data/ftcorridors10qa/structures')
await fs.mkdir(fixtureDir, {recursive: true})
await fs.copyFile('D:/ft工程文件/FT地牢_楼梯C_破损楼梯间_20261102/src/main/resources/data/ftstaircqa/structures/empty.nbt', path.join(fixtureDir, 'empty.nbt'))
console.log(JSON.stringify(report, null, 2))
