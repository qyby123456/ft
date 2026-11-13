export default function build(api) {
  const structure = api.createStructure([65,22,65])
  const block = (east,up,south,name,nbt) => api.setBlock(structure,[east,up,south],`minecraft:${name}`,undefined,nbt)
  const box = (minimum,maximum,name) => api.fill(structure,minimum,maximum,`minecraft:${name}`)
  box([0,0,0],[64,21,64],'water[level=0]')
  box([0,0,0],[64,1,64],'dark_prismarine')
  for(let east=0;east<65;east++)for(let south=0;south<65;south++)block(east,2,south,(east*13+south*7)%29<5?'sandstone':'prismarine_bricks')
  const rooms = [
    {id:'great-hall',bounds:[30,2,62,32],height:14,target:[46,3,27]},
    {id:'western-sanctum',bounds:[2,41,29,62],height:11,target:[15,3,57]},
    {id:'library',bounds:[2,2,24,22],height:10,target:[12,3,13]},
    {id:'coral-courtyard',bounds:[2,25,24,32],height:7,target:[14,3,29]},
    {id:'treasure-cell',bounds:[45,43,62,61],height:9,target:[53,3,54]},
    {id:'north-turn-corridor',bounds:[25,2,29,40],height:8,target:[27,3,20]},
    {id:'cross-gallery',bounds:[2,33,64,40],height:8,target:[38,3,36]},
    {id:'entrance-gallery',bounds:[30,40,42,64],height:10,target:[36,3,55]},
    {id:'treasure-connector',bounds:[42,47,45,53],height:7,target:[43,3,50]}
  ]
  for(const room of rooms){
    const [west,north,east,south]=room.bounds
    box([west,3,north],[east,room.height,north],'prismarine_bricks')
    box([west,3,south],[east,room.height,south],'prismarine_bricks')
    box([west,3,north],[west,room.height,south],'prismarine_bricks')
    box([east,3,north],[east,room.height,south],'prismarine_bricks')
    for(let eastward=west+1;eastward<east;eastward++)for(let southward=north+1;southward<south;southward++){
      if(eastward===west+1||eastward===east-1||southward===north+1||southward===south-1)block(eastward,2,southward,'sandstone')
      else if((eastward+southward)%7===0)block(eastward,2,southward,'prismarine')
    }
    for(const eastward of [west,east])for(const southward of [north,south]){
      block(eastward,room.height+1,southward,'prismarine')
      block(eastward,6,southward,'sea_lantern')
    }
  }
  const archX=(east,center,top=8)=>{
    for(let across=-3;across<=3;across++)for(let up=3;up<=top;up++)block(east,up,center+across,Math.abs(across)<=2&&up<top-Math.max(0,Math.abs(across)-1)?'water[level=0]':'prismarine')
    for(const across of [-3,3])block(east,6,center+across,'sea_lantern')
  }
  const archZ=(south,center,top=8)=>{
    for(let across=-3;across<=3;across++)for(let up=3;up<=top;up++)block(center+across,up,south,Math.abs(across)<=2&&up<top-Math.max(0,Math.abs(across)-1)?'water[level=0]':'prismarine')
    for(const across of [-3,3])block(center+across,6,south,'sea_lantern')
  }
  for(const east of [24,25,29,30])archX(east,12)
  for(const east of [24,25,29,30])archX(east,28)
  for(const south of [32,33]){archZ(south,14);archZ(south,46)}
  for(const south of [33,40])archZ(south,27)
  for(const south of [40,41])archZ(south,15)
  archZ(40,36)
  for(const east of [42,43,44,45])archX(east,50)
  for(const south of [62,63,64])archZ(south,36,10)
  for(const east of [62,63,64])archX(east,36,8)
  box([25,3,34],[29,7,39],'water[level=0]')
  box([31,3,41],[41,9,61],'water[level=0]')
  for(let east=40;east<=52;east++){
    const peak=15+Math.max(0,3-Math.floor(Math.abs(east-46)/2))
    box([east,14,2],[east,peak,3],'prismarine_bricks')
  }
  box([41,5,3],[51,13,3],'dark_prismarine')
  for(const [east,up] of [[46,7],[46,9],[46,11],[44,9],[48,9]])block(east,up,4,'sea_lantern')
  for(const [east,south] of [[36,10],[56,10],[36,25],[56,25]]){
    box([east-1,3,south-1],[east+1,3,south+1],'dark_prismarine')
    box([east,4,south],[east,9,south],'prismarine_bricks')
    block(east,10,south,'sea_lantern')
  }
  box([42,3,15],[50,3,21],'dark_prismarine')
  box([43,4,16],[49,4,20],'prismarine')
  block(46,5,18,'sea_lantern')
  box([10,3,49],[20,3,55],'dark_prismarine')
  box([11,4,50],[19,4,54],'prismarine_bricks')
  for(const [east,south] of [[8,47],[22,47],[8,57],[22,57]]){
    box([east,3,south],[east,4,south],'prismarine_bricks');block(east,5,south,'sea_lantern')
  }
  for(let east=6;east<=19;east++)for(let up=3;up<=5;up++)block(east,up,3,'bookshelf')
  for(const east of [6,11,16,21]){
    block(east,3,6,'sandstone')
    block(east,4,6,'barrel[facing=south,open=false]',api.createBlockNbt({id:'minecraft:barrel',Items:[]}))
  }
  box([9,3,16],[17,3,18],'prismarine')
  block(13,4,17,'sea_lantern')
  box([49,3,45],[58,3,48],'dark_prismarine')
  const chests=[[53,4,46],[58,3,57]]
  for(const [east,up,south]of chests)block(east,up,south,'chest[facing=south,type=single,waterlogged=true]',api.createBlockNbt({id:'minecraft:chest',Items:[]}))
  for(const [east,south]of [[7,28],[19,28]]){block(east,3,south,'prismarine_bricks');block(east,4,south,'sea_lantern')}
  const clusters=[[4,3,5],[22,3,19],[4,3,30],[21,3,26],[32,3,5],[60,3,6],[60,3,29],[32,3,30],[6,3,43],[26,3,59],[4,3,60],[60,3,45],[47,3,59],[40,3,58],[4,3,37],[60,3,38],[49,5,17],[11,5,51]]
  for(let index=0;index<clusters.length;index++){
    const [east,up,south]=clusters[index]
    block(east,up,south,`${['tube','bubble','fire','horn','brain'][index%5]}_coral_block`)
    block(east,up+1,south,`${['tube','bubble','fire','horn','brain'][index%5]}_coral[waterlogged=true]`)
  }
  for(const [east,south]of [[5,9],[21,17],[33,7],[59,26],[4,45],[27,58],[48,58],[60,52],[33,56],[40,44],[6,26],[20,30]]){
    block(east,3,south,'kelp_plant');block(east,4,south,'kelp_plant');block(east,5,south,'kelp[age=25]')
  }
  for(const [east,south]of [[8,11],[18,20],[38,6],[53,28],[6,53],[24,44],[49,55],[60,58],[33,60]])block(east,3,south,'sea_pickle[pickles=2,waterlogged=true]')
  const targets=[
    [36,3,64],[36,3,60],[36,3,50],[36,3,43],[36,3,36],
    [15,3,39],[15,3,43],[15,3,57],[24,3,51],
    [27,3,36],[27,3,28],[14,3,29],[27,3,20],[27,3,12],[12,3,13],[11,3,7],
    [33,3,12],[46,3,12],[46,3,27],[46,3,35],[60,3,36],[64,3,36],
    [43,3,50],[48,3,50],[53,4,49],[53,4,48],[58,3,58]
  ]
  return {structure,metadata:{name:'s5-10-coral-ocean-complex',rooms,targets,chests,ports:[{position:[36,3,64],outward:'south',width:5,height:3},{position:[64,3,36],outward:'east',width:5,height:3}],water_contract:{horizontal_ring:8,top_cover:8,solid_bed_layers:3,all_ports_source_water:true}}}
}
