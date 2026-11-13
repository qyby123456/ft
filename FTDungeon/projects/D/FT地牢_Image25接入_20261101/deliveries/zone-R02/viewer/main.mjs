import { NbtFile, Structure, ThreeStructureRenderer, loadDefaultPackResources } from '@mattzh72/lodestone'

const views = {
  perspective: { label: '东南剖视 · 指挥桌 / 旗帜 / 军械角', button: '总览', position: [39, 25, 41], target: [10.5, 3.1, 10.5], fov: 38, cut: block => block.pos[1] >= 7 || block.pos[1] > 0 && (block.pos[0] >= 18 || block.pos[2] >= 18) },
  north: { label: '北向内部 · 北入口通路与指挥桌', button: '北内', position: [10.5, 24, -28], target: [10.5, 2.8, 11], fov: 40, cut: block => block.pos[1] >= 7 || block.pos[2] <= 1 && block.pos[1] > 0 },
  south: { label: '南向内部 · 空容器与红色旗帜', button: '南内', position: [10.5, 24, 49], target: [10.5, 2.8, 10], fov: 40, cut: block => block.pos[1] >= 7 || block.pos[2] >= 18 && block.pos[1] > 0 },
  west: { label: '西向内部 · 支柱与封闭木门', button: '西内', position: [-28, 24, 10.5], target: [11, 2.8, 10.5], fov: 40, cut: block => block.pos[1] >= 7 || block.pos[0] <= 2 && block.pos[1] > 0 },
  east: { label: '东向内部 · 军械架 / 熄灭火盆', button: '东内', position: [49, 24, 10.5], target: [10, 2.8, 10.5], fov: 40, cut: block => block.pos[1] >= 7 || block.pos[0] >= 18 && block.pos[1] > 0 },
  shell_north: { label: '北侧完整外壳 · 中央三宽入口', button: '北壳', position: [10.5, 25, -34], target: [10.5, 4.3, 10.5], fov: 38 },
  shell_south: { label: '南侧完整外壳 · 铁栏背窗', button: '南壳', position: [10.5, 25, 55], target: [10.5, 4.3, 10.5], fov: 38 },
  shell_west: { label: '西侧完整外壳 · 完整墙体', button: '西壳', position: [-34, 25, 10.5], target: [10.5, 4.3, 10.5], fov: 38 },
  shell_east: { label: '东侧完整外壳 · 封闭木门背板', button: '东壳', position: [55, 25, 10.5], target: [10.5, 4.3, 10.5], fov: 38 },
  roof: { label: '完整双层屋顶 · 全面积覆盖', button: '屋顶', position: [10.5, 58, 10.51], target: [10.5, 0, 10.5], fov: 36 },
  entrance: { label: '北入口内部 · 三格净高与梁网', button: '入口', position: [10.5, 3.1, 2.4], target: [10.5, 3.6, 18], fov: 83 },
}
const metadata = await (await fetch('/metadata.json')).json()
const buffer = new Uint8Array(await (await fetch('/build.nbt')).arrayBuffer())
const original = Structure.fromNbt(NbtFile.read(buffer).root)
const pack = await loadDefaultPackResources({ baseUrl: new URL('/default-pack/', location.href).toString() })
const canvas = document.getElementById('scene')
const renderer = new ThreeStructureRenderer(canvas, original, pack.resources, {
  preserveDrawingBuffer: true, asyncBuild: false, antialias: true, useInvisibleBlockBuffer: false,
  sunlight: { direction: [-0.35, 0.9, -0.4], color: [1, 0.9, 0.8], ambientColor: [0.83, 0.85, 0.87], intensity: 0.75, ambientIntensity: 0.83, fillIntensity: 0.45, rimIntensity: 0.3, exposure: 1.04, sky: { zenithColor: [0.1, 0.12, 0.15], horizonColor: [0.2, 0.21, 0.23], groundColor: [0.08, 0.09, 0.1], stars: { enabled: false } }, fog: { density: 0 }, shadow: { enabled: true, intensity: 0.25, softness: 0.7 }, emissive: { intensity: 1.3, range: 10 } },
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
  for (const block of original.getBlocks()) if (!view.cut || !view.cut(block)) structure.addBlock(block.pos, block.state.getName(), block.state.getProperties(), block.nbt)
  renderer.setStructure(structure)
  await renderer.whenReady()
  renderer.setCamera({ position: view.position, target: view.target, fov: view.fov })
  document.getElementById('label').textContent = view.label
  document.getElementById('note').textContent = view.cut ? '仅渲染副本剖切 · 最终 NBT 保留完整屋顶 / 梁网 / 墙体' : '实际 room.nbt 软件渲染 · 非概念图 / 非 Minecraft 客户端截图'
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
document.getElementById('fingerprint').textContent = `SOURCE SHA-256 ${metadata.sha256}`
window.addEventListener('resize', draw)
window.review = { show, views: Object.keys(views), source_sha256: metadata.sha256, get current() { return current } }
await show('perspective')
document.getElementById('loading').remove()
window.reviewReady = true
