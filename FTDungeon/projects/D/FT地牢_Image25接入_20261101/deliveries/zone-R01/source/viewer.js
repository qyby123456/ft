import { NbtFile, Structure, ThreeStructureRenderer, loadDefaultPackResources } from '/@fs/C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'

document.head.insertAdjacentHTML('beforeend', `<style>
*{box-sizing:border-box}body{margin:0;background:#131b20;color:#e9deca;font-family:Arial,"Microsoft YaHei",sans-serif}header{height:88px;padding:16px 28px;display:flex;align-items:center;justify-content:space-between;background:#19252b;border-bottom:1px solid #53604c}h1{font-size:24px;margin:0 0 7px;color:#d9be80}p{margin:0;font-size:13px;color:#a7b7ab}#view{font-size:16px;color:#a1c6a1}select{background:#25382c;border:1px solid #6b846a;color:#e0e3d0;padding:6px;margin-left:10px}canvas{display:block;width:100vw;height:calc(100vh - 130px)}footer{height:42px;padding:12px 28px;font-size:12px;background:#19252b;color:#b1beac}
</style>`)
document.body.innerHTML = `<header><div><h1>zone-R01 · 塌塔基座</h1><p>RUINED WATCHTOWER · GROUNDED COLUMNS · GUARDED STAIRS · 25 × 17 × 25</p></div><div><span id="view">Loading actual NBT…</span><select id="view-picker" aria-label="切换方向"></select></div></header><canvas id="structure"></canvas><footer>实际 NBT / Lodestone 软件渲染，非实机截图 · 原结构露天残顶 · 内视剖切仅用于展示 · 独立候选，未部署</footer>`
const [bytes, pack] = await Promise.all([
  fetch('/__minecraft_builder__/build.nbt').then(response => response.arrayBuffer()),
  loadDefaultPackResources({ baseUrl: new URL('/__minecraft_builder__/default-pack/', location.href).toString() }),
])
const original = Structure.fromNbt(NbtFile.read(new Uint8Array(bytes)).root)
const canvas = document.getElementById('structure')
const renderer = new ThreeStructureRenderer(canvas, original, pack.resources, {
  preserveDrawingBuffer: true, antialias: true, asyncBuild: false, drawDistance: 1000,
  sunlight: { direction: [-0.3, 0.9, -0.4], intensity: 0.95, ambientIntensity: 0.78, fillIntensity: 0.35, rimIntensity: 0.2, exposure: 1.02, sky: { zenithColor: [0.065, 0.085, 0.1], horizonColor: [0.13, 0.17, 0.19], groundColor: [0.075, 0.09, 0.1], stars: { enabled: false } } },
})
const center = [12.5, 6.3, 12.5]
const cameras = {
  'exterior-north': { position: [42, 34, -36], target: center, kind: 'full', title: 'N / 北外视 · 完整残墙与接口' },
  'exterior-east': { position: [61, 34, 42], target: center, kind: 'full', title: 'E / 东外视 · 残塔原貌' },
  'exterior-south': { position: [-17, 34, 61], target: center, kind: 'full', title: 'S / 南外视 · 完整残墙' },
  'exterior-west': { position: [-36, 34, -17], target: center, kind: 'full', title: 'W / 西外视 · 苔石与残顶' },
  'interior-north': { position: [39, 31, -30], target: [12.5, 5.2, 12.5], kind: 'north', title: 'N / 北内视 · 展示剖切' },
  'interior-east': { position: [55, 34, 38], target: [12.5, 5.2, 12.5], kind: 'east', title: 'E / 东内视 · 展示剖切' },
  'interior-south': { position: [-14, 32, 55], target: [12.5, 5.2, 12.5], kind: 'south', title: 'S / 南内视 · 双柱木梁与护阶' },
  'interior-west': { position: [-31, 32, -14], target: [12.5, 5.2, 12.5], kind: 'west', title: 'W / 西内视 · 展示剖切' },
  'cutaway': { position: [-17, 34, 53], target: [12.5, 5, 12.5], kind: 'hero', title: 'AXON / 塌塔基座 · 结构剖视' },
  'entrance': { position: [12.5, 3.65, 0.25], target: [12.5, 5, 16], kind: 'full', fov: 78, title: 'ENTRY / 北入口 · 原始NBT' },
  'plan': { position: [12.5, 58, 12.55], target: [12.5, 0, 12.5], kind: 'plan', title: 'PLAN / 首层安全通道 · 展示剖切' },
}
function visibleStructure(kind) {
  if (kind === 'full') return original
  const display = new Structure(original.getSize())
  for (const block of original.getBlocks()) {
    const [blockX, blockY, blockZ] = block.pos
    if (kind === 'north' && block.state.is('minecraft:red_wall_banner')) continue
    if (kind === 'north' && blockZ <= 1 && blockY >= 3) continue
    if (kind === 'east' && blockX >= 23 && blockY >= 3) continue
    if (kind === 'south' && blockZ >= 23 && blockY >= 3) continue
    if (kind === 'west' && blockX <= 1 && blockY >= 3) continue
    if (kind === 'hero' && (blockZ >= 23 || blockX <= 1) && blockY >= 3) continue
    if (kind === 'plan' && blockY >= 3) continue
    display.addBlock(block.pos, block.state.getName(), block.state.getProperties(), block.nbt)
  }
  return display
}
async function showView(name) {
  const camera = cameras[name]
  if (!camera) throw new Error(`Unknown view ${name}`)
  const display = visibleStructure(camera.kind)
  renderer.setStructure(display)
  const bounds = canvas.getBoundingClientRect()
  renderer.setViewport(0, 0, bounds.width, bounds.height, 1)
  renderer.setCamera({ position: camera.position, target: camera.target, fov: camera.fov ?? 35 })
  await renderer.whenReady()
  renderer.drawStructure()
  await new Promise(resolve => requestAnimationFrame(resolve))
  renderer.drawStructure()
  document.getElementById('view').textContent = camera.title
  document.getElementById('view-picker').value = name
  window.currentZoneR01View = name
  return { name, ...camera, original_records: original.getBlocks().length, display_records: display.getBlocks().length }
}
window.zoneR01 = { showView, cameras: Object.keys(cameras), renderer, original }
const picker = document.getElementById('view-picker')
for (const [name, camera] of Object.entries(cameras)) picker.add(new Option(camera.title, name))
picker.addEventListener('change', () => showView(picker.value))
window.addEventListener('resize', () => showView(window.currentZoneR01View))
await showView(cameras[location.hash.slice(1)] ? location.hash.slice(1) : 'cutaway')
window.zoneR01Ready = true
