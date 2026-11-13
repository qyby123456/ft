import {tools} from '../kit.mjs'
export default function build(api) {
  const {structure,put,fill,container,arch,shell,pillar,lamp,pot}=tools(api,[27,12,35])
  shell(12)
  for(const east of [8,18]) for(let south=2;south<30;south+=7) {
    fill([east,2,south],[east,7,south+5],'iron_bars')
    fill([east===8?1:19,2,south-1],[east===8?8:25,7,south-1],'cut_sandstone')
    fill([east,7,south],[east,7,south+5],'chiseled_sandstone')
    const doorSouth=south+2
    put([east,2,doorSouth],'iron_door',{facing:east===8?'west':'east',half:'lower',hinge:'left',open:'false',powered:'false'})
    put([east,3,doorSouth],'iron_door',{facing:east===8?'west':'east',half:'upper',hinge:'left',open:'false',powered:'false'})
    const controlEast=east===8?9:17
    put([east,3,doorSouth-1],'chiseled_sandstone')
    put([controlEast,3,doorSouth-1],'lever',{face:'wall',facing:east===8?'east':'west',powered:'false'})
    fill([east===8?2:23,2,south+1],[east===8?3:24,2,south+3],'hay_block')
    pot(east===8?6:20,2,south+4)
    pillar(east,south-1,9)
  }
  for(let south=6;south<28;south+=7) {lamp(11,11,south);lamp(15,11,south);put([13,1,south],'orange_terracotta')}
  fill([10,2,29],[10,2,31],'dark_oak_planks');fill([14,2,29],[14,2,31],'dark_oak_planks');fill([10,3,29],[14,3,31],'dark_oak_planks')
  put([12,4,30],'lectern',{facing:'south'});put([10,4,30],'candle',{candles:'2',lit:'true'});container([16,2,31])
  arch('north',13);arch('south',13)
  return {structure,metadata:{name:'s2-12-prison-block'}}
}
