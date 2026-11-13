const fs = require('node:fs')
const path = require('node:path')
const crypto = require('node:crypto')
const { chromium } = require('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright')

async function main() {
  const browser = await chromium.launch({ channel: 'msedge', headless: true, args: ['--use-angle=swiftshader', '--enable-unsafe-swiftshader'] })
  const records = []
  try {
    for (const [label, port, rotation] of [['complete', 5588, 2.5], ['cutaway-north', 5589, 3.14], ['cutaway-east', 5589, -1.57], ['cutaway-south', 5589, 0], ['cutaway-west', 5589, 1.57]]) {
      const page = await browser.newPage({ viewport: { width: 1400, height: 1080 } })
      const errors = []
      page.on('pageerror', error => errors.push(error.message))
      await page.route('**/src/main.js', async route => {
        const response = await route.fetch()
        const source = (await response.text()).replace('this.yRotation = 0.82', `this.yRotation = ${rotation}`).replace('Math.max(...size) * 1.9', 'Math.max(...size) * 1.35')
        await route.fulfill({ response, body: source })
      })
      await page.goto(`http://127.0.0.1:${port}/?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=S8-01-${label}`, { waitUntil: 'networkidle', timeout: 120000 })
      await page.waitForFunction(() => document.getElementById('loading').classList.contains('hidden'), { timeout: 120000 })
      await page.evaluate(() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve))))
      const target = `qa/takeover-lodestone-${label}.png`
      await page.screenshot({ path: path.join(__dirname, target) })
      const response = await page.request.get(`http://127.0.0.1:${port}/__minecraft_builder__/build.nbt`)
      const servedHash = crypto.createHash('sha256').update(await response.body()).digest('hex')
      records.push({ label, screenshot: target, served_sha256: servedHash, stats: await page.locator('#build-stats').innerText(), page_errors: errors, camera_y_rotation: rotation, source: label === 'complete' ? 'FINAL_NBT' : 'DERIVED_INSPECTION_ONLY_NOT_DELIVERABLE' })
      await page.close()
      if (errors.length) throw new Error(errors.join('; '))
    }
    fs.writeFileSync(path.join(__dirname, 'qa/takeover-lodestone.json'), JSON.stringify({ status: 'PASS', renderer: 'bundled Lodestone ThreeStructureRenderer WebGL; camera-only browser override', records }, null, 2))
    console.log('Captured complete NBT and four inspection cutaways')
  } finally {
    await browser.close()
  }
}

main().catch(error => { console.error(error); process.exitCode = 1 })
