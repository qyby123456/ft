'use strict';
const fs=require('fs'),path=require('path');
const {Canvas}=require('./png.cjs');
const root=path.resolve(__dirname,'../..');
const out=(...p)=>path.join(root,'src/main/resources/assets/ftboss',...p);
const writeJson=(p,v)=>{fs.mkdirSync(path.dirname(p),{recursive:true});fs.writeFileSync(p,JSON.stringify(v,null,2)+'\n')};

// 512x512 atlas: a restrained royal-purple and antique-gold odachi palette.
const TILE=128;
const MAT={
  NIGHT:[0,0],PURPLE:[128,0],GOLD:[256,0],LEATHER:[384,0],
  STEEL:[0,128],EDGE:[128,128],CORE:[256,128],WHITE:[384,128],
  OBSIDIAN:[0,256],RUNE:[128,256],BRONZE:[256,256],RIFT:[384,256],
  WOOD:[0,384],SILK:[128,384],GOLD_DARK:[256,384],VELVET:[384,384]
};
const round=n=>Math.round(n*1000)/1000,vec=a=>a.map(round);
function faceUv(m,s,d=4){
  const[u,v]=MAT[m],x=Math.min(56,Math.max(2,Math.round(s[0]*d))),y=Math.min(56,Math.max(2,Math.round(s[1]*d))),z=Math.min(28,Math.max(2,Math.round(s[2]*d)));
  return {north:{uv:[u+4,v+4],uv_size:[x,y]},south:{uv:[u+68,v+4],uv_size:[x,y]},east:{uv:[u+4,v+68],uv_size:[z,y]},west:{uv:[u+36,v+68],uv_size:[z,y]},up:{uv:[u+68,v+68],uv_size:[x,z]},down:{uv:[u+68,v+98],uv_size:[x,z]}};
}
function cube(o,s,m,opt={}){const c={origin:vec(o),size:vec(s),uv:faceUv(m,s,opt.density||4)};if(opt.inflate)c.inflate=opt.inflate;if(opt.rotation)c.rotation=vec(opt.rotation);if(opt.pivot)c.pivot=vec(opt.pivot);return c}
const centered=(x,y,z,sx,sy,sz,m,o={})=>cube([x-sx/2,y-sy/2,z-sz/2],[sx,sy,sz],m,o);
const bar=(x,y,z,l,t,d,a,m,i=0)=>centered(x,y,z,l,t,d,m,{rotation:[0,0,a],pivot:[x,y,z],inflate:i});
function segment(x1,y1,x2,y2,z,t,d,m,i=0){return bar((x1+x2)/2,(y1+y2)/2,z,Math.hypot(x2-x1,y2-y1),t,d,Math.atan2(y2-y1,x2-x1)*180/Math.PI,m,i)}
function segmentXZ(x1,z1,x2,z2,y,t,d,m,i=0){const x=(x1+x2)/2,z=(z1+z2)/2,a=-Math.atan2(z2-z1,x2-x1)*180/Math.PI;return centered(x,y,z,Math.hypot(x2-x1,z2-z1),t,d,m,{rotation:[0,a,0],pivot:[x,y,z],inflate:i})}
function diamondFrame(c,x,y,z,rx,ry,t,d,m){const p=[[x,y+ry],[x+rx,y],[x,y-ry],[x-rx,y]];for(let i=0;i<4;i++)c.push(segment(...p[i],...p[(i+1)%4],z,t,d,m,.006))}
function lerp(a,b,t){return[a[0]+(b[0]-a[0])*t,a[1]+(b[1]-a[1])*t]}
function offsetLine(a,b,off){const dx=b[0]-a[0],dy=b[1]-a[1],l=Math.hypot(dx,dy),nx=-dy/l,ny=dx/l;return[[a[0]+nx*off,a[1]+ny*off],[b[0]+nx*off,b[1]+ny*off]]}
const bones=[];
function bone(name,parent,pivot,cubes=[]){const b={name,pivot:vec(pivot),cubes};if(parent)b.parent=parent;bones.push(b)}
bone('root',null,[0,0,0]);
bone('weapon','root',[0,6,0]);

