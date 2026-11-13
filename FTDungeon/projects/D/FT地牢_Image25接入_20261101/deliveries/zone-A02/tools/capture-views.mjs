import { createRequire } from 'node:module'
import { readFile, writeFile, mkdir } from 'node:fs/promises'
import { createHash } from 'node:crypto'
import { fileURLToPath } from 'node:url'
import path from 'node:path'
const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const require = createRequire(import.meta.url)
const { chromium } = require('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright-core')
const digest = bytes => createHash('sha256').update(bytes).digest('hex')
const fingerprint = digest(await readFile(path.join(root, 'room.nbt')))
const baseUrl = process.argv[2]
if (!baseUrl) throw new Error('Pass the Minecraft Builder preview URL')
const captures = []
const errors = []
const directions = { north: [-12, 29, -31], east: [52, 29, -12], south: [33, 29, 52], west: [-31, 29, 33] }
await mkdir(path.join(root, 'views'), { recursive: true })
const browser = await chromium.launch({ executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe', headless: true, args: ['--enable-unsafe-swiftshader', '--use-angle=swiftshader', '--disable-dev-shm-usage'] })
try {
  const page = await browser.newPage({ viewport: { width: 1536, height: 1024 }, deviceScaleFactor: 1 })
  page.on('pageerror', error => errors.push(String(error)))
  await page.route('**/src/main.js', async route => {
    const response = await route.fetch()
    let source = await response.text()
    source = source.replace('const size = structure.getSize()', 'const size = structure.getSize(); const viewParams = new URLSearchParams(location.search); const eye = JSON.parse(viewParams.get("eye")); let clipped = 0; if(viewParams.get("view") === "interior") { for(const block of structure.getBlocks()) { const [px, py, pz] = block.pos; if(py >= 10 || py >= 2 && (eye[0] < 10 ? px === 0 : px === 20) || py >= 2 && (eye[2] < 10 ? pz === 0 : pz === 20)) { if(!block.state.is("minecraft:air")) clipped++; structure.addBlock(block.pos, "minecraft:air"); } } }; window.a02ClipCount = clipped')
    source = source.replace('await renderer.whenReady()', 'window.a02Camera = (eye, target) => { renderer.setCamera({position:eye,target,fov:34}); renderer.drawStructure() }; await renderer.whenReady()')
    await route.fulfill({ response, body: source })
  })
  for (const mode of ['interior', 'exterior']) {
    for (const [direction, eye] of Object.entries(directions)) {
      const url = new URL(baseUrl)
      url.searchParams.set('view', mode)
      url.searchParams.set('eye', JSON.stringify(eye))
      url.searchParams.set('name', 'A02 collapsed bedroom | ' + mode + ' ' + direction)
      await page.goto(url.toString(), { waitUntil: 'domcontentloaded', timeout: 120000 })
      await page.waitForFunction(() => document.getElementById('loading')?.classList.contains('hidden') && typeof window.a02Camera === 'function', { timeout: 120000 })
      const servedHash = await page.evaluate(async () => {
        const bytes = await (await fetch('/__minecraft_builder__/build.nbt')).arrayBuffer()
        return Array.from(new Uint8Array(await crypto.subtle.digest('SHA-256', bytes))).map(value => value.toString(16).padStart(2, '0')).join('')
      })
      if (servedHash !== fingerprint) throw new Error('Preview NBT differs from the delivered NBT')
      await page.evaluate(eye => window.a02Camera(eye, [10.5, 4.3, 10.5]), eye)
      await page.evaluate(() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve))))
      const output = path.join(root, 'views', mode + '-' + direction + '.png')
      await page.screenshot({ path: output })
      captures.push({ mode, direction, camera: eye, target: [10.5, 4.3, 10.5], file: 'views/' + mode + '-' + direction + '.png', sha256: digest(await readFile(output)), source_nbt_sha256: servedHash, clipped_non_air_cells: await page.evaluate(() => window.a02ClipCount) })
      console.log('CAPTURE ' + mode + ' ' + direction)
    }
  }
  if (errors.length) throw new Error(errors.join('\n'))
  await writeFile(path.join(root, 'qa/visual.json'), JSON.stringify({ status: 'RENDERED_AWAITING_VISUAL_INSPECTION', sha256: fingerprint, renderer: 'Minecraft Builder / Lodestone 0.6.0 / headless Edge WebGL', reference_is_gameplay_screenshot: false, source_nbt_verified_by_sha256: true, cutaway_is_viewer_only: true, exterior_modifies_no_blocks: true, captures, browser_errors: errors, limitations: 'Lodestone lighting, biome tint and special blocks are preview approximations. Native behavior is separately verified by Forge.' }, null, 2) + '\n')
} finally { await browser.close() }

