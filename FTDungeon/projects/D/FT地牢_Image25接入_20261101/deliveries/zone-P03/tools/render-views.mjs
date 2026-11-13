import fs from 'node:fs/promises'
import path from 'node:path'
import { createRequire } from 'node:module'
import { createHash } from 'node:crypto'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const require = createRequire('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/runtime.cjs')
const { chromium } = require('playwright')
const browser = await chromium.launch({ executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe', headless: true, args: ['--use-angle=swiftshader', '--enable-unsafe-swiftshader'] })
const page = await browser.newPage({ viewport: { width: 1500, height: 1100 }, deviceScaleFactor: 1 })
const errors = []
page.on('pageerror', error => errors.push(error.message))
await page.route('**/src/main.js*', async route => {
  const response = await route.fetch()
  let source = await response.text()
  const needle = 'await renderer.whenReady()\n  render()'
  if (!source.includes(needle)) throw new Error('Viewer capture hook changed')
  source = source.replace(needle, 'await renderer.whenReady()\n  window.p03 = {renderer, structure, Structure, LIGHTING_PRESETS};\n  render()')
  source = source.replace('asyncBuild: true,', 'preserveDrawingBuffer: true,\n    asyncBuild: true,')
  await route.fulfill({ response, body: source })
})
await page.goto('http://127.0.0.1:5213/?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=P03', { waitUntil: 'networkidle', timeout: 180000 })
await page.waitForFunction(() => Boolean(window.p03), { timeout: 180000 })
await page.addStyleTag({ content: 'header, button, #loading, .hud, .toolbar, .hint {display:none!important} body,main,#app{margin:0!important;padding:0!important} #preview{position:fixed!important;inset:0!important;width:100vw!important;height:100vh!important}' })
await fs.mkdir(path.join(root, 'views'), { recursive: true })
const jobs = [
  { name: 'eye-north', mode: 'eye', eye: [13.5, 6.62, 2.4], target: [13.5, 5.2, 24] },
  { name: 'eye-south', mode: 'eye', eye: [13.5, 6.62, 26.5], target: [13.5, 5.2, 4] },
  { name: 'recovery', mode: 'eye', eye: [8.5, 4.62, 12.5], target: [6.5, 4.2, 5.5] }
]
for (const [direction, vector] of Object.entries({ north: [-0.6, -1], east: [1, -0.6], south: [0.6, 1], west: [-1, 0.6] })) {
  for (const mode of ['interior', 'exterior']) jobs.push({ name: mode + '-' + direction, mode, vector, eye: [13.5 + vector[0] * 42, 38, 14.5 + vector[1] * 42], target: [13.5, 4.2, 14.5] })
}
const captures = []
try {
  for (const job of jobs) {
    await page.evaluate(async job => {
      const { renderer, structure, Structure, LIGHTING_PRESETS } = window.p03
      let visible = structure
      if (job.mode === 'interior') {
        visible = new Structure(structure.getSize())
        for (const block of structure.getBlocks()) {
          const [east, up, south] = block.pos
          if (up >= 11) continue
          const nearEast = job.vector[0] < 0 ? east <= 1 : east >= 25
          const nearSouth = job.vector[1] < 0 ? south <= 1 : south >= 27
          if (up >= 3 && (nearEast || nearSouth)) continue
          visible.addBlock(block.pos, block.state.getName(), block.state.getProperties(), block.nbt)
        }
      }
      renderer.setStructure(visible)
      await renderer.whenReady()
      renderer.setViewport(0, 0, 1500, 1100, 1)
      renderer.setSunlight({ ...LIGHTING_PRESETS.day, intensity: 0.82, ambientIntensity: 0.75, exposure: 1.08, fog: { density: 0 }, emissive: { range: 16, intensity: 1.4, tint: [1, 0.75, 0.45] } })
      renderer.setCamera({ position: job.eye, target: job.target, fov: job.mode === 'eye' ? 75 : 46 })
      renderer.drawStructure()
      await new Promise(resolve => requestAnimationFrame(resolve))
      renderer.drawStructure()
    }, job)
    await page.waitForTimeout(500)
    const output = path.join(root, 'views', job.name + '.png')
    await page.locator('#preview').screenshot({ path: output, timeout: 90000 })
    captures.push({ ...job, file: path.relative(root, output), sha256: createHash('sha256').update(await fs.readFile(output)).digest('hex'), cutaway_preview_only: job.mode === 'interior' })
    console.log('P03 captured ' + job.name)
  }
  if (errors.length) throw new Error(errors.join('\n'))
  await fs.copyFile(path.join(root, 'views/interior-north.png'), path.join(root, 'preview.png'))
  const sourceHash = createHash('sha256').update(await fs.readFile(path.join(root, 'room.nbt'))).digest('hex')
  await fs.writeFile(path.join(root, 'qa/views.json'), JSON.stringify({ status: 'PASS', renderer: 'Minecraft Builder Lodestone 0.6.0 + headless Chromium WebGL', source_sha256: sourceHash, original_nbt_modified: false, cut_rule: 'y>=11 roof plus nearest double exterior walls at y>=3 removed in memory only', console_errors: errors, captures }, null, 2) + '\n')
} finally {
  await browser.close()
}
