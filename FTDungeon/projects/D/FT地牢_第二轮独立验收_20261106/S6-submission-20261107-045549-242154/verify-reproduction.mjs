import fs from 'node:fs'
import path from 'node:path'
import {fileURLToPath} from 'node:url'
import {spawnSync} from 'node:child_process'
import {createHash} from 'node:crypto'
import assert from 'node:assert/strict'
const root=path.dirname(fileURLToPath(import.meta.url))
const runner='C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/run-build.mjs'
const hash=filename=>createHash('sha256').update(fs.readFileSync(filename)).digest('hex')
const folders=fs.readdirSync(root).filter(name=>/^\d\d-/.test(name)).sort().concat('assembly')
const reports=[]
for(const folder of folders) {
  const directory=path.join(root,folder)
  const summary=JSON.parse(fs.readFileSync(path.join(directory,'summary.json')))
  const out=path.join(directory,'qa/reproduced')
  const args=[runner,path.join(directory,'recipe.mjs'),'--out-dir',out,'--data-version','3465']
  const result=spawnSync(process.execPath,args,{encoding:'utf8',windowsHide:true})
  fs.writeFileSync(path.join(directory,'qa/reproduction.log'),result.stdout+result.stderr)
  assert.equal(result.status,0,folder+' reproduction failed')
  const reproduced=path.join(out,path.basename(summary.nbtPath))
  assert.equal(hash(reproduced),hash(summary.nbtPath),folder+' bytes differ')
  reports.push({folder,source_sha256:hash(summary.nbtPath),reproduced_sha256:hash(reproduced),byte_equal:true,command:[process.execPath,...args]})
}
const references=JSON.parse(fs.readFileSync(path.join(root,'references.json')))
const referenceReport=references.references.map(reference=>{
  const actual=hash(reference.image_path)
  assert.equal(actual,reference.sha256)
  return {id:reference.id,image_path:reference.image_path,image_sha256:actual,prompt_path:reference.prompt_path,prompt_sha256:hash(reference.prompt_path)}
})
fs.writeFileSync(path.join(root,'qa/reproduction.json'),JSON.stringify({scope:'Fresh runner export compared byte-for-byte to final candidates; assembly uses current reviewed module hashes',reports,references:referenceReport,independent_acceptance:false},null,2))
console.log('Verified 10 room recipes, connected assembly, 10 image hashes and prompt files')
