import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { chromium } from 'file:///C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright/index.mjs'

const root = path.dirname(fileURLToPath(import.meta.url))
await fs.mkdir(path.join(root, 'preview'), { recursive: true })
const browser = await chromium.launch({ channel: 'msedge', headless: true, args: ['--disable-gpu-sandbox', '--disable-dev-shm-usage', '--use-angle=swiftshader', '--enable-unsafe-swiftshader'] })
const page = await browser.newPage({ viewport: { width: 1536, height: 1024 }, deviceScaleFactor: 1 })
const errors = []
page.on('pageerror', error => errors.push(String(error)))
page.on('console', message => { if (message.type() === 'error') errors.push(message.text()) })
try {
  await page.goto('http://127.0.0.1:53116/', { timeout: 180000, waitUntil: 'domcontentloaded' })
  await page.waitForFunction(() => window.reviewReady, { timeout: 180000 })
  const names = await page.evaluate(() => window.review.views)
  const evidence = []
  for (const name of names) {
    const details = await page.evaluate(name => window.review.show(name), name)
    await page.waitForTimeout(200)
    const file = path.join(root, 'preview', `${name}.png`)
    await page.screenshot({ path: file, timeout: 120000 })
    evidence.push({ ...details, path: file })
    if (name === 'perspective') await fs.copyFile(file, path.join(root, 'preview.png'))
    console.log(`Captured ${name}`)
  }
  await fs.writeFile(path.join(root, 'qa/render-evidence.json'), JSON.stringify({ status: errors.length ? 'REVIEW_REQUIRED' : 'PASS', renderer: 'Lodestone 0.6.0 / Three.js / Edge headless WebGL', source: path.join(root, 'room.nbt'), views: evidence, browser_errors: errors, disclaimer: 'Software NBT render; not a Minecraft client screenshot. Cutaways affect only render copies.' }, null, 2) + '\n')
} finally { await browser.close() }
