import {toolkit} from './common.mjs'
export default function build(api){
  const {put,box,lamp,icicle,finish}=toolkit(api,[29,16,29],'s3-16')
  box(0,0,0,28,1,28,'mossy_stone_bricks')
  box(0,2,0,28,13,1,'stone_bricks');box(0,2,27,28,13,28,'stone_bricks')
  box(0,2,2,1,13,26,'mossy_stone_bricks');box(27,2,2,28,13,26,'mossy_stone_bricks')
  box(0,14,0,28,14,28,'packed_ice');box(0,15,0,28,15,28,'snow_block')
  box(8,2,8,20,3,20,'mossy_stone_bricks');box(10,2,10,18,2,18,'blue_ice');box(10,3,10,18,3,18,'air')
  box(8,4,8,20,4,9,'stone_brick_slab[type=bottom]');box(8,4,19,20,4,20,'stone_brick_slab[type=bottom]')
  box(8,4,10,9,4,18,'stone_brick_slab[type=bottom]');box(19,4,10,20,4,18,'stone_brick_slab[type=bottom]')
  for(const east of [9,19]){box(east,4,14,east,10,14,'spruce_log');put(east,11,14,'polished_andesite')}
  box(9,10,14,19,10,14,'spruce_log[axis=x]');box(14,6,14,14,9,14,'chain[axis=y]')
  put(14,5,14,'barrel[facing=up]',{id:'minecraft:barrel',Items:[]})
  box(13,1,2,15,1,7,'blue_ice');box(12,2,5,16,2,6,'stone_brick_slab[type=top]')
  box(2,2,22,6,2,24,'spruce_fence');box(2,3,22,6,3,24,'spruce_slab[type=top]');put(3,4,23,'lantern[hanging=false]')
  for(const [east,south] of [[22,11],[23,11],[22,12],[7,6]])put(east,2,south,'barrel[facing=up]',{id:'minecraft:barrel',Items:[]})
  box(11,2,24,17,9,25,'packed_ice');box(13,2,23,15,6,23,'blue_ice')
  box(11,2,24,17,9,25,'mossy_stone_bricks')
  for(let east=10;east<=19;east++){
    const height=5+Math.floor((1+Math.sin(east*.8))*3)
    box(east,2,25,east,height,26,'packed_ice')
    if(east%3===0)box(east,3,24,east,height-2,24,'blue_ice')
  }
  for(const [east,south] of [[8,8],[19,8],[8,19],[19,19]]){
    box(east,2,south,east+1,3,south+1,'chiseled_stone_bricks')
    put(east,4,south,'snow_block')
  }
  box(3,2,25,6,3,26,'mossy_stone_bricks')
  box(3,4,26,5,5,26,'packed_ice')
  for(const [east,south] of [[5,8],[23,20]])lamp(east,south,14)
  for(const east of [3,8,19,25])icicle(east,26,13,4)
  box(0,2,17,1,5,19,'air');box(27,2,17,28,5,19,'air')
  return finish({title:'霜井与冻结水渠',reference:'S3-16',features:['中央方井','木梁链条提桶','冻结浅渠与桥','维修台','后壁冰瀑'],ports:[{name:'west',foot:[0,2,18],width:3,height:4,normal:[-1,0,0]},{name:'east',foot:[28,2,18],width:3,height:4,normal:[1,0,0]}],start:[0,2,18],targets:[{label:'east exit',position:[28,2,18]},{label:'well side',position:[7,2,14]},{label:'maintenance desk',position:[6,2,21]},{label:'back ledge',position:[21,2,24]}],differences:['井与水渠为蓝冰，完全冻结无可取水功能；无动态水流','桶使用空木桶，提桶机构静态无红石','维修边缘可绕行，井有实体矮围防跌落'],cutY:14})
}
