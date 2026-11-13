import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createHash } from 'node:crypto'
import { createRequire } from 'node:module'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const require = createRequire('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/package.json')
const { NbtFile, Structure } = require('@mattzh72/lodestone')
const fingerprint = bytes => createHash('sha256').update(bytes).digest('hex')
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const decoded = NbtFile.read(bytes).root
const structure = Structure.fromNbt(decoded)
const records = structure.getBlocks()
const blockAt = (east, up, south) => structure.getBlock([east, up, south])
const nameAt = (east, up, south) => blockAt(east, up, south)?.state.getName().toString()
const isAir = (east, up, south) => nameAt(east, up, south) === 'minecraft:air'
const isSolid = (east, up, south) => /minecraft:(stone_bricks|mossy_stone_bricks|cracked_stone_bricks|chiseled_stone_bricks|polished_andesite|moss_block|rooted_dirt|dark_oak_planks|dark_oak_log|sandstone|cut_sandstone|chiseled_sandstone)$/.test(nameAt(east, up, south))
const checks = []
const check = (name, condition, detail) => { checks.push({ name, pass: Boolean(condition), detail }); if (!condition) throw new Error(name + ': ' + JSON.stringify(detail)) }
await fs.mkdir(path.join(root, 'qa'), { recursive: true })
check('gzip_structure_size_version_single_palette', bytes[0] === 31 && bytes[1] === 139 && structure.getSize().join() === '19,12,19' && decoded.getNumber('DataVersion') === 3465 && !decoded.has('palettes'), { size: structure.getSize(), data_version: decoded.getNumber('DataVersion') })
check('full_explicit_volume_unique_in_bounds', records.length === 19 * 12 * 19 && new Set(records.map(block => block.pos.join())).size === records.length && records.every(block => structure.isInside(block.pos)), { records: records.length })
check('zero_structure_entities', decoded.getList('entities').length === 0, { count: decoded.getList('entities').length })
const materials = {}
for (const block of records) materials[block.state.getName().toString()] = (materials[block.state.getName().toString()] ?? 0) + 1
check('vanilla_no_hazards_or_technical_blocks', Object.keys(materials).every(name => name.startsWith('minecraft:') && !/(lava|water$|fire|magma|campfire|cobweb|spawner|command_block|structure_block|jigsaw|powder_snow|wither|cactus|berry)/.test(name)), { materials: Object.keys(materials).length })
const layer = up => Array.from({ length: 361 }, (_, index) => isSolid(index % 19, up, Math.floor(index / 19))).every(Boolean)
check('complete_foundation_and_two_layer_roof', [0, 1, 10, 11].every(layer), { layers: [0, 1, 10, 11], blocks_per_layer: 361 })
const route = []
for (let east = 0; east < 19; east++) for (let south = 0; south < 19; south++) {
  if ((east >= 7 && east <= 11 && south <= 11) || (east >= 9 && south >= 7 && south <= 11)) route.push([east, 2, south])
}
check('five_wide_three_high_level_corner_route', route.every(([east, up, south]) => isSolid(east, up - 1, south) && [up, up + 1, up + 2].every(height => isAir(east, height, south))), { floor_cells: route.length, width: 5, clear_height: 3 })
const entrance = [9, 2, 0]
const interior = [9, 2, 4]
const exit = [18, 2, 9]
const reachable = new Set([entrance.join()])
const queue = [entrance]
while (queue.length) {
  const [east, up, south] = queue.shift()
  for (const [deltaEast, deltaSouth] of [[1, 0], [-1, 0], [0, 1], [0, -1]]) {
    const next = [east + deltaEast, up, south + deltaSouth]
    if (reachable.has(next.join()) || !structure.isInside(next) || !isSolid(next[0], up - 1, next[2]) || !isAir(next[0], up, next[2]) || !isAir(next[0], up + 1, next[2])) continue
    reachable.add(next.join())
    queue.push(next)
  }
}
check('entrance_safe_point_exit_bidirectional_walk', reachable.has(interior.join()) && reachable.has(exit.join()), { entrance, interior, exit, reachable_feet_cells: reachable.size })
const openings = []
let sealed = true
for (let offset = 0; offset < 19; offset++) for (let up = 2; up <= 9; up++) {
  if (isAir(offset, up, 0)) openings.push(['north', offset, up])
  if (isAir(18, up, offset)) openings.push(['east', offset, up])
  sealed &&= !isAir(0, up, offset) && !isAir(offset, up, 18)
}
check('west_and_south_boundaries_sealed', sealed, { layers: 'y=2..9' })
check('only_two_five_by_five_boundary_openings', openings.length === 50 && openings.every(([, offset, up]) => offset >= 7 && offset <= 11 && up >= 2 && up <= 6), { open_blocks: openings.length })
const vines = records.filter(block => block.state.getName().toString() === 'minecraft:vine')
const directions = { west: [-1, 0, 0], east: [1, 0, 0], north: [0, 0, -1], south: [0, 0, 1], up: [0, 1, 0] }
check('all_vine_faces_have_solid_backing', vines.length > 20 && vines.every(block => Object.entries(directions).filter(([face]) => block.state.getProperties()[face] === 'true').every(([, offset]) => isSolid(...block.pos.map((value, index) => value + offset[index])))), { vines: vines.length })
const lamps = records.filter(block => block.state.getName().toString() === 'minecraft:lantern')
check('four_hanging_lamps_chain_to_roof', lamps.length === 4 && lamps.every(({ pos: [east, up, south] }) => [7, 8, 9].every(height => nameAt(east, height, south) === 'minecraft:chain') && isSolid(east, 10, south)), { lamps: lamps.map(block => block.pos) })
const roots = records.filter(block => block.state.getName().toString() === 'minecraft:hanging_roots')
check('hanging_roots_backed_by_rooted_dirt', roots.length === 5 && roots.every(({ pos: [east, up, south] }) => nameAt(east, up + 1, south) === 'minecraft:rooted_dirt'), { roots: roots.length })
const smallProps = records.filter(block => /minecraft:(candle|flower_pot|potted_.*|moss_carpet)$/.test(block.state.getName().toString()))
check('candles_pots_moss_have_floor_support', smallProps.every(({ pos: [east, up, south] }) => isSolid(east, up - 1, south)), { supported_props: smallProps.length })
check('statue_and_altar_grounded', [2, 3, 4, 5, 6, 7].every(up => isSolid(5, up, 15)) && [3, 7].every(east => isSolid(east, 3, 14)), { statue: [5, 7, 15], altar_floor: [3, 2, 13] })
check('persistent_leaves_no_decay', records.filter(block => block.state.getName().toString().endsWith('_leaves')).every(block => block.state.getProperties().persistent === 'true'), { leaves: materials['minecraft:azalea_leaves'] })
const columns = [[5, 4], [14, 4], [4, 10], [14, 14]]
check('four_columns_independently_grounded_to_roof', columns.every(([east, south]) => Array.from({ length: 12 }, (_, up) => isSolid(east, up, south)).every(Boolean)), { columns, continuous_y: [0, 11] })
check('wooden_shelf_posts_and_boards_supported', [4, 7].every(south => [1, 2, 3, 4, 5].every(up => isSolid(2, up, south))) && [2, 4].every(up => [4, 5, 6, 7].every(south => isSolid(2, up, south))), { grounded_posts: [[2, 2, 4], [2, 2, 7]], board_y: [2, 4] })
const blockEntities = records.filter(block => block.nbt)
check('three_empty_barrels_one_decorative_banner', blockEntities.length === 4 && blockEntities.every(block => {
  if (block.nbt.has('LootTable') || block.nbt.has('LootTableSeed')) return false
  return block.nbt.getString('id') === 'minecraft:banner' || (block.nbt.getString('id') === 'minecraft:barrel' && block.nbt.getList('Items').length === 0)
}), { blocks: blockEntities.map(block => ({ pos: block.pos, id: block.nbt.getString('id') })) })
const reference = 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/corners/C03-corners-overgrown-corner-shrine.png'
const referenceHash = fingerprint(await fs.readFile(reference))
check('reference_sha256_matches_assignment', referenceHash === '66ec2c533d55317d11a79016865dd7dc6c3b7c19d24d5786cc09d8508bbc9d27', { sha256: referenceHash })
const metadata = {
  id: 'C03', name: '蔓生角落神龛', category: 'corner', size: [19, 12, 19], entrance, interior, facing: 'north', exit,
  sha256: fingerprint(bytes), reference, reference_sha256: referenceHash, data_version: 3465,
  ports: [{ id: 'entrance', facing: 'north', feet: entrance, width: 5, height: 5, floor_y: 1, aperture: { from: [7, 2, 0], to: [11, 6, 1] } }, { id: 'exit', facing: 'east', feet: exit, width: 5, height: 5, floor_y: 1, aperture: { from: [17, 2, 7], to: [18, 6, 11] } }],
  safe_points: [interior, [13, 2, 9]], route: { shape: 'north-to-east right-angle', width: 5, clear_height: 3, feet_y: 2, cells: route, reachable_feet_cells: reachable.size },
  explicit_air: true, full_volume_preflight_required: true, roof: { complete: true, layers: [10, 11] },
  containers: { count: 3, empty: true, loot_tables: false }, entities: 0, deployed: false,
  integration: { occupancy: 'Reject occupied full transformed volume including air, existing fluids and entities; do not clear player builds.', persistence: 'Use an independent C03 generation marker and preserve existing rooms and player changes.', rotation: 'Transform coordinates and states together using native StructureTemplate rotations around BlockPos.ZERO; rotated footprints may have negative offsets.' },
  materials
}
const rotationTransforms = { NONE: ([east, up, south]) => [east, up, south], CLOCKWISE_90: ([east, up, south]) => [-south, up, east], CLOCKWISE_180: ([east, up, south]) => [-east, up, -south], COUNTERCLOCKWISE_90: ([east, up, south]) => [south, up, -east] }
metadata.rotated_ports = Object.entries(rotationTransforms).map(([rotation, transform], index) => ({ rotation, rotation_pivot: [0, 0, 0], entrance: transform(entrance), interior: transform(interior), exit: transform(exit), entrance_facing: ['north', 'east', 'south', 'west'][index], exit_facing: ['east', 'south', 'west', 'north'][index], footprint_min: index === 0 ? [0, 0, 0] : index === 1 ? [-18, 0, 0] : index === 2 ? [-18, 0, -18] : [0, 0, -18], footprint_max: index === 0 ? [18, 11, 18] : index === 1 ? [0, 11, 18] : index === 2 ? [0, 11, 0] : [18, 11, 0] }))
await fs.writeFile(path.join(root, 'room.json'), JSON.stringify(metadata, null, 2) + '\n')
await fs.writeFile(path.join(root, 'qa/static.json'), JSON.stringify({ status: 'PASS', source_nbt_sha256: metadata.sha256, checks, summary: { checks: checks.length, records: records.length, palette_count: decoded.getList('palette').length, explicit_air: materials['minecraft:air'], reachable_feet_cells: reachable.size, route_cells: route.length }, limitations: ['Static geometry checks supplemented by isolated Forge collision, registry, block entity and neighbor tick verification.'] }, null, 2) + '\n')
console.log(JSON.stringify({ status: 'PASS', checks: checks.length, sha256: metadata.sha256, records: records.length, reachable: reachable.size, route: route.length }))

