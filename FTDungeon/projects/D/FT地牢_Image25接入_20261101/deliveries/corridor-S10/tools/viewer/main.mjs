import {NbtFile, Structure, ThreeStructureRenderer, loadDefaultPackResources} from '@mattzh72/lodestone'

const canvas = document.querySelector('canvas')
const caption = document.querySelector('#caption')
const [response, pack] = await Promise.all([fetch('/room.nbt'), loadDefaultPackResources({baseUrl: new URL('/pack/', location.href).toString()})])
const bytes = new Uint8Array(await response.arrayBuffer())
const structure = Structure.fromNbt(NbtFile.read(bytes).root)
const hashBuffer = await crypto.subtle.digest('SHA-256', bytes)
const sourceHash = Array.from(new Uint8Array(hashBuffer), value => value.toString(16).padStart(2, '0')).join('')
const renderer = new ThreeStructureRenderer(canvas, structure, pack.resources, {
  chunkSize: 16, drawDistance: 1000, useInvisibleBlockBuffer: false, preserveDrawingBuffer: true, antialias: true,
  sunlight: {
    direction: [0.4, 0.85, -0.25], intensity: 1.0, ambientIntensity: 0.8, fillIntensity: 0.45, rimIntensity: 0.2, exposure: 1.03,
    sky: {zenithColor: [0.055, 0.085, 0.11], horizonColor: [0.15, 0.21, 0.24], groundColor: [0.08, 0.11, 0.13], stars: {enabled: false}},
    fog: {enabled: false, color: [0.15, 0.21, 0.24]}
  }
})
renderer.setViewport(0, 0, innerWidth, innerHeight, 1)
const cameras = {
  preview: {position: [6.5, 4.25, 0.15], target: [6.5, 4.5, 15], fov: 72},
  'interior-north': {position: [6.5, 23, -29], target: [6.5, 4.5, 8], fov: 32},
  'interior-south': {position: [6.5, 23, 45], target: [6.5, 4.5, 8], fov: 32},
  'interior-west': {position: [-30, 23, 8], target: [6.5, 4, 8], fov: 32},
  'interior-east': {position: [43, 23, 8], target: [6.5, 4, 8], fov: 32},
  'exterior-north': {position: [6.5, 23, -29], target: [6.5, 4.5, 8], fov: 32},
  'exterior-south': {position: [6.5, 23, 45], target: [6.5, 4.5, 8], fov: 32},
  'exterior-west': {position: [-30, 23, 8], target: [6.5, 4, 8], fov: 32},
  'exterior-east': {position: [43, 23, 8], target: [6.5, 4, 8], fov: 32}
}
function provider(view) {
  if (!view.startsWith('interior-')) return structure
  const side = view.slice(9)
  const visible = pos => pos[1] < 10 && !(side === 'north' && pos[2] < 1) && !(side === 'south' && pos[2] > 14) && !(side === 'west' && pos[0] < 2) && !(side === 'east' && pos[0] > 10)
  return {
    getSize: () => structure.getSize(),
    getBlocks: () => structure.getBlocks().filter(block => visible(block.pos)),
    getBlock: pos => visible(pos) ? structure.getBlock(pos) : null
  }
}
window.renderView = async view => {
  if (!cameras[view]) throw new Error(`Unknown view ${view}`)
  const selection = provider(view)
  renderer.setStructure(selection)
  await renderer.whenReady()
  renderer.setCamera(cameras[view])
  renderer.drawStructure()
  await new Promise(resolve => requestAnimationFrame(resolve))
  renderer.drawStructure()
  caption.innerHTML = `S10 · COPPER MACHINERY / ${view.toUpperCase().replace('-', ' ')}<small>13 × 11 × 16 · Java 1.20.1 · ${view.startsWith('interior-') ? 'render-only cutaway · final roof intact' : 'actual final NBT · complete geometry'}<br>SHA-256 ${sourceHash.slice(0, 20)}…</small>`
  return {view, sourceHash, storedRecords: structure.getBlocks().length, visibleRecords: selection.getBlocks().length, camera: cameras[view]}
}
window.previewReady = true
window.previewHash = sourceHash
await window.renderView('preview')
