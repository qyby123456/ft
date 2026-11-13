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
const hash = content => createHash('sha256').update(content).digest('hex')
const group = JSON.parse(await fs.readFile(path.join(root, 'group.json'), 'utf8'))
const chosen = process.argv.slice(2)
const selected = chosen.length ? group.rooms.filter(room => chosen.includes(room.id)) : group.rooms
const server = spawn(process.execPath, [path.join(skill, 'scripts/preview.mjs'), '--nbt', path.join(root, group.rooms[0].id, 'room.nbt'), '--port', '0', '--no-open'], {cwd: skill, windowsHide: true, stdio: ['ignore', 'pipe', 'pipe']})
let serverLog = ''
let browser
try {
  const url = await new Promise((resolve, reject) => {
    const timeout = setTimeout(() => reject(new Error('Preview startup timeout')), 90000)
    server.stdout.on('data', bytes => {
      serverLog += bytes.toString()
      const match = serverLog.match(/Minecraft build preview: (http[^\r\n]+)/)
      if (match) { clearTimeout(timeout); resolve(match[1]) }
    })
    server.stderr.on('data', bytes => { serverLog += bytes.toString() })
    server.once('exit', code => { clearTimeout(timeout); reject(new Error(`Preview exited ${code}`)) })
  })
  browser = await chromium.launch({executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe', headless: true, args: ['--no-sandbox', '--use-angle=swiftshader', '--enable-unsafe-swiftshader']})
  const page = await browser.newPage({viewport: {width: 1200, height: 900}, deviceScaleFactor: 1})
  const errors = []
  page.on('pageerror', error => errors.push(String(error)))
  await page.goto(url, {waitUntil: 'domcontentloaded', timeout: 120000})
  await page.waitForSelector('#loading.hidden', {state: 'attached', timeout: 180000})
  await page.evaluate(async () => {
    const sourceCode = await (await fetch('/src/main.js')).text()
    const moduleUrl = sourceCode.match(/import\s*\{[^}]*ThreeStructureRenderer[^}]*\}\s*from\s*["']([^"']+)/)?.[1]
    if (!moduleUrl) throw new Error('Installed Lodestone import not found')
    const {NbtFile, Structure, ThreeStructureRenderer, loadDefaultPackResources} = await import(moduleUrl)
    const pack = await loadDefaultPackResources({baseUrl: new URL('/__minecraft_builder__/default-pack/', location.href).toString()})
    document.body.innerHTML = '<canvas id="review-canvas"></canvas>'
    const canvas = document.getElementById('review-canvas')
    canvas.style.cssText = 'display:block;position:fixed;inset:0;width:100vw;height:100vh;'
    const renderer = new ThreeStructureRenderer(canvas, new Structure([1, 1, 1]), pack.resources, {preserveDrawingBuffer: true, antialias: true, asyncBuild: true, sunlight: {direction: [0.4, 0.9, -0.25], intensity: 0.95, ambientIntensity: 0.9, fillIntensity: 0.5, rimIntensity: 0.2, exposure: 1.1, sky: {zenithColor: [0.045, 0.065, 0.11], horizonColor: [0.09, 0.12, 0.17], groundColor: [0.025, 0.035, 0.055], stars: {enabled: false}}, fog: {density: 0}}})
    renderer.setViewport(0, 0, 1200, 900, 1)
    await renderer.whenReady()
    window.forestReview = {renderer, NbtFile, Structure}
  })
  for (const room of selected) {
    const folder = path.join(root, room.id)
    await fs.mkdir(path.join(folder, 'views'), {recursive: true})
    const bytes = await fs.readFile(path.join(folder, 'room.nbt'))
    const fingerprint = hash(bytes)
    await page.evaluate(encoded => {
      const {NbtFile, Structure} = window.forestReview
      const source = Structure.fromNbt(NbtFile.read(Uint8Array.from(atob(encoded), symbol => symbol.charCodeAt(0))).root)
      window.forestReview.source = source
    }, bytes.toString('base64'))
    const [width, height, depth] = room.size
    const centerX = width / 2
    const centerZ = depth / 2
    const distance = Math.max(width, depth, height) * 1.5
    const views = [{id: 'hero', mode: 'hero', file: 'preview.png', position: [centerX - distance, distance, centerZ + distance]}]
    views.push({id: 'chest-roots-closeup', mode: 'south', file: 'views/chest-roots-closeup.png', position: [11, 12, 22], target: [9, 5, 4], fov: 43})
    views.push({id: 'player-interior', mode: 'full', file: 'views/player-interior.png', position: [10.5, 3.62, 13.5], target: [9, 5, 3], fov: 85})
    for (const kind of ['interior', 'exterior']) {
      for (const direction of ['north', 'south', 'west', 'east']) {
        const position = direction === 'north' ? [centerX + distance * 0.25, distance, centerZ - distance * 1.4] : direction === 'south' ? [centerX - distance * 0.25, distance, centerZ + distance * 1.4] : direction === 'west' ? [centerX - distance * 1.4, distance, centerZ - distance * 0.25] : [centerX + distance * 1.4, distance, centerZ + distance * 0.25]
        views.push({id: `${direction}-${kind}`, mode: kind === 'exterior' ? 'full' : direction, file: `views/${direction}-${kind}.png`, position})
      }
    }
    const evidence = []
    for (const view of views) {
      const statistics = await page.evaluate(async options => {
        const {renderer, source, Structure} = window.forestReview
        const [width, height, depth] = source.getSize()
        let displayed = source
        if (options.mode !== 'full') {
          displayed = new Structure(source.getSize())
          for (const block of source.getBlocks()) {
            const [across, vertical, forward] = block.pos
            const hidden = vertical >= height - 1 || vertical >= 2 && (options.mode === 'north' && forward <= 1 || (options.mode === 'south' || options.mode === 'hero') && forward >= depth - 2 || (options.mode === 'west' || options.mode === 'hero') && across <= 1 || options.mode === 'east' && across >= width - 2)
            if (!hidden) displayed.addBlock(block.pos, block.state.getName(), block.state.getProperties(), block.nbt)
          }
        }
        renderer.setStructure(displayed)
        await renderer.whenReady()
        renderer.setCamera({position: options.position, target: options.target ?? [width / 2, options.mode === 'full' ? height / 2 : height * 0.32, depth / 2], fov: options.fov ?? 35})
        renderer.drawStructure()
        await new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)))
        renderer.drawStructure()
        return {source_records: source.getBlocks().length, displayed_records: displayed.getBlocks().length, cutaway_display_only: options.mode !== 'full'}
      }, view)
      const target = path.join(folder, view.file)
      await page.locator('#review-canvas').screenshot({path: target, timeout: 120000})
      evidence.push({...view, ...statistics, sha256: hash(await fs.readFile(target))})
    }
    if (hash(await fs.readFile(path.join(folder, 'room.nbt'))) !== fingerprint) throw new Error('Source NBT changed during render')
    if (errors.length) throw new Error(errors.join('\n'))
    await fs.writeFile(path.join(folder, 'qa/render.json'), JSON.stringify({status: 'PASS_RENDERED_NOT_RECEIVER_APPROVED', renderer: 'Minecraft Builder Lodestone WebGL', source_nbt_sha256: fingerprint, actual_nbt_render: true, source_nbt_unchanged: true, views: evidence, errors, limitations: 'Software preview, not a Minecraft client screenshot. Cutaway affects display copies only.'}, null, 2) + '\n')
    console.log('RENDERED', room.id)
  }
} finally {
  if (browser) await browser.close()
  server.kill()
  await fs.writeFile(path.join(root, 'qa/preview-server.log'), serverLog)
}
