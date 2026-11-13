const fs = require('node:fs/promises')
const path = require('node:path')
const {spawn} = require('node:child_process')
const {createRequire} = require('node:module')
const runtimeRequire = createRequire('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/package.json')
const {chromium} = runtimeRequire('playwright')
const skill = 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
const root = __dirname
const server = spawn(process.execPath, [path.join(skill,'scripts/preview.mjs'),'--nbt',path.join(root,'mossy-forest-connected.nbt'),'--port','0','--no-open'], {cwd:skill,windowsHide:true,stdio:['ignore','pipe','pipe']})
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
  const bytes = await fs.readFile(path.join(root,'mossy-forest-connected.nbt'))
  const layout = JSON.parse(await fs.readFile(path.join(root,'layout.json'),'utf8'))
  await page.evaluate(async ({encoded,modules})=>{
    const sourceCode=await(await fetch('/src/main.js')).text()
    const moduleUrl=sourceCode.match(/import\s*\{[^}]*ThreeStructureRenderer[^}]*\}\s*from\s*["']([^"']+)/)?.[1]
    if(!moduleUrl)throw new Error('Installed renderer import unavailable')
    const {NbtFile,Structure,ThreeStructureRenderer,loadDefaultPackResources}=await import(moduleUrl)
    const pack=await loadDefaultPackResources({baseUrl:new URL('/__minecraft_builder__/default-pack/',location.href).toString()})
    document.body.innerHTML='<canvas id="review"></canvas>'
    const canvas=document.getElementById('review')
    canvas.style.cssText='position:fixed;inset:0;width:100vw;height:100vh;background:#17212b'
    const renderer=new ThreeStructureRenderer(canvas,new Structure([1,1,1]),pack.resources,{preserveDrawingBuffer:true,antialias:true,asyncBuild:true,sunlight:{direction:[0.4,0.9,-0.25],intensity:0.95,ambientIntensity:0.85,exposure:1.1,sky:{zenithColor:[0.045,0.065,0.11],horizonColor:[0.09,0.12,0.17],groundColor:[0.025,0.035,0.055],stars:{enabled:false}},fog:{density:0}}})
    renderer.setViewport(0,0,1600,1000,1)
    await renderer.whenReady()
    const source=Structure.fromNbt(NbtFile.read(Uint8Array.from(atob(encoded),symbol=>symbol.charCodeAt(0))).root)
    window.review={renderer,source,Structure,modules}
  },{encoded:bytes.toString('base64'),modules:layout.modules})
  const views=[
    {id:'overview-complete',mode:'full',position:[220,180,260],target:[72,5,87],fov:32},
    {id:'overview-cutaway',mode:'cutaway',position:[220,180,260],target:[72,5,87],fov:32},
    {id:'reverse-cutaway',mode:'cutaway',position:[-120,180,-120],target:[72,5,87],fov:32},
    {id:'entry',mode:'full',position:[75.5,3.62,171.5],target:[75.5,8,146],fov:80},
    {id:'main-hall',mode:'full',position:[75.5,5.62,120.5],target:[75,8,95],fov:85},
    {id:'garden-link',mode:'full',position:[100.5,5.62,120.5],target:[100.5,6,108],fov:85},
    {id:'garden',mode:'full',position:[113.5,5.62,125.5],target:[129,10,118],fov:85},
    {id:'treasury',mode:'full',position:[120.5,5.62,80.5],target:[120.5,8,66],fov:85}
  ]
  const evidence=[]
  for(const view of views){
    const stats=await page.evaluate(async options=>{
      const {renderer,source,Structure,modules}=window.review
      let displayed=source
      let removed=0
      if(options.mode==='cutaway'){
        displayed=new Structure(source.getSize())
        for(const block of source.getBlocks()){
          const [east,height,south]=block.pos
          const module=modules.find(module=>east>=module.origin[0]&&east<module.origin[0]+module.size[0]&&south>=module.origin[2]&&south<module.origin[2]+module.size[2])
          const hidden=module?height>=module.origin[1]+8:height>=8
          if(hidden){removed++;continue}
          displayed.addBlock(block.pos,block.state.getName(),block.state.getProperties(),block.nbt)
        }
      }
      renderer.setStructure(displayed)
      await renderer.whenReady()
      renderer.setCamera({position:options.position,target:options.target,fov:options.fov})
      renderer.drawStructure()
      await new Promise(resolve=>requestAnimationFrame(()=>requestAnimationFrame(resolve)))
      renderer.drawStructure()
      return {source_records:source.getBlocks().length,displayed_records:displayed.getBlocks().length,removed_records:removed}
    },view)
    await page.locator('#review').screenshot({path:path.join(root,`${view.id}.png`),timeout:180000})
    evidence.push({...view,...stats})
    console.log('Rendered',view.id)
  }
  await fs.writeFile(path.join(root,'qa/render.json'),JSON.stringify({source_sha256:layout.sha256,renderer:'Real Lodestone WebGL',views:evidence,cutaway_rule:'Display only: remove module cells at local Y>=8, outside-module cells at global Y>=8. No translation, source untouched. Full views do not cut.'},null,2))
}
main().catch(error=>{console.error(error);process.exitCode=1}).finally(async()=>{if(browser)await browser.close();server.kill();await fs.writeFile(path.join(root,'qa/preview-server.log'),log)})
