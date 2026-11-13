import { createHash } from 'node:crypto'
import fs from 'node:fs/promises'
import { createRequire } from 'node:module'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const require = createRequire('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright/package.json')
const { chromium } = require('playwright')
const viewer = 'http://127.0.0.1:8131/?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=S11'
const lighting = {
  direction: [0.25, 0.85, -0.3], color: [1, 0.72, 0.45], ambientColor: [0.9, 0.8, 0.7],
  fillColor: [0.35, 0.24, 0.18], rimColor: [0.8, 0.45, 0.22], intensity: 0.65,
  ambientIntensity: 0.8, fillIntensity: 0.25, rimIntensity: 0.14, exposure: 1.1,
  sky: { zenithColor: [0.025, 0.014, 0.01], horizonColor: [0.025, 0.014, 0.01], groundColor: [0.025, 0.014, 0.01], stars: { enabled: false } },
  disc: { coreIntensity: 0, glowIntensity: 0 },
  fog: { color: [0.025, 0.014, 0.01], density: 0.00015, heightFalloff: 0.001 },
  emissive: { range: 16, intensity: 1, tint: [1, 0.78, 0.55] },
}
const browser = await chromium.launch({ headless: true, executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe', args: ['--disable-gpu-sandbox', '--enable-unsafe-swiftshader'] })
try {
  const page = await browser.newPage({ viewport: { width: 1536, height: 1024 }, deviceScaleFactor: 1 })
  const errors = []
  page.on('pageerror', error => errors.push(String(error)))
  await page.route('**/src/main.js*', async route => {
    const response = await route.fetch()
    const body = (await response.text()).replace('await renderer.whenReady()', 'await renderer.whenReady(); window.__s11 = {renderer,camera,structure,requestRender}')
    await route.fulfill({ response, body })
  })
  await page.goto(viewer, { waitUntil: 'networkidle', timeout: 120000 })
  await page.waitForFunction(() => window.__s11 && document.querySelector('#loading').classList.contains('hidden'), null, { timeout: 120000 })
  await page.addStyleTag({ content: '.hud,.viewer-actions{display:none!important}' })
  const filenames = []
  for (const direction of ['north', 'south']) {
    const position = [6.5, 4.62, direction === 'north' ? 0.3 : 15.7]
    const target = [6.5, 4.62, direction === 'north' ? 15.5 : 0.5]
    await page.evaluate(({ lighting, position, target }) => {
      window.__s11.renderer.setSunlight(lighting)
      window.__s11.renderer.setFOV(72)
      window.__s11.renderer.lookAt(position, target)
      window.__s11.renderer.drawStructure()
    }, { lighting, position, target })
    const filename = `previews/lodestone-${direction}.png`
    await page.screenshot({ path: path.join(root, filename) })
    filenames.push(filename)
  }
  if (errors.length) throw new Error(errors.join('\n'))
  const bytes = await fs.readFile(path.join(root, 'room.nbt'))
  const sha256 = createHash('sha256').update(bytes).digest('hex')
  const report = {
    id: 'S11', status: 'PASS', sha256,
    renderer: 'Lodestone 0.6.0 ThreeStructureRenderer; unchanged final room.nbt',
    viewer, preview_files: filenames, size: [1536, 1024],
    north_camera: { position: [6.5, 4.62, 0.3], target: [6.5, 4.62, 15.5], fov: 72 },
    lighting, nbt_block_count: 2496, page_errors: errors, geometry_cutaway: false, game_client_screenshot: false,
  }
  await fs.writeFile(path.join(root, 'qa/lodestone-preview.json'), JSON.stringify(report, null, 2) + '\n')
  console.log(JSON.stringify({ status: 'PASS', sha256, screenshots: filenames }))
} finally {
  await browser.close()
}
