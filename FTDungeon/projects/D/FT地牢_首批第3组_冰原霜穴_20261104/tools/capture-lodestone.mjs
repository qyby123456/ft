import fs from 'node:fs/promises'
import path from 'node:path'
import { spawn } from 'node:child_process'
import { createRequire } from 'node:module'
import { fileURLToPath } from 'node:url'
import { createHash } from 'node:crypto'

const require = createRequire(import.meta.url)
const { chromium } = require('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright')
const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const preview = 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/preview.mjs'
const browser = await chromium.launch({ executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe', headless: true, args: ['--enable-unsafe-swiftshader'] })
const reports = []
try {
  for (let number = 1; number <= 9; number++) {
    const id = 'FTN3-' + String(number).padStart(2, '0')
    const folder = path.join(root, 'incoming', id)
    const cutaway = process.argv.includes('--cutaway')
    const target = path.join(folder, cutaway ? 'views/preview-only-cutaway.nbt' : 'room.nbt')
    const child = spawn(process.execPath, [preview, '--nbt', target, '--port', '0', '--no-open'], { windowsHide: true, stdio: ['ignore', 'pipe', 'pipe'] })
    let logs = ''
    child.stderr.on('data', data => { logs += data.toString() })
    try {
      const url = await new Promise((resolve, reject) => {
        const timer = setTimeout(() => reject(new Error('Preview start timeout: ' + logs)), 60000)
        child.stdout.on('data', data => {
          logs += data.toString()
          const match = logs.match(/Minecraft build preview: (http:\/\/[^\s]+)/)
          if (match) { clearTimeout(timer); resolve(match[1]) }
        })
        child.once('exit', code => { clearTimeout(timer); reject(new Error('Preview exited: ' + code + ' ' + logs)) })
      })
      const page = await browser.newPage({ viewport: { width: 1440, height: 1000 } })
      const errors = []
      page.on('pageerror', error => errors.push(error.message))
      await page.goto(url, { waitUntil: 'domcontentloaded', timeout: 120000 })
      await page.waitForFunction(() => document.getElementById('loading')?.classList.contains('hidden'), null, { timeout: 180000 })
      await page.waitForTimeout(800)
      const screenshot = path.join(folder, cutaway ? 'lodestone-interior.png' : 'lodestone-preview.png')
      await page.screenshot({ path: screenshot })
      if (errors.length) throw new Error(errors.join('\n'))
      reports.push({ id, status: 'PASS', renderer: 'Bundled Minecraft Builder Lodestone viewer / headless Edge', mode: cutaway ? 'Preview-only derivative; final room.nbt unchanged' : 'Complete unmodified NBT exterior', screenshot, preview_nbt: target, preview_nbt_sha256: createHash('sha256').update(await fs.readFile(target)).digest('hex'), nbt_sha256: createHash('sha256').update(await fs.readFile(path.join(folder, 'room.nbt'))).digest('hex'), errors, actual_game_screenshot: false })
      console.log('LODESTONE ' + id)
      await page.close()
    } finally {
      child.kill()
    }
  }
  await fs.writeFile(path.join(root, process.argv.includes('--cutaway') ? 'qa/lodestone-interior.json' : 'qa/lodestone.json'), JSON.stringify(reports, null, 2) + '\n')
} finally {
  await browser.close()
}
