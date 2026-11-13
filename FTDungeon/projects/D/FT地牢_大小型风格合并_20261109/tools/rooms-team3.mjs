import fs from 'node:fs';
import path from 'node:path';
import crypto from 'node:crypto';
import {createRequire} from 'node:module';
import {fileURLToPath} from 'node:url';
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..');
const skill=process.env.MINECRAFT_BUILDER_SKILL??'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill';
const {Structure,NbtFile,BlockState}=createRequire(path.join(skill,'package.json'))('@mattzh72/lodestone');
const data=path.join(root,'src/main/resources/data/fteclipsegen');
const variants=JSON.parse(fs.readFileSync(path.join(data,'catalog.json'),'utf8')).variants;
const air='minecraft:air';
const sha=value=>crypto.createHash('sha256').update(value).digest('hex');
const full=state=>state!==air&&!/stairs|slab|roots|fence|chain/.test(state);
const themes={swamp:{wall:'mud_bricks',floor:'packed_mud',trim:'mangrove_planks',beam:'mangrove_log',light:'shroomlight',stair:'mangrove_stairs',slab:'mangrove_slab',names:['药草作坊','红树根厅','泥砖墓库','菌菇实验室','木架回廊']},coral:{wall:'prismarine_bricks',floor:'smooth_quartz',trim:'dark_prismarine',beam:'quartz_pillar',light:'sea_lantern',stair:'prismarine_brick_stairs',slab:'prismarine_brick_slab',names:['海晶潮殿','贝壳拱厅','航海图室','干珊瑚花园','珍珠祭台']}};

