import { NbtFile, Structure, ThreeStructureRenderer, loadDefaultPackResources } from '@mattzh72/lodestone'

const parameters = new URLSearchParams(location.search)
if (parameters.has('capture')) document.body.classList.add('capture')
const canvas = document.querySelector('canvas')
const views = {
  'interior-south': { label: '内景 · 向南贯通', position: [9.5, 3.65, 2.9], target: [9.5, 3.85, 37], fov: 74 },
  'interior-north': { label: '内景 · 向北贯通', position: [9.5, 3.65, 37.1], target: [9.5, 3.85, 3], fov: 74 },
  'interior-west': { label: '内景 · 西侧铁狱', position: [10.8, 4.0, 12.8], target: [1.5, 3.5, 14.2], fov: 75 },
  'interior-east': { label: '内景 · 东侧铁狱', position: [8.2, 4.0, 26.8], target: [17.5, 3.5, 28.2], fov: 75 },
  'exterior-north': { label: '完整外景 · 北', position: [-12, 21, -27], target: [9.5, 5, 18], fov: 43 },
  'exterior-south': { label: '完整外景 · 南', position: [31, 21, 67], target: [9.5, 5, 22], fov: 43 },
  'exterior-west': { label: '完整外景 · 西', position: [-39, 25, 5], target: [9.5, 5, 20], fov: 43 },
  'exterior-east': { label: '完整外景 · 东', position: [58, 25, 35], target: [9.5, 5, 20], fov: 43 },
  cutaway: { label: '剖切总览 · 仅预览裁切', position: [-31, 36, -28], target: [9.5, 2.5, 19], fov: 49, cutaway: true }
}
for (const [value, view] of Object.entries(views)) document.querySelector('select').add(new Option(view.label, value))
const bytes = new Uint8Array(await (await fetch('/room.nbt')).arrayBuffer())
const structure = Structure.fromNbt(NbtFile.read(bytes).root)
const pack = await loadDefaultPackResources({ baseUrl: new URL('/pack/', location.href).toString() })
const lighting = {
  direction: [0.3, 0.85, 0.4], intensity: 0.55, ambientIntensity: 0.58, fillIntensity: 0.3,
  rimIntensity: 0.1, exposure: 1.03, color: [1, 0.96, 0.9], ambientColor: [0.75, 0.73, 0.7],
  fillColor: [0.62, 0.58, 0.5], rimColor: [0.75, 0.7, 0.61],
  sky: { zenithColor: [0.08, 0.09, 0.105], horizonColor: [0.12, 0.13, 0.15], groundColor: [0.07, 0.075, 0.085], stars: { enabled: false } },
  emissive: { range: 12, intensity: 1.1, tint: [1, 0.67, 0.32] },
  fog: { color: [0.08, 0.075, 0.07], density: 0.00001 }, shadow: { enabled: true, intensity: 0.23, softness: 0.8 }
}
let renderer
let renderedCutaway
window.m05 = { ready: false, source: '/room.nbt', actualRecords: structure.getBlocks().length, views: Object.keys(views) }
async function showView(key) {
  const view = views[key]
  if (!view) throw new Error(`Unknown view ${key}`)
  if (!renderer || renderedCutaway !== Boolean(view.cutaway)) {
    renderer?.dispose()
    let displayed = structure
    if (view.cutaway) {
      displayed = new Structure(structure.getSize())
      for (const block of structure.getBlocks()) {
        const [blockX, blockY, blockZ] = block.pos
        if (blockY >= 8 || blockX === 0 || blockZ <= 1 && blockY >= 2) continue
        displayed.addBlock(block.pos, block.state.getName(), block.state.getProperties(), block.nbt)
      }
    }
    renderer = new ThreeStructureRenderer(canvas, displayed, pack.resources, { preserveDrawingBuffer: true, antialias: true, chunkSize: 16, sunlight: lighting, asyncBuild: true })
    await renderer.whenReady()
    renderedCutaway = Boolean(view.cutaway)
  }
  const bounds = canvas.getBoundingClientRect()
  renderer.setViewport(0, 0, bounds.width, bounds.height, Math.min(devicePixelRatio, 1.5))
  renderer.setCamera(view)
  renderer.drawStructure()
  document.querySelector('select').value = key
  document.querySelector('#loading').style.display = 'none'
  window.m05.current = key
  window.m05.ready = true
  window.m05.displayed = renderer.getCamera()
  return { view: key, sourceRecords: structure.getBlocks().length, cutaway: Boolean(view.cutaway) }
}
window.m05.showView = showView
document.querySelector('select').onchange = event => showView(event.target.value)
window.addEventListener('resize', () => showView(window.m05.current))
await showView(parameters.get('view') ?? 'interior-south')
