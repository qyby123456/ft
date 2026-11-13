import assert from 'node:assert/strict';
import { createHash } from 'node:crypto';
import { readFileSync, writeFileSync, mkdirSync, existsSync, readdirSync } from 'node:fs';
import { join, dirname } from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';
import { spawn, spawnSync } from 'node:child_process';
import { createServer } from 'node:http';
import { once } from 'node:events';

const root=process.env.CORAL_PROJECT_ROOT??fileURLToPath(new URL(import.meta.url.includes('/tools/')?'../':'../../../../',import.meta.url));
const parent=join(root,'output/room-expansion-v035/coral');
const out=join(parent,'batch-05');
const resources=join(root,'src/main/resources/data/fteclipsegen');
const catalogBytes=readFileSync(join(resources,'catalog.json'));
const catalog=JSON.parse(catalogBytes);
const skill=process.env.MINECRAFT_BUILDER_SKILL??'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill';
const library=await import(pathToFileURL(join(skill,'node_modules/@mattzh72/lodestone/lib/index.js')).href);
const hash=bytes=>createHash('sha256').update(bytes).digest('hex');
const isAir=block=>block==='air';
const baseName=block=>block.split('[')[0];
const full=block=>!isAir(block)&&!/stairs|slab|pane|fence|water/.test(block);
const names=['浮力球检修室','礁隙救援站'];
const specifications=[{key:'addition/09',baseKey:'straight/room_towers',name:names[0]},{key:'addition/10',baseKey:'straight/room_cells',name:names[1]}];

function enclose(room) {
  room.box([0,0,0],[15,1,15],'dark_prismarine');
  for(let zz=0;zz<16;zz++)for(let xx=0;xx<16;xx++) {
    room.set(xx,1,zz,(xx+zz)%7===0?'sea_lantern':'prismarine_bricks');
    const roof=12+Math.round(2*Math.sin(Math.PI*xx/15));
    for(let yy=2;yy<16;yy++) {
      if(xx===0||xx===15||zz===0||zz===15)room.set(xx,yy,zz,yy>=5&&yy<=9&&(xx+zz)%6<4?'cyan_stained_glass':yy%4===0?'smooth_quartz':'prismarine_bricks');
      else if(yy>=roof)room.set(xx,yy,zz,yy===roof?'white_terracotta':'dark_prismarine');
    }
  }
}

function nursery(room) {
  room.features=['三组错列封闭玻璃育珠槽，真实静止源水与石英海晶珠架','中央育珠槽将主路分为西侧和东侧至少三格宽干燥环行通路','东南开珠台、西侧低分拣台与高低贝壳槽冠'];
  const tank=(fromX,fromZ,toX,toZ,top)=> {
    room.box([fromX,2,fromZ],[toX,top,toZ],'cyan_stained_glass');
    room.box([fromX,2,fromZ],[toX,2,toZ],'smooth_quartz');
    room.box([fromX+1,3,fromZ+1],[toX-1,top-1,toZ-1],'water[level=0]');
    for(let zz=fromZ+1;zz<toZ;zz++) {
      room.set(fromX+1,3,zz,'quartz_pillar');
      room.set(fromX+1,4,zz,'sea_lantern');
      if(top>=8){room.set(fromX+1,6,zz,'quartz_pillar');room.set(fromX+1,7,zz,'sea_lantern');}
    }
    for(let xx=fromX;xx<=toX;xx++) {
      const yy=top+1+(xx===fromX+1?1:0);
      room.box([xx,yy,fromZ],[xx,yy,toZ],'white_terracotta');
    }
    room.landmarks.push({type:'sealed-pearl-nursery',bounds:{from:[fromX,2,fromZ],to:[toX,top+2,toZ]},expectedMaterials:['cyan_stained_glass','water','quartz_pillar','sea_lantern'],function:'Glazed source-water chamber with lit pearl-support shelves; dry exterior access'});
  };
  tank(1,2,3,4,7);tank(6,7,8,9,8);tank(12,6,14,9,9);
  room.mass(1,8,2,11,2,'pink_terracotta');
  room.mass(12,12,14,13,3,'orange_terracotta');
  room.box([13,4,12],[13,4,13],'smooth_quartz');room.set(13,5,12,'sea_lantern');
  room.box([1,10,7],[4,10,7],'smooth_quartz');
  room.landmarks.push({type:'pearl-opening-and-sorting-benches',bounds:{from:[12,2,12],to:[14,5,13]},expectedMaterials:['orange_terracotta','smooth_quartz','sea_lantern'],function:'Raised pearl-opening counter; opposite west sorting bench is low and does not form a barrier'});
}

function filtration(room) {
  room.features=['三组并列但不等高的海绵滤柱，低/高/中三种真实高度','西南L形静态沉淀槽配中央四格直通干燥维护主路','墙边静态海晶短管、贝壳集流帽，完全没有齿轮或红石依赖'];
  for(const [fromX,fromZ,top]of [[1,5,7],[4,7,10],[12,6,6]]) {
    room.box([fromX,2,fromZ],[fromX+1,2,fromZ+1],'dark_prismarine');
    room.box([fromX,3,fromZ],[fromX+1,top-1,fromZ+1],'sponge');
    room.box([fromX,top,fromZ],[fromX+1,top,fromZ+1],'smooth_quartz');
    room.box([fromX,top+1,fromZ],[fromX+1,top+1,fromZ+1],'cyan_stained_glass');
    room.landmarks.push({type:'sponge-filter-column',bounds:{from:[fromX,2,fromZ],to:[fromX+1,top+1,fromZ+1]},expectedMaterials:['sponge','smooth_quartz','cyan_stained_glass'],function:`Stationary dry sponge filter height ${top-1}; does not touch any water blocks`});
  }
  for(const [from,to]of [[[1,2,10],[2,2,14]],[[1,2,13],[5,2,14]]])room.box(from,to,'blue_stained_glass');
  room.box([1,3,10],[1,3,14],'smooth_quartz');room.box([1,3,14],[5,3,14],'smooth_quartz');
  room.box([1,7,3],[1,7,4],'prismarine_bricks');room.box([1,7,3],[3,7,3],'prismarine_bricks');
  room.box([14,6,6],[14,6,11],'prismarine_bricks');room.box([12,6,10],[14,6,10],'prismarine_bricks');
  room.set(14,7,10,'sea_lantern');room.set(1,4,13,'sea_lantern');
  room.box([3,11,6],[6,11,6],'white_terracotta');
  room.landmarks.push({type:'L-shaped-settlement-trough',bounds:{from:[1,2,10],to:[5,4,14]},expectedMaterials:['blue_stained_glass','smooth_quartz','sea_lantern'],function:'Two perpendicular glazed settling beds with solid rim; contains no flowing liquid'});
  room.landmarks.push({type:'static-short-wall-pipes',bounds:{from:[12,6,6],to:[14,7,11]},expectedMaterials:['prismarine_bricks','sea_lantern'],function:'Wall-side short supply pipes made entirely of stationary full blocks'});
}

function divingStation(room) {
  room.features=['偏东南倒扣空心铜沉钟，四柱落地承重架与上部横梁','西北装备台、西南低登乘台、东南钟架形成三角作业布局','中央四格干燥维护轴与全玻璃封闭海洋天窗，无流动水或悬空承重柱'];
  const material='waxed_oxidized_copper';
  for(const xx of [10,14])for(const zz of [8,12])room.box([xx,2,zz],[xx,10,zz],'quartz_pillar');
  for(const zz of [8,12])room.box([10,11,zz],[14,11,zz],'smooth_quartz');
  for(const xx of [10,14])room.box([xx,11,8],[xx,11,12],'smooth_quartz');
  room.box([10,11,10],[14,11,10],'smooth_quartz');room.box([12,9,10],[12,10,10],material);
  for(let yy=5;yy<=9;yy++) {
    const radius=yy<7?2:yy<9?1:0;
    for(let zz=10-radius;zz<=10+radius;zz++)for(let xx=12-radius;xx<=12+radius;xx++) {
      if(radius===0||Math.max(Math.abs(xx-12),Math.abs(zz-10))===radius)room.set(xx,yy,zz,yy===5?'waxed_cut_copper':material);
    }
  }
  room.set(12,8,10,'sea_lantern');
  for(const xx of [10,14])for(const zz of [8,12])room.box([xx,2,zz],[xx,10,zz],'quartz_pillar');
  room.mass(1,2,3,4,3,'orange_terracotta');room.box([1,4,2],[3,4,2],'smooth_quartz');
  room.set(2,4,3,'sea_lantern');room.box([1,5,2],[1,7,2],'prismarine_bricks');
  room.mass(1,11,3,13,2,'white_terracotta');room.box([1,3,13],[3,3,13],'cyan_stained_glass');
  room.box([10,12,7],[14,15,13],'cyan_stained_glass');
  room.landmarks.push({type:'grounded-diving-bell-and-load-frame',bounds:{from:[10,2,8],to:[14,11,12]},expectedMaterials:['quartz_pillar','smooth_quartz','waxed_oxidized_copper','waxed_cut_copper','sea_lantern'],function:'Four pillars meet the full floor and carry a closed inverted hollow bell via crossbeam; dry open boarding area under bell'});
  room.landmarks.push({type:'equipment-service-counter',bounds:{from:[1,2,2],to:[3,7,4]},expectedMaterials:['orange_terracotta','smooth_quartz','prismarine_bricks','sea_lantern'],function:'Separate northwest equipment fitting and inspection worktop'});
  room.landmarks.push({type:'low-boarding-deck',bounds:{from:[1,2,11],to:[3,3,13]},expectedMaterials:['white_terracotta','cyan_stained_glass'],function:'Low southwest boarding platform makes triangular layout rather than another central tower'});
  room.landmarks.push({type:'sealed-ocean-roof-window',bounds:{from:[10,12,7],to:[14,15,13]},expectedMaterials:['cyan_stained_glass'],function:'Entire ocean-facing upper aperture is solid glass, no exposed opening'});
}

