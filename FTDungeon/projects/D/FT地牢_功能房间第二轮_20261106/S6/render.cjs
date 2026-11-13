const fs = require('node:fs/promises')
const path = require('node:path')
const {spawn} = require('node:child_process')
const {createHash} = require('node:crypto')
const {createRequire} = require('node:module')
const runtimeRequire = createRequire('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/package.json')
const {chromium} = runtimeRequire('playwright')
const skill = 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
const root = path.resolve(process.argv[2])
let server, browser, log = ''
async function main() {
  const summary = JSON.parse(await fs.readFile(path.join(root,'summary.json'),'utf8'))
  const bytes = await fs.readFile(summary.nbtPath)
  await fs.mkdir(path.join(root,'qa'),{recursive:true})
  server = spawn(process.execPath,[path.join(skill,'scripts/preview.mjs'),'--nbt',summary.nbtPath,'--port','0','--no-open'],{cwd:skill,windowsHide:true,stdio:['ignore','pipe','pipe']})
  const url = await new Promise((resolve,reject)=>{
    const timer=setTimeout(()=>reject(new Error('Preview timeout')),90000)
    server.stdout.on('data',bytes=>{log+=bytes.toString();const match=log.match(/Minecraft build preview: (http[^\r\n]+)/);if(match){clearTimeout(timer);resolve(match[1])}})
    server.stderr.on('data',bytes=>{log+=bytes.toString()})
  })
  browser=await chromium.launch({executablePath:'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe',headless:true,args:['--no-sandbox','--use-angle=swiftshader','--enable-unsafe-swiftshader']})
  const page=await browser.newPage({viewport:{width:1440,height:1000},deviceScaleFactor:1})
  await page.goto(url,{waitUntil:'domcontentloaded',timeout:180000})
  await page.waitForSelector('#loading.hidden',{state:'attached',timeout:240000})
  await page.evaluate(async encoded=>{
    const sourceCode=await(await fetch('/src/main.js')).text()
    const moduleUrl=sourceCode.match(/import\s*\{[^}]*ThreeStructureRenderer[^}]*\}\s*from\s*["']([^"']+)/)?.[1]
    const {NbtFile,Structure,ThreeStructureRenderer,loadDefaultPackResources}=await import(moduleUrl)
    const pack=await loadDefaultPackResources({baseUrl:new URL('/__minecraft_builder__/default-pack/',location.href).toString()})
    document.body.innerHTML='<canvas id="review"></canvas>'
    const canvas=document.getElementById('review')
    canvas.style.cssText='position:fixed;inset:0;width:100vw;height:100vh;background:#171a22'
    const source=Structure.fromNbt(NbtFile.read(Uint8Array.from(atob(encoded),symbol=>symbol.charCodeAt(0))).root)
    const renderer=new ThreeStructureRenderer(canvas,new Structure([1,1,1]),pack.resources,{preserveDrawingBuffer:true,antialias:true,asyncBuild:true,sunlight:{direction:[0.4,0.9,0.3],intensity:0.9,ambientIntensity:0.95,exposure:1.15},fog:{density:0}})
    renderer.setViewport(0,0,1440,1000,1)
    await renderer.whenReady()
    window.review={renderer,source,Structure}
  },bytes.toString('base64'))
  const [width,height,depth]=summary.size
  const span=Math.max(width,depth)
  const views=[{id:'cutaway',cut:true,position:[width/2+span,span*0.95,depth/2+span],target:[width/2,height*0.25,depth/2],fov:43},{id:'complete',cut:false,position:[width/2+span,span*0.95,depth/2+span],target:[width/2,height*0.35,depth/2],fov:43}]
  try { views.push(...JSON.parse(await fs.readFile(path.join(root,'views.json'),'utf8'))) } catch(error) { if(error.code!=='ENOENT') throw error }
  const evidence=[]
  for(const view of views) {
    const stats=await page.evaluate(async view=>{
      const {renderer,source,Structure}=window.review
      const displayed=new Structure(source.getSize())
      const [width,height,depth]=source.getSize()
      let removed=0
      for(const block of source.getBlocks()) {
        const [east,up,south]=block.pos
        if(view.cut&&(up>=height-3||((east===width-1||south===depth-1)&&up>=3))) {removed++;continue}
        displayed.addBlock(block.pos,block.state.getName(),block.state.getProperties(),block.nbt)
      }
      renderer.setStructure(displayed)
      await renderer.whenReady()
      renderer.setCamera({position:view.position,target:view.target,fov:view.fov})
      renderer.drawStructure()
      await new Promise(resolve=>requestAnimationFrame(()=>requestAnimationFrame(resolve)))
      renderer.drawStructure()
      return {source_records:source.getBlocks().length,removed_records:removed,displayed_records:displayed.getBlocks().length}
    },view)
    await page.locator('#review').screenshot({path:path.join(root,`${view.id}.png`),timeout:180000})
    evidence.push({...view,...stats})
    console.log('Rendered',view.id)
  }
  await fs.writeFile(path.join(root,'qa/render.json'),JSON.stringify({source_sha256:createHash('sha256').update(bytes).digest('hex'),renderer:'Real Lodestone ThreeStructureRenderer WebGL',views:evidence,cutaway:'Display copy only: top three layers plus east/south outer walls above floor removed. Source unmodified.'},null,2))
}
main().catch(error=>{console.error(error);process.exitCode=1}).finally(async()=>{if(browser)await browser.close();if(server)server.kill();await fs.writeFile(path.join(root,'qa/preview-server.log'),log)})
