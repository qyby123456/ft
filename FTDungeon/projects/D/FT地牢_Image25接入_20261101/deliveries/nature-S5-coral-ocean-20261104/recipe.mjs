import fs from 'node:fs'

const rooms = [
  ['large-chamber','珊瑚环礁大厅',[19,11,19],['north','south','east']],
  ['medium-room','海晶休憩室',[15,9,15],['north','south']],
  ['small-room','珊瑚守望龛',[9,7,11],['north','east']],
  ['great-hall','潮汐柱廊',[21,11,15],['north','south']],
  ['corridor','海光直廊',[11,8,19],['north','south']],
  ['corner','珊瑚转角廊',[15,8,15],['north','east']],
  ['entrance','海底遗迹前厅',[17,10,15],['north','south']],
  ['courtyard','水镜天井庭院',[19,12,19],['north','south','west']],
  ['treasure','潮心祭坛宝库',[17,10,17],['north','east']],
]

export default function build(api) {
  const index = Number(process.env.S5_ROOM_INDEX)
  const [slug,name,,directions] = rooms[index]
  const authority = JSON.parse(fs.readFileSync(new URL('./source-evidence.json', import.meta.url), 'utf8'))
  const source = authority.modules[index]
  const size = source.size
  const [width,height,depth] = size
  const centerX = Math.floor(width/2)
  const centerZ = Math.floor(depth/2)
  const structure = api.createStructure(size)
  const put = (blockX,blockY,blockZ,state) => api.setBlock(structure,[blockX,blockY,blockZ],`minecraft:${state}`)
  const box = (minimum,maximum,state) => api.fill(structure,minimum,maximum,`minecraft:${state}`)
  box([0,0,0],[width-1,height-1,depth-1],'air')
  box([0,0,0],[width-1,0,depth-1],'prismarine_bricks')
  for (let blockX=0;blockX<width;blockX++) {
    for (let blockZ=0;blockZ<depth;blockZ++) {
      if (blockX%4===0 || blockZ%4===0) put(blockX,0,blockZ,'dark_prismarine')
      if (Math.abs(blockX-centerX)<=1 || Math.abs(blockZ-centerZ)<=1) put(blockX,0,blockZ,(blockX+blockZ)%3===0?'smooth_sandstone':'prismarine_bricks')
    }
  }
  box([0,1,0],[0,height-1,depth-1],'prismarine_bricks')
  box([width-1,1,0],[width-1,height-1,depth-1],'prismarine_bricks')
  box([0,1,0],[width-1,height-1,0],'prismarine_bricks')
  box([0,1,depth-1],[width-1,height-1,depth-1],'prismarine_bricks')
  box([0,height-1,0],[width-1,height-1,depth-1],'prismarine_bricks')
  for (let blockZ=3;blockZ<depth-2;blockZ+=4) {
    for (const blockX of [1,width-2]) {
      box([blockX,1,blockZ],[blockX,height-2,blockZ],'prismarine')
      put(blockX,height-3,blockZ,'sea_lantern')
      put(blockX===1?0:width-1,3,blockZ+1,'cyan_stained_glass')
    }
    box([1,height-2,blockZ],[width-2,height-2,blockZ],'dark_prismarine')
  }
  const aquarium = (originX,originZ,span=3) => {
    box([originX,1,originZ],[originX+span-1,1,originZ+span-1],'dark_prismarine')
    box([originX,2,originZ],[originX+span-1,4,originZ+span-1],'glass')
    box([originX+1,2,originZ+1],[originX+span-2,3,originZ+span-2],'water[level=0]')
    put(originX+1,2,originZ+1,index%2?'fire_coral_block':'brain_coral_block')
    put(originX+1,1,originZ+1,'sea_lantern')
  }
  if (slug==='large-chamber') {
    aquarium(centerX-4,3,5)
    aquarium(centerX,depth-8,5)
    box([2,1,centerZ],[3,2,centerZ],'prismarine')
    put(2,3,centerZ,'sea_lantern')
  } else if (slug==='medium-room') {
    aquarium(1,depth-5)
    aquarium(width-4,2)
    box([1,1,5],[centerX+1,3,5],'cyan_stained_glass')
    box([centerX-1,1,depth-6],[width-2,3,depth-6],'cyan_stained_glass')
  } else if (slug==='courtyard') {
    aquarium(centerX-2,centerZ-2,5)
    box([2,3,8],[4,3,depth-3],'prismarine_bricks')
    box([2,1,8],[4,2,8],'prismarine')
    box([2,1,depth-3],[4,2,depth-3],'prismarine')
    box([5,4,9],[5,4,depth-4],'glass')
    for (let step=0;step<3;step++) {
      box([2,1,5+step],[4,step+1,5+step],'prismarine_bricks')
      box([2,step+1,5+step],[4,step+1,5+step],'prismarine_brick_stairs[facing=south,half=bottom,shape=straight,waterlogged=false]')
    }
    box([centerX-3,height-1,centerZ-3],[centerX+3,height-1,centerZ+3],'glass')
  } else if (slug==='corner') {
    box([1,1,1],[centerX-3,height-2,centerZ-3],'prismarine_bricks')
    box([centerX+3,1,1],[width-2,height-2,centerZ-3],'prismarine_bricks')
    box([1,1,centerZ+3],[width-2,height-2,depth-2],'prismarine_bricks')
    aquarium(1,centerZ-2)
  } else if (slug==='corridor') {
    for (const blockZ of [2,8,14]) {
      aquarium(1,blockZ)
      aquarium(width-4,blockZ)
      put(centerX,height-2,blockZ+1,'sea_lantern')
    }
  } else if (slug==='small-room') {
    aquarium(1,depth-4)
    put(width-2,1,depth-2,'chiseled_quartz_block')
    put(width-2,2,depth-2,'sea_lantern')
  } else {
    for (const originX of [2,width-5]) {
      for (const originZ of [2,depth-5]) aquarium(originX,originZ)
    }
    if (slug==='great-hall') {
      for (const blockX of [6,width-7]) {
        for (const blockZ of [4,depth-5]) {
          box([blockX,1,blockZ],[blockX,height-2,blockZ],'prismarine')
          put(blockX,3,blockZ,'sea_lantern')
          box([blockX-1,height-3,blockZ-1],[blockX+1,height-3,blockZ+1],'dark_prismarine')
        }
      }
    } else if (slug==='entrance') {
      for (const blockX of [centerX-3,centerX+3]) {
        box([blockX,1,1],[blockX,height-3,1],'dark_prismarine')
        put(blockX,3,1,'sea_lantern')
      }
      box([centerX-3,height-3,1],[centerX+3,height-3,1],'prismarine')
      box([2,1,centerZ+2],[4,1,centerZ+2],'smooth_sandstone')
    } else if (slug==='treasure') {
      box([centerX-1,1,depth-6],[centerX+1,1,depth-4],'smooth_quartz')
      put(centerX,2,depth-5,'sea_lantern')
      put(centerX,3,depth-5,'chiseled_quartz_block')
      for (const blockX of [centerX-3,centerX+3]) box([blockX,1,depth-5],[blockX,height-2,depth-5],'prismarine')
    } else {
      const span = slug==='medium-room' ? 3 : 5
      aquarium(centerX-Math.floor(span/2),centerZ-Math.floor(span/2),span)
      if (slug==='courtyard') box([centerX-3,height-1,centerZ-3],[centerX+3,height-1,centerZ+3],'glass')
    }
  }
  const ports = directions.map(direction => ({direction,position:direction==='north'?[centerX,1,0]:direction==='south'?[centerX,1,depth-1]:direction==='east'?[width-1,1,centerZ]:[0,1,centerZ],clearance:[3,3],floor_y:0}))
  for (const port of ports) {
    const [portX,,portZ] = port.position
    if (port.direction==='north' || port.direction==='south') {
      box([portX-1,1,portZ],[portX+1,3,portZ],'air')
      box([portX-2,4,portZ],[portX+2,4,portZ],'dark_prismarine')
      put(portX-2,3,portZ,'sea_lantern')
      put(portX+2,3,portZ,'sea_lantern')
    } else {
      box([portX,1,portZ-1],[portX,3,portZ+1],'air')
      box([portX,4,portZ-2],[portX,4,portZ+2],'dark_prismarine')
      put(portX,3,portZ-2,'sea_lantern')
      put(portX,3,portZ+2,'sea_lantern')
    }
  }
  fs.writeFileSync(process.env.S5_SPEC_PATH,JSON.stringify({id:`NAT-S5-${String(index+1).padStart(2,'0')}`,name,slug,size,source,entrance:[centerX,1,0],interior:[centerX,1,2],facing:'north',ports,exits:ports.slice(1),adaptation:'Dry walkways and sealed live-coral aquaria; glass skylight; no loot or entities.'},null,2))
  return {structure,metadata:{name:'room'}}
}
