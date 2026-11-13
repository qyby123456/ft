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
await fs.mkdir(path.join(root, 'views'), {recursive: true})
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
    if (!moduleUrl) throw new Error('Cannot resolve installed Lodestone viewer import')
    const {NbtFile, Structure, ThreeStructureRenderer, loadDefaultPackResources} = await import(moduleUrl)
    const bytes = new Uint8Array(await (await fetch('/__minecraft_builder__/build.nbt')).arrayBuffer())
    const source = Structure.fromNbt(NbtFile.read(bytes).root)
    const pack = await loadDefaultPackResources({baseUrl: new URL('/__minecraft_builder__/default-pack/', location.href).toString()})
    document.body.innerHTML = '<canvas id="review-canvas"></canvas>'
    const canvas = document.getElementById('review-canvas')
    canvas.style.cssText = 'display:block;position:fixed;inset:0;width:100vw;height:100vh;'
    const renderer = new ThreeStructureRenderer(canvas, source, pack.resources, {preserveDrawingBuffer: true, antialias: true, asyncBuild: true, sunlight: {direction: [0.35, 0.85, -0.25], intensity: 0.9, ambientIntensity: 0.82, fillIntensity: 0.4, rimIntensity: 0.2, exposure: 1.04, sky: {zenithColor: [0.055, 0.09, 0.16], horizonColor: [0.11, 0.17, 0.25], groundColor: [0.035, 0.05, 0.08], stars: {enabled: false}}, fog: {density: 0}}})
    renderer.setViewport(0, 0, 1536, 1024, 1)
    await renderer.whenReady()
    window.s08Review = {renderer, source, Structure}
  })
  const views = [
    {id: 'hero', file: 'preview.png', mode: 'full', position: [5.5, 3.65, 1.7], target: [5.5, 3.65, 15], fov: 78},
    {id: 'north-interior', file: 'views/north-interior.png', mode: 'north', position: [16, 17, -22], target: [5.5, 3.6, 7.5], fov: 39},
    {id: 'south-interior', file: 'views/south-interior.png', mode: 'south', position: [-5, 17, 38], target: [5.5, 3.6, 8.5], fov: 39},
    {id: 'west-interior', file: 'views/west-interior.png', mode: 'west', position: [-23, 17, 0], target: [5.5, 3.6, 8], fov: 43},
    {id: 'east-interior', file: 'views/east-interior.png', mode: 'east', position: [34, 17, 16], target: [5.5, 3.6, 8], fov: 43},
    {id: 'north-exterior', file: 'views/north-exterior.png', mode: 'full', position: [19, 18, -23], target: [5.5, 4.5, 8], fov: 40},
    {id: 'south-exterior', file: 'views/south-exterior.png', mode: 'full', position: [-8, 18, 39], target: [5.5, 4.5, 8], fov: 40},
    {id: 'west-exterior', file: 'views/west-exterior.png', mode: 'full', position: [-27, 18, 0], target: [5.5, 4.5, 8], fov: 42},
    {id: 'east-exterior', file: 'views/east-exterior.png', mode: 'full', position: [38, 18, 16], target: [5.5, 4.5, 8], fov: 42},
  ]
  const evidence = []
  for (const view of views) {
    const statistics = await page.evaluate(async options => {
      const {renderer, source, Structure} = window.s08Review
      let displayed = source
      if (options.mode !== 'full') {
        displayed = new Structure(source.getSize())
        for (const block of source.getBlocks()) {
          const [across, height, depth] = block.pos
          const wallAttachedShelf = block.state.is('minecraft:smooth_quartz_slab') && (options.mode === 'west' && across === 2 || options.mode === 'east' && across === 8)
          const hidden = wallAttachedShelf || height >= 8 || height >= 2 && (options.mode === 'north' && depth <= 1 || options.mode === 'south' && depth >= 14 || options.mode === 'west' && across <= 1 || options.mode === 'east' && across >= 9)
          if (!hidden) displayed.addBlock(block.pos, block.state.getName(), block.state.getProperties(), block.nbt)
        }
      }
      renderer.setStructure(displayed)
      await renderer.whenReady()
      renderer.setCamera({position: options.position, target: options.target, fov: options.fov})
      renderer.drawStructure()
      await new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)))
      renderer.drawStructure()
      return {source_records: source.getBlocks().length, displayed_records: displayed.getBlocks().length, source_unchanged: source.getBlocks().length === 1760}
    }, view)
    const target = path.join(root, view.file)
    await page.locator('#review-canvas').screenshot({path: target, timeout: 120000})
    evidence.push({...view, ...statistics, sha256: hash(await fs.readFile(target))})
    process.stdout.write(`Rendered ${view.id}\n`)
  }
  if (hash(await fs.readFile(path.join(root, 'room.nbt'))) !== fingerprint) throw new Error('Source NBT changed while rendering')
  if (errors.length) throw new Error(errors.join('\n'))
  await fs.writeFile(path.join(root, 'qa/render.json'), JSON.stringify({status: 'PASS', renderer: 'Minecraft Builder installed preview / Lodestone WebGL', source: 'room.nbt', structure_sha256: fingerprint, actual_nbt_render: true, source_nbt_unchanged: true, views: evidence, browser_errors: errors, note: 'Software render, not an in-game screenshot. Cutaways hide only display copies.'}, null, 2) + '\n')
} finally {
  if (browser) await browser.close()
  server.kill()
  await fs.writeFile(path.join(root, 'qa/preview-server.log'), serverLog)
}
