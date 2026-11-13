import { NbtFile, Structure, ThreeStructureRenderer, loadDefaultPackResources } from '/@fs/C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/.vite/deps/@mattzh72_lodestone.js'

const canvas = document.getElementById('scene')
const { resources } = await loadDefaultPackResources({ baseUrl: new URL('/__minecraft_builder__/default-pack/', window.location.href).toString() })
let renderer
window.renderView = async options => {
  renderer?.dispose()
  const response = await fetch(options.url ?? '/__minecraft_builder__/build.nbt')
  if (!response.ok) throw new Error(`NBT fetch failed ${response.status}`)
  const structure = Structure.fromNbt(NbtFile.read(new Uint8Array(await response.arrayBuffer())).root)
  const size = structure.getSize()
  const originalCount = structure.getBlocks().length
  if (options.cutaway) {
    for (const block of structure.getBlocks()) {
      const [posX, posY, posZ] = block.pos
      if (posY >= 10 || (options.cutaway === 'east' && posX >= 10) || (options.cutaway === 'west' && posX <= 6)) structure.addBlock(block.pos, 'minecraft:air')
    }
  }
  renderer = new ThreeStructureRenderer(canvas, structure, resources, {
    preserveDrawingBuffer: true,
    asyncBuild: true,
    sunlight: {
      direction: [0.35, 0.85, 0.45], intensity: 1.05, ambientIntensity: 0.78, fillIntensity: 0.46, rimIntensity: 0.22, exposure: 1.12,
      sky: { zenithColor: [0.08, 0.14, 0.20], horizonColor: [0.25, 0.34, 0.40], groundColor: [0.12, 0.16, 0.18], stars: { enabled: false } },
      fog: { color: [0.14, 0.20, 0.25], density: 0.00001, heightFalloff: 0.001 },
    },
  })
  renderer.setViewport(0, 0, window.innerWidth, window.innerHeight, 1)
  renderer.setCamera({ position: options.position, target: options.target, fov: options.fov ?? 52 })
  await renderer.whenReady()
  renderer.drawStructure()
  await new Promise(resolve => requestAnimationFrame(resolve))
  renderer.drawStructure()
  document.getElementById('status').style.display = 'none'
  return { size, originalCount, camera: renderer.getCamera(), renderOnlyCutaway: Boolean(options.cutaway) }
}
window.renderReady = true
