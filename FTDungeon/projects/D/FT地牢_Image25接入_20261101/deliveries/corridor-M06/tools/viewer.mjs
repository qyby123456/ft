import { NbtFile, Structure, ThreeStructureRenderer, loadDefaultPackResources } from '@mattzh72/lodestone'

const bytes = new Uint8Array(await (await fetch('/__minecraft_builder__/build.nbt')).arrayBuffer())
const structure = Structure.fromNbt(NbtFile.read(bytes).root)
window.m06Stage = 'nbt-loaded'
const pack = await loadDefaultPackResources({ baseUrl: new URL('/__minecraft_builder__/default-pack/', location.href).toString() })
window.m06Stage = 'resources-loaded'
const canvas = document.getElementById('scene')
const coldLighting = { color: [0.72, 0.85, 1], intensity: 0.55, ambientColor: [0.34, 0.48, 0.7], ambientIntensity: 0.45, fillColor: [0.25, 0.4, 0.6], fillIntensity: 0.12, rimColor: [0.3, 0.45, 0.65], rimIntensity: 0.15, exposure: 1, emissive: { intensity: 0.65, range: 9, tint: [1, 0.85, 0.65] }, shadow: { enabled: false }, sky: { zenithColor: [0.05, 0.09, 0.15], horizonColor: [0.12, 0.18, 0.25], groundColor: [0.08, 0.12, 0.17], sunGlowIntensity: 0 }, disc: { coreIntensity: 0, glowIntensity: 0 } }
const reviewLighting = { ...coldLighting, color: [0.8, 0.9, 1], intensity: 0.85, ambientColor: [0.7, 0.8, 0.95], ambientIntensity: 0.85, fillIntensity: 0.2, exposure: 1.1, emissive: { intensity: 0.3, range: 6, tint: [1, 0.85, 0.65] } }
const renderer = new ThreeStructureRenderer(canvas, structure, pack.resources, { chunkSize: 16, drawDistance: 1000, useInvisibleBlockBuffer: false, asyncBuild: false, preserveDrawingBuffer: true, antialias: false, sunlight: coldLighting })
window.m06Stage = 'renderer-created'
renderer.setViewport(0, 0, window.innerWidth, window.innerHeight, 1)
const directions = {
  north: { position: [-22, 26, -32], target: [7.5, 5, 19] },
  south: { position: [37, 26, 72], target: [7.5, 5, 21] },
  west: { position: [-48, 32, 5], target: [7.5, 4, 20] },
  east: { position: [63, 32, 35], target: [7.5, 4, 20] },
  hero: { position: [7.5, 3.62, 0.7], target: [7.5, 3.62, 39] },
  reverse: { position: [7.5, 3.62, 39.3], target: [7.5, 3.62, 0] },
}
window.showView = async (direction = 'hero', cutaway = false) => {
  const view = directions[direction]
  const visible = new Structure(structure.getSize())
  for (const block of structure.getBlocks()) {
    const [across, height, depth] = block.pos
    const name = block.state.getName().toString()
    if (name === 'minecraft:air') continue
    if (cutaway && height >= 11) continue
    if (cutaway && height >= 2 && direction === 'north' && (depth === 0 || across <= 4)) continue
    if (cutaway && height >= 2 && direction === 'south' && (depth === 39 || across >= 10)) continue
    if (cutaway && height >= 2 && direction === 'west' && across <= 4) continue
    if (cutaway && height >= 2 && direction === 'east' && across >= 10) continue
    visible.addBlock(block.pos, name, block.state.getProperties(), block.nbt)
  }
  renderer.setStructure(visible)
  renderer.setSunlight(direction === 'hero' || direction === 'reverse' ? coldLighting : reviewLighting)
  window.m06Stage = 'structure-set-' + direction
  renderer.setFOV(direction === 'hero' || direction === 'reverse' ? 72 : 40)
  renderer.lookAt(view.position, view.target)
  await renderer.whenReady()
  window.m06Stage = 'before-draw-' + direction
  renderer.drawStructure()
  window.m06Stage = 'drawn-' + direction
  document.getElementById('label').textContent = `M06 · ${direction.toUpperCase()} · ${cutaway ? 'CUTAWAY / roof removed in preview only' : 'COMPLETE NBT'} · 15 × 13 × 40`
  window.renderStatus = { direction, cutaway, visible: visible.getBlocks().length, fullRecords: structure.getBlocks().length }
  return window.renderStatus
}
await window.showView('hero', false)
window.m06Ready = true
