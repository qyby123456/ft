import { NbtFile, Structure, ThreeStructureRenderer, loadDefaultPackResources } from '@mattzh72/lodestone'

const canvas = document.querySelector('canvas')
const bytes = new Uint8Array(await (await fetch('/room.nbt')).arrayBuffer())
const original = Structure.fromNbt(NbtFile.read(bytes).root)
const pack = await loadDefaultPackResources({ baseUrl: new URL('/pack/', location.href).toString() })
const renderer = new ThreeStructureRenderer(canvas, original, pack.resources, {
  preserveDrawingBuffer: true, asyncBuild: true, asyncChunkBuildTimeMs: 8, chunkSize: 16,
  sunlight: { direction: [0.4, 0.85, -0.3], intensity: 1.0, ambientIntensity: 0.8, fillIntensity: 0.4, exposure: 1.03, fog: { density: 0 }, shadow: { enabled: true, intensity: 0.22 }, emissive: { range: 12, intensity: 1.2 } }
})
const definitions = {
  hero: { position: [6.5, 3.05, 1.2], target: [6.5, 3.05, 15], fov: 78, caption: '北向南 · 原模板内部一点透视 · 完整屋顶', cut: false },
  north: { position: [6.5, 20, -22], target: [6.5, 3, 8], fov: 47, caption: '北侧内部剖视 · 移除屋顶与北端近侧墙，仅供检查', cut: true },
  south: { position: [6.5, 20, 38], target: [6.5, 3, 8], fov: 47, caption: '南侧内部剖视 · 移除屋顶与南端近侧墙，仅供检查', cut: true },
  west: { position: [-23, 19, 8], target: [6.5, 3, 8], fov: 47, caption: '西侧内部剖视 · 移除屋顶与西墙，仅供检查', cut: true },
  east: { position: [36, 19, 8], target: [6.5, 3, 8], fov: 47, caption: '东侧内部剖视 · 移除屋顶与东墙，仅供检查', cut: true },
  envelope: { position: [-22, 24, -26], target: [6.5, 3.5, 8], fov: 46, caption: '完整外壳 · 封闭末地石屋顶 / 两端贯通开口', cut: false }
}
window.renderView = async name => {
  const view = definitions[name]
  if (!view) throw new Error('Unknown view: ' + name)
  let shown = original
  if (view.cut) {
    shown = new Structure(original.getSize())
    for (const entry of original.getBlocks()) {
      const [blockX, height, blockZ] = entry.pos
      if (height >= 8) continue
      if (height >= 1 && ((name === 'north' && blockZ === 0) || (name === 'south' && blockZ === 15) || (name === 'west' && blockX <= 2) || (name === 'east' && blockX >= 10))) continue
      shown.addBlock(entry.pos, entry.state.getName(), entry.state.getProperties(), entry.nbt)
    }
  }
  renderer.setStructure(shown)
  renderer.setViewport(0, 0, innerWidth, innerHeight, 1)
  await renderer.whenReady()
  renderer.setCamera({ position: view.position, target: view.target, fov: view.cut ? 35 : view.fov })
  renderer.drawStructure()
  await new Promise(resolve => requestAnimationFrame(resolve))
  renderer.drawStructure()
  document.querySelector('#caption').textContent = view.caption
  document.querySelector('#loading').hidden = true
  window.viewReady = name
  const exactStates = shown.getBlocks().every(entry => entry.state.toString() === original.getBlock(entry.pos).state.toString())
  if (!exactStates) throw new Error('Cutaway changed a retained block state')
  return { name, sourceBlocks: original.getBlocks().length, shownBlocks: shown.getBlocks().length, exactStates }
}
await window.renderView(new URLSearchParams(location.search).get('view') || 'hero')
