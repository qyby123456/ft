const response = await fetch('/__minecraft_builder__/build.nbt')
if (!response.ok) throw new Error('Cannot load final room.nbt')
const payload = await response.arrayBuffer()
const sourceHash = [...new Uint8Array(await crypto.subtle.digest('SHA-256', payload))].map(value => value.toString(16).padStart(2, '0')).join('')
const finalStructure = Structure.fromNbt(NbtFile.read(new Uint8Array(payload)).root)
const pack = await loadDefaultPackResources({ baseUrl: new URL('/__minecraft_builder__/default-pack/', location.href).toString() })
document.body.innerHTML = `<style>*{box-sizing:border-box}body{margin:0;background:#151923;color:#eee3d4;font-family:'Microsoft YaHei',sans-serif}#view{display:block;width:100vw;height:100vh}#label{position:fixed;top:24px;left:30px;padding:14px 20px;background:#151923e8;border-left:4px solid #f09848;pointer-events:none}h1{font-size:26px;margin:0 0 7px}p{font-size:15px;margin:0;color:#c0b6aa}#foot{position:fixed;bottom:15px;left:30px;font-size:13px;background:#151923e8;padding:8px 12px;color:#b9b1a6}</style><canvas id="view"></canvas><div id="label"><h1 id="title"></h1><p id="subtitle"></p></div><div id="foot">实际最终 NBT · Minecraft Builder / Lodestone · 软件渲染，非游戏截图 · zone-M04</div>`
const canvas = document.getElementById('view')
const renderer = new ThreeStructureRenderer(canvas, finalStructure, pack.resources, {
  chunkSize: 16, drawDistance: 1000, useInvisibleBlockBuffer: false, asyncBuild: true, preserveDrawingBuffer: true, antialias: true,
  sunlight: {
    direction: [0.4, 0.85, -0.5], intensity: 1.0, ambientIntensity: 0.85, fillIntensity: 0.55, rimIntensity: 0.28, exposure: 1.12,
    sky: { zenithColor: [0.065, 0.075, 0.105], horizonColor: [0.14, 0.15, 0.18], groundColor: [0.055, 0.06, 0.075], stars: { enabled: false } },
    fog: { density: 0 }, shadow: { enabled: false }, emissive: { range: 13, intensity: 1.5, tint: [1, 0.68, 0.36] },
  },
})
renderer.setViewport(0, 0, innerWidth, innerHeight, 1)
await renderer.whenReady()
window.zonePreview = {
  async render(options) {
    const { mode, position, target, title, subtitle, fov = 42 } = options
    let visible = finalStructure
    if (mode === 'cutaway') {
      visible = new Structure(finalStructure.getSize())
      for (const entry of finalStructure.getBlocks()) {
        const [width, height, depth] = entry.pos
        const nearWall = position[0] < 12.5 ? width === 0 : width === 24
        const nearEnd = position[2] < 12.5 ? depth === 0 : depth === 24
        if (height === 13 || height >= 3 && (nearWall || nearEnd)) continue
        visible.addBlock(entry.pos, entry.state.getName(), entry.state.getProperties(), entry.nbt)
      }
    }
    renderer.setStructure(visible)
    await renderer.whenReady()
    renderer.setCamera({ position, target, fov })
    renderer.drawStructure()
    document.getElementById('title').textContent = title
    document.getElementById('subtitle').textContent = subtitle
    await new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)))
    renderer.drawStructure()
    return { source_sha256: sourceHash, source_records: finalStructure.getBlocks().length, rendered_records: visible.getBlocks().length, mode, position, target, fov }
  },
}
window.zoneReady = true
