import fs from 'node:fs'
import path from 'node:path'
import {fileURLToPath} from 'node:url'
import {spawnSync} from 'node:child_process'
import {createHash} from 'node:crypto'
import assert from 'node:assert/strict'
import {Structure} from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'
const root=path.dirname(fileURLToPath(import.meta.url))
const directory=path.join(root,'qa/safety-fixtures')
fs.mkdirSync(directory,{recursive:true})
const inputs=[]
for(const [group,mode] of ['sealed','partial-face','flammable-neighbor'].entries()) {
  const structure=new Structure([9,9,9])
  structure.addBlock([4,2,4],'minecraft:lava',{level:'0'})
  for(const delta of [[1,0,0],[-1,0,0],[0,1,0],[0,-1,0],[0,0,1],[0,0,-1]]) structure.addBlock(delta.map((value,axis)=>value+[4,2,4][axis]),'minecraft:obsidian')
  if(mode==='partial-face') structure.addBlock([5,2,4],'minecraft:polished_blackstone_brick_slab',{type:'bottom',waterlogged:'false'})
  if(mode==='flammable-neighbor') structure.addBlock([5,4,4],'minecraft:oak_planks')
  const bytes=structure.writeNbt({dataVersion:3465,compression:'gzip'})
  const candidate=path.join(directory,mode+'.nbt')
  fs.writeFileSync(candidate,bytes)
  inputs.push({group,candidate,sha256:createHash('sha256').update(bytes).digest('hex')})
}
fs.writeFileSync(path.join(directory,'route-input.json'),JSON.stringify(inputs,null,2))
const classpath=fs.readFileSync('D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/S8-rebuild-single-20261104-09/qa/classpath.txt','utf8').trim()
const result=spawnSync('java',['-Xmx1024m','-Dfile.encoding=UTF-8','-cp',path.join(root,'qa/classes')+';'+classpath,'NativeSafetyAudit',directory],{encoding:'utf8',windowsHide:true,cwd:directory})
fs.writeFileSync(path.join(directory,'run.log'),result.stdout+result.stderr)
assert.equal(result.status,0)
const reports=JSON.parse(fs.readFileSync(path.join(directory,'native-fluid-fire.json')))
assert.equal(reports[0].failures.length,0)
assert(reports[1].failures.some(failure=>failure.startsWith('Incomplete fluid collision face')))
assert(reports[2].failures.some(failure=>failure.startsWith('Native flammable block near lava')))
fs.writeFileSync(path.join(directory,'fixture-result.json'),JSON.stringify({sealed_pass:true,slab_collision_gap_detected:true,native_flammable_plank_detected:true,independent_acceptance:false},null,2))
console.log('Safety positive and negative fixtures passed')
