const { chromium } = require('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright')
const fs = require('node:fs')
const path = require('node:path')
const crypto = require('node:crypto')

async function main() {
  const browser = await chromium.launch({ channel: 'msedge', headless: true, args: ['--enable-webgl', '--use-gl=angle', '--use-angle=swiftshader', '--enable-unsafe-swiftshader'] })
  const page = await browser.newPage({ viewport: { width: 1536, height: 1024 }, deviceScaleFactor: 1 })
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
    await page.waitForTimeout(1200)
    await page.screenshot({ path: path.join(__dirname, filename) })
    views.push({ filename, camera: settings, ...source })
  }
  const full = await load(5424, 'S4-06 FULL CANDIDATE - open L corner', 'incoming/S4-06/room.nbt')
  await shot('preview-exterior.png', { center: [12.5, 5, 12.5], distance: 37, pitch: 0.58, yaw: 0.82 }, full)
  await shot('preview-corner.png', { center: [10, 6, 14], distance: 14, pitch: 0.30, yaw: 0.75 }, full)
  await shot('preview-interior-north-arm.png', { center: [5.5, 6, 9], distance: 9, pitch: 0.02, yaw: 0 }, full)
  await shot('preview-interior-east-arm.png', { center: [15, 6, 19.5], distance: 9, pitch: 0.02, yaw: Math.PI / 2 }, full)
  await page.locator('#lighting-toggle').click()
  await shot('preview-interior-night.png', { center: [10, 6, 14], distance: 14, pitch: 0.30, yaw: 0.75 }, full)
  const cut = await load(5425, 'S4-06 PREVIEW ONLY - Y8+ CUT TO AIR', 'preview-only/upper-cutaway.nbt')
  await shot('preview-cutaway.png', { center: [12.5, 4, 12.5], distance: 35, pitch: 0.85, yaw: 0.82 }, cut)
  fs.writeFileSync(path.join(__dirname, 'preview-check.json'), JSON.stringify({ status: errors.length ? 'FAIL' : 'PASS', sha256: full.sha256, renderer: 'Actual NBT / Lodestone ThreeStructureRenderer / Edge WebGL; camera-only injection, no visual or geometry substitution.', views, errors }, null, 2))
  await browser.close()
  if (errors.length) throw new Error(errors.join('\n'))
}
main().catch(error => { console.error(error); process.exit(1) })

