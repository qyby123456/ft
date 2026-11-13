import { NbtFile, Structure, ThreeStructureRenderer, loadDefaultPackResources } from '@mattzh72/lodestone'

const parameters = new URLSearchParams(location.search)
const view = parameters.get('view') ?? 'interior'
const source = parameters.get('source') ?? '/room.nbt'
const allowedSource = source === '/room.nbt' || /^\/qa\/native-placed-(none|clockwise_90|clockwise_180|counterclockwise_90)\.nbt$/.test(source)
if (!allowedSource) throw new Error('Unknown NBT source')
const canvas = document.getElementById('scene')
const metadata = await fetch('/room.json').then(response => response.json())
const bytes = new Uint8Array(await fetch(source).then(response => response.arrayBuffer()))
const structure = Structure.fromNbt(NbtFile.read(bytes).root)
const [width, height, length] = structure.getSize()
const cut = new Structure(structure.getSize())
const keep = position => {
  const [posX, posY, posZ] = position
  if (['north', 'south', 'west', 'east'].includes(view) && posY >= 6 && ![0, 5, 10, 15].includes(posZ)) return false
  if (view === 'north') return posY < height - 1 && (posY === 0 || posZ >= Math.floor(length / 3))
  if (view === 'south') return posY < height - 1 && (posY === 0 || posZ < Math.ceil(length * 2 / 3))
  if (view === 'west') return posY < height - 1 && (posY === 0 || posX >= Math.floor(width / 2))
  if (view === 'east') return posY < height - 1 && (posY === 0 || posX <= Math.floor(width / 2))
  return true
}
for (const block of structure.getBlocks()) {
  if (keep(block.pos)) cut.addBlock(block.pos, block.state.getName(), block.state.getProperties(), block.nbt)
}
const pack = await loadDefaultPackResources({ baseUrl: new URL('/default-pack/', location.href).toString() })
const renderer = new ThreeStructureRenderer(canvas, cut, pack.resources, {
  chunkSize: 16, drawDistance: 1000, asyncBuild: false, preserveDrawingBuffer: true,
  sunlight: {
    direction: [0.4, 0.8, -0.3], intensity: 0.85, ambientIntensity: 0.66, fillIntensity: 0.35,
    rimIntensity: 0.2, exposure: 1.05, sky: { zenithColor: [0.065, 0.1, 0.11], horizonColor: [0.16, 0.19, 0.19], groundColor: [0.09, 0.12, 0.12], stars: { enabled: false } },
  },
})
renderer.setViewport(0, 0, canvas.clientWidth, canvas.clientHeight, 1)
const cameras = {
  interior: [[width / 2, 2.75, 0.8], [width / 2, 2.75, length]],
  north: [[width / 2, 6, -9], [width / 2, 3, length * 0.7]],
  south: [[width / 2, 6, length + 9], [width / 2, 3, length * 0.3]],
  west: [[-23, 13, length / 2 - 1.2], [width * 0.65, 3.1, length / 2]],
  east: [[width + 23, 13, length / 2 + 1.2], [width * 0.35, 3.1, length / 2]],
  shell: [[width + 16, 19, -18], [width / 2, height / 2, length / 2]],
  roof: [[width / 2 + 0.01, 33, length / 2], [width / 2, 0, length / 2]],
}
const camera = cameras[view]
if (!camera) throw new Error('Unknown view')
renderer.setFOV(view === 'interior' ? 76 : 40)
renderer.lookAt(camera[0], camera[1])
await renderer.whenReady()
renderer.drawStructure()
const labels = { interior: '北口居中 · 完整 NBT 内景', north: '北向内部剖视 · 移除近侧及顶层遮挡', south: '南向内部剖视 · 移除近侧及顶层遮挡', west: '西向内部剖视 · 保留远侧半体', east: '东向内部剖视 · 保留远侧半体', shell: '完整外壳 · 未剖切', roof: '完整屋顶 · 未剖切俯视' }
document.getElementById('label').textContent = labels[view] + (source === '/room.nbt' ? '' : ' / Forge 世界回捕')
document.querySelector('.tag').innerHTML = '实际 NBT · Lodestone<br>' + [width, height, length].join(' × ') + ' / Java 1.20.1'
document.getElementById('fingerprint').innerHTML = '<b>源 NBT SHA-256</b><br>' + metadata.sha256.slice(0, 24) + '…'
document.getElementById('loading').remove()
const sourceDigest = [...new Uint8Array(await crypto.subtle.digest('SHA-256', bytes))].map(value => value.toString(16).padStart(2, '0')).join('')
window.s02Preview = { ready: true, source, view, size: structure.getSize(), source_records: structure.getBlocks().length, rendered_records: cut.getBlocks().length, render_source_sha256: sourceDigest, metadata_sha256: metadata.sha256 }
