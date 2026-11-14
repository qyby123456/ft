const fs=require('fs');
const els=[];
for(const [half,off] of [['lower',0],['upper',16]]){
  const m=JSON.parse(fs.readFileSync(`out_statues_real/models/block/statue_sugar_queen_${half}_painted.json`,'utf8'));
  for(const e of m.elements) els.push({from:[e.from[0],e.from[1]+off,e.from[2]],to:[e.to[0],e.to[1]+off,e.to[2]],uv:e.faces.north.uv});
}
// 只看 y 在 10..20(躯干/胸口高度) 的元素, 按 z 从小到大
const list=els.filter(e=>e.from[1]<20&&e.to[1]>10).sort((a,b)=>a.from[2]-b.from[2]);
console.log('胸口高度(y10..20)的元素, 按 z 由小到大(小 z = 靠近正面相机):');
for(const e of list){
  const mid=(e.uv[0]+e.uv[2])/2/16*128, midv=(e.uv[1]+e.uv[3])/2/16*128;
  console.log(`  z=${e.from[2].toFixed(2)}..${e.to[2].toFixed(2)} x=${e.from[0].toFixed(1)}..${e.to[0].toFixed(1)} y=${e.from[1].toFixed(1)}..${e.to[1].toFixed(1)}  uv中心=(${mid.toFixed(0)},${midv.toFixed(0)})`);
}
