const fs=require('fs'),path=require('path');
const A='D:/ft工程文件/FT-SugarQueen-Boss-Mod/src/main/resources/assets/ftboss';
let n=0,bad=0,missing=[];
for(const f of fs.readdirSync(A+'/blockstates')){
  const j=JSON.parse(fs.readFileSync(A+'/blockstates/'+f,'utf8'));
  const variants=j.variants||{}; const refs=new Set();
  for(const [k,v] of Object.entries(variants)){
    const arr=Array.isArray(v)?v:[v];
    for(const e of arr) if(e.model) refs.add(e.model);
  }
  n++;
  for(const r of refs){
    const rel=r.replace(/^ftboss:block\//,'');
    if(!fs.existsSync(A+'/models/block/'+rel+'.json')) { bad++; missing.push(`${f}: 引用了不存在的模型 ${r}`); }
  }
  process.stdout.write(`${f.padEnd(28)} 变体 ${Object.keys(variants).length} 个 -> 模型 ${refs.size} 个 ${bad?'':'OK'}\n`);
}
console.log(bad? '\n❌ 缺失:\n'+missing.join('\n') : '\n✓ 所有 blockstate 引用的模型都存在');
// 顺便检查 item 模型
let ibad=0;
for(const f of fs.readdirSync(A+'/models/item')){
  const j=JSON.parse(fs.readFileSync(A+'/models/item/'+f,'utf8'));
  if(j.parent && j.parent.startsWith('ftboss:') && !fs.existsSync(A+'/models/'+j.parent.replace('ftboss:','')+'.json')){ ibad++; console.log('❌ item 模型父级缺失:',f,j.parent); }
  for(const [k,v] of Object.entries(j.textures||{})){
    if(typeof v!=='string') continue;
    if(!v.startsWith('ftboss:')) continue;
    const p=A+'/textures/'+v.replace('ftboss:','')+'.png';
    if(!fs.existsSync(p)){ ibad++; console.log('❌ item 贴图缺失:',f,v); }
  }
}
console.log(ibad? `❌ item 模型问题 ${ibad} 处` : '✓ item 模型与贴图都存在');
