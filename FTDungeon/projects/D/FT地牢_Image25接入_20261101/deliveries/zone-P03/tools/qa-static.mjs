import fs from 'node:fs/promises'
import path from 'node:path'
import { createHash } from 'node:crypto'
import { fileURLToPath, pathToFileURL } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const { NbtFile, Structure } = await import(pathToFileURL('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'))
const reference = 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/parkour/P03-parkour-lava-bubble-jumps.png'
const referenceSha256 = '2c844d5d29371dfc8e529c47aa77f3b61c48d64d3364aca57e365532e75dd6d3'
const hash = bytes => createHash('sha256').update(bytes).digest('hex')
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const file = NbtFile.read(bytes)
const structure = Structure.fromNbt(file.root)
const records = structure.getBlocks()
const byPosition = new Map(records.map(block => [block.pos.join(','), block]))
const nameAt = position => byPosition.get(position.join(','))?.state.getName().toString() ?? 'minecraft:air'
const checks = {}
const check = (label, valid) => { if (!valid) throw new Error(label); checks[label] = true }
const sourceHash = hash(bytes)
check('reference_sha256', hash(await fs.readFile(reference)) === referenceSha256)
check('size_27x13x29', JSON.stringify(structure.getSize()) === '[27,13,29]')
check('full_explicit_volume_and_unique_positions', records.length === 10179 && byPosition.size === 10179)
check('gzip', bytes[0] === 31 && bytes[1] === 139)
check('data_version_3465', file.root.getNumber('DataVersion') === 3465)
check('entities_empty', file.root.getList('entities').length === 0)
const forbidden = new Set(['spawner', 'command_block', 'chain_command_block', 'repeating_command_block', 'structure_block', 'jigsaw', 'tnt', 'respawn_anchor', 'structure_void'])
check('vanilla_only_no_forbidden_blocks_or_block_entities', records.every(block => block.state.getName().toString().startsWith('minecraft:') && !forbidden.has(block.state.getName().toString().split(':')[1]) && !block.nbt))
for (let east = 0; east < 27; east++) {
  for (let south = 0; south < 29; south++) {
    check('complete_solid_base', nameAt([east, 0, south]) === 'minecraft:polished_blackstone')
    check('complete_roof', nameAt([east, 12, south]) === 'minecraft:polished_blackstone_bricks')
  }
}
const lava = records.filter(block => block.state.getName().toString() === 'minecraft:lava')
check('289_lava_sources', lava.length === 289)
for (const block of lava) {
  const [east, up, south] = block.pos
  check('lava_bottom', nameAt([east, up - 1, south]) === 'minecraft:polished_blackstone')
  check('lava_lid_full_glass', nameAt([east, up + 1, south]) === 'minecraft:glass')
  check('lava_sources_only', block.state.getProperties().level === '0')
  for (const [deltaEast, deltaSouth] of [[1, 0], [-1, 0], [0, 1], [0, -1]]) check('lava_sidewalls', ['minecraft:lava', 'minecraft:polished_blackstone'].includes(nameAt([east + deltaEast, up, south + deltaSouth])))
}
for (const south of [0, 1, 27, 28]) {
  for (let east = 12; east <= 14; east++) {
    check('three_wide_port_floor', ['minecraft:polished_blackstone_bricks', 'minecraft:red_nether_bricks'].includes(nameAt([east, 4, south])))
    for (let up = 5; up <= 8; up++) check('four_high_ports', nameAt([east, up, south]) === 'minecraft:air')
  }
}
const jumpPairs = [
  [[11.5, 5, 5.5], [11.5, 5, 7.5]],
  [[12.5, 5, 9.5], [13.5, 5, 11.5]],
  [[13.5, 5, 13.5], [12.5, 5, 15.5]],
  [[12.5, 5, 17.5], [13.5, 5, 19.5]],
  [[14.5, 5, 21.5], [14.5, 5, 23.5]]
]
const jumps = jumpPairs.map(([takeoff, landing], index) => {
  const samples = [{ tick: 0, feet: takeoff }]
  const distance = Math.hypot(landing[0] - takeoff[0], landing[2] - takeoff[2])
  let feetUp = takeoff[1]
  let velocityUp = 0.42
  for (let tick = 1; tick <= 20; tick++) {
    feetUp += velocityUp
    velocityUp = (velocityUp - 0.08) * 0.98
    const progress = Math.min(1, tick / 11)
    const feet = [takeoff[0] + (landing[0] - takeoff[0]) * progress, Math.max(5, feetUp), takeoff[2] + (landing[2] - takeoff[2]) * progress]
    samples.push({ tick, feet })
    if (feetUp < 5) break
  }
  check('no_sprint_horizontal_speed_at_most_0_204', distance / 11 <= 0.204)
  check('horizontal_arrival_before_landing', samples.length > 11)
  for (const endpoint of [takeoff, landing]) check('full_solid_takeoff_and_landing', !['minecraft:air', 'minecraft:glass', 'minecraft:lava'].includes(nameAt([Math.floor(endpoint[0]), 4, Math.floor(endpoint[2])])))
  for (const { feet } of samples.slice(1, -1)) {
    for (let east = Math.floor(feet[0] - 0.3); east <= Math.floor(feet[0] + 0.3 - 1e-7); east++) {
      for (let up = Math.floor(feet[1] + 1e-7); up <= Math.floor(feet[1] + 1.8 - 1e-7); up++) {
        for (let south = Math.floor(feet[2] - 0.3); south <= Math.floor(feet[2] + 0.3 - 1e-7); south++) check('player_jump_clearance', nameAt([east, up, south]) === 'minecraft:air')
      }
    }
  }
  return { index: index + 1, takeoff, landing, distance, height_change: 0, ticks: samples.length - 1, horizontal_speed_per_tick: distance / 11, player_width: 0.6, player_height: 1.8, samples }
})
const catchCells = []
for (let east = 0; east < 27; east++) {
  for (let south = 0; south < 29; south++) if (nameAt([east, 2, south]) === 'minecraft:glass' && nameAt([east, 3, south]) === 'minecraft:air' && nameAt([east, 4, south]) === 'minecraft:air') catchCells.push([east, 3, south])
}
const catchKeys = new Set(catchCells.map(position => position.join(',')))
const reached = new Set(['6,3,7'])
const pending = [[6, 3, 7]]
while (pending.length) {
  const [east, up, south] = pending.shift()
  for (const [deltaEast, deltaSouth] of [[1, 0], [-1, 0], [0, 1], [0, -1]]) {
    const next = [east + deltaEast, up, south + deltaSouth]
    if (catchKeys.has(next.join(',')) && !reached.has(next.join(','))) { reached.add(next.join(',')); pending.push(next) }
  }
}
check('all_catch_cells_reach_recovery_stairs', reached.size === catchCells.length)
const lanterns = records.filter(block => block.state.getName().toString() === 'minecraft:lantern')
check('eight_hanging_lanterns', lanterns.length === 8)
for (const block of lanterns) {
  const [east, up, south] = block.pos
  check('lantern_has_chain', nameAt([east, up + 1, south]) === 'minecraft:chain' && nameAt([east, up + 2, south]) === 'minecraft:chain')
  check('chain_roof_anchor', nameAt([east, 11, south]) === 'minecraft:polished_blackstone_bricks')
}
const metadata = {
  schema: 'ft-independent-zone-room-v1', id: 'P03', name: '熔岩泡跳台', category: 'parkour', size: [27, 13, 29],
  entrance: [13, 5, 0], interior: [13, 5, 3], exit: [13, 5, 28], facing: 'north', sha256: sourceHash,
  reference, reference_sha256: referenceSha256, minecraft: '1.20.1', data_version: 3465,
  ports: [{ side: 'north', feet: [13, 5, 0], width: 3, clear_height: 4 }, { side: 'south', feet: [13, 5, 28], width: 3, clear_height: 4 }],
  floor_y: 4, player_feet_y: 5, roof_included: true,
  parkour: { platform_top_y: 5, pad_count: 4, pad_size: [3, 3], jumps: jumps.map(({ samples, ...jump }) => jump), checkpoint: [13, 5, 3], difficulty: 'easy; no sprint or potion required' },
  recovery: { type: 'full glass safety floor over sealed lava; northwest return stair', catch_feet_y: 3, maximum_fall_blocks: 2, stairs: { east_range: [5, 7], south_range: [5, 6], from: [6, 3, 7], to: [6, 5, 4] }, walk_back_to_entrance: true, exit_requires_elevated_route: true },
  lava: { cells: 289, sources_only: true, min: [5, 1, 6], max: [21, 1, 22], bottom: 'solid polished blackstone at y=0', lid: 'full glass at y=2', never_exposed_to_player: true },
  placement: { mode: 'isolated delivery only; not deployed', full_volume_preflight_required: true, explicit_air: true, occupied_volume_must_be_refused: true, native_rotation_required: true },
  visual_adaptations: ['solid roof retained; cutaway only in previews', 'transparent safety lid preserves visible lava and guarantees harmless misses', 'wood crates replaced by nonflammable carved stone crates', 'four offset 3x3 pads prioritize easy repeatable jumps']
}
await fs.mkdir(path.join(root, 'qa'), { recursive: true })
await fs.writeFile(path.join(root, 'room.json'), JSON.stringify(metadata, null, 2) + '\n')
await fs.writeFile(path.join(root, 'qa/route.json'), JSON.stringify({ status: 'PASS', source_sha256: sourceHash, method: 'vanilla 0.42 initial jump, gravity 0.08, vertical drag 0.98, nonsprint speed <=0.204; native collision verification required', catch_cells: catchCells.length, recovery_connected: reached.size, jumps }, null, 2) + '\n')
await fs.writeFile(path.join(root, 'qa/static.json'), JSON.stringify({ status: 'PASS', source_sha256: sourceHash, reference_sha256: referenceSha256, checks, block_records: records.length, palette: [...new Set(records.map(block => block.state.toString()))], lava_sources: lava.length, catch_cells: catchCells.length, max_jump_distance: Math.max(...jumps.map(jump => jump.distance)) }, null, 2) + '\n')
console.log(JSON.stringify({ status: 'PASS', checks: Object.keys(checks).length, source_sha256: sourceHash, max_jump: Math.max(...jumps.map(jump => jump.distance)), catch_cells: catchCells.length }))
