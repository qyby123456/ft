const { Canvas } = require('./png.cjs');
const fs = require('fs');
const dir = process.argv[2];
const rnd = (x,y,s)=>(((x*31+y*17+s*7)%9)/9);
// 碎片: 紫水晶碎片 + 铜锈边
const a = new Canvas(16,16);
for (let y=0;y<16;y++) for (let x=0;x<16;x++){
  const dx=x-7, dy=y-8, d=Math.abs(dx)+Math.abs(dy);
  if (d<=6 && d>=2){ const n=rnd(x,y,1)*40-20; a.set(x,y,150+n,96+n,214+n,1); }
  if (d<2){ const n=rnd(x,y,2)*30; a.set(x,y,214+n,180+n,246,1); }
  if (d===1||d===6){ a.set(x,y,96,150,120,1); }
}
a.save(dir+'/defect_fragment.png');
// 核心(召唤物): 深板岩外壳 + 铜环 + 紫核心
const b = new Canvas(16,16);
for (let y=0;y<16;y++) for (let x=0;x<16;x++){
  const n=rnd(x,y,3)*22-11;
  const ring = (Math.abs(x-7.5)>4.5||Math.abs(y-7.5)>4.5);
  if (ring) b.set(x,y,62+n,64+n,76+n,1); else b.set(x,y,176+n,108+n,58+n,1);
}
for (let y=5;y<11;y++) for (let x=5;x<11;x++){
  const n=rnd(x,y,4)*24-12;
  const mid = (Math.abs(x-7.5)<2 && Math.abs(y-7.5)<2);
  if (mid) b.set(x,y,196+n,150+n,246,1); else b.set(x,y,124+n,72+n,190+n,1);
}
b.set(7,7,236,214,255,1); b.set(8,8,236,214,255,1);
b.save(dir+'/defect_core.png');
console.log('物品贴图已生成');
