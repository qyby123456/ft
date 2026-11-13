import { NbtFile, Structure, ThreeStructureRenderer, loadDefaultPackResources } from '/@fs/C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'

document.head.insertAdjacentHTML('beforeend', '<style>*{box-sizing:border-box}body{margin:0;background:#10262b;color:#e2d8bc;font-family:Arial,"Microsoft YaHei",sans-serif}header{height:90px;padding:18px 30px;display:flex;align-items:center;justify-content:space-between;background:#14272b;border-bottom:1px solid #40524c}h1{font-size:26px;margin:0 0 7px;color:#e4c98e}p{margin:0;font-size:13px;letter-spacing:1px;color:#a4bdb2}#label{font-size:17px;color:#add5c3}select{background:#213b3d;border:1px solid #52736a;color:#d6e1d3;padding:7px;margin-left:16px}canvas{display:block;width:100vw;height:calc(100vh - 138px)}footer{height:48px;padding:15px 30px;font-size:13px;color:#b4c4b4;background:#14272b}</style>')
document.body.innerHTML = '<header><div><h1>C02 · 淹水拐弯</h1><p>FLOODED CORNER BEND · MOSSY CISTERN · 19 × 11 × 19</p></div><div><span id="label">Loading…</span><select id="picker" aria-label="视图"></select></div></header><canvas id="structure"></canvas><footer id="footnote"></footer>'
const [bytes, pack] = await Promise.all([
  fetch('/__minecraft_builder__/build.nbt').then(response => response.arrayBuffer()),
  loadDefaultPackResources({ baseUrl: new URL('/__minecraft_builder__/default-pack/', location.href).toString() }),
])
const original = Structure.fromNbt(NbtFile.read(new Uint8Array(bytes)).root)
const canvas = document.getElementById('structure')
const renderer = new ThreeStructureRenderer(canvas, original, pack.resources, {
  preserveDrawingBuffer: true, antialias: true, asyncBuild: false, drawDistance: 1000,
  sunlight: {
    direction: [0.3, 0.85, -0.4], intensity: 0.92, ambientIntensity: 0.78,
    fillIntensity: 0.35, rimIntensity: 0.22, exposure: 1.06,
    sky: { zenithColor: [0.045, 0.09, 0.10], horizonColor: [0.10, 0.17, 0.18], groundColor: [0.045, 0.075, 0.08], stars: { enabled: false } },
  },
})
const center = [9.5, 4.5, 9.5]
const cameras = {
  'exterior-north': { position: [-13, 22, -25], target: center, title: 'N / 北外壳 · 北入口', kind: 'full' },
  'exterior-east': { position: [44, 22, -13], target: center, title: 'E / 东外壳 · 东出口', kind: 'full' },
  'exterior-south': { position: [32, 22, 44], target: center, title: 'S / 南外壳 · 密封背墙', kind: 'full' },
  'exterior-west': { position: [-25, 22, 32], target: center, title: 'W / 西外壳 · 密封侧墙', kind: 'full' },
  'interior-north': { position: [9.5, 22, -25], target: center, title: 'N / 北侧内部 · 展示剖切', kind: 'north' },
  'interior-east': { position: [44, 22, 9.5], target: center, title: 'E / 东侧内部 · 展示剖切', kind: 'east' },
  'interior-south': { position: [9.5, 22, 44], target: center, title: 'S / 南侧内部 · 展示剖切', kind: 'south' },
  'interior-west': { position: [-25, 22, 9.5], target: center, title: 'W / 西侧内部 · 展示剖切', kind: 'west' },
  'hero': { position: [-17, 25, -20], target: [9.5, 3.6, 9.5], title: 'AXON / 柱厅、浅水与 L 通路', kind: 'hero', fov: 36 },
  'plan': { position: [9.5, 42, 9.51], target: [9.5, 0, 9.5], title: 'PLAN / 北 → 东 · 同高干燥三格通路', kind: 'plan', fov: 36 },
  'port-north': { position: [9.5, 3.7, 1.2], target: [9.5, 3.7, 15], title: '北口内视 / 完整原 NBT', kind: 'full', fov: 82 },
  'port-east': { position: [17.8, 3.7, 9.5], target: [3, 3.7, 9.5], title: '东口内视 / 完整原 NBT', kind: 'full', fov: 82 },
}
const displays = new Map()
function visibleStructure(kind) {
  if (kind === 'full') return original
  if (displays.has(kind)) return displays.get(kind)
  const display = new Structure(original.getSize())
  for (const block of original.getBlocks()) {
    const [blockX, blockY, blockZ] = block.pos
    if (blockY === 10) continue
    if (kind === 'plan' && blockY > 2) continue
    if (kind !== 'plan' && blockY >= 2) {
      if ((kind === 'north' || kind === 'hero') && blockZ <= 1) continue
      if ((kind === 'west' || kind === 'hero') && blockX <= 1) continue
      if (kind === 'south' && blockZ >= 17) continue
      if (kind === 'east' && blockX >= 17) continue
    }
    display.addBlock(block.pos, block.state.getName(), block.state.getProperties(), block.nbt)
  }
  displays.set(kind, display)
  return display
}
async function showView(name) {
  const camera = cameras[name]
  if (!camera) throw new Error('Unknown camera ' + name)
  const display = visibleStructure(camera.kind)
  renderer.setStructure(display)
  const bounds = canvas.getBoundingClientRect()
  renderer.setViewport(0, 0, bounds.width, bounds.height, 1)
  renderer.setCamera({ position: camera.position, target: camera.target, fov: camera.fov ?? 34 })
  await renderer.whenReady()
  renderer.drawStructure()
  await new Promise(resolve => requestAnimationFrame(resolve))
  renderer.drawStructure()
  document.getElementById('label').textContent = camera.title
  document.getElementById('picker').value = name
  document.getElementById('footnote').textContent = camera.kind === 'full'
    ? '实际 room.nbt 软件渲染 · 完整屋顶与外墙 · 北口 [9,2,0] / 东口 [18,2,9] · 非游戏客户端截图'
    : '实际 room.nbt 软件渲染 · 仅展示时剖切，交付模板保留完整屋顶与外墙 · 三格宽干燥通路 / 一格深密封水池'
  window.currentC02View = name
  return { name, ...camera, original_records: original.getBlocks().length, display_records: display.getBlocks().length }
}
window.c02 = { showView, cameras: Object.keys(cameras), renderer, original }
const picker = document.getElementById('picker')
for (const [name, camera] of Object.entries(cameras)) picker.add(new Option(camera.title, name))
picker.addEventListener('change', () => showView(picker.value))
window.addEventListener('resize', () => showView(window.currentC02View))
await showView('hero')
window.c02Ready = true

