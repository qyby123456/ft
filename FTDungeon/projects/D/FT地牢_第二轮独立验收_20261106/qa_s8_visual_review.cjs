const fs = require('node:fs/promises');
const path = require('node:path');
const crypto = require('node:crypto');
const { pathToFileURL } = require('node:url');
const { createRequire } = require('node:module');
const { chromium } = createRequire('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/package.json')('playwright');
const folder = 'C:/Users/YOUR_USERNAME/.codex/visualizations/2026/11/04/01a1a685-045b-76d2-aa2b-2a3f24520603/s8-independent-visual-review-20261107-1600';
async function main() {
  const browser = await chromium.launch({ executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe', headless: true, args: ['--no-sandbox'] });
  try {
    const page = await browser.newPage({ viewport: { width: 1800, height: 1100 } });
    const errors = [];
    page.on('pageerror', error => errors.push(String(error)));
    await page.goto(pathToFileURL(path.join(folder, 'index.html')).href, { waitUntil: 'load' });
    await page.locator('details').evaluateAll(elements => elements.forEach(element => element.open = true));
    await page.locator('img').evaluateAll(elements => elements.forEach(element => element.loading = 'eager'));
    await page.waitForFunction(() => Array.from(document.images).every(image => image.complete && image.naturalWidth > 0));
    const images = await page.locator('img').evaluateAll(elements => elements.map(element => ({ source: element.getAttribute('src'), width: element.naturalWidth, height: element.naturalHeight })));
    if (images.length !== 25 || errors.length) throw new Error(JSON.stringify({ images: images.length, errors }));
    const links = await page.locator('a').evaluateAll(elements => elements.map(element => element.getAttribute('href')));
    for (const link of links) {
      if (link.startsWith('#')) {
        if (await page.locator(link).count() !== 1) throw new Error('Missing review anchor: ' + link);
        continue;
      }
      const target = path.resolve(folder, link.split('#')[0]);
      if (!target.startsWith(path.resolve(folder) + path.sep)) throw new Error('External review link: ' + link);
      await fs.access(target);
    }
    const screenshots = [];
    for (const room of ['S8-13', 'S8-16', 'S8-19', 'S8-20']) {
      const filename = room + '-comparison.png';
      await page.locator('#' + room + ' > .pair').first().screenshot({ path: path.join(folder, filename) });
      const bytes = await fs.readFile(path.join(folder, filename));
      screenshots.push({ room, filename, sha256: crypto.createHash('sha256').update(bytes).digest('hex') });
    }
    const report = { status: 'PASS_HTML_ASSET_LOADING_ONLY', checked_at: new Date().toISOString(), image_count: images.length, images, local_links: links, screenshots, page_errors: errors, visual_inspected: false, accepted: false, limitation: 'Browser checks establish image loading and source pairing only; aesthetic and scene fidelity require actual image observation.' };
    await fs.writeFile(path.join(folder, 'html-qa.json'), JSON.stringify(report, null, 2));
    console.log(JSON.stringify({ status: report.status, images: images.length, links: links.length, screenshots: screenshots.length, visual_inspected: false }));
  } finally {
    await browser.close();
  }
}
main().catch(error => { console.error(error); process.exitCode = 1; });
