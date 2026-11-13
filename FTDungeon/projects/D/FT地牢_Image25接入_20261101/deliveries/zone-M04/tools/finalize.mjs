import fs from 'node:fs/promises'
import path from 'node:path'
import crypto from 'node:crypto'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const read = async file => JSON.parse(await fs.readFile(path.join(root, file), 'utf8'))
const write = async (file, data) => fs.writeFile(path.join(root, file), JSON.stringify(data, null, 2) + '\n')
const hashFile = async file => crypto.createHash('sha256').update(await fs.readFile(path.join(root, file))).digest('hex')
const room = await read('room.json')
const hash = await hashFile('room.nbt')
if (hash !== room.sha256 || room.id !== 'zone-M04') throw new Error('Unexpected final template identity')
const reports = {}
for (const name of ['static', 'render', 'native', 'capture-comparison']) {
  reports[name] = await read(`qa/${name}.json`)
  if (reports[name].status !== 'PASS' || reports[name].structure_sha256 !== hash) throw new Error(`Stale or failed evidence: ${name}`)
}
const visualNotes = {
  'interior-north': 'North frame is separate from the clear three-wide entrance; columns stand on floor bases, no prop intersects a column. Continuous beams support the lamps. Sealed glass channel and central bridge visible.',
  'interior-south': 'Reverse cutaway shows no rear exit and clear rear work area; forge, anvil, ore crates and block-built coal handcart sit on the floor. Four columns and glass-covered lava are visible.',
  'interior-west': 'Western angle verifies mine rail resting on floor, barrels separated from supports, rail crossing level route, and no breaks in bridge or guard lines.',
  'interior-east': 'Eastern angle verifies coal handcart base and side panels, empty smelters and enclosed glowing forge. Roof beam intersections and lamp chains remain attached.',
  'exterior-north': 'Full roof and north portal present. No unintended exterior opening or exposed liquid.',
  'exterior-south': 'Continuous roof and closed south wall; ore accents replace wall blocks without creating holes.',
  'exterior-west': 'West and south exterior remain enclosed; no side exit or liquid exposure.',
  'exterior-east': 'East shell and north entrance visible; roof complete and no protruding unsafe decorative block.',
  'entrance-north': 'Complete-NBT interior view confirms entrance landing, level three-wide route and bridge, glass tops, railings and overhead clearance.',
  'bridge-south': 'Complete-NBT reverse interior confirms clear return to north portal, glass cap and guardrails beside bridge, roof anchors and continuous support beams.',
}
const visualImages = []
for (const [name, notes] of Object.entries(visualNotes)) {
  const file = `views/${name}.png`
  visualImages.push({ file, sha256: await hashFile(file), status: 'PASS', review: notes })
}
await write('qa/visual-review.json', {
  status: 'PASS', id: room.id, structure_sha256: hash, method: 'Agent visually inspected two four-view contact sheets and both full-NBT interior images using view_image. Individual full-resolution sources retained.',
  images: visualImages,
  revision: { issue: 'Initial lamp chains replaced blocks in the transverse support beams at ceiling level.', fix: 'Chains end below ceiling level; solid beam/stone anchors remain intact and connect to the complete roof.', superseded_evidence: 'qa/baseline-before-beam-fix/', final_visuals_regenerated: true },
  scope: 'Visual geometry review of actual final NBT software renders; native block survival, inventories and fluid stability validated separately.'
})
const originFile = 'out/mines/M04-mines-lava-seam-mine.png'
const manifest = await read('reference/batch-manifest.json')
const verification = await read('reference/batch-verify.json')
const manifestRecord = manifest.files.find(record => record.file === originFile)
const verificationRecord = verification.records.find(record => record.file === originFile)
if (await hashFile('reference/M04-mines-lava-seam-mine.png') !== room.reference_sha256 || manifestRecord?.sha256 !== room.reference_sha256 || verificationRecord?.sha256 !== room.reference_sha256) throw new Error('Reference chain fingerprint mismatch')
await write('qa/reference.json', { status: 'PASS', source_path: room.reference, source_sha256: room.reference_sha256, local_copy: 'reference/M04-mines-lava-seam-mine.png', manifest_record: manifestRecord, verification_record: verificationRecord, prompt: 'reference/prompt.txt', source_readme: 'reference/batch-README.md', paid_image_api_invoked: false })
const forgeLog = (await fs.readdir(path.join(root, 'qa'))).filter(name => /^forge-.*\.log$/.test(name)).sort().at(-1)
const forgeBytes = await fs.readFile(path.join(root, 'qa', forgeLog))
const forgeText = forgeBytes.toString(forgeBytes[0] === 255 && forgeBytes[1] === 254 ? 'utf16le' : 'utf8')
if (!forgeText.includes(`ZONE_M04_FORGE_PASS sha256=${hash}`) || !forgeText.includes('BUILD SUCCESSFUL')) throw new Error('Final native log lacks hash-bound successful test/build')
await write('qa.json', {
  status: 'PASS', id: room.id, producer_acceptance: 'PASS', unified_receiver_acceptance: 'PENDING', structure_sha256: hash, reference_sha256: room.reference_sha256,
  artifact: { minecraft: '1.20.1', forge: reports.native.forge, size: room.size, records: 8750, data_version: 3465, compression: 'gzip', palette: 'single' },
  static: { status: 'PASS', file: 'qa/static.json', checks: reports.static.checks.length, route_cells: 72 },
  visual: { status: 'PASS', file: 'qa/visual-review.json', render: 'qa/render.json', four_direction_interior_and_exterior: true, full_nbt_interior_views: 2, software_render_not_game_screenshot: true },
  forge_native: { status: 'PASS', file: 'qa/native.json', log: `qa/${forgeLog}`, rotations: 4, placements: 8, elapsed_server_ticks: reports.native.post_placement_server_ticks, palette_states: reports.native.strict_palette_states, compared_records_per_snapshot: 35000, player_collision_aabb: [0.6, 1.8, 0.6], dry_route_cells_with_approach_per_rotation: 81, safe_landing_cells_per_rotation: 25, full_roof_blocks: 625, source_lava_per_rotation: 57, lava_native_random_tick_stress_calls: reports.native.lava_native_random_tick_stress_calls, source_fluid_ticks_scheduled: true, do_fire_tick: true, fluid_fire_boundary_cells_per_rotation: 16182, leaks: 0, fire: 0, unsupported_blocks: 0, state_changes: 0, entities_or_item_drops: 0, native_auto_created_empty_containers: 9 },
  independent_native_capture: { status: 'PASS', file: 'qa/capture-comparison.json', captures: 4, compared_records: reports['capture-comparison'].total_records_independently_compared, mismatches: 0 },
  reference: { status: 'PASS', file: 'qa/reference.json' },
  design_safety: { six_face_lava_seal: true, sealed_full_block_glass_cap: true, iron_guardrails: true, three_wide_solid_bridge: true, five_by_five_safe_landing: true, continuous_floor_to_roof_columns: 4, chain_anchored_lamps: 6, full_roof: true, flame_or_open_lava: false },
  initial_rejections: [{ log: 'qa/forge-20261103-124014-803.log', reason: 'PowerShell launch wrapper treated javac deprecation stderr as a terminating shell error; no successful test claimed.' }, { archive: 'qa/baseline-before-beam-fix/', reason: 'Superseded lamp/beam anchoring before full native acceptance.' }],
  deployment: room.deployment,
  scope: 'Producer verification only. No formal integration, lobby route, persistent generation/idempotency/occupancy or full-modpack client/multiplayer/FPS claim. Lava isolation assumes the delivered enclosure remains intact.'
})
await write('handoff.json', {
  id: room.id, source_id: room.source_id, name: room.name, category: room.category, delivery_directory: root, status: 'PRODUCER_PASS_AWAITING_RECEIVER', ready_for_unified_integration: false,
  structure_sha256: hash, reference_sha256: room.reference_sha256, size: room.size, entrance: room.entrance, interior: room.interior, facing: room.facing, ports: room.ports,
  four_piece_set: ['room.nbt', 'room.json', 'preview.png', 'qa.json'], evidence: ['qa/static.json', 'qa/render.json', 'qa/visual-review.json', 'qa/native.json', 'qa/capture-comparison.json', 'qa/reference.json'],
  review: 'review.html', full_roof: true, lava: room.lava, deployment: room.deployment, identity_warning: 'zone-M04 mine room belongs to dungeon-zone-batch-20261103. Distinct from corridor-M04 flooded End corridor.'
})
const selected = ['room.nbt', 'room.json', 'preview.png', 'qa.json', 'summary.json', 'README.md', 'review.html', 'handoff.json', 'build.gradle', 'settings.gradle', 'gradle.properties']
async function includeDirectory(relative) {
  for (const entry of await fs.readdir(path.join(root, relative), { withFileTypes: true })) {
    const file = `${relative}/${entry.name}`
    if (entry.isDirectory()) await includeDirectory(file)
    else selected.push(file)
  }
}
for (const directory of ['tools', 'views', 'reference', 'qa-resources']) await includeDirectory(directory)
for (const name of await fs.readdir(path.join(root, 'qa'))) {
  if (name.endsWith('.json') || name.endsWith('.log') || name.startsWith('native-placed-') && name.endsWith('.nbt')) selected.push(`qa/${name}`)
}
const files = []
for (const file of [...new Set(selected)].sort()) files.push({ file, bytes: (await fs.stat(path.join(root, file))).size, sha256: await hashFile(file) })
await write('delivery-manifest.json', { id: room.id, status: 'PASS', structure_sha256: hash, reference_sha256: room.reference_sha256, files, scope: 'Current deliverable and reproducible producer evidence only. Excludes build caches, isolated world data, and superseded baseline archive. No self hash.' })
console.log(JSON.stringify({ status: 'PASS', files: files.length, structure_sha256: hash, four_piece_set: files.filter(entry => ['room.nbt', 'room.json', 'preview.png', 'qa.json'].includes(entry.file)) }))
