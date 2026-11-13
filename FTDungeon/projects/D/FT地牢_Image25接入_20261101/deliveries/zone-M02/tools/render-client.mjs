import { NbtFile, Structure, ThreeStructureRenderer, loadDefaultPackResources } from '/@fs/C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/.vite/deps/@mattzh72_lodestone.js'

const canvas = document.getElementById('scene')
const { resources } = await loadDefaultPackResources({ baseUrl: new URL('/__minecraft_builder__/default-pack/', window.location.href).toString() })
let renderer
window.renderView = async options => {
  renderer?.dispose()
  const response = await fetch('/__minecraft_builder__/build.nbt')
  if (!response.ok) throw new Error('NBT fetch failed: ' + response.status)
  const structure = Structure.fromNbt(NbtFile.read(new Uint8Array(await response.arrayBuffer())).root)
  const originalCount = structure.getBlocks().length
  let removed = 0
  if (options.cutaway) {
    for (const block of structure.getBlocks()) {
      const [posX, posY, posZ] = block.pos
      const front = options.cutaway === 'north' ? posZ === 0 || posX === 24 : options.cutaway === 'east' ? posX === 24 || posZ === 24 : options.cutaway === 'south' ? posZ === 24 || posX === 0 : posX === 0 || posZ === 0
      if (posY >= (options.cutaway === 'plan' ? 7 : 13) || (options.cutaway !== 'plan' && front && posY >= 4)) { structure.addBlock(block.pos, 'minecraft:air'); removed++ }
    }
  }
  renderer = new ThreeStructureRenderer(canvas, structure, resources, {
    preserveDrawingBuffer: true, asyncBuild: true,
    sunlight: {
      direction: [0.35, 0.85, 0.45], intensity: 1.0, ambientIntensity: 0.86, fillIntensity: 0.55, rimIntensity: 0.22, exposure: 1.12,
      sky: { zenithColor: [0.08, 0.14, 0.20], horizonColor: [0.25, 0.34, 0.40], groundColor: [0.12, 0.16, 0.18], stars: { enabled: false } },
      fog: { color: [0.14, 0.20, 0.25], density: 0.00001, heightFalloff: 0.001 }
    }
  })
  renderer.setViewport(0, 0, window.innerWidth, window.innerHeight, 1)
  renderer.setCamera({ position: options.position, target: options.target, fov: options.fov ?? 48 })
  await renderer.whenReady()
  renderer.drawStructure()
  await new Promise(resolve => requestAnimationFrame(resolve))
  renderer.drawStructure()
  document.getElementById('status').style.display = 'none'
  return { size: structure.getSize(), originalCount, render_only_removed_records: removed, camera: renderer.getCamera(), renderOnlyCutaway: Boolean(options.cutaway) }
}
window.renderReady = true
