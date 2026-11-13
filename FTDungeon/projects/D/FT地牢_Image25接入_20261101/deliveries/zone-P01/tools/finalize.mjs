import fs from 'node:fs/promises'
import path from 'node:path'
import { createHash } from 'node:crypto'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const sha256 = bytes => createHash('sha256').update(bytes).digest('hex')
const read = async file => JSON.parse(await fs.readFile(path.join(root,file),'utf8'))
const require = (condition,message) => { if (!condition) throw new Error(message) }
const metadata = await read('room.json')
const staticQa = await read('qa/static.json')
const nativeQa = await read('qa/native.json')
const views = await read('qa/views.json')
const decode = await read('qa/image-decode.json')
const visual = await read('qa/visual-review.json')
const hash = sha256(await fs.readFile(path.join(root,'room.nbt')))
require(hash === metadata.sha256 && hash === staticQa.source_sha256 && hash === nativeQa.structure_sha256 && hash === views.source_sha256,'Evidence source hashes differ')
for (const [name,value] of Object.entries({staticQa,nativeQa,views,decode,visual})) require(value.status === 'PASS',name+' not passed')
require(Object.values(staticQa.checks).every(Boolean),'Static checks failed')
require(nativeQa.rotations.length === 4 && nativeQa.records_checked_after_ticks === 30856 && nativeQa.stabilization_ticks === 200,'Native rotation/stability count differs')
require(nativeQa.native_jump_passes_after_ticks === 32 && nativeQa.native_fall_passes_after_ticks === 64,'Native movement count differs')
for (const rotation of nativeQa.rotations) {
  require(rotation.voxel_records_checked === 7714 && rotation.recovery_floor_cells === 84 && rotation.capture_matches_source && !rotation.ground_bypass,'Native coverage differs')
  require(rotation.native_player_jumps.length === 8 && rotation.native_player_falls.length === 16 && rotation.walking_segments.length === 5,'Incomplete composed route')
  require(sha256(await fs.readFile(path.join(root,rotation.world_capture))) === rotation.world_capture_sha256,'World capture hash differs')
  for (const fall of rotation.native_player_falls) require(fall.vanilla_predicted_damage_hp <= (fall.after_jump ? 2 : 0),'Fall damage bound exceeded')
}
require(views.captures.length === 10 && decode.images.length === 10,'Missing views')
for (const capture of views.captures) require(sha256(await fs.readFile(path.join(root,capture.path))) === capture.sha256,'Rendered view hash differs')
require(sha256(await fs.readFile(path.join(root,'preview.png'))) === sha256(await fs.readFile(path.join(root,'views/interior-north.png'))),'Preview not final NBT view')
const nativeLog = await fs.readFile(path.join(root,'qa/forge-gametest.log'),'utf8')
require(nativeLog.includes('BUILD SUCCESSFUL') && nativeLog.includes('P01_NATIVE_PASS rotations=4/4 voxels=30856 jumps=32 falls=64 ticks=200'),'Final Forge log not passed')
const batchRoot = 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103'
const provenance = {}
for (const [kind,relative,key] of [['manifest','docs/manifest.json','files'],['verify','docs/verify.json','records']]) {
  const bytes = await fs.readFile(path.join(batchRoot,relative))
  const data = JSON.parse(bytes.toString('utf8'))
  const record = data[key].find(entry => entry.file === 'out/parkour/P01-parkour-gap-jump-pit.png')
  require(record?.sha256 === metadata.reference_sha256 && record.ok && record.size[0] === 1536 && record.size[1] === 1024,'Batch '+kind+' mismatch')
  provenance[kind] = {path:path.join(batchRoot,relative),sha256:sha256(bytes),record}
}
const referenceBytes = await fs.readFile(metadata.reference)
require(sha256(referenceBytes) === metadata.reference_sha256,'Reference image changed')
provenance.reference = {path:metadata.reference,sha256:metadata.reference_sha256,bytes:referenceBytes.length}
provenance.readme = {path:path.join(batchRoot,'README.md'),sha256:sha256(await fs.readFile(path.join(batchRoot,'README.md')))}
provenance.status = 'PASS'
await fs.writeFile(path.join(root,'qa/reference.json'),JSON.stringify(provenance,null,2)+'\n')
const fallChecks = nativeQa.rotations.flatMap(rotation => rotation.native_player_falls)
const report = {
  schema:'ft-independent-zone-room-v1',id:'P01',status:'PASS',sha256:hash,reference:metadata.reference,reference_sha256:metadata.reference_sha256,size:metadata.size,entrance:metadata.entrance,interior:metadata.interior,exit:metadata.exit,minecraft:'1.20.1',forge:'47.4.10',
  checks:{static_geometry_and_reference:'PASS',full_roof_and_foundation:'PASS',three_wide_four_high_ports:'PASS',four_two_cell_jumps_with_six_cell_clearance:'PASS',all_84_pit_cells_recoverable:'PASS',no_walk_only_bypass:'PASS',native_registry_and_roundtrip:'PASS',forge_four_rotations_placement:'PASS',forge_200_tick_stability:'PASS',four_world_captures_match_source:'PASS',native_forward_reverse_jumps:'PASS',native_direct_and_failed_jump_landings:'PASS',vanilla_fall_damage_upper_bound:'PASS',negative_probes:'PASS',four_interior_four_exterior_views:'PASS',visual_review:'PASS'},
  native_summary:{rotations:nativeQa.rotations.map(rotation=>rotation.rotation),records_per_rotation:7714,records_checked_after_ticks:30856,stabilization_ticks:200,lights_supported_per_rotation:14,recoverable_pit_floor_cells_per_rotation:84,entry_connected_foot_cells_per_rotation:244,native_jump_passes_after_ticks:32,native_fall_passes_after_ticks:64,forward_and_reverse:true,maximum_failed_jump_fall_blocks:Math.max(...fallChecks.map(fall=>fall.maximum_fall_blocks)),maximum_failed_jump_damage_hp:2,straight_step_off_damage_hp:0,damage_evidence:'Native calculateFallDamage; FakePlayer health does not establish zero damage',negative_probes:nativeQa.negative_probes},
  evidence:{static:'qa/static.json',reference:'qa/reference.json',native:'qa/native.json',native_log:'qa/forge-gametest.log',render_manifest:'qa/views.json',image_decode:'qa/image-decode.json',visual_review:'qa/visual-review.json',interior_sheet:'views/interior-sheet.png',exterior_sheet:'views/exterior-sheet.png',native_world_captures:nativeQa.rotations.map(rotation=>rotation.world_capture)},
  attempts:[{number:1,status:'failed_fixture_resource_then_fixed',reason:'Missing pack.mcmeta prevented empty GameTest template discovery',log:'qa/attempt-1-fixture-template.log'},{number:2,status:'passed_before_apex_audit',log:'qa/attempt-2-passed-before-apex-audit.log'},{number:3,status:'occupied_fixture_refused_as_designed',reason:'Enhanced test attempted to reuse occupied isolated world; runner now uses unique directories',log:'qa/attempt-3-refused-reused-test-world.log'},{number:4,status:'PASS',log:'qa/forge-gametest.log'}],
  safety:{deployed:false,production_instance_modified:false,production_saves_opened_or_modified:false,other_room_sources_modified:false,shared_core_modified:false,paid_image_api_called:false,isolated_test_worlds_only:true},
  not_tested:['Full FarmingTales modpack client human playthrough, FPS, multiplayer.','FT core route integration, persistent generation markers, existing return portals.','Modified physics, jump boosts or critically low health fall survivability.'],
  integration_requirements:['Preflight all 7714 cells including explicit air, entities, fluids and world boundary before placement.','Reject occupied volumes; preserve a persistent per-room generation marker and player changes.','Rotate both ports and block states using the same native rotation.','Do not deploy the QA fixture mod or its disposable worlds.']
}
await fs.writeFile(path.join(root,'qa.json'),JSON.stringify(report,null,2)+'\n')
const files = ['room.nbt','room.json','preview.png','qa.json','README.md','summary.json']
async function appendTree(relative) {
  for (const entry of await fs.readdir(path.join(root,relative),{withFileTypes:true})) {
    const next = path.join(relative,entry.name)
    if (entry.isDirectory()) await appendTree(next)
    else if (entry.isFile()) files.push(next)
  }
}
await appendTree('qa'); await appendTree('views'); await appendTree('tools'); await appendTree('native-qa/src')
files.push('native-qa/build.gradle','native-qa/settings.gradle','native-qa/gradle.properties')
const lines = []
for (const file of files.sort()) lines.push(sha256(await fs.readFile(path.join(root,file)))+'  '+file.replaceAll('\\','/'))
await fs.writeFile(path.join(root,'SHA256SUMS.txt'),lines.join('\n')+'\n')
console.log(JSON.stringify({id:'P01',status:'PASS',sha256:hash,artifact_hashes:files.length,native_rotations:4,jumps:32,falls:64,maximum_failed_jump_damage_hp:2}))

