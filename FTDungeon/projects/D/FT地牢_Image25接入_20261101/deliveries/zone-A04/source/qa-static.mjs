import fs from 'node:fs/promises'
import path from 'node:path'
import { createHash } from 'node:crypto'
import { fileURLToPath, pathToFileURL } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const skill = 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
const { NbtFile, Structure } = await import(pathToFileURL(path.join(skill, 'node_modules/@mattzh72/lodestone/lib/index.js')))
const reference = 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/abandoned/A04-abandoned-smashed-workshop.png'
const referenceHash = '514982633d51b5b667a0edac720160ec3ae2ce16c92df65df478be0ea824f7c2'
const hash = bytes => createHash('sha256').update(bytes).digest('hex')
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const file = NbtFile.read(bytes)
const structure = Structure.fromNbt(file.root)
const records = structure.getBlocks()
const positions = new Map(records.map(block => [block.pos.join(','), block]))
const nameAt = position => positions.get(position.join(','))?.state.getName().toString() ?? 'minecraft:air'
const checks = {}
const check = (label, passed) => { checks[label] = passed; if (!passed) throw new Error(label) }
check('reference_sha256', hash(await fs.readFile(reference)) === referenceHash)
check('gzip_compression', bytes[0] === 31 && bytes[1] === 139)
check('data_version_3465', file.root.getNumber('DataVersion') === 3465)
check('size_19_11_19', JSON.stringify(structure.getSize()) === '[19,11,19]')
check('full_explicit_unique_3971_coordinates', positions.size === 3971 && records.length === 3971)
check('single_palette', !file.root.has('palettes'))
check('no_entities', file.root.getList('entities').length === 0)
const forbidden = new Set(['command_block', 'chain_command_block', 'repeating_command_block', 'structure_block', 'jigsaw', 'structure_void', 'spawner', 'tnt', 'respawn_anchor', 'lava', 'water', 'fire', 'soul_fire', 'powder_snow', 'cobweb'])
const materials = {}
const containers = []
for (const block of records) {
  const name = block.state.getName().toString()
  check('coordinates_in_bounds', block.pos.every((coordinate, axis) => coordinate >= 0 && coordinate < [19,11,19][axis]))
  check('vanilla_and_no_hazards_or_control_blocks', name.startsWith('minecraft:') && !forbidden.has(name.split(':')[1]))
  materials[name] = (materials[name] ?? 0) + 1
  if (block.nbt) {
    check('only_empty_known_containers', ['minecraft:barrel', 'minecraft:chest', 'minecraft:furnace'].includes(name) && block.nbt.has('Items') && block.nbt.getList('Items').length === 0 && !block.nbt.has('LootTable'))
    containers.push({ position: block.pos, type: name, empty: true, loot_table: false })
  }
}
check('five_empty_containers', containers.length === 5)
for (let east = 0; east < 19; east++) for (let south = 0; south < 19; south++) {
  check('solid_full_floor', !['minecraft:air'].includes(nameAt([east, 0, south])))
  check('complete_final_roof_361_cells', nameAt([east, 10, south]) !== 'minecraft:air')
}
for (let east = 8; east <= 10; east++) for (let south = 0; south <= 14; south++) for (let up = 1; up <= 3; up++) check('three_wide_three_high_level_safe_route', nameAt([east, up, south]) === 'minecraft:air')
const queue = records.filter(block => block.pos[1] === 0).map(block => block.pos)
const grounded = new Set(queue.map(position => position.join(',')))
for (let index = 0; index < queue.length; index++) {
  const [east, up, south] = queue[index]
  for (const neighbor of [[east+1,up,south],[east-1,up,south],[east,up+1,south],[east,up-1,south],[east,up,south+1],[east,up,south-1]]) {
    const key = neighbor.join(',')
    if (positions.has(key) && nameAt(neighbor) !== 'minecraft:air' && !grounded.has(key)) { grounded.add(key); queue.push(neighbor) }
  }
}
check('all_nonair_geometry_connected_to_ground', grounded.size === records.filter(block => block.state.getName().toString() !== 'minecraft:air').length)
for (const east of [6,12]) {
  for (const up of [6,7]) check('lantern_chain_to_beam', nameAt([east,up,9]) === 'minecraft:chain')
  check('lantern_anchor_on_beam', nameAt([east,8,9]) === 'minecraft:stripped_spruce_log')
}
for (let east = 5; east <= 12; east++) for (const south of [16,17]) check('bench_worktop_backed_by_full_base', !['minecraft:air','minecraft:spruce_slab'].includes(nameAt([east,1,south])))
check('chest_lid_clear', nameAt([15,2,16]) === 'minecraft:air')
const metadata = {
  id: 'A04', delivery_id: 'zone-A04', name: '砸毁工坊', category: 'abandoned', size: structure.getSize(),
  entrance: [9,1,0], interior: [9,1,3], facing: 'north', floor_y: 0, player_feet_y: 1,
  ports: [{side:'north',feet:[9,1,0],width:3,clear_height:3}],
  main_route: {min:[8,1,0],max:[10,3,14],width:3,clearance_height:3,level:true},
  interaction_points: {workbench:[9,1,14],anvil:[12,1,7],grindstone:[7,1,12],west_barrel:[3,1,13],chest:[15,1,15],east_barrels:[15,1,14]},
  reference, reference_sha256: referenceHash, sha256: hash(bytes), minecraft:'1.20.1', data_version:3465,
  air_policy:'Explicit air fills all 3971 cells; integration must reject any occupied cell in the full volume before placement.',
  design_adaptations:['Concept ceiling opening becomes sealed cracked-stone repair patch. Preview cutaway never changes final NBT.','Vanilla wall grindstone, suspended chains and attached buttons interpret the tool rack without entities or custom models.','Fallen lanterns remain supported standing lantern blocks. Stone and timber rubble stays outside the central safe route.'],
  roof:'Complete stone layer at y=10, timber lining at y=9, no skylight opening.',
  containers, materials, deployment:'not_deployed', production_saves_modified:false,
  recipe:'source/recipe.mjs', preview:'preview.png', qa:'qa.json', native_qa:'qa/native.json', views:'views/manifest.json'
}
await fs.writeFile(path.join(root,'room.json'), JSON.stringify(metadata,null,2)+'\n')
const report = {id:'A04',status:'PASS',evidence_level:'Static final-NBT audit; native collision/state validity is recorded separately.',source_sha256:metadata.sha256,reference_sha256:referenceHash,checks,records:records.length,palette:file.root.getList('palette').length,nonair:grounded.size,containers,materials}
await fs.writeFile(path.join(root,'qa/static.json'),JSON.stringify(report,null,2)+'\n')
console.log(JSON.stringify({status:'PASS',sha256:metadata.sha256,checks:Object.keys(checks).length,records:records.length,nonair:grounded.size}))
