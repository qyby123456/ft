import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createHash } from 'node:crypto'
import { plan } from './recipe.mjs'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const reference = 'D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/M03-blood-ritual-intact.png'
const referenceSha256 = '2bd12059b40ca01c7f4576e3af80f099382dc20b1c87ae34128332f5391c33ea'
const digest = bytes => createHash('sha256').update(bytes).digest('hex')
if (digest(await fs.readFile(reference)) !== referenceSha256) throw new Error('Reference image SHA-256 mismatch')
const source = await fs.readFile(path.join(root, 'builder/room.nbt'))
await fs.copyFile(path.join(root, 'builder/room.nbt'), path.join(root, 'room.nbt'))
const supportedDecor = []
for (const depth of plan.bayStations) {
  for (const across of [2, 10]) {
    for (const candleDepth of [depth - 1, depth + 1]) {
      supportedDecor.push({ kind: 'candle', position: [across, 3, candleDepth], anchor: [across, 2, candleDepth], face: 'up' })
    }
    supportedDecor.push({ kind: 'banner', position: [across, 6, depth], anchor: [across === 2 ? 1 : 11, 6, depth], face: across === 2 ? 'east' : 'west' })
  }
}
const metadata = {
  ...plan,
  facing: 'north',
  corridor_id: 'M03',
  kind: 'medium',
  theme: 'blood-ritual',
  condition: 'intact',
  light: 'candles',
  feature: 'repeated stone arches with cracked relief accents',
  sha256: digest(source),
  reference,
  reference_sha256: referenceSha256,
  data_version: 3465,
  minecraft: '1.20.1',
  axis: '+Z',
  length_blocks: 41,
  length_chunks: 41 / 16,
  portals: [
    { direction: 'north', center: plan.entrance, clear_width: 5, clear_height: 3 },
    { direction: 'south', center: plan.exit, clear_width: 5, clear_height: 3 },
  ],
  palette_intent: ['gray stone bricks', 'deepslate bands', 'blood-red inset wool runner', 'solid red-nether-brick channels', 'black-on-red patterned banners', 'ivory ritual candles'],
  adaptation: ['The conceptual blood channels are solid dry inlays, not fluid.', 'The runner is flush with the stone floor, not a tripping step.', 'Intact roof and arch structure take priority over the prompt label broken-arches; small cracked accents retain the motif.'],
  supported_decor: supportedDecor,
  deployed: false,
  player_saves_touched: false,
  runtime_scope: 'Independent vanilla structure only. No FT core, game instance, merchant, combat, loot or world integration changes.',
}
await fs.writeFile(path.join(root, 'room.json'), JSON.stringify(metadata, null, 2) + String.fromCharCode(10))
console.log(JSON.stringify({ size: plan.size, sha256: metadata.sha256, decor: supportedDecor.length }))
