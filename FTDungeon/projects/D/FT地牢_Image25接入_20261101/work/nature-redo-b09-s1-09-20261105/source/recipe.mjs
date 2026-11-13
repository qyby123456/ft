export default function build(api) {
  const structure=api.createStructure([29,17,29])
  const set=(across,vertical,forward,block,properties,nbt)=>api.setBlock(structure,[across,vertical,forward],block,properties,nbt)
  const box=(start,end,block,properties)=>api.fill(structure,start,end,block,properties)
  const leaf={persistent:'true',distance:'7',waterlogged:'false'}
  const stone=(across,vertical,forward)=>['stone_bricks','mossy_stone_bricks','mossy_stone_bricks','cracked_stone_bricks','stone_bricks'][(across*11+vertical*7+forward*13)%5]
  box([0,0,0],[28,16,28],'air')
  box([0,0,0],[28,0,28],'stone_bricks')
  for(let across=0;across<29;across++)for(let forward=0;forward<29;forward++) {
    set(across,1,forward,(across*7+forward*11)%13<3?'moss_block':stone(across,1,forward))
    set(across,16,forward,'mossy_stone_bricks')
    if(across===0||across===28||forward===0||forward===28)for(let vertical=2;vertical<16;vertical++)set(across,vertical,forward,stone(across,vertical,forward))
  }
  for(const across of [0,28]) {
    box([across,2,7],[across,6,9],'air')
    box([across,2,6],[across,7,6],'chiseled_stone_bricks')
    box([across,2,10],[across,7,10],'chiseled_stone_bricks')
    box([across,7,7],[across,7,9],'stone_bricks')
  }
  box([13,2,28],[15,6,28],'air')
  for(const across of [2,26])for(const forward of [2,13,26]) {
    box([across,2,forward],[across,11,forward],'mossy_stone_bricks')
    box([across-1,11,forward],[across+1,12,forward],'chiseled_stone_bricks')
  }
  box([9,2,4],[19,4,11],'stone_bricks')
  box([9,4,4],[19,4,11],'mossy_stone_bricks')
  for(let forward=12;forward<=17;forward++) {
    const feet=2+(18-forward)*.5
    box([12,2,forward],[16,Math.floor(feet)-1,forward],'stone_bricks')
    if(feet%1)box([12,Math.floor(feet),forward],[16,Math.floor(feet),forward],'stone_brick_slab',{type:'bottom',waterlogged:'false'})
  }
  box([13,1,18],[15,1,26],'red_terracotta')
  box([14,4,5],[14,4,11],'red_terracotta')
  set(14,5,10,'chest',{facing:'south',type:'single',waterlogged:'false'},api.createBlockNbt({id:'minecraft:chest'}))
  for(const across of [7,21]) {
    box([across-1,2,5],[across+1,2,9],'mossy_stone_bricks')
    box([across-1,3,6],[across-1,4,8],'stone_bricks')
    box([across+1,3,6],[across+1,4,8],'stone_bricks')
    box([across-1,5,6],[across+1,6,8],'chiseled_stone_bricks')
    box([across,7,6],[across,8,7],'mossy_stone_bricks')
    box([across-2,5,7],[across-2,6,9],'stone_bricks')
    box([across+2,5,7],[across+2,6,9],'stone_bricks')
    set(across,7,8,'carved_pumpkin',{facing:'south'})
    set(across,9,7,'moss_block')
  }
  for(let across=10;across<=18;across++)for(let vertical=5;vertical<=12;vertical++) {
    const radius=Math.hypot(across-14,vertical-8.5)
    if(radius>2.7&&radius<4)set(across,vertical,1,'chiseled_stone_bricks')
  }
  for(const across of [10,18]) {
    box([across,7,2],[across,11,2],'red_terracotta')
    set(across,6,2,'gold_block')
    set(across,11,3,'red_wall_banner',{facing:'south'},api.createBlockNbt({id:'minecraft:banner'}))
    box([across-1,12,2],[across+1,12,2],'dark_oak_log',{axis:'x'})
  }
  for(let across=6;across<=10;across++)for(let forward=22;forward<=25;forward++) {
    const rim=across===6||across===10||forward===22||forward===25
    set(across,1,forward,rim?'mossy_stone_bricks':'water',rim?undefined:{level:'0'})
  }
  for(const [across,forward] of [[2,3],[26,3],[2,25],[26,25]]) {
    box([across,2,forward],[across,14,forward],'dark_oak_log',{axis:'y'})
    box([across,11,forward],[across+1,12,forward],'dark_oak_log',{axis:'x'})
  }
  box([2,13,2],[26,14,3],'dark_oak_log',{axis:'x'})
  box([2,13,2],[3,14,26],'dark_oak_log',{axis:'z'})
  box([25,13,2],[26,14,26],'dark_oak_log',{axis:'z'})
  box([2,13,25],[26,14,26],'dark_oak_log',{axis:'x'})
  for(let across=3;across<=25;across++)for(let forward=2;forward<=26;forward++) {
    if((forward<6||across<5||across>23||forward>24)&&(across*3+forward*7)%5!==0)set(across,15,forward,'oak_leaves',leaf)
  }
  for(const [across,forward,length] of [[5,4,3],[23,4,4],[4,15,3],[24,18,4],[12,25,3],[19,3,2]]) {
    set(across,14,forward,'dark_oak_log',{axis:'y'})
    for(let drop=0;drop<length;drop++)set(across,13-drop,forward,drop===length-1?'cave_vines':'cave_vines_plant',drop===length-1?{berries:'true',age:'25'}:{berries:'true'})
  }
  for(const [across,forward] of [[10,14],[18,14],[14,23]]) {
    box([across,12,forward],[across,15,forward],'dark_oak_log',{axis:'y'})
    set(across,11,forward,'lantern',{hanging:'true',waterlogged:'false'})
  }
  for(const [across,forward] of [[2,18],[26,20]]) {
    set(across,2,forward,'terracotta')
    set(across,3,forward,'potted_red_mushroom')
  }
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
