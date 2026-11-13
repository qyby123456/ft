import fs from 'node:fs/promises'
import path from 'node:path'
import { createHash } from 'node:crypto'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const hashFile = async relative => createHash('sha256').update(await fs.readFile(path.join(root, relative))).digest('hex')
const readJson = async relative => JSON.parse(await fs.readFile(path.join(root, relative), 'utf8'))
const writeJson = async (relative, value) => fs.writeFile(path.join(root, relative), JSON.stringify(value, null, 2) + '\n')
const sourceHash = await hashFile('room.nbt')
const metadata = await readJson('room.json')
const staticReport = await readJson('qa/static.json')
const nativeReport = await readJson('qa/native.json')
const viewReport = await readJson('qa/views.json')
for (const report of [staticReport, nativeReport, viewReport]) if (report.status !== 'PASS' || report.source_sha256 !== sourceHash) throw new Error('QA is not PASS or refers to a different NBT')
if (metadata.sha256 !== sourceHash || nativeReport.rotations.length !== 4 || nativeReport.stability_ticks !== 240) throw new Error('Native evidence incomplete')
const nativeLog = await fs.readFile(path.join(root, 'qa/forge-gametest.log'), 'utf8')
if (!nativeLog.includes('BUILD SUCCESSFUL') || !nativeLog.includes('A04_NATIVE_PASS rotations=4/4 records=15884 ticks=240 sha256=' + sourceHash)) throw new Error('Missing successful native log marker')
await fs.copyFile(path.join(root, 'views/eye-north.png'), path.join(root, 'preview.png'))
const sheetFiles = ['views/interior-four-views.png', 'views/exterior-four-views.png', 'views/four-direction-review.png']
const captures = []
for (const capture of viewReport.captures) {
  if (await hashFile(capture.path) !== capture.sha256) throw new Error('Changed view ' + capture.path)
  captures.push({ ...capture, source_sha256: sourceHash })
}
for (const relative of sheetFiles) captures.push({ path: relative, sha256: await hashFile(relative), composed_from_actual_nbt_renders: true, source_sha256: sourceHash })
await writeJson('views/manifest.json', {
  id: 'A04', status: 'PASS', source_nbt: 'room.nbt', source_sha256: sourceHash,
  renderer: viewReport.renderer, render_level: 'Software WebGL Lodestone preview, not a Minecraft client screenshot.',
  interior_cut_rule: viewReport.interior_cut_rule, final_nbt_modified: false,
  direction_coordinates: {north:'-Z',south:'+Z',west:'-X',east:'+X'}, captures
})
const visualReview = {
  id:'A04', status:'PASS', role:'producer visual review', source_sha256:sourceHash,
  reviewed_files:['views/interior-four-views.png','views/exterior-four-views.png','views/eye-north.png','views/eye-south.png'],
  observations:{
    design:'Mossy cracked stone room, grounded masonry pillars, timber beam grid, tool rack/workbench, grindstones, damaged anvil, cold furnace chimney, boarded window, empty storage and scattered rubble preserve the workshop theme.',
    supports:'Four stone pillars meet the y=8 beam frame. Two chain lamps connect through y=6..7 to y=8 solid beams. Tool chains meet the full timber rack at y=5. Worktops are backed by full base blocks; ground rubble and standing lanterns sit on the floor.',
    circulation:'North three-wide/three-high opening and central x=8..10 route to z=14 are clear. All seven station/safety targets are reachable in native collision checks; 240 level walkable feet cells per rotation.',
    containers:'Three barrels, one chest and one cold furnace are empty with no loot tables. Chest overhead is clear. Five native empty-container checks pass in every rotation before and after stabilization.',
    shell:'Only the north entrance opens through the shell. West boarded window has glass backing. All 361 roof cells at y=10 are solid; concept roof break is a sealed cracked-masonry repair patch.',
    clipping:'No unintended unsupported or intersecting prop geometry observed. Preview-only wall/roof removal is recorded in the view manifest.'
  },
  receiver_independent_acceptance:'Not performed by this producer; receiving thread retains its own acceptance gate.'
}
await writeJson('qa/visual-review.json',visualReview)
const batchRoot = 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103'
const batchFiles = []
for (const relative of ['README.md','docs/manifest.json','docs/verify.json']) {
  const absolute = path.join(batchRoot,relative)
  batchFiles.push({path:absolute,sha256:createHash('sha256').update(await fs.readFile(absolute)).digest('hex')})
}
await writeJson('qa/provenance.json', {
  id:'A04',delivery_id:'zone-A04',reference:metadata.reference,reference_sha256:metadata.reference_sha256,
  batch_files:batchFiles,contract:'D:/ft工程文件/FT地牢_Image25接入_20261101/CONTRACT.md',
  builder_skill:'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/SKILL.md',
  build_recipe:'source/recipe.mjs',build_recipe_sha256:await hashFile('source/recipe.mjs'),
  forge_fixture:'forge/src/main/java/net/farmingtales/a04qa/RoomTests.java',forge_fixture_sha256:await hashFile('forge/src/main/java/net/farmingtales/a04qa/RoomTests.java'),
  completed_native_run_directory:'forge/run-isolated',successful_log:'qa/forge-gametest.log',
  native_log_sha256:await hashFile('qa/forge-gametest.log'),source_sha256:sourceHash,
  execution_note:'Delivered QA was executed in the initial fresh run-isolated world. The replay script now uses a new timestamped run directory for each subsequent run.'
})
const evidenceFiles = ['room.nbt','room.json','preview.png','qa/static.json','qa/native.json','qa/visual-review.json','qa/provenance.json','qa/forge-gametest.log','views/manifest.json',...sheetFiles,...nativeReport.rotations.map(rotation=>rotation.capture)]
const evidence = []
for (const relative of evidenceFiles) evidence.push({path:relative,sha256:await hashFile(relative)})
const qa = {
  schema:'ft-independent-zone-room-delivery-v1',id:'A04',delivery_id:'zone-A04',name:'砸毁工坊',status:'PASS',
  sha256:sourceHash,reference:metadata.reference,reference_sha256:metadata.reference_sha256,
  size:metadata.size,entrance:metadata.entrance,interior:metadata.interior,facing:metadata.facing,
  static:{status:'PASS',report:'qa/static.json',checks:Object.keys(staticReport.checks).length,records:3971,palette:35,nonair:1920},
  four_direction_views:{status:'PASS',interior_views:4,full_shell_views:4,eye_views:2,report:'views/manifest.json',visual_review:'qa/visual-review.json',nbt_unchanged:true},
  forge:{status:'PASS',runtime:nativeReport.runtime,report:'qa/native.json',log:'qa/forge-gametest.log',rotations:4,stability_ticks:240,forced_tick_chunks:true,records_checked_after_ticks:15884,roundtrip_captures:4,reachable_level_feet_per_rotation:240,reachable_targets_per_rotation:7,empty_containers_per_rotation:5,negative_probes_per_rotation:nativeReport.negative_probes_per_rotation},
  safety:{north_entrance_width:3,north_entrance_height:3,solid_floor:true,complete_roof:true,fixtures_supported:true,container_loot:false,entities:0,fluids:0,hazardous_route_blocks:0},
  evidence_level:'Static final-NBT audit, producer visual review and isolated headless Forge GameTest placement/stability. Full modpack client play, multiplayer and FPS have not been tested.',
  integration:{state:'independent_candidate_not_deployed',unique_runtime_slot:null,receiver_acceptance:'pending',full_volume_occupancy_preflight_required:true,connect_hall_to_north_port_required:true,rotation_must_transform_ports_and_block_states:true,preserve_existing_world_and_player_edits:true},
  boundaries:{formal_instance_modified:false,player_saves_modified:false,other_delivery_ids_modified:false,deployed:false},
  evidence
}
await writeJson('qa.json',qa)
const checksumFiles = [...new Set([...evidenceFiles,'qa.json','README.md','summary.json','source/recipe.mjs','source/qa-static.mjs','source/render-views.mjs','source/make-sheets.py','source/finalize.mjs','source/run-native.ps1','forge/build.gradle','forge/settings.gradle','forge/src/main/java/net/farmingtales/a04qa/RoomTests.java','forge/src/main/java/net/farmingtales/a04qa/Verification.java',...captures.map(capture=>capture.path)])].sort()
const sums = []
for (const relative of checksumFiles) sums.push((await hashFile(relative))+'  '+relative.replaceAll('\\','/'))
await fs.writeFile(path.join(root,'SHA256SUMS.txt'),sums.join('\n')+'\n')
console.log(JSON.stringify({id:'A04',status:'PASS',sha256:sourceHash,files:checksumFiles.length,rotations:4,ticks:240}))
