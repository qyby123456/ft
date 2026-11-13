const fs = require('node:fs/promises')
const path = require('node:path')
const {spawn} = require('node:child_process')
const {createRequire} = require('node:module')
const {createHash} = require('node:crypto')
const runtimeRequire = createRequire('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/package.json')
const {chromium} = runtimeRequire('playwright')
const skill = 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
const root = __dirname
const server = spawn(process.execPath,[path.join(skill,'scripts/preview.mjs'),'--nbt',path.join(root,'preview-sources/geometry-water-hidden.nbt'),'--port','0','--no-open'],{cwd:skill,windowsHide:true,stdio:['ignore','pipe','pipe']})
let browser
let log = ''
async function main() {
  const url = await new Promise((resolve,reject)=>{
    const timer = setTimeout(()=>reject(new Error('Preview startup timeout')),90000)
    server.stdout.on('data',bytes=>{log+=bytes.toString();const match=log.match(/Minecraft build preview: (http[^\r\n]+)/);if(match){clearTimeout(timer);resolve(match[1])}})
    server.stderr.on('data',bytes=>{log+=bytes.toString()})
    server.once('exit',code=>reject(new Error(`Preview exited ${code}`)))
  })
  browser = await chromium.launch({executablePath:'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe',headless:true,args:['--no-sandbox','--use-angle=swiftshader','--enable-unsafe-swiftshader']})
  const page = await browser.newPage({viewport:{width:1600,height:1000},deviceScaleFactor:1})
  await page.goto(url,{waitUntil:'domcontentloaded',timeout:180000})
  await page.waitForSelector('#loading.hidden',{state:'attached',timeout:240000})
  await page.evaluate(async ()=>{
    const sourceCode=await(await fetch('/src/main.js')).text()
    const moduleUrl=sourceCode.match(/import\s*\{[^}]*ThreeStructureRenderer[^}]*\}\s*from\s*["']([^"']+)/)?.[1]
    if(!moduleUrl)throw new Error('Installed renderer import unavailable')
    const {NbtFile,Structure,ThreeStructureRenderer,loadDefaultPackResources}=await import(moduleUrl)
    const pack=await loadDefaultPackResources({baseUrl:new URL('/__minecraft_builder__/default-pack/',location.href).toString()})
    document.body.innerHTML='<canvas id="review"></canvas><div id="label"></div>'
    const canvas=document.getElementById('review')
    canvas.style.cssText='position:fixed;inset:0;width:100vw;height:100vh;background:#102331'
    document.getElementById('label').style.cssText='position:fixed;top:20px;left:24px;z-index:99;color:white;background:#071a2cdd;padding:12px 18px;font:18px sans-serif'
    const renderer=new ThreeStructureRenderer(canvas,new Structure([1,1,1]),pack.resources,{preserveDrawingBuffer:true,antialias:true,asyncBuild:true,sunlight:{direction:[0.4,0.9,-0.25],intensity:0.95,ambientIntensity:0.85,exposure:1.1,sky:{zenithColor:[0.045,0.065,0.11],horizonColor:[0.09,0.12,0.17],groundColor:[0.025,0.035,0.055],stars:{enabled:false}},fog:{density:0}}})
    renderer.setViewport(0,0,1600,1000,1)
    await renderer.whenReady()
    window.review={renderer,NbtFile,Structure}
  })
  const manifest=JSON.parse(await fs.readFile(path.join(root,'qa/preview-sources.json'),'utf8'))
  const views=[
    {id:'overview-geometry-water-hidden',source:'geometry-water-hidden',position:[420,330,430],target:[131,5,125],fov:39,label:'S5 | GEOMETRY ONLY — WATER / TANK HIDDEN'},
    {id:'overview-cutaway-water-hidden',source:'cutaway-water-hidden',position:[420,330,430],target:[131,5,125],fov:39,label:'S5 | CUTAWAY — WATER / TANK / UPPER WALLS HIDDEN'},
    {id:'entry-actual-water',source:'actual-water-07',position:[107.5,5.62,237.5],target:[107.5,12,214],fov:85,label:'S5 | ENTRY — ACTUAL SOURCE WATER, EXACT CROP'},
    {id:'chamber-actual-water',source:'actual-water-01',position:[107.5,5.62,22.5],target:[107.5,11,43],fov:85,label:'S5 | MAIN CHAMBER — ACTUAL SOURCE WATER, EXACT CROP'},
    {id:'upper-gallery-actual-water',source:'actual-water-08',position:[163.5,15.62,159.5],target:[187,18,159.5],fov:85,label:'S5 | UPPER SWIM GALLERY — ACTUAL SOURCE WATER, EXACT CROP'},
    {id:'vault-actual-water',source:'actual-water-09',position:[223.5,5.62,160.5],target:[240,11,157],fov:85,label:'S5 | TRIDENT VAULT — ACTUAL SOURCE WATER, EXACT CROP'}
  ]
  const evidence=[]
  for(const view of views){
    const source=manifest.sources.find(entry=>entry.id===view.source)
    const bytes=await fs.readFile(source.file)
    if(createHash('sha256').update(bytes).digest('hex')!==source.sha256)throw new Error('Preview hash mismatch')
    await page.evaluate(async ({encoded,view,origin})=>{
      const {renderer,NbtFile,Structure}=window.review
      const structure=Structure.fromNbt(NbtFile.read(Uint8Array.from(atob(encoded),symbol=>symbol.charCodeAt(0))).root)
      renderer.setStructure(structure)
      await renderer.whenReady()
      renderer.setCamera({position:view.position.map((value,axis)=>value-origin[axis]),target:view.target.map((value,axis)=>value-origin[axis]),fov:view.fov})
      document.getElementById('label').textContent=view.label
      renderer.drawStructure()
      await new Promise(resolve=>requestAnimationFrame(()=>requestAnimationFrame(resolve)))
      renderer.drawStructure()
    },{encoded:bytes.toString('base64'),view,origin:source.origin})
    await page.screenshot({path:path.join(root,`${view.id}.png`),timeout:180000})
    evidence.push({...view,preview_sha256:source.sha256,preview_origin:source.origin,display_only:source.display_only})
    console.log('Rendered',view.id)
  }
  await fs.writeFile(path.join(root,'qa/render.json'),JSON.stringify({source_sha256:manifest.source_sha256,renderer:'Real Lodestone WebGL',views:evidence,geometry_rule:manifest.geometry_rule},null,2))
}
main().catch(error=>{console.error(error);process.exitCode=1}).finally(async()=>{if(browser)await browser.close();server.kill();await fs.writeFile(path.join(root,'qa/preview-server.log'),log)})
