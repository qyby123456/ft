import { NbtFile, Structure, ThreeStructureRenderer, loadDefaultPackResources } from '/@fs/C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'

document.head.insertAdjacentHTML('beforeend', '<style>*{box-sizing:border-box}body{margin:0;background:#101a20;color:#e4dac5;font-family:Arial,"Microsoft YaHei",sans-serif}header{height:96px;padding:20px 28px;display:flex;align-items:center;justify-content:space-between;background:#15252c;border-bottom:1px solid #395052}h1{font-size:25px;margin:0 0 7px;color:#e3c18a}p{margin:0;font-size:13px;color:#a9bdba}#view{font-size:17px;font-weight:600;color:#a4d9cd}select{background:#20363c;border:1px solid #597b76;color:#d8dfcd;padding:6px;margin-left:12px}canvas{display:block;width:100vw;height:calc(100vh - 140px)}footer{height:44px;padding:12px 28px;font-size:12px;color:#b6c5be;background:#15252c}</style>')
document.body.innerHTML = '<header><div><h1>H04 · 柱列中庭</h1><p>PILLARED ATRIUM · FOUNTAIN · TWIN STAIRS · 33 × 23 × 37 · JAVA 1.20.1</p></div><div><span id="view">Loading actual NBT…</span><select id="view-picker" aria-label="切换视角"></select></div></header><canvas id="structure"></canvas><footer>实际 room.nbt / Lodestone 软件渲染，非游戏截图 · 原模板完整封顶 · 剖切仅用于展示，不更改 NBT · 北入口 [16,2,0] / 安全点 [16,2,7]</footer>'
const [bytes, pack] = await Promise.all([
  fetch('/__minecraft_builder__/build.nbt').then(response => response.arrayBuffer()),
  loadDefaultPackResources({ baseUrl: new URL('/__minecraft_builder__/default-pack/', location.href).toString() }),
])
const original = Structure.fromNbt(NbtFile.read(new Uint8Array(bytes)).root)
const canvas = document.getElementById('structure')
const renderer = new ThreeStructureRenderer(canvas, original, pack.resources, {
  preserveDrawingBuffer: true, antialias: true, asyncBuild: false, drawDistance: 1000,
  sunlight: { direction: [0.35,0.85,-0.25], intensity: 0.92, ambientIntensity: 0.75, fillIntensity: 0.36, rimIntensity: 0.22, exposure: 1.04,
    sky: { zenithColor: [0.065,0.11,0.14], horizonColor: [0.14,0.2,0.22], groundColor: [0.07,0.1,0.12], stars: { enabled: false } } },
})
const center = [16.5,9,18.5]
const cameras = {
  'exterior-north': { position: [-25,35,-35], target: center, title: 'N / 北外视 · 完整屋顶与北入口', kind: 'full' },
  'exterior-east': { position: [69,35,-19], target: center, title: 'E / 东外视 · 全高柱廊外壳', kind: 'full' },
  'exterior-south': { position: [58,35,73], target: center, title: 'S / 南外视 · 连续屋顶与南出口', kind: 'full' },
  'exterior-west': { position: [-37,35,56], target: center, title: 'W / 西外视 · 密封侧窗和西出口', kind: 'full' },
  'interior-north': { position: [16.5,28,-35], target: [16.5,7,20], title: 'N → S / 北向内视 · 展示剖切', kind: 'north', fov: 38 },
  'interior-east': { position: [70,28,18.5], target: [15,7,18.5], title: 'E → W / 东向内视 · 展示剖切', kind: 'east', fov: 38 },
  'interior-south': { position: [16.5,28,72], target: [16.5,7,17], title: 'S → N / 南向内视 · 展示剖切', kind: 'south', fov: 38 },
  'interior-west': { position: [-37,28,18.5], target: [18,7,18.5], title: 'W → E / 西向内视 · 展示剖切', kind: 'west', fov: 38 },
  'cutaway': { position: [-29,34,-28], target: [16,7,19], title: 'AXON / 柱列、双梯、喷泉与回廊', kind: 'cutaway', fov: 36 },
  'plan': { position: [16.5,58,18.55], target: [16.5,0,18.5], title: 'PLAN / 地面入口和喷泉环行路线', kind: 'plan', fov: 42 },
  'eye-north': { position: [16.5,3.65,4.5], target: [16.5,8,22], title: 'EYE N / 北入口原模板内视', kind: 'full', fov: 92 },
  'eye-east': { position: [29.5,3.65,19.5], target: [14,7,19.5], title: 'EYE E / 东入口原模板内视', kind: 'full', fov: 92 },
  'eye-south': { position: [16.5,3.65,33.5], target: [16.5,8,17], title: 'EYE S / 南入口原模板内视', kind: 'full', fov: 92 },
  'eye-west': { position: [3.5,3.65,19.5], target: [19,7,19.5], title: 'EYE W / 西入口原模板内视', kind: 'full', fov: 92 },
}
const cache = new Map()
function visibleStructure(kind) {
  if (kind === 'full') return original
  if (cache.has(kind)) return cache.get(kind)
  const display = new Structure(original.getSize())
  for (const block of original.getBlocks()) {
    const [blockX, blockY, blockZ] = block.pos
    if (blockY >= 21) continue
    if ((kind === 'north' || kind === 'cutaway') && blockZ <= 1 && blockY >= 2) continue
    if ((kind === 'west' || kind === 'cutaway') && blockX <= 1 && blockY >= 2) continue
    if (kind === 'east' && blockX >= 31 && blockY >= 2) continue
    if (kind === 'south' && blockZ >= 35 && blockY >= 2) continue
    if (kind === 'plan' && blockY >= 4) continue
    display.addBlock(block.pos, block.state.getName(), block.state.getProperties(), block.nbt)
  }
  cache.set(kind, display)
  return display
}
async function showView(name) {
  const camera = cameras[name]
  if (!camera) throw new Error('Unknown view ' + name)
  const visible = visibleStructure(camera.kind)
  renderer.setStructure(visible)
  const bounds = canvas.getBoundingClientRect()
  renderer.setViewport(0,0,bounds.width,bounds.height,1)
  renderer.setCamera({ position: camera.position, target: camera.target, fov: camera.fov ?? 36 })
  await renderer.whenReady()
  renderer.drawStructure()
  await new Promise(resolve => requestAnimationFrame(resolve))
  renderer.drawStructure()
  document.getElementById('view').textContent = camera.title
  document.getElementById('view-picker').value = name
  window.currentH04View = name
  return { name, ...camera, original_records: original.getBlocks().length, display_records: visible.getBlocks().length }
}
window.h04 = { showView, cameras: Object.keys(cameras), renderer, original }
const picker = document.getElementById('view-picker')
for (const [name, camera] of Object.entries(cameras)) picker.add(new Option(camera.title, name))
picker.addEventListener('change', () => showView(picker.value))
window.addEventListener('resize', () => showView(window.currentH04View))
await showView(cameras[location.hash.slice(1)] ? location.hash.slice(1) : 'cutaway')
window.h04Ready = true
