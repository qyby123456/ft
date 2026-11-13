import {toolkit} from './common.mjs'
export default function build(api){
  const {put,box,lamp,icicle,finish}=toolkit(api,[35,14,29],'s3-15')
  box(0,0,0,34,1,28,'stone_bricks');box(5,1,5,29,1,23,'sand')
  box(0,2,0,34,11,1,'deepslate_bricks');box(0,2,27,34,11,28,'deepslate_bricks')
  box(0,2,2,1,11,26,'stone_bricks');box(33,2,2,34,11,26,'stone_bricks')
  box(0,12,0,34,12,28,'packed_ice');box(0,13,0,34,13,28,'snow_block')
  for(const east of [9,17,25]){put(east,2,24,'spruce_fence');put(east,3,24,'target')}
  for(const [east,south] of [[10,10],[17,19],[25,9]]){
    put(east,2,south,'spruce_fence');box(east,3,south,east,4,south,'hay_block[axis=y]')
    put(east-1,3,south,'hay_block[axis=x]');put(east+1,3,south,'hay_block[axis=x]');put(east,5,south,'carved_pumpkin[facing=north]')
  }
  for(let south=14;south<=19;south++){
    const level=2+Math.floor((south-14)/2)
    if(level>2)box(11,2,south,14,level-1,south,'spruce_planks')
    box(11,level,south,14,level,south,(south%2===0)?'spruce_slab[type=bottom]':'spruce_planks')
  }
  for(const south of [17,21]){
    box(24,2,south-1,24,2,south+1,'spruce_log[axis=z]')
    box(29,2,south-1,29,2,south+1,'spruce_log[axis=z]')
    box(24,3,south,24,4,south,'spruce_fence')
    box(29,3,south,29,4,south,'spruce_fence')
    box(24,5,south,29,5,south,'spruce_log[axis=x]')
    put(25,4,south,'spruce_fence');put(28,4,south,'spruce_fence')
  }
  for(const east of [5,18]){box(east,2,3,east+10,2,3,'spruce_slab[type=bottom]');box(east,2,5,east+10,2,5,'spruce_slab[type=bottom]')}
  box(3,2,20,3,5,25,'spruce_fence');for(const south of [21,23,25])put(4,5,south,'end_rod[facing=east]')
  put(4,2,18,'barrel[facing=up]',{id:'minecraft:barrel',Items:[]})
  box(30,2,20,31,5,23,'blue_ice');box(3,2,7,4,2,10,'snow_block')
  for(const east of [3,12,22,31])icicle(east,26,11,4)
  lamp(7,22,12);lamp(29,12,12)
  box(0,2,10,1,6,14,'air');box(33,2,10,34,5,12,'air')
  put(33,2,11,'spruce_door[facing=east,half=lower,hinge=left,open=true,powered=false]');put(33,3,11,'spruce_door[facing=east,half=upper,hinge=left,open=true,powered=false]')
  return finish({title:'雪穴砂地训练场',reference:'S3-15',features:['砂地演练区','三个草人','三靶','低障碍与台阶','双排观众凳','一侧宽洞一侧开门'],ports:[{name:'west',foot:[0,2,12],width:5,height:5,normal:[-1,0,0]},{name:'east',foot:[34,2,11],width:3,height:4,normal:[1,0,0]}],start:[0,2,12],targets:[{label:'east exit',position:[34,2,11]},{label:'targets',position:[17,2,23]},{label:'training centre',position:[17,2,13]},{label:'audience',position:[22,2,4]}],differences:['草人为方块雕塑非可击杀实体','靶为原版target；未连接计分红石','障碍是可绕行低障碍，不承诺跑酷验收','场地完整封顶以满足结构交付，概念敞开效果仅预览'],cutY:12})
}
