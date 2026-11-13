const fs = require('node:fs/promises')
const path = require('node:path')
const { chromium } = require('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright/index.js')

async function main() {
  const root = path.resolve(__dirname, '..')
  const url = process.argv[2]
  if (!url || !url.startsWith('http://127.0.0.1:')) throw new Error('Expected the local Minecraft Builder preview URL')
  const body = await fs.readFile(path.join(__dirname, 'viewer-m04.js'), 'utf8')
  const browser = await chromium.launch({
    executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe',
    headless: true, args: ['--use-angle=swiftshader', '--enable-unsafe-swiftshader', '--disable-gpu-sandbox'],
  })
  const page = await browser.newPage({ viewport: { width: 1600, height: 1000 }, deviceScaleFactor: 1 })
  const errors = []
  page.on('pageerror', error => errors.push(String(error)))
  await page.route('**/src/main.js*', async route => {
    const original = await route.fetch()
    const source = await original.text()
    const marker = source.indexOf('const originalWarn')
    if (marker < 0) throw new Error('Cannot locate canonical viewer import boundary')
    await route.fulfill({ response: original, body: source.slice(0, marker) + '\n' + body })
  })
  try {
    await page.goto(url, { waitUntil: 'networkidle', timeout: 120000 })
    await page.waitForFunction(() => window.m04Ready === true, { timeout: 120000 })
    await fs.mkdir(path.join(root, 'views'), { recursive: true })
    const descriptions = []
    const directions = {
      north: { name: '北面', position: [-25, 27, -32] },
      south: { name: '南面', position: [38, 27, 73] },
      west: { name: '西面', position: [-48, 28, 5] },
      east: { name: '东面', position: [61, 28, 36] },
    }
    for (const mode of ['exterior', 'interior']) {
      for (const [direction, options] of Object.entries(directions)) {
        const file = `views/${mode}-${direction}.png`
        const data = await page.evaluate(options => window.m04Preview.render(options), {
          mode: mode === 'interior' ? 'cutaway' : 'complete', position: options.position,
          target: [6.5, 5, 20.5], title: `M04 末地淹水吊笼长廊 · ${mode === 'interior' ? '内部剖切' : '完整外观'} · ${options.name}`,
          subtitle: mode === 'interior' ? '仅显示时移除屋面及近侧墙；最终 NBT 保留完整屋顶与两端开口' : '13 × 12 × 41 方块 / 2.56 区块长 / 仅南北两个 3×4 端口',
        })
        await page.screenshot({ path: path.join(root, file) })
        descriptions.push({ file, ...data })
      }
    }
    for (const reverse of [false, true]) {
      const file = reverse ? 'views/axial-south.png' : 'views/axial-north.png'
      const data = await page.evaluate(options => window.m04Preview.render(options), {
        mode: 'complete', position: [6.5, 4.2, reverse ? 38.5 : 2.5],
        target: [6.5, 4.2, reverse ? 0 : 41], fov: 76,
        title: `M04 末地淹水吊笼长廊 · ${reverse ? '南端向北' : '北端向南'}`,
        subtitle: '完整 NBT 轴向视图 / 3 格宽干燥直线主路 / 两侧 1 格深封闭浅水 / 8 组空吊笼',
      })
      await page.screenshot({ path: path.join(root, file) })
      if (!reverse) await page.screenshot({ path: path.join(root, 'preview.png') })
      descriptions.push({ file, ...data })
    }
    if (errors.length) throw new Error(errors.join('\n'))
    const gpu = await page.evaluate(() => {
      const context = document.getElementById('view').getContext('webgl2')
      const extension = context.getExtension('WEBGL_debug_renderer_info')
      return extension ? context.getParameter(extension.UNMASKED_RENDERER_WEBGL) : 'WebGL2'
    })
    const report = { status: 'PASS', renderer: 'Minecraft Builder / Lodestone 0.6.0', gpu, viewport: [1600, 1000], images: descriptions, browser_errors: errors, claims: 'Software WebGL images from final room.nbt. Four complete-shell and four display-only cutaway views plus two complete axial interiors. Not Minecraft client screenshots.' }
    await fs.writeFile(path.join(root, 'qa/render-qa.json'), JSON.stringify(report, null, 2) + '\n')
    console.log(JSON.stringify({ status: 'PASS', captures: descriptions.length, gpu, structure_sha256: descriptions[0].source_sha256 }))
  } finally {
    await browser.close()
  }
}
main().catch(error => { console.error(error); process.exitCode = 1 })
