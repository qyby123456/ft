const fs = require('node:fs')
const path = require('node:path')
const { chromium } = require('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright')

async function main() {
  const browser = await chromium.launch({ channel: 'msedge', headless: true, args: ['--use-angle=swiftshader', '--enable-unsafe-swiftshader'] })
  try {
    const page = await browser.newPage({ viewport: { width: 1400, height: 1080 } })
    const errors = []
    page.on('pageerror', error => errors.push(error.message))
    await page.goto('http://127.0.0.1:5588/?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=S8R_IMG01', { waitUntil: 'networkidle', timeout: 120000 })
    await page.waitForFunction(() => document.getElementById('loading').classList.contains('hidden'), { timeout: 120000 })
    await page.screenshot({ path: path.join(__dirname, 'lodestone-preview.png') })
    const stats = await page.locator('#build-stats').innerText()
    fs.writeFileSync(path.join(__dirname, 'qa/lodestone-browser.json'), JSON.stringify({ status: errors.length ? 'FAIL' : 'PASS', viewer_stats: stats, page_errors: errors, screenshot: 'lodestone-preview.png', source: 'actual bundled Lodestone WebGL viewer with final complete NBT; not a concept image', url: page.url() }, null, 2))
    console.log(stats)
    if (errors.length) throw new Error(errors.join('; '))
  } finally {
    await browser.close()
  }
}

main().catch(error => { console.error(error); process.exitCode = 1 })
