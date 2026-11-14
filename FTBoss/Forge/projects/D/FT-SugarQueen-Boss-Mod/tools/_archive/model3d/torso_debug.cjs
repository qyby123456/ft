const fs=require('fs');
const m=JSON.parse(fs.readFileSync('out_statues_real/models/block/statue_sugar_queen_lower_painted.json','utf8'));
// 只留躯干那一个 element
const one={textures:m.textures,elements:[m.elements[0]]};
fs.writeFileSync('out_statues_real/models/block/_test_torso.json',JSON.stringify(one,null,1));
console.log('躯干 element:', JSON.stringify(m.elements[0]));
// 也把"胸口高度上所有元素"导出来看谁在最前面
const els=m.elements.filter(e=>e.from[1]<14&&e.to[1]>8&&e.from[2]<7.0);
console.log('胸口最前面的元素(z<7):');
for(const e of els) console.log('  z='+e.from[2].toFixed(2)+'..'+e.to[2].toFixed(2)+' x='+e.from[0].toFixed(1)+'..'+e.to[0].toFixed(1)+' y='+e.from[1].toFixed(1)+'..'+e.to[1].toFixed(1)+' northUV='+e.faces.north.uv.join(','));
