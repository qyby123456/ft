const fs = require('node:fs/promises')
const path = require('node:path')
const {spawn} = require('node:child_process')
const {createRequire} = require('node:module')
const runtimeRequire = createRequire('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/package.json')
const {chromium} = runtimeRequire('playwright')
const skill = 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
const root = path.dirname(path.resolve(process.argv[2]))
const candidate = path.resolve(process.argv[2])
const server = spawn(process.execPath, [path.join(skill,'scripts/preview.mjs'),'--nbt',candidate,'--port','0','--no-open'], {cwd:skill,windowsHide:true,stdio:['ignore','pipe','pipe']})
let browser
let log = ''
async function main() {
  await fs.mkdir(path.join(root,'qa'),{recursive:true})
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
  const bytes = await fs.readFile(candidate)
  const summary = JSON.parse(await fs.readFile(path.join(root,'summary.json'),'utf8'))
  const assembly = candidate.includes('functions-connected') ? JSON.parse(await fs.readFile(path.join(root,'assembly.json'),'utf8')) : null
  const layout = {modules:assembly?.modules ?? [],sha256:require('node:crypto').createHash('sha256').update(bytes).digest('hex')}
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
  const [width,height,depth] = summary.size
  const views=[
    {id:'full',mode:'full',position:[width*1.6,height*2.3,depth*1.8],target:[width/2,height/2-2,depth/2],fov:43},
    {id:'cutaway',mode:'cutaway',position:[width*1.6,height*2.3,depth*1.8],target:[width/2,height/2-2,depth/2],fov:43},
    {id:'interior',mode:'full',position:[width/2,6.5,depth-4],target:[width/2,7,4],fov:85}
  ]
  if(assembly){
    for(const view of views.slice(0,2)){
      view.position=[330,230,250];view.target=[162,10,75];view.fov=48
    }
    views[2]={id:'interior',mode:'full',position:[29,8.5,37],target:[29,9,16],fov:85}
    views.push({id:'plan-cutaway',mode:'cutaway',position:[163,270,150],target:[163,8,75],fov:47})
  }
  if(candidate.includes('secret-vault'))views.push({id:'vault-detail',mode:'full',position:[25,5.3,14.5],target:[27,4.3,6],fov:85})
  if(candidate.includes('workshop'))views.push({id:'project-detail',mode:'full',position:[20,8,23],target:[15,5.5,16],fov:65})
  if(candidate.includes('prison-block'))views.push({id:'guard-detail',mode:'full',position:[29,5.5,20],target:[33,3.7,16],fov:70})
  if(candidate.includes('kitchen-mess'))views.push({id:'prep-detail',mode:'full',position:[17,7,18],target:[11,4.6,13],fov:70})
  if(candidate.includes('training-yard'))views.push({id:'dummy-detail',mode:'full',position:[18,6.5,18],target:[12,4.5,13],fov:75})
  if(candidate.includes('well-cistern'))views.push({id:'channel-detail',mode:'full',position:[27,9,30],target:[18,3.5,25],fov:70})
  const details={library:[[17,6,14],[3,6,7]],'prison-block':[[11,5,17],[11,4,6]],'kitchen-mess':[[15,7,13],[12,7,5]],armory:[[18,7,14],[18,6,6]],'training-yard':[[19,6,15],[19,6,3]],'well-cistern':[[18,10,27],[17,7,15]],workshop:[[12,6,13],[8,4,8]],'crypt-ossuary':[[16,7,13],[10,7,4]],'watch-balcony':[[17,12,19],[3,14,14]]}
  for(const [key,view]of Object.entries(details))if(candidate.includes(key))views.push({id:'feature-detail',mode:'full',position:view[0],target:view[1],fov:75})
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
          const size=source.getSize(); const hidden=modules.length ? module ? height>=module.origin[1]+module.size[1]-2 || (height>=module.origin[1]+3 && (east>=module.origin[0]+module.size[0]-4 || south>=module.origin[2]+module.size[2]-4)) : height>=12 && block.state.getName()!=='minecraft:barrier' : height>=size[1]-2 || (height>=3 && (east>=size[0]-4 || south>=size[2]-4)) || (options.vault && ((east>=19&&south<=12&&height>=10)||(south>=19&&south<=22&&height>=7)))
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
    },{...view,vault:candidate.includes('secret-vault')})
    await page.locator('#review').screenshot({path:path.join(root,`${view.id}.png`),timeout:180000})
    evidence.push({...view,...stats})
    console.log('Rendered',view.id)
  }
  await fs.writeFile(path.join(root,'qa/render.json'),JSON.stringify({source_sha256:layout.sha256,renderer:'Real Lodestone WebGL',views:evidence,cutaway_rule:'Display only: remove roof and east/south walls. No translation; source untouched.'},null,2))
}
main().catch(error=>{console.error(error);process.exitCode=1}).finally(async()=>{if(browser)await browser.close();server.kill();await fs.writeFile(path.join(root,'qa/preview-server.log'),log)})




