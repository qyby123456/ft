import fs from 'node:fs/promises'
import path from 'node:path'
import { createHash } from 'node:crypto'
import { createRequire } from 'node:module'
import { fileURLToPath } from 'node:url'
const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const require = createRequire('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/package.json')
const { chromium } = require('playwright')
const url = process.argv[2]
if (!url?.startsWith('http://127.0.0.1:')) throw new Error('Expected local Minecraft Builder URL')
const body = await fs.readFile(path.join(root, 'source/viewer-a03.js'), 'utf8')
const browser = await chromium.launch({ executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe', headless: true, args: ['--use-angle=swiftshader', '--enable-unsafe-swiftshader', '--disable-gpu-sandbox'] })
const errors = []
const images = []
const digest = bytes => createHash('sha256').update(bytes).digest('hex')
const nbtSha256 = digest(await fs.readFile(path.join(root, 'room.nbt')))
try {
  const page = await browser.newPage({ viewport: { width: 1536, height: 1024 }, deviceScaleFactor: 1 })
  page.on('pageerror', error => errors.push(String(error)))
  await page.route('**/src/main.js*', async route => {
    const response = await route.fetch()
    const source = await response.text()
    const marker = source.indexOf('const originalWarn')
    if (marker < 0) throw new Error('Canonical viewer imports not found')
    await route.fulfill({ response, body: source.slice(0, marker) + '\n' + body })
  })
  await page.goto(url, { waitUntil: 'domcontentloaded', timeout: 120000 })
  await page.waitForFunction(() => window.a03Ready, { timeout: 120000 })
  const directions = { north: [-18, 17, -22], south: [37, 17, 41], west: [-24, 17, 33], east: [43, 17, -14] }
  const names = { north: '北面', south: '南面', west: '西面', east: '东面' }
  const capture = async (file, options) => {
    const evidence = await page.evaluate(options => window.a03Preview.render(options), options)
    await page.screenshot({ path: path.join(root, file) })
    images.push({ path: file, sha256: digest(await fs.readFile(path.join(root, file))), ...evidence })
    console.log('RENDERED ' + file)
  }
  for (const mode of ['exterior', 'interior']) {
    for (const [side, position] of Object.entries(directions)) {
      await capture('views/' + mode + '-' + side + '.png', { mode: mode === 'interior' ? 'cutaway' : 'complete', position, target: [9.5, 3.5, 9.5], title: 'A03 荒废厨房 · ' + names[side] + (mode === 'interior' ? '内部剖视' : '完整外观'), subtitle: mode === 'interior' ? '仅显示时移除屋顶与两面近墙；保留灯链横梁；最终 NBT 完整' : '19 × 11 × 19 / 完整石屋顶 / 北侧 3×4 入口 / 冷炉灶与空容器' })
    }
  }
  await capture('preview.png', { mode: 'cutaway', position: [-18, 17, -22], target: [9.5, 3.5, 9.5], title: 'A03 荒废厨房', subtitle: '石砌冷炉灶 / 四脚木桌 / 落地柜架 / 有支撑吊灯与锅具 · 北侧安全入口' })
  await capture('views/entrance-eye.png', { mode: 'complete', position: [9.5, 2.65, 1.5], target: [9.5, 3.0, 16], fov: 78, title: 'A03 荒废厨房 · 入口视点', subtitle: '完整最终 NBT / 安全脚位 [9,1,4] / 三格宽入口与前场' })
  if (errors.length) throw new Error(errors.join('; '))
  const gpu = await page.evaluate(() => { const context = document.getElementById('view').getContext('webgl2'); const extension = context.getExtension('WEBGL_debug_renderer_info'); return extension ? context.getParameter(extension.UNMASKED_RENDERER_WEBGL) : 'WebGL2' })
  const manifest = { id: 'A03', status: 'RENDERED_PENDING_VISUAL_REVIEW', source_sha256: nbtSha256, source_unchanged: nbtSha256 === digest(await fs.readFile(path.join(root, 'room.nbt'))), renderer: 'Minecraft Builder / Lodestone 0.6.0', gpu, software_render: true, game_client_screenshot: false, viewport: [1536, 1024], cutaway_policy: 'Interior screenshots only hide y>=9, two camera-facing outer wall strips, and capitals of removed near-wall piers; y=8 lamp beams remain visible. Final room.nbt retains the complete roof.', images, errors }
  await fs.writeFile(path.join(root, 'views/manifest.json'), JSON.stringify(manifest, null, 2) + '\n')
} finally { await browser.close() }

