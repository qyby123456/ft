import fs from 'node:fs/promises'
import path from 'node:path'
import crypto from 'node:crypto'
import { createRequire } from 'node:module'
import { fileURLToPath } from 'node:url'
const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const runtimeRequire = createRequire('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/package.json')
const { chromium } = runtimeRequire('playwright')
const url = process.argv[2] ?? 'http://127.0.0.1:5397/?nbt=/__minecraft_builder__/build.nbt&name=corridor-M07'
const hook = String.raw`
  let qaCamera = null
  const completeStructure = structure
  const viewCameras = {
    axis: { position: [8.5, 2.65, 1.4], target: [8.5, 3.5, 39], fov: 78 },
    reverse: { position: [8.5, 2.65, 39.6], target: [8.5, 3.5, 2], fov: 78 },
    north: { position: [-25, 28, -31], target: [8.5, 4, 20.5], fov: 47 },
    south: { position: [42, 28, 72], target: [8.5, 4, 20.5], fov: 47 },
    west: { position: [-49, 31, 8], target: [8.5, 4, 20.5], fov: 47 },
    east: { position: [66, 31, 33], target: [8.5, 4, 20.5], fov: 47 },
    sealed: { position: [47, 37, -38], target: [8.5, 5, 20.5], fov: 47 },
  }
  window.renderView = async function(view) {
    let shown = completeStructure
    const cutaway = ['north', 'south', 'west', 'east'].includes(view)
    if (cutaway) {
      shown = new Structure(size)
      for (const entry of completeStructure.getBlocks()) {
        const [blockX, blockY, blockZ] = entry.pos
        if (blockY >= 11) continue
        if (blockY > 0 && ((['north','west'].includes(view) && blockX <= 2) || (['south','east'].includes(view) && blockX >= 14))) continue
        if (blockY > 0 && ((['north','east'].includes(view) && blockZ === 0) || (['south','west'].includes(view) && blockZ === 40))) continue
        shown.addBlock(entry.pos, entry.state.getName(), entry.state.getProperties(), entry.nbt)
      }
    }
    renderer.setStructure(shown)
    await renderer.whenReady()
    renderer.setSunlight({
      direction: vec3.fromValues(0.25, 0.85, -0.35),
      intensity: cutaway || view === 'sealed' ? 1.05 : 0.28,
      ambientIntensity: cutaway || view === 'sealed' ? 0.72 : 0.85,
      ambientColor: [0.76,0.78,0.85], fillIntensity: 0.4, rimIntensity: 0.25, exposure: 1.08,
      sky: { zenithColor:[0.045,0.055,0.075], horizonColor:[0.1,0.12,0.15], groundColor:[0.04,0.045,0.055], stars:{enabled:false} },
      fog: { color:[0.05,0.055,0.07], density:0.00001, heightFalloff:0.001 },
      emissive: { range:14, intensity:1.25, tint:[1,0.82,0.52] },
    })
    qaCamera = viewCameras[view]
    renderer.setCamera(qaCamera)
    renderer.drawStructure()
    window.__corridorView = { view, sourceCells:blockCount, shownCells:shown.getBlocks().length, cutaway, camera:qaCamera }
    return window.__corridorView
  }
`
const browser = await chromium.launch({headless:true, executablePath:'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe', args:['--use-angle=swiftshader','--enable-unsafe-swiftshader']})
const page = await browser.newPage({viewport:{width:1536,height:1024},deviceScaleFactor:1})
const errors = []
page.on('pageerror',error=>errors.push(error.message))
await page.route('**/src/main.js*', async route => {
  const response = await route.fetch()
  let source = await response.text()
  if (!source.includes('const center = vec3.fromValues')) throw new Error('Unexpected skill viewer source')
  source = source.replace('const center = vec3.fromValues', hook+'\n  const center = vec3.fromValues')
  source = source.replace('frameRequest = 0\n    const state = camera.getCamera()', 'frameRequest = 0\n    if (qaCamera) { renderer.setCamera(qaCamera); renderer.drawStructure(); return }\n    const state = camera.getCamera()')
  await route.fulfill({response,body:source})
})
await page.goto(url,{waitUntil:'networkidle',timeout:180000})
await page.waitForSelector('#loading.hidden',{state:'attached',timeout:180000})
await page.addStyleTag({content:'.hud,.viewer-actions {display:none!important}'})
await fs.mkdir(path.join(root,'qa/views'),{recursive:true})
const views = []
for (const view of ['axis','reverse','north','south','west','east','sealed']) {
  const evidence = await page.evaluate(view=>window.renderView(view),view)
  await page.waitForTimeout(350)
  const output = view === 'axis' ? path.join(root,'preview.png') : path.join(root,'qa/views',view+'.png')
  await page.locator('#preview').screenshot({path:output})
  const image = await fs.readFile(output)
  views.push({...evidence,file:path.relative(root,output).replaceAll('\\','/'),sha256:crypto.createHash('sha256').update(image).digest('hex')})
  console.log('Captured '+view)
}
await browser.close()
const sourceHash = crypto.createHash('sha256').update(await fs.readFile(path.join(root,'room.nbt'))).digest('hex')
await fs.writeFile(path.join(root,'qa/render.json'),JSON.stringify({id:'corridor-M07',status:errors.length?'FAIL':'PASS',source_sha256:sourceHash,renderer:'Minecraft Builder included viewer / Lodestone 0.6.0 / headless Edge WebGL',client_gameplay:false,render_only_cutaway_does_not_modify_nbt:true,errors,views},null,2)+'\n')
if(errors.length) throw new Error(errors.join('; '))

