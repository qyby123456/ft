export default function build(api) {
  const structure = api.createStructure([27,17,29])
  const set = (across,vertical,forward,block,properties) => api.setBlock(structure,[across,vertical,forward],block,properties)
  const box = (start,end,block,properties) => api.fill(structure,start,end,block,properties)
  const leaf = {persistent:'true',distance:'7',waterlogged:'false'}
  const stone = (across,vertical,forward)=>['stone_bricks','mossy_stone_bricks','mossy_stone_bricks','cracked_stone_bricks','stone_bricks'][(across * 7 + vertical * 13 + forward * 11) % 5]
  box([0,0,0],[26,16,28],'air')
  box([0,0,0],[26,1,28],'stone_bricks')
  box([0,2,0],[26,2,23],'stone_bricks')
  box([0,3,0],[26,3,4],'stone_bricks')
  for(let across=0;across<27;across++)for(let forward=0;forward<29;forward++) {
    const floor = forward <= 4 ? 3 : forward <= 23 ? 2 : 1
    const ground = (across * 11 + forward * 3) % 19
    set(across,floor,forward,ground < 3 && across !== 12 && across !== 13 ? 'moss_block' : ground === 5 && across < 9 ? 'coarse_dirt' : stone(across,floor,forward))
    set(across,16,forward,'mossy_stone_bricks')
    if(across===0||across===26||forward===0||forward===28)for(let vertical=floor+1;vertical<16;vertical++)set(across,vertical,forward,stone(across,vertical,forward))
  }
  box([12,2,28],[13,6,28],'air')
  box([11,4,0],[13,9,0],'air')
  box([0,3,23],[26,6,23],'mossy_stone_bricks')
  box([1,2,24],[25,2,24],'stone_bricks')
  for(const across of [12,13]) {
    set(across,2,24,'stone_brick_slab',{type:'bottom',waterlogged:'false'})
    set(across,3,23,'dark_oak_door',{facing:'south',half:'lower',hinge:across===12?'left':'right',open:'false',powered:'false'})
    set(across,4,23,'dark_oak_door',{facing:'south',half:'upper',hinge:across===12?'left':'right',open:'false',powered:'false'})
  }
  for(const across of [10,15])box([across,3,22],[across+1,7,23],'stone_bricks')
  box([9,3,6],[16,12,7],'mossy_stone_bricks')
  box([11,3,6],[13,9,7],'air')
  box([12,10,6],[12,10,7],'air')
  box([11,3,5],[13,3,5],'stone_brick_slab',{type:'bottom',waterlogged:'false'})
  box([11,2,5],[13,2,5],'stone_bricks')
  for(const across of [8,15]) {
    box([across,3,6],[across+1,3,9],'chiseled_stone_bricks')
    box([across,4,6],[across+1,10,7],'stone_bricks')
    box([across,11,6],[across+1,12,8],'mossy_stone_bricks')
  }
  for(const [across,forward] of [[2,5],[2,13],[23,4],[23,15]]) {
    box([across,3,forward],[across+1,11,forward+1],'dark_oak_log',{axis:'y'})
    box([across,10,forward-1],[across+1,13,forward+2],'dark_oak_log',{axis:'y'})
    box([across-1,3,forward],[across+2,3,forward+2],'dark_oak_log',{axis:'z'})
  }
  box([2,12,3],[3,13,21],'dark_oak_log',{axis:'z'})
  box([23,12,3],[24,13,21],'dark_oak_log',{axis:'z'})
  box([3,13,4],[23,14,5],'dark_oak_log',{axis:'x'})
  box([2,10,13],[7,11,14],'dark_oak_log',{axis:'x'})
  box([19,10,14],[24,11,15],'dark_oak_log',{axis:'x'})
  for(const across of [3,21])for(const forward of [10,18])box([across,3,forward],[across+1,8,forward+1],'mossy_stone_bricks')
  for(const [across,forward,height] of [[3,18,1],[5,14,2],[4,9,2],[6,9,1],[19,9,1],[21,9,1],[23,9,1]]) {
    box([across,3,forward],[across+1,2+height,forward+1],'dark_oak_planks')
    for(let vertical=3;vertical<=2+height;vertical++)set(across,vertical,forward,'oak_log',{axis:'y'})
  }
  box([19,5,2],[24,8,2],'dark_oak_planks')
  box([20,6,3],[23,7,3],'birch_planks')
  for(const [across,vertical] of [[20,6],[22,6],[21,7],[23,7]])set(across,vertical,3,'mossy_cobblestone')
  box([22,3,19],[23,4,20],'stone_bricks')
  set(22,5,19,'lantern',{hanging:'false',waterlogged:'false'})
  for(let across=2;across<=7;across++)for(let forward=19;forward<=22;forward++) {
    const rim=across===2||across===7||forward===19||forward===22
    set(across,2,forward,rim?'mossy_stone_bricks':'water',rim?undefined:{level:'0'})
  }
  for(const [across,forward] of [[5,7],[20,7],[5,15],[21,15]]) {
    set(across,11,forward,'dark_oak_log',{axis:'x'})
    box([across,12,forward],[across,12,forward+1],'dark_oak_log',{axis:'z'})
    for(let vertical=10;vertical>=8;vertical--)set(across,vertical,forward,vertical===8?'cave_vines':'cave_vines_plant',vertical===8?{berries:'true',age:'25'}:{berries:'true'})
  }
  for(const [across,forward] of [[10,8],[14,8],[3,21],[24,21]]) {
    box([across,8,forward],[across,11,forward],'dark_oak_log',{axis:'y'})
    set(across,7,forward,'lantern',{hanging:'true',waterlogged:'false'})
  }
  for(const [across,forward] of [[1,17],[8,20],[24,12],[18,4],[2,26],[23,26]]) {
    const floor=forward<=4?3:forward<=23?2:1
    if(String(structure.getBlock([across,floor+1,forward]).state.getName())==='minecraft:air')set(across,floor+1,forward,'oak_leaves',leaf)
  }
  for(let across=1;across<26;across++)for(let forward=1;forward<23;forward++)for(const vertical of [12,13,14,15]) {
    if(!(across<7||across>20||forward<7)||(across*3+forward*7+vertical)%5>2||String(structure.getBlock([across,vertical,forward]).state.getName())!=='minecraft:air')continue
    const connected=[[1,0,0],[-1,0,0],[0,1,0],[0,-1,0],[0,0,1],[0,0,-1]].some(([deltaX,deltaY,deltaZ])=>String(structure.getBlock([across+deltaX,vertical+deltaY,forward+deltaZ])?.state.getName()??'minecraft:air')!=='minecraft:air')
    if(connected)set(across,vertical,forward,'oak_leaves',leaf)
  }
  const distances=new Map()
  let frontier=structure.getBlocks().filter(block=>String(block.state.getName()).endsWith('_log')).map(block=>block.pos)
  for(let distance=1;distance<=6;distance++) {
    const next=[]
    for(const [across,vertical,forward] of frontier)for(const [deltaX,deltaY,deltaZ] of [[1,0,0],[-1,0,0],[0,1,0],[0,-1,0],[0,0,1],[0,0,-1]]) {
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
