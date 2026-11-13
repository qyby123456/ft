import fs from 'node:fs/promises'
import path from 'node:path'
import { spawn } from 'node:child_process'
import { createHash } from 'node:crypto'
import { createRequire } from 'node:module'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const runtime = createRequire('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/.entry.cjs')
const { chromium } = runtime('playwright')
const sharp = runtime('sharp')
const output = path.join(root, 'preview')
await fs.mkdir(output, { recursive: true })
const sourceSha256 = createHash('sha256').update(await fs.readFile(path.join(root, 'room.nbt'))).digest('hex')
const server = spawn(process.execPath, [
  'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/preview.mjs',
  '--nbt', path.join(root, 'room.nbt'), '--host', '127.0.0.1', '--port', '0', '--no-open',
], { windowsHide: true, stdio: ['ignore', 'pipe', 'pipe'] })
let serverLog = ''
const baseUrl = await new Promise((resolve, reject) => {
  const timer = setTimeout(() => reject(new Error('Preview startup timed out')), 90000)
  server.stdout.on('data', data => {
    serverLog += data.toString()
    const match = serverLog.match(/Minecraft build preview: (http:\/\/[^\s]+)/)
    if (match) { clearTimeout(timer); resolve(match[1]) }
  })
  server.stderr.on('data', data => { serverLog += data.toString() })
  server.on('exit', code => { clearTimeout(timer); reject(new Error(`Preview stopped: ${code}: ${serverLog}`)) })
})
let browser
try {
  browser = await chromium.launch({ executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe', headless: true, args: ['--enable-webgl', '--use-angle=swiftshader', '--enable-unsafe-swiftshader'] })
  const page = await browser.newPage({ viewport: { width: 1536, height: 1024 }, deviceScaleFactor: 1 })
  const errors = []
  page.on('pageerror', error => errors.push(error.message))
  await page.goto(baseUrl, { waitUntil: 'networkidle', timeout: 120000 })
  await page.waitForFunction(() => document.getElementById('loading')?.classList.contains('hidden') || document.getElementById('loading')?.style.display === 'none', null, { timeout: 120000 })
  const moduleText = await page.evaluate(async () => (await fetch('/src/main.js')).text())
  const moduleUrl = moduleText.match(/import\s*\{[^}]*NbtFile[^}]*\}\s*from\s*["']([^"']+)["']/)?.[1]
  if (!moduleUrl) throw new Error('Could not resolve Lodestone module from installed viewer')
  await page.evaluate(async ({ moduleUrl }) => {
    const { NbtFile, Structure, ThreeStructureRenderer, loadDefaultPackResources } = await import(moduleUrl)
    const file = NbtFile.read(new Uint8Array(await (await fetch('/__minecraft_builder__/build.nbt')).arrayBuffer()))
    const source = Structure.fromNbt(file.root)
    const { resources } = await loadDefaultPackResources({ baseUrl: `${location.origin}/__minecraft_builder__/default-pack/` })
    document.body.innerHTML = '<canvas id="m18"></canvas><div id="caption"></div><div id="subcaption"></div><div id="footer">ACTUAL NBT · LODESTONE 0.6.0 · SOFTWARE PREVIEW, NOT A MINECRAFT SCREENSHOT</div>'
    const style = document.createElement('style')
    style.textContent = 'html,body{margin:0;width:100%;height:100%;background:#0b1624;overflow:hidden;font-family:Arial,"Microsoft YaHei",sans-serif}#m18{display:block;width:1536px;height:1024px}#caption{position:fixed;top:27px;left:34px;color:#edf7ff;font-size:30px;letter-spacing:1px;text-shadow:0 2px 8px #051224}#subcaption{position:fixed;top:72px;left:35px;color:#add8f1;font-size:17px;text-shadow:0 2px 8px #051224}#footer{position:fixed;bottom:20px;left:35px;color:#8faec5;font-size:13px;text-shadow:0 2px 8px #051224}'
    document.head.append(style)
    const renderer = new ThreeStructureRenderer(document.getElementById('m18'), source, resources, {
      preserveDrawingBuffer: true, asyncBuild: false, antialias: true, drawDistance: 1000,
      sunlight: {
        direction: [0.35, 0.85, -0.2], intensity: 0.35, ambientIntensity: 0.55, fillIntensity: 0.2,
        color: [0.8, 0.9, 1], ambientColor: [0.72, 0.85, 1.0], rimIntensity: 0.1, exposure: 0.94,
        sky: { zenithColor: [0.015, 0.035, 0.065], horizonColor: [0.015, 0.035, 0.065], groundColor: [0.015, 0.035, 0.065], sunGlowIntensity: 0, stars: { enabled: false } },
        fog: { color: [0.04, 0.08, 0.13], density: 0.0002, heightFalloff: 0.001 },
        emissive: { range: 10, intensity: 1.35, tint: [1, 0.75, 0.4] },
      },
    })
    renderer.setViewport(0, 0, 1536, 1024, 1)
    await renderer.whenReady()
    window.m18 = { renderer, source }
    window.renderM18 = async view => {
      let shown = source
      if (view.cutaway) {
        const records = source.getBlocks().filter(block => {
          const [across, height, depth] = block.pos
          if (height >= 10) return false
          if (height >= 1 && (view.west ? across <= 2 : across >= 12)) return false
          if (height >= 1 && (view.north ? depth === 0 : depth === 47)) return false
          return true
        })
        const index = new Map(records.map(block => [block.pos.join(','), block]))
        shown = { getSize: () => source.getSize(), getBlocks: () => records, getBlock: position => index.get(position.join(',')) ?? null }
      }
      renderer.setStructure(shown)
      renderer.setFOV(view.fov ?? 55)
      renderer.lookAt(view.camera, view.target)
      await renderer.whenReady()
      document.getElementById('caption').textContent = view.title
      document.getElementById('subcaption').textContent = view.cutaway ? '15 × 12 × 48 | Display-only roof / near-wall cutaway; source NBT is fully enclosed' : '15 × 12 × 48 | Full final NBT; nonmelting frost, warm candles, six suspended cages'
      renderer.drawStructure()
      await new Promise(resolve => requestAnimationFrame(() => { renderer.drawStructure(); resolve() }))
      return { blocks: shown.getBlocks().length, camera: renderer.getCamera(), sourceBlocks: source.getBlocks().length }
    }
  }, { moduleUrl })
  const views = [
    { name: 'north-interior', title: 'M18 · 冰霜烛龛悬笼直廊 · NORTH → SOUTH', camera: [7.5, 3.1, 2.2], target: [7.5, 3.8, 47], fov: 72 },
    { name: 'south-interior', title: 'M18 · 完整屋顶 / 双端贯通 · SOUTH → NORTH', camera: [7.5, 3.1, 45.8], target: [7.5, 3.8, 0], fov: 72 },
    { name: 'northwest', title: 'M18 · NORTHWEST · 只读剖切', camera: [-37, 32, -29], target: [7.5, 4, 23.5], cutaway: true, west: true, north: true, fov: 34 },
    { name: 'northeast', title: 'M18 · NORTHEAST · 只读剖切', camera: [52, 32, -29], target: [7.5, 4, 23.5], cutaway: true, west: false, north: true, fov: 34 },
    { name: 'southwest', title: 'M18 · SOUTHWEST · 只读剖切', camera: [-37, 32, 77], target: [7.5, 4, 23.5], cutaway: true, west: true, north: false, fov: 34 },
    { name: 'southeast', title: 'M18 · SOUTHEAST · 只读剖切', camera: [52, 32, 77], target: [7.5, 4, 23.5], cutaway: true, west: false, north: false, fov: 34 },
    { name: 'complete-exterior', title: 'M18 · 完整外壳 / 无分支 / 双端门洞', camera: [-38, 35, -34], target: [7.5, 4, 23.5], fov: 38 },
  ]
  const evidence = []
  for (const view of views) {
    const result = await page.evaluate(view => window.renderM18(view), view)
    await page.screenshot({ path: path.join(output, `${view.name}.png`) })
    evidence.push({ ...view, ...result, path: `preview/${view.name}.png`, source_sha256: sourceSha256 })
    console.log(`Rendered ${view.name}`)
  }
  await fs.copyFile(path.join(output, 'north-interior.png'), path.join(root, 'preview.png'))
  const tiles = await Promise.all(['northwest', 'northeast', 'southwest', 'southeast'].map(name => sharp(path.join(output, `${name}.png`)).resize(1152, 768).toBuffer()))
  await sharp({ create: { width: 2304, height: 1536, channels: 3, background: '#0b1624' } }).composite(tiles.map((input, index) => ({ input, left: index % 2 * 1152, top: Math.floor(index / 2) * 768 }))).png().toFile(path.join(output, 'four-view.png'))
  await fs.writeFile(path.join(root, 'qa/render.json'), `${JSON.stringify({ status: errors.length ? 'FAIL' : 'PASS', renderer: 'Minecraft Builder installed preview + Lodestone 0.6.0 / Chromium software WebGL', source_sha256: sourceSha256, full_nbt_blocks: 8640, views: evidence, page_errors: errors, limitations: ['Software previews, not game screenshots; emissive light is approximate.', 'Four-view cutaways are display-only filters; source NBT is never edited.'] }, null, 2)}\n`)
  if (errors.length) throw new Error(errors.join('\n'))
} finally {
  if (browser) await browser.close()
  server.kill()
  await fs.writeFile(path.join(root, 'qa/preview-server.log'), serverLog)
}
