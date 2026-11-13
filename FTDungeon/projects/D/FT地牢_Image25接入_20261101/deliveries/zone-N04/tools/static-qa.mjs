import fs from 'node:fs/promises'
import path from 'node:path'
import { createHash } from 'node:crypto'
import { createRequire } from 'node:module'
import { fileURLToPath, pathToFileURL } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const require = createRequire('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/package.json')
const { NbtFile, Structure } = await import(pathToFileURL(require.resolve('@mattzh72/lodestone')).href)
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const nbt = NbtFile.read(bytes).root
const structure = Structure.fromNbt(nbt)
const blocks = structure.getBlocks()
const hash = buffer => createHash('sha256').update(buffer).digest('hex')
const sha256 = hash(bytes)
const reference = 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/hub/N04-hub-portalfinder-hub.png'
const referenceSha = hash(await fs.readFile(reference))
const errors = []
const check = (condition, message) => { if (!condition) errors.push(message) }
const at = position => structure.getBlock(position)?.state.getName().toString() ?? 'minecraft:air'
const full = position => !/:(air|chain|lantern)$/.test(at(position))
const air = position => at(position) === 'minecraft:air'
const counts = {}
const occupied = new Set()
const forbidden = /portal|command_block|structure_block|structure_void|jigsaw|spawner|tnt|lava|water$|fire|cobweb|powder_snow|magma|campfire|chest|barrel|hopper|dropper|dispenser|bedrock/
check(referenceSha === '81f17850087057a9a0c1925f1fe747220c5a8e163003799c55fa5a97a1b756c3', 'Reference hash mismatch')
const manifest = JSON.parse(await fs.readFile('D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/docs/manifest.json', 'utf8'))
const verify = JSON.parse(await fs.readFile('D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/docs/verify.json', 'utf8'))
for (const [document, records] of [['manifest', manifest.files], ['verify', verify.records]]) {
  check(records.some(record => record.file === 'out/hub/N04-hub-portalfinder-hub.png' && record.sha256 === referenceSha && record.ok === true), document + ' source mismatch')
}
check(JSON.stringify(structure.getSize()) === '[35,16,35]', 'Dimensions mismatch')
check(blocks.length === 19600 && nbt.getNumber('DataVersion') === 3465, 'Invalid volume/version')
check(nbt.getList('entities').length === 0, 'Unexpected entities')
let hanging = 0
let standing = 0
let chains = 0
for (const block of blocks) {
  const position = block.pos
  const name = at(position)
  const properties = block.state.getProperties()
  const key = position.join(',')
  check(!occupied.has(key), 'Duplicate ' + key)
  occupied.add(key)
  check(position.every((value, axis) => value >= 0 && value < structure.getSize()[axis]), 'Bounds ' + key)
  check(name.startsWith('minecraft:') && !forbidden.test(name), 'Forbidden ' + name)
  check(!block.nbt, 'Unexpected block entity at ' + key)
  counts[name] = (counts[name] ?? 0) + 1
  if (name === 'minecraft:lantern') {
    check(properties.waterlogged === 'false', 'Waterlogged lantern')
    if (properties.hanging === 'true') {
      hanging++
      check(at([position[0], position[1] + 1, position[2]]) === 'minecraft:chain', 'Unhung lantern ' + key)
    } else {
      standing++
      check(full([position[0], position[1] - 1, position[2]]), 'Unsupported standing lamp ' + key)
    }
  }
  if (name === 'minecraft:chain') {
    chains++
    let supportY = position[1] + 1
    while (at([position[0], supportY, position[2]]) === 'minecraft:chain') supportY++
    check(full([position[0], supportY, position[2]]), 'Unanchored chain ' + key)
  }
}
for (let blockX = 0; blockX < 35; blockX++) {
  for (let blockZ = 0; blockZ < 35; blockZ++) {
    check(full([blockX, 0, blockZ]) && full([blockX, 14, blockZ]) && full([blockX, 15, blockZ]), 'Foundation or roof gap')
    if ((blockX >= 15 && blockX <= 19) || (blockZ >= 15 && blockZ <= 19)) {
      check(full([blockX, 1, blockZ]), 'Cross-route floor gap')
      for (let blockY = 2; blockY <= 6; blockY++) check(air([blockX, blockY, blockZ]), 'Blocked five-wide/five-high cross at ' + [blockX, blockY, blockZ])
    }
  }
}
const openings = [{ side: 'north', feet: [17, 2, 0], outward: [0, 0, -1], material: 'amethyst / blackstone' },
  { side: 'east', feet: [34, 2, 17], outward: [1, 0, 0], material: 'deepslate / blackstone' },
  { side: 'south', feet: [17, 2, 34], outward: [0, 0, 1], material: 'waxed oxidized copper / red bricks' },
  { side: 'west', feet: [0, 2, 17], outward: [-1, 0, 0], material: 'sandstone' }].map(port => ({ ...port, width: 5, clear_height: 5, floor_y: 1 }))
