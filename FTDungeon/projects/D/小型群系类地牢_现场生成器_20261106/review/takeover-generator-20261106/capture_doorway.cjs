const { chromium } = require('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright');
const path = require('node:path');

async function main() {
  const browser = await chromium.launch({headless: true, args: ['--use-angle=swiftshader', '--enable-webgl']});
  try {
    for (const [label, port] of [['original', 5291], ['derived', 5292]]) {
      const page = await browser.newPage({viewport: {width: 1200, height: 900}, deviceScaleFactor: 1});
      await page.goto(`http://127.0.0.1:${port}/?name=${label}-doorway-cutaway`, {waitUntil: 'networkidle'});
      await page.waitForSelector('#loading.hidden', {timeout: 60000, state: 'attached'});
      await page.mouse.move(400, 450);
      await page.mouse.down();
      await page.mouse.move(771, 430, {steps: 30});
      await page.mouse.up();
      await page.mouse.wheel(0, -150);
      await page.waitForTimeout(1000);
      await page.screenshot({path: path.join(__dirname, `${label}-doorway-cutaway.png`)});
      await page.close();
    }
  } finally {
    await browser.close();
  }
}

main().catch(error => { process.stderr.write(String(error)); process.exitCode = 1; });
