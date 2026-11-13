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
  const instrumentation = []
  page.on('pageerror', error => errors.push(String(error)))
  await page.route('**/src/main.js*', async route => {
    const response = await route.fetch()
    const source = await response.text()
    const hook = '  function render() {'
    if (!source.includes(hook)) throw new Error('Bundled camera hook unavailable')
    const body = source.replace(hook, '  window.__inspectionCamera = {camera, renderer, requestRender};\n' + hook)
    instrumentation.push({url: route.request().url(), original_source_sha256: crypto.createHash('sha256').update(source).digest('hex'), served_source_sha256: crypto.createHash('sha256').update(body).digest('hex'), change: 'expose existing renderer/camera/requestRender to page for eye-level camera positioning only; disk skill files unchanged'})
    await route.fulfill({response, body})
  })
  const records = []
  const modes = [
    ['exterior', 5461, {position: [-14, 39, -17], target: [18, 9, 18]}],
    ['exterior-opposite', 5461, {position: [52, 39, 54], target: [18, 9, 18]}],
    ['interior-cutaway', 5462, {position: [-12, 32, -12], target: [18, 8, 18]}],
    ['court-overhead', 5462, {position: [18, 57, 17], target: [18, 3, 18]}],
    ['fountain-closeup', 5463, {position: [-7, 15, -10], target: [6, 6, 6]}],
    ['eye-level-court', 5461, {position: [18.5, 4.62, 6.5], target: [18.5, 6, 18.5]}],
    ['eye-level-rear', 5461, {position: [18.5, 4.62, 30.5], target: [18.5, 6, 18.5]}],
    ['eye-level-gallery', 5461, {position: [18.5, 14.62, 5.5], target: [18.5, 10, 20]}],
    ['stair-gallery', 5461, {position: [7.5, 15, 21], target: [5.5, 8, 11]}],
    ['eye-level-diagonal', 5461, {position: [10.5, 4.62, 7.5], target: [18.5, 10, 18.5]}]
  ]
  for (const [mode, port, camera] of modes) {
    const url = `http://127.0.0.1:${port}/?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=S6-08-${mode}`
    const response = await page.request.get(`http://127.0.0.1:${port}/__minecraft_builder__/build.nbt`)
    const loadedHash = crypto.createHash('sha256').update(await response.body()).digest('hex')
    const file = port === 5461 ? path.join(root, 'room.nbt') : port === 5463 ? path.join(root, 'lodestone/fountain-detail.nbt') : path.join(root, 'lodestone/inspection-cutaway.nbt')
    if (loadedHash !== hash(file)) throw new Error('Preview endpoint hash mismatch')
    await page.goto(url)
    await page.waitForFunction(() => document.querySelector('#loading')?.classList.contains('hidden'), undefined, {timeout: 120000})
    await page.evaluate(view => {
        const inspector = window.__inspectionCamera
        if (!inspector) throw new Error('Camera bridge not exposed')
        inspector.camera.getCamera = () => view
        inspector.requestRender()
    }, camera)
    await page.waitForTimeout(2000)
    const filename = `lodestone-${mode}.png`
    await page.screenshot({path: path.join(root, 'lodestone', filename), fullPage: true})
    records.push({file: filename, sha256: hash(path.join(root, 'lodestone', filename)), url, loaded_nbt_sha256: loadedHash, camera, viewer_stats: await page.locator('#build-stats').innerText(), capture: 'actual bundled Lodestone ThreeStructureRenderer browser screenshot; WebGL SwiftShader; camera hook only, original blocks/materials/rendering. Not in-game or software voxel rendering.'})
  }
  fs.writeFileSync(path.join(root, 'lodestone/screenshots.json'), JSON.stringify({final_nbt_sha256: hash(path.join(root, 'room.nbt')), cutaway: JSON.parse(fs.readFileSync(path.join(root, 'lodestone/cutaway-provenance.json'), 'utf8')), screenshots: records, page_errors: errors, camera_only_response_instrumentation: instrumentation, disk_skill_modified: false, not_a_game_screenshot: true}, null, 2))
  await browser.close()
  if (errors.length) throw new Error(errors.join('\n'))
  console.log('Captured ten actual Lodestone full/court/fountain and final-NBT ground/gallery/stair views')
}
main().catch(error => {console.error(error); process.exitCode = 1})