const safe = [17, 2, 17]
const reached = new Set(['17,2,0'])
const pending = [[17, 2, 0]]
for (let index = 0; index < pending.length; index++) {
  const position = pending[index]
  for (const [offsetX, offsetZ] of [[-1, 0], [1, 0], [0, -1], [0, 1]]) {
    const next = [position[0] + offsetX, 2, position[2] + offsetZ]
    if (next[0] < 0 || next[0] > 34 || next[2] < 0 || next[2] > 34 || reached.has(next.join(','))) continue
    if (full([next[0], 1, next[2]]) && [2, 3, 4].every(blockY => air([next[0], blockY, next[2]]))) {
      reached.add(next.join(','))
      pending.push(next)
    }
  }
}
for (const port of openings) check(reached.has(port.feet.join(',')), 'Unreachable ' + port.side)
for (const safePoint of [safe, [17, 2, 6], [6, 2, 17], [28, 2, 17], [17, 2, 28]]) check(reached.has(safePoint.join(',')), 'Unreachable safe point')
check(hanging === 8 && standing === 4 && chains === 32, 'Lamp theme mismatch')
const adaptations = ['紫晶寻门以可步行实体门廊、上部门楣玻璃和方向地坪表达；不含传送门方块、命令或跨维度传送逻辑。', '四向门廊分别采用紫晶、深板岩、红砖锈铜、砂岩；所有出口接同高五格宽十字主路。', '概念图敞顶展示改为完整双层屋顶；仅软件剖切隐藏屋顶或观察侧墙。', '木箱为去皮云杉原木静态摆件，纹章为紫色混凝土与紫晶；无库存、奖励或旗帜方块实体。']
const metadata = { id: 'zone-N04', source_id: 'N04', name: '寻门枢纽', category: 'hub', size: [35, 16, 35], data_version: 3465,
  entrance: [17, 2, 0], interior: safe, safe_points: [safe, [17, 2, 6], [6, 2, 17], [28, 2, 17], [17, 2, 28]], facing: 'north', openings,
  sha256, reference, reference_sha256: referenceSha, full_volume_air_records: true, runtime_teleport: false, runtime_slot: null,
  deployment: 'not_deployed', source_documents: ['README.md', 'docs/manifest.json', 'docs/verify.json'].map(part => 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/' + part),
  contract: 'D:/ft工程文件/FT地牢_Image25接入_20261101/CONTRACT.md', design_adaptations: adaptations,
  route: { type: 'five-wide cardinal cross', floor_y: 1, feet_y: 2, minimum_headroom: 5, hub: safe }, materials: counts,
  integration_requirements: ['验证整个 35x16x35 体积及液体/实体占用，包括空气记录；不得无条件清空。', '为 zone-N04 分配独立槽位与持久标记；入口安全点随原点和旋转一起变换。', '四向外接走廊由接入负责人布置；本交付不包含大厅接线、回程门或传送实现。'] }
const report = { id: 'zone-N04', status: errors.length ? 'FAIL' : 'PASS', sha256, reference_sha256: referenceSha, size: structure.getSize(), records: blocks.length,
  data_version: nbt.getNumber('DataVersion'), full_volume: true, unique_positions: occupied.size, foundation_cells: 1225, roof_cells: 2450,
  cross_route_cells: 325, cross_route_clear_height: 5, four_ports_reachable: openings.every(port => reached.has(port.feet.join(','))), safe_point_reachable: reached.has(safe.join(',')),
  reachable_feet: reached.size, hanging_lanterns: hanging, standing_lanterns: standing, anchored_chains: chains,
  entities: 0, block_entities: 0, fluids: 0, runtime_portal_blocks: 0, source_documents_verified: true, errors, materials: counts }
await fs.mkdir(path.join(root, 'qa'), { recursive: true })
await fs.writeFile(path.join(root, 'room.json'), JSON.stringify(metadata, null, 2) + '\n')
await fs.writeFile(path.join(root, 'qa/static.json'), JSON.stringify(report, null, 2) + '\n')
console.log(JSON.stringify({ ...report, materials: undefined }, null, 2))
if (errors.length) process.exitCode = 1
