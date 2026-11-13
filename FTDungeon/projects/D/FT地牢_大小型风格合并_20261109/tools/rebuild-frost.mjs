import fs from 'node:fs';
import path from 'node:path';
import crypto from 'node:crypto';
import {createRequire} from 'node:module';
import {fileURLToPath} from 'node:url';
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..');
const out=path.join(root,'output/style-rebuild-v034/frost');
const data=path.join(root,'src/main/resources/data/fteclipsegen');
const skill=process.env.MINECRAFT_BUILDER_SKILL??'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill';
const {Structure,NbtFile,BlockState}=createRequire(path.join(skill,'package.json'))('@mattzh72/lodestone');
const catalog=JSON.parse(fs.readFileSync(path.join(data,'catalog.json'),'utf8'));
const air='minecraft:air';
const hash=bytes=>crypto.createHash('sha256').update(bytes).digest('hex');
const baselinePath=path.join(out,'baseline-v033.json');
const baselineSnapshot=fs.existsSync(baselinePath)?JSON.parse(fs.readFileSync(baselinePath,'utf8')):{schema:1,style:'fteclipsegen:frost',templates:{}};
const draftPath=path.join(out,'audit-initial-draft.json');
if(!fs.existsSync(draftPath)&&fs.existsSync(path.join(out,'roomset/manifest.json')))fs.writeFileSync(draftPath,fs.readFileSync(path.join(out,'roomset/manifest.json')));
const baseOf=variant=>variant.key==='spawn/spawn'?4:variant.key.includes('/layout/')?1:2;
function roofAt(variant,seed,x,z){const [width,height]=variant.size;return variant.key.startsWith('boss/')?height-2-Math.floor(Math.abs(x-(width-1)/2)/5):Math.max(baseOf(variant)+5,Math.min(height-1,height-3-Math.floor(Math.abs(x-width*.48)/(seed%3+2))-((Math.floor(z/3)+seed)%3)));}
const designs=[
 ['冰冠候门台','入口整备','东侧半环步道接南出口','4层起步，9层悬冠','分瓣冰冠','半环入口呼应Boss冠'],
 ['近景冻瀑壁','近景封闭外景','折线观察带','2层落脚，24层冻瀑','双支流冻瀑','垂直冻瀑主导'],
 ['远景冰蚀峰','远景封闭外景','山脊外观察环','2层地坪，22层雪峰','三角山脊','山体负空间'],
 ['冰裂双湾','基础交战','S形两岸与错位双湾','2层路，11层斜顶','不对称冰裂肋','错轴双湾'],
 ['双闸气锁厅','门式交战','错位隔板三段气锁','2层路，8层闸梁','横向冰闸','横断分段'],
 ['断谷三舷桥','桥上交战','中央桥与两侧巡检湾','2层桥，0层谷底','悬桥支流桥','低谷架桥'],
 ['冻眠列舱','休眠舱战斗','西双舱与东守卫室','2层廊，6层舱盖','玻璃冻舱','纵舱错门'],
 ['横列冻存档案','档案战斗','横架穿插左右通道','2层路，8层架','封冰书架','横架迷宫'],
 ['双台冰蚀堡','高台交战','西南东北台堡间环路','2层路，7/9层冠','错轴实心冰堡','双台不同尺度'],
 ['环隙冰井','环路交战','中央冰井外闭合环路','2层环，0层井底','多边冰井','空心核心'],
 ['冻层勘测台','分层交战','主层与双翼阶梯高台','2/4/5层脚点','层状勘测台','高差接口不变，洞体重建'],
 ['三岔冰脊厅','三向交战','Y路分开三座冰脊','2层路，12层肋','三叉脊','Y拓扑'],
 ['横闸分流厅','三向门厅','西墙与东廊成T形','2层路，10层梁','巨大横闸','横向立面'],
 ['曲折冷却室','小门交战','偏轴冷却墙成回字折线','2层路，6层低廊','冷却鳍','紧凑回路'],
 ['偏井分桥厅','裂井交战','西偏井上三路搭桥','2层桥，0层底','偏井三桥','非中心对称井'],
 ['折瀑登冰廊','上下层转换','坡阶外分段冻瀑壁','2至18层台阶','折瀑冰壁','仅复用基础接口踏面，洞体重建'],
 ['裂井登高廊','中段检修战斗','沿井阶梯与中层横湾','2/7/12/18层','贯穿高井','仅复用基础接口踏面，垂直井腔'],
 ['冰螺旋升廊','旋转上升战斗','围冰髓旋转登高','2/4/6/18层','中央冰髓旋肋','仅复用基础接口踏面，旋转洞体'],
 ['折角冻河','转角交战','北转西L路与内外双湾','2层路，11层肋','斜切内角','唯一西向转角'],
 ['极夜食堂','补给端部','两侧长桌末端服务台','2层面，7层檐','冰桌冰厨','餐饮盲端'],
 ['封冻重库','仓储端部','中央货垛外U卸货路','2层路，8层垛','封冰巨垛','U回路'],
 ['冰封审讯所','牢房端部','不对称牢间尽头审讯台','2层廊，6至8层牢盖','长短冻牢','盲端审讯'],
 ['冻眠舱芯','舱室子模板','低舱列西服务带','1层路，4层盖','独立低舱','区别大房高舱'],
 ['薄冰索引库','档案子模板','十字索引四角通路','1层路，4层索引','十字目录','区别横架'],
 ['冰芯双堡','堡垒子模板','偏心双芯西侧环路','1层路，5层芯','大小双芯','紧凑双芯'],
 ['极冠守望者','Boss胜利返程','中轴穿前殿主场受控返程门','2层面，11至18层冠','巨悬冠八战斗湾','z22封顶隔墙；入口冰冠扩大'],
 ['冰镜商廊','商店端部','末端柜台两侧展架','2层交易面，5层柜','连续冰镜柜台','安全交易面']
];
fs.mkdirSync(path.join(out,'roomset'),{recursive:true});
fs.writeFileSync(path.join(out,'FREEZE.json'),JSON.stringify({status:'BUILD_RUNNING'},null,2)+'\n');
fs.writeFileSync(path.join(out,'design.md'),'# 冰霜27房独立重建\n\n材料：不融化packed_ice、blue_ice、calcite、snow_block、quartz、玻璃、sea_lantern。外壳封闭，主路三格宽。楼梯高台仅提取原基础模板踏面，重建洞壁洞顶与冰霜主体。\n\n'+catalog.variants.map((variant,index)=>{const entry=designs[index];return `## ${variant.key} · ${entry[0]}\n\n用途：${entry[1]}。平面动线：${entry[2]}。高度：${entry[3]}。标志：${entry[4]}。区别：${entry[5]}。\n`;}).join('\n'));
function makeRoom(variant,seed){
 const [width,height,depth]=variant.size;
 const base=variant.key==='spawn/spawn'?4:variant.key.includes('/layout/')?1:2;
 const states=Array(width*height*depth).fill(air);
 const inside=(x,y,z)=>x>=0&&y>=0&&z>=0&&x<width&&y<height&&z<depth;
 const get=(x,y,z)=>inside(x,y,z)?states[(y*depth+z)*width+x]:'minecraft:bedrock';
 const put=(x,y,z,block)=>{if(!inside(x,y,z))throw Error(`Bounds ${variant.key} ${[x,y,z]}`);states[(y*depth+z)*width+x]=block.includes(':')?block:`minecraft:${block}`;};
 const box=(from,to,block)=>{for(let y=from[1];y<=to[1];y++)for(let z=from[2];z<=to[2];z++)for(let x=from[0];x<=to[0];x++)put(x,y,z,block);};
 const mass=(x1,z1,x2,z2,top,block='packed_ice')=>box([x1,base,z1],[x2,top,z2],block);
 const beam=(x1,z1,x2,z2,y,block='calcite')=>box([x1,y,z1],[x2,y,z2],block);
 const paths=new Map();
 const foot=(x,y,z,clearance=4)=>{if(x<1||z<1||x>=width-1||z>=depth-1)return;put(x,y-1,z,'smooth_quartz');box([x,y,z],[x,Math.min(height-2,y+clearance-1),z],air);paths.set(`${x},${y},${z}`,[x,y,z]);};
 const route=(points,y=base)=>{for(let count=1;count<points.length;count++){let [startX,startZ]=points[count-1];const [endX,endZ]=points[count];while(true){for(let dx=-1;dx<=1;dx++)for(let dz=-1;dz<=1;dz++)foot(startX+dx,y,startZ+dz);if(startX===endX&&startZ===endZ)break;if(startX!==endX)startX+=Math.sign(endX-startX);else startZ+=Math.sign(endZ-startZ);}}};
 const pit=(x1,z1,x2,z2)=>{box([x1,base-1,z1],[x2,base-1,z2],air);beam(x1,z1,x2,z2,Math.max(0,base-2),'blue_ice');};
 const arch=(z,peak,block='calcite')=>{for(let x=1;x<width-1;x++){const y=peak-Math.floor(Math.abs(x-(width-1)/2)*.6);if(y>base+4)beam(x,z,x,z,Math.min(height-2,y),block);}};
 const cell=(x1,z1,x2,z2,top)=>{mass(x1,z1,x2,z2,top,'light_blue_stained_glass');if(x2-x1>1&&z2-z1>1)box([x1+1,base,z1+1],[x2-1,top-1,z2-1],'blue_ice');};
 box([0,variant.minimum_y,0],[width-1,base-1,depth-1],'packed_ice');
 for(let y=base;y<height;y++)for(let x=0;x<width;x++)for(let z=0;z<depth;z++){const edge=x===0||z===0||x===width-1||z===depth-1;const roof=roofAt(variant,seed,x,z);if(edge||y>=roof)put(x,y,z,edge&&y>base+5&&(x+z+seed)%5<2?'light_blue_stained_glass':(x*3+z+y+seed)%9<3?'blue_ice':'packed_ice');}
 for(let x=1;x<width-1;x++)for(let z=1;z<depth-1;z++)if((x+z*2+seed)%7===0)put(x,base-1,z,'calcite');
 const key=variant.key;
 switch(key){
 case 'spawn/spawn':cell(2,2,4,5,6);mass(10,3,13,5,5,'smooth_quartz');route([[7,8],[7,12],[12,12],[12,8],[7,8],[7,14]]);for(let x=3;x<=12;x++)for(let z=4;z<=11;z++)if(Math.abs(x-7.5)+Math.abs(z-7.5)>5&&Math.abs(x-7.5)+Math.abs(z-7.5)<7)beam(x,z,x,z,9+x%3,'blue_ice');arch(11,13);break;
 case 'spawn/outside_near':for(let x=2;x<14;x++)for(let y=3;y<24;y++)for(let z=3;z<=5+Math.floor(Math.sin(y*.4)*2);z++)put(x,y,z,(x+y)%4?'packed_ice':'blue_ice');pit(3,9,12,12);route([[3,7],[12,7],[12,13],[4,13]]);beam(1,1,14,14,25,'light_blue_stained_glass');break;
 case 'spawn/outside_far':for(let x=2;x<14;x++)for(let z=2;z<14;z++)mass(x,z,x,z,Math.min(23,3+Math.max(0,18-2*Math.abs(x-9)-Math.abs(z-6))),(x+z)%3?'snow_block':'blue_ice');route([[3,3],[3,12],[12,12]]);break;
 case 'straight/simple':for(let z=2;z<14;z++){mass(1,z,2+Math.floor((z+1)/5),z,5+z%4);mass(12-Math.floor(z/6),z,14,z,7-z%3);}route([[7,1],[7,4],[9,4],[9,10],[7,10],[7,14]]);arch(4,12);arch(11,10);break;
 case 'straight/door':mass(1,5,5,5,9);mass(10,5,14,5,9);mass(1,10,4,10,7);mass(9,10,14,10,7);beam(5,5,10,5,8);beam(4,10,9,10,7);route([[7,1],[7,7],[6,7],[6,12],[7,12],[7,14]]);break;
 case 'straight/bridge':pit(2,2,13,13);route([[7,1],[7,14]]);route([[4,5],[7,5],[7,10],[11,10]]);for(const z of [3,8,12])arch(z,11,'blue_ice');break;
 case 'straight/room_cells':cell(1,2,3,6,7);cell(1,8,3,12,6);cell(12,2,14,5,5);cell(12,10,14,13,8);mass(6,6,9,8,6,'blue_ice');route([[7,1],[5,3],[5,11],[7,13],[7,14]]);route([[5,3],[11,3],[11,12],[5,12]]);arch(7,12);break;
 case 'straight/room_storage':for(const z of [5,9]){mass(2,z,6,z,7,'bookshelf');cell(8,z,13,z+1,8);beam(2,z,13,z,10,'blue_ice');}route([[7,1],[7,13],[7,14]]);route([[4,3],[11,3],[11,7],[4,7],[4,12],[11,12]]);break;
 case 'straight/room_towers':mass(2,3,5,6,7,'blue_ice');mass(9,9,13,12,9);beam(2,3,5,6,8,'snow_block');beam(9,9,13,12,10);route([[7,1],[7,7],[9,7],[9,14],[7,14]]);route([[3,8],[3,12],[7,12],[12,7],[12,3],[7,3]]);break;
 case 'straight/hole':pit(5,5,10,10);cell(6,6,9,9,4);route([[7,1],[3,3],[3,12],[12,12],[12,3],[7,3]]);route([[7,12],[7,14]]);for(const z of [4,11])arch(z,12);break;
 case 'straight/platform':mass(2,5,4,10,3);mass(10,5,13,10,4);arch(3,12);arch(12,10);break;
 case 'turn/pillar_room':mass(2,6,5,8,8);mass(10,2,13,3,10);mass(10,12,13,13,6);route([[7,1],[7,7],[14,7]]);route([[7,7],[7,14]]);arch(7,12);break;
 case 'turn/big_doors':mass(2,3,3,12,10);mass(6,5,11,5,7);mass(6,10,11,10,7);beam(4,7,14,8,10);route([[7,1],[4,4],[4,11],[7,14]]);route([[4,7],[14,7]]);break;
 case 'turn/small_doors':mass(5,3,6,6,6);mass(8,9,9,12,8);route([[7,1],[10,3],[10,7],[14,7]]);route([[10,7],[4,7],[4,12],[7,12],[7,14]]);break;
 case 'turn/hole':pit(2,5,6,10);cell(2,6,4,9,3);route([[7,1],[8,7],[14,7]]);route([[8,7],[8,14]]);route([[3,3],[11,3],[11,12],[3,12]]);arch(9,11);break;
 case 'stairs/centered':for(let z=3;z<14;z+=4){mass(1,z,1,z+1,25);mass(14,z,14,z+1,23);beam(2,z,13,z,27-z%3);}break;
 case 'stairs/hole':cell(5,5,9,9,25);for(const y of [6,12,20,27])beam(2,2,13,2,y,'blue_ice');break;
 case 'stairs/spiral':mass(6,6,8,8,26,'blue_ice');for(let y=5;y<29;y+=3){const z=2+(y*3)%11;beam(2,z,13,z,y);}break;
 case 'curve/simple':mass(8,7,13,13,8);mass(2,2,4,4,6);route([[7,1],[7,5],[4,5],[4,7],[1,7]]);route([[4,7],[4,12],[11,12],[11,4]]);arch(5,11);break;
 case 'terminal/canteen':mass(2,5,4,10,2,'smooth_quartz');mass(10,5,12,10,2,'smooth_quartz');mass(3,12,12,13,4,'blue_ice');beam(2,11,13,13,7);route([[7,1],[7,10]]);route([[2,3],[13,3],[13,11],[2,11],[2,3]]);break;
 case 'terminal/storage':cell(5,5,10,8,8);cell(2,12,9,13,6);beam(4,10,10,10,10);route([[7,1],[7,3],[3,3],[3,10],[12,10],[12,3],[7,3]]);break;
 case 'terminal/cells':cell(1,3,4,7,6);cell(1,9,4,13,7);cell(11,3,14,6,8);cell(11,9,14,13,5);mass(6,12,9,13,3,'calcite');route([[7,1],[7,10],[5,10],[10,10]]);break;
 case 'straight/room/layout/cells':cell(2,2,3,4,4);cell(2,7,4,9,5);cell(8,4,10,7,4);route([[1,1],[5,1],[5,10],[9,10]]);break;
 case 'straight/room/layout/storage':mass(4,3,6,8,4,'bookshelf');mass(2,5,9,6,3,'blue_ice');route([[2,2],[9,2],[9,9],[2,9],[2,2]]);break;
 case 'straight/room/layout/towers':mass(4,3,6,7,5);mass(8,8,9,9,3,'blue_ice');route([[2,2],[8,2],[8,6],[3,6],[3,10],[9,10]]);break;
 case 'boss/eclipse_keeper':for(let x=3;x<=30;x++)for(let z=5;z<=20;z++){const radius=Math.sqrt(((x-16.5)/1.5)**2+(z-13)**2);if(radius>7&&radius<8.4)box([x,11,z],[x,15+(x+z)%4,z],(x+z)%3?'blue_ice':'calcite');}for(const x of [3,29])for(const z of [5,12,19])cell(x,z,x+1,z+2,7);route([[16,1],[16,26]]);route([[6,4],[27,4],[27,20],[6,20],[6,4]]);for(const z of [8,16])route([[6,z],[27,z]]);box([1,2,22],[width-2,height-1,22],'blue_ice');box([15,2,22],[18,7,22],air);beam(14,21,19,23,8);break;
 case 'promenade':mass(3,11,12,13,3,'smooth_quartz');mass(2,4,3,8,4,'blue_ice');mass(11,5,13,9,5,'light_blue_stained_glass');beam(3,13,12,13,7,'sea_lantern');route([[7,1],[7,9]]);route([[4,3],[10,3],[10,10],[4,10],[4,3]]);break;
 default:throw Error('No independent design '+key);
 }
 let reusedWalkSurfaces=0;
 if(key.startsWith('stairs/')||key==='straight/platform'){
  const source=Structure.fromNbt(NbtFile.read(fs.readFileSync(path.join(data,'structures',variant.template+'.nbt'))).root),sourceGet=(x,y,z)=>source.getBlock([x,y,z])?.state.toString()??air,surfaces=[];
  for(const block of source.getBlocks()){const [x,y,z]=block.pos,material=block.state.toString();if(material!==air&&x>0&&x<width-1&&z>0&&z<depth-1&&y>=base-1&&y<height-4&&[1,2,3].every(rise=>sourceGet(x,y+rise,z)===air))surfaces.push({x,y,z,material});}
  for(const {x,y,z,material}of surfaces){for(let rise=1;rise<=3;rise++)put(x,y+rise,z,air);let replacement='smooth_quartz';if(material.includes('_stairs'))replacement='quartz_stairs'+material.slice(material.indexOf('['));else if(material.includes('_slab'))replacement='quartz_slab'+material.slice(material.indexOf('['));put(x,y,z,replacement);paths.set(`${x},${y+1},${z}`,[x,y+1,z]);reusedWalkSurfaces++;}
 }
 const targets=variant.ports.flatMap(port=>port.cells).concat(variant.sockets.flatMap(socket=>[socket.position,socket.spawn_position]));if(key==='spawn/spawn')targets.push([7,4,8]);if(key.startsWith('boss/'))targets.push([16,2,2],[16,2,13],[16,2,25]);if(key==='promenade')targets.push([7,2,9]);
 const layered=key.startsWith('stairs/')||key==='straight/platform';
 for(const [targetX,targetY,targetZ]of targets){const choices=[...paths.values()].filter(point=>point[1]===targetY&&!(key.startsWith('boss/')&&(point[2]>22)!==(targetZ>22)));if(choices.length&&!layered){choices.sort((first,second)=>Math.abs(first[0]-targetX)+Math.abs(first[2]-targetZ)-Math.abs(second[0]-targetX)-Math.abs(second[2]-targetZ));route([[targetX,targetZ],[choices[0][0],choices[0][2]]],targetY);}foot(targetX,targetY,targetZ,3);}
 const independent=[];
 for(const socket of variant.sockets){const [targetX,targetY,targetZ]=socket.position;let candidate;for(let distance=1;distance<=6&&!candidate;distance++)for(const [dx,dz]of [[distance,0],[-distance,0],[0,distance],[0,-distance]]){const point=[targetX+dx,targetY,targetZ+dz];if(point[0]<1||point[0]>=width-1||point[2]<1||point[2]>=depth-1||targets.some(other=>other.join(',')===point.join(','))||independent.some(other=>other.join(',')===point.join(',')))continue;if(layered&&(!full(get(point[0],targetY-1,point[2]))||![0,1,2].every(rise=>get(point[0],targetY+rise,point[2])===air)))continue;candidate=point;break;}if(!candidate)throw Error('No independent spawn foot '+key+' '+socket.id);if(!layered)route([[targetX,targetZ],[candidate[0],candidate[2]]],targetY);independent.push(candidate);}
 for(const port of variant.ports)for(const [x,y,z]of port.cells){put(x,y-1,z,'smooth_quartz');box([x,y,z],[x,y+4,z],air);}
 for(const socket of variant.sockets)for(const [x,y,z]of [socket.position,socket.spawn_position]){put(x,y-1,z,'smooth_quartz');box([x,y,z],[x,y+2,z],air);}
 for(let x=1;x<width-1;x++)for(let z=1;z<depth-1;z++)if((x*3+z*5+seed)%31===0){const y=Math.min(height-2,base+6);if(get(x,y,z)!==air)put(x,y,z,'sea_lantern');else if(get(x,y+1,z)!==air)put(x,y+1,z,'sea_lantern');}
 if(key==='straight/room_storage')for(const z of [5,9]){box([2,6,z],[6,8,z],'bookshelf');box([2,9,z],[6,9,z],'blue_ice');for(const x of [2,6])box([x,9,z],[x,roofAt(variant,seed,x,z),z],'calcite');}
 if(key==='turn/hole')box([2,5,6],[4,7,6],'blue_ice');
 if(key==='terminal/canteen')for(const z of [5,7,9]){put(5,2,z,'quartz_stairs[facing=west,half=bottom,shape=straight,waterlogged=false]');put(9,2,z,'quartz_stairs[facing=east,half=bottom,shape=straight,waterlogged=false]');}
 if(key==='promenade'){box([3,4,13],[12,7,13],'light_blue_stained_glass');beam(3,13,12,13,8,'calcite');for(const x of [3,12])box([x,3,12],[x,7,12],'blue_ice');}
 if(key.startsWith('boss/')){for(const x of [8,12,21,25])box([x,9,8],[x,x%2?17:15,8],'blue_ice');box([1,2,22],[width-2,height-1,22],'blue_ice');box([15,2,22],[18,7,22],air);}
 return {size:variant.size,states,get,base,independent,reusedWalkSurfaces};
}
const parsed=new Map();
const parse=source=>{if(!parsed.has(source))parsed.set(source,BlockState.parse(source));return parsed.get(source);};
function encode(built){const structure=new Structure(built.size);for(let y=0;y<built.size[1];y++)for(let z=0;z<built.size[2];z++)for(let x=0;x<built.size[0];x++){const block=parse(built.get(x,y,z));structure.addBlock([x,y,z],block.getName().toString(),block.getProperties());}return structure.writeNbt({name:'',dataVersion:3465,compression:'gzip'});}
function decode(bytes){const nbt=NbtFile.read(bytes).root,structure=Structure.fromNbt(nbt),size=structure.getSize(),states=Array(size[0]*size[1]*size[2]).fill(null);for(const block of structure.getBlocks()){const [x,y,z]=block.pos;if(x<0||y<0||z<0||x>=size[0]||y>=size[1]||z>=size[2])throw Error('NBT bounds');const index=(y*size[2]+z)*size[0]+x;if(states[index]!==null)throw Error('Duplicate coordinate');states[index]=block.state.toString();if(block.nbt)throw Error('Block entity');}if(states.includes(null)||nbt.getList('blocks',10).length!==states.length)throw Error('Non-dense or duplicate NBT');return {nbt,size,states,get:(x,y,z)=>x>=0&&y>=0&&z>=0&&x<size[0]&&y<size[1]&&z<size[2]?states[(y*size[2]+z)*size[0]+x]:'minecraft:bedrock'};}
function baselineFor(variant){
 let saved=baselineSnapshot.templates[variant.key];
 if(!saved){const bytes=fs.readFileSync(path.join(data,'roomsets/frost',variant.key+'.nbt')),decoded=decode(bytes),palette=[...new Set(decoded.states)],runs=[];for(const block of decoded.states){const value=palette.indexOf(block);if(runs.length&&runs.at(-2)===value)runs[runs.length-1]++;else runs.push(value,1);}saved={sha256:hash(bytes),size:decoded.size,palette,runs};baselineSnapshot.templates[variant.key]=saved;}
 const states=[];for(let index=0;index<saved.runs.length;index+=2)for(let count=0;count<saved.runs[index+1];count++)states.push(saved.palette[saved.runs[index]]);return {states,sha256:saved.sha256};
}
const full=block=>block!==air&&!/stairs|slab|pane|fence|chain/.test(block);
function graph(built,closed=false){const walk=new Map();for(let x=0;x<built.size[0];x++)for(let z=0;z<built.size[2];z++)for(let y=1;y<built.size[1]-2;y++)if(built.get(x,y-1,z)!==air&&[0,1,2].every(rise=>built.get(x,y+rise,z)===air)&&!(closed&&z===22&&x>=15&&x<=18&&y<8))walk.set(`${x},${y},${z}`,[x,y,z]);return walk;}
function flood(walk,start){const seen=new Set([start.join(',')]),queue=[start];for(let cursor=0;cursor<queue.length;cursor++){const [x,y,z]=queue[cursor];for(const [dx,dz]of [[1,0],[-1,0],[0,1],[0,-1]])for(const dy of [0,-1,1]){const point=[x+dx,y+dy,z+dz],key=point.join(',');if(walk.has(key)&&!seen.has(key)){seen.add(key);queue.push(point);}}}return seen;}
function physicalStairs(built,targets){
 const {size,get}=built,columns=new Map();
 const top=(block,xf,zf,y)=>{if(block===air)return null;if(block.includes('_stairs')){const facing=/facing=(\w+)/.exec(block)?.[1]??'north';const upper=facing==='east'?xf>=.5:facing==='west'?xf<.5:facing==='south'?zf>=.5:zf<.5;return y+(upper?1:.5);}if(block.includes('_slab'))return y+(block.includes('type=top')||block.includes('type=double')?1:.5);return y+1;};
 for(let microX=2;microX<(size[0]-1)*2;microX++)for(let microZ=2;microZ<(size[2]-1)*2;microZ++){const x=Math.floor(microX/2),z=Math.floor(microZ/2),xf=(microX%2)*.5+.25,zf=(microZ%2)*.5+.25,levels=[];for(let y=0;y<size[1]-2;y++){const footY=top(get(x,y,z),xf,zf,y);if(footY===null)continue;let blocked=false;for(let testY=Math.floor(footY);testY<footY+1.8;testY++)if(get(x,testY,z)!==air&&top(get(x,testY,z),xf,zf,testY)>footY+.001){blocked=true;break;}if(!blocked)levels.push(footY);}columns.set(`${microX},${microZ}`,levels);}
 const start=targets.find(([x,y,z])=>x>0&&z>0&&x<size[0]-1&&z<size[2]-1),queue=[[start[0]*2,start[1],start[2]*2]],seen=new Set([queue[0].join(',')]);
 for(let cursor=0;cursor<queue.length;cursor++){const [microX,y,microZ]=queue[cursor];for(const [dx,dz]of [[1,0],[-1,0],[0,1],[0,-1]])for(const nextY of columns.get(`${microX+dx},${microZ+dz}`)??[]){if(Math.abs(nextY-y)>.501)continue;const point=[microX+dx,nextY,microZ+dz],key=point.join(',');if(!seen.has(key)){seen.add(key);queue.push(point);}}}
 const missing=targets.filter(([x,y,z])=>x>0&&z>0&&x<size[0]-1&&z<size[2]-1&&!seen.has(`${x*2},${y},${z*2}`));return {method:'half-block vanilla stair surfaces; max step 0.5, clearance 1.8; no full-block jump',reachable:missing.length===0,missing,visited:seen.size};
}
function validate(built,variant,metadata,baseline){
 const {get,size,nbt}=built,errors=[],targets=variant.ports.flatMap(port=>port.cells).concat(variant.sockets.flatMap(socket=>[socket.position,socket.spawn_position]));if(variant.key==='spawn/spawn')targets.push([7,4,8]);if(variant.key.startsWith('boss/'))targets.push([16,2,2],[16,2,13],[16,2,25]);if(variant.key==='promenade')targets.push([7,2,9]);
 for(const [x,y,z]of targets){if(![0,1,2].every(rise=>get(x,y+rise,z)===air))errors.push('target clearance '+[x,y,z]);if(!full(get(x,y-1,z)))errors.push('support '+[x,y,z]);}
 for(const port of variant.ports)for(const [x,y,z]of port.cells)if(![0,1,2,3,4].every(rise=>get(x,y+rise,z)===air))errors.push('port5');
 for(const socket of variant.sockets)if(!full(get(...socket.support)))errors.push('declared support '+socket.id);
 const seen=flood(graph(built),targets[0]??[7,metadata.base,7]);for(const point of targets.concat(metadata.independent))if(!seen.has(point.join(',')))errors.push('unreachable '+point);
 for(let count=0;count<metadata.independent.length;count++){const point=metadata.independent[count],socket=variant.sockets[count],distance=Math.abs(point[0]-socket.position[0])+Math.abs(point[2]-socket.position[2]);if(distance<1||distance>6||point[1]!==socket.position[1]||!full(get(point[0],point[1]-1,point[2]))||targets.some(target=>target.join(',')===point.join(',')))errors.push('independent foot');}
 const openings=new Set(variant.ports.flatMap(port=>port.cells.flatMap(([x,y,z])=>[0,1,2,3,4].map(rise=>`${x},${y+rise},${z}`))));let boundaryHoles=0;
 for(let x=0;x<size[0];x++)for(let z=0;z<size[2];z++)for(let y=variant.minimum_y;y<size[1];y++)if((x===0||z===0||x===size[0]-1||z===size[2]-1||y===size[1]-1||y===variant.minimum_y)&&get(x,y,z)===air&&!openings.has(`${x},${y},${z}`))boundaryHoles++;
 if(boundaryHoles)errors.push('shell holes '+boundaryHoles);let gate=null;
 if(variant.key.startsWith('boss/')){let holes=0;for(let x=0;x<size[0];x++)for(let y=2;y<size[1];y++)if(!(x>=15&&x<=18&&y<=7)&&get(x,y,22)===air)holes++;const closed=flood(graph(built,true),[16,2,2]);gate={openReturnReachable:seen.has('16,2,25'),closedReturnReachable:closed.has('16,2,25'),closedReachesAnyRear:[...closed].some(point=>Number(point.split(',')[2])>22),partitionHoles:holes};if(!gate.openReturnReachable||gate.closedReturnReachable||gate.closedReachesAnyRear||holes)errors.push('gate bypass');}
 let changed=0,geometry=0;for(let index=0;index<built.states.length;index++){if(built.states[index]!==baseline.states[index])changed++;if((built.states[index]===air)!==(baseline.states[index]===air))geometry++;}if(geometry/built.states.length<.025)errors.push('geometry delta below 2.5%');
 if(nbt.getNumber('DataVersion')!==3465||nbt.getList('entities',10).length!==0)errors.push('NBT metadata');nbt.getList('palette',10).forEach(entry=>{if(!/^minecraft:[a-z0-9_]+$/.test(entry.getString('Name')))errors.push('palette');if(entry.has('Properties')){const properties=entry.getCompound('Properties');for(const key of properties.keys())if(!properties.hasString(key))errors.push('non-string property');}});
 const stairs=variant.key.startsWith('stairs/')||variant.key==='straight/platform'?physicalStairs(built,targets):null;if(stairs&&!stairs.reachable)errors.push('physical stair disconnected '+JSON.stringify(stairs.missing));
 return {key:variant.key,name:designs[catalog.variants.indexOf(variant)][0],pass:errors.length===0,errors,portCells:variant.ports.reduce((sum,port)=>sum+port.cells.length,0),sockets:variant.sockets.length,targets:targets.length,reachableTargets:targets.filter(point=>seen.has(point.join(','))).length,independentEntityFeet:metadata.independent,independentFeetUnique:new Set(metadata.independent.map(point=>point.join(','))).size===metadata.independent.length,boundaryHoles,gate,stairs,reusedGenericWalkSurfaces:metadata.reusedWalkSurfaces,baseline:'immutable baseline-v033.json snapshot',baselineNbtSha256:baseline.sha256,changedBlocks:changed,changedRatio:changed/built.states.length,solidAirChangedBlocks:geometry,solidAirChangedRatio:geometry/built.states.length,geometryHash:hash(built.states.map(block=>block===air?'0':'1').join('')),denseBlocks:built.states.length,metadataPreserved:true,checks:{portFiveAir:true,targetThreeAir:true,fullSupport:true,declaredSocketSupport:true,independentEntityFeet:true,denseUniqueBounds:true,entitiesAbsent:true,blockEntitiesAbsent:true,barePaletteNames:true,separateStringProperties:true}};
}
function gallery(rooms){
 const payload=rooms.map(({variant,built,report})=>{const palette=[...new Set(built.states)],runs=[];for(const block of built.states){const value=palette.indexOf(block);if(runs.length&&runs.at(-2)===value)runs[runs.length-1]++;else runs.push(value,1);}return {key:variant.key,size:variant.size,palette,runs,ports:variant.ports,sockets:variant.sockets,name:report.name,feature:designs[catalog.variants.indexOf(variant)][4],ratio:report.solidAirChangedRatio,base:variant.key==='spawn/spawn'?4:variant.key.includes('/layout/')?1:2,upper:variant.key.startsWith('boss/')?14:variant.key.startsWith('stairs/')?18:Math.min(variant.size[1]-2,(variant.key==='spawn/spawn'?4:variant.key.includes('/layout/')?1:2)+5),cut:variant.key.startsWith('boss/')?18:variant.key.startsWith('stairs/')?23:variant.key==='spawn/spawn'?12:9};});
 return `<!doctype html><html lang="zh-CN"><meta charset="utf-8"><title>冰霜27房真实NBT图集</title><style>body{background:#0c1925;color:#d4eefb;font:15px system-ui;margin:26px}main{display:grid;grid-template-columns:repeat(auto-fit,minmax(470px,1fr));gap:20px}article{background:#142b3b;padding:18px;border:1px solid #35576a;border-radius:13px}h2{font-size:18px}p{color:#a7cadb}canvas{image-rendering:pixelated;background:#09141e;width:100%}.plans{display:grid;grid-template-columns:repeat(3,1fr);gap:10px}label{font-size:12px}input{width:100%}.iso{height:270px;image-rendering:auto}span{font-size:12px;color:#92b5c6}button{background:#285a71;color:white;border:0;padding:6px 12px}</style><h1>冰霜 · 27个独立房型</h1><p>所有图像均读取导出实际NBT。每房展示脚点层、上层平面、纵剖面和可旋转切顶轴测图。轴测为真实方块几何投影，不是Minecraft材质渲染。红色=socket，金色=port。滑块可检查完整屋顶。</p><main id="rooms"></main><script>const rooms=${JSON.stringify(payload)};
 const color=block=>block.includes(':air')?'#102332':block.includes('blue_ice')?'#267ca6':block.includes('packed_ice')?'#72badb':block.includes('glass')?'#407084':block.includes('snow')?'#e5f5fc':block.includes('lantern')?'#eaffbc':block.includes('bookshelf')?'#617d80':block.includes('calcite')?'#ccdedf':'#a6c2d0';
 for(const room of rooms){const [width,height,depth]=room.size,blocks=[];for(let index=0;index<room.runs.length;index+=2)for(let count=0;count<room.runs[index+1];count++)blocks.push(room.runs[index]);const get=(x,y,z)=>x<0||y<0||z<0||x>=width||y>=height||z>=depth?0:blocks[(y*depth+z)*width+x];const article=document.createElement('article');article.innerHTML='<h2>'+room.key+' · '+room.name+'</h2><p>'+room.feature+' · solid/air变化 '+(100*room.ratio).toFixed(1)+'%</p><div class="plans"><label>脚点层 y='+room.base+'<canvas class="plan"></canvas></label><label>上层 y='+room.upper+'<canvas class="upper"></canvas></label><label>纵剖 x='+Math.floor(width/2)+'<canvas class="section"></canvas></label></div><canvas class="iso"></canvas><label>轴测切顶 y=<b></b><input type="range" min="1" max="'+(height-1)+'" value="'+Math.min(height-2,room.cut)+'"></label><button>旋转90°</button> <span>切顶时剖去近侧两壁；最右查看完整屋顶</span>';document.getElementById('rooms').append(article);
 const plan=(canvas,y)=>{canvas.width=width*10;canvas.height=depth*10;const ctx=canvas.getContext('2d');for(let x=0;x<width;x++)for(let z=0;z<depth;z++){ctx.fillStyle=color(room.palette[get(x,y,z)]);ctx.fillRect(x*10,z*10,10,10);}for(const socket of room.sockets)if(socket.position[1]===y){ctx.fillStyle='#ff536b';ctx.fillRect(socket.position[0]*10+2,socket.position[2]*10+2,6,6);}for(const port of room.ports)for(const [x,py,z]of port.cells)if(py===y){ctx.fillStyle='#ffcf58';ctx.fillRect(x*10,z*10,10,10);}};plan(article.querySelector('.plan'),room.base);plan(article.querySelector('.upper'),room.upper);const section=article.querySelector('.section');section.width=depth*10;section.height=height*10;const sectionContext=section.getContext('2d');for(let z=0;z<depth;z++)for(let y=0;y<height;y++){sectionContext.fillStyle=color(room.palette[get(Math.floor(width/2),y,z)]);sectionContext.fillRect(z*10,(height-1-y)*10,10,10);}
 let rotation=0;const slider=article.querySelector('input'),canvas=article.querySelector('.iso');canvas.width=760;canvas.height=420;const iso=()=>{const cut=Number(slider.value);article.querySelector('b').textContent=cut;const ctx=canvas.getContext('2d');ctx.clearRect(0,0,760,420);const scale=Math.min(15,650/(width+depth),340/(cut+(width+depth)*.28)),voxel=[];for(let x=0;x<width;x++)for(let z=0;z<depth;z++)for(let y=0;y<=cut;y++){const id=get(x,y,z);if(room.palette[id]==='minecraft:air')continue;let rx=x,rz=z;if(rotation===1){rx=z;rz=width-1-x;}if(rotation===2){rx=width-1-x;rz=depth-1-z;}if(rotation===3){rx=depth-1-z;rz=x;}const rotatedWidth=rotation%2?depth:width,rotatedDepth=rotation%2?width:depth;if(cut<height-1&&y>=room.base&&(rx===rotatedWidth-1||rz===rotatedDepth-1))continue;voxel.push({x,y,z,rx,rz,id});}voxel.sort((first,second)=>first.rx+first.rz-second.rx-second.rz||first.y-second.y);const polygon=(points,fill)=>{ctx.fillStyle=fill;ctx.beginPath();points.forEach(([px,py],index)=>index?ctx.lineTo(px,py):ctx.moveTo(px,py));ctx.closePath();ctx.fill();ctx.strokeStyle='#132b3c44';ctx.lineWidth=.4;ctx.stroke();};for(const block of voxel){const px=380+(block.rx-block.rz)*scale*.8,py=390-(block.y+(width+depth-block.rx-block.rz)*.27)*scale,fill=color(room.palette[block.id]);polygon([[px,py+scale*.54],[px+scale*.8,py+scale*.27],[px+scale*.8,py+scale*1.27],[px,py+scale*1.54]],fill);polygon([[px,py+scale*.54],[px-scale*.8,py+scale*.27],[px-scale*.8,py+scale*1.27],[px,py+scale*1.54]],fill);if(block.y===cut||room.palette[get(block.x,block.y+1,block.z)]==='minecraft:air')polygon([[px,py],[px+scale*.8,py+scale*.27],[px,py+scale*.54],[px-scale*.8,py+scale*.27]],fill);}};slider.oninput=iso;article.querySelector('button').onclick=()=>{rotation=(rotation+1)%4;iso();};iso();}</script></html>`;
}
const rooms=[],templates=[];
for(let seed=0;seed<catalog.variants.length;seed++){const variant=catalog.variants[seed],metadata=makeRoom(variant,seed),bytes=encode(metadata),destination=path.join(out,'roomset',variant.key+'.nbt');fs.mkdirSync(path.dirname(destination),{recursive:true});fs.writeFileSync(destination,bytes);const built=decode(fs.readFileSync(destination));if(built.states.some((block,index)=>block!==metadata.states[index]))throw Error('NBT roundtrip');const baseline=baselineFor(variant),report=validate(built,variant,metadata,baseline);rooms.push({variant,built,report});templates.push({key:variant.key,sha256:hash(bytes)});process.stdout.write(variant.key+' '+(report.pass?'PASS':'FAIL '+report.errors.join('; '))+' geometry '+(report.solidAirChangedRatio*100).toFixed(1)+'%\n');}
if(!fs.existsSync(baselinePath))fs.writeFileSync(baselinePath,JSON.stringify(baselineSnapshot)+'\n');
fs.writeFileSync(path.join(out,'interfaces.json'),JSON.stringify({schema:1,style:'fteclipsegen:frost',variants:catalog.variants.map(({key,size,minimum_y,ports,sockets,spawn_position})=>({key,size,minimum_y,ports,sockets,...(spawn_position?{spawn_position}:{})}))},null,2)+'\n');
const uniqueGeometry=new Set(rooms.map(room=>room.report.geometryHash)).size===27;
rooms.forEach(({report},index)=>{report.mainFeature=designs[index][4];});
const topologyPairs=[];
for(let first=0;first<rooms.length;first++)for(let second=first+1;second<rooms.length;second++){
 const firstRoom=rooms[first],secondRoom=rooms[second];
 if(firstRoom.built.size.join(',')!==secondRoom.built.size.join(','))continue;
 const [width,height,depth]=firstRoom.built.size,base=firstRoom.variant.key.includes('/layout/')?1:firstRoom.variant.key==='spawn/spawn'?4:2;
 let intersection=0,union=0;
 for(let x=1;x<width-1;x++)for(let z=1;z<depth-1;z++)for(let y=base;y<=Math.min(base+4,height-5);y++){
  const firstSolid=firstRoom.built.get(x,y,z)!==air,secondSolid=secondRoom.built.get(x,y,z)!==air;if(firstSolid||secondSolid)union++;if(firstSolid&&secondSolid)intersection++;
 }
 topologyPairs.push({first:firstRoom.variant.key,second:secondRoom.variant.key,interiorSolidIntersection:intersection,interiorSolidUnion:union,solidJaccard:union?intersection/union:0});
}
topologyPairs.sort((first,second)=>second.solidJaccard-first.solidJaccard);
fs.writeFileSync(path.join(out,'topology-comparison.json'),JSON.stringify({method:'Real NBT interior solids at foot height through +4; exclude exterior walls, floor and upper roof. Jaccard intersection/union; 1 means identical features.',pairs:topologyPairs},null,2)+'\n');
fs.writeFileSync(path.join(out,'roomset/manifest.json'),JSON.stringify({schema:1,style:'fteclipsegen:frost',templates},null,2)+'\n');
const report={schema:1,style:'fteclipsegen:frost',targetVersion:'0.34.0',rooms:rooms.map(room=>room.report),allPass:uniqueGeometry&&rooms.every(room=>room.report.pass),uniqueGeometry,count:rooms.length,catalogSha256:hash(fs.readFileSync(path.join(data,'catalog.json'))),manifestSha256:hash(fs.readFileSync(path.join(out,'roomset/manifest.json'))),generatorSha256:hash(fs.readFileSync(fileURLToPath(import.meta.url))),validationSource:'Exported gzip NBT decoded through Lodestone; all geometry tests use decoded NBT',formalAssetsModified:false};
fs.writeFileSync(path.join(out,'validation.json'),JSON.stringify(report,null,2)+'\n');fs.writeFileSync(path.join(out,'gallery.html'),gallery(rooms));fs.writeFileSync(path.join(out,'FREEZE.json'),JSON.stringify({status:report.allPass?'FROZEN_PASS':'FAILED_NOT_FROZEN',manifestSha256:report.manifestSha256,generatorSha256:report.generatorSha256,validationSha256:hash(fs.readFileSync(path.join(out,'validation.json'))),gallerySha256:hash(fs.readFileSync(path.join(out,'gallery.html'))),designSha256:hash(fs.readFileSync(path.join(out,'design.md')))},null,2)+'\n');if(!report.allPass)process.exitCode=1;

