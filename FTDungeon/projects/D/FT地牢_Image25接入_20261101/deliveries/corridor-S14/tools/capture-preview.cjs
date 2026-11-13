const fs = require('node:fs/promises')
const path = require('node:path')
const { chromium } = require('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright')

async function main() {
  const root = path.resolve(__dirname, '..')
  const browser = await chromium.launch({ executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe', headless: true, args: ['--use-angle=swiftshader', '--enable-unsafe-swiftshader', '--disable-gpu-sandbox'] })
  try {
    const page = await browser.newPage({ viewport: { width: 1536, height: 1024 }, deviceScaleFactor: 1 })
    const errors = []
    const failedResources = []
    page.on('pageerror', error => errors.push(String(error)))
    page.on('response', response => { if (response.status() >= 400 && !response.url().endsWith('/favicon.ico')) failedResources.push({ url: response.url(), status: response.status() }) })
    await page.goto(process.argv[2] || 'http://127.0.0.1:5314/viewer.html', { waitUntil: 'domcontentloaded', timeout: 120000 })
    await page.waitForFunction(() => window.viewReady, null, { timeout: 120000 })
    const views = []
    for (const name of ['hero', 'north', 'south', 'west', 'east', 'envelope']) {
      views.push(await page.evaluate(name => window.renderView(name), name))
      const destination = name === 'hero' ? path.join(root, 'preview.png') : path.join(root, 'evidence', name === 'envelope' ? 'full-envelope.png' : name + '-cutaway.png')
      await page.screenshot({ path: destination })
    }
    await fs.writeFile(path.join(root, 'evidence/render-session.json'), JSON.stringify({ views, errors, failedResources, renderer: 'Lodestone 0.6.0 / ThreeStructureRenderer / local Edge SwiftShader' }, null, 2))
    if (errors.length || failedResources.length || views.some(view => !view.exactStates)) throw new Error('Preview rendering QA failed')
    console.log(JSON.stringify({ status: 'PASS', views: views.length }))
  } finally {
    await browser.close()
  }
}
main().catch(error => { console.error(error); process.exitCode = 1 })
