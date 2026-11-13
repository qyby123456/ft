import fs from 'node:fs/promises'
import path from 'node:path'
import { createHash } from 'node:crypto'
import { createRequire } from 'node:module'
import { fileURLToPath } from 'node:url'
import startViewer from './serve.mjs'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const runtime = createRequire('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/package.json')
const { chromium } = runtime('playwright')
const sharp = runtime('sharp')
const output = path.join(root, 'preview')
await fs.mkdir(output, { recursive: true })
const customServer = await startViewer(root)
const browser = await chromium.launch({
  executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe',
  headless: true,
  args: ['--enable-webgl', '--ignore-gpu-blocklist', '--enable-unsafe-swiftshader', '--use-angle=swiftshader'],
})
const page = await browser.newPage({ viewport: { width: 1440, height: 1080 }, deviceScaleFactor: 1 })
const errors = []
page.on('pageerror', error => { errors.push(error.message); console.error(error.stack) })
try {
  const server = process.env.M08_PREVIEW_URL ?? 'http://127.0.0.1:5188'
  await page.goto(`${server}/?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=M08-room.nbt`, { waitUntil: 'networkidle', timeout: 120000 })
  await page.waitForSelector('#loading.hidden', { state: 'attached', timeout: 120000 })
  await page.screenshot({ path: path.join(output, 'builder-viewer.png') })
  await page.goto(customServer.resolvedUrls.local[0], { waitUntil: 'networkidle', timeout: 120000 })
  await page.waitForFunction(() => window.m08Ready === true, null, { timeout: 120000 })
  const views = await page.evaluate(() => window.m08.views)
  const records = []
  for (const name of views) {
    const camera = await page.evaluate(name => window.m08.show(name), name)
    await page.waitForTimeout(250)
    const filename = `${name}.png`
    await page.screenshot({ path: path.join(output, filename) })
    const image = await fs.readFile(path.join(output, filename))
    records.push({ file: `preview/${filename}`, sha256: createHash('sha256').update(image).digest('hex'), camera })
  }
  if (errors.length) throw new Error(errors.join('\n'))
  await fs.copyFile(path.join(output, 'axial-north.png'), path.join(root, 'preview.png'))
  for (const kind of ['interior', 'exterior']) {
    const layers = []
    for (const [index, direction] of ['north', 'east', 'south', 'west'].entries()) {
      const image = await sharp(path.join(output, `${kind}-${direction}.png`)).resize(720, 540).png().toBuffer()
      layers.push({ input: image, left: index % 2 * 720, top: Math.floor(index / 2) * 540 })
    }
    await sharp({ create: { width: 1440, height: 1080, channels: 4, background: '#151419' } }).composite(layers).png().toFile(path.join(output, `four-view-${kind}.png`))
  }
  const hash = createHash('sha256').update(await fs.readFile(path.join(root, 'room.nbt'))).digest('hex')
  await fs.writeFile(path.join(root, 'qa/render.json'), JSON.stringify({
    status: 'PASS', engine: '@mattzh72/lodestone 0.6.0 / ThreeStructureRenderer / Edge headless SwiftShader',
    source: 'room.nbt', sha256: hash, dimensions: [1440, 1080], records,
    cutaway_changes_nbt: false, renderer_errors: errors, actual_modpack_screenshot: false,
  }, null, 2) + '\n')
  console.log(JSON.stringify({ status: 'PASS', views: records.length, sha256: hash }))
} catch (error) {
  await page.screenshot({ path: path.join(root, 'qa/render-failure.png') }).catch(() => {})
  await fs.writeFile(path.join(root, 'qa/render-failure.json'), JSON.stringify({ error: error.message, errors, url: page.url() }, null, 2))
  throw error
} finally {
  await browser.close()
  await customServer.close()
}
