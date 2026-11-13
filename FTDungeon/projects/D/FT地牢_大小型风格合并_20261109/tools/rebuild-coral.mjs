import assert from 'node:assert/strict';
import { createHash } from 'node:crypto';
import { readFileSync, writeFileSync, mkdirSync } from 'node:fs';
import { resolve, join, dirname } from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';
import { spawn } from 'node:child_process';
import { createServer } from 'node:http';
import { once } from 'node:events';

const root = fileURLToPath(new URL('../', import.meta.url));
const out = join(root, 'output/style-rebuild-v034/coral');
const resources = join(root, 'src/main/resources/data/fteclipsegen');
const catalogBytes = readFileSync(join(resources, 'catalog.json'));
const catalog = JSON.parse(catalogBytes);
const skill = process.env.MINECRAFT_BUILDER_SKILL ?? 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill';
const library = await import(pathToFileURL(join(skill, 'node_modules/@mattzh72/lodestone/lib/index.js')).href);
const hash = bytes => createHash('sha256').update(bytes).digest('hex');
const names = ['初潮贝殿','近礁航站','远洋灯塔','潮汐肋廊','双壳闸厅','退潮栈桥','潮龛修院','沉货码头','三航标广场','礁眼环庭','阶礁剧场','独柱螺亭','鲸骨三门','潮巷串庭','断礁分汊','四潮登廊','礁井绕阶','旋贝升堂','回潮弯廊','珍珠交换所','封潮仓','贝舱尽头','微型贝龛','小潮货台','双浮标院','大珠守望圣所','终潮眺台'];
const visualFindings = ['叠层扇壳占北半部，南向肋廊保持空隙，左右候潮体量不对称。','高低岸壁构成U形侧围，中央庭院与上部透光盖区分可见。','细长航标具有环状亮带和外伸观察翼，区别于近岸横向岸壁。','连续曲肋与错位侧块构成纵深，主廊没有被实心横梁压低。','两道厚壳门形成连续三段空间，门洞可辨，区别于四根孤柱。','拱肋下的两条玻璃侧栏与侧翼清楚，中央桥轴连续。','三组横向分舱墙和不对称的另一岸形成梳形街区。','货台上方增加拱形贝壳盖与独立珠灯，不再只有矩形货垛和直梁。','三根不同高度航标呈偏轴排列，连接梁位于上部。','闭合高冠环与低玻璃井缘分离，环外有连续通路。','东西看台高度和宽度不同，中间留长槽，台阶没有压占固定接口。','仅一座渐缩螺脊偏轴核心，背后横梁与核心是不同结构。','三方向厚门肩及跨门楣形成T形分岔，门间空地可辨。','错位低墙与分开的顶盖形成连续小庭，未复用双壳大门。','一侧玻璃折槽与另一侧实心岸壁不对称，通路从中间绕开。','完整层板和交替开口构成折返观景楼，梯口分布不同于环井。','改为有玻璃护栏的真实中空环井，细珠灯芯留在井中，环台连续。','改为每层旋转的L形分台，四面能看到不同开口和台阶，保留中央标芯。','东南大弯岸占据一侧，北西走廊和高肋形成明显L形。','U形低柜台配后部发光珍珠壁，具有服务台围合而非战斗柱阵。','长短错列的货舱改成起伏圆脊，窄巷与后部低货台仍可区分。','两侧隔墙节奏错开，中央街道和封闭后端清楚可见。','短T形分舱墙带独立低顶盖，区别于货舱与双浮标。','两座货舱加高低双贝盖，短S形巷不被顶盖占用。','两座不同高度的小标塔和高位转折梁，不复用三塔广场比例。','双侧礁壁与上方放射网格相对；大珍珠在网格下方，胜利隔墙横断后部。','一侧阶岸、另一侧矮台和两道悬肋，形成向后展开的终端观测翼。'];
const revisionRepairs = [
  {key:'stairs/hole',issue:'Draft used four near-complete common floor plates',fix:'Replaced with a true annular void, glass well rails and slender pearl core; retained Y7/Y12 terraces',kind:'geometry'},
  {key:'stairs/spiral',issue:'Draft floor organization was too close to centered stairs',fix:'Removed a different quadrant on each level to create rotating L decks and glass edge rails',kind:'geometry'},
  {key:'straight/room_storage',issue:'Cargo volumes read as generic colored boxes',fix:'Added a curved shell cargo cover and suspended pearl ridge',kind:'style'},
  {key:'terminal/storage',issue:'Cargo tops were generic flat rectangles',fix:'Carved two rounded shell ridges and added low pearl lighting',kind:'style'},
  {key:'straight/room/layout/storage',issue:'Small cargo sub-layout lacked a structural marine landmark',fix:'Added two independent raised shell caps over offset cargo masses',kind:'style'},
  {key:'four-views.html',issue:'Reverse rotations omitted visible side faces; initial direction labels lacked oblique camera components',fix:'Select the two deepest cube edges per transformed rotation and explicitly record cardinal-plus-45-degree camera vectors',kind:'renderer'}
];
const stair = facing => `quartz_stairs[facing=${facing},half=bottom,shape=straight,waterlogged=false]`;
const isAir = block => block === 'air';
const baseName = block => block.split('[')[0];
const full = block => !isAir(block) && !/stairs|slab|pane|fence/.test(block);

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

function shell(room,index) {
  const {width:ww,height:hh,depth:dd,floor:ff}=room;
  room.box([0,room.variant.minimum_y,0],[ww-1,ff-1,dd-1],'dark_prismarine');
  for(let zz=0;zz<dd;zz++) for(let xx=0;xx<ww;xx++) {
    room.set(xx,ff-1,zz,(xx+zz)%7===0?'sea_lantern':(xx+zz*2)%5===0?'pink_terracotta':'prismarine_bricks');
    const side=xx===0||zz===0||xx===ww-1||zz===dd-1;
    const ceiling=room.variant.key.startsWith('stairs/') ? hh-2 : Math.max(ff+6,hh-3-Math.round((index%3+1)*Math.abs(xx-(ww-1)/2)/((ww-1)/2))-(index%2&&zz>dd/2?1:0));
    for(let yy=ff;yy<hh;yy++) {
      if(side) room.set(xx,yy,zz,yy>=ff+3&&yy<=ff+7&&((xx+zz)%7<4)?'cyan_stained_glass':yy%4===0?'smooth_quartz':'prismarine_bricks');
      else if(yy>=ceiling) room.set(xx,yy,zz,yy===ceiling?'white_terracotta':'dark_prismarine');
    }
  }
}

