const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const childProcess = require('node:child_process');
const assert = require('node:assert/strict');
const root = path.resolve(__dirname,'../../../..');
const sha = file => crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex');
const run = args => childProcess.execFileSync(process.execPath,args,{cwd:root,encoding:'utf8'});
for(const [type,identifier,texture,work] of [
  ['boss_materials','tide_material','tide','boss-materials-refine-20261021'],
  ['boss_relics','tide_pearl_pendant','tide_pearl_pendant','boss-relics-refine-20261021']
]) {
  const tools='tools/model3d/'+type+'_refine/tide/';
  const base='src/main/resources/assets/ftboss/';
  const files=[base+'models/item/'+identifier+'.json',base+'models/item/'+type+'/'+identifier+'.json',base+'models/item/'+type+'/'+identifier+'_gui.json',base+'textures/item/'+identifier+'.png',base+'textures/item/'+type+'/'+texture+'.png','art/'+type+'/blockbench/'+identifier+'.bbmodel'];
  const before=files.map(file=>({path:file,sha256:sha(path.join(root,file))}));
  const generation=run([tools+'generate.cjs']);
  const after=files.map(file=>({path:file,sha256:sha(path.join(root,file))}));
  assert.deepEqual(before,after,'reproducible generation '+identifier);
  const checks=[generation,run([tools+'verify.cjs']),run(['tools/model3d/lint_java_model.cjs',base+'models/item/'+type+'/'+identifier+'.json',base+'models/item/'+type+'/'+identifier+'_gui.json']),run(['tools/model3d/check_uv_coverage.cjs',base+'models/item/'+type+'/'+identifier+'.json',base+'textures/item/'+type+'/'+texture+'.png']),run([tools+'render.cjs'])];
  const directory=path.join(root,'_work',work,'tide');
  fs.writeFileSync(path.join(directory,'final-checks.log'),checks.join('\n'));
  const previewDirectory=path.join(root,'art',type,'refine/tide');
  const previews=fs.readdirSync(previewDirectory).filter(file=>file.endsWith('.png')&&!file.startsWith('reference_')).map(file=>({path:path.join(previewDirectory,file).replaceAll('\\','/'),sha256:sha(path.join(previewDirectory,file))}));
  fs.writeFileSync(path.join(directory,'reproducibility.json'),JSON.stringify({status:'PASS',repeatGenerationIdentical:true,assets:after,previews},null,2)+'\n');
  console.log(identifier+': final assets, repeat generation, UV, geometry, source parity and previews PASS');
}
