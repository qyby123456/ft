import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createHash } from 'node:crypto'
import { specification } from '../recipe.mjs'

const root = path.dirname(path.dirname(fileURLToPath(import.meta.url)))
const digest = bytes => createHash('sha256').update(bytes).digest('hex')
const referenceBytes = await fs.readFile(specification.reference)
if (digest(referenceBytes) !== specification.referenceSha256) throw new Error('Reference SHA-256 mismatch')
const sha256 = digest(await fs.readFile(path.join(root, 'room.nbt')))
const metadata = {
  id: specification.id, source_id: specification.sourceId, name: specification.name,
  type: 'straight_corridor', length_class: 'short', size: specification.size,
  entrance: specification.entrance, interior: specification.interior, exit: specification.exit,
  facing: 'north', exit_facing: 'south', sha256,
  reference: specification.reference, reference_sha256: specification.referenceSha256,
  data_version: 3465, minecraft_version: '1.20.1',
  entrances: [
    { position: specification.entrance, facing: 'north', clear_width: 7, clear_height: 6 },
    { position: specification.exit, facing: 'south', clear_width: 7, clear_height: 6 },
  ],
  protected_walkway: { from: [6, 1, 0], to: [8, 3, 15], support_y: 0 },
  reference_adaptations: [
    'Reference end rune door becomes an open south arch with crimson lintel: through-corridor, never a dead end.',
    'Fictional dark iron is vanilla blackstone/deepslate/obsidian; gold trim and crimson motifs are solid block inlays.',
    'Four decorative empty iron lantern cages; no mobs, loot, live fire blocks, liquids or mod dependencies.',
  ],
  placement_policy: 'Candidate only. Explicit air is not permission to clear: receiving core must preflight the entire volume including air, reject occupied/fluid/entity volumes, and preserve per-instance generation markers.',
  deployed: false, production_save_modified: false,
}
await fs.writeFile(path.join(root, 'room.json'), `${JSON.stringify(metadata, null, 2)}\n`)
await fs.mkdir(path.join(root, 'qa'), { recursive: true })
await fs.mkdir(path.join(root, 'reference'), { recursive: true })
await fs.writeFile(path.join(root, 'reference', 'fingerprint.json'), `${JSON.stringify({ ...specification, verified: true, bytes: referenceBytes.length }, null, 2)}\n`)
for (const filename of ['SPEC.md', 'README.md', 'manifest.json']) {
  const source = path.join(path.dirname(path.dirname(specification.reference)), filename)
  await fs.copyFile(source, path.join(root, 'reference', filename))
}
console.log(JSON.stringify({ status: 'PREPARED', id: metadata.id, sha256, size: metadata.size }))
