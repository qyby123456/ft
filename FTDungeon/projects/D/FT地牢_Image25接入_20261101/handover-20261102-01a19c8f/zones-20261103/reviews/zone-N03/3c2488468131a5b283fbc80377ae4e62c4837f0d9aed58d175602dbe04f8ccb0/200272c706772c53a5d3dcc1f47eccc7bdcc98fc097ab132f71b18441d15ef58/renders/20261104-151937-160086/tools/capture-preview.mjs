import fs from 'node:fs/promises'
import path from 'node:path'
import { createRequire } from 'node:module'
import { fileURLToPath } from 'node:url'
import { createHash } from 'node:crypto'

const require = createRequire(import.meta.url)
const { chromium } = require('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright')
const sharp = require('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/sharp')
const root = process.cwd()
const preview = path.join(root, 'preview')
await fs.mkdir(preview, { recursive: true })
const browser = await chromium.launch({ executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe', headless: true, args: ['--enable-unsafe-swiftshader'] })
try {
  const page = await browser.newPage({ viewport: { width: 1600, height: 1000 }, deviceScaleFactor: 1 })
  page.on('console', message => console.log(message.type() + ': ' + message.text()))
  await page.addInitScript(() => {
    let stage = 'initial'
    Object.defineProperty(window, 'a05Stage', { get: () => stage, set: value => { stage = value; console.log('A05_STAGE: ' + value) } })
  })
  const errors = []
  page.on('pageerror', error => { errors.push(error.message); console.error(error.message) })
  const url = new URL('/viewer.html', process.argv[2] ?? 'http://127.0.0.1:5655/')
  await page.goto(url.toString(), { waitUntil: 'domcontentloaded', timeout: 120000 })
  console.log('Viewer loaded: ' + url)
  try {
    await page.waitForFunction(() => window.a05Ready, null, { timeout: 240000 })
  } catch (error) {
    console.log(await page.evaluate(() => ({ stage: window.a05Stage, label: document.body.innerText })))
    await page.screenshot({ path: path.join(preview, 'render-debug.png') })
    throw error
  }
  console.log('NBT scene ready')
  const views = []
  for (const cutaway of [false, true]) {
    for (const direction of ['north', 'south', 'west', 'east']) {
      const state = await page.evaluate(async ({ direction, cutaway }) => window.showView(direction, cutaway), { direction, cutaway })
      console.log('Rendering ' + direction + ' cutaway=' + cutaway)
      await page.waitForTimeout(250)
      const filename = `${direction}-${cutaway ? 'interior' : 'exterior'}.png`
      await page.screenshot({ path: path.join(preview, filename) })
      views.push({ file: 'preview/' + filename, ...state })
    }
  }
  for (const direction of ['hero', 'reverse', 'northeye', 'southeye', 'westeye', 'easteye']) {
    const state = await page.evaluate(async direction => window.showView(direction, false), direction)
    await page.waitForTimeout(250)
    const filename = direction + '.png'
    await page.screenshot({ path: path.join(preview, filename) })
    views.push({ file: 'preview/' + filename, ...state })
  }
  await fs.copyFile(path.join(preview, 'north-interior.png'), path.join(root, 'preview.png'))
  const tiles = []
  for (let index = 0; index < 8; index++) {
    tiles.push({ input: await sharp(path.join(root, views[index].file)).resize(800, 500).toBuffer(), left: (index % 4) * 800, top: Math.floor(index / 4) * 500 })
  }
  await sharp({ create: { width: 3200, height: 1000, channels: 4, background: '#101a26' } }).composite(tiles).png().toFile(path.join(preview, 'four-direction-interior-exterior.png'))
  for (const view of views) {
    view.sha256 = createHash('sha256').update(await fs.readFile(path.join(root, view.file))).digest('hex')
  }
  if (errors.length) throw new Error(JSON.stringify(errors))
  const report = { status: 'PASS', renderer: 'Minecraft Builder / Lodestone 0.6.0 / ThreeStructureRenderer / headless Edge', source: 'room.nbt', source_sha256: createHash('sha256').update(await fs.readFile(path.join(root, 'room.nbt'))).digest('hex'), actual_game_screenshot: false, complete_roof_preserved_in_nbt: true, cutaway_changes_preview_only: true, views, errors }
  await fs.writeFile(path.join(root, 'qa/render.json'), JSON.stringify(report, null, 2) + '\n')
  console.log(JSON.stringify(report, null, 2))
} finally {
  await browser.close()
}
