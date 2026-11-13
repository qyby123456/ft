export const size=[89,25,89]
export const areas=[
  {id:'great-hall',name:'北部冰晶四柱大厅',bounds:[29,3,65,35],ceiling:23,role:'hall'},
  {id:'west-pool',name:'西侧冻结大池室',bounds:[3,18,27,49],ceiling:18,role:'pool'},
  {id:'treasury',name:'东北小宝库',bounds:[67,8,85,31],ceiling:16,role:'treasure'},
  {id:'bone-room',name:'西南骨骸小室',bounds:[3,57,29,81],ceiling:16,role:'crypt'},
  {id:'ice-court',name:'东南四柱冰庭',bounds:[57,48,85,79],ceiling:null,role:'courtyard'},
  {id:'entrance',name:'南侧门庭',bounds:[34,64,52,81],ceiling:16,role:'entrance'},
  {id:'spine',name:'中央纵向连廊',bounds:[39,32,47,68],ceiling:14,role:'corridor'},
  {id:'west-link',name:'西向横廊',bounds:[24,43,43,51],ceiling:14,role:'corridor'},
  {id:'west-turn',name:'西南转角廊',bounds:[21,47,29,61],ceiling:14,role:'corridor'},
  {id:'east-link',name:'东向横廊',bounds:[43,39,61,47],ceiling:14,role:'corridor'},
  {id:'east-turn',name:'东南转角廊',bounds:[53,43,61,53],ceiling:14,role:'corridor'},
  {id:'treasury-link',name:'宝库短廊',bounds:[61,21,71,29],ceiling:14,role:'corridor'},
  {id:'approach',name:'外侧雪阶',bounds:[39,78,47,88],ceiling:null,role:'approach'},
]
export const contains=(area,east,south,margin=0)=>east>=area.bounds[0]+margin&&east<=area.bounds[2]-margin&&south>=area.bounds[1]+margin&&south<=area.bounds[3]-margin
export const cellAreas=(east,south)=>areas.filter(area=>contains(area,east,south))
export const plannedRoof=(east,south)=>cellAreas(east,south).some(area=>area.ceiling!==null)

