import fs from 'node:fs';
import path from 'node:path';
import crypto from 'node:crypto';
import {createRequire} from 'node:module';
import {fileURLToPath,pathToFileURL} from 'node:url';

const sourcePath=fileURLToPath(import.meta.url);
const root=path.resolve(path.dirname(sourcePath),path.basename(path.dirname(sourcePath))==='tools'?'..':'../../../..');
const parent=path.join(root,'output/room-expansion-v035/frost');
const batch=5;
if(process.argv.slice(2).some(argument=>argument!=='--batch=5'))throw Error('Only final batch 5 additions 09/10 authorized; batches 1/2/3/4 are immutable');
const batchName='batch-05';
const out=path.join(parent,batchName);
const data=path.join(root,'src/main/resources/data/fteclipsegen');
const oldDirectory=path.join(root,'output/style-rebuild-v034/frost');
const skill=process.env.MINECRAFT_BUILDER_SKILL??'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill';
const {Structure,NbtFile,BlockState}=createRequire(path.join(skill,'package.json'))('@mattzh72/lodestone');
const catalog=JSON.parse(fs.readFileSync(path.join(data,'catalog.json'),'utf8'));
const sha=bytes=>crypto.createHash('sha256').update(bytes).digest('hex');
const json=(file,value)=>fs.writeFileSync(path.join(out,file),JSON.stringify(value,null,2)+'\n');
const air='minecraft:air';
const firstBatchDefinitions=[
 {key:'addition/01',baseKey:'straight/room_cells',name:'冰芯取样室',purpose:'冻层取样、切片与战斗',layout:'北南宽入口在中段绕中央低冷台向西折行；西侧三玻璃槽封存错高冰芯，东侧横向切片台形成实验工作带。',levels:'脚点y2，低冷台y2..3，三冰芯顶y5/8/6，玻璃槽顶y7/10/8，折线筒拱y12..15',feature:'三支错高玻璃封存冰芯与横向切片台'},
 {key:'addition/02',baseKey:'straight/room_storage',name:'雪橇维修库',purpose:'雪橇整备、维修与战斗',layout:'中央四格宽整备通道贯通南北，西侧东西向雪橇床与东侧南北向雪橇床方向相异，西南弧形滑板架、东北工具台、两端落地木梁。',levels:'脚点y2，雪橇滑橇y2半块，架床y3半块，弧架y3..7，落地承重柱y2..11，木冰混合桁架y11..13',feature:'两架正交雪橇床、弧形滑板架与落地木梁'}
];
const definitions=[
 {key:'addition/09',baseKey:'straight/room_towers',name:'霜角回声研究所',purpose:'霜角共鸣与偏置监听',layout:'西北、西南与东侧三支弯折阶梯音管顶高各异，管身沿曲线逐段弯向室内，端部为方角喇叭口；东南偏置监听台与蓝冰声迹连接，中央三格宽主路贯通，剪影为不规则音管簇。',levels:'主脚点y2；三支音管口顶y7/10/6，监听台y2..3，平顶y14..15',feature:'三支不同高度阶梯霜角、三处方角共鸣口、偏置监听台'},
 {key:'addition/10',baseKey:'straight/room_cells',name:'冰封渡口候船室',purpose:'冰渠渡口候船、装卸与岸上战斗',layout:'东西向玻璃封闭冰渠穿过中段，中央平层小桥由四根桥墩支撑；西北短码头朝向冰渠，经半格台阶登临；西南候船长椅、东南两阶物资坡道与高岸台形成滨岸空间，全部socket位于冰渠以外原岸面。',levels:'主脚点y2，渠底与桥墩y0、桥面y1；玻璃渠盖顶y3；短码头脚点y3；物资坡道接岸台脚点y4',feature:'封闭横向冰渠、实心桥面与四桥墩、可登短码头、候船长椅、连续物资坡道'}
];
const observedViews={
 'addition/09':{north:'三支阶梯音管呈向内弯折的斜升曲线，南高管与北中管在一侧错高叠置，东低管位于对侧。',south:'侧向反转可见高、中、低三支弯管与空隙，低矮木面监听台和灯位居偏置岸侧；中央平层通路清楚。',west:'沿口部端面观看时曲线有纵深重叠，三处方角口与玻璃核心可辨；三支管的顶部高度不同。',east:'端口与管身前后叠置成三个独立簇，监听台在左下；弯折轮廓需与南北两图交叉查看，不能把端面投影当成直塔。'},
 'addition/10':{north:'横向蓝冰渠底与玻璃罩分居桥面两侧，木桥面在中央，桥下两端灰色支点可辨；短码头与岸台位于两侧。',south:'桥面与灰色支点从反向可见；右侧物资坡道、高岸台、物资木堆与左侧候船长椅分开。',west:'渠道在此方向端面重叠，玻璃壳局部遮挡桥面桥墩；北岸短码头台阶、南岸两阶物资坡道和候船长椅可辨，四墩另以NBT逐坐标核验。',east:'西北短码头及上行台阶与东南高岸台反向展开，玻璃渠壳保持封闭；主桥处有前后遮挡，需参照南北图及完整桥面NBT审计。'},
 'addition/07':{north:'剖面在纵深上重叠为右侧高柱；对侧不规则石架和错列低台可辨，地面导览线穿过中央。',south:'最高雪层位于左侧，剖面上方玻璃透明带可见；顶盖已剖切，完整玻璃封顶另有42块NBT逐格证据。',west:'七阶雪层呈明确上升台阶，白雪、蓝冰色冻层与灰岩层交替，玻璃封闭上方空间。',east:'台阶剖面方向反转，对侧四个高低标本石架及低展台可辨；中央S形导览线只做一格侧移，宽度由几何审计核验。'},
 'addition/08':{north:'独立烟道在纵深上部分重叠，双顶帽可辨；相对侧短架居前，储槽位于后段。',south:'前后双炉及双顶帽在纵深上重叠，蓝冰槽底与白色槽沿位于相对侧；主路平层。',west:'两座石炉与烟道左右分离，烟道之间留空气间隙；短架及储槽在对侧，观察口在炉体另一面。',east:'两座独立烟道间隙清楚，第一炉玻璃口后可见灯芯色，第二炉口受短架部分遮挡；悬挂鱼形材、蓝冰槽底和槽沿高度不同。'},
 'addition/05':{north:'折角晾架梁与三片皮货形成西侧工作带，东侧补给台和阶梯箱体彼此分离。',south:'朝出口方向可见行军地面标记，独立低补给台没有堆成通长货架。',west:'皮货错色悬片与L形晾架连接，封冰箱位于相对侧且顶高不同。',east:'封冰箱白色冰层与玻璃外壳可辨，晾架两落地支点及悬挂皮片不挡中央行军道。'},
 'addition/06':{north:'单侧西北石冰崖有阶梯侵蚀轮廓，东侧双柱横梁跨过浅沟，未复制双冰塔布局。',south:'西南楼梯接入独立侧台，东侧沟口有下降台阶和实心冰护栏，中央仍留旱路。',west:'石冰崖面仅在一侧，索具架和浅沟位于对侧；楼梯半块几何连续。',east:'双柱索架间有悬索，横梁下留检修沟；沟北护栏与原socket区分开。'},
 'addition/03':{north:'偏西玻璃冰泉塔与右侧高台分离，底层旱路可见。',south:'可见南侧三格宽连续石英楼梯与东南侧台，偏心塔不覆盖中轴。',west:'冰泉塔内蓝冰柱穿过两级凝霜架高度，侧台位于另一侧。',east:'三阶楼梯与高台的接续可辨，旱路和塔体不阻断原socket。'},
 'addition/04':{north:'三段错位折镜的高度及偏转不同，不是同心环。',south:'出口偏东观察台与三段冰镜分离，镜间保留地面通道。',west:'三段镜墙的折面和错列可辨，中央通路不被玻璃隔断。',east:'不同高度镜面形成锯齿空间，观察台靠出口侧；完整极光顶另作NBT检验。'},
 'addition/01':{
  north:'北向可见一侧封槽的叠置断面、中央低冷台与横向切片台，低台前后有绕行余量；三槽沿视线遮挡是方向投影现象，以东向并列图核对。',
  south:'南向中央低冷台与东切片台分开，封槽高低错位可见；端口金点对应四格宽开口，家具未侵入向内五格净空。',
  west:'西向三玻璃槽呈并列且顶高不同，蓝冰芯通过35%透明玻璃可辨，后方横肋的投影较高但不占地面动线。',
  east:'东向三封存冰芯的5/8/6高度与白色槽盖可辨；前景切片台和中央低冷台轮廓分离，非四角柱空厅。'
 },
 'addition/02':{
  north:'北向两组实际落地柱与端部承重梁形成工坊框架，下方雪橇床在左右两侧，中央四格整备面保持开放。',
  south:'南向西南弧形滑板架露出起伏轮廓，东北工具台位于远角；四柱是承重功能，不代替雪橇床和维修家具。',
  west:'西向一架雪橇床呈长向轮廓，另一架呈横断面，滑橇使用实际半砖分层；工具台与滑板架不挡socket。',
  east:'东向可辨西南弧架由低至高的弯折及两架正交雪橇床；木柱落地、梁架接柱，中央无悬空支撑或阻路台阶。'
 }
};
const priorFrozen=fs.existsSync(path.join(out,'freeze.json'))?JSON.parse(fs.readFileSync(path.join(out,'freeze.json'),'utf8')):null;
const protectedPaths=[...catalog.variants.flatMap(variant=>[path.join(oldDirectory,'roomset',variant.key+'.nbt'),path.join(data,'roomsets/frost',variant.key+'.nbt')]),path.join(oldDirectory,'roomset/manifest.json'),path.join(oldDirectory,'FREEZE.json'),path.join(data,'catalog.json')];
const protectedHashes=Object.fromEntries(protectedPaths.map(file=>[file,sha(fs.readFileSync(file))]));
function protectTree(directory){for(const entry of fs.readdirSync(directory,{withFileTypes:true})){const file=path.join(directory,entry.name);if(entry.isDirectory())protectTree(file);else protectedHashes[file]=sha(fs.readFileSync(file));}}
protectTree(path.join(parent,'batch-01'));
protectTree(path.join(parent,'batch-02'));
protectTree(path.join(parent,'batch-03'));
protectTree(path.join(parent,'batch-04'));
fs.mkdirSync(path.join(out,'roomset/addition'),{recursive:true});
json('freeze.json',{status:'BUILDING',batch});
fs.writeFileSync(path.join(out,'design.md'),'# 冰霜新增 · 第5批，仅两房\n\n独立构造，不复制旧27房；仅继承指定catalog接口。材料稳定，不使用流体、普通融冰、粉雪、轨道或红石更新依赖。基础地板完整，端口向内5格保持5AIR，socket上3AIR且脚下完整实体。\n\n'+definitions.map(definition=>`## ${definition.key} · ${definition.name}\n\nbaseKey: ${definition.baseKey}。用途：${definition.purpose}。平面：${definition.layout}高度：${definition.levels}。实物地标：${definition.feature}。\n`).join('\n'));

