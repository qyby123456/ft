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
if (staticQA.sha256 !== sha256 || native.sha256 !== sha256 || views.final_nbt_sha256 !== sha256) throw new Error('Evidence hash mismatch')
if (staticQA.status !== 'PASS' || native.status !== 'PASS' || native.survival_failures.length || native.unsupported.length || native.routes.some(route => !route.reachable) || views.page_errors.length || views.views.length !== 8) throw new Error('Incomplete or failed checks')
if (views.cutaway_sha256 !== await hash(path.join(root, 'preview-only/preview-only-cutaway.nbt')) || views.cutaway_sha256 !== staticQA.preview_sha256) throw new Error('Cutaway mismatch')
for (const view of views.views) if (view.sha256 !== await hash(view.file)) throw new Error(`Stale PNG ${view.file}`)
const predecessor = 'D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/S3-01-ACCEPTANCE.json'
const gate = JSON.parse(await fs.readFile(predecessor, 'utf8'))
if (gate.status !== 'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION' || gate.nbt_sha256 !== await hash(path.join(root, '../S3-01/room.nbt'))) throw new Error('Predecessor not accepted or changed')
const provenance = {}
for (const file of [predecessor, 'D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/vanilla-block-tags.json', 'D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/vanilla-tag-provenance.json', 'D:/ft工程文件/锈铜宝库地牢_20261101/qa/vanilla-1.20.1-mapped.jar', 'D:/ft工程文件/霜雪花园地牢_20261101/build/classpath/runData_minecraftClasspath.txt']) provenance[file] = await hash(file)
await fs.writeFile(path.join(root, 'validation-provenance.json'), JSON.stringify(provenance, null, 2))
await fs.writeFile(path.join(root, 'qa.json'), JSON.stringify({ sha256, static: staticQA.status, native: native.status, tagged_support_checks: native.survival_checked_all_records, vanilla_tags_bound: native.tag_count, body_routes: native.routes.map(route => ({ id: route.id, reachable: route.reachable })), lodestone_views: 8, cutaway: 'exact deletion-only projection of final NBT', palette: staticQA.palette, block_entities: staticQA.block_entities, light_propagation_world_placement_ticks_gameplay: 'NOT_RUN', independent_fidelity_and_repetition: 'PENDING' }, null, 2))
await fs.writeFile(path.join(root, 'ACCEPTANCE.json'), JSON.stringify({ group: 3, image: 2, sha256, size: [29, 17, 27], status: 'AWAITING_INDEPENDENT_REVIEW', predecessor_accepted: gate.nbt_sha256, technical_self_checks: 'PASS', independent_fidelity: 'PENDING', independent_repetition: 'PENDING', world_ticks_gameplay: 'NOT_RUN', deployment: 'NOT_DEPLOYED', next_image_started: false }, null, 2))
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
