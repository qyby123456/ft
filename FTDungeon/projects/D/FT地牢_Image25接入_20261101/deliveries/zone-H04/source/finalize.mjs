import assert from 'node:assert/strict'
import fs from 'node:fs/promises'
import path from 'node:path'
import { createHash } from 'node:crypto'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const digestFile = async relative => createHash('sha256').update(await fs.readFile(path.join(root, relative))).digest('hex')
const readReport = async name => JSON.parse(await fs.readFile(path.join(root, 'qa', name), 'utf8'))
const digest = await digestFile('room.nbt')
const metadata = JSON.parse(await fs.readFile(path.join(root, 'room.json'), 'utf8'))
assert.equal(metadata.sha256, digest)
const reports = {}
for (const name of ['static.json', 'forge-native.json', 'views.json', 'composites.json', 'visual-review.json']) {
  const report = await readReport(name)
  assert.equal(report.status, 'PASS', name)
  assert.equal(report.sha256, digest, 'Stale evidence in ' + name)
  reports[name] = report
}
const native = reports['forge-native.json']
assert.equal(native.rotations, 4)
assert.equal(native.after_400_ticks.length, 4)
assert.equal(native.negative_probes_rejected.length, 6)
assert.equal(native.production_save_used, false)
assert.equal(native.full_modpack_playtest, false)
for (const row of native.after_400_ticks) {
  assert.equal(row.records, 28083)
  assert.equal(row.continuous_grounded_pillars, 4)
  assert.equal(row.closed_roof_cells, 1221)
  assert.equal(row.unwanted_entities, 0)
  assert.equal(row.sealed_source_water, 40)
  assert.equal(row.spatial.ports_3x4, 4)
  assert.equal(row.spatial.stair_collision_samples, 82)
}
const views = reports['views.json']
assert.equal(views.views.length, 14)
assert.deepEqual(views.screenshot_size, [1600, 1100])
assert.equal(views.browser_errors.length, 0)
for (const item of [...views.views, ...reports['composites.json'].images]) assert.equal(await digestFile(item.file), item.sha256, 'View fingerprint changed: ' + item.file)
const logs = (await fs.readdir(path.join(root, 'qa'))).filter(name => /^forge-.*[.]log$/.test(name)).sort()
const finalLog = 'qa/' + logs.at(-1)
const logBytes = await fs.readFile(path.join(root, finalLog))
const logText = logBytes.toString(logBytes[0] === 255 && logBytes[1] === 254 ? 'utf16le' : 'utf8')
assert(logText.includes('H04_NATIVE_GAME_TEST_PASS: rotations=4/4; records=112332; ticks=400; sha256=' + digest), 'Final Forge log fingerprint mismatch')
assert(logText.includes('BUILD SUCCESSFUL') && logText.includes('All 1 required tests passed'))
const report = {
  id: 'H04', name: '柱列中庭', status: 'PASS', sha256: digest,
  reference_sha256: metadata.reference_sha256, size: metadata.size,
  entrance: metadata.entrance, interior: metadata.interior, facing: 'north',
  evidence_levels: { static: 'PASS', actual_nbt_software_views: 'PASS', agent_visual_review: 'PASS', forge_4_rotations_400_ticks: 'PASS', full_modpack_client_playtest: 'NOT_RUN', production_deployment: 'NOT_PERFORMED' },
  checks: {
    grounded_independent_pillars: 4, uninterrupted_pillar_to_beam_to_roof: true,
    complete_roof_cells: 1221, complete_foundation_and_floor: true,
    dry_ground_reachable_nodes: 668, upper_reachable_nodes: 256,
    clear_3_by_4_ports: 4, entrance_safe_point_two_way: true,
    stairs_count: 2, stairs_width: 3, stairs_steps: 8,
    native_player_collision_size: [0.6,1.8], stair_samples_each_rotation: 82,
    source_water_blocks: 40, sealed_fluid_faces: 200,
    supported_lanterns: 26, roof_anchored_pendants: 5, decorative_banners: 16,
    non_air_blocks: 12080, records_per_rotation: 28083, total_rotation_records: 112332,
    state_unchanged_after_ticks: 400, full_neighbor_updates: true,
    negative_probes_rejected: native.negative_probes_rejected,
    generated_entities: 0, unexpected_block_entities: 0, gameplay_loot: false,
    render_views: 14, complete_exterior_directions: 4, cutaway_interior_directions: 4, unchanged_template_eye_directions: 4,
    browser_errors: 0,
  },
  reports: Object.keys(reports).map(name => 'qa/' + name), forge_log: finalLog,
  source_fidelity_notes: ['Complete roof and four walls reconstructed from the cutaway reference', 'Pillar spacing adjusted to keep two-wide dry circulation around the basin', 'Upper fountain water curtain expressed with cyan stained glass; only the sealed basin contains water', 'Native banner Patterns data preserved; software renderer may omit the golden pattern overlay'],
  isolation: { workspace: root, production_instance_modified: false, player_save_used: false, other_delivery_modified: false, new_paid_image_generation: false },
}
await fs.writeFile(path.join(root, 'qa.json'), JSON.stringify(report, null, 2) + '\n')
const files = ['room.nbt','room.json','summary.json','README.md','preview.png','qa.json','preview/index.html','preview/interactive.html', ...Object.keys(reports).map(name=>'qa/'+name), 'qa/reference-evidence.json', finalLog]
files.push(...views.views.map(item=>item.file), ...reports['composites.json'].images.map(item=>item.file))
for (const directory of ['source', 'forge/src']) {
  async function visit(relative) {
    const entries = await fs.readdir(path.join(root, relative), { withFileTypes: true })
    for (const entry of entries) {
      const filename = relative + '/' + entry.name
      if (entry.isDirectory()) await visit(filename)
      else if (!filename.includes('__pycache__')) files.push(filename)
    }
  }
  await visit(directory)
}
files.push('forge/build.gradle','forge/settings.gradle','forge/gradle.properties')
const lines = []
for (const filename of [...new Set(files)].sort()) lines.push(await digestFile(filename) + '  ' + filename)
await fs.writeFile(path.join(root,'SHA256SUMS.txt'), lines.join('\n') + '\n')
console.log(JSON.stringify({ status: 'PASS', sha256: digest, manifest_files: lines.length, final_log: finalLog, qa: 'qa.json' }, null, 2))
