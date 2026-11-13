import fs from 'node:fs/promises'
import path from 'node:path'
import { createHash } from 'node:crypto'
import { createRequire } from 'node:module'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const require = createRequire('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/package.json')
const { chromium } = require('playwright')
const url = process.argv[2] ?? 'http://127.0.0.1:5266/'
const injection = `
  window.__m16 = { structure, renderer, async view(mode, side) {
    let displayed = structure;
    if (mode === 'cutaway') {
      displayed = new Structure(structure.getSize());
      for (const block of structure.getBlocks()) {
        const [blockX, blockY, blockZ] = block.pos;
        if (blockY === 10 || (side === 'north' && blockZ <= 1) || (side === 'south' && blockZ >= 38) || (side === 'west' && blockX <= 1) || (side === 'east' && blockX >= 13)) continue;
        displayed.addBlock(block.pos, block.state.getName(), block.state.getProperties(), block.nbt);
      }
    }
    renderer.setStructure(displayed);
    await renderer.whenReady();
    const positions = { north: [-8,28,-32], south: [23,28,72], west: [-34,28,20], east: [49,28,20] };
    const eye = mode === 'eye';
    renderer.setSunlight({direction:[.35,.85,.25],intensity:.45,ambientIntensity:.68,fillIntensity:.25,rimIntensity:.2,exposure:.95,
      sky:{zenithColor:[.025,.04,.06],horizonColor:[.04,.065,.075],groundColor:[.02,.03,.04],stars:{enabled:false}},
      fog:{color:[.04,.065,.075],density:.0001,heightFalloff:.001}});
    renderer.setCamera({ position: eye ? [7.5,3.65,1.25] : positions[side], target: eye ? [7.5,3.65,39] : [7.5,3.5,20], fov: eye ? 73 : 48 });
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
  await page.waitForFunction(() => window.__m16, { timeout: 90000 })
  await page.addStyleTag({ content: '.hud,.viewer-actions,#loading{display:none!important}' })
  const folder = path.join(root, 'views')
  await fs.mkdir(folder, { recursive: true })
  const nbtSha256 = createHash('sha256').update(await fs.readFile(path.join(root, 'room.nbt'))).digest('hex')
  const images = []
  const capture = async (mode, side, destination) => {
    const evidence = await page.evaluate(({mode, side}) => window.__m16.view(mode, side), { mode, side })
    await page.locator('#preview').screenshot({ path: destination })
    images.push({ path: path.relative(root, destination).replaceAll('\\', '/'), sha256: createHash('sha256').update(await fs.readFile(destination)).digest('hex'), ...evidence })
    console.log('RENDERED', mode, side, destination)
  }
  await capture('eye', 'north', path.join(root, 'preview.png'))
  for (const side of ['north', 'south', 'west', 'east']) {
    await capture('cutaway', side, path.join(folder, `${side}-interior.png`))
    await capture('exterior', side, path.join(folder, `${side}-exterior.png`))
  }
  if (errors.length) throw new Error(errors.join('; '))
  const manifest = { id: 'corridor-M16', status: 'RENDERED_PENDING_VISUAL_REVIEW', sha256: nbtSha256,
    renderer: 'Minecraft Builder / Lodestone 0.6.0 / Three.js, real final NBT', software_render: true,
    game_client_screenshot: false, source_unchanged: nbtSha256 === createHash('sha256').update(await fs.readFile(path.join(root, 'room.nbt'))).digest('hex'),
    cutaway_policy: 'Preview-only roof and view-facing outer wall removal; final NBT remains complete', images, errors }
  await fs.writeFile(path.join(folder, 'manifest.json'), JSON.stringify(manifest, null, 2) + '\n')
} finally {
  await browser.close()
}
