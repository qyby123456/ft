const fs=require('node:fs/promises')
const path=require('node:path')
const {spawn}=require('node:child_process')
const {createRequire}=require('node:module')
const {chromium}=createRequire('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/package.json')('playwright')
const skill='C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
const folder=path.resolve(process.argv[2])
const crypto=require('node:crypto')
let server,browser,log=''
async function main(){
 const design=JSON.parse(await fs.readFile(path.join(folder,'design.json'),'utf8'))
 const files=await fs.readdir(folder);const nbt=files.find(name=>name.endsWith('.nbt'))
 const bytes=await fs.readFile(path.join(folder,nbt));const sha256=crypto.createHash('sha256').update(bytes).digest('hex')
 server=spawn(process.execPath,[path.join(skill,'scripts/preview.mjs'),'--nbt',path.join(folder,nbt),'--port','0','--no-open'],{cwd:skill,windowsHide:true,stdio:['ignore','pipe','pipe']})
 const url=await new Promise((resolve,reject)=>{const timer=setTimeout(()=>reject(Error('preview timeout')),90000);server.stdout.on('data',data=>{log+=data;const match=log.match(/Minecraft build preview: (http[^\r\n]+)/);if(match){clearTimeout(timer);resolve(match[1])}});server.stderr.on('data',data=>log+=data);server.on('exit',code=>reject(Error('preview exit '+code)))})
 browser=await chromium.launch({executablePath:'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe',headless:true,args:['--no-sandbox','--use-angle=swiftshader','--enable-unsafe-swiftshader']})
 const page=await browser.newPage({viewport:{width:1400,height:1000}})
 await page.goto(url,{timeout:180000,waitUntil:'domcontentloaded'});await page.waitForSelector('#loading.hidden',{state:'attached',timeout:240000})
 await page.evaluate(async ({encoded})=>{
  const code=await(await fetch('/src/main.js')).text();const moduleUrl=code.match(/import\s*\{[^}]*ThreeStructureRenderer[^}]*\}\s*from\s*["']([^"']+)/)?.[1]
  const {NbtFile,Structure,ThreeStructureRenderer,loadDefaultPackResources}=await import(moduleUrl)
  const pack=await loadDefaultPackResources({baseUrl:new URL('/__minecraft_builder__/default-pack/',location.href).toString()})
  document.body.innerHTML='<canvas id="review"></canvas>';const canvas=document.getElementById('review');canvas.style.cssText='position:fixed;inset:0;width:100vw;height:100vh;background:#202b28'
  const source=Structure.fromNbt(NbtFile.read(Uint8Array.from(atob(encoded),symbol=>symbol.charCodeAt(0))).root)
  const renderer=new ThreeStructureRenderer(canvas,new Structure([1,1,1]),pack.resources,{preserveDrawingBuffer:true,antialias:true,asyncBuild:true,sunlight:{direction:[0.4,0.9,-0.25],intensity:0.85,ambientIntensity:0.75,exposure:1,sky:{zenithColor:[0.045,0.065,0.11],horizonColor:[0.09,0.12,0.17],groundColor:[0.025,0.035,0.055],stars:{enabled:false}}},fog:{density:0}})
  renderer.setViewport(0,0,1400,1000,1);await renderer.whenReady();window.review={source,renderer,Structure}
 },{encoded:bytes.toString('base64')})
 const [width,height,depth]=design.size;const extent=Math.max(width,depth)
 const views=[{id:'complete',position:[width/2+extent*1.1,height+extent*0.9,depth/2+extent*1.3],target:[width/2,height/2,depth/2],fov:43},{id:'cutaway',position:[width/2+extent*1.1,height+extent*0.9,depth/2+extent*1.3],target:[width/2,height/2-1,depth/2],fov:43}]
 for(const focus of design.focus_views??[])views.push({...focus,cut:true})
 const evidence=[]
 if(design.cutaway)design.cutaway.removeAbove=design.cutaway.removeAbove??height-3
 for(const view of views){
  const stats=await page.evaluate(async ({view,cutaway})=>{const {source,renderer,Structure}=window.review;let displayed=source;let removed=0;if(view.id==='cutaway'||view.cut){displayed=new Structure(source.getSize());for(const block of source.getBlocks()){const [x,y,z]=block.pos;if(y>=(cutaway?.removeAbove??10)||y>=2&&(z>=(cutaway?.frontDepth??source.getSize()[2]-2)||x>=(cutaway?.sideX??source.getSize()[0]-2))||(cutaway?.hiddenRanges??[]).some(range=>block.pos.every((coordinate,axis)=>coordinate>=range.from[axis]&&coordinate<=range.to[axis]))){removed++;continue}displayed.addBlock(block.pos,block.state.getName(),block.state.getProperties(),block.nbt)}}renderer.setStructure(displayed);await renderer.whenReady();renderer.setCamera(view);renderer.drawStructure();await new Promise(resolve=>requestAnimationFrame(()=>requestAnimationFrame(resolve)));renderer.drawStructure();return {source_records:source.getBlocks().length,display_records:displayed.getBlocks().length,removed_records:removed}}, {view,cutaway:design.cutaway})
  await page.locator('#review').screenshot({path:path.join(folder,view.id+'.png'),timeout:180000});evidence.push({...view,...stats});console.log('Rendered',design.id,view.id)
 }
 await fs.writeFile(path.join(folder,'render.json'),JSON.stringify({source_sha256:sha256,renderer:'Real Lodestone ThreeStructureRenderer WebGL',display_only_cutaway:true,views:evidence},null,2))
}
main().catch(error=>{console.error(error);process.exitCode=1}).finally(async()=>{if(browser)await browser.close();if(server)server.kill();await fs.writeFile(path.join(folder,'preview-server.log'),log)})
