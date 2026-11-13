import fs from 'node:fs/promises'
import path from 'node:path'
import { createHash } from 'node:crypto'
import { fileURLToPath, pathToFileURL } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const skill = 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
const { NbtFile, Structure } = await import(pathToFileURL(path.join(skill, 'node_modules/@mattzh72/lodestone/lib/index.js')))
const reference = 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/parkour/P01-parkour-gap-jump-pit.png'
const referenceHash = 'd8809777350e5a681cd94c59af4d328631e02ef5bc2226f1ebe859a6e8de7603'
const digest = bytes => createHash('sha256').update(bytes).digest('hex')
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const encoded = NbtFile.read(bytes)
const structure = Structure.fromNbt(encoded.root)
const records = structure.getBlocks()
const positions = new Map(records.map(block => [block.pos.join(','), block]))
const nameAt = (east, up, south) => positions.get([east, up, south].join(','))?.state.getName().toString() ?? 'minecraft:air'
const clear = point => nameAt(...point) === 'minecraft:air'
const full = point => { const name = nameAt(...point); return name !== 'minecraft:air' && !name.endsWith('iron_bars') && !name.endsWith('lantern') && !name.endsWith('chain') && !name.endsWith('torch') }
const walkable = point => clear(point) && clear([point[0], point[1] + 1, point[2]]) && full([point[0], point[1] - 1, point[2]])
const checks = {}
const check = (name, condition) => { if (!condition) throw new Error(name); checks[name] = true }
check('reference_fingerprint', digest(await fs.readFile(reference)) === referenceHash)
check('gzip_compressed', bytes[0] === 31 && bytes[1] === 139)
check('java_1_20_1_data_version', encoded.root.getNumber('DataVersion') === 3465)
check('dimensions', JSON.stringify(structure.getSize()) === '[19,14,29]')
check('unique_explicit_full_volume', records.length === 7714 && positions.size === 7714)
check('no_entities', encoded.root.getList('entities').length === 0)
const forbidden = new Set(['command_block','chain_command_block','repeating_command_block','structure_block','jigsaw','structure_void','spawner','tnt','respawn_anchor'])
const hazards = new Set(['lava','water','fire','soul_fire','magma_block','cactus','powder_snow','cobweb','campfire','soul_campfire','wither_rose','sweet_berry_bush'])
const materials = {}
const supportedFixtures = []
for (const block of records) {
  const identifier = block.state.getName().toString()
  const short = identifier.split(':')[1]
  check('all_coordinates_in_bounds', block.pos.every((value, axis) => Number.isInteger(value) && value >= 0 && value < [19,14,29][axis]))
  check('vanilla_palette', identifier.startsWith('minecraft:'))
  check('no_executable_or_spawn_blocks', !forbidden.has(short))
  check('no_fluids_or_damage_blocks', !hazards.has(short))
  check('no_block_entities_loot_or_inventory', !block.nbt)
  materials[identifier] = (materials[identifier] ?? 0) + 1
  const [east, up, south] = block.pos
  if (short === 'lantern') {
    const hanging = block.state.getProperties().hanging === 'true'
    if (hanging) {
      let support = up + 1
      while (nameAt(east, support, south) === 'minecraft:chain') support++
      check('all_hanging_chains_anchored', full([east, support, south]))
    } else check('all_standing_lamps_supported', full([east, up - 1, south]))
    supportedFixtures.push(block.pos)
  }
  if (short === 'wall_torch') {
    const facing = block.state.getProperties().facing
    const back = {east: [-1,0,0],west: [1,0,0],north: [0,0,1],south: [0,0,-1]}[facing]
    check('wall_torches_attached', full([east + back[0], up, south + back[2]]))
  }
}
for (let east = 0; east < 19; east++) {
  for (let south = 0; south < 29; south++) {
    check('unbroken_foundation', full([east,0,south]))
    check('unbroken_roof', full([east,13,south]))
  }
}
for (const south of [0,28]) {
  for (let east = 8; east <= 10; east++) {
    check('three_wide_supported_ports', walkable([east,5,south]))
    for (let up = 5; up <= 8; up++) check('four_high_ports', clear([east,up,south]))
  }
}
const gaps = [[7,8],[12,13],[17,18],[22,23]]
const jumps = []
for (const [index, [first,last]] of gaps.entries()) {
  for (let east = 8; east <= 10; east++) {
    check('supported_takeoff_and_landing', walkable([east,5,first-1]) && walkable([east,5,last+1]))
    for (let south = first; south <= last; south++) {
      check('real_two_cell_gap', nameAt(east,4,south) === 'minecraft:air')
      check('three_cell_safe_drop', walkable([east,2,south]))
    }
    for (let south = first-1; south <= last+2; south++) {
      for (let up = 5; up <= 10; up++) check('minimum_six_block_jump_clearance', clear([east,up,south]))
    }
  }
  jumps.push({id: 'J'+(index+1), takeoff: [9,5,first-1], landing: [9,5,last+1], gap_z: [first,last], empty_gap_blocks: 2, centre_to_centre_blocks: 3, elevation_change: 0, landing_width: 3, minimum_clearance: 6})
}
function reachable(start) {
  const reached = new Set([start.join(',')])
  const queue = [start]
  for (let offset = 0; offset < queue.length; offset++) {
    const current = queue[offset]
    for (const [eastOffset,southOffset] of [[1,0],[-1,0],[0,1],[0,-1]]) {
      for (const upOffset of [0,1,-1]) {
        const next = [current[0]+eastOffset,current[1]+upOffset,current[2]+southOffset]
        if (next.some((value,axis) => value < 0 || value >= [19,14,29][axis]) || reached.has(next.join(',')) || !walkable(next)) continue
        if (upOffset === 1 && !clear([current[0],current[1]+2,current[2]])) continue
        if (upOffset === -1 && !clear([next[0],next[1]+2,next[2]])) continue
        reached.add(next.join(',')); queue.push(next)
      }
    }
  }
  return reached
}
const fromEntry = reachable([9,5,0])
check('entry_safe_point_roundtrip', fromEntry.has('9,5,3'))
check('walking_alone_does_not_bypass_jumps', !fromEntry.has('9,5,28'))
const recovery = []
for (let east = 6; east <= 12; east++) {
  for (let south = 7; south <= 23; south++) {
    if (!walkable([east,2,south])) continue
    const reached = reachable([east,2,south])
    check('every_pit_bottom_cell_returns_to_entrance', reached.has('9,5,0'))
    recovery.push([east,2,south])
  }
}
check('lower_return_aisles_two_wide', [6,7,11,12].every(east => Array.from({length:15},(_,offset) => walkable([east,2,offset+9])).every(Boolean)))
const sourceHash = digest(bytes)
const metadata = {
  id: 'P01', name: '跳坑跑酷 · 石柱试步厅', category: 'parkour', size: [19,14,29], entrance: [9,5,0], interior: [9,5,3], exit: [9,5,28], facing: 'north', minecraft: '1.20.1', dataVersion: 3465, sha256: sourceHash,
  reference, reference_sha256: referenceHash,
  coordinate_convention: 'relative integer XYZ player feet; supporting deck at y=4; falling pit feet y=2',
  ports: {north:{feet:[9,5,0],width:3,height:4},south:{feet:[9,5,28],width:3,height:4}},
  parkour: {direction:'north to south; reversible', deck_feet_y:5, landing_pads:[{from:[8,4,9],to:[10,4,11]},{from:[8,4,14],to:[10,4,16]},{from:[8,4,19],to:[10,4,21]}], jumps, recommended_movement:'short sprint jump; ordinary survival speed, no potion or flight', minimum_jump_headroom:6},
  recovery: {pit_bottom_feet_y:2,deck_to_floor_blocks:3,maximum_fall_from_normal_jump_apex_blocks:4.253,vanilla_predicted_failed_jump_damage_hp_max:2,straight_step_off_damage_hp:0,damage_floor_blocks:[],return_aisles_x:[[6,7],[11,12]],stair_feet_sequences:[[[6,2,9],[6,3,8],[6,4,7],[6,5,6]],[[12,2,9],[12,3,8],[12,4,7],[12,5,6]]], description:'Missed jumps land on a solid dry floor. A missed jump from its apex can cost up to one heart; a straight step-off costs none. Follow the pale side strips north, jump the two one-block risers, return to the start deck.'},
  hazard_boundary: {type:'nonlethal three-block-deep dry pit',pit_from:[6,2,7],pit_to:[12,4,23],side_rails:{x:[5,13],y:[5,7],z:[7,23]},barriers_prevent_side_gallery_bypass:true},
  integration:{deploy:false,preflight_full_volume:true,volume_cells:7714,require_empty_air_and_no_entities_fluids:true,require_persistent_per_room_marker:true,rotate_ports_and_states_together:true}
}
await fs.mkdir(path.join(root,'qa'),{recursive:true})
await fs.writeFile(path.join(root,'room.json'),JSON.stringify(metadata,null,2)+'\n')
await fs.writeFile(path.join(root,'qa/static.json'),JSON.stringify({id:'P01',status:'PASS',source_sha256:sourceHash,reference_sha256:referenceHash,checks,materials,block_records:records.length,unique_positions:positions.size,lamps:supportedFixtures.length,jumps,recoverable_pit_floor_cells:recovery.length,recovery_positions:recovery,entry_reachable_cells:fromEntry.size,limits:'Static integer feet path checks; real collision, rotation, gravity and jump motion checked separately by Forge.'},null,2)+'\n')
console.log(JSON.stringify({status:'PASS',sha256:sourceHash,checks:Object.keys(checks).length,jumps:4,recovery_cells:recovery.length}))

