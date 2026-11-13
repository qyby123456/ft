import fs from 'node:fs'
import crypto from 'node:crypto'

export default function build(api) {
  const source = 'D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/S8-rebuild-single-20261104-01/incoming/S8R_IMG01/room.nbt'
  const bytes = fs.readFileSync(source)
  if (crypto.createHash('sha256').update(bytes).digest('hex') !== 'ee50ce32006d15b24ae2b2932bbf4ebbef6683ae8b9ee33ca4781348ac0fa798') throw new Error('Wrong final source NBT')
  const structure = api.lodestone.Structure.fromNbt(api.lodestone.NbtFile.read(new Uint8Array(bytes)).root)
  const direction = process.env.S8_INSPECTION_DIRECTION
  const cuts = {
    north: [[[0, 3, 0], [34, 19, 1]], [[4, 19, 4], [30, 19, 6]]],
    east: [[[33, 3, 0], [34, 19, 34]], [[28, 19, 4], [30, 19, 30]]],
    south: [[[0, 3, 33], [34, 19, 34]], [[4, 19, 28], [30, 19, 30]]],
    west: [[[0, 3, 0], [1, 19, 34]], [[4, 19, 4], [6, 19, 30]]],
  }
  if (!cuts[direction]) throw new Error('S8_INSPECTION_DIRECTION must be a cardinal direction')
  for (const [first, last] of [[[0, 20, 0], [34, 20, 34]], ...cuts[direction]]) api.fill(structure, first, last, 'minecraft:air')
  return { structure, metadata: { name: `inspection-only-${direction}` } }
}
