const fs=require('fs');
const m=JSON.parse(fs.readFileSync('out_statues_real/models/block/statue_sugar_queen_lower_painted.json','utf8'));
const e=m.elements[0];
console.log('躯干 element north uv =', e.faces.north.uv.join(','), ' (源 box-UV rect 应为 [9,9,25,24] -> 期望发出 3.125,1.125,1.125,3)');
console.log('            up   uv =', e.faces.up.uv.join(','),   ' (源 [9,1,25,9]? 期望 v 交换)');
console.log('            south uv =', e.faces.south.uv.join(','));
// 检查所有 uv 都在 0..16 且有镜像(u1>u2 或 v1>v2)存在
let mirror=0,total=0,oob=0;
for(const f of ['statue_sugar_queen','statue_butcher','statue_saya','statue_wine_barrel','statue_rat_swarm'])
 for(const half of ['lower','upper']){
  const j=JSON.parse(fs.readFileSync(`out_statues_real/models/block/${f}_${half}_painted.json`,'utf8'));
  for(const el of j.elements) for(const k of ['north','south','east','west','up','down']){
    const uv=el.faces[k].uv; total++;
    if(uv[0]>uv[2]||uv[1]>uv[3]) mirror++;
    if(uv.some(v=>v<-0.01||v>16.01)) oob++;
  }
 }
console.log(`总面数 ${total}, 其中镜像写法 ${mirror}, 越界 ${oob}`);
