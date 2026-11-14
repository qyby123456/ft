const fs=require('fs'),path=require('path');
const src=fs.readFileSync('gen_statue_real.cjs','utf8');
// 复用生成器里的函数: 直接 require 不行(会执行主流程), 这里用简易方式重算包围盒
const A='D:/ft工程文件/FT-SugarQueen-Boss-Mod/src/main/resources/assets/ftboss';
const geos={gingerbread_king:['geo/gingerbread_king.geo.json',128,128],butcher:['../../../tools/model3d/out/butcher_preview.geo.json',128,128],wine_barrel:['geo/wine_barrel_boss.geo.json',256,256],big_rat:['geo/big_rat.geo.json',64,64]};
function boxes(file){
  const g=JSON.parse(fs.readFileSync(file,'utf8'));
  const geo=g['minecraft:geometry'][0];
  const out=[];
  const I=[1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1];
  const mul=(a,b)=>{const o=new Array(16);for(let r=0;r<4;r++)for(let c=0;c<4;c++){let s=0;for(let k=0;k<4;k++)s+=a[r*4+k]*b[k*4+c];o[r*4+c]=s;}return o;};
  const tr=(x,y,z)=>[1,0,0,x,0,1,0,y,0,0,1,z,0,0,0,1];
  const xf=(m,p)=>[m[0]*p[0]+m[1]*p[1]+m[2]*p[2]+m[3],m[4]*p[0]+m[5]*p[1]+m[6]*p[2]+m[7],m[8]*p[0]+m[9]*p[1]+m[10]*p[2]+m[11]];
  let bones=geo.bones;
  if(!bones.some(b=>b.children)){const map={};for(const b of bones)map[b.name]=Object.assign({},b,{children:[]});const roots=[];for(const b of bones){if(b.parent&&map[b.parent])map[b.parent].children.push(map[b.name]);else roots.push(map[b.name]);}bones=roots;}
  (function walk(bs,pm){for(const b of bs){const p=b.pivot||[0,0,0];const m=mul(pm,tr(...p));for(const c of (b.cubes||[])){const p0=xf(m,c.origin),p1=xf(m,[c.origin[0]+c.size[0],c.origin[1]+c.size[1],c.origin[2]+c.size[2]]);out.push({o:[Math.min(p0[0],p1[0]),Math.min(p0[1],p1[1]),Math.min(p0[2],p1[2])],s:c.size.slice()});}if(b.children)walk(b.children,m);}})(bones,I);
  return out;
}
for(const [k,[f]] of Object.entries(geos)){
  const bs=boxes(path.join(A,f));
  let mn=[1e9,1e9,1e9],mx=[-1e9,-1e9,-1e9];
  for(const b of bs)for(let i=0;i<3;i++){mn[i]=Math.min(mn[i],b.o[i]);mx[i]=Math.max(mx[i],b.o[i]+b.s[i]);}
  const w=mx[0]-mn[0],h=mx[1]-mn[1],d=mx[2]-mn[2];
  const sNow=Math.min(30/h,15/w,15/d);
  const s22=Math.min(30/h,22/w,22/d);
  console.log(`${k.padEnd(17)} 源 ${w.toFixed(1)}x${h.toFixed(1)}x${d.toFixed(1)} 单位 (${(w/16).toFixed(2)}x${(h/16).toFixed(2)}x${(d/16).toFixed(2)} 格)`);
  console.log(`   现在(foot15): 高 ${(h*sNow/16).toFixed(2)} 格 宽 ${(w*sNow/16).toFixed(2)}      放宽到22: 高 ${(h*s22/16).toFixed(2)} 格 宽 ${(w*s22/16).toFixed(2)}`);
}

