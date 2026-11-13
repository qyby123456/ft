import { NbtFile, Structure, ThreeStructureRenderer, loadDefaultPackResources } from '@mattzh72/lodestone'

const bytes = new Uint8Array(await (await fetch('/__minecraft_builder__/build.nbt')).arrayBuffer())
const structure = Structure.fromNbt(NbtFile.read(bytes).root)
window.a05Stage = 'nbt-loaded'
const pack = await loadDefaultPackResources({ baseUrl: new URL('/__minecraft_builder__/default-pack/', location.href).toString() })
window.a05Stage = 'resources-loaded'
const canvas = document.getElementById('scene')
const lighting = { color: [0.95, 0.89, 0.8], intensity: 0.85, ambientColor: [0.8, 0.83, 0.9], ambientIntensity: 0.85, fillColor: [0.5, 0.6, 0.75], fillIntensity: 0.2, rimColor: [0.6, 0.65, 0.75], rimIntensity: 0.15, exposure: 1.05, emissive: { intensity: 0.4, range: 7, tint: [1, 0.72, 0.38] }, shadow: { enabled: false }, sky: { zenithColor: [0.08, 0.09, 0.13], horizonColor: [0.15, 0.16, 0.21], groundColor: [0.1, 0.1, 0.13], sunGlowIntensity: 0 }, disc: { coreIntensity: 0, glowIntensity: 0 } }
const renderer = new ThreeStructureRenderer(canvas, structure, pack.resources, { chunkSize: 16, drawDistance: 1000, useInvisibleBlockBuffer: false, asyncBuild: false, preserveDrawingBuffer: true, antialias: false, sunlight: lighting })
renderer.setViewport(0, 0, window.innerWidth, window.innerHeight, 1)
const directions = {
  north: { position: [-22.8, 33, -24.7], target: [9.5, 3.8499999999999996, 9.5], remove: (across, depth) => across < 1 || depth < 1 },
  south: { position: [43.699999999999996, 33, 43.699999999999996], target: [9.5, 3.8499999999999996, 9.5], remove: (across, depth) => across >= 18 || depth >= 18 },
  west: { position: [-24.7, 33, 43.699999999999996], target: [9.5, 3.8499999999999996, 9.5], remove: (across, depth) => across < 1 || depth >= 18 },
  east: { position: [43.699999999999996, 33, -24.7], target: [9.5, 3.8499999999999996, 9.5], remove: (across, depth) => across >= 18 || depth < 1 },
  hero: { position: [10.5, 3.62, 0.6], target: [10.5, 4.3, 19] },
  reverse: { position: [10.5, 3.62, 18.6], target: [10.5, 4.3, 0] },
}
window.showView = async (direction = 'north', cutaway = true) => {
  const view = directions[direction]
  const visible = new Structure(structure.getSize())
  for (const block of structure.getBlocks()) {
    const [across, height, depth] = block.pos
    const name = block.state.getName().toString()
    if (name === 'minecraft:air') continue
    if (cutaway && height >= 9 && !name.endsWith('_log') && !name.endsWith('_wood') && !name.includes('chain')) continue
    if (cutaway && height >= 2 && view.remove(across, depth)) continue
    visible.addBlock(block.pos, name, block.state.getProperties(), block.nbt)
  }
  renderer.setStructure(visible)
  renderer.setSunlight(lighting)
  renderer.setFOV(direction === 'hero' || direction === 'reverse' ? 78 : 38)
  renderer.lookAt(view.position, view.target)
  await renderer.whenReady()
  renderer.drawStructure()
  document.getElementById('label').textContent = `zone-C02 · ${direction.toUpperCase()} · ${cutaway ? 'CUTAWAY / roof hidden in preview only' : 'COMPLETE NBT'} · 19 × 11 × 19`
  window.a05Stage = 'drawn-' + direction
  window.renderStatus = { direction, cutaway, visible: visible.getBlocks().length, fullRecords: structure.getBlocks().length }
  return window.renderStatus
}
await window.showView('north', true)
window.a05Ready = true
