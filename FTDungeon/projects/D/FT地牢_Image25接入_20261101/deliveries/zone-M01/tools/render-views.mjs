import fs from 'node:fs/promises'
import path from 'node:path'
import { createRequire } from 'node:module'
import { createHash } from 'node:crypto'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const require = createRequire('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/runtime.cjs')
const { chromium } = require('playwright')
const browser = await chromium.launch({ executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe', headless: true, args: ['--use-angle=swiftshader', '--enable-unsafe-swiftshader'] })
const page = await browser.newPage({ viewport: { width: 1440, height: 1000 }, deviceScaleFactor: 1 })
const consoleErrors = []
page.on('pageerror', error => consoleErrors.push(error.message))
await page.route('**/src/main.js*', async route => {
  const response = await route.fetch()
  let source = await response.text()
  const needle = 'await renderer.whenReady()\n  render()'
  if (!source.includes(needle)) throw new Error('Installed viewer changed; cannot expose capture controls')
  source = source.replace(needle, 'await renderer.whenReady()\n  window.zoneM01 = {renderer, structure, Structure, LIGHTING_PRESETS};\n  render()')
  source = source.replace('asyncBuild: true,', 'preserveDrawingBuffer: true,\n    asyncBuild: true,')
  await route.fulfill({ response, body: source })
})
await page.goto('http://127.0.0.1:5261/?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=zone-M01', { waitUntil: 'networkidle', timeout: 180000 })
await page.waitForFunction(() => Boolean(window.zoneM01), { timeout: 180000 })
await page.addStyleTag({ content: 'header, button, #loading, .hud, .toolbar, .hint {display:none!important} body,main,#app{margin:0!important;padding:0!important} #preview{position:fixed!important;inset:0!important;width:100vw!important;height:100vh!important}' })
await fs.mkdir(path.join(root, 'views'), { recursive: true })
const jobs = [
  { name: 'eye-south', mode: 'eye', eye: [13.5, 3.62, 21], target: [13.5, 3.7, 1] },
  { name: 'eye-east', mode: 'eye', eye: [25.25, 3.62, 9.5], target: [1, 3.7, 9.5] }
]
for (const [direction, vector] of Object.entries({ north: [-0.75, -1], east: [1, -0.75], south: [0.75, 1], west: [-1, 0.75] })) {
  for (const mode of ['interior', 'exterior']) jobs.push({ name: mode + '-' + direction, mode, vector, eye: [13.5 + vector[0] * 34, 29, 11.5 + vector[1] * 34], target: [13.5, 3.5, 11.5] })
}
const captures = []
try {
  for (const job of jobs) {
    await page.evaluate(async job => {
      const { renderer, structure, Structure, LIGHTING_PRESETS } = window.zoneM01
      let visible = structure
      if (job.mode === 'interior') {
        visible = new Structure(structure.getSize())
        for (const block of structure.getBlocks()) {
          const [east, up, south] = block.pos
          if (up >= 9) continue
          const nearX = job.vector[0] < 0 ? east <= 0 : east >= 26
          const nearZ = job.vector[1] < 0 ? south <= 0 : south >= 22
          if (up >= 2 && (nearX || nearZ)) continue
          visible.addBlock(block.pos, block.state.getName(), block.state.getProperties(), block.nbt)
        }
      }
      renderer.setStructure(visible)
      await renderer.whenReady()
      renderer.setViewport(0, 0, 1440, 1000, 1)
      renderer.setSunlight({ ...LIGHTING_PRESETS.day, intensity: 0.85, ambientIntensity: 0.70, exposure: 1.02, fog: { density: 0 }, emissive: { range: 18, intensity: 1.4, tint: [1, 0.8, 0.55] } })
      if (job.mode === 'eye') renderer.setSunlight({ sky: { zenithColor: [0.008, 0.008, 0.008], horizonColor: [0.008, 0.008, 0.008], groundColor: [0.008, 0.008, 0.008], sunGlowIntensity: 0, stars: { enabled: false } } })
      renderer.setCamera({ position: job.eye, target: job.target, fov: job.mode === 'eye' ? 72 : 48 })
      renderer.drawStructure()
      await new Promise(resolve => requestAnimationFrame(resolve))
      renderer.drawStructure()
    }, job)
    await page.waitForTimeout(500)
    const output = path.join(root, 'views', job.name + '.png')
    await page.locator('#preview').screenshot({ path: output, timeout: 90000 })
    const bytes = await fs.readFile(output)
    captures.push({ ...job, path: path.relative(root, output), sha256: createHash('sha256').update(bytes).digest('hex'), cutaway_preview_only: job.mode === 'interior' })
    console.log('Captured ' + job.name)
  }
  await fs.copyFile(path.join(root, 'views/interior-south.png'), path.join(root, 'preview.png'))
  if (consoleErrors.length) throw new Error(consoleErrors.join('\n'))
  const sourceHash = createHash('sha256').update(await fs.readFile(path.join(root, 'room.nbt'))).digest('hex')
  await fs.writeFile(path.join(root, 'qa/views.json'), JSON.stringify({ status: 'PASS', renderer: 'Minecraft Builder Lodestone 0.6.0 bundled viewer + Chromium WebGL', source_sha256: sourceHash, original_nbt_modified: false, interior_cut_rule: 'roof/ceiling y>=9 and the two near boundary walls removed in memory only; timber structure and furnishings retained', console_errors: consoleErrors, captures }, null, 2) + '\n')
} finally {
  await browser.close()
}
