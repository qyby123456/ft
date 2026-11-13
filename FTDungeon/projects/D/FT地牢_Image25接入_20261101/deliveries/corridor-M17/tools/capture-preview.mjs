import { createRequire } from 'node:module'
import { readFile, writeFile, copyFile } from 'node:fs/promises'
import { createHash } from 'node:crypto'
import { fileURLToPath } from 'node:url'
import path from 'node:path'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const require = createRequire(import.meta.url)
const { chromium } = require('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright-core')
const fingerprint = createHash('sha256').update(await readFile(path.join(root, 'room.nbt'))).digest('hex')
const url = process.argv[2] ?? 'http://127.0.0.1:5197/?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=M17'
const browser = await chromium.launch({ executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe', headless: true, args: ['--enable-unsafe-swiftshader', '--use-angle=swiftshader', '--disable-dev-shm-usage'] })
const errors = []
const captures = []
try {
  const page = await browser.newPage({ viewport: { width: 1536, height: 1024 }, deviceScaleFactor: 1 })
  page.on('pageerror', error => errors.push(String(error)))
  await page.route('**/src/main.js', async route => {
    const response = await route.fetch()
    let source = await response.text()
    source = source.replace('await renderer.whenReady()', 'window.m17SetView = (position, target) => { renderer.setCamera({position,target,fov:68}); renderer.drawStructure() }; await renderer.whenReady()')
    await route.fulfill({ response, body: source })
  })
  await page.goto(url, { waitUntil: 'domcontentloaded', timeout: 90000 })
  await page.waitForFunction(() => document.getElementById('loading')?.classList.contains('hidden') && typeof window.m17SetView === 'function', { timeout: 90000 })
  const served = await page.evaluate(async () => Array.from(new Uint8Array(await (await fetch('/__minecraft_builder__/build.nbt')).arrayBuffer())))
  if (createHash('sha256').update(Buffer.from(served)).digest('hex') !== fingerprint) throw new Error('Preview server is serving a different NBT')
  for (const direction of ['north', 'south']) {
    const camera = direction === 'north' ? [8.5, 4.62, 0.5] : [8.5, 4.62, 40.5]
    const target = direction === 'north' ? [8.5, 4.62, 40] : [8.5, 4.62, 0]
    await page.evaluate(({ camera, target }) => window.m17SetView(camera, target), { camera, target })
    const output = path.join(root, 'views', `lodestone-axis-${direction}.png`)
    await page.screenshot({ path: output })
    captures.push({ direction, lighting: 'day', camera, target, path: output, sha256: createHash('sha256').update(await readFile(output)).digest('hex') })
  }
  await page.locator('#lighting-toggle').click()
  await page.evaluate(() => window.m17SetView([8.5, 4.62, 0.5], [8.5, 4.62, 40]))
  const night = path.join(root, 'views', 'lodestone-axis-night.png')
  await page.screenshot({ path: night })
  captures.push({ direction: 'north', lighting: 'night', path: night, sha256: createHash('sha256').update(await readFile(night)).digest('hex') })
  if (errors.length) throw new Error(errors.join('\n'))
  await copyFile(path.join(root, 'views', 'lodestone-axis-north.png'), path.join(root, 'preview.png'))
  await writeFile(path.join(root, 'qa', 'lodestone-preview.json'), JSON.stringify({ status: 'PASS', sha256: fingerprint, source: url, source_nbt_verified_by_sha256: true, renderer: 'Minecraft Builder / Lodestone 0.6.0', is_gameplay_screenshot: false, limitations: 'Lighting, liquids, transparency and biome tint are preview approximations. Native block and fluid behavior are verified by Forge.', browser_errors: errors, captures }, null, 2) + '\n')
  console.log('M17 Lodestone captures PASS ' + fingerprint)
} finally {
  await browser.close()
}
