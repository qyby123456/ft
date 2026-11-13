const { chromium } = require('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright')
const fs = require('node:fs')
const path = require('node:path')
const crypto = require('node:crypto')
const root = __dirname
const hash = file => crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex')

async function main() {
  const browser = await chromium.launch({channel: 'msedge', headless: true, args: ['--enable-webgl', '--use-angle=swiftshader', '--enable-unsafe-swiftshader']})
  const page = await browser.newPage({viewport: {width: 1600, height: 1100}, deviceScaleFactor: 1})
  const errors = []
  page.on('pageerror', error => errors.push(String(error)))
  const records = []
  for (const [mode, port] of [['exterior', 5381], ['interior-cutaway', 5382], ['rear-arches-front', 5382], ['right-cascade-inspection', 5382]]) {
    const url = `http://127.0.0.1:${port}/?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=S6-01-R3-${mode}`
    const response = await page.request.get(`http://127.0.0.1:${port}/__minecraft_builder__/build.nbt`)
    const loadedHash = crypto.createHash('sha256').update(await response.body()).digest('hex')
    const file = mode === 'exterior' ? path.join(root, 'room.nbt') : path.join(root, 'lodestone/inspection-cutaway.nbt')
    if (loadedHash !== hash(file)) throw new Error('Preview endpoint hash mismatch')
    await page.goto(url)
    await page.waitForFunction(() => document.querySelector('#loading')?.classList.contains('hidden'), {timeout: 120000})
    const canvas = page.locator('#preview')
    const bounds = await canvas.boundingBox()
    if (mode !== 'exterior') {
      await page.mouse.move(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2)
      await page.mouse.down()
      const shiftX = mode === 'rear-arches-front' ? 371 : mode === 'right-cascade-inspection' ? 165 : 250
      const shiftY = mode === 'rear-arches-front' ? -30 : 0
      await page.mouse.move(bounds.x + bounds.width / 2 + shiftX, bounds.y + bounds.height / 2 + shiftY, {steps: 20})
      await page.mouse.up()
    }
    await page.mouse.move(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2)
    await page.mouse.wheel(0, -190)
    await page.waitForTimeout(2000)
    const filename = `lodestone-${mode}.png`
    await page.screenshot({path: path.join(root, 'lodestone', filename), fullPage: true})
    records.push({file: filename, sha256: hash(path.join(root, 'lodestone', filename)), url, loaded_nbt_sha256: loadedHash, viewer_stats: await page.locator('#build-stats').innerText(), capture: 'actual browser screenshot of bundled Lodestone ThreeStructureRenderer, WebGL SwiftShader; no software voxel render substitution'})
  }
  fs.writeFileSync(path.join(root, 'lodestone/screenshots.json'), JSON.stringify({final_nbt_sha256: hash(path.join(root, 'room.nbt')), cutaway: JSON.parse(fs.readFileSync(path.join(root, 'lodestone/cutaway-provenance.json'), 'utf8')), screenshots: records, page_errors: errors, not_a_game_screenshot: true}, null, 2))
  await browser.close()
  if (errors.length) throw new Error(errors.join('\n'))
  console.log('Captured actual Lodestone exterior and cutaway-interior screenshots, final NBT hashes bound')
}
main().catch(error => {console.error(error); process.exitCode = 1})
