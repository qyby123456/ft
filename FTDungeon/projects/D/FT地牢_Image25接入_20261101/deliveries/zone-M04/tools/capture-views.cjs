const fs = require('node:fs/promises')
const path = require('node:path')
const { chromium } = require('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright/index.js')

async function main() {
  const root = path.resolve(__dirname, '..')
  const url = process.argv[2]
  if (!url || !url.startsWith('http://127.0.0.1:')) throw new Error('Expected local Minecraft Builder preview URL')
  const body = await fs.readFile(path.join(__dirname, 'viewer-zone.js'), 'utf8')
  const browser = await chromium.launch({ executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe', headless: true, args: ['--use-angle=swiftshader', '--enable-unsafe-swiftshader', '--disable-gpu-sandbox'] })
  const page = await browser.newPage({ viewport: { width: 1600, height: 1100 }, deviceScaleFactor: 1 })
  const errors = []
  page.on('pageerror', error => errors.push(String(error)))
  await page.route('**/src/main.js*', async route => {
    const original = await route.fetch()
    const source = await original.text()
    const marker = source.indexOf('const originalWarn')
    if (marker < 0) throw new Error('Cannot locate canonical viewer imports')
    await route.fulfill({ response: original, body: source.slice(0, marker) + '\n' + body })
  })
  try {
    await page.goto(url, { waitUntil: 'networkidle', timeout: 120000 })
    await page.waitForFunction(() => window.zoneReady === true, { timeout: 120000 })
    await fs.mkdir(path.join(root, 'views'), { recursive: true })
    const descriptions = []
    const directions = {
      north: { name: '北侧', position: [-24, 32, -29] },
      south: { name: '南侧', position: [49, 32, 54] },
      west: { name: '西侧', position: [-29, 32, 49] },
      east: { name: '东侧', position: [54, 32, -24] },
    }
    for (const mode of ['exterior', 'interior']) {
      for (const [direction, options] of Object.entries(directions)) {
        const file = `views/${mode}-${direction}.png`
        const data = await page.evaluate(options => window.zonePreview.render(options), {
          mode: mode === 'interior' ? 'cutaway' : 'complete', position: options.position, target: [12.5, 5.5, 12.5],
          title: `zone-M04 熔岩矿脉 · ${mode === 'interior' ? '内部剖切' : '完整外观'} · ${options.name}`,
          subtitle: mode === 'interior' ? '六面封闭熔岩槽 / 三格宽石桥 / 四根落地支架 / 剖切只影响显示' : '25 × 14 × 25 方块 / 完整顶盖 / 北入口 3×4 / 独立分区房间',
        })
        await page.screenshot({ path: path.join(root, file) })
        if (mode === 'interior' && direction === 'north') await page.screenshot({ path: path.join(root, 'preview.png') })
        descriptions.push({ file, ...data })
      }
    }
    for (const reverse of [false, true]) {
      const file = reverse ? 'views/bridge-south.png' : 'views/entrance-north.png'
      const data = await page.evaluate(options => window.zonePreview.render(options), {
        mode: 'complete', position: reverse ? [12.5, 5.0, 22] : [12.5, 5.0, 2.0], target: reverse ? [12.5, 4.7, 0] : [12.5, 4.7, 23], fov: 80,
        title: `zone-M04 熔岩矿脉 · ${reverse ? '桥后向入口' : '入口与安全落点'}`,
        subtitle: '完整最终 NBT 内部观察 / 实体全封闭熔岩与隔离栏 / 3 格通路与 5×5 安全落点',
      })
      await page.screenshot({ path: path.join(root, file) })
      descriptions.push({ file, ...data })
    }
    if (errors.length) throw new Error(errors.join('\n'))
    const gpu = await page.evaluate(() => {
      const context = document.getElementById('view').getContext('webgl2')
      const extension = context.getExtension('WEBGL_debug_renderer_info')
      return extension ? context.getParameter(extension.UNMASKED_RENDERER_WEBGL) : 'WebGL2'
    })
    await fs.writeFile(path.join(root, 'qa/render.json'), JSON.stringify({ status: 'PASS', id: 'zone-M04', structure_sha256: descriptions[0].source_sha256, renderer: 'Minecraft Builder / Lodestone 0.6.0', gpu, viewport: [1600, 1100], images: descriptions, browser_errors: errors, scope: 'Four complete exteriors, four display-only cutaways and two complete-NBT interior views. Software renders, not game screenshots.' }, null, 2) + '\n')
    console.log(JSON.stringify({ status: 'PASS', captures: descriptions.length, gpu, structure_sha256: descriptions[0].source_sha256 }))
  } finally { await browser.close() }
}
main().catch(error => { console.error(error); process.exitCode = 1 })
