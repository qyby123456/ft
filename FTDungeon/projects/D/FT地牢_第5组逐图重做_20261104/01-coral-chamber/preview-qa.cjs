const { chromium } = require('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright')
const fs = require('node:fs')
const path = require('node:path')
async function main() {
  const browser = await chromium.launch({headless: true, args: ['--enable-webgl', '--use-angle=swiftshader', '--enable-unsafe-swiftshader']})
  const page = await browser.newPage({viewport: {width: 1440, height: 1100}, deviceScaleFactor: 1})
  const errors = []
  page.on('pageerror', error => {errors.push(error.message); console.error(error.message)})
  page.on('console', message => {if (message.type() === 'error') console.error(message.text())})
  const port = process.env.PREVIEW_PORT || '5275'
  const suffix = process.env.PREVIEW_SUFFIX || ''
  await page.goto(`http://127.0.0.1:${port}/?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=S5-01${suffix}`, {waitUntil: 'domcontentloaded'})
  await page.waitForSelector('#loading.hidden', {state: 'attached', timeout: 180000})
  const canvas = page.locator('canvas')
  await canvas.hover()
  await page.mouse.wheel(0, -150)
  await page.waitForTimeout(1800)
  await page.screenshot({path: path.join(__dirname, `qa/lodestone-front${suffix}.png`)})
  const bounds = await canvas.boundingBox()
  await page.mouse.move(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2)
  await page.mouse.down()
  await page.mouse.move(bounds.x + bounds.width / 2 + 190, bounds.y + bounds.height / 2 + 45, {steps: 20})
  await page.mouse.up()
  await page.waitForTimeout(1500)
  await page.screenshot({path: path.join(__dirname, `qa/lodestone-rotated${suffix}.png`)})
  fs.writeFileSync(path.join(__dirname, `qa/preview${suffix}.json`), JSON.stringify({status: errors.length ? 'FAIL' : 'PASS', errors, stats: await page.locator('#build-stats').innerText(), renderer: 'Lodestone ThreeStructureRenderer', view: suffix ? 'Water hidden inspection only, not importable candidate' : 'Actual final NBT'}, null, 2))
  await browser.close()
  if (errors.length) process.exitCode = 1
}
main().catch(error => {console.error(error); process.exitCode = 1})
