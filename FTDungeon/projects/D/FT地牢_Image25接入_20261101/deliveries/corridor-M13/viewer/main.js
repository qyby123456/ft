import {NbtFile, Structure, ThreeStructureRenderer, loadDefaultPackResources} from '/@fs/C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'

const canvas = document.getElementById('preview')
const [bytes, pack, metadata] = await Promise.all([
  fetch('/room.nbt').then(response => response.arrayBuffer()),
  loadDefaultPackResources({baseUrl: new URL('/pack/', window.location.href).toString()}),
  fetch('/room.json').then(response => response.json())
])
const structure = Structure.fromNbt(NbtFile.read(new Uint8Array(bytes)).root)
const renderer = new ThreeStructureRenderer(canvas, structure, pack.resources, {
  drawDistance: 1000, preserveDrawingBuffer: true, asyncBuild: true,
  sunlight: {
    direction: [-0.5, 0.85, -0.4], color: [0.88, 0.91, 0.82],
    ambientColor: [0.62, 0.7, 0.59], fillColor: [0.45, 0.54, 0.43],
    ambientIntensity: 0.48, intensity: 0.38, fillIntensity: 0.22, exposure: 1,
    sky: {zenithColor: [0.055, 0.08, 0.065], horizonColor: [0.07, 0.1, 0.08], groundColor: [0.04, 0.06, 0.045], sunGlowIntensity: 0, stars: {enabled: false}},
    disc: {coreIntensity: 0, glowIntensity: 0}, fog: {color: [0.055, 0.08, 0.065], density: 0},
    emissive: {intensity: 1.05, range: 10, tint: [1, 0.79, 0.48]}
  }
})
const views = {
  'interior-north': {label: '北入口 · 向南直视', position: [6.5, 2.65, 1.2], target: [6.5, 3.25, 38], fov: 76},
  'interior-south': {label: '南出口 · 向北回望', position: [6.5, 2.65, 38.8], target: [6.5, 3.25, 2], fov: 76},
  'interior-west': {label: '西侧剖切 · 连续拱列', position: [-30, 24, 9], target: [6.5, 3.6, 20], fov: 52, cut: 'west'},
  'interior-east': {label: '东侧剖切 · 灯链与木嵌墙', position: [43, 24, 31], target: [6.5, 3.6, 20], fov: 52, cut: 'east'},
  'exterior-north': {label: '北面外观 · 完整结构', position: [0, 17, -28], target: [6.5, 4.8, 14], fov: 54},
  'exterior-south': {label: '南面外观 · 完整结构', position: [13, 17, 68], target: [6.5, 4.8, 26], fov: 54},
  'exterior-west': {label: '西面外观 · 完整屋顶', position: [-35, 25, 11], target: [6.5, 4.8, 20], fov: 54},
  'exterior-east': {label: '东面外观 · 完整屋顶', position: [48, 25, 29], target: [6.5, 4.8, 20], fov: 54},
  'overview-cutaway': {label: '西北剖切总览 · 真实 NBT', position: [-22, 25, -14], target: [6.5, 3.2, 19], fov: 58, cut: 'west'}
}
function visible(block, cut) {
  if (!cut) return true
  const [blockX, blockY, blockZ] = block.pos
  if (blockY === 10) return false
  if (blockY > 0 && (cut === 'west' ? blockX <= 1 : blockX >= 11)) return false
  if (blockY >= 8 && (blockX <= 3 || blockX >= 9) && ![0, 8, 16, 24, 32, 39].includes(blockZ)) return false
  if (blockY > 0 && blockZ === (cut === 'west' ? 0 : 39)) return false
  return true
}
async function setView(name) {
  const view = views[name]
  if (!view) throw new Error('Unknown view ' + name)
  let provider = structure
  if (view.cut) {
    const blocks = structure.getBlocks().filter(block => visible(block, view.cut))
    const lookup = new Map(blocks.map(block => [block.pos.join(','), block]))
    provider = {getSize: () => structure.getSize(), getBlocks: () => blocks, getBlock: position => lookup.get(position.join(',')) ?? null}
  }
  renderer.setStructure(provider)
  const rectangle = canvas.getBoundingClientRect()
  renderer.setViewport(0, 0, rectangle.width, rectangle.height, 1)
  renderer.setCamera(view)
  await renderer.whenReady()
  renderer.drawStructure()
  await new Promise(resolve => requestAnimationFrame(() => {renderer.drawStructure(); resolve()}))
  document.getElementById('view-title').textContent = view.label
  document.getElementById('provenance').textContent = `真实 room.nbt · SHA-256 ${metadata.sha256.slice(0, 16)}… · Lodestone 软件渲染，非游戏客户端${view.cut ? '；已剖切显示，NBT 保持完整屋顶。' : '；未剖切。'}`
  return {name, ...view, cutaway: Boolean(view.cut), sha256: metadata.sha256}
}
window.m13QA = {setView, views, metadata, structure, renderer, ready: false}
await setView(new URLSearchParams(window.location.search).get('view') || 'interior-north')
window.m13QA.ready = true
document.getElementById('loading').remove()