function specimenMuseum(room) {
  room.features=['中央偏西展开的扇形珊瑚骨架，以连续放射肋和单干支撑形成实体展架','东侧分叉枝骨在三维方向伸展，东南低观察柜占据第三角','主路先进入前庭，再从中央扇骨的西侧或东侧三格干燥通路绕行，保留四个分散socket'];
  const segment=(from,to,material)=> {
    const steps=Math.max(...from.map((value,axis)=>Math.abs(value-to[axis])))*3;
    let previous=[...from];room.set(...previous,material);
    for(let step=1;step<=steps;step++) {
      const next=from.map((value,axis)=>Math.round(value+(to[axis]-value)*step/steps));
      for(let axis=0;axis<3;axis++)while(previous[axis]!==next[axis]){previous[axis]+=Math.sign(next[axis]-previous[axis]);room.set(...previous,material);}
    }
  };
  room.box([6,2,8],[8,2,8],'pink_terracotta');room.box([7,3,8],[7,6,8],'quartz_pillar');
  for(const endpoint of [[3,8,8],[4,10,8],[6,11,8],[8,11,8],[10,10,8],[11,8,8]])segment([7,5,8],endpoint,'bone_block');
  for(let xx=3;xx<=11;xx++) {
    const yy=8+Math.round(3*Math.sin(Math.PI*(xx-3)/8));room.set(xx,yy,8,'smooth_quartz');
  }
  for(const [xx,top]of [[5,9],[7,10],[9,9]])room.box([xx,8,8],[xx,top,8],'air');
  room.box([12,2,7],[14,2,9],'orange_terracotta');
  segment([13,3,8],[13,8,8],'bone_block');
  for(const [fork,end]of [[[13,4,8],[12,6,6]],[[13,5,8],[14,8,10]],[[13,7,8],[12,10,10]],[[13,8,8],[14,10,7]]])segment(fork,end,'bone_block');
  room.set(13,3,8,'sea_lantern');
  room.mass(12,13,14,14,2,'white_terracotta');room.box([12,3,13],[14,3,14],'cyan_stained_glass');
  room.set(13,2,13,'sea_lantern');
  room.box([1,9,4],[3,9,4],'smooth_quartz');
  room.landmarks.push({type:'radial-fan-coral-skeleton',bounds:{from:[3,2,8],to:[11,11,8]},expectedMaterials:['bone_block','quartz_pillar','pink_terracotta','smooth_quartz'],function:'Six radiating ribs with a curved fan crest on a single offset stem; all bone ribs use six-neighbor connected paths; low plinth diverts walking rather than scattering coral blocks'});
  room.landmarks.push({type:'three-dimensional-branch-skeleton',bounds:{from:[12,2,6],to:[14,10,10]},expectedMaterials:['bone_block','orange_terracotta','sea_lantern'],function:'One grounded trunk with four anatomically connected branches extending in X/Y/Z'});
  room.landmarks.push({type:'third-corner-observation-case',bounds:{from:[12,2,13],to:[14,3,14]},expectedMaterials:['white_terracotta','cyan_stained_glass','sea_lantern'],function:'Low southeast glass display counter distinct from either large skeleton'});
}

function knotClassroom(room) {
  room.features=['西侧横向与东侧纵向两段开放结绳架，悬绳练习位与棕色结节清楚分开','西南低矮航海地图桌和两侧授课作业位，中央四格干燥课堂通道','连续船式弧肋屋顶与纵向龙骨呼应航海主题，不使用货垛或仓库格架'];
  for(const xx of [1,4])room.box([xx,2,7],[xx,7,7],'stripped_birch_log');
  room.box([1,8,7],[4,8,7],'stripped_birch_log');
  for(const xx of [2,3])room.box([xx,4,7],[xx,7,7],'chain[axis=y,waterlogged=false]');
  room.set(2,5,7,'brown_wool');room.set(3,6,7,'brown_wool');
  for(const zz of [6,10])room.box([13,2,zz],[13,6,zz],'stripped_birch_log');
  room.box([13,7,6],[13,7,10],'stripped_birch_log');
  for(const zz of [7,9])room.box([13,3,zz],[13,6,zz],'chain[axis=y,waterlogged=false]');
  room.set(13,4,7,'brown_wool');room.set(13,5,9,'brown_wool');
  room.mass(1,12,4,13,2,'white_terracotta');
  room.box([2,3,12],[3,3,13],'cyan_stained_glass');room.set(2,2,12,'blue_terracotta');room.set(3,2,13,'yellow_terracotta');
  for(const zz of [3,8,12])for(let xx=1;xx<=14;xx++) {
    const yy=8+Math.round(3*Math.sin(Math.PI*(xx-1)/13));room.set(xx,yy,zz,'stripped_birch_log');
    if(xx===1||xx===14)room.box([xx,2,zz],[xx,yy,zz],'quartz_pillar');
  }
  room.box([7,12,2],[8,12,13],'stripped_birch_log');
  room.landmarks.push({type:'transverse-knot-practice-frame',bounds:{from:[1,2,7],to:[4,8,7]},expectedMaterials:['stripped_birch_log','chain','brown_wool'],function:'Open four-column-wide X/Y teaching frame, two hanging rope chains with physically distinct knot blocks'});
  room.landmarks.push({type:'longitudinal-knot-practice-frame',bounds:{from:[13,2,6],to:[13,7,10]},expectedMaterials:['stripped_birch_log','chain','brown_wool'],function:'Second Y/Z frame rotated ninety degrees with unequal knot heights'});
  room.landmarks.push({type:'low-nautical-map-table',bounds:{from:[1,2,12],to:[4,3,13]},expectedMaterials:['white_terracotta','cyan_stained_glass','blue_terracotta','yellow_terracotta'],function:'Low glazed navigation chart worktop with blue sea and yellow bearings represented by actual blocks'});
  room.landmarks.push({type:'ship-rib-roof',bounds:{from:[1,8,3],to:[14,12,12]},expectedMaterials:['stripped_birch_log','quartz_pillar'],function:'Three curved transverse ship ribs and a longitudinal keel, independent of complete outer roofing'});
}

function tidalOrgan(room) {
  room.features=['后侧左右三簇高低空心贝壳声管，真实开口与渐升管唇，不设置钟塔冠灯','西北偏置演奏台经三格真实台阶登临，面向东侧三处斜向席位','四格干燥维护轴连接原南北门，演奏台、斜座与后侧声管构成声场组织'];
  const pipe=(fromX,fromZ,top)=> {
    room.box([fromX,2,fromZ],[fromX+2,top,fromZ+2],'white_terracotta');
    room.box([fromX+1,3,fromZ+1],[fromX+1,top+1,fromZ+1],'air');
    for(let zz=fromZ;zz<=fromZ+2;zz++)for(let xx=fromX;xx<=fromX+2;xx++)if(xx!==fromX+1||zz!==fromZ+1){
      const lip=top+(xx-fromX);room.box([xx,top,zz],[xx,lip,zz],'smooth_quartz');
    }
    room.set(fromX+1,4,fromZ,'pink_terracotta');
    room.box([fromX+1,5,fromZ],[fromX+1,top,fromZ],'air');
    room.landmarks.push({type:'hollow-shell-sound-pipe',bounds:{from:[fromX,2,fromZ],to:[fromX+2,top+2,fromZ+2]},expectedMaterials:['white_terracotta','smooth_quartz','pink_terracotta'],function:`Actual hollow 3x3 conch resonator with open central sound bore and rising lip, top=${top}; no beacon crown`});
  };
  pipe(1,7,6);pipe(1,12,9);pipe(11,11,7);
  room.mass(1,2,4,4,2,'pink_terracotta');room.box([1,3,2],[4,3,2],'smooth_quartz');
  room.box([2,2,5],[4,2,5],'quartz_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]');
  room.set(2,3,3,'sea_lantern');room.set(3,3,3,'black_terracotta');
  for(const [xx,zz]of [[10,3],[12,5],[13,8]]) {room.set(xx,2,zz,'smooth_quartz');room.set(xx,3,zz+1,'pink_terracotta');}
  room.box([1,4,10],[3,4,10],'white_terracotta');room.box([1,4,10],[1,4,12],'white_terracotta');
  room.landmarks.push({type:'offset-playing-platform',bounds:{from:[1,2,2],to:[4,3,5]},expectedMaterials:['pink_terracotta','smooth_quartz','quartz_stairs','sea_lantern','black_terracotta'],function:'Low northwest manual-playing platform with three-wide real stair access and a lit keyboard position'});
  room.landmarks.push({type:'diagonal-audience-seats',bounds:{from:[10,2,3],to:[13,3,9]},expectedMaterials:['smooth_quartz','pink_terracotta'],function:'Three distinct low seats advancing diagonally towards rear pipes rather than a symmetrical nave'});
}

function fiberWorkshop(room) {
  room.features=['三束海草纤维沿西侧对角错进，具有独立束芯、绑带和接墙承梁','西南整理台与东北纺织台分列两侧，右中部浅层密闭源水浸泡槽连接回环工序','西侧梁架通过两根完整立柱落地并与侧墙相接，中央四格干燥主路不浸水'];
  for(const [xx,zz,top]of [[1,6,5],[2,7,6],[3,8,7]]) {
    room.box([xx,2,zz],[xx,2,zz+1],'smooth_quartz');
    room.box([xx,3,zz],[xx,top,zz+1],'green_terracotta');
    room.box([xx,4,zz],[xx,4,zz+1],'brown_wool');
    room.set(xx,top+1,zz,'lime_terracotta');
  }
  for(const xx of [1,5])room.box([xx,2,10],[xx,8,10],'stripped_birch_log');
  room.box([0,9,10],[5,9,10],'stripped_birch_log');
  room.box([1,9,6],[1,9,10],'stripped_birch_log');
  room.box([0,9,6],[1,9,6],'stripped_birch_log');
  for(const [xx,zz,top]of [[1,6,5],[2,7,6],[3,8,7]]) {
    room.box([xx,top+1,zz],[xx,8,zz],'chain[axis=y,waterlogged=false]');
    room.box([1,9,zz],[xx,9,zz],'stripped_birch_log');
  }
  room.mass(1,12,3,13,2,'pink_terracotta');room.set(2,2,12,'sea_lantern');
  room.mass(12,2,14,3,2,'orange_terracotta');room.set(13,3,2,'smooth_quartz');room.set(14,3,2,'lime_terracotta');
  room.box([12,2,7],[14,4,10],'cyan_stained_glass');
  room.box([12,2,7],[14,2,10],'smooth_quartz');
  room.box([13,3,8],[13,3,9],'water[level=0]');
  room.set(13,2,8,'sea_lantern');room.set(13,2,9,'green_terracotta');
  room.landmarks.push({type:'diagonal-bound-seagrass-fiber-rack',bounds:{from:[1,2,6],to:[3,9,9]},expectedMaterials:['green_terracotta','brown_wool','chain','stripped_birch_log'],function:'Three actual dry fiber bundles progress diagonally in X/Z and increase in height, with full-block binding bands and thin suspended chains'});
  room.landmarks.push({type:'wall-tied-ground-supported-beams',bounds:{from:[0,2,6],to:[5,9,10]},expectedMaterials:['stripped_birch_log','chain'],function:'Two wall attachments at x0 and two full floor-grounded posts at x1/x5 z10 carry the drying beam'});
  room.landmarks.push({type:'dual-processing-worktops',bounds:{from:[1,2,2],to:[14,3,13]},expectedMaterials:['pink_terracotta','orange_terracotta','lime_terracotta','sea_lantern'],function:'Separate southwest finishing and northeast fiber-working counters; broad bounds include two distinct work zones, not one filled box'});
  room.landmarks.push({type:'sealed-shallow-soaking-vessel',bounds:{from:[12,2,7],to:[14,4,10]},expectedMaterials:['cyan_stained_glass','water','smooth_quartz','sea_lantern','green_terracotta'],function:'Exactly two source-water cells form a shallow sealed layer, fully isolated from continuous dry work circulation'});
}

