import fs from 'node:fs/promises'
import path from 'node:path'
import {fileURLToPath} from 'node:url'
import {spawnSync} from 'node:child_process'
import {createHash} from 'node:crypto'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const folder = path.join(root, 'nature-b1-s1-01')
const reference = 'D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S1-mossy-forest/out/S1-01-large-chamber.png'
const sha256 = bytes => createHash('sha256').update(bytes).digest('hex')
const referenceHash = sha256(await fs.readFile(reference))
if (referenceHash !== '488389dcaa82a65f90dfe81cfdc289521ee6016c74af2ad06b5e2bd41103b400') throw new Error('Reference changed')
await fs.mkdir(folder, {recursive: true})
const skill = 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
const execution = spawnSync(process.execPath, [path.join(skill, 'scripts/run-build.mjs'), path.join(root, 'source/recipe.mjs'), '--out-dir', folder, '--name', 'room', '--data-version', '3465'], {cwd: skill, encoding: 'utf8', windowsHide: true})
await fs.writeFile(path.join(folder, 'build.log'), execution.stdout + execution.stderr)
if (execution.status !== 0) throw new Error(execution.stderr)
const route = new Set()
const strip = (startX, startZ, endX, endZ) => {
  for (let across = startX; across <= endX; across++) for (let forward = startZ; forward <= endZ; forward++) route.add(`${across},${forward}`)
}
strip(11, 0, 13, 5)
strip(8, 4, 19, 6)
strip(4, 7, 6, 17)
strip(18, 7, 20, 17)
strip(5, 17, 19, 18)
strip(0, 11, 6, 13)
strip(18, 11, 24, 13)
const metadata = {id: 'nature-b1-s1-01', number: '01', name: '盘根祭坛大厅 · 逐图修订', size: [25, 15, 25], entrance: [12, 2, 0], interior: [12, 2, 4], facing: 'north', feet_y: 2, ports: [{side: 'north', feet: [12, 2, 0], width: 3, clearHeight: 3}, {side: 'west', feet: [0, 2, 12], width: 3, clearHeight: 3}, {side: 'east', feet: [24, 2, 12], width: 3, clearHeight: 3}], route: [...route].map(cell => {const [across, forward] = cell.split(',').map(Number); return [across, 2, forward]}), roof_exceptions: [], sha256: sha256(await fs.readFile(path.join(folder, 'room.nbt'))), reference, reference_sha256: referenceHash, design_authority: 'Single reference image S1-01; ANALYSIS.md separates observed features from design interpretations.', compatibility_reference: 'D:/ft工程文件/结构参考_20261103/LEARNING_NOTES.md', rotation_pivot: [0, 0, 0], minecraft: '1.20.1', status: 'SERIAL_FIRST_IMAGE_PENDING_ACCEPTANCE', deviations: ['25x15x25 dimensions are an interpretation, not exact values measured from the image.', 'Full safety roof and north entrance are explicit FT adaptations.', 'No shader rays, particle effects, reward containers or combat runtime.']}
await fs.writeFile(path.join(folder, 'room.json'), JSON.stringify(metadata, null, 2) + '\n')
await fs.writeFile(path.join(root, 'group.json'), JSON.stringify({id: 'nature-b1-s1-01-serial', group_fingerprint: referenceHash, mode: 'STRICT_SERIAL_ONE_IMAGE_ONE_MODULE', rooms: [{id: metadata.id, name: metadata.name, size: metadata.size, sha256: metadata.sha256, reference}], next_image: 'S1-02', next_image_authorized_to_start: false, previous_batch: 'Historical candidates only; not accepted under revised workflow.', paid_image_requests: 0}, null, 2) + '\n')
console.log(execution.stdout)
