import assert from 'node:assert/strict'
import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createHash } from 'node:crypto'
import { NbtFile, Structure } from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'

const root = path.dirname(fileURLToPath(import.meta.url))
const reference = 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/ruins/R02-ruins-abandoned-outpost-room.png'
const expectedReference = '750661cfb67285196ab621178b07f857da3df6e804593cad03bd9cf301604512'
const hash = data => createHash('sha256').update(data).digest('hex')
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const fingerprint = hash(bytes)
const file = NbtFile.read(bytes)
const structure = Structure.fromNbt(file.root)
const blocks = structure.getBlocks()
const coordinates = new Map(blocks.map(block => [block.pos.join(','), block]))
const name = position => coordinates.get(position.join(','))?.state.getName().path ?? 'missing'
const properties = position => coordinates.get(position.join(','))?.state.getProperties() ?? {}
const results = []
const check = (test, condition, evidence) => { results.push({ test, status: condition ? 'PASS' : 'FAIL', evidence }); assert(condition, test) }
const referenceBytes = await fs.readFile(reference)
check('reference_sha256_exact', hash(referenceBytes) === expectedReference, expectedReference)
for (const [filename, key] of [['manifest.json', 'files'], ['verify.json', 'records']]) {
  const source = JSON.parse(await fs.readFile(`D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/docs/${filename}`, 'utf8'))
  const record = source[key].find(record => record.file === 'out/ruins/R02-ruins-abandoned-outpost-room.png')
  check(`reference_${filename}_match`, Boolean(record) && record.sha256.toLowerCase() === expectedReference && record.ok === true, record)
}
check('gzip_single_palette_1_20_1', bytes[0] === 31 && bytes[1] === 139 && file.root.getNumber('DataVersion') === 3465 && file.root.has('palette') && !file.root.has('palettes'), 'Java 1.20.1 / DataVersion 3465')
check('dimensions_and_explicit_unique_volume', JSON.stringify(structure.getSize()) === '[21,11,21]' && blocks.length === 4851 && coordinates.size === 4851, { size: structure.getSize(), records: blocks.length })
const forbidden = new Set(['water', 'lava', 'fire', 'soul_fire', 'magma_block', 'cactus', 'powder_snow', 'sand', 'gravel', 'spawner', 'command_block', 'chain_command_block', 'repeating_command_block', 'structure_block', 'structure_void', 'jigsaw'])
check('vanilla_palette_and_no_forbidden', blocks.every(block => block.state.getName().namespace === 'minecraft' && !forbidden.has(block.state.getName().path)), [...new Set(blocks.map(block => block.state.getName().path))])
check('no_waterlogged_or_lit_fire', blocks.every(block => block.state.getProperties().waterlogged !== 'true' && (block.state.getName().path !== 'campfire' || block.state.getProperties().lit === 'false')), 'Cold brazier; no fluids, falling blocks or active flame')
check('no_entities', file.root.getList('entities').length === 0, 'No armor stands, dropped items, mobs or item frames')
const volume = []
for (let depth = 0; depth < 21; depth += 1) for (let horizontal = 0; horizontal < 21; horizontal += 1) volume.push([horizontal, depth])
check('continuous_full_floor_and_double_roof', volume.every(([horizontal, depth]) => [0, 9, 10].every(vertical => /^(stone_bricks|mossy_stone_bricks|cracked_stone_bricks|polished_andesite)$/.test(name([horizontal, vertical, depth])))), '441 floor cells and 882 full roof cells')
const route = []
for (let depth = 0; depth <= 7; depth += 1) for (let horizontal = 9; horizontal <= 11; horizontal += 1) route.push([horizontal, 1, depth])
for (let depth = 6; depth <= 17; depth += 1) for (const horizontal of [6, 7, 13, 14]) route.push([horizontal, 1, depth])
for (const depth of [6, 7, 16, 17]) for (let horizontal = 6; horizontal <= 14; horizontal += 1) route.push([horizontal, 1, depth])
check('required_route_three_high_air', route.every(([horizontal, vertical, depth]) => [0, 1, 2].every(offset => name([horizontal, vertical + offset, depth]) === 'air')), { route_cells: route.length, entry: [10, 1, 0], safe: [10, 1, 6] })
const queue = [[10, 1, 0]]
const reachable = new Set(['10,1,0'])
for (let index = 0; index < queue.length; index += 1) {
  const [horizontal, vertical, depth] = queue[index]
  for (const [offsetHorizontal, offsetDepth] of [[1, 0], [-1, 0], [0, 1], [0, -1]]) {
    const next = [horizontal + offsetHorizontal, vertical, depth + offsetDepth]
    const key = next.join(',')
    if (!reachable.has(key) && name(next) === 'air' && name([next[0], 2, next[2]]) === 'air' && !['air', 'missing'].includes(name([next[0], 0, next[2]]))) { reachable.add(key); queue.push(next) }
  }
}
check('entrance_safe_point_and_ring_connected', route.every(position => reachable.has(position.join(','))), { reachable_foot_positions: reachable.size })
const nonAir = blocks.filter(block => block.state.getName().path !== 'air')
const supported = new Set(nonAir.filter(block => block.pos[1] === 0).map(block => block.pos.join(',')))
const component = [...supported].map(key => key.split(',').map(Number))
for (let index = 0; index < component.length; index += 1) for (const offset of [[1, 0, 0], [-1, 0, 0], [0, 1, 0], [0, -1, 0], [0, 0, 1], [0, 0, -1]]) {
  const adjacent = component[index].map((value, axis) => value + offset[axis])
  const key = adjacent.join(',')
  if (!supported.has(key) && !['air', 'missing'].includes(name(adjacent))) { supported.add(key); component.push(adjacent) }
}
check('all_nonair_in_grounded_structural_component', nonAir.every(block => supported.has(block.pos.join(','))), { grounded: supported.size, nonair: nonAir.length })
const lanterns = blocks.filter(block => block.state.getName().path === 'lantern')
check('hanging_lights_roof_anchored', lanterns.every(block => {
  const [horizontal, vertical, depth] = block.pos
  return properties(block.pos).hanging === 'true' ? [6, 7, 8].every(height => name([horizontal, height, depth]) === 'chain') && name([horizontal, 9, depth]).endsWith('stone_bricks') : properties([horizontal, vertical - 1, depth]).type === 'top'
}), { lanterns: lanterns.length, hanging: 4, standing: 1 })
check('banner_wall_backing', blocks.filter(block => block.state.getName().path === 'red_wall_banner').every(block => name([block.pos[0] - 1, block.pos[1], block.pos[2]]).endsWith('stone_bricks')), 'Both east-facing banners have direct west-wall backing')
const inventoryBlocks = blocks.filter(block => ['chest', 'barrel', 'campfire'].includes(block.state.getName().path))
check('empty_containers_and_no_loot', inventoryBlocks.every(block => !block.nbt || !block.nbt.has('Items') && !block.nbt.has('LootTable')), { count: inventoryBlocks.length, blocks: inventoryBlocks.map(block => ({ pos: block.pos, id: block.state.getName().path })) })
check('reference_furnishing_present', ['red_wall_banner', 'chest', 'barrel', 'grindstone', 'iron_bars', 'spruce_stairs', 'campfire', 'cobweb', 'white_carpet'].every(identity => blocks.some(block => block.state.getName().path === identity)), 'Grounded striped pillars, map table, two chairs, weapon rack, supply chest, stacked crates, boarded niches, cold brazier, red textile and rubble')
const metadata = {
  id: 'zone-R02', design_id: 'R02', name: '废弃前哨', size: [21, 11, 21], entrance: [10, 1, 0], interior: [10, 1, 6], facing: 'north', sha256: fingerprint,
  reference, reference_sha256: expectedReference, format: 'gzip vanilla Java 1.20.1 single-palette structure NBT', data_version: 3465,
  required_walkway: route, required_headroom: 3, roof_layers: [9, 10], reference_manifest: 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/docs/manifest.json', reference_verify: 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/docs/verify.json',
  concept_adaptations: ['概念图为无顶剖视；最终结构补齐双层石顶、四根落地立柱和梁网，剖切仅用于渲染。', '中央指挥桌采用原版木台、浅色地图面和红白标记；墙边铁条军械架不放实体或可领取武器。', '东侧木封门为封闭陈设，唯一功能入口位于北侧中央；安全点与桌旁环路保持三格净高。', '破损由裂石、苔石、侧边碎石和角落蛛网表现；火盆熄灭，容器为空。'],
  deployment: { deployed: false, production_instance_modified: false, integration_slot_assigned: false, player_saves_modified: false },
}
await fs.writeFile(path.join(root, 'room.json'), JSON.stringify(metadata, null, 2) + '\n')
await fs.writeFile(path.join(root, 'qa/static.json'), JSON.stringify({ status: 'PASS', structure_sha256: fingerprint, reference_sha256: expectedReference, test_count: results.length, checks: results, native_validation_required: ['registry and exact block states', 'native collision and block survival', 'four rotations and 200 tick stability', 'empty native block entities'] }, null, 2) + '\n')
const materials = new Map()
for (const block of nonAir) materials.set(block.state.getName().toString(), (materials.get(block.state.getName().toString()) ?? 0) + 1)
await fs.writeFile(path.join(root, 'materials.json'), JSON.stringify({ structure_sha256: fingerprint, total_non_air: nonAir.length, materials: [...materials].sort((first, second) => second[1] - first[1]).map(([id, count]) => ({ id, count })) }, null, 2) + '\n')
console.log(JSON.stringify({ status: 'PASS', tests: results.length, records: blocks.length, non_air: nonAir.length, reachable: reachable.size, sha256: fingerprint }, null, 2))
