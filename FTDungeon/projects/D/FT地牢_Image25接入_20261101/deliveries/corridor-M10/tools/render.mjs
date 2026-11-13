import fs from 'node:fs/promises'
import path from 'node:path'
import { createRequire } from 'node:module'
import { createHash } from 'node:crypto'
import { fileURLToPath } from 'node:url'

const require = createRequire('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/package.json')
const { chromium } = require('playwright')
const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const base = process.argv[2] ?? 'http://127.0.0.1:41770/'
const browser = await chromium.launch({ headless: true, executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe', args: ['--use-gl=angle', '--use-angle=swiftshader', '--enable-unsafe-swiftshader'] })
const page = await browser.newPage({ viewport: { width: 1600, height: 1050 }, deviceScaleFactor: 1 })
const errors = []
page.on('pageerror', error => errors.push(error.message))
try {
  await page.goto(base, { waitUntil: 'networkidle', timeout: 180000 })
  await page.waitForFunction(() => document.querySelector('#loading.hidden'), { timeout: 180000 })
  await page.evaluate(async () => {
    const moduleUrl = performance.getEntriesByType('resource').map(entry => entry.name).find(name => name.includes('@mattzh72_lodestone.js'))
    const lib = await import(moduleUrl)
    const bytes = new Uint8Array(await (await fetch('/__minecraft_builder__/build.nbt')).arrayBuffer())
    const full = lib.Structure.fromNbt(lib.NbtFile.read(bytes).root)
    const pack = await lib.loadDefaultPackResources({ baseUrl: new URL('/__minecraft_builder__/default-pack/', location.href).toString() })
    document.body.innerHTML = '<canvas id="review-canvas"></canvas><div id="review-title"></div><div id="review-footer"></div>'
    const style = document.createElement('style')
    style.textContent = 'html,body{margin:0;width:100%;height:100%;overflow:hidden;background:#171b21}#review-canvas{display:block;width:100%;height:100%}#review-title{position:absolute;top:22px;left:28px;color:#ffe1a5;background:#171b21dd;padding:13px 20px;border:1px solid #7c674a;font:600 26px "Microsoft YaHei",sans-serif;border-radius:6px}#review-footer{position:absolute;bottom:22px;left:28px;color:#d6d2c9;background:#171b21e8;padding:12px 20px;font:17px "Microsoft YaHei",sans-serif;border-radius:6px}'
    document.head.appendChild(style)
    const canvas = document.getElementById('review-canvas')
    const renderer = new lib.ThreeStructureRenderer(canvas, full, pack.resources, {
      chunkSize: 16, drawDistance: 1000, useInvisibleBlockBuffer: false, asyncBuild: true,
      preserveDrawingBuffer: true, antialias: true,
      sunlight: { direction: [0.4, 0.8, -0.4], intensity: 0.55, ambientIntensity: 0.55, fillIntensity: 0.25,
        exposure: 1.02, sky: { zenithColor: [0.07, 0.085, 0.11], horizonColor: [0.14, 0.16, 0.19], groundColor: [0.07, 0.08, 0.09], stars: { enabled: false } },
        shadow: { enabled: false }, fog: { density: 0 }, emissive: { range: 10, intensity: 0.8 } }
    })
    renderer.setViewport(0, 0, 1600, 1050, 1)
    await renderer.whenReady()
    window.review = { lib, full, renderer }
  })
  const views = [
    { file: 'preview.png', kind: 'interior', side: 'north', title: 'M10 · 焚毁废弃矿道 / 北入口步行视角', position: [7.5, 3.62, 1.35], target: [7.5, 3.62, 38], fov: 69 },
    { file: 'views/interior-north.png', kind: 'cutaway', side: 'north', title: 'M10 · 北向内部剖视 / 40 格直廊', position: [7.5, 23, -26], target: [7.5, 3.7, 19], fov: 48 },
    { file: 'views/interior-south.png', kind: 'cutaway', side: 'south', title: 'M10 · 南向内部剖视 / 两端贯通', position: [7.5, 23, 66], target: [7.5, 3.7, 21], fov: 48 },
    { file: 'views/interior-west.png', kind: 'cutaway', side: 'west', title: 'M10 · 西向内部剖视 / 梁柱与吊笼', position: [-38, 29, 20], target: [7.5, 4, 20], fov: 51 },
    { file: 'views/interior-east.png', kind: 'cutaway', side: 'east', title: 'M10 · 东向内部剖视 / 轨道与安全通路', position: [53, 29, 20], target: [7.5, 4, 20], fov: 51 },
    { file: 'views/exterior-north.png', kind: 'exterior', side: 'north', title: 'M10 · 北向完整外壳 / 未剖切最终 NBT', position: [25, 35, -40], target: [7.5, 5, 19], fov: 43 },
    { file: 'views/exterior-south.png', kind: 'exterior', side: 'south', title: 'M10 · 南向完整外壳 / 完整双层顶盖', position: [-10, 35, 80], target: [7.5, 5, 21], fov: 43 },
    { file: 'views/exterior-west.png', kind: 'exterior', side: 'west', title: 'M10 · 西向完整外壳 / 无侧支路', position: [-52, 42, 20], target: [7.5, 5, 20], fov: 48 },
    { file: 'views/exterior-east.png', kind: 'exterior', side: 'east', title: 'M10 · 东向完整外壳 / 连续石质围护', position: [67, 42, 20], target: [7.5, 5, 20], fov: 48 },
    { file: 'views/interior-south-eye.png', kind: 'interior', side: 'south', title: 'M10 · 南入口回望 / 反向可步行', position: [7.5, 3.62, 38.65], target: [7.5, 3.62, 1], fov: 69 }
  ]
  for (const view of views) {
    await page.evaluate(async view => {
      const { lib, full, renderer } = window.review
      let displayed = full
      if (view.kind === 'cutaway') {
        displayed = new lib.Structure([...full.getSize()])
        for (const block of full.getBlocks()) {
          const [posX, posY, posZ] = block.pos
          if (posY >= 9) continue
          if (posY >= 2 && (view.side === 'north' && posZ === 0 || view.side === 'south' && posZ === 39 || view.side === 'west' && posX <= 1 || view.side === 'east' && posX >= 13)) continue
          displayed.addBlock(block.pos, block.state.getName(), block.state.getProperties(), block.nbt)
        }
      }
      renderer.setStructure(displayed)
      await renderer.whenReady()
      renderer.setCamera({ position: view.position, target: view.target, fov: view.fov })
      document.getElementById('review-title').textContent = view.title
      document.getElementById('review-footer').textContent = '15 × 11 × 40 · Java 1.20.1 · 实际 NBT / Lodestone 软件渲染 · 非游戏截图' + (view.kind === 'cutaway' ? ' · 剖切仅用于预览，不修改最终结构' : '')
      renderer.drawStructure()
    }, view)
    await page.waitForTimeout(350)
    await fs.mkdir(path.dirname(path.join(root, view.file)), { recursive: true })
    await page.screenshot({ path: path.join(root, view.file) })
    process.stdout.write(view.file + '\n')
  }
  if (errors.length) throw new Error(errors.join('\n'))
  const hash = createHash('sha256').update(await fs.readFile(path.join(root, 'room.nbt'))).digest('hex')
  await fs.writeFile(path.join(root, 'views/manifest.json'), JSON.stringify({ status: 'RENDERED_PENDING_VISUAL_REVIEW', sha256: hash, renderer: 'Lodestone 0.6.0 ThreeStructureRenderer', source: 'final room.nbt', production_save_used: false, views, browser_errors: errors }, null, 2))
} finally {
  await browser.close()
}