export default function build(api){
  const structure=api.createStructure(size)
  const set=(east,height,south,block)=>api.setBlock(structure,[east,height,south],`minecraft:${block}`)
  const box=(from,to,block)=>api.fill(structure,from,to,`minecraft:${block}`)
  const noise=(east,height,south)=>(east*71+height*43+south*137+east*south*17)%101
  box([0,0,0],[88,24,88],'air')
  box([0,0,0],[88,0,88],'stone_bricks')
  for(let east=0;east<89;east++)for(let south=0;south<89;south++){
    const touching=cellAreas(east,south)
    if(!touching.length)continue
    const interiors=touching.filter(area=>contains(area,east,south,2))
    const exposed=touching.some(area=>area.ceiling===null)
    const roof=touching.filter(area=>area.ceiling!==null)
    const ceiling=roof.length?Math.max(...roof.map(area=>area.ceiling)):null
    box([east,1,south],[east,4,south],'stone_bricks')
    const patch=Math.sin(east*0.48+south*0.2)+Math.cos(south*0.41-east*0.19)
    set(east,4,south,patch< -0.6?'snow_block':patch>0.8?'packed_ice':'stone_bricks')
    if(!interiors.length){
      const wallTop=ceiling!==null?ceiling+1:10
      for(let height=5;height<=wallTop;height++)set(east,height,south,noise(east,height,south)<20?'cobblestone':noise(east,height,south)<36?'cracked_stone_bricks':'stone_bricks')
      if(wallTop<24)set(east,wallTop+1,south,'snow_block')
    }
    if(ceiling!==null)box([east,ceiling,south],[east,Math.min(24,ceiling+1),south],'stone_bricks')
  }
  const lamp=(east,height,south)=>{
    set(east,height+2,south,'spruce_log[axis=x]')
    set(east,height+1,south,'chain[axis=y,waterlogged=false]')
    set(east,height,south,'lantern[hanging=true,waterlogged=false]')
  }
  const iceCluster=(east,south,top)=>{
    box([east-1,5,south-1],[east+1,6,south+1],'packed_ice')
    box([east,7,south],[east,top,south],'blue_ice')
  }
  for(const [east,south]of [[37,12],[57,12],[37,27],[57,27]]){
    box([east-1,5,south-1],[east+1,6,south+1],'stone_bricks')
    box([east,7,south],[east,10,south],'chiseled_stone_bricks')
    box([east-1,11,south-1],[east+1,11,south+1],'stone_bricks')
  }
  box([44,5,17],[50,6,23],'packed_ice')
  iceCluster(47,20,14)
  iceCluster(43,21,9)
  iceCluster(51,19,10)
  box([44,8,5],[50,18,5],'packed_ice')
  box([47,8,5],[47,21,5],'blue_ice')
  for(const east of[32,62])for(const south of[9,19,29]){
    box([east,5,south],[east,20,south],'spruce_log[axis=y]')
    const inner=east===32?33:61
    box([Math.min(east,inner),20,south],[Math.max(east,inner),20,south],'spruce_log[axis=x]')
    lamp(inner,18,south)
  }
  box([9,4,25],[21,4,40],'air')
  box([9,3,25],[21,3,40],'blue_ice')
  for(let east=11;east<=19;east++)set(east,4,41,'stone_brick_stairs[facing=south,half=bottom,shape=straight,waterlogged=false]')
  for(const[east,south]of[[8,23],[22,24],[8,42],[22,42]])iceCluster(east,south,8)
  for(const east of[6,24])for(const south of[23,36,45]){
    box([east,5,south],[east,15,south],'spruce_log[axis=y]')
    lamp(east===6?7:23,13,south)
  }
  box([72,5,12],[80,5,18],'stone_bricks')
  box([74,6,13],[78,6,17],'polished_andesite')
  box([75,5,19],[77,5,19],'stone_brick_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]')
  box([75,6,18],[77,6,18],'stone_brick_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]')
  api.setBlock(structure,[76,7,15],'minecraft:chest[facing=south,type=single,waterlogged=false]',undefined,api.createBlockNbt({id:'minecraft:chest',Items:[]}))
  for(const east of[70,82])for(const south of[12,25]){
    box([east,5,south],[east,13,south],'spruce_log[axis=y]')
    lamp(east===70?71:81,11,south)
  }
  for(const [east,south]of[[8,64],[17,62],[23,73]]){
    box([east,5,south],[east+2,5,south+1],'spruce_planks')
    box([east,6,south],[east+2,6,south],'spruce_slab[type=bottom,waterlogged=false]')
  }
  for(const south of[65,68,71]){
    box([12,5,south],[16,5,south],'bone_block[axis=x]')
    box([12,6,south],[12,7,south],'bone_block[axis=y]')
    box([16,6,south],[16,7,south],'bone_block[axis=y]')
    box([13,7,south],[15,7,south],'bone_block[axis=x]')
  }
  for(const east of[6,26])for(const south of[61,70,77]){
    box([east,5,south],[east,13,south],'spruce_log[axis=y]')
    lamp(east===6?7:25,11,south)
  }
  box([66,4,59],[77,4,69],'air')
  box([66,3,59],[77,3,69],'blue_ice')
  for(let east=68;east<=75;east++)set(east,4,70,'stone_brick_stairs[facing=south,half=bottom,shape=straight,waterlogged=false]')
  for(const[east,south]of[[63,55],[80,55],[63,73],[80,73]]){
    box([east-1,5,south-1],[east+1,6,south+1],'stone_bricks')
    box([east,7,south],[east,9,south],'chiseled_stone_bricks')
    set(east,10,south,'snow_block')
  }
  for(const[east,south]of[[61,60],[81,63],[68,74]])iceCluster(east,south,8)
  for(const east of[60,82])for(const south of[52,66,76]){
    box([east,5,south],[east,9,south],'spruce_log[axis=y]')
    lamp(east===60?61:81,8,south)
  }
  for(const south of[69,77])for(const east of[37,49]){
    box([east,5,south],[east,13,south],'spruce_log[axis=y]')
    lamp(east===37?38:48,11,south)
  }
  for(const east of[39,47]){
    box([east,5,81],[east,11,81],'spruce_log[axis=y]')
    box([east-1,5,81],[east+1,6,81],'stone_bricks')
  }
  box([39,12,81],[47,13,81],'spruce_log[axis=x]')
  box([39,14,81],[47,14,81],'snow_block')
  lamp(40,10,81)
  lamp(46,10,81)
  for(const[east,south]of[[41,38],[45,52],[26,48],[25,55],[55,43],[57,49],[66,25]]){
    const covering=cellAreas(east,south).filter(area=>area.ceiling!==null)
    const top=Math.max(...covering.map(area=>area.ceiling))
    box([east,top-1,south],[east,top,south],'spruce_log[axis=y]')
    set(east,top-2,south,'lantern[hanging=true,waterlogged=false]')
  }
  for(const area of areas.filter(area=>area.role!=='approach')){
    const [west,north,east,south]=area.bounds
    if(area.ceiling!==null)for(const position of[[west+3,north+3],[east-3,south-3]]){
      const [column,depth]=position
      const attachment=Math.max(...cellAreas(column,depth).filter(region=>region.ceiling!==null).map(region=>region.ceiling))
      box([column,area.ceiling-2,depth],[column,attachment,depth],'packed_ice')
      set(column,area.ceiling-3,depth,'blue_ice')
    }
  }
  box([41,1,85],[45,4,88],'air')
  box([41,1,88],[45,1,88],'stone_bricks')
  for(const south of[85,86,87]){
    const height=89-south
    box([41,1,south],[45,height-1,south],'stone_bricks')
    box([41,height,south],[45,height,south],'stone_brick_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]')
  }
  box([41,5,79],[45,9,88],'air')
  box([41,2,88],[45,5,88],'air')
  return{structure,metadata:{name:'room'}}
}
