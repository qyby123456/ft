const fs = require('node:fs/promises')
const path = require('node:path')
const crypto = require('node:crypto')
const { chromium } = require(process.env.PLAYWRIGHT_MODULE || 'C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright')

const root = path.resolve(__dirname, '..')
const url = process.argv[2]
if (!url) throw new Error('Provide the URL printed by the installed Minecraft Builder preview script')

async function main() {
  const browser = await chromium.launch({ channel: 'msedge', headless: true, args: ['--enable-webgl', '--use-angle=swiftshader', '--enable-unsafe-swiftshader'] })
  try {
    const page = await browser.newPage({ viewport: { width: 1600, height: 1100 }, deviceScaleFactor: 1 })
    const errors = []
    page.on('pageerror', error => errors.push(error.message))
    await page.route('**/src/main.js', async route => {
      const response = await route.fetch()
      let source = await response.text()
      source = source.replace('asyncBuild: true,', 'asyncBuild: true, preserveDrawingBuffer: true,')
      source = source.replace('await renderer.whenReady()', 'window.__s03 = { renderer, camera, structure, Structure, requestRender }; await renderer.whenReady()')
      await route.fulfill({ response, body: source })
    })
    await page.goto(url, { waitUntil: 'networkidle', timeout: 120000 })
    await page.waitForSelector('#loading.hidden', { state: 'attached', timeout: 120000 })
    await page.addStyleTag({ content: '#preview { position:fixed!important;inset:0!important;width:100vw!important;height:100vh!important; }' })
    const sourceInfo = await page.evaluate(() => ({ size: window.__s03.structure.getSize(), records: window.__s03.structure.getBlocks().length }))
    const views = [
      { name: 'interior', cut: 'none', position: [5.5, 2.62, 0.7], target: [5.5, 2.62, 15.5], fov: 72 },
      { name: 'north', cut: 'none', position: [5.5, 5.5, -15], target: [5.5, 4.2, 8], fov: 48 },
      { name: 'south', cut: 'none', position: [5.5, 5.5, 31], target: [5.5, 4.2, 8], fov: 48 },
      { name: 'west', cut: 'west-wall-and-roof', position: [-19, 14, 8], target: [5.5, 3.3, 8], fov: 43 },
      { name: 'east', cut: 'east-wall-and-roof', position: [30, 14, 8], target: [5.5, 3.3, 8], fov: 43 },
      { name: 'overview', cut: 'east-wall-and-roof', position: [24, 22, -20], target: [5.5, 3.0, 8], fov: 43 },
    ]
    await fs.mkdir(path.join(root, 'qa', 'views'), { recursive: true })
    for (const view of views) {
      await page.evaluate(async options => {
        const state = window.__s03
        let visible = state.structure
        if (options.cut !== 'none') {
          visible = new state.Structure([...state.structure.getSize()])
          for (const block of state.structure.getBlocks()) {
            const [blockX, height] = block.pos
            if (height === 9) continue
            if (height >= 1 && ((options.cut.startsWith('west') && blockX <= 1) || (options.cut.startsWith('east') && blockX >= 9))) continue
            visible.addBlock(block.pos, block.state.getName(), block.state.getProperties(), block.nbt)
          }
        }
        state.renderer.setStructure(visible)
        state.renderer.setFOV(options.fov)
        state.renderer.setViewport(0, 0, 1600, 1100, 1)
        state.renderer.setSunlight({ direction: [0.35, 0.85, 0.25], color: [0.88, 0.96, 1], ambientColor: [0.72, 0.85, 0.88], fillColor: [0.4, 0.7, 0.72], intensity: 0.8, ambientIntensity: 0.84, fillIntensity: 0.42, exposure: 1.13, fog: { density: 0 }, emissive: { intensity: 1.4, range: 16, tint: [0.58, 1, 1] }, sky: { zenithColor: [0.035, 0.07, 0.08], horizonColor: [0.09, 0.14, 0.15], groundColor: [0.025, 0.045, 0.04], sunGlowIntensity: 0, stars: { enabled: false } }, disc: { coreIntensity: 0, glowIntensity: 0 } })
        state.camera.getCamera = () => ({ position: options.position, target: options.target })
        await state.renderer.whenReady()
        state.requestRender()
        await new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)))
      }, view)
      await page.waitForTimeout(350)
      const data = await page.locator('#preview').evaluate(canvas => canvas.toDataURL('image/png').split(',')[1])
      await fs.writeFile(path.join(root, 'qa', 'views', `${view.name}.png`), Buffer.from(data, 'base64'))
      console.log(`Rendered ${view.name}: ${view.cut}`)
    }
    const nbtSha = crypto.createHash('sha256').update(await fs.readFile(path.join(root, 'room.nbt'))).digest('hex')
    await fs.writeFile(path.join(root, 'qa', 'preview-provenance.json'), JSON.stringify({ status: errors.length ? 'FAIL' : 'PASS', nbt_sha256: nbtSha, renderer: '@mattzh72/lodestone 0.6.0 ThreeStructureRenderer, installed Minecraft Builder viewer and bundled vanilla default pack', source: sourceInfo, view_specs: views, browser_page_errors: errors, nbt_modified_for_rendering: false, cutaway_policy: 'Only temporary renderer Structure copies omit named walls/roof. Main interior and north/south views use the full delivered NBT.', screenshot_type: 'Software WebGL structure rendering, not a Minecraft client screenshot', preview_url: url }, null, 2))
    if (errors.length) throw new Error(errors.join('\n'))
  } finally {
    await browser.close()
  }
}

main().catch(error => { console.error(error); process.exitCode = 1 })
