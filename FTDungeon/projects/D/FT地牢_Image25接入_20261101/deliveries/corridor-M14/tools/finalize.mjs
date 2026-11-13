import fs from 'node:fs/promises'
import path from 'node:path'
import crypto from 'node:crypto'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const readJson = async relative => JSON.parse((await fs.readFile(path.join(root, relative), 'utf8')).replace(/^\uFEFF/, ''))
const sha256 = async absolute => crypto.createHash('sha256').update(await fs.readFile(absolute)).digest('hex')
const metadata = await readJson('room.json')
const fingerprint = await sha256(path.join(root, 'room.nbt'))
if (metadata.sha256 !== fingerprint) throw new Error('NBT fingerprint mismatch')
const staticReport = await readJson('qa/static.json')
const designSource = await readJson('qa/reference.json')
if (designSource.status !== 'PASS' || designSource.sha256 !== metadata.reference_sha256 || !designSource.full_png_decode) throw new Error('Reference PNG/manifest/index verification incomplete')
const native = await readJson('qa/native.json')
const lodestone = await readJson('qa/lodestone-render.json')
const run = await readJson('evidence/native-run.json')
const log = await fs.readFile(path.join(root, run.log), 'utf8')
if (staticReport.status !== 'PASS' || native.status !== 'PASS' || lodestone.status !== 'PASS') throw new Error('A prerequisite QA report did not pass')
if ([staticReport.sha256, native.sha256, lodestone.source_sha256].some(value => value !== fingerprint)) throw new Error('QA evidence belongs to different NBT')
if (run.exit_code !== 0 || !log.includes('M14_NATIVE_GAME_TEST_PASS: rotations=4/4; records=29520; ticks=400; sha256=' + fingerprint) || !log.includes('All 1 required tests passed') || !log.includes('BUILD SUCCESSFUL')) throw new Error('Native terminal evidence incomplete')
const viewsRoot = 'preview/four-views/m14-final/corridor-M14'
if (await sha256(path.join(root, viewsRoot, 'room.nbt')) !== fingerprint) throw new Error('Four-view snapshot mismatch')
const viewFiles = []
for (const mode of ['interior', 'exterior']) {
  for (const direction of ['north', 'south', 'west', 'east']) viewFiles.push(viewsRoot + '/' + mode + '-' + direction + '.png')
  viewFiles.push(viewsRoot + '/' + mode + '-four-views.png')
}
const pngFiles = ['preview.png', 'preview/lodestone-south.png', 'preview/lodestone-exterior.png', ...viewFiles]
const imageEvidence = []
for (const relative of pngFiles) {
  const bytes = await fs.readFile(path.join(root, relative))
  if (bytes.subarray(0, 8).toString('hex') !== '89504e470d0a1a0a') throw new Error('Invalid PNG ' + relative)
  imageEvidence.push({ path: relative, width: bytes.readUInt32BE(16), height: bytes.readUInt32BE(20), sha256: await sha256(path.join(root, relative)) })
}
const visual = { status: 'PASS', sha256: fingerprint, reviewer: 'Agent visual inspection of both four-view sheets and north/south Lodestone eye-level renders', views: 8, basis: 'Actual final room.nbt; no reference image substitution', images: imageEvidence, checks: [{ name: 'Straight medium corridor, no branch, no stairs', status: 'PASS', detail: '41-block parallel walls, flat y=2 route, two open end arches.' }, { name: 'Reference-theme fidelity', status: 'PASS_WITH_DOCUMENTED_ADAPTATIONS', detail: 'Six red giant mushrooms, six stylized cyan mushrooms, eight suspended iron cages, hanging roots/spores, wall webs, cracked floor and orange seams. Lava replaced by glass-covered solid lights; normal renderer lighting is brighter than concept.' }, { name: 'Full shell and roof', status: 'PASS', detail: '615 top roof cells; both ends open only at the declared 3x5 ports; no side branches.' }, { name: 'Physical support and beam junctions', status: 'PASS', detail: 'Cage chain joins basalt beam and cage roof; lantern rests on full-block cage base; giant caps join stems and fungal beds. Roots and spore blossoms attach to final ceiling. Cutaway retains anchor patches.' }, { name: 'Path and furniture clearance', status: 'PASS', detail: 'Interior five-wide by four-high clear aisle; ports three-wide by five-high. Native collision flood reaches 316 dry feet and both ports in all rotations.' }, { name: 'Liquids, inventory and entities', status: 'PASS', detail: 'No fluid, open flame, inventories, block entities or structure entities; high wall webs outside path.' }], cutaway: 'Preview-only removal of roof layers y>=10 and near-side walls, preserving basalt beams and plant anchors; final NBT unchanged.', limitations: ['Software rendering is not a full modpack client screenshot.', 'Lodestone default pack may approximate or omit builtin entity models; exact primitive state checks are covered by Forge.', 'No client, multiplayer, FPS, shader or production integration claim.'] }
await fs.writeFile(path.join(root, 'qa/visual-review.json'), JSON.stringify(visual, null, 2) + '\n')
const qa = { status: 'PASS', id: metadata.id, design_id: 'M14', sha256: fingerprint, reference_sha256: metadata.reference_sha256, size: metadata.size, summary: { medium_length_blocks: 41, straight: true, turns: 0, branches: 0, stairs: 0, static_checks: staticReport.checks.length, full_volume_records: 7380, solid_blocks: 4283, north_port: [7, 2, 0], interior: [7, 2, 4], south_port: [7, 2, 40], full_roof_cells: 615, dry_reachable_feet: 316, interior_views: 4, exterior_views: 4, native_rotations_passed: 4, native_records_compared_per_phase: 29520, native_checks_phases: ['immediate', 'after_400_ticks'], native_neighbor_shape_updates: 29520, native_stability_ticks: 400, forced_random_ticks: 0, reason_no_forced_random_ticks: 'The selected stable blocks report no randomly-ticking states; randomTickSpeed remains 3 during the fixture.', live_fluids: 0, entities: 0, block_entities: 0, containers: 0 }, evidence: { static: 'qa/static.json', native: 'qa/native.json', visual: 'qa/visual-review.json', lodestone: 'qa/lodestone-render.json', native_run: 'evidence/native-run.json', log: run.log, internal_sheet: viewsRoot + '/interior-four-views.png', external_sheet: viewsRoot + '/exterior-four-views.png' }, history: [{ attempt: 1, result: 'Fixture setup failure before room test', detail: 'Missing empty test structure; fixed by prepare-fixture.mjs, attempt1 logs and isolated crash report retained.' }, { attempt: 2, result: 'PASS', detail: 'Real Forge placement, neighbor-shape update and 400-tick stability all passed with final NBT fingerprint.' }], integration_ready_for_coordinator_review: true, integrated_or_deployed: false, production_saves_touched: false, production_jars_or_sources_modified: false, full_modpack_client_playtest: false }
await fs.writeFile(path.join(root, 'qa.json'), JSON.stringify(qa, null, 2) + '\n')
const sources = ['D:/ft工程文件/FT地牢_Image25接入_20261101/CONTRACT.md', 'D:/ft工程文件/image2.5/dungeon-corridors-20261103/SPEC.md', 'D:/ft工程文件/image2.5/dungeon-corridors-20261103/prompts/M14-fungal-cave-aged.txt', metadata.reference, 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/SKILL.md', 'D:/ft工程文件/FT地牢_Image25接入_20261101/tools/review_four_views.py']
const provenance = []
sources.push('D:/ft工程文件/FT地牢_Image25接入_20261101/handover-20261102-01a19c8f/corridors-20261103/CONTRACT.md', 'D:/ft工程文件/image2.5/dungeon-corridors-20261103/README.md', 'D:/ft工程文件/image2.5/dungeon-corridors-20261103/manifest.json', 'D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/corridor_index.json')
for (const source of sources) provenance.push({ source, sha256: await sha256(source), access: 'read-only' })
await fs.writeFile(path.join(root, 'qa/provenance.json'), JSON.stringify(provenance, null, 2) + '\n')
const delivered = ['room.nbt','room.json','preview.png','qa.json','README.md','REPORT_FOR_COORDINATOR.md','recipe.mjs','Run-NativeQA.ps1','summary.json','qa/static.json','qa/native.json','qa/lodestone-render.json','qa/visual-review.json','qa/provenance.json','qa/four-view-manifest.json','evidence/native-run.json',run.log,'native-test/build.gradle','native-test/settings.gradle','native-test/src/main/resources/META-INF/mods.toml','native-test/src/main/resources/pack.mcmeta','native-test/src/main/resources/data/ftcorridorm14qa/structures/empty.nbt','native-test/src/main/java/net/farmingtales/corridorm14qa/CorridorM14Verification.java','tools/prepare-fixture.mjs','tools/static-qa.mjs','tools/render-four-views.py','tools/render-lodestone.cjs','tools/finalize.mjs',...pngFiles.filter(relative=>relative!=='preview.png')]
const files = []
delivered.push(viewsRoot + '/room.nbt', viewsRoot + '/room.json', 'evidence/forge-gametest-m14-attempt1-20261103.log')
delivered.push('qa/reference.json', 'tools/verify-reference.py')
for (const relative of delivered) {
  const absolute = path.join(root,relative)
  files.push({ path: relative, bytes: (await fs.stat(absolute)).size, sha256: await sha256(absolute) })
}
await fs.writeFile(path.join(root,'delivery-manifest.json'),JSON.stringify({status:'PASS',id:metadata.id,sha256:fingerprint,files,excluded_from_artifact_list:['native-test/build','native-test/.gradle','native-test/runs/*/world','preview/lodestone/node_modules','render-source'],no_deployment:true},null,2)+'\n')
console.log(JSON.stringify({status:'PASS',id:metadata.id,sha256:fingerprint,files:files.length,qa:'qa.json'},null,2))
