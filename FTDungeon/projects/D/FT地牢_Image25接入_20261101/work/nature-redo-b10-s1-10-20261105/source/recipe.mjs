import {size,regions,openings,inRegion} from './layout.mjs'
export default function build(api) {
  const structure=api.createStructure(size)
  const set=(across,vertical,forward,block,properties,nbt)=>api.setBlock(structure,[across,vertical,forward],block,properties,nbt)
  const box=(start,end,block,properties)=>api.fill(structure,start,end,block,properties)
  const leaf={persistent:'true',distance:'7',waterlogged:'false'}
  const stone=(across,vertical,forward)=>['stone_bricks','mossy_stone_bricks','mossy_stone_bricks','cracked_stone_bricks','stone_bricks'][(across*11+vertical*7+forward*13)%5]
  box([0,0,0],[60,12,60],'air')
  box([0,0,0],[60,0,60],'stone')
  box([0,1,0],[60,1,60],'grass_block',{snowy:'false'})
  for(const region of regions) {
    const [minX,minZ,maxX,maxZ]=region.bounds
    for(let across=minX;across<=maxX;across++)for(let forward=minZ;forward<=maxZ;forward++) {
      set(across,1,forward,(across*7+forward*11)%17<4?'moss_block':stone(across,1,forward))
      if(region.roof)set(across,12,forward,'mossy_stone_bricks')
      if(across===minX||across===maxX||forward===minZ||forward===maxZ)for(let vertical=2;vertical<=11;vertical++)set(across,vertical,forward,stone(across,vertical,forward))
    }
  }
  for(const region of regions.slice(0,6)) {
    const [minX,minZ,maxX,maxZ]=region.bounds
    box([minX+1,2,minZ+1],[maxX-1,7,maxZ-1],'air')
  }
  for(const [minX,minZ,maxX,maxZ] of openings)box([minX,2,minZ],[maxX,7,maxZ],'air')
  for(const region of regions.filter(region=>!region.id.includes('corridor')&&!region.id.includes('elbow')&&!region.id.includes('link'))) {
    const [minX,minZ,maxX,maxZ]=region.bounds
    for(const across of [minX+1,maxX-1])for(const forward of [minZ+1,maxZ-1]) {
      box([across,2,forward],[across,10,forward],'dark_oak_log',{axis:'y'})
      box([across,10,forward],[across,11,forward],'oak_leaves',leaf)
    }
    for(let across=minX+1;across<maxX;across++) {
      set(across,10,minZ+1,'dark_oak_log',{axis:'x'})
      set(across,10,maxZ-1,'dark_oak_log',{axis:'x'})
      if(across%3!==0){set(across,11,minZ+1,'oak_leaves',leaf);set(across,11,maxZ-1,'oak_leaves',leaf)}
    }
    if(region.roof)for(const across of [minX+3,maxX-3]) {
      const forward=minZ+3
      box([across,9,forward],[across,11,forward],'dark_oak_log',{axis:'y'})
      set(across,8,forward,'lantern',{hanging:'true',waterlogged:'false'})
    }
  }
  box([8,3,22],[15,3,26],'oak_planks')
  for(const across of [8,15])for(const forward of [22,26])set(across,2,forward,'oak_log',{axis:'y'})
  for(const across of [9,12,15])for(const forward of [20,28])set(across,2,forward,'oak_stairs',{facing:forward===20?'south':'north',half:'bottom',shape:'straight',waterlogged:'false'})
  for(const [across,forward] of [[5,18],[6,18],[5,30],[23,24]])box([across,2,forward],[across,3,forward],'stripped_oak_log',{axis:'y'})
  for(let across=17;across<=26;across++)for(let forward=6;forward<=11;forward++) {
    const rim=across===17||across===26||forward===6||forward===11
    set(across,1,forward,rim?'mossy_stone_bricks':'water',rim?undefined:{level:'0'})
  }
  box([15,2,4],[17,6,6],'mossy_cobblestone')
  box([16,2,5],[16,5,5],'water',{level:'0'})
  box([16,2,6],[16,5,6],'glass')
  for(const across of [34,38,42,53,55])box([across,2,4],[across,6,4],'bookshelf')
  for(const across of [33,55])for(const forward of [7,15,24]) {
    box([across,2,forward],[across,8,forward],'stone_bricks')
    box([across-1,8,forward],[across+1,9,forward],'chiseled_stone_bricks')
  }
  box([43,2,5],[51,2,9],'stone_bricks')
  box([45,2,5],[49,2,8],'gold_block')
  box([46,2,10],[48,2,10],'stone_brick_slab',{type:'bottom',waterlogged:'false'})
  set(47,3,7,'chest',{facing:'south',type:'single',waterlogged:'false'},api.createBlockNbt({id:'minecraft:chest'}))
  set(8,2,44,'chest',{facing:'south',type:'single',waterlogged:'false'},api.createBlockNbt({id:'minecraft:chest'}))
  box([7,1,46],[11,1,49],'red_terracotta')
  for(const across of [5,17]) {
    box([across,2,40],[across,5,40],'bookshelf')
    set(across,6,40,'lantern',{hanging:'false',waterlogged:'false'})
  }
  box([37,2,39],[39,8,41],'dark_oak_log',{axis:'y'})
  box([34,8,40],[43,9,40],'dark_oak_log',{axis:'x'})
  box([38,8,36],[38,9,44],'dark_oak_log',{axis:'z'})
  box([36,2,40],[40,3,40],'dark_oak_log',{axis:'x'})
  for(let across=33;across<=44;across++)for(let forward=35;forward<=44;forward++)if(Math.hypot((across-38.5)/1.2,forward-39.5)<5) {
    set(across,10,forward,'oak_leaves',leaf)
    if((across+forward)%3!==0)set(across,11,forward,'oak_leaves',leaf)
  }
  for(let across=39;across<=45;across++)for(let forward=43;forward<=46;forward++) {
    const rim=across===39||across===45||forward===43||forward===46
    set(across,1,forward,rim?'mossy_stone_bricks':'water',rim?undefined:{level:'0'})
  }
  for(const [across,forward] of [[34,40],[42,40]]) {
    set(across,7,forward,'cave_vines_plant',{berries:'true'})
    set(across,6,forward,'cave_vines',{berries:'true',age:'25'})
  }
  for(const [across,forward] of [[20,17],[24,4],[34,5],[54,6],[5,41],[18,50]]) {
    box([across,10,forward],[across,11,forward],'dark_oak_log',{axis:'y'})
    set(across,9,forward,'cave_vines_plant',{berries:'true'})
    set(across,8,forward,'cave_vines',{berries:'true',age:'25'})
  }
  for(const [across,forward] of [[5,24],[18,44],[28,32],[48,44]]) {
    set(across,2,forward,'moss_block')
    set(across,3,forward,'potted_red_mushroom')
  }
  for(const across of [47,53]) {
    box([across,2,59],[across,7,59],'chiseled_stone_bricks')
    set(across,4,60,'lantern',{hanging:'false',waterlogged:'false'})
    set(across,3,60,'stone_bricks')
  }
  box([47,8,59],[53,9,59],'dark_oak_log',{axis:'x'})
  const distances=new Map()
  let frontier=structure.getBlocks().filter(block=>String(block.state.getName()).endsWith('_log')).map(block=>block.pos)
  for(let distance=1;distance<=6;distance++) {
    const next=[]
    for(const [across,vertical,forward] of frontier)for(const [deltaX,deltaY,deltaZ] of [[1,0,0],[-1,0,0],[0,1,0],[0,0,1],[0,0,-1],[0,-1,0]]) {
      const point=[across+deltaX,vertical+deltaY,forward+deltaZ]
      const block=structure.getBlock(point)
      const key=point.join(',')
      if(!distances.has(key)&&block&&String(block.state.getName()).endsWith('_leaves')){distances.set(key,distance);next.push(point)}
    }
    frontier=next
  }
  for(const block of structure.getBlocks())if(String(block.state.getName()).endsWith('_leaves'))api.setBlock(structure,block.pos,'oak_leaves',{...leaf,distance:String(distances.get(block.pos.join(','))??7)})
  return {structure,metadata:{name:'room'}}
}
