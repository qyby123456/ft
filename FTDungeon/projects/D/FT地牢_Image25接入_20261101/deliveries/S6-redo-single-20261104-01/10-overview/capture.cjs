const { chromium } = require('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright')
const fs = require('node:fs')
const path = require('node:path')
const crypto = require('node:crypto')
const root = __dirname
const hash = file => crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex')

async function main() {
  const requestedMode = process.argv[2]
  const previous = requestedMode ? JSON.parse(fs.readFileSync(path.join(root, 'lodestone/screenshots.json'), 'utf8')) : undefined
  const browser = await chromium.launch({channel: 'msedge', headless: true, args: ['--enable-webgl', '--use-angle=swiftshader', '--enable-unsafe-swiftshader']})
  const page = await browser.newPage({viewport: {width: 1600, height: 1100}, deviceScaleFactor: 1})
  const errors = []
  const instrumentation = previous?.camera_only_response_instrumentation ?? []
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
  const records = previous ? previous.screenshots.filter(record => record.file !== `lodestone-${requestedMode}.png`) : []
  const modes = [
    ['exterior', 5481, {position: [-38, 76, -39], target: [32, 5, 37]}],
    ['exterior-opposite', 5481, {position: [102, 76, 112], target: [32, 5, 37]}],
    ['interior-cutaway', 5482, {position: [-9, 53, -8], target: [32, 3, 37]}],
    ['layout-overhead', 5482, {position: [32, 78, 36.9], target: [32, 2, 37]}],
    ['treasure-detail', 5483, {position: [9.5, 4.62, 10.5], target: [9.5, 4.2, 17]}],
    ['eye-entry', 5481, {position: [32.5, 4.62, 2.5], target: [32.5, 4.62, 23]}],
    ['eye-court', 5481, {position: [24.5, 4.62, 18.5], target: [34, 3.4, 29]}],
    ['eye-west-medium', 5481, {position: [10.5, 4.62, 30.5], target: [10.5, 4.62, 41.5]}],
    ['eye-west-pool', 5481, {position: [18.5, 4.62, 52.5], target: [8, 3.2, 61]}],
    ['eye-great-hall', 5481, {position: [34.5, 4.62, 46.5], target: [34.5, 6, 68]}],
    ['eye-hall-reverse', 5481, {position: [34.5, 6.62, 67.5], target: [34.5, 5, 46]}],
    ['eye-treasure', 5481, {position: [55.5, 4.62, 60.5], target: [55.5, 4.2, 67]}],
    ['eye-east-small', 5481, {position: [57.5, 4.62, 27.5], target: [53, 3.7, 20]}],
    ['eye-L-turn', 5481, {position: [50.5, 4.62, 35.5], target: [57.5, 4.62, 36.5]}],
    ['eye-L-long-arm', 5481, {position: [57.5, 4.62, 33.5], target: [57.5, 4.62, 51.5]}],
    ['west-supply', 5481, {position: [5.5, 4.62, 33.5], target: [5.5, 3.7, 36.5]}],
    ['east-supply', 5481, {position: [59.5, 4.62, 23.5], target: [59.5, 3.7, 26.5]}]
  ]
  for (const [mode, port, camera] of modes) {
    if (requestedMode && mode !== requestedMode) continue
    const url = `http://127.0.0.1:${port}/?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=S6-10-${mode}`
    const response = await page.request.get(`http://127.0.0.1:${port}/__minecraft_builder__/build.nbt`, {maxRetries: 3})
    const loadedHash = crypto.createHash('sha256').update(await response.body()).digest('hex')
    const file = port === 5481 ? path.join(root, 'room.nbt') : port === 5483 ? path.join(root, 'lodestone/treasure-detail.nbt') : path.join(root, 'lodestone/inspection-cutaway.nbt')
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
  console.log('Captured seventeen actual Lodestone full-dungeon, cutaway, region and container approach views')
}
main().catch(error => {console.error(error); process.exitCode = 1})