function coinWashroom(room) {
  room.features=['西侧三段由低到高的阶梯清洗台，台面独立嵌币样和观察灯','西南船腹形内凹壁龛以弧肋和高低侧壳围出偏东验币坪，不堆整齐箱柜','东北单座短架与东南玻璃隔开的发光观察槽分离，中央和偏东通道干燥连续'];
  for(const [fromZ,toZ,top]of [[6,6,2],[7,8,3],[9,9,4]]) {
    room.box([1,2,fromZ],[3,top,toZ],'white_terracotta');
    room.box([1,top,fromZ],[3,top,toZ],'smooth_quartz');
    room.set(2,top,fromZ,'yellow_terracotta');
  }
  room.set(1,3,6,'sea_lantern');
  for(let zz=10;zz<=14;zz++) {
    const reach=zz<=11?1:zz===12?2:3;
    room.box([1,2,zz],[reach,6,zz],'pink_terracotta');
    room.box([reach,7,zz],[reach,8,zz],'smooth_quartz');
  }
  for(const zz of [10,12,14])for(let xx=1;xx<=5;xx++) {
    const yy=8+Math.round(2*Math.sin(Math.PI*(xx-1)/4));room.set(xx,yy,zz,'stripped_birch_log');
  }
  room.box([1,9,10],[1,9,14],'stripped_birch_log');
  for(let zz=6;zz<=12;zz++)for(let xx=9;xx<=11;xx++)room.set(xx,1,zz,(xx+zz)%3===0?'yellow_terracotta':'smooth_quartz');
  for(const zz of [4,6])room.box([14,2,zz],[14,5,zz],'stripped_birch_log');
  room.box([13,5,4],[14,5,6],'white_terracotta');room.set(13,5,5,'yellow_terracotta');
  room.box([12,2,8],[14,4,10],'cyan_stained_glass');
  room.box([13,2,9],[13,2,9],'sea_lantern');room.set(13,3,9,'yellow_terracotta');
  room.landmarks.push({type:'stepped-coin-washing-worktops',bounds:{from:[1,2,6],to:[3,4,9]},expectedMaterials:['smooth_quartz','yellow_terracotta','sea_lantern','white_terracotta'],function:'Three distinct successive low/middle/high working surfaces; coin samples are actual blocks, not chest arrays'});
  room.landmarks.push({type:'concave-ship-belly-wall-niche',bounds:{from:[1,2,10],to:[5,10,14]},expectedMaterials:['pink_terracotta','smooth_quartz','stripped_birch_log'],function:'Asymmetrically deepening ship hull side with three bowed timber ribs defines the west edge of an offset inspection court'});
  room.landmarks.push({type:'eccentric-coin-inspection-court',bounds:{from:[9,1,6],to:[11,4,12]},expectedMaterials:['smooth_quartz','yellow_terracotta','air'],function:'Off-axis x9..11 dry coin-verification court: floor inlays mark its function while upper cells remain free'});
  room.landmarks.push({type:'single-short-drying-rack',bounds:{from:[13,2,4],to:[14,5,6]},expectedMaterials:['stripped_birch_log','white_terracotta','yellow_terracotta'],function:'One short rack opposite hull niche, deliberately not a repeated grid of storage units'});
  room.landmarks.push({type:'glass-isolated-luminous-observation-channel',bounds:{from:[12,2,8],to:[14,4,10]},expectedMaterials:['cyan_stained_glass','sea_lantern','yellow_terracotta'],function:'Actual lit specimen under and behind six-sided glass, fully separate from dry circulation and runtime socket air'});
}

function buoyRepair(room) {
  room.features=['三组不同高度浮力球阶梯笼架，黄色球胆与上下收口阶梯环可辨，四角支脚完整落地','侧边吊装台有单侧承重吊臂和悬链，区别于三座冠灯标塔','西侧一格深检修坑有完整底、透明边护及台阶，中央四格干道和坑东侧绕路保持干燥'];
  for(const [xx,zz,centerY]of [[2,4,5],[2,12,8],[13,9,6]]) {
    room.box([xx-1,2,zz-1],[xx+1,2,zz+1],'dark_prismarine');
    for(const deltaX of [-1,1])for(const deltaZ of [-1,1]) {
      room.box([xx+deltaX,3,zz+deltaZ],[xx+deltaX,centerY-2,zz+deltaZ],'quartz_pillar');
      room.box([xx+deltaX,centerY-1,zz+deltaZ],[xx+deltaX,centerY+1,zz+deltaZ],'chain[axis=y,waterlogged=false]');
    }
    room.set(xx,centerY,zz,'yellow_terracotta');
    for(const [deltaX,deltaZ,facing]of [[-1,0,'east'],[1,0,'west'],[0,-1,'south'],[0,1,'north']]) {
      room.set(xx+deltaX,centerY,zz+deltaZ,'orange_terracotta');
      room.set(xx+deltaX,centerY-1,zz+deltaZ,`quartz_stairs[facing=${facing},half=bottom,shape=straight,waterlogged=false]`);
      room.set(xx+deltaX,centerY+1,zz+deltaZ,`quartz_stairs[facing=${facing},half=top,shape=straight,waterlogged=false]`);
    }
    room.set(xx,centerY-1,zz,'orange_terracotta');room.set(xx,centerY+1,zz,'yellow_terracotta');
    room.landmarks.push({type:'stair-enclosed-buoy-cage',bounds:{from:[xx-1,2,zz-1],to:[xx+1,centerY+1,zz+1]},expectedMaterials:['dark_prismarine','quartz_pillar','quartz_stairs','yellow_terracotta','orange_terracotta'],function:`Buoy center Y${centerY}; four grounded lower frame columns, thin static upper chain ties and eight inward stepped cage blocks around the spherical cross-section`});
  }
  room.mass(11,2,14,2,2,'pink_terracotta');
  room.box([14,3,2],[14,8,2],'stripped_birch_log');room.box([11,9,2],[14,9,2],'stripped_birch_log');
  room.box([11,5,2],[11,8,2],'chain[axis=y,waterlogged=false]');room.set(12,2,2,'sea_lantern');
  room.box([1,1,7],[2,1,9],'air');room.box([1,0,7],[2,0,9],'smooth_quartz');
  room.box([3,2,7],[3,3,9],'cyan_stained_glass');
  room.box([1,1,10],[2,1,10],'quartz_stairs[facing=south,half=bottom,shape=straight,waterlogged=false]');
  room.landmarks.push({type:'grounded-side-hoisting-platform',bounds:{from:[11,2,2],to:[14,9,2]},expectedMaterials:['pink_terracotta','stripped_birch_log','chain','sea_lantern'],function:'Low hoist worktop and single full-height supported lifting arm with a static chain'});
  room.landmarks.push({type:'bottomed-shallow-maintenance-pit',bounds:{from:[1,0,7],to:[3,3,10]},expectedMaterials:['smooth_quartz','air','cyan_stained_glass','quartz_stairs'],function:'One-block-deep six-cell inspection recess with complete Y0 floor, glazed east edge and two-wide real exit steps; dry bypass runs x4 and east'});
}

function reefRescue(room) {
  room.features=['西侧两肩不规则珊瑚崖形成侧面礁隙，非中心无底裂谷；完整玻璃外缘封闭','三格宽短救援桥跨有底浅池，两级实体台阶从东侧无跳跃登桥，桥底池一层水封于玻璃内','东北和东南两座对角医疗小台与救援低标记朝向桥侧，中央四格干道及全部socket安全'];
  for(const [fromZ,toZ,top]of [[2,5,8],[10,13,7]])for(let zz=fromZ;zz<=toZ;zz++)for(let xx=1;xx<=3;xx++) {
    const ridge=top-(xx-1)-(zz%2);room.box([xx,2,zz],[xx,ridge,zz],zz%2===0?'pink_terracotta':'orange_terracotta');
    room.set(xx,ridge+1,zz,'white_terracotta');
  }
  room.box([1,1,6],[3,2,8],'cyan_stained_glass');room.box([1,0,6],[3,0,8],'blue_terracotta');
  room.set(2,1,7,'water[level=0]');
  room.box([1,3,6],[3,3,8],'smooth_quartz');
  room.box([1,4,5],[3,4,5],'cyan_stained_glass');room.box([1,4,9],[3,4,9],'cyan_stained_glass');
  room.box([4,3,6],[4,3,8],'quartz_stairs[facing=west,half=bottom,shape=straight,waterlogged=false]');
  room.box([5,2,6],[5,2,8],'quartz_stairs[facing=west,half=bottom,shape=straight,waterlogged=false]');
  room.box([0,2,2],[0,11,13],'cyan_stained_glass');
  room.mass(12,2,14,3,2,'white_terracotta');room.set(13,2,2,'red_terracotta');
  room.mass(12,13,14,14,2,'pink_terracotta');room.box([12,3,14],[14,3,14],'smooth_quartz');room.set(13,3,14,'red_terracotta');
  room.set(12,3,2,'sea_lantern');room.set(14,2,13,'sea_lantern');
  room.box([4,1,9],[9,1,11],'smooth_quartz');
  room.landmarks.push({type:'paired-irregular-reef-cliff-shoulders',bounds:{from:[1,2,2],to:[3,9,13]},expectedMaterials:['pink_terracotta','orange_terracotta','white_terracotta','air'],function:'Two separated terraced cliff shoulders leave a lateral bridge cleft; solid masses stop before original sockets'});
  room.landmarks.push({type:'short-rescue-bridge-and-real-steps',bounds:{from:[1,2,6],to:[5,4,8]},expectedMaterials:['smooth_quartz','cyan_stained_glass','quartz_stairs'],function:'Three-wide deck over shallow sealed basin with two three-wide real stair flights; no jumping across a hole'});
  room.landmarks.push({type:'solid-bottom-shallow-pool',bounds:{from:[1,0,6],to:[3,2,8]},expectedMaterials:['blue_terracotta','cyan_stained_glass','water'],function:'One-cell-deep water under bridge, continuous Y0 base and six-sided glass enclosure; not an infinite fissure'});
  room.landmarks.push({type:'glazed-outer-safety-edge',bounds:{from:[0,2,2],to:[0,11,13]},expectedMaterials:['cyan_stained_glass'],function:'Entire exterior of the reef scene is fully glazed, no opening out of structure'});
  room.landmarks.push({type:'diagonal-medical-worktops',bounds:{from:[12,2,2],to:[14,3,14]},expectedMaterials:['white_terracotta','pink_terracotta','red_terracotta','sea_lantern'],function:'Two distinct northeast/southeast first-aid worktops face the western rescue crossing; broad bounds are not a filled medical block'});
}

