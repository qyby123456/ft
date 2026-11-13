import { readFile, writeFile, mkdir } from 'node:fs/promises'
import { createHash } from 'node:crypto'
import { fileURLToPath } from 'node:url'
import path from 'node:path'
import { createRequire } from 'node:module'
import { SIZE, PILLARS, LIGHTS, TARGETS, roofHole } from './recipe.mjs'
const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const require = createRequire(import.meta.url)
const { NbtFile, Structure } = require('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone')
const digest = bytes => createHash('sha256').update(bytes).digest('hex')
const sourcePath = 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/abandoned/A02-abandoned-collapsed-bedroom.png'
const referenceHash = '946dadb0601ffabfb9257ad4ef5455225a146fa6be1abcb324ddac169ee833b4'
const failures = []
const check = (condition, message) => { if (!condition) failures.push(message) }
const bytes = await readFile(path.join(root, 'room.nbt'))
const sha256 = digest(bytes)
const nbt = NbtFile.read(bytes)
const structure = Structure.fromNbt(nbt.root)
const blocks = structure.getBlocks()
const blockMap = new Map(blocks.map(block => [block.pos.join(','), block]))
const at = (posX, posY, posZ) => blockMap.get([posX, posY, posZ].join(','))
const name = block => block?.state.getName().toString()
const isAir = block => !block || name(block) === 'minecraft:air'
check(digest(await readFile(sourcePath)) === referenceHash, 'Reference hash mismatch')
const batchRoot = path.resolve(path.dirname(sourcePath), '../..')
const manifest = JSON.parse(await readFile(path.join(batchRoot, 'docs/manifest.json'), 'utf8'))
const verify = JSON.parse(await readFile(path.join(batchRoot, 'docs/verify.json'), 'utf8'))
check(manifest.files.find(entry => entry.file.endsWith('A02-abandoned-collapsed-bedroom.png'))?.sha256 === referenceHash, 'Manifest mismatch')
check(verify.records.find(entry => entry.file.endsWith('A02-abandoned-collapsed-bedroom.png'))?.sha256 === referenceHash, 'Verify mismatch')
check(JSON.stringify(structure.getSize()) === JSON.stringify(SIZE), 'Unexpected size')
check(nbt.root.getNumber('DataVersion') === 3465, 'Wrong DataVersion')
check(nbt.root.getList('entities').length === 0, 'Unexpected entities')
check(blocks.length === SIZE.reduce((total, axis) => total * axis, 1), 'Missing explicit cells')
const palette = new Map()
const entities = []
for (const block of blocks) {
  check(block.pos.every((value, axis) => Number.isInteger(value) && value >= 0 && value < SIZE[axis]), 'Out of bounds: ' + block.pos)
  check(name(block).startsWith('minecraft:'), 'Non-vanilla block')
  check(!/command_block|structure_block|structure_void|jigsaw|spawner|chest|barrel|hopper|dispenser|dropper|lava|:water$|:fire$|:sand$|gravel|anvil|pointed_dripstone/.test(name(block)), 'Forbidden block: ' + name(block))
  palette.set(name(block), (palette.get(name(block)) ?? 0) + 1)
  if (block.nbt) {
    entities.push({ position: block.pos, id: block.nbt.getString('id') })
    check(['minecraft:bed', 'minecraft:banner'].includes(block.nbt.getString('id')), 'Unexpected block entity')
    for (const field of ['Items', 'LootTable', 'Book', 'Command']) check(!block.nbt.has(field), 'Unexpected payload: ' + field)
  }
  if (/_carpet$|:candle$/.test(name(block))) check(!isAir(at(block.pos[0], block.pos[1] - 1, block.pos[2])), 'Unsupported textile or candle: ' + block.pos)
  if (name(block).endsWith('_bed')) {
    const partnerZ = block.pos[2] + (block.state.getProperty('part') === 'head' ? 1 : -1)
    check(name(at(block.pos[0], block.pos[1], partnerZ)) === name(block), 'Unpaired bed')
    check(!isAir(at(block.pos[0], 1, block.pos[2])), 'Unsupported bed')
  }
}
for (const [posX, posZ] of PILLARS) {
  for (let posY = 2; posY <= 9; posY++) check(!isAir(at(posX, posY, posZ)), 'Pillar discontinuity')
  for (const [deltaX, deltaZ] of [[-1, 0], [1, 0], [0, -1], [0, 1]]) check(isAir(at(posX + deltaX, 2, posZ + deltaZ)) && isAir(at(posX + deltaX, 3, posZ + deltaZ)), 'Pillar clearance blocked')
}
for (const [posX, posZ] of LIGHTS) {
  check(name(at(posX, 9, posZ)) === 'minecraft:dark_oak_log', 'Unanchored lamp beam')
  check(name(at(posX, 8, posZ)) === 'minecraft:chain' && name(at(posX, 7, posZ)) === 'minecraft:lantern', 'Broken hanging lamp')
}
let roofCells = 0
let holeCells = 0
for (let posX = 0; posX <= 20; posX++) {
  for (let posZ = 0; posZ <= 20; posZ++) {
    check(!isAir(at(posX, 0, posZ)) && !isAir(at(posX, 1, posZ)), 'Floor discontinuity')
    check(isAir(at(posX, 10, posZ)) === roofHole(posX, posZ), 'Roof differs from damage plan: ' + [posX, posZ])
    if (roofHole(posX, posZ)) holeCells++; else roofCells++
  }
}
for (let posX = 9; posX <= 11; posX++) {
  for (let posZ = 0; posZ <= 20; posZ++) {
    for (let posY = 2; posY <= 5; posY++) check(isAir(at(posX, posY, posZ)), 'Main route blocked')
  }
}
const walkable = (posX, posZ) => posX >= 0 && posX <= 20 && posZ >= 0 && posZ <= 20 && isAir(at(posX, 2, posZ)) && isAir(at(posX, 3, posZ)) && !isAir(at(posX, 1, posZ))
const reachable = new Set(['10,0'])
const queue = [[10, 0]]
for (let index = 0; index < queue.length; index++) {
  const [posX, posZ] = queue[index]
  for (const [deltaX, deltaZ] of [[-1, 0], [1, 0], [0, -1], [0, 1]]) {
    const nextX = posX + deltaX
    const nextZ = posZ + deltaZ
    const key = [nextX, nextZ].join(',')
    if (walkable(nextX, nextZ) && !reachable.has(key)) { reachable.add(key); queue.push([nextX, nextZ]) }
  }
}
for (const [posX, , posZ] of TARGETS) check(reachable.has([posX, posZ].join(',')), 'Unreachable landmark: ' + [posX, posZ])
const solid = new Set(blocks.filter(block => !isAir(block)).map(block => block.pos.join(',')))
const connected = new Set(blocks.filter(block => block.pos[1] === 0).map(block => block.pos.join(',')))
const supportQueue = [...connected].map(key => key.split(',').map(Number))
for (let index = 0; index < supportQueue.length; index++) {
  for (const offset of [[1, 0, 0], [-1, 0, 0], [0, 1, 0], [0, -1, 0], [0, 0, 1], [0, 0, -1]]) {
    const next = supportQueue[index].map((value, axis) => value + offset[axis])
    const key = next.join(',')
    if (solid.has(key) && !connected.has(key)) { connected.add(key); supportQueue.push(next) }
  }
}
check(connected.size === solid.size, 'Detached components: ' + (solid.size - connected.size))
const metadata = {
  id: 'zone-A02', name: 'A02 塌毁卧室', category: 'abandoned', size: SIZE, dataVersion: 3465, sha256,
  source: { image: sourcePath, sha256: referenceHash, manifest: path.join(batchRoot, 'docs/manifest.json'), verify: path.join(batchRoot, 'docs/verify.json'), reference_is_concept_art: true },
  entrance: [10, 2, 0], entrance_facing: 'north', exit: [10, 2, 20], safePoint: [10, 2, 10],
  main_route: { x: [9, 11], y_feet: 2, z: [0, 20], clear_height: 4 },
  floor_y: 1, roof_y: 10, collapse: { corner: 'northeast', opening_cells: holeCells, supported_floor_debris: true },
  pillars: PILLARS, hanging_lights: LIGHTS, landmarks: TARGETS, rewards: false, entities: 0, deployment: 'independent candidate; never installed',
}
await mkdir(path.join(root, 'qa'), { recursive: true })
await writeFile(path.join(root, 'room.json'), JSON.stringify(metadata, null, 2) + '\n')
const result = { status: failures.length ? 'FAIL' : 'PASS', sha256, source_sha256: referenceHash, source_manifest_verified: true, source_verify_verified: true, size: SIZE, records: blocks.length, solid_blocks: solid.size, palette: Object.fromEntries(palette), block_entities: entities, explicit_air: palette.get('minecraft:air'), roof_full_cells: roofCells, roof_collapse_cells: holeCells, independent_pillars: 4, hanging_lamps: 4, route_width: 3, route_clear_height: 4, route_length: 21, reachable_feet_positions: reachable.size, reachable_landmarks: TARGETS.length, foundation_connected_solids: connected.size, failures }
await writeFile(path.join(root, 'qa/static.json'), JSON.stringify(result, null, 2) + '\n')
console.log(JSON.stringify({ status: result.status, sha256, records: blocks.length, solids: solid.size, failures }))
if (failures.length) process.exitCode = 1

