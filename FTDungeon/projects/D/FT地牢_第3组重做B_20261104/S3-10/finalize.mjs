import fs from 'node:fs/promises'
import path from 'node:path'
import crypto from 'node:crypto'
import { fileURLToPath } from 'node:url'

const root = path.dirname(fileURLToPath(import.meta.url))
const hash = async file => crypto.createHash('sha256').update(await fs.readFile(file)).digest('hex')
const json = async name => JSON.parse(await fs.readFile(path.join(root, name), 'utf8'))
const sha256 = await hash(path.join(root, 'room.nbt'))
const staticQA = await json('static-qa.json')
const native = await json('native-qa.json')
const views = await json('view-map.json')
const materials = await json('materials.json')
const containers = await json('container-check.json')
const room = await json('room.json')
const fragments = await json('fragments.json')
const cut = await json('cutaway-proof.json')
if (fragments.status !== 'PASS' || fragments.source_sha256 !== sha256 || fragments.coordinate_coverage !== 198025 || fragments.state_mismatches || fragments.block_entity_mismatches || fragments.duplicate_coordinates || fragments.fragments.length !== 4) throw new Error('Fragment reassembly failure')
for (const fragment of fragments.fragments) if (fragment.size.some(axis => axis > 48) || fragment.sha256 !== await hash(path.join(root, fragment.file))) throw new Error('Fragment file mismatch')
if (fragments.reassembled_sha256 !== await hash(path.join(root, 'reassembled.nbt')) || native.fragment_native_template_checks?.length !== 5 || cut.sha256 !== sha256 || cut.preview_sha256 !== staticQA.preview_sha256 || !cut.block_entities_unchanged) throw new Error('Fragment native/cut evidence mismatch')
for (const artifact of [...fragments.fragments, { file: 'reassembled.nbt', sha256: fragments.reassembled_sha256, records: fragments.reassembled_records }]) {
  const nativeCheck = native.fragment_native_template_checks.find(check => check.file === path.basename(artifact.file))
  if (!nativeCheck || nativeCheck.template !== 'PASS' || nativeCheck.sha256 !== artifact.sha256 || nativeCheck.records !== artifact.records) throw new Error(`Stale native fragment evidence ${artifact.file}`)
}
if (materials.sha256 !== sha256 || containers.sha256 !== sha256 || room.reference_sha256 !== await hash(room.reference)) throw new Error('Materials/container/reference source mismatch')
if (native.container_validation?.single_chest !== 1 || !native.container_validation.empty_27_slots_and_native_unblocked || native.block_entities_native_load_save?.length !== 1) throw new Error('Native container evidence failed')
if ([staticQA.sha256, native.sha256, views.final_nbt_sha256].some(value => value !== sha256)) throw new Error('Evidence hash mismatch')
if (staticQA.status !== 'PASS' || native.status !== 'PASS' || native.survival_failures.length || native.unsupported.length || native.routes.length !== 30 || native.routes.some(route => !route.reachable) || views.page_errors.length || views.views.length !== 8) throw new Error('Incomplete or failed checks')
if (views.cutaway_sha256 !== await hash(path.join(root, 'preview-only/preview-only-cutaway.nbt')) || views.cutaway_sha256 !== staticQA.preview_sha256) throw new Error('Cutaway mismatch')
for (const view of views.views) if (view.sha256 !== await hash(view.file) || view.final_nbt_sha256 !== sha256 || view.source_sha256 !== (view.derived_cutaway ? views.cutaway_sha256 : sha256)) throw new Error(`Stale PNG ${view.file}`)
const interior = views.additional_full_nbt_interior
if (!interior || interior.source_sha256 !== sha256 || interior.sha256 !== await hash(interior.file) || interior.geometry_modified) throw new Error('Missing full-NBT interior')
if (views.additional_leg_cameras?.length !== 7) throw new Error('Missing corner leg/bridge evidence')
for (const camera of views.additional_leg_cameras) if (camera.source_sha256 !== sha256 || camera.sha256 !== await hash(camera.file) || camera.geometry_modified) throw new Error('Stale full-NBT leg evidence')
const predecessor = 'D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/S3-09-independent/independent-details.json'
const gate = JSON.parse(await fs.readFile(predecessor, 'utf8'))
if (gate.status !== 'PASS' || await hash(path.join(root, '../S3-09/room.nbt')) !== 'e480a9df20240c77de5d7bb56434636af3d2fad5460f9ffd62477822e67baea0') throw new Error('Predecessor not accepted or changed')
const preserved = {}
for (const previous of ['S3-01', 'S3-02', 'S3-03', 'S3-04', 'S3-05', 'S3-06', 'S3-07', 'S3-08', 'S3-09']) {
  const previousRoot = path.join(root, '..', previous)
  const manifest = JSON.parse(await fs.readFile(path.join(previousRoot, 'SHA256.json'), 'utf8'))
  for (const [name, expected] of Object.entries(manifest)) if (await hash(path.join(previousRoot, name)) !== expected) throw new Error(`Changed accepted evidence ${previous}/${name}`)
  preserved[previous] = { sha256: await hash(path.join(previousRoot, 'room.nbt')), files_verified: Object.keys(manifest).length }
}
const provenance = {}
for (const file of [predecessor, 'D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/vanilla-block-tags.json', 'D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/vanilla-tag-provenance.json', 'D:/ft工程文件/锈铜宝库地牢_20261101/qa/vanilla-1.20.1-mapped.jar', 'D:/ft工程文件/霜雪花园地牢_20261101/build/classpath/runData_minecraftClasspath.txt']) provenance[file] = await hash(file)
const write = async (name, data) => fs.writeFile(path.join(root, name), JSON.stringify(data, null, 2))
await write('validation-provenance.json', { dependencies: provenance, accepted_evidence_preserved: preserved })
await write('qa.json', { sha256, static: staticQA.status, native: native.status, all_records_survival_checked: native.survival_checked_all_records, vanilla_tags_bound: native.tag_count, body_routes: native.routes.map(route => ({ id: route.id, reachable: route.reachable })), reachable_grid_positions: native.reachable_grid_positions, lodestone_views: 8, extra_full_nbt_local_interiors: 8, cutaway: 'exact air-only projection of final NBT', palette: staticQA.palette, block_entities: staticQA.block_entities, light_propagation_world_placement_ticks_gameplay: 'NOT_RUN', independent_fidelity_and_repetition: 'PENDING' })
await write('ACCEPTANCE.json', { group: 3, image: 10, sha256, size: [89, 25, 89], status: 'AWAITING_INDEPENDENT_REVIEW', predecessor_accepted: 'e480a9df20240c77de5d7bb56434636af3d2fad5460f9ffd62477822e67baea0', technical_self_checks: 'PASS', independent_fidelity: 'PENDING', independent_repetition: 'PENDING', world_ticks_gameplay: 'NOT_RUN', deployment: 'NOT_DEPLOYED', next_image_started: false })
const manifest = {}
async function collect(relative) {
  for (const entry of await fs.readdir(path.join(root, relative), { withFileTypes: true })) {
    const name = path.join(relative, entry.name).replaceAll('\\', '/')
    if (name === 'SHA256.json') continue
    if (entry.isDirectory()) await collect(name)
    else manifest[name] = await hash(path.join(root, name))
  }
}
await collect('')
await write('SHA256.json', manifest)
for (const [name, expected] of Object.entries(manifest)) if (await hash(path.join(root, name)) !== expected) throw new Error(`Readback mismatch ${name}`)
console.log(JSON.stringify({ sha256, manifest_entries: Object.keys(manifest).length, manifest_readback: 'PASS', previous_nine_unchanged: true, candidate: 'AWAITING_INDEPENDENT_REVIEW' }, null, 2))
