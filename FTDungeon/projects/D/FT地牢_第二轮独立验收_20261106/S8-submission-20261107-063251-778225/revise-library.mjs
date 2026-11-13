export function reviseLibrary(api, structure, size) {
  const put = (position, block) => api.setBlock(structure, position, `minecraft:${block}`)
  const patches = [[3,5,5,4],[25,23,6,5],[21,3,5,3],[7,24,4,3]]
  for (let east=0;east<size[0];east++) for(let south=0;south<size[2];south++) {
    const edge=Math.min(east,size[0]-1-east,south,size[2]-1-south),roof=12+Math.min(2,edge)
    const distance=Math.min(...patches.map(([centerEast,centerSouth,radiusEast,radiusSouth])=>((east-centerEast)/radiusEast)**2+((south-centerSouth)/radiusSouth)**2))
    put([east,roof,south],distance<1+Math.sin(east*1.7+south*2.3)*0.18?'amethyst_block':distance<1.65?'calcite':'cobbled_deepslate')
    if(edge===1)put([east,roof-1,south],distance<1.1?'amethyst_block':'cobbled_deepslate')
    if(edge===2&&(south<10||south>21)) {
      const facing=east===2?'east':east===size[0]-3?'west':south===2?'south':'north'
      put([east,roof-1,south],`deepslate_brick_stairs[facing=${facing},half=top,shape=straight,waterlogged=false]`)
    }
  }
  for(const side of ['west','east']) {
    const wall=side==='west'?0:30,inward=side==='west'?1:-1
    for(let depth=0;depth<3;depth++) {
      const east=wall+inward*depth
      for(const south of [13,14,18,19])for(let height=2;height<=(south===14||south===18?6:7);height++)put([east,height,south],height===3?'deepslate_bricks':'polished_deepslate')
      for(const south of [15,17])put([east,6,south],`deepslate_brick_stairs[facing=${south===15?'south':'north'},half=top,shape=straight,waterlogged=false]`)
      put([east,6,16],'air')
      put([east,7,16],'chiseled_deepslate')
      for(let south=14;south<=18;south++) {
        if(south!==16)put([east,7,south],'deepslate_bricks')
        put([east,8,south],'deepslate_tile_slab[type=bottom,waterlogged=false]')
      }
    }
    for(let south=3;south<=25;south++) {
      const height=9+Math.round(Math.sin(south*0.53+(side==='west'?0:2)))
      put([wall+inward,height,south],south<8||south>21?'amethyst_block':'cobbled_deepslate')
      if(south%3===0)put([wall+inward,height+1,south],'calcite')
      if(south===5||south===23)put([wall+inward*2,height,south],`amethyst_cluster[facing=${side==='west'?'east':'west'},waterlogged=false]`)
    }
  }
}
