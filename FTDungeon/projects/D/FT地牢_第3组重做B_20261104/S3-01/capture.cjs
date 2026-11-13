const { chromium } = require('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright')
const path = require('node:path')
const fs = require('node:fs/promises')
const crypto = require('node:crypto')

async function main() {
  const browser = await chromium.launch({ channel: 'msedge', headless: true, args: ['--enable-webgl', '--ignore-gpu-blocklist', '--use-angle=swiftshader'] })
  const page = await browser.newPage({ viewport: { width: 1440, height: 1100 }, deviceScaleFactor: 1 })
  const errors = []
  page.on('pageerror', error => errors.push(error.message))
  for (const [port, prefix] of [[5294, 'interior'], [5293, 'exterior']]) {
    await page.goto(`http://127.0.0.1:${port}/?name=S3-01%20REWORK%20B`, { waitUntil: 'networkidle' })
    await page.waitForSelector('#loading.hidden', { state: 'attached', timeout: 180000 })
    await page.waitForTimeout(1500)
    await page.mouse.move(700, 550)
    await page.mouse.wheel(0, -200)
    await page.waitForTimeout(500)
    await page.screenshot({ path: path.join(__dirname, `${prefix}-01.png`) })
    for (let index = 2; index <= 4; index++) {
      await page.mouse.move(650, 550)
      await page.mouse.down()
      await page.mouse.move(901.327, 550, { steps: 20 })
      await page.mouse.up()
      await page.waitForTimeout(500)
      await page.screenshot({ path: path.join(__dirname, `${prefix}-0${index}.png`) })
    }
    if (prefix === 'interior') {
      await page.screenshot({ path: path.join(__dirname, 'preview.png') })
      await page.click('#lighting-toggle')
      await page.waitForTimeout(800)
      await page.screenshot({ path: path.join(__dirname, 'preview-night.png') })
    }
  }
  const sha256 = crypto.createHash('sha256').update(await fs.readFile(path.join(__dirname, 'room.nbt'))).digest('hex')
  const cutaway_sha256 = crypto.createHash('sha256').update(await fs.readFile(path.join(__dirname, 'preview-only/preview-only-cutaway.nbt'))).digest('hex')
  await fs.writeFile(path.join(__dirname, 'render-check.json'), JSON.stringify({ renderer: 'bundled Lodestone ThreeStructureRenderer, official viewer, actual exported NBT', pages: [5293, 5294], views: 8, sha256, cutaway_sha256, page_errors: errors }, null, 2))
  await browser.close()
  if (errors.length) throw new Error(errors.join('\n'))
}

main().catch(error => { console.error(error); process.exitCode = 1 })
