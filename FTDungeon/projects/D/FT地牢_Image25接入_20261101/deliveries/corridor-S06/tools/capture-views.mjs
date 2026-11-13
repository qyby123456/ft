import fs from 'node:fs/promises'
import path from 'node:path'
import crypto from 'node:crypto'
import { fileURLToPath } from 'node:url'
import { chromium } from 'file:///C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright/index.mjs'
import sharp from 'file:///C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/sharp/dist/index.cjs'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const url = process.argv[2] ?? 'http://127.0.0.1:5179/'
const metadata = JSON.parse(await fs.readFile(path.join(root, 'room.json'), 'utf8'))
const served = Buffer.from(await (await fetch(new URL('/__minecraft_builder__/build.nbt', url))).arrayBuffer())
const servedHash = crypto.createHash('sha256').update(served).digest('hex')
if (servedHash !== metadata.sha256) throw new Error('Preview server is not serving the final room.nbt')
const source = await (await fetch(new URL('/src/main.js', url))).text()
const modulePath = source.match(/from\s*["']([^"']*lodestone[^"']*)["']/)?.[1]
if (!modulePath) throw new Error('Could not locate the bundled Lodestone browser module')
const browser = await chromium.launch({ executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe', headless: true, args: ['--disable-gpu-sandbox', '--enable-unsafe-swiftshader'] })
const page = await browser.newPage({ viewport: { width: 1536, height: 1024 }, deviceScaleFactor: 1 })
const errors = []
page.on('pageerror', error => errors.push(String(error)))
await fs.mkdir(path.join(root, 'views'), { recursive: true })
try {
  await page.goto(url, { waitUntil: 'domcontentloaded', timeout: 180000 })
  await page.waitForSelector('#loading.hidden', { state: 'attached', timeout: 180000 })
  await page.evaluate(async ({ moduleUrl, fingerprint }) => {
    const { NbtFile, Structure, ThreeStructureRenderer, loadDefaultPackResources } = await import(moduleUrl)
    const bytes = new Uint8Array(await (await fetch('/__minecraft_builder__/build.nbt')).arrayBuffer())
    const structure = Structure.fromNbt(NbtFile.read(bytes).root)
    const pack = await loadDefaultPackResources({ baseUrl: new URL('/__minecraft_builder__/default-pack/', location.href).toString() })
    document.head.innerHTML = '<meta charset="utf-8"><title>S06 浅淹苔石短拱廊 · NBT 核验预览</title><style>*{box-sizing:border-box}html,body{margin:0;width:100%;height:100%;overflow:hidden;background:#10191e;color:#d6e7e4;font-family:Segoe UI,Microsoft YaHei,sans-serif}header{height:80px;padding:15px 28px;border-bottom:1px solid #38504f;display:flex;justify-content:space-between;align-items:center}h1{font-size:24px;letter-spacing:1px;margin:0 0 4px}p{font-size:13px;margin:0;color:#9cbab5}header aside{text-align:right;color:#7fcac3;font-size:14px;line-height:22px}canvas{display:block;width:100%;height:904px}footer{height:40px;padding:10px 28px;background:#10191e;color:#839d99;font-size:12px}</style>'
    document.body.innerHTML = '<header><div><h1 id="view-title"></h1><p id="view-description"></p></div><aside>S06 · 浅淹苔石短拱廊<br>13 × 10 × 16 · Java 1.20.1</aside></header><canvas id="review-canvas"></canvas><footer>Actual final room.nbt · Lodestone 0.6.0 · SHA-256 ' + fingerprint + ' · 软件渲染，非整合包实机截图</footer>'
    const canvas = document.getElementById('review-canvas')
    let renderer
    const cameraPositions = {
      north: [20, 17, -20], south: [-7, 17, 36],
      west: [-23, 17, -5], east: [36, 17, 21],
    }
    window.renderReview = async (mode, direction) => {
      if (renderer) renderer.dispose()
      const keep = position => {
        if (mode !== 'interior' && mode !== 'plan') return true
        const [posX, posY, posZ] = position
        if (posY >= 8) return false
        if (mode === 'plan' || posY < 2) return true
        if (direction === 'north') return posZ >= 3
        if (direction === 'south') return posZ <= 12
        if (direction === 'west') return posX >= 4
        return posX <= 8
      }
      const visible = structure.getBlocks().filter(block => keep(block.pos))
      const provider = { getSize: () => structure.getSize(), getBlocks: () => visible, getBlock: position => keep(position) ? structure.getBlock(position) : null }
      const inside = mode === 'hero'
      renderer = new ThreeStructureRenderer(canvas, provider, pack.resources, {
        preserveDrawingBuffer: true, antialias: true, chunkSize: 16,
        sunlight: {
          direction: [-0.35, 0.8, -0.5], color: [0.81, 0.9, 0.94],
          ambientColor: [0.58, 0.69, 0.7], fillColor: [0.52, 0.68, 0.72], rimColor: [0.75, 0.93, 0.94],
          intensity: inside ? 0.16 : 1.05, ambientIntensity: inside ? 0.48 : 0.72,
          fillIntensity: 0.25, rimIntensity: 0.2, exposure: inside ? 1.1 : 1.1,
          emissive: { range: 9, intensity: 0.75, tint: inside ? [0.7, 0.92, 1] : [1, 1, 1] },
          shadow: { enabled: !inside, intensity: 0.35 },
          sky: { zenithColor: [0.045, 0.085, 0.1], horizonColor: [0.1, 0.16, 0.18], groundColor: [0.045, 0.07, 0.08], sunGlowIntensity: 0 },
          fog: { density: 0, color: [0.1, 0.16, 0.18] },
        },
      })
      renderer.setViewport(0, 0, 1536, 904, 1)
      const position = inside ? [6.5, 4.15, 0.85] : mode === 'plan' ? [6.5, 41, 8.01] : cameraPositions[direction]
      const target = inside ? [6.5, 4.15, 14] : [6.5, 3.5, 8]
      renderer.setCamera({ position, target, fov: inside ? 72 : 35 })
      await renderer.whenReady()
      renderer.drawStructure()
      await new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)))
      renderer.drawStructure()
      const labels = { north: '北向', south: '南向', west: '西向', east: '东向' }
      document.getElementById('view-title').textContent = inside ? 'S06 / 浅淹苔石短拱廊' : mode === 'plan' ? '俯视 / 水体与干燥通路' : labels[direction] + ' / ' + (mode === 'exterior' ? '完整外壳' : '内部剖视')
      document.getElementById('view-description').textContent = inside ? '五道完整石拱 · 八盏灵魂灯 · 双侧密封浅水 · 三格宽无台阶干路' : mode === 'exterior' ? '完整结构：双层屋顶、连续外墙、南北贯通拱口；无隐藏方块' : '仅为检查临时隐藏遮挡面，最终 NBT 保留完整屋顶与围护'
      return { mode, direction, rendered_records: visible.length, camera: { position, target }, hidden_for_review_only: mode === 'interior' || mode === 'plan' }
    }
  }, { moduleUrl: new URL(modulePath, url).toString(), fingerprint: metadata.sha256 })
  const evidence = []
  for (const [mode, direction, output] of [
    ['hero', 'north', 'preview.png'],
    ...['north', 'south', 'west', 'east'].flatMap(direction => [['interior', direction, `views/${direction}-interior.png`], ['exterior', direction, `views/${direction}-exterior.png`]]),
    ['plan', 'north', 'views/plan.png'],
  ]) {
    const result = await page.evaluate(([mode, direction]) => window.renderReview(mode, direction), [mode, direction])
    await page.screenshot({ path: path.join(root, output), timeout: 90000 })
    evidence.push({ ...result, path: output, sha256: crypto.createHash('sha256').update(await fs.readFile(path.join(root, output))).digest('hex') })
    process.stdout.write(`Rendered ${output}\n`)
  }
  const composites = []
  const directions = ['north', 'south', 'west', 'east']
  for (let index = 0; index < directions.length; index++) {
    for (const [column, mode] of ['interior', 'exterior'].entries()) {
      composites.push({ input: await sharp(path.join(root, 'views', `${directions[index]}-${mode}.png`)).resize(960, 640).toBuffer(), left: column * 960, top: index * 640 })
    }
  }
  await sharp({ create: { width: 1920, height: 2560, channels: 3, background: '#10191e' } }).composite(composites).png().toFile(path.join(root, 'views', 'four-direction-review.png'))
  await fs.writeFile(path.join(root, 'qa', 'render.json'), `${JSON.stringify({ status: errors.length ? 'FAIL' : 'PASS', source_nbt_sha256: metadata.sha256, renderer: 'Minecraft Builder / Lodestone 0.6.0 bundled default pack', screenshot_type: 'actual_nbt_software_render', evidence, browser_errors: errors, final_nbt_mutated: false }, null, 2)}\n`)
  if (errors.length) throw new Error(errors.join('\n'))
} finally {
  await browser.close()
}