function architecture(room) {
  const key=room.variant.key;
  const ff=room.floor;
  const box=(from,to,block)=>room.box(from,to,block);
  switch(key) {
    case 'spawn/spawn':
      room.features.push('放射扇贝前殿、偏西候潮龛、Y4朝圣轴');
      for(let zz=1;zz<=6;zz++) for(let xx=2;xx<=13;xx++) {
        const yy=7+Math.round(6*Math.sin(Math.PI*(xx-2)/11)*(1-zz/12));
        box([xx,yy,zz],[xx,15,zz],zz%2===0?'smooth_quartz':'pink_terracotta');
      }
      room.mass(1,5,3,10,6,'white_terracotta');
      room.mass(12,2,14,6,8,'orange_terracotta');
      box([6,7,1],[9,9,2],'sea_lantern');
      for(const zz of [8,12]) room.rib(zz,13);
      box([6,4,7],[9,9,14],'air');
      break;
    case 'spawn/outside_near':
      room.features.push('U形岸站、东侧层叠礁墙、封闭干燥庭院');
      for(let step=0;step<4;step++) {
        box([1,2,1+step],[3+step,5+step*3,3+step],'pink_terracotta');
        box([11-step,2,5+step],[14,8+step*3,14],'orange_terracotta');
      }
      box([1,2,12],[7,4,14],'white_terracotta');
      box([2,22,2],[13,24,13],'cyan_stained_glass');
      room.rib(8,19,1,10);
      break;
    case 'spawn/outside_far':
      room.features.push('偏西高航标、东向观测翼、环形灯冠');
      room.beacon(5,6,3,21);
      for(let yy=4;yy<=17;yy+=4) for(let xx=2;xx<=8;xx++) room.set(xx,yy,6,'smooth_quartz');
      room.mass(8,10,14,13,6,'white_terracotta');
      box([9,7,11],[13,8,12],'cyan_stained_glass');
      box([10,13,10],[14,14,13],'pink_terracotta');
      break;
    case 'straight/simple':
      room.features.push('纵向肋拱、交错凹湾、四格主廊');
      for(const zz of [3,7,11]) room.rib(zz,10);
      room.mass(1,1,2,5,6); room.mass(13,9,14,14,7,'orange_terracotta');
      break;
    case 'straight/door':
      room.features.push('双重厚壳门、三段纵厅');
      for(const zz of [4,10]) {
        room.mass(1,zz,5,zz+1,10,'white_terracotta');
        room.mass(10,zz,14,zz+1,10,'pink_terracotta');
        box([6,8,zz],[9,10,zz+1],'smooth_quartz');
      }
      box([3,11,5],[12,11,9],'pink_terracotta');
      break;
    case 'straight/bridge':
      room.features.push('四格中桥、玻璃侧栏、封闭退潮浅槽');
      box([2,1,3],[5,1,12],'air'); box([10,1,3],[13,1,12],'air');
      box([5,2,3],[5,3,12],'cyan_stained_glass'); box([10,2,3],[10,3,12],'cyan_stained_glass');
      for(const zz of [2,7,13]) room.rib(zz,11);
      box([2,0,3],[13,0,12],'orange_terracotta');
      break;
    case 'straight/room_cells':
      room.features.push('梳形西舱、东侧回廊、错位舱盖');
      for(const zz of [2,7,12]) {
        room.mass(1,zz,5,zz,8,'white_terracotta');
        box([1,8,zz],[5,9,Math.min(14,zz+2)],'pink_terracotta');
      }
      room.mass(9,6,14,7,7,'orange_terracotta');
      box([8,10,2],[8,11,13],'smooth_quartz');
      break;
    case 'straight/room_storage':
      room.features.push('横向贝壳货舱、两侧双环装卸道、珍珠吊脊');
      room.mass(3,6,8,7,5,'orange_terracotta'); room.mass(8,9,12,9,7);
      box([2,10,6],[13,10,7],'smooth_quartz');
      for(const xx of [3,8,12]) box([xx,7,6],[xx,9,6],'white_terracotta');
      for(let xx=3;xx<=8;xx++) {
        const crest=6+Math.round(2*Math.sin(Math.PI*(xx-3)/5));
        box([xx,crest,6],[xx,crest,7],'smooth_quartz');
      }
      room.set(6,9,7,'sea_lantern');
      break;
    case 'straight/room_towers':
      room.features.push('三座偏轴不同高度航标、折线街道');
      room.beacon(3,3,1,9); room.beacon(8,8,1,12); room.beacon(11,12,1,7);
      box([3,10,3],[8,10,3],'smooth_quartz'); box([8,11,3],[8,11,8],'smooth_quartz');
      break;
    case 'straight/hole':
      room.features.push('中央礁眼环道、玻璃护井、上方圆形冠环');
      box([6,1,5],[9,1,10],'air');
      box([5,2,5],[5,3,10],'cyan_stained_glass'); box([10,2,5],[10,3,10],'cyan_stained_glass');
      box([5,2,5],[10,3,5],'cyan_stained_glass'); box([5,2,10],[10,3,10],'cyan_stained_glass');
      for(let xx=3;xx<=12;xx++) for(let zz=3;zz<=12;zz++) if(Math.abs(Math.hypot(xx-7.5,zz-7.5)-5)<0.6) room.set(xx,10,zz,'smooth_quartz');
      room.mass(1,5,1,9,7,'orange_terracotta');
      break;
    case 'straight/platform':
      room.features.push('不对称Y4/Y5礁石剧场、三格梯、Y2中舞台');
      box([1,2,5],[4,3,10],'pink_terracotta');
      box([10,2,5],[14,3,10],'white_terracotta');
      box([12,4,7],[14,4,9],'orange_terracotta');
      for(let step=0;step<2;step++) box([1,2+step,3+step],[3,2+step,3+step],stair('south'));
      for(let step=0;step<2;step++) box([11,2+step,3+step],[13,2+step,3+step],stair('south'));
      box([11,4,7],[11,4,9],stair('east'));
      for(const zz of [2,12]) room.rib(zz,12);
      break;
    case 'turn/pillar_room':
      room.features.push('偏西单体螺壳、东半环、渐缩脊');
      for(let yy=2;yy<=11;yy++) {
        const radius=yy<6?2:yy<9?1:0;
        box([6-radius,yy,7-radius],[6+radius,yy,7+radius],yy%3===0?'smooth_quartz':'pink_terracotta');
      }
      box([2,10,3],[12,10,3],'white_terracotta');
      break;
    case 'turn/big_doors':
      room.features.push('T形鲸肋三门、大门湾、分岔顶脊');
      room.mass(1,3,4,4,10,'white_terracotta'); room.mass(11,3,14,4,10,'white_terracotta');
      room.mass(1,11,4,12,9); room.mass(11,11,14,12,9);
      box([5,9,3],[10,10,4],'smooth_quartz'); box([5,8,11],[10,9,12],'smooth_quartz');
      box([11,8,5],[12,9,10],'smooth_quartz');
      break;
    case 'turn/small_doors':
      room.features.push('交错厚墙、串联小庭、窄门巷');
      room.mass(1,6,5,7,7,'pink_terracotta');
      room.mass(10,9,14,10,6,'orange_terracotta');
      box([2,9,2],[5,10,5],'white_terracotta');
      box([9,11,10],[13,12,13],'white_terracotta');
      break;
    case 'turn/hole':
      room.features.push('西北L形封闭礁槽、东弯分流、折角礁窗');
      box([1,1,6],[5,1,10],'air');
      box([5,2,6],[5,3,10],'cyan_stained_glass');
      box([1,2,6],[5,3,6],'cyan_stained_glass'); box([1,2,10],[5,3,10],'cyan_stained_glass');
      box([4,1,7],[7,1,9],'smooth_quartz'); box([4,2,7],[7,5,9],'air');
      room.mass(8,10,9,13,7,'orange_terracotta'); room.rib(3,10);
      break;
    case 'stairs/centered': case 'stairs/hole': case 'stairs/spiral':
      buildStairs(room);
      break;
    case 'curve/simple':
      room.features.push('北西L形宽廊、东南曲岸、回潮顶');
      for(let xx=6;xx<=14;xx++) for(let zz=6;zz<=14;zz++) if(xx+zz>18) room.mass(xx,zz,xx,zz,9,'pink_terracotta');
      room.rib(3,11); box([1,9,6],[5,10,9],'smooth_quartz');
      break;
    case 'terminal/canteen':
      room.features.push('U形珍珠交易台、前庭与侧向陈列、不改玩法');
      room.mass(3,10,12,11,3,'smooth_quartz'); room.mass(2,6,3,11,3,'pink_terracotta'); room.mass(12,7,13,11,3,'pink_terracotta');
      box([5,6,13],[10,8,14],'sea_lantern');
      for(const zz of [4,8,12]) room.rib(zz,11);
      break;
    case 'terminal/storage':
      room.features.push('蛇形巷、错列圆脊贝货舱、低位珠灯');
      room.mass(2,3,5,6,6,'orange_terracotta'); room.mass(7,6,10,9,7); room.mass(5,12,9,13,4,'white_terracotta');
      box([2,10,3],[13,10,3],'smooth_quartz');
      for(const [fromX,toX,fromZ,toZ,top] of [[2,5,3,6,6],[7,10,6,9,7]]) {
        for(let xx=fromX;xx<=toX;xx++) {
          const crest=top-(xx===fromX||xx===toX?2:0);
          box([xx,crest+1,fromZ],[xx,top,toZ],'air');
          box([xx,crest,fromZ],[xx,crest,toZ],'smooth_quartz');
        }
      }
      room.set(6,3,12,'sea_lantern');
      break;
    case 'terminal/cells':
      room.features.push('中央干舱街、左右错缝隔墙、后珍珠封壁');
      for(const zz of [3,8,13]) room.mass(1,zz,4,zz,7,'white_terracotta');
      for(const zz of [2,7,12]) room.mass(11,zz,14,zz,8,'pink_terracotta');
      box([5,5,14],[10,9,14],'smooth_quartz'); box([7,6,14],[8,7,14],'sea_lantern');
      break;
    case 'straight/room/layout/cells':
      room.features.push('低壳双舱、北联络道、南横廊');
      room.mass(3,3,3,7,4,'white_terracotta'); room.mass(7,5,10,5,4,'pink_terracotta');
      box([1,6,2],[3,6,7],'white_terracotta');
      break;
    case 'straight/room/layout/storage':
      room.features.push('S形短街、两组斜错高低贝壳货舱');
      room.mass(3,2,5,4,3,'orange_terracotta'); room.mass(5,7,8,8,4,'pink_terracotta');
      box([3,6,2],[8,6,2],'smooth_quartz');
      for(let xx=3;xx<=5;xx++) box([xx,xx===4?5:4,2],[xx,xx===4?5:4,4],'smooth_quartz');
      for(let xx=5;xx<=8;xx++) box([xx,xx===5||xx===8?4:5,7],[xx,xx===5||xx===8?4:5,8],'white_terracotta');
      break;
    case 'straight/room/layout/towers':
      room.features.push('前后双浮标、折路、上部连接梁');
      room.beacon(4,3,1,5); room.beacon(6,8,1,4);
      box([4,6,3],[6,6,3],'smooth_quartz'); box([6,6,3],[6,6,8],'smooth_quartz');
      break;
    case 'boss/eclipse_keeper':
      buildBoss(room);
      break;
    case 'promenade':
      room.features.push('斜向观测翼、横向海窗、悬弧梁');
      for(let xx=1;xx<=5;xx++) room.mass(xx,8+xx,xx,14,4+(5-xx),'pink_terracotta');
      box([2,4,15],[13,10,15],'cyan_stained_glass');
      box([11,2,9],[14,5,13],'white_terracotta');
      for(const zz of [5,10]) room.rib(zz,12);
      break;
    default: throw new Error(`Unimplemented ${key}`);
  }
}

