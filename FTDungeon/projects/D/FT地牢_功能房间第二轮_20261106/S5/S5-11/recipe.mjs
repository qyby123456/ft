import {room} from '../tools.mjs'
export default function(api){
  const {put,fill,box,container,coral,finish}=room(api,[29,14,25])
  box([0,0,0],[28,11,24],'prismarine_bricks')
  fill([1,1,1],[27,1,23],'dark_prismarine')
  fill([1,2,1],[27,2,23],'prismarine')
  fill([3,2,5],[25,2,20],'water')
  for(const east of [2,8,14,20,26]){
    fill([east,3,1],[east,10,2],'prismarine_bricks')
    put([east,7,3],'sea_lantern')
  }
  for(const [start,end] of [[3,7],[9,13],[15,19],[21,25]])fill([start,3,1],[end,9,1],'bookshelf')
  fill([1,3,4],[1,8,18],'bookshelf')
  fill([27,3,4],[27,8,18],'bookshelf')
  for(const south of [6,17]){
    fill([10,2,south],[17,2,south+2],'prismarine_bricks')
    fill([11,3,south],[16,3,south],'dark_oak_planks')
    put([13,4,south],'lectern',{facing:'south',has_book:'false',powered:'false'})
    for(const east of [11,16])put([east,4,south],'candle',{candles:'1',lit:'true',waterlogged:'false'})
  }
  fill([14,3,2],[14,9,2],'ladder',{facing:'south',waterlogged:'false'})
  fill([22,3,2],[24,5,3],'dark_oak_planks')
  container([23,4,4])
  put([23,3,4],'dark_oak_planks')
  for(const east of [0,28]){
    fill([east,3,10],[east,6,14],'air')
    fill([east,2,10],[east,2,14],'prismarine_bricks')
  }
  fill([0,2,10],[28,2,14],'prismarine_bricks')
  fill([0,3,10],[2,6,14],'air')
  fill([26,3,10],[28,6,14],'air')
  fill([12,2,8],[14,2,16],'prismarine_bricks')
  fill([21,2,3],[25,2,6],'prismarine_bricks')
  for(const [east,south,color] of [[4,6,'brain'],[24,18,'fire'],[5,19,'bubble']])coral([east,1,south],color)
  for(const east of [5,23])for(const south of [5,20])put([east,2,south],'sea_lantern')
  fill([3,11,4],[25,11,20],'cyan_stained_glass')
  for(const east of [2,8,14,20,26])fill([east,11,1],[east,11,23],'prismarine_bricks')
  fill([0,12,0],[28,12,0],'prismarine_bricks')
  fill([0,12,24],[28,12,24],'prismarine_bricks')
  return finish('S5-11','Tall U-shaped archive shelving, dual reading islands, ladder and cabinet; complete glazed tide roof.',[
    {name:'west',feet:[0,3,12],facing:'west',width:5,height:4},{name:'east',feet:[28,3,12],facing:'east',width:5,height:4}
  ],[{label:'desk-front',position:[13,3,9]},{label:'desk-back',position:[13,3,16]},{label:'cabinet',position:[23,3,5]},{label:'ladder-base',position:[14,3,3]}],[
    'Concept cutaway replaced by complete prismarine walls and cyan-glass roof.',
    'Only one-block-deep contained pools; dry raised central spine keeps head above water.',
    'Lecterns have no book payload; scrolls, locked state and magical bubbles not simulated.',
    'Living coral is waterlogged; no kelp above the shallow waterline. Roof coral omitted rather than allowed to die.'
  ])
}
