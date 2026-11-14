const fs=require('fs'),path=require('path');
const A='D:/ft工程文件/FT-SugarQueen-Boss-Mod/src/main/resources/assets/ftboss';
const I=[1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1];
const mul=(a,b)=>{const o=new Array(16);for(let r=0;r<4;r++)for(let c=0;c<4;c++){let s=0;for(let k=0;k<4;k++)s+=a[r*4+k]*b[k*4+c];o[r*4+c]=s;}return o;};
const tr=(x,y,z)=>[1,0,0,x,0,1,0,y,0,0,1,z,0,0,0,1];
const xf=(m,p)=>[m[0]*p[0]+m[1]*p[1]+m[2]*p[2]+m[3],m[4]*p[0]+m[5]*p[1]+m[6]*p[2]+m[7],m[8]*p[0]+m[9]*p[1]+m[10]*p[2]+m[11]];
function load(file,skip){
  const g=JSON.parse(fs.readFileSync(file,'utf8'))['minecraft:geometry'][0];
  let bones=g.bones;
  if(!bones.some(b=>b.children)){const map={};for(const b of bones)map[b.name]=Object.assign({},b,{children:[]});const roots=[];for(const b of bones){if(b.parent&&map[b.parent])map[b.parent].children.push(map[b.name]);else roots.push(map[b.name]);}bones=roots;}
  const out=[];
  (function walk(bs,pm){for(const b of bs){if(skip&&skip.test(b.name))continue;const p=b.pivot||[0,0,0];
    const m2=mul(mul(pm,tr(...p)),tr(-p[0],-p[1],-p[2]));
    for(const c of (b.cubes||[])){const p0=xf(m2,c.origin),p1=xf(m2,[c.origin[0]+c.size[0],c.origin[1]+c.size[1],c.origin[2]+c.size[2]]);
      out.push({bone:b.name,o:[Math.min(p0[0],p1[0]),Math.min(p0[1],p1[1]),Math.min(p0[2],p1[2])],s:[Math.abs(p1[0]-p0[0]),Math.abs(p1[1]-p0[1]),Math.abs(p1[2]-p0[2])]});}
    if(b.children)walk(b.children,m2);}})(bones,I);
  return out;
}
const bb=cs=>{let mn=[1e9,1e9,1e9],mx=[-1e9,-1e9,-1e9];for(const c of cs)for(let i=0;i<3;i++){mn[i]=Math.min(mn[i],c.o[i]);mx[i]=Math.max(mx[i],c.o[i]+c.s[i]);}return {mn,mx};};
const ov=(a,b,k)=>Math.min(a.mx[k],b.mx[k])-Math.max(a.mn[k],b.mn[k]);
const SRC={ '姜饼王':A+'/geo/gingerbread_king.geo.json','屠夫':'out/butcher_preview.geo.json','酒桶王':A+'/geo/wine_barrel_boss.geo.json','鼠群':A+'/geo/big_rat.geo.json' };
for(const [name,f] of Object.entries(SRC)){
  const cs=load(f);
  const head=cs.filter(c=>/head/i.test(c.bone));
  const arms=cs.filter(c=>/arm|hand|claw|paw/i.test(c.bone));
  const rest=cs.filter(c=>!/head/i.test(c.bone));
  const torso=cs.filter(c=>/body|torso|chest|trunk/i.test(c.bone));
  console.log(`\n== ${name} ==  head=${head.length} arm=${arms.length} torso=${torso.length}`);
  if(head.length&&rest.length){ const H=bb(head),R=bb(rest);
    console.log(`   头 y ${H.mn[1].toFixed(1)}..${H.mx[1].toFixed(1)}  其余 y ${R.mn[1].toFixed(1)}..${R.mx[1].toFixed(1)}  y重叠=${ov(H,R,1).toFixed(2)} (头中心y=${((H.mn[1]+H.mx[1])/2).toFixed(1)} 其余中心y=${((R.mn[1]+R.mx[1])/2).toFixed(1)})`);
  }
  if(arms.length&&torso.length){ const Ar=bb(arms),T=bb(torso);
    console.log(`   臂 x ${Ar.mn[0].toFixed(1)}..${Ar.mx[0].toFixed(1)}  躯干 x ${T.mn[0].toFixed(1)}..${T.mx[0].toFixed(1)}  x重叠=${ov(Ar,T,0).toFixed(2)}  y重叠=${ov(Ar,T,1).toFixed(2)}`);
  }
}

