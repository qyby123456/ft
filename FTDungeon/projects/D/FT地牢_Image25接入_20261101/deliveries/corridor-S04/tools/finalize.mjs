import fs from 'node:fs/promises';
import path from 'node:path';
import assert from 'node:assert/strict';
import { createHash } from 'node:crypto';
import { fileURLToPath } from 'node:url';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const readJson = async relative => JSON.parse(await fs.readFile(path.join(root, relative), 'utf8'));
const hash = async relative => createHash('sha256').update(await fs.readFile(path.join(root, relative))).digest('hex');
const metadata = await readJson('room.json');
const staticReport = await readJson('qa/static.json');
const nativeReport = await readJson('qa/forge-native.json');
const renderReport = await readJson('qa/render.json');
const sourceHash = await hash('room.nbt');
assert.equal(sourceHash, '11b8cd39ebf685ddb32ceb62211a0b8a58257d13ca0e31ca7261fb969a144baf', 'Producer visual review belongs to a different NBT; inspect the new views before updating the reviewed fingerprint');
assert.equal(metadata.sha256, sourceHash);
assert.equal(staticReport.sha256, sourceHash);
assert.equal(nativeReport.sha256, sourceHash);
assert.equal(renderReport.source_sha256, sourceHash);
assert.equal(staticReport.status, 'PASS');
assert.equal(nativeReport.status, 'PASS');
assert.equal(renderReport.status, 'PASS');
assert.equal(nativeReport.rotations.length, 4);
assert(nativeReport.rotations.every(rotation => rotation.status === 'PASS' && rotation.exact_state_records === 1584 && rotation.reachable_three_wide_route_cells === 48 && rotation.dropped_items === 0));
assert(nativeReport.ticks_observed >= 240);
assert.equal(renderReport.screenshots.length, 8);
for (const image of renderReport.screenshots) assert.equal(await hash(image.file), image.sha256);
const log = await fs.readFile(path.join(root, 'qa/forge-run.log'), 'utf8');
assert(log.includes('S04_FORGE_PLACEMENT_PASS') && log.includes('All 1 required tests passed') && log.includes('BUILD SUCCESSFUL'));
const reviews = {
  'north-exterior': 'PASS: north arch is open, floor is flush, full earth roof and side walls are intact.',
  'north-interior': 'PASS: three-wide straight aisle reads clearly to the open south arch; red caps, cyan-white strips and aged arches match the reference theme.',
  'east-exterior': 'PASS: east exterior has a continuous earth shell and sealed roof; there are no side branches.',
  'east-interior': 'PASS: preview-only cutaway exposes the west mycelium shelf, mushroom stems, off-path webs, light recesses and continuous floor.',
  'south-exterior': 'PASS: south end repeats a usable open stone arch with supported floor; no closed door or end wall blocks passage.',
  'south-interior': 'PASS: reverse route to north is open at the same elevation; no stairs, branch, fluid or web obstructs the aisle.',
  'west-exterior': 'PASS: west earth shell and full roof remain continuous; entry and exit arch geometry are intact.',
  'west-interior': 'PASS: opposite cutaway shows independent red mushrooms, lamp plinths and floor edge details; decoration stays outside the center aisle.',
};
const visual = {
  id: 'corridor-S04', producer_metadata_id: 'S04', status: 'PASS', source_sha256: sourceHash,
  reviewer: 'Producer Codex / direct image inspection via view_image',
  reviewed_individually: true,
  views: renderReport.screenshots.map(image => ({ file: image.file, sha256: image.sha256, name: image.name, verdict: reviews[image.name] })),
  source_roof_retained: true,
  source_nbt_unchanged: true,
  internal_cutaways: ['east-interior', 'west-interior'],
  full_template_eye_level_internal_views: ['north-interior', 'south-interior'],
  accepted_adaptations: metadata.reference_adaptations,
  limitations: ['Production review only. Receiver must independently review the frozen bundle and rerun its isolated Forge fixture before integration readiness.', 'Sea lanterns/end rods reproduce the light palette; warped fungi do not glow in unmodified vanilla Minecraft.', 'Side cutaways intentionally remove the nearest wall/shelf and roof for visibility; north/south interiors show the complete final template.'],
};
await fs.writeFile(path.join(root, 'qa/visual-review.json'), JSON.stringify(visual, null, 2) + '\n');
renderReport.visual_review = 'PASS_PRODUCER_INDIVIDUAL_INSPECTION';
renderReport.visual_review_report = 'qa/visual-review.json';
await fs.writeFile(path.join(root, 'qa/render.json'), JSON.stringify(renderReport, null, 2) + '\n');
const evidenceFiles = ['room.nbt', 'room.json', 'preview.png', 'preview/four-directions.png', 'qa/static.json', 'qa/forge-native.json', 'qa/native-roundtrip.nbt', 'qa/forge-run.log', 'qa/render.json', 'qa/visual-review.json', 'recipe.mjs', 'tools/inspect.mjs', 'tools/capture.mjs', 'tools/finalize.mjs', 'forge/build.gradle', 'forge/src/main/java/net/farmingtales/corridorqa/CorridorPlacementTests.java', ...renderReport.screenshots.map(image => image.file)];
const evidence = {};
for (const relative of evidenceFiles) {
  const bytes = await fs.readFile(path.join(root, relative));
  evidence[relative] = { sha256: createHash('sha256').update(bytes).digest('hex'), bytes: bytes.length };
}
const report = {
  id: 'corridor-S04', producer_metadata_id: 'S04', name: metadata.name, status: 'PASS',
  acceptance_level: 'PRODUCER_COMPLETE_AWAITING_INDEPENDENT_RECEIVER_ACCEPTANCE',
  sha256: sourceHash, reference: metadata.reference, reference_sha256: metadata.reference_sha256,
  minecraft: '1.20.1', forge: '47.4.10', data_version: 3465, size: metadata.size,
  length_class: 'short', length_blocks: 16, axis: 'z',
  entrance: metadata.entrance, interior: metadata.interior, exit: metadata.exit, exit_interior: metadata.exitInterior,
  checks: { static: 'PASS', final_nbt_renderer_views: 'PASS', eight_individual_producer_visual_reviews: 'PASS', vanilla_registry_and_full_states: 'PASS', native_nbt_roundtrip: 'PASS', forge_real_four_rotation_placement: 'PASS', forced_neighbor_updates: 'PASS', ticks_240_exact_state_stability: 'PASS', three_wide_three_high_route_both_ways: 'PASS', native_supports_and_no_item_drops: 'PASS', full_roof_and_closed_side_walls: 'PASS', no_entities_inventory_loot_or_liquid: 'PASS' },
  native: { report: 'qa/forge-native.json', log: 'qa/forge-run.log', rotations: 4, ticks_observed: 240, records_per_rotation: 1584, non_air_per_rotation: 901, reachable_route_cells_per_rotation: 48, dropped_items: 0, test_world: 'forge/run/s04-isolated-gametest', player_worlds_accessed: false },
  visual: { report: 'qa/visual-review.json', renderer_report: 'qa/render.json', overview: 'preview/four-directions.png', views: renderReport.screenshots.map(image => image.file) },
  adaptations: metadata.reference_adaptations,
  integration: { ready_for_unified_integration: false, receiver_acceptance: 'PENDING', core_catalog_modified: false, deployed: false, player_save_modified: false, reserved_intake_slot: 64, note: 'Reserved intake slot only. Producer PASS does not replace independent receiver acceptance or runtime integration tests.' },
  limits: ['Lodestone renders and isolated Forge GameTests are distinct from full-modpack client gameplay.', 'Hall route connection, both-end external adapters, occupancy rejection, persistence/idempotence and existing-world migration belong to the core integrator.', 'Explicit air records require whole-volume preflight. Do not clear or overwrite occupied player space.'],
  evidence,
};
await fs.writeFile(path.join(root, 'qa.json'), JSON.stringify(report, null, 2) + '\n');
await fs.writeFile(path.join(root, 'qa/delivery-manifest.json'), JSON.stringify({ id: report.id, status: 'PASS', source_sha256: sourceHash, files: { ...evidence, 'qa.json': { sha256: await hash('qa.json'), bytes: (await fs.stat(path.join(root, 'qa.json'))).size } } }, null, 2) + '\n');
console.log(JSON.stringify({ id: report.id, status: report.status, sha256: sourceHash, files: Object.keys(evidence).length, receiver_acceptance: 'PENDING' }));
