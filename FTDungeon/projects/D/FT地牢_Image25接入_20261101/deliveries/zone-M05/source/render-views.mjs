import fs from 'node:fs/promises'
import path from 'node:path'
import { createRequire } from 'node:module'
import { createHash } from 'node:crypto'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const require = createRequire('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/runtime.cjs')
const { chromium } = require('playwright')
const browser = await chromium.launch({ executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe', headless: true, args: ['--use-angle=swiftshader', '--enable-unsafe-swiftshader'] })
const page = await browser.newPage({ viewport: { width: 1440, height: 1080 }, deviceScaleFactor: 1 })
const consoleErrors = []
page.on('pageerror', error => consoleErrors.push(error.message))
await page.route('**/src/main.js*', async route => {
  const response = await route.fetch()
  let source = await response.text()
  const needle = 'await renderer.whenReady()\n  render()'
  if (!source.includes(needle)) throw new Error('Viewer capture hook changed')
  source = source.replace(needle, 'await renderer.whenReady()\n  window.zoneM05 = {renderer, structure, Structure, LIGHTING_PRESETS};\n  render()')
  source = source.replace('asyncBuild: true,', 'preserveDrawingBuffer: true,\n    asyncBuild: true,')
  await route.fulfill({ response, body: source })
})
const sourceHash = createHash('sha256').update(await fs.readFile(path.join(root, 'room.nbt'))).digest('hex')
await page.goto('http://127.0.0.1:5365/?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=zone-M05', { waitUntil: 'networkidle', timeout: 180000 })
await page.waitForFunction(() => Boolean(window.zoneM05), { timeout: 180000 })
await page.addStyleTag({ content: 'header,button,#loading,.hud,.toolbar,.hint{display:none!important}body,main,#app{margin:0!important;padding:0!important}#preview{position:fixed!important;inset:0!important;width:100vw!important;height:100vh!important}' })
await fs.mkdir(path.join(root, 'views'), { recursive: true })
const directions = { north: [-0.55, -1], east: [1, -0.55], south: [0.55, 1], west: [-1, 0.55] }
const jobs = []
for (const [direction, vector] of Object.entries(directions)) for (const mode of ['interior', 'exterior']) jobs.push({ name: mode + '-' + direction, mode, vector, eye: [13.5 + vector[0] * 40, 34, 13.5 + vector[1] * 40], target: [13.5, 5, 13.5] })
jobs.push(
  { name: 'eye-north', mode: 'eye', eye: [13.5, 3.62, 3.5], target: [13.5, 4.2, 23] },
  { name: 'eye-east', mode: 'eye', eye: [23.5, 3.62, 13.5], target: [4, 4.2, 13.5] },
  { name: 'eye-south', mode: 'eye', eye: [13.5, 3.62, 23.5], target: [13.5, 4.2, 3] },
  { name: 'eye-west', mode: 'eye', eye: [3.5, 3.62, 13.5], target: [23, 4.2, 13.5] }
)
const captures = []
try {
  for (const job of jobs) {
    await page.evaluate(async job => {
      const { renderer, structure, Structure, LIGHTING_PRESETS } = window.zoneM05
      let visible = structure
      if (job.mode === 'interior') {
        visible = new Structure(structure.getSize())
        for (const block of structure.getBlocks()) {
          const [east, up, south] = block.pos
          if (up >= 13 || up === 12 && !block.state.getName().path.endsWith('_log')) continue
          const nearX = job.vector[0] < 0 ? east <= 2 : east >= 24
          const nearZ = job.vector[1] < 0 ? south <= 2 : south >= 24
          if (up >= 2 && (nearX || nearZ)) continue
          visible.addBlock(block.pos, block.state.getName(), block.state.getProperties(), block.nbt)
        }
      }
      renderer.setStructure(visible)
      await renderer.whenReady()
      renderer.setViewport(0, 0, 1440, 1080, 1)
      renderer.setSunlight({ ...LIGHTING_PRESETS.day, intensity: 0.7, ambientIntensity: 0.76, exposure: 1.05, fog: { density: 0 }, emissive: { range: 18, intensity: 1.7, tint: [0.8, 0.85, 1] } })
      if (job.mode === 'eye') renderer.setSunlight({ sky: { zenithColor: [0.008, 0.01, 0.015], horizonColor: [0.008, 0.01, 0.015], groundColor: [0.008, 0.01, 0.015], sunGlowIntensity: 0, stars: { enabled: false } } })
      renderer.setCamera({ position: job.eye, target: job.target, fov: job.mode === 'eye' ? 80 : 48 })
      renderer.drawStructure()
      await new Promise(resolve => requestAnimationFrame(resolve))
      renderer.drawStructure()
    }, job)
    await page.waitForTimeout(600)
    const output = path.join(root, 'views', job.name + '.png')
    await page.locator('#preview').screenshot({ path: output, timeout: 90000 })
    const bytes = await fs.readFile(output)
    captures.push({ ...job, path: path.relative(root, output), sha256: createHash('sha256').update(bytes).digest('hex'), cutaway_preview_only: job.mode === 'interior' })
    console.log('Captured ' + job.name)
  }
  await fs.copyFile(path.join(root, 'views/interior-north.png'), path.join(root, 'preview.png'))
  if (consoleErrors.length) throw new Error(consoleErrors.join('\n'))
  const afterHash = createHash('sha256').update(await fs.readFile(path.join(root, 'room.nbt'))).digest('hex')
  if (sourceHash !== afterHash) throw new Error('Source NBT changed while rendering')
  await fs.writeFile(path.join(root, 'qa/views.json'), JSON.stringify({ status: 'CAPTURED_PENDING_VISUAL_REVIEW', renderer: 'Minecraft Builder Lodestone 0.6.0 bundled viewer + Chromium WebGL', source_sha256: sourceHash, original_nbt_modified: false, interior_cut_rule: 'roof y>=13, non-timber y12 ceiling, and two camera-facing walls removed in memory only', console_errors: consoleErrors, captures }, null, 2) + '\n')
} finally { await browser.close() }
