import fs from 'node:fs/promises'
import path from 'node:path'
import { createHash } from 'node:crypto'
import { createRequire } from 'node:module'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const require = createRequire('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/package.json')
const { chromium } = require('playwright')
const url = process.argv[2] ?? 'http://127.0.0.1:5374/'
const injection = `
  window.__n04 = { structure, renderer, async view(mode, side) {
    let displayed = structure;
    if (mode === 'cutaway') {
      displayed = new Structure(structure.getSize());
      for (const block of structure.getBlocks()) {
        const [blockX, blockY, blockZ] = block.pos;
        if (blockY >= 14 || (side === 'north' && blockZ <= 1) || (side === 'south' && blockZ >= 33) || (side === 'west' && blockX <= 1) || (side === 'east' && blockX >= 33)) continue;
        displayed.addBlock(block.pos, block.state.getName(), block.state.getProperties(), block.nbt);
      }
    }
    renderer.setStructure(displayed);
    await renderer.whenReady();
    const positions = { north: [-18,35,-32], east: [67,35,-18], south: [53,35,67], west: [-32,35,53] };
    const eyes = { north: [17.5,4.5,25], east: [10,4.5,17.5], south: [17.5,4.5,10], west: [25,4.5,17.5] };
    const targets = { north: [17.5,5,2], east: [32,5,17.5], south: [17.5,5,32], west: [2,5,17.5] };
    renderer.setSunlight({direction:[.35,.85,.25],intensity:.6,ambientIntensity:.76,fillIntensity:.3,rimIntensity:.25,exposure:1.05,
      sky:{zenithColor:[.027,.025,.055],horizonColor:[.065,.04,.08],groundColor:[.025,.02,.04],stars:{enabled:false}},
      fog:{color:[.045,.03,.065],density:.0001,heightFalloff:.001}});
    renderer.setCamera({ position: mode === 'eye' ? eyes[side] : positions[side], target: mode === 'eye' ? targets[side] : [17.5,5,17.5], fov: mode === 'eye' ? 78 : 48 });
    renderer.drawStructure();
    await new Promise(resolve=>requestAnimationFrame(()=>{renderer.drawStructure();resolve()}));
    return { mode, side, displayedRecords: displayed.getBlocks().length, sourceRecords:structure.getBlocks().length, camera:renderer.getCamera() };
  }};
  loading.classList.add('hidden')`
const browser = await chromium.launch({ executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe',
  headless: true, args: ['--enable-unsafe-swiftshader', '--use-gl=angle', '--use-angle=swiftshader', '--disable-dev-shm-usage'] })
const errors = []
try {
  const page = await browser.newPage({ viewport: { width: 1536, height: 1024 }, deviceScaleFactor: 1 })
  page.on('pageerror', error => errors.push(error.message))
  await page.route('**/src/main.js', async route => {
    const response = await route.fetch()
    const source = (await response.text()).replace("  loading.classList.add('hidden')", injection)
    await route.fulfill({ response, body: source })
  })
  await page.goto(url, { waitUntil: 'domcontentloaded', timeout: 90000 })
  await page.waitForFunction(() => window.__n04, { timeout: 90000 })
  await page.addStyleTag({ content: '.hud,.viewer-actions,#loading{display:none!important}' })
  const folder = path.join(root, 'views')
  await fs.mkdir(folder, { recursive: true })
  const nbtSha256 = createHash('sha256').update(await fs.readFile(path.join(root, 'room.nbt'))).digest('hex')
  const images = []
  const capture = async (mode, side, destination) => {
    const evidence = await page.evaluate(({mode, side}) => window.__n04.view(mode, side), { mode, side })
    await page.locator('#preview').screenshot({ path: destination })
    images.push({ path: path.relative(root, destination).replaceAll('\\', '/'), sha256: createHash('sha256').update(await fs.readFile(destination)).digest('hex'), ...evidence })
    console.log('RENDERED', mode, side, destination)
  }
  await capture('eye', 'north', path.join(root, 'preview.png'))
  for (const side of ['north', 'south', 'west', 'east']) {
    await capture('eye', side, path.join(folder, `${side}-interior.png`))
    await capture('cutaway', side, path.join(folder, `${side}-cutaway.png`))
    await capture('exterior', side, path.join(folder, `${side}-exterior.png`))
  }
  if (errors.length) throw new Error(errors.join('; '))
  const manifest = { id: 'zone-N04', status: 'RENDERED_PENDING_VISUAL_REVIEW', sha256: nbtSha256,
    renderer: 'Minecraft Builder / Lodestone 0.6.0 / Three.js, real final NBT', software_render: true,
    game_client_screenshot: false, source_unchanged: nbtSha256 === createHash('sha256').update(await fs.readFile(path.join(root, 'room.nbt'))).digest('hex'),
    cutaway_policy: 'Eye and exterior use unchanged full NBT. Cutaway removes only y>=14 roof and 2-block viewing-side wall in browser memory; beams, chain anchors and final NBT remain intact', images, errors }
  await fs.writeFile(path.join(folder, 'manifest.json'), JSON.stringify(manifest, null, 2) + '\n')
} finally {
  await browser.close()
}
