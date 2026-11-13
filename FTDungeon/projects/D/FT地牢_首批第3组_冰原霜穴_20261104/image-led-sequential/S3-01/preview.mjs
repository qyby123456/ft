import fs from 'node:fs/promises'
import path from 'node:path'
import { spawn } from 'node:child_process'
import { createRequire } from 'node:module'
import { fileURLToPath } from 'node:url'
import { createHash } from 'node:crypto'

const require = createRequire(import.meta.url)
const { chromium } = require('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright')
const root = path.dirname(fileURLToPath(import.meta.url))
const browser = await chromium.launch({ executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe', headless: true, args: ['--enable-unsafe-swiftshader'] })
const reports = []
try {
  for (const cutaway of [false,true]) {
    const target = path.join(root,cutaway ? 'preview-only-cutaway.nbt' : 'room.nbt')
    const child = spawn(process.execPath,['C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/preview.mjs','--nbt',target,'--port','0','--no-open'],{ windowsHide: true,stdio: ['ignore','pipe','pipe'] })
    try {
      const url = await new Promise((resolve,reject) => {
        let output = ''
        const timer = setTimeout(() => reject(new Error('Preview timeout '+output)),60000)
        child.stdout.on('data',data => {
          output += data.toString()
          const match = output.match(/Minecraft build preview: (http:\/\/[^\s]+)/)
          if (match) { clearTimeout(timer);resolve(match[1]) }
        })
        child.once('exit',code => { clearTimeout(timer);reject(new Error('Preview exit '+code)) })
      })
      const page = await browser.newPage({ viewport: { width:1600,height:1100 } })
      const errors = []
      page.on('pageerror',error => errors.push(error.message))
      await page.goto(url,{ waitUntil:'domcontentloaded',timeout:120000 })
      await page.waitForFunction(() => document.getElementById('loading')?.classList.contains('hidden'),null,{ timeout:180000 })
      await page.waitForTimeout(800)
      const destination = path.join(root,cutaway ? 'lodestone-interior.png' : 'lodestone-exterior.png')
      await page.screenshot({ path:destination })
      if (errors.length) throw new Error(errors.join('\n'))
      reports.push({ mode:cutaway?'preview-only cutaway':'complete room.nbt',status:'PASS',renderer:'Bundled Lodestone / headless Edge',actual_game_screenshot:false,errors,source:target,source_sha256:createHash('sha256').update(await fs.readFile(target)).digest('hex') })
      await page.close()
    } finally { child.kill() }
  }
  await fs.writeFile(path.join(root,'qa/lodestone.json'),JSON.stringify(reports,null,2)+'\n')
  console.log('PASS S3-01 Lodestone interior/exterior; no other reference image processed')
} finally { await browser.close() }
