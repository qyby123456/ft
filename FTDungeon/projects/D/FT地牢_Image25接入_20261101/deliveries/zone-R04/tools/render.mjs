import fs from 'node:fs/promises'
import path from 'node:path'
import { createRequire } from 'node:module'
import { createHash } from 'node:crypto'
import { fileURLToPath } from 'node:url'

const require = createRequire('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/package.json')
const { chromium } = require('playwright')
const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const base = process.argv[2] ?? 'http://127.0.0.1:41904/'
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
    { file: 'preview.png', kind: 'cutaway', side: 'northwest', title: 'R04 · 焦毁营房 / 双层床与落地断梁', position: [-20, 25, -22], target: [12.5, 4, 12.5], fov: 35 },
    { file: 'views/interior-north.png', kind: 'cutaway', side: 'north', title: 'R04 · 北向内部 / 三格中央安全路', position: [12.5, 27, -35], target: [12.5, 4, 12.5], fov: 43 },
    { file: 'views/interior-south.png', kind: 'cutaway', side: 'south', title: 'R04 · 南向内部 / 床架与残骸', position: [12.5, 27, 60], target: [12.5, 4, 12.5], fov: 43 },
    { file: 'views/interior-west.png', kind: 'cutaway', side: 'west', title: 'R04 · 西向内部 / 承重柱与灰堆', position: [-35, 27, 12.5], target: [12.5, 4, 12.5], fov: 43 },
    { file: 'views/interior-east.png', kind: 'cutaway', side: 'east', title: 'R04 · 东向内部 / 落地断梁与焦旗', position: [60, 27, 12.5], target: [12.5, 4, 12.5], fov: 43 },
    { file: 'views/exterior-north.png', kind: 'exterior', side: 'north', title: 'R04 · 北向完整外壳 / 入口与封闭残顶', position: [-19, 30, -35], target: [12.5, 5, 12.5], fov: 46 },
    { file: 'views/exterior-south.png', kind: 'exterior', side: 'south', title: 'R04 · 南向完整外壳 / 出口与冷色天窗', position: [44, 30, 60], target: [12.5, 5, 12.5], fov: 46 },
    { file: 'views/exterior-west.png', kind: 'exterior', side: 'west', title: 'R04 · 西向完整外壳 / 连续围墙', position: [-35, 30, 44], target: [12.5, 5, 12.5], fov: 46 },
    { file: 'views/exterior-east.png', kind: 'exterior', side: 'east', title: 'R04 · 东向完整外壳 / 残垛与屋面', position: [60, 30, -19], target: [12.5, 5, 12.5], fov: 46 },
    { file: 'views/entrance-eye.png', kind: 'interior', side: 'north', title: 'R04 · 北入口步行视角 / 安全点在前方', position: [12.5, 3.62, 0.5], target: [12.5, 3.62, 23], fov: 76 },
    { file: 'views/safe-point-eye.png', kind: 'interior', side: 'north', title: 'R04 · 内部安全点 / 两侧床架与灯链', position: [12.5, 3.62, 5.5], target: [12.5, 4, 20], fov: 88 }
  ]
  for (const view of views) {
    await page.evaluate(async view => {
      const { lib, full, renderer } = window.review
      let displayed = full
      if (view.kind === 'cutaway') {
        displayed = new lib.Structure([...full.getSize()])
        for (const block of full.getBlocks()) {
          const [posX, posY, posZ] = block.pos
          if (posY >= 11) continue
          if (posY >= 2 && (view.side === 'north' && posZ === 0 || view.side === 'south' && posZ === 24 || view.side === 'west' && posX === 0 || view.side === 'east' && posX === 24 || view.side === 'northwest' && (posX === 0 || posZ === 0))) continue
          displayed.addBlock(block.pos, block.state.getName(), block.state.getProperties(), block.nbt)
        }
      }
      renderer.setStructure(displayed)
      await renderer.whenReady()
      renderer.setCamera({ position: view.position, target: view.target, fov: view.fov })
      document.getElementById('review-title').textContent = view.title
      document.getElementById('review-footer').textContent = '25 × 13 × 25 · Java 1.20.1 · 实际 NBT / Lodestone 软件渲染 · 非游戏截图' + (view.kind === 'cutaway' ? ' · 剖切仅用于预览，不修改最终结构' : '')
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
