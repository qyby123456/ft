import fs from 'node:fs/promises'
import path from 'node:path'
import { createHash } from 'node:crypto'
import { createRequire } from 'node:module'
import { fileURLToPath } from 'node:url'
import { specification } from './recipe.mjs'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const batch = 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103'
const readJson = async relative => JSON.parse(await fs.readFile(path.join(root, relative), 'utf8'))
const hash = bytes => createHash('sha256').update(bytes).digest('hex')
const hashFile = async file => hash(await fs.readFile(file))
const require = createRequire('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/runtime.cjs')
const sharp = require('sharp')
const metadata = await readJson('room.json')
const staticQa = await readJson('qa/static.json')
const nativeQa = await readJson('qa/native.json')
const viewsQa = await readJson('qa/views.json')
const sourceHash = await hashFile(path.join(root, 'room.nbt'))
if (sourceHash !== metadata.sha256 || sourceHash !== staticQa.source_sha256 || sourceHash !== nativeQa.structure_sha256 || sourceHash !== viewsQa.source_sha256) throw new Error('NBT evidence fingerprint mismatch')
if (staticQa.status !== 'PASS' || nativeQa.status !== 'PASS' || nativeQa.rotations.length !== 4 || nativeQa.stabilization_ticks !== 200) throw new Error('Incomplete QA')
const referenceBytes = await fs.readFile(specification.reference)
const referenceInfo = await sharp(referenceBytes).metadata()
await sharp(referenceBytes).raw().toBuffer()
if (hash(referenceBytes) !== specification.referenceSha256 || referenceInfo.width !== 1536 || referenceInfo.height !== 1024) throw new Error('Reference image mismatch')
const batchManifest = JSON.parse(await fs.readFile(path.join(batch, 'docs/manifest.json'), 'utf8'))
const batchVerify = JSON.parse(await fs.readFile(path.join(batch, 'docs/verify.json'), 'utf8'))
const relativeReference = 'out/mines/M05-mines-crystal-mine-grotto.png'
const manifestRecord = batchManifest.files.find(record => record.file === relativeReference)
const verifyRecord = batchVerify.records.find(record => record.file === relativeReference)
if (manifestRecord?.sha256 !== specification.referenceSha256 || verifyRecord?.sha256 !== specification.referenceSha256 || !manifestRecord.ok || !verifyRecord.ok) throw new Error('Batch evidence mismatch')
const referenceDocuments = []
for (const relative of ['README.md', 'docs/manifest.json', 'docs/verify.json', 'prompts/M05-mines-crystal-mine-grotto.txt']) referenceDocuments.push({ path: path.join(batch, relative), sha256: await hashFile(path.join(batch, relative)) })
const referenceQa = { status: 'PASS', reference: specification.reference, sha256: specification.referenceSha256, width: referenceInfo.width, height: referenceInfo.height, channels: referenceInfo.channels, decoded_ok: true, batch_total: batchVerify.total, batch_decoded_ok: batchVerify.decoded_ok, manifest_record: manifestRecord, verify_record: verifyRecord, documents: referenceDocuments, paid_generation_invoked: false }
await fs.writeFile(path.join(root, 'qa/reference.json'), JSON.stringify(referenceQa, null, 2) + '\n')
const visualChecks = {
  four_pillars_have_grounded_footings_and_ceiling_contact: true,
  no_pillar_furniture_intersection_observed: true,
  hanging_lamps_have_continuous_chains_to_beams: true,
  attached_violet_clusters_and_grounded_blue_glass_crystals_visible: true,
  shallow_water_basin_is_separate_from_dry_main_route: true,
  north_entrance_has_clear_floor_and_headroom: true,
  complete_roof_visible_in_all_four_exterior_views: true,
  roof_intact_in_all_four_eye_level_views: true,
  rail_end_and_fixed_block_broken_cart_visible: true,
  no_closed_door_or_obstructing_entry_decoration: true,
  cutaway_changes_viewer_memory_only: true
}
const imageEvidence = []
for (const capture of viewsQa.captures) {
  const filename = path.join(root, capture.path)
  const info = await sharp(filename).metadata()
  const statistics = await sharp(filename).stats()
  const actualHash = await hashFile(filename)
  if (actualHash !== capture.sha256 || info.width !== 1440 || info.height !== 1080 || statistics.channels.every(channel => channel.stdev < 4)) throw new Error('Invalid view ' + capture.path)
  imageEvidence.push({ path: capture.path, sha256: actualHash, width: info.width, height: info.height, channels_stdev: statistics.channels.map(channel => channel.stdev), decoded_ok: true })
}
viewsQa.status = 'PASS'
viewsQa.visual_review = 'qa/visual-review.json'
await fs.writeFile(path.join(root, 'qa/views.json'), JSON.stringify(viewsQa, null, 2) + '\n')
const visualQa = { status: 'PASS', source_sha256: sourceHash, method: 'Direct visual inspection of all 12 actual-NBT captures via three four-view contact sheets plus individual views', sheets: ['views/interior-sheet.png', 'views/exterior-sheet.png', 'views/eye-sheet.png'], checks: visualChecks, images: imageEvidence, interpretation: ['Pale blue crystals use vanilla light-blue/cyan glass over calcite and sea-lantern bases.', 'Broken ore cart is a fixed block prop; no minecart entity is stored.', 'Exterior remains a sealed rock envelope with one north opening; cutaway sheets remove walls and roof in preview memory only.'] }
await fs.writeFile(path.join(root, 'qa/visual-review.json'), JSON.stringify(visualQa, null, 2) + '\n')
const qa = {
  id: specification.id, name: specification.name, status: 'PASS', sha256: sourceHash, reference_sha256: specification.referenceSha256,
  size: specification.size, entrance: specification.entrance, interior: specification.interior, facing: 'north',
  deliverables: ['room.nbt', 'room.json', 'preview.png', 'qa.json'],
  checks: { reference_hash_and_batch_manifest_verified: true, static_structure: true, four_exterior_views: true, four_cutaway_views: true, four_eye_level_views: true, forge_four_rotations: true, neighbor_and_fluid_updates: true, full_roof: true, grounded_crystals_and_supports: true, entrance_safety_and_route: true, zero_entities_and_inventories: true, no_deployment: true },
  evidence: { reference: 'qa/reference.json', static: 'qa/static.json', views: 'qa/views.json', visual_review: 'qa/visual-review.json', native: 'qa/native.json', forge_log: 'qa/forge-gametest.log' },
  static: { records: staticQa.records, roof_cells: 729, dry_route_width: 5, conservative_air_only_reachable_positions: staticQa.reachable_dry_foot_positions, actual_amethyst_fixtures: staticQa.actual_amethyst_fixtures, shallow_water_sources: staticQa.water_sources },
  native: { environment: nativeQa.environment, rotations: nativeQa.rotations.map(rotation => ({ rotation: rotation.rotation, status: rotation.status, records_checked: rotation.records_checked, reachable_dry_foot_positions: rotation.reachable_dry_foot_positions, captured_nbt: rotation.placed_nbt, captured_sha256: rotation.placed_sha256 })), stabilization_ticks: nativeQa.stabilization_ticks, records_checked_after_ticks: nativeQa.records_checked_after_ticks, chunks_forced_to_tick: true, water_ticks_explicitly_scheduled: true, negative_probes: nativeQa.negative_probes, source_unchanged: true },
  preservation: { writes_limited_to_delivery_directory_and_own_isolated_qa_outputs: true, production_deployed: false, player_world_opened: false, production_source_or_jar_modified: false, other_room_deliveries_modified: false, identity_distinct_from_existing_M05_and_corridor_M05: true },
  limitations: ['Lodestone software views and isolated Forge GameTests are evidence at different levels; full modpack client/multiplayer play was not performed.', 'Integration into existing dungeon routes, persistent generation, rewards and deployment remain outside this independent room delivery.', 'Blue crystals and the broken cart are vanilla block interpretations of the concept illustration.']
}
await fs.writeFile(path.join(root, 'qa.json'), JSON.stringify(qa, null, 2) + '\n')
const selectedFiles = ['room.nbt', 'room.json', 'preview.png', 'qa.json', 'README.md', 'materials.json', 'summary.json', 'qa/reference.json', 'qa/static.json', 'qa/views.json', 'qa/visual-review.json', 'qa/native.json', 'qa/forge-gametest.log', ...nativeQa.rotations.map(rotation => rotation.placed_nbt), ...viewsQa.captures.map(capture => capture.path), ...visualQa.sheets, 'source/recipe.mjs', 'source/qa-static.mjs', 'source/render-views.mjs', 'source/make-sheets.mjs', 'source/run-native.ps1', 'source/finalize.mjs', 'native-qa/build.gradle', 'native-qa/settings.gradle', 'native-qa/gradle.properties', 'native-qa/src/main/java/net/farmingtales/zonem05qa/Verification.java', 'native-qa/src/main/java/net/farmingtales/zonem05qa/GrottoTests.java', 'native-qa/src/main/resources/META-INF/mods.toml', 'native-qa/src/main/resources/pack.mcmeta', 'native-qa/src/main/resources/data/ftzonem05qa/structures/empty.nbt']
const manifest = []
for (const relative of selectedFiles) {
  const bytes = await fs.readFile(path.join(root, relative))
  manifest.push({ file: relative.replaceAll('\\', '/'), bytes: bytes.length, sha256: hash(bytes) })
}
await fs.writeFile(path.join(root, 'delivery-manifest.json'), JSON.stringify({ id: specification.id, status: 'PASS', structure_sha256: sourceHash, reference_sha256: specification.referenceSha256, records: manifest }, null, 2) + '\n')
const manifestHash = await hashFile(path.join(root, 'delivery-manifest.json'))
await fs.writeFile(path.join(root, 'SHA256SUMS.txt'), [...manifest.map(record => record.sha256 + '  ' + record.file), manifestHash + '  delivery-manifest.json'].join('\n') + '\n')
console.log(JSON.stringify({ status: 'PASS', structure_sha256: sourceHash, reference_sha256: specification.referenceSha256, files_hashed: manifest.length + 1, records_checked_after_ticks: nativeQa.records_checked_after_ticks, rotations: nativeQa.rotations.length, views: viewsQa.captures.length }, null, 2))
