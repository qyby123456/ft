const response = await fetch('/__minecraft_builder__/build.nbt')
if (!response.ok) throw new Error('Could not load actual room.nbt')
const payload = await response.arrayBuffer()
const sourceHash = [...new Uint8Array(await crypto.subtle.digest('SHA-256', payload))].map(value => value.toString(16).padStart(2, '0')).join('')
const finalStructure = Structure.fromNbt(NbtFile.read(new Uint8Array(payload)).root)
const pack = await loadDefaultPackResources({ baseUrl: new URL('/__minecraft_builder__/default-pack/', location.href).toString() })
document.body.innerHTML = '<style>*{box-sizing:border-box}body{margin:0;background:#11181c;color:#e8dfd1;font-family:Microsoft YaHei,sans-serif}#view{display:block;width:100vw;height:100vh}#label{position:fixed;top:24px;left:28px;padding:14px 20px;background:#10191bdf;border-left:4px solid #cba165;pointer-events:none}#label h1{font-size:25px;margin:0 0 8px;font-weight:600}#label p{font-size:15px;margin:0;color:#c6c2b3}#foot{position:fixed;bottom:18px;left:28px;font-size:13px;color:#b7c1bb;background:#10191bdf;padding:8px 12px}</style><canvas id="view"></canvas><div id="label"><h1 id="title"></h1><p id="subtitle"></p></div><div id="foot">最终 room.nbt · Minecraft Builder / Lodestone 0.6.0 · 软件渲染，非游戏实机截图</div>'
const canvas = document.getElementById('view')
const renderer = new ThreeStructureRenderer(canvas, finalStructure, pack.resources, {
  chunkSize: 16, drawDistance: 1000, useInvisibleBlockBuffer: false, asyncBuild: true,
  preserveDrawingBuffer: true, antialias: true,
  sunlight: {
    direction: [0.4, 0.85, -0.5], intensity: 0.85, ambientIntensity: 0.8,
    fillIntensity: 0.4, rimIntensity: 0.28, exposure: 1.06,
    sky: { zenithColor: [0.04, 0.06, 0.07], horizonColor: [0.09, 0.13, 0.14], groundColor: [0.04, 0.05, 0.06], stars: { enabled: false } },
    fog: { density: 0 }, shadow: { enabled: false },
    emissive: { range: 12, intensity: 1.3, tint: [1, 0.88, 0.68] },
  },
})
renderer.setViewport(0, 0, innerWidth, innerHeight, 1)
await renderer.whenReady()
window.a03Preview = {
  sourceHash,
  async render(options) {
    const { mode, position, target, title, subtitle, fov = 42 } = options
    let visible = finalStructure
    if (mode === 'cutaway') {
      visible = new Structure(finalStructure.getSize())
      for (const entry of finalStructure.getBlocks()) {
        const [width, height, depth] = entry.pos
        const nearWall = position[0] < 9.5 ? width <= 1 : width >= 17
        const nearEnd = position[2] < 9.5 ? depth <= 1 : depth >= 17
        const removedCapital = height === 7 && (position[0] < 9.5 ? width === 2 : width === 16) && [1, 7, 12, 17].includes(depth)
        if (height >= 9 || height >= 1 && (nearWall || nearEnd) || removedCapital) continue
        visible.addBlock(entry.pos, entry.state.getName(), entry.state.getProperties(), entry.nbt)
      }
    }
    renderer.setStructure(visible)
    await renderer.whenReady()
    renderer.setCamera({ position, target, fov })
    document.getElementById('title').textContent = title
    document.getElementById('subtitle').textContent = subtitle
    renderer.drawStructure()
    await new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)))
    renderer.drawStructure()
    return { source_sha256: sourceHash, source_records: finalStructure.getBlocks().length, rendered_records: visible.getBlocks().length, mode, position, target, fov }
  },
}
window.a03Ready = true

