const fs = require('node:fs/promises')
const path = require('node:path')
const { pathToFileURL } = require('node:url')
const { chromium } = require('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright')

async function main() {
  const root = path.resolve(__dirname, '..')
  const skill = 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
  const { createServer } = await import(pathToFileURL(path.join(skill, 'node_modules/vite/dist/node/index.js')).href)
  const pack = path.join(skill, 'node_modules/@mattzh72/lodestone/assets/default-pack')
  const viewer = path.join(root, 'views/lodestone')
  await fs.mkdir(viewer, { recursive: true })
  const html = `<!doctype html><html><head><meta charset="utf-8"><title>H05 vaulted great hall actual NBT</title><style>html,body{margin:0;background:#111722;overflow:hidden}canvas{display:block}#caption{position:fixed;left:20px;top:18px;color:#f2f0d9;background:#111722dd;padding:12px 18px;border:1px solid #435047;border-radius:8px;font:16px sans-serif}small{display:block;color:#9cbfbc;margin-top:7px}</style></head><body><canvas id="view"></canvas><div id="caption">H05 · 穹顶大殿 · 31 × 27 × 39<small>最终 room.nbt / Lodestone 0.6.0 软件渲染 · 非游戏截图</small></div><script type="module">
import { NbtFile, Structure, ThreeStructureRenderer, loadDefaultPackResources } from '/@fs/${skill}/node_modules/@mattzh72/lodestone/lib/index.js';
const response = await fetch('/room.nbt');
const bytes = new Uint8Array(await response.arrayBuffer());
const structure = Structure.fromNbt(NbtFile.read(bytes).root);
const pack = await loadDefaultPackResources({baseUrl:location.origin+'/pack/'});
const renderer = new ThreeStructureRenderer(document.getElementById('view'),structure,pack.resources,{preserveDrawingBuffer:true,asyncBuild:true,asyncChunkBuildTimeMs:8,drawDistance:400,sunlight:{intensity:0.35,ambientIntensity:0.72,fillIntensity:0.25,rimIntensity:0.15,exposure:1.08,sky:{zenithColor:[0.02,0.025,0.04],horizonColor:[0.04,0.04,0.045],groundColor:[0.02,0.02,0.025]},fog:{density:0.00003}}});
renderer.setViewport(0,0,innerWidth,innerHeight,1);
renderer.setFOV(64);
await renderer.whenReady();
window.renderView = (position,target) => {renderer.lookAt(position,target);renderer.drawStructure();};
window.renderView([15.5,5.4,3.5],[15.5,9.5,36]);
window.ready=true;
</script></body></html>`
  await fs.writeFile(path.join(viewer, 'index.html'), html)
  const server = await createServer({ root: viewer, clearScreen: false, server: { host: '127.0.0.1', port: 0, fs: { strict: false } }, plugins: [{name:'h05-files',configureServer(instance){instance.middlewares.use(async (request,response,next)=>{try { const url = decodeURIComponent((request.url || '/').split('?')[0]); const target = url === '/room.nbt' ? path.join(root,'room.nbt') : url.startsWith('/pack/') ? path.resolve(pack,url.slice(6)) : null; if(!target) return next(); if(url.startsWith('/pack/') && !target.startsWith(path.resolve(pack)+path.sep)) {response.statusCode=403;return response.end();} const bytes = await fs.readFile(target); response.setHeader('Content-Type',target.endsWith('.json')?'application/json':target.endsWith('.png')?'image/png':'application/octet-stream');response.end(bytes);} catch(error){response.statusCode=404;response.end(error.message);}})}}] })
  await server.listen()
  const url = server.resolvedUrls.local[0]
  const browser = await chromium.launch({ executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe', headless: true, args: ['--enable-webgl','--use-gl=angle','--use-angle=swiftshader','--disable-gpu-sandbox'] })
  const page = await browser.newPage({ viewport: {width:1536,height:1024} })
  const errors = []
  page.on('pageerror', error => errors.push(error.message))
  try {
    await page.goto(url, {waitUntil:'networkidle',timeout:180000})
    await page.waitForFunction(() => window.ready === true, {timeout:180000})
    await page.screenshot({path:path.join(root,'views','lodestone-north.png')})
    await page.evaluate(() => window.renderView([15.5,6.5,35.5],[15.5,9,3]))
    await page.screenshot({path:path.join(root,'views','lodestone-south.png')})
    await page.evaluate(() => window.renderView([-35,40,-40],[15.5,11,19.5]))
    await page.screenshot({path:path.join(root,'views','lodestone-exterior.png')})
    await fs.writeFile(path.join(root,'qa','lodestone-render.json'),JSON.stringify({status:errors.length?'FAIL':'PASS',renderer:'Lodestone 0.6.0 / ThreeStructureRenderer',source_sha256:JSON.parse(await fs.readFile(path.join(root,'room.json'),'utf8')).sha256,source:'room.nbt',preview:'views/lodestone-north.png',errors,game_screenshot:false,shaders_or_particle_claims:false},null,2)+'\n')
    if(errors.length) throw new Error(errors.join('\n'))
    console.log('H05_LODESTONE_RENDER_PASS')
  } finally {
    await browser.close()
    await server.close()
  }
}

main().catch(error => { console.error(error); process.exit(1) })

