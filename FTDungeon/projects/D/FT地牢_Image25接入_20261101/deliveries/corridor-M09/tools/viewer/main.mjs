import { NbtFile, Structure, ThreeStructureRenderer, loadDefaultPackResources } from '@mattzh72/lodestone'

const canvas = document.querySelector('canvas')
const { resources } = await loadDefaultPackResources({ baseUrl: `${location.origin}/pack/` })
const bytes = new Uint8Array(await (await fetch('/room.nbt')).arrayBuffer())
const full = Structure.fromNbt(NbtFile.read(bytes).root)
const renderer = new ThreeStructureRenderer(canvas, full, resources, {
  chunkSize: 16, drawDistance: 1000, preserveDrawingBuffer: true,
  asyncBuild: true, asyncChunkBuildTimeMs: 10,
  sunlight: {
    direction: [0.35, 0.85, 0.25], intensity: 0.65, ambientIntensity: 0.72,
    fillIntensity: 0.5, rimIntensity: 0.2, exposure: 1.2,
    sky: { zenithColor: [0.065, 0.045, 0.1], horizonColor: [0.13, 0.09, 0.16], groundColor: [0.065, 0.045, 0.08] },
  },
})
const center = [7.5, 4, 20.5]
const directions = {
  north: [-20, 27, -42], south: [20, 27, 42],
  west: [-39, 27, 20], east: [39, 27, -20],
}
let lastView = 'hero'

function slice(structure, offset) {
  const result = new Structure(structure.getSize())
  for (const block of structure.getBlocks()) {
    const [posX, posY, posZ] = block.pos
    if (posY >= 10) continue
    if (offset[0] < 0 && posX <= 2 && posY >= 2) continue
    if (offset[0] > 0 && posX >= 12 && posY >= 2) continue
    if (offset[2] < 0 && posZ === 0 && posY >= 2) continue
    if (offset[2] > 0 && posZ === 40 && posY >= 2) continue
    result.addBlock(block.pos, block.state.getName(), block.state.getProperties(), block.nbt)
  }
  return result
}

async function setView(view, native = false) {
  lastView = view
  let source = full
  if (native) {
    const data = new Uint8Array(await (await fetch('/native.nbt')).arrayBuffer())
    source = Structure.fromNbt(NbtFile.read(data).root)
  }
  let position
  let target = center
  let selected = source
  let label
  if (view === 'hero' || view === 'reverse') {
    const reverse = view === 'reverse'
    position = [7.5, 3.7, reverse ? 38.2 : 2.8]
    target = [7.5, 3.95, reverse ? 0 : 40]
    label = reverse ? '南端向北 / 一字贯通轴线' : '北端向南 / 破拱、金纹与暖烛'
    renderer.setFOV(72)
  } else if (view === 'plan') {
    selected = slice(source, [-1, 1, -1])
    position = [7.5, 64, 20.49]
    target = [7.5, 0, 20.5]
    label = '实际 NBT 平面 / 入口北、出口南 / X=6…8 全长安全直路'
    renderer.setFOV(50)
  } else {
    const [kind, direction] = view.split('-')
    const offset = directions[direction]
    position = center.map((coordinate, index) => coordinate + offset[index])
    if (kind === 'interior') selected = slice(source, offset)
    label = `${direction.toUpperCase()} / ${kind === 'interior' ? '内视剖切：仅预览移除屋顶与近侧外墙' : '外观：完整封闭屋顶与侧墙'}`
    renderer.setFOV(48)
  }
  renderer.setViewport(0, 0, innerWidth, innerHeight, 1)
  renderer.setStructure(selected)
  await renderer.whenReady()
  renderer.lookAt(position, target)
  renderer.drawStructure()
  await new Promise(resolve => requestAnimationFrame(resolve))
  renderer.drawStructure()
  document.querySelector('#label').textContent = label
  document.querySelector('#evidence').textContent = `${native ? 'Forge 240 tick 原生回采' : '最终 room.nbt'} · 15 × 12 × 41 · NBT 软件渲染，非 AI 图或游戏截图`
  return { view, native, camera: { position, target }, source: native ? 'qa/native-placed-none.nbt' : 'room.nbt', cutaway: view.startsWith('interior') || view === 'plan' }
}
window.m09 = { setView, renderer, full, ready: true }
for (const button of document.querySelectorAll('button')) button.addEventListener('click', () => setView(button.dataset.view))
addEventListener('resize', () => setView(lastView))
await setView('hero')
document.querySelector('#loading').hidden = true
