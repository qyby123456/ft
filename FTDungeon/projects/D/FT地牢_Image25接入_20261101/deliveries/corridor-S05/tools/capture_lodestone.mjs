import fs from 'node:fs/promises'
import path from 'node:path'
import { createHash } from 'node:crypto'
import { createRequire } from 'node:module'
import { fileURLToPath } from 'node:url'

const root = path.dirname(path.dirname(fileURLToPath(import.meta.url)))
const require = createRequire('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/require-anchor.cjs')
const { chromium } = require('playwright')
const url = process.argv[2] ?? 'http://127.0.0.1:3435/?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=S05-blackstone-flooded'
const fingerprint = createHash('sha256').update(await fs.readFile(path.join(root, 'room.nbt'))).digest('hex')
const browser = await chromium.launch({
  executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe',
  headless: true,
  args: ['--use-angle=swiftshader', '--enable-unsafe-swiftshader', '--disable-gpu-sandbox'],
})
const errors = []
try {
  const page = await browser.newPage({ viewport: { width: 1440, height: 1000 } })
  page.on('pageerror', error => errors.push(error.message))
  await page.route('**/src/main.js', async route => {
    const response = await route.fetch()
    const body = (await response.text()).replace('await renderer.whenReady()', 'window.s05Renderer = renderer; window.s05Camera = camera; await renderer.whenReady()')
    await route.fulfill({ response, body })
  })
  await page.goto(url, { waitUntil: 'domcontentloaded', timeout: 120000 })
  await page.locator('#loading').waitFor({ state: 'hidden', timeout: 120000 })
  const stats = await page.locator('#build-stats').innerText()
  if (!stats.includes('11 x 10 x 16') || !stats.includes('1,760')) throw new Error('Unexpected loaded structure: ' + stats)
  await page.screenshot({ path: path.join(root, 'preview/lodestone-exterior.png') })
  await page.evaluate(() => {
    const position = new Float32Array([5.5, 3.62, 1.25])
    const target = new Float32Array([5.5, 3.62, 15.5])
    window.s05Camera.getCamera = () => ({ position, target })
    window.s05Renderer.lookAt(position, target)
    window.s05Renderer.drawStructure()
  })
  await page.screenshot({ path: path.join(root, 'preview/lodestone-interior.png') })
  if (errors.length) throw new Error(errors.join('; '))
  const outputs = []
  for (const relative of ['preview/lodestone-exterior.png', 'preview/lodestone-interior.png']) {
    outputs.push({ path: relative, sha256: createHash('sha256').update(await fs.readFile(path.join(root, relative))).digest('hex') })
  }
  await fs.writeFile(path.join(root, 'qa/lodestone.json'), JSON.stringify({
    status: 'PASS', source_nbt_sha256: fingerprint, stats, url, viewport: [1440, 1000], errors, outputs,
    eye_camera: { position: [5.5, 3.62, 1.25], target: [5.5, 3.62, 15.5] },
    scope: 'Live Lodestone default-pack WebGL rendering in headless Edge, not a Minecraft client screenshot. In-memory camera exposure only; installed viewer files are unchanged.',
  }, null, 2) + '\n')
  console.log('S05_LODESTONE_PASS')
} finally {
  await browser.close()
}
