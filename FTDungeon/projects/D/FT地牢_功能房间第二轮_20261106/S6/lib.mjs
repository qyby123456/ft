export function room(api, size, id) {
  const structure = api.createStructure(size)
  const [width,height,depth] = size
  const defaults = block => ['basalt','polished_basalt','chain'].includes(block)?{axis:'y'}:undefined
  const put = (x,y,z,block,properties,nbt) => api.setBlock(structure,[x,y,z],`minecraft:${block}`,properties??defaults(block),nbt)
  const fill = (from,to,block) => api.fill(structure,from,to,`minecraft:${block}`,defaults(block))
  fill([0,0,0],[width-1,height-1,depth-1],'air')
  fill([0,0,0],[width-1,2,depth-1],'blackstone')
  for (let x=0;x<width;x++) for(let z=0;z<depth;z++) put(x,2,z,(x+z)%7===0?'gilded_blackstone':'polished_blackstone_bricks')
  for(let y=3;y<height;y++) {
    fill([0,y,0],[width-1,y,0],'basalt')
    fill([0,y,depth-1],[width-1,y,depth-1],'basalt')
    fill([0,y,0],[0,y,depth-1],'smooth_basalt')
    fill([width-1,y,0],[width-1,y,depth-1],'smooth_basalt')
  }
  fill([0,height-1,0],[width-1,height-1,depth-1],'polished_blackstone_bricks')
  const pillar = (x,z) => {
    fill([x,3,z],[x,height-2,z],'obsidian')
    put(x,7,z,'shroomlight')
    put(x,8,z,'polished_blackstone_bricks')
  }
  const light = (x,z) => {
    fill([x,height-3,z],[x,height-2,z],'chain')
    put(x,height-4,z,'lantern',{hanging:'true'})
  }
  const chest = (x,y,z,facing='south') => put(x,y,z,'chest',{facing,type:'single',waterlogged:'false'},api.createBlockNbt({id:'minecraft:chest',Items:[]}))
  const port = (side,center) => {
    const east = side==='west'?0:side==='east'?width-1:center
    const south = side==='north'?0:side==='south'?depth-1:center
    for(let across=-1;across<=1;across++) for(let y=3;y<=6;y++) put(east+(side==='north'||side==='south'?across:0),y,south+(side==='east'||side==='west'?across:0),'air')
    return {side,feet:[east,3,south],width:3,clearHeight:4}
  }
  const lava = (from,to) => {
    fill([from[0]-1,from[1]-1,from[2]-1],[to[0]+1,to[1]+1,to[2]+1],'obsidian')
    fill(from,to,'lava')
    fill([from[0],to[1]+1,from[2]],[to[0],to[1]+1,to[2]],'orange_stained_glass')
  }
  return {structure,put,fill,pillar,light,chest,port,lava,finish:()=>({structure,metadata:{name:id}})}
}
