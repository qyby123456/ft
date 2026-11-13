function enrich(api,structure,center,height=3){
 const [width,tall,depth]=structure.getSize();const roof=tall-3;
 const put=(pos,block)=>api.setBlock(structure,pos,`minecraft:${block}`);
 for(const east of [0,width-1]){
  for(const south of [2,depth-3]){
   api.fill(structure,[east,3,south],[east,roof,south],'minecraft:dark_oak_log[axis=y]');
   for(const across of [-1,1])api.fill(structure,[east,3,south+across],[east,4,south+across],'minecraft:mossy_cobblestone');
  }
  for(const south of [center-2,center+2])api.fill(structure,[east,height,south],[east,height+5,south],'minecraft:dark_oak_log[axis=y]');
  api.fill(structure,[east,height+5,center-2],[east,height+5,center+2],'minecraft:dark_oak_log[axis=z]');
 }
 for(let east=0;east<width;east++)for(const south of [0,depth-1]){
  put([east,roof+1,south],'mossy_cobblestone');
  if(east%5<2)put([east,roof+2,south],'mossy_stone_bricks');
 }
 for(let south=2;south<depth-2;south++)for(const east of [0,width-1]){
  put([east,roof+1,south],'mossy_cobblestone');if(south%6<2)put([east,roof+2,south],'mossy_stone_bricks');
 }
 for(let east=3;east<width-2;east+=3){
  const length=2+east%4;
  for(let height=Math.min(roof-1,9);height>Math.min(roof-1,9)-length;height--){
   const pos=[east,height,2];const existing=structure.getBlock?.(pos);
   const support=structure.getBlock([east,height,1]);
   if(support&&(!existing||existing.state.getName().toString()==='minecraft:air'))put(pos,'vine[north=true]');
  }
 }
}
export function enhance(api,structure,metadata){
 const set=(pos,block,properties,nbt)=>api.setBlock(structure,pos,`minecraft:${block}`,properties,nbt);
 const fill=(from,to,block)=>api.fill(structure,from,to,`minecraft:${block}`);
 const reference=metadata.reference;
 const centers=metadata.ports.map(port=>Math.floor(port.feet[2]));
 enrich(api,structure,centers[0],3);
 const [width,tall,depth]=structure.getSize();
 for(const east of [1,width-2])for(const south of [3,depth-4]){
  fill([east,3,south],[east,Math.min(10,tall-4),south],'dark_oak_log[axis=y]');
  set([east,3,south+1],'mossy_cobblestone');
 }
 if(reference==='S4-11'){
  fill([24,7,3],[26,7,5],'dark_oak_planks');
  for(let step=0;step<10;step++){
   const south=15-step;const height=3+Math.floor(step/2);
   fill([23,3,south],[24,height,south],step%2?'dark_oak_planks':'dark_oak_slab[type=bottom]');
  }
  metadata.targets.push({label:'upper bookshelf gallery',position:[23.5,8,4.5]});
  metadata.differences.push('Visual revision adds supported 2-wide half-step access ramp to gallery; original book ladder retained.');
 }
 if(reference==='S4-12'){
  for(const south of [9,15])for(const east of [6,14,23]){
   set([east-2,3,south],'iron_door[facing=east,half=lower,hinge=left,open=true,powered=false]');
   set([east-2,4,south],'iron_door[facing=east,half=upper,hinge=left,open=true,powered=false]');
   set([east-3,3,south],'lever[face=floor,facing=north,powered=false]');
  }
  metadata.differences.push('Door leaves are decorative beside permanent clear 3-wide entrances; levers are operable but no cell-lock or keyed mechanism is claimed.');
 }
 if(reference==='S4-13'){
  set([6,4,4],'cauldron');
  metadata.differences.push('Hanging cauldron sits above nonflammable froglight; no actual ignition source in furnace area.');
 }
 if(reference==='S4-14'){
  for(const east of [3,5])fill([east,4,6],[east,6,6],'air');
  metadata.entity_specs=[{position:[3.5,4,6.5],material:'iron'},{position:[5.5,4,6.5],material:'golden'}];
  metadata.differences=metadata.differences.filter(note=>!note.startsWith('Iron and gold block mannequins'));
  metadata.differences.push('Two real vanilla equipped armor stands on original display bases; no weapons or rewards in containers. Armor/entity payload inserted by qa/postprocess.py.');
 }
 if(reference==='S4-16'){
  set([12,5,11],'cauldron');
  metadata.differences.push('Suspended bucket replaced by recognizable native cauldron under chain, shown in dedicated closeup.');
 }
 if(reference==='S4-18'){
  const protectedTiles=new Set();
  for(const target of metadata.targets)for(let east=Math.floor(target.position[0])-1;east<=Math.floor(target.position[0])+1;east++)for(let south=Math.floor(target.position[2])-1;south<=Math.floor(target.position[2])+1;south++)protectedTiles.add(`${east},${south}`);
  for(let east=8;east<=20;east++)for(let south=7;south<=20;south++){
   if(east>=10&&east<=18&&south>=10&&south<=18)continue;
   if(protectedTiles.has(`${east},${south}`))continue;
   const existing=structure.getBlock([east,3,south]);
   if(existing&&existing.state.getName().toString()!=='minecraft:air')continue;
   set([east,2,south],'water[level=0]');
   if((east*7+south)%17===0)set([east,3,south],'lily_pad');
  }
  fill([2,2,13],[10,2,15],'dark_oak_planks');fill([18,2,13],[26,2,15],'dark_oak_planks');
  fill([13,2,6],[15,2,10],'mossy_stone_bricks');
  for(let east=8;east<=20;east++)for(let south=7;south<=20;south++){
   const decoration=structure.getBlock([east,3,south]);const below=structure.getBlock([east,2,south]);
   if(decoration?.state.getName().toString()==='minecraft:lily_pad'&&below?.state.getName().toString()!=='minecraft:water')set([east,3,south],'air');
  }
  metadata.targets.push({label:'northwest sarcophagus return',position:[5.5,3,11.5]},{label:'northeast sarcophagus return',position:[22.5,3,11.5]},{label:'southwest sarcophagus return',position:[5.5,3,24.5]});
  metadata.differences.push('Visual revision restores broad water ring around raised altar island, stone/dry approaches and timber crossing bridges; all original targets retained.');
 }
 if(reference==='S4-19'){
  fill([27,7,10],[28,12,10],'dark_oak_log[axis=y]');fill([27,7,14],[28,12,14],'dark_oak_log[axis=y]');
  fill([27,12,10],[28,12,14],'dark_oak_log[axis=z]');
  fill([0,5,23],[11,10,24],'gray_stained_glass');
  metadata.differences.push('West exterior stair viewing strip uses gray glass while full boundary remains intact.');
 }
 if(reference==='S4-20'){
  fill([8,5,11],[19,9,12],'air');
  fill([18,3,2],[19,9,6],'air');
  fill([21,3,2],[28,3,3],'dark_oak_planks');
  fill([21,4,2],[21,8,3],'dark_oak_log[axis=y]');fill([28,4,2],[28,8,3],'dark_oak_log[axis=y]');fill([21,8,2],[28,8,3],'dark_oak_log[axis=x]');
  set([24,3,4],'chest',{facing:'south',type:'single',waterlogged:'false'},api.createBlockNbt({id:'minecraft:chest',Items:[]}));
  metadata.differences.push('Vault focal niche framed in coarse timber, high sight-blocking inner partition lowered above unchanged hidden passage; all route targets retained.');
 }
 metadata.visual_revision='Parent visual REQUEST_CHANGES 20261106: coarse framing, staggered eaves, plant density plus room-specific fixes; earlier evidence retained in qa/pre-visual-revision.';
}