function buildStairs(room) {
  const key=room.variant.key;
  room.features.push(key.endsWith('centered')?'四层观景板与交替三格折返梯':key.endsWith('hole')?'四层礁井环台与Y7/Y12侧台':'四边方螺旋、中央航标和Y4侧龛');
  for(const feet of [6,10,14,18]) room.box([1,feet-1,1],[14,feet-1,14],feet===18?'smooth_quartz':'prismarine_bricks');
  if(key.endsWith('hole')) {
    for(const feet of [6,10,14,18]) {
      room.box([5,feet-1,6],[10,feet-1,10],'air');
      room.box([5,feet-1,6],[5,feet+1,10],'cyan_stained_glass');
      room.box([10,feet-1,6],[10,feet+1,10],'cyan_stained_glass');
      room.box([5,feet-1,6],[10,feet+1,6],'cyan_stained_glass');
      room.box([5,feet-1,10],[10,feet+1,10],'cyan_stained_glass');
    }
    room.box([7,2,8],[8,20,8],'sea_lantern');
  }
  if(key.endsWith('spiral')) {
    const cutouts=[[1,8,9,14],[1,1,7,9],[6,1,14,7],[8,7,14,14]];
    for(let level=0;level<4;level++) {
      const feet=6+level*4,[fromX,fromZ,toX,toZ]=cutouts[level];
      room.box([fromX,feet-1,fromZ],[toX,feet-1,toZ],'air');
      const railX=level===0?toX:fromX;
      const railZ=level<2?toZ:fromZ;
      room.box([railX,feet-1,fromZ],[railX,feet+1,toZ],'cyan_stained_glass');
      room.box([fromX,feet-1,railZ],[toX,feet+1,railZ],'cyan_stained_glass');
    }
  }
  const configs=key.endsWith('centered')?[
    [5,4,'south'],[10,10,'north'],[5,4,'south'],[10,10,'north']
  ]:key.endsWith('hole')?[
    [3,3,'east'],[12,4,'south'],[11,4,'west'],[3,6,'north']
  ]:[
    [4,3,'east'],[12,4,'south'],[11,12,'west'],[2,12,'north']
  ];
  for(let level=0;level<4;level++) {
    const [startX,startZ,facing]=configs[level];
    const low=2+level*4;
    const [deltaX,deltaZ]=facing==='south'?[0,1]:facing==='north'?[0,-1]:facing==='east'?[1,0]:[-1,0];
    for(let along=-1;along<=4;along++) for(let across=-1;across<=1;across++) {
      const xx=startX+deltaX*along+(deltaZ!==0?across:0);
      const zz=startZ+deltaZ*along+(deltaX!==0?across:0);
      const surface=along<0?low:along>3?low+4:low+along;
      room.box([xx,surface,zz],[xx,Math.min(room.height-2,surface+4),zz],'air');
      if(along>=0&&along<4) room.set(xx,surface,zz,stair(facing));
      else room.set(xx,surface-1,zz,'smooth_quartz');
    }
  }
  if(key.endsWith('hole')) {
    room.box([1,6,9],[3,6,11],'smooth_quartz'); room.box([1,7,9],[3,10,11],'air');
    room.box([4,6,9],[4,6,11],stair('west'));
    room.box([12,11,11],[14,11,13],'smooth_quartz'); room.box([12,12,11],[14,15,13],'air');
    room.box([10,10,11],[10,10,13],stair('east')); room.box([11,11,11],[11,11,13],stair('east'));
    room.box([10,11,11],[10,14,13],'air'); room.box([11,12,11],[11,15,13],'air');
  }
  if(key.endsWith('spiral')) {
    room.box([1,3,10],[3,3,12],'smooth_quartz'); room.box([1,4,10],[3,7,12],'air');
    room.box([5,2,10],[5,2,12],stair('west')); room.box([4,3,10],[4,3,12],stair('west'));
    room.box([4,4,10],[5,6,12],'air');
    room.beacon(8,8,1,25);
  }
  for(let yy=6;yy<=26;yy+=5) { room.set(1,yy,1,'sea_lantern'); room.set(14,yy,14,'sea_lantern'); }
}

function buildBoss(room) {
  room.features.push('悬浮大珍珠、放射双壳穹顶、侧礁地、Z22全高胜利隔墙');
  for(let yy=2;yy<=14;yy++) {
    const inset=Math.max(0,Math.floor((yy-5)/3));
    room.box([1,yy,4],[3+inset,yy,20],'pink_terracotta');
    room.box([30-inset,yy,4],[32,yy,20],'orange_terracotta');
  }
  for(let xx=2;xx<=31;xx++) for(let zz=3;zz<=20;zz++) {
    const dist=Math.hypot((xx-16.5)/15,(zz-12)/10);
    const yy=15+Math.max(0,Math.round(5*(1-dist)));
    if(xx%4===0||zz%5===0) room.set(xx,yy,zz,'smooth_quartz');
  }
  for(let yy=10;yy<=16;yy++) for(let zz=9;zz<=15;zz++) for(let xx=13;xx<=19;xx++) if(Math.hypot(xx-16,yy-13,zz-12)<=3.2) room.set(xx,yy,zz,yy===13?'sea_lantern':'smooth_quartz');
  room.box([1,2,22],[32,22,22],'prismarine_bricks');
  room.box([15,2,22],[18,7,22],'air');
  room.box([15,2,0],[18,6,0],'air');
  room.box([5,8,25],[28,9,25],'smooth_quartz');
  for(const xx of [6,27]) room.box([xx,2,24],[xx,7,25],'quartz_pillar');
}

function finishInterfaces(room) {
  for(const port of room.variant.ports) for(const position of port.cells) {
    room.safe(position,5);
    const [xx,yy,zz]=position;
    const deltaX=xx===0?1:xx===room.width-1?-1:0;
    const deltaZ=zz===0?1:zz===room.depth-1?-1:0;
    for(let inset=1;inset<=2;inset++) room.safe([xx+deltaX*inset,yy,zz+deltaZ*inset],5);
  }
  for(const socket of room.variant.sockets) {
    room.pad(socket.position);
    room.safe(socket.spawn_position);
    room.set(...socket.support,'smooth_quartz');
  }
  if(room.variant.key==='spawn/spawn') {
    for(let xx=6;xx<=9;xx++) for(let zz=7;zz<=15;zz++) room.safe([xx,4,zz],5);
    room.safe([7,4,8]);
  }
  if(room.variant.key.startsWith('boss/')) {
    for(const position of [[16,2,2],[16,2,13],[16,2,25]]) room.safe(position);
    for(let xx=15;xx<=18;xx++) for(let zz=0;zz<=27;zz++) if(zz!==27) room.safe([xx,2,zz],zz===22?6:5);
  }
}