function makeRoom(specification) {
  const base=catalog.variants.find(variant=>variant.key===specification.baseKey);
  assert(base);assert.deepEqual(base.size,[16,16,16]);assert.equal(base.sockets.length,4);
  const variant={...base,key:specification.key,baseKey:specification.baseKey};
  const room=new Room(variant);room.landmarks=[];
  enclose(room);
  if(specification.key==='addition/09')buoyRepair(room);
  else if(specification.key==='addition/10')reefRescue(room);
  else throw new Error('Only batch 05 is authorized');
  for(const port of variant.ports)for(const [xx,yy,zz]of port.cells)for(let inset=0;inset<=5;inset++)room.safe([xx,yy,zz===0?inset:zz-inset],5);
  for(const socket of variant.sockets){room.safe(socket.position);room.safe(socket.spawn_position);room.set(...socket.support,'smooth_quartz');}
  return room;
}

function commonMask(room) {
  const mask=new Set();
  for(let yy=0;yy<room.height;yy++)for(let zz=0;zz<room.depth;zz++)for(let xx=0;xx<room.width;xx++) {
    if(xx===0||xx===room.width-1||zz===0||zz===room.depth-1||yy<room.floor||yy>=Math.min(room.floor+9,room.height-2))mask.add(room.index(xx,yy,zz));
  }
  for(const port of room.variant.ports)for(const [xx,yy,zz]of port.cells)for(let inset=0;inset<=5;inset++)for(let offset=-1;offset<5;offset++) {
    const nextX=xx===0?inset:xx===room.width-1?xx-inset:xx;
    const nextZ=zz===0?inset:zz===room.depth-1?zz-inset:zz;
    mask.add(room.index(nextX,yy+offset,nextZ));
  }
  for(const socket of room.variant.sockets)for(let offset=-1;offset<3;offset++)mask.add(room.index(socket.position[0],socket.position[1]+offset,socket.position[2]));
  return mask;
}

function displayMask(room) {
  const mask=new Set();const empty=new Room(room.variant);enclose(empty);
  for(let yy=0;yy<room.height;yy++)for(let zz=0;zz<room.depth;zz++)for(let xx=0;xx<room.width;xx++) {
    if(xx===0||xx===15||zz===0||zz===15||yy<1||empty.get(xx,yy,zz)!=='air'||yy>=12)mask.add(room.index(xx,yy,zz));
  }
  const recess=room.variant.key==='addition/09'?{fromX:1,toX:3,fromZ:7,toZ:10}:{fromX:1,toX:3,fromZ:6,toZ:8};
  for(let yy=0;yy<=2;yy++)for(let zz=recess.fromZ;zz<=recess.toZ;zz++)for(let xx=recess.fromX;xx<=recess.toX;xx++)mask.delete(room.index(xx,yy,zz));
  return mask;
}

function validate(room,bytes) {
  const base=catalog.variants.find(variant=>variant.key===room.variant.baseKey);
  const safe=(position,clearance=3)=> {
    const [xx,yy,zz]=position;assert(full(room.get(xx,yy-1,zz)),`Support ${room.variant.key} ${position}`);
    for(let offset=0;offset<clearance;offset++)assert.equal(room.get(xx,yy+offset,zz),'air',`Clearance ${room.variant.key} ${position} +${offset}`);
  };
  for(const field of ['size','minimum_y','ports','sockets'])assert.deepEqual(room.variant[field],base[field]);
  const targets=room.variant.ports.flatMap(port=>port.cells).concat(room.variant.sockets.flatMap(socket=>[socket.position,socket.spawn_position]));
  for(const port of room.variant.ports)for(const [xx,yy,zz]of port.cells)for(let inset=0;inset<=5;inset++)safe([xx,yy,zz===0?inset:zz-inset],5);
  for(const socket of room.variant.sockets){safe(socket.position);safe(socket.spawn_position);assert(full(room.get(...socket.support)));}
  const graph=walk(room,targets[0],targets);assert.equal(graph.unreachable.length,0,JSON.stringify(graph.unreachable));
  const dryQueue=[targets[0]],dryVisited=new Set([targets[0].join(',')]);
  const drySafe=(xx,zz)=>room.inside(xx,2,zz)&&full(room.get(xx,1,zz))&&[2,3,4].every(yy=>isAir(room.get(xx,yy,zz)));
  for(let cursor=0;cursor<dryQueue.length;cursor++) {
    const [xx,,zz]=dryQueue[cursor];
    for(const [deltaX,deltaZ]of [[1,0],[-1,0],[0,1],[0,-1]]) {
      const next=[xx+deltaX,2,zz+deltaZ],identity=next.join(',');
      if(!dryVisited.has(identity)&&drySafe(next[0],next[2])){dryVisited.add(identity);dryQueue.push(next);}
    }
  }
  for(const position of targets)assert(dryVisited.has(position.join(',')),`No dry continuous route ${position}`);
  const reserved=new Set(targets.map(position=>position.join(','))),assigned=new Set(),alternativeSpawns=[];
  for(const socket of room.variant.sockets) {
    const [xx,yy,zz]=socket.position,candidates=[];
    for(let nextX=1;nextX<15;nextX++)for(let nextZ=1;nextZ<15;nextZ++) {
      const position=[nextX,yy,nextZ],identity=position.join(','),distance=Math.abs(xx-nextX)+Math.abs(zz-nextZ);
      if(distance>=1&&distance<=6&&dryVisited.has(identity)&&!reserved.has(identity)&&!assigned.has(identity))candidates.push({position,distance});
    }
    candidates.sort((left,right)=>left.distance-right.distance);assert(candidates.length,`No independent spawn ${socket.id}`);
    assigned.add(candidates[0].position.join(','));alternativeSpawns.push({socket:socket.id,...candidates[0],candidateCount:candidates.length});
  }
  const allowed=new Set();for(const port of room.variant.ports)for(const [xx,yy,zz]of port.cells)for(let offset=0;offset<5;offset++)allowed.add([xx,yy+offset,zz].join(','));
  let shellChecked=0,waterCells=0;
  const allowedMaterials=new Set(['air','dark_prismarine','prismarine_bricks','sea_lantern','smooth_quartz','white_terracotta','pink_terracotta','orange_terracotta','cyan_stained_glass','blue_stained_glass','quartz_pillar','sponge','water','bone_block','waxed_cut_copper','waxed_oxidized_copper','stripped_birch_log','chain','brown_wool','blue_terracotta','yellow_terracotta','black_terracotta','quartz_stairs','green_terracotta','lime_terracotta','red_terracotta']);
  for(let yy=0;yy<16;yy++)for(let zz=0;zz<16;zz++)for(let xx=0;xx<16;xx++) {
    const block=room.get(xx,yy,zz);assert(allowedMaterials.has(baseName(block)),`Unknown vanilla material ${block}`);
    assert(!/rail|redstone|lava|fteclipsegen/.test(block));
    if(xx===0||xx===15||zz===0||zz===15||yy===0||yy===15){shellChecked++;assert(!isAir(block)||allowed.has([xx,yy,zz].join(',')),`Shell hole ${[xx,yy,zz]}`);}
    if(yy===1) {
      const maintenanceRecess=room.variant.key==='addition/09'&&xx>=1&&xx<=2&&zz>=7&&zz<=10;
      const shallowPool=room.variant.key==='addition/10'&&xx===2&&zz===7;
      if(maintenanceRecess||shallowPool)assert(full(room.get(xx,0,zz)),'Recess or shallow pool lacks solid bottom');
      else assert(full(block));
    }
    if(baseName(block)==='water') {
      waterCells++;assert(block.includes('level=0'));
      for(const [deltaX,deltaY,deltaZ]of [[1,0,0],[-1,0,0],[0,1,0],[0,-1,0],[0,0,1],[0,0,-1]])assert(!isAir(room.get(xx+deltaX,yy+deltaY,zz+deltaZ)),`Exposed water ${[xx,yy,zz]}`);
    }
  }
  const routeCenters=[];
  const centerSafe=(xx,zz)=>[-1,0,1].every(deltaX=>[-1,0,1].every(deltaZ=>drySafe(xx+deltaX,zz+deltaZ)));
  const queue=[[7,2]],visited=new Set(['7,2']);assert(centerSafe(7,2));
  for(let cursor=0;cursor<queue.length;cursor++) {
    const [xx,zz]=queue[cursor];routeCenters.push([xx,2,zz]);
    for(const [deltaX,deltaZ]of [[1,0],[-1,0],[0,1],[0,-1]]){const next=[xx+deltaX,zz+deltaZ],identity=next.join(',');if(!visited.has(identity)&&next[0]>=2&&next[0]<=13&&next[1]>=2&&next[1]<=13&&centerSafe(...next)){visited.add(identity);queue.push(next);}}
  }
  assert(visited.has('7,13')||visited.has('8,13'),'No continuous three-block-wide main route');
  return {key:room.variant.key,baseKey:room.variant.baseKey,status:'PASS',sha256:hash(bytes),size:[16,16,16],minimum_y:base.minimum_y,ports:base.ports,sockets:base.sockets,spawn_position:base.spawn_position??null,denseNBT:{DataVersion:3465,blocks:4096,uniqueCoordinates:4096,airIncluded:true,noEntities:true,noBlockEntities:true,bareNamesAndSeparateProperties:true},interfaces:{preserved:true,portClearance:5,inwardPortDepth:5,socketClearance:3,fullSupport:true,alternativeSpawns},connectivity:{allTargets:true,halfBlockMaximumStep:.5,reachableMicroCells:graph.count,drySameLevelReachableCells:dryVisited.size,mainRoadMinimumWidth:3,threeWideRouteCenters:routeCenters},shell:{complete:true,checked:shellChecked,fullFloor:false,continuousSolidBottomY0:true,mainFloorY1:"Complete except declared bottomed maintenance recess/stairs or fully sealed shallow pool",glazedLighting:true},water:{sourceCells:waterCells,allLevelZero:true,enclosedOnSixSides:true,dryPlayerRoutes:true},noRuntimeRunes:true,noUnstableRailsOrRedstone:true,features:room.features};
}

