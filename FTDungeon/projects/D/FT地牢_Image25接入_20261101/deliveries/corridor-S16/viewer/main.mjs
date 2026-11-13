import { NbtFile, Structure, ThreeStructureRenderer, loadDefaultPackResources } from '@mattzh72/lodestone'

const views = {
  perspective: { label: '北入口 · 纵深', button: '纵深', position: [5.5, 2.65, 0.3], target: [5.5, 3.0, 16.5], fov: 73 },
  reverse: { label: '南出口 · 反向纵深', button: '反向', position: [5.5, 2.65, 15.7], target: [5.5, 3.0, -1], fov: 73 },
  north: { label: '北向内部剖视', button: '北剖', position: [5.5, 19, -23], target: [5.5, 3.0, 7.8], fov: 39, cut: block => block.pos[1] >= 8 || block.pos[2] === 0 && block.pos[1] > 0 },
  south: { label: '南向内部剖视', button: '南剖', position: [5.5, 19, 39], target: [5.5, 3.0, 8.2], fov: 39, cut: block => block.pos[1] >= 8 || block.pos[2] === 15 && block.pos[1] > 0 },
  west: { label: '西向内部剖视', button: '西剖', position: [-24, 17, 8], target: [5.5, 3.2, 8], fov: 40, cut: block => block.pos[1] >= 8 || block.pos[0] <= 2 && block.pos[1] > 0 },
  east: { label: '东向内部剖视', button: '东剖', position: [35, 17, 8], target: [5.5, 3.2, 8], fov: 40, cut: block => block.pos[1] >= 8 || block.pos[0] >= 8 && block.pos[1] > 0 },
  exterior: { label: '完整外壳 · 屋顶未剖切', button: '外壳', position: [24, 21, -22], target: [5.5, 3.6, 8], fov: 40 },
  roof: { label: '完整顶板 · 连续覆盖', button: '屋顶', position: [5.5, 40, 8.01], target: [5.5, 0, 8], fov: 39 },
}
const metadata = await (await fetch('/metadata.json')).json()
const buffer = new Uint8Array(await (await fetch('/build.nbt')).arrayBuffer())
const original = Structure.fromNbt(NbtFile.read(buffer).root)
const pack = await loadDefaultPackResources({ baseUrl: new URL('/default-pack/', location.href).toString() })
const canvas = document.getElementById('scene')
const renderer = new ThreeStructureRenderer(canvas, original, pack.resources, {
  preserveDrawingBuffer: true, asyncBuild: false, antialias: true, useInvisibleBlockBuffer: false,
  sunlight: { direction: [0.3, 0.85, -0.5], color: [0.77, 0.89, 1], ambientColor: [0.69, 0.82, 0.98], intensity: 0.7, ambientIntensity: 0.85, fillIntensity: 0.4, rimIntensity: 0.35, exposure: 1.03, sky: { zenithColor: [0.07, 0.12, 0.2], horizonColor: [0.16, 0.23, 0.3], groundColor: [0.04, 0.07, 0.1], stars: { enabled: false } }, fog: { density: 0 }, shadow: { enabled: true, intensity: 0.25, softness: 0.7 }, emissive: { intensity: 1.2, range: 10 } },
})
let current = 'perspective'
const draw = () => {
  const bounds = canvas.getBoundingClientRect()
  renderer.setViewport(0, 0, bounds.width, bounds.height, 1)
  renderer.drawStructure()
}
async function show(name) {
  const view = views[name]
  if (!view) throw new Error(`Unknown view ${name}`)
  const structure = new Structure(original.getSize())
  for (const block of original.getBlocks()) {
    if (!view.cut || !view.cut(block)) structure.addBlock(block.pos, block.state.getName(), block.state.getProperties(), block.nbt)
  }
  renderer.setStructure(structure)
  await renderer.whenReady()
  renderer.setCamera({ position: view.position, target: view.target, fov: view.fov })
  document.getElementById('label').textContent = view.label
  document.getElementById('note').textContent = view.cut ? '仅预览剖切 · 最终 NBT 保留完整屋顶与墙体' : '由实际 room.nbt 渲染 · 非 AI 图 / 非客户端截图'
  for (const button of document.querySelectorAll('button')) button.classList.toggle('active', button.dataset.view === name)
  current = name
  draw()
  await new Promise(resolve => requestAnimationFrame(resolve))
  draw()
  return { view: name, source_sha256: metadata.sha256, visible_records: structure.getBlocks().length, original_records: original.getBlocks().length, section_cut: Boolean(view.cut) }
}
for (const [name, view] of Object.entries(views)) {
  const button = document.createElement('button')
  button.textContent = view.button
  button.dataset.view = name
  button.onclick = () => show(name)
  document.getElementById('views').append(button)
}
document.getElementById('fingerprint').textContent = `SOURCE SHA-256 ${metadata.sha256.slice(0, 24)}…`
window.addEventListener('resize', draw)
window.review = { show, views: Object.keys(views), source_sha256: metadata.sha256, get current() { return current } }
await show('perspective')
document.getElementById('loading').remove()
window.reviewReady = true
