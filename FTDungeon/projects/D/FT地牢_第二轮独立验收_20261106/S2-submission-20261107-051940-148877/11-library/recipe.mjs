import {tools} from '../kit.mjs'
export default function build(api) {
  const {structure,put,fill,container,arch,shell,pillar,lamp,pot,banner}=tools(api,[25,13,23])
  shell(12)
  for(const east of [2,8,16,22]) pillar(east,1,12)
  for(const south of [2,8,15,20]) pillar(23,south,12)
  fill([3,2,1],[7,8,1],'bookshelf');fill([9,2,1],[15,6,1],'bookshelf');fill([17,2,1],[21,9,1],'bookshelf')
  fill([23,2,3],[23,8,7],'bookshelf');fill([23,2,9],[23,8,14],'bookshelf')
  for(let up=2;up<9;up++) put([22,up,11],'ladder',{facing:'west'})
  for(let south=10;south<=12;south++) put([21,8,south],'dark_oak_slab',{type:'top'})
  for(const south of [10,12]) put([22,8,south],'dark_oak_slab',{type:'top'})
  container([20,2,3]);container([20,3,3]);container([21,2,3]);container([21,3,3])
  for(const [east,south] of [[7,10],[15,16]]) {
    for(const offset of [0,4]) {put([east+offset,2,south],'dark_oak_fence');put([east+offset,2,south+2],'dark_oak_fence')}
    fill([east,3,south],[east+4,3,south+2],'dark_oak_planks')
    put([east+2,4,south+1],'lectern',{facing:'south',has_book:'false'})
    put([east,4,south],'candle',{candles:'3',lit:'true'});put([east+4,4,south+2],'candle',{candles:'2',lit:'true'})
    put([east+2,2,south+3],'dark_oak_stairs',{facing:'north'})
  }
  for(const [east,south] of [[3,5],[17,3],[21,17]]) pot(east,2,south)
  for(const [east,south] of [[3,19],[21,20]]) {put([east,2,south],'sand');put([east,3,south],'dead_bush')}
  for(let east=9;east<=15;east++) for(let south=7;south<=17;south++) if(east===9||east===15||south===7||south===17||east===12&&south===12) put([east,1,south],'orange_terracotta')
  for(const [east,south] of [[6,6],[18,8],[15,19]]) lamp(east,11,south)
  fill([9,11,6],[14,11,10],'yellow_stained_glass')
  arch('west',9);arch('east',18)
  banner(12,9,1)
  return {structure,metadata:{name:'s2-11-library'}}
}
