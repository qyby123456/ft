import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createRequire } from 'node:module'
const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const runtimeRequire = createRequire('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/package.json')
const sharp = runtimeRequire('sharp')
const label = (text, width, height) => Buffer.from('<svg xmlns="http://www.w3.org/2000/svg" width="'+width+'" height="'+height+'"><rect width="100%" height="100%" fill="#111c1a"/><text x="20" y="'+Math.round(height*0.66)+'" font-family="Segoe UI, sans-serif" font-size="'+Math.round(height*0.42)+'" fill="#e1e6d3">'+text+'</text></svg>')
const directions = ['north', 'south', 'west', 'east']
for (const mode of ['internal', 'external']) {
  const layers = []
  for (let index = 0; index < directions.length; index += 1) {
    const direction = directions[index]
    const left = index % 2 * 900
    const top = Math.floor(index / 2) * 654
    const image = await sharp(path.join(root, 'views', direction+'-'+mode+'.png')).resize(900, 600).toBuffer()
    layers.push({ input: image, left, top: top+54 }, { input: label(direction.toUpperCase()+' / '+(mode === 'internal' ? 'ROOF + FRONT WALL CUT' : 'COMPLETE SHELL'), 900, 54), left, top })
  }
  await sharp({ create: { width: 1800, height: 1308, channels: 3, background: '#0f1715' } }).composite(layers).png().toFile(path.join(root, 'views', 'four-'+mode+'.png'))
}
const reviewLayers = []
for (let index = 0; index < directions.length; index += 1) {
  for (let modeIndex = 0; modeIndex < 2; modeIndex += 1) {
    const direction = directions[index]
    const mode = modeIndex === 0 ? 'internal' : 'external'
    const left = index * 600
    const top = modeIndex * 444
    const image = await sharp(path.join(root, 'views', direction+'-'+mode+'.png')).resize(600, 400).toBuffer()
    reviewLayers.push({input:image,left,top:top+44},{input:label(direction.toUpperCase()+' / '+mode.toUpperCase(),600,44),left,top})
  }
}
await sharp({create:{width:2400,height:888,channels:3,background:'#0f1715'}}).composite(reviewLayers).png().toFile(path.join(root,'views','four-direction-review.png'))
await sharp(path.join(root,'views','overview.png')).composite([{input:label('zone-R03 / OVERGROWN SHRINE / ACTUAL NBT CUTAWAY',1536,60),left:0,top:0}]).png().toFile(path.join(root,'preview.png'))
console.log('Wrote preview.png and three direction sheets from actual NBT renderings')

