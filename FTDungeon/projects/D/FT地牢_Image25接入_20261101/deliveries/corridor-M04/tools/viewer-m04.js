const response = await fetch('/__minecraft_builder__/build.nbt')
if (!response.ok) throw new Error('Could not load actual room.nbt')
const payload = await response.arrayBuffer()
const sourceHash = [...new Uint8Array(await crypto.subtle.digest('SHA-256', payload))].map(value => value.toString(16).padStart(2, '0')).join('')
const finalStructure = Structure.fromNbt(NbtFile.read(new Uint8Array(payload)).root)
const pack = await loadDefaultPackResources({ baseUrl: new URL('/__minecraft_builder__/default-pack/', location.href).toString() })
document.body.innerHTML = `<style>
*{box-sizing:border-box}body{margin:0;background:#111725;color:#e8e1d6;font-family:'Microsoft YaHei',sans-serif}
#view{display:block;width:100vw;height:100vh}#label{position:fixed;top:25px;left:30px;padding:15px 20px;background:#14182cdc;border-left:4px solid #c7a8e5;pointer-events:none}
#label h1{font-size:24px;margin:0 0 8px;font-weight:600}#label p{font-size:15px;margin:0;color:#b5c1cd}#foot{position:fixed;bottom:18px;left:30px;font-size:14px;color:#a8b5c9;background:#12182bdc;padding:8px 12px}
</style><canvas id="view"></canvas><div id="label"><h1 id="title"></h1><p id="subtitle"></p></div><div id="foot">最终 room.nbt · Minecraft Builder / Lodestone 0.6.0 · 软件渲染，非游戏实机截图</div>`
const canvas = document.getElementById('view')
const renderer = new ThreeStructureRenderer(canvas, finalStructure, pack.resources, {
  chunkSize: 16, drawDistance: 1000, useInvisibleBlockBuffer: false, asyncBuild: true,
  preserveDrawingBuffer: true, antialias: true,
  sunlight: {
    direction: [0.4, 0.8, -0.5], intensity: 0.9, ambientIntensity: 0.78,
    fillIntensity: 0.4, rimIntensity: 0.28, exposure: 1.05,
    sky: { zenithColor: [0.055, 0.07, 0.12], horizonColor: [0.12, 0.15, 0.22], groundColor: [0.05, 0.065, 0.1], stars: { enabled: false } },
    fog: { density: 0 }, shadow: { enabled: false },
    emissive: { range: 14, intensity: 1.3, tint: [1, 0.91, 0.78] },
  },
})
renderer.setViewport(0, 0, innerWidth, innerHeight, 1)
await renderer.whenReady()
window.m04Preview = {
  sourceHash,
  sourceRecords: finalStructure.getBlocks().length,
  async render(options) {
    const { mode, position, target, title, subtitle, fov = 42 } = options
    let visible = finalStructure
    if (mode === 'cutaway') {
      visible = new Structure(finalStructure.getSize())
      for (const entry of finalStructure.getBlocks()) {
        const [width, height, depth] = entry.pos
        const nearWall = position[0] < 6.5 ? width === 0 : width === 12
        const nearEnd = position[2] < 20.5 ? depth === 0 : depth === 40
        if (height === 11 || height >= 2 && (nearWall || nearEnd)) continue
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
window.m04Ready = true
