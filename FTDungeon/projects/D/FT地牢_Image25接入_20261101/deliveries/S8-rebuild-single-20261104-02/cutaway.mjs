import fs from 'node:fs'
import crypto from 'node:crypto'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

export default function preview(api) {
  const root = path.dirname(fileURLToPath(import.meta.url))
  const metadata = JSON.parse(fs.readFileSync(path.join(root, 'incoming/S8R_IMG02/room.json'), 'utf8'))
  const bytes = fs.readFileSync(path.join(root, 'incoming/S8R_IMG02/room.nbt'))
  if (crypto.createHash('sha256').update(bytes).digest('hex') !== metadata.sha256) throw new Error('Final NBT hash mismatch')
  const structure = api.lodestone.Structure.fromNbt(api.lodestone.NbtFile.read(new Uint8Array(bytes)).root)
  const cuts = {
    north: [[[0, 2, 0], [24, 13, 1]], [[1, 13, 1], [23, 13, 2]]],
    east: [[[23, 2, 0], [24, 13, 24]], [[22, 13, 1], [23, 13, 23]]],
    south: [[[0, 2, 23], [24, 13, 24]], [[1, 13, 22], [23, 13, 23]]],
    west: [[[0, 2, 0], [1, 13, 24]], [[1, 13, 1], [2, 13, 23]]],
  }
  const direction = process.env.S8_INSPECTION_DIRECTION
  if (!cuts[direction]) throw new Error('Explicit S8_INSPECTION_DIRECTION required')
  for (const [first, last] of [[[0, 14, 0], [24, 14, 24]], ...cuts[direction]]) api.fill(structure, first, last, 'minecraft:air')
  return { structure, metadata: { name: `inspection-only-${direction}` } }
}
