const { chromium } = require('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright')
const path = require('node:path')
const fs = require('node:fs/promises')
const crypto = require('node:crypto')

async function main() {
  const browser = await chromium.launch({ channel: 'msedge', headless: true, args: ['--enable-webgl', '--ignore-gpu-blocklist', '--use-angle=swiftshader'] })
  const errors = []
  const views = []
  const hash = bytes => crypto.createHash('sha256').update(bytes).digest('hex')
  const nbtHash = hash(await fs.readFile(path.join(__dirname, 'room.nbt')))
  const previewHash = hash(await fs.readFile(path.join(__dirname, 'preview-only/preview-only-cutaway.nbt')))
  try {
    const page = await browser.newPage({ viewport: { width: 1440, height: 1100 }, deviceScaleFactor: 1 })
    page.on('pageerror', error => errors.push(error.message))
    for (const [port, prefix] of [[5344, 'interior'], [5343, 'exterior']]) {
      await page.goto(`http://127.0.0.1:${port}/?name=S3-09%20Frost%20Treasure`, { waitUntil: 'networkidle' })
      await page.waitForSelector('#loading.hidden', { state: 'attached', timeout: 180000 })
      await page.waitForTimeout(1000)
      await page.mouse.move(700, 550)
      await page.mouse.wheel(0, -420)
      await page.waitForTimeout(500)
      for (let index = 1; index <= 4; index++) {
        if (index > 1) {
          await page.mouse.move(650, 550)
          await page.mouse.down()
          await page.mouse.move(901.327, 550, { steps: 20 })
          await page.mouse.up()
          await page.waitForTimeout(500)
        }
        const filename = `${prefix}-0${index}.png`
        await page.screenshot({ path: path.join(__dirname, filename) })
        views.push({ file: path.join(__dirname, filename), sha256: hash(await fs.readFile(path.join(__dirname, filename))), source_nbt: path.join(__dirname, prefix === 'exterior' ? 'room.nbt' : 'preview-only/preview-only-cutaway.nbt'), source_sha256: prefix === 'exterior' ? nbtHash : previewHash, final_nbt_sha256: nbtHash, pitch_radians: 0.62, yaw_radians: 0.82 + (index - 1) * 251.327 / 160, camera_quadrant: ['southwest', 'northwest', 'northeast', 'southeast'][index - 1], derived_cutaway: prefix === 'interior' })
        if (prefix === 'interior' && index === 3) {
          await page.screenshot({ path: path.join(__dirname, 'preview.png') })
          await page.click('#lighting-toggle')
          await page.waitForTimeout(800)
          await page.screenshot({ path: path.join(__dirname, 'preview-night.png') })
          await page.click('#lighting-toggle')
          await page.waitForTimeout(500)
        }
      }
    }
    const inside = await browser.newPage({ viewport: { width: 1440, height: 1100 }, deviceScaleFactor: 1 })
    inside.on('pageerror', error => errors.push(error.message))
    await inside.route('**/src/main.js', async route => {
      const response = await route.fetch()
      const text = await response.text()
      if (!text.includes('renderer.lookAt(state.position, state.target)')) throw new Error('Viewer camera hook not found')
      await route.fulfill({ response, body: text.replace('renderer.lookAt(state.position, state.target)', 'renderer.lookAt([28, 13, 32], [18, 12, 10])') })
    })
    await inside.goto('http://127.0.0.1:5343/?name=S3-09%20Full%20NBT%20Treasure', { waitUntil: 'networkidle' })
    await inside.waitForSelector('#loading.hidden', { state: 'attached', timeout: 180000 })
    await inside.waitForTimeout(1500)
    await inside.screenshot({ path: path.join(__dirname, 'nave-full-nbt.png') })
    const interiorCamera = { file: path.join(__dirname, 'nave-full-nbt.png'), sha256: hash(await fs.readFile(path.join(__dirname, 'nave-full-nbt.png'))), source_nbt: path.join(__dirname, 'room.nbt'), source_sha256: nbtHash, eye: [28, 13, 32], target: [18, 12, 10], geometry_modified: false, method: 'In-memory Playwright response camera override only; installed viewer and NBT untouched' }
    const legCameras = []
    for (const camera of [
      { name: 'reverse-full-nbt', eye: [18, 15, 5], target: [20, 7, 30] },
      { name: 'chest-lid-full-nbt', eye: [22, 14, 16], target: [18, 9, 10] },
      { name: 'bridge-channel-full-nbt', eye: [32, 10, 34], target: [24, 5, 26] },
    ]) {
      const leg = await browser.newPage({ viewport: { width: 1440, height: 1100 }, deviceScaleFactor: 1 })
      leg.on('pageerror', error => errors.push(error.message))
      await leg.route('**/src/main.js', async route => {
        const response = await route.fetch()
        const text = await response.text()
        if (!text.includes('renderer.lookAt(state.position, state.target)')) throw new Error('Viewer camera hook missing')
        await route.fulfill({ response, body: text.replace('renderer.lookAt(state.position, state.target)', `renderer.lookAt(${JSON.stringify(camera.eye)}, ${JSON.stringify(camera.target)})`) })
      })
      await leg.goto(`http://127.0.0.1:5343/?name=S3-09%20${camera.name}`, { waitUntil: 'networkidle' })
      await leg.waitForSelector('#loading.hidden', { state: 'attached', timeout: 180000 })
      await leg.waitForTimeout(1200)
      const file = path.join(__dirname, `${camera.name}.png`)
      await leg.screenshot({ path: file })
      legCameras.push({ ...camera, file, sha256: hash(await fs.readFile(file)), source_sha256: nbtHash, geometry_modified: false })
      await leg.close()
    }
    if (nbtHash !== hash(await fs.readFile(path.join(__dirname, 'room.nbt')))) throw new Error('NBT changed while capturing')
    await fs.writeFile(path.join(__dirname, 'view-map.json'), JSON.stringify({ final_nbt_sha256: nbtHash, cutaway_sha256: previewHash, renderer: 'bundled Lodestone official viewer/ThreeStructureRenderer; headless Edge WebGL; actual NBT', view_directions: 'four diagonal quadrants, not orthographic cardinal images', page_errors: errors, views, additional_full_nbt_interior: interiorCamera, additional_leg_cameras: legCameras }, null, 2))
    if (errors.length) throw new Error(errors.join('\n'))
  } finally {
    await browser.close()
  }
}

main().catch(error => { console.error(error); process.exitCode = 1 })
