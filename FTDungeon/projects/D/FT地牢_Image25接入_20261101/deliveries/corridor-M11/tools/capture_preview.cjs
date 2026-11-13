const fs = require('node:fs/promises')
const path = require('node:path')
const { spawn } = require('node:child_process')
const { createHash } = require('node:crypto')
const { chromium } = require('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright')

const root = path.resolve(__dirname, '..')
const output = path.join(root, 'qa', 'render-raw')
const previewScript = 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/preview.mjs'
const port = Number(process.env.M11_PREVIEW_PORT || 5311)
const url = `http://127.0.0.1:${port}/?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=corridor-M11`
const views = [
  { name: 'hero', mode: 'complete', position: [7.5, 4.4, 0.65], target: [7.5, 4.4, 39], fov: 66 },
  { name: 'north', mode: 'cutaway', position: [-25, 31, -20], target: [7.5, 4.5, 20], fov: 40 },
  { name: 'south', mode: 'cutaway', position: [40, 31, 60], target: [7.5, 4.5, 20], fov: 40 },
  { name: 'west', mode: 'cutaway', position: [-43, 31, 12], target: [7.5, 4.5, 20], fov: 43 },
  { name: 'east', mode: 'cutaway', position: [58, 31, 28], target: [7.5, 4.5, 20], fov: 43 },
  { name: 'exterior', mode: 'complete', position: [-25, 36, -20], target: [7.5, 5.5, 20], fov: 43 },
]

async function main() {
  await fs.mkdir(output, { recursive: true })
  let server
  try {
    await fetch(url)
  } catch {
    server = spawn(process.execPath, [previewScript, '--nbt', path.join(root, 'room.nbt'), '--port', String(port), '--no-open'], { windowsHide: true, stdio: 'ignore' })
    for (let attempt = 0; attempt < 60; attempt += 1) {
      try {
        await fetch(url)
        break
      } catch {
        await new Promise(resolve => setTimeout(resolve, 500))
      }
    }
  }
  const sha256 = createHash('sha256').update(await fs.readFile(path.join(root, 'room.nbt'))).digest('hex')
  const served = Buffer.from(await (await fetch(`http://127.0.0.1:${port}/__minecraft_builder__/build.nbt`)).arrayBuffer())
  if (createHash('sha256').update(served).digest('hex') !== sha256) throw new Error('Preview server is serving a different NBT')
  const browser = await chromium.launch({ executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe', headless: true, args: ['--enable-webgl', '--use-angle=swiftshader', '--enable-unsafe-swiftshader'] })
  try {
    const page = await browser.newPage({ viewport: { width: 1536, height: 1024 }, deviceScaleFactor: 1 })
    const errors = []
    page.on('pageerror', error => errors.push(error.message))
    await page.route('**/src/main.js*', async route => {
      const response = await route.fetch()
      const source = (await response.text()).replace('await renderer.whenReady()', 'await renderer.whenReady(); globalThis.__m11 = {structure, pack, renderer, camera, requestRender}')
      await route.fulfill({ response, body: source })
    })
    await page.goto(url, { waitUntil: 'domcontentloaded', timeout: 60000 })
    await page.waitForFunction(() => Boolean(globalThis.__m11), { timeout: 120000 })
    await page.addStyleTag({ content: '#hud, .hud, #lighting-toggle, #loading, header, .controls, .help {display:none!important}' })
    for (const view of views) {
      const info = await page.evaluate(async settings => {
        const original = __m11.structure
        const visible = new original.constructor(original.getSize().slice())
        const nearWest = settings.position[0] < 7.5
        const nearNorth = settings.position[2] < 20
        let omitted = 0
        for (const block of original.getBlocks()) {
          const [posX, posY, posZ] = block.pos
          const nearWall = nearWest ? posX <= 2 : posX >= 12
          const nearEnd = nearNorth ? posZ === 0 : posZ === 39
          if (settings.mode === 'cutaway' && (posY >= 10 || posY >= 3 && (nearWall || nearEnd))) {
            omitted += 1
            continue
          }
          visible.addBlock(block.pos, block.state.name, block.state.getProperties ? block.state.getProperties() : block.state.properties, block.nbt)
        }
        __m11.renderer.setStructure(settings.mode === 'complete' ? original : visible)
        await __m11.renderer.whenReady()
        __m11.camera.getCamera = () => ({ position: settings.position, target: settings.target })
        __m11.renderer.setFOV(settings.fov)
        const interior = settings.name === 'hero'
        __m11.renderer.setSunlight({
          direction: [0.3, 0.85, -0.25], color: [1, 0.93, 0.8], intensity: interior ? 0.2 : 1.05,
          ambientColor: [1, 0.91, 0.77], ambientIntensity: interior ? 0.4 : 0.6,
          fillColor: [0.7, 0.77, 0.86], fillIntensity: 0.16, rimColor: [1, 0.8, 0.55], rimIntensity: 0.1,
          exposure: 1.03,
          sky: { zenithColor: [0.025, 0.035, 0.045], horizonColor: [0.022, 0.028, 0.03], groundColor: [0.018, 0.022, 0.025], sunGlowIntensity: 0, stars: { enabled: false } },
          disc: { coreIntensity: 0, glowIntensity: 0 },
          fog: { color: [0.025, 0.028, 0.025], density: interior ? 0.00025 : 0.00001, heightFalloff: 0.001 },
          shadow: { enabled: interior, intensity: interior ? 0.35 : 0.45, softness: 2 },
          emissive: { range: 16, intensity: interior ? 1.6 : 0.9, tint: [1, 0.7, 0.35] },
          postProcess: { enabled: false },
        })
        __m11.requestRender()
        return { source_records: original.getBlocks().length, visible_records: settings.mode === 'complete' ? original.getBlocks().length : visible.getBlocks().length, omitted_records: omitted }
      }, view)
      await page.waitForTimeout(500)
      await page.locator('#preview').screenshot({ path: path.join(output, `${view.name}.png`) })
      Object.assign(view, info)
      console.log(`M11_RENDERED ${view.name} omitted=${info.omitted_records}`)
    }
    if (errors.length) throw new Error(errors.join('\n'))
    await fs.writeFile(path.join(output, 'capture.json'), JSON.stringify({ renderer: 'Lodestone ThreeStructureRenderer, vanilla default-pack, final room.nbt', sha256, served_nbt_sha256: sha256, url, size: [1536, 1024], views, errors, gameplay_screenshot: false }, null, 2) + '\n')
  } finally {
    await browser.close()
    if (server) server.kill()
  }
}

main().catch(error => { console.error(error); process.exitCode = 1 })
