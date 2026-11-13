const fs=require('node:fs/promises')
const path=require('node:path')
const {spawn}=require('node:child_process')
const {createRequire}=require('node:module')
const {chromium}=createRequire('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/package.json')('playwright')
const root=__dirname
const id=process.argv[2]
const folder=path.join(root,id)
const skill='C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
const server=spawn(process.execPath,[path.join(skill,'scripts/preview.mjs'),'--nbt',path.join(folder,id.toLowerCase()+'.nbt'),'--port','0','--no-open'],{cwd:skill,windowsHide:true,stdio:['ignore','pipe','pipe']})
let browser,log=''
async function main(){
  await fs.mkdir(path.join(folder,'qa'),{recursive:true})
  const url=await new Promise((resolve,reject)=>{
    const timer=setTimeout(()=>reject(Error('Preview startup timeout')),90000)
    server.stdout.on('data',bytes=>{log+=bytes;const found=log.match(/Minecraft build preview: (http[^\r\n]+)/);if(found){clearTimeout(timer);resolve(found[1])}})
    server.stderr.on('data',bytes=>{log+=bytes})
    server.once('exit',code=>reject(Error('Preview exited '+code)))
  })
  browser=await chromium.launch({executablePath:'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe',headless:true,args:['--no-sandbox','--use-angle=swiftshader','--enable-unsafe-swiftshader']})
  const page=await browser.newPage({viewport:{width:1500,height:1000}})
  await page.goto(url,{waitUntil:'domcontentloaded',timeout:180000})
  await page.waitForSelector('#loading.hidden',{state:'attached',timeout:240000})
  const bytes=await fs.readFile(path.join(folder,id.toLowerCase()+'.nbt'))
  const design=JSON.parse(await fs.readFile(path.join(folder,'design.json'),'utf8'))
  await page.evaluate(async encoded=>{
    const code=await(await fetch('/src/main.js')).text()
    const moduleUrl=code.match(/import\s*\{[^}]*ThreeStructureRenderer[^}]*\}\s*from\s*["']([^"']+)/)[1]
    const {NbtFile,Structure,ThreeStructureRenderer,loadDefaultPackResources}=await import(moduleUrl)
    const pack=await loadDefaultPackResources({baseUrl:new URL('/__minecraft_builder__/default-pack/',location.href).toString()})
    document.body.innerHTML='<canvas id="review"></canvas>'
    const canvas=document.getElementById('review');canvas.style.cssText='position:fixed;inset:0;width:100vw;height:100vh;background:#16333b'
    const source=Structure.fromNbt(NbtFile.read(Uint8Array.from(atob(encoded),symbol=>symbol.charCodeAt(0))).root)
    const renderer=new ThreeStructureRenderer(canvas,source,pack.resources,{preserveDrawingBuffer:true,antialias:true,asyncBuild:true,sunlight:{direction:[-0.6,0.9,0.5],intensity:1,ambientIntensity:0.85,exposure:1.1,sky:{zenithColor:[0.04,0.12,0.16],horizonColor:[0.1,0.25,0.3],groundColor:[0.03,0.08,0.1],stars:{enabled:false}},fog:{density:0}}})
    renderer.setViewport(0,0,1500,1000,1);await renderer.whenReady();window.review={source,renderer,Structure}
  },bytes.toString('base64'))
  const views=[]
  for(const mode of ['complete','cutaway']){
    const stats=await page.evaluate(async({mode,size,cutHeight,modules})=>{
      const {source,renderer,Structure}=window.review
      let shown=source,removed=0
      if(mode==='cutaway'){
        shown=new Structure(source.getSize())
        for(const block of source.getBlocks()){
          const [east,height,south]=block.pos
          const module=modules?.find(module=>block.pos.every((value,axis)=>value>=module.origin[axis]&&value<module.origin[axis]+module.size[axis]))
          const hidden=modules?(module?height>=module.origin[1]+Math.min(module.cutHeight,9):height>=12):(height>=cutHeight||(height>=3&&(east<=0||south>=size[2]-1)))
          if(hidden){removed++;continue}
          shown.addBlock(block.pos,block.state.getName(),block.state.getProperties(),block.nbt)
        }
      }
      renderer.setStructure(shown);await renderer.whenReady()
      const maximum=Math.max(size[0],size[2])
      renderer.setCamera({position:modules?[size[0]/2,maximum*0.82,size[2]+maximum*0.65]:[-maximum*1.25,maximum*1.2,maximum*1.7],target:[size[0]/2,size[1]/3,size[2]/2],fov:38})
      renderer.drawStructure();await new Promise(resolve=>requestAnimationFrame(()=>requestAnimationFrame(resolve)));renderer.drawStructure()
      return {source_records:source.getBlocks().length,displayed_records:shown.getBlocks().length,removed_records:removed}
    },{mode,size:design.size,cutHeight:design.cutHeight??10,modules:design.modules})
    await page.locator('#review').screenshot({path:path.join(folder,mode+'.png'),timeout:180000})
    views.push({mode,...stats})
  }
  if(design.id==='S5-20'||design.id==='S5-12'||design.modules){
    const closeups=design.id==='S5-20'?[{label:'vault-interior',position:[32,5.2,15],target:[33,5.2,6]}]:design.id==='S5-12'?[{label:'central-aisle',position:[13,5,29],target:[13,5,4]}]:[{label:'complex-aisle',position:[12,10.7,23],target:[60,11,23]}]
    for(const view of closeups){
      await page.evaluate(async view=>{const {renderer,source}=window.review;renderer.setStructure(source);await renderer.whenReady();renderer.setCamera({...view,fov:80});renderer.drawStructure();await new Promise(resolve=>requestAnimationFrame(()=>requestAnimationFrame(resolve)));renderer.drawStructure()},view)
      await page.locator('#review').screenshot({path:path.join(folder,view.label+'.png'),timeout:180000});views.push({...view,mode:'complete-interior'})
    }
  }
  const sha256=require('node:crypto').createHash('sha256').update(bytes).digest('hex')
  await fs.writeFile(path.join(folder,'qa/render.json'),JSON.stringify({sha256,renderer:'Actual Lodestone ThreeStructureRenderer WebGL',views,cutaway:'Display-only removal: y>=cutHeight or (y>=3 and (x=0 or z=max)); source NBT unchanged.'},null,2))
  console.log(id,views)
}
main().catch(error=>{console.error(error);process.exitCode=1}).finally(async()=>{if(browser)await browser.close();server.kill();await fs.writeFile(path.join(folder,'qa/preview.log'),log)})
