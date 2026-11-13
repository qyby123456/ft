import { NbtFile, Structure, ThreeStructureRenderer, loadDefaultPackResources } from '@mattzh72/lodestone'

const parameters = new URLSearchParams(location.search)
if (parameters.has('capture')) document.body.classList.add('capture')
const canvas = document.querySelector('canvas')
const views = {
  'cutaway-north': { label: '北侧内部剖视', position: [46, 36, -38], target: [14.5, 4, 17.5], fov: 45, cut: 'north' },
  'cutaway-south': { label: '南侧内部剖视', position: [-17, 36, 73], target: [14.5, 4, 17.5], fov: 45, cut: 'south' },
  'cutaway-west': { label: '西侧内部剖视', position: [-42, 36, -14], target: [14.5, 4, 17.5], fov: 45, cut: 'west' },
  'cutaway-east': { label: '东侧内部剖视', position: [71, 36, 49], target: [14.5, 4, 17.5], fov: 45, cut: 'east' },
  'exterior-north': { label: '完整外壳 · 北', position: [47, 35, -41], target: [14.5, 6, 17.5], fov: 45 },
  'exterior-south': { label: '完整外壳 · 南', position: [-18, 35, 76], target: [14.5, 6, 17.5], fov: 45 },
  'exterior-west': { label: '完整外壳 · 西', position: [-45, 35, -15], target: [14.5, 6, 17.5], fov: 45 },
  'exterior-east': { label: '完整外壳 · 东', position: [74, 35, 50], target: [14.5, 6, 17.5], fov: 45 },
  'inside-south': { label: '完整屋顶内景 · 面向祭台', position: [14.5, 5.2, 3.7], target: [14.5, 6.4, 31.5], fov: 88 },
  'inside-north': { label: '完整屋顶内景 · 面向入口', position: [14.5, 6.2, 29.5], target: [14.5, 6, 2], fov: 88 }
}
for (const [key, view] of Object.entries(views)) document.querySelector('select').add(new Option(view.label, key))
const bytes = new Uint8Array(await (await fetch('/room.nbt')).arrayBuffer())
const source = Structure.fromNbt(NbtFile.read(bytes).root)
const pack = await loadDefaultPackResources({ baseUrl: new URL('/pack/', location.href).toString() })
const lighting = {
  direction: [0.4, 0.85, 0.3], intensity: 0.7, ambientIntensity: 0.68, fillIntensity: 0.35,
  rimIntensity: 0.12, exposure: 1.1, color: [1, 0.93, 0.82], ambientColor: [0.8, 0.77, 0.72],
  fillColor: [0.65, 0.6, 0.55], rimColor: [0.85, 0.74, 0.6],
  sky: { zenithColor: [0.09, 0.1, 0.13], horizonColor: [0.17, 0.17, 0.19], groundColor: [0.09, 0.09, 0.1], stars: { enabled: false } },
  emissive: { range: 12, intensity: 1.1, tint: [1, 0.7, 0.35] },
  fog: { color: [0.1, 0.095, 0.09], density: 0.00001 }, shadow: { enabled: true, intensity: 0.23, softness: 0.8 }
}
let renderer
let currentCut
window.h03 = { ready: false, actualRecords: source.getBlocks().length, views: Object.keys(views) }
async function showView(key) {
  const view = views[key]
  if (!view) throw new Error(`Unknown view ${key}`)
  const cut = view.cut ?? 'none'
  let displayedRecords
  if (!renderer || currentCut !== cut) {
    renderer?.dispose()
    let displayed = source
    if (cut !== 'none') {
      displayed = new Structure(source.getSize())
      for (const block of source.getBlocks()) {
        const [blockX, blockY, blockZ] = block.pos
        const nearNorth = (cut === 'north' || cut === 'west') && blockZ <= 1
        const nearSouth = (cut === 'south' || cut === 'east') && blockZ >= 33
        const nearWest = (cut === 'west' || cut === 'south') && blockX <= 1
        const nearEast = (cut === 'east' || cut === 'north') && blockX >= 27
        if (blockY >= 14 || blockY >= 2 && (nearNorth || nearSouth || nearWest || nearEast)) continue
        displayed.addBlock(block.pos, block.state.getName(), block.state.getProperties(), block.nbt)
      }
    }
    displayedRecords = displayed.getBlocks().length
    renderer = new ThreeStructureRenderer(canvas, displayed, pack.resources, { preserveDrawingBuffer: true, antialias: true, chunkSize: 16, sunlight: lighting, asyncBuild: true })
    await renderer.whenReady()
    currentCut = cut
    window.h03.displayedRecords = displayedRecords
  }
  const bounds = canvas.getBoundingClientRect()
  renderer.setViewport(0, 0, bounds.width, bounds.height, Math.min(devicePixelRatio, 1.5))
  renderer.setCamera(view)
  renderer.drawStructure()
  document.querySelector('select').value = key
  document.querySelector('#loading').style.display = 'none'
  window.h03.current = key
  window.h03.ready = true
  return { view: key, sourceRecords: source.getBlocks().length, displayedRecords: window.h03.displayedRecords, cutaway: cut !== 'none', removed: cut === 'none' ? [] : ['roof y>=14', 'two near walls above floor only'], camera: renderer.getCamera() }
}
window.h03.showView = showView
document.querySelector('select').onchange = event => showView(event.target.value)
window.addEventListener('resize', () => showView(window.h03.current))
await showView(parameters.get('view') ?? 'cutaway-north')
