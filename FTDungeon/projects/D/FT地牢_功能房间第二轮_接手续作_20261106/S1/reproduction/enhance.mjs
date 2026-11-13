export function enhance(api,structure,design){
 const [width,height,depth]=structure.getSize()
 const set=(x,y,z,name)=>api.setBlock(structure,[x,y,z],`minecraft:${name}`)
 const fill=(a,b,name)=>api.fill(structure,a,b,`minecraft:${name}`)
 const air=(x,y,z)=>structure.getBlock([x,y,z])?.state.getName().toString()==='minecraft:air'
 for(const port of design.ports){
  const [px,feet,pz]=port.feet
  const eastWest=['west','east'].includes(port.side)
  const side=port.side==='west'?1:port.side==='east'?width-2:port.side==='north'?1:depth-2
  const center=eastWest?pz:px
  const half=Math.floor(port.width/2)
  const put=(across,y,name)=>eastWest?set(side,y,across,name):set(across,y,side,name)
  for(const across of [center-half-1,center+half+1]){
   for(let y=feet;y<feet+port.clearHeight+1;y++)put(across,y,y%3===0?'chiseled_stone_bricks':'mossy_stone_bricks')
   put(across,feet+port.clearHeight+1,'dark_oak_log[axis=y]')
  }
  for(let across=center-half;across<=center+half;across++){
   put(across,feet+port.clearHeight+1,eastWest?'dark_oak_log[axis=z]':'dark_oak_log[axis=x]')
   put(across,feet+port.clearHeight,'stone_brick_stairs[facing='+ (eastWest?(across<center?'south':'north'):(across<center?'east':'west'))+',half=top,shape=straight]')
  }
 }
 const floor=design.start[1]-1
 const roof=design.cutaway.removeAbove
 for(let x=6;x<width-4;x+=8){
  if(roof-1>floor+6){fill([x,roof-1,2],[x,roof-1,depth-3],'dark_oak_log[axis=z]');for(const z of [2,depth-3])for(let y=floor+5;y<roof-1;y++)if(air(x,y,z)&&!design.ports.some(port=>['north','south'].includes(port.side)&&Math.abs(z-port.feet[2])<=3&&Math.abs(x-port.feet[0])<=Math.floor(port.width/2)+2&&y<port.feet[1]+port.clearHeight))set(x,y,z,'mossy_stone_bricks')}
 }
 const water=structure.getBlocks().filter(block=>block.state.getName().toString()==='minecraft:water')
 const shores=new Set()
 for(const block of water){const [x,y,z]=block.pos;for(const [dx,dz] of [[1,0],[-1,0],[0,1],[0,-1]]){const nx=x+dx,nz=z+dz;if(nx<2||nx>width-3||nz<2||nz>depth-3)continue;const adjacent=structure.getBlock([nx,y,nz]);if(adjacent&&!['minecraft:air','minecraft:water','minecraft:dark_oak_planks'].includes(adjacent.state.getName().toString())&&air(nx,y+1,nz)){set(nx,y,nz,(nx+nz)%3?'moss_block':'muddy_mangrove_roots');shores.add([nx,y,nz].join(','))}}}
 for(const position of shores){const [x,y,z]=position.split(',').map(Number);if((x*3+z)%5===0&&air(x,y+1,z))set(x,y+1,z,'moss_carpet')}
 for(const x of [2,width-3])for(let z=5;z<depth-5;z+=7){
  if(design.ports.some(port=>['west','east'].includes(port.side)&&Math.abs(x-port.feet[0])<=3&&Math.abs(z-port.feet[2])<=Math.floor(port.width/2)+2))continue
  for(let rise=0;rise<5;rise++){const y=floor+2+rise;if(y<roof-1&&air(x,y,z))set(x,y,z,'dark_oak_log[axis=y]')}
  const y=Math.min(roof-2,floor+7);for(const dz of [-1,0,1])if(air(x,y,z+dz))set(x,y,z+dz,'oak_leaves[persistent=true,distance=1]')
 }
 if(design.id==='S1-12'){
  for(const x of [9,19])for(const z of [9,16,23]){
   set(x,3,z-1,'stone_bricks');set(x===9?10:18,3,z-1,'stone_button[face=wall,facing='+(x===9?'east':'west')+',powered=false]')
  }
 }
 if(design.id==='S1-15'){
  for(const [x,z] of [[10,11],[19,12],[13,17]])fill([x-2,3,z],[x+2,3,z],'dark_oak_log[axis=x]')
  fill([22,2,22],[24,5,28],'air')
  for(let step=0;step<7;step++){const rise=Math.min(step,6-step);const y=2+Math.floor(rise/2);fill([22,y,22+step],[24,y,22+step],rise%2===0?'dark_oak_slab[type=bottom]':'dark_oak_planks')}
  for(const z of [22,28])for(const x of [22,24])set(x,2,z,'dark_oak_fence')
 }
 if(design.id==='S1-13'){
  fill([5,2,7],[10,2,8],'dark_oak_fence');fill([5,3,7],[10,3,8],'oak_slab[type=top]');set(6,4,7,'flower_pot');set(9,4,7,'cake');
 }
 if(design.id==='S1-18'){
  for(const x of [4,11,12,24])fill([x,4,3],[x,13,3],'mossy_stone_bricks');fill([4,13,3],[24,13,3],'stone_brick_stairs[facing=south,half=top,shape=straight]')
 }
 if(design.id==='S1-19'){
  fill([18,7,17],[24,7,22],'clay');fill([18,8,17],[18,9,22],'mossy_stone_bricks');fill([24,8,17],[24,9,22],'mossy_stone_bricks');fill([19,8,17],[23,9,17],'mossy_stone_bricks');fill([19,8,22],[23,9,22],'mossy_stone_bricks');
  for(const x of [4,13,24]){fill([x,2,19],[x+1,7,20],'mossy_stone_bricks');fill([x,8,19],[Math.min(x+4,26),8,20],'stone_brick_stairs[facing=east,half=top,shape=straight]')}
 }
 if(design.id==='S1-20'){
  fill([11,7,4],[13,10,14],'air');fill([11,7,4],[11,7,14],'dark_oak_log[axis=z]');
  fill([15,2,2],[15,8,5],'mossy_stone_bricks');fill([28,2,2],[28,8,5],'mossy_stone_bricks');fill([15,9,2],[28,10,5],'dark_oak_log[axis=x]');
  for(const x of [16,27])fill([x,7,3],[x,8,4],'stone_brick_stairs[facing=south,half=top,shape=straight]')
 }
}
