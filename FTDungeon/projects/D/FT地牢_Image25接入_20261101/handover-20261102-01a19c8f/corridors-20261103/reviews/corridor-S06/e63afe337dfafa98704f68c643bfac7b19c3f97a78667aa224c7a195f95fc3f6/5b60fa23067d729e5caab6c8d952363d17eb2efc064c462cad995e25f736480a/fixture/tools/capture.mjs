import fs from 'node:fs/promises'
import path from 'node:path'
import { createRequire } from 'node:module'
import { root, readRoom, sha256, writeJson } from './common.mjs'

const require = createRequire('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright/package.json')
const { chromium } = require('playwright')
const info = JSON.parse(await fs.readFile(path.join(root, 'qa/preview-server.json')))
const browser = await chromium.launch({ channel: 'msedge', headless: true, args: ['--use-angle=swiftshader', '--enable-webgl', '--ignore-gpu-blocklist'] })
const page = await browser.newPage({ viewport: { width: 1536, height: 1024 }, deviceScaleFactor: 1 })
const errors = []
page.on('pageerror', error => errors.push(error.message))
await page.goto(info.url, { waitUntil: 'networkidle', timeout: 120000 })
await page.waitForFunction(() => window.m09?.ready && document.querySelector('#loading').hidden, { timeout: 120000 })
const outputs = []
await fs.mkdir(path.join(root, 'views'), { recursive: true })
const views = ['hero', 'reverse', ...['north', 'south', 'west', 'east'].flatMap(direction => [`interior-${direction}`, `exterior-${direction}`]), 'plan']
if (process.argv.includes('--native')) views.push('native-hero')
for (const view of views) {
  const parameters = await page.evaluate(async view => window.m09.setView(view === 'native-hero' ? 'hero' : view, view === 'native-hero'), view)
  await page.waitForTimeout(250)
  const relative = view === 'hero' ? 'preview.png' : `views/${view}.png`
  const bytes = await page.screenshot({ path: path.join(root, relative), type: 'png' })
  outputs.push({ file: relative, sha256: sha256(bytes), size: [1536, 1024], ...parameters })
  console.log(`M09_RENDER ${relative}`)
}
await browser.close()
const room = await readRoom()
await writeJson('qa/render.json', {
  status: errors.length ? 'FAIL' : 'PASS', source_nbt_sha256: room.hash,
  renderer: 'Lodestone 0.6.0 / Three.js / WebGL / vanilla default-pack',
  screenshot_type: 'Software renderer, not a Minecraft client screenshot',
  outputs, browser_errors: errors,
  limitations: ['No shaders, custom resource pack, particles or modpack-client lighting.', 'Cutaways are display-only; delivered NBT retains roof and walls.', 'Safe orange glass and shroomlights translate concept lava seams; actual lava is deliberately absent.'],
})
if (errors.length) process.exitCode = 1
