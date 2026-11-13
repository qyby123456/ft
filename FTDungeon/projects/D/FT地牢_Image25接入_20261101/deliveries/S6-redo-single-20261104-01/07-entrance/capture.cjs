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
    ['exterior', 5451, {position: [-7, 23, -10], target: [13, 5, 17]}],
    ['exterior-opposite', 5451, {position: [35, 24, 45], target: [13, 5, 17]}],
    ['interior-cutaway', 5452, {position: [-5, 23, -5], target: [13, 5, 18]}],
    ['court-overhead', 5452, {position: [13, 35, 14], target: [13, 4, 17]}],
    ['gate-stairs-closeup', 5453, {position: [-3, 9, -5], target: [4, 4, 4]}],
    ['eye-level-approach', 5451, {position: [13.5, 4.62, 1.5], target: [13, 6.62, 15]}],
    ['eye-level-court', 5451, {position: [13.5, 6.62, 12.5], target: [13.5, 6.62, 31]}],
    ['eye-level-rear', 5451, {position: [13.5, 6.62, 30.5], target: [13, 6.62, 8]}],
    ['guard-niche', 5451, {position: [11, 8, 19], target: [6, 7, 24]}]
  ]
  for (const [mode, port, camera] of modes) {
    const url = `http://127.0.0.1:${port}/?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=S6-07-${mode}`
    const response = await page.request.get(`http://127.0.0.1:${port}/__minecraft_builder__/build.nbt`)
    const loadedHash = crypto.createHash('sha256').update(await response.body()).digest('hex')
    const file = port === 5451 ? path.join(root, 'room.nbt') : port === 5453 ? path.join(root, 'lodestone/gate-stairs-detail.nbt') : path.join(root, 'lodestone/inspection-cutaway.nbt')
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
  console.log('Captured nine actual Lodestone full/court/gate-stair and final-NBT eye-level/guard views')
}
main().catch(error => {console.error(error); process.exitCode = 1})
