import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { spawn } from 'node:child_process'
import { createHash } from 'node:crypto'
import assert from 'node:assert/strict'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const { url } = JSON.parse(await fs.readFile(path.join(root, 'qa/preview-server.json'), 'utf8'))
const edge = process.env.P02_BROWSER ?? 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe'
const profile = path.join(root, 'qa/browser-profile-' + Date.now())
const log = await fs.open(path.join(root, 'qa/browser.log'), 'w')
const browser = spawn(edge, ['--headless=new', '--no-first-run', '--no-default-browser-check', '--disable-background-networking', '--disable-sync', '--disable-extensions', '--hide-scrollbars', '--enable-unsafe-swiftshader', '--use-angle=swiftshader', '--remote-debugging-port=0', '--user-data-dir=' + profile, 'about:blank'], { windowsHide: true, stdio: ['ignore', log.fd, log.fd] })
const wait = milliseconds => new Promise(resolve => setTimeout(resolve, milliseconds))
const digest = bytes => createHash('sha256').update(bytes).digest('hex')
let socket
try {
  let port
  for (let attempt = 0; attempt < 180; attempt += 1) {
    try { port = Number((await fs.readFile(path.join(profile, 'DevToolsActivePort'), 'utf8')).split(String.fromCharCode(10))[0]); break } catch { await wait(500) }
  }
  assert(port, 'Headless Edge did not expose the isolated debugging port')
  const target = await fetch('http://127.0.0.1:' + port + '/json/new?about:blank', { method: 'PUT' }).then(response => response.json())
  socket = new WebSocket(target.webSocketDebuggerUrl)
  await new Promise((resolve, reject) => { socket.addEventListener('open', resolve, { once: true }); socket.addEventListener('error', reject, { once: true }) })
  let nextId = 0
  const pending = new Map()
  const errors = []
  socket.addEventListener('message', event => {
    const message = JSON.parse(event.data)
    if (message.id) {
      const callback = pending.get(message.id)
      if (!callback) return
      clearTimeout(callback.timer)
      pending.delete(message.id)
      if (message.error) callback.reject(new Error(JSON.stringify(message.error)))
      else callback.resolve(message.result)
    }
    if (message.method === 'Runtime.exceptionThrown') errors.push(message.params.exceptionDetails)
  })
  const send = (method, params = {}) => new Promise((resolve, reject) => {
    const id = ++nextId
    const timer = setTimeout(() => { pending.delete(id); reject(new Error('CDP timeout: ' + method)) }, 180000)
    pending.set(id, { resolve, reject, timer })
    socket.send(JSON.stringify({ id, method, params }))
  })
  const evaluate = async expression => {
    const response = await send('Runtime.evaluate', { expression, returnByValue: true, awaitPromise: true })
    if (response.exceptionDetails) throw new Error(JSON.stringify(response.exceptionDetails))
    return response.result.value
  }
  await send('Page.enable')
  await send('Runtime.enable')
  await send('Emulation.setDeviceMetricsOverride', { width: 1600, height: 1000, deviceScaleFactor: 1, mobile: false })
  const receipt = { status: 'PASS', renderer: 'Minecraft Builder / Lodestone 0.6.0 / headless Edge WebGL', structure_sha256: digest(await fs.readFile(path.join(root, 'room.nbt'))), views: [] }
  const capture = async (view, source, output) => {
    const address = new URL(url)
    address.searchParams.set('view', view)
    if (source) address.searchParams.set('source', source)
    await send('Page.navigate', { url: address.href })
    let ready
    for (let attempt = 0; attempt < 240; attempt += 1) {
      ready = await evaluate('window.p02Preview ?? null')
      if (ready?.ready) break
      if (errors.length) throw new Error(JSON.stringify(errors))
      await wait(500)
    }
    assert(ready?.ready, 'Preview never became ready: ' + address)
    await wait(750)
    const { data } = await send('Page.captureScreenshot', { format: 'png', fromSurface: true, captureBeyondViewport: false })
    const bytes = Buffer.from(data, 'base64')
    assert.equal(bytes.readUInt32BE(16), 1600)
    assert.equal(bytes.readUInt32BE(20), 1000)
    await fs.writeFile(path.join(root, output), bytes)
    receipt.views.push({ file: output, sha256: digest(bytes), bytes: bytes.length, ...ready })
    console.log('Captured ' + output + ': ' + bytes.length + ' bytes')
  }
  await fs.mkdir(path.join(root, 'preview'), { recursive: true })
  for (const view of ['interior', 'isometric', 'north', 'south', 'west', 'east', 'shell', 'shell-north', 'shell-south', 'shell-west', 'shell-east', 'roof']) {
    await capture(view, null, view === 'isometric' ? 'preview.png' : 'preview/' + view + '.png')
  }
  if (process.argv.includes('--native')) {
    for (const rotation of ['none', 'clockwise_90', 'clockwise_180', 'counterclockwise_90']) {
      await capture('shell', '/qa/native-placed-' + rotation + '.nbt', 'preview/native-' + rotation + '.png')
    }
  }
  const makeSheet = async (files, output) => {
    const images = await Promise.all(files.map(async file => 'data:image/png;base64,' + (await fs.readFile(path.join(root, file))).toString('base64')))
    const expression = '(async () => { document.body.innerHTML = "<canvas id=sheet width=1600 height=1000></canvas>"; document.body.style.cssText="margin:0;background:#142023"; const context=document.getElementById("sheet").getContext("2d"); const sources=' + JSON.stringify(images) + '; for(let index=0;index<sources.length;index++){const picture=new Image();picture.src=sources[index];await picture.decode();context.drawImage(picture,(index%2)*800,Math.floor(index/2)*500,800,500);} return true; })()'
    await evaluate(expression)
    const { data } = await send('Page.captureScreenshot', { format: 'png', fromSurface: true, captureBeyondViewport: false })
    const bytes = Buffer.from(data, 'base64')
    await fs.writeFile(path.join(root, output), bytes)
    receipt.views.push({ file: output, sha256: digest(bytes), composition_sources: files })
  }
  await makeSheet(['preview/north.png', 'preview/south.png', 'preview/west.png', 'preview/east.png'], 'preview/four-view-sheet.png')
  await makeSheet(['preview/shell-north.png', 'preview/shell-south.png', 'preview/shell-west.png', 'preview/shell-east.png'], 'preview/four-shell-sheet.png')
  if (process.argv.includes('--native')) await makeSheet(['preview/native-none.png', 'preview/native-clockwise_90.png', 'preview/native-clockwise_180.png', 'preview/native-counterclockwise_90.png'], 'preview/native-placement-sheet.png')
  receipt.browser_exceptions = errors
  assert.equal(errors.length, 0)
  await fs.writeFile(path.join(root, 'qa/render.json'), JSON.stringify(receipt, null, 2) + String.fromCharCode(10))
  console.log('All requested NBT previews captured.')
} finally {
  if (socket?.readyState === WebSocket.OPEN) socket.close()
  browser.kill()
  await log.close()
}
