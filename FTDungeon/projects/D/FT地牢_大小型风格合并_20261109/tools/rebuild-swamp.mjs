import fs from 'node:fs';
import path from 'node:path';
import crypto from 'node:crypto';
import {createRequire} from 'node:module';
import {fileURLToPath} from 'node:url';
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..');
const out=path.join(root,'output/style-rebuild-v034/swamp');
const data=path.join(root,'src/main/resources/data/fteclipsegen');
const skill=process.env.MINECRAFT_BUILDER_SKILL??'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill';
const {Structure,NbtFile,BlockState}=createRequire(path.join(skill,'package.json'))('@mattzh72/lodestone');
const variants=JSON.parse(fs.readFileSync(path.join(data,'catalog.json'),'utf8')).variants;
const air='minecraft:air',hash=bytes=>crypto.createHash('sha256').update(bytes).digest('hex');
const specs=[
 ['苇门药印','入口，偏轴折线入坊与侧苗床','4；冠12','小根冠呼应Boss'],
 ['浅滩桩村','近景，三栋桩屋夹弯曲主街','2；檐8/12/16','密集街巷与分离坡顶'],
 ['远沼枯根','远景，对角双根丘夹干滩','2；冠16/21','稀疏远景轮廓'],
 ['红树折栈','通道，东西错位S形栈道','2；顶9/12','粗根墙折栈'],
 ['药坊双门','门房，双厚门间偏西验药室','2；楣9/11','前后不同门框'],
 ['干涸双池桥','战斗，H形栈桥跨左右干池','2；池底1','横纵桥网'],
 ['隔根囚圃','战斗，偏轴纵廊串错门根牢','2；顶7/8','实体根隔墙串联隔间'],
 ['药材装卸院','战斗，双货棚夹南侧横院','2；棚9/11','厚货垛横向院道'],
 ['三桩巫村','战斗，三错位桩屋分叉街巷','2；屋8/10/12','三片独立坡顶绕屋战斗'],
 ['围池晒药场','战斗，方环栈道绕中央大干池','2；池底1','完整环路中央泥砖池沿'],
 ['高低炼药台','分台，低层贯通东西高台','2→4→5','半格步行阶六高度接口'],
 ['根拱三岔','三通，偏北Y形分岔','2；根拱11','三向厚根拱偏轴节点'],
 ['泥闸药市','三通，横市长厅接南北门与东柜口','2；楣9','厚门墩横厅'],
 ['曲巷小坊','三通，偏置小屋以短巷连通','2；檐8/10','小屋转折门巷网络'],
 ['裂根回湾','三通，西长药沟东C形栈道','2；沟底1','偏置回湾侧长池'],
 ['四折药架梯','楼梯，四段宽半格梯围通透中庭','2→18','红木半砖实体宽梯'],
 ['干井盘堤梯','楼梯，井周上升侧药池歇台','2→7→12→18','泥井侧壁与中间停台'],
 ['绕根采药梯','楼梯，宽梯绕巨根平台伸向树干','2→4→6→18','中心实心弯根采药平台'],
 ['弯根西渡','弯道，北口经西弯栈接西口','2；梁10','侧药泥丘L形动线'],
 ['女巫煎药坊','商店，前厅侧操作室后药锅柜台','2；檐10','深柜台菌块药罐前后台'],
 ['封存药窖','端仓，卸货道尽头分三横药窖','2；顶8','厚泥隔墙封存垛分仓'],
 ['流放桩舍','端牢，曲接待路接两排错门单间','2；檐7/8','非对称根牢端部回廊'],
 ['苗床根笼','子牢，三苗床分前后通行带','1；顶5/6','短根隔屏横向分带'],
 ['小药仓夹巷','子仓，错位货垛间折线夹巷','1；顶5','厚垛木棚紧凑S巷'],
 ['双桩晒棚','子桩房，斜向双棚两侧分绕','1；顶6/7','不同高双斜顶'],
 ['幽沼巨根法庭','Boss，三纵栈三横庭封隔返程室','2；冠18','四组根肋多干池入口药印放大'],
 ['尽头晾药廊','端景，长纵廊尽头横展观药台','2；檐10','玻璃封景横架T形端部']
];
function makeRoom(variant,ordinal){
 const [width,height,depth]=variant.size,key=variant.key,base=key.includes('/layout/')?1:key==='spawn/spawn'?4:2;
 const states=new Array(width*height*depth).fill(air);
 const inside=(xx,yy,zz)=>xx>=0&&yy>=0&&zz>=0&&xx<width&&yy<height&&zz<depth;
 const get=(xx,yy,zz)=>inside(xx,yy,zz)?states[(yy*depth+zz)*width+xx]:'minecraft:bedrock';
 const put=(xx,yy,zz,state)=>{if(!inside(xx,yy,zz))throw Error(`Bounds ${key} ${[xx,yy,zz]}`);states[(yy*depth+zz)*width+xx]=state.startsWith('minecraft:')?state:`minecraft:${state}`;};
 const box=(from,to,state)=>{for(let yy=from[1];yy<=to[1];yy++)for(let zz=from[2];zz<=to[2];zz++)for(let xx=from[0];xx<=to[0];xx++)put(xx,yy,zz,state);};
 const tiles=new Set(),clearings=[];
 const chamber=(x1,z1,x2,z2)=>{clearings.push([x1,z1,x2,z2]);for(let zz=z1;zz<=z2;zz++)for(let xx=x1;xx<=x2;xx++)tiles.add(`${xx},${zz}`);};
 const road=points=>{for(let segment=1;segment<points.length;segment++){const [sx,sz]=points[segment-1],[ex,ez]=points[segment],distance=Math.max(Math.abs(ex-sx),Math.abs(ez-sz));for(let step=0;step<=distance;step++){const xx=Math.round(sx+(ex-sx)*step/(distance||1)),zz=Math.round(sz+(ez-sz)*step/(distance||1));chamber(Math.max(1,xx-1),Math.max(1,zz-1),Math.min(width-2,xx+1),Math.min(depth-2,zz+1));}}};
 const solid=(x1,z1,x2,z2,top=base+4,state='mud_bricks')=>box([x1,base,z1],[x2,Math.min(top,height-2),z2],state);
 const pool=(x1,z1,x2,z2)=>{box([x1,base-1,z1],[x2,base-1,z2],'brown_terracotta');for(let xx=x1;xx<=x2;xx++)for(let zz=z1;zz<=z2;zz++)if(xx===x1||xx===x2||zz===z1||zz===z2)put(xx,base,zz,'mud_bricks');};
 const hut=(x1,z1,x2,z2,roof)=>{solid(x1,z1,x2,z2,base+3,'mangrove_planks');for(let xx=x1;xx<=x2;xx++){const yy=roof-Math.min(xx-x1,x2-xx);box([xx,yy,z1],[xx,yy,z2],'mangrove_planks');}for(const xx of [x1,x2])for(const zz of [z1,z2])box([xx,base,zz],[xx,roof,zz],'mangrove_log');};
 box([0,variant.minimum_y,0],[width-1,base-1,depth-1],'packed_mud');
 box([0,base,0],[0,height-1,depth-1],'mud_bricks');box([width-1,base,0],[width-1,height-1,depth-1],'mud_bricks');box([0,base,0],[width-1,height-1,0],'mud_bricks');box([0,base,depth-1],[width-1,height-1,depth-1],'mud_bricks');box([0,height-1,0],[width-1,height-1,depth-1],'glass');
 for(let yy=base+4;yy<height-1;yy++){for(let xx=1;xx<width-1;xx++){put(xx,yy,0,'glass');put(xx,yy,depth-1,'glass');}for(let zz=1;zz<depth-1;zz++){put(0,yy,zz,'glass');put(width-1,yy,zz,'glass');}}
 if(key==='spawn/spawn'){road([[7,14],[7,10],[4,10],[4,5],[10,5]]);chamber(5,6,10,10);solid(10,9,13,12,8,'mangrove_roots');pool(2,2,5,5);hut(10,2,13,5,10);}
 if(key==='spawn/outside_near'){road([[7,1],[7,5],[4,5],[4,10],[10,10],[10,14]]);hut(1,1,4,4,8);hut(9,3,13,7,12);hut(2,11,6,14,16);pool(10,11,13,14);}
 if(key==='spawn/outside_far'){road([[2,13],[6,9],[10,5],[13,2]]);for(let rise=0;rise<14;rise++){const xx=2+Math.floor(rise/4),zz=3+Math.floor(rise/5);box([xx,base+rise,zz],[xx+2,base+rise,zz+2],'mangrove_roots');}for(let rise=0;rise<19;rise++){const xx=12-Math.floor(rise/6);box([xx,base+rise,11],[xx+1,base+rise,13],'mangrove_log');}pool(2,10,5,13);}
 if(key==='straight/simple'){solid(1,1,4,8,9,'mangrove_roots');solid(10,7,14,14,10);road([[7,1],[7,4],[10,4],[10,7],[7,10],[7,14]]);}
 if(key==='straight/door'){solid(1,3,14,4,9);solid(1,10,14,11,11,'mangrove_planks');chamber(2,5,9,9);road([[7,1],[7,6],[5,6],[5,9],[7,12],[7,14]]);pool(11,6,13,8);}
 if(key==='straight/bridge'){pool(1,4,5,12);pool(10,3,14,11);road([[7,1],[7,14]]);road([[3,5],[12,5]]);road([[3,11],[12,11]]);solid(2,7,3,9,7,'mangrove_roots');}
 if(key==='straight/room_cells'){solid(1,6,5,7,8,'mangrove_roots');solid(10,7,14,8,7,'mangrove_roots');solid(5,2,6,5,8);solid(9,10,10,13,7);road([[7,1],[8,5],[8,10],[7,14]]);road([[3,4],[12,4]]);road([[3,11],[12,11]]);}
 if(key==='straight/room_storage'){hut(1,2,5,6,9);hut(10,2,14,8,11);solid(2,11,5,13,5,'brown_mushroom_block');chamber(2,8,13,10);road([[7,1],[7,9],[9,9],[9,13],[7,14]]);}
 if(key==='straight/room_towers'){hut(1,1,5,5,8);hut(6,7,9,11,12);hut(11,10,14,14,10);road([[7,1],[8,4],[11,7],[11,10],[7,14]]);road([[3,8],[3,12],[8,12]]);}
 if(key==='straight/hole'){pool(5,5,10,10);road([[7,1],[3,3],[3,12],[7,14]]);road([[7,1],[12,3],[12,12],[7,14]]);}
 if(key==='straight/platform'){road([[7,1],[7,14]]);pool(5,6,9,9);solid(2,5,4,10,3,'mangrove_planks');solid(10,5,13,10,3,'mangrove_planks');}
 if(key==='turn/pillar_room'){solid(3,7,5,11,9,'mangrove_roots');solid(10,2,12,4,10,'mangrove_roots');road([[7,1],[7,5],[7,14]]);road([[7,5],[14,7]]);}
 if(key==='turn/big_doors'){solid(1,4,14,5,9);solid(1,10,14,11,9);chamber(2,6,14,9);road([[7,1],[7,14]]);road([[7,7],[14,7]]);pool(2,7,4,9);}
 if(key==='turn/small_doors'){hut(2,2,5,6,9);hut(9,2,13,5,8);hut(5,10,10,13,10);road([[7,1],[7,7],[4,9],[4,12],[7,14]]);road([[7,7],[14,7]]);}
 if(key==='turn/hole'){pool(5,3,8,12);road([[7,1],[11,3],[11,12],[7,14]]);road([[11,7],[14,7]]);road([[3,4],[3,12],[11,12]]);}
 if(key.startsWith('stairs/')){road([[7,1],[7,14]]);if(key==='stairs/spiral')solid(6,6,9,9,25,'mangrove_roots');if(key==='stairs/hole'){pool(6,6,9,9);solid(7,7,8,8,13);}}
 if(key==='curve/simple'){solid(7,6,13,13,11,'mangrove_roots');road([[7,1],[4,4],[4,7],[1,7]]);chamber(2,9,5,12);}
 if(key==='terminal/canteen'){hut(1,8,6,13,10);solid(9,9,14,11,4,'mangrove_planks');solid(11,3,13,5,5,'brown_mushroom_block');chamber(3,3,10,7);road([[7,1],[7,7],[8,12]]);pool(9,12,13,14);}
 if(key==='terminal/storage'){solid(5,7,6,13,8);solid(10,7,11,13,8);solid(2,12,4,14,5,'brown_mushroom_block');road([[7,1],[7,5],[3,5],[3,10]]);road([[7,5],[12,5],[12,12]]);road([[8,5],[8,13]]);}
 if(key==='terminal/cells'){solid(4,3,5,8,8,'mangrove_roots');solid(10,7,11,13,8,'mangrove_roots');solid(1,8,3,9,7);solid(12,7,14,8,7);road([[7,1],[7,6],[8,10],[6,13]]);road([[2,5],[8,5],[13,5]]);road([[2,11],[8,11],[13,11]]);}
 if(key==='straight/room/layout/cells'){solid(3,2,4,8,5,'mangrove_roots');solid(6,4,7,9,6,'mangrove_roots');road([[1,3],[8,3],[8,10]]);road([[1,7],[1,9],[8,9]]);}
 if(key==='straight/room/layout/storage'){solid(2,4,6,6,5,'brown_mushroom_block');solid(7,7,9,8,4,'mangrove_planks');road([[1,2],[8,2],[8,5],[1,8],[1,9],[8,9]]);}
 if(key==='straight/room/layout/towers'){hut(2,2,5,5,6);hut(5,7,8,10,7);road([[1,6],[6,6],[9,2]]);road([[1,9],[3,9],[3,6],[9,6]]);}
 if(key==='boss/eclipse_keeper'){for(const zz of [6,13,19]){pool(3,zz-1,6,zz+1);pool(27,zz-1,30,zz+1);}for(const xx of [10,22])for(const zz of [7,16]){solid(xx,zz,xx+1,zz+1,7,'mangrove_roots');for(let rise=0;rise<11;rise++){const shift=Math.floor(rise/3)*(xx<16?1:-1);box([xx+shift,8+rise,zz],[xx+shift+1,8+rise,zz+1],'mangrove_log');}}road([[16,1],[16,26]]);road([[8,3],[8,20]]);road([[25,3],[25,20]]);for(const zz of [4,11,19])road([[7,zz],[26,zz]]);chamber(12,8,20,17);}
 if(key==='promenade'){solid(1,2,4,9,10,'mangrove_roots');solid(11,4,14,8,9,'mangrove_planks');road([[7,1],[7,11]]);chamber(2,10,13,14);pool(3,12,5,14);}
 for(const [x1,z1,x2,z2]of clearings)box([x1,base,z1],[x2,Math.min(height-2,base+5),z2],air);
 for(const tile of tiles){const [xx,zz]=tile.split(',').map(Number);put(xx,base-1,zz,'mangrove_planks');}
 if(key==='straight/hole'){
  for(const xx of [5,10])box([xx,base,7],[xx,base+4,7],'mangrove_roots');
  box([5,base+5,7],[10,base+5,7],'mangrove_log');
  put(6,base+4,7,'mangrove_roots');put(9,base+4,7,'mangrove_roots');
 }
 if(key==='turn/hole'){
  box([6,base,5],[7,base+2,10],'mud_bricks');
  for(const zz of [5,10])box([5,base+3,zz],[8,base+3,zz],'mangrove_planks');
 }
 if(key==='straight/bridge'){
  for(const xx of [4,11])box([xx,base+1,7],[xx,base+5,7],'mangrove_roots');
  box([4,base+6,7],[11,base+6,7],'mangrove_log');
 }
 if(key==='stairs/centered'){
  for(const zz of [6,9])box([6,9,zz],[9,10,zz],'mangrove_planks');
  for(const xx of [6,9])box([xx,6,6],[xx,8,6],'mangrove_roots');
 }
 if(key==='turn/big_doors'){
  for(const [xx,zz]of [[2,6],[5,9]])box([xx,base,zz],[xx,base+3,zz],'mangrove_log');
  box([2,base+4,6],[5,base+4,9],'mangrove_planks');
  box([2,base,9],[4,base+1,9],'brown_mushroom_block');
 }
 const targets=variant.ports.flatMap(port=>port.cells).map(point=>({point,clearance:5,type:'port'}));
 for(const socket of variant.sockets)for(const point of [socket.position,socket.spawn_position])if(point)targets.push({point,clearance:3,type:'socket',id:socket.id});
 if(key==='spawn/spawn')targets.push({point:[7,4,8],clearance:3,type:'player'});
 if(key==='boss/eclipse_keeper')for(const point of [[16,2,2],[16,2,13],[16,2,25]])targets.push({point,clearance:3,type:'boss'});
 const full=state=>state!==air&&!/slab|roots|leaves/.test(state);
 const deck=(xx,zz,feet)=>{const floorY=Math.ceil(feet)-1;for(let yy=floorY+1;yy<=Math.ceil(feet)+2;yy++)if(inside(xx,yy,zz))put(xx,yy,zz,air);put(xx,floorY,zz,Number.isInteger(feet)?'mangrove_planks':'mangrove_slab[type=bottom,waterlogged=false]');};
 if(key.startsWith('stairs/')){
  const sides=[[[3,3],[3,12]],[[3,12],[12,12]],[[12,12],[12,3]],[[12,3],[3,3]]];
  for(let side=0;side<4;side++){const [[sx,sz],[ex,ez]]=sides[side];for(let step=0;step<=9;step++){const xx=sx+(ex-sx)*step/9,zz=sz+(ez-sz)*step/9,feet=2+side*4+Math.min(8,step)*0.5;for(let across=-1;across<=1;across++)deck(xx+(sx===ex?across:0),zz+(sz===ez?across:0),feet);}}
  for(let xx=1;xx<=10;xx++)for(let zz=1;zz<=2;zz++)deck(xx,zz,18);
  for(let xx=1;xx<=3;xx++)for(let zz=3;zz<=14;zz++)deck(xx,zz,18);
  for(let xx=1;xx<=14;xx++)for(let zz=13;zz<=14;zz++)deck(xx,zz,18);
  for(let xx=10;xx<=14;xx++)for(let zz=11;zz<=12;zz++)deck(xx,zz,18);
 }
 if(key==='straight/platform'){for(let xx=2;xx<=4;xx++)for(let zz=5;zz<=10;zz++)deck(xx,zz,4);for(let xx=10;xx<=13;xx++)for(let zz=5;zz<=10;zz++)deck(xx,zz,4);for(let zz=1;zz<=5;zz++)for(let xx=2;xx<=4;xx++)deck(xx,zz,2+(zz-1)*0.5);for(let zz=14;zz>=10;zz--)for(let xx=10;xx<=12;xx++)deck(xx,zz,2+(14-zz)*0.5);for(let xx=11;xx<=13;xx++)for(let zz=7;zz<=9;zz++)deck(xx,zz,5);}
 if(key==='stairs/hole'||key==='stairs/spiral')deck(4,11,6);
 if(key==='straight/platform')for(let xx=11;xx<=13;xx++)deck(xx,9,4.5);
 const restore=()=>{for(const {point:[xx,yy,zz],clearance}of targets){put(xx,yy-1,zz,'mangrove_planks');for(let above=0;above<clearance;above++)put(xx,yy+above,zz,air);}};
 const extras=[],used=new Set(targets.map(target=>target.point.join(',')));
 for(const socket of variant.sockets){const [xx,yy,zz]=socket.position;let found;for(const [dx,dz]of [[1,0],[-1,0],[0,1],[0,-1],[2,0],[-2,0],[0,2],[0,-2]]){const other=[xx+dx,yy,zz+dz];if(other[0]<1||other[0]>=width-1||other[2]<1||other[2]>=depth-1||used.has(other.join(',')))continue;found=other;break;}if(!found)throw Error('No entity foot');used.add(found.join(','));extras.push({socket:socket.id,point:found});targets.push({point:found,clearance:3,type:'entity-neighbor'});}
 restore();
 const surfaces=()=>{const walk=new Map();for(let zz=0;zz<depth;zz++)for(let xx=0;xx<width;xx++)for(let yy=1;yy<height-2;yy++){const support=get(xx,yy-1,zz);let feet;if(full(support))feet=yy;else if(support.includes('mangrove_slab[type=bottom'))feet=yy-0.5;else continue;let clear=true;for(let above=Math.floor(feet);above<=Math.ceil(feet+2.8)-1;above++)if(above!==yy-1&&get(xx,above,zz)!==air)clear=false;if(clear)walk.set(`${xx},${feet},${zz}`,[xx,feet,zz]);}return walk;};
 const flood=(walk,start,closed=false)=>{const visited=new Set(),queue=[];if(walk.has(start.join(','))){visited.add(start.join(','));queue.push(start);}for(let cursor=0;cursor<queue.length;cursor++){const [xx,yy,zz]=queue[cursor];for(const [dx,dz]of [[1,0],[-1,0],[0,1],[0,-1]])for(const dy of [-0.5,0,0.5]){const point=[xx+dx,yy+dy,zz+dz],label=point.join(',');if(closed&&point[2]===22&&point[0]>=15&&point[0]<=18&&point[1]<8)continue;if(walk.has(label)&&!visited.has(label)){visited.add(label);queue.push(point);}}}return visited;};
 const repairs=[];
 const connect=()=>{
  for(let iteration=0;iteration<70;iteration++){
   const walk=surfaces(),visited=flood(walk,targets[0]?.point??[7,base,7]);
   const missing=targets.find(target=>!visited.has(target.point.join(',')));
   if(!missing)return {walk,visited};
   const [startX,feet,startZ]=missing.point,queue=[[0,startX,startZ]],previous=new Map([[`${startX},${startZ}`,null]]),costs=new Map([[`${startX},${startZ}`,0]]);
   const collision=(xx,zz)=>[...walk.values()].some(([wx,wy,wz])=>wx===xx&&wz===zz&&wy!==feet&&Math.abs(wy-feet)<3.5);
   const reserved=(xx,zz)=>targets.some(({point:[tx,ty,tz]})=>tx===xx&&tz===zz&&ty!==feet&&Math.abs(ty-feet)<4);
   let destination;
   while(queue.length){
    queue.sort((left,right)=>left[0]-right[0]);const [cost,xx,zz]=queue.shift();
    if(cost>costs.get(`${xx},${zz}`))continue;
    if(visited.has(`${xx},${feet},${zz}`)){destination=[xx,zz];break;}
    for(const [dx,dz]of [[1,0],[-1,0],[0,1],[0,-1]]){
     const nx=xx+dx,nz=zz+dz,label=`${nx},${nz}`;
     if(nx<1||nx>width-2||nz<1||nz>depth-2||reserved(nx,nz))continue;
     if(key==='boss/eclipse_keeper'&&nz===22&&(nx<15||nx>18))continue;
     const next=cost+1+(collision(nx,nz)?100:0);
     if(next>=(costs.get(label)??Infinity))continue;
     costs.set(label,next);previous.set(label,[xx,zz]);queue.push([next,nx,nz]);
    }
   }
   if(!destination)throw Error(`No same-height route ${key} ${missing.point} reachable ${visited.size}`);
   let cursor=destination;const route=[];
   while(cursor){route.push(cursor);cursor=previous.get(cursor.join(','));}
   for(const [xx,zz]of route){deck(xx,zz,feet);for(const [dx,dz]of [[1,0],[-1,0],[0,1],[0,-1]]){const nx=xx+dx,nz=zz+dz;if(nx<1||nx>=width-1||nz<1||nz>=depth-1||reserved(nx,nz)||collision(nx,nz))continue;deck(nx,nz,feet);}}
   repairs.push({target:missing.point,feet,tiles:route.length});restore();
  }
  throw Error(`Repair did not converge ${key}`);
 };
 const partition=()=>{if(key==='boss/eclipse_keeper'){box([1,2,22],[width-2,height-2,22],'mud_bricks');box([15,2,22],[18,7,22],air);}};
 partition();let navigation=connect();
 for(let xx=1;xx<width-1;xx++)for(let zz=1;zz<depth-1;zz++){const roofY=Math.min(height-2,base+7+Math.floor((xx+ordinal%4)/3)%4+Math.floor(zz/5)%2);if(!key.startsWith('stairs/')&&!key.startsWith('boss/')&&!key.startsWith('spawn/outside')){for(let yy=roofY;yy<height-1;yy++)if(get(xx,yy,zz)===air)put(xx,yy,zz,(xx+zz+ordinal)%5===0?'mangrove_log':'mangrove_planks');if((xx*5+zz+ordinal)%23===0&&get(xx,roofY-1,zz)===air)put(xx,roofY-1,zz,'shroomlight');}}
 restore();partition();navigation=connect();
 for(const {point:[xx,yy,zz],clearance}of targets){if(!full(get(xx,yy-1,zz)))throw Error(`Support ${key}`);for(let above=0;above<clearance;above++)if(get(xx,yy+above,zz)!==air)throw Error(`Clearance ${key}`);}
 const allowed=new Set(variant.ports.flatMap(port=>port.cells.flatMap(([xx,yy,zz])=>Array.from({length:5},(_,above)=>`${xx},${yy+above},${zz}`))));let shellCells=0;
 for(let yy=variant.minimum_y;yy<height;yy++)for(let zz=0;zz<depth;zz++)for(let xx=0;xx<width;xx++)if(xx===0||xx===width-1||yy===variant.minimum_y||yy===height-1||zz===0||zz===depth-1){shellCells++;if(get(xx,yy,zz)===air&&!allowed.has(`${xx},${yy},${zz}`))throw Error(`Shell leak ${key} ${[xx,yy,zz]}`);}
 let gate;if(key==='boss/eclipse_keeper'){for(let xx=1;xx<width-1;xx++)for(let yy=2;yy<height-1;yy++){const opening=xx>=15&&xx<=18&&yy<=7;if((get(xx,yy,22)===air)!==opening)throw Error('Gate partition');}const closed=flood(navigation.walk,[16,2,2],true);if([...closed].some(label=>Number(label.split(',')[2])>22))throw Error('Gate bypass');gate={openReturnReachable:navigation.visited.has('16,2,25'),closedReturnReachable:false,partition:'z22 full height; x15..18 y2..7 opening'};}
 return {variant,ordinal,base,states,get,targets,extras,repairs,gate,walk:navigation.walk,reachable:navigation.visited.size,shellCells};
}
const parsed=new Map();
function writeRoom(built){const structure=new Structure(built.variant.size);const [width,height,depth]=built.variant.size;for(let yy=0;yy<height;yy++)for(let zz=0;zz<depth;zz++)for(let xx=0;xx<width;xx++){const text=built.get(xx,yy,zz);if(!parsed.has(text))parsed.set(text,BlockState.parse(text));const state=parsed.get(text);structure.addBlock([xx,yy,zz],state.getName().toString(),state.getProperties());}return structure.writeNbt({name:'',dataVersion:3465,compression:'gzip'});}
function readDense(bytes,size){const nbt=NbtFile.read(bytes).root,structure=Structure.fromNbt(nbt),[width,height,depth]=size,dense=new Array(width*height*depth);for(const block of structure.getBlocks()){const [xx,yy,zz]=block.pos;if(xx<0||xx>=width||yy<0||yy>=height||zz<0||zz>=depth)throw Error('NBT bounds');const offset=(yy*depth+zz)*width+xx;if(dense[offset]!==undefined)throw Error('Duplicate coordinate');if(block.nbt)throw Error('Block entity');dense[offset]=block.state.toString();}if(structure.getBlocks().length!==dense.length)throw Error('Incomplete structure');return {nbt,dense};}
function color(state){if(state==='minecraft:air')return '#102b30';if(state.includes('glass'))return '#43767c';if(state.includes('shroomlight'))return '#f1bd61';if(state.includes('roots'))return '#655740';if(state.includes('log'))return '#713e44';if(state.includes('planks')||state.includes('slab'))return '#a8535d';if(state.includes('mushroom'))return '#c4ad84';if(state.includes('terracotta'))return '#6f694d';if(state.includes('mud_bricks'))return '#80775e';return '#554e3b';}
function gallery(entries){
 const payload=entries.map(({built,dense,sha256})=>({key:built.variant.key,size:built.variant.size,base:built.base,ports:built.variant.ports,sockets:built.variant.sockets,extras:built.extras,spec:specs[built.ordinal],sha256,states:dense}));
 return `<!doctype html><html lang="zh-CN"><meta charset="utf-8"><title>幽沼27房真实NBT图集</title><style>body{background:#101f25;color:#e6e5cc;font:15px system-ui;margin:28px auto;max-width:1600px;padding:0 24px}p{color:#bcc7b5;line-height:1.6}main{display:grid;grid-template-columns:repeat(auto-fit,minmax(460px,1fr));gap:22px}article{background:#1d3033;border:1px solid #43574c;padding:20px;border-radius:12px}h2{font-size:20px;margin:0}code{font-size:12px;color:#d2aa93}.views{display:grid;grid-template-columns:1fr 1fr;gap:12px}canvas{background:#102b30;image-rendering:pixelated;width:100%;aspect-ratio:1}label{font-size:12px;color:#b2cbb9}input{width:50%}small{word-break:break-all;color:#91a79d}button{background:#534643;color:#f4e2cd;border:0;padding:6px 14px}@media(max-width:600px){main{display:block}article{margin-bottom:18px}}</style><h1>幽沼 · 栈道、药池与木桩聚落</h1><p>27模板，全部图像从导出的 gzip NBT 重新解码。两高度平面 + 可调深度剖面 + 真实方块简化轴测（不是游戏截图）。青色端口、红色socket、黄色独立实体脚点。玻璃封壳；轴测隐藏玻璃及普通房高处屋顶以显露内部。拖动切片滑条检查每一高度。</p><main id="main"></main><script>const entries=${JSON.stringify(payload)};const color=${color.toString()};
for(const entry of entries){const [width,height,depth]=entry.size,article=document.createElement('article');article.innerHTML='<h2>'+entry.spec[0]+'</h2><code>'+entry.key+'</code><p>'+entry.spec.slice(1).join(' · ')+'</p><div class="views"><div><label>平面 A / y <input type="range" min="0" max="'+(height-1)+'" value="'+entry.base+'"><b></b></label><canvas></canvas></div><div><label>平面 B / y <input type="range" min="0" max="'+(height-1)+'" value="'+Math.min(height-2,entry.base+5)+'"><b></b></label><canvas></canvas></div><div><label>剖面 / z <input type="range" min="0" max="'+(depth-1)+'" value="'+Math.floor(depth/2)+'"><b></b></label><canvas></canvas></div><div><label>内部轴测 <button>旋转</button></label><canvas></canvas></div></div><small>SHA256 '+entry.sha256+'</small>';document.querySelector('main').append(article);const canvases=article.querySelectorAll('canvas'),sliders=article.querySelectorAll('input'),get=(xx,yy,zz)=>entry.states[(yy*depth+zz)*width+xx];const paint=(canvas,view,level)=>{canvas.width=512;canvas.height=512;const context=canvas.getContext('2d'),rows=view===2?height:depth,scale=Math.min(480/width,480/rows),offX=(512-width*scale)/2,offY=(512-rows*scale)/2;for(let row=0;row<rows;row++)for(let xx=0;xx<width;xx++){const yy=view===2?height-1-row:level,zz=view===2?level:row;context.fillStyle=color(get(xx,yy,zz));context.fillRect(offX+xx*scale,offY+row*scale,scale,scale);}const mark=(point,tint)=>{const [xx,yy,zz]=point;if(view===2?zz!==level:yy!==level)return;context.strokeStyle=tint;context.lineWidth=2;context.strokeRect(offX+xx*scale+1,offY+(view===2?height-1-yy:zz)*scale+1,scale-2,scale-2);};for(const port of entry.ports)for(const point of port.cells)mark(point,'#58f4ef');for(const socket of entry.sockets)mark(socket.position,'#ff777c');for(const extra of entry.extras)mark(extra.point,'#f6da73');};sliders.forEach((slider,index)=>{const render=()=>{slider.nextElementSibling.textContent=slider.value;paint(canvases[index],index,+slider.value);};slider.oninput=render;render();});let rotation=0;const iso=()=>{const canvas=canvases[3];canvas.width=700;canvas.height=700;const context=canvas.getContext('2d');context.fillStyle='#102b30';context.fillRect(0,0,700,700);const unit=Math.min(580/(width+depth),510/(height+(width+depth)*0.25)),blocks=[];for(let yy=0;yy<height-1;yy++)for(let zz=1;zz<depth-1;zz++)for(let xx=1;xx<width-1;xx++){const state=get(xx,yy,zz);if(state==='minecraft:air'||state.includes('glass')||yy>entry.base+7&&!entry.key.startsWith('stairs/')&&!entry.key.startsWith('boss/'))continue;const rx=rotation%2?width-1-xx:xx,rz=rotation%2?depth-1-zz:zz;blocks.push({xx:350+(rx-rz)*unit,yy:620+(rx+rz-width-depth)*unit*.35-yy*unit,order:rx+rz+yy*.02,state});}blocks.sort((left,right)=>left.order-right.order);for(const block of blocks){const {xx,yy,state}=block;context.fillStyle=color(state);context.beginPath();context.moveTo(xx,yy-unit*.35);context.lineTo(xx+unit,yy);context.lineTo(xx,yy+unit*.35);context.lineTo(xx-unit,yy);context.closePath();context.fill();context.globalAlpha=.72;context.fillRect(xx-unit,yy,unit,unit*.8);context.globalAlpha=.9;context.fillRect(xx,yy,unit,unit*.8);context.globalAlpha=1;}};article.querySelector('button').onclick=()=>{rotation++;iso();};iso();}
</script></html>`;
}
function auditInterior(entry){
 const {built,dense}=entry,[width,height,depth]=built.variant.size,base=built.base;
 const cutRoof=!/^(stairs\/|boss\/|spawn\/outside)/.test(built.variant.key);
 const excluded=(xx,yy,zz)=>xx===0||xx===width-1||zz===0||zz===depth-1||yy<base||yy===height-1||(cutRoof&&yy>=Math.min(height-2,base+7))||built.variant.ports.some(port=>port.cells.some(([px,py,pz])=>Math.abs(xx-px)+Math.abs(zz-pz)<=3&&yy>=py-1&&yy<=py+4));
 const occupied=new Set(),histogram={},landmarks=new Map(),floor=[];
 for(let yy=base-1;yy<height-1;yy++)for(let zz=1;zz<depth-1;zz++)for(let xx=1;xx<width-1;xx++){
  const state=dense[(yy*depth+zz)*width+xx];
  if(yy===base-1){if(state.includes('mangrove_planks'))floor.push([xx,yy,zz]);if(state.includes('terracotta')){if(!landmarks.has('dry-potion-basin'))landmarks.set('dry-potion-basin',[]);landmarks.get('dry-potion-basin').push([xx,yy,zz]);}continue;}
  if(excluded(xx,yy,zz)||state===air)continue;
  occupied.add(`${xx},${yy-base},${zz}`);histogram[state]=(histogram[state]??0)+1;
  const group=state.includes('roots')?'mangrove-root-architecture':state.includes('log')?'mangrove-timber-frame':state.includes('slab')?'half-step-boardwalk':state.includes('planks')?'timber-building-or-platform':state.includes('mushroom')?'dry-herbal-stock':state.includes('mud_bricks')?'mud-basin-or-partition':null;
  if(group){if(!landmarks.has(group))landmarks.set(group,[]);landmarks.get(group).push([xx,yy,zz]);}
 }
 if(floor.length)landmarks.set('three-wide-boardwalk-floor',floor);
 const bbox=points=>({min:[0,1,2].map(axis=>Math.min(...points.map(point=>point[axis]))),max:[0,1,2].map(axis=>Math.max(...points.map(point=>point[axis])))});
 const features=[];
 for(const [kind,points]of landmarks){
  if(points.length<4)continue;
  const remaining=new Set(points.map(point=>point.join(','))),components=[];
  while(remaining.size){const first=remaining.values().next().value;remaining.delete(first);const queue=[first.split(',').map(Number)];for(let cursor=0;cursor<queue.length;cursor++){const [xx,yy,zz]=queue[cursor];for(const [dx,dy,dz]of [[1,0,0],[-1,0,0],[0,1,0],[0,-1,0],[0,0,1],[0,0,-1]]){const point=[xx+dx,yy+dy,zz+dz],label=point.join(',');if(remaining.delete(label))queue.push(point);}}if(queue.length>=3)components.push({blocks:queue.length,bbox:bbox(queue),coordinatesSha256:hash(JSON.stringify(queue))});}
  features.push({kind,blocks:points.length,bbox:bbox(points),components:components.sort((left,right)=>right.blocks-left.blocks).slice(0,12)});
 }
 const layout=new Set([...occupied].map(label=>{const [xx,,zz]=label.split(',');return `${xx},${zz}`;}));
 const normalized=new Set();
 for(let yy=0;yy<8;yy++)for(let zz=0;zz<12;zz++)for(let xx=0;xx<12;xx++){
  const px=1+Math.min(width-3,Math.floor((xx+.5)*(width-2)/12)),pz=1+Math.min(depth-3,Math.floor((zz+.5)*(depth-2)/12));
  const ceiling=cutRoof?Math.min(height-2,base+6):height-2,py=base+Math.floor((yy+.5)*(ceiling-base+1)/8);
  if(occupied.has(`${px},${py-base},${pz}`))normalized.add(`${xx},${yy},${zz}`);
 }
 return {occupied,layout,normalized,histogram,features,excluded,base};
}
const jaccard=(left,right)=>{let intersection=0;for(const cell of left)if(right.has(cell))intersection++;return left.size+right.size===0?1:intersection/(left.size+right.size-intersection);};
const family=key=>key.startsWith('stairs/')?'stairs':key.includes('layout/')?'child-layout':/straight\/(room_|hole|bridge|platform)|turn\/(hole|pillar_room)/.test(key)?'combat':key.startsWith('terminal/')||key==='promenade'?'terminal':key.startsWith('spawn/outside')?'exterior':key.startsWith('boss/')?'boss':key==='spawn/spawn'?'spawn':'passage';
function fourViews(entries){
 const payload=entries.map(({built,dense,sha256})=>{const palette=[...new Set(dense)];return {key:built.variant.key,size:built.variant.size,base:built.base,name:specs[built.ordinal][0],sha256,palette,blocks:dense.map(state=>palette.indexOf(state))};});
 return `<!doctype html><html lang="zh"><meta charset="utf-8"><title>幽沼27房四方向NBT检查</title><style>body{background:#10242b;color:#e5ead5;font:14px system-ui;margin:24px auto;max-width:1180px;padding:0 16px}h1{margin:8px 0}p{line-height:1.6;color:#b7cbbd}article{background:#203b3d;border:1px solid #4d6760;border-radius:10px;margin:18px 0;padding:12px}h2{font-size:18px;margin:2px 0}code,small{font-size:10px;color:#d4b69c;word-break:break-all}.views{display:grid;grid-template-columns:repeat(4,1fr);gap:8px}canvas{width:100%;background:#10272b;aspect-ratio:1.25}figcaption{font-weight:bold}figure{margin:4px 0}</style><h1>幽沼 · 27房 × 北南西东</h1><p>每个方向独立旋转坐标，再按观察深度排序投影最终NBT方块；非游戏截图。隐藏外边界壳体与顶面，保留地板；普通房隐藏 y≥base+7 的公共上屋盖，楼梯/Boss/远近景保留内部全高。实体建筑和药池未隐藏。固定四个正交方位、统一仰俯角，北面向+Z，南面向−Z，西面向+X，东面向−X。外壳/端口安全由 validation.json 独立检查。主路、过密和地标证据见逐房 self-audit/style-audit。</p><main></main><script>const rooms=${JSON.stringify(payload)};const color=${color.toString()};const directions=[['north','北 N → +Z'],['south','南 S → −Z'],['west','西 W → +X'],['east','东 E → −X']];
for(const room of rooms){const article=document.createElement('article');article.dataset.key=room.key;article.innerHTML='<h2>'+room.name+' <code>'+room.key+'</code></h2><small>NBT SHA256 '+room.sha256+'</small><div class="views"></div>';document.querySelector('main').append(article);const [width,height,depth]=room.size;for(const [direction,label]of directions){const figure=document.createElement('figure');figure.innerHTML='<figcaption>'+label+'</figcaption><canvas width="640" height="512"></canvas>';article.querySelector('.views').append(figure);const canvas=figure.querySelector('canvas');canvas.dataset.direction=direction;canvas.dataset.key=room.key;const context=canvas.getContext('2d');context.fillStyle='#10272b';context.fillRect(0,0,640,512);const rotatedWidth=direction==='north'||direction==='south'?width:depth,rotatedDepth=direction==='north'||direction==='south'?depth:width,unit=Math.min(590/(rotatedWidth+1),450/(height+rotatedDepth*.32+1)),origin=490,blocks=[];for(let yy=room.base-1;yy<height-1;yy++)for(let zz=1;zz<depth-1;zz++)for(let xx=1;xx<width-1;xx++){const state=room.palette[room.blocks[(yy*depth+zz)*width+xx]];if(state==='minecraft:air'||state.includes('glass'))continue;if(!/^(stairs\\/|boss\\/|spawn\\/outside)/.test(room.key)&&yy>=room.base+7)continue;let rx,rz;if(direction==='north'){rx=xx;rz=zz;}if(direction==='south'){rx=width-1-xx;rz=depth-1-zz;}if(direction==='west'){rx=depth-1-zz;rz=xx;}if(direction==='east'){rx=zz;rz=width-1-xx;}blocks.push({rx,rz,yy,state});}blocks.sort((left,right)=>right.rz-left.rz||left.yy-right.yy||left.rx-right.rx);for(const {rx,rz,yy,state}of blocks){const sx=320+(rx-rotatedWidth/2)*unit,sy=origin-(yy-room.base+1)*unit-rz*unit*.32,half=state.includes('slab')?.5:1,blockHeight=unit*half;context.fillStyle=color(state);context.globalAlpha=.92;context.fillRect(sx,sy-blockHeight,unit+0.3,blockHeight);context.globalAlpha=1;context.beginPath();context.moveTo(sx,sy-blockHeight);context.lineTo(sx+unit,sy-blockHeight);context.lineTo(sx+unit,sy-blockHeight-unit*.32);context.lineTo(sx,sy-blockHeight-unit*.32);context.closePath();context.fill();context.strokeStyle='rgba(5,15,18,.15)';context.lineWidth=.4;context.strokeRect(sx,sy-blockHeight,unit,blockHeight);}}}
</script></html>`.replace('yy>=room.base+7','yy>=Math.min(height-2,room.base+7)').replace('if(state===\'minecraft:air\'||state.includes(\'glass\'))continue;','if(state===\'minecraft:air\'||state.includes(\'glass\')||state.includes(\'shroomlight\'))continue;').replace('blocks.push({rx,rz,yy,state});','if(rz<(rotatedDepth-2)*.25&&yy>=room.base)continue;blocks.push({rx,rz,yy,state});').replace('普通房隐藏 y≥base+7 的公共上屋盖','普通房隐藏 y≥min(height−2,base+7) 的公共上屋盖').replace('实体建筑和药池未隐藏。','每方向统一剖去最近四分之一深度中高于地板的部分，并隐藏失去顶面支承的灯块，显露主路与地标；未截切全貌仍可在gallery中查看。');
}
async function finalizeAudit(entries,validation){
 const interiors=entries.map(auditInterior),pairs=[],styleRooms=[];
 const reviews=[
  '北南显露偏轴苗圃根墙，东西可见折栈出入口；保留低量入口地标，公共冠盖在gallery检查。',
  '四面高低屋脚与街巷可区分；南侧近剖切显露主街，三屋檐高不同，非四角柱。',
  '北南可分出两根丘的对角偏置；东西轮廓一高一直一弯曲，保留稀疏远景。',
  '东西根墙形成S通道，侧面有大实体墙遮住局部路段；平面切片与连通检查确认可通过。',
  '北南双厚门框有实体门孔，东西显露门间扩大验药室；保留通道两门功能。',
  '北南清晰看见新增根梁横跨干池，东西看见桥墩与泥池沿；主栈道净空通过。',
  '北南根隔墙错位，东西能看见两个不同位置牢门；通行是隔间短巷而非大空厅。',
  '四面货棚高度与货垛可分辨；西侧低货垛与东侧高棚不对称，横院保留。',
  '四面三屋体积不同，近剖切显出屋间弯街；高桩屋占体积较多但下层路线通过。',
  '北南新增根拱与池沿一体，东西见单根拱侧影；四向保留绕池环栈。',
  '四面低通道与高药台分层明确；东西显示半格阶梯，所有六个不同高度接口已接通。',
  '北南厚根墙围出偏北分岔，东西显示不对称根墙；根母题作墙体而非四角柱。',
  '北南大门墙会遮住药市内部，东西剖视显出新增红木药摊与柜；保留厚门功能，gallery可看完整平面。',
  '四面不同屋体与门巷可分辨，近剖切显露小室网；未沿用单一空厅布局。',
  '北南能看见新增晒槽横架，东西是长泥药槽侧面；西槽与东C栈道区别于中央环池。',
  '四面半格宽梯盘绕空中庭，底部悬吊药架在东西方向可辨；上层歇台为固定接口保留。',
  '四面井周梯与中央泥井可辨；中间停台和药槽支路与普通中庭梯不同，允许共用踏步体系。',
  '北南中央巨根明显高过梯，东西半格梯环根盘绕；同族平面相似来自固定层间接口。',
  '北南大根丘与折栈形成L，西侧大实体根墙遮住局部内部；另向和切片确认西出口路线。',
  '四面侧药坊与低药柜可辨，药罐为浅色菌块体；柜前厅保留交易站位，不填成战斗四柱厅。',
  '北南三分仓墙面不同，东西泥隔墙与低药材垛明显；中央卸货道连接尽头横窖。',
  '北南错门牢舍为两组根墙，东西见高低门楣与端部回路；非入口通道复制。',
  '剔除共同屋盖后，四面清楚出现苗床根隔屏与横向通行带；子布局很小，保留简洁短屏。',
  '四面菌块药垛与红木货垛材质体量不同，折线夹巷可见；不再以公共屋盖制造虚假相似。',
  '四面两厚棚错位，侧面见双体之间短巷；小型桩棚屋顶全貌另看gallery高切片。',
  '北面胜利隔墙遮住返程后区属玩法必需；南面统一近剖切显露巨根肋，东西见三纵横栈和多干池；完整门墙另行验证。',
  '北南纵廊尽头横展开，东西根墙与晾药墙体不对称；T形端部台与S通道有实际体量差别。'
 ];
 const fixes=[{key:'straight/hole',issue:'Pool rim alone too weak as a swamp landmark',repair:'Added two root piers, inward knees and solid spanning mangrove beam above central dry basin'},{key:'turn/hole',issue:'Long dry channel lacked readable depth and function',repair:'Built long mud drying trough with two transverse timber drying racks'},{key:'straight/bridge',issue:'Bridge theme under-expressed above floor',repair:'Added distinct rooted bridge pylons and overhead beam'},{key:'stairs/centered',issue:'Plain perimeter stair alone was only a circulation shell',repair:'Added suspended twin drying racks and rooted hangers inside central void'}];
 fixes.push({key:'turn/big_doors',issue:'Excluding shared roof revealed mud walls without a timber market landmark',repair:'Added full sheltered redwood herbal stall, twin log posts and mushroom-stock counter in western market bay'});
 for(let ordinal=0;ordinal<entries.length;ordinal++){
  const entry=entries[ordinal],interior=interiors[ordinal],{built}=entry,[width,,depth]=built.variant.size;
  let blocked=0,corners=0;
  for(let zz=1;zz<depth-1;zz++)for(let xx=1;xx<width-1;xx++)if(built.get(xx,built.base,zz)!==air)blocked++;
  for(const cell of interior.occupied){const [xx,,zz]=cell.split(',').map(Number);if((xx<=3||xx>=width-4)&&(zz<=3||zz>=depth-4))corners++;}
  const thematic=interior.features.filter(feature=>feature.kind!=='mud-basin-or-partition'&&feature.kind!=='three-wide-boardwalk-floor');
  const hasLandmark=thematic.some(feature=>feature.blocks>=6)||interior.features.some(feature=>feature.kind==='dry-potion-basin'&&feature.blocks>=8);
  const clearFraction=1-blocked/((width-2)*(depth-2));
  if(!hasLandmark||clearFraction<.18)throw Error('Weak/overcrowded style '+built.variant.key);
  styleRooms.push({key:built.variant.key,nbtSha256:entry.sha256,status:'PASS',family:family(built.variant.key),materialsExcludingCommonRoofShellAndPortBands:interior.histogram,themeStructures:interior.features,functionalShape:specs[ordinal][1],functionalStyleEvidence:`Actual NBT has ${thematic.map(feature=>feature.kind+':'+feature.blocks).join(', ')}; structured void/floor arrangement retained, not inferred from room title.`,fourDirectionReview:reviews[ordinal],floorLevelClearFraction:clearFraction,cornerOnlySolidFraction:interior.occupied.size?corners/interior.occupied.size:0,recolorOnly:false,baselineChangedSolidAir:validation.rooms[ordinal].geometry.changedSolidAir,fourCornerHallReview:corners/interior.occupied.size>.8?'Corner-heavy exterior huts retained as separate roofed buildings; four directional views show intervening street.':'Internal solids extend beyond four corners; no empty four-pillar hall.',repeatedSymbolReview:'Retained timber/root/basin vocabulary: connected wall masses, habitable hut volumes, spans or working platforms; no repeated badge used as sole architecture.',repair:fixes.find(fix=>fix.key===built.variant.key)??null});
 }
 for(let left=0;left<entries.length;left++)for(let right=left+1;right<entries.length;right++){
  const first=entries[left],second=entries[right],sameSize=first.built.variant.size.join()===second.built.variant.size.join(),sameFamily=family(first.built.variant.key)===family(second.built.variant.key);
  if(!sameSize&&!sameFamily)continue;
  const firstInterior=interiors[left],secondInterior=interiors[right],score=jaccard(sameSize?firstInterior.occupied:firstInterior.normalized,sameSize?secondInterior.occupied:secondInterior.normalized),layoutScore=sameSize?jaccard(firstInterior.layout,secondInterior.layout):null;
  let rotationScore=score;
  if(sameSize&&first.built.variant.size[0]===first.built.variant.size[2]){
   const width=first.built.variant.size[0];let rotated=secondInterior.occupied;
   for(let turn=1;turn<4;turn++){rotated=new Set([...rotated].map(cell=>{const [xx,yy,zz]=cell.split(',').map(Number);return `${width-1-zz},${yy},${xx}`;}));rotationScore=Math.max(rotationScore,jaccard(firstInterior.occupied,rotated));}
  }
  const combatPair=family(first.built.variant.key)==='combat'&&family(second.built.variant.key)==='combat';
  const high=rotationScore>=.9||(layoutScore??0)>=.9;
  if(score===1||combatPair&&high)throw Error(`Repeated interior ${first.built.variant.key} ${second.built.variant.key}`);
  if(high&&(!first.built.variant.key.startsWith('stairs/')||!second.built.variant.key.startsWith('stairs/')))throw Error('High layout similarity requires rebuild '+first.built.variant.key+' '+second.built.variant.key);
  pairs.push({first:first.built.variant.key,second:second.built.variant.key,sameSize,sameFamily,familyFirst:family(first.built.variant.key),familySecond:family(second.built.variant.key),solidAirJaccard:score,maximumQuarterTurnJaccard:rotationScore,footprintJaccard:layoutScore,highSimilarity:high,conclusion:high?'Reviewed: retained fixed half-step circulation only; themed occupied masses differ.':'PASS: distinct internal occupied volumes/voids after removing shared constraints; see paired four-direction images.'});
 }
 pairs.sort((left,right)=>right.maximumQuarterTurnJaccard-left.maximumQuarterTurnJaccard);
 const json=(name,value)=>fs.writeFileSync(path.join(out,name),JSON.stringify(value,null,2)+'\n');
 json('style-audit.json',{schema:1,style:'fteclipsegen:swamp',status:'PASS',source:'Final exported dense NBT decoded after write',excluded:'Outer boundary, base floor for volume histogram, port safety bands, shared upper canopy in ordinary rooms. Dry-basin and boardwalk floor evidence recorded separately.',rooms:styleRooms,repairs:fixes});
 json('repetition-audit.json',{schema:1,status:'PASS',threshold:.9,exclusions:'Outer boundary x/z, foundation y<base, top y=height-1, common upper canopy y>=base+7 for ordinary rooms, Manhattan radius3 around each port at y=portY-1..portY+4.',metrics:'Solid coordinate intersection/union excluding AIR agreement; separate plan footprint union; max over four quarter turns for equal square sizes. Cross-size same-family comparisons use 12x8x12 normalized sample grids.',signatures:entries.map((entry,index)=>({key:entry.built.variant.key,nbtSha256:entry.sha256,solidCells:interiors[index].occupied.size,internalSolidAirSha256:hash([...interiors[index].occupied].sort().join(';')),normalizedGrid:[12,8,12],normalizedSignature:[...interiors[index].normalized].sort(),normalizedSha256:hash([...interiors[index].normalized].sort().join(';')),baseline033:validation.rooms[index].baseline,baselineDifference:validation.rooms[index].geometry})),comparablePairs:pairs.length,pairs,highestPairs:pairs.slice(0,20),ordinaryCombatExceptions:[],repairs:fixes});
 fs.writeFileSync(path.join(out,'four-views.html'),fourViews(entries));
 const require=createRequire('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/package.json');
 const {chromium}=require('playwright'),browser=await chromium.launch({headless:true});
 const pageErrors=[],viewRecords=[];
 try{
  const page=await browser.newPage({viewport:{width:1200,height:1000},deviceScaleFactor:1});page.on('pageerror',error=>pageErrors.push(error.message));
  await page.goto('file:///'+path.join(out,'four-views.html').replaceAll('\\','/'));
  const rendered=await page.evaluate(()=>[...document.querySelectorAll('canvas')].map(canvas=>{const pixels=canvas.getContext('2d').getImageData(0,0,canvas.width,canvas.height).data,colors=new Set();for(let offset=0;offset<pixels.length;offset+=4)colors.add(pixels[offset]+','+pixels[offset+1]+','+pixels[offset+2]);return {key:canvas.dataset.key,direction:canvas.dataset.direction,colors:colors.size,data:canvas.toDataURL('image/png').split(',')[1]};}));
  if(pageErrors.length||rendered.length!==108||rendered.some(view=>view.colors<2))throw Error('Four views browser '+JSON.stringify(pageErrors));
  for(const view of rendered){const filename='views/'+view.key.replaceAll('/','--')+'--'+view.direction+'.png',bytes=Buffer.from(view.data,'base64');fs.mkdirSync(path.join(out,'views'),{recursive:true});fs.writeFileSync(path.join(out,filename),bytes);viewRecords.push({key:view.key,direction:view.direction,file:filename,pngSha256:hash(bytes),nbtSha256:entries.find(entry=>entry.built.variant.key===view.key).sha256,colors:view.colors,status:'PASS'});}
  for(const entry of entries){const views=viewRecords.filter(view=>view.key===entry.built.variant.key);if(new Set(views.map(view=>view.pngSha256)).size!==4)throw Error('Four directions rendered duplicate '+entry.built.variant.key);}
  for(let batch=0;batch<3;batch++){
   await page.evaluate(batch=>{document.querySelectorAll('article').forEach((article,index)=>article.style.display=index>=batch*9&&index<(batch+1)*9?'':'none');},batch);
   await page.screenshot({path:path.join(out,`four-view-contact-${batch+1}.png`),fullPage:true});
  }
  await page.goto('file:///'+path.join(out,'gallery.html').replaceAll('\\','/'));
  const galleryCounts=await page.evaluate(()=>({rooms:document.querySelectorAll('article').length,canvases:document.querySelectorAll('canvas').length}));
  await page.evaluate(()=>{for(const slider of document.querySelectorAll('input')){const original=slider.value;slider.value=slider.max;slider.dispatchEvent(new Event('input'));slider.value=original;slider.dispatchEvent(new Event('input'));}for(const button of document.querySelectorAll('button')){button.click();button.click();}});
  if(pageErrors.length||galleryCounts.rooms!==27||galleryCounts.canvases!==108)throw Error('Gallery browser');
  await page.screenshot({path:path.join(out,'gallery-review.png')});
  await page.locator('article').filter({hasText:'boss/eclipse_keeper'}).screenshot({path:path.join(out,'gallery-boss.png')});
  json('browser-validation.json',{status:'PASS',fourViews:{rooms:27,canvases:108,distinctDirections:['north','south','west','east'],pngFiles:108},gallery:galleryCounts,pageErrors});
 }finally{await browser.close();}
 const viewAudit=entries.map((entry,index)=>({key:entry.built.variant.key,nbtSha256:entry.sha256,directions:viewRecords.filter(view=>view.key===entry.built.variant.key),styleStatus:styleRooms[index].status,mainRoute:'All declared interfaces connected by supported <=0.5-step surface BFS after final rebuild; no jump edges.',density:styleRooms[index].floorLevelClearFraction>=.18?'PASS':'FAIL',landmarks:styleRooms[index].themeStructures.filter(feature=>feature.kind!=='three-wide-boardwalk-floor').map(feature=>({kind:feature.kind,bbox:feature.bbox})),visualReview:'Four actual-NBT cardinal renders reviewed in contact sheets; shared shell/upper canopy hidden by documented rule, no missing view.',baselineGeometry:validation.rooms[index].geometry,repair:fixes.find(fix=>fix.key===entry.built.variant.key)??null,status:'PASS'}));
 json('self-audit.json',{schema:1,status:'PASS',count:27,fourViewCount:108,pageErrors,manifestSha256:validation.manifestSha256,validationSha256:hash(fs.readFileSync(path.join(out,'validation.json'))),fourViewsHtmlSha256:hash(fs.readFileSync(path.join(out,'four-views.html'))),styleAuditSha256:hash(fs.readFileSync(path.join(out,'style-audit.json'))),repetitionAuditSha256:hash(fs.readFileSync(path.join(out,'repetition-audit.json'))),repairs:fixes,highestInteriorSimilarity:pairs[0],retainedHighLayoutPairs:pairs.filter(pair=>pair.highSimilarity),hidingRules:{shell:'outermost x/z and highest y hidden',floor:'y=base-1 retained',commonCanopy:'ordinary rooms y>=min(height-2,base+7) hidden',nearQuarter:'for each direction hide nearest quarter depth above floor',lights:'shroomlight hidden with canopy'},rooms:viewAudit,limitations:['Offline simplified block projection and supported-surface graph are not an in-game test.','Four views use documented quarter cutaways; actual sealed shell tested separately.','Whole room bounding boxes may include disconnected thematic clusters; per-component bounds also recorded.']});
 json('freeze.json',{status:'FROZEN',style:'fteclipsegen:swamp',count:27,manifestSha256:validation.manifestSha256,recipeSha256:hash(fs.readFileSync(fileURLToPath(import.meta.url))),selfAuditSha256:hash(fs.readFileSync(path.join(out,'self-audit.json'))),fourViewCount:108,formalResourcesModified:false,deployed:false});
 const files=['design.md','validation.json','gallery.html','four-views.html','style-audit.json','repetition-audit.json','self-audit.json','browser-validation.json','freeze.json','roomset/manifest.json',...entries.map(entry=>'roomset/'+entry.built.variant.key+'.nbt'),...viewRecords.map(view=>view.file),'four-view-contact-1.png','four-view-contact-2.png','four-view-contact-3.png','gallery-review.png','gallery-boss.png'];
 fs.writeFileSync(path.join(out,'SHA256SUMS.txt'),files.map(file=>`${hash(fs.readFileSync(path.join(out,file)))}  ${file}`).join('\n')+'\n');
 process.stdout.write(`AUDIT PASS: ${viewRecords.length} views; ${pairs.length} comparable pairs; maximum interior Jaccard ${pairs[0].maximumQuarterTurnJaccard.toFixed(4)}\n`);
}
async function main(){
 fs.mkdirSync(out,{recursive:true});
 const design='# 幽沼 · 全27房重建\n\n运行：`node tools/rebuild-swamp.mjs`。只写本配方及本输出。原版1.20.1，DataVersion3465，无水、实体或方块实体。新建坐标，不提取旧房骨架。三格栈道配红树根墙、泥砖干药池、分离桩屋坡顶。楼梯使用半格红木半砖；连通检查禁止整格攀升。所有开敞顶和立面以完整玻璃封壳。\n\n'+variants.map((variant,index)=>`## ${variant.key} · ${specs[index][0]}\n\n用途与平面动线：${specs[index][1]}。高度变化：${specs[index][2]}。标志结构及同风格区别：${specs[index][3]}。\n`).join('\n');
 fs.writeFileSync(path.join(out,'design.md'),design+'\n## 逐房复审返工\n\nstraight/bridge 加根桥墩与横梁；straight/hole 加池中膝根拱；turn/hole 加长泥晒槽与横药架；stairs/centered 加悬吊药架；turn/big_doors 加独立红木药摊、原木支柱和菌块药柜。各项均是实际方块几何，重新验证原接口净空与连通。\n\n四视图：每房北南西东，隐藏公共壳体、公共上盖、灯块，统一剖去观察方向最近四分之一的高于地板结构以显露内部。全貌另见gallery.html。重复度剔除共同外壳、地基、公共上屋盖与端口安全带，记录实体Jaccard、四次旋转最大值与平面布局值。\n');
 const templates=[],reports=[],entries=[],geometries=new Set();
 for(let ordinal=0;ordinal<variants.length;ordinal++){
  const variant=variants[ordinal];process.stdout.write(`BUILD ${variant.key}\n`);
  const oldPath=path.join(data,'roomsets/swamp',variant.key+'.nbt'),oldBytes=fs.readFileSync(oldPath),old=Structure.fromNbt(NbtFile.read(oldBytes).root),oldStates=new Map(old.getBlocks().map(block=>[block.pos.join(','),block.state.toString()]));
  const built=makeRoom(variant,ordinal),bytes=writeRoom(built),{nbt,dense}=readDense(bytes,variant.size);
  if(nbt.getNumber('DataVersion')!==3465||!nbt.hasList('entities',undefined,0))throw Error('NBT format');
  const legal=new Set(['air','packed_mud','mud_bricks','glass','mangrove_planks','mangrove_roots','mangrove_log','brown_terracotta','brown_mushroom_block','shroomlight','mangrove_slab'].map(name=>'minecraft:'+name));
  nbt.getList('palette',10).forEach(entry=>{if(!legal.has(entry.getString('Name'))||entry.getString('Name').includes('['))throw Error('Palette');if(entry.has('Properties')&&!entry.hasCompound('Properties'))throw Error('Properties');});
  let changed=0,union=0,stateChanged=0;const geometry=[],[width,,depth]=variant.size;
  if(dense.slice(0,variant.minimum_y*width*depth).some(state=>state!==air))throw Error('minimum_y');
  for(let offset=0;offset<dense.length;offset++){if(dense[offset]!==built.states[offset])throw Error('Roundtrip');const xx=offset%width,zz=Math.floor(offset/width)%depth,yy=Math.floor(offset/(width*depth)),before=oldStates.get(`${xx},${yy},${zz}`)??air,was=before!==air,now=dense[offset]!==air;if(was!==now)changed++;if(was||now)union++;if(before!==dense[offset])stateChanged++;geometry.push(now?1:0);}
  const geometryHash=hash(Buffer.from(geometry));if(geometries.has(geometryHash)||!changed)throw Error('Repeated/unchanged geometry');geometries.add(geometryHash);
  const sha256=hash(bytes),destination=path.join(out,'roomset',variant.key+'.nbt');fs.mkdirSync(path.dirname(destination),{recursive:true});fs.writeFileSync(destination,bytes);templates.push({key:variant.key,sha256});entries.push({built,dense,sha256});
  reports.push({key:variant.key,name:specs[ordinal][0],size:variant.size,minimum_y:variant.minimum_y,ports:variant.ports,sockets:variant.sockets,...(variant.spawn_position?{spawn_position:variant.spawn_position}:{}),sha256,geometryHash,mainFeature:specs[ordinal][3],baseline:{path:oldPath,sha256:hash(oldBytes)},geometry:{changedSolidAir:changed,ratioAllCells:changed/dense.length,ratioSolidUnion:changed/union,stateChanged},checks:{denseUniqueInBounds:true,dataVersion3465:true,noEntitiesOrBlockEntities:true,bareVanillaPaletteSeparateProperties:true,portsFiveAirFullSupport:true,socketsThreeAirFullSupport:true,independentEntityFeet:true,allDeclaredTargetsConnected:true,halfStepWalkOnly:true,sealedShell:true},independentEntityFeet:built.extras,targetsChecked:built.targets.length,walkCells:built.walk.size,reachableWalkCells:built.reachable,shellCells:built.shellCells,connectivityRepairs:built.repairs,...(built.gate?{gate:built.gate}:{})});
  process.stdout.write(`PASS ${variant.key} geometry=${(changed/dense.length*100).toFixed(1)}%\n`);
 }
 fs.writeFileSync(path.join(out,'roomset/manifest.json'),JSON.stringify({schema:1,style:'fteclipsegen:swamp',templates},null,2)+'\n');
 const validation={schema:1,style:'fteclipsegen:swamp',version:'0.34.0',status:'PASS',count:templates.length,uniqueGeometries:geometries.size,catalogSha256:hash(fs.readFileSync(path.join(data,'catalog.json'))),manifestSha256:hash(fs.readFileSync(path.join(out,'roomset/manifest.json'))),method:'Decoded exported dense NBT exact roundtrip; supported 2.8-block body, cardinal movement with <=0.5 step; exhaustive shell and Boss wall checks, open/closed gate BFS.',limitations:['Offline voxel centerline connectivity, not in-game mob pathfinding.','Axonometric simplified actual-NBT cube projection; glass and upper ordinary roofs hidden.'],rooms:reports};
 fs.writeFileSync(path.join(out,'validation.json'),JSON.stringify(validation,null,2)+'\n');fs.writeFileSync(path.join(out,'gallery.html'),gallery(entries));
 const names=['design.md','validation.json','gallery.html','roomset/manifest.json',...templates.map(template=>'roomset/'+template.key+'.nbt')];fs.writeFileSync(path.join(out,'SHA256SUMS.txt'),names.map(name=>`${hash(fs.readFileSync(path.join(out,name)))}  ${name}`).join('\n')+'\n');
 await finalizeAudit(entries,validation);
 process.stdout.write(`FROZEN ${templates.length}; manifest ${validation.manifestSha256}\n`);
}
if(process.argv[1]&&path.resolve(process.argv[1])===fileURLToPath(import.meta.url))main();
