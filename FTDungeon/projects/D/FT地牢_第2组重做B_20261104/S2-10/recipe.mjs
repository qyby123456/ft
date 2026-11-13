export default function build(api) {
  const structure=api.createStructure([65,18,67])
  const put=(east,up,south,block,properties,entity)=>api.setBlock(structure,[east,up,south],`minecraft:${block}`,properties,entity)
  const fill=(first,last,block,properties)=>api.fill(structure,first,last,`minecraft:${block}`,properties)
  fill([0,0,0],[64,17,66],'air')
  const floor=(left,front,right,back)=>{
    fill([left,0,front],[right,2,back],'sandstone')
    for(let east=left;east<=right;east++)for(let south=front;south<=back;south++)put(east,2,south,(east*3+south)%23===0?'cut_sandstone':'smooth_sandstone')
  }
  const room=(left,front,right,back,top,style)=>{
    floor(left,front,right,back)
    for(let up=3;up<=top;up++) {
      const material=up===7||up===top-2?'orange_terracotta':up===8||up===top-1?'chiseled_sandstone':'sandstone'
      fill([left,up,front],[right,up,front],material)
      fill([left,up,back],[right,up,back],material)
      fill([left,up,front],[left,up,back],material)
      fill([right,up,front],[right,up,back],material)
    }
    if(style==='closed')fill([left,top,front],[right,top,back],'cut_sandstone')
    else {
      fill([left,top,front],[right,top,front+1],'cut_sandstone')
      fill([left,top,back-1],[right,top,back],'cut_sandstone')
      fill([left,top,front],[left+1,top,back],'cut_sandstone')
      fill([right-1,top,front],[right,top,back],'cut_sandstone')
    }
  }
  room(24,3,42,20,13,'rim')
  room(2,3,21,22,12,'rim')
  room(2,27,21,51,14,'rim')
  room(24,24,44,45,12,'rim')
  room(23,48,46,64,15,'rim')
  room(54,3,62,24,12,'rim')
  room(54,48,62,64,12,'closed')
  room(47,3,52,64,11,'closed')
  const tunnel=(left,front,right,back,axis)=>{
    floor(left,front,right,back)
    fill([left,3,front],[right,7,back],'air')
    if(axis==='x') {
      fill([left,3,front],[right,7,front],'cut_sandstone')
      fill([left,3,back],[right,7,back],'cut_sandstone')
    } else {
      fill([left,3,front],[left,7,back],'cut_sandstone')
      fill([right,3,front],[right,7,back],'cut_sandstone')
    }
    fill([left,8,front],[right,8,back],'cut_sandstone')
  }
  tunnel(21,9,24,15,'x')
  tunnel(42,9,47,15,'x')
  tunnel(29,20,37,24,'z')
  tunnel(21,32,24,38,'x')
  tunnel(44,32,47,38,'x')
  tunnel(29,45,37,48,'z')
  tunnel(9,22,15,27,'z')
  tunnel(21,48,23,52,'x')
  tunnel(52,11,54,17,'x')
  tunnel(52,52,54,58,'x')
  for(const [east,south] of [[6,7],[17,7],[6,18],[17,18],[6,31],[17,31],[6,47],[17,47],[27,52],[42,52],[27,60],[42,60]]) {
    const top=south>=52?14:south>=31?13:11
    fill([east-1,3,south-1],[east+1,3,south+1],'cut_sandstone')
    for(let up=4;up<top;up++)put(east,up,south,up===6||up===10?'chiseled_red_sandstone':'cut_sandstone')
    fill([east-1,top,south-1],[east+1,top,south+1],'chiseled_sandstone')
    if(south>=52)fill([east,15,south],[east,15,63],'cut_sandstone')
    else if(south>=31)fill([2,14,south],[7,14,south],'cut_sandstone')
    else fill([2,12,south],[7,12,south],'cut_sandstone')
  }
  for(const [east,south,height]of [[28,28,5],[40,28,6],[28,41,4],[40,41,7]]) {
    fill([east-1,3,south-1],[east+1,3,south+1],'cut_sandstone')
    fill([east,4,south],[east,height,south],'chiseled_sandstone')
  }
  const mosaic=(left,front,right,back)=>{
    const centerEast=(left+right)/2,centerSouth=(front+back)/2
    for(let east=left;east<=right;east++)for(let south=front;south<=back;south++) {
      const distance=Math.abs(east-centerEast)+Math.abs(south-centerSouth)
      put(east,2,south,east===left||east===right||south===front||south===back?'chiseled_red_sandstone':Math.floor(distance)%4<=1?'orange_terracotta':'yellow_terracotta')
    }
  }
  mosaic(5,32,18,45)
  mosaic(27,51,42,58)
  mosaic(5,9,18,16)
  mosaic(55,7,61,17)
  mosaic(27,7,39,16)
  for(let south=4;south<=63;south++)for(const east of [49,50])put(east,2,south,south%4===0?'chiseled_red_sandstone':'orange_terracotta')
  for(let east=30;east<=36;east++)for(let up=6;up<=13;up++)put(east,up,63,Math.abs(east-33)+Math.abs(up-10)<=3?'orange_terracotta':'chiseled_sandstone')
  for(let south=33;south<=44;south++)for(let up=5;up<=11;up++)put(3,up,south,(south+up)%4===0?'yellow_terracotta':'orange_terracotta')
  fill([30,3,59],[36,3,62],'cut_sandstone')
  for(let east=31;east<=35;east++)put(east,3,58,'smooth_sandstone_stairs',{facing:'south',half:'bottom',shape:'straight',waterlogged:'false'})
  for(const [east,south]of [[7,37],[7,41],[7,45]]) {
    fill([east,3,south],[east+1,3,south+1],'cut_sandstone')
    put(east,4,south,'lantern',{hanging:'false',waterlogged:'false'})
  }
  fill([30,2,30],[38,2,38],'cut_sandstone')
  for(let east=30;east<=38;east++)for(let south=30;south<=38;south++)put(east,3,south,east===30||east===38||south===30||south===38?'chiseled_sandstone':'water',east===30||east===38||south===30||south===38?undefined:{level:'0'})
  fill([33,3,33],[35,3,35],'cut_sandstone')
  fill([34,4,34],[34,7,34],'chiseled_sandstone')
  put(34,8,34,'smooth_sandstone_slab',{type:'bottom',waterlogged:'false'})
  const chest=(east,up,south,facing,title)=>put(east,up,south,'chest',{facing,type:'single',waterlogged:'false'},api.createBlockNbt({id:'minecraft:chest',CustomName:JSON.stringify({text:title}),Items:[{Slot:new api.NbtByte(0),id:'minecraft:gold_ingot',Count:new api.NbtByte(3)},{Slot:new api.NbtByte(13),id:'minecraft:bread',Count:new api.NbtByte(4)}]}))
  chest(5,3,20,'east','前库补给')
  chest(18,3,20,'west','前库藏物')
  chest(33,4,61,'north','后殿宝匣')
  chest(58,3,21,'north','东小室宝匣')
  chest(58,3,61,'north','密室藏物')
  for(const [east,south]of [[6,20],[17,20],[55,60],[61,60]]) {
    if(east<=17)continue
    put(east,3,south,'barrel',{facing:'up',open:'false'},api.createBlockNbt({id:'minecraft:barrel',Items:[]}))
  }
  const hanging=(east,south,roof)=>{
    put(east,roof-3,south,'lantern',{hanging:'true',waterlogged:'false'})
    for(let up=roof-2;up<roof;up++)put(east,up,south,'chain',{axis:'y',waterlogged:'false'})
  }
  for(const south of [7,19,27,39,47,59])hanging(49,south,11)
  for(const [east,south,roof]of [[25,10,13],[41,10,13],[3,11,12],[20,17,12],[3,32,14],[20,43,14],[24,56,15],[45,56,15],[55,7,12],[61,20,12],[58,55,12]])hanging(east,south,roof)
  for(const [east,south]of [[10,19],[15,19],[7,29],[18,29],[28,62],[38,62],[55,19],[61,19],[27,26],[42,43],[27,18],[40,18]]) {
    put(east,3,south,'terracotta')
    put(east,4,south,'flower_pot')
  }
  for(const [east,south]of [[26,28],[42,28],[26,41],[42,41],[7,5],[18,5],[5,49],[18,49]]) {
    put(east,2,south,'sand')
    put(east,3,south,'dead_bush')
  }
  for(const [east,south]of [[5,29],[18,46],[29,40],[39,40],[31,28],[38,28]])put(east,3,south,'smooth_sandstone_slab',{type:'bottom',waterlogged:'false'})
  floor(28,0,38,3)
  for(let east=30;east<=36;east++) {
    put(east,1,1,'smooth_sandstone_stairs',{facing:'south',half:'bottom',shape:'straight',waterlogged:'false'})
    put(east,2,2,'smooth_sandstone_stairs',{facing:'south',half:'bottom',shape:'straight',waterlogged:'false'})
    for(let up=3;up<=7;up++)put(east,up,3,'air')
  }
  fill([28,1,0],[38,2,0],'air')
  fill([28,2,1],[38,2,1],'air')
  fill([28,1,1],[29,1,1],'air')
  fill([37,1,1],[38,1,1],'air')
  for(const east of [28,38]) {
    fill([east,3,3],[east,7,3],'chiseled_sandstone')
    put(east,8,3,'lantern',{hanging:'false',waterlogged:'false'})
  }
  fill([29,10,3],[37,11,3],'chiseled_red_sandstone')
  return {structure,metadata:{name:'room'}}
}