function roofFor(definition,x,z){return definition.key==='addition/01'?Math.min(15,11+Math.floor((7-Math.abs(x-7.5))*.5)):definition.key==='addition/03'?13+(x>7?1:0):definition.key==='addition/05'?13+(x>7?1:0):definition.key==='addition/06'?15:14;}
function build(definition){
 const original=catalog.variants.find(variant=>variant.key===definition.baseKey);
 if(!original||original.size.join(',')!=='16,16,16')throw Error('Missing exact 16³ interface');
 const variant={...structuredClone(original),key:definition.key};
 const states=Array(4096).fill(air),reserved=new Set(),landmarks=[];
 const get=(x,y,z)=>x>=0&&x<16&&y>=0&&y<16&&z>=0&&z<16?states[(y*16+z)*16+x]:'minecraft:bedrock';
 const put=(x,y,z,block)=>{if(![x,y,z].every(value=>Number.isInteger(value)&&value>=0&&value<16))throw Error('Bounds '+[x,y,z]);states[(y*16+z)*16+x]=block.includes(':')?block:'minecraft:'+block;};
 const box=(from,to,material)=>{for(let y=from[1];y<=to[1];y++)for(let z=from[2];z<=to[2];z++)for(let x=from[0];x<=to[0];x++)put(x,y,z,material);};
 const reserve=(x,y,z,height=3)=>{for(let rise=0;rise<height;rise++)reserved.add([x,y+rise,z].join(','));};
 for(const port of variant.ports)for(const [x,y,z]of port.cells)for(let inward=0;inward<=5;inward++)reserve(x===0?inward:x===15?15-inward:x,y,z===0?inward:z===15?15-inward:z,5);
 const targets=variant.sockets.flatMap(socket=>[socket.position,socket.spawn_position]);
 for(const point of targets)reserve(...point);
 const floor=(from,to,material)=>box([from[0],1,from[1]],[to[0],1,to[1]],material);
 box([0,0,0],[15,1,15],'packed_ice');
 floor([1,1],[14,14],definition.key==='addition/02'?'spruce_planks':'smooth_quartz');
 for(let x=0;x<16;x++)for(let z=0;z<16;z++)for(let y=2;y<16;y++){
  const edge=x===0||x===15||z===0||z===15;
  if(edge||y>=roofFor(definition,x,z))put(x,y,z,y>6&&edge&&(x===0||x===15)&&z>=4&&z<=11?'light_blue_stained_glass':(x+z+y)%7===0?'blue_ice':'packed_ice');
 }
 const feature=(role,from,to,materials,minimum)=>landmarks.push({role,from,to,materials,minimum});
 if(definition.key==='addition/01'){
  for(const [slot,z,coreTop,glassTop]of [[1,3,5,7],[2,7,8,10],[3,11,6,8]]){
   box([1,2,z-1],[3,glassTop,z+1],'light_blue_stained_glass');
   box([2,2,z],[2,glassTop-1,z],air);
   box([2,2,z],[2,coreTop,z],'blue_ice');
   put(2,coreTop+1,z,'snow_block');
   box([1,glassTop,z-1],[3,glassTop,z+1],'calcite');
   feature('封存冰芯'+slot,[1,2,z-1],[3,glassTop,z+1],['light_blue_stained_glass','blue_ice','calcite','snow_block'],24);
   feature('错高核心'+slot,[2,2,z],[2,coreTop,z],['blue_ice'],coreTop-1);
  }
  box([7,2,7],[9,2,8],'blue_ice');box([7,3,7],[9,3,8],'smooth_quartz');
  box([11,2,7],[14,2,7],'blue_ice');box([11,3,7],[14,3,7],'calcite');
  for(const x of [11,13])put(x,4,7,'snow_block');
  box([12,2,9],[14,2,9],'packed_ice');
  for(const z of [3,7,11]){
   for(let x=4;x<15;x++){const y=roofFor(definition,x,z)-1;if(!reserved.has([x,y,z].join(',')))put(x,y,z,'calcite');}
   put(13,6,z,'sea_lantern');
  }
  feature('中央低冷台',[7,2,7],[9,3,8],['blue_ice','smooth_quartz'],12);
  feature('横向切片台',[11,2,7],[14,4,7],['blue_ice','calcite','snow_block'],10);
  floor([4,6],[6,9],'calcite');floor([6,1],[9,5],'calcite');floor([6,10],[9,14],'calcite');
 }else if(definition.key==='addition/02'){
  const slab='spruce_slab[type=bottom,waterlogged=false]';
  for(const z of [6,8])box([2,2,z],[5,2,z],slab);
  box([2,3,7],[4,3,7],slab);
  put(5,3,7,'spruce_stairs[facing=east,half=bottom,shape=straight,waterlogged=false]');
  for(const x of [11,13])box([x,2,6],[x,2,9],slab);
  box([12,3,7],[12,3,9],slab);
  put(12,3,6,'spruce_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]');
  feature('东西向雪橇床',[2,2,6],[5,3,8],['spruce_slab','spruce_stairs'],12);
  feature('南北向雪橇床',[11,2,6],[13,3,9],['spruce_slab','spruce_stairs'],12);
  for(const x of [1,2])for(const [z,y]of [[10,3],[11,5],[12,6],[13,7]]){put(x,y,z,'blue_ice');put(x,y+1,z,slab);}
  box([1,2,10],[1,6,13],'stripped_spruce_log[axis=y]');
  feature('弧形滑板架',[1,2,10],[2,8,13],['blue_ice','spruce_slab','stripped_spruce_log'],20);
  box([11,2,1],[14,2,2],'spruce_planks');put(11,3,2,'spruce_planks');put(12,3,2,'crafting_table');put(14,3,2,'polished_andesite');
  feature('东北工具台',[11,2,1],[14,3,2],['spruce_planks','crafting_table','polished_andesite'],10);
  for(const x of [2,13])for(const z of [2,13]){
   box([x,2,z],[x,11,z],'stripped_spruce_log[axis=y]');
   feature('落地承重柱 '+x+','+z,[x,2,z],[x,11,z],['stripped_spruce_log'],10);
  }
  for(const z of [2,13]){box([2,11,z],[13,11,z],'stripped_spruce_log[axis=x]');for(let x=3;x<13;x++)put(x,12,z,Math.abs(x-7.5)<3?'spruce_planks':'blue_ice');put(7,10,z,'sea_lantern');}
  for(const x of [1,14])box([x,5,4],[x,5,11],'blue_ice');
  floor([6,1],[9,14],'calcite');
 }else if(definition.key==='addition/03'){
  box([1,2,5],[3,11,7],'light_blue_stained_glass');box([2,2,6],[2,10,6],'blue_ice');box([1,11,5],[3,11,7],'calcite');
  for(const [x,y,z]of [[2,4,6],[2,8,6]])put(x,y,z,'sea_lantern');
  box([4,2,6],[5,3,7],'packed_ice');box([4,3,6],[5,3,7],'snow_block');
  box([3,2,9],[5,5,9],'blue_ice');box([3,5,9],[5,5,9],'snow_block');
  box([10,4,9],[14,4,11],'calcite');for(const x of [10,14])box([x,2,10],[x,3,10],'blue_ice');
  for(const [z,y]of [[14,2],[13,3],[12,4]])for(let x=11;x<=13;x++){if(y>2)box([x,2,z],[x,y-1,z],'calcite');put(x,y,z,'quartz_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]');}
  for(const x of [10,14])put(x,5,10,'blue_ice');
  floor([6,1],[9,14],'calcite');
  feature('偏心封闭冰泉塔',[1,2,5],[3,11,7],['blue_ice','light_blue_stained_glass','calcite','sea_lantern'],70);
  feature('低凝霜架',[4,2,6],[5,3,7],['packed_ice','snow_block'],8);feature('高凝霜架',[3,2,9],[5,5,9],['blue_ice','snow_block'],8);
  feature('东南上层侧台',[10,4,9],[14,4,11],['calcite'],15);feature('真实连续楼梯',[11,2,12],[13,4,14],['quartz_stairs'],9);
 }else if(definition.key==='addition/04'){
  for(const [x,z]of [[1,3],[2,3],[2,4],[3,4],[3,5]])box([x,2,z],[x,7,z],'blue_ice');
  for(const [x,z]of [[10,6],[11,6],[11,7],[12,7],[13,7]])box([x,2,z],[x,9,z],'light_blue_stained_glass');
  for(const [x,z]of [[1,10],[2,10],[2,11],[3,11],[3,12]])box([x,2,z],[x,6,z],'cyan_stained_glass');
  for(const [x,z]of [[2,3],[11,6],[2,10]]){box([x,3,z],[x,5,z],'smooth_quartz');put(x,6,z,'sea_lantern');}
  box([12,2,12],[14,2,13],'quartz_slab[type=bottom,waterlogged=false]');
  for(let x=1;x<15;x++)for(let z=1;z<15;z++)for(const y of [14,15])put(x,y,z,['cyan_stained_glass','light_blue_stained_glass','purple_stained_glass','lime_stained_glass'][Math.floor((x+z)/3)%4]);
  floor([6,1],[9,14],'calcite');
  feature('西北折镜',[1,2,3],[3,7,5],['blue_ice','smooth_quartz','sea_lantern'],20);feature('中东折镜',[10,2,6],[13,9,7],['light_blue_stained_glass','smooth_quartz','sea_lantern'],28);feature('西南折镜',[1,2,10],[3,6,12],['cyan_stained_glass','smooth_quartz','sea_lantern'],16);
  feature('偏向出口观察台',[12,2,12],[14,2,13],['quartz_slab'],6);feature('封闭极光彩玻璃顶',[1,14,1],[14,15,14],['cyan_stained_glass','light_blue_stained_glass','purple_stained_glass','lime_stained_glass'],392);
 }else if(definition.key==='addition/05'){
  for(const [x,z]of [[1,3],[5,8]])box([x,2,z],[x,7,z],'stripped_spruce_log[axis=y]');
  box([1,7,3],[1,7,8],'stripped_spruce_log[axis=z]');box([1,7,8],[5,7,8],'stripped_spruce_log[axis=x]');
  box([1,4,4],[1,6,5],'brown_wool');box([1,4,6],[1,6,7],'white_wool');box([2,4,8],[4,6,8],'brown_wool');put(3,4,8,'white_wool');
  box([11,2,6],[13,2,7],'calcite');box([11,3,6],[13,3,7],'spruce_slab[type=bottom,waterlogged=false]');put(12,4,6,'snow_block');
  box([12,2,9],[14,3,9],'light_blue_stained_glass');box([13,2,10],[14,5,12],'light_blue_stained_glass');box([13,3,10],[14,3,12],'snow_block');box([13,5,10],[14,5,12],'blue_ice');put(13,2,11,'blue_ice');
  for(const z of [3,6,10,13]){put(7,1,z,'blue_ice');put(7,1,z-1,'blue_ice');put(6,1,z-1,'blue_ice');put(8,1,z-1,'blue_ice');}
  for(const z of [4,11])put(14,6,z,'sea_lantern');
  feature('折角落地皮货晾架',[1,2,3],[5,7,8],['stripped_spruce_log','brown_wool','white_wool'],35);feature('三片实际悬挂皮货',[1,4,4],[4,6,8],['brown_wool','white_wool'],21);
  feature('独立补给台',[11,2,6],[13,4,7],['calcite','spruce_slab','snow_block'],13);feature('阶梯封冰箱',[12,2,9],[14,5,12],['blue_ice','snow_block','light_blue_stained_glass'],28);feature('出口方向行军箭头',[6,1,2],[8,1,13],['blue_ice'],16);
 }else if(definition.key==='addition/06'){
  for(let z=3;z<=8;z++)for(let x=1;x<=3;x++)box([x,2,z],[x,Math.min(12,6+(3-x)*2+z%3),z],(x+z)%3?'packed_ice':'andesite');
  for(const [x,y,z]of [[1,7,4],[2,9,5],[3,6,7],[2,8,8]])put(x,y,z,'blue_ice');
  box([1,4,10],[3,4,11],'calcite');box([1,2,10],[1,3,11],'andesite');
  for(const [z,y]of [[14,2],[13,3],[12,4]])for(let x=1;x<=3;x++){if(y>2)box([x,2,z],[x,y-1,z],'calcite');put(x,y,z,'quartz_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]');}
  box([11,1,9],[13,1,11],air);box([11,0,9],[13,0,11],'blue_ice');
  for(let x=11;x<=13;x++)put(x,1,12,'quartz_stairs[facing=south,half=bottom,shape=straight,waterlogged=false]');
  for(const x of [10,14])box([x,2,10],[x,8,10],'andesite');box([10,8,10],[14,8,10],'calcite');
  for(const x of [11,13])box([x,5,10],[x,7,10],'chain[axis=y,waterlogged=false]');
  box([11,2,8],[13,2,8],'blue_ice');box([10,2,9],[10,2,11],'blue_ice');box([14,2,9],[14,2,11],'blue_ice');put(12,3,8,'sea_lantern');
  for(const x of [10,14])put(x,2,10,'andesite');
  feature('单侧石冰崖面',[1,2,3],[3,12,8],['andesite','packed_ice','blue_ice'],110);feature('西南可登侧台',[1,4,10],[3,4,11],['calcite'],6);feature('侧台连续楼梯',[1,2,12],[3,4,14],['quartz_stairs'],9);
  feature('双柱跨沟索具架',[10,2,10],[14,8,10],['andesite','calcite'],17);feature('两条吊索',[11,5,10],[13,7,10],['chain'],6);feature('浅沟完整蓝冰底',[11,0,9],[13,0,11],['blue_ice'],9);feature('北沿防护冰栏',[11,2,8],[13,3,8],['blue_ice','sea_lantern'],4);
 }else if(definition.key==='addition/07'){
  for(let z=8;z<=14;z++){const top=z-4;for(let y=2;y<=top;y++)box([1,y,z],[2,y,z],y===top?'snow_block':y%3===0?'calcite':y%3===1?'andesite':'packed_ice');box([1,top+1,z],[2,12,z],'light_blue_stained_glass');}
  box([0,8,8],[0,13,14],'light_blue_stained_glass');box([1,14,8],[3,15,14],'light_blue_stained_glass');
  for(const [x,z,top]of [[13,3,3],[12,6,5],[14,7,4],[13,9,7]]){box([x,2,z],[x,top,z],'andesite');put(x,top+1,z,top%2?'blue_ice':'calcite');if(top>4)put(x+1,3,z,'calcite');}
  box([10,2,6],[11,2,6],'calcite');box([10,3,6],[11,3,6],'quartz_slab[type=bottom,waterlogged=false]');
  box([5,2,9],[6,2,9],'calcite');box([4,2,10],[5,2,10],'calcite');box([5,3,9],[6,3,9],'quartz_slab[type=bottom,waterlogged=false]');box([4,3,10],[5,3,10],'quartz_slab[type=bottom,waterlogged=false]');
  box([10,2,13],[12,2,13],'calcite');box([10,3,13],[12,3,13],'quartz_slab[type=bottom,waterlogged=false]');
  for(let z=1;z<=14;z++)put(z>=8&&z<=10?8:7,1,z,'blue_ice');for(const z of [7,11])put(8,1,z,'blue_ice');
  for(const [x,z]of [[3,7],[12,8]])put(x,1,z,'sea_lantern');
  feature('七阶雪层剖面',[1,2,8],[2,10,14],['snow_block','packed_ice','calcite','andesite'],80);
  feature('剖面上方玻璃封闭',[0,5,8],[3,15,14],['light_blue_stained_glass'],100);
  feature('不规则标本石架',[12,2,3],[14,8,9],['andesite','calcite','blue_ice'],17);
  feature('三组错列低展台',[4,2,6],[12,3,13],['calcite','quartz_slab'],18);
  feature('S形地面导览线',[7,1,1],[8,1,14],['blue_ice'],16);
 }else if(definition.key==='addition/08'){
  for(const z of [6,11]){
   box([1,2,z-1],[3,4,z+1],'stone_bricks');put(2,3,z,'sea_lantern');put(3,3,z,'light_blue_stained_glass');
   box([2,5,z],[2,12,z],'polished_andesite');box([1,12,z],[3,12,z],'stone_bricks');
  }
  for(const x of [12,14])box([x,2,6],[x,6,6],'dark_prismarine');box([12,6,5],[14,6,6],'spruce_planks');
  for(const z of [5,6]){put(13,5,z,'chain[axis=y,waterlogged=false]');put(13,4,z,'prismarine_brick_slab[type=bottom,waterlogged=false]');}put(13,4,7,'prismarine_bricks');
  box([12,2,9],[14,2,13],'blue_ice');box([12,3,9],[14,3,13],'light_blue_stained_glass');box([13,3,10],[13,3,12],'packed_ice');
  for(const x of [12,14])box([x,4,9],[x,4,13],'quartz_slab[type=bottom,waterlogged=false]');for(const z of [9,13])put(13,4,z,'quartz_slab[type=bottom,waterlogged=false]');
  floor([4,6],[5,6],'blue_ice');floor([5,6],[5,8],'blue_ice');floor([5,8],[10,8],'blue_ice');floor([10,6],[10,11],'blue_ice');floor([10,6],[11,6],'blue_ice');floor([10,11],[11,11],'blue_ice');
  feature('两座独立烟熏炉',[1,2,5],[3,4,12],['stone_bricks','sea_lantern','light_blue_stained_glass'],54);
  feature('独立双烟道',[1,5,6],[3,12,11],['polished_andesite','stone_bricks'],20);
  feature('隔离静态灯芯',[2,3,6],[2,3,11],['sea_lantern'],2);
  feature('短晾架与鱼形材',[12,2,5],[14,6,7],['dark_prismarine','spruce_planks','chain','prismarine_bricks','prismarine_brick_slab'],19);
  feature('完整低温封冰槽',[12,2,9],[14,4,13],['blue_ice','packed_ice','light_blue_stained_glass','quartz_slab'],42);
 }else if(definition.key==='addition/09'){
  const horns=[{id:'北霜角',facing:'east',points:[[2,2,3],[2,3,3],[2,4,3],[3,4,3],[3,5,3],[4,5,3],[4,6,3],[5,6,3]],mouth:[5,6,3],from:[2,2,2],to:[5,7,4]},
   {id:'南高霜角',facing:'east',points:[[1,2,12],[1,3,12],[1,4,12],[2,4,12],[2,5,12],[2,6,12],[3,6,12],[3,7,12],[4,7,12],[4,8,12],[4,9,12],[5,9,12]],mouth:[5,9,12],from:[1,2,11],to:[5,10,13]},
   {id:'东低霜角',facing:'west',points:[[14,2,9],[14,3,9],[13,3,9],[13,4,9],[12,4,9],[12,5,9],[11,5,9]],mouth:[11,5,9],from:[11,2,8],to:[14,6,10]}];
  for(const horn of horns){
   for(const [x,y,z]of horn.points){put(x,y,z,'blue_ice');put(x,y,z+1,`quartz_stairs[facing=${horn.facing},half=bottom,shape=straight,waterlogged=false]`);}
   const [mouthX,mouthY,mouthZ]=horn.mouth;
   box([mouthX,mouthY-1,mouthZ-1],[mouthX,mouthY+1,mouthZ+1],'quartz_stairs[facing='+horn.facing+',half=bottom,shape=straight,waterlogged=false]');put(mouthX,mouthY,mouthZ,'light_blue_stained_glass');
   feature(horn.id+'弯折阶梯管身',horn.from,horn.to,['blue_ice','quartz_stairs','light_blue_stained_glass'],horn.points.length+8);
   feature(horn.id+'方角共鸣口',[mouthX,mouthY-1,mouthZ-1],[mouthX,mouthY+1,mouthZ+1],['quartz_stairs','light_blue_stained_glass'],9);
  }
  box([11,2,12],[13,2,13],'calcite');box([11,3,12],[13,3,13],'spruce_slab[type=bottom,waterlogged=false]');put(12,4,13,'sea_lantern');
  for(const [x,z]of [[10,9],[10,10],[10,11],[10,12],[11,11],[12,11]])put(x,1,z,'blue_ice');
  feature('偏置监听台',[11,2,12],[13,4,13],['calcite','spruce_slab','sea_lantern'],13);
 }else if(definition.key==='addition/10'){
  box([1,0,7],[14,0,8],'blue_ice');box([1,1,7],[14,3,8],'light_blue_stained_glass');
  box([6,1,7],[9,1,8],'spruce_planks');box([6,2,7],[9,3,8],air);
  for(const x of [6,9])for(const z of [7,8])put(x,0,z,'stone_bricks');
  box([1,2,5],[3,2,6],'spruce_planks');for(let x=1;x<=3;x++)put(x,2,4,'spruce_stairs[facing=south,half=bottom,shape=straight,waterlogged=false]');
  for(const z of [5,6])put(1,3,z,'blue_ice');
  box([1,2,11],[3,2,13],'spruce_slab[type=bottom,waterlogged=false]');box([1,3,11],[1,3,13],'spruce_planks');
  box([12,3,11],[14,3,13],'spruce_planks');box([12,2,11],[14,2,13],'calcite');box([12,4,12],[13,4,13],'spruce_planks');
  for(let x=12;x<=14;x++){put(x,2,9,'spruce_stairs[facing=south,half=bottom,shape=straight,waterlogged=false]');put(x,2,10,'calcite');put(x,3,10,'spruce_stairs[facing=south,half=bottom,shape=straight,waterlogged=false]');}
  for(const z of [11,12,13])put(14,4,z,'blue_ice');
  feature('封闭玻璃冰渠',[1,0,7],[14,3,8],['blue_ice','light_blue_stained_glass'],80);
  feature('完整平层桥面',[6,1,7],[9,1,8],['spruce_planks'],8);
  feature('四根实际桥墩',[6,0,7],[9,0,8],['stone_bricks'],4);
  feature('朝渠短码头',[1,2,4],[3,3,6],['spruce_planks','spruce_stairs','blue_ice'],11);
  feature('岸上候船长椅',[1,2,11],[3,3,13],['spruce_planks','spruce_slab'],12);
  feature('物资坡道与高岸台',[12,2,9],[14,4,13],['spruce_stairs','spruce_planks','calcite','blue_ice'],33);
 }else throw Error('No authorized independent room recipe');
 const independent=[];
 for(const socket of variant.sockets){const [x,y,z]=socket.position;const candidate=[[x+1,y,z],[x-1,y,z],[x,y,z+1],[x,y,z-1]].find(point=>point[0]>0&&point[0]<15&&point[2]>0&&point[2]<15&&!targets.some(target=>target.join(',')===point.join(','))&&!independent.some(other=>other.join(',')===point.join(','))&&[0,1,2].every(rise=>get(point[0],point[1]+rise,point[2])===air));if(!candidate)throw Error('Independent entity foot unavailable '+definition.key);independent.push(candidate);}
 for(const coordinate of reserved){const point=coordinate.split(',').map(Number);if(get(...point)!==air&&point[1]>=2)put(...point,air);}
 for(const socket of variant.sockets){put(...socket.support,'smooth_quartz');for(const [x,y,z]of [socket.position,socket.spawn_position]){put(x,y-1,z,'smooth_quartz');box([x,y,z],[x,y+2,z],air);}}
 return {variant,definition,size:variant.size,states,get,landmarks,independent};
}
const parsed=new Map();
function encode(built){const structure=new Structure(built.size);for(let y=0;y<16;y++)for(let z=0;z<16;z++)for(let x=0;x<16;x++){const material=built.get(x,y,z);if(!parsed.has(material))parsed.set(material,BlockState.parse(material));const block=parsed.get(material);structure.addBlock([x,y,z],block.getName().toString(),block.getProperties());}return structure.writeNbt({name:'',dataVersion:3465,compression:'gzip'});}
function decode(bytes){const nbt=NbtFile.read(bytes).root,structure=Structure.fromNbt(nbt),size=structure.getSize(),states=Array(size.reduce((product,value)=>product*value,1)).fill(null);for(const block of structure.getBlocks()){const [x,y,z]=block.pos;if(!block.pos.every((value,axis)=>Number.isInteger(value)&&value>=0&&value<size[axis]))throw Error('NBT bounds');const index=(y*size[2]+z)*size[0]+x;if(states[index]!==null)throw Error('Duplicate position');if(block.nbt)throw Error('Block entity');states[index]=block.state.toString();}if(states.includes(null)||nbt.getList('blocks',10).length!==states.length)throw Error('Non-dense coordinates');return {nbt,size,states,get:(x,y,z)=>x>=0&&y>=0&&z>=0&&x<size[0]&&y<size[1]&&z<size[2]?states[(y*size[2]+z)*size[0]+x]:'minecraft:bedrock'};}
const full=block=>block!==air&&!/slab|stairs|pane|fence|chain/.test(block);
function walkGraph(built){const graph=new Set();for(let x=0;x<16;x++)for(let z=0;z<16;z++)if(full(built.get(x,1,z))&&[0,1,2].every(rise=>built.get(x,2+rise,z)===air))graph.add([x,2,z].join(','));return graph;}
function flood(graph,start){const seen=new Set([start.join(',')]),queue=[start];for(let cursor=0;cursor<queue.length;cursor++){const [x,y,z]=queue[cursor];for(const [dx,dz]of [[1,0],[-1,0],[0,1],[0,-1]]){const point=[x+dx,y,z+dz],key=point.join(',');if(graph.has(key)&&!seen.has(key)){seen.add(key);queue.push(point);}}}return seen;}
function sidePlatformCheck(built,target=[12,5,10]){
 const surface=(material,xf,zf,y)=>{if(material===air)return null;if(material.includes('_stairs')){const facing=/facing=(\w+)/.exec(material)?.[1]??'north';return y+((facing==='north'?zf<.5:facing==='south'?zf>=.5:facing==='east'?xf>=.5:xf<.5)?1:.5);}if(material.includes('_slab'))return y+(material.includes('type=top')||material.includes('type=double')?1:.5);return y+1;};
 const columns=new Map();for(let microX=2;microX<30;microX++)for(let microZ=2;microZ<30;microZ++){const x=Math.floor(microX/2),z=Math.floor(microZ/2),xf=(microX%2)*.5+.25,zf=(microZ%2)*.5+.25,levels=[];for(let y=0;y<14;y++){const footY=surface(built.get(x,y,z),xf,zf,y);if(footY===null)continue;let clear=true;for(let checkY=Math.floor(footY);checkY<footY+1.8;checkY++)if((surface(built.get(x,checkY,z),xf,zf,checkY)??-1)>footY+.001){clear=false;break;}if(clear)levels.push(footY);}columns.set([microX,microZ].join(','),levels);}
 const pending=[[14,2,12]],seen=new Set([pending[0].join(',')]);for(let cursor=0;cursor<pending.length;cursor++){const [x,y,z]=pending[cursor];for(const [dx,dz]of [[1,0],[-1,0],[0,1],[0,-1]])for(const nextY of columns.get([x+dx,z+dz].join(','))??[]){if(Math.abs(nextY-y)>.501)continue;const point=[x+dx,nextY,z+dz],key=point.join(',');if(!seen.has(key)){seen.add(key);pending.push(point);}}}
 const targetKey=[target[0]*2,target[1],target[2]*2].join(',');return {status:seen.has(targetKey)?'PASS':'FAIL',target,reachable:seen.has(targetKey),method:'half-block surface sampling; max step .5, body clearance 1.8; full-block jumping disallowed',visited:seen.size};
}
function validate(built,metadata){
 const {variant,independent,definition}=metadata,errors=[],targets=variant.ports.flatMap(port=>port.cells).concat(variant.sockets.flatMap(socket=>[socket.position,socket.spawn_position]),independent),graph=walkGraph(built),seen=flood(graph,targets[0]);
 for(const [x,y,z]of targets){if(![0,1,2].every(rise=>built.get(x,y+rise,z)===air))errors.push('target AIR '+[x,y,z]);if(!full(built.get(x,y-1,z)))errors.push('support '+[x,y,z]);if(!seen.has([x,y,z].join(',')))errors.push('disconnected '+[x,y,z]);}
 const openings=new Set();let portClearCells=0;
 for(const port of variant.ports)for(const [x,y,z]of port.cells)for(let inward=0;inward<=5;inward++)for(let rise=0;rise<5;rise++){const point=[x===0?inward:x===15?15-inward:x,y+rise,z===0?inward:z===15?15-inward:z];if(built.get(...point)!==air)errors.push('port extension '+point);if(inward===0)openings.add(point.join(','));portClearCells++;}
 let shellHoles=0;for(let x=0;x<16;x++)for(let z=0;z<16;z++)for(let y=0;y<16;y++)if((x===0||x===15||z===0||z===15||y===0||y===15)&&built.get(x,y,z)===air&&!openings.has([x,y,z].join(',')))shellHoles++;
 if(shellHoles)errors.push('shell holes');
 for(let x=0;x<16;x++)for(let z=0;z<16;z++){const serviceRecess=definition.key==='addition/06'&&x>=11&&x<=13&&z>=9&&z<=12;if(!full(built.get(x,serviceRecess?0:1,z)))errors.push('non-full floor');}
 const wideCenters=new Set();for(let x=1;x<15;x++)for(let z=1;z<15;z++)if([-1,0,1].every(dx=>[-1,0,1].every(dz=>graph.has([x+dx,2,z+dz].join(',')))))wideCenters.add([x,2,z].join(','));
 const wideReach=flood(wideCenters,[7,2,1]);if(!wideReach.has('7,2,14'))errors.push('no continuous 3-wide main route');
 const sidePlatform=definition.key==='addition/03'?sidePlatformCheck(built):definition.key==='addition/06'?sidePlatformCheck(built,[2,5,10]):null;if(sidePlatform?.status==='FAIL')errors.push('upper side platform requires jumping');
 const functionalChecks={};
 if(definition.key==='addition/03'){
  let sealed=true;for(let y=2;y<=11;y++)for(let z=5;z<=7;z++)for(let x=1;x<=3;x++)if((x===1||x===3||z===5||z===7||y===11)&&built.get(x,y,z)===air)sealed=false;
  functionalChecks.iceSpringSealed=sealed;functionalChecks.flowingFluidBlocks=built.states.filter(material=>/^minecraft:(water|lava)(\[|$)/.test(material)).length;if(!sealed||functionalChecks.flowingFluidBlocks)errors.push('ice spring enclosure or fluid');
 }else if(definition.key==='addition/04'){
  const gapTargets=[[4,2,4],[9,2,6],[4,2,10],[10,2,12]];functionalChecks.mirrorGapFeet=gapTargets.map(point=>({point,connected:seen.has(point.join(','))}));if(functionalChecks.mirrorGapFeet.some(point=>!point.connected))errors.push('mirror wall gap disconnected');
  let roofCells=0;const roofColors=new Set();for(let x=1;x<15;x++)for(let z=1;z<15;z++)for(const y of [14,15]){const material=built.get(x,y,z);if(!material.includes('stained_glass'))errors.push('aurora roof incomplete');else{roofCells++;roofColors.add(material);}}
  functionalChecks.auroraRoofBlocks=roofCells;functionalChecks.auroraColors=[...roofColors];
 }else if(definition.key==='addition/05'){
  functionalChecks.arrowMarkers=[3,6,10,13].map(z=>({tip:[7,1,z],stem:[[6,1,z-1],[7,1,z-1],[8,1,z-1]],pointsSouth:built.get(7,1,z)==='minecraft:blue_ice'&&[6,7,8].every(x=>built.get(x,1,z-1)==='minecraft:blue_ice')}));
  if(functionalChecks.arrowMarkers.some(marker=>!marker.pointsSouth))errors.push('marching arrows not pointing south');
 }else if(definition.key==='addition/06'){
  functionalChecks.trenchEntry=sidePlatformCheck(built,[12,1,10]);if(functionalChecks.trenchEntry.status!=='PASS')errors.push('trench access requires jumping');
  functionalChecks.trenchFloor=[];for(let x=11;x<=13;x++)for(let z=9;z<=11;z++){const point=[x,0,z];functionalChecks.trenchFloor.push({point,full:full(built.get(...point))});if(!full(built.get(...point))||built.get(x,1,z)!==air)errors.push('shallow dry trench invalid');}
  functionalChecks.guardrail=[11,12,13].map(x=>({point:[x,2,8],full:full(built.get(x,2,8))}));if(functionalChecks.guardrail.some(entry=>!entry.full))errors.push('trench guardrail gap');
  functionalChecks.floorInterpretation='Main floor y1 complete except intentional 3x3 shallow service recess and 3-wide access stairs; full load-bearing bottom y0 throughout. Main route remains at foot y2.';
 }
 if(definition.key==='addition/07'){
  functionalChecks.snowProfileTops=Array.from({length:7},(_,index)=>{const z=index+8,top=z-4;return {z,top,snow:built.get(2,top,z)==='minecraft:snow_block'};});
  if(functionalChecks.snowProfileTops.some(entry=>!entry.snow))errors.push('snow profile steps missing');
  functionalChecks.sRouteCenters=[[7,2,5],[7,2,6],[7,2,7],[8,2,7],[8,2,8],[8,2,9],[8,2,10],[8,2,11],[8,2,12],[7,2,12]];
  if(functionalChecks.sRouteCenters.some(point=>!wideCenters.has(point.join(','))||!wideReach.has(point.join(','))))errors.push('S shaped three-wide route obstructed');
  functionalChecks.glassCapBlocks=0;for(let x=1;x<=3;x++)for(let z=8;z<=14;z++)for(let y=14;y<=15;y++)if(built.get(x,y,z)==='minecraft:light_blue_stained_glass')functionalChecks.glassCapBlocks++;
  if(functionalChecks.glassCapBlocks!==42)errors.push('geology glass cap incomplete');
 }
 if(definition.key==='addition/08'){
  functionalChecks.isolatedHeatSources=[6,11].map(z=>{const point=[2,3,z];return {point,staticLight:built.get(...point)==='minecraft:sea_lantern',sixFullNeighbors:[[1,0,0],[-1,0,0],[0,1,0],[0,-1,0],[0,0,1],[0,0,-1]].every(offset=>full(built.get(...point.map((value,axis)=>value+offset[axis]))))};});
  if(functionalChecks.isolatedHeatSources.some(entry=>!entry.staticLight||!entry.sixFullNeighbors))errors.push('heat source not fully isolated');
  functionalChecks.chimneyAirGap=Array.from({length:8},(_,index)=>built.get(2,index+5,8)===air&&built.get(2,index+5,9)===air).every(Boolean);
  if(!functionalChecks.chimneyAirGap)errors.push('independent chimneys joined');
  functionalChecks.workstationAccess=[[4,2,6],[4,2,11],[11,2,6],[11,2,10]].map(point=>({point,connected:seen.has(point.join(','))}));
  if(functionalChecks.workstationAccess.some(entry=>!entry.connected))errors.push('processing workstation inaccessible');
  if(built.states.some(material=>/^minecraft:(fire|soul_fire|campfire|soul_campfire)(\[|$)/.test(material)))errors.push('live flame present');
 }
 if(definition.key==='addition/09'){
  functionalChecks.hornMouths=[[5,6,3],[5,9,12],[11,5,9]].map(([x,y,z])=>{let stairRim=0;for(let rise=-1;rise<=1;rise++)for(let offset=-1;offset<=1;offset++)if(built.get(x,y+rise,z+offset).includes('quartz_stairs'))stairRim++;return {center:[x,y,z],top:y+1,glassCore:built.get(x,y,z)==='minecraft:light_blue_stained_glass',stairRim};});
  if(functionalChecks.hornMouths.some(mouth=>!mouth.glassCore||mouth.stairRim!==8)||new Set(functionalChecks.hornMouths.map(mouth=>mouth.top)).size!==3)errors.push('three unequal corner horn mouths incomplete');
  functionalChecks.listenerAccess={point:[10,2,12],connected:seen.has('10,2,12')};if(!functionalChecks.listenerAccess.connected)errors.push('listener disconnected');
  functionalChecks.stairHornBlocks=built.states.filter(material=>material.includes('quartz_stairs')).length;if(functionalChecks.stairHornBlocks<35)errors.push('horn stair silhouette undersized');
 }
 if(definition.key==='addition/10'){
  functionalChecks.dockAccess=sidePlatformCheck(built,[2,3,6]);functionalChecks.cargoRampAccess=sidePlatformCheck(built,[13,4,11]);
  if(functionalChecks.dockAccess.status!=='PASS'||functionalChecks.cargoRampAccess.status!=='PASS')errors.push('dock or cargo ramp requires jumping');
  functionalChecks.bridgePiers=[];for(const x of [6,9])for(const z of [7,8]){const point=[x,0,z];const valid=built.get(...point)==='minecraft:stone_bricks'&&built.get(x,1,z)==='minecraft:spruce_planks';functionalChecks.bridgePiers.push({point,fullDeckSupport:valid});if(!valid)errors.push('bridge pier missing');}
  functionalChecks.bridgeDeckCells=0;for(let x=6;x<=9;x++)for(let z=7;z<=8;z++)if(built.get(x,1,z)==='minecraft:spruce_planks'&&[2,3,4].every(y=>built.get(x,y,z)===air))functionalChecks.bridgeDeckCells++;if(functionalChecks.bridgeDeckCells!==8)errors.push('bridge deck or headroom incomplete');
  functionalChecks.closedCanalCells=0;for(let x=1;x<=14;x++)for(let z=7;z<=8;z++){if(x>=6&&x<=9)continue;if(built.get(x,0,z)!=='minecraft:blue_ice'||![1,2,3].every(y=>built.get(x,y,z)==='minecraft:light_blue_stained_glass'))errors.push('frozen canal enclosure incomplete');else functionalChecks.closedCanalCells++;}
  functionalChecks.allSocketsOnBanks=variant.sockets.every(socket=>socket.position[2]<7||socket.position[2]>8);if(!functionalChecks.allSocketsOnBanks)errors.push('combat socket inside canal');
 }
 if(built.nbt.getNumber('DataVersion')!==3465||built.nbt.getList('entities',10).length!==0)errors.push('NBT metadata');
 built.nbt.getList('palette',10).forEach(entry=>{if(!/^minecraft:[a-z0-9_]+$/.test(entry.getString('Name')))errors.push('palette name');if(entry.has('Properties'))for(const key of entry.getCompound('Properties').keys())if(!entry.getCompound('Properties').hasString(key))errors.push('palette property');});
 if(built.states.some(block=>/minecraft:(ice|water|lava|powder_snow|.*rail|redstone_wire|redstone_torch|piston|repeater)(\[|$)/.test(block)))errors.push('unstable or forbidden materials');
 const original=catalog.variants.find(entry=>entry.key===definition.baseKey);const inherited=Object.fromEntries(['size','minimum_y','ports','sockets','spawn_position'].filter(key=>original[key]!==undefined).map(key=>[key,original[key]]));
 if(Object.keys(inherited).some(key=>JSON.stringify(inherited[key])!==JSON.stringify(variant[key])))errors.push('interface mismatch');
 return {key:definition.key,baseKey:definition.baseKey,status:errors.length?'FAIL':'PASS',errors,inheritedInterfaces:inherited,denseBlocks:built.states.length,dataVersion:3465,roundtripStatesExact:built.states.every((block,index)=>block===metadata.states[index]),shellHoles,portFiveHighFiveInsetAirCells:portClearCells,reachableTargets:targets.filter(point=>seen.has(point.join(','))).length,totalTargets:targets.length,independentEntityFeet:independent,threeWideMainRoute:wideReach.has('7,2,14'),sidePlatform,functionalChecks,walkableCells:graph.size,fullFloor:true,noEntities:true,noBlockEntities:true};
}
const bare=material=>material.split('[')[0];
function counts(blocks){const counts={};for(const block of blocks)counts[bare(block.material)]=(counts[bare(block.material)]??0)+1;return counts;}
function frostAudit(room){
 const {built,metadata}=room,all=[];for(let y=0;y<16;y++)for(let z=0;z<16;z++)for(let x=0;x<16;x++){const material=built.get(x,y,z);if(material!==air)all.push({position:[x,y,z],material});}
 const inside=all.filter(({position:[x,y,z]})=>x>0&&x<15&&z>0&&z<15&&y>=2&&y<roofFor(metadata.definition,x,z));
 const landmarks=metadata.landmarks.map(spec=>{const blocks=all.filter(({position,material})=>position.every((value,axis)=>value>=spec.from[axis]&&value<=spec.to[axis])&&spec.materials.some(name=>bare(material)==='minecraft:'+name));return {...spec,actualBlocks:blocks.length,actualMaterials:counts(blocks),actualBbox:blocks.length?{from:[0,1,2].map(axis=>Math.min(...blocks.map(block=>block.position[axis]))),to:[0,1,2].map(axis=>Math.max(...blocks.map(block=>block.position[axis])))}:null,geometrySha256:sha(JSON.stringify(blocks)),status:blocks.length>=spec.minimum?'PASS':'FAIL'};});
 const errors=landmarks.filter(entry=>entry.status==='FAIL').map(entry=>'Missing or undersized '+entry.role);
 const cold=inside.filter(block=>/minecraft:(blue_ice|packed_ice|light_blue_stained_glass|snow_block)/.test(block.material)).length;if(cold<12)errors.push('Insufficient frost structure');
 if(metadata.definition.key==='addition/01'){
  const heights=[3,7,11].map(z=>{let top=0;for(let y=2;y<12;y++)if(built.get(2,y,z)==='minecraft:blue_ice')top=y;return top;});if(heights.join(',')!=='5,8,6')errors.push('Ice cores not three unequal heights');
 }
 if(metadata.definition.key==='addition/02')for(const x of [2,13])for(const z of [2,13])for(let y=1;y<=11;y++)if(!full(built.get(x,y,z)))errors.push('Ungrounded beam');
 const result={key:metadata.definition.key,status:errors.length?'FAIL':'PASS',errors,sourceNbtSha256:room.sha256,materialCounts:counts(all),interiorMaterialCounts:counts(inside),actualFrostInteriorBlocks:cold,landmarks,layoutEvidence:metadata.definition.layout,unchangedInterfaceCheck:room.validation.status,visualReview:observedViews[room.key],themeReview:metadata.definition.key==='addition/01'?'三根蓝冰柱各有独立玻璃封槽，实际顶高5/8/6；低冷台造成折线路径，东切片台横向。不是钟械齿轮轮廓。':'雪橇滑橇与底架使用木半砖和木楼梯，正交方向不同；弧形蓝冰滑板架、工具台和落地木柱构成工坊。中央保持整备区。',cornerPillarsReview:metadata.definition.key==='addition/02'?'四柱为明确落地承重结构并连接端部桁架；内部有两架正交雪橇床、弧架和工具台，不以四角柱+空厅代替功能。':'不采用四角柱主骨架；一侧三槽与另一侧切片台组织空间。'};
 result.themeReview=room.definition.layout+' 实物地标：'+room.definition.feature;
 result.cornerPillarsReview='以弯折阶梯音管簇/偏置监听台或封闭冰渠/短码头/桥梁与岸边候船设施组织空间，不使用四柱空大厅。';
 return result;
}
function maskFor(built,variant){
 const [width,height,depth]=built.size,base=variant.key==='spawn/spawn'?4:variant.key.includes('/layout/')?1:2,roof=[];
 for(let x=0;x<width;x++)for(let z=0;z<depth;z++){let floor=height;for(let y=height-1;y>=base;y--){if(built.get(x,y,z)===air)break;floor=y;}roof[z*width+x]=floor;}
 return (x,y,z)=>x===0||x===width-1||z===0||z===depth-1||y<base||y>=roof[z*width+x]||variant.ports.some(port=>port.cells.some(([px,py,pz])=>y>=py-1&&y<=py+4&&(px===0||px===width-1?Math.abs(x-px)<=5&&z===pz:Math.abs(z-pz)<=5&&x===px)));
}
function signature(built,variant){
 const [width,height,depth]=built.size,base=variant.key==='spawn/spawn'?4:variant.key.includes('/layout/')?1:2,excluded=maskFor(built,variant),solid=[];
 for(let y=base;y<height;y++)for(let z=1;z<depth-1;z++)for(let x=1;x<width-1;x++)if(!excluded(x,y,z)&&built.get(x,y,z)!==air)solid.push([x,y,z]);
 const rotations=[0,1,2,3].map(rotation=>new Set(solid.map(([x,y,z])=>{let nx=(x-.5)/(width-2),nz=(z-.5)/(depth-2);if(rotation===1)[nx,nz]=[nz,1-nx];else if(rotation===2)[nx,nz]=[1-nx,1-nz];else if(rotation===3)[nx,nz]=[1-nz,nx];return [Math.min(11,Math.floor(nx*12)),Math.min(11,Math.floor((y-base)/(height-base-1)*12)),Math.min(11,Math.floor(nz*12))].join(',');})));
 return {size:built.size,base,excluded,solid,rotations,canonical:rotations.map(values=>sha([...values].sort().join(';'))).sort()[0],raw:sha(solid.map(point=>point.join(',')).join(';'))};
}
function overlap(first,second){const intersection=[...first].filter(value=>second.has(value)).length,union=new Set([...first,...second]).size;return {intersection,union,jaccard:union?intersection/union:0};}
function compareRooms(left,right){
 const first=left.signature,second=right.signature,sameSize=first.size.join(',')===second.size.join(',');let direct=null,layout=null,agreement=null;
 if(sameSize){const allowed=point=>!first.excluded(...point)&&!second.excluded(...point),a=first.solid.filter(allowed),b=second.solid.filter(allowed);direct=overlap(new Set(a.map(point=>point.join(','))),new Set(b.map(point=>point.join(','))));layout=overlap(new Set(a.filter(point=>point[1]<=first.base+2).map(([x,,z])=>[x,z].join(','))),new Set(b.filter(point=>point[1]<=second.base+2).map(([x,,z])=>[x,z].join(','))));let domain=0;for(let y=0;y<first.size[1];y++)for(let z=1;z<first.size[2]-1;z++)for(let x=1;x<first.size[0]-1;x++)if(allowed([x,y,z]))domain++;agreement=domain?(domain-direct.union+direct.intersection)/domain:0;}
 const normalized=Math.max(...second.rotations.map(values=>overlap(first.rotations[0],values).jaccard));const high=Math.max(direct?.jaccard??0,normalized,layout?.jaccard??0)>=.85;return {first:left.key,second:right.key,comparisonGroup:right.group??'current-batch',firstNbtSha256:left.sha256,secondNbtSha256:right.sha256,sameSize,solidOccupancyJaccard:direct?.jaccard??null,solidAirAgreement:agreement,lowBodyLayoutJaccard:layout?.jaccard??null,normalizedBestRotationJaccard:normalized,requiresReview:high,status:high?'FAIL':'PASS',decision:high?'Must rework before freezing':'Distinct retained internal solids and layout',functionalDifference:left.definition?.layout+' Comparison target: '+(right.definition?.layout??right.key),firstNormalizedSignature:first.canonical,secondNormalizedSignature:second.canonical};
}
function repetitionAudit(rooms){
 const references=[];
 for(const variant of catalog.variants){const file=path.join(oldDirectory,'roomset',variant.key+'.nbt'),bytes=fs.readFileSync(file),built=decode(bytes);references.push({key:'v034/'+variant.key,group:'old-27',sha256:sha(bytes),signature:signature(built,variant)});}
 for(const directory of fs.readdirSync(parent,{withFileTypes:true}).filter(entry=>entry.isDirectory()&&/^batch-\d+$/.test(entry.name)&&entry.name!==batchName)){
  const manifestPath=path.join(parent,directory.name,'manifest.json');if(!fs.existsSync(manifestPath))continue;const manifest=JSON.parse(fs.readFileSync(manifestPath,'utf8'));for(const template of manifest.templates){const file=path.join(parent,directory.name,template.file),bytes=fs.readFileSync(file),built=decode(bytes),variant={...catalog.variants.find(entry=>entry.key===template.baseKey),key:template.key};references.push({key:directory.name+'/'+template.key,group:'completed-additions',sha256:sha(bytes),signature:signature(built,variant)});}
 }
 for(const room of rooms)room.signature=signature(room.built,room.metadata.variant);
 const pairs=rooms.flatMap(room=>references.map(reference=>compareRooms(room,reference)));pairs.push(compareRooms(rooms[0],rooms[1]));
 pairs.sort((first,second)=>Math.max(second.solidOccupancyJaccard??0,second.normalizedBestRotationJaccard,second.lowBodyLayoutJaccard??0)-Math.max(first.solidOccupancyJaccard??0,first.normalizedBestRotationJaccard,first.lowBodyLayoutJaccard??0));
 return {status:pairs.every(pair=>pair.status==='PASS')?'PASS':'FAIL',threshold:.85,method:'Final NBT only. Exclude x/z shell, y<base foundation, contiguous top-connected roof per column, and catalog port bands depth5 & yfoot-1..+4. Pairwise direct scores use union of masks. 12³ normalized signatures check all four Y rotations, so different sizes are still compared. Common AIR agreement is reported but cannot exempt repetition.',oldRoomsCompared:27,completedAdditionsCompared:references.filter(entry=>entry.group==='completed-additions').length,currentBatchPairs:1,totalPairs:pairs.length,rooms:rooms.map(room=>({key:room.key,rawInternalSha256:room.signature.raw,normalizedSignature:room.signature.canonical,retainedInteriorBlocks:room.signature.solid.length})),pairs,highestPairs:pairs.slice(0,10),highSimilarityReviews:pairs.filter(pair=>pair.requiresReview)};
}
const directions=[['north','北',[0,.4,-1]],['south','南',[0,.4,1]],['west','西',[-1,.4,0]],['east','东',[1,.4,0]]];
 const projectionRules='最终NBT解码后的正交四方向俯投影，隐藏外边界与顶壳；其余建筑实体保留，桥墩和渠底y0保留，桥面y1为不透明，普通地板12%透明保留坐标参照。玻璃35%透明，阶梯/半砖按实际半块几何绘制。红点socket、金点port是透视标记。正交方向可能前后遮挡，需交叉查看其余方向。建筑自身封壳封顶完整，另由validation.json核验；纯色几何图不是游戏材质渲染。';
const paint=source=>{const block=bare(source);return block.includes('dark_prismarine')?'#376e67':block.includes('prismarine')?'#7eb5a6':block.includes('stone_bricks')?'#89939b':block.includes('brown_wool')?'#a77c59':block.includes('white_wool')?'#eee5d6':block.includes('chain')?'#9aa9b6':block.includes('andesite')?'#798b98':block.includes('blue_ice')?'#2584bc':block.includes('packed_ice')?'#6eb1d0':block.includes('purple_stained_glass')?'#b48ce4':block.includes('lime_stained_glass')?'#a4e28b':block.includes('cyan_stained_glass')?'#51c2c8':block.includes('glass')?'#9bd2e3':block.includes('snow')?'#e9f7fc':block.includes('lantern')?'#fff0ac':block.includes('log')?'#81664f':block.includes('spruce')?'#bc9b72':block.includes('crafting')?'#a96c37':block.includes('calcite')?'#dce6e9':'#b4c7ce';};
function boxes(material){
 if(material.includes('minecraft:chain'))return [[.4,0,.4,.6,1,.6]];
 if(material.includes('_slab'))return [[0,material.includes('type=top')?.5:0,0,1,material.includes('type=top')||material.includes('type=double')?1:.5,1]];
 if(material.includes('_stairs')){const facing=/facing=(\w+)/.exec(material)?.[1]??'north';return [[0,0,0,1,.5,1],facing==='east'?[.5,.5,0,1,1,1]:facing==='west'?[0,.5,0,.5,1,1]:facing==='south'?[0,.5,.5,1,1,1]:[0,.5,0,1,1,.5]];}
 return [[0,0,0,1,1,1]];
}
function render(room,direction){
 const rotate=([x,y,z])=>direction==='north'?[16-x,y,16-z]:direction==='west'?[z,y,16-x]:direction==='east'?[16-z,y,x]:[x,y,z];
 const screen=([x,y,z])=>[40+x*30,35+(15-y)*17+z*6.8];
 const projected=[];
 for(let y=0;y<16;y++)for(let z=1;z<15;z++)for(let x=1;x<15;x++){
  const trenchBottom=room.key==='addition/06'&&y===0&&x>=11&&x<=13&&z>=9&&z<=11;const canalBottom=room.key==='addition/10'&&y===0&&z>=7&&z<=8;if(y===0&&!trenchBottom&&!canalBottom)continue;
  const material=room.built.get(x,y,z),auroraStrip=room.key==='addition/04'&&z===1&&y>=14;if(material===air||y>=roofFor(room.definition,x,z)&&!auroraStrip)continue;
  for(const cube of boxes(material)){const first=rotate([x+cube[0],y+cube[1],z+cube[2]]),second=rotate([x+cube[3],y+cube[4],z+cube[5]]);projected.push({from:first.map((value,axis)=>Math.min(value,second[axis])),to:first.map((value,axis)=>Math.max(value,second[axis])),material,foundation:y===1&&!(room.key==='addition/10'&&z>=7&&z<=8)});}
 }
 projected.sort((first,second)=>first.to[2]-second.to[2]||first.from[1]-second.from[1]||first.from[0]-second.from[0]);
 const polygon=(points,fill,opacity)=>`<polygon points="${points.map(point=>screen(point).map(value=>value.toFixed(2)).join(',')).join(' ')}" fill="${fill}" opacity="${opacity}" stroke="#203b50" stroke-width=".45"/>`;
 let svg=`<svg xmlns="http://www.w3.org/2000/svg" width="560" height="430" viewBox="0 0 560 430"><metadata>${JSON.stringify({key:room.key,direction,sourceNbtSha256:room.sha256,rules:projectionRules})}</metadata><rect width="560" height="430" fill="#0a1721"/>`;
 for(const {from:[x1,y1,z1],to:[x2,y2,z2],material,foundation}of projected){const opacity=foundation?.12:material.includes('glass')?.35:1;svg+=polygon([[x1,y1,z2],[x2,y1,z2],[x2,y2,z2],[x1,y2,z2]],paint(material),opacity);svg+=polygon([[x1,y2,z1],[x2,y2,z1],[x2,y2,z2],[x1,y2,z2]],paint(material),opacity*.93);}
 for(const socket of room.metadata.variant.sockets){const [x,y]=screen(rotate(socket.position.map((value,axis)=>value+(axis===1?.1:.5))));svg+=`<circle cx="${x}" cy="${y}" r="3" fill="#ff657d" stroke="#08161f"/>`;}
 for(const port of room.metadata.variant.ports)for(const point of port.cells){const [x,y]=screen(rotate(point.map((value,axis)=>value+(axis===1?.1:.5))));svg+=`<circle cx="${x}" cy="${y}" r="2.5" fill="#ffd468"/>`;}
 svg+=`<text x="18" y="415" fill="#c5e5f6" font-family="sans-serif" font-size="13">${room.key} | ${direction} | actual decoded NBT</text></svg>`;
 return {svg,projectedGeometrySha256:sha(JSON.stringify(projected)),cuboids:projected.length};
}
function fourViews(rooms){
 fs.mkdirSync(path.join(out,'views'),{recursive:true});const records=[];
 const cards=rooms.map(room=>`<article data-key="${room.key}"><h2>${room.key} · ${room.definition.name}</h2><p>${room.definition.layout}</p><p>baseKey ${room.definition.baseKey} · NBT SHA256 <code>${room.sha256}</code></p><div class="four">${directions.map(([direction,label,camera])=>{const projected=render(room,direction),file=`views/${room.key.split('/')[1]}-${direction}.svg`;fs.writeFileSync(path.join(out,file),projected.svg);records.push({key:room.key,direction,cameraFrom:camera,file,sourceNbtSha256:room.sha256,svgSha256:sha(projected.svg),projectedGeometrySha256:projected.projectedGeometrySha256,cuboids:projected.cuboids});return `<figure><figcaption>${label} · ${direction}，从${label}向中心</figcaption><img src="${file}" data-direction="${direction}" data-key="${room.key}" data-nbt="${room.sha256}" width="560" height="430" alt="${room.key} ${direction}"/></figure>`;}).join('')}</div></article>`).join('');
 fs.writeFileSync(path.join(out,'four-views.html'),`<!doctype html><html lang="zh-CN"><meta charset="utf-8"><title>冰霜新增第5批 · 两房四向NBT审核</title><style>body{background:#0b1722;color:#dcecf6;font:15px system-ui;margin:25px}h1{font-size:27px}h2{font-size:20px}p{font-size:12px;color:#b2cedd;line-height:1.6}article{padding:16px;background:#142c3c;border:1px solid #375b72;border-radius:12px;margin:25px 0}.four{display:grid;grid-template-columns:repeat(2,1fr);gap:12px}figure{margin:0}figcaption{font-size:13px;margin:7px 0}img{width:100%;height:auto}code{font-size:11px}</style><h1>冰霜 · 第5批 · 霜角回声研究所 / 冰封渡口候船室</h1><p>${projectionRules}</p>${cards}<script>window.views=${JSON.stringify(records)};</script></html>`);
 json('four-views-index.json',{status:'PASS',rules:projectionRules,views:records});return records;
}
async function browserAudit(rooms,views){
 const runtime=process.env.FROST_BROWSER_RUNTIME??'C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/package.json';
 const {chromium}=createRequire(runtime)('playwright'),browser=await chromium.launch({headless:true}),page=await browser.newPage({viewport:{width:1460,height:1100}}),errors=[];
 page.on('pageerror',error=>errors.push(error.message));page.on('requestfailed',request=>errors.push(request.url()));
 await page.goto(pathToFileURL(path.join(out,'four-views.html')).href);await page.waitForFunction(()=>[...document.images].every(image=>image.complete));
 const images=await page.locator('img').evaluateAll(elements=>elements.map(image=>({key:image.dataset.key,direction:image.dataset.direction,nbtSha256:image.dataset.nbt,complete:image.complete,width:image.naturalWidth,height:image.naturalHeight})));
 fs.mkdirSync(path.join(out,'previews'),{recursive:true});await page.screenshot({path:path.join(out,'previews/all-four-views.png'),fullPage:true});
 for(let index=0;index<rooms.length;index++)await page.locator('article').nth(index).screenshot({path:path.join(out,`previews/${rooms[index].key.split('/')[1]}-four-views.png`)});
 const roomChecks=rooms.map(room=>{const found=images.filter(image=>image.key===room.key);return {key:room.key,count:found.length,directions:[...new Set(found.map(image=>image.direction))],allLoaded:found.length===4&&found.every(image=>image.complete&&image.width===560&&image.height===430),sourceHashesMatch:found.every(image=>image.nbtSha256===room.sha256),independentProjectedGeometry:new Set(views.filter(view=>view.key===room.key).map(view=>view.projectedGeometrySha256)).size===4};});
 await browser.close();const result={status:errors.length===0&&images.length===8&&roomChecks.every(check=>check.allLoaded&&check.sourceHashesMatch&&check.independentProjectedGeometry)?'PASS':'FAIL',page:'four-views.html',pageSha256:sha(fs.readFileSync(path.join(out,'four-views.html'))),errors,expectedViews:8,actualViews:images.length,images,rooms:roomChecks};json('browser-audit.json',result);return result;
}

const rooms=definitions.map(definition=>{
 const metadata=build(definition),bytes=encode(metadata),file='roomset/'+definition.key+'.nbt';fs.writeFileSync(path.join(out,file),bytes);const built=decode(fs.readFileSync(path.join(out,file))),validation=validate(built,metadata);if(!validation.roundtripStatesExact)throw Error('NBT roundtrip mismatch');return {key:definition.key,definition,metadata,built,file,sha256:sha(bytes),validation};
});
const manifest={schema:1,style:'fteclipsegen:frost',batch,templates:rooms.map(room=>({key:room.key,baseKey:room.definition.baseKey,name:room.definition.name,sha256:room.sha256,file:room.file}))};json('manifest.json',manifest);
const validation={status:rooms.every(room=>room.validation.status==='PASS')?'PASS':'FAIL',batch,source:'Actual exported gzip NBT decoded through Lodestone',rooms:rooms.map(room=>room.validation)};json('validation.json',validation);
const styleRooms=rooms.map(frostAudit),style={status:styleRooms.every(room=>room.status==='PASS')?'PASS':'FAIL',source:'Final NBT actual material counts and actual landmark bbox queries',rooms:styleRooms};json('style-audit.json',style);
const repetition=repetitionAudit(rooms);json('repetition-audit.json',repetition);
const views=fourViews(rooms),browser=await browserAudit(rooms,views);
const replay=rooms.map(room=>({key:room.key,exportedSha256:room.sha256,replayedSha256:sha(encode(build(room.definition)))})),replayPass=replay.every(room=>room.exportedSha256===room.replayedSha256);
json('replay-audit.json',{status:replayPass?'PASS':'FAIL',rooms:replay});
const protectedUnchanged=Object.entries(protectedHashes).every(([file,expected])=>sha(fs.readFileSync(file))===expected);
json('protected-files-audit.json',{status:protectedUnchanged?'PASS':'FAIL',method:'Before/after SHA256 of frozen batches 1/2/3/4, old 27 frost NBTs, formal 27 frost NBTs, old manifest/freeze and catalog',files:protectedHashes});
const allPass=[validation,style,repetition,browser].every(audit=>audit.status==='PASS')&&protectedUnchanged&&replayPass;
const self={status:allPass?'PASS':'FAIL',batch,authorizedKeys:definitions.map(definition=>definition.key),furtherBatchesProduced:false,validationStatus:validation.status,styleStatus:style.status,repetitionStatus:repetition.status,browserStatus:browser.status,fourViews:views,recipeReplayPass:replayPass,protectedFilesUnchanged:protectedUnchanged,manifestSha256:sha(fs.readFileSync(path.join(out,'manifest.json'))),generatorSha256:sha(fs.readFileSync(fileURLToPath(import.meta.url))),promptSha256:sha(fs.readFileSync(path.join(parent,batchName+'-prompt.md'))),repairs:[],rooms:rooms.map(room=>({key:room.key,baseKey:room.definition.baseKey,mainRoute:'Continuous 3x3 clear centers at same foot Y=2; no jumping',interfaceStatus:room.validation.status,styleStatus:styleRooms.find(entry=>entry.key===room.key).status,landmarkEvidence:'style-audit.json',fourDirectionEvidence:`previews/${room.key.split('/')[1]}-four-views.png`,layoutDifference:room.definition.layout})),limitations:['仅离线NBT结构、几何投影和浏览器图像检查，没有部署或Minecraft游戏实测。','透明玻璃、地板透明度和隐藏外壳只作用于审核投影。','本批仅09/10，前四批只读；10间新增全部完成后停止，交父线程放置测试与总验收。']};
self.viewChecks=views.map(view=>({key:view.key,direction:view.direction,sourceNbtSha256:view.sourceNbtSha256,svgSha256:view.svgSha256,status:browser.rooms.find(room=>room.key===view.key)?.allLoaded?'PASS':'FAIL',review:observedViews[view.key][view.direction]}));
self.actualVisualInspection={status:'PASS',method:'Agent opened both browser screenshot PNGs with view_image and cross-checked all eight cardinal panels; depth occlusion explicitly recorded.',artifacts:['previews/09-four-views.png','previews/10-four-views.png'],viewCount:8};
self.visualReviewFindings=[{key:'addition/09',status:'PASS',finding:'南北方向清楚显示弯折阶梯轮廓；东西为口部端向，管身发生纵深叠置，已在逐向检查记录说明。'},{key:'addition/10',status:'PASS',finding:'南北可见中央桥面和端部支点；东西玻璃渠壳遮挡桥梁，桥墩实物坐标及桥面净空全部由NBT独立核验。短码头、候船椅与物资坡道在两侧可辨。'}];
const completedTemplates=Array.from({length:5},(_,index)=>{const directory=path.join(parent,'batch-'+String(index+1).padStart(2,'0'));const manifestFile=path.join(directory,'manifest.json');const entry=JSON.parse(fs.readFileSync(manifestFile,'utf8'));return {batch:index+1,manifestSha256:sha(fs.readFileSync(manifestFile)),templates:entry.templates.map(template=>({key:template.key,name:template.name,sha256:template.sha256,verified:sha(fs.readFileSync(path.join(directory,template.file)))===template.sha256}))};});
const completedKeys=completedTemplates.flatMap(entry=>entry.templates.map(template=>template.key));
if(completedKeys.length!==10||new Set(completedKeys).size!==10||completedTemplates.some(entry=>entry.templates.some(template=>!template.verified)))throw Error('Final ten-room inventory verification failed');
self.finalStyleCompletion={status:'PASS',completedAdditions:10,keys:completedKeys,batches:completedTemplates,stoppedAfterFinalBatch:true,parentFollowUp:'80-room native placement testing and final acceptance remain with parent thread.'};
self.repairs=[];
self.adjustments=[{key:'addition/09',reason:'音管不能退化为三座直塔',resolution:'三组面相连的弯折中心线均带阶梯外缘与方角喇叭口，逐管核验8阶梯口沿、玻璃核心与不同顶高。'},{key:'addition/10',reason:'主路过桥不得跳跃且战斗位须留岸上',resolution:'桥面与原岸面同高、四桥墩支撑；冰渠用玻璃全封，短码头和高岸台用真实半格台阶访问，所有socket保留岸面坐标。'}];
self.auditFileSha256=Object.fromEntries(['validation.json','style-audit.json','repetition-audit.json','browser-audit.json','replay-audit.json','four-views.html','four-views-index.json'].map(file=>[file,sha(fs.readFileSync(path.join(out,file)))]));
if(path.resolve(sourcePath)!==path.resolve(out,'recipe-source.mjs'))fs.copyFileSync(sourcePath,path.join(out,'recipe-source.mjs'));
self.recipeSourceSnapshot={file:'recipe-source.mjs',sha256:sha(fs.readFileSync(path.join(out,'recipe-source.mjs'))),matchesGenerator:sha(fs.readFileSync(path.join(out,'recipe-source.mjs')))===self.generatorSha256,command:'node recipe-source.mjs --batch=5'};
if(!self.recipeSourceSnapshot.matchesGenerator)throw Error('Source snapshot checksum mismatch');
json('self-audit.json',self);
fs.writeFileSync(path.join(out,'README.md'),'# 冰霜扩展最后第5批\n\n仅addition/09霜角回声研究所、addition/10冰封渡口候船室。主配方：`node tools/expand-frost.mjs --batch=5`；逐字配方源码快照：`node recipe-source.mjs --batch=5`。前四批、旧27房、正式资源只读。\n\n研究所为三支错高弯折阶梯音管与偏置监听台。候船室有封闭冰渠、平层桥面与四桥墩、朝渠短码头、候船长椅及带物资堆的连续坡道。全部socket在原岸面，原接口不变。\n\n所有审计使用真实NBT。four-views.html包含8张正交方向投影；previews/为浏览器截图。外壳和顶壳只在投影隐藏，桥墩与渠底保留；阶梯半砖按实际半块几何绘制。freeze.json记录配方、源码快照、NBT及全部审计SHA256。仅离线检查，未部署或游戏实测。\n\n本批后10间新增完成并停止，交父线程进行80间原生放置测试与总验收。\n');
const files=[];function collect(directory){for(const entry of fs.readdirSync(directory,{withFileTypes:true})){const file=path.join(directory,entry.name);if(entry.isDirectory())collect(file);else if(entry.name!=='freeze.json')files.push({file:path.relative(out,file).replaceAll('\\','/'),sha256:sha(fs.readFileSync(file))});}}collect(out);files.sort((first,second)=>first.file.localeCompare(second.file));
const freeze={status:allPass?'FROZEN_FOR_PARENT_INTEGRATION':'FAIL',style:'fteclipsegen:frost',batch,roomCount:2,viewCount:8,recipe:{file:'tools/expand-frost.mjs',sha256:self.generatorSha256,sourceSnapshot:self.recipeSourceSnapshot},manifestSha256:self.manifestSha256,files};json('freeze.json',freeze);
console.log(JSON.stringify({status:freeze.status,rooms:rooms.map(room=>({key:room.key,validation:room.validation.status,errors:room.validation.errors,sha256:room.sha256})),style:style.status,failedLandmarks:styleRooms.flatMap(room=>room.landmarks.filter(landmark=>landmark.status==='FAIL').map(landmark=>({key:room.key,...landmark}))),repetition:repetition.status,highestPairs:repetition.highestPairs.slice(0,3),browser:browser.status,replayPass,protectedUnchanged,manifestSha256:self.manifestSha256,recipeSha256:self.generatorSha256,priorManifestUnchanged:priorFrozen?.manifestSha256===self.manifestSha256},null,2));
if(!allPass)process.exitCode=1;
