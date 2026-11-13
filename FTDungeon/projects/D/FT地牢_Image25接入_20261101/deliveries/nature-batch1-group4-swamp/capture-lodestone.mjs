import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createRequire } from 'node:module'
import { createHash } from 'node:crypto'

const require = createRequire(import.meta.url)
const { chromium } = require('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright')
const root = path.dirname(fileURLToPath(import.meta.url))
const previews = JSON.parse(await fs.readFile(path.join(root, 'lodestone-previews.json'), 'utf8')).rooms
const browser = await chromium.launch({ executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe', headless: true, args: ['--enable-unsafe-swiftshader'] })
const reports = []
try {
  for (const preview of previews) {
    if (process.argv[2] && preview.id !== process.argv[2]) continue
    const page = await browser.newPage({ viewport: { width: 1440, height: 1000 } })
    const errors = []
    page.on('pageerror', error => errors.push(error.message))
    await page.goto(preview.url, { waitUntil: 'domcontentloaded', timeout: 90000 })
    await page.waitForFunction(() => document.getElementById('loading')?.classList.contains('hidden'), null, { timeout: 120000 })
    await page.waitForTimeout(500)
    const folder = path.join(root, 'incoming', preview.id)
    const screenshot = path.join(folder, 'lodestone-preview.png')
    await page.screenshot({ path: screenshot })
    if (errors.length) throw new Error(preview.id + ': ' + errors.join('\n'))
    reports.push({ id: preview.id, status: 'PASS', renderer: 'Official minecraft-builder-skill Lodestone / headless Edge', screenshot, sha256: createHash('sha256').update(await fs.readFile(path.join(folder, 'room.nbt'))).digest('hex'), errors, game_world_placement: false })
    console.log('LODESTONE ' + preview.id)
    await page.close()
  }
  const reportName = process.argv[2] ? 'lodestone-' + process.argv[2] + '.json' : 'lodestone.json'
  await fs.writeFile(path.join(root, 'qa', reportName), JSON.stringify(reports, null, 2) + '\n')
} finally {
  await browser.close()
}
