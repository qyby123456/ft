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
if ([staticQA.sha256, native.sha256, views.final_nbt_sha256].some(value => value !== sha256)) throw new Error('Evidence hash mismatch')
if (staticQA.status !== 'PASS' || native.status !== 'PASS' || native.survival_failures.length || native.unsupported.length || native.routes.length !== 12 || native.routes.some(route => !route.reachable) || views.page_errors.length || views.views.length !== 8) throw new Error('Incomplete or failed checks')
if (views.cutaway_sha256 !== await hash(path.join(root, 'preview-only/preview-only-cutaway.nbt')) || views.cutaway_sha256 !== staticQA.preview_sha256) throw new Error('Cutaway mismatch')
for (const view of views.views) if (view.sha256 !== await hash(view.file) || view.final_nbt_sha256 !== sha256 || view.source_sha256 !== (view.derived_cutaway ? views.cutaway_sha256 : sha256)) throw new Error(`Stale PNG ${view.file}`)
const interior = views.additional_full_nbt_interior
if (!interior || interior.source_sha256 !== sha256 || interior.sha256 !== await hash(interior.file) || interior.geometry_modified) throw new Error('Missing full-NBT interior')
if (views.additional_leg_cameras?.length !== 3) throw new Error('Missing corner leg/bridge evidence')
for (const camera of views.additional_leg_cameras) if (camera.source_sha256 !== sha256 || camera.sha256 !== await hash(camera.file) || camera.geometry_modified) throw new Error('Stale full-NBT leg evidence')
const predecessor = 'D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/S3-05-independent/independent-details.json'
const gate = JSON.parse(await fs.readFile(predecessor, 'utf8'))
if (gate.status !== 'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION' || gate.sha256 !== await hash(path.join(root, '../S3-05/room.nbt'))) throw new Error('Predecessor not accepted or changed')
const preserved = {}
for (const previous of ['S3-01', 'S3-02', 'S3-03', 'S3-04', 'S3-05']) {
  const previousRoot = path.join(root, '..', previous)
  const manifest = JSON.parse(await fs.readFile(path.join(previousRoot, 'SHA256.json'), 'utf8'))
  for (const [name, expected] of Object.entries(manifest)) if (await hash(path.join(previousRoot, name)) !== expected) throw new Error(`Changed accepted evidence ${previous}/${name}`)
  preserved[previous] = { sha256: await hash(path.join(previousRoot, 'room.nbt')), files_verified: Object.keys(manifest).length }
}
const provenance = {}
for (const file of [predecessor, 'D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/vanilla-block-tags.json', 'D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/vanilla-tag-provenance.json', 'D:/ft工程文件/锈铜宝库地牢_20261101/qa/vanilla-1.20.1-mapped.jar', 'D:/ft工程文件/霜雪花园地牢_20261101/build/classpath/runData_minecraftClasspath.txt']) provenance[file] = await hash(file)
const write = async (name, data) => fs.writeFile(path.join(root, name), JSON.stringify(data, null, 2))
await write('validation-provenance.json', { dependencies: provenance, accepted_evidence_preserved: preserved })
await write('qa.json', { sha256, static: staticQA.status, native: native.status, all_records_survival_checked: native.survival_checked_all_records, vanilla_tags_bound: native.tag_count, body_routes: native.routes.map(route => ({ id: route.id, reachable: route.reachable })), reachable_grid_positions: native.reachable_grid_positions, lodestone_views: 8, extra_full_nbt_axial_interior: true, cutaway: 'exact air-only projection of final NBT', palette: staticQA.palette, block_entities: [], light_propagation_world_placement_ticks_gameplay: 'NOT_RUN', independent_fidelity_and_repetition: 'PENDING' })
await write('ACCEPTANCE.json', { group: 3, image: 6, sha256, size: [37, 19, 35], status: 'AWAITING_INDEPENDENT_REVIEW', predecessor_accepted: gate.sha256, technical_self_checks: 'PASS', independent_fidelity: 'PENDING', independent_repetition: 'PENDING', world_ticks_gameplay: 'NOT_RUN', deployment: 'NOT_DEPLOYED', next_image_started: false })
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
console.log(JSON.stringify({ sha256, manifest_entries: Object.keys(manifest).length, manifest_readback: 'PASS', previous_five_unchanged: true, candidate: 'AWAITING_INDEPENDENT_REVIEW' }, null, 2))