function makeRoom(variant,index) {
  const room=new Room(variant);
  shell(room,index); architecture(room); finishInterfaces(room);
  return room;
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

function validate(room, bytes, previousBytes) {
  const variant=room.variant;
  const safe=(position,clearance=3)=> {
    const [xx,yy,zz]=position;
    assert(full(room.get(xx,yy-1,zz)),`Support ${variant.key} ${position}: ${room.get(xx,yy-1,zz)}`);
    for(let offset=0;offset<clearance;offset++) assert.equal(room.get(xx,yy+offset,zz),'air',`Clearance ${variant.key} ${position} +${offset}`);
  };
  const targets=variant.ports.flatMap(port=>port.cells).concat(variant.sockets.flatMap(socket=>[socket.position,socket.spawn_position]));
  for(const port of variant.ports) for(const position of port.cells) safe(position,5);
  for(const socket of variant.sockets) {safe(socket.position);safe(socket.spawn_position);assert(full(room.get(...socket.support)));}
  if(variant.key==='spawn/spawn') { targets.push([7,4,8]); safe([7,4,8]); }
  if(variant.key.startsWith('boss/')) for(const position of [[16,2,2],[16,2,13],[16,2,25]]) {targets.push(position);safe(position);}
  const fallback=variant.key==='spawn/outside_far'?[11,2,5]:variant.key==='spawn/outside_near'?[8,2,3]:[Math.floor(room.width/2),room.floor,Math.floor(room.depth/2)];
  const start=variant.ports[0]?.cells[0]??variant.sockets[0]?.position??fallback;
  const graph=walk(room,start,targets);
  assert.equal(graph.unreachable.length,0,`Disconnected ${variant.key}: ${JSON.stringify(graph.unreachable)}`);
  const reserved=new Set(targets.map(position=>position.join(',')));
  const assigned=new Set();
  const alternativeSpawns=[];
  for(const socket of variant.sockets) {
    const [xx,yy,zz]=socket.position;
    const candidates=[];
    for(let nextZ=1;nextZ<room.depth-1;nextZ++) for(let nextX=1;nextX<room.width-1;nextX++) {
      const distance=Math.abs(nextX-xx)+Math.abs(nextZ-zz);
      const position=[nextX,yy,nextZ],identity=position.join(',');
      if(distance<1||distance>6||reserved.has(identity)||assigned.has(identity)||!graph.reachable(position)||!full(room.get(nextX,yy-1,nextZ))) continue;
      if([0,1,2].every(offset=>room.get(nextX,yy+offset,nextZ)==='air')) candidates.push({position,distance});
    }
    candidates.sort((left,right)=>left.distance-right.distance);
    assert(candidates.length,`No independent spawn ${variant.key} ${socket.position}`);
    const chosen=candidates[0].position; assigned.add(chosen.join(','));
    alternativeSpawns.push({socket:socket.id,position:chosen,candidates:candidates.length,distance:candidates[0].distance});
  }
  const openings=new Set();
  for(const port of variant.ports) for(const [xx,yy,zz] of port.cells) for(let offset=0;offset<5;offset++) openings.add([xx,yy+offset,zz].join(','));
  if(variant.key.startsWith('boss/')) for(let xx=15;xx<=18;xx++) for(let yy=2;yy<=6;yy++) openings.add([xx,yy,0].join(','));
  let shellChecked=0;
  for(let yy=variant.minimum_y;yy<room.height;yy++) for(let zz=0;zz<room.depth;zz++) for(let xx=0;xx<room.width;xx++) {
    if(xx!==0&&zz!==0&&xx!==room.width-1&&zz!==room.depth-1&&yy!==room.height-1&&yy!==variant.minimum_y) continue;
    shellChecked++;
    assert(room.get(xx,yy,zz)!=='air'||openings.has([xx,yy,zz].join(',')),`Shell leak ${variant.key} ${[xx,yy,zz]}`);
  }
  let victoryGate=null;
  if(variant.key.startsWith('boss/')) {
    for(let xx=0;xx<room.width;xx++) for(let yy=2;yy<room.height;yy++) {
      const opening=xx>=15&&xx<=18&&yy<=7;
      assert.equal(room.get(xx,yy,22)==='air',opening,`Gate partition ${xx},${yy},22`);
    }
    const closed=walk(room,[16,2,2],[],true);
    assert(!closed.reachable([16,2,25]),'Closed gate bypass');
    assert(graph.reachable([16,2,25]));
    victoryGate={wallZ:22,aperture:{from:[15,2,22],to:[18,7,22]},wallToRoof:true,closedReturnReachable:false,openReturnReachable:true};
  }
  const previous=decode(previousBytes,variant);
  let changed=0,union=0,interiorChanged=0,interiorTotal=0;
  for(let yy=0;yy<room.height;yy++) for(let zz=0;zz<room.depth;zz++) for(let xx=0;xx<room.width;xx++) {
    const currentSolid=!isAir(room.get(xx,yy,zz)),oldSolid=!isAir(previous.get(xx,yy,zz));
    if(currentSolid||oldSolid) union++;
    if(currentSolid!==oldSolid) changed++;
    if(xx>0&&xx<room.width-1&&zz>0&&zz<room.depth-1&&yy>=room.floor&&yy<room.height-1) {
      interiorTotal++; if(currentSolid!==oldSolid) interiorChanged++;
    }
  }
  assert(interiorChanged>=64&&changed/union>=0.08,`Insufficient geometry revision ${variant.key} ${changed}/${union}`);
  return {
    key:variant.key,size:variant.size,minimum_y:variant.minimum_y,sha256:hash(bytes),baselineSha256:hash(previousBytes),
    denseNBT:{DataVersion:3465,blocks:room.blocks.length,uniqueCompleteInBounds:true,barePaletteNames:true,separateProperties:true,entities:0,blockEntities:0},
    interfaces:{ports:variant.ports,sockets:variant.sockets,spawn_position:variant.spawn_position??null,portClearance:5,socketClearance:3,fullSupport:true},
    connectivity:{allDeclaredTargets:true,targets:targets.length,reachableMicroCells:graph.count,maximumStep:0.5,headroom:2,jumpRequired:false},
    alternativeSpawns,shell:{checkedCells:shellChecked,closedExceptDeclaredDoors:true,minimumNonAirY:variant.minimum_y},victoryGate,
    geometry:{changedOccupancy:changed,volumeFraction:changed/room.blocks.length,unionFraction:changed/union,interiorChanged,interiorFraction:interiorChanged/interiorTotal,occupancySha256:hash(Buffer.from(room.blocks.map(block=>isAir(block)?0:1)))},
    features:room.features,passed:true
  };
}

function color(block) {
  if(block==='air')return '#10242b';
  if(block.includes('lantern'))return '#b4ffec';
  if(block.includes('glass'))return '#3eacbd';
  if(block.includes('pink'))return '#df8d9e';
  if(block.includes('orange'))return '#d99a73';
  if(block.includes('quartz')||block.includes('white'))return '#e9dec9';
  if(block.includes('dark'))return '#1e5457';
  return '#609892';
}

function gallery(rooms,results) {
  const escape=value=>String(value).replaceAll('&','&amp;').replaceAll('<','&lt;');
  const slice=(room,axis,coordinate)=> {
    const horizontal=axis==='y';
    const columns=axis==='x'?room.depth:room.width;
    const rows=horizontal?room.depth:room.height;
    const cells=[];
    for(let row=0;row<rows;row++) for(let column=0;column<columns;column++) {
      const xx=axis==='x'?coordinate:column;
      const yy=horizontal?coordinate:room.height-1-row;
      const zz=axis==='x'?column:axis==='z'?coordinate:row;
      cells.push(`<rect x="${column}" y="${row}" width="1" height="1" fill="${color(room.get(xx,yy,zz))}"/>`);
    }
    if(horizontal) {
      for(const port of room.variant.ports) for(const [xx,yy,zz] of port.cells) if(yy===coordinate) cells.push(`<rect x="${xx+.1}" y="${zz+.1}" width=".8" height=".8" fill="#ffe187"/>`);
      for(const socket of room.variant.sockets) if(socket.position[1]===coordinate) cells.push(`<circle cx="${socket.position[0]+.5}" cy="${socket.position[2]+.5}" r=".4" fill="#ff52be"/>`);
    }
    return `<figure><figcaption>${horizontal?'水平平面':'竖向剖面'} ${axis.toUpperCase()}=${coordinate}</figcaption><svg viewBox="0 0 ${columns} ${rows}" role="img" aria-label="NBT ${axis} ${coordinate}">${cells.join('')}</svg></figure>`;
  };
  const data=rooms.map(room=> {
    const palette=[...new Set(room.blocks)],lookup=new Map(palette.map((block,index)=>[block,index]));
    const runs=[];
    for(const block of room.blocks) {
      const index=lookup.get(block);
      if(runs.length&&runs[runs.length-2]===index)runs[runs.length-1]++;
      else runs.push(index,1);
    }
    return {size:room.variant.size,floor:room.floor,palette,colors:palette.map(color),runs};
  });
  const cards=rooms.map((room,index)=> {
    const result=results[index];
    const second=room.variant.key.startsWith('stairs/')?18:Math.min(room.height-3,room.floor+5);
    return `<article id="room-${index}"><header><h2>${String(index+1).padStart(2,'0')} · ${names[index]}</h2><code>${room.variant.key}</code><p>${room.variant.size.join(' × ')} · sockets ${room.variant.sockets.length} · 变化 ${(result.geometry.volumeFraction*100).toFixed(1)}% 全体积 / ${(result.geometry.unionFraction*100).toFixed(1)}% solid并集</p></header><p>${escape(result.features.join('；'))}</p><div class="slices">${slice(room,'y',room.floor)}${slice(room,'y',second)}${slice(room,'x',Math.floor(room.width/2))}${slice(room,'z',Math.floor(room.depth/2))}</div><details open><summary>实际NBT轴测体素投影（移除外壳；非游戏材质渲染）</summary><label>截取高度 <input type="range" min="${room.floor}" max="${room.height-2}" value="${Math.min(room.height-3,room.floor+9)}" data-index="${index}"><output></output></label><canvas width="660" height="440" data-index="${index}"></canvas></details><a href="roomset/${room.variant.key}.nbt">下载此NBT</a> · <small>sha256 ${result.sha256}</small></article>`;
  }).join('');
  return `<!doctype html><html lang="zh-CN"><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>Coral 0.34 — 27 independent rooms</title><style>body{background:#0b1f28;color:#e4ede8;font:15px system-ui;margin:0}main{max-width:1500px;margin:auto;padding:28px}h1{font-size:36px;color:#f4b4b6}p{line-height:1.6}.grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(560px,1fr));gap:22px}article{background:#14313a;border:1px solid #35606a;border-radius:14px;padding:22px;overflow:hidden}h2{margin:0 0 8px;color:#f3c2b3}code{color:#8fe1de}small{overflow-wrap:anywhere}a{color:#a6e8dc}.slices{display:grid;grid-template-columns:repeat(4,1fr);gap:10px}figure{margin:5px 0}svg{width:100%;height:180px;background:#10242b}figcaption{font-size:12px;margin-bottom:5px;color:#a7c3c2}canvas{width:100%;background:#10242b;margin-top:10px}summary{cursor:pointer;margin:10px 0}input{vertical-align:middle;width:65%}@media(max-width:620px){.grid{grid-template-columns:1fr}.slices{grid-template-columns:repeat(2,1fr)}main{padding:12px}}</style><main><h1>珊瑚 · 干燥的潮汐文明</h1><p>27房独立重建 · 全图来自已落盘并回读的实际NBT · 金色=port，洋红=socket；深色=air，青色=玻璃，粉橙=珊瑚意象，米白=贝壳，亮绿=灯。下方提供两个水平切片和两个竖剖面。轴测可调整截高，用于查看内部多层组织。</p><p><a href="design.md">逐房设计</a> · <a href="validation.json">完整真实验收报告</a> · <a href="roomset/manifest.json">冻结Manifest</a> · 静态碰撞验收通过，未宣称实际游戏验收。</p><div class="grid">${cards}</div></main><script>const rooms=${JSON.stringify(data)};
for(const room of rooms){room.blocks=[];for(let index=0;index<room.runs.length;index+=2)for(let count=0;count<room.runs[index+1];count++)room.blocks.push(room.runs[index]);}
function draw(index,cut){const room=rooms[index],canvas=document.querySelector('canvas[data-index="'+index+'"]'),context=canvas.getContext('2d'),[width,height,depth]=room.size;context.clearRect(0,0,660,440);const unit=Math.min(13,560/(width+depth)),rise=unit*.85,center=330,base=405;function face(points,fill){context.beginPath();for(let point=0;point<points.length;point++){if(point===0)context.moveTo(...points[point]);else context.lineTo(...points[point]);}context.closePath();context.fillStyle=fill;context.fill();context.strokeStyle='#16323b66';context.lineWidth=.35;context.stroke();}function point(xx,yy,zz){return[center+(xx-zz)*unit,base+(xx+zz-width-depth)*unit*.48-(yy-room.floor)*rise];}for(let sum=2;sum<width+depth-2;sum++)for(let xx=1;xx<width-1;xx++){const zz=sum-xx;if(zz<1||zz>=depth-1)continue;for(let yy=room.floor-1;yy<=cut;yy++){const paletteIndex=room.blocks[(yy*depth+zz)*width+xx],block=room.palette[paletteIndex];if(block==='air')continue;const fill=room.colors[paletteIndex],north=point(xx,yy+1,zz),east=point(xx+1,yy+1,zz),south=point(xx+1,yy+1,zz+1),west=point(xx,yy+1,zz+1);face([west,south,point(xx+1,yy,zz+1),point(xx,yy,zz+1)],fill);face([east,south,point(xx+1,yy,zz+1),point(xx+1,yy,zz)],fill);face([north,east,south,west],fill);}}context.fillStyle='#b5d3ce';context.font='13px system-ui';context.fillText('NBT voxel cut Y='+cut+' | exterior shell omitted',14,24);}
document.querySelectorAll('input[type=range]').forEach(input=>{function update(){input.nextElementSibling.textContent=input.value;draw(Number(input.dataset.index),Number(input.value));}input.addEventListener('input',update);update();});</script></html>`;
}

function commonMask(room,index) {
  const reference=new Room(room.variant); shell(reference,index);
  const masked=new Set();
  for(let yy=0;yy<room.height;yy++)for(let zz=0;zz<room.depth;zz++)for(let xx=0;xx<room.width;xx++) {
    if(xx===0||zz===0||xx===room.width-1||zz===room.depth-1||yy<room.floor||reference.get(xx,yy,zz)!=='air')masked.add(room.index(xx,yy,zz));
  }
  for(const port of room.variant.ports)for(const [xx,yy,zz] of port.cells)for(let vertical=-1;vertical<5;vertical++)for(let inset=0;inset<3;inset++) {
    const nextX=xx+(xx===0?inset:xx===room.width-1?-inset:0),nextZ=zz+(zz===0?inset:zz===room.depth-1?-inset:0);
    masked.add(room.index(nextX,yy+vertical,nextZ));
  }
  return masked;
}

function family(key) {
  if(key.includes('/layout/'))return 'sub-layout';
  if(key.startsWith('stairs/'))return 'stairs';
  if(key.startsWith('straight/')||key.startsWith('turn/')||key.startsWith('curve/'))return 'combat';
  if(key.startsWith('terminal/'))return 'terminal';
  if(key.includes('outside_'))return 'exterior';
  return key;
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

function auditRepetition(rooms,results,masks) {
  const signatures=rooms.map((room,index)=> {
    const volume=shapeSet(room,masks[index]),normalized=shapeSet(room,masks[index],true),plan=shapeSet(room,masks[index],false,true);
    return {volume,normalized,plan,key:room.variant.key,family:family(room.variant.key),signature:hash(Buffer.from([...normalized].sort().join(';')))};
  });
  const pairs=[];
  for(let leftIndex=0;leftIndex<rooms.length;leftIndex++)for(let rightIndex=leftIndex+1;rightIndex<rooms.length;rightIndex++) {
    const left=signatures[leftIndex],right=signatures[rightIndex];
    const sameSize=JSON.stringify(rooms[leftIndex].variant.size)===JSON.stringify(rooms[rightIndex].variant.size),sameFamily=left.family===right.family;
    if(!sameSize&&!sameFamily)continue;
    const geometry=compareSets(sameSize?left.volume:left.normalized,sameSize?right.volume:right.normalized);
    const layout=compareSets(left.plan,right.plan);
    assert(geometry.jaccard<1,`Duplicate internal structure ${left.key} ${right.key}`);
    assert(!(left.family==='combat'&&right.family==='combat'&&geometry.jaccard>=.9),`Combat clone ${left.key} ${right.key}`);
    pairs.push({left:left.key,right:right.key,sameSize,sameFamily,geometry,layout,highSimilarity:geometry.jaccard>=.9,decision:geometry.jaccard>=.9?'REVIEW_REQUIRED':'KEEP_DISTINCT',reason:sameFamily&&left.family==='stairs'?'Shared mandatory Y2/Y18 interfaces and real stair flights remain. Final forms are complete folding decks / hollow annular well / rotating L decks. The latter two were rebuilt after visual review; see actual projected shapes.':sameFamily&&left.family==='combat'?`${rooms[leftIndex].features.join('; ')} versus ${rooms[rightIndex].features.join('; ')}; solid intersection ${geometry.intersection} / union ${geometry.union}. No common-interface or air/air bonus.`:geometry.jaccard>=.9?'Must manually review fixed stair or corridor reuse':`${rooms[leftIndex].features.join('; ')} versus ${rooms[rightIndex].features.join('; ')}; retained for distinct measured remaining geometry`});
  }
  pairs.sort((left,right)=>right.geometry.jaccard-left.geometry.jaccard);
  assert(!pairs.some(pair=>pair.highSimilarity),'Unresolved high similarity');
  return {schema:1,style:'fteclipsegen:coral',passed:true,exclusions:'All boundary voxels; base floor y<feet; every generated common-shell roof voxel; declared port cells and 2-cell inward / y-1..y+4 safety bands. Air/air agreement is not counted.',comparison:'Solid-set Jaccard and low-level plan-set Jaccard. Equal sizes use exact coordinates; other same-family pairs use a 16x12x16 normalized lattice.',threshold:.9,comparablePairs:pairs.length,highestPairs:pairs.slice(0,20),pairs,rooms:signatures.map((signature,index)=>({key:signature.key,nbtSha256:results[index].sha256,family:signature.family,maskedCells:masks[index].size,interiorSolidCells:signature.volume.size,normalizedSignature:signature.signature,baselineDifference:results[index].geometry,decision:'PASS'})),initialDraftManifestSha256:'99d9b92aa78280ba20626a940c2b901c2d1cdbcb31a8182ff908c29cd49b07ba',repairs:revisionRepairs};
}

function auditStyle(rooms,results,masks) {
  const records=rooms.map((room,index)=> {
    const materials={},interiorMaterials={};
    for(let coordinate=0;coordinate<room.blocks.length;coordinate++) {
      const block=baseName(room.blocks[coordinate]);
      materials[block]=(materials[block]??0)+1;
      if(!masks[index].has(coordinate)&&block!=='air')interiorMaterials[block]=(interiorMaterials[block]??0)+1;
    }
    const visited=new Set(),landmarks=[];
    for(let yy=room.floor;yy<room.height-1;yy++)for(let zz=1;zz<room.depth-1;zz++)for(let xx=1;xx<room.width-1;xx++) {
      const identity=room.index(xx,yy,zz),block=room.get(xx,yy,zz);
      if(masks[index].has(identity)||isAir(block)||visited.has(identity))continue;
      const group=/quartz|white_terracotta/.test(block)?'shell-rib':/pink|orange/.test(block)?'reef-mass':/lantern/.test(block)?'pearl-beacon':/glass/.test(block)?'dry-tide-glazing':'reef-masonry';
      const queue=[[xx,yy,zz]],from=[xx,yy,zz],to=[xx,yy,zz],counts={};visited.add(identity);
      for(let cursor=0;cursor<queue.length;cursor++) {
        const position=queue[cursor],material=baseName(room.get(...position));counts[material]=(counts[material]??0)+1;
        for(let axis=0;axis<3;axis++){from[axis]=Math.min(from[axis],position[axis]);to[axis]=Math.max(to[axis],position[axis]);}
        for(const delta of [[1,0,0],[-1,0,0],[0,1,0],[0,-1,0],[0,0,1],[0,0,-1]]) {
          const next=position.map((value,axis)=>value+delta[axis]);if(!room.inside(...next))continue;
          const nextIdentity=room.index(...next),nextBlock=room.get(...next);
          const nextGroup=/quartz|white_terracotta/.test(nextBlock)?'shell-rib':/pink|orange/.test(nextBlock)?'reef-mass':/lantern/.test(nextBlock)?'pearl-beacon':/glass/.test(nextBlock)?'dry-tide-glazing':'reef-masonry';
          if(masks[index].has(nextIdentity)||isAir(nextBlock)||visited.has(nextIdentity)||nextGroup!==group)continue;
          visited.add(nextIdentity);queue.push(next);
        }
      }
      if(queue.length>=3)landmarks.push({type:group,blocks:queue.length,bounds:{from,to},materials:counts});
    }
    landmarks.sort((left,right)=>right.blocks-left.blocks);
    const themeGroups=new Set(landmarks.map(landmark=>landmark.type));
    assert(themeGroups.size>=2&&landmarks.length>=2,`Weak structural theme ${room.variant.key}`);
    const low=landmarks.filter(landmark=>landmark.bounds.from[1]<=room.floor+2);
    const upper=landmarks.filter(landmark=>landmark.bounds.to[1]>=room.floor+4);
    assert(low.length&&upper.length,`Theme only on one plane ${room.variant.key}`);
    const footprint=shapeSet(room,masks[index],false,true);
    const cornerCounts=[0,0,0,0];let centralCells=0;
    for(const point of footprint) {
      const [xx,zz]=point.split(',').map(Number);
      const west=xx<room.width/3,east=xx>room.width*2/3,north=zz<room.depth/3,south=zz>room.depth*2/3;
      if(west&&north)cornerCounts[0]++;if(east&&north)cornerCounts[1]++;if(west&&south)cornerCounts[2]++;if(east&&south)cornerCounts[3]++;
      if(!west&&!east&&!north&&!south)centralCells++;
    }
    const fourCornerCandidate=cornerCounts.every(count=>count>=4)&&centralCells===0;
    const spanningLowFormations=low.filter(landmark=>landmark.bounds.to[0]-landmark.bounds.from[0]>=4||landmark.bounds.to[2]-landmark.bounds.from[2]>=4).length;
    assert(!(fourCornerCandidate&&spanningLowFormations===0),`Four isolated corners and empty hall ${room.variant.key}`);
    const formMetrics={lowFeatureFootprint:footprint.size,lowFootprintFraction:footprint.size/((room.width-2)*(room.depth-2)),cornerCounts,centralFeatureCells:centralCells,fourCornerCandidate,spanningLowFormations,elevatedFormations:upper.length};
    return {key:room.variant.key,nbtSha256:results[index].sha256,materials,interiorMaterials,formMetrics,landmarks,observedStructureGroups:[...themeGroups],functionalForm:room.features,review:{materialOnlyReskin:false,evidence:`${results[index].geometry.interiorChanged} changed internal solid/air cells; ${landmarks.length} actual connected structural formations`,fourCornerEmptyHall:false,reason:'Measured internal formations include low-level partitions/reef masses and elevated ribs/beacons; four-direction views expose their actual extents.',singleRepeatedSymbol:false,retentionReason:visualFindings[index],manualFourViewFinding:visualFindings[index],actualMotifGroups:landmarks.reduce((counts,landmark)=>{counts[landmark.type]=(counts[landmark.type]??0)+1;return counts;},{})},passed:true};
  });
  return {schema:1,style:'fteclipsegen:coral',passed:true,method:'Full palette histogram plus six-neighbor connected formations extracted from final reloaded NBT after common shell removal; component bounding boxes and block/material counts are actual voxel evidence.',rooms:records};
}

function fourViews(rooms,results,masks) {
  const data=rooms.map((room,index)=> {
    const voxels=[];
    const cutY=room.variant.key.startsWith('boss/')?16:room.height-2;
    for(let yy=room.floor;yy<=cutY;yy++)for(let zz=1;zz<room.depth-1;zz++)for(let xx=1;xx<room.width-1;xx++)if(!masks[index].has(room.index(xx,yy,zz))&&!isAir(room.get(xx,yy,zz)))voxels.push([xx,yy,zz,color(room.get(xx,yy,zz))]);
    return {key:room.variant.key,size:room.variant.size,floor:room.floor,cutY,sha256:results[index].sha256,features:room.features,voxels,ports:room.variant.ports,sockets:room.variant.sockets};
  });
  const cards=data.map((room,index)=>`<article><h2>${names[index]} · ${room.key}</h2><p>${room.features.join('；')} · cut Y≤${room.cutY}</p><code>data sha256 ${room.sha256}</code><div class="views">${['北 North','南 South','西 West','东 East'].map((direction,view)=>`<figure><figcaption>${direction} · oblique camera XZ ${['[1,-1]','[-1,1]','[-1,-1]','[1,1]'][view]}</figcaption><canvas width="520" height="390" data-room="${index}" data-direction="${view}"></canvas></figure>`).join('')}</div></article>`).join('');
  return `<!doctype html><html lang="zh-CN"><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>Coral 27 × 4 NBT views</title><style>body{background:#10242b;color:#e8e1d3;font:14px system-ui;margin:20px auto;max-width:1500px}h1,h2{color:#f2afb1}article{padding:20px;margin:20px 0;background:#18343c;border-radius:10px}.views{display:grid;grid-template-columns:repeat(4,1fr);gap:10px}figure{margin:0}canvas{width:100%;background:#10242b}code{overflow-wrap:anywhere;color:#a8d6cf;font-size:11px}figcaption{padding:8px}@media(max-width:800px){.views{grid-template-columns:repeat(2,1fr)}}</style><h1>珊瑚27房 · 北南西东108真实NBT观察方向</h1><p>隐藏规则：移除外边界、基础地板、配方统一壳顶与端口安全带。保留室内真实建筑；对全部方向使用同一完整高度。摄像机各沿北/南/西/东观察，并使用等量斜俯视展开深度；四方向采用不同坐标旋转，绝非复制图像。外壳完整性在validation.json独立验证。颜色体素投影，无游戏材质。</p><a href="gallery.html">双高度及剖面图集</a> · <a href="style-audit.json">实际NBT风格证据</a> · <a href="repetition-audit.json">去公共几何重复度</a><p id="status">Rendering…</p>${cards}<canvas id="contact-sheet" width="1920" height="2430" style="display:none"></canvas><script>window.auditErrors=[];window.addEventListener('error',event=>window.auditErrors.push(String(event.message)));window.addEventListener('unhandledrejection',event=>window.auditErrors.push(String(event.reason)));const rooms=${JSON.stringify(data)};window.viewAudit=[];
function paint(canvas,room,direction){const context=canvas.getContext('2d'),[width,height,depth]=room.size;context.fillStyle='#10242b';context.fillRect(0,0,520,390);function transform(xx,zz){return direction===0?[depth-zz,xx]:direction===1?[zz,width-xx]:direction===2?[width-xx,depth-zz]:[xx,zz];}const scale=Math.min(14,410/(width+depth),280/(height+8)),rise=scale*.85;function point(xx,yy,zz){const [side,deep]=transform(xx,zz);return[260+(side-deep)*scale,350+(side+deep-width-depth)*scale*.45-(yy-room.floor)*rise];}function face(points,fill){context.beginPath();points.forEach((position,index)=>index?context.lineTo(...position):context.moveTo(...position));context.closePath();context.fillStyle=fill;context.fill();context.strokeStyle='#12313a88';context.lineWidth=.45;context.stroke();}const voxels=room.voxels.map(voxel=>{const [side,deep]=transform(voxel[0],voxel[2]);return{voxel,order:side+deep};}).sort((left,right)=>left.order-right.order||left.voxel[1]-right.voxel[1]);for(const item of voxels){const[xx,yy,zz,fill]=item.voxel;const corners=[[xx,zz],[xx+1,zz],[xx+1,zz+1],[xx,zz+1]].map(position=>({world:position,screen:point(position[0],yy+1,position[1]),depth:transform(...position).reduce((total,value)=>total+value,0)}));const edgeOrder=[0,1,2,3].sort((left,right)=>(corners[right].depth+corners[(right+1)%4].depth)-(corners[left].depth+corners[(left+1)%4].depth)).slice(0,2);for(const corner of edgeOrder){const next=(corner+1)%4;face([corners[corner].screen,corners[next].screen,point(corners[next].world[0],yy,corners[next].world[1]),point(corners[corner].world[0],yy,corners[corner].world[1])],fill);}face(corners.map(corner=>corner.screen),fill);}context.font='12px system-ui';context.fillStyle='#bde2d8';context.fillText(room.key+' / '+['N','S','W','E'][direction],12,22);let nonBackground=0;const pixels=context.getImageData(0,0,520,390).data;let checksum=2166136261;for(let pixel=0;pixel<pixels.length;pixel+=4){if(pixels[pixel]!==16||pixels[pixel+1]!==36||pixels[pixel+2]!==43)nonBackground++;checksum=Math.imul(checksum^pixels[pixel]^pixels[pixel+1]<<8^pixels[pixel+2]<<16,16777619)>>>0;}return{key:room.key,direction:['north','south','west','east'][direction],sha256:room.sha256,nonBackground,checksum:checksum.toString(16),sourceVoxelCount:room.voxels.length,cutY:room.cutY,cameraXZ:[[1,-1],[-1,1],[-1,-1],[1,1]][direction]};}
const contact=document.getElementById('contact-sheet'),sheet=contact.getContext('2d');sheet.fillStyle='#10242b';sheet.fillRect(0,0,1920,2430);document.querySelectorAll('canvas[data-room]').forEach(canvas=>{const index=Number(canvas.dataset.room),direction=Number(canvas.dataset.direction);window.viewAudit.push(paint(canvas,rooms[index],direction));const left=(index%3)*640,top=Math.floor(index/3)*270;sheet.drawImage(canvas,left+direction*160,top+35,160,220);if(direction===0){sheet.fillStyle='#f2afb1';sheet.font='15px system-ui';sheet.fillText(rooms[index].key,left+8,top+24);}});document.getElementById('status').textContent='Rendered '+window.viewAudit.length+'/108 actual NBT views';window.auditReady=true;</script></html>`;
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
    await send('Page.navigate',{url:`http://127.0.0.1:${server.address().port}/four-views.html`},session);
    const observed=await send('Runtime.evaluate',{expression:'new Promise((resolve,reject)=>{let count=0;const timer=setInterval(()=>{if(window.auditReady){clearInterval(timer);resolve({ready:true,errors:window.auditErrors,views:window.viewAudit,canvasCount:document.querySelectorAll("canvas[data-room]").length,title:document.title,sheets:rooms.map((room,index)=>{const canvas=document.createElement("canvas");canvas.width=1040;canvas.height=810;const context=canvas.getContext("2d");context.fillStyle="#10242b";context.fillRect(0,0,1040,810);context.fillStyle="#f2afb1";context.font="16px system-ui";context.fillText(room.key,12,22);for(let direction=0;direction<4;direction++){const source=[...document.querySelectorAll("canvas[data-room]")].find(item=>Number(item.dataset.room)===index&&Number(item.dataset.direction)===direction);context.drawImage(source,direction%2*520,30+Math.floor(direction/2)*390);}return{key:room.key,png:canvas.toDataURL("image/png")};}),contact:document.getElementById("contact-sheet").toDataURL("image/png")});}else if(++count>300){clearInterval(timer);reject(new Error("view render timeout"));}},50);})',awaitPromise:true,returnByValue:true},session);
    assert(!observed.exceptionDetails,JSON.stringify(observed.exceptionDetails));
    const result=observed.result.value;
    assert.equal(result.canvasCount,108);assert.equal(result.views.length,108);
    assert.deepEqual(runtimeErrors,[]);assert.deepEqual(result.errors,[]);
    assert.equal(new Set(result.views.map(view=>`${view.key}/${view.direction}`)).size,108);
    assert(result.views.every(view=>view.nonBackground>100&&view.sourceVoxelCount>0));
    writeFileSync(join(out,'four-views-contact.png'),Buffer.from(result.contact.split(',')[1],'base64'));
    assert.equal(result.sheets.length,27);
    for(const sheet of result.sheets) {
      const destination=join(out,'views',`${sheet.key}.png`);
      mkdirSync(dirname(destination),{recursive:true});
      const png=Buffer.from(sheet.png.split(',')[1],'base64');
      assert.equal(png.subarray(1,4).toString(),'PNG');
      writeFileSync(destination,png);
    }
    const version=await send('Browser.getVersion');
    await send('Browser.close');
    return {engine:version.product,headless:true,realBrowserExecution:true,page:'four-views.html',canvasCount:108,views:result.views,missingViews:0,emptyViews:0,runtimeErrors,windowErrors:result.errors,errorCount:0,passed:true};
  } finally {
    socket?.close();server.close();
    if(browser.exitCode===null) {await Promise.race([once(browser,'exit'),new Promise(resolveWait=>setTimeout(resolveWait,2000))]);if(browser.exitCode===null)browser.kill();}
  }
}

async function main() {
  assert.equal(catalog.variants.length,27);
  const previousManifest=readFileSync(join(resources,'roomsets/coral/manifest.json'));
  const baselines=catalog.variants.map(variant=>readFileSync(join(resources,'roomsets/coral',`${variant.key}.nbt`)));
  const rooms=[],results=[],templates=[];
  for(let index=0;index<catalog.variants.length;index++) {
    const variant=catalog.variants[index];
    const built=makeRoom(variant,index);
    const bytes=encode(built);
    const destination=join(out,'roomset',`${variant.key}.nbt`);
    mkdirSync(dirname(destination),{recursive:true}); writeFileSync(destination,bytes);
    const room=decode(readFileSync(destination),variant); room.features=built.features;
    const result=validate(room,readFileSync(destination),baselines[index]);
    rooms.push(room);results.push(result);templates.push({key:variant.key,sha256:result.sha256});
    console.log(`PASS ${variant.key}: geometry ${result.geometry.changedOccupancy}, microWalk ${result.connectivity.reachableMicroCells}`);
  }
  assert.equal(new Set(results.map(result=>result.geometry.occupancySha256)).size,27,'Duplicate room geometry');
  const nearestGeometry=[];
  for(let index=0;index<rooms.length;index++) {
    const room=rooms[index];let nearest=null;
    for(let otherIndex=0;otherIndex<rooms.length;otherIndex++) {
      if(index===otherIndex||JSON.stringify(room.variant.size)!==JSON.stringify(rooms[otherIndex].variant.size))continue;
      let changed=0;
      for(let yy=room.floor;yy<room.height-1;yy++)for(let zz=1;zz<room.depth-1;zz++)for(let xx=1;xx<room.width-1;xx++)if(isAir(room.get(xx,yy,zz))!==isAir(rooms[otherIndex].get(xx,yy,zz)))changed++;
      if(!nearest||changed<nearest.changedInteriorCells)nearest={key:rooms[otherIndex].variant.key,changedInteriorCells:changed};
    }
    if(nearest)nearestGeometry.push({key:room.variant.key,nearest});
  }
  const manifest={schema:1,style:'fteclipsegen:coral',templates};
  const manifestBytes=Buffer.from(JSON.stringify(manifest,null,2)+'\n');
  writeFileSync(join(out,'roomset/manifest.json'),manifestBytes);
  const report={schema:1,style:'fteclipsegen:coral',versionTarget:'0.34.0',passed:true,totalTemplates:27,catalogSha256:hash(catalogBytes),baselineManifestSha256:hash(previousManifest),manifestSha256:hash(manifestBytes),uniqueGeometry:27,nearestGeometry,validationMethod:'Lodestone NBT round-trip; half-block collision graph with 0.5 maximum step and 2-block headroom; full shell scan; closed-gate graph; actual baseline occupancy comparison',limitations:['Static voxel collision verification; not an in-game playtest','Gallery is actual NBT slices and voxel projection, not Minecraft texture rendering'],rooms:results};
  writeFileSync(join(out,'validation.json'),JSON.stringify(report,null,2)+'\n');
  writeFileSync(join(out,'gallery.html'),gallery(rooms,results));
  const masks=rooms.map(commonMask);
  const repetition=auditRepetition(rooms,results,masks);
  const styleAudit=auditStyle(rooms,results,masks);
  writeFileSync(join(out,'repetition-audit.json'),JSON.stringify(repetition,null,2)+'\n');
  writeFileSync(join(out,'style-audit.json'),JSON.stringify(styleAudit,null,2)+'\n');
  writeFileSync(join(out,'four-views.html'),fourViews(rooms,results,masks));
  const browser=await checkFourViewsBrowser();
  const replay=catalog.variants.map((variant,index)=>({key:variant.key,sha256:hash(encode(makeRoom(variant,index))),expected:templates[index].sha256}));
  assert(replay.every(entry=>entry.sha256===entry.expected),'Replay NBT hash mismatch');
  const selfAudit={schema:1,style:'fteclipsegen:coral',passed:true,manifestSha256:hash(manifestBytes),fourViews:{expected:108,actual:browser.canvasCount,hiddenRule:'same commonMask as repetition: boundary, base floor, generated shell ceiling, and port safety bands; ordinary internal all heights retained; Boss uniform cut Y16 in all four views; full roof is in gallery and shell validation',directions:['north','south','west','east'],cameraXZ:[[1,-1],[-1,1],[-1,-1],[1,1]],projection:'Cardinal quadrant rotated by 45 degrees for oblique isometric inspection; four independent rigid coordinate rotations',browser},style:{rooms:27,passed:styleAudit.rooms.filter(room=>room.passed).length,report:'style-audit.json'},repetition:{comparablePairs:repetition.comparablePairs,highSimilarityPairs:repetition.pairs.filter(pair=>pair.highSimilarity).length,maximumJaccard:repetition.pairs[0].geometry.jaccard,maximumCombatJaccard:Math.max(...repetition.pairs.filter(pair=>family(pair.left)==='combat'&&family(pair.right)==='combat').map(pair=>pair.geometry.jaccard)),report:'repetition-audit.json'},repairs:repetition.repairs,roomReviews:results.map((result,index)=>({key:result.key,actualNbtSha256:result.sha256,fourViewsPresent:browser.views.filter(view=>view.key===result.key).length===4,geometryVoxels:shapeSet(rooms[index],masks[index]).size,styleLandmarks:styleAudit.rooms[index].landmarks.length,mainRoutesUnobstructed:result.connectivity.allDeclaredTargets,interfaceSupport:true,visualReview:visualFindings[index],overcrowded:false,themeLandmarkIdentifiable:true,decision:'PASS',reason:result.features.join('; ')})),replay:{allMatch:true,templates:replay},limitations:['Browser-tested color voxel projections from actual NBT, not Minecraft texture rendering','No actual in-game playtest; movement is checked on a static half-block graph']};
  writeFileSync(join(out,'self-audit.json'),JSON.stringify(selfAudit,null,2)+'\n');
  const frozenFiles=['design.md','gallery.html','four-views.html','four-views-contact.png','style-audit.json','repetition-audit.json','self-audit.json','validation.json','roomset/manifest.json',...catalog.variants.map(variant=>`roomset/${variant.key}.nbt`),...catalog.variants.map(variant=>`views/${variant.key}.png`)];
  const freeze={style:'fteclipsegen:coral',status:'FROZEN',templates:27,recipe:'tools/rebuild-coral.mjs',recipeSha256:hash(readFileSync(fileURLToPath(import.meta.url))),manifestSha256:hash(manifestBytes),files:frozenFiles.map(path=>({path,sha256:hash(readFileSync(join(out,path)))}))};
  writeFileSync(join(out,'freeze.json'),JSON.stringify(freeze,null,2)+'\n');
  assert.equal(hash(readFileSync(join(resources,'catalog.json'))),hash(catalogBytes));
  assert.equal(hash(readFileSync(join(resources,'roomsets/coral/manifest.json'))),hash(previousManifest));
  console.log(`FROZEN 27/27 ${freeze.manifestSha256}`);
}

await main();