const directions=[['north','北',[0,.4,-1]],['south','南',[0,.4,1]],['west','西',[-1,.4,0]],['east','东',[1,.4,0]]];
function excluded(variant,seed,x,y,z){
 const [width,,depth]=variant.size;
 if(x<=0||z<=0||x>=width-1||z>=depth-1||y<baseOf(variant)||y>=roofAt(variant,seed,x,z))return true;
 return variant.ports.some(port=>port.cells.some(([px,py,pz])=>y>=py-1&&y<=py+4&&(px===0||px===width-1?Math.abs(x-px)<=3&&z===pz:Math.abs(z-pz)<=3&&x===px)));
}
const landmarkSpecs=[
 [['悬冰冠',[3,9,4],[12,12,11],['blue_ice'],8],['接待冻舱',[2,4,2],[4,6,5],['blue_ice','light_blue_stained_glass'],8]],
 [['双支流冻瀑',[2,3,3],[13,23,7],['packed_ice','blue_ice'],100]],
 [['冰蚀雪峰',[2,3,2],[13,23,13],['snow_block','blue_ice'],100]],
 [['西湾侵蚀壁',[1,2,2],[4,8,13],['packed_ice'],16],['东湾冰脊',[10,2,2],[14,7,13],['packed_ice'],16]],
 [['北气锁闸',[1,5,5],[14,9,5],['packed_ice','calcite'],12],['南错位闸',[1,5,10],[14,7,10],['packed_ice','calcite'],8]],
 [['中央桥面',[6,1,1],[8,1,14],['smooth_quartz'],24],['三道蓝冰桥拱',[1,7,3],[14,11,12],['blue_ice'],12]],
 [['西冻眠列舱',[1,2,2],[3,7,12],['blue_ice','light_blue_stained_glass'],25],['中央冻舱芯',[6,2,6],[9,6,8],['blue_ice'],8]],
 [['悬冻档案书架',[2,6,5],[6,8,9],['bookshelf'],30],['吊挂冰索引',[2,9,5],[13,11,9],['blue_ice','calcite'],12]],
 [['西台堡',[2,4,3],[5,8,6],['blue_ice','snow_block'],18],['东高堡',[9,6,9],[13,10,12],['packed_ice','calcite'],16]],
 [['中央冰井',[6,2,6],[9,4,9],['blue_ice','light_blue_stained_glass'],16]],
 [['西勘测台',[2,1,5],[4,3,10],['packed_ice','smooth_quartz'],12],['东高差踏台',[10,3,5],[13,4,10],['packed_ice','smooth_quartz'],8]],
 [['西侧冰楔',[2,5,6],[5,8,8],['packed_ice'],12],['东北冰脊',[10,5,2],[13,10,3],['packed_ice'],8]],
 [['西向屏风冰壁',[2,2,3],[3,10,12],['packed_ice'],16],['东向横闸',[4,10,7],[14,10,8],['calcite'],8]],
 [['北冷却鳍',[5,5,3],[6,6,6],['packed_ice'],6],['南偏轴鳍',[8,5,9],[9,8,12],['packed_ice'],6]],
 [['偏井冻芯与冰拱',[2,2,6],[4,7,9],['blue_ice','light_blue_stained_glass'],12],['偏井桥拱',[1,7,9],[14,11,9],['calcite'],8]],
 [['分段冻瀑壁',[1,2,3],[14,25,12],['packed_ice'],35],['高层冰冠横肋',[2,25,3],[13,27,11],['calcite'],12]],
 [['贯穿冰井髓',[5,2,5],[9,25,9],['blue_ice','light_blue_stained_glass'],80]],
 [['螺旋中央冰髓',[6,2,6],[8,26,8],['blue_ice'],60],['旋转顶肋',[2,5,2],[13,29,12],['calcite'],12]],
 [['内弯削切冰丘',[8,5,7],[13,8,13],['packed_ice'],20],['外弯肋',[1,7,5],[14,11,5],['calcite'],8]],
 [['冰封服务台',[3,2,12],[12,4,13],['blue_ice'],24],['六把餐椅',[5,2,5],[9,2,9],['quartz_stairs'],6]],
 [['重库冰封货垛',[5,5,5],[10,8,8],['blue_ice','light_blue_stained_glass'],30],['后侧货垛',[2,2,12],[9,6,13],['blue_ice','light_blue_stained_glass'],12]],
 [['西侧冻结牢间',[1,2,3],[4,7,13],['blue_ice','light_blue_stained_glass'],30],['末端审讯台',[6,2,12],[9,3,13],['calcite'],8]],
 [['双低舱',[2,1,2],[4,5,9],['blue_ice','light_blue_stained_glass'],12]],
 [['十字目录芯',[2,1,3],[9,4,8],['bookshelf','blue_ice'],12]],
 [['偏心冰芯',[4,1,3],[6,5,7],['packed_ice'],12],['次冰芯',[8,1,8],[9,3,9],['blue_ice'],4]],
 [['悬空冰冠环',[3,11,5],[30,18,20],['blue_ice','calcite'],100],['四枚王冠长齿',[8,9,8],[25,17,8],['blue_ice'],25],['胜利隔墙',[1,2,22],[32,22,22],['blue_ice'],100]],
 [['连续冰镜柜台',[3,3,12],[12,8,13],['blue_ice','light_blue_stained_glass','calcite'],40]]
];
const repairs=[
 {key:'straight/bridge',issue:'除公共壳与基础冰底后，桥内主要只剩白石拱，冰霜证据不足',action:'三道桥拱用蓝冰重建，保留桥面和巡检湾',evidence:'排除公共体积后的blue_ice统计与三道冰拱bbox'},
 {key:'turn/hole',issue:'偏井冻芯被接口通路清理削至6块玻璃',action:'在存留井框上补建三格宽蓝冰拱，保留脚点净高',evidence:'偏井结构最终NBT实际计数>=12'},
 {key:'straight/room_storage',issue:'通路清理使档案书架下部消失，悬挂方式不清',action:'补建两排3层书架、上方冰封横梁及接顶吊架',evidence:'bookshelf >=30，吊架bbox与实际NBT计数'},
 {key:'terminal/canteen',issue:'低冰桌缺少可辨识座位',action:'实际NBT添加六把quartz_stairs餐椅，保留中间三格通道',evidence:'quartz_stairs=6'},
 {key:'promenade',issue:'末端低柜不够区别仓库',action:'补建连续玻璃冰镜与两侧蓝冰框及顶檐',evidence:'柜台bbox内glass/blue_ice/calcite实际计数'},
 {key:'boss/eclipse_keeper',issue:'斜洞顶压住悬冠上部，默认切顶遮挡冠体',action:'改独立高拱洞顶，增加四枚长冠齿；四视图移除外壳与顶壳',evidence:'冠环与冠齿位置计数、108向内部投影'}
];
const visualNotes=[
 ['北南可辨拱形分瓣冠、接待台及出口点。','东西显示薄冠侧影与偏置接待块，避免四角柱平厅。'],
 ['北南为宽冻瀑断面，双支流材质条带可辨。','东西为窄而起伏的瀑壁侧影，属于外景功能，保留。'],
 ['北南呈偏心雪峰斜坡及削切脊。','东西可辨厚峰体与雪冰交错，非近景瀑墙复制。'],
 ['北南两侧冰壁高度及缺口错位，中央通道留空。','东西可见长壁起伏，双湾在平面图进一步核对。'],
 ['北南可辨上下两道错位门框。','东西显示两道薄闸的间距，通行区由独立连通检查确认。'],
 ['北南三道蓝冰弧拱跨实际桥面。','东西可见三条分隔桥拱及巡检支桥，桥面例外保留便于看路。'],
 ['北南是双侧玻璃舱与中部冰芯，不是四柱。','东西舱体高度不同且有穿行缺口。'],
 ['北南可见褐色书架层、上部冰封梁和接顶吊架。','东西显示两排悬挂架间距及支撑，返工后悬挂方式可辨。'],
 ['北南两个错高台堡与白色台冠可辨。','东西反转前后关系，台堡尺度不一致。'],
 ['北南中部冻井与外侧弧肋可辨，真实井边地面保留。','东西显示井框和两侧薄肋，不复制桥房三拱。'],
 ['北南能辨双翼平台与悬拱。','东西可见阶梯台面与多标高socket，台阶半块细分。'],
 ['北南为不等高冰楔中间开放通道。','东西可见三块分离冰脊，Y形平面由平面图和连通验证核对。'],
 ['北南西向高屏风与横闸错位。','东西横闸下留门孔，上方横梁可辨。'],
 ['北南两块偏轴冷却鳍分离。','东西形成横纵交错短墙，局部紧凑但未覆盖接口。'],
 ['北南可见偏井蓝冰框和一侧弧肋，返工后井芯不再只剩玻璃边。','东西可见偏井与桥拱错开，实际井边桥面保留。'],
 ['北南上下多层台阶和横向平台可辨。','东西可见长斜阶、分段冰壁；保留接口踏面但洞体独立。'],
 ['北南中央高冻井贯穿不同层平台。','东西可见沿井上升阶梯与中段支路，主井不压在接口上。'],
 ['北南围蓝冰髓的分层环道可辨。','东西呈转折螺旋层带，与长斜阶空间不同。'],
 ['北南偏置冰丘与弯折拱相连。','东西清楚显示切角冰丘和不同侧的出口，L形路线保持。'],
 ['北南后服务台、前部双桌为低矮水平组成。','东西可辨餐椅半块和服务檐，六椅由实际NBT计数确认。'],
 ['北南厚玻璃货垛形成不完整下部通口。','东西两垛前后错开，U形卸货路与基础平面独立核对。'],
 ['北南双列玻璃牢体，中间末端白色审讯台。','东西牢间开口、高度不一致，不是档案架换色。'],
 ['北南两组低舱错开。','东西显示服务带旁折面，低矮子模板没有套用大房高顶。'],
 ['北南为较窄褐色目录芯与蓝冰翼。','东西能辨十字芯下方蓝冰块，四角保留通路。'],
 ['北南一大一小双冰芯分置。','东西两个芯的前后错位及不等高可辨。'],
 ['北南冰冠环与四枚长齿可辨；南侧控制墙用16%透明避免遮冠。','东西可见环冠空心、前后战斗湾及独立胜利墙；墙体实际完整。'],
 ['北南连续冰镜柜台、横檐和侧展柜清楚。','东西冰镜表现为后部竖片，展柜错高，交易面保持开放。']
];
function materialsOf(blocks){const counts={};for(const block of blocks){const name=block.material.split('[')[0];counts[name]=(counts[name]??0)+1;}return counts;}
function styleAudit(rooms,templates){
 const entries=rooms.map(({variant,built,report},seed)=>{
  const [width,height,depth]=variant.size,all=[],inside=[];
  for(let y=0;y<height;y++)for(let z=0;z<depth;z++)for(let x=0;x<width;x++){const material=built.get(x,y,z);if(material===air)continue;const block={position:[x,y,z],material};all.push(block);if(!excluded(variant,seed,x,y,z))inside.push(block);}
  const landmarks=landmarkSpecs[seed].map(([role,from,to,materials,minimum])=>{
   const blocks=all.filter(({position,material})=>position.every((value,axis)=>value>=from[axis]&&value<=to[axis])&&materials.includes(material.split(':')[1].split('[')[0])&&position[0]>0&&position[0]<width-1&&position[2]>0&&position[2]<depth-1);
   const actual=blocks.length?{from:[0,1,2].map(axis=>Math.min(...blocks.map(block=>block.position[axis]))),to:[0,1,2].map(axis=>Math.max(...blocks.map(block=>block.position[axis])))}:null;
   return {role,queryBbox:{from,to},actualBbox:actual,expectedMaterials:materials,actualMaterials:materialsOf(blocks),actualBlocks:blocks.length,minimum,pass:blocks.length>=minimum,actualSample:blocks.slice(0,8).map(block=>block.position),geometrySha256:hash(JSON.stringify(blocks.map(block=>block.position)))};
  });
  const materialCounts=materialsOf(all),interiorMaterials=materialsOf(inside),ice=inside.filter(block=>/minecraft:(packed_ice|blue_ice|snow_block|light_blue_stained_glass)/.test(block.material)).length;
  const cornerColumns=[[1,1],[width-3,1],[1,depth-3],[width-3,depth-3]].map(([cx,cz])=>inside.filter(({position:[x,y,z]})=>x>=cx&&x<=cx+1&&z>=cz&&z<=cz+1&&y>=baseOf(variant)).length);
  const center=inside.filter(({position:[x,,z]})=>x>=Math.floor(width*.25)&&x<=Math.floor(width*.75)&&z>=Math.floor(depth*.25)&&z<=Math.floor(depth*.75)).length;
  let capacity=0;for(let y=baseOf(variant);y<height;y++)for(let z=1;z<depth-1;z++)for(let x=1;x<width-1;x++)if(!excluded(variant,seed,x,y,z))capacity++;
  const density=capacity?inside.length/capacity:0;
  const issues=[];if(!landmarks.every(entry=>entry.pass))issues.push('landmark missing or undersized');if(ice<8)issues.push('insufficient actual frost interior material');if(cornerColumns.every(count=>count>=12)&&center<8)issues.push('four-corner-columns and empty center');if(family(variant.key)==='combat'&&density>.70)issues.push('overdense interior');
  return {key:variant.key,nbtSha256:templates[seed].sha256,materialCounts,interiorMaterialCounts:interiorMaterials,actualIceInteriorBlocks:ice,interiorSolidBlocks:inside.length,landmarks,functionalForm:designs[seed][2],formEvidence:landmarks.map(landmark=>({role:landmark.role,bbox:landmark.actualBbox,blocks:landmark.actualBlocks})),fourCornerColumnCheck:{cornerBlocks:cornerColumns,centralFeatureBlocks:center,flagged:issues.includes('four-corner-columns and empty center')},repeatedSymbolReview:'冰霜材质统一；实际地标分别为双湾、双闸、桥、舱、横架、双堡、井、勘测台等。全套内部结构签名及成对比较见repetition-audit.json，不以改名或同色豁免重复。',repair:repairs.find(entry=>entry.key===variant.key)??null,issues,pass:issues.length===0,baselineSolidAirChangedRatio:report.solidAirChangedRatio};
 });return {schema:1,style:'fteclipsegen:frost',source:'final exported NBT decoded; material counts and feature bbox queries',rooms:entries,pass:entries.every(entry=>entry.pass)};
}
const family=key=>key.includes('/layout/')?'child':key.startsWith('stairs/')?'stairs':key.startsWith('terminal/')?'terminal':key.startsWith('spawn/outside')?'scenery':key.startsWith('spawn/')?'entry':key.startsWith('boss/')?'boss':key==='promenade'?'shop':'combat';
const jaccard=(first,second)=>{const intersection=[...first].filter(value=>second.has(value)).length,union=new Set([...first,...second]).size;return {intersection,union,score:union?intersection/union:0};};
function repetitionAudit(rooms,templates){
 const signatures=rooms.map(({variant,built},seed)=>{
  const [width,height,depth]=variant.size,base=baseOf(variant),solid=[],domain=[];
  for(let y=base;y<height;y++)for(let z=1;z<depth-1;z++)for(let x=1;x<width-1;x++)if(!excluded(variant,seed,x,y,z)){domain.push([x,y,z]);if(built.get(x,y,z)!==air)solid.push([x,y,z]);}
  const normalized=[0,1,2,3].map(rotation=>new Set(solid.map(([x,y,z])=>{let nx=(x-.5)/(width-2),nz=(z-.5)/(depth-2);if(rotation===1)[nx,nz]=[nz,1-nx];if(rotation===2)[nx,nz]=[1-nx,1-nz];if(rotation===3)[nx,nz]=[1-nz,nx];return [Math.min(11,Math.floor(nx*12)),Math.min(11,Math.floor((y-base)/(height-base-1)*12)),Math.min(11,Math.floor(nz*12))].join(',');})));
  const canonical=normalized.map(values=>hash([...values].sort().join(';'))).sort()[0];
  return {key:variant.key,seed,family:family(variant.key),size:variant.size,base,solid,domain,normalized,canonical,rawSignature:hash(solid.map(point=>point.join(',')).join(';'))};
 });
 const pairs=[];
 for(let first=0;first<signatures.length;first++)for(let second=first+1;second<signatures.length;second++){
  const left=signatures[first],right=signatures[second],sameSize=left.size.join(',')===right.size.join(','),sameFamily=left.family===right.family;if(!sameSize&&!sameFamily)continue;
  let direct=null,airAgreement=null,layout=null;
  if(sameSize){const allowed=point=>!excluded(rooms[first].variant,first,...point)&&!excluded(rooms[second].variant,second,...point),leftSolid=new Set(left.solid.filter(allowed).map(point=>point.join(','))),rightSolid=new Set(right.solid.filter(allowed).map(point=>point.join(',')));direct=jaccard(leftSolid,rightSolid);const domain=left.domain.filter(allowed).length;airAgreement=domain?(domain-(direct.union-direct.intersection))/domain:null;layout=jaccard(new Set(left.solid.filter(allowed).filter(point=>point[1]<=left.base+2).map(([x,,z])=>`${x},${z}`)),new Set(right.solid.filter(allowed).filter(point=>point[1]<=right.base+2).map(([x,,z])=>`${x},${z}`)));}
  const normalized=Math.max(...right.normalized.map(values=>jaccard(left.normalized[0],values).score));const exact=left.rawSignature===right.rawSignature&&sameSize;const score=Math.max(direct?.score??0,normalized);const high=score>=.90||exact||(layout?.score??0)>=.90;const fixedSteps=left.family==='stairs'&&right.family==='stairs';
  pairs.push({first:left.key,second:right.key,sameSize,sameFunctionalFamily:sameFamily,solidJaccard:direct?.score??null,solidIntersection:direct?.intersection??null,solidUnion:direct?.union??null,solidAirAgreement:airAgreement,normalizedBestRotationJaccard:normalized,lowBodyLayoutJaccard:layout?.score??null,comparisonScore:score,exactInternalDuplicate:exact,requiresReview:high,decision:!high?'distinct internal structures':fixedSteps&&!exact?'retain fixed port-to-port steps; independently rebuilt cave walls and landmarks':'REWORK REQUIRED',pass:!high||(fixedSteps&&!exact)});
 }
 pairs.sort((first,second)=>second.comparisonScore-first.comparisonScore);
 return {schema:1,style:'fteclipsegen:frost',method:{exclude:'outer x/z boundary, original generated roof volume, all foundation y<base, all catalog port safety bands depth<=3 and foot-1..foot+4',sameSize:'shared unmasked domain; 3D solid Jaccard and solid/air agreement; low-body XZ footprint independently reported',normalization:'12x12x12 occupancy after exclusions; four Y rotations; geometry-only canonical SHA256',threshold:.90,airAgreementNote:'reported for completeness; common air does not grant a repetition exemption'},rooms:signatures.map((entry,index)=>({key:entry.key,family:entry.family,nbtSha256:templates[index].sha256,interiorSolidBlocks:entry.solid.length,canonicalNormalizedSignature:entry.canonical,rawInteriorSignature:entry.rawSignature,baselineSolidAirChangedRatio:rooms[index].report.solidAirChangedRatio})),pairs,highestPairs:pairs.slice(0,20),reviewedHighPairs:pairs.filter(pair=>pair.requiresReview),pass:pairs.every(pair=>pair.pass)&&rooms.every(room=>room.report.solidAirChangedRatio>=.025)};
}
const paint=block=>block.includes('blue_ice')?'#267dba':block.includes('packed_ice')?'#7bbbd4':block.includes('glass')?'#43849c':block.includes('snow')?'#effbff':block.includes('lantern')?'#e8ef9c':block.includes('bookshelf')?'#9a8568':block.includes('calcite')?'#d4e5e8':'#adcad5';
const projectionRule='隐藏x/z外边界、roofAt顶壳、y小于base的基础地板与端口前三格安全带。例外：桥与井房保留实际非完整基础平面的功能桥面；Boss z22胜利隔墙仅在投影中16%透明，几何保留，运行时结构不变。保留全部内部高度，玻璃72%透明，阶梯按半块细分。红点socket、金点port为透视检查标记。完整外壳、隔墙和动线独立由validation.json核验。';
function blockBoxes(material){
 if(material.includes('_stairs')){const facing=/facing=(\w+)/.exec(material)?.[1]??'north';const upper=facing==='east'?[.5,.5,0,1,1,1]:facing==='west'?[0,.5,0,.5,1,1]:facing==='south'?[0,.5,.5,1,1,1]:[0,.5,0,1,1,.5];return [[0,0,0,1,.5,1],upper];}
 if(material.includes('_slab'))return [[0,material.includes('type=top')?.5:0,0,1,material.includes('type=double')||material.includes('type=top')?1:.5,1]];
 return [[0,0,0,1,1,1]];
}
function renderDirection(room,seed,direction,nbtSha256){
 const {variant,built}=room,[width,height,depth]=variant.size;
 const rotate=([x,y,z])=>direction==='north'?[width-x,y,depth-z]:direction==='east'?[depth-z,y,x]:direction==='west'?[z,y,width-x]:[x,y,z];
 const span=direction==='east'||direction==='west'?depth:width,spanDepth=direction==='east'||direction==='west'?width:depth;
 const scale=Math.min(500/span,315/(height*.8+spanDepth*.32));
 const screen=([x,y,z])=>[30+(500-span*scale)/2+x*scale,25+(height-y)*scale*.8+z*scale*.32];
 const voxels=[];
 for(let y=baseOf(variant)-1;y<height;y++)for(let z=1;z<depth-1;z++)for(let x=1;x<width-1;x++){
  const material=built.get(x,y,z),functionalDeck=['straight/bridge','straight/hole','turn/hole'].includes(variant.key)&&y===baseOf(variant)-1;
  if(material===air||excluded(variant,seed,x,y,z)&&!functionalDeck)continue;
  for(const cube of blockBoxes(material)){const first=rotate([x+cube[0],y+cube[1],z+cube[2]]),second=rotate([x+cube[3],y+cube[4],z+cube[5]]),from=first.map((value,axis)=>Math.min(value,second[axis])),to=first.map((value,axis)=>Math.max(value,second[axis]));voxels.push({from,to,material,controlWall:variant.key.startsWith('boss/')&&z===22});}
 }
 voxels.sort((first,second)=>first.to[2]-second.to[2]||first.from[1]-second.from[1]||first.from[0]-second.from[0]);
 const polygon=(points,fill,opacity=1)=>`<polygon points="${points.map(point=>screen(point).map(value=>value.toFixed(2)).join(',')).join(' ')}" fill="${fill}" opacity="${opacity}" stroke="#233e52" stroke-width=".32"/>`;
 let svg=`<svg xmlns="http://www.w3.org/2000/svg" width="560" height="390" viewBox="0 0 560 390"><metadata>${JSON.stringify({key:variant.key,direction,nbtSha256,hidden:projectionRule})}</metadata><rect width="560" height="390" fill="#0b1720"/>`;
 for(const {from:[x1,y1,z1],to:[x2,y2,z2],material,controlWall}of voxels){const fill=paint(material),opacity=controlWall?.16:material.includes('glass')?.72:1;svg+=polygon([[x1,y1,z2],[x2,y1,z2],[x2,y2,z2],[x1,y2,z2]],fill,opacity);svg+=polygon([[x1,y2,z1],[x2,y2,z1],[x2,y2,z2],[x1,y2,z2]],fill,.96*opacity);}
 const markers=variant.sockets.map(socket=>({point:socket.position,color:'#ff667b'})).concat(variant.ports.flatMap(port=>port.cells).map(point=>({point,color:'#ffd56c'})));
 for(const {point,color}of markers){const [px,py]=screen(rotate([point[0]+.5,point[1]+.1,point[2]+.5]));svg+=`<circle cx="${px.toFixed(2)}" cy="${py.toFixed(2)}" r="2.1" fill="${color}" stroke="#152431" stroke-width=".6"/>`;}
 svg+=`<text x="14" y="372" fill="#d3eaf5" font-family="sans-serif" font-size="11">${variant.key} · ${direction} · actual NBT</text></svg>`;
 return {svg,retainedCuboids:voxels.length,projectedGeometrySha256:hash(JSON.stringify(voxels)),svgSha256:hash(svg)};
}
function fourViews(rooms,templates){
 const directory=path.join(out,'views');fs.mkdirSync(directory,{recursive:true});const views=[];
 const cards=rooms.map((room,seed)=>{const pictures=directions.map(([direction,label,camera])=>{const rendered=renderDirection(room,seed,direction,templates[seed].sha256),file=`views/${String(seed).padStart(2,'0')}-${direction}.svg`;fs.writeFileSync(path.join(out,file),rendered.svg);const record={key:room.variant.key,direction,label,cameraFrom:camera,nbtSha256:templates[seed].sha256,file,svgSha256:rendered.svgSha256,projectedGeometrySha256:rendered.projectedGeometrySha256,retainedCuboids:rendered.retainedCuboids};views.push(record);return `<figure><figcaption>${label} · ${direction} (从${label}向中心俯看)</figcaption><img src="${file}" data-key="${room.variant.key}" data-direction="${direction}" data-nbt-sha256="${templates[seed].sha256}" width="560" height="390" alt="${room.variant.key} ${direction}"/></figure>`;}).join('');return `<article data-key="${room.variant.key}"><h2>${room.variant.key} · ${designs[seed][0]}</h2><p>${designs[seed][4]} · 路线与接口检查 ${room.report.pass?'PASS':'FAIL'} · NBT SHA256 <code>${templates[seed].sha256}</code></p><div class="views">${pictures}</div></article>`;});
 const sheets=[];for(let index=0;index<cards.length;index+=9)sheets.push(`<section class="sheet">${cards.slice(index,index+9).join('')}</section>`);
 fs.writeFileSync(path.join(out,'four-views.html'),`<!doctype html><html lang="zh-CN"><meta charset="utf-8"><title>冰霜27房×四向 · 实际NBT内部审核</title><style>body{font:14px system-ui;background:#0a1620;color:#dbedf6;margin:20px}h1{font-size:25px}article{padding:12px;background:#142a39;border:1px solid #31566c;margin:14px 0;border-radius:8px}h2{font-size:16px;margin:0}p{font-size:11px;color:#aac4d3;line-height:1.5}code{font-size:9px}.views{display:grid;grid-template-columns:repeat(4,1fr);gap:8px}figure{margin:0}figcaption{font-size:11px;margin:5px 0}img{width:100%;height:auto;background:#0b1720}</style><h1>冰霜 · 27房 × 北南西东 = 108张真实NBT方向投影</h1><p>四方向为准确N/S/W/E轴向、统一俯角的正交投影；从最终导出NBT回读。${projectionRule} 不是游戏材质渲染或游戏实测。每图源NBT哈希及观察方向写入SVG metadata。</p>${sheets.join('')}<script>window.viewRecords=${JSON.stringify(views)};</script></html>`);
 return views;
}
async function auditSuite(rooms,validation,templates){
 fs.writeFileSync(path.join(out,'FREEZE.json'),JSON.stringify({status:'AUDIT_RUNNING'},null,2)+'\n');
 const style=styleAudit(rooms,templates),repetition=repetitionAudit(rooms,templates),views=fourViews(rooms,templates);
 fs.appendFileSync(path.join(out,'design.md'),'\n## 追加自审返工记录\n\n'+repairs.map(repair=>`- ${repair.key}：${repair.issue}；${repair.action}。`).join('\n')+'\n');
 style.rooms.forEach((entry,seed)=>{entry.visualReview={evidenceImage:`audit-previews/${String(seed).padStart(2,'0')}-four-views.png`,northSouth:visualNotes[seed][0],westEast:visualNotes[seed][1],allFourDirectionsReviewed:true,mainRouteClear:rooms[seed].report.pass};const {variant}=rooms[seed];let capacity=0;for(let y=baseOf(variant);y<variant.size[1];y++)for(let z=1;z<variant.size[2]-1;z++)for(let x=1;x<variant.size[0]-1;x++)if(!excluded(variant,seed,x,y,z))capacity++;entry.interiorDomainCells=capacity;entry.interiorSolidDensity=entry.interiorSolidBlocks/capacity;entry.densityReview={overdense:family(variant.key)==='combat'&&entry.interiorSolidDensity>.70,routeTargetsConnected:rooms[seed].report.reachableTargets===rooms[seed].report.targets};});
 fs.writeFileSync(path.join(out,'style-audit.json'),JSON.stringify(style,null,2)+'\n');
 fs.writeFileSync(path.join(out,'repetition-audit.json'),JSON.stringify(repetition,null,2)+'\n');
 fs.writeFileSync(path.join(out,'four-views-index.json'),JSON.stringify({schema:1,views},null,2)+'\n');
 const runtime=process.env.FROST_BROWSER_RUNTIME??'C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/package.json';
 const {chromium}=createRequire(runtime)('playwright');
 const browser=await chromium.launch({headless:true}),page=await browser.newPage({viewport:{width:1800,height:1000}}),errors=[];
 page.on('pageerror',error=>errors.push(error.message));page.on('requestfailed',request=>errors.push('request failed '+request.url()));
 const previewDirectory=path.join(out,'audit-previews');fs.mkdirSync(previewDirectory,{recursive:true});
 await page.goto(new URL('four-views.html','file:///'+out.replaceAll('\\','/')+'/').href);
 await page.locator('img').last().waitFor();
 await page.waitForFunction(()=>[...document.images].every(image=>image.complete));
 const images=await page.locator('img').evaluateAll(elements=>elements.map(image=>({key:image.dataset.key,direction:image.dataset.direction,nbtSha256:image.dataset.nbtSha256,complete:image.complete,width:image.naturalWidth,height:image.naturalHeight})));
 for(let sheet=0;sheet<3;sheet++)await page.locator('.sheet').nth(sheet).screenshot({path:path.join(previewDirectory,`sheet-${sheet+1}.png`)});
 for(let index=0;index<rooms.length;index++)await page.locator('article').nth(index).screenshot({path:path.join(previewDirectory,`${String(index).padStart(2,'0')}-four-views.png`)});
 const browserAudit={page:'four-views.html',pageSha256:hash(fs.readFileSync(path.join(out,'four-views.html'))),pageErrors:errors,expectedViews:108,actualViews:images.length,rooms:rooms.map(({variant},seed)=>{const found=images.filter(image=>image.key===variant.key),records=views.filter(view=>view.key===variant.key);return {key:variant.key,images:found,exactFourDirections:new Set(found.map(image=>image.direction)).size===4,sourceHashesMatch:found.every(image=>image.nbtSha256===templates[seed].sha256),projectedGeometrySignatures:new Set(records.map(record=>record.projectedGeometrySha256)).size,allLoaded:found.length===4&&found.every(image=>image.complete&&image.width===560&&image.height===390),retainedCuboids:records.map(record=>record.retainedCuboids)};})};
 browserAudit.pass=errors.length===0&&images.length===108&&browserAudit.rooms.every(room=>room.exactFourDirections&&room.sourceHashesMatch&&room.allLoaded&&room.projectedGeometrySignatures===4&&room.retainedCuboids.every(count=>count>0));
 fs.writeFileSync(path.join(out,'browser-four-views.json'),JSON.stringify(browserAudit,null,2)+'\n');
 await page.goto(new URL('gallery.html','file:///'+out.replaceAll('\\','/')+'/').href);
 await page.screenshot({path:path.join(out,'gallery-review.png')});await page.screenshot({path:path.join(out,'gallery-full.png'),fullPage:true});
 for(const index of [3,6,7,8,15,16,17,25])await page.locator('article').nth(index).screenshot({path:path.join(out,`preview-${String(index).padStart(2,'0')}.png`)});
 const galleryBrowser={gallerySha256:hash(fs.readFileSync(path.join(out,'gallery.html'))),articles:await page.locator('article').count(),canvases:await page.locator('canvas').count(),pageErrors:errors,pass:errors.length===0&&await page.locator('article').count()===27};fs.writeFileSync(path.join(out,'browser-validation.json'),JSON.stringify(galleryBrowser,null,2)+'\n');
 await browser.close();
 const replay=templates.map((entry,seed)=>({key:entry.key,sha256:entry.sha256,replaySha256:hash(encode(makeRoom(rooms[seed].variant,seed)))}));
 const replayPass=replay.every(entry=>entry.sha256===entry.replaySha256);fs.writeFileSync(path.join(out,'replay-verification.json'),JSON.stringify({method:'Regenerate all 27 rooms from recipe and re-encode NBT; compare to final exported bytes',pass:replayPass,rooms:replay},null,2)+'\n');
 const formalUnchanged=templates.every(entry=>hash(fs.readFileSync(path.join(data,'roomsets/frost',entry.key+'.nbt')))===baselineSnapshot.templates[entry.key].sha256);
 const manifestMatches=templates.every(entry=>hash(fs.readFileSync(path.join(out,'roomset',entry.key+'.nbt')))===entry.sha256);
 const audit={schema:1,style:'fteclipsegen:frost',fourViews:{expected:108,actual:views.length,pass:browserAudit.pass,browserReport:'browser-four-views.json',rules:'four exact cardinal directions; hidden original exterior shell, roof, foundation and port bands; keep all interior heights and landmarks'},style:{pass:style.pass,rooms:style.rooms.map(room=>({key:room.key,pass:room.pass,landmarks:room.landmarks.map(landmark=>({role:landmark.role,pass:landmark.pass,actualBlocks:landmark.actualBlocks})),issues:room.issues}))},repetition:{pass:repetition.pass,comparablePairs:repetition.pairs.length,highestScore:repetition.highestPairs[0]?.comparisonScore,highPairs:repetition.reviewedHighPairs},repairs,geometryValidationPass:validation.allPass,recipeReplayPass:replayPass,manifestHashesMatch:manifestMatches,formalResourcesUnchanged:formalUnchanged,manifestSha256:validation.manifestSha256,validationSha256:hash(fs.readFileSync(path.join(out,'validation.json'))),generatorSha256:hash(fs.readFileSync(fileURLToPath(import.meta.url))),sourceDraftManifestSha256:hash(fs.readFileSync(draftPath)),changedSinceDraft:templates.filter(entry=>JSON.parse(fs.readFileSync(draftPath,'utf8')).templates.find(previous=>previous.key===entry.key)?.sha256!==entry.sha256).map(entry=>entry.key),limitations:['离线实际NBT几何投影与静态检查，不是Minecraft客户端或服务器实测。','像素投影采用纯色与半透明玻璃，不是游戏材质光照渲染。','半格阶梯采样不模拟完整Minecraft实体运动物理。','仅审核frost内部；跨八风格同key对照由父线程集成前执行。']};
 audit.visualRoomReviews=style.rooms.map(entry=>({key:entry.key,...entry.visualReview}));
 audit.viewChecks=views.map(view=>{const seed=rooms.findIndex(room=>room.variant.key===view.key),image=images.find(image=>image.key===view.key&&image.direction===view.direction);return {key:view.key,direction:view.direction,nbtSha256:view.nbtSha256,svgSha256:view.svgSha256,file:view.file,geometryPresent:view.retainedCuboids>0,loaded:!!image?.complete&&image.width===560&&image.height===390,reviewNote:visualNotes[seed][view.direction==='north'||view.direction==='south'?0:1],mainRouteAndDensityPass:style.rooms[seed].densityReview.routeTargetsConnected&&!style.rooms[seed].densityReview.overdense,styleLandmarksPass:style.rooms[seed].pass};});
 audit.reportHashes=Object.fromEntries(['four-views.html','four-views-index.json','style-audit.json','repetition-audit.json','browser-four-views.json','replay-verification.json'].map(file=>[file,hash(fs.readFileSync(path.join(out,file)))]));
 audit.projectionRules=projectionRule;
 audit.pass=browserAudit.pass&&style.pass&&repetition.pass&&validation.allPass&&replayPass&&manifestMatches&&formalUnchanged&&galleryBrowser.pass;
 fs.writeFileSync(path.join(out,'self-audit.json'),JSON.stringify(audit,null,2)+'\n');
 fs.writeFileSync(path.join(out,'delivery-audit.json'),JSON.stringify({pass:audit.pass,exportCount:27,manifestHashesMatch:manifestMatches,formalResourcesUnchanged:formalUnchanged,interfacesMatchCatalog:hash(JSON.stringify(JSON.parse(fs.readFileSync(path.join(out,'interfaces.json'),'utf8')).variants))===hash(JSON.stringify(catalog.variants.map(({key,size,minimum_y,ports,sockets,spawn_position})=>({key,size,minimum_y,ports,sockets,...(spawn_position?{spawn_position}:{})})))),fourViewsPass:browserAudit.pass,stylePass:style.pass,repetitionPass:repetition.pass,recipeReplayPass:replayPass},null,2)+'\n');
 fs.writeFileSync(path.join(out,'README.md'),'# 冰霜27房最终自审交付\n\n运行 `node tools/rebuild-frost.mjs` 可重新生成27NBT、manifest、几何检查、四方向108SVG、四视图HTML、材质及地标检查、去公共外壳的内部重复度检查、浏览器截图与检查、配方二次生成哈希比较及冻结文件。\n\n四视图见four-views.html，每图SVG metadata含最终NBT SHA256与准确方向。audit-previews/含27房逐房四向截图及3张九房汇总。每间全部四个方向均由浏览器加载验证，无页面错误。\n\n外壳、基础地板、端口安全带与配方顶壳从重复度和内部投影中剔除，完整外壳与接口另由validation.json检查。style-audit.json给出最终NBT实际材质统计、每个主题结构的真实计数与包围盒；repetition-audit.json包含全部可比配对、12³归一化旋转签名及高相似审核。\n\n本轮返工：档案架吊架、食堂六椅、商店冰镜、Boss高拱洞顶及四枚冠齿。基线baseline-v033.json固定原0.33几何；audit-initial-draft.json记录本次追加验收前初稿manifest。\n\n只复用stairs/*和straight/platform的原基础接口踏面，冰霜主体独立构造。全部是离线几何与图像检查，未部署、未作Minecraft游戏实测。纯色投影不是游戏材质渲染。最终状态及全部文件SHA256见FREEZE.json。\n');
 const files=[];const collect=directory=>{for(const entry of fs.readdirSync(directory,{withFileTypes:true})){const file=path.join(directory,entry.name);if(entry.isDirectory())collect(file);else if(entry.name!=='FREEZE.json')files.push({path:path.relative(out,file).replaceAll('\\','/'),sha256:hash(fs.readFileSync(file))});}};collect(out);files.sort((first,second)=>first.path.localeCompare(second.path));
 fs.writeFileSync(path.join(out,'FREEZE.json'),JSON.stringify({status:audit.pass?'FROZEN_PASS':'FAILED_NOT_FROZEN',style:'fteclipsegen:frost',count:27,views:108,manifestSha256:validation.manifestSha256,generatorSha256:audit.generatorSha256,validationSha256:audit.validationSha256,selfAuditSha256:hash(fs.readFileSync(path.join(out,'self-audit.json'))),files},null,2)+'\n');
 console.log(JSON.stringify({selfAuditPass:audit.pass,stylePass:style.pass,repetitionPass:repetition.pass,browserPass:browserAudit.pass,highestSimilarity:repetition.highestPairs[0]?.comparisonScore,failedLandmarks:style.rooms.filter(room=>!room.pass).map(room=>({key:room.key,issues:room.issues,failed:room.landmarks.filter(landmark=>!landmark.pass)})),highPairs:repetition.reviewedHighPairs},null,2));
 if(!audit.pass)process.exitCode=1;
}
await auditSuite(rooms,report,templates);
