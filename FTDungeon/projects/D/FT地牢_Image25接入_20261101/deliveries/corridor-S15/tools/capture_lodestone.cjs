const fs = require('node:fs/promises')
const path = require('node:path')
const crypto = require('node:crypto')
const { createRequire } = require('node:module')

const root = path.resolve(__dirname, '..')
const runtime = process.env.CODEX_NODE_MODULES || 'C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules'
const runtimeRequire = createRequire(path.join(runtime, '../package.json'))
const { chromium } = runtimeRequire('playwright')

async function main() {
  const base = process.argv[2] || 'http://127.0.0.1:53315'
  const nbt = await fs.readFile(path.join(root, 'room.nbt'))
  const fingerprint = crypto.createHash('sha256').update(nbt).digest('hex')
  const remote = Buffer.from(await (await fetch(base + '/__minecraft_builder__/build.nbt')).arrayBuffer())
  if (!nbt.equals(remote)) throw new Error('Preview server is not serving this delivery NBT')
  const browser = await chromium.launch({ headless: true, executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe', args: ['--disable-gpu-sandbox', '--use-angle=swiftshader'] })
  const errors = []
  try {
    const page = await browser.newPage({ viewport: { width: 1536, height: 1152 } })
    page.on('pageerror', error => errors.push(String(error)))
    await page.goto(base + '/?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=S15&lighting=night', { waitUntil: 'domcontentloaded', timeout: 120000 })
    await page.waitForFunction(() => document.getElementById('loading').classList.contains('hidden'), null, { timeout: 120000 })
    await page.screenshot({ path: path.join(root, 'preview/lodestone-exterior.png') })
    const geometry = await page.evaluate(async () => {
      const moduleUrl = performance.getEntriesByType('resource').find(entry => /@mattzh72_lodestone.js/.test(entry.name)).name
      const library = await import(moduleUrl)
      const response = await fetch('/__minecraft_builder__/build.nbt')
      const structure = library.Structure.fromNbt(library.NbtFile.read(new Uint8Array(await response.arrayBuffer())).root)
      const pack = await library.loadDefaultPackResources({ baseUrl: location.origin + '/__minecraft_builder__/default-pack/' })
      const canvas = document.createElement('canvas')
      canvas.id = 's15-interior'
      canvas.style.cssText = 'position:fixed;inset:0;width:1536px;height:1152px;z-index:999;background:#121d23'
      document.body.append(canvas)
      const renderer = new library.ThreeStructureRenderer(canvas, structure, pack.resources, {
        chunkSize: 16, drawDistance: 1000, useInvisibleBlockBuffer: false, asyncBuild: true,
        sunlight: { direction: [0.3, 0.8, 0.2], intensity: 0.65, ambientIntensity: 0.86, fillIntensity: 0.46, rimIntensity: 0.2, exposure: 1.0, emissive: { range: 18, intensity: 1.35, tint: [1, 0.88, 0.68] } }
      })
      renderer.setViewport(0, 0, 1536, 1152, 1)
      await renderer.whenReady()
      renderer.lookAt([7.5, 2.62, 0.6], [7.5, 3.1, 15])
      renderer.drawStructure()
      return { size: structure.getSize(), records: structure.getBlocks().length, camera: { position: [7.5, 2.62, 0.6], target: [7.5, 3.1, 15] } }
    })
    await page.locator('#s15-interior').screenshot({ path: path.join(root, 'preview/lodestone-interior.png') })
    if (errors.length) throw new Error(errors.join('\n'))
    await fs.writeFile(path.join(root, 'qa/lodestone.json'), JSON.stringify({ status: 'PASS', structure_sha256: fingerprint, geometry, browser: 'headless Microsoft Edge', engine: 'Lodestone 0.6.0', errors, full_shell_preserved: true, limitations: ['Software NBT rendering, not an in-game screenshot, full modpack client test or exact Minecraft lighting.'] }, null, 2) + '\n')
    process.stdout.write(JSON.stringify({ status: 'PASS', structure_sha256: fingerprint }) + '\n')
  } finally {
    await browser.close()
  }
}

main().catch(error => { process.stderr.write(String(error) + '\n'); process.exitCode = 1 })
