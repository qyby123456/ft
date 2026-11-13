import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath, pathToFileURL } from 'node:url'
import { createHash } from 'node:crypto'

export const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
export const skillRoot = 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
export const lodestone = await import(pathToFileURL(path.join(skillRoot, 'node_modules/@mattzh72/lodestone/lib/index.js')))
export const sha256 = bytes => createHash('sha256').update(bytes).digest('hex')
export const writeJson = async (relative, value) => {
  const destination = path.join(root, relative)
  await fs.mkdir(path.dirname(destination), { recursive: true })
  await fs.writeFile(destination, `${JSON.stringify(value, null, 2)}\n`)
}
export async function readRoom(relative = 'room.nbt') {
  const bytes = await fs.readFile(path.join(root, relative))
  const file = lodestone.NbtFile.read(bytes)
  const structure = lodestone.Structure.fromNbt(file.root)
  return { bytes, file, structure, hash: sha256(bytes) }
}