function room(style,variant){
 if(variant.key==='spawn/spawn'||variant.key==='boss/eclipse_keeper')return ceremonialRoom(style,variant);
 const theme=themes[style], [width,height,depth]=variant.size;
 const states=new Array(width*height*depth).fill(air);
 const inside=(x,y,z)=>x>=0&&y>=0&&z>=0&&x<width&&y<height&&z<depth;
 const index=(x,y,z)=>(y*depth+z)*width+x;
 const get=(x,y,z)=>inside(x,y,z)?states[index(x,y,z)]:'minecraft:bedrock';
 const put=(x,y,z,block)=>{if(!inside(x,y,z))throw Error(`Bounds ${variant.key} ${[x,y,z]}`);states[index(x,y,z)]=block.startsWith('minecraft:')?block:`minecraft:${block}`;};
 const box=(from,to,block)=>{for(let y=from[1];y<=to[1];y++)for(let z=from[2];z<=to[2];z++)for(let x=from[0];x<=to[0];x++)put(x,y,z,block);};
 const child=variant.key.includes('/layout/'), base=child?1:variant.key==='spawn/spawn'?4:2, ceiling=height-2;
 const seed=variants.findIndex(entry=>entry.key===variant.key), kind=seed%5;
 box([0,Math.max(variant.minimum_y,base-2),0],[width-1,base-1,depth-1],theme.floor);
 box([0,base,0],[0,height-1,depth-1],theme.wall);box([width-1,base,0],[width-1,height-1,depth-1],theme.wall);
 box([0,base,0],[width-1,height-1,0],theme.wall);box([0,base,depth-1],[width-1,height-1,depth-1],theme.wall);
 box([0,ceiling,0],[width-1,height-1,depth-1],theme.wall);
 for(let x=1;x<width-1;x++)for(let z=1;z<depth-1;z++)if(style==='swamp'?(x+z)%5===0:(x-width/2)**2+(z-depth/2)**2<12||(x+z)%7===0)put(x,base-1,z,theme.trim);
 const safe=new Set(), skeletonAir=new Set();
 const reserve=(point,radius=0)=>{for(let dx=-radius;dx<=radius;dx++)for(let dz=-radius;dz<=radius;dz++)for(let dy=0;dy<3;dy++)safe.add([point[0]+dx,point[1]+dy,point[2]+dz].join(','));};
 for(let x=6;x<=9&&x<width-1;x++)for(let z=1;z<depth-1;z++)reserve([x,base,z]);
 for(let z=Math.floor(depth/2)-1;z<=Math.floor(depth/2)+1;z++)for(let x=1;x<width-1;x++)reserve([x,base,z]);
 for(const socket of variant.sockets){reserve(socket.position,1);reserve(socket.spawn_position,1);}
 const deco=(x,y,z,block)=>{if(inside(x,y,z)&&!safe.has([x,y,z].join(',')))put(x,y,z,block);};
 const decoBox=(from,to,block)=>{for(let y=from[1];y<=to[1];y++)for(let z=from[2];z<=to[2];z++)for(let x=from[0];x<=to[0];x++)deco(x,y,z,block);};
 const skeleton=variant.key.startsWith('stairs/')||variant.key==='straight/platform';
 if(skeleton){
  const source=Structure.fromNbt(NbtFile.read(fs.readFileSync(path.join(data,'structures',variant.template+'.nbt'))).root);
  const sourceAir=(x,y,z)=>source.getBlock([x,y,z])?.state.toString()===air;
  for(const block of source.getBlocks()){
   const [x,y,z]=block.pos, state=block.state.toString();
   if(state===air||x===0||x===width-1||z===0||z===depth-1||y<base-1||y+3>=height||!sourceAir(x,y+1,z)||!sourceAir(x,y+2,z)||!sourceAir(x,y+3,z))continue;
   let replacement=theme.trim;
   if(state.includes('_stairs'))replacement=theme.stair+state.slice(state.indexOf('['));
   else if(state.includes('_slab'))replacement=theme.slab+state.slice(state.indexOf('['));
   put(x,y,z,replacement);if(y>base)put(x,y-1,z,theme.beam);
   for(let dy=1;dy<=3;dy++)skeletonAir.add([x,y+dy,z].join(','));
  }
  for(const key of skeletonAir){safe.add(key);put(...key.split(',').map(Number),air);}
 }
 if(style==='swamp'){
  for(let z=3;z<depth-2;z+=4){
   for(const x of [2,width-3]){decoBox([x,base,z],[x,Math.min(ceiling-1,base+6),z],'mangrove_log');deco(x+(x<width/2?1:-1),base,z,'mangrove_roots');deco(x,base+1,z+1,'mangrove_roots');}
   for(let x=2;x<width-2;x++)deco(x,Math.min(ceiling-1,base+7),z,'mangrove_log[axis=x]');
  }
  for(const x of [1,width-2])for(let z=2;z<depth-2;z+=3){
   const feature=(seed+z)%5;
   if(feature===0){decoBox([x,base,z],[x,base+1,z+1],'mud_bricks');deco(x,base+2,z,'moss_block');deco(x,base+3,z,'red_mushroom_block');}
   else if(feature===1){decoBox([x,base,z],[x,base+3,z],'mushroom_stem');decoBox([Math.max(1,x-1),base+4,z-1],[Math.min(width-2,x+1),base+4,z+1],'brown_mushroom_block');}
   else if(feature===2){decoBox([x,base,z],[x,base+2,z+1],'chiseled_stone_bricks');deco(x,base+3,z,'mossy_stone_bricks');}
   else{deco(x,base,z,'mangrove_planks');deco(x,base+1,z,feature===3?'hay_block':'bookshelf');deco(x,base+2,z,'moss_block');}
  }
  for(let z=2;z<depth-2;z+=4){deco(1,base+5,z,'shroomlight');deco(width-2,base+5,z,'shroomlight');}
 }else{
  for(let z=2;z<depth-1;z+=4)for(let x=1;x<width-1;x++){
   const normalized=Math.abs((x-(width-1)/2)/((width-2)/2)), archY=Math.min(ceiling-1,base+5+Math.round(4*(1-normalized*normalized)));
   deco(x,archY,z,(x+z)%3===0?'sea_lantern':'smooth_quartz');
   if(x===1||x===width-2)decoBox([x,base,z],[x,archY,z],'quartz_pillar');
   if(x%3===0)deco(x,archY+1,z,'pink_terracotta');
  }
  for(const x of [2,width-3])for(let z=3;z<depth-2;z+=4){
   const feature=(seed+z)%5;
   if(feature===0){deco(x,base,z,'dark_prismarine');deco(x,base+1,z,'pearlescent_froglight');deco(x,base+2,z,'white_stained_glass');}
   else if(feature===1){for(let dy=0;dy<3;dy++)deco(x+(dy%2)*(x<width/2?1:-1),base+dy,z,['dead_tube_coral_block','dead_brain_coral_block','dead_fire_coral_block'][dy]);}
   else if(feature===2){decoBox([x,base,z],[x,base,z+1],'cartography_table');deco(x,base+1,z,'blue_glazed_terracotta');}
   else if(feature===3){decoBox([x,base,z],[x,base+2,z],'prismarine');deco(x,base+3,z,'sea_lantern');}
   else{deco(x,base,z,'pink_terracotta');deco(x,base+1,z,'calcite');}
  }
 }
 if(!skeleton&&!child)for(let z=4;z<depth-3;z+=6){
  if(style==='swamp'){decoBox([3,base,z],[4,base,z+1],kind===2?'mud_bricks':'mangrove_planks');deco(3,base+1,z,kind===3?'mushroom_stem':'moss_block');deco(width-4,base+1,z+1,kind===0?'hay_block':'mangrove_roots');}
  else{for(let dx=-2;dx<=2;dx++)deco(Math.floor(width/2)+dx,base+5+Math.abs(dx),z,kind===2?'blue_glazed_terracotta':'calcite');decoBox([width-5,base,z],[width-4,base,z+1],kind===3?'dead_horn_coral_block':'dark_prismarine');}
 }
 if(variant.key.startsWith('terminal/')||variant.key==='promenade'){
  const center=Math.floor(width/2);
  if(style==='swamp'){decoBox([center-3,base,depth-4],[center+3,base,depth-3],variant.key.endsWith('cells')?'mud_bricks':'mangrove_planks');for(let x=center-3;x<=center+3;x+=3)deco(x,base+1,depth-3,'moss_block');}
  else for(let step=0;step<4;step++)decoBox([center-3+step,base+step,depth-3],[center+3-step,base+step,depth-3],step===3?'pearlescent_froglight':'calcite');
 }
 if(variant.key.startsWith('boss/')){
  const center=Math.floor(width/2);
  if(style==='swamp'){
   for(const x of [5,width-6])for(const z of [5,13,depth-6]){decoBox([x,base,z],[x,ceiling-3,z],'mangrove_log');for(let branch=0;branch<4;branch++)deco(x+(x<center?1:-1)*branch,base+7+branch,z,'mangrove_roots');}
   decoBox([center-3,base,depth-5],[center+3,base+2,depth-3],'mud_bricks');decoBox([center-2,base+3,depth-4],[center+2,base+3,depth-3],'moss_block');
  }else{
   for(let radius=3;radius<=8;radius+=2)for(let dx=-radius;dx<=radius;dx++)deco(center+dx,base+5+Math.round(Math.sqrt(radius*radius-dx*dx)),depth-5,'calcite');
   decoBox([center-3,base,depth-5],[center+3,base,depth-3],'dark_prismarine');decoBox([center-1,base+1,depth-4],[center+1,base+2,depth-3],'pearlescent_froglight');
  }
 }
 if(skeleton)for(const key of skeletonAir)put(...key.split(',').map(Number),air);
 const clear=(point,clearance=3)=>{const [x,y,z]=point;for(let dy=0;dy<clearance;dy++)put(x,y+dy,z,air);put(x,y-1,z,theme.floor);};
 for(const port of variant.ports)for(const point of port.cells){clear(point,5);const [x,y,z]=point;for(let distance=1;distance<=2;distance++)clear([x===0?distance:x===width-1?x-distance:x,y,z===0?distance:z===depth-1?z-distance:z],5);}
 for(const socket of variant.sockets){clear(socket.position);clear(socket.spawn_position);put(...socket.support,theme.floor);}
 if(variant.key==='spawn/spawn')clear([7,4,8]);if(variant.key==='promenade')clear([7,2,11]);
 if(variant.key.startsWith('boss/')){box([15,2,20],[18,7,24],air);for(const point of [[16,2,2],[16,2,25],[16,2,13]])clear(point);}
 return {states,size:variant.size,get,base,skeleton,theme:theme.names[kind]};
}

