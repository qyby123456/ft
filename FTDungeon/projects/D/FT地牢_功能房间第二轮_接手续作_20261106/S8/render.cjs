const fs=require('node:fs/promises'),path=require('node:path'),{spawn}=require('node:child_process'),{createRequire}=require('node:module')
const {chromium}=createRequire('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/package.json')('playwright')
const skill='C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill',root=path.resolve(process.argv[2])
let browser,server,log=''
async function main(){
 const filename=(await fs.readdir(root)).find(name=>name.endsWith('.nbt'))
 server=spawn(process.execPath,[path.join(skill,'scripts/preview.mjs'),'--nbt',path.join(root,filename),'--port','0','--no-open'],{cwd:skill,windowsHide:true,stdio:['ignore','pipe','pipe']})
 const url=await new Promise((resolve,reject)=>{const timer=setTimeout(()=>reject(Error('Preview timeout')),90000);server.stdout.on('data',data=>{log+=data;const match=log.match(/Minecraft build preview: (http[^\r\n]+)/);if(match){clearTimeout(timer);resolve(match[1])}});server.stderr.on('data',data=>log+=data);server.on('exit',code=>reject(Error(`preview ${code}`)))})
 browser=await chromium.launch({executablePath:'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe',headless:true,args:['--no-sandbox','--use-angle=swiftshader','--enable-unsafe-swiftshader']})
 const page=await browser.newPage({viewport:{width:1500,height:1000}})
 await page.goto(url,{waitUntil:'domcontentloaded',timeout:180000});await page.waitForSelector('#loading.hidden',{state:'attached',timeout:240000})
 const bytes=await fs.readFile(path.join(root,filename))
 const layout=path.basename(root)==='assembly'?JSON.parse(await fs.readFile(path.join(root,'layout.json'),'utf8')):null
 const result=await page.evaluate(async ({encoded,modules})=>{
  const code=await(await fetch('/src/main.js')).text(),moduleUrl=code.match(/import\s*\{[^}]*ThreeStructureRenderer[^}]*\}\s*from\s*["']([^"']+)/)[1]
  const {NbtFile,Structure,ThreeStructureRenderer,loadDefaultPackResources}=await import(moduleUrl)
  const pack=await loadDefaultPackResources({baseUrl:new URL('/__minecraft_builder__/default-pack/',location.href).toString()})
  const source=Structure.fromNbt(NbtFile.read(Uint8Array.from(atob(encoded),letter=>letter.charCodeAt(0))).root)
  const size=source.getSize();document.body.innerHTML='<canvas id="review"></canvas>';const canvas=document.querySelector('canvas');canvas.style.cssText='width:100vw;height:100vh;position:fixed;inset:0;background:#151323'
  const renderer=new ThreeStructureRenderer(canvas,source,pack.resources,{preserveDrawingBuffer:true,antialias:true,asyncBuild:true,sunlight:{direction:[0.4,0.9,0.25],intensity:1,ambientIntensity:0.85,exposure:1.2}})
  renderer.setViewport(0,0,1500,1000,1);await renderer.whenReady();window.review={renderer,source,Structure,size,modules};return {size,source_records:source.getBlocks().length}
 },{encoded:bytes.toString('base64'),modules:layout?.modules??null})
 const views=[]
 const custom=process.argv[3]?JSON.parse(await fs.readFile(process.argv[3],'utf8')):{}
 for(const mode of [...['complete','cutaway','interior','reverse'],...Object.keys(custom)]){
 const stats=await page.evaluate(async ({mode,custom})=>{
  const {renderer,source,Structure,size,modules}=window.review;let displayed=source
  if(mode==='cutaway'){displayed=new Structure(size);for(const block of source.getBlocks()){const [x,y,z]=block.pos;if(y>=size[1]-4||((x>=size[0]-2||z>=size[2]-2)&&y>2))continue;displayed.addBlock(block.pos,block.state.getName(),block.state.getProperties(),block.nbt)}}
  if(mode==='cutaway'&&modules){displayed=new Structure(size);for(const block of source.getBlocks()){
   const [x,y,z]=block.pos;if(block.state.getName().toString()==='minecraft:barrier')continue
   const module=modules.find(module=>x>=module.origin[0]&&x<module.origin[0]+module.size[0]&&z>=module.origin[2]&&z<module.origin[2]+module.size[2])
   const hidden=module?(y>=module.origin[1]+module.size[1]-4||((x>=module.origin[0]+module.size[0]-2||z>=module.origin[2]+module.size[2]-2)&&y>module.origin[1]+2)):y>=8
   if(!hidden)displayed.addBlock(block.pos,block.state.getName(),block.state.getProperties(),block.nbt)
  }}
  renderer.setStructure(displayed);await renderer.whenReady()
  const span=Math.max(size[0],size[2]),camera=mode==='interior'?{position:[size[0]/2,4.5,size[2]-3],target:[size[0]/2,5,3],fov:87}:{position:[size[0]/2+span*1.25,span*1.12,size[2]/2+span*1.35],target:[size[0]/2,size[1]/3,size[2]/2],fov:43}
  if(mode==='reverse')Object.assign(camera,{position:[size[0]/2,5,8],target:[size[0]/2,4,size[2]-4],fov:105})
  if(modules&&['complete','cutaway'].includes(mode))Object.assign(camera,{position:[size[0]/2+span*0.6,span*0.6,size[2]/2+span*0.7],target:[size[0]/2,8,size[2]/2],fov:43})
  if(modules&&mode==='interior')Object.assign(camera,{position:[7,5.62,57],target:[70,5.62,55],fov:85})
  if(modules&&mode==='reverse')Object.assign(camera,{position:[199,8.5,95],target:[190,10,84],fov:95})
  if(custom[mode])Object.assign(camera,custom[mode])
  renderer.setCamera(camera);renderer.drawStructure();await new Promise(resolve=>requestAnimationFrame(()=>requestAnimationFrame(resolve)));renderer.drawStructure();return {displayed_records:displayed.getBlocks().length,camera}
 },{mode,custom})
 await page.locator('#review').screenshot({path:path.join(root,`${mode}.png`),timeout:180000});views.push({mode,...stats})
 }
 const crypto=require('node:crypto');await fs.writeFile(path.join(root,'render-evidence.json'),JSON.stringify({sha256:crypto.createHash('sha256').update(bytes).digest('hex'),renderer:'Lodestone ThreeStructureRenderer WebGL on Edge SwiftShader',...result,views,cutaway:'Display copy only; remove roof Y>=height-4 and near two walls above Y2. Source NBT unchanged.'},null,2))
 console.log('Rendered',root)
}
main().catch(error=>{console.error(error);process.exitCode=1}).finally(async()=>{if(browser)await browser.close();if(server)server.kill();await fs.writeFile(path.join(root,'preview.log'),log)})
