const fs=require('fs');
for (const t of ['glass','lightning','frost','ion','dark']) {
  const g=JSON.parse(fs.readFileSync(`out_orbs/${t}_orb.geo.json`,'utf8'))['minecraft:geometry'][0];
  let mn=[9e9,9e9,9e9],mx=[-9e9,-9e9,-9e9];
  for(const b of g.bones) for(const c of (b.cubes||[])) for(let i=0;i<3;i++){
    mn[i]=Math.min(mn[i],c.origin[i]); mx[i]=Math.max(mx[i],c.origin[i]+c.size[i]);
  }
  const d=[0,1,2].map(i=>mx[i]-mn[i]);
  console.log(`${t.padEnd(10)} ${d.map(v=>v.toFixed(1)).join(' x ')} 单位 = ${d.map(v=>(v/16).toFixed(2)).join(' x ')} 格`);
}
