const { chromium } = require('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright')
const fs = require('node:fs')
const path = require('node:path')
const crypto = require('node:crypto')

async function main() {
  const browser = await chromium.launch({ channel: 'msedge', headless: true, args: ['--enable-webgl', '--use-gl=angle', '--use-angle=swiftshader', '--enable-unsafe-swiftshader'] })
  const page = await browser.newPage({ viewport: { width: 1536, height: 1100 }, deviceScaleFactor: 1 })
  const errors = []
  const views = []
  page.on('pageerror', error => errors.push(String(error)))
  await page.route('**/src/main.js*', async route => {
    const response = await route.fetch()
    const body = (await response.text()).replace('const camera = new OrbitCamera(canvas, center, Math.max(18, Math.max(...size) * 1.9), requestRender)', 'const camera = new OrbitCamera(canvas, center, Math.max(18, Math.max(...size) * 1.9), requestRender); window.qaCamera = camera')
    await route.fulfill({ response, body })
  })
  async function load(port, label, relative) {
    const url = `http://127.0.0.1:${port}/?name=${encodeURIComponent(label)}`
    await page.goto(url, { waitUntil: 'networkidle', timeout: 120000 })
    await page.waitForFunction(() => document.querySelector('#loading').classList.contains('hidden'), { timeout: 120000 })
    const response = await page.request.get(`http://127.0.0.1:${port}/__minecraft_builder__/build.nbt`)
    const bytes = await response.body()
    if (!bytes.equals(fs.readFileSync(path.join(__dirname, relative)))) throw new Error('Served NBT mismatch: ' + label)
    return { url, sha256: crypto.createHash('sha256').update(bytes).digest('hex') }
  }
  async function shot(filename, settings, source) {
    await page.evaluate(settings => {
      const camera = window.qaCamera
      camera.center.set(settings.center)
      camera.distance = settings.distance
      camera.xRotation = settings.pitch
      camera.yRotation = settings.yaw
      camera.onChange()
    }, settings)
    await page.waitForTimeout(1300)
    await page.screenshot({ path: path.join(__dirname, filename) })
    views.push({ filename, camera: settings, ...source })
  }
  const full = await load(5504, 'S4-10 FULL SOURCE - seven-region swamp complex', 'incoming/S4-10/room.nbt')
  await shot('preview-exterior.png', { center: [36.5, 6, 36.5], distance: 74, pitch: 0.76, yaw: -0.70 }, full)
  await shot('preview-reverse.png', { center: [36.5, 6, 36.5], distance: 74, pitch: 0.76, yaw: 2.44 }, full)
  await shot('preview-plan.png', { center: [36.5, 4, 36.5], distance: 76, pitch: 1.49, yaw: 0 }, full)
  await shot('preview-entry.png', { center: [38.5, 8, 59], distance: 25, pitch: 0.15, yaw: -0.12 }, full)
  await shot('preview-courtyard.png', { center: [38.5, 7, 36], distance: 8, pitch: 0.12, yaw: 0 }, full)
  await shot('preview-library.png', { center: [12, 7, 14], distance: 8, pitch: 0.20, yaw: 0.35 }, full)
  await shot('preview-north-hall.png', { center: [38, 8, 12], distance: 10, pitch: 0.14, yaw: 0 }, full)
  await shot('preview-treasure.png', { center: [62.5, 7.5, 15.5], distance: 7, pitch: 0.19, yaw: 0.20 }, full)
  await shot('preview-flooded-hall.png', { center: [13, 8, 45], distance: 12, pitch: 0.27, yaw: 0.20 }, full)
  await shot('preview-workshop.png', { center: [60, 7, 45], distance: 12, pitch: 0.23, yaw: -0.55 }, full)
  await shot('preview-turn-corridor.png', { center: [21, 5.5, 28], distance: 18, pitch: 1.10, yaw: -0.35 }, full)
  await page.locator('#lighting-toggle').click()
  await shot('preview-night.png', { center: [36.5, 6, 36.5], distance: 74, pitch: 0.76, yaw: -0.70 }, full)
  const cut = await load(5505, 'S4-10 PREVIEW ONLY - Y10+ CUT TO AIR', 'preview-only/upper-cutaway.nbt')
  await shot('preview-cutaway.png', { center: [36.5, 4, 36.5], distance: 74, pitch: 0.90, yaw: -0.70 }, cut)
  fs.writeFileSync(path.join(__dirname, 'preview-check.json'), JSON.stringify({ status: errors.length ? 'FAIL' : 'PASS', sha256: full.sha256, renderer: 'Actual dense full-source NBT / Lodestone ThreeStructureRenderer / Edge WebGL; camera-only injection, no visual or geometry substitution. Regional screenshots all use the same uncut full source.', views, errors }, null, 2))
  await browser.close()
  if (errors.length) throw new Error(errors.join('\n'))
}
main().catch(error => { console.error(error); process.exit(1) })