// Long tsuka with a real wood core, purple samegawa/ito layers and paired golden menuki.
const tsukaCore=[
  centered(0,-13.2,0,1.52,15.4,1.48,'WOOD',{inflate:.018}),
  centered(0,-13.2,0,1.28,14.9,1.72,'VELVET'),
  // A visible continuous purple shell prevents shader-darkened gaps between the ito wraps.
  centered(0,-13.2,0,1.72,15.2,1.8,'VELVET',{inflate:.016}),
  centered(0,-5.3,0,1.9,.48,1.92,'GOLD_DARK',{inflate:.022})
];
bone('tsuka_core','weapon',[0,-13.2,0],tsukaCore);
const tsukaWrap=[];
for(let i=0;i<14;i++){
  const y=-19.72+i*1.04,a=i%2?23:-23;
  tsukaWrap.push(centered(0,y,0,2.02,.48,1.94,'SILK',{rotation:[0,0,a],pivot:[0,y,0],inflate:.012}));
}
for(const side of[-1,1]){
  tsukaWrap.push(centered(0,-14.4,1.05*side,.58,1.05,.18,'GOLD',{rotation:[0,0,45],pivot:[0,-14.4,1.05*side],inflate:.01}));
  tsukaWrap.push(centered(0,-10.0,1.05*side,.48,.82,.18,'GOLD_DARK',{rotation:[0,0,45],pivot:[0,-10.0,1.05*side],inflate:.008}));
}
bone('tsuka_wrap','tsuka_core',[0,-13.2,0],tsukaWrap);
const kashira=[
  centered(0,-21.35,0,2.25,.9,2.12,'GOLD_DARK',{inflate:.025}),
  centered(0,-21.82,0,1.78,.48,1.72,'GOLD',{inflate:.018}),
  centered(0,-22.2,0,1.2,.54,1.2,'PURPLE',{rotation:[0,0,45],pivot:[0,-22.2,0]}),
  centered(0,-22.2,-.7,.5,.32,.2,'CORE',{rotation:[0,0,45],pivot:[0,-22.2,-.7]}),
  centered(0,-22.2,.7,.5,.32,.2,'CORE',{rotation:[0,0,45],pivot:[0,-22.2,.7]})
];
bone('kashira','tsuka_core',[0,-21.5,0],kashira);

// An ornate openwork tsuba. The ring and spokes live in the XZ plane, unlike a western crossguard.
const tsubaRing=[];
function ellipseRing(rx,rz,count,mat,t=.48,d=.48){for(let i=0;i<count;i++){const a=i*Math.PI*2/count,b=(i+1)*Math.PI*2/count;tsubaRing.push(segmentXZ(Math.cos(a)*rx,Math.sin(a)*rz,Math.cos(b)*rx,Math.sin(b)*rz,-4.45,t,d,mat,.012))}}
ellipseRing(4.75,3.35,28,'GOLD',.42,.4);
ellipseRing(2.0,1.35,18,'GOLD_DARK',.38,.34);
bone('tsuba_ring','weapon',[0,-4.45,0],tsubaRing);
const tsubaSpokes=[];
for(let i=0;i<18;i++){
  const a=(i+.5)*Math.PI*2/18;
  tsubaSpokes.push(segmentXZ(Math.cos(a)*1.68,Math.sin(a)*1.12,Math.cos(a)*4.25,Math.sin(a)*2.92,-4.45,.34,.34,i%2?'PURPLE':'SILK',.008));
}
for(const side of[-1,1])tsubaSpokes.push(centered(0,-4.45,2.25*side,1.0,.72,.48,'CORE',{rotation:[0,45*side,0],pivot:[0,-4.45,2.25*side]}));
bone('tsuba_spokes','tsuba_ring',[0,-4.45,0],tsubaSpokes);
// The openwork remains decorative, but a solid central guard plate keeps the hilt/blade joint readable
// after ground-view scaling, shaders and mip filtering.
const tsubaBacking=[
  centered(0,-4.45,0,3.32,.74,2.62,'GOLD_DARK',{inflate:.012}),
  centered(0,-4.45,0,2.52,.9,1.82,'PURPLE',{inflate:.014}),
  centered(0,-4.45,-1.18,2.0,.54,.3,'GOLD',{inflate:.008}),
  centered(0,-4.45,1.18,2.0,.54,.3,'GOLD',{inflate:.008})
];
bone('tsuba_backing','weapon',[0,-4.45,0],tsubaBacking);
const seppa=[
  centered(0,-5.0,0,2.72,.34,2.5,'GOLD_DARK',{inflate:.014}),
  centered(0,-4.78,0,2.25,.2,2.12,'NIGHT'),
  centered(0,-4.1,0,2.72,.34,2.5,'GOLD',{inflate:.014}),
  centered(0,-3.88,0,2.25,.2,2.12,'NIGHT')
];
bone('seppa','weapon',[0,-4.45,0],seppa);
const habaki=[
  centered(0,-2.98,0,2.9,1.82,1.92,'GOLD_DARK',{inflate:.02}),
  centered(-.14,-2.83,-1.02,2.35,1.48,.18,'GOLD',{inflate:.012}),
  centered(-.14,-2.83,1.02,2.35,1.48,.18,'GOLD',{inflate:.012}),
  centered(-1.32,-2.98,0,.3,1.72,2.02,'BRONZE'),
  centered(1.32,-2.98,0,.3,1.72,2.02,'NIGHT')
];
bone('habaki','weapon',[0,-2.9,0],habaki);
// This overlap bridge sits inside the habaki and first blade mass, so no view angle can expose a void.
const bladeRootBridge=[
  centered(0,-2.15,0,2.58,1.82,.92,'PURPLE',{inflate:.03}),
  centered(-1.18,-2.12,0,.3,1.9,1.08,'EDGE',{inflate:.018}),
  centered(1.18,-2.12,0,.3,1.9,1.04,'GOLD_DARK',{inflate:.018})
];
bone('blade_root_bridge','weapon',[0,-2.15,0],bladeRootBridge);

