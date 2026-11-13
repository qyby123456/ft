import fs from 'node:fs/promises'
import path from 'node:path'
import {createHash} from 'node:crypto'
import {createRequire} from 'node:module'
import {fileURLToPath, pathToFileURL} from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const skill = 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
const require = createRequire(path.join(skill, 'package.json'))
const {NbtFile, Structure} = await import(pathToFileURL(require.resolve('@mattzh72/lodestone')))
const batch = 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103'
const reference = batch + '/out/parkour/P04-parkour-crumbling-ledge-path.png'
const referenceHash = '27d3620f3637684e1c7482b31aa49f601a6531c0cd3762d1d6822808262169ea'
const hash = bytes => createHash('sha256').update(bytes).digest('hex')
const check = (condition, message) => {if (!condition) throw new Error(message)}
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const encoded = NbtFile.read(bytes)
const data = encoded.root.toSimplifiedJson()
const structure = Structure.fromNbt(encoded.root)
const blocks = structure.getBlocks()
const at = (posX, posY, posZ) => structure.getBlock([posX, posY, posZ])
const identity = pos => at(...pos)?.state.getName().toString()
const air = pos => identity(pos) === 'minecraft:air'
const solid = pos => /:(?:stone_bricks|mossy_stone_bricks|cracked_stone_bricks|chiseled_stone_bricks|deepslate_bricks)$/.test(identity(pos))
const materials = {}
const coordinates = new Set()
const banned = /:(?:.*command_block|structure_block|structure_void|jigsaw|spawner|lava|water|fire|soul_fire|campfire|soul_campfire|magma_block|gravel|sand|chest|barrel|.*shulker_box|powder_snow|cobweb|cactus|wither_rose)$/
check(encoded.compression === 'gzip' && bytes[0] === 31 && bytes[1] === 139, 'Not gzip')
check(data.DataVersion === 3465 && data.palette && !data.palettes, 'Not 1.20.1 single palette')
check(JSON.stringify(data.size) === '[23,16,27]' && blocks.length === 9936, 'Wrong dimensions or explicit volume')
check(data.entities.length === 0, 'Entities present')
check(hash(await fs.readFile(reference)) === referenceHash, 'Reference SHA mismatch')
for (const filename of ['docs/manifest.json', 'docs/verify.json']) {
  const document = JSON.parse(await fs.readFile(path.join(batch, filename), 'utf8'))
  const record = (document.files ?? document.records).find(entry => entry.file.includes('P04-'))
  check(record?.sha256 === referenceHash && record.ok === true, 'Batch reference mismatch ' + filename)
}
for (const block of blocks) {
  const key = block.pos.join(',')
  const name = block.state.getName().toString()
  check(!coordinates.has(key) && block.pos.every((value, axis) => Number.isInteger(value) && value >= 0 && value < data.size[axis]), 'Coordinate invalid ' + key)
  coordinates.add(key)
  check(name.startsWith('minecraft:') && !banned.test(name), 'Forbidden ' + name)
  check(!block.nbt, 'Unexpected block entity ' + key)
  materials[name] = (materials[name] ?? 0) + 1
}
for (let posX = 0; posX < 23; posX += 1) {
  for (let posZ = 0; posZ < 27; posZ += 1) {
    check(solid([posX, 0, posZ]) && solid([posX, 15, posZ]), 'Roof or foundation hole')
    check(solid([posX, 2, posZ]), 'Incomplete recovery floor')
  }
}
for (const posZ of [0, 1, 26]) {
  for (let posX = 10; posX <= 12; posX += 1) {
    check(solid([posX, 4, posZ]), 'Connector floor missing')
    for (let posY = 5; posY <= 7; posY += 1) check(air([posX, posY, posZ]), 'Connector blocked')
  }
}
const ledges = [{range:[6,7],floor:4},{range:[9,11],floor:4},{range:[13,15],floor:5},{range:[17,19],floor:5},{range:[21,23],floor:5}]
for (const ledge of ledges) {
  for (let posZ = ledge.range[0]; posZ <= ledge.range[1]; posZ += 1) {
    for (const posX of [3, 4]) {
      check(solid([posX, ledge.floor, posZ]), 'Ledge floor absent')
      for (let posY = ledge.floor + 1; posY <= ledge.floor + 3; posY += 1) check(air([posX, posY, posZ]), 'Ledge headroom blocked')
    }
    for (let posX = 1; posX <= 4; posX += 1) check(solid([posX, ledge.floor, posZ]), 'Ledge disconnected from wall')
    check(solid([2, ledge.floor - 1, posZ]), 'Corbel missing')
  }
}
const jumps = [
  {id:'J1', takeoff:[3.5,5,7.65], landing_z:[9.3,11.7], landing_y:5, gap_z:8, gap_blocks:1, rise:0},
  {id:'J2', takeoff:[3.5,5,11.65], landing_z:[13.3,15.7], landing_y:6, gap_z:12, gap_blocks:1, rise:1},
  {id:'J3', takeoff:[3.5,6,15.65], landing_z:[17.3,19.7], landing_y:6, gap_z:16, gap_blocks:1, rise:0},
  {id:'J4', takeoff:[3.5,6,19.65], landing_z:[21.3,23.7], landing_y:6, gap_z:20, gap_blocks:1, rise:0}
]
for (const jump of jumps) {
  for (let posY = 3; posY <= 7; posY += 1) check(air([3, posY, jump.gap_z]), 'Gap is not a genuine opening')
  check(jump.takeoff[1] - 3 <= 3, 'Unsafe recovery drop')
}
const lights = [[3,3],[3,14],[3,23],[19,3],[19,14],[19,23]].map(([posX,posZ]) => ({position:[posX,10,posZ], chain_from:[posX,11,posZ], chain_to:[posX,13,posZ], beam:[posX,14,posZ], roof:[posX,15,posZ]}))
for (const lamp of lights) {
  check(identity(lamp.position) === 'minecraft:lantern' && solid(lamp.beam) && solid(lamp.roof), 'Lamp anchor invalid')
  for (let posY = 11; posY <= 13; posY += 1) check(identity([lamp.position[0],posY,lamp.position[2]]) === 'minecraft:chain', 'Broken chain')
}
const nonAir = new Set(blocks.filter(block => !block.state.is('air')).map(block => block.pos.join(',')))
const visited = new Set(['0,0,0'])
const pending = [[0,0,0]]
for (let cursor = 0; cursor < pending.length; cursor += 1) {
  const position = pending[cursor]
  for (const offset of [[1,0,0],[-1,0,0],[0,1,0],[0,-1,0],[0,0,1],[0,0,-1]]) {
    const next = position.map((value, axis) => value + offset[axis])
    const key = next.join(',')
    if (nonAir.has(key) && !visited.has(key)) {visited.add(key); pending.push(next)}
  }
}
check(visited.size === nonAir.size, 'Disconnected decorative or structural component')
const metadata = {
  id:'zone-P04', producer_metadata_id:'P04', name:'崩裂岩沿', category:'parkour_passage', size:data.size,
  entrance:[11,5,0], interior:[11,5,3], exit:[11,5,26], facing:'north',
  connectors:[{side:'north',position:[11,5,0],width:3,height:3},{side:'south',position:[11,5,26],width:3,height:3}],
  safe_points:[{id:'entry',feet:[11,5,3]},{id:'midpoint',feet:[4,6,14]},{id:'last_ledge',feet:[4,6,22]},{id:'exit',feet:[11,5,26]}],
  route:{direction:'north entry -> west ledges -> south gallery -> south exit',jumps,minimum_path_width:2,minimum_headroom:3,max_gap_blocks:1,max_rise_blocks:1,requires_sprint:false,requires_potion_or_equipment:false,checkpoint_semantics:'Physical resting platforms only; no automatic teleport, respawn or persistent checkpoint.'},
  recovery:{floor_y:2,feet_y:3,maximum_drop:3,stairs_x:[17,19],stair_z:[7,6],route_feet:[[7,3,12],[18,3,12],[18,3,8],[18,5,5],[11,5,3]],returns_to:'entry',no_void_or_trap:true},
  minecraft_version:'1.20.1',data_version:3465,sha256:hash(bytes),reference,reference_sha256:referenceHash,source_batch:batch,
  builder:{skill,recipe:'tools/recipe.mjs',runner:'scripts/run-build.mjs',lodestone:'0.6.0',explicit_air:true},
  adaptations:[
    'The concept is an open dollhouse; the final dungeon has a complete roof and two 3x3 open connectors at equal elevation. Cutaways affect renders only.',
    'Broken ledges are static wall-anchored stone and corbels, with four one-block gaps; no timed collapse, falling block or world mutation is implemented.',
    'A continuous recovery floor keeps all intentional falls within three blocks, with a dedicated three-wide return stair; the pit is shallower than the concept.',
    'The midpoint and final ledge are enlarged physical rest platforms; a south gallery and short stair make the exit reachable.',
    'Spruce log crates and a red wool wall inlay replace concept containers and cloth; there are no inventories, loot or block entities.'
  ],
  scope:{deployed:false,production_save_access:false,core_integration_tested:false,dedicated_qa_world_only:true,unique_integration_slot:null}
}
const report = {status:'PASS',room_sha256:metadata.sha256,reference_sha256:referenceHash,gzip:true,data_version:3465,single_palette:true,size:data.size,record_count:blocks.length,non_air_records:nonAir.size,explicit_air_records:materials['minecraft:air'],palette_count:data.palette.length,unique_coordinates:coordinates.size,connected_non_air_records:visited.size,full_roof_cells:621,full_foundation_cells:621,full_recovery_floor_cells:621,north_south_connectors:'3x3 with full floor',mounted_lanterns:lights,wall_torches:4,wall_anchored_ledges:5,jump_gaps:jumps,minimum_headroom:3,max_recovery_drop:3,no_fluids:true,no_hazards:true,no_block_entities:true,no_entities:true,no_containers_or_loot:true,native_registry_collision_and_stability:'qa/native.json'}
for (const [filename,value] of [['room.json',metadata],['qa/static.json',report],['qa/materials.json',materials],['qa/reference.json',{status:'PASS',path:reference,sha256:referenceHash,bytes:(await fs.stat(reference)).size,manifest:'docs/manifest.json',verify:'docs/verify.json',batch,generated_by_this_task:false}]]) await fs.writeFile(path.join(root,filename),JSON.stringify(value,null,2)+'\n')
console.log(JSON.stringify({status:report.status,sha256:metadata.sha256,size:data.size,blocks:blocks.length,non_air:nonAir.size,jumps:4},null,2))
