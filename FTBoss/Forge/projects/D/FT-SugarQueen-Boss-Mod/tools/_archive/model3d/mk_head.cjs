const fs=require('fs');
const A='D:/ft工程文件/FT-SugarQueen-Boss-Mod/src/main/resources/assets/ftboss';
const g=JSON.parse(fs.readFileSync(A+'/geo/gingerbread_king.geo.json','utf8'));
const geo=g['minecraft:geometry'][0];
const head=geo.bones.find(b=>b.name==='head');
const out={format_version:'1.12.0','minecraft:geometry':[{description:geo.description,bones:[{name:'head',pivot:head.pivot,cubes:[head.cubes[0]]}]}]};
fs.writeFileSync('out/_head_only.geo.json',JSON.stringify(out,null,1));
console.log('头部 cube:',JSON.stringify(head.cubes[0]));
