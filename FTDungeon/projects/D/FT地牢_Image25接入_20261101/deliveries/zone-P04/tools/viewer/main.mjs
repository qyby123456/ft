import {NbtFile, Structure, ThreeStructureRenderer, loadDefaultPackResources} from '@mattzh72/lodestone'

const canvas = document.querySelector('canvas')
const caption = document.querySelector('#caption')
const [response, pack] = await Promise.all([fetch('/room.nbt'), loadDefaultPackResources({baseUrl: new URL('/pack/', location.href).toString()})])
const bytes = new Uint8Array(await response.arrayBuffer())
const structure = Structure.fromNbt(NbtFile.read(bytes).root)
const hashBuffer = await crypto.subtle.digest('SHA-256', bytes)
const sourceHash = Array.from(new Uint8Array(hashBuffer), value => value.toString(16).padStart(2,'0')).join('')
const renderer = new ThreeStructureRenderer(canvas, structure, pack.resources, {
  chunkSize:16,drawDistance:1000,useInvisibleBlockBuffer:false,preserveDrawingBuffer:true,antialias:true,
  sunlight:{direction:[0.4,0.85,-0.25],intensity:1.0,ambientIntensity:0.85,fillIntensity:0.45,rimIntensity:0.2,exposure:1.05,
    sky:{zenithColor:[0.055,0.075,0.09],horizonColor:[0.15,0.19,0.22],groundColor:[0.08,0.1,0.12],stars:{enabled:false}},
    fog:{enabled:false,color:[0.15,0.19,0.22]}}
})
renderer.setViewport(0,0,innerWidth,innerHeight,1)
const cameras = {
  preview:{position:[44,35,-27],target:[11,6,13],fov:37},
  'interior-north':{position:[11.5,32,-43],target:[11.5,6,13.5],fov:35},
  'interior-south':{position:[11.5,32,70],target:[11.5,6,13.5],fov:35},
  'interior-west':{position:[-47,34,13.5],target:[11.5,6,13.5],fov:35},
  'interior-east':{position:[70,34,13.5],target:[11.5,6,13.5],fov:35},
  'exterior-north':{position:[11.5,32,-43],target:[11.5,7.5,13.5],fov:35},
  'exterior-south':{position:[11.5,32,70],target:[11.5,7.5,13.5],fov:35},
  'exterior-west':{position:[-47,34,13.5],target:[11.5,7.5,13.5],fov:35},
  'exterior-east':{position:[70,34,13.5],target:[11.5,7.5,13.5],fov:35}
}
function provider(view) {
  if (view.startsWith('exterior-')) return structure
  const side = view === 'preview' ? 'north-east' : view.slice(9)
  const visible = pos => pos[1] < 15 && !(side.includes('north') && pos[2] < 2 && pos[1] > 4) && !(side.includes('south') && pos[2] > 24 && pos[1] > 4) && !(side.includes('west') && pos[0] < 2 && pos[1] > 2) && !(side.includes('east') && pos[0] > 20 && pos[1] > 2) && !(pos[1] === 8 && [8,20].includes(pos[2]) && ((side.includes('west') && pos[0] === 2) || (side.includes('east') && pos[0] === 20)))
  return {getSize:()=>structure.getSize(),getBlocks:()=>structure.getBlocks().filter(block=>visible(block.pos)),getBlock:pos=>visible(pos)?structure.getBlock(pos):null}
}
window.renderView = async view => {
  if (!cameras[view]) throw new Error('Unknown view '+view)
  const selection = provider(view)
  renderer.setStructure(selection)
  await renderer.whenReady()
  renderer.setCamera(cameras[view])
  renderer.drawStructure()
  await new Promise(resolve=>requestAnimationFrame(resolve))
  renderer.drawStructure()
  caption.textContent = 'P04 崩裂岩沿 · '+view.toUpperCase()+'\n23 × 16 × 27 · actual Java 1.20.1 NBT\n'+(view.startsWith('exterior-')?'Complete final shell':'Render-only cutaway; final roof intact')+'\nSHA-256 '+sourceHash.slice(0,24)+'…'
  return {view,sourceHash,storedRecords:structure.getBlocks().length,visibleRecords:selection.getBlocks().length,camera:cameras[view],cutaway:!view.startsWith('exterior-')}
}
window.previewReady = true
await window.renderView('preview')
