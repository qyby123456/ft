import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { Structure } from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'
const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const target = path.join(root, 'native-test/src/main/resources/data/ftzoneh05qa/structures')
await fs.mkdir(target, { recursive: true })
const structure = new Structure([1, 1, 1])
structure.addBlock([0, 0, 0], 'minecraft:air')
await fs.writeFile(path.join(target, 'empty.nbt'), structure.writeNbt({ name: '', dataVersion: 3465, compression: 'gzip' }))
