import { spawn } from 'node:child_process'
import { createRequire } from 'node:module'
import { createHash } from 'node:crypto'
import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const skillRoot = 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
const runtime = 'C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright/package.json'
const playwright = createRequire(runtime)('playwright')
const suppliedUrl = process.argv[2]
let server
let browser
let serverLog = ''
const views = [
  { name: 'north-exterior', position: [37, 29, -37], target: [7.5, 4.5, 18], fov: 48 },
  { name: 'south-exterior', position: [-22, 29, 77], target: [7.5, 4.5, 22], fov: 48 },
  { name: 'east-exterior', position: [66, 32, 41], target: [7.5, 4.5, 20], fov: 48 },
  { name: 'west-exterior', position: [-51, 32, -1], target: [7.5, 4.5, 20], fov: 48 },
  { name: 'north-interior', position: [7.5, 3.15, 2.1], target: [7.5, 3.6, 36], fov: 75 },
  { name: 'south-interior', position: [7.5, 3.15, 37.8], target: [7.5, 3.6, 3], fov: 75 },
  { name: 'east-interior', position: [47, 25, 20], target: [7.5, 3, 20], fov: 57, section: 'east' },
  { name: 'west-interior', position: [-32, 25, 20], target: [7.5, 3, 20], fov: 57, section: 'west' },
]
const errors = []

async function startPreview() {
  if (suppliedUrl) return suppliedUrl
  server = spawn(process.execPath, ['scripts/preview.mjs', '--nbt', path.join(root, 'room.nbt'), '--no-open', '--port', '5195'], { cwd: skillRoot, windowsHide: true, stdio: ['ignore', 'pipe', 'pipe'] })
  return new Promise((resolve, reject) => {
    const timeout = setTimeout(() => reject(new Error('Preview server startup timed out')), 90000)
    const receive = buffer => {
      serverLog += buffer.toString()
      const match = serverLog.match(/Minecraft build preview: (http:\/\/[^\s]+)/)
      if (match) {
        clearTimeout(timeout)
        resolve(match[1])
      }
    }
    server.stdout.on('data', receive)
    server.stderr.on('data', receive)
    server.on('error', reject)
    server.on('exit', code => { if (code) reject(new Error(`Preview server exited: ${code}`)) })
  })
}

try {
  const url = await startPreview()
  browser = await playwright.chromium.launch({ headless: true, executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe', args: ['--use-angle=swiftshader', '--enable-webgl', '--ignore-gpu-blocklist'] })
  const page = await browser.newPage({ viewport: { width: 1600, height: 1000 }, deviceScaleFactor: 1 })
  page.on('pageerror', error => errors.push(String(error)))
  await page.route('**/src/main.js', async route => {
    const response = await route.fetch()
    let body = await response.text()
    body = body.replace("loading.classList.add('hidden')", "window.m15 = {renderer, structure, Structure}; loading.classList.add('hidden')")
    await route.fulfill({ response, body })
  })
  await page.goto(url, { waitUntil: 'networkidle', timeout: 120000 })
  await page.waitForFunction(() => !!window.m15, { timeout: 120000 })
  await page.addStyleTag({ content: '#lighting-toggle,.hud,.controls,header,footer,#loading,.topbar,.badge,.panel{display:none!important}' })
  for (const view of views) {
    const render = await page.evaluate(async options => {
      const { renderer, structure, Structure } = window.m15
      let selected = structure
      if (options.section) {
        selected = new Structure(structure.getSize())
        for (const block of structure.getBlocks()) {
          const [horizontal, height] = block.pos
          if (height >= 10) continue
          if (height > 0 && ((options.section === 'east' && horizontal >= 11) || (options.section === 'west' && horizontal <= 3))) continue
          selected.addBlock(block.pos, block.state.getName(), block.state.getProperties(), block.nbt)
        }
      }
      renderer.setStructure(selected)
      await renderer.whenReady()
      const interior = options.name.endsWith('-interior') && !options.section
      renderer.setSunlight({ direction: [0.2, 0.8, -0.3], intensity: interior ? 0.4 : 0.9, ambientIntensity: interior ? 0.55 : 0.66, fillIntensity: 0.2, rimIntensity: 0.12, exposure: 1, sky: { zenithColor: [0.015, 0.02, 0.03], horizonColor: [0.025, 0.03, 0.045], groundColor: [0.01, 0.012, 0.018], stars: { enabled: false } }, fog: { color: [0.025, 0.03, 0.04], density: interior ? 0.0005 : 0, heightFalloff: 0.01 } })
      renderer.setFOV(options.fov)
      renderer.lookAt(options.position, options.target)
      renderer.drawStructure()
      return { displayed_records: selected.getBlocks().length, section: options.section ?? null }
    }, view)
    await page.locator('#preview').screenshot({ path: path.join(root, 'views', `${view.name}.png`) })
    Object.assign(view, render)
    console.log(`Rendered ${view.name}`)
  }
  await fs.copyFile(path.join(root, 'views', 'north-interior.png'), path.join(root, 'preview.png'))
  const fingerprint = createHash('sha256').update(await fs.readFile(path.join(root, 'room.nbt'))).digest('hex')
  await fs.writeFile(path.join(root, 'qa', 'view-render-qa.json'), JSON.stringify({ status: errors.length ? 'FAIL' : 'PASS', sha256: fingerprint, renderer: 'Minecraft Builder / Lodestone ThreeStructureRenderer / bundled default resource pack', viewport: [1600, 1000], views, errors, cutaway_changes_nbt: false, client_screenshot: false, ai_image: false, limits: 'Display lighting, particle-free candles and default-pack plant colors are not a full Forge client lighting/particle simulation.' }, null, 2) + '\n')
  if (errors.length) throw new Error(errors.join('\n'))
} finally {
  if (browser) await browser.close()
  if (server) server.kill()
  await fs.writeFile(path.join(root, 'evidence', 'lodestone-preview.log'), serverLog)
}
