import fs from 'node:fs'
import path from 'node:path'
import {fileURLToPath} from 'node:url'
import {createHash} from 'node:crypto'
import {spawnSync} from 'node:child_process'
import assert from 'node:assert/strict'
const root=path.dirname(fileURLToPath(import.meta.url))
const workspace=path.join(root,'independent-reproduction-assembly')
const input=path.join(workspace,'input')
const output=path.join(workspace,'output')
const hash=filename=>createHash('sha256').update(fs.readFileSync(filename)).digest('hex')
const files=[]
const folders=fs.readdirSync(root).filter(folder=>/^\d\d-/.test(folder))
for(const folder of [...folders,'assembly']) {
  const directory=path.join(input,folder)
  fs.mkdirSync(path.join(directory,'qa'),{recursive:true})
  const summary=JSON.parse(fs.readFileSync(path.join(root,folder,'summary.json')))
  const needed=folder==='assembly'?['recipe.mjs','summary.json',path.basename(summary.nbtPath)]:['summary.json','design.json','qa/self-review.json',path.basename(summary.nbtPath)]
  for(const relative of needed) {
    const original=path.join(root,folder,relative)
    const copy=path.join(directory,relative)
    fs.copyFileSync(original,copy)
    files.push({original,copy,original_sha256:hash(original),copy_sha256:hash(copy)})
  }
  if(folder!=='assembly') {
    const copySummary=path.join(directory,'summary.json')
    const rewritten={...summary,nbtPath:path.join(directory,path.basename(summary.nbtPath))}
    fs.writeFileSync(copySummary,JSON.stringify(rewritten,null,2))
    const entry=files.find(file=>file.copy===copySummary)
    entry.copy_sha256=hash(copySummary)
    entry.path_only_rewrite={property:'nbtPath',from:summary.nbtPath,to:rewritten.nbtPath}
  }
}
const executed=spawnSync(process.execPath,['C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/run-build.mjs',path.join(input,'assembly/recipe.mjs'),'--out-dir',output,'--data-version','3465'],{encoding:'utf8',windowsHide:true})
fs.writeFileSync(path.join(workspace,'run.log'),executed.stdout+executed.stderr)
assert.equal(executed.status,0)
const source=JSON.parse(fs.readFileSync(path.join(root,'assembly/summary.json'))).nbtPath
const reproduced=path.join(output,path.basename(source))
assert.equal(hash(source),hash(reproduced))
for(const file of files) {
  assert.equal(hash(file.original),file.original_sha256)
  assert.equal(hash(file.copy),file.copy_sha256)
}
fs.writeFileSync(path.join(workspace,'result.json'),JSON.stringify({status:'PASS',source_sha256:hash(source),reproduced_sha256:hash(reproduced),compressed_bytes_equal:true,frozen_inputs:files,scope:'Only nb tPath filesystem locators rewritten in copied summaries; exact frozen assembly recipe reads copied immutable candidates and reviews, outputs to independent directory. Original inputs and copied preexisting inputs unchanged.',independent_acceptance:false},null,2))
console.log('Frozen-input assembly reproduced byte-for-byte; original inputs unchanged')
