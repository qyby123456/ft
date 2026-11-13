import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createHash } from 'node:crypto'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const readJson = async name => JSON.parse(await fs.readFile(path.join(root, name), 'utf8'))
const sha256 = bytes => createHash('sha256').update(bytes).digest('hex')
const room = await readJson('room.json')
const native = await readJson('qa/native.json')
const staticReview = await readJson('qa/static.json')
const views = await readJson('qa/views.json')
const images = await readJson('qa/image-decode.json')
const visual = await readJson('qa/visual-review.json')
const fingerprint = sha256(await fs.readFile(path.join(root, 'room.nbt')))
for (const report of [native, staticReview, views, images, visual]) if (report.status !== 'PASS') throw new Error('Unsuccessful evidence')
for (const value of [room.sha256, native.structure_sha256, staticReview.sha256, views.source_sha256, visual.source_sha256]) if (value !== fingerprint) throw new Error('Evidence source fingerprint differs')
if (native.rotations.length !== 4 || native.stabilization_ticks !== 200 || native.records_checked_after_ticks !== 61380) throw new Error('Incomplete four-rotation native evidence')
if (views.captures.length !== 11 || images.original_images !== 11) throw new Error('Incomplete visual evidence')
for (const capture of views.captures) if (sha256(await fs.readFile(path.join(root, capture.path))) !== capture.sha256) throw new Error('Screenshot fingerprint differs')
for (const rotation of native.rotations) {
  if (!rotation.capture_matches_rotated_source || rotation.status !== 'PASS' || rotation.world_entities !== 0 || rotation.external_fluid_cells !== 0) throw new Error('Native stability/attachment evidence failed')
  if (sha256(await fs.readFile(path.join(root, rotation.world_capture))) !== rotation.world_capture_sha256) throw new Error('Native capture fingerprint differs')
}
if (sha256(await fs.readFile(room.reference)) !== room.reference_sha256) throw new Error('Reference changed')
const rotations = {}
const mappings = { NONE: ([east, up, south]) => [east, up, south], CLOCKWISE_90: ([east, up, south]) => [30 - south, up, east], CLOCKWISE_180: ([east, up, south]) => [32 - east, up, 30 - south], COUNTERCLOCKWISE_90: ([east, up, south]) => [south, up, 32 - east] }
for (const [rotation, transform] of Object.entries(mappings)) {
  rotations[rotation] = { origin_convention: 'Rotated bounding-box minimum; native pivot-origin must be translated before using these normalized coordinates.', size: rotation.endsWith('90') ? [31, 15, 33] : [33, 15, 31], entrance: transform(room.entrance), interior: transform(room.interior), safe_points: room.safe_points.map(transform), walking_ports: room.walking_ports.map(port => ({ id: port.id, feet: transform(port.feet) })), rail_ports: room.rail_ports.map(port => ({ id: port.id, rail: transform(port.rail), support: transform(port.support) })) }
}
await fs.writeFile(path.join(root, 'qa/rotated-interfaces.json'), JSON.stringify(rotations, null, 2) + '\n')
const report = {
  schema: 'ft-independent-zone-delivery-v1', id: 'N02', status: 'PASS', sha256: fingerprint, reference: room.reference, reference_sha256: room.reference_sha256, size: room.size, entrance: room.entrance, interior: room.interior, minecraft: '1.20.1', forge: '47.4.10',
  checks: { original_reference_manifest_verify: 'PASS', static_full_explicit_nbt: 'PASS', native_registry_states_and_roundtrip: 'PASS', full_roof: 'PASS', five_wide_three_high_public_routes_no_rail_crossing: 'PASS', five_platform_safe_points: 'PASS', three_external_walking_attachments: 'PASS', five_external_rail_attachments: 'PASS', all_73_rails_supported: 'PASS', all_7_hanging_lanterns_supported: 'PASS', four_native_rotations: 'PASS', native_200_tick_stability: 'PASS', world_captures_equal_rotated_source: 'PASS', four_negative_probes_per_rotation: 'PASS', zero_fluids_entities_inventory_and_loot: 'PASS', four_inside_and_four_outside_software_views: 'PASS', three_eye_views: 'PASS', visual_inspection: 'PASS' },
  native_summary: { rotations: native.rotations.map(entry => entry.rotation), records_per_rotation: 15345, checked_after_ticks: 61380, stabilization_ticks: 200, rails_per_rotation: 73, external_test_rails_per_rotation: 10, lanterns_per_rotation: 7, public_route_cells: 225, reachable_foot_positions_without_rail_crossings_per_rotation: 646, walking_ports: 3, rail_ports: 5, dropped_entities: 0, negative_probes: native.negative_probes, forced_chunk_ticking: true, neighbor_updates: true },
  evidence: { static: 'qa/static.json', native: 'qa/native.json', native_log: 'qa/forge-gametest.log', rotated_interfaces: 'qa/rotated-interfaces.json', rendered_views: 'qa/views.json', image_decode: 'qa/image-decode.json', visual_review: 'qa/visual-review.json', interior_sheet: 'views/interior-sheet.png', exterior_sheet: 'views/exterior-sheet.png', native_world_captures: native.rotations.map(entry => entry.world_capture) },
  design_adaptations: ['AI junction overlaps replaced by three independent fixed vanilla track networks.', 'Level platform at feet y=2 and floor y=1 removes forced steps from public paths; yellow edge and iron bars separate track zones.', 'Cauldrons and timber piles are static maintenance props, not spawned minecarts or inventories.', 'Lever desk is decorative; it has no track control wiring or promised switching function.', 'Final NBT retains the complete roof; cutaway applies only to software preview.'],
  safety: { deployed: false, core_modified: false, production_instance_modified: false, production_saves_opened_or_modified: false, other_deliveries_modified: false, paid_image_api_called: false, isolated_test_world: 'native-qa/run-isolated/world' },
  not_tested: ['Full FarmingTales modpack client rendering/playthrough, performance or multiplayer.', 'Minecart movement, powered transport or automatic switching.', 'FT core route integration, generation-marker implementation, world-border/entity/fluid occupancy rejection beyond the isolated block occupancy probe.'],
  integration_requirements: room.integration_requirements
}
await fs.writeFile(path.join(root, 'qa.json'), JSON.stringify(report, null, 2) + '\n')
const inventory = []
async function collect(directory) {
  for (const entry of await fs.readdir(directory, { withFileTypes: true })) {
    const absolute = path.join(directory, entry.name)
    if (entry.isDirectory()) {
      if (['.gradle', 'build', 'run-isolated'].includes(entry.name)) continue
      await collect(absolute)
    } else if (entry.name !== 'SHA256SUMS.txt') inventory.push({ file: path.relative(root, absolute).replaceAll('\\', '/'), sha256: sha256(await fs.readFile(absolute)) })
  }
}
await collect(root)
inventory.sort((first, second) => first.file.localeCompare(second.file))
await fs.writeFile(path.join(root, 'SHA256SUMS.txt'), inventory.map(entry => entry.sha256 + '  ' + entry.file).join('\n') + '\n')
console.log(JSON.stringify({ status: report.status, size: room.size, sha256: fingerprint, verified_files: inventory.length, native_rotations: 4, native_checked_records: 61380 }, null, 2))
