import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createHash } from 'node:crypto'
import { createRequire } from 'node:module'

const require = createRequire('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright/package.json')
const { chromium } = require('playwright')
const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const base = process.argv[2] ?? 'http://127.0.0.1:53901/'
const digest = bytes => createHash('sha256').update(bytes).digest('hex')
const before = digest(await fs.readFile(path.join(root, 'room.nbt')))
const browser = await chromium.launch({ headless: true, executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe', args: ['--use-angle=swiftshader', '--enable-webgl', '--enable-unsafe-swiftshader', '--disable-gpu-sandbox'] })
try {
  const page = await browser.newPage({ viewport: { width: 1536, height: 1024 }, deviceScaleFactor: 1 })
  const errors = []
  page.on('pageerror', error => errors.push(String(error)))
  const url = new URL('/preview/interactive.html', base)
  await page.goto(url.href, { waitUntil: 'domcontentloaded', timeout: 180000 })
  await page.waitForFunction(() => window.n01Ready === true, undefined, { timeout: 180000 })
  const views = await page.evaluate(() => window.n01.cameras)
  const evidence = []
  for (const name of views) {
    const camera = await page.evaluate(name => window.n01.showView(name), name)
    const destination = path.join(root, 'preview', `${name}.png`)
    await page.screenshot({ path: destination, type: 'png' })
    evidence.push({ file: `preview/${name}.png`, sha256: digest(await fs.readFile(destination)), ...camera })
    console.log(`Rendered ${name}`)
  }
  const sheetPage = await browser.newPage({ viewport: { width: 1536, height: 1124 }, deviceScaleFactor: 1 })
  for (const kind of ['interior', 'exterior']) {
    const cards = []
    for (const direction of ['north', 'east', 'south', 'west']) {
      const name = `${kind}-${direction}`
      const image = (await fs.readFile(path.join(root, 'preview', `${name}.png`))).toString('base64')
      cards.push(`<figure><img src="data:image/png;base64,${image}"><figcaption>${name.toUpperCase()} · ORIGINAL NBT</figcaption></figure>`)
    }
    await sheetPage.setContent(`<!doctype html><html><head><meta charset="utf-8"><style>*{box-sizing:border-box}body{margin:0;background:#14222c;color:#ecd6aa;font-family:Arial,"Microsoft YaHei"}header{height:92px;padding:18px 30px}h1{margin:0 0 8px;font-size:26px}p{margin:0;color:#bfd0d1;font-size:15px}main{display:grid;grid-template-columns:1fr 1fr;gap:14px;padding:0 20px}figure{margin:0;background:#20313b}img{display:block;width:100%;height:472px;object-fit:contain}figcaption{height:30px;padding:6px 16px;font-size:13px}</style></head><body><header><h1>N01 / 四向${kind === 'interior' ? '内' : '外'}视验收</h1><p>25 × 12 × 25 · 四口 5 × 5 · 原始 room.nbt / Lodestone 软件预览，非游戏截图</p></header><main>${cards.join('')}</main></body></html>`, { waitUntil: 'load' })
    const destination = path.join(root, 'preview', `four-${kind}.png`)
    await sheetPage.screenshot({ path: destination, type: 'png' })
    evidence.push({ file: `preview/four-${kind}.png`, sha256: digest(await fs.readFile(destination)), contact_sheet: true })
  }
  await fs.copyFile(path.join(root, 'preview/cutaway.png'), path.join(root, 'preview.png'))
  if (errors.length) throw new Error(errors.join('\n'))
  const after = digest(await fs.readFile(path.join(root, 'room.nbt')))
  if (before !== after) throw new Error('Rendering changed NBT bytes')
  await fs.writeFile(path.join(root, 'qa/views.json'), JSON.stringify({ status: 'PASS', sha256: after, renderer: 'Minecraft Builder Lodestone 0.6.0 / ThreeStructureRenderer / Chromium WebGL', real_nbt: true, original_nbt_modified: false, screenshot_size: [1536, 1024], contact_sheet_size: [1536, 1124], four_interior_original_nbt: true, four_exterior_original_nbt: true, cutaways_labeled: true, views: evidence, browser_errors: errors, client_playtest: false, viewer_url: url.href }, null, 2) + '\n')
} finally {
  await browser.close()
}
