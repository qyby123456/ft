import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createHash } from 'node:crypto'
import { createRequire } from 'node:module'

const require = createRequire('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright/package.json')
const { chromium } = require('playwright')
const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const browser = await chromium.launch({ headless: true, executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe', args: ['--use-angle=swiftshader', '--enable-webgl', '--enable-unsafe-swiftshader', '--disable-gpu-sandbox'] })
try {
  const page = await browser.newPage({ viewport: { width: 1536, height: 1024 }, deviceScaleFactor: 1 })
  const errors = []
  page.on('pageerror', error => errors.push(String(error)))
  await page.goto('http://127.0.0.1:5311/preview/interactive.html', { waitUntil: 'domcontentloaded', timeout: 180000 })
  await page.waitForFunction(() => window.zoneR01Ready === true, undefined, { timeout: 180000 })
  const views = await page.evaluate(() => window.zoneR01.cameras)
  const evidence = []
  for (const name of views) {
    const camera = await page.evaluate(name => window.zoneR01.showView(name), name)
    const destination = path.join(root, 'preview', `${name}.png`)
    await page.screenshot({ path: destination, type: 'png' })
    evidence.push({ file: `preview/${name}.png`, sha256: createHash('sha256').update(await fs.readFile(destination)).digest('hex'), ...camera })
    console.log(`Rendered ${name}`)
  }
  if (errors.length) throw new Error(errors.join('\n'))
  const digest = createHash('sha256').update(await fs.readFile(path.join(root, 'room.nbt'))).digest('hex')
  await fs.writeFile(path.join(root, 'qa/views.json'), JSON.stringify({ status: 'PASS', id: 'zone-R01', sha256: digest, renderer: 'Minecraft Builder Lodestone 0.6.0 / ThreeStructureRenderer / Chromium WebGL', real_nbt: true, original_nbt_modified: false, screenshot_size: [1536, 1024], views: evidence, browser_errors: errors, client_playtest: false }, null, 2) + '\n')
} finally { await browser.close() }