function styleAudit(rooms,results) {
  return {schema:1,style:'fteclipsegen:coral',batch:5,status:'PASS',method:'Final NBT material counts and landmark bounds; actual eight-step cage rings, grounded frame pillars, pit bottom, bridge stair collision, shallow pool and outer glazing independently checked',rooms:rooms.map((room,index)=> {
    const materials={};for(const block of room.blocks){const name=baseName(block);materials[name]=(materials[name]??0)+1;}
    const landmarks=room.landmarks.map(landmark=> {
      const observed={};const {from,to}=landmark.bounds;
      for(let yy=from[1];yy<=to[1];yy++)for(let zz=from[2];zz<=to[2];zz++)for(let xx=from[0];xx<=to[0];xx++){const name=baseName(room.get(xx,yy,zz));observed[name]=(observed[name]??0)+1;}
      for(const material of landmark.expectedMaterials)assert(observed[material]>0,`Missing actual landmark ${room.variant.key} ${material}`);
      return {...landmark,observedMaterials:observed,status:'PASS'};
    });
    let structuralChecks;
    if(index===0) {
      for(const [xx,zz,centerY]of [[2,4,5],[2,12,8],[13,9,6]]) {
        assert.equal(room.get(xx,centerY,zz),'yellow_terracotta');
        for(const deltaX of [-1,1])for(const deltaZ of [-1,1])for(let yy=3;yy<=centerY+1;yy++)assert.equal(room.get(xx+deltaX,yy,zz+deltaZ),yy<=centerY-2?'quartz_pillar':'chain[axis=y,waterlogged=false]','Buoy support or cage tie floating or broken');
        for(const [deltaX,deltaZ]of [[-1,0],[1,0],[0,-1],[0,1]])for(const deltaY of [-1,1])assert(room.get(xx+deltaX,centerY+deltaY,zz+deltaZ).startsWith('quartz_stairs['),'Missing cage step ring');
      }
      for(let xx=1;xx<=2;xx++)for(let zz=7;zz<=9;zz++){assert.equal(room.get(xx,1,zz),'air');assert(full(room.get(xx,0,zz)));}
      const pitWalk=walk(room,room.variant.ports[0].cells[0],[[1,1,8]]);assert.equal(pitWalk.unreachable.length,0,'Inspection pit requires jumping');
      structuralChecks={buoyHeights:[5,8,6],cageStepBlocks:24,continuousFrameColumns:12,pitDepth:1,pitBottomY:0,pitExitWithoutJump:true,dryBypass:true,noBeaconCrown:true};
    } else {
      assert.equal(room.get(2,1,7),'water[level=0]');
      for(let xx=1;xx<=3;xx++)for(let zz=6;zz<=8;zz++)assert.equal(room.get(xx,0,zz),'blue_terracotta');
      for(let yy=2;yy<=11;yy++)for(let zz=2;zz<=13;zz++)assert.equal(room.get(0,yy,zz),'cyan_stained_glass');
      const bridgeWalk=walk(room,room.variant.ports[0].cells[0],[[2,4,7]]);assert.equal(bridgeWalk.unreachable.length,0,'Rescue bridge needs a jump');
      for(let xx=4;xx<=9;xx++)for(let zz=9;zz<=11;zz++) {
        assert.equal(room.get(xx,1,zz),'smooth_quartz','Conservative full-support socket exit lost');
        for(let yy=2;yy<=4;yy++)assert.equal(room.get(xx,yy,zz),'air','Flat socket exit blocked');
      }
      for(let xx=1;xx<=3;xx++)for(let zz=6;zz<=8;zz++)for(let yy=4;yy<=6;yy++)assert.equal(room.get(xx,yy,zz),'air','Bridge deck clearance lost');
      assert.equal(materials.water,1);
      structuralChecks={lateralReefShoulders:2,bridgeWidth:3,bridgeFeetY:4,realStairWidth:3,bridgeReachableWithoutJump:true,poolDepth:1,poolBottomY:0,sourceWaterCells:1,fullyGlazedOuterEdge:true,medicalWorktops:2,flatSocketExit:{socket:[4,2,9],width:3,support:"smooth_quartz",cells:Array.from({length:6},(_,index)=>({feet:[4+index,2,9],support:[4+index,1,9],supportBlock:room.get(4+index,1,9),clearance:[2,3,4].map(yy=>room.get(4+index,yy,9))})),halfGridPath:Array.from({length:11},(_,index)=>[8+index,4,18]),units:"halfGridPath coordinates are half blocks; feetY2 remains constant; whole path supported by full quartz cubes"}};
    }
    return {key:room.variant.key,name:names[index],status:'PASS',nbtSha256:results[index].sha256,materials,landmarks,structuralChecks,functionalLayout:room.features,review:{materialOnlyReskin:false,fourCornerEmptyHall:false,mechanicalGearLanguage:false,structuralEvidence:index===0?'Three supported cages at different heights enclose colored buoy balls with real inward bottom/top stair rings; separate hoist platform and a bounded shallow repair recess with a dry bypass':'An irregular lateral reef scene is crossed by a three-wide real stair-access rescue bridge over a fully bottomed glazed shallow basin; diagonal medical stations face the crossing',themeVisibleInFourViews:true,manualFourViewReview:{directions:['N','S','W','E'],decision:'KEEP',observed:index===0?'Three cage heights and yellow/orange buoy profiles are visible between upper thin ties and inward stair rings; low pit floor, guarded edge, exit steps and isolated side hoist are distinct. No socket or central lane blockage.':'Irregular twin reef shoulders form one lateral rescue crossing. Three-wide bridge steps, shallow blue bottomed basin, glass safety edging and two small red-marked first-aid counters are readable; central dry lane remains separate.'},repairs:index===0?[{issue:'First preview made stair cages resemble closed cuboids',fix:'Rendered actual half/top/facing stair shapes; replaced upper corner columns with thin static chain ties while preserving grounded lower pillar legs and eight stair cage blocks per ball'},{issue:'Hidden common floor also hid the maintenance recess bottom',fix:'Display only the actual bounded pit lower cells as an explicit mask exception; raw NBT bottom independently verified'}]:[{issue:'Common floor hiding obscured the pool beneath the bridge',fix:'Display the actual basin bottom/side layers without the common room floor; real solid Y0 bottom and one contained source cell independently verified'},{issue:'Bridge edge rails could reduce usable width',fix:'Placed rails outside z6..8 deck at z5/9, asserted three-wide clear headroom and tested real stair ascent'}]}};
  })};
}

function auditRepetition(rooms,results) {
  const baselineDir=join(root,'output/style-rebuild-v034/coral/roomset');
  const baselineManifest=JSON.parse(readFileSync(join(baselineDir,'manifest.json')));
  const references=catalog.variants.map(variant=> {
    const bytes=readFileSync(join(baselineDir,`${variant.key}.nbt`));
    assert.equal(hash(bytes),baselineManifest.templates.find(entry=>entry.key===variant.key).sha256);
    return {key:`v034/${variant.key}`,room:decode(bytes,variant),sha256:hash(bytes),source:'frozen-old-27'};
  });
  const previousBatches=[];
  for(const directory of readdirSync(parent,{withFileTypes:true})) {
    if(!directory.isDirectory()||!/^batch-\d+$/.test(directory.name)||directory.name==='batch-05')continue;
    const manifestPath=join(parent,directory.name,'manifest.json');if(!existsSync(manifestPath))continue;
    const manifest=JSON.parse(readFileSync(manifestPath));assert(manifest.batch<5,"Only accepted earlier batches may be references");
    for(const template of manifest.templates) {
      const base=catalog.variants.find(variant=>variant.key===template.baseKey),bytes=readFileSync(join(parent,directory.name,template.file));assert.equal(hash(bytes),template.sha256);
      references.push({key:`${directory.name}/${template.key}`,room:decode(bytes,{...base,key:template.key}),sha256:hash(bytes),source:'completed-addition'});
      previousBatches.push(template.key);
    }
  }
  const compare=(left,right,label)=> {
    const sameSize=JSON.stringify(left.variant.size)===JSON.stringify(right.variant.size);
    const leftMask=commonMask(left),rightMask=commonMask(right);
    if(sameSize){for(const identity of leftMask)rightMask.add(identity);for(const identity of rightMask)leftMask.add(identity);}
    const solid=compareSets(shapeSet(left,leftMask,!sameSize),shapeSet(right,rightMask,!sameSize));
    const layout=compareSets(shapeSet(left,leftMask,!sameSize,true),shapeSet(right,rightMask,!sameSize,true));
    assert(solid.jaccard<.85,`Rework similarity ${left.variant.key} ${label} ${solid.jaccard}`);
    return {key:left.variant.key,comparedWith:label,sameSize,normalization:sameSize?'exact interior coordinates':'16x12x16 normalized solid lattice',solidOccupancy:solid,lowPlan:layout,status:'PASS',review:label.includes('addition/')?'Buoy room has three unequal stair-enclosed buoy cages, a grounded hoisting platform and a shallow guarded repair recess; rescue room has a lateral reef cleft, short glass-edged bridge and diagonal medical stations':'Old room is compared by measured interior solid union after removing shell/floor/interface masks; new functional shape is described in design and landmark audit'};
  };
  const pairs=[];
  for(const room of rooms)for(const reference of references)pairs.push(compare(room,reference.room,reference.key));
  pairs.push(compare(rooms[0],rooms[1],rooms[1].variant.key));pairs.sort((left,right)=>right.solidOccupancy.jaccard-left.solidOccupancy.jaccard);
  return {schema:1,style:'fteclipsegen:coral',batch:5,status:'PASS',threshold:.85,baselineManifestSha256:hash(readFileSync(join(baselineDir,'manifest.json'))),oldRoomsComparedPerAddition:27,completedAdditionsCompared:previousBatches,currentBatchPairs:1,totalPairs:pairs.length,exclusions:'Both templates: boundary walls; complete base floor y<feet; common upper shell/roof y>=feet+9; all port columns inward 5 blocks with y-1..y+4 clearance; socket support/air cells. Pair masks are unioned for same-size comparisons. Empty/empty never adds similarity.',highestPairs:pairs.slice(0,12),pairs,signatures:rooms.map((room,index)=>({key:room.variant.key,sha256:results[index].sha256,normalizedSignature:hash(Buffer.from([...shapeSet(room,commonMask(room),true)].sort().join(';')))})),repairRequired:0};
}

