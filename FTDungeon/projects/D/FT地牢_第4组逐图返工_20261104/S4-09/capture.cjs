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
  const full = await load(5494, 'S4-09 FULL CANDIDATE - twin-warden treasure sanctum', 'incoming/S4-09/room.nbt')
  await shot('preview-exterior.png', { center: [15.5, 8, 15.5], distance: 45, pitch: 0.57, yaw: -0.72 }, full)
  await shot('preview-reverse.png', { center: [15.5, 8, 15.5], distance: 45, pitch: 0.57, yaw: 2.42 }, full)
  await shot('preview-eye.png', { center: [15.5, 9, 8], distance: 15, pitch: 0.08, yaw: 0 }, full)
  await shot('preview-chest.png', { center: [15.5, 10, 7.5], distance: 8, pitch: 0.18, yaw: 0.25 }, full)
  await shot('preview-wardens.png', { center: [15.5, 10, 7.5], distance: 21, pitch: 0.28, yaw: 0 }, full)
  await shot('preview-pottery.png', { center: [6.5, 10, 4.5], distance: 10, pitch: 0.18, yaw: 0.12 }, full)
  await page.locator('#lighting-toggle').click()
  await shot('preview-night.png', { center: [15.5, 8, 15.5], distance: 45, pitch: 0.57, yaw: -0.72 }, full)
  const cut = await load(5495, 'S4-09 PREVIEW ONLY - Y14+ CUT TO AIR', 'preview-only/upper-cutaway.nbt')
  await shot('preview-cutaway.png', { center: [15.5, 6, 15.5], distance: 43, pitch: 0.85, yaw: -0.72 }, cut)
  fs.writeFileSync(path.join(__dirname, 'preview-check.json'), JSON.stringify({ status: errors.length ? 'FAIL' : 'PASS', sha256: full.sha256, renderer: 'Actual NBT / Lodestone ThreeStructureRenderer / Edge WebGL; camera-only injection, no visual or geometry substitution.', views, errors }, null, 2))
  await browser.close()
  if (errors.length) throw new Error(errors.join('\n'))
}
main().catch(error => { console.error(error); process.exit(1) })

