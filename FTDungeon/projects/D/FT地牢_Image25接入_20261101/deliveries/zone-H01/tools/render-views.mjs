import fs from 'node:fs/promises'
import path from 'node:path'
import { createRequire } from 'node:module'
import { createHash } from 'node:crypto'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const require = createRequire('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/runtime.cjs')
const { chromium } = require('playwright')
const baseUrl = process.argv[2] ?? 'http://127.0.0.1:5174/'
const browser = await chromium.launch({ executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe', headless: true, args: ['--use-angle=swiftshader', '--enable-unsafe-swiftshader'] })
const page = await browser.newPage({ viewport: { width: 1600, height: 1100 }, deviceScaleFactor: 1 })
const consoleErrors = []
page.on('pageerror', error => consoleErrors.push(error.message))
await page.route('**/src/main.js*', async route => {
  const response = await route.fetch()
  let source = await response.text()
  const needle = 'await renderer.whenReady()\n  render()'
  if (!source.includes(needle)) throw new Error('Installed viewer changed; capture control unavailable')
  source = source.replace(needle, 'await renderer.whenReady()\n  window.h01 = {renderer, structure, Structure, LIGHTING_PRESETS};\n  render()')
  source = source.replace('asyncBuild: true,', 'preserveDrawingBuffer: true,\n    asyncBuild: true,')
  await route.fulfill({ response, body: source })
})
const sourceHash = createHash('sha256').update(await fs.readFile(path.join(root, 'room.nbt'))).digest('hex')
const jobs = [
  { name: 'eye-north', mode: 'eye', eye: [14.5, 3.62, 2.25], target: [14.5, 6.0, 28.8], fov: 76 },
  { name: 'throne-detail', mode: 'eye', eye: [14.5, 7.5, 19.5], target: [14.5, 7.0, 29], fov: 68 }
]
for (const [direction, vector] of Object.entries({ north: [-0.4, -1], south: [0.4, 1], west: [-1, 0.4], east: [1, -0.4] })) {
  for (const mode of ['interior', 'exterior']) jobs.push({ name: mode + '-' + direction, mode, vector, eye: [14.5 + vector[0] * 53, 35, 16.5 + vector[1] * 53], target: [14.5, 6.0, 16.5], fov: 46 })
}
const captures = []
try {
  await page.goto(baseUrl + '?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=H01', { waitUntil: 'networkidle', timeout: 240000 })
  await page.waitForFunction(() => Boolean(window.h01), { timeout: 240000 })
  await page.addStyleTag({ content: 'header,button,#loading,.hud,.toolbar,.hint{display:none!important} body,main,#app{margin:0!important;padding:0!important} #preview{position:fixed!important;inset:0!important;width:100vw!important;height:100vh!important}' })
  for (const job of jobs) {
    await page.evaluate(async job => {
      const { renderer, structure, Structure, LIGHTING_PRESETS } = window.h01
      let visible = structure
      if (job.mode === 'interior') {
        visible = new Structure(structure.getSize())
        for (const block of structure.getBlocks()) {
          const [east, up, south] = block.pos
          if (up >= 16) continue
          const nearEast = job.vector[0] < 0 ? east <= 1 : east >= 27
          const nearSouth = job.vector[1] < 0 ? south <= 1 : south >= 31
          if (up >= 2 && (nearEast || nearSouth)) continue
          visible.addBlock(block.pos, block.state.getName(), block.state.getProperties(), block.nbt)
        }
      }
      renderer.setStructure(visible)
      await renderer.whenReady()
      renderer.setViewport(0, 0, 1600, 1100, 1)
      renderer.setSunlight({ ...LIGHTING_PRESETS.day, intensity: 0.88, ambientIntensity: 0.76, exposure: 1.06, fog: { density: 0 }, emissive: { range: 20, intensity: 1.6, tint: [1, 0.78, 0.5] } })
      if (job.mode === 'eye') renderer.setSunlight({ sky: { zenithColor: [0.012, 0.014, 0.02], horizonColor: [0.012, 0.014, 0.02], groundColor: [0.012, 0.014, 0.02], sunGlowIntensity: 0, stars: { enabled: false } } })
      renderer.setCamera({ position: job.eye, target: job.target, fov: job.fov })
      renderer.drawStructure()
      await new Promise(resolve => requestAnimationFrame(resolve))
      renderer.drawStructure()
    }, job)
    await page.waitForTimeout(650)
    const output = path.join(root, 'views', job.name + '.png')
    await page.locator('#preview').screenshot({ path: output, timeout: 120000 })
    captures.push({ ...job, path: path.relative(root, output), sha256: createHash('sha256').update(await fs.readFile(output)).digest('hex'), cutaway_preview_only: job.mode === 'interior' })
    console.log('Captured ' + job.name)
  }
  await fs.copyFile(path.join(root, 'views/eye-north.png'), path.join(root, 'preview.png'))
  if (consoleErrors.length) throw new Error(consoleErrors.join('\n'))
  if (sourceHash !== createHash('sha256').update(await fs.readFile(path.join(root, 'room.nbt'))).digest('hex')) throw new Error('NBT changed during rendering')
  await fs.writeFile(path.join(root, 'qa/views.json'), JSON.stringify({ status: 'PASS', renderer: 'Minecraft Builder / Lodestone 0.6.0 / Chromium WebGL', source_sha256: sourceHash, original_nbt_modified: false, interior_cut_rule: 'Only in memory: remove roof y>=16 and near two-block outer walls at y>=2. Preserve all columns, beam ribs, stairs and furnishings.', console_errors: consoleErrors, captures }, null, 2) + '\n')
} finally {
  await browser.close()
}