function ceremonialRoom(style,variant){
 const [width,height,depth]=variant.size, boss=variant.key.startsWith('boss/'), base=boss?2:4, theme=themes[style];
 const states=new Array(width*height*depth).fill(air);
 const inside=(x,y,z)=>x>=0&&y>=0&&z>=0&&x<width&&y<height&&z<depth;
 const get=(x,y,z)=>inside(x,y,z)?states[(y*depth+z)*width+x]:'minecraft:bedrock';
 const put=(x,y,z,block)=>{if(!inside(x,y,z))throw Error(`Ceremony bounds ${style} ${[x,y,z]}`);states[(y*depth+z)*width+x]=block.startsWith('minecraft:')?block:'minecraft:'+block;};
 const box=(from,to,block)=>{for(let y=from[1];y<=to[1];y++)for(let z=from[2];z<=to[2];z++)for(let x=from[0];x<=to[0];x++)put(x,y,z,block);};
 const line=(from,to,block,radius=0)=>{
  const steps=Math.max(...to.map((value,index)=>Math.abs(value-from[index])))*3;
  for(let step=0;step<=steps;step++){
   const point=from.map((value,index)=>Math.round(value+(to[index]-value)*step/Math.max(steps,1)));
   for(let dx=-radius;dx<=radius;dx++)for(let dy=-radius;dy<=radius;dy++)for(let dz=-radius;dz<=radius;dz++)if(inside(point[0]+dx,point[1]+dy,point[2]+dz))put(point[0]+dx,point[1]+dy,point[2]+dz,block);
  }
 };
 const orb=(center,radii,block)=>{for(let x=Math.ceil(center[0]-radii[0]);x<=Math.floor(center[0]+radii[0]);x++)for(let y=Math.ceil(center[1]-radii[1]);y<=Math.floor(center[1]+radii[1]);y++)for(let z=Math.ceil(center[2]-radii[2]);z<=Math.floor(center[2]+radii[2]);z++)if(inside(x,y,z)&&((x-center[0])/radii[0])**2+((y-center[1])/radii[1])**2+((z-center[2])/radii[2])**2<=1)put(x,y,z,block);};
 const crown=(center,scale)=>{
  const [centerX,centerY,centerZ]=center;
  for(let spoke=0;spoke<9;spoke++){
   const angle=spoke*Math.PI*2/9;
   const anchor=[centerX+Math.round(Math.cos(angle)*scale),centerY,centerZ+Math.round(Math.sin(angle)*scale*0.65)];
   const elbow=[centerX+Math.round(Math.cos(angle)*scale*0.72),centerY+Math.round(scale*0.9),centerZ+Math.round(Math.sin(angle)*scale*0.5)];
   const tip=[centerX+Math.round(Math.cos(angle)*scale*0.36),centerY+Math.round(scale*1.65)+(spoke%2),centerZ+Math.round(Math.sin(angle)*scale*0.3)];
   line(anchor,elbow,'mangrove_log',scale>4?1:0);line(elbow,tip,'mangrove_roots');
   if(spoke%2===0)put(...tip,'shroomlight');
  }
 };
 const shellRib=(centerX,footY,backZ,radius,rise)=>{
  for(let step=0;step<=70;step++){
   const angle=step*Math.PI/70;
   const point=[Math.round(centerX+Math.cos(angle)*radius),Math.round(footY+Math.sin(angle)*rise),Math.round(backZ-Math.sin(angle)*1.5)];
   put(...point,'calcite');
   if(inside(point[0],point[1],point[2]+1))put(point[0],point[1],point[2]+1,'smooth_quartz');
  }
 };
 box([0,variant.minimum_y,0],[width-1,base-1,depth-1],theme.floor);
 box([0,base,0],[0,height-1,depth-1],theme.wall);box([width-1,base,0],[width-1,height-1,depth-1],theme.wall);
 box([0,base,0],[width-1,height-1,0],theme.wall);box([0,base,depth-1],[width-1,height-1,depth-1],theme.wall);
 box([0,height-2,0],[width-1,height-1,depth-1],boss?(style==='swamp'?'dark_oak_planks':'dark_prismarine'):theme.wall);
 for(let x=1;x<width-1;x++)for(let z=1;z<depth-1;z++){
  const dx=x-(boss?16:7.5), dz=z-(boss?12:8);
  const ring=Math.round(Math.sqrt(dx*dx+dz*dz));
  if(style==='swamp'?(ring%5===0&&z<22):((x+z)%8===0||ring===5||ring===9))put(x,base-1,z,style==='swamp'?'mangrove_planks':'prismarine_bricks');
  if((boss?x>=15&&x<=17:x>=6&&x<=9))put(x,base-1,z,style==='swamp'?'polished_andesite':'smooth_quartz');
 }
 if(!boss&&style==='swamp'){
  for(const [x,z]of [[2,4],[12,5],[3,10],[11,11]]){
   box([x-1,4,z-1],[x+1,4,z+1],'mud_bricks');box([x,5,z],[x,5,z],'moss_block');
   line([x,5,z],[x+(x<7?1:-1),8,z],'mangrove_log');
   orb([x+(x<7?1:-1),9,z],[1.6,1.3,1.7],'mangrove_roots');put(x,8,z,'shroomlight');
  }
  box([1,4,1],[4,5,2],'mangrove_planks');
  for(let x=1;x<=4;x++){put(x,6,1,x%2?'moss_block':'hay_block');put(x,7,1,'brown_mushroom_block');}
  crown([7,5,3],3);
  for(const side of [3,12]){line([side,4,13],[side+(side<7?1:-1),10,13],'mangrove_log',1);line([side,10,13],[7,12,13],'mangrove_roots');put(side,9,12,'shroomlight');}
  box([5,11,14],[10,12,14],'mud_bricks');put(7,11,13,'chiseled_stone_bricks');
  for(const z of [6,10]){box([1,4,z],[1,6,z+1],'bookshelf');put(2,4,z,'mangrove_roots');}
 }else if(!boss){
  shellRib(7.5,4,3,6,8);shellRib(7.5,5,5,5,7);
  for(let spoke=0;spoke<9;spoke++){
   const angle=Math.PI*spoke/8;
   line([7,5,2],[Math.round(7.5+Math.cos(angle)*5),Math.round(5+Math.sin(angle)*6),3],spoke%2?'pink_terracotta':'calcite');
  }
  orb([7.5,8,3],[1.8,1.8,1.1],'pearlescent_froglight');
  for(const [x,z]of [[2,8],[12,9]]){
   box([x-1,4,z-1],[x+1,4,z+1],'dark_prismarine');
   for(let step=0;step<24;step++){
    const angle=step*Math.PI*2/24;
    put(Math.round(x+Math.cos(angle)*1.6),Math.round(7+Math.sin(angle)*1.8),z,'waxed_cut_copper');
   }
   put(x,7,z,'sea_lantern');put(x,5,z,'blue_glazed_terracotta');
  }
  for(const x of [3,12]){line([x,4,13],[x,8,13],'prismarine_bricks');line([x,8,13],[7,12,13],'calcite');}
  for(const [x,z]of [[2,11],[12,4]]){put(x,4,z,'dead_brain_coral_block');put(x,5,z,'dead_tube_coral_block');put(x+1,5,z,'dead_horn_coral_block');}
 }else if(style==='swamp'){
  for(const side of [-1,1])for(const z of [6,12,18]){
   const anchorX=side<0?2:31;
   const elbowX=16+side*(z===12?9:11);
   line([anchorX,2,z],[elbowX,9,z+1],'mangrove_log',1);
   line([elbowX,9,z+1],[16+side*5,16,z+2],'mangrove_log',1);
   line([16+side*5,16,z+2],[16,19,z+1],'mangrove_roots');
   line([anchorX,3,z],[anchorX-side*3,2,z+3],'mangrove_roots');
  }
  crown([16,7,17],7);
  for(const [x,z]of [[5,8],[28,8],[5,17],[28,17]]){
   for(let dx=-2;dx<=2;dx++)for(let dz=-2;dz<=2;dz++){
    const edge=Math.max(Math.abs(dx),Math.abs(dz))===2;
    put(x+dx,edge?2:1,z+dz,edge?'mud_bricks':(dx+dz)%2?'green_terracotta':'moss_block');
   }
   put(x,1,z,'shroomlight');
   line([x,3,z+2],[x+(x<16?1:-1),5,z+2],'mushroom_stem');put(x,6,z+2,'brown_mushroom_block');
  }
  box([11,2,19],[21,2,20],'mud_bricks');
  for(const x of [12,20]){put(x,3,20,'chiseled_stone_bricks');put(x,4,20,'shroomlight');}
  for(let x=1;x<33;x++)if(x<12||x>21)put(x,9,21,'mangrove_roots');
 }else{
  for(const backZ of [7,13,19]){
   shellRib(16.5,2,backZ,14,16);
   shellRib(16.5,3,backZ+1,12,14);
   for(let step=0;step<36;step++){
    const angle=step*Math.PI/35;
    const x=Math.round(16.5+Math.cos(angle)*13),y=Math.round(3+Math.sin(angle)*15);
    if(step%6===0)put(x,y,backZ,'sea_lantern');
   }
  }
  for(let spoke=0;spoke<13;spoke++){
   const angle=spoke*Math.PI/12;
   line([16,7,21],[Math.round(16.5+Math.cos(angle)*12),Math.round(7+Math.sin(angle)*12),21],spoke%2?'pink_terracotta':'calcite');
  }
  orb([16.5,12,19.5],[3.5,3.7,1.6],'white_stained_glass');
  orb([16.5,12,19.3],[2.4,2.6,1.2],'pearlescent_froglight');
  for(const x of [4,29])for(const z of [7,16]){
   box([x-1,2,z-1],[x+1,2,z+1],'dark_prismarine');
   put(x,3,z,'dead_brain_coral_block');put(x,4,z,'dead_fire_coral_block');put(x+(x<16?1:-1),4,z,'dead_horn_coral_block');
  }
  box([12,2,19],[21,2,20],'prismarine_bricks');
 }
 if(boss){
  box([1,2,22],[width-2,height-2,22],style==='swamp'?'mud_bricks':'prismarine_bricks');
  for(const x of [13,20])box([x,2,21],[x,9,23],style==='swamp'?'mangrove_log':'calcite');
  box([14,8,21],[19,9,23],style==='swamp'?'mangrove_roots':'smooth_quartz');
  box([15,2,22],[18,7,22],air);
  for(let x=15;x<=17;x++)for(let z=1;z<depth-1;z++)box([x,2,z],[x,4,z],air);
  for(const x of [4,29])put(x,5,25,theme.light);
 }else{
  for(let x=6;x<=9;x++)for(let z=7;z<depth;z++)box([x,4,z],[x,6,z],air);
 }
 const clear=(point,clearance=3)=>{const [x,y,z]=point;box([x,y,z],[x,y+clearance-1,z],air);put(x,y-1,z,theme.floor);};
 for(const port of variant.ports)for(const [x,y,z]of port.cells)for(let inset=0;inset<=2;inset++)clear([x,y,z-inset],5);
 for(const socket of variant.sockets){
  for(const point of [socket.position,socket.spawn_position]){
   for(let dx=-1;dx<=1;dx++)for(let dz=-1;dz<=1;dz++)clear([point[0]+dx,point[1],point[2]+dz]);
  }
  put(...socket.support,theme.floor);
 }
 if(boss){for(const point of [[16,2,2],[16,2,25],[16,2,13]])clear(point);}else clear([7,4,8]);
 return {states,size:variant.size,get,base,skeleton:false,theme:boss?(style==='swamp'?'红树女巫祭殿·根冠与干燥药池':'深海珍珠圣殿·巨型贝壳肋拱'):(style==='swamp'?'药草封印门厅·红树芽床':'贝壳航标·潮汐仪门厅')};
}

