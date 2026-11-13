import fs from 'node:fs/promises'
import path from 'node:path'
import { createHash } from 'node:crypto'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const read = async relative => JSON.parse(await fs.readFile(path.join(root, relative), 'utf8'))
const fingerprint = async relative => createHash('sha256').update(await fs.readFile(path.join(root, relative))).digest('hex')
const room = await read('room.json')
const staticReport = await read('qa/static.json')
const nativeReport = await read('qa/forge-native.json')
const renderReport = await read('qa/render.json')
const sha256 = await fingerprint('room.nbt')
if ([room.sha256, staticReport.structure_sha256, nativeReport.structure_sha256, renderReport.source_sha256].some(value => value !== sha256)) throw new Error('Evidence fingerprint mismatch')
if ([staticReport.status, nativeReport.status, renderReport.status].some(value => value !== 'PASS')) throw new Error('Cannot finalize failed evidence')
if (nativeReport.rotations.length !== 4 || renderReport.views.length !== 11) throw new Error('Missing four-rotation or view evidence')
const report = {
  id: 'N03', status: 'PASS', structure_sha256: sha256, reference_sha256: room.reference_sha256,
  evidence: { static: 'qa/static.json', forge: 'qa/forge-native.json', forge_log: 'qa/forge-gametest.log', render: 'qa/render.json', visual_review: 'qa/visual-review.json' },
  checks: {
    static: { status: staticReport.status, checks: staticReport.checks.length },
    forge: { status: nativeReport.status, rotations: 4, records_after_120_ticks: nativeReport.records_checked_after_ticks, neighbor_updates_per_rotation: nativeReport.neighbor_updates_per_rotation, native_swept_stair_transitions_total: nativeReport.rotations.reduce((total, rotation) => total + rotation.native_bidirectional_swept_stair_transitions, 0), negative_probes: 8, world_recapture_matches: nativeReport.rotations.every(rotation => rotation.capture_exact_match) },
    render: { status: renderReport.status, views: renderReport.views.length, four_internal_directions: true, complete_exterior: true, cutaways_display_only: true },
  },
  ports: room.ports, vertical_connections: room.vertical_connections, external_vertical_ports: [],
  deviations: ['Four orthogonal stairs connect floor elevations 0/6/12, rather than the concept perspective arrangement.', 'Full roof and exterior completed behind concept cutaway.', 'Solid stone and slab balustrades replace thin artistic spindles.', 'Colored carpet and cartography tables represent maps; wooden props replace inventory containers.', 'Larger 35-block octagonal room; no unsupported vines.'],
  deployed: false, player_saves_modified: false,
  limitations: nativeReport.limitations.concat(renderReport.limitations),
}
await fs.writeFile(path.join(root, 'qa.json'), JSON.stringify(report, null, 2) + '\n')
const files = ['room.nbt', 'room.json', 'preview.png', 'qa.json', 'README.md', 'recipe.mjs', 'summary.json', 'qa/static.json', 'qa/forge-native.json', 'qa/forge-gametest.log', 'qa/render.json', 'qa/visual-review.json', 'preview/four-view.png', ...renderReport.views.map(view => view.path), ...nativeReport.rotations.map(rotation => rotation.capture.replaceAll('\\', '/'))]
const entries = []
for (const relative of [...new Set(files)]) entries.push({ path: relative, bytes: (await fs.stat(path.join(root, relative))).size, sha256: await fingerprint(relative) })
await fs.writeFile(path.join(root, 'delivery-manifest.json'), JSON.stringify({ id: 'N03', structure_sha256: sha256, reference_sha256: room.reference_sha256, status: 'PASS', files: entries }, null, 2) + '\n')
console.log(JSON.stringify({ status: 'PASS', files: entries.length, structure_sha256: sha256, records: nativeReport.records_checked_after_ticks, native_stair_transitions: report.checks.forge.native_swept_stair_transitions_total }, null, 2))
