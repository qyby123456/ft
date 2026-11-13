const fs=require('node:fs/promises')
const path=require('node:path')
const crypto=require('node:crypto')
const {spawn}=require('node:child_process')
const {createRequire}=require('node:module')
const {chromium}=createRequire('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/package.json')('playwright')
const skill='C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
const folder=path.resolve(process.argv[2])
let browser,server,log=''
async function main(){
  const specification=JSON.parse(await fs.readFile(path.join(folder,'design.json'),'utf8'))
  const sourcePath=path.join(folder,specification.nbt)
  const bytes=await fs.readFile(sourcePath)
  server=spawn(process.execPath,[path.join(skill,'scripts/preview.mjs'),'--nbt',sourcePath,'--port','0','--no-open'],{cwd:skill,windowsHide:true,stdio:['ignore','pipe','pipe']})
  const url=await new Promise((resolve,reject)=>{const timer=setTimeout(()=>reject(Error('preview timeout')),90000);server.stdout.on('data',bytes=>{log+=bytes;const match=log.match(/Minecraft build preview: (http[^\r\n]+)/);if(match){clearTimeout(timer);resolve(match[1])}});server.stderr.on('data',bytes=>{log+=bytes});server.once('exit',code=>reject(Error(`preview exit ${code}`)))})
  browser=await chromium.launch({executablePath:'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe',headless:true,args:['--no-sandbox','--use-angle=swiftshader','--enable-unsafe-swiftshader']})
  const page=await browser.newPage({viewport:{width:1500,height:1050},deviceScaleFactor:1})
  await page.goto(url,{waitUntil:'domcontentloaded',timeout:180000});await page.waitForSelector('#loading.hidden',{state:'attached',timeout:240000})
  await page.evaluate(async encoded=>{
    const code=await(await fetch('/src/main.js')).text();const moduleUrl=code.match(/import\s*\{[^}]*ThreeStructureRenderer[^}]*\}\s*from\s*["']([^"']+)/)?.[1]
    const {NbtFile,Structure,ThreeStructureRenderer,loadDefaultPackResources}=await import(moduleUrl)
    const pack=await loadDefaultPackResources({baseUrl:new URL('/__minecraft_builder__/default-pack/',location.href).toString()})
    document.body.innerHTML='<canvas id="review"></canvas>';const canvas=document.getElementById('review');canvas.style.cssText='position:fixed;inset:0;width:100vw;height:100vh;background:#231e18'
    const loaded=Structure.fromNbt(NbtFile.read(Uint8Array.from(atob(encoded),symbol=>symbol.charCodeAt(0))).root)
    const source=new Structure(loaded.getSize());for(const block of loaded.getBlocks()) if(!['minecraft:air','minecraft:barrier'].includes(block.state.getName().toString())) source.addBlock(block.pos,block.state.getName(),block.state.getProperties(),block.nbt)
    const renderer=new ThreeStructureRenderer(canvas,new Structure([1,1,1]),pack.resources,{preserveDrawingBuffer:true,antialias:true,asyncBuild:true,sunlight:{direction:[-0.4,0.9,0.3],intensity:0.75,ambientIntensity:0.65,exposure:0.8,sky:{zenithColor:[0.045,0.04,0.035],horizonColor:[0.09,0.08,0.06],groundColor:[0.025,0.02,0.015],stars:{enabled:false}}},fog:{density:0}})
    renderer.setViewport(0,0,1500,1050,1);await renderer.whenReady();window.review={source,renderer,Structure}
  },bytes.toString('base64'))
  if(specification.id==='S2-19') specification.views.push({id:'preview-stair',position:[23.5,3.62,14.5],target:[23.5,6,7],fov:85})
  if(specification.id==='S2-11') specification.views.push({id:'preview-archive-details',position:[19.5,6.62,15.5],target:[21,6,5],fov:88})
  if(specification.id==='S2-11') specification.views.push({id:'preview-ladder',position:[16.5,5.62,11.5],target:[22.1,5,11.5],fov:72},{id:'preview-banner',position:[12.5,9.62,7.5],target:[12.5,9,1],fov:70},{id:'preview-door-arch',position:[17.5,5.62,18.5],target:[24,6,18.5],fov:78})
  if(specification.id==='S2-13') specification.views.push({id:'preview-cauldron',position:[10.5,7.62,9.5],target:[10,5,3],fov:80})
  if(specification.id==='S2-17') specification.views.push({id:'preview-workbench',position:[9.5,6.62,17.5],target:[13,4,12],fov:85})
  if(specification.id==='S2-19') specification.views.push({id:'preview-support-arches',position:[12.5,3.62,25.5],target:[10,5,18],fov:95})
  if(specification.id==='S2-20') specification.views.push({id:'preview-vault-focus',position:[9.5,6.62,18.5],target:[15,4,7],fov:85})
  await page.evaluate(specification=>{window.review.specification=specification},specification)
  const evidence=[]
  if(specification.modules) await page.evaluate(()=>{const specification=window.review.specification;specification.corridorFootprints=specification.corridors.map(corridor=>{const cells=[];for(let step=1;step<corridor.waypoints.length;step++){const first=corridor.waypoints[step-1],last=corridor.waypoints[step];const distance=Math.abs(last[0]-first[0])+Math.abs(last[2]-first[2]);for(let offset=0;offset<=distance;offset++)cells.push([first[0]+(last[0]-first[0])*offset/distance,first[2]+(last[2]-first[2])*offset/distance])}return {...corridor,cells}})})
  for(const view of specification.views){
    const stats=await page.evaluate(async view=>{const {source,renderer,Structure,specification}=window.review;let displayed=source,removed=0;if(view.cutaway){displayed=new Structure(source.getSize());for(const block of source.getBlocks()){const [east,up,south]=block.pos;const module=specification.modules?.find(module=>east>=module.origin[0]&&east<module.origin[0]+module.size[0]&&south>=module.origin[2]&&south<module.origin[2]+module.size[2]);const hidden=specification.modules?(module?up>=module.origin[1]+module.size[1]-3||up>=module.origin[1]+2&&(east===module.origin[0]||south===module.origin[2]+module.size[2]-1):specification.corridorFootprints.some(corridor=>corridor.cells.some(cell=>Math.abs(east-cell[0])<=2&&Math.abs(south-cell[1])<=2)&&up>=corridor.feet+4)):up>=view.cutY || up>=2&&(south>=source.getSize()[2]-1||east===0);if(hidden){removed++;continue}displayed.addBlock(block.pos,block.state.getName(),block.state.getProperties(),block.nbt)}}renderer.setStructure(displayed);await renderer.whenReady();renderer.setCamera({position:view.position,target:view.target,fov:view.fov||42});renderer.drawStructure();await new Promise(resolve=>setTimeout(resolve,1200));renderer.drawStructure();return {source_records:source.getBlocks().length,displayed_records:displayed.getBlocks().length,removed_display_only:removed}},view)
    await page.screenshot({path:path.join(folder,view.id+'.png')});evidence.push({...view,...stats})
  }
  await fs.writeFile(path.join(folder,'qa','render.json'),JSON.stringify({source_sha256:crypto.createHash('sha256').update(bytes).digest('hex'),renderer:'Lodestone ThreeStructureRenderer / default resource pack',source_unchanged:bytes.equals(await fs.readFile(sourcePath)),views:evidence},null,2))
}
main().catch(error=>{console.error(error);process.exitCode=1}).finally(async()=>{if(browser)await browser.close();if(server)server.kill();await fs.writeFile(path.join(folder,'qa','preview-server.log'),log)})
