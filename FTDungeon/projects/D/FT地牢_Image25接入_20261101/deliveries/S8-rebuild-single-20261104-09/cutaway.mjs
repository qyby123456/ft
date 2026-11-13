import fs from 'node:fs'
import path from 'node:path'
import crypto from 'node:crypto'
import { fileURLToPath } from 'node:url'

export default function preview(api) {
  const root = path.dirname(fileURLToPath(import.meta.url))
  const bytes = fs.readFileSync(path.join(root, 'incoming/S8R_IMG09/room.nbt'))
  const metadata = JSON.parse(fs.readFileSync(path.join(root, 'incoming/S8R_IMG09/room.json'), 'utf8'))
  if (crypto.createHash('sha256').update(bytes).digest('hex') !== metadata.sha256) throw new Error('Final source mismatch')
  const structure = api.lodestone.Structure.fromNbt(api.lodestone.NbtFile.read(new Uint8Array(bytes)).root)
  const cuts = JSON.parse(fs.readFileSync(path.join(root, 'qa/cut-rules.json'), 'utf8'))
  const direction = process.env.S8_INSPECTION_DIRECTION
  if (!cuts[direction]) throw new Error('Explicit cardinal direction required')
  for (const [first, last] of cuts[direction]) api.fill(structure, first, last, 'minecraft:air')
  return { structure, metadata: { name: `inspection-only-${direction}` } }
}




