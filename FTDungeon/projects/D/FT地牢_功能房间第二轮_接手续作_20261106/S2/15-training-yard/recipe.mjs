import {tools} from '../kit.mjs'
export default function build(api){
  const {structure,put,fill,container,arch,shell,lamp,pot,banner}=tools(api,[31,13,27]);shell(13,7)
  for(let east=3;east<=27;east++) for(let south=5;south<=22;south++) put([east,1,south],(east*7+south)%11===0?'gravel':'sand')
  for(const east of [11,15,19]) {put([east,2,2],'oak_fence');put([east,3,2],'target')}
  for(const [east,south] of [[10,10],[18,10],[14,15]]) {put([east,2,south],'oak_planks');put([east,3,south],'oak_fence');put([east,4,south],'hay_block',{axis:'y'});put([east,5,south],'carved_pumpkin',{facing:'south'});put([east-1,4,south],'oak_fence');put([east+1,4,south],'oak_fence')}
  for(const east of [6,21]) {fill([east,2,23],[east+4,2,23],'oak_stairs');fill([east,2,24],[east+4,2,24],'oak_slab')}
  for(const [east,south] of [[8,19],[20,18]]) {put([east,2,south],'oak_fence');put([east+3,2,south],'oak_fence');for(let offset=0;offset<=3;offset++) put([east+offset,3,south],'orange_terracotta')}
  fill([24,2,12],[24,4,12],'oak_planks');fill([27,2,12],[27,4,12],'oak_planks');fill([24,5,12],[27,5,12],'oak_planks')
  container([3,2,4]);container([27,2,4]);pot(26,2,7)
  for(const east of [7,23]) lamp(east,12,7)
  fill([7,12,8],[23,12,21],'yellow_stained_glass')
  arch('west',8);arch('east',8)
  banner(15,8,1);banner(4,8,1);banner(26,8,1)
  return {structure,metadata:{name:'s2-15-training-yard'}}
}
