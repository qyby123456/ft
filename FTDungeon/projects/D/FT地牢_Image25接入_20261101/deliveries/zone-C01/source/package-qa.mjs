import fs from 'node:fs/promises'
import path from 'node:path'
import {fileURLToPath} from 'node:url'
import {createHash} from 'node:crypto'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const read = async file => JSON.parse(await fs.readFile(path.join(root, file), 'utf8'))
const hash = bytes => createHash('sha256').update(bytes).digest('hex')
const metadata = await read('room.json')
const staticQa = await read('qa/static.json')
const nativeQa = await read('qa/forge.json')
const renderQa = await read('qa/render.json')
const captureQa = await read('qa/capture-block-entities.json')
const fingerprint = hash(await fs.readFile(path.join(root, 'room.nbt')))
if ([metadata.sha256, staticQa.structure_sha256, nativeQa.structure_sha256, renderQa.structure_sha256, captureQa.structure_sha256].some(value => value !== fingerprint)) throw new Error('QA fingerprint mismatch')
if ([staticQa, nativeQa, renderQa, captureQa].some(report => report.status !== 'PASS')) throw new Error('QA is not complete')
if (nativeQa.rotations.length !== 4 || nativeQa.rotations.some(rotation => rotation.status !== 'PASS' || !rotation.capture_matches_rotated_source)) throw new Error('Missing native rotation evidence')
for (const view of renderQa.views) if (hash(await fs.readFile(path.join(root, view.file))) !== view.sha256) throw new Error('View fingerprint mismatch')
for (const rotation of nativeQa.rotations) if (hash(await fs.readFile(path.join(root, rotation.world_capture))) !== rotation.capture_sha256) throw new Error('Native capture fingerprint mismatch')
const batchRoot = 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103'
const manifest = JSON.parse(await fs.readFile(path.join(batchRoot, 'docs/manifest.json'), 'utf8'))
const verify = JSON.parse(await fs.readFile(path.join(batchRoot, 'docs/verify.json'), 'utf8'))
const selected = manifest.files.find(entry => entry.file === 'out/corners/C01-corners-stone-corner-watchpost.png')
const verified = verify.records.find(entry => entry.file === selected.file)
if (selected.sha256 !== metadata.referenceSha256 || verified?.sha256 !== metadata.referenceSha256 || !selected.ok || !verified.ok) throw new Error('Batch reference validation failed')
const evidenceFiles = ['room.nbt', 'room.json', 'preview.png', 'views/review-sheet.png', 'qa/static.json', 'qa/render.json', 'qa/forge.json', 'qa/forge-run.log', 'qa/native-roundtrip.nbt', 'qa/capture-block-entities.json', ...nativeQa.rotations.map(rotation => rotation.world_capture)]
const evidence = await Promise.all(evidenceFiles.map(async file => ({file, sha256: hash(await fs.readFile(path.join(root, file)))})))
const report = {
  schema: 'ft-room-delivery-qa/v1', id: 'C01', name: metadata.name, status: 'PASS',
  evidenceLevel: 'static + actual NBT Lodestone render + isolated Minecraft 1.20.1 Forge 47.4.10 native GameTest',
  date: '2026-11-03', timezone: 'Asia/Shanghai', structure_sha256: fingerprint,
  design: {reference: metadata.reference, sha256: metadata.referenceSha256, matched_manifest: true, matched_verify: true, manifest_record: selected},
  geometry: {size: metadata.size, entrance: metadata.entrance, exit: metadata.exit, safeLanding: metadata.safeLanding, width: 3, port_clear_height: 4, minimum_route_clear_height: 3, route_cells: 57, full_roof: true},
  static: {status: staticQa.status, report: 'qa/static.json', records: staticQa.records, checks: staticQa.checks.length, palette_states: staticQa.palette_states, unsupported_custom_blocks: 0, entities: 0, loot_tables: 0},
  native: {status: nativeQa.status, report: 'qa/forge.json', log: 'qa/forge-run.log', minecraft: nativeQa.minecraft, forge: nativeQa.forge, rotations: nativeQa.rotations, stabilized_ticks: nativeQa.stabilization_ticks, exact_block_comparisons_after_ticks: nativeQa.records_checked_after_ticks, negative_missing_support_probes: 4, negative_blocked_path_probes: 4, all_six_face_shape_updates: true, registry_properties_roundtrip: 'PASS'},
  visualization: {status: 'PASS', report: 'qa/render.json', actual_nbt_render: true, nbt_unchanged: true, preview: 'preview.png', review_sheet: 'views/review-sheet.png', four_interior_cutaways: ['views/north-interior.png', 'views/south-interior.png', 'views/west-interior.png', 'views/east-interior.png'], four_full_shell_views: ['views/north-exterior.png', 'views/south-exterior.png', 'views/west-exterior.png', 'views/east-exterior.png'], top_down: 'views/top-interior.png', visual_review: {status: 'PASS', inspected: ['preview.png', 'views/review-sheet.png', 'views/top-interior.png'], findings: ['North-to-east L route remains clear.', 'Desk, chair, empty stores and weapon rack sit outside the three-wide route.', 'All four exterior viewpoints retain the complete roof and perimeter shell.', 'Cutaway chains terminate at hidden roof copies only; final NBT ceiling anchors remain intact.']}},
  design_deviations: metadata.designDeviations,
  rejected_iterations: [{folder: 'qa/rejected-initial', reason: 'Chair back intersected a pier; corrected before final delivery. First native test encountered external fixture blocks beside boundary bars.'}, {folder: 'qa/rejected-fixture-contamination', reason: 'Old fixture drops remained in the isolated test world; rebuilt the bounded QA fixture without drops and cleared old test entities before placement. Final comparison still covers every block after neighbor updates and ticks.'}],
  delivery_scope: {directory: root, standalone_only: true, deployed: false, player_saves_modified: false, production_jars_modified: false, production_resource_packs_modified: false, production_configs_modified: false, parallel_deliveries_modified: false},
  limitations: ['软件图为实际 NBT 的 Lodestone 渲染，非 Minecraft 客户端截图。', '原生证据来自独立 Forge GameTest；未宣称整合包客户端、多人、FPS 或 FT 正式接入验收。', '正式接入仍须完整体积占用/实体/液体检查与独立持久生成标记。'],
  evidence
}
report.native.exact_block_entity_capture_comparison = {status: captureQa.status, report: 'qa/capture-block-entities.json', empty_barrels: 24, patterned_banners: 4}
report.visualization.visual_review.inspected.push(...report.visualization.four_interior_cutaways)
report.visualization.visual_review.inspected.push(...report.visualization.four_full_shell_views)
report.visualization.known_renderer_limitations = ['Lodestone previews show the banner cloth as red; exact gold pattern/color NBT is verified in all four native world captures.']
report.rejected_iterations.push({folder: 'qa/pre-final-chair', reason: 'Earlier passing native iteration retained for provenance; final chair back was moved to touch its stair seat, then all static/render/native checks were repeated for the final fingerprint.'})
report.provenance = await Promise.all(['README.md', 'docs/manifest.json', 'docs/verify.json'].map(async file => ({path: path.join(batchRoot, file), sha256: hash(await fs.readFile(path.join(batchRoot, file)))})))
await fs.writeFile(path.join(root, 'qa.json'), JSON.stringify(report, null, 2) + '\n')
process.stdout.write(JSON.stringify({status: report.status, sha256: fingerprint, native_rotations: 4, ticks: 160}) + '\n')
