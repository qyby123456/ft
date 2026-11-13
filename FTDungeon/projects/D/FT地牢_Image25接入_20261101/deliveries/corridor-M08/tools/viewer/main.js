import { NbtFile, Structure, ThreeStructureRenderer, loadDefaultPackResources } from '/@fs/C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'

const canvas = document.querySelector('#scene')
const bytes = new Uint8Array(await (await fetch('/__minecraft_builder__/build.nbt')).arrayBuffer())
const original = Structure.fromNbt(NbtFile.read(bytes).root)
const pack = await loadDefaultPackResources({ baseUrl: `${location.origin}/__minecraft_builder__/default-pack/` })
const renderer = new ThreeStructureRenderer(canvas, original, pack.resources, {
  preserveDrawingBuffer: true,
  antialias: true,
  chunkSize: 16,
  sunlight: {
    direction: [0.35, 0.85, 0.25], intensity: 0.6, ambientIntensity: 0.65,
    ambientColor: [0.82, 0.78, 0.74], fillIntensity: 0.23, rimIntensity: 0.22,
    exposure: 0.95, shadow: { enabled: false },
    emissive: { intensity: 0.48, range: 9, tint: [1.0, 0.5, 0.2] },
    postProcess: { bloom: { enabled: false } },
    sky: { zenithColor: [0.035, 0.025, 0.04], horizonColor: [0.11, 0.065, 0.05], groundColor: [0.045, 0.035, 0.04], stars: { enabled: false } },
    fog: { color: [0.09, 0.055, 0.04], density: 0, heightFalloff: 0 },
  },
})
const views = {
  'axial-north': { label: '北端入口 → 南端出口 · 完整 NBT', position: [8.5, 4.62, 3.0], target: [8.5, 4.62, 39.5], fov: 72 },
  'axial-south': { label: '南端出口 → 北端入口 · 完整 NBT', position: [8.5, 4.62, 37.8], target: [8.5, 4.62, 1.5], fov: 72 },
  'interior-north': { label: '北向内部 · 仅预览去顶 / 去近侧墙', position: [8.5, 29, -29], target: [8.5, 3.8, 20.5], cut: 'north' },
  'interior-east': { label: '东向内部 · 仅预览去顶 / 去近侧墙', position: [53, 29, 20.5], target: [8.5, 4, 20.5], cut: 'east' },
  'interior-south': { label: '南向内部 · 仅预览去顶 / 去近侧墙', position: [8.5, 29, 70], target: [8.5, 3.8, 20.5], cut: 'south' },
  'interior-west': { label: '西向内部 · 仅预览去顶 / 去近侧墙', position: [-36, 29, 20.5], target: [8.5, 4, 20.5], cut: 'west' },
  'exterior-north': { label: '北向外观 · 完整屋顶与外墙', position: [8.5, 29, -29], target: [8.5, 5, 20.5] },
  'exterior-east': { label: '东向外观 · 完整屋顶与外墙', position: [53, 29, 20.5], target: [8.5, 5, 20.5] },
  'exterior-south': { label: '南向外观 · 完整屋顶与外墙', position: [8.5, 29, 70], target: [8.5, 5, 20.5] },
  'exterior-west': { label: '西向外观 · 完整屋顶与外墙', position: [-36, 29, 20.5], target: [8.5, 5, 20.5] },
}

function cutaway(direction) {
  const result = new Structure(original.getSize())
  for (const block of original.getBlocks()) {
    const [column, height, depth] = block.pos
    if (height >= 10) continue
    if (height >= 3 && ((direction === 'north' && depth <= 1) || (direction === 'south' && depth >= 39)
      || (direction === 'east' && column >= 14) || (direction === 'west' && column <= 2))) continue
    result.addBlock(block.pos, block.state.getName(), block.state.getProperties())
  }
  return result
}

async function show(name) {
  const view = views[name]
  if (!view) throw new Error(`Unknown view ${name}`)
  renderer.setStructure(view.cut ? cutaway(view.cut) : original)
  renderer.setViewport(0, 0, canvas.clientWidth, canvas.clientHeight, 1)
  renderer.setCamera({ position: view.position, target: view.target, fov: view.fov ?? 49 })
  await renderer.whenReady()
  renderer.drawStructure()
  document.querySelector('#view-label').textContent = view.label
  window.currentView = { name, ...view, nbtBytes: bytes.length, records: original.getBlocks().length }
  return window.currentView
}

window.m08 = { show, views: Object.keys(views) }
for (const button of document.querySelectorAll('[data-view]')) button.onclick = () => show(button.dataset.view)
await show('axial-north')
window.m08Ready = true
