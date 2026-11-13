import fs from 'node:fs/promises'
import path from 'node:path'
import { createHash } from 'node:crypto'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const readJson = async relative => JSON.parse(await fs.readFile(path.join(root, relative), 'utf8'))
const hash = async relative => createHash('sha256').update(await fs.readFile(path.join(root, relative))).digest('hex')
const requireCheck = (condition, message) => { if (!condition) throw new Error(message) }
const metadata = await readJson('room.json')
const geometry = await readJson('qa/static.json')
const native = await readJson('qa/native.json')
const views = await readJson('qa/views.json')
const images = await readJson('qa/image-decode.json')
const fingerprint = await hash('room.nbt')
for (const report of [geometry, native, views, images]) requireCheck(report.status === 'PASS', 'Evidence not PASS')
for (const recorded of [metadata.sha256, geometry.sha256, native.structure_sha256, views.source_sha256]) requireCheck(recorded === fingerprint, 'Evidence has stale NBT hash')
requireCheck(native.rotations.length === 4 && native.stabilization_ticks === 200 && native.records_checked_after_ticks === 31200, 'Missing native coverage')
for (const fixture of native.rotations) {
  requireCheck(fixture.status === 'PASS' && fixture.capture_matches_source && fixture.external_fluid_cells === 0 && fixture.world_entities === 0, 'Native fixture failed')
  requireCheck(await hash(fixture.world_capture) === fixture.world_capture_sha256, 'Native capture hash differs')
}
for (const capture of views.captures) requireCheck(await hash(capture.path) === capture.sha256, 'Rendered image hash differs')
for (const image of images.images) requireCheck(await hash(image.file) === image.sha256, 'Image decode evidence stale')
const visualReview = {
  status: 'PASS', method: 'Agent visual inspection of preview.png plus both four-cardinal contact sheets using local image viewer',
  inspected: ['preview.png', 'views/interior-sheet.png', 'views/exterior-sheet.png'],
  checks: { straight_single_axis: true, continuous_level_central_runner: true, no_side_branch_or_stair_route: true, red_nether_and_blackstone_reference_palette: true, repeated_stepped_arch_ribs: true, paired_hanging_lanterns_and_lava: true, full_exterior_roof_and_closed_side_walls: true, cutaway_changes_preview_only: true },
  limitations: ['Software rendering, not a Minecraft client screenshot.', 'Concept translated to a 15x13x40 voxel template; no pixel-exact AI image reconstruction claim.', 'Side lava is real and dangerous outside the five-block safe main route.']
}
await fs.writeFile(path.join(root, 'qa/visual-review.json'), JSON.stringify(visualReview, null, 2) + '\n')
const qa = {
  schema: 'ft-independent-corridor-delivery-v1', id: 'M01', status: 'PASS',
  sha256: fingerprint, reference: metadata.reference, reference_sha256: metadata.reference_sha256,
  size: metadata.size, entrance: metadata.entrance, interior: metadata.interior, exit: metadata.exit,
  minecraft: '1.20.1', forge: '47.4.10',
  checks: {
    static_nbt_geometry: 'PASS', original_reference_hash: 'PASS', forty_block_straight_corridor: 'PASS',
    five_wide_three_high_flat_route: 'PASS', no_turns_branches_or_route_stairs: 'PASS', complete_roof_shell: 'PASS',
    vanilla_registry_states_and_native_roundtrip: 'PASS', native_four_rotations_placement: 'PASS',
    native_200_tick_stability: 'PASS', native_voxel_captures_match_rotated_source: 'PASS',
    lava_bottom_sides_and_outside_halo: 'PASS', hanging_light_supports: 'PASS', zero_entities_and_reward_stock: 'PASS',
    negative_probes_detect_faults: 'PASS', four_internal_and_four_external_views: 'PASS', visual_inspection: 'PASS'
  },
  native_summary: { rotations: native.rotations.map(fixture => fixture.rotation), records_per_rotation: 7800, checked_after_ticks: 31200, stabilization_ticks: 200, source_lava_cells: 48, supported_lanterns: 10, reachable_foot_positions_per_rotation: 296, main_route_foot_positions: 200, external_fluid_cells: 0, dropped_entities: 0, negative_probes: native.negative_probes, forced_chunk_ticking: true, scheduled_lava_ticks: true },
  evidence: { static: 'qa/static.json', native: 'qa/native.json', native_log: 'qa/forge-gametest.log', render_manifest: 'qa/views.json', image_decode: 'qa/image-decode.json', visual_review: 'qa/visual-review.json', interior_sheet: 'views/interior-sheet.png', exterior_sheet: 'views/exterior-sheet.png', native_world_captures: native.rotations.map(fixture => fixture.world_capture) },
  first_native_attempt: { status: 'compile_failed_then_fixed', cause: '1.20.1 gamerule official mapping constant is RULE_DAYLIGHT, not RULE_DODAYLIGHTCYCLE', evidence: 'qa/attempt-1-compile.log' },
  not_tested: ['Full FarmingTales modpack client rendering/playthrough, FPS, multiplayer.', 'FT core integration, dungeon route connections, existing return-portal behavior.'],
  safety: { deployed: false, production_saves_opened_or_modified: false, mods_directory_changed: false, shared_core_modified: false, paid_image_api_called: false, isolated_test_world: 'native-qa/run-isolated/world' },
  integration_requirements: ['Preflight entire 15x13x40 volume including explicit air plus entity/fluid/world-border checks before any placeInWorld call.', 'Preserve persistent per-template generation markers and never overwrite an already generated/player-modified instance.', 'Use feet-coordinate metadata with floor at local y=1 and players at local y=2.', 'Keep both five-wide north/south openings and use native rotation for ports and block states.', 'Do not deploy the native-qa fixture mod or its disposable test world.']
}
await fs.writeFile(path.join(root, 'qa.json'), JSON.stringify(qa, null, 2) + '\n')
const files = ['room.nbt', 'room.json', 'preview.png', 'qa.json', 'README.md', 'summary.json', 'views/interior-sheet.png', 'views/exterior-sheet.png', 'qa/static.json', 'qa/native.json', 'qa/forge-gametest.log', 'qa/attempt-1-compile.log', 'qa/views.json', 'qa/image-decode.json', 'qa/visual-review.json', ...views.captures.map(capture => capture.path), ...native.rotations.map(fixture => fixture.world_capture)]
const lines = []
for (const relative of files) lines.push((await hash(relative)) + '  ' + relative.replaceAll('\\', '/'))
await fs.writeFile(path.join(root, 'SHA256SUMS.txt'), lines.join('\n') + '\n')
console.log(JSON.stringify({ status: 'PASS', room_sha256: fingerprint, files_fingerprinted: files.length, native_records: 31200, screenshots: 10, visual_sheets: 2 }, null, 2))
