export function tools(api, size) {
  const structure = api.createStructure(size)
  const defaults = block => block.endsWith('_slab') ? {type:'bottom',waterlogged:'false'} : block.endsWith('_stairs') ? {half:'bottom',shape:'straight',facing:'north',waterlogged:'false'} : block==='barrel' ? {facing:'north',open:'false'} : block==='lectern' ? {facing:'north',powered:'false',has_book:'false'} : block==='ladder' ? {facing:'north',waterlogged:'false'} : block==='decorated_pot' ? {facing:'north',cracked:'false',waterlogged:'false'} : undefined
  const put = (position, block, properties, nbt) => api.setBlock(structure, position, block.includes(':') ? block : `minecraft:${block}`, {...defaults(block),...properties}, nbt ? api.createBlockNbt(nbt) : undefined)
  const fill = (start, end, block) => {for(let up=start[1];up<=end[1];up++) for(let south=start[2];south<=end[2];south++) for(let east=start[0];east<=end[0];east++) put([east,up,south],block)}
  const container = (position, block = 'barrel', facing = 'north') => put(position, block, {facing}, {id:`minecraft:${block}`, Items: new api.NbtList([], 10)})
  const arch = (side, center, feet = 2, width = 3, height = 4) => {
    const across = (offset, up) => side === 'north' || side === 'south' ? [center+offset,up,side==='north'?0:size[2]-1] : [side==='west'?0:size[0]-1,up,center+offset]
    const radius = Math.floor(width / 2)
    for(let offset=-radius-1;offset<=radius+1;offset++) for(let up=feet;up<=feet+height;up++) if(Math.abs(offset)===radius+1 || up===feet+height) put(across(offset,up),'chiseled_sandstone')
    for(let offset=-radius;offset<=radius;offset++) for(let up=feet;up<feet+height;up++) put(across(offset,up),'air')
    const inset = position => side==='west'?[1,position[1],position[2]]:side==='east'?[size[0]-2,position[1],position[2]]:side==='north'?[position[0],position[1],1]:[position[0],position[1],size[2]-2]
    for(const offset of [-radius-1,radius+1]) {
      for(let up=feet;up<feet+height+1;up++) put(inset(across(offset,up)),up===feet||up===feet+height?'chiseled_sandstone':'sandstone')
      put(inset(across(offset,feet+height+1)),'smooth_sandstone_slab',{type:'bottom'})
    }
    for(let offset=-radius;offset<=radius;offset++) {
      put(inset(across(offset,feet+height)),'sandstone_stairs',{facing:side,half:'top',shape:'straight'})
      put(inset(across(offset,feet+height+1)),'cut_sandstone_slab',{type:'bottom'})
    }
    for(const offset of [-radius-2,radius+2]) put(inset(across(offset,feet+height)),'chiseled_sandstone')
    return {id:side, side, feet:across(0,feet), width, clearHeight:height}
  }
  const shell = (height, accent = 4) => {
    fill([0,0,0],[size[0]-1,size[1]-1,size[2]-1],'air')
    fill([0,0,0],[size[0]-1,1,size[2]-1],'sandstone')
    for(let east=1;east<size[0]-1;east++) for(let south=1;south<size[2]-1;south++) put([east,1,south], (east+south)%9===0?'cut_sandstone':'smooth_sandstone')
    for(let up=2;up<height-1;up++) {
      const material = up===accent?'orange_terracotta':up===height-2?'chiseled_sandstone':'cut_sandstone'
      fill([0,up,0],[size[0]-1,up,0],material); fill([0,up,size[2]-1],[size[0]-1,up,size[2]-1],material)
      fill([0,up,0],[0,up,size[2]-1],material); fill([size[0]-1,up,0],[size[0]-1,up,size[2]-1],material)
    }
    fill([0,height-1,0],[size[0]-1,height-1,size[2]-1],'smooth_sandstone')
    for(let east=1;east<size[0]-1;east++) for(let south=1;south<size[2]-1;south++) {
      const edge=east===2||east===size[0]-3||south===2||south===size[2]-3
      const center=Math.abs(east-Math.floor(size[0]/2))+Math.abs(south-Math.floor(size[2]/2))
      if(edge||center===3) put([east,1,south],'orange_terracotta')
      if(center===0) put([east,1,south],'chiseled_sandstone')
    }
    for(const east of [1,size[0]-2]) for(const south of [1,size[2]-2]) {
      fill([east,2,south],[east,height-3,south],'sandstone')
      put([east,2,south],'chiseled_sandstone');put([east,height-4,south],'orange_terracotta');put([east,height-3,south],'chiseled_sandstone')
    }
    for(let east=1;east<size[0]-1;east++) for(const south of [1,size[2]-2]) {put([east,height-2,south],'sandstone_stairs',{facing:south===1?'south':'north',half:'top'});put([east,height-3,south],'cut_sandstone_slab',{type:'top'})}
    for(let south=2;south<size[2]-2;south++) for(const east of [1,size[0]-2]) {put([east,height-2,south],'sandstone_stairs',{facing:east===1?'east':'west',half:'top'});put([east,height-3,south],'cut_sandstone_slab',{type:'top'})}
  }
  const pillar = (east,south,height) => {fill([east,2,south],[east,height-2,south],'sandstone'); put([east,2,south],'chiseled_sandstone'); put([east,height-3,south],'orange_terracotta');put([east,height-2,south],'chiseled_sandstone');for(const offset of [-1,1]) if(east+offset>0&&east+offset<size[0]-1) put([east+offset,height-2,south],'sandstone_stairs',{facing:offset<0?'east':'west',half:'top'})}
  const banner = (east,up,south,facing='south') => put([east,up,south],'orange_wall_banner',{facing},{id:'minecraft:banner',Patterns:[{Pattern:'bo',Color:4},{Pattern:'mr',Color:4},{Pattern:'mc',Color:1}]})
  const lamp = (east,ceiling,south) => {put([east,ceiling-1,south],'chain',{axis:'y'});put([east,ceiling-2,south],'lantern',{hanging:'true'})}
  const pot = (east,up,south) => put([east,up,south],'decorated_pot',undefined,{id:'minecraft:decorated_pot'})
  return {structure,put,fill,container,arch,shell,pillar,lamp,pot,banner}
}
