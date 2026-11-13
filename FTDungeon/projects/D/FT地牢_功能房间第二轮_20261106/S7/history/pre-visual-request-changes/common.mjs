export function tools(api,size) {
 const structure=api.createStructure(size)
 const defaults=block=>block.endsWith('_slab')?{type:'top',waterlogged:'false'}:block.endsWith('_log')||block==='chain'?{axis:'y'}:block==='mycelium'||block==='podzol'?{snowy:'false'}:block==='red_mushroom_block'||block==='mushroom_stem'?{up:'true',down:'true',north:'true',south:'true',east:'true',west:'true'}:block==='lantern'?{hanging:'false',waterlogged:'false'}:{}
 const put=(position,block,properties,nbt)=>api.setBlock(structure,position,`minecraft:${block}`,{...defaults(block),...properties},nbt)
 const box=(from,to,block)=>api.fill(structure,from,to,`minecraft:${block}`,defaults(block))
 function ground(){box([0,0,0],[size[0]-1,1,size[2]-1],'rooted_dirt');for(let east=0;east<size[0];east++)for(let south=0;south<size[2];south++)put([east,2,south],(east*31+south*17)%13<4?'podzol':'mycelium')}
 function walls(){const height=size[1]-2;box([0,3,0],[size[0]-1,height,1],'rooted_dirt');box([0,3,size[2]-2],[size[0]-1,height,size[2]-1],'rooted_dirt');box([0,3,0],[1,height,size[2]-1],'rooted_dirt');box([size[0]-2,3,0],[size[0]-1,height,size[2]-1],'rooted_dirt');box([0,height+1,0],[size[0]-1,size[1]-1,size[2]-1],'rooted_dirt');for(let east=2;east<size[0]-2;east+=4){box([east,3,1],[east,height,1],'purple_terracotta');box([east,3,size[2]-2],[east,height,size[2]-2],'purple_terracotta')}}
 function mushroom(east,south,base,height,radius=3){box([east,base,south],[east,base+height-1,south],'mushroom_stem');for(let dx=-radius;dx<=radius;dx++)for(let dz=-radius;dz<=radius;dz++){if(dx*dx+dz*dz>radius*radius+1)continue;put([east+dx,base+height,south+dz],'red_mushroom_block');if(Math.abs(dx)<radius&&Math.abs(dz)<radius)put([east+dx,base+height+1,south+dz],'red_mushroom_block')}put([east,base+height-1,south],'shroomlight')}
 function port(side,center,foot=3){const east=side==='west'?0:size[0]-2;box([east,foot-1,center-2],[east+1,foot-1,center+2],'dark_oak_planks');box([east,foot,center-1],[east+1,foot+3,center+1],'air');for(const south of [center-2,center+2])box([east,foot,south],[east+1,foot+4,south],'dark_oak_log');box([east,foot+4,center-2],[east+1,foot+4,center+2],'dark_oak_log');return {side,foot:[side==='west'?0:size[0]-1,foot,center],clear_width:3,clear_height:4}}
 function container(position,block='barrel',facing='south'){put(position,block,{facing},api.createBlockNbt({id:`minecraft:${block}`,Items:[]}))}
 function fungalWalls(){
  walls()
  for(let up=3;up<size[1]-1;up++)for(let east=0;east<size[0];east++)for(const south of [1,size[2]-2])put([east,up,south],(Math.floor(east/3)+Math.floor(up/3))%3===0?'brown_terracotta':'purple_terracotta')
  for(let up=3;up<size[1]-1;up++)for(let south=2;south<size[2]-2;south++)for(const east of [1,size[0]-2])put([east,up,south],(Math.floor(south/3)+Math.floor(up/3))%3===0?'brown_terracotta':'purple_terracotta')
 }
 return {structure,put,box,ground,walls:fungalWalls,mushroom,port,container}
}
