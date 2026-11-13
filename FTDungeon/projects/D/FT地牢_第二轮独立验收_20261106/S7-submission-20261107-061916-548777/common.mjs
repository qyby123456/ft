export function tools(api,size) {
 const structure=api.createStructure(size)
 const defaults=block=>block.endsWith('_slab')?{type:'top',waterlogged:'false'}:block.endsWith('_log')||block==='chain'?{axis:'y'}:block==='mycelium'||block==='podzol'?{snowy:'false'}:block==='red_mushroom_block'||block==='mushroom_stem'?{up:'true',down:'true',north:'true',south:'true',east:'true',west:'true'}:block==='lantern'?{hanging:'false',waterlogged:'false'}:{}
 const put=(position,block,properties,nbt)=>api.setBlock(structure,position,`minecraft:${block}`,{...defaults(block),...properties},nbt)
 const box=(from,to,block)=>api.fill(structure,from,to,`minecraft:${block}`,defaults(block))
 function ground(){
  box([0,0,0],[size[0]-1,1,size[2]-1],'rooted_dirt')
  for(let east=0;east<size[0];east++)for(let south=0;south<size[2];south++){
   const field=Math.sin(east*0.29+Math.cos(south*0.21)*2)+Math.cos(south*0.31-east*0.11)+0.4*Math.sin((east+south)*0.43)
   put([east,2,south],field>1.0?'purple_terracotta':field>0.05?'mycelium':field>-0.9?'podzol':'coarse_dirt')
  }
 }
 function walls(){const height=size[1]-2;box([0,3,0],[size[0]-1,height,1],'rooted_dirt');box([0,3,size[2]-2],[size[0]-1,height,size[2]-1],'rooted_dirt');box([0,3,0],[1,height,size[2]-1],'rooted_dirt');box([size[0]-2,3,0],[size[0]-1,height,size[2]-1],'rooted_dirt');box([0,height+1,0],[size[0]-1,size[1]-1,size[2]-1],'rooted_dirt');for(let east=2;east<size[0]-2;east+=4){box([east,3,1],[east,height,1],'purple_terracotta');box([east,3,size[2]-2],[east,height,size[2]-2],'purple_terracotta')}}
 function mushroom(east,south,base,height,radius=3){box([east,base,south],[east,base+height-1,south],'mushroom_stem');for(let dx=-radius;dx<=radius;dx++)for(let dz=-radius;dz<=radius;dz++){if(dx*dx+dz*dz>radius*radius+1)continue;put([east+dx,base+height,south+dz],'red_mushroom_block');if(Math.abs(dx)<radius&&Math.abs(dz)<radius)put([east+dx,base+height+1,south+dz],'red_mushroom_block')}put([east,base+height-1,south],'shroomlight')}
 function port(side,center,foot=3){
  const east=side==='west'?0:size[0]-2,inner=side==='west'?2:size[0]-3
  box([east,foot-1,center-2],[east+1,foot-1,center+2],'dark_oak_planks')
  box([east,foot,center-1],[east+1,foot+3,center+1],'air')
  for(const south of [center-2,center+2]){
   box([east,foot,south],[east+1,foot+4,south],'dark_oak_log')
   box([inner,foot,south],[inner,foot+3,south],'mossy_stone_bricks')
   put([inner,foot+4,south],'stone_bricks')
  }
  box([east,foot+4,center-2],[east+1,foot+4,center+2],'dark_oak_log')
  box([inner,foot+5,center-2],[inner,foot+5,center+2],'mossy_stone_bricks')
  return {side,foot:[side==='west'?0:size[0]-1,foot,center],clear_width:3,clear_height:4}
 }
 function container(position,block='barrel',facing='south'){put(position,block,{facing},api.createBlockNbt({id:`minecraft:${block}`,Items:[]}))}
 function fungalWalls(){
  walls()
  function face(along,up,axis,far){
   const field=Math.sin(along*0.33+Math.cos(up*0.25)*2)+Math.cos(up*0.43+along*0.1)
   const block=field>0.8?'rooted_dirt':field>-0.6?'purple_terracotta':'brown_terracotta'
   const depth=up>size[1]*0.6?2+Math.floor((1+Math.sin(along*0.39+up*0.24))*0.8):1
   for(let inward=1;inward<=depth;inward++){
    const across=far?(axis==='x'?size[0]:size[2])-1-inward:inward
    put(axis==='x'?[across,up,along]:[along,up,across],block)
   }
  }
  for(let up=3;up<size[1]-1;up++){
   for(let along=1;along<size[0]-1;along++)for(const far of [false,true])face(along,up,'z',far)
   for(let along=2;along<size[2]-2;along++)for(const far of [false,true])face(along,up,'x',far)
  }
  for(let along=5;along<size[0]-4;along+=7){
   const up=Math.min(size[1]-5,9+(along%3))
   for(const south of [2,size[2]-3]){
    box([along-1,up,south],[along+1,up,south],'rooted_dirt')
    box([along-1,up+1,south],[along+1,up+1,south],'mycelium')
    put([along,up+2,south],'red_mushroom')
    put([along-1,up-1,south],'hanging_roots',{waterlogged:'false'})
   }
  }
  for(let along=6;along<size[2]-4;along+=8)for(const east of [2,size[0]-3]){
   const up=Math.min(size[1]-5,10+along%2)
   box([east,up,along-1],[east,up,along+1],'rooted_dirt')
   put([east,up-1,along],'hanging_roots',{waterlogged:'false'})
   put([east,up+1,along],'shroomlight')
  }
 }
 return {structure,put,box,ground,walls:fungalWalls,mushroom,port,container}
}
