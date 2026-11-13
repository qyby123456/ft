import fs from 'node:fs/promises'
import path from 'node:path'
import {spawn} from 'node:child_process'
import {fileURLToPath} from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const server = JSON.parse(await fs.readFile(path.join(root, 'qa/preview-server.json'), 'utf8'))
const profile = path.join(root, `qa/edge-profile-${Date.now()}`)
await fs.mkdir(profile, {recursive: true})
const browser = spawn('C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe', [
  '--headless=new', '--no-first-run', '--no-default-browser-check', '--disable-extensions', '--disable-features=msEdgeSidebarV2',
  '--use-angle=swiftshader', '--enable-unsafe-swiftshader', '--remote-debugging-port=0', '--window-size=1200,850', `--user-data-dir=${profile}`, 'about:blank'
], {windowsHide: true, stdio: ['ignore', 'ignore', 'ignore']})
let socket
try {
  let port
  for (let attempt = 0; attempt < 120; attempt += 1) {
    try {port = Number((await fs.readFile(path.join(profile, 'DevToolsActivePort'), 'utf8')).split('\n')[0]); break} catch {}
    await new Promise(resolve => setTimeout(resolve, 500))
  }
  if (!port) throw new Error('Headless Edge debugging endpoint not ready')
  const pages = await (await fetch(`http://127.0.0.1:${port}/json/list`)).json()
  const page = pages.find(entry => entry.type === 'page')
  socket = new WebSocket(page.webSocketDebuggerUrl)
  await new Promise((resolve, reject) => {socket.addEventListener('open', resolve, {once: true}); socket.addEventListener('error', reject, {once: true})})
  let counter = 0
  const pending = new Map()
  const errors = []
  socket.addEventListener('message', event => {
    const message = JSON.parse(event.data)
    if (message.id && pending.has(message.id)) {
      const call = pending.get(message.id)
      clearTimeout(call.timer)
      pending.delete(message.id)
      if (message.error) call.reject(new Error(JSON.stringify(message.error)))
      else call.resolve(message.result)
    } else if (message.method === 'Runtime.exceptionThrown') errors.push(message.params.exceptionDetails)
  })
  const send = (method, params = {}) => new Promise((resolve, reject) => {
    const identity = ++counter
    const timer = setTimeout(() => {pending.delete(identity); reject(new Error(`CDP timeout ${method}`))}, 180000)
    pending.set(identity, {resolve, reject, timer})
    socket.send(JSON.stringify({id: identity, method, params}))
  })
  await send('Runtime.enable')
  await send('Page.enable')
  await send('Emulation.setDeviceMetricsOverride', {width: 1200, height: 850, deviceScaleFactor: 1, mobile: false})
  await send('Page.navigate', {url: server.url})
  for (let attempt = 0; attempt < 180; attempt += 1) {
    const ready = await send('Runtime.evaluate', {expression: 'window.previewReady === true', returnByValue: true})
    if (ready.result.value) break
    if (errors.length) throw new Error(JSON.stringify(errors))
    if (attempt === 179) throw new Error('Lodestone viewer never became ready')
    await new Promise(resolve => setTimeout(resolve, 1000))
  }
  await fs.mkdir(path.join(root, 'views'), {recursive: true})
  const manifest = {renderer: '@mattzh72/lodestone 0.6.0 ThreeStructureRenderer', browser: 'Microsoft Edge headless / SwiftShader', views: []}
  for (const view of ['preview', 'interior-north', 'interior-south', 'interior-west', 'interior-east', 'exterior-north', 'exterior-south', 'exterior-west', 'exterior-east']) {
    const result = await send('Runtime.evaluate', {expression: `window.renderView(${JSON.stringify(view)})`, awaitPromise: true, returnByValue: true})
    if (result.exceptionDetails) throw new Error(JSON.stringify(result.exceptionDetails))
    const screenshot = await send('Page.captureScreenshot', {format: 'png', captureBeyondViewport: false})
    const relative = view === 'preview' ? 'preview.png' : `views/${view}.png`
    await fs.writeFile(path.join(root, relative), Buffer.from(screenshot.data, 'base64'))
    manifest.views.push({...result.result.value, path: relative})
    console.log(`RENDERED ${relative}`)
  }
  await fs.writeFile(path.join(root, 'qa/render.json'), JSON.stringify(manifest, null, 2) + '\n')
  try {await send('Browser.close')} catch {}
} finally {
  socket?.close()
  browser.kill()
}
