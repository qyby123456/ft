const fs = require('node:fs')
const path = require('node:path')
const crypto = require('node:crypto')
const { chromium } = require('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright')

async function main() {
  const browser = await chromium.launch({ channel: 'msedge', headless: true, args: ['--use-angle=swiftshader', '--enable-unsafe-swiftshader'] })
  const records = []
  try {
    for (const [direction, rotation] of [['north', Math.PI], ['east', -Math.PI / 2], ['south', 0], ['west', Math.PI / 2]]) {
      const bytes = fs.readFileSync(path.join(__dirname, 'directional', direction, `inspection-only-${direction}.nbt`))
      const page = await browser.newPage({ viewport: { width: 1400, height: 1080 } })
      const errors = []
      page.on('pageerror', error => errors.push(error.message))
      await page.route('**/src/main.js', async route => {
        const response = await route.fetch()
        const source = (await response.text()).replace('this.yRotation = 0.82', `this.yRotation = ${rotation}`).replace('Math.max(...size) * 1.9', 'Math.max(...size) * 1.35')
        await route.fulfill({ response, body: source })
      })
      await page.route('**/__minecraft_builder__/build.nbt', route => route.fulfill({ status: 200, contentType: 'application/octet-stream', body: bytes }))
      await page.goto(`http://127.0.0.1:5588/?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=INSPECTION-ONLY-${direction}`, { waitUntil: 'networkidle', timeout: 120000 })
      await page.waitForFunction(() => document.getElementById('loading').classList.contains('hidden'), { timeout: 120000 })
      await page.evaluate(() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve))))
      const servedHash = await page.evaluate(async () => {
        const bytes = await (await fetch('/__minecraft_builder__/build.nbt')).arrayBuffer()
        return Array.from(new Uint8Array(await crypto.subtle.digest('SHA-256', bytes))).map(value => value.toString(16).padStart(2, '0')).join('')
      })
      const hash = crypto.createHash('sha256').update(bytes).digest('hex')
      if (servedHash !== hash || errors.length) throw new Error('NBT hash/page error ' + direction + errors.join(';'))
      const image = `directional/${direction}/lodestone-${direction}.png`
      await page.screenshot({ path: path.join(__dirname, image) })
      records.push({ direction, camera_y_rotation: rotation, nbt: `directional/${direction}/inspection-only-${direction}.nbt`, derived_sha256: hash, browser_fetched_sha256: servedHash, screenshot: image, screenshot_sha256: crypto.createHash('sha256').update(fs.readFileSync(path.join(__dirname, image))).digest('hex'), page_errors: errors, stats: await page.locator('#build-stats').innerText(), renderer: 'Bundled Lodestone WebGL; browser-only camera and NBT response override; unique direction-specific source bytes' })
      await page.close()
    }
  } finally {
    await browser.close()
  }
  fs.writeFileSync(path.join(__dirname, 'directional-browser.json'), JSON.stringify({ status: 'PASS_LOAD_AND_HASHES', complete_candidate_modified: false, records }, null, 2))
  console.log('Four unique cardinal cutaway WebGL previews captured')
}

main().catch(error => { console.error(error); process.exitCode = 1 })
