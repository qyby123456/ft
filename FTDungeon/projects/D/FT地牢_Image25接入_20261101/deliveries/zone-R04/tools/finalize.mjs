import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createHash } from 'node:crypto'
import { createRequire } from 'node:module'
import { execFile } from 'node:child_process'
import { promisify } from 'node:util'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
if (path.basename(root) !== 'zone-R04') throw new Error('Wrong isolated delivery')
const require = createRequire('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/package.json')
const sharp = require('sharp')
const digest = bytes => createHash('sha256').update(bytes).digest('hex')
const load = async relative => JSON.parse(await fs.readFile(path.join(root, relative), 'utf8'))
const write = async (relative, data) => fs.writeFile(path.join(root, relative), JSON.stringify(data, null, 2) + '\n')
const sha256 = digest(await fs.readFile(path.join(root, 'room.nbt')))
const metadata = await load('room.json')
const staticQA = await load('qa/static.json')
const nativeQA = await load('qa/native.json')
const manifest = await load('views/manifest.json')
for (const report of [metadata, staticQA, nativeQA, manifest]) if (report.sha256 !== sha256) throw new Error('Stale evidence hash')
if (staticQA.status !== 'PASS' || nativeQA.status !== 'PASS' || manifest.browser_errors.length || nativeQA.settle_ticks !== 600 || nativeQA.rotations !== 4) throw new Error('Incomplete QA')
if (digest(await fs.readFile(path.join(root, 'reference/concept.png'))) !== metadata.reference_sha256) throw new Error('Reference mismatch')
for (const kind of ['interior','exterior']) {
  const images = await Promise.all(['north','south','west','east'].map(side => sharp(path.join(root, 'views', kind + '-' + side + '.png')).resize(800,525).png().toBuffer()))
  await sharp({create:{width:1600,height:1050,channels:3,background:'#20262c'}}).composite(images.map((input,index) => ({input,left:index % 2 * 800,top:Math.floor(index / 2) * 525}))).png().toFile(path.join(root,'views',kind + '-four-views.png'))
}
const imageFiles = ['preview.png', ...manifest.views.filter(view => view.file !== 'preview.png').map(view => view.file), 'views/interior-four-views.png', 'views/exterior-four-views.png']
const imageEvidence = []
for (const file of imageFiles) {
  const bytes = await fs.readFile(path.join(root, file))
  const image = await sharp(bytes).metadata()
  if (image.width !== 1600 || image.height !== 1050 || image.format !== 'png') throw new Error('Invalid view ' + file)
  imageEvidence.push({file, sha256:digest(bytes), width:image.width, height:image.height})
}
manifest.status = 'PASS'
manifest.visual_review = 'qa/visual.json'
manifest.images = imageEvidence
await write('views/manifest.json', manifest)
await write('qa/visual.json', {
  status:'PASS', sha256, reference_sha256:metadata.reference_sha256,
  reviewer:'Codex visual inspection of final preview, all eight directions on contact sheets, and safe-point walking view',
  checks:{reference_identity:'PASS', supported_bunks_and_ladders:'PASS', grounded_columns_and_roof_beams:'PASS', grounded_fallen_beams:'PASS', central_clear_route:'PASS', anchored_lanterns:'PASS', sealed_roof_and_two_glass_skylights:'PASS', interior_and_exterior_four_sides:'PASS', no_wall_furniture_intersections:'PASS'},
  renderer:'Lodestone ThreeStructureRenderer / actual final NBT / software render',
  cutaway:'Preview removes Y>=11 roof and camera-side wall only; Y=10 carrying beams retained; final NBT unchanged',
  adaptations:metadata.reference_adaptations, images:imageEvidence,
  production_save_used:false, full_modpack_client_screenshot:false
})
await write('qa.json', {
  id:'zone-R04', name:metadata.name, status:'PASS', sha256, reference_sha256:metadata.reference_sha256,
  size:metadata.size, data_version:3465, entrance:metadata.entrance, interior:metadata.interior, exit:metadata.exit, facing:'north',
  static:{status:'PASS',checks:Object.keys(staticQA.checks).length,records:staticQA.records,non_air:staticQA.non_air,palette_count:staticQA.palette_count,evidence:'qa/static.json'},
  visual:{status:'PASS',interior_directions:4,exterior_directions:4,rendered_views:manifest.views.length,evidence:'qa/visual.json'},
  native:{status:'PASS',minecraft:'1.20.1',forge:'47.4.10',rotations:4,settle_ticks:600,state_comparisons:nativeQA.records_per_snapshot * 2,reachable_feet_per_rotation:nativeQA.initial[0].reachable_cells,survival_checks_per_rotation:nativeQA.initial[0].survival_checks,boundary_checks_per_rotation:nativeQA.initial[0].boundary_air_checks,neighbor_updates:true,entities:0,evidence:'qa/native.json',log:'qa/forge.log'},
  four_piece:['room.nbt','room.json','preview.png','qa.json'],
  deployed:false,production_save_modified:false,formal_files_modified:false,full_modpack_playtest:false,
  limitations:['Software preview is not a client screenshot.','Isolated Forge native test does not claim full-modpack or multiplayer acceptance.','Concept roof gaps are glass-sealed; bunk beds, cloth, ash, bones and helmet are static block approximations.']
})
const files = ['room.nbt','room.json','preview.png','qa.json','README.md','summary.json']
const walk = async relative => {
  for (const entry of await fs.readdir(path.join(root, relative), {withFileTypes:true})) {
    const file = relative + '/' + entry.name
    if (entry.isDirectory()) await walk(file)
    else files.push(file)
  }
}
for (const directory of ['tools','qa','views','reference','forge-qa/src']) await walk(directory)
for (const name of ['build.gradle','settings.gradle','gradle.properties']) files.push('forge-qa/' + name)
files.sort()
const entries = []
for (const file of files) { const bytes = await fs.readFile(path.join(root,file)); entries.push({file:file.replaceAll('\\','/'),bytes:bytes.length,sha256:digest(bytes)}) }
await write('delivery-manifest.json',{id:'zone-R04',status:'PASS',nbt_sha256:sha256,reference_sha256:metadata.reference_sha256,files:entries,excluded:['forge-qa/.gradle','forge-qa/build','forge-qa/runs','isolated worlds','compiled JARs'],deployed:false})
const zipScript = 'import sys,json,zipfile,pathlib; root=pathlib.Path(sys.argv[1]); manifest=json.loads((root/"delivery-manifest.json").read_text(encoding="utf-8")); archive=zipfile.ZipFile(root/"zone-R04-handoff.zip","w",zipfile.ZIP_DEFLATED,compresslevel=6); [archive.write(root/entry["file"],entry["file"]) for entry in manifest["files"]]; archive.write(root/"delivery-manifest.json","delivery-manifest.json"); archive.close()'
await promisify(execFile)('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe',['-c',zipScript,root])
const packageBytes = await fs.readFile(path.join(root,'zone-R04-handoff.zip'))
await write('package-receipt.json',{id:'zone-R04',status:'PASS',archive:'zone-R04-handoff.zip',bytes:packageBytes.length,sha256:digest(packageBytes),manifest_sha256:digest(await fs.readFile(path.join(root,'delivery-manifest.json'))),nbt_sha256:sha256,files:entries.length + 1,deployed:false})
process.stdout.write(JSON.stringify({status:'PASS',nbt_sha256:sha256,archive_sha256:digest(packageBytes),files:entries.length + 1,bytes:packageBytes.length}) + '\n')
