import fs from 'node:fs/promises'
import path from 'node:path'
import { createHash } from 'node:crypto'
import { fileURLToPath } from 'node:url'
import { createRequire } from 'node:module'

const require = createRequire('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright/package.json')
const { chromium } = require('playwright')
const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const base = process.argv[2]
if (!base) throw new Error('Provide the delivery-local viewer URL')
const browser = await chromium.launch({ headless: true, executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe', args: ['--use-angle=swiftshader', '--enable-webgl', '--enable-unsafe-swiftshader', '--disable-gpu-sandbox'] })
try {
  const page = await browser.newPage({ viewport: { width: 1536, height: 1024 }, deviceScaleFactor: 1 })
  const errors = []
  page.on('pageerror', error => errors.push(String(error)))
  await page.goto(new URL('/views/index.html', base).href, { waitUntil: 'domcontentloaded', timeout: 180000 })
  await page.waitForFunction(() => window.c02Ready === true, undefined, { timeout: 180000 })
  const cameras = await page.evaluate(() => window.c02.cameras)
  const evidence = []
  for (const name of cameras) {
    const camera = await page.evaluate(name => window.c02.showView(name), name)
    const filename = path.join(root, 'views', name + '.png')
    await page.screenshot({ path: filename, type: 'png' })
    evidence.push({ file: 'views/' + name + '.png', sha256: createHash('sha256').update(await fs.readFile(filename)).digest('hex'), ...camera })
    if (name === 'hero') await fs.copyFile(filename, path.join(root, 'preview.png'))
    console.log('Rendered ' + name)
  }
  if (errors.length) throw new Error(errors.join('\n'))
  const hash = createHash('sha256').update(await fs.readFile(path.join(root, 'room.nbt'))).digest('hex')
  await fs.writeFile(path.join(root, 'qa/render-manifest.json'), JSON.stringify({ status: 'PASS', sha256: hash, renderer: 'Minecraft Builder / Lodestone 0.6.0 / ThreeStructureRenderer / Chromium WebGL', real_nbt: true, original_nbt_modified: false, screenshot_size: [1536, 1024], views: evidence, browser_errors: errors, client_playtest: false }, null, 2) + '\n')
} finally { await browser.close() }

