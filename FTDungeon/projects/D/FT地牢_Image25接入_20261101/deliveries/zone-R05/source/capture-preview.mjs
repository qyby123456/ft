import { createRequire } from 'node:module'
import { readFile, writeFile } from 'node:fs/promises'
import { createHash } from 'node:crypto'
import { fileURLToPath } from 'node:url'
import path from 'node:path'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const require = createRequire(import.meta.url)
const { chromium } = require('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright-core')
const fingerprint = createHash('sha256').update(await readFile(path.join(root, 'room.nbt'))).digest('hex')
const url = process.argv[2] ?? 'http://127.0.0.1:5179/?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=R05'
const browser = await chromium.launch({ executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe', headless: true, args: ['--enable-unsafe-swiftshader', '--use-angle=swiftshader', '--disable-dev-shm-usage'] })
const errors = []
const captures = []
try {
  const page = await browser.newPage({ viewport: { width: 1536, height: 1024 }, deviceScaleFactor: 1 })
  page.on('pageerror', error => errors.push(String(error)))
  await page.route('**/src/main.js', async route => {
    const response = await route.fetch()
    let source = await response.text()
    source = source.replace('await renderer.whenReady()', 'window.r05SetView = (position, target) => { renderer.setCamera({position,target,fov:68}); renderer.drawStructure() }; await renderer.whenReady()')
    await route.fulfill({ response, body: source })
  })
  await page.goto(url, { waitUntil: 'domcontentloaded', timeout: 90000 })
  await page.waitForFunction(() => document.getElementById('loading')?.classList.contains('hidden') && typeof window.r05SetView === 'function', { timeout: 90000 })
  const served = await page.evaluate(async () => Array.from(new Uint8Array(await (await fetch('/__minecraft_builder__/build.nbt')).arrayBuffer())))
  if (createHash('sha256').update(Buffer.from(served)).digest('hex') !== fingerprint) throw new Error('Served NBT differs from delivery')
  for (const [direction, camera, target] of [['north', [13.5, 5.7, 2.5], [13.5, 6, 25]], ['south', [13.5, 5.7, 25.5], [13.5, 6, 3]], ['window', [12, 5.7, 14], [25, 6.5, 14]], ['exterior', [-24, 34, -28], [13, 7, 14]]]) {
    await page.evaluate(({ camera, target }) => window.r05SetView(camera, target), { camera, target })
    const output = path.join(root, 'views', `lodestone-${direction}.png`)
    await page.screenshot({ path: output })
    captures.push({ direction, camera, target, path: output, sha256: createHash('sha256').update(await readFile(output)).digest('hex') })
  }
  if (errors.length) throw new Error(errors.join('\n'))
  await writeFile(path.join(root, 'qa', 'lodestone-preview.json'), JSON.stringify({ status: 'PASS', sha256: fingerprint, source: url, source_nbt_verified_by_sha256: true, renderer: 'Minecraft Builder / Lodestone', is_gameplay_screenshot: false, browser_errors: errors, captures }, null, 2) + '\n')
  console.log('R05 Lodestone capture PASS ' + fingerprint)
} finally {
  await browser.close()
}
