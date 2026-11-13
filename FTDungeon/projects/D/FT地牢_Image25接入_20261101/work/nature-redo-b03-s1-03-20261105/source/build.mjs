import fs from 'node:fs/promises'
import path from 'node:path'
import {fileURLToPath} from 'node:url'
import {spawnSync} from 'node:child_process'
import {createHash} from 'node:crypto'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const id = 'nature-redo-b03-s1-03'
const folder = path.join(root, id)
const reference = 'D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S1-mossy-forest/out/S1-03-small-room.png'
const sha256 = bytes => createHash('sha256').update(bytes).digest('hex')
const referenceHash = sha256(await fs.readFile(reference))
if (referenceHash !== 'cd8685c13f8f0e7a9011ed0d7904a8103f0c04e0d02ad063352855282ce3c8fe') throw new Error('Reference changed')
await fs.mkdir(folder, {recursive: true})
const skill = 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
const execution = spawnSync(process.execPath, [path.join(skill, 'scripts/run-build.mjs'), path.join(root, 'source/recipe.mjs'), '--out-dir', folder, '--name', 'room', '--data-version', '3465'], {cwd: skill, encoding: 'utf8', windowsHide: true})
await fs.writeFile(path.join(folder, 'build.log'), execution.stdout + execution.stderr)
if (execution.status !== 0) throw new Error(execution.stderr)
const route = new Set()
const strip = (startX, startZ, endX, endZ) => {
  for (let across = startX; across <= endX; across++) for (let forward = startZ; forward <= endZ; forward++) route.add(`${across},${forward}`)
}
strip(0,9,7,11)
strip(7,11,13,13)
strip(13,7,15,11)
strip(13,5,18,7)
const fingerprint = sha256(await fs.readFile(path.join(folder, 'room.nbt')))
const metadata = {id, number:'03', name:'盘根小室与单箱石台', size:[19,12,17], entrance:[0,2,10], interior:[7,2,11], facing:'west', feet_y:2, ports:[{side:'west',feet:[0,2,10],width:3,clearHeight:3},{side:'east',feet:[18,2,6],width:3,clearHeight:3}], route:[...route].map(cell => {const [across,forward]=cell.split(',').map(Number); return [across,2,forward]}), roof_exceptions:[], sha256:fingerprint, reference, reference_sha256:referenceHash, rotation_pivot:[0,0,0], minecraft:'1.20.1', status:'S1_03_PENDING_INDEPENDENT_REVIEW', deviations:['19x12x17 integer interpretation, not an exact image measurement.', 'Full roof and near walls enclose the image cutaway; only west/east ports remain.', 'Potted mushroom/empty pots on grounded solid shelves replace loose mushrooms and small vessels.', 'Roots use stepped full blocks; no shader rays or exterior forest.', 'One empty raised chest; no loot, encounter or mechanism.'], connections:'Transform ports and bounding box around [0,0,0]; join opposing port feet at equal height via supported dry 3x3 connector; refuse occupied volume.'}
await fs.writeFile(path.join(folder,'room.json'),JSON.stringify(metadata,null,2)+'\n')
await fs.writeFile(path.join(root,'group.json'),JSON.stringify({id:'nature-redo-b03',group_fingerprint:referenceHash,mode:'ONE_IMAGE_ONLY',rooms:[{id,name:metadata.name,size:metadata.size,sha256:fingerprint,reference}],next_image:'S1-04',next_image_authorized_to_start:false,paid_image_requests:0,previous_accepted_sha256:'f541344aa8a3a2f3cc47d1f4580efd9a06036b60bf5bfd43ca6b4ef6858467c2'},null,2)+'\n')
console.log(execution.stdout)