// A long, gently curved, single-edged odachi blade. +X is the dark spine; -X is the bright convex edge.
const BLADE_SEGMENTS=22;
const bladePath=Array.from({length:BLADE_SEGMENTS+1},(_,i)=>{
  const t=i/BLADE_SEGMENTS;
  return [2.28*(.08*t+.92*t*t),-2.1+38.85*t];
});
const bladeWidths=Array.from({length:BLADE_SEGMENTS},(_,i)=>{
  const t=i/(BLADE_SEGMENTS-1);
  return 3.12-1.05*Math.pow(t,1.05);
});
const bladeBody=[],bladeEdge=[],bladeSpine=[];
for(let i=0;i<bladePath.length-1;i++){
  const a=bladePath[i],b=bladePath[i+1],w=bladeWidths[i];
  bladeBody.push(segment(...a,...b,0,w,1.0,'PURPLE',.014));
  const[e1,e2]=offsetLine(a,b,w/2-.08),[s1,s2]=offsetLine(a,b,-w/2+.09);
  bladeEdge.push(segment(...e1,...e2,0,.36,1.2,'EDGE',.009));
  bladeSpine.push(segment(...s1,...s2,0,.34,1.14,i%2?'GOLD_DARK':'NIGHT',.008));
}
bone('blade_body','weapon',[.55,17,0],bladeBody);
// A recessed, overlap-inflated backing follows the animated blade and closes all seams between
// the 22 decorative sections without covering their front/back surface detail.
const bladeBacking=[];
for(let i=0;i<bladePath.length-1;i++){
  const a=bladePath[i],b=bladePath[i+1],w=bladeWidths[i];
  bladeBacking.push(segment(...a,...b,0,w*.92,.84,'PURPLE',.075));
}
bone('blade_backing','blade_body',[.55,17,0],bladeBacking);
bone('blade_edge','blade_body',[-1.2,17,0],bladeEdge);
bone('blade_spine','blade_body',[2.2,17,0],bladeSpine);

