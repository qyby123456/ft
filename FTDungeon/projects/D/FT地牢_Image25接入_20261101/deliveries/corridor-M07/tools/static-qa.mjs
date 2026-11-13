import assert from 'node:assert/strict'
import crypto from 'node:crypto'
import fs from 'node:fs/promises'
import path from 'node:path'
import { createRequire } from 'node:module'
import { fileURLToPath, pathToFileURL } from 'node:url'
const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const skillRequire = createRequire('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/package.json')
const { NbtFile, Structure } = await import(pathToFileURL(skillRequire.resolve('@mattzh72/lodestone')))
const reference = 'D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/M07-cataclysm-dark-iron-damaged.png'
const expectedReferenceHash = '7aa60a98ff9c54dbb9b5e61c76973619d89a107a8e906c4787e561dd08fcccfa'
const sha256 = bytes => crypto.createHash('sha256').update(bytes).digest('hex')
const referenceBytes = await fs.readFile(reference)
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const nbt = NbtFile.read(bytes).root
const raw = nbt.toSimplifiedJson()
const structure = Structure.fromNbt(nbt)
const size = structure.getSize()
const blocks = structure.getBlocks()
const digest = sha256(bytes)
const checks = []
const check = (name, callback) => { callback(); checks.push({ name, status: 'PASS' }) }
const blockAt = position => structure.getBlock(position)?.state.getName().toString()
const airAt = position => blockAt(position) === 'minecraft:air'
const solidAt = position => { const name = blockAt(position); return name && name !== 'minecraft:air' && !/(stairs|slab|lantern|chain)$/.test(name) }
const safeFeet = position => airAt(position) && airAt([position[0], 2, position[2]]) && solidAt([position[0], 0, position[2]])
const counts = {}
for (const block of blocks) { const name = block.state.getName().toString(); counts[name] = (counts[name] ?? 0) + 1 }
check('reference_full_sha256', () => assert.equal(sha256(referenceBytes), expectedReferenceHash))
check('gzip_single_palette_1_20_1', () => { assert.equal(bytes[0],31); assert.equal(bytes[1],139); assert.equal(raw.DataVersion,3465); assert.ok(raw.palette && !raw.palettes) })
check('medium_41_block_length', () => assert.deepEqual(size,[17,13,41]))
check('complete_unique_in_bounds_volume', () => { assert.equal(blocks.length,9061); assert.equal(new Set(blocks.map(block=>block.pos.join(','))).size,9061); assert.ok(blocks.every(block=>block.pos.every((value,axis)=>Number.isInteger(value)&&value>=0&&value<size[axis]))) })
check('vanilla_no_entities_block_entities_loot_or_hazards', () => { assert.equal(raw.entities.length,0); assert.ok(raw.blocks.every(block=>!block.nbt)); assert.ok(Object.keys(counts).every(name=>name.startsWith('minecraft:')&&!/(command_block|structure_block|structure_void|jigsaw|spawner|tnt|chest|barrel|hopper|dispenser|dropper|lava|water|fire|campfire|magma_block|powder_snow|cobweb|sand|gravel|anvil)$/.test(name))) })
check('full_foundation_and_double_roof', () => { for(let blockX=0;blockX<17;blockX+=1) for(let blockZ=0;blockZ<41;blockZ+=1) for(const blockY of [0,11,12]) assert.ok(solidAt([blockX,blockY,blockZ])) })
check('parallel_solid_side_walls_no_branches', () => { for(const wallX of [0,1,15,16]) for(let blockY=1;blockY<11;blockY+=1) for(let blockZ=0;blockZ<41;blockZ+=1) assert.ok(solidAt([wallX,blockY,blockZ])) })
check('matching_north_south_5x6_open_portals', () => { for(const endZ of [0,40]) for(let blockX=6;blockX<=10;blockX+=1) for(let blockY=1;blockY<=6;blockY+=1) assert.ok(airAt([blockX,blockY,endZ])) })
check('five_wide_three_high_flat_straight_route', () => { for(let blockX=6;blockX<=10;blockX+=1) for(let blockZ=0;blockZ<41;blockZ+=1) { assert.ok(safeFeet([blockX,1,blockZ])); assert.ok(airAt([blockX,3,blockZ])) } })
const visited = new Set(['8,1,0'])
const pending = [[8,1,0]]
for(let cursor=0;cursor<pending.length;cursor+=1) for(const [offsetX,offsetZ] of [[1,0],[-1,0],[0,1],[0,-1]]) { const current=pending[cursor]; const next=[current[0]+offsetX,1,current[2]+offsetZ]; if(next[0]<0||next[0]>=17||next[2]<0||next[2]>=41||visited.has(next.join(','))||!safeFeet(next)) continue; visited.add(next.join(',')); pending.push(next) }
check('bfs_entrance_interior_exit_reachable', () => { for(const target of [[8,1,0],[8,1,3],[8,1,40]]) assert.ok(visited.has(target.join(','))) })
check('seven_hanging_lanterns_fourteen_anchored_chains', () => { assert.equal(counts['minecraft:lantern'],7); assert.equal(counts['minecraft:chain'],14); for(const station of [2,8,14,20,26,32,38]) { assert.equal(blockAt([8,7,station]),'minecraft:lantern'); for(const blockY of [8,9]) assert.equal(blockAt([8,blockY,station]),'minecraft:chain'); assert.ok(solidAt([8,10,station])) } })
check('five_arch_chips_not_roof_holes', () => { for(const position of [[4,7,8],[5,8,14],[12,7,20],[11,8,26],[4,7,32]]) { assert.ok(airAt(position)); assert.ok(solidAt([position[0],11,position[2]])) } })
check('rubble_supported_outside_main_route', () => { for(const block of blocks.filter(entry=>entry.state.getName().toString().endsWith('_slab'))) { assert.ok(block.pos[0]<6||block.pos[0]>10); assert.ok(solidAt([block.pos[0],0,block.pos[2]])) } })
check('obsidian_gold_crimson_and_cracked_stone_features', () => { for(const name of ['obsidian','gold_block','gilded_blackstone','red_glazed_terracotta','red_nether_bricks','cracked_polished_blackstone_bricks']) assert.ok(counts['minecraft:'+name]>0) })
const metadata = { id:'corridor-M07', name:'灾变暗铁残拱长廊', kind:'corridor', length_class:'medium', size, entrance:[8,1,0], interior:[8,1,3], exit:[8,1,40], facing:'north', exit_facing:'south', axis:'+Z', portal:{width:5,clear_height:6,feet_y:1}, main_route:{min_width:5,min_clear_height:3,floor_y:0,stairs:false,branches:false}, sha256:digest, reference, reference_sha256:expectedReferenceHash, minecraft:'1.20.1',data_version:3465,generator:'Minecraft Builder / Lodestone 0.6.0',recipe:'recipe/corridor-M07.mjs', design:{arches:7,warm_hanging_lanterns:7,chains:14,arch_chips:5,palette:['dark iron substitute: deepslate / blackstone','obsidian','gold','crimson decorative runes']}, integration:{deployed:false,player_saves_accessed:false,distinct_from_existing_room_M07:true,requires_full_volume_occupancy_check:true} }
await fs.mkdir(path.join(root,'qa'),{recursive:true})
await fs.writeFile(path.join(root,'room.json'),JSON.stringify(metadata,null,2)+'\n')
const result = {id:metadata.id,status:'PASS',sha256:digest,size,checks,explicit_air_records:counts['minecraft:air'],recorded_cells:blocks.length,conservative_reachable_feet:visited.size,material_counts:counts,limits:['Static checks are not native collision or client gameplay evidence.']}
await fs.writeFile(path.join(root,'qa/static.json'),JSON.stringify(result,null,2)+'\n')
console.log(JSON.stringify(result,null,2))

