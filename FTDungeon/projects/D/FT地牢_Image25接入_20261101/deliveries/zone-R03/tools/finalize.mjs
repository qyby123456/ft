import assert from 'node:assert/strict'
import crypto from 'node:crypto'
import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const hash = value => crypto.createHash('sha256').update(value).digest('hex')
const readJson = async relative => JSON.parse(await fs.readFile(path.join(root, relative), 'utf8'))
const sha256 = hash(await fs.readFile(path.join(root, 'room.nbt')))
const room = await readJson('room.json')
assert.equal(room.id, 'zone-R03'); assert.equal(room.sha256, sha256)
const reports = {}
for (const filename of ['static', 'forge-native', 'render', 'roundtrip', 'visual-review']) {
  const report = await readJson('qa/'+filename+'.json')
  assert.equal(report.status, 'PASS')
  assert.equal(report.sha256 ?? report.source_sha256, sha256)
  reports[filename] = report
}
assert.equal(reports['forge-native'].rotations.length, 4)
assert.equal(reports['forge-native'].ticks_observed, 240)
assert.equal(reports['forge-native'].rotations.reduce((total, rotation) => total + rotation.states_preserved, 0), 43200)
for (const view of reports.render.views) assert.equal(hash(await fs.readFile(path.join(root, view.file))), view.sha256)
const forgeLog = await fs.readFile(path.join(root, 'qa/forge-run.log'), 'utf8')
assert.ok(forgeLog.includes('ZONE_R03_FORGE_PASS') && forgeLog.includes('All 1 required tests passed') && forgeLog.includes('BUILD SUCCESSFUL'))
const preview = await fs.readFile(path.join(root, 'preview.png'))
assert.equal(preview.readUInt32BE(16), 1536); assert.equal(preview.readUInt32BE(20), 1024)
const reportHashes = {}
for (const filename of ['static', 'forge-native', 'render', 'roundtrip', 'visual-review']) reportHashes['qa/'+filename+'.json'] = hash(await fs.readFile(path.join(root, 'qa/'+filename+'.json')))
const qa = { id:'zone-R03',status:'PASS',sha256,reference_sha256:room.reference_sha256,size:room.size,scope:'Independent room delivery ready for authoritative handover review and unique-slot assignment',static:{status:'PASS',checks:reports.static.checks.length,explicit_records:10800},four_direction_review:{status:'PASS',internal_views:4,external_views:4,axis_views:2,overview_views:1,visual_report:'qa/visual-review.json',contact_sheet:'views/four-direction-review.png'},forge:{status:'PASS',minecraft:'1.20.1',forge:'47.4.10',rotations:4,records_verified:43200,ticks_observed:240,random_tick_speed:0,forced_neighbor_updates:true,collision_reachable_feet_each_rotation:382,water_cells_each_rotation:24,vines_survived_each_rotation:73,roof_cells_each_rotation:675,dropped_items:0,escaped_fluids:0,native_roundtrip:'PASS',log:'qa/forge-run.log',report:'qa/forge-native.json'},four_files:{'room.nbt':sha256,'room.json':hash(await fs.readFile(path.join(root,'room.json'))),'preview.png':hash(preview)},evidence_sha256:reportHashes,deployed:false,production_files_modified:false,player_saves_accessed:false,integration_slot:null,full_modpack_client_tested:false,limits:['Static and Lodestone screenshots are software evidence, not Minecraft client screenshots.','Forge testing used an isolated GameTest world at Y=180, forced chunks and randomTickSpeed=0. Vegetation random growth is not claimed stable indefinitely.','No FT hall route, unique slot, occupancy/persistence, multiplayer or full modpack gameplay acceptance is included.'],previous_attempts:[{type:'compile_failure',cause:'Official 1.20.1 mapping name POTTED_FLOWERING_AZALEA used after correcting the initial longer constant.',preserved:true}],source_documents:room.source_documents }
await fs.writeFile(path.join(root,'qa.json'), JSON.stringify(qa,null,2)+'\n')
const manifest = []
const addFile = async relative => { const buffer = await fs.readFile(path.join(root,relative)); manifest.push({file:relative,bytes:buffer.length,sha256:hash(buffer)}) }
for (const file of ['room.nbt','room.json','preview.png','qa.json','README.md','recipe.mjs','summary.json']) await addFile(file)
for (const folder of ['qa','views','tools']) for (const entry of (await fs.readdir(path.join(root,folder),{withFileTypes:true})).filter(entry=>entry.isFile()).sort((left,right)=>left.name.localeCompare(right.name))) await addFile(folder+'/'+entry.name)
const addTree = async folder => { for (const entry of (await fs.readdir(path.join(root,folder),{withFileTypes:true})).sort((left,right)=>left.name.localeCompare(right.name))) { const relative = folder+'/'+entry.name; if (entry.isDirectory()) await addTree(relative); else if (entry.isFile()) await addFile(relative); } }
await addTree('forge/src')
await addTree('forge/gradle')
for (const file of ['forge/build.gradle','forge/settings.gradle','forge/gradle.properties']) await addFile(file)
await fs.writeFile(path.join(root,'checksums.sha256'),manifest.map(entry=>entry.sha256+'  '+entry.file).join('\n')+'\n')
await fs.writeFile(path.join(root,'delivery-manifest.json'),JSON.stringify({id:'zone-R03',status:'PASS',nbt_sha256:sha256,files:manifest,forge_source_root:'forge/src',excluded_from_distribution:['forge/build','forge/.gradle','forge/run']},null,2)+'\n')
console.log(JSON.stringify({id:'zone-R03',status:'PASS',sha256,files:manifest.length,qa_sha256:hash(await fs.readFile(path.join(root,'qa.json')))}))

