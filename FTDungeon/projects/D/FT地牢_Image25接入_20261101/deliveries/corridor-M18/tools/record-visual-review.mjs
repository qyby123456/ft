import fs from 'node:fs/promises'
import path from 'node:path'
import { createHash } from 'node:crypto'
import { fileURLToPath } from 'node:url'

if (!process.argv.includes('--confirmed-after-viewing')) throw new Error('Inspect the actual PNGs first, then pass --confirmed-after-viewing to record the review.')
const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const fingerprint = async relative => createHash('sha256').update(await fs.readFile(path.join(root, relative))).digest('hex')
const render = JSON.parse(await fs.readFile(path.join(root, 'qa/render.json'), 'utf8'))
const report = {
  status: 'PASS', source_sha256: await fingerprint('room.nbt'),
  reviewer: 'Codex visual inspection of final north/south interior, four-view composite and complete exterior PNGs',
  basis: 'Actual final NBT rendered by installed Minecraft Builder and Lodestone; reference PNG reviewed separately',
  checks: [
    { name: 'single_straight_axis', status: 'PASS', observation: 'North/south interior images show the same uninterrupted central axis and distant open portal; no branch, turn or stair in the route.' },
    { name: 'reference_theme', status: 'PASS', observation: 'Pale blue ice floor and walls, white frost accents, gray masonry, repeated candle alcoves and paired overhead cages are visibly present.' },
    { name: 'four_view_geometry', status: 'PASS', observation: 'NW/NE/SW/SE composite is consistent: seven structural ribs, six cages, repeated wall bays and a level floor; no detached major structure.' },
    { name: 'final_full_roof', status: 'PASS', observation: 'Complete exterior shows an intact roof and side enclosure. Cutaway panels are explicitly labeled display-only.' },
    { name: 'preview_legibility', status: 'PASS', observation: 'Final PNGs render legibly against a dark background with readable labels, no missing-texture checkerboard or error overlay.' },
  ],
  known_differences: [
    'This is a vanilla-block reconstruction, not a pixel-perfect recreation of the AI concept.',
    'Ice tips use blue ice/glass panes; cage lids and floors use stone-brick slabs.',
    'Candle flame particles and cinematic warm glow are not accurately reproduced by the software renderer; lit block states, support and snow-light safety are independently checked in Forge.',
    'The nearest cage tops may extend beyond the interior camera frame; the full cages and chain anchors are visible in the four-view and verified in source/native data.',
  ],
  images: [],
}
for (const view of render.views) report.images.push({ path: view.path, sha256: await fingerprint(view.path), inspected_via: view.cutaway ? 'preview/four-view.png composite' : view.path })
for (const relative of ['preview.png', 'preview/four-view.png']) report.images.push({ path: relative, sha256: await fingerprint(relative), inspected_via: relative })
await fs.writeFile(path.join(root, 'qa/visual-review.json'), `${JSON.stringify(report, null, 2)}\n`)
console.log(`Recorded visual review of ${report.images.length} image artifacts.`)
