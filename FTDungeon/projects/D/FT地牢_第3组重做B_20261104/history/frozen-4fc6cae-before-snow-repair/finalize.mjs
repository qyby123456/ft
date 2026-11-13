import fs from 'node:fs/promises'
import path from 'node:path'
import crypto from 'node:crypto'
import { fileURLToPath } from 'node:url'

const root = path.dirname(fileURLToPath(import.meta.url))
const hash = async name => crypto.createHash('sha256').update(await fs.readFile(path.join(root, name))).digest('hex')
const sha256 = await hash('room.nbt')
const qa = JSON.parse(await fs.readFile(path.join(root, 'qa.json'), 'utf8'))
const render = JSON.parse(await fs.readFile(path.join(root, 'render-check.json'), 'utf8'))
if (qa.sha256 !== sha256 || qa.native_minecraft_load.sha256 !== sha256 || render.sha256 !== sha256 || render.page_errors.length) throw new Error('Evidence mismatch')
for (const image of ['preview.png', 'preview-night.png', 'interior-01.png', 'interior-02.png', 'interior-03.png', 'interior-04.png', 'exterior-01.png', 'exterior-02.png', 'exterior-03.png', 'exterior-04.png']) {
  if ((await fs.stat(path.join(root, image))).size < 10000) throw new Error(`Missing render ${image}`)
}
const acceptance = {
  group: 3,
  image: 1,
  id: 'S3-01-REWORK-B',
  sha256,
  technical_self_check: 'PASS',
  native_registry_template_and_collision: 'PASS',
  lodestone_four_direction_exterior_and_cutaway: 'PASS',
  fidelity_self_review: 'DOCUMENTED_WITH_VANILLA_DISCRETIZATION_LIMITS',
  independent_fidelity_acceptance: 'PENDING',
  group_pairwise_repetition: 'N/A_SINGLE_NEW_STRUCTURE',
  cross_group_similarity: 'PENDING_FINAL_MATRIX',
  world_placement_ticks_and_playtest: 'NOT_RUN',
  deployment: 'NOT_DEPLOYED',
  acceptance: 'AWAITING_INDEPENDENT_REVIEW_NOT_FINAL_PASS',
  next_image_started: false,
}
await fs.writeFile(path.join(root, '..', 'ACCEPTANCE.json'), JSON.stringify(acceptance, null, 2))
const manifest = {}
for (const name of await fs.readdir(root)) {
  if (name === 'SHA256.json') continue
  if ((await fs.stat(path.join(root, name))).isFile()) manifest[name] = await hash(name)
}
manifest['preview-only/preview-only-cutaway.nbt'] = await hash('preview-only/preview-only-cutaway.nbt')
await fs.writeFile(path.join(root, 'SHA256.json'), JSON.stringify(manifest, null, 2))
console.log(JSON.stringify(acceptance, null, 2))