// Deep fuller and a double-step golden hamon are modeled on both faces instead of painted as flat icon lines.
function fuller(side){const c=[];for(let i=0;i<bladePath.length-1;i++){const a=bladePath[i],b=bladePath[i+1],w=bladeWidths[i],[f1,f2]=offsetLine(a,b,-w*.18);c.push(segment(...f1,...f2,.57*side,.22,.11,'NIGHT',.003))}return c}
function hamon(side){const c=[];for(let i=0;i<bladePath.length-1;i++){const a=bladePath[i],b=bladePath[i+1],w=bladeWidths[i];for(let h=0;h<2;h++){const p=lerp(a,b,h/2),q=lerp(a,b,(h+1)/2),wave=((i*2+h)%2?-.035:.09),[g1,g2]=offsetLine(p,q,w*.28+wave);c.push(segment(...g1,...g2,.59*side,.19,.11,h%2?'GOLD':'RUNE',.003))}}return c}
bone('blade_fuller_front','blade_body',[1,17,-.75],fuller(-1));
bone('blade_fuller_back','blade_body',[1,17,.75],fuller(1));
bone('blade_hamon_front','blade_body',[-1,18,-.76],hamon(-1));
bone('blade_hamon_back','blade_body',[-1,18,.76],hamon(1));

// The kissaki turns back toward the cutting edge and closes as one solid asymmetric point.
const tipA=bladePath.at(-1),tipPath=[tipA,[2.43,37.68],[2.48,38.55],[2.36,39.32],[2.08,40.0]],tipWidths=[1.95,1.55,1.08,.62];
const bladeTip=[];
for(let i=0;i<tipPath.length-1;i++){
  const a=tipPath[i],b=tipPath[i+1],w=tipWidths[i];
  bladeTip.push(segment(...a,...b,0,w,.96-i*.045,i<2?'PURPLE':'STEEL',.01));
  bladeTip.push(segment(...offsetLine(a,b,w/2-.05).flat(),0,.3,1.12-i*.045,'EDGE',.006));
  bladeTip.push(segment(...offsetLine(a,b,-w/2+.06).flat(),0,.28,1.06-i*.045,'GOLD_DARK',.005));
}
const tipEnd=tipPath.at(-1);
bladeTip.push(centered(tipEnd[0],tipEnd[1],0,.34,.5,.82,'EDGE',{rotation:[0,0,35],pivot:[tipEnd[0],tipEnd[1],0],inflate:.004}));
bone('blade_tip','blade_body',[2.4,38.5,0],bladeTip);

// Twelve paired emissive packets chase from habaki to kissaki during each attack.
// Their idle scale is zero; the attack clip reveals overlapping packets in sequence,
// producing a continuous purple-gold glint that visibly travels along both blade faces.
const FLOW_LIGHT_STEPS=12,flowLightNames=[];
function bladePointAt(t){
  const p=Math.max(0,Math.min(1,t))*BLADE_SEGMENTS,i=Math.min(BLADE_SEGMENTS-1,Math.floor(p));
  return lerp(bladePath[i],bladePath[i+1],p-i);
}
function bladeWidthAt(t){
  const p=Math.max(0,Math.min(1,t))*(BLADE_SEGMENTS-1),i=Math.min(BLADE_SEGMENTS-2,Math.floor(p));
  return bladeWidths[i]+(bladeWidths[i+1]-bladeWidths[i])*(p-i);
}
for(let i=0;i<FLOW_LIGHT_STEPS;i++){
  const centerT=.04+i*.92/(FLOW_LIGHT_STEPS-1),a=bladePointAt(centerT-.038),b=bladePointAt(centerT+.038);
  const width=bladeWidthAt(centerT),[f1,f2]=offsetLine(a,b,width*.27),name='flow_light_'+String(i+1).padStart(2,'0');
  const pivot=[(f1[0]+f2[0])/2,(f1[1]+f2[1])/2,0],c=[];
  for(const side of[-1,1]){
    c.push(segment(...f1,...f2,.665*side,.3,.08,'CORE',.005));
    c.push(segment(...f1,...f2,.72*side,.105,.06,'GOLD',.008));
  }
  flowLightNames.push(name);
  bone(name,'blade_body',pivot,c);
}

// Paired imperial-mon style inlays give the lower blade a deliberate focal ornament.
function bladeOrnament(side){const c=[],z=.78*side,x=.06,y=2.15;diamondFrame(c,x,y,z,.86,1.28,.18,.14,'GOLD');for(const p of[[-1.45,y],[1.55,y],[x,y-2],[x,y+2.05]])c.push(segment(x,y,...p,z,.16,.13,'RUNE',.004));return c}
bone('blade_ornament_front','blade_body',[0,2,-.8],bladeOrnament(-1));
bone('blade_ornament_back','blade_body',[0,2,.8],bladeOrnament(1));

