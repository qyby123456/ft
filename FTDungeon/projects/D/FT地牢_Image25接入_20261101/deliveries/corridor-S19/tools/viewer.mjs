const optimizedSource = await (await fetch('/src/main.js')).text()
const optimizedEntry = optimizedSource.match(/from "([^"]+@mattzh72_lodestone\.js[^\"]*)"/)[1]
const { NbtFile, Structure, ThreeStructureRenderer, loadDefaultPackResources } = await import(optimizedEntry)

const canvas = document.querySelector('#scene')
const status = document.querySelector('#status')
const bytes = new Uint8Array(await (await fetch('/__minecraft_builder__/build.nbt')).arrayBuffer())
const fingerprint = Array.from(new Uint8Array(await crypto.subtle.digest('SHA-256', bytes)), value => value.toString(16).padStart(2, '0')).join('')
const original = Structure.fromNbt(NbtFile.read(bytes).root)
const pack = await loadDefaultPackResources({ baseUrl: `${location.origin}/__minecraft_builder__/default-pack/` })
const renderer = new ThreeStructureRenderer(canvas, original, pack.resources, {
  preserveDrawingBuffer: true, antialias: true, asyncBuild: true, drawDistance: 256,
  sunlight: {
    direction: [0.35, 0.85, -0.25], intensity: 0.75, ambientIntensity: 0.9, fillIntensity: 0.45, rimIntensity: 0.25, exposure: 1.15,
    sky: { zenithColor: [0.045, 0.047, 0.073], horizonColor: [0.12, 0.105, 0.15], groundColor: [0.035, 0.032, 0.045], stars: { enabled: false } },
    shadow: { enabled: false }, fog: { density: 0 }, emissive: { intensity: 1.2, range: 12 }
  }
})
document.querySelector('#fingerprint').textContent = `NBT SHA-256 ${fingerprint.slice(0, 16)}…`

function selectedStructure(view) {
  if (view === 'interior' || view === 'exterior') return original
  const clipped = new Structure(original.getSize())
  for (const block of original.getBlocks()) {
    const [posX, posY, posZ] = block.pos
    if (block.state.is('minecraft:air')) continue
    if (view === 'floorplan') {
      if (posY > 1) continue
    } else {
      if (posY === 10) continue
      if (posY === 9 && ![0, 4, 8, 11, 15].includes(posZ) && posX !== 7) continue
      if (posY >= 2 && (view === 'north' && posZ === 0 || view === 'south' && posZ === 15 || view === 'west' && posX <= 1 || view === 'east' && posX >= 13)) continue
    }
    clipped.addBlock(block.pos, block.state.getName(), block.state.getProperties(), block.nbt)
  }
  return clipped
}

const presets = {
  interior: { position: [7.5, 3.7, 1.15], target: [7.5, 3.7, 14], fov: 80, label: 'INTERIOR / 北向南' },
  north: { position: [7.5, 16, -20], target: [7.5, 4.5, 8], fov: 47, label: 'NORTH / 北侧剖视' },
  south: { position: [7.5, 16, 36], target: [7.5, 4.5, 8], fov: 47, label: 'SOUTH / 南侧剖视' },
  west: { position: [-22, 16, 8], target: [7.5, 4.5, 8], fov: 47, label: 'WEST / 西侧剖视' },
  east: { position: [37, 16, 8], target: [7.5, 4.5, 8], fov: 47, label: 'EAST / 东侧剖视' },
  exterior: { position: [-21, 25, -24], target: [7.5, 4.6, 8], fov: 40, label: 'EXTERIOR / 完整外壳' },
  floorplan: { position: [7.5, 50, 8], target: [7.5, 0, 8], up: [0, 0, 1], fov: 26, label: 'PLAN / 地面镶嵌' }
}

window.renderView = async view => {
  const preset = presets[view]
  if (!preset) throw new Error(`Unknown view ${view}`)
  window.viewReady = false
  const structure = selectedStructure(view)
  renderer.setStructure(structure)
  renderer.setViewport(0, 0, innerWidth, innerHeight - 144, 1)
  renderer.setCamera({ ...preset, up: preset.up ?? [0, 1, 0] })
  await renderer.whenReady()
  renderer.drawStructure()
  await new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)))
  renderer.drawStructure()
  document.querySelector('#view-label').textContent = preset.label
  document.querySelector('#note').textContent = ['interior', 'exterior'].includes(view)
    ? '15 × 11 × 16 · 最终 NBT 完整屋顶与两端接口 · 零液体 / 零容器 / 零实体'
    : view === 'floorplan' ? '仅显示地板层 · 金纹中轴与两侧封闭橙色光带 · 连续 3×3 主通道'
      : '仅预览裁去朝向墙和屋面；保留梁网与吊笼锚点 · 最终 NBT 未裁切'
  status.hidden = true
  window.viewReady = true
  return { view, fingerprint, renderedBlocks: structure.getBlocks().length, camera: preset }
}
window.renderEvidence = { fingerprint, size: original.getSize(), originalRecords: original.getBlocks().length }
await window.renderView('interior')
window.s19Ready = true
