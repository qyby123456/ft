import fs from 'node:fs/promises'
import path from 'node:path'
import crypto from 'node:crypto'
import { fileURLToPath } from 'node:url'
import { NbtFile, Structure } from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const referenceRoot = 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103'
const referenceFile = 'out/abandoned/A01-abandoned-ruined-study.png'
const referenceHash = 'e28fe9c5d774a0f141b616ebeb683f09bc6a8c0bc90fe74e39d88b4f1f2d9a0d'
const hash = bytes => crypto.createHash('sha256').update(bytes).digest('hex')
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const sourceHash = hash(bytes)
const file = NbtFile.read(bytes)
const structure = Structure.fromNbt(file.root)
const records = structure.getBlocks()
const blocks = new Map(records.map(block => [block.pos.join(','), block]))
const nameAt = position => blocks.get(position.join(','))?.state.getName().toString()
const counts = {}
for (const block of records) {
  const name = block.state.getName().toString()
  counts[name] = (counts[name] || 0) + 1
}
const checks = []
function check(name, condition, detail) {
  checks.push({ name, status: condition ? 'PASS' : 'FAIL', detail })
  if (!condition) throw new Error(`${name}: ${JSON.stringify(detail)}`)
}
await fs.mkdir(path.join(root, 'qa'), { recursive: true })
await fs.mkdir(path.join(root, 'evidence'), { recursive: true })
try {
  const referenceBytes = await fs.readFile(path.join(referenceRoot, referenceFile))
  check('reference_file_hash_and_png_dimensions', hash(referenceBytes) === referenceHash && referenceBytes.readUInt32BE(16) === 1536 && referenceBytes.readUInt32BE(20) === 1024, { sha256: referenceHash, bytes: referenceBytes.length, width: 1536, height: 1024 })
  for (const document of ['manifest', 'verify']) {
    const content = JSON.parse(await fs.readFile(path.join(referenceRoot, `docs/${document}.json`), 'utf8'))
    const entries = Array.isArray(content) ? content : content.files ?? content.records
    const entry = entries.find(entry => entry.file === referenceFile)
    check(`reference_${document}_agreement`, entry?.sha256 === referenceHash && entry.bytes === referenceBytes.length && entry.ok === true, entry)
  }
  check('gzip_data_version_single_palette', bytes[0] === 31 && bytes[1] === 139 && file.root.getNumber('DataVersion') === 3465 && !file.root.has('palettes'), 3465)
  check('size_and_full_unique_bounded_records', JSON.stringify(structure.getSize()) === '[17,11,17]' && records.length === 3179 && blocks.size === 3179 && records.every(block => block.pos.every((value, axis) => Number.isInteger(value) && value >= 0 && value < [17, 11, 17][axis])), { size: structure.getSize(), records: records.length })
  check('no_structure_entities', file.root.getList('entities').length === 0, 0)
  const forbidden = /command_block|structure_block|structure_void|jigsaw|spawner|lava|water$|fire$|magma_block|cactus|powder_snow|berry_bush|wither_rose|campfire|tnt/
  check('vanilla_no_active_hazards', Object.keys(counts).every(name => name.startsWith('minecraft:') && !forbidden.test(name)), counts)
  const containers = records.filter(block => block.nbt)
  check('three_explicit_empty_barrels', containers.length === 3 && containers.every(block => block.state.getName().toString() === 'minecraft:barrel' && block.nbt.getString('id') === 'minecraft:barrel' && block.nbt.getList('Items').length === 0 && !block.nbt.has('LootTable')), containers.map(block => ({ position: block.pos, items: 0, loot_table: false })))
  let floor = 0
  let roof = 0
  for (let blockX = 0; blockX < 17; blockX++) {
    for (let blockZ = 0; blockZ < 17; blockZ++) {
      if (nameAt([blockX, 0, blockZ]) !== 'minecraft:air' && nameAt([blockX, 1, blockZ]) !== 'minecraft:air') floor++
      if (nameAt([blockX, 10, blockZ]) === 'minecraft:spruce_planks') roof++
    }
  }
  check('complete_roof_and_double_floor', floor === 289 && roof === 289, { floor, roof })
  let shell = true
  for (let blockY = 2; blockY <= 9; blockY++) {
    for (let offset = 0; offset < 17; offset++) {
      for (const position of [[offset, blockY, 0], [offset, blockY, 16], [0, blockY, offset], [16, blockY, offset]]) {
        const opening = position[2] === 0 && position[0] >= 7 && position[0] <= 9 && blockY <= 5
        shell &&= (nameAt(position) === 'minecraft:air') === opening
      }
    }
  }
  check('single_north_port_closed_shell', shell, { entrance: [8, 2, 0], opening: [3, 4] })
  let lane = true
  for (let blockX = 7; blockX <= 9; blockX++) {
    for (let blockZ = 0; blockZ <= 14; blockZ++) {
      for (let blockY = 2; blockY <= 5; blockY++) lane &&= nameAt([blockX, blockY, blockZ]) === 'minecraft:air'
    }
  }
  check('three_wide_four_high_main_lane_and_safe_area', lane, { x: [7, 9], z: [0, 14], feet_y: 2, safe: [8, 2, 4] })
  const shelves = records.filter(block => block.state.getName().toString() === 'minecraft:bookshelf')
  check('all_bookshelf_stacks_grounded_or_wall_backed', shelves.length >= 40 && shelves.every(block => block.pos[0] === 1 || block.pos[2] === 15) && shelves.every(block => nameAt([block.pos[0], block.pos[1] - 1, block.pos[2]]) !== 'minecraft:air'), { bookshelf_blocks: shelves.length })
  for (const [blockX, blockZ] of [[3, 8], [12, 8]]) {
    check(`anchored_lantern_${blockX}_${blockZ}`, nameAt([blockX, 9, blockZ]) === 'minecraft:spruce_log' && nameAt([blockX, 8, blockZ]) === 'minecraft:chain' && nameAt([blockX, 7, blockZ]) === 'minecraft:chain' && nameAt([blockX, 6, blockZ]) === 'minecraft:lantern', { beam: [blockX, 9, blockZ], lantern: [blockX, 6, blockZ] })
  }
  check('study_theme_readable', counts['minecraft:bookshelf'] >= 40 && counts['minecraft:cobweb'] === 5 && counts['minecraft:lantern'] === 4 && counts['minecraft:red_wool'] === 9 && counts['minecraft:polished_blackstone_bricks'] === 9, counts)
  const metadata = {
    id: 'zone-A01', design_id: 'A01', name: '废弃书房', category: 'abandoned_room', size: [17, 11, 17], entrance: [8, 2, 0], interior: [8, 2, 4], facing: 'north', safe_points: [[8, 2, 4]],
    ports: [{ facing: 'north', feet: [8, 2, 0], opening: [3, 4], purpose: 'entry_and_return' }],
    path: { x: [7, 9], z: [0, 14], feet_y: 2, clear_height: 4 }, data_version: 3465, sha256: sourceHash,
    reference: `${referenceRoot}/${referenceFile}`, reference_sha256: referenceHash, builder: 'Minecraft Builder Skill / Lodestone 0.6.0', recipe: 'recipe.mjs',
    entities: 0, block_entities: 3, containers: containers.map(block => ({ type: 'minecraft:barrel', position: block.pos, item_count: 0, loot_table: false })),
    adaptations: ['Closed solid roof added above the concept cutaway.', 'Tilted bookcase and fallen chair use stable axis-aligned bookshelves and stairs.', 'Paper and loose books use white/brown carpet, without item entities or readable books.', 'Worn red rug is flush wool flooring to keep the main lane flat.', 'Fireplace is cold, with blackstone ash and no fire or fuel.'],
    deployed: false, production_saves_touched: false
  }
  await fs.writeFile(path.join(root, 'room.json'), JSON.stringify(metadata, null, 2) + '\n')
  await fs.writeFile(path.join(root, 'qa/static.json'), JSON.stringify({ status: 'PASS', source_sha256: sourceHash, checks, counts, non_air_blocks: records.length - counts['minecraft:air'], production_saves_touched: false }, null, 2) + '\n')
  const resources = path.join(root, 'native-test/src/main/resources')
  await fs.mkdir(path.join(resources, 'data/ftzonea01qa/structures'), { recursive: true })
  const empty = new Structure([1, 1, 1])
  empty.addBlock([0, 0, 0], 'minecraft:air')
  await fs.writeFile(path.join(resources, 'data/ftzonea01qa/structures/empty.nbt'), empty.writeNbt({ name: '', dataVersion: 3465, compression: 'gzip' }))
  await fs.writeFile(path.join(resources, 'pack.mcmeta'), JSON.stringify({ pack: { pack_format: 15, description: 'A01 isolated QA, never deploy' } }) + '\n')
  console.log(JSON.stringify({ status: 'PASS', checks: checks.length, sha256: sourceHash, records: records.length, non_air: records.length - counts['minecraft:air'] }))
} catch (error) {
  await fs.writeFile(path.join(root, 'qa/static.json'), JSON.stringify({ status: 'FAIL', source_sha256: sourceHash, checks, error: error.message }, null, 2) + '\n')
  throw error
}
