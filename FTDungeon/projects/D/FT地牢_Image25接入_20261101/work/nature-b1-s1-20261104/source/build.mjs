import fs from 'node:fs/promises'
import path from 'node:path'
import {fileURLToPath} from 'node:url'
import {spawnSync} from 'node:child_process'
import {createHash} from 'node:crypto'
import {rooms, routeFor} from './forest.mjs'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const skill = 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
const references = 'D:/ft工程文件/image2.5/nature-dungeon-styles-20261104'
const sha256 = bytes => createHash('sha256').update(bytes).digest('hex')
const inputsPath = path.join(root, 'evidence/input-manifest.json')
const inputs = JSON.parse(await fs.readFile(inputsPath, 'utf8'))
for (const entry of [inputs.source_jar, ...inputs.documents, ...Object.values(inputs.templates).flatMap(template => [template, template.details])]) {
  if (sha256(await fs.readFile(entry.path)) !== entry.sha256) throw new Error(`Authoritative input changed: ${entry.path}`)
}
const manifest = JSON.parse(await fs.readFile(path.join(references, 'manifest.json'), 'utf8'))
const selected = Object.entries(manifest.images).filter(([key]) => /^S1-(0[1-9]|10)-/.test(key)).sort(([left], [right]) => left.localeCompare(right))
if (selected.length !== 10) throw new Error('Expected precisely ten first-batch S1 reference images')
for (const [key, entry] of selected) {
  if (sha256(await fs.readFile(path.join(references, entry.path))) !== entry.sha256) throw new Error(`Reference changed: ${key}`)
}
const fingerprints = selected.map(([key, entry]) => `${key}\t${entry.path}\t${entry.sha256}`)
const groupFingerprint = sha256(fingerprints.join('\n'))
const results = []
for (const room of rooms) {
  const folder = path.join(root, room.id)
  await fs.mkdir(folder, {recursive: true})
  const recipe = path.join(root, 'source', `${room.id}.mjs`)
  await fs.writeFile(recipe, `import {rooms, buildRoom} from './forest.mjs'\nexport default api => buildRoom(api, rooms.find(room => room.id === '${room.id}'))\n`)
  const command = [path.join(skill, 'scripts/run-build.mjs'), recipe, '--out-dir', folder, '--name', 'room', '--data-version', '3465', '--compression', 'gzip', '--json']
  const execution = spawnSync(process.execPath, command, {cwd: skill, encoding: 'utf8', windowsHide: true})
  await fs.writeFile(path.join(folder, 'build.log'), execution.stdout + execution.stderr)
  if (execution.status !== 0) throw new Error(`Build failed: ${room.id}: ${execution.stderr}`)
  const summary = JSON.parse(await fs.readFile(path.join(folder, 'summary.json'), 'utf8'))
  const reference = selected.find(([key]) => key.startsWith(`S1-${room.number}-`))[1]
  const [width, , depth] = room.size
  const centerX = Math.floor(width / 2)
  const centerZ = Math.floor(depth / 2)
  const ports = room.ports.map(side => ({side, feet: side === 'north' ? [centerX, 2, 0] : side === 'south' ? [centerX, 2, depth - 1] : side === 'west' ? [0, 2, centerZ] : [width - 1, 2, centerZ], width: 3, clearHeight: 3, outward: side === 'north' ? [0, 0, -1] : side === 'south' ? [0, 0, 1] : side === 'west' ? [-1, 0, 0] : [1, 0, 0]}))
  const metadata = {...room, size: room.size, sha256: sha256(await fs.readFile(path.join(folder, 'room.nbt'))), entrance: [centerX, 2, 0], interior: [centerX, 2, 3], facing: 'north', ports, reference: path.join(references, reference.path), reference_sha256: reference.sha256, group_fingerprint: groupFingerprint, minecraft: '1.20.1', data_version: 3465, rotation_pivot: [0, 0, 0], feet_y: 2, route: routeFor(room).map(([across, forward]) => [across, 2, forward]), roof_exceptions: room.sky ? Array.from({length: room.sky[1] - room.sky[0] + 1}, (_, across) => Array.from({length: room.sky[3] - room.sky[2] + 1}, (_, forward) => [across + room.sky[0], forward + room.sky[2]])).flat() : [], status: 'CANDIDATE_NOT_INTEGRATED', deviations: ['Concept lighting is not reproduced as shader effects.', 'No closed door blocks the three-block-wide entrance.', 'Dimensions are integer block approximations; every entrance uses feet Y=2.', 'Treasure chests are intentionally empty; rewards and encounters are not implemented.'], connections: 'Align opposing port feet at matching height with a supported three-wide, three-high dry connector. Minecraft native structure rotation is around local [0,0,0], not the room center. Validate the entire bounding volume before placement. No automatic world placement is included.'}
  await fs.writeFile(path.join(folder, 'room.json'), JSON.stringify(metadata, null, 2) + '\n')
  await fs.writeFile(path.join(folder, 'evidence.json'), JSON.stringify({input_manifest: inputsPath, input_fingerprint: inputs.input_fingerprint, source: inputs.templates[room.source_evidence.source], adaptation: room.source_evidence, image_role: 'Secondary forest-theme reference, not geometric authority', runtime: inputs.runtime}, null, 2) + '\n')
  results.push({id: room.id, name: room.name, size: room.size, sha256: metadata.sha256, records: summary.blockCount, reference: metadata.reference})
  console.log('BUILT', room.id, room.size.join('x'), summary.blockCount)
}
await fs.writeFile(path.join(root, 'group.json'), JSON.stringify({id: 'nature-b1-s1', name: '苔藓古林 / 第一批第一组', group_fingerprint: groupFingerprint, references: selected.map(([key, entry]) => ({id: key, ...entry, absolute_path: path.join(references, entry.path)})), rooms: results, overview_is_reference_not_room: true, paid_image_requests: 0, deployment_authorized: false, source_manifest: path.join(references, 'manifest.json')}, null, 2) + '\n')