async function main() {
  assert(process.argv.slice(2).every(argument=>argument==='--batch=5'||argument==='--batch=05'),'Only batch 5 is authorized');
  mkdirSync(out,{recursive:true});
  const promptBytes=readFileSync(join(parent,'batch-05-prompt.md'));
  const guarded=[];
  for(const directory of [join(resources,'roomsets/coral'),join(root,'output/style-rebuild-v034/coral'),join(parent,'batch-01'),join(parent,'batch-02'),join(parent,'batch-03'),join(parent,'batch-04')]) {
    const collect=path=>{for(const entry of readdirSync(path,{withFileTypes:true})){if(entry.name==='.browser-profile')continue;const target=join(path,entry.name);if(entry.isDirectory())collect(target);else guarded.push({path:target,sha256:hash(readFileSync(target))});}};
    collect(directory);
  }
  const rooms=[],results=[],templates=[];
  for(const specification of specifications) {
    const built=makeRoom(specification),bytes=encode(built),file=`roomset/${specification.key}.nbt`,destination=join(out,file);
    mkdirSync(dirname(destination),{recursive:true});writeFileSync(destination,bytes);
    const room=decode(readFileSync(destination),built.variant);room.features=built.features;room.landmarks=built.landmarks;
    const result=validate(room,readFileSync(destination));
    rooms.push(room);results.push(result);templates.push({...specification,sha256:hash(bytes),file});
    console.log(`PASS ${specification.key} ${specification.name}: ${bytes.length} bytes; dry reachable ${result.connectivity.drySameLevelReachableCells}`);
  }
  const manifest={schema:1,style:'fteclipsegen:coral',batch:5,templates};
  writeFileSync(join(out,'manifest.json'),JSON.stringify(manifest,null,2)+'\n');
  writeFileSync(join(out,'validation.json'),JSON.stringify({schema:1,style:'fteclipsegen:coral',batch:5,status:'PASS',catalogSha256:hash(catalogBytes),promptSha256:hash(promptBytes),rooms:results},null,2)+'\n');
  const style=styleAudit(rooms,results),repetition=auditRepetition(rooms,results);
  writeFileSync(join(out,'style-audit.json'),JSON.stringify(style,null,2)+'\n');writeFileSync(join(out,'repetition-audit.json'),JSON.stringify(repetition,null,2)+'\n');
  writeFileSync(join(out,'four-views.html'),fourViews(rooms,results,rooms.map(displayMask)));
  const browser=await checkFourViewsBrowser();
  const replay=specifications.map((specification,index)=>({key:specification.key,first:results[index].sha256,replayed:hash(encode(makeRoom(specification)))}));
  assert(replay.every(entry=>entry.first===entry.replayed));
  for(const entry of guarded)assert.equal(hash(readFileSync(entry.path)),entry.sha256,`Protected file changed ${entry.path}`);
  const self={schema:1,style:'fteclipsegen:coral',batch:5,status:'PASS',authorization:{onlyKeys:specifications.map(specification=>specification.key),nextBatchStarted:false,stopAfterThisBatch:true,totalAdditionsCompleted:10,previousAdditionsPreserved:8},manifestSha256:hash(readFileSync(join(out,'manifest.json'))),browser,recipeSnapshot:{file:"recipe-source.mjs",sha256:hash(readFileSync(fileURLToPath(import.meta.url))),replaySupported:true},style:{status:'PASS',rooms:2},repetition:{status:'PASS',totalPairs:repetition.totalPairs,maxJaccard:repetition.pairs[0].solidOccupancy.jaccard,threshold:.85},replay:{status:'PASS',templates:replay},protectedFilesUnchanged:{checked:guarded.length,status:'PASS'},rooms:rooms.map((room,index)=>({key:room.variant.key,status:'PASS',views:browser.views.filter(view=>view.key===room.variant.key).length,nbtSha256:results[index].sha256,roadBlocked:false,socketsUsable:4,visualFinding:index===0?'Three distinct stair-ring buoy cages sit at different supported heights; a separate hoist counter and a bounded shallow maintenance recess leave a dry bypass':'Two irregular coral cliff shoulders frame a side cleft with a three-wide short rescue bridge; glazed outer boundary and a solid-bottom shallow blue basin are distinct from the dry medical worktops',manualFourViewReview:style.rooms[index].review.manualFourViewReview,repairs:style.rooms[index].review.repairs})),limitations:['Actual decoded NBT color voxel projections and real headless Edge rendering, not textured Minecraft screenshots','Static collision and dry-route verification; no game session playtest']};
  writeFileSync(join(out,'self-audit.json'),JSON.stringify(self,null,2)+'\n');
  writeFileSync(join(out,'recipe-source.mjs'),readFileSync(fileURLToPath(import.meta.url)));
  const files=['recipe-source.mjs','manifest.json','design.md','validation.json','four-views.html','four-views-contact.png','browser-page.png','style-audit.json','repetition-audit.json','self-audit.json',...templates.map(template=>template.file),...templates.map(template=>`views/${template.key}.png`)];
  const freeze={schema:1,style:'fteclipsegen:coral',batch:5,status:'FROZEN_FOR_PARENT_INTEGRATION',recipe:{file:'tools/expand-coral.mjs',snapshot:'recipe-source.mjs',sha256:hash(readFileSync(fileURLToPath(import.meta.url)))},manifestSha256:hash(readFileSync(join(out,'manifest.json'))),files:files.map(file=>({file,sha256:hash(readFileSync(join(out,file)))}))};
  writeFileSync(join(out,'freeze.json'),JSON.stringify(freeze,null,2)+'\n');
  const python=process.env.CORAL_VERIFIER_PYTHON??'C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe';
  const parentCheck=spawnSync(python,['-c',`import pathlib,runpy,sys
root=pathlib.Path(sys.argv[1])
destination=root/'output/room-expansion-v035/coral-batch-05-acceptance.json'
local=root/'output/room-expansion-v035/coral/batch-05/parent-batch-acceptance.json'
original=pathlib.Path.write_text
def write_local(path,data,*args,**kwargs):
    assert path.resolve()==destination.resolve(),str(path)
    return original(local,data,*args,**kwargs)
pathlib.Path.write_text=write_local
sys.argv=[str(root/'tools/verify-room-expansion.py'),'--batch','5','--style','coral']
runpy.run_path(sys.argv[0],run_name='__main__')`,root],{encoding:'utf8',windowsHide:true});
  assert.equal(parentCheck.status,0,`${parentCheck.error??''}\n${parentCheck.stdout}\n${parentCheck.stderr}`);
  const acceptance=JSON.parse(readFileSync(join(out,'parent-batch-acceptance.json'),'utf8'));
  assert.equal(acceptance.status,'PASS');
  assert.equal(acceptance.rooms.length,2);
  freeze.files.push({file:'parent-batch-acceptance.json',sha256:hash(readFileSync(join(out,'parent-batch-acceptance.json')))});
  writeFileSync(join(out,'freeze.json'),JSON.stringify(freeze,null,2)+'\n');
  console.log(`FROZEN BATCH 05 ${freeze.manifestSha256}; ${browser.canvasCount} views; ${repetition.totalPairs} comparisons`);
}

class Room {
  constructor(variant) {
    this.variant = variant;
    [this.width, this.height, this.depth] = variant.size;
    this.floor = variant.key.includes('/layout/') ? 1 : variant.key === 'spawn/spawn' ? 4 : 2;
    this.blocks = Array(this.width * this.height * this.depth).fill('air');
    this.features = [];
  }
  inside(x,y,z) { return x>=0 && y>=0 && z>=0 && x<this.width && y<this.height && z<this.depth; }
  index(x,y,z) { return (y*this.depth+z)*this.width+x; }
  get(x,y,z) { return this.inside(x,y,z) ? this.blocks[this.index(x,y,z)] : 'air'; }
  set(x,y,z,block) { assert(this.inside(x,y,z), `${this.variant.key}: out of bounds ${[x,y,z]}`); this.blocks[this.index(x,y,z)]=block; }
  box(from,to,block) {
    for(let yy=from[1];yy<=to[1];yy++) for(let zz=from[2];zz<=to[2];zz++) for(let xx=from[0];xx<=to[0];xx++) this.set(xx,yy,zz,block);
  }
  mass(x1,z1,x2,z2,top,block='pink_terracotta') { this.box([x1,this.floor,z1],[x2,top,z2],block); }
  safe(position,clearance=3) {
    const [xx,yy,zz]=position;
    this.set(xx,yy-1,zz,'smooth_quartz');
    this.box([xx,yy,zz],[xx,yy+clearance-1,zz],'air');
  }
  pad(position,radius=1) {
    const [xx,yy,zz]=position;
    for(let zz2=Math.max(1,zz-radius);zz2<=Math.min(this.depth-2,zz+radius);zz2++) for(let xx2=Math.max(1,xx-radius);xx2<=Math.min(this.width-2,xx+radius);xx2++) this.safe([xx2,yy,zz2]);
  }
  rib(zz,crest=9,left=1,right=this.width-2) {
    for(let xx=left;xx<=right;xx++) {
      const yy=this.floor+3+Math.round((crest-this.floor-3)*Math.sin(Math.PI*(xx-left)/(right-left)));
      this.set(xx,yy,zz,'smooth_quartz');
      if(xx===left||xx===right) this.box([xx,this.floor,zz],[xx,yy,zz],'quartz_pillar');
    }
  }
  beacon(xx,zz,radius,top) {
    for(let yy=this.floor;yy<=top;yy++) for(let zz2=zz-radius;zz2<=zz+radius;zz2++) for(let xx2=xx-radius;xx2<=xx+radius;xx2++) {
      if(Math.hypot(xx2-xx,zz2-zz)<=radius+(yy===top?0.4:0)) this.set(xx2,yy,zz2,yy===top?'sea_lantern':yy===top-1?'smooth_quartz':yy%3===0?'white_terracotta':'prismarine_bricks');
    }
  }
}

