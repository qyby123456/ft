import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createRequire } from 'node:module'
import { createHash } from 'node:crypto'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const require = createRequire(import.meta.url)
const { chromium } = require('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright')
const sharp = require('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/sharp')
const digest = bytes => createHash('sha256').update(bytes).digest('hex')
const sourceHash = digest(await fs.readFile(path.join(root, 'room.nbt')))
const browser = await chromium.launch({ headless: true, channel: 'msedge', args: ['--enable-webgl', '--ignore-gpu-blocklist', '--enable-unsafe-swiftshader'] })
const page = await browser.newPage({ viewport: { width: 1536, height: 1024 }, deviceScaleFactor: 1 })
const errors = []
page.on('pageerror', error => errors.push(error.message))
const evidence = []
await fs.mkdir(path.join(root, 'previews'), { recursive: true })
try {
  const address = new URL(process.argv[2] ?? 'http://127.0.0.1:5183/')
  address.searchParams.set('capture', '1')
  await page.goto(address.toString(), { waitUntil: 'networkidle', timeout: 180000 })
  await page.waitForFunction(() => window.h03?.ready, { timeout: 180000 })
  const views = await page.evaluate(() => window.h03.views)
  for (const view of views) {
    const details = await page.evaluate(async key => await window.h03.showView(key), view)
    await page.waitForTimeout(350)
    const relative = `previews/${view}.png`
    const filename = path.join(root, relative)
    await page.locator('canvas').screenshot({ path: filename })
    const stats = await sharp(filename).stats()
    if (stats.entropy < 1.5) throw new Error(`Unexpectedly flat screenshot: ${view}, entropy=${stats.entropy}`)
    evidence.push({ ...details, file: relative, dimensions: [1536, 1024], entropy: stats.entropy, sha256: digest(await fs.readFile(filename)) })
    process.stdout.write(`H03_CAPTURED ${view} entropy=${stats.entropy.toFixed(2)}\n`)
  }
  if (errors.length) throw new Error(`Browser errors: ${errors.join('; ')}`)
  if (sourceHash !== digest(await fs.readFile(path.join(root, 'room.nbt')))) throw new Error('NBT changed during capture')
  await fs.copyFile(path.join(root, 'previews/cutaway-north.png'), path.join(root, 'preview.png'))
  const ordered = ['cutaway-north', 'cutaway-south', 'cutaway-west', 'cutaway-east', 'exterior-north', 'exterior-south', 'exterior-west', 'exterior-east', 'inside-south', 'inside-north']
  const labels = ['NORTH / INTERNAL SECTION', 'SOUTH / INTERNAL SECTION', 'WEST / INTERNAL SECTION', 'EAST / INTERNAL SECTION', 'NORTH / COMPLETE SHELL', 'SOUTH / COMPLETE SHELL', 'WEST / COMPLETE SHELL', 'EAST / COMPLETE SHELL', 'ALTAR / INTACT CEILING', 'ENTRANCE / INTACT CEILING']
  const overlays = []
  for (const [index, view] of ordered.entries()) {
    const left = index % 2 * 768
    const top = 96 + Math.floor(index / 2) * 544
    overlays.push({ input: await sharp(path.join(root, `previews/${view}.png`)).resize(768, 512).toBuffer(), left, top: top + 32 })
    overlays.push({ input: Buffer.from(`<svg width="768" height="32"><rect width="100%" height="100%" fill="#27232b"/><text x="18" y="22" font-family="Segoe UI,Arial" font-size="15" fill="#e5cb83">${labels[index]}</text></svg>`), left, top })
  }
  overlays.push({ input: Buffer.from('<svg width="1536" height="96"><rect width="100%" height="100%" fill="#171820"/><text x="26" y="40" font-family="Segoe UI,Arial" font-size="25" fill="#eddfba">H03 / CANDLE CEREMONIAL HALL</text><text x="26" y="70" font-family="Segoe UI,Arial" font-size="15" fill="#afa694">29 x 16 x 35 | Actual final NBT / Lodestone software render | 4 sections + 4 complete shells + 2 intact interiors</text></svg>'), left: 0, top: 0 })
  await sharp({ create: { width: 1536, height: 2816, channels: 3, background: '#171820' } }).composite(overlays).png().toFile(path.join(root, 'previews/four-directions-sheet.png'))
  const report = { id: 'zone-H03', status: 'CAPTURED_PENDING_VISUAL_REVIEW', sha256: sourceHash, renderer: 'Lodestone 0.6.0 / ThreeStructureRenderer', source: 'room.nbt', source_records: 16240, page_errors: errors, cutaway_changes_nbt: false, views: evidence, evidence_level: 'Software render of actual final NBT, not modpack client screenshots' }
  await fs.writeFile(path.join(root, 'qa/visual.json'), JSON.stringify(report, null, 2) + '\n')
} finally { await browser.close() }
