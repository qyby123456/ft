import fs from 'node:fs/promises'
import path from 'node:path'
import crypto from 'node:crypto'
import { createRequire } from 'node:module'
import { fileURLToPath } from 'node:url'
const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const runtimeRequire = createRequire('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/package.json')
const { chromium } = runtimeRequire('playwright')
const url = process.argv[2] ?? 'http://127.0.0.1:5573/?nbt=/__minecraft_builder__/build.nbt&name=zone-R03'
const hook = String.raw`
  let qaCamera = null
  const completeStructure = structure
  const viewCameras = {
    axis: { position: [12.5,4.8,1.8], target: [12.5,5.2,24], fov: 76 },
    reverse: { position: [12.5,4.8,24.7], target: [12.5,5.2,3], fov: 76 },
    overview: { position: [-15,25,-18], target: [12.5,5.5,13.5], fov: 48 },
    north: { position: [12.5,26,-30], target: [12.5,5.5,13.5], fov: 48 },
    east: { position: [56,26,13.5], target: [12.5,5.5,13.5], fov: 48 },
    south: { position: [12.5,26,57], target: [12.5,5.5,13.5], fov: 48 },
    west: { position: [-31,26,13.5], target: [12.5,5.5,13.5], fov: 48 },
  }
  window.renderView = async function(view) {
    let shown = completeStructure
    const cutaway = view.endsWith('-internal') || view === 'overview'
    const direction = view.replace(/-(internal|external)$/, '')
    if (cutaway) {
      shown = new Structure(size)
      for (const entry of completeStructure.getBlocks()) {
        const [blockX,blockY,blockZ] = entry.pos
        if (blockY >= 13) continue
        if (blockY >= 2 && ((['overview','west'].includes(direction) && blockX <= 1) || (direction === 'east' && blockX >= 23))) continue
        if (blockY >= 2 && ((['overview','north'].includes(direction) && blockZ <= 1) || (direction === 'south' && blockZ >= 25))) continue
        shown.addBlock(entry.pos,entry.state.getName(),entry.state.getProperties(),entry.nbt)
      }
    }
    renderer.setStructure(shown)
    await renderer.whenReady()
    const overview = !['axis','reverse'].includes(view)
    renderer.setSunlight({
      direction: vec3.fromValues(-0.35,0.85,-0.25),
      intensity: overview ? 1.02 : 0.4,
      ambientIntensity: overview ? 0.8 : 0.85,
      ambientColor: [0.83,0.86,0.78], fillIntensity: 0.5, rimIntensity: 0.25, exposure: 1.05,
      sky: {zenithColor:[0.04,0.055,0.06],horizonColor:[0.08,0.10,0.10],groundColor:[0.035,0.045,0.04],stars:{enabled:false}},
      fog:{color:[0.05,0.065,0.055],density:0.00001,heightFalloff:0.001},
      emissive:{range:15,intensity:1.3,tint:[1,0.85,0.56]},
    })
    qaCamera = viewCameras[direction]
    renderer.setCamera(qaCamera)
    renderer.drawStructure()
    window.__corridorView = {view,sourceCells:blockCount,shownCells:shown.getBlocks().length,cutaway,camera:qaCamera}
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
await fs.mkdir(path.join(root,'views'),{recursive:true})
const views = []
for (const view of ['overview','axis','reverse','north-internal','south-internal','west-internal','east-internal','north-external','south-external','west-external','east-external']) {
  const evidence = await page.evaluate(view=>window.renderView(view),view)
  await page.waitForTimeout(350)
  const output = path.join(root,'views',view+'.png')
  await page.locator('#preview').screenshot({path:output})
  const image = await fs.readFile(output)
  views.push({...evidence,file:path.relative(root,output).replaceAll('\\','/'),sha256:crypto.createHash('sha256').update(image).digest('hex')})
  console.log('Captured '+view)
}
await browser.close()
const sourceHash = crypto.createHash('sha256').update(await fs.readFile(path.join(root,'room.nbt'))).digest('hex')
await fs.writeFile(path.join(root,'qa/render.json'),JSON.stringify({id:'zone-R03',status:errors.length?'FAIL':'PASS',source_sha256:sourceHash,renderer:'Minecraft Builder included viewer / Lodestone 0.6.0 / headless Edge WebGL',client_gameplay:false,render_only_cutaway_does_not_modify_nbt:true,errors,views},null,2)+'\n')
if(errors.length) throw new Error(errors.join('; '))