// A purple crystal menuki remains the animation focus; the tsuba and gold inlay frame it.
const core=[
  centered(0,-12.15,-1.08,.86,1.5,.2,'GOLD_DARK',{rotation:[0,0,45],pivot:[0,-12.15,-1.08]}),
  centered(0,-12.15,1.08,.86,1.5,.2,'GOLD_DARK',{rotation:[0,0,45],pivot:[0,-12.15,1.08]}),
  centered(0,-12.15,-1.2,.4,.72,.12,'CORE',{rotation:[0,0,45],pivot:[0,-12.15,-1.2]}),
  centered(0,-12.15,1.2,.4,.72,.12,'CORE',{rotation:[0,0,45],pivot:[0,-12.15,1.2]})
];
bone('core','tsuka_core',[0,-12.15,0],core);

function xOnPath(y){for(let i=0;i<bladePath.length-1;i++){const a=bladePath[i],b=bladePath[i+1];if(y>=a[1]&&y<=b[1])return a[0]+(b[0]-a[0])*(y-a[1])/(b[1]-a[1]);}return bladePath.at(-1)[0]}
const socketFrames=[],socketPositions=[];
for(let i=0;i<30;i++){
  const y=.15+i*1.16,x=xOnPath(y)+(i%2?.7:.16);
  socketPositions.push([x,y]);
  for(const side of[-1,1])socketFrames.push(centered(x,y,.79*side,.22,.46,.12,'GOLD_DARK',{rotation:[0,0,i%2?26:-26],pivot:[x,y,.79*side]}));
}
bone('socket_frames','blade_body',[.8,16,0],socketFrames);
bone('socket_energy','weapon',[0,10,0]);
socketPositions.forEach(([x,y],i)=>bone('socket_fill_'+String(i+1).padStart(2,'0'),'socket_energy',[x,y,0],[
  centered(x,y,-.87,.15,.34,.1,'WHITE',{rotation:[0,0,i%2?26:-26],pivot:[x,y,-.87]}),
  centered(x,y,.87,.15,.34,.1,'WHITE',{rotation:[0,0,i%2?-26:26],pivot:[x,y,.87]})
]));

// Three cumulative socket transformations. The base odachi remains restrained; each milestone adds real geometry.
function faceSeam(c,points,width=.16){
  for(const side of[-1,1])for(let i=0;i<points.length-1;i++)c.push(segment(...points[i],...points[i+1],.64*side,width,.12,'RIFT',.006));
}
const stage01=[];
faceSeam(stage01,[[-.34,4.9],[-.04,6.15],[-.24,7.35],[.18,8.55]],.14);
faceSeam(stage01,[[.02,6.15],[.58,6.8],[.73,7.65]],.11);
for(const side of[-1,1]){
  stage01.push(centered(0,-4.45,3.02*side,.52,.46,.22,'RIFT',{rotation:[0,45*side,0],pivot:[0,-4.45,3.02*side],inflate:.01}));
  stage01.push(centered(0,-12.15,1.32*side,.24,.42,.1,'RIFT',{rotation:[0,0,45],pivot:[0,-12.15,1.32*side],inflate:.006}));
}
bone('socket_stage_01','socket_energy',[0,8,0],stage01);

