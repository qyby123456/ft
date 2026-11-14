const fs=require('fs');
const A='D:/ft工程文件/FT-SugarQueen-Boss-Mod/src/main/resources/assets/ftboss';
const g=JSON.parse(fs.readFileSync(A+'/geo/gingerbread_king.geo.json','utf8'))['minecraft:geometry'][0];
console.log('== 源 geo 的 cube(box-UV) ==');
let i=0;
for(const b of g.bones) for(const c of (b.cubes||[])){
  const [u,v]=c.uv,[w,h,d]=c.size;
  console.log(`${(i+++'').padStart(2)} ${b.name.padEnd(8)} o=${c.origin.join(',')} s=${c.size.join(',')} uv=${u},${v}  north=[${u+d},${v+d},${u+d+w},${v+d+h}] south=[${u+d+w+d},${v+d},${2*d+2*w+u},${v+d+h}]`);
}
console.log('== 我生成的 lower_painted 前 6 个 element ==');
const m=JSON.parse(fs.readFileSync('out_statues_real/models/block/statue_sugar_queen_lower_painted.json','utf8'));
m.elements.slice(0,6).forEach((e,k)=>console.log(`${k} from=${e.from.join(',')} to=${e.to.join(',')} north=${e.faces.north.uv.join(',')} up=${e.faces.up.uv.join(',')}`));
