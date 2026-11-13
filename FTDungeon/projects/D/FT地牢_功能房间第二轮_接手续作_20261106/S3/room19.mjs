import {toolkit} from './common.mjs'
export default function build(api){
  const tools=toolkit(api,[35,23,61],'s3-19')
  const put=(east,up,south,state,entity)=>tools.put(east,up,south+30,state,entity)
  const box=(east,up,south,right,top,back,state,entity)=>tools.box(east,up,south+30,right,top,back+30,state,entity)
  const icicle=(east,south,top,length)=>tools.icicle(east,south+30,top,length)
  const finish=details=>{
    for(const port of details.ports)port.foot[2]+=30
    details.start[2]+=30
    for(const target of details.targets)target.position[2]+=30
    details.features.push('三十格纵深完整封闭冰洞观景口','分层洞壁与悬垂冰檐')
    details.differences[1]='高台前方保留三十格封闭冰洞纵深，以弧形冻石壁代替近距平砖墙；栏杆及完整外壳不变'
    return tools.finish(details)
  }
  box(0,0,0,34,1,30,'deepslate_tiles')
  box(0,2,0,34,20,1,'deepslate_bricks');box(0,2,29,34,20,30,'deepslate_bricks')
  box(0,2,2,1,20,28,'deepslate_bricks');box(33,2,2,34,20,28,'deepslate_bricks')
  box(0,21,0,34,21,30,'packed_ice');box(0,22,0,34,22,30,'snow_block')
  box(3,2,15,28,7,28,'stone_bricks');box(16,2,28,20,7,30,'stone_bricks')
  box(4,2,15,27,5,24,'air')
  for(const east of [4,26])box(east,2,24,east+1,6,25,'stone_bricks')
  for(let step=0;step<12;step++){
    const south=2+step,level=2+Math.floor(step/2)
    if(level>2)box(29,2,south,32,level-1,south,'stone_bricks')
    box(29,level,south,32,level,south,step%2===0?'stone_brick_slab[type=bottom]':'stone_bricks')
  }
  box(29,2,14,32,7,17,'stone_bricks')
  box(3,8,15,27,8,15,'spruce_fence');box(3,8,16,3,8,27,'spruce_fence')
  for(const east of [3,9,17,25]){put(east,8,15,'stone_brick_wall');put(east,9,15,'snow_block')}
  for(const [east,south,high] of [[3,5,6],[25,4,8]]){
    box(east,2,south,east+2,3,south+3,'deepslate_bricks')
    box(east,4,south+1,east+1,high-1,south+2,'packed_ice')
    put(east,high,south+2,'blue_ice')
  }
  for(const east of [10,18])put(east,8,18,'spruce_fence')
  box(10,9,18,18,9,20,'spruce_slab[type=top]');put(11,10,19,'lantern[hanging=false]')
  put(15,10,19,'lightning_rod[facing=west,powered=false]')
  put(16,10,19,'spruce_fence');put(16,11,19,'lightning_rod[facing=west,powered=false]')
  put(15,11,19,'dark_prismarine_slab[type=bottom]')
  box(5,8,26,10,11,26,'spruce_fence');for(const east of [6,8,10])put(east,12,26,'end_rod[facing=up]')
  put(22,8,26,'barrel[facing=up]',{id:'minecraft:barrel',Items:[]})
  box(6,8,17,6,14,17,'spruce_fence');box(6,14,17,9,14,17,'spruce_log[axis=x]');put(9,13,17,'chain[axis=y]');put(9,12,17,'lantern[hanging=true]')
  for(const east of [3,11,22,31])icicle(east,28,20,4)
  box(2,2,19,3,7,25,'packed_ice');box(25,8,25,27,11,27,'blue_ice')
  box(33,2,2,34,5,4,'air');box(17,8,29,19,11,30,'air')
  tools.box(0,0,0,34,1,29,'deepslate_tiles')
  tools.box(0,2,0,34,20,1,'deepslate_tiles')
  tools.box(0,2,2,1,20,29,'deepslate_tiles')
  tools.box(33,2,2,34,20,29,'deepslate_tiles')
  tools.box(0,21,0,34,21,29,'packed_ice')
  tools.box(0,22,0,34,22,29,'snow_block')
  box(3,4,0,27,18,1,'air')
  for(let south=2;south<30;south++){
    const inset=2+Math.floor((1+Math.sin(south*.41))*2)
    tools.box(2,2,south,inset,5+(south%5),south,'deepslate_bricks')
    tools.box(30-inset,2,south,32,7+(south%4),south,'packed_ice')
    tools.box(2,18+(south%3),south,inset+2,20,south,'packed_ice')
    tools.box(28-inset,17+(south%3),south,32,20,south,'deepslate_bricks')
  }
  for(const [east,south,high] of [[6,6,8],[24,11,6],[8,19,5],[27,24,9]]){
    tools.box(east-2,2,south-1,east+2,3,south+2,'deepslate_bricks')
    tools.box(east-1,4,south,east+1,high-2,south+1,'packed_ice')
    tools.box(east,high-1,south,east,high,south,'blue_ice')
    tools.icicle(east+2,south+2,21,4+(south%3))
  }
  tools.box(6,2,2,28,4,4,'deepslate_bricks')
  tools.box(11,5,2,22,8,3,'packed_ice')
  tools.box(15,9,2,18,11,2,'blue_ice')
  for(let east=2;east<33;east++){
    const depth=2+Math.floor((1+Math.sin(east*.63))*2)
    const height=5+Math.floor((1+Math.cos(east*.38))*4)
    tools.box(east,2,2,east,height,depth,'deepslate_tiles')
    tools.box(east,height+1,2,east,height+2,Math.max(2,depth-1),'packed_ice')
    tools.box(east,16+Math.floor(Math.abs(east-17)/5),2,east,20,depth+1,'deepslate_bricks')
  }
  box(3,4,0,5,18,2,'chiseled_stone_bricks')
  box(26,4,0,28,18,2,'chiseled_stone_bricks')
  box(5,18,0,26,19,3,'stone_bricks')
  return finish({title:'冰渊高台瞭望所',reference:'S3-19',features:['高六格俯瞰平台','十二级半格阶梯','观测桌与望远镜意象','栏墙灯柱','下方冰渊景观'],ports:[{name:'lower-east',foot:[34,2,3],width:3,height:4,normal:[1,0,0]},{name:'upper-south',foot:[18,8,30],width:3,height:4,normal:[0,0,1]}],start:[34,2,3],targets:[{label:'upper exit',position:[18,8,30]},{label:'watch desk',position:[14,8,21]},{label:'parapet',position:[20,8,16]},{label:'stair landing',position:[30,8,16]}],differences:['望远镜以避雷针表现，不提供放大视野交互','概念无限深渊改成完整封闭冰窟，保留高台与实际六格高差','半格台阶可步行上行，非单纯装饰楼梯'],cutY:21})
}
