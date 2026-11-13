export default function build(api) {
  const structure=api.createStructure([35,22,35])
  const set=(across,vertical,forward,block,properties)=>api.setBlock(structure,[across,vertical,forward],block,properties)
  const box=(start,end,block,properties)=>api.fill(structure,start,end,block,properties)
  const leaf={persistent:'true',distance:'7',waterlogged:'false'}
  const stone=(across,vertical,forward)=>['stone_bricks','mossy_stone_bricks','mossy_stone_bricks','cracked_stone_bricks','stone_bricks'][(across*11+vertical*7+forward*13)%5]
  box([0,0,0],[34,21,34],'air')
  box([0,0,0],[34,0,34],'stone_bricks')
  for(let across=0;across<35;across++)for(let forward=0;forward<35;forward++) {
    const ground=(across*7+forward*11)%19
    set(across,1,forward,ground<4?'moss_block':ground===5?'coarse_dirt':stone(across,1,forward))
    if(!(across>=8&&across<=26&&forward>=8&&forward<=26))set(across,21,forward,'mossy_stone_bricks')
    if(across===0||across===34||forward===0||forward===34)for(let vertical=2;vertical<21;vertical++)set(across,vertical,forward,stone(across,vertical,forward))
  }
  for(const forward of [0,34])box([9,2,forward],[11,6,forward],'air')
  box([16,2,34],[18,6,34],'air')
  box([9,2,34],[11,6,34],'stone_bricks')
  box([0,2,25],[0,6,27],'air')
  box([34,2,8],[34,6,10],'air')
  box([2,9,2],[32,9,6],'mossy_stone_bricks')
  box([28,9,6],[32,9,32],'mossy_stone_bricks')
  box([2,9,28],[32,9,32],'mossy_stone_bricks')
  box([2,9,6],[6,9,8],'mossy_stone_bricks')
  box([2,10,7],[6,10,7],'air')
  for(let forward=9;forward<=24;forward++) {
    const feet=2+(25-forward)*.5
    const fullTop=Math.floor(feet)-1
    box([3,2,forward],[5,fullTop,forward],'stone_bricks')
    if(feet%1)set(3,Math.floor(feet),forward,'stone_brick_slab',{type:'bottom',waterlogged:'false'})
    if(feet%1)for(const across of [4,5])set(across,Math.floor(feet),forward,'stone_brick_slab',{type:'bottom',waterlogged:'false'})
    box([2,2,forward],[2,Math.ceil(feet),forward],'mossy_stone_bricks')
    box([6,2,forward],[6,Math.ceil(feet),forward],'mossy_stone_bricks')
  }
  for(const across of [2,7,27,33])for(const forward of [2,14,26,32]) {
    if(across===2&&forward===14)continue
    if(forward===26&&across<8)continue
    if(across===7&&forward===14)continue
    box([across,2,forward],[across,8,forward],'stone_bricks')
    box([across,8,forward],[across+1,9,forward+1],'dark_oak_log',{axis:'x'})
  }
  box([2,8,2],[32,8,3],'dark_oak_log',{axis:'x'})
  box([31,8,3],[32,8,32],'dark_oak_log',{axis:'z'})
  box([2,8,31],[32,8,32],'dark_oak_log',{axis:'x'})
  for(let across=7;across<=27;across++) {
    set(across,10,7,'oak_fence',{waterlogged:'false'})
    set(across,10,27,'oak_fence',{waterlogged:'false'})
  }
  for(let forward=8;forward<=26;forward++)set(27,10,forward,'oak_fence',{waterlogged:'false'})
  box([7,9,7],[27,9,7],'mossy_stone_bricks')
  box([7,9,27],[27,9,27],'mossy_stone_bricks')
  box([27,9,8],[27,9,26],'mossy_stone_bricks')
  for(const across of [1,33])for(const forward of [3,30]) {
    box([across,10,forward],[across+1,17,forward+1],'dark_oak_log',{axis:'y'})
    box([across,17,forward],[across+1,19,forward+1],'dark_oak_log',{axis:'y'})
  }
  box([2,18,2],[32,19,3],'dark_oak_log',{axis:'x'})
  box([31,18,3],[32,19,32],'dark_oak_log',{axis:'z'})
  box([2,18,31],[32,19,32],'dark_oak_log',{axis:'x'})
  box([2,18,3],[3,19,31],'dark_oak_log',{axis:'z'})
  box([12,2,12],[17,2,18],'moss_block')
  box([13,3,13],[16,5,17],'dark_oak_log',{axis:'y'})
  box([13,6,13],[15,12,16],'dark_oak_log',{axis:'y'})
  box([12,10,13],[14,15,15],'dark_oak_log',{axis:'y'})
  box([11,14,12],[14,17,14],'dark_oak_log',{axis:'y'})
  box([9,15,12],[12,16,14],'dark_oak_log',{axis:'x'})
  box([14,12,15],[18,13,17],'dark_oak_log',{axis:'x'})
  box([17,13,16],[20,14,18],'dark_oak_log',{axis:'x'})
  box([11,2,13],[13,3,15],'dark_oak_log',{axis:'x'})
  box([15,2,17],[18,3,18],'dark_oak_log',{axis:'z'})
  box([16,2,12],[18,2,14],'dark_oak_log',{axis:'x'})
  const clumps=[[11,18,13,6,2,5],[16,17,16,5,2,4],[19,15,18,4,2,3]]
  for(const [centerX,centerY,centerZ,radiusX,radiusY,radiusZ] of clumps)for(let across=7;across<=25;across++)for(let vertical=13;vertical<=20;vertical++)for(let forward=8;forward<=23;forward++) {
    const radius=((across-centerX)/radiusX)**2+((vertical-centerY)/radiusY)**2+((forward-centerZ)/radiusZ)**2
    if(radius<1+((across+forward*3)%5)*.05&&String(structure.getBlock([across,vertical,forward]).state.getName())==='minecraft:air')set(across,vertical,forward,'oak_leaves',leaf)
  }
  for(let across=20;across<=29;across++)for(let forward=20;forward<=27;forward++) {
    const rim=across===20||across===29||forward===20||forward===27
    set(across,1,forward,rim?'mossy_stone_bricks':'water',rim?undefined:{level:'0'})
  }
  box([23,2,20],[25,6,22],'mossy_cobblestone')
  box([24,2,21],[24,5,21],'water',{level:'0'})
  box([24,2,22],[24,5,22],'glass')
  for(const [across,forward,top,length] of [[10,13,15,3],[17,16,13,3],[19,18,14,2],[6,5,17,3],[29,15,17,3],[15,30,17,3]]) {
    box([across,top+1,forward],[across,20,forward],'dark_oak_log',{axis:'y'})
    for(let drop=0;drop<length;drop++)set(across,top-drop,forward,drop===length-1?'cave_vines':'cave_vines_plant',drop===length-1?{berries:'true',age:'25'}:{berries:'true'})
  }
  for(const [across,forward] of [[6,6],[29,13],[15,29]]) {
    box([across,16,forward],[across,20,forward],'dark_oak_log',{axis:'y'})
    set(across,15,forward,'lantern',{hanging:'true',waterlogged:'false'})
  }
  for(const [across,forward] of [[12,11],[18,15],[19,19],[21,28],[29,25],[7,23],[26,18]])if(String(structure.getBlock([across,2,forward]).state.getName())==='minecraft:air')set(across,2,forward,'oak_leaves',leaf)
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
