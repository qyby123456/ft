import { NbtFile, Structure, ThreeStructureRenderer, loadDefaultPackResources } from '/@fs/C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'

document.head.insertAdjacentHTML('beforeend', `<style>
*{box-sizing:border-box}body{margin:0;background:#111d22;color:#e8e3cf;font-family:Arial,"Microsoft YaHei",sans-serif}
header{height:84px;padding:16px 30px;display:flex;align-items:center;justify-content:space-between;background:#182a2e;border-bottom:1px solid #42625c}
h1{font-size:25px;margin:0 0 7px;color:#e8c58b}p{margin:0;font-size:13px;color:#b0c6bb}#view{font-size:17px;color:#a7ddbf}select{background:#223b3b;border:1px solid #537a6d;color:#eee8cd;padding:7px;margin-left:12px}
canvas{display:block;width:100vw;height:calc(100vh - 130px)}footer{height:46px;padding:13px 30px;font-size:12px;color:#c1cec0;background:#182a2e}
</style>`)
document.body.innerHTML = `<header><div><h1>P05 · 石柱跃厅</h1><p>五柱连跳 · 两格空隙 · 浅水回程 · 17 × 14 × 29</p></div><div><span id="view">读取实际 NBT…</span><select id="view-picker" aria-label="切换查看方向"></select></div></header><canvas id="structure"></canvas><footer>实际 NBT 的 Lodestone 软件预览，非游戏截图 / 剖切仅用于展示，交付结构完整封顶 / 黄入口 → 五根石柱 → 绿出口；水中双梯返回黄入口</footer>`
const [bytes, pack] = await Promise.all([
  fetch('/__minecraft_builder__/build.nbt').then(response => response.arrayBuffer()),
  loadDefaultPackResources({ baseUrl: new URL('/__minecraft_builder__/default-pack/', location.href).toString() }),
])
const original = Structure.fromNbt(NbtFile.read(new Uint8Array(bytes)).root)
const canvas = document.getElementById('structure')
const renderer = new ThreeStructureRenderer(canvas, original, pack.resources, {
  preserveDrawingBuffer: true, antialias: true, asyncBuild: false, drawDistance: 1000,
  sunlight: {
    direction: [0.35, 0.85, -0.3], intensity: 0.92, ambientIntensity: 0.82,
    fillIntensity: 0.4, rimIntensity: 0.25, exposure: 1.12,
    sky: { zenithColor: [0.07, 0.11, 0.14], horizonColor: [0.14, 0.22, 0.24], groundColor: [0.08, 0.12, 0.13], stars: { enabled: false } },
  },
})
const center = [8.5, 5.5, 14.5]
const cameras = {
  'exterior-north': { position: [-20, 24, -31], target: center, title: 'N / 北侧外壳 · 黄入口', kind: 'full', fov: 37 },
  'exterior-east': { position: [52, 24, -12], target: center, title: 'E / 东侧外壳 · 完整封顶', kind: 'full', fov: 37 },
  'exterior-south': { position: [39, 24, 60], target: center, title: 'S / 南侧外壳 · 绿出口', kind: 'full', fov: 37 },
  'exterior-west': { position: [-35, 24, 41], target: center, title: 'W / 西侧外壳 · 密封墙体', kind: 'full', fov: 37 },
  'interior-north': { position: [8, 7.2, 1.2], target: [8, 4.3, 25], title: 'N → S / 北入口内视 · 原始完整 NBT', kind: 'full', fov: 78 },
  'interior-south': { position: [8, 7.2, 27.8], target: [8, 4.3, 3], title: 'S → N / 南出口内视 · 原始完整 NBT', kind: 'full', fov: 78 },
  'interior-east': { position: [14.7, 6.7, 14], target: [3, 4, 14], title: 'E → W / 东侧内部 · 原始完整 NBT', kind: 'full', fov: 104 },
  'interior-west': { position: [2.1, 6.7, 14], target: [14, 4, 14], title: 'W → E / 西侧内部 · 原始完整 NBT', kind: 'full', fov: 104 },
  'cutaway': { position: [-27, 28, -20], target: [8.5, 3.6, 14], title: 'AXON / 五根落地柱与水池 · 展示剖切', kind: 'cutaway', fov: 37 },
  'cutaway-east': { position: [43, 25, 40], target: [8.5, 3.8, 14], title: 'AXON / 东侧结构与回程梯 · 展示剖切', kind: 'east', fov: 39 },
  'plan': { position: [8.5, 51, 14.55], target: [8.5, 0, 14.5], title: 'PLAN / 北↑ · 黄入绿出 · 两格跳距', kind: 'plan', fov: 39 },
}

function visibleStructure(kind) {
  if (kind === 'full') return original
  const display = new Structure(original.getSize())
  for (const block of original.getBlocks()) {
    const [blockX, blockY, blockZ] = block.pos
    const blockName = block.state.getName().toString()
    if (blockY >= 11) continue
    if (blockName === 'minecraft:chain' || blockName === 'minecraft:lantern') continue
    if (kind === 'plan' && blockY >= 5) continue
    if (kind === 'cutaway' && ((blockX <= 1 && blockZ >= 4 && blockZ <= 25 && blockY >= 2) || ((blockX === 0 || blockZ === 0) && blockY >= 5))) continue
    if (kind === 'cutaway' && blockX === 1 && blockZ === 2 && blockY >= 5) continue
    if (kind === 'east' && ((blockX >= 15 && blockZ >= 4 && blockZ <= 25 && blockY >= 2) || ((blockX === 16 || blockZ === 28) && blockY >= 5))) continue
    display.addBlock(block.pos, block.state.getName(), block.state.getProperties())
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
  return { name, ...camera, original_records: original.getBlocks().length, displayed_records: visibleStructure(camera.kind).getBlocks().length }
}
const picker = document.getElementById('view-picker')
for (const [name, camera] of Object.entries(cameras)) picker.add(new Option(camera.title, name))
picker.addEventListener('change', () => showView(picker.value))
window.addEventListener('resize', () => showView(picker.value))
window.p05 = { cameras: Object.keys(cameras), showView }
await showView('cutaway')
window.p05Ready = true
