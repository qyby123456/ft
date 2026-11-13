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
const fullDirections = ['exterior', 'reverse', 'detail', 'grand', 'ritual', 'gallery', 'shrine']
const entries = fullDirections.map(direction => ({ direction, derived: derivation.exterior, source_sha256: derivation.source_sha256, derived_sha256: derivation.source_sha256 })).concat(derivation.derivatives)
const cameras = { grand: { center: [33, 6, 58], distance: 12, rotation: Math.PI, elevation: 0.08 }, ritual: { center: [10, 6, 39], distance: 8, rotation: -Math.PI / 2, elevation: 0.08 }, gallery: { center: [49.5, 5, 38], distance: 12, rotation: Math.PI, elevation: 0 }, shrine: { center: [58, 5, 59], distance: 6, rotation: Math.PI, elevation: 0.03 } }
const browser = await chromium.launch({ executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe', headless: true, args: ['--enable-unsafe-swiftshader'] })
const reports = []
try {
  for (const entry of entries) {
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
      const special = cameras[entry.direction]
      if (special) await page.route('**/src/main.js', async route => {
        const response = await route.fetch()
        const original = await response.text()
        const expression = 'const center = vec3.fromValues(size[0] / 2, Math.max(1, size[1] / 2.5), size[2] / 2)'
        if (!original.includes(expression)) throw new Error('Camera expression missing')
        const body = original.replace(expression, `const center = vec3.fromValues(${special.center.join(', ')})`)
        await route.fulfill({ response, body })
      })
      await page.goto(url, { waitUntil: 'domcontentloaded', timeout: 120000 })
      await page.waitForFunction(() => document.getElementById('loading')?.classList.contains('hidden'), null, { timeout: 180000 })
      const rotation = special?.rotation ?? { exterior: 3.85, reverse: 0.7, detail: Math.PI, north: Math.PI - 0.12, south: -0.12, west: Math.PI / 2 - 0.12, east: -Math.PI / 2 - 0.12 }[entry.direction]
      const xRotation = special?.elevation ?? (entry.direction === 'detail' ? -0.08 : 0.90)
      await page.mouse.move(800, 550)
      await page.mouse.down()
      await page.mouse.move(800 + (rotation - 0.82) * 160, 550 + (xRotation - 0.62) * 160, { steps: 35 })
      await page.mouse.up()
      const wheel = special ? (special.distance - 127.3) / 0.08 : entry.direction === 'detail' ? -1425 : -620
      await page.mouse.wheel(0, wheel)
      await page.waitForTimeout(1500)
      const screenshot = path.join(root, 'incoming/G2B-10', `lodestone-${entry.direction}.png`)
      await page.screenshot({ path: screenshot })
      if (errors.length) throw new Error(errors.join('\n'))
      reports.push({ direction: entry.direction, screenshot, screenshot_sha256: createHash('sha256').update(await fs.readFile(screenshot)).digest('hex'), source_sha256: entry.source_sha256, rendered: entry.derived, rendered_sha256: entry.derived_sha256, renderer: 'Actual bundled Lodestone WebGL viewer in headless Edge', camera: { center: special?.center ?? [32.5, 7.2, 33.5], target_override: special ? 'Browser response replaces only const center vec3 expression in viewer src/main.js; no NBT, geometry, visibility, block or model mutations. Skill files unchanged.' : null, y_rotation: rotation, x_rotation: xRotation, distance: 127.3 + wheel * 0.08, viewport: [1600, 1200] }, screenshot_not_gameplay: true, preview_not_deployable: !fullDirections.includes(entry.direction), errors })
      await page.close()
      console.log('S2-10 CAPTURED ' + entry.direction)
    } finally { child.kill() }
  }
  await fs.writeFile(path.join(root, 'qa/lodestone-screenshots.json'), JSON.stringify(reports, null, 2) + '\n')
} finally { await browser.close() }
