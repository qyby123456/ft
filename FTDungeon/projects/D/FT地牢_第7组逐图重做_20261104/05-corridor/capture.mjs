import fs from 'node:fs/promises'
import path from 'node:path'
import { spawn } from 'node:child_process'
import { createRequire } from 'node:module'
import { fileURLToPath } from 'node:url'
import { createHash } from 'node:crypto'

const require = createRequire(import.meta.url)
const { chromium } = require('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright')
const root = path.dirname(fileURLToPath(import.meta.url))
const finalNbt = path.join(root, 'incoming/S7R2-05/room.nbt')
const previewScript = 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/preview.mjs'
const hashBytes = data => createHash('sha256').update(data).digest('hex')
const digest = async filename => hashBytes(await fs.readFile(filename))
const sourceHash = await digest(finalNbt)
const browser = await chromium.launch({ executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe', headless: true, args: ['--enable-unsafe-swiftshader'] })
const reports = []
try {
  for (const [mode, filename] of [['exterior', finalNbt], ['interior', path.join(root, 'views/PREVIEW_ONLY_cutaway.nbt')], ['eye-level', finalNbt]]) {
    const child = spawn(process.execPath, [previewScript, '--nbt', filename, '--port', '0', '--no-open'], { windowsHide: true, stdio: ['ignore', 'pipe', 'pipe'] })
    let logs = ''
    child.stderr.on('data', data => { logs += data.toString() })
    try {
      const url = await new Promise((resolve, reject) => {
        const timer = setTimeout(() => reject(new Error('Preview timeout ' + logs)), 60000)
        child.stdout.on('data', data => {
          logs += data.toString()
          const match = logs.match(/Minecraft build preview: (http:\/\/[^\s]+)/)
          if (match) { clearTimeout(timer); resolve(match[1]) }
        })
        child.once('exit', code => { clearTimeout(timer); reject(new Error('Preview exit ' + code + logs)) })
      })
      const page = await browser.newPage({ viewport: { width: 1600, height: 1100 } })
      const errors = []
      page.on('pageerror', error => errors.push(error.message))
      let cameraPatch = null
      if (mode === 'eye-level') {
        await page.route('**/src/main.js*', async route => {
          const response = await route.fetch()
          const original = await response.text()
          const anchor = 'const camera = new OrbitCamera(canvas, center, Math.max(18, Math.max(...size) * 1.9), requestRender)'
          if (!original.includes(anchor)) throw new Error('Viewer camera anchor not found')
          const injection = '\n  camera.center = vec3.fromValues(7.5, 3.62, 21.5)\n  camera.distance = 20\n  camera.xRotation = 0\n  camera.yRotation = Math.PI\n  window.__s7EyeCamera = { position: [7.5, 3.62, 1.5], target: [7.5, 3.62, 21.5], feetY: 2, eyeHeight: 1.62 }\n'
          const adjusted = original.replace(anchor, anchor + injection)
          cameraPatch = { scope: 'BROWSER_RESPONSE_CAMERA_ONLY', viewer_path: 'assets/viewer/src/main.js', anchor, injection, original_response_sha256: hashBytes(original), adjusted_response_sha256: hashBytes(adjusted), installed_skill_modified: false, structure_or_resources_modified: false }
          await route.fulfill({ response, body: adjusted })
        })
      }
      await page.goto(url, { waitUntil: 'domcontentloaded', timeout: 120000 })
      await page.waitForFunction(() => document.getElementById('loading')?.classList.contains('hidden'), null, { timeout: 180000 })
      await page.waitForTimeout(1000)
      let eyeCamera = null
      if (mode === 'eye-level') {
        eyeCamera = await page.evaluate(() => window.__s7EyeCamera)
        if (!eyeCamera || !cameraPatch) throw new Error('Eye camera not bound')
        await fs.writeFile(path.join(root, 'qa/eye-camera.json'), JSON.stringify({ ...cameraPatch, camera: eyeCamera, source_nbt_sha256: sourceHash, displayed_nbt: filename, actual_world_placement: false }, null, 2) + '\n')
      } else {
        await page.mouse.move(800, 550)
        await page.mouse.wheel(0, mode === 'exterior' ? -250 : -450)
        if (mode === 'interior') {
          await page.mouse.down()
          await page.mouse.move(700, 550, { steps: 15 })
          await page.mouse.up()
        }
      }
      await page.waitForTimeout(500)
      const screenshot = path.join(root, 'lodestone-' + mode + '.png')
      await page.screenshot({ path: screenshot })
      if (errors.length) throw new Error(errors.join('\n'))
      reports.push({ id: 'S7R2-05', mode, status: 'PASS', label: 'ACTUAL_LODESTONE_GPU_PREVIEW_NOT_GAMEPLAY', screenshot, screenshot_sha256: await digest(screenshot), source_nbt_sha256: sourceHash, displayed_nbt_sha256: await digest(filename), displayed_nbt: filename, eye_camera: eyeCamera, errors, actual_world_placement: false })
      console.log('S7-05 LODESTONE PASS ' + mode)
      await page.close()
    } finally {
      child.kill()
    }
  }
  if (await digest(finalNbt) !== sourceHash) throw new Error('Source NBT changed during preview')
  await fs.writeFile(path.join(root, 'qa/lodestone.json'), JSON.stringify(reports, null, 2) + '\n')
} finally {
  await browser.close()
}
