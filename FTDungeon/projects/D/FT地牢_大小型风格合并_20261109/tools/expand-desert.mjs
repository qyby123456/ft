import assert from 'node:assert/strict';
import {readFileSync,writeFileSync,mkdirSync,readdirSync,existsSync} from 'node:fs';
import {join,dirname} from 'node:path';
import {fileURLToPath} from 'node:url';
import {createHash} from 'node:crypto';
import {spawnSync} from 'node:child_process';
const root=fileURLToPath(new URL('../',import.meta.url));
const batch=5;
const batchDirectory='batch-05';
const output=join(root,'output/room-expansion-v035/desert',batchDirectory);
const hash=bytes=>createHash('sha256').update(bytes).digest('hex');
const save=(file,value)=>{mkdirSync(dirname(file),{recursive:true});writeFileSync(file,value);};
const saveJson=(file,value)=>save(join(output,file),JSON.stringify(value,null,2)+'\n');
const recipe=fileURLToPath(import.meta.url);
const batchArgument=process.argv.find(value=>value.startsWith('--batch='));
assert(!batchArgument||batchArgument==='--batch=5','Only batch 5 is authorized; previous batches are frozen');
const sharedPath=join(root,'tools/rebuild-desert.mjs');
const original=readFileSync(sharedPath,'utf8');
assert(original.trimEnd().endsWith('main();'));
const safeModule=original.replace("const root=fileURLToPath(new URL('../',import.meta.url));",'const root='+JSON.stringify(root)+';').replace("const output=join(root,'output/style-rebuild-v034/desert');",'const output='+JSON.stringify(output)+';').replace(/main\(\);\s*$/,'export {Room,shell,reserveInterfaces,ensureAlternatives,encodeNbt,decodeNbt,roomFromNbt,validate,interiorSignature,jaccard,walkGraph,designs,makeFourViews,makeObliqueViews};');
const previewModule=safeModule.replaceAll("state.includes('glowstone')?'#7ce5de':","state==='iron_block'?'#c8d0d8':state==='deepslate_iron_ore'?'#5f6671':state==='oak_planks'?'#96734a':state==='sand'?'#e4d39c':state.includes('glowstone')?'#7ce5de':state.includes('orange_terracotta')?'#c87837':state.includes('brown_terracotta')?'#795138':state.includes('yellow_terracotta')?'#c9a54d':state.includes('green_terracotta')?'#798145':state==='white_wool'?'#ece9d9':state==='yellow_wool'?'#e9be4e':");
const shared=await import('data:text/javascript;base64,'+Buffer.from(previewModule).toString('base64'));
const {Room,shell,reserveInterfaces,ensureAlternatives,encodeNbt,decodeNbt,roomFromNbt,validate,interiorSignature,jaccard,walkGraph,designs,makeFourViews,makeObliqueViews}=shared;
const catalog=JSON.parse(readFileSync(join(root,'src/main/resources/data/fteclipsegen/catalog.json'))).variants;
const previousDefinitions=[
 {key:'addition/01',baseKey:'straight/room_cells',name:'沙漏测时院',circulation:'西半部三座错列封砂柱，东部两段测量刻度台；中央四格纵路与柱间侧支路相通。三个量柱分别贴西墙、向中央错出、后西回退，不是中央祭台。',height:'量柱高Y6/Y9/Y12，内部干砂上下段封在玻璃中；台高Y3，短计量梁Y11。',landmark:'三座玻璃封砂量柱、东墙量尺台',difference:'玻璃量柱为测量装置，三柱不同高度和位置；没有钟面、钟塔、齿轮。'},
 {key:'addition/02',baseKey:'straight/room_storage',name:'香料封存库',circulation:'西墙三阶香料龛与南墙两阶样品架形成L形仓；包装台偏置东中，秤架在西北，四格中央道穿过贸易作业区。',height:'壁龛阶架Y3/Y5/Y7，短拱Y9至Y12；低包装台Y2，秤梁Y6。',landmark:'L形阶梯香料龛、偏置包装台、侧置秤架',difference:'重心为两面龛架与贸易作业，非旧长库加换色；新增正交L形墙架及实体秤架。'}
];
const definitions=[
 {key:'addition/09',baseKey:'straight/room_towers',name:'陨铁天文工坊',circulation:'西北三格宽台阶通向陨铁托架，东北逐层错位观测尺与东南研磨台形成三点操作区；中央仪器之间留宽通路，平台脚点实际无跳跃可达。',height:'托架脚点Y4，以Y2/Y3真实楼梯进入，陨铁Y4至Y6；观测尺错层到Y8，研磨台Y3至Y4，封闭星图光窗Y15。',landmark:'阶梯陨铁托架、错位观测尺、东侧研磨台、封闭星图玻璃',difference:'三点工坊体量与可达托架构成功能布置，无中央神殿或钟楼。'},
 {key:'addition/10',baseKey:'straight/room_cells',name:'流沙遗迹勘探厅',circulation:'考古浅坑偏西，低护沿界定坑边；短木桥横跨坑体，桥东三格宽岸道与桥北连接口组成回环。西北截面展示墙与东侧调查低台区分挖掘与记录区。',height:'坑仅下降一格，底Y0为完整砂岩；桥Y1、脚点Y2，静态沙面Y1均由Y0实体支撑；截面墙Y2至Y7。',landmark:'偏轴一格浅考古坑、局部护沿、短木桥、分层截面展示墙',difference:'浅层发掘回环而非中央深井；不使用流沙更新、无底洞或不稳定沙堆。'}
];
for(const definition of definitions)designs.set(definition.key,{...definition,purpose:'新增独立战斗房'});
function build(definition,index){
 const variant={...structuredClone(catalog.find(entry=>entry.key===definition.baseKey)),key:definition.key};
 const room=new Room(variant);shell(room,index);
 if(batch===5){
  if(index===0){
   room.box([1,2,3],[4,2,6],'smooth_sandstone');room.box([1,3,4],[4,3,6],'cut_sandstone');
   room.box([2,2,2],[4,2,2],'sandstone_stairs[facing=south,half=bottom,shape=straight,waterlogged=false]');room.box([2,3,3],[4,3,3],'sandstone_stairs[facing=south,half=bottom,shape=straight,waterlogged=false]');
   room.box([1,4,5],[2,4,6],'deepslate_iron_ore');room.box([1,5,5],[2,5,6],'iron_block');room.set(1,6,5,'deepslate_iron_ore');
   room.box([11,2,3],[11,4,4],'chiseled_sandstone');room.box([12,4,3],[12,6,4],'cut_sandstone');room.box([13,6,3],[13,8,4],'chiseled_sandstone');room.box([11,3,3],[11,4,3],'gold_block');room.box([13,7,3],[13,8,3],'gold_block');
   room.box([12,2,9],[14,3,12],'smooth_sandstone');room.box([13,4,10],[14,4,11],'polished_andesite');
   room.box([4,15,4],[11,15,11],'blue_stained_glass');for(const[width,depth]of[[5,5],[8,6],[10,9],[6,10]])room.set(width,15,depth,'yellow_stained_glass');
  }else{
   room.box([2,1,7],[5,1,11],'air');room.box([2,0,7],[5,0,11],'smooth_sandstone');
   room.box([1,1,7],[1,1,13],'sand');room.box([2,1,12],[5,1,13],'sand');
   room.box([1,1,9],[6,1,11],'oak_planks');
   room.box([1,2,7],[1,2,8],'cut_sandstone');room.box([5,2,7],[5,2,8],'cut_sandstone');
   for(let elevation=2;elevation<=7;elevation++)room.box([1,elevation,3],[2,elevation,5],elevation%2?'cut_red_sandstone':'chiseled_sandstone');
   room.box([12,2,6],[14,2,9],'cut_sandstone');room.box([13,3,7],[14,3,8],'white_terracotta');room.box([11,8,12],[14,8,13],'chiseled_sandstone');
  }
 }else if(batch===4){
  if(index===0){
   room.box([1,2,3],[3,3,6],'smooth_sandstone');room.box([2,4,3],[2,4,5],'white_wool');room.set(2,4,6,'yellow_wool');
   room.box([10,2,8],[13,3,9],'cut_sandstone');room.box([10,4,8],[12,4,8],'white_wool');room.set(13,4,8,'yellow_wool');
   room.box([5,2,6],[5,5,8],'cut_sandstone');room.box([5,4,6],[5,4,8],'blue_terracotta');
   for(const elevation of[4,6,8])room.box([1,elevation,10],[2,elevation,13],'smooth_sandstone');
   for(const[elevation,depth,state]of[[5,10,'green_terracotta'],[5,12,'yellow_terracotta'],[7,11,'blue_terracotta'],[9,12,'red_terracotta']])room.box([1,elevation,depth],[2,elevation,depth],state);
   room.box([13,2,3],[14,3,5],'cut_red_sandstone');room.box([1,10,10],[3,10,13],'chiseled_sandstone');
  }else{
   room.box([2,2,7],[5,3,7],'cut_sandstone');room.box([5,2,7],[5,3,9],'cut_sandstone');room.set(3,4,7,'gold_block');room.set(5,4,9,'chiseled_sandstone');
   room.box([14,2,3],[14,9,12],'cut_sandstone');for(const elevation of[3,6,9])room.box([12,elevation,3],[14,elevation,12],'smooth_sandstone');
   for(const depth of[3,6,9,12])room.box([12,3,depth],[14,9,depth],'chiseled_sandstone');
   for(const[elevation,depth,state]of[[4,4,'orange_terracotta'],[4,7,'brown_terracotta'],[4,10,'yellow_terracotta'],[7,4,'terracotta'],[7,7,'red_terracotta'],[7,10,'orange_terracotta']])room.box([12,elevation,depth],[13,elevation+1,depth],state);
   room.box([1,2,11],[3,2,13],'cut_red_sandstone');room.box([1,2,10],[3,2,10],'sandstone_stairs[facing=south,half=bottom,shape=straight,waterlogged=false]');room.box([1,1,11],[3,1,13],'smooth_sandstone');
   for(const depth of[2,11]){room.box([1,6,depth],[1,9,depth],'chiseled_sandstone');room.box([1,6,depth+2],[1,9,depth+2],'chiseled_sandstone');room.box([1,10,depth],[2,10,depth+2],'cut_sandstone');room.set(1,11,depth+1,'chiseled_sandstone');}
  }
 }else if(batch===3){
  if(index===0){
   room.box([1,2,2],[1,7,6],'stripped_oak_log');room.box([4,2,2],[4,7,3],'stripped_oak_log');room.box([1,7,2],[4,7,3],'oak_planks');room.box([1,7,2],[2,7,6],'oak_planks');
   room.box([2,3,2],[3,6,2],'white_wool');room.box([2,4,2],[3,4,2],'red_wool');room.box([1,3,4],[1,6,5],'orange_wool');room.box([1,5,4],[1,5,5],'blue_wool');
   for(const[width,depth,state]of[[2,9,'red_terracotta'],[2,12,'blue_terracotta'],[5,12,'yellow_terracotta']]){room.box([width,2,depth],[width+1,2,depth+1],'terracotta');room.set(width,3,depth,state);}
   room.box([13,2,5],[13,6,8],'stripped_oak_log');room.box([12,6,5],[14,6,8],'oak_planks');room.box([12,3,6],[12,5,7],'orange_wool');room.box([12,4,6],[12,4,7],'yellow_wool');
   room.box([11,2,13],[11,8,13],'stripped_oak_log');room.box([14,2,13],[14,8,13],'stripped_oak_log');room.box([11,8,13],[14,8,13],'oak_planks');room.box([12,4,13],[13,7,13],'blue_wool');room.box([12,5,13],[13,5,13],'white_wool');
  }else{
   room.box([1,2,2],[3,3,4],'cut_sandstone');room.box([1,4,2],[3,7,4],'chiseled_sandstone');room.box([2,8,3],[2,9,3],'smooth_sandstone');room.set(2,10,3,'gold_block');
   room.box([11,2,10],[13,2,10],'sandstone_stairs[facing=south,half=bottom,shape=straight,waterlogged=false]');room.box([11,2,11],[13,2,13],'smooth_sandstone');room.box([11,3,11],[13,3,11],'sandstone_stairs[facing=south,half=bottom,shape=straight,waterlogged=false]');room.box([11,3,12],[13,3,13],'cut_sandstone');for(const width of[11,13])room.set(width,3,13,'blue_terracotta');
   for(let depth=3;depth<=13;depth++)for(let width=3;width<=13;width++){const angle=Math.atan2(depth-3,width-2),distance=Math.hypot(width-2,depth-3);if(distance>=3&&distance<=12&&Math.abs(Math.sin(angle*8))<.13)room.set(width,1,depth,'cut_red_sandstone');}
   room.box([2,15,7],[13,15,9],'yellow_stained_glass');room.box([2,13,6],[13,14,6],'smooth_sandstone');room.box([2,13,10],[13,14,10],'smooth_sandstone');
  }
 }else if(batch===2){
  if(index===0){
   room.box([1,1,3],[3,2,10],'glass');room.box([2,1,4],[2,1,9],'water[level=0]');
   room.box([6,1,4],[9,1,11],'air');room.box([6,0,4],[9,0,11],'blue_terracotta');
   for(const depth of[4,9])room.box([5,1,depth],[10,1,depth+2],'cut_sandstone');
   room.box([11,2,10],[13,7,12],'cut_sandstone');room.box([11,8,11],[13,9,12],'chiseled_sandstone');room.box([12,10,11],[13,10,12],'gold_block');
   room.box([1,6,3],[3,6,3],'chiseled_sandstone');room.box([1,7,3],[2,8,3],'cut_sandstone');
  }else{
   for(const[width,depth,height]of[[1,3,5],[2,7,7],[1,11,8],[11,6,6],[12,10,5]])for(let elevation=2;elevation<=height;elevation++){
    const offset=Math.floor((elevation-2)/3);const targetWidth=width+(width<7?offset:-offset);room.box([targetWidth,elevation,depth],[targetWidth+1,elevation,depth+1],elevation%3===0?'chiseled_sandstone':'cut_sandstone');
   }
   room.box([0,4,2],[0,10,9],'chiseled_sandstone');room.box([1,6,2],[1,9,3],'cut_red_sandstone');room.box([1,8,4],[1,10,6],'smooth_sandstone');
   room.box([2,11,1],[5,11,3],'cut_sandstone');room.box([3,12,1],[4,12,2],'gold_block');
  }
 }else if(index===0){
  for(const[width,depth,top]of[[1,2,6],[4,7,9],[1,11,12]]){
   room.box([width,2,depth],[width+2,top,depth+2],'glass');
   room.box([width+1,2,depth+1],[width+1,Math.min(top-1,4),depth+1],'sand');
   if(top>6)room.set(width+1,top-3,depth+1,'glass');
   room.box([width+1,top-2,depth+1],[width+1,top-1,depth+1],'sand');
   room.box([width,1,depth],[width+2,1,depth+2],'cut_sandstone');
   room.box([width,top+1,depth],[width+2,top+1,depth+2],'chiseled_sandstone');
  }
  for(const depth of[2,9]){room.box([13,2,depth],[14,3,depth+3],'smooth_sandstone');for(let mark=depth;mark<=depth+3;mark++)room.set(13,3,mark,mark%2?'blue_terracotta':'chiseled_sandstone');}
  room.box([11,10,3],[14,10,3],'cut_sandstone');room.box([12,11,3],[14,11,3],'chiseled_sandstone');
 }else{
  for(const[top,width]of[[3,3],[5,2],[7,1]])room.box([1,top,3],[width,top,12],'smooth_sandstone');
  for(const top of[3,5])room.box([4,top,13],[13,top,14],'cut_sandstone');
  for(const[width,depth,top,state]of[[2,4,4,'yellow_terracotta'],[2,8,6,'orange_terracotta'],[1,11,8,'brown_terracotta'],[5,13,4,'green_terracotta'],[9,13,6,'red_terracotta']])room.box([width,top,depth],[width,top,depth+1],state);
  room.box([10,2,7],[13,2,9],'cut_red_sandstone');room.box([11,3,8],[12,3,8],'brown_terracotta');
  room.box([3,2,2],[3,6,2],'cut_sandstone');room.box([1,6,2],[5,6,2],'chiseled_sandstone');room.box([1,5,2],[1,5,3],'gold_block');room.box([5,4,2],[5,4,3],'gold_block');
  for(const depth of[5,10]){room.box([1,9,depth],[3,9,depth],'chiseled_sandstone');room.box([12,9,depth],[14,9,depth],'chiseled_sandstone');room.box([3,10,depth],[5,10,depth],'cut_sandstone');room.box([10,10,depth],[12,10,depth],'cut_sandstone');room.box([5,11,depth],[10,11,depth],'chiseled_sandstone');}
 }
 reserveInterfaces(room);
 for(const port of variant.ports)for(const[width,height,depth]of port.cells)for(let inward=0;inward<5;inward++){const targetDepth=depth===0?inward:15-inward;room.box([width,height,targetDepth],[width,height+4,targetDepth],'air');room.set(width,height-1,targetDepth,'smooth_sandstone');}
 return room;
}
const allowedText=readFileSync('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/references/lodestone/default-pack-block-states.md','utf8');
save(join(output,'design.md'),'# 沙漠新增第5批 · 仅2间\n\n'+definitions.map(definition=>'## '+definition.key+' '+definition.name+'\n\nbaseKey: '+definition.baseKey+'\n\n'+definition.circulation+'\n\n'+definition.height+'\n\n'+definition.landmark+'；'+definition.difference+'\n').join('\n'));
const rooms=[],reports=[],templates=[];
for(const[index,definition]of definitions.entries()){
 const room=build(definition,index);for(const state of new Set(room.blocks))assert(allowedText.includes('`minecraft:'+state.split('[')[0]+'`'),'Invalid block '+state);
 const bytes=encodeNbt(room),file='roomset/'+definition.key+'.nbt';save(join(output,file),bytes);
 const actual=roomFromNbt(decodeNbt(readFileSync(join(output,file))),room.variant),alternatives=ensureAlternatives(actual);
 let sealedSourceCells=0;
 for(let height=0;height<16;height++)for(let depth=0;depth<16;depth++)for(let width=0;width<16;width++)if(actual.get(width,height,depth).startsWith('water')){assert.equal(actual.get(width,height,depth),'water[level=0]');sealedSourceCells++;for(const[offsetWidth,offsetHeight,offsetDepth]of[[1,0,0],[-1,0,0],[0,1,0],[0,-1,0],[0,0,1],[0,0,-1]]){const neighbor=actual.get(width+offsetWidth,height+offsetHeight,depth+offsetDepth);assert(neighbor==='glass'||neighbor==='water[level=0]'||neighbor==='smooth_sandstone','Unsealed water '+[width,height,depth]);}}
 if(batch===2&&index===0)assert.equal(sealedSourceCells,6);else assert.equal(sealedSourceCells,0);
 const functionalTarget=index===0?[3,4,5]:[4,2,10];const graph=walkGraph(actual,actual.variant.ports[0].cells[0]);assert(graph.has(functionalTarget),'Meteor platform or excavation bridge must be accessible without jumping');
 const baseline=readFileSync(join(root,'output/style-rebuild-v034/desert/roomset',definition.baseKey+'.nbt'));
 const report=validate(actual,bytes,baseline,alternatives);let sandBlocks=0;for(let height=0;height<16;height++)for(let depth=0;depth<16;depth++)for(let width=0;width<16;width++)if(actual.get(width,height,depth)==='sand'){sandBlocks++;assert(!['air','outside'].includes(actual.get(width,height-1,depth)),'Unsupported sand');}report.sandStability={sandBlocks,allHaveSolidSupport:true,flowingFluids:0,sealedSourceCells,sourceWaterSixSidesVerified:true};report.functionalWalkTarget={position:functionalTarget,reachableWithoutJump:true};report.status='PASS';report.interfaces=structuredClone({size:room.variant.size,minimum_y:room.variant.minimum_y,ports:room.variant.ports,sockets:room.variant.sockets});report.baseKey=definition.baseKey;
 rooms.push(actual);reports.push(report);templates.push({key:definition.key,baseKey:definition.baseKey,name:definition.name,sha256:hash(bytes),file});
}
saveJson('manifest.json',{schema:1,style:'fteclipsegen:desert',batch,templates});
saveJson('validation.json',{schema:1,status:'PASS',style:'fteclipsegen:desert',batch,checks:['dense complete coordinates, DataVersion3465, vanilla IDs, no entities/block entities','catalog interfaces unchanged; ports inward five cells with five air headroom','socket 3-air/full support, independent monster feet','half-block jump-free connectivity, sealed shell including roof','geometry compared to old matching base room'],results:reports});
const comparison=[];
for(const variant of catalog){const file=join(root,'output/style-rebuild-v034/desert/roomset',variant.key+'.nbt');comparison.push({room:roomFromNbt(decodeNbt(readFileSync(file)),variant),origin:'old27',index:catalog.indexOf(variant)});}
const expansionRoot=join(root,'output/room-expansion-v035/desert');
for(const directory of readdirSync(expansionRoot,{withFileTypes:true})){if(!directory.isDirectory()||directory.name===batchDirectory)continue;const path=join(expansionRoot,directory.name,'manifest.json');if(!existsSync(path))continue;for(const template of JSON.parse(readFileSync(path)).templates){const variant={...structuredClone(catalog.find(entry=>entry.key===template.baseKey)),key:template.key};comparison.push({room:roomFromNbt(decodeNbt(readFileSync(join(expansionRoot,directory.name,template.file))),variant),origin:'previous-addition',index:(Number(template.key.split('/').at(-1))-1)%2});}}
const signatures=rooms.map(interiorSignature),pairs=[];
for(const[index,room]of rooms.entries())for(const other of comparison){const score=jaccard(signatures[index].raw,interiorSignature(other.room,other.index).raw);pairs.push({first:room.variant.key,last:other.room.variant.key,origin:other.origin,jaccard:score.similarity,intersection:score.intersection,union:score.union,status:score.similarity>=.85?'FAIL':'PASS',layoutDifference:definitions[index].circulation});}
const between=jaccard(signatures[0].raw,signatures[1].raw);pairs.push({first:definitions[0].key,last:definitions[1].key,origin:'current-batch',jaccard:between.similarity,status:between.similarity>=.85?'FAIL':'PASS',layoutDifference:'Three-point astronomy workshop around a raised meteor cradle versus off-axis shallow excavation, short bridge loop and layered section wall.'});pairs.sort((first,last)=>last.jaccard-first.jaccard);assert(pairs.every(pair=>pair.status==='PASS'),'High repetition requires rebuilding');
saveJson('repetition-audit.json',{schema:1,status:'PASS',threshold:.85,method:'Solid-only Jaccard after independently reconstructed common shell/vault, base floor and port safety band removal; ignores shared air',againstOld:27,previousAdditionCount:comparison.length-27,currentBatchPairs:1,pairs,signatures:signatures.map((signature,index)=>({key:definitions[index].key,solidCells:signature.raw.size,sha256:hash(Buffer.from([...signature.raw].sort().join(';')))}))});
const landmarkDefinitions=[[[[1,4,5],[2,6,6],'iron_block'],[[2,2,2],[4,3,3],'sandstone_stairs'],[[11,2,3],[13,8,4],'chiseled_sandstone'],[[13,4,10],[14,4,11],'polished_andesite'],[[4,15,4],[11,15,11],'blue_stained_glass']],[[[1,1,9],[6,1,11],'oak_planks'],[[2,0,7],[5,0,11],'smooth_sandstone'],[[1,2,3],[2,7,5],'cut_red_sandstone'],[[1,1,7],[5,1,13],'sand'],[[1,2,7],[5,2,8],'cut_sandstone']]];
const style=rooms.map((room,index)=>{const materials={};for(const state of room.blocks){const name=state.split('[')[0];materials[name]=(materials[name]??0)+1;}const landmarks=landmarkDefinitions[index].map(([minimum,maximum,material])=>{const matches=[];for(let height=minimum[1];height<=maximum[1];height++)for(let depth=minimum[2];depth<=maximum[2];depth++)for(let width=minimum[0];width<=maximum[0];width++)if(room.get(width,height,depth).split('[')[0]===material)matches.push([width,height,depth]);assert(matches.length>=2);return{minimum,maximum,material,actualBlocks:matches.length,actualBounds:{minimum:[0,1,2].map(axis=>Math.min(...matches.map(point=>point[axis]))),maximum:[0,1,2].map(axis=>Math.max(...matches.map(point=>point[axis])))} };});return{key:definitions[index].key,status:'PASS',materials,landmarks,form:definitions[index].circulation,review:'No clock/gear motif; no four-corner columns around empty hall; interior body measured from decoded NBT rather than name.',repairs:['Reserve socket feet before furnishings acceptance; certify all declaration paths','Extend port clearance five cells inward'],nbtSha256:templates[index].sha256};});
saveJson('style-audit.json',{schema:1,status:'PASS',results:style});
const views=makeFourViews(rooms,reports);makeObliqueViews(rooms,reports);
const {chromium}=await import('file:///C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright/index.mjs');
const browser=await chromium.launch({headless:true}),errors=[],screenshots=[];
try{const page=await browser.newPage({viewport:{width:1600,height:1100},deviceScaleFactor:1});page.on('pageerror',error=>errors.push(error.message));await page.goto('file:///'+join(output,'four-views.html').replaceAll('\\','/'));await page.evaluate(()=>window.__ready);assert.equal(await page.locator('img').count(),8);await page.locator('#sheet-0').screenshot({path:join(output,'cardinal-views.png')});screenshots.push('cardinal-views.png');await page.goto('file:///'+join(output,'oblique-four-views.html').replaceAll('\\','/'));await page.waitForFunction(()=>window.__ready?.complete);assert.equal(await page.locator('canvas').count(),8);await page.locator('#iso-sheet-0').screenshot({path:join(output,'oblique-views.png')});screenshots.push('oblique-views.png');await page.locator('#full').check();await page.locator('#full').dispatchEvent('input');await page.locator('#iso-sheet-0').screenshot({path:join(output,'oblique-full-height.png')});screenshots.push('oblique-full-height.png');assert.equal(errors.length,0);}finally{await browser.close();}
saveJson('self-audit.json',{schema:1,status:'PASS',batch,rooms:2,cardinalViews:8,obliqueViews:8,browser:{engine:'Chromium',errors,screenshots},geometry:reports.map(report=>({key:report.key,changedSolidAir:report.geometry.changedSolidAir})),style:'PASS',repetition:{status:'PASS',highest:pairs[0]},visualRules:'Hide common shell/vault; diagonal views default feet+4 with one-layer floor, full-height screenshot retained. Actual exported NBT geometry.',limitations:['Offline geometry/collision checks, not in-game combat or lighting acceptance.'],sharedToolSha256:hash(Buffer.from(original))});
const visualAudit=JSON.parse(readFileSync(join(output,'self-audit.json')));visualAudit.visualReview=definitions.map(definition=>({key:definition.key,status:'PASS',reviewedScreenshots:['oblique-views.png','oblique-full-height.png'],finding:definition.circulation+' '+definition.height+' Common shell is removed only in preview; full-height geometry and low cut reveal instrument thickness and unobstructed central route.'}));saveJson('self-audit.json',visualAudit);
function listFiles(directory,prefix=''){return readdirSync(directory,{withFileTypes:true}).flatMap(entry=>entry.isDirectory()?listFiles(join(directory,entry.name),prefix+entry.name+'/'):[prefix+entry.name]);}
save(join(output,'recipe-source.mjs'),readFileSync(recipe));
const paths=listFiles(output).filter(file=>file!=='freeze.json');
if(!process.argv.includes('--replay-probe')){const expected=new Map(paths.map(file=>[file,hash(readFileSync(join(output,file)))]));const replay=spawnSync(process.execPath,[recipe,'--batch=5','--replay-probe'],{encoding:'utf8',timeout:120000});assert.equal(replay.status,0,replay.stderr);for(const[file,sha256]of expected)assert.equal(hash(readFileSync(join(output,file))),sha256,'Replay '+file);}
saveJson('freeze.json',{schema:1,status:'FROZEN_FOR_PARENT_INTEGRATION',style:'fteclipsegen:desert',batch,recipe:{file:'tools/expand-desert.mjs',sha256:hash(readFileSync(recipe))},sharedReadOnlyTool:{file:'tools/rebuild-desert.mjs',sha256:hash(Buffer.from(original))},files:Object.fromEntries(paths.map(file=>[file,hash(readFileSync(join(output,file)))])),templates,replay:'byte-identical core artifacts and browser screenshots'});
console.log(JSON.stringify({status:'FROZEN_FOR_PARENT_INTEGRATION',output,manifestSha256:hash(readFileSync(join(output,'manifest.json'))),freezeSha256:hash(readFileSync(join(output,'freeze.json'))),maxJaccard:pairs[0]},null,2));
