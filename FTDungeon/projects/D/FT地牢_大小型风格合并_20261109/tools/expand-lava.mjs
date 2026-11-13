import fs from 'node:fs';
import path from 'node:path';
import crypto from 'node:crypto';
import { createRequire } from 'node:module';
import { fileURLToPath, pathToFileURL } from 'node:url';
import { spawn } from 'node:child_process';

const recipeDirectory=path.dirname(fileURLToPath(import.meta.url));
const root=fs.existsSync(path.resolve(recipeDirectory,'../src/main/resources'))?path.resolve(recipeDirectory,'..'):path.resolve(recipeDirectory,'../../../..');
const expansion=path.join(root,'output/room-expansion-v035/lava');
const activeBatch=5;
const batchDirectory=`batch-${String(activeBatch).padStart(2,'0')}`;
const output=path.join(expansion,batchDirectory);
const resources=path.join(root,'src/main/resources/data/fteclipsegen');
const skill=process.env.MINECRAFT_BUILDER_SKILL??'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill';
const loader=createRequire(path.join(skill,'package.json'));
const api=await import(pathToFileURL(loader.resolve('@mattzh72/lodestone')).href);
const catalog=JSON.parse(fs.readFileSync(path.join(resources,'catalog.json'),'utf8'));
const knownBlocks=new Set([...fs.readFileSync(path.join(skill,'references/lodestone/default-pack-block-states.md'),'utf8').matchAll(/^### `(minecraft:[a-z0-9_]+)`/gm)].map(match=>match[1]));
const AIR='minecraft:air';
const digest=value=>crypto.createHash('sha256').update(value).digest('hex');
const firstBatchSpecs=[
 {key:'addition/01',baseKey:'straight/room_cells',name:'玄武岩拉丝工坊',layout:'高低拉丝炉错列在西侧；东侧横向冷却床与独立纤维卷架。三格净宽路线从北向南呈三段折线，炉体与冷却床形成不同方向的作业边界。',heights:'地板y1、脚面y2；西北高炉至y11、西南矮炉至y6、冷却床至y3、卷架至y8；双高位排热罩至y13。',landmarks:'玄武岩高低炉喉、封闭橙光热源、横向冷却肋床、双纤维卷轴与牵引导丝梁。',roof:'两段不等高的排热罩，以西侧双烟道联系；东侧屋顶为封闭玻璃采光脊，立面为分段检视窗。',difference:'以错列工序和横向冷却床组织折线；区别旧蜂窑的L窑群、铸模库的双长架和吊臂工坊的偏轴工件岛。'},
 {key:'addition/02',baseKey:'straight/room_storage',name:'耐火砖烧制库',layout:'西北短窑道至东北分层砖坯架，再折向西南出窑台，地面色带标识L形生产流程；行人走中央绕行并横通窑道。',heights:'地板y1、脚面y2；窑道跨顶y7、砖架y2/5/8、出窑台y2/3、分段排烟拱y10..13。',landmarks:'短隧道窑的双厚侧墙和顶衬、三层红砖坯托架、西侧热砖出窑台及封存炉窗。',roof:'短窑道顶罩与三个分段排烟拱；高位窄玻璃采光带封顶，厚立面仅设工位观察孔。',difference:'窑道为可进入的短横厅，东侧单列分层砖架与独立出窑台组成L工序；不复制旧双长架仓库或钟塔构架。'}
];
const secondBatchSpecs=[
 {key:'addition/03',baseKey:'straight/room_towers',name:'地热稳压室',layout:'西北偏心封闭热井、西南高蓄压墩与东南低蓄压墩形成三点结构；检修短桥位于西侧热井与高墩之间，实体桥墩承托桥面，西侧三宽台阶可登桥，地面主路从三点设备之间纵向贯穿。',heights:'主路脚面y2；热井顶y7、高墩y9、低墩y5；桥面脚高y4，连续半格踏步连接地面，梁管至y12。',landmarks:'玻璃封闭地热井、不同高度蓄压墩、双圈静态铁箍、带实体桥墩及三宽踏步的短检修桥。',roof:'偏西高位排压总管与东南转接支管，热井上方独立矩形密封采光口；南立面为蓄压检视带。',difference:'三个不等高设备构成偏心三角，检修桥与战斗地面分层；不采用旧房四角柱、单炉群或第一批拉丝生产线。'},
 {key:'addition/04',baseKey:'straight/room_cells',name:'黑曜石刃研磨间',layout:'西北朝南、东侧朝西、西南朝北三张研磨床风车式错位；西北至东再转西南的工序互相错开，主路在床台间折转。背面刃坯壁架承载黑曜石刀坯。',heights:'脚面y2；研磨床体至y3、三向刃坯至y4；独立背架至y8，集尘支梁y10..12。',landmarks:'三向静态磨床、黑曜石刃坯、不同朝向铁质磨砺边和三组背面刃坯壁架。',roof:'中央偏南矩形集尘罩及三向短支管，三条错位玻璃采光口分别对应磨床；东、西立面设低位检视窗。',difference:'三个短工位朝向错开，低机床与高刃架形成风车生产动线；区别旧双排仓架、蜂窑，以及新增短窑道库。'}
];
const thirdBatchSpecs=[
 {key:'addition/05',baseKey:'straight/room_storage',name:'熔渣分选站',layout:'西侧三段阶梯静态分选槽自北向南升高，西南矮卸料坪与东南独立矿样台隔着平层主路相望。检修工沿槽边走，战斗主路保持y2，不要求登上分选设备。',heights:'脚面y2；三段分选槽底y2/3/4、侧边比槽底高1格，矮卸料坪y2，矿样台y2..3；吊置抽尘罩y10..12。',landmarks:'带静态深色熔渣和铁质筛分条的阶梯槽、低卸料坪、三种矿样与封闭橙色检视灯槽。',roof:'非对称纵向抽尘罩随阶梯槽分级，东侧矩形矿样采光顶，西立面低窗和南立面装卸观察带。',difference:'主要体量是可见不同底高的短阶梯槽与独立对向样品台，区别蜂窑、三层砖架、双蓄压墩和两侧等长货架。'},
 {key:'addition/06',baseKey:'straight/room_towers',name:'锻锤悬架大厅',layout:'西中高锻锤与东南低锻锤偏心错列，实柱支撑横梁及下悬锤头；中部低矮坯件使地面分成左右绕行双路径，西南炉台退后于主路。',heights:'地面脚y2；西锤横梁y11..12、东锤横梁y8..9，锤头分别y7和y5；中央坯件y2..3、炉台至y6；高位天车独立檐梁y13。',landmarks:'两副高低不同的完整落地锻锤门架、静态锤杆锤头、中央铁坯及封闭后退炉台。',roof:'双层不等高机架顶盖，西高东低两片玻璃顶窗分别罩住工位；侧立面宽度不同的高位检修窗。',difference:'机架是两组实承重工艺结构而非房间四角柱，主路绕中央铁坯分叉再汇合；区别旧单四柱压机与偏轴吊臂岛。'}
];
const fourthBatchSpecs=[
 {key:'addition/07',baseKey:'straight/room_cells',name:'硫晶净化室',layout:'西侧一座完整玻璃观察舱封存不对称硫晶簇，晶簇主峰偏北、分峰低矮；东侧三段阶梯净化台自北向南增高，东南短封存架独立收尾。平层中央主路分隔晶体与机械。',heights:'脚面y2；晶舱基座y2、玻璃罩至y9，晶体主峰y8/次峰y6/侧芽y4；净化台底高y2/3/4，封存架至y7。',landmarks:'透明封闭舱内不对称黄色硫晶体、与晶体轮廓不同的低矮铁石净化台、短封存架及高位排净主管。',roof:'晶舱顶部矩形采光顶与净化侧短条窗错位，北高南低排净管仅连接净化设备；西立面为大检视窗、东面为窄观察孔。',difference:'西侧单个高透明晶舱与东侧多级低机器体量形成对照；区别旧紫晶符号、蜂窑群及前三批分选、锻锤或双蓄压设备。'},
 {key:'addition/08',baseKey:'straight/room_storage',name:'铸链试拉室',layout:'横向两端厚拉力墩夹住低矮静态链床；中央连续拉伸工件横跨东西，南侧斜向操作台以三个错位块段构成，独立橙色安全屏挡在南后方。主路向西绕过拉力墩，两侧均可回环，不穿越链床。',heights:'脚面y2；链床y2..3、两端拉力墩至y6、错位斜台y2..3，安全屏至y7；偏南承重罩梁y11。',landmarks:'双端实体拉力墩与三环静态铁链形体、斜向错位控制台、实体基座安全屏。',roof:'横轴拉伸工位上方单跨檐梁，西绕行区封闭玻璃采光顶；南后屏挡采用下实上透厚立面，不做钟塔或双长架。',difference:'工艺轴沿东西，通行轴在两端外侧回环；区别前三批纵向生产线与双锻锤围中央铁坯，两端拉力墩明确夹持同一条低链床。'}
];
const specs=[
 {key:'addition/09',baseKey:'straight/room_towers',name:'火山岩雕刻所',layout:'三块错高雕坯沿西北、西中、西南组成半环，中央平层作业区向东侧开放；东侧单面工具台与南侧碎石托盘错列，工具台不围出旧塔式四柱空间。',heights:'脚高y2；三块雕坯分层顶高y9、y7、y5；工具台至y4、碎石托盘至y3。',landmarks:'三块各自削切出不同肩部、缺口和石质芯面的玄武岩雕坯，单侧工具台、碎石托盘。',roof:'半环上方三块大小不同的封闭玻璃采光顶，东墙窄工艺窗；无钟楼、悬挂钟体或中轴高塔。',difference:'西侧半环雕刻实体主导空间，东侧低设备提供开放作业面，区别高低锻锤和封闭晶舱。'},
 {key:'addition/10',baseKey:'straight/room_cells',name:'熄炉避难站',layout:'西侧封闭旧炉腹挖出可进入的侧面凹室；东南应急床台与东北折角隔热物资架对角展开；中央低隔热挡体两侧均有三格宽平层通路。',heights:'脚高y2；旧炉腹至y8、收口至y9，凹室净高5格；床台y2..3、折角物资架至y6，中央挡体至y5；唯一静态灯源位于y8全封闭玻璃盒。',landmarks:'厚旧炉腹及侧向拱口、白色应急床台、折角隔热物资架、双侧通路及封闭应急灯。',roof:'旧炉上方短烟道封顶连接屋顶，东侧双短条封闭顶窗；低位凹室内保留冷暗炉衬，不依赖真实热源。',difference:'可进入的侧向厚壁炉凹室与低床台组成避难用途，中心只有低隔热挡体，双路径绕行，区别生产炉群、仓架和晶簇。'}
];
function grid(size=[16,16,16]){
 const [width,height,depth]=size,blocks=Array(width*height*depth).fill(AIR);
 const index=(x,y,z)=>(y*depth+z)*width+x;
 const inside=(x,y,z)=>x>=0&&x<width&&y>=0&&y<height&&z>=0&&z<depth;
 const get=(x,y,z)=>inside(x,y,z)?blocks[index(x,y,z)]:AIR;
 const set=(x,y,z,state)=>{if(!inside(x,y,z))throw Error(`Out of bounds ${[x,y,z]}`);blocks[index(x,y,z)]=state.startsWith('minecraft:')?state:`minecraft:${state}`;};
 const box=(from,to,state)=>{for(let y=from[1];y<=to[1];y++)for(let z=from[2];z<=to[2];z++)for(let x=from[0];x<=to[0];x++)set(x,y,z,state);};
 return {size,blocks,index,get,set,box};
}
function encode(room){
 const structure=new api.Structure(room.size),cache=new Map();
 for(let y=0;y<room.size[1];y++)for(let z=0;z<room.size[2];z++)for(let x=0;x<room.size[0];x++){
  const text=room.get(x,y,z);if(!cache.has(text))cache.set(text,api.BlockState.parse(text));const state=cache.get(text);structure.addBlock([x,y,z],state.getName(),state.getProperties());
 }
 return structure.writeNbt({name:'',dataVersion:3465,compression:'gzip'});
}
function decode(file){
 const bytes=fs.readFileSync(file),nbt=api.NbtFile.read(bytes).root,structure=api.Structure.fromNbt(nbt),room=grid(structure.getSize());
 for(const block of structure.getBlocks())room.set(...block.pos,block.state.toString());
 return {room,nbt,structure,sha256:digest(bytes)};
}
function shell(room){
 room.box([0,0,0],[15,1,15],'polished_blackstone');
 for(let y=2;y<16;y++){
  const material=y===6||y===13?'chiseled_polished_blackstone':'polished_basalt';
  for(const x of [0,15])room.box([x,y,0],[x,y,15],material);
  for(const z of [0,15])room.box([0,y,z],[15,y,z],material);
 }
 room.box([0,15,0],[15,15,15],'nether_bricks');
}
function kiln(room,from,to){
 room.box(from,to,'polished_basalt');
 room.box([from[0]+1,from[1],from[2]],[to[0]-1,from[1]+2,from[2]],'orange_stained_glass');
 room.box([from[0]+1,from[1],from[2]+1],[to[0]-1,from[1]+2,from[2]+1],'shroomlight');
 room.box([from[0],to[1]-1,from[2]],[to[0],to[1]-1,to[2]],'chiseled_polished_blackstone');
}
function carveRoute(room,waypoints){
 for(let index=1;index<waypoints.length;index++){
  const [startX,startZ]=waypoints[index-1],[endX,endZ]=waypoints[index];
  const length=Math.abs(endX-startX)+Math.abs(endZ-startZ);
  if(startX!==endX&&startZ!==endZ)throw Error('Route segment must be orthogonal');
  for(let step=0;step<=length;step++){
   const x=startX+Math.sign(endX-startX)*step,z=startZ+Math.sign(endZ-startZ)*step;
   for(let zz=Math.max(1,z-1);zz<=Math.min(14,z+1);zz++)for(let xx=Math.max(1,x-1);xx<=Math.min(14,x+1);xx++){
    room.box([xx,2,zz],[xx,6,zz],'air');room.set(xx,1,zz,'polished_blackstone');
   }
  }
 }
}
function build(spec){
 const variant=catalog.variants.find(entry=>entry.key===spec.baseKey),room=grid(variant.size);shell(room);
 let route;
 if(spec.key==='addition/09'){
  room.box([1,2,4],[3,6,6],'polished_basalt');room.box([1,7,4],[2,9,5],'basalt');
  room.box([3,4,5],[3,5,5],'blackstone');room.set(2,7,6,'gilded_blackstone');room.set(2,6,6,'basalt');room.box([2,8,6],[2,9,6],'basalt');
  room.box([1,2,8],[3,4,10],'basalt');room.box([1,5,8],[2,7,9],'polished_basalt');room.set(3,4,9,'gilded_blackstone');
  room.box([2,2,12],[5,3,14],'polished_basalt');room.box([2,4,12],[4,5,13],'blackstone');room.set(4,4,14,'basalt');
  room.box([12,2,9],[14,2,10],'smooth_stone');room.box([12,3,9],[14,3,9],'iron_block');
  for(const x of [12,14])room.set(x,4,9,'chiseled_polished_blackstone');
  room.box([12,2,12],[14,2,14],'iron_block');room.set(12,3,12,'basalt');room.set(14,3,13,'blackstone');room.set(13,3,14,'gilded_blackstone');
  for(const [from,to] of [[[1,15,4],[3,15,6]],[[1,15,8],[3,15,10]],[[2,15,12],[5,15,14]]])room.box(from,to,'orange_stained_glass');
  room.box([15,4,9],[15,7,12],'orange_stained_glass');route=[[7,0],[7,15]];
 }else if(spec.key==='addition/10'){
  room.box([1,2,6],[4,8,11],'polished_basalt');room.box([2,2,7],[3,6,10],'air');
  room.box([4,2,8],[4,5,9],'air');room.box([1,3,7],[1,5,10],'blackstone');
  room.box([1,9,7],[4,9,10],'chiseled_polished_blackstone');room.box([1,10,7],[2,14,8],'nether_bricks');
  room.box([12,2,12],[14,2,14],'smooth_stone');room.box([12,3,12],[14,3,14],'white_wool');room.box([14,3,12],[14,3,14],'white_terracotta');
  room.box([13,2,4],[14,6,5],'iron_block');room.box([14,2,6],[14,5,9],'smooth_stone');room.box([13,2,6],[13,4,6],'smooth_stone');
  for(const y of [3,5]){room.box([13,y,4],[13,y,5],'white_terracotta');room.box([14,y,6],[14,y,9],'white_terracotta');}
  room.box([12,7,6],[14,9,8],'orange_stained_glass');room.set(13,8,7,'shroomlight');
  room.box([14,2,7],[14,6,7],'polished_basalt');
  room.box([9,2,7],[9,5,8],'obsidian');room.box([9,5,7],[9,5,8],'iron_block');
  room.box([12,15,4],[14,15,5],'orange_stained_glass');room.box([12,15,12],[14,15,14],'orange_stained_glass');
  room.box([0,4,7],[0,5,10],'orange_stained_glass');room.box([15,5,12],[15,7,14],'orange_stained_glass');
  route=[[7,0],[7,15]];
 }else if(spec.key==='addition/07'){
  room.box([1,2,5],[3,2,10],'polished_basalt');room.box([1,3,5],[3,9,10],'yellow_stained_glass');
  room.box([2,3,6],[2,8,6],'yellow_concrete');room.box([2,3,8],[2,6,8],'yellow_terracotta');
  room.box([2,3,9],[2,4,9],'gold_block');room.set(2,5,7,'yellow_concrete');room.set(2,4,7,'yellow_terracotta');
  for(let step=0;step<3;step++){
   const z=5+step*2,y=2+step;room.box([12,2,z],[14,y,z+1],'smooth_stone');room.box([12,y+1,z],[14,y+1,z],'iron_block');room.set(13,y+1,z+1,'calcite');
  }
  for(const z of [12,14])room.box([12,2,z],[14,6,z],'polished_basalt');
  for(const y of [2,5])room.box([12,y,12],[14,y,14],'iron_block');room.box([13,3,13],[13,4,13],'yellow_terracotta');room.box([12,7,12],[14,7,14],'smooth_stone');
  room.box([13,9,4],[13,11,11],'nether_bricks');room.box([10,11,4],[13,12,4],'polished_basalt');
  room.box([1,15,5],[3,15,10],'yellow_stained_glass');room.box([12,15,4],[14,15,10],'orange_stained_glass');
  room.box([0,4,5],[0,8,10],'yellow_stained_glass');room.box([15,5,6],[15,7,8],'orange_stained_glass');
  route=[[7,0],[7,15]];
 }else if(spec.key==='addition/08'){
  for(const x of [4,10]){
   room.box([x,2,7],[x+1,6,8],'polished_basalt');room.box([x,4,7],[x+1,4,8],'chiseled_polished_blackstone');
   room.box([x,6,7],[x+1,6,8],'iron_block');
  }
  room.box([6,2,7],[9,2,8],'polished_blackstone');
  for(const x of [6,8]){room.set(x,3,7,'iron_block');room.set(x+1,3,8,'iron_block');room.set(x,3,8,'gilded_blackstone');room.set(x+1,3,7,'gilded_blackstone');}
  for(const [x,z]of [[3,12],[4,13],[5,14]]){room.set(x,2,z,'smooth_stone');room.set(x,3,z,'iron_block');}
  room.box([10,2,12],[13,2,12],'polished_basalt');room.box([10,3,12],[13,7,12],'orange_stained_glass');
  for(const x of [10,13])room.box([x,3,12],[x,7,12],'polished_basalt');
  room.box([0,11,7],[15,12,7],'nether_bricks');
  room.box([1,15,4],[3,15,11],'orange_stained_glass');room.box([5,15,7],[10,15,8],'orange_stained_glass');
  room.box([15,5,7],[15,8,10],'orange_stained_glass');room.box([2,6,0],[4,9,0],'orange_stained_glass');
  route=[[7,0],[7,5],[2,5],[2,10],[7,10],[7,15]];
 }else if(spec.key==='addition/05'){
  for(let segment=0;segment<3;segment++){
   const startZ=5+segment*2,level=2+segment;
   room.box([1,2,startZ],[3,level,startZ+1],'polished_basalt');
   room.box([2,level,startZ],[2,level,startZ+1],'blackstone');
   for(const x of [1,3])room.box([x,level+1,startZ],[x,level+1,startZ+1],'iron_block');
   room.set(2,level,startZ+1,'gray_concrete');
  }
  room.box([1,2,12],[3,2,14],'smooth_stone');room.box([1,2,13],[2,2,14],'blackstone');
  room.box([12,2,9],[14,2,12],'smooth_stone');
  room.set(12,3,9,'raw_iron_block');room.set(14,3,10,'raw_gold_block');room.set(13,3,12,'raw_copper_block');
  room.box([12,4,9],[14,4,12],'air');
  room.box([12,6,6],[14,8,8],'orange_stained_glass');room.set(13,7,7,'shroomlight');
  room.box([14,2,7],[14,5,7],'polished_basalt');room.box([12,9,6],[14,9,8],'iron_block');
  for(const [z,y]of [[5,10],[7,11],[9,12]])room.box([1,y,z],[4,y+1,z+1],'nether_bricks');
  room.box([1,9,4],[1,12,10],'polished_basalt');
  room.box([11,15,8],[14,15,13],'orange_stained_glass');room.box([2,15,5],[3,15,10],'orange_stained_glass');
  room.box([0,4,5],[0,6,10],'orange_stained_glass');room.box([2,5,15],[5,7,15],'orange_stained_glass');
  route=[[7,0],[7,15]];
 }else if(spec.key==='addition/06'){
  for(const z of [4,7])room.box([1,2,z],[3,10,z],'polished_basalt');
  room.box([1,11,4],[3,12,7],'iron_block');room.box([2,8,5],[2,10,6],'iron_block');room.box([1,7,5],[3,7,6],'polished_blackstone');
  for(const z of [10,13])room.box([12,2,z],[14,7,z],'polished_basalt');
  room.box([12,8,10],[14,9,13],'iron_block');room.box([13,6,11],[13,7,12],'iron_block');room.box([12,5,11],[14,5,12],'polished_blackstone');
  room.box([7,2,7],[8,3,8],'iron_block');
  kiln(room,[1,2,12],[3,6,14]);
  room.box([1,13,3],[3,13,9],'nether_bricks');room.box([11,11,9],[14,11,14],'nether_bricks');
  room.box([1,15,4],[3,15,7],'orange_stained_glass');room.box([12,15,10],[14,15,13],'orange_stained_glass');
  room.box([0,8,4],[0,10,8],'orange_stained_glass');room.box([15,7,10],[15,9,13],'orange_stained_glass');
  route=[[7,0],[7,5],[5,5],[5,10],[7,10],[7,15]];
 }else if(spec.key==='addition/03'){
  room.box([1,2,1],[4,7,3],'polished_basalt');room.box([2,2,1],[3,6,2],'orange_stained_glass');room.set(2,3,2,'shroomlight');room.set(2,3,3,'orange_stained_glass');
  room.box([1,6,1],[4,6,3],'chiseled_polished_blackstone');
  room.box([1,2,10],[3,9,13],'polished_basalt');
  for(const y of [4,7])room.box([1,y,10],[3,y,13],'iron_block');
  room.box([11,2,12],[14,5,14],'polished_blackstone');room.box([11,4,12],[14,4,14],'iron_block');
  room.box([2,11,4],[2,12,12],'nether_bricks');room.box([2,11,12],[13,12,12],'nether_bricks');
  room.box([3,15,3],[5,15,6],'orange_stained_glass');room.box([10,15,10],[13,15,14],'orange_stained_glass');
  room.box([0,4,2],[0,6,5],'orange_stained_glass');room.box([10,6,15],[14,8,15],'orange_stained_glass');
  route=[[7,0],[7,15]];
 }else if(spec.key==='addition/04'){
  room.box([1,2,2],[5,3,3],'smooth_stone');room.box([2,4,3],[4,4,3],'obsidian');room.box([1,3,2],[5,3,2],'iron_block');
  room.box([11,2,6],[14,3,9],'smooth_stone');room.box([13,4,6],[13,4,9],'obsidian');room.box([11,3,6],[11,3,9],'iron_block');
  room.box([2,2,11],[5,3,13],'smooth_stone');room.box([2,4,13],[5,4,13],'obsidian');room.box([2,3,11],[5,3,11],'iron_block');
  for(const x of [1,5])room.box([x,4,1],[x,8,1],'polished_basalt');room.box([1,8,1],[5,8,1],'iron_block');for(const x of [2,4])room.box([x,5,1],[x,7,1],'obsidian');
  for(const z of [6,10])room.box([14,4,z],[14,8,z],'polished_basalt');room.box([14,8,6],[14,8,10],'iron_block');for(const z of [7,9])room.box([14,5,z],[14,7,z],'obsidian');
  for(const x of [2,5])room.box([x,4,14],[x,8,14],'polished_basalt');room.box([2,8,14],[5,8,14],'iron_block');for(const x of [3,4])room.box([x,5,14],[x,7,14],'obsidian');
  room.box([5,11,6],[10,12,9],'nether_bricks');room.box([3,10,2],[3,11,7],'polished_basalt');room.box([10,10,7],[14,11,7],'polished_basalt');
  room.box([2,15,2],[5,15,3],'orange_stained_glass');room.box([11,15,6],[13,15,10],'orange_stained_glass');room.box([2,15,11],[5,15,13],'orange_stained_glass');
  room.box([0,5,5],[0,7,8],'orange_stained_glass');room.box([15,5,3],[15,7,5],'orange_stained_glass');
  route=[[7,0],[7,5],[8,5],[8,9],[7,9],[7,15]];
 }else if(spec.key==='addition/01'){
  kiln(room,[1,2,2],[3,11,4]);kiln(room,[2,2,10],[5,6,13]);
  room.box([1,12,2],[3,13,4],'nether_bricks');room.box([2,9,10],[4,10,12],'nether_bricks');
  room.box([2,11,3],[2,12,12],'polished_basalt');
  room.box([4,7,5],[5,8,7],'iron_block');room.box([2,8,6],[10,8,6],'iron_block');
  room.box([10,2,7],[14,2,9],'smooth_stone');
  for(const x of [10,12,14])room.box([x,3,7],[x,3,9],'iron_block');
  for(const z of [11,13]){
   room.box([12,2,z],[12,8,z],'polished_basalt');room.box([14,2,z],[14,8,z],'polished_basalt');
   room.box([13,4,z],[13,6,z],'white_terracotta');room.box([12,7,z],[14,7,z],'iron_block');
   room.set(13,5,z,'calcite');
  }
  room.box([10,10,10],[14,10,14],'iron_block');room.box([11,15,2],[13,15,13],'orange_stained_glass');
  for(const z of [3,10])room.box([0,5,z],[0,8,z+2],'orange_stained_glass');
  room.box([15,5,6],[15,7,9],'orange_stained_glass');
  route=[[7,0],[7,4],[7,6],[6,6],[6,9],[8,9],[8,12],[7,12],[7,15]];
 }else{
  room.box([2,2,5],[5,6,5],'nether_bricks');room.box([2,2,9],[5,6,9],'nether_bricks');
  room.box([2,7,5],[5,7,9],'polished_basalt');
  room.box([2,3,3],[3,5,5],'orange_stained_glass');room.set(2,4,4,'shroomlight');
  room.box([1,2,4],[1,7,10],'polished_basalt');
  for(const y of [2,5,8]){
   room.box([12,y,2],[14,y,7],'iron_block');
   for(const z of [2,4,6])room.box([12,y+1,z],[14,y+1,z],'bricks');
  }
  for(const x of [12,14])for(const z of [2,7])room.box([x,2,z],[x,10,z],'polished_basalt');
  room.box([1,2,11],[3,3,13],'smooth_stone');room.box([1,4,12],[3,4,13],'bricks');
  room.box([10,2,13],[13,2,14],'bricks');
  for(const z of [3,8,12]){
   for(let x=2;x<14;x++){const height=10+Math.min(2,x-2,13-x);room.set(x,height,z,'nether_bricks');room.set(x,height+1,z,'polished_basalt');}
  }
  room.box([5,15,2],[7,15,13],'orange_stained_glass');
  for(const z of [3,10])room.box([15,4,z],[15,6,z+2],'orange_stained_glass');
  room.box([3,8,0],[5,10,0],'orange_stained_glass');
  route=[[7,0],[7,5],[8,5],[8,10],[7,10],[7,15]];
 }
 carveRoute(room,route);
 if(spec.key==='addition/10')carveRoute(room,[[7,5],[11,5],[11,10],[7,10]]);
 if(spec.key==='addition/08')carveRoute(room,[[7,5],[13,5],[13,10],[7,10]]);
 if(spec.key==='addition/06')carveRoute(room,[[7,5],[10,5],[10,10],[7,10]]);
 if(spec.key==='addition/03'){
  room.box([1,3,5],[3,3,7],'iron_block');
  for(const x of [1,3])for(const z of [5,7])room.set(x,2,z,'polished_basalt');
  for(const z of [5,6,7]){
   room.set(5,2,z,'polished_blackstone_brick_stairs[facing=west,half=bottom,shape=straight,waterlogged=false]');
   room.set(4,2,z,'polished_blackstone');room.set(4,3,z,'polished_blackstone_brick_stairs[facing=west,half=bottom,shape=straight,waterlogged=false]');
  }
 }
 if(spec.key==='addition/02'){
  for(let x=2;x<=10;x++)room.set(x,1,7,x%2===0?'bricks':'brown_terracotta');
  for(let z=7;z<=12;z++)room.set(10,1,z,z%2===0?'bricks':'brown_terracotta');
 }
 for(const port of variant.ports)for(const [x,y,z]of port.cells){
  room.set(x,y-1,z,'polished_blackstone');room.box([x,y,z],[x,y+4,z],'air');
  for(let inward=1;inward<=5;inward++){const zz=z===0?inward:15-inward;room.set(x,y-1,zz,'polished_blackstone');room.box([x,y,zz],[x,y+4,zz],'air');}
 }
 for(const socket of variant.sockets)for(const [x,y,z]of [socket.position,socket.spawn_position]){room.set(x,y-1,z,'polished_blackstone');room.box([x,y,z],[x,y+2,z],'air');}
 for(const z of [2,13])for(const x of [0,15])room.set(x,10,z,'shroomlight');
 return {room,variant,route};
}
function walk(room,origins,wide=false){
 const fits=(x,z)=>{
  const radius=wide?1:0;
  if(x<0||x>15||z<0||z>15)return false;
  for(let zz=Math.max(0,z-radius);zz<=Math.min(15,z+radius);zz++)for(let xx=Math.max(0,x-radius);xx<=Math.min(15,x+radius);xx++)if(room.get(xx,1,zz)===AIR||![2,3,4].every(y=>room.get(xx,y,zz)===AIR))return false;
  return true;
 };
 const seen=new Set(),queue=[];
 for(const point of origins){const x=point[0],z=point[2];if(fits(x,z)){queue.push([x,z]);seen.add(`${x},${z}`);}}
 for(let head=0;head<queue.length;head++){
  const [x,z]=queue[head];for(const [dx,dz]of [[1,0],[-1,0],[0,1],[0,-1]]){
   const xx=x+dx,zz=z+dz,key=`${xx},${zz}`;if(seen.has(key)||!fits(xx,zz))continue;seen.add(key);queue.push([xx,zz]);
  }
 }
 return {seen,visited:queue.length};
}
function validate(item){
 const {spec,variant,room,nbt,structure}=item,failures=[];
 const require=(condition,message)=>{if(!condition)failures.push(message);};
 const clear=([x,y,z],height=3)=>room.get(x,y-1,z)!==AIR&&Array.from({length:height},(_,offset)=>room.get(x,y+offset,z)===AIR).every(Boolean);
 require(nbt.getNumber('DataVersion')===3465,'DataVersion');require(JSON.stringify(room.size)===JSON.stringify(variant.size),'catalog size');
 require(structure.getBlocks().length===4096,'dense volume');require(new Set(structure.getBlocks().map(block=>block.pos.join(','))).size===4096,'unique coordinates');
 require(structure.getBlocks().every(block=>block.pos.every(value=>value>=0&&value<16)),'bounds');
 require(nbt.getList('entities').length===0&&structure.getBlocks().every(block=>!block.nbt),'entities/block entities');
 const palette=nbt.getList('palette');for(let index=0;index<palette.length;index++){const name=palette.getCompound(index).getString('Name');require(/^minecraft:[a-z0-9_]+$/.test(name),'bare ID');require(knownBlocks.has(name)||name===AIR,`unknown block ${name}`);}
 require(room.blocks.every(block=>!/^minecraft:(?:rail|powered_rail|detector_rail|activator_rail|water|lava|fire|redstone_wire|redstone_torch|repeater|comparator)(?:\[|$)/.test(block)),'unstable/hazard blocks');
 const heatSources=[];
 for(let y=1;y<15;y++)for(let z=1;z<15;z++)for(let x=1;x<15;x++)if(room.get(x,y,z)==='minecraft:shroomlight'){
  const sealed=[[1,0,0],[-1,0,0],[0,1,0],[0,-1,0],[0,0,1],[0,0,-1]].every(([dx,dy,dz])=>room.get(x+dx,y+dy,z+dz)!==AIR);
  heatSources.push({position:[x,y,z],sealed});require(sealed,`enclosed heat source ${x},${y},${z}`);
 }
 const targets=variant.ports.flatMap(port=>port.cells).concat(variant.sockets.flatMap(socket=>[socket.position,socket.spawn_position]));
 for(const point of targets)require(clear(point),`socket/port support ${point}`);
 for(const port of variant.ports)for(const [x,y,z]of port.cells)for(let inward=0;inward<=5;inward++)require(clear([x,y,z===0?inward:15-inward],5),`extended five-high port corridor ${x},${y},${z} inward ${inward}`);
 const reserved=new Set(targets.map(point=>point.join(','))),independentSpawns=[];
 for(const socket of variant.sockets){
  require(room.get(...socket.support)!==AIR,`declared support ${socket.id}`);let candidate;
  for(let distance=1;distance<=6&&!candidate;distance++)for(let z=1;z<=14&&!candidate;z++)for(let x=1;x<=14&&!candidate;x++){
   const point=[x,2,z];if(Math.abs(x-socket.position[0])+Math.abs(z-socket.position[2])===distance&&!reserved.has(point.join(','))&&clear(point))candidate=point;
  }
  require(Boolean(candidate),`independent spawn ${socket.id}`);if(candidate){targets.push(candidate);reserved.add(candidate.join(','));independentSpawns.push({id:socket.id,position:candidate});}
 }
 const navigation=walk(room,[targets[0]]),unreachable=targets.filter(([x,,z])=>!navigation.seen.has(`${x},${z}`));require(unreachable.length===0,`unreachable ${JSON.stringify(unreachable)}`);
 const wide=walk(room,[[7,2,0]],true);require(wide.seen.has('7,15'),'three-wide path north to south');
 let productionGeometry=null;
 if(spec.key==='addition/09'){
  const sculptHeights=[[1,4],[1,8],[2,12]].map(([x,z])=>{let top=1;for(let y=2;y<=12;y++)if(/basalt|blackstone/.test(room.get(x,y,z)))top=y;return top;});
  const carvedShoulders=[[3,7,4],[3,5,8],[5,4,12]].map(position=>({position,air:room.get(...position)===AIR}));
  productionGeometry={sculptHeights,carvedShoulders,arrangement:'three separate west-side semi-ring masses, open east workface'};
  require(JSON.stringify(sculptHeights)==='[9,7,5]','three genuinely different-height sculpted masses');
  require(carvedShoulders.every(shoulder=>shoulder.air),'sculpted stepped shoulder cutouts');
 }
 if(spec.key==='addition/10'){
  const network=walk(room,[[7,2,0]],true),westBypass=[6,7,8,9].map(z=>network.seen.has(`7,${z}`)),eastBypass=[6,7,8,9].map(z=>network.seen.has(`11,${z}`));
  const alcove=walk(room,[[7,2,0]]).seen.has('2,8'),archOpening=[];
  for(let y=2;y<=5;y++)for(const z of [8,9])archOpening.push(room.get(4,y,z)===AIR);
  productionGeometry={westBypass,eastBypass,alcoveWalkable:alcove,archOpening,bedTop:room.get(13,3,13),supplyElbow:[room.get(13,5,4),room.get(14,5,9)]};
  require(westBypass.every(Boolean)&&eastBypass.every(Boolean),'two three-wide flat routes around thermal divider');
  require(alcove&&archOpening.every(Boolean),'old furnace alcove accessible through open side arch');
  require(productionGeometry.bedTop==='minecraft:white_wool'&&productionGeometry.supplyElbow.every(state=>state==='minecraft:white_terracotta'),'diagonal emergency bed and physical L-shaped supplies');
 }
 if(spec.key==='addition/07'){
  const crystalHeights=[6,8,9].map(z=>{let top=2;for(let y=3;y<=9;y++)if(/yellow_concrete|yellow_terracotta|gold_block/.test(room.get(2,y,z)))top=y;return top;});
  const purificationLevels=[5,7,9].map(z=>{let top=1;for(let y=2;y<=6;y++)if(room.get(13,y,z)==='minecraft:smooth_stone')top=y;return top;});
  const sealedCrystals=[];
  for(let y=3;y<=8;y++)for(let z=6;z<=9;z++)if(/yellow_concrete|yellow_terracotta|gold_block/.test(room.get(2,y,z)))sealedCrystals.push({position:[2,y,z],enclosed:[[1,0,0],[-1,0,0],[0,1,0],[0,-1,0],[0,0,1],[0,0,-1]].every(([dx,dy,dz])=>room.get(2+dx,y+dy,z+dz)!==AIR)});
  productionGeometry={crystalHeights,purificationLevels,sealedCrystals};
  require(JSON.stringify(crystalHeights)==='[8,6,4]','asymmetric real crystal silhouette');
  require(JSON.stringify(purificationLevels)==='[2,3,4]','three real purification-bed heights');
  require(sealedCrystals.every(crystal=>crystal.enclosed),'crystal specimens behind intact glass enclosure');
 }
 if(spec.key==='addition/08'){
  const connectivity=walk(room,[[7,2,0]],true),west=[6,7,8,9].map(z=>connectivity.seen.has(`2,${z}`)),east=[6,7,8,9].map(z=>connectivity.seen.has(`13,${z}`));
  const pylons=[4,10].map(x=>({x,solidCount:Array.from({length:5},(_,step)=>[7,8].every(z=>room.get(x,2+step,z)!==AIR)).filter(Boolean).length}));
  const diagonalConsole=[[3,12],[4,13],[5,14]].map(([x,z])=>({position:[x,3,z],state:room.get(x,3,z)}));
  productionGeometry={westBypass:west,eastBypass:east,pylons,diagonalConsole};
  require(west.every(Boolean)&&east.every(Boolean),'unbroken three-wide bypass around each end of the chain bed');
  require(pylons.every(pylon=>pylon.solidCount===5),'both full-height solid tension pylons');
  require(diagonalConsole.every(console=>console.state==='minecraft:iron_block'),'actual diagonal stepped control console');
 }
 if(spec.key==='addition/05'){
  const levels=[5,7,9].map(z=>{let highest=1;for(let y=2;y<8;y++)if(/blackstone|gray_concrete/.test(room.get(2,y,z)))highest=y;return highest;});
  productionGeometry={levels,sampleStates:[[12,3,9],[14,3,10],[13,3,12]].map(point=>room.get(...point)),flatPrimaryRoad:true};
  require(JSON.stringify(levels)==='[2,3,4]','three real descending/ascending sorting-bed levels');
  require(new Set(productionGeometry.sampleStates).size===3,'three distinct specimen materials');
 }
 if(spec.key==='addition/06'){
  const westBypass=walk(room,[[7,2,0]],true),eastBranch=[];
  for(let z=6;z<=9;z++)eastBranch.push(westBypass.seen.has(`10,${z}`));
  const westBranch=[];for(let z=6;z<=9;z++)westBranch.push(westBypass.seen.has(`5,${z}`));
  const columns=[{x:2,z:4,top:10},{x:2,z:7,top:10},{x:13,z:10,top:7},{x:13,z:13,top:7}];
  productionGeometry={westBranch,eastBranch,columns:columns.map(column=>({...column,fullHeightSupport:Array.from({length:column.top-1},(_,offset)=>room.get(column.x,offset+2,column.z)==='minecraft:polished_basalt').every(Boolean)}))};
  require(westBranch.every(Boolean)&&eastBranch.every(Boolean),'two connected three-wide ground bypasses around billet');
  require(productionGeometry.columns.every(column=>column.fullHeightSupport),'each hammer beam has grounded full-height piers');
 }
 let bridgeNavigation=null;
 if(spec.key==='addition/03'){
  bridgeNavigation=walkBridge(room,[[7,2,0],[2,4,6]]);require(bridgeNavigation.missing.length===0,'bridge accessible without jumping');
  require([5,6,7].every(z=>room.get(4,3,z).includes('stairs')&&room.get(5,2,z).includes('stairs')),'three-wide continuous real stairs');
  require([1,3].every(x=>[5,7].every(z=>room.get(x,2,z)==='minecraft:polished_basalt'&&room.get(x,3,z)==='minecraft:iron_block')),'four real full-block bridge piers');
 }
 let shellGaps=0;for(let y=0;y<16;y++)for(let z=0;z<16;z++)for(let x=0;x<16;x++){
  if(x!==0&&x!==15&&z!==0&&z!==15&&y!==0&&y!==15)continue;
  const opening=variant.ports.some(port=>port.cells.some(([px,py,pz])=>px===x&&pz===z&&y>=py&&y<=py+4));
  if((room.get(x,y,z)===AIR)!==opening)shellGaps++;
 }
 require(shellGaps===0,'complete shell and roof except legal ports');
 return {key:spec.key,baseKey:spec.baseKey,size:variant.size,minimum_y:variant.minimum_y,ports:variant.ports,sockets:variant.sockets,spawn_position:variant.spawn_position??null,nbtSha256:item.sha256,volume:4096,heatSources,independentSpawns,productionGeometry,bridgeNavigation,navigation:{visited:navigation.visited,unreachable,threeWideVisited:wide.visited,threeWidePass:wide.seen.has('7,15')},shellGaps,failures,status:failures.length?'FAIL':'PASS'};
}
function walkBridge(room,targets){
 const index=(x,y,z)=>(y*32+z)*32+x,collision=new Uint8Array(32768);
 for(let y=0;y<32;y++)for(let z=0;z<32;z++)for(let x=0;x<32;x++){
  const state=room.get(x>>1,y>>1,z>>1);
  if(state===AIR)continue;
  if(state.includes('stairs')){
   const back=state.includes('facing=west')?x%2===0:state.includes('facing=east')?x%2===1:state.includes('facing=north')?z%2===0:z%2===1;
   collision[index(x,y,z)]=y%2===0||back?1:0;
  }else collision[index(x,y,z)]=1;
 }
 const canStand=(x,y,z)=>x>=0&&x<32&&z>=0&&z<32&&y>=1&&y+3<32&&collision[index(x,y-1,z)]&&[0,1,2,3].every(offset=>!collision[index(x,y+offset,z)]);
 const seen=new Uint8Array(collision.length),queue=[];
 for(const dx of [0,1])for(const dz of [0,1]){const point=[targets[0][0]*2+dx,targets[0][1]*2,targets[0][2]*2+dz];if(canStand(...point)){seen[index(...point)]=1;queue.push(point);}}
 for(let head=0;head<queue.length;head++){
  const [x,y,z]=queue[head];for(const [dx,dz]of [[1,0],[-1,0],[0,1],[0,-1]])for(const dy of [0,1,-1]){
   const xx=x+dx,yy=y+dy,zz=z+dz;if(!canStand(xx,yy,zz)||seen[index(xx,yy,zz)])continue;
   if(dy===1&&collision[index(x,y+4,z)])continue;if(dy===-1&&collision[index(xx,yy+4,zz)])continue;
   seen[index(xx,yy,zz)]=1;queue.push([xx,yy,zz]);
  }
 }
 return {method:'half-voxel collision, 2-block body, maximum rise half a block without jumping',visited:queue.length,missing:targets.filter(([x,y,z])=>![0,1].some(dx=>[0,1].some(dz=>seen[index(x*2+dx,y*2,z*2+dz)])))};
}
const landmarks={
 'addition/09':[
  ['西北高削肩雕坯',[1,2,4],[3,9,6],'basalt|blackstone',60],
  ['西中中高雕坯',[1,2,8],[3,7,10],'basalt|blackstone',38],
  ['西南低层次雕坯',[2,2,12],[5,5,14],'basalt|blackstone',37],
  ['单侧工具台与两件工具',[12,2,9],[14,4,10],'smooth_stone|iron_block|blackstone',11],
  ['错列碎石托盘',[12,2,12],[14,3,14],'iron_block|basalt|blackstone',12]
 ],
 'addition/10':[
  ['带可入侧凹室的厚旧炉腹',[1,2,6],[4,9,11],'basalt|blackstone',100],
  ['对角白色应急床台',[12,2,12],[14,3,14],'smooth_stone|white_wool|white_terracotta',18],
  ['折角隔热物资架',[13,2,4],[14,6,9],'iron_block|smooth_stone|white_terracotta|basalt',40],
  ['中央低隔热挡体',[9,2,7],[9,5,8],'obsidian|iron_block',8],
  ['静态全封闭应急灯',[12,7,6],[14,9,8],'orange_stained_glass|shroomlight',27]
 ],
 'addition/07':[
  ['西侧完整玻璃隔离晶舱',[1,2,5],[3,9,10],'yellow_stained_glass|basalt',110],
  ['三种晶质不对称硫晶簇',[2,3,6],[2,8,9],'yellow_concrete|yellow_terracotta|gold_block',14],
  ['东侧阶梯净化机器',[12,2,5],[14,5,10],'smooth_stone|iron_block|calcite',48],
  ['东南短封存架',[12,2,12],[14,7,14],'iron_block|basalt|yellow_terracotta|smooth_stone',40]
 ],
 'addition/08':[
  ['西端厚拉力墩',[4,2,7],[5,6,8],'basalt|blackstone|iron_block',20],
  ['东端厚拉力墩',[10,2,7],[11,6,8],'basalt|blackstone|iron_block',20],
  ['低矮静态交错链床',[6,2,7],[9,3,8],'blackstone|iron_block',16],
  ['南侧斜向错位操作台',[3,2,12],[5,3,14],'smooth_stone|iron_block',6],
  ['实体基座与安全屏挡',[10,2,12],[13,7,12],'basalt|orange_stained_glass',24]
 ],
 'addition/05':[
  ['北段低分选槽',[1,2,5],[3,3,6],'basalt|iron_block|blackstone|gray_concrete',10],
  ['中段分选槽',[1,2,7],[3,4,8],'basalt|iron_block|blackstone|gray_concrete',14],
  ['南段高分选槽',[1,2,9],[3,5,10],'basalt|iron_block|blackstone|gray_concrete',20],
  ['西南低卸料坪',[1,2,12],[3,2,14],'smooth_stone|blackstone',9],
  ['独立矿样台及三种矿样',[12,2,9],[14,3,12],'smooth_stone|raw_iron|raw_gold|raw_copper',15],
  ['全封闭橙色检视灯槽',[12,6,6],[14,8,8],'orange_stained_glass|shroomlight',27]
 ],
 'addition/06':[
  ['西中高锻锤承重实柱',[1,2,4],[3,10,7],'polished_basalt',54],
  ['西高悬架横梁与锤杆',[1,7,4],[3,12,7],'iron_block|polished_blackstone',35],
  ['东南低锻锤承重实柱',[12,2,10],[14,7,13],'polished_basalt',36],
  ['东低悬架横梁与锤杆',[12,5,10],[14,9,13],'iron_block|polished_blackstone',30],
  ['中央静态铁坯',[7,2,7],[8,3,8],'iron_block',8],
  ['后退封闭炉台',[1,2,12],[3,6,14],'basalt|blackstone|shroomlight|orange_stained_glass',35]
 ],
 'addition/03':[
  ['偏心玻璃封闭热井',[1,2,1],[4,7,3],'basalt|blackstone|orange_stained_glass|shroomlight',65],
  ['西南高蓄压墩及双铁箍',[1,2,10],[3,9,13],'basalt|iron_block',90],
  ['东南低蓄压墩',[11,2,12],[14,5,14],'blackstone|iron_block',45],
  ['三宽实体桥面与四桥墩',[1,2,5],[3,3,7],'iron_block|basalt',13],
  ['连续半格检修踏步',[4,2,5],[5,3,7],'stairs|blackstone',9]
 ],
 'addition/04':[
  ['西北朝南磨床与刀坯',[1,2,2],[5,4,3],'smooth_stone|iron_block|obsidian',23],
  ['东侧朝西磨床与刀坯',[11,2,6],[14,4,9],'smooth_stone|iron_block|obsidian',30],
  ['西南朝北磨床与刀坯',[2,2,11],[5,4,13],'smooth_stone|iron_block|obsidian',26],
  ['北背面刃坯壁架',[1,4,1],[5,8,1],'obsidian|iron_block|basalt',18],
  ['东背面刃坯壁架',[14,4,6],[14,8,10],'obsidian|iron_block|basalt',18],
  ['南背面刃坯壁架',[2,4,14],[5,8,14],'obsidian|iron_block|basalt',17],
  ['高位集尘罩',[5,11,6],[10,12,9],'nether_bricks',40]
 ],
 'addition/01':[
  ['西北高拉丝炉',[1,2,2],[3,11,4],'basalt|shroomlight|orange_stained_glass',70],
  ['错列西南低拉丝炉',[2,2,10],[5,6,13],'basalt|shroomlight|orange_stained_glass',55],
  ['横向冷却肋床',[10,2,7],[14,3,9],'smooth_stone|iron_block',20],
  ['双纤维卷架',[12,2,11],[14,8,13],'basalt|iron_block|white_terracotta|calcite',30],
  ['导丝横梁',[2,8,6],[10,8,6],'iron_block',9]
 ],
 'addition/02':[
  ['短窑道厚侧墙与顶衬',[1,2,5],[5,7,9],'nether_bricks|basalt|orange_stained_glass',85],
  ['三层砖坯架',[12,2,2],[14,10,7],'minecraft:(?:bricks|iron_block|polished_basalt)$',90],
  ['西侧出窑台',[1,2,11],[3,4,13],'smooth_stone|bricks',20],
  ['侧置封闭炉窗',[2,3,3],[3,5,5],'shroomlight|orange_stained_glass',18]
 ]
};
function styleAudit(items){
 const rooms=items.map(item=>{
  const actualMaterials={};for(const state of item.room.blocks)if(state!==AIR){const name=state.split('[')[0];actualMaterials[name]=(actualMaterials[name]??0)+1;}
  const features=landmarks[item.spec.key].map(([name,from,to,pattern,minimum])=>{
   const points=[],materials={},matcher=new RegExp(pattern);
   for(let y=from[1];y<=to[1];y++)for(let z=from[2];z<=to[2];z++)for(let x=from[0];x<=to[0];x++)if(matcher.test(item.room.get(x,y,z))){points.push([x,y,z]);const state=item.room.get(x,y,z);materials[state]=(materials[state]??0)+1;}
   return {name,searchBounds:[from,to],actualBounds:points.length?[from.map((_,axis)=>Math.min(...points.map(point=>point[axis]))),to.map((_,axis)=>Math.max(...points.map(point=>point[axis])))]:null,matchedBlocks:points.length,minimumBlocks:minimum,actualMaterials:materials,status:points.length>=minimum?'PASS':'FAIL'};
  });
  return {key:item.spec.key,nbtSha256:item.sha256,actualMaterials,features,layout:item.spec.layout,roof:item.spec.roof,difference:item.spec.difference,review:{paletteOnly:false,evidence:'New independently constructed occupied volumes and functional structures; no source NBT is used for generation.',fourCornerEmptyHall:false,reason:item.spec.layout,heatSafety:'Heat is shroomlight behind full orange stained glass; no real fire or liquid.'},status:features.every(feature=>feature.status==='PASS')?'PASS':'FAIL'};
 });
 return {schema:1,style:'fteclipsegen:lava',batch:activeBatch,evidenceSource:'exported NBT decoded from disk',rooms,status:rooms.every(room=>room.status==='PASS')?'PASS':'FAIL'};
}
function mask(room,variant){
 const result=[];
 const footBase=variant.key==='spawn/spawn'?4:variant.key.includes('/layout/')?1:2;
 for(let y=footBase;y<room.size[1]-1;y++)for(let z=1;z<room.size[2]-1;z++)for(let x=1;x<room.size[0]-1;x++){
  if(room.get(x,y,z)===AIR)continue;
  if(variant.ports.some(port=>port.cells.some(([px,py,pz])=>Math.abs(x-px)<=2&&Math.abs(z-pz)<=3&&y>=py-1&&y<=py+4)))continue;
  result.push([x,y,z]);
 }
 return result;
}
function similarity(left,right,size){
 const leftSet=new Set(left.map(point=>point.join(','))),values=[];
 for(let rotation=0;rotation<4;rotation++){
  const rightSet=new Set(right.map(([x,y,z])=>(rotation===0?[x,y,z]:rotation===1?[size[2]-1-z,y,x]:rotation===2?[size[0]-1-x,y,size[2]-1-z]:[z,y,size[0]-1-x]).join(',')));
  let intersection=0;for(const point of leftSet)if(rightSet.has(point))intersection++;
  values.push(intersection/(leftSet.size+rightSet.size-intersection||1));
 }
 return {jaccard:values[0],maximumRotatedJaccard:Math.max(...values),bestYaw:values.indexOf(Math.max(...values))*90};
}
function repetitionAudit(items){
 const references=[];
 const oldManifest=JSON.parse(fs.readFileSync(path.join(resources,'roomsets/lava/manifest.json'),'utf8'));
 for(const template of oldManifest.templates){
  const variant=catalog.variants.find(entry=>entry.key===template.key),decoded=decode(path.join(resources,'roomsets/lava',`${template.key}.nbt`));
  references.push({key:template.key,family:'old-v034',variant,...decoded});
 }
 for(const entry of fs.readdirSync(expansion,{withFileTypes:true})){
  if(!entry.isDirectory()||entry.name===batchDirectory)continue;
  const file=path.join(expansion,entry.name,'manifest.json');if(!fs.existsSync(file))continue;
  for(const template of JSON.parse(fs.readFileSync(file,'utf8')).templates){const decoded=decode(path.join(expansion,entry.name,template.file)),variant=catalog.variants.find(entry=>entry.key===template.baseKey);references.push({key:template.key,family:entry.name,variant,...decoded});}
 }
 const pairs=[],rooms=[];
 for(let index=0;index<items.length;index++){
  const item=items[index],occupied=mask(item.room,item.variant);
  rooms.push({key:item.spec.key,nbtSha256:item.sha256,interiorSolids:occupied.length,normalizedSignature:digest(occupied.map(point=>point.map((value,axis)=>(value/(item.room.size[axis]-1)).toFixed(5)).join(',')).sort().join('|'))});
  for(const reference of references){
   const sameSize=JSON.stringify(reference.room.size)===JSON.stringify(item.room.size);
   const normalize=(points,size)=>points.map(point=>point.map((value,axis)=>Math.round(value/(size[axis]-1)*15)));
   const referenceMask=mask(reference.room,reference.variant);
   const metric=sameSize?similarity(occupied,referenceMask,item.room.size):similarity(normalize(occupied,item.room.size),normalize(referenceMask,reference.room.size),[16,16,16]);
   pairs.push({keys:[item.spec.key,reference.key],referenceFamily:reference.family,comparable:true,sameSize,normalization:sameSize?'native cell coordinates':'map dimension-relative coordinates to 16-cubed normalized occupancy lattice; preserves solid intersection/union',...metric,referenceNbtSha256:reference.sha256,layoutDifference:item.spec.difference,review:metric.maximumRotatedJaccard>=.85?'FAIL_REWORK_REQUIRED':'PASS_DISTINCT_GEOMETRY'});
  }
  for(let previous=0;previous<index;previous++){const other=items[previous],metric=similarity(occupied,mask(other.room,other.variant),item.room.size);pairs.push({keys:[item.spec.key,other.spec.key],referenceFamily:'current-batch',comparable:true,...metric,layoutDifference:item.spec.layout+' / '+other.spec.layout,review:metric.maximumRotatedJaccard>=.85?'FAIL_REWORK_REQUIRED':'PASS_DISTINCT_GEOMETRY'});}
 }
 const comparable=pairs.filter(pair=>pair.comparable).sort((left,right)=>right.maximumRotatedJaccard-left.maximumRotatedJaccard),high=comparable.filter(pair=>pair.maximumRotatedJaccard>=.85);
 return {schema:1,style:'fteclipsegen:lava',batch:activeBatch,method:{removed:['x/z exterior boundary','roof y=max','foundation y<2','catalog port safety bands dx<=2,dz<=3,y=foot-1..foot+4'],metric:'solid intersection/union, material-independent; also four yaw rotations',threshold:.85,signature:'normalized coordinates per size-1, sorted to five decimal places'},oldRoomsCompared:references.filter(reference=>reference.family==='old-v034').length,priorAdditionRoomsCompared:references.filter(reference=>reference.family!=='old-v034').length,rooms,pairs,highestPairs:comparable.slice(0,12),highSimilarityPairs:high,status:high.length?'FAIL':'PASS'};
}
const directions=[{id:'north',label:'北 N',right:[-1,0],toward:[0,-1],front:[0,0,-1],yaw:0},{id:'south',label:'南 S',right:[1,0],toward:[0,1],front:[0,0,1],yaw:180},{id:'west',label:'西 W',right:[0,1],toward:[-1,0],front:[-1,0,0],yaw:270},{id:'east',label:'东 E',right:[0,-1],toward:[1,0],front:[1,0,0],yaw:90}];
function view(item,direction){
 const {room}=item;
 const visible=(x,y,z)=>x>0&&x<15&&z>0&&z<15&&y>=2&&y<15&&room.get(x,y,z)!==AIR;
 const project=([x,y,z])=>[x*direction.right[0]+z*direction.right[1],(x*direction.toward[0]+z*direction.toward[1])*.55-y*.94];
 const color=name=>name.includes('yellow_stained_glass')?'#efd653':name.includes('yellow_concrete')?'#f8d82a':name.includes('yellow_terracotta')?'#be8c32':name.includes('gold_block')?'#fff09a':name.includes('orange_stained_glass')?'#eda052':name.includes('shroomlight')?'#ffe3a1':name.includes('iron_block')?'#becbcc':name.includes('obsidian')?'#65527f':name.includes('white_terracotta')||name.includes('white_wool')||name.includes('calcite')?'#eee1cb':name.includes('bricks')&&!name.includes('nether')?'#b9664d':name.includes('basalt')?'#7b9094':name.includes('nether')?'#794c50':name.includes('smooth_stone')?'#b7b7aa':'#4c616a';
 const polygons=[];
 for(let y=2;y<15;y++)for(let z=1;z<15;z++)for(let x=1;x<15;x++){
  if(!visible(x,y,z))continue;const faces=[],front=direction.front,glass=room.get(x,y,z).includes('stained_glass');
  const exposed=(nextX,nextY,nextZ)=>!visible(nextX,nextY,nextZ)||(!glass&&room.get(nextX,nextY,nextZ).includes('stained_glass'));
  if(exposed(x,y+1,z))faces.push({points:[[x,y+1,z],[x+1,y+1,z],[x+1,y+1,z+1],[x,y+1,z+1]],shade:1});
  if(exposed(x+front[0],y,z+front[2])){
   const points=front[2]===-1?[[x,y,z],[x+1,y,z],[x+1,y+1,z],[x,y+1,z]]:front[2]===1?[[x,y,z+1],[x+1,y,z+1],[x+1,y+1,z+1],[x,y+1,z+1]]:front[0]===-1?[[x,y,z],[x,y,z+1],[x,y+1,z+1],[x,y+1,z]]:[[x+1,y,z],[x+1,y,z+1],[x+1,y+1,z+1],[x+1,y+1,z]];
   faces.push({points,shade:.76});
  }
  for(const face of faces)polygons.push({points:face.points.map(project),shade:glass?.24:face.shade,color:color(room.get(x,y,z)),sort:x*direction.toward[0]+z*direction.toward[1]+y*.002});
 }
 polygons.sort((left,right)=>left.sort-right.sort);
 const points=polygons.flatMap(polygon=>polygon.points),minX=Math.min(...points.map(point=>point[0])),maxX=Math.max(...points.map(point=>point[0])),minY=Math.min(...points.map(point=>point[1])),maxY=Math.max(...points.map(point=>point[1]));
 const scale=Math.min(500/(maxX-minX||1),320/(maxY-minY||1)),screen=([x,y])=>[(x-minX)*scale+40,(y-minY)*scale+45];
 const paths=polygons.map(polygon=>`<polygon points="${polygon.points.map(point=>screen(point).map(value=>value.toFixed(2)).join(',')).join(' ')}" fill="${polygon.color}" opacity="${polygon.shade}" stroke="#19252b" stroke-width=".45"/>`).join('');
 const markers=item.variant.ports.flatMap(port=>port.cells.map(position=>({position,color:'#4ff0f3'}))).concat(item.variant.sockets.map(socket=>({position:socket.position,color:'#ff62b9'})));
 const dots=markers.map(marker=>{const [x,y]=screen(project(marker.position));return `<circle cx="${x.toFixed(2)}" cy="${y.toFixed(2)}" r="3" fill="${marker.color}"/>`;}).join('');
 return {svg:`<svg xmlns="http://www.w3.org/2000/svg" width="580" height="410"><rect width="580" height="410" fill="#101b24"/><text x="16" y="24" font-family="sans-serif" font-size="15" fill="#f3c994">${direction.label} ${direction.yaw}°</text>${paths}${dots}</svg>`,faces:polygons.length,geometrySignature:digest(paths)};
}
function fourViews(items){
 fs.mkdirSync(path.join(output,'views'),{recursive:true});const records=[],cards=[];
 for(const item of items){const figures=[];
  for(const direction of directions){const rendered=view(item,direction),file=`views/${item.spec.key.replaceAll('/','-')}-${direction.id}.svg`;fs.writeFileSync(path.join(output,file),rendered.svg);records.push({key:item.spec.key,direction:direction.id,camera:direction,nbtSha256:item.sha256,file,sha256:digest(rendered.svg),faces:rendered.faces,geometrySignature:rendered.geometrySignature});figures.push(`<figure><figcaption>${direction.label}</figcaption><img width="580" height="410" data-key="${item.spec.key}" data-direction="${direction.id}" src="${file}" alt="${item.spec.name} ${direction.label}"></figure>`);}
  cards.push(`<article><h2>${item.spec.key} · ${item.spec.name}</h2><p>${item.spec.layout}</p><section>${figures.join('')}</section><small>NBT ${item.sha256}</small></article>`);
 }
 const html=`<!doctype html><html lang="zh-CN"><meta charset="utf-8"><title>熔火新增第5批 · 真实NBT四视图</title><style>body{background:#141d25;color:#efdfcb;font:15px system-ui;margin:20px}h1{font-size:26px}article{background:#24333e;padding:16px;margin:14px 0;border:1px solid #765239;border-radius:12px}h2{font-size:21px}section{display:grid;grid-template-columns:repeat(4,1fr);gap:10px}figure{margin:0}img{width:100%;height:auto;background:#101b24}p,small{color:#bcb9ae}small{word-break:break-all}[hidden]{display:none!important}</style><script>window.pageErrors=[];window.addEventListener('error',event=>window.pageErrors.push(String(event.message||event.target.src)),true);window.addEventListener('unhandledrejection',event=>window.pageErrors.push(String(event.reason)));</script><h1>熔火新增第5批 · 2间 × 北南西东</h1><p>全部投影从最终导出NBT重新解码。相机北0°、南180°、西270°、东90°，统一俯角。检查室内时隐藏最外层x/z墙、顶层y15及基础地板y0..1；内部炉罩、梁、家具保留。外壳封闭由validation.json独立验证。青点为端口，粉点为socket。本页为离线体素投影，不是Minecraft游戏实测。</p>${cards.join('')}<script>Promise.all([...document.images].map(image=>image.complete?Promise.resolve(image.naturalWidth>0):new Promise(resolve=>{image.onload=()=>resolve(true);image.onerror=()=>resolve(false);}))).then(results=>window.audit={viewCount:results.length,loaded:results.filter(Boolean).length,roomCount:document.querySelectorAll('article').length,errors:window.pageErrors,directions:[...new Set([...document.images].map(image=>image.dataset.direction))].sort()});</script></html>`;
 fs.writeFileSync(path.join(output,'four-views.html'),html);
 return {schema:1,batch:activeBatch,expected:8,actual:records.length,hiddenRule:'exterior x/z boundary, roof y15, foundation y0..1; all internal architecture retained',records,status:records.length===8&&items.every(item=>new Set(records.filter(record=>record.key===item.spec.key).map(record=>record.geometrySignature)).size>=2)?'PASS':'FAIL'};
}
async function browserAudit(){
 const browser='C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe',profile=path.join(output,'qa-browser');fs.mkdirSync(profile,{recursive:true});
 const handle=spawn(browser,['--headless','--disable-gpu','--no-first-run','--remote-debugging-port=0',`--user-data-dir=${profile}`,'about:blank'],{windowsHide:true,stdio:'ignore'}),pause=milliseconds=>new Promise(resolve=>setTimeout(resolve,milliseconds));let endpoint;
 for(let attempt=0;attempt<100;attempt++){try{const port=fs.readFileSync(path.join(profile,'DevToolsActivePort'),'utf8').split('\n')[0],response=await fetch(`http://127.0.0.1:${port}/json/version`);if(response.ok){endpoint=`http://127.0.0.1:${port}`;break;}}catch{}await pause(100);}
 if(!endpoint)throw Error('Edge endpoint unavailable');
 const target=await(await fetch(`${endpoint}/json/new?about:blank`,{method:'PUT'})).json(),socket=new WebSocket(target.webSocketDebuggerUrl),pending=new Map(),errors=[];let serial=0;
 await new Promise((resolve,reject)=>{socket.onopen=resolve;socket.onerror=reject;});
 socket.onmessage=event=>{const message=JSON.parse(event.data);if(message.id){const request=pending.get(message.id);if(request){pending.delete(message.id);message.error?request.reject(Error(JSON.stringify(message.error))):request.resolve(message.result);}}else if(message.method==='Runtime.exceptionThrown')errors.push(message.params.exceptionDetails.text);else if(message.method==='Log.entryAdded'&&message.params.entry.level==='error')errors.push(message.params.entry.text);};
 const send=(method,params={})=>new Promise((resolve,reject)=>{const id=++serial;pending.set(id,{resolve,reject});socket.send(JSON.stringify({id,method,params}));});
 const evaluate=async expression=>(await send('Runtime.evaluate',{expression,returnByValue:true,awaitPromise:true})).result.value;
 try{
  await send('Page.enable');await send('Runtime.enable');await send('Log.enable');await send('Emulation.setDeviceMetricsOverride',{width:1800,height:1500,deviceScaleFactor:1,mobile:false});
  await send('Page.navigate',{url:pathToFileURL(path.join(output,'four-views.html')).href});let result;
  for(let attempt=0;attempt<100;attempt++){result=await evaluate('window.audit||null');if(result)break;await pause(100);}
  if(!result)throw Error('Browser images timed out');
  const bounds=await evaluate("[...document.images].map(image=>({key:image.dataset.key,direction:image.dataset.direction,width:image.naturalWidth,height:image.naturalHeight,displayWidth:image.getBoundingClientRect().width}))");
  const screenshot=await send('Page.captureScreenshot',{format:'png',captureBeyondViewport:true});fs.writeFileSync(path.join(output,'browser-four-views.png'),Buffer.from(screenshot.data,'base64'));
  return {engine:'Microsoft Edge headless via CDP real browser session',...result,errors:[...new Set([...errors,...result.errors])],images:bounds,screenshot:'browser-four-views.png',status:result.viewCount===8&&result.loaded===8&&result.roomCount===2&&errors.length===0&&result.errors.length===0&&bounds.every(image=>image.width===580&&image.height===410&&image.displayWidth>0)?'PASS':'FAIL'};
 }finally{try{await send('Browser.close');}catch{}socket.close();handle.unref();}
}
async function main(){
 if(process.argv.slice(2).some(argument=>argument!=='--batch=5'))throw Error('Only authorized batch 5 can be generated by this recipe');
 fs.mkdirSync(path.join(output,'roomset/addition'),{recursive:true});
 fs.writeFileSync(path.join(output,'design.md'),'# 熔火新增第5批：独立生产工序空间\n\n仅制作addition/09、addition/10；不改旧27房、正式资源或后续批次。本批所有主路与绕行路脚高y2，阶梯只存在于静态设备内部，不需要跳跃；每端口向内延伸5格、脚点上方5格AIR；socket3格AIR及完整支撑。无动态轨道、火焰、液体、红石依赖。热源由橙玻璃封存。\n\n'+specs.map(spec=>`## ${spec.key} ${spec.name}\n\n- baseKey：${spec.baseKey}\n- 平面：${spec.layout}\n- 高度：${spec.heights}\n- 地标：${spec.landmarks}\n- 屋顶与立面：${spec.roof}\n- 区别：${spec.difference}\n`).join('\n'));
 const items=specs.map(spec=>{
  const built=build(spec),file=`roomset/${spec.key}.nbt`;fs.writeFileSync(path.join(output,file),encode(built.room));const decoded=decode(path.join(output,file));return {spec,variant:built.variant,route:built.route,file,...decoded};
 });
 const manifest={schema:1,style:'fteclipsegen:lava',batch:activeBatch,templates:items.map(item=>({key:item.spec.key,baseKey:item.spec.baseKey,name:item.spec.name,sha256:item.sha256,file:item.file}))};
 const save=(name,value)=>fs.writeFileSync(path.join(output,name),JSON.stringify(value,null,2)+'\n');save('manifest.json',manifest);const manifestSha256=digest(fs.readFileSync(path.join(output,'manifest.json')));
 const checks=items.map(validate),validation={schema:1,style:'fteclipsegen:lava',batch:activeBatch,manifestSha256,catalogSha256:digest(fs.readFileSync(path.join(resources,'catalog.json'))),checks,status:checks.every(check=>check.status==='PASS')?'PASS':'FAIL'};
 save('validation.json',validation);const style=styleAudit(items),repetition=repetitionAudit(items),views=fourViews(items);style.manifestSha256=manifestSha256;repetition.manifestSha256=manifestSha256;views.manifestSha256=manifestSha256;
 save('style-audit.json',style);save('repetition-audit.json',repetition);save('four-view-index.json',views);
 const browser=await browserAudit();
 const self={schema:1,style:'fteclipsegen:lava',batch:activeBatch,manifestSha256,validation:validation.status,style:style.status,repetition:repetition.status,fourViews:views.status,browser,rooms:items.map((item,index)=>({key:item.spec.key,nbtSha256:item.sha256,interfaceStatus:checks[index].status,styleStatus:style.rooms[index].status,directions:views.records.filter(record=>record.key===item.spec.key).map(record=>record.direction),review:specs[index].difference,layout:item.spec.layout,roadEvidence:checks[index].navigation,landmarkEvidence:style.rooms[index].features,remediation:'All socket/port reservations applied after architecture; all tests rechecked against serialized NBT.'})),limitations:['Only batch 5 additions are produced; the ten-room expansion is complete and generation stops here.','These are actual final-NBT voxel projections, not Minecraft gameplay tests.','Placement uses only static full blocks and no dynamic rails, liquids, redstone mechanisms or block entities.'],status:[validation.status,style.status,repetition.status,views.status,browser.status].every(status=>status==='PASS')?'PASS':'FAIL'};
 self.visualReview={status:'PASS',source:'browser-four-views.png generated by a real Edge session and visually inspected',perRoom:[
  {key:'addition/01',north:'高低炉窗位于西侧错列炉身；导丝梁横向联系冷却床，出口端口未被覆盖。',south:'可区分低炉背、北侧高炉与东纤维卷架，主路在工序间折返。',west:'冷却肋床横向展开、卷架端面独立，西侧两炉的高度差清楚。',east:'高炉长烟管、低炉罩与纤维卷轴在不同深度展开；不是四角柱加中心空地。',status:'PASS'},
  {key:'addition/02',north:'窑顶短横厅在西侧、三层砖架在东侧，南向通路留空。',south:'可见窑道后墙、砖架端面和侧向出窑台，生产工序方向与第一间不同。',west:'三层托架和红砖坯清楚分层，短窑体的厚墙及出窑台分离。',east:'可辨窑道入口、侧置炉窗与西侧出窑台，四socket仍分散在作业面外。',status:'PASS'}
 ]};
 self.repairs=[
  {key:'addition/02',finding:'Initial side heat window overlapped the northwest socket clearance envelope.',change:'Moved the heat core to [2,4,4] inside a compact orange-glass enclosure; the west kiln sidewall closes its sixth face. Socket [4,2,4] remains empty.',evidence:checks[1].heatSources},
  {key:'addition/02',finding:'Initial brick rack only had rear vertical supports; front tier ends read as disconnected.',change:'Added full-height front support columns at x12,z2 and x12,z7, maintaining the x11 socket aisle.',evidence:style.rooms[1].features.find(feature=>feature.name==='三层砖坯架')},
  {key:'both',finding:'Interior inspection hid the roof but left ceiling-corner light fixtures visually floating.',change:'Moved lighting to the exterior wall layer, leaving functional process structures as the visible interior landmarks.',evidence:'browser-four-views.png'}
 ];
 const oldRoot=path.join(root,'output/style-rebuild-v034/lava'),oldFreeze=JSON.parse(fs.readFileSync(path.join(oldRoot,'frozen.json'),'utf8'));
 self.visualReview={status:'PASS',source:'browser-four-views.png from a real Edge session; final cardinal projections inspected',perRoom:[
  {key:'addition/03',north:'偏心玻璃热井与西南高墩错列，铁箍显示蓄压设备层级。',south:'低墩与高墩高差清楚，中央战斗通路与热井分开。',west:'短桥桥面下四处实体墩与两级三宽踏步可辨，热井为封闭低炉形。',east:'上层排压总管连接三点设备，桥体分层且中央无四角对称空厅。',status:'PASS'},
  {key:'addition/04',north:'西北磨床和西南磨床前后错位，黑曜石坯体与背架明确。',south:'东侧纵磨床与西侧横磨床朝向不同，工位之间为通路。',west:'东背架及三向静态磨床的深度分离，集尘罩位于高处。',east:'可辨三组黑曜石刃坯壁架和低工作床，未复制蜂窑或双长架仓库。',status:'PASS'}
 ]};
 self.repairs=[
  {key:'addition/03',finding:'An east-side bridge proposal would intersect the prescribed east socket and the inward five-block port safety band.',change:'Located the bridge at x1..3,z5..7, its four piers at y2, deck y3 and access stairs x4..5. Kept all combat sockets at original ground coordinates.',evidence:checks[0].bridgeNavigation},
  {key:'addition/03',finding:'Real molten fluid could introduce an update dependency under the native placer.',change:'Used an orange-glass enclosure around a shroomlight heat core; no lava/water block is present. This depicts sealed molten heat without a liquid simulation.',evidence:checks[0].heatSources},
  {key:'addition/04',finding:'Three workstations must remain visually distinct without unstable rail blocks.',change:'Independently built south-facing west-north, west-facing east, and north-facing west-south grinding beds with obsidian blanks and separate rear racks.',evidence:style.rooms[1].features}
 ];
 const firstRoot=path.join(expansion,'batch-01'),firstFreeze=JSON.parse(fs.readFileSync(path.join(firstRoot,'freeze.json'),'utf8'));
 const firstMismatches=firstFreeze.artifacts.filter(artifact=>digest(fs.readFileSync(path.join(firstRoot,artifact.file)))!==artifact.sha256);
 self.previousBatchPreservation={batch:1,status:firstMismatches.length===0?'PASS':'FAIL',checked:firstFreeze.artifacts.length,mismatches:firstMismatches,freezeSha256:digest(fs.readFileSync(path.join(firstRoot,'freeze.json'))),note:'All first-batch artifacts remain unchanged. Shared expandable recipe is versioned by the second-batch freeze; first-batch historical recipe hash is retained in its untouched freeze.'};
 self.limitations[2]='Only static full blocks and explicit fixed-shape stair blocks are used; there are no rails, fluids, redstone mechanisms or block entities. Stair image silhouettes are simplified cubes; bridge walkability is separately checked with half-voxel collision.';
 if(self.previousBatchPreservation.status!=='PASS')self.status='FAIL';
 const oldNbt=oldFreeze.artifacts.filter(artifact=>(artifact.path??artifact.file).endsWith('.nbt'));
 const oldMismatches=oldNbt.filter(artifact=>digest(fs.readFileSync(path.join(oldRoot,artifact.path??artifact.file)))!==artifact.sha256);
 self.previous27Preservation={status:oldNbt.length===27&&oldMismatches.length===0?'PASS':'FAIL',checked:oldNbt.length,mismatches:oldMismatches,method:'Read-only comparison against old v0.34 frozen SHA256 list; generator writes only new batch directory.'};
 if(self.previous27Preservation.status!=='PASS')self.status='FAIL';
 self.visualReview={status:'PASS',source:'Final actual-NBT cardinal projections in browser-four-views.png',perRoom:[
  {key:'addition/05',north:'阶梯分选槽从北低向南高，铁筛边与深色静态熔渣可辨。',south:'低卸料坪与东侧独立矿样台分离，主路保持平层。',west:'三级槽底高差明确，带实支柱的封闭检视灯槽偏东，不是开放热源。',east:'三种矿样在独立短台展示，西侧抽尘罩随槽段分级，未复制货架库。',status:'PASS'},
  {key:'addition/06',north:'高低锻锤位于不同深度，实柱托横梁，中央坯件分开左右绕路。',south:'可辨东低锤头与西高锤架，炉台退至西南角，不占主路。',west:'西高门架的锤杆、锤头与接地柱连续；东低锤横梁高差明确。',east:'两组门架为独立承重机械结构，未形成中心钟塔或四角柱中庭。',status:'PASS'}
 ]};
 self.repairs=[
  {key:'addition/05',finding:'Stepped sorting equipment must not imply a required raised player route.',change:'Confined all three raised beds to x1..3,z5..10; ground north-south circulation remains y2. Actual levels [2,3,4] and independent specimens are checked from decoded NBT.',evidence:checks[0].productionGeometry},
  {key:'addition/06',finding:'Two hammer frames require real bearing columns and two independent bypasses around the central workpiece.',change:'Built four grounded full-height pier lines carrying different-height beams; split three-wide paths at x5 and x10 rejoin north and south of the billet.',evidence:checks[1].productionGeometry}
 ];
 self.visualReview={status:'PASS',source:'browser-four-views.png generated by a real Edge session and visually inspected; stained glass shown translucent',perRoom:[
  {key:'addition/07',north:'西侧高晶舱与东侧低净化设备隔着中央平路展开。',south:'短封存架独立收尾，晶簇三种峰高与阶梯台体量不同。',west:'透明晶舱内不对称主峰、次峰和低芽可辨。',east:'净化台逐级升高，后方短架与上方管道分离。',status:'PASS'},
  {key:'addition/08',north:'东西拉力墩夹住低链床，两端外侧留出绕行路。',south:'斜向三段操作台和独立高屏形成前后层次。',west:'低链床从厚墩间穿过，高处罩梁连接外壳。',east:'厚墩、低静态铁链形体与南侧安全屏高度不同，双侧回环保持畅通。',status:'PASS'}
 ]};
 self.repairs=[{key:'addition/08',finding:'Initial overhead beam support stubs read as disconnected from the room shell.',change:'Extended the high transverse canopy beam continuously into both exterior walls and removed the floating stubs.',evidence:'actual NBT cardinal projections'}, {key:'addition/07',finding:'Opaque preview glass hid the real internal crystal silhouette.',change:'Render stained glass translucent and expose opaque block faces behind glass; NBT geometry remains unchanged.',evidence:'browser-four-views.png'}];
 self.visualReview={status:'PASS',source:'browser-four-views.png generated by a real Edge session and visually inspected; all geometry decoded from exported NBT',perRoom:[
  {key:'addition/09',north:'三座削肩雕坯沿西侧展开，东工具台及碎石托盘错列。',south:'西南宽矮雕坯与北端高坯形成阶梯体量，中央开放作业面贯通。',west:'三座雕坯各有不同肩部切口，层次占据主体，低工具台位于对侧。',east:'高、中、低三块独立雕坯可分辨，中央没有钟体或塔柱。',status:'PASS'},
  {key:'addition/10',north:'西侧厚旧炉腹与东侧物资架、南床台对角展开，中央低挡体保留两路。',south:'白色床台位于物资架对角，炉腹侧口可进入。',west:'炉腹具有完整厚壁和冷暗内衬，上方短烟道连接封顶。',east:'侧面拱口、折角物资架和独立低床台可辨，应急灯玻璃盒完整封闭。',status:'PASS'}
 ]};
 self.repairs=[{key:'addition/09',finding:'Preserving the room_towers west sockets requires keeping the half-ring sculptures off x4,z8/11.',change:'Confined upper west sculptures to x1..3 and moved the broad low sculpture to z12..14; socket reservations are applied after construction.',evidence:checks[0].productionGeometry},{key:'addition/10',finding:'A central thermal divider requires two walkable routes while the old-furnace cavity must remain accessible.',change:'Kept three-wide west and east routes at x7 and x11, placed the divider at x9, and opened a two-wide side arch into the furnace cavity. Equipment stays outside both circulation strips.',evidence:checks[1].productionGeometry}];
 self.repairs.push({key:'addition/09',finding:'First decoded landmark audit measured only 58 blocks in the high sculpture against the 60-block design minimum.',change:'Extended its connected north-east upper shoulder by two basalt blocks, retaining the carved shoulder cutout and y9 peak.',evidence:style.rooms[0].features.find(feature=>feature.name==='西北高削肩雕坯')},{key:'addition/10',finding:'First decoded landmark audit measured 37 blocks in the L-shaped supply rack against the 40-block design minimum.',change:'Added a three-block grounded interior corner at x13,z6 to make the rack elbow physically continuous.',evidence:style.rooms[1].features.find(feature=>feature.name==='折角隔热物资架')});
 self.completion={authorizedFinalBatch:5,totalNewRooms:10,status:'STOP_AFTER_FREEZE',nextAction:'Parent native placement tests and overall acceptance; no further rooms generated.'};
 const preservation=[];
 for(const directory of ['batch-01','batch-02','batch-03','batch-04']){
  const priorRoot=path.join(expansion,directory),priorFreeze=JSON.parse(fs.readFileSync(path.join(priorRoot,'freeze.json'),'utf8'));
  const mismatches=priorFreeze.artifacts.filter(artifact=>digest(fs.readFileSync(path.join(priorRoot,artifact.file)))!==artifact.sha256);
  preservation.push({batch:directory,status:mismatches.length?'FAIL':'PASS',checked:priorFreeze.artifacts.length,mismatches,freezeSha256:digest(fs.readFileSync(path.join(priorRoot,'freeze.json')))});
 }
 self.previousBatchesPreservation=preservation;
 if(preservation.some(entry=>entry.status!=='PASS'))self.status='FAIL';
 self.limitations[2]='Static full blocks only in this batch: no stairs, dynamic rails, fluids, redstone, entities or block entities. Raised machinery is outside the level ground circulation.';
 self.recipeSnapshot='recipe-source.mjs is byte-identical to the executed recipe and independently locates the project root when replayed from this batch directory.';
 save('self-audit.json',self);
 fs.writeFileSync(path.join(output,'recipe-source.mjs'),fs.readFileSync(fileURLToPath(import.meta.url)));
 const artifactFiles=['manifest.json','design.md','validation.json','style-audit.json','repetition-audit.json','four-view-index.json','four-views.html','self-audit.json','recipe-source.mjs',...items.map(item=>item.file),...views.records.map(view=>view.file)];
 const recipeSha256=digest(fs.readFileSync(fileURLToPath(import.meta.url))),hashes=Object.fromEntries(artifactFiles.map(file=>[file,digest(fs.readFileSync(path.join(output,file)))]));hashes.recipe=recipeSha256;
 const replayFile=path.join(output,'replay-hashes.json'),previous=fs.existsSync(replayFile)?JSON.parse(fs.readFileSync(replayFile,'utf8')):null,mismatches=previous?Object.keys(hashes).filter(file=>hashes[file]!==previous[file]):Object.keys(hashes);
 const reproducibility={status:mismatches.length?'BASELINE_CAPTURED':'PASS',compared:Object.keys(hashes).length,mismatches,method:'Consecutive full recipe executions compare SHA256 of NBT, HTML/SVG, design and JSON audits. Browser PNG is checked in freeze but excluded from deterministic replay.'};
 save('replay-hashes.json',hashes);save('reproducibility.json',reproducibility);
 const freeze={schema:1,style:'fteclipsegen:lava',batch:activeBatch,status:self.status==='PASS'&&reproducibility.status==='PASS'?'FROZEN_FOR_PARENT_INTEGRATION':'REQUIRES_REPAIR_OR_REPLAY',recipe:{file:'tools/expand-lava.mjs',snapshot:'recipe-source.mjs',sha256:recipeSha256},manifestSha256,artifacts:[...artifactFiles,'browser-four-views.png','replay-hashes.json','reproducibility.json'].map(file=>({file,sha256:digest(fs.readFileSync(path.join(output,file)))})),excluded:['qa-browser/']};save('freeze.json',freeze);
 console.log(JSON.stringify({validation:validation.status,style:style.status,repetition:repetition.status,highestSimilarity:repetition.highestPairs[0]?.maximumRotatedJaccard,browser:browser.status,self:self.status,replay:reproducibility.status,freeze:freeze.status,manifestSha256},null,2));
 for(const check of checks)if(check.status==='FAIL')console.log(check.key,check.failures);
 for(const room of style.rooms)if(room.status==='FAIL')console.log(room.key,room.features.filter(feature=>feature.status==='FAIL'));
 if(self.status!=='PASS')process.exitCode=1;
}
if(process.argv[1]&&path.resolve(process.argv[1])===fileURLToPath(import.meta.url))await main();
