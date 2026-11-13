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
const errors = []
page.on('pageerror', error => errors.push(error.message))
await page.route('**/src/main.js*', async route => {
  const response = await route.fetch()
  let source = await response.text()
  const needle = 'await renderer.whenReady()\n  render()'
  if (!source.includes(needle)) throw new Error('Installed viewer changed')
  source = source.replace(needle, 'await renderer.whenReady()\n  window.c03 = {renderer, structure, Structure, LIGHTING_PRESETS};\n  render()')
  source = source.replace('asyncBuild: true,', 'preserveDrawingBuffer: true,\n    asyncBuild: true,')
  await route.fulfill({ response, body: source })
})
const jobs = []
for (const [direction, vector] of Object.entries({ north: [0.65, -1], east: [1, 0.65], south: [-0.65, 1], west: [-1, -0.65] })) {
  for (const mode of ['interior', 'exterior']) jobs.push({ name: mode + '-' + direction, mode, vector, eye: [9.5 + vector[0] * 26, 23, 9.5 + vector[1] * 26], target: [9.5, 4.2, 9.5] })
}
jobs.push({ name: 'eye-north', mode: 'eye', eye: [9.5, 3.62, 0.45], target: [8.5, 4.2, 15.5] })
jobs.push({ name: 'eye-east', mode: 'eye', eye: [18.55, 3.62, 9.5], target: [3.5, 4.2, 12.5] })
jobs.push({ name: 'shrine-detail', mode: 'detail', eye: [11.5, 7, 7.5], target: [5.2, 5, 15] })
const captures = []
try {
  await page.goto('http://127.0.0.1:5273/?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=C03', { waitUntil: 'networkidle', timeout: 180000 })
  await page.waitForFunction(() => Boolean(window.c03), { timeout: 180000 })
  await page.addStyleTag({ content: 'header, button, #loading, .hud, .toolbar, .hint {display:none!important} body,main,#app{margin:0!important;padding:0!important} #preview{position:fixed!important;inset:0!important;width:100vw!important;height:100vh!important}' })
  await fs.mkdir(path.join(root, 'views'), { recursive: true })
  for (const job of jobs) {
    await page.evaluate(async job => {
      const { renderer, structure, Structure, LIGHTING_PRESETS } = window.c03
      let visible = structure
      if (job.mode === 'interior') {
        visible = new Structure(structure.getSize())
        for (const block of structure.getBlocks()) {
          const [east, up, south] = block.pos
          if (up >= 10) continue
          const nearX = job.vector[0] < 0 ? east <= 2 : east >= 16
          const nearZ = job.vector[1] < 0 ? south <= 2 : south >= 16
          if (up >= 2 && (nearX || nearZ)) continue
          visible.addBlock(block.pos, block.state.getName(), block.state.getProperties(), block.nbt)
        }
      }
      renderer.setStructure(visible)
      await renderer.whenReady()
      renderer.setViewport(0, 0, 1440, 1000, 1)
      renderer.setSunlight({ ...LIGHTING_PRESETS.day, direction: [0.35, 0.85, -0.35], intensity: 0.85, ambientIntensity: 0.78, exposure: 1.03, fog: { density: 0 }, sky: { zenithColor: [0.025, 0.035, 0.035], horizonColor: [0.075, 0.085, 0.08], groundColor: [0.035, 0.045, 0.04], sunGlowIntensity: 0, stars: { enabled: false } }, emissive: { range: 18, intensity: 1.35, tint: [1, 0.81, 0.54] } })
      renderer.setCamera({ position: job.eye, target: job.target, fov: ['eye', 'detail'].includes(job.mode) ? 70 : 42 })
      renderer.drawStructure()
      await new Promise(resolve => requestAnimationFrame(resolve))
      renderer.drawStructure()
    }, job)
    await page.waitForTimeout(500)
    const output = path.join(root, 'views', job.name + '.png')
    await page.locator('#preview').screenshot({ path: output, timeout: 90000 })
    captures.push({ ...job, file: 'views/' + job.name + '.png', sha256: createHash('sha256').update(await fs.readFile(output)).digest('hex'), cutaway_preview_only: job.mode === 'interior' })
    console.log('Captured ' + job.name)
  }
  await fs.copyFile(path.join(root, 'views/interior-north.png'), path.join(root, 'preview.png'))
  if (errors.length) throw new Error(errors.join('\n'))
  const sourceHash = createHash('sha256').update(await fs.readFile(path.join(root, 'room.nbt'))).digest('hex')
  await fs.writeFile(path.join(root, 'qa/views.json'), JSON.stringify({ status: 'PASS', renderer: 'Minecraft Builder Lodestone 0.6.0 bundled viewer + Chromium WebGL', source_nbt_sha256: sourceHash, original_nbt_modified: false, width: 1440, height: 1000, captures, cutaway: 'Interior screenshots hide y>=10 and the two near-side wall/decor strips in memory only. Exterior and eye views load the complete source NBT.' }, null, 2) + '\n')
} finally {
  await browser.close()
}

