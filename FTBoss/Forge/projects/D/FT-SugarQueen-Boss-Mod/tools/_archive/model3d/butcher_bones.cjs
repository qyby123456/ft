const fs=require('fs');
const g=JSON.parse(fs.readFileSync('out/butcher_preview.geo.json','utf8'))['minecraft:geometry'][0];
const I=[1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1];
const mul=(a,b)=>{const o=new Array(16);for(let r=0;r<4;r++)for(let c=0;c<4;c++){let s=0;for(let k=0;k<4;k++)s+=a[r*4+k]*b[k*4+c];o[r*4+c]=s;}return o;};
const tr=(x,y,z)=>[1,0,0,x,0,1,0,y,0,0,1,z,0,0,0,1];
const xf=(m,p)=>[m[0]*p[0]+m[1]*p[1]+m[2]*p[2]+m[3],m[4]*p[0]+m[5]*p[1]+m[6]*p[2]+m[7],m[8]*p[0]+m[9]*p[1]+m[10]*p[2]+m[11]];
let bones=g.bones;
if(!bones.some(b=>b.children)){const map={};for(const b of bones)map[b.name]=Object.assign({},b,{children:[]});const roots=[];for(const b of bones){if(b.parent&&map[b.parent])map[b.parent].children.push(map[b.name]);else roots.push(map[b.name]);}bones=roots;}
const perBone={};
(function walk(bs,pm){for(const b of bs){const p=b.pivot||[0,0,0];const m=mul(pm,tr(...p));m; const m2=mul(mul(pm,tr(...p)),tr(-p[0],-p[1],-p[2]));
  for(const c of (b.cubes||[])){const p0=xf(m2,c.origin),p1=xf(m2,[c.origin[0]+c.size[0],c.origin[1]+c.size[1],c.origin[2]+c.size[2]]);
    const o=[Math.min(p0[0],p1[0]),Math.min(p0[1],p1[1]),Math.min(p0[2],p1[2])],s=[Math.abs(p1[0]-p0[0]),Math.abs(p1[1]-p0[1]),Math.abs(p1[2]-p0[2])];
    (perBone[b.name]=perBone[b.name]||[]).push({o,s});}
  if(b.children)walk(b.children,m);}})(bones,I);
// Java 模型 y 向下(脚底 y=24): 为了看"头 vs 身体"的关系, 直接打印 y 区间(数值越小=越高)
for(const [name,cs] of Object.entries(perBone)){
  let mn=[1e9,1e9,1e9],mx=[-1e9,-1e9,-1e9];
  for(const c of cs)for(let i=0;i<3;i++){mn[i]=Math.min(mn[i],c.o[i]);mx[i]=Math.max(mx[i],c.o[i]+c.s[i]);}
  console.log(`${name.padEnd(7)} cube=${cs.length}  x ${mn[0].toFixed(1)}..${mx[0].toFixed(1)}  y ${mn[1].toFixed(1)}..${mx[1].toFixed(1)}  z ${mn[2].toFixed(1)}..${mx[2].toFixed(1)}`);
}