function addStageBlade(c,pathPoints,widths,bodyMaterial){
  for(let i=0;i<pathPoints.length-1;i++){
    const a=pathPoints[i],b=pathPoints[i+1],w=widths[i];
    c.push(segment(...a,...b,0,w,.99-i*.025,bodyMaterial,.016));
    c.push(segment(...offsetLine(a,b,w/2-.05).flat(),0,.31,1.14-i*.025,'EDGE',.009));
    c.push(segment(...offsetLine(a,b,-w/2+.06).flat(),0,.3,1.08-i*.025,i%2?'GOLD_DARK':'NIGHT',.008));
  }
}
const stage15=[];
const stage15Path=[[2.08,36.75],[2.31,38.15],[2.55,39.55],[2.82,40.9],[3.13,42.2],[3.48,43.42]];
addStageBlade(stage15,stage15Path,[1.92,1.73,1.5,1.2,.78],'PURPLE');
stage15.push(centered(3.51,43.62,0,.4,.62,.78,'EDGE',{rotation:[0,0,27],pivot:[3.51,43.62,0],inflate:.006}));
faceSeam(stage15,[[1.45,27.4],[1.08,29.1],[1.72,30.4],[1.34,32.15],[2.0,33.55]],.17);
faceSeam(stage15,[[1.1,29.05],[.58,29.72],[.47,30.82]],.12);
faceSeam(stage15,[[2.07,36.95],[2.56,38.45],[2.25,39.7],[3.0,41.1],[2.82,42.2]],.18);
bone('socket_stage_15','socket_energy',[2.5,38.5,0],stage15);

const stage30=[];
const stage30Path=[[3.16,41.75],[3.45,43.05],[3.76,44.28],[4.08,45.42],[4.39,46.45],[4.67,47.32]];
addStageBlade(stage30,stage30Path,[1.33,1.14,.94,.7,.42],'OBSIDIAN');
for(let i=0;i<stage30Path.length-1;i++){
  const a=stage30Path[i],b=stage30Path[i+1];
  for(const side of[-1,1])stage30.push(segment(...a,...b,.53*side,.24,.13,'RIFT',.008));
}
stage30.push(centered(4.72,47.52,0,.34,.58,.72,'RIFT',{rotation:[0,0,24],pivot:[4.72,47.52,0],inflate:.008}));
faceSeam(stage30,[[.72,18.1],[1.38,19.5],[.95,21.05],[1.65,22.6],[1.27,24.3],[2.05,25.8]],.19);
faceSeam(stage30,[[1.0,21.0],[.35,21.85],[.26,23.05]],.12);
faceSeam(stage30,[[2.26,34.2],[1.73,35.55],[2.54,36.7],[2.12,38.05],[3.0,39.2]],.2);
for(const shard of[[3.25,34.6,.72,1.42,-28],[3.74,38.2,.66,1.28,31],[4.18,41.0,.58,1.12,-32]]){
  const[x,y,w,h,a]=shard;
  stage30.push(centered(x,y,0,w,h,.82,'PURPLE',{rotation:[0,0,a],pivot:[x,y,0],inflate:.012}));
  for(const side of[-1,1])stage30.push(centered(x,y,.48*side,w*.38,h*.72,.12,'RIFT',{rotation:[0,0,a],pivot:[x,y,.48*side],inflate:.006}));
}
bone('socket_stage_30','socket_energy',[3.6,42.5,0],stage30);

bone('sigil_orbit','weapon',[0,10,0]);
const sigilPositions=[[-.12,-18.4],[.12,-16.1],[-.1,-8.6],[.1,-6.65],[.34,7.0],[.58,14.3],[1.02,22.2],[1.52,29.7]];
sigilPositions.forEach(([x,y],i)=>bone('sigil_'+(i+1),'sigil_orbit',[x,y,0],[
  centered(x,y,-.9,.42,.7,.14,'GOLD',{rotation:[0,0,45],pivot:[x,y,-.9],inflate:.006}),
  centered(x,y,.9,.42,.7,.14,'GOLD',{rotation:[0,0,-45],pivot:[x,y,.9],inflate:.006})
]));

