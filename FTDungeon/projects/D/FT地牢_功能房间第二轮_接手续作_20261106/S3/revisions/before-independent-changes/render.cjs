const fs=require('node:fs/promises')
const path=require('node:path')
const crypto=require('node:crypto')
const {spawn}=require('node:child_process')
const {createRequire}=require('node:module')
const runtime=createRequire('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/package.json')
const {chromium}=runtime('playwright')
const skill='C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
const id=process.argv[2],folder=path.join(__dirname,'rooms',id)
const server=spawn(process.execPath,[path.join(skill,'scripts/preview.mjs'),'--nbt',path.join(folder,id+'.nbt'),'--port','0','--no-open'],{cwd:skill,windowsHide:true,stdio:['ignore','pipe','pipe']})
let browser,log=''
async function main(){
  await fs.mkdir(path.join(folder,'qa'),{recursive:true})
  const url=await new Promise((resolve,reject)=>{const timer=setTimeout(()=>reject(Error('Preview timeout')),90000);server.stdout.on('data',bytes=>{log+=bytes;const match=log.match(/Minecraft build preview: (http[^\r\n]+)/);if(match){clearTimeout(timer);resolve(match[1])}});server.stderr.on('data',bytes=>log+=bytes);server.once('exit',code=>reject(Error('Preview exit '+code)))})
  browser=await chromium.launch({executablePath:'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe',headless:true,args:['--no-sandbox','--use-angle=swiftshader','--enable-unsafe-swiftshader']})
  const page=await browser.newPage({viewport:{width:1440,height:1000}})
  await page.goto(url,{waitUntil:'domcontentloaded',timeout:180000})
  await page.waitForSelector('#loading.hidden',{state:'attached',timeout:240000})
  const bytes=await fs.readFile(path.join(folder,id+'.nbt'))
  const design=JSON.parse(await fs.readFile(path.join(folder,'design.json'),'utf8'))
  await page.evaluate(async ({encoded,modules})=>{
    const sourceCode=await(await fetch('/src/main.js')).text()
    const moduleUrl=sourceCode.match(/import\s*\{[^}]*ThreeStructureRenderer[^}]*\}\s*from\s*["']([^"']+)/)?.[1]
    const {NbtFile,Structure,ThreeStructureRenderer,loadDefaultPackResources}=await import(moduleUrl)
    const pack=await loadDefaultPackResources({baseUrl:new URL('/__minecraft_builder__/default-pack/',location.href).toString()})
    document.body.innerHTML='<canvas id="review"></canvas>'
    const canvas=document.getElementById('review');canvas.style.cssText='position:fixed;inset:0;width:100vw;height:100vh;background:#172333'
    const renderer=new ThreeStructureRenderer(canvas,new Structure([1,1,1]),pack.resources,{preserveDrawingBuffer:true,antialias:true,asyncBuild:true,sunlight:{direction:[-0.4,0.9,-0.3],intensity:0.85,ambientIntensity:0.85,exposure:1.05,sky:{zenithColor:[0.045,0.065,0.11],horizonColor:[0.09,0.12,0.17],groundColor:[0.025,0.035,0.055],stars:{enabled:false}}},fog:{density:0}})
    renderer.setViewport(0,0,1440,1000,1)
    await renderer.whenReady()
    const source=Structure.fromNbt(NbtFile.read(Uint8Array.from(atob(encoded),symbol=>symbol.charCodeAt(0))).root)
    window.review={renderer,source,Structure,modules}
  },{encoded:bytes.toString('base64'),modules:design.modules})
  const [width,height,depth]=design.size,scale=Math.max(width,depth)
  const views=[{id:'complete',cut:false,position:[width/2-scale*1.2,height+scale,depth/2-scale*1.5],target:[width/2,height/2,depth/2],fov:43},{id:'cutaway',cut:true,position:[width/2-scale*1.05,height+scale*.8,depth/2-scale*1.25],target:[width/2,3,depth/2],fov:43}]
  const closeViews={
    's3-11':[{id:'archive-detail',position:[14,9,13],target:[16,6,23],fov:78}],
    's3-12':[{id:'cells-detail',position:[14,6,12],target:[10,4,23],fov:85}],
    's3-13':[{id:'hearth-detail',position:[12,8,16],target:[9,6,26],fov:73},{id:'pantry-detail',position:[25,7,20],target:[25,5,27],fov:75}],
    's3-14':[{id:'armor-detail',position:[11,6,12],target:[7,5,18],fov:70}],
    's3-15':[{id:'training-detail',position:[16,6,14],target:[17,4,24],fov:82}],
    's3-16':[{id:'bucket-detail',position:[20,9,7],target:[14,7,14],fov:72}],
    's3-17':[{id:'tools-detail',position:[10,5,15],target:[6,3,20],fov:77}],
    's3-18':[{id:'ossuary-detail',position:[15,7,18],target:[15,5,25],fov:83}],
    's3-19':[{id:'watch-detail',position:[21,13,23],target:[15,10,18],fov:78}],
    's3-20':[{id:'vault-detail',position:[22,7,14],target:[22,5,21],fov:75}]
  }
  for(const view of closeViews[id]??[])views.push({...view,cut:false})
  if(id==='s3-merged'){
    views.push({id:'connection-detail',cut:false,position:[57,4.62,32],target:[68,5,32],fov:84})
    views[0].fov=26
    views[1].fov=26
    views.push({id:'raised-vault-route',cut:true,position:[62,52,188],target:[61,9,166],fov:62})
  }
  const evidence=[]
  for(const view of views){
    const stats=await page.evaluate(async ({view,cutY})=>{
      const {renderer,source,Structure}=window.review
      let displayed=source,removed=0
      if(view.cut){displayed=new Structure(source.getSize());for(const block of source.getBlocks()){const [east,up,south]=block.pos;const module=window.review.modules?.find(module=>east>=module.origin[0]&&east<module.origin[0]+module.size[0]&&south>=module.origin[2]&&south<module.origin[2]+module.size[2]);const mergedCut=window.review.modules?(module?up>=module.origin[1]+module.size[1]-2||(up>=module.origin[1]+2&&(east<=module.origin[0]+1||south<=module.origin[2]+1)):up>=(south>=130?13:7)):false;if(block.state.getName().toString()==='minecraft:barrier'||mergedCut||(!window.review.modules&&(up>=cutY||(up>=2&&(east<=1||south<=1))))){removed++;continue}displayed.addBlock(block.pos,block.state.getName(),block.state.getProperties(),block.nbt)}}
      renderer.setStructure(displayed);await renderer.whenReady();renderer.setCamera({position:view.position,target:view.target,fov:view.fov});renderer.drawStructure();await new Promise(resolve=>requestAnimationFrame(()=>requestAnimationFrame(resolve)));renderer.drawStructure()
      return {source_records:source.getBlocks().length,display_records:displayed.getBlocks().length,removed_records:removed}
    },{view,cutY:design.cutY})
    await page.locator('#review').screenshot({path:path.join(folder,view.id+'.png'),timeout:180000})
    evidence.push({...view,...stats});console.log('Rendered',id,view.id)
  }
  await fs.writeFile(path.join(folder,'qa/render.json'),JSON.stringify({sha256:crypto.createHash('sha256').update(bytes).digest('hex'),renderer:'Real Lodestone WebGL',views:evidence,cutaway:'Display copy only. Remove Y>=cutY and front/left walls above floor. Source NBT untouched.'},null,2))
}
main().catch(error=>{console.error(error);process.exitCode=1}).finally(async()=>{if(browser)await browser.close();server.kill();await fs.writeFile(path.join(folder,'qa/preview-server.log'),log)})
