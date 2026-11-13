import fs from 'node:fs/promises'
import path from 'node:path'
import crypto from 'node:crypto'
import {fileURLToPath} from 'node:url'
import {createRequire} from 'node:module'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const require = createRequire('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/package.json')
const {chromium} = require('playwright')
const browser = await chromium.launch({headless: true, channel: 'msedge', args: ['--use-angle=swiftshader', '--enable-unsafe-swiftshader']})
const page = await browser.newPage({viewport: {width: 1440, height: 1040}, deviceScaleFactor: 1})
const errors = []
page.on('pageerror', error => errors.push(error.message))
await page.goto('http://127.0.0.1:51813/', {waitUntil: 'networkidle', timeout: 120000})
await page.waitForFunction(() => window.m13QA?.ready, {timeout: 120000})
const views = await page.evaluate(() => Object.keys(window.m13QA.views))
const captures = []
await fs.mkdir(path.join(root, 'qa/views'), {recursive: true})
for (const name of views) {
  const details = await page.evaluate(name => window.m13QA.setView(name), name)
  await page.waitForTimeout(200)
  const output = path.join(root, `qa/views/${name}.png`)
  const image = await page.screenshot({path: output})
  captures.push({...details, path: output, image_sha256: crypto.createHash('sha256').update(image).digest('hex'), width: 1440, height: 1040})
  console.log('Captured ' + name)
}
await fs.copyFile(path.join(root, 'qa/views/interior-north.png'), path.join(root, 'preview.png'))
await fs.writeFile(path.join(root, 'qa/captures.json'), JSON.stringify({id: 'M13', renderer: 'Lodestone 0.6.0 ThreeStructureRenderer / bundled default pack', errors, captures}, null, 2) + '\n')
await browser.close()
if (errors.length) throw new Error(errors.join('\n'))
