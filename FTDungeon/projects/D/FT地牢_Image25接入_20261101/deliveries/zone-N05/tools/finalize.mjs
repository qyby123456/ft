import fs from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { createHash } from 'node:crypto';
import assert from 'node:assert/strict';
const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const hash = bytes => createHash('sha256').update(bytes).digest('hex');
const readJson = async name => JSON.parse(await fs.readFile(path.join(root,name),'utf8'));
const digest = hash(await fs.readFile(path.join(root,'room.nbt')));
const metadata = await readJson('room.json');
const staticReport = await readJson('qa/static.json');
const native = await readJson('qa/forge-native.json');
const render = await readJson('qa/render.json');
const review = await readJson('qa/visual-review.json');
for (const report of [metadata,staticReport,native]) assert.equal(report.sha256,digest);
for (const report of [render,review]) assert.equal(report.source_sha256,digest);
for (const report of [staticReport,native,render,review]) assert.equal(report.status,'PASS');
render.visual_review = 'PASS';
render.visual_review_evidence = 'qa/visual-review.json';
await fs.writeFile(path.join(root,'qa/render.json'),JSON.stringify(render,null,2)+'\n');
assert.equal(native.rotations.length,4);
assert.equal(native.ticks_observed,240);
assert(native.forced_neighbor_updates && native.forced_chunk_ticking && native.isolated_test_environment.terrain_clearance_margin_blocks === 3);
assert(native.rotations.every(rotation => rotation.state_drift===0 && rotation.dropped_items===0 && rotation.exact_state_records===20184 && rotation.ladder_segments===18));
assert.equal(render.screenshots.length,17);
assert.equal(review.views.length,17);
for (const screenshot of render.screenshots) {
  assert.equal(hash(await fs.readFile(path.join(root,screenshot.file))),screenshot.sha256);
  assert(review.views.some(view=>view.file===screenshot.file && view.sha256===screenshot.sha256 && view.status==='PASS'));
}
const log = await fs.readFile(path.join(root,'qa/forge-run.log'),'utf8');
for (const marker of ['N05_FORGE_PLACEMENT_PASS','All 1 required tests passed','BUILD SUCCESSFUL']) assert(log.includes(marker));
const files = ['room.nbt','room.json','preview.png','README.md','recipe.mjs','summary.json','preview/four-directions.png','preview/full-interior-details.png','preview/viewer.html','preview/viewer.mjs','qa/static.json','qa/route-cells.json','qa/decoded-nbt.json','qa/forge-native.json','qa/native-roundtrip.nbt','qa/forge-run.log','qa/forge-first-rejected.log','qa/forge-diagnostic.log','qa/forge-native-unbuffered-pass.json','qa/rejected-world-items.json','qa/render.json','qa/visual-review.json','tools/inspect.mjs','tools/capture.mjs','tools/finalize.mjs','tools/inspect-rejected-world.mjs','tools/run-forge.ps1','forge/build.gradle','forge/gradle.properties','forge/settings.gradle','forge/src/main/java/net/farmingtales/zoneqa/ZoneQa.java','forge/src/main/java/net/farmingtales/zoneqa/LiftHubTests.java','forge/src/main/resources/META-INF/mods.toml','forge/src/main/resources/pack.mcmeta','forge/src/main/resources/data/ftzonen05qa/structures/empty.nbt',...render.screenshots.map(item=>item.file)];
const evidence = {};
for (const name of files) {
  const bytes = await fs.readFile(path.join(root,name));
  evidence[name] = {sha256:hash(bytes),bytes:bytes.length};
}
const qa = {
  id:'zone-N05',name:'升降枢纽',date:'2026-11-03',status:'PASS',status_scope:'Producer completion; independent receiver acceptance pending',sha256:digest,
  size:metadata.size,data_version:3465,minecraft:'1.20.1',reference:metadata.reference,
  checks:{static:'PASS',four_direction_exterior_interior_visual_review:'PASS',full_nbt_interior_landing_review:'PASS',forge_four_rotation_real_placement:'PASS',forced_neighbor_updates:'PASS',native_collision_connectivity_support:'PASS',ticks_240_stability:'PASS',source_nbt_hash_consistency:'PASS'},
  geometry:{horizontal_ports:4,internal_vertical_docks:2,main_feet_y:7,main_floor_y:6,main_route_width:3,main_route_clear_height:3,main_route_cells:240,reachable_graph_cells_each_rotation:1835,emergency_ladder_segments:18,guard_height:2,closed_bottom_and_roof:true,static_cage:true},
  runtime:{forge:'47.4.10',rotations:4,total_exact_state_records:80736,ticks_observed:240,state_drift:0,dropped_items:0,supported_lanterns_each_rotation:12},
  visual:{renderer:render.renderer,individual_png_count:17,individual_size:[1536,1024],four_cardinal_full_exteriors:true,four_cardinal_declared_sections:true,four_cardinal_full_main_level_views:true,three_ladder_landing_views:true,delivered_nbt_cut:false},
  rejected_run:{preserved:true,reason:'Two pre-existing ambient brown mushroom item entities were just outside the 180-degree placement volume but inside the item-scan margin; source palette contains no mushrooms and no source state drift occurred.',evidence:'qa/rejected-world-items.json',resolution:'Fresh diagnostic world passed; final clean buffered isolated terrain with ambient entities removed, mob spawning and random ticks disabled passed all unchanged-NBT checks.'},
  integration:{deployed:false,core_catalog_modified:false,production_files_modified:false,player_save_modified:false,receiver_acceptance:'PENDING',ready_for_unified_integration:false},
  limits:['Static suspended cage and winch only; no functional elevator or transport logic.','Vertical docks are internal ladder landings with closed roof/bottom; future exterior stacking requires a separately verified adapter.','Native path graph, body collision and ladder support are verified, but actual player climbing and full-modpack client gameplay are not.','Software renders are not game screenshots.','External port adapters, hall connection, occupancy rejection, persistence, idempotence, migration, multiplayer and FPS remain integration work.','Explicit air records require complete-volume preflight and no-overwrite handling.'],
  evidence,
};
await fs.writeFile(path.join(root,'qa.json'),JSON.stringify(qa,null,2)+'\n');
const qaBytes = await fs.readFile(path.join(root,'qa.json'));
const manifest = {id:'zone-N05',date:'2026-11-03',status:'PASS',source_sha256:digest,four_piece:['room.nbt','room.json','preview.png','qa.json'],files:{...evidence,'qa.json':{sha256:hash(qaBytes),bytes:qaBytes.length}},excludes:['Forge build caches and generated test worlds are retained locally but are not source delivery artifacts.'],integration:qa.integration};
await fs.writeFile(path.join(root,'delivery-manifest.json'),JSON.stringify(manifest,null,2)+'\n');
console.log(JSON.stringify({status:'PASS',size:metadata.size,sha256:digest,four_piece:Object.fromEntries(manifest.four_piece.map(name=>[name,manifest.files[name]])),evidence_files:Object.keys(manifest.files).length}));
