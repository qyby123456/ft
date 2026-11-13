import fs from 'node:fs'

export default function build(api) {
  const size = [21,12,21]
  const structure = api.createStructure(size)
  const box = (minimum,maximum,state) => api.fill(structure,minimum,maximum,`minecraft:${state}`)
  const put = (blockX,blockY,blockZ,state) => api.setBlock(structure,[blockX,blockY,blockZ],`minecraft:${state}`)
  box([0,0,0],[20,11,20],'air')
  box([0,0,0],[20,0,20],'prismarine_bricks')
  for (let blockX=1;blockX<20;blockX++) {
    for (let blockZ=1;blockZ<20;blockZ++) {
      if ((blockX*7+blockZ*11)%13<3) put(blockX,0,blockZ,'prismarine')
      if ((blockX*3+blockZ*5)%17===0) put(blockX,0,blockZ,'smooth_sandstone')
    }
  }
  box([0,1,0],[0,11,20],'prismarine_bricks')
  box([20,1,0],[20,11,20],'prismarine_bricks')
  box([0,1,0],[20,11,0],'prismarine_bricks')
  box([0,1,20],[20,11,20],'prismarine_bricks')
  box([0,11,0],[20,11,20],'dark_prismarine')
  for (const blockX of [1,5,15,19]) {
    for (const blockZ of [1,19]) {
      box([blockX,1,blockZ],[blockX,8,blockZ],'prismarine')
      box([blockX,8,blockZ],[blockX,9,blockZ],'dark_prismarine')
      put(blockX,7,blockZ,'sea_lantern')
    }
  }
  for (const blockZ of [5,15]) {
    for (const blockX of [1,19]) {
      box([blockX,1,blockZ],[blockX,7,blockZ],'prismarine')
      put(blockX,6,blockZ,'sea_lantern')
    }
  }
  box([7,1,7],[13,1,13],'prismarine')
  box([8,2,8],[12,2,12],'dark_prismarine')
  box([9,3,9],[11,3,11],'prismarine_bricks')
  for (const blockX of [7,13]) {
    for (const blockZ of [7,13]) put(blockX,2,blockZ,'sea_lantern')
  }
  const branch = (blockX,baseY,blockZ,color,tall) => {
    box([blockX,baseY,blockZ],[blockX,baseY+tall,blockZ],color)
    put(blockX-1,baseY+1,blockZ,color)
    put(blockX+1,baseY+1,blockZ,color)
    put(blockX-1,baseY+2,blockZ,color)
    put(blockX+1,baseY+2,blockZ,color)
  }
  branch(10,4,10,'pink_concrete',3)
  branch(8,3,9,'orange_terracotta',2)
  branch(12,3,11,'orange_terracotta',2)
  for (const [blockX,blockZ] of [[3,3],[17,17],[3,17],[17,3]]) {
    box([blockX-1,1,blockZ-1],[blockX+1,1,blockZ+1],'prismarine')
    branch(blockX,2,blockZ,(blockX+blockZ)%4===0?'pink_concrete':'orange_terracotta',2)
  }
  for (const blockZ of [5,13]) {
    for (const blockX of [2,16]) {
      box([blockX,1,blockZ],[blockX+2,1,blockZ+2],'dark_prismarine')
      box([blockX,2,blockZ],[blockX+2,3,blockZ+2],'glass')
      put(blockX+1,2,blockZ+1,'water[level=0]')
      put(blockX+1,1,blockZ+1,'sea_lantern')
    }
  }
  const ports = [
    {direction:'north',position:[10,1,0]},
    {direction:'south',position:[10,1,20]},
    {direction:'west',position:[0,1,10]},
    {direction:'east',position:[20,1,10]},
  ]
  for (const port of ports) {
    const [portX,,portZ] = port.position
    const axial = port.direction==='north' || port.direction==='south'
    const archHeight = port.direction==='south'?6:5
    if (axial) {
      box([portX-1,1,portZ],[portX+1,archHeight,portZ],'air')
      box([portX-2,1,portZ],[portX-2,archHeight,portZ],'dark_prismarine')
      box([portX+2,1,portZ],[portX+2,archHeight,portZ],'dark_prismarine')
      box([portX-2,archHeight+1,portZ],[portX+2,archHeight+1,portZ],'prismarine')
      put(portX-1,archHeight,portZ,'prismarine_bricks')
      put(portX+1,archHeight,portZ,'prismarine_bricks')
      put(portX,archHeight+2,portZ,'sea_lantern')
    } else {
      box([portX,1,portZ-1],[portX,archHeight,portZ+1],'air')
      box([portX,1,portZ-2],[portX,archHeight,portZ-2],'dark_prismarine')
      box([portX,1,portZ+2],[portX,archHeight,portZ+2],'dark_prismarine')
      box([portX,archHeight+1,portZ-2],[portX,archHeight+1,portZ+2],'prismarine')
      put(portX,archHeight,portZ-1,'prismarine_bricks')
      put(portX,archHeight,portZ+1,'prismarine_bricks')
      put(portX,archHeight+2,portZ,'sea_lantern')
    }
    port.clearance = [3,3]
    port.floor_y = 0
  }
  const metadata = {id:'NAT-S5-01',name:'中央珊瑚岛环厅',size,slug:'large-chamber',entrance:[10,1,0],interior:[10,1,3],facing:'north',ports,exits:ports.slice(1),authority:'S5-01-large-chamber.png; IMAGE-ANALYSIS.md',workflow:'ONE_IMAGE_ONLY',adaptation:'Dry ring route; solid-block coral sculptures; localized sealed water; final roof retained.'}
  fs.writeFileSync(new URL('./rooms/NAT-S5-01/room.json',import.meta.url),JSON.stringify(metadata,null,2))
  return {structure,metadata:{name:'room'}}
}
