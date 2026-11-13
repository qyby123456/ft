import fs from 'node:fs/promises'
import path from 'node:path'
import { createRequire } from 'node:module'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const require = createRequire('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/runtime.cjs')
const sharp = require('sharp')
for (const mode of ['interior', 'exterior', 'eye']) {
  const layers = []
  for (const [index, direction] of ['north', 'east', 'south', 'west'].entries()) {
    const image = await sharp(path.join(root, 'views', mode + '-' + direction + '.png')).resize(720, 540).png().toBuffer()
    const left = (index % 2) * 720
    const top = Math.floor(index / 2) * 580
    layers.push({ input: image, left, top: top + 40 })
    const caption = Buffer.from('<svg width="720" height="40"><rect width="720" height="40" fill="#172536"/><text x="20" y="27" font-family="Arial" font-size="20" fill="#eef4ff">zone-M05 / ' + mode.toUpperCase() + ' / ' + direction.toUpperCase() + '</text></svg>')
    layers.push({ input: caption, left, top })
  }
  await sharp({ create: { width: 1440, height: 1160, channels: 3, background: '#172536' } }).composite(layers).png().toFile(path.join(root, 'views', mode + '-sheet.png'))
}
await fs.copyFile(path.join(root, 'views/interior-north.png'), path.join(root, 'preview.png'))
console.log('Wrote interior, exterior and eye four-view sheets from actual NBT captures')
