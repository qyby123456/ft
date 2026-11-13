import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createHash } from 'node:crypto'
import { NbtFile, Structure } from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const file = NbtFile.read(bytes)
const structure = Structure.fromNbt(file.root)
const blocks = structure.getBlocks()
const hash = createHash('sha256').update(bytes).digest('hex')
const reference = 'D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/M06-frozen-prison-burnt.png'
const referenceHash = createHash('sha256').update(await fs.readFile(reference)).digest('hex')
const expectedReference = '3a7bc9bffe8a727ca0b05ca07b492fe1623ac2c0e82c26ada0867b144d19eadf'
const checks = []
const check = (name, success, details) => {
  checks.push({ name, status: success ? 'PASS' : 'FAIL', details })
  if (!success) throw new Error(name)
}
const get = position => structure.getBlock(position)?.state.getName().toString()
const isAir = position => get(position) === 'minecraft:air'
const isFull = position => !['minecraft:air', 'minecraft:iron_bars', 'minecraft:chain', 'minecraft:candle'].includes(get(position)) && !get(position)?.endsWith('_stairs') && !get(position)?.endsWith('_slab')
const materials = {}
for (const block of blocks) {
  const name = block.state.getName().toString()
  materials[name] = (materials[name] ?? 0) + 1
}
check('reference_sha256', referenceHash === expectedReference, referenceHash)
check('gzip', bytes[0] === 31 && bytes[1] === 139)
check('size', JSON.stringify(structure.getSize()) === '[15,13,40]', structure.getSize())
check('dense_unique_in_bounds', blocks.length === 7800 && new Set(blocks.map(block => block.pos.join(','))).size === 7800 && blocks.every(block => structure.isInside(block.pos)), blocks.length)
check('vanilla_only', Object.keys(materials).every(name => name.startsWith('minecraft:')))
check('no_entities_or_block_entities', file.root.getList('entities').length === 0 && blocks.every(block => !block.nbt))
check('no_hazards_or_unstable_ice', !Object.keys(materials).some(name => /(?:^minecraft:(water|lava|fire|soul_fire|ice|frosted_ice|magma_block|campfire|soul_campfire|powder_snow|cobweb|spawner|jigsaw|structure_block|command_block|sand|gravel)$)/.test(name)))
for (let across = 0; across < 15; across++) {
  for (let depth = 0; depth < 40; depth++) {
    if (!isFull([across, 0, depth]) || !isFull([across, 12, depth])) throw new Error('Base or roof hole')
  }
}
check('complete_base_and_roof', true, { foundation: 600, roof: 600 })
for (let depth = 0; depth < 40; depth++) {
  for (let across = 6; across <= 8; across++) {
    if (!isFull([across, 1, depth]) || get([across, 1, depth]).includes('ice')) throw new Error('Main route floor')
    for (let height = 2; height <= 4; height++) if (!isAir([across, height, depth])) throw new Error('Main route obstruction')
  }
}
check('straight_three_wide_three_high_both_ports', true, { axis: 'z', length: 40, feetY: 2, pathCells: 120, entrance: [7, 2, 0], exit: [7, 2, 39] })
const candles = blocks.filter(block => block.state.getName().toString() === 'minecraft:candle')
check('candle_supports', candles.length === 10 && candles.every(block => isFull([block.pos[0], block.pos[1] - 1, block.pos[2]])), candles.length)
check('six_cages_anchored', [5, 15, 27].every(depth => [5, 9].every(across => get([across, 10, depth]) === 'minecraft:chain' && isFull([across, 11, depth]) && get([across, 9, depth]) === 'minecraft:deepslate_tile_slab')), 6)
check('frozen_prison_readability', materials['minecraft:iron_bars'] > 250 && materials['minecraft:packed_ice'] > 300 && materials['minecraft:blue_ice'] > 20)
check('burnt_scenic_patches', materials['minecraft:blackstone'] > 60 && materials['minecraft:coal_block'] >= 12 && materials['minecraft:shroomlight'] === 4)
const metadata = {
  id: 'corridor-M06', source_id: 'M06', name: '焚痕冰封牢狱直廊',
  size: structure.getSize(), entrance: [7, 2, 0], interior: [7, 2, 4], exit: [7, 2, 39], facing: 'north',
  ports: [{ face: 'north', feet: [7, 2, 0], width: 3, height: 3 }, { face: 'south', feet: [7, 2, 39], width: 3, height: 3 }],
  sha256: hash, reference, reference_sha256: referenceHash,
  minecraft: '1.20.1', data_version: 3465, kind: 'medium', corridor_length_blocks: 40, corridor_length_chunks: 2.5,
  theme: 'frozen-prison', condition: 'burnt', light: 'candles', feature: 'hanging-cages',
  source_batch: 'D:/ft工程文件/image2.5/dungeon-corridors-20261103',
  iron_bar_connections: 'Explicit native-compatible connections; generic Lodestone auto connections disabled to avoid attaching to candles and partial slabs.',
  recipe: 'recipe.mjs', production_deployed: false, production_save_modified: false,
  adaptations: ['Packed/blue ice replaces melting ice.', 'Four recessed shroomlights suggest embers without fire/lava.', 'Six empty block-built cages; no prisoners, mobs, rewards, or entities.', 'Full roof retained; cutaways affect rendering only.'],
}
await fs.mkdir(path.join(root, 'qa'), { recursive: true })
await fs.writeFile(path.join(root, 'room.json'), JSON.stringify(metadata, null, 2) + '\n')
await fs.writeFile(path.join(root, 'qa/static.json'), JSON.stringify({ status: 'PASS', sha256: hash, reference_sha256: referenceHash, checks, records: blocks.length, materials }, null, 2) + '\n')
console.log(JSON.stringify({ status: 'PASS', sha256: hash, checks: checks.length, records: blocks.length, materials }, null, 2))
