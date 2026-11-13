import fs from 'node:fs/promises'
import path from 'node:path'
import { createHash } from 'node:crypto'
import { fileURLToPath, pathToFileURL } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const skill = 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
const { NbtFile, Structure } = await import(pathToFileURL(path.join(skill, 'node_modules/@mattzh72/lodestone/lib/index.js')))
const batch = 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103'
const reference = batch + '/out/mines/M01-mines-active-mine-tunnel.png'
const expectedReferenceHash = '13dfd6e737c9fcbbee1b171b94d70c21b20e605140b101878cf369aaf2eb8038'
const hash = bytes => createHash('sha256').update(bytes).digest('hex')
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const file = NbtFile.read(bytes)
const structure = Structure.fromNbt(file.root)
const blocks = structure.getBlocks()
const byPosition = new Map(blocks.map(block => [block.pos.join(','), block]))
const blockAt = position => byPosition.get(position.join(','))
const nameAt = position => blockAt(position)?.state.getName().toString()
const checks = {}
const check = (name, valid) => { if (!valid) throw new Error(name); checks[name] = true }
check('source_image_hash', hash(await fs.readFile(reference)) === expectedReferenceHash)
const sourceEvidence = []
for (const relative of ['README.md', 'docs/manifest.json', 'docs/verify.json']) {
  const contents = await fs.readFile(path.join(batch, relative))
  const record = { file: path.join(batch, relative), sha256: hash(contents) }
  if (relative.endsWith('.json')) {
    const document = JSON.parse(contents)
    const image = (document.files ?? document.records).find(entry => entry.file === 'out/mines/M01-mines-active-mine-tunnel.png')
    check(relative + '_image_record', image?.sha256 === expectedReferenceHash && image.ok && image.size.join(',') === '1536,1024')
    record.image_record = image
  }
  sourceEvidence.push(record)
}
check('gzip_nbt', bytes[0] === 31 && bytes[1] === 139)
check('size_27_11_23', structure.getSize().join(',') === '27,11,23')
check('all_6831_cells_unique_explicit', blocks.length === 6831 && byPosition.size === 6831)
check('data_version_3465', file.root.getNumber('DataVersion') === 3465)
check('zero_entities', file.root.getList('entities').length === 0)
const forbidden = new Set(['lava', 'water', 'fire', 'soul_fire', 'spawner', 'command_block', 'chain_command_block', 'repeating_command_block', 'structure_block', 'jigsaw', 'structure_void', 'tnt', 'sand', 'gravel'])
const materials = {}
const lamps = []
const containers = []
for (const block of blocks) {
  const name = block.state.getName().toString()
  check('all_positions_in_bounds', block.pos.every((value, axis) => value >= 0 && value < structure.getSize()[axis]))
  check('vanilla_safe_palette', name.startsWith('minecraft:') && !forbidden.has(name.split(':')[1]))
  materials[name] = (materials[name] ?? 0) + 1
  if (name === 'minecraft:lantern') lamps.push(block.pos)
  if (block.nbt) {
    check('only_empty_barrel_block_entities', name === 'minecraft:barrel' && block.nbt.getString('id') === 'minecraft:barrel' && block.nbt.getList('Items').length === 0 && !block.nbt.has('LootTable'))
    containers.push(block.pos)
  }
}
check('four_empty_barrels', containers.length === 4)
const route = []
for (let east = 0; east < 27; east++) for (const south of [8, 9, 11, 12]) route.push([east, 2, south])
for (let east = 11; east <= 15; east++) for (let south = 13; south <= 22; south++) route.push([east, 2, south])
for (const [east, height, south] of route) {
  check('pedestrian_route_has_solid_floor', ['minecraft:stone_bricks', 'minecraft:polished_andesite'].includes(nameAt([east, height - 1, south])))
  for (let up = 2; up <= 4; up++) check('pedestrian_route_three_clear_blocks', nameAt([east, up, south]) === 'minecraft:air')
}
for (let east = 0; east < 27; east++) {
  check('27_continuous_east_west_rails', nameAt([east, 2, 10]) === 'minecraft:rail' && blockAt([east, 2, 10]).state.getProperties().shape === 'east_west')
  check('rails_on_full_support', nameAt([east, 1, 10]) === 'minecraft:polished_andesite')
  for (let up = 3; up <= 4; up++) check('rail_clearance', nameAt([east, up, 10]) === 'minecraft:air')
  for (let south = 0; south < 23; south++) {
    check('unbroken_full_roof_621_cells', nameAt([east, 10, south]) === 'minecraft:stone')
    check('unbroken_full_ceiling_621_cells', nameAt([east, 9, south]) === 'minecraft:spruce_planks')
    check('unbroken_underfloor_621_cells', nameAt([east, 0, south]) === 'minecraft:stone')
  }
}
for (const east of [6, 20]) for (const south of [5, 16]) {
  check('four_support_posts_on_stone_pedestals', nameAt([east, 2, south]) === 'minecraft:stone_bricks')
  for (let up = 3; up <= 7; up++) check('posts_reach_connected_beams', nameAt([east, up, south]) === 'minecraft:stripped_spruce_log')
  check('posts_reach_connected_beams', nameAt([east, 8, south]) === 'minecraft:stripped_spruce_log')
}
check('six_hanging_lamps', lamps.length === 6)
for (const [east, up, south] of lamps) {
  check('lamps_chain_to_timber', up === 5 && nameAt([east, 6, south]) === 'minecraft:chain' && nameAt([east, 7, south]) === 'minecraft:chain' && nameAt([east, 8, south]) === 'minecraft:stripped_spruce_log')
}
const entrance = [13, 2, 22]
const interior = [13, 2, 18]
const metadata = {
  schema: 'ft-independent-zone-room-v1', id: 'zone-M01', batch_id: 'M01', batch_type: 'zone', name: '活跃矿道', kind: 'mine', size: structure.getSize(),
  entrance, interior, safe_point: interior, floor_y: 1, player_feet_y: 2, facing: 'south',
  ports: [{ side: 'south', feet: entrance, width: 5, clear_height: 5, purpose: 'pedestrian entrance' }, { side: 'west', feet: [0, 2, 9], width: 5, clear_height: 5, rail_center: [0, 2, 10] }, { side: 'east', feet: [26, 2, 9], width: 5, clear_height: 5, rail_center: [26, 2, 10] }],
  route: { pedestrian_lanes_z: [8, 9, 11, 12], x_range: [0, 26], south_stem_x: [11, 15], south_stem_z: [13, 22], clearance_height: 3, rail_axis: 'x', rail_z: 10, route_cells: route.length },
  roof: { intact: true, ceiling_y: 9, roof_y: 10, cells: 621 }, support_posts: [[6, 2, 5], [6, 2, 16], [20, 2, 5], [20, 2, 16]],
  reference, reference_sha256: expectedReferenceHash, source_documents: sourceEvidence, sha256: hash(bytes), data_version: 3465, minecraft: '1.20.1', forge: '47.4.10',
  materials, empty_containers: containers, entities: 0, lanterns: lamps,
  interpretation: 'Coal/iron ore walls, four grounded timber posts, crossing support beams, six chain lanterns, continuous rail with pedestrian lanes, empty barrels, workbench/anvil/grindstone and ore sorting bins. Reference minecarts are represented by stationary sorting bins off the railway; no minecart entities. Full roof retained; preview cutaway only.',
  nbt_air_policy: 'All 6831 cells explicit, including air. Full-volume occupancy and entity/fluid/world-border preflight required before any integration.',
  deployment: 'not_deployed', production_saves_modified: false, recipe: 'tools/recipe.mjs', preview: 'preview.png', qa: 'qa.json'
}
await fs.mkdir(path.join(root, 'qa'), { recursive: true })
await fs.writeFile(path.join(root, 'room.json'), JSON.stringify(metadata, null, 2) + '\n')
await fs.writeFile(path.join(root, 'qa/static.json'), JSON.stringify({ id: 'zone-M01', status: 'PASS', sha256: metadata.sha256, reference_sha256: expectedReferenceHash, checks, records: blocks.length, route_cells: route.length, materials, empty_containers: containers, source_documents: sourceEvidence }, null, 2) + '\n')
console.log(JSON.stringify({ status: 'PASS', sha256: metadata.sha256, checks: Object.keys(checks).length, records: blocks.length, pedestrian_cells: route.length }))
