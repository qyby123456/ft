import { NbtFile, Structure, ThreeStructureRenderer, loadDefaultPackResources } from '@mattzh72/lodestone'

const parameters = new URLSearchParams(location.search)
const view = parameters.get('view') ?? 'interior'
const source = parameters.get('source') ?? '/room.nbt'
if (!(source === '/room.nbt' || /^\/qa\/native-placed-(none|clockwise_90|clockwise_180|counterclockwise_90)\.nbt$/.test(source))) throw new Error('Unknown NBT source')
const metadata = await fetch('/room.json').then(response => response.json())
const bytes = new Uint8Array(await fetch(source).then(response => response.arrayBuffer()))
const structure = Structure.fromNbt(NbtFile.read(bytes).root)
const [width, height, length] = structure.getSize()
const cut = new Structure(structure.getSize())
const interiorViews = ['isometric', 'north', 'south', 'west', 'east']
const keep = ([posX, posY, posZ]) => {
  if (!interiorViews.includes(view)) return true
  if (posY >= 10) return false
  if (view === 'isometric') return posY <= 2 || (posX >= 1 && posZ >= 1)
  if (view === 'north') return posY <= 2 || posZ >= 1
  if (view === 'south') return posY <= 2 || posZ < length - 1
  if (view === 'west') return posY <= 2 || posX > 0
  return posY <= 2 || posX < width - 1
}
for (const block of structure.getBlocks()) if (keep(block.pos)) cut.addBlock(block.pos, block.state.getName(), block.state.getProperties(), block.nbt)
const canvas = document.getElementById('scene')
const pack = await loadDefaultPackResources({ baseUrl: new URL('/default-pack/', location.href).toString() })
const renderer = new ThreeStructureRenderer(canvas, cut, pack.resources, {
  chunkSize: 16, drawDistance: 1000, asyncBuild: false, preserveDrawingBuffer: true,
  sunlight: { direction: [0.4, 0.8, -0.3], intensity: 0.85, ambientIntensity: 0.7, fillIntensity: 0.4, rimIntensity: 0.25, exposure: 1.08,
    sky: { zenithColor: [0.06, 0.11, 0.12], horizonColor: [0.13, 0.21, 0.22], groundColor: [0.07, 0.12, 0.12], stars: { enabled: false } } },
})
renderer.setViewport(0, 0, canvas.clientWidth, canvas.clientHeight, 1)
const cameras = {
  interior: [[8.5, 4.65, 1.4], [8.5, 3.7, 28]],
  isometric: [[-20, 29, -21], [8.5, 2.2, 19]],
  north: [[width / 2, 24, -29], [width / 2, 2.2, length / 2]],
  south: [[width / 2, 24, length + 29], [width / 2, 2.2, length / 2]],
  west: [[-42, 33, length / 2], [width / 2, 2.2, length / 2]],
  east: [[width + 42, 33, length / 2], [width / 2, 2.2, length / 2]],
  shell: [[width + 29, 34, -30], [width / 2, 4.5, length / 2]],
  'shell-north': [[width / 2, 23, -35], [width / 2, 4.5, length / 2]],
  'shell-south': [[width / 2, 23, length + 35], [width / 2, 4.5, length / 2]],
  'shell-west': [[-44, 30, length / 2], [width / 2, 4.5, length / 2]],
  'shell-east': [[width + 44, 30, length / 2], [width / 2, 4.5, length / 2]],
  roof: [[width / 2 + 0.01, 65, length / 2], [width / 2, 0, length / 2]],
}
if (!cameras[view]) throw new Error('Unknown camera')
renderer.setFOV(view === 'interior' ? 76 : 43)
renderer.lookAt(...cameras[view])
await renderer.whenReady()
renderer.drawStructure()
const labels = { interior: '北口内景 · 完整结构', isometric: '水渠总览 · 屋面与近侧墙剖切', north: '北向内部 · 屋面与北墙剖切', south: '南向内部 · 屋面与南墙剖切', west: '西向内部 · 屋面与西墙剖切', east: '东向内部 · 屋面与东墙剖切', shell: '完整外壳 · 未剖切', 'shell-north': '北向外壳 · 未剖切', 'shell-south': '南向外壳 · 未剖切', 'shell-west': '西向外壳 · 未剖切', 'shell-east': '东向外壳 · 未剖切', roof: '完整屋顶俯视 · 未剖切' }
document.getElementById('label').textContent = labels[view] + (source === '/room.nbt' ? '' : ' / Forge 世界回捕')
document.querySelector('.tag').innerHTML = '实际 NBT · Lodestone<br>' + structure.getSize().join(' × ') + ' / Java 1.20.1'
document.getElementById('fingerprint').innerHTML = '<b>源 NBT SHA-256</b><br>' + metadata.sha256.slice(0, 24) + '…'
document.getElementById('loading').remove()
const digest = [...new Uint8Array(await crypto.subtle.digest('SHA-256', bytes))].map(value => value.toString(16).padStart(2, '0')).join('')
window.p02Preview = { ready: true, source, view, size: structure.getSize(), source_records: structure.getBlocks().length, rendered_records: cut.getBlocks().length, render_source_sha256: digest, metadata_sha256: metadata.sha256 }
