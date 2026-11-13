import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath, pathToFileURL } from 'node:url'
import { createRequire } from 'node:module'
import { createHash } from 'node:crypto'
import { design } from './recipe.mjs'

const require = createRequire('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/package.json')
const { NbtFile, Structure } = await import(pathToFileURL(require.resolve('@mattzh72/lodestone')).href)
const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const digest = bytes => createHash('sha256').update(bytes).digest('hex')
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const data = NbtFile.read(bytes).root
const structure = Structure.fromNbt(data)
const blocks = structure.getBlocks()
const at = pos => structure.getBlock(pos)?.state.getName().toString() ?? 'minecraft:air'
const checks = {}
const check = (name, passed) => { checks[name] = passed ? 'PASS' : 'FAIL'; if (!passed) throw new Error(name) }
const expectedHash = design.reference_sha256
check('reference_sha256', digest(await fs.readFile(design.reference)) === expectedHash)
const batch = path.resolve(path.dirname(design.reference), '../..')
const manifest = JSON.parse(await fs.readFile(path.join(batch, 'docs/manifest.json'), 'utf8'))
const verify = JSON.parse(await fs.readFile(path.join(batch, 'docs/verify.json'), 'utf8'))
check('batch_manifest_match', manifest.files.find(record => record.file.includes('R04-')).sha256 === expectedHash)
check('batch_verify_match', verify.records.find(record => record.file.includes('R04-')).sha256 === expectedHash)
check('gzip', bytes[0] === 31 && bytes[1] === 139)
check('java_1_20_1', data.getNumber('DataVersion') === 3465)
check('dimensions', JSON.stringify(structure.getSize()) === JSON.stringify(design.size))
check('single_palette', !data.has('palettes'))
check('no_entities', data.getList('entities').length === 0)
check('full_explicit_volume', blocks.length === 25 * 13 * 25)
check('unique_positions', new Set(blocks.map(block => block.pos.join(','))).size === blocks.length)
check('all_inside_bounds', blocks.every(block => block.pos.every((value, axis) => value >= 0 && value < design.size[axis])))
const forbidden = new Set(['fire', 'soul_fire', 'lava', 'water', 'magma_block', 'campfire', 'soul_campfire', 'cactus', 'powder_snow', 'cobweb', 'tnt', 'spawner', 'command_block', 'chain_command_block', 'repeating_command_block', 'structure_block', 'structure_void', 'jigsaw'])
check('vanilla_no_hazards', blocks.every(block => at(block.pos).startsWith('minecraft:') && !forbidden.has(at(block.pos).split(':')[1])))
check('no_inventory_block_entities_loot', blocks.every(block => block.nbt === undefined))
for (const posY of [0, 1, 11]) check('continuous_foundation_floor_roof_' + posY, Array.from({ length: 625 }, (_, index) => at([index % 25, posY, Math.floor(index / 25)]) !== 'minecraft:air').every(Boolean))
check('safe_route_three_wide_three_high', blocks.filter(block => block.pos[0] >= 11 && block.pos[0] <= 13 && block.pos[1] >= 2 && block.pos[1] <= 4).every(block => at(block.pos) === 'minecraft:air'))
for (const [posX, posZ] of design.pillars) {
  check(`pillar_${posX}_${posZ}`, Array.from({ length: 8 }, (_, index) => [posX, posX + 1].every(coreX => [posZ, posZ + 1].every(coreZ => at([coreX, index + 2, coreZ]) !== 'minecraft:air'))).every(Boolean))
  check(`pillar_roof_contact_${posX}_${posZ}`, at([posX, 10, posZ]) === 'minecraft:dark_oak_log')
}
for (const bunk of design.bunks) {
  for (const posX of [bunk.minX, bunk.maxX]) for (const posZ of [bunk.minZ, bunk.maxZ]) check(`bedpost_${posX}_${posZ}`, Array.from({ length: 6 }, (_, index) => at([posX, index + 2, posZ]) === 'minecraft:dark_oak_log').every(Boolean))
  for (const posY of [2, 5]) check(`bunk_deck_${bunk.minX}_${posY}`, Array.from({ length: 5 }, (_, index) => [bunk.minX, bunk.minX + 1, bunk.maxX].every(posX => at([posX, posY, index + 10]) === 'minecraft:dark_oak_slab')).every(Boolean))
  for (let posY = 2; posY <= 6; posY++) check(`ladder_backing_${bunk.minX}_${posY}`, at([bunk.ladder[0], posY, bunk.ladder[1]]) === 'minecraft:ladder' && at([bunk.ladder[2] === 'east' ? bunk.ladder[0] - 1 : bunk.ladder[0] + 1, posY, bunk.ladder[1]]) === 'minecraft:dark_oak_log')
}
for (const [posX, posY, posZ] of design.fallen) check(`fallen_beam_floor_load_${posX}_${posZ}`, at([posX, posY, posZ]) === 'minecraft:dark_oak_log' && Array.from({ length: posY }, (_, index) => at([posX, index, posZ]) !== 'minecraft:air').every(Boolean))
for (const [posX, posZ] of design.hanging) check(`anchored_lantern_${posX}_${posZ}`, at([posX, 7, posZ]) === 'minecraft:lantern' && [8, 9].every(posY => at([posX, posY, posZ]) === 'minecraft:chain') && at([posX, 10, posZ]) === 'minecraft:dark_oak_log')
for (const block of blocks.filter(block => at(block.pos).endsWith('_carpet'))) check('carpet_support_' + block.pos.join('_'), at([block.pos[0], block.pos[1] - 1, block.pos[2]]) !== 'minecraft:air')
const walkable = pos => at(pos) === 'minecraft:air' && at([pos[0], pos[1] + 1, pos[2]]) === 'minecraft:air' && at([pos[0], pos[1] - 1, pos[2]]) !== 'minecraft:air'
const reached = new Set([design.entrance.join(',')])
const pending = [design.entrance]
while (pending.length) {
  const current = pending.shift()
  for (const [offsetX, offsetZ] of [[1, 0], [-1, 0], [0, 1], [0, -1]]) {
    const next = [current[0] + offsetX, 2, current[2] + offsetZ]
    if (next[0] < 0 || next[0] > 24 || next[2] < 0 || next[2] > 24 || reached.has(next.join(',')) || !walkable(next)) continue
    reached.add(next.join(',')); pending.push(next)
  }
}
check('entrance_safe_point_exit_bunk_aisles_reachable', [design.entrance, design.interior, design.exit, [12, 2, 12], [5, 2, 12], [19, 2, 12]].every(pos => reached.has(pos.join(','))))
const materials = {}
for (const block of blocks) materials[at(block.pos)] = (materials[at(block.pos)] ?? 0) + 1
check('theme_bunk_lights_skylight', materials['minecraft:lantern'] === 6 && materials['minecraft:chain'] === 8 && materials['minecraft:light_blue_stained_glass'] === 40)
const sha256 = digest(bytes)
const metadata = { ...design, source_id: 'R04', kind: 'ruins', facing: 'north', sha256, data_version: 3465, axis: 'z', walkable_width: 3, walkable_height: 3, safe_points: [design.interior, [12, 2, 12]], connections: [{ side: 'north', feet: design.entrance, aperture: [3, 4] }, { side: 'south', feet: design.exit, aperture: [3, 4] }], roof: { sealed: true, surface_y: 11, glass_skylights: 2, columns: 4 }, features: { bunk_frames: 2, sleeping_decks: 4, fallen_beams: 2, lanterns: 6, cosmetic_embers: 2 }, safety: { real_fire: false, fluids: false, damage_blocks: false, entities: false, block_entities: false, loot: false }, reference_adaptations: ['破损屋顶两处由冷色整块玻璃封口，保留焦木梁与封闭石质顶盖。', '双层床为落地立柱、上下床板和破布构成的静态装饰，无可睡眠床和物品实体。', '斜落梁使用阶梯式体素并逐段垫实至地面；避开三格宽中央通路。', '灰堆、骨骸、铜盔和焦旗为原版静态方块的象征性复刻，无库存或掉落实体。'], integration: { deployed: false, production_save_modified: false, slot_assigned: false } }
await fs.writeFile(path.join(root, 'room.json'), JSON.stringify(metadata, null, 2) + '\n')
await fs.writeFile(path.join(root, 'qa/static.json'), JSON.stringify({ status: 'PASS', sha256, reference_sha256: expectedHash, batch_manifest_sha256: digest(await fs.readFile(path.join(batch, 'docs/manifest.json'))), batch_verify_sha256: digest(await fs.readFile(path.join(batch, 'docs/verify.json'))), checks, records: blocks.length, non_air: blocks.length - materials['minecraft:air'], palette_count: data.getList('palette').length, reachable_feet: reached.size, materials }, null, 2) + '\n')
process.stdout.write(JSON.stringify({ status: 'PASS', checks: Object.keys(checks).length, size: design.size, sha256, reached: reached.size }) + '\n')
