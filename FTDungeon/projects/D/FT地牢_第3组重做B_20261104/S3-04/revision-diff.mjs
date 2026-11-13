import fs from 'node:fs/promises'
import path from 'node:path'
import crypto from 'node:crypto'
import { fileURLToPath } from 'node:url'
import { NbtFile, Structure } from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'

const root = path.dirname(fileURLToPath(import.meta.url))
const archive = path.join(root, '../history/S3-04-before-stone-column-r2-37ad900')
const hash = bytes => crypto.createHash('sha256').update(bytes).digest('hex')
const beforeBytes = await fs.readFile(path.join(archive, 'room.nbt'))
const afterBytes = await fs.readFile(path.join(root, 'room.nbt'))
if (hash(beforeBytes) !== '37ad90011ed98a49c46494174dd3e1810e8c10c536ddab59ac0ae507fe99a8a3') throw new Error('Wrong baseline')
const before = Structure.fromNbt(NbtFile.read(beforeBytes).root).getBlocks()
const after = Structure.fromNbt(NbtFile.read(afterBytes).root).getBlocks()
const oldGrid = new Map(before.map(block => [block.pos.join(','), block]))
const changes = []
const allowed = ([east, height, south]) => [9, 25].some(center => [10, 22, 34, 46].some(depth => (Math.abs(east - center) <= 1 && Math.abs(south - depth) <= 1 && height >= 7 && height <= 17) || (Math.abs(east - center) <= 2 && Math.abs(south - depth) <= 2 && height >= 19 && height <= 21)))
if (before.length !== after.length) throw new Error('Coordinate count changed')
for (const block of after) {
  const previous = oldGrid.get(block.pos.join(','))
  if (!previous || JSON.stringify(previous.nbt) !== JSON.stringify(block.nbt)) throw new Error('Coordinates or block entities changed')
  if (previous.state.toString() !== block.state.toString()) {
    if (!allowed(block.pos)) throw new Error(`Out-of-scope change ${block.pos}`)
    changes.push({ pos: block.pos, before: previous.state.toString(), after: block.state.toString() })
  }
}
const countWood = blocks => blocks.filter(block => block.state.getName().toString() === 'minecraft:spruce_log').length
const archiveManifest = JSON.parse(await fs.readFile(path.join(archive, 'SHA256.json'), 'utf8'))
for (const [name, expected] of Object.entries(archiveManifest)) if (hash(await fs.readFile(path.join(archive, name))) !== expected) throw new Error(`Archive changed: ${name}`)
const report = { status: 'PASS', revision: 'R2 stone-dominant columns', before_sha256: hash(beforeBytes), after_sha256: hash(afterBytes), changed_records: changes.length, outside_column_regions_changed: 0, spruce_logs_before: countWood(before), spruce_logs_after: countWood(after), wood_reduction_percent: (1 - countWood(after) / countWood(before)) * 100, unchanged: ['all coordinates and block entities', 'six boundary portals', 'floor and route supports', 'roof Y24..26', 'lanterns and chains', 'banners', 'snow and ice'], archive_manifest_entries_verified: Object.keys(archiveManifest).length, changes }
await fs.writeFile(path.join(root, 'revision-diff.json'), JSON.stringify(report, null, 2))
console.log(JSON.stringify({ ...report, changes: undefined }, null, 2))
