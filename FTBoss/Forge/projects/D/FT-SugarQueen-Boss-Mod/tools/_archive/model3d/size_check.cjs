const fs=require('fs');
for(const id of ['statue_sugar_queen','statue_butcher','statue_saya','statue_wine_barrel','statue_rat_swarm']){
  let mn=[1e9,1e9,1e9],mx=[-1e9,-1e9,-1e9];
  for(const [half,off] of [['lower',0],['upper',16]]){
    const m=JSON.parse(fs.readFileSync(`out_statues_real/models/block/${id}_${half}_painted.json`,'utf8'));
    for(const e of m.elements)for(let i=0;i<3;i++){mn[i]=Math.min(mn[i],e.from[i]+(i===1?off:0));mx[i]=Math.max(mx[i],e.to[i]+(i===1?off:0));}
  }
  console.log(`${id.padEnd(22)} ${((mx[0]-mn[0])/16).toFixed(2)}x${((mx[1]-mn[1])/16).toFixed(2)}x${((mx[2]-mn[2])/16).toFixed(2)} 格`);
}
