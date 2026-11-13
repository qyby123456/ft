const fs = require('node:fs/promises')
const path = require('node:path')
const crypto = require('node:crypto')
const { pathToFileURL } = require('node:url')
const { chromium } = require('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright')

async function main() {
  const root = path.resolve(__dirname, '..')
  const skill = 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
  const { createServer } = await import(pathToFileURL(path.join(skill, 'node_modules/vite/dist/node/index.js')).href)
  const pack = path.join(skill, 'node_modules/@mattzh72/lodestone/assets/default-pack')
  const viewer = path.join(root, 'views/viewer')
  await fs.mkdir(viewer, { recursive: true })
  const html = `<!doctype html><html><head><meta charset="utf-8"><title>A01 abandoned study NBT inspection</title><style>html,body{margin:0;background:#172029;overflow:hidden}canvas{display:block}#caption{position:fixed;left:24px;top:20px;color:#f8ecd7;background:#172029e8;padding:12px 18px;font:18px sans-serif;border:1px solid #626753}</style></head><body><canvas id="view"></canvas><div id="caption"></div><script type="module">
import { NbtFile, Structure, ThreeStructureRenderer, loadDefaultPackResources } from '/@fs/${skill}/node_modules/@mattzh72/lodestone/lib/index.js';
const bytes = new Uint8Array(await (await fetch('/room.nbt')).arrayBuffer());
const original = Structure.fromNbt(NbtFile.read(bytes).root);
const pack = await loadDefaultPackResources({baseUrl:location.origin+'/pack/'});
const renderer = new ThreeStructureRenderer(document.getElementById('view'),original,pack.resources,{preserveDrawingBuffer:true,asyncBuild:true,asyncChunkBuildTimeMs:8,drawDistance:250,sunlight:{intensity:0.6,ambientIntensity:0.82,fillIntensity:0.35,rimIntensity:0.2,exposure:1.1,sky:{zenithColor:[0.07,0.10,0.12],horizonColor:[0.1,0.13,0.14],groundColor:[0.07,0.08,0.08]},fog:{density:0.00001}}});
renderer.setViewport(0,0,innerWidth,innerHeight,1);
renderer.setFOV(42);
await renderer.whenReady();
window.renderView = async (mode,direction) => {
  const cameras = {north:[-12,26,-26],south:[29,26,43],west:[-26,26,29],east:[43,26,-12]};
  const camera = cameras[direction];
  const selected = new Structure(original.getSize());
  for (const block of original.getBlocks()) {
    const [blockX,blockY,blockZ] = block.pos;
    const name = block.state.getName().toString();
    if (mode === 'interior') {
      if (blockY === 10) continue;
      const nearWall = (camera[0]<8.5?blockX===0:blockX===16)||(camera[2]<8.5?blockZ===0:blockZ===16);
      if (blockY>=2 && nearWall) continue;
      if (blockY===9 && name!=='minecraft:spruce_log') continue;
      if (blockY===9 && (camera[0]<8.5?blockX===1:blockX===15)) continue;
      if (blockY===9 && (camera[2]<8.5?blockZ===1:blockZ===15)) continue;
    }
    selected.addBlock(block.pos,name,block.state.getProperties(),block.nbt);
  }
  renderer.setStructure(selected);
  await renderer.whenReady();
  renderer.lookAt(camera,[8.5,4.6,8.5]);
  renderer.drawStructure();
  document.getElementById('caption').textContent='A01 Abandoned Study | '+direction.toUpperCase()+' | '+(mode==='interior'?'CUTAWAY - view only':'COMPLETE SHELL')+' | 17 x 11 x 17';
  await new Promise(resolve=>requestAnimationFrame(()=>requestAnimationFrame(resolve)));
  return {blocks:selected.getBlocks().length,source_blocks:original.getBlocks().length};
};
window.ready=true;
</script></body></html>`
  await fs.writeFile(path.join(viewer, 'index.html'), html)
  const server = await createServer({ root: viewer, clearScreen: false, server: { host: '127.0.0.1', port: 0, fs: { strict: false } }, plugins: [{ name: 'a01-local-files', configureServer(instance) { instance.middlewares.use(async (request, response, next) => {
    try {
      const url = decodeURIComponent((request.url || '/').split('?')[0])
      const target = url === '/room.nbt' ? path.join(root, 'room.nbt') : url.startsWith('/pack/') ? path.resolve(pack, url.slice(6)) : null
      if (!target) return next()
      if (url.startsWith('/pack/') && !target.startsWith(path.resolve(pack) + path.sep)) { response.statusCode = 403; return response.end() }
      const bytes = await fs.readFile(target)
      response.setHeader('Content-Type', target.endsWith('.json') ? 'application/json' : target.endsWith('.png') ? 'image/png' : 'application/octet-stream')
      response.end(bytes)
    } catch (error) { response.statusCode = 404; response.end(error.message) }
  }) } }] })
  await server.listen()
  const browser = await chromium.launch({ executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe', headless: true, args: ['--enable-webgl', '--use-gl=angle', '--use-angle=swiftshader', '--disable-gpu-sandbox'] })
  const page = await browser.newPage({ viewport: { width: 1536, height: 1024 } })
  const errors = []
  page.on('pageerror', error => errors.push(error.message))
  const outputs = []
  try {
    await page.goto(server.resolvedUrls.local[0], { waitUntil: 'networkidle', timeout: 240000 })
    await page.waitForFunction(() => window.ready === true, { timeout: 240000 })
    for (const mode of ['interior', 'exterior']) {
      for (const direction of ['north', 'south', 'west', 'east']) {
        const detail = await page.evaluate(async ({ mode, direction }) => window.renderView(mode, direction), { mode, direction })
        const relative = `views/${mode}-${direction}.png`
        await page.screenshot({ path: path.join(root, relative) })
        const bytes = await fs.readFile(path.join(root, relative))
        outputs.push({ file: relative, mode, direction, ...detail, sha256: crypto.createHash('sha256').update(bytes).digest('hex') })
        console.log('A01_RENDERED ' + relative)
      }
    }
    await fs.copyFile(path.join(root, 'views/interior-north.png'), path.join(root, 'preview.png'))
    const sourceHash = crypto.createHash('sha256').update(await fs.readFile(path.join(root, 'room.nbt'))).digest('hex')
    const report = { status: errors.length ? 'FAIL' : 'PASS', source_sha256: sourceHash, renderer: 'Lodestone 0.6.0 / ThreeStructureRenderer / default resource pack', outputs, errors, source_nbt_modified_for_cutaway: false, game_screenshots: false }
    await fs.writeFile(path.join(root, 'qa/render.json'), JSON.stringify(report, null, 2) + '\n')
    if (errors.length) throw new Error(errors.join('\n'))
  } finally {
    await browser.close()
    await server.close()
  }
}

main().catch(error => { console.error(error); process.exit(1) })
