'use strict';
const fs=require('fs'),path=require('path');
const {Canvas}=require('./png.cjs');
const root=path.resolve(__dirname,'../..');
const geoPath=path.join(root,'src/main/resources/assets/ftboss/geo/gourmet_blank_blade.geo.json');
const outPath=path.join(root,'art/gourmet_blank_blade_preview.png');
const geo=JSON.parse(fs.readFileSync(geoPath,'utf8'))['minecraft:geometry'][0];
const W=2048,H=1100,c=new Canvas(W,H),TILE=128;
for(let y=0;y<H;y++)for(let x=0;x<W;x++){
  const panel=Math.floor(x/(W/4)),cx=(panel+.5)*(W/4),d=Math.hypot((x-cx)/(W/4),(y-H*.5)/H);
  const v=Math.max(0,1-d*1.5),stageWarm=panel*.8;c.set(x,y,7+9*v+stageWarm,5+7*v,18+28*v,1);
}
const palette={
  '0,0':[28,20,37],'128,0':[108,48,148],'256,0':[213,153,49],'384,0':[78,42,57],
  '0,128':[139,116,166],'128,128':[226,218,237],'256,128':[173,70,219],'384,128':[241,233,246],
  '0,256':[32,27,41],'128,256':[174,99,204],'256,256':[157,96,31],'384,256':[232,43,34],
  '0,384':[69,43,42],'128,384':[116,43,151],'256,384':[145,88,24],'384,384':[67,25,88]
};
const emissive=new Set(['256,0','256,128','128,256','384,256']);
function materialOf(q){const uv=q.uv.north.uv,key=`${Math.floor(uv[0]/TILE)*TILE},${Math.floor(uv[1]/TILE)*TILE}`;return{color:palette[key]||[128,80,160],glow:emissive.has(key)}}
function rotate(p,rot,piv){if(!rot)return p;let[x,y,z]=p,[px,py,pz]=piv||[0,0,0];x-=px;y-=py;z-=pz;let a=(rot[2]||0)*Math.PI/180,nx=x*Math.cos(a)-y*Math.sin(a),ny=x*Math.sin(a)+y*Math.cos(a);x=nx;y=ny;a=(rot[1]||0)*Math.PI/180;nx=x*Math.cos(a)+z*Math.sin(a);let nz=-x*Math.sin(a)+z*Math.cos(a);x=nx;z=nz;a=(rot[0]||0)*Math.PI/180;ny=y*Math.cos(a)-z*Math.sin(a);nz=y*Math.sin(a)+z*Math.cos(a);return[x+px,y+py,z+pz]}
function requiredStage(name){if(name.startsWith('socket_stage_30'))return 30;if(name.startsWith('socket_stage_15'))return 15;if(name.startsWith('socket_stage_01'))return 1;return 0}
const cubes=[];
for(const b of geo.bones){
  if(b.name==='blade_backing'||b.name.startsWith('socket_fill_')||b.name.startsWith('flow_light_')||/^sigil_\d+$/.test(b.name))continue;
  for(const q of b.cubes||[]){
    const[x,y,z]=q.origin,[sx,sy,sz]=q.size,piv=q.pivot||[x+sx/2,y+sy/2,z+sz/2];
    const p=[[x,y,z],[x+sx,y,z],[x+sx,y+sy,z],[x,y+sy,z],[x,y,z+sz],[x+sx,y,z+sz],[x+sx,y+sy,z+sz],[x,y+sy,z+sz]].map(v=>rotate(v,q.rotation,piv));
    cubes.push({bone:b.name,required:requiredStage(b.name),p,mat:materialOf(q)});
  }
}
const atStage=stage=>cubes.filter(q=>q.required<=stage);
function shade(col,f){return col.map(v=>Math.max(0,Math.min(255,Math.round(v*f))))}
function drawPoly(points,col,a=.98,glow=false){
  if(glow){const center=points.reduce((s,p)=>[s[0]+p[0]/points.length,s[1]+p[1]/points.length],[0,0]);for(let k=16;k>=5;k-=3){const ex=points.map(p=>[Math.round(center[0]+(p[0]-center[0])*(1+k/100)),Math.round(center[1]+(p[1]-center[1])*(1+k/100))]);c.quad(ex,...col,.055)}}
  c.quad(points,...col,a);
}
function drawFlat(backView,ox,stage){
  const scale=12.8,oy=675,items=atStage(stage).sort((a,b)=>{const za=a.p.reduce((n,p)=>n+p[2],0)/8,zb=b.p.reduce((n,p)=>n+p[2],0)/8;return backView?za-zb:zb-za});
  for(const s of items){const ids=backView?[5,4,7,6]:[0,1,2,3],pts=ids.map(i=>[Math.round(ox+(backView?-s.p[i][0]:s.p[i][0])*scale),Math.round(oy-s.p[i][1]*scale)]);drawPoly(pts,s.mat.color,.98,s.mat.glow)}
}
function drawOblique(ox,stage){
  const scale=11.8,oy=675,faces=[],defs=[[[0,1,2,3],1.0],[[5,4,7,6],.9],[[1,5,6,2],.72],[[4,0,3,7],.82],[[3,2,6,7],1.08],[[4,5,1,0],.62]];
  const depth=p=>p[2]*.86-p[0]*.42;
  const project=p=>[Math.round(ox+(p[0]*.88+p[2]*.62)*scale),Math.round(oy+(-p[1]+p[2]*.16)*scale)];
  for(const s of atStage(stage))for(const[ids,light]of defs){const ps=ids.map(i=>s.p[i]);faces.push({depth:ps.reduce((n,p)=>n+depth(p),0)/4,pts:ps.map(project),col:shade(s.mat.color,light),glow:s.mat.glow})}
  faces.sort((a,b)=>a.depth-b.depth);for(const f of faces)drawPoly(f.pts,f.col,.97,f.glow);
}
drawFlat(false,256,0);drawFlat(false,768,1);drawOblique(1280,15);drawOblique(1792,30);
fs.mkdirSync(path.dirname(outPath),{recursive:true});c.save(outPath);
console.log(`Rendered ${outPath}: cumulative 0, 1, 15 and 30-socket states from ${cubes.length} non-fill cubes.`);