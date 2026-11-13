import assert from 'node:assert/strict'
import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createHash } from 'node:crypto'

const root = path.dirname(fileURLToPath(import.meta.url))
const hash = bytes => createHash('sha256').update(bytes).digest('hex')
const readJson = async file => JSON.parse((await fs.readFile(path.join(root, file), 'utf8')).replace(/^\uFEFF/, ''))
const manifest = await readJson('manifest.json')
for (const entry of manifest.files) {
  const target = path.resolve(root, entry.path)
  assert(target.startsWith(root + path.sep), 'Manifest escapes room directory')
  const bytes = await fs.readFile(target)
  assert.equal(hash(bytes), entry.sha256, `${entry.path} sha256 changed`)
  assert.equal(bytes.length, entry.bytes, `${entry.path} size changed`)
}
const metadata = await readJson('room.json')
const report = await readJson('qa.json')
assert.equal(hash(await fs.readFile(path.join(root, 'room.nbt'))), manifest.structure_sha256)
assert.equal(metadata.sha256, manifest.structure_sha256)
assert.equal(report.status, 'PASS')
assert.equal(report.structure_sha256, manifest.structure_sha256)
assert.equal(hash(await fs.readFile(metadata.reference)), metadata.reference_sha256)
console.log(JSON.stringify({ status: 'PASS', verified_files: manifest.files.length, structure_sha256: manifest.structure_sha256, reference_sha256: metadata.reference_sha256 }, null, 2))
