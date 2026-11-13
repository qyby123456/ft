export function architecture(api, structure, size, options) {
  const put=(position,block)=>api.setBlock(structure,position,`minecraft:${block}`)
  for(let east=0;east<size[0];east++)for(let south=0;south<size[2];south++) {
    const edge=Math.min(east,size[0]-1-east,south,size[2]-1-south)
    const roof=options.roof(east,south,edge)
    const distance=Math.min(...options.veins.map(([centerEast,centerSouth,radiusEast,radiusSouth])=>((east-centerEast)/radiusEast)**2+((south-centerSouth)/radiusSouth)**2))
    put([east,roof,south],distance<1+0.17*Math.sin(east*1.9+south*1.3)?'amethyst_block':distance<1.5?'calcite':'cobbled_deepslate')
    if(edge===1)put([east,roof-1,south],distance<1?'amethyst_block':'cobbled_deepslate')
    if(edge===2){
      const facing=east===2?'east':east===size[0]-3?'west':south===2?'south':'north'
      put([east,roof-1,south],`deepslate_brick_stairs[facing=${facing},half=top,shape=straight,waterlogged=false]`)
    }
    if(edge===0)for(let height=8;height<roof;height++) {
      const ridge=9+Math.round(Math.sin(east*0.57+south*0.38)*2)
      const nearVein=distance<1.7&&Math.abs(height-ridge)<3
      put([east,height,south],nearVein?Math.abs(height-ridge)<2?'amethyst_block':'calcite':'cobbled_deepslate')
      if(nearVein&&height===ridge){
        const inset=[east===0?1:east===size[0]-1?east-1:east,height,south===0?1:south===size[2]-1?south-1:south]
        put(inset,'amethyst_block')
      }
    }
  }
  for(const port of options.ports){
    const alongEast=port.side==='north'||port.side==='south',coordinate=port.side==='west'||port.side==='north'?0:alongEast?size[2]-1:size[0]-1
    const inward=coordinate===0?1:-1,center=alongEast?port.foot[0]:port.foot[2],base=port.foot[1],start=center-Math.floor(port.width/2),top=base+port.height
    const location=(across,height,depth)=>alongEast?[across,height,coordinate+inward*depth]:[coordinate+inward*depth,height,across]
    for(let depth=0;depth<(port.frame_depth??3);depth++){
      for(const across of [start-2,start-1,start+port.width,start+port.width+1])for(let height=base;height<=top+1;height++)put(location(across,height,depth),'deepslate_bricks')
      for(let across=start;across<start+port.width;across++){
        const shoulder=across===start||across===start+port.width-1
        const facing=alongEast?(across===start?'east':'west'):(across===start?'south':'north')
        put(location(across,top,depth),shoulder?`deepslate_brick_stairs[facing=${facing},half=top,shape=straight,waterlogged=false]`:'air')
        put(location(across,top+1,depth),shoulder?'deepslate_tile_slab[type=bottom,waterlogged=false]':'chiseled_deepslate')
        put(location(across,top+2,depth),'deepslate_tile_slab[type=bottom,waterlogged=false]')
      }
    }
  }
}
