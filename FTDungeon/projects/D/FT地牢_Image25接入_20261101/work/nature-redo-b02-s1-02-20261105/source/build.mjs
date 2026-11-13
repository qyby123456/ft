import fs from 'node:fs/promises'
import path from 'node:path'
import {fileURLToPath} from 'node:url'
import {spawnSync} from 'node:child_process'
import {createHash} from 'node:crypto'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const id = 'nature-redo-b02-s1-02'
const folder = path.join(root, id)
const reference = 'D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S1-mossy-forest/out/S1-02-medium-room.png'
const sha256 = bytes => createHash('sha256').update(bytes).digest('hex')
const referenceHash = sha256(await fs.readFile(reference))
if (referenceHash !== '66cdda2e7fff580e89bd442017624faae0ef108209af30d5efa9806583ec53d1') throw new Error('Reference changed')
await fs.mkdir(folder, {recursive: true})
const skill = 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
const execution = spawnSync(process.execPath, [path.join(skill, 'scripts/run-build.mjs'), path.join(root, 'source/recipe.mjs'), '--out-dir', folder, '--name', 'room', '--data-version', '3465'], {cwd: skill, encoding: 'utf8', windowsHide: true})
await fs.writeFile(path.join(folder, 'build.log'), execution.stdout + execution.stderr)
if (execution.status !== 0) throw new Error(execution.stderr)
const route = new Set()
const strip = (startX, startZ, endX, endZ) => {
  for (let across = startX; across <= endX; across++) for (let forward = startZ; forward <= endZ; forward++) route.add(`${across},${forward}`)
}
strip(10,0,12,6)
strip(6,5,16,7)
strip(6,7,8,13)
strip(14,7,16,13)
strip(8,13,16,15)
strip(10,15,12,20)
const fingerprint = sha256(await fs.readFile(path.join(folder, 'room.nbt')))
const metadata = {id, number: '02', name: '古林泉室与储藏龛', size: [23,13,21], entrance:[11,2,0], interior:[11,2,5], facing:'north', feet_y:2, ports:[{side:'north',feet:[11,2,0],width:3,clearHeight:3},{side:'south',feet:[11,2,20],width:3,clearHeight:3}], route:[...route].map(cell => {const [across,forward]=cell.split(',').map(Number); return [across,2,forward]}), roof_exceptions:[], sha256:fingerprint, reference, reference_sha256:referenceHash, rotation_pivot:[0,0,0], minecraft:'1.20.1', status:'S1_02_PENDING_INDEPENDENT_REVIEW', deviations:['23x13x21 is an integer interpretation, not an exact image measurement.','South opening/full roof are explicit safety adaptations.','Short waterfall is interpreted as a sealed glazed source-water channel; not an open flowing waterfall.','No particles, shader rays, encounters or generated loot.'], connections:'Transform ports and bounding box around [0,0,0]; join opposing port feet at equal height via dry supported 3x3 connector; refuse occupied volume.'}
await fs.writeFile(path.join(folder,'room.json'),JSON.stringify(metadata,null,2)+'\n')
await fs.writeFile(path.join(root,'group.json'),JSON.stringify({id:'nature-redo-b02',group_fingerprint:referenceHash,mode:'ONE_IMAGE_ONLY',rooms:[{id,name:metadata.name,size:metadata.size,sha256:fingerprint,reference}],next_image:'S1-03',next_image_authorized_to_start:false,paid_image_requests:0,previous_accepted_sha256:'1174e5ae0e8824d1f0ab4ff78498fe7ffbe71a1561603145c7d69c5894e5ddea'},null,2)+'\n')
console.log(execution.stdout)
