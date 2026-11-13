import { NbtFile, Structure, ThreeStructureRenderer, loadDefaultPackResources } from '/@fs/C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'

document.head.insertAdjacentHTML('beforeend', `<style>
  *{box-sizing:border-box}body{margin:0;background:#101a20;color:#e4dac5;font-family:Arial,"Microsoft YaHei",sans-serif}
  header{height:82px;padding:16px 28px;display:flex;align-items:center;justify-content:space-between;background:#15252c;border-bottom:1px solid #395052}
  h1{font-size:23px;margin:0 0 7px;color:#e3c18a}p{margin:0;font-size:13px;color:#a9bdba}#view{font-size:17px;font-weight:600;color:#a4d9cd}select{background:#20363c;border:1px solid #597b76;color:#d8dfcd;padding:6px;margin-left:12px}
  canvas{display:block;width:100vw;height:calc(100vh - 126px)}footer{height:44px;padding:12px 28px;font-size:12px;color:#b6c5be;background:#15252c}
</style>`)
document.body.innerHTML = `<header><div><h1>S20 · 废弃矿道 / 浅水短廊</h1><p>MINE TIMBERS · BROKEN ARCHES · SEALED WATER · 11 × 9 × 16</p></div><div><span id="view">Loading actual NBT…</span><select id="view-picker" aria-label="切换查看方向"></select></div></header><canvas id="structure"></canvas><footer>实际 NBT · Lodestone 软件预览，非实机截图 / 完整屋顶仅在剖视图中隐藏 / 北南两端 3 × 3 同高接口</footer>`

const [bytes, pack] = await Promise.all([
  fetch('/__minecraft_builder__/build.nbt').then(response => response.arrayBuffer()),
  loadDefaultPackResources({ baseUrl: new URL('/__minecraft_builder__/default-pack/', location.href).toString() }),
])
const original = Structure.fromNbt(NbtFile.read(new Uint8Array(bytes)).root)
const canvas = document.getElementById('structure')
const renderer = new ThreeStructureRenderer(canvas, original, pack.resources, {
  preserveDrawingBuffer: true, antialias: true, asyncBuild: false, drawDistance: 1000,
  sunlight: {
    direction: [0.35, 0.85, -0.25], intensity: 0.9, ambientIntensity: 0.72,
    fillIntensity: 0.32, rimIntensity: 0.2, exposure: 1.04,
    sky: { zenithColor: [0.065, 0.11, 0.14], horizonColor: [0.14, 0.2, 0.22], groundColor: [0.07, 0.1, 0.12], stars: { enabled: false } },
  },
})
const center = [5.5, 3.6, 8]
const cameras = {
  'exterior-north': { position: [-16, 17, -23], target: center, title: 'N / 北外视 · 完整封顶', kind: 'full' },
  'exterior-east': { position: [35, 17, -14], target: center, title: 'E / 东外视 · 密封侧墙', kind: 'full' },
  'exterior-south': { position: [27, 17, 39], target: center, title: 'S / 南外视 · 双向贯通', kind: 'full' },
  'exterior-west': { position: [-24, 17, 30], target: center, title: 'W / 西外视 · 完整封顶', kind: 'full' },
  'interior-north': { position: [5.5, 3.65, 1.3], target: [5.5, 3.6, 15.6], title: 'N → S / 北口内视 · 原 NBT', kind: 'full', fov: 76 },
  'interior-south': { position: [5.5, 3.65, 14.7], target: [5.5, 3.6, 0.4], title: 'S → N / 南口内视 · 原 NBT', kind: 'full', fov: 76 },
  'interior-east': { position: [27, 15, 8], target: center, title: 'E / 东侧内视 · 仅展示剖切', kind: 'east' },
  'interior-west': { position: [-16, 15, 8], target: center, title: 'W / 西侧内视 · 仅展示剖切', kind: 'west' },
  'cutaway': { position: [-13, 18, -12], target: [5.5, 2.2, 8], title: 'AXON / 内部构造 · 展示剖切', kind: 'cutaway', fov: 36 },
  'plan': { position: [5.5, 31, 8.05], target: [5.5, 0, 8], title: 'PLAN / 屋顶隐藏 · 干燥轨道与双水槽', kind: 'plan', fov: 36 },
}

function visibleStructure(kind) {
  if (kind === 'full') return original
  const display = new Structure(original.getSize())
  for (const block of original.getBlocks()) {
    const [blockX, blockY, blockZ] = block.pos
    if (blockY >= 7) continue
    if (kind === 'east' && blockX === 10 && blockY >= 2) continue
    if (kind === 'west' && blockX === 0 && blockY >= 2) continue
    if (kind === 'cutaway' && ((blockZ === 0 || blockX === 0) && blockY >= 2)) continue
    if (kind === 'plan' && blockY >= 2 && !block.state.is('minecraft:rail')) continue
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
  renderer.setCamera({ position: camera.position, target: camera.target, fov: camera.fov ?? 30 })
  await renderer.whenReady()
  renderer.drawStructure()
  await new Promise(resolve => requestAnimationFrame(resolve))
  renderer.drawStructure()
  document.getElementById('view').textContent = camera.title
  document.getElementById('view-picker').value = name
  window.currentS20View = name
  return { name, ...camera, original_records: original.getBlocks().length, display_records: visibleStructure(camera.kind).getBlocks().length }
}
window.s20 = { showView, cameras: Object.keys(cameras), renderer, original }
const picker = document.getElementById('view-picker')
for (const [name, camera] of Object.entries(cameras)) picker.add(new Option(camera.title, name))
picker.addEventListener('change', () => showView(picker.value))
window.addEventListener('resize', () => showView(window.currentS20View))
await showView(cameras[location.hash.slice(1)] ? location.hash.slice(1) : 'interior-north')
window.s20Ready = true
