export default function build(api) {
  const structure=api.createStructure([33,24,35])
  const put=(east,up,south,block,properties,entity)=>api.setBlock(structure,[east,up,south],`minecraft:${block}`,block==='sandstone_wall'?{up:'true',north:south===28?'none':'low',south:south===28?'none':'low',east:south===28?'low':'none',west:south===28?'low':'none',waterlogged:'false',...properties}:properties,entity)
  const fill=(first,last,block,properties)=>api.fill(structure,first,last,`minecraft:${block}`,properties)
  fill([0,0,0],[32,23,34],'air')
  fill([0,0,4],[32,2,34],'sandstone')
  for(let east=0;east<=32;east++)for(let south=4;south<=34;south++)put(east,2,south,((east===10||east===24)&&south>=8&&south<=29)||(south===9||south===27)&&east>=7&&east<=27?'orange_terracotta':(east+south*3)%17===0?'cut_sandstone':'smooth_sandstone')
  fill([11,0,0],[21,0,3],'cut_sandstone')
  for(let east=13;east<=19;east++) {
    put(east,1,1,'smooth_sandstone_stairs',{facing:'south',half:'bottom',shape:'straight',waterlogged:'false'})
    put(east,2,2,'smooth_sandstone_stairs',{facing:'south',half:'bottom',shape:'straight',waterlogged:'false'})
  }
  fill([13,1,2],[19,1,3],'sandstone')
  fill([13,2,3],[19,2,3],'smooth_sandstone')
  for(let up=3;up<=19;up++) {
    const material=up===7||up===15?'orange_terracotta':up===8||up===17?'chiseled_sandstone':'sandstone'
    fill([0,up,4],[1,up,34],material)
    fill([31,up,4],[32,up,34],material)
    fill([0,up,33],[32,up,34],material)
  }
  fill([0,3,4],[12,4,4],'sandstone')
  fill([20,3,4],[32,4,4],'sandstone')
  for(const east of [3,28])for(const south of [6,15,25,31]) {
    fill([east-1,3,south-1],[east+1,3,south+1],'cut_sandstone')
    for(let up=4;up<=18;up++)put(east,up,south,up===7||up===15?'chiseled_red_sandstone':up===6||up===14?'orange_terracotta':'cut_sandstone')
    fill([east-1,9,south-1],[east+1,10,south+1],'chiseled_sandstone')
    fill([east-1,18,south-1],[east+1,19,south+1],'chiseled_sandstone')
  }
  fill([2,10,5],[5,10,32],'smooth_sandstone')
  fill([27,10,5],[30,10,32],'smooth_sandstone')
  fill([2,10,29],[30,10,32],'smooth_sandstone')
  fill([6,10,5],[6,10,28],'cut_sandstone')
  fill([26,10,5],[26,10,28],'cut_sandstone')
  fill([6,10,28],[26,10,28],'cut_sandstone')
  fill([0,19,4],[5,19,34],'cut_sandstone')
  fill([27,19,4],[32,19,34],'cut_sandstone')
  fill([2,19,29],[30,19,34],'cut_sandstone')
  for(const east of [3,28])for(const [start,end] of [[6,15],[15,25],[25,31]])for(let offset=1;offset<end-start;offset++) {
    const edge=Math.min(offset,end-start-offset)
    if(edge<=2)fill([east,edge===1?8:9,start+offset],[east,9,start+offset],'cut_sandstone')
  }
  for(const east of [0,32])for(let south=4;south<=34;south++)put(east,20,south,'sandstone_wall')
  for(let east=1;east<=31;east++)put(east,20,34,'sandstone_wall',{north:'none',south:'none',east:'low',west:'low'})
  for(let south=5;south<=28;south++)for(const east of [6,26])put(east,11,south,'sandstone_wall')
  for(let east=6;east<=26;east++)put(east,11,28,'sandstone_wall')
  for(let step=0;step<8;step++)for(let east=3;east<=5;east++) {
    fill([east,3,6+step],[east,3+step,6+step],'sandstone')
    put(east,3+step,6+step,'smooth_sandstone_stairs',{facing:'south',half:'bottom',shape:'straight',waterlogged:'false'})
    if(step<7)put(east,10,6+step,'air')
    for(let up=4+step;up<=12;up++)put(east,up,6+step,'air')
  }
  for(let south=6;south<=13;south++) {
    const step=south-6
    fill([6,3,south],[6,3+step,south],'sandstone')
    put(2,4+step,south,'sandstone_wall')
    put(6,4+step,south,'sandstone_wall')
  }
  for(const side of ['south','west','east'])for(let inset=0;inset<=1;inset++)for(let offset=-3;offset<=3;offset++)for(let up=3;up<=9;up++) {
    const east=side==='west'?inset:side==='east'?32-inset:16+offset
    const south=side==='south'?34-inset:20+offset
    const radius=up<=6?2:up===7?1:up===8?0:-1
    put(east,up,south,Math.abs(offset)<=radius?'air':up===7?'orange_terracotta':'smooth_sandstone')
  }
  fill([12,2,12],[22,2,22],'cut_sandstone')
  for(let east=12;east<=22;east++)for(let south=12;south<=22;south++)put(east,3,south,east===12||east===22||south===12||south===22?'chiseled_sandstone':'water',east===12||east===22||south===12||south===22?undefined:{level:'0'})
  fill([15,3,14],[19,5,16],'cut_sandstone')
  fill([16,6,15],[18,8,16],'cut_sandstone')
  fill([15,9,14],[19,9,17],'chiseled_sandstone')
  put(17,8,16,'water',{level:'0'})
  put(17,8,17,'water',{level:'1'})
  for(let up=4;up<=7;up++)put(17,up,17,'water',{level:'8'})
  for(const [east,south]of [[14,20],[20,20],[14,18],[20,14]])put(east,4,south,'lily_pad')
  fill([7,2,22],[10,2,25],'dirt')
  fill([8,3,23],[9,15,24],'oak_log',{axis:'y'})
  for(let east=5;east<=13;east++)for(let south=19;south<=28;south++)for(let up=14;up<=21;up++) {
    const distance=((east-9)/5)**2+((south-24)/5.5)**2+((up-17)/4.5)**2
    if(distance<1.05&&(east*7+south*3+up)%13!==0)put(east,up,south,'oak_leaves',{distance:'1',persistent:'true',waterlogged:'false'})
  }
  fill([9,13,24],[12,13,24],'oak_log',{axis:'x'})
  fill([7,12,24],[8,12,24],'oak_log',{axis:'x'})
  for(const [east,south]of [[7,23],[10,25],[8,22],[9,25]])put(east,3,south,'oak_log',{axis:'y'})
  for(const [east,south]of [[8,8],[25,9],[8,28],[25,27]]) {
    put(east,3,south,'terracotta')
    put(east,4,south,'flower_pot')
  }
  for(const [east,south]of [[8,6],[24,6],[24,29],[7,29]]) {
    put(east,2,south,'sand')
    put(east,3,south,'dead_bush')
  }
  for(const [east,south,facing]of [[2,14,'east'],[30,14,'west'],[2,27,'east'],[30,27,'west']])for(let up=12;up<=16;up++)put(east,up,south,'vine',{east:facing==='west'?'true':'false',west:facing==='east'?'true':'false',north:'false',south:'false',up:'false'})
  for(const [east,south]of [[4,18],[28,18],[16,31]]) {
    put(east,16,south,'lantern',{hanging:'true',waterlogged:'false'})
    for(const up of [17,18])put(east,up,south,'chain',{axis:'y',waterlogged:'false'})
  }
  return {structure,metadata:{name:'room'}}
}
