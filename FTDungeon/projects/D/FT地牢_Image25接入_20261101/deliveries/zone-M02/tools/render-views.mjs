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
page.on('pageerror', error => { errors.push(String(error)); console.error(error) })
const target = [12.5, 6.3, 12.5]
const views = [
  { name: 'exterior-north', position: [36, 30, -33], target },
  { name: 'exterior-east', position: [58, 30, 36], target },
  { name: 'exterior-south', position: [-11, 30, 58], target },
  { name: 'exterior-west', position: [-33, 30, -11], target },
  { name: 'interior-north', position: [36, 18, -33], target: [12.5, 5.5, 12.5], cutaway: 'north', fov: 45 },
  { name: 'interior-east', position: [58, 18, 36], target: [12.5, 5.5, 12.5], cutaway: 'east', fov: 45 },
  { name: 'interior-south', position: [-11, 18, 58], target: [12.5, 5.5, 12.5], cutaway: 'south', fov: 45 },
  { name: 'interior-west', position: [-33, 18, -11], target: [12.5, 5.5, 12.5], cutaway: 'west', fov: 45 },
  { name: 'plan', position: [12.5, 52, 12.6], target: [12.5, 0, 12.5], cutaway: 'plan', fov: 40 }
]
const sourceHash = crypto.createHash('sha256').update(await fs.readFile(path.join(root, 'room.nbt'))).digest('hex')
const manifest = { id: 'zone-M02', renderer: 'Minecraft Builder / Lodestone 0.6.0 ThreeStructureRenderer', evidence_type: 'Actual NBT software render; not Minecraft player-client screenshots', source_sha256: sourceHash, views: [] }
await fs.mkdir(path.join(root, 'views'), { recursive: true })
try {
  await page.route('**/tools/render.html', async route => route.fulfill({ contentType: 'text/html; charset=utf-8', body: await fs.readFile(path.join(root, 'tools/render.html'), 'utf8') }))
  await page.goto('http://127.0.0.1:5342/@fs/' + root.replaceAll('\\', '/') + '/tools/render.html', { waitUntil: 'domcontentloaded', timeout: 120000 })
  await page.waitForFunction(() => window.renderReady === true, undefined, { timeout: 120000 })
  for (const view of views) {
    const settings = await page.evaluate(async options => window.renderView(options), view)
    const output = path.join(root, 'views', view.name + '.png')
    await page.locator('canvas').screenshot({ path: output, timeout: 60000 })
    manifest.views.push({ ...view, ...settings, path: path.relative(root, output), sha256: crypto.createHash('sha256').update(await fs.readFile(output)).digest('hex') })
    console.log('Rendered ' + view.name)
  }
  if (errors.length) throw new Error(errors.join('\n'))
  if (crypto.createHash('sha256').update(await fs.readFile(path.join(root, 'room.nbt'))).digest('hex') !== sourceHash) throw new Error('NBT changed during rendering')
  manifest.browser_errors = errors
  await fs.copyFile(path.join(root, 'views/interior-north.png'), path.join(root, 'preview.png'))
  await fs.writeFile(path.join(root, 'qa/render-manifest.json'), JSON.stringify(manifest, null, 2) + '\n')
} finally { await browser.close() }
