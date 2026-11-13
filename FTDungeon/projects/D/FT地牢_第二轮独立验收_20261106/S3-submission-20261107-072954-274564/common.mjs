import fs from 'node:fs'
import path from 'node:path'
import {fileURLToPath} from 'node:url'
export function toolkit(api,size,id,explicitDefaults=false) {
  const structure=api.createStructure(size),cells=new Map()
  function put(x,y,z,state,entity) {
    if(state==='spruce_log')state='spruce_log[axis=y]'
    if(state.startsWith('barrel[')&&!state.includes('open='))state=state.replace(']',',open=false]')
    cells.set([x,y,z].join(','),{pos:[x,y,z],state,entity:entity??null})
    api.setBlock(structure,[x,y,z],`minecraft:${state}`,undefined,entity?api.createBlockNbt(entity):undefined)
  }
  function box(x1,y1,z1,x2,y2,z2,state,entity) {
    for(let east=x1;east<=x2;east++)for(let up=y1;up<=y2;up++)for(let south=z1;south<=z2;south++)put(east,up,south,state,entity)
  }
  function lamp(x,z,ceiling) {box(x,ceiling-2,z,x,ceiling-1,z,'chain[axis=y]');put(x,ceiling-3,z,'lantern[hanging=true]')}
  function icicle(x,z,ceiling,length=3) {
    const varied=length+(x%3)
    box(x,ceiling-1,z,Math.min(x+1,size[0]-1),ceiling,Math.min(z+1,size[2]-1),'packed_ice')
    for(let down=2;down<varied;down++)put(x,ceiling-down,z,'blue_ice')
    put(x,ceiling-varied,z,'light_blue_stained_glass_pane')
  }
  function finish(details) {
    const top=size[1]-3,back=size[2]-2,right=size[0]-2
    box(0,top,0,size[0]-1,top,1,'snow_block')
    box(0,top,back,size[0]-1,top,size[2]-1,'snow_block')
    box(0,top,2,1,top,back-1,'snow_block')
    box(right,top,2,size[0]-1,top,back-1,'snow_block')
    for(let east=3;east<size[0]-3;east+=8){
      box(east,2,back-1,east,top-1,back-1,'spruce_log[axis=y]')
      put(east,top-2,back-2,'stone_brick_slab[type=top]')
      put(east,top+1,back,'snow_block')
    }
    box(2,top-2,back-1,right-1,top-2,back-1,'spruce_log[axis=x]')
    box(2,top-3,back-1,right-1,top-3,back,'stone_bricks')
    box(2,top-2,back-2,right-1,top-2,back,'stone_brick_slab[type=top]')
    for(let east=3;east<size[0]-4;east+=6){
      const length=2+(east%4)
      box(east,top-1,back-3,east+2,top,back,'packed_ice')
      box(east+1,top-length,back-2,east+1,top-2,back-1,'blue_ice')
      put(east+1,top-length-1,back-2,'light_blue_stained_glass_pane')
      box(east,top+1,back-2,east+2,top+1,back,'snow_block')
    }
    for(const port of details.ports){
      const [east,up,south]=port.foot,offset=Math.floor(port.width/2)+1
      const eastWest=port.normal[0]!==0
      const inward=port.normal.map(value=>-value)
      for(let depth=0;depth<3;depth++){
        const frameEast=east+inward[0]*depth,frameSouth=south+inward[2]*depth
        for(const side of [-offset,offset]){
          const columnEast=frameEast+(eastWest?0:side),columnSouth=frameSouth+(eastWest?side:0)
          box(columnEast,up,columnSouth,columnEast,up+port.height,columnSouth,depth===1?'stone_bricks':'chiseled_stone_bricks')
        }
        box(frameEast-(eastWest?0:offset),up+port.height,frameSouth-(eastWest?offset:0),frameEast+(eastWest?0:offset),up+port.height+1,frameSouth+(eastWest?offset:0),'stone_bricks')
      }
      for(const side of [-offset,offset]){
        const columnEast=east+(eastWest?0:side),columnSouth=south+(eastWest?side:0)
        box(columnEast,up,columnSouth,columnEast,up+port.height,columnSouth,'chiseled_stone_bricks')
        put(columnEast,up+port.height+1,columnSouth,'snow_block')
      }
      box(east-(eastWest?0:offset),up+port.height,south-(eastWest?offset:0),east+(eastWest?0:offset),up+port.height,south+(eastWest?offset:0),eastWest?'spruce_log[axis=z]':'spruce_log[axis=x]')
    }
    details.architectural_revision='石木门框、后壁梁柱、连续雪沿、错落收尖冰挂；显式木纹轴向和木桶open=false使原生预览与实际一致'
    const folder=path.join(path.dirname(fileURLToPath(import.meta.url)),'rooms',id)
    fs.mkdirSync(folder,{recursive:true})
    fs.writeFileSync(path.join(folder,'design.json'),JSON.stringify({id,size,...details},null,2))
    fs.writeFileSync(path.join(folder,'cells.json'),JSON.stringify([...cells.values()]))
    return {structure,metadata:{name:id}}
  }
  return {structure,put,box,lamp,icicle,finish}
}
