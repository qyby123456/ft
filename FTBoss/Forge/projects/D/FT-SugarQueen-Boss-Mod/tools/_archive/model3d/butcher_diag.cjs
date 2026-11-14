const fs=require('fs');
const m=JSON.parse(fs.readFileSync('out_statues_real/models/block/statue_butcher_lower_painted.json','utf8'));
const up=JSON.parse(fs.readFileSync('out_statues_real/models/block/statue_butcher_upper_painted.json','utf8'));
const els=[];
for(const e of m.elements) els.push({src:'lower',from:e.from,to:e.to,uv:e.faces.north.uv});
for(const e of up.elements) els.push({src:'upper',from:[e.from[0],e.from[1]+16,e.from[2]],to:[e.to[0],e.to[1]+16,e.to[2]],uv:e.faces.north.uv});
console.log(`共 ${els.length} 个 element`);
// 几何: 找相交或共面的对
const EPS=1e-6;
let pairs=0, coin=0;
for(let i=0;i<els.length;i++)for(let j=i+1;j<els.length;j++){
  const a=els[i],b=els[j];
  const ov=[0,1,2].map(k=>Math.min(a.to[k],b.to[k])-Math.max(a.from[k],b.from[k]));
  if(ov.every(v=>v>=0)){ pairs++; const flat=ov.filter(v=>v<=0.02).length;
    if(pairs<=14) console.log(`  相交/贴合 A#${i}(${a.src}) B#${j}(${b.src}) 重叠=${ov.map(v=>v.toFixed(3)).join(',')}  贴面轴数=${flat}`);
    if(flat>0) coin++;
  }
}
console.log(`几何: 相交或贴合的元素对 = ${pairs} (其中含"贴合(<=0.02单位)"的 = ${coin})`);
// 贴图: north 面 UV 矩形是否互相重叠(单位: 贴图 0..16 单位)
const rects=els.map(e=>({uv:e.uv}));
let uvOv=0;
for(let i=0;i<rects.length;i++)for(let j=i+1;j<rects.length;j++){
  const a=rects[i].uv,b=rects[j].uv;
  const ow=Math.min(a[2],b[2])-Math.max(a[0],b[0]);
  const oh=Math.min(a[3],b[3])-Math.max(a[1],b[1]);
  if(ow>0.01&&oh>0.01){ uvOv++; if(uvOv<=10) console.log(`  UV 重叠: A#${i} [${a.join(',')}]  B#${j} [${b.join(',')}]  -> ${ow.toFixed(2)}x${oh.toFixed(2)}`); }
}
console.log(`UV(north) 互相重叠的对 = ${uvOv}`);