const geo={format_version:'1.12.0','minecraft:geometry':[{description:{identifier:'geometry.gourmet_blank_blade',texture_width:512,texture_height:512,visible_bounds_width:5.8,visible_bounds_height:7.8,visible_bounds_offset:[0,.5,0]},bones}]};
const pulse={'0.0':[.97,.97,.97],'1.25':[1.06,1.06,1.06],'2.5':[.98,.98,.98],'3.75':[1.045,1.045,1.045],'5.0':[.97,.97,.97]};
const hiddenFlow={scale:{'0.0':[0,0,0],'5.0':[0,0,0]}};
const idleBones={
  weapon:{rotation:{'0.0':[0,0,-.35],'2.5':[0,0,.35],'5.0':[0,0,-.35]}},
  core:{scale:pulse},
  blade_hamon_front:{scale:{'0.0':[1,1,1],'2.5':[1.01,1.015,1.01],'5.0':[1,1,1]}},
  blade_hamon_back:{scale:{'0.0':[1,1,1],'2.5':[1.01,1.015,1.01],'5.0':[1,1,1]}},
  tsuba_spokes:{rotation:{'0.0':[0,-.7,0],'2.5':[0,.7,0],'5.0':[0,-.7,0]}},
  socket_energy:{scale:{'0.0':[.99,.99,.99],'2.5':[1.025,1.025,1.025],'5.0':[.99,.99,.99]}},
  sigil_orbit:{scale:{'0.0':[.98,.98,.98],'2.5':[1.035,1.035,1.035],'5.0':[.98,.98,.98]}}
};
for(const name of flowLightNames)idleBones[name]=hiddenFlow;
const attackBones={
  blade_body:{scale:{'0.0':[1,1,1],'.2':[1.012,1.018,1.012],'.62':[1,1,1]}}
};
flowLightNames.forEach((name,i)=>{
  const start=.045+i*.031,key=n=>String(round(n));
  attackBones[name]={scale:{
    '0.0':[0,0,0],
    [key(start)]:[0,0,0],
    [key(start+.035)]:[1.45,1.12,1.45],
    [key(start+.095)]:[.82,.94,.82],
    [key(start+.17)]:[0,0,0],
    '.62':[0,0,0]
  }};
});
const evolveBones={
  weapon:{rotation:{'0.0':[0,0,0],'.45':[0,180,2],'1.05':[0,540,-2],'1.6':[0,720,0]},scale:{'0.0':[1,1,1],'.35':[1.06,1.06,1.06],'.85':[.97,1.04,.97],'1.6':[1,1,1]}},
  blade_body:{scale:{'0.0':[1,1,1],'.42':[1.025,1.07,1.025],'1.6':[1,1,1]}},
  tsuba_ring:{scale:{'0.0':[1,1,1],'.42':[1.14,1.03,1.14],'1.6':[1,1,1]}},
  core:{scale:{'0.0':[1,1,1],'.3':[1.7,1.7,1.7],'.9':[1.18,1.18,1.18],'1.6':[1,1,1]}},
  sigil_orbit:{scale:{'0.0':[.7,.7,.7],'.5':[1.3,1.3,1.3],'1.6':[1,1,1]}},
  socket_energy:{scale:{'0.0':[.7,.7,.7],'.45':[1.28,1.28,1.28],'1.6':[1,1,1]}}
};
const socketBones={
  socket_energy:{scale:{'0.0':[1,1,1],'.18':[1.38,1.38,1.38],'.42':[.9,.9,.9],'.8':[1,1,1]}},
  core:{scale:{'0.0':[1,1,1],'.22':[1.32,1.32,1.32],'.8':[1,1,1]}},
  tsuba_spokes:{rotation:{'0.0':[0,0,0],'.22':[0,9,0],'.8':[0,0,0]}}
};
for(const name of flowLightNames){
  evolveBones[name]={scale:{'0.0':[0,0,0],'1.6':[0,0,0]}};
  socketBones[name]={scale:{'0.0':[0,0,0],'.8':[0,0,0]}};
}
const animations={format_version:'1.8.0',animations:{
  'animation.gourmet_blank_blade.idle':{loop:true,animation_length:5,bones:idleBones},
  'animation.gourmet_blank_blade.evolve':{loop:false,animation_length:1.6,bones:evolveBones},
  'animation.gourmet_blank_blade.socket':{loop:false,animation_length:.8,bones:socketBones},
  'animation.gourmet_blank_blade.attack':{loop:false,animation_length:.62,bones:attackBones}
}};
const model={parent:'builtin/entity',gui_light:'front',textures:{particle:'ftboss:item/gourmet_blank_blade'},display:{thirdperson_righthand:{rotation:[0,90,-5],translation:[0,1.3,0],scale:[.54,.54,.54]},thirdperson_lefthand:{rotation:[0,-90,5],translation:[0,1.3,0],scale:[.54,.54,.54]},firstperson_righthand:{rotation:[0,88,18],translation:[1.4,-.2,-1.1],scale:[.44,.44,.44]},firstperson_lefthand:{rotation:[0,-88,-18],translation:[1.4,-.2,-1.1],scale:[.44,.44,.44]},gui:{rotation:[8,25,-25],translation:[0,-.8,0],scale:[.27,.27,.27]},ground:{rotation:[0,0,74],translation:[0,2.1,0],scale:[.23,.23,.23]},fixed:{rotation:[0,0,-22],translation:[0,-.6,0],scale:[.29,.29,.29]}}};
writeJson(out('geo','gourmet_blank_blade.geo.json'),geo);writeJson(out('animations','gourmet_blank_blade.animation.json'),animations);writeJson(out('models','item','gourmet_blank_blade.json'),model);

