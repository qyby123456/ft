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
const revision = await json('revision-diff.json')
if (revision.status !== 'PASS' || revision.after_sha256 !== sha256 || revision.outside_column_regions_changed !== 0) throw new Error('Revision regression mismatch')
if (staticQA.sha256 !== sha256 || native.sha256 !== sha256 || views.final_nbt_sha256 !== sha256) throw new Error('Evidence hash mismatch')
if (staticQA.status !== 'PASS' || native.status !== 'PASS' || native.survival_failures.length || native.unsupported.length || native.routes.some(route => !route.reachable) || views.page_errors.length || views.views.length !== 8) throw new Error('Incomplete or failed checks')
if (views.cutaway_sha256 !== await hash(path.join(root, 'preview-only/preview-only-cutaway.nbt')) || views.cutaway_sha256 !== staticQA.preview_sha256) throw new Error('Cutaway mismatch')
for (const view of views.views) if (view.sha256 !== await hash(view.file)) throw new Error(`Stale PNG ${view.file}`)
const nave = views.additional_full_nbt_interior
if (!nave || nave.source_sha256 !== sha256 || nave.sha256 !== await hash(nave.file) || nave.geometry_modified) throw new Error('Missing full-NBT interior evidence')
const predecessor = 'D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/S3-03-ACCEPTANCE.json'
const gate = JSON.parse(await fs.readFile(predecessor, 'utf8'))
if (gate.status !== 'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION' || gate.nbt_sha256 !== await hash(path.join(root, '../S3-03/room.nbt'))) throw new Error('Predecessor not accepted or changed')
for (const previous of ['S3-01', 'S3-02', 'S3-03']) {
  const previousRoot = path.join(root, '..', previous)
  const previousManifest = JSON.parse(await fs.readFile(path.join(previousRoot, 'SHA256.json'), 'utf8'))
  for (const [name, expected] of Object.entries(previousManifest)) if (await hash(path.join(previousRoot, name)) !== expected) throw new Error(`Changed accepted evidence ${previous}/${name}`)
}
const provenance = {}
for (const file of [predecessor, 'D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/vanilla-block-tags.json', 'D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/vanilla-tag-provenance.json', 'D:/ft工程文件/锈铜宝库地牢_20261101/qa/vanilla-1.20.1-mapped.jar', 'D:/ft工程文件/霜雪花园地牢_20261101/build/classpath/runData_minecraftClasspath.txt']) provenance[file] = await hash(file)
await fs.writeFile(path.join(root, 'validation-provenance.json'), JSON.stringify(provenance, null, 2))
await fs.writeFile(path.join(root, 'qa.json'), JSON.stringify({ sha256, static: staticQA.status, native: native.status, tagged_support_checks: native.survival_checked_all_records, vanilla_tags_bound: native.tag_count, body_routes: native.routes.map(route => ({ id: route.id, reachable: route.reachable })), lodestone_views: 8, cutaway: 'exact deletion-only projection of final NBT', palette: staticQA.palette, block_entities: staticQA.block_entities, light_propagation_world_placement_ticks_gameplay: 'NOT_RUN', independent_fidelity_and_repetition: 'PENDING' }, null, 2))
await fs.writeFile(path.join(root, 'ACCEPTANCE.json'), JSON.stringify({ group: 3, image: 4, sha256, size: [35, 27, 57], status: 'AWAITING_INDEPENDENT_REVIEW', predecessor_accepted: gate.nbt_sha256, technical_self_checks: 'PASS', independent_fidelity: 'PENDING', independent_repetition: 'PENDING', world_ticks_gameplay: 'NOT_RUN', deployment: 'NOT_DEPLOYED', next_image_started: false }, null, 2))
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
await fs.writeFile(path.join(root, 'SHA256.json'), JSON.stringify(manifest, null, 2))
for (const [name, expected] of Object.entries(manifest)) if (await hash(path.join(root, name)) !== expected) throw new Error(`Readback mismatch ${name}`)
console.log(JSON.stringify({ sha256, manifest_entries: Object.keys(manifest).length, manifest_readback: 'PASS', predecessor_untouched_and_accepted: true, candidate: 'AWAITING_INDEPENDENT_REVIEW' }, null, 2))
