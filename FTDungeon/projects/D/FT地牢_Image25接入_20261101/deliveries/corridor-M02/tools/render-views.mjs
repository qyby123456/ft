import fs from 'node:fs/promises'
import path from 'node:path'
import crypto from 'node:crypto'
import { fileURLToPath } from 'node:url'
import { createRequire } from 'node:module'

const require = createRequire(import.meta.url)
const { chromium } = require('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright')
const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const browser = await chromium.launch({ headless: true, executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe', args: ['--use-angle=swiftshader', '--enable-unsafe-swiftshader', '--disable-dev-shm-usage'] })
const page = await browser.newPage({ viewport: { width: 1536, height: 1024 }, deviceScaleFactor: 1 })
const errors = []
page.on('pageerror', error => { errors.push(String(error)); console.error('Browser error:', error) })
page.on('console', message => { if (message.type() === 'error') console.error('Browser console:', message.text()) })
page.on('requestfailed', request => console.error('Request failed:', request.url(), request.failure()?.errorText))
const views = [
  { name: 'exterior-north', position: [8.5, 25, -35], target: [8.5, 4, 16] },
  { name: 'exterior-east', position: [57, 29, 20.5], target: [8.5, 5, 20.5] },
  { name: 'exterior-south', position: [8.5, 25, 76], target: [8.5, 4, 25] },
  { name: 'exterior-west', position: [-40, 29, 20.5], target: [8.5, 5, 20.5] },
  { name: 'interior-north', position: [8.5, 4.8, 1.2], target: [8.5, 4.8, 40], fov: 68 },
  { name: 'interior-east-cutaway', position: [34, 20, 21], target: [7, 4.5, 21], cutaway: 'east', fov: 62 },
  { name: 'interior-south', position: [8.5, 4.8, 39.8], target: [8.5, 4.8, 0], fov: 68 },
  { name: 'interior-west-cutaway', position: [-17, 20, 21], target: [10, 4.5, 21], cutaway: 'west', fov: 62 },
]
const manifest = { renderer: 'Minecraft Builder / Lodestone 0.6.0 ThreeStructureRenderer', evidence_type: 'Actual NBT software render; not a Minecraft client screenshot', source_sha256: crypto.createHash('sha256').update(await fs.readFile(path.join(root, 'room.nbt'))).digest('hex'), views: [] }
try {
  await page.route('**/tools/render.html', async route => route.fulfill({ contentType: 'text/html; charset=utf-8', body: await fs.readFile(path.join(root, 'tools/render.html'), 'utf8') }))
  await page.goto('http://127.0.0.1:5312/@fs/' + root.replaceAll('\\', '/') + '/tools/render.html', { waitUntil: 'domcontentloaded', timeout: 120000 })
  await page.waitForFunction(() => window.renderReady === true, undefined, { timeout: 120000 })
  for (const view of views) {
    const settings = await page.evaluate(async options => window.renderView(options), view)
    const output = path.join(root, 'views', view.name + '.png')
    await page.locator('canvas').screenshot({ path: output, timeout: 60000 })
    manifest.views.push({ ...view, ...settings, path: path.relative(root, output), sha256: crypto.createHash('sha256').update(await fs.readFile(output)).digest('hex') })
    console.log('Rendered ' + view.name)
  }
  await fs.copyFile(path.join(root, 'views/interior-north.png'), path.join(root, 'preview.png'))
  manifest.browser_errors = errors
  if (errors.length) throw new Error(errors.join('\n'))
  await fs.writeFile(path.join(root, 'qa/render-manifest.json'), JSON.stringify(manifest, null, 2) + '\n')
} finally {
  await browser.close()
}
