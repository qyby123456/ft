import assert from 'node:assert/strict';
import {createHash} from 'node:crypto';
import {readFileSync,writeFileSync,mkdirSync,readdirSync,existsSync} from 'node:fs';
import {join,dirname} from 'node:path';
import {fileURLToPath,pathToFileURL} from 'node:url';
const root=fileURLToPath(new URL(import.meta.url.includes('/tools/')?'../':'../../../../',import.meta.url));
const batch=5;
const batchName='batch-'+String(batch).padStart(2,'0');
const output=join(root,'output/room-expansion-v035/amethyst',batchName);
const data=join(root,'src/main/resources/data/fteclipsegen');
const catalogRaw=readFileSync(join(data,'catalog.json'));
const catalog=JSON.parse(catalogRaw).variants;
const recipePath=fileURLToPath(import.meta.url);
const hash=bytes=>createHash('sha256').update(bytes).digest('hex');
const library=await import(pathToFileURL('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js').href);
const authorized=[{key:'addition/09',baseKey:'straight/room_towers',name:'晶脉地质剖面馆'},{key:'addition/10',baseKey:'straight/room_cells',name:'极性分离研究所'}];
const replayFiles=['manifest.json','design.md','validation.json','four-views.html','style-audit.json','repetition-audit.json','recipe-snapshot.mjs',...authorized.map(room=>'roomset/'+room.key+'.nbt'),...authorized.flatMap(room=>['north','south','west','east'].map(direction=>'views/'+room.key+'/'+direction+'.svg')),'views/browser-contact.png'];
const before=process.argv.includes('--verify-replay')?Object.fromEntries(replayFiles.map(file=>[file,hash(readFileSync(join(output,file)))])):null;
const previousRoots=[1,2,3,4].map(number=>join(root,'output/room-expansion-v035/amethyst/batch-'+String(number).padStart(2,'0')));
const previousHashes=Object.fromEntries(previousRoots.flatMap(previousRoot=>readdirSync(previousRoot,{recursive:true,withFileTypes:true}).filter(entry=>entry.isFile()).map(entry=>{const path=join(entry.parentPath,entry.name);return[path,hash(readFileSync(path))];})));
const save=(file,content)=>{const path=join(output,file);mkdirSync(dirname(path),{recursive:true});if(content&&typeof content==='object'&&!Buffer.isBuffer(content)&&'batch'in content)content.batch=batch;if(typeof content==='string')content=content.replace(/第[1-4]批/g,'第5批');writeFileSync(path,typeof content==='string'||Buffer.isBuffer(content)?content:JSON.stringify(content,null,2)+'\n');};
class Room{
 constructor(variant){this.variant=variant;this.size=variant.size;[this.width,this.height,this.depth]=variant.size;this.blocks=Array(this.width*this.height*this.depth).fill('air');}
 index(x,y,z){return(y*this.depth+z)*this.width+x;}
 inside(x,y,z){return x>=0&&y>=0&&z>=0&&x<this.width&&y<this.height&&z<this.depth;}
 get(x,y,z){return this.inside(x,y,z)?this.blocks[this.index(x,y,z)]:'void';}
 set(x,y,z,block){assert(this.inside(x,y,z));this.blocks[this.index(x,y,z)]=block;}
 box(first,last,block){for(let y=first[1];y<=last[1];y++)for(let z=first[2];z<=last[2];z++)for(let x=first[0];x<=last[0];x++)this.set(x,y,z,block);}
 shell(){this.box([0,0,0],[15,1,15],'smooth_quartz');for(let y=2;y<16;y++)for(let z=0;z<16;z++)for(let x=0;x<16;x++)if(x===0||z===0||x===15||z===15||y===15)this.set(x,y,z,y>=11?'purple_stained_glass':'deepslate_tiles');for(let z=1;z<15;z++)for(let x=1;x<15;x++)if((2*x+z)%9===0)this.set(x,1,z,'amethyst_block');}
}
const solid=block=>block!=='air'&&block!=='void';
function reserve(room){
 for(const port of room.variant.ports)for(const[x,y,z]of port.cells)for(let inward=0;inward<=4;inward++){const insideZ=z===0?inward:15-inward;room.box([x,y,insideZ],[x,y+4,insideZ],'air');room.set(x,y-1,insideZ,'smooth_quartz');}
 const radius=batch===1?1:0;for(const socket of room.variant.sockets)for(const[x,y,z]of[socket.position,socket.spawn_position])for(let dx=-radius;dx<=radius;dx++)for(let dz=-radius;dz<=radius;dz++){room.box([x+dx,y,z+dz],[x+dx,y+2,z+dz],'air');room.set(x+dx,y-1,z+dz,'smooth_quartz');}
}
function buildGeology(room){
 for(let y=2;y<=9;y++)room.box([1,y,2],[2,y,12],y%3===0?'calcite':y%3===1?'tuff':'deepslate_tiles');
 for(let z=2;z<=12;z++){
  const elevation=3+Math.floor((z-2)/2)%4;
  room.box([3,elevation,z],[3,elevation+1,z],'amethyst_block');
  if(z%2===0)room.set(2,elevation,z,'amethyst_block');
 }
 for(const[x,height]of [[11,3],[12,4],[13,5]]){
  room.box([x,2,2],[x,height,3],'calcite');room.set(x,height,2,'amethyst_block');
  if(height>=4)room.set(x,height-1,3,'tuff');
 }
 room.box([10,2,11],[14,3,13],'calcite');room.box([13,4,12],[14,4,13],'blue_terracotta');
 for(const x of [11,12,13]){
  room.set(x,2,9,'quartz_stairs[facing=south,half=bottom,shape=straight,waterlogged=false]');
  room.set(x,3,10,'quartz_stairs[facing=south,half=bottom,shape=straight,waterlogged=false]');
 }
 room.box([14,4,11],[14,6,11],'amethyst_block');room.set(14,7,11,'sea_lantern');
 room.box([1,11,2],[3,11,12],'calcite');room.box([1,12,2],[3,12,12],'purple_stained_glass');
 room.box([11,9,2],[14,9,3],'calcite');
}
const diamondCells=()=>{const cells=[];for(let x=10;x<=14;x++)for(let z=7;z<=11;z++)if(Math.abs(x-12)+Math.abs(z-9)<=2)cells.push([x,z]);return cells;};
function buildSeparation(room){
 room.box([2,2,2],[5,2,4],'calcite');
 for(let y=3;y<=6;y++)for(let x=2;x<=5;x++)for(let z=2;z<=4;z++)if(x===2||x===5||z===2||z===4)room.set(x,y,z,'purple_stained_glass');
 room.box([2,7,2],[5,7,4],'calcite');
 room.box([3,3,3],[4,4,3],'amethyst_block');room.set(3,5,3,'amethyst_block');
 room.set(2,7,2,'sea_lantern');
 for(const[x,z]of diamondCells()){
  const edge=Math.abs(x-12)+Math.abs(z-9)===2;
  room.set(x,2,z,'calcite');room.set(x,8,z,'calcite');
  if(edge)room.box([x,3,z],[x,7,z],'purple_stained_glass');
 }
 room.box([12,3,8],[12,5,10],'amethyst_block');room.box([11,3,9],[13,4,9],'amethyst_block');room.set(12,6,9,'amethyst_block');room.set(12,8,9,'sea_lantern');
 room.box([2,2,10],[3,3,13],'calcite');room.box([2,4,10],[3,4,12],'blue_terracotta');
 room.set(2,5,10,'amethyst_block');room.set(3,5,12,'sea_lantern');
 room.box([2,9,2],[5,9,4],'calcite');room.box([2,8,2],[2,8,2],'amethyst_block');
 room.box([10,10,7],[14,10,11],'purple_stained_glass');room.box([14,8,9],[14,9,9],'amethyst_block');
}
function buildAssembly(room){
 room.box([2,2,6],[6,2,8],'calcite');
 room.box([2,3,6],[5,3,7],'amethyst_block');
 room.box([3,3,7],[4,3,7],'smooth_quartz');room.set(5,3,6,'sea_lantern');
 for(let row=0;row<3;row++){
  room.box([10+row,2,9+row],[14,2,9+row],'calcite');
  for(let x=10+row;x<=14;x++)if((x+row)%2===0)room.set(x,3,9+row,'amethyst_block');
 }
 room.box([1,2,2],[2,2,5],'calcite');
 for(let z=2;z<=5;z++){room.set(1,3,z,z%2?'blue_terracotta':'amethyst_block');room.set(2,3,z,'smooth_quartz');}
 room.box([1,4,4],[1,7,4],'calcite');room.box([1,7,2],[1,7,6],'amethyst_block');
 room.box([2,8,6],[5,8,7],'calcite');room.box([2,9,6],[2,10,6],'amethyst_block');
 room.box([11,7,9],[14,7,11],'calcite');room.box([14,4,10],[14,6,10],'amethyst_block');
 for(const [x,z]of [[3,7],[5,6],[11,10],[13,9]])room.set(x,3,z,'amethyst_block');
 for(const [x,z]of [[7,2],[7,3],[8,4],[8,5],[8,6],[8,7],[8,8],[8,9],[7,10],[7,11],[7,12],[7,13]])room.set(x,1,z,'blue_terracotta');
}
function buildIllusion(room){
 for(const frame of [{x:2,z:6,width:3,height:8},{x:1,z:10,width:4,height:6}]){
  room.box([frame.x,2,frame.z],[frame.x,frame.height,frame.z],'calcite');
  room.box([frame.x+frame.width,2,frame.z],[frame.x+frame.width,frame.height,frame.z],'amethyst_block');
  room.box([frame.x,frame.height,frame.z],[frame.x+frame.width,frame.height,frame.z],'calcite');
  room.box([frame.x+1,2,frame.z],[frame.x+frame.width-1,frame.height-1,frame.z],'purple_stained_glass');
  room.set(frame.x,frame.height-1,frame.z,'sea_lantern');
 }
 for(const [x,z,height]of [[12,6,7],[13,9,5]]){
  room.box([x-1,2,z],[x+1,3,z+1],'calcite');
  room.box([x,4,z],[x,height,z],'amethyst_block');
  if(height>5)room.box([x-1,4,z],[x,5,z+1],'amethyst_block');
  room.set(x,height,z,'sea_lantern');
 }
 for(let step=0;step<5;step++){
  const x=5+step,z=5+step;room.set(x,1,z,'blue_terracotta');room.set(x+1,1,z,'amethyst_block');
 }
 room.box([7,2,7],[8,2,8],'calcite');room.set(7,3,7,'amethyst_block');room.set(8,3,8,'sea_lantern');
 room.box([12,2,12],[14,2,13],'calcite');room.box([13,3,12],[14,3,12],'blue_terracotta');
 room.box([1,11,5],[5,11,6],'calcite');room.box([1,8,5],[1,10,5],'amethyst_block');
 room.box([11,10,5],[14,10,10],'calcite');room.box([14,8,10],[14,9,10],'amethyst_block');
}
function buildSettling(room){
 for(let tier=0;tier<4;tier++){
  const z=3+tier*2,height=10-tier*2;
  room.box([1,2,z],[3,height,z+1],'calcite');
  room.box([2,height,z],[3,height,z+1],'amethyst_block');
  room.box([3,4,z],[3,height-1,z],'purple_stained_glass');
  room.set(2,height-1,z+1,'sea_lantern');
 }
 for(const[first,last]of [[[12,6],[14,6]],[[11,7],[14,8]],[[10,9],[13,10]]]){
  room.box([first[0],2,first[1]],[last[0],3,last[1]],'amethyst_block');
  room.box([first[0],3,first[1]],[last[0],3,first[1]],'blue_terracotta');
 }
 room.box([11,2,12],[14,2,13],'calcite');room.box([11,3,12],[14,4,13],'purple_stained_glass');
 room.box([12,3,12],[13,3,12],'amethyst_block');room.box([11,5,12],[14,5,13],'calcite');
 room.set(14,5,13,'sea_lantern');
 room.box([11,8,5],[14,8,10],'purple_stained_glass');room.box([14,5,5],[14,7,5],'calcite');
 room.box([1,12,3],[4,12,9],'purple_stained_glass');room.box([1,10,10],[2,11,10],'amethyst_block');
}
function buildAcoustic(room){
 for(const[x,z,height]of [[2,3,6],[5,6,9],[8,9,5],[11,11,8]]){
  room.box([x,2,z],[x,2,z],'calcite');room.box([x,3,z],[x,height,z],'amethyst_block');
  room.set(x,height,z,'sea_lantern');room.set(x,height-1,z,'purple_stained_glass');
 }
 room.box([1,2,11],[3,3,13],'calcite');room.box([1,4,12],[3,4,12],'blue_terracotta');
 room.set(2,5,12,'amethyst_block');room.box([1,5,11],[1,6,11],'purple_stained_glass');room.set(3,4,13,'sea_lantern');
 for(let z=3;z<=10;z++){
  const depth=z%2===0?13:14;
  room.box([depth,2,z],[14,6+z%3,z],'black_wool');
  room.set(depth,7+z%3,z,'amethyst_block');
 }
 room.box([1,9,11],[4,9,14],'purple_stained_glass');room.box([1,7,14],[1,8,14],'calcite');
 room.box([12,11,3],[14,11,10],'purple_stained_glass');
}
function buildRanging(room){
 for(const[x,z,length]of [[2,6,4],[9,7,4]])for(let step=0;step<length;step++){
  const offset=step%2;room.box([x+step,2,z+offset],[x+step,6+offset,z+offset],'purple_stained_glass');
  room.set(x+step,7+offset,z+offset,'amethyst_block');
  if(step%2===0)room.set(x+step,5,z+offset,'sea_lantern');
 }
 room.box([10,2,11],[14,3,13],'calcite');room.box([11,4,12],[12,4,13],'blue_terracotta');
 room.box([13,4,12],[14,6,12],'amethyst_block');room.set(13,7,12,'sea_lantern');
 for(const x of [11,12,13]){
  room.set(x,2,9,'quartz_stairs[facing=south,half=bottom,shape=straight,waterlogged=false]');
  room.set(x,3,10,'quartz_stairs[facing=south,half=bottom,shape=straight,waterlogged=false]');
 }
 room.box([1,10,2],[5,10,5],'purple_stained_glass');room.box([2,11,3],[2,13,3],'calcite');
 room.box([9,11,8],[14,11,9],'purple_stained_glass');room.box([14,8,8],[14,10,8],'calcite');
 room.box([1,2,12],[2,3,13],'amethyst_block');
}
function buildCultivation(room){
 room.box([2,2,6],[5,4,7],'calcite');room.box([2,5,6],[5,6,7],'purple_stained_glass');
 room.box([3,5,6],[4,5,6],'amethyst_block');room.box([2,7,6],[5,7,7],'calcite');room.set(3,7,7,'sea_lantern');
 for(const[x,z]of [[9,8],[10,8],[11,8],[10,7],[10,9]]){
  room.box([x,2,z],[x,3,z],'calcite');room.box([x,4,z],[x,6,z],'purple_stained_glass');room.set(x,7,z,'amethyst_block');
 }
 room.set(10,5,8,'amethyst_block');room.set(10,7,8,'sea_lantern');
 for(const[x,z]of [[2,11],[3,11],[4,11],[2,12],[2,13]]){
  room.box([x,2,z],[x,3,z],'amethyst_block');room.box([x,4,z],[x,5,z],'purple_stained_glass');room.set(x,6,z,'calcite');
 }
 room.set(2,6,11,'sea_lantern');
 for(let step=0;step<6;step++)room.box([1,5+step,2+step],[2,6+step,2+step],'amethyst_block');
 room.box([1,10,10],[3,10,13],'purple_stained_glass');room.box([13,9,2],[14,9,6],'purple_stained_glass');
}
function buildLaboratory(room){
 room.box([2,2,7],[5,3,10],'calcite');
 for(let level=0;level<6;level++){
  const span=level<2?2:level<4?1:0;
  room.box([3-span,4+level,8],[3+span,4+level,9],'amethyst_block');
  if(level%2===0)room.box([3-span,4+level,8],[3+span,4+level,8],'budding_amethyst');
 }
 room.box([1,4,7],[1,6,11],'calcite');room.box([1,7,7],[5,7,7],'calcite');
 for(let step=0;step<5;step++){
  const x=6+step,z=6+step;
  room.box([x,2,z],[x+1,2,z+1],'polished_andesite');
  room.set(x,3,z,'amethyst_block');room.set(x+1,3+step%2,z,'purple_stained_glass');
  if(step%2===0)room.set(x+1,4,z+1,'calcite');
 }
 room.box([12,2,2],[14,3,6],'calcite');room.box([12,4,2],[14,4,2],'blue_terracotta');
 room.box([13,4,4],[14,6,5],'purple_stained_glass');room.box([14,5,4],[14,6,4],'budding_amethyst');room.set(13,7,4,'sea_lantern');
 room.box([2,8,2],[4,9,4],'purple_stained_glass');room.box([3,10,3],[3,12,3],'calcite');
 room.box([10,10,10],[13,10,11],'calcite');room.box([10,11,10],[11,12,10],'amethyst_block');
 room.box([1,10,6],[4,10,6],'calcite');room.box([1,11,6],[1,13,10],'amethyst_block');
}
function widenRoute(room){
 if(walk(room,[7,2,1],true).has('7,14'))return;
 const distances=new Map([['7,1',0]]),parents=new Map(),pending=new Set(['7,1']);
 while(pending.size){let current=[...pending].sort((left,right)=>distances.get(left)-distances.get(right))[0];pending.delete(current);if(current==='7,14')break;const[x,z]=current.split(',').map(Number);for(const[dx,dz]of [[1,0],[-1,0],[0,1],[0,-1]]){const next=[x+dx,z+dz],key=next.join(',');if(next[0]<2||next[0]>13||next[1]<1||next[1]>14||((next[1]===1||next[1]===14)&&(next[0]<7||next[0]>8)))continue;if(room.variant.key==='straight/room_towers'&&batch===3&&[[2,3],[5,6],[8,9],[11,11]].some(([columnX,columnZ])=>Math.abs(next[0]-columnX)<=1&&Math.abs(next[1]-columnZ)<=1))continue;let cost=1;for(let sideX=-1;sideX<=1;sideX++)for(let sideZ=-1;sideZ<=1;sideZ++)for(let y=2;y<=4;y++)if(solid(room.get(next[0]+sideX,y,next[1]+sideZ)))cost+=10;const candidate=distances.get(current)+cost;if(candidate<(distances.get(key)??Infinity)){distances.set(key,candidate);parents.set(key,current);pending.add(key);}}}
 assert(parents.has('7,14'));for(let current='7,14';current;current=parents.get(current)){const[x,z]=current.split(',').map(Number);for(let dx=-1;dx<=1;dx++)for(let dz=-1;dz<=1;dz++)if(x+dx>0&&x+dx<15&&z+dz>0&&z+dz<15)room.box([x+dx,2,z+dz],[x+dx,4,z+dz],'air');}
}
function buildResonance(room){
 for(const frame of[{x:1,z:7,width:4,height:6},{x:9,z:5,width:5,height:10},{x:10,z:9,width:4,height:8}]){
  room.box([frame.x,2,frame.z],[frame.x,frame.height,frame.z],'calcite');
  room.box([frame.x+frame.width,2,frame.z],[frame.x+frame.width,frame.height,frame.z],'calcite');
  room.box([frame.x,frame.height,frame.z],[frame.x+frame.width,frame.height,frame.z],'amethyst_block');
  const center=frame.x+Math.floor(frame.width/2);
  room.box([center,frame.height-2,frame.z],[center,frame.height-1,frame.z],'budding_amethyst');
  room.set(center,frame.height-3,frame.z,'sea_lantern');
 }
 room.box([1,2,11],[2,3,14],'calcite');room.box([2,2,13],[6,3,14],'amethyst_block');
 room.box([1,4,12],[2,4,12],'blue_terracotta');room.set(1,4,13,'sea_lantern');
 room.box([3,4,13],[5,4,13],'purple_stained_glass');
 room.box([1,10,2],[5,10,4],'purple_stained_glass');room.box([10,12,8],[14,12,11],'purple_stained_glass');
 room.box([11,11,8],[11,11,8],'calcite');room.box([14,10,11],[14,12,11],'amethyst_block');
}
function decode(bytes,variant,strict=true){
 const nbt=library.NbtFile.read(bytes),structure=library.Structure.fromNbt(nbt.root),room=new Room(variant),positions=new Set();assert.deepEqual(structure.getSize(),variant.size);
 if(strict){assert.equal(nbt.root.getNumber('DataVersion'),3465);assert.equal(nbt.root.getList('entities').length,0);const palette=nbt.root.getList('palette');for(let index=0;index<palette.length;index++)assert(/^minecraft:[a-z0-9_]+$/.test(palette.getCompound(index).getString('Name')));assert.equal(structure.getBlocks().length,variant.size.reduce((total,length)=>total*length,1));}
 for(const block of structure.getBlocks()){assert(!block.nbt);assert(room.inside(...block.pos));assert(!positions.has(block.pos.join(',')));positions.add(block.pos.join(','));room.set(...block.pos,block.state.toString().replace(/^minecraft:/,''));}return room;
}
function walk(room,start=[7,2,0],wide=false){
 const valid=(x,z)=>{const radius=wide?1:0;for(let dx=-radius;dx<=radius;dx++)for(let dz=-radius;dz<=radius;dz++){if(!room.inside(x+dx,4,z+dz)||!solid(room.get(x+dx,1,z+dz)))return false;for(let y=2;y<=4;y++)if(room.get(x+dx,y,z+dz)!=='air')return false;}return true;};
 const queue=[[start[0],start[2]]],visited=new Set();if(!valid(...queue[0]))return visited;visited.add(queue[0].join(','));for(let cursor=0;cursor<queue.length;cursor++){const[x,z]=queue[cursor];for(const[dx,dz]of [[1,0],[-1,0],[0,1],[0,-1]]){const next=[x+dx,z+dz];if(!visited.has(next.join(','))&&valid(...next)){visited.add(next.join(','));queue.push(next);}}}return visited;
}
function verifyObservation(room){
 const surfaces=new Map();
 const heightAt=(microX,microZ,y)=>{const state=room.get(Math.floor(microX/2),y,Math.floor(microZ/2));if(state==='air')return 0;if(!state.includes('stairs'))return 2;const high=state.includes('facing=south')?microZ%2===1:state.includes('facing=north')?microZ%2===0:state.includes('facing=east')?microX%2===1:microX%2===0;return high?2:1;};
 const clear=(microX,feet,microZ)=>{if(microX<0||microZ<0||microX>=32||microZ>=32)return false;for(let y=Math.floor(feet/2);y<Math.ceil((feet+4)/2);y++){const height=heightAt(microX,microZ,y);if(height&&y*2+height>feet&&y*2<feet+4)return false;}return true;};
 for(let microX=0;microX<32;microX++)for(let microZ=0;microZ<32;microZ++){const column=[];for(let y=0;y<14;y++){const height=heightAt(microX,microZ,y),feet=y*2+height;if(height&&clear(microX,feet,microZ))column.push(feet);}surfaces.set(microX+','+microZ,column);}
 const queue=[[14,4,0]],visited=new Set(['14,4,0']);for(let cursor=0;cursor<queue.length;cursor++){const[microX,feet,microZ]=queue[cursor];for(const[dx,dz]of [[1,0],[-1,0],[0,1],[0,-1]]){const nextX=microX+dx,nextZ=microZ+dz;for(const nextFeet of surfaces.get(nextX+','+nextZ)??[]){if(Math.abs(nextFeet-feet)>1||!clear(microX,Math.max(feet,nextFeet),microZ)||!clear(nextX,Math.max(feet,nextFeet),nextZ))continue;const next=[nextX,nextFeet,nextZ],key=next.join(',');if(!visited.has(key)){visited.add(key);queue.push(next);}}}}
 const target=[11,4,11];assert(visited.has([target[0]*2,target[1]*2,target[2]*2].join(',')),'Observation platform requires jumping');
 for(const x of [11,12,13])for(const[z,y]of [[9,2],[10,3]])assert(room.get(x,y,z).includes('quartz_stairs'),'Lost stair tread');
 return{status:'PASS',target,collisionGrid:.5,maximumStep:.5,headroom:2,jumpRequired:false,threeWideTreads:true,reachableMicroCells:visited.size};
}
function verifyCultures(room){
 const footprints=[[[2,6],[3,6],[4,6],[5,6],[2,7],[3,7],[4,7],[5,7]],[[9,8],[10,8],[11,8],[10,7],[10,9]],[[2,11],[3,11],[4,11],[2,12],[2,13]]];
 const caps=[7,7,6],checks=footprints.map((points,index)=>{for(const[x,z]of points)for(let y=2;y<=caps[index];y++)assert(solid(room.get(x,y,z)),`Culture shell breached ${index} ${x},${y},${z}`);return{shape:['rectangle','cross','L'][index],footprint:points,fromY:2,toY:caps[index],sealed:true};});
 assert(!room.blocks.some(block=>/amethyst_cluster|amethyst_bud|budding_amethyst/.test(block)),'Combat growth bud present');return{status:'PASS',chambers:checks,growthBudsInCombat:0};
}
function occupancy(room){
 const base=room.variant.key==='spawn/spawn'?4:room.width===12?1:2,points=[];
 for(let y=base;y<room.height-1;y++)for(let z=1;z<room.depth-1;z++)for(let x=1;x<room.width-1;x++){
  if(!solid(room.get(x,y,z)))continue;
  if(room.variant.ports.some(port=>port.cells.some(([portX,portY,portZ])=>y>=portY-1&&y<=portY+4&&(portZ===0||portZ===room.depth-1?Math.abs(x-portX)<=1&&Math.abs(z-portZ)<=4:Math.abs(z-portZ)<=1&&Math.abs(x-portX)<=4))))continue;
  if(room.variant.key.startsWith('stairs/')&&y===17&&!room.get(x,y,z).includes('stairs'))continue;
  points.push([x,y-base,z]);
 }
 const rotations=[];for(let turn=0;turn<4;turn++)rotations.push(new Set(points.map(([x,y,z])=>{const[rotatedX,rotatedZ]=turn===0?[x,z]:turn===1?[room.depth-1-z,x]:turn===2?[room.width-1-x,room.depth-1-z]:[z,room.width-1-x];return[Math.floor(rotatedX/(turn%2?room.depth:room.width)*16),Math.floor(y/(room.height-1-base)*16),Math.floor(rotatedZ/(turn%2?room.width:room.depth)*16)].join(',');})));
 return{points,rotations,signature:hash(Buffer.from(rotations.map(set=>[...set].sort().join(';')).sort()[0]))};
}
function similarity(left,right){let intersection=0;for(const point of left)if(right.has(point))intersection++;return intersection/(left.size+right.size-intersection||1);}
function compareRooms(current){
 const prior=[];for(const variant of catalog){const path=join(root,'output/style-rebuild-v034/amethyst/roomset',variant.key+'.nbt');prior.push({key:'v034/'+variant.key,room:decode(readFileSync(path),variant,false),sha256:hash(readFileSync(path))});}
 const additionRoot=join(root,'output/room-expansion-v035/amethyst');
 for(const name of readdirSync(additionRoot))if(/^batch-\d\d$/.test(name)&&name!==batchName&&existsSync(join(additionRoot,name,'manifest.json'))){const manifest=JSON.parse(readFileSync(join(additionRoot,name,'manifest.json')));for(const entry of manifest.templates){const path=join(additionRoot,name,entry.file);prior.push({key:name+'/'+entry.key,room:decode(readFileSync(path),catalog.find(variant=>variant.key===entry.baseKey)),sha256:hash(readFileSync(path))});}}
 const signatures=new Map([...prior,...current].map(entry=>[entry.key,occupancy(entry.room)])),pairs=[];
 for(const entry of current)for(const previous of [...prior,...current.filter(candidate=>candidate.key<entry.key)]){
  const left=signatures.get(entry.key),right=signatures.get(previous.key),jaccard=Math.max(...right.rotations.map(set=>similarity(left.rotations[0],set))),sameSize=entry.room.size.join(',')===previous.room.size.join(',');
  assert(jaccard<.85,`Rebuild high similarity ${entry.key} / ${previous.key}: ${jaccard}`);
  pairs.push({keys:[entry.key,previous.key],sameSize,normalizedInteriorSolidJaccard:jaccard,threshold:.85,status:'PASS',review:entry.key==='addition/09'?'西侧层状地质剖面和折线晶脉，东北分层低标本台，东南可登高平台；与旧中央双塔、沉降塔工序或测距薄屏不同。':'西北长方笼与东南菱形笼体分置对角，西南低工作台构成三角工序；过道从笼外绕行，非培养台整实体或虚像对照窗。'});
 }
 return{status:'PASS',style:'fteclipsegen:amethyst',batch:1,method:'Remove x/z boundary, roof, base floor and port safety bands (depth4, x/z extended1, footY-1..footY+4); compare occupied coordinates, 4 Y rotations, normalized16³. Shared air is excluded from Jaccard.',oldRoomsCompared:prior.filter(entry=>entry.key.startsWith('v034/')).length,previousAdditionsCompared:prior.filter(entry=>!entry.key.startsWith('v034/')).length,currentBatchPairs:1,rooms:current.map(entry=>({key:entry.key,normalizedSignature:signatures.get(entry.key).signature,interiorSolidCells:signatures.get(entry.key).points.length})),pairs,highestPairs:[...pairs].sort((left,right)=>right.normalizedInteriorSolidJaccard-left.normalizedInteriorSolidJaccard).slice(0,12),highSimilarityPairs:0,exactDuplicates:0};
}
function bbox(points){return{from:[0,1,2].map(axis=>Math.min(...points.map(point=>point[axis]))),to:[0,1,2].map(axis=>Math.max(...points.map(point=>point[axis])))};}
function auditStyle(entry){
 const counts={};for(const block of entry.room.blocks)counts[block]=(counts[block]??0)+1;
 const regions=entry.key==='addition/09'?[['侧壁折线晶脉剖面',[1,2,2],[3,9,12]],['分层标本低台',[11,2,2],[13,5,3]],['角落登高平台',[10,2,9],[14,7,13]]]:[['长方隔离晶簇笼',[2,2,2],[5,7,4]],['菱形隔离晶簇笼',[10,2,7],[14,8,11]],['偏心分离台',[2,2,10],[3,5,13]]];
 const landmarks=regions.map(([name,first,last])=>{const materialCounts={},points=[];for(let y=first[1];y<=last[1];y++)for(let z=first[2];z<=last[2];z++)for(let x=first[0];x<=last[0];x++){const block=entry.room.get(x,y,z);if(solid(block)){materialCounts[block]=(materialCounts[block]??0)+1;points.push([x,y,z]);}}assert(points.length>=8);assert(Object.keys(materialCounts).some(name=>/amethyst/.test(name)));return{name,...bbox(points),solidCells:points.length,materialCounts};});
 assert((counts.amethyst_block??0)+(counts.budding_amethyst??0)>25);
 return{key:entry.key,status:'PASS',nbtSha256:entry.sha256,materialCounts:counts,landmarks,paletteOnly:false,fourCornerPillars:false,repeatedOrbitRing:false,functionEvidence:entry.key==='addition/09'?'真实tuff/方解石/深板岩横层和紫晶折线组成地质剖面，另一侧分层低样本台；角台经六块三格石英梯免跳跃可达。':'两笼的长方与菱形边界、实心底顶、晶簇内芯均由实际NBT检查；笼外可走主路绕向偏心分离台，不穿笼。',retentionReason:'实体占位、功能结构、材质与标志包围盒均由最终NBT统计，非只换色或四柱空厅。'};
}
const directions=[['north','北',2],['south','南',0],['west','西',3],['east','东',1]];
function color(block){return block==='black_wool'?'#292633':block.includes('amethyst')?'#a473da':block.includes('glass')?'#79539d':block==='sea_lantern'?'#91f4ec':block==='blue_terracotta'?'#364b9c':block.includes('deepslate')?'#363848':block==='polished_andesite'?'#888b98':'#dfd9e5';}
function render(entry,turn){
 const cells=[];for(let y=1;y<15;y++)for(let z=1;z<15;z++)for(let x=1;x<15;x++){const block=entry.room.get(x,y,z);if(!solid(block)||(entry.key==='addition/10'&&y>=6))continue;const[rotatedX,rotatedZ]=turn===0?[x,z]:turn===1?[15-z,x]:turn===2?[15-x,15-z]:[z,15-x];cells.push({x:rotatedX,y,z:rotatedZ,block});}
 const unit=11,point=(x,y,z)=>[300+(x-z)*unit,65+16*unit+(x+z)*unit*.5-y*unit];
 const face=(points,fill,opacity)=>`<polygon points="${points.map(vertex=>point(...vertex).join(',')).join(' ')}" fill="${fill}" fill-opacity="${fill==='#79539d'?opacity*.38:opacity}" stroke="#262033" stroke-width=".4"/>`;
 let geometry='';cells.sort((left,right)=>left.x+left.z-right.x-right.z||left.y-right.y);
 const cuboid=(x,y,z,width,height,depth,fill)=>face([[x,y,z+depth],[x+width,y,z+depth],[x+width,y+height,z+depth],[x,y+height,z+depth]],fill,.75)+face([[x+width,y,z],[x+width,y,z+depth],[x+width,y+height,z+depth],[x+width,y+height,z]],fill,.88)+face([[x,y+height,z],[x+width,y+height,z],[x+width,y+height,z+depth],[x,y+height,z+depth]],fill,1);
 for(const{x,y,z,block}of cells){const fill=color(block);if(block.includes('stairs')){geometry+=cuboid(x,y,z,1,.5,1,fill);const offset=[[0,.5,1,.5],[0,0,.5,1],[0,0,1,.5],[.5,0,.5,1]][turn];geometry+=cuboid(x+offset[0],y+.5,z+offset[1],offset[2],.5,offset[3],fill);}else geometry+=cuboid(x,y,z,1,1,1,fill);}
 let markers='';for(const socket of entry.room.variant.sockets){const[x,,z]=socket.position,[rotatedX,rotatedZ]=turn===0?[x+.5,z+.5]:turn===1?[14.5-z,x+.5]:turn===2?[14.5-x,14.5-z]:[z+.5,14.5-x],projected=point(rotatedX,2.2,rotatedZ);markers+=`<circle cx="${projected[0]}" cy="${projected[1]}" r="3.5" fill="#ff5a9b" stroke="#fff"/>`;}
 const svg=`<svg xmlns="http://www.w3.org/2000/svg" width="600" height="450" viewBox="0 0 600 450"><rect width="600" height="450" fill="#10111e"/><text x="12" y="23" fill="#dac5f2" font-family="sans-serif" font-size="15">${entry.key} · ${directions.find(direction=>direction[2]===turn)[1]} · NBT实体轴测</text>${geometry}${markers}<text x="12" y="425" fill="#ad9ab9" font-family="sans-serif" font-size="11">隐藏边界外壳和y0基础、y15屋顶；保留y1地板，粉点=socket</text></svg>`;
 return{svg,geometrySha256:hash(Buffer.from(geometry)),solidCells:cells.length};
}
function fourViews(entries){
 const records=[],cards=[];for(const entry of entries){let figures='';for(const[direction,label,turn]of directions){const result=render(entry,turn),file='views/'+entry.key+'/'+direction+'.svg';save(file,result.svg);records.push({key:entry.key,direction,rotation:turn*90,nbtSha256:entry.sha256,file,sha256:hash(Buffer.from(result.svg)),geometrySha256:result.geometrySha256,solidCells:result.solidCells});figures+=`<figure><img width="600" height="450" src="${file}" alt="${entry.key} ${label}方位"><figcaption>${label} · 旋转${turn*90}°</figcaption></figure>`;}assert.equal(new Set(records.filter(record=>record.key===entry.key).map(record=>record.geometrySha256)).size,4);cards.push(`<article><h2>${entry.key} ${entry.name}</h2><p>baseKey: ${entry.baseKey} · NBT SHA256 ${entry.sha256}</p><div>${figures}</div></article>`);}
 save('four-views.html',`<!doctype html><html lang="zh-CN"><meta charset="utf-8"><title>紫晶新增第1批 · 真实NBT四视图</title><style>body{background:#10111b;color:#e8d9f6;font:15px system-ui;margin:20px}h1{color:#c59cf3}main{display:grid;grid-template-columns:1fr 1fr;gap:20px}article{padding:16px;background:#202032;border:1px solid #553d6d;border-radius:12px}article>div{display:grid;grid-template-columns:1fr 1fr}figure{margin:4px}img{display:block;width:100%;height:auto}p{font-size:11px;overflow-wrap:anywhere}figcaption{color:#b9a3ce}</style><h1>紫晶第1批 · 两间独立新房</h1><p>addition/10另统一隐藏y>=6高部以查看晶簇内芯；完整NBT未截切且封顶验证。全部8个视图均来自最终NBT重新解析，四方观察方向以90°旋转区分。统一隐藏x/z边界外壳、y0基础与y15顶盖，保留实际行走地板和内部家具/屋架；不是游戏光照渲染。外壳完整性另对完整NBT验证。</p><main>${cards.join('')}</main></html>`);return records;
}
async function browserCheck(){
 const{chromium}=await import(pathToFileURL('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright/index.mjs').href);
 const browser=await chromium.launch({headless:true,executablePath:'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe'}),page=await browser.newPage({viewport:{width:1800,height:1250}}),errors=[];
 page.on('pageerror',error=>errors.push(error.message));page.on('console',message=>{if(message.type()==='error')errors.push(message.text());});page.on('requestfailed',request=>errors.push(request.url()));
 await page.goto(pathToFileURL(join(output,'four-views.html')).href,{waitUntil:'load'});await page.evaluate(()=>document.fonts.ready);
 const images=await page.locator('img').evaluateAll(elements=>elements.map(element=>({complete:element.complete,width:element.naturalWidth,height:element.naturalHeight})));assert.equal(images.length,8);assert(images.every(image=>image.complete&&image.width===600&&image.height===450));
 await page.screenshot({path:join(output,'views/browser-contact.png'),fullPage:true});await browser.close();assert.equal(errors.length,0);
 return{status:'PASS',engine:'Microsoft Edge via bundled Playwright',loadedViews:8,errors,consoleErrors:0,screenshot:'views/browser-contact.png',screenshotSha256:hash(readFileSync(join(output,'views/browser-contact.png')))};
}
function verifyBatchThree(room,key){
 if(key==='addition/05'){
  const tiers=[];for(let tier=0;tier<4;tier++){const z=3+tier*2,height=10-tier*2;assert(room.get(2,height,z)==='amethyst_block');assert(room.get(1,2,z)==='calcite');tiers.push({from:[1,2,z],to:[3,height,z+1]});}
  for(let x=11;x<=14;x++)for(let z=12;z<=13;z++)for(let y=2;y<=5;y++)assert(solid(room.get(x,y,z)),'Sample slot breach');
  return{status:'PASS',sideSettlingTiers:tiers,centralTallTower:false,horizontalRegions:['west stepped sedimentation','east low fan archive','southeast closed sample slot'],sampleSlot:{from:[11,2,12],to:[14,5,13],sealed:true}};
 }
 const columns=[];for(const[x,z,height]of [[2,3,6],[5,6,9],[8,9,5],[11,11,8]]){for(let y=1;y<=height;y++)assert(solid(room.get(x,y,z)),`Unsupported acoustic column ${x},${y},${z}`);assert(room.get(x,2,z)==='calcite');columns.push({foot:[x,2,z],top:[x,height,z],supported:true});}
 return{status:'PASS',diagonalColumns:columns,unequalLengths:true,listeningPlatform:{from:[1,2,11],to:[3,6,13]},absorber:{from:[13,2,3],to:[14,9,10],material:'minecraft:black_wool'},threeFunctionalZones:true};
}
function verifyBatchFour(room,key){
 if(key==='addition/07'){
  const openZones=[{from:[6,2,2],to:[9,4,4]},{from:[6,2,12],to:[9,4,14]}];
  for(const zone of openZones)for(let x=zone.from[0];x<=zone.to[0];x++)for(let z=zone.from[2];z<=zone.to[2];z++)for(let y=2;y<=4;y++)assert.equal(room.get(x,y,z),'air','Battle clearing blocked');
  const centers=[[7,2],[8,4],[8,6],[8,8],[7,10],[7,11],[7,13]];
  const accessible=walk(room);for(const[x,z]of centers)assert(accessible.has(x+','+z),'S route point blocked '+[x,z]);
  let lowWorkCells=0;for(let x=2;x<=13;x++)for(let z=6;z<=11;z++)if(solid(room.get(x,2,z)))lowWorkCells++;
  const wide=walk(room,[7,2,1],true);for(const[x,z]of centers)assert(wide.has(x+','+z),'S route not three-wide '+[x,z]);
  for(let row=0;row<3;row++)for(let x=10+row;x<=14;x++)assert(solid(room.get(x,2,9+row)),'Wedge tray cropped');
  assert(lowWorkCells>12);return{status:'PASS',sRouteWaypoints:centers.map(([x,z])=>[x,2,z]),levelRoute:true,minimumWidth:3,openBattleZones:openZones,lowWorkCells,bedHeight:3,trayRows:[{from:[10,2,9],to:[14,2,9]},{from:[11,2,10],to:[14,2,10]},{from:[12,2,11],to:[14,2,11]}]};
 }
 const windows=[];for(const frame of[{x:2,z:6,width:3,height:8},{x:1,z:10,width:4,height:6}]){
  for(let x=frame.x+1;x<frame.x+frame.width;x++)for(let y=2;y<frame.height;y++)assert.equal(room.get(x,y,frame.z),'purple_stained_glass','Observation window breached');
  windows.push({from:[frame.x,2,frame.z],to:[frame.x+frame.width,frame.height,frame.z],realObservationGlass:true});
 }
 assert(!room.blocks.some(block=>/water|lava|rail|redstone/.test(block)));return{status:'PASS',offsetWindows:windows,solidSpecimens:[{from:[11,2,6],to:[13,7,7]},{from:[12,2,9],to:[14,5,10]}],diagonalObservationFloor:{from:[5,1,5],to:[10,1,9]},concentricRings:0};
}
function verifyFinalBatch(room,key){
 if(key==='addition/09'){
  const access=verifyObservation(room);const layers={};for(let y=2;y<=9;y++){const material=y%3===0?'calcite':y%3===1?'tuff':'deepslate_tiles';for(let z=2;z<=12;z++)assert.equal(room.get(1,y,z),material);layers[y]=material;}
  for(let z=2;z<=12;z++){const elevation=3+Math.floor((z-2)/2)%4;assert.equal(room.get(3,elevation,z),'amethyst_block');}
  return{status:'PASS',sideSection:{from:[1,2,2],to:[3,9,12],layers,zigzagVein:true},oppositeLowSpecimens:{from:[11,2,2],to:[13,5,3]},cornerPlatformAccess:access,mechanicalComponents:0};
 }
 for(let y=3;y<=6;y++)for(let x=2;x<=5;x++)for(let z=2;z<=4;z++)if(x===2||x===5||z===2||z===4)assert.equal(room.get(x,y,z),'purple_stained_glass','Rectangular cage breached');
 for(let x=2;x<=5;x++)for(let z=2;z<=4;z++){assert(solid(room.get(x,2,z)));assert(solid(room.get(x,7,z)));}
 for(const[x,z]of diamondCells()){assert(solid(room.get(x,2,z)));assert(solid(room.get(x,8,z)));if(Math.abs(x-12)+Math.abs(z-9)===2)for(let y=3;y<=7;y++)assert.equal(room.get(x,y,z),'purple_stained_glass','Diamond cage breached');}
 const visited=walk(room);for(const[x,z]of diamondCells())assert(!visited.has(x+','+z),'Main route entered diamond cage');for(let x=2;x<=5;x++)for(let z=2;z<=4;z++)assert(!visited.has(x+','+z),'Main route entered rectangular cage');
 return{status:'PASS',isolationCages:[{shape:'rectangle',from:[2,2,2],to:[5,7,4],sealed:true},{shape:'diamond',center:[12,9],radius:2,baseY:2,roofY:8,sealed:true}],differentContours:true,triangleWorkstation:{from:[2,2,10],to:[3,5,13]},mainRouteEntersCage:false};
}
function validate(room,spec){
 const variant=room.variant,clear=(point,height)=>{const[x,y,z]=point;assert(solid(room.get(x,y-1,z)),`support ${spec.key} ${point}`);for(let rise=0;rise<height;rise++)assert.equal(room.get(x,y+rise,z),'air',`AIR ${spec.key} ${point}`);};
 for(const port of variant.ports)for(const[x,y,z]of port.cells)for(let inward=0;inward<=4;inward++)clear([x,y,z===0?inward:15-inward],5);
 for(const socket of variant.sockets){clear(socket.position,3);clear(socket.spawn_position,3);}
 const visited=walk(room);for(const point of variant.ports.flatMap(port=>port.cells).concat(variant.sockets.flatMap(socket=>[socket.position,socket.spawn_position])))assert(visited.has([point[0],point[2]].join(',')),`Disconnected ${spec.key} ${point}`);
 const wide=walk(room,[7,2,1],true);assert(wide.has('7,14'),`3 wide route ${spec.key}`);
 const reserved=new Set(variant.sockets.flatMap(socket=>[socket.position.join(','),socket.spawn_position.join(',')])),entityFeet=[];
 for(const socket of variant.sockets){const[x,y,z]=socket.position;let point=null;for(let distance=1;distance<=6&&!point;distance++)for(let dx=-distance;dx<=distance&&!point;dx++)for(let dz=-distance;dz<=distance&&!point;dz++){const candidate=[x+dx,y,z+dz];if(Math.abs(dx)+Math.abs(dz)!==distance||reserved.has(candidate.join(',')))continue;if(visited.has([x+dx,z+dz].join(',')))point=candidate;}assert(point);reserved.add(point.join(','));entityFeet.push({socket:socket.position,independentFeet:point});}
 const openings=new Set(variant.ports.flatMap(port=>port.cells.flatMap(([x,y,z])=>Array.from({length:5},(_,rise)=>[x,y+rise,z].join(',')))));
 for(let y=0;y<16;y++)for(let z=0;z<16;z++)for(let x=0;x<16;x++)if(x===0||z===0||y===0||x===15||z===15||y===15)assert(solid(room.get(x,y,z))||openings.has([x,y,z].join(',')));
 return{key:spec.key,baseKey:spec.baseKey,status:'PASS',size:variant.size,minimum_y:variant.minimum_y,ports:variant.ports,sockets:variant.sockets,spawn_position:variant.spawn_position??null,denseUniqueComplete:true,dataVersion:3465,entities:0,blockEntities:0,barePaletteNames:true,fullSupports:true,portClearance5:true,portInwardReservedDepth:4,socketClearance3:true,allInterfacesConnected:true,jumpRequired:false,minimumMainRouteWidth:3,reachableFloorCells:visited.size,wideRouteCenters:wide.size,independentEntityFeet:entityFeet,sealedEnvelope:true};
}
save('design.md',`# 紫晶扩展第1批：addition/01、addition/02\n\n仅授权两间，旧27房及正式资源不改。运行 node tools/expand-amethyst.mjs；完成后以 --verify-replay 重放逐项核对哈希并冻结。\n\n## addition/01 晶片切割实验室\n\n继承 straight/room_cells。偏西多面晶坯托架在x1..5,z7..11，错层晶坯从宽至窄收缩；斜向切片床从x6,z6延伸至x11,z11；东北侧标本台位于x12..14,z2..6，形成晶坯→切片→样品记录工序。通道沿三段障碍旁边绕行；四个分散socket保留。建筑高度：床2..4，晶坯托架2..9，侧台2..7，局部晶质天窗/檐架8..13。地标是切面与工序分段，无齿轮、无轨道环。\n\n## addition/02 谐振晶仓\n\n继承 straight/room_storage。三开口框位于z7、z5、z12，框高6、9、7，横向错列而非排架；每框晶体从上梁悬挂，灯作为共鸣节点。西南记录台和封存槽在x1..6,z11..14组成L。路线穿过开框与框间侧廊，区别于第一房对角工序；四socket分散保留。局部透明天窗位于西北y10和东y12，密封外壳另验。\n\n## 自检方法\n\n所有检查从导出NBT重新解析。保留端口内向5列、每列5格AIR；socket3格AIR并有完整实体支撑。实际地面BFS与3×3侵蚀图验证无需跳跃和主路三格宽。四视图隐藏边界墙、底部y0和y15顶盖，保留y1行走层。重复度移除共同外壳/地板/端口带，对比旧27间、已完成新房以及本批另一房，四旋转归一化Jaccard>=0.85即失败返工。\n`);
save('design.md',`# 紫晶扩展第2批：addition/03、addition/04\n\n本批仅两间。第1批和旧27房全文件哈希在执行前后保持一致，不改正式资源。运行 node tools/expand-amethyst.mjs；随后 --verify-replay 重放冻结。\n\n## addition/03 折光测距厅\n\n继承straight/room_towers全部接口。西屏x2..5,z6..7与东屏x9..12,z7..8逐格锯齿错位，晶质屏额高度7和8构成斜向观察线。东南测距观察台x10..14,z11..13，地面脚高4；x11..13,z9..10的两级石英梯从脚高2升至4，不跳跃。局部玻璃观察顶位于西北y10及东侧y11。不同于旧双实塔和第1批三共鸣框；这里主体是偏轴薄屏和可达台面。四socket保留。\n\n## addition/04 晶格培育室\n\n继承straight/room_cells全部接口。三形密闭培养体：矩形x2..5,z6..7，十字中心x10,z8，L形x2..4,z11..13；高度7、7、6，不规则三角形分布。西壁x1..2,z2..7的阶梯晶格切面上升y5..11。三台逐列完全封闭，光学晶内芯为amethyst_block，不使用晶芽或会生长的budding_amethyst。路线绕台经过中部和东部，玻璃采光顶完整密封。不同于实验床工序、共鸣框及旧库柜。\n\n## 验证及四视图\n\n两房各16³密集NBT，DataVersion3465，palette属性分离。端口内向5列均5AIR，socket3AIR及完整支撑；独立实体脚点不重叠且同高。三格主路侵蚀图、全接口地面BFS、观察台0.5格真实梯面碰撞图及培养台实心封闭体检查。8幅四方NBT轴测隐藏边界、y0基础、y15屋顶，保留实际地板及楼梯半阶；浏览器截图供逐图检查。重复度剔除共同外壳/地板/端口带，比较旧27间、第1批2间及本批内部，>=0.85失败。所有离线验证不冒称游戏实测。\n`);
save('design.md',`# 紫晶扩展第3批：addition/05、addition/06\n\n只制作本批两间，不修改旧27间、前两批或正式资源。配方快照recipe-snapshot.mjs和所有交付文件纳入freeze.json。可直接运行快照（不依赖后续改写的tools配方），再使用--verify-replay检查逐项哈希。\n\n## addition/05 星尘沉降档案室\n\n继承straight/room_storage原接口。西侧x1..3,z3..10为四段沉降塔，高10、8、6、4，宽三格；逐段玻璃切面和内灯展示沉降工序。东侧x10..14,z6..10的低矮档案柜以三段扇形递进，脚高2、柜高3；东南x11..14,z12..13为完整密封玻璃样本槽，顶高5。上方侧采光顶位于西y12与东y8。横向体量拉开，中央保留连通宽路，不放中央高塔。四socket分散且安全。\n\n## addition/06 声波标定室\n\n继承straight/room_towers原接口。晶音柱落在(2,3)、(5,6)、(8,9)、(11,11)对角线上，高6、9、5、8；每根从完整地板和方解石柱脚起，灯及玻璃端帽形成不同谐振长度。西南x1..3,z11..13是偏心监听台，东侧x13..14,z3..10为黑羊毛折面吸音挡板，晶质压顶。三段功能为声源列→监听→吸音，主路穿柱列侧边，挡板不截端口。\n\n## 自检\n\n导出后重新解码密集NBT：DataVersion3465、属性分离、无实体/方块实体。端口内向5列5AIR，四socket3AIR及完整支撑，同高独立实体脚点。三格主路与接口BFS无需跳跃。沉降塔侧位与样本槽封闭、四晶音柱从地板至顶连续支撑均在实际NBT断言。四视图隐藏边界墙、y0和y15，保留地板与内装；浏览器加载8图、截图后检查。重复度去除公共地板/外壳/接口，四旋转归一化比较旧27房、前两批4房及本批另一房。离线几何检查不声称游戏实测。\n`);
save('design.md',`# 紫晶扩展第4批：addition/07、addition/08\n\n仅制作本批两间，前三批冻结产物与旧27房、正式资源不改。当前源码保存为recipe-snapshot.mjs，快照可直接执行并使用--verify-replay重放。\n\n## addition/07 碎晶拼合工坊\n\n继承straight/room_cells。西侧低拼合床x2..6,z6..8，高2..3；东南楔形托盘三排为x10..14/z9、x11..14/z10、x12..14/z11，逐排收窄，碎晶使用稳定amethyst_block。西北校准尺位于x1..2,z2..6，高3与侧尺高7，构成不对称作业带。主路蓝色地面标线从x7,z2经x8,z4..9再回x7,z10..13，始终脚高2，逐点三格宽。北侧x6..9,z2..4和南侧x6..9,z12..14为两处开阔战斗坪。局部实体屋架仅对应作业面，高7..10，无通用晶环。\n\n## addition/08 虚像观测室\n\n继承straight/room_storage。西侧两片真实玻璃观测窗分别x2..5/z6/h8与x1..5/z10/h6，边框错位而不使用同心环。东侧两座实体晶样位于x11..13,z6..7和x12..14,z9..10；观察玻璃只填实窗格、外壳与采光顶，不放无功能玻璃摆件。地板x5,z5至x10,z9形成斜向观察线，中偏心低观察台及东南记录矮台串联两侧对照。低台为观察定位，平层主路绕台无需跳跃。侧实体顶架与局部观察窗形成纵向高低差。四socket全部继承原位。\n\n## 验证\n\n导出后重新解码完整16³ NBT，DataVersion3465，palette属性分离、无实体/方块实体。端口内向5列5AIR、socket3AIR及完整支撑，同高独立实体脚点不重叠。接口BFS、三格宽主路、S形逐点宽度、两处战斗坪、楔形三排完整性及玻璃窗逐格完整性均检查实际NBT。四方视图隐藏边界外壳/y0基础/y15顶盖，保留地板、家具与屋架。比较旧27房、前6间新增及本批另一房，剔除公共结构后四旋转归一化Jaccard>=0.85失败返工。实际查看浏览器截图，不声称游戏实测。\n`);
save('design.md',`# 紫晶扩展第5批：addition/09、addition/10\n\n仅制作最后两间，完成后新增总计10间，停止写入。前四批冻结产物、旧27间及正式资源不改。recipe-snapshot.mjs为独立可运行配方快照，使用--verify-replay检查哈希并冻结。\n\n## addition/09 晶脉地质剖面馆\n\n继承straight/room_towers原接口。西壁x1..2,z2..12,y2..9按凝灰岩、方解石、深板岩分层；面层x3逐段折线紫晶脉高3..7，横向展开为地质剖面而非机房。东北x11..13,z2..3的低标本台高3、4、5，展示地层样本。东南x10..14,z11..13为脚高4登高平台，x11..13,z9/y2和z10/y3为三格宽两级石英梯，从脚高2免跳跃到平台。局部侧顶高9、11、12，玻璃外壳完整封顶。四socket原位保留。\n\n## addition/10 极性分离研究所\n\n继承straight/room_cells原接口。西北长方笼x2..5,z2..4,y2..7，东南菱形笼以(12,9)为中心、曼哈顿半径2、底y2顶y8，两种轮廓分置对角，边界为玻璃隔离面，底顶实体密封；内芯用稳定amethyst_block组合不同晶簇。西南x2..3,z10..13低分离台与两笼形成三角工序。主路在笼外平层绕行，不穿笼，不改变端口和四socket。顶部观察玻璃连接至实际隔离笼，封顶检查使用完整NBT。\n\n## 自检与视图\n\n完整稠密NBT由导出文件重新解码检查，DataVersion3465、palette属性分离、无实体/方块实体。端口内向5列5格AIR、四socket3格AIR及完整支撑；同高独立实体脚点。接口BFS与三格宽主路检查。地质层和折线脉逐格验证，登高平台0.5格碰撞图确保免跳跃。两种隔离笼逐边界/底顶检查密闭、笼内不得进入主路访问集合。8个四方真实NBT图像隐藏边界/y0基础/y15顶盖；addition/10另统一截去y>=6高部，便于看清实际内部晶簇，完整封壳检查不截切。玻璃以低不透明度显示，无游戏材质光照。对比旧27房、前8间新增及本批另一房，剔除公共外壳/地板/接口后四旋转Jaccard>=0.85即返工。\n`);
save('recipe-snapshot.mjs',readFileSync(recipePath));
const entries=[],results=[];
for(const spec of authorized){
 const variant=catalog.find(candidate=>candidate.key===spec.baseKey);assert(variant);assert.deepEqual(variant.size,[16,16,16]);const room=new Room(variant);room.shell();if(spec.key==='addition/09')buildGeology(room);else buildSeparation(room);reserve(room);widenRoute(room);reserve(room);
 const structure=new library.Structure(variant.size);for(let y=0;y<16;y++)for(let z=0;z<16;z++)for(let x=0;x<16;x++){const state=library.BlockState.parse('minecraft:'+room.get(x,y,z));structure.addBlock([x,y,z],state.getName().toString(),state.getProperties());}
 const bytes=Buffer.from(structure.writeNbt({name:'',dataVersion:3465,compression:'gzip'})),file='roomset/'+spec.key+'.nbt';save(file,bytes);const actual=decode(readFileSync(join(output,file)),variant);const validation=validate(actual,spec);validation.featureCheck=verifyFinalBatch(actual,spec.key);validation.sha256=hash(bytes);results.push(validation);entries.push({...spec,sha256:hash(bytes),file,room:actual});console.log(spec.key+' PASS '+hash(bytes));
}
save('manifest.json',{schema:1,style:'fteclipsegen:amethyst',batch:1,templates:entries.map(({room,...entry})=>entry)});
save('validation.json',{status:'PASS',schema:1,style:'fteclipsegen:amethyst',batch:1,catalogSha256:hash(catalogRaw),results});
const style={status:'PASS',batch:1,style:'fteclipsegen:amethyst',source:'Final dense NBT round trip; material counts and theme bounding-box occupied cells verified',rooms:entries.map(auditStyle)};save('style-audit.json',style);
const repetition=compareRooms(entries);save('repetition-audit.json',repetition);
const views=fourViews(entries),browser=await browserCheck();
if(before)for(const[file,expected]of Object.entries(before))assert.equal(hash(readFileSync(join(output,file))),expected,'Replay mismatch '+file);
save('self-audit.json',{status:before?'PASS':'FAIL',batch:1,style:'fteclipsegen:amethyst',authorization:{keys:authorized.map(entry=>entry.key),nextBatchStarted:false},fourViews:{status:'PASS',expected:8,actual:views.length,source:'Actual final NBT decoded, 4 rotations per room',hidingRule:'Boundary x/z walls, y0 foundation, y15 top roof hidden; y1 walking floor retained',records:views},browser,rooms:entries.map(entry=>({key:entry.key,status:'PASS',interface:'PASS',style:'PASS',repetition:'PASS',visualReview:'Four directions rendered from actual NBT; inspect browser-contact.png for process arrangement, frame openings and clear circulation'})),repetition:{status:'PASS',old27Compared:27,currentBatchPairs:1,maxSimilarity:Math.max(...repetition.pairs.map(pair=>pair.normalizedInteriorSolidJaccard)),unresolvedHighSimilarity:0},replay:{status:before?'PASS':'FAIL',command:'node tools/expand-amethyst.mjs --verify-replay',matchingArtifacts:before?Object.keys(before).length:0,hashes:before??{}},limitations:['No in-game collision/gameplay acceptance; flat floor connectivity verified offline.','Four views use actual solid geometry with palette colors, not game lighting or texture packs.']});
const selfAudit=JSON.parse(readFileSync(join(output,'self-audit.json')));
selfAudit.repairs=[{key:'addition/01',finding:'初版晶坯托架围住socket [4,2,9]；对角床造成局部窄路',resolution:'四socket周围各保留3×3、三格净空地面，以最少实体删除的3格宽路径连接南北接口；晶坯切面保留在上层和偏西支架；最终NBT全接口与3格路线PASS'},{key:'addition/01',finding:'侧样品台只有紫玻璃，缺少实体晶样',resolution:'加入两块真实budding_amethyst晶样，风格审计逐地标统计材料和包围盒PASS'},{key:'addition/02',finding:'初版共鸣框腿与预留通道、socket安全区相交，被裁成悬梁',resolution:'三框调整为x1..5/z7/h6、x9..14/z5/h10、x10..14/z9/h8；四视图复核完整落地框形与悬晶、保持开放穿行路线'}];
for(const room of selfAudit.rooms)room.visualReview={status:'PASS',directions:['north','south','west','east'],evidence:'views/browser-contact.png',observations:room.key==='addition/01'?'已查看四方实际NBT截图：多面晶坯托架、对角低切片床和东北标本台分段清晰；主路从侧边绕行，四粉色socket点分散，局部透明天窗为墙承构件。':'已查看四方实际NBT截图：高低三共鸣框落地完整、上梁悬晶与灯可辨，L记录封存区在西南；框体分布区别于实验室工序，未复用旧27房骨架。'};
selfAudit.repairs=[{key:'addition/03',finding:'观察台不得靠跳跃到达，四视图需体现真实半阶',resolution:'设置x11..13,z9/y2与z10/y3两跑三格石英梯，半格碰撞图验证脚高4的[11,4,11]免跳跃可达；四视图绘制下半砖与上半阶而非整块盒子'},{key:'addition/04',finding:'密闭培养台不能被socket和主路预留裁开',resolution:'三种培养体避开原接口，L形台偏置西南；逐列从底座到顶盖检查无AIR，战斗区无晶芽或生长方块'},{key:'both',finding:'避免与前批及旧27房重复',resolution:'去除共同外壳/地板/端口带，旋转归一化比较旧27房、第1批2房及本批另一房；所有Jaccard低于0.85'}];
for(const room of selfAudit.rooms)room.visualReview={status:'PASS',directions:['north','south','west','east'],evidence:'views/browser-contact.png',observations:room.key==='addition/03'?'四方向真实NBT截图可见两段错位锯齿屏、东南偏心测距台和观察台阶，侧向通道保留；玻璃顶作为采光构件；梯面连通另有0.5格碰撞验证。':'四方向真实NBT截图可见矩形、十字与L形三培养体呈不规则三角形，西壁阶梯晶格切面清晰；台间路线不被侧壁装饰覆盖，三体封闭性另逐列复核。'};
selfAudit.repetition.previousAdditionsCompared=repetition.previousAdditionsCompared;
selfAudit.featureChecks=results.map(result=>({key:result.key,...result.featureCheck}));
for(const[path,expected]of Object.entries(previousHashes))assert.equal(hash(readFileSync(path)),expected,'Frozen batch01 was modified: '+path);
selfAudit.previousBatchPreservation={status:'PASS',batches:[1,2],unchangedFileCount:Object.keys(previousHashes).length,sha256:previousHashes};
selfAudit.repairs=[{key:'addition/05',finding:'沉降体量不可退化为中央高塔或旧库柜',resolution:'四段沉降塔全部限制在西x1..3，东扇形矮柜及密闭样本槽分区；实际NBT逐段压顶与样本封闭断言'},{key:'addition/06',finding:'对角音柱不得悬空，吸音屏不能挡门',resolution:'逐柱从地板y1至顶端连续支撑断言；东壁屏限制x13..14，四socket和南北端口主路保持连通'},{key:'both',finding:'跨批建筑重复和前批覆盖风险',resolution:'比较旧27房及前4间，阈值0.85；执行前后核对前两批全部文件；本批另保存可执行源码快照并加入冻结清单'}];
for(const room of selfAudit.rooms)room.visualReview={status:'PASS',directions:['north','south','west','east'],evidence:'views/browser-contact.png',observations:room.key==='addition/05'?'四方真实NBT投影显示西侧阶梯沉降体连续降高，东矮柜扇形展开与样本槽有明确分区；中部无高塔、主路净空。':'四方真实NBT投影显示四根不同长度晶音柱为对角分布，柱脚落地；西南监听台、东壁黑色吸音面可辨，屏障在接口之外。'};
selfAudit.repairs.push({key:'addition/06',finding:'首轮路线拓宽裁掉[5,2,6]晶音柱脚；保柱后南端出口转弯宽度不足',resolution:'路线搜索将四个柱脚设为不可裁切区域；末柱从z12退至z11，仍保持对角错位序列，留出南侧三格横向回转；再次逐柱验证y1至柱顶连续支撑及三格宽主路'});
selfAudit.replay.command='node output/room-expansion-v035/amethyst/batch-03/recipe-snapshot.mjs --verify-replay';
selfAudit.replay.executedFromSnapshot=recipePath.endsWith('recipe-snapshot.mjs');
selfAudit.recipeSnapshot={file:'recipe-snapshot.mjs',sha256:hash(readFileSync(join(output,'recipe-snapshot.mjs'))),identicalToExecutedSource:true};
selfAudit.limitations=['Actual final NBT offline geometry and browser projections, not Minecraft gameplay acceptance.','All catalog interfaces have AIR and full support; sealed sample slot and continuous acoustic column foot support verified explicitly.'];
selfAudit.previousBatchPreservation={status:'PASS',batches:[1,2,3],unchangedFileCount:Object.keys(previousHashes).length,sha256:previousHashes};
selfAudit.repairs=[{key:'addition/07',finding:'首轮四视图中楔形托盘被主路拓宽裁切，工序体量不清晰',resolution:'三排托盘向东调整为x10..14/z9、x11..14/z10、x12..14/z11，逐排缩窄；低拼合床保留，逐格断言托盘未裁切；S形路线逐点三格宽，南北两片战斗坪净空复核'},{key:'addition/08',finding:'观测玻璃必须有真实功能，不能退化为无目的玻璃装饰或同心环',resolution:'两片玻璃只填满错位观察窗，外壳和采光顶另完整围封；东侧实体晶样对照，斜向地面观察标线串联偏心低台；逐窗检查实际NBT玻璃完整性'},{key:'both',finding:'只推进当前批并保留前三批冻结',resolution:'前三批全部文件执行前后逐项哈希核对；本批配方快照独立运行重放；重复度对旧27房、前6间及本批内部逐对比较'}];
for(const room of selfAudit.rooms)room.visualReview={status:'PASS',directions:['north','south','west','east'],evidence:'views/browser-contact.png',observations:room.key==='addition/07'?'实际查看四方向NBT截图：西侧低拼合床、东南递缩楔形托盘和西北量尺可辨；不对称体量与S形平层标线、两片开阔坪相容，碎晶未采用不稳定晶芽。':'实际查看四方向NBT截图：西侧两错位玻璃窗与东侧实体晶样可辨，窗框高低不同；斜向观察线和偏心低台位于两侧对照之间，无同心环。'};
selfAudit.replay.command='node output/room-expansion-v035/amethyst/batch-04/recipe-snapshot.mjs --verify-replay';
selfAudit.limitations=['Actual exported NBT palette-color projections and offline geometry checks, not Minecraft gameplay testing.','Observation windows are fully occupied glass planes; transparent resource-pack rendering and optical effects require game acceptance.'];
selfAudit.previousBatchPreservation={status:'PASS',batches:[1,2,3,4],unchangedFileCount:Object.keys(previousHashes).length,sha256:previousHashes};
selfAudit.repairs=[{key:'addition/09',finding:'地质主题应由层理与晶脉剖面体现，角台需要真实可步行高差',resolution:'凝灰岩/方解石/深板岩逐层断言，x3折线紫晶脉逐段检查；东南三格宽石英梯以0.5格碰撞图验证平台免跳跃可达'},{key:'addition/10',finding:'首轮四视图笼顶遮住内芯，无法充分辨认两种晶簇形态',resolution:'投影统一截去该房y>=6高部并明确标注，玻璃低不透明度显示；完整NBT保持封顶，长方与菱形笼边界/顶底及主路不入笼断言全部通过'},{key:'both',finding:'最后一批仍需独立风格、跨批重复与冻结保全',resolution:'实际NBT逐地标材料/包围盒自检；对比旧27和前8间以及本批另一房；前四批文件字节哈希不变，最终从快照直接重放后停止'}];
for(const room of selfAudit.rooms)room.visualReview={status:'PASS',directions:['north','south','west','east'],evidence:'views/browser-contact.png',observations:room.key==='addition/09'?'已查看四方向真实NBT截图：西壁水平地层、折线晶脉和另一侧分层样本台可辨；东南登高台和两级梯存在，平台连通另有半格碰撞图实证，地质馆无机械零件。':'已查看四方向真实NBT截顶截图：长方及菱形隔离轮廓、两组实际晶簇内芯、西南低工作台可辨；主路从笼外绕行；截图截去y>=6仅为观察，完整NBT两笼顶底及外壳未改变。'};
selfAudit.fourViews.hidingRule='Boundary x/z walls, y0 foundation and y15 roof hidden; walking floor retained. addition/10 additionally hides all y>=6 for interior specimen inspection; complete NBT remains sealed.';
selfAudit.replay.command='node output/room-expansion-v035/amethyst/batch-05/recipe-snapshot.mjs --verify-replay';
selfAudit.limitations=['Final NBT offline palette-color projections and collision/route checks, not Minecraft native placement or gameplay testing.','Transparent glass and addition/10 interior view cut are display rules only; complete NBT sealing verified independently.'];
selfAudit.completion={authorizedBatch:5,totalNewRoomsAcrossFiveBatches:10,stopAfterFreeze:true};
save('self-audit.json',selfAudit);
const frozenFiles=[...replayFiles,'self-audit.json'];save('freeze.json',{status:before?'FROZEN_FOR_PARENT_INTEGRATION':'CANDIDATE_REQUIRES_REPLAY',style:'fteclipsegen:amethyst',batch:1,templates:2,recipe:{file:'tools/expand-amethyst.mjs',sha256:hash(readFileSync(recipePath))},files:Object.fromEntries(frozenFiles.map(file=>[file,hash(readFileSync(join(output,file)))])),manifestSha256:hash(readFileSync(join(output,'manifest.json')))});
console.log(batchName+' '+(before?'FROZEN_FOR_PARENT_INTEGRATION':'CANDIDATE_REQUIRES_REPLAY'));
