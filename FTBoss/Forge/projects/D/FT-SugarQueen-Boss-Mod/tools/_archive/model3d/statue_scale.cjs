const fs=require('fs'),path=require('path');
const A='D:/ft工程文件/FT-SugarQueen-Boss-Mod/src/main/resources/assets/ftboss';
const I=[1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1];
const mul=(a,b)=>{const o=new Array(16);for(let r=0;r<4;r++)for(let c=0;c<4;c++){let s=0;for(let k=0;k<4;k++)s+=a[r*4+k]*b[k*4+c];o[r*4+c]=s;}return o;};
const tr=(x,y,z)=>[1,0,0,x,0,1,0,y,0,0,1,z,0,0,0,1];
const xf=(m,p)=>[m[0]*p[0]+m[1]*p[1]+m[2]*p[2]+m[3],m[4]*p[0]+m[5]*p[1]+m[6]*p[2]+m[7],m[8]*p[0]+m[9]*p[1]+m[10]*p[2]+m[11]];
function bbox(file){
  const g=JSON.parse(fs.readFileSync(file,'utf8'))['minecraft:geometry'][0];
  let bones=g.bones;
  if(!bones.some(b=>b.children)){const map={};for(const b of bones)map[b.name]=Object.assign({},b,{children:[]});const roots=[];for(const b of bones){if(b.parent&&map[b.parent])map[b.parent].children.push(map[b.name]);else roots.push(map[b.name]);}bones=roots;}
  const out=[]; 
  (function walk(bs,pm){for(const b of bs){const p=b.pivot||[0,0,0];const m=mul(pm,tr(...p));for(const c of (b.cubes||[])){const p0=xf(m,c.origin),p1=xf(m,[c.origin[0]+c.size[0],c.origin[1]+c.size[1],c.origin[2]+c.size[2]]);out.push({o:[Math.min(p0[0],p1[0]),Math.min(p0[1],p1[1]),Math.min(p0[2],p1[2])],s:c.size});}if(b.children)walk(b.children,m);}})(bones,I);
  let mn=[1e9,1e9,1e9],mx=[-1e9,-1e9,-1e9];
  for(const b of out)for(let i=0;i<3;i++){mn[i]=Math.min(mn[i],b.o[i]);mx[i]=Math.max(mx[i],b.o[i]+b.s[i]);}
  return {w:(mx[0]-mn[0])/16,h:(mx[1]-mn[1])/16,d:(mx[2]-mn[2])/16};
}
const list={
  '姜饼王(geo)': bbox(A+'/geo/gingerbread_king.geo.json'),
  '酒桶王(geo)': bbox(A+'/geo/wine_barrel_boss.geo.json'),
  '鼠群大鼠(geo)': bbox(A+'/geo/big_rat.geo.json')
};
// 屠夫: Java 模型已知 1.81 x 2.56 x 1.11; 沙耶: 玩家模型 x1.3 缩放
list['屠夫(Java模型)']={w:1.81,h:2.56,d:1.11};
list['沙耶(玩家模型x1.3)']={w:0.9*1.3,h:1.8*1.3,d:0.7*1.3};
const FIT={w:1.25,h:1.9,d:1.25};
for(const [k,v] of Object.entries(list)){
  const s=Math.min(FIT.w/v.w, FIT.h/v.h, FIT.d/v.d);
  console.log(`${k.padEnd(18)} 模型 ${v.w.toFixed(2)}x${v.h.toFixed(2)}x${v.d.toFixed(2)} 格 -> 缩放 ${s.toFixed(3)} -> 成品 ${(v.w*s).toFixed(2)}x${(v.h*s).toFixed(2)}x${(v.d*s).toFixed(2)} 格`);
}
