import fs from 'node:fs/promises'
import path from 'node:path'
import crypto from 'node:crypto'
import { fileURLToPath } from 'node:url'
import { NbtFile, Structure } from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'

const destination = path.dirname(fileURLToPath(import.meta.url))
const root = path.dirname(destination)
const oldFile = path.resolve(root, '../history/frozen-4fc6cae-before-snow-repair/room.nbt')
const oldBytes = await fs.readFile(oldFile)
const newBytes = await fs.readFile(path.join(root, 'room.nbt'))
const decode = bytes => new Map(Structure.fromNbt(NbtFile.read(bytes).root).getBlocks().map(block => [block.pos.join(','), block.state.toString()]))
const before = decode(oldBytes)
const after = decode(newBytes)
const changes = []
for (const [pos, state] of before) if (state !== after.get(pos)) changes.push({ pos: pos.split(',').map(Number), before: state, after: after.get(pos) })
if (changes.length !== 122 || changes.some(change => !change.before.startsWith('minecraft:snow[') || change.after !== 'minecraft:air')) throw new Error('Unexpected changes outside snow repair')
const hash = bytes => crypto.createHash('sha256').update(bytes).digest('hex')
await fs.writeFile(path.join(destination, 'revision-diff.json'), JSON.stringify({ status: 'PASS', previous_sha256: hash(oldBytes), sha256: hash(newBytes), changed_count: changes.length, all_changes: 'Only invalid snow layers to air, no solid support, materials, geometry, doorway, roof, landmark or light changes', changes }, null, 2))
console.log(`PASS ${changes.length} snow-only changes`)
