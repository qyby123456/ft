import fs from 'node:fs/promises'
import path from 'node:path'
import { createHash } from 'node:crypto'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const readJson = async relative => JSON.parse(await fs.readFile(path.join(root, relative), 'utf8'))
const metadata = await readJson('room.json')
const staticReport = await readJson('qa/static.json')
const forgeReport = await readJson('qa/forge-native.json')
const viewReport = await readJson('qa/views.json')
const digest = createHash('sha256').update(await fs.readFile(path.join(root, 'room.nbt'))).digest('hex')
for (const report of [metadata, staticReport, forgeReport]) if (report.sha256 !== digest) throw new Error('Evidence hash mismatch')
for (const report of [staticReport, forgeReport, viewReport]) if (report.status !== 'PASS') throw new Error('Incomplete QA')
if (viewReport.source_sha256 !== digest || forgeReport.rotations.length !== 4 || forgeReport.ticks_observed !== 240 || viewReport.captures.length !== 10) throw new Error('Missing required evidence')
const visualReview = {
  id: 'zone-C04', status: 'PASS', source_sha256: digest,
  reviewed: ['preview/interior-sheet.png', 'preview/exterior-sheet.png', 'preview/eye-sheet.png', 'preview/interior-north.png', 'preview/interior-south.png', 'preview/exterior-east.png', 'preview/eye-east.png'],
  observations: ['Four interior orientations show a clear central floor and north to east bend', 'Southwest rubble and fallen timber stay outside the protected route', 'Storage shelf, tabletop candles, grounded pillars and suspended lanterns are visible', 'Complete exterior has a continuous roof and only the two intentional connectors', 'Eye views retain the actual complete roof and show usable door clearance'],
  interpretation: 'Minecraft block resolution interpretation of the supplied concept; no AI-generated completion image used',
  limitations: ['Lodestone software preview; not an in-game client screenshot', 'Banner pattern composition can differ between renderer and Minecraft; native entity and survival checked by Forge']
}
await fs.writeFile(path.join(root, 'qa/visual-review.json'), JSON.stringify(visualReview, null, 2) + '\n')
const report = {
  id: 'zone-C04', name: '焦毁塌角', status: 'PASS', sha256: digest, size: metadata.size,
  reference: metadata.reference, reference_sha256: metadata.referenceSha256,
  static: { status: staticReport.status, evidence: 'qa/static.json', complete_volume_records: staticReport.complete_volume_records, non_air_blocks: staticReport.non_air_blocks, protected_route_positions: staticReport.protected_floor_positions, disconnected_non_air: 0, hazardous_blocks: 0 },
  native: { status: forgeReport.status, evidence: 'qa/forge-native.json', log: 'qa/forge-run.log', minecraft: forgeReport.minecraft, forge: forgeReport.forge, rotations: forgeReport.rotations.map(rotation => rotation.rotation), ticks_observed: forgeReport.ticks_observed, forced_neighbor_updates: true, fixture_floor_world_y: forgeReport.fixture_floor_world_y },
  views: { status: 'PASS', evidence: 'qa/views.json', visual_review: 'qa/visual-review.json', interior_four_views: 'preview/interior-sheet.png', exterior_four_views: 'preview/exterior-sheet.png', entrance_eye_views: 'preview/eye-sheet.png', actual_nbt_render: true, original_nbt_modified_for_views: false },
  earlier_attempt: { outcome: 'ENVIRONMENT_CONTAMINATION', log: 'qa/forge-attempt1-natural-terrain-fluid.log', reason: 'Default GameTest helper origin was underground; natural terrain lava entered an intentionally open connector after neighbor updates. Final test moves all four fixtures to world y=200.' },
  deployment: { performed: false, production_save_modified: false, production_jar_modified: false, resource_pack_modified: false, production_configuration_modified: false, parallel_delivery_modified: false },
  limitations: ['Independent vanilla 1.20.1 structure delivery; no core routing integration or deployment', 'Forge GameTest server validation and Lodestone previews; not a full modpack client, FPS or multiplayer play test', 'Integrator must validate entire target volume including explicit air and keep both connector approaches free of terrain and fluid']
}
await fs.writeFile(path.join(root, 'qa.json'), JSON.stringify(report, null, 2) + '\n')
const files = ['room.nbt', 'room.json', 'summary.json', 'preview.png', 'qa.json', 'README.md', 'qa/static.json', 'qa/forge-native.json', 'qa/native-roundtrip.nbt', 'qa/forge-run.log', 'qa/forge-attempt1-natural-terrain-fluid.log', 'qa/views.json', 'qa/visual-review.json', ...viewReport.captures.map(capture => capture.path), 'preview/interior-sheet.png', 'preview/exterior-sheet.png', 'preview/eye-sheet.png', 'source/recipe.mjs', 'source/static-qa.mjs', 'source/render-views.mjs', 'source/contact-sheets.mjs', 'source/run-forge.ps1', 'source/finalize.mjs', 'forge/build.gradle', 'forge/settings.gradle', 'forge/src/main/java/net/farmingtales/zonec04qa/ZoneC04Tests.java', 'forge/src/main/java/net/farmingtales/zonec04qa/ZoneC04Qa.java', 'forge/src/main/resources/META-INF/mods.toml', 'forge/src/main/resources/pack.mcmeta', 'forge/src/main/resources/data/zonec04qa/structures/empty.nbt']
const records = []
for (const relative of files) {
  const bytes = await fs.readFile(path.join(root, relative))
  records.push({ file: relative.replaceAll('\\', '/'), bytes: bytes.length, sha256: createHash('sha256').update(bytes).digest('hex') })
}
await fs.writeFile(path.join(root, 'qa/hashes.json'), JSON.stringify({ id: 'zone-C04', source_sha256: digest, records }, null, 2) + '\n')
console.log(JSON.stringify({ status: 'PASS', source_sha256: digest, evidence_files: records.length, rotations: forgeReport.rotations.map(rotation => ({ rotation: rotation.rotation, reachable: rotation.collision_reachable_positions })) }, null, 2))
