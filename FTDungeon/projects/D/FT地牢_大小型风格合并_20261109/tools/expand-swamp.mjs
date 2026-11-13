import fs from 'node:fs';
import path from 'node:path';
import crypto from 'node:crypto';
import {createRequire} from 'node:module';
import {fileURLToPath,pathToFileURL} from 'node:url';
let root=path.dirname(fileURLToPath(import.meta.url));
while(!fs.existsSync(path.join(root,'src/main/resources/data/fteclipsegen/catalog.json'))){const parent=path.dirname(root);if(parent===root)throw Error('Project catalog not found');root=parent;}
const batch=5;
const output=path.join(root,'output/room-expansion-v035/swamp/batch-05');
const data=path.join(root,'src/main/resources/data/fteclipsegen');
const require=createRequire('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/package.json');
const {Structure,NbtFile,BlockState}=require('@mattzh72/lodestone');
const variants=JSON.parse(fs.readFileSync(path.join(data,'catalog.json'),'utf8')).variants;
const sha=bytes=>crypto.createHash('sha256').update(bytes).digest('hex');
const air='minecraft:air';
const specs=[{key:'addition/09',baseKey:'straight/room_towers',name:'湿地测潮记录室',layout:'西墙三根不等高水位刻柱形成时间标尺，西北偏心观测小台以半格阶接干道；东南记录柜与两侧封闭观察窗相望，地板全程干燥',features:['unequal-wall-tide-gauge-posts','off-center-stepped-observation-deck','side-record-cabinet','sealed-glass-observation-windows']},{key:'addition/10',baseKey:'straight/room_cells',name:'守沼人灯笼庭',layout:'西侧两次转折木架以吊链悬挂三个不同高度灯笼，东侧根席与东北独立矮祭桌围合聚落庭院；中轴与横向旱路保留交错视线，无中央蘑菇',features:['bent-timber-lantern-arbor','three-height-hanging-lanterns','side-root-mat','independent-low-altar-courtyard']}];
const json=(name,value)=>fs.writeFileSync(path.join(output,name),JSON.stringify(value,null,2)+'\n');
function verifyPreserved(){
 const protectedFiles=[];
 for(let previous=1;previous<batch;previous++){
  const folder='batch-'+String(previous).padStart(2,'0'),previousRoot=path.join(root,'output/room-expansion-v035/swamp',folder);
  for(const line of fs.readFileSync(path.join(previousRoot,'SHA256SUMS.txt'),'utf8').trim().split('\n')){const [digest,file]=line.split('  ');if(sha(fs.readFileSync(path.join(previousRoot,file)))!==digest)throw Error('Previous frozen artifact changed '+folder+'/'+file);protectedFiles.push({file:folder+'/'+file,sha256:digest});}
 }
 const oldRoot=path.join(root,'output/style-rebuild-v034/swamp'),oldManifest=JSON.parse(fs.readFileSync(path.join(oldRoot,'roomset/manifest.json'),'utf8'));
 for(const template of oldManifest.templates){if(sha(fs.readFileSync(path.join(oldRoot,'roomset',template.key+'.nbt')))!==template.sha256)throw Error('Old27 changed');}
 return {status:'PASS',priorBatchFiles:protectedFiles,old27ManifestSha256:sha(fs.readFileSync(path.join(oldRoot,'roomset/manifest.json'))),old27NbtChecked:27,officialResourceWrites:0};
}
function build(spec){
 const variant=variants.find(room=>room.key===spec.baseKey),size=variant.size,[width,height,depth]=size;
 if(size.join()!=='16,16,16')throw Error('Interface dimensions');
 const states=new Array(width*height*depth).fill(air),landmarks=[];
 const get=(xx,yy,zz)=>xx>=0&&xx<width&&yy>=0&&yy<height&&zz>=0&&zz<depth?states[(yy*depth+zz)*width+xx]:'minecraft:bedrock';
 const put=(xx,yy,zz,block)=>{if(xx<0||xx>=width||yy<0||yy>=height||zz<0||zz>=depth)throw Error('Bounds');states[(yy*depth+zz)*width+xx]=block.startsWith('minecraft:')?block:'minecraft:'+block;};
 const box=(from,to,block)=>{for(let yy=from[1];yy<=to[1];yy++)for(let zz=from[2];zz<=to[2];zz++)for(let xx=from[0];xx<=to[0];xx++)put(xx,yy,zz,block);};
 const mark=(name,from,to)=>landmarks.push({name,requestedBBox:{min:from,max:to}});
 box([0,0,0],[15,1,15],'packed_mud');
 for(let yy=2;yy<16;yy++){box([0,yy,0],[15,yy,0],yy<7?'mud_bricks':'glass');box([0,yy,15],[15,yy,15],yy<7?'mud_bricks':'glass');box([0,yy,1],[0,yy,14],yy<7?'mud_bricks':'glass');box([15,yy,1],[15,yy,14],yy<7?'mud_bricks':'glass');}
 box([1,15,1],[14,15,14],'glass');
 for(let xx=1;xx<15;xx++)for(let zz=1;zz<15;zz++)put(xx,1,zz,(xx>=6&&xx<=9)||zz>=7&&zz<=9?'mangrove_planks':'packed_mud');
 const timberFrame=(x1,z1,x2,z2,top)=>{for(const xx of [x1,x2])for(const zz of [z1,z2])box([xx,2,zz],[xx,top,zz],'mangrove_log');box([x1,top,z1],[x2,top,z2],'mangrove_planks');box([Math.floor((x1+x2)/2),top-2,Math.floor((z1+z2)/2)],[Math.floor((x1+x2)/2),top-1,Math.floor((z1+z2)/2)],'stripped_mangrove_log');};
 if(spec.key==='addition/01'){
  timberFrame(2,2,5,4,9);mark('northwest-high-peat-press',[2,2,2],[5,9,4]);
  timberFrame(10,10,13,13,6);mark('southeast-low-peat-press',[10,2,10],[13,6,13]);
  box([2,2,10],[8,2,11],'brown_terracotta');box([2,3,10],[8,3,10],'mud_bricks');mark('transverse-peat-drying-bed',[2,2,10],[8,3,11]);
  box([10,2,2],[13,2,5],'mangrove_planks');
  for(let xx=10;xx<=12;xx++)put(xx,2,6,'mangrove_slab[type=bottom,waterlogged=false]');
  mark('half-step-raised-working-platform',[10,2,2],[13,2,6]);
  box([2,5,6],[4,5,6],'mangrove_roots');box([3,6,6],[4,6,6],'mangrove_roots');
 }else if(spec.key==='addition/02'){
  for(const [x1,z1,x2,z2]of [[2,2,4,6],[2,10,5,13]]){
   box([x1,2,z1],[x2,2,z2],'mud_bricks');box([x1+1,2,z1+1],[x2-1,2,z2-1],'clay');
   mark('sealed-dry-pulp-trough',[x1,2,z1],[x2,2,z2]);
  }
  for(const [x1,z1,x2,z2,top]of [[10,2,13,5,7],[11,10,13,13,9]]){
   for(const xx of [x1,x2])for(const zz of [z1,z2])box([xx,2,zz],[xx,top,zz],'mangrove_log');
   for(const yy of [top-3,top]){box([x1,yy,z1],[x2,yy,z2],'mangrove_planks');box([x1+1,yy,z1+1],[x2-1,yy,z2-1],'white_wool');}
   mark('tiered-reed-paper-screen',[x1,2,z1],[x2,top,z2]);
  }
  box([3,2,8],[4,2,9],'brown_terracotta');box([12,2,7],[13,2,8],'brown_terracotta');
  mark('low-pulp-vats-west',[3,2,8],[4,2,9]);mark('low-pulp-vats-east',[12,2,7],[13,2,8]);
  for(const xx of [2,13])box([xx,2,7],[xx,10,7],'mangrove_log');
  box([2,10,7],[13,10,7],'mangrove_log');mark('transverse-workshop-timber-beam',[2,2,7],[13,10,7]);
 }
 if(spec.key==='addition/03'){
  box([1,2,2],[4,2,5],'mud_bricks');
  for(let yy=3;yy<=8;yy++)for(let xx=1;xx<=4;xx++)for(let zz=2;zz<=5;zz++)if(xx===1||xx===4||zz===2||zz===5)put(xx,yy,zz,'glass');
  box([1,9,2],[4,9,5],'mangrove_planks');box([2,3,3],[3,3,4],'green_terracotta');
  mark('eccentric-sealed-gas-well',[1,2,2],[4,9,5]);
  for(const [x1,z1,x2,z2,top]of [[11,1,14,2,6],[1,12,2,14,7]]){
   box([x1,2,z1],[x2,2,z2],'mud_bricks');
   for(const xx of [x1,x2])for(const zz of [z1,z2])box([xx,3,zz],[xx,top-1,zz],'mangrove_log');
   box([x1,top,z1],[x2,top,z2],'mangrove_planks');
   box([x1,top+1,z1],[x1,top+2,z1],'green_terracotta');
   mark('rooted-gas-collection-hood',[x1,2,z1],[x2,top+2,z2]);
  }
  for(const [xx,zz]of [[5,3],[5,12],[10,2],[10,13]])box([xx,0,zz],[xx,1,zz],'mangrove_log');
  box([2,1,6],[5,1,7],'mangrove_planks');box([3,1,12],[6,1,13],'mangrove_planks');
  mark('solid-footed-well-service-bridge',[2,0,6],[9,1,7]);
 }
 if(spec.key==='addition/04'){
  box([1,2,10],[1,4,13],'mangrove_planks');box([2,2,14],[4,4,14],'mangrove_planks');box([5,2,12],[5,4,13],'mangrove_planks');
  for(const [xx,zz]of [[1,11],[1,13],[2,14],[4,14],[5,12]])put(xx,5,zz,'green_terracotta');
  mark('half-arc-medicine-cabinet',[1,2,10],[5,5,14]);
  box([3,2,12],[4,2,12],'mud_bricks');box([3,3,12],[4,3,12],'white_wool');
  mark('offset-leech-diagnostic-table',[3,2,12],[4,3,12]);
  for(const zz of [6,9]){box([11,2,zz],[13,5,zz+1],'glass');box([11,2,zz],[13,2,zz+1],'mud_bricks');box([12,3,zz],[12,4,zz],'clay');mark('closed-static-culture-tank',[11,2,zz],[13,5,zz+1]);}
  box([3,2,7],[5,5,7],'mangrove_planks');put(3,6,7,'mangrove_log');put(5,6,7,'mangrove_log');
  mark('partial-privacy-screen',[3,2,7],[5,6,7]);
 }
 if(spec.key==='addition/05'){
  for(const [zz,left,right]of [[2,1,3],[5,1,4],[8,2,5],[12,3,5]]){
   for(let xx=left;xx<=right;xx++){const edge=Math.min(xx-left,right-xx),top=edge===0?5:edge===1?3:2;box([xx,2,zz],[xx,top,zz],'mangrove_log');}
   mark('stepped-U-ship-rib',[left,2,zz],[right,5,zz]);
  }
  for(let zz=2;zz<=12;zz++){const shift=zz<6?1:zz<10?2:3;put(shift,5,zz,'mangrove_planks');put(Math.min(5,shift+2),5,zz,'mangrove_planks');box([shift,2,zz],[shift,3,zz],'mangrove_planks');}
  box([1,5,2],[3,5,2],'mangrove_planks');box([3,5,12],[5,5,12],'mangrove_planks');
  mark('diagonal-boat-belly-and-keel',[1,2,2],[5,5,12]);
  box([11,2,7],[13,2,9],'mud_bricks');box([12,3,8],[12,10,8],'stripped_mangrove_log');
  box([9,10,8],[13,10,8],'mangrove_log');box([10,7,8],[10,9,8],'stripped_mangrove_log');
  mark('single-arm-salvage-winch-frame',[9,2,7],[13,10,9]);
  box([10,1,13],[14,1,14],'mud_bricks');box([11,2,14],[12,2,14],'brown_terracotta');box([14,2,13],[14,2,14],'clay');
  mark('salvage-sorting-apron',[10,1,13],[14,2,14]);
 }
 if(spec.key==='addition/06'){
  for(const [cx,cz,stemTop,cap]of [[3,3,7,'brown_mushroom_block'],[12,12,4,'red_mushroom_block']]){
   box([cx-1,1,cz-1],[cx+1,1,cz+1],'mycelium');box([cx,2,cz],[cx,stemTop,cz],'mushroom_stem');
   box([cx-2,stemTop+1,cz-2],[cx+2,stemTop+1,cz+2],cap);box([cx-1,stemTop+2,cz-1],[cx+1,stemTop+2,cz+1],cap);
   mark('rooted-unequal-mushroom',[cx-2,1,cz-2],[cx+2,stemTop+2,cz+2]);
  }
  box([1,2,8],[2,2,12],'mud_bricks');box([1,3,8],[1,4,12],'glass');
  for(const zz of [8,10,12])put(2,3,zz,'brown_mushroom_block');
  mark('wall-side-spore-specimen-trough',[1,2,8],[2,4,12]);
  box([3,1,12],[6,1,14],'mangrove_planks');
  box([10,2,1],[14,2,2],'mangrove_planks');mark('observation-low-platform',[10,2,1],[14,2,3]);
 }
 for(let xx=1;xx<15;xx++)for(let zz=1;zz<15;zz++){const roofY=11+Math.floor(xx/4)%3;if((spec.key==='addition/01'&&zz<=5)||(spec.key==='addition/02'&&zz>=10)||(spec.key==='addition/03'&&xx<=5)||(spec.key==='addition/04'&&zz>=11)||(spec.key==='addition/05'&&zz>=13))put(xx,roofY,zz,'mangrove_planks');}
 if(spec.key==='addition/05'){
  for(const xx of [1,5,10,14])box([xx,2,14],[xx,11+Math.floor(xx/4)%3,14],'mangrove_log');
  box([1,11,14],[14,11,14],'mangrove_planks');box([8,12,14],[11,12,14],'mangrove_planks');mark('supported-salvage-store-canopy',[1,2,14],[14,13,14]);
 }
 if(spec.key==='addition/03'){
  for(const xx of [1,5])for(const zz of [1,14])box([xx,2,zz],[xx,11+Math.floor(xx/4)%3,zz],'mangrove_log');
  for(const zz of [1,14])box([1,11,zz],[5,11,zz],'mangrove_planks');
  mark('foundation-rooted-workshop-canopy',[1,2,1],[5,12,14]);
 }
 if(spec.key==='addition/04'){
  for(const xx of [1,5,10,14])box([xx,2,14],[xx,11+Math.floor(xx/4)%3,14],'mangrove_log');
  box([1,11,14],[14,11,14],'mangrove_planks');box([8,12,14],[11,12,14],'mangrove_planks');
  mark('supported-diagnostic-canopy',[1,2,14],[14,13,14]);
 }
 if(spec.key==='addition/07'){
  box([1,2,11],[5,2,14],'mud_bricks');
  for(const zz of [12,13])for(let xx=1;xx<=5;xx++)if(zz===12||xx<=3)put(xx,3,zz,'bamboo_block');
  box([1,4,12],[5,4,12],'mangrove_planks');
  mark('transverse-half-built-raft-on-dry-slipway',[1,2,11],[5,4,14]);
  for(const zz of [2,6])box([1,2,zz],[1,7,zz],'mangrove_log');
  for(const yy of [3,6])box([1,yy,2],[2,yy,6],'mangrove_planks');
  for(const zz of [2,4,6])box([2,4,zz],[2,5,zz],'bamboo_block');
  mark('slender-long-reed-bundle-rack',[1,2,2],[2,7,6]);
  box([2,2,7],[5,2,7],'mangrove_log');box([2,3,7],[5,3,7],'mangrove_planks');
  put(3,4,7,'bamboo_block');mark('L-return-tool-stools',[2,2,7],[5,4,7]);
 }
 if(spec.key==='addition/08'){
  for(const [x1,z1,x2,z2,top]of [[11,6,14,8,7],[1,9,2,11,5]]){
   for(const xx of [x1,x2])for(const zz of [z1,z2])box([xx,2,zz],[xx,top,zz],'mangrove_log');
   for(const yy of [3,top])box([x1,yy,z1],[x2,yy,z2],'mangrove_planks');
   put(x1,top+1,z1,'calcite');put(x2,top+1,z2,'prismarine');
   mark('staggered-small-relic-rack',[x1,2,z1],[x2,top+1,z2]);
  }
 }
 if(spec.key==='addition/09'){
  for(const [zz,top]of [[2,5],[6,8],[13,10]]){
   box([1,2,zz],[1,top,zz],'stripped_mangrove_log');
   for(let yy=2;yy<=top;yy++)put(2,yy,zz,yy%2===0?'calcite':'mangrove_log');
   mark('unequal-engraved-tide-post',[1,2,zz],[2,top,zz]);
  }
  for(const xx of [11,14])for(const zz of [10,14])box([xx,2,zz],[xx,7,zz],'mangrove_log');
  for(const yy of [3,6])box([11,yy,10],[14,yy,14],'mangrove_planks');
  box([12,4,12],[13,4,13],'white_wool');box([12,7,12],[13,7,13],'white_wool');
  mark('side-tide-record-cabinet',[11,2,10],[14,7,14]);
  for(const xx of [0,15])box([xx,3,3],[xx,10,11],'glass');
  mark('west-sealed-observation-window',[0,3,3],[0,10,11]);mark('east-sealed-observation-window',[15,3,3],[15,10,11]);
 }
 if(spec.key==='addition/10'){
  for(const [xx,zz]of [[2,2],[2,7],[5,13]])box([xx,2,zz],[xx,9,zz],'mangrove_log');
  box([2,9,2],[2,9,7],'mangrove_planks');box([2,9,7],[5,9,7],'mangrove_planks');box([5,9,7],[5,9,13],'mangrove_planks');
  mark('twice-bent-lantern-arbor',[2,2,2],[5,9,13]);
  for(const [xx,zz,yy]of [[2,4,6],[4,7,5],[5,11,7]]){
   for(let chainY=yy+1;chainY<9;chainY++)put(xx,chainY,zz,'chain[axis=y,waterlogged=false]');
   put(xx,yy,zz,'lantern[hanging=true,waterlogged=false]');mark('supported-hanging-lantern',[xx,yy,zz],[xx,9,zz]);
  }
  box([11,2,6],[14,2,9],'mangrove_roots');box([14,2,6],[14,2,9],'mangrove_planks');
  mark('side-woven-root-seating-mat',[11,2,6],[14,2,9]);
  for(const xx of [11,14])for(const zz of [1,2])put(xx,2,zz,'mud_bricks');
  box([11,3,1],[14,3,2],'mangrove_planks');box([12,3,1],[13,3,2],'white_wool');put(12,4,1,'calcite');
  mark('independent-low-guardian-altar',[11,2,1],[14,4,2]);
 }
 const targets=variant.ports.flatMap(port=>port.cells.map(point=>({point,clearance:5,kind:'port'})));
 for(const socket of variant.sockets)for(const point of [socket.position,socket.spawn_position])if(point)targets.push({point,clearance:3,kind:'socket',id:socket.id});
 const occupiedTargets=new Set(targets.map(target=>target.point.join(','))),extraFeet=[];
 for(const socket of variant.sockets){const [xx,yy,zz]=socket.position;const point=[[1,0],[-1,0],[0,1],[0,-1]].map(([dx,dz])=>[xx+dx,yy,zz+dz]).find(point=>point[0]>0&&point[0]<15&&point[2]>0&&point[2]<15&&!occupiedTargets.has(point.join(',')));if(!point)throw Error('Independent foot');occupiedTargets.add(point.join(','));targets.push({point,clearance:3,kind:'independent-entity'});extraFeet.push({socket:socket.id,point});}
 const carve=(xx,zz,y1=2,y2=4)=>{for(let yy=y1;yy<=y2;yy++)put(xx,yy,zz,air);put(xx,y1-1,zz,'mangrove_planks');};
 for(let xx=6;xx<=9;xx++)for(let zz=0;zz<16;zz++)carve(xx,zz,2,6);
 for(const target of targets){const [xx,yy,zz]=target.point;for(let xline=Math.min(xx,7);xline<=Math.max(xx,7);xline++)for(let offset=-1;offset<=1;offset++){const row=zz+offset;if(row>0&&row<15&&xline>0&&xline<15)carve(xline,row);}put(xx,yy-1,zz,'mangrove_planks');for(let offset=0;offset<target.clearance;offset++)put(xx,yy+offset,zz,air);}
 const portBand=[],waterCells=[];
 if(spec.key==='addition/09'){
  box([3,2,1],[5,2,3],'mangrove_planks');
  for(let xx=3;xx<=5;xx++)put(xx,2,4,'mangrove_slab[type=bottom,waterlogged=false]');
  mark('offcenter-stepped-observation-platform',[3,2,1],[5,2,4]);
 }
 if(spec.key==='addition/07'){
  box([12,1,2],[14,1,13],'mud_bricks');box([13,0,2],[13,0,13],'mud_bricks');
  for(let zz=3;zz<=12;zz++){put(13,1,zz,'water[level=0]');put(13,2,zz,'glass');waterCells.push([13,1,zz]);}
  put(13,2,2,'mud_bricks');put(13,2,13,'mud_bricks');
  mark('sealed-shallow-perimeter-water-channel',[12,0,2],[14,2,13]);
 }
 if(spec.key==='addition/08'){
  for(const [cx,cz,waterY]of [[2,2,6],[3,7,4],[4,13,2]]){
   box([cx-1,2,cz-1],[cx+1,waterY,cz+1],'mud_bricks');
   for(let xx=cx-1;xx<=cx+1;xx++)for(let zz=cz-1;zz<=cz+1;zz++)put(xx,waterY,zz,'glass');
   put(cx,waterY,cz,'water[level=0]');box([cx-1,waterY+1,cz-1],[cx+1,waterY+1,cz+1],'glass');waterCells.push([cx,waterY,cz]);
   mark('descending-sealed-wash-trough',[cx-1,2,cz-1],[cx+1,waterY+1,cz+1]);
  }
  for(let xx=4;xx<=9;xx++)for(let zz=9;zz<=11;zz++)carve(xx,zz);
 }
 if(spec.key==='addition/06'){
  box([10,2,1],[14,2,2],'mangrove_planks');
  for(let xx=10;xx<=12;xx++)put(xx,2,3,'mangrove_slab[type=bottom,waterlogged=false]');
  mark('three-wide-half-step-access',[10,2,1],[14,2,3]);
 }
 if(spec.key==='addition/01'){
  box([10,2,1],[13,2,2],'mangrove_planks');
  for(let xx=10;xx<=12;xx++)put(xx,2,3,'mangrove_slab[type=bottom,waterlogged=false]');
  mark('socket-safe-repositioned-platform-step',[10,2,1],[13,2,3]);
 }
 for(const port of variant.ports)for(const [xx,yy,zz]of port.cells)for(let inward=0;inward<=5;inward++){const pz=zz===0?inward:zz===15?15-inward:zz,px=xx===0?inward:xx===15?15-inward:xx;for(let offset=0;offset<5;offset++){put(px,yy+offset,pz,air);portBand.push([px,yy+offset,pz]);}put(px,yy-1,pz,'mangrove_planks');}
 return {spec,variant,size,states,get,landmarks,targets,extraFeet,portBand,waterCells};
}
const parsed=new Map();
function encode(built){const structure=new Structure(built.size);for(let yy=0;yy<16;yy++)for(let zz=0;zz<16;zz++)for(let xx=0;xx<16;xx++){const text=built.get(xx,yy,zz);if(!parsed.has(text))parsed.set(text,BlockState.parse(text));const state=parsed.get(text);structure.addBlock([xx,yy,zz],state.getName().toString(),state.getProperties());}return structure.writeNbt({name:'',dataVersion:3465,compression:'gzip'});}
function decode(bytes){const nbt=NbtFile.read(bytes).root,structure=Structure.fromNbt(nbt),size=structure.getSize(),states=new Map();for(const block of structure.getBlocks()){const key=block.pos.join(',');if(states.has(key)||block.nbt)throw Error('Duplicate/block entity');if(block.pos.some((value,axis)=>value<0||value>=size[axis]))throw Error('Bounds');states.set(key,block.state.toString());}if(states.size!==size.reduce((product,value)=>product*value,1))throw Error('Dense');return {nbt,size,states,get:(xx,yy,zz)=>states.get([xx,yy,zz].join(','))??'minecraft:bedrock'};}
function validate(built,decoded){
 const {get}=decoded,solid=state=>state!==air&&!state.includes('slab')&&!state.includes('roots')&&!state.startsWith('minecraft:water')&&!state.startsWith('minecraft:chain')&&!state.startsWith('minecraft:lantern');
 if(decoded.nbt.getNumber('DataVersion')!==3465||!decoded.nbt.hasList('entities',undefined,0))throw Error('NBT schema');
 const legal=new Set(['air','packed_mud','mud_bricks','glass','mangrove_planks','mangrove_log','stripped_mangrove_log','brown_terracotta','green_terracotta','mangrove_slab','mangrove_roots','clay','white_wool','mycelium','mushroom_stem','brown_mushroom_block','red_mushroom_block','bamboo_block','calcite','prismarine','water','chain','lantern'].map(name=>'minecraft:'+name));
 decoded.nbt.getList('palette',10).forEach(entry=>{if(!legal.has(entry.getString('Name')))throw Error('Vanilla palette');if(entry.has('Properties')&&!entry.hasCompound('Properties'))throw Error('Properties');});
 for(let yy=0;yy<16;yy++)for(let zz=0;zz<16;zz++)for(let xx=0;xx<16;xx++)if(get(xx,yy,zz)!==built.get(xx,yy,zz))throw Error('Roundtrip');
 for(const {point:[xx,yy,zz],clearance}of built.targets){if(!solid(get(xx,yy-1,zz)))throw Error('Target support');for(let offset=0;offset<clearance;offset++)if(get(xx,yy+offset,zz)!==air)throw Error('Target air');}
 for(const point of built.portBand)if(get(...point)!==air)throw Error('Five deep port');
 for(const [xx,yy,zz]of built.waterCells){if(get(xx,yy,zz)!=='minecraft:water[level=0]')throw Error('Water source');for(const [dx,dy,dz]of [[1,0,0],[-1,0,0],[0,1,0],[0,-1,0],[0,0,1],[0,0,-1]]){const neighbor=get(xx+dx,yy+dy,zz+dz);if(neighbor!=='minecraft:water[level=0]'&&!solid(neighbor))throw Error('Water not enclosed '+[xx,yy,zz]);}}
 const allowed=new Set(built.variant.ports.flatMap(port=>port.cells.flatMap(([xx,yy,zz])=>Array.from({length:5},(_,offset)=>[xx,yy+offset,zz].join(',')))));
 for(let yy=0;yy<16;yy++)for(let zz=0;zz<16;zz++)for(let xx=0;xx<16;xx++)if((xx===0||xx===15||yy===0||yy===15||zz===0||zz===15)&&get(xx,yy,zz)===air&&!allowed.has([xx,yy,zz].join(',')))throw Error('Shell');
 const surfaces=new Set();for(let yy=1;yy<13;yy++)for(let zz=0;zz<16;zz++)for(let xx=0;xx<16;xx++){const support=get(xx,yy-1,zz),feet=support.includes('slab')?yy-.5:solid(support)?yy:null;if(feet===null)continue;let clear=true;for(let body=Math.ceil(feet);body<feet+2.8;body++)if(get(xx,body,zz)!==air)clear=false;if(clear)surfaces.add([xx,feet,zz].join(','));}
 const first=built.targets[0].point,seen=new Set([first.join(',')]),queue=[first];for(let cursor=0;cursor<queue.length;cursor++){const [xx,yy,zz]=queue[cursor];for(const [dx,dz]of [[1,0],[-1,0],[0,1],[0,-1]])for(const dy of [-.5,0,.5]){const next=[xx+dx,yy+dy,zz+dz],key=next.join(',');if(surfaces.has(key)&&!seen.has(key)){seen.add(key);queue.push(next);}}}for(const target of built.targets)if(!seen.has(target.point.join(',')))throw Error('Disconnected '+target.point);
 if(built.spec.key==='addition/01'&&!seen.has('11,3,2'))throw Error('Raised deck inaccessible');
 if(built.spec.key==='addition/06'&&!seen.has('11,3,2'))throw Error('Observation platform inaccessible');
 if(built.spec.key==='addition/09'&&!seen.has('4,3,2'))throw Error('Tide observation platform inaccessible');
 const rooted=new Set(),rootQueue=[];
 for(let xx=1;xx<15;xx++)for(let zz=1;zz<15;zz++)if(!get(xx,1,zz).startsWith('minecraft:water')){rooted.add([xx,1,zz].join(','));rootQueue.push([xx,1,zz]);}
 for(let cursor=0;cursor<rootQueue.length;cursor++){const [xx,yy,zz]=rootQueue[cursor];for(const [dx,dy,dz]of [[1,0,0],[-1,0,0],[0,1,0],[0,-1,0],[0,0,1],[0,0,-1]]){const next=[xx+dx,yy+dy,zz+dz],label=next.join(',');if(next[0]<1||next[0]>14||next[1]<1||next[1]>14||next[2]<1||next[2]>14||get(...next)===air||get(...next).startsWith('minecraft:water')||rooted.has(label))continue;rooted.add(label);rootQueue.push(next);}}
 for(let xx=1;xx<15;xx++)for(let yy=2;yy<15;yy++)for(let zz=1;zz<15;zz++)if(get(xx,yy,zz)!==air&&!get(xx,yy,zz).startsWith('minecraft:water')&&!rooted.has([xx,yy,zz].join(',')))throw Error('Unrooted interior block '+[xx,yy,zz]);
 return {status:'PASS',size:decoded.size,minimum_y:built.variant.minimum_y,ports:built.variant.ports,sockets:built.variant.sockets,spawn_position:built.variant.spawn_position??null,denseBlocks:decoded.states.size,dataVersion:3465,checks:{dense:true,noEntitiesOrBlockEntities:true,bareVanillaPalette:true,exactRoundtrip:true,shellSealed:true,allInternalSolidsFoundationConnected:true,portsFiveHighAndFiveDeep:true,fullSupports:true,sourceWaterSixFacesEnclosed:true,socketsThreeAir:true,halfStepConnected:true,tideObservationPlatform:built.spec.key!=='addition/09'||seen.has('4,3,2'),observationPlatform:built.spec.key!=='addition/06'||seen.has('11,3,2'),raisedWorkdeck:built.spec.key!=='addition/01'||seen.has('11,3,2')},sealedWaterSourceCells:built.waterCells,independentEntityFeet:built.extraFeet,reachableSurfaceCells:seen.size};
}
function interior(decoded,variant){
 const base=variant.key?.includes('/layout/')?1:variant.key==='spawn/spawn'?4:2,[width,height,depth]=decoded.size,solid=new Set(),footprint=new Set(),histogram={};
 for(const [key,state]of decoded.states){const [xx,yy,zz]=key.split(',').map(Number);if(state===air||state.startsWith('minecraft:water')||xx===0||xx===width-1||zz===0||zz===depth-1||yy<base||yy>=Math.min(height-2,base+7))continue;if(variant.ports.some(port=>port.cells.some(([px,py,pz])=>Math.abs(xx-px)+Math.abs(zz-pz)<=5&&yy>=py-1&&yy<=py+4)))continue;solid.add(`${xx},${yy-base},${zz}`);footprint.add(`${xx},${zz}`);histogram[state]=(histogram[state]??0)+1;}
 const normalized=new Set();for(const key of solid){const [xx,yy,zz]=key.split(',').map(Number);normalized.add([Math.floor((xx-1)*14/(width-2)),yy,Math.floor((zz-1)*14/(depth-2))].join(','));}
 return {solid,footprint,histogram,normalized,signature:sha([...solid].sort().join(';'))};
}
function compare(first,second){let intersection=0;for(const value of first)if(second.has(value))intersection++;return first.size+second.size===0?1:intersection/(first.size+second.size-intersection);}
function auditStyle(room){
 const {built,decoded}=room,histogram={};for(const state of decoded.states.values())histogram[state]=(histogram[state]??0)+1;
 const features=built.landmarks.map(landmark=>{const {min,max}=landmark.requestedBBox,points=[],materials={};for(let yy=min[1];yy<=max[1];yy++)for(let zz=min[2];zz<=max[2];zz++)for(let xx=min[0];xx<=max[0];xx++){const state=decoded.get(xx,yy,zz);if(state===air)continue;points.push([xx,yy,zz]);materials[state]=(materials[state]??0)+1;}if(points.length<(landmark.name==='supported-hanging-lantern'?3:4))throw Error('Lost feature '+landmark.name);return {...landmark,actualSolidBlocks:points.length,actualBBox:{min:[0,1,2].map(axis=>Math.min(...points.map(point=>point[axis]))),max:[0,1,2].map(axis=>Math.max(...points.map(point=>point[axis])))},materials,coordinatesSha256:sha(JSON.stringify(points))};});
 if(built.spec.key==='addition/01'){if(decoded.get(3,9,3)!=='minecraft:mangrove_planks'||decoded.get(12,6,12)!=='minecraft:mangrove_planks')throw Error('Unequal presses');}
 if(built.spec.key==='addition/02'&&!(histogram['minecraft:white_wool']>0&&histogram['minecraft:clay']>0))throw Error('Paper landmark');
 if(built.spec.key==='addition/09'){
  for(const [zz,top]of [[2,5],[6,8],[13,10]])if(decoded.get(1,top,zz)!=='minecraft:stripped_mangrove_log'||decoded.get(1,top+1,zz)!==air)throw Error('Unequal tide gauge heights');
  for(const xx of [0,15])for(let yy=3;yy<=10;yy++)for(let zz=3;zz<=11;zz++)if(decoded.get(xx,yy,zz)!=='minecraft:glass')throw Error('Observation window gap');
  if((histogram['minecraft:water[level=0]']??0)!==0)throw Error('Wet observation floor');
  return {key:built.spec.key,status:'PASS',nbtSha256:room.sha256,actualMaterials:histogram,actualThemeStructures:features,functionalShape:built.spec.layout,review:'西墙刻柱分别到y5/8/10，原木与方解石交替刻度形成真实高差；偏西北观测台通过三格半砖阶抵达，东南双层记录柜收纳白色卷册；两侧大玻璃窗完整封闭。',fourDirectionReview:'北南与东西四向看到不同刻柱高度、偏心小台与另一侧记录柜；无水方块，主路保持干燥，窗不挪动南北接口。',liquidPolicy:'No water or other liquids; every floor/port route remains dry.',corrections:['Off-center deck added after original socket-lane carving; step x3..5,z4 leads to actual observation foot [4,3,2].','Window boundaries independently checked as full glass at x0/15,y3..10,z3..11.']};
 }
 if(built.spec.key==='addition/10'){
  for(const [xx,zz,yy]of [[2,4,6],[4,7,5],[5,11,7]]){
   if(decoded.get(xx,yy,zz)!=='minecraft:lantern[hanging=true,waterlogged=false]')throw Error('Missing hanging lantern');
   for(let chainY=yy+1;chainY<9;chainY++)if(decoded.get(xx,chainY,zz)!=='minecraft:chain[axis=y,waterlogged=false]')throw Error('Unattached lantern chain');
   if(decoded.get(xx,9,zz)!=='minecraft:mangrove_planks')throw Error('Unattached lantern beam');
  }
  if([...decoded.states.values()].some(state=>state.includes('mushroom')))throw Error('Forbidden mushroom centerpiece');
  return {key:built.spec.key,status:'PASS',nbtSha256:room.sha256,actualMaterials:histogram,actualThemeStructures:features,functionalShape:built.spec.layout,review:'木架在[2,9,7]和[5,9,7]两次弯折，三盏实际灯笼位于y6/5/7并以吊链挂接；东侧根席与东北独立矮桌分开布置，庭院中心留视线和交错通路，不使用中央蘑菇。',fourDirectionReview:'四向可分辨弯架长短边、不同灯笼下垂高度、侧根席和独立低祭桌；透明窗壳隐藏规则不影响独立封壳验证，socket安全带位于灯架下方或侧面。',liquidPolicy:'Dry courtyard, no fluids; actual native hanging lantern and chain states.',corrections:['Lanterns and chains explicitly excluded from full support surfaces in traversal validation.','Every hanging lantern chain is verified cell-by-cell up to its beam; beams connect to grounded posts.','Low altar kept northeast of central route, independent of root mat and arbor.']};
 }
 if(built.spec.key==='addition/07'){
  if(decoded.get(5,3,12)!=='minecraft:bamboo_block'||decoded.get(3,3,13)!=='minecraft:bamboo_block'||decoded.get(4,3,13)=== 'minecraft:bamboo_block')throw Error('Raft not half-finished');
  if(decoded.get(1,7,2)!=='minecraft:mangrove_log'||decoded.get(3,3,7)!=='minecraft:mangrove_planks')throw Error('L-shaped workbelt');
  if(built.waterCells.length!==10)throw Error('Perimeter water missing');
  return {key:built.spec.key,status:'PASS',nbtSha256:room.sha256,actualMaterials:histogram,actualThemeStructures:features,functionalShape:built.spec.layout,review:'西南横向竹筏由完整一排和未完半排竹节构成，落在干燥泥砖船台；西侧细长苇束架转接工具凳成L形，东侧覆玻璃浅沟与独立旱路分离。',fourDirectionReview:'北南可辨横筏宽度、缺失半排与船台边沿，东西辨出苇架长边和工具凳短边；浅沟在玻璃盖下连续但不接两端门洞。',liquidPolicy:'Ten static source-water blocks at x13,y1,z3..12; glass above, full mud bottom and sides, solid end caps. Six faces checked in final decoded NBT.',corrections:['Shallow water installed after socket passage carving, on east side outside all target feet and beneath glass covers.','Main north-south dry lane has four cells width; no water on any interface or dry route.','Added grounded mud end caps at x13,y2,z2/13 supporting the glass cover, then checked structural connectivity excluding water as a support.']};
 }
 if(built.spec.key==='addition/08'){
  const expected=[[2,6,2],[3,4,7],[4,2,13]];
  for(const point of expected)if(decoded.get(...point)!=='minecraft:water[level=0]')throw Error('Descending wash stage');
  if(histogram['minecraft:calcite']!==2||histogram['minecraft:prismarine']!==2)throw Error('Relic displays');
  return {key:built.spec.key,status:'PASS',nbtSha256:room.sha256,actualMaterials:histogram,actualThemeStructures:features,functionalShape:built.spec.layout,review:'三个泥砖基座内的玻璃封闭洗槽按[2,6,2]→[3,4,7]→[4,2,13]斜向逐级下降；东西遗物架错位，方解石/海晶块标本不重复使用其他房机械骨架。',fourDirectionReview:'北南有高、中、低三个槽的分层轮廓，东西可区分两侧小遗物架；低槽北侧另扩三格干燥支路，四个socket与端口均保持通行。',liquidPolicy:'Three independent level0 water samples fully enclosed in native glass/mud troughs, no exposed flow or connection to ports.',corrections:['Low trough placed beyond socket line z11; north-side dry connector widened at x4..9,z9..11 after basin construction.','Checked exact source coordinates and six full neighbors after all carve operations.','Expanded each wash-tank glass lid to a full 3x3 plate attached to its side rim; water excluded from structural support traversal.']};
 }
 if(built.spec.key==='addition/05'){
  for(const zz of [2,5,8,12])if(!['minecraft:mangrove_log','minecraft:mangrove_planks'].includes(decoded.get(zz<6?1:zz<10?2:3,5,zz)))throw Error('Missing ship rib rim');
  if(decoded.get(12,8,8)!=='minecraft:stripped_mangrove_log'||decoded.get(10,10,8)!=='minecraft:mangrove_log')throw Error('Derrick');
  return {key:built.spec.key,status:'PASS',nbtSha256:room.sha256,actualMaterials:histogram,actualThemeStructures:features,functionalShape:built.spec.layout,review:'四段U形船肋和偏移龙骨形成船腹轮廓，向东南斜置；东侧为单臂静态打捞绞架，南侧分拣坪留出货物，非钟械双柱卷扬骨架。',fourDirectionReview:'北南四道肋骨与船腹侧沿能区分，东西看到西侧沉船片与东侧高单臂；原接口只切开局部肋下工道，船侧上沿连续支承，基础连通检查防止浮块。',liquidPolicy:'No water, powered mechanisms, rails or block entities.',corrections:['Ship hull defined as shifted U ribs rather than reused warehouse frames.','Original socket approaches cut only required air zones; continuous upper gunwales preserve structural support.','Static salvage arm and overhead apron canopy are attached to grounded native timber posts.']};
 }
 if(built.spec.key==='addition/06'){
  if(decoded.get(3,2,3)!=='minecraft:mushroom_stem'||decoded.get(12,2,12)!=='minecraft:mushroom_stem'||decoded.get(3,9,3)!=='minecraft:brown_mushroom_block'||decoded.get(12,6,12)!=='minecraft:red_mushroom_block')throw Error('Unequal rooted mushrooms');
  if(decoded.get(3,1,3)!=='minecraft:mycelium'||decoded.get(12,1,12)!=='minecraft:mycelium')throw Error('Mycelium foot');
  return {key:built.spec.key,status:'PASS',nbtSha256:room.sha256,actualMaterials:histogram,actualThemeStructures:features,functionalShape:built.spec.layout,review:'西北褐蕈顶高9、东南红蕈顶高6，菌柄均落在菌丝土实体根脚；西墙标本槽与东北观察低台使用三格半砖台阶连接，菌盖只占对角。',fourDirectionReview:'北南/东西呈不同高度两菌盖，西侧玻璃标本槽与低台位置分离；东北踏步避开四个原刷怪口，最终NBT验证观察脚点[11,3,2]可步行抵达。',liquidPolicy:'Static native blocks only, no entities, fluids or growing mushroom mechanisms.',corrections:['Both mushroom canopies use one connected cap/stem/mycelium assembly; no floating decorative caps.','Observation platform step rebuilt after interface carving at x10..12,z3, preserving socket at x12,z4.','Mushroom block palette validated independently from generic redwood structure materials.']};
 }
 if(built.spec.key==='addition/03'){
  for(let yy=3;yy<=8;yy++)for(let xx=1;xx<=4;xx++)for(let zz=2;zz<=5;zz++)if((xx===1||xx===4||zz===2||zz===5)&&decoded.get(xx,yy,zz)!=='minecraft:glass')throw Error('Gas glass well punctured');
  for(const [xx,zz]of [[5,3],[5,12],[10,2],[10,13]])if(decoded.get(xx,0,zz)!=='minecraft:mangrove_log'||!['minecraft:mangrove_log','minecraft:mangrove_planks'].includes(decoded.get(xx,1,zz)))throw Error('Missing bridge foot');
  return {key:built.spec.key,status:'PASS',nbtSha256:room.sha256,actualMaterials:histogram,actualThemeStructures:features,functionalShape:built.spec.layout,review:'偏心玻璃井在西北，东北/西南两处实体泥基木罩成三角；红木桥脚落在完整基础，禁止用悬空或危险气体机制表现设备。',fourDirectionReview:'四向透明投影显示玻璃井内陶土与不同高度的侧收集罩；主道沿中轴贯通，接井操作面由三格支路连接，近剖墙规则明确。',liquidPolicy:'No fluids or real gas; inert enclosed glass and green terracotta represent storage.',corrections:['Compared exact well wall coordinates after original socket access carving; every glass side remains intact.','Four bridge feet have mangrove-log foundations y0 beneath full native log/plank decks at y1.','Added full-height grounded canopy posts and connecting eave beams; every internal nonair block reaches the foundation in six-neighbor support graph.']};
 }
 if(built.spec.key==='addition/04'){
  for(const zz of [6,9])for(let xx=11;xx<=13;xx++)if(decoded.get(xx,5,zz)!=='minecraft:glass')throw Error('Culture tank lid');
  if(decoded.get(3,3,12)!=='minecraft:white_wool'||decoded.get(4,3,12)!=='minecraft:white_wool')throw Error('Diagnostic table');
  return {key:built.spec.key,status:'PASS',nbtSha256:room.sha256,actualMaterials:histogram,actualThemeStructures:features,functionalShape:built.spec.layout,review:'西南三段药柜形成半弧围住偏西白色诊台；东侧封闭玻璃养殖缸与黏土内容物，中央屏风局部围合而不切断主路。',fourDirectionReview:'北南能辨两缸与偏西诊台，东西见药柜半弧和短木屏风；连续木板干道与四处分散刷怪接口经最终NBT BFS确认。',liquidPolicy:'No fluid mobs or water; sealed glass vessels with stable clay contents.',corrections:['Tank rows placed z6/9 between socket approach lanes z3..5 and z11..13; final lids checked individually.','Cabinet arc and diagnostic table are actual connected block volumes, not named generic decoration.','Added grounded canopy pillars and stepped eave ties; every internal nonair block is foundation-connected.']};
 }
 return {key:built.spec.key,status:'PASS',nbtSha256:room.sha256,actualMaterials:histogram,actualThemeStructures:features,functionalShape:built.spec.layout,review:built.spec.key==='addition/01'?'压榨柱框包裹工作体、两架高度9/6不同；横晒床与木台形成斜向工序，非四角柱空厅。':'浅黏土槽沿西侧分开；东侧白羊毛纸帘嵌入两级实木架，低泥浆桶位于横道两侧，非换色仓库。',fourDirectionReview:built.spec.key==='addition/01'?'北南和东西方向分别可区分高低压架，北侧木台与半格踏步已连通；近剖切隐藏的压架腿由平面和反向视图补查。':'北南可见侧槽中黏土浆与纸帘白色工作面，东西区分两架高度；横梁两端新增原木支撑，完整模型不悬空，剖切方向会隐藏近侧柱。',liquidPolicy:'No liquid used; shallow troughs hold stable clay pulp/dry peat. No flow or updates.',corrections:built.spec.key==='addition/01'?['Initial socket approach disconnected raised platform. Moved three-wide half-step landing to x10..12,z3 connecting raised deck x10..13,z1..2; final decoded-NBT BFS reaches [11,3,2].','Adjusted cardinal projection scale to avoid tall press clipping.']:['Added solid endpoint log supports at x2/13,z7,y2..10 for workshop beam.','All four original socket pockets and five-deep port lanes recarved and verified.']};
}
function tint(state){if(state==='minecraft:air')return '#10272b';if(state.startsWith('minecraft:lantern'))return '#ffd878';if(state.startsWith('minecraft:chain'))return '#63717b';if(state.startsWith('minecraft:water'))return '#4088b5';if(state.includes('bamboo'))return '#b1b06c';if(state.includes('calcite'))return '#e4dfcf';if(state.includes('prismarine'))return '#4ba398';if(state.includes('red_mushroom_block'))return '#d85152';if(state.includes('brown_mushroom_block'))return '#b6a281';if(state.includes('mushroom_stem'))return '#d5ceba';if(state.includes('mycelium'))return '#8e7b8c';if(state.includes('white_wool'))return '#e8e4c8';if(state.includes('clay'))return '#a7b3bb';if(state.includes('terracotta'))return '#77554b';if(state.includes('roots'))return '#776d48';if(state.includes('log'))return '#82444b';if(state.includes('planks')||state.includes('slab'))return '#b65f69';if(state.includes('mud_bricks'))return '#9b8f70';if(state.includes('glass'))return '#365c66';return '#5d6249';}
function fourViews(rooms){
 const payload=rooms.map(room=>({key:room.built.spec.key,name:room.built.spec.name,sha256:room.sha256,states:[...room.decoded.states]}));
 return `<!doctype html><html lang="zh"><meta charset="utf-8"><title>幽沼新增第5批 · 真实NBT四视图</title><style>body{background:#0d2228;color:#e9e6d1;font:15px system-ui;margin:24px auto;max-width:1400px;padding:0 20px}p{color:#b9cbba;line-height:1.6}article{background:#203b3e;border:1px solid #55685d;border-radius:12px;padding:16px;margin:20px 0}h2{font-size:22px}small{font-size:11px;color:#d1b292}.views{display:grid;grid-template-columns:repeat(4,1fr);gap:12px}figure{margin:0}canvas{width:100%;aspect-ratio:1.1;background:#10272b}figcaption{margin:6px 0}.plan{display:grid;grid-template-columns:1fr 1fr;gap:12px;max-width:600px}input{width:65%}</style><h1>幽沼新增 · 第5批2房</h1><p>从最终导出密集NBT解码。北向+Z、南向−Z、西向+X、东向−X，四个独立观察方向。隐藏最外层壳体与 y≥11 的屋顶；每方向剖去最近3格高于地面的方块，保留地板，露出工序/工作架。为辨识内部使用统一高俯角。下附不同高度真实平面切片，补看剖切隐藏的地标；完整壳体由独立验证检查。简化方块投影，非游戏截图。</p><main></main><script>const rooms=${JSON.stringify(payload)};const tint=${tint.toString()};
for(const room of rooms){const states=new Map(room.states),article=document.createElement('article');article.dataset.key=room.key;article.innerHTML='<h2>'+room.key+' · '+room.name+'</h2><small>NBT SHA256 '+room.sha256+'</small><div class="views"></div><div class="plan"></div>';document.querySelector('main').append(article);const get=(xx,yy,zz)=>states.get([xx,yy,zz].join(','));for(const [direction,label]of [['north','北 N → +Z'],['south','南 S → −Z'],['west','西 W → +X'],['east','东 E → −X']]){const figure=document.createElement('figure');figure.innerHTML='<figcaption>'+label+'</figcaption><canvas width="660" height="600"></canvas>';article.querySelector('.views').append(figure);const canvas=figure.querySelector('canvas');canvas.dataset.key=room.key;canvas.dataset.direction=direction;const context=canvas.getContext('2d');context.fillStyle='#10272b';context.fillRect(0,0,660,600);const blocks=[];for(let yy=1;yy<11;yy++)for(let zz=1;zz<15;zz++)for(let xx=1;xx<15;xx++){const state=get(xx,yy,zz);if(state==='minecraft:air')continue;let rx,rz;if(direction==='north'){rx=xx;rz=zz;}if(direction==='south'){rx=15-xx;rz=15-zz;}if(direction==='west'){rx=15-zz;rz=xx;}if(direction==='east'){rx=zz;rz=15-xx;}if(rz<4&&yy>1)continue;blocks.push({rx,rz,yy,state});}blocks.sort((left,right)=>right.rz-left.rz||left.yy-right.yy||left.rx-right.rx);for(const {rx,rz,yy,state}of blocks){const unit=36,sx=42+rx*unit,sy=570-(yy-1)*unit-rz*unit*.62,high=state.includes('slab')?unit*.5:unit;context.fillStyle=tint(state);context.globalAlpha=state.includes('glass')?.25:.78;context.fillRect(sx,sy-high,unit+.2,high);context.globalAlpha=state.includes('glass')?.35:1;context.fillRect(sx,sy-high-unit*.62,unit,unit*.62);context.strokeStyle='rgba(6,16,19,.22)';context.lineWidth=.65;context.strokeRect(sx,sy-high,unit,high);}}for(const initial of [2,6]){const figure=document.createElement('figure');figure.innerHTML='<figcaption>真实水平切片 y <input min="0" max="15" type="range" value="'+initial+'"><b>'+initial+'</b></figcaption><canvas width="480" height="480"></canvas>';article.querySelector('.plan').append(figure);const canvas=figure.querySelector('canvas'),slider=figure.querySelector('input'),context=canvas.getContext('2d');const paint=()=>{const yy=+slider.value;figure.querySelector('b').textContent=yy;for(let zz=0;zz<16;zz++)for(let xx=0;xx<16;xx++){context.fillStyle=tint(get(xx,yy,zz));context.fillRect(xx*30,zz*30,30,30);}};slider.oninput=paint;paint();}}
</script></html>`.replace('const unit=36','const unit=27');
}
async function main(){
 const preservationBefore=verifyPreserved();
 if(process.argv.slice(2).some(argument=>argument!=='--batch=5'&&argument!=='--batch=05'))throw Error('Only batch 05 is authorized; previous batches are frozen');
 fs.mkdirSync(output,{recursive:true});
 fs.writeFileSync(path.join(output,'recipe-source.mjs'),fs.readFileSync(fileURLToPath(import.meta.url)));
 fs.writeFileSync(path.join(output,'design.md'),'# 幽沼新增第5批\n\n仅授权addition/09与addition/10。运行 `node tools/expand-swamp.mjs --batch=5`。不写旧27房、不写正式资源。快照可直接运行 `node output/room-expansion-v035/swamp/batch-05/recipe-source.mjs --batch=5`，自动向上定位catalog工程根；快照和活动配方字节一致并列入freeze清单。\n\n'+specs.map(spec=>`## ${spec.key} ${spec.name}\n\n接口完整继承 ${spec.baseKey}。${spec.layout}。实际地标：${spec.features.join('、')}。干燥实体地板、封壳玻璃采光，完全新坐标，不提取旧建筑。\n`).join('\n')+'\n测潮记录室全程干燥、观察窗玻璃封闭；灯笼庭使用实际悬挂灯笼与竖向吊链，不使用中央蘑菇或液体。所有端口向内五格保留五格高净空。四个socket不挪动，另留独立实体脚点。\n');
 const rooms=[];
 for(const spec of specs){const built=build(spec),bytes=encode(built),decoded=decode(bytes),checks=validate(built,decoded),file='roomset/'+spec.key+'.nbt';fs.mkdirSync(path.dirname(path.join(output,file)),{recursive:true});fs.writeFileSync(path.join(output,file),bytes);const room={built,decoded,sha256:sha(bytes),file,checks};room.interior=interior(decoded,built.variant);room.style=auditStyle(room);rooms.push(room);console.log('PASS '+spec.key+' '+room.sha256);}
 const templates=rooms.map(room=>({...room.built.spec,features:undefined,layout:undefined,sha256:room.sha256,file:room.file}));
 json('manifest.json',{schema:1,style:'fteclipsegen:swamp',batch,templates});
 json('validation.json',{status:'PASS',schema:1,catalogSha256:sha(fs.readFileSync(path.join(data,'catalog.json'))),manifestSha256:sha(fs.readFileSync(path.join(output,'manifest.json'))),rooms:rooms.map(room=>({key:room.built.spec.key,nbtSha256:room.sha256,...room.checks})),limits:'Offline supported-surface BFS with <=0.5 elevation steps, not in-game testing.'});
 json('style-audit.json',{status:'PASS',schema:1,rooms:rooms.map(room=>room.style)});
 const comparisons=[],references=[];
 for(const variant of variants){const file=path.join(root,'output/style-rebuild-v034/swamp/roomset',variant.key+'.nbt'),bytes=fs.readFileSync(file),decoded=decode(bytes);references.push({key:'v034/'+variant.key,sha256:sha(bytes),variant,decoded,interior:interior(decoded,variant),layout:'Existing v034 '+variant.key});}
 const parentDir=path.dirname(output);for(const folder of fs.readdirSync(parentDir)){if(!/^batch-\d\d$/.test(folder)||Number(folder.slice(-2))>=batch)continue;const manifestFile=path.join(parentDir,folder,'manifest.json');if(!fs.existsSync(manifestFile))continue;const manifest=JSON.parse(fs.readFileSync(manifestFile,'utf8'));for(const template of manifest.templates){const bytes=fs.readFileSync(path.join(parentDir,folder,template.file)),decoded=decode(bytes),variant=variants.find(variant=>variant.key===template.baseKey);references.push({key:folder+'/'+template.key,sha256:sha(bytes),variant,decoded,interior:interior(decoded,variant),layout:template.name});}}
 for(const room of rooms)for(const reference of references){const sameSize=room.decoded.size.join()===reference.decoded.size.join(),first=room.interior,second=reference.interior,score=compare(sameSize?first.solid:first.normalized,sameSize?second.solid:second.normalized);let rotationMax=score;if(sameSize){let rotated=second.solid;for(let turn=1;turn<4;turn++){rotated=new Set([...rotated].map(value=>{const [xx,yy,zz]=value.split(',').map(Number);return [15-zz,yy,xx].join(',');}));rotationMax=Math.max(rotationMax,compare(first.solid,rotated));}}if(rotationMax>=.85)throw Error('High repetition '+reference.key);comparisons.push({key:room.built.spec.key,reference:reference.key,referenceSha256:reference.sha256,sameSize,solidAirJaccard:score,maximumQuarterTurnJaccard:rotationMax,layoutDifference:room.built.spec.layout+' / compared with '+reference.layout,conclusion:'PASS distinct occupied volumes and process layout'});}
 const internalScore=compare(rooms[0].interior.solid,rooms[1].interior.solid);let internalRotation=internalScore,rotated=rooms[1].interior.solid;for(let turn=1;turn<4;turn++){rotated=new Set([...rotated].map(value=>{const [xx,yy,zz]=value.split(',').map(Number);return [15-zz,yy,xx].join(',');}));internalRotation=Math.max(internalRotation,compare(rooms[0].interior.solid,rotated));}if(internalRotation>=.85)throw Error('Batch duplicate');comparisons.push({key:specs[0].key,reference:specs[1].key,solidAirJaccard:internalScore,maximumQuarterTurnJaccard:internalRotation,layoutDifference:'Unequal tide gauges with offset observation deck and side archive versus a twice-bent hanging-lantern arbor with root seating and independent low altar',conclusion:'PASS'});comparisons.sort((left,right)=>(right.maximumQuarterTurnJaccard??right.solidAirJaccard)-(left.maximumQuarterTurnJaccard??left.solidAirJaccard));
 json('repetition-audit.json',{status:'PASS',schema:1,threshold:.85,exclusions:'Outer x/z boundary, foundation y<base, shared roof y>=min(height-2,base+7), Manhattan radius5 port safety bands at portY-1..portY+4; Water contents and AIR agreement excluded.',normalization:'Same size uses original coordinates, unequal sizes normalize horizontal coordinates to14x14; base-relative heights retained. Same-size references additionally compare four rotations.',references:references.map(reference=>({key:reference.key,sha256:reference.sha256})),signatures:rooms.map(room=>({key:room.built.spec.key,sha256:room.sha256,interiorSha256:room.interior.signature,solidCells:room.interior.solid.size,materialHistogram:room.interior.histogram})),comparisonCount:comparisons.length,comparisons,highest:comparisons.slice(0,10),reviewRequired:[],batchInternalJaccard:internalScore});
 fs.writeFileSync(path.join(output,'four-views.html'),fourViews(rooms));
 const playwrightRequire=createRequire('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/package.json'),{chromium}=playwrightRequire('playwright'),browser=await chromium.launch({headless:true}),pageErrors=[],views=[];
 try{const page=await browser.newPage({viewport:{width:1480,height:1100}});page.on('pageerror',error=>pageErrors.push(error.message));await page.goto(pathToFileURL(path.join(output,'four-views.html')).href);const rendered=await page.evaluate(()=>[...document.querySelectorAll('.views canvas')].map(canvas=>{const pixels=canvas.getContext('2d').getImageData(0,0,canvas.width,canvas.height).data,colors=new Set();for(let offset=0;offset<pixels.length;offset+=4)colors.add(pixels[offset]+','+pixels[offset+1]+','+pixels[offset+2]);return {key:canvas.dataset.key,direction:canvas.dataset.direction,colors:colors.size,data:canvas.toDataURL('image/png').split(',')[1]};}));if(pageErrors.length||rendered.length!==8||rendered.some(view=>view.colors<3))throw Error('Four views browser');fs.mkdirSync(path.join(output,'views'),{recursive:true});for(const view of rendered){const bytes=Buffer.from(view.data,'base64'),file='views/'+view.key.replace('/','-')+'-'+view.direction+'.png';fs.writeFileSync(path.join(output,file),bytes);views.push({key:view.key,direction:view.direction,colors:view.colors,file,sha256:sha(bytes),nbtSha256:rooms.find(room=>room.built.spec.key===view.key).sha256});}for(const spec of specs)if(new Set(views.filter(view=>view.key===spec.key).map(view=>view.sha256)).size!==4)throw Error('Duplicate views');await page.evaluate(()=>{for(const slider of document.querySelectorAll('input')){slider.value='3';slider.dispatchEvent(new Event('input'));}});await page.screenshot({path:path.join(output,'contact-sheet.png'),fullPage:true});if(pageErrors.length)throw Error('Page errors');}finally{await browser.close();}
 json('self-audit.json',{status:'PASS',schema:1,preservationBefore,preservationAfter:verifyPreserved(),authorizedBatch:batch,roomCount:2,fourViewCount:8,pageErrors,manifestSha256:sha(fs.readFileSync(path.join(output,'manifest.json'))),validationSha256:sha(fs.readFileSync(path.join(output,'validation.json'))),styleAuditSha256:sha(fs.readFileSync(path.join(output,'style-audit.json'))),repetitionAuditSha256:sha(fs.readFileSync(path.join(output,'repetition-audit.json'))),rooms:rooms.map(room=>({key:room.built.spec.key,nbtSha256:room.sha256,views:views.filter(view=>view.key===room.built.spec.key),functionalReview:room.style.review,layout:room.built.spec.layout,status:'PASS',corrections:room.style.corrections})),browser:{actualExecution:true,canvasCount:8,allNonblank:true,allFourDirectionsDistinctPerRoom:true,pageErrors},limits:['Simplified actual-NBT block projection with documented quarter cutaways, not Minecraft game rendering.','No in-game mob pathfinding or gameplay execution.','Prior27 templates and official resources read-only; no future batch generated.']});
 const frozenFiles=['recipe-source.mjs','manifest.json','design.md','validation.json','four-views.html','style-audit.json','repetition-audit.json','self-audit.json','contact-sheet.png',...rooms.map(room=>room.file),...views.map(view=>view.file)];
 json('freeze.json',{status:'FROZEN_FOR_PARENT_INTEGRATION',schema:1,batch,style:'fteclipsegen:swamp',recipe:{file:'tools/expand-swamp.mjs',snapshot:'recipe-source.mjs',sha256:sha(fs.readFileSync(fileURLToPath(import.meta.url)))},manifestSha256:sha(fs.readFileSync(path.join(output,'manifest.json'))),files:frozenFiles.map(file=>({file,sha256:sha(fs.readFileSync(path.join(output,file)))})),officialResourcesModified:false,old27Modified:false,nextBatchAuthorized:false});
 const sums=[...frozenFiles,'freeze.json'].map(file=>`${sha(fs.readFileSync(path.join(output,file)))}  ${file}`).join('\n')+'\n';fs.writeFileSync(path.join(output,'SHA256SUMS.txt'),sums);console.log('FROZEN_FOR_PARENT_INTEGRATION batch05 rooms=2 views=8 maxJaccard='+(comparisons[0].maximumQuarterTurnJaccard??comparisons[0].solidAirJaccard)+' manifest='+sha(fs.readFileSync(path.join(output,'manifest.json'))));
}
if(process.argv[1]&&path.resolve(process.argv[1])===fileURLToPath(import.meta.url))await main();
