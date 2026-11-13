import fs from 'node:fs/promises'
import path from 'node:path'
import { spawn } from 'node:child_process'
import { createRequire } from 'node:module'
import { fileURLToPath } from 'node:url'
import { createHash } from 'node:crypto'

const require = createRequire(import.meta.url)
const { chromium } = require('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright')
const root = path.dirname(fileURLToPath(import.meta.url))
const derivation = JSON.parse(await fs.readFile(path.join(root, 'derivation.json'), 'utf8'))
const browser = await chromium.launch({ executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe', headless: true, args: ['--enable-unsafe-swiftshader'] })
const reports = []
try {
  for (const entry of derivation.derivatives) {
    const child = spawn(process.execPath, ['C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/preview.mjs', '--nbt', entry.derived, '--port', '0', '--no-open'], { windowsHide: true, stdio: ['ignore', 'pipe', 'pipe'] })
    let logs = ''
    child.stderr.on('data', data => { logs += data.toString() })
    try {
      const url = await new Promise((resolve, reject) => {
        const timer = setTimeout(() => reject(new Error(logs)), 60000)
        child.stdout.on('data', data => {
          logs += data.toString()
          const match = logs.match(/Minecraft build preview: (http:\/\/[^\s]+)/)
          if (match) { clearTimeout(timer); resolve(match[1]) }
        })
        child.once('exit', code => { clearTimeout(timer); reject(new Error('Preview exited ' + code)) })
      })
      const page = await browser.newPage({ viewport: { width: 1600, height: 1200 } })
      const errors = []
      page.on('pageerror', error => errors.push(error.message))
      await page.goto(url, { waitUntil: 'domcontentloaded', timeout: 120000 })
      await page.waitForFunction(() => document.getElementById('loading')?.classList.contains('hidden'), null, { timeout: 180000 })
      const rotation = { north: Math.PI - 0.12, south: -0.12, west: Math.PI / 2 - 0.12, east: -Math.PI / 2 - 0.12 }[entry.direction]
      const delta = (rotation - 0.82) * 160
      await page.mouse.move(800, 550)
      await page.mouse.down()
      await page.mouse.move(800 + delta, 576, { steps: 35 })
      await page.mouse.up()
      await page.mouse.wheel(0, -260)
      await page.waitForTimeout(1500)
      const screenshot = path.join(root, `lodestone-interior-${entry.direction}.png`)
      await page.screenshot({ path: screenshot })
      if (errors.length) throw new Error(errors.join('\n'))
      reports.push({ direction: entry.direction, screenshot, screenshot_sha256: createHash('sha256').update(await fs.readFile(screenshot)).digest('hex'), source_sha256: entry.source_sha256, derivative_sha256: entry.derived_sha256, derived: entry.derived, renderer: 'Actual bundled Lodestone WebGL viewer in headless Edge; UI drag and wheel only', camera: { y_rotation: rotation, x_rotation: 0.7825, distance: 38.1, viewport: [1600, 1200] }, screenshot_not_gameplay: true, preview_not_deployable: true, errors })
      await page.close()
      console.log('CAPTURED ' + entry.direction)
    } finally { child.kill() }
  }
  await fs.writeFile(path.join(root, 'screenshots.json'), JSON.stringify(reports, null, 2) + '\n')
} finally { await browser.close() }
