const fs=require('fs');
const src=process.argv[2], out=process.argv[3];
const j=JSON.parse(fs.readFileSync(src,'utf8'));
let n=0;
for(const t of (j.textures||[])){
  const d=(t.layers&&t.layers[0]&&t.layers[0].data_url)||t.source;
  if(!d) continue;
  const m=d.match(/^data:image\/png;base64,(.*)$/s);
  if(!m) continue;
  const f=out.replace('%d',n===0?'':n);
  fs.writeFileSync(f, Buffer.from(m[1],'base64'));
  console.log('wrote', f, 'from', t.name, t.width+'x'+t.height);
  n++;
}
if(n===0) console.log('no inline png found');
