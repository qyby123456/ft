import {spawn} from 'node:child_process'
import {createHash} from 'node:crypto'
import fs from 'node:fs/promises'
import {createRequire} from 'node:module'
import path from 'node:path'
import {fileURLToPath} from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const skill = 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
const require = createRequire('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/package.json')
const {chromium} = require('playwright')
const hash = bytes => createHash('sha256').update(bytes).digest('hex')
const fingerprint = hash(await fs.readFile(path.join(root, 'room.nbt')))
const server = spawn(process.execPath, [path.join(skill, 'scripts/preview.mjs'), '--nbt', path.join(root, 'room.nbt'), '--port', '0', '--no-open'], {cwd: skill, windowsHide: true, stdio: ['ignore', 'pipe', 'pipe']})
let serverLog = ''
let browser
try {
  const url = await new Promise((resolve, reject) => {
    const timeout = setTimeout(() => reject(new Error('Preview startup timeout')), 120000)
    server.stdout.on('data', bytes => {
      serverLog += bytes.toString()
      const match = serverLog.match(/Minecraft build preview: (http[^\r\n]+)/)
      if (match) { clearTimeout(timeout); resolve(match[1]) }
    })
    server.stderr.on('data', bytes => { serverLog += bytes.toString() })
    server.once('exit', code => { clearTimeout(timeout); reject(new Error(`Preview exited ${code}: ${serverLog}`)) })
  })
  browser = await chromium.launch({executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe', headless: true, args: ['--no-sandbox', '--use-angle=swiftshader', '--enable-unsafe-swiftshader']})
  const page = await browser.newPage({viewport: {width: 1536, height: 1024}, deviceScaleFactor: 1})
  const errors = []
  page.on('pageerror', error => errors.push(String(error)))
  await page.goto(url, {waitUntil: 'domcontentloaded', timeout: 120000})
  await page.waitForSelector('#loading.hidden', {state: 'attached', timeout: 180000})
  await page.evaluate(async () => {
    const sourceCode = await (await fetch('/src/main.js')).text()
    const moduleUrl = sourceCode.match(/import\s*\{[^}]*ThreeStructureRenderer[^}]*\}\s*from\s*["']([^"']+)/)?.[1]
    if (!moduleUrl) throw new Error('Installed Lodestone import not found')
    const {NbtFile, Structure, ThreeStructureRenderer, loadDefaultPackResources} = await import(moduleUrl)
    const bytes = new Uint8Array(await (await fetch('/__minecraft_builder__/build.nbt')).arrayBuffer())
    const source = Structure.fromNbt(NbtFile.read(bytes).root)
    const pack = await loadDefaultPackResources({baseUrl: new URL('/__minecraft_builder__/default-pack/', location.href).toString()})
    document.body.innerHTML = '<canvas id="review-canvas"></canvas>'
    const canvas = document.getElementById('review-canvas')
    canvas.style.cssText = 'display:block;position:fixed;inset:0;width:100vw;height:100vh;'
    const renderer = new ThreeStructureRenderer(canvas, source, pack.resources, {preserveDrawingBuffer: true, antialias: true, asyncBuild: true, sunlight: {direction: [0.4, 0.9, -0.25], intensity: 0.95, ambientIntensity: 0.9, fillIntensity: 0.5, rimIntensity: 0.2, exposure: 1.1, sky: {zenithColor: [0.045, 0.065, 0.11], horizonColor: [0.09, 0.12, 0.17], groundColor: [0.025, 0.035, 0.055], stars: {enabled: false}}, fog: {density: 0}}})
    renderer.setViewport(0, 0, 1536, 1024, 1)
    await renderer.whenReady()
    window.c01Review = {renderer, source, Structure}
  })
  const views = [
    {id: 'hero-cutaway', file: 'preview.png', mode: 'hero', position: [37, 30, -31], target: [9.5, 3.7, 9.5], fov: 38},
    {id: 'north-interior', file: 'views/north-interior.png', mode: 'north', position: [20, 29, -36], target: [9.5, 3.6, 9.5], fov: 38},
    {id: 'south-interior', file: 'views/south-interior.png', mode: 'south', position: [-1, 29, 55], target: [9.5, 3.6, 9.5], fov: 38},
    {id: 'west-interior', file: 'views/west-interior.png', mode: 'west', position: [-36, 29, -1], target: [9.5, 3.6, 9.5], fov: 38},
    {id: 'east-interior', file: 'views/east-interior.png', mode: 'east', position: [55, 29, 20], target: [9.5, 3.6, 9.5], fov: 38},
    {id: 'north-exterior', file: 'views/north-exterior.png', mode: 'full', position: [34, 27, -35], target: [9.5, 5, 9.5], fov: 39},
    {id: 'south-exterior', file: 'views/south-exterior.png', mode: 'full', position: [-15, 27, 54], target: [9.5, 5, 9.5], fov: 39},
    {id: 'west-exterior', file: 'views/west-exterior.png', mode: 'full', position: [-35, 27, -15], target: [9.5, 5, 9.5], fov: 39},
    {id: 'east-exterior', file: 'views/east-exterior.png', mode: 'full', position: [54, 27, 34], target: [9.5, 5, 9.5], fov: 39},
    {id: 'top-interior', file: 'views/top-interior.png', mode: 'top', position: [9.5, 59, 9.6], target: [9.5, 0, 9.5], fov: 30},
  ]
  const evidence = []
  for (const view of views) {
    view.fov *= 0.74
    const statistics = await page.evaluate(async options => {
      const {renderer, source, Structure} = window.c01Review
      let displayed = source
      if (options.mode !== 'full') {
        displayed = new Structure(source.getSize())
        for (const block of source.getBlocks()) {
          const [across, height, depth] = block.pos
          const hidden = height >= 8 || height >= 2 && (
            (options.mode === 'north' || options.mode === 'hero') && depth <= 1 ||
            options.mode === 'south' && depth >= 17 ||
            options.mode === 'west' && across <= 1 ||
            (options.mode === 'east' || options.mode === 'hero') && across >= 17)
          if (!hidden) displayed.addBlock(block.pos, block.state.getName(), block.state.getProperties(), block.nbt)
        }
      }
      renderer.setStructure(displayed)
      await renderer.whenReady()
      renderer.setCamera({position: options.position, target: options.target, fov: options.fov})
      renderer.drawStructure()
      await new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)))
      renderer.drawStructure()
      return {source_records: source.getBlocks().length, displayed_records: displayed.getBlocks().length, source_unchanged: source.getBlocks().length === 3971}
    }, view)
    const target = path.join(root, view.file)
    await page.locator('#review-canvas').screenshot({path: target, timeout: 120000})
    evidence.push({...view, ...statistics, sha256: hash(await fs.readFile(target))})
    process.stdout.write(`Rendered ${view.id}\n`)
  }
  if (hash(await fs.readFile(path.join(root, 'room.nbt'))) !== fingerprint) throw new Error('Source NBT changed during render')
  if (errors.length) throw new Error(errors.join('\n'))
  const sheet = await browser.newPage({viewport: {width: 1600, height: 720}, deviceScaleFactor: 1})
  const sheetViews = evidence.filter(view => /interior|exterior/.test(view.id) && !view.id.startsWith('top'))
  const tiles = await Promise.all(sheetViews.map(async view => `<article><h2>${view.id}</h2><img src="data:image/png;base64,${(await fs.readFile(path.join(root, view.file))).toString('base64')}"></article>`))
  await sheet.setContent(`<html><meta charset="utf-8"><style>body{margin:0;padding:20px;background:#0c121d;color:#e6e9ef;font-family:Arial}h1{font-size:24px;margin:0 0 6px}p{font-size:12px;margin:0 0 16px;color:#9caec4}main{display:grid;grid-template-columns:repeat(4,1fr);gap:12px}article{background:#131d2b;border:1px solid #2a3c55;border-radius:7px;overflow:hidden}h2{font-size:14px;margin:10px 12px}img{width:100%;display:block}</style><h1>C01 石角岗哨 / NBT 结构八视图</h1><p>上排：北南西东内部剖视；下排：北南西东完整外壳。软件渲染，剖切不改动最终 NBT。${fingerprint}</p><main>${tiles.join('')}</main></html>`)
  await sheet.screenshot({path: path.join(root, 'views/review-sheet.png'), fullPage: true})
  await fs.writeFile(path.join(root, 'qa/render.json'), JSON.stringify({status: 'PASS', renderer: 'Minecraft Builder Lodestone 0.6.0 WebGL', source: 'room.nbt', structure_sha256: fingerprint, actual_nbt_render: true, source_nbt_unchanged: true, views: evidence, browser_errors: errors, note: 'Software render, not an in-game screenshot. Cutaways hide only display copies; full shell views use unmodified NBT.'}, null, 2) + '\n')
} finally {
  if (browser) await browser.close()
  server.kill()
  await fs.writeFile(path.join(root, 'qa/preview-server.log'), serverLog)
}
