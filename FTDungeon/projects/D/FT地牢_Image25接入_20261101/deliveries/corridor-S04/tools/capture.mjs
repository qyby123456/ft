import fs from 'node:fs/promises';
import path from 'node:path';
import { createHash } from 'node:crypto';
import { createRequire } from 'node:module';
import { fileURLToPath } from 'node:url';
import assert from 'node:assert/strict';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const packageRoot = process.env.CODEX_NODE_PACKAGES ?? 'C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules';
const require = createRequire(path.join(packageRoot, '../package.json'));
const { chromium } = require('playwright');
const sharp = require('sharp');
const sourceHash = createHash('sha256').update(await fs.readFile(path.join(root, 'room.nbt'))).digest('hex');
let executablePath = chromium.executablePath();
try { await fs.access(executablePath); } catch { executablePath = 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe'; }
const browser = await chromium.launch({ headless: true, executablePath, args: ['--enable-unsafe-swiftshader', '--use-angle=swiftshader', '--disable-gpu-sandbox', '--disable-dev-shm-usage'] });
const consoleErrors = [];
try {
  const page = await browser.newPage({ viewport: { width: 1536, height: 1024 }, deviceScaleFactor: 1 });
  page.on('pageerror', error => {
    consoleErrors.push(error.stack ?? String(error));
    page.evaluate(message => { window.s04Failed = message; }, String(error)).catch(() => undefined);
  });
  page.on('console', message => { if (message.type() === 'error') consoleErrors.push(message.text()); });
  const server = process.env.S04_PREVIEW_SERVER ?? 'http://127.0.0.1:5184';
  const route = '/@fs/' + root.replaceAll('\\', '/') + '/preview/viewer.mjs';
  const html = (await fs.readFile(path.join(root, 'preview/viewer.html'), 'utf8')).replace('./viewer.mjs', route);
  await page.route('**/s04-capture', request => request.fulfill({ status: 200, contentType: 'text/html', body: html }));
  await page.goto(server + '/s04-capture', { waitUntil: 'domcontentloaded', timeout: 180000 });
  try {
    await page.waitForFunction(() => window.s04?.ready || window.s04Failed, null, { timeout: 180000 });
    assert.equal(consoleErrors.length, 0, JSON.stringify(consoleErrors));
  } catch (failure) {
    console.error(JSON.stringify({ errors: consoleErrors, body: await page.locator('body').innerText() }));
    throw failure;
  }
  assert.equal(await page.evaluate(() => window.s04.sha256), sourceHash);
  const results = [];
  const directions = ['north', 'east', 'south', 'west'];
  for (const direction of directions) {
    for (const type of ['exterior', 'interior']) {
      const name = `${direction}-${type}`;
      const camera = await page.evaluate(name => window.s04.view(name), name);
      await page.waitForTimeout(200);
      const destination = path.join(root, 'preview', `${name}.png`);
      await page.screenshot({ path: destination, animations: 'disabled' });
      const bytes = await fs.readFile(destination);
      const image = await sharp(bytes).metadata();
      const statistics = await sharp(bytes).stats();
      assert(image.width === 1536 && image.height === 1024);
      assert(statistics.channels.some(channel => channel.stdev > 15), `Blank render: ${name}`);
      results.push({ name, file: `preview/${name}.png`, width: image.width, height: image.height, sha256: createHash('sha256').update(bytes).digest('hex'), camera });
    }
  }
  assert.equal(consoleErrors.length, 0, JSON.stringify(consoleErrors));
  await fs.copyFile(path.join(root, 'preview/north-interior.png'), path.join(root, 'preview.png'));
  const width = 1600;
  const thumbWidth = 768;
  const thumbHeight = 512;
  const rowHeight = 546;
  const layers = [];
  for (let index = 0; index < results.length; index++) {
    layers.push({ input: await sharp(path.join(root, results[index].file)).resize(thumbWidth, thumbHeight).png().toBuffer(), left: 24 + (index % 2) * 784, top: 100 + Math.floor(index / 2) * rowHeight });
  }
  const heading = Buffer.from(`<svg width="1600" height="90"><style>text{font-family:Segoe UI,Arial,sans-serif;fill:#e1eee7}</style><text x="26" y="38" font-size="27">S04 / AGED FUNGAL PASSAGE</text><text x="26" y="69" font-size="16">Four cardinal directions · Exterior + interior · E/W cutaways are preview-only · NBT ${sourceHash.slice(0, 16)}</text></svg>`);
  layers.push({ input: heading, left: 0, top: 0 });
  await sharp({ create: { width, height: 100 + 4 * rowHeight, channels: 4, background: '#101917' } }).composite(layers).png().toFile(path.join(root, 'preview/four-directions.png'));
  const report = { id: 'S04', status: 'PASS', source_sha256: sourceHash, renderer: 'Lodestone 0.6.0 / ThreeStructureRenderer / vanilla default pack', screenshots: results, screenshot_count: 8, console_errors: consoleErrors, source_nbt_unchanged: createHash('sha256').update(await fs.readFile(path.join(root, 'room.nbt'))).digest('hex') === sourceHash, visual_review: 'PENDING', limits: ['Renderer screenshots, not full-modpack client captures', 'East and west interior views remove roof and near-side shelf/wall in memory only; delivered NBT retains all blocks'] };
  await fs.writeFile(path.join(root, 'qa/render.json'), JSON.stringify(report, null, 2) + '\n');
  console.log(JSON.stringify({ status: report.status, screenshots: results.length, source_sha256: sourceHash, overview: path.join(root, 'preview/four-directions.png') }));
} finally {
  await browser.close();
}
