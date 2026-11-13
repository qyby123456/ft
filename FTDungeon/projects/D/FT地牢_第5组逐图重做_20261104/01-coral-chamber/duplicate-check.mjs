import fs from 'node:fs'
import crypto from 'node:crypto'
import {NbtFile, Structure} from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'
const root = new URL('.', import.meta.url)
const target = new URL('nbt/s5-01-coral-chamber-redo.nbt', root)
const comparisons = [
  'D:/ft工程文件/FT地牢_第2组重做B_20261104/S2-01/incoming/G2B-01/room.nbt',
  'D:/ft工程文件/FT地牢_第7组逐图重做_20261104/01-large-chamber/incoming/S7R2-01/room.nbt',
  'D:/ft工程文件/FT地牢_首批第3组_冰原霜穴_20261104/image-led-sequential/S3-01/room.nbt',
]
const hash = bytes => crypto.createHash('sha256').update(bytes).digest('hex')
function geometry(file) {
  const bytes = fs.readFileSync(file)
  const structure = Structure.fromNbt(NbtFile.read(bytes).root)
  const positions = structure.getBlocks().filter(entry => !['minecraft:air', 'minecraft:water', 'minecraft:structure_void'].includes(entry.state.getName().toString())).map(entry => entry.pos.join(',')).sort()
  return {file: String(file), size: structure.getSize(), sha256: hash(bytes), solidGeometry: hash(JSON.stringify(positions)), nonAirNonWater: positions.length}
}
const current = geometry(target)
const checked = comparisons.filter(file => fs.existsSync(file)).map(geometry)
const exactDuplicate = checked.some(entry => entry.sha256 === current.sha256 || entry.solidGeometry === current.solidGeometry)
fs.writeFileSync(new URL('qa/duplicate-check.json', root), JSON.stringify({status: exactDuplicate ? 'FAIL' : 'PASS_LIMITED', current, checked, limit: 'Exact NBT and material-independent non-air/non-water coordinate identity only. Includes inactive group3 failure evidence, not claimed as active candidate. Does not prove absence of near-duplicates or cover unavailable groups.'}, null, 2))
if (exactDuplicate) process.exitCode = 1
