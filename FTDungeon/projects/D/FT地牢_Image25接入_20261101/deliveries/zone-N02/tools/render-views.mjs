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
const errors = []
page.on('pageerror', error => errors.push(error.message))
await page.route('**/src/main.js*', async route => {
  const response = await route.fetch()
  let source = await response.text()
  const needle = 'await renderer.whenReady()\n  render()'
  if (!source.includes(needle)) throw new Error('Viewer capture hook not found')
  source = source.replace(needle, 'await renderer.whenReady()\n  window.n02 = { renderer, structure, Structure, LIGHTING_PRESETS };\n  render()')
  source = source.replace('asyncBuild: true,', 'preserveDrawingBuffer: true,\n    asyncBuild: true,')
  await route.fulfill({ response, body: source })
})
await page.goto('http://127.0.0.1:5392/?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=N02', { waitUntil: 'networkidle', timeout: 180000 })
await page.waitForFunction(() => Boolean(window.n02), { timeout: 180000 })
await page.addStyleTag({ content: 'header,button,#loading,.hud,.toolbar,.hint{display:none!important}body,main,#app{margin:0!important;padding:0!important}#preview{position:fixed!important;inset:0!important;width:100vw!important;height:100vh!important}' })
await fs.mkdir(path.join(root, 'views'), { recursive: true })
const jobs = []
for (const [direction, vector] of Object.entries({ north: [-0.35, -1], east: [1, -0.35], south: [0.35, 1], west: [-1, 0.35] })) {
  for (const mode of ['interior', 'exterior']) jobs.push({ name: mode + '-' + direction, direction, mode, vector, eye: [16.5 + vector[0] * 52, 42, 15.5 + vector[1] * 52], target: [16.5, 5.8, 15.5] })
}
for (const [name, eye, target] of [
  ['eye-north', [16.5, 3.62, 1.2], [16.5, 4, 29]],
  ['eye-platform', [20.5, 3.62, 24.5], [8, 3.5, 4]],
  ['eye-branch', [30, 3.62, 23.5], [25, 3.7, 6]]
]) jobs.push({ name, mode: 'eye', eye, target })
const captures = []
try {
  for (const job of jobs) {
    await page.evaluate(async job => {
      const { renderer, structure, Structure, LIGHTING_PRESETS } = window.n02
      let visible = structure
      if (job.mode === 'interior') {
        visible = new Structure(structure.getSize())
        for (const block of structure.getBlocks()) {
          const [east, up, south] = block.pos
          if (up >= 11) continue
          const nearEast = job.vector[0] < 0 ? east === 0 : east === 32
          const nearSouth = job.vector[1] < 0 ? south === 0 : south === 30
          if (up >= 2 && (nearEast || nearSouth)) continue
          visible.addBlock(block.pos, block.state.getName(), block.state.getProperties(), block.nbt)
        }
      }
      renderer.setStructure(visible)
      await renderer.whenReady()
      renderer.setViewport(0, 0, 1440, 1080, 1)
      renderer.setSunlight({ ...LIGHTING_PRESETS.day, intensity: 0.78, ambientIntensity: 0.72, exposure: 1.02, fog: { density: 0 }, emissive: { range: 18, intensity: 1.45, tint: [1, 0.8, 0.55] } })
      renderer.setCamera({ position: job.eye, target: job.target, fov: job.mode === 'eye' ? 78 : 48 })
      renderer.drawStructure()
      await new Promise(resolve => requestAnimationFrame(resolve))
      renderer.drawStructure()
    }, job)
    await page.waitForTimeout(450)
    const output = path.join(root, 'views', job.name + '.png')
    await page.locator('#preview').screenshot({ path: output, timeout: 90000 })
    captures.push({ ...job, path: path.relative(root, output), sha256: createHash('sha256').update(await fs.readFile(output)).digest('hex'), preview_cutaway_only: job.mode === 'interior' })
    console.log('Captured ' + job.name)
  }
  await fs.copyFile(path.join(root, 'views/interior-south.png'), path.join(root, 'preview.png'))
  if (errors.length) throw new Error(errors.join('\n'))
  const source = createHash('sha256').update(await fs.readFile(path.join(root, 'room.nbt'))).digest('hex')
  await fs.writeFile(path.join(root, 'qa/views.json'), JSON.stringify({ status: 'PASS', renderer: 'Minecraft Builder / Lodestone 0.6.0 bundled viewer / Chromium WebGL', source_sha256: source, original_nbt_modified: false, interior_cut_rule: 'Preview only: remove y>=11 roof/upper ribs and camera-facing boundary walls at y>=2; retain all interior columns, track, furnishings and structural full NBT.', captures, page_errors: errors }, null, 2) + '\n')
} finally {
  await browser.close()
}
