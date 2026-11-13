import { NbtFile, Structure, ThreeStructureRenderer, loadDefaultPackResources } from '/@fs/C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'

document.head.insertAdjacentHTML('beforeend', `<style>
*{box-sizing:border-box}body{margin:0;background:#121d25;color:#eadcc4;font-family:Arial,"Microsoft YaHei",sans-serif}
header{height:82px;padding:15px 28px;display:flex;align-items:center;justify-content:space-between;background:#182731;border-bottom:1px solid #40515b}
h1{font-size:24px;margin:0 0 6px;color:#efd09b}p{margin:0;font-size:13px;color:#afbec3}#view{font-size:16px;font-weight:600;color:#cbdccf}select{background:#223a44;border:1px solid #59726c;color:#e6dfc9;padding:6px;margin-left:12px}
canvas{display:block;width:100vw;height:calc(100vh - 126px)}footer{height:44px;padding:12px 28px;font-size:12px;color:#bfcccb;background:#182731}
</style>`)
document.body.innerHTML = `<header><div><h1>N01 · 四向枢纽 / 罗盘灯台</h1><p>FOUR PORTS · FLAT COMPASS · FREE-STANDING PILLARS · 25 × 12 × 25</p></div><div><span id="view">Loading actual NBT…</span><select id="view-picker" aria-label="切换查看方向"></select></div></header><canvas id="structure"></canvas><footer>真实 room.nbt · Lodestone 软件渲染，非实机截图 / 四口 5 × 5 / 三格环路、五格净空 / 中心安全脚点 (12,2,9) / 展示剖切不写回 NBT</footer>`
const [bytes, pack] = await Promise.all([
  fetch('/__minecraft_builder__/build.nbt').then(response => response.arrayBuffer()),
  loadDefaultPackResources({ baseUrl: new URL('/__minecraft_builder__/default-pack/', location.href).toString() }),
])
const original = Structure.fromNbt(NbtFile.read(new Uint8Array(bytes)).root)
const canvas = document.getElementById('structure')
const renderer = new ThreeStructureRenderer(canvas, original, pack.resources, {
  preserveDrawingBuffer: true, antialias: true, asyncBuild: false, drawDistance: 1000,
  sunlight: {
    direction: [0.35, 0.85, -0.25], intensity: 0.85, ambientIntensity: 0.8,
    fillIntensity: 0.3, rimIntensity: 0.2, exposure: 1.04,
    sky: { zenithColor: [0.055, 0.08, 0.1], horizonColor: [0.13, 0.18, 0.2], groundColor: [0.065, 0.08, 0.09], stars: { enabled: false } },
  },
})
const center = [12.5, 5.5, 12.5]
const cameras = {
  'cutaway': { position: [49, 43, 50], target: [12.5, 3, 12.5], title: 'AXON / 罗盘与环路 · 仅展示剖切', kind: 'cutaway', fov: 34 },
  'plan': { position: [12.5, 52, 12.6], target: [12.5, 0, 12.5], title: 'PLAN / 北上东右 · 仅展示剖切', kind: 'plan', fov: 34 },
  'exterior-north': { position: [12.5, 20, -33], target: center, title: 'N / 北外视 · 五格接口与完整屋顶', kind: 'full', fov: 42 },
  'exterior-east': { position: [58, 20, 12.5], target: center, title: 'E / 东外视 · 五格接口与完整屋顶', kind: 'full', fov: 42 },
  'exterior-south': { position: [12.5, 20, 58], target: center, title: 'S / 南外视 · 五格接口与完整屋顶', kind: 'full', fov: 42 },
  'exterior-west': { position: [-33, 20, 12.5], target: center, title: 'W / 西外视 · 五格接口与完整屋顶', kind: 'full', fov: 42 },
  'interior-north': { position: [12.5, 3.65, 2.5], target: [12.5, 3.8, 20], title: 'N → S / 北口内视 · 原 NBT', kind: 'full', fov: 88 },
  'interior-east': { position: [22.5, 3.65, 12.5], target: [5, 3.8, 12.5], title: 'E → W / 东口内视 · 原 NBT', kind: 'full', fov: 88 },
  'interior-south': { position: [12.5, 3.65, 22.5], target: [12.5, 3.8, 5], title: 'S → N / 南口内视 · 原 NBT', kind: 'full', fov: 88 },
  'interior-west': { position: [2.5, 3.65, 12.5], target: [20, 3.8, 12.5], title: 'W → E / 西口内视 · 原 NBT', kind: 'full', fov: 88 },
  'complete': { position: [53, 37, 54], target: center, title: 'FULL / 完整导出结构 · 原 NBT', kind: 'full', fov: 38 },
}
function visibleStructure(kind) {
  if (kind === 'full') return original
  const display = new Structure(original.getSize())
  for (const block of original.getBlocks()) {
    const [blockX, blockY, blockZ] = block.pos
    if (blockY === 11) continue
    if (kind === 'plan' && blockY > 3) continue
    if (kind === 'cutaway' && blockY >= 10) continue
    if (kind === 'cutaway' && blockX >= 18 && blockX <= 21 && blockZ >= 18 && blockZ <= 21 && blockY >= 4) continue
    if (kind === 'cutaway' && (blockX >= 23 || blockZ >= 23) && blockY >= 2) continue
    display.addBlock(block.pos, block.state.getName(), block.state.getProperties(), block.nbt)
  }
  return display
}
async function showView(name) {
  const camera = cameras[name]
  if (!camera) throw new Error(`Unknown view ${name}`)
  renderer.setStructure(visibleStructure(camera.kind))
  const bounds = canvas.getBoundingClientRect()
  renderer.setViewport(0, 0, bounds.width, bounds.height, 1)
  renderer.setCamera({ position: camera.position, target: camera.target, fov: camera.fov })
  await renderer.whenReady()
  renderer.drawStructure()
  await new Promise(resolve => requestAnimationFrame(resolve))
  renderer.drawStructure()
  document.getElementById('view').textContent = camera.title
  document.getElementById('view-picker').value = name
  return { name, ...camera, source: 'room.nbt', cutaway: camera.kind !== 'full', original_nbt_modified: false }
}
const picker = document.getElementById('view-picker')
for (const [name, camera] of Object.entries(cameras)) picker.add(new Option(camera.title, name))
picker.addEventListener('change', () => showView(picker.value))
window.n01 = { showView, cameras: Object.keys(cameras) }
await showView('cutaway')
window.n01Ready = true
