import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { Structure } from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const resources = path.join(root, 'native-test/src/main/resources')
const target = path.join(resources, 'data/ftcorridorm14qa/structures')
await fs.mkdir(target, { recursive: true })
const empty = new Structure([1, 1, 1])
empty.addBlock([0, 0, 0], 'minecraft:air')
await fs.writeFile(path.join(target, 'empty.nbt'), empty.writeNbt({ name: '', dataVersion: 3465, compression: 'gzip' }))
await fs.writeFile(path.join(resources, 'pack.mcmeta'), JSON.stringify({ pack: { pack_format: 15, description: 'FT corridor M14 isolated QA resources; never deploy' } }, null, 2) + '\n')
