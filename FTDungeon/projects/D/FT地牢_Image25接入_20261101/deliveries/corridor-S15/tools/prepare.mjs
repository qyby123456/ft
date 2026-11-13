import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createHash } from 'node:crypto'
import build from '../recipe.mjs'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const reference = 'D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/S15-prison-cells-aged.png'
const expectedReference = 'f0c77a9be5e1d6db19c5874c431de002f2f17a21232f83a879bd774b2c6be09b'
const digest = async file => createHash('sha256').update(await fs.readFile(file)).digest('hex')
const referenceHash = await digest(reference)
if (referenceHash !== expectedReference) throw new Error('Reference hash mismatch')
const summary = JSON.parse(await fs.readFile(path.join(root, 'summary.json'), 'utf8'))
const result = build({ createStructure: () => null, setBlock: () => undefined, fill: () => undefined })
const metadata = {
  id: 'CORRIDOR_S15',
  design_id: 'S15',
  name: '陈旧监牢短廊',
  size: summary.size,
  entrance: [7, 1, 0],
  interior: [7, 1, 2],
  exit: [7, 1, 15],
  facing: 'north',
  entrances: [
    { side: 'north', center: [7, 1, 0], opening: [[6, 1, 0], [8, 3, 0]], width: 3, height: 3, state: 'always_open' },
    { side: 'south', center: [7, 1, 15], opening: [[6, 1, 15], [8, 3, 15]], width: 3, height: 3, state: 'always_open' }
  ],
  safe_points: result.metadata.blueprint.safe_points,
  passage: { axis: 'z', bounds: [[6, 1, 0], [8, 3, 15]], width: 3, clear_height: 3, level: true, branches: false },
  sha256: await digest(path.join(root, 'room.nbt')),
  reference,
  reference_sha256: referenceHash,
  reference_size: [1536, 1024],
  batch_readme: 'D:/ft工程文件/image2.5/dungeon-corridors-20261103/README.md',
  batch_spec: 'D:/ft工程文件/image2.5/dungeon-corridors-20261103/SPEC.md',
  batch_manifest: 'D:/ft工程文件/image2.5/dungeon-corridors-20261103/manifest.json',
  minecraft: '1.20.1',
  data_version: 3465,
  category: 'corridor_short',
  nominal_length_chunks: 1,
  roof: 'complete_two_layers_except_structurally_anchored_chain_pockets',
  liquid_policy: 'none',
  inventory_policy: 'no_containers_no_loot_tables_no_items_no_entities',
  integration_status: 'standalone_not_deployed',
  reserved_slot: null,
  adaptations: [
    '概念图尽头关闭的铁门改为南侧常开三宽三高出口；北侧相同接口，确保一字贯通。',
    '两侧六间牢房为封闭装饰囚室，不是分岔；铁门无按钮、无奖励和囚犯实体。',
    '灰石、裂石、苔痕、四盏链悬魂灯、两只四链吊笼保留；原版铁栏为灰色，无自定义锈色纹理。',
    '吊笼为紧凑2×2栏栅雕塑，四根链条各自连到完整顶层；底板从Y=4起，不压缩Y=1..3主路净空。',
    '囚室床铺为木平台加灰白毯，不使用在FT维度可能爆炸的可交互床。',
    '蛛网仅在封闭侧牢上角，主路无蛛网、掉落物、液体、台阶或地面碎石碰撞。',
    '软件剖视仅在内存隐藏屋顶和近侧墙，交付NBT保留完整外壳。'
  ]
}
for (const [filename, value] of [['room.json', metadata], ['blueprint.json', result.metadata.blueprint]]) {
  await fs.writeFile(path.join(root, filename), JSON.stringify(value, null, 2) + '\n')
}
process.stdout.write(JSON.stringify({ status: 'PREPARED', size: metadata.size, sha256: metadata.sha256 }) + '\n')
