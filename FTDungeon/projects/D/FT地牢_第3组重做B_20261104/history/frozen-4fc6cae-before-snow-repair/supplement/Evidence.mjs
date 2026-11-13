import fs from 'node:fs/promises'
import path from 'node:path'
import crypto from 'node:crypto'
import { fileURLToPath } from 'node:url'
import { NbtFile, Structure } from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'

const destination = path.dirname(fileURLToPath(import.meta.url))
const root = path.dirname(destination)
const hash = bytes => crypto.createHash('sha256').update(bytes).digest('hex')
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const sha256 = hash(bytes)
if (sha256 !== '4fc6cae96b2e9fe1ba51060d11456a56ab1e2aab50232daf8e7b89bc10a160af') throw new Error('Frozen NBT changed')
const structure = Structure.fromNbt(NbtFile.read(bytes).root)
const blocks = structure.getBlocks()
const grid = new Map(blocks.map(block => [block.pos.join(','), block.state.getName().toString()]))
const get = pos => grid.get(pos.join(','))
const metadata = JSON.parse(await fs.readFile(path.join(root, 'room.json'), 'utf8'))
const targets = [
  { id: 'north', role: 'connection', pos: [17, 2, 0] },
  { id: 'west', role: 'connection', pos: [0, 2, 13] },
  { id: 'east', role: 'connection', pos: [34, 2, 13] },
  { id: 'interior-safe', role: 'safe-point', pos: [17, 2, 6] },
  { id: 'ring-north', role: 'ring', pos: [17, 2, 10] },
  { id: 'ring-west', role: 'ring', pos: [8, 2, 18] },
  { id: 'ring-south', role: 'ring', pos: [17, 2, 28] },
  { id: 'ring-east', role: 'ring', pos: [26, 2, 18] },
  { id: 'altar-approach', role: 'altar-view-not-summit', pos: [17, 2, 26] },
]
const boundary = []
for (const block of blocks) {
  const [x, y, z] = block.pos
  if (y !== 2 || (x !== 0 && x !== 34 && z !== 0 && z !== 34)) continue
  if (block.state.getName().toString() === 'minecraft:air' && get([x, 3, z]) === 'minecraft:air') boundary.push(block.pos)
}
const routeData = { sha256, coordinate_system: '+X east, +Y up, +Z south; integer player feet; origin [0,0,0]', connections: metadata.connections, boundary_air_feet_candidates: boundary, start: 'north', targets, ring_order: ['ring-north', 'ring-west', 'ring-south', 'ring-east', 'ring-north'], altar: { base_footprint_box: [[10, 2, 12], [24, 5, 25]], geometric_center: [17, 5, 18], main_crystal_tip: [17, 14, 18], approach_target: 'altar-approach', summit_route: 'NOT_DESIGNED_OR_CLAIMED; no reward/container/interaction target on summit' } }
await fs.writeFile(path.join(destination, 'route-targets.json'), JSON.stringify(routeData, null, 2))
const cutoffBytes = await fs.readFile(path.join(root, 'preview-only/preview-only-cutaway.nbt'))
const cutaway = Structure.fromNbt(NbtFile.read(cutoffBytes).root)
const expectedCut = pos => {
  const [x, y, z] = pos
  return y >= 20 || (y >= 15 && x > 4 && z > 4) || (y >= 3 && (x >= 30 || z >= 30))
}
const cutGrid = new Map(cutaway.getBlocks().map(block => [block.pos.join(','), block.state.toString()]))
const cutMatches = blocks.every(block => cutGrid.get(block.pos.join(',')) === (expectedCut(block.pos) ? 'minecraft:air' : block.state.toString()))
if (!cutMatches) throw new Error('Cutaway not exact documented projection of final NBT')
const views = []
for (const type of ['exterior', 'interior']) for (let index = 1; index <= 4; index++) {
  const filename = `${type}-0${index}.png`
  const yaw = 0.82 + (index - 1) * 251.327 / 160
  views.push({ file: path.join(root, filename), sha256: hash(await fs.readFile(path.join(root, filename))), source_nbt: path.join(root, type === 'exterior' ? 'room.nbt' : 'preview-only/preview-only-cutaway.nbt'), source_sha256: type === 'exterior' ? sha256 : hash(cutoffBytes), original_nbt_sha256: sha256, camera_yaw_radians: yaw, pitch_radians: 0.62, camera_quadrant: ['southwest', 'northwest', 'northeast', 'southeast'][index - 1], looking_toward: ['northeast', 'southeast', 'southwest', 'northwest'][index - 1], roof: type === 'exterior' ? 'complete final NBT, elevated camera sees roof' : 'derived cutaway; removed roof and south/east walls, never import' })
}
const roof = []
for (let x = 0; x < 35; x++) for (let z = 0; z < 35; z++) {
  const solids = blocks.filter(block => block.pos[0] === x && block.pos[2] === z && block.pos[1] >= 15 && block.state.getName().toString() !== 'minecraft:air').map(block => block.pos[1])
  roof.push({ x, z, roof_solid_y: solids })
}
await fs.writeFile(path.join(destination, 'view-map.json'), JSON.stringify({ sha256, cutaway_sha256: hash(cutoffBytes), exact_final_to_cutaway_projection: cutMatches, camera_derivation: 'OrbitCamera inverse view: x=-sin(yaw)*cos(pitch)*distance, z=cos(yaw)*cos(pitch)*distance; capture drag +251.327 px /160 rad per view. Diagonal four quadrants, not cardinal orthographic views.', views }, null, 2))
await fs.writeFile(path.join(destination, 'roof-columns.json'), JSON.stringify({ sha256, complete: roof.every(column => column.roof_solid_y.length > 0), columns: roof }, null, 2))
const counts = {}
for (const block of blocks) { const name = block.state.getName().toString(); counts[name] = (counts[name] || 0) + 1 }
await fs.writeFile(path.join(destination, 'material-counts.json'), JSON.stringify({ sha256, counts, ordinary_ice: counts['minecraft:ice'] || 0, snow_layers: counts['minecraft:snow'] || 0, snow_blocks: counts['minecraft:snow_block'] || 0, ticks: 'NOT_RUN' }, null, 2))
const forbidden = new Set(['minecraft:ice', 'minecraft:packed_ice', 'minecraft:barrier'])
const forbiddenSupports = blocks.filter(block => block.state.getName().toString() === 'minecraft:snow' && forbidden.has(get([block.pos[0], block.pos[1] - 1, block.pos[2]]))).map(block => ({ pos: block.pos, support: get([block.pos[0], block.pos[1] - 1, block.pos[2]]) }))
await fs.writeFile(path.join(destination, 'snow-support-risk.json'), JSON.stringify({ sha256, method: 'Actual vanilla 1.20.1 snow_layer_cannot_survive_on tag resource joined to actual final NBT supports; not unbound Bootstrap tag evaluation', count: forbiddenSupports.length, positions: forbiddenSupports, expected_on_neighbor_update: 'SnowLayerBlock.updateShape removes unsupported snow; world ticks NOT_RUN', frozen_nbt_modified: false }, null, 2))
console.log(JSON.stringify({ sha256, boundary_candidates: boundary.length, exact_final_to_cutaway_projection: cutMatches, view_count: views.length, roof_columns: roof.length }))
