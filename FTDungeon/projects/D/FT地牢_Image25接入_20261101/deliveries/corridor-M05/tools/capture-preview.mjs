import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createRequire } from 'node:module'
import { createHash } from 'node:crypto'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const require = createRequire(import.meta.url)
const { chromium } = require('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright')
const sharp = require('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/sharp')
const browser = await chromium.launch({ headless: true, channel: 'msedge', args: ['--enable-webgl', '--ignore-gpu-blocklist', '--enable-unsafe-swiftshader'] })
const page = await browser.newPage({ viewport: { width: 1536, height: 1024 }, deviceScaleFactor: 1 })
const errors = []
page.on('pageerror', error => errors.push(error.message))
const evidence = []
const previewRoot = path.join(root, 'previews')
await fs.mkdir(previewRoot, { recursive: true })
try {
  await page.goto((process.argv[2] ?? 'http://127.0.0.1:5145/') + '?capture=1', { waitUntil: 'networkidle', timeout: 180000 })
  await page.waitForFunction(() => window.m05?.ready, { timeout: 180000 })
  const views = await page.evaluate(() => window.m05.views)
  for (const view of views) {
    const details = await page.evaluate(async key => await window.m05.showView(key), view)
    await page.waitForTimeout(350)
    const target = path.join(previewRoot, `${view}.png`)
    await page.locator('canvas').screenshot({ path: target })
    const stats = await sharp(target).stats()
    if (stats.entropy < 1.5) throw new Error(`Render unexpectedly flat: ${view}, entropy=${stats.entropy}`)
    evidence.push({ ...details, file: `previews/${view}.png`, dimensions: [1536, 1024], entropy: stats.entropy })
    process.stdout.write(`CAPTURED ${view}\n`)
  }
  if (errors.length) throw new Error(`Browser errors: ${errors.join('; ')}`)
  await fs.copyFile(path.join(previewRoot, 'interior-south.png'), path.join(root, 'preview.png'))
  const ordered = ['exterior-north', 'exterior-south', 'exterior-west', 'exterior-east', 'interior-north', 'interior-south', 'interior-west', 'interior-east']
  const labels = ['NORTH / COMPLETE EXTERIOR', 'SOUTH / COMPLETE EXTERIOR', 'WEST / COMPLETE EXTERIOR', 'EAST / COMPLETE EXTERIOR', 'INTERIOR / LOOKING NORTH', 'INTERIOR / LOOKING SOUTH', 'INTERIOR / WEST CELL FRONT', 'INTERIOR / EAST CELL FRONT']
  const overlays = []
  const tileWidth = 768
  const tileHeight = 544
  for (const [index, view] of ordered.entries()) {
    const left = (index % 2) * tileWidth
    const top = 96 + Math.floor(index / 2) * tileHeight
    overlays.push({ input: await sharp(path.join(previewRoot, `${view}.png`)).resize(tileWidth, 512).toBuffer(), left, top: top + 32 })
    const label = `<svg width="768" height="32"><rect width="100%" height="100%" fill="#212326"/><text x="18" y="22" font-family="Segoe UI,Arial" font-size="15" fill="#edd4a4">${labels[index]}</text></svg>`
    overlays.push({ input: Buffer.from(label), left, top })
  }
  const heading = '<svg width="1536" height="96"><rect width="100%" height="100%" fill="#17191c"/><text x="26" y="40" font-family="Segoe UI,Arial" font-size="25" font-weight="bold" fill="#eddbb8">M05 / PRISON CELLS - INTACT</text><text x="26" y="70" font-family="Segoe UI,Arial" font-size="15" fill="#aaa89f">19 x 12 x 40 | 2.5 chunks | Actual NBT / Lodestone software rendering | Four complete exteriors + four interiors</text></svg>'
  overlays.push({ input: Buffer.from(heading), left: 0, top: 0 })
  await sharp({ create: { width: 1536, height: 96 + tileHeight * 4, channels: 3, background: '#17191c' } }).composite(overlays).png().toFile(path.join(previewRoot, 'four-directions-sheet.png'))
  const sha256 = createHash('sha256').update(await fs.readFile(path.join(root, 'room.nbt'))).digest('hex')
  await fs.writeFile(path.join(root, 'qa/visual.json'), JSON.stringify({ id: 'corridor-M05', status: 'PASS', sha256, renderer: 'Lodestone 0.6.0 / ThreeStructureRenderer', source: 'room.nbt', source_records: 9120, cutaway_changes_nbt: false, page_errors: errors, views: evidence, evidence_level: 'software-rendered actual NBT; not modpack client screenshots' }, null, 2) + '\n')
} finally {
  await browser.close()
}
