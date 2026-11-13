import {room} from '../tools.mjs'
export default function(api){
  const {put,fill,box,container,coral,finish}=room(api,[37,13,23])
  box([0,0,0],[36,10,22],'prismarine_bricks');fill([1,1,1],[35,1,21],'dark_prismarine');fill([1,2,1],[35,2,21],'prismarine')
  fill([7,3,1],[9,9,17],'dark_prismarine');fill([7,3,15],[9,6,17],'air')
  fill([1,3,8],[6,8,10],'prismarine_bricks');fill([0,3,11],[0,6,15],'air')
  fill([10,3,1],[26,9,3],'dark_prismarine');fill([10,3,19],[26,9,21],'dark_prismarine')
  fill([27,3,1],[29,9,13],'prismarine_bricks');fill([27,3,11],[29,6,13],'air')
  put([26,5,6],'lever',{face:'wall',facing:'west',powered:'false'})
  fill([30,3,4],[35,3,8],'chiseled_stone_bricks');container([33,4,6],'chest','south')
  for(const east of [31,35]){put([east,4,5],'gold_block');put([east,4,8],'gold_block')}
  put([35,8,4],'cobweb');put([30,7,5],'sea_lantern');put([35,7,9],'sea_lantern')
  fill([12,2,5],[15,2,8],'water');coral([13,1,6],'brain');fill([20,2,15],[24,2,17],'water');coral([22,1,16],'fire')
  for(const east of [4,12,20,28])put([east,8,18],'sea_lantern')
  fill([36,3,12],[36,6,16],'air')
  fill([1,10,4],[35,10,20],'cyan_stained_glass');for(const east of [7,18,29])fill([east,10,0],[east,10,22],'prismarine_bricks')
  return finish('S5-20','Dog-leg concealed passage past dark-prismarine baffles into gold-trimmed chest alcove; lever and off-route cobweb.',[
    {name:'west',feet:[0,3,13],facing:'west',width:5,height:4},{name:'east',feet:[36,3,14],facing:'east',width:5,height:4}
  ],[{label:'concealed-turn',position:[8,3,16]},{label:'passage',position:[18,3,11]},{label:'vault-throat',position:[28,3,12]},{label:'chest-approach',position:[33,3,10]}],[
    'This is a dedicated secret-vault room, not an overview.',
    'Disguised baffles form a real open dog-leg path; lever decorative, no piston secret-door/lock claim.',
    'Gold piles replaced by solid gold blocks; chest empty, no treasure rewards configured.',
    'Cobweb isolated overhead outside travel path; shallow live-coral pools remain contained.'
  ])
}
