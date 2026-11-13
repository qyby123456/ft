import fs from 'node:fs/promises'
import path from 'node:path'
import {fileURLToPath} from 'node:url'
import {createHash} from 'node:crypto'
import {NbtFile} from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const sourceBytes = await fs.readFile(path.join(root, 'room.nbt'))
const source = NbtFile.read(sourceBytes).root.toSimplifiedJson()
const native = JSON.parse(await fs.readFile(path.join(root, 'qa/forge.json'), 'utf8'))
const rows = []
const transform = (position, rotation) => {
  const [across, height, depth] = position
  if (rotation === 'CLOCKWISE_90') return [18 - depth, height, across]
  if (rotation === 'CLOCKWISE_180') return [18 - across, height, 18 - depth]
  if (rotation === 'COUNTERCLOCKWISE_90') return [depth, height, 18 - across]
  return position
}
const canonical = value => JSON.stringify(value, Object.keys(value).sort())
for (const rotation of native.rotations) {
  const document = NbtFile.read(await fs.readFile(path.join(root, rotation.world_capture))).root.toSimplifiedJson()
  const captured = new Map(document.blocks.map(entry => [entry.pos.join(','), entry]))
  let emptyBarrels = 0
  let patternedBanners = 0
  for (const entry of source.blocks.filter(record => record.nbt)) {
    const record = captured.get(transform(entry.pos, rotation.rotation).join(','))
    if (!record?.nbt) throw new Error('Captured block entity missing')
    if (entry.nbt.id === 'minecraft:barrel') {
      if (record.nbt.id !== entry.nbt.id || record.nbt.Items.length !== 0 || record.nbt.LootTable) throw new Error('Barrel data changed')
      emptyBarrels++
    } else {
      if (record.nbt.id !== entry.nbt.id || record.nbt.Patterns.length !== entry.nbt.Patterns.length || !record.nbt.Patterns.every((pattern, index) => canonical(pattern) === canonical(entry.nbt.Patterns[index]))) throw new Error('Banner pattern or color changed')
      patternedBanners++
    }
  }
  if (emptyBarrels !== 6 || patternedBanners !== 1) throw new Error('Block entity count mismatch')
  rows.push({rotation: rotation.rotation, empty_barrels: emptyBarrels, exact_patterned_banners: patternedBanners, status: 'PASS'})
}
await fs.writeFile(path.join(root, 'qa/capture-block-entities.json'), JSON.stringify({status: 'PASS', structure_sha256: createHash('sha256').update(sourceBytes).digest('hex'), source: 'native world captures after 160 ticks', rotations: rows}, null, 2) + '\n')
process.stdout.write('Native capture block entities: PASS 4/4\n')