const tex=new Canvas(512,512);
const clamp=n=>Math.max(0,Math.min(255,n));
function tile(tx,ty,base,shine,mode){
  for(let y=0;y<TILE;y++)for(let x=0;x<TILE;x++){
    const grain=(((x*29+y*43+(x^y)*11)%31)-15)*(mode==='metal'?.42:mode==='silk'?.28:.5);
    const broad=Math.max(0,1-Math.abs((x%64)-21)/34);
    const diagonal=Math.max(0,1-Math.abs(((x+y*2)%96)-38)/52);
    const weave=mode==='silk'?((((x>>2)+(y>>2))%2)*5-2.5):0;
    const hammered=mode==='gold'?((((x>>3)*7+(y>>3)*13)%11)-5)*1.1:0;
    const sheen=(mode==='metal'||mode==='gold')?broad*.68+diagonal*.18:mode==='silk'?diagonal*.34:.08*broad;
    const r=base[0]+(shine[0]-base[0])*sheen+grain+weave+hammered;
    const g=base[1]+(shine[1]-base[1])*sheen+grain+weave+hammered;
    const b=base[2]+(shine[2]-base[2])*sheen+grain+weave+hammered;
    tex.set(tx+x,ty+y,clamp(r),clamp(g),clamp(b),1);
  }
}
tile(0,0,[18,12,27],[54,40,69],'stone');
tile(128,0,[62,24,91],[171,92,211],'metal');
tile(256,0,[145,86,20],[255,215,94],'gold');
tile(384,0,[50,27,38],[117,64,82],'woven');
tile(0,128,[91,72,119],[198,174,222],'metal');
tile(128,128,[186,174,210],[255,249,255],'metal');
tile(256,128,[112,40,166],[228,135,255],'metal');
tile(384,128,[226,214,234],[255,252,255],'metal');
tile(0,256,[20,17,29],[57,48,70],'stone');
tile(128,256,[122,64,155],[235,174,255],'metal');
tile(256,256,[107,60,22],[205,146,55],'gold');
tile(384,256,[62,7,19],[255,68,42],'metal');
tile(0,384,[47,30,35],[103,67,54],'woven');
tile(128,384,[73,24,105],[181,89,218],'silk');
tile(256,384,[90,53,17],[193,126,38],'gold');
tile(384,384,[52,24,69],[133,64,164],'silk');
tex.save(out('textures','item','gourmet_blank_blade.png'));
const glow=new Canvas(512,512);
function glowTile(tx,ty,color,a){for(let y=0;y<TILE;y++)for(let x=0;x<TILE;x++){const p=.78+((x*3+y*5)%17)/70;glow.set(tx+x,ty+y,color[0]*p,color[1]*p,color[2]*p,a)}}
glowTile(256,0,[255,205,89],.38);
glowTile(256,128,[211,103,255],.55);
glowTile(128,256,[221,144,255],.3);
glowTile(256,256,[232,166,65],.2);
glowTile(384,256,[255,42,28],.72);
glowTile(256,384,[231,158,56],.18);
glow.save(out('textures','item','gourmet_blank_blade_glowmask.png'));
const cubeCount=bones.reduce((n,b)=>n+(b.cubes?.length||0),0);
console.log(`Generated three-stage royal purple-gold long odachi: ${bones.length} bones, ${cubeCount} cubes, continuous hilt/guard/blade backing, 22-section curved single edge, fine openwork tsuba, 30 paired sockets, 8 paired evolution crests, red emissive rifts at 1/15/30 sockets.`);
