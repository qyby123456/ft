import fs from 'node:fs/promises'
import path from 'node:path'
import crypto from 'node:crypto'
import { spawn } from 'node:child_process'
import { createRequire } from 'node:module'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const skill = 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
const require = createRequire(import.meta.url)
const { chromium } = require('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright')
const hash = crypto.createHash('sha256').update(await fs.readFile(path.join(root, 'room.nbt'))).digest('hex')
await fs.mkdir(path.join(root, 'preview'), { recursive: true })
const server = spawn(process.execPath, [path.join(skill, 'scripts/preview.mjs'), '--nbt', path.join(root, 'room.nbt'), '--no-open', '--port', '0'], { cwd: skill, windowsHide: true, stdio: ['ignore', 'pipe', 'pipe'] })
let browser
let serverLog = ''
try {
  const url = await new Promise((resolve, reject) => {
    const timeout = setTimeout(() => reject(new Error('Preview server did not start')), 60000)
    server.on('error', reject)
    server.on('exit', code => reject(new Error(`Preview server exited ${code}`)))
    server.stderr.on('data', chunk => { serverLog += chunk.toString() })
    server.stdout.on('data', chunk => {
      serverLog += chunk.toString()
      const match = serverLog.match(/Minecraft build preview: (http:\/\/[^\s]+)/)
      if (match) { clearTimeout(timeout); resolve(match[1]) }
    })
  })
  browser = await chromium.launch({ headless: true, executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe', args: ['--disable-gpu-sandbox', '--use-angle=swiftshader'] })
  const page = await browser.newPage({ viewport: { width: 1536, height: 1024 }, deviceScaleFactor: 1 })
  const pageErrors = []
  page.on('pageerror', error => pageErrors.push(String(error)))
  await page.route('**/src/main.js', async route => {
    const response = await route.fetch()
    let body = await response.text()
    body = body.replace("  loading.classList.add('hidden')", "  window.review = { structure, renderer, pack, Structure, presets: LIGHTING_PRESETS };\n  loading.classList.add('hidden')")
    await route.fulfill({ response, body })
  })
  await page.goto(url, { waitUntil: 'networkidle', timeout: 120000 })
  await page.waitForFunction(() => window.review, { timeout: 120000 })
  await page.addStyleTag({ content: `body * {visibility:hidden!important} canvas,#review-overlay,#review-overlay * {visibility:visible!important} #preview {position:fixed!important;inset:0!important;width:100vw!important;height:100vh!important} #review-overlay {position:fixed;inset:0;pointer-events:none;z-index:9999;font-family:'Microsoft YaHei',sans-serif;color:#edf1f3} .review-title{position:absolute;top:24px;left:24px;background:#111820df;padding:14px 20px;border-left:4px solid #c0a674;border-radius:4px;font-size:26px} .review-sub{display:block;font-size:16px;color:#b7c4cc;margin-top:5px} .review-foot{position:absolute;bottom:20px;left:24px;background:#111820e8;padding:10px 16px;border-radius:4px;font-size:16px;line-height:1.7}` })
  await page.evaluate(() => {
    const overlay = document.createElement('div')
    overlay.id = 'review-overlay'
    overlay.innerHTML = '<div class="review-title" id="review-title"></div><div class="review-foot" id="review-foot"></div>'
    document.body.append(overlay)
  })
  const views = [
    { direction: 'north', chinese: '北向', camera: [29, 29, -39], hideX: 'east', hideZ: 'north' },
    { direction: 'south', chinese: '南向', camera: [-16, 29, 79], hideX: 'west', hideZ: 'south' },
    { direction: 'west', chinese: '西向', camera: [-43, 28, -8], hideX: 'west', hideZ: 'north' },
    { direction: 'east', chinese: '东向', camera: [56, 28, 48], hideX: 'east', hideZ: 'south' }
  ]
  const frames = []
  async function capture(configuration) {
    const detail = await page.evaluate(async ({ configuration, hash }) => {
      const { structure, renderer, Structure, presets } = window.review
      const visible = new Structure(structure.getSize())
      let removed = 0
      for (const block of structure.getBlocks()) {
        const [blockX, blockY, blockZ] = block.pos
        let omit = false
        if (configuration.mode === 'cutaway') {
          const roofAnchorStrip = [0, 7, 10, 17, 20, 27, 30, 36, 39].includes(blockZ)
          omit = blockY === 10 && !roofAnchorStrip
          omit ||= blockY >= 1 && (configuration.hideX === 'west' ? blockX <= 2 : blockX >= 10)
          omit ||= blockY >= 1 && blockZ === (configuration.hideZ === 'north' ? 0 : 39)
        }
        if (omit) { if (!block.state.is('minecraft:air')) removed++; continue }
        visible.addBlock(block.pos, block.state.getName(), block.state.getProperties(), block.nbt)
      }
      renderer.setStructure(visible)
      await renderer.whenReady()
      const hero = configuration.mode === 'hero'
      renderer.setSunlight({ ...presets.night,
        intensity: hero ? 0.15 : 0.45, ambientIntensity: hero ? 0.57 : 0.73, fillIntensity: 0.18, rimIntensity: 0.12, exposure: hero ? 1.15 : 1.08,
        ambientColor: hero ? [0.72, 0.67, 0.57] : [0.83, 0.87, 0.91], fillColor: [0.35, 0.3, 0.25],
        sky: { zenithColor: [0.008, 0.009, 0.012], horizonColor: [0.012, 0.013, 0.016], groundColor: [0.01, 0.01, 0.012], stars: { enabled: false } },
        fog: { color: [0.02, 0.02, 0.025], density: 0.00003, heightFalloff: 0.001 },
        emissive: { range: 14, intensity: 1.8, tint: [1, 0.75, 0.42] }
      })
      renderer.setViewport(0, 0, 1536, 1024, 1)
      renderer.setFOV(hero ? 74 : 32)
      renderer.lookAt(configuration.camera, hero ? [6.5, 2.62, 39] : [6.5, 3.6, 20])
      renderer.drawStructure()
      document.getElementById('review-title').innerHTML = configuration.title + '<span class="review-sub">corridor-M12 · 13 × 11 × 40 · 8 吊笼 / 8 烛龛 · 五格宽平直主路</span>'
      document.getElementById('review-foot').innerHTML = (configuration.mode === 'cutaway' ? '检查剖切：仅临时隐藏近侧墙及部分顶盖；保留吊点顶盖条带。最终 NBT 完整。' : '完整 NBT；未隐藏外壳。') + '<br>实际 NBT / Lodestone 软件渲染，非游戏截图 · SHA-256 ' + hash.slice(0, 16)
      await new Promise(resolve => requestAnimationFrame(() => { renderer.drawStructure(); resolve() }))
      return { removed_non_air: removed, displayed_records: visible.getBlocks().length }
    }, { configuration, hash })
    await page.waitForTimeout(400)
    const filename = configuration.mode === 'hero' ? 'preview/hero.png' : `preview/${configuration.mode}-${configuration.direction}.png`
    await page.screenshot({ path: path.join(root, filename), type: 'png' })
    frames.push({ file: filename, mode: configuration.mode, direction: configuration.direction, camera: configuration.camera, target: configuration.mode === 'hero' ? [6.5, 2.62, 39] : [6.5, 3.6, 20], ...detail })
    console.log(`RENDERED ${filename}`)
  }
  await capture({ mode: 'hero', direction: 'north', camera: [6.5, 2.62, -2.4], title: 'M12 陈旧石砖吊笼烛廊 · 一字贯通' })
  await fs.copyFile(path.join(root, 'preview/hero.png'), path.join(root, 'preview.png'))
  for (const view of views) {
    await capture({ ...view, mode: 'cutaway', title: `M12 ${view.chinese}内部剖视` })
    await capture({ ...view, mode: 'exterior', title: `M12 ${view.chinese}完整外壳` })
  }
  if (pageErrors.length) throw new Error(pageErrors.join('\n'))
  const report = { status: 'RENDERED', visual_review_report: 'qa/four-view-review.json', sha256: hash, renderer: 'Lodestone 0.6.0 ThreeStructureRenderer / Edge WebGL SwiftShader', source: 'room.nbt', screenshots_are_gameplay: false, reference_image_used_as_preview: false, frames, page_errors: pageErrors }
  await fs.writeFile(path.join(root, 'qa/render.json'), JSON.stringify(report, null, 2) + '\n')
  await fs.writeFile(path.join(root, 'qa/preview-server.log'), serverLog)
} finally {
  await browser?.close()
  server.kill()
}
