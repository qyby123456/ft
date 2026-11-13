import fs from 'node:fs/promises'
import path from 'node:path'
import { spawn } from 'node:child_process'
import { createRequire } from 'node:module'
import { fileURLToPath } from 'node:url'
import { createHash } from 'node:crypto'

const require = createRequire(import.meta.url)
const { chromium } = require('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright')
const root = path.dirname(fileURLToPath(import.meta.url))
const finalNbt = path.join(root, 'incoming/G2B-01-R2/room.nbt')
const digest = createHash('sha256').update(await fs.readFile(finalNbt)).digest('hex')
const browser = await chromium.launch({ executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe', headless: true, args: ['--enable-unsafe-swiftshader'] })
const reports = []
try {
  for (const mode of ['exterior', 'interior']) {
    const target = mode === 'exterior' ? finalNbt : path.join(root, 'preview-only/preview-only.nbt')
    const child = spawn(process.execPath, ['C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/preview.mjs', '--nbt', target, '--port', '0', '--no-open'], { windowsHide: true, stdio: ['ignore', 'pipe', 'pipe'] })
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
      const page = await browser.newPage({ viewport: { width: 1600, height: 1100 } })
      const errors = []
      page.on('pageerror', error => errors.push(error.message))
      await page.goto(url, { waitUntil: 'domcontentloaded', timeout: 120000 })
      await page.waitForFunction(() => document.getElementById('loading')?.classList.contains('hidden'), null, { timeout: 180000 })
      await page.waitForTimeout(1800)
      await page.mouse.move(600, 500)
      await page.mouse.down()
      await page.mouse.move(1103, 500, { steps: 25 })
      await page.mouse.up()
      await page.mouse.wheel(0, -190)
      await page.waitForTimeout(900)
      const screenshot = path.join(root, 'incoming/G2B-01-R2/lodestone-' + mode + '.png')
      await page.screenshot({ path: screenshot })
      if (errors.length) throw new Error(errors.join('\n'))
      reports.push({ mode, screenshot, screenshot_sha256: createHash('sha256').update(await fs.readFile(screenshot)).digest('hex'), final_nbt: finalNbt, final_nbt_sha256: digest, rendered_nbt: target, rendered_nbt_sha256: createHash('sha256').update(await fs.readFile(target)).digest('hex'), renderer: 'Actual bundled Lodestone viewer WebGL canvas in headless Edge', actual_minecraft_game_screenshot: false, cutaway_not_deployable: mode === 'interior', errors })
      await page.close()
      console.log('CAPTURED ' + mode)
    } finally { child.kill() }
  }
  await fs.writeFile(path.join(root, 'qa/lodestone-screenshots.json'), JSON.stringify(reports, null, 2) + '\n')
} finally { await browser.close() }
