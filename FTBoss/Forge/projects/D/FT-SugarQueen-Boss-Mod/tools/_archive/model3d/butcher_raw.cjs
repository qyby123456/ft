const fs=require('fs');
const g=JSON.parse(fs.readFileSync('out/butcher_preview.geo.json','utf8'))['minecraft:geometry'][0];
console.log('identifier=',g.description.identifier);
(function walk(bs,d){for(const b of bs){console.log('  '.repeat(d)+`${b.name} pivot=${JSON.stringify(b.pivot)} rot=${JSON.stringify(b.rotation)}`);
  for(const c of (b.cubes||[])) console.log('  '.repeat(d+1)+`cube o=${c.origin.join(',')} s=${c.size.join(',')} uv=${JSON.stringify(c.uv)}`);
  if(b.children)walk(b.children,d+1);}})(g.bones,0);
