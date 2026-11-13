import fs from 'node:fs/promises'
import path from 'node:path'
import { createRequire } from 'node:module'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const require = createRequire('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/runtime.cjs')
const { chromium } = require('playwright')
const browser = await chromium.launch({ executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe', headless: true })
try {
  for (const mode of ['interior', 'exterior', 'eye']) {
    const directions = mode === 'eye' ? ['north', 'east'] : ['north', 'east', 'south', 'west']
    const cards = []
    for (const direction of directions) {
      const bytes = await fs.readFile(path.join(root, 'preview', `${mode}-${direction}.png`))
      cards.push(`<section><div>${mode.toUpperCase()} / ${direction.toUpperCase()}</div><img src="data:image/png;base64,${bytes.toString('base64')}"></section>`)
    }
    const page = await browser.newPage({ viewport: { width: 1440, height: mode === 'eye' ? 632 : 1212 }, deviceScaleFactor: 1 })
    await page.setContent(`<html><head><meta charset="utf-8"><style>*{box-sizing:border-box}body{margin:0;background:#172025;color:#f0e8d3;font:18px system-ui}header{height:56px;padding:14px 22px}main{display:grid;grid-template-columns:1fr 1fr;gap:2px}section{background:#263138}section div{padding:6px 16px;height:28px;font-size:14px;letter-spacing:2px}img{display:block;width:720px;height:550px}</style></head><body><header>C04 · 焦毁塌角 · ${mode === 'interior' ? '内部四向（仅预览剖切）' : mode === 'exterior' ? '完整外壳四向' : '完整结构入口视角'} · NBT 实际渲染</header><main>${cards.join('')}</main></body></html>`)
    await page.locator('img').first().waitFor()
    await page.evaluate(async () => Promise.all([...document.images].map(image => image.decode())))
    await page.screenshot({ path: path.join(root, 'preview', `${mode}-sheet.png`), fullPage: true })
    await page.close()
  }
} finally {
  await browser.close()
}