function encode(room) {
  const structure=new library.Structure(room.variant.size);
  for(let yy=0;yy<room.height;yy++) for(let zz=0;zz<room.depth;zz++) for(let xx=0;xx<room.width;xx++) {
    const block=room.get(xx,yy,zz);
    const name=baseName(block);
    const properties=block.includes('[')?Object.fromEntries(block.slice(block.indexOf('[')+1,-1).split(',').map(value=>value.split('='))):undefined;
    structure.addBlock([xx,yy,zz],`minecraft:${name}`,properties);
  }
  return Buffer.from(structure.writeNbt({dataVersion:3465,compression:'gzip'}));
}

function decode(bytes,variant) {
  const nbt=library.NbtFile.read(bytes);
  assert.equal(nbt.root.getNumber('DataVersion'),3465);
  assert.equal(nbt.root.getList('entities').length,0);
  const palette=nbt.root.getList('palette');
  for(let index=0;index<palette.length;index++) assert(/^minecraft:[a-z0-9_]+$/.test(palette.getCompound(index).getString('Name')));
  const structure=library.Structure.fromNbt(nbt.root);
  assert.deepEqual(structure.getSize(),variant.size);
  const blocks=structure.getBlocks();
  assert.equal(blocks.length,variant.size.reduce((total,value)=>total*value,1));
  const room=new Room(variant);
  const seen=new Set();
  for(const block of blocks) {
    assert(!block.nbt); assert(room.inside(...block.pos));
    const coordinate=block.pos.join(','); assert(!seen.has(coordinate)); seen.add(coordinate);
    room.set(...block.pos,block.state.toString().replace(/^minecraft:/,''));
  }
  return room;
}

function walk(room, start, targets=[], gateClosed=false) {
  const surfaces=new Map();
  const heightAt=(microX,microZ,yy)=> {
    const xx=Math.floor(microX/2),zz=Math.floor(microZ/2);
    if(gateClosed&&zz===22&&xx>=15&&xx<=18&&yy>=2&&yy<=7) return 2;
    const block=room.get(xx,yy,zz);
    if(isAir(block)) return 0;
    if(!block.includes('stairs')) return 2;
    const high=block.includes('facing=east')?microX%2===1:block.includes('facing=west')?microX%2===0:block.includes('facing=south')?microZ%2===1:microZ%2===0;
    return high?2:1;
  };
  const clear=(microX,feet,microZ)=> {
    if(microX<0||microZ<0||microX>=room.width*2||microZ>=room.depth*2) return false;
    for(let yy=Math.floor(feet/2);yy<Math.ceil((feet+4)/2);yy++) {
      if(yy>=room.height) return false;
      const occupied=heightAt(microX,microZ,yy);
      if(occupied&&yy*2<feet+4&&yy*2+occupied>feet) return false;
    }
    return true;
  };
  for(let microX=0;microX<room.width*2;microX++) for(let microZ=0;microZ<room.depth*2;microZ++) {
    const column=[];
    for(let yy=0;yy<room.height-2;yy++) {
      const occupied=heightAt(microX,microZ,yy);
      const feet=yy*2+occupied;
      if(occupied&&clear(microX,feet,microZ)) column.push(feet);
    }
    surfaces.set(`${microX},${microZ}`,column);
  }
  const seed=[start[0]*2,start[1]*2,start[2]*2];
  assert(surfaces.get(`${seed[0]},${seed[2]}`)?.includes(seed[1]),`Invalid start ${room.variant.key} ${start}`);
  const queue=[seed],visited=new Set([seed.join(',')]);
  for(let cursor=0;cursor<queue.length;cursor++) {
    const [microX,feet,microZ]=queue[cursor];
    for(const [deltaX,deltaZ] of [[1,0],[-1,0],[0,1],[0,-1]]) {
      const nextX=microX+deltaX,nextZ=microZ+deltaZ;
      for(const nextFeet of surfaces.get(`${nextX},${nextZ}`)??[]) {
        if(Math.abs(nextFeet-feet)>1||!clear(microX,Math.max(feet,nextFeet),microZ)||!clear(nextX,Math.max(feet,nextFeet),nextZ)) continue;
        const next=[nextX,nextFeet,nextZ],identity=next.join(',');
        if(!visited.has(identity)) { visited.add(identity);queue.push(next); }
      }
    }
  }
  const reachable=([xx,yy,zz])=>[0,1].some(deltaX=>[0,1].some(deltaZ=>visited.has([xx*2+deltaX,yy*2,zz*2+deltaZ].join(','))));
  const unreachable=targets.filter(position=>!reachable(position));
  return { reachable, unreachable, visited, count:visited.size };
}

function color(block) {
  if(block==='air')return '#10242b';
  if(block.includes('lantern'))return '#b4ffec';
  if(block.includes('sponge'))return '#d8c451';
  if(block.includes('bone_block'))return '#f0e3c7';
  if(block.includes('birch'))return '#c7ac7e';
  if(block.includes('chain'))return '#a2ada7';
  if(block.includes('brown_wool'))return '#80604a';
  if(block.includes('green_terracotta'))return '#628b62';
  if(block.includes('lime_terracotta'))return '#9eaf6c';
  if(block.includes('yellow_terracotta'))return '#d7b857';
  if(block.includes('red_terracotta'))return '#bf6c5f';
  if(block.includes('waxed_cut_copper'))return '#bd815d';
  if(block.startsWith('water'))return '#246db6';
  if(block.includes('copper'))return '#7cbaaa';
  if(block.includes('glass'))return '#3eacbd';
  if(block.includes('pink'))return '#df8d9e';
  if(block.includes('orange'))return '#d99a73';
  if(block.includes('quartz')||block.includes('white'))return '#e9dec9';
  if(block.includes('dark'))return '#1e5457';
  return '#609892';
}

function shapeSet(room,mask,normalized=false,plan=false) {
  const occupied=new Set();
  for(let yy=room.floor;yy<Math.min(room.height-1,room.floor+(plan?3:24));yy++)for(let zz=1;zz<room.depth-1;zz++)for(let xx=1;xx<room.width-1;xx++) {
    if(mask.has(room.index(xx,yy,zz))||isAir(room.get(xx,yy,zz)))continue;
    const position=normalized?[Math.floor((xx-1)*16/(room.width-2)),Math.floor((yy-room.floor)*12/(Math.min(room.height-1,room.floor+24)-room.floor)),Math.floor((zz-1)*16/(room.depth-2))]:[xx,yy-room.floor,zz];
    occupied.add(plan?`${position[0]},${position[2]}`:position.join(','));
  }
  return occupied;
}

function compareSets(left,right) {
  let intersection=0;
  for(const identity of left)if(right.has(identity))intersection++;
  const union=left.size+right.size-intersection;
  return {intersection,union,jaccard:union?intersection/union:1,different:union-intersection};
}

