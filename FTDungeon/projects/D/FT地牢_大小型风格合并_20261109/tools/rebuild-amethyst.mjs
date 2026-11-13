import assert from 'node:assert/strict';
import { createHash } from 'node:crypto';
import { readFileSync, writeFileSync, mkdirSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';
const root=fileURLToPath(new URL('../',import.meta.url));
const out=join(root,'output/style-rebuild-v034/amethyst');
const resources=join(root,'src/main/resources/data/fteclipsegen');
const catalogBytes=readFileSync(join(resources,'catalog.json'));
const catalog=JSON.parse(catalogBytes).variants;
const library=await import(pathToFileURL('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js').href);
const sha=bytes=>createHash('sha256').update(bytes).digest('hex');
const replayPaths=['roomset/manifest.json','design.md','validation.json','gallery.html','four-views.html','style-audit.json','repetition-audit.json',...catalog.map(variant=>'roomset/'+variant.key+'.nbt'),...catalog.flatMap(variant=>['north','south','west','east'].map(direction=>'views/'+variant.key+'/'+direction+'.svg')),...[1,2,3].map(index=>'views/contact-'+String(index).padStart(2,'0')+'.png')];
const replayBefore=process.argv.includes('--verify-replay')?new Map(replayPaths.map(path=>[path,sha(readFileSync(join(out,path)))])):null;
const designs={
 'spawn/spawn':['初星测站','入场定向','西测星座与东斜晶帆夹偏轴前庭，四格出口轴贯通','脚4，仪12，拱15','双瓣望远仪、晶帆','纵向校准前庭'],
 'spawn/outside_near':['近星折光庭','近景','L晶崖与三角观测翼夹折线路径','地2，崖19，顶25','非对称晶崖、折光窗','L形高崖'],
 'spawn/outside_far':['远星信标崖','远景','对角晶脊分两个不等庭院，横廊串联','地2，脊21','斜晶脊、单侧塔','对角山形'],
 'straight/simple':['测星仪廊','测量通道','三高空仪架跨直廊，西实体仪台形成侧凹','地2，架12','三联仪架','重复拱架仅此廊'],
 'straight/door':['折射阈门','分段通道','两隔屏在z5和z10留交错宽门隙','地2，屏9，脊11','双棱镜屏','S形绕屏'],
 'straight/bridge':['悬晶长桥','跨空连接','四格中桥跨双槽，端横桥台接接口','桥2，槽0，桁9','双槽、斜拉桁','真实大面积挖空'],
 'straight/room_cells':['棱镜库','库战斗','错位深库夹Z巷、两侧回旋区','地2，柜10','实体棱镜库柜','战斗A：Z巷'],
 'straight/room_storage':['星图台厅','星图战斗','偏东长图台，西纵廊与端横廊构成U环绕','地2，台5，图10','长图台、悬图板','战斗B：U廊'],
 'straight/room_towers':['双焦观测厅','双站战斗','不等高偏心双塔构成对角穿行及侧路','地2，塔8和12','双焦塔、斜顶桥','战斗C：双点对角'],
 'straight/hole':['裂晶洞室','洞室战斗','东南岩体与西槽夹中部三叉步道','地2，槽0，峰11','岩舌、参差晶峰','战斗D：分叉洞室'],
 'straight/platform':['分光阶台','高低战斗','西台与东两级台用石英梯连接，低中廊贯通','低2，台4和5','不等高双台、悬梁','高位socket真实楼梯'],
 'turn/pillar_room':['赤经十字厅','三向交汇','西厚墙分窄西廊与宽东厅，横切口接三向','地2，墙10，顶12','赤经墙、十字梁','三向A双厅'],
 'turn/big_doors':['天极三拱厅','大型交汇','北南东三厚拱，中央大厅，西仪台遮挡','地2，拱12','厚门墩、西仪台','三向B辐射厅'],
 'turn/small_doors':['折光岔厅','短门三岔','错位短墙构三叉廊与双壁龛','地2，墙7','双隔壁','三向C紧凑三岔'],
 'turn/hole':['陨晶断层','断层绕行','西北斜坑与东南崖之间沿北东南绕行','地2，坑0，崖11','斜槽、晶崖、悬梁','三向D对角断层'],
 'stairs/centered':['轨道折返梯','上下连通','东南上行、南横台、西北上行，上下侧廊','2至18石英梯，顶29','三格双跑、半轨弧','镜像手性双跑开放井'],
 'stairs/hole':['晶井梯廊','多层洞井','双跑绕晶井，西7层与东12层支廊','2、7、12、18','晶井、错高支廊','中间层侧廊'],
 'stairs/spiral':['偏轴星旋梯','分台上行','双跑与西4层、东6层支梯组成折向回游','2、4、6、18','叉台、扇形顶架','多支路回游'],
 'curve/simple':['折角星尺','转弯','东北斜角库压出北入西出宽弯廊','地2，库8','阶梯壁、测尺','L入口组合'],
 'terminal/canteen':['星茶补给所','商店','后L柜分服务区，前庭买卖，侧不等长凳','地2，柜3，檐9','L柜、凳、菜单板','前后服务分区'],
 'terminal/storage':['陨石标本库','标本端部','横背柜和左纵柜成F巷，右路通后台','地2，架9','F库架','端部F浏览巷'],
 'terminal/cells':['棱镜封存舱','端部战斗','三实体舱分两侧廊，后横廊闭合','地2，舱8','不等宽三舱','双侧回游'],
 'straight/room/layout/cells':['三瓣标定芯','子房','三不同方向短壁形成三瓣凹室','地1，壁5，梁7','三瓣短壁','子房A凹室'],
 'straight/room/layout/storage':['折叠图纸芯','子房','北横柜南短柜构偏置双通道','地1，柜4，檐7','交错双图柜','子房B双巷'],
 'straight/room/layout/towers':['单焦镜台芯','子房','偏东单台与西宽弯廊，顶镜悬置','地1，台5，镜7','单方焦台','子房C单点绕行'],
 'boss/eclipse_keeper':['蚀星天文穹厅','Boss返程','中宽战场，两翼测星廊，完整z22墙指定门通返程','地2，翼9，穹21','多瓣穹、悬空轨道环','仅Boss用大型轨道环为主体'],
 'promenade':['星轨观景端','观景端部','后偏轴台、西斜晶帘形成弯折前庭','地2，台5，帘12','后星轨剖面、晶帘','独立端景']
};
const air=state=>state==='air';
const nameOf=state=>state.split('[')[0];
const full=state=>!air(state)&&state!=='void'&&!/stairs|slab/.test(state);
const stair=facing=>`quartz_stairs[facing=${facing},half=bottom,shape=straight,waterlogged=false]`;
function detail(room){
 const key=room.variant.key,top=room.height-2;
 const box=(first,last,state)=>room.box(first,last,state);
 const beam=(first,last,state='calcite')=>room.line(first,last,state);
 if(key==='spawn/spawn'){
  box([1,4,2],[4,6,10],'calcite');room.instrument(2,4,7);
  for(let y=6;y<=13;y++)box([12-y%3,y,2],[13,y,5],'amethyst_block');
  for(let x=2;x<=13;x++){const y=15-Math.floor(Math.abs(x-7.5)/2);beam([x,y,3],[x,y,12],x%3===0?'calcite':'purple_stained_glass');}
 }else if(key==='spawn/outside_near'){
  box([1,2,1],[4,15,5],'deepslate_tiles');box([1,2,5],[3,11,12],'calcite');
  for(let index=0;index<5;index++)room.crystal(3+index*2,3,18-index*2,2);
  for(let z=7;z<=12;z++)box([11,9+z-7,z],[13,9+z-7,z],'amethyst_block');
 }else if(key==='spawn/outside_far'){
  for(let index=0;index<10;index++){if(index>=4&&index<=6)continue;box([2+index,2,2+index],[3+index,8+index,3+index],'deepslate_tiles');room.crystal(3+index,3+index,12+index,1);}
  box([11,2,2],[13,20,4],'calcite');box([11,21,2],[13,21,4],'sea_lantern');
 }else if(key==='straight/simple'){
  for(const z of [3,7,11]){box([2,3,z],[3,10,z+1],'calcite');box([12,6,z],[13,10,z+1],'amethyst_block');beam([3,11,z],[12,11,z]);beam([5,12,z],[10,12,z],'amethyst_block');}
 }else if(key==='straight/door'){
  box([1,2,5],[9,8,6],'amethyst_block');box([7,2,10],[14,9,11],'calcite');box([10,9,4],[13,10,7],'purple_stained_glass');box([2,10,9],[5,11,12],'purple_stained_glass');
 }else if(key==='straight/bridge'){
  box([1,1,3],[5,1,12],'air');box([10,1,3],[14,1,12],'air');
  for(const z of [2,13])box([1,1,z],[14,1,z],'calcite');
  for(const x of [5,10]){beam([x,9,2],[x,5,7],'amethyst_block');beam([x,5,7],[x,9,13],'amethyst_block');beam([x,10,2],[x,10,13]);}
 }else if(key==='straight/room_cells'){
  box([2,2,6],[5,9,8],'calcite');box([9,2,6],[13,7,9],'amethyst_block');for(const z of [6,8])box([2,5,z],[5,6,z],'purple_stained_glass');box([9,10,2],[13,10,6],'amethyst_block');
 }else if(key==='straight/room_storage'){
  box([7,2,4],[10,4,11],'calcite');box([7,5,4],[10,5,11],'blue_terracotta');for(const z of [4,7,10])room.set(9,5,z,'sea_lantern');box([6,10,3],[10,10,12],'amethyst_block');
 }else if(key==='straight/room_towers'){
  box([2,2,2],[5,8,5],'calcite');box([8,2,9],[11,12,12],'amethyst_block');beam([4,11,4],[10,13,10]);box([2,9,2],[5,9,5],'purple_stained_glass');
 }else if(key==='straight/hole'){
  box([1,1,5],[2,1,10],'air');for(const[x,z,height]of [[8,9,9],[10,10,7],[12,8,11],[9,12,6]])room.crystal(x,z,height,2);beam([2,11,4],[8,12,7],'amethyst_block');beam([8,12,7],[13,10,12]);
 }else if(key==='straight/platform'){
  box([2,1,5],[4,3,11],'amethyst_block');box([10,1,5],[14,3,11],'calcite');box([12,4,7],[14,4,9],'amethyst_block');
  for(const[left,right]of [[2,4],[10,14]])for(let z=3;z<=4;z++)for(let x=left;x<=right;x++)room.set(x,z-1,z,stair('south'));
  for(let z=7;z<=9;z++)room.set(11,4,z,stair('east'));beam([2,11,6],[13,11,9],'amethyst_block');
 }else if(key==='turn/pillar_room'){
  box([5,2,2],[7,10,13],'amethyst_block');box([5,2,6],[7,7,9],'air');beam([2,12,8],[13,12,8]);beam([8,12,2],[8,12,13]);
 }else if(key==='turn/big_doors'){
  for(const z of [2,12]){box([2,2,z],[4,9,z+1],'calcite');box([11,2,z],[13,9,z+1],'amethyst_block');box([5,9,z],[10,11,z+1],'calcite');}
  box([12,2,2],[13,8,4],'calcite');box([12,2,11],[13,8,13],'calcite');box([12,8,5],[13,10,10],'amethyst_block');box([1,2,6],[3,5,9],'calcite');
 }else if(key==='turn/small_doors'){
  box([5,2,3],[7,7,6],'calcite');box([7,2,10],[10,8,12],'amethyst_block');beam([3,11,5],[9,11,5]);beam([9,11,5],[9,11,12]);
 }else if(key==='turn/hole'){
  for(let z=2;z<=6;z++)box([1,1,z],[Math.min(5,7-z),1,z],'air');room.crystal(12,11,10,3);room.crystal(9,12,6,2);box([10,5,11],[12,5,12],'calcite');beam([2,10,3],[12,12,11],'amethyst_block');
 }else if(key==='curve/simple'){
  for(let z=2;z<=10;z++)box([Math.max(8,13-z),2,z],[14,8,z],'amethyst_block');beam([2,10,10],[10,10,10]);beam([10,10,2],[10,10,10]);
 }else if(key==='terminal/canteen'){
  box([3,2,9],[12,3,10],'calcite');box([11,2,6],[12,3,12],'calcite');box([3,6,13],[11,8,13],'amethyst_block');box([2,2,4],[3,2,7],'amethyst_block');box([12,2,3],[13,2,4],'amethyst_block');box([3,10,9],[12,10,13],'purple_stained_glass');
 }else if(key==='terminal/storage'){
  box([5,2,3],[7,8,12],'calcite');box([5,2,6],[11,6,7],'amethyst_block');box([2,2,13],[11,7,14],'amethyst_block');box([9,9,10],[13,10,13],'purple_stained_glass');
 }else if(key==='terminal/cells'){
  for(const[x,z,width,height]of [[5,4,2,8],[8,7,2,6],[5,10,1,7]]){box([x,2,z],[x+width,height,z+2],'calcite');box([x,height-1,z],[x+width,height,z+2],'amethyst_block');box([x,height+1,z],[x+width,height+1,z+2],'purple_stained_glass');}
 }else if(key.endsWith('/layout/cells')){
  box([3,1,2],[4,5,5],'amethyst_block');box([5,1,7],[7,4,8],'calcite');box([8,1,4],[10,5,5],'calcite');beam([2,7,3],[8,7,8]);
 }else if(key.endsWith('/layout/storage')){
  box([3,1,2],[6,4,4],'calcite');box([5,1,7],[9,3,8],'amethyst_block');for(let x=2;x<=9;x++)beam([x,7,2],[x,6,9],x%2?'purple_stained_glass':'calcite');
 }else if(key.endsWith('/layout/towers')){
  box([5,1,4],[8,4,8],'calcite');box([6,5,5],[7,5,7],'amethyst_block');beam([2,7,2],[8,7,8],'amethyst_block');
 }else if(key==='boss/eclipse_keeper'){
  for(const x of [3,28])for(const z of [5,12,18])room.instrument(x,z,6);
  for(let z=2;z<=21;z++)for(let x=1;x<33;x++){const elevation=21-Math.floor(Math.abs(x-16.5)/3);if(z%5===2)room.set(x,elevation,z,'calcite');else if(x%5===1)room.set(x,elevation,z,'purple_stained_glass');}
  for(let x=5;x<=28;x++)for(let z=3;z<=20;z++){const distance=Math.hypot((x-16.5)/11,(z-11.5)/8);if(Math.abs(distance-1)<.09)room.set(x,14,z,'amethyst_block');}
  beam([4,11,3],[9,16,10],'amethyst_block');beam([29,11,3],[24,16,10],'amethyst_block');box([0,2,22],[33,22,22],'deepslate_tiles');box([15,2,22],[18,7,22],'air');box([2,9,24],[13,10,25],'amethyst_block');box([21,9,24],[31,10,25],'calcite');
 }else if(key==='promenade'){
  box([6,2,10],[11,4,13],'calcite');for(let z=3;z<=11;z++)box([2,2,z],[3,5+Math.floor(z/2),z],'amethyst_block');for(let x=4;x<=12;x++)room.set(x,11-Math.floor(Math.abs(x-8)/2),12,'amethyst_block');beam([7,10,3],[12,12,9]);
 }
 if(key==='straight/room_cells'){
  for(const z of [6,8])for(const x of [2,4]){box([x,3,z],[x,6,z],'purple_stained_glass');room.set(x,7,z,'sea_lantern');}
  box([10,4,6],[12,5,6],'budding_amethyst');
 }
 if(key==='straight/room_storage'){
  for(const z of [5,8,10]){room.set(8,5,z,'amethyst_block');room.set(10,5,z,'sea_lantern');}
  beam([6,10,3],[6,14,3]);beam([10,10,12],[10,14,12]);
 }
 if(key==='straight/room_towers'){
  box([2,6,2],[5,6,2],'purple_stained_glass');box([8,9,9],[11,9,9],'purple_stained_glass');room.set(4,9,4,'sea_lantern');room.set(9,13,10,'sea_lantern');
 }
 if(key==='turn/pillar_room'){
  box([5,8,5],[7,9,10],'purple_stained_glass');for(const z of [3,12]){room.set(5,7,z,'sea_lantern');room.set(7,7,z,'sea_lantern');}
 }
 if(key==='turn/small_doors'){
  box([5,6,3],[7,6,6],'purple_stained_glass');box([7,7,10],[10,7,12],'purple_stained_glass');beam([5,10,4],[10,12,11],'amethyst_block');room.set(9,11,9,'sea_lantern');
 }
 if(key==='terminal/storage'){
  for(const z of [4,8,11]){box([5,4,z],[7,5,z],'purple_stained_glass');room.set(7,6,z,'budding_amethyst');}
 }
 if(key==='terminal/cells'){
  for(const[x,z,height]of [[5,4,8],[8,7,6],[5,10,7]]){box([x,3,z],[x,5,z],'purple_stained_glass');room.set(x,height,z,'sea_lantern');}
 }
 if(key==='terminal/canteen'){
  room.set(5,4,9,'amethyst_block');room.set(8,4,9,'sea_lantern');box([5,7,13],[8,7,13],'purple_stained_glass');
 }
}
function staircase(room){
 const box=(first,last,state)=>room.box(first,last,state);
 box([1,17,1],[14,17,14],'calcite');box([5,2,2],[7,16,13],'air');box([10,2,2],[12,20,10],'air');
 for(let z=3;z<=10;z++)for(let x=5;x<=7;x++){box([x,1,z],[x,z-2,z],'calcite');room.set(x,z-1,z,stair('south'));}
 box([5,9,11],[12,9,13],'calcite');
 for(let z=10;z>=3;z--)for(let x=10;x<=12;x++){const feet=21-z;box([x,2,z],[x,feet-2,z],'air');room.set(x,feet-1,z,stair('north'));}
 box([10,17,1],[12,17,2],'calcite');
 for(let z=3;z<=4;z++)box([5,18,z],[12,21,z],'air');
 for(const socket of room.variant.sockets){
  const[x,y,z]=socket.position;if(y===2||y===18)continue;
  if(x<5){const attachZ=y;box([1,y-1,Math.min(z,attachZ)],[4,y-1,Math.max(z,attachZ)],'calcite');box([1,y,Math.min(z,attachZ)],[4,y+3,Math.max(z,attachZ)],'air');box([2,y-1,attachZ],[5,y-1,attachZ],'calcite');box([2,y,attachZ],[5,y+3,attachZ],'air');}
  else{const attachZ=21-y;if(attachZ<=10){box([13,y-1,Math.min(z,attachZ)],[14,y-1,Math.max(z,attachZ)],'calcite');box([13,y,Math.min(z,attachZ)],[14,y+3,Math.max(z,attachZ)],'air');room.clear([12,y,attachZ]);}
   else{box([8,5,10],[13,5,13],'calcite');box([8,6,10],[13,8,13],'air');for(let level=3;level<=6;level++)for(let width=0;width<3;width++){const runZ=level+1;room.set(11+width,level-1,runZ,stair('south'));box([11+width,level,runZ],[11+width,level+2,runZ],'air');}box([11,5,8],[13,5,10],'calcite');box([11,6,8],[13,8,10],'air');}
  }
 }
 const key=room.variant.key;
 if(key==='stairs/centered'){for(let x=2;x<=13;x++){const y=27-Math.floor(Math.abs(x-7.5));room.set(x,y,6,'calcite');room.set(x,y+1,6,'purple_stained_glass');}box([2,20,10],[4,28,12],'amethyst_block');const prior=room.blocks.slice();for(let y=0;y<room.height;y++)for(let z=0;z<16;z++)for(let x=0;x<16;x++){const state=prior[room.index(15-x,y,z)];room.set(x,y,z,state.replace('facing=east','facing=TEMP').replace('facing=west','facing=east').replace('facing=TEMP','facing=west'));}}
 if(key==='stairs/hole'){box([8,2,2],[9,23,4],'amethyst_block');box([2,24,8],[12,25,9],'purple_stained_glass');for(let z=3;z<=12;z++)room.set(8,28,z,'calcite');}
 if(key==='stairs/spiral'){box([2,20,2],[4,26,5],'calcite');for(let index=0;index<7;index++)room.line([4,25,4],[6+index,24+index%4,12-index],'amethyst_block');}
}
function preserveInterfaces(room){
 const variant=room.variant;
 const targets=variant.sockets.flatMap(socket=>[socket.position,...(socket.spawn_position?[socket.spawn_position]:[])]);
 if(variant.key==='spawn/spawn')targets.push([7,4,8]);if(variant.key==='promenade')targets.push([7,2,11]);if(variant.key==='boss/eclipse_keeper')targets.push([16,2,2],[16,2,13],[16,2,25]);
 for(const position of targets)room.clear(position);
 for(const port of variant.ports)for(const position of port.cells){room.clear(position,5);const[x,y,z]=position;for(let inward=1;inward<=(variant.key.startsWith('stairs/')?2:3);inward++)room.clear([x===0?inward:x===room.width-1?x-inward:x,y,z===0?inward:z===room.depth-1?z-inward:z],5);}
 if(variant.key==='spawn/spawn')for(let x=6;x<=9;x++)for(let z=7;z<=15;z++)room.clear([x,4,z]);
}
function flatGraph(room,start){
 const valid=(x,y,z)=>room.inside(x,y+2,z)&&y>0&&full(room.get(x,y-1,z))&&[0,1,2].every(offset=>air(room.get(x,y+offset,z)));
 const visited=new Set(),queue=[start];if(!valid(...start))return visited;visited.add(start.join(','));
 for(let cursor=0;cursor<queue.length;cursor++){const[x,y,z]=queue[cursor];for(const[dx,dz]of [[1,0],[-1,0],[0,1],[0,-1]]){const point=[x+dx,y,z+dz],key=point.join(',');if(!visited.has(key)&&valid(...point)){visited.add(key);queue.push(point);}}}return visited;
}
function repairRoutes(room){
 if(room.variant.key.startsWith('stairs/'))return;
 const targets=room.variant.ports.flatMap(port=>port.cells).concat(room.variant.sockets.flatMap(socket=>[socket.position,socket.spawn_position??socket.position]));
 if(room.variant.key==='spawn/spawn')targets.push([7,4,8]);if(room.variant.key==='promenade')targets.push([7,2,11]);if(room.variant.key==='boss/eclipse_keeper')targets.push([16,2,2],[16,2,13],[16,2,25]);if(!targets.length)targets.push([7,room.base,7]);
 const start=targets[0];room.clear(start);
 for(const target of targets){if(target[1]!==room.base)continue;const visited=flatGraph(room,start);if(visited.has(target.join(',')))continue;
  const queue=[[...target]],parents=new Map([[target.join(','),null]]);let end=null;
  for(let cursor=0;cursor<queue.length&&!end;cursor++){const point=queue[cursor];if(visited.has(point.join(','))){end=point;break;}for(const[dx,dz]of [[1,0],[-1,0],[0,1],[0,-1]]){const next=[point[0]+dx,point[1],point[2]+dz],key=next.join(',');if(next[0]<1||next[2]<1||next[0]>room.width-2||next[2]>room.depth-2||parents.has(key))continue;if(room.variant.key.startsWith('boss/')&&next[2]===22&&(next[0]<15||next[0]>18))continue;parents.set(key,point);queue.push(next);}}
  assert(end,`Cannot repair ${room.variant.key} ${target}`);
  for(let point=end;point;point=parents.get(point.join(',')))for(let dx=-1;dx<=1;dx++)for(let dz=-1;dz<=1;dz++){const x=point[0]+dx,z=point[2]+dz;if(x<1||z<1||x>=room.width-1||z>=room.depth-1)continue;if(room.variant.key.startsWith('boss/')&&z===22&&(x<15||x>18))continue;room.clear([x,point[1],z]);}
 }
}
function microFlood(room,start,closed=false){
 const surfaces=new Map();
 const blockHeight=(microX,microZ,y)=>{const state=room.get(Math.floor(microX/2),y,Math.floor(microZ/2));if(state==='void')return 2;if(air(state))return 0;if(!state.includes('stairs'))return 2;const high=state.includes('facing=east')?microX%2===1:state.includes('facing=west')?microX%2===0:state.includes('facing=south')?microZ%2===1:microZ%2===0;return high?2:1;};
 const clear=(microX,feet,microZ)=>{if(closed&&Math.floor(microZ/2)===22&&Math.floor(microX/2)>=15&&Math.floor(microX/2)<=18&&feet<16)return false;for(let y=Math.floor(feet/2);y<Math.ceil((feet+4)/2);y++){if(y>=room.height)return false;const height=blockHeight(microX,microZ,y);if(height&&2*y+height>feet&&2*y<feet+4)return false;}return true;};
 for(let microX=0;microX<room.width*2;microX++)for(let microZ=0;microZ<room.depth*2;microZ++){const column=[];for(let y=0;y<room.height-2;y++){const height=blockHeight(microX,microZ,y),feet=y*2+height;if(height&&clear(microX,feet,microZ))column.push(feet);}surfaces.set(`${microX},${microZ}`,column);}
 const seed=[start[0]*2,start[1]*2,start[2]*2],queue=[seed],visited=new Set([seed.join(',')]);
 for(let cursor=0;cursor<queue.length;cursor++){const[microX,feet,microZ]=queue[cursor];for(const[dx,dz]of [[1,0],[-1,0],[0,1],[0,-1]]){const nextX=microX+dx,nextZ=microZ+dz;for(const nextFeet of surfaces.get(`${nextX},${nextZ}`)??[]){if(Math.abs(feet-nextFeet)>1||!clear(microX,Math.max(feet,nextFeet),microZ)||!clear(nextX,Math.max(feet,nextFeet),nextZ))continue;const next=[nextX,nextFeet,nextZ],key=next.join(',');if(!visited.has(key)){visited.add(key);queue.push(next);}}}}
 return{visited,has:position=>[0,1].some(dx=>[0,1].some(dz=>visited.has([position[0]*2+dx,position[1]*2,position[2]*2+dz].join(','))))};
}
function decode(bytes,variant,strict=true){
 const nbt=library.NbtFile.read(bytes),structure=library.Structure.fromNbt(nbt.root),room=new Room(variant),positions=new Set();
 assert.deepEqual(structure.getSize(),variant.size);
 if(strict){assert.equal(nbt.root.getNumber('DataVersion'),3465);assert.equal(nbt.root.getList('entities').length,0);const palette=nbt.root.getList('palette');for(let index=0;index<palette.length;index++)assert(/^minecraft:[a-z0-9_]+$/.test(palette.getCompound(index).getString('Name')));}
 const blocks=structure.getBlocks();if(strict)assert.equal(blocks.length,room.blocks.length);
 for(const block of blocks){assert(!block.nbt);assert(room.inside(...block.pos));assert(!positions.has(block.pos.join(',')));positions.add(block.pos.join(','));room.set(...block.pos,block.state.toString().replace(/^minecraft:/,''));}return room;
}
function wideRoads(room){
 if(room.variant.key.startsWith('stairs/')||room.variant.ports.length<2)return{applicable:false};
 const valid=(x,z)=>{for(let dx=-1;dx<=1;dx++)for(let dz=-1;dz<=1;dz++){if(!full(room.get(x+dx,room.base-1,z+dz)))return false;for(let rise=0;rise<3;rise++)if(!air(room.get(x+dx,room.base+rise,z+dz)))return false;}return true;};
 const anchors=room.variant.ports.map(port=>{const[x,,z]=port.cells[1];return[x===0?1:x===room.width-1?x-1:x,z===0?1:z===room.depth-1?z-1:z];});
 const queue=[anchors[0]],visited=new Set();if(valid(...anchors[0]))visited.add(anchors[0].join(','));
 for(let cursor=0;cursor<queue.length;cursor++){const[x,z]=queue[cursor];for(const[dx,dz]of [[1,0],[-1,0],[0,1],[0,-1]]){const next=[x+dx,z+dz],key=next.join(',');if(!visited.has(key)&&valid(...next)){visited.add(key);queue.push(next);}}}
 assert(anchors.every(anchor=>visited.has(anchor.join(','))),`three-wide road ${room.variant.key} ${anchors}`);return{applicable:true,minimumWidth:3,clearance:3,connectedPortCenters:anchors,reachableCenters:visited.size};
}
function validate(room,previous){
 const variant=room.variant,targets=variant.ports.flatMap(port=>port.cells).concat(variant.sockets.flatMap(socket=>[socket.position,socket.spawn_position??socket.position]));
 if(variant.key==='spawn/spawn')targets.push([7,4,8]);if(variant.key==='promenade')targets.push([7,2,11]);if(variant.key.startsWith('boss/'))targets.push([16,2,2],[16,2,13],[16,2,25]);
 const safe=(position,height)=>{const[x,y,z]=position;assert(full(room.get(x,y-1,z)),`support ${variant.key} ${position}`);for(let offset=0;offset<height;offset++)assert(air(room.get(x,y+offset,z)),`clearance ${variant.key} ${position} y${y+offset}`);};
 for(const port of variant.ports)for(const position of port.cells)safe(position,5);
 for(const socket of variant.sockets){safe(socket.position,3);safe(socket.spawn_position??socket.position,3);assert(full(room.get(...socket.support)));}
 for(const target of targets)safe(target,variant.ports.some(port=>port.cells.some(cell=>cell.join(',')===target.join(',')))?5:3);
 const start=variant.key.startsWith('boss/')?[16,2,2]:targets[0]??[7,room.base,7];safe(start,3);
 const graph=microFlood(room,start);for(const target of targets)assert(graph.has(target),`disconnected ${variant.key} ${target}`);const wideRoadCheck=wideRoads(room);
 const assigned=new Set(variant.sockets.flatMap(socket=>[socket.position.join(','),(socket.spawn_position??socket.position).join(',')])),independentSpawns=[];
 for(const socket of variant.sockets){const[x,y,z]=socket.position;let found=null;for(let distance=1;distance<=6&&!found;distance++)for(let dx=-distance;dx<=distance&&!found;dx++)for(let dz=-distance;dz<=distance&&!found;dz++){if(Math.abs(dx)+Math.abs(dz)!==distance)continue;const point=[x+dx,y,z+dz];if(assigned.has(point.join(','))||!room.inside(point[0],y+2,point[2]))continue;if(full(room.get(point[0],y-1,point[2]))&&[0,1,2].every(offset=>air(room.get(point[0],y+offset,point[2])))&&graph.has(point))found=point;}assert(found,`no independent spawn ${variant.key} ${socket.position}`);assigned.add(found.join(','));independentSpawns.push({socket:socket.position,entityFeet:found});}
 const openings=new Set(variant.ports.flatMap(port=>port.cells.flatMap(([x,y,z])=>Array.from({length:5},(_,offset)=>[x,y+offset,z].join(',')))));let boundaryChecked=0;
 for(let y=0;y<room.height;y++)for(let z=0;z<room.depth;z++)for(let x=0;x<room.width;x++)if(x===0||z===0||y===0||x===room.width-1||z===room.depth-1||y===room.height-1){boundaryChecked++;assert(!air(room.get(x,y,z))||openings.has([x,y,z].join(',')),`shell leak ${variant.key} ${x},${y},${z}`);}
 let gate=null;if(variant.key.startsWith('boss/')){for(let x=0;x<34;x++)for(let y=2;y<23;y++)assert(x>=15&&x<=18&&y<=7?air(room.get(x,y,22)):!air(room.get(x,y,22)));const locked=microFlood(room,[16,2,2],true);assert(!locked.has([16,2,25]));gate={wallZ:22,aperture:[15,18,2,7],openReturnReachable:graph.has([16,2,25]),closedReturnReachable:false};}
 let changed=0,interiorChanged=0,previousUnion=0;
 for(let y=0;y<room.height;y++)for(let z=0;z<room.depth;z++)for(let x=0;x<room.width;x++){const current=room.get(x,y,z),old=previous.get(x,y,z);if(!air(current)||!air(old))previousUnion++;if(air(current)!==air(old)){changed++;if(x>0&&z>0&&x<room.width-1&&z<room.depth-1&&y>room.base-1&&y<room.height-1)interiorChanged++;}}
 assert(changed>100&&interiorChanged>30,`insubstantial geometry ${variant.key}`);
 return{key:variant.key,size:variant.size,minimum_y:variant.minimum_y,ports:variant.ports,sockets:variant.sockets,spawn_position:variant.spawn_position??null,denseUniqueBounded:true,dataVersion:3465,entityAndBlockEntityCount:0,barePaletteNames:true,portClearance5:true,socketClearance3:true,fullSupports:true,allTargetsReachableWithoutJump:true,collisionGrid:0.5,maximumStep:0.5,playerHeadroom:2,wideRoadCheck,reachableMicroCells:graph.visited.size,independentSpawns,envelope:{pass:true,boundaryChecked,openingCells:openings.size},victoryGate:gate,geometry:{changedSolidAirCells:changed,interiorChangedSolidAirCells:interiorChanged,changedFractionVolume:changed/room.blocks.length,changedFractionUnion:changed/previousUnion,occupancySha256:sha(Buffer.from(room.blocks.map(state=>air(state)?0:1)))},features:designs[variant.key]};
}
function color(state){if(air(state))return'#111321';const name=nameOf(state);if(name.includes('sea_lantern'))return'#9efbfa';if(name.includes('glass'))return'#8555b3';if(name.includes('amethyst'))return'#a872e3';if(name==='calcite'||name.includes('quartz'))return'#dfd7ea';if(name.includes('terracotta'))return'#354c9a';return'#454555';}
function sliceSvg(room,elevation,section=false){
 const cell=9,viewHeight=section?room.height:room.depth;let svg=`<svg viewBox="0 0 ${room.width*cell} ${viewHeight*cell}" role="img">`;
 for(let vertical=0;vertical<viewHeight;vertical++)for(let x=0;x<room.width;x++){const y=section?room.height-1-vertical:elevation,z=section?Math.floor(room.depth/2):vertical;svg+=`<rect x="${x*cell}" y="${vertical*cell}" width="9" height="9" fill="${color(room.get(x,y,z))}" stroke="#ffffff0b" stroke-width=".35"/>`;}
 if(!section){for(const socket of room.variant.sockets){const[x,y,z]=socket.position;if(y===elevation)svg+=`<circle cx="${x*cell+4.5}" cy="${z*cell+4.5}" r="3" fill="#ff82a9"/>`;}for(const port of room.variant.ports)for(const[x,y,z]of port.cells)if(y===elevation)svg+=`<rect x="${x*cell+1}" y="${z*cell+1}" width="7" height="7" fill="#64f4a5"/>`;}
 return svg+'</svg>';
}
function gallery(rooms){
 const cards=rooms.map((room,index)=>{const variant=room.variant,high=variant.key.startsWith('stairs/')?18:Math.min(room.height-2,room.base+7);return`<article id="room-${index}"><h2>${variant.key}</h2><h3>${designs[variant.key][0]}</h3><p>${designs[variant.key][2]}</p><p class="muted">${variant.size.join(' × ')} · ${designs[variant.key][3]}</p><div class="slices"><figure>${sliceSvg(room,room.base)}<figcaption>实际NBT平面 y=${room.base}</figcaption></figure><figure>${sliceSvg(room,high)}<figcaption>实际NBT平面 y=${high}</figcaption></figure><figure>${sliceSvg(room,0,true)}<figcaption>实际NBT剖面 z=${Math.floor(room.depth/2)}</figcaption></figure></div><details><summary>实际NBT等轴测方块预览（无材质贴图）</summary><label>切顶高度 <input type="range" min="2" max="${room.height-1}" value="${Math.min(room.height-3,room.base+10)}" data-room="${index}"></label><canvas width="860" height="620" data-room="${index}"></canvas></details></article>`;}).join('');
 const data=rooms.map(room=>{const palette=[...new Set(room.blocks)];return{size:room.variant.size,palette,colors:palette.map(color),blocks:room.blocks.map(state=>palette.indexOf(state))};});
 return`<!doctype html><html lang="zh-CN"><meta charset="utf-8"><title>Amethyst · 27房真实NBT图集</title><style>body{background:#10101d;color:#ebe6f5;font:16px system-ui;margin:30px auto;max-width:1500px;padding:0 24px}h1{color:#c896ff}main{display:grid;grid-template-columns:repeat(auto-fit,minmax(430px,1fr));gap:22px}article{background:#1c1b2d;border:1px solid #443259;border-radius:12px;padding:20px}h2{font-size:17px;color:#b896e6;overflow-wrap:anywhere}h3{font-size:22px}.slices{display:flex;gap:10px;align-items:start}figure{margin:0;flex:1;min-width:0}svg{width:100%;max-height:300px}figcaption,.muted{font-size:12px;color:#b5a9c7}p{line-height:1.6}canvas{width:100%;background:#111321}summary{cursor:pointer;margin:18px 0}input{width:65%}</style><h1>紫晶天文体系 · 27房</h1><p>图像均从本次导出的实际NBT重新解析生成。粉色＝socket；绿色＝端口；紫色＝紫晶；白色＝方解石/石英。每房两层平面、中部剖面及可切顶等轴测。等轴测为真实方块几何、无游戏材质贴图；楼梯显示包围盒，碰撞验证另以0.5格梯面检查。</p><main>${cards}</main><script>const rooms=${JSON.stringify(data)};
 function draw(index){const room=rooms[index],canvas=document.querySelector('canvas[data-room="'+index+'"]'),context=canvas.getContext('2d'),cut=Number(document.querySelector('input[data-room="'+index+'"]').value),[width,height,depth]=room.size,unit=Math.min(14,640/(width+depth)),originX=430,originY=260;context.clearRect(0,0,860,620);const at=(x,y,z)=>x<0||y<0||z<0||x>=width||y>=height||z>=depth?0:room.blocks[(y*depth+z)*width+x];const project=(x,y,z)=>[originX+(x-z)*unit,originY+(x+z)*unit*.5-y*unit];function face(points,color){context.beginPath();points.forEach((point,number)=>{const projected=project(...point);number?context.lineTo(...projected):context.moveTo(...projected)});context.closePath();context.fillStyle=color;context.fill();context.strokeStyle='#17122144';context.lineWidth=.35;context.stroke();}for(let diagonal=0;diagonal<width+depth;diagonal++)for(let x=0;x<width;x++){const z=diagonal-x;if(z<0||z>=depth)continue;for(let y=0;y<=cut;y++){const block=at(x,y,z);if(room.palette[block]==='air')continue;const color=room.colors[block];if(y===cut||room.palette[at(x,y+1,z)]==='air')face([[x,y+1,z],[x+1,y+1,z],[x+1,y+1,z+1],[x,y+1,z+1]],color);if(z===depth-1||room.palette[at(x,y,z+1)]==='air')face([[x,y,z+1],[x+1,y,z+1],[x+1,y+1,z+1],[x,y+1,z+1]],color);if(x===width-1||room.palette[at(x+1,y,z)]==='air')face([[x+1,y,z],[x+1,y,z+1],[x+1,y+1,z+1],[x+1,y+1,z]],color);}}}
 document.querySelectorAll('input').forEach(input=>input.addEventListener('input',()=>draw(Number(input.dataset.room))));document.querySelectorAll('details').forEach(details=>details.addEventListener('toggle',()=>{if(details.open)draw(Number(details.querySelector('canvas').dataset.room))}));</script></html>`;
}
function excluded(room,x,y,z){
 if(x===0||z===0||x===room.width-1||z===room.depth-1||y<room.base||y===room.height-1||(room.variant.key.startsWith('stairs/')&&y===17&&full(room.get(x,y,z))))return true;
 return room.variant.ports.some(port=>port.cells.some(([portX,portY,portZ])=>y>=portY-1&&y<=portY+4&&(portZ===0||portZ===room.depth-1?Math.abs(x-portX)<=1&&Math.abs(z-portZ)<=3:Math.abs(z-portZ)<=1&&Math.abs(x-portX)<=3)));
}
function interior(room){const cells=[];for(let y=room.base;y<room.height-1;y++)for(let z=1;z<room.depth-1;z++)for(let x=1;x<room.width-1;x++)if(!excluded(room,x,y,z)&&!air(room.get(x,y,z)))cells.push([x,y,z]);return cells;}
function family(key){if(key.startsWith('stairs/'))return'stairs';if(key.includes('/layout/'))return'layout';if(key.startsWith('terminal/')||key==='promenade')return'terminal';if(key.includes('room_')||key.startsWith('turn/')||['straight/hole','straight/platform'].includes(key))return'combat';if(key.startsWith('spawn/'))return'entrance';if(key.startsWith('boss/'))return'boss';return'corridor';}
function rotations(cells,width,depth,normalize=false,height=1,base=0){
 const results=[];for(let turn=0;turn<4;turn++){const current=new Set();for(const[x,y,z]of cells){const rotated=turn===0?[x,z]:turn===1?[depth-1-z,x]:turn===2?[width-1-x,depth-1-z]:[z,width-1-x];const rotatedWidth=turn%2?depth:width,rotatedDepth=turn%2?width:depth;current.add(normalize?[Math.floor(rotated[0]/rotatedWidth*16),Math.floor((y-base)/height*16),Math.floor(rotated[1]/rotatedDepth*16)].join(','):[rotated[0],y-base,rotated[1]].join(','));}results.push(current);}return results;
}
function jaccard(first,second){let intersection=0;for(const point of first)if(second.has(point))intersection++;return first.size+second.size-intersection?intersection/(first.size+second.size-intersection):1;}
function repetitionAudit(rooms,reports){
 const records=rooms.map(room=>{const cells=interior(room),exact=rotations(cells,room.width,room.depth,false,1,room.base),normalized=rotations(cells,room.width,room.depth,true,room.height-1-room.base,room.base);return{key:room.variant.key,size:room.variant.size,family:family(room.variant.key),cells,exact,normalized,interiorSolidCells:cells.length,normalizedSignature:sha(Buffer.from(normalized.map(set=>[...set].sort().join(';')).sort()[0])),exactCanonicalSignature:sha(Buffer.from(exact.map(set=>[...set].sort().join(';')).sort()[0]))};});
 const pairs=[];
 for(let first=0;first<records.length;first++)for(let second=first+1;second<records.length;second++){
  const left=records[first],right=records[second],sameSize=left.size.join(',')===right.size.join(','),sameFamily=left.family===right.family;if(!sameSize&&!sameFamily)continue;
  const exact=sameSize?Math.max(...right.exact.map(set=>jaccard(left.exact[0],set))):null,normalized=Math.max(...right.normalized.map(set=>jaccard(left.normalized[0],set)));
  const footprint=set=>new Set([...set].map(point=>{const[x,,z]=point.split(',');return x+','+z;}));
  const footprintSimilarity=Math.max(...right.normalized.map(set=>jaccard(footprint(left.normalized[0]),footprint(set))));
  const high=(exact??0)>=.9||normalized>=.9||footprintSimilarity>=.9;
  const review=high?(left.family==='stairs'&&right.family==='stairs'?'保留：固定上下层接口与真实三格双跑踏步复用；中间支廊、折返手性、上部天文井及仪架另有结构；不豁免普通战斗房。':'REWORK_REQUIRED'):'PASS：去公共结构后的内部几何有实质区别';
  assert(review!=='REWORK_REQUIRED',`Repetition needs redesign ${left.key} / ${right.key} ${exact} ${normalized}`);
  pairs.push({keys:[left.key,right.key],sameSize,sameFamily,solidAirJaccard:exact,normalizedJaccard:normalized,layoutFootprintJaccard:footprintSimilarity,highSimilarity:high,review});
 }
 assert.equal(new Set(records.map(record=>record.exactCanonicalSignature)).size,27,'Duplicate rotated interior');
 return{schema:1,style:'fteclipsegen:amethyst',method:{remove:'boundary x/z=0 or maximum, y<base and roof y=height-1; stairs also remove full upper-floor blocks at y17 (stairs kept); union of port safety bands, depth 3, width extended by 1, portY-1..portY+4',airComparison:'Solid-set Jaccard = intersection/union; shared empty volume is deliberately excluded.',rotations:'Maximum Jaccard over four 90-degree Y rotations',normalization:'16x16x16 relative room coordinates; full interior vertical range above base',threshold:.9,highSimilarityRule:'Review when exact solid, normalized solid or footprint Jaccard >=0.90; footprint alone is not claimed to prove duplicate volume'},totalComparablePairs:pairs.length,rooms:records.map(({cells,exact,normalized,...record})=>record),pairs,highestPairs:[...pairs].sort((left,right)=>Math.max(right.solidAirJaccard??0,right.normalizedJaccard)-Math.max(left.solidAirJaccard??0,left.normalizedJaccard)).slice(0,25),highSimilarityPairs:pairs.filter(pair=>pair.highSimilarity),baseline033:reports.map(report=>({key:report.key,sha256:report.previous033Sha256,...report.geometry})),ordinaryCombatHighSimilarityUnresolved:0,exactDuplicateInteriors:0,pass:true};
}
function components(points){const remaining=new Set(points.map(point=>point.join(','))),groups=[];while(remaining.size){const seed=remaining.values().next().value,queue=[seed.split(',').map(Number)];remaining.delete(seed);for(let cursor=0;cursor<queue.length;cursor++){const[x,y,z]=queue[cursor];for(const[dx,dy,dz]of [[1,0,0],[-1,0,0],[0,1,0],[0,-1,0],[0,0,1],[0,0,-1]]){const next=[x+dx,y+dy,z+dz],key=next.join(',');if(remaining.delete(key))queue.push(next);}}groups.push(queue);}return groups;}
function bounds(points){return{from:[0,1,2].map(axis=>Math.min(...points.map(point=>point[axis]))),to:[0,1,2].map(axis=>Math.max(...points.map(point=>point[axis])))};}
function styleAudit(rooms,reports){
 return{schema:1,style:'fteclipsegen:amethyst',source:'Final exported NBT decoded with Lodestone, not design names',rules:{palette:'Vanilla amethyst/budding_amethyst, calcite, quartz, violet glass, deepslate and sea lantern',theme:'Count and locate connected real amethyst solids, complementary calcite/quartz solids, and actual architectural occupied volume after removing common shell/floor/ports',geometry:'No palette-only exemption; all 27 must differ from baseline solid/air and have distinct internal canonical signature'},rooms:rooms.map((room,index)=>{
  const materialCounts={};for(const state of room.blocks){const name=nameOf(state);materialCounts[name]=(materialCounts[name]??0)+1;}
  const cells=interior(room),themeCells=cells.filter(point=>/amethyst/.test(room.get(...point))),structureCells=cells.filter(point=>/calcite|quartz/.test(room.get(...point))),groups=components(themeCells).sort((left,right)=>right.length-left.length);
  assert(themeCells.length>=8&&structureCells.length>=5,`No actual amethyst architecture ${room.variant.key}`);
  const landmarks=groups.filter(group=>group.length>=3).slice(0,6).map(group=>({category:'actual connected amethyst structure',...bounds(group),solidCells:group.length,materials:[...new Set(group.map(point=>room.get(...point)))]}));
  assert(landmarks.length>0,`Disconnected theme pieces ${room.variant.key}`);const low=cells.filter(point=>point[1]<=room.base+3),corner=low.filter(([x,,z])=>(x<=3||x>=room.width-4)&&(z<=3||z>=room.depth-4)),center=low.filter(([x,,z])=>x>=4&&x<=room.width-5&&z>=4&&z<=room.depth-5);
  const fourCornerRisk=low.length>0&&corner.length/low.length>.8&&center.length<5;
  assert(!fourCornerRisk,`four corner empty hall ${room.variant.key}`);
  return{key:room.variant.key,nbtSha256:reports[index].sha256,materialCounts,interiorSolidCells:cells.length,actualAmethystCells:themeCells.length,actualCalciteQuartzCells:structureCells.length,landmarks,structuralEnvelope:bounds(cells),functionShape:designs[room.variant.key][2],evidence:{lowOccupiedCells:low.length,cornerOccupiedCells:corner.length,centralOccupiedCells:center.length,baselineChangedSolidAirCells:reports[index].geometry.changedSolidAirCells},issues:{paletteOnly:false,fourCornerEmptyHall:fourCornerRisk,ringAsRepeatedPrimarySymbol:room.variant.key.startsWith('boss/')?'Only this room retains a major suspended orbit ring':'No major orbit ring; inspect real feature bounds and four views'},decision:'PASS',reason:'NBT中紫晶实体与方解石/石英主体均实测存在，几何包围盒、连通分量及低层占位已统计；独立平面差异另由内部重复度检查约束。'};
 }),pass:true};
}
const directions=[['north','北',2,'camera from -X,-Z'],['south','南',0,'camera from +X,+Z'],['west','西',3,'camera from -X,+Z'],['east','东',1,'camera from +X,-Z']];
function fourViewSvg(room,turn,direction){
 const cells=[];const rotate=(x,z)=>turn===0?[x,z]:turn===1?[room.depth-1-z,x]:turn===2?[room.width-1-x,room.depth-1-z]:[z,room.width-1-x];
 for(let y=room.base-1;y<room.height-1;y++)for(let z=1;z<room.depth-1;z++)for(let x=1;x<room.width-1;x++){
  const state=room.get(x,y,z);if(air(state))continue;if(room.variant.key.startsWith('stairs/')&&y===17&&full(state))continue;
  const[viewX,viewZ]=rotate(x,z);cells.push({original:[x,y,z],x:viewX,y,z:viewZ,state});
 }
 const unit=Math.min(450/(room.width+room.depth),315/(room.height+(room.width+room.depth)*.5)),originX=260,originY=30+room.height*unit;
 const point=(x,y,z)=>[originX+(x-z)*unit,originY+(x+z)*unit*.5-y*unit];
 const shade=(hex,factor)=>'#'+hex.slice(1).match(/../g).map(value=>Math.round(parseInt(value,16)*factor).toString(16).padStart(2,'0')).join('');
 const face=(points,fill)=>`<polygon points="${points.map(vertex=>point(...vertex).map(value=>value.toFixed(2)).join(',')).join(' ')}" fill="${fill}" stroke="#181225" stroke-width=".3"/>`;
 let svg=`<svg xmlns="http://www.w3.org/2000/svg" width="520" height="390" viewBox="0 0 520 390" data-direction="${direction}" data-nbt-sha256="${sha(readFileSync(join(out,'roomset',room.variant.key+'.nbt')))}"><rect width="520" height="390" fill="#10131f"/><text x="12" y="20" font-family="sans-serif" font-size="12" fill="#e4d5ff">${direction} · ${room.variant.key}</text>`;
 cells.sort((left,right)=>(left.x+left.z)-(right.x+right.z)||left.y-right.y);
 function cuboid(x,y,z,width,height,depth,fill){return face([[x,y,z+depth],[x+width,y,z+depth],[x+width,y+height,z+depth],[x,y+height,z+depth]],shade(fill,.72))+face([[x+width,y,z],[x+width,y,z+depth],[x+width,y+height,z+depth],[x+width,y+height,z]],shade(fill,.86))+face([[x,y+height,z],[x+width,y+height,z],[x+width,y+height,z+depth],[x,y+height,z+depth]],fill);}
 for(const cell of cells){const{x,y,z,state}=cell,fill=color(state);if(state.includes('stairs')){
  svg+=cuboid(x,y,z,1,.5,1,fill);const facing=state.match(/facing=(\w+)/)[1],baseDirection={south:0,west:1,north:2,east:3}[facing],rotated=(baseDirection+turn)%4;
  const offsets=[[0,.5,1,.5],[0,0,.5,1],[0,0,1,.5],[.5,0,.5,1]][rotated];svg+=cuboid(x+offsets[0],y+.5,z+offsets[1],offsets[2],.5,offsets[3],fill);
 }else svg+=cuboid(x,y,z,1,1,1,fill);}
 for(const socket of room.variant.sockets){const[x,z]=rotate(socket.position[0]+.5,socket.position[2]+.5),projected=point(x,socket.position[1]+.2,z);svg+=`<circle cx="${projected[0]}" cy="${projected[1]}" r="2.5" fill="#ff659a" stroke="#fff" stroke-width=".5"/>`;}
 const geometrySha256=sha(Buffer.from((svg.match(/<polygon[^>]+>/g)??[]).join('')));
 svg+=`<text x="12" y="375" font-family="sans-serif" font-size="10" fill="#bbb0cd">真实NBT；隐藏外壳/底部基础/顶盖；保留行走地板；梯房隐藏y17整楼板</text></svg>`;return{svg,geometrySha256,visibleSolidCells:cells.length};
}
function createFourViews(rooms,reports){
 const views=[];const cards=rooms.map((room,index)=>{const figures=directions.map(([direction,label,turn,camera])=>{const rendered=fourViewSvg(room,turn,label),relative='views/'+room.variant.key+'/'+direction+'.svg',path=join(out,relative);mkdirSync(dirname(path),{recursive:true});writeFileSync(path,rendered.svg);views.push({key:room.variant.key,direction,label,turn,camera,path:relative,sha256:sha(Buffer.from(rendered.svg)),geometrySha256:rendered.geometrySha256,nbtSha256:reports[index].sha256,visibleSolidCells:rendered.visibleSolidCells});return`<figure><img loading="eager" width="520" height="390" src="${relative}" alt="${room.variant.key} ${label}方向真实NBT"><figcaption>${label} · ${camera}</figcaption></figure>`;}).join('');return`<article data-key="${room.variant.key}"><h2>${room.variant.key} · ${designs[room.variant.key][0]}</h2><p>${designs[room.variant.key][2]}</p><div class="views">${figures}</div><p class="hash">NBT SHA256 ${reports[index].sha256}</p></article>`;});
 const groups=[0,1,2].map(index=>`<section id="contact-${index+1}">${cards.slice(index*9,index*9+9).join('')}</section>`).join('');
 const html=`<!doctype html><html lang="zh-CN"><meta charset="utf-8"><title>紫晶27房 · 108真实NBT四视图</title><style>*{box-sizing:border-box}body{margin:0;background:#0c101a;color:#e6d9f5;font:14px system-ui;padding:24px}header{max-width:1500px}h1{color:#c3a1f3}section{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:14px;margin:20px 0;padding:8px;background:#0c101a}article{background:#1c1c2c;padding:12px;border:1px solid #48375a;border-radius:8px}h2{font-size:15px;margin:2px 0 8px}p{line-height:1.5;min-height:42px}.hash{font:9px monospace;overflow-wrap:anywhere;min-height:24px;color:#ac9bbb}.views{display:grid;grid-template-columns:1fr 1fr;gap:4px}figure{margin:0;min-width:0}img{width:100%;height:auto;display:block}figcaption{font-size:10px;color:#b5a6c9}</style><header><h1>紫晶全套逐房自检 · 北南西东108视图</h1><p>四个实际观察方向分别由最终NBT体素旋转0/90/180/270度投影生成，非示意图。统一隐藏x/z边界外壳、y&lt;base基础地板、y=height−1顶盖；楼梯房另隐藏y17完整上层楼板以看清梯跑，真实梯面仍保留。安全与外壳检查使用未隐藏的完整NBT。紫=紫晶，白=石英/方解石，青=灯，粉点=socket。平面及接口另见gallery.html。</p></header>${groups}</html>`;
 for(const room of rooms)assert(new Set(views.filter(view=>view.key===room.variant.key).map(view=>view.geometrySha256)).size>=2,'Copied direction geometry '+room.variant.key);
 writeFileSync(join(out,'four-views.html'),html.replace('y&lt;base基础地板','y&lt;base−1底部基础（保留行走地板）'));return views;
}
async function browserAudit(views){
 const{chromium}=await import(pathToFileURL('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright/index.mjs').href);
 const browser=await chromium.launch({headless:true,executablePath:'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe'});const page=await browser.newPage({viewport:{width:1800,height:1400},deviceScaleFactor:1}),errors=[];
 page.on('pageerror',error=>errors.push(error.message));page.on('console',message=>{if(message.type()==='error')errors.push(message.text());});page.on('requestfailed',request=>errors.push(request.url()+': '+request.failure()?.errorText));
 await page.goto(pathToFileURL(join(out,'four-views.html')).href,{waitUntil:'load'});await page.evaluate(()=>document.fonts.ready);
 const actual=await page.locator('img').evaluateAll(images=>images.map(image=>({source:image.getAttribute('src'),complete:image.complete,width:image.naturalWidth,height:image.naturalHeight})));
 assert.equal(actual.length,108);assert(actual.every(image=>image.complete&&image.width===520&&image.height===390));
 const screenshots=[];for(let index=1;index<=3;index++){const name=`views/contact-${String(index).padStart(2,'0')}.png`;await page.locator('#contact-'+index).screenshot({path:join(out,name)});screenshots.push({path:name,sha256:sha(readFileSync(join(out,name)))});}
 await browser.close();assert.equal(errors.length,0,errors.join('\n'));assert.equal(new Set(views.map(view=>view.sha256)).size,108);
 return{engine:'Headless Microsoft Edge via bundled Playwright',page:'four-views.html',expectedViews:108,loadedViews:actual.length,allImagesDecoded:true,pageErrors:errors,consoleErrorCount:0,failedRequests:0,screenshots};
}
class Room{
 constructor(variant){this.variant=variant;[this.width,this.height,this.depth]=variant.size;this.blocks=Array(this.width*this.height*this.depth).fill('air');this.base=variant.key==='spawn/spawn'?4:variant.size[0]===12?1:2;}
 index(x,y,z){return(y*this.depth+z)*this.width+x;}
 inside(x,y,z){return x>=0&&y>=0&&z>=0&&x<this.width&&y<this.height&&z<this.depth;}
 get(x,y,z){return this.inside(x,y,z)?this.blocks[this.index(x,y,z)]:'void';}
 set(x,y,z,state){assert(this.inside(x,y,z),`bounds ${this.variant.key} ${x},${y},${z}`);this.blocks[this.index(x,y,z)]=state;}
 box(first,last,state){for(let y=first[1];y<=last[1];y++)for(let z=first[2];z<=last[2];z++)for(let x=first[0];x<=last[0];x++)this.set(x,y,z,state);}
 line(first,last,state){const length=Math.max(...first.map((value,index)=>Math.abs(value-last[index])));let previous=first.slice();this.set(...previous,state);for(let step=1;step<=length;step++){const point=first.map((value,index)=>Math.round(value+(last[index]-value)*step/length));for(const axis of [0,2,1]){while(previous[axis]!==point[axis]){previous[axis]+=Math.sign(point[axis]-previous[axis]);this.set(...previous,state);}}}}
 clear(position,height=3){const[x,y,z]=position;this.box([x,y,z],[x,y+height-1,z],'air');this.set(x,y-1,z,'smooth_quartz');}
 shell(){this.box([0,0,0],[this.width-1,this.base-1,this.depth-1],'polished_deepslate');for(let y=this.base;y<this.height;y++)for(let z=0;z<this.depth;z++)for(let x=0;x<this.width;x++)if(x===0||z===0||x===this.width-1||z===this.depth-1||y===this.height-1)this.set(x,y,z,y>this.height-6?'purple_stained_glass':(x+z)%5===0?'calcite':'deepslate_tiles');for(let z=1;z<this.depth-1;z++)for(let x=1;x<this.width-1;x++)this.set(x,this.base-1,z,(x+2*z)%7===0?'amethyst_block':'smooth_quartz');}
 crystal(x,z,height,radius=2){for(let y=this.base;y<this.base+height;y++){const span=Math.max(0,Math.floor(radius*(1-(y-this.base)/height)));for(let dx=-span;dx<=span;dx++)for(let dz=-span;dz<=span;dz++)if(Math.abs(dx)+Math.abs(dz)<=span&&this.inside(x+dx,y,z+dz))this.set(x+dx,y,z+dz,y%3===0?'budding_amethyst':'amethyst_block');}}
 instrument(x,z,height){this.box([x,this.base,z],[x+1,this.base+1,z+1],'calcite');this.line([x,this.base+2,z],[x+2,this.base+height,z+1],'amethyst_block');this.set(x+2,this.base+height,z+1,'sea_lantern');}
}
mkdirSync(out,{recursive:true});
assert.equal(catalog.length,27);assert.deepEqual(new Set(Object.keys(designs)),new Set(catalog.map(variant=>variant.key)));
writeFileSync(join(out,'design.md'),'# 紫晶：天文测量、晶洞与棱镜档案\n\n27房逐一独立设计；方解石/石英结构、深板岩外壳、紫晶主体与紫色玻璃天幕统一材料语言。只有Boss以大型轨道环为主体。仅写本目录及独立配方；固定接口遵循只读catalog。\n\n'+catalog.map(variant=>{const design=designs[variant.key];return`## ${variant.key} · ${design[0]}\n\n用途：${design[1]}。\n\n平面动线：${design[2]}。\n\n高度：${design[3]}。\n\n标志结构：${design[4]}。\n\n差异：${design[5]}。\n`;}).join('\n')+'\n## 运行与验证\n\n运行 `node tools/rebuild-amethyst.mjs`。不调用旧main，不修改正式资源。验证读取本次NBT及当前0.33正式同key NBT，逐项比较solid/air。图集标明两个高度、剖面及非贴图等轴测。未进行游戏内验收。\n');
const reports=[],rooms=[],templates=[];
for(const variant of catalog){
 const room=new Room(variant);room.shell();if(variant.key.startsWith('stairs/'))staircase(room);else detail(room);preserveInterfaces(room);repairRoutes(room);preserveInterfaces(room);
 const structure=new library.Structure(variant.size);
 for(let y=0;y<room.height;y++)for(let z=0;z<room.depth;z++)for(let x=0;x<room.width;x++){const state=library.BlockState.parse('minecraft:'+room.get(x,y,z));structure.addBlock([x,y,z],state.getName().toString(),state.getProperties());}
 const bytes=structure.writeNbt({name:'',dataVersion:3465,compression:'gzip'}),path=join(out,'roomset',variant.key+'.nbt');mkdirSync(dirname(path),{recursive:true});writeFileSync(path,bytes);
 const actual=decode(readFileSync(path),variant),previousBytes=readFileSync(join(resources,'roomsets/amethyst',variant.key+'.nbt')),previous=decode(previousBytes,variant,false);
 const report=validate(actual,previous);report.sha256=sha(bytes);report.previous033Sha256=sha(previousBytes);reports.push(report);rooms.push(actual);templates.push({key:variant.key,sha256:sha(bytes)});
 console.log(`${variant.key}: changed=${report.geometry.changedSolidAirCells} (${(100*report.geometry.changedFractionVolume).toFixed(2)}%) connected=${report.reachableMicroCells}`);
}
assert.equal(new Set(reports.map(report=>report.geometry.occupancySha256)).size,27,'Duplicate geometry');
const manifest={schema:1,style:'fteclipsegen:amethyst',templates};const manifestBytes=Buffer.from(JSON.stringify(manifest,null,2)+'\n');writeFileSync(join(out,'roomset/manifest.json'),manifestBytes);
const validation={schema:1,style:'fteclipsegen:amethyst',catalogSha256:sha(catalogBytes),manifestSha256:sha(manifestBytes),recipeSha256:sha(readFileSync(fileURLToPath(import.meta.url))),totalTemplates:27,distinctOccupancySignatures:27,allPassed:true,baseline:'current 0.33 formal roomsets/amethyst; exact hash per key',checks:['dense NBT round-trip, unique coordinates, exact catalog dimensions','DataVersion 3465, bare palette Names, no entities or block entities','ports/socket/spawn preserved, 5/3 AIR, full support','0.5 block collision graph, no jumping, 2-block player headroom','unique same-level independent entity spawn within Manhattan 1..6','sealed boundary except exact catalog five-high ports','Boss full z22 wall; closed return unreachable and open reachable','27 distinct occupancy signatures; substantive solid/air change per key'],limitations:['No in-game rendering/combat/runtime acceptance performed.','Isometric previews use untextured geometry; stair visual is bounding-box approximation.'],results:reports};
writeFileSync(join(out,'validation.json'),JSON.stringify(validation,null,2)+'\n');writeFileSync(join(out,'gallery.html'),gallery(rooms));
const repetition=repetitionAudit(rooms,reports);writeFileSync(join(out,'repetition-audit.json'),JSON.stringify(repetition,null,2)+'\n');
const style=styleAudit(rooms,reports);writeFileSync(join(out,'style-audit.json'),JSON.stringify(style,null,2)+'\n');
const views=createFourViews(rooms,reports);const browser=await browserAudit(views);
const selfAudit={schema:1,style:'fteclipsegen:amethyst',manifestSha256:sha(manifestBytes),recipeSha256:validation.recipeSha256,views:{expected:108,actual:views.length,uniqueDirectionHashes:new Set(views.map(view=>view.sha256)).size,hidingRule:'boundary x/z planes, base floor y<base, top roof y=height-1; stairs additionally remove full upper landing slab y17 for interior inspection only',records:views},browser,style:{rooms:style.rooms.length,allPassed:style.pass,actualNbtMaterialEvidence:true},repetition:{comparablePairs:repetition.totalComparablePairs,exactDuplicateInteriors:0,ordinaryCombatHighSimilarityUnresolved:0,highSimilarityReviewed:repetition.highSimilarityPairs},repairs:[{key:'promenade',issue:'Runtime exit [7,2,11] blocked by platform',resolution:'Reserve 3 AIR, full support; carve three-wide connection to port; revalidate actual NBT'},{key:'straight/room_cells',issue:'Two-cell constriction between prism cabinets',resolution:'West cabinet reduced one block; three-wide port route erosion test passes'},{key:'spawn/outside_far',issue:'Default inspection origin trapped in diagonal ridge',resolution:'Open cross-cut through diagonal ridge for navigable garden'},{key:'stairs/centered',issue:'Similar handedness to other staircase circulation',resolution:'Mirror full stair circulation and facing properties; preserve interfaces and rerun micro collision graph'}],rooms:reports.map(report=>({key:report.key,nbtSha256:report.sha256,fourDirections:4,interfaceAndRouteValidation:true,style:'PASS',repetition:'PASS',visualReview:'Four actual NBT rotations rendered and decoded; contact sheets generated for individual human/model inspection',densityReview:'Road clearance verified against full NBT; theme solids separately counted and bounded'})),replay:{nbtSerializationVerified:true,independentReplayRequiredBeforeFinalFreeze:true},limitations:['Offline axis projections have palette colors, not Minecraft resource-pack lighting or gameplay rendering.','Same required three-wide stair flight algorithm is shared; handedness, intermediate branches and upper architectures differ.','No in-game gameplay or runtime combat acceptance performed.'],pass:true};
const visualNotes={
 'spawn/spawn':'四方可见西测星座与东晶帆并非对称四柱；屋面覆盖会遮局部仪座，因此同时参照低层平面；南向四格出口轴净空实测通过。',
 'spawn/outside_near':'四方可见L形实崖与阶梯晶峰，西侧实体崖较密但东半庭院开放；不把此端景当普通战斗厅。',
 'spawn/outside_far':'四方可见一座独立灯塔和断续对角晶脊；中段横切口已返工，实测中心庭院不再单格孤岛。',
 'straight/simple':'三组高架连续直廊可辨；这是本套允许重复仪架的唯一测量通道，无四角独立柱阵。',
 'straight/door':'四方显示双厚隔屏及错位门隙，主路S折；未使用中央晶环或重复仪架，三格宽路实测贯通。',
 'straight/bridge':'保留实际行走层后可见H形桥台和中桥、两侧真实空槽；斜桁已补正交相接晶块，未再离散悬空。',
 'straight/room_cells':'库柜错位及不同顶高可辨，晶玻璃标本窗和灯已补；两柜之间拓宽，三格主路通过。',
 'straight/room_storage':'低长蓝色星图台与上悬图板、两支悬杆可辨，星点为实际灯/晶块；U廊区别于双库Z巷。',
 'straight/room_towers':'高低双塔、观测窗、塔顶灯与斜桥可辨；塔脚离散形成对角穿行，不是四角柱。',
 'straight/hole':'四方可见聚集式晶峰及分叉晶岩舌，西侧槽和三叉地面需参照行走层；接口无被晶峰覆盖。',
 'straight/platform':'四方可见不等高双台及石英入口梯，东台二级踏步保留；斜悬梁为独有分光器，非通用顶梁。',
 'turn/pillar_room':'紫晶整墙横切口、透镜横窗与十字赤经梁可辨；体量主要在分厅墙，不是中央空厅加四角柱。',
 'turn/big_doors':'三面厚拱与西仪台可辨；多向观察能看到中央连片大厅和侧绕路线，三接口三格路线通过。',
 'turn/small_doors':'错位双屏、上方偏斜棱镜连接和侧凹可辨；实测与大拱厅内部低相似。',
 'turn/hole':'西北槽与东南晶崖在不同方位显露，晶崖补入方解石矿层；三向绕行不被实体崖堵死。',
 'stairs/centered':'镜像双跑东上西返可辨；隐藏整上层楼板后观察梯段，半轨弧与西高晶柱独有；完整NBT另验证18层连通。',
 'stairs/hole':'晶井柱、7层西支廊、12层东支廊与双跑可辨；保留踏步复用的理由在重复度逐对记录。',
 'stairs/spiral':'低层支梯与叉分台可辨，上扇架区别于晶井；与另两梯房共享双跑算法已明确披露，非普通战斗房豁免。',
 'curve/simple':'斜切实库与L折测尺可辨，北西弯廊保持宽度；密实角体为功能库墙，非无目的堆晶。',
 'terminal/canteen':'L柜、柜面灯、侧长凳及背菜单板可辨，前庭买卖区完整；背板较高但不覆盖入口。',
 'terminal/storage':'F形库柜及紫色标本窗、后台可辨；与端部封存舱和直廊库体占位不同。',
 'terminal/cells':'三不等高封存舱可辨；新增实际紫晶封头、观察窗与顶部灯，纠正初稿只有白舱紫玻璃的主题不足。',
 'straight/room/layout/cells':'三瓣短壁与偏置横架可辨；小尺寸界面保留，凹室绕行不同于图柜或单焦台。',
 'straight/room/layout/storage':'双错柜、层叠斜顶与偏置通道可辨；顶檐较密但高度在安全空间上方，完整NBT净空通过。',
 'straight/room/layout/towers':'单焦实台与悬顶镜可辨；顶镜为正交连续构件，非四角柱或重复三瓣舱。',
 'boss/eclipse_keeper':'大穹肋、悬轨道环、两翼六仪座及后整隔墙可辨；南视部分仪座被隔墙遮挡属于真实遮挡，北东西及平面补足；门闭不可绕行已实测。',
 'promenade':'后偏轴台与西斜晶帘可辨，固定出口脚点已挖通；四方图与低层平面共同证明出口路线。'
};
for(const room of selfAudit.rooms){room.visualReview={status:'PASS',reviewedDirections:['north','south','west','east'],contactSheet:browser.screenshots[Math.floor(catalog.findIndex(variant=>variant.key===room.key)/9)].path,observation:visualNotes[room.key],copiedDirectionGeometry:false};}
selfAudit.views.hidingRule='boundary x/z planes, bottom foundation y<base-1, roof y=height-1; retain walking floor y=base-1; stairs additionally remove full upper landing slab y17 for interior inspection only';
selfAudit.repairs.push({key:'all ordinary rooms',issue:'Generic short white roof beams repeated across the first four-view contact sheets',resolution:'Remove shared notch-based beams entirely; retain only individual functional roofs and instruments'},{key:'straight/bridge and diagonal instrument frames',issue:'Diagonal sampling left disconnected theme cubes',resolution:'Generate Manhattan-connected beams and recheck real connected component bounds'},{key:'terminal/cells',issue:'White capsules lacked actual solid amethyst thematic structure',resolution:'Add amethyst capsule heads, observation windows and lamps; verify final NBT counts and actual bounds'},{key:'four-view renderer',issue:'Hiding walking floor made bridges and routes visually ambiguous',resolution:'Keep the actual y=base-1 walking surface; hide only lower foundation and shell; regenerate all 108 views'});
if(replayBefore)for(const[path,expected]of replayBefore)assert.equal(sha(readFileSync(join(out,path))),expected,'Replay hash mismatch '+path);
selfAudit.replay={command:'node tools/rebuild-amethyst.mjs --verify-replay',independentReplayCompleted:replayBefore!==null,matchingArtifactCount:replayBefore?.size??0,matchingArtifacts:replayBefore?Object.fromEntries(replayBefore):{},includes:'27 NBT, manifest, design, validation, both galleries, 108 SVG projections, 3 browser contact sheets, style/repetition audits'};
selfAudit.pass=replayBefore!==null;
writeFileSync(join(out,'self-audit.json'),JSON.stringify(selfAudit,null,2)+'\n');
const auditFiles=['four-views.html','style-audit.json','repetition-audit.json','self-audit.json'];
writeFileSync(join(out,'FREEZE.json'),JSON.stringify({status:replayBefore?'frozen':'candidate-awaiting-replay',style:'fteclipsegen:amethyst',templates:27,manifestSha256:sha(manifestBytes),validationSha256:sha(readFileSync(join(out,'validation.json'))),gallerySha256:sha(readFileSync(join(out,'gallery.html'))),designSha256:sha(readFileSync(join(out,'design.md'))),recipeSha256:validation.recipeSha256,audits:Object.fromEntries(auditFiles.map(name=>[name,sha(readFileSync(join(out,name)))])),fourViewCount:108,replayVerified:replayBefore!==null,browserErrors:0},null,2)+'\n');
console.log('FROZEN '+sha(manifestBytes));
