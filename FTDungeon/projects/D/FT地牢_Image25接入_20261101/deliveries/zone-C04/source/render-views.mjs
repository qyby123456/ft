import fs from 'node:fs/promises'
import path from 'node:path'
import { createRequire } from 'node:module'
import { createHash } from 'node:crypto'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const require = createRequire('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/runtime.cjs')
const { chromium } = require('playwright')
const browser = await chromium.launch({ executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe', headless: true, args: ['--use-angle=swiftshader', '--enable-unsafe-swiftshader'] })
const page = await browser.newPage({ viewport: { width: 1440, height: 1100 }, deviceScaleFactor: 1 })
const errors = []
page.on('pageerror', error => errors.push(error.message))
await page.route('**/src/main.js*', async route => {
  const response = await route.fetch()
  let source = await response.text()
  const needle = 'await renderer.whenReady()\n  render()'
  if (!source.includes(needle)) throw new Error('Viewer hook changed')
  source = source.replace(needle, 'await renderer.whenReady()\n  window.c04 = {renderer, structure, Structure, LIGHTING_PRESETS};\n  render()')
  source = source.replace('asyncBuild: true,', 'preserveDrawingBuffer: true,\n    asyncBuild: true,')
  await route.fulfill({ response, body: source })
})
const port = process.argv[2] ?? '5244'
await page.goto(`http://127.0.0.1:${port}/?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=C04`, { waitUntil: 'networkidle', timeout: 180000 })
await page.waitForFunction(() => Boolean(window.c04), { timeout: 180000 })
await page.addStyleTag({ content: 'header,button,#loading,.hud,.toolbar,.hint{display:none!important}body,main,#app{margin:0!important;padding:0!important}#preview{position:fixed!important;inset:0!important;width:100vw!important;height:100vh!important}' })
const jobs = []
for (const [direction, vector] of Object.entries({ north: [-0.65, -1], east: [1, -0.65], south: [0.65, 1], west: [-1, 0.65] })) {
  for (const mode of ['interior', 'exterior']) jobs.push({ name: `${mode}-${direction}`, mode, vector, eye: [12.5 + vector[0] * 36, 34, 12.5 + vector[1] * 36], target: [12.5, 4.5, 12.5] })
}
jobs.push({ name: 'eye-north', mode: 'eye', eye: [12.5, 2.62, 1.1], target: [12.5, 3.2, 18] })
jobs.push({ name: 'eye-east', mode: 'eye', eye: [23.9, 2.62, 12.5], target: [7, 3, 12.5] })
const captures = []
try {
  for (const job of jobs) {
    await page.evaluate(async job => {
      const { renderer, structure, Structure, LIGHTING_PRESETS } = window.c04
      let visible = structure
      if (job.mode === 'interior') {
        visible = new Structure(structure.getSize())
        for (const block of structure.getBlocks()) {
          const [east, up, south] = block.pos
          if (up >= 10) continue
          const nearEast = job.vector[0] < 0 ? east <= 1 : east >= 23
          const nearSouth = job.vector[1] < 0 ? south <= 1 : south >= 23
          if (up >= 1 && (nearEast || nearSouth)) continue
          visible.addBlock(block.pos, block.state.getName(), block.state.getProperties(), block.nbt)
        }
      }
      renderer.setStructure(visible)
      await renderer.whenReady()
      renderer.setViewport(0, 0, 1440, 1100, 1)
      renderer.setSunlight({ ...LIGHTING_PRESETS.day, intensity: 0.82, ambientIntensity: 0.72, exposure: 1.1, fog: { density: 0 }, emissive: { range: 18, intensity: 1.35, tint: [1, 0.8, 0.55] } })
      renderer.setCamera({ position: job.eye, target: job.target, fov: job.mode === 'eye' ? 74 : 47 })
      renderer.drawStructure()
      await new Promise(resolve => requestAnimationFrame(resolve))
      renderer.drawStructure()
    }, job)
    await page.waitForTimeout(400)
    const output = path.join(root, 'preview', job.name + '.png')
    await page.locator('#preview').screenshot({ path: output, timeout: 90000 })
    captures.push({ ...job, path: path.relative(root, output), sha256: createHash('sha256').update(await fs.readFile(output)).digest('hex'), cutaway_preview_only: job.mode === 'interior' })
    console.log('Captured ' + job.name)
  }
  if (errors.length) throw new Error(errors.join('\n'))
  await fs.copyFile(path.join(root, 'preview/interior-north.png'), path.join(root, 'preview.png'))
  await fs.writeFile(path.join(root, 'qa/views.json'), JSON.stringify({ id: 'zone-C04', status: 'PASS', renderer: 'Minecraft Builder Lodestone 0.6.0 bundled viewer + Chromium WebGL', source_sha256: createHash('sha256').update(await fs.readFile(path.join(root, 'room.nbt'))).digest('hex'), original_nbt_modified: false, cutaway_rule: 'Only camera-near outer walls and y>=10 removed in memory for interior views; exterior and eye use complete NBT', captures, browser_errors: errors }, null, 2) + '\n')
} finally {
  await browser.close()
}