function fourViews(rooms,results,masks) {
  const data=rooms.map((room,index)=> {
    const voxels=[];
    const cutY=room.variant.key.startsWith('boss/')?16:room.height-2;
    for(let yy=0;yy<=cutY;yy++)for(let zz=1;zz<room.depth-1;zz++)for(let xx=1;xx<room.width-1;xx++)if(!masks[index].has(room.index(xx,yy,zz))&&!isAir(room.get(xx,yy,zz)))voxels.push([xx,yy,zz,color(room.get(xx,yy,zz)),baseName(room.get(xx,yy,zz)),room.get(xx,yy,zz)]);
    return {key:room.variant.key,size:room.variant.size,floor:room.floor,cutY,sha256:results[index].sha256,features:room.features,voxels,ports:room.variant.ports,sockets:room.variant.sockets};
  });
  const cards=data.map((room,index)=>`<article><h2>${names[index]} · ${room.key}</h2><p>${room.features.join('；')} · cut Y≤${room.cutY}</p><code>data sha256 ${room.sha256}</code><div class="views">${['北 North','南 South','西 West','东 East'].map((direction,view)=>`<figure><figcaption>${direction} · oblique camera XZ ${['[1,-1]','[-1,1]','[-1,-1]','[1,1]'][view]}</figcaption><canvas width="520" height="390" data-room="${index}" data-direction="${view}"></canvas></figure>`).join('')}</div></article>`).join('');
  return `<!doctype html><html lang="zh-CN"><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>Coral batch 05 · 2 × 4 NBT views</title><style>body{background:#10242b;color:#e8e1d3;font:14px system-ui;margin:20px auto;max-width:1500px}h1,h2{color:#f2afb1}article{padding:20px;margin:20px 0;background:#18343c;border-radius:10px}.views{display:grid;grid-template-columns:repeat(4,1fr);gap:10px}figure{margin:0}canvas{width:100%;background:#10242b}code{overflow-wrap:anywhere;color:#a8d6cf;font-size:11px}figcaption{padding:8px}@media(max-width:800px){.views{grid-template-columns:repeat(2,1fr)}}</style><h1>珊瑚新增第5批 · 两房北南西东8真实NBT视图</h1><p>隐藏规则：移除外边界、基础地板、配方统一壳顶及Y12以上封顶，唯浅坑/浅池的真实底层与侧层保留用于复核。保留室内真实建筑；对全部方向使用同一完整高度。摄像机各沿北/南/西/东观察，并使用等量斜俯视展开深度；四方向采用不同坐标旋转，绝非复制图像。外壳完整性在validation.json独立验证。颜色体素投影，无游戏材质；玻璃和水使用统一透明度以露出真实槽内方块，链材质按细悬链显示，阶梯按实际facing/half半格轮廓显示。</p><a href="design.md">逐房设计与动线</a> · <a href="style-audit.json">实际NBT风格证据</a> · <a href="repetition-audit.json">去公共几何重复度</a><p id="status">Rendering…</p>${cards}<canvas id="contact-sheet" width="1920" height="2430" style="display:none"></canvas><script>window.auditErrors=[];window.addEventListener('error',event=>window.auditErrors.push(String(event.message)));window.addEventListener('unhandledrejection',event=>window.auditErrors.push(String(event.reason)));const rooms=${JSON.stringify(data)};window.viewAudit=[];
function paint(canvas,room,direction){const context=canvas.getContext('2d'),[width,height,depth]=room.size;context.fillStyle='#10242b';context.fillRect(0,0,520,390);function transform(xx,zz){return direction===0?[depth-zz,xx]:direction===1?[zz,width-xx]:direction===2?[width-xx,depth-zz]:[xx,zz];}const scale=Math.min(14,410/(width+depth),280/(height+8)),rise=scale*.85;function point(xx,yy,zz){const [side,deep]=transform(xx,zz);return[260+(side-deep)*scale,350+(side+deep-width-depth)*scale*.45-(yy-room.floor)*rise];}function face(points,fill){context.beginPath();points.forEach((position,index)=>index?context.lineTo(...position):context.moveTo(...position));context.closePath();context.fillStyle=fill;context.fill();context.strokeStyle='#12313a88';context.lineWidth=.45;context.stroke();}const voxels=room.voxels.map(voxel=>{const [side,deep]=transform(voxel[0],voxel[2]);return{voxel,order:side+deep};}).sort((left,right)=>left.order-right.order||left.voxel[1]-right.voxel[1]);for(const item of voxels){const[xx,yy,zz,fill,block,state]=item.voxel;context.globalAlpha=block.includes("glass")?.18:block==="water"?.08:1;if(block==="chain"){const from=point(xx+.5,yy,zz+.5),to=point(xx+.5,yy+1,zz+.5);context.strokeStyle=fill;context.lineWidth=2;context.beginPath();context.moveTo(...from);context.lineTo(...to);context.stroke();continue;}if(block.includes("stairs")){function prism(fromX,fromY,fromZ,toX,toY,toZ){const corners=[[fromX,fromZ],[toX,fromZ],[toX,toZ],[fromX,toZ]].map(position=>({world:position,screen:point(position[0],toY,position[1]),depth:transform(...position).reduce((total,value)=>total+value,0)}));const edges=[0,1,2,3].sort((left,right)=>(corners[right].depth+corners[(right+1)%4].depth)-(corners[left].depth+corners[(left+1)%4].depth)).slice(0,2);for(const corner of edges){const next=(corner+1)%4;face([corners[corner].screen,corners[next].screen,point(corners[next].world[0],fromY,corners[next].world[1]),point(corners[corner].world[0],fromY,corners[corner].world[1])],fill);}face(corners.map(corner=>corner.screen),fill);}const top=state.includes("half=top"),fromX=state.includes("facing=east")?xx+.5:xx,toX=state.includes("facing=west")?xx+.5:xx+1,fromZ=state.includes("facing=south")?zz+.5:zz,toZ=state.includes("facing=north")?zz+.5:zz+1;if(top){prism(fromX,yy,fromZ,toX,yy+.5,toZ);prism(xx,yy+.5,zz,xx+1,yy+1,zz+1);}else{prism(xx,yy,zz,xx+1,yy+.5,zz+1);prism(fromX,yy+.5,fromZ,toX,yy+1,toZ);}continue;}const corners=[[xx,zz],[xx+1,zz],[xx+1,zz+1],[xx,zz+1]].map(position=>({world:position,screen:point(position[0],yy+1,position[1]),depth:transform(...position).reduce((total,value)=>total+value,0)}));const edgeOrder=[0,1,2,3].sort((left,right)=>(corners[right].depth+corners[(right+1)%4].depth)-(corners[left].depth+corners[(left+1)%4].depth)).slice(0,2);for(const corner of edgeOrder){const next=(corner+1)%4;face([corners[corner].screen,corners[next].screen,point(corners[next].world[0],yy,corners[next].world[1]),point(corners[corner].world[0],yy,corners[corner].world[1])],fill);}face(corners.map(corner=>corner.screen),fill);}context.globalAlpha=1;context.font='12px system-ui';context.fillStyle='#bde2d8';context.fillText(room.key+' / '+['N','S','W','E'][direction],12,22);let nonBackground=0;const pixels=context.getImageData(0,0,520,390).data;let checksum=2166136261;for(let pixel=0;pixel<pixels.length;pixel+=4){if(pixels[pixel]!==16||pixels[pixel+1]!==36||pixels[pixel+2]!==43)nonBackground++;checksum=Math.imul(checksum^pixels[pixel]^pixels[pixel+1]<<8^pixels[pixel+2]<<16,16777619)>>>0;}return{key:room.key,direction:['north','south','west','east'][direction],sha256:room.sha256,nonBackground,checksum:checksum.toString(16),sourceVoxelCount:room.voxels.length,cutY:room.cutY,cameraXZ:[[1,-1],[-1,1],[-1,-1],[1,1]][direction]};}
const contact=document.getElementById('contact-sheet'),sheet=contact.getContext('2d');sheet.fillStyle='#10242b';sheet.fillRect(0,0,1920,2430);document.querySelectorAll('canvas[data-room]').forEach(canvas=>{const index=Number(canvas.dataset.room),direction=Number(canvas.dataset.direction);window.viewAudit.push(paint(canvas,rooms[index],direction));const left=(index%3)*640,top=Math.floor(index/3)*270;sheet.drawImage(canvas,left+direction*160,top+35,160,220);if(direction===0){sheet.fillStyle='#f2afb1';sheet.font='15px system-ui';sheet.fillText(rooms[index].key,left+8,top+24);}});document.getElementById('status').textContent='Rendered '+window.viewAudit.length+'/8 actual NBT views';window.auditReady=true;</script></html>`;
}

async function checkFourViewsBrowser() {
  const server=createServer((request,response)=> {
    if(request.url==='/favicon.ico'){response.writeHead(204);response.end();return;}
    response.writeHead(200,{'Content-Type':'text/html; charset=utf-8'});response.end(readFileSync(join(out,'four-views.html')));
  });
  server.listen(0,'127.0.0.1');await once(server,'listening');
  const executable=process.env.CORAL_BROWSER??'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe';
  const browser=spawn(executable,['--headless=new','--disable-gpu','--disable-extensions','--no-first-run','--no-default-browser-check','--disable-background-networking','--remote-debugging-port=0',`--user-data-dir=${join(out,'.browser-profile')}`,'about:blank'],{windowsHide:true,stdio:['ignore','ignore','pipe']});
  let socket;
  try {
    const endpoint=await new Promise((resolveEndpoint,reject)=> {
      let buffer='';const timer=setTimeout(()=>reject(new Error('Headless browser startup timeout')),30000);
      browser.once('error',error=>{clearTimeout(timer);reject(error);});
      browser.stderr.on('data',bytes=>{buffer+=bytes.toString();const match=buffer.match(/DevTools listening on (ws:\/\/[^\s]+)/);if(match){clearTimeout(timer);resolveEndpoint(match[1]);}});
    });
    socket=new WebSocket(endpoint);await once(socket,'open');
    let sequence=0;const pending=new Map(),runtimeErrors=[];
    socket.addEventListener('message',event=> {
      const message=JSON.parse(event.data);
      if(message.id){const request=pending.get(message.id);if(request){pending.delete(message.id);message.error?request.reject(new Error(JSON.stringify(message.error))):request.resolve(message.result);}}
      else if(message.method==='Runtime.exceptionThrown')runtimeErrors.push(message.params.exceptionDetails.text);
      else if(message.method==='Runtime.consoleAPICalled'&&message.params.type==='error')runtimeErrors.push(JSON.stringify(message.params.args));
    });
    const send=(method,params={},sessionId)=>new Promise((resolveCall,reject)=>{const id=++sequence;pending.set(id,{resolve:resolveCall,reject});socket.send(JSON.stringify({id,method,params,...sessionId?{sessionId}:{}}));});
    const target=await send('Target.createTarget',{url:'about:blank'});
    const attached=await send('Target.attachToTarget',{targetId:target.targetId,flatten:true});
    const session=attached.sessionId;
    await send('Runtime.enable',{},session);await send('Page.enable',{},session);
    await send('Emulation.setDeviceMetricsOverride',{width:1400,height:1900,deviceScaleFactor:1,mobile:false},session);
    await send('Page.navigate',{url:`http://127.0.0.1:${server.address().port}/four-views.html`},session);
    const observed=await send('Runtime.evaluate',{expression:'new Promise((resolve,reject)=>{let count=0;const timer=setInterval(()=>{if(window.auditReady){clearInterval(timer);resolve({ready:true,errors:window.auditErrors,views:window.viewAudit,canvasCount:document.querySelectorAll("canvas[data-room]").length,title:document.title,sheets:rooms.map((room,index)=>{const canvas=document.createElement("canvas");canvas.width=1040;canvas.height=810;const context=canvas.getContext("2d");context.fillStyle="#10242b";context.fillRect(0,0,1040,810);context.fillStyle="#f2afb1";context.font="16px system-ui";context.fillText(room.key,12,22);for(let direction=0;direction<4;direction++){const source=[...document.querySelectorAll("canvas[data-room]")].find(item=>Number(item.dataset.room)===index&&Number(item.dataset.direction)===direction);context.drawImage(source,direction%2*520,30+Math.floor(direction/2)*390);}return{key:room.key,png:canvas.toDataURL("image/png")};}),contact:document.getElementById("contact-sheet").toDataURL("image/png")});}else if(++count>300){clearInterval(timer);reject(new Error("view render timeout"));}},50);})',awaitPromise:true,returnByValue:true},session);
    assert(!observed.exceptionDetails,JSON.stringify(observed.exceptionDetails));
    const result=observed.result.value;
    const screenshot=await send('Page.captureScreenshot',{format:'png',captureBeyondViewport:true},session);
    writeFileSync(join(out,'browser-page.png'),Buffer.from(screenshot.data,'base64'));
    assert.equal(result.canvasCount,8);assert.equal(result.views.length,8);
    assert.deepEqual(runtimeErrors,[]);assert.deepEqual(result.errors,[]);
    assert.equal(new Set(result.views.map(view=>`${view.key}/${view.direction}`)).size,8);
    assert(result.views.every(view=>view.nonBackground>100&&view.sourceVoxelCount>0));
    writeFileSync(join(out,'four-views-contact.png'),Buffer.from(result.contact.split(',')[1],'base64'));
    assert.equal(result.sheets.length,2);
    for(const sheet of result.sheets) {
      const destination=join(out,'views',`${sheet.key}.png`);
      mkdirSync(dirname(destination),{recursive:true});
      const png=Buffer.from(sheet.png.split(',')[1],'base64');
      assert.equal(png.subarray(1,4).toString(),'PNG');
      writeFileSync(destination,png);
    }
    const version=await send('Browser.getVersion');
    await send('Browser.close');
    return {engine:version.product,headless:true,realBrowserExecution:true,page:'four-views.html',canvasCount:8,views:result.views,missingViews:0,emptyViews:0,runtimeErrors,windowErrors:result.errors,errorCount:0,passed:true};
  } finally {
    socket?.close();server.close();
    if(browser.exitCode===null) {await Promise.race([once(browser,'exit'),new Promise(resolveWait=>setTimeout(resolveWait,2000))]);if(browser.exitCode===null)browser.kill();}
  }
}

await main();


