const fs = require('node:fs')
const path = require('node:path')
const crypto = require('node:crypto')
const { chromium } = require('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright')

async function main() {
  const browser = await chromium.launch({ channel: 'msedge', headless: true, args: ['--use-angle=swiftshader', '--enable-unsafe-swiftshader'] })
  const records = []
  const scenarios = []
  for (const [direction, rotation] of [['north', Math.PI], ['east', -Math.PI / 2], ['south', 0], ['west', Math.PI / 2]]) {
    for (const mode of ['interior', 'exterior']) scenarios.push({ direction, rotation, mode })
  }
  scenarios.push({ direction: 'north', rotation: Math.PI, mode: 'eye-level' }, { direction: 'south', rotation: 0, mode: 'eye-level' })
  scenarios.push({ direction: 'south', rotation: 0, mode: 'corner-detail' })
  try {
    for (const { direction, rotation, mode } of scenarios) {
      const nbtPath = mode === 'interior' ? `views/${direction}/inspection-only-${direction}.nbt` : 'incoming/S8R_IMG07/room.nbt'
      const bytes = fs.readFileSync(path.join(__dirname, nbtPath))
      const page = await browser.newPage({ viewport: { width: 1400, height: 1080 } })
      const errors = []
      const elevation = mode === 'eye-level' ? 0 : mode === 'interior' ? 0.4 : 0.62
      page.on('pageerror', error => errors.push(error.message))
      await page.route('**/src/main.js', async route => {
        const response = await route.fetch()
        let source = (await response.text()).replace('this.yRotation = 0.82', `this.yRotation = ${rotation}`).replace('this.xRotation = 0.62', `this.xRotation = ${elevation}`)
        if (mode === 'eye-level') {
          source = source.replace('vec3.fromValues(size[0] / 2, Math.max(1, size[1] / 2.5), size[2] / 2)', direction === 'north' ? 'vec3.fromValues(12, 5.1, 12)' : 'vec3.fromValues(12, 5.62, 14)').replace('Math.max(18, Math.max(...size) * 1.9)', '9')
        } else {
          source = source.replace('Math.max(...size) * 1.9', 'Math.max(...size) * 1.12')
        }
        if (mode === 'eye-level') {
          const position = direction === 'north' ? [12, 5.1, 0.5] : [12, 5.62, 23]
          const target = direction === 'north' ? [12, 5.1, 12] : [12, 5.62, 8]
          source = source.replace('position: vec3.fromValues(inverse[12], inverse[13], inverse[14]),', `position: vec3.fromValues(${position.join(',')}),`).replace('target: this.center,', `target: vec3.fromValues(${target.join(',')}),`)
        }
        if (mode === 'corner-detail') source = source.replace('position: vec3.fromValues(inverse[12], inverse[13], inverse[14]),','position: vec3.fromValues(20, 11, 8),').replace('target: this.center,','target: vec3.fromValues(10, 5, 18),')
        await route.fulfill({ response, body: source })
      })
      await page.route('**/__minecraft_builder__/build.nbt', route => route.fulfill({ status: 200, contentType: 'application/octet-stream', body: bytes }))
      await page.goto(`http://127.0.0.1:5607/?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=S8-07-${mode}-${direction}`, { waitUntil: 'networkidle', timeout: 120000 })
      await page.waitForFunction(() => document.getElementById('loading').classList.contains('hidden'), { timeout: 120000 })
      await page.evaluate(() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve))))
      const servedHash = await page.evaluate(async () => {
        const bytes = await (await fetch('/__minecraft_builder__/build.nbt')).arrayBuffer()
        return Array.from(new Uint8Array(await crypto.subtle.digest('SHA-256', bytes))).map(value => value.toString(16).padStart(2, '0')).join('')
      })
      const hash = crypto.createHash('sha256').update(bytes).digest('hex')
      if (servedHash !== hash || errors.length) throw new Error('Wrong browser input or page error ' + direction + errors.join(';'))
      const screenshot = `views/${direction}/${mode}-${direction}.png`
      await page.screenshot({ path: path.join(__dirname, screenshot) })
      records.push({ direction, mode, nbt: nbtPath, served_sha256: servedHash, screenshot, screenshot_sha256: crypto.createHash('sha256').update(fs.readFileSync(path.join(__dirname, screenshot))).digest('hex'), page_errors: errors, camera_y_rotation: rotation, camera_x_rotation: elevation, camera_distance: mode === 'eye-level' ? 9 : 27 * 1.12, eye_level_camera_position: mode === 'eye-level' ? (direction === 'north' ? [6.5, 3.62, 3.5] : [24.5, 3.62, 20.5]) : null, eye_level_camera_target: mode === 'eye-level' ? (direction === 'north' ? [6.5, 3.62, 12.5] : [15.5, 3.62, 20.5]) : null })
      if (mode === 'eye-level') Object.assign(records[records.length - 1], { eye_level_camera_position: direction === 'north' ? [12, 5.1, 0.5] : [12, 5.62, 23], eye_level_camera_target: direction === 'north' ? [12, 5.1, 12] : [12, 5.62, 8], camera_distance: null, camera_override: 'Fixed complete-source camera; north is elevated frontal gate inspection, south is player eye height' })
      await page.close()
      if (mode === 'corner-detail') Object.assign(records[records.length - 1], { camera_position: [20, 11, 8], camera_target: [10, 5, 18], camera_distance: null, camera_x_rotation: null, camera_y_rotation: null, camera_override: 'Fixed lookAt using original final NBT, no geometry edits' })
    }
  } finally {
    await browser.close()
  }
  fs.writeFileSync(path.join(__dirname, 'qa/lodestone.json'), JSON.stringify({ status: 'PASS_HASHED_WEBGL_LOADS', renderer: 'bundled Lodestone ThreeStructureRenderer; eight cardinal, two complete-source eye-level approaches and one complete-source antechamber detail; offline WebGL not Minecraft gameplay', records }, null, 2))
  console.log('Eleven hashed Lodestone views captured; intact-source eye-level approaches and inside-antechamber detail')
}

main().catch(error => { console.error(error); process.exitCode = 1 })


