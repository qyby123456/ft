export default function build(api) {
  const structure=api.createStructure([29,18,29])
  const put=(east,up,south,block,properties,entity)=>api.setBlock(structure,[east,up,south],`minecraft:${block}`,properties,entity)
  const fill=(first,last,block,properties)=>api.fill(structure,first,last,`minecraft:${block}`,properties)
  fill([0,0,0],[28,17,28],'air')
  fill([0,0,3],[28,2,28],'sandstone')
  for(let east=0;east<29;east++)for(let south=3;south<29;south++)put(east,2,south,(east+south*3)%19===0?'cut_sandstone':'smooth_sandstone')
  fill([10,0,0],[18,0,2],'cut_sandstone')
  fill([11,1,2],[17,1,2],'sandstone')
  for(let east=11;east<=17;east++) {
    put(east,1,1,'smooth_sandstone_stairs',{facing:'south',half:'bottom',shape:'straight',waterlogged:'false'})
    put(east,2,2,'smooth_sandstone_stairs',{facing:'south',half:'bottom',shape:'straight',waterlogged:'false'})
  }
  for(let up=3;up<=14;up++) {
    const material=up===7||up===12?'orange_terracotta':up===8||up===13?'chiseled_sandstone':'sandstone'
    fill([0,up,3],[1,up,28],material)
    fill([27,up,3],[28,up,28],material)
    fill([0,up,27],[28,up,28],material)
  }
  for(const east of [3,25])for(const south of [5,16,25]) {
    fill([east-1,3,south-1],[east+1,3,south+1],'cut_sandstone')
    fill([east-1,4,south-1],[east+1,4,south+1],'smooth_sandstone')
    for(let up=5;up<=13;up++)put(east,up,south,up===6||up===11?'orange_terracotta':up===7||up===12?'chiseled_red_sandstone':'cut_sandstone')
    fill([east-1,13,south-1],[east+1,14,south+1],'chiseled_sandstone')
    fill([east-2,15,south-2],[east+2,15,south+2],'cut_sandstone')
  }
  fill([0,14,3],[5,14,28],'cut_sandstone')
  fill([23,14,3],[28,14,28],'cut_sandstone')
  fill([6,14,24],[22,14,28],'cut_sandstone')
  fill([6,14,3],[22,14,5],'cut_sandstone')
  for(const side of ['west','east'])for(let inset=0;inset<=1;inset++)for(let offset=-3;offset<=3;offset++)for(let up=3;up<=10;up++) {
    const radius=up<=6?2:up<=8?1:up===9?0:-1
    put(side==='west'?inset:28-inset,up,12+offset,Math.abs(offset)<=radius?'air':up===8?'orange_terracotta':'smooth_sandstone')
  }
  for(const east of [3,25])for(let offset=-3;offset<=3;offset++)for(let up=7;up<=12;up++)if(up>=10||Math.abs(offset)>=3||up===9&&Math.abs(offset)>=2)put(east,up,12+offset,up===10?'chiseled_sandstone':'cut_sandstone')
  for(let east=8;east<=20;east++)for(let south=9;south<=16;south++) {
    const edge=east===8||east===20||south===9||south===16
    const diamond=Math.abs(east-14)+Math.abs(south-12.5)
    put(east,2,south,edge?'chiseled_red_sandstone':diamond<=1.5||diamond>=3.5&&diamond<=4.5?'yellow_terracotta':'orange_terracotta')
  }
  fill([10,5,26],[18,13,26],'cut_sandstone')
  for(let east=10;east<=18;east++)for(let up=5;up<=13;up++) {
    const distance=Math.abs(east-14)+Math.abs(up-9)
    put(east,up,26,distance===0||distance===3?'yellow_terracotta':distance<=4?'orange_terracotta':'smooth_sandstone')
  }
  for(const east of [7,21])for(let up=7;up<=12;up++)put(east,up,26,up===9?'yellow_terracotta':'orange_terracotta')
  fill([11,3,19],[17,3,24],'cut_sandstone')
  fill([12,4,21],[16,4,24],'chiseled_sandstone')
  for(let east=12;east<=16;east++) {
    put(east,3,18,'smooth_sandstone_stairs',{facing:'south',half:'bottom',shape:'straight',waterlogged:'false'})
    put(east,4,20,'smooth_sandstone_stairs',{facing:'south',half:'bottom',shape:'straight',waterlogged:'false'})
  }
  for(const east of [9,19]) {
    fill([east-1,3,23],[east+1,4,25],'cut_sandstone')
    fill([east,5,24],[east,8,24],'chiseled_sandstone')
    fill([east-1,7,24],[east+1,8,24],'cut_sandstone')
    put(east,9,24,'chiseled_sandstone')
    put(east,10,24,'smooth_sandstone_slab',{type:'bottom',waterlogged:'false'})
    put(east,6,23,'orange_terracotta')
  }
  const chest=(east,up,south,facing,title,items)=>put(east,up,south,'chest',{facing,type:'single',waterlogged:'false'},api.createBlockNbt({id:'minecraft:chest',CustomName:JSON.stringify({text:title}),Items:items.map(([slot,id,count])=>({Slot:new api.NbtByte(slot),id:`minecraft:${id}`,Count:new api.NbtByte(count)}))}))
  chest(14,5,22,'north','砂日宝匣',[[0,'gold_ingot',6],[4,'emerald',3],[13,'diamond',1],[21,'bread',4]])
  for(const [east,facing]of [[4,'east'],[24,'west']]) {
    fill([east,2,19],[east,2,21],'oak_planks')
    chest(east,3,20,facing,'侧龛藏物',[[0,'gold_nugget',12],[13,'iron_ingot',4],[21,'torch',8]])
  }
  for(const east of [10,18]) {
    fill([east,3,20],[east,4,20],'chiseled_sandstone')
    put(east,5,20,'lantern',{hanging:'false',waterlogged:'false'})
  }
  for(const [east,south]of [[4,9],[24,9],[4,23],[24,23],[10,25],[18,25]]) {
    put(east,10,south,'lantern',{hanging:'true',waterlogged:'false'})
    for(let up=11;up<=13;up++)put(east,up,south,'chain',{axis:'y',waterlogged:'false'})
  }
  for(const [east,south]of [[6,22],[22,22],[6,24],[22,24],[6,7],[22,7]]) {
    put(east,3,south,'terracotta')
    put(east,4,south,'flower_pot')
  }
  for(const [east,south]of [[2,6],[26,6],[2,26],[26,26],[8,5],[20,5]]) {
    put(east,2,south,'sand')
    put(east,3,south,'dead_bush')
  }
  for(const [east,south]of [[8,7],[21,8],[9,17],[20,17]])put(east,3,south,'smooth_sandstone_slab',{type:'bottom',waterlogged:'false'})
  fill([0,3,3],[9,4,3],'cut_sandstone')
  fill([19,3,3],[28,4,3],'cut_sandstone')
  for(const [east,south]of [[1,7],[27,7],[1,20],[27,20],[8,27],[20,27]])put(east,15,south,'sandstone')
  return {structure,metadata:{name:'room'}}
}
