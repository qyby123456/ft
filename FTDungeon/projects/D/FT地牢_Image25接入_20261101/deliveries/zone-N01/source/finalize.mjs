import assert from 'node:assert/strict'
import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createHash } from 'node:crypto'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const digest = bytes => createHash('sha256').update(bytes).digest('hex')
const read = async file => JSON.parse(await fs.readFile(path.join(root, file), 'utf8'))
const sha256 = digest(await fs.readFile(path.join(root, 'room.nbt')))
const metadata = await read('room.json')
const staticReport = await read('qa/static.json')
const nativeReport = await read('qa/forge-native.json')
const viewsReport = await read('qa/views.json')
const planReport = await read('qa/interface-plan.json')
for (const report of [metadata, staticReport, nativeReport, viewsReport, planReport]) assert.equal(report.sha256, sha256)
for (const report of [staticReport, nativeReport, viewsReport, planReport]) assert.equal(report.status, 'PASS')
assert.equal(nativeReport.rotations, 4)
assert.equal(nativeReport.stability_ticks, 400)
assert.equal(nativeReport.negative_probes_rejected.length, 7)
assert.equal(nativeReport.initial.length, 4)
assert.equal(nativeReport.after_400_ticks.length, 4)
for (const phase of [nativeReport.initial, nativeReport.after_400_ticks]) {
  for (const rotation of phase) {
    assert.equal(rotation.records, 7500)
    assert.equal(rotation.preserved_sign_block_entities, 12)
    assert.equal(rotation.spatial.ordered_port_pairs, 12)
    assert.equal(rotation.spatial.protected_route_cells, 168)
    for (const route of rotation.spatial.paths) {
      assert.equal(route.steps, route.world_path.length - 1)
      for (let step = 1; step < route.world_path.length; step++) {
        const before = route.world_path[step - 1]
        const after = route.world_path[step]
        assert.equal(after.reduce((sum, coordinate, axis) => sum + Math.abs(coordinate - before[axis]), 0), 1)
      }
    }
  }
}
for (const view of viewsReport.views) assert.equal(digest(await fs.readFile(path.join(root, view.file))), view.sha256)
assert.equal(digest(await fs.readFile(path.join(root, planReport.file))), planReport.image_sha256)
assert.deepEqual(await fs.readFile(path.join(root, 'preview.png')), await fs.readFile(path.join(root, 'preview/cutaway.png')))
const log = await fs.readFile(path.join(root, 'qa/forge-run.log'), 'utf8')
assert.ok(log.includes('N01_NATIVE_GAME_TEST_PASS: rotations=4/4; records=30000; directed_paths=48; ticks=400; sha256=' + sha256))
assert.ok(log.includes('All 1 required tests passed'))
assert.ok(log.includes('BUILD SUCCESSFUL'))
const referenceBytes = await fs.readFile(metadata.reference)
assert.equal(digest(referenceBytes), metadata.reference_sha256)
await fs.writeFile(path.join(root, 'qa/reference.json'), JSON.stringify({ status: 'PASS', source: metadata.reference, sha256: metadata.reference_sha256, lineage: staticReport.lineage, new_paid_generation: false }, null, 2) + '\n')
const visual = {
  status: 'PASS', method: 'Agent inspected cutaway, plan, four-interior, four-exterior and interface-plan images',
  original_nbt_four_interior_and_exterior: true, complete_roof: true, four_ports_clear: true,
  compass_floor_and_central_wayfinder: true, pillars_separate_from_props: true, safe_area_and_ring_visible: true,
  cutaway_note: 'Roof, upper beams, camera-facing walls and front pillar upper segment hidden only for cutaway; suspended chain endpoints are section cuts.',
  game_screenshot: false,
}
await fs.writeFile(path.join(root, 'qa/visual-review.json'), JSON.stringify({ ...visual, sha256 }, null, 2) + '\n')
const qa = {
  id: 'N01', status: 'PASS', sha256, reference_sha256: metadata.reference_sha256, size: metadata.size,
  minecraft: '1.20.1', forge: '47.4.10', data_version: 3465, static: 'PASS',
  four_direction_interior_and_exterior_views: 'PASS', forge_placement_rotations: 4,
  native_records_per_phase: 30000, stability_ticks: 400, ordered_port_paths_per_rotation: 12,
  ordered_port_paths_per_phase: 48, external_connectors_per_rotation: 4, center_safe: metadata.center_safe,
  port_aperture: [5, 5], protected_route_width: 3, protected_route_clearance: 5,
  protected_route_cells: 168, block_entities: 12, entities: 0, containers: 0,
  negative_probes_rejected: nativeReport.negative_probes_rejected, visual_review: visual,
  deployment: 'NOT_DEPLOYED', production_save_used: false, production_mods_modified: false,
  full_modpack_client_playtest: false,
  evidence: ['qa/static.json', 'qa/forge-native.json', 'qa/forge-run.log', 'qa/views.json', 'qa/interface-plan.json', 'qa/reference.json', 'qa/visual-review.json'],
  limits: ['Isolated Forge is not a complete modpack/client/multiplayer/FPS playtest.', 'Software previews are not game screenshots.', 'Explicit air requires a full rotated-volume occupancy check before any future placement.', 'N/E/S/W signs label template-local ports and rotate with the structure.', 'Safety describes geometry and inherent hazards, not runtime mob exclusion.'],
}
await fs.writeFile(path.join(root, 'qa.json'), JSON.stringify(qa, null, 2) + '\n')
const files = []
async function scan(directory) {
  for (const entry of await fs.readdir(path.join(root, directory), { withFileTypes: true })) {
    const relative = path.posix.join(directory.replaceAll('\\', '/'), entry.name)
    if (entry.isDirectory()) {
      if (relative === 'forge/.gradle' || relative === 'forge/build' || relative.startsWith('forge/run-')) continue
      await scan(relative)
    } else if (relative !== 'delivery-manifest.json') {
      const bytes = await fs.readFile(path.join(root, relative))
      files.push({ file: relative, bytes: bytes.length, sha256: digest(bytes) })
    }
  }
}
await scan('')
files.sort((left, right) => left.file.localeCompare(right.file))
const manifest = { id: 'N01', status: 'PASS', deployment: 'NOT_DEPLOYED', sha256, reference_sha256: metadata.reference_sha256, excludes: ['forge/.gradle', 'forge/build', 'forge/run-*'], files }
await fs.writeFile(path.join(root, 'delivery-manifest.json'), JSON.stringify(manifest, null, 2) + '\n')
for (const file of files) assert.equal(digest(await fs.readFile(path.join(root, file.file))), file.sha256)
console.log(JSON.stringify({ status: 'PASS', id: 'N01', sha256, files: files.length, native_rotations: 4, stability_ticks: 400, directed_paths_per_phase: 48, deployment: 'NOT_DEPLOYED' }, null, 2))
