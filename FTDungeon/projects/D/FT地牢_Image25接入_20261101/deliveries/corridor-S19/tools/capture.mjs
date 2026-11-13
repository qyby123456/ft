import fs from 'node:fs/promises'
import path from 'node:path'
import assert from 'node:assert/strict'
import { createHash } from 'node:crypto'
import { createRequire } from 'node:module'
import { fileURLToPath, pathToFileURL } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const modules = 'C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules'
const { chromium } = await import(pathToFileURL(`${modules}/playwright/index.mjs`))
const sharp = createRequire(`${modules}/../package.json`)('sharp')
const base = process.argv[2]
assert(base, 'Pass the skill preview server URL')
const url = new URL('/s19.html', base).href
const browser = await chromium.launch({ channel: 'msedge', headless: true, args: ['--use-angle=swiftshader', '--enable-unsafe-swiftshader'] })
const page = await browser.newPage({ viewport: { width: 1536, height: 1024 }, deviceScaleFactor: 1 })
const errors = []
page.on('pageerror', error => { errors.push(error.message); console.log('PAGE_ERROR ' + error.message) })
page.on('console', message => { if (message.type() === 'error') console.log('CONSOLE_ERROR ' + message.text()) })
const views = []
try {
  await page.route('**/s19.html', route => route.fulfill({ path: path.join(root, 'tools/viewer.html'), contentType: 'text/html; charset=utf-8' }))
  await page.route('**/viewer.mjs', route => route.fulfill({ path: path.join(root, 'tools/viewer.mjs'), contentType: 'text/javascript; charset=utf-8' }))
  await page.goto(url, { waitUntil: 'networkidle', timeout: 180000 })
  await page.waitForFunction(() => window.s19Ready === true, null, { timeout: 120000 }).catch(async error => {
    console.log(JSON.stringify({ errors, status: await page.locator('#status').textContent() }))
    throw error
  })
  const evidence = await page.evaluate(() => window.renderEvidence)
  const expected = createHash('sha256').update(await fs.readFile(path.join(root, 'room.nbt'))).digest('hex')
  assert.equal(evidence.fingerprint, expected)
  for (const view of ['interior', 'north', 'south', 'west', 'east', 'exterior', 'floorplan']) {
    const result = await page.evaluate(view => window.renderView(view), view)
    await page.waitForTimeout(350)
    const destination = path.join(root, view === 'interior' ? 'preview.png' : `qa/${view}.png`)
    await page.screenshot({ path: destination })
    views.push({ ...result, path: destination, sha256: createHash('sha256').update(await fs.readFile(destination)).digest('hex') })
  }
  const tiles = []
  for (const [index, view] of ['north', 'south', 'west', 'east'].entries()) {
    const image = await sharp(path.join(root, `qa/${view}.png`)).resize(1152, 768).toBuffer()
    tiles.push({ input: image, left: index % 2 * 1152, top: Math.floor(index / 2) * 768 })
  }
  await sharp({ create: { width: 2304, height: 1536, channels: 3, background: '#11121a' } }).composite(tiles).png().toFile(path.join(root, 'qa/four-views.png'))
  assert.deepEqual(errors, [])
  await fs.writeFile(path.join(root, 'qa/render.json'), JSON.stringify({ status: 'PASS', nbt_sha256: expected, engine: 'Lodestone 0.6.0 / Three.js / Edge headless WebGL SwiftShader', source: 'final room.nbt, not concept art', original_records: evidence.originalRecords, views, errors, full_modpack_screenshot: false, cutaways_modify_final_nbt: false }, null, 2) + '\n')
  console.log(JSON.stringify({ status: 'PASS', nbt_sha256: expected, views: views.length, preview: path.join(root, 'preview.png'), viewer: url }))
} finally {
  await browser.close()
}