function validate(built,variant){
 const {get,size}=built;
 const targets=variant.ports.flatMap(port=>port.cells).concat(variant.sockets.flatMap(socket=>[socket.position,socket.spawn_position]));
 if(variant.key==='spawn/spawn')targets.push([7,4,8]);if(variant.key==='promenade')targets.push([7,2,11]);
 if(variant.key.startsWith('boss/'))targets.push([16,2,2],[16,2,25],[16,2,13]);
 for(const [x,y,z]of targets){for(let dy=0;dy<3;dy++)if(get(x,y+dy,z)!==air)throw Error(`Blocked ${variant.key} ${[x,y,z]}`);if(!full(get(x,y-1,z)))throw Error(`Support ${variant.key}`);}
 for(const port of variant.ports)for(const [x,y,z]of port.cells)for(let dy=0;dy<5;dy++)if(get(x,y+dy,z)!==air)throw Error(`Port5 ${variant.key}`);
 for(const socket of variant.sockets)if(!full(get(...socket.support)))throw Error(`Socket support ${variant.key}`);
 const walk=new Map();
 for(let x=0;x<size[0];x++)for(let z=0;z<size[2];z++)for(let y=1;y<size[1]-2;y++)if(get(x,y-1,z)!==air&&get(x,y,z)===air&&get(x,y+1,z)===air&&get(x,y+2,z)===air)walk.set([x,y,z].join(','),[x,y,z]);
 const start=targets[0]??[Math.floor(size[0]/2),built.base,Math.floor(size[2]/2)], visited=new Set([start.join(',')]), queue=[start];
 for(let cursor=0;cursor<queue.length;cursor++){
  const [x,y,z]=queue[cursor];
  for(const [dx,dz]of [[1,0],[-1,0],[0,1],[0,-1]])for(const dy of [0,1,-1]){const point=[x+dx,y+dy,z+dz],key=point.join(',');if(walk.has(key)&&!visited.has(key)){visited.add(key);queue.push(point);}}
 }
 const unreachable=targets.filter(point=>!visited.has(point.join(',')));if(unreachable.length)throw Error(`Unreachable ${variant.key}: ${JSON.stringify(unreachable)}`);
 for(const socket of variant.sockets){const [x,y,z]=socket.position;let neighbors=0;for(let dx=-6;dx<=6;dx++)for(let dz=-6;dz<=6;dz++){const distance=Math.abs(dx)+Math.abs(dz);if(distance>=1&&distance<=6&&full(get(x+dx,y-1,z+dz))&&visited.has([x+dx,y,z+dz].join(',')))neighbors++;}if(!neighbors)throw Error(`Spawn neighbor ${variant.key} ${socket.id}`);}
 if(variant.key.startsWith('boss/'))for(let x=15;x<=18;x++)for(let y=2;y<=7;y++)if(get(x,y,22)!==air)throw Error('Boss gate');
 for(let x=0;x<size[0];x++)for(let z=0;z<size[2];z++)if(get(x,size[1]-1,z)===air)throw Error('Roof hole');
 const openings=new Set(variant.ports.flatMap(port=>port.cells.flatMap(([x,y,z])=>Array.from({length:5},(_,rise)=>[x,y+rise,z].join(',')))));
 for(let y=built.base;y<size[1];y++)for(let x=0;x<size[0];x++)for(let z=0;z<size[2];z++)if((x===0||z===0||x===size[0]-1||z===size[2]-1)&&get(x,y,z)===air&&!openings.has([x,y,z].join(',')))throw Error(`Wall hole ${variant.key}`);
 if(built.states.some(state=>/minecraft:(water|lava|sand|red_sand|gravel|.*concrete_powder)(\[|$)/.test(state)))throw Error('Dangerous block');
 let sealedGateCheck;
 if(variant.key.startsWith('boss/')){
  const sealed=new Set(['16,2,2']),pending=[[16,2,2]];
  const blocked=point=>point[0]>=15&&point[0]<=18&&point[1]>=2&&point[1]<=7&&point[2]===22;
  for(let cursor=0;cursor<pending.length;cursor++){
   const [x,y,z]=pending[cursor];
   for(const [dx,dz]of [[1,0],[-1,0],[0,1],[0,-1]])for(const dy of [0,1,-1]){
    const point=[x+dx,y+dy,z+dz],key=point.join(',');
    if(walk.has(key)&&!blocked(point)&&!blocked([point[0],point[1]+1,point[2]])&&!blocked([point[0],point[1]+2,point[2]])&&!sealed.has(key)){sealed.add(key);pending.push(point);}
   }
  }
  if(sealed.has('16,2,25')||[...sealed].some(key=>Number(key.split(',')[2])>22))throw Error('Return partition bypass');
  sealedGateCheck='Blocked gate separates entire return chamber PASS';
  for(let x=15;x<=17;x++)for(let z=1;z<27;z++)for(let y=2;y<=4;y++)if(get(x,y,z)!==air)throw Error('Three-wide Boss main route');
 }
 return {portCells:variant.ports.reduce((sum,port)=>sum+port.cells.length,0),sockets:variant.sockets.length,safeTargets:targets.length,reachableTargets:targets.length,reachableWalkCells:visited.size,denseBlocks:built.states.length,skeletonReused:built.skeleton,sealedGateCheck,checks:'5AIR-ports/3AIR-sockets/full-support/spawn-neighbor/bounds/dry/boss-gate/reachable PASS'};
}
const parsedStates=new Map();
function parseState(source){
 if(!parsedStates.has(source)){
  const state=BlockState.parse(source);
  if(!/^[a-z0-9_.-]+:[a-z0-9_./-]+$/.test(state.getName().toString()))throw Error('Invalid ResourceLocation '+source);
  parsedStates.set(source,state);
 }
 return parsedStates.get(source);
}
function structure(built){
 const result=new Structure(built.size);
 for(let y=0;y<built.size[1];y++)for(let z=0;z<built.size[2];z++)for(let x=0;x<built.size[0];x++){
  const state=parseState(built.get(x,y,z));
  result.addBlock([x,y,z],state.getName().toString(),state.getProperties());
 }
 return result;
}
function validatePalette(nbt,built){
 let propertyEntries=0;
 nbt.getList('palette',10).forEach(entry=>{
  const name=entry.getString('Name');
  if(!/^[a-z0-9_.-]+:[a-z0-9_./-]+$/.test(name))throw Error('Illegal palette Name '+name);
  if(entry.has('Properties')){
   if(!entry.hasCompound('Properties'))throw Error('Invalid Properties '+name);
   const properties=entry.getCompound('Properties');
   for(const key of properties.keys())if(!properties.hasString(key))throw Error('Non-string property '+name+' '+key);
   propertyEntries++;
  }
 });
 const roundtrip=Structure.fromNbt(nbt);
 for(const block of roundtrip.getBlocks())if(!block.state.equals(parseState(built.get(...block.pos))))throw Error('Lost block state '+block.pos);
 return {paletteNames:'valid ResourceLocation',stateProperties:'exact per-block roundtrip PASS',propertyEntries};
}
export default function build(){const style=process.env.ROOM_STYLE??'swamp',key=process.env.ROOM_KEY??'straight/simple',variant=variants.find(entry=>entry.key===key);const built=room(style,variant);validate(built,variant);return {structure:structure(built),metadata:{name:`rooms-team3-${style}-${key.replaceAll('/','-')}`}};}
function ceremonialPreview(cache){
 const selected=Object.entries(cache).flatMap(([style,entries])=>entries.filter(({variant})=>variant.key==='spawn/spawn'||variant.key==='boss/eclipse_keeper').map(({variant,built})=>({style,key:variant.key,theme:built.theme,size:built.size,base:built.base,states:built.states})));
 const colors=state=>{
  if(state===air)return '#172b36';
  if(/light|lantern/.test(state))return '#ffe9a4';
  if(/roots/.test(state))return '#627b4d';
  if(/mangrove_log/.test(state))return '#865548';
  if(/mangrove_planks/.test(state))return '#9b6855';
  if(/moss|green_terracotta/.test(state))return '#65804c';
  if(/calcite|quartz/.test(state))return '#e9e0d3';
  if(/glass/.test(state))return '#b8dddf';
  if(/pink/.test(state))return '#c78e8d';
  if(/prismarine/.test(state))return '#3b8d8b';
  if(/coral/.test(state))return '#928082';
  if(/copper/.test(state))return '#bf825e';
  return '#72654c';
 };
 const cards=selected.map((entry,index)=>{
  const [width,height,depth]=entry.size;
  const at=(x,y,z)=>entry.states[(y*depth+z)*width+x];
  const view=(axis,slice)=>{
   const horizontal=axis==='side'?depth:width, vertical=axis==='plan'?depth:height;
   let tiles='';
   for(let row=0;row<vertical;row++)for(let col=0;col<horizontal;col++){
    const point=axis==='plan'?[col,slice,row]:axis==='side'?[slice,height-1-row,col]:[col,height-1-row,slice];
    const block=at(...point);
    tiles+=`<rect x="${col*10}" y="${row*10}" width="10" height="10" fill="${colors(block)}"><title>${point.join(',')}: ${block}</title></rect>`;
   }
   return `<svg viewBox="0 0 ${horizontal*10} ${vertical*10}" xmlns="http://www.w3.org/2000/svg">${tiles}</svg>`;
  };
  const back=entry.key.startsWith('boss/')?19:3;
  const front=view('front',back),side=view('side',entry.key.startsWith('boss/')?16:7),plan=view('plan',entry.base+1);
  const name=entry.style+'-'+entry.key.replaceAll('/','-');
  fs.writeFileSync(path.join(root,'output/rooms-team3-section-'+name+'.svg'),front);
  return `<article><h2>${entry.style} · ${entry.theme}</h2><p>${entry.key} / ${entry.size.join(' × ')}，黄：光源，绿：根/药池，白：贝壳/珍珠。悬停格子显示坐标与状态。</p><div class="views"><figure>${plan}<figcaption>平面 y=${entry.base+1} · 中央主路与战斗区</figcaption></figure><figure>${front}<figcaption>横剖 z=${back} · 根冠 / 贝壳肋拱</figcaption></figure><figure>${side}<figcaption>纵剖 x=${entry.key.startsWith('boss/')?16:7} · 上部祭仪与返程隔墙</figcaption></figure></div><label>横剖深度 z：<input type="range" min="0" max="${depth-1}" value="${back}" data-room="${index}"><output>${back}</output></label><div class="dynamic" id="slice-${index}">${front}</div></article>`;
 }).join('');
 const script=`const rooms=${JSON.stringify(selected)};const color=${colors.toString()};const air='minecraft:air';document.querySelectorAll('input').forEach(input=>input.addEventListener('input',()=>{input.nextElementSibling.value=input.value;const room=rooms[Number(input.dataset.room)],[width,height,depth]=room.size;let tiles='';for(let y=0;y<height;y++)for(let x=0;x<width;x++){const block=room.states[(y*depth+Number(input.value))*width+x];tiles+='<rect x="'+x*10+'" y="'+(height-1-y)*10+'" width="10" height="10" fill="'+color(block)+'"><title>'+[x,y,input.value]+': '+block+'</title></rect>';}document.getElementById('slice-'+input.dataset.room).innerHTML='<svg viewBox="0 0 '+width*10+' '+height*10+'">'+tiles+'</svg>';}));`;
 fs.writeFileSync(path.join(root,'output/rooms-team3-ceremony-sections.html'),`<!doctype html><html lang="zh"><meta charset="utf-8"><title>幽沼与珊瑚 · 初始 / Boss 重设计剖视</title><style>body{background:#101e27;color:#e6ece9;font:15px system-ui;margin:32px auto;max-width:1440px;padding:0 24px}h1{font-size:32px}article{background:#20333d;border:1px solid #46606c;border-radius:16px;padding:24px;margin:24px 0}h2{font-size:23px}p,figcaption{color:#b8c9cb}figure{margin:0}svg{width:100%;max-height:360px;image-rendering:pixelated}.views{display:grid;grid-template-columns:repeat(3,1fr);gap:20px}.dynamic{max-width:700px;margin:20px auto}input{width:45%;margin:24px}figcaption{margin:12px 0}@media(max-width:850px){.views{grid-template-columns:1fr}}</style><h1>从初始器物到巨型祭仪</h1><p>幽沼：红树芽床、药草封印 → 巨大根冠、干燥药池。珊瑚：贝壳航标、潮汐仪 → 巨型肋拱、深海珍珠。真实密集 NBT 配方坐标剖视；滑动查看所有深度。Boss z22 除中央屏障开口外为封顶隔墙。</p>${cards}<script>${script}</script></html>`);
}

function main(){
 const changedKeys=new Set(['spawn/spawn','boss/eclipse_keeper']);
 const baselinePath=path.join(root,'output/rooms-team3-ceremony-baseline.json');
 fs.mkdirSync(path.dirname(baselinePath),{recursive:true});
 if(!fs.existsSync(baselinePath)){
  const baseline={};
  for(const style of Object.keys(themes)){
   baseline[style]={};
   for(const variant of variants){
    const bytes=fs.readFileSync(path.join(data,'roomsets',style,variant.key+'.nbt'));
    const old=Structure.fromNbt(NbtFile.read(bytes).root);
    const entry={sha256:sha(bytes)};
    if(changedKeys.has(variant.key)){
     let occupancy='';
     for(let y=0;y<variant.size[1];y++)for(let z=0;z<variant.size[2];z++)for(let x=0;x<variant.size[0];x++)occupancy+=old.getBlock([x,y,z])?.state.toString()===air?'0':'1';
     entry.occupancy=occupancy;
    }
    baseline[style][variant.key]=entry;
   }
  }
  fs.writeFileSync(baselinePath,JSON.stringify(baseline,null,2)+'\n');
 }
 const baseline=JSON.parse(fs.readFileSync(baselinePath,'utf8'));
 const revision={schema:1,changedTemplates:[],unchangedTemplates:[],checks:'PASS'};
 const report={schema:1,dataVersion:3465,countPerStyle:variants.length,notes:['Only stair/elevated-platform walk-surface skeletons are extracted; all walls, ceilings, furniture and thematic geometry rebuilt.','Offline connectivity uses supported three-air cells and cardinal moves with maximum one-block elevation changes; not an in-game navigation simulation.','Coral contains no water or live coral. Dense explicit air, no entities or block entities.'],styles:{},geometryDifferences:[]}, cache={};
 for(const style of Object.keys(themes)){
  const templates=[];report.styles[style]=[];cache[style]=[];
  for(const variant of variants){
   const built=room(style,variant),checks=validate(built,variant),bytes=structure(built).writeNbt({name:'',dataVersion:3465,compression:'gzip'});
   const nbt=NbtFile.read(bytes).root,roundtrip=Structure.fromNbt(nbt);if(nbt.getNumber('DataVersion')!==3465||!nbt.hasList('entities',undefined,0)||roundtrip.getBlocks().length!==built.states.length||roundtrip.getBlocks().some(block=>block.nbt))throw Error('Roundtrip');
   const paletteChecks=validatePalette(nbt,built);
   const baselineEntry=baseline[style][variant.key];
   if(!changedKeys.has(variant.key)){
    if(sha(bytes)!==baselineEntry.sha256)throw Error(`Protected template changed ${style}/${variant.key}`);
    revision.unchangedTemplates.push({style,key:variant.key,sha256:sha(bytes),byteIdentical:true});
   }else{
    const occupancy=built.states.map(state=>state===air?'0':'1').join('');
    const changedCoordinates=[...occupancy].reduce((sum,value,index)=>sum+Number(value!==baselineEntry.occupancy[index]),0);
    if(!changedCoordinates)throw Error('Missing ceremony geometry change');
    revision.changedTemplates.push({style,key:variant.key,oldSha256:baselineEntry.sha256,newSha256:sha(bytes),differentSolidAirCoordinates:changedCoordinates,...checks,...paletteChecks});
   }
   const destination=path.join(data,'roomsets',style,variant.key+'.nbt');fs.mkdirSync(path.dirname(destination),{recursive:true});
   if(!fs.existsSync(destination)||sha(fs.readFileSync(destination))!==sha(bytes))fs.writeFileSync(destination,bytes);
   const original=Structure.fromNbt(NbtFile.read(fs.readFileSync(path.join(data,'structures',variant.key+'.nbt'))).root);
   let differentFromOriginal=0;for(let y=0;y<built.size[1];y++)for(let z=0;z<built.size[2];z++)for(let x=0;x<built.size[0];x++)if((built.get(x,y,z)===air)!==(!original.getBlock([x,y,z])||original.getBlock([x,y,z]).state.toString()===air))differentFromOriginal++;
   templates.push({key:variant.key,sha256:sha(bytes)});report.styles[style].push({key:variant.key,size:variant.size,minimum_y:variant.minimum_y,ports:variant.ports,socketsInterface:variant.sockets,theme:built.theme,sha256:sha(bytes),differentSolidAirFromOriginal:differentFromOriginal,...checks,...paletteChecks});cache[style].push({variant,built});
  }
  const occupancySignatures=cache[style].map(({built})=>sha(built.states.map(state=>state===air?'0':'1').join('')));
  const uniqueOccupancy=new Set(occupancySignatures).size;
  if(uniqueOccupancy!==variants.length||report.styles[style].filter(entry=>entry.differentSolidAirFromOriginal>0).length<20)throw Error('Insufficient geometry diversity');
  report[style+'Summary']={templates:templates.length,uniqueOccupancy,changedFromOriginal:report.styles[style].filter(entry=>entry.differentSolidAirFromOriginal>0).length,occupancySignatures};
  fs.writeFileSync(path.join(data,'roomsets',style,'manifest.json'),JSON.stringify({schema:1,style:`fteclipsegen:${style}`,templates},null,2)+'\n');
 }
 for(let index=0;index<variants.length;index++){const left=cache.swamp[index].built.states,right=cache.coral[index].built.states;const count=left.reduce((sum,state,offset)=>sum+Number((state===air)!==(right[offset]===air)),0);if(!count)throw Error('Palette-only '+variants[index].key);report.geometryDifferences.push({key:variants[index].key,differentSolidAirCoordinates:count});}
 fs.mkdirSync(path.join(root,'output'),{recursive:true});fs.writeFileSync(path.join(root,'output/rooms-team3-report.json'),JSON.stringify(report,null,2)+'\n');
 revision.unchangedCount=revision.unchangedTemplates.length;
 revision.manifests=Object.keys(themes).map(style=>({style,sha256:sha(fs.readFileSync(path.join(data,'roomsets',style,'manifest.json')))}));
 fs.writeFileSync(path.join(root,'output/rooms-team3-ceremony-report.json'),JSON.stringify(revision,null,2)+'\n');
 ceremonialPreview(cache);
 const cards=Object.entries(cache).flatMap(([style,entries])=>entries.map(({variant,built})=>{let tiles='';for(let x=0;x<built.size[0];x++)for(let z=0;z<built.size[2];z++)tiles+=`<rect x="${x*6}" y="${z*6}" width="6" height="6" fill="${built.get(x,built.base+1,z)===air?(style==='swamp'?'#756449':'#d8e5dc'):(style==='swamp'?'#3e4834':'#246675')}"/>`;for(const socket of variant.sockets)if(socket.position[1]===built.base)tiles+=`<circle cx="${socket.position[0]*6+3}" cy="${socket.position[2]*6+3}" r="2" fill="#ef5360"/>`;return `<article><h3>${style} · ${variant.key}</h3><p>${built.theme} · ${built.size.join('×')}</p><svg viewBox="0 0 ${built.size[0]*6} ${built.size[2]*6}">${tiles}</svg></article>`;})).join('');
 fs.writeFileSync(path.join(root,'output/rooms-team3-gallery.html'),`<!doctype html><html lang="zh"><meta charset="utf-8"><title>幽沼 / 珊瑚 · 平面检查</title><style>body{background:#102029;color:#e8f2ef;font:14px system-ui;margin:32px}main{display:grid;grid-template-columns:repeat(auto-fill,minmax(210px,1fr));gap:16px}article{background:#20343b;padding:16px;border-radius:12px}h3{font-size:13px}svg{width:100%;max-height:220px}p{color:#adbfba}</style><h1>幽沼 / 珊瑚 · 54 模板</h1><p>脚部上方一格截面；红点为同层socket。楼梯立体检查使用Lodestone预览。</p><main>${cards}</main></html>`);
 console.log(JSON.stringify({templates:54,minimumGeometryDifference:Math.min(...report.geometryDifferences.map(entry=>entry.differentSolidAirCoordinates))}));
}
export {room, validate};
if(process.argv[1]&&path.resolve(process.argv[1])===fileURLToPath(import.meta.url))main();
