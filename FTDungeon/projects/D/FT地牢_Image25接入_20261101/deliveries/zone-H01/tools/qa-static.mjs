import fs from 'node:fs/promises'
import path from 'node:path'
import { createHash } from 'node:crypto'
import { fileURLToPath, pathToFileURL } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const skill = 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
const { NbtFile, Structure } = await import(pathToFileURL(path.join(skill, 'node_modules/@mattzh72/lodestone/lib/index.js')))
const batch = 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103'
const reference = batch + '/out/halls/H01-halls-throne-hall-dais.png'
const referenceSha256 = '5b1cd83ecf19000c21254810b1e571d63ed0fde16c12a0fd09601a925c01f492'
const digest = bytes => createHash('sha256').update(bytes).digest('hex')
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const encoded = NbtFile.read(bytes)
const structure = Structure.fromNbt(encoded.root)
const size = structure.getSize()
const records = structure.getBlocks()
const byPosition = new Map(records.map(record => [record.pos.join(','), record]))
const blockAt = position => byPosition.get(position.join(','))
const nameAt = position => blockAt(position)?.state.getName().toString()
const air = position => nameAt(position) === 'minecraft:air'
const solid = position => !['minecraft:air', 'minecraft:chain', 'minecraft:lantern', 'minecraft:campfire', 'minecraft:red_wall_banner', 'minecraft:cobweb', 'minecraft:iron_bars'].includes(nameAt(position)) && !!nameAt(position)
const checks = {}
const check = (label, condition) => { if (!condition) throw new Error(label); checks[label] = true }
check('source_reference_sha256_matches', digest(await fs.readFile(reference)) === referenceSha256)
for (const document of ['manifest.json', 'verify.json']) {
  const source = JSON.parse(await fs.readFile(path.join(batch, 'docs', document), 'utf8'))
  const entry = (source.files ?? source.records ?? []).find(item => item.file === 'out/halls/H01-halls-throne-hall-dais.png')
  check('reference_listed_in_' + document, entry?.sha256 === referenceSha256 && entry.ok === true)
}
check('gzip_single_palette_java_1201', bytes[0] === 31 && bytes[1] === 139 && encoded.root.getNumber('DataVersion') === 3465 && !encoded.root.has('palettes'))
check('dimensions_29x18x33', JSON.stringify(size) === '[29,18,33]')
check('complete_explicit_volume_unique_positions', records.length === 17226 && byPosition.size === 17226)
check('zero_structure_entities', encoded.root.getList('entities').length === 0)
const forbidden = new Set(['spawner', 'command_block', 'chain_command_block', 'repeating_command_block', 'structure_block', 'structure_void', 'jigsaw', 'tnt', 'respawn_anchor'])
const materials = {}
const lamps = []
const braziers = []
const blockEntities = []
for (const record of records) {
  const name = record.state.getName().toString()
  check('all_positions_integer_and_in_bounds', record.pos.every((value, axis) => Number.isInteger(value) && value >= 0 && value < size[axis]))
  check('vanilla_namespace_and_no_executable_blocks', name.startsWith('minecraft:') && !forbidden.has(name.split(':')[1]))
  check('no_liquids_uncontained_fire_or_loot_tables', !['minecraft:water', 'minecraft:lava', 'minecraft:fire', 'minecraft:soul_fire'].includes(name) && !record.nbt?.has('LootTable'))
  materials[name] = (materials[name] ?? 0) + 1
  if (record.nbt) {
    check('only_empty_barrels_and_decorative_banners', ['minecraft:barrel', 'minecraft:red_wall_banner'].includes(name))
    if (name === 'minecraft:barrel') check('barrels_empty_no_loot', !record.nbt.has('Items') || record.nbt.getList('Items').length === 0)
    blockEntities.push({ pos: record.pos, block: name, id: record.nbt.getString('id') })
  }
  if (name === 'minecraft:lantern') lamps.push(record.pos)
  if (name === 'minecraft:campfire') braziers.push(record.pos)
  if (name === 'minecraft:cobweb') check('cobwebs_only_above_player_routes', record.pos[1] >= 12)
}
for (let east = 0; east < 29; east++) for (let south = 0; south < 33; south++) {
  check('continuous_solid_foundation', solid([east, 0, south]) && solid([east, 1, south]))
  check('complete_two_block_roof', solid([east, 16, south]) && solid([east, 17, south]))
}
for (let up = 2; up <= 15; up++) {
  for (let south = 0; south < 33; south++) for (const east of [0, 28]) check('sealed_sides_full_glass_windows', solid([east, up, south]))
  for (let east = 0; east < 29; east++) {
    check('solid_south_wall', solid([east, up, 32]))
    if (!(east >= 12 && east <= 16 && up <= 7)) check('north_wall_only_declared_port', solid([east, up, 0]))
  }
}
const route = []
for (let south = 0; south <= 22; south++) for (let east = 12; east <= 16; east++) {
  check('five_wide_level_main_aisle_three_high', solid([east, 1, south]) && [2, 3, 4].every(up => air([east, up, south])))
  route.push([east, 2, south])
}
for (let east = 12; east <= 16; east++) {
  for (const [south, up] of [[23, 2], [24, 3]]) {
    const stair = blockAt([east, up, south])
    check('two_stair_rows_ascending_south_with_headroom', stair.state.is('minecraft:polished_andesite_stairs') && stair.state.getProperty('facing') === 'south' && stair.state.getProperty('half') === 'bottom' && [up + 1, up + 2, up + 3].every(height => air([east, height, south])))
  }
  for (let south = 25; south <= 27; south++) check('dais_landing_three_high_clear', solid([east, 3, south]) && [4, 5, 6].every(up => air([east, up, south])))
}
for (const east of [4, 24]) for (let south = 7; south <= 16; south++) check('side_furniture_retains_bypass', [2, 3].every(up => air([east, up, south])))
for (const east of [6, 22]) for (const south of [8, 17, 28]) for (let up = 1; up <= 16; up++) check('six_pillars_continuously_supported_to_roof', solid([east, up, south]))
check('six_hanging_lanterns', lamps.length === 6)
for (const [east, up, south] of lamps) {
  check('lantern_chains_have_continuous_solid_anchor', [10, 11, 12].every(height => nameAt([east, height, south]) === 'minecraft:chain') && [13, 14, 15, 16].every(height => solid([east, height, south])))
}
check('four_supported_braziers', braziers.length === 4)
for (const [east, up, south] of braziers) {
  check('braziers_off_main_route_on_stone', solid([east, up - 1, south]) && (east < 11 || east > 17))
  for (let offsetEast = -1; offsetEast <= 1; offsetEast++) for (let offsetSouth = -1; offsetSouth <= 1; offsetSouth++) for (let offsetUp = -1; offsetUp <= 1; offsetUp++) {
    const name = nameAt([east + offsetEast, up + offsetUp, south + offsetSouth]) ?? ''
    check('no_combustible_decor_touching_braziers', !name.includes('wool') && !name.includes('bookshelf') && !name.includes('spruce') && !name.includes('barrel') && !name.includes('banner'))
  }
}
const deviations = [
  '概念图为敞开的剖视展示；最终模板补齐北立面、完整双层屋顶和六格高北入口，剖切只作用于预览。',
  '主红毯用与地板齐平的红色羊毛配黄陶/金块边线表示，避免薄地毯改变接入脚位；王座台使用两级可走楼梯。',
  '原图自由布幔和金属圆柱按整方块实现为红羊毛垂幔、金边石柱及原版带金花纹旗帜。',
  '火盆用离路的石质高座营火表示；武器架为无实体铁栏/金饰陈设，木桶无库存，不添加战利品或怪物。',
  '扩为29×18×33格以保留五格中轴和家具旁通道；整体比例、窗格及陈设按可行走尺度调整。'
]
const metadata = {
  id: 'H01', name: '残金王座大厅', category: 'halls', size,
  entrance: [14, 2, 0], interior: [14, 2, 4], facing: 'north',
  ports: [{ side: 'north', feet: [14, 2, 0], width: 5, clear_height: 6 }],
  floor_y: 1, player_feet_y: 2, throne_approach: [14, 4, 26],
  main_route: { min: [12, 2, 0], max: [16, 4, 22], width: 5, clearance_height: 3 },
  dais: { min: [8, 2, 23], max: [20, 3, 30], stairs: [{ z: 23, y: 2, facing: 'south' }, { z: 24, y: 3, facing: 'south' }], landing_feet_y: 4 },
  safe_points: [[14, 2, 4], [14, 2, 12], [14, 2, 21], [14, 4, 26]],
  reference, reference_sha256: referenceSha256, sha256: digest(bytes),
  data_version: 3465, minecraft: '1.20.1', forge_test_target: '47.4.10',
  nbt_air_policy: 'Explicit air; all 17226 cells recorded. Full-volume occupancy preflight required before integration.',
  complete_roof: true, materials, block_entities: blockEntities, design_deviations: deviations,
  build_recipe: 'tools/recipe.mjs', deployment: 'not_deployed', production_saves_modified: false,
  integration_notes: ['Only one north port; this is a terminal throne hall.', 'Do not deploy the disposable native-qa fixture.', 'Rotate coordinates and block states together using the same vanilla transform.', 'Refuse occupied volume including explicit air; preserve per-room generation markers and player edits.']
}
await fs.writeFile(path.join(root, 'room.json'), JSON.stringify(metadata, null, 2) + '\n')
await fs.writeFile(path.join(root, 'qa/static.json'), JSON.stringify({ status: 'PASS', id: 'H01', source_sha256: metadata.sha256, reference_sha256: referenceSha256, size, voxel_records: records.length, checks, route_foot_positions: route.length, lamps, braziers, block_entities: blockEntities, materials, evidence_level: 'NBT geometry only; real registry, collision paths and stability checked separately by Forge' }, null, 2) + '\n')
console.log(JSON.stringify({ status: 'PASS', checks: Object.keys(checks).length, size, sha256: metadata.sha256 }))
